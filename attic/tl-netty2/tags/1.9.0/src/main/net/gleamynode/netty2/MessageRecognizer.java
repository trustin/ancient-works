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
 * Converts {@link ByteBuffer}into {@link Message}. I/O worker thread calls
 * {@link #recognize(ByteBuffer)}first to determine the proper {@link Message},
 * and then it calls corresponding {@link Message}'s
 * {@link Message#read(ByteBuffer)}method to generate
 * <code>messageReceived</code> event.
 *
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 * @author Trustin Lee (http://gleamynode.net/dev/)
 */
public interface MessageRecognizer {
    /**
     * Converts {@link ByteBuffer}into {@link Message}.
     *
     * @param buffer
     *            incoming bytes
     * @return the converted message only if this recognizer understands the
     *         incoming data. <code>null</code> otherwise (i.e. the recognizer
     *         cannot determine the message type because the incoming data is
     *         too small yet.)
     * @throws MessageParseException
     *             if this recognizer does not understand the incoming data, or
     *             if the validation is failed.
     */
    Message recognize(ByteBuffer buffer) throws MessageParseException;
}
