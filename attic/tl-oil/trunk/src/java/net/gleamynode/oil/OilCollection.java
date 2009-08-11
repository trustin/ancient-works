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
 * @(#) $Id: OilCollection.java 32 2004-11-09 14:37:16Z trustin $
 */
package net.gleamynode.oil;


/**
 * A base representation of object collections such as {@link Queue} and
 * {@link Index}.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 32 $, $Date: 2004-11-09 23:37:16 +0900 (화, 09 11월 2004) $
 */
public interface OilCollection {
    /**
     * Returns the name of this collection.
     */
    String getName();

    /**
     * Returns the size (the number of elements or entries) of this collection.
     */
    int size();

    /**
     * Returns <code>true</code> if <code>size() == 0</code>.
     */
    boolean isEmpty();

    /**
     * Removes all elements or entries that this collection contains.
     */
    void clear();
}
