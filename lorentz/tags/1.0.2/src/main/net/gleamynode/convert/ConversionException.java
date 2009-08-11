/*
 * @(#) $Id: ConversionException.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert;

/**
 * Thrown when {@link Converter}failed to convert an object into a string or
 * vice versa.
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class ConversionException extends RuntimeException {
	/**
	 * Creates a new exception with a message.
	 */
	public ConversionException(String message) {
		super(message);
	}
}
