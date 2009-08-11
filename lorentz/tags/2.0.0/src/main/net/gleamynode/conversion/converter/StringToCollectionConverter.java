/*
 *   @(#) $Id: StringToCollectionConverter.java 22 2005-07-29 08:00:54Z trustin $
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
package net.gleamynode.conversion.converter;

import java.util.ArrayList;
import java.util.Collection;

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.converter.support.CollectionUtil;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 22 $, $Date: 2005-07-29 17:00:54 +0900 (Fri, 29 Jul 2005) $
 */
public class StringToCollectionConverter extends Converter {

	public StringToCollectionConverter() {
        super(String.class, Collection.class);
	}
    
    protected StringToCollectionConverter(Class targetType) {
        super(String.class, targetType);
    }

	public Object convert(Object o) throws ConversionException {
        String s = (String)o;
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
						c.add(CollectionUtil.unescape(s, beginIdx, i));
						beginIdx = i + 1;
					}
					break;
			}
		}

		c.add(CollectionUtil.unescape(s, beginIdx, len));
		return c;
	}

	protected Collection newCollection() {
		return new ArrayList();
	}
}