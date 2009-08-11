//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import net.gleamynode.io.BlockingInputStream;
import net.gleamynode.io.Bytes;
import net.gleamynode.io.UnmarkableInputStream;

/**
 * <p>
 * A message-based socket class. It hides low-level TCP/IP communication
 * by event model. To do so, <code>MessageSocket</code> object can't operate
 * without the help of <code>MessageSocketRunner</code>. 
 * <code>MessageSocketRunner</code> will drive the <code>MessageSocket</code>
 * using its internal communication flow. (If you want to use your own
 * communication flow, you can implement the <code>MessageSocketRunner</code>
 * interface by yourself.) <code>MessageSocketRunner</code> will call these
 * methods:
 * <ul>
 *   <li>processMessageReceived() - reads bytes from socket, interprets
 *                                  the message, and fires the event.</li>
 *   <li>processMessageSent() - writes message into socket and fires
 *                              the event</li>
 *   <li>processIdle() - handles idle timeout event which occurrs when
 *                       no messages are being exchanged for a while.</li>
 *   <li>processTimer() - handles timer event which is frequently used 
 *                        for logical timeout handling in usual TCP/IP
 *                        communication.</li>
 * </ul>
 * </p>
 * <p>
 * <b>NOTE</b>: you don't need to call these methods explicitly. Actually,
 * calling these methods will make things go wrong because they are not
 * thread-safe.
 * </p>
 * <p>
 * <b>NOTE 2</b>: <code>MessageSocket</code> doesn't explicitly 
 * initialize or connect <code>java.net.Socket</code>. The
 * <code>java.net.Socket</code> object should be connected to
 * somewhere before you pass it to <code>MessageSocket</code>.
 * </p>
 *
 * <h2>How messages are recognized and read from the stream.</h2>
 * <p>
 * If you construct a new <code>MessageSocket</code> or change current
 * <code>Protocol</code> by calling <code>setProtocol()</code> method,
 * <code>MessageSocket</code> iterates all messages of the protocol, and
 * clones all messages into its internal array. It means that
 * every <code>MessageSocket</code> objects have their own <code>Message</code>
 * object to avoid concurrency problem. After creating the table of messages,
 * it creates a 'candidate table' of the same size with message table.
 * At first, the candidate table is filled with <code>true</code> because
 * all type of messages can appear from the stream.
 * <code>MessageSocketRunner</code> will frequently call
 * <code>processMessageReceived()</code> to check any bytes came from the the
 * stream and to reduce the number of the candidates.
 * <code>processMessageReceived()</code> will rely on the method
 * <code>int Message.recognize(byte[] clue)</code>. The method will return
 * one of these values; Message.RECOGNIZE_YES, Message.RECOGNIZE_NOT_YET,
 * Message.RECOGNIZE_NO. See the detail at <code>Message</code> class 
 * description page.
 * As bytes coming from the stream, the number of candidates decreases,
 * and finally if the number of the left candidates becomes 1, 
 * it is bingo, and messageReceived event will be sent to listeners.
 * If becomes 0, exceptionCaught event whose enclosed
 * exception is <code>UnknownMessageException</code> will be.
 * </p>
 * 
 * <h2>'Closing' and 'Closed'</h2>
 * <p>
 * The status 'closing' means a user called <code>close()</code> method,
 * but the socket is not closed actually. The socket will be closed soon
 * after processing all remaining events.
 * </p>
 * <p>
 * The status 'closed' means the socket is really closed. No more bytes
 * can flow thru it. You can wait for the socket go 'closed' by calling
 * <code>cloaseAndWait()</code> method. You also can close the socket
 * immediately by calling the <code>closeImmediately</code> method
 * though it is not recommended.
 * </p>
 * <p>
 * Note that these two status is different and both of them cannot
 * be true.
 * </p>
 * 
 * <p>
 * <b>NOTE</b>: Never forget to set <code>ReadTimeout</code> and
 * <code>ReadIntervalTimeout</code> value. If not, 
 * <code>processMessageReceived()</code> may block forever. (e.g. DoS attack).
 * Don't call <code>int InputStream.read()</code> method because it can
 * block forever even though timeout values were set.
 * See net.gleamynode.io.BlockingInputStream(MessageSocket uses it) to know more.
 * </p>
 *
 * <p>
 * When <code>MessageSocket</code> fires exceptionCaught event, if the
 * exception had occurred while handling message I/O, the socket will
 * automatically become 'closing' status, and will not receive any more
 * message though it will flush the remaining send message buffer.
 * </p>
 * <p>If the exception was thrown in non-I/O routine(idle, timer,
 * connectionEstablished, connectionClosed), the connection will not
 * be closed because it is recoverable problem.
 * </p>
 *
 * <h2>CHANGES</h2>
 * <h3>1.5 <small>2003. 2. 24.</small></h3>
 * <p><ul>
 *   <li>Renamed method names according to the changed listener interface 
 *       name.</li>
 *   <li>Sync with new {@link Message} interface.</li>
 *   <li>Added {@link #isTimerSet()} method.</li>
 *   <li>Updated license statement.</li>
 * </ul></p>
 * 
 * <h3>1.4.2 <small>2003. 2. 24.</small></h3>
 * <p><ul>
 *   <li>{@link #DEFAULT_MAX_LISTENERS}, {@link #DEFAUlT_MESSAGE_QUEUE_SIZE} is
 *       <code>private</code> now.</li>
 *   <li>{@link #out} is {@link java.io.BufferedOutputStream} now.</li>
 *   <li>Reformatted code.</li>
 * </ul></p>
 * <h3>1.4.1 <small>2003. 1. 15.</small></h3>
 * <p><ul>
 *   <li>Removed unnecessary import statements.</li>
 * </ul></p>
 * <h3>1.4</h3>
 * <p><ul>
 *   <li>Removed unnecessary import statements.</li>
 *   <li>Made it reusable. (added {@link #setSocket(java.net.Socket)})
 *   </li>
 *   <li>Revised documentation.</li>
 *   <li>Fixed a bug that {@link #setProtocol(Protocol)} doesn't
 *       call <code>resetCandidates()</code> when new protocol is
 *       same with current one.</li>
 * </ul></p>
 * <h3>1.3</h3>
 * <p><ul>
 *   <li>closeOffset field has been removed because it is unnecessary.</li>
 *   <li>Fixed a bug that messages in the queue are sent even though a socket 
 *       write error occurred.</li>
 *   <li>Fixed a bug that <code>IOException</code>s are thrown more than once
 *       while reading a message.</li>
 * </ul></p>
 *
 * <h3>1.2</h3>
 * <p><ul>
 *   <li>MessageSocketEvent's message is not a clone of the internal message
 *       object anymore. Performance improvement expected.</li>
 *   <li>Reduced memory footprint using clue table. byte array allocation will
 *       occur only once for each clue length.</li>
 *   <li><code>setProtocol()</code> doesn't unnecessarilly reallocate 
 *       arrays.</li>
 *   <li><code>listeners</code> is now an array instead of <code>Vector</code>.
 *       </li>
 *   <li><code>messageQueue</code> is now an array instead of
 *       <code>LinkedList</code>.</li>
 * </ul></p>
 *
 * <h3>1.1</h3>
 * <p><ul>
 *   <li>Added firstCandidate property to speed up candidate check loop.</li>
 *   <li>Always becomes 'closing' status when the exception is caught handling
 *       message I/O, but the remaining send buffer wil be flushed before
 *       the socket gets 'closed'.</li>
 *   <li>noMoreReceive flag added to prevent MessageSocketRunner from firing
 *       duplicated events while the socket is 'closing'.</li>
 *   <li>Added the method resetCandidates() and removeCandidate(int idx).</li>
 * </ul></p>
 *
 * @author  Trustin Lee
 * @version 1.5
 *
 * @see java.net.Socket
 * @see Message
 * @see Protocol
 * @see MessageSocketRunner
 * @see net.gleamynode.io.BlockingInputStream
 */
public class MessageSocket {
	/**
	 * The initial maximum number of listeners of this socket.
	 */
	private static final int DEFAULT_MAX_LISTENERS = 4;

	/**
	 * The initial size of message queue.
	 */
	private static final int DEFAULT_MESSAGE_QUEUE_SIZE = 128;

	/**
	 * The buffer size of {@link #in} and {@link #out}.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 4096;

	private MessageSocketEvent eventWithoutMessage;
	private Socket socket;
	private InputStream socketIn;
	private OutputStream socketOut;
	private BufferedInputStream in;
	private BlockingInputStream safeIn;
	private BufferedOutputStream out;
	private int idleTime;
	private long lastIdleTime;
	private long nextTimerTime;

	private MessageSocketListener[] listeners =
		new MessageSocketListener[DEFAULT_MAX_LISTENERS];

	private Message[] messageQueue =
		new Message[DEFAULT_MESSAGE_QUEUE_SIZE];
	private int firstOffset = 0;
	private int lastOffset = 0;
	private boolean queueFull = false;

	private boolean established;
	private boolean closed;
	private boolean closing;
	private boolean noMoreReceive;

	private Protocol protocol;
	private Message[] mtab;
	private boolean[] candidates; // true if the message is a candidate
	private int leftCandidates; // the number of candidates
	private int firstCandidate; // the index # of the first candidate.
	private byte[] clue; // the clue buffer.
	private int maxClueLen; // maxmum idx number of clueTable.

	/**
	 * Constructs a new <code>MessageSocket</code> without
	 * any socket and any protocol.
	 * You can call {@link #setSocket(java.net.Socket)} and
	 * {@link #setProtocol(Protocol)} to make this class work.
	 */
	public MessageSocket() {
		eventWithoutMessage = new MessageSocketEvent(this);
		idleTime = 0;
		lastIdleTime = 0L;
		nextTimerTime = 0L;
		established = false;
		closed = true;
		closing = false;
		noMoreReceive = true;

		socket = null;
		protocol = null;
		in = null;
		safeIn = null;
		out = null;
	}

	/**
	 * Constructs a new <code>MessageSocket</code> with the specified
	 * protocol but without socket.
	 * 
	 * You can call {@link #setSocket(Socket)} method to make this class
	 * work.
	 */
	public MessageSocket(Protocol protocol) {
		eventWithoutMessage = new MessageSocketEvent(this);
		idleTime = 0;
		lastIdleTime = 0L;
		nextTimerTime = 0L;
		established = false;
		closed = true;
		closing = false;
		noMoreReceive = false;

		socket = null;
		in = null;
		safeIn = null;
		out = null;

		if (protocol == null) {
			throw new NullPointerException("protocol is null");
		}

		setProtocol(protocol);
	}

	/**
	 * Constructs a new <code>MessageSocket</code> with the specified
	 * socket and protocol.
	 */
	public MessageSocket(Socket socket, Protocol protocol)
		throws IOException {
		eventWithoutMessage = new MessageSocketEvent(this);
		idleTime = 0;
		lastIdleTime = 0L;
		nextTimerTime = 0L;
		established = false;
		closed = true;
		closing = false;
		noMoreReceive = false;

		if (socket == null) {
			throw new NullPointerException("socket is null");
		}

		if (protocol == null) {
			throw new NullPointerException("protocol is null");
		}

		setProtocol(protocol);
		setSocket(socket);
	}

	/**
	 * Adds a <code>MessageSocketListener</code> to this socket.
	 */
	public void addMessageSocketListener(MessageSocketListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener is null");
		}

		int len = listeners.length;
		boolean added = false;
		synchronized (listeners) {
			// check duplication
			for (int i = 0; i < len; i++) {
				if (listeners[i] == listener) {
					return;
				}
			}

			// add
			for (int i = 0; i < len; i++) {
				if (listeners[i] == null) {
					listeners[i] = listener;
					added = true;
					break;
				}
			}

			if (!added) { // expand listeners
				MessageSocketListener[] newListeners =
					new MessageSocketListener[len * 2];
				// twice would be nice.
				int i;
				for (i = 0; i < len; i++) {
					newListeners[i] = listeners[i];
				}
				newListeners[i] = listener; // add listener
				this.listeners = newListeners; // replace with larger one.
			}
		}

	}

	/**
	 * Removes specified <code>MessageSocketListener</code> from this socket.
	 */
	public void removeMessageSocketListener(MessageSocketListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener is null");
		}
		int len = listeners.length;
		synchronized (listeners) {
			for (int i = 0; i < len; i++) {
				if (listeners[i] == listener) {
					listeners[i] = null;
				}
			}
		}
	}

	/**
	 * Send the TCP/IP message. The message wil be actually sent
	 * when it is at the top of the internal message queue of this
	 * socket.
	 *
	 * @return false if the socket is closing or closed.
	 */
	public boolean sendMessage(Message message) {
		if (closing || closed)
			return false;

		qadd(message);
		return true;
	}

	/**
	 * Returns <code>true</code> if this socket is closing.
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Returns <code>true</code> if this socket is closed.
	 */
	public boolean isClosing() {
		return closing;
	}

	/**
	 * Request the socket to close.
	 * Thie method will change the socket's status into 'closing' and
	 * return immediately.
	 */
	public void close() {
		closing = true;
	}

	/**
	 * Request the socket to close and wait until it really closes.
	 */
	public synchronized void closeAndWait() {
		close();
		while (!closed) {
			try {
				wait(1000);
			}
			catch (InterruptedException ite) {}
		}
	}

	/**
	 * Immediately close the socket regardless of remaining events, message queue.
	 */
	public void closeImmediately() {
		closeConnection();
	}

	/**
	 * Returns current protocol.
	 */
	public Protocol getProtocol() {
		return protocol;
	}

	/**
	 * Returns current socket.
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Returns the idle time value of this socket.
	 */
	public int getIdleTime() {
		return idleTime;
	}

	/**
	 * Returns the read timeout value of this socket.
	 */
	public int getReadTimeout() {
		return safeIn.getTimeout();
	}

	/**
	 * Return the read interval timeout value of this socket.
	 */
	public int getReadIntervalTimeout() {
		return safeIn.getIntervalTimeout();
	}

	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}

	public InetAddress getLocalAddress() {
		return socket.getLocalAddress();
	}

	public int getLocalPort() {
		return socket.getLocalPort();
	}

	public int getPort() {
		return socket.getPort();
	}

	public int getReceiveBufferSize() throws SocketException {
		return socket.getReceiveBufferSize();
	}

	public int getSendBufferSize() throws SocketException {
		return socket.getSendBufferSize();
	}

	public int getSoLinger() throws SocketException {
		return socket.getSoLinger();
	}

	public int getSoTimeout() throws SocketException {
		return socket.getSoTimeout();
	}

	public boolean getTcpNoDelay() throws SocketException {
		return socket.getTcpNoDelay();
	}

	/**
	 * Changes the protocol of this socket.
	 * The change of the protocol will be immediately affected.
	 */
	public synchronized void setProtocol(Protocol newProtocol) {
		int typeCount;
		int newMaxClueLen;
		
		if (this.protocol != null
			&& this.protocol == newProtocol) {
			resetCandidates();
			return;
		}

		this.protocol = newProtocol;

		// clone all messages for thread-safe I/O.
		// plus, calculate some constant value.
		typeCount = protocol.getMessageTypeCount();
		mtab = protocol.getMessages();
		int mlen = typeCount;

		if (candidates == null || candidates.length != mlen) {
			candidates = new boolean[mlen];
		}
		resetCandidates();

		
		newMaxClueLen = protocol.getMaxClueLength();;
		if ( clue == null || newMaxClueLen != this.maxClueLen ) {
			this.clue = new byte[newMaxClueLen];
		}
		this.maxClueLen = newMaxClueLen;
	}

	/**
	 * Sets this socket's low level socket.
	 * It will only succeed when no connection is active.
	 * You have to close current connection to set new socket.
	 * 
	 * @throws IOException if current connection is active yet.
	 */
	public synchronized void setSocket(Socket s) throws IOException {
		if (!isClosed()) {
			throw new IOException("Current socket is not closed yet.");
		}

		// reinitialize state variables.
		lastIdleTime = 0L;
		nextTimerTime = 0L;
		established = false;
		closed = false;
		closing = false;
		noMoreReceive = false;
		socket = s;

		// reinitialize streams.
		socketIn = socket.getInputStream();
		socketOut = socket.getOutputStream();

		in = new BufferedInputStream(socketIn, DEFAULT_BUFFER_SIZE);
		out = new BufferedOutputStream(socketOut, DEFAULT_BUFFER_SIZE);
		safeIn = new BlockingInputStream(new UnmarkableInputStream(in));
	}

	/**
	 * Sets the idle time value of this socket.
	 * non-positive value will disable idle event.
	 */
	public void setIdleTime(int i) {
		idleTime = i;
	}

	/**
	 * Sets the read timeout value of this socket.
	 * Non-positive value will disable this feature.
	 *
	 * @see net.gleamynode.io.BlockingInputStream
	 */
	public void setReadTimeout(int newTimeout) {
		safeIn.setTimeout(newTimeout);
	}

	/**
	 * Sets the read interval timeout value of this socket.
	 * Non-positive value will disable this feature.
	 *
	 * @see net.gleamynode.io.BlockingInputStream
	 */
	public void setReadIntervalTimeout(int newTimeout) {
		safeIn.setIntervalTimeout(newTimeout);
	}

	public void setReceiveBufferSize(int i) throws SocketException {
		socket.setReceiveBufferSize(i);
	}

	public void setSendBufferSize(int i) throws SocketException {
		socket.setSendBufferSize(i);
	}

	public void setSoLinger(boolean flag, int i)
		throws SocketException {
		socket.setSoLinger(flag, i);
	}

	public void setSoTimeout(int i) throws SocketException {
		socket.setSoTimeout(i);
	}

	public void setTcpNoDelay(boolean flag) throws SocketException {
		socket.setTcpNoDelay(flag);
	}

	/**
	 * Returns <code>true</code> if the timer is set by calling 
	 * {@link #setTimer(int)} method with positive integer.
	 */
	public boolean isTimerSet() {
		return nextTimerTime != 0;
	}

	/**
	 * Sets next alarm timer. For example, if you set timer to 10000,
	 * timer event will occur in 10 seconds. You can clear the timer by
	 * setting the value to '0';
	 */
	public void setTimer(int i) {
		if (i <= 0)
			nextTimerTime = 0L;
		else
			nextTimerTime = System.currentTimeMillis() + i;
	}

	/**
	 * Processes idle event. Don't call this method directly, but
	 * use <code>MessageSocketRunner</code> to drive the socket.
	 *
	 * @return true if idle event occurred.
	 *
	 * @see MessageSocketRunner
	 */
	public boolean processIdle() {
		MessageSocketListener[] listeners = this.listeners;
		MessageSocketListener listener;
		
		if (!ensureConnection())
			return false;

		if (idleTime > 0
			&& System.currentTimeMillis() - lastIdleTime > idleTime) {
			int len = listeners.length;
			synchronized (listeners) {
				for (int i = 0; i < len; i++) {
					listener = listeners[i];
					if (listener != null) {
						try {
							listener.idle(eventWithoutMessage);
						}
						catch (Throwable t) {
							fireExceptionCaught(eventWithoutMessage, t);
						}
					}
				}
			}
			lastIdleTime = System.currentTimeMillis();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Processes messageReceived event. Don't call this method directly, but
	 * use <code>MessageSocketRunner</code> to drive the socket.
	 *
	 * @return true if messageReceived event occurred.
	 *
	 * @see MessageSocketRunner
	 */
	public boolean processMessageReceived() {
		MessageSocketListener[] listeners = this.listeners;
		MessageSocketListener listener;
		MessageSocketEvent mse;
		
		int i;
		int recognizeCode;
		int candidatesToCheck = leftCandidates;
		Message m;

		int clueLen;

		if (!ensureConnection())
			return false;

		try {
			int availableBytes = in.available();
			if (availableBytes <= 0) {
				return false; // nothing to receive
			}
			if (noMoreReceive) {
				in.skip(availableBytes);
				return false;
			}
		}
		catch (IOException ioex) {
			fireExceptionCaught(ioex);
			noMoreReceive = true;
			close();
			return false;
		}

		if (leftCandidates == 0) {
			try {
				clueLen = in.available();
			} catch (IOException e) {
				clueLen = maxClueLen;
			}
			
			if (clueLen > maxClueLen) {
				clueLen = maxClueLen;
			}

			fireExceptionCaught(new UnknownMessageException(Bytes.getHexdump(clue, 0, clueLen)));
			noMoreReceive = true;
			close();
			return false;
		}

		mainloop : for (i = firstCandidate; i < mtab.length; i++) {
			if (!candidates[i]) { // not a candidate, skip.
				// adjust firstCandidate if necessary
				if (i == firstCandidate) {
					firstCandidate++;
				}
				continue;
			}

			m = mtab[i];

			// initialize first against unexpected user exception
			recognizeCode = Message.RECOGNIZE_NO;

			// inspect the clue and determine the data is mtab[i]'s.
			in.mark(Integer.MAX_VALUE);

			try {
				clueLen = in.available();
				if (clueLen > maxClueLen) {
					clueLen = maxClueLen;
				}
				in.read(clue, 0, clueLen);
				recognizeCode = m.recognize(clue, clueLen);
			}
			catch (Throwable t) {
				fireExceptionCaught(t);
				noMoreReceive = true;
				close();
				break mainloop; // get out of the recognition loop
			}
			finally {
				try {
					in.reset();
				}
				catch (IOException ioe) {}
			}

			// interpret recognize code
			switch (recognizeCode) {
				case Message.RECOGNIZE_NO :
					// this message is not a candidate anymore.
					removeCandidate(i);
					break;
				case Message.RECOGNIZE_NOT_YET :
					// this message is not recognizable yet.
					break;
				case Message.RECOGNIZE_YES :
					// this message is the message I have been waiting!
					// read messages
					try {
						// reset candidates table
						resetCandidates();

						// read the message
						m.read(safeIn);
						// blocked I/O will be performed. timeout exceptions should be processed.

						lastIdleTime = System.currentTimeMillis();
						mse = new MessageSocketEvent(this, m);
						int len = listeners.length;
						synchronized (listeners) {
							for (i = 0; i < len; i++) {
								listener = listeners[i];
								if (listener != null) {
									try {
										listener.messageReceived(mse);
									}
									catch (Throwable t) {
										fireExceptionCaught(mse, t);
									}
								}
							}
						}
						return true;
					}
					catch (Throwable t) {
						fireExceptionCaught(t);
						noMoreReceive = true;
						close();
						break mainloop;
					}
				default :
					fireExceptionCaught(
						new RuntimeException(
							"recognize() returned wrong code: "
								+ recognizeCode));
					noMoreReceive = true;
					close();
			} // end of switch-case

			// if there are no more candidates to check, exit the loop.
			if (candidatesToCheck == 1)
				break;
			else {
				candidatesToCheck--;
			}
		} // end of for

		return false;
	}

	/**
	 * Processes messageSent event. Don't call this method directly, but
	 * use <code>MessageSocketRunner</code> to drive the socket.
	 *
	 * @return true if messageSent event occurred.
	 *
	 * @see MessageSocketRunner
	 */
	public boolean processMessageSent() {
		Message m;

		MessageSocketListener[] listeners = this.listeners;
		MessageSocketListener listener;
		MessageSocketEvent mse;
		
		if (!ensureConnection())
			return false;

		if (!qempty()) { // if queue contains something to send.
			m = qget();
			try {
				m.write(out);
				out.flush();
			}
			catch (Throwable t) {
				fireExceptionCaught(t);
				noMoreReceive = true;
				close();
				qclear();
				return false;
			}
			lastIdleTime = System.currentTimeMillis();
			mse = new MessageSocketEvent(this, m);
			int len = listeners.length;
			synchronized (listeners) {
				for (int i = 0; i < len; i++) {
					listener = listeners[i];
					if (listener != null) {
						try {
							listener.messageSent(mse);
						}
						catch (Throwable t) {
							fireExceptionCaught(mse, t);
						}
					}
				}

			}
			return true;
		}
		else if (closing) { // if queue is empty and connection is waiting for closing.
			closeConnection();
		}

		return false;
	}

	/**
	 * Processes timer event. Don't call this method directly, but
	 * use <code>MessageSocketRunner</code> to drive the socket.
	 *
	 * @return true if timer event occurred.
	 *
	 * @see MessageSocketRunner
	 */
	public boolean processTimer() {
		int len;
		
		MessageSocketListener[] listeners = this.listeners;
		MessageSocketListener listener;
		
		if (!ensureConnection())
			return false;

		if (nextTimerTime > 0L
			&& System.currentTimeMillis() > nextTimerTime) {
			nextTimerTime = 0L;
			len = listeners.length;
			synchronized (listeners) {
				for (int i = 0; i < len; i++) {
					listener = listeners[i];
					if (listener != null) {
						try {
							listener.timer(eventWithoutMessage);
						}
						catch (Throwable t) {
							fireExceptionCaught(t);
						}
					}
				}

			}
			return true;
		}

		return false;
	}

	private boolean ensureConnection() {
		if (closed)
			return false;
		else if (!established)
			establishConnection();

		return true;
	}

	/**
	 * Fires connectionEstablished event.
	 */
	private void establishConnection() {
		int len = listeners.length;

		MessageSocketListener[] listeners = this.listeners;
		MessageSocketListener listener;

		synchronized (listeners) {
			for (int i = 0; i < len; i++) {
				listener = listeners[i];
				if (listener != null) {
					try {
						listener.connectionEstablished(eventWithoutMessage);
					}
					catch (Throwable t) {
						fireExceptionCaught(t);
					}
				}
			}

		}
		lastIdleTime = System.currentTimeMillis();
		established = true;
	}

	/**
	 * Closes the connection really.
	 */
	private synchronized void closeConnection() {
		if ( closed ) return;
		
		MessageSocketListener[] listeners = this.listeners;
		MessageSocketListener listener;
		
		closing = true;
		try {
			in.close();
		}
		catch (IOException _ex) {}
		try {
			out.close();
		}
		catch (IOException _ex) {}
		try {
			socket.close();
		}
		catch (IOException _ex) {}
		int len = listeners.length;
		synchronized (listeners) {
			for (int i = 0; i < len; i++) {
				listener = listeners[i];
				if (listener != null) {
					try {
						listener.connectionClosed(eventWithoutMessage);
					}
					catch (Throwable t) {
						fireExceptionCaught(t);
					}
				}
			}

		}

		idleTime = 0;
		lastIdleTime = 0L;
		nextTimerTime = 0L;
		established = false;
		closed = true;
		closing = false;
		noMoreReceive = true;

		socket = null;
		protocol = null;
		in = null;
		safeIn = null;
		out = null;

		notifyAll();
	}

	private void resetCandidates() {
		Arrays.fill(candidates, true);
		leftCandidates = mtab.length;
		firstCandidate = 0;
	}

	private void removeCandidate(int idx) {
		candidates[idx] = false;
		leftCandidates--;
	}

	private void fireExceptionCaught(Throwable ex) {
		MessageSocketListener[] listeners = this.listeners;
		MessageSocketListener listener;
		MessageSocketEvent mse = new MessageSocketEvent(this, ex);
		
		int len = listeners.length;
		synchronized (listeners) {
			for (int i = 0; i < len; i++) {
				listener = listeners[i];
				if (listener != null) {
					listener.exceptionCaught(mse);
				}
			}
		}
	}

	private void fireExceptionCaught(
		MessageSocketEvent origEvnt,
		Throwable ex) {
		
		MessageSocketListener[] listeners = this.listeners;
		MessageSocketListener listener;
		
		MessageSocketEvent mse =
			new MessageSocketEvent(this, origEvnt.getMessage(), ex);
			
		int len = listeners.length;
		synchronized (listeners) {
			for (int i = 0; i < len; i++) {
				listener = listeners[i];
				if (listener != null) {
					listener.exceptionCaught(mse);
				}
			}
		}
	}

	///////////////////////////////////////
	// messageQueue manipulation methods //
	///////////////////////////////////////

	private boolean qempty() {
		return (firstOffset == lastOffset && !queueFull);
	}

	private void qadd(Message message) {
		synchronized (messageQueue) {
			if (queueFull) {
				qexpand();
			}

			messageQueue[lastOffset++] = message;
			lastOffset %= messageQueue.length;

			if (lastOffset == firstOffset) {
				queueFull = true;
			}
		}
	}

	private Message qget() {
		Message ret;
		synchronized (messageQueue) {
			ret = messageQueue[firstOffset];
			messageQueue[firstOffset] = null;
			firstOffset ++;
			firstOffset %= messageQueue.length;
			if (queueFull) {
				queueFull = false;
			}
		}

		return ret;
	}

	private void qexpand() {
		int oldLen = messageQueue.length;
		Message[] newMessageQueue = new Message[oldLen * 2];
		if (firstOffset < lastOffset) {
			System.arraycopy(
				messageQueue,
				firstOffset,
				newMessageQueue,
				0,
				lastOffset - firstOffset);
		}
		else {
			System.arraycopy(
				messageQueue,
				firstOffset,
				newMessageQueue,
				0,
				oldLen - firstOffset);
			System.arraycopy(
				messageQueue,
				0,
				newMessageQueue,
				oldLen - firstOffset,
				lastOffset);
		}

		firstOffset = 0;
		lastOffset = oldLen;
		messageQueue = newMessageQueue;
		queueFull = false;
	}

	private void qclear() {
		synchronized (messageQueue) {
			Arrays.fill(messageQueue, null); 
			firstOffset = 0;
			lastOffset = 0;
			queueFull = false;
		}
	}
}
