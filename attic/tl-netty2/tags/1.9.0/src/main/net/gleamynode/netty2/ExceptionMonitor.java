/*
 * @(#) $Id: ExceptionMonitor.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;


/**
 * Monitors uncaught exceptions.  {@link #exceptionCaught(Throwable)} method is
 * invoked by Netty if there were any uncaught exceptions.
 * 
 * @author Trustin Lee
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
public interface ExceptionMonitor {
	/**
	 * Invoked when a uncaught exception is thrown.
	 */
	void exceptionCaught(Throwable exception);
}
