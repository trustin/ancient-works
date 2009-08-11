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
package net.gleamynode.netty2;


/**
 * Dispatches session events to {@link SessionListener}.
 * <p>
 * Netty provides two default implementations:
 * <ul>
 * <li>{@link SimpleEventDispatcher}</li>
 * <li>{@link OrderedEventDispatcher}</li>
 * <li>{@link LowLatencyEventDispatcher}</li>
 * </ul>
 *
 * @author Trustin Lee (http://gleamynode.net/dev/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
public interface EventDispatcher {
    /**
     * Returns the number of session events that are still not dispatched to
     * {@link SessionListener}s.
     */
    int getWaitingEventSize();

    /**
     * Fires the specified session event.
     */
    void fire(Event event);

    /**
     * (Optional operation) Flushes the buffered events. This method is invoked
     * by {@link ReadController#processEvent(Event)}and
     * {@link WriteController#processEvent(Event)}after the expected I/O
     * operations have been completed. So you can implement
     * {@link EventDispatcher}to buffer the <code>fireXXX</code> calls and
     * fire (flush) them here in actually.
     */
    void flush();
}
