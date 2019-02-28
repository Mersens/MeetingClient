package svs.meeting.util;

import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import svs.meeting.app.MyApplication;
import svs.meeting.data.Config;
import svs.meeting.service.ServiceStore;


/**
 * Created by Mersens on 2016/9/12.
 */
public class RequestManager {
    public final static int CONNECT_TIMEOUT = 100;
    public final static int READ_TIMEOUT = 100;
    public final static int WRITE_TIMEOUT = 100;
    private static RequestManager mRequestManager;//管理者实例
    public Retrofit mRetrofit;
    public OkHttpClient mClient;//OkHttpClient实例
    public ServiceStore mServiceStore;//请求接口
    private SSLSocketFactory sslSocketFactory;

    private RequestManager() {
        init();
    }

    //单例模式，对提供管理者实例
    public static RequestManager getInstance() {
        if (mRequestManager == null) {
            synchronized (RequestManager.class) {
                if (mRequestManager == null) {
                    mRequestManager = new RequestManager();
                }
            }
        }
        return mRequestManager;
    }

    private void init() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        builder.interceptors().add(new ReceivedCookiesInterceptor(MyApplication.getInstance().getApplicationContext()));
        builder.interceptors().add(new AddCookiesInterceptor(MyApplication.getInstance().getApplicationContext()));
        builder.interceptors().add(new MyInterceptor());
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder().addHeader("Connection", "close").build();
                String url = request.url().toString();
                Log.e("intercept", url);
                return chain.proceed(request);
            }
        });
        builder.sslSocketFactory(sslSocketFactory);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        mClient = builder.build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(Config.WEB_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mClient)
                .build();

        mServiceStore = mRetrofit.create(ServiceStore.class);
    }

    public interface onRequestCallBack {
        void onSuccess(String msg);

        void onError(String error);
    }

}
