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
 * A decendant of {@link java.io.ByteArrayOutputStream} that user can
 * directly access the internal byte array for performance gain.
 *
 * @author Trustin Lee
 * @version $Revision: 1.2 $, $Date: 2004/03/12 05:22:32 $
 */
public class ByteArrayOutputStream extends java.io.ByteArrayOutputStream {
    public ByteArrayOutputStream() {
        super();
    }

    public ByteArrayOutputStream(int size) {
        super(size);
    }

    /**
     * returns the internal byte array.
     */
    public byte[] getByteArray() {
        return buf;
    }

    /**
     * the number of bytes written actually.
     */
    public int getCount() {
        return count;
    }
}
