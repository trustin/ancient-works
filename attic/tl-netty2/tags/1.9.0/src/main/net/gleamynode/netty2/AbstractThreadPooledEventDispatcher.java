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
 * @(#) $Id: AbstractThreadPooledEventDispatcher.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A base class for thread-pooled event dispatchers.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
public abstract class AbstractThreadPooledEventDispatcher
    implements ThreadPooledEventDispatcher {
    private String threadNamePrefix = "netty-evt";
    private boolean started;
    protected final List workers = new ArrayList();
    private int threadPoolSize = 0;
    private int threadPriority = Thread.NORM_PRIORITY;
    private int threadId = 0;

    protected AbstractThreadPooledEventDispatcher() {
    }

    public synchronized void start() {
        if (started) {
            return;
        }

        if (threadPoolSize <= 0) {
            throw new IllegalStateException("threadPoolSize is not set");
        }

        started = true;
        forkThreads(threadPoolSize);
    }

    public synchronized void stop() {
        if (!started) {
            return;
        }

        forkThreads(-threadPoolSize);
        started = false;
    }

    public boolean isStarted() {
        return started;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public synchronized void setThreadPoolSize(int newSize) {
        Check.threadPoolSize(newSize);

        if (started) {
            throw new IllegalStateException("Thread pool size cannot be changed while running.");
        }

        threadPoolSize = newSize;
    }

    private void forkThreads(int delta) {
        if (delta == 0) {
            return;
        }

        if (delta > 0) {
            for (; delta > 0; delta--) {
                workers.add(newWorker());
            }
        } else {
            for (; delta < 0; delta++) {
                AbstractWorker worker = removeWorker();
                if (worker != null)
                    workers.remove(worker);
            }
        }
    }

    protected abstract AbstractWorker newWorker();
    
    protected abstract AbstractWorker removeWorker();

    public int getThreadPriority() {
        return threadPriority;
    }

    public synchronized void setThreadPriority(int newPriority) {
        Check.threadPriority(newPriority);
        this.threadPriority = newPriority;

        if (isStarted()) {
            Iterator it = workers.iterator();

            while (it.hasNext()) {
                AbstractWorker worker = (AbstractWorker) it.next();
                worker.setPriority(newPriority);
            }
        }
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        Check.notNull(threadNamePrefix, "threadNamePrefix");
        this.threadNamePrefix = threadNamePrefix;
    }

    public void flush() {
    }

    protected abstract class AbstractWorker extends Thread {
        protected final EventQueue localEventQueue;

        protected AbstractWorker(EventQueue eventQueue) {
            super(getThreadNamePrefix() + '-' + threadId++);

            setPriority(getThreadPriority());
            setDaemon(true);
            this.localEventQueue = eventQueue;

            super.start();
        }

        public final void run() {
            Event event;
            EventType type;
            Session session;
            Object item;

            while (isStarted()) {
                event = localEventQueue.fetch();
                type = event.getType();

                if (type == EventType.FEWER_THREADS) {
                    break;
                }

                session = event.getSession();
                item = event.getItem();

                try {
                    if (type == EventType.RECEIVED) {
                        session.fireMessageReceived((Message) item);
                    } else if (type == EventType.SENT) {
                        session.fireMessageSent((Message) item);
                    } else if (type == EventType.EXCEPTION) {
                        session.fireExceptionCaught((Throwable) item);
                    } else if (type == EventType.DISCONNECTED) {
                        onDisconnection(session);
                        session.fireConnectionClosed();
                    } else if (type == EventType.CONNECTED) {
                        session.fireConnectionEstablished();
                    } else if (type == EventType.IDLE) {
                        session.fireSessionIdle();
                    } else {
                        throw new RuntimeException("Invalid event: " + type);
                    }
                } catch (Throwable t) {
                    session.fireExceptionCaught(t);
                }
            }
            
            onEnd();
        }

        protected abstract void onDisconnection(Session session);
        
        protected abstract void onEnd();
    }
}
