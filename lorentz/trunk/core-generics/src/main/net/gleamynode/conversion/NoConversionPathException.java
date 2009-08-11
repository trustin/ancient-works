/*
 *   @(#) $Id: NoConversionPathException.java 31 2005-07-29 16:04:01Z trustin $
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

/**
 * Thrown when {@link Converter} failed to find appropriate converters to perform
 * conversion.
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 31 $, $Date: 2005-07-30 01:04:01 +0900 (Sat, 30 Jul 2005) $
 */
public class NoConversionPathException extends ConversionException {

    private static final long serialVersionUID = -7734928942778097344L;

    /**
     * Creates a new exception.
     */
    public NoConversionPathException() {
        super();
    }

    /**
     * Creates a new exception with a message.
     */
    public NoConversionPathException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and a cause.
     */
    public NoConversionPathException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with a cause.
     */
    public NoConversionPathException(Throwable cause) {
        super(cause);
    }
}
