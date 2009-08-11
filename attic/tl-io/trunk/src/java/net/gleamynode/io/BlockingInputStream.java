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


/**
 * <p>
 * An <code>InputStream</code> class that methods block until desired
 * amountof bytes get read.
 * </p>
 * <p>
 * There are two timeout values to prevent the method to block forever.
 * <ul>
 *   <li><code>first timeout</code> - the method will throw
 *       {@link FirstReadTimeoutException} if the first one byte is not read
 *       within <code>firstTimeout</code> milliseconds.</li>
 *   <li><code>timeout</code> - the method will throw
 *       {@link ReadTimeoutException} if the desired amount of bytes are not
 *       read from the stream <code>timeout</code> milliseconds.</li>
 *   <li><code>interval timeout</code> - the method will throw
 *       {@link ReadIntervalTimeoutException} if no bytes are read from
 *       the stream for <code>intervalTimeout</code> milliseconds.</li>
 * </ul>
 * <code>timeout</code> should be greater than <code>intervalTimeout</code>.
 * If not, only {@link ReadTimeoutException} will be thrown.
 *
 * </p>
 * <b>NOTE</b>: The problem the prior version did not read any data until
 *              desired amount of bytes arrive at the stream buffer has
 *              been resolved in this version. No need to increase stream
 *              buffer size anymore.
 * </p>
 *
 * <p>
 * <b>NOTE</b>: {@link #skip(long)} method doesn't block.
 * </p>
 *
 * <p>
 * <b>Thread safety</b>: <b>SAFE</b>.
 * </p>
 *
 * @author Trustin Lee (<a href="http://projects.gleamynode.net/">http://projects.gleamynode.net/</a>)
 *
 * @version $Revision: 1.5 $
 *
 * @see IOTimeoutException
 * @see java.io.InputStream
 */
public class BlockingInputStream extends InputStream {
    private InputStream in;
    private int firstTimeout;
    private int timeout;
    private int intervalTimeout;
    private byte[] oneByte = new byte[1];

    /**
     * Constructs a new <code>BlockingInputStream</code> with the specified
     * <code>InputStream</code>. There will not be any timeout.
     *
     * @param in an <code>InputStream</code> to enable blocked I/O.
     */
    public BlockingInputStream(InputStream in) {
        this.in = in;
        this.timeout = 0;
        this.intervalTimeout = 0;
    }

    /**
     * Constructs a new <code>BlockingInputStream</code> with the specified
     * <code>InputStream</code>. There will be only timeout (no interval
     * timeout).
     *
     * @param in an <code>InputStream</code> to enable blocked I/O.
     * @param timeout a timeout value in milliseconds.
     */
    public BlockingInputStream(InputStream in, int timeout) {
        this.in = in;
        this.timeout = timeout;
        this.intervalTimeout = 0;
    }

    /**
     * Constructs a new <code>BlockingInputStream</code> with the specified
     * <code>InputStream</code>. Both timeout and interval timeout will work.
     *
     * @param in an <code>InputStream</code> to enable blocked I/O.
     * @param timeout a timeout value in milliseconds.
     * @param intervalTimeout a timeout value between incoming bytes in
     *                        milliseconds.
     */
    public BlockingInputStream(InputStream in, int timeout, int intervalTimeout) {
        this.in = in;
        this.timeout = timeout;
        this.intervalTimeout = intervalTimeout;
    }

    /**
     * Returne the number of bytes in the buffer.
     */
    public int available() throws IOException {
        return in.available();
    }

    /**
     * Closes the stream.
     */
    public void close() throws IOException {
        in.close();
    }

    /**
         * Sets the timeout value of first byte in milliseconds.
         */
    public int getFirstTimeout() {
        return firstTimeout;
    }

    /**
     * Returns the timeout value between incoming bytes in milliseconds.
     */
    public int getIntervalTimeout() {
        return intervalTimeout;
    }

    /**
     * Returns the timeout value in milliseconds.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout value of first byte in milliseconds. The first one byte
     * should be read from the stream within the specified
     * <code>firstTimeout</code> milliseconds.
     */
    public void setFirstTimeout(int firstTimeout) {
        this.firstTimeout = firstTimeout;
    }

    /**
     * Sets the timeout value in milliseconds. All desired amount of
     * bytes should be read from the stream within the specified
     * <code>timeout</code> milliseconds.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the timeout value between incoming bytes. After the stream was
     * blocked and some bytes were read, the method will return if no bytes
     * are read for the specified <code>intervalTimeout</code> milliseconds,
     * or all bytes are read fine.
     */
    public void setIntervalTimeout(int intervalTimeout) {
        this.intervalTimeout = intervalTimeout;
    }

    public synchronized void mark(int i) {
        in.mark(i);
    }

    /**
     * Returns <code>true</code> if <code>mark()</code> is supported.
     */
    public boolean markSupported() {
        return in.markSupported();
    }

    /**
     * Reads one byte from the stream.
     *
     * @throws ReadTimeoutException if exceeded timeout.
     * @throws ReadIntervalTimeoutException if exceeded interval timeout.
     */
    public int read() throws IOException {
        byte[] oneByte = this.oneByte;

        if (this.read(oneByte) == -1) {
            return -1;
        }

        return (oneByte[0] >= 0) ? oneByte[0] : (256 + oneByte[0]);
    }

    /**
     * Reads bytes and stores them into the specified byte array.
     * This method will return if the array is full or
     * the method get timeout.
     *
     * @throws ReadTimeoutException if exceeded timeout.
     * @throws ReadIntervalTimeoutException if exceeded interval timeout.
     */
    public int read(byte[] bytes) throws IOException {
        return this.read(bytes, 0, bytes.length);
    }

    /**
     * Reads bytes and stores them into the specified byte array.
     * This method will return if <code>len</code> bytes are received or
     * the method get timeout.
     *
     * @throws ReadTimeoutException if exceeded timeout.
     * @throws ReadIntervalTimeoutException if exceeded interval timeout.
     */
    public int read(byte[] bytes, int off, int len)
             throws IOException {
        InputStream in = this.in;
        int timeout = this.timeout;
        int intervalTimeout = this.intervalTimeout;

        long startTime;
        int readByteCount;
        int totalByteCount = 0;
        long lastReceivedTime;
        long currentTime;

        if (firstTimeout > 0) {
            // firstTimeout feature enabled.
            startTime = System.currentTimeMillis();

            while (available() <= 0) {
                if ((int) (System.currentTimeMillis() - startTime) > firstTimeout) {
                    throw new FirstReadTimeoutException("Couldn't receive "
                                                        + bytes + " bytes in "
                                                        + firstTimeout
                                                        + " milliseconds.");
                }

                Thread.yield();
            }
        }

        if (timeout > 0) {
            // timeout feature enabled, initialize startTime.
            startTime = System.currentTimeMillis();

            if (intervalTimeout > 0) { // both timeout and intervalTimeout

                // intervalTimeout feature enabled. initialize lastReceivedTime.
                lastReceivedTime = Long.MAX_VALUE;

                for (;;) {
                    totalByteCount += (readByteCount =
                        in.read(bytes, off, len));

                    if ((readByteCount == len) || (readByteCount < 0)) {
                        break;
                    }

                    off += readByteCount;
                    len -= readByteCount;

                    currentTime = System.currentTimeMillis();

                    if ((int) (currentTime - startTime) > timeout) {
                        throw new ReadTimeoutException("Couldn't receive "
                                                       + bytes + " bytes in "
                                                       + timeout
                                                       + " milliseconds.");
                    }

                    if (readByteCount > 0) {
                        lastReceivedTime = currentTime;
                    } else {
                        if ((int) (currentTime - lastReceivedTime) > intervalTimeout) {
                            throw new ReadIntervalTimeoutException("Couldn't receive any bytes in "
                                                                   + intervalTimeout
                                                                   + " milliseconds (while waiting for "
                                                                   + bytes
                                                                   + " bytes).");
                        }
                    }

                    Thread.yield();
                }
            } else { // only timeout

                for (;;) {
                    totalByteCount += (readByteCount =
                        in.read(bytes, off, len));

                    if ((readByteCount == len) || (readByteCount < 0)) {
                        break;
                    }

                    off += readByteCount;
                    len -= readByteCount;

                    currentTime = System.currentTimeMillis();

                    if ((int) (currentTime - startTime) > timeout) {
                        throw new ReadTimeoutException("Couldn't receive "
                                                       + bytes + " bytes in "
                                                       + timeout
                                                       + " milliseconds.");
                    }

                    Thread.yield();
                }
            }
        } else if (intervalTimeout > 0) { // only intervalTimeout

            // intervalTimeout feature enabled. initialize lastReceivedTime.
            lastReceivedTime = Long.MAX_VALUE;

            for (;;) {
                totalByteCount += (readByteCount = in.read(bytes, off, len));

                if ((readByteCount == len) || (readByteCount < 0)) {
                    break;
                }

                off += readByteCount;
                len -= readByteCount;

                currentTime = System.currentTimeMillis();

                if (readByteCount > 0) {
                    lastReceivedTime = currentTime;
                } else {
                    if ((int) (currentTime - lastReceivedTime) > intervalTimeout) {
                        throw new ReadIntervalTimeoutException("Couldn't receive any bytes in "
                                                               + intervalTimeout
                                                               + " milliseconds (while waiting for "
                                                               + bytes
                                                               + " bytes).");
                    }
                }

                Thread.yield();
            }
        } else { // no timeout

            for (;;) {
                totalByteCount += (readByteCount = in.read(bytes, off, len));

                if ((readByteCount == len) || (readByteCount < 0)) {
                    break;
                }

                off += readByteCount;
                len -= readByteCount;

                Thread.yield();
            }
        }

        // if zero byte is read and 'in' reaches to EOF, totablByteCount is naturally '-1'.
        return totalByteCount;
    }

    public void reset() throws IOException {
        in.reset();
    }

    /**
     * Drops some bytes from the stream. This method is non-blocking!
     */
    public long skip(long bytes) throws IOException {
        return in.skip(bytes);
    }
}

//
//  CHANGELOG
// ===========
// $Log: BlockingInputStream.java,v $
// Revision 1.5  2003/04/11 15:48:35  anoripi
// Utilized CVS keyword substitution in comments.
// Replaced the license statement to concise one.
// Revised comments.
// Added firstTimeout feature. Read JavaDoc.
// Became thread-safe, though the performance of int read() might get down.
//
//
