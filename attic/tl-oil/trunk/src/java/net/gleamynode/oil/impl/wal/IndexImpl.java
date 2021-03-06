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
 * @(#) $Id: IndexImpl.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;

import net.gleamynode.oil.Index;
import net.gleamynode.oil.IndexIterator;
import net.gleamynode.oil.OilException;
import net.gleamynode.oil.impl.wal.log.IndexClearLog;
import net.gleamynode.oil.impl.wal.log.IndexLog;
import net.gleamynode.oil.impl.wal.log.IndexPutLog;
import net.gleamynode.oil.impl.wal.log.IndexRemoveLog;


/**
 * key와 value 쌍으로 이루어진 데이터를 저장하고 검색하는 저장소. {@link java.util.Map}과 유사한 인터페이스를
 * 갖는다.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (�썡, 03  1�썡 2005) $
 */
class IndexImpl implements Index {
    private final Map map = new ConcurrentReaderHashMap();
    private final WalDatabase parent;
    private final LogStore store;
    private final int id;
    private final ReadWriteLock lock =
        new ReentrantWriterPreferenceReadWriteLock();

    IndexImpl(WalDatabase parentDb, int id) {
        this.parent = parentDb;
        this.store = parentDb.getStore();
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public WalDatabase getParent() {
        return parent;
    }

    public String getName() {
        return parent.getNameCatalog().getName(id);
    }

    void close() {
        SyncUtil.acquire(lock.writeLock());
        map.clear();
        lock.writeLock().release();
    }

    /**
     * 이 인덱스에서 주어진 key에 해당하는 value를 찾아 리턴한다.
     *
     * @param key
     *            검색하고자 하는 key
     * @return 주어진 key에 해당하는 value 개체. key를 찾을 수 없으면 <code>null</code>.
     */
    public Object get(Object key) {
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());

        Object value = map.get(key);
        lock.readLock().release();
        parent.releaseSharedLock();

        return value;
    }

    /**
     * 이 인덱스에 주어진 key와 value 쌍을 저장한다. 동일한 key가 이미 존재할 경우 value를 덮어씌운다.
     *
     * @param key
     *            저장하고자 하는 key
     * @param value
     *            저장하고자 하는 value
     * @throws OilException
     *             저장에 실패했을 때
     */
    public Object put(Object key, Object value) {
        Validate.notNull(key);
        Validate.notNull(value);

        Object oldValue;

        parent.acquireSharedLock();
        SyncUtil.acquire(lock.writeLock());

        try {
            oldValue = map.put(key, value);

            if (oldValue == value) {
                return value;
            }

            store.write(new IndexPutLog(id, key, value));
        } finally {
            lock.writeLock().release();
            parent.releaseSharedLock();
        }

        return oldValue;
    }

    /**
     * 이 인덱스에서 주어진 key의 존재 여부를 리턴한다.
     *
     * @param key
     *            존재 여부를 확인하고자 하는 key
     * @return 주어진 key가 인덱스에 존재하면 <code>true</code>, 그렇지 않으면
     *         <code>false</code>. 검색에 실패했을 때
     */
    public boolean containsKey(Object key) {
        Validate.notNull(key);

        boolean result;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());
        result = map.containsKey(key);
        lock.readLock().release();
        parent.releaseSharedLock();

        return result;
    }

    /**
     * 이 인덱스에서 주어진 key와 그에 해당하는 value를 삭제한다.
     *
     * @param key
     *            삭제하고자 하는 key - value 쌍의 key
     * @throws OilException
     *             삭제에 실패했을 때
     */
    public Object remove(Object key) {
        Validate.notNull(key);

        Object oldValue;

        parent.acquireSharedLock();
        SyncUtil.acquire(lock.writeLock());

        try {
            oldValue = map.remove(key);

            if (oldValue == null) {
                return null;
            }

            store.write(new IndexRemoveLog(id, key));
        } finally {
            lock.writeLock().release();
            parent.releaseSharedLock();
        }

        return oldValue;
    }

    /**
     * 이 인덱스가 담고 있는 모든 key - value 쌍을 삭제한다.
     *
     * @throws OilException
     *             인덱스 청소에 실패했을 때
     */
    public void clear() {
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.writeLock());

        try {
            map.clear();
            store.write(new IndexClearLog(id));
        } finally {
            lock.writeLock().release();
            parent.releaseSharedLock();
        }
    }

    public boolean isEmpty() {
        boolean result;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());
        result = map.isEmpty();
        lock.readLock().release();
        parent.releaseSharedLock();

        return result;
    }

    /**
     * 이 인덱스가 담고 있는 key - value 쌍의 수를 리턴한다.
     *
     * @return 이 인덱스가 담고 있는 key - value 쌍의 수
     */
    public int size() {
        int result;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());
        result = map.size();
        lock.readLock().release();
        parent.releaseSharedLock();

        return result;
    }
    
    int unsafeSize() {
        return map.size();
    }

    public IndexIterator iterator() {
        IndexIterator result;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());
        result = new IndexIteratorImpl(this, lock, map.entrySet().iterator());
        lock.readLock().release();
        parent.releaseSharedLock();

        return result;
    }

    void read(IndexLog log) {
        if (log instanceof IndexPutLog) {
            IndexPutLog putLog = (IndexPutLog) log;
            map.put(putLog.getKey(), putLog.getValue());
        } else if (log instanceof IndexRemoveLog) {
            IndexRemoveLog removeLog = (IndexRemoveLog) log;
            map.remove(removeLog.getKey());
        } else if (log instanceof IndexClearLog) {
            map.clear();
        } else {
            throw new OilException("Unexpected log: " + log);
        }
    }

    void writeAll(LogStore store, Progress progress) {
        Iterator it = map.entrySet().iterator();

        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            store.write(new IndexPutLog(id, entry.getKey(), entry.getValue()));
            progress.increase(1);
        }
    }
}
