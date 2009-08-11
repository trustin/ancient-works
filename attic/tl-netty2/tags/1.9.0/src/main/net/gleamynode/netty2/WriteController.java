/*
 *   @(#) $Id: WriteController.java 19 2005-04-19 15:29:55Z trustin $
 *   
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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
class WriteController extends Controller implements Runnable {
    private final Set sessions = new HashSet();

    private final IoProcessor ioProcessor;

    private Thread thread;

    private volatile boolean waitingForCompletion;

    private volatile int remainingRequests;

    private boolean timeToStop;

    public WriteController(IoProcessor ioProcessor) {
        this.ioProcessor = ioProcessor;
    }

    public void setThreadPriority(int newPriority) {
        if ((thread != null) && thread.isAlive()) {
            thread.setPriority(newPriority);
        }
    }

    public void init() {
        thread = new Thread(this, ioProcessor.getThreadNamePrefix() + "-wc");
        thread.setPriority(ioProcessor.getControllerThreadPriority());
        thread.start();
    }

    public synchronized void startDestroy() {
        timeToStop = true;
        notify();
    }

    public void finishDestroy() {
        while (thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void addSession(Session s) {
        if (s.isWriteBufferFull())
            return;

        sessions.add(s);
        notify();
    }

    public void run() {
        Session[] readySessions = new Session[16];
        int readySessionSize = 0;

        while (!timeToStop) {
            synchronized (this) {
                while (sessions.size() <= 0) {
                    if (timeToStop) {
                        return;
                    }

                    try {
                        wait(1000);
                    } catch (InterruptedException ie) {
                    }
                }

                readySessionSize = sessions.size();

                if (readySessions.length < readySessionSize) {
                    readySessions = new Session[readySessionSize];
                }

                readySessionSize--; // decrease for efficient loop.

                Iterator it = sessions.iterator();

                for (int i = readySessionSize; i >= 0; i--) {
                    readySessions[i] = (Session) it.next();
                    it.remove();
                }
            }

            for (int i = readySessionSize; i >= 0; i--) {
                increaseRemainingRequests();
                ioProcessor.push(readySessions[i].EVENT_READY_TO_WRITE);
            }

            waitForWriteCompletion();
        }
    }

    private synchronized void increaseRemainingRequests() {
        remainingRequests++;
    }

    private synchronized void decreaseRemainingRequests() {
        if ((--remainingRequests == 0) && waitingForCompletion) {
            notify();
        }
    }

    private synchronized void waitForWriteCompletion() {
        waitingForCompletion = true;

        while ((remainingRequests > 0) && !timeToStop) {
            try {
                wait();
            } catch (InterruptedException e1) {
            }
        }

        waitingForCompletion = false;
    }

    public boolean isProcessable(Event event) {
        return event.getType() == EventType.READY_TO_WRITE;
    }

    public void processEvent(Event event) {
        Session session = event.getSession();

        try {
            if (event.getType() == EventType.READY_TO_WRITE) {
                doWrite(session);
            }

            session.setLastIoTime(System.currentTimeMillis());
        } catch (AsynchronousCloseException e) {
        } catch (CancelledKeyException e) {
            ioProcessor.getExceptionMonitor().exceptionCaught(e);
        } catch (Throwable t) {
            session.getEventDispatcher().fire(
                    new Event(EventType.EXCEPTION, session, t));

            if (t instanceof IOException) {
                session.close();
            }
        } finally {
            decreaseRemainingRequests();
            session.getEventDispatcher().flush();
        }
    }

    private void doWrite(Session session) throws IOException {
        if (session.isWriteBufferFull())
            return;

        Queue queue = session.getWriteRequestQueue();
        ByteBuffer writeBuf = session.getWriteBuffer();

        if (session.isClosed() || (writeBuf == null)) {
            queue.close();
            synchronized (this) {
                sessions.remove(session);
            }
            return;
        }

        while (true) {
            if (session.getWritingMessage() == null) {
                // Brand new message
                Message m = (Message) queue.pop();
                if (m == null) {
                    // Nothing left to send
                    session.setWriteBufferFull(false);
                    return;
                }

                session.setWritingMessage(m);
                session.setWriteStartTime(System.currentTimeMillis());
            }

            // Write as much as you can to the buffer
            Message m = session.getWritingMessage();
            boolean wroteLastPart = m.write(writeBuf);
            session.setWritingLastPart(wroteLastPart);

            // Flush it to the socket
            boolean allWritten = flush(session);
            if (allWritten) {
                writeBuf.clear();
                if (wroteLastPart) {
                    session.setWritingMessage(null);
                    session.getEventDispatcher().fire(
                            new Event(EventType.SENT, session, m));
                }
            } else {
                writeBuf.compact();
                session.setWriteBufferFull(true);
                ((ReadController) ioProcessor.getReadController())
                        .notifyOpWrite(session);
                return;
            }
        }
    }

    private boolean flush(Session session) throws IOException {
        SocketChannel channel = session.getChannel();
        ByteBuffer writeBuf = session.getWriteBuffer();
        writeBuf.flip();
        while (writeBuf.remaining() > 0) {
            if (channel.write(writeBuf) == 0) {
                return false;
            }
        }
        return true;
    }
}
