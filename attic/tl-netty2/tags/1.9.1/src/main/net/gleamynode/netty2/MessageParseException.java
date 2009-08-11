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

import java.io.IOException;

import java.nio.ByteBuffer;


/**
 * An exception that is thrown when {@link MessageRecognizer}or {@link Message}
 * cannot understand or validate incoming data.
 *
 * @author Trustin Lee (http://gleamynode.net/dev/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 */
public class MessageParseException extends IOException {

    private static final long serialVersionUID = 3545236933807648819L;
    
    private ByteBuffer buffer;

    /**
     * Constructs a new instance.
     */
    public MessageParseException() {
    }

    /**
     * Constructs a new instance with the specified message.
     */
    public MessageParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified cause.
     */
    public MessageParseException(Throwable cause) {
        initCause(cause);
    }

    /**
     * Constructs a new instance with the specified message and the specified
     * cause.
     */
    public MessageParseException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    /**
     * Returns the message and the hexdump of the unknown part.
     */
    public String getMessage() {
        String message = super.getMessage();

        if (message == null) {
            message = "";
        }

        if (buffer != null) {
            return message + ((message.length() > 0) ? " " : "")
                   + "(Hexdump: " + ByteBuffers.getHexdump(buffer) + ')';
        } else {
            return message;
        }
    }

    /**
     * Returns unknown message part.
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
}
