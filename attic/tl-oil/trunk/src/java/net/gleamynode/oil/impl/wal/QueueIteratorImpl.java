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
 * @(#) $Id: QueueIteratorImpl.java 38 2004-11-14 15:20:22Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import org.apache.commons.lang.Validate;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.Sync;

import net.gleamynode.oil.Queue;
import net.gleamynode.oil.QueueIterator;
import net.gleamynode.oil.QueueReference;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 38 $, $Date: 2004-11-15 00:20:22 +0900 (ì›”, 15 11ì›” 2004) $
 */
class QueueIteratorImpl implements QueueIterator {
    private final WalDatabase grandparent;
    private final QueueImpl parent;
    private final ReadWriteLock lock =
        new ReentrantWriterPreferenceReadWriteLock();
    private final QueueExtentList extentList;
    private QueueExtent extent;
    private int nextExtentIdx;
    private int offset;

    QueueIteratorImpl(QueueImpl parent, QueueExtentList extentList) {
        this.parent = parent;
        this.grandparent = parent.getParent();
        this.extentList = extentList;
    }

    /**
     * Iterator ÀÇ ÇöÀç À§Ä¡¸¦ ÇÑ ½Ö ¾ÕÀ¸·Î ÀüÁø½ÃÅ°°í ´õ ÀÐÀ» °ÍÀÌ ÀÖ´ÂÁö ¸®ÅÏÇÑ´Ù.
     *
     * @return ´õ key - value ½ÖÀÌ ³²¾ÆÀÖ´ÂÁö ¿©ºÎ
     * @throws net.gleamynode.oil.OilException
     *             ÇöÀç À§Ä¡¸¦ ÀüÁø½ÃÅ°´Âµ¥ ½ÇÆÐÇßÀ» ¶§
     */
    public boolean next() {
        boolean result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parent.getLock().readLock());
        SyncUtil.acquire(lock.writeLock());

        try {
            QueueExtent e = extent;
outerLoop: 
            for (;;) {
                if (e == null) {
                    boolean found = false;

                    while ((e = extentList.getByIndex(nextExtentIdx)) != null) {
                        nextExtentIdx++;

                        if (e.size() > 0) {
                            offset = 0;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        this.extent = null;
                        result = false;

                        break outerLoop;
                    }
                } else {
                    offset++;
                }

                for (int i = offset; i < e.getEndOffset(); i++) {
                    if (e.get(i) != null) {
                        offset = i;
                        this.extent = e;
                        result = true;

                        break outerLoop;
                    }
                }

                e = null;
            }
        } finally {
            lock.writeLock().release();
            parent.getLock().readLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    private void tryToRemoveCurrentExtent() {
        // purge it if it is not the last one
        if (extent.tryToDiscard()) {
            extent = null;
        }
    }

    public Object getValue() {
        Object result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parent.getLock().readLock());
        SyncUtil.acquire(lock.readLock());

        try {
            result = getCurrentExtent().get(offset);
        } finally {
            lock.readLock().release();
            parent.getLock().readLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    public QueueReference getReference() {
        QueueReference result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parent.getLock().readLock());
        SyncUtil.acquire(lock.readLock());

        try {
            QueueExtent e = getCurrentExtent();

            if (e.get(offset) == null) {
                result = null;
            } else {
                result =
                    new QueueReferenceImpl(e.getId(), e.getChecksum(), offset);
            }
        } finally {
            lock.readLock().release();
            parent.getLock().readLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    public Object setValue(Object item) {
        Validate.notNull(item);

        Object result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parent.getLock().writeLock());
        SyncUtil.acquire(lock.writeLock());

        try {
            result = getCurrentExtent().set(offset, item, true);
        } finally {
            lock.writeLock().release();
            parent.getLock().writeLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    public void update() {
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parent.getLock().writeLock());
        SyncUtil.acquire(lock.writeLock());

        try {
            QueueExtent e = getCurrentExtent();
            Object value = e.get(offset);

            if (value == null) {
                throw new IllegalStateException();
            } else {
                e.set(offset, value, true);
            }
        } finally {
            lock.writeLock().release();
            parent.getLock().writeLock().release();
            grandparent.releaseSharedLock();
        }
    }

    public QueueReference moveTo(Queue queue) {
        Validate.notNull(queue);

        if (!(queue instanceof QueueImpl)) {
            throw new IllegalArgumentException();
        }

        if (this == queue) {
            throw new IllegalArgumentException();
        }

        QueueReference result;
        QueueImpl q = (QueueImpl) queue;

        if (grandparent != q.getParent()) {
            throw new IllegalArgumentException();
        }

        Sync lockA = parent.getLock().writeLock();
        Sync lockB = q.getLock().writeLock();

        // sort locks to avoid deadlocks.
        if (System.identityHashCode(lockA) > System.identityHashCode(lockB)) {
            Sync tmp = lockA;
            lockA = lockB;
            lockB = tmp;
        }

        grandparent.acquireSharedLock();
        SyncUtil.acquire(lockA);
        SyncUtil.acquire(lockB);
        SyncUtil.acquire(lock.writeLock());

        try {
            QueueExtent extent = getCurrentExtent();
            result = extent.moveTo(offset, q);

            if (result != null) {
                tryToRemoveCurrentExtent();
            }
        } finally {
            lock.writeLock().release();
            lockB.release();
            lockA.release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    public boolean isRemoved() {
        boolean result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parent.getLock().readLock());
        SyncUtil.acquire(lock.readLock());

        QueueExtent e = extent;
        result = (e == null) || (e.get(offset) == null);
        lock.readLock().release();
        parent.getLock().readLock().release();
        grandparent.releaseSharedLock();

        return result;
    }

    public Object remove() {
        Object result;
        grandparent.acquireSharedLock();
        SyncUtil.acquire(parent.getLock().writeLock());
        SyncUtil.acquire(lock.writeLock());

        try {
            QueueExtent e = getCurrentExtent();
            result = e.remove(offset);

            if (result != null) {
                tryToRemoveCurrentExtent();
            }
        } finally {
            lock.writeLock().release();
            parent.getLock().writeLock().release();
            grandparent.releaseSharedLock();
        }

        return result;
    }

    private QueueExtent getCurrentExtent() {
        QueueExtent e = extent;

        if (e == null) {
            throw new IllegalStateException();
        }

        return e;
    }
}
