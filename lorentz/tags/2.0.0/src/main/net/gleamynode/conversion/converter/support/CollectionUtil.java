/*
 *   @(#) $Id: CollectionUtil.java 22 2005-07-29 08:00:54Z trustin $
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
package net.gleamynode.conversion.converter.support;

import net.gleamynode.conversion.ConversionException;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 22 $, $Date: 2005-07-29 17:00:54 +0900 (Fri, 29 Jul 2005) $
 */
public class CollectionUtil
{
    private CollectionUtil()
    {
    }

    public static String escape(String value) {
        boolean wrapWithQuote = false;
        // +8 is a spare padding for quote expansion
        int len = value.length();
        StringBuffer buf = new StringBuffer(len + 8);
        int i;
        char c;

        if (len == 0)
            return "\"\"";
        if (Character.isWhitespace(value.charAt(0)))
            wrapWithQuote = true;
        else if (Character.isWhitespace(value.charAt(len - 1)))
            wrapWithQuote = true;

        for (i = 0; i < len; i++) {
            c = value.charAt(i);

            switch (c) {
                case ',' :
                case '=' :
                    wrapWithQuote = true;
                    buf.append(c);
                    break;
                case '"' :
                    wrapWithQuote = true;
                    buf.append("\"\"");
                    break;
                default :
                    buf.append(c);
            }
        }

        if (wrapWithQuote) {
            return "\"" + buf.toString() + '"';
        } else {
            return buf.toString();
        }
    }

    public static String unescape(String value, int begin, int end)
            throws ConversionException {
        // trim left
        for (int i = begin; i < end; i++) {
            if (Character.isWhitespace(value.charAt(i)))
                begin++;
            else
                break;
        }

        // trim right
        for (int i = end - 1; i >= begin; i--) {
            if (Character.isWhitespace(value.charAt(i)))
                end--;
            else
                break;
        }

        // check basic stuff
        switch (end - begin) {
            case 0 :
                throw new ConversionException(
                        "Empty string must be quoted by '\"'");
            case 1 :
                if (value.charAt(begin) == '"')
                    throw new ConversionException("Mismatching '\"'");
                break;
            case 2 :
                if (value.charAt(begin) == '"'
                        && value.charAt(begin + 1) == '"')
                    return "";
        }

        // remove wrapping quotes
        if (value.charAt(begin) == '"') {
            if (value.charAt(end - 1) != '"')
                throw new ConversionException("Mismatching '\"'");
            else {
                begin ++;
                end --;
            }
        }
        
        StringBuffer buf = new StringBuffer(end - begin);
        for (int i = begin; i < end; i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"' :
                    if (i == end - 1)
                        throw new ConversionException("Mismatching '\"'");
                    else if (value.charAt(i) != '"')
                        throw new ConversionException("Mismatching '\"'");
                    else {
                        buf.append('"');
                        i++;
                    }
                    break;
                default :
                    buf.append(c);
            }
        }

        return buf.toString();
    }
}
