/*
 *   @(#) $Id: StringToInetSocketAddressConverter.java 21 2005-07-29 07:59:15Z trustin $
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

import java.net.InetSocketAddress;

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 21 $, $Date: 2005-07-29 16:59:15 +0900 (Fri, 29 Jul 2005) $
 */
public class StringToInetSocketAddressConverter extends Converter {

	public StringToInetSocketAddressConverter() {
        super(String.class, InetSocketAddress.class);
	}

	public Object convert(Object o) {
        String s = (String)o;
		int colonPos = s.lastIndexOf(':');
		if (colonPos < 0)
			throw new ConversionException("no port numer is specified: " + s);

		return new InetSocketAddress(s.substring(0, colonPos), Integer
				.parseInt(s.substring(colonPos + 1)));
	}
}