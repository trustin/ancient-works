/*
 *   @(#) $Id: ConversionKey.java 129 2005-11-14 09:35:57Z trustin $
 *
 *   Copyright 2004 Trustin Lee
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
package net.gleamynode.conversion.support;

/**
 * A key for accessing {@link ConversionPathCache}.
 *
 * @author Trustin Lee
 * @version $Rev: 129 $, $Date: 2005-11-14 18:35:57 +0900 (Mon, 14 Nov 2005) $
 *
 */
public class ConversionKey {
    private final Class sourceType;
    private final Class targetType;
    
    public ConversionKey(Class sourceType, Class targetType) {
        this.sourceType = sourceType;
        this.targetType = targetType;
    }
    
    public Class getSourceType() {
        return sourceType;
    }

    public Class getTargetType() {
        return targetType;
    }
    
    public int hashCode() {
        return sourceType.hashCode() ^ targetType.hashCode();
    }
    
    public boolean equals(Object o) {
        if (this == o)
            return true;
        
        if (!(o instanceof ConversionKey))
            return false;
        
        ConversionKey that = (ConversionKey) o;
        return this.sourceType == that.sourceType && this.targetType == that.targetType;
    }
}
