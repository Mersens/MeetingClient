package svs.meeting.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import svs.meeting.app.MyApplication;
import svs.meeting.data.Config;
import svs.meeting.data.VoiceMode;
import svs.meeting.util.httputils.HttpClientSslHelper;
import svs.meeting.widgets.LoadingDialog;

public class Helper {

    private static final String TAG = "Helper";
    public static int screenWidth = 0;
    public static int screenHeight = 0;
    private static Vibrator vib = null;
    private static MediaPlayer player = null;
    public static java.text.NumberFormat numberFormat;
    public static int statusBarHeight = 0;

    /**
     * 显示加载对话框
     */
    private static LoadingDialog dialog;
    public static void showDialog(Activity activity){
        if(dialog == null){
            dialog = new LoadingDialog.Builder(activity).create();
            dialog.show();
        }else{
            if(!dialog.isShowing()){
                dialog = new LoadingDialog.Builder(activity).create();
                dialog.show();
            }
        }


    }
    public static void dismisDialog(){
        if(dialog != null){
            if(dialog.isShowing()){
                dialog.dismiss();
            }
        }
    }

    public static void initScreenParams(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    public static void switchActivity(Context context, Class<?> actCls) {
        switchActivity(context, actCls, null);
    }

    public static int Dp2Px(Context context, float dp) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        float density = metrics.density;
        return (int) (dp * density + 0.5f);
    }

    public static int Px2Dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static void setPaddingForStatusBar(View view, Activity activity) {
        int statusBarHeight = Helper.getStatusBarHeight(activity);
        view.setPadding(view.getPaddingLeft(), statusBarHeight, view.getPaddingRight(), view.getPaddingBottom());
    }

    public static int getStatusBarHeight(Activity activity) {
        if (statusBarHeight > 0)
            return statusBarHeight;
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = activity.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            Log.d("EX", "get status bar height fail");
            e1.printStackTrace();
            statusBarHeight = 75;
        }
        return statusBarHeight;
    }


    public static String getJson(String fileName, Context context){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line=bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }


    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            // 读取uri所在的图片
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void switchActivity(Context context, Class<?> actCls, Bundle params) {
        Intent intent = new Intent();
        intent.setClass(context, actCls);
        if (params != null) {
            Set<String> set = params.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String k = it.next();
                try {
                    Object o = params.get(k);
                    if (o instanceof java.io.Serializable)
                        intent.putExtra(k, (java.io.Serializable) o);
                    else {
                        String val = params.getString(k);
                        intent.putExtra(k, val);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        //boolean isMain=context.getClass().equals(MainActivity.class);
        context.startActivity(intent);
        //设置切换动画，从右边进入，左边退出,带动态效果
        //((Activity)context).overridePendingTransition(R.anim.new_dync_in_from_right, R.anim.new_dync_out_to_left);
        //((Activity)context).overridePendingTransition(R.anim.fade, R.anim.fade_out);
        //if(!isMain)
        //((Activity)context).finish();
    }

    public static String post(String url, java.util.HashMap<String, String> params) throws Exception {
        return post(url, params, "UTF-8");
    }

    public static String post(String url) throws Exception {
        return post(url, null, "UTF-8");
    }

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    public static synchronized String post(String url, java.util.HashMap<String, String> paramsMap, String strEncode) throws Exception {
        System.out.println("url=" + url + ",params=" + paramsMap);
        OkHttpClient client = new OkHttpClient();

        //处理参数
        StringBuilder tempParams = new StringBuilder();
        int pos = 0;
        for (String key : paramsMap.keySet()) {
            if (pos > 0) {
                tempParams.append("&");
            }
            tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), strEncode)));
            pos++;
        }
        //生成参数
        String params = tempParams.toString();
        //创建�?��请求实体对象 RequestBody
        System.out.println("REQUEST params:" + params.toString());
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, params);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static Drawable roundDrawable(Drawable imageDrawable, Context context) {
        Bitmap bmp = Helper.drawableToBitmap(imageDrawable, context);
        bmp = Helper.toRoundBitmap(bmp);//createRoundConerImage(bmp,bmp.getWidth(),bmp.getHeight(),30);
        return Helper.bitmapToDrawble(bmp, context);
    }

    /**
     * Drawable转化为Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable, Context context) {
        int width = Helper.Dp2Px(context, drawable.getIntrinsicWidth()); //获取的单位为dp
        int height = Helper.Dp2Px(context, drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        //XLog.log("drawableToBitmap=>bitmap("+bitmap.getWidth()+","+bitmap.getHeight()+"),drawable("+drawable.getIntrinsicWidth()+","+drawable.getBounds().width()+")",Helper.class);
        return bitmap;

    }

    /**
     * Bitmap to Drawable
     *
     * @param bitmap
     * @param mcontext
     * @return
     */
    public static Drawable bitmapToDrawble(Bitmap bitmap, Context mcontext) {
        Drawable drawable = new BitmapDrawable(mcontext.getResources(), bitmap);
        //XLog.log("bitmapToDrawble=>bitmap("+bitmap.getWidth()+","+bitmap.getHeight()+"),drawable("+drawable.getIntrinsicWidth()+","+drawable.getBounds().width()+")",Helper.class);
        return drawable;
    }

    /**
     * 根据原图添加圆角
     *
     * @param source
     * @return
     */
    public static Bitmap createRoundConerImage(Bitmap source, int mWidth, int mHeight, float mRadius) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        RectF rect = new RectF(0, 0, source.getWidth(), source.getHeight());
        canvas.drawRoundRect(rect, mRadius, mRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            left = 0;
            top = 0;
            right = width;
            bottom = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);// 设置画笔无锯齿

        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
        paint.setColor(color);

        // 以下有两种方法画圆,drawRounRect和drawCircle
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
        canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
        canvas.drawBitmap(bitmap, src, dst, paint); //以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

        return output;
    }


    /**
     * 得到本地或者网络上的bitmap url - 网络或者本地图片的绝对路径,比如:
     * <p>
     * A.网络路径: url="http://blog.foreverlove.us/girl2.png" ;
     * <p>
     * B.本地路径:url="file://mnt/sdcard/photo/image.png";
     * <p>
     * C.支持的图片格式 ,png, jpg,bmp,gif等等
     *
     * @param url
     * @return
     */
    public static Bitmap GetLocalOrNetBitmap(String url) {
        Bitmap bitmap = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(new File(url)), 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    @SuppressWarnings("deprecation")
    public static Drawable resizeImage2(String path,
                                        int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不加载bitmap到内存中
        BitmapFactory.decodeFile(path, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            //Log.d(tag, "sampleSize = " + sampleSize);
            options.inSampleSize = sampleSize;
        }

        options.inJustDecodeBounds = false;
        return new BitmapDrawable(BitmapFactory.decodeFile(path, options));
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }


    public static String uploadFiles(String actionUrl, Map<String, String> params, Map<String, File> files) throws IOException {

        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";
        URL uri = new URL(actionUrl);
        HttpURLConnection conn = null;
        if (actionUrl.toLowerCase().startsWith("https")) {
            conn = (HttpsURLConnection) uri.openConnection();
            if (conn instanceof HttpsURLConnection) {
                SSLContext sslContext = HttpClientSslHelper.getSslContext(MyApplication.applicationContext);
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslContext.getSocketFactory());
            }
        } else {
            conn = (HttpURLConnection) uri.openConnection();
        }
//        conn.setReadTimeout(5 * 1000);
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false);
        conn.setRequestMethod("POST"); // Post方式
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");

        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\""
                    + entry.getKey() + "\"" + LINEND);
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(entry.getValue());
            sb.append(LINEND);
        }

        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
        outStream.write(sb.toString().getBytes());

        if (files != null){
            for (Map.Entry<String, File> file : files.entrySet()) {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""
                        + file.getKey() + "\"" + LINEND);
                sb1.append("Content-Type: multipart/form-data; charset="
                        + CHARSET + LINEND);
                sb1.append(LINEND);
                outStream.write(sb1.toString().getBytes());
                InputStream is = new FileInputStream(file.getValue());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                is.close();
                outStream.write(LINEND.getBytes());
            }
    }


        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
        outStream.write(end_data);
        outStream.flush();
        // 得到响应码
//        boolean success = conn.getResponseCode()==200;
        InputStream in;
     if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
          in = conn.getInputStream();
     }else{
          in = conn.getErrorStream();
     }

        InputStreamReader isReader = new InputStreamReader(in);
        BufferedReader bufReader = new BufferedReader(isReader);
        String line = null;
        String data = "";
        while ((line = bufReader.readLine()) != null){
            data += line;
        }


        outStream.close();
        conn.disconnect();
        return data;
    }



    @SuppressLint("SimpleDateFormat")
    public static String formatDate(String pattern, java.util.Date date) {

        SimpleDateFormat df = new SimpleDateFormat(pattern);
        String formated = df.format(date);
        df = null;
        return formated;
    }

    @SuppressLint("SimpleDateFormat")
    public static long parseTime(String pattern, String strDate) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate).getTime();
        } catch (Exception ex) {
            return 0;
        }
    }

    public static void playAudio(Context context, int res, boolean loop) {
        playAudio(context, res, loop, false);
    }

    public static void playAudio(Context context, String path, boolean loop, boolean vibator){
        stopPlayer();
        player = MediaPlayer.create(context, Uri.parse(path));
        player.setLooping(loop);
        player.start();
        if (vibator) {
            playVibator(context, 1000 * 5);
        }
    }

    public static void playAudio(Context context, int res, boolean loop, boolean vibator) {
        stopPlayer();
        //*
        player = MediaPlayer.create(context, res);
        player.setLooping(loop);
        player.start();
        if (vibator) {
            playVibator(context, 1000 * 5);
        }//*/
    }

    public static void stopPlayer() {
        if (player != null) {
            if (player.isPlaying())
                player.stop();
            player.release();
            player = null;
        }

        if (vib != null) {
            vib.cancel();
        }
    }

    public static void playVibator(Context context, long timelong) {
        vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        long[] pattern = {200, 600, 200, 600};
        vib.vibrate(pattern, 0);
    }

    public static String getRunningActivityName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            XLog.warn("versionCode=" + info.versionCode + ",name=" + info.versionName, Helper.class);
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static int getRotationAngle(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    public static int getRotationAngle(int rotation) {
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    public static String loadAssets(Context context, String name) {
        try {
            InputStream in = context.getAssets().open(name);
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            String content = new String(buffer);
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Drawable LoadImageFromWebOperations(String url) {
        URL m;
        InputStream i = null;
        try {
            m = new URL(url);
            if (url.toLowerCase().startsWith("https")){
                SSLContext sslContext = HttpClientSslHelper.getSslContext(MyApplication.applicationContext);
                HttpsURLConnection connection = (HttpsURLConnection) m.openConnection();
                connection.setSSLSocketFactory(sslContext.getSocketFactory());
                i = (InputStream)connection.getContent();
            }else{
                i = (InputStream)m.getContent();
            }

            Drawable d = Drawable.createFromStream(i, "src name");
            return d;
        } catch (Exception e) {
            System.out.println("Exc=" + e);
            return null;
        }
    }


    public static void loadImageAsync(final String url, final ImageLoadListener l) {
        new Thread() {
            public void run() {
                //XLog.info("request="+url);
                try {
                    Drawable drawable = LoadImageFromWebOperations(url);
                    if (l != null)
                        l.onImageLoaded(drawable, url);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    public static LayoutAnimationController getAnimationController() {
        return getAnimationController(150);
    }

    public static LayoutAnimationController getAnimationController(int duration) {
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.8f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    public static String formatAmount(double amount) {
        if (amount == 0) {
            return "0.00";
        }
        int rmb = (int) (amount * 100);
        amount = rmb;
        amount = amount / 100;
        DecimalFormat df = new DecimalFormat("#.00");
        String str = df.format(amount);
        return str;
    }

    public static String formatAmountRMB(double amount) {
        if (amount == 0) {
            return "0.00";
        }
        int rmb = (int) (amount * 100);
        amount = rmb;
        amount = amount / 100;
        DecimalFormat df = new DecimalFormat("#.00");
        String str = df.format(amount);
        return MoneyBlWs(str, 2);
    }

    public static String MoneyBlWs(String money, int i) {
        String newnum = "";
        if (i == 0) {
            if (money.contains(",")) {
                money = money.replace(",", "");
            }
            if (money.contains(".")) {
                newnum = money.substring(0, money.indexOf("."));
            } else {
                newnum = money;
            }
        } else if (i > 0) {
            if (money.contains(",")) {
                money = money.replace(",", "");
            }
            String[] arry = money.split("\\.");
            String zhengshu = arry[0];
            if (zhengshu.equals("")) {
                zhengshu = "0";
            }
            if (arry.length > i - 1) {
                System.out.println(newnum);
                if (arry[1].length() > 1) {
                    String xiaoshu = arry[1].substring(0, i);
                    newnum = zhengshu + "." + xiaoshu;
                } else {
                    newnum =  zhengshu + "." +arry[1] + "0";
                }
            } else {
                newnum = zhengshu + ".00";
            }
            double aa = Double.parseDouble(newnum);
            if (aa != 0) {
                DecimalFormat df = new DecimalFormat("###,###.00");
                System.out.println(df.format(aa));
                newnum = df.format(aa);
                arry = newnum.split("\\.");
                if (arry[0].equals("")) {
                    newnum = "0"+newnum;
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }

        return newnum;

    }

    public static  boolean isAvilible(Context context, String packageName){
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if(packageInfos != null){
            for(int i = 0; i < packageInfos.size(); i++){
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    public static String getHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length() - 1);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T parseObject(JSONObject json, Class<T> cls) {
        T t = null;
        try {
            t = cls.newInstance();
            Method[] methods = cls.getMethods();
            HashMap<String, String> methodsMap = new HashMap<String, String>();
            for (Method m : methods) {
                if (m.getName().startsWith("set")) {
                    String attName = m.getName().substring(3).toLowerCase();
                    methodsMap.put(attName, m.getName());
                    Object[] types = m.getParameterTypes();
                    if (types.length == 1 && types[0].toString().equals("boolean")) {
                        methodsMap.put("is" + attName.toLowerCase(), m.getName());
                    }
                }
            }
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                String name = f.getName();
                Class type = f.getType();
                String typeName = type.getName();
                String k = name.toLowerCase();

                if (methodsMap.containsKey(k)) {
                    Method m = cls.getDeclaredMethod(methodsMap.get(k), type);
                    //Log.i(TAG,"name="+name+",k="+k+",method="+m.getName());
                    if (json.has(name)) {
                        if (typeName.equals("boolean"))
                            m.invoke(t, json.getBoolean(name));
                        else if (typeName.equals("int"))
                            m.invoke(t, json.getInt(name));
                        else if (typeName.equals("org.json.JSONArray")) {
                            if (json.has(name))
                                m.invoke(t, json.getJSONArray(name));
                        } else if (typeName.equals("java.util.HashMap")) {
                            //m.invoke(t,json.getJSONArray(name));
                        } else
                            m.invoke(t, json.getString(name));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return t;
    }

    public static <T> JSONObject toJSONObject(T o) {
        JSONObject json = new JSONObject();
        try {
            Class cls = o.getClass();
            Method[] methods = cls.getMethods();
            HashMap<String, String> methodsMap = new HashMap<String, String>();
            for (Method m : methods) {
                if (m.getName().startsWith("get")) {
                    String attName = m.getName().substring(3).toLowerCase();
                    methodsMap.put(attName, m.getName());
                } else if (m.getName().startsWith("is")) {
                    String attName = m.getName().substring(2).toLowerCase();
                    methodsMap.put(attName, m.getName());
                    methodsMap.put(m.getName().toLowerCase(), m.getName());
                } else
                    methodsMap.put(m.getName(), "===");
            }
            //Log.i(TAG,"map=="+methodsMap);
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                String name = f.getName();
                Class type = f.getType();
                String typeName = type.getName();
                String k = name.toLowerCase();

                //Log.i("TAG","==>"+name+",type="+typeName);
                if (methodsMap.containsKey(k)) {
                    //Log.i(TAG,"method="+methodsMap.get(k)+",attr="+name);
                    Method m = cls.getDeclaredMethod(methodsMap.get(k));
                    Object ret = m.invoke(o);
                    json.put(name, ret);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return json;
    }

    public static String translateTime(String time) {
        long lTime = Helper.parseTime("yyyy-MM-dd HH:mm:ss", time);
//		java.util.Calendar now=java.util.Calendar.getInstance();
//		java.util.Calendar chatTime=java.util.Calendar.getInstance();
//		chatTime.setTimeInMillis(lTime);
//		int diff=now.get(java.util.Calendar.DAY_OF_YEAR)-chatTime.get(java.util.Calendar.DAY_OF_YEAR);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd ");
        Date date1 = null;
        try {
            date1 = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date2 = new Date();
        String Strdate2 = sdf2.format(date2);
        try {
            date2 = sdf2.parse(Strdate2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int diff = (int) ((date2.getTime() + 1000 * 3600 * 24 - date1.getTime()) / (1000 * 3600 * 24));
        //long diff= TimeUnit.MILLISECONDS.toDays(now.getTimeInMillis()-chatTime.getTimeInMillis());
        String strTime = time.substring(time.indexOf(" ") + 1);
        if (diff == 0) {
            strTime = "今天 " + strTime;
        } else if (diff == 1) {
            strTime = "昨天 " + strTime;
        } else if (diff > 1 && diff < 7) {
            strTime = diff + "天前 " + strTime;
        } else if (diff >= 7 && diff < 31) {
            long c = diff / 7;
            strTime = c + "周前 " + strTime;
        } else if (diff >= 30 && diff < 180) {
            long c = diff / 30;
            strTime = c + "个月前 " + strTime;
        } else if (diff >= 180 && diff < 366) {
            strTime = "半年前 " + strTime;
        } else {
            strTime = "一年前";
        }

        return strTime;//+"("+diff+")"+time;
    }

//    public static SSLContext sslContext;
    //public  static  Call call;

    public static void cancleDown(){
        //if (call != null){
        //    call.cancel();
        //}
        XLog.warn("cancelDown",Helper.class);
    }
    public static <T> void downLoadFile(String fileUrl, final String destFileName, final IDownloadCallback callBack) {
//		OkHttpClient mOkHttpClient=new OkHttpClient();
        final File file = new File(destFileName);
        if (file.exists()) {
            //successCallBack((T) file, callBack);
            return;
        }
//        overlockCard();
        XLog.warn("downLoadFile=>url:"+fileUrl+",destFileName:"+destFileName,Helper.class);
        SSLContext sslcontext = HttpClientSslHelper.getSslContext(MyApplication.applicationContext);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .sslSocketFactory(sslcontext.getSocketFactory())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .cookieJar(new CookieJar() {
                    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();
        final Request request = new Request.Builder().url(fileUrl).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.toString());
                callBack.onDownloadError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    long total = response.body().contentLength();
                    //Log.e(TAG, "total------>" + total);
                    callBack.onDownloadStart(total);
                    long current = 0;
                    is = response.body().byteStream();
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        current += len;
                        fos.write(buf, 0, len);
                        //Log.e(TAG, "current------>" + current);
                        //progressCallBack(total, current, callBack);
                        int progress = (int) (((float) current / total) * 100);
                        callBack.onDownloadProgress(progress);
                    }
                    fos.flush();
                    //successCallBack((T) file, callBack);
                    if (current == total) {
                        callBack.onDownloadComplete();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    //failedCallBack("下载失败", callBack);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        });
    }

    public static void showKeyboard(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
    }

    public static void hideKeyboard(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                //http://stackoverflow.com/a/7696791/1091751
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                View v = activity.getCurrentFocus();
                if (v == null) {
                    XLog.log("No current focus", activity.getClass());
                } else {
                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }

    /**
     * Gets the corresponding path to a file from the given content:// URI
     *
     * @param selectedVideoUri The content:// URI to find the file path from
     * @param contentResolver  The content resolver to use to perform the query.
     * @return the file path as a string
     */
    public static String getFilePathFromContentUri(Uri selectedVideoUri,
                                                   ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
//      也可用下面的方法拿到cursor
//      Cursor cursor = this.context.managedQuery(selectedVideoUri, filePathColumn, null, null, null);

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    /**
     * Gets the content:// URI from the given corresponding path to a file
     *
     * @param context
     * @param imageFile
     * @return content Uri
     */
    public static Uri getImageContentUri(Context context, java.io.File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 忽略所有https证书
     */
//    public static void overlockCard() {
//        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//            @Override
//            public void checkClientTrusted(
//                    java.security.cert.X509Certificate[] chain,
//                    String authType) throws CertificateException {
//            }
//
//            @Override
//            public void checkServerTrusted(
//                    java.security.cert.X509Certificate[] chain,
//                    String authType) throws CertificateException {
//            }
//
//            @Override
//            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                X509Certificate[] x509Certificates = new X509Certificate[0];
//                return x509Certificates;
//            }
//        }};
//        try {
//            sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustAllCerts,
//                    new java.security.SecureRandom());
//        } catch (Exception e) {
//            XLog.log("ssl出现异常");
//        }
//
//    }


    //下载铃声
    public static void downVoice(JSONArray jsonArray, String baseUrl)throws Exception {
        //XLog.log("下载铃声"+jsonArray.toString());
        String savePath = Environment.getExternalStorageDirectory() + "/";
        for (int i = 0; i < jsonArray.length() ; i++) {
            JSONObject js = jsonArray.getJSONObject(i);
            VoiceMode mode = new VoiceMode();
            mode.setPath(js.has("path")?baseUrl+js.getString("path"):"");
            String name = js.has("name")?js.getString("name"):"";
            mode.setName(name);
            mode.setDefault(js.has("default")?js.getBoolean("default"):false);
            mode.setSavePath(savePath+"/" + name+".mp3");
            Config.voiceList.add(mode);
            new downVoiceThrend(mode).start();
        }
    }

    private static class downVoiceThrend extends Thread {

        private VoiceMode mode;
        public downVoiceThrend(VoiceMode mode){
            this.mode = mode;
        }

        @Override
        public void run() {
            super.run();
            File voiceFile = new File(mode.getSavePath());
            if (voiceFile.exists()){
                voiceFile.delete();
            }
            Helper.downLoadFile(mode.getPath(), mode.getSavePath(), new IDownloadCallback() {
                @Override
                public void onDownloadStart(long total) {
                    XLog.log("开始下载:"+mode.getPath()+","+mode.getSavePath()+",name="+mode.getName(),Helper.class);
                }
                @Override
                public void onDownloadComplete() {
                    XLog.log("铃声下载完成"+mode.getName());
                }
                @Override
                public void onDownloadProgress(int percent) {

                }
                @Override
                public void onDownloadError() {

                }
            });

        }
    }

    public static void playVoice(Activity activity , String name){
        XLog.log("playVoice:"+name+",activity="+activity.getClass().getCanonicalName(),Helper.class);
        if(name == null || name.equals("")){
            return;
        }
        for (int i = 0; i < Config.voiceList.size() ; i++) {
            VoiceMode mode = Config.voiceList.get(i);
            if(mode.getName().equals(name)){
                Helper.playAudio(activity, mode.getSavePath(), false , false);
                break;
            }
        }
    }


}
