package svs.meeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.easydarwin.easypusher.RecordService;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.app.LivePlayerDemoActivity;
import svs.meeting.app.R;
import svs.meeting.app.VoteBallotEntity;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.MsgType;
import svs.meeting.util.Helper;
import svs.meeting.util.RxBus;

public class DisplayActivity extends BaseActivity {
    private CompositeDisposable mCompositeDisposable;
    private ImageView mImgBg;
    private TextView mTextTitle;
    private TextView mTextTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_display);
        init();
    }

    private void init(){
        initViews();
        initDatas();
        initRxbus();
    }

    private void initRxbus() {
        mCompositeDisposable = new CompositeDisposable();
        //监听订阅事件
        Disposable d = RxBus.getInstance().toObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (o instanceof EventEntity) {
                            EventEntity e = (EventEntity) o;
                            String type = e.type;
                            String v = e.value;
                            if(type.equals(EventEntity.MQTT_MSG)){
                                EventEntity.MQEntity entity=e.getMqEntity();
                                String msg=entity.getMsgType();
                                if(MsgType.MSG_SHARE.equals(msg)){
                                    String str=entity.getContent();
                                    if(str.contains(",")){
                                        String strs[]=str.split(",");
                                        if(strs[0].equals("START")){
                                            String name=strs[1];
                                            String url="rtmp://"+Config.LOCAL_HOST+"/live/"+name;
                                            Log.e("SCREEN_PUSH_url","URL=="+url);
                                            Bundle bundle=new Bundle();
                                            bundle.putString("playUrl",url);
                                            Helper.switchActivity(DisplayActivity.this, LivePlayerDemoActivity.class,bundle);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
        //subscription交给compositeSubscription进行管理，防止内存溢出
        mCompositeDisposable.add(d);
    }

    private void initDatas() {
        try {
            String name=Config.meetingInfo.getString("name");
            int fontSize = Config.meetingInfo.getInt("size");
            String time=Config.meetingInfo.getString("meeting_time");
            mTextTitle.setText(name);
            mTextTitle.setTextSize(fontSize);
            mTextTime.setText("会议召开进行时："+time);
            final String url = Config.WEB_URL + "/" + Config.meetingInfo.getString("logo");
            Glide.with(DisplayActivity.this)
                    .load(url)
                    .into(mImgBg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initViews() {
        mImgBg=findViewById(R.id.img_bg);
        mTextTitle=findViewById(R.id.tv_title);
        mTextTime=findViewById(R.id.tv_time);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();

    }
}
