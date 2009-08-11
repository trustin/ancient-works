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


/**
 * A decendant of {@link java.io.ByteArrayInputStream} that user can
 * read data randomly to reuse the buffer.
 *
 * @author Trustin Lee
 * @version $Revision$, $Date$
 */
public class ByteArrayInputStream extends java.io.ByteArrayInputStream {
    public ByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    public ByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    /**
     * Returns the current offset of the buffer.
     */
    public int position() {
        return pos;
    }

    /**
     * Sets the current offset of the buffer.
     *
     * @return <code>this</code> for invocation chaining.
     */
    public ByteArrayInputStream position(int newOffset) {
        pos = newOffset;
        return this;
    }

    /**
     * Returns the end offset of the buffer to limit the amount of data
     * user reads.
     */
    public int limit() {
        return count;
    }

    /**
     * Sets the end offset of the buffer to limit the amount of data
     * user reads.  If <code>endOffset</code> exceeds the buffer capacity,
     * it will be set to the buffer capacity.
     *
     * @return <code>this</code> for invocation chaining.
     */
    public ByteArrayInputStream limit(int endOffset) {
        count = Math.min(endOffset, buf.length);
        return this;
    }

    /**
     * Returns <code>limit() - position()</code>.
     */
    public int remaining() {
        return count - pos;
    }

    /**
     * Returns the actual size of the buffer.
     */
    public int capacity() {
        return buf.length;
    }
}
