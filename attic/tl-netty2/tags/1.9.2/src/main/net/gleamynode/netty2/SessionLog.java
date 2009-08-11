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
 * @(#) $Id: SessionLog.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2;

import org.apache.commons.logging.Log;


/**
 * <a href="http://jakarta.apache.org/commons/logging/">Apache Jakarta Commons Logging</a> helper.
 * Log message format is '<code>[socketAddress] User message</code>'.
 *
 * You can use this class in two ways:
 *
 * <ul>
 * <li>Static:
 * <pre>
 * private static final Log log = LogFactory.getLog(MySessionListener.class);
 * ...
 *
 * public void messageReceived(Session s, Message m) {
 *     SessionLog.info(log, s, "Received: " + m);
 *     ...
 * }
 * </pre></li>
 * <li>Non-static (Please note that this code assumes <code>connectionEstablished</code> event is
 * always fired in prior to other events):
 * <pre>
 * private static final Log log = LogFactory.getLog(MySessionListener.class);
 *
 * public class MySessionInfo {
 *     private final SessionLog log;
 *     ...
 *
 *     public MySessionInfo(Session session) {
 *         log = new SessionLog(session, LogFactory.getLog(MySessionListener.class);
 *         ...
 *     }
 *
 *     public Log getLog() {
 *         return log;
 *     }
 *     ...
 * }
 *
 * public class MySessionListener implements SessionListener {
 *     public void connectionEstablished(Session s) {
 *         MySessionInfo info = new MySessionInfo(s);
 *         s.setAttachment(new MySessionInfo(s));
 *         info.getLog().info("Connection established.");
 *         ...
 *     }
 *
 *     public void messageReceived(Session s, Message m) {
 *         MySessionInfo info = (MySessionInfo) s.getAttachment();
 *         info.getLog().info("Received: " + m);
 *         ...
 *     }
 *     ...
 * }
 * </pre></li>
 * </ul>
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 */
public class SessionLog implements Log {
    private final Session session;
    private final Log log;

    public SessionLog(Session session, Log log) {
        Check.notNull(session, "session");
        Check.notNull(log, "log");
        this.session = session;
        this.log = log;
    }

    public static void trace(Log log, Session session, Object obj) {
        log.trace(getMessage(session, obj));
    }

    public static void trace(Log log, Session session, Object obj,
                             Throwable cause) {
        log.trace(getMessage(session, obj), cause);
    }

    public static void debug(Log log, Session session, Object obj) {
        log.debug(getMessage(session, obj));
    }

    public static void debug(Log log, Session session, Object obj,
                             Throwable cause) {
        log.debug(getMessage(session, obj), cause);
    }

    public static void info(Log log, Session session, Object obj) {
        log.info(getMessage(session, obj));
    }

    public static void info(Log log, Session session, Object obj,
                            Throwable cause) {
        log.info(getMessage(session, obj), cause);
    }

    public static void warn(Log log, Session session, Object obj) {
        log.warn(getMessage(session, obj));
    }

    public static void warn(Log log, Session session, Object obj,
                            Throwable cause) {
        log.warn(getMessage(session, obj), cause);
    }

    public static void error(Log log, Session session, Object obj) {
        log.error(getMessage(session, obj));
    }

    public static void error(Log log, Session session, Object obj,
                             Throwable cause) {
        log.error(getMessage(session, obj), cause);
    }

    public static void fatal(Log log, Session session, Object obj) {
        log.fatal(getMessage(session, obj));
    }

    public static void fatal(Log log, Session session, Object obj,
                             Throwable cause) {
        log.fatal(getMessage(session, obj), cause);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public void trace(Object obj) {
        trace(log, session, getMessage(session, obj));
    }

    public void trace(Object obj, Throwable cause) {
        trace(log, session, getMessage(session, obj), cause);
    }

    public void debug(Object obj) {
        debug(log, session, getMessage(session, obj));
    }

    public void debug(Object obj, Throwable cause) {
        debug(log, session, getMessage(session, obj), cause);
    }

    public void info(Object obj) {
        info(log, session, getMessage(session, obj));
    }

    public void info(Object obj, Throwable cause) {
        info(log, session, getMessage(session, obj), cause);
    }

    public void warn(Object obj) {
        warn(log, session, getMessage(session, obj));
    }

    public void warn(Object obj, Throwable cause) {
        warn(log, session, getMessage(session, obj), cause);
    }

    public void error(Object obj) {
        error(log, session, getMessage(session, obj));
    }

    public void error(Object obj, Throwable cause) {
        error(log, session, getMessage(session, obj), cause);
    }

    public void fatal(Object obj) {
        fatal(log, session, getMessage(session, obj));
    }

    public void fatal(Object obj, Throwable cause) {
        fatal(log, session, getMessage(session, obj), cause);
    }

    private static String getMessage(Session session, Object obj) {
        String addr = session.getSocketAddressString();
        String msg = String.valueOf(obj);
        StringBuffer buf = new StringBuffer(addr.length() + msg.length() + 3);
        buf.append('[');
        buf.append(addr);
        buf.append(']');
        buf.append(' ');
        buf.append(msg);
        return buf.toString();
    }
}
