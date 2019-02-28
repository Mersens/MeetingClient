package svs.meeting.widgets;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import svs.meeting.app.R;


public class FWebView extends LinearLayout {

    private Context context;
    private WebView webView;
    private ProgressBar progressBar;
    private View mErrorView;


    public FWebView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater from = LayoutInflater.from(context);
        View view = from.inflate(R.layout.layout_f_webview , this);

        /*加载信息*/
        progressBar = (ProgressBar) view.findViewById(R.id.fwv_progressbar);
        webView = (WebView) view.findViewById(R.id.fwv_webview);

    }


    /*获取webview控件*/
    public WebView getWebView() {
        return webView;
    }

    /*设置加载进度*/
    public void setProgressBar(int position) {
        if(position < 100){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.GONE);
        }
        progressBar.setProgress(position);
    }

    /*设置加载失败信息
    * 显示自定义错误提示页面，用一个View覆盖在WebView
    * */

    public void showErroMsg(){
        LinearLayout webParentView = (LinearLayout)webView.getParent();
        initErrorPage();
        while (webParentView.getChildCount() > 1){
            webParentView.removeViewAt(0);
        }
        @SuppressWarnings("deprecation")
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        webParentView.addView(mErrorView, 0, lp);
    }

    public void hideErrorPage(){
        LinearLayout webParentView = (LinearLayout)webView.getParent();
        while (webParentView.getChildCount() > 1) {
            webParentView.removeViewAt(0);
        }
    }

    public void initErrorPage(){
        if (mErrorView == null){

            LayoutInflater from = LayoutInflater.from(context);
            mErrorView = from.inflate(R.layout.weberropage , null);
            mErrorView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    webView.reload();
                }
            });
        }
    }


}
