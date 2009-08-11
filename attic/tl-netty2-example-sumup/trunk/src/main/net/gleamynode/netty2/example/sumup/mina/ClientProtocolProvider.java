/*
 * @(#) $Id: ClientProtocolProvider.java 12 2005-04-18 03:46:00Z trustin $
 */
package net.gleamynode.netty2.example.sumup.mina;

import net.gleamynode.netty2.example.sumup.SumUpMessageRecognizer;
import net.gleamynode.netty2.mina.NettyCodecFactory;

import org.apache.mina.protocol.ProtocolCodecFactory;
import org.apache.mina.protocol.ProtocolHandler;
import org.apache.mina.protocol.ProtocolProvider;

/**
 * {@link ProtocolProvider} for SumUp client.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 12 $, $Date: 2005-04-18 12:46:00 +0900 $,
 */
public class ClientProtocolProvider implements ProtocolProvider {

    private static final ProtocolCodecFactory CODEC_FACTORY =
        new NettyCodecFactory(
                new SumUpMessageRecognizer(SumUpMessageRecognizer.CLIENT_MODE));

    private final ProtocolHandler handler;

    public ClientProtocolProvider(int[] values) {
        handler = new ClientSessionHandler(values);
    }

    public ProtocolCodecFactory getCodecFactory() {
        return CODEC_FACTORY;
    }

    public ProtocolHandler getHandler() {
        return handler;
    }
}