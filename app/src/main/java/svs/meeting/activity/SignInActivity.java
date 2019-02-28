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
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.adapter.SignInAdapter;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.User;
import svs.meeting.service.TimerService;
import svs.meeting.util.Helper;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.util.ServiceUtils;
import svs.meeting.util.Timer;
import svs.meeting.widgets.TipsDialogFragment;

public class SignInActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ImageView mImg;
    private TextView mTextName;
    private TextView mTextTime;
    private RelativeLayout mLayoutSignin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_singin);
        init();
    }

    private void init() {
        initActionBar();
        initViews();
        initDatas();
    }

    private void initDatas() {
        try {
            final String url = Config.WEB_URL + "/" + Config.meetingInfo.getString("logo");
            Glide.with(SignInActivity.this)
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mImg);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void initViews() {
        mImg = findViewById(R.id.img_bg);
        mTextName = findViewById(R.id.tv_name);
        mTextTime = findViewById(R.id.tv_time);
        mLayoutSignin = findViewById(R.id.layout_sign);
        try {
            String name = Config.display_atts.getString("cardName");
            String time = Config.meetingInfo.getString("meeting_time");
            mTextName.setText(name);
            mTextTime.setText(time);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mLayoutSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSign();

            }
        });
    }

    private void doSign() {
        try {
            Map<String, String> map = Config.getParameters();
            String ip_addr = Config.clientInfo.getString("ip_addr");
            String seat_no = Config.clientInfo.getString("tid");
            String uname = Config.clientInfo.getString("name");
            String meeting_id = Config.meetingInfo.getString("id");

            map.put("ip_addr", ip_addr);
            map.put("seat_no", seat_no);
            map.put("uname", uname);
            map.put("meeting_id", meeting_id);
            RequestManager.getInstance()
                    .mServiceStore
                    .do_login(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                        @Override
                        public void onSuccess(String msg) {
                            Log.e("doSign onSuccess", msg);
                            if (!TextUtils.isEmpty(msg)) {
                                try {
                                    JSONObject json = new JSONObject(msg);
                                    if (json.getBoolean("success")) {
                                        showTipsViews("签到成功！");
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


    private void showTipsViews(String msg) {
        final TipsDialogFragment dialogFragment = TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getSupportFragmentManager(), "Tips");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
                Helper.switchActivity(SignInActivity.this, SignInShowActivity.class);

                finish();
            }

            @Override
            public void onClickOk() {
                dialogFragment.dismissAllowingStateLoss();
                Helper.switchActivity(SignInActivity.this, SignInShowActivity.class);
                finish();
            }
        });

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


}
