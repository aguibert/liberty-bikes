/**
 *
 */
package org.libertybikes.player.service;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@ApplicationScoped
public class DisableSSLVerificationBean {

    public void trustAllCertificates(@Observes @Initialized(ApplicationScoped.class) Object init) {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        } };

        try {
            System.out.println("@AGG default SSLContext is: " + SSLContext.getDefault().getProtocol());
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            System.out.println("Unable to install custom SSL socket factory to trust self-signed certificates");
            e.printStackTrace();
        }
        System.out.println("Installed no-op TrustManager");
    }

}
