/*
 * @(#) $Id: AddMessage.java 11 2005-04-18 03:42:45Z trustin $
 */
package net.gleamynode.netty2.example.sumup;

import java.nio.ByteBuffer;

import net.gleamynode.netty2.MessageParseException;

/**
 * <code>ADD</code> message in SumUp protocol.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 11 $, $Date: 2005-04-18 12:42:45 +0900 $
 */
public class AddMessage extends AbstractMessage {
	
	private int value;

	public AddMessage() {
		super(Constants.ADD);
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}

	protected boolean readBody(ByteBuffer buf) throws MessageParseException {
		// don't read body if it is partially readable
		if (buf.remaining() < Constants.ADD_BODY_LEN) return false;
		value = buf.getInt();
		return true;
	}

	protected boolean writeBody(ByteBuffer buf) {
		// check if there is enough space to write body
		if (buf.remaining() < Constants.ADD_BODY_LEN)
			return false;
		
		buf.putInt(value);
		
		return true;
	}
	
	public String toString() {
		// it is a good practice to create toString() method on message classes.
		return getSequence() + ":ADD(" + value + ')';
	}
}
