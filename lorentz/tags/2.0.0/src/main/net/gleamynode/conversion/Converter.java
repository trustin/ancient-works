/*
 *   @(#) $Id: Converter.java 36 2005-08-05 12:27:31Z trustin $
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

import net.gleamynode.conversion.converter.CharsetToStringConverter;
import net.gleamynode.conversion.converter.ClassToStringConverter;
import net.gleamynode.conversion.converter.CollectionToStringConverter;
import net.gleamynode.conversion.converter.InetAddressToStringConverter;
import net.gleamynode.conversion.converter.InetSocketAddressToStringConverter;
import net.gleamynode.conversion.converter.MapToStringConverter;
import net.gleamynode.conversion.converter.ObjectToStringConverter;
import net.gleamynode.conversion.converter.PatternToStringConverter;
import net.gleamynode.conversion.converter.StringToBigDecimalConverter;
import net.gleamynode.conversion.converter.StringToBigIntegerConverter;
import net.gleamynode.conversion.converter.StringToBooleanConverter;
import net.gleamynode.conversion.converter.StringToByteConverter;
import net.gleamynode.conversion.converter.StringToCharacterConverter;
import net.gleamynode.conversion.converter.StringToCharsetConverter;
import net.gleamynode.conversion.converter.StringToClassConverter;
import net.gleamynode.conversion.converter.StringToCollectionConverter;
import net.gleamynode.conversion.converter.StringToCurrencyConverter;
import net.gleamynode.conversion.converter.StringToDoubleConverter;
import net.gleamynode.conversion.converter.StringToFileConverter;
import net.gleamynode.conversion.converter.StringToFloatConverter;
import net.gleamynode.conversion.converter.StringToInetAddressConverter;
import net.gleamynode.conversion.converter.StringToInetSocketAddressConverter;
import net.gleamynode.conversion.converter.StringToIntegerConverter;
import net.gleamynode.conversion.converter.StringToListConverter;
import net.gleamynode.conversion.converter.StringToLocaleConverter;
import net.gleamynode.conversion.converter.StringToLongConverter;
import net.gleamynode.conversion.converter.StringToMapConverter;
import net.gleamynode.conversion.converter.StringToPatternConverter;
import net.gleamynode.conversion.converter.StringToPropertiesConverter;
import net.gleamynode.conversion.converter.StringToSetConverter;
import net.gleamynode.conversion.converter.StringToShortConverter;
import net.gleamynode.conversion.converter.StringToTimeZoneConverter;
import net.gleamynode.conversion.converter.StringToUrlConverter;
import net.gleamynode.conversion.converter.TimeZoneToStringConverter;
import net.gleamynode.conversion.converter.optional.Init;
import net.gleamynode.conversion.support.ConverterRegistry;
import net.gleamynode.conversion.support.SmartConverter;

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
 * @version $Rev: 36 $, $Date: 2005-08-05 21:27:31 +0900 (Fri, 05 Aug 2005) $
 */
public abstract class Converter implements Comparable {
    private static final ConverterRegistry registry = new ConverterRegistry();
    private static final SmartConverter smartConverter = new SmartConverter(registry);

	static {
        // Initialize and register all core converters
        register(new StringToLocaleConverter());
        register(new CharsetToStringConverter());
        register(new ClassToStringConverter());
        register(new CollectionToStringConverter());
        register(new InetAddressToStringConverter());
        register(new InetSocketAddressToStringConverter());
        register(new MapToStringConverter());
        register(new ObjectToStringConverter());
        register(new PatternToStringConverter());
        register(new StringToBigDecimalConverter());
        register(new StringToBigIntegerConverter());
        register(new StringToBooleanConverter());
        register(new StringToByteConverter());
        register(new StringToCharacterConverter());
        register(new StringToCharsetConverter());
        register(new StringToClassConverter());
        register(new StringToCollectionConverter());
        register(new StringToCurrencyConverter());
        register(new StringToDoubleConverter());
        register(new StringToFileConverter());
        register(new StringToFloatConverter());
        register(new StringToInetAddressConverter());
        register(new StringToInetSocketAddressConverter());
        register(new StringToIntegerConverter());
        register(new StringToListConverter());
        register(new StringToLongConverter());
        register(new StringToMapConverter());
        register(new StringToPatternConverter());
        register(new StringToPropertiesConverter());
        register(new StringToSetConverter());
        register(new StringToShortConverter());
        register(new StringToTimeZoneConverter());
        register(new StringToUrlConverter());
        register(new TimeZoneToStringConverter());
        
        // Initialize optional converters
        new Init();
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
        return smartConverter.convert(source, targetType);
	}
    
    /**
     * Registers the specified converter to the global converter registry.
     * Registered converters are used when {@link #convert(Object, Class)}
     * is invoked. 
     */
    public static void register(Converter converter) {
        registry.register(converter);
    }
    
    /**
     * Deregisters the specified converter from the global converter registry.
     * Deregistered converters are not used anymore when
     * {@link #convert(Object, Class)} is invoked. 
     */
    public static void deregister(Converter converter) {
       registry.deregister(converter); 
    }
    
    /**
     * Deregisters the converter with the specified source and target type
     * from the global converter registry.  Deregistered converters are not
     * used anymore when {@link #convert(Object, Class)} is invoked. 
     */
    public static void deregister(Class sourceType, Class targetType) {
       registry.deregister(sourceType, targetType); 
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
	 * Implement this method to convert the specified object into the object
     * of the target type.
     *
	 * @throws Exception if failed to convert
	 */
	public abstract Object convert(Object o) throws Exception;
    
    public final boolean equals(Object o) {
        return this == o;
    }
    
    public final int hashCode() {
        return System.identityHashCode(this); 
    }
    
    public final int compareTo(Object o) {
        return this.hashCode() - System.identityHashCode(o);
    }
}
