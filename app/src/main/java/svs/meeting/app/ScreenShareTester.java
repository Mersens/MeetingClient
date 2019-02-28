package svs.meeting.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import svs.meeting.service.ScreenShotService;
import svs.meeting.util.Helper;
import svs.meeting.util.XLog;


public class ScreenShareTester extends AppCompatActivity {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_share_tester);
        mMediaProjectionManager = (MediaProjectionManager)getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startIntent();
    }

    private int result = 0;
    private Intent intent = null;
    private int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mMediaProjectionManager;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startIntent(){
        if(intent != null && result != 0){
            ((MyApplication)getApplication()).setResult(result);
            ((MyApplication)getApplication()).setIntent(intent);
            Intent intent = new Intent(getApplicationContext(), ScreenShotService.class);
            startService(intent);
        }else{
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            ((MyApplication)getApplication()).setMediaProjectionManager(mMediaProjectionManager);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }else if(data != null && resultCode != 0){
                result = resultCode;
                intent = data;
                ((MyApplication)getApplication()).setResult(resultCode);
                ((MyApplication)getApplication()).setIntent(data);
                Intent intent = new Intent(getApplicationContext(), ScreenShotService.class);
                startService(intent);

                finish();
            }
        }
    }
}
