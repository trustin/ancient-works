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

import java.util.Vector;

import net.gleamynode.conversion.support.discovery.ResourceNameDiscover;
import net.gleamynode.conversion.support.discovery.ResourceNameIterator;


/**
 * Holder for multiple ResourceNameDiscover instances.
 * The result is the union of the results from each
 * (not a chained sequence, where results feed the next in line.
 *
 * @author Richard A. Sitze
 */
public class NameDiscoverers
    extends ResourceNameDiscoverImpl
    implements ResourceNameDiscover
{
    private Vector discoverers = new Vector();
    
    /**
     *  Construct a new resource name discoverer
     */
    public NameDiscoverers() {
    }
    
    /**
     * Specify an additional class loader to be used in searching.
     * The order of loaders determines the order of the result.
     * It is recommended to add the most specific loaders first.
     */
    public void addResourceNameDiscover(ResourceNameDiscover discover) {
        if (discover != null) {
            discoverers.addElement(discover);
        }
    }

    protected ResourceNameDiscover getResourceNameDiscover(int idx) {
        return (ResourceNameDiscover)discoverers.get(idx);
    }

    protected int size() {
        return discoverers.size();
    }

    /**
     * Set of results of all discoverers.
     * 
     * @return ResourceIterator
     */
    public ResourceNameIterator findResourceNames(final String resourceName) {
        return new ResourceNameIterator() {
            private int idx = 0;
            private ResourceNameIterator iterator = null;
            
            public boolean hasNext() {
                if (iterator == null  ||  !iterator.hasNext()) {
                    iterator = getNextIterator();
                    if (iterator == null) {
                        return false;
                    }
                }
                return iterator.hasNext();
            }
            
            public String nextResourceName() {
                return iterator.nextResourceName();
            }
            
            private ResourceNameIterator getNextIterator() {
                while (idx < size()) {
                    ResourceNameIterator iter =
                        getResourceNameDiscover(idx++).findResourceNames(resourceName);

                    if (iter.hasNext()) {
                        return iter;
                    }
                }
                return null;
            }
        };
    }
}
