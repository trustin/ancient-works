/*
 * @(#) $Id: NettyEncoder.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2.mina;

import net.gleamynode.netty2.Message;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolEncoder;
import org.apache.mina.protocol.ProtocolEncoderOutput;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.ProtocolViolationException;

/**
 * A MINA <tt>ProtocolEncoder</tt> that encodes Netty2 {@link Message}s
 * into byte buffers. 
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $,
 */
public class NettyEncoder implements ProtocolEncoder
{
    /**
     * Creates a new instance.
     */
    public NettyEncoder()
    {
    }

    public void encode( ProtocolSession session, Object message,
                       ProtocolEncoderOutput out )
            throws ProtocolViolationException
    {
        if( ! ( message instanceof Message ) )
        {
            throw new ProtocolViolationException(
                    "This encoder can decode only Netty Messages." );
        }

        for( ;; )
        {
            ByteBuffer buf = ByteBuffer.allocate( 8192 );
            Message m = ( Message ) message;
            try
            {
                if( m.write( buf.buf() ) )
                {
                    break;
                }
            }
            finally
            {
                buf.flip();
                if( buf.hasRemaining() )
                {
                    out.write( buf );
                }
                else
                {
                    buf.release();
                }
            }
        }
    }
}