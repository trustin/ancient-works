/*
 *   @(#) $Id: Jdk14ConverterPack.java 80 2005-08-19 17:29:22Z trustin $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.gleamynode.conversion.ConverterPack;

public class Jdk14ConverterPack extends ConverterPack {

    public Jdk14ConverterPack() {
    }

    public Iterator newConverters() {
        List converters = new ArrayList();
        
        converters.add(new CharsetToStringConverter());
        converters.add(new InetSocketAddressToStringConverter());
        converters.add(new PatternToStringConverter());
        converters.add(new StringToCharsetConverter());
        converters.add(new StringToCurrencyConverter());
        converters.add(new StringToInetSocketAddressConverter());
        converters.add(new StringToPatternConverter());
        
        return converters.iterator();
    }

}
