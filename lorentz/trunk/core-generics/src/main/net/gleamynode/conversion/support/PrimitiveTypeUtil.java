/*
 *   @(#) $Id: PrimitiveTypeUtil.java 20 2005-07-29 07:53:34Z trustin $
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
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 20 $, $Date: 2005-07-29 16:53:34 +0900 (Fri, 29 Jul 2005) $
 */
public class PrimitiveTypeUtil
{
    public static Class toPrimitiveType(Class type)
    {
        if (type == Boolean.class)
            return boolean.class;
        if (type == Character.class)
            return char.class;
        if (type == Double.class)
            return double.class;
        if (type == Float.class)
            return float.class;
        if (type == Integer.class)
            return int.class;
        if (type == Long.class)
            return long.class;
        if (type == Short.class)
            return short.class;
        return null;
    }
    
    private PrimitiveTypeUtil()
    {
    }
}
