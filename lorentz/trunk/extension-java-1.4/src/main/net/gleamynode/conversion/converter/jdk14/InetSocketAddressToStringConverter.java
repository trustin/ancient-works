/*
 *   @(#) $Id: InetSocketAddressToStringConverter.java 113 2005-10-01 13:06:43Z trustin $
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
package net.gleamynode.conversion.converter.jdk14;

import java.net.InetSocketAddress;

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.ConverterContext;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 113 $, $Date: 2005-10-01 22:06:43 +0900 (Sat, 01 Oct 2005) $
 */
public class InetSocketAddressToStringConverter extends Converter {

	public InetSocketAddressToStringConverter() {
        super(InetSocketAddress.class, String.class);
	}

	public Object doConversion(Object o, ConverterContext ctx) throws ConversionException {
		InetSocketAddress addr = (InetSocketAddress) o;
		return addr.getAddress().getHostAddress() + ':' + addr.getPort();
	}
}