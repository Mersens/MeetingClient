package svs.meeting.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.NotesEntity;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.widgets.LinedEditText;

public class UpdateNotesActivity extends BaseActivity {
    private Toolbar mToolbar;
    private TextView mTextTitle;
    private NotesEntity entity;
    private TextView mTextTime;
    private LinedEditText mEditContent;
    private EditText mEditNoteTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_update_notes);
        init();
    }
    private void init() {
        entity=(NotesEntity)getIntent().getSerializableExtra("notesentity");
        initActionBar();
        initViews();
        initDatas();
        initEvent();
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("笔记修改");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    private void initViews() {
        mTextTitle=findViewById(R.id.tv_right_title);
        mTextTitle.setText("完成");
        mTextTitle.setVisibility(View.VISIBLE);
        mTextTime=findViewById(R.id.add_diary_tv_date);
        mEditContent=findViewById(R.id.add_diary_et_content);
        mEditNoteTitle=findViewById(R.id.add_diary_et_title);
    }

    private void initDatas() {
        if(entity==null){
            return;
        }
        mTextTime.setText("今天 "+getNowTime());
        mEditContent.setText(entity.getNote_content());
        mEditNoteTitle.setText(entity.getNote_title());
    }

    private void initEvent() {
        mTextTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title=mEditNoteTitle.getText().toString();
                final String content=mEditContent.getText().toString();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(UpdateNotesActivity.this, "标题不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(UpdateNotesActivity.this, "内容不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    String seat_no = Config.clientInfo.getString("tid");
                    String id = Config.meetingInfo.getString("id");
                    String uname = Config.clientInfo.getString("name");
                    String meeting_name=Config.meetingInfo.getString("name");
                    Map<String, String> map = Config.getParameters();
                    map.put("id",entity.getId());
                    map.put("pid",seat_no);
                    map.put("note_type","01");
                    map.put("note_content",content);
                    map.put("modified",getNowTime());
                    map.put("meeting_id",id);
                    map.put("meeting_name",meeting_name);
                    map.put("topic","free");
                    map.put("note_title",title);
                    map.put("uname",uname);
                    Map<String, String> m = Config.getParameters();
                    JSONObject jsonObject = new JSONObject(map);
                    String mapToJson=jsonObject.toString();
                    m.put("raw",mapToJson);
                    updateNotes(m);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    private String getNowTime(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd") ;
        return dateFormat.format(date);
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

    private void updateNotes( Map<String, String> map){

        RequestManager.getInstance()
                .mServiceStore
                .create_notes(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                    @Override
                    public void onSuccess(String msg) {
                        Log.e("updateNotes onSuccess", msg);
                        if(!TextUtils.isEmpty(msg)){
                            try {
                                JSONObject json=new JSONObject(msg);
                                if(json.getBoolean("success")){
                                    Toast.makeText(UpdateNotesActivity.this, "修改成功！", Toast.LENGTH_SHORT).show();
                                    finish();

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("updateNotes onError", msg);
                    }
                }));
    }
}
