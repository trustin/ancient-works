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
 * @(#) $Id: IndexLog.java 42 2004-11-23 06:26:38Z trustin $
 */
package net.gleamynode.oil.impl.wal.log;


/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 42 $, $Date: 2004-11-23 15:26:38 +0900 (화, 23 11월 2004) $
 */
public abstract class IndexLog extends Log {
    static final long serialVersionUID = -8752235279466044835L;
    protected int indexId;

    protected IndexLog() {
    }

    protected IndexLog(int indexId) {
        this.indexId = indexId;
    }

    public final int getIndexId() {
        return indexId;
    }
}
