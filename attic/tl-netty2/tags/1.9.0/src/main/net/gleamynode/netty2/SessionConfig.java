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
 * @(#) $Id: SessionConfig.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;

import java.net.ConnectException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * Contains properties for {@link Session}s. You can create one config object
 * and apply it calling {@link Session#setConfig(SessionConfig)}method.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 *
 * @see Session
 */
public class SessionConfig implements SessionConfigMBean {
    private int idleTime;
    private int connectTimeout;
    private int writeTimeout;
    private int maxQueuedWriteCount;
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    /**
     * Creates a new instance with default settings.
     */
    public SessionConfig() {
    }

    /**
     * Returns connect timeout in seconds.
     */
    public int getConnectTimeout() {
        return connectTimeout / 1000;
    }

    /**
     * Returns connect timeout in milliseconds.
     */
    public int getConnectTimeoutInMillis() {
        return connectTimeout;
    }

    /**
     * Sets connect timeout in seconds. Connection attempt will automatically
     * fail and {@link ConnectException}will be thrown if the specified time
     * passes. Default value is <code>0</code> (disabled).
     *
     * @throws IllegalArgumentException
     *             if the specified timeout is too big or less than
     *             <code>0</code>.
     */
    public void setConnectTimeout(int connectTimeout) {
        Check.timeInSeconds(connectTimeout, "connecTimeout");
        this.connectTimeout = connectTimeout * 1000;
    }

    /**
     * Returns the idle time of this session in seconds.
     */
    public int getIdleTime() {
        return idleTime / 1000;
    }

    /**
     * Returns the idle time of this session in milliseconds.
     */
    public int getIdleTimeInMillis() {
        return idleTime;
    }

    /**
     * Sets the idle time of this session in seconds. If there was no I/O for
     * <code>idleTime</code> seconds, it will generate
     * <code>sessionIdle</code> event. Specify <code>0</code> to disable.
     *
     * @throws IllegalArgumentException
     *             if the specified time is too big or less tnan 0
     */
    public void setIdleTime(int idleTime) {
        Check.timeInSeconds(idleTime, "idleTime");
        this.idleTime = idleTime * 1000;
    }

    /**
     * Returns the maximum number of remaining write requests which were queued
     * by {@link Session#write(Message)}. If the number of remaining write
     * requests exceeds this value, {@link Session#write(Message)}method will
     * block. The default value is <code>0</code> (disabled).
     */
    public int getMaxQueuedWriteCount() {
        return maxQueuedWriteCount;
    }

    /**
     * Sets the maximum number of remaining write requests which were queued by
     * {@link Session#write(Message)}. If the number of remaining write
     * requests exceeds this value, {@link Session#write(Message)}method will
     * block. The default value is <code>0</code> (disabled).
     */
    public void setMaxQueuedWriteCount(int newLimit) {
        if (newLimit < 0) {
            throw new IllegalArgumentException("queued write limit: "
                                               + newLimit);
        }

        this.maxQueuedWriteCount = newLimit;
    }

    /**
     * Returns write timeout in seconds. I/O thread will throw a
     * {@link java.net.SocketTimeoutException}if it takes too long to flush the
     * write buffer. This is useful dropping too slow clients. Default value is
     * <code>0</code> (disabled).
     *
     * @return the write timeout, <code>0</code> if disabled.
     */
    public int getWriteTimeout() {
        return writeTimeout / 1000;
    }

    /**
     * Returns write timeout in milliseconds. I/O thread will throw a
     * {@link java.net.SocketTimeoutException}if it takes too long to flush the
     * write buffer. This is useful dropping too slow clients. Default value is
     * <code>0</code> (disabled).
     *
     * @return the write timeout, <code>0</code> if disabled.
     */
    public int getWriteTimeoutInMillis() {
        return writeTimeout;
    }

    /**
     * Sets write timeout in seconds. I/O thread will throw a
     * {@link java.net.SocketTimeoutException}if it takes too long to flush the
     * write buffer. This is useful dropping too slow clients. Default value is
     * <code>0</code> (disabled).
     *
     * @throws IllegalArgumentException
     *             if the specified value is less than <code>0</code>.
     */
    public void setWriteTimeout(int writeTimeout) {
        Check.timeInSeconds(writeTimeout, "writeTimeout");
        this.writeTimeout = writeTimeout * 1000;
    }

    /**
     * Returns the {@link ByteOrder}of {@link ByteBuffer}s that are passed to
     * {@link Message}s. The default value is {@link ByteOrder#BIG_ENDIAN}.
     */
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     * Sets the {@link ByteOrder}of {@link ByteBuffer}s that are passed to
     * {@link Message}s. Changing the order does not affect currently
     * communicating sessions. The default value is {@link ByteOrder#BIG_ENDIAN}.
     */
    public void setByteOrder(ByteOrder byteOrder) {
        if (byteOrder == null) {
            throw new NullPointerException("byteOrder");
        }

        this.byteOrder = byteOrder;
    }
}
