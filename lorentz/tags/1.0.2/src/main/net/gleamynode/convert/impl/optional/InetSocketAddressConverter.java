/*
 * @(#) $Id: InetSocketAddressConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl.optional;

import java.net.InetSocketAddress;

import net.gleamynode.convert.ConversionException;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class InetSocketAddressConverter extends OptionalConverter {

	public InetSocketAddressConverter() {
	}

	public Class getTargetClass() {
		return InetSocketAddress.class;
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof InetSocketAddress) {
			InetSocketAddress addr = (InetSocketAddress) o;
			return addr.getAddress().getHostAddress() + ':' + addr.getPort();
		} else {
			return convertToObject(o.toString()).toString();
		}
	}

	public Object convertToObject(String s) throws ConversionException {
		int colonPos = s.lastIndexOf(':');
		if (colonPos < 0)
			throw new ConversionException("no port numer is specified: " + s);

		try {
			return new InetSocketAddress(s.substring(0, colonPos), Integer
					.parseInt(s.substring(colonPos + 1)));
		} catch (Exception e) {
			throw new ConversionException(s);
		}
	}
}