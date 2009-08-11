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
public class SourceFolder implements Comparable<SourceFolder> {
    private final File folder;
    private final File outputFolder;

    public SourceFolder(File folder, File outputFolder) {
        if (folder == null) {
            throw new NullPointerException("folder");
        }
        if (outputFolder == null) {
            throw new NullPointerException("outputFolder");
        }
        this.folder = folder;
        this.outputFolder = outputFolder;
    }

    public File getFolder() {
        return folder;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    @Override
    public int hashCode() {
        return folder.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SourceFolder)) {
            return false;
        }

        SourceFolder that = (SourceFolder) o;
        return folder.equals(that.folder);
    }

    public int compareTo(SourceFolder o) {
        return folder.compareTo(o.folder);
    }

    @Override
    public String toString() {
        return "(" + folder + " => " + outputFolder + ")";
    }
}
