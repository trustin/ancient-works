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
 * @(#) $Id: OneTimeSchedule.java 40 2004-11-23 05:22:49Z trustin $
 */
package net.gleamynode.oil.defragment;


/**
 * A one-time schedule which runs defragmentation at the specified time.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 40 $, $Date: 2004-11-23 14:22:49 +0900 (화, 23 11월 2004) $
 */
public class OneTimeSchedule implements Schedule {
    private final long nextScheduledTime;

    /**
     * Creates a new one-time schedule which runs defragmentation at the
     * specified millis time.
     */
    public OneTimeSchedule(long nextScheduledTime) {
        this.nextScheduledTime = nextScheduledTime;
    }

    public long nextScheduledTime() {
        return nextScheduledTime;
    }

    public boolean isPeriodic() {
        return false;
    }
}
