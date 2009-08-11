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
 * @(#) $Id: FileLogReader.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal.store;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import net.gleamynode.io.ByteArrayInputStream;
import net.gleamynode.oil.OilException;
import net.gleamynode.oil.impl.wal.log.Log;

/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 */
class FileLogReader {
    private final RandomAccessFile raf;
    
    private final InputStream in;

    private final ClassCatalog catalog;

    private byte[] buf = new byte[1024];

    private ByteArrayInputStream bufIn = new ByteArrayInputStream(buf);

    private CompactObjectInputStream bufObjIn;

    private final int maxLogSize;

    public FileLogReader(ClassCatalog catalog, RandomAccessFile raf, InputStream in,
                         int maxLogSize) throws IOException {
        this.raf = raf;
        this.in = new BufferedInputStream(
                                          in,
                                          FileLogStoreConstants.DEFAULT_IO_BUFFER_SIZE);
        this.catalog = catalog;
        this.bufObjIn = new CompactObjectInputStream(catalog, bufIn);
        this.maxLogSize = maxLogSize;
    }

    public void close() throws IOException {
        in.close();
    }
    
    public long getCurrentReadPointer() {
        try {
            return raf.getFilePointer();
        } catch (IOException e) {
            throw new OilException("Failed to get current file pointer.", e);
        }
    }
    
    public long getLastReadPointer() {
        try {
            return raf.length();
        } catch (IOException e) {
            throw new OilException("Failed to get file length.", e);
        }
    }

    public Log read() {
        try {
            int h1 = in.read();

            if (h1 < 0) {
                return null;
            }

            int h2;

            for (;;) {
                h2 = in.read();

                if (h2 < 0) {
                    return null;
                }

                if ((h1 != FileLogStoreConstants.LOG_HEADER[0])
                    || (h2 != FileLogStoreConstants.LOG_HEADER[1])) {
                    h1 = h2;
                } else {
                    return (Log) readObject();
                }
            }
        } catch (EOFException e) {
            return null;
        } catch (Exception e) {
            throw new OilException("failed to read.", e);
        }
    }

    private Object readObject() throws ClassNotFoundException, IOException {
        final int size;
        final short checksum;

        readFully(in, buf, 0, 6);
        size = ((buf[0] & 0xFF) << 24) | ((buf[1] & 0xFF) << 16)
               | ((buf[2] & 0xFF) << 8) | (buf[3] & 0xFF);
        checksum = (short) (((buf[4] & 0xFF) << 8) | (buf[5] & 0xFF));

        if (size <= 0) {
            throw new IOException("object size is not greater than 0: "
                                  + size);
        }

        if (size > maxLogSize) {
            throw new IOException("object size exceeds " + maxLogSize + ": "
                                  + size);
        }

        if (buf.length < size) {
            buf = new byte[size];
            bufIn = new ByteArrayInputStream(buf);
            bufObjIn = new CompactObjectInputStream(catalog, bufIn);
        }

        byte[] buf = this.buf;
        short actualChecksum = 0;

        readFully(in, buf, 0, size);

        for (int i = size - 1; i >= 0; i--) {
            actualChecksum += Math.abs(buf[i]);
        }

        if (checksum != actualChecksum) {
            throw new IOException("invalid checksum: " + actualChecksum
                                  + " (expected " + checksum + ')');
        }

        bufIn.position(0);
        bufIn.limit(size);

        return bufObjIn.readObject();
    }

    private static void readFully(InputStream in, byte[] buf, int offset,
                                  int len) throws IOException {
        int n = 0;

        while (n < len) {
            int count = in.read(buf, offset + n, len - n);

            if (count < 0) {
                throw new EOFException();
            }

            n += count;
        }
    }
}