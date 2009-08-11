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
 * @(#) $Id: QueueIterator.java 35 2004-11-09 15:43:37Z trustin $
 */
package net.gleamynode.oil;


/**
 * An iterator for {@link Queue}.
 * 
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 35 $, $Date: 2004-11-10 00:43:37 +0900 (수, 10 11월 2004) $
 */
public interface QueueIterator extends OilIterator {
	/**
	 * Returns the reference of current element.
	 * 
	 * @throws IllegalStateException if the element is already removed
	 */
    QueueReference getReference();
    
    /**
     * Moves the current element to the specified queue.
     * The destination queue must belong to the same database.
     * 
     * @return a newly assigned reference
     */
    QueueReference moveTo(Queue queue);
}
