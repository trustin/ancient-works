/*
 * @(#) $Id: ExceptionMonitor.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2;


/**
 * Monitors uncaught exceptions.  {@link #exceptionCaught(Throwable)} method is
 * invoked by Netty if there were any uncaught exceptions.
 * 
 * @author Trustin Lee
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 */
public interface ExceptionMonitor {
	/**
	 * Invoked when a uncaught exception is thrown.
	 */
	void exceptionCaught(Throwable exception);
}
