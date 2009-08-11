package net.gleamynode.cruft;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gentoo {
    
    public static boolean isSuperUser() {
        File shadow = new File("/etc/shadow");
        return shadow.canRead() && shadow.canWrite();
    }
    
    public static boolean isGentoo() {
        return new File("/etc/gentoo-release").exists();
    }
    
    public static Set<String> getInstalledAtoms() {
        Set<String> atoms = new HashSet<String>();

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
                    atoms.add(f1.getName() + '/' + stripVersion(f2.getName()));
                }
            }
        }
        
        return atoms;
    }
    
    private static final Pattern VERSION_PATTERN =
        Pattern.compile("-[^-]+(-r\\d+)?$");
    
    private static String stripVersion(String atom) {
        Matcher m = VERSION_PATTERN.matcher(atom);
        if (!m.find()) {
            return atom;
        }
        
        return atom.substring(0, m.start());
    }
}
