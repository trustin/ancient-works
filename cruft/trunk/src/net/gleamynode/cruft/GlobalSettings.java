package net.gleamynode.cruft;

import java.util.regex.Pattern;

public class GlobalSettings {
    public static final Pattern EXCLUSION_PATTERN = Pattern.compile(
            "(^$" +
            "|^/boot(/|$)" +
            "|^/dev(/|$)" +
            "|^/etc/(" +
                     "\\.pwd\\.lock$" +
                     "|adjtime$" +
                     "|csh\\.env$" +
                     "|fstab$" +
                     "|group-?$" +
                     "|hosts(\\.(allow|deny))?$" +
                     "|ld\\.so\\.(conf|cache)$" +
                     "|locale\\.gen$" +
                     "|localtime$" +
                     "|make\\.conf$" +
                     "|modprobe\\.conf$" +
                     "|modules\\.conf$" +
                     "|mtab$" +
                     "|passwd-?$" +
                     "|portage(/|$)" +
                     "|profile\\.(csh|env)$" +
                     "|resolv\\.conf$" +
                     "|runlevels(/|$)" +
                     "|shadow-?$" +
                   ")" +
            "|^/home(/|$)" +
            "|^/lib(32|64)?/(firmware|modules)(/|$)" +
            "|^/media(/|$)" +
            "|^/mnt(/|$)" +
            "|^/proc(/|$)" +
            "|^/root(/|$)" +
            "|^/srv(/|$)" +
            "|^/sys(/|$)" +
            "|^/tmp(/|$)" +
            "|^/usr/(" +
                     "lib(32|64)?/perl\\d+/site_perl/[^/]+/[^/]+(/|$)" +
                     "|local(/|$)" +
                     "|portage(/|$)" +
                     "|share/config$" +
                     "|src(/|$)" +
                     "|tmp(/|$)" +
                   ")" +
            "|^/var/(" +
                     "lib/(" +
                           "init.d(/|$)" +
                           "|portage(/(config|world))?$" +
                         ")" +
                     "|lock/subsys$" +
                     "|log/(" +
                            "portage(/|$)" +
                            "|(dmesg|emerge\\.log|faillog|lastlog|messages|portage/elog/summary.log|wtmp)(\\.\\d+(\\..*)?)?$" +
                          ")" +
                     "|run/(" +
                            "random-seed$" +
                            "|utmp$" +
                          ")" +
                     "|tmp(/|$)" +
                     "|cache((/edb)?/)?$" +
                     "|db((/pkg)?/)?$" +
                     "|www(/|$)" +
                   ")" +
            "|/lost\\+found$" +
            ")");

    public static final Pattern VIRTUAL__OPENGL = Pattern.compile(
            "(^$" +
            "|^/etc/env\\.d/\\d+opengl$" +
            "|^/usr/lib(32|64)?/libGL\\.la$" +
            ")");
    
    public static final Pattern APP_ADMIN__ESELECT = Pattern.compile(
            "(^$" +
            "|^/var/lib/eselect/env/ld-mtimedb$" +
            ")");
    
    public static final Pattern APP_ADMIN__LOGROTATE = Pattern.compile(
            "(^$" +
            "|^/var/lib/logrotate\\.status$" +
            ")");
    
    public static final Pattern APP_ADMIN__SUDO = Pattern.compile(
            "(^$" +
            "|^/var/run/sudo(/[^/]+)?$" +
            ")");
    
    public static final Pattern APP_ADMIN__SYSLOG_NG = Pattern.compile(
            "(^$" +
            "|^/var/lib/syslog-ng\\.persist$" +
            "|^/var/run/syslog-ng\\.pid$" +
            ")");
    
    public static final Pattern APP_ADMIN__SYSSTAT = Pattern.compile(
            "(^$" +
            "|^/var/log/sa(/|$)" +
            ")");
    
    public static final Pattern APP_EMULATION__VMWARE_SERVER = Pattern.compile(
            "(^$" +
            "|^/etc/pam\\.d/vmware-authd$" +
            "|^/etc/vmware/config$" +
            "|^/etc/vmware/license[^/]+$" +
            "|^/etc/vmware/netmap\\.conf$" +
            "|^/etc/vmware/ssl(/rui\\.(crt|key))?$" +
            "|^/etc/vmware/vm-list(-private)?$" +
            "|^/etc/xinetd\\.d/vmware-authd$" +
            "|^/opt/vmware/server/lib/serverd/init\\.pl$" +
            "|^/usr/share/man/man3/VMware::VmPerl[^/]*\\.3pm$" +
            "|^/var/lock/subsys(/vmware)?$" +
            "|^/var/log/vmware(/|$)" +
            "|^/var/run/vmnet-[^/]+\\.pid$" +
            "|^/var/run/vmware(/|$)" +
            ")");
    
    public static final Pattern APP_MISC__SCREEN = Pattern.compile(
            "(^$" +
            "|^/var/run/screen(/S-[^/]+(/[^/]+)?)?$" +
            ")");
    
    public static final Pattern APP_SHELLS__BASH = Pattern.compile(
            "(^$" +
            "|^/etc/bash/bash_logout$" +
            ")");
    
    public static final Pattern APP_TEXT__TETEX = Pattern.compile(
            "(^$" +
            "|^/var/lib/texmf(/|$)" +
            "|^/usr/share/texmf[^/]*/ls-R$" +
            "|^/etc/texmf/ls-R$" +
            ")");
    
    public static final Pattern APP_MISC__CA_CERTIFICATES = Pattern.compile(
            "(^$" +
            "|^/etc/ssl/certs/ca-certificates\\.crt$" +
            ")");
    
    public static final Pattern DEV_DB__MYSQL = Pattern.compile(
            "(^$" +
            "|^/var/log/mysql(/mysqld?\\.(log|err))?$" +
            "|^/var/run/mysqld(/mysqld\\.(pid|sock))?$" +
            "|^/var/run/[^/]+-mysqld$" +
            ")");
    
    public static final Pattern DEV_JAVA__JAVA_CONFIG = Pattern.compile(
            "(^$" +
            "|^/usr/share/java-config-2/vm/[^/]+-jdk-[^/]+$" +
            "|^/opt/[^/]+-jdk-[^/]+(/|$)" +
            ")");
    
    public static final Pattern DEV_LANG__PERL = Pattern.compile(
            "(^$" +
            "|^/usr/lib64/perl(\\d+)?/[^/]+/[^/]+/Encode/ConfigLocal\\.pm$" +
            "|^/usr/lib64/perl(\\d+)?/[^/]+/[^/]+/perllocal.pod$" +
            "|^/usr/lib64/perl(\\d+)?/vendor_perl/[^/]+/XML/SAX/ParserDetails\\.ini$" +
            ")");
    
    public static final Pattern DEV_LANG__PHP = Pattern.compile(
            "(^$" +
            "|^/usr/share/php/\\.channels(/|$)" +
            "|^/usr/share/php/\\.registry(/|$)" +
            "|^/usr/share/php/\\.(lock|depdb|depdblock|filemap)$" +
            ")");
    
    public static final Pattern DEV_RUBY__RUBYGEMS = Pattern.compile(
            "(^$" +
            "|^/usr/lib(32|64)?/ruby/gems/[^/]+/source_cache$" +
            "|^/usr/bin/rubyforge$" +
            "|^/usr/bin/sow$" +
            ")");
    
    public static final Pattern GNOME_BASE__GCONF = Pattern.compile(
            "(^$" +
            "|^/etc/gconf/gconf\\.xml\\.defaults/%gconf-tree(-[^/]+)?\\.xml$" +
            ")");
    
    public static final Pattern GNOME_BASE__GDM = Pattern.compile(
            "(^$" +
            "|^/var/lib/gdm$" +
            "|^/var/lib/gdm/\\.scim$" +
            "|^/var/lib/gdm/\\.fontconfig/[^/]+$" +
            "|^/var/log/gdm(/|$)" +
            "|^/var/gdm/\\.(cookie|gdmfifo)$" +
            "|^/var/gdm/[^/]+\\.(Xauth|Xservers)$" +
            "|^/var/run/gdm[\\._](pid|socket)$" +
            ")");
    
    public static final Pattern KDE_BASE__KDELIBS = Pattern.compile(
            "(^$" +
            "|^/usr/share/services/ksycoca$" +
            "|^/usr/kde/[^/]+/share/services/ksycoca$" +
            "|^/usr/kde/[^/]+/share/templates/\\.source/emptydir$" +
            ")");
    
    public static final Pattern MAIL_MTA__EXIM = Pattern.compile(
            "(^$" +
            "|^/etc/exim/exim\\.conf$" +
            "|^/etc/ssl/exim/server\\.(crt|pem|key)$" +
            "|^/var/run/exim\\.pid$" +
            "|^/var/spool/exim/input(/[^/]+)?$" +
            "|^/var/spool/exim/msglog$" +
            "|^/var/spool/exim/db/retry(\\.lockfile)?$" +
            "|^/var/spool/exim/db/wait-remote_smtp(\\.lockfile)?$" +
            ")");
    
    public static final Pattern MEDIA_GFX__GRAPHVIZ = Pattern.compile(
            "(^$" +
            "|^/usr/lib(32|64)?/graphviz/config$" +
            ")");
    
    public static final Pattern MEDIA_LIBS__MESA = VIRTUAL__OPENGL;
    
    public static final Pattern MEDIA_SOUND__ALSA_UTILS = Pattern.compile(
            "(^$" +
            "|^/var/run/pulse(/|$)" +
            ")");
    
    public static final Pattern MEDIA_SOUND__PULSEAUDIO = Pattern.compile(
            "(^$" +
            "|^/var/lib/alsa/asound\\.state$" +
            "|^/etc/asound\\.conf$" +
            ")");
    
    public static final Pattern NET_ANALYZER__FAIL2BAN = Pattern.compile(
            "(^$" +
            "|^/var/log/fail2ban\\.log[^/]*$" +
            ")");
    
    public static final Pattern NET_DNS__PDNSD = Pattern.compile(
            "(^$" +
            "|^/etc/pdnsd/pdnsd\\.conf$" +
            "|^/var/run/pdnsd\\.pid$" +
            ")");
    
    public static final Pattern NET_MAIL__DOVECOT = Pattern.compile(
            "(^$" +
            "|^/etc/ssl/dovecot/server\\.(crt|pem|key)$" +
            "|^/var/lib/dovecot/ssl-parameters\\.dat$" +
            "|^/var/run/dovecot/auth-(master|worker\\.\\d+)$" +
            "|^/var/run/dovecot/dict-server$" +
            "|^/var/run/dovecot/login/default$" +
            "|^/var/run/dovecot/login/ssl-parameters\\.dat$" +
            "|^/var/run/dovecot/master\\.pid$" +
            ")");
    
    public static final Pattern NET_MAIL__FETCHMAIL = Pattern.compile(
            "(^$" +
            "|^/etc/fetchmailrc$" +
            "|^/var/run/fetchmail\\.pid$" +
            ")");
    
    public static final Pattern NET_MISC__DHCDBD = Pattern.compile(
            "(^$" +
            "|^/var/lib/dhcp/dhclient\\.leases$" +
            "|^/var/run/dhcdbd\\.pid$" +
            ")");
    
    public static final Pattern NET_MISC__DHCP = Pattern.compile(
            "(^$" +
            "|^/var/lib/dhcp/dhclient\\.leases$" +
            "|^/var/run/dhclient-[^/]+\\.pid$" +
            ")");
    
    public static final Pattern NET_MISC__DHCPCD = Pattern.compile(
            "(^$" +
            "|^/var/lib/dhcpcd/dhcpcd-[^/]+\\.info$" +
            "|^/var/lib/dhcpcd/dhcpcd.duid$" +
            "|^/var/run/dhcpcd(-[^/]+)?\\.pid$" +
            ")");
    
    public static final Pattern NET_MISC__NETWORKMANAGER = Pattern.compile(
            "(^$" +
            "|^/var/run/NetworkManager(Dispatcher)?\\.pid$" +
            ")");
    
    public static final Pattern NET_MISC__NTP = Pattern.compile(
            "(^$" +
            "|^/var/lib/ntp/ntp\\.drift$" +
            "|^/var/run/ntpd\\.pid$" +
            ")");
    
    public static final Pattern NET_MISC__NXSERVER_FREEEDITION = Pattern.compile(
            "(^$" +
            "|^/usr/NX/home/nx/\\.Xauthority$" +
            "|^/usr/NX/home/nx/\\.ssh/authorized_keys\\d+?$" +
            "|^/usr/NX/home/nx/\\.ssh/known_hosts$" +
            "|^/usr/NX/etc(/|$)" +
            "|^/usr/NX/var/db/broadcast$" +
            "|^/usr/NX/var/db/running/(display|sessionId).*$" +
            "|^/var/run/sshd\\.pid$" +
            ")");
    
    public static final Pattern NET_MISC__OPENSSH = Pattern.compile(
            "(^$" +
            "|^/etc/ssh/ssh_host_([a-z]+_)?key(\\.pub)?$" +
            "|^/var/run/sshd\\.pid$" +
            ")");
    
    public static final Pattern NET_MISC__RSYNC = Pattern.compile(
            "(^$" +
            "|^/var/run/rsyncd\\.pid$" +
            ")");
    
    public static final Pattern NET_MISC__VPNC = Pattern.compile(
            "(^$" +
            "|^/var/run/vpnc/pid$" +
            ")");
    
    public static final Pattern NET_P2P__MLDONKEY = Pattern.compile(
            "(^$" +
            "|^/var/run/mldonkey\\.pid$" +
            ")");
    
    public static final Pattern NET_PRINT__CUPS = Pattern.compile(
            "(^$" +
            "|^/etc/cups/ppd(/[^/]+\\.ppd)?$" +
            "|^/etc/cups/printers\\.conf(\\.O)?$" +
            "|^/etc/printcap$" +
            "|^/var/run/cups/cups\\.sock$" +
            "|^/var/run/cups/certs/\\d+$" +
            "|^/var/spool/cups/c\\d+$" +
            "|^/var/log/cups/(access|error|page)_log(\\.O)?$" +
            ")");
    
    public static final Pattern NET_WWW__NSPLUGINWRAPPER = Pattern.compile(
            "(^$" +
            "|^/usr/lib(32|64)?/nsbrowser/plugins/npwrapper\\.[^/]+\\.so$" +
            ")");
    
    public static final Pattern SYS_APPS__DBUS = Pattern.compile(
            "(^$" +
            "|^/var/run/dbus\\.pid$" +
            "|^/var/lib/dbus/machine-id$" +
            "|^/var/run/dbus/system_bus_socket$" +
            ")");
    
    public static final Pattern SYS_APPS__HAL = Pattern.compile(
            "(^$" +
            "|^/var/cache/hald/fdi-cache$" +
            "|^/var/run/hald.pid$" +
            ")");
    
    public static final Pattern SYS_APPS__LOGWATCH = Pattern.compile(
            "(^$" +
            "|^/etc/logwatch/conf/override\\.conf$" +
            ")");
    
    public static final Pattern SYS_APPS__MAN = Pattern.compile(
            "(^$" +
            "|^/usr/share/man/whatis$" +
            ")");
    
    public static final Pattern SYS_APPS__MLOCATE = Pattern.compile(
            "(^$" +
            "|^/var/lib/mlocate/mlocate\\.db$" +
            ")");
    
    public static final Pattern SYS_APPS__SMARTMONTOOLS = Pattern.compile(
            "(^$" +
            "|^/var/run/smartd\\.pid$" +
            ")");

    public static final Pattern SYS_APPS__TEXINFO = Pattern.compile(
            "(^$" +
            "|^/usr/share/(.+/)?info/dir$" +
            ")");

    public static final Pattern SYS_APPS__XINETD = Pattern.compile(
            "(^$" +
            "|^/var/run/xinetd\\.pid$" +
            ")");

    public static final Pattern SYS_DEVEL__BINUTILS = Pattern.compile(
            "(^$" +
            "|^/etc/env\\.d/\\d+binutils$" +
            "|^/etc/env\\.d/binutils/config-[^/]+$" +
            ")");
    
    public static final Pattern SYS_DEVEL__GCC = Pattern.compile(
            "(^$" +
            "|^/etc/env\\.d/\\d+gcc-[^/]+$" +
            "|^/etc/env\\.d/gcc/config-[^/]+$" +
            "|^/lib(32|64)?/libgcc_s[^/]+$" +
            "|^/lib(32|64)?/cpp$" +
            "|^/usr/bin/c\\+\\+$" +
            "|^/usr/bin/c89$" +
            "|^/usr/bin/c99$" +
            "|^/usr/bin/cc$" +
            "|^/usr/bin/cpp$" +
            "|^/usr/bin/g\\+\\+$" +
            "|^/usr/bin/gcc$" +
            "|^/usr/bin/gcov$" +
            "|^/usr/bin/[^/]+-linux-gnu-c\\+\\+$" +
            "|^/usr/bin/[^/]+-linux-gnu-cpp$" +
            "|^/usr/bin/[^/]+-linux-gnu-g\\+\\+$" +
            "|^/usr/bin/[^/]+-linux-gnu-gcc$" +
            "|^/lib(32|64)?/rcscripts/awk/fixlafiles\\.awk$" +
            "|^/sbin/fix_libtool_files\\.sh$" +
            ")");
    
    public static final Pattern SYS_FS__MDADM = Pattern.compile(
            "(^$" +
            "|^/var/run/mdadm\\.pid$" +
            ")");
    
    public static final Pattern SYS_FS__UDEV = Pattern.compile(
            "(^$" +
            "|^/etc/udev/rules\\.d/70-persistent-[^/]+\\.rules$" +
            ")");
    
    public static final Pattern SYS_KERNEL__GENKERNEL = Pattern.compile(
            "(^$" +
            "|^/etc/kernels(/kernel-config-[^/]+)?$" +
            "|^/var/log/genkernel\\.log(\\.\\d+(\\..*)?)?$" +
            ")");
    
    public static final Pattern SYS_KERNEL__MODULE_REBUILD = Pattern.compile(
            "(^$" +
            "|^/var/lib/module-rebuild(/moduledbe?)?$" +
            ")");
    
    public static final Pattern SYS_LIBS__CRACKLIB = Pattern.compile(
            "(^$" +
            "|^/usr/lib(32|64)?/cracklib_dict\\.(hwm|pwd|pwi)$" +
            ")");
    
    public static final Pattern SYS_LIBS__GLIBC = Pattern.compile(
            "(^$" +
            "|^/usr/lib(32|64)?/gconv/gconv-modules\\.cache$" +
            "|^/usr/lib(32|64)?/locale/locale-archive$" +
            ")");
    
    public static final Pattern SYS_LIBS__GPM = Pattern.compile(
            "(^$" +
            "|^/var/run/gpm\\.pid$" +
            ")");
    
    public static final Pattern SYS_PROCESS__ANACRON = Pattern.compile(
            "(^$" +
            "|^/var/spool/anacron/cron\\.(daily|monthly|weekly)$" +
            ")");
    
    public static final Pattern SYS_PROCESS__VIXIE_CRON = Pattern.compile(
            "(^$" +
            "|^/var/run/cron\\.pid$" +
            "|^/var/spool/cron/crontabs/[^/]+$" +
            "|^/var/spool/cron/lastrun/cron\\.(daily|hourly|monthly|weekly)$" +
            "|^/var/log/cron\\.log[^/]*$" +
            ")");
    
    public static final Pattern WWW_SERVERS__APACHE = Pattern.compile(
            "(^$" +
            "|^/etc/apache2/ssl/server\\.(crt|pem|key)$$" +
            "|^/var/log/apache2/(access|deflate|error|ssl_access|ssl_error|ssl_request)_log[^/]*$" +
            "|^/var/run/apache2\\.pid$" +
            "|^/var/run/cgisock\\.\\d+$" +
            ")");
    
    public static final Pattern X11_BASE__XORG_SERVER = Pattern.compile(
            "(^$" +
            "|^/etc/X11/xorg\\.conf$" +
            "|^/var/log/Xorg\\.\\d+\\.log(\\.old)?$" +
            ")");
    
    public static final Pattern X11_DRIVERS__NVIDIA_DRIVERS = VIRTUAL__OPENGL;
    
    public static final Pattern X11_LIBS__GTK_ = Pattern.compile(
            "(^$" +
            "|^/etc/gtk-2\\.0(/[^/]+(/gdk-pixbuf\\.loaders)?)?$" +
            "|^/etc/gtk-2\\.0(/[^/]+(/gtk\\.immodules)?)?$" +
            ")");
    
    public static final Pattern X11_LIBS__PANGO = Pattern.compile(
            "(^$" +
            "|^/etc/pango(/[^/]+(/pango\\.modules)?)?$" +
            ")");
    
    public static final Pattern X11_LIBS__WXGTK = Pattern.compile(
            "(^$" +
            "|^/var/lib/wxwidgets/current$" +
            ")");
    
    public static final Pattern X11_MISC__SHARED_MIME_INFO = Pattern.compile(
            "(^$" +
            "|^/usr/share/mime/[^/]+(/[^/]+\\.xml)?$" +
            "|^/usr/share/applications/mimeinfo\\.cache$" +
            ")");
}
