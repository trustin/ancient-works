/*
 *   @(#) $Id: StringToDateConverter.java 114 2005-10-01 13:19:54Z trustin $
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
package net.gleamynode.conversion.support;

import java.util.Date;

import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.ConverterContext;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 114 $, $Date: 2005-10-01 22:19:54 +0900 (Sat, 01 Oct 2005) $
 */
public class StringToDateConverter extends Converter {

	public StringToDateConverter() {
        super(String.class, Date.class);
	}

	public Object doConversion(Object o, ConverterContext ctx) {
		return new Date(Long.parseLong((String) o));
	}

    public void doCopy( Object source, Object target, ConverterContext ctx ) {
        ((Date) target).setTime(Long.parseLong((String) source));
    }
    
    
}
