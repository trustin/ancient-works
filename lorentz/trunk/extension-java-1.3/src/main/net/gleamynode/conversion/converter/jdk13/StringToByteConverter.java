/*
 *   @(#) $Id: StringToByteConverter.java 112 2005-10-01 13:05:13Z trustin $
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

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.ConverterContext;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 112 $, $Date: 2005-10-01 22:05:13 +0900 (Sat, 01 Oct 2005) $
 */
public class StringToByteConverter extends Converter {

	public StringToByteConverter() {
        super(String.class, Byte.class);
	}

	public Object doConversion(Object o, ConverterContext ctx) {
        // We don't use NumberFormat to convert a String into a byte because
        // it is not so common; users usually use '0x1234' notation or just plain
        // integer value.
        String val = ((String) o).trim();
        if (val.length() == 0) {
            throw new ConversionException("Can't convert an empty string into byte.");
        }
        
        // 0 - other, 1 - octet,
        // 2 - hexadecimal (0x...), 3 - hexadecimal (#.../$.../x...)
        int type;
        
        switch (val.charAt(0)) {
        case '0':
            // octet or hexadecimal
            if (val.length() == 1) {
                return new Byte((byte) 0);
            }
            
            type = (val.charAt(1) == 'x')? 2 : 1;
            break;
        case '#':
        case '$':
        case 'x':
            type = 3;
            break;
        default:
            type = 0;
        }
        
        switch (type) {
        case 0:
            return new Byte(Byte.parseByte(val));
        case 1:
            return new Byte(Byte.parseByte(val.substring(1), 8));
        case 2:
            return new Byte(Byte.parseByte(val.substring(2), 16));
        case 3:
            return new Byte(Byte.parseByte(val.substring(1), 16));
        default:
            throw new InternalError();
        }
	}
}
