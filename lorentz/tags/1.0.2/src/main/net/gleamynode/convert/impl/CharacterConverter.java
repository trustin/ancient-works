/*
 * @(#) $Id: CharacterConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl;

import net.gleamynode.convert.ConversionException;
import net.gleamynode.convert.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class CharacterConverter extends Converter {

	public CharacterConverter() {
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof Character)
			return o.toString();
		else {
			return convertToObject(o.toString()).toString();
		}
	}

	public Object convertToObject(String s) throws ConversionException {
		if (s.length() != 1)
			throw new ConversionException(s);
		return new Character(s.charAt(0));
	}
}
