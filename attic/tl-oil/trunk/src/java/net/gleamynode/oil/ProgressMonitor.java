/*
 * @(#) $Id: ProgressMonitor.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil;

/**
 * Monitors database open / defragment / recovery progress.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 */
public interface ProgressMonitor {
    public void onStart(long total);
    public void onProgress(long current);
    public void onEnd();
}
