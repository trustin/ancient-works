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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

/**
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 340 $, $Date: 2008-11-19 09:58:15 +0900 (Wed, 19 Nov 2008) $
 */
public abstract class AbstractProjectGenerator implements BuildListener {

    private String projectName;
    private File baseDir;
    private final Set<SourceFolder> sourceFolders = new TreeSet<SourceFolder>();
    private final List<String> classpath = new ArrayList<String>();

    public void buildStarted(BuildEvent e) {
        sourceFolders.clear();
        classpath.clear();
    }

    public void buildFinished(BuildEvent e) {
        if (e.getException() != null) {
            e.getProject().log(
                    "Not generating the project files because the build failed.");
            return;
        }

        Project p = e.getProject();
        p.log("Generating the project files ...");

        projectName = p.getName();
        baseDir = p.getBaseDir();

        List<String> newClasspath = new ArrayList<String>();
        outerLoop:
        for (String s: classpath) {
            for (SourceFolder f: sourceFolders) {
                if (f.getOutputFolder().toString().equals(s) ||
                    PathUtil.shorten(f.getOutputFolder(), baseDir).toString().equals(s)) {
                    continue outerLoop;
                }
            }

            newClasspath.add(s);
        }

        try {
            generateProjectFiles(projectName, baseDir, sourceFolders, newClasspath);
        } catch (Throwable t) {
            throw new BuildException("Failed to generate the project files.", t);
        }

        p.log("Done");
    }

    protected abstract void generateProjectFiles(
            String projectName, File baseDirectory,
            Set<SourceFolder> sourceFolders, List<String> classpath) throws IOException;

    public void targetStarted(BuildEvent e) {
        // NOOP
    }

    public void targetFinished(BuildEvent e) {
        // NOOP
    }

    public void taskStarted(BuildEvent e) {
        // NOOP
    }

    public void taskFinished(BuildEvent e) {
        Task t = e.getTask();
        if (t instanceof UnknownElement) {
            UnknownElement ue = (UnknownElement) t;
            ue.maybeConfigure();
            t = ue.getTask();
        }

        if (t == null) {
            return;
        }

        if (t instanceof Javac) {
            handleJavac((Javac) t);
        }
    }

    public void messageLogged(BuildEvent e) {
        // NOOP
    }

    private void handleJavac(Javac t) {
        List<File> folders = new ArrayList<File>();

        Path p;

        p = t.getSrcdir();
        if (p != null) {
            for (String s: p.list()) {
                folders.add(new File(s));
            }
        }

        p = t.getSourcepath();
        if (p != null) {
            for (String s: p.list()) {
                folders.add(new File(s));
            }
        }

        File outputFolder = t.getDestdir();

        for (File f: folders) {
            sourceFolders.add(new SourceFolder(f, outputFolder));
        }

        // Guess resource directories by relying on Maven 2 directory layout.
        for (File f: folders) {
            if (f.getName().equalsIgnoreCase("java")) {
                File resources = new File(f.getParentFile(), "resources");
                if (resources.isDirectory()) {
                    sourceFolders.add(new SourceFolder(resources, outputFolder));
                }
            }
        }

        p = t.getClasspath();
        if (p != null) {
            for (String s: p.list()) {
                if (!classpath.contains(s)) {
                    classpath.add(s);
                }
            }
        }
    }
}
