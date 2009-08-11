/*
 * @(#) $Id: ExceptionLoggingMonitor.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2;

import java.lang.reflect.Method;

import java.text.SimpleDateFormat;

import java.util.Date;


/**
 * An {@link ExceptionMonitor} which logs uncaught exceptions.
 * It tries to log an exception using Apache Jakarta Commons Logging first.
 * If failed, it will write it out to stderr.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 */
public class ExceptionLoggingMonitor implements ExceptionMonitor {
    private static final ExceptionLoggingMonitor instance =
        new ExceptionLoggingMonitor();
    private static final Object log;
    private static final Method errorMethod;

    static {
        Object tempLog = null;
        Method tempErrorMethod = null;

        try {
            Class logCls = Class.forName("org.apache.commons.logging.Log");
            Class logFactoryCls =
                Class.forName("org.apache.commons.logging.LogFactory");
            Method getLogMethod =
                logFactoryCls.getMethod("getLog", new Class[] { String.class });
            tempLog =
                getLogMethod.invoke(null,
                                    new Object[] { ExceptionLoggingMonitor.class.getPackage()
                                                                                .getName() });
            tempErrorMethod =
                logCls.getMethod("error",
                                 new Class[] { Object.class, Throwable.class });
        } catch (Exception e) {
            tempLog = null;
            tempErrorMethod = null;
        }

        log = tempLog;
        errorMethod = tempErrorMethod;
    }

    /**
     * Creates a new instance.
     */
    protected ExceptionLoggingMonitor() {
    }

    protected static void logError(String message, Throwable cause) {
        if (log == null) {
            logToStderr(message, cause);
        } else {
            try {
                errorMethod.invoke(log, new Object[] { message, cause });
            } catch (Exception e) {
                logToStderr(message, cause);
            }
        }
    }

    private static void logToStderr(String message, Throwable cause) {
        SimpleDateFormat sdf =
            new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] '[ERROR]' ");

        synchronized (System.err) {
            System.err.print(sdf.format(new Date()));
            System.err.println(message);
            cause.printStackTrace(System.err);
        }
    }

    /**
     * Returns the instance.
     */
    public static ExceptionLoggingMonitor getInstance() {
        return instance;
    }

    public void exceptionCaught(Throwable exception) {
        logError("Unexpected exception.", exception);
    }
}
