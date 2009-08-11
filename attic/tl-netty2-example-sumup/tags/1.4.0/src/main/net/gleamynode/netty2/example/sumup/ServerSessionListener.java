/*
 * @(#) $Id: ServerSessionListener.java 17 2005-04-19 15:29:11Z trustin $
 */
package net.gleamynode.netty2.example.sumup;

import net.gleamynode.netty2.Message;
import net.gleamynode.netty2.Session;
import net.gleamynode.netty2.SessionListener;
import net.gleamynode.netty2.SessionLog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link SessionListener}for SumUp server.
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 17 $, $Date: 2005-04-20 00:29:11 +0900 $
 */
public class ServerSessionListener implements SessionListener {
	private static final Log log = LogFactory
			.getLog(ServerSessionListener.class);

	public ServerSessionListener() {
	}

	public void connectionEstablished(Session session) {
		SessionLog.info(log, session, "Connection established.");

		// set idle time to 60 seconds
		session.getConfig().setIdleTime(60);

		// initial sum is zero
		session.setAttachment(new Integer(0));
	}

	public void connectionClosed(Session session) {
		SessionLog.info(log, session, "Connection closed.");
	}

	public void messageReceived(Session session, Message message) {
		SessionLog.info(log, session, "RCVD: " + message);

		// client only sends AddMessage. otherwise, we will have to identify
		// its type using instanceof operator.
		AddMessage am = (AddMessage) message;

		// add the value to the current sum.
		int sum = ((Integer) session.getAttachment()).intValue();
		int value = am.getValue();
		long expectedSum = (long) sum + value;
		if (expectedSum > Integer.MAX_VALUE || expectedSum < Integer.MIN_VALUE) {
			// if the sum overflows or underflows, return error message
			ResultMessage rm = new ResultMessage();
			rm.setSequence(am.getSequence()); // copy sequence
			rm.setOk(false);
			session.write(rm);
		} else {
			// sum up
			sum = (int) expectedSum;
			session.setAttachment(new Integer(sum));

			// return the result message
			ResultMessage rm = new ResultMessage();
			rm.setSequence(am.getSequence()); // copy sequence
			rm.setOk(true);
			rm.setValue(sum);
			session.write(rm);
		}
	}

	public void messageSent(Session session, Message message) {
		SessionLog.info(log, session, "SENT: " + message);
	}

	public void sessionIdle(Session session) {
		SessionLog.warn(log, session, "Disconnecting the idle.");
		// disconnect an idle client
		session.close();
	}

	public void exceptionCaught(Session session, Throwable cause) {
		SessionLog.error(log, session, "Unexpected exception.", cause);
		// close the connection on exceptional situation
		session.close();
	}
}