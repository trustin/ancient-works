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
/*
 * @(#) $Id$
 */
package net.gleamynode.io;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;


/**
 * An output stream that writes data into <code>java.nio.ByteBuffer</code>.
 *
 * @author Trustin Lee
 * @version $Revision: 1.2 $, $Date: 2004/03/03 06:43:09 $
 */
public class ByteBufferOutputStream extends OutputStream {
    private ByteBuffer buf;

    public ByteBufferOutputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public void close() {
    }

    public void flush() {
    }

    public void write(int b) throws IOException {
        try {
            buf.put((byte) (b & 0xFF));
        } catch (BufferOverflowException boe) {
            throw (IOException) new IOException().initCause(boe);
        } catch (ReadOnlyBufferException robe) {
            throw (IOException) new IOException().initCause(robe);
        }
    }

    public void write(byte[] b, int off, int len)
               throws IOException {
        try {
            buf.put(b, off, len);
        } catch (BufferOverflowException boe) {
            throw (IOException) new IOException().initCause(boe);
        } catch (ReadOnlyBufferException robe) {
            throw (IOException) new IOException().initCause(robe);
        }
    }

    public void write(byte[] b) throws IOException {
        try {
            buf.put(b);
        } catch (BufferOverflowException boe) {
            throw (IOException) new IOException().initCause(boe);
        } catch (ReadOnlyBufferException robe) {
            throw (IOException) new IOException().initCause(robe);
        }
    }
}
