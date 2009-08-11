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

package net.gleamynode.conversion.support.discovery.resource.names;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import net.gleamynode.conversion.support.discovery.Resource;
import net.gleamynode.conversion.support.discovery.ResourceDiscover;
import net.gleamynode.conversion.support.discovery.ResourceIterator;
import net.gleamynode.conversion.support.discovery.ResourceNameDiscover;
import net.gleamynode.conversion.support.discovery.ResourceNameIterator;
import net.gleamynode.conversion.support.discovery.resource.ClassLoaders;
import net.gleamynode.conversion.support.discovery.resource.DiscoverResources;



/**
 * Discover ALL files of a given name, and return resource names
 * contained within the set of files:
 * <ul>
 *   <li>one resource name per line,</li>
 *   <li>whitespace ignored,</li>
 *   <li>comments begin with '#'</li>
 * </ul>
 * 
 * Default discoverer is DiscoverClassLoaderResources,
 * but it can be set to any other.
 *
 * @author Richard A. Sitze
 * @author Costin Manolache
 * @author James Strachan
 */
public class DiscoverNamesInFile
    extends ResourceNameDiscoverImpl
    implements ResourceNameDiscover
{
    private ResourceDiscover discoverResources;
    
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverNamesInFile() {
        discoverResources = new DiscoverResources();
    }
    
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverNamesInFile(ClassLoaders loaders) {
        discoverResources = new DiscoverResources(loaders);
    }
    
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverNamesInFile(ResourceDiscover discoverer) {
        this.discoverResources = discoverer;
    }

    /**
     * Specify set of class loaders to be used in searching.
     */
    public void setDiscoverer(ResourceDiscover discover) {
        this.discoverResources = discover;
    }

    /**
     * To be used by downstream elements..
     */
    public ResourceDiscover getDiscover() {
        return discoverResources;
    }

    /**
     * @return Enumeration of ServiceInfo
     */
    public ResourceNameIterator findResourceNames(final String fileName) {
        return new ResourceNameIterator() {
            private ResourceIterator files =
                getDiscover().findResources(fileName);

            private int idx = 0;
            private Vector classNames = null;
            private String resource = null;
            
            public boolean hasNext() {
                if (resource == null) {
                    resource = getNextClassName();
                }
                return resource != null;
            }
            
            public String nextResourceName() {
                String element = resource;
                resource = null;
                return element;
            }
            
            private String getNextClassName() {
                if (classNames == null || idx >= classNames.size()) {
                    classNames = getNextClassNames();
                    idx = 0;
                    if (classNames == null) {
                        return null;
                    }
                }

                String className = (String)classNames.get(idx++);

                return className;
            }

            private Vector getNextClassNames() {
                while (files.hasNext()) {
                    Vector results = readServices(files.nextResource());
                    if (results != null  &&  results.size() > 0) {
                        return results;
                    }
                }
                return null;
            }
        };
    }

    /**
     * Read everything, no defering here..
     * Ensure that files are closed before we leave.
     */
    private Vector readServices(final Resource info) {
        Vector results = new Vector();
        
        InputStream is = info.getResourceAsStream();
        
        if( is != null ) {
            try {
                try {
                    // This code is needed by EBCDIC and other
                    // strange systems.  It's a fix for bugs
                    // reported in xerces
                    BufferedReader rd;
                    try {
                        rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    } catch (java.io.UnsupportedEncodingException e) {
                        rd = new BufferedReader(new InputStreamReader(is));
                    }
                    
                    try {
                        String serviceImplName;
                        while( (serviceImplName = rd.readLine()) != null) {
                            int idx = serviceImplName.indexOf('#');
                            if (idx >= 0) {
                                serviceImplName = serviceImplName.substring(0, idx);
                            }
                            serviceImplName = serviceImplName.trim();
    
                            if (serviceImplName.length() != 0) {
                                results.add(serviceImplName);
                            }
                        }
                    } finally {
                        rd.close();
                    }
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        
        return results;
    }
}
