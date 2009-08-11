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
 * @(#) $Id: SessionConfigMBean.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;


/**
 * <a href="http://java.sun.com/products/JavaManagement/">JMX (Java Management
 * eXtenstions) </a> support interface for {@link SessionConfig}.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 *
 * @see SessionConfig
 */
public interface SessionConfigMBean {
    public int getConnectTimeout();

    public void setConnectTimeout(int connectTimeout);

    public int getIdleTime();

    public void setIdleTime(int idleTime);

    public int getMaxQueuedWriteCount();

    public void setMaxQueuedWriteCount(int newLimit);

    public int getWriteTimeout();

    public void setWriteTimeout(int writeTimeout);
}
