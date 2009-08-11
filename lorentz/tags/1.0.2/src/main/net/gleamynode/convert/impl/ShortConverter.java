/*
 * @(#) $Id: ShortConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl;

import net.gleamynode.convert.ConversionException;
import net.gleamynode.convert.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class ShortConverter extends Converter {

	public ShortConverter() {
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof Short)
			return o.toString();
		else {
			return convertToObject(o.toString()).toString();
		}
	}

	public Object convertToObject(String s) throws ConversionException {
		try {
			return new Short(s);
		} catch (NumberFormatException e) {
			throw new ConversionException(s);
		}
	}
}
