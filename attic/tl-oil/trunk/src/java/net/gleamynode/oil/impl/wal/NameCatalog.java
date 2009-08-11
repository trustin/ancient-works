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
 * @(#) $Id: NameCatalog.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.gleamynode.oil.OilException;
import net.gleamynode.oil.RunRecoveryException;
import net.gleamynode.oil.impl.wal.log.NameLog;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (월, 03  1월 2005) $
 */
class NameCatalog {
    private final WalDatabase parent;
    private final LogStore store;
    private final Map name2id = new HashMap();
    private final Map id2name = new HashMap();
    private int nextId = 0;

    public NameCatalog(WalDatabase parentDb) {
        this.parent = parentDb;
        store = parentDb.getStore();
    }

    synchronized void close() {
        name2id.clear();
        id2name.clear();
    }
    
    public int size() {
        return nextId;
    }

    public String getName(int id) {
        parent.acquireSharedLock();

        try {
            String name = (String) id2name.get(new Integer(id));

            if (name == null) {
                throw new RunRecoveryException();
            } else {
                return name;
            }
        } finally {
            parent.releaseSharedLock();
        }
    }

    public int getId(String name) {
        parent.acquireSharedLock();

        try {
            Integer id = (Integer) name2id.get(name);

            if (id == null) {
                return newPair(name);
            }

            return id.intValue();
        } finally {
            parent.releaseSharedLock();
        }
    }

    private synchronized int newPair(String name) {
        Integer id = (Integer) name2id.get(name);

        if (id != null)
            return id.intValue();

        int newId;

        if ((nextId < 0) || (nextId >= Constants.MAX_NAMES)) {
            throw new OilException("name catalog is full");
        }

        newId = nextId++;

        id = new Integer(newId);
        name2id.put(name, id);
        id2name.put(id, name);
        store.write(new NameLog(newId, name));

        return newId;
    }

    void read(NameLog log) {
        String name = log.getName();
        Integer id = new Integer(log.getNameId());

        if (name2id.containsKey(name))
            throw new RunRecoveryException("Duplicate name: " + name);

        if (id2name.containsKey(id))
            throw new RunRecoveryException("Duplicate nameId: " + id);

        name2id.put(name, id);
        id2name.put(id, name);
        if (nextId <= id.intValue())
            nextId = id.intValue() + 1;
    }

    void writeAll(LogStore store, Progress progress) {
        Iterator it = name2id.entrySet().iterator();

        while (it.hasNext()) {
            Entry e = (Entry) it.next();
            store.write(new NameLog(((Integer) e.getValue()).intValue(),
                                    (String) e.getKey()));
            progress.increase(1);
        }
    }
}
