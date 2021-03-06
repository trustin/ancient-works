/*
 * @(#) $Id: NettyDecoder.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2.mina;

import net.gleamynode.netty2.Message;
import net.gleamynode.netty2.MessageParseException;
import net.gleamynode.netty2.MessageRecognizer;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolDecoder;
import org.apache.mina.protocol.ProtocolDecoderOutput;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.ProtocolViolationException;

/**
 * A MINA <tt>ProtocolDecoder</tt> that decodes byte buffers into
 * Netty2 {@link Message}s using specified {@link MessageRecognizer}s. 
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $,
 */
public class NettyDecoder implements ProtocolDecoder
{
    private final MessageRecognizer recognizer;

    private java.nio.ByteBuffer readBuf = java.nio.ByteBuffer.allocate( 1024 );

    private Message readingMessage;

    /**
     * Creates a new instance with the specified {@link MessageRecognizer}.
     */
    public NettyDecoder( MessageRecognizer recognizer )
    {
        if( recognizer == null )
            throw new NullPointerException();

        this.recognizer = recognizer;
    }

    public void decode( ProtocolSession session, ByteBuffer in,
                       ProtocolDecoderOutput out )
            throws ProtocolViolationException
    {

        put( in );

        Message m = readingMessage;
        try
        {
            for( ;; )
            {
                readBuf.flip();
                if( m == null )
                {
                    int limit = readBuf.limit();
                    boolean failed = true;
                    try
                    {
                        m = recognizer.recognize( readBuf );
                        failed = false;
                    }
                    finally
                    {
                        if( failed )
                        {
                            // clear the read buffer if failed to recognize
                            readBuf.clear();
                            break;
                        }
                        else
                        {
                            if( m == null )
                            {
                                readBuf.limit( readBuf.capacity() );
                                readBuf.position( limit );
                                break; // finish decoding
                            }
                            else
                            {
                                // reset buffer for read
                                readBuf.limit( limit );
                                readBuf.position( 0 );
                            }
                        }
                    }
                }

                if( m != null )
                {
                    try
                    {
                        if( m.read( readBuf ) )
                        {
                            out.write( m );
                            m = null;
                        }
                    }
                    finally
                    {
                        if( readBuf.hasRemaining() )
                        {
                            readBuf.compact();
                        }
                        else
                        {
                            readBuf.clear();
			    break;
                        }
                    }
                }
            }
        }
        catch( MessageParseException e )
        {
            m = null; // discard reading message
            throw new ProtocolViolationException( "Failed to decode.", e );
        }
        finally
        {
            readingMessage = m;
        }
    }

    private void put( ByteBuffer in )
    {
        // copy to read buffer
        if( in.remaining() > readBuf.remaining() )
            expand( ( readBuf.position() + in.remaining() ) * 3 / 2 );
        readBuf.put( in.buf() );
    }

    private void expand( int newCapacity )
    {
        java.nio.ByteBuffer newBuf = java.nio.ByteBuffer
                .allocate( newCapacity );
        readBuf.flip();
        newBuf.put( readBuf );
        readBuf = newBuf;
    }
}
