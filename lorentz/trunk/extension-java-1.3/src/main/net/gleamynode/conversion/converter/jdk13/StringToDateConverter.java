/*
 *   @(#) $Id: StringToCollectionConverter.java 86 2005-08-20 07:09:18Z trustin $
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

import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.ConverterContext;

public class StringToDateConverter extends Converter {
	
	public StringToDateConverter() {
		super(String.class, Date.class);
	}

	public Object doConversion(Object o, ConverterContext ctx) throws Exception {
		DateFormat df;
		df = ctx.getDateFormat();
		
		if ( df == null ) {
            df = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.MEDIUM,
                    ctx.getLocale());
		}
		
		return df.parse( (String)o );
	}

    public void doCopy(Object source, Object target, ConverterContext ctx) throws Exception {
        DateFormat df;
        df = ctx.getDateFormat();
        
        if ( df == null ) {
            df = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.MEDIUM,
                    ctx.getLocale());
        }
        
        ((Date) target).setTime(df.parse( (String) source ).getTime());
    }
}