/*
 *   @(#) $Id: MapToStringConverter.java 22 2005-07-29 08:00:54Z trustin $
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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.converter.support.CollectionUtil;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 22 $, $Date: 2005-07-29 17:00:54 +0900 (Fri, 29 Jul 2005) $
 */
public class MapToStringConverter extends Converter {

	public MapToStringConverter() {
        super(Map.class, String.class);
	}

	public Object convert(Object o) throws ConversionException {
		Map m = (Map) o;
		StringBuffer buf = new StringBuffer();
		Iterator it = m.entrySet().iterator();
		if (it.hasNext()) {
			Entry e = (Entry) it.next();
			buf.append(CollectionUtil.escape((String) Converter.convert(e.getKey(), String.class)));
			buf.append('=');
			buf.append(CollectionUtil.escape((String) Converter.convert(e.getValue(), String.class)));

			while (it.hasNext()) {
				e = (Entry) it.next();
				buf.append(',');
				buf.append(CollectionUtil.escape((String) Converter.convert(e.getKey(), String.class)));
				buf.append('=');
				buf.append(CollectionUtil.escape((String) Converter.convert(e.getValue(), String.class)));
			}
		}

		return buf.toString();
	}
}