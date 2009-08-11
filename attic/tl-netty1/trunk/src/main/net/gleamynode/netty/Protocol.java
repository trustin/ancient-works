//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

/**
 * <p>
 * Represents a set of TCP/IP message types.
 * </p>
 *
 * <h2>CHANGELOG</h2>
 * <h3>2.0</h3>
 * <p><ul>
 *   <li>{@link #getMessages()} replaces <code>getAllMessages()</code>.</li>
 *   <li>Not an interface anymore.</li>
 *   <li>Updated license statement.</li>
 * </ul</p>
 * 
 * <h3>1.2</h3>
 * <p><ul>
 *   <li>Removed unnecessary import statement.</li>
 * </ul</p>
 * 
 * <h3>1.1</h3>
 * <p><ul>
 *   <li><code>getAllMessages()</code> returns the array of
 *       <code>Message</code>s to improve performance.</li>
 *   <li>Added <code>getMaxClueLength</code> to reduce
 *       memory footprint problem of <code>MessageSocket</code>.
 * </ul></p>
 *
 * @author  Trustin Lee
 * @version 2.0
 *
 * @see MessageSocket
 */
public class Protocol {
	private final int maxClueLength;
	private final Class[] types;
	private final int typeCount;
	
	public Protocol(Class[] messageTypes) throws InstantiationException, IllegalAccessException {
		
		int typeCount;

		Class type = null;
		Message m;

		int maxClueLength = Integer.MIN_VALUE;
		int localClueLength;
		
		if ( messageTypes == null ) {
			throw new NullPointerException("messageTypes is null");
		}
		
		typeCount = messageTypes.length;
		
		if ( typeCount == 0 ) {
			throw new IllegalArgumentException("messageTypes is empty");
		}
		
		
		this.typeCount = typeCount;
		this.types = new Class[typeCount];
		
		for ( int i = 0; i < typeCount; i ++ ) {
			type = messageTypes[i];
			
			// check it is instantiatable
			m = (Message)type.newInstance();

			// determine max clue length
			localClueLength = m.getClueLength();
			if ( localClueLength > maxClueLength ) {
				maxClueLength = localClueLength;
			}
			
			this.types[i] = type;
		}
		
		this.maxClueLength = maxClueLength;
	}
	
    /**
     * Returns the maximum length(byte unit) of clue (usually
     * TCP/IP message header length) in this protocol.
     */
    public final int getMaxClueLength() {
    	return maxClueLength;
    }
    
    /**
     * Returns the array of new instances of message types that the procotol
     * can handle.
     * 
     * Everytime this method is invoked, new instances of messages will be 
     * created. 
     */
    public final Message[] getMessages() {
    	int typeCount = this.typeCount;
    	Message[] ret = new Message[typeCount];
    	Class[] types = this.types;

    	
		try {
	    	for ( int i = 0; i < typeCount; i ++ ) {
				ret[i] = (Message)types[i].newInstance();
	    	}
		}
		catch (InstantiationException e) {
			// this cannot happen!
			throw new Error(e);
		}
		catch (IllegalAccessException e) {
			// this cannot happen!
			throw new Error(e);
		}
    	
    	return ret;
    }
    
    /**
     * Returns the number of message types that this protocol can handle.
     */
    public final int getMessageTypeCount() {
    	return typeCount;
    }
}
