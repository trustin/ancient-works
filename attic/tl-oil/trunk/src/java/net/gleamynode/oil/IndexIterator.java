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
 * @(#) $Id: IndexIterator.java 38 2004-11-14 15:20:22Z trustin $
 */
package net.gleamynode.oil;


/**
 * An iterator for {@link Index}.
 * <p>
 * Example:
 * <pre>
 * // this code prints and remove all key-value pairs from the index.
 * Index idx = ...;
 * IndexIterator it = idx.iterator();
 * while (it.next()) {
 *     String key = (String) it.getKey();
 *     String value = (String) it.getValue();
 *     System.out.println(key + " = " + value);
 *     it.remove();
 * }
 * </pre>
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 38 $, $Date: 2004-11-15 00:20:22 +0900 (월, 15 11월 2004) $
 */
public interface IndexIterator extends OilIterator {
	/**
	 * Returns the key object of current entry.
	 * 
	 * @throws IllegalStateException if the current entry is already removed
	 */
    Object getKey();
}
