/*
 * @(#) $Id: InetAddressConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.gleamynode.convert.ConversionException;
import net.gleamynode.convert.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class InetAddressConverter extends Converter {

	public InetAddressConverter() {
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof InetAddress)
			return ((InetAddress)o).getHostAddress();
		else {
			return convertToObject(o.toString()).toString();
		}
	}

	public Object convertToObject(String s) throws ConversionException {
		try {
			return InetAddress.getByName(s);
		} catch (UnknownHostException e) {
			throw new ConversionException("unknown host: " + s);
		}
	}
}
