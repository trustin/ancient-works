//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

import java.util.EventListener;

/**
 * <p>
 * An <code>EventListener</code> that handles <code>MessageSocketEvent</code>s.
 * You'll implement the protocol by implementing this interface.
 * </p>
 *
 * @author  Trustin Lee
 * @version 1.0
 */
public interface MessageSocketListener extends EventListener {
    /**
     * Invoked when the connection is established.
     */
    void connectionEstablished(MessageSocketEvent evt);

    /**
     * Invoked when the connection is closed.
     */
    void connectionClosed(MessageSocketEvent evt);

    /**
     * Invoked when the <code>MessageSocket</code> received a TCP/IP message.
     */
    void messageReceived(MessageSocketEvent evt);

    /**
     * Invoked when the <code>MessageSocket</code> sent a TCP/IP message.
     */
    void messageSent(MessageSocketEvent evt);
    
    /**
     * Invoked when no messages is being exchanged for the idle time
     * specified in <code>MessageSocket</code>
     */
    void idle(MessageSocketEvent evt);

    /**
     * Invoked when it is the time that the user wanted to be alarmed by
     * <code>MessageSocket</code>.
     */
    void timer(MessageSocketEvent evt);
    
    /**
     * Invoked when the exception was caught while processing the connection.
     */
    void exceptionCaught(MessageSocketEvent evt);
}
