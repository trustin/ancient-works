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
 * key¿Í value ½ÖÀ¸·Î ÀÌ·ç¾îÁø µ¥ÀÌÅÍ¸¦ ÀúÀåÇÏ°í °Ë»öÇÏ´Â ÀúÀå¼Ò. {@link java.util.Map}°ú À¯»çÇÑ ÀÎÅÍÆäÀÌ½º¸¦
 * °®´Â´Ù.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (ì›”, 03  1ì›” 2005) $
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
     * ÀÌ ÀÎµ¦½º¿¡¼­ ÁÖ¾îÁø key¿¡ ÇØ´çÇÏ´Â value¸¦ Ã£¾Æ ¸®ÅÏÇÑ´Ù.
     *
     * @param key
     *            °Ë»öÇÏ°íÀÚ ÇÏ´Â key
     * @return ÁÖ¾îÁø key¿¡ ÇØ´çÇÏ´Â value °³Ã¼. key¸¦ Ã£À» ¼ö ¾øÀ¸¸é <code>null</code>.
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
     * ÀÌ ÀÎµ¦½º¿¡ ÁÖ¾îÁø key¿Í value ½ÖÀ» ÀúÀåÇÑ´Ù. µ¿ÀÏÇÑ key°¡ ÀÌ¹Ì Á¸ÀçÇÒ °æ¿ì value¸¦ µ¤¾î¾º¿î´Ù.
     *
     * @param key
     *            ÀúÀåÇÏ°íÀÚ ÇÏ´Â key
     * @param value
     *            ÀúÀåÇÏ°íÀÚ ÇÏ´Â value
     * @throws OilException
     *             ÀúÀå¿¡ ½ÇÆĞÇßÀ» ¶§
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
     * ÀÌ ÀÎµ¦½º¿¡¼­ ÁÖ¾îÁø keyÀÇ Á¸Àç ¿©ºÎ¸¦ ¸®ÅÏÇÑ´Ù.
     *
     * @param key
     *            Á¸Àç ¿©ºÎ¸¦ È®ÀÎÇÏ°íÀÚ ÇÏ´Â key
     * @return ÁÖ¾îÁø key°¡ ÀÎµ¦½º¿¡ Á¸ÀçÇÏ¸é <code>true</code>, ±×·¸Áö ¾ÊÀ¸¸é
     *         <code>false</code>. °Ë»ö¿¡ ½ÇÆĞÇßÀ» ¶§
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
     * ÀÌ ÀÎµ¦½º¿¡¼­ ÁÖ¾îÁø key¿Í ±×¿¡ ÇØ´çÇÏ´Â value¸¦ »èÁ¦ÇÑ´Ù.
     *
     * @param key
     *            »èÁ¦ÇÏ°íÀÚ ÇÏ´Â key - value ½ÖÀÇ key
     * @throws OilException
     *             »èÁ¦¿¡ ½ÇÆĞÇßÀ» ¶§
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
     * ÀÌ ÀÎµ¦½º°¡ ´ã°í ÀÖ´Â ¸ğµç key - value ½ÖÀ» »èÁ¦ÇÑ´Ù.
     *
     * @throws OilException
     *             ÀÎµ¦½º Ã»¼Ò¿¡ ½ÇÆĞÇßÀ» ¶§
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
     * ÀÌ ÀÎµ¦½º°¡ ´ã°í ÀÖ´Â key - value ½ÖÀÇ ¼ö¸¦ ¸®ÅÏÇÑ´Ù.
     *
     * @return ÀÌ ÀÎµ¦½º°¡ ´ã°í ÀÖ´Â key - value ½ÖÀÇ ¼ö
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
