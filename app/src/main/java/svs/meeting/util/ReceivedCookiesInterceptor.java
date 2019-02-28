package svs.meeting.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/3/30.
 */

public class ReceivedCookiesInterceptor implements Interceptor {
    private Context context;

    public ReceivedCookiesInterceptor(Context context) {
        super();
        this.context = context;

    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        //这里获取请求返回的cookie
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            final StringBuffer cookieBuffer = new StringBuffer();
            List<String> list=originalResponse.headers("Set-Cookie");
            for(String str:list){
                String[] cookieArray = str.split(";");
                cookieBuffer.append(cookieArray[0]).append(";");
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("cookie", cookieBuffer.toString());
            editor.commit();
        }

        return originalResponse;
    }
}
