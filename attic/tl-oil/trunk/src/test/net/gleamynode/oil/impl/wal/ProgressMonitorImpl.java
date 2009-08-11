/*
 * @(#) $Id: ProgressMonitorImpl.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import net.gleamynode.oil.ProgressMonitor;

/**
 * Default {@link ProgressMonitor} implementation for testing.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 */
class ProgressMonitorImpl implements ProgressMonitor {
    public static final ProgressMonitor INSTANCE = new ProgressMonitorImpl();
    
    private ProgressMonitorImpl() {
    }

    public void onStart(long total) {
        System.out.print('[');
        System.out.print(total);
        System.out.print("] ");
        System.out.flush();
    }

    public void onProgress(long current) {
        System.out.print('.');
        System.out.flush();
    }

    public void onEnd() {
        System.out.println(" Done");
    }
}
