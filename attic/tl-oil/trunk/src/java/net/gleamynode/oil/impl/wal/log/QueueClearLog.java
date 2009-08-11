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
 * @(#) $Id: QueueClearLog.java 42 2004-11-23 06:26:38Z trustin $
 */
package net.gleamynode.oil.impl.wal.log;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 42 $, $Date: 2004-11-23 15:26:38 +0900 (화, 23 11월 2004) $
 */
public class QueueClearLog extends QueueLog {
    static final long serialVersionUID = -6298052908494691445L;

    public QueueClearLog() {
    }

    public QueueClearLog(int queueId) {
        super(queueId);
    }

    public void readExternal(ObjectInput in)
                      throws IOException, ClassNotFoundException {
        queueId = in.readInt();

        if (traceEnabled)
            System.out.println("QueueClear: " + queueId);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(queueId);
    }
}
