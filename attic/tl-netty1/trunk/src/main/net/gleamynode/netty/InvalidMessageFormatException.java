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
 * An exception that is thrown when a <code>Message</code> object failed
 * to read bytes from the stream because of the wrong message format.
 * </p>
 *
 * @author  Trustin Lee
 * @version 1.0
 *
 * @see Message
 */
public class InvalidMessageFormatException extends MessageSocketException {
    /**
     * Constructs a new <code>InvalidMessageFormatException</code>.
     */
    public InvalidMessageFormatException() {
    }
    
    /**
     * Constructs a new <code>InvalidMessageFormatException</code>
     * with some description.
     */
    public InvalidMessageFormatException(String message) {
        super(message);
    }
}