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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.gleamynode.conversion.support.discovery.DiscoveryException;
import net.gleamynode.conversion.support.discovery.Resource;
import net.gleamynode.conversion.support.discovery.ResourceIterator;
import net.gleamynode.conversion.support.discovery.resource.ClassLoaders;
import net.gleamynode.conversion.support.discovery.resource.DiscoverResources;


/**
 * Mechanisms to locate and load a class.
 * The load methods locate a class only.
 * The find methods locate a class and verify that the
 * class implements an given interface or extends a given class.
 * 
 * @author Richard A. Sitze
 * @author Craig R. McClanahan
 * @author Costin Manolache
 */
public class ResourceUtils {
    /**
     * Get package name.
     * Not all class loaders 'keep' package information,
     * in which case Class.getPackage() returns null.
     * This means that calling Class.getPackage().getName()
     * is unreliable at best.
     */
    public static String getPackageName(Class clazz) {
        Package clazzPackage = clazz.getPackage();
        String packageName;
        if (clazzPackage != null) {
            packageName = clazzPackage.getName();
        } else {
            String clazzName = clazz.getName();
            packageName = new String(clazzName.toCharArray(), 0, clazzName.lastIndexOf('.'));
        }
        return packageName;
    }
    
    
    /**
     * Load the resource <code>resourceName</code>.
     * Try each classloader in succession,
     * until first succeeds, or all fail.
     * If all fail and <code>resouceName</code> is not absolute
     * (doesn't start with '/' character), then retry with
     * <code>packageName/resourceName</code> after changing all
     * '.' to '/'.
     * 
     * @param resourceName The name of the resource to load.
     */
    public static Resource getResource(Class spi,
                                       String resourceName,
                                       ClassLoaders loaders)
        throws DiscoveryException
    {
        DiscoverResources explorer = new DiscoverResources(loaders);
        ResourceIterator resources = explorer.findResources(resourceName);
        
        if (spi != null  &&
            !resources.hasNext()  &&
            resourceName.charAt(0) != '/')
        {
            /**
             * If we didn't find the resource, and if the resourceName
             * isn't an 'absolute' path name, then qualify with
             * package name of the spi.
             */
            resourceName = getPackageName(spi).replace('.','/') + "/" + resourceName;
            resources = explorer.findResources(resourceName);
        }
        
        return resources.hasNext()
               ? resources.nextResource()
               : null;
    }
    
    /**
     * Load named property file, optionally qualifed by spi's package name
     * as per Class.getResource.
     * 
     * A property file is loaded using the following sequence of class loaders:
     *   <ul>
     *     <li>Thread Context Class Loader</li>
     *     <li>DiscoverSingleton's Caller's Class Loader</li>
     *     <li>SPI's Class Loader</li>
     *     <li>DiscoverSingleton's (this class) Class Loader</li>
     *     <li>System Class Loader</li>
     *   </ul>
     * 
     * @param
     * @param propertiesFileName The property file name.
     * 
     * @return Instance of a class implementing the SPI.
     * 
     * @exception DiscoveryException Thrown if the name of a class implementing
     *            the SPI cannot be found, if the class cannot be loaded and
     *            instantiated, or if the resulting class does not implement
     *            (or extend) the SPI.
     */    
    public static Properties loadProperties(Class spi,
                                            String propertiesFileName,
                                            ClassLoaders classLoaders)
        throws DiscoveryException
    {
        Properties properties = null;
        
        if (propertiesFileName != null) {
            try {
                Resource resource = getResource(spi, propertiesFileName, classLoaders);
                if (resource != null) {
                    InputStream stream = resource.getResourceAsStream();
        
                    if (stream != null) {
                        properties = new Properties();
                        try {
                            properties.load(stream);
                        } finally {
                            stream.close();
                        }
                    }
                }
            } catch (IOException e) {
                ;  // ignore
            } catch (SecurityException e) {
                ;  // ignore
            }
        }
        
        return properties;
    }
}
