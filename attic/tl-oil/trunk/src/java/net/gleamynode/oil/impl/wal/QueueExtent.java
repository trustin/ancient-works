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
 * @(#) $Id: QueueExtent.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import net.gleamynode.oil.OilException;
import net.gleamynode.oil.RunRecoveryException;
import net.gleamynode.oil.impl.wal.log.QueueAddExtentLog;
import net.gleamynode.oil.impl.wal.log.QueueMoveLog;
import net.gleamynode.oil.impl.wal.log.QueuePutLog;
import net.gleamynode.oil.impl.wal.log.QueueRemoveExtentLog;
import net.gleamynode.oil.impl.wal.log.QueueRemoveLog;

/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 */
class QueueExtent {
    private static long nextChecksum = System.currentTimeMillis();

    private final WalDatabase parentDb;

    private final QueueImpl parentQueue;

    private final LogStore store;

    private final int extentId;

    private final long checksum;

    private final Object[] items;

    private final QueueExtentList extentList;

    private int endOffset;

    private int size;

    public QueueExtent(QueueImpl parentQueue, QueueExtentList extentList,
                       int extentId, int maxItems) throws OilException {
        checkMaxItems(maxItems);
        this.parentDb = parentQueue.getParent();
        this.parentQueue = parentQueue;
        this.store = parentDb.getStore();
        this.extentList = extentList;

        this.extentId = extentId;
        this.checksum = nextChecksum();
        this.items = new Object[maxItems];

        store.write(new QueueAddExtentLog(parentQueue.getId(), extentId,
                                          checksum, maxItems));
    }

    public QueueExtent(QueueImpl parentQueue, QueueExtentList extentList,
                       int extentId, long extendedId, int maxItems) {
        checkMaxItems(maxItems);
        this.parentDb = parentQueue.getParent();
        this.parentQueue = parentQueue;
        this.store = parentDb.getStore();
        this.extentList = extentList;

        this.extentId = extentId;
        this.checksum = extendedId;
        this.items = new Object[maxItems];
    }

    private static synchronized long nextChecksum() {
        return nextChecksum++;
    }

    public boolean tryToDiscard() {
        if (!extentList.tryToRemove(this)) {
            return false;
        }

        if (size != 0) {
            throw new IllegalStateException();
        }

        for (int i = items.length - 1; i >= 0; i--) {
            if (items[i] != null) {
                throw new IllegalStateException();
            }
        }

        store.write(new QueueRemoveExtentLog(parentQueue.getId(), extentId));

        return true;
    }

    private static void checkMaxItems(int maxItems) {
        if (maxItems <= 0) {
            throw new RunRecoveryException();
        }

        if (maxItems > Constants.MAX_MAX_ITEMS_PER_EXTENT) {
            throw new RunRecoveryException();
        }
    }

    public int getId() {
        return extentId;
    }

    public long getChecksum() {
        return checksum;
    }

    public int size() {
        return size;
    }

    public int getMaxItems() {
        return items.length;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public boolean isPushable() {
        return endOffset < items.length;
    }

    public int push(Object item, boolean log) {
        int offset;

        if (endOffset == items.length) {
            return -1;
        }

        offset = endOffset;
        items[endOffset] = item;
        size++;
        endOffset++;

        if (log) {
            store.write(new QueuePutLog(parentQueue.getId(), extentId,
                                        offset, item));
        }

        return offset;
    }

    public Object get(int offset) {
        if ((offset < 0) || (offset >= items.length)) {
            return null;
        } else {
            return items[offset];
        }
    }

    public Object set(int offset, Object item, boolean log)
            throws OilException {
        Object oldItem = items[offset];

        if (oldItem == null) {
            size++;
        }

        items[offset] = item;
        updateEndOffset(offset);

        if (log) {
            store.write(new QueuePutLog(parentQueue.getId(), extentId,
                                        offset, item));
        }

        return oldItem;
    }

    public boolean exists(int offset) {
        if ((offset < 0) || (offset >= items.length)) {
            return false;
        } else {
            return items[offset] != null;
        }
    }

    public Object remove(int offset) {
        return remove(offset, true);
    }

    public Object remove(int offset, boolean log) {
        Object oldValue = items[offset];

        if (oldValue == null) {
            return null;
        }

        items[offset] = null;
        size--;

        if (log) {
            store.write(new QueueRemoveLog(parentQueue.getId(), extentId,
                                           offset));
        }

        return oldValue;
    }

    public QueueReferenceImpl moveTo(int offset, QueueImpl queue) {
        // remove
        Object item = items[offset];

        if (item == null) {
            return null;
        }

        items[offset] = null;
        size--;

        // and push
        QueueReferenceImpl ref = queue.push(item, false);
        store.write(new QueueMoveLog(parentQueue.getId(), extentId, offset,
                                     queue.getId(), ref.getExtentId(), ref
                                             .getOffset()));

        return ref;
    }

    public void read(QueuePutLog log) {
        int offset = log.getOffset();
        updateEndOffset(offset);

        Object oldItem = items[offset];
        items[offset] = log.getValue();

        if (oldItem == null) {
            size++;
        }
    }

    private void updateEndOffset(int offset) {
        if ((offset + 1) > endOffset) {
            endOffset = offset + 1;
        }
    }

    public void read(QueueRemoveLog log) {
        int offset = log.getOffset();

        if (items[offset] != null) {
            items[offset] = null;
            size--;
        }
    }

    public void writeAll(LogStore store, Progress progress) {
        store.write(new QueueAddExtentLog(parentQueue.getId(), extentId,
                                          checksum, getMaxItems()));
        progress.increase(1);

        final Object[] items = this.items;
        final int len = items.length;

        for (int i = 0; i < len; i++) {
            Object item = items[i];

            if (item != null) {
                store.write(new QueuePutLog(parentQueue.getId(), extentId, i,
                                            item));
                progress.increase(1);
            }
        }
    }
}