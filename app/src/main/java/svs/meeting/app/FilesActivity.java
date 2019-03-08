package svs.meeting.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import svs.meeting.activity.BaseActivity;
import svs.meeting.activity.PersonalPaletteActivity;
import svs.meeting.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import svs.meeting.data.Config;
import svs.meeting.list.FileItem;
import svs.meeting.list.FileItemListAdapter;
import svs.meeting.util.Helper;
import svs.meeting.util.HttpUtil;
import svs.meeting.util.IHttpCallback;
import svs.meeting.util.XLog;
import svs.meeting.widgets.DialogWaiting;

public class FilesActivity extends BaseActivity implements ListView.OnItemClickListener {
    private Toolbar mToolbar;
    private ListView listView;
    private FileItemListAdapter fileListAdapter;
    private java.util.ArrayList<FileItem> fileList = new java.util.ArrayList<FileItem>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        initActionBar();
        listView = (ListView) this.findViewById(R.id.listview1);
        fileListAdapter = new FileItemListAdapter(this, listView, null);
        fileListAdapter.setList(fileList);
        listView.setAdapter(fileListAdapter);
        listView.setLayoutAnimation(Helper.getAnimationController());
        listView.setOnItemClickListener(this);
        listFiles();
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("会议资料");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    private void listFiles() {
        String sql="select a.*,b.id as document_id from files a left outer join (select * from documents where meeting_id='"+Config.meetingId+"' group by file_id) b on a.id=b.file_id " +
                "where a.meeting_id='"+Config.meetingId+"' and a.file_type='00'";
        java.util.HashMap<String, String> params = Config.getParameters();
        String url = "/query";
        params.put("ql",sql);
        HttpUtil.requestURL(Config.WEB_URL + url, params, new IHttpCallback() {
            @Override
            public void onHttpComplete(int code, String result) {
                // TODO Auto-generated method stub
                //XLog.warn("获取文件列表("+(code==HttpUtil.HTTP_OK)+"):"+result,LoginActivity.class);
                handler.sendMessage(handler.obtainMessage(0x01, result));
            }
        });
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e("msg.obj.toString()","msg.obj.toString()="+msg.obj.toString());
            Intent intent = new Intent();
            switch (msg.what) {
                case 0x01:
                    try{
                        org.json.JSONObject json = new org.json.JSONObject(msg.obj.toString());
                        if(json.getString("success").equals("true")){
                            org.json.JSONArray rows=json.getJSONArray("rows");
                            fileList.clear();
                            for(int i=0;i<rows.length();i++){
                                org.json.JSONObject item=rows.getJSONObject(i);
                                FileItem fileItem=new FileItem();
                                fileItem.setFile_name(item.getString("file_name"));
                                fileItem.setFile_path(item.getString("file_path"));
                                fileItem.setFile_ext(item.getString("file_ext"));
                                fileList.add(fileItem);
                            }
                            fileListAdapter.notifyDataSetChanged();
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                    break;
/*                case 0x02:
                    try{
                        org.json.JSONObject json = new org.json.JSONObject(msg.obj.toString());
                        if(json.getString("success").equals("true")){
                            String path=json.getString("path");
                            Bundle bd=new Bundle();
                            bd.putString("path",path);
                            bd.putInt("count",json.getInt("count"));
                            Helper.switchActivity(FilesActivity.this,FileViewerActivity.class,bd);
                        }else{
                            Toast.makeText(FilesActivity.this,"文件不存在!",Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    break;*/
                default:
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        FileItem item=fileList.get(i);
        String path=item.getFile_path();
        Bundle bd=new Bundle();
        if(path.endsWith(".flv")||path.endsWith(".mp4")){
            String url=Config.WEB_URL+"/"+path;
            bd.putString("playUrl",url);
            Helper.switchActivity(FilesActivity.this, LivePlayerDemoActivity.class,bd);
        }else if(path.endsWith(".png")){
            bd.putString("path",item.getFile_path());
            bd.putString("name",item.getFile_name());
            bd.putInt("type",1);
            Helper.switchActivity(FilesActivity.this,PersonalPaletteActivity.class,bd);
        }


/*        FileItem item=fileList.get(i);
        XLog.log("=="+i+",l"+l+",name=="+item.getFile_name()+",path="+item.getFile_path()+",dest="+item.getDest_file());
        java.util.HashMap<String, String> params = Config.getParameters();
        String url = "/png_check";
        params.put("dpi","120");
        params.put("pdf","/"+item.getFile_name());
        dlgWaiting.showDialog();
        HttpUtil.requestURL(Config.WEB_URL + url, params, new IHttpCallback() {
            @Override
            public void onHttpComplete(int code, String result) {
                // TODO Auto-generated method stub
                dlgWaiting.hideDialog();
                XLog.warn("结果("+(code==HttpUtil.HTTP_OK)+"):"+result,LoginActivity.class);
                //handler.sendMessage(handler.obtainMessage(code, result));
                handler.sendMessage(handler.obtainMessage(0x02, result));

            }
        });*/

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
        handler.removeCallbacksAndMessages(null);
    }
}
