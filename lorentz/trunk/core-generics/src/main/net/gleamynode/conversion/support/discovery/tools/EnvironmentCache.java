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

import java.util.HashMap;

import net.gleamynode.conversion.support.discovery.jdk.JDKHooks;


/**
 * Cache by a 'key' unique to the environment:
 * 
 * - ClassLoader::groupContext::Object Cache
 *         Cache : HashMap
 *         Key   : Thread Context Class Loader (<code>ClassLoader</code>)
 *         Value : groupContext::SPI Cache (<code>HashMap</code>)
 * 
 * //- groupContext::Object Cache
 * //         Cache : HashMap
 * //         Key   : groupContext (<code>String</code>)
 * //        Value : <code>Object</code>
 * 
 * When we 'release', it is expected that the caller of the 'release'
 * have the same thread context class loader... as that will be used
 * to identify cached entries to be released.
 * 
 * @author Richard A. Sitze
 */
public class EnvironmentCache {
    /**
     * Allows null key, important as default groupContext is null.
     * 
     * We will manage synchronization directly, so all caches are implemented
     * as HashMap (unsynchronized).
     * 
     */
    private static final HashMap root_cache = new HashMap();

    /**
     * Initial hash size for SPI's, default just seem TO big today..
     */
    public static final int smallHashSize = 13;
    
    /**
     * Get object keyed by classLoader.
     */
    public static synchronized Object get(ClassLoader classLoader)
    {
        /**
         * 'null' (bootstrap/system class loader) thread context class loader
         * is ok...  Until we learn otherwise.
         */
        return root_cache.get(classLoader);
    }
    
    /**
     * Put service keyed by spi & classLoader.
     */
    public static synchronized void put(ClassLoader classLoader, Object object)
    {
        /**
         * 'null' (bootstrap/system class loader) thread context class loader
         * is ok...  Until we learn otherwise.
         */
        if (object != null) {
            root_cache.put(classLoader, object);
        }
    }


    /********************** CACHE-MANAGEMENT SUPPORT **********************/
    
    /**
     * Release all internal references to previously created service
     * instances associated with the current thread context class loader.
     * The <code>release()</code> method is called for service instances that
     * implement the <code>Service</code> interface.
     *
     * This is useful in environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent
     * garbage collection.
     */
    public static synchronized void release() {
        /**
         * 'null' (bootstrap/system class loader) thread context class loader
         * is ok...  Until we learn otherwise.
         */
        root_cache.remove(JDKHooks.getJDKHooks().getThreadContextClassLoader());
    }
    
    
    /**
     * Release any internal references to a previously created service
     * instance associated with the current thread context class loader.
     * If the SPI instance implements <code>Service</code>, then call
     * <code>release()</code>.
     */
    public static synchronized void release(ClassLoader classLoader) {
        /**
         * 'null' (bootstrap/system class loader) thread context class loader
         * is ok...  Until we learn otherwise.
         */
        root_cache.remove(classLoader);
    }
}
