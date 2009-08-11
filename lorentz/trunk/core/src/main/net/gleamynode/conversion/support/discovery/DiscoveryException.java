/*
 * $Header$
 * $Revision: 76 $
 * $Date: 2005-08-20 02:00:59 +0900 (Sat, 20 Aug 2005) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package net.gleamynode.conversion.support.discovery;


/**
 * <p>An exception that is thrown only if a suitable service
 * instance cannot be created by <code>ServiceFactory</code></p>
 * 
 * <p>Copied from LogConfigurationException<p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 76 $ $Date: 2005-08-20 02:00:59 +0900 (Sat, 20 Aug 2005) $
 */
public class DiscoveryException extends RuntimeException {


    /**
     * Construct a new exception with <code>null</code> as its detail message.
     */
    public DiscoveryException() {
        super();
    }

    /**
     * Construct a new exception with the specified detail message.
     *
     * @param message The detail message
     */
    public DiscoveryException(String message) {
        super(message);
    }

    /**
     * Construct a new exception with the specified cause and a derived
     * detail message.
     *
     * @param cause The underlying cause
     */
    public DiscoveryException(Throwable cause) {
        this((cause == null) ? null : cause.toString(), cause);
    }

    /**
     * Construct a new exception with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause The underlying cause
     */
    public DiscoveryException(String message, Throwable cause) {
        super(message);
        this.cause = cause; // Two-argument version requires JDK 1.4 or later
    }

    /**
     * The underlying cause of this exception.
     */
    protected Throwable cause = null;

    /**
     * Return the underlying cause of this exception (if any).
     */
    public Throwable getCause() {
        return this.cause;
    }
    
    public String toString() {
        String ls = System.getProperty("line.separator");
        String str = super.toString();
        if (cause != null) {
            str = str + ls +
                  "*****" + ls +
                  stackToString(cause);
        }
        return str;
    }

    private static String stackToString(Throwable e){
      java.io.StringWriter sw= new java.io.StringWriter(1024); 
      java.io.PrintWriter pw= new java.io.PrintWriter(sw); 
      e.printStackTrace(pw);
      pw.close();
      return sw.toString();
    }
}
