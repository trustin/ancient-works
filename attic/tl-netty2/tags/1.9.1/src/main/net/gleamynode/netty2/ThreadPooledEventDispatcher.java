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
 * @(#) $Id: ThreadPooledEventDispatcher.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2;


/**
 * An interface for thread-pooled event dispatchers.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 *
 * @see SimpleEventDispatcher
 * @see OrderedEventDispatcher
 */
public interface ThreadPooledEventDispatcher extends EventDispatcher {
    /**
     * Starts this event dispatcher. this method will silently return if it is
     * already started.
     *
     * @throws IllegalStateException
     *             if <code>threadPoolSize</code> is not set.
     */
    void start();

    /**
     * Stops this event dispatcher. This method will silently return if it is
     * already stopped.
     */
    void stop();

    /**
     * Returns <code>true</code> if this event dispatcher is started.
     */
    boolean isStarted();

    /**
     * Returns the number of dispatcher threads.
     */
    int getThreadPoolSize();

    /**
     * Sets the number of dispatcher threads. This property is adjustable in
     * runtime.
     */
    void setThreadPoolSize(int newSize);

    /**
     * Returns the priority of dispatcher threads.
     */
    int getThreadPriority();

    /**
     * Sets the priority of dispatcher threads.
     *
     * @throws IllegalArgumentException
     *             if the specified priority is not between
     *             {@link Thread#MIN_PRIORITY}and {@link Thread#MAX_PRIORITY}.
     */
    void setThreadPriority(int newPriority);

    /**
     * Returns the prefix of dispatcher thread name.
     */
    String getThreadNamePrefix();

    /**
     * Sets the prefix of dispatcher thread name. This will help you to
     * determine which thread is which when you debug. The actual thread name
     * will be <code><em>threadNamePrefix</em> + '-' + threadId</code>.
     */
    void setThreadNamePrefix(String threadNamePrefix);
}
