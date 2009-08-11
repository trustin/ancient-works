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
 * An exception that is thrown when the received bytes from the socket
 * was not recognizable by any <code>Message</code> objects.
 * </p>
 *
 * @author  Trustin Lee
 * @version 1.0
 *
 * @see MessageSocket
 */
public class UnknownMessageException extends MessageSocketException {
    /**
     * Constructs a new <code>UnknownMessageException</code>.
     */
    public UnknownMessageException() {
    }
    
    /**
     * Constructs a new <code>UnknownMessageException</code>
     * with some description.
     */
    public UnknownMessageException(String message) {
        super(message);
    }
}