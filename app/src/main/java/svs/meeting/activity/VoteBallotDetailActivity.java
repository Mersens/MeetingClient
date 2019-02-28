package svs.meeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.adapter.LSignInAdapter;
import svs.meeting.adapter.SignInAdapter;
import svs.meeting.adapter.WSignInAdapter;
import svs.meeting.adapter.YSignInAdapter;
import svs.meeting.app.MainActivity;
import svs.meeting.app.R;
import svs.meeting.app.VoteBallotEntity;
import svs.meeting.data.Config;
import svs.meeting.data.MsgType;
import svs.meeting.data.SignInfoEntity;
import svs.meeting.data.User;
import svs.meeting.service.MqttManagerV3;
import svs.meeting.service.SignTimerService;
import svs.meeting.service.TimerService;
import svs.meeting.util.Helper;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.widgets.TipsDialogFragment;

public class VoteBallotDetailActivity extends BaseActivity implements SignTimerService.onUpdateListener {
    private Toolbar mToolbar;
    private BarChart mChart;
    int signCount =0 ;
    int allCount=0;
    private List<User> mList=new ArrayList<>();
    private List<String> signList=new ArrayList<>();
    private List<SignInfoEntity> signInfoList=new ArrayList<>();
    private List<SignInfoEntity> signOutList=new ArrayList<>();
    private TextView mTextYD;
    private TextView mTextYQD;
    private TextView mTextWQD;
    private TextView mTextQDL;
    private ImageView mImgYD;
    private ImageView mImgYQD;
    private ImageView mImgWQD;
    private ImageView mImgQDL;
    private TextView mTextTitle;
    private TextView mBtnStart;
    private String title;
    private ListView mListView1;
    private ListView mListView2;
    private ListView mListView3;
    private YSignInAdapter adapter1;
    private WSignInAdapter adapter2;
    private LSignInAdapter adapter3;
    private VoteBallotEntity entity;
    private RelativeLayout layout_tp;
    private TextView mTextTime;
    private int sign_rate;
    float scale;
    private int type;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_voteballot_detail);
        init();
    }

    private void init() {
        if(getIntent().hasExtra("type")){
            type=getIntent().getIntExtra("type",1);
        }else {
            title=getIntent().getStringExtra("title");
            entity=(VoteBallotEntity)getIntent().getSerializableExtra("voteballot");
            try {
                JSONObject object=new JSONObject(entity.getAtts());
                sign_rate=object.getInt("sign_rate");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        initActionBar();
        initViews();
        initDatas();

    }
    private void startSignService(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int time=Config.signSetting.getInt("sign_time");
                    Intent intent=new Intent(VoteBallotDetailActivity.this,SignTimerService.class);
                    intent.putExtra("time",10);
                    startService(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        SignTimerService.setOnUpdateListener(this);
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("开始投票");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initViews() {
        layout_tp=findViewById(R.id.layout_tp);
        mTextTime=findViewById(R.id.tv_time);
        if(type==1){
            layout_tp.setVisibility(View.GONE);
            mTextTime.setVisibility(View.VISIBLE);
            startSignService();
        }
        mChart = (BarChart) findViewById(R.id.chart1);

        mChart.getDescription().setEnabled(false);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawBarShadow(false);
        mChart.setDrawGridBackground(false);
        List<String> xAxisValue=new ArrayList<>();
        xAxisValue.add("应到");
        xAxisValue.add("已签到");
        xAxisValue.add("未签到");
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(xAxisValue.size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisValue));
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return ""+(int)value;
            }
        });
        leftAxis.setYOffset(3);
        leftAxis.setStartAtZero(true);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return ""+(int)value;
            }
        });
        rightAxis.setYOffset(3);
        rightAxis.setStartAtZero(true);

        mChart.getAxisLeft().setDrawGridLines(false);

        mChart.getLegend().setEnabled(false);
        mChart.setFitBars(true);


        mTextYD=findViewById(R.id.tv_yd);
        mImgYD=findViewById(R.id.img_yd);
        mImgYD.setBackgroundColor(ColorTemplate.VORDIPLOM_COLORS[0]);

        mTextYQD=findViewById(R.id.tv_yqd);
        mImgYQD=findViewById(R.id.img_yqd);
        mImgYQD.setBackgroundColor(ColorTemplate.VORDIPLOM_COLORS[1]);

        mTextWQD=findViewById(R.id.tv_wqd);
        mImgWQD=findViewById(R.id.img_wqd);
        mImgWQD.setBackgroundColor(ColorTemplate.VORDIPLOM_COLORS[2]);

        mTextQDL=findViewById(R.id.tv_qdl);
        mImgQDL=findViewById(R.id.img_qdl);
        mImgQDL.setBackgroundColor(ColorTemplate.VORDIPLOM_COLORS[3]);

        mTextTitle=findViewById(R.id.tv_name);
        mBtnStart=findViewById(R.id.tv_tp);
        mListView1=findViewById(R.id.listview1);
        mListView2=findViewById(R.id.listview2);
        mListView3=findViewById(R.id.listview3);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(scale<51){
                    String tips="签到比例必须达到"+sign_rate+"%"+"\n"+"当前比例:"+scale+"%";
                    showTips(tips);
                }else {
                    sendVoteInfo();
                    //开始投票

                }
            }
        });

    }

    private void sendVoteInfo(){
        try {
            String id = Config.meetingInfo.getString("id");
            String totalCountSql="select count(*) from meeting_devices where meeting_id="+id+" and (dev_type='01' or dev_type='02') and enabled='01'";
            String signedCountSql="select count(*) from logins where login_type<>'02' and meeting_id="+id;
            String sql="update votes set status='02',total_count=("+totalCountSql+"),signed_count=("+signedCountSql+"),sign_rate_fact="+entity.getSign_rate_fact()+" where id="+entity.getId();
            Map<String, String> map = Config.getParameters();
            map.put("type", "hql");
            map.put("batch","1");
            map.put("ql", sql);
            map.put("encoding","utf-8");
            RequestManager.getInstance()
                    .mServiceStore
                    .startVoteBallot(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                        @Override
                        public void onSuccess(String msg) {
                            Log.e("sendVoteInfo onSuccess", msg);
                            if(!TextUtils.isEmpty(msg)){
                                try {
                                    JSONObject json=new JSONObject(msg);
                                    if(json.getBoolean("success")){
                                        String split = "\\~^";
                                        MqttManagerV3 mqtt = MqttManagerV3.getInstance();
                                        JSONObject object = new JSONObject();
                                        Gson gson = new Gson();
                                        object.put("action","start");
                                        object.put("data",gson.toJson(entity));
                                        String message=object.toString();
                                        String seat_no = Config.clientInfo.getString("tid");
                                        String uname = Config.clientInfo.getString("name");
                                        String strMsg=uname+split+seat_no+split+MsgType.MSG_VOTE+split +message+split+new Date().getTime()+split+Config.CLIENT_IP;
                                        mqtt.send(strMsg,"");
                                        Bundle bundle=new Bundle();
                                        bundle.putString("title",title);
                                        int time=Integer.parseInt(entity.getSign_rate_fact());
                                        bundle.putInt("time",time);
                                        bundle.putSerializable("voteballot",entity);
                                        Helper.switchActivity(VoteBallotDetailActivity.this, StartVoteBallotActivity.class,bundle);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onError(String msg) {
                            Log.e("sendVoteInfo onError", msg);
                        }
                    }));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showTips(String msg) {
        final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getSupportFragmentManager(),"tips");
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
    private void setData(int allCount,final int y_sign) {

        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
        yVals.add(new BarEntry(0,  allCount));
        yVals.add(new BarEntry(1, y_sign));
        yVals.add(new BarEntry(2, (allCount-y_sign)));


        BarDataSet set = new BarDataSet(yVals, "Data Set");

        set.setColors(ColorTemplate.VORDIPLOM_COLORS);
        set.setDrawValues(false);

        BarData data = new BarData(set);

        mChart.setData(data);
        mChart.invalidate();
        mChart.animateY(1000);

        mTextYD.setText("应到人数:"+allCount+"人");
        mTextYQD.setText("已签到人数:"+y_sign+"人");
        mTextWQD.setText("未签到人数:"+(allCount-y_sign)+"人");
        float f1=allCount;
        float f2=y_sign;
        float f=(f2/f1)*100;
        scale=formatDouble(f);
        mTextQDL.setText("签到比例:"+scale+"%");
        mTextTitle.setText("投票名称:"+title);

    }

    public static float formatDouble(float d) {
        DecimalFormat df = new DecimalFormat("#.00");
        return Float.parseFloat(df.format(d));
    }
    private void initDatas() {
        try {
            allCount=Config.signSetting.getInt("total");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getSignInfo();

    }
    private void getSignInfo() {
        try {
            String id = Config.meetingInfo.getString("id");
            String sql="select * from logins where login_type<>'02' and meeting_id="+id;
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
                            Log.e("getSignInfo onSuccess", msg);
                            if(!TextUtils.isEmpty(msg)){
                                try {
                                    JSONObject json=new JSONObject(msg);
                                    if(json.getBoolean("success")){
                                        JSONArray array=new JSONArray(json.getString("rows"));
                                        signCount=array.length();
                                        for (int i = 0; i <signCount ; i++) {
                                            JSONObject object=array.getJSONObject(i);
                                            SignInfoEntity entity=new SignInfoEntity();
                                            String seat_no=object.getString("seat_no");
                                            String login_type=object.getString("login_type");
                                            signList.add(seat_no);
                                            entity.setSeat_no(seat_no);
                                            entity.setId(object.getString("id"));
                                            entity.setIp_addr(object.getString("ip_addr"));
                                            entity.setMeeting_id(object.getString("meeting_id"));
                                            entity.setUname(object.getString("uname"));
                                            entity.setLogin_type(login_type);
                                            signInfoList.add(entity);
                                            if("02".equals(login_type)){
                                                signOutList.add(entity);
                                            }
                                        }
                                        adapter1=new YSignInAdapter(signInfoList,VoteBallotDetailActivity.this);
                                        mListView1.setAdapter(adapter1);
                                        adapter3=new LSignInAdapter(signOutList,VoteBallotDetailActivity.this);
                                        mListView3.setAdapter(adapter3);

                                        doQuery();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onError(String msg) {
                            Log.e("doQuery onError", msg);
                        }
                    }));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doQuery() {
        try {
            String id = Config.meetingInfo.getString("id");
            String sql="select * from meeting_devices where meeting_id="+id+" and (dev_type='01' or dev_type='02') order by tid asc";

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
                            Log.e("doQuery111 onSuccess", msg);
                            if(!TextUtils.isEmpty(msg)){
                                try {
                                    JSONObject json=new JSONObject(msg);
                                    if(json.getBoolean("success")){
                                        mList.clear();
                                        JSONArray array=new JSONArray(json.getString("rows"));
                                        for (int i = 0; i <array.length() ; i++) {
                                            JSONObject object=array.getJSONObject(i);
                                            String tid=object.getString("tid");
                                            if(signList.contains(tid)){
                                                continue;
                                            }
                                            User user=new User();
                                            user.setUsername(object.getString("name"));
                                            user.setTid(tid);
                                            mList.add(user);
                                        }
                                        adapter2=new WSignInAdapter(mList,VoteBallotDetailActivity.this);
                                        mListView2.setAdapter(adapter2);
                                        setData(allCount,signCount);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onError(String msg) {
                            Log.e("doQuery onError", msg);
                        }
                    }));
        } catch (JSONException e) {
            e.printStackTrace();
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
    public void onUpdate(final int time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(time==0){
                    Helper.switchActivity(VoteBallotDetailActivity.this, MainActivity.class);
                    finish();
                    return;
                }else {
                    mTextTime.setText(time+" 秒后自动关闭");
                }
            }
        });
    }
}
