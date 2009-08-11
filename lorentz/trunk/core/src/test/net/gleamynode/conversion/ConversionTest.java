/*
 *   @(#) $Id: ConversionTest.java 122 2005-10-01 15:35:22Z trustin $
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
package net.gleamynode.conversion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.gleamynode.conversion.support.ObjectToStringConverter;
import net.gleamynode.conversion.support.StringToDateConverter;
import net.gleamynode.conversion.support.StringToLongConverter;

public class ConversionTest extends TestCase {

    public void setUp() {
        Converter.register(new ObjectToStringConverter());
        Converter.register(new StringToLongConverter());
        Converter.register(new StringToDateConverter());
    }
    
    public void tearDown() {
        
    }
    
    public void testNull() {
        Assert.assertNull(Converter.convert(null, Long.class));
    }
    
    public void testSelfConversion() {
        // This code shouldn't involve any cloning operations
        String source = "1234";
        Assert.assertSame(source, Converter.convert(source, CharSequence.class));
        
        // This code should cause cloning operation
        List source2 = new ArrayList();
        Assert.assertNotSame(source2, Converter.convert(source2, List.class));
        Assert.assertEquals(source2, Converter.convert(source2, List.class));
    }
    
    public void testDirectConversion() {
        Assert.assertEquals(
                new Long(1234),
                Converter.convert("1234", Long.class));
    }
    
    public void testIndirectConversionA() {
        Assert.assertEquals(
                new Long(1234),
                Converter.convert(new Date(1234), Long.class));
    }
    
    public void testIndirectConversionB() {
        Assert.assertEquals(
                new Date(1234),
                Converter.convert(new Long(1234), Date.class));
    }
    
    public void testExclusion() {
        ConverterContext ctx = new ConverterContext();
        ctx.getExclusions().add(String.class);
        
        try {
            Converter.convert(new Date(1234), Long.class, ctx);
            Assert.fail();
        } catch(NoConversionPathException e) {
            // OK
        }
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ConversionTest.class);
    }
}
