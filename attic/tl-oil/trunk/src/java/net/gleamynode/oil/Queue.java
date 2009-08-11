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
 * @(#) $Id: Queue.java 42 2004-11-23 06:26:38Z trustin $
 */
package net.gleamynode.oil;


/**
 * A Queue with random access support.
 * <p>
 * {@link #push(Object)} method returns a {@link QueueReference} which refers
 * to the newly added queue item.  You can remember that reference and
 * random-access the item with it later.  Please refer to {@link QueueReference}
 * documentation for the specific usage.  
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 42 $, $Date: 2004-11-23 15:26:38 +0900 (화, 23 11월 2004) $
 */
public interface Queue extends OilCollection {
	/**
	 * Pushes the specified object to this queue.
	 * 
	 * @return the reference to newly add item.
	 */
    QueueReference push(Object item);

    /**
     * Returns the object which the specified reference refers to.
     * 
     * @return <code>null</code> if the object is removed or does not exist
     */
    Object get(QueueReference reference);

    /**
     * Returns <code>true</code> if there is an object in queue which is referred by the specified reference.
     */
    boolean exists(QueueReference reference);

    /**
     * Removes the object which the specified reference refers to.
     * 
     * @return the removed object.  <code>null</code> if the object is already removed or doesn't exist
     */
    Object remove(QueueReference reference);

    /**
     * Moves the object which the specified srcRef refers to to the specified destination queue.
     * The destination queue must belong to the same database.
     *  
     * @return a reassigned reference
     */
    QueueReference moveTo(QueueReference srcRef, Queue destQueue);

    /**
     * Returns the iterator of this queue.
     */
    QueueIterator iterator();
}
