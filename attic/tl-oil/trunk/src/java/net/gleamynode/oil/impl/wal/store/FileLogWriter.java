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
 * @(#) $Id: FileLogWriter.java 32 2004-11-09 14:37:16Z trustin $
 */
package net.gleamynode.oil.impl.wal.store;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.collections.Buffer;

import net.gleamynode.io.ByteArrayOutputStream;

import net.gleamynode.oil.OilException;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 32 $, $Date: 2004-11-09 23:37:16 +0900 (화, 09 11월 2004) $
 */
class FileLogWriter {
    private final OutputStream out;
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private final CompactObjectOutputStream bufOut;
    private final int maxLogSize;

    public FileLogWriter(ClassCatalog catalog, OutputStream out, int maxLogSize)
                  throws IOException {
        this.out =
            new BufferedOutputStream(out,
                                     FileLogStoreConstants.DEFAULT_IO_BUFFER_SIZE);
        this.bufOut = new CompactObjectOutputStream(catalog, buf);
        this.maxLogSize = maxLogSize;
    }

    public void close() throws IOException {
        out.close();
    }

    public void flush(Buffer logBuf) {
        Object log;

        try {
            for (;;) {
                synchronized (logBuf) {
                    if (logBuf.isEmpty()) {
                        break;
                    }

                    log = logBuf.remove();
                }

                out.write(FileLogStoreConstants.LOG_HEADER);
                writeObject(log);
            }

            out.flush();
        } catch (IOException e) {
            throw new OilException("failed to write log.", e);
        }
    }

    private void writeObject(Object obj) throws IOException {
        buf.reset();
        bufOut.reset();
        bufOut.writeObject(obj);
        bufOut.flush();

        final int size = buf.getCount();

        if (size > maxLogSize) {
            throw new IOException("Serialized object exceeds " + maxLogSize +
                                  ": " + size);
        }

        out.write((size >>> 24) & 0xFF);
        out.write((size >>> 16) & 0xFF);
        out.write((size >>> 8) & 0xFF);
        out.write(size & 0xFF);

        final byte[] data = buf.getByteArray();
        short checksum = 0;

        // calculate checksum
        for (int i = size - 1; i >= 0; i--) {
            checksum += Math.abs(data[i]);
        }

        // write checksum and the object
        out.write((checksum >>> 8) & 0xFF);
        out.write(checksum & 0xFF);
        out.write(buf.getByteArray(), 0, size);
    }
}
