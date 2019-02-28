package svs.meeting.util;

import android.os.CountDownTimer;
import android.os.Handler;

import svs.meeting.data.EventEntity;

public class Timer extends CountDownTimer {
    public static final int IN_RUNNING = 1001;
    public static int END_RUNNING = 1002;
    private static Handler mHandler;

    public Timer(long millisInFuture, long countDownInterval,
                 Handler handler) {
        super(millisInFuture, countDownInterval);
        mHandler = handler;
    }


    @Override
    public void onFinish() {
        // TODO Auto-generated method stub

        RxBus.getInstance().send(new EventEntity("finish",""));
    }

    @Override
    public void onTick(long millisUntilFinished) {
        // TODO Auto-generated method stub
        RxBus.getInstance().send(new EventEntity("time",(millisUntilFinished / 1000)+""));

    }

}
