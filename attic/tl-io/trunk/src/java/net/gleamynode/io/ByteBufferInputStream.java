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
import java.io.InputStream;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;


/**
 * An input stream that reads data from <code>java.nio.ByteBuffer</code>.
 * <p>
 * This input stream does not support mark/reset.
 *
 * @author Trustin Lee
 * @version $Revision: 1.2 $, $Date: 2004/03/03 06:49:16 $
 */
public class ByteBufferInputStream extends InputStream {
    private ByteBuffer buf;

    public ByteBufferInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public int available() {
        return buf.limit() - buf.position();
    }

    public void close() {
    }

    public void mark(int readlimit) {
    }

    public boolean markSupported() {
        return false;
    }

    public void reset() throws IOException {
        throw new IOException("mark not supported");
    }

    public int read() {
        try {
            return buf.get() & 0xFF;
        } catch (BufferUnderflowException bue) {
            return -1;
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int maxLen = buf.limit() - buf.position();

        if (maxLen == 0) {
            return -1;
        }

        if (len > maxLen) {
            len = maxLen;
        }

        buf.get(b, off, len);
        return len;
    }

    public int read(byte[] b) throws IOException {
        int maxLen = buf.limit() - buf.position();

        if (maxLen == 0) {
            return -1;
        }

        int len = b.length;

        if (len > maxLen) {
            len = maxLen;
        }

        buf.get(b, 0, len);
        return len;
    }

    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0L;
        }

        int oldPos = buf.position();
        int newPos = (int) (oldPos + n);
        int limit = buf.limit();

        if (newPos > limit) {
            newPos = limit;
        }

        buf.position(newPos);
        return newPos - oldPos;
    }
}
