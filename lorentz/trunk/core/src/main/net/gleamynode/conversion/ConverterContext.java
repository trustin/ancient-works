/*
 *   @(#) $Id: ConverterContext.java 129 2005-11-14 09:35:57Z trustin $
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

import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.gleamynode.conversion.support.ConversionPathImpl;

/**
 * Context information such as locale and text format that is provided to
 * {@link Converter}s.
 *
 * @author Trustin Lee
 * @version $Rev: 129 $, $Date: 2005-11-14 18:35:57 +0900 (Mon, 14 Nov 2005) $
 */
public class ConverterContext {
    
    private Locale locale;
    private final ConversionPath path = new ConversionPathImpl();
    private final IntermediaryTypes exclusions = new IntermediaryTypes();
    private final Map formats = new HashMap();
    private final Map attributes = new HashMap();
    
    public ConverterContext() {
        this(null); // use system default locale
    }
    
    public ConverterContext(Locale locale) {
        setLocale(locale);
    }
    
    public IntermediaryTypes getExclusions()
    {
        return exclusions;
    }
    
    /**
     * Returns the path of the recent conversion.
     */
    public ConversionPath getPath() {
        return path;
    }
    
    public Locale getLocale()
    {
        return locale;
    }
    
    public void setLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
            
        this.locale = locale;
    }
    
    public NumberFormat getNumberFormat() {
        return (NumberFormat) getFormat(Number.class);
    }
    
    public void setNumberFormat(NumberFormat format) {
        setFormat(Number.class, format);
    }
    
    public DateFormat getDateFormat() {
        return (DateFormat) getFormat(Date.class);
    }
    
    public void setDateFormat(DateFormat format) {
        setFormat(Date.class, format);
    }
    
    public Format getFormat(Class targetType) {
        return (Format) formats.get(targetType);
    }
    
    public void setFormat(Class targetType, Format format) {
        formats.put(targetType, format);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }
    
    public Set getAttributeNames() {
        return attributes.keySet();
    }

    public Object clone() {
        ConverterContext ret = new ConverterContext();
        ret.locale = this.locale;
        ((ConversionPathImpl) ret.path).setAll(this.path);
        ret.exclusions.addAll(this.exclusions);
        ret.formats.putAll(this.formats);
        ret.attributes.putAll(this.attributes);
        return ret;
    }
}
