package svs.meeting.util.httputils;

import android.content.Context;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;


/**
 * Created by 刘灿成 on 2018/2/23 0023.
 *  https安全认证
 */

public class HttpClientSslHelper {

    public static final String KEY_STORE_CLIENT_PATH = "https_client.bks";
    public static final String KEY_STORE_TYPE = "BKS";
    public static final String KEY_STORE_PASSWORD = "123456";

    public static final String P12KEYPATH = "lianmeng.p12";
    public static final String P12KEYPW = "123456";
    public static final String P12TYPE = "PKCS12";

    public static PrivateKey clientCertPrivateKey = null;
    public static X509Certificate[] certificatesChain = null;

    private static SSLContext sslContext = null;

    public static SSLContext getSslContext(Context context){

        if (sslContext == null){
            sslContext = setP12Certificates(context);
        }

        return sslContext;
    }

    /**
     * P12
     */
    private static SSLContext setP12Certificates(Context context){
        if (sslContext == null){
            try {
                InputStream inputStream = context.getAssets().open(P12KEYPATH);

                sslContext = SSLContext.getInstance("TLS");

                KeyStore clientKeyStore = KeyStore.getInstance(P12TYPE);
                clientKeyStore.load(inputStream , P12KEYPW.toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(clientKeyStore, P12KEYPW.toCharArray());

                sslContext.init(
                        keyManagerFactory.getKeyManagers(),
                        new X509TrustManager[]{new MyX509TrustManager()},
                        null);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sslContext;
    }

    /**
     * webview里证书验证请求
     */
    public static void initPrivateKeyAndX509Certificate(Context context) throws Exception {
        KeyStore keyStore  = KeyStore.getInstance(P12TYPE);
        InputStream inputStream = context.getAssets().open(P12KEYPATH);
        keyStore.load( inputStream , P12KEYPW.toCharArray());
        Enumeration<?> localEnumeration  = keyStore.aliases();
        while (localEnumeration.hasMoreElements()) {
            String str3 = (String) localEnumeration.nextElement();
            clientCertPrivateKey = (PrivateKey) keyStore.getKey(str3, P12KEYPW.toCharArray());
            if (clientCertPrivateKey == null) {
                continue;
            }else{
                Certificate[] arrayOfCertificate = keyStore.getCertificateChain(str3);
                certificatesChain = new X509Certificate[arrayOfCertificate.length];
                for (int j = 0; j < certificatesChain.length; j++) {
                    certificatesChain[j] = ((X509Certificate) arrayOfCertificate[j]);
                }
            }
        }
    }

    /**
     * BKS
     */
    private static SSLContext setBKSCertificates(Context context){
        if (sslContext == null) {
            try {
                InputStream inputStream = context.getAssets().open(KEY_STORE_CLIENT_PATH);

                sslContext = SSLContext.getInstance("TLS");

                KeyStore clientKeyStore = KeyStore.getInstance(KEY_STORE_TYPE);
                clientKeyStore.load(inputStream , KEY_STORE_PASSWORD.toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(clientKeyStore, KEY_STORE_PASSWORD.toCharArray());

                sslContext.init(
                        keyManagerFactory.getKeyManagers(),
                        new X509TrustManager[]{new MyX509TrustManager()},
                        null);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sslContext;
    }


    private static class MyX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            //该方法检查客户端的证书，若不信任该证书则抛出异常。
            // 由于我们不需要对客户端进行认证，
            // 因此我们只需要执行默认的信任管理器的这个方法。
            // JSSE中，默认的信任管理器类为TrustManager。
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String s) throws CertificateException {
            //　该方法检查服务器的证书，若不信任该证书同样抛出异常。
            // 通过自己实现该方法，可以使之信任我们指定的任何证书。
            // 在实现该方法时，也可以简单的不做任何处理，即一个空的函数体，由于不会抛出异常，它就会信任任何证书。
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
            //返回受信任的X509证书数组。
        }
    }
}
