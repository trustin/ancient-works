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

import java.io.UnsupportedEncodingException;

import java.util.Arrays;


/**
 * Provides popular byte constants and general read/write methods
 * with various encodings(big-endian, little-endian, ASCII decimal.)
 *
 * @author Trustin Lee (<a href="http://projects.gleamynode.net/">http://projects.gleamynode.net/</a>)
 *
 * @version $Revision: 1.10 $, $Date: 2004/02/11 08:31:47 $
 */
public final class Bytes {
    /** Null */
    public static final byte NUL = 0;

    /** Start of heading */
    public static final byte SOH = 1;

    /** Start of text */
    public static final byte STX = 2;

    /** End of text */
    public static final byte ETX = 3;

    /** End of transmission */
    public static final byte EOT = 4;

    /** Enquiry */
    public static final byte ENQ = 5;

    /** Acknowledge */
    public static final byte ACK = 6;

    /** Bell */
    public static final byte BEL = 7;

    /** Backspace */
    public static final byte BS = 8;

    /** Horizontal tabulation */
    public static final byte HT = 9;

    /** Linefeed */
    public static final byte LF = 10;

    /** Vertical tabulation */
    public static final byte VT = 11;

    /** Form feed */
    public static final byte FF = 12;

    /** Carrage return */
    public static final byte CR = 13;

    /** Shift out */
    public static final byte SO = 14;

    /** Shift in */
    public static final byte SI = 15;

    /** Data link escape */
    public static final byte DLE = 16;

    /** Device control 1 */
    public static final byte DC1 = 17;

    /** Device control 2 */
    public static final byte DC2 = 18;

    /** Device control 3 */
    public static final byte DC3 = 19;

    /** Device control 4 */
    public static final byte DC4 = 20;

    /** Negative acknowledge */
    public static final byte NAK = 21;

    /** Synchronous idle */
    public static final byte SYN = 22;

    /** End of transmission block */
    public static final byte ETB = 23;

    /** Cancel */
    public static final byte CAN = 24;

    /** End of medium */
    public static final byte EM = 25;

    /** Substitute */
    public static final byte SUB = 26;

    /** Escape */
    public static final byte ESC = 27;

    /** File separator */
    public static final byte FS = 28;

    /** Group separator */
    public static final byte GS = 29;

    /** Record separator */
    public static final byte RS = 30;

    /** Unit separator */
    public static final byte US = 31;

    /** Space */
    public static final byte SPC = 32;

    /** Delete */
    public static final byte DEL = 127;
    static final int DEC_SIGNED_INT_LEN = 11;
    static final int DEC_BUFFER_SIZE = 21;
    static final byte[] highDigits;
    static final byte[] lowDigits;

    // initialize lookup tables
    static {
        final byte[] digits =
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        int i;
        byte[] high = new byte[256];
        byte[] low = new byte[256];

        for (i = 0; i < 256; i++) {
            high[i] = digits[i >>> 4];
            low[i] = digits[i & 0x0F];
        }

        highDigits = high;
        lowDigits = low;
    }

    // lookup tables
    static final int[] digit2int;

    static {
        int i;
        int[] tmp = new int[256];

        for (i = 0; i < 256; i++) {
            tmp[i] = -1;
        }

        for (i = '0'; i <= '9'; i++) {
            tmp[i + 128] = i - '0';
        }

        digit2int = tmp;
    }

    static final byte[] digits =
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    static final byte[] digitTens =
        { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', };
    static final byte[] digitOnes =
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', };
    static final byte[] minInt =
        { '-', '2', '1', '4', '7', '4', '8', '3', '6', '4', '8' };

    private Bytes() {
    }

    ////////////////////////////
    // Hexdump class methods //
    ////////////////////////////

    /**
     * Returns hexdump string of the specified byte array.
     */
    public static String getHexdump(byte[] data) {
        return getHexdump(data, 0, data.length);
    }

    /**
     * Returns hexdump string of the specified byte array.
     */
    public static String getHexdump(byte[] data, int offset, int len) {
        StringBuffer buf = new StringBuffer(len << 2);
        writeHexdump(data, offset, len, buf);
        return buf.toString();
    }

    /**
     * Appends Hexdump string of the specified byte array to the specified
     * {@link StringBuffer}.
     */
    public static void writeHexdump(byte[] data, StringBuffer out) {
        writeHexdump(data, 0, data.length, out);
    }

    /**
     * Appends Hexdump string of the specified byte array to the specified
     * {@link StringBuffer}.
     */
    public static void writeHexdump(byte[] data, int offset, int len,
                                    StringBuffer out) {
        int i;
        int byteValue;
        final int end = offset + len;

        for (i = offset; i < end; i++) {
            byteValue = data[i] & 0xFF;
            out.append((char) highDigits[byteValue]);
            out.append((char) lowDigits[byteValue]);
        }
    }

    //////////////////////////
    // String class methods //
    //////////////////////////
    public static String readString(byte[] data, int offset, int maxLen) {
        final int end = offset + maxLen;
        int i;

        for (i = offset; (i < end) && (data[i] != NUL); i++)
            continue;

        return new String(data, offset, i - offset);
    }

    public static void writeString(String value, byte[] data, int offset,
                                   int maxLen) {
        byte[] src = value.getBytes();
        int len = src.length;

        if (len >= maxLen) {
            System.arraycopy(src, 0, data, offset, maxLen);
        } else {
            System.arraycopy(src, 0, data, offset, len);
            data[offset + len] = NUL;
        }
    }

    public static String readString(byte[] data, int offset, int maxLen,
                                    String enc)
                             throws UnsupportedEncodingException {
        final int end = offset + maxLen;
        int i;

        for (i = offset; (i < end) && (data[i] != NUL); i++)
            continue;

        return new String(data, offset, i - offset, enc);
    }

    public static void writeString(String value, byte[] data, int offset,
                                   int maxLen, String enc)
                            throws UnsupportedEncodingException {
        byte[] src = value.getBytes(enc);
        int len = src.length;

        if (len >= maxLen) {
            System.arraycopy(src, 0, data, offset, maxLen);
        } else {
            System.arraycopy(src, 0, data, offset, len);
            data[offset + len] = NUL;
        }
    }

    //////////////////////////////
    // Big-endian class methods //
    //////////////////////////////
    public static int readBigEndian(byte[] data, int offset, int len) {
        int ret = 0;
        int end = offset + len;

        for (int i = offset; i < end; i++) {
            ret <<= 8;
            ret |= (data[i] & 0xff);
        }

        return ret;
    }

    public static long readBigEndianLong(byte[] data, int offset, int len) {
        long ret = 0;
        int end = offset + len;

        for (int i = offset; i < end; i++) {
            ret <<= 8;
            ret |= (data[i] & 0xff);
        }

        return ret;
    }

    public static void writeBigEndian(int value, byte[] data, int offset,
                                      int len) {
        final int end = offset;
        final int start = end + len;

        for (int i = start - 1; i >= end; i--) {
            data[i] = (byte) (value & 0xff);
            value >>>= 8;
        }
    }

    public static void writeBigEndian(long value, byte[] data, int offset,
                                      int len) {
        final int end = offset;
        final int start = len + end;

        for (int i = start - 1; i >= end; i--) {
            data[i] = (byte) (value & 0xff);
            value >>>= 8;
        }
    }

    /////////////////////////////////
    // Little-endian class methods //
    /////////////////////////////////
    public static int readLittleEndian(byte[] data, int offset, int len) {
        int ret = 0;
        final int off = offset;
        final int end = off + len;

        for (int i = end - 1; i >= off; i--) {
            ret <<= 8;
            ret |= (data[i] & 0xff);
        }

        return ret;
    }

    public static long readLittleEndianLong(byte[] data, int offset, int len) {
        long ret = 0;
        final int off = offset;
        final int end = off + len;

        for (int i = end - 1; i >= off; i--) {
            ret <<= 8;
            ret |= (data[i] & 0xff);
        }

        return ret;
    }

    public static void writeLittleEndian(int value, byte[] data, int offset,
                                         int len) {
        final int end = offset + len;

        for (int i = offset; i < end; i++) {
            data[i] = (byte) (value & 0xff);
            value >>>= 8;
        }
    }

    public static void writeLittleEndian(long value, byte[] data, int offset,
                                         int len) {
        final int end = offset + len;

        for (int i = offset; i < end; i++) {
            data[i] = (byte) (value & 0xff);
            value >>>= 8;
        }
    }

    ////////////////////////////////
    // Decimal text class methods //
    ////////////////////////////////
    public static int readDecimal(byte[] data, int offset, int len) {
        int digit;
        int ret = 0;
        int off = offset;
        final int end = off + len;
        boolean negative;

        switch (data[off]) {
        case '-':
            negative = true;
            off++;
            break;
        case '+':
            negative = false;
            off++;
        default:
            negative = false;
        }

        for (int i = off; i < end; i++) {
            digit = digit2int[data[i] + 128];

            if (digit < 0) {
                break;
            }

            ret = (ret << 3) + (ret << 1);
            ret -= digit;
        }

        return (negative) ? ret : (-ret);
    }

    public static long readDecimalLong(byte[] data, int offset, int len) {
        int digit;
        long ret = 0;
        int off = offset;
        final int end = off + len;
        boolean negative;

        switch (data[off]) {
        case '-':
            negative = true;
            off++;
            break;
        case '+':
            negative = false;
            off++;
        default:
            negative = false;
        }

        for (int i = off; i < end; i++) {
            digit = digit2int[data[i] + 128];

            if (digit < 0) {
                break;
            }

            ret = (ret << 3) + (ret << 1);
            ret -= digit;
        }

        return (negative) ? ret : (-ret);
    }

    public static void writeDecimal(int value, byte[] data, int offset, int len) {
        int q;
        int r;
        int i = DEC_BUFFER_SIZE;
        int actualLen;
        int padding;
        boolean negative = value < 0;
        byte[] buffer = new byte[DEC_BUFFER_SIZE];

        if (negative) {
            if (value == Integer.MIN_VALUE) {
                System.arraycopy(minInt, DEC_SIGNED_INT_LEN - len, data,
                                 offset, len);
                return;
            }

            value = -value;
        }

        while (value >= 65536) {
            q = value / 100;
            r = value - ((q << 6) + (q << 5) + (q << 2));
            value = q;
            buffer[--i] = digitOnes[r];
            buffer[--i] = digitTens[r];
        }

        for (;;) {
            q = (value * 52429) >>> (16 + 3);
            r = value - ((q << 3) + (q << 1));
            buffer[--i] = digits[r];
            value = q;

            if (value == 0) {
                break;
            }
        }

        // append minus sign
        if (negative) {
            data[offset++] = (byte) '-';
            len--;
        }

        // append zero padding
        actualLen = DEC_BUFFER_SIZE - i;
        padding = len - actualLen;

        if (padding < 0) {
            // truncate digits
            actualLen = len;
        }

        // copy digits
        System.arraycopy(buffer, i, data, offset, actualLen);

        if (padding > 0) {
            System.out.println("appended NUL");
            data[offset + actualLen] = Bytes.NUL;
        }
    }

    public static void writeDecimal(long value, byte[] data, int offset,
                                    int len) {
        int i = DEC_BUFFER_SIZE - 1;
        int actualLen;
        int padding;
        boolean negative = value < 0;
        byte[] buffer = new byte[DEC_BUFFER_SIZE];

        if (!negative) {
            value = -value;
        }

        while (value <= -10) {
            buffer[i--] = digits[(int) (-(value % 10))];
            value /= 10;
        }

        // write the first digit
        buffer[i] = digits[(int) (-value)];

        // append minus sign
        if (negative) {
            data[offset++] = (byte) '-';
            len--;
        }

        actualLen = DEC_BUFFER_SIZE - i;
        padding = len - actualLen;

        if (padding < 0) {
            // truncate digits
            actualLen = len;
        }

        // copy digits
        System.arraycopy(buffer, i, data, offset, actualLen);

        if (padding > 0) {
            System.out.println("appended NUL");
            data[offset + actualLen] = Bytes.NUL;
        }
    }

    public static void writeAlignedDecimal(int value, byte[] data, int offset,
                                           int len) {
        if (value < 0) {
            throw new IllegalArgumentException("aligned decimal cannot be negative");
        }

        int q;
        int r;
        int i = DEC_BUFFER_SIZE;
        int actualLen;
        int padding;
        byte[] buffer = new byte[DEC_BUFFER_SIZE];

        while (value >= 65536) {
            q = value / 100;
            r = value - ((q << 6) + (q << 5) + (q << 2));
            value = q;
            buffer[--i] = digitOnes[r];
            buffer[--i] = digitTens[r];
        }

        for (;;) {
            q = (value * 52429) >>> (16 + 3);
            r = value - ((q << 3) + (q << 1));
            buffer[--i] = digits[r];
            value = q;

            if (value == 0) {
                break;
            }
        }

        // append zero padding
        actualLen = DEC_BUFFER_SIZE - i;
        padding = len - actualLen;

        if (padding > 0) {
            padding += offset;
            Arrays.fill(data, offset, padding, (byte) '0');
            offset = padding;
        } else {
            // truncate digits
            actualLen = len;
        }

        // copy digits
        System.arraycopy(buffer, i, data, offset, actualLen);
    }

    public static void writeAlignedDecimal(long value, byte[] data,
                                           int offset, int len) {
        if (value < 0L) {
            throw new IllegalArgumentException("aligned decimal cannot be negative");
        }

        int i = DEC_BUFFER_SIZE - 1;
        int actualLen;
        int padding;
        byte[] buffer = new byte[DEC_BUFFER_SIZE];

        value = -value;

        while (value <= -10) {
            buffer[i--] = digits[(int) (-(value % 10))];
            value /= 10;
        }

        // write the first digit
        buffer[i] = digits[(int) (-value)];

        actualLen = DEC_BUFFER_SIZE - i;

        padding = len - actualLen;

        if (padding > 0) {
            padding += offset;
            Arrays.fill(data, offset, padding, (byte) '0');
            offset = padding;
        } else {
            // truncate digits
            actualLen = len;
        }

        System.arraycopy(buffer, i, data, offset, actualLen);
    }
}
