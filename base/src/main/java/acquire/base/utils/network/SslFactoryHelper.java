package acquire.base.utils.network;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * SSL certificate factory utils
 *
 * <p>e.g. 1. Load SSL certificate,one way authentication (Client verification server)
 * <pre>
 *    TrustManager[] trustManagers = SslFactoryHelper.generateTrustManagers(cers);
 *    SSLSocketFactory sslSocketFactory = SslFactoryHelper.getSslSocketFactory(null, trustManagers);
 * </pre>
 * <p>
 * e.g. 2. Load SSL certificate, mutual authentication
 * <pre>
 *    TrustManager[] trustManagers = SslFactoryHelper.generateTrustManagers(trustCers);
 *    KeyManager[] keyManagers = SslFactoryHelper.generateKeyManagers(keyManagerCers)
 *    SSLSocketFactory sslSocketFactory = SslFactoryHelper.getSslSocketFactory(keyManagers, trustManagers);
 * </pre>
 * e.g. 3. Trust all
 * <pre>
 *     TrustManager[] trustManagers = SslFactoryHelper.generateDefaultTrustManagers();
 *     SSLSocketFactory sslSocketFactory = SslFactoryHelper.getTrusAllSocketFactory();
 * </pre>
 * e.g. 4. Use system certificate
 * <pre>
 *     InputStream cer = SslFactoryHelper.getCerFromSystem(cerSn);
 *     TrustManager[] trustManagers = SslFactoryHelper.generateTrustManagers(cer);
 *     SSLSocketFactory sslSocketFactory = SslFactoryHelper.getSslSocketFactory(null, trustManagers);
 * </pre>
 *
 * @author Janson
 * @date 2018/3/26
 */
public class SslFactoryHelper {
    private final static String PROTOCOL = "SSL";
    /**
     * SSL certificate password
     * eg."123456".toCharArray();
     */
    private final static char[] CLIENT_KEY_PASSWORD = null;
    /**
     * SSL certificate type
     * eg. KeyStore.getDefaultType() 或 "BKS";
     */
    private final static String KEY_TYPE = KeyStore.getDefaultType();

    /**
     * Create {@link SSLSocketFactory}
     *
     * @param kms The client certificate to verify the server. One way verify: NullAble; two way verify: NonNull
     * @param tms Trust certificate。It's server certificate to verify the client.
     * @return {@link SSLSocketFactory}
     */
    @Nullable
    public static SSLSocketFactory getSslSocketFactory(KeyManager[] kms, TrustManager[] tms) {
        try {
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(kms, tms, null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get certificate from system ca store.
     *
     * @param cerSn hex certificate serial number
     * @return certificate bytes input stream
     */
    public static InputStream getCerFromSystem(String cerSn) {
        try {
            BigInteger serialNumber = new BigInteger(cerSn, 16);
            KeyStore keyStore = KeyStore.getInstance("AndroidCAStore");
            keyStore.load(null, null);
            Enumeration<String> ss = keyStore.aliases();
            while (ss.hasMoreElements()) {
                String alias = ss.nextElement();
                X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                if (serialNumber.compareTo(cert.getSerialNumber()) == 0) {
                    return new ByteArrayInputStream(cert.getEncoded());
                }
            }
        } catch (IllegalArgumentException | IOException | NoSuchAlgorithmException |
                 CertificateException | KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Trust all
     *
     * @return {@link SSLSocketFactory}
     */
    @Nullable
    public static SSLSocketFactory getTrustAllSocketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
            }
        }};
        try {
            // return SSLContext.getDefault().getSocketFactory(); //also can
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(null, trustAllCerts, null);
            return sslContext.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private static KeyStore generateKeyStore(@NonNull InputStream... certificates) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        KeyStore keyStore = KeyStore.getInstance(KEY_TYPE);
        //CLIENT_KEY_PASSWORD can be null
        keyStore.load(null, CLIENT_KEY_PASSWORD);
        int index = 0;
        /*
         * Load certificate
         */
        for (InputStream certificate : certificates) {
            if (certificate == null) {
                return null;
            }

            String alias = Integer.toString(index++);
            keyStore.setCertificateEntry(alias, certificateFactory.generateCertificate(certificate));
            certificate.close();
        }

        //no certificate
        if (index == 0) {
            return null;
        }
        return keyStore;
    }

    /**
     * Generate {@link KeyManager}. If one way verify, not need to invoke this method
     *
     * @param keyManagerCers The client certificate to verify the server
     * @return {@link KeyManager[]
     */
    public static KeyManager[] generateKeyManagers(InputStream... keyManagerCers) {
        if (keyManagerCers == null) {
            return null;
        }
        try {
            KeyStore keyStore = generateKeyStore(keyManagerCers);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, CLIENT_KEY_PASSWORD);
            return keyManagerFactory.getKeyManagers();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException |
                 UnrecoverableKeyException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Trust certificate。It's server certificate to verify the client.
     */
    public static TrustManager[] generateTrustManagers(InputStream... trustCers) {
        if (trustCers == null) {
            return null;
        }
        try {
            KeyStore keyStore = generateKeyStore(trustCers);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            return trustManagers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generate default {@link TrustManager}
     */
    @Nullable
    public static TrustManager[] generateDefaultTrustManagers() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            return trustManagers;
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

}
