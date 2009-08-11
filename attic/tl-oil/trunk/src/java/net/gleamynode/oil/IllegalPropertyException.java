/*
 *   Copyright 2004 The Apache Software Foundation
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
/*
 * @(#) $Id: IllegalPropertyException.java 38 2004-11-14 15:20:22Z trustin $
 */
package net.gleamynode.oil;


/**
 * An {@link OilException} that is thrown when the extended properties of
 * {@link Database} is not valid, and so {@link Database#open()} fails.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 38 $, $Date: 2004-11-15 00:20:22 +0900 (월, 15 11월 2004) $
 */
public class IllegalPropertyException extends OilException {
    /**
     * Creates a new instance.
     */
    public IllegalPropertyException() {
        super();
    }

    /**
     * Creates a new instance.
     */
    public IllegalPropertyException(String s) {
        super(s);
    }

    /**
     * Creates a new instance.
     */
    public IllegalPropertyException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Creates a new instance.
     */
    public IllegalPropertyException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
