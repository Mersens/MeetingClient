package svs.meeting.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.easydarwin.easypusher.RecordService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.app.LivePlayerDemoActivity;
import svs.meeting.app.MainActivity;
import svs.meeting.app.R;
import svs.meeting.app.VoteBallotEntity;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.MsgType;
import svs.meeting.data.SignInfoEntity;
import svs.meeting.data.User;
import svs.meeting.service.SignTimerService;
import svs.meeting.util.Helper;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.util.RxBus;

public class CheckResultActivity extends BaseActivity implements SignTimerService.onUpdateListener{
    private CompositeDisposable mCompositeDisposable;
    private Toolbar mToolbar;
    private PieChart mChart;
    private String mTitle;
    int signCount =0 ;
    int allCount=0;
    String id;
    private TextView mTextYD;
    private TextView mTextYQD;
    private TextView mTextWQD;
    private TextView mTextQDL;
    private TextView mTextTitle;
    private ImageView mImgYD;
    private ImageView mImgYQD;
    private ImageView mImgWQD;
    private ImageView mImgQDL;
    private List<User> mList=new ArrayList<>();
    private List<String> signList=new ArrayList<>();
    private List<SignInfoEntity> signInfoList=new ArrayList<>();
    private float scale;

    private ImageView mImgTY;
    private ImageView mImgFD;
    private ImageView mImgQQ;

    private TextView mTextTY;
    private TextView mTextFD;
    private TextView mTextQQ;

    private TextView mTextSetTGL;
    private TextView mTextDQTGL;
    private TextView mTextResult;

    int countSum =0;
    int waiverCount=0;
    int passCount=0;
    int opposition=0;
    private VoteBallotEntity entity;
    private int sign_rate;
    private float resule_sign_rate;
    private int type=0;
    private TextView mTextTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_check_result);
        init();
    }

    private void init() {
        if(getIntent().hasExtra("type")){
            type=getIntent().getIntExtra("type",0);
        }
        mTitle=getIntent().getStringExtra("title");
        id=getIntent().getStringExtra("id");
        entity=(VoteBallotEntity)getIntent().getSerializableExtra("voteballot");
        try {
            JSONObject object=new JSONObject(entity.getAtts());
            sign_rate=object.getInt("sign_rate");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initActionBar();
        initViews();
        initDatas();
        initRxbus();
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("投票结果");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
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
                                  if(MsgType.MSG_VOTE.equals(msg)){
                                    String content=entity.getContent();
                                    JSONObject jsonObject=new JSONObject(content);
                                    String action=jsonObject.getString("action");
                                    if("start".equals(action)){
                                        JSONObject object=new JSONObject(jsonObject.getString("data"));
                                        VoteBallotEntity entity1=new VoteBallotEntity();
                                        entity1.setId(object.getString("id"));
                                        entity1.setStatus(object.getString("status"));
                                        entity1.setVote_mode(object.getString("vote_mode"));
                                        entity1.setVote_name(object.getString("vote_name"));
                                        entity1.setDuration(object.getString("duration"));
                                        entity1.setContent(object.getString("content"));
                                        entity1.setAtts(object.getString("atts"));
                                        entity1.setTotal_count(object.getString("total_count"));
                                        entity1.setSigned_count(object.getString("signed_count"));
                                        entity1.setMeeting_id(object.getString("meeting_id"));
                                        entity1.setSign_rate_fact(object.getString("sign_rate_fact"));
                                        Bundle bundle=new Bundle();
                                        bundle.putString("title",entity1.getVote_name());
                                        int time=Integer.parseInt(entity1.getDuration());
                                        bundle.putInt("time",time);
                                        bundle.putSerializable("voteballot",entity1);
                                        Helper.switchActivity(CheckResultActivity.this, StartVoteBallotActivity.class,bundle);
                                    }else if("vote_action".equals(action)){
                                        initDatas();
                                    }
                                }
                            }
                        }
                    }
                });
        //subscription交给compositeSubscription进行管理，防止内存溢出
        mCompositeDisposable.add(d);
    }

    private void startSignService(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int time=Config.signSetting.getInt("sign_time");
                    Intent intent=new Intent(CheckResultActivity.this,SignTimerService.class);
                    intent.putExtra("time",30);
                    startService(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        SignTimerService.setOnUpdateListener(this);
    }
    private void initViews() {
        mTextTime=findViewById(R.id.tv_time);
        if(type==1){
            mTextTime.setVisibility(View.VISIBLE);
            startSignService();
        }
        mChart = (PieChart) findViewById(R.id.pieChart);

        mChart.setUsePercentValues(false);
        mChart.setCenterText("投票结果");
        mChart.setHoleColor(Color.WHITE);
        mChart.getDescription().setEnabled(false);
        mChart.setHoleRadius(50);
        //设置中间透明圈的半径,值为所占饼图的百分比
        mChart.setTransparentCircleRadius(24);
        Legend l = mChart.getLegend();
        l.setEnabled(false);

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

        mTextTY=findViewById(R.id.tv_ty);
        mImgTY=findViewById(R.id.img_ty);
        mImgTY.setBackgroundColor(ColorTemplate.VORDIPLOM_COLORS[0]);

        mTextFD=findViewById(R.id.tv_fd);
        mImgFD=findViewById(R.id.img_fd);
        mImgFD.setBackgroundColor(ColorTemplate.VORDIPLOM_COLORS[1]);

        mTextQQ=findViewById(R.id.tv_qq);
        mImgQQ=findViewById(R.id.img_qq);
        mImgQQ.setBackgroundColor(ColorTemplate.VORDIPLOM_COLORS[2]);

        mTextTitle=findViewById(R.id.title);
        mTextTitle.setText(mTitle);

        mTextSetTGL=findViewById(R.id.tv_tgl);
        mTextDQTGL=findViewById(R.id.tv_dqtgl);
        mTextResult=findViewById(R.id.tv_bjjg);

        mTextSetTGL.setText(sign_rate+"%");
    }

    private void initDatas() {
        try {
            allCount=Config.signSetting.getInt("total");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getSignInfo();

    }

    private void getTPResultInfo() {
        String sql="select count(*) as total,result from vote_result where vote_id="+id+" group by result";
        Map<String, String> map = Config.getParameters();
        map.put("type", "sql");
        map.put("ql", sql);
        RequestManager.getInstance()
                .mServiceStore
                .do_query(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                    @Override
                    public void onSuccess(String msg) {
                        Log.e("getTPResultInfo", msg);
                        if(!TextUtils.isEmpty(msg)){
                            try {
                                JSONObject json=new JSONObject(msg);
                                if(json.getBoolean("success")){
                                    JSONArray array=new JSONArray(json.getString("rows"));
                                    for (int i = 0; i <array.length() ; i++) {
                                        JSONObject object=array.getJSONObject(i);
                                        int total=object.getInt("total");
                                        String result=object.getString("result");
                                        countSum=countSum+total;
                                        if("同意".equals(result)){
                                            passCount=total;
                                        }else if("弃权".equals(result)){
                                            waiverCount=total;
                                        }else if("反对".equals(result)) {
                                            opposition=total;
                                        }
                                    }
                                    mTextTY.setText("同意 "+passCount);
                                    mTextFD.setText("反对 "+opposition);
                                    mTextQQ.setText("弃权 "+(waiverCount+signCount-countSum));

                                    float f1=signCount;
                                    float f2=passCount;
                                    float f=(f2/f1)*100;
                                    resule_sign_rate=formatDouble(f);
                                    mTextDQTGL.setText(resule_sign_rate+"%");
                                    if(resule_sign_rate<sign_rate){
                                        mTextResult.setText("未通过");
                                        mTextResult.setTextColor(getResources().getColor(R.color.colorAccent));

                                    }else {
                                        mTextResult.setText("已通过");
                                        mTextResult.setTextColor(getResources().getColor(R.color.actionbar_color));
                                    }
                                    float f3=signCount;
                                    float f4=opposition;
                                    float f5=(f4/f3)*100;

                                    float f6=signCount;
                                    float f7=(waiverCount+signCount-countSum);
                                    float f8=(f7/f6)*100;

                                    setPieChartData(resule_sign_rate,formatDouble(f5),formatDouble(f8));
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

                                        }


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
                                        setData(allCount,signCount);
                                        getTPResultInfo();
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
    private void setData(int allCount,final int y_sign) {
        mTextYD.setText("应到人数:"+allCount+"人");
        mTextYQD.setText("已签到人数:"+y_sign+"人");
        mTextWQD.setText("未签到人数:"+(allCount-y_sign)+"人");
        float f1=allCount;
        float f2=y_sign;
        float f=(f2/f1)*100;
        scale=formatDouble(f);
        mTextQDL.setText("签到比例:"+scale+"%");
        mTextTitle.setText("投票名称:"+mTitle);

    }
    public static float formatDouble(float d) {
        DecimalFormat df = new DecimalFormat("#.00");
        return Float.parseFloat(df.format(d));
    }
    private void setPieChartData(float ty,float fd,float qq) {
        ArrayList<String> nameList = new ArrayList<String>();
        for(int i=0;i<3;i++){
            nameList.add("");

        }
        ArrayList<PieEntry> valueList = new ArrayList<PieEntry>();
        valueList.add(new PieEntry(ty, 0));
        valueList.add(new PieEntry(fd, 1));
        valueList.add(new PieEntry(qq, 2));
        PieDataSet dataSet = new PieDataSet(valueList, "不同颜色代表的含义");

        //设置个饼状图之间的距离
        dataSet.setSliceSpace(0f);
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        //设置是否显示区域文字内容
        mChart.setDrawSliceText(true);
        PieData data = new PieData( dataSet);
        //设置以百分比显示
        data.setValueFormatter(new PercentFormatter());
        //区域文字的大小
        data.setValueTextSize(11f);
        //设置区域文字的颜色
        data.setValueTextColor(Color.WHITE);
        //设置区域文字的字体
        data.setValueTypeface(Typeface.DEFAULT);
        mChart.setData(data);
        //设置是否显示区域百分比的值
        for (IDataSet<?> set : mChart.getData().getDataSets()){
            set.setDrawValues(!set.isDrawValuesEnabled());
        }
        // undo all highlights
        mChart.highlightValues(null);
        mChart.invalidate();

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
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    @Override
    public void onUpdate(final int time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(time==0){
                    Helper.switchActivity(CheckResultActivity.this, MainActivity.class);
                    finish();
                    return;
                }else {
                    mTextTime.setText(time+" 秒后自动关闭");
                }
            }
        });
    }
}
