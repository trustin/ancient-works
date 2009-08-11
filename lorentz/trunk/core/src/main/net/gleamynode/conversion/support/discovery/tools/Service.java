/*
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

import java.util.Enumeration;

import net.gleamynode.conversion.support.discovery.ResourceClass;
import net.gleamynode.conversion.support.discovery.ResourceClassIterator;
import net.gleamynode.conversion.support.discovery.ResourceNameIterator;
import net.gleamynode.conversion.support.discovery.resource.ClassLoaders;
import net.gleamynode.conversion.support.discovery.resource.classes.DiscoverClasses;
import net.gleamynode.conversion.support.discovery.resource.names.DiscoverServiceNames;


/**
 * [this was ServiceDiscovery12... the 1.1 versus 1.2 issue
 * has been abstracted to org.apache.commons.discover.jdk.JDKHooks]
 * 
 * <p>Implement the JDK1.3 'Service Provider' specification.
 * ( http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html )
 * </p>
 *
 * This class supports any VM, including JDK1.1, via
 * org.apache.commons.discover.jdk.JDKHooks.
 *
 * The caller will first configure the discoverer by adding ( in the desired
 * order ) all the places to look for the META-INF/services. Currently
 * we support loaders.
 *
 * The findResources() method will check every loader.
 *
 * @author Richard A. Sitze
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author James Strachan
 */
public class Service
{
    /** Construct a new service discoverer
     */
    protected Service() {
    }
    
    /**
     * as described in
     * sun/jdk1.3.1/docs/guide/jar/jar.html#Service Provider,
     * Except this uses <code>Enumeration</code>
     * instead of <code>Interator</code>.
     * 
     * @return Enumeration of class instances (<code>Object</code>)
     */
    public static Enumeration providers(Class spiClass) {
        return providers(new SPInterface(spiClass), null);
    }
    
    /**
     * This version lets you specify constructor arguments..
     * 
     * @param spi SPI to look for and load.
     * @param classLoaders loaders to use in search.
     *        If <code>null</code> then use ClassLoaders.getAppLoaders().
     */
    public static Enumeration providers(final SPInterface spi,
                                        ClassLoaders loaders)
    {
        if (loaders == null) {
            loaders = ClassLoaders.getAppLoaders(spi.getSPClass(),
                                                 Service.class,
                                                 true);
        }
        
        ResourceNameIterator servicesIter =
            (new DiscoverServiceNames(loaders)).findResourceNames(spi.getSPName());

        final ResourceClassIterator services =
            (new DiscoverClasses(loaders)).findResourceClasses(servicesIter);
        
        return new Enumeration() {
            private Object object = null;
            
            public boolean hasMoreElements() {
                if (object == null) {
                    object = getNextClassInstance();
                }
                return object != null;
            }
            
            public Object nextElement() {
                Object obj = object;
                object = null;
                return obj;
            }

            private Object getNextClassInstance() {
                while (services.hasNext()) {
                    ResourceClass info = services.nextResourceClass();
                    try {
                        return spi.newInstance(info.loadClass());
                    } catch (Exception e) {
                        // ignore
                    }
                }
                return null;
            }
        };
    }
}
