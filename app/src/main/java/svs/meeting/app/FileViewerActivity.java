package svs.meeting.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import svs.meeting.activity.BaseActivity;
import svs.meeting.activity.ImgEditActivity;
import svs.meeting.activity.PublicPaletteActivity;
import svs.meeting.app.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;

import svs.meeting.data.Config;
import svs.meeting.list.ImageListAdapter;
import svs.meeting.util.Helper;
import svs.meeting.util.ImageDownloader;
import svs.meeting.widgets.DialogWaiting;

public class FileViewerActivity extends BaseActivity {
    private Toolbar mToolbar;
    private String fileName;
    private String filePath;
    private ImageView mImg;
    private String url;
    private TextView mTextEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);
        init();
    }

    private void init() {
        initActionBar();
        initViews();
        initDatas();
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("资料文件");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        fileName=getIntent().getStringExtra("name");
        filePath=getIntent().getStringExtra("path");

    }
    private void initViews() {
        mImg=findViewById(R.id.img);
        mTextEdit=mToolbar.findViewById(R.id.tv_right_title);
        mTextEdit.setText("编辑");
        mTextEdit.setVisibility(View.VISIBLE);
        mTextEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FileViewerActivity.this, "点击编辑", Toast.LENGTH_SHORT).show();
                Log.e("fileName","fileName="+fileName);
                Intent intent=new Intent(FileViewerActivity.this, ImgEditActivity.class);
                intent.putExtra(ImgEditActivity.EXTRA_IMAGE_SAVE_PATH,fileName);
                startActivityForResult(intent,1);
            }
        });
       /* mTextEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FileViewerActivity.this, "点击编辑", Toast.LENGTH_SHORT).show();
                Helper.switchActivity(FileViewerActivity.this, ImgEditActivity.class);
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("onActivityResult","onActivityResult requestCode="+requestCode+"  resultCode="+resultCode);
    }

    private void initDatas() {
        if(!TextUtils.isEmpty(filePath)){
            url=Config.WEB_URL+"/"+filePath;
            File file=new File(Config.STOREPATH+fileName);
            if(file.exists()){
                Bitmap bitmap=BitmapFactory.decodeFile(Config.STOREPATH+fileName);
                if(bitmap!=null){
                    mImg.setImageBitmap(bitmap);
                }
            }else {
                new BitmapThread(url,fileName).start();
            }

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

    class BitmapThread extends Thread {
        private String bitmapUrl;
        private String key;

        BitmapThread(String bitmapUrl,String key) {
            this.bitmapUrl = bitmapUrl;
            this.key=key;

        }
        @Override
        public void run() {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(bitmapUrl);
                Log.e("bitmapUrl","bitmapUrl="+bitmapUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }
                if(bitmap!=null){
                    final Bitmap finalBitmap = bitmap;
                    saveImageToGallery(FileViewerActivity.this,finalBitmap,key);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImg.setImageBitmap(finalBitmap);
                        }
                    });
                }else {
                    Log.e("bitmapdown","bitmapdown=null");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public static boolean saveImageToGallery(Context context, Bitmap bmp,String name){
        // 首先保存图片
        File appDir = new File(Config.STOREPATH);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = name + "";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



}
