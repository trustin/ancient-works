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
 * @(#) $Id: ReadTimeoutException.java,v 1.4 2003/04/22 11:26:10 anoripi Exp $
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
 * An exception that is thrown from the methods in {@link BlockingInputStream}
 * if the desired amount of bytes are not read from the stream within
 * <code>timeout</code> milliseconds.
 * </p>
 *
 * @author Trustin Lee (<a href="http://projects.gleamynode.net/">http://projects.gleamynode.net/</a>)
 *
 * @version $Revision: 1.4 $
 *
 * @see BlockingInputStream
 */
public class ReadTimeoutException extends IOTimeoutException {
    /**
     * Cosntructs a new <code>ReadTimeoutException</code> with no description.
     */
    public ReadTimeoutException() {
    }

    /**
     * Cosntructs a new <code>ReadTimeoutException</code> with some description.
     */
    public ReadTimeoutException(String message) {
        super(message);
    }
}

//
//  CHANGELOG
// ===========
// $Log: ReadTimeoutException.java,v $
// Revision 1.4  2003/04/22 11:26:10  anoripi
// Fixed typo error in JavaDoc.
//
// Revision 1.3  2003/04/11 15:55:05  anoripi
// Utilized CVS keyword substitution in comments.
// Replaced the license statement to concise one.
// Revised comments.
//
//
