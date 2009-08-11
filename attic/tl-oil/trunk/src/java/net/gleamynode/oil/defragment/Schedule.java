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
 * @(#) $Id: Schedule.java 40 2004-11-23 05:22:49Z trustin $
 */
package net.gleamynode.oil.defragment;


/**
 * Represents a schedule for database defragmentation.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 40 $, $Date: 2004-11-23 14:22:49 +0900 (화, 23 11월 2004) $
 */
public interface Schedule {
    /**
     * Returns millis time of next defragmentation.
     */
    long nextScheduledTime();

    /**
     * Returns <code>true</code> if this schedule is periodic.  If periodic,
     * {@link ScheduledDefragmentor} invokes {@link #nextScheduledTime()} periodically
     * to defragment database periodically.  If not periodic, it is a one-time
     * schedule and {@link ScheduledDefragmentor} will discard this schedule after
     * defragmentation.
     */
    boolean isPeriodic();
}
