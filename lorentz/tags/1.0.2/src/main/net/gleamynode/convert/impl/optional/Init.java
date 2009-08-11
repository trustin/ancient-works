/*
 * @(#) $Id: Init.java 8 2004-08-02 03:04:49Z trustin $
 */
package net.gleamynode.convert.impl.optional;

import net.gleamynode.convert.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 8 $, $Date: 2004-08-02 12:04:49 +0900 (월, 02  8월 2004) $
 */
public class Init {
	static {
		register("Currency");
		register("InetSocketAddress");
		register("ObjectName");
		register("Pattern");
		register("Charset");
	}

	private static void register(String name) {
		OptionalConverter converter;
		try {
			converter = (OptionalConverter) Class.forName(
					Init.class.getPackage().getName() + '.' + name
							+ "Converter", true, Init.class.getClassLoader())
					.newInstance();
			Converter.register(converter.getTargetClass(), converter);
		} catch (Throwable t) {
		}
	}

	public Init() {
	}
}