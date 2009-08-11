//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * <p>
 * A message-based version of <code>java.net.ServerSocket</code>.
 * It has almost same interface &amp; behavior with
 * <code>java.net.ServerSocket</code>. The only difference is
 * that it returns <code>MessageSocket</code>
 * instead of <code>java.net.Socket</code> when the method
 * <code>accept()</code> is called.
 * </p>
 * <p>
 * {@link MessageServerSocket} uses {@link MessageSocketPool} to
 * reduce the overhead of creating instances of {@link MessageSocket}.
 * You can call {@link #getPool()} method to get the pool object and
 * resize it.
 * </p>
 *
 * <h2>CHANGELOG</h2>
 * <h3>1.1 <small>2003. 2. 25.</small></h3>
 * <p><ul>
 *   <li>Utilizes {@link MessageSocketPool}.</li>
 *   <li>Updated license statement.</li>
 * </ul></p>
 * 
 * <h3>1.0</h3>
 * <p><ul>
 *   <li>The initial release.</li>
 * </ul></p>
 * 
 * @author  Trustin Lee
 * @version 1.1
 *
 * @see MessageSocket
 * @see MessageSocketPool
 * @see java.net.ServerSocket
 */
public class MessageServerSocket {
    private ServerSocket serverSocket;
    private MessageSocketPool pool;
    
    /**
     * Constructs a new <code>MessageServerSocket</code> on a specified port
     * with the specified protocol.
     */
    public MessageServerSocket(Protocol protocol, int port) throws IOException {
        if (protocol == null) {
            throw new NullPointerException("protocol is null");
        }
        else {
            serverSocket = new ServerSocket(port);
			this.pool = new MessageSocketPool(protocol);
        }
    }
    
    /**
     * Constructs a new <code>MessageServerSocket</code> on a specified port
     * with the specified protocol and backlog (the maximum length of the
     * queue).
     */
    public MessageServerSocket(Protocol protocol, int port, int backlog) throws IOException {
        if (protocol == null) {
            throw new NullPointerException("protocol is null");
        }
        else {
            serverSocket = new ServerSocket(port, backlog);
			this.pool = new MessageSocketPool(protocol);
        }
    }
    
    /**
     * Constructs a new <code>MessageServerSocket</code> on a specified port
     * with the specified protocol, backlog (the maximum length of the
     * queue), and the local IP address to bind to.
     */
    public MessageServerSocket(Protocol protocol, int port, int backlog, InetAddress bindAddr) throws IOException {
        if (protocol == null) {
            throw new NullPointerException("protocol is null");
        }
        else {
            serverSocket = new ServerSocket(port, backlog, bindAddr);
			this.pool = new MessageSocketPool(protocol);
        }
    }
    
    /**
     * Listens for a connection to be made to this message socket and accepts it.
     */
    public MessageSocket accept() throws IOException {
        return pool.getSocket(serverSocket.accept());
    }
    
    /**
     * Returns the {@link MessageSocketPool} that this server socket gets 
     * {@link MessageSocket} from.
     */
    public MessageSocketPool getPool() {
    	return pool;
    }
    
    public void close() throws IOException {
        serverSocket.close();
    }
    
    public InetAddress getInetAddress() {
        return serverSocket.getInetAddress();
    }
    
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }
    
    public int getSoTimeout() throws IOException {
        return serverSocket.getSoTimeout();
    }
    
    public void setSoTimeout(int newTimeout) throws IOException {
        serverSocket.setSoTimeout(newTimeout);
    }
    
    public String toString() {
        return "MessageSocket(" + serverSocket.toString() + ')';
    }
}
