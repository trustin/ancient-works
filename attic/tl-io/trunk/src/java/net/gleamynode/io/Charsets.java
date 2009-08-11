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
 * @(#) $Id$
 */
package net.gleamynode.io;

import java.nio.charset.Charset;


/**
 * Provides some predefined <code>Charset</code>s as constants.
 *
 * @version $Revision: 1.1 $, $Date: 2004/02/09 01:54:45 $
 * @author Trustin Lee
 */
public class Charsets {
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset UTF_16 = Charset.forName("UTF-16");
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
    public static final Charset EUC_KR = Charset.forName("EUC-KR");
    public static final Charset EUC_JP = Charset.forName("EUC-JP");

    private Charsets() {
    }
}
