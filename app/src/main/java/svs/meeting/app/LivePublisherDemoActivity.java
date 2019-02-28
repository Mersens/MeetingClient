package svs.meeting.app;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import cn.nodemedia.NodeCameraView;
import cn.nodemedia.NodePublisher;
import cn.nodemedia.NodePublisherDelegate;
import cn.nodemedia.NodeRecorderView;
import svs.meeting.util.SharedPreUtil;

/**
 * 直播推流实例
 */
public class LivePublisherDemoActivity extends AppCompatActivity implements View.OnClickListener, NodePublisherDelegate {
    private NodeRecorderView npv;
    private Button micBtn, swtBtn, videoBtn, flashBtn, camBtn;
    private SeekBar mLevelSB;
    private boolean isStarting = false;
    private boolean isMicOn = true;
    private boolean isCamOn = true;
    private boolean isFlsOn = true;

    private Button capBtn;
    private NodePublisher np;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_publisher_demo);
        isStarting = false;
        npv = (NodeRecorderView) findViewById(R.id.camera_preview);
        micBtn = (Button) findViewById(R.id.button_mic);
        swtBtn = (Button) findViewById(R.id.button_sw);
        videoBtn = (Button) findViewById(R.id.button_video);
        flashBtn = (Button) findViewById(R.id.button_flash);
        camBtn = (Button) findViewById(R.id.button_cam);
        capBtn = (Button) findViewById(R.id.pub_cap_button);
        mLevelSB = (SeekBar) findViewById(R.id.pub_level_seekBar);

        micBtn.setOnClickListener(this);
        swtBtn.setOnClickListener(this);
        videoBtn.setOnClickListener(this);
        flashBtn.setOnClickListener(this);
        camBtn.setOnClickListener(this);
        capBtn.setOnClickListener(this);

        mLevelSB.setMax(5);
        mLevelSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                np.setBeautyLevel(progress);
            }
        });

        String pubUrl = SharedPreUtil.getString(this, "pubUrl");

        np = new NodePublisher(this);
        np.setNodePublisherDelegate(this);
        np.setCameraPreview(npv, NodePublisher.CAMERA_FRONT, true);
        np.setAudioParam(32 * 1000, NodePublisher.AUDIO_PROFILE_HEAAC);
        np.setVideoParam(NodePublisher.VIDEO_PPRESET_16X9_360, 24, 500 * 1000, NodePublisher.VIDEO_PROFILE_MAIN, false);
        np.setDenoiseEnable(true);
        np.setBeautyLevel(3);
        np.setOutputUrl(pubUrl);
        /**
         * @brief rtmpdump 风格的connect参数
         * Append arbitrary AMF data to the Connect message. The type must be B for Boolean, N for number, S for string, O for object, or Z for null.
         * For Booleans the data must be either 0 or 1 for FALSE or TRUE, respectively. Likewise for Objects the data must be 0 or 1 to end or begin an object, respectively.
         * Data items in subobjects may be named, by prefixing the type with 'N' and specifying the name before the value, e.g. NB:myFlag:1.
         * This option may be used multiple times to construct arbitrary AMF sequences. E.g.
         */
        np.setConnArgs("S:info O:1 NS:uid:10012 NB:vip:1 NN:num:209.12 O:0");
        np.startPreview();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        np.stopPreview();
        np.stop();
        np.release();
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.pub_cap_button:
                np.capturePicture(new NodePublisher.CapturePictureListener() {
                    @Override
                    public void onCaptureCallback(Bitmap bitmap) {
                        if (bitmap == null) {
                            handler.sendEmptyMessage(2103);
                            return;
                        }
                        try {
                            String capFilePath = Environment.getExternalStorageDirectory().getPath() + "/pub_cap_" + System.currentTimeMillis() + ".jpg";
                            File SavePath = new File(capFilePath);
                            FileOutputStream out = new FileOutputStream(SavePath);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                            out.close();
                            bitmap.recycle();
                            handler.sendEmptyMessage(2102);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                break;
            case R.id.button_mic:
                if (isStarting) {
                    isMicOn = !isMicOn;
                    np.setAudioEnable(isMicOn);
                    if (isMicOn) {
                        handler.sendEmptyMessage(3101);
                    } else {
                        handler.sendEmptyMessage(3100);
                    }
                }
                break;
            case R.id.button_sw:
                np.switchCamera();
                np.setFlashEnable(false);
                isFlsOn = false;
                flashBtn.setBackgroundResource(R.drawable.ic_flash_off);
                break;
            case R.id.button_video:
                if (isStarting) {
                    np.stop();
                } else {
                    int ret = np.start();
                    Log.e("NP","start ret :" +ret);

                }
                break;
            case R.id.button_flash:
                int ret;
                if (isFlsOn) {
                    ret = np.setFlashEnable(false);
                } else {
                    ret = np.setFlashEnable(true);
                }
                if (ret == -1) {
                    // 无闪光灯,或处于前置摄像头,不支持闪光灯操作
                } else if (ret == 0) {
                    // 闪光灯被关闭
                    flashBtn.setBackgroundResource(R.drawable.ic_flash_off);
                    isFlsOn = false;
                } else {
                    // 闪光灯被打开
                    flashBtn.setBackgroundResource(R.drawable.ic_flash_on);
                    isFlsOn = true;
                }
                break;
            case R.id.button_cam:
                if (isStarting) {
                    isCamOn = !isCamOn;
                    np.setVideoEnable(isCamOn);
                    if (isCamOn) {
                        handler.sendEmptyMessage(3103);
                    } else {
                        handler.sendEmptyMessage(3102);
                    }
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onEventCallback(NodePublisher nodePublisher, int event, String msg) {
        Log.i("NodeMedia.NodePublisher","EventCallback:"+event+" msg:"+msg);
        handler.sendEmptyMessage(event);
    }

    private Handler handler = new Handler() {
        // 回调处理
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2000:
                    Toast.makeText(LivePublisherDemoActivity.this, "正在发布视频", Toast.LENGTH_SHORT).show();
                    break;
                case 2001:
                    Toast.makeText(LivePublisherDemoActivity.this, "视频发布成功", Toast.LENGTH_SHORT).show();
                    videoBtn.setBackgroundResource(R.drawable.ic_video_start);
                    isStarting = true;
                    break;
                case 2002:
                    Toast.makeText(LivePublisherDemoActivity.this, "视频发布失败", Toast.LENGTH_SHORT).show();
                    break;
                case 2004:
                    Toast.makeText(LivePublisherDemoActivity.this, "视频发布结束", Toast.LENGTH_SHORT).show();
                    videoBtn.setBackgroundResource(R.drawable.ic_video_stop);
                    isStarting = false;
                    break;
                case 2005:
                    Toast.makeText(LivePublisherDemoActivity.this, "网络异常,发布中断", Toast.LENGTH_SHORT).show();
                    break;
                case 2100:
                    // 发布端网络阻塞，已缓冲了2秒的数据在队列中
                    Toast.makeText(LivePublisherDemoActivity.this, "网络阻塞，发布卡顿", Toast.LENGTH_SHORT).show();
                    break;
                case 2101:
                    // 发布端网络恢复畅通
                    Toast.makeText(LivePublisherDemoActivity.this, "网络恢复，发布流畅", Toast.LENGTH_SHORT).show();
                    break;
                case 2102:
                    Toast.makeText(LivePublisherDemoActivity.this, "截图保存成功", Toast.LENGTH_SHORT).show();
                    break;
                case 2103:
                    Toast.makeText(LivePublisherDemoActivity.this, "截图保存失败", Toast.LENGTH_SHORT).show();
                    break;
                case 2104:
                    Toast.makeText(LivePublisherDemoActivity.this, "网络阻塞严重,无法继续推流,断开连接", Toast.LENGTH_SHORT).show();
                    break;
                case 2300:
                    Toast.makeText(LivePublisherDemoActivity.this, "摄像头和麦克风都不能打开,用户没有给予访问权限或硬件被占用", Toast.LENGTH_SHORT).show();
                    break;
                case 2301:
                    Toast.makeText(LivePublisherDemoActivity.this, "麦克风无法打开", Toast.LENGTH_SHORT).show();
                    break;
                case 2302:
                    Toast.makeText(LivePublisherDemoActivity.this, "摄像头无法打开", Toast.LENGTH_SHORT).show();
                    break;
                case 3100:
                    // mic off
                    micBtn.setBackgroundResource(R.drawable.ic_mic_off);
                    Toast.makeText(LivePublisherDemoActivity.this, "麦克风静音", Toast.LENGTH_SHORT).show();
                    break;
                case 3101:
                    // mic on
                    micBtn.setBackgroundResource(R.drawable.ic_mic_on);
                    Toast.makeText(LivePublisherDemoActivity.this, "麦克风恢复", Toast.LENGTH_SHORT).show();
                    break;
                case 3102:
                    // camera off
                    camBtn.setBackgroundResource(R.drawable.ic_cam_off);
                    Toast.makeText(LivePublisherDemoActivity.this, "摄像头传输关闭", Toast.LENGTH_SHORT).show();
                    break;
                case 3103:
                    // camera on
                    camBtn.setBackgroundResource(R.drawable.ic_cam_on);
                    Toast.makeText(LivePublisherDemoActivity.this, "摄像头传输打开", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
}
