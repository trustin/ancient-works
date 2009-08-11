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
 * @(#) $Id: Database.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil;

import java.util.Properties;
import java.util.Set;


/**
 * (<strong>Entry Point</strong>) A Database which provides an object queue
 * and its random accessibility.
 * <p>
 * This database provides two data structures:
 * <ul>
 * <li>{@link Queue} provides sequential access
 * </li>
 * <li>{@link Index} provides random access based on {@link java.util.Map}.
 * </ul>
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 */
public interface Database {
    /**
     * Opens the database.
     *
     * @throws OilException
     *             if failed to open this database
     * @throws RunRecoveryException
     *             if this log store is corrupted. Call {@link #recover()}to
     *             attempt to recover the log store.
     */
    void open();
    
    /**
     * Opens the database with the specified progress monitor.
     *
     * @throws OilException
     *             if failed to open this database
     * @throws RunRecoveryException
     *             if this log store is corrupted. Call {@link #recover()}to
     *             attempt to recover the log store.
     */
    void open(ProgressMonitor progressMonitor);

    /**
     * Attempts to recover this database.
     *
     * @throws OilException
     *             if the database is already open, or recovery is failed.
     */
    void recover();

    /**
     * Attempts to recover this database with the specified progress monitor.
     *
     * @throws OilException
     *             if the database is already open, or recovery is failed.
     */
    void recover(ProgressMonitor progressMonitor);

    /**
     * Defragments this database.  This method is also called 'compaction'.
     * It differs among implementations, but it generally removes unused records
     * from database and therefore compacts it.
     *
     * @throws OilException
     *             if failed to defrag. If failed, the database might be closed.
     */
    void defragment();

    /**
     * Defragments this database with the specified progress monitor.  This
     * method is also called 'compaction'.  It differs among implementations,
     * but it generally removes unused records from database and therefore
     * compacts it.
     *
     * @throws OilException
     *             if failed to defrag. If failed, the database might be closed.
     */
    void defragment(ProgressMonitor progressMonitor);

    /**
     * Returns the {@link Queue} with the specified name.  If the queue with
     * the specified name does not exist, it will create one and return it, so
     * this method never returns <code>null</code>.
     *
     * @throws OilException
     *             if failed to open a new queue.
     */
    Queue getQueue(String name);

    /**
     * Returns the {@link net.gleamynode.oil.Index}with the specified name.
     * If the index with the specified name does not exist, it will create one
     * and return it, so this method never returns <code>null</code>.
     *
     * @throws OilException
     *             if failed to open a new index.
     */
    Index getIndex(String name);

    /**
     * Returns the names of the queues this database contains.
     *
     * @throws OilException
     *             if failed to get the name set
     */
    Set getQueueNames();

    /**
     * Returns the the names of the indexes this database contains.
     *
     * @throws OilException
     *             if failed to get the name set
     */
    public Set getIndexNames();

    /**
     * Returns <code>true</code> if this database is open.
     */
    boolean isOpen();

    /**
     * Closes this database and release all resources associated with it.
     *
     * @throws OilException
     *             if failed to close the database.
     */
    void close();

    /**
     * Returns the extended properties of this database.  This property
     * usually contains implementation-specific properties such as database file
     * path.  The returned object is a deep copy, so you have to call
     * {@link #setProperties(Properties)} to change the properties.
     */
    Properties getProperties();

    /**
     * Sets the extended properties of this database.  This property usually
     * contains implementation-specific properties such as database file path
     * or buffer size.
     */
    void setProperties(Properties properties);
}
