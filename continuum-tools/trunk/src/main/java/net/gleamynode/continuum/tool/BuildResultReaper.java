/*
 * Continuum Tools - Useful command line tools for Continuum
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
package net.gleamynode.continuum.tool;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.maven.continuum.xmlrpc.client.ContinuumXmlRpcClient;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;

public class BuildResultReaper {

    private static final long MAX_AGE =
        TimeUnit.MILLISECONDS.convert(6, TimeUnit.HOURS);

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println(BuildResultReaper.class.getName() +
                    " <URL> <username> <password>");
            return;
        }

        URL url = new URL(args[0]);
        String username = args[1];
        String password = args[2];

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                // NOOP
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                System.out.println("SSL authentication type: " + authType);
                System.out.println("Accepting the following certificate issuers:");
                for (X509Certificate cert: certs) {
                    System.out.println(
                            "* " + cert.getIssuerX500Principal().getName());
                }
            }
        } };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        ContinuumXmlRpcClient client =
            new ContinuumXmlRpcClient(url, username, password);

        long currentTime = System.currentTimeMillis();

        for (ProjectGroupSummary pgs: client.getAllProjectGroups()) {
            for (ProjectSummary ps: client.getProjects(pgs.getId())) {
                System.out.println("Project: " + ps.getName());
                for (BuildResultSummary brs: client.getBuildResultsForProject(ps.getId())) {

                    // Skip the builds in progress
                    if (brs.getState() == ContinuumProjectState.BUILDING) {
                        continue;
                    }

                    boolean remove = false;

                    // Remove the failed build result which is older than MAX_AGE.
                    if (brs.getState() == ContinuumProjectState.OK &&
                        brs.getEndTime() != 0 &&
                        currentTime - brs.getEndTime() > MAX_AGE) {
                        remove = true;
                    }

                    // Remove the build result which didn't even run.
                    else if (brs.getBuildNumber() == 0) {
                        remove = true;
                    }

                    if (remove) {
                        System.out.println("* Removing build #" + brs.getBuildNumber());
                        client.removeBuildResult(client.getBuildResult(ps.getId(), brs.getId()));
                    }
                }
            }
        }
    }
}
