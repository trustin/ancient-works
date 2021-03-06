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
 * @(#) $Id: IndexRemoveLog.java 42 2004-11-23 06:26:38Z trustin $
 */
package net.gleamynode.oil.impl.wal.log;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 42 $, $Date: 2004-11-23 15:26:38 +0900 (화, 23 11월 2004) $
 */
public class IndexRemoveLog extends IndexLog {
    static final long serialVersionUID = -4778538822963874598L;
    private Object key;

    /**
     * Creates a new instance.
     */
    public IndexRemoveLog() {
    }

    public IndexRemoveLog(int indexId, Object key) {
        super(indexId);
        this.key = key;
    }

    public Object getKey() {
        return key;
    }

    public void readExternal(ObjectInput in)
                      throws IOException, ClassNotFoundException {
        indexId = in.readInt();
        key = in.readObject();

        if (traceEnabled)
            System.out.println("IndexRemove: " + indexId);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(indexId);
        out.writeObject(key);
    }
}
