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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.gleamynode.conversion.support.discovery.DiscoveryException;


/**
 * @author Richard A. Sitze
 */
public class ClassUtils {
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
            packageName = clazzName.substring(0, clazzName.lastIndexOf('.'));
        }
        return packageName;
    }
    
    /**
     * @return Method 'public static returnType methodName(paramTypes)',
     *         if found to be <strong>directly</strong> implemented by clazz.
     */
    public static Method findPublicStaticMethod(Class clazz,
                                                Class returnType,
                                                String methodName,
                                                Class[] paramTypes) {
        boolean problem = false;
        Method method = null;

        // verify '<methodName>(<paramTypes>)' is directly in class.
        try {
            method = clazz.getDeclaredMethod(methodName, paramTypes);
        } catch(NoSuchMethodException e) {
            problem = true;
        }
        
        // verify 'public static <returnType>'
        if (!problem  &&
            !(Modifier.isPublic(method.getModifiers()) &&
              Modifier.isStatic(method.getModifiers()) &&
              method.getReturnType() == returnType)) {
            problem = true;
            method = null;
        }
        
        return method;
    }

    /**
     * Instantiate a new 
     */    
    public static Object newInstance(Class impl, Class paramClasses[], Object params[])
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        if (paramClasses == null || params == null) {
            return impl.newInstance();
        } else {
            Constructor constructor = impl.getConstructor(paramClasses);
            return constructor.newInstance(params);
        }
    }
    
    /**
     * Throws exception if <code>impl</code> does not
     * implement or extend the SPI.
     */
    public static void verifyAncestory(Class spi, Class impl)
        throws DiscoveryException
    {
        if (spi == null) {
            throw new DiscoveryException("No interface defined!");
        }

        if (impl == null) {
            throw new DiscoveryException("No implementation defined for " + spi.getName());
        }

        if (!spi.isAssignableFrom(impl)) {
            throw new DiscoveryException("Class " + impl.getName() +
                                         " does not implement " + spi.getName());
        }
    }
}
