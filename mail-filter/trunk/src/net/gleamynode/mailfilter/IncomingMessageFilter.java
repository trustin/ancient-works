package net.gleamynode.mailfilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class IncomingMessageFilter {

    private static final String CHARSET = "utf-8";

    private static final List<Pattern> SUBJECT_PATTERNS = new ArrayList<Pattern>();
    private static final List<String> SUBJECT_REPLACEMENTS = new ArrayList<String>();
    private static final Set<String> GARBLED_KOREAN = new HashSet<String>();
    private static final Pattern KOREAN_CHARSET_PATTERN = Pattern.compile(
            ";\\s*charset\\s*=\\s*[\"']*\\s*(euc[-_]?kr|[-_a-z]*949)\\s*[\"']*\\s*$",
            Pattern.CASE_INSENSITIVE);

    private static void addSubjectPattern(String pattern, String replacement) {
        SUBJECT_PATTERNS
                .add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
        SUBJECT_REPLACEMENTS.add(replacement);
    }

    private static void addGarbledKorean(String s) {
        try {
            for (int i = 0; i < s.length(); i++) {
                GARBLED_KOREAN.add(new String(s.substring(i, i + 1).getBytes(
                        "CP949"), "ISO-8859-1"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    static {
        // Remove tabs and newlines.
        addSubjectPattern("\\n", "");
        addSubjectPattern("\\t", " ");

        // Remove unnecessarily duplicated characters.
        addSubjectPattern("  +", " ");

        // Replace wide characters with narrow ones.
        addSubjectPattern("：", ":");

        // Normalize reply labels
        addSubjectPattern("(^|(?<=[-_:\\s]))(전체\\s*)?(答复|回复|답장|회신|Sv|AW)\\s*:", "Re:");
        addSubjectPattern(
                "(^|(?<=[-_:\\s]))(re\\s*:|\\[\\s*re\\s*\\]|re\\s*>|re\\^[0-9]*:|reply\\s*:)\\s*",
                "Re: ");
        addSubjectPattern("(^|(?<=[-_:\\s]))Re\\[[0-9]+\\]\\s*:?\\s*", "Re: ");
        addSubjectPattern("(^|(?<=[-_:\\s]))Re:\\s*", "Re: ");
        addSubjectPattern("Subject\\s*:\\s*Re\\s*:\\s*", "Re: ");
        addSubjectPattern("(^|(?<=[-_:\\s]))Re\\s*:\\s*Subject\\s*:\\s*", "Re: ");
        addSubjectPattern("  +", " ");
        addSubjectPattern("(^|(?<=[-_:\\s]))(Re: )+", "Re: ");

        // Normalize forward labels.
        addSubjectPattern("전달\\s*[:;]", "Fwd:");
        addSubjectPattern("fwd?[:;]\\s*", "Fwd: ");
        addSubjectPattern("Fwd[:;]", "Fwd: ");
        addSubjectPattern("\\[fwd?\\]\\s*", "Fwd: ");
        addSubjectPattern("  +", " ");
        addSubjectPattern("(Fwd[:;] )+", "Fwd: ");

        // Normalize was: labels.
        addSubjectPattern("was\\s*[:;]\\s*", "Was: ");
        addSubjectPattern("was\\s+re\\s*[:;]\\s*", "Was: Re: ");

        // Remove unnecessarily duplicated characters again.
        addSubjectPattern("  +", " ");

        // Trim
        addSubjectPattern("(^ +| +$)", "");
        addSubjectPattern("\\[ +", "\\[");
        addSubjectPattern(" +\\]", "\\]");
        addSubjectPattern("\\( +", "\\(");
        addSubjectPattern(" +\\)", "\\)");
        addSubjectPattern("\\{ +", "\\{");
        addSubjectPattern(" +\\}", "\\}");
        addSubjectPattern("\\(\\s+\\)", "\\(\\)");
        addSubjectPattern("\\[\\s+\\]", "\\[\\]");
        addSubjectPattern("\\{\\s+\\}", "\\{\\}");
        addSubjectPattern("(\\}|\\]|\\))\\s*-+\\s*", "$1");

        // Fix mismatching brackets
        addSubjectPattern("\\[([^\\[\\]\\{\\}]+)\\}", "\\[$1\\]");

        // Normalize SVN commit log messages
        addSubjectPattern("^JBoss Remoting SVN:\\s*", "");
        addSubjectPattern("^\\[jboss-remoting-commits\\]\\s*", "svn commit: ");
        addSubjectPattern("^\\[remoting\\]\\s*svn commit:\\s*", "svn commit: ");
        addSubjectPattern("^(r[0-9]+)\\s+-\\s+", "svn commit: $1 - ");
        addSubjectPattern("^SVN commit:", "svn commit:");

        // Shorten too long labels
        addSubjectPattern("^\\[Trustin Lee's GLEAMYNODE\\.net\\]\\s*", "");
        addSubjectPattern("^comment received:", "Comment received:");

        // Remove unnecessary labels
        addSubjectPattern("\\[?\\s*(spam|스팸)[^\\[\\]\\?\"':;!]*\\]?", "");
        addSubjectPattern("\\[jboss-dev-forums\\]\\s*\\[[^\\[\\]\\?\"':;!]+\\]\\s*", "");
        addSubjectPattern("\\[jboss-dev-forums\\]\\s*Re:\\s*\\[[^\\[\\]\\?\"':;!]+\\]\\s*", "Re: ");
        addSubjectPattern("\\[[^\\[\\]\\?\"':;!]*(rss)[^\\[\\]\\?\"':;!]*\\]\\s*\\[[^\\[\\]\\?\"':;!]*\\]\\s*", "");
        addSubjectPattern("\\[[^\\[\\]\\?\"':;!]*(rss|ann(ounce[a-z]*)?|notice|notification|discuss|proposal|rfc|heads?[-_ ]up|status|FYI|뉴스|명세서|문의|알림|안내|공지|리포트)[^\\[\\]\\?\"':;!]*\\]\\s*", "");
        addSubjectPattern("\\[[^\\[\\]\\?\"':;!]*(삼성생명|KB카드|G마켓|인터파크|스마일서브|smileserv|marketwatch|벅스|vid)[^\\[\\]\\?\"':;!]*\\]\\s*", "");
        addSubjectPattern("\\[[^\\[\\]\\?\"':;!]*(jboss-dev-forums|remoting|messaging|continuum|jira|conf(luence)?|위즐|WIKIN)[^\\[\\]\\?\"':;!]*\\]\\s*", "");
        addSubjectPattern("\\[[-_a-z0-9]+-(list|dev|user|announce|issue|team|interest|discuss|commit|cvs|staff)[a-z]*\\]\\s*", "");
        addSubjectPattern("\\[(list|dev|user|announce|issue|team|interest|discuss|commit|cvs|staff|rhn)[a-z]*-[-_a-z0-9]+\\]\\s*", "");
        addSubjectPattern("\\[(|[^\\[\\]\\?\"':;!]+[-_:\\s]+jira|jira|지라)\\]\\s*", "");

        // [label] Re: -> Re: [label]
        addSubjectPattern("(\\[[^\\[\\]\\?\"':;!]+\\])\\s*Re:\\s*", "Re: $1 ");
        addSubjectPattern("(^|(?<=[-_:\\s]))(Re: )+", "Re: "); // strip duplicate 'Re's again.

        // [label] : subject -> [label] subject
        addSubjectPattern("(\\[[^\\[\\]\\?\"':;!]+\\])\\s*[-:]\\s*(\\S)", "$1 $2");

        // [label]X -> [label] X
        addSubjectPattern("(\\[[^\\[\\]\\?\"':;!]+\\])([a-z0-9])", "$1 $2");

        // [labelA] [labelB] -> [labelA][labelB]
        addSubjectPattern("(\\[[^\\[\\]\\?\"':;!]+\\])\\s+(\\[[^\\[\\]\\?\"':;!]+\\])", "$1$2");
        addSubjectPattern("(\\[[^\\[\\]\\?\"':;!]+\\])\\s+(\\[[^\\[\\]\\?\"':;!]+\\])", "$1$2");
        addSubjectPattern("(\\[[^\\[\\]\\?\"':;!]+\\])\\s+(\\[[^\\[\\]\\?\"':;!]+\\])", "$1$2");
        addSubjectPattern("\\]\\[Fwd", "\\] \\[Fwd"); // ][Fwd is an exception.

        // (label) Re: -> Re: (label)
        addSubjectPattern("(\\([^\\)]+\\))\\s*Re:\\s*", "Re: $1 ");
        addSubjectPattern("(^|(?<=[-_:\\s]))(Re: )+", "Re: "); // strip duplicate 'Re's again.

        // (label) : subject -> (label) subject
        addSubjectPattern("(\\([^\\)]+\\))\\s*[-:]\\s*(\\S)", "$1 $2");

        // (label)X -> (label) X
        addSubjectPattern("(\\([^\\)]+\\))([a-z0-9])", "$1 $2");

        // (labelA) (labelB) -> (labelA)(labelB)
        addSubjectPattern("(\\([^\\)]+\\))\\s+(\\([^\\)]+\\))", "$1$2");
        addSubjectPattern("(\\([^\\)]+\\))\\s+(\\([^\\)]+\\))", "$1$2");
        addSubjectPattern("(\\([^\\)]+\\))\\s+(\\([^\\)]+\\))", "$1$2");
        addSubjectPattern("\\)\\(Fwd", "\\) \\(Fwd"); // )(Fwd is an exception.

        // Add some useful characters for garbled character detection.
        addGarbledKorean("은는이가에게으로십신실시습다까님께안공내김박최채");
        addGarbledKorean("하한오정나강곽임남주추방사장차황탁천지축들대가취");
        addGarbledKorean("월화수목금토일될된됩유류네셨관련송부의견관메일초");
        addGarbledKorean("세요결제내역희승팀비교심테스트엄청건했였드립료림");
        addGarbledKorean("말씀옥션행온뱅크모닝디카파칼라티켓링보객서읍았터");
        addGarbledKorean("삼성생명화재보험문상배윤경템플야후등록확인텔레콤");
        addGarbledKorean("와우북롯데");
    }

    public static boolean filter(File file) {
        if (file.isFile()) {
            if (file.length() == 0) {
                file.delete();
                return true;
            }

            byte[] dataIn = null;
            try {
                dataIn = FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            if (filter(new ByteArrayInputStream(dataIn), baos)) {
                byte[] dataOut = baos.toByteArray();
                if (!Arrays.equals(dataIn, dataOut)) {
                    System.err.println(file.getPath());
                    File newFile = new File(file.getPath() + ".new");
                    newFile.delete();
                    try {
                        FileUtils.writeByteArrayToFile(newFile, dataOut);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    File backupDir;
                    if (file.getParentFile().getParent() == null) {
                        backupDir = new File(
                                file.getParentFile() + File.separator +
                                ".." + File.separator + "bak");
                    } else {
                        backupDir = new File(
                                file.getParentFile().getParent() +
                                File.separator + "bak");
                    }

                    backupDir.mkdirs();
                    File backupFile = new File(backupDir.getPath()
                            + File.separatorChar + file.getName());
                    backupFile.delete();
                    if (file.renameTo(backupFile)) {
                        if (!newFile.renameTo(file)) {
                            System.err.println("Failed to replace: " + file);
                            // Revert back the change.
                            backupFile.renameTo(file);
                            newFile.delete();
                            return false;
                        }
                    } else {
                        System.err.println("Failed to back up: " + file);
                    }
                }

                // Correct file length field in the file name
                File newFile = new File(file.getPath().replaceAll(
                        "(,[WS]=[0-9]+)*:", ",S=" + dataOut.length + ":"));
                if (!file.getName().equalsIgnoreCase(newFile.getName())) {
                    newFile.delete();
                    file.renameTo(newFile);
                }
                // ALL DONE! :)
            }
            return true;
        } else if (file.isDirectory()) {
            System.err.println("Recursing into " + file + " ... ");

            File[] children = file.listFiles();
            int cnt = 0;
            for (File f : children) {
                if (!filter(f)) {
                    return false;
                }
                cnt++;
                if (cnt % 1000 == 0) {
                    System.err.println(cnt + " / " + children.length);
                }
            }

            if (children.length % 1000 != 0) {
                System.err.println(children.length + " / " + children.length);
            }
            return true;
        } else {
            return false;
        }
    }

    private static byte[] removeCR(byte[] dataOut) {
        int removedCR = 0;
        for (int i = 0; i < dataOut.length - 1; i++) {
            if (dataOut[i] == '\r' && dataOut[i + 1] == '\n') {
                System.arraycopy(dataOut, i + 1, dataOut, i, dataOut.length - i
                        - 1);
                removedCR++;
            }
        }
        if (removedCR > 0) {
            byte[] newDataOut = new byte[dataOut.length - removedCR];
            System.arraycopy(dataOut, 0, newDataOut, 0, newDataOut.length);
            dataOut = newDataOut;
        }
        return dataOut;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            if (!filter(System.in, System.out)) {
                System.exit(1);
            }
        } else {
            for (String file : args) {
                if (!filter(new File(file))) {
                    System.exit(1);
                }
            }
        }
    }

    private static boolean filter(InputStream in, OutputStream out) {
    	byte[] original;
    	byte[] filtered;
    	try {
    		original = IOUtils.toByteArray(in);
    	} catch (IOException e) {
    		IOUtils.closeQuietly(in);
    		return false;
    	}

        MimeMessage m = null;
        try {
            Session s = Session.getDefaultInstance(new Properties());
            m = new MimeMessage(s, new ByteArrayInputStream(original));

            // Normalize subject.
            normalizeSubject(m);

            // Normalize recipients.
            normalizeRecipients(m, Message.RecipientType.TO);
            normalizeRecipients(m, Message.RecipientType.CC);
            normalizeRecipients(m, Message.RecipientType.BCC);
            normalizeRecipients(m, MimeMessage.RecipientType.NEWSGROUPS);

            // Normalize reply-to.
            m.setReplyTo(normalizeAddresses(m.getReplyTo()));

            // Normalize from.
            Address[] fromAddresses = normalizeAddresses(m.getFrom());
            if (fromAddresses.length == 0) {
                m.setFrom(null);
            } else {
                m.setFrom(fromAddresses[0]);
            }

            // Normalize sender.
            m.setSender(normalizeAddress(m.getSender()));

            // Remove unnecessary 'X-*' headers.
            removeUnnecessaryHeaders(m);

            // Normalize sent date and delivery date.
            normalizeDates(m);

            // Fix Content-Type
            fixContentType(m);

            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            m.writeTo(baos);
            filtered = removeCR(baos.toByteArray());
        } catch (Throwable t) {
        	// Fall back to the original copy when filtering fails for some reason.
        	filtered = original;
        }

        try {
            out.write(filtered);
            return true;
        } catch (Throwable t) {
            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(System.getProperty("java.io.tmpdir",
                        "/tmp")
                        + File.separatorChar + "mailfilter.log", true);
                PrintStream ps = new PrintStream(fo);
                if (m != null) {
                    ps.println("Failed to process a message: "
                            + m.getMessageID());
                }
                t.printStackTrace(ps);
            } catch (Throwable t2) {
                // false will be returned below.
            } finally {
                if (fo != null) {
                    try {
                        fo.close();
                    } catch (IOException t3) {
                        // Ignore.
                    }
                }
            }
        }
        return false;
    }

    private static void fixContentType(MimeMessage m) throws MessagingException {
        String[] contentTypes = m.getHeader("Content-Type");
        if (contentTypes == null || contentTypes.length != 1) {
            return;
        }

        String contentType = contentTypes[0];
        if (contentType != null) {
            String newContentType = KOREAN_CHARSET_PATTERN.matcher(
                    contentType).replaceAll("; charset=x-windows-949");
            if (!contentType.equals(newContentType)) {
                m.setHeader("Content-Type", newContentType);
            }
        }
    }

    private static void normalizeDates(MimeMessage m) throws MessagingException {
        Date sentDate = m.getSentDate();
        m.setSentDate(m.getSentDate());
        if (sentDate == null) {
            m.removeHeader("Delivery-Date");
        } else {
            m.setHeader("Delivery-Date", new MailDateFormat().format(sentDate));
        }
    }

    private static void normalizeSubject(MimeMessage m)
            throws MessagingException, UnsupportedEncodingException {
        String subject = m.getSubject();
        if (subject != null) {
            m.setSubject(
                    normalizeSubject(MimeUtility
                            .decodeText(fixGarbledKorean(MimeUtility
                                    .unfold(subject)))), CHARSET);
        }
    }

    @SuppressWarnings("unchecked")
    private static void removeUnnecessaryHeaders(MimeMessage m) throws MessagingException {
        List<String> toRemove = new ArrayList<String>();
        toRemove.add("Received-SPF");
        toRemove.add("Received");
        toRemove.add("Authentication-Results");
        toRemove.add("DKIM-Signature");
        toRemove.add("DomainKey-Signature");

        Enumeration<Header> e = m.getAllHeaders();
        while (e.hasMoreElements()) {
            Header header = e.nextElement();
            String name = header.getName();
            if (name == null) {
                continue;
            }

            String lowerCasedName = name.toLowerCase();
            if (!lowerCasedName.startsWith("x-") &&
                !lowerCasedName.startsWith("old-x-")) {
                continue;
            }
            if ("x-attachment-id".equalsIgnoreCase(lowerCasedName)) {
                continue;
            }
            if ("x-beenthere".equalsIgnoreCase(lowerCasedName)) {
                continue;
            }
            if ("x-keywords".equalsIgnoreCase(lowerCasedName)) {
                continue;
            }
            if ("x-precedence".equalsIgnoreCase(lowerCasedName)) {
                continue;
            }
            if ("x-priority".equalsIgnoreCase(lowerCasedName)) {
                continue;
            }
            if ("x-status".equalsIgnoreCase(lowerCasedName)) {
                continue;
            }

            toRemove.add(name);
        }

        for (String name: toRemove) {
            m.removeHeader(name);
        }
    }

    private static String normalizeSubject(String subject) {
        if (subject == null) {
            return null;
        }

        final int nPatterns = SUBJECT_PATTERNS.size();
        for (int i = 0; i < nPatterns; i++) {
            subject = SUBJECT_PATTERNS.get(i).matcher(
                    subject).replaceAll(SUBJECT_REPLACEMENTS.get(i));
        }
        return subject;
    }

    private static void normalizeRecipients(MimeMessage m,
            Message.RecipientType type) throws MessagingException,
            UnsupportedEncodingException, AddressException {
        m.setRecipients(type, normalizeAddresses(m.getRecipients(type)));
    }

    private static Address[] normalizeAddresses(Address[] recipients)
            throws UnsupportedEncodingException {
        if (recipients == null) {
            return new Address[0];
        }

        List<Address> newRecipients = new ArrayList<Address>();
        for (Address a : recipients) {
            a = normalizeAddress(a);
            newRecipients.add(a);
        }
        return newRecipients.toArray(new Address[recipients.length]);
    }

    private static Address normalizeAddress(Address a)
            throws UnsupportedEncodingException {
        if (a instanceof InternetAddress) {
            InternetAddress ia = (InternetAddress) a;
            if (ia.getPersonal() != null) {
                ia.setPersonal(normalizeAddress(MimeUtility
                        .decodeText(fixGarbledKorean(MimeUtility.unfold(ia
                                .getPersonal()))).trim()), CHARSET);
            }
        }
        return a;
    }

    private static String normalizeAddress(String a) {
        return a.replace("\"(", "(").replace(")\"", ")").replaceAll("(^\" *| *\"$)", "");
    }

    private static String fixGarbledKorean(String value)
            throws UnsupportedEncodingException {
        for (String needle : GARBLED_KOREAN) {
            if (value.indexOf(needle) >= 0) {
                value = new String(value.getBytes("ISO-8859-1"), "CP949");
                break;
            }
        }
        return value;
    }
}
