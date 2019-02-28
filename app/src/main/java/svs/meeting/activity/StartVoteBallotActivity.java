package svs.meeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

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
import svs.meeting.adapter.WSignInAdapter;
import svs.meeting.app.MainActivity;
import svs.meeting.app.R;
import svs.meeting.app.VoteBallotEntity;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.MsgType;
import svs.meeting.data.User;
import svs.meeting.data.VoteOptions;
import svs.meeting.service.MqttManagerV3;
import svs.meeting.service.TimeIntentService;
import svs.meeting.util.Helper;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.util.RxBus;
import svs.meeting.util.ServiceUtils;
import svs.meeting.widgets.TipsDialogFragment;

public class StartVoteBallotActivity extends BaseActivity implements TimeIntentService.onUpdateListener {
    private CompositeDisposable mCompositeDisposable;
    private Toolbar mToolbar;
    private ImageView mImg_bg;
    private TextView mTextTitle;
    private String title;
    private String id;
    private TextView mTextTime;
    private int time;
    private VoteBallotEntity entity;
    private LinearLayout layout_content;
    private List<VoteOptions> options=new ArrayList<>();
    private LayoutInflater inflater;
    private static int srcs[]={R.drawable.circle_agree_bg,
            R.drawable.circle_disagree_bg,
            R.drawable.circle_waiver_bg};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_start_vote);
        inflater=LayoutInflater.from(this);
        init();
    }

    private void init() {
        title=getIntent().getStringExtra("title");
        time=getIntent().getIntExtra("time",0);
        entity=(VoteBallotEntity)getIntent().getSerializableExtra("voteballot");
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
                            if(type.equals(EventEntity.MQTT_MSG)){
                                EventEntity.MQEntity entity1=e.getMqEntity();
                                String msg=entity1.getMsgType();
                                if(MsgType.MSG_VOTE.equals(msg)){
                                    String content=entity1.getContent();
                                    JSONObject jsonObject=new JSONObject(content);
                                    String action=jsonObject.getString("action");
                                    if("close".equals(action)){
                                        Bundle bundle=new Bundle();
                                        bundle.putString("title",entity.getVote_name());
                                        bundle.putString("id",entity.getId());
                                        bundle.putSerializable("voteballot",entity);
                                        bundle.putInt("type",1);
                                        Helper.switchActivity(StartVoteBallotActivity.this, CheckResultActivity.class,bundle);
                                        finish();
                                    }
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
        mToolbar.setTitle("开始投票");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initViews() {
        mImg_bg=findViewById(R.id.img_bg);
        try {
            final String url = Config.WEB_URL + "/" + Config.meetingInfo.getString("logo");
            Glide.with(StartVoteBallotActivity.this)
                    .load(url)
                    .into(mImg_bg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTextTitle=findViewById(R.id.tv_title);
        mTextTitle.setText("投票名称: "+title);
        mTextTime=findViewById(R.id.tv_time);
        layout_content=findViewById(R.id.layout_content);


    }

    private void getVoteById(final String result){
            String id = entity.getId();
            String sql="select * from votes where id="+id;
            Map<String, String> map = Config.getParameters();
            map.put("type", "hql");
            map.put("ql", sql);
            RequestManager.getInstance()
                    .mServiceStore
                    .do_query(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                        @Override
                        public void onSuccess(String msg) {
                            Log.e("getVoteById onSuccess", msg);
                            if(!TextUtils.isEmpty(msg)){
                                try {
                                    JSONObject json=new JSONObject(msg);
                                    if(json.getBoolean("success")){
                                        JSONArray array=new JSONArray(json.getString("rows"));
                                        if(array.length()>0){
                                            JSONObject object=array.getJSONObject(0);
                                            VoteBallotEntity entity=new VoteBallotEntity();
                                            entity.setId(object.getString("id"));
                                            entity.setStatus(object.getString("status"));
                                            entity.setVote_mode(object.getString("vote_mode"));
                                            entity.setVote_name(object.getString("vote_name"));
                                            entity.setDuration(object.getString("duration"));
                                            entity.setContent(object.getString("content"));
                                            entity.setAtts(object.getString("atts"));
                                            entity.setTotal_count(object.getString("total_count"));
                                            entity.setSigned_count(object.getString("signed_count"));
                                            entity.setMeeting_id(object.getString("meeting_id"));
                                            entity.setSign_rate_fact(object.getString("sign_rate_fact"));
                                            doStartVoteBallot(result,entity);
                                        }


                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onError(String msg) {
                            Log.e("getVoteById onError", msg);
                        }
                    }));

    }


    private void doStartVoteBallot(String result,final VoteBallotEntity entity){
        //开始投票
        try {
        if("03".equals(entity.getStatus())){
            showFinishView("投票无效，投票已结束!");
            return;
        }
        String vote_id=entity.getId();
        String meeting_id=entity.getMeeting_id();
        String seat_no = Config.clientInfo.getString("tid");
        String sql="insert into vote_result(vote_id,meeting_id,seat_no,result,vote_time) values("+vote_id+","+meeting_id+",'"+seat_no+"','"+result+"','"+getNowTime()+"')";
        Map<String, String> map = Config.getParameters();
        map.put("type", "hql");
        map.put("ql", sql);
        RequestManager.getInstance()
                .mServiceStore
                .startVoteBallot(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                    @Override
                    public void onSuccess(String msg) {
                        Log.e("startVoteBallot", msg);
                        if(!TextUtils.isEmpty(msg)){
                            try {
                                JSONObject object=new JSONObject(msg);
                                if(object.getBoolean("success")){
                                    Toast.makeText(StartVoteBallotActivity.this, "投票成功！", Toast.LENGTH_SHORT).show();
                                    sendInfo(entity);

                                }else {
                                    Toast.makeText(StartVoteBallotActivity.this, "投票失败！", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("delNotesById onError", msg);
                    }
                }));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendInfo(final VoteBallotEntity entity){
        try {
            String split = "\\~^";
            MqttManagerV3 mqtt = MqttManagerV3.getInstance();
            JSONObject object = new JSONObject();
            Gson gson = new Gson();
            object.put("action","close");
            object.put("data",gson.toJson(entity));
            String message=object.toString();
            String seat_no = Config.clientInfo.getString("tid");
            String uname = Config.clientInfo.getString("name");
            String strMsg=uname+split+seat_no+split+MsgType.MSG_VOTE+split +message+split+new Date().getTime()+split+Config.CLIENT_IP;
            mqtt.send(strMsg,"");
            Bundle bundle=new Bundle();
            bundle.putString("title",entity.getVote_name());
            bundle.putString("id",entity.getId());
            bundle.putSerializable("voteballot",entity);
            Helper.switchActivity(StartVoteBallotActivity.this, CheckResultActivity.class,bundle);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getNowTime(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss") ;
        return dateFormat.format(date);
    }

    private void showFinishView(String msg){
       final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getSupportFragmentManager(),"showFinishView");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void onClickOk() {
                dialogFragment.dismissAllowingStateLoss();
            }
        });


    }


    private void showVoteBallotView(final String result){
        String msg="你选择了["+result+"]"+"\n"+"确定进行投票吗？";
        final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getSupportFragmentManager(),"showVoteBallotView");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void onClickOk() {
                dialogFragment.dismissAllowingStateLoss();
                getVoteById(result);
            }
        });
    }

    private void initDatas() {
        initOptions();
        if(!ServiceUtils.isServiceWork(this,"svs.meeting.service.TimeIntentService")){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent=new Intent(StartVoteBallotActivity.this,TimeIntentService.class);
                    intent.putExtra("time",time);//单位分钟
                    startService(intent);
                }
            }).start();

        }

        TimeIntentService.setOnUpdateListener(this);
    }

    private void initOptions() {
        String atts=entity.getAtts();
        try {
            JSONObject object=new JSONObject(atts);
            JSONArray array=new JSONArray(object.getString("options"));
            for (int i = 0; i <array.length() ; i++) {
                JSONObject json=array.getJSONObject(i);
                VoteOptions voteOptions=new VoteOptions();
                voteOptions.setIndex(json.getString("index"));
                voteOptions.setMx_internal_uid(json.getString("mx_internal_uid"));
                voteOptions.setOption_name(json.getString("option_name"));
                options.add(voteOptions);
                layout_content.addView(getVoteItem(i,voteOptions));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private View getVoteItem(int id,VoteOptions voteOptions){
        View view=inflater.inflate(R.layout.start_vote_item,null);
        TextView name=view.findViewById(R.id.tv_name);
        name.setText(voteOptions.getOption_name());
        view.setId(id);
        view.setOnClickListener(new MyClick(id));
        return view;
    }

    private class MyClick implements View.OnClickListener{
        private int id;
        public MyClick(int id){
            this.id=id;
        }
        @Override
        public void onClick(View v) {
            showVoteBallotView(options.get(id).getOption_name());
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
    public void onUpdate(final int t) {
        Log.e("onUpdate","onUpdate="+t);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(t==0){
                    mTextTime.setText("投票结束！");
                    mTextTime.setTextColor(getResources().getColor(R.color.colorAccent));
                    try {
                    String split = "\\~^";
                    MqttManagerV3 mqtt = MqttManagerV3.getInstance();
                    JSONObject object = new JSONObject();
                    Gson gson = new Gson();
                    object.put("action","close");
                    object.put("data",gson.toJson(entity));
                    String message=object.toString();
                    String seat_no = Config.clientInfo.getString("tid");
                    String uname = Config.clientInfo.getString("name");
                    String strMsg=uname+split+seat_no+split+MsgType.MSG_VOTE+split +message+split+new Date().getTime()+split+Config.CLIENT_IP;
                    mqtt.send(strMsg,"");
                        Bundle bundle=new Bundle();
                        bundle.putString("title",entity.getVote_name());
                        bundle.putString("id",entity.getId());
                        bundle.putSerializable("voteballot",entity);
                        bundle.putInt("type",1);
                        Helper.switchActivity(StartVoteBallotActivity.this, CheckResultActivity.class,bundle);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return;
                }else{
                    mTextTime.setText(t+" 秒");
                }

            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }
}
