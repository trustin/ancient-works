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


/**
 * <p>
 * An {@link java.io.OutputStream} that outputs everything in hexadecimal form.
 * Useful for examining binary data.
 * </p>
 * <p>
 * <b>Thread safety</b>: {@link #write(byte[])}, {@link #write(byte[],int,int)}
 *                       is <b>NOT</b> synchronized.
 * </p>
 *
 * @author Trustin Lee (<a href="http://projects.gleamynode.net/">http://projects.gleamynode.net/</a>)
 *
 * @version $Revision: 1.4 $
 *
 * @see java.io.OutputStream
 */
public class HexadecimalOutputStream extends OutputStream {
    private static final int BUFFER_SIZE = 4096;

    // lookup table
    private static final byte[] table;

    // initialize the lookup table
    static {
        final byte[] d2h =
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        byte[] newTable = new byte[512];
        int i;

        for (i = 0; i < 256; i++) {
            newTable[i * 2] = d2h[i / 16];
            newTable[i * 2] = d2h[i % 16];
        }

        table = newTable;
    }

    private OutputStream out;
    private byte[] buffer = new byte[BUFFER_SIZE];

    /**
     * Constructs a new <code>HexadecimailOutputStream</code> with the specified
     * <code>OutputStream</code>.
     *
     * @throws NullPointerException if <code>out</code> is <code>null</code>.
     */
    public HexadecimalOutputStream(OutputStream out) {
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        this.out = out;
    }

    public void close() throws IOException {
        out.close();
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len)
               throws IOException {
        int i;
        int tableOffset;
        int end = off + len;
        int bufferPos = 0;

        for (i = off; i < end; i++) {
            tableOffset = b[i];

            if (tableOffset < 0) {
                tableOffset += 256;
            }

            tableOffset <<= 1;

            buffer[bufferPos++] = table[tableOffset];
            buffer[bufferPos++] = table[tableOffset + 1];

            if (bufferPos >= BUFFER_SIZE) {
                out.write(buffer);
                bufferPos = 0;
            }
        }

        if (bufferPos > 0) {
            out.write(buffer, 0, bufferPos);
        }
    }

    public void write(int b) throws IOException {
        if (b < 0) {
            b += 256;
        }

        if ((b < 0) || (b >= 256)) {
            throw new IllegalArgumentException(String.valueOf(b));
        }

        out.write(table, b << 1, 2);
    }
}

//
//  CHANGELOG
// ===========
//  $Log: HexadecimalOutputStream.java,v $
//  Revision 1.4  2003/04/21 04:10:43  anoripi
//  Utilized CVS keyword substitution in comments.
//  Replaced license statement at source code to concise one.
//
//
