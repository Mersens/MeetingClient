package svs.meeting.data;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import svs.meeting.app.R;
import svs.meeting.service.LocalService;
import svs.meeting.util.DBUtil;

public class Config {
    public static org.json.JSONObject appConfig=null;
    public static java.util.HashMap<String, String> contactsMap = new java.util.HashMap<String, String>();
    public static String LOCAL_HOST = "192.168.1.133";
    public static String WEB_HOST = LOCAL_HOST+":8888";
    public static final String updataPAK = "http://47.94.40.223/shopwork/shared/update/";
    public static final String WEB_URL = "http://"+WEB_HOST+"";
    public static String seat_no="10";
    public static int meetingId;
    public static final String VIDEOPUSH_NAME="screen_push";
    public static final String VIDEOPUSH_URL="rtmp://"+LOCAL_HOST+"/live/"+VIDEOPUSH_NAME;
    public static String STOREPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MettingFile"+File.separator;
    public static String CLIENT_IP="192.168.1.13";
    public static org.json.JSONObject meetingInfo=null;
    public static org.json.JSONObject signSetting=null;
    public static org.json.JSONObject clientInfo=null;
    public static org.json.JSONObject display_atts=null;
    public static int sign_statu=0;
    public static boolean isSignStatu=false;
    public static boolean isStartTimerService=false;
    public static boolean isAllowSignAgain=false;
    //APP内部铃声
    public static ArrayList<VoiceMode> voiceList = new ArrayList<>();
    private static String defaultSound=null;

    private static String TAG = Config.class.getSimpleName();
    //区别个人账户和企业账户标识
    public static String isChange = "0";//0：个人账户 110:企业账户
    //个人公司人员
    public static JSONArray deptUsers = new JSONArray();
    public static JSONObject unionObject = null;
    //标准债权、债务
    public static String ZBYE = "0.00";
    public static String BZZQ = "0.00";
    //个人任务项
    public static JSONArray personalTask = new JSONArray();
    //切换账户
    public static String myid;
    public static int power = 0;
    public static Map<String, Object> enterpriseMap = new HashMap<>();

    public static JSONObject deviceInfo = null;
    public static HashMap<String, HashMap<String, String>> localStore = new java.util.HashMap<String, HashMap<String, String>>();

    public static String getCurrentApp(){
        String appName="-";
        try{
            appName=appConfig==null?"-":appConfig.getString("name");
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return appName;
    }

    public static void setBottomNav(){
        DBUtil.putConfigVariable("local", "bottom_nav", "4");
        String deptId = DBUtil.getConfigVariable("local" , "deptId");

        String change_user = DBUtil.getConfigVariable("local", "change_user");
        try {
            JSONArray jsonArray = new JSONArray(change_user);
            for (int i = 0; i < jsonArray.length() ; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String aaa = jsonObject.getString("deptId");
                if (deptId.equals(aaa)){
                    if (jsonObject.toString().contains("md_account")){
                        DBUtil.putConfigVariable("local", "bottom_nav", "5");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 网络 请求通用参数集合
     * @return
     */
    public static java.util.HashMap<String, String> getParameters() {
        java.util.HashMap<String, String> params = new java.util.HashMap<String, String>();
        params.put("auth_client","svs");
        return params;
    }

    public static StringBuffer getParametersString() {
        StringBuffer sb = new StringBuffer();
        java.util.HashMap<String, String> m = getParameters();
        for (String k : m.keySet()) {
            if (sb.length() == 0)
                sb.append("?");
            else
                sb.append("&");
            sb.append(k + "=" + m.get(k));
        }
        return sb;
    }

    public static void setData(String key, String data) {
        String cate = "_global_";
        HashMap<String, String> m = null;
        if (Config.localStore.containsKey(cate))
            m = Config.localStore.get(cate);
        else {
            m = new HashMap<String, String>();
            Config.localStore.put(cate, m);
        }
        m.put(key, data);
    }

    public static String getData(String key) {
        String cate = "_global_";
        HashMap<String, String> m = null;
        if (!Config.localStore.containsKey(cate))
            return null;
        m = Config.localStore.get(cate);
        if (m.containsKey(key))
            return m.get(key);
        return "";
    }

    public static ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("CONFIG", "onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.e("CONFIG", "onServiceDisconnected");
        }
    };

    public static void setDeviceInfo(JSONObject devInfo) {
        deviceInfo = devInfo;
    }

    public static JSONObject getDeviceInfo() {
        return deviceInfo;
    }

    //发送广播， 更新web页面的部分数据
    public static void sendRefresh(Activity activity , String p , String c){
        //execJs("javascript:onAppEvent('UPLOAD_RESULT','" + destFile + "','true')");
        Intent intent = new Intent();
        intent.setAction(LocalService.ACTION);
        intent.putExtra("evid" , p);
        intent.putExtra("name" , c);
        activity.sendBroadcast(intent);
    }

    public static String getDefaultSound(){
        if(defaultSound!=null)
            return defaultSound;
        for (int i = 0; i < Config.voiceList.size(); i++) {
            VoiceMode mode = Config.voiceList.get(i);
            if(mode.isDefault()){
                defaultSound=mode.getName();
            }
        }
        return defaultSound;
    }
}
