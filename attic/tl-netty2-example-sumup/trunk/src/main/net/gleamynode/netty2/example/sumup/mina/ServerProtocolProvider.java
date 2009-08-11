/*
 * @(#) $Id: ServerProtocolProvider.java 12 2005-04-18 03:46:00Z trustin $
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
 * @version $Rev: 12 $, $Date: 2005-04-18 12:46:00 +0900 $,
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