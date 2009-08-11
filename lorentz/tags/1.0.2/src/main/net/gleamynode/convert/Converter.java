/*
 * @(#) $Id: Converter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import net.gleamynode.convert.impl.BigDecimalConverter;
import net.gleamynode.convert.impl.BigIntegerConverter;
import net.gleamynode.convert.impl.BooleanConverter;
import net.gleamynode.convert.impl.CharacterConverter;
import net.gleamynode.convert.impl.ClassConverter;
import net.gleamynode.convert.impl.DoubleConverter;
import net.gleamynode.convert.impl.FileConverter;
import net.gleamynode.convert.impl.FloatConverter;
import net.gleamynode.convert.impl.InetAddressConverter;
import net.gleamynode.convert.impl.IntegerConverter;
import net.gleamynode.convert.impl.LocaleConverter;
import net.gleamynode.convert.impl.LongConverter;
import net.gleamynode.convert.impl.PropertiesConverter;
import net.gleamynode.convert.impl.ShortConverter;
import net.gleamynode.convert.impl.StringCollectionConverter;
import net.gleamynode.convert.impl.StringConverter;
import net.gleamynode.convert.impl.StringListConverter;
import net.gleamynode.convert.impl.StringMapConverter;
import net.gleamynode.convert.impl.StringSetConverter;
import net.gleamynode.convert.impl.TimeZoneConverter;
import net.gleamynode.convert.impl.UrlConverter;
import net.gleamynode.convert.impl.optional.Init;

/**
 * Converts an object into a string and vice versa.
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public abstract class Converter {

	private static final Map converters = new HashMap();

	static {
		register(BigDecimal.class, new BigDecimalConverter());
		register(BigInteger.class, new BigIntegerConverter());
		register(boolean.class, new BooleanConverter());
		register(Boolean.class, new BooleanConverter());
		register(char.class, new CharacterConverter());
		register(Character.class, new CharacterConverter());
		register(Class.class, new ClassConverter());
		register(double.class, new DoubleConverter());
		register(Double.class, new DoubleConverter());
		register(File.class, new FileConverter());
		register(float.class, new FloatConverter());
		register(Float.class, new FloatConverter());
		register(int.class, new IntegerConverter());
		register(Integer.class, new IntegerConverter());
		register(Locale.class, new LocaleConverter());
		register(long.class, new LongConverter());
		register(Long.class, new LongConverter());
		register(short.class, new ShortConverter());
		register(Short.class, new ShortConverter());
		register(String.class, new StringConverter());
		register(Collection.class, new StringCollectionConverter());
		register(List.class, new StringListConverter());
		register(Set.class, new StringSetConverter());
		register(Map.class, new StringMapConverter());
		register(Properties.class, new PropertiesConverter());
		register(TimeZone.class, new TimeZoneConverter());
		register(URL.class, new UrlConverter());
		register(InetAddress.class, new InetAddressConverter());

		new Init();
	}

	/**
	 * Registers the specified converter that can convert the object of the
	 * specified <code>targetClass</code> and vice versa.
	 */
	public static void register(Class targetClass, Converter converter) {
		synchronized (converters) {
			converters.put(targetClass.getName(), converter);
		}
	}

	/**
	 * Deregisters the converter that converts the object of the specified
	 * <code>targetClass</code> and vice versa.
	 */
	public static void deregister(Class targetClass) {
		synchronized (converters) {
			converters.remove(targetClass.getName());
		}
	}

	/**
	 * Converts the specified object into a string via the appropriate
	 * converter. This method tries to get an approprite converter with the
	 * class of the specified object. It climbs up the class hierarchy and
	 * traverse the interface map to find the most appropriate one.
	 * 
	 * @throws ConversionException
	 *             if failed to convert
	 */
	public static String toString(Object object) throws ConversionException {
		if (object == null)
			return null;
		return getConverter(object.getClass()).convertToString(object);
	}

	/**
	 * Converts the specified string into an object via the appropriate
	 * converter. This method tries to get an approprite converter with the
	 * apecified target class. It climbs up the class hierarchy and traverse the
	 * interface map to find the most appropriate one.
	 * 
	 * @throws ConversionException
	 *             if failed to convert
	 */
	public static Object toObject(String string, Class targetClass)
			throws ConversionException {
		if (string == null)
			return null;
		return getConverter(targetClass).convertToObject(string);
	}

	private static Converter getConverter(Class type)
			throws ConversionException {
		Converter converter = getConverter(type, new HashSet());
		if (converter == null)
			throw new ConversionException("no coverter found for type: " + type);
		else
			return converter;

	}

	private static Converter getConverter(Class type, Set triedClassNames)
			throws ConversionException {
		Converter converter;

		String typeName = type.getName();
		if (triedClassNames.contains(typeName))
			return null;
		triedClassNames.add(typeName);

		converter = (Converter) converters.get(typeName);
		if (converter == null) {
			converter = getConverter(type, triedClassNames);
			if (converter != null)
				return converter;

			Class[] interfaces = type.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				converter = getConverter(interfaces[i], triedClassNames);
				if (converter != null)
					return converter;
			}

			return null;
		} else
			return converter;
	}

	protected Converter() {
	}

	/**
	 * Implement this method to convert the specified object into the string.
	 * @throws ConversionException if failed to convert
	 */
	public abstract String convertToString(Object o) throws ConversionException;

	/**
	 * Implement this method to convert the specified string into the object.
	 * @throws ConversionException if failed to convert
	 */
	public abstract Object convertToObject(String s) throws ConversionException;
}
