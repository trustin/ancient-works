/*
 * @(#) $Id: StringMapConverter.java 7 2004-08-02 02:27:38Z trustin $
 */
package net.gleamynode.convert.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.gleamynode.convert.ConversionException;
import net.gleamynode.convert.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 7 $, $Date: 2004-08-02 11:27:38 +0900 (월, 02  8월 2004) $
 */
public class StringMapConverter extends Converter {

	public StringMapConverter() {
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof Map) {
			Map m = (Map) o;
			StringBuffer buf = new StringBuffer();
			Iterator it = m.entrySet().iterator();
			if (it.hasNext()) {
				Entry e = (Entry) it.next();
				buf.append(StringCollectionConverter.escape(Converter
						.toString(e.getKey())));
				buf.append('=');
				buf.append(StringCollectionConverter.escape(Converter
						.toString(e.getValue())));

				while (it.hasNext()) {
					e = (Entry) it.next();
					buf.append(',');
					buf.append(StringCollectionConverter.escape(Converter
							.toString(e.getKey())));
					buf.append('=');
					buf.append(StringCollectionConverter.escape(Converter
							.toString(e.getValue())));
				}
			}

			return buf.toString();
		}

		throw new ConversionException("not a Map: " + o);
	}

	public Object convertToObject(String s) throws ConversionException {
		Map m = newMap();
		s = s.trim();
		if (s.length() == 0)
			return m;

		final int len = s.length();
		boolean inQuote = false;
		String key = null;
		int beginIdx = 0;
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '"' :
					if (inQuote) {
						if (i != len - 1) {
							if (s.charAt(i + 1) != ch)
								inQuote = false;
							else
								i++;
						} else
							inQuote = false;
					} else
						inQuote = true;
					break;
				case '=' :
					if (!inQuote) {
						if (key != null)
							throw new ConversionException("No value is specified.");
						key = StringCollectionConverter.unescape(s, beginIdx, i);
						beginIdx = i + 1;
					}
					break;
				case ',' :
					if (!inQuote) {
						if (key == null)
							throw new ConversionException("No value is specified.");
						m.put(key, StringCollectionConverter.unescape(s, beginIdx, i));
						key = null;
						beginIdx = i + 1;
					}
					break;
			}
		}

		if (key == null)
			throw new ConversionException("Unexpected end of string");
		else
			m.put(key, StringCollectionConverter.unescape(s, beginIdx, len));
		return m;
	}

	protected Map newMap() {
		return new HashMap();
	}
}