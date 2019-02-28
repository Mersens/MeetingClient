package svs.meeting.util.httputils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.webkit.ClientCertRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import svs.meeting.util.XLog;
import svs.meeting.widgets.FWebView;

/**
 * Created by 刘灿成 on 2018/2/27 0027.
 *
 *  webview双向认证 重写WebViewClient
 *
 *     1. 双向认证在android5.0以上很好解决，
 *       但是在Android5.0以下，webviewclient中没有客户端向服务器发送证书的回调接口（回调是个隐藏函数）
 *
 *     2.拦截Webview的Request的请求，然后自己实现httpconnection捞取数据,
 *       然后返回新的WebResourceResponse给Webview。
 *       重写webviewclient中的shouldInterceptRequest方法即可。
 */

public class SslPinningWebViewClient extends WebViewClient {

    private Context context;
    private SSLContext sslContext = null;
    private FWebView fwebView = null;
    private boolean isErro = false;

    public SslPinningWebViewClient(Context context )throws IOException {
        this.context = context;
//        sslContext = HttpClientSslHelper.getSslContext(context);
    }

    public SslPinningWebViewClient(Context context , FWebView fWebView)throws IOException {
        this.context = context;
        this.fwebView = fWebView;
    }


    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        //6.0以下执行
        isErro = true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (isErro){
            if(fwebView != null){
                fwebView.showErroMsg();
            }
        }else{
            if(fwebView != null){
                fwebView.hideErrorPage();
            }
        }
        isErro = false;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        //6.0以上执行
        isErro = true;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        try {
//            if(url.startsWith("weixin://") || url.startsWith("alipays://") ||
//                    url.startsWith("mailto://") || url.startsWith("taobao://")
//                //其他自定义的scheme
//                    ) {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                context.startActivity(intent);
//                return true;
//            }
//        } catch (Exception e) { //防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
//            return false;
//        }
//        //处理http和https开头的url
//        view.loadUrl(url);
//        return true;
        if( url.startsWith("http:") || url.startsWith("https:") ) {
            return false;
        }
        XLog.log("---"+url);
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity( intent );
        }catch(Exception e){}
        return true;
    }



    /**
     * 提醒主机应用程序处理SSL客户端认证请求，如果客户端提供的keys符合，
     * 主机应用程序响应客户端请求并展示UI界面。主机应用程序有三种方法响应：
     * 第一种，proceed()；
     * 第二种，cancel()；
     * 第三种，ignore()；
     * 如果调用proceed()或cancel()，WebView在内存保留请求的结果，同时同一请求不会再次回调onReceivedClientCertRequest()方法。
     * 如果调用ignore()方法，WebView不保存请求结果。
     *  由于网络堆栈的多层级缓存请求结果，因此ignore()响应方式是唯一最好的选择。
     *  onReceivedClientCertRequest()方法在UI线程中调用，在回调该方法时，请求网络连接是暂停的。
     * @param view
     * @param request
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        try {
            if (HttpClientSslHelper.clientCertPrivateKey == null ||
                    HttpClientSslHelper.certificatesChain == null){
                HttpClientSslHelper.initPrivateKeyAndX509Certificate(context);
            }

            request.proceed(HttpClientSslHelper.clientCertPrivateKey , HttpClientSslHelper.certificatesChain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * https双向认证
     * @param uri
     * @return
     */
    private WebResourceResponse processRequest(Uri uri) {
        sslContext = HttpClientSslHelper.getSslContext(context);
        try {
            URL url = new URL(uri.toString());
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

            urlConnection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            InputStream is = urlConnection.getInputStream();
            String contentType = urlConnection.getContentType();
            String encoding = urlConnection.getContentEncoding();
            if(contentType != null) {
                String mimeType = contentType;
                if (contentType.contains(";")) {
                    mimeType = contentType.split(";")[0].trim();
                }
                return new WebResourceResponse(mimeType, encoding, is);
            }
        } catch (SSLHandshakeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
