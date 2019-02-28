package svs.meeting.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.adapter.SignInAdapter;
import svs.meeting.adapter.VoteBallotAdapter;
import svs.meeting.app.LoginActivity;
import svs.meeting.app.R;
import svs.meeting.app.VoteBallotEntity;
import svs.meeting.data.Config;
import svs.meeting.data.User;
import svs.meeting.util.Helper;
import svs.meeting.util.HttpUtil;
import svs.meeting.util.IHttpCallback;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.widgets.TipsDialogFragment;

public class VoteBallotActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ListView mListView;
    List<VoteBallotEntity> list = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vote_ballot);
        init();
    }

    private void init() {
        initActionBar();
        initViews();

    }

    @Override
    protected void onResume() {
        super.onResume();
        list.clear();
        initDatas();
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("投票表决");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initViews() {
        mListView = findViewById(R.id.listview);

    }

    private void initDatas() {
        doQuery();
    }
    private void doQuery() {
        try {
            String id = Config.meetingInfo.getString("id");
            String sql="select * from votes where meeting_id="+id+" order by id asc";
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
                            Log.e("VoteBallotActivity", msg);
                            if(!TextUtils.isEmpty(msg)){
                                try {
                                    JSONObject json=new JSONObject(msg);
                                    if(json.getBoolean("success")){
                                        JSONArray array=new JSONArray(json.getString("rows"));
                                        for (int i = 0; i <array.length() ; i++) {
                                            JSONObject object=array.getJSONObject(i);
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
                                            list.add(entity);
                                        }

                                        VoteBallotAdapter adapter = new VoteBallotAdapter(list, VoteBallotActivity.this);
                                        mListView.setAdapter(adapter);
                                        adapter.setOnOperationClickListener(new VoteBallotAdapter.onOperationClickListener() {
                                            @Override
                                            public void onStart(int index,VoteBallotEntity entity) {
                                                String name=entity.getVote_name();
                                                String msg="确定对主题["+name+"]投票吗？";
                                                showTips(msg,name,entity);
                                            }

                                            @Override
                                            public void onCheck(int index,VoteBallotEntity entity) {
                                                Bundle bundle=new Bundle();
                                                bundle.putString("title",entity.getVote_name());
                                                bundle.putString("id",entity.getId());
                                                bundle.putSerializable("voteballot",entity);
                                                Helper.switchActivity(VoteBallotActivity.this, CheckResultActivity.class,bundle);
                                            }
                                        });

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

    private void showTips(String msg,final String title,final VoteBallotEntity entity) {
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
                Bundle bundle=new Bundle();
                bundle.putString("title",title);
                bundle.putSerializable("voteballot",entity);
                Helper.switchActivity(VoteBallotActivity.this, VoteBallotDetailActivity.class,bundle);
            }
        });
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
