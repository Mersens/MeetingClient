package svs.meeting.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.MsgType;
import svs.meeting.service.MqttManagerV3;
import svs.meeting.widgets.TipsDialogFragment;


public class NotificationActivity extends BaseActivity {
    private Toolbar mToolbar;
    private String name;
    private int type=0;
    private String agreeEndWithStr="_agree";
    private String refuseEndWithStr="_refuse";
    private int from=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_notification);
        init();
    }

    private void init(){
        initActionBar();
        name=getIntent().getStringExtra("name");
        type=getIntent().getIntExtra("type",0);
        String msg=null;
        String t=null;
        if(type==1){
            msg=name+"申请同屏共享，是否同意？";
            t="shareScreen";
        }else if(type==2){
            msg=name+"申请发言，是否同意？";
            t="speaker";
        }else if(type==3){
            msg=name+"申请离开，是否同意？";
            t="leave";
        }

        showViews(t,msg);
    }

    private void showViews(final String type,String msg){
        final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg,"拒绝","同意");
        dialogFragment.show(getSupportFragmentManager(),"NotifyViews");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                sendInfo(type+refuseEndWithStr);
                dialogFragment.dismissAllowingStateLoss();
                NotificationActivity.this.finish();
            }
            @Override
            public void onClickOk() {
                sendInfo(type+agreeEndWithStr);
                dialogFragment.dismissAllowingStateLoss();
                NotificationActivity.this.finish();
            }
        });

    }

    private void sendInfo(String type){
        try {
            String split = "\\~^";
            MqttManagerV3 mqtt = MqttManagerV3.getInstance();
            JSONObject object = new JSONObject();
            object.put("type",type);
            String message=object.toString();
            String seat_no = Config.clientInfo.getString("tid");
            String uname = Config.clientInfo.getString("name");
            String strMsg=uname+split+seat_no+split+MsgType.MSG_RESPONSE+split +message+split+new Date().getTime()+split+Config.CLIENT_IP;
            mqtt.send(strMsg,"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
