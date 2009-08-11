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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a TCP/IP socket connection and provides methods to read/write
 * events from/to {@link IoProcessor}via {@link EventDispatcher}.
 * <p>
 * There are three constructors to create a session:
 * <ul>
 * <li><strong>Default constructor </strong> that is useful for setter
 * injection and unit testing.</li>
 * <li><strong>Constructor with already connected {@link SocketChannel}
 * </strong>.</li>
 * <li><strong>Constructor with {@link SocketAddress}</strong> first attempts
 * to connect to the specified address and then starts communication.</li>
 * </ul>
 * <p>
 * Subscribe first ({@link #addSessionListener(SessionListener)}) and call
 * {@link #start()}to start connection attempt and communication.
 *
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 * @author Trustin Lee (http://gleamynode.net/dev/)
 *
 * @see SessionConfig
 */
public class Session {
    final Event EVENT_CLOSE_REQUEST =
        new Event(EventType.CLOSE_REQUEST, this, null);
    final Event EVENT_CONNECTED = new Event(EventType.CONNECTED, this, null);
    final Event EVENT_DISCONNECTED =
        new Event(EventType.DISCONNECTED, this, null);
    final Event EVENT_NOT_CONNECTED =
        new Event(EventType.CONNECTION_TIMEOUT, this, null);
    final Event EVENT_IDLE = new Event(EventType.IDLE, this, null);
    final Event EVENT_READY_TO_READ =
        new Event(EventType.READY_TO_READ, this, null);
    final Event EVENT_READY_TO_WRITE =
        new Event(EventType.READY_TO_WRITE, this, null);
    private SessionConfig config;
    private IoProcessor ioProcessor;
    private EventDispatcher eventDispatcher;
    private MessageRecognizer messageRecognizer;
    private List sessionListeners = new ArrayList();
    private Object[] safeSessionListeners = new SessionListener[0];
    private boolean started;
    private boolean closing;
    private boolean idle;
    private long lastIoTime;
    private Object attachment;
    private SelectionKey selectionKey;
    private SocketChannel channel;
    private SocketAddress socketAddress;
    private String socketAddressString;
    private long writeStartTime;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private final WriteQueue writeRequestQueue = new WriteQueue(16);
    private Message readingMessage;
    private Message writingMessage;
    private boolean writingLastPart;
    private boolean writeBufferFull;
    private ExceptionMonitor monitor = ExceptionLoggingMonitor.getInstance();

    /**
     * Constructs a non-initialized session. This constructor is useful when
     * you're using a lightweight container which provides setter injections
     * (i.e. Spring). You must call {@link #setIoProcessor(IoProcessor)},
     * {@link #setMessageRecognizer(MessageRecognizer)},
     * {@link #setEventDispatcher(EventDispatcher)}, and
     * {@link #setSocketAddress(SocketAddress)}or
     * {@link #setChannel(SocketChannel)}to complete the initialization of the
     * session. Otherwise, you can use this constructor to create a mock object
     * for unit testing.
     */
    public Session() {
        config = new SessionConfig();
    }

    /**
     * Constructs a new session with the specified channel and with the default
     * settings.
     *
     * @param channel
     *            A {@link SocketChannel}to perform the actual I/O
     * @throws IllegalArgumentException
     *             if the specified channel is not connected yet.
     */
    public Session(IoProcessor ioProcessor, SocketChannel channel,
                   MessageRecognizer messageRecognizer,
                   EventDispatcher eventDispatcher) {
        this(ioProcessor, channel, messageRecognizer, eventDispatcher,
             new SessionConfig());
    }

    /**
     * Constructs a new session that connects to the specified socket address
     * with the default settings.
     *
     * @param socketAddress
     *            a {@link SocketAddress}to connect to
     *
     * @throws IllegalArgumentException
     *             if the specified timeout is too big or less than
     *             <code>0</code>.
     */
    public Session(IoProcessor ioProcessor, SocketAddress socketAddress,
                   MessageRecognizer messageRecognizer,
                   EventDispatcher eventDispatcher) {
        this(ioProcessor, socketAddress, messageRecognizer, eventDispatcher,
             new SessionConfig());
    }

    /**
     * Constructs a new session with the specified channel and with the
     * specified settings.
     *
     * @param channel
     *            A {@link SocketChannel}to perform the actual I/O
     * @param config
     *            A session settings
     *
     * @throws IllegalArgumentException
     *             if the specified channel is not connected yet.
     */
    public Session(IoProcessor ioProcessor, SocketChannel channel,
                   MessageRecognizer messageRecognizer,
                   EventDispatcher eventDispatcher, SessionConfig config) {
        setIoProcessor(ioProcessor);
        setChannel(channel, true);
        setMessageRecognizer(messageRecognizer);
        setEventDispatcher(eventDispatcher);
        setConfig(config);
    }

    /**
     * Constructs a new session that connects to the specified socket address
     * with the specified settings.
     *
     * @param socketAddress
     *            a {@link SocketAddress}to connect to
     * @param config
     *            A session settings
     *
     * @throws IllegalArgumentException
     *             if the specified timeout is too big or less than
     *             <code>0</code>.
     */
    public Session(IoProcessor ioProcessor, SocketAddress socketAddress,
                   MessageRecognizer messageRecognizer,
                   EventDispatcher eventDispatcher, SessionConfig config) {
        this.channel = null;
        setIoProcessor(ioProcessor);
        setSocketAddress(socketAddress);
        setMessageRecognizer(messageRecognizer);
        setEventDispatcher(eventDispatcher);
        setConfig(config);
    }

    /**
     * Subscribe a {@link SessionListener}to receive incoming events.
     */
    public synchronized void addSessionListener(SessionListener listener) {
        Check.notNull(listener, "listener");
        sessionListeners.add(listener);
        safeSessionListeners = sessionListeners.toArray();
    }

    /**
     * Unsubscribe a {@link SessionListener}to stop receiving incoming events.
     */
    public synchronized void removeSessionListener(SessionListener listener) {
        Check.notNull(listener, "listener");
        sessionListeners.remove(listener);
        safeSessionListeners = sessionListeners.toArray();
    }

    void setSessionListeners(List listeners) {
        Check.notNull(listeners, "listeners");
        sessionListeners = listeners;
        safeSessionListeners = listeners.toArray();
    }

    /**
     * Starts communication. I/O processor will will try to connect to the
     * address that <code>socketAddress</code> property specifies if and only
     * if it is set. Otherwise, it will try to start with the socket channel
     * which is already connected.  Please note that this method returns
     * immediately and you'll get notified from registered {@link SessionListener}'s
     * {@link SessionListener#connectionEstablished(Session)} method.
     *
     * @return <code>true</code>, if and only if the communication has been
     *         started. <code>false</code> if the session is already started
     *         or closing.
     *
     * @throws IllegalStateException
     *             if any required property is not set or the specified IoProcessor is not started.
     */
    public synchronized boolean start() {
        if (started || closing) {
            return false;
        }

        if (ioProcessor == null) {
            throw new IllegalStateException("ioProcessor is not specified.");
        }

        if (eventDispatcher == null) {
            throw new IllegalStateException("eventDispatcher is not specified.");
        }

        if ((channel == null) && (socketAddress == null)) {
            throw new IllegalStateException("Neither channel nor socket address is specified.  Please call setChannel(SocketChannel) or setSocketAddress(SocketAddress) method to initialize the session.");
        }

        started = true;

        setReadingMessage(null);
        setWritingMessage(null);
        setWritingLastPart(false);
        setWriteBufferFull(false);
        setIdle(false);
        setLastIoTime(System.currentTimeMillis());
        setSelectionKey(null);

        ioProcessor.notifyEstablishedSession(this);
        return true;
    }

    /**
     * Closes the session. Socket connection will be closed and
     * <code>connectionClosed</code> event will be dispatched to
     * {@link SessionListener}. Please note that the messages you've wrote
     * using {@link #write(Message)}will be discarded if they are not actually
     * written to the socket channel.
     */
    public synchronized void close() {
        if (!started || closing) {
            return;
        }

        closing = true;
        ioProcessor.notifyEstablishedSession(this);
    }

    /**
     * Returns the current settings of this session.
     */
    public SessionConfig getConfig() {
        return config;
    }

    /**
     * Sets the current settings of this session.
     */
    public void setConfig(SessionConfig config) {
        Check.notNull(config, "config");
        this.config = config;
    }

    /**
     * Writes the specified message to the socket channel. This method does not
     * directly write the message using I/O operations, but it just queues the
     * message into the internal queue and notify it to {@link IoProcessor}to
     * let it to handle I/O operations, so it does not mean the message is
     * written to the socket channel even if this method returns
     * <code>true</code>. If the message is really written,
     * {@link SessionListener#messageSent(Session, Message)}method will be
     * invoked by {@link EventDispatcher}.
     *
     * @return <code>true</code> if and only if the write request has been
     *         queued. <code>false</code> if the connection is closed or
     *         closing.
     */
    public boolean write(Message message) {
        return write(message, Long.MAX_VALUE);
    }

    /**
     * Writes the specified message to the socket channel. This method is
     * identical with {@link #write(Message)} except that it provides a
     * timeout option in milliseconds unit.
     */
    public boolean write(Message message, long timeout) {
        if (isClosed() || closing) {
            return false;
        }

        Check.notNull(message, "message");

        writeRequestQueue.setMaxSize(config.getMaxQueuedWriteCount());
        if (writeRequestQueue.push(message, timeout)) {
            ioProcessor.notifyWriteRequest(this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the numbers of remaining write requests which were queued by
     * {@link #write(Message)}.
     *
     * @see #setMaxQueuedWriteCount(int)
     */
    public int getQueuedWriteCount() {
        return writeRequestQueue.size();
    }

    /**
     * Returns <code>true</code> if and only if this session is idle.
     */
    public boolean isIdle() {
        return idle;
    }

    void setIdle(boolean idle) {
        this.idle = idle;
    }

    /**
     * Returns millis time that I/O occurred last.
     */
    public long getLastIoTime() {
        return lastIoTime;
    }

    void setLastIoTime(long lastIoTime) {
        this.lastIoTime = lastIoTime;
        this.idle = false;
    }

    boolean isWriteTimedOut(long currentTime) {
        return (config.getWriteTimeoutInMillis() > 0)
               && (getWritingMessage() != null)
               && ((currentTime - getWriteStartTime()) >= config
                                                          .getWriteTimeoutInMillis());
    }

    /**
     * Returns <code>true</code. if the session is started.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Returns <code>true</code> if the connection is open.
     */
    public boolean isConnected() {
        SocketChannel channel = this.channel;
        return (channel != null) && channel.isConnected();
    }

    /**
     * Returns <code>true</code> if the connection is closed.
     */
    public boolean isClosed() {
        SocketChannel channel = this.channel;
        return (channel == null) || !channel.isConnected();
    }

    /**
     * Returns <code>true</code> if once {@link #close()}is called and close
     * operation is pending.
     *
     * @return <code>false</code> if the session is not opened or already
     *         closed
     */
    public boolean isClosing() {
        return closing;
    }

    /**
     * Returns <code>true</code> if the connection attempt is being made.
     */
    public boolean isConnectionPending() {
        SocketChannel channel = this.channel;
        return (channel != null) && channel.isConnectionPending();
    }

    /**
     * Returns the socket address this session is connected to.
     */
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    /**
     * Returns the string representation of the socket address this session is
     * connected to.
     */
    public String getSocketAddressString() {
        return socketAddressString;
    }

    /**
     * Sets the socket address this session will connect to. You can reuse this
     * session using this method. Calling {@link #start()}method will make this
     * session connect to the specified socket address.
     *
     * @throws IllegalStateException
     *             if this session is already started.
     */
    public void setSocketAddress(SocketAddress socketAddress) {
        if (started) {
            throw new IllegalStateException("already started");
        }

        Check.notNull(socketAddress, "socketAddress");
        this.socketAddress = socketAddress;
        this.socketAddressString = socketAddress.toString();
    }

    /**
     * Returns the attachment of this session. The purpose of this method is
     * identical to {@link SelectionKey#attachment()}.
     */
    public Object getAttachment() {
        return attachment;
    }

    /**
     * Sets the attachment of this session. The purpose of this method is
     * identical to {@link SelectionKey#attach(java.lang.Object)}.
     */
    public void setAttachment(Object newAttachment) {
        this.attachment = newAttachment;
    }

    /**
     * Returns the {@link Message}that is being read now. This getter is useful
     * to know what message was being read when an exception is thrown.
     *
     * @return <code>null</code> if there is no message being read now.
     */
    public Message getReadingMessage() {
        return readingMessage;
    }

    void setReadingMessage(Message m) {
        this.readingMessage = m;
    }

    /**
     * Returns the {@link Message}that is being written now. This getter is
     * useful to know what message was being written when an exception is
     * thrown.
     *
     * @return <code>null</code> if there is no message being written now.
     */
    public Message getWritingMessage() {
        return writingMessage;
    }

    void setWritingMessage(Message m) {
        this.writingMessage = m;
    }

    /**
     * Returns the I/O processor this session reads and writes the message via.
     */
    public IoProcessor getIoProcessor() {
        return ioProcessor;
    }

    /**
     * Sets the I/O processor this session reads and writes the message via.
     * This property cannot be changed if once set.
     *
     * @throws IllegalStateException
     *             if this property is already set
     */
    public void setIoProcessor(IoProcessor ioProcessor) {
        Check.notNull(ioProcessor, "ioProcessor");

        if (this.ioProcessor != null) {
            throw new IllegalStateException("ioProcessor cannot be set more than once.");
        }

        this.ioProcessor = ioProcessor;
    }

    /**
     * Returns the {@link MessageRecognizer}who recognizes the incoming data
     * from this session.
     */
    public MessageRecognizer getMessageRecognizer() {
        return messageRecognizer;
    }

    /**
     * Sets the {@link MessageRecognizer}who recognizes the incoming data from
     * this session.
     */
    public void setMessageRecognizer(MessageRecognizer messageRecognizer) {
        Check.notNull(messageRecognizer, "messageRecognizer");
        this.messageRecognizer = messageRecognizer;
    }

    /**
     * Returns the {@link EventDispatcher}who dispatches the events of this
     * session.
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Sets the {@link EventDispatcher}who dispatches the events of this
     * session. This property cannot be changed if once set.
     *
     * @throws IllegalStateException
     *             if this property is already set
     */
    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        Check.notNull(eventDispatcher, "eventDispatcher");

        if (this.eventDispatcher != null) {
            throw new IllegalStateException("eventDispatcher cannot be set more than once.");
        }

        this.eventDispatcher = eventDispatcher;
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
     * Returns the underlying socket channel of this session.
     *
     * @return <code>null</code> if the connection is closed. You can use
     *         {@link #getSocketAddress()},{@link #isConnected()},
     *         {@link #isConnectionPending()},{@link #isClosed()}methods
     *         instead.
     */
    public SocketChannel getChannel() {
        return channel;
    }

    /**
     * Sets the underlying socket channel of this session. You can reuse this
     * session using this method. {@link #start()}method will make this session
     * communicate using the specified channel.
     *
     * @param channel
     *            the {@link SocketChannel}this session will use
     * @throws IllegalStateException
     *             if this session is already started
     * @throws IllegalArgumentException
     *             if the specified channel is not yet connected
     *
     */
    public void setChannel(SocketChannel channel) {
        if (started) {
            throw new IllegalStateException("already started");
        }

        setChannel(channel, true);
    }

    void setChannel(SocketChannel channel, boolean connected) {
        if (connected) {
            Check.notNull(channel, "channel");

            if (!channel.isConnected()) {
                throw new IllegalArgumentException("channel is not connected");
            }

            Socket s = channel.socket();
            setSocketAddress(new InetSocketAddress(s.getInetAddress(),
                                                   s.getPort()));
        }

        if (channel == null) {
            started = false;
            closing = false;
        }

        this.channel = channel;
    }

    SelectionKey getSelectionKey() {
        return selectionKey;
    }

    void setSelectionKey(SelectionKey key) {
        this.selectionKey = key;
    }

    void openBuffers() {
        readBuffer = ByteBufferPool.open();
        writeBuffer = ByteBufferPool.open();
        readBuffer.order(config.getByteOrder());
        writeBuffer.order(config.getByteOrder());

        writeBuffer.limit(0);
        writeRequestQueue.open();
    }

    void closeBuffers() {
        writeRequestQueue.close();

        if (readBuffer != null) {
            ByteBufferPool.close(readBuffer);
            readBuffer = null;
        }

        if (writeBuffer != null) {
            ByteBufferPool.close(writeBuffer);
            writeBuffer = null;
        }
    }

    ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    Queue getWriteRequestQueue() {
        return writeRequestQueue;
    }

    long getWriteStartTime() {
        return writeStartTime;
    }

    void setWriteStartTime(long writeStartTime) {
        this.writeStartTime = writeStartTime;
    }

    boolean isWritingLastPart() {
        return writingLastPart;
    }

    void setWritingLastPart(boolean writingLastPart) {
        this.writingLastPart = writingLastPart;
    }

    boolean isWriteBufferFull() {
        return writeBufferFull;
    }

    void setWriteBufferFull(boolean writeBufferFull) {
        this.writeBufferFull = writeBufferFull;
    }

    /**
     * Fires '<code>connectionEstablished</code>' event to registered
     * {@link SessionListener}s. This method is invoked by
     * {@link EventDispatcher}s. <strong>DO NOT </strong> call this method
     * directly.
     */
    public void fireConnectionEstablished() {
        final Object[] listeners = safeSessionListeners;
        final int size = listeners.length;

        for (int i = 0; i < size; i++) {
            ((SessionListener) listeners[i]).connectionEstablished(this);
        }
    }

    /**
     * Fires '<code>connectionClosed</code>' event to registered
     * {@link SessionListener}s. This method is invoked by
     * {@link EventDispatcher}s. <strong>DO NOT </strong> call this method
     * directly.
     */
    public void fireConnectionClosed() {
        final Object[] listeners = safeSessionListeners;
        final int size = listeners.length;

        for (int i = 0; i < size; i++) {
            ((SessionListener) listeners[i]).connectionClosed(this);
        }
    }

    /**
     * Fires '<code>messageReceived</code>' event to registered
     * {@link SessionListener}s. This method is invoked by
     * {@link EventDispatcher}s. <strong>DO NOT </strong> call this method
     * directly.
     */
    public void fireMessageReceived(Message m) {
        final Object[] listeners = safeSessionListeners;
        final int size = listeners.length;

        for (int i = 0; i < size; i++) {
            ((SessionListener) listeners[i]).messageReceived(this, m);
        }
    }

    /**
     * Fires '<code>messageSent</code>' event to registered
     * {@link SessionListener}s. This method is invoked by
     * {@link EventDispatcher}s. <strong>DO NOT </strong> call this method
     * directly.
     */
    public void fireMessageSent(Message m) {
        final Object[] listeners = safeSessionListeners;
        final int size = listeners.length;

        for (int i = 0; i < size; i++) {
            ((SessionListener) listeners[i]).messageSent(this, m);
        }
    }

    /**
     * Fires '<code>sessionIdle</code>' event to registered
     * {@link SessionListener}s. This method is invoked by
     * {@link EventDispatcher}s. <strong>DO NOT </strong> call this method
     * directly.
     */
    public void fireSessionIdle() {
        final Object[] listeners = safeSessionListeners;
        final int size = listeners.length;

        for (int i = 0; i < size; i++) {
            ((SessionListener) listeners[i]).sessionIdle(this);
        }
    }

    /**
     * Fires '<code>sessionIdle</code>' event to registered
     * {@link SessionListener}s. This method is invoked by
     * {@link EventDispatcher}s. <strong>DO NOT </strong> call this method
     * directly.
     */
    public void fireExceptionCaught(Throwable t) {
        final Object[] listeners = safeSessionListeners;
        final int size = listeners.length;

        try {
            for (int i = 0; i < size; i++) {
                ((SessionListener) listeners[i]).exceptionCaught(this, t);
            }
        } catch (Throwable uncaught) {
            monitor.exceptionCaught(uncaught);
        }
    }

    // backward compatibility methods

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public int getConnectTimeout() {
        return config.getConnectTimeout();
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public int getConnectTimeoutInMillis() {
        return config.getConnectTimeoutInMillis();
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public void setConnectTimeout(int connectTimeout) {
        config.setConnectTimeout(connectTimeout);
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public int getIdleTime() {
        return config.getIdleTime();
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public int getIdleTimeInMillis() {
        return config.getIdleTimeInMillis();
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public void setIdleTime(int idleTime) {
        config.setIdleTime(idleTime);
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public int getMaxQueuedWriteCount() {
        return config.getMaxQueuedWriteCount();
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public void setMaxQueuedWriteCount(int newLimit) {
        config.setMaxQueuedWriteCount(newLimit);
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public int getWriteTimeout() {
        return config.getWriteTimeout();
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public int getWriteTimeoutInMillis() {
        return config.getWriteTimeoutInMillis();
    }

    /**
     * @deprecated Use {@link SessionConfig}instead.
     */
    public void setWriteTimeout(int writeTimeout) {
        config.setWriteTimeout(writeTimeout);
    }
}
