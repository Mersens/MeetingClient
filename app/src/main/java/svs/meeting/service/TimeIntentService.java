package svs.meeting.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class TimeIntentService extends IntentService {

    public static onUpdateListener listener;
    private boolean isRunning;
    private int count;
    private int allCount;


    public TimeIntentService() {
        super("TimeIntentService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int time=intent.getIntExtra("time",0);
        allCount=time;//秒

        try {

            isRunning = true;
            count = 0;
            while (isRunning) {
                count++;
                if (count >= allCount) {
                    isRunning = false;
                }
                Thread.sleep(1000);
                if(listener!=null){
                    listener.onUpdate((allCount-count));
                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public interface onUpdateListener{
        void onUpdate(int time);
    }
    public static void setOnUpdateListener(onUpdateListener l){
        listener=l;

    }

}
