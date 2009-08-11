/*
 * @(#) $Id: Server.java 11 2005-04-18 03:42:45Z trustin $
 */
package net.gleamynode.netty2.example.sumup;

import java.net.InetSocketAddress;

import net.gleamynode.netty2.IoProcessor;
import net.gleamynode.netty2.MessageRecognizer;
import net.gleamynode.netty2.OrderedEventDispatcher;
import net.gleamynode.netty2.SessionServer;
import net.gleamynode.netty2.ThreadPooledEventDispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (<strong>Entry Point</strong>) Starts SumUp server.
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 11 $, $Date: 2005-04-18 12:42:45 +0900 $
 */
public class Server {
	private static final Log log = LogFactory.getLog(Server.class);

	private static final int SERVER_PORT = 8080;
	private static final int DISPATCHER_THREAD_POOL_SIZE = 16;

	public static void main(String[] args) throws Throwable {
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
				SumUpMessageRecognizer.SERVER_MODE);
		
		// prepare session event listener which will provide communication workflow.
		ServerSessionListener listener = new ServerSessionListener();

		// prepare session server
		SessionServer server = new SessionServer();
		server.setIoProcessor(ioProcessor);
		server.setEventDispatcher(eventDispatcher);
		server.setMessageRecognizer(recognizer);
		
		server.addSessionListener(listener);
		server.setBindAddress(new InetSocketAddress(SERVER_PORT));
		
		// open the server port, accept connections, and start communication
		log.info("Listening on port " + SERVER_PORT);
		server.start();
	}
}