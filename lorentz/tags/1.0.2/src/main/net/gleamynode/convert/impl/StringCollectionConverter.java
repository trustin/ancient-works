/*
 * @(#) $Id: StringCollectionConverter.java 14 2004-08-02 07:49:16Z trustin $
 */
package net.gleamynode.convert.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.gleamynode.convert.ConversionException;
import net.gleamynode.convert.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 14 $, $Date: 2004-08-02 16:49:16 +0900 (월, 02  8월 2004) $
 */
public class StringCollectionConverter extends Converter {

	public StringCollectionConverter() {
	}

	public String convertToString(Object o) throws ConversionException {
		if (o instanceof Collection) {
			Collection c = (Collection) o;
			StringBuffer buf = new StringBuffer();
			Iterator it = c.iterator();
			if (it.hasNext()) {
				buf.append(escape(Converter.toString(it.next())));

				while (it.hasNext()) {
					buf.append(',');
					buf.append(escape(Converter.toString(it.next())));
				}
			}

			return buf.toString();
		}

		throw new ConversionException("not a Collection: " + o);
	}

	public Object convertToObject(String s) throws ConversionException {
		Collection c = newCollection();
		s = s.trim();
		if (s.length() == 0)
			return c;

		final int len = s.length();
		boolean inQuote = false;
		int beginIdx = 0;
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '"' :
					if (inQuote) {
						if (i != len - 1) {
							if (s.charAt(i + 1) != '"')
								inQuote = false;
							else
								i++;
						} else
							inQuote = false;
					} else
						inQuote = true;
					break;
				case ',' :
					if (!inQuote) {
						c.add(unescape(s, beginIdx, i));
						beginIdx = i + 1;
					}
					break;
			}
		}

		c.add(unescape(s, beginIdx, len));
		return c;
	}

	static String escape(String value) {
		boolean wrapWithQuote = false;
		// +8 is a spare padding for quote expansion
		int len = value.length();
		StringBuffer buf = new StringBuffer(len + 8);
		int i;
		char c;

		if (len == 0)
			return "\"\"";
		if (Character.isWhitespace(value.charAt(0)))
			wrapWithQuote = true;
		else if (Character.isWhitespace(value.charAt(len - 1)))
			wrapWithQuote = true;

		for (i = 0; i < len; i++) {
			c = value.charAt(i);

			switch (c) {
				case ',' :
				case '=' :
					wrapWithQuote = true;
					buf.append(c);
					break;
				case '"' :
					wrapWithQuote = true;
					buf.append("\"\"");
					break;
				default :
					buf.append(c);
			}
		}

		if (wrapWithQuote) {
			return "\"" + buf.toString() + '"';
		} else {
			return buf.toString();
		}
	}

	static String unescape(String value, int begin, int end)
			throws ConversionException {
		// trim left
		for (int i = begin; i < end; i++) {
			if (Character.isWhitespace(value.charAt(i)))
				begin++;
			else
				break;
		}

		// trim right
		for (int i = end - 1; i >= begin; i--) {
			if (Character.isWhitespace(value.charAt(i)))
				end--;
			else
				break;
		}

		// check basic stuff
		switch (end - begin) {
			case 0 :
				throw new ConversionException(
						"Empty string must be quoted by '\"'");
			case 1 :
				if (value.charAt(begin) == '"')
					throw new ConversionException("Mismatching '\"'");
				break;
			case 2 :
				if (value.charAt(begin) == '"'
						&& value.charAt(begin + 1) == '"')
					return "";
		}

		// remove wrapping quotes
		if (value.charAt(begin) == '"') {
			if (value.charAt(end - 1) != '"')
				throw new ConversionException("Mismatching '\"'");
			else {
				begin ++;
				end --;
			}
		}
		
		StringBuffer buf = new StringBuffer(end - begin);
		for (int i = begin; i < end; i++) {
			char c = value.charAt(i);

			switch (c) {
				case '"' :
					if (i == end - 1)
						throw new ConversionException("Mismatching '\"'");
					else if (value.charAt(i) != '"')
						throw new ConversionException("Mismatching '\"'");
					else {
						buf.append('"');
						i++;
					}
					break;
				default :
					buf.append(c);
			}
		}

		return buf.toString();
	}

	protected Collection newCollection() {
		return new ArrayList();
	}
}