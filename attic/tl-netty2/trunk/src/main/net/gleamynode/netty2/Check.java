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
 * @(#) $Id: Check.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2;


/**
 * Provides common check methods.
 *
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 */
class Check {
    private Check() {
    }

    public static void notNull(Object o, String name) {
        if (o == null) {
            throw new NullPointerException(name);
        }
    }

    public static void threadPoolSize(int newSize) {
        if (newSize <= 0) {
            throw new IllegalArgumentException("thread pool size must be positive");
        }
    }

    public static void threadPriority(int priority) {
        if (priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException("thread priority cannot be less than "
                                               + Thread.MIN_PRIORITY);
        }

        if (priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("thread priority cannot be greater than "
                                               + Thread.MAX_PRIORITY);
        }
    }

    public static void timeInSeconds(int time, String name) {
        if (time > (Integer.MAX_VALUE / 1000)) {
            throw new IllegalArgumentException(name + " is too big: " + time);
        }

        if (time < 0) {
            throw new IllegalArgumentException(name + " is less than 0: "
                                               + time);
        }
    }
}
