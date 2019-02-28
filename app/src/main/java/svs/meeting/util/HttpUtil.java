package svs.meeting.util;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import svs.meeting.app.MyApplication;
import svs.meeting.security.MyHostNameVerifier;
import svs.meeting.security.MyTrustManager;
import svs.meeting.util.httputils.HttpClientSslHelper;

public class HttpUtil {

    public static final int HTTP_OK = 0x99000;
    public static final int HTTP_ERROR = 0x99111;
    public static String sessionValue;

    public static void requestURL(final String url, final IHttpCallback callback) {
        requestURL(url, null, callback);
    }

    public static void requestURL(final String url, final java.util.HashMap<String, String> params, final IHttpCallback callback){
        requestURL(url,params,callback,"POST");
    }

    public static void requestURL(final String url, final java.util.HashMap<String, String> params, final IHttpCallback callback, final String method) {
        new Thread() {
            public void run() {
                XLog.log("网络请求URL==》" + url+",METHOD="+method);
                XLog.log("请求参数params==》" + params.toString());
                try {

                    //String str=Helper.post(url, params);
                    String str = sendRequest(url, params,method);

                    XLog.log("网络返回return==》" + str,HttpUtil.class);
                    callback.onHttpComplete(HTTP_OK, str);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String strEx = ex.toString();
                    callback.onHttpComplete(HTTP_ERROR, strEx);
                }
            }
        }.start();
    }

    public static String post(String url, java.util.HashMap<String, String> params) {
        return sendRequest(url,params,"POST");
    }

    private static String sendRequest(String url, java.util.HashMap<String, String> params, String method) {
        try {
            HttpURLConnection conn = null;
            if (url.toLowerCase().startsWith("https")) {
                conn = (HttpsURLConnection) new URL(url).openConnection();
                if (conn instanceof HttpsURLConnection) {
                    SSLContext sslContext = HttpClientSslHelper.getSslContext(MyApplication.applicationContext);
                    ((HttpsURLConnection) conn).setSSLSocketFactory(sslContext.getSocketFactory());
                }
            } else {
                conn = (HttpURLConnection) new URL(url).openConnection();
            }

            conn.setReadTimeout(5 * 1000);
            conn.setDoInput(true);

            conn.setUseCaches(false);
            conn.setRequestMethod(method); // Post or GET
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");

            if(method.equals("POST")){
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                if (sessionValue != null)
                    conn.setRequestProperty("Cookie", sessionValue);

                DataOutputStream outStream = new DataOutputStream(
                        conn.getOutputStream());
                if (params != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (sb.length() > 0)
                            sb.append("&");
                        sb.append(entry.getKey());
                        sb.append("=");
                        sb.append(entry.getValue());
                    }

                    outStream.write(sb.toString().getBytes());
                    outStream.flush();
                }
                outStream.close();
            }





            if (conn.getHeaderField("Set-Cookie") != null)
                sessionValue = conn.getHeaderField("Set-Cookie");

            //boolean success = conn.getResponseCode()==200;
            XLog.log("返回代码:"+conn.getResponseCode());
            InputStream in = conn.getInputStream();
            InputStreamReader isReader = new InputStreamReader(in);
            BufferedReader bufReader = new BufferedReader(isReader);
            String line = null;
            String data = "";
            while ((line = bufReader.readLine()) != null)
                data += line;


            conn.disconnect();
            return data;
        } catch (Exception e) {
            XLog.log(HttpUtil.class.getName() + e.getMessage());
        }
        return "";
    }


    public static void GetHttps(String url) {
        String https = url;// = "https://eway.tech/ds/opers.test";
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostNameVerifier());
            HttpsURLConnection conn = (HttpsURLConnection) new URL(https).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET"); // Post��ʽ  
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            XLog.log("" + "connecting.........." + conn);
            try {
                conn.connect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "gbk"));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);

            XLog.log("=========" + sb.toString());

        } catch (Exception e) {
            XLog.log("=========" + e.getMessage());
            XLog.log(HttpUtil.class.getName() + e.getMessage());
        }

    }

}
