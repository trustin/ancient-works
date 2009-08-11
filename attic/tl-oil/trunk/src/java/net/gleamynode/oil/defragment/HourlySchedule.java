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
 * @(#) $Id: HourlySchedule.java 65 2005-01-03 04:52:18Z trustin $
 */
package net.gleamynode.oil.defragment;

import java.util.Calendar;


/**
 * An hourly defragmentation schedule.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 65 $, $Date: 2005-01-03 13:52:18 +0900 (월, 03  1월 2005) $
 */
public class HourlySchedule implements Schedule {
    private final int minute;

    /**
     * Creates a new schedules which defragments every specified <code>minute</code>.
     */
    public HourlySchedule(int minute) {
        this.minute = minute;
    }

    public long nextScheduledTime() {
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.before(now)) {
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }

        return cal.getTime().getTime();
    }

    public boolean isPeriodic() {
        return true;
    }
}
