/*
 *   @(#) $Id: ConversionException.java 76 2005-08-19 17:00:59Z trustin $
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

import net.gleamynode.conversion.support.lang.exception.NestableRuntimeException;

/**
 * Thrown when {@link Converter} failed to convert an object.
 * 
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 76 $, $Date: 2005-08-20 02:00:59 +0900 (Sat, 20 Aug 2005) $
 */
public class ConversionException extends NestableRuntimeException {

    private static final long serialVersionUID = -7327694392138075920L;

    /**
     * Creates a new exception.
     */
    public ConversionException() {
        super();
    }

    /**
	 * Creates a new exception with a message.
	 */
	public ConversionException(String message) {
		super(message);
	}

    /**
     * Creates a new exception with a message and a cause.
     */
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with a cause.
     */
    public ConversionException(Throwable cause) {
        super(cause);
    }
}
