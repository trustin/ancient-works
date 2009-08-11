package net.gleamynode.cruft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cruft {
    private static final Pattern OBJ_PATTERN =
        Pattern.compile("^obj (.*) \\p{XDigit}+ \\p{Digit}+$");
    private static final Pattern SYM_PATTERN =
        Pattern.compile("^sym (.*) -> .* \\p{Digit}+$");
    private static final Pattern DIR_PATTERN =
        Pattern.compile("^dir (.*)$");
    
    private static final Pattern KEEP_PATTERN =
    Pattern.compile("/.keep(_[^/]+)?$");

    public static void main(String[] args) throws Exception {
        if (!Gentoo.isSuperUser()) {
            System.err.println("You must be a super user to run this application.");
            System.exit(1);
        }
        
        if (!Gentoo.isGentoo()) {
            System.err.println("This application runs on Gentoo Linux only.");
            System.exit(1);
        }
        
        Set<String> cruft = getCruft();
        for (String s: cruft) {
            System.out.println(s);
        }
    }

    public static Set<String> getCruft() throws IOException {
        Set<String> portageFiles = getPortageFiles();
        Set<String> candidateFiles = getCandidateFiles();
        
        Set<String> cruft = new HashSet<String>();
        for (String s: candidateFiles) {
            if (!portageFiles.contains(s)) {
                cruft.add(s);
            }
        }
        
        Set<String> filteredCruft = new TreeSet<String>(cruft);
        for (String s: cruft) {
            File f = new File(s);
            if (f.isDirectory()) {
                File[] children = f.listFiles();
                if (children != null && children.length != 0) {
                    filteredCruft.remove(s);
                    continue;
                }
            }
            
            if (KEEP_PATTERN.matcher(s).find()) {
                for (;;) {
                    filteredCruft.remove(f.toString());
                    f = f.getParentFile();
                    if (f == null) {
                        break;
                    }
                }
            }
        }
        
        Set<String> atoms = Gentoo.getInstalledAtoms();
        Set<String> normalizedAtoms = new HashSet<String>();
        for (String a: atoms) {
            a = a.replace('-', '_').replace('+', '_').replace("/", "__").toUpperCase();
            normalizedAtoms.add(a);
        }
        
        for (String a: normalizedAtoms) {
            try {
                Field f = GlobalSettings.class.getField(a);
                Pattern p = (Pattern) f.get(null);
                for (String s: cruft) {
                    if (p.matcher(s).find()) {
                        filteredCruft.remove(s);
                    }
                }
            } catch (Exception e) {
                // Ignore.
            }
        }
        
        return filteredCruft;
    }

    private static Set<String> getPortageFiles() throws IOException {
        Set<String> files = new HashSet<String>();
        Set<File> contentFiles = new HashSet<File>();

        File dbDir = new File("/var/db/pkg");
        for (File f1: dbDir.listFiles()) {
            if (!f1.isDirectory()) {
                continue;
            }
            
            for (File f2: f1.listFiles()) {
                if (!f2.isDirectory()) {
                    continue;
                }
                
                File f3 = new File(f2, "CONTENTS");
                if (f3.exists()) {
                    contentFiles.add(f3);
                }
            }
        }
        
        for (File c: contentFiles) {
            BufferedReader in = new BufferedReader(new FileReader(c));
            try {
                for (;;) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    
                    Matcher matcher = null;
                    if (line.startsWith("obj")) {
                        matcher = OBJ_PATTERN.matcher(line);
                    } else if (line.startsWith("sym")) {
                        matcher = SYM_PATTERN.matcher(line);
                    } else if (line.startsWith("dir")) {
                        matcher = DIR_PATTERN.matcher(line);
                    }
                    
                    if (matcher == null || !matcher.matches()) {
                        continue;
                    }
                    
                    addPortageFile(files, matcher.group(1));
                    addPortageFile(files, new File(matcher.group(1)).getCanonicalPath());
                }
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        
        return files;
    }

        private static void addPortageFile(Set<String> files, String f) {
                if (files.add(f)) {
                    if (f.endsWith(".py")) {
                        files.add(f + 'c');
                        files.add(f + 'o');
                    } else if (f.endsWith(".py-2.0")) {
                        f = f.substring(0, f.length() - 4);
                        files.add(f + 'c');
                        files.add(f + 'o');
                    } else if (f.endsWith("/fonts.alias")) {
                        String fontDir = new File(f).getParent() + '/';
                        files.add(fontDir + "fonts.dir");
                        files.add(fontDir + "encodings.dir");
                    } else if (f.endsWith("/index.theme")) {
                        String themeDir = new File(f).getParent() + '/';
                        files.add(themeDir + "icon-theme.cache");
                    }
                    
                    if (f.endsWith("/lib32")) {
                        addPortageFile(files, f.substring(0, f.length() - 6) + "/lib64");
                    } else if (f.endsWith("/lib64")) {
                        addPortageFile(files, f.substring(0, f.length() - 6) + "/lib32");
                    } else if (f.contains("/lib32/")) {
                        addPortageFile(files, f.replace("/lib32/", "/lib64/"));
                    } else if (f.contains("/lib64")) {
                        addPortageFile(files, f.replace("/lib64/", "/lib32/"));
                    }
                }
        }
    
    private static Set<String> getCandidateFiles() throws IOException {
        Set<String> files = new HashSet<String>();
        addCandidateFiles(files, new File("/"));
        return files;
    }
    
    private static void addCandidateFiles(Set<String> files, File cwd) throws IOException {
        String ccwd = cwd.getCanonicalPath();
        
        if (GlobalSettings.EXCLUSION_PATTERN.matcher(ccwd).find()) {
            return;
        } else if (LocalSettings.EXCLUSION_PATTERN.matcher(ccwd).find()) {
            return;
        }
        
        if (!files.add(ccwd)) {
            return;
        }
        
        File[] children = cwd.listFiles();
        if (children != null) {
            for (File f: children) {
                addCandidateFiles(files, f);
            }
        }
    }
}
