/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 */

package net.gleamynode.conversion.support.discovery.tools;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import net.gleamynode.conversion.support.discovery.jdk.JDKHooks;



/**
 * <p>This class may disappear in the future, or be moved to another project..
 * </p>
 * 
 * <p>Extend the concept of System properties to a hierarchical scheme
 * based around class loaders.  System properties are global in nature,
 * so using them easily violates sound architectural and design principles
 * for maintaining separation between components and runtime environments.
 * Nevertheless, there is a need for properties broader in scope than
 * class or class instance scope.
 * </p>
 * 
 * <p>This class is one solution.
 * </p>
 * 
 * <p>Manage properties according to a secure
 * scheme similar to that used by classloaders:
 * <ul>
 *   <li><code>ClassLoader</code>s are organized in a tree hierarchy.</li>
 *   <li>each <code>ClassLoader</code> has a reference
 *       to a parent <code>ClassLoader</code>.</li>
 *   <li>the root of the tree is the bootstrap <code>ClassLoader</code>er.</li>
 *   <li>the youngest decendent is the thread context class loader.</li>
 *   <li>properties are bound to a <code>ClassLoader</code> instance
 *   <ul>
 *     <li><i>non-default</i> properties bound to a parent <code>ClassLoader</code>
 *         instance take precedence over all properties of the same name bound
 *         to any decendent.
 *         Just to confuse the issue, this is the default case.</li>
 *     <li><i>default</i> properties bound to a parent <code>ClassLoader</code>
 *         instance may be overriden by (default or non-default) properties of
 *         the same name bound to any decendent.
 *         </li>
 *   </ul>
 *   </li>
 *   <li>System properties take precedence over all other properties</li>
 * </ul>
 * </p>
 * 
 * <p>This is not a perfect solution, as it is possible that
 * different <code>ClassLoader</code>s load different instances of
 * <code>ScopedProperties</code>.  The 'higher' this class is loaded
 * within the <code>ClassLoader</code> hierarchy, the more usefull
 * it will be.
 * </p>
 * 
 * @author Richard A. Sitze
 */
public class ManagedProperties {
    /**
     * Cache of Properties, keyed by (thread-context) class loaders.
     * Use <code>HashMap</code> because it allows 'null' keys, which
     * allows us to account for the (null) bootstrap classloader.
     */
    private static final HashMap propertiesCache = new HashMap();
    
                                                        
    /**
     * Get value for property bound to the current thread context class loader.
     * 
     * @param property property name.
     * @return property value if found, otherwise default.
     */
    public static String getProperty(String propertyName) {
        return getProperty(getThreadContextClassLoader(), propertyName);
    }
    
    /**
     * Get value for property bound to the current thread context class loader.
     * If not found, then return default.
     * 
     * @param property property name.
     * @param dephault default value.
     * @return property value if found, otherwise default.
     */
    public static String getProperty(String propertyName, String dephault) {
        return getProperty(getThreadContextClassLoader(), propertyName, dephault);
    }
    
    /**
     * Get value for property bound to the class loader.
     * 
     * @param classLoader
     * @param property property name.
     * @return property value if found, otherwise default.
     */
    public static String getProperty(ClassLoader classLoader, String propertyName) {
        String value = System.getProperty(propertyName);
        if (value == null) {
            Value val = getValueProperty(classLoader, propertyName);
            if (val != null) {
                value = val.value;
            }
        }
        return value;
    }
    
    /**
     * Get value for property bound to the class loader.
     * If not found, then return default.
     * 
     * @param classLoader
     * @param property property name.
     * @param dephault default value.
     * @return property value if found, otherwise default.
     */
    public static String getProperty(ClassLoader classLoader, String propertyName, String dephault) {
        String value = getProperty(classLoader, propertyName);
        return (value == null) ? dephault : value;
    }

    /**
     * Set value for property bound to the current thread context class loader.
     * @param property property name
     * @param value property value (non-default)  If null, remove the property.
     */
    public static void setProperty(String propertyName, String value) {
        setProperty(propertyName, value, false);
    }
    
    /**
     * Set value for property bound to the current thread context class loader.
     * @param property property name
     * @param value property value.  If null, remove the property.
     * @param isDefault determines if property is default or not.
     *        A non-default property cannot be overriden.
     *        A default property can be overriden by a property
     *        (default or non-default) of the same name bound to
     *        a decendent class loader.
     */
    public static void setProperty(String propertyName, String value, boolean isDefault) {
        if (propertyName != null) {
            synchronized (propertiesCache) {
                ClassLoader classLoader = getThreadContextClassLoader();
                HashMap properties = (HashMap)propertiesCache.get(classLoader);
                
                if (value == null) {
                    properties.remove(propertyName);
                } else {
                    if (properties == null) {
                        properties = new HashMap();
                        propertiesCache.put(classLoader, properties);
                    }
                
                    properties.put(propertyName, new Value(value, isDefault));
                }
            }
        }
    }
    
    /**
     * Set property values for <code>Properties</code> bound to the
     * current thread context class loader.
     * 
     * @param newProperties name/value pairs to be bound
     */
    public static void setProperties(Map newProperties) {
        setProperties(newProperties, false);
    }
    
    
    /**
     * Set property values for <code>Properties</code> bound to the
     * current thread context class loader.
     * 
     * @param newProperties name/value pairs to be bound
     * @param isDefault determines if properties are default or not.
     *        A non-default property cannot be overriden.
     *        A default property can be overriden by a property
     *        (default or non-default) of the same name bound to
     *        a decendent class loader.
     */
    public static void setProperties(Map newProperties, boolean isDefault) {
        java.util.Iterator it = newProperties.entrySet().iterator();

        /**
         * Each entry must be mapped to a Property.
         * 'setProperty' does this for us.
         */
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            setProperty( String.valueOf(entry.getKey()),
                         String.valueOf(entry.getValue()),
                         isDefault);
        }
    }

    
    /**
     * Return list of all property names.  This is an expensive
     * operation: ON EACH CALL it walks through all property lists 
     * associated with the current context class loader upto
     * and including the bootstrap class loader.
     */
    public static Enumeration propertyNames() {
        Hashtable allProps = new Hashtable();

        ClassLoader classLoader = getThreadContextClassLoader();

        /**
         * Order doesn't matter, we are only going to use
         * the set of all keys...
         */
        while (true) {
            HashMap properties = null;

            synchronized (propertiesCache) {
                properties = (HashMap)propertiesCache.get(classLoader);
            }

            if (properties != null) {
                allProps.putAll(properties);
            }

            if (classLoader == null) break;
            
            classLoader = getParent(classLoader);
        }
        
        return allProps.keys();
    }
    
    /**
     * This is an expensive operation.
     * ON EACH CALL it walks through all property lists 
     * associated with the current context class loader upto
     * and including the bootstrap class loader.
     * 
     * @return Returns a <code>java.util.Properties</code> instance
     * that is equivalent to the current state of the scoped
     * properties, in that getProperty() will return the same value.
     * However, this is a copy, so setProperty on the
     * returned value will not effect the scoped properties.
     */
    public static Properties getProperties() {
        Properties p = new Properties();
        
        Enumeration names = propertyNames();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            p.put(name, getProperty(name));
        }
        
        return p;
    }


    /***************** INTERNAL IMPLEMENTATION *****************/

    private static class Value {
        final String value;
        final boolean isDefault;
        
        Value(String value, boolean isDefault) {
            this.value = value;
            this.isDefault = isDefault;
        }
    }

    /**
     * Get value for properties bound to the class loader.
     * Explore up the tree first, as higher-level class
     * loaders take precedence over lower-level class loaders.
     */
    private static final Value getValueProperty(ClassLoader classLoader, String propertyName) {
        Value value = null;

        if (propertyName != null) {
            /**
             * If classLoader isn't bootstrap loader (==null),
             * then get up-tree value.
             */
            if (classLoader != null) {
                value = getValueProperty(getParent(classLoader), propertyName);
            }
            
            if (value == null  ||  value.isDefault) {
                synchronized (propertiesCache) {
                    HashMap properties = (HashMap)propertiesCache.get(classLoader);
                        
                    if (properties != null) {
                        Value altValue = (Value)properties.get(propertyName);
                        
                        // set value only if override exists..
                        // otherwise pass default (or null) on..
                        if (altValue != null)
                            value = altValue;
                    }
                }
            }
        }
        
        return value;
    }
    
    private static final ClassLoader getThreadContextClassLoader() {
        return JDKHooks.getJDKHooks().getThreadContextClassLoader();
    }

    private static final ClassLoader getParent(final ClassLoader classLoader) {
        return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return classLoader.getParent();
                    }
                });
    }
}
