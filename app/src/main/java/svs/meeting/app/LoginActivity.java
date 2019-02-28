package svs.meeting.app;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import svs.meeting.app.R;

import svs.meeting.data.Config;
import svs.meeting.util.Helper;
import svs.meeting.util.HttpUtil;
import svs.meeting.util.IHttpCallback;
import svs.meeting.util.XLog;
import svs.meeting.widgets.DialogWaiting;

public class LoginActivity extends AppCompatActivity {
    private DialogWaiting dlgWaiting;
    private Button btnSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dlgWaiting = new DialogWaiting(this);
        btnSign=(Button)this.findViewById(R.id.btnSign);
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSign();
            }
        });
    }

    private void doSign() {




    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("tag", "requestCode****" + requestCode);
        /*
        srHelper.onActivityResult(this, requestCode, resultCode, data, new ScreenRecorderHelper.OnRecordStatusChangeListener() {
            @Override
            public void onChangeSuccess() {
                //开始录制，处理开始录制后的事件
                XLog.log(("start......."));
            }

            @Override
            public void onChangeFailed() {
                //如果录制失败，则不作任何变化
                XLog.log(("failed......."));
            }
        });*/
    }
}
