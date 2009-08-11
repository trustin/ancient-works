/*
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package net.gleamynode.netty2;

import java.nio.ByteBuffer;


/**
 * <p>
 * An interface that represents a message read from/written into channels. It
 * should know how to read/write itself from/into the channel.
 * </p>
 *
 * <p>
 * Please use <code>instanceof</code> operator to determine the type of the
 * message.
 * </p>
 *
 * @author Trustin Lee (http://gleamynode.net/dev/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
public interface Message {
    /**
     * Reads the message from the specified buffer. I/O worker thread reads
     * incoming data from the channel into the buffer, flips it, and passes it
     * as an argument; the <code>position</code> will be <code>0</code> and
     * <code>limit</code> will be the number of bytes read.
     *
     * @return <code>true</code> if the message is read fully.
     *         <code>false</code> if the buffer is exhausted and more bytes
     *         are left to read, or if more bytes are required to complete the
     *         message.
     * @throws MessageParseException
     *             if the bytes in the buffer is not understandable.
     */
    boolean read(ByteBuffer buffer) throws MessageParseException;

    /**
     * Writes this message into the specified buffer. I/O worker thread will
     * flip the buffer and write it into the channel until this method returns
     * <code>true</code>. the <code>position</code> will be <code>0</code>
     * and <code>limit</code> will be the same as <code>capacity</code>.
     *
     * @return <code>true</code> if the message is written fully.
     *         <code>false</code> if the buffer is full and more bytes are
     *         left to write, or the message wants to write the remainder at
     *         next invocation.
     */
    boolean write(ByteBuffer buffer);
}
