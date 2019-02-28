package svs.meeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.adapter.SignInAdapter;
import svs.meeting.app.R;
import svs.meeting.app.SplashActivity;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.User;
import svs.meeting.service.TimerService;
import svs.meeting.util.Helper;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.util.RxBus;
import svs.meeting.util.ServiceUtils;
import svs.meeting.util.Timer;
import svs.meeting.widgets.TipsDialogFragment;

public class SignInShowActivity extends BaseActivity implements TimerService.onUpdateListener {
    private CompositeDisposable mCompositeDisposable;
    private Toolbar mToolbar;
    private TextView mTextTime;
    private TextView mTextSignCount;
    private TextView mTextAllCount;
    private ListView mListview;
    private TextView mTextSign;
    int signCount =0 ;
    int allCount=0;
    private ScrollView mScrollView;
    private boolean isdestory=false;
    private List<User> mList=new ArrayList<>();
    private List<String> signList=new ArrayList<>();
    private SignInAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_show);
        init();
    }
    private void init() {
        initActionBar();
        initViews();
        initDatas();
    }

    private void initDatas() {
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
                                        mTextSignCount.setText(signCount+"");
                                        for (int i = 0; i <signCount ; i++) {
                                            JSONObject object=array.getJSONObject(i);
                                             signList.add(object.getString("seat_no"));
                                             Log.e("seat_no",object.getString("seat_no"));
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
                                            Log.e("tid","tid"+object.getString("tid"));
                                            mList.add(user);
                                        }

                                        if(adapter==null){
                                            adapter=new SignInAdapter(mList,SignInShowActivity.this);
                                            mListview.setAdapter(adapter);
                                        }
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
    private void initViews() {
        mTextTime=findViewById(R.id.tv_time);
        mTextSignCount=findViewById(R.id.tv_qd_count);
        mTextSignCount.setTextColor(getResources().getColor(R.color.actionbar_color));
        mTextAllCount=findViewById(R.id.tv_all_count);
        mTextAllCount.setTextColor(getResources().getColor(R.color.actionbar_color));
        mListview=findViewById(R.id.listview);
        mTextSign=findViewById(R.id.tv_qd);
        try {
            allCount=Config.signSetting.getInt("total");
            mTextAllCount.setText(allCount+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mScrollView=findViewById(R.id.scrollView);

        mCompositeDisposable = new CompositeDisposable();
        //监听订阅事件
        Disposable d = RxBus.getInstance().toObservable().subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (o instanceof EventEntity) {

                        }
                    }
                });
        //subscription交给compositeSubscription进行管理，防止内存溢出
        mCompositeDisposable.add(d);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int time=Config.signSetting.getInt("sign_time");
                        Intent intent=new Intent(SignInShowActivity.this,TimerService.class);
                        intent.putExtra("time",time);
                        startService(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        TimerService.setOnUpdateListener(this);
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("签到");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
/*        stopService(mIntent);
        mCodeHandler.removeCallbacksAndMessages(null);*/
    }


    @Override
    public void onUpdate(final int time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(time==0){
                    mTextTime.setText("签到结束！");
                    Config.isStartTimerService=true;
                    return;
                }else {
                    mTextTime.setText(time+" 秒");

                }
            }
        });

    }
}
