//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

import java.io.IOException;

/**
 * <p>
 * An <code>IOException</code> which is related TCP/IP message recognition.
 * </p>
 *
 * @author  Trustin Lee
 * @version 1.0
 */
public class MessageSocketException extends IOException {
    /**
     * Constructs a new <code>MessageSocketException</code>.
     */
    public MessageSocketException() {
    }
    
    /**
     * Constructs a new <code>MessageSocketException</code> with
     * the specified description.
     */
    public MessageSocketException(String message) {
        super(message);
    }
}