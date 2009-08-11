/*
 *   @(#) $Id: ConversionEngine.java 129 2005-11-14 09:35:57Z trustin $
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.ConversionPath;
import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.ConverterContext;
import net.gleamynode.conversion.NoConversionPathException;

public class ConversionEngine {
    private static final Collection<Converter> EMPTY_COLLECTION = new ArrayList<Converter>(); 
    private final ConverterRegistry registry;
    private final ConversionPathCache conversionCache;
    private final ConversionPathCache copyCache;

    public ConversionEngine(ConverterRegistry registry, ConversionPathCache conversionCache, ConversionPathCache copyCache ) {
        this.registry = registry;
        this.conversionCache = conversionCache;
        this.copyCache = copyCache;
    }
    
    public <T> T convert(Object source, Class<T> targetType, ConverterContext ctx) {
        if (source == null)
            return null;
        
        Class sourceType = source.getClass();
        if (targetType.isAssignableFrom(sourceType)) {
            // Return back the source object if it is immutable.
            if (registry.isImmutable(sourceType)) {
                return targetType.cast(source);
            } else {
                try
                {
                    Method cloneMethod = sourceType.getMethod("clone", (Class[]) null);
                    return targetType.cast(cloneMethod.invoke(source, (Object[]) null));
                }
                catch( Exception e )
                {
                    // Abandon cloning
                }
            }
        }
        
        ConversionPath path = conversionCache.get(new ConversionKey(sourceType, targetType));
        if (path != null) {
            return targetType.cast(path.convert(source, ctx));
        }
        
        ((ConversionPathImpl) ctx.getPath()).clear();
        T target = convert0(source, sourceType, targetType, ctx, new HashSet<Class>());
        if (target == null)
            throw new NoConversionPathException();

        return target;
    }
    
    private <T> T convert0(Object source, Class initialSourceType, Class<T> targetType, ConverterContext ctx, Set<Class> triedClasses) {
        Collection<Converter> converters = getConverters(source.getClass(), targetType, false, new HashSet<Class>());
        if (converters.size() == 0) {
            return null;
        }
        
        Iterator<Converter> it = converters.iterator();
        
        while (it.hasNext()) {
            Converter c = it.next();
            // Prevent unnecessary recursion
            if (triedClasses.contains(c.getTargetType()) ||
                    (!targetType.isAssignableFrom(initialSourceType) &&
                            ((Class<?>) c.getTargetType()).isAssignableFrom(initialSourceType))) {
                continue;
            }
            
            boolean terminal = targetType.isAssignableFrom(c.getTargetType());

            // Skip if the converter's target type is intermediary 
            // and it is in exclusion list.
            if (!terminal &&
                    ctx.getExclusions().contains(c.getTargetType())) {
                continue;
            }
            
            ((ConversionPathImpl) ctx.getPath()).add(c);
            try {
                Object result = c.doConversion(source, ctx);
                if (terminal) {
                    conversionCache.add(
                            new ConversionKey(initialSourceType, targetType),
                            ctx.getPath());
                    return targetType.cast(result);
                } else {
                    triedClasses.add(c.getSourceType());
                    try {
                        result = convert0(result, initialSourceType, targetType, ctx, triedClasses);
                        if (result != null)
                            return targetType.cast(result);
                    } finally {
                        triedClasses.remove(c.getSourceType());
                    }
                }
            } catch( Throwable t ) {
            }
            ((ConversionPathImpl) ctx.getPath()).remove();
        }
        
        return null;
    }
    
    public void copy(Object source, Object target, ConverterContext ctx) {
        if (source == null)
            throw new NullPointerException("source");
        
        if (target == null) {
            throw new NullPointerException("target");
        }
        
        ConversionPath path = copyCache.get(new ConversionKey(source.getClass(), target.getClass()));
        if (path != null) {
            path.copy(source, target, ctx);
        }
        else
        {
            ((ConversionPathImpl) ctx.getPath()).clear();
            boolean done = copy0(source, target, source.getClass(), ctx, new HashSet<Class>());
            if (!done)
                throw new NoConversionPathException();
        }
    }
    
    private boolean copy0(Object source, Object target, Class initialSourceType, ConverterContext ctx, Set<Class> triedClasses) {
        Collection<Converter> converters = getConverters(source.getClass(), target.getClass(), true, new HashSet<Class>());
        if (converters.size() == 0) {
            return false;
        }
        
        Iterator<Converter> it = converters.iterator();
        
        while (it.hasNext()) {
            Converter c = it.next();
            
            // Prevent unnecessary recursion
            if (triedClasses.contains(c.getTargetType()) ||
                    (!target.getClass().isAssignableFrom(initialSourceType) &&
                            ((Class<?>) c.getTargetType()).isAssignableFrom(initialSourceType))) {
                continue;
            }
            
            boolean terminal = target.getClass().isAssignableFrom(c.getTargetType());
            
            // Skip if the converter's target type is intermediary 
            // and it is in exclusion list.
            if (!terminal &&
                    ctx.getExclusions().contains(c.getTargetType())) {
                continue;
            }
            
            ((ConversionPathImpl) ctx.getPath()).add(c);
            try {
                Object result;
                if (terminal) {
                    c.doCopy(source, target, ctx);
                    copyCache.add(
                            new ConversionKey(initialSourceType, target.getClass()),
                            ctx.getPath());
                    return true;
                } else {
                    result = c.doConversion(source, ctx);
                }
                
                triedClasses.add(c.getSourceType());
                try {
                    boolean done = copy0(result, target, initialSourceType, ctx, triedClasses);
                    if (done)
                        return true;
                } finally {
                    triedClasses.remove(c.getSourceType());
                }
            } catch( Throwable t ) {
            }
            ((ConversionPathImpl) ctx.getPath()).remove();
        }
        
        return false;
    }

    public Collection<Converter> getConverters(Class sourceType, final Class targetType, final boolean copy, Set<Class> triedClasses)
            throws ConversionException {
        if (triedClasses.contains(sourceType))
            return EMPTY_COLLECTION;
        triedClasses.add(sourceType);

        List<Converter> result = new ArrayList<Converter>();
        result.addAll(registry.getConverters(sourceType).values());
        if (!sourceType.isPrimitive()) {
            // Find a superclass first (except Object)
            Class superClass = sourceType.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                result.addAll(getConverters(superClass, targetType, copy, triedClasses));
            }

            // And then interfaces
            Class[] interfaces = sourceType.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                result.addAll(getConverters(interfaces[i], targetType, copy, triedClasses));
            }
            
            // Try Object lastly
            result.addAll(getConverters(Object.class, targetType, copy, triedClasses));
        }

        // Sort the result by possibility of choosing the shortest path ASAP.
        // Please note that Collections.sort uses stable sort algorithm, so
        // we will retain the original order unless we can't figure out the
        // exact order of the candidate converters.
        Collections.sort(result, new Comparator<Converter>() {
            public int compare( Converter a, Converter b )
            {
                boolean isATerminal = ((Class<?>) a.getTargetType()).isAssignableFrom(targetType);
                boolean isBTerminal = ((Class<?>) b.getTargetType()).isAssignableFrom(targetType);
                boolean doesASupportConversion = registry.supportsConversion(a);
                boolean doesBSupportConversion = registry.supportsConversion(b);
                boolean doesASupportCopy = registry.supportsCopy(a);
                boolean doesBSupportCopy = registry.supportsCopy(b);
                
                if (isATerminal) {
                    if (isBTerminal) {
                        if (copy) {
                            if (doesASupportCopy) {
                                if (doesBSupportCopy) {
                                    return 0;
                                } else {
                                    return -1;
                                }
                            } else {
                                if (doesBSupportCopy) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        } else { // conversion
                            if (doesASupportConversion) {
                                if (doesBSupportConversion) {
                                    return 0;
                                } else {
                                    return -1;
                                }
                            } else {
                                if (doesBSupportConversion) {
                                    return 1; 
                                } else {
                                    return 0;
                                }
                            }
                        }
                    } else {
                        if (copy) {
                            if (doesASupportCopy) {
                                return -1;
                            } else {
                                return 0;
                            }
                        } else {
                            if (doesASupportConversion) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    }
                } else { // A is not a terminal converter.
                    if (isBTerminal) {
                        if (copy) {
                            if (doesBSupportCopy) {
                                return 1;
                            } else {
                                return 0;
                            }
                        } else { // conversion
                            if (doesBSupportConversion) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    } else {
                        return 0;
                    }
                }
            }
        });
        
        return result;
    }
    
    /*
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
    */
}
