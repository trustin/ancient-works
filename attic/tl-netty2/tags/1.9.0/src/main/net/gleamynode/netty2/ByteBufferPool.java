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

import java.nio.ByteBuffer;


/**
 * TODO Insert type comment.
 *
 * @version $Rev: 19 $, $Date: 2005-04-20 00:29:55 +0900 $
 * @author Trustin Lee (http://gleamynode.net/dev/)
 */
class ByteBufferPool {
    static final int DEFAULT_BUF_SIZE = 8192;
    private static Queue buffers = new Queue(16);

    static {
        buffers.open();
    }

    public static synchronized ByteBuffer open() {
        ByteBuffer buf = (ByteBuffer) buffers.pop();

        if (buf == null) {
            buf = ByteBuffer.allocateDirect(DEFAULT_BUF_SIZE);
        } else {
            buf.clear();
        }

        return buf;
    }

    public static synchronized void close(ByteBuffer buf) {
        buffers.push(buf);
    }
}
