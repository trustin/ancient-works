/*
 *   @(#) $Id: Init.java 36 2005-08-05 12:27:31Z trustin $
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
package net.gleamynode.conversion.converter.optional;

import net.gleamynode.conversion.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 36 $, $Date: 2005-08-05 21:27:31 +0900 (Fri, 05 Aug 2005) $
 */
public class Init {
	static {
		register("ObjectNameToString");
        register("StringToObjectName");
	}

	private static void register(String name) {
		try {
			Converter.register(
                    (Converter) Class.forName(
                            Init.class.getPackage().getName() + '.' + name +
                            "Converter", true, Init.class.getClassLoader())
                            .newInstance());
		} catch (Throwable t) {
		}
	}

	public Init() {
	}
}