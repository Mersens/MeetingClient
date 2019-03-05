package svs.meeting.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.easydarwin.easypusher.RecordService;
import org.json.JSONObject;

import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.activity.BaseActivity;
import svs.meeting.activity.StartVoteBallotActivity;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.MsgType;
import svs.meeting.util.Helper;
import svs.meeting.util.RxBus;
import svs.meeting.util.SharedPreUtil;
import svs.meeting.widgets.TipsDialogFragment;

/**
 * 直播播放示例
 */
public class LivePlayerDemoActivity extends BaseActivity implements NodePlayerDelegate {
    private CompositeDisposable mCompositeDisposable;
   private NodePlayer np;
   private String url;
   private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_player_demo);
        if(getIntent().hasExtra("playUrl")){
            url=getIntent().getStringExtra("playUrl");
        }
        if(TextUtils.isEmpty(url)){

            return;
        }

        initNodePlayer();
        initActionBar();
        initRxbus();
    }

    private void initRxbus() {
        mCompositeDisposable = new CompositeDisposable();
        //监听订阅事件
        Disposable d = RxBus.getInstance().toObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (o instanceof EventEntity) {
                            EventEntity e = (EventEntity) o;
                            String type = e.type;
                            String v = e.value;
                          if(type.equals(EventEntity.MQTT_MSG)){
                                EventEntity.MQEntity entity=e.getMqEntity();
                                String msg=entity.getMsgType();
                                if(MsgType.MSG_SHARE.equals(msg)){
                                    String str=entity.getContent();
                                    if(str.equals("STOP")){
                                            np.stop();
                                            np.release();
                                            finish();
                                    }
                                }
                            }
                        }
                    }
                });
        //subscription交给compositeSubscription进行管理，防止内存溢出
        mCompositeDisposable.add(d);
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("外部视频");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initNodePlayer() {
        //创建NodePlayer实例
        np = new NodePlayer(this);

        //查询播放视图
        NodePlayerView npv = (NodePlayerView)findViewById(R.id.live_player_view);
        //设置播放视图的渲染器模式,可以使用SurfaceView或TextureView. 默认SurfaceView
        npv.setRenderType(NodePlayerView.RenderType.SURFACEVIEW);
        //设置视图的内容缩放模式
        npv.setUIViewContentMode(NodePlayerView.UIViewContentMode.ScaleToFill);


        //将播放视图绑定到播放器
        np.setPlayerView(npv);

        //设置事件回调代理
        np.setNodePlayerDelegate(this);

        //开启硬件解码,支持4.3以上系统,初始化失败自动切为软件解码,默认开启.
        np.setHWEnable(true);

        /**
         * 设置启动缓冲区时长,单位毫秒.此参数关系视频流连接成功开始获取数据后缓冲区存在多少毫秒后开始播放
         */
        np.setBufferTime(1000);

        /**
         * 设置最大缓冲区时长,单位毫秒.此参数关系视频最大缓冲时长.
         * RTMP基于TCP协议不丢包,网络抖动且缓冲区播完,之后仍然会接受到抖动期的过期数据包.
         * 设置改参数,sdk内部会自动清理超出部分的数据包以保证不会存在累计延迟,始终与直播时间线保持最大maxBufferTime的延迟
         */
        np.setMaxBufferTime(3000);

        /**
         * 设置连接超时时长,单位毫秒.默认为0 一直等待.
         * 连接部分RTMP服务器,握手并连接成功后,当播放一个不存在的流地址时,会一直等待下去.
         * 如需超时,设置该值.超时后返回1006状态码.
         */
//        np.setConnectWaitTimeout(10*1000);

        /**
         * @brief rtmpdump 风格的connect参数
         * Append arbitrary AMF data to the Connect message. The type must be B for Boolean, N for number, S for string, O for object, or Z for null.
         * For Booleans the data must be either 0 or 1 for FALSE or TRUE, respectively. Likewise for Objects the data must be 0 or 1 to end or begin an object, respectively.
         * Data items in subobjects may be named, by prefixing the type with 'N' and specifying the name before the value, e.g. NB:myFlag:1.
         * This option may be used multiple times to construct arbitrary AMF sequences. E.g.
         */
//        np.setConnArgs("S:info O:1 NS:uid:10012 NB:vip:1 NN:num:209.12 O:0");


        /**
         * 设置RTSP使用TCP传输模式
         * 支持的模式有:
         * NodePlayer.RTSP_TRANSPORT_UDP
         * NodePlayer.RTSP_TRANSPORT_TCP
         * NodePlayer.RTSP_TRANSPORT_UDP_MULTICAST
         * NodePlayer.RTSP_TRANSPORT_HTTP
         */
//        np.setRtspTransport(NodePlayer.RTSP_TRANSPORT_TCP);

        /**
         * 设置播放直播视频url
         */
        np.setInputUrl(url);


        /**
         * 在本地开起一个RTMP服务,并进行监听播放,局域网内其他手机或串流器能推流到手机上直接进行播放,无需中心服务器支持
         * 播放的ip可以是本机IP,也可以是0.0.0.0,但不能用127.0.0.1
         * app/stream 可加可不加,只要双方匹配就行
         */
//        np.setLocalRTMP(true);


        /**
         * 开始播放直播视频
         */
        np.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 停止播放
         */
        np.stop();

        /**
         * 释放资源
         */
        np.release();
        mCompositeDisposable.clear();
    }


    /**
     * 事件回调
     * @param nodePlayer 对象
     * @param event 事件状态码
     * @param msg   事件描述
     */
    @Override
    public void onEventCallback(NodePlayer nodePlayer, int event, String msg) {
        Log.i("NodeMedia.NodePlayer","onEventCallback:"+event+" msg:"+msg);

        switch (event) {
            case 1000:
                // 正在连接视频
                break;
            case 1001:
                // 视频连接成功
                break;
            case 1002:
                // 视频连接失败 流地址不存在，或者本地网络无法和服务端通信，回调这里。5秒后重连， 可停止
//                nodePlayer.stopPlay();
                break;
            case 1003:
                // 视频开始重连,自动重连总开关
//                nodePlayer.stopPlay();
                break;
            case 1004:
                // 视频播放结束
                break;
            case 1005:
                // 网络异常,播放中断,播放中途网络异常，回调这里。1秒后重连，如不需要，可停止
//                nodePlayer.stopPlay();
                break;
            case 1006:
                //RTMP连接播放超时
                break;
            case 1100:
                // 播放缓冲区为空
//				System.out.println("NetStream.Buffer.Empty");
                break;
            case 1101:
                // 播放缓冲区正在缓冲数据,但还没达到设定的bufferTime时长
//				System.out.println("NetStream.Buffer.Buffering");
                break;
            case 1102:
                // 播放缓冲区达到bufferTime时长,开始播放.
                // 如果视频关键帧间隔比bufferTime长,并且服务端没有在缓冲区间内返回视频关键帧,会先开始播放音频.直到视频关键帧到来开始显示画面.
//				System.out.println("NetStream.Buffer.Full");
                break;
            case 1103:
//				System.out.println("Stream EOF");
                // 客户端明确收到服务端发送来的 StreamEOF 和 NetStream.Play.UnpublishNotify时回调这里
                // 注意:不是所有云cdn会发送该指令,使用前请先测试
                // 收到本事件，说明：此流的发布者明确停止了发布，或者因其网络异常，被服务端明确关闭了流
                // 本sdk仍然会继续在1秒后重连，如不需要，可停止
//                nodePlayer.stopPlay();
                break;
            case 1104:
                //解码后得到的视频高宽值 格式为:{width}x{height}
                break;
            default:
                break;
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
            showTipsView("确定退出吗？");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showTipsView(String msg){
        final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getSupportFragmentManager(),"exitLive");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void onClickOk() {
                dialogFragment.dismissAllowingStateLoss();
                finish();
            }
        });
    }



}
