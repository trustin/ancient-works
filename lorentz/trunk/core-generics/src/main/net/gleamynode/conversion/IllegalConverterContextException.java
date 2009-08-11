/*
 *   @(#) $Id: IllegalConverterContextException.java 121 2005-10-01 15:24:11Z trustin $
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
 * Thrown when ConverterContext doesn't have appropriate attribute that the converter needs
 * or a format of the attribute is not appropriate. 
 * 
 * @author Jaeheon Kim (frima0@inpresence.com)
 * @version $Date: 2005. 8. 27.
 */
public class IllegalConverterContextException extends ConversionException {
	
    private static final long serialVersionUID = 6847936870921396865L;

    /**
     * Creates a new exception.
     */	
	public IllegalConverterContextException() {
		super();
	}

    /**
     * Creates a new exception with a message.
     */
	public IllegalConverterContextException(String message) {
		super(message);
	}

    /**
     * Creates a new exception with a message and a cause.
     */
	public IllegalConverterContextException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
     * Creates a new exception with a cause.
     */
	public IllegalConverterContextException(Throwable cause) {
		super(cause);
	}

}
