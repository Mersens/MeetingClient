package svs.meeting.security;

import android.util.Log;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class MyTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
            // TODO Auto-generated method stub
    	Log.i("", "checkClientTrusted");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
            // TODO Auto-generated method stub
    	Log.i("", "checkServerTrusted");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
    		Log.i("", "getAcceptedIssuers");
            return null;
    }        
}