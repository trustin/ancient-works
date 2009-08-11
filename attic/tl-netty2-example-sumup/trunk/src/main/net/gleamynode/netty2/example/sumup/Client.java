/*
 * @(#) $Id: Client.java 11 2005-04-18 03:42:45Z trustin $
 */
package net.gleamynode.netty2.example.sumup;

import java.net.InetSocketAddress;

import net.gleamynode.netty2.IoProcessor;
import net.gleamynode.netty2.MessageRecognizer;
import net.gleamynode.netty2.OrderedEventDispatcher;
import net.gleamynode.netty2.Session;
import net.gleamynode.netty2.ThreadPooledEventDispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (<strong>Entry Point</strong>) Starts SumUp client.
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 11 $, $Date: 2005-04-18 12:42:45 +0900 $
 */
public class Client {
	private static final Log log = LogFactory.getLog(Client.class);
	
	private static final String HOSTNAME = "localhost";
	private static final int PORT = 8080;
	private static final int CONNECT_TIMEOUT = 30; // seconds
	private static final int DISPATCHER_THREAD_POOL_SIZE = 4;

	public static void main(String[] args) throws Throwable {
	    if (args.length == 0) {
	        System.out.println("Please specify the list of any integers");
	        return;
	    }

		// prepare values to sum up
		int[] values = new int[args.length];
		for (int i = 0; i < args.length; i++) {
			values[i] = Integer.parseInt(args[i]);
		}

		// initialize I/O processor and event dispatcher
		IoProcessor ioProcessor = new IoProcessor();
		ThreadPooledEventDispatcher eventDispatcher = new OrderedEventDispatcher();

		// start with the default number of I/O worker threads
		ioProcessor.start();

		// start with a few event dispatcher threads
		eventDispatcher.setThreadPoolSize(DISPATCHER_THREAD_POOL_SIZE);
		eventDispatcher.start();

		// prepare message recognizer
		MessageRecognizer recognizer = new SumUpMessageRecognizer(
				SumUpMessageRecognizer.CLIENT_MODE);

		// create a client session
		Session session = new Session(ioProcessor, new InetSocketAddress(
				HOSTNAME, PORT), recognizer, eventDispatcher);
		
		// set configuration
		session.getConfig().setConnectTimeout(CONNECT_TIMEOUT);
		
		// suscribe and start communication
		ClientSessionListener listener = new ClientSessionListener(values);
		session.addSessionListener(listener);
		
		log.info("Connecting to " + session.getSocketAddress());
		session.start();
		
		// wait until the summation is done
		while ( !listener.isComplete() ) {
			Thread.sleep(1000);
		}
		
		// stop I/O processor and event dispatcher
		eventDispatcher.stop();
		ioProcessor.stop();
	}
}