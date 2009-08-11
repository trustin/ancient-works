/*
 *   @(#) $Id: Converter.java 129 2005-11-14 09:35:57Z trustin $
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

import java.util.Enumeration;
import java.util.Iterator;

import net.gleamynode.conversion.support.ConversionEngine;
import net.gleamynode.conversion.support.ConversionPathCache;
import net.gleamynode.conversion.support.ConverterRegistry;
import net.gleamynode.conversion.support.discovery.tools.Service;

/**
 * Converts an object into another one.
 * 
 * <h2>How To Convert An Object Into Another</h2>
 * <pre>
 * BigInteger bi = ...;
 * Integer i = (Integer) Converter.convert(bi, Integer.class);
 * </pre>
 * 
 * <h2>How To Add A New Converter</h2>
 * First, create a class that extends {@link Converter}.
 * <pre>
 * public MyConverter extends Converter {
 *     public MyConverter() {
 *         super(FromType.class, ToType.class);
 *     }
 *     
 *     public Object convert(Object o) throws Exception {
 *         // Insert your conversion code here.
 *         ...
 *         return result;
 *     }
 * }
 * </pre>
 * To register the converter you've created:
 * <pre>
 * Converter.register(new MyConverter());
 * </pre>
 * Now you can convert between your custom types:
 * <pre>
 * FromType f = ...;
 * ToType t = (ToType) Converter.convert(f, ToType.class);
 * </pre>
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 129 $, $Date: 2005-11-14 18:35:57 +0900 (Mon, 14 Nov 2005) $
 */
public abstract class Converter implements Comparable {
    private static final ConversionPathCache conversionCache = new ConversionPathCache();
    private static final ConversionPathCache copyCache = new ConversionPathCache();
    private static final ConverterRegistry registry = new ConverterRegistry(conversionCache, copyCache);
    private static final ConversionEngine engine = new ConversionEngine(registry, conversionCache, copyCache);
    
    private static ConverterContext defaultContext = new ConverterContext();

	static {
        registerServiceProviders();
	}

	/**
	 * Converts the specified source object into an object of the specified target
     * type via an appropriate converter. This method tries to get an appropriate
     * converter with the specified target type. It climbs up the class hierarchy
     * and traverse the interface map to find the most appropriate one.
	 * 
	 * @throws ConversionException if failed to convert
	 */
	public static Object convert(Object source, Class targetType)
			throws ConversionException {
        return convert(source, targetType, null);
	}
    
    /**
     * Converts the specified source object into an object of the specified target
     * type via an appropriate converter. This method tries to get an appropriate
     * converter with the specified target type. It climbs up the class hierarchy
     * and traverse the interface map to find the most appropriate one.
     * 
     * @param ctx a context object to be passed to {@link Converter#doConversion(Object, ConverterContext)} implementations
     * @throws ConversionException if failed to convert
     */
    public static Object convert(Object source, Class targetType, ConverterContext ctx)
            throws ConversionException {
        if (ctx == null) {
            ctx = (ConverterContext) defaultContext.clone();
        }
        return engine.convert(source, targetType, ctx);
    }
    
    /**
     * Copies the specified source object into the specified target object via
     * an appropriate converter.  This method tries to get an appropriate
     * converter with the type of the specified target object. It climbs up the
     * class hierarchy and traverse the interface map to find the most appropriate
     * one.
     * 
     * @throws ConversionException if failed to copy
     */
    public static void copy(Object source, Object target)
            throws ConversionException {
        copy(source, target, null);
    }
    
    /**
     * Copies the specified source object into the specified target object via
     * an appropriate converter.  This method tries to get an appropriate
     * converter with the type of the specified target object. It climbs up the
     * class hierarchy and traverse the interface map to find the most appropriate
     * one.
     * 
     * @param ctx a context object to be passed to {@link Converter#doCopy(Object, Object, ConverterContext)} implementations.
     * @throws ConversionException if failed to copy
     */
    public static void copy(Object source, Object target, ConverterContext ctx)
            throws ConversionException {
        if (ctx == null) {
            ctx = (ConverterContext) defaultContext.clone();
        }
        engine.copy(source, target, ctx);
    }
    
    /**
     * Does the same job with {@link #convert(Object, Class)} except that it
     * returns <tt>null</tt> instead of throwing a {@link ConversionException}.
     * 
     * @return <tt>null</tt> if failed to convert
     */
    public static Object safeConvert(Object source, Class targetType) {
        try {
            return convert(source, targetType);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Does the same job with {@link #convert(Object, Class, ConverterContext)}
     * except that it returns <tt>null</tt> instead of throwing a
     * {@link ConversionException}.
     * 
     * @return <tt>null</tt> if failed to convert
     */
    public static Object safeConvert(Object source, Class targetType, ConverterContext ctx) {
        try {
            return convert(source, targetType, ctx);
        } catch (Throwable t) {
            return null;
        }
    }
    
    /**
     * Returns the default context which is used when you didn't specify
     * {@link ConverterContext}.
     */
    public static ConverterContext getDefaultContext() {
        return (ConverterContext) defaultContext.clone();
    }
    
    /**
     * Sets the default context which is used when you didn't specify
     * {@link ConverterContext}.
     * 
     * @param defaultContext <tt>null</tt> to reset to the system default
     */
    public static void setDefaultContext(ConverterContext defaultContext) {
        if (defaultContext == null) {
            defaultContext = new ConverterContext();
        }
        Converter.defaultContext = (ConverterContext) defaultContext.clone();
    }

    /**
     * Registers the specified converter to the global converter registry.
     */
    public static void register(Converter converter) {
        registry.register(converter);
    }
    
    /**
     * Registers all converters which the specified {@link ConverterPack}
     * contains to the global converter registry. 
     */
    public static void register(ConverterPack converterPack) {
        Iterator i = converterPack.newConverters();
        while (i.hasNext()) {
            Converter c = (Converter) i.next();
            register(c);
        }
    }
    
    /**
     * Deregisters the specified converter from the global converter registry.
     */
    public static void deregister(Converter converter) {
       registry.deregister(converter);
    }
    
    /**
     * Deregisters all converters which the specified {@link ConverterPack}
     * contains from the global converter registry. 
     */
    public static void deregister(ConverterPack converterPack) {
        Iterator i = converterPack.newConverters();
        while (i.hasNext()) {
            Converter c = (Converter) i.next();
            deregister(c);
        }
    }

    /**
     * Registers all {@link Converter}s and {@link ConverterPack}s
     * found using SPI.
     */
    private static void registerServiceProviders() {
        for (Enumeration e = Service.providers(ConverterPack.class);
             e.hasMoreElements();) {
            ConverterPack p = (ConverterPack) e.nextElement();
            register(p);
        }
        for (Enumeration e = Service.providers(Converter.class);
             e.hasMoreElements();) {
            Converter c = (Converter) e.nextElement();
            register(c);
        }
    }
    
    private final Class sourceType;
    private final Class targetType;

    /**
     * Creates a new converter which converts an object of <tt>sourceType</tt>
     * into an object of <tt>targetType</tt>.  A converter is registered to
     * global converter registry automatically when this constructor is invoked.
     */
	protected Converter(Class sourceType, Class targetType) {
        if (sourceType == null)
            throw new NullPointerException("sourceType");
        if (targetType == null)
            throw new NullPointerException("targetType");
        
        if (sourceType.getName().equals(targetType.getName()))
            throw new IllegalArgumentException("sourceType and targetType is identical: " + sourceType);
        
        this.sourceType = sourceType;
        this.targetType = targetType;
	}
    
    /**
     * Returns the type of the object this converter can convert into.
     */
    public final Class getSourceType() {
        return sourceType;
    }
    
    /**
     * Returns the type of the object this converter can convert from.
     */
    public final Class getTargetType() {
        return targetType;
    }

	/**
	 * Override this method to convert the specified object into 
	 * newly-created object of the target type.
     *
	 * @throws Exception if failed to convert
	 */
	public Object doConversion(Object o, ConverterContext ctx) throws Exception {
        throw new UnsupportedOperationException( 
                "Conversion from " + o + " (" + sourceType.getName() + ") to " +
                targetType.getName() + " is unsupported." );
	}

	/**
	 * Override this method to copy the specified <tt>source</tt> object
     * into <tt>target</tt>.
     * 
	 * @throws Exception if failed to copy
	 */
	public void doCopy(Object source, Object target, ConverterContext ctx) throws Exception {
		throw new UnsupportedOperationException( 
				"Copy from " + source + " (" + sourceType.getName() + ") to " +
                target + " (" + targetType.getName() + ") is unsupported." );
	}
	
    public final boolean equals(Object o) {
        // This makes it easy to manage converters with HashSet.
        return this == o;
    }
    
    public final int hashCode() {
        // This makes it easy to manage converters with HashSet.
        return System.identityHashCode(this); 
    }
    
    public final int compareTo(Object o) {
        // This makes it easy to manage converters with HashSet.
        return this.hashCode() - System.identityHashCode(o);
    }
}
