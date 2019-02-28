package svs.meeting.app;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.easydarwin.config.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import svs.meeting.util.CrashHandler;
import svs.meeting.util.Helper;

/**
 * Created by 刘灿成 on 2018/1/23/023.
 */

public class MyApplication extends Application {
    public static MyApplication application = null;
    public static Context applicationContext;
    private List<Activity> mList=new ArrayList<>();
    public static final String KEY_ENABLE_VIDEO = "key-enable-video";
    public static final String KEY_ENABLE_AUDIO = "key-enable-audio";
    public static final String CHANNEL_CAMERA = "camera";
    public static final Bus BUS = new Bus(ThreadEnforcer.ANY);
    public long mRecordingBegin;
    public boolean mRecording;

    private int result;
    private Intent intent;
    private MediaProjectionManager mMediaProjectionManager;

    public int getResult(){
        return result;
    }

    public Intent getIntent(){
        return intent;
    }

    public MediaProjectionManager getMediaProjectionManager(){
        return mMediaProjectionManager;
    }

    public void setResult(int result1){
        this.result = result1;
    }

    public void setIntent(Intent intent1){
        this.intent = intent1;
    }

    public void setMediaProjectionManager(MediaProjectionManager mMediaProjectionManager){
        this.mMediaProjectionManager = mMediaProjectionManager;
    }



    public MyApplication() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        getContext();
        application = this;
        //CrashHandler.getInstance().init(getApplicationContext());
        addLifecycleCallbacks();
        Helper.initScreenParams(applicationContext);
        resetDefaultServer();
        File youyuan = getFileStreamPath("SIMYOU.ttf");
        if (!youyuan.exists()){
            AssetManager am = getAssets();
            try {
                InputStream is = am.open("zk/SIMYOU.ttf");
                FileOutputStream os = openFileOutput("SIMYOU.ttf", MODE_PRIVATE);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.close();
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

       //


        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.camera);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_CAMERA, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }
    private void resetDefaultServer() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultIP = sharedPreferences.getString(Config.SERVER_IP, Config.DEFAULT_SERVER_IP);
        if ("114.55.107.180".equals(defaultIP)
                || "121.40.50.44".equals(defaultIP)
                || "www.easydarwin.org".equals(defaultIP)){
            sharedPreferences.edit().putString(Config.SERVER_IP, Config.DEFAULT_SERVER_IP).apply();
        }

        String defaultRtmpURL = sharedPreferences.getString(Config.SERVER_URL, Config.DEFAULT_SERVER_URL);
        int result1 = defaultRtmpURL.indexOf("rtmp://www.easydss.com/live");
        int result2 = defaultRtmpURL.indexOf("rtmp://121.40.50.44/live");
        if(result1 != -1 || result2 != -1){
            sharedPreferences.edit().putString(Config.SERVER_URL, Config.DEFAULT_SERVER_URL).apply();
        }
    }
    private void addLifecycleCallbacks(){
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                Log.e("onActivityCreated","onActivityCreated==="+activity.getClass().getName());
                mList.add(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                mList.remove(activity);
                Log.e("onActivityDestroyed","onActivityDestroyed==="+activity.getClass().getName());
            }
        });
    }
    public String getUrl() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defValue = Config.DEFAULT_SERVER_URL;
        String ip = sharedPreferences.getString(Config.SERVER_URL, defValue);
        if (ip.equals(defValue)){
            sharedPreferences.edit().putString(Config.SERVER_URL, defValue).apply();
        }
        return ip;
    }
    public void exit() {
        try {
            for (int i=0;i<mList.size();i++) {
                Activity activity=mList.get(i);
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

        }
    }
    public static MyApplication getInstance(){
        if(application==null){
            synchronized (MyApplication.class){
                if(application==null){
                    application=new MyApplication();
                }
            }
        }
        return application;
    }
    public void getContext() {
        applicationContext = getApplicationContext();
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
