/*
 *   @(#) $Id: StringToSetConverter.java 60 2005-08-18 09:06:14Z trustin $
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
package net.gleamynode.conversion.converter.jdk13;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 60 $, $Date: 2005-08-18 18:06:14 +0900 (Thu, 18 Aug 2005) $
 */
public class StringToSetConverter extends StringToCollectionConverter {
	public StringToSetConverter() {
        super(Set.class);
	}
	
	protected Collection newCollection() {
		return new HashSet();
	}
}
