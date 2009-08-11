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
 * @(#) $Id: LowLatencyEventDispatcher.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;


/**
 * An {@link EventDispatcher}that provides low latency. This event dispatcher
 * uses the same thread with {@link IoProcessor}. So I/O operation will get
 * slow as {@link SessionListener}processes events slowly, and you'll have to
 * adjust I/O thread pool size using {@link IoProcessor#setThreadPoolSize(int)}.
 * There is no need to invoke any initialization/deinitialization methods.
 * <p>
 * Please note that I/O threads will not terminate even if
 * {@link IoProcessor#stop()}is invoked if there are any remaining events for
 * process to this event dispatcher. They will be terminated when JVM exits
 * because they are daemon threads.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
public class LowLatencyEventDispatcher implements EventDispatcher,
                                                  LowLatencyEventDispatcherMBean {
    private final EventQueue eventQueue = new EventQueue(16);

    /**
     * Creates a new low latency event dispatcher.
     */
    public LowLatencyEventDispatcher() {
    }

    /**
     * Returns <code>0</code> because this thread is not multi-threaded.
     */
    public int getWaitingEventSize() {
        return eventQueue.size();
    }

    public void fire(Event event) {
        eventQueue.push(event);
    }

    public void flush() {
        Event event;
        EventType type;

        while ((event = eventQueue.fetchNow()) != null) {
            type = event.getType();

            if (type == EventType.RECEIVED) {
                event.getSession().fireMessageReceived((Message) event.getItem());
            } else if (type == EventType.SENT) {
                event.getSession().fireMessageSent((Message) event.getItem());
            } else if (type == EventType.CONNECTED) {
                event.getSession().fireConnectionEstablished();
            } else if (type == EventType.DISCONNECTED) {
                event.getSession().fireConnectionClosed();
            } else if (type == EventType.EXCEPTION) {
                event.getSession().fireExceptionCaught((Throwable) event
                                                       .getItem());
            } else if (type == EventType.IDLE) {
                event.getSession().fireSessionIdle();
            } else {
                throw new RuntimeException("Invalid event type: " + type);
            }
        }
    }
}
