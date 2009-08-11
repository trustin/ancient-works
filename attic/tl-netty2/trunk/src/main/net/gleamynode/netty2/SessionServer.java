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
 * @(#) $Id: SessionServer.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2;

import java.io.IOException;

import java.net.SocketAddress;

import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.util.ArrayList;
import java.util.List;


/**
 * Listens to the TCP/IP port, accepts the incoming connections, and starts the
 * corresponding {@link Session}s. Usage:
 *
 * <pre>
 * SessionServer server = new SessionServer();
 * server.setIoProcessor(ioProcessor);
 * server.setEventDispatcher(eventDispatcher);
 * server.setMessageRecognizer(myMessageRecognizer);
 * server.setBindAddress(new InetSocketAddress(8080));
 * server.addSessionListener(mySessionListener);
 * server.start();
 * </pre>
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 */
public class SessionServer {
    private static int id = 0;
    private IoProcessor ioProcessor;
    private EventDispatcher eventDispatcher;
    private MessageRecognizer messageRecognizer;
    private SocketAddress bindAddress;
    private int backlog = 50;
    private SessionConfig defaultConfig = new SessionConfig();
    private final ArrayList sessionListeners = new ArrayList();
    private List safeSessionListeners = new ArrayList();
    private String threadName = "SessionServer-" + (++id);
    private int threadPriority = Thread.NORM_PRIORITY;
    private ServerSocketChannel ssc;
    private Worker worker;
    private boolean started;
    private boolean timeToStop;
    private ExceptionMonitor monitor = ExceptionLoggingMonitor.getInstance();

    /**
     * Creates a new instance.
     */
    public SessionServer() {
    }

    /**
     * Returns the socket address this server listens on.
     */
    public SocketAddress getBindAddress() {
        return bindAddress;
    }

    /**
     * Sets the socket address this server listens on.
     *
     * @throws IllegalStateException
     *             if the server is already running.
     */
    public synchronized void setBindAddress(SocketAddress bindAddress) {
        ensureNotStarted();
        Check.notNull(bindAddress, "bindAddress");
        this.bindAddress = bindAddress;
    }

    /**
     * Returns the backlog value of the server socket.
     */
    public int getBacklog() {
        return backlog;
    }

    /**
     * Sets the backlog value of the server socket.
     *
     * @throws IllegalStateException
     *             if the server is already running.
     */
    public synchronized void setBacklog(int backLog) {
        ensureNotStarted();

        if (backLog < 0) {
            throw new IllegalArgumentException("backLog: " + backLog);
        }

        this.backlog = backLog;
    }

    /**
     * Returns the default configuration of newly created sessions.
     */
    public SessionConfig getDefaultConfig() {
        return defaultConfig;
    }

    /**
     * Sets the default configuration of newly created sessions.
     */
    public void setDefaultConfig(SessionConfig defaultConfig) {
        Check.notNull(defaultConfig, "defaultConfig");
        this.defaultConfig = defaultConfig;
    }

    /**
     * Returns the {@link EventDispatcher}that will be passed to newly created
     * sessions' constructor.
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Sets the {@link EventDispatcher}that will be passed to newly created
     * sessions' constructor.
     *
     * @throws IllegalStateException
     *             if the server is already running.
     */
    public synchronized void setEventDispatcher(EventDispatcher eventDispatcher) {
        ensureNotStarted();
        Check.notNull(eventDispatcher, "eventDispatcher");
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * Returns the {@link IoProcessor}that will be passed to newly created
     * sessions' constructor.
     */
    public IoProcessor getIoProcessor() {
        return ioProcessor;
    }

    /**
     * Sets the {@link IoProcessor}that will be passed to newly created
     * sessions' constructor.
     *
     * @throws IllegalStateException
     *             if the server is already running.
     */
    public synchronized void setIoProcessor(IoProcessor ioProcessor) {
        ensureNotStarted();
        Check.notNull(ioProcessor, "ioProcessor");
        this.ioProcessor = ioProcessor;
    }

    /**
     * Returns the {@link MessageRecognizer}that will be passed to newly
     * created sessions' constructor.
     */
    public MessageRecognizer getMessageRecognizer() {
        return messageRecognizer;
    }

    /**
     * Sets the {@link MessageRecognizer}that will be passed to newly created
     * sessions' constructor.
     */
    public void setMessageRecognizer(MessageRecognizer messageRecognizer) {
        Check.notNull(messageRecognizer, "messageRecognizer");
        this.messageRecognizer = messageRecognizer;
    }

    /**
     * Returns the name of the server thread.
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Sets the name of the server thread.
     */
    public synchronized void setThreadName(String threadName) {
        Check.notNull(threadName, "threadName");
        this.threadName = threadName;

        if (worker != null) {
            worker.setName(threadName);
        }
    }

    /**
     * Returns the priority of the server thread.
     */
    public int getThreadPriority() {
        return threadPriority;
    }

    /**
     * Sets the priority of the server thread.
     */
    public synchronized void setThreadPriority(int threadPriority) {
        Check.threadPriority(threadPriority);
        this.threadPriority = threadPriority;

        if (worker != null) {
            worker.setPriority(threadPriority);
        }
    }

    /**
     * Returns <code>true</code> if this server is started.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Subscribe a {@link SessionListener}to receive incoming events from the
     * new session.
     */
    public synchronized void addSessionListener(SessionListener listener) {
        Check.notNull(listener, "listener");
        sessionListeners.add(listener);
        safeSessionListeners = (List) sessionListeners.clone();
    }

    /**
     * Unsubscribe a {@link SessionListener}to stop receiving incoming events
     * from the new session.
     */
    public synchronized void removeSessionListener(SessionListener listener) {
        Check.notNull(listener, "listener");
        sessionListeners.remove(listener);
        safeSessionListeners = (List) sessionListeners.clone();
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

    /**
     * Starts accepting the incoming connections.
     *
     * @throws IOException
     *             if failed to open the server socket
     * @throws IllegalStateException
     *             if some properties are not specified
     */
    public synchronized void start() throws IOException {
        if (started) {
            return;
        }

        if (ioProcessor == null) {
            throw new IllegalStateException("ioProcessor is not specified.");
        }

        if (eventDispatcher == null) {
            throw new IllegalStateException("eventDispatcher is not specified.");
        }

        if (messageRecognizer == null) {
            throw new IllegalStateException("messageRecognizer is not specified.");
        }

        if (bindAddress == null) {
            throw new IllegalStateException("bindAddress is not specified.");
        }

        ssc = ServerSocketChannel.open();
        ssc.socket().bind(bindAddress, backlog);
        ssc.socket().setReuseAddress(true);

        timeToStop = false;
        worker = new Worker();
        worker.start();

        started = true;
    }

    /**
     * Stops accepting the incoming connections.
     */
    public synchronized void stop() {
        if (!started) {
            return;
        }

        timeToStop = true;

        while (worker.isAlive()) {
            worker.interrupt();

            try {
                worker.join(1000);
            } catch (InterruptedException e) {
            }
        }

        worker = null;

        try {
            ssc.close();
        } catch (IOException e) {
            monitor.exceptionCaught(e);
        }

        ssc = null;
        started = false;
    }

    private void ensureNotStarted() {
        if (started) {
            throw new IllegalStateException("Cannot be changed while running");
        }
    }

    private class Worker extends Thread {
        public Worker() {
            super(threadName);
            setPriority(threadPriority);
        }

        public void run() {
            while (!timeToStop) {
                try {
                    startSession(ssc.accept());
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    monitor.exceptionCaught(e);

                    try {
                        // Sleep for a while (halt device, etc)
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                }
            }
        }

        private void startSession(SocketChannel ch) {
            Session s =
                new Session(ioProcessor, ch, messageRecognizer,
                            eventDispatcher, defaultConfig);
            s.setSessionListeners(safeSessionListeners);
            s.start();
        }
    }
}
