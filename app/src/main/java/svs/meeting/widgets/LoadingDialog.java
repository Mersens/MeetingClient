package svs.meeting.widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import pl.droidsonroids.gif.GifImageView;
import svs.meeting.app.R;

/**
 * Created by 刘灿成 on 2018/7/26 0026.
 * 自定义加载对话框
 */

public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context) {
        super(context);
    }

    public LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder{

        private Context context;
        private String message;
        private boolean isShowMessage=true;
        private boolean isCancelable=true;
        private boolean isCancelOutside=false;


        public Builder(Context context) {
            this.context = context;
        }

        //设置提示信息
        public Builder setMessage(String message){
            this.message=message;
            return this;
        }

        //设置是否显示提示信息
        public Builder setShowMessage(boolean isShowMessage){
            this.isShowMessage=isShowMessage;
            return this;
        }

        //设置是否可以按返回键取消
        public Builder setCancelable(boolean isCancelable){
            this.isCancelable=isCancelable;
            return this;
        }

        //设置是否可以取消
        public Builder setCancelOutside(boolean isCancelOutside){
            this.isCancelOutside=isCancelOutside;
            return this;
        }

        public LoadingDialog create(){

            LoadingDialog loadingDailog = new LoadingDialog(context , R.style.loadingDialog);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view=inflater.inflate(R.layout.dialog_loading,null);
            LinearLayout loadLayout = (LinearLayout) view.findViewById(R.id.dialog_loading_view);
            GifImageView gib = new GifImageView(context );
            gib.setImageResource( R.drawable.app_loading );
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity= Gravity.CENTER;
            gib.setLayoutParams(params);
            loadLayout.addView(gib);
            loadingDailog.setContentView(view);
            loadingDailog.setCancelable(isCancelable);
            loadingDailog.setCanceledOnTouchOutside(isCancelOutside);
            return  loadingDailog;
        }
    }

}
