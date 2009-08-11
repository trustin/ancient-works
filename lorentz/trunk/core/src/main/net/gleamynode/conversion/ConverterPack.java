/*
 *   @(#) $Id: ConverterPack.java 76 2005-08-19 17:00:59Z trustin $
 *
 *   Copyright 2004 Trustin Lee
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package net.gleamynode.conversion;

import java.util.Iterator;

import net.gleamynode.conversion.Converter;

/**
 * A Service-provider class that registers a set of {@link Converter}s
 * when JAR file is loaded. 
 *
 * @author Trustin Lee
 * @version $Rev: 76 $, $Date: 2005-08-20 02:00:59 +0900 (Sat, 20 Aug 2005) $
 *
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">JAR File Specification</a>
 */
public abstract class ConverterPack {

    /**
     * Creates a new {@link ConverterPack}.
     */
    protected ConverterPack() {
    }

    /**
     * Implement this method to return an {@link Iterator} that iterates
     * all {@link Converter}s that this {@link ConverterPack} provides.
     */
    public abstract Iterator newConverters();
}
