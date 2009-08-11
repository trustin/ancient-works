/*
 * @(#) $Id: SumUpMessageRecognizer.java 11 2005-04-18 03:42:45Z trustin $
 */
package net.gleamynode.netty2.example.sumup;

import java.nio.ByteBuffer;

import net.gleamynode.netty2.Message;
import net.gleamynode.netty2.MessageParseException;
import net.gleamynode.netty2.MessageRecognizer;

/**
 * Recognizes SumUp protocol messages. Works both in server mode and client
 * mode. Sesver will receive only <code>ADD</code> message, and client will
 * receive only <code>RESULT</code> message.
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 11 $, $Date: 2005-04-18 12:42:45 +0900 $
 */
public class SumUpMessageRecognizer implements MessageRecognizer {

	public static final int CLIENT_MODE = 1;

	public static final int SERVER_MODE = 2;

	private int mode;

	public SumUpMessageRecognizer(int mode) {
		switch (mode) {
		case CLIENT_MODE:
		case SERVER_MODE:
			this.mode = mode;
			break;
		default:
			throw new IllegalArgumentException("invalid mode: " + mode);
		}
	}

	public Message recognize(ByteBuffer buf) throws MessageParseException {
		// return null if message type is not arrived yet.
		if (buf.remaining() < Constants.TYPE_LEN)
			return null;
		
		int type = buf.getShort();
		switch (mode) {
		// server can receive ADD message only.
		case SERVER_MODE:
			switch (type) {
			case Constants.ADD:
				return new AddMessage();
			default:
				throw new MessageParseException("unknown type: " + type);
			}
		// client can receive RESULT message only.
		case CLIENT_MODE:
			switch (type) {
			case Constants.RESULT:
				return new ResultMessage();
			default:
				throw new MessageParseException("unknown type: " + type);
			}
		default:
			throw new InternalError(); // this cannot happen
		}
	}
}