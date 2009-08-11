/*
 * @(#) $Id: Progress.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import net.gleamynode.oil.ProgressMonitor;

/**
 * TODO Document me.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 */
class Progress {
    private long current;

    private final long total;

    private final ProgressMonitor monitor;

    Progress(long total, ProgressMonitor monitor) {
        this.total = total;
        this.monitor = monitor;
    }

    long getCurrent() {
        return current;
    }

    void increase(int delta) {
        this.current += delta;
        fireOnProgress();
    }

    void setCurrent(long current) {
        if (this.current == current)
            return;

        this.current = current;
        fireOnProgress();
    }
    
    void fireOnStart() {
        if (monitor != null) {
            try {
                monitor.onStart(total);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    void fireOnProgress() {
        if (monitor != null) {
            try {
                monitor.onProgress(current);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
    
    void fireOnEnd() {
        if (monitor != null) {
            try {
                monitor.onEnd();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}