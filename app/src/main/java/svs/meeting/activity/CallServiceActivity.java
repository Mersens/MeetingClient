package svs.meeting.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.adapter.CallServiceAdapter;
import svs.meeting.app.R;
import svs.meeting.app.VoteBallotEntity;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.MsgEntity;
import svs.meeting.data.MsgType;
import svs.meeting.db.DBDao;
import svs.meeting.db.DBDaoImpl;
import svs.meeting.service.MqttManagerV3;
import svs.meeting.util.DisplayHelper;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.util.RxBus;
import svs.meeting.widgets.TipsDialogFragment;


public class CallServiceActivity extends BaseActivity implements View.OnClickListener {
    private CompositeDisposable mCompositeDisposable;
    private Toolbar mToolbar;
    private ListView mListView;
    private LinearLayout mLayoutContent;
    CallServiceAdapter adapter;
    private String topic="svs/all";
    private String from_seat;
    private String meeting_id;
    private String uname;
    private DBDao dao;
    List<MsgEntity> msgList=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_callservice);
        dao=new DBDaoImpl(this);
        init();
    }

    private void init() {
        try {
            from_seat=Config.clientInfo.getString("tid");
            meeting_id=Config.meetingInfo.getString("id");
            uname=Config.clientInfo.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initActionBar();
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
                            if (type.equals(EventEntity.MQTT_MSG)) {
                                EventEntity.MQEntity mqEntity= e.getMqEntity();
                                if(MsgType.MSG_SERVICE.equals(mqEntity.getMsgType())){
                                    String name=Config.clientInfo.getString("name");
                                    String content=mqEntity.getContent();
                                    String strs[]=content.split(";");
                                    String uname=strs[1];
                                    if(name.equals(uname)){
                                        return;
                                    }
                                    String seat_no=strs[2];
                                    String time=strs[3];
                                    String ip=strs[4];
                                    String c=strs[0];
                                    JSONObject object=new JSONObject(c);
                                    String str=object.getString("strContent");
                                    MsgEntity entity=new MsgEntity();
                                    entity.setPid(seat_no);
                                    entity.setMsg_time(getNowTime());
                                    entity.setMsg_type(MsgType.MSG_SERVICE);
                                    entity.setMsg(str);
                                    entity.setTopic(mqEntity.getTopic());
                                    entity.setFrom_name(uname);
                                    entity.setFrom_seat(seat_no);
                                    entity.setMeeting_id(meeting_id);
                                    entity.setSid(time);
                                    entity.setOid("");
                                    entity.setType(2);
                                    dao.addMsgInfo(entity);
                                    adapter.addMsg(entity);

                                }
                            }
                        }
                    }
                });
        //subscription交给compositeSubscription进行管理，防止内存溢出
        mCompositeDisposable.add(d);
    }
    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("呼叫服务");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initViews() {
        mListView = findViewById(R.id.listview);
        mLayoutContent = findViewById(R.id.layout_content);
        for (int i = 0; i < mLayoutContent.getChildCount(); i++) {
            FrameLayout frameLayout = (FrameLayout) mLayoutContent.getChildAt(i);
            frameLayout.setOnClickListener(this);
        }
    }

    private void initDatas() {
        adapter = new CallServiceAdapter(msgList, this);
        mListView.setAdapter(adapter);
        getListInfo();
    }

    private void getListInfo() {
        msgList.clear();
        List<MsgEntity> list=dao.findMsgById(topic,from_seat,MsgType.MSG_SERVICE,meeting_id);
        if(list!=null){
            if(list.size()>0){
                msgList.addAll(list);
                adapter.setList(msgList);
            }
        }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_bks:
                String msg1 = "我 " + getNowtime() + " 需要白开水。";
                String m = getServiceContent(1);
                showDialogView(m, msg1);
                break;
            case R.id.layout_mkf:
                String msg2 = "我 " + getNowtime() + " 需要麦克风。";
                String m1 = getServiceContent(2);
                showDialogView(m1, msg2);
                break;
            case R.id.layout_b:
                String msg3 = "我 " + getNowtime() + " 需要笔。";
                String m2 = getServiceContent(3);
                showDialogView(m2, msg3);
                break;
            case R.id.layout_cs:
                String msg4 = "我 " + getNowtime() + " 需要茶水。";
                String m3 = getServiceContent(4);
                showDialogView(m3, msg4);
                break;
            case R.id.layout_bz:
                String msg5 = "我 " + getNowtime() + " 需要白纸。";
                String m4 = getServiceContent(5);
                showDialogView(m4, msg5);
                break;
            case R.id.layout_gzry:
                String msg6 = "我 " + getNowtime() + " 需要工作人员帮助。";
                String m5 = getServiceContent(6);
                showDialogView(m5, msg6);
                break;
        }
    }

    private void showDialogView(final String type, final String msg) {
        final TipsDialogFragment tipsDialogFragment = TipsDialogFragment.getInstance(msg);
        tipsDialogFragment.show(getSupportFragmentManager(), "tips");
        tipsDialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                tipsDialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void onClickOk() {
                sendMsg(type);
                tipsDialogFragment.dismissAllowingStateLoss();
            }
        });

    }

    private String getServiceContent(int type) {
        String result = null;
        switch (type) {
            case 1:
                result = "请给我一杯白开水";
                break;
            case 4:
                result = "请给我一杯茶水";
                break;
            case 2:
                result = "请给我一个麦克风";
                break;
            case 5:
                result = "请给我一些白纸";
                break;
            case 3:
                result = "请给我一支笔";
                break;
            case 6:
                result = "请帮我呼叫工作人员";
                break;
            default:

                break;

        }
        return result;
    }

    private void sendMsg(String msg) {
        String split = "\\~^";
        MqttManagerV3 mqtt = MqttManagerV3.getInstance();
        JSONObject object = new JSONObject();
        try {
            String seat_no = Config.clientInfo.getString("tid");
            String uname = Config.clientInfo.getString("name");
            object.put("strContent", msg);
            object.put("type", "call");
            String strMsg = uname + split + seat_no + split + MsgType.MSG_SERVICE + split + object.toString() + split + new Date().getTime() + split + Config.CLIENT_IP;
            Log.e("strMsg", "strMsg===" + strMsg);
            mqtt.send(strMsg, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveDataToLoc(msg);
    }
    private void saveDataToLoc(String msg){
        MsgEntity entity=new MsgEntity();
        entity.setPid(from_seat);
        entity.setMsg_time(getNowTime());
        entity.setMsg_type(MsgType.MSG_SERVICE);
        entity.setMsg(msg);
        entity.setTopic(topic);
        entity.setFrom_name(uname);
        entity.setFrom_seat(from_seat);
        entity.setMeeting_id(meeting_id);
        entity.setSid(getSid());
        entity.setOid("");
        entity.setType(1);
        dao.addMsgInfo(entity);
        adapter.addMsg(entity);
    }
    private String getSid(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date());
    }
    private String getNowTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }
    private String getNowtime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String time = sdf.format(new Date());
        return time;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }
}
