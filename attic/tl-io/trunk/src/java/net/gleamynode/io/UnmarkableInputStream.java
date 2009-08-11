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
 * @(#) $Id: UnmarkableInputStream.java,v 1.2 2003/04/21 04:15:23 anoripi Exp $
 *
 * Copyright (C) gleamynode.net. All rights reserved.
 *
 * This software is published under the terms of the GNU Lesser General
 * Public License version 2.1, a copy of which has been included with this
 * distribution in the LICENSE file.
 */
package net.gleamynode.io;


/**
 * <p>
 * An <code>InputStream</code> that hides mark feature.
 * </p>
 *
 * @author Trustin Lee (<a href="http://projects.gleamynode.net/">http://projects.gleamynode.net/</a>)
 *
 * @version $Revision: 1.2 $
 */
public class UnmarkableInputStream extends java.io.FilterInputStream {
    /**
     * Constructs a new <code>UnmarkableInputStream</code>.
     */
    public UnmarkableInputStream(java.io.InputStream in) {
        super(in);
    }

    /**
     * Returns <code>false</code>.
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * Does nothing.
     */
    public void mark(int readlimit) {
        return;
    }

    /**
     * Throws an <code>IOException</code>.
     */
    public void reset() throws java.io.IOException {
        throw new java.io.IOException("mark not supported");
    }
}

//
//  CHANGELOG
// ===========
// $Log: UnmarkableInputStream.java,v $
// Revision 1.2  2003/04/21 04:15:23  anoripi
// Utilized CVS keyword substitution in comments.
// Replaced license statement at source code to concise one.
//
//
