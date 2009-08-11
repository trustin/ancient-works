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
 * @(#) $Id: FileLogStore.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;

import net.gleamynode.oil.IllegalPropertyException;
import net.gleamynode.oil.OilException;
import net.gleamynode.oil.impl.wal.LogStore;
import net.gleamynode.oil.impl.wal.log.Log;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;

/**
 * A default implementation of {@link LogStore} which stores logs in a file.
 * <p>
 * Properties:
 * <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><code>file</code></td>
 * <td>The path to log file.  Please note that {@link FileLogStore} also creates a
 * class catalog file whose path is <code><em>&lt;file&gt;</em>.cat</code>.  Class
 * catalog file is essential for reading logs, so please make sure that file is not
 * corrupted.</td>
 * </tr>
 * <tr>
 * <td><code>maxItemSize</code></td>
 * <td>The maximum size of a log in bytes.  {@link FileLogStore} refuses to read
 * and write a log whose size exceeds <code>maxItemSize</code>. The default value
 * is <code>1048576</code> (1 megabytes).</td>
 * </tr>
 * <tr>
 * <td><code>bufferFlushInterval</code></td>
 * <td>{@link FileLogStore} thread will flush log buffer every
 * <code>bufferFlushInterval</code> millis. The default value is <code>5000</code>
 * milliseconds.</td>
 * </tr>
 * <tr>
 * <td><code>bufferSize</code></td>
 * <td>The size of log buffer which contains logs to be flushed to the file.
 * Please note that the unit is not bytes but the number of logs.  The default value is 
 * <code>4096</code> logs.</td>
 * </tr>
 * <tr>
 * <td><code>threadName</code></td>
 * <td>The name of log buffer flusher thread. The default value is <code>FileLogStore</code>.</td>
 * </tr>
 * <tr>
 * <td><code>threadPriority</code></td>
 * <td>The priority of log buffer flusher thread. The default value is {@link Thread#NORM_PRIORITY}.</td>
 * </tr>
 * </table>
 * 
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 */
public class FileLogStore implements LogStore {
    private Properties properties = new Properties();

    private File file;

    private int bufferFlushInterval;

    private int bufferSize;

    private String threadName;

    private int threadPriority;

    private FileLogStore newStore;

    private ClassCatalog catalog;

    private final boolean useExternalCatalog;

    private FileLogReader reader;

    private FileLogWriter writer;

    private int maxItemSize = FileLogStoreConstants.DEFAULT_MAX_ITEM_SIZE;

    private final Buffer logBuf = new UnboundedFifoBuffer();

    private Flusher flusher;

    private long lastFlushTime;

    public FileLogStore() {
        this(null);
    }

    private FileLogStore(ClassCatalog catalog) {
        if (catalog != null) {
            this.catalog = catalog;
            useExternalCatalog = true;
        } else {
            useExternalCatalog = false;
        }
    }

    public Properties getProperties() {
        return (Properties) properties.clone();
    }

    public void setProperties(Properties properties) {
        this.properties = (Properties) properties.clone();
    }

    public boolean isOpen() {
        return reader != null;
    }

    public void open() {
        if (isOpen()) {
            throw new IllegalStateException();
        }

        parseProperties();

        try {
            File parentDir = file.getCanonicalFile().getParentFile();

            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
        } catch (IOException e) {
            throw new OilException("failed to get parent directory path.", e);
        }

        RandomAccessFile raf = null;

        if (!useExternalCatalog) {
            catalog = new ClassCatalog(file.getPath() + ".cat");
        }

        boolean done = false;

        try {
            raf = new RandomAccessFile(file, "rw");
            raf.seek(0L);
            reader = new FileLogReader(catalog, raf, new FileInputStream(raf
                    .getFD()), maxItemSize);
            raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            writer = new FileLogWriter(catalog, new FileOutputStream(raf
                    .getFD()), maxItemSize);

            flusher = new Flusher();
            flusher.start();
            done = true;
        } catch (IOException e) {
            throw new OilException(e);
        } finally {
            if (!done) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }

                    reader = null;
                }

                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                    }

                    writer = null;
                }

                if (!useExternalCatalog) {
                    catalog.close();
                }
            }
        }
    }

    private void parseProperties() {
        String file;
        int maxItemSize;
        int bufferFlushInterval;
        int bufferSize;
        String threadName;
        int threadPriority;

        // file
        file = properties.getProperty(FileLogStoreConstants.PROP_FILE);

        if (file == null) {
            throw new IllegalPropertyException();
        }

        // maxItemSize
        String value = properties
                .getProperty(FileLogStoreConstants.PROP_MAX_ITEM_SIZE, String
                        .valueOf(FileLogStoreConstants.DEFAULT_MAX_ITEM_SIZE));

        try {
            maxItemSize = Integer.parseInt(value);
        } catch (Exception e) {
            throw new IllegalPropertyException(e);
        }

        if (maxItemSize <= 0) {
            throw new IllegalPropertyException();
        }

        // bufferFlushInterval
        value = properties
                .getProperty(
                             FileLogStoreConstants.PROP_BUFFER_FLUSH_INTERVAL,
                             String
                                     .valueOf(FileLogStoreConstants.DEFAULT_BUFFER_FLUSH_INTERVAL));

        try {
            bufferFlushInterval = Integer.parseInt(value);
        } catch (Exception e) {
            throw new IllegalPropertyException(e);
        }

        if (bufferFlushInterval < 1) {
            throw new IllegalPropertyException();
        }

        // bufferSize
        value = properties
                .getProperty(FileLogStoreConstants.PROP_BUFFER_SIZE, String
                        .valueOf(FileLogStoreConstants.DEFAULT_BUFFER_SIZE));

        try {
            bufferSize = Integer.parseInt(value);
        } catch (Exception e) {
            throw new IllegalPropertyException(e);
        }

        if (bufferSize < 1) {
            throw new IllegalPropertyException();
        }

        // threadName
        threadName = properties
                .getProperty(FileLogStoreConstants.PROP_THREAD_NAME,
                             FileLogStoreConstants.DEFAULT_THREAD_NAME)
                .trim();

        if (threadName.length() == 0) {
            throw new IllegalPropertyException();
        }

        // threadPriority
        value = properties
                .getProperty(
                             FileLogStoreConstants.PROP_THREAD_PRIORITY,
                             String
                                     .valueOf(FileLogStoreConstants.DEFAULT_THREAD_PRIORITY));

        try {
            threadPriority = Integer.parseInt(value);
        } catch (Exception e) {
            throw new IllegalPropertyException(e);
        }

        if ((threadPriority < Thread.MIN_PRIORITY)
            || (threadPriority > Thread.MAX_PRIORITY)) {
            throw new IllegalPropertyException();
        }

        this.file = new File(file);
        this.maxItemSize = maxItemSize;
        this.bufferFlushInterval = bufferFlushInterval;
        this.bufferSize = bufferSize;
        this.threadName = threadName;
        this.threadPriority = threadPriority;
    }

    public LogStore startCompaction() {
        File newFile = new File(file.getPath() + ".new");

        if (newFile.exists() && !newFile.delete()) {
            throw new OilException("failed to delete: " + newFile);
        }

        // override file name
        Properties newProperties = (Properties) properties.clone();
        newProperties.setProperty(FileLogStoreConstants.PROP_FILE, newFile
                .getPath());

        newStore = new FileLogStore(catalog);
        newStore.setProperties(newProperties);
        newStore.open();

        return newStore;
    }

    public void finishCompaction(boolean success) {
        newStore.close();
        close();

        File newFile = newStore.file;
        newStore = null;

        if (success) {
            if (file.exists() && !file.delete()) {
                throw new OilException("failed to delete: " + file);
            }

            if (!newFile.renameTo(file)) {
                throw new OilException("failed to rename: " + newFile
                                       + " -> " + file);
            }

            open();
        } else {
            if (!newFile.delete()) {
                throw new OilException("failed to delete: " + newFile);
            }
        }
    }

    public void close() {
        if (flusher != null) {
            flusher.shutdown();
            flusher = null;
        }

        doClose();
    }

    private void doClose() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }

        reader = null;

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
            }
        }

        writer = null;

        if (!useExternalCatalog) {
            catalog.close();
        }
    }

    public Log read(boolean recover) {
        return reader.read();
    }

    public long getCurrentReadPointer() {
        return reader.getCurrentReadPointer();
    }

    public long getLastReadPointer() {
        return reader.getLastReadPointer();
    }

    public void write(Log log) {
        while (logBuf.size() > bufferSize) {
            flush();
        }

        synchronized (logBuf) {
            logBuf.add(log);
        }
    }

    public void flush() {
        flusher.flush();
    }

    private class Flusher extends Thread {
        private boolean timeToStop;

        private boolean timeToFlush;

        public Flusher() {
            super(threadName);
            setPriority(threadPriority);
        }

        public synchronized void flush() {
            timeToFlush = true;
            notify();

            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        public void shutdown() {
            timeToStop = true;
            flush();

            while (isAlive()) {
                try {
                    join();
                } catch (InterruptedException e) {
                }
            }

            // flush once again after this thread dies.
            writer.flush(logBuf);
        }

        public void run() {
            lastFlushTime = System.currentTimeMillis();

            try {
                while (!timeToStop) {
                    // sleep keeping the pace
                    long currentTime = System.currentTimeMillis();
                    long delay = bufferFlushInterval
                                 - (currentTime - lastFlushTime);

                    if (delay > 0) {
                        synchronized (this) {
                            if (!timeToFlush) {
                                try {
                                    wait(delay);
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }

                    // and flush if there are something
                    synchronized (this) {
                        lastFlushTime = System.currentTimeMillis();

                        try {
                            writer.flush(logBuf);
                        } finally {
                            timeToFlush = false;
                            notifyAll();
                        }
                    }
                }
            } catch (Exception e) {
                // fatal store error
                e.printStackTrace();
                doClose();
            }
        }
    }
}