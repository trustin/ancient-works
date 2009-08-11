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
 * @(#) $Id: Constants.java 42 2004-11-23 06:26:38Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import net.gleamynode.oil.impl.wal.store.FileLogStore;


/**
 * Constants which represents limit and default values.
 * 
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 42 $, $Date: 2004-11-23 15:26:38 +0900 (화, 23 11월 2004) $
 */
public class Constants {
    /**
     * the maximum of <code>maxItemsPerExtent</code> property.
     */
    public static final int MAX_MAX_ITEMS_PER_EXTENT = 1048576;
    
    /**
     * The maximum number of extents per queue.
     */
    public static final int MAX_EXTENTS_PER_QUEUE = 65536;
    
    /**
     * The maximum number of queues in a database.
     */
    public static final int MAX_QUEUES = 65536;

    /**
     * The maximum number of indices in a database.
     */
    public static final int MAX_INDICES = MAX_QUEUES;

    /**
     * The maximum number of queue/index names in a database.
     * {@link WalDatabase} internally translates queue/index names into
     * integer value.
     */
    public static final int MAX_NAMES = MAX_QUEUES + MAX_INDICES;
    
    /**
     * The default value of <code>maxItemsPerExtent</code>.
     */
    public static final int DEFAULT_MAX_ITEMS_PER_EXTENT = 8192;

    static final int DEFAULT_EXTENTS = 64;
    static final int DEFAULT_QUEUES = 64;
    static final int DEFAULT_INDICES = DEFAULT_QUEUES;
    static final String DEFAULT_LOG_STORE = FileLogStore.class.getName();
    static final String PROP_MAX_ITEMS_PER_EXTENT = "maxItemsPerExtent";
    static final String PROP_LOG_STORE = "logStore";

    private Constants() {
    }
}
