/*
 *   @(#) $Id: Jdk13ConverterPack.java 101 2005-08-28 13:01:05Z trustin $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.gleamynode.conversion.ConverterPack;

public class Jdk13ConverterPack extends ConverterPack {

    public Jdk13ConverterPack() {
    }

    public Iterator newConverters() {
        List converters = new ArrayList();

        converters.add(new ClassToStringConverter());
        converters.add(new CollectionToStringConverter());
        converters.add(new InetAddressToStringConverter());
        converters.add(new MapToStringConverter());
        converters.add(new ObjectToStringConverter());
        converters.add(new StringToBigDecimalConverter());
        converters.add(new StringToBigIntegerConverter());
        converters.add(new StringToBooleanConverter());
        converters.add(new StringToByteConverter());
        converters.add(new StringToCharacterConverter());
        converters.add(new StringToClassConverter());
        converters.add(new StringToCollectionConverter());
        converters.add(new StringToDoubleConverter());
        converters.add(new StringToFileConverter());
        converters.add(new StringToFloatConverter());
        converters.add(new StringToInetAddressConverter());
        converters.add(new StringToIntegerConverter());
        converters.add(new StringToListConverter());
        converters.add(new StringToLocaleConverter());
        converters.add(new StringToLongConverter());
        converters.add(new StringToMapConverter());
        converters.add(new StringToNumberConverter());
        converters.add(new StringToPropertiesConverter());
        converters.add(new StringToSetConverter());
        converters.add(new StringToShortConverter());
        converters.add(new StringToTimeZoneConverter());
        converters.add(new StringToUrlConverter());
        converters.add(new TimeZoneToStringConverter());
        converters.add(new StringToDateConverter());
        
        return converters.iterator();
    }
}
