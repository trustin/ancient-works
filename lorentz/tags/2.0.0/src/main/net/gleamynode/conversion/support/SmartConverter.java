/*
 *   @(#) $Id: SmartConverter.java 36 2005-08-05 12:27:31Z trustin $
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.NoConversionPathException;

public class SmartConverter {
    private static final Map EMPTY_MAP = new HashMap(); 
    private final ConverterRegistry registry;

    public SmartConverter(ConverterRegistry registry) {
        this.registry = registry;
    }
    
    public Object convert(Object source, Class targetClass) {
        if (source == null)
            return null;
        
        if (targetClass.isAssignableFrom(source.getClass()))
            return source;
        
        List chosenConverters = new ArrayList();
        if (findConverters(
                source.getClass(), targetClass,
                chosenConverters)) {
            Iterator it = chosenConverters.iterator();
            try {
                // Execute the conversion chain.
                while (it.hasNext()) {
                    source = ((Converter) it.next()).convert(source);
                }
            } catch (Exception e) {
                throw new ConversionException(
                        "Exception caught while executing a conversion chain: " +
                        toConversionChainString(source, chosenConverters), e);
            }
            
            return source;
            
        } else {
            throw new NoConversionPathException();
        }
    }


    private boolean findConverters(Class sourceType, Class targetType, List chosenConverterList) {
        Map converters = getConverters(sourceType, new HashSet());
        if (converters.size() == 0) {
            return false;
        }
        
        Iterator it = converters.values().iterator();
        outerLoop:
        while (it.hasNext()) {
            Converter c = (Converter) it.next();
            
            // Prevent infinite recursion
            Iterator ccIt = chosenConverterList.iterator();
            while (ccIt.hasNext()) {
                Converter oldConverter = (Converter) ccIt.next();
                if (oldConverter == c) {
                    continue outerLoop;
                }
                if (oldConverter.getSourceType() == c.getTargetType()) {
                    continue outerLoop;
                }
            }

            chosenConverterList.add(c);

            if (c.getTargetType() == targetType) {
                return true;
            }
            if (findConverters(c.getTargetType(), targetType, chosenConverterList)) {
                return true;
            }
            
            chosenConverterList.remove(chosenConverterList.size() - 1);
        }
        
        return false;
    }

    public Map getConverters(Class sourceType, Set triedClasses)
            throws ConversionException {
        if (triedClasses.contains(sourceType))
            return EMPTY_MAP;
        triedClasses.add(sourceType);

        Map converters = registry.getConverters(sourceType);
        if (converters.size() == 0) {
            if (sourceType.isPrimitive()) {
                
            } else {
                // Find a superclass first (except Object)
                Class superClass = sourceType.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    converters = getConverters(superClass, triedClasses);
                } else {
                    if (converters.size() == 0) {
                        // And then interfaces
                        Class[] interfaces = sourceType.getInterfaces();
                        for (int i = 0; i < interfaces.length; i++) {
                            converters = getConverters(interfaces[i], triedClasses);
                            if (converters.size() > 0)
                                break;
                        }
                        
                        if (converters.size() == 0) {
                            // Try Object at last
                            converters = getConverters(Object.class, triedClasses);
                        }
                    }
                }
            }
        }
        
        return converters;
    }
    
    private static String toConversionChainString(Object source, List converters) {
        StringBuffer buf = new StringBuffer();
        Iterator it = converters.iterator();
        Converter c = (Converter) it.next();
        if (source.getClass() != c.getSourceType()) {
            buf.append(source.getClass().getName());
            buf.append(" -> ");
        }
        buf.append(c.getSourceType().getName());
        buf.append(" -> ");
        buf.append(c.getTargetType().getName());
        
        while (it.hasNext()) {
            c = (Converter) it.next();
            buf.append(" -> ");
            buf.append(c.getTargetType().getName());
        }
        
        return buf.toString();
    }
}
