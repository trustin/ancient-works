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
 * @(#) $Id: ThreadPooledEventDispatcherMBean.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;


/**
 * <a href="http://java.sun.com/products/JavaManagement/">JMX (Java Management
 * eXtenstions) </a> support interface for thread-pooled event dispatchers.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
public interface ThreadPooledEventDispatcherMBean
    extends ThreadPooledEventDispatcher, EventDispatcherMBean {
    /**
     * @see ThreadPooledEventDispatcher#start()
     */
    void start();

    /**
     * @see ThreadPooledEventDispatcher#stop()
     */
    void stop();

    /**
     * @see ThreadPooledEventDispatcher#isStarted()
     */
    boolean isStarted();

    /**
     * @see ThreadPooledEventDispatcher#getThreadPoolSize()
     */
    int getThreadPoolSize();

    /**
     * @see ThreadPooledEventDispatcher#setThreadPoolSize(int)
     */
    void setThreadPoolSize(int newSize);

    /**
     * @see ThreadPooledEventDispatcher#getThreadPriority()
     */
    int getThreadPriority();

    /**
     * @see ThreadPooledEventDispatcher#setThreadPriority(int)
     */
    void setThreadPriority(int newPriority);

    /**
     * @see ThreadPooledEventDispatcher#getThreadNamePrefix()
     */
    String getThreadNamePrefix();

    /**
     * @see ThreadPooledEventDispatcher#setThreadNamePrefix(String)
     */
    void setThreadNamePrefix(String threadNamePrefix);
}
