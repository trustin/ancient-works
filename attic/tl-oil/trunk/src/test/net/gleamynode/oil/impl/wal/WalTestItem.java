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
 * @(#) $Id: WalTestItem.java 34 2004-11-09 14:57:44Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import java.io.Serializable;


/**
 * A queue/index element for testing purpose.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 34 $, $Date: 2004-11-09 23:57:44 +0900 (화, 09 11월 2004) $
 */
class WalTestItem implements Serializable {
    private String value;

    public WalTestItem(String value) {
        this.value = value;
    }

    public int hashCode() {
        return value.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof WalTestItem) {
            return value.equals(((WalTestItem) o).value);
        } else {
            return false;
        }
    }

    public String toString() {
        return value;
    }
}
