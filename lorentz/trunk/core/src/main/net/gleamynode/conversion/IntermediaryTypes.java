/*
 *   @(#) $Id: IntermediaryTypes.java 121 2005-10-01 15:24:11Z trustin $
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a set of intermediaty types involved in conversion/copy process.
 * <p>
 * For example, <tt>java.lang.String</tt> is an intermediary type in this case:
 * <pre>
 * java.lang.Long -&gt; java.lang.String -&gt; java.math.BigInteger
 * </pre>
 * 
 * @author Trustin Lee
 * @version $Rev: 121 $, $Date: 2005-10-02 00:24:11 +0900 (Sun, 02 Oct 2005) $
 */
public class IntermediaryTypes {
    private final Set types = new HashSet();

    public IntermediaryTypes() {
    }

    public int size() {
        return types.size();
    }

    public boolean isEmpty() {
        return types.isEmpty();
    }

    public boolean contains(Class type) {
        return types.contains(type);
    }

    public Iterator iterator() {
        return types.iterator();
    }

    public Class[] toArray() {
        Class[] array = new Class[types.size()];
        return (Class[]) types.toArray(array);
    }

    public boolean add(Class type) {
        return types.add(type);
    }

    public boolean remove(Class type) {
        return types.remove(type);
    }

    public boolean containsAll(IntermediaryTypes types)
    {
        return this.types.containsAll(types.types);
    }

    public boolean addAll(IntermediaryTypes types) {
        return this.types.addAll(types.types);
    }

    public boolean retainAll(IntermediaryTypes types) {
        return this.types.retainAll(types.types);
    }

    public boolean removeAll(IntermediaryTypes types) {
        return this.types.removeAll(types.types);
    }

    public void clear() {
        types.clear();
    }
}
