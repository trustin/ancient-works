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

package net.gleamynode.conversion.support.discovery.resource;

import java.util.Vector;

import net.gleamynode.conversion.support.discovery.jdk.JDKHooks;


/**
 * There are many different contexts in which
 * loaders can be used.  This provides a holder
 * for a set of class loaders, so that they
 * don't have to be build back up everytime...
 *
 * @author Richard A. Sitze
 * @author Craig R. McClanahan
 * @author Costin Manolache
 */
public class ClassLoaders
{
    protected Vector classLoaders = new Vector();
    
    /** Construct a new class loader set
     */
    public ClassLoaders() {
    }
    
    public int size() {
        return classLoaders.size();
    }
    
    public ClassLoader get(int idx) {
        return (ClassLoader)classLoaders.elementAt(idx);
    }

    /**
     * Specify a new class loader to be used in searching.
     * The order of loaders determines the order of the result.
     * It is recommended to add the most specific loaders first.
     */
    public void put(ClassLoader classLoader) {
        if (classLoader != null) {
            classLoaders.addElement(classLoader);
        }
    }
    

    /**
     * Specify a new class loader to be used in searching.
     * The order of loaders determines the order of the result.
     * It is recommended to add the most specific loaders first.
     * 
     * @param prune if true, verify that the class loader is
     *              not an Ancestor (@see isAncestor) before
     *              adding it to our list.
     */
    public void put(ClassLoader classLoader, boolean prune) {
        if (classLoader != null  &&  !(prune && isAncestor(classLoader))) {
            classLoaders.addElement(classLoader);
        }
    }
    
    
    /**
     * Check to see if <code>classLoader</code> is an
     * ancestor of any contained class loader.
     * 
     * This can be used to eliminate redundant class loaders
     * IF all class loaders defer to parent class loaders
     * before resolving a class.
     * 
     * It may be that this is not always true.  Therefore,
     * this check is not done internally to eliminate
     * redundant class loaders, but left to the discretion
     * of the user.
     */
    public boolean isAncestor(final ClassLoader classLoader) {
        /* bootstrap classloader, at root of all trees! */
        if (classLoader == null)
            return true;

        for (int idx = 0; idx < size(); idx++) {
            for(ClassLoader walker = get(idx);
                walker != null;
                walker = walker.getParent())
            {
                if (walker == classLoader) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Utility method.  Returns a preloaded ClassLoaders instance
     * containing the following class loaders, in order:
     * 
     * <ul>
     *   <li>spi.getClassLoader</li>
     *   <li>seeker.getClassLoader</li>
     *   <li>System Class Loader</li>
     * </ul>
     * 
     * Note that the thread context class loader is NOT present.
     * This is a reasonable set of loaders to try if the resource to be found
     * should be restricted to a libraries containing the SPI and Factory.
     * 
     * @param spi WHAT is being looked for (an implementation of this class,
     *            a default property file related to this class).
     * @param factory WHO is performing the lookup.
     * @param prune Determines if ancestors are allowed to be loaded or not.
     */    
    public static ClassLoaders getLibLoaders(Class spi, Class factory, boolean prune) {
        ClassLoaders loaders = new ClassLoaders();
        
        if (spi != null) loaders.put(spi.getClassLoader());
        if (factory != null) loaders.put(factory.getClassLoader(), prune);
        loaders.put(JDKHooks.getJDKHooks().getSystemClassLoader(), prune);
        
        return loaders;
    }
    
    /**
     * Utility method.  Returns a preloaded ClassLoaders instance
     * containing the following class loaders, in order:
     * 
     * <ul>
     *   <li>Thread Context Class Loader</li>
     *   <li>spi.getClassLoader</li>
     *   <li>seeker.getClassLoader</li>
     *   <li>System Class Loader</li>
     * </ul>
     * 
     * Note that the thread context class loader IS  present.
     * This is a reasonable set of loaders to try if the resource to be found
     * may be provided by an application.
     * 
     * @param spi WHAT is being looked for (an implementation of this class,
     *            a default property file related to this class).
     * @param factory WHO is performing the lookup (factory).
     * @param prune Determines if ancestors are allowed to be loaded or not.
     */    
    public static ClassLoaders getAppLoaders(Class spi, Class factory, boolean prune) {
        ClassLoaders loaders = new ClassLoaders();

        loaders.put(JDKHooks.getJDKHooks().getThreadContextClassLoader());
        if (spi != null) loaders.put(spi.getClassLoader(), prune);
        if (factory != null) loaders.put(factory.getClassLoader(), prune);
        loaders.put(JDKHooks.getJDKHooks().getSystemClassLoader(), prune);
        
        return loaders;
    }
}
