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
 * @(#) $Id: ScheduledDefragmentor.java 42 2004-11-23 06:26:38Z trustin $
 */
package net.gleamynode.oil.defragment;

import java.util.Iterator;
import java.util.Set;

import net.gleamynode.oil.Database;

import org.apache.commons.collections.map.IdentityMap;
import org.apache.commons.collections.set.MapBackedSet;
import org.apache.commons.lang.Validate;


/**
 * A utility class which calls {@link Database#defragment()} periodically.
 * Just call {@link #schedule(Schedule)} to schedule defragmentation.
 * <p>
 * One dedicated worker thread per instance will be created and started when
 * you schedule defragmentation at first.  You can cancel all schedules by
 * calling {@link #cancel()}, but you'll have to recreate the instance because
 * it is not reusable just like {@link java.util.Timer}.
 * <p>
 * Example:
 * <pre>
 * Database db = ...;
 * ScheduledDefragmentor defragger = new ScheduledDefragmentor(db);
 * 
 * // Defrag the database every 5 o'clock am.
 * defragger.schedule(new HourlySchedule(5));
 * </pre>
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 42 $, $Date: 2004-11-23 15:26:38 +0900 (화, 23 11월 2004) $
 */
public class ScheduledDefragmentor {
    private static final String DEFAULT_THREAD_NAME = "ScheduledDefragmentor";
    private static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;
    private final Database database;
    private final Set schedules = MapBackedSet.decorate(new IdentityMap());
    private final Worker worker = new Worker();
    private boolean timeToStop;

    /**
     * Creates a new instance which defragments the specified database.
     */
    public ScheduledDefragmentor(Database database) {
        Validate.notNull(database);
        this.database = database;
    }

    /**
     * Schedule the specified {@link Schedule} whicn tells when to
     * defragment the database.  If the worker thread is not started yet,
     * it will be started automatically and you can stop it by calling
     * {@link #cancel()}.
     */
    public synchronized void schedule(Schedule schedule) {
        Validate.notNull(schedule);

        if (!worker.isAlive()) {
            try {
                worker.start();
            } catch (IllegalThreadStateException e) {
                throw new IllegalStateException();
            }
        }

        schedules.add(schedule);
        notify();
    }

    /**
     * Cancels the specified schedule.
     */
    public synchronized void unschedule(Schedule schedule) {
        Validate.notNull(schedule);
        schedules.remove(schedule);
    }

    /**
     * Cancels all registered schedules and stops the internal worker thread.
     * Invoking {@link #schedule(Schedule)} after calling this method won't work;
     * {@link ScheduledDefragmentor} is not reusable.
     */
    public void cancel() {
        timeToStop = true;
        worker.interrupt();

        while (worker.isAlive()) {
            try {
                worker.join();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Returns the name of the worker thread.
     */
    public String getThreadName() {
        return worker.getName();
    }

    /**
     * Sets the name of the worker thread.
     */
    public void setThreadName(String name) {
        worker.setName(name);
    }

    /**
     * Returns the priority of the worker thread.
     */
    public int getThreadPriority() {
        return worker.getPriority();
    }

    /**
     * Sets the priority of the worker thread.
     */
    public void setThreadPriority(int priority) {
        worker.setPriority(priority);
    }

    private class Worker extends Thread {
        private Worker() {
            super(DEFAULT_THREAD_NAME);
            setPriority(DEFAULT_THREAD_PRIORITY);
        }

        public void run() {
            while (!timeToStop) {
                long currentTime = System.currentTimeMillis();
                Schedule chosenSchedule = null;
                long scheduledTime = Long.MAX_VALUE;

                synchronized (schedules) {
                    Iterator it = schedules.iterator();

                    while (it.hasNext()) {
                        Schedule s = (Schedule) it.next();
                        long time = s.nextScheduledTime();

                        if (time < scheduledTime) {
                            scheduledTime = time;
                            chosenSchedule = s;
                        }
                    }
                }

                if (chosenSchedule == null) {
                    synchronized (ScheduledDefragmentor.this) {
                        try {
                            ScheduledDefragmentor.this.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                } else {
                    if (!chosenSchedule.isPeriodic()) {
                        unschedule(chosenSchedule);
                    }

                    long delay = scheduledTime - currentTime;

                    if (delay > 0) {
                        try {
                            Thread.sleep(delay);

                            if (database.isOpen()) {
                                database.defragment();
                            }
                        } catch (InterruptedException e) {
                        } catch (IllegalStateException e) {
                            if (database.isOpen()) {
                                e.printStackTrace();

                                break;
                            }
                        }
                    } else {
                        try {
                            if (database.isOpen()) {
                                database.defragment();
                            }
                        } catch (IllegalStateException e) {
                            if (database.isOpen()) {
                                e.printStackTrace();

                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
