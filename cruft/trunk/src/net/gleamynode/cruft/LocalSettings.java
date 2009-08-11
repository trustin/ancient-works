package net.gleamynode.cruft;

import java.util.regex.Pattern;

public class LocalSettings {
    
    public static final Pattern EXCLUSION_PATTERN =
        Pattern.compile(
                "(^$" +
                "|^/usr/share/fonts/windows(/|$)" +
                "|^/opt/gentoo-x86(/|$)" +
                "|^/opt/dial(/|$)" +
                "|^/opt/eclipse(/|$)" +
                "|^/opt/rawtherapee(/|$)" +
                "|^/opt/rss2email(/|$)" +
                "|^/opt/sancho(/|$)" +
                "|^/opt/yjp(/|$)" +
                "|^/usr/share/gdm/themes/LinuxGdm(/(?!.*!)|$)" +
                "|^/usr/lib64/nautilus/extensions-2\\.0/libnautilus-actions\\.(la|so)$" +
                "|^/etc/url-monitor\\.conf$" +
                "|^/etc/vpnc/redhat-\\d+\\.conf$" +
                "|^/etc/vpnc/vpnc-script-wrapper$" +
                "|^/etc/fail2ban/action\\.d/hostsdeny-nounban\\.conf$" +
                "|^/etc/fail2ban/action\\.d/mail-whois-euckr\\.conf$" +
                "|^/etc/backup-exclude(/|$)" +
                "|^/etc/env.d/\\d+local$" +
                "|^/var/log/airplay\\.log[^/]*$" +
                "|^/var/log/backup(-[^/]+)?\\.log[^/]*$" +
                "|^/etc/apache2/modules\\.d/\\d+_mod_deflate.conf$" +
                "|^/etc/cron\\.d/url-monitor\\.cron$" +
                "|^/etc/cron\\.daily/backup-noah\\.cron$" +
                "|^/etc/cron\\.daily/eclean\\.cron$" +
                "|^/etc/cron\\.daily/emerge-sync\\.cron$" +
                "|^/etc/cron\\.daily/exim-thaw-all\\.cron$" +
                "|^/etc/cron\\.daily/rss2email\\.cron$" +
                "|^/etc/cron\\.hourly/restart-pdnsd\\.cron$" +
                "|^/etc/cron\\.hourly/scrobble\\.cron$" +
                "|^/etc/cron\\.hourly/update-everydns\\.cron$" +
                "|^/etc/cron\\.hourly/update-livecustomer\\.cron$" +
                "|^/etc/cron\\.weekly/backup\\.cron$" +
                "|^/etc/cron\\.weekly/ping-sitemap\\.cron$" +
                "|^/etc/cron\\.weekly/xfs_fsr\\.cron$" +
                "|^/etc/exim/mailfilter(/|$)" +
                "|^/etc/exim/filter-incoming$" +
                "|^/etc/exim/passwd.client$" +
                "|^/etc/NetworkManager/dispatcher\\.d/reset-resolv\\.conf$" +
                "|^/etc/NetworkManager/dispatcher\\.d/restart-vpnc-infinite$" +
                ")");

}
