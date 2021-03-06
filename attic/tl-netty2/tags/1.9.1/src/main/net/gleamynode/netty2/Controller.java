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
 * @(#) $Id: Controller.java 4 2005-04-18 03:04:09Z trustin $
 */
package net.gleamynode.netty2;


/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 4 $, $Date: 2005-04-18 12:04:09 +0900 $
 */
abstract class Controller {
    public abstract void setThreadPriority(int newPriority);

    public abstract void init();

    public abstract void startDestroy();

    public abstract void finishDestroy();

    public abstract void addSession(Session session);

    public abstract boolean isProcessable(Event event);

    public abstract void processEvent(Event event);
}
