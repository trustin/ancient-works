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
 * @(#) $Id: EventQueue.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2;

import java.util.Arrays;


/**
 * A thread-safe event queue.
 *
 * @author Trustin Lee (http://gleamynode.net/dev/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 */
class EventQueue {
    private Event[] events;
    private int first = 0;
    private int last = 0;
    private int size = 0;
    private int waitingForNewItem;

    /**
     * Construct a new, empty <code>Queue</code> with the specified initial
     * capacity.
     */
    public EventQueue(int initialCapacity) {
        events = new Event[initialCapacity];
    }

    /**
     * Clears this queue.
     */
    public synchronized void clear() {
        Arrays.fill(events, null);
        first = 0;
        last = 0;
        size = 0;
    }

    /**
     * Fetches an event entry from this queue.
     */
    public synchronized Event fetch() {
        Event e;
        waitingForNewItem++;

        while ((e = fetchNow0()) == null) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }

        waitingForNewItem--;
        return e;
    }

    public synchronized Event fetchNow() {
        return fetchNow0();
    }

    private Event fetchNow0() {
        if (size == 0) {
            return null;
        }

        Event event = events[first];
        events[first++] = null;

        if (first == events.length) {
            first = 0;
        }

        size--;
        return event;
    }

    /**
     * Enqueue into this queue.
     */
    public synchronized void push(Event event) {
        if (size == events.length) {
            // expand queue
            final int oldLen = events.length;
            Event[] newEvents = new Event[oldLen * 2];

            if (first < last) {
                System.arraycopy(events, first, newEvents, 0, last - first);
            } else {
                System.arraycopy(events, first, newEvents, 0, oldLen - first);
                System.arraycopy(events, 0, newEvents, oldLen - first, last);
            }

            first = 0;
            last = oldLen;
            events = newEvents;
        }

        events[last++] = event;

        if (last == events.length) {
            last = 0;
        }

        size++;

        if (waitingForNewItem > 0) {
            notify();
        }
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
