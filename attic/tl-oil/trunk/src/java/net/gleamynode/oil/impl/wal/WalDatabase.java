/*
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
/*
 * @(#) $Id: WalDatabase.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import net.gleamynode.oil.Database;
import net.gleamynode.oil.IllegalPropertyException;
import net.gleamynode.oil.Index;
import net.gleamynode.oil.OilException;
import net.gleamynode.oil.ProgressMonitor;
import net.gleamynode.oil.Queue;
import net.gleamynode.oil.RunRecoveryException;
import net.gleamynode.oil.impl.wal.log.IndexLog;
import net.gleamynode.oil.impl.wal.log.Log;
import net.gleamynode.oil.impl.wal.log.NameLog;
import net.gleamynode.oil.impl.wal.log.QueueLog;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;

/**
 * A {@link Database} which stores all data into both main memory and {@link LogStore}
 * using WAL (Write-Ahead Logging) technique.
 * <p>
 * Extended properties:
 * <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><code>logStore</code></td>
 * <td>(Optional) The name of {@link LogStore} implementation class.
 * The default one is {@link net.gleamynode.oil.impl.wal.store.FileLogStore}.</td>
 * </tr>
 * <tr>
 * <td><code>logStore.<em>&lt;propName&gt;</em></td>
 * <td>The property which is passed to the {@link LogStore} you specified at
 * <code>logStore</code> property. For example, <code>logStore.file=test.db</code>
 * will be passed as <code>file=test.db</code>.  See JavaDocs of {@link LogStore}
 * implementation classes for store-specific properties.</td>
 * </tr>
 * <tr>
 * <td><code>maxItemsPerExtent</code></td>
 * <td>(Optional) The maximum number of items a queue extent can contain.
 * Queues in {@link WalDatabase} consists of multiple extents which contains
 * queue items.  {@link WalDatabase} manages the list of extents, and extents
 * manage queue items which belongs to them.  All extents are represented as
 * an fixed-size array, and <code>maxItemsPerExtent</code> becomes its length.</td>
 * </tr>
 * </table>
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 * @see LogStore
 */
public class WalDatabase implements Database {
    private boolean open;

    private LogStore store;

    private int maxItemsPerQueueExtent = Constants.DEFAULT_MAX_ITEMS_PER_EXTENT;

    private NameCatalog nameCatalog;

    private QueueImpl[] queueMap;

    private IndexImpl[] indexMap;

    private final ReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();

    private Properties properties = new Properties();

    /**
     * Creates a new persistent messaging database without store.
     */
    public WalDatabase() {
    }

    public Properties getProperties() {
        Properties result;
        SyncUtil.acquire(lock.readLock());
        result = (Properties) properties.clone();
        lock.readLock().release();

        return result;
    }

    public void setProperties(Properties properties) {
        SyncUtil.acquire(lock.writeLock());

        try {
            if (open) {
                throw new IllegalStateException();
            }

            this.properties = (Properties) properties.clone();
        } finally {
            lock.writeLock().release();
        }
    }

    /**
     * Returns the {@link LogStore} that this database is reading and writing
     * logs.
     *
     * @return <code>null</code> if the store is not set yet
     */
    LogStore getStore() {
        return store;
    }

    int getMaxItemsPerQueueExtent() {
        return maxItemsPerQueueExtent;
    }

    public void open() {
        open(null);
    }

    public void open(ProgressMonitor progressMonitor) {
        SyncUtil.acquire(lock.writeLock());

        try {
            if (open) {
                throw new IllegalStateException();
            }

            parseProperties();

            try {
                store.open();
                readLogs(false, progressMonitor);
                open = true;
            } finally {
                if (!open) {
                    if (store.isOpen()) {
                        store.close();
                    }
                }
            }
        } finally {
            lock.writeLock().release();
        }
    }

    private void parseProperties() {
        int maxItemsPerQueueExtent;
        String logStoreClassName;
        LogStore logStore;
        Properties logStoreProps;

        String value = properties
                .getProperty(Constants.PROP_MAX_ITEMS_PER_EXTENT, String
                        .valueOf(Constants.DEFAULT_MAX_ITEMS_PER_EXTENT));

        try {
            maxItemsPerQueueExtent = Integer.parseInt(value);
        } catch (Exception e) {
            throw new IllegalPropertyException(e);
        }

        if ((maxItemsPerQueueExtent < 0)
            || (maxItemsPerQueueExtent > Constants.MAX_MAX_ITEMS_PER_EXTENT)) {
            throw new IllegalPropertyException();
        }

        logStoreClassName = properties
                .getProperty(Constants.PROP_LOG_STORE,
                             Constants.DEFAULT_LOG_STORE);

        try {
            logStore = (LogStore) Class.forName(logStoreClassName)
                    .newInstance();
        } catch (Exception e) {
            throw new IllegalPropertyException(e);
        }

        Enumeration e = properties.keys();
        logStoreProps = new Properties();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            if (key.startsWith(Constants.PROP_LOG_STORE + '.')) {
                logStoreProps.setProperty(key
                        .substring(Constants.PROP_LOG_STORE.length() + 1),
                                          properties.getProperty(key));
            }
        }

        logStore.setProperties(logStoreProps);

        this.maxItemsPerQueueExtent = maxItemsPerQueueExtent;
        this.store = logStore;
    }

    /**
     * Attempts to recover this database. This method tries to ready not
     * corrupted log entries as much as possible, and then creates a new log
     * store using log compaction.
     *
     * @throws OilException
     *             if the database is already open, or log compaction is failed.
     */
    public void recover() {
        recover(null);
    }

    /**
     * Attempts to recover this database. This method tries to ready not
     * corrupted log entries as much as possible, and then creates a new log
     * store using log compaction.
     *
     * @throws OilException
     *             if the database is already open, or log compaction is failed.
     */
    public void recover(ProgressMonitor progressMonitor) {
        SyncUtil.acquire(lock.writeLock());

        try {
            if (open) {
                throw new IllegalStateException();
            }

            parseProperties();
            store.open();

            try {
                readLogs(true, progressMonitor);
                doDefragment(progressMonitor);
            } finally {
                store.close();
            }
        } finally {
            lock.writeLock().release();
        }
    }

    private void readLogs(boolean recover, ProgressMonitor monitor) {
        init();

        boolean done = false;

        int count = 0;
        Progress progress = new Progress(store.getLastReadPointer(), monitor);
        progress.fireOnStart();

        try {
            if (recover) {
                for (;;) {
                    try {
                        if (!readLogEntry(recover, progress)) {
                            break;
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            } else {
                try {
                    while (readLogEntry(recover, progress)) {
                        count++;

                        continue;
                    }
                } catch (Throwable t) {
                    throw new RunRecoveryException(t);
                }
            }

            done = true;
        } finally {
            progress.fireOnEnd();
            if (!done) {
                clear();
            }
        }
    }

    private boolean readLogEntry(boolean recover, Progress progress)
            throws Exception {
        Log log = store.read(recover);
        progress.setCurrent(store.getCurrentReadPointer());

        if (log == null) {
            return false;
        }

        if (log instanceof QueueLog) {
            QueueLog queueLog = (QueueLog) log;
            getQueue(queueLog.getQueueId()).read(queueLog);
        } else if (log instanceof IndexLog) {
            IndexLog indexLog = (IndexLog) log;
            getIndex(indexLog.getIndexId()).read(indexLog);
        } else if (log instanceof NameLog) {
            nameCatalog.read((NameLog) log);
        } else {
            throw new RunRecoveryException();
        }

        return true;
    }

    /**
     * Compacts this database. Compaction causes all database operations to
     * block until the compaction ends, and it might take a long time depending
     * on the size of the database.
     *
     * @throws OilException
     *             if failed to compact logs. If failed, the database might be
     *             closed.
     */
    public void defragment() {
        defragment(null);
    }

    /**
     * Compacts this database. Compaction causes all database operations to
     * block until the compaction ends, and it might take a long time depending
     * on the size of the database.
     *
     * @throws OilException
     *             if failed to compact logs. If failed, the database might be
     *             closed.
     */
    public void defragment(ProgressMonitor progressMonitor) {
        SyncUtil.acquire(lock.writeLock());

        try {
            ensureOpen();

            // flush the unflushed logs.
            store.flush();

            // no additional logs will be appended to store buffer until
            // defragmentation ends because we acquired an exclusive lock.
            try {
                doDefragment(progressMonitor);
            } finally {
                open = store.isOpen();
            }
        } finally {
            lock.writeLock().release();
        }
    }

    private void doDefragment(ProgressMonitor progressMonitor) {
        LogStore newStore = store.startCompaction();

        boolean done = false;

        // get total size
        long total = 0L;
        total += nameCatalog.size();
        for (int i = 0; i < queueMap.length; i++) {
            QueueImpl queue = queueMap[i];

            if (queue != null) {
                total += queue.unsafeExtentSize();
                total += queue.unsafeSize();
            }
        }

        for (int i = 0; i < indexMap.length; i++) {
            IndexImpl index = indexMap[i];

            if (index != null) {
                total += index.unsafeSize();
            }
        }

        Progress progress = new Progress(total, progressMonitor);

        progress.fireOnStart();
        // write all
        try {
            QueueImpl[] queueMap = this.queueMap;
            nameCatalog.writeAll(newStore, progress);

            for (int i = 0; i < queueMap.length; i++) {
                QueueImpl queue = queueMap[i];

                if (queue != null) {
                    queue.writeAll(newStore, progress);
                }
            }

            for (int i = 0; i < indexMap.length; i++) {
                IndexImpl index = indexMap[i];

                if (index != null) {
                    index.writeAll(newStore, progress);
                }
            }

            done = true;
        } finally {
            store.finishCompaction(done);
            progress.fireOnEnd();
        }
    }

    NameCatalog getNameCatalog() {
        SyncUtil.acquire(lock.readLock());

        try {
            ensureOpen();

            return nameCatalog;
        } finally {
            lock.readLock().release();
        }
    }

    public Queue getQueue(String name) {
        SyncUtil.acquire(lock.readLock());

        try {
            ensureOpen();

            return getQueue(nameCatalog.getId(name));
        } finally {
            lock.readLock().release();
        }
    }

    QueueImpl getQueue(int id) {
        checkId(id);

        if (id >= queueMap.length) {
            QueueImpl[] newQueueMap = new QueueImpl[Math
                    .min(id * 2, Constants.MAX_QUEUES)];
            System.arraycopy(queueMap, 0, newQueueMap, 0, queueMap.length);
            queueMap = newQueueMap;
        }

        QueueImpl queue = queueMap[id];

        if (queue == null) {
            queueMap[id] = queue = new QueueImpl(this, id);
        }

        return queue;
    }

    private void checkId(int id) {
        if (id < 0 || id > 65536) {
            throw new RunRecoveryException("Invalid queue or index ID: " + id);
        }
    }

    public Index getIndex(String name) {
        SyncUtil.acquire(lock.readLock());

        try {
            ensureOpen();

            return getIndex(nameCatalog.getId(name));
        } finally {
            lock.readLock().release();
        }
    }

    IndexImpl getIndex(int id) {
        checkId(id);

        if (id >= indexMap.length) {
            IndexImpl[] newIndexMap = new IndexImpl[Math
                    .min(id * 2, Constants.MAX_INDICES)];
            System.arraycopy(indexMap, 0, newIndexMap, 0, indexMap.length);
            indexMap = newIndexMap;
        }

        IndexImpl index = indexMap[id];

        if (index == null) {
            indexMap[id] = index = new IndexImpl(this, id);
        }

        return index;
    }

    public Set getQueueNames() {
        return getNames(queueMap);
    }

    public Set getIndexNames() {
        return getNames(indexMap);
    }

    private Set getNames(Object[] map) {
        Set result = new TreeSet();

        SyncUtil.acquire(lock.readLock());

        try {
            for (int i = map.length - 1; i >= 0; i--) {
                Object o = map[i];

                if (o != null) {
                    result.add(nameCatalog.getName(i));
                }
            }
        } finally {
            lock.readLock().release();
        }

        return result;
    }

    public boolean isOpen() {
        return open;
    }

    private void ensureOpen() {
        if (!open) {
            throw new IllegalStateException("database is not open");
        }
    }

    public void close() {
        SyncUtil.acquire(lock.writeLock());

        try {
            if (!open) {
                return;
            }

            clear();

            if (open) {
                store.close();
                open = false;
            }
        } finally {
            lock.writeLock().release();
        }
    }

    private void init() {
        nameCatalog = new NameCatalog(this);
        queueMap = new QueueImpl[Constants.DEFAULT_QUEUES];
        indexMap = new IndexImpl[Constants.DEFAULT_INDICES];
    }

    private void clear() {
        nameCatalog.close();

        for (int i = indexMap.length - 1; i >= 0; i--) {
            IndexImpl index = indexMap[i];

            if (index != null) {
                indexMap[i] = null;
                index.close();
            }
        }

        for (int i = queueMap.length - 1; i >= 0; i--) {
            QueueImpl queue = queueMap[i];

            if (queue != null) {
                queueMap[i] = null;
                queue.close();
            }
        }

        nameCatalog = null;
    }

    void acquireSharedLock() {
        SyncUtil.acquire(lock.readLock());

        if (!open || !store.isOpen()) {
            lock.readLock().release();
            throw new IllegalStateException();
        }
    }

    void releaseSharedLock() {
        lock.readLock().release();
    }
}