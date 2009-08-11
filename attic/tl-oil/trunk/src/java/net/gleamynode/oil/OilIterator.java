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
package net.gleamynode.oil;


/**
 * A base interface of iterators for {@link OilCollection}s.
 * It is very similar to single-directional {@link java.sql.ResultSet}.
 * 
 * TODO Current implementation sometimes returns null and otherwise <code>IllegalStateException</code> Let's unify this.
 *  
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 38 $, $Date: 2004-11-15 00:20:22 +0900 (월, 15 11월 2004) $
 */
public interface OilIterator {
	/**
	 * Moves the current position of this iterator forward.
	 * 
	 * @return <code>true</code> if there is a next element or entry
	 */
    boolean next();

    /**
     * Returns the current element or value of current position.
     * 
     * @throws IllegalStateException if the element or entry is already removed
     */
    Object getValue();

    /**
     * Replaces the current element or value of current position with the 
     * specified one.
     * 
     * @throws IllegalStateException if the element or entry is already removed
     */
    Object setValue(Object newValue);

    /**
     * Reflect the change of in-memory object to permanent storage.
     * This method is useful when database implementation stores objects
     * whose properties are changed in memory and those changes should be 
     * reflected to permanent storage such as a file.
     * 
     * @throws IllegalStateException if the element or entry is already removed
     */
    void update();

    /**
     * Removes the current element or entry and returns the removed value.
     * 
     * @throws IllegalStateException if the element or entry is already removed
     */
    Object remove();

    /**
     * Returns <code>true</code> if and only if the current element or entry is removed.
     */
    boolean isRemoved();
}
