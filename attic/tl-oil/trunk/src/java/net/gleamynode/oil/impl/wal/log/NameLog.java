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
 * @(#) $Id: NameLog.java 42 2004-11-23 06:26:38Z trustin $
 */
package net.gleamynode.oil.impl.wal.log;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 42 $, $Date: 2004-11-23 15:26:38 +0900 (화, 23 11월 2004) $
 */
public class NameLog extends Log {
    static final long serialVersionUID = 6147817295400048251L;
    private int nameId;
    private String name;

    public NameLog() {
        super();
    }

    public NameLog(int nameId, String name) {
        this.nameId = nameId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getNameId() {
        return nameId;
    }

    public void readExternal(ObjectInput in)
                      throws IOException, ClassNotFoundException {
        nameId = in.readInt();
        name = in.readUTF();

        if (traceEnabled)
            System.out.println("NameLog: " + nameId + ", " + name);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(nameId);
        out.writeUTF(name);
    }
}
