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
 * @(#) $Id: QueueExtentList.java 32 2004-11-09 14:37:16Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import java.util.Arrays;

import net.gleamynode.oil.RunRecoveryException;


/**
 * TODO Insert type comment.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 32 $, $Date: 2004-11-09 23:37:16 +0900 (화, 09 11월 2004) $
 */
class QueueExtentList {
    private QueueExtent[] map;
    private QueueExtent[] list;
    private int size;

    /**
     * Creates a new instance.
     */
    public QueueExtentList() {
        map = new QueueExtent[Constants.DEFAULT_EXTENTS];
        list = new QueueExtent[Constants.DEFAULT_EXTENTS];
    }

    public QueueExtentList(QueueExtentList el) {
        QueueExtent[] elMap = el.map;
        QueueExtent[] elList = el.list;
        int elSize = el.size;

        map = new QueueExtent[elMap.length];
        list = new QueueExtent[elSize];

        System.arraycopy(elMap, 0, map, 0, elMap.length);
        System.arraycopy(elList, 0, list, 0, elSize);
        size = elSize;
    }

    public int size() {
        return size;
    }

    public QueueExtent getById(int id) {
        if ((id < 0) || (id >= map.length)) {
            return null;
        } else {
            return map[id];
        }
    }

    public QueueExtent getByIndex(int idx) {
        if ((idx < 0) || (idx >= size)) {
            return null;
        } else {
            return list[idx];
        }
    }

    public void add(QueueExtent e) {
        int id = e.getId();
        ensureMapCapacity(id);
        ensureListCapacity();

        map[id] = e;
        list[size++] = e;
    }

    public boolean tryToRemove(QueueExtent e) {
        return tryToRemoveById(e.getId()) != null;
    }

    public QueueExtent tryToRemoveById(int id) {
        QueueExtent e = map[id];

        if ((e == null) || (e.size() > 0)) {
            return null;
        }

        final int lastIndex = size - 1;

        // try the last element first
        if (list[lastIndex] == e) {
            if (e.isPushable())
                return null;
            else {
                list[lastIndex] = null;
                size--;
                return e;
            }
        }

        for (int i = lastIndex - 1; i >= 0; i--) {
            if (list[i] == e) {
                System.arraycopy(list, i + 1, list, i, size - i - 1);
                list[lastIndex] = null;
                size--;
                map[id] = null;
                return e;
            }
        }

        return null;
    }

    public void clear() {
        Arrays.fill(map, null);
        Arrays.fill(list, 0, size, null);
        size = 0;
    }

    private void ensureMapCapacity(int newId) {
        if (newId >= map.length) {
            if (newId >= Constants.MAX_EXTENTS_PER_QUEUE) {
                throw new RunRecoveryException();
            }

            QueueExtent[] newExtentMap =
                new QueueExtent[Math.min(newId * 2,
                                         Constants.MAX_EXTENTS_PER_QUEUE)];
            System.arraycopy(map, 0, newExtentMap, 0, map.length);
            map = newExtentMap;
        }
    }

    private void ensureListCapacity() {
        if (size == list.length) {
            if (size >= Constants.MAX_EXTENTS_PER_QUEUE) {
                throw new RunRecoveryException();
            }

            QueueExtent[] newList =
                new QueueExtent[Math.min(size * 2,
                                         Constants.MAX_EXTENTS_PER_QUEUE)];
            System.arraycopy(list, 0, newList, 0, list.length);
            list = newList;
        }
    }

    public Object clone() {
        return new QueueExtentList(this);
    }
}
