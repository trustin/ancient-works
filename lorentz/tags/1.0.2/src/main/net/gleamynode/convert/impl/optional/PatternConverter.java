/*
 * @(#) $Id: PatternConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl.optional;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.gleamynode.convert.ConversionException;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class PatternConverter extends OptionalConverter {

	public PatternConverter() {
	}
	
	public Class getTargetClass() {
		return Pattern.class;
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof Pattern)
			return ((Pattern)o).pattern();
		else {
			return convertToObject(o.toString()).toString();
		}
	}

	public Object convertToObject(String s) throws ConversionException {
		try {
			return Pattern.compile(s);
		} catch (PatternSyntaxException e) {
			throw new ConversionException(s);
		}
	}
}
