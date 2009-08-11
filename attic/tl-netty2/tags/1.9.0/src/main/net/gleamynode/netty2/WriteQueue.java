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
 * @(#) $Id: WriteQueue.java 19 2005-04-19 15:29:55Z trustin $
 */
package net.gleamynode.netty2;


/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 */
class WriteQueue extends Queue {
    private int waitingForPop;
    private int maxSize;

    public WriteQueue(int initialCapacity) {
        super(initialCapacity);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public synchronized void open() {
        super.open();
    }

    public synchronized void close() {
        super.close();

        if (waitingForPop > 0) {
            notifyAll();
        }
    }

    public synchronized Object first() {
        return super.first();
    }

    public synchronized Object pop() {
        Object result = super.pop();

        if ((waitingForPop > 0) && (size() < maxSize)) {
            notifyAll();
        }

        return result;
    }

    public boolean push(Object obj) {
        return push(obj, Long.MAX_VALUE);
    }

    public synchronized boolean push(Object obj, long timeout) {
        if (maxSize > 0) {
            waitingForPop++;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;

            while (size() >= maxSize) {
                try {
                    wait(timeout - (currentTime - startTime));
                } catch (InterruptedException e) {
                }

                currentTime = System.currentTimeMillis();

                if ((currentTime - startTime) >= timeout) {
                    return false;
                }
            }

            waitingForPop--;
        }

        return super.push(obj);
    }
}
