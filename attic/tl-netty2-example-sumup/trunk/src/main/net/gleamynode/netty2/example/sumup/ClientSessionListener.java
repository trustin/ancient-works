/*
 * @(#) $Id: ClientSessionListener.java 11 2005-04-18 03:42:45Z trustin $
 */
package net.gleamynode.netty2.example.sumup;

import java.net.ConnectException;

import net.gleamynode.netty2.Message;
import net.gleamynode.netty2.Session;
import net.gleamynode.netty2.SessionListener;
import net.gleamynode.netty2.SessionLog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link SessionListener}for SumUp client.
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 11 $, $Date: 2005-04-18 12:42:45 +0900 $
 */
public class ClientSessionListener implements SessionListener {
	private static final Log log = LogFactory
			.getLog(ClientSessionListener.class);

	private final int[] values;
	private boolean complete;

	public ClientSessionListener(int[] values) {
		this.values = values;
	}

	public boolean isComplete() {
		return complete;
	}

	public void connectionEstablished(Session session) {
		SessionLog.info(log, session, "Connection established.");

		// send summation requests
		for (int i = 0; i < values.length; i++) {
			AddMessage m = new AddMessage();
			m.setSequence(i);
			m.setValue(values[i]);
			session.write(m);
		}
	}

	public void connectionClosed(Session session) {
		SessionLog.info(log, session, "Connection closed.");
	}

	public void messageReceived(Session session, Message message) {
		SessionLog.info(log, session, "RCVD: " + message);

		// server only sends ResultMessage. otherwise, we will have to identify
		// its type using instanceof operator.
		ResultMessage rm = (ResultMessage) message;
		if (rm.isOk()) {
			// server returned OK code.
			// if received the result message which has the last sequence
			// number,
			// it is time to disconnect.
			if (rm.getSequence() == values.length - 1) {
				// print the sum and disconnect.
				SessionLog.info(log, session, "The sum: " + rm.getValue());
				session.close();
				complete = true;
			}
		} else {
			// seever returned error code because of overflow, etc.
			SessionLog.error(log, session, "Server error, disconnecting...");
			session.close();
			complete = true;
		}
	}

	public void messageSent(Session session, Message message) {
		SessionLog.info(log, session, "SENT: " + message);
	}

	public void sessionIdle(Session session) {
		// there is no idle time for client
	}

	public void exceptionCaught(Session session, Throwable cause) {
		SessionLog.error(log, session, "Unexpected exception.", cause);

		if (cause instanceof ConnectException) {
			// failed to connect to the server, retry after a while.
			SessionLog.info(log, session, "Sleeping...");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}

			SessionLog.info(log, session, "Reconnecting... ");
			session.start();
		} else {
			session.close();
		}
	}
}