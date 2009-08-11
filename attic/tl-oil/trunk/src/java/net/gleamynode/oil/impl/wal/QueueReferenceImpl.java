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
 * @(#) $Id: QueueReferenceImpl.java 32 2004-11-09 14:37:16Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.gleamynode.oil.QueueReference;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 32 $, $Date: 2004-11-09 23:37:16 +0900 (화, 09 11월 2004) $
 */
class QueueReferenceImpl implements QueueReference {
    static final long serialVersionUID = 1L;
    private int extentId;
    private long checksum;
    private int offset;

    public QueueReferenceImpl(int extentId, long checksum, int offset) {
        this.extentId = extentId;
        this.checksum = checksum;
        this.offset = offset;
    }

    public long getChecksum() {
        return checksum;
    }

    public int getExtentId() {
        return extentId;
    }

    public int getOffset() {
        return offset;
    }

    public String toString() {
        return new ToStringBuilder(this).append("offset", this.offset)
                                        .append("extentId", this.extentId)
                                        .append("checksum", this.checksum)
                                        .toString();
    }
}
