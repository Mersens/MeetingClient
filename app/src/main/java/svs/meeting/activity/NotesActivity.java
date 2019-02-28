package svs.meeting.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.adapter.NotesAdapter;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.NotesEntity;
import svs.meeting.data.User;
import svs.meeting.util.Helper;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.widgets.TipsDialogFragment;

public class NotesActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ListView mListView;
    private FloatingActionButton fab;
    private List<NotesEntity> mList=new ArrayList<>();
    private NotesAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_notes);
        init();
    }
    private void init() {
        initActionBar();
        initViews();
        initEvent();
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("会议笔记");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @SuppressLint("ResourceType")
    private void initViews() {
        mListView = findViewById(R.id.listview);
        adapter=new NotesAdapter(mList,this);
        mListView.setAdapter(adapter);
        fab = findViewById(R.id.fab);
        adapter.setOnButtonClickListener(new NotesAdapter.OnButtonClickListener() {
            @Override
            public void onUpdateClick(int pos,NotesEntity entity) {
                Bundle bundle=new Bundle();
                bundle.putSerializable("notesentity",entity);
                Helper.switchActivity(NotesActivity.this, UpdateNotesActivity.class,bundle);
            }

            @Override
            public void onDelClick(int pos,NotesEntity entity) {
                showDelTips("确定删除？",pos,entity);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        initDatas();

    }

    private void initDatas() {
        getNotesInfo();
    }


    public void getNotesInfo(){
        try {
            String id = Config.meetingInfo.getString("id");
            String seat_no = Config.clientInfo.getString("tid");
            String sql="select * from notes where meeting_id='"+id+"' and seat_no='"+seat_no+"' and note_type='01'";
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
                            Log.e("getNotesInfo onSuccess", msg);
                            if(!TextUtils.isEmpty(msg)){
                                try {
                                    JSONObject json=new JSONObject(msg);
                                    if(json.getBoolean("success")){
                                        //查询成功，绑定数据
                                        mList.clear();
                                        JSONArray array=new JSONArray(json.getString("rows"));
                                            for (int i = 0; i <array.length() ; i++) {
                                                JSONObject object=array.getJSONObject(i);
                                                NotesEntity entity=new NotesEntity();
                                                entity.setId(object.getString("id"));
                                                entity.setMeeting_id(object.getString("meeting_id"));
                                                entity.setModified(object.getString("modified"));
                                                entity.setNote_content(object.getString("note_content"));
                                                entity.setNote_title(object.getString("note_title"));
                                                entity.setSeat_no(object.getString("seat_no"));
                                                entity.setNote_type(object.getString("note_type"));
                                                mList.add(entity);

                                        }
                                        adapter.setList(mList);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onError(String msg) {
                            Log.e("getNotesInfo onError", msg);
                        }
                    }));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showDelTips(String msg,final int pos,final NotesEntity entity){
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
                delNotesById(pos,entity);
            }
        });
    }

    private void  delNotesById(final int pos,final NotesEntity entity){
        String id = entity.getId();
        String sql="delete from notes where id="+id;
        Map<String, String> map = Config.getParameters();
        map.put("type", "hql");
        map.put("ql", sql);
        RequestManager.getInstance()
                .mServiceStore
                .delNotesById(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                    @Override
                    public void onSuccess(String msg) {
                        Log.e("delNotesById onSuccess", msg);
                        if(!TextUtils.isEmpty(msg)){
                            try {
                                JSONObject json=new JSONObject(msg);
                                if(json.getBoolean("success")){
                                    adapter.remove(pos);
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
    }


    private void initEvent() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.switchActivity(NotesActivity.this, AddNotesActivity.class);
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
