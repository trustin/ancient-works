/*
 * @(#) $Id: CharsetConverter.java 8 2004-08-02 03:04:49Z trustin $
 */
package net.gleamynode.convert.impl.optional;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import net.gleamynode.convert.ConversionException;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 8 $, $Date: 2004-08-02 12:04:49 +0900 (월, 02  8월 2004) $
 */
public class CharsetConverter extends OptionalConverter {

	public CharsetConverter() {
	}

	public Class getTargetClass() {
		return Charset.class;
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof Charset)
			return ((Charset) o).name();
		else
			return ((Charset) convertToObject(o.toString())).name();
	}

	public Object convertToObject(String s) throws ConversionException {
		try {
			return Charset.forName(s);
		} catch (IllegalCharsetNameException e) {
			throw new ConversionException("Illegal charset name: " + s);
			
		} catch (UnsupportedCharsetException e) {
			throw new ConversionException("Unsupported charset: " + s);
		}
	}
}