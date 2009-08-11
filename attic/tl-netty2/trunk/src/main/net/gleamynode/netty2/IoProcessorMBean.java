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
 * @(#) $Id: IoProcessorMBean.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2;

import java.io.IOException;


/**
 * <a href="http://java.sun.com/products/JavaManagement/">JMX (Java Management
 * eXtenstions) </a> support interface for {@link IoProcessor}.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 *
 * @see IoProcessor
 */
public interface IoProcessorMBean {
    void start() throws IOException;

    void stop();

    boolean isStarted();

    int getThreadPoolSize();

    void setThreadPoolSize(int newSize);

    int getControllerThreadPriority();

    void setControllerThreadPriority(int newPriority);

    int getThreadPriority();

    void setThreadPriority(int newPriority);

    int getReadTries();

    void setReadTries(int readTries);

    String getThreadNamePrefix();

    void setThreadNamePrefix(String threadNamePrefix);
}
