/*
 * $Header$
 * $Revision: 76 $
 * $Date: 2005-08-20 02:00:59 +0900 (Sat, 20 Aug 2005) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package net.gleamynode.conversion.support.discovery.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Vector;

import net.gleamynode.conversion.support.discovery.DiscoveryException;
import net.gleamynode.conversion.support.discovery.ResourceClass;
import net.gleamynode.conversion.support.discovery.ResourceClassIterator;
import net.gleamynode.conversion.support.discovery.ResourceNameIterator;
import net.gleamynode.conversion.support.discovery.resource.ClassLoaders;
import net.gleamynode.conversion.support.discovery.resource.classes.DiscoverClasses;
import net.gleamynode.conversion.support.discovery.resource.names.DiscoverServiceNames;


/**
 * <p>Discover class that implements a given service interface,
 * with discovery and configuration features similar to that employed
 * by standard Java APIs such as JAXP.
 * </p>
 * 
 * <p>In the context of this package, a service interface is defined by a
 * Service Provider Interface (SPI).  The SPI is expressed as a Java interface,
 * abstract class, or (base) class that defines an expected programming
 * interface.
 * </p>
 * 
 * <p>DiscoverClass provides the <code>find</code> methods for locating a
 * class that implements a service interface (SPI).  Each form of
 * <code>find</code> varies slightly, but they all perform the same basic
 * function.
 * 
 * The <code>DiscoverClass.find</code> methods proceed as follows:
 * </p>
 * <ul>
 *   <p><li>
 *   Get the name of an implementation class.  The name is the first
 *   non-null value obtained from the following resources:
 *   <ul>
 *     <li>
 *     The value of the (scoped) system property whose name is the same as
 *     the SPI's fully qualified class name (as given by SPI.class.getName()).
 *     The <code>ScopedProperties</code> class provides a way to bind
 *     properties by classloader, in a secure hierarchy similar in concept
 *     to the way classloader find class and resource files.
 *     See <code>ScopedProperties</code> for more details.
 *     <p>If the ScopedProperties are not set by users, then behaviour
 *     is equivalent to <code>System.getProperty()</code>.
 *     </p>
 *     </li>
 *     <p><li>
 *     The value of a <code>Properties properties</code> property, if provided
 *     as a parameter, whose name is the same as the SPI's fully qualifed class
 *     name (as given by SPI.class.getName()).
 *     </li></p>
 *     <p><li>
 *     The value obtained using the JDK1.3+ 'Service Provider' specification
 *     (http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html) to locate a
 *     service named <code>SPI.class.getName()</code>.  This is implemented
 *     internally, so there is not a dependency on JDK 1.3+.
 *     </li></p>
 *   </ul>
 *   </li></p>
 *   <p><li>
 *   If the name of the implementation class is non-null, load that class.
 *   The class loaded is the first class loaded by the following sequence
 *   of class loaders:
 *   <ul>
 *     <li>Thread Context Class Loader</li>
 *     <li>DiscoverSingleton's Caller's Class Loader</li>
 *     <li>SPI's Class Loader</li>
 *     <li>DiscoverSingleton's (this class or wrapper) Class Loader</li>
 *     <li>System Class Loader</li>
 *   </ul>
 *   An exception is thrown if the class cannot be loaded.
 *   </li></p>
 *   <p><li>
 *   If the name of the implementation class is null, AND the default
 *   implementation class name (<code>defaultImpl</code>) is null,
 *   then an exception is thrown.
 *   </li></p>
 *   <p><li>
 *   If the name of the implementation class is null, AND the default
 *   implementation class (<code>defaultImpl</code>) is non-null,
 *   then load the default implementation class.  The class loaded is the
 *   first class loaded by the following sequence of class loaders:
 *   <ul>
 *     <li>SPI's Class Loader</li>
 *     <li>DiscoverSingleton's (this class or wrapper) Class Loader</li>
 *     <li>System Class Loader</li>
 *   </ul>
 *   <p>
 *   This limits the scope in which the default class loader can be found
 *   to the SPI, DiscoverSingleton, and System class loaders.  The assumption here
 *   is that the default implementation is closely associated with the SPI
 *   or system, and is not defined in the user's application space.
 *   </p>
 *   <p>
 *   An exception is thrown if the class cannot be loaded.
 *   </p>
 *   </li></p>
 *   <p><li>
 *   Verify that the loaded class implements the SPI: an exception is thrown
 *   if the loaded class does not implement the SPI.
 *   </li></p>
 * </ul>
 * </p>
 *
 * <p><strong>IMPLEMENTATION NOTE</strong> - This implementation is modelled
 * after the SAXParserFactory and DocumentBuilderFactory implementations
 * (corresponding to the JAXP pluggability APIs) found in Apache Xerces.
 * </p>
 * 
 * @author Richard A. Sitze
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @version $Revision: 76 $ $Date: 2005-08-20 02:00:59 +0900 (Sat, 20 Aug 2005) $
 */
public class DiscoverClass {
    /**
     * Readable placeholder for a null value.
     */
    public static final DefaultClassHolder nullDefaultImpl = null;

    /**
     * Readable placeholder for a null value.
     */
    public static final PropertiesHolder nullProperties = null;
    
    
    private ClassLoaders classLoaders = null;


    /**
     * Create a class instance with dynamic environment
     * (thread context class loader is determined on each call).
     * 
     * Dynamically construct class loaders on each call.
     */    
    public DiscoverClass() {
        this(null);
    }

    /**
     * Create a class instance with dynamic environment
     * (thread context class loader is determined on each call).
     * 
     * Cache static list of class loaders for each call.
     */    
    public DiscoverClass(ClassLoaders classLoaders) {
        this.classLoaders = classLoaders;
    }
    
    
    public ClassLoaders getClassLoaders(Class spiClass) {
        return classLoaders;
    }


    /**
     * Find class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @return Class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded, or if
     *            the resulting class does not implement (or extend) the SPI.
     */
    public Class find(Class spiClass)
        throws DiscoveryException
    {
        return find(getClassLoaders(spiClass),
                    new SPInterface(spiClass),
                    nullProperties,
                    nullDefaultImpl);
    }

    /**
     * Find class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param properties Used to determine name of SPI implementation.
     * 
     * @return Class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded, or if
     *            the resulting class does not implement (or extend) the SPI.
     */
    public Class find(Class spiClass, Properties properties)
        throws DiscoveryException
    {
        return find(getClassLoaders(spiClass),
                    new SPInterface(spiClass),
                    new PropertiesHolder(properties),
                    nullDefaultImpl);
    }

    /**
     * Find class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param defaultImpl Default implementation name.
     * 
     * @return Class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded, or if
     *            the resulting class does not implement (or extend) the SPI.
     */
    public Class find(Class spiClass, String defaultImpl)
        throws DiscoveryException
    {
        return find(getClassLoaders(spiClass),
                    new SPInterface(spiClass),
                    nullProperties,
                    new DefaultClassHolder(defaultImpl));
    }

    /**
     * Find class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param properties Used to determine name of SPI implementation,.
     * 
     * @param defaultImpl Default implementation class.
     * 
     * @return Class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded, or if
     *            the resulting class does not implement (or extend) the SPI.
     */
    public Class find(Class spiClass, Properties properties, String defaultImpl)
        throws DiscoveryException
    {
        return find(getClassLoaders(spiClass),
                    new SPInterface(spiClass),
                    new PropertiesHolder(properties),
                    new DefaultClassHolder(defaultImpl));
    }

    /**
     * Find class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param properties Used to determine name of SPI implementation,.
     * 
     * @param defaultImpl Default implementation class.
     * 
     * @return Class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded, or if
     *            the resulting class does not implement (or extend) the SPI.
     */
    public Class find(Class spiClass, String propertiesFileName, String defaultImpl)
        throws DiscoveryException
    {
        return find(getClassLoaders(spiClass),
                    new SPInterface(spiClass),
                    new PropertiesHolder(propertiesFileName),
                    new DefaultClassHolder(defaultImpl));
    }

    /**
     * Find class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param properties Used to determine name of SPI implementation,.
     * 
     * @param defaultImpl Default implementation class.
     * 
     * @return Class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded, or if
     *            the resulting class does not implement (or extend) the SPI.
     */
    public static Class find(ClassLoaders loaders,
                             SPInterface spi,
                             PropertiesHolder properties,
                             DefaultClassHolder defaultImpl)
        throws DiscoveryException
    {
        if (loaders == null) {
            loaders = ClassLoaders.getLibLoaders(spi.getSPClass(),
                                                 DiscoverClass.class,
                                                 true);
        }
        
        Properties props = (properties == null)
                           ? null
                           : properties.getProperties(spi, loaders);
        
        String[] classNames = discoverClassNames(spi, props);
        
        if (classNames.length > 0) {
            DiscoverClasses classDiscovery = new DiscoverClasses(loaders);
            
            ResourceClassIterator classes =
                classDiscovery.findResourceClasses(classNames[0]);
            
            // If it's set as a property.. it had better be there!
            if (classes.hasNext()) {
                ResourceClass info = classes.nextResourceClass();
                try {
                    return info.loadClass();
                } catch (Exception e) {
                    // ignore
                }
            }
        } else {
            ResourceNameIterator classIter =
                (new DiscoverServiceNames(loaders)).findResourceNames(spi.getSPName());

            ResourceClassIterator classes =
                (new DiscoverClasses(loaders)).findResourceClasses(classIter);
                
            
            if (!classes.hasNext()  &&  defaultImpl != null) {
                return defaultImpl.getDefaultClass(spi, loaders);
            }
            
            // Services we iterate through until we find one that loads..
            while (classes.hasNext()) {
                ResourceClass info = classes.nextResourceClass();
                try {
                    return info.loadClass();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        
        throw new DiscoveryException("No implementation defined for " + spi.getSPName());
        // return null;
    }
    
    /**
     * Create new instance of class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @return Instance of a class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded and
     *            instantiated, or if the resulting class does not implement
     *            (or extend) the SPI.
     */
    public Object newInstance(Class spiClass)
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        return newInstance(getClassLoaders(spiClass),
                           new SPInterface(spiClass),
                           nullProperties,
                           nullDefaultImpl);
    }

    /**
     * Create new instance of class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param properties Used to determine name of SPI implementation,
     *                   and passed to implementation.init() method if
     *                   implementation implements Service interface.
     * 
     * @return Instance of a class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded and
     *            instantiated, or if the resulting class does not implement
     *            (or extend) the SPI.
     */
    public Object newInstance(Class spiClass, Properties properties)
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        return newInstance(getClassLoaders(spiClass),
                           new SPInterface(spiClass),
                           new PropertiesHolder(properties),
                           nullDefaultImpl);
    }

    /**
     * Create new instance of class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param defaultImpl Default implementation.
     * 
     * @return Instance of a class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded and
     *            instantiated, or if the resulting class does not implement
     *            (or extend) the SPI.
     */
    public Object newInstance(Class spiClass, String defaultImpl)
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        return newInstance(getClassLoaders(spiClass),
                           new SPInterface(spiClass),
                           nullProperties,
                           new DefaultClassHolder(defaultImpl));
    }

    /**
     * Create new instance of class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param properties Used to determine name of SPI implementation,
     *                   and passed to implementation.init() method if
     *                   implementation implements Service interface.
     * 
     * @param defaultImpl Default implementation.
     * 
     * @return Instance of a class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded and
     *            instantiated, or if the resulting class does not implement
     *            (or extend) the SPI.
     */
    public Object newInstance(Class spiClass, Properties properties, String defaultImpl)
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        return newInstance(getClassLoaders(spiClass),
                           new SPInterface(spiClass),
                           new PropertiesHolder(properties),
                           new DefaultClassHolder(defaultImpl));
    }

    /**
     * Create new instance of class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param properties Used to determine name of SPI implementation,
     *                   and passed to implementation.init() method if
     *                   implementation implements Service interface.
     * 
     * @param defaultImpl Default implementation.
     * 
     * @return Instance of a class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded and
     *            instantiated, or if the resulting class does not implement
     *            (or extend) the SPI.
     */
    public Object newInstance(Class spiClass, String propertiesFileName, String defaultImpl)
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        return newInstance(getClassLoaders(spiClass),
                           new SPInterface(spiClass),
                           new PropertiesHolder(propertiesFileName),
                           new DefaultClassHolder(defaultImpl));
    }

    /**
     * Create new instance of class implementing SPI.
     * 
     * @param spiClass Service Provider Interface Class.
     * 
     * @param properties Used to determine name of SPI implementation,
     *                   and passed to implementation.init() method if
     *                   implementation implements Service interface.
     * 
     * @param defaultImpl Default implementation.
     * 
     * @return Instance of a class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded and
     *            instantiated, or if the resulting class does not implement
     *            (or extend) the SPI.
     */
    public static Object newInstance(ClassLoaders loaders,
                                     SPInterface spi,
                                     PropertiesHolder properties,
                                     DefaultClassHolder defaultImpl)
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        return spi.newInstance(find(loaders, spi, properties, defaultImpl));
    }

    /**
     * <p>Discover names of SPI implementation Classes from properties.
     * The names are the non-null values, in order, obtained from the following
     * resources:
     *   <ul>
     *     <li>ManagedProperty.getProperty(SPI.class.getName());</li>
     *     <li>properties.getProperty(SPI.class.getName());</li>
     *   </ul>
     * 
     * @param properties Properties that may define the implementation
     *                   class name(s).
     * 
     * @return String[] Name of classes implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found.
     */
    public static String[] discoverClassNames(SPInterface spi,
                                              Properties properties)
    {
        Vector names = new Vector();
        
        String spiName = spi.getSPName();
        String propertyName = spi.getPropertyName();

        boolean includeAltProperty = !spiName.equals(propertyName);
        
        // Try the (managed) system property spiName
        String className = getManagedProperty(spiName);
        if (className != null) names.addElement(className);
        
        if (includeAltProperty) {
            // Try the (managed) system property propertyName
            className = getManagedProperty(propertyName);
            if (className != null) names.addElement(className);
        }

        if (properties != null) {
            // Try the properties parameter spiName
            className = properties.getProperty(spiName);
            if (className != null) names.addElement(className);

            if (includeAltProperty) {
                // Try the properties parameter propertyName
                className = properties.getProperty(propertyName);
                if (className != null) names.addElement(className);
            }
        }

        String[] results = new String[names.size()];
        names.copyInto(results);        

        return results;
    }


    /**
     * Load the class whose name is given by the value of a (Managed)
     * System Property.
     * 
     * @see ManagedProperties
     * 
     * @param attribute the name of the system property whose value is
     *        the name of the class to load.
     */
    public static String getManagedProperty(String propertyName) {
        String value;
        try {
            value = ManagedProperties.getProperty(propertyName);
        } catch (SecurityException e) {
            value = null;
        }
        return value;
    }
}
