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
package net.gleamynode.netty2;


/**
 * An event listener that handles session events dispatched from
 * {@link EventDispatcher}. Implement the protocol workflow by implementing
 * this interface.
 * <p>
 * There are 6 session events available:
 * <ul>
 * <li><code><strong>connectionEstablished</strong></code>: Connected to
 * the {@link java.net.SocketAddress}at {@link Session}successfully.</li>
 * <li><code><strong>connectionClosed</strong></code>: The connection is
 * closed. This event always takes place after {@link java.io.IOException}
 * (except {@link java.net.ConnectException}) is thrown because the connection
 * is automatically closed.</li>
 * <li><code><strong>messageReceived</strong></code>: The message is read
 * from the incoming channel data.</li>
 * <li><code><strong>messageSent</strong></code>: The message is sent to
 * the socket channel.</li>
 * <li><code><strong>sessionIdle</strong></code>: The session is idle for
 * {@link Session}.<code>idleTime</code> seconds</li>
 * <li><code><strong>exceptionCaught</strong></code>: An exception has been
 * thrown while doing I/O or processing business logic in
 * {@link SessionListener}.</li>
 * </ul>
 *
 * @author Trustin Lee (http://gleamynode.net/dev/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 */
public interface SessionListener {
    /**
     * Invoked when the connection is established.
     */
    void connectionEstablished(Session session);

    /**
     * Invoked when the connection is closed.
     */
    void connectionClosed(Session session);

    /**
     * Invoked when a message has arrived.
     */
    void messageReceived(Session session, Message message);

    /**
     * Invoked when a message has been sent.
     */
    void messageSent(Session session, Message message);

    /**
     * Invoked when the session is idle for predefined amount of time.
     */
    void sessionIdle(Session session);

    /**
     * Invoked when an exception is caught while communicating.
     */
    void exceptionCaught(Session session, Throwable cause);
}
