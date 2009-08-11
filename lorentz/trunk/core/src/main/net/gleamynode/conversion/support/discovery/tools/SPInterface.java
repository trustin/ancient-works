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

import net.gleamynode.conversion.support.discovery.DiscoveryException;


/**
 * Represents a Service Programming Interface (spi).
 * - SPI's name
 * - SPI's (provider) class
 * - SPI's (alternate) override property name
 * 
 * In addition, while there are many cases where this is NOT
 * usefull, for those in which it is:
 * 
 * - expected constructor argument types and parameters values.
 * 
 * @author Richard A. Sitze
 */
public class SPInterface {
    /**
     * The service programming interface: intended to be
     * an interface or abstract class, but not limited
     * to those two.
     */        
    private final Class spi;
    
    /**
     * The property name to be used for finding the name of
     * the SPI implementation class.
     */
    private final String propertyName;
    
    
    private Class  paramClasses[] = null;
    private Object params[] = null;


    /**
     * Construct object representing Class <code>provider</code>.
     * 
     * @param provider The SPI class
     */
    public SPInterface(Class provider) {
        this(provider, provider.getName());
    }
    
    /**
     * Construct object representing Class <code>provider</code>.
     * 
     * @param provider The SPI class
     * 
     * @param propertyName when looking for the name of a class implementing
     *        the provider class, a discovery strategy may involve looking for
     *        (system or other) properties having either the name of the class
     *        (provider) or the <code>propertyName</code>.
     */
    public SPInterface(Class spi, String propertyName) {
        this.spi = spi;
        this.propertyName = propertyName;
    }

    /**
     * Construct object representing Class <code>provider</code>.
     * 
     * @param provider The SPI class
     * 
     * @param constructorParamClasses classes representing the
     *        constructor argument types.
     * 
     * @param constructorParams objects representing the
     *        constructor arguments.
     */
    public SPInterface(Class provider,
                       Class constructorParamClasses[],
                       Object constructorParams[])
    {
        this(provider,
             provider.getName(),
             constructorParamClasses,
             constructorParams);
    }
    
    /**
     * Construct object representing Class <code>provider</code>.
     * 
     * @param provider The SPI class
     * 
     * @param propertyName when looking for the name of a class implementing
     *        the provider class, a discovery strategy may involve looking for
     *        (system or other) properties having either the name of the class
     *        (provider) or the <code>propertyName</code>.
     * 
     * @param constructorParamClasses classes representing the
     *        constructor argument types.
     * 
     * @param constructorParams objects representing the
     *        constructor arguments.
     */
    public SPInterface(Class spi,
                       String propertyName,
                       Class constructorParamClasses[],
                       Object constructorParams[])
    {
        this.spi = spi;
        this.propertyName = propertyName;
        this.paramClasses = constructorParamClasses;
        this.params = constructorParams;
    }

    public String getSPName() {
        return spi.getName();
    }

    public Class getSPClass() {
        return spi;
    }
    
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Instantiate a new 
     */    
    public Object newInstance(Class impl)
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        verifyAncestory(impl);
        
        return ClassUtils.newInstance(impl, paramClasses, params);
    }
    
    public void verifyAncestory(Class impl) {
        ClassUtils.verifyAncestory(spi, impl);
    }
}
