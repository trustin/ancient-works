//
// Copyright (C) gleamynode.net. All rights reserved.
//
// This software is published under the terms of the GNU Lesser General 
// Public License version 2.1, a copy of which has been included with this 
// distribution in the LICENSE file.
//
package net.gleamynode.netty;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * <p>
 * A <code>MessageSocketRunner</code> which features thread pooling.
 * It has three configuration value; <code>spareThreads</code>,
 * <code>maxThreads</code>, <code>maxSockets</code>.
 * If the number of active sockets exceeds <code>maxThreads</code>,
 * one thread will drive many <code>MessageSocket</code>s to recude
 * thread switching overhead.
 * </p>
 *
 * @author  Trustin Lee
 * @version 1.0
 */
public class ThreadPooledMessageSocketRunner implements MessageSocketRunner {
    private static int threadID = 0;
    public static final int DEFAULT_MAX_THREADS = 128;
    public static final int DEFAULT_SPARE_THREADS = 16;
    public static final int DEFAULT_MAX_SOCKETS = 512;
    private HashSet threadTab[];
    private int spareThreads;
    private int maxThreads;
    private int idleTimeout;
    private Object counterMutex;
    private volatile int nThreads;
    private volatile int nSockets;
    private volatile int idleThreads;
    private boolean requestedStop;
    
    /**
     * Constructs a new <code>ThreadPooledMessageSocketRunner</code> with
     * the specified parameters.
     */
    public ThreadPooledMessageSocketRunner(int spareThreads, int maxThreads, int maxSockets) {
        counterMutex = new Object();
        nThreads = 0;
        nSockets = 0;
        idleThreads = 0;
        requestedStop = false;
        init(spareThreads, maxThreads, maxSockets);
    }
    
    private void addServerThread() {
        ServerThread serverthread = new ServerThread();
        serverthread.start();
        threadTab[0].add(serverthread);
    }
    
    /**
     * Returns the timeout value of idle threads.
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }
    
    /**
     * Returns the maxmum number of threads possible.
     */
    public int getMaxThreads() {
        return maxThreads;
    }
    
    /**
     * Returns the number of spare threads.
     */
    public int getSpareThreads() {
        return spareThreads;
    }
    
    private void init(int spareThreads, int maxThreads, int maxSockets) {
        if (maxThreads <= 0)
            throw new IllegalArgumentException("maxThreads is not positive");
        if (spareThreads <= 0)
            throw new IllegalArgumentException("spareThreads is not positive");
        if (maxSockets <= 0)
            throw new IllegalArgumentException("maxSockets is not positive");
        this.spareThreads = spareThreads;
        this.maxThreads = maxThreads;
        setIdleTimeout(0);
        threadTab = new HashSet[maxSockets / maxThreads + 1 + (maxSockets % maxThreads != 0 ? 1 : 0)];
        int i;
        for (i = 0; i < threadTab.length; i++) {
            threadTab[i] = new HashSet();
        }
        
        for (i = 0; i < spareThreads; i++) {
            addServerThread();
        }
    }
    
    public synchronized boolean process(MessageSocket messagesocket) {
        if (messagesocket == null)
            throw new NullPointerException("msocket is null");
        if (requestedStop)
            return false;
        boolean flag = false;
        int i = threadTab.length - 1;
        synchronized (threadTab) {
            for (int j = 0; j < i; j++) {
                Iterator iterator = threadTab[j].iterator();
                if (!iterator.hasNext())
                    continue;
                ServerThread serverthread = (ServerThread)iterator.next();
                synchronized (serverthread.handlingSocketsMutex) {
                    iterator.remove();
                    serverthread.add(messagesocket);
                    serverthread.handlingSockets++;
                    threadTab[serverthread.handlingSockets].add(serverthread);
                }
                flag = true;
                break;
            }
            
        }
        return flag;
    }
    
    private void setIdleTimeout(int newIdleTimeout) {
        idleTimeout = newIdleTimeout;
    }
    
    public synchronized void stop() {
        requestedStop = true;
    }
    
    private class ServerThread extends Thread {
        private LinkedList sockets;
        private LinkedList addedSockets;
        Object handlingSocketsMutex;
        int handlingSockets;
        
        public ServerThread() {
            super("PMSocket" + ThreadPooledMessageSocketRunner.threadID++);
            sockets = new LinkedList();
            addedSockets = new LinkedList();
            handlingSocketsMutex = new Object();
            handlingSockets = 0;
            synchronized (counterMutex) {
                idleThreads++;
            }
            nThreads++;
        }
        
        public final void run() {
            for (;;) {
                if (sockets.isEmpty()) {
                    if (requestedStop)
                        break;
                    
                    boolean gotJob;
                    int i;
                    if (idleTimeout <= 0)
                        i = 100;
                    else
                        i = idleTimeout;
                    gotJob = acceptSocket(i);
                    if (!gotJob) {
                        synchronized (threadTab[0]) {
                            synchronized (counterMutex) {
                                if (spareThreads < idleThreads && addedSockets.size() <= 0) {
                                    idleThreads--;
                                    threadTab[0].remove(this);
                                    break;
                                }
                            }
                        }
                        continue;
                    }
                }
                
                synchronized (threadTab[0]) {
                    synchronized (counterMutex) {
                        idleThreads--;
                        if (spareThreads > idleThreads && nThreads < maxThreads)
                            addServerThread();
                    }
                }
                
                do {
                    if (!addedSockets.isEmpty()) {
                        synchronized (addedSockets) {
                            nSockets += addedSockets.size();
                            sockets.addAll(addedSockets);
                            addedSockets.clear();
                        }
                    }
                    for (java.util.ListIterator listiterator = sockets.listIterator();
                    listiterator.hasNext();
                    ) {
                        boolean moreRCV, moreSND;
                        MessageSocket messagesocket;
                        messagesocket = (MessageSocket) listiterator.next();
                        for ( ;; ) {
                            moreSND = messagesocket.processMessageSent();
                            moreRCV = messagesocket.processMessageReceived();
                            if ( !moreSND && !moreRCV ) break;
                            Thread.yield();
                        }
                        
                        if (messagesocket.isClosed()) {
                            nSockets--;
                            listiterator.remove();
                            synchronized (threadTab) {
                                synchronized (handlingSocketsMutex) {
                                    threadTab[handlingSockets].remove(this);
                                    handlingSockets--;
                                    threadTab[handlingSockets].add(this);
                                }
                            }
                        }
                        else {
                            messagesocket.processIdle();
                            messagesocket.processTimer();
                            try {
                                Thread.sleep(10L);
                            }
                            catch (InterruptedException _ex) {
                            }
                        }
                    }
                    
                    try {
                        Thread.sleep(20L);
                    }
                    catch (InterruptedException _ex) {
                    }
                }
                while (!sockets.isEmpty());
                
                synchronized (counterMutex) {
                    idleThreads++;
                }
            }
            nThreads--;
            return;
        }
        
        final synchronized void add(MessageSocket messagesocket) {
            if (messagesocket == null)
                throw new NullPointerException("messageSocket is null");
            synchronized (addedSockets) {
                addedSockets.add(messagesocket);
            }
            notify();
        }
        
        final synchronized boolean acceptSocket(int timeout) {
            if (addedSockets.isEmpty()) {
                try {
                    wait(timeout);
                }
                catch (InterruptedException _ex) {
                }
                return !addedSockets.isEmpty();
            }
            else {
                return true;
            }
        }
    }
}