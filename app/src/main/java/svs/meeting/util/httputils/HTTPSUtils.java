package svs.meeting.util.httputils;

import android.content.Context;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2018/3/18.
 */

public class HTTPSUtils {
    private OkHttpClient client;

    public Context mContext;


    /**
     * 获取OkHttpClient实例
     *
     * @return
     */
    public OkHttpClient getInstance() {
        return client;
    }

    /**
     * 初始化HTTPS,添加信任证书
     *
     * @param context
     */
    public HTTPSUtils(Context context) {
        mContext = context;
        SSLSocketFactory sslSocketFactory;
        try {
            SSLContext sslContext = HttpClientSslHelper.getSslContext(mContext);
            sslSocketFactory = sslContext.getSocketFactory();
            client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
