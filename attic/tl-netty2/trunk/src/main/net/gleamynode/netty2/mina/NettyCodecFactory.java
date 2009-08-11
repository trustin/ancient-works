/*
 * @(#) $Id: NettyCodecFactory.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2.mina;

import net.gleamynode.netty2.Message;
import net.gleamynode.netty2.MessageRecognizer;

import org.apache.mina.protocol.ProtocolCodecFactory;
import org.apache.mina.protocol.ProtocolDecoder;
import org.apache.mina.protocol.ProtocolEncoder;

/**
 * A MINA <tt>ProtocolCodecFactory</tt> that provides encoder and decoder
 * for Netty2 {@link Message}s and {@link MessageRecognizer}s.
 * <p>
 * Please note that this codec factory assumes one {@link MessageRecognizer}
 * can be used for multiple sessions.  If not, you'll have to create your
 * own factory after this factory.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $,
 */
public class NettyCodecFactory implements ProtocolCodecFactory {

    private static final NettyEncoder ENCODER = new NettyEncoder();

    private final MessageRecognizer recognizer;
    
    public NettyCodecFactory(MessageRecognizer recognizer) {
        this.recognizer = recognizer;
    }

    public ProtocolEncoder newEncoder() {
        return ENCODER;
    }

    public ProtocolDecoder newDecoder() {
        return new NettyDecoder(recognizer);
    }
}
