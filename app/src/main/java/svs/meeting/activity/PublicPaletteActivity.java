package svs.meeting.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.yinghe.whiteboardlib.bean.PointEntity;
import com.yinghe.whiteboardlib.bean.SketchData;
import com.yinghe.whiteboardlib.bean.StrokeRecord;
import com.yinghe.whiteboardlib.fragment.WhiteBoardFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.app.FileViewerActivity;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.DawingTypes;
import svs.meeting.data.NotesEntity;
import svs.meeting.data.PaletteEntity;
import svs.meeting.util.PaletteUtils;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.widgets.LoadingDialogFragment;

public class PublicPaletteActivity extends BaseActivity implements WhiteBoardFragment.SendBtnCallback, WhiteBoardFragment.OnPageSelectListener {
    private Toolbar mToolbar;
    private String path;
    private int count;
    private int curPage;
    private String name;
    private String file_id = "aaa";
    private WhiteBoardFragment whiteBoardFragment;
    LoadingDialogFragment loadingDialogFragment = LoadingDialogFragment.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_public_palette);
        init();
    }

    private void init() {
        whiteBoardFragment = WhiteBoardFragment.newInstance();
        FragmentTransaction ts = getSupportFragmentManager().beginTransaction();
        ts.add(R.id.frame_content, whiteBoardFragment, "wb").commitAllowingStateLoss();
        whiteBoardFragment.setOnSketchDataListener(new WhiteBoardFragment.OnSketchDataListener() {
            @Override
            public void onSketchData(SketchData data, Bitmap bitmap) {
                analysisSketchData(data, bitmap);
            }
        });
        whiteBoardFragment.setOnPageSelectListener(this);
        initActionBar();
        getPaletteBgInfo();

    }

    private void getSaveData() {
        try {
            String seat_no = Config.clientInfo.getString("tid");
            String sql = "select * from documents where page='" + curPage  + "' and uid='" + seat_no + "'";
            Log.e("sql", "sql==" + sql);
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
                            Log.e("getSaveData onSuccess", msg);
                            if (!TextUtils.isEmpty(msg)) {
                                try {
                                    JSONObject json = new JSONObject(msg);
                                    if (json.getBoolean("success")) {
                                        resolveJson(json.getString("rows"));
                                    } else {
                                        Toast.makeText(PublicPaletteActivity.this, "数据查询失败！", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        @Override
                        public void onError(String msg) {
                            Log.e("getSaveData onError", msg);
                        }
                    }));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void resolveJson(String str) {
        List<PaletteEntity> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(str);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                PaletteEntity entity = new PaletteEntity();
                entity.setId(object.getString("id"));
                entity.setFile_id(object.getString("file_id"));
                entity.setMeeting_id(object.getString("meeting_id"));
                entity.setUid(object.getString("uid"));
                entity.setFile_url(object.getString("file_url"));
                entity.setPage(object.getString("page"));
                entity.setDoc_content(object.getString("doc_content"));
                entity.setDoc_name(object.getString("doc_name"));
                entity.setModified(object.getString("modified"));
                entity.setHeight(object.getInt("height"));
                entity.setWidth(object.getInt("width"));
                list.add(entity);
            }
            drawPalettePath(list);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void drawPalettePath(List<PaletteEntity> list) {
        List<StrokeRecord> lists = new ArrayList<>();
        SketchData data = new SketchData();
        try {
            for (PaletteEntity entity : list) {
                String doc_content = entity.getDoc_content();
                if (!TextUtils.isEmpty(doc_content)) {
                    JSONArray array = new JSONArray(doc_content);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        String type = object.getString("type");
                        if (DawingTypes.LINE.equals(type)) {
                            //封装直线数据
                            JSONObject p1=new JSONObject(object.getString("p1"));
                            JSONObject p2=new JSONObject(object.getString("p2"));
                            int startX=p1.getInt("x");
                            int startY=p1.getInt("y");
                            int endX=p2.getInt("x");
                            int endY=p2.getInt("y");
                            StrokeRecord record = new StrokeRecord(StrokeRecord.STROKE_TYPE_LINE);
                            Path path = new Path();
                            path.moveTo(startX, startY);
                            path.lineTo(endX, endY);
                            record.path = path;
                            record.paint = PaletteUtils.getDefaultPaint();
                            int color=object.getInt("color");
                            record.paint.setColor(color);
                            lists.add(record);
                        } else if (DawingTypes.FREE_LINE.equals(type)) {
                            //封装线条数据
                            JSONObject p1=new JSONObject(object.getString("p1"));
                            JSONObject p2=new JSONObject(object.getString("p2"));
                            int startX=p1.getInt("x");
                            int startY=p1.getInt("y");
                            int endX=p2.getInt("x");
                            int endY=p2.getInt("y");
                            StrokeRecord record = new StrokeRecord(StrokeRecord.STROKE_TYPE_DRAW);
                            Path path = new Path();
                            path.moveTo(startX, startY);
                            record.path = path;
                            record.paint = PaletteUtils.getDefaultPaint();
                            int color=object.getInt("color");
                            record.paint.setColor(color);
                            String point=object.getString("points");
                            JSONArray jsonArray=new JSONArray(point);
                            for (int j = 0; j <jsonArray.length() ; j++) {
                                String str=jsonArray.getString(j);
                                String points[]=str.split("\\|");
                                float x=Float.parseFloat(points[0]);
                                float y=Float.parseFloat(points[1]);
                                record.path.quadTo(x, y, x, y);
                            }
                            lists.add(record);

                        } else if (DawingTypes.ERASER.equals(type)) {
                            //封装橡皮擦数据
                            JSONObject p1=new JSONObject(object.getString("p1"));
                            JSONObject p2=new JSONObject(object.getString("p2"));
                            int startX=p1.getInt("x");
                            int startY=p1.getInt("y");
                            int endX=p2.getInt("x");
                            int endY=p2.getInt("y");
                            StrokeRecord record = new StrokeRecord(StrokeRecord.STROKE_TYPE_ERASER);
                            Path path = new Path();
                            path.moveTo(startX, startY);
                            record.path = path;
                            record.paint = PaletteUtils.getDefaultPaint();
                            record.paint.setStrokeWidth(50);
                            record.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                            String point=object.getString("points");
                            JSONArray jsonArray=new JSONArray(point);
                            for (int j = 0; j <jsonArray.length() ; j++) {
                                String str=jsonArray.getString(j);
                                String points[]=str.split("\\|");
                                float x=Float.parseFloat(points[0]);
                                float y=Float.parseFloat(points[1]);
                                record.path.quadTo(x, y, x, y);
                            }
                            lists.add(record);


                        } else if (DawingTypes.CIRCLE.equals(type)) {
                            //封装圆数据
                            JSONObject p1=new JSONObject(object.getString("p1"));
                            JSONObject p2=new JSONObject(object.getString("p2"));
                            int startX=p1.getInt("x");
                            int startY=p1.getInt("y");
                            int endX=p2.getInt("x");
                            int endY=p2.getInt("y");

                            StrokeRecord record = new StrokeRecord(StrokeRecord.STROKE_TYPE_CIRCLE);
                            RectF rect = new RectF(startX, startY, startX, startY);
                            record.rect = rect;
                            record.paint = PaletteUtils.getDefaultPaint();
                            int color=object.getInt("color");
                            record.paint.setColor(color);
                            record.rect.set(startX < endX ? startX : endX, startY < endY ?
                                    startY : endY, startX > endX ? startX : endX, startY > endY ? startX : endY);
                            lists.add(record);


                        } else if (DawingTypes.RECTANGLE.equals(type)) {
                            //封装矩形数据
                            JSONObject p1=new JSONObject(object.getString("p1"));
                            JSONObject p2=new JSONObject(object.getString("p2"));
                            int startX=p1.getInt("x");
                            int startY=p1.getInt("y");
                            int endX=p2.getInt("x");
                            int endY=p2.getInt("y");
                            StrokeRecord record = new StrokeRecord(StrokeRecord.STROKE_TYPE_RECTANGLE);
                            RectF rect = new RectF(startX, startY, startX, startY);
                            record.rect = rect;
                            record.paint = PaletteUtils.getDefaultPaint();
                            int color=object.getInt("color");
                            record.paint.setColor(color);
                            record.rect.set(startX < endX ? startX : endX, startY < endY ?
                                    startY : endY, startX > endX ? startX : endX, startY > endY ? startX : endY);
                            lists.add(record);

                        } else if (DawingTypes.TEXT.equals(type)) {
                            //封装文本数据
                            JSONObject p1=new JSONObject(object.getString("p1"));
                            int startX=p1.getInt("x");
                            int startY=p1.getInt("y");
                            JSONObject jsonObject=new JSONObject(object.getString("extra"));
                            String text=jsonObject.getString("text");
                            String strWidth=jsonObject.getString("width");
                            int color=jsonObject.getInt("color");

                            StrokeRecord record = new StrokeRecord(StrokeRecord.STROKE_TYPE_TEXT);
                            record.textOffX=startX;
                            record.textOffY=startY;
                            record.textPaint=new TextPaint();
                            record.textPaint.setAntiAlias(true);
                            record.textPaint.setColor(color);
                            record.textPaint.setTextSize(18);
                            record.text=text;
                            record.textWidth=Integer.parseInt(strWidth);
                            lists.add(record);
                        }
                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        data.strokeRecordList = lists;
        whiteBoardFragment.setLocData(data);
    }

    @Override
    public void onSendBtnClick(final File filePath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("onSendBtnClick", "onSendBtnClick=" + filePath.getAbsolutePath());
            }
        });
    }

    private void getPaletteBgInfo() {
        Map<String, String> map = Config.getParameters();
        RequestManager.getInstance()
                .mServiceStore
                .png_check(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                    @Override
                    public void onSuccess(String msg) {
                        Log.e("getPaletteBgInfo", msg);
                        if (!TextUtils.isEmpty(msg)) {
                            try {
                                JSONObject json = new JSONObject(msg);
                                if (json.getBoolean("success")) {
                                    path = json.getString("path");
                                    count = json.getInt("count");
                                    curPage = 1;
                                    if (!TextUtils.isEmpty(path) && count > 0) {
                                        name = "p_" + curPage + ".png";
                                        loadingDialogFragment.show(getSupportFragmentManager(), "analysisSketchData");
                                        new BitmapThread(Config.WEB_URL + path + "/" + name, name).start();
                                        getSaveData();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("getPaletteBgInfo", msg);
                    }
                }));
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("公共白板");
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
    public void onPageUp() {
        if (curPage == 1) {
            Toast.makeText(this, "已是第一页！", Toast.LENGTH_SHORT).show();
            return;
        }
        curPage = curPage - 1;
        name = "p_" + curPage + ".png";
        loadingDialogFragment.show(getSupportFragmentManager(), "analysisSketchData");
        new BitmapThread(Config.WEB_URL + path + "/" + name, name).start();

    }

    @Override
    public void onPageNext() {
        if (curPage > count) {
            Toast.makeText(this, "已是最后一页！", Toast.LENGTH_SHORT).show();
            return;
        }
        curPage = curPage + 1;
        name = "p_" + curPage + ".png";
        loadingDialogFragment.show(getSupportFragmentManager(), "analysisSketchData");
        new BitmapThread(Config.WEB_URL + path + "/" + name, name).start();

    }

    class BitmapThread extends Thread {
        private String bitmapUrl;
        private String key;

        BitmapThread(String bitmapUrl, String key) {
            this.bitmapUrl = bitmapUrl;
            this.key = key;
        }

        @Override
        public void run() {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(bitmapUrl);
                Log.e("bitmapUrl", "bitmapUrl=" + bitmapUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialogFragment.dismissAllowingStateLoss();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialogFragment.dismissAllowingStateLoss();
                        }
                    });
                }
                if (bitmap != null) {
                    final Bitmap finalBitmap = bitmap;
                    boolean isSuccess = saveImageToGallery(PublicPaletteActivity.this, finalBitmap, key);
                    if (isSuccess) {
                        File file = new File(Config.STOREPATH + key);
                        if (file.exists()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("whiteBoardFragment", "Config.STOREPATH+key==" + Config.STOREPATH + key);
                                    whiteBoardFragment.setCurBackgroundByPath(Config.STOREPATH + key);
                                    whiteBoardFragment.setPageData(curPage, count);
                                }
                            });
                        }
                    }
                } else {
                    Log.e("bitmapdown", "bitmapdown=null");
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

    public static boolean saveImageToGallery(Context context, Bitmap bmp, String name) {
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

    private void analysisSketchData(SketchData data, Bitmap bitmap) {
        loadingDialogFragment.show(getSupportFragmentManager(), "analysisSketchData");
        JSONArray array = new JSONArray();
        if (data != null) {
            for (int i = 0; i < data.strokeRecordList.size(); i++) {
                StrokeRecord record = data.strokeRecordList.get(i);

                int type = record.type;
                if (type == StrokeRecord.STROKE_TYPE_LINE) {//直线
                    int downX = record.downX;
                    int downY = record.downY;
                    int preX = record.preX;
                    int preY = record.preY;
                    //画笔设置
                    float paintwidth = record.paint.getStrokeWidth();
                    float paintColor = record.strokeColor;
                    //上传直线数据
                    try {
                        JSONObject map = new JSONObject();
                        map.put("p2", PaletteUtils.getPointJson(preX, preY));
                        map.put("p1", PaletteUtils.getPointJson(downX, downY));
                        map.put("pen", paintwidth + "");
                        map.put("color", paintColor + "");
                        map.put("type", DawingTypes.LINE);
                        array.put(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (type == StrokeRecord.STROKE_TYPE_ERASER) {//橡皮擦
                    int downX = record.downX;
                    int downY = record.downY;
                    int preX = record.preX;
                    int preY = record.preY;
                    //画笔设置
                    float paintwidth = record.paint.getStrokeWidth();
                    float paintColor = record.strokeColor;
                    List<PointEntity> list = record.erasers;
                    JSONArray points = new JSONArray();
                    for (PointEntity p : list) {
                        //循环获取橡皮擦路径数据
                        points.put(p.getEndX() + "|" + p.getEndY());
                    }
                    try {
                        JSONObject map = new JSONObject();
                        map.put("p2", PaletteUtils.getPointJson(preX, preY));
                        map.put("p1", PaletteUtils.getPointJson(downX, downY));
                        map.put("pen", paintwidth + "");
                        map.put("color", paintColor + "");
                        map.put("type", DawingTypes.ERASER);
                        map.put("points", points);
                        array.put(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (type == StrokeRecord.STROKE_TYPE_DRAW) {//线条
                    int downX = record.downX;
                    int downY = record.downY;
                    int preX = record.preX;
                    int preY = record.preY;
                    //画笔设置
                    float paintwidth = record.paint.getStrokeWidth();
                    float paintColor = record.strokeColor;
                    List<PointEntity> list = record.points;
                    JSONArray points = new JSONArray();
                    for (PointEntity p : list) {
                        //循环获取橡皮擦路径数据
                        points.put(p.getEndX() + "|" + p.getEndY());
                    }
                    try {
                        JSONObject map = new JSONObject();
                        map.put("p2", PaletteUtils.getPointJson(preX, preY));
                        map.put("p1", PaletteUtils.getPointJson(downX, downY));
                        map.put("pen", paintwidth + "");
                        map.put("color", paintColor + "");
                        map.put("type", DawingTypes.FREE_LINE);
                        map.put("points", points);
                        array.put(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (type == StrokeRecord.STROKE_TYPE_CIRCLE) {//圆
                    int downX = record.downX;
                    int downY = record.downY;
                    int preX = record.preX;
                    int preY = record.preY;
                    //画笔设置
                    float paintwidth = record.paint.getStrokeWidth();
                    float paintColor = record.strokeColor;
                    //圆形四个点
                    try {
                        JSONObject map = new JSONObject();
                        map.put("p2", PaletteUtils.getPointJson(preX, preY));
                        map.put("p1", PaletteUtils.getPointJson(downX, downY));
                        map.put("pen", paintwidth + "");
                        map.put("color", paintColor + "");
                        map.put("type", DawingTypes.CIRCLE);
                        array.put(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else if (type == StrokeRecord.STROKE_TYPE_RECTANGLE) {//矩形
                    int downX = record.downX;
                    int downY = record.downY;
                    int preX = record.preX;
                    int preY = record.preY;
                    //画笔设置
                    float paintwidth = record.paint.getStrokeWidth();
                    float paintColor = record.strokeColor;
                    //矩形的四个点
                    try {
                        JSONObject map = new JSONObject();
                        map.put("p2", PaletteUtils.getPointJson(preX, preY));
                        map.put("p1", PaletteUtils.getPointJson(downX, downY));
                        map.put("pen", paintwidth + "");
                        map.put("color", paintColor + "");
                        map.put("type", DawingTypes.RECTANGLE);
                        array.put(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (type == StrokeRecord.STROKE_TYPE_TEXT) {//文本
                    //文本内容
                    int textOffX = record.textOffX;
                    int textOffY = record.textOffY;
                    String text = record.text;
                    TextPaint tp=record.textPaint;
                    float paintColor = record.strokeColor;
                    Log.e("eeee","rrrr=="+record.textWidth+";"+record.textPaint.getStrokeWidth());
                    try {
                        JSONObject map = new JSONObject();
                        map.put("p1", PaletteUtils.getPointJson(textOffX, textOffY));
                        map.put("pen", tp.getStrokeWidth() + "");
                        map.put("color", tp.getColor() + "");
                        map.put("type", DawingTypes.TEXT);
                        JSONObject object=new JSONObject();
                        object.put("x",textOffX+"");
                        object.put("y",textOffY+"");
                        object.put("name",text);
                        object.put("color",paintColor+"");
                        object.put("text",text);
                        object.put("width",record.textWidth);
                        Log.e("extra","extra=="+object.toString());
                         map.put("extra",object);
                        array.put(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        saveData(array, bitmap);
    }

    private void saveData(final JSONArray array, final Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "bitmap不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String id = Config.meetingInfo.getString("id");
            String uname = Config.clientInfo.getString("name");
            String meeting_name = Config.meetingInfo.getString("name");
            String seat_no = Config.clientInfo.getString("tid");
            String bitmapBuffer = PaletteUtils.bitmapToBase64(bitmap);
            Map<String, String> params = new HashMap<>();
            params.put("end_filter", "on_upload_image");
            params.put("rawImage", bitmapBuffer);
            JSONObject map = new JSONObject();
            map.put("file_id", "aaa");
            map.put("meeting_id", id);
            map.put("meeting_name", meeting_name);
            map.put("uid", seat_no);
            map.put("uname", uname);
            map.put("file_url", 1 + "");
            map.put("page", curPage);
            map.put("doc_name", name);
            map.put("modified", getNowTime());
            map.put("doc_content", array.toString());
            map.put("width", bitmap.getWidth() + "");
            map.put("height", bitmap.getHeight() + "");
            map.put("doc_id", "0");
            params.put("raw", map.toString());
            Object object = new JSONObject(params);
            Log.e("saveDoc", "saveDoc==" + object.toString());
            RequestManager.getInstance()
                    .mServiceStore
                    .saveDoc(params)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                        @Override
                        public void onSuccess(String msg) {
                            Log.e("saveDoc", msg);
                            if (!TextUtils.isEmpty(msg)) {
                                try {
                                    JSONObject json = new JSONObject(msg);
                                    loadingDialogFragment.dismissAllowingStateLoss();
                                    if (json.getBoolean("success")) {
                                        Toast.makeText(PublicPaletteActivity.this,
                                                "保存成功！", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(PublicPaletteActivity.this,
                                                "保存失败！", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(String msg) {
                            loadingDialogFragment.dismissAllowingStateLoss();
                            Log.e("saveDoc", msg);
                        }
                    }));
        } catch (JSONException e) {
            e.printStackTrace();
            loadingDialogFragment.dismissAllowingStateLoss();
        }
    }


    private String getUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private String getNowTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    public String getName() {
        return name;
    }
}
