//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

import java.util.EventObject;

/**
 * <p>
 * An <code>EventObject</code> that indicates message socket related
 * event has occurred.
 * </p>
 *
 * <h2>CHANGELOG</h2>
 * <h3>1.1 <small>2003. 2. 25.</small></h3>
 * <p><ul>
 *   <li>Fixed constructor argument type properly.</li>
 *   <li>Added {@link #getSocket()} method.</li>
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
 */
public class MessageSocketEvent extends EventObject {
    
    private MessageSocket ms;
    private Message message;
    private Throwable exception;
    
    /**
     * Constructs a new <code>MessageSocketEvent</code>.
     */
    public MessageSocketEvent(MessageSocket source) {
        super(source);
        this.ms = source;
        this.message = null;
        this.exception = null;
    }
    
    /**
     * Constructs a new <code>MessageSocketEvent</code>.
     */
    public MessageSocketEvent(MessageSocket source, Message message) {
        super(source);
		this.ms = source;
        this.exception = null;
        this.message = message;
    }
    
    /**
     * Constructs a new <code>MessageSocketEvent</code>.
     */
    public MessageSocketEvent(MessageSocket source, Throwable exception) {
        super(source);
		this.ms = source;
        this.message = null;
        this.exception = exception;
    }
    
    /**
     * Constructs a new <code>MessageSocketEvent</code>.
     */
    public MessageSocketEvent(MessageSocket source, Message message, Throwable exception) {
        super(source);
		this.ms = source;
        this.message = message;
        this.exception = exception;
    }
    
    /**
     * Returns the {@link MessageSocket} object who generated ths event.
     * 
     */
    public MessageSocket getSocket() {
    	return ms;
    }

    /**
     * Returns the <code>Message</code> object related with this event.
     *
     * @return null if no message is related with.
     */
    public Message getMessage() {
        return message;
    }
    
    /**
     * Returns the exception related with this event.
     *
     * @return null if no exception is related with.
     */
    public Throwable getException() {
        return exception;
    }
}
