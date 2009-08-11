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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 308 $, $Date: 2008-11-04 19:08:06 +0900 (Tue, 04 Nov 2008) $
 */
public class EclipseProjectGenerator extends AbstractProjectGenerator {

    @Override
    protected void generateProjectFiles(
            String projectName, File baseDirectory,
            Set<SourceFolder> sourceFolders, List<String> classpath)
            throws IOException {

        File projectFile = new File(baseDirectory, ".project");
        if (!projectFile.exists()) {
            generateDotProject(projectFile, projectName);
        }

        File classpathFile = new File(baseDirectory, ".classpath");
        generateDotClasspath(classpathFile, baseDirectory, sourceFolders, classpath);
    }

    private void generateDotProject(File projectFile, String projectName) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buf.append("<projectDescription>");
        buf.append("<name>" + projectName + "</name>");
        buf.append("<comment></comment>");
        buf.append("<projects></projects>");
        buf.append("<buildSpec>");
        buf.append("<buildCommand>");
        buf.append("<name>org.eclipse.jdt.core.javabuilder</name>");
        buf.append("<arguments></arguments>");
        buf.append("</buildCommand>");
        buf.append("</buildSpec>");
        buf.append("<natures>");
        buf.append("<nature>org.eclipse.jdt.core.javanature</nature>");
        buf.append("</natures>");
        buf.append("</projectDescription>");

        OutputStream out = new FileOutputStream(projectFile);
        try {
            out.write(buf.toString().getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }

    private void generateDotClasspath(
            File classpathFile, File baseDirectory,
            Set<SourceFolder> sourceFolders, List<String> classpath) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buf.append("<classpath>");

        String defaultOutputFolder = null;
        for (SourceFolder f: sourceFolders) {
            buf.append("<classpathentry kind=\"src\" path=\"");
            buf.append(PathUtil.shorten(f.getFolder(), baseDirectory));
            buf.append("\" output=\"");
            buf.append(PathUtil.shorten(f.getOutputFolder(), baseDirectory));
            buf.append("\"/>");

            // TODO Inclusion / exclusion patterns

            if (defaultOutputFolder == null) {
                defaultOutputFolder = PathUtil.shorten(f.getOutputFolder(), baseDirectory).toString();
            }
        }

        if (defaultOutputFolder != null) {
            buf.append("<classpathentry kind=\"output\" path=\"");
            buf.append(defaultOutputFolder);
            buf.append("\"/>");
        }

        buf.append("<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>");

        for (String s: classpath) {
            File f = new File(s);
            if (!f.exists()) {
                continue;
            }
            
            buf.append("<classpathentry kind=\"lib\" path=\"");
            buf.append(PathUtil.shorten(f, baseDirectory).toString());
            buf.append("\"/>");
        }

        buf.append("</classpath>");

        OutputStream out = new FileOutputStream(classpathFile);
        try {
            out.write(buf.toString().getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }
}
