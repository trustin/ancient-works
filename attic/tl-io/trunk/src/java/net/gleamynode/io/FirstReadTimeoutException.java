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


/**
 * An exception that is thrown from the methods in {@link BlockingInputStream}
 * if the first one byte is not read from the stream within
 * <code>firstTimeout</code> milliseconds.
 *
 * @author Trustin Lee (<a href="http://projects.gleamynode.net/">http://projects.gleamynode.net/</a>)
 *
 * @version $Revision: 1.2 $
 *
 * @see BlockingInputStream
 */
public class FirstReadTimeoutException extends IOTimeoutException {
    /**
     * Constructs a new instance of {@link FirstReadTimeoutException}.
     */
    public FirstReadTimeoutException() {
        super();
    }

    /**
     * Constructs a new instance of {@link FirstReadTimeoutException} with the
     * specified message.
     */
    public FirstReadTimeoutException(String message) {
        super(message);
    }
}

//
//  CHANGELOG
// ===========
// $Log: FirstReadTimeoutException.java,v $
// Revision 1.2  2003/04/22 11:26:10  anoripi
// Fixed typo error in JavaDoc.
//
// Revision 1.1  2003/04/11 15:54:44  anoripi
// The first release.
//
//
