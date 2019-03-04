package svs.meeting.fragments;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.google.gson.Gson;

import org.easydarwin.easypusher.BackgroundCameraService;
import org.easydarwin.easypusher.RecordService;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.activity.CalculatorActivity;
import svs.meeting.activity.CallServiceActivity;
import svs.meeting.activity.CheckResultActivity;
import svs.meeting.activity.ContactActivity;
import svs.meeting.activity.NotesActivity;
import svs.meeting.activity.PersonalPaletteActivity;
import svs.meeting.activity.ShowDesktopActivity;
import svs.meeting.activity.SignInShowActivity;
import svs.meeting.activity.StartVoteBallotActivity;
import svs.meeting.activity.VoteBallotActivity;
import svs.meeting.activity.VoteBallotDetailActivity;
import svs.meeting.app.BuildConfig;
import svs.meeting.app.FilesActivity;
import svs.meeting.app.LivePlayerDemoActivity;
import svs.meeting.app.MainActivity;
import svs.meeting.app.MyApplication;
import svs.meeting.app.R;
import svs.meeting.app.VoteBallotEntity;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.IntentType;
import svs.meeting.data.MsgType;
import svs.meeting.service.FloatMenuService;
import svs.meeting.service.MqttManagerV3;
import svs.meeting.util.Helper;
import svs.meeting.util.NotificationUtils;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.util.RxBus;
import svs.meeting.util.XLog;
import svs.meeting.widgets.DrawingOrderRelativeLayout;
import svs.meeting.widgets.MetroItemFrameLayout;
import svs.meeting.widgets.TipsDialogFragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.easydarwin.update.UpdateMgr.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

public class MainMenuClientFragment extends Fragment implements View.OnClickListener {
    public static boolean isPushScreen=false;
    private CompositeDisposable mCompositeDisposable;
    private int type=-1;
    public static final int REQUEST_OVERLAY_PERMISSION = 1004;
    public static final String KEY_ENABLE_BACKGROUND_CAMERA = "key_enable_background_camera";
    private static final int REQUEST_SCAN_TEXT_URL = 1003;
    static final String TAG = "EasyPusher";
    public static final int REQUEST_MEDIA_PROJECTION = 1002;
    public static final int REQUEST_CAMERA_PERMISSION = 1003;
    public static final int REQUEST_STORAGE_PERMISSION = 1004;
    private boolean mNeedGrantedPermission;
    private BackgroundCameraService mService;
    private ServiceConnection conn;
    public static Intent mResultIntent;
    public static int mResultCode;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_main_client,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initRxbus();
        if(Build.VERSION.SDK_INT >Build.VERSION_CODES.LOLLIPOP){
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION);
                mNeedGrantedPermission = true;
                return;
            } else {
                // resume..
            }
        }else {
            goonWithPermissionGranted();
        }
        type=getArguments().getInt("type",type);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showContent();
            }
        },1500);

    }
    private void goonWithPermissionGranted() {
        // create background service for background use.
        Intent intent = new Intent(getActivity(), BackgroundCameraService.class);
        getActivity().startService(intent);

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mService = ((BackgroundCameraService.LocalBinder) iBinder).getService();
//                mMediaStream = EasyApplication.sMS;

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        getActivity().bindService(new Intent(getActivity(), BackgroundCameraService.class), conn, 0);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                break;
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mNeedGrantedPermission = false;
                    goonWithPermissionGranted();

                } else {

                }
                break;
            }
        }
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
                            if (type.equals("stop_push")) {
                                Intent intent = new Intent(getActivity(), RecordService.class);
                                getActivity().stopService(intent);
                            }else if(type.equals(EventEntity.MQTT_MSG)){
                                EventEntity.MQEntity entity=e.getMqEntity();
                                String msg=entity.getMsgType();
          /*                      if(MsgType.MSG_SHARE.equals(msg)){
                                    String str=entity.getContent();
                                    if(str.contains(",")){
                                        String strs[]=str.split(",");
                                        if(strs[0].equals("START")){
                                            String name=strs[1];
                                            String url="rtmp://"+Config.LOCAL_HOST+"/live/"+name;
                                            Log.e("SCREEN_PUSH_url","URL=="+url);
                                            Bundle bundle=new Bundle();
                                            bundle.putString("playUrl",url);
                                            Helper.switchActivity(getActivity(), LivePlayerDemoActivity.class,bundle);
                                        }
                                    }
                                }*/ if(MsgType.MSG_VOTE.equals(msg)){
                                    String content=entity.getContent();
                                    JSONObject jsonObject=new JSONObject(content);
                                    String action=jsonObject.getString("action");
                                    if("start".equals(action)){
                                        JSONObject object=new JSONObject(jsonObject.getString("data"));
                                        VoteBallotEntity entity1=new VoteBallotEntity();
                                        entity1.setId(object.getString("id"));
                                        entity1.setStatus(object.getString("status"));
                                        entity1.setVote_mode(object.getString("vote_mode"));
                                        entity1.setVote_name(object.getString("vote_name"));
                                        entity1.setDuration(object.getString("duration"));
                                        entity1.setContent(object.getString("content"));
                                        entity1.setAtts(object.getString("atts"));
                                        entity1.setTotal_count(object.getString("total_count"));
                                        entity1.setSigned_count(object.getString("signed_count"));
                                        entity1.setMeeting_id(object.getString("meeting_id"));
                                        entity1.setSign_rate_fact(object.getString("sign_rate_fact"));
                                        Bundle bundle=new Bundle();
                                        bundle.putString("title",entity1.getVote_name());
                                        int time=Integer.parseInt(entity1.getDuration());
                                        bundle.putInt("time",time);
                                        bundle.putSerializable("voteballot",entity1);
                                        Helper.switchActivity(getActivity(), StartVoteBallotActivity.class,bundle);
                                    }
                                }else if(MsgType.MSG_RESPONSE.equals(msg)){
                                    String str=entity.getContent();
                                    JSONObject object=new JSONObject(str);
                                    String t=object.getString("type");
                                    String title=null;
                                    if("shareScreen_agree".equals(t)){
                                        title="同屏共享已同意！";
                                        onPushScreen();
                                    }else if("shareScreen_refuse".equals(t)){
                                        title="同屏共享被拒绝！";
                                    }else if("speaker_agree".equals(t)){
                                        title="申请发言已同意！";
                                    }else if("speaker_refuse".equals(t)){
                                        title="申请发言被拒绝！";
                                    }else if("leave_agree".equals(t)){
                                        title="申请离开已同意！";
                                        doLeave();
                                    }else if("leave_refuse".equals(t)){
                                        title="申请离开被拒绝！";
                                    }
                                    NotificationUtils notificationUtils = new NotificationUtils(getActivity(), null);
                                    notificationUtils.sendNotification(title, title);
                                }
                            }
                        }
                    }
                });
        //subscription交给compositeSubscription进行管理，防止内存溢出
        mCompositeDisposable.add(d);
    }

    private void doLeave(){
        try {
            String seat_no = Config.clientInfo.getString("tid");
            String id = Config.meetingInfo.getString("id");
            String ql="update logins set login_type='02' where seat_no='"+seat_no+"' and meeting_id="+id;
            Map<String, String> map = Config.getParameters();
            map.put("type", "hql");
            map.put("ql", ql);
            RequestManager.getInstance()
                    .mServiceStore
                    .setMeetingStatu(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                        @Override
                        public void onSuccess(String msg) {
                            Log.e("onSuccess", msg);
                            if(!TextUtils.isEmpty(msg)){
                                try {
                                    JSONObject json=new JSONObject(msg);
                                    if(json.getBoolean("success")){
                                        String split = "\\~^";
                                        MqttManagerV3 mqtt = MqttManagerV3.getInstance();
                                        JSONObject object = new JSONObject();
                                        object.put("action","leave");
                                        String message=object.toString();
                                        String seat_no = Config.clientInfo.getString("tid");
                                        String uname = Config.clientInfo.getString("name");
                                        String strMsg=uname+split+seat_no+split+MsgType.MSG_LOGIN+split +message+split+new Date().getTime()+split+Config.CLIENT_IP;
                                        mqtt.send(strMsg,"");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onError(String msg) {
                            Log.e("sendVoteInfo onError", msg);
                        }
                    }));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initViews(View view) {
        TextView txtTitle = (TextView) this.getView().findViewById(R.id.txtTitle);
        XLog.log("==" + Config.meetingInfo);
        LinearLayout contentPanel = (LinearLayout) this.getView().findViewById(R.id.contentPanel);
        LinearLayout topPanel = (LinearLayout) this.getView().findViewById(R.id.topPanel);
        LinearLayout bottomPanel = (LinearLayout) this.getView().findViewById(R.id.bottomPanel);
        //GridLayout gridContent=(GridLayout)this.getView().findViewById(R.id.contentGrid);
        contentPanel.measure(0, 0);
        //XLog.log("top 宽:"+contentPanel.getMeasuredWidth()+",高:"+contentPanel.getMeasuredHeight()+",SW="+ Helper.screenWidth+",SH="+Helper.screenHeight);

        int topHeight = Helper.screenHeight / 5;//(Helper.screenHeight-contentPanel.getMeasuredHeight())/2;
        LinearLayout.LayoutParams paramsTop = (LinearLayout.LayoutParams) topPanel.getLayoutParams();
        paramsTop.height = topHeight;//Helper.Px2Dp(this.getContext(),topHeight);
        int marginInPx = (Helper.screenWidth - Helper.Dp2Px(getContext(), contentPanel.getWidth())) / 2;
        paramsTop.rightMargin = paramsTop.leftMargin = Helper.Px2Dp(getContext(), marginInPx);
        topPanel.setLayoutParams(paramsTop);

        LinearLayout.LayoutParams paramsBottom = (LinearLayout.LayoutParams) bottomPanel.getLayoutParams();
        paramsBottom.height = topHeight * 2 / 4;
        bottomPanel.setLayoutParams(paramsBottom);

        try {
            txtTitle.setText(Config.meetingInfo.getString("name"));
            String strFont = Config.meetingInfo.getString("font");
            int fontSize = Config.meetingInfo.getInt("size");
            txtTitle.setTextSize(Helper.Px2Dp(this.getContext(), fontSize));
            String color=Config.meetingInfo.getString("color");
            if(!TextUtils.isEmpty(color)){
                if(!"null".equals(color)){
                    txtTitle.setTextColor(Color.parseColor("#"+color));
                }
            }
            //txtTitle.setTextColor(Color.parseColor("#"+Config.meetingInfo.getString("color")));

            String url = Config.WEB_URL + "/" + Config.meetingInfo.getString("logo");
            Glide.with(this).load(url)
                    .into(new ViewTarget<View, GlideDrawable>(view) {
                        //括号里为需要加载的控件
                        @Override
                        public void onResourceReady(GlideDrawable resource,
                                                    GlideAnimation<? super GlideDrawable> glideAnimation) {
                            view.setBackground(resource.getCurrent());
                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        initButtons();
    }
    private void initButtons() {
        DrawingOrderRelativeLayout layout = (DrawingOrderRelativeLayout) this.getActivity().findViewById(R.id.list);
        for (int i = 0; i < layout.getChildCount(); i++) {
            MetroItemFrameLayout metroItem = (MetroItemFrameLayout) layout.getChildAt(i);
            metroItem.setOnClickListener(this);
        }
    }

    public static MainMenuClientFragment getInstance(int type){
        MainMenuClientFragment fragment=new MainMenuClientFragment();
        Bundle bundle=new Bundle();
        bundle.putInt("type",type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view10://会议笔记
                Helper.switchActivity(this.getActivity(), NotesActivity.class);
                break;
            case R.id.view11://即时交流
                Helper.switchActivity(this.getActivity(), ContactActivity.class);
                break;
            case R.id.view://会议资料
                Helper.switchActivity(this.getActivity(), FilesActivity.class);
                break;
            case R.id.view4://申请发言
                showFYTipsView("确定向主持人申请发言？");
                break;
            case R.id.view2://申请离开
                showFKTipsView("确定向主持人申请离开？");
                break;
            case R.id.view3://申请同屏
                showTPTipsView("确定向主持人申请同屏？");
                break;
            case R.id.view5://电子白板
                Helper.switchActivity(this.getActivity(), PersonalPaletteActivity.class);
                break;
            case R.id.view6://外部视频
                Bundle bundle=new Bundle();
                bundle.putString("playUrl", Config.WEB_URL + "/upload/kda.mp4");
                Helper.switchActivity(this.getActivity(), LivePlayerDemoActivity.class,bundle);
                break;
            case R.id.view12://呼叫服务
                Helper.switchActivity(this.getActivity(), CallServiceActivity.class);
                break;
            case R.id.view13://桌牌显示
                Helper.switchActivity(this.getActivity(), ShowDesktopActivity.class);
                break;
            case R.id.view7://显示桌面
                Intent intentService=new Intent(getActivity(),FloatMenuService.class);
                intentService.putExtra("type",FloatMenuService.CLIENT_TYPE);
                getActivity().startService(intentService);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                break;
            case R.id.view8://退出系统
                showExitView("确定退出系统？");
                break;
        }

    }

    private void showFYTipsView(final String msg){
       final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getChildFragmentManager(),"showFYTipsView");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
                //申请发言
            }

            @Override
            public void onClickOk() {
                dialogFragment.dismissAllowingStateLoss();
                sendInfo("speaker");
            }
        });
    }

    private void showFKTipsView(final String msg){
        final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getChildFragmentManager(),"showFKTipsView");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
                //申请离开
            }

            @Override
            public void onClickOk() {
                dialogFragment.dismissAllowingStateLoss();
                sendInfo("leave");
            }
        });

    }

    private void showTPTipsView(final String msg){
        final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getChildFragmentManager(),"showTPTipsView");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void onClickOk() {
                dialogFragment.dismissAllowingStateLoss();
                //申请同屏
                sendInfo("shareScreen");
            }
        });
    }
    private void sendInfo(String type){
        try {
            String split = "\\~^";
            MqttManagerV3 mqtt = MqttManagerV3.getInstance();
            JSONObject object = new JSONObject();
            object.put("type",type);
            String message=object.toString();
            String seat_no = Config.clientInfo.getString("tid");
            String uname = Config.clientInfo.getString("name");
            String strMsg=uname+split+seat_no+split+MsgType.MSG_REQUEST+split +message+split+new Date().getTime()+split+Config.CLIENT_IP;
            mqtt.send(strMsg,"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showExitView(String msg){
        final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getChildFragmentManager(),"showExitView");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void onClickOk() {
                MyApplication.getInstance().exit();
                dialogFragment.dismissAllowingStateLoss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().stopService(new Intent(getActivity(),FloatMenuService.class));

    }
    private void showContent(){
        Log.e("showContent","showContent=="+type);
        switch (type){
            case IntentType.TPGX:
                if(!isPushScreen){
                    showTPTipsView("确定向主持人申请同屏？");
                }
                break;
            case IntentType.WBSP:
                Bundle bundle=new Bundle();
                bundle.putString("playUrl", Config.WEB_URL + "/upload/kda.mp4");
                Helper.switchActivity(this.getActivity(), LivePlayerDemoActivity.class,bundle);
                break;
            case IntentType.HYZL:
                Helper.switchActivity(this.getActivity(), FilesActivity.class);
                break;
            case IntentType.HJFW:
                Helper.switchActivity(this.getActivity(), CallServiceActivity.class);
                break;
            case IntentType.JSQ:
                Helper.switchActivity(this.getActivity(), CalculatorActivity.class);
                break;
            case IntentType.FHHY:
                break;
        }
    }
    private void showShareScreenView(String msg){
        final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getChildFragmentManager(),"showShareScreenView");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void onClickOk() {
                dialogFragment.dismissAllowingStateLoss();
                onPushScreen();
                //开启同屏
            }
        });
    }

    public void onPushScreen() {
        //Helper.switchActivity(this.getActivity(), StreamActivity.class);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            new AlertDialog.Builder(getActivity()).setMessage("推送屏幕需要安卓5.0以上,您当前系统版本过低,不支持该功能。").setTitle("抱歉").show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getActivity())) {
                new AlertDialog.Builder(getActivity()).setMessage("推送屏幕需要APP出现在顶部.是否确定?").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                    }
                }).setNegativeButton(android.R.string.cancel,null).setCancelable(false).show();
                return;
            }
        }

        if (RecordService.mEasyPusher != null) {
            Intent intent = new Intent(getActivity(), RecordService.class);
            getActivity().stopService(intent);

        } else {
            startScreenPushIntent();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                Log.e(TAG, "get capture permission success!");
                mResultCode = resultCode;
                mResultIntent = data;
                startScreenPushIntent();

            }
        }
    }

    private void startScreenPushIntent() {

        if (mResultIntent != null && mResultCode != 0) {
            Intent intent = new Intent(getActivity(), RecordService.class);
            getActivity().startService(intent);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaProjectionManager mMpMngr = (MediaProjectionManager) getActivity().getSystemService(MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mMpMngr.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            }
        }
    }
    @Override
    public void onDestroy() {
        if (!mNeedGrantedPermission) {
            getActivity().unbindService(conn);
        }
        mCompositeDisposable.clear();
        super.onDestroy();

    }

}
