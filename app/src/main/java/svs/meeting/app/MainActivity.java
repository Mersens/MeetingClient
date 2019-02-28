package svs.meeting.app;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.promeg.pinyinhelper.Pinyin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import svs.meeting.activity.BaseActivity;
import svs.meeting.activity.CallServiceActivity;
import svs.meeting.activity.ChatActivity;
import svs.meeting.activity.PublicPaletteActivity;
import svs.meeting.activity.ServiceActivity;
import svs.meeting.activity.ShowDesktopActivity;
import svs.meeting.activity.SignInActivity;
import svs.meeting.activity.SignInShowActivity;
import svs.meeting.activity.VoteBallotDetailActivity;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.Friend;
import svs.meeting.data.MsgEntity;
import svs.meeting.data.MsgType;
import svs.meeting.data.User;
import svs.meeting.db.DBDao;
import svs.meeting.db.DBDaoImpl;
import svs.meeting.fragments.FirstFragment;
import svs.meeting.fragments.MainMenuClientFragment;
import svs.meeting.fragments.MainMenuFragment;
import svs.meeting.fragments.SecondFragment;
import svs.meeting.service.FloatMenuService;
import svs.meeting.service.LocalService;
import svs.meeting.service.MessageProcessor;
import svs.meeting.util.DBUtil;
import svs.meeting.util.Helper;
import svs.meeting.util.NotificationUtils;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.util.RxBus;
import svs.meeting.util.SharePreferenceUtil;
import svs.meeting.util.XLog;
import svs.meeting.widgets.TipsDialogFragment;

public class MainActivity extends BaseActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private CompositeDisposable mCompositeDisposable;
    private DBDao dao;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private String dev_type;
    private long timeLastPressed = 0;
    private LocalReceiver myReceiver = null;
    private ArrayList<Fragment> fragmentList = new ArrayList<>();
    private int[] _icons0 = new int[]{
            R.drawable.icon_union_d,
            R.drawable.icon_shop_d,
            R.drawable.icon_zqzw_d,
            R.drawable.icon_mine_d
    };
    private int[] _icons1 = new int[]{
            R.drawable.icon_union_e,
            R.drawable.icon_shop_e,
            R.drawable.icon_zqzw_e,
            R.drawable.icon_mine_e};
    private int[] _btns = new int[]{
            R.id.btn3,
            R.id.btn2,
            R.id.btn_message,
            R.id.btn4
    };
    private Button[] buttons = new Button[4];
    private int bottom_nav_num;
    private int type = -1;
    private String meeting_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dao = new DBDaoImpl(this);
        String info = SharePreferenceUtil.getInstance(this).getMeetingInfo();
        try {
            if (!TextUtils.isEmpty(info)) {
                JSONObject json = new JSONObject(info);
                Config.meetingInfo = new org.json.JSONObject(json.getString("meeting"));
                Config.clientInfo = json.getJSONObject("client");
                Config.signSetting = new org.json.JSONObject(json.getString("sign_setting"));
                Config.meetingId = Config.meetingInfo.getInt("id");
                Config.display_atts = Config.clientInfo.getJSONObject("display_atts");
                Config.myid = Config.clientInfo.getString("id");
                Config.isAllowSignAgain = Config.signSetting.getBoolean("allow_sign_again");
            }
            dev_type = Config.clientInfo.getString("dev_type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (getIntent().hasExtra("type")) {
            type = getIntent().getIntExtra("type", type);
        }
        try {
            meeting_id = Config.meetingInfo.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        bottom_nav_num = buttons.length;
        for (int i = 0; i < bottom_nav_num; i++) {
            buttons[i] = (Button) this.findViewById(_btns[i]);
            buttons[i].setOnClickListener(this);
        }
        if ("01".equals(dev_type)) {
            //主持端
            fragmentList.add(MainMenuFragment.getInstance(type));
        } else if ("02".equals(dev_type)) {
            //参会端
            fragmentList.add(MainMenuClientFragment.getInstance(type));
        }

        fragmentList.add(new FirstFragment());
        fragmentList.add(new SecondFragment());
        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        this.showFragment(0);
        doQuery();
        DBUtil.putConfigVariable("local", "server", Config.LOCAL_HOST);
        DBUtil.putConfigVariable("local", "port", "1883");
        DBUtil.putConfigVariable("local", "user", "sytem");
        DBUtil.putConfigVariable("local", "password", "manager");
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, LocalService.class);
                bindService(intent, Config.connection, BIND_AUTO_CREATE);
            }
        }, 1500);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalService.ACTION);
        myReceiver = new LocalReceiver();
        registerReceiver(myReceiver, intentFilter);
        MessageProcessor.getInstance().updateChatlogs();
        //Glide.with(activity).load(splash).into(startImage);

        /*
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Helper.switchActivity(MainActivity.this,LoginActivity.class);
            }
        }, 500);
        */

        /*
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this)) {
            XLog.warn("初始化 vitamio 失败!",MainActivity.class);
            return;
        }*/

        String[] requestPerms = {Manifest.permission.WRITE_EXTERNAL_STORAGE
                , "android.permission.CAMERA"
                , "android.permission.RECORD_AUDIO"
        };
        requestPerms(requestPerms, 100, "请求权限，否则无法正常运行");
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
                            if (type.equals(EventEntity.MQTT_MSG)) {
                                EventEntity.MQEntity mqEntity = e.getMqEntity();
                                if (MsgType.MSG_CHAT.equals(mqEntity.getMsgType()) || MsgType.MSG_SERVICE.equals(mqEntity.getMsgType())) {
                                    String name = Config.clientInfo.getString("name");
                                    Log.e("name", "name==" + name);
                                    String content = mqEntity.getContent();
                                    String strs[] = content.split(";");
                                    String uname = strs[1];
                                    Log.e("uname", "uname==" + uname);
                                    if (name.equals(uname)) {
                                        return;
                                    }
                                    String seat_no = strs[2];
                                    String time = strs[3];
                                    String ip = strs[4];
                                    String c = strs[0];
                                    JSONObject object = new JSONObject(c);
                                    String str = object.getString("strContent");
                                    MsgEntity entity = new MsgEntity();
                                    entity.setPid(seat_no);
                                    entity.setMsg_time(getNowTime());
                                    if (MsgType.MSG_SERVICE.equals(mqEntity.getMsgType())) {
                                        entity.setMsg_type(MsgType.MSG_SERVICE);
                                    } else if (MsgType.MSG_CHAT.equals(mqEntity.getMsgType())) {
                                        entity.setMsg_type(MsgType.MSG_CHAT);
                                    }

                                    entity.setMsg(str);
                                    entity.setTopic(mqEntity.getTopic());
                                    entity.setFrom_name(uname);
                                    entity.setFrom_seat(seat_no);
                                    entity.setMeeting_id(meeting_id);
                                    entity.setSid(time);
                                    entity.setOid("");
                                    entity.setType(2);
                                    dao.addMsgInfo(entity);
                                    Friend friend = new Friend();
                                    friend.setIp_addr(ip);
                                    friend.setUname(uname);
                                    friend.setSeat_no(seat_no);
                                    User user = new User();
                                    user.setUsername(uname);
                                    friend.setFriendUser(user);
                                    String pinyin = Pinyin.toPinyin(uname.charAt(0));
                                    friend.setPinyin(pinyin.substring(0, 1).toUpperCase());
                                    if (MsgType.MSG_SERVICE.equals(mqEntity.getMsgType())) {
                                        Intent intent = new Intent(MainActivity.this, CallServiceActivity.class);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, -1, intent
                                                , PendingIntent.FLAG_UPDATE_CURRENT);
                                        NotificationUtils notificationUtils = new NotificationUtils(MainActivity.this, pendingIntent);
                                        notificationUtils.sendNotification("通知消息", uname);
                                    } else if (MsgType.MSG_CHAT.equals(mqEntity.getMsgType())) {
                                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                        intent.putExtra("friend", friend);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, -1, intent
                                                , PendingIntent.FLAG_UPDATE_CURRENT);
                                        NotificationUtils notificationUtils = new NotificationUtils(MainActivity.this, pendingIntent);
                                        notificationUtils.sendNotification("通知消息", uname);
                                    }

                                }else if(MsgType.MSG_LOGIN.equals(mqEntity.getMsgType())){
                                    String str=mqEntity.getContent();
                                    Log.e("MSG_LOGIN","MSG_LOGIN=="+str);
                                    JSONObject object=new JSONObject(str);
                                    if(object.has("action")){
                                        String action=object.getString("action");
                                        if("start".equals(action)){
                                            if(Config.sign_statu==0){
                                                Helper.switchActivity(MainActivity.this, ShowDesktopActivity.class);
                                            }
                                        }else if("end".equals(action)){
                                            Config.isStartTimerService=true;
                                            Bundle bundle=new Bundle();
                                            bundle.putInt("type",1);
                                            Helper.switchActivity(MainActivity.this, VoteBallotDetailActivity.class,bundle);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
        //subscription交给compositeSubscription进行管理，防止内存溢出
        mCompositeDisposable.add(d);
    }

    private String getNowTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private void analysisJson(JSONObject object) throws JSONException {
        if (object.has("action")) {
            String action = object.getString("action");
            if ("start_04".equals(action)) {
                //跳转到签到界面
                Helper.switchActivity(MainActivity.this, SignInActivity.class);
            }
        } else if (object.has("text")) {
            String msg = object.getString("text");
            showTipsView(msg);
        }
    }

    private void showTipsView(String msg) {
        final TipsDialogFragment tipsDialogFragment = TipsDialogFragment.getInstance(msg);
        tipsDialogFragment.show(getSupportFragmentManager(), "showTipsView");
        tipsDialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                tipsDialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void onClickOk() {
                tipsDialogFragment.dismissAllowingStateLoss();
            }
        });


    }

    private void setActiveButton(Button button) {
        for (int i = 0; i < bottom_nav_num; i++) {
            Button btn = buttons[i];
            if (btn.equals(button)) {
                btn.setEnabled(false);
                btn.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(_icons1[i]), null, null);
                btn.setTextColor(getResources().getColor(R.color.clr_theme_text_selected));
            } else {
                btn.setEnabled(true);
                btn.setTextColor(Color.parseColor("#ff666666"));
                btn.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(_icons0[i]), null, null);
            }
        }


    }

    private void doQuery() {
        try {
            String id = Config.meetingInfo.getString("id");
            String seat_no = Config.clientInfo.getString("tid");
            String sql = "select * from logins where login_type<>'02' and seat_no='" + seat_no + "' and meeting_id=" + id;
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
                            if (!TextUtils.isEmpty(msg)) {
                                try {
                                    JSONObject json = new JSONObject(msg);
                                    if (json.getBoolean("success")) {
                                        JSONArray array = new JSONArray(json.getString("rows"));
                                        Config.sign_statu = array.length();
                                        if (Config.sign_statu > 0) {
                                            if (!Config.isStartTimerService) {
                                                if ("01".equals(dev_type)) {
                                                    Helper.switchActivity(MainActivity.this, SignInShowActivity.class);
                                                }
                                            }
                                        } else {
                                            Helper.switchActivity(MainActivity.this, ShowDesktopActivity.class);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(String msg) {
                            Log.e("doQuery onError", msg);
                        }
                    }));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        Button btn = (Button) view;
        this.setActiveButton(btn);

        switch (view.getId()) {
            case R.id.btn3:
                //viewPager.setCurrentItem(0);
                showFragment(0);
                break;
            case R.id.btn2:
                //viewPager.setCurrentItem(1);
                showFragment(1);
                break;
            case R.id.btn_message:
                //viewPager.setCurrentItem(2);
                break;
            case R.id.btn4:
                //viewPager.setCurrentItem(3);
                break;
            default:
                break;
        }
    }

    private void showFragment(int idx) {
        if (idx > fragmentList.size() - 1) {
            Toast.makeText(this, "目标不存在!", Toast.LENGTH_LONG);
            return;
        }

        FragmentManager fm = this.getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.replace(R.id.content, fragmentList.get(idx));
        transaction.commit();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!action.equals(LocalService.ACTION))
                return;
            int msgid = intent.getIntExtra("msgid", 0);
            if (msgid == MessageProcessor.ID_MSG_EVENT) {
                //Helper.playAudio(MainActivity.this, R.raw.bell0, false);
            } else if (msgid == MessageProcessor.ID_LOGS_LOADED || msgid == MessageProcessor.ID_LOGS_HAS_READ
                    || msgid == MessageProcessor.ID_MSG_ARRIVED) {

            }
        }
    }


    @Override
    public void onBackPressed() {
        long period = System.nanoTime() - timeLastPressed;
        if (TimeUnit.NANOSECONDS.toSeconds(period) > 3) {
            Toast.makeText(getApplicationContext(), "再按一次退出应用",
                    Toast.LENGTH_SHORT).show();
            timeLastPressed = System.nanoTime();
            return;
        }
        MyApplication.getInstance().exit();
    }

    @Override
    protected void onDestroy() {
        try {
            if (myReceiver != null) {
                this.unregisterReceiver(myReceiver);
                this.myReceiver = null;
            }
            unbindService(Config.connection);
        } catch (Exception ex) {
            XLog.error(ex.toString(), MainActivity.class);
        }
        // mCompositeDisposable.clear();
        super.onDestroy();
    }

    //--------------------------------------申请权限模块-------------------------------------

    /**
     * 动态请求权限
     *
     * @param perms   权限数组
     * @param message 提示语 例如：请授予[读写]权限，否则无法正常升级
     */
    public void requestPerms(String[] perms, int requestCode, String message) {
        if (perms.length == 0) {
            return;
        }
        if (EasyPermissions.hasPermissions(this, perms)) {
            onRequestPermissionsResult(true, requestCode);
        } else {
            EasyPermissions.requestPermissions(this, message, requestCode, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        onRequestPermissionsResult(true, requestCode);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        onRequestPermissionsResult(false, requestCode);
    }

    /**
     * 申请权限结果
     *
     * @param result true：成功申请权限
     *               false:申请权限失败
     */
    public void onRequestPermissionsResult(boolean result, int requestCode) {

    }

}
