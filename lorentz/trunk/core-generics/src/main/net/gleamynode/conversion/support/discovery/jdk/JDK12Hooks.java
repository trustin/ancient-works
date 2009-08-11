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

package net.gleamynode.conversion.support.discovery.jdk;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;


/**
 * @author Richard A. Sitze
 */
class JDK12Hooks extends JDKHooks {
    private static final ClassLoader systemClassLoader
        = findSystemClassLoader();

    /**
     * The thread context class loader is available for JDK 1.2
     * or later, if certain security conditions are met.
     * 
     * @return The thread context class loader, if available.
     *         Otherwise return null.
     */
    public ClassLoader getThreadContextClassLoader() {
        ClassLoader classLoader;
        
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException e) {
            /**
             * SecurityException is thrown when
             * a) the context class loader isn't an ancestor of the
             *    calling class's class loader, or
             * b) if security permissions are restricted.
             * 
             * For (a), ignore and keep going.  We cannot help but also
             * ignore (b) with the logic below, but other calls elsewhere
             * (to obtain a class loader) will re-trigger this exception
             * where we can make a distinction.
             */
            classLoader = null;  // ignore
        }
        
        // Return the selected class loader
        return classLoader;
    }
    
    /**
     * The system class loader is available for JDK 1.2
     * or later, if certain security conditions are met.
     * 
     * @return The system class loader, if available.
     *         Otherwise return null.
     */
    public ClassLoader getSystemClassLoader() {
        return systemClassLoader;
    }

    /**
     * Implement ClassLoader.getResources for JDK 1.2
     */
    public Enumeration getResources(ClassLoader loader,
                                    String resourceName)
        throws IOException
    {
        /**
         * The simple answer is/was:
         *    return loader.getResources(resourceName);
         * 
         * However, some classloaders overload the behavior of getResource
         * (loadClass, etc) such that the order of returned results changes
         * from normally expected behavior.
         * 
         * Example: locate classes/resources from child ClassLoaders first,
         *          parents last (in some J2EE environs).
         * 
         * The resource returned by getResource() should be the same as the
         * first resource returned by getResources().  Unfortunately, this
         * is not, and cannot be: getResources() is 'final' in the current
         * JDK's (1.2, 1.3, 1.4).
         * 
         * To address this, the implementation of this method will
         * return an Enumeration such that the first element is the
         * results of getResource, and all trailing elements are
         * from getResources.  On each iteration, we check so see
         * if the resource (from getResources) matches the first resource,
         * and eliminate the redundent element.
         */
        
        final URL first = (URL)loader.getResource(resourceName);
        final Enumeration rest = loader.getResources(resourceName);
        
        return new Enumeration() {
            private boolean firstDone = (first == null);
            private URL next = getNext();
            
            public Object nextElement() {
                URL o = next;
                next = getNext();
                return o;
            }

            public boolean hasMoreElements() {
                return next != null;
            }
            
            private URL getNext() {
                URL n;
                
                if (!firstDone) {
                    /**
                     * First time through, use results of getReference()
                     * if they were non-null.
                     */
                    firstDone = true;
                    n = first;
                } else {
                    /**
                     * Subsequent times through,
                     * use results of getReferences()
                     * but take out anything that matches 'first'.
                     * 
                     * Iterate through list until we find one that
                     * doesn't match 'first'.
                     */
                    n = null;
                    while (rest.hasMoreElements()  &&  n == null) {
                        n = (URL)rest.nextElement();
                        if (first != null &&
                            n != null &&
                            n.equals(first))
                        {
                            n = null;
                        }
                    }
                }
                
                return n;
            }
        };
    }
    
    static private ClassLoader findSystemClassLoader() {
        ClassLoader classLoader;
        
        try {
            classLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException e) {
            /**
             * Ignore and keep going.
             */
            classLoader = null;
        }

        if (classLoader == null) {
            classLoader = new PsuedoSystemClassLoader();
        }
        
        // Return the selected class loader
        return classLoader;
    }
}
