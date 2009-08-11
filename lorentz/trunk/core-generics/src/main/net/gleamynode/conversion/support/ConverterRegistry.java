/*
 *   @(#) $Id: ConverterRegistry.java 129 2005-11-14 09:35:57Z trustin $
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.gleamynode.conversion.Converter;

public class ConverterRegistry {
    // Map<sourceType, Map<targetType, Converter>>
    private final Map<Class, Map<Class, Converter>> converters =
        new HashMap<Class, Map<Class, Converter>>();
    private final Set<Converter> convertersWithCopy = new HashSet<Converter>();
    private final Set<Converter> convertersWithConversion = new HashSet<Converter>();
    private final Set<Class> immutableTypes = new HashSet<Class>();
    private final Set<Class> mutableTypes = new HashSet<Class>();
    private final ConversionPathCache conversionCache;
    private final ConversionPathCache copyCache;

    public ConverterRegistry( ConversionPathCache conversionCache, ConversionPathCache copyCache ) {
        this.conversionCache = conversionCache;
        this.copyCache = copyCache;
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
        // Test if doConversion method throws UnsupportedOperationException.
        // If it doesn't throw an UnsupportedOperationException, we can believe
        // the converter provides conversion operation.
        boolean supportsConversion = false;
        try {
            converter.doConversion(null, null);
            supportsConversion = true;
        } catch (UnsupportedOperationException e) {
        } catch (Throwable t) {
            supportsConversion = true;
        }
        
        if (supportsConversion) {
            convertersWithConversion.add(converter);
        }

        // Test if doCopy method throws UnsupportedOperationException.
        // If it doesn't throw an UnsupportedOperationException, we can believe
        // the target type is mutable and the converter provides copy operation.
        boolean supportsCopy = false;
        try {
            converter.doCopy(null, null, null);
            supportsCopy = true;
        } catch (UnsupportedOperationException e) {
        } catch (Throwable t) {
            supportsCopy = true;
        }
        
        if (supportsCopy) {
            mutableTypes.add(targetType);
            immutableTypes.remove(targetType); // Revoke the belief
            convertersWithCopy.add(converter);
        } else {
            if (!mutableTypes.contains(targetType)) {
                immutableTypes.add(targetType);
            }
        }
        
        // Register converter
        getConverters(sourceType).put(targetType, converter);
        
        if (supportsConversion)
        {
            conversionCache.invalidate();
            copyCache.invalidate();
        }
        
        if (supportsCopy) {
            copyCache.invalidate();
        }
    }
    
    public void deregister(Converter converter) {
        deregister(converter.getSourceType(), converter.getTargetType(), converter);
    }
    
    private void deregister(Class sourceType, Class targetType, Converter converter) {
        Class primitiveSourceType = PrimitiveTypeUtil.toPrimitiveType(sourceType);
        Class primitiveTargetType = PrimitiveTypeUtil.toPrimitiveType(targetType);

        synchronized (converters) {
            deregister0(sourceType, targetType, converter);
            if (primitiveSourceType != null) {
                deregister0(primitiveSourceType, targetType, converter);
            }
            if (primitiveTargetType != null) {
                deregister0(sourceType, primitiveTargetType, converter);
            }
            if (primitiveSourceType != null && primitiveTargetType != null) {
                deregister0(primitiveSourceType, primitiveTargetType, converter);
            }
        }
    }
    
    private void deregister0(Class sourceType, Class targetType, Converter converter) {
        Map<Class, Converter> t2c = getConverters(sourceType);
        
        if ( t2c != null ) {
            t2c.remove(targetType);
        
            if ( t2c.isEmpty() ) {
                converters.remove(t2c);
            }
        }
        
        if (convertersWithConversion.remove(converter)) {
            conversionCache.invalidate();
            copyCache.invalidate();
        }
        
        if (convertersWithCopy.remove(converter)) {
            copyCache.invalidate();
        }
    }
    
    public Map<Class, Converter> getConverters(Class sourceType) {
        Map<Class, Converter> t2c;
        synchronized (converters) {
            t2c = converters.get(sourceType);
            if (t2c == null) {
                t2c = new HashMap<Class, Converter>();
                converters.put(sourceType, t2c);
            }
        }
        return t2c;
    }
    
    public boolean isImmutable(Class targetType) {
        return immutableTypes.contains(targetType);
    }
    
    public boolean isMutable(Class targetType) {
        return mutableTypes.contains(targetType);
    }
    
    public boolean supportsConversion(Converter converter) {
        return convertersWithConversion.contains(converter);
    }

    public boolean supportsCopy(Converter converter) {
        return convertersWithCopy.contains(converter);
    }
}
