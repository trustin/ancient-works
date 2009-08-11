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
package net.gleamynode.netty2;


/**
 * Represents the type of the event.
 *
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 * @author Trustin Lee (http://gleamynode.net/dev/)
 */
public class EventType {
    public static final EventType CONNECTED = new EventType("CONNECTED");
    public static final EventType CONNECTION_TIMEOUT =
        new EventType("CONNECTION_TIMEOUT");
    public static final EventType DISCONNECTED = new EventType("DISCONNECTED");
    public static final EventType READY_TO_READ =
        new EventType("READY_TO_READ");
    public static final EventType READY_TO_WRITE =
        new EventType("READY_TO_WRITE");
    public static final EventType IDLE = new EventType("IDLE");
    public static final EventType CLOSE_REQUEST =
        new EventType("CLOSE_REQUEST");
    public static final EventType RECEIVED = new EventType("RECEIVED");
    public static final EventType SENT = new EventType("SENT");
    public static final EventType EXCEPTION = new EventType("EXCEPTION");
    static final EventType FEWER_THREADS = new EventType("FEWER_THREADS");
    private final String desc;

    private EventType(String desc) {
        this.desc = desc;
    }

    public String toString() {
        return desc;
    }
}
