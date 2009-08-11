/*
 * @(#) $Id: CurrencyConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl.optional;

import java.util.Currency;

import net.gleamynode.convert.ConversionException;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class CurrencyConverter extends OptionalConverter {

	public CurrencyConverter() {
	}

	public Class getTargetClass() {
		return Currency.class;
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof Currency)
			return o.toString();
		else {
			return convertToObject(o.toString()).toString();
		}
	}

	public Object convertToObject(String s) throws ConversionException {
		try {
			return Currency.getInstance(s);
		} catch (IllegalArgumentException e) {
			throw new ConversionException(s);
		}
	}
}
