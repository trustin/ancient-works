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
 * @(#) $Id: RunRecoveryException.java 32 2004-11-09 14:37:16Z trustin $
 */
package net.gleamynode.oil;


/**
 * An {@link OilException} that is thrown while opening a corrupted database.
 * Call {@link Database#recover()} to recover it.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 32 $, $Date: 2004-11-09 23:37:16 +0900 (화, 09 11월 2004) $
 */
public class RunRecoveryException extends OilException {
    /**
     * Creates a new instance.
     */
    public RunRecoveryException() {
    }

    /**
     * Creates a new instance.
     */
    public RunRecoveryException(String s) {
        super(s);
    }

    /**
     * Creates a new instance.
     */
    public RunRecoveryException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Creates a new instance.
     */
    public RunRecoveryException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
