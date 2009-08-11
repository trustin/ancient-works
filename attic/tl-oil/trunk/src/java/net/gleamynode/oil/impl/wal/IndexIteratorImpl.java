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
 * {@link IndexImpl}°¡ Æ÷ÇÔÇÏ°í ÀÖ´Â key - value ½ÖÀ» Â÷·Ê´ë·Î ¼øÈ¸ÇÒ ¼ö ÀÖ´Â iterator.
 *
 * JDBCÀÇ {@link java.sql.ResultSet}°ú µ¿ÀÏÇÑ ¹æ½ÄÀ¸·Î ¼øÈ¸ÇÒ ¼ö ÀÖ´Ù.
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
 * ÀÎµ¦½ºÀÇ ¼øÈ¸ µµÁß¿¡ ÀÎµ¦½ºÀÇ ³»¿ëÀÌ ¼öÁ¤µÉ °æ¿ì ¿¹¿Ü°¡ ¹ß»ýÇÑ´Ù. ÀÌ´Â Java Collections APIÀÇ
 * {@link java.util.ConcurrentModificationException}ÀÌ ¹ß»ýÇÏ´Â °Í°ú µ¿ÀÏÇÑ ÀÌÀ¯¿¡ ±âÀÎÇÑ´Ù.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 38 $, $Date: 2004-11-15 00:20:22 +0900 (ì›”, 15 11ì›” 2004) $
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
     * Iterator ÀÇ ÇöÀç À§Ä¡¸¦ ÇÑ ½Ö ¾ÕÀ¸·Î ÀüÁø½ÃÅ°°í ´õ ÀÐÀ» °ÍÀÌ ÀÖ´ÂÁö ¸®ÅÏÇÑ´Ù.
     *
     * @return ´õ key - value ½ÖÀÌ ³²¾ÆÀÖ´ÂÁö ¿©ºÎ
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
     * Iterator °¡ °¡¸®Å°°í ÀÖ´Â key - value ½ÖÀÇ key °ªÀ» ¸®ÅÏÇÑ´Ù.
     *
     * @return ÇöÀç À§Ä¡ÀÇ key °ª
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
     * Iterator °¡ °¡¸®Å°°í ÀÖ´Â key - value ½ÖÀÇ value °ªÀ» ¸®ÅÏÇÑ´Ù.
     *
     * @return ÇöÀç À§Ä¡ÀÇ value °ª
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
     * Iterator °¡ °¡¸®Å°°í ÀÖ´Â key - value ½ÖÀÇ value °ªÀ» °»½ÅÇÑ´Ù.
     *
     * @param newValue
     *            °»½ÅÇÒ °ª
     * @throws net.gleamynode.oil.OilException
     *             ½ÖÀÇ value ¸¦ °»½Å¿¡ ½ÇÆÐÇßÀ» °æ¿ì.
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
     * Iterator °¡ °¡¸®Å°°í ÀÖ´Â key - value ½ÖÀ» »èÁ¦ÇÑ´Ù. »èÁ¦µÈ ÀÌÈÄÀÇ {@link #getKey()}¹×
     * {@link #getValue()},{@link #setValue(Object)}È£ÃâÀº ¿¹¿Ü¸¦ ´øÁø´Ù. ´ÙÀ½ ½ÖÀ» ÀÐ±â À§ÇØ
     * {@link #next()}¸¦ È£ÃâÇÒ ¼ö ÀÖ´Ù.
     *
     * @throws OilException
     *             ½ÖÀ» »èÁ¦ÇÏ´Â µ¥ ½ÇÆÐÇßÀ» ¶§
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
