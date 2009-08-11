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
 * @(#) $Id: QueueReference.java 42 2004-11-23 06:26:38Z trustin $
 */
package net.gleamynode.oil;

import java.io.Serializable;


/**
 * A pointer to a queue item.  You can random-access the content of queue
 * using this.  It is similar to 'primary key' in RDBMS; Use {@link Index}
 * which contains your custom keys as keys and this references as values to
 * locate the item in the queue: 
 * <p>
 * <pre>
 * Queue queue = ...;
 * Index index = ...;
 * QueueReference ref = queue.push("Needle");
 * index.put("Magnifier", ref);
 * ...
 * 
 * String needle = (String) queue.remove(
 *         (QueueReference) index.get("Magnifier"));
 * </pre>
 * 
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 42 $, $Date: 2004-11-23 15:26:38 +0900 (화, 23 11월 2004) $
 * 
 * @see Queue
 */
public interface QueueReference extends Serializable {
}
