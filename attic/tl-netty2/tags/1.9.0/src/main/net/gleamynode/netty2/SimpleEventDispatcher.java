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
 * A thread-pooled {@link EventDispatcher}that the events for the same session
 * does <strong>NOT </strong> occur in the same order it was generated actually.
 * That is, the events might come in random order (e.g. <code>
 * messageReceived:SecondMessage</code>,
 * <code>connectionEstablished</code>,<code>messageReceived:FirstMessage
 * </code>,
 * <code>connectionClosed</code>,<code>messageReceived:ThirdMessage</code>).
 * This dispatcher is appropriate for the case the order of event is not
 * important. If so, use {@link OrderedEventDispatcher}instead.
 * <p>
 * Set thread pool size ({@link #setThreadPoolSize(int)}) and call
 * {@link #start()}to activate this event dispatcher.
 * </p>
 * Please note that dispatcher threads will not terminate even if
 * {@link #stop()}is invoked if there are any remaining events to process for
 * this event dispatcher. They will be terminated when JVM exits because they
 * are daemon threads.
 *
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 * @author Trustin Lee (http://gleamynode.net/dev/)
 */
public class SimpleEventDispatcher extends AbstractThreadPooledEventDispatcher
    implements ThreadPooledEventDispatcher, SimpleEventDispatcherMBean {
    private final EventQueue globalEventQueue = new EventQueue(16);

    /**
     * Creates a new instance.
     */
    public SimpleEventDispatcher() {
    }

    public int getWaitingEventSize() {
        return globalEventQueue.size();
    }

    protected AbstractWorker newWorker() {
        return new Worker();
    }
    
    protected AbstractWorker removeWorker() {
        globalEventQueue.push(Event.FEWER_THREADS);
        return null; // Worker will remove itself
    }

    public synchronized void fire(Event event) {
        if (!isStarted())
            throw new IllegalStateException("not running");
        globalEventQueue.push(event);
    }

    private class Worker extends AbstractWorker {
        public Worker() {
            super(globalEventQueue);
        }

        // do nothing
        protected void onDisconnection(Session session) {
        }
        
        protected void onEnd() {
            synchronized (SimpleEventDispatcher.this) {
                workers.remove(this);
            }
        }
    }
}
