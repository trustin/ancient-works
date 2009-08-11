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
 * @(#) $Id: CompactObjectInputStream.java 32 2004-11-09 14:37:16Z trustin $
 */
package net.gleamynode.oil.impl.wal.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 32 $, $Date: 2004-11-09 23:37:16 +0900 (화, 09 11월 2004) $
 */
class CompactObjectInputStream extends ObjectInputStream {
    private final ClassCatalog classCatalog;

    public CompactObjectInputStream(ClassCatalog classCatalog, InputStream in)
                             throws IOException, SecurityException {
        super(in);
        this.classCatalog = classCatalog;
    }

    protected ObjectStreamClass readClassDescriptor()
                                             throws IOException, 
                                                    ClassNotFoundException {
        int id = readInt();
        ObjectStreamClass desc = classCatalog.getDescriptor(id);

        if (desc == null) {
            throw new IOException("unknown class descriptor: " + id);
        } else {
            return desc;
        }
    }

    protected void readStreamHeader() {
    }
}
