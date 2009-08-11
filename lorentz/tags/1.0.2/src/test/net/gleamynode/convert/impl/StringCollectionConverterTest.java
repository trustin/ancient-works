/*
 * @(#) $Id: StringCollectionConverterTest.java 15 2004-08-02 07:52:11Z trustin $
 */
package net.gleamynode.convert.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.gleamynode.convert.ConversionException;
import net.gleamynode.convert.impl.StringCollectionConverter;
import net.gleamynode.convert.impl.StringMapConverter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 15 $, $Date: 2004-08-02 16:52:11 +0900 (월, 02  8월 2004) $
 */
public class StringCollectionConverterTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(StringCollectionConverterTest.class);
	}

	public void testCollectionConversion() throws Throwable {
		StringCollectionConverter converter = new StringCollectionConverter();

		String a = "a, b";
		Collection b = new ArrayList();
		b.add("a");
		b.add("b");
		Assert.assertEquals(b, converter.convertToObject(a));
		Assert.assertEquals("a,b", converter.convertToString(b));

		a = "\"a\"\",\", b, c";
		b.clear();
		b.add("a\",");
		b.add("b");
		b.add("c");
		Assert.assertEquals(b, converter.convertToObject(a));
		Assert.assertEquals("\"a\"\",\",b,c", converter.convertToString(b));

		a = "\"\",\"\",\"\"";
		b.clear();
		b.add("");
		b.add("");
		b.add("");
		Assert.assertEquals(b, converter.convertToObject(a));
		Assert.assertEquals(a, converter.convertToString(b));

		try {
			converter.convertToObject("\"a");
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}
	}

	public void testMapConversion() throws Throwable {
		StringMapConverter converter = new StringMapConverter();
		String a = "a = b, c = d";
		Map b = new HashMap();
		b.put("a", "b");
		b.put("c", "d");
		Assert.assertEquals(b, converter.convertToObject(a));
		Assert.assertEquals("a=b,c=d", converter.convertToString(b));

		a = "\"a\"\",=\" = b";
		b.clear();
		b.put("a\",=", "b");
		Assert.assertEquals(b, converter.convertToObject(a));
		Assert.assertEquals("\"a\"\",=\"=b", converter.convertToString(b));
		
		a = "\"a\"= \"\"";
		b.clear();
		b.put("a", "");
		Assert.assertEquals(b, converter.convertToObject(a));
		Assert.assertEquals("a=\"\"", converter.convertToString(b));

		a = "\"\"=b";
		b.clear();
		b.put("", "b");
		Assert.assertEquals(b, converter.convertToObject(a));
		Assert.assertEquals(a, converter.convertToString(b));
		
		a = "e=\"e=mc^2\"";
		b.clear();
		b.put("e", "e=mc^2");
		Assert.assertEquals(b, converter.convertToObject(a));
		Assert.assertEquals(a, converter.convertToString(b));

		try {
			converter.convertToObject("\"a");
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}

		try {
			converter.convertToObject("a, b");
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}

		try {
			converter.convertToObject("a = b, c, d = e");
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}

		try {
			converter.convertToObject("a, b = c");
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}
	}
}