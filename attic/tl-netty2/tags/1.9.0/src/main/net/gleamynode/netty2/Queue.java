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
 * @(#) $Id: Queue.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;

import java.util.Arrays;


/**
 * <p>
 * A simple queue class. This class is <b>NOT </b> thread-safe.
 * </p>
 *
 * @author Trustin Lee (http://gleamynode.net/dev/)
 *         href="http://projects.gleamynode.net/">http://projects.gleamynode.net/
 *         </a>)
 *
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
class Queue {
    private Object[] items;
    private int first = 0;
    private int last = 0;
    private int size = 0;
    private boolean open = false;

    /**
     * Construct a new, empty <code>Queue</code> with the specified initial
     * capacity.
     */
    public Queue(int initialCapacity) {
        items = new Object[initialCapacity];
    }

    public void open() {
        clear();
        open = true;
    }

    public void close() {
        open = false;
        clear();
    }

    /**
     * Clears this queue.
     */
    private void clear() {
        Arrays.fill(items, null);
        first = 0;
        last = 0;
        size = 0;
    }

    /**
     * Dequeues from this queue.
     *
     * @return <code>null</code>, if this queue is empty or the element is
     *         really <code>null</code>.
     */
    public Object pop() {
        if (size == 0) {
            return null;
        }

        Object ret = items[first];
        items[first] = null;
        first = (first + 1) % items.length;

        size--;

        return ret;
    }

    /**
     * Enqueue into this queue.
     */
    public boolean push(Object obj) {
        if (!open) {
            return false;
        }

        if (size == items.length) {
            // expand queue
            final int oldLen = items.length;
            Object[] tmp = new Object[oldLen * 2];

            if (first < last) {
                System.arraycopy(items, first, tmp, 0, last - first);
            } else {
                System.arraycopy(items, first, tmp, 0, oldLen - first);
                System.arraycopy(items, 0, tmp, oldLen - first, last);
            }

            first = 0;
            last = oldLen;
            items = tmp;
        }

        items[last] = obj;
        last = (last + 1) % items.length;
        size++;
        return true;
    }

    /**
     * Returns the first element of the queue.
     *
     * @return <code>null</code>, if the queue is empty, or the element is
     *         really <code>null</code>.
     */
    public Object first() {
        if (!open) {
            return null;
        }

        if (size == 0) {
            return null;
        }

        return items[first];
    }

    /**
     * Returns <code>true</code> if the queue is empty.
     */
    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Returns the number of elements in the queue.
     */
    public int size() {
        return size;
    }
}
