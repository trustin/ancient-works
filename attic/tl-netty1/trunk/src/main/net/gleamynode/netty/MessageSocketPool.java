//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;
import java.io.IOException;
import java.net.Socket;

/**
 * <p>
 * A pool of {@link MessageSocket}s.
 * 
 * It creates many {@link MessageSocket} objects with the same 
 * {@link Protocol} to reduce instance creation overhead.
 * 
 * The pool reuses {@link MessageSocket} instances and automatically
 * increases its capacity if necessary.
 * </p>
 * <p>
 * <b>Thread safety</b>: Yes
 * </p>
 * 
 * <h2>CHANGELOG</h2>
 * <h3>0.1 <small>2003. 2. 25.</small></h3>
 * <p><ul>
 *   <li>The initial release.</li>
 * </ul></p>
 * 
 * @version 0.1
 * @author Trustin
 * 
 * @see MessageSocket
 */
public class MessageSocketPool {
	private static final int DEFAULT_SIZE = 32;
	
	private Protocol protocol;
	private MessageSocket[] sockets;
	private int available;
	private int inUse;
	
	private SocketCloseListener scl = new SocketCloseListener();
	
	/**
	 * Constructs a new instance of {@link MessageSocketPool} that pools
	 * the {@link MessageSocket}s that handles the specified {@link Protocol}.
	 * 
	 * The default size of the pool is 32.
	 */
	public MessageSocketPool(Protocol protocol) {
		this(protocol, DEFAULT_SIZE);
	}
	
	/**
	 * Constructs a new instance of {@link MessageSocketPool} that pools
	 * the {@link MessageSocket}s that handles the specified {@link Protocol}.
	 * 
	 * @param size the size of the pool.
	 */
	public MessageSocketPool(Protocol protocol, int size) {
		if ( protocol == null ) {
			throw new NullPointerException("protocol is null");
		}
		
		if ( size <= 0 ) {
			throw new IllegalArgumentException("size(" + size + ") <= 0");
		}
		
		this.available = size;
		this.inUse = 0;
		this.protocol = protocol;
		
		this.sockets = new MessageSocket[size];
		
		for ( int i = size; i > 0; i ++ ) {
			addNewSocket();
		}
	}
	
	/**
	 * Returns an instance of {@link MessageSocket} which uses the specified
	 * {@link Socket} to communicate.
	 * 
	 * @throws IOException setting the {@link Socket} of the 
	 *                     {@link MessageSocket} object failed.
	 */
	public synchronized MessageSocket getSocket(Socket s) throws IOException {
		MessageSocket ms = removeSocket();
		ms.setSocket(s);
		inUse ++;
		
		return ms;
	}
	
	/**
	 * The size of the pool.
	 * 
	 * It returns the same result with the expression 
	 * <code>available() + inUse()</code>.
	 */
	public int size() {
		return available + inUse;
	}
	
	/**
	 * Returns the number of available {@link MessageSocket}s.
	 * 
	 * This value will decrease when user calls {@link #getSocket(Socket)}.
	 * This value will increase when user closes the socket.
	 */
	public int available() {
		return available;
	}
	
	/**
	 * Returns the number of {@link MessageSocket}s that are in use by user
	 * 
	 * This value will increase when user calls {@link #getSocket(Socket)}.
	 * This value will decrease when user closes the socket.
	 */
	public int inUse() {
		return inUse;
	}
	
	/**
	 * Resizes the pool to the specified size..
	 * 
	 * It might not decrease as many as you want to, because some sockets
	 * might be in use by user so that they can't be released. 
	 */
	public synchronized void setSize(int newSize) {
		int curSize = available + inUse;
		int i;
		int start;
		
		if ( newSize > curSize ) {
			for ( i = newSize - curSize; i > 0; i -- ) {
				addNewSocket();
			}
		}
		else if ( newSize < curSize ) {
			start = curSize - newSize;
			if ( start > available ) {
				start = available;
			}
			
			for ( i = start; i > 0; i -- ) {
				removeSocket();
			}
		}
	}
	
	private MessageSocket addNewSocket() {
		MessageSocket s = new MessageSocket(protocol);
		s.addMessageSocketListener(scl);
		addSocket(s);

		return s;
	}
	
	private void addSocket(MessageSocket ms) {
		int curSize = available + inUse;
		int newSize;
		MessageSocket[] newSockets;
		
		if ( available == curSize ) {
			// ensure capacity
			newSize = curSize << 1;
			newSockets = new MessageSocket[newSize];
		
			System.arraycopy(sockets, 0, newSockets, 0, curSize);
		
			sockets = newSockets;
		}

		sockets[available++] = ms;
	}
	
	private MessageSocket removeSocket() {
		int t = --available;
		MessageSocket ret = sockets[t];

		sockets[t] = null;
		return ret;
	}
	
	private class SocketCloseListener implements MessageSocketListener {
		public void connectionEstablished(MessageSocketEvent evt) {
		}

		public void connectionClosed(MessageSocketEvent evt) {
			synchronized ( MessageSocketPool.this ) {
				addSocket(evt.getSocket());
				inUse --;
			}
		}

		public void messageReceived(MessageSocketEvent evt) {
		}

		public void messageSent(MessageSocketEvent evt) {
		}

		public void idle(MessageSocketEvent evt) {
		}

		public void timer(MessageSocketEvent evt) {
		}

		public void exceptionCaught(MessageSocketEvent evt) {
		}
	}
}
