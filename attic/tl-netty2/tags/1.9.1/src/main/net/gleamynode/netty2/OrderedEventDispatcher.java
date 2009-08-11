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

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A thread-pooled {@link EventDispatcher}that the events for the same session
 * occurs in the same order it was generated actually. It has synchronization
 * overhead compared to {@link SimpleEventDispatcher}, but it will be more
 * useful in most situations. See {@link SimpleEventDispatcher}for its
 * shortness.
 * <p>
 * Please note that {@link OrderedEventDispatcher}does not guarantee read and
 * write events are fired in order, but it does guarantee that the same type of
 * events will be fired in order:
 * </ul>
 * <li>Case 1: 'messageReceived' for the response message is fired before
 * 'messageSent' for the request message is.)</li>
 * <li>Case 2: 'messageSent' or 'exceptionCaught' event is fired after
 * 'connectionClosed' is fired.</li>
 * </ul>
 * I can't find any easy way to order read and write events for now.
 * <p>
 * Set thread pool size ({@link #setThreadPoolSize(int)}) and call
 * {@link #start()}to activate this event dispatcher.
 * </p>
 * Please note that dispatcher threads will not terminate even if
 * {@link #stop()}is invoked if there are any remaining events to process for
 * this event dispatcher. They will be terminated when JVM exits because they
 * are daemon threads.
 *
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 * @author Trustin Lee (http://gleamynode.net/dev/)
 */
public class OrderedEventDispatcher extends AbstractThreadPooledEventDispatcher
    implements ThreadPooledEventDispatcher, OrderedEventDispatcherMBean {
    private final Map sessionMap = new IdentityHashMap();
    private int nextWorkerIdx = 0;

    /**
     * Creates a new instance.
     */
    public OrderedEventDispatcher() {
    }

    public synchronized int getWaitingEventSize() {
        int sum = 0;
        Iterator it = workers.iterator();

        while (it.hasNext()) {
            Worker worker = (Worker) it.next();
            sum += worker.localEventQueue.size();
        }

        return sum;
    }

    protected AbstractWorker newWorker() {
        return new Worker();
    }

    protected synchronized AbstractWorker removeWorker() {
        AbstractWorker worker = (AbstractWorker) workers.get(workers.size() - 1);
        worker.localEventQueue.push(Event.FEWER_THREADS);
        return worker;
    }

    public synchronized void fire(Event event) {
        if (!isStarted())
            throw new IllegalStateException("not running");

        SessionInfo info = getSessionInfo(event.getSession());
        if (event.getType() == EventType.SENT) {
            info.writeWorker.localEventQueue.push(event);
        }
        else {
            info.readWorker.localEventQueue.push(event);
        }
    }

    private SessionInfo getSessionInfo(Session session) {
        SessionInfo info;

        synchronized (sessionMap) {
            info = (SessionInfo) sessionMap.get(session);

            if (info == null) {
                info = (SessionInfo) sessionMap.get(session);

                if (info == null) {
                    info = new SessionInfo();
                    sessionMap.put(session, info);
                }
            }

            if (info.readWorker == null) {
                info.readWorker = nextWorker();
            }
            if (info.writeWorker == null) {
                info.writeWorker = nextWorker();
            }
            
        }

        return info;
    }

    private synchronized Worker nextWorker() {
        int workerIdx = nextWorkerIdx++;
        nextWorkerIdx %= workers.size();
        return (Worker) workers.get(workerIdx);
    }
    
    private class Worker extends AbstractWorker {
        public Worker() {
            super(new EventQueue(16));
        }

        protected void onDisconnection(Session session) {
            synchronized (sessionMap) {
                sessionMap.remove(session);
            }
        }
        
        protected void onEnd() {
        }
    }

    private static class SessionInfo {
        public volatile int eventCount;
        public Worker readWorker;
        public Worker writeWorker;
    }
}
