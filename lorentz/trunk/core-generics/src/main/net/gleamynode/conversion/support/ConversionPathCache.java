/*
 *   @(#) $Id: ConversionPathCache.java 129 2005-11-14 09:35:57Z trustin $
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.gleamynode.conversion.ConversionPath;

/**
 * A cache which stores precalculated {@link ConversionPath}s.
 *
 * @author Trustin Lee
 * @version $Rev: 129 $, $Date: 2005-11-14 18:35:57 +0900 (Mon, 14 Nov 2005) $
 */
public class ConversionPathCache {
    private final Map<ConversionKey, ConversionPath> map =
        Collections.synchronizedMap(new HashMap<ConversionKey, ConversionPath>());
    
    public ConversionPathCache() {
    }
    
    public void add(ConversionKey key, ConversionPath path) {
        map.put(key, path.clone());
    }
    
    public ConversionPath get(ConversionKey key) {
        return map.get(key);
    }
    
    public void invalidate() {
        map.clear();
    }
}
