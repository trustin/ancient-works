/*
 *   @(#) $Id: StringCollectionConverterTest.java 21 2005-07-29 07:59:15Z trustin $
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
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 21 $, $Date: 2005-07-29 16:59:15 +0900 (Fri, 29 Jul 2005) $
 */
public class StringCollectionConverterTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(StringCollectionConverterTest.class);
	}

	public void testCollectionConversion() throws Throwable {
		String a = "a, b";
		Collection b = new ArrayList();
		b.add("a");
		b.add("b");
		Assert.assertEquals(b, Converter.convert(a, Collection.class));
		Assert.assertEquals("a,b", Converter.convert(b, String.class));

		a = "\"a\"\",\", b, c";
		b.clear();
		b.add("a\",");
		b.add("b");
		b.add("c");
		Assert.assertEquals(b, Converter.convert(a, Collection.class));
		Assert.assertEquals("\"a\"\",\",b,c", Converter.convert(b, String.class));

		a = "\"\",\"\",\"\"";
		b.clear();
		b.add("");
		b.add("");
		b.add("");
		Assert.assertEquals(b, Converter.convert(a, Collection.class));
		Assert.assertEquals(a, Converter.convert(b, String.class));

		try {
            Converter.convert("\"a", Collection.class);
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}
	}

	public void testMapConversion() throws Throwable {
		String a = "a = b, c = d";
		Map b = new HashMap();
		b.put("a", "b");
		b.put("c", "d");
		Assert.assertEquals(b, Converter.convert(a, Map.class));
		Assert.assertEquals("a=b,c=d", Converter.convert(b, String.class));

		a = "\"a\"\",=\" = b";
		b.clear();
		b.put("a\",=", "b");
		Assert.assertEquals(b, Converter.convert(a, Map.class));
		Assert.assertEquals("\"a\"\",=\"=b", Converter.convert(b, String.class));
		
		a = "\"a\"= \"\"";
		b.clear();
		b.put("a", "");
		Assert.assertEquals(b, Converter.convert(a, Map.class));
		Assert.assertEquals("a=\"\"", Converter.convert(b, String.class));

		a = "\"\"=b";
		b.clear();
		b.put("", "b");
		Assert.assertEquals(b, Converter.convert(a, Map.class));
		Assert.assertEquals(a, Converter.convert(b, String.class));
		
		a = "e=\"e=mc^2\"";
		b.clear();
		b.put("e", "e=mc^2");
		Assert.assertEquals(b, Converter.convert(a, Map.class));
		Assert.assertEquals(a, Converter.convert(b, String.class));

		try {
            Converter.convert("\"a", Map.class);
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}

		try {
            Converter.convert("a, b", Map.class);
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}

		try {
            Converter.convert("a = b, c, d = e", Map.class);
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}

		try {
            Converter.convert("a, b = c", Map.class);
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}
	}
}