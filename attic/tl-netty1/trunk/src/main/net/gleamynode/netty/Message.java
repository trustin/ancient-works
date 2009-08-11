//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * <p>
 * An interface that represents a TCP/IP message.
 * It should recognize itself by reading bytes from InputStream,
 * read from InputStream, and write into OutputStream.
 * </p>
 * <p>
 * Any classes which implements this interface must have a default 
 * constructor. If you want to receive the message properly, you 
 * have to implement {@link #read(InputStream)} method to set its all 
 * properties. Messages can be either read-only or write-only if
 * only used for receiving/sending.it. See {@link #read(InputStream)}
 * and {@link #write(OutputStream)} method for detail.
 * </p>
 * <p>
 * Use <code>instanceof</code> operator to determine the type of the message.
 * Netty doesn't determine message types by an integer code anymore.
 * </p>
 * 
 * <p>
 * <b>NOTE</b>: 'clue' is always the first n bytes of the message.
 *              You can safely consider it as a message header.
 * </p>
 * 
 * <h2>CHANGES</h2>
 * <h3>2.1 <small>2003. 2. 28.</small></h3>
 * <p><ul>
 *   <li>{@link #read(InputStream)} and {@link #write(OutputStream)}
 *       can throw {@link UnsupportedOperationException} for the case
 *       that the message is read/write-only.</li>
 * </ul></p>
 * 
 * <h3>2.0 <small>2003. 2. 24.</small></h3>
 * <p><ul>
 *   <li>Removed <code>clone()</code> method. Instead of it,
 *       <code>Messages</code> must implement a default constructor and
 *       be able to set its properties with {@link #read(InputStream)}
 *       method.</li>
 *   <li>Removed <code>getMessageType()</code> method.
 *       Instead of it, you have to use the <code>class</code> of the 
 *       <code>Message</code> instance to determine the type of the
 *       message.</li>
 *   <li>Added {@link #getClueLength()} method.</li>
 *   <li>Updated license statement.</li>
 * </ul></p>
 * 
 * <h3>1.0</h3>
 * <p><ul>
 *   <li>The initial release.</li>
 * </ul></p>
 * 
 * @author  Trustin Lee
 * @version 2.1
 */
public interface Message {
    /**
     * Means it is definite the incoming message is not this type of message.
     */
    public static final int RECOGNIZE_NO = 0;
    /**
     * Means it is not able to determine the incoming message is this type of 
     * message or not (needs more clue).
     */
    public static final int RECOGNIZE_NOT_YET = 1;
    /**
     * Means it is definite the incoming message is this type of message.
     */
    public static final int RECOGNIZE_YES = 2;
    
    
    /**
     * Returns the minimum number of bytes to be read to determine the incoming
     * data represents this message.
     * 
     * Clue is usually the length of message header.
     * This method should always return the same, positive value.
     */
    int getClueLength();
    
    /**
     * Recognizes the specified <code>clue</code> which determines that
     * incoming message is this type of message.
     * 
     * Never modify the content of <code>clue</code>, {@link MessageSocket} 
     * reuses it.
     * 
     * @param len the length of the <code>clue</code>. This can be smaller than
     *             <code>clue.length</code>.
     *
     * @return {@link #RECOGNIZE_NO} - incoming message is never myself.
     *         {@link #RECOGNIZE_NOT_YET} - There is possibility that incoming 
     *                                      message is myself.
     *         {@link #RECOGNIZE_YES} - incoming message is definitely myself.
     */
    int recognize(byte[] clue, int len);
    
    /**
     * Reads the message from the specified <code>InputStream</code>.
     * 
     * Throw a {@link UnsupportedOperationException} if the message is
     * write-only (in case that you send the message but don't receive.)
     *
     * @throws InvalidMessageFormatException if the wrong message
     *                                       data was read.
     * 
     * @throws IOException if I/O error occurred reading the message.
     * @throws UnsupportedOperationException if the message is write-only.
     */
    void read(InputStream in) throws IOException, UnsupportedOperationException;
    
    /**
     * Writes this message into the specified <code>OutputStream</code>.
     * 
     * Throw a {@link UnsupportedOperationException} if the message is
     * read-only (in case that you receive the message but don't send.)
     *
     * @throws IOException if I/O error occurred writing the message.
     * @throws UnsupportedOperationException if the message is read-only.
     */
    void write(OutputStream out) throws IOException, UnsupportedOperationException;
}
