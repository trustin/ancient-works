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
 * @(#) $Id: LogStore.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import java.util.Properties;

import net.gleamynode.oil.impl.wal.log.Log;


/**
 * A storage of logs {@link WalDatabase} generates.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 */
public interface LogStore {
    /**
     * Returns the extended properties of this store.  This property
     * usually contains implementation-specific properties such as log file
     * path.  The returned object is a deep copy, so you have to call
     * {@link #setProperties(Properties)} to change the properties.
     */
    Properties getProperties();
    
    /**
     * Sets the extended properties of this store.  This property usually
     * contains implementation-specific properties such as database file path
     * or buffer size.
     */
    void setProperties(Properties properties);
    
    /**
     * Opens the store.
     */
    void open();

    /**
     * Flushes log buffer.
     */
    void flush();

    /**
     * Closes the store.
     */
    void close();

    /**
     * Returns <code>true</code> if and only if this store is open.
     */
    boolean isOpen();

    /**
     * Reads a {@link Log} from the store.
     */
    Log read(boolean recover);
    
    /**
     * Returns the current read position.
     */
    long getCurrentReadPointer();
    
    /**
     * Returns the last read pointer.
     */
    long getLastReadPointer();

    /**
     * Writes (appends) a {@link Log} into the store.
     */
    void write(Log log);

    /**
     * Returns a new {@link LogStore} which will replace this store and contain
     * the defragmented logs.
     */
    LogStore startCompaction();

    /**
     * Finishes the defragmentation and replace the old store.
     */
    void finishCompaction(boolean success);
}
