/*
 * @(#) $Id: BooleanConverter.java 4 2004-07-19 08:38:33Z trustin $
 */
package net.gleamynode.convert.impl;

import net.gleamynode.convert.ConversionException;
import net.gleamynode.convert.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 4 $, $Date: 2004-07-19 17:38:33 +0900 (월, 19  7월 2004) $
 */
public class BooleanConverter extends Converter {

	public BooleanConverter() {
	}

	public String convertToString(Object o) throws ConversionException {
		boolean value;
		if (o instanceof Boolean)
			value = o.equals(Boolean.TRUE);
		else
			value = convertToObject(o.toString()) == Boolean.TRUE;
		
		return value? "true" : "false";
	}

	public Object convertToObject(String s) throws ConversionException {
		if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)
				|| "on".equalsIgnoreCase(s) || "t".equalsIgnoreCase(s)
				|| "y".equalsIgnoreCase(s) || "1".equals(s))
			return Boolean.TRUE;
		else if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)
				|| "off".equalsIgnoreCase(s) || "f".equalsIgnoreCase(s)
				|| "n".equalsIgnoreCase(s) || "0".equals(s))
			return Boolean.FALSE;
		else
			throw new ConversionException(s);
	}
}