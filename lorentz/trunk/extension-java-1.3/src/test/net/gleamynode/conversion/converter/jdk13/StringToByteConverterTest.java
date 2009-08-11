/*
 *   @(#) $Id: StringCollectionConverterTest.java 99 2005-08-28 12:32:27Z trustin $
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

import java.net.InetAddress;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;

public class StringToByteConverterTest extends TestCase {

    public static void main( String[] args ) {
        junit.textui.TestRunner.run( StringToByteConverterTest.class );
    }

    protected void setUp() throws Exception {
        Converter.register( new Jdk13ConverterPack() );

        // It takes too long to resolve the host name which is not actually.
        Converter.getDefaultContext().getExclusions().add(InetAddress.class);
    }

    public void testDecimalConversion() {
        Assert.assertEquals( new Byte((byte) 0), Converter.convert("0", Byte.class));
        Assert.assertEquals( new Byte((byte) 100), Converter.convert("100", Byte.class));
        Assert.assertEquals( new Byte((byte) -100), Converter.convert("-100", Byte.class));
    }
    
    public void testOctetConversion() {
        Assert.assertEquals( new Byte((byte) 28), Converter.convert("034", Byte.class));
    }
    
    public void testHexadecimalConversion() {
        Assert.assertEquals( new Byte((byte) 52), Converter.convert("#34", Byte.class));
        Assert.assertEquals( new Byte((byte) 52), Converter.convert("$34", Byte.class));
        Assert.assertEquals( new Byte((byte) 52), Converter.convert("x34", Byte.class));
        Assert.assertEquals( new Byte((byte) 52), Converter.convert("0x34", Byte.class));
    }
    
    public void testInvalidValue() {
        try {
            Converter.convert("", Byte.class);
            Assert.fail();
        } catch (ConversionException e) {
            // OK
        }
        try {
            Converter.convert("#", Byte.class);
            Assert.fail();
        } catch (ConversionException e) {
            // OK
        }
        try {
            Converter.convert("$", Byte.class);
            Assert.fail();
        } catch (ConversionException e) {
            // OK
        }
        try {
            Converter.convert("x", Byte.class);
            Assert.fail();
        } catch (ConversionException e) {
            // OK
        }
        try {
            Converter.convert("0x", Byte.class);
            Assert.fail();
        } catch (ConversionException e) {
            // OK
        }
    }
}
