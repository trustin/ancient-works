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

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;


/**
 * Provides general read/write methods which are absent in
 * <code>java.nio.ByteBuffer</code>.
 *
 * @version $Revision: 1.4 $, $Date: 2004/02/11 08:31:24 $
 * @author Trustin Lee
 *
 * @see Bytes
 */
public class ByteBuffers {
    private ByteBuffers() {
    }

    public static String getHexdump(ByteBuffer in) {
        StringBuffer out = new StringBuffer((in.limit() - in.position()) << 2);
        writeHexdump(in, out);
        return out.toString();
    }

    public static void writeHexdump(ByteBuffer in, StringBuffer out) {
        int size = in.limit() - in.position();

        for (; size > 0; size--) {
            int byteValue = in.get() & 0xFF;
            out.append((char) Bytes.highDigits[byteValue]);
            out.append((char) Bytes.lowDigits[byteValue]);
        }
    }

    public static void read(ByteBuffer in, CharBuffer out, int fieldSize,
                            CharsetDecoder decoder) {
        checkFieldSize(fieldSize);
        
        if (fieldSize == 0)
            return;

        boolean utf16 = decoder.charset().name().startsWith("UTF-16");

        if (utf16 && ((fieldSize & 1) != 0)) {
            throw new IllegalArgumentException("fieldSize is not even.");
        }

        int i;
        int oldLimit = in.limit();
        int limit = in.position() + fieldSize;

        if (oldLimit < limit) {
            throw new BufferOverflowException();
        }

        in.mark();

        if (!utf16) {
            for (i = 0; i < fieldSize; i++) {
                if (in.get() == 0) {
                    break;
                }
            }

            if (i == fieldSize) {
                in.limit(limit);
            } else {
                in.limit(in.position() - 1);
            }
        } else {
            for (i = 0; i < fieldSize; i += 2) {
                if ((in.get() == 0) && (in.get() == 0)) {
                    break;
                }
            }

            if (i == fieldSize) {
                in.limit(limit);
            } else {
                in.limit(in.position() - 2);
            }
        }

        in.reset();
        decoder.decode(in, out, true);
        in.limit(oldLimit);
        in.position(limit);
    }

    public static String read(ByteBuffer in, int fieldSize,
                              CharsetDecoder decoder) {
        CharBuffer out =
            CharBuffer.allocate((int) (decoder.maxCharsPerByte() * fieldSize)
                                + 1);
        read(in, out, fieldSize, decoder);
        return out.flip().toString();
    }

    public static void write(CharBuffer in, ByteBuffer out, int fieldSize,
                             CharsetEncoder encoder) {
        checkFieldSize(fieldSize);
        
        if (fieldSize == 0)
            return;

        boolean utf16 = encoder.charset().name().startsWith("UTF-16");

        if (utf16 && ((fieldSize & 1) != 0)) {
            throw new IllegalArgumentException("fieldSize is not even.");
        }
        
        int oldLimit = out.limit();
        int limit = out.position() + fieldSize;

        if (oldLimit < limit) {
            throw new BufferOverflowException();
        }

        out.limit(limit);
        encoder.encode(in, out, true);
        out.limit(oldLimit);

        if (limit > out.position()) {
            if (!utf16) {
                out.put(Bytes.NUL);
            } else {
                out.put(Bytes.NUL);
                out.put(Bytes.NUL);
            }
        }

        out.position(limit);
    }

    public static void write(CharSequence in, ByteBuffer out, int fieldSize,
                             CharsetEncoder encoder) {
        write(CharBuffer.wrap(in), out, fieldSize, encoder);
    }

    public static int readDecimal(ByteBuffer in, int fieldSize) {
        checkFieldSize(fieldSize);
        
        if (fieldSize == 0)
            return 0;

        int digit;
        int ret;
        boolean negative;

        switch (ret = in.get()) {
        case '-':
            negative = true;
            ret = 0;
            break;
        case '+':
            negative = false;
            ret = 0;
            break;
        default:
            negative = false;
            digit = Bytes.digit2int[ret + 128];

            if (digit < 0) {
                in.position((in.position() + fieldSize) - 1);
                return 0;
            }

            ret = -digit;
        }

        int i;

        for (i = fieldSize - 1; i > 0; i--) {
            digit = Bytes.digit2int[in.get() + 128];

            if (digit < 0) {
                in.position((in.position() + i) - 1);
                break;
            }

            ret = (ret << 3) + (ret << 1);
            ret -= digit;
        }

        return (negative) ? ret : (-ret);
    }

    public static long readDecimalLong(ByteBuffer in, int fieldSize) {
        checkFieldSize(fieldSize);
        
        if (fieldSize == 0)
            return 0L;

        int digit;
        int tmp;
        long ret;
        boolean negative;

        switch (tmp = in.get()) {
        case '-':
            negative = true;
            ret = 0L;
            break;
        case '+':
            negative = false;
            ret = 0L;
            break;
        default:
            negative = false;
            digit = Bytes.digit2int[tmp + 128];

            if (digit < 0) {
                in.position((in.position() + fieldSize) - 1);
                return 0;
            }

            ret = -digit;
        }

        int i;

        for (i = fieldSize - 1; i > 0; i--) {
            digit = Bytes.digit2int[in.get() + 128];

            if (digit < 0) {
                in.position((in.position() + i) - 1);
                break;
            }

            ret = (ret << 3) + (ret << 1);
            ret -= digit;
        }

        return (negative) ? ret : (-ret);
    }

    public static void writeDecimal(int value, ByteBuffer out, int fieldSize) {
        int q;
        int r;
        int i = Bytes.DEC_BUFFER_SIZE;
        int actualLen;
        int padding;
        boolean negative = value < 0;
        byte[] buffer = new byte[Bytes.DEC_BUFFER_SIZE];

        if (negative) {
            if (value == Integer.MIN_VALUE) {
                if (Bytes.DEC_SIGNED_INT_LEN > fieldSize) {
                    throw new BufferOverflowException();
                }

                out.put(Bytes.minInt);
                padding = fieldSize - Bytes.DEC_SIGNED_INT_LEN;

                if (padding > 0) {
                    out.put(Bytes.NUL);
                    out.position((out.position() + padding) - 1);
                }

                return;
            }

            value = -value;
        }

        while (value >= 65536) {
            q = value / 100;
            r = value - ((q << 6) + (q << 5) + (q << 2));
            value = q;
            buffer[--i] = Bytes.digitOnes[r];
            buffer[--i] = Bytes.digitTens[r];
        }

        for (;;) {
            q = (value * 52429) >>> (16 + 3);
            r = value - ((q << 3) + (q << 1));
            buffer[--i] = Bytes.digits[r];
            value = q;

            if (value == 0) {
                break;
            }
        }

        // append minus sign
        if (negative) {
            out.put((byte) '-');
            fieldSize--;
        }

        // copy digits
        actualLen = Bytes.DEC_BUFFER_SIZE - i;
        padding = fieldSize - actualLen;

        if (padding < 0) {
            throw new BufferOverflowException();
        }

        out.put(buffer, i, actualLen);

        if (padding > 0) {
            out.put(Bytes.NUL);
            out.position((out.position() + padding) - 1);
        }
    }

    public static void writeDecimal(long value, ByteBuffer out, int fieldSize) {
        int i = Bytes.DEC_BUFFER_SIZE - 1;
        int actualLen;
        int padding;
        boolean negative = value < 0;
        byte[] buffer = new byte[Bytes.DEC_BUFFER_SIZE];

        if (!negative) {
            value = -value;
        }

        while (value <= -10) {
            buffer[i--] = Bytes.digits[(int) (-(value % 10))];
            value /= 10;
        }

        // write the first digit
        buffer[i] = Bytes.digits[(int) (-value)];

        // append minus sign
        if (negative) {
            out.put((byte) '-');
            fieldSize--;
        }

        // copy digits
        actualLen = Bytes.DEC_BUFFER_SIZE - i;
        padding = fieldSize - actualLen;

        if (padding < 0) {
            throw new BufferOverflowException();
        }

        out.put(buffer, i, actualLen);

        if (padding > 0) {
            out.put(Bytes.NUL);
            out.position((out.position() + padding) - 1);
        }
    }

    public static void writeAlignedDecimal(int value, ByteBuffer out,
                                           int fieldSize) {
        if (value < 0) {
            throw new IllegalArgumentException("aligned decimal cannot be negative");
        }

        int q;
        int r;
        int i = Bytes.DEC_BUFFER_SIZE;
        int actualLen;
        int padding;
        byte[] buffer = new byte[Bytes.DEC_BUFFER_SIZE];

        while (value >= 65536) {
            q = value / 100;
            r = value - ((q << 6) + (q << 5) + (q << 2));
            value = q;
            buffer[--i] = Bytes.digitOnes[r];
            buffer[--i] = Bytes.digitTens[r];
        }

        for (;;) {
            q = (value * 52429) >>> (16 + 3);
            r = value - ((q << 3) + (q << 1));
            buffer[--i] = Bytes.digits[r];
            value = q;

            if (value == 0) {
                break;
            }
        }

        // copy digits
        actualLen = Bytes.DEC_BUFFER_SIZE - i;
        padding = fieldSize - actualLen;

        if (padding < 0) {
            throw new BufferOverflowException();
        }

        for (; padding > 0; padding--)
            out.put((byte) '0');

        out.put(buffer, i, actualLen);
    }

    public static void writeAlignedDecimal(long value, ByteBuffer out,
                                           int fieldSize) {
        if (value < 0L) {
            throw new IllegalArgumentException("aligned decimal cannot be negative");
        }

        int i = Bytes.DEC_BUFFER_SIZE - 1;
        int actualLen;
        int padding;
        byte[] buffer = new byte[Bytes.DEC_BUFFER_SIZE];

        while (value >= 10) {
            buffer[i--] = Bytes.digits[(int) (value % 10)];
            value /= 10;
        }

        // write the first digit
        buffer[i] = Bytes.digits[(int) value];

        // copy digits
        actualLen = Bytes.DEC_BUFFER_SIZE - i;
        padding = fieldSize - actualLen;

        if (padding < 0) {
            throw new BufferOverflowException();
        }

        for (; padding > 0; padding--)
            out.put((byte) '0');

        out.put(buffer, i, actualLen);
    }

    //	public static int readHexdecimalInt(ByteBuffer in, int fieldSize) {
    //		int digit;
    //		int ret = 0;
    //
    //		for ( ; fieldSize > 0; fieldSize -- ) {
    //			digit = Bytes.hexDigit2int[in.get() + 128];
    //			if (digit < 0) {
    //				in.position(in.position() + fieldSize);
    //				break;
    //			}
    //			ret <<= 4;
    //			ret |= digit;
    //		}
    //
    //		return ret;
    //	}
    //	
    //	public static long readHexadecimalLong(ByteBuffer in, int fieldSize) {
    //		int digit;
    //		long ret = 0;
    //
    //		for ( ; fieldSize > 0; fieldSize -- ) {
    //			digit = Bytes.hexDigit2int[in.get() + 128];
    //			if (digit < 0) {
    //				in.position(in.position() + fieldSize);
    //				break;
    //			}
    //			ret <<= 4;
    //			ret |= digit;
    //		}
    //
    //		return ret;
    //	}

    /**
     * Fills the specified {@link ByteBuffer} with the specified value.
     * This method moves buffer position forward.
     */
    public static void fill(ByteBuffer buf, byte value, int size) {
        int q = size >>> 3;
        int r = size & 7;

        if (q > 0) {
            int intValue =
                value | (value << 8) | (value << 16) | (value << 24);
            long longValue = intValue;
            longValue <<= 32;
            longValue |= intValue;

            for (int i = q; i > 0; i--) {
                buf.putLong(longValue);
            }
        }

        q = r >>> 2;
        r = r & 3;

        if (q > 0) {
            int intValue =
                value | (value << 8) | (value << 16) | (value << 24);
            buf.putInt(intValue);
        }

        q = r >> 1;
        r = r & 1;

        if (q > 0) {
            short shortValue = (short) (value | (value << 8));
            buf.putShort(shortValue);
        }

        if (r > 0) {
            buf.put(value);
        }
    }

    /**
     * Fills the specified {@link ByteBuffer} with the specified value.
     * This method does not change buffer position.
     */
    public static void fillAndReset(ByteBuffer buf, byte value, int size) {
        int pos = buf.position();
        fill(buf, value, size);
        buf.position(pos);
    }

    /**
     * Fills the specified {@link ByteBuffer} with {@link Bytes#NUL}.
     * This method moves buffer position forward.
     */
    public static void fill(ByteBuffer buf, int size) {
        int q = size >>> 3;
        int r = size & 7;

        for (int i = q; i > 0; i--) {
            buf.putLong(0L);
        }

        q = r >>> 2;
        r = r & 3;

        if (q > 0) {
            buf.putInt(0);
        }

        q = r >> 1;
        r = r & 1;

        if (q > 0) {
            buf.putShort((short) 0);
        }

        if (r > 0) {
            buf.put((byte) 0);
        }
    }

    /**
     * Fills the specified {@link ByteBuffer} with {@link Bytes#NUL}.
     * This method does not change buffer position.
     */
    public static void fillAndReset(ByteBuffer buf, int size) {
        int pos = buf.position();
        fill(buf, size);
        buf.position(pos);
    }

    private static void checkFieldSize(int fieldSize) {
        if (fieldSize < 0) {
            throw new IllegalArgumentException("fieldSize cannot be negative: "
                                               + fieldSize);
        }
    }
}
