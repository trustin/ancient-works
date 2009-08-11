/*
 * @(#) $Id: ObjectNameConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl.optional;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.gleamynode.convert.ConversionException;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class ObjectNameConverter extends OptionalConverter {

	public ObjectNameConverter() {
	}

	public Class getTargetClass() {
		return ObjectName.class;
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof ObjectName)
			return ((ObjectName) o).getCanonicalName();
		else {
			return convertToObject(o.toString()).toString();
		}
	}

	public Object convertToObject(String s) throws ConversionException {
		try {
			return new ObjectName(s);
		} catch (MalformedObjectNameException e) {
			throw new ConversionException(s);
		}
	}
}