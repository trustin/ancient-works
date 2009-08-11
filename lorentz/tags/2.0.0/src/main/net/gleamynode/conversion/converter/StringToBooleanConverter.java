/*
 *   @(#) $Id: StringToBooleanConverter.java 21 2005-07-29 07:59:15Z trustin $
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

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 21 $, $Date: 2005-07-29 16:59:15 +0900 (Fri, 29 Jul 2005) $
 */
public class StringToBooleanConverter extends Converter {

	public StringToBooleanConverter() {
        super(String.class, Boolean.class);
	}

	public Object convert(Object o) throws ConversionException {
        String s = (String)o;
		if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)
				|| "on".equalsIgnoreCase(s) || "t".equalsIgnoreCase(s)
				|| "y".equalsIgnoreCase(s) || "1".equals(s))
			return Boolean.TRUE;
		else if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)
				|| "off".equalsIgnoreCase(s) || "f".equalsIgnoreCase(s)
				|| "n".equalsIgnoreCase(s) || "0".equals(s))
			return Boolean.FALSE;
		else
			throw new ConversionException(s);
	}
}