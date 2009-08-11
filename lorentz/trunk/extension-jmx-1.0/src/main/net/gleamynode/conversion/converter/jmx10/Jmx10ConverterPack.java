/*
 *   @(#) $Id: Jmx10ConverterPack.java 80 2005-08-19 17:29:22Z trustin $
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
package net.gleamynode.conversion.converter.jmx10;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.gleamynode.conversion.ConverterPack;

public class Jmx10ConverterPack extends ConverterPack {

    public Jmx10ConverterPack() {
    }

    public Iterator newConverters() {
        List converters = new ArrayList();
        
        converters.add(new ObjectNameToStringConverter());
        converters.add(new StringToObjectNameConverter());
        
        return converters.iterator();
    }

}
