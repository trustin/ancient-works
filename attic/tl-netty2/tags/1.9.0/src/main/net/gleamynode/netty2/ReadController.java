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
 * @(#) $Id: ReadController.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;

import java.io.IOException;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.nio.ByteBuffer;
import java.nio.channels.*;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
class ReadController extends Controller implements Runnable {
    private final Queue sessionQueue = new Queue(16);

    private final IoProcessor ioProcessor;

    private final Selector selector;

    private Thread thread;

    private volatile boolean waitingForCompletion;

    private volatile int remainingRequests;

    private long lastIdleCheckTime = System.currentTimeMillis();

    private boolean timeToStop;

    public ReadController(IoProcessor ioProcessor) throws IOException {
        this.ioProcessor = ioProcessor;
        selector = Selector.open();
        sessionQueue.open();
    }

    public void setThreadPriority(int newPriority) {
        if ((thread != null) && thread.isAlive()) {
            thread.setPriority(newPriority);
        }
    }

    public void init() {
        thread = new Thread(this, ioProcessor.getThreadNamePrefix() + "-rc");
        thread.setPriority(ioProcessor.getControllerThreadPriority());
        thread.start();
    }

    public void startDestroy() {
        timeToStop = true;
        selector.wakeup();
    }

    public void finishDestroy() {
        while (thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public void addSession(Session session) {
        synchronized (sessionQueue) {
            sessionQueue.push(session);
        }

        selector.wakeup();
    }

    public void notifyOpWrite(Session session) {
        selector.wakeup();
        session.getSelectionKey().interestOps(
                SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public void run() {
        while (!timeToStop) {
            try {
                int nKeys = selector.select(1000);

                if (timeToStop) {
                    break;
                }

                processIdleOrTimedOutSessions();
                processNewSessions();
                processReadySessions(nKeys);

                waitForCompletion();
            } catch (IOException ioe) {
                // ignore interrupted system call, halt devices, ...
                ioProcessor.getExceptionMonitor().exceptionCaught(ioe);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } catch (Throwable t) {
                ioProcessor.getExceptionMonitor().exceptionCaught(t);
            }
        }
    }

    private void processNewSessions() {
        Session session;

        if (sessionQueue.size() > 0) {
            synchronized (sessionQueue) {
                while ((session = (Session) sessionQueue.pop()) != null) {
                    try {
                        if (session.isClosing()) {
                            processClosingSession(session);
                        } else {
                            processOpeningSession(session);
                        }
                    } catch (IOException ioe) {
                        increaseRemainingRequests();
                        ioProcessor.push(new Event(EventType.EXCEPTION,
                                session, ioe));
                    }
                }
            }
        }
    }

    private void processClosingSession(Session session) {
        increaseRemainingRequests();
        ioProcessor.push(session.EVENT_CLOSE_REQUEST);
    }

    private void processOpeningSession(Session session) throws SocketException,
            IOException, ClosedChannelException {
        Selector selector = this.selector;
        SocketChannel channel;
        channel = session.getChannel();

        if (channel != null) {
            if (session.getSelectionKey() != null) {
                // OP_WRITE registration
                session.getSelectionKey().interestOps(
                        SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } else {
                // start an already connected new session
                Socket s = channel.socket();
                s.setKeepAlive(true);
                channel.configureBlocking(false);
                pushConnected(session, channel, selector);
            }
        } else {
            // start to connect a non-connected new session
            channel = SocketChannel.open();

            Socket s = channel.socket();
            s.setKeepAlive(true);
            channel.configureBlocking(false);

            boolean connected;
            boolean done = false;

            try {
                connected = channel.connect(session.getSocketAddress());
                done = true;
            } finally {
                if (done) {
                    session.setChannel(channel, false);
                } else {
                    session.setChannel(null, false);
                }
            }

            if (connected) { // immediate establishment
                pushConnected(session, channel, selector);
            } else { // non-immediate one
                channel.register(selector, SelectionKey.OP_CONNECT, session);
                session.setSelectionKey(channel.keyFor(selector));
            }
        }
    }

    private void pushConnected(Session session, SocketChannel channel,
            Selector selector) throws IOException {
        channel.register(selector, SelectionKey.OP_READ, session);
        session.setSelectionKey(channel.keyFor(selector));
        increaseRemainingRequests();
        ioProcessor.push(session.EVENT_CONNECTED);
    }

    private void processIdleOrTimedOutSessions() {
        Set keys = selector.keys();
        Set selectedKeys = selector.selectedKeys();
        Iterator it;
        Session session;
        SessionConfig config;

        // process idle sessions
        long currentTime = System.currentTimeMillis();

        if ((keys != null) && ((currentTime - lastIdleCheckTime) >= 1000)) {
            lastIdleCheckTime = currentTime;
            it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                session = (Session) key.attachment();

                config = session.getConfig();

                if (!session.getChannel().isConnected()) {
                    int timeout = config.getConnectTimeoutInMillis();

                    if ((timeout > 0)
                            && ((currentTime - session.getLastIoTime()) >= timeout)) {
                        selectedKeys.remove(key);
                        increaseRemainingRequests();
                        ioProcessor.push(session.EVENT_NOT_CONNECTED);
                    }
                }

                if ((config.getIdleTimeInMillis() > 0)
                        && !session.isIdle()
                        && ((currentTime - session.getLastIoTime()) >= config
                                .getIdleTimeInMillis())) {
                    session.setIdle(true);
                    increaseRemainingRequests();
                    ioProcessor.push(session.EVENT_IDLE);
                } else if (session.isWriteTimedOut(currentTime)) {
                    ioProcessor.push(new Event(EventType.EXCEPTION, session,
                            new SocketTimeoutException("write timed out")));
                }
            }
        }
    }

    private void processReadySessions(int nKeys) {
        if (nKeys <= 0) {
            return;
        }

        Set keys;
        Iterator it;
        Session session;

        // process ready channels
        keys = selector.selectedKeys();

        if ((keys != null) && !keys.isEmpty()) {
            it = keys.iterator();

            if (it.hasNext()) {
                do {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();
                    session = (Session) key.attachment();

                    if (key.isConnectable()) {
                        increaseRemainingRequests();
                        ioProcessor.push(session.EVENT_CONNECTED);
                    } else if (key.isReadable()) {
                        increaseRemainingRequests();
                        ioProcessor.push(session.EVENT_READY_TO_READ);
                    } else if (key.isWritable()) {
                        session.setWriteBufferFull(false);
                        key.interestOps(SelectionKey.OP_READ);
                        ioProcessor.getWriteController().addSession(session);
                    }
                } while (it.hasNext());
            }
        }
    }

    private synchronized void increaseRemainingRequests() {
        remainingRequests++;
    }

    private synchronized void decreaseRemainingRequests() {
        --remainingRequests;

        if ((remainingRequests == 0) && waitingForCompletion) {
            notify();
        }
    }

    private synchronized void waitForCompletion() {
        waitingForCompletion = true;

        while ((remainingRequests > 0) && !timeToStop) {
            try {
                wait();
            } catch (InterruptedException e1) {
            }
        }

        waitingForCompletion = false;
    }

    public boolean isProcessable(Event e) {
        return e.getType() != EventType.READY_TO_WRITE;
    }

    public void processEvent(Event event) {
        EventType type = event.getType();
        Session session = event.getSession();

        try {
            if (type == EventType.READY_TO_READ) {
                doRead(session);
            } else if (type == EventType.EXCEPTION) {
                session.getEventDispatcher().fire(event);
            } else if (type == EventType.CLOSE_REQUEST) {
                doClose(session);
            } else if (type == EventType.CONNECTED) {
                doConnected(session);
            } else if (type == EventType.CONNECTION_TIMEOUT) {
                doConnectionTimeout(session);
            }

            if (type == EventType.IDLE) {
                session.getEventDispatcher().fire(event);
            } else {
                session.setLastIoTime(System.currentTimeMillis());
            }
        } catch (AsynchronousCloseException e) {
        } catch (CancelledKeyException cke) {
            ioProcessor.getExceptionMonitor().exceptionCaught(cke);
        } catch (Throwable t) {
            session.getEventDispatcher().fire(
                    new Event(EventType.EXCEPTION, session, t));

            if (t instanceof IOException) {
                doClose(session);
            }
        } finally {
            decreaseRemainingRequests();
            session.getEventDispatcher().flush();
        }
    }

    private void doConnected(Session session) throws IOException {
        SocketChannel channel = session.getChannel();

        if (!channel.isConnected()) {
            boolean done = false;

            try {
                channel.finishConnect();
                done = true;
            } finally {
                // reset the session state if failed to connect
                if (!done) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                    }

                    session.setChannel(null, false);
                }
            }

            channel.register(selector, SelectionKey.OP_READ, session);
            session.setSelectionKey(channel.keyFor(selector));
        }

        Socket s = channel.socket();
        s.setReceiveBufferSize(ByteBufferPool.DEFAULT_BUF_SIZE);
        s.setSendBufferSize(ByteBufferPool.DEFAULT_BUF_SIZE);

        session.openBuffers();
        session.getEventDispatcher().fire(session.EVENT_CONNECTED);
    }

    private void doConnectionTimeout(Session session) throws IOException {
        session.getSelectionKey().cancel();

        SocketChannel channel = session.getChannel();

        if (channel != null) {
            if (channel.finishConnect()) {
                try {
                    channel.close();
                } catch (IOException ioe) {
                }
            }

            session.setChannel(null, false);
        }

        session.getEventDispatcher().fire(
                new Event(EventType.EXCEPTION, session, new ConnectException(
                        "timeout (" + session.getConfig().getConnectTimeout()
                                + " seconds)")));
    }

    private void doRead(Session session) throws IOException {
        // READ
        SocketChannel channel = session.getChannel();
        ByteBuffer readBuf = session.getReadBuffer();

        if ((channel == null) || (readBuf == null)) {
            return;
        }

        // read
        int readBytes = 0;
        boolean streamClosed = false;

        for (;;) {
            int n = channel.read(readBuf);

            if (n < 0) {
                streamClosed = true;
                break;
            } else if (n > 0) {
                readBytes += n;
            } else {
                break;
            }
        }

        // and interpret
        if (readBytes > 0) {
            for (;;) {
                readBuf.flip();

                Message m = session.getReadingMessage();

                if (m == null) {
                    MessageRecognizer recognizer = session
                            .getMessageRecognizer();

                    // emulate mark
                    int limit = readBuf.limit();
                    Exception exception = null;

                    try {
                        m = recognizer.recognize(readBuf);
                    } catch (Exception e) {
                        exception = e;
                        if (e instanceof MessageParseException) {
                            MessageParseException mpe = (MessageParseException) e;
                            readBuf.limit(limit);
                            readBuf.position(0);
                            ByteBuffer readBufCopy = ByteBuffer.allocate(limit);
                            readBufCopy.put(readBuf);
                            readBufCopy.clear();
                            mpe.setBuffer(readBufCopy);
                        }
                        break;
                    } finally {
                        if (exception == null && m == null) {
                            // emulate reset
                            readBuf.limit(readBuf.capacity());
                            readBuf.position(limit);
                        } else {
                            readBuf.limit(limit);
                            readBuf.position(0);

                            if (exception != null) {
                                session.getEventDispatcher().fire(
                                        new Event(EventType.EXCEPTION, session,
                                                exception));
                            }
                        }
                    }

                    if (m == null) {
                        break;
                    } else {
                        session.setReadingMessage(m);
                    }
                }

                boolean done;
                int limit = readBuf.limit();

                try {
                    done = m.read(readBuf);
                } catch (MessageParseException mpe) {
                    readBuf.position(0);
                    readBuf.limit(limit);
                    mpe.setBuffer(readBuf);
                    session.getEventDispatcher().fire(
                            new Event(EventType.EXCEPTION, session, mpe));
                    break;
                }

                if (done) {
                    session.setReadingMessage(null);
                    session.getEventDispatcher().fire(
                            new Event(EventType.RECEIVED, session, m));

                    if (readBuf.remaining() > 0) {
                        readBuf.compact();
                        continue;
                    } else {
                        readBuf.clear();
                        break;
                    }
                } else {
                    readBuf.compact();
                    break;
                }
            }
        }

        // disconnection handling
        if (streamClosed) {
            doClose(session);
        }
    }

    private static void doClose(Session session) {
        SelectionKey key = session.getSelectionKey();

        if (key != null) {
            key.cancel();
        }

        session.closeBuffers();

        if (session.isConnected()) {
            try {
                session.getChannel().close();
            } catch (IOException e) {
            }

            session.setChannel(null, false);
            session.getEventDispatcher().fire(session.EVENT_DISCONNECTED);
        } else {
            session.setChannel(null, false);
        }
    }
}