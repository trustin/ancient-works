/*
 * @(#) $Id: LocaleConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl;

import java.util.Locale;
import java.util.StringTokenizer;

import net.gleamynode.convert.ConversionException;
import net.gleamynode.convert.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class LocaleConverter extends Converter {

	public LocaleConverter() {
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof Locale)
			return o.toString();
		else {
			return convertToObject(o.toString()).toString();
		}
	}

	public Object convertToObject(String s) throws ConversionException {
		String language = "";
		String country = "";
		String variant = "";

		StringTokenizer tk = new StringTokenizer(s.toString(), "_");
		if (tk.hasMoreTokens()) {
			language = tk.nextToken().trim();
			if (tk.hasMoreTokens()) {
				country = tk.nextToken().trim();
				if (tk.hasMoreTokens()) {
					variant = tk.nextToken().trim();
				}
			}
		}

		return new Locale(language, country, variant);
	}
}