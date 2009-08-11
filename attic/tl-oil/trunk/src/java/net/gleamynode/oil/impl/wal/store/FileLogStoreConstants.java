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
 * @(#) $Id: FileLogStoreConstants.java 32 2004-11-09 14:37:16Z trustin $
 */
package net.gleamynode.oil.impl.wal.store;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 32 $, $Date: 2004-11-09 23:37:16 +0900 (화, 09 11월 2004) $
 */
class FileLogStoreConstants {
    public static final int DEFAULT_IO_BUFFER_SIZE = 8192;
    public static final byte[] LOG_HEADER = { 0x07, 0x26 };
    public static final int DEFAULT_MAX_ITEM_SIZE = 1048576;
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final int DEFAULT_BUFFER_FLUSH_INTERVAL = 5000; // milliseconds
    public static final String DEFAULT_THREAD_NAME = "FileLogStore";
    public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;
    static final String PROP_FILE = "file";
    static final String PROP_MAX_ITEM_SIZE = "maxItemSize";
    static final String PROP_BUFFER_FLUSH_INTERVAL = "bufferFlushInterval";
    static final String PROP_BUFFER_SIZE = "bufferSize";
    static final String PROP_THREAD_NAME = "threadName";
    static final String PROP_THREAD_PRIORITY = "threadPriority";

    private FileLogStoreConstants() {
    }
}
