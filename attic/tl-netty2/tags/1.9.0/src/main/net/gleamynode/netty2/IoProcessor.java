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

import java.io.IOException;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Performs I/O operations, interprets them into session events, and passes them
 * to {@link EventDispatcher}.
 * <p>
 * {@link IoProcessor}consists of two controller threads and other worker
 * threads:
 * <ul>
 * <li><strong>Read controller </strong> forwards these events to worker
 * threads:
 * <ul>
 * <li>NIO readiness selection for OP_CONNECT and OP_READ</li>
 * <li>{@link SessionListener#sessionIdle(Session)}event</li>
 * </ul>
 * </li>
 * <li><strong>Write controller </strong> forwards these events to worker
 * thread:
 * <ul>
 * <li>{@link Session#write(Message)}request</li>
 * </ul>
 * </li>
 * <li><strong>Worker threads </strong> receive I/O (or idle) events from
 * controller threads, translate them to session events, and then pass the
 * session events to the {@link EventDispatcher}.</li>
 * </ul>
 * <p>
 * {@link IoProcessor}has properties such as:
 * <ul>
 * <li><code><strong>threadPoolSize</strong></code>: the number of worker
 * threads</li>
 * <li><code><strong>controllerThreadPriority</strong></code>: the thread
 * priority of the controller threads. Default is {@link Thread#NORM_PRIORITY}.
 * </li>
 * <li><code><strong>threadPriority</strong></code>: the thread priority of
 * the worker threads. Default is {@link Thread#NORM_PRIORITY}.</li>
 * <li><code><strong>readTries</strong></code>: the max number of read
 * tries per OP_READ event; Some NIO implementations does not read all data at
 * once. Default is <code>2</code>.</li>
 * </ul>
 * <p>
 * To activate, call {@link #start()}method.
 *
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 * @author Trustin Lee (http://gleamynode.net/dev/)
 */
public class IoProcessor implements IoProcessorMBean {
    private static final String DEFAULT_THREAD_NAME_PREFIX = "netty-io";
    private final EventQueue eventQueue = new EventQueue(16);
    private int threadId = 0;
    private int controllerThreadPriority = Thread.NORM_PRIORITY;
    private int threadPriority = Thread.NORM_PRIORITY;
    private String threadNamePrefix = DEFAULT_THREAD_NAME_PREFIX;
    private boolean started;
    private int threadPoolSize =
        Runtime.getRuntime().availableProcessors() * 2;
    private final List workers = new ArrayList();
    private Controller readController;
    private Controller writeController;
    private ExceptionMonitor monitor = ExceptionLoggingMonitor.getInstance();

    /**
     * Constructs a new instance with default properties.
     */
    public IoProcessor() {
        // in case Runtime.getRuntime().availableProcessors() does not return the positive
        if (threadPoolSize <= 0) {
            threadPoolSize = 2;
        }
    }

    /**
     * Starts all controllers and worker threads. Invoking this method has no
     * effect when this I/O processor is already started.
     *
     * @throws IOException
     *             if failed to open a {@link Selector}.
     */
    public synchronized void start() throws IOException {
        if (started) {
            return;
        }

        checkPoolSize();

        readController = new ReadController(this);
        writeController = new WriteController(this);
        readController.init();
        writeController.init();
        forkThreads(threadPoolSize);
        started = true;
    }

    /**
     * Stops all controllers and worker threads. Invoking this method has no
     * effect when this I/O processor is already stopped.
     */
    public synchronized void stop() {
        if (!started) {
            return;
        }

        started = false;
        forkThreads(-threadPoolSize);
        readController.startDestroy();
        writeController.startDestroy();
        readController.finishDestroy();
        writeController.finishDestroy();
        readController = null;
        writeController = null;
    }

    /**
     * Returns <code>true</code> only if this I/O processor is started.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Returns the number of worker threads.
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * Sets the number of worker threads. The number of worker threads is also
     * adjustable in runtime.
     */
    public synchronized void setThreadPoolSize(int newSize) {
        Check.threadPoolSize(newSize);

        if (started) {
            forkThreads(newSize - threadPoolSize);
        }

        threadPoolSize = newSize;
    }

    /**
     * Returns the priority of controller threads.
     */
    public int getControllerThreadPriority() {
        return controllerThreadPriority;
    }

    /**
     * Sets the priority of controller threads. The default value is
     * {@link Thread#NORM_PRIORITY}.
     *
     * @throws IllegalArgumentException
     *             if the specified priority is not between
     *             {@link Thread#MIN_PRIORITY}and {@link Thread#MAX_PRIORITY}.
     */
    public void setControllerThreadPriority(int newPriority) {
        Check.threadPriority(newPriority);
        this.controllerThreadPriority = newPriority;

        if (started) {
            readController.setThreadPriority(newPriority);
            writeController.setThreadPriority(newPriority);
        }
    }

    /**
     * Returns the priority of worker threads.
     */
    public int getThreadPriority() {
        return threadPriority;
    }

    /**
     * Sets the priority of worker threads. The default value is
     * {@link Thread#NORM_PRIORITY}.
     *
     * @throws IllegalArgumentException
     *             if the specified priority is not between
     *             {@link Thread#MIN_PRIORITY}and {@link Thread#MAX_PRIORITY}.
     */
    public synchronized void setThreadPriority(int newPriority) {
        Check.threadPriority(newPriority);
        this.threadPriority = newPriority;

        if (started) {
            Iterator it = workers.iterator();

            while (it.hasNext()) {
                Worker worker = (Worker) it.next();
                worker.setPriority(newPriority);
            }
        }
    }

    /**
     * Returns the maximum number of read tries per {@link SelectionKey#OP_READ}
     * event.
     *
     * @deprecated Netty2 now automatically retries a read operation until it
     *             returns 0.
     */
    public int getReadTries() {
        return Integer.MAX_VALUE;
    }

    /**
     * Sets the maximum number of read tries per {@link SelectionKey#OP_READ}
     * event. This property is adjustable in runtime.
     *
     * @deprecated Netty2 now automatically retries a read operation until it
     *             returns 0.
     * @throws IllegalArgumentException
     *             if the specified value is not greater than <code>0</code>.
     */
    public void setReadTries(int readTries) {
    }

    /**
     * Returns the prefix of the I/O thread name.
     */
    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    /**
     * Sets the prefix of the I/O thread name. The actual thread name will be
     * <code><em>threadNamePrefix</em> + '-' + threadId</code>.
     */
    public void setThreadNamePrefix(String threadNamePrefix) {
        Check.notNull(threadNamePrefix, "threadNamePrefix");
        this.threadNamePrefix = threadNamePrefix;
    }

    /**
     * Returns the {@link ExceptionMonitor}.
     */
    public ExceptionMonitor getExceptionMonitor() {
        return monitor;
    }

    /**
     * Sets the {@link ExceptionMonitor}.  Any uncaught exceptions will be
     * forwarded to the specified {@link ExceptionMonitor}
     * 
     * @throws NullPointerException if <code>monitor</code> is <code>null</code>.
     */
    public void setExceptionMonitor(ExceptionMonitor monitor) {
        Check.notNull(monitor, "exception monitor");
        this.monitor = monitor;
    }

    Controller getReadController() {
        return readController;
    }

    Controller getWriteController() {
        return writeController;
    }

    void push(Event event) {
        ensureStarted();
        eventQueue.push(event);
    }

    void notifyWriteRequest(Session session) {
        ensureStarted();
        writeController.addSession(session);
    }

    void notifyEstablishedSession(Session session) {
        ensureStarted();
        readController.addSession(session);
    }

    private void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("IoProcessor is not started.");
        }
    }

    private void forkThreads(int delta) {
        if (delta == 0) {
            return;
        }

        if (delta > 0) {
            for (; delta > 0; delta--) {
                new Worker();
            }
        } else {
            for (; delta < 0; delta++) {
                eventQueue.push(Event.FEWER_THREADS);
            }
        }
    }

    private void process(Event event) {
        Controller controller;

        if (writeController.isProcessable(event)) {
            controller = writeController;
        } else if (readController.isProcessable(event)) {
            controller = readController;
        } else {
            throw new RuntimeException("unknown event: " + event);
        }

        controller.processEvent(event);
    }

    private void checkPoolSize() {
        Check.threadPoolSize(threadPoolSize);
    }

    private class Worker extends Thread {
        public Worker() {
            super(threadNamePrefix + '-' + threadId++);
            setPriority(threadPriority);
            setDaemon(true);

            synchronized (IoProcessor.this) {
                workers.add(this);
            }

            super.start();
        }

        public void run() {
            Event event;

            do {
                event = eventQueue.fetch();

                if (event.getType() == EventType.FEWER_THREADS) {
                    break;
                }

                process(event);
            } while (started);

            synchronized (IoProcessor.this) {
                workers.remove(this);
            }
        }
    }
}
