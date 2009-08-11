/*
 *   @(#) $Id: StringToMapConverter.java 112 2005-10-01 13:05:13Z trustin $
 *
 *   Copyright 2004 Trustin Lee
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package net.gleamynode.conversion.converter.jdk13;

import java.util.HashMap;
import java.util.Map;

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.ConverterContext;
import net.gleamynode.conversion.converter.jdk13.support.CollectionUtil;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 112 $, $Date: 2005-10-01 22:05:13 +0900 (Sat, 01 Oct 2005) $
 */
public class StringToMapConverter extends Converter {

	public StringToMapConverter() {
        super(String.class, Map.class);
	}
    
    protected StringToMapConverter(Class targetClass) {
        super(String.class, targetClass);
    }

	public Object doConversion(Object o, ConverterContext ctx) throws ConversionException {
        String s = (String)o;
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
						key = CollectionUtil.unescape(s, beginIdx, i);
						beginIdx = i + 1;
					}
					break;
				case ',' :
					if (!inQuote) {
						if (key == null)
							throw new ConversionException("No value is specified.");
						m.put(key, CollectionUtil.unescape(s, beginIdx, i));
						key = null;
						beginIdx = i + 1;
					}
					break;
			}
		}

		if (key == null)
			throw new ConversionException("Unexpected end of string");
		else
			m.put(key, CollectionUtil.unescape(s, beginIdx, len));
		return m;
	}

	protected Map newMap() {
		return new HashMap();
	}
}