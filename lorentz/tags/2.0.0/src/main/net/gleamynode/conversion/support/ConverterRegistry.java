/*
 *   @(#) $Id: ConverterRegistry.java 36 2005-08-05 12:27:31Z trustin $
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

import java.util.HashMap;
import java.util.Map;

import net.gleamynode.conversion.Converter;

public class ConverterRegistry {
    // Map<sourceType, Map<targetType, Converter>>
    private final Map converters = new HashMap();

    public ConverterRegistry() {
    }

    /**
     * Registers the specified converter.
     */
    public void register(Converter converter) {
        Class sourceType = converter.getSourceType();
        Class targetType = converter.getTargetType();
        Class primitiveSourceType = PrimitiveTypeUtil.toPrimitiveType(sourceType);
        Class primitiveTargetType = PrimitiveTypeUtil.toPrimitiveType(targetType);
        
        synchronized (converters) {
            register0(sourceType, targetType, converter);
            if (primitiveSourceType != null) {
                register0(primitiveSourceType, targetType, converter);
            }
            if (primitiveTargetType != null) {
                register0(sourceType, primitiveTargetType, converter);
            }
            if (primitiveSourceType != null && primitiveTargetType != null) {
                register0(primitiveSourceType, primitiveTargetType, converter);
            }
        }
    }

    private void register0(Class sourceType, Class targetType, Converter converter) {
        Map t2c = getConverters(sourceType);
        
        if (t2c.containsKey(targetType)) {
            throw new IllegalArgumentException(
                    "A converter that converts " + sourceType + " into " +
                    targetType + " is already registered.");
        }
        t2c.put(targetType, converter);
    }
    
    public void deregister(Converter converter) {
        deregister(converter.getSourceType(), converter.getTargetType());
    }
    
    public void deregister(Class sourceType, Class targetType) {
        Class primitiveSourceType = PrimitiveTypeUtil.toPrimitiveType(sourceType);
        Class primitiveTargetType = PrimitiveTypeUtil.toPrimitiveType(targetType);

        synchronized (converters) {
            deregister0(sourceType, targetType);
            if (primitiveSourceType != null) {
                deregister0(primitiveSourceType, targetType);
            }
            if (primitiveTargetType != null) {
                deregister0(sourceType, primitiveTargetType);
            }
            if (primitiveSourceType != null && primitiveTargetType != null) {
                deregister0(primitiveSourceType, primitiveTargetType);
            }
        }
    }
    
    private void deregister0(Class sourceType, Class targetType) {
        Map t2c = getConverters(sourceType);
        t2c.remove(targetType);
    }
    
    public Map getConverters(Class sourceType) {
        Map t2c;
        synchronized (converters) {
            t2c = (Map) converters.get(sourceType);
            if (t2c == null) {
                t2c = new HashMap();
                converters.put(sourceType, t2c);
            }
        }
        return t2c;
    }
}
