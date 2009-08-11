/*
 *   @(#) $Id: ConverterTest.java 20 2005-07-29 07:53:34Z trustin $
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

import java.math.BigInteger;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ConverterTest extends TestCase {

    public void testNull() {
        Assert.assertNull(Converter.convert(null, Integer.class));
    }
    
    public void testSelfConversion() {
        Assert.assertEquals("1234", Converter.convert("1234", CharSequence.class));
    }
    
    public void testDirectConversion() {
        Assert.assertEquals(
                new Integer(1234),
                Converter.convert("1234", Integer.class));
    }
    
    public void testIndirectConversion() {
        Assert.assertEquals(
                new Integer(1234),
                Converter.convert(new BigInteger("1234"), Integer.class));
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ConverterTest.class);
    }
}
