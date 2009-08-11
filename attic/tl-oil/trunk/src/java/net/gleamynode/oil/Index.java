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
 * @(#) $Id: Index.java 35 2004-11-09 15:43:37Z trustin $
 */
package net.gleamynode.oil;


/**
 * A {@link java.util.Map}-like collection.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 35 $, $Date: 2004-11-10 00:43:37 +0900 (수, 10 11월 2004) $
 */
public interface Index extends OilCollection {
	
	/**
	 * Returns the value associated with the specified key.
	 * 
	 * @return <code>null</code> if the specified key is not found
	 */
    Object get(Object key);

    /**
     * Puts the apecified key and value pair.
     * 
     * @return the replaced old value object if the specified key already exists.
     */
    Object put(Object key, Object value);

    /**
     * Returns <code>true</code> if the specified key exists.
     */
    boolean containsKey(Object key);

    /**
     * Removes the apecified key and its associated value from this index.
     * 
     * @return the removed value object. <code>null</code> if the key doesn't exist. 
     */
    Object remove(Object key);

    void clear();

    /**
     * Returns the iterator of this index.  It is different from
     * {@link java.util.Iterator} that this returned iterator does not throw
     * {@link java.util.ConcurrentModificationException}s.
     */
    IndexIterator iterator();
}
