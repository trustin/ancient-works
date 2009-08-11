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
 * @(#) $Id: Event.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;


/**
 * Represents an event that is passed among I/O controller, I/O processor, and event
 * dispatchers.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
public class Event {
    static final Event FEWER_THREADS =
        new Event(EventType.FEWER_THREADS, null, null);
    private final EventType type;
    private final Session session;
    private final Object item;

    /**
     * Creates a new instance.
     *
     * @param type
     *            the type of the event
     * @param session
     *            the session which is related with this event
     * @param item
     *            additional object related with this event ({@link Message},
     *            {@link Exception}, or <code>null</code> if none)
     */
    public Event(EventType type, Session session, Object item) {
        this.type = type;
        this.session = session;
        this.item = item;
    }

    /**
     * Returns the type of the event.
     */
    public EventType getType() {
        return type;
    }

    /**
     * Returns the session which is related with this event.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns additional object related with this event ({@link Message},
     * {@link Exception}, or <code>null</code> if none).
     */
    public Object getItem() {
        return item;
    }
}
