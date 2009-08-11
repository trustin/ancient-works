//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

/**
 * <p>
 * An interface that all classes that drives <code>MessageSocket</code>s'
 * communication flow should implement.
 * </p>
 * <p>
 * The runner must call <code>MessageSocket</code>'s all
 * <code>processXXX()</code> methods frequently to make the socket work
 * fine. See the default implementations' source code for detail.
 * </p>
 *
 * @author  Trustin Lee
 * @version 1.0
 */
public interface MessageSocketRunner {
    /**
     * Request this runner drive the specified <code>MessageSocket</code>.
     *
     * @return false if the request failed. (e.g. the runner is stopping)
     */
    public abstract boolean process(MessageSocket messagesocket);
    
    /**
     * Request this runner stop accepting <code>MessageSocket</code>s.
     * After all connections are closed, this runner's thread will finish
     * smoothly.
     */
    public abstract void stop();
}