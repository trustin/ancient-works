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
 * @(#) $Id: SyncUtil.java 32 2004-11-09 14:37:16Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import EDU.oswego.cs.dl.util.concurrent.Sync;


/**
 * TODO Insert type comment.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 32 $, $Date: 2004-11-09 23:37:16 +0900 (화, 09 11월 2004) $
 */
class SyncUtil {
    private SyncUtil() {
    }

    public static final void acquire(Sync sync) {
        boolean wasInterrupted = Thread.interrupted(); // record and clear

        for (;;) {
            try {
                sync.acquire(); // or any other method throwing

                // InterruptedException
                break;
            } catch (InterruptedException ex) { // re-interrupted; try again
                wasInterrupted = true;
            }
        }

        if (wasInterrupted) { // re-establish interrupted state
            Thread.currentThread().interrupt();
        }
    }
}
