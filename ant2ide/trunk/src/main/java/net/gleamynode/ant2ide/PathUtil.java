/*
 * Ant2IDE - Generates IDE project files from Ant build.xml
 * 
 * Copyright (C) 2008  Trustin Heuiseung Lee
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package net.gleamynode.ant2ide;

import java.io.File;

/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 308 $, $Date: 2008-11-04 19:08:06 +0900 (Tue, 04 Nov 2008) $
 */
public class PathUtil {
    public static File shorten(File path, File baseDirectory) {
        if (baseDirectory == null) {
            return path;
        }
        
        if (path.toString().startsWith(baseDirectory.toString() + File.separatorChar)) {
            return new File(path.toString().substring(baseDirectory.toString().length() + 1));
        }

        return path;
    }
}
