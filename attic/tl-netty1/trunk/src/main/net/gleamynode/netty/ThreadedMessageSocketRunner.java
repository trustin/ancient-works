//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

import java.util.HashSet;
import java.util.Iterator;

/** 
 * <p>
 * A <code>MessageSocketRunner</code> that creates one thread per
 * one active <code>MesageSocket</code> and drives them.
 * </p>
 * 
 * <h2>CHANGELOG</h2>
 * <h3>1.2</h3>
 * <p><ul>
 *   <li>Sleeps less if timer is set.</li>
 *   <li>Updated license statement.</li>
 * </ul></p>
 * 
 * <h3>1.1a</h3>
 * <p><ul>
 *   <li>Added   <code>java.util.HashSet</code> object to manage the created
 * threads.</li>
 *   <li>Revised {@link #stop()} method to stop all created threads.</li>
 * </ul></p>
 * 
 * <h3>1.1</h3>
 * <p><ul>
 *   <li>Fixed a memory leak bug caused by unused Vecor object.</li>
 * </ul></p>
 *
 * @author  Trustin Lee
 * @version 1.2
 */
public class ThreadedMessageSocketRunner implements MessageSocketRunner {
    private boolean requestedStop = false;
    
    private HashSet threads = new HashSet();
    
    /**
     * Constructs a new <code>ThreadedMessageSocketRunner</code>.
     */
    public ThreadedMessageSocketRunner() {
    }
    
    public boolean process(MessageSocket messagesocket) {
        if ( requestedStop ) return false;
        
        MessageSocketThread messagesocketthread = new MessageSocketThread(messagesocket);
        synchronized ( threads ) {
			threads.add(messagesocketthread);
        }
        messagesocketthread.start();
        return true;
    }
    
    public void stop() {
    	Iterator it;
        requestedStop = true;
		synchronized ( threads ) {
			it = threads.iterator();
			while ( it.hasNext() ) {
				((MessageSocketThread)it.next()).ms.close();
				it.remove();
			}
		}
    }
    
    private class MessageSocketThread extends Thread {
        private MessageSocket ms;

        MessageSocketThread(MessageSocket messagesocket) {
            ms = messagesocket;
        }
        
        public final void run() {
            boolean moreRCV, moreSND;
            while (!ms.isClosed()) {
                
                for ( ;; ) {
                    moreRCV = ms.processMessageReceived();
                    moreSND = ms.processMessageSent();
                    if ( !moreRCV && !moreSND ) {
                        break;
                    }
                    Thread.yield();
                }
                
                if (ms.processIdle() && !ms.isTimerSet() ) {
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException _ex) { }
                }
                
                ms.processTimer();
                
                try {
                    Thread.sleep(10L);
                }
                catch (InterruptedException _ex) { }
            }
            
            synchronized ( threads ) { 
				threads.remove(this);
            }
        }
    }
}

