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
 * @(#) $Id: QueueImpl.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import net.gleamynode.oil.OilException;
import net.gleamynode.oil.Queue;
import net.gleamynode.oil.QueueIterator;
import net.gleamynode.oil.QueueReference;
import net.gleamynode.oil.RunRecoveryException;
import net.gleamynode.oil.impl.wal.log.QueueAddExtentLog;
import net.gleamynode.oil.impl.wal.log.QueueClearLog;
import net.gleamynode.oil.impl.wal.log.QueueLog;
import net.gleamynode.oil.impl.wal.log.QueueMoveLog;
import net.gleamynode.oil.impl.wal.log.QueuePutLog;
import net.gleamynode.oil.impl.wal.log.QueueRemoveExtentLog;
import net.gleamynode.oil.impl.wal.log.QueueRemoveLog;

import org.apache.commons.lang.Validate;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.Sync;


/**
 * Å¥ ÀÚ·á±¸Á¶¸¦ µû¸£´Â °³Ã¼ ÀúÀå¼Ò¸¦ Ç¥ÇöÇÑ´Ù.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (ì›”, 03  1ì›” 2005) $
 */
class QueueImpl implements Queue {
    private final WalDatabase parent;
    private final LogStore store;
    private final int id;
    private final ReadWriteLock lock =
        new ReentrantWriterPreferenceReadWriteLock();
    private final QueueExtentList extentList = new QueueExtentList();

    QueueImpl(WalDatabase parent, int id) {
        this.parent = parent;
        this.store = parent.getStore();
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

    ReadWriteLock getLock() {
        return lock;
    }

    void close() {
        SyncUtil.acquire(lock.writeLock());
        extentList.clear();
        lock.writeLock().release();
    }

    /**
     * Á¦½ÃÇÑ °³Ã¼¸¦ ÀÌ Å¥ÀÇ ¸Ç µÚ¿¡ Ãß°¡ÇÑ´Ù.
     *
     * @param item
     *            ÀÌ Å¥¿¡ Ãß°¡ÇÒ °³Ã¼
     * @return Ãß°¡ÇÑ Ç×¸ñÀÇ Å¥ »óÀÇ À§Ä¡¸¦ °¡¸®Å°´Â ÂüÁ¶
     * @throws OilException
     *             Ãß°¡°¡ ½ÇÆÐÇßÀ» ¶§
     * @see QueueReferenceImpl
     */
    public QueueReference push(Object item) {
        return push(item, true);
    }

    QueueReferenceImpl push(Object item, boolean log) {
        Validate.notNull(item);

        QueueReferenceImpl newRef;

        parent.acquireSharedLock();
        SyncUtil.acquire(lock.writeLock());

        try {
            QueueExtent extent = getPushableExtent();
            int offset = extent.push(item, log);
            newRef =
                new QueueReferenceImpl(extent.getId(), extent.getChecksum(),
                                       offset);
        } finally {
            lock.writeLock().release();
            parent.releaseSharedLock();
        }

        return newRef;
    }

    /**
     * Å¥ ÂüÁ¶°¡ °¡¸®Å°´Â °³Ã¼¸¦ ÀÐ¾îµéÀÎ´Ù.
     *
     * @param reference
     *            Å¥ »óÀÇ À§Ä¡¸¦ °¡¸®Å°´Â ÂüÁ¶
     * @return ÁÖ¾îÁø ÂüÁ¶¿¡ À§Ä¡ÇÑ °³Ã¼. °³Ã¼°¡ »èÁ¦µÇ¾úÀ¸¸é <code>null</code>.
     */
    public Object get(QueueReference reference) {
        Validate.notNull(reference);

        Object result;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());

        try {
            QueueReferenceImpl ref = (QueueReferenceImpl) reference;
            QueueExtent extent = extentList.getById(ref.getExtentId());

            if (extent == null) {
                result = null;
            } else if (extent.getChecksum() != ref.getChecksum()) {
                result = null;
            } else {
                result = extent.get(ref.getOffset());
            }
        } finally {
            lock.readLock().release();
            parent.releaseSharedLock();
        }

        return result;
    }

    /**
     * Å¥ ÂüÁ¶°¡ °¡¸®Å°´Â °³Ã¼ÀÇ Á¸Àç ¿©ºÎ¸¦ ¸®ÅÏÇÑ´Ù.
     *
     * @param reference
     *            Å¥ »óÀÇ À§Ä¡¸¦ °¡¸®Å°´Â ÂüÁ¶
     * @return ÁÖ¾îÁø ÂüÁ¶¿¡ °³Ã¼°¡ Á¸ÀçÇÏ¸é <code>true</code>. »èÁ¦µÇ¾úÀ¸¸é <code>false</code>.
     */
    public boolean exists(QueueReference reference) {
        Validate.notNull(reference);

        boolean result;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());

        try {
            QueueReferenceImpl ref = (QueueReferenceImpl) reference;
            QueueExtent extent = extentList.getById(ref.getExtentId());

            if (extent == null) {
                result = false;
            } else if (extent.getChecksum() != ref.getChecksum()) {
                result = false;
            } else {
                result = extent.exists(ref.getOffset());
            }
        } finally {
            lock.readLock().release();
            parent.releaseSharedLock();
        }

        return result;
    }

    /**
     * Å¥ ÂüÁ¶°¡ °¡¸®Å°´Â °³Ã¼¸¦ »èÁ¦ÇÑ´Ù.
     *
     * @param reference
     *            Å¥ »óÀÇ À§Ä¡¸¦ °¡¸®Å°´Â ÂüÁ¶
     * @return °³Ã¼°¡ »èÁ¦µÇ¾úÀ¸¸é <code>true</code>. ÀÌ¹Ì »èÁ¦µÇ¾úÀ¸¸é <code>false</code>.
     * @throws OilException
     *             »èÁ¦¿¡ ½ÇÆÐÇßÀ» ¶§
     */
    public Object remove(QueueReference reference) {
        Validate.notNull(reference);

        Object result;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.writeLock());

        try {
            QueueReferenceImpl ref = (QueueReferenceImpl) reference;
            QueueExtent extent = extentList.getById(ref.getExtentId());

            if (extent == null) {
                result = null;
            } else if (extent.getChecksum() != ref.getChecksum()) {
                result = null;
            } else {
                result = extent.remove(ref.getOffset());

                if (result != null) {
                    extent.tryToDiscard();
                }
            }
        } finally {
            lock.writeLock().release();
            parent.releaseSharedLock();
        }

        return result;
    }

    public QueueReference moveTo(QueueReference reference, Queue queue)
                          throws OilException {
        if (!(reference instanceof QueueReferenceImpl)) {
            throw new IllegalArgumentException();
        }

        if (!(queue instanceof QueueImpl)) {
            throw new IllegalArgumentException();
        }

        if (this == queue) {
            throw new IllegalArgumentException();
        }

        QueueReference result;
        QueueReferenceImpl ref = (QueueReferenceImpl) reference;
        QueueImpl q = (QueueImpl) queue;

        if (parent != q.parent) {
            throw new IllegalArgumentException();
        }

        Sync lockA = lock.writeLock();
        Sync lockB = q.lock.writeLock();

        // sort locks to avoid deadlocks.
        if (System.identityHashCode(lockA) > System.identityHashCode(lockB)) {
            Sync tmp = lockA;
            lockA = lockB;
            lockB = tmp;
        }

        parent.acquireSharedLock();
        SyncUtil.acquire(lockA);
        SyncUtil.acquire(lockB);

        try {
            QueueExtent extent = extentList.getById(ref.getExtentId());

            if (extent == null) {
                result = null;
            } else if (extent.getChecksum() != ref.getChecksum()) {
                result = null;
            } else {
                result = extent.moveTo(ref.getOffset(), q);

                if (result != null) {
                    extent.tryToDiscard();
                }
            }
        } finally {
            lockB.release();
            lockA.release();
            parent.releaseSharedLock();
        }

        return result;
    }

    public boolean isEmpty() {
        /**
           boolean result;
           parent.acquireSharedLock();
           SyncUtil.acquire(lock.readLock());
           int nExtents = extentList.size();
           switch (nExtents) {
           case 0:
               result = true;
               break;
           case 1:
               result = extentList.getByIndex(0).size() == 0;
               break;
           default:
               result = false;
           }
           lock.readLock().release();
           parent.releaseSharedLock();
           return result;
         */
        int size = 0;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());

        try {
            for (int i = extentList.size() - 1; i >= 0; i--) {
                size += extentList.getByIndex(i).size();
            }

            if ((size == 0) && (extentList.size() > 1)) {
                System.out.println("Size is 0 with invalid extentList.size (" +
                                   extentList.size() + ")");

                for (int i = extentList.size() - 1; i >= 0; i--) {
                    System.out.println(extentList.getByIndex(i).size());
                }
            }
        } finally {
            lock.readLock().release();
            parent.releaseSharedLock();
        }

        return size == 0;
    }

    /**
     * ÀÌ Å¥°¡ ´ã°í ÀÖ´Â °³Ã¼ÀÇ ¼ö¸¦ ¸®ÅÏÇÑ´Ù.
     *
     * @return ÀÌ Å¥°¡ ´ã°í ÀÖ´Â °³Ã¼ÀÇ ¼ö
     */
    public int size() {
        int size = 0;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());

        try {
            for (int i = extentList.size() - 1; i >= 0; i--) {
                size += extentList.getByIndex(i).size();
            }
        } finally {
            lock.readLock().release();
            parent.releaseSharedLock();
        }

        return size;
    }
    
    int unsafeExtentSize() {
        return extentList.size();
    }
    
    int unsafeSize() {
        int size = 0;
        for (int i = extentList.size() - 1; i >= 0; i--) {
            size += extentList.getByIndex(i).size();
        }
        return size;
    }

    /**
     * ÀÌ Å¥°¡ ´ã°í ÀÖ´Â ³»¿ëÀ» ¸ðµÎ ºñ¿î´Ù
     *
     * @throws OilException
     *             ºñ¿ì´Â µ¥ ½ÇÆÐÇßÀ» ¶§
     */
    public void clear() {
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.writeLock());

        try {
            extentList.clear();
            store.write(new QueueClearLog(id));
        } finally {
            lock.writeLock().release();
            parent.releaseSharedLock();
        }
    }

    public QueueIterator iterator() {
        QueueIterator result;
        parent.acquireSharedLock();
        SyncUtil.acquire(lock.readLock());
        result = new QueueIteratorImpl(this, new QueueExtentList(extentList));
        lock.readLock().release();
        parent.releaseSharedLock();

        return result;
    }

    private QueueExtent getPushableExtent() {
        int extentListSize = extentList.size();

        if (extentListSize == 0) {
            return addExtent();
        } else {
            QueueExtent e = extentList.getByIndex(extentListSize - 1);

            if (e.isPushable()) {
                return e;
            } else {
                e.tryToDiscard();

                return addExtent();
            }
        }
    }

    private QueueExtent addExtent() {
        for (int newId = 0; newId <= Constants.MAX_EXTENTS_PER_QUEUE;
                 newId++) {
            if (extentList.getById(newId) == null) {
                QueueExtent newExtent =
                    new QueueExtent(this, extentList, newId,
                                    parent.getMaxItemsPerQueueExtent());

                extentList.add(newExtent);
                return newExtent;
            }
        }

        throw new OilException("insufficient extent ids");
    }

    void read(QueueLog log) {
        QueueExtent extent;

        if (log instanceof QueuePutLog) {
            QueuePutLog putLog = (QueuePutLog) log;
            extent = extentList.getById(putLog.getExtentId());

            if (extent == null) {
                throw new RunRecoveryException();
            }

            extent.read(putLog);
        } else if (log instanceof QueueRemoveLog) {
            QueueRemoveLog removeLog = (QueueRemoveLog) log;
            extent = extentList.getById(removeLog.getExtentId());

            if (extent == null) {
                throw new RunRecoveryException();
            }

            extent.read(removeLog);
        } else if (log instanceof QueueMoveLog) {
            QueueMoveLog moveLog = (QueueMoveLog) log;
            read(moveLog);
        } else if (log instanceof QueueAddExtentLog) {
            QueueAddExtentLog addExtentLog = (QueueAddExtentLog) log;
            int extentId = addExtentLog.getExtentId();
            extent =
                new QueueExtent(this, extentList, extentId,
                                addExtentLog.getExtentChecksum(),
                                addExtentLog.getMaxItems());
            extentList.add(extent);
        } else if (log instanceof QueueRemoveExtentLog) {
            QueueRemoveExtentLog removeExtentLog = (QueueRemoveExtentLog) log;
            int extentId = removeExtentLog.getExtentId();

            if (extentList.tryToRemoveById(extentId) == null) {
                throw new RunRecoveryException();
            }
        } else if (log instanceof QueueClearLog) {
            extentList.clear();
        } else {
            throw new RunRecoveryException();
        }
    }

    private void read(QueueMoveLog log) {
        int sourceExtentId = log.getExtentId();
        int sourceOffset = log.getOffset();
        int targetQueueId = log.getTargetQueueId();
        int targetExtentId = log.getTargetExtentId();
        int targetOffset = log.getTargetOffset();

        QueueExtent sourceExtent = extentList.getById(sourceExtentId);

        if (sourceExtent == null) {
            throw new RunRecoveryException();
        }

        Object item = sourceExtent.get(sourceOffset);

        if (item == null) {
            throw new RunRecoveryException();
        }

        QueueImpl targetQueue = parent.getQueue(targetQueueId);

        if (targetQueue == null) {
            throw new RunRecoveryException();
        }

        sourceExtent.remove(sourceOffset, false);

        QueueExtent targetExtent =
            targetQueue.extentList.getById(targetExtentId);

        if (targetExtent == null) {
            throw new RunRecoveryException();
        }

        targetExtent.set(targetOffset, item, false);
    }

    void writeAll(LogStore store, Progress progress) {
        for (int i = extentList.size() - 1; i >= 0; i--) {
            QueueExtent extent = extentList.getByIndex(i);
            extent.writeAll(store, progress);
        }
    }
}
