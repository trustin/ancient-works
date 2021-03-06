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
 * @(#) $Id: IndexIteratorImpl.java 38 2004-11-14 15:20:22Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;

import net.gleamynode.oil.IndexIterator;
import net.gleamynode.oil.OilException;
import net.gleamynode.oil.impl.wal.log.IndexPutLog;
import net.gleamynode.oil.impl.wal.log.IndexRemoveLog;


/**
 * {@link IndexImpl}가 포함하고 있는 key - value 쌍을 차례대로 순회할 수 있는 iterator.
 *
 * JDBC의 {@link java.sql.ResultSet}과 동일한 방식으로 순회할 수 있다.
 *
 * <pre>
 * IndexIterator it = null;
 * try {
 *         it = idx.iterator();
 *         while (it.next()) {
 *                 System.out.println(&quot;key: &quot; + it.key() + &quot; value: &quot; + it.value());
 *         }
 * } catch (PMFException e) {
 *         e.printStackTrace();
 * } finally {
 *         if (it != null) {
 *                 try {
 *                         it.close();
 *                 } catch (PMFException e) {
 *                 }
 *                 it = null;
 *         }
 * }
 * </pre>
 *
 * <h3>Fail-fast modification check</h3>
 * 인덱스의 순회 도중에 인덱스의 내용이 수정될 경우 예외가 발생한다. 이는 Java Collections API의
 * {@link java.util.ConcurrentModificationException}이 발생하는 것과 동일한 이유에 기인한다.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 38 $, $Date: 2004-11-15 00:20:22 +0900 (�썡, 15 11�썡 2004) $
 */
class IndexIteratorImpl implements IndexIterator {
    private final WalDatabase grandparent;
    private final IndexImpl parent;
    private final ReadWriteLock parentLock;
    private final ReadWriteLock lock =
        new ReentrantWriterPreferenceReadWriteLock();
    private final LogStore store;
    private final Iterator it;
    private Entry entry;

    IndexIteratorImpl(IndexImpl parent, ReadWriteLock parentLock, Iterator it) {
        this.parent = parent;
        this.parentLock = parentLock;
        this.grandparent = parent.getParent();
        this.store = grandparent.getStore();
        this.it = it;
    }

    /**
     * Iterator 의 현재 위치를 한 쌍 앞으로 전진시키고 더 읽을 것이 있는지 리턴한다.
     *
     * @return 더 key - value 쌍이 남아있는지 여부
     */
    public boolean next() {
        boolean result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parentLock.readLock());
        SyncUtil.acquire(lock.writeLock());

        try {
            if (it.hasNext()) {
                entry = (Entry) it.next();
                result = true;
            } else {
                entry = null;
                result = false;
            }
        } finally {
            lock.writeLock().release();
            parentLock.readLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    /**
     * Iterator 가 가리키고 있는 key - value 쌍의 key 값을 리턴한다.
     *
     * @return 현재 위치의 key 값
     */
    public Object getKey() {
        Object result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parentLock.readLock());
        SyncUtil.acquire(lock.readLock());

        try {
            result = currentEntry().getKey();
        } finally {
            lock.readLock().release();
            parentLock.readLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    /**
     * Iterator 가 가리키고 있는 key - value 쌍의 value 값을 리턴한다.
     *
     * @return 현재 위치의 value 값
     */
    public Object getValue() {
        Object result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parentLock.readLock());
        SyncUtil.acquire(lock.readLock());

        try {
            result = currentEntry().getValue();
        } finally {
            lock.readLock().release();
            parentLock.readLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    /**
     * Iterator 가 가리키고 있는 key - value 쌍의 value 값을 갱신한다.
     *
     * @param newValue
     *            갱신할 값
     * @throws net.gleamynode.oil.OilException
     *             쌍의 value 를 갱신에 실패했을 경우.
     */
    public Object setValue(Object newValue) {
        Validate.notNull(newValue);

        Object result;

        grandparent.acquireSharedLock();
        SyncUtil.acquire(parentLock.writeLock());
        SyncUtil.acquire(lock.writeLock());

        try {
            Entry e = currentEntry();
            result = e.setValue(newValue);
            store.write(new IndexPutLog(parent.getId(), e.getKey(), newValue));
        } finally {
            lock.writeLock().release();
            parentLock.writeLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    public void update() {
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parentLock.readLock());
        SyncUtil.acquire(lock.writeLock());

        try {
            Entry e = currentEntry();
            store.write(new IndexPutLog(parent.getId(), e.getKey(),
                                        e.getValue()));
        } finally {
            lock.writeLock().release();
            parentLock.readLock().release();
            grandparent.releaseSharedLock();
        }
    }

    public boolean isRemoved() {
        boolean result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parentLock.readLock());
        SyncUtil.acquire(lock.readLock());
        result = entry == null;
        lock.readLock().release();
        parentLock.readLock().release();
        grandparent.releaseSharedLock();

        return result;
    }

    /**
     * Iterator 가 가리키고 있는 key - value 쌍을 삭제한다. 삭제된 이후의 {@link #getKey()}및
     * {@link #getValue()},{@link #setValue(Object)}호출은 예외를 던진다. 다음 쌍을 읽기 위해
     * {@link #next()}를 호출할 수 있다.
     *
     * @throws OilException
     *             쌍을 삭제하는 데 실패했을 때
     */
    public Object remove() {
        Object result;

        grandparent.acquireSharedLock();
        SyncUtil.acquire(parentLock.writeLock());
        SyncUtil.acquire(lock.writeLock());

        try {
            Entry e = currentEntry();
            result = e.getValue();
            store.write(new IndexRemoveLog(parent.getId(), e.getKey()));
            it.remove();
            entry = null;
        } finally {
            lock.writeLock().release();
            parentLock.writeLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    private Entry currentEntry() {
        if (entry == null) {
            throw new IllegalStateException();
        } else {
            return entry;
        }
    }
}
