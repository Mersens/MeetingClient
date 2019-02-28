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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.widgets.LinedEditText;

public class AddNotesActivity extends BaseActivity {
    private Toolbar mToolbar;
    private TextView mTextTitle;
    private TextView mTextTime;
    private LinedEditText mEditContent;
    private EditText mEditNoteTitle;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_notes);
        init();
    }
    private void init() {
        initActionBar();
        initViews();
        initDatas();
        initEvent();
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("笔记添加");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    private void initViews() {
        mTextTitle=mToolbar.findViewById(R.id.tv_right_title);
        mTextTitle.setText("完成");
        mTextTitle.setVisibility(View.VISIBLE);
        mTextTime=findViewById(R.id.add_diary_tv_date);
        mEditContent=findViewById(R.id.add_diary_et_content);
        mEditNoteTitle=findViewById(R.id.add_diary_et_title);
    }

    private void initDatas() {
        mTextTime.setText("今天 "+getNowTime());
    }

    private void initEvent() {
        mTextTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final String title=mEditNoteTitle.getText().toString();
                final String content=mEditContent.getText().toString();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddNotesActivity.this, "标题不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(AddNotesActivity.this, "内容不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    String seat_no = Config.clientInfo.getString("tid");
                    String id = Config.meetingInfo.getString("id");
                    String uname = Config.clientInfo.getString("name");
                    String meeting_name=Config.meetingInfo.getString("name");
                    Map<String, String> map = Config.getParameters();
                    map.put("id","0");
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
                    addNotes(m);

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

    private void addNotes( Map<String, String> map){

        RequestManager.getInstance()
                .mServiceStore
                .create_notes(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                    @Override
                    public void onSuccess(String msg) {
                        Log.e("addNotes onSuccess", msg);
                        if(!TextUtils.isEmpty(msg)){
                            try {
                                JSONObject json=new JSONObject(msg);
                                if(json.getBoolean("success")){
                                    Toast.makeText(AddNotesActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                                    finish();

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

}
