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
 * @(#) $Id: ClassCatalog.java 32 2004-11-09 14:37:16Z trustin $
 */
package net.gleamynode.oil.impl.wal.store;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.RandomAccessFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.gleamynode.oil.OilException;


/**
 * °³Ã¼¸¦ ÀúÀå¼Ò¿¡ ÀúÀåÇÒ ¶§ °³Ã¼ ½ºÆ®¸²µéÀÌ ÀÐ°í ¾²´Â Å¬·¡½º µð½ºÅ©¸³ÅÍ¸¦ µû·Î ÀúÀåÇØ Ä«Å»·Î±×·Î ÀÌ¿ëÇÏ±â À§ÇÑ ÀúÀå¼Ò. µð½ºÅ©¸³ÅÍ¸¦ ÀÌ
 * Ä«Å»·Î±×¿¡ µî·ÏÇÏ¸é Á¤¼ö·Î µÈ id¸¦ ºÎ¿©¹Þ°Ô µÇ¸ç, ±× id·Î ÀúÀåµÈ µð½ºÅ©¸³ÅÍ¸¦ °Ë»öÇÒ ¼ö ÀÖ´Ù.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 32 $, $Date: 2004-11-09 23:37:16 +0900 (í™”, 09 11ì›” 2004) $
 */
class ClassCatalog {
    private static final int DEFAULT_CATALOG_SIZE = 64;
    private RandomAccessFile raf;
    private DataOutputStream out;
    private ObjectStreamClass[] descriptors =
        new ObjectStreamClass[DEFAULT_CATALOG_SIZE];
    private int nextDescriptorId;
    private Map map = new HashMap();

    public ClassCatalog(String filePath) {
        this(new File(filePath));
    }

    public ClassCatalog(File file) {
        try {
            raf = new RandomAccessFile(file, "rw");

            DataInputStream in =
                new DataInputStream(new FileInputStream(raf.getFD()));

            for (;;) {
                int id = in.readInt();
                String className = in.readUTF();
                ObjectStreamClass descriptor =
                    ObjectStreamClass.lookup(Class.forName(className));

                if (descriptor == null) {
                    throw new OilException("failed to look up descriptor for: " +
                                           className);
                }

                if (id >= descriptors.length) {
                    ObjectStreamClass[] newDescriptors =
                        new ObjectStreamClass[id * 2];
                    System.arraycopy(descriptors, 0, newDescriptors, 0,
                                     descriptors.length);
                    descriptors = newDescriptors;
                }

                descriptors[id] = descriptor;
                map.put(className, new Integer(id));

                if (nextDescriptorId <= id) {
                    nextDescriptorId = id + 1;
                }
            }
        } catch (EOFException e) {
        } catch (IOException e) {
            throw new OilException(e);
        } catch (ClassNotFoundException e) {
            throw new OilException(e);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                }

                raf = null;
            }
        }

        boolean open = false;

        try {
            raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            out = new DataOutputStream(new FileOutputStream(raf.getFD()));
            open = true;
        } catch (IOException e) {
            throw new OilException(e);
        } finally {
            if ((raf != null) && !open) {
                try {
                    raf.close();
                } catch (IOException e) {
                }

                raf = null;
            }
        }
    }

    public synchronized void close() {
        Arrays.fill(descriptors, null);
        map.clear();

        try {
            out.close();
        } catch (IOException e) {
        }

        out = null;

        try {
            raf.close();
        } catch (IOException e) {
        }

        raf = null;
    }

    public ObjectStreamClass getDescriptor(int id) {
        if ((id < 0) || (id >= descriptors.length)) {
            return null;
        } else {
            return descriptors[id];
        }
    }

    public int setDescriptor(ObjectStreamClass descriptor) {
        String className = descriptor.getName();
        Integer id = (Integer) map.get(className);

        if (id != null) {
            return id.intValue();
        }

        synchronized (this) {
            int newId = nextDescriptorId;

            if (newId < 0) {
                throw new OilException("class catalog is full");
            }

            try {
                write(out, newId, descriptor);

                if (newId == descriptors.length) {
                    ObjectStreamClass[] newDescriptors =
                        new ObjectStreamClass[newId * 2];
                    System.arraycopy(descriptors, 0, newDescriptors, 0,
                                     descriptors.length);
                    descriptors = newDescriptors;
                }

                descriptors[newId] = descriptor;
                map.put(className, new Integer(newId));
                nextDescriptorId++;

                return newId;
            } catch (IOException ioe) {
                throw new OilException("failed to write class decriptor", ioe);
            }
        }
    }

    private void write(DataOutputStream out, int id,
                       ObjectStreamClass descriptor)
                throws IOException {
        out.writeInt(id);
        out.writeUTF(descriptor.getName());
        out.flush();
    }
}
