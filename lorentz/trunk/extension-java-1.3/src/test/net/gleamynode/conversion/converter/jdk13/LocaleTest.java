/*
 *   @(#) $Id: StringCollectionConverterTest.java 103 2005-08-28 22:07:38Z jaeheon $
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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.ConverterContext;

/**
 * @author JaeHeon Kim (frima0@inpresence.com)
 * @version $Rev: 103 $, $Date: 2005-08-28 22:07:38 +0900 (Sun, 28 Aug 2005) $
 */
public class LocaleTest extends TestCase {
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(LocaleTest.class);
	}
	
	public void testNumberConverting() throws Throwable {
		
		Converter.register( new Jdk13ConverterPack() );
		ConverterContext ctxUs = new ConverterContext();
		ConverterContext ctxIta = new ConverterContext();
		ctxUs.setLocale(Locale.US);
		ctxIta.setLocale(Locale.ITALY);
		
		String a = "50.95";
		Float b = new Float( a );
		Assert.assertEquals(b, Converter.convert(a, Float.class, ctxUs));
		
		a = "2.048,30";
		Assert.assertEquals(
				new Double(2048.30), Converter.convert(a, Double.class, ctxIta) );
	
		try {
			Converter.convert("a2048", Integer.class, ctxUs);
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}
	
	}
	
	public void testDateTimeConverting() throws Throwable {
		
		Converter.register( new Jdk13ConverterPack() );
		ConverterContext ctx = new ConverterContext();
		ctx.setLocale(Locale.US);
		
		String a = "Aug 28, 2005 10:12:30 pm"; 
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM ,DateFormat.MEDIUM, Locale.US);
		ctx.setDateFormat(df);
		
		Assert.assertEquals( 
				df.parseObject(a), Converter.convert( a, Date.class, ctx) );
		
		Date date = new Date( System.currentTimeMillis() );
		a = df.format( date );
		Assert.assertEquals( 
				df.parseObject(a), Converter.convert(a, Date.class, ctx) );
		
		try {
			Converter.convert("8/28/05 10:12:30 pm", Date.class, ctx);
			Assert.fail("no conversion exception is thrown.");
		} catch (ConversionException e) {
		}
		
	}
	
}
