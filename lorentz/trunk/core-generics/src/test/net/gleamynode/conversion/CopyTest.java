/*
 *   @(#) $Id: CopyTest.java 122 2005-10-01 15:35:22Z trustin $
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

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.gleamynode.conversion.support.ObjectToStringConverter;
import net.gleamynode.conversion.support.StringToDateConverter;
import net.gleamynode.conversion.support.StringToLongConverter;

public class CopyTest extends TestCase {

    public void setUp() {
        Converter.register(new ObjectToStringConverter());
        Converter.register(new StringToLongConverter());
        Converter.register(new StringToDateConverter());
    }
    
    public void tearDown() {
        
    }
    
    public void testNull() {
        try {
            Converter.copy(null, new Date());
            Assert.fail();
        } catch (NullPointerException e) {
            // OK
        }
        
        try {
            Converter.copy(new Long(1234), null);
            Assert.fail();
        } catch (NullPointerException e) {
            // OK
        }
    }
    
    public void testSelfConversion() {
        Date source = new Date(1234);
        Date target = new Date();
        Converter.copy(source, target);
        Assert.assertEquals(source, target);
    }
    
    public void testDirectConversion() {
        String source = "1234";
        Date target = new Date();
        Converter.copy(source, target);
        Assert.assertEquals(source, String.valueOf(target.getTime()));
    }
    
    public void testIndirectConversion() {
        Long source = new Long(1234);
        Date target = new Date();
        Converter.copy(source, target);
        Assert.assertEquals(source, new Long(target.getTime()));
    }

    public void testExclusion() {
        ConverterContext ctx = new ConverterContext();
        ctx.getExclusions().add(String.class);
        
        try {
            Converter.copy(new Long(1234), new Date(), ctx);
            Assert.fail();
        } catch(NoConversionPathException e) {
            // OK
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(CopyTest.class);
    }
}
