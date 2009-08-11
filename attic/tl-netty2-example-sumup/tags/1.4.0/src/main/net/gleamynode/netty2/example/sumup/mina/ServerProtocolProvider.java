/*
 * @(#) $Id: ServerProtocolProvider.java 17 2005-04-19 15:29:11Z trustin $
 */
package net.gleamynode.netty2.example.sumup.mina;

import net.gleamynode.netty2.example.sumup.SumUpMessageRecognizer;
import net.gleamynode.netty2.mina.NettyCodecFactory;

import org.apache.mina.protocol.ProtocolCodecFactory;
import org.apache.mina.protocol.ProtocolHandler;
import org.apache.mina.protocol.ProtocolProvider;

/**
 * {@link ProtocolProvider} for SumUp server.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 17 $, $Date: 2005-04-20 00:29:11 +0900 $,
 */
public class ServerProtocolProvider implements ProtocolProvider {

    private static final ProtocolCodecFactory CODEC_FACTORY =
        new NettyCodecFactory(
                new SumUpMessageRecognizer(SumUpMessageRecognizer.SERVER_MODE));

    private static final ProtocolHandler HANDLER = new ServerSessionHandler();

    public ServerProtocolProvider() {
    }

    public ProtocolCodecFactory getCodecFactory() {
        return CODEC_FACTORY;
    }

    public ProtocolHandler getHandler() {
        return HANDLER;
    }
}