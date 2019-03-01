package svs.meeting.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.activity.DisplayActivity;
import svs.meeting.activity.ServiceActivity;
import svs.meeting.activity.ShowDesktopActivity;
import svs.meeting.activity.SignInActivity;
import svs.meeting.activity.SignInShowActivity;
import svs.meeting.activity.VoteBallotActivity;
import svs.meeting.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import svs.meeting.data.Config;
import svs.meeting.data.Device;
import svs.meeting.util.DBUtil;
import svs.meeting.util.Helper;
import svs.meeting.util.HttpUtil;
import svs.meeting.util.IHttpCallback;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.util.SharePreferenceUtil;
import svs.meeting.util.XLog;


/**
 * 启动页
 *
 */
public class SplashActivity extends Activity {

	private boolean loadComplete = false;
	private boolean loadError=false;
	protected boolean _active = true;
    protected int _splashTime = 10;
    private static boolean isRunnging=true;
    private ActHandler actHandler=new ActHandler();
    private static final String TAG=SplashActivity.class.getName();

    private Activity activity;
	private long startTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isTaskRoot()) {
			finish();
			return;
		}
		setContentView(R.layout.activity_splash);
		activity = SplashActivity.this;

		initView();

		//此处进行初始化


		this.getSettings();
		//初始化完毕
		//loadComplete = true;

	}



	private void getSettings(){
		java.util.HashMap<String, String> params = Config.getParameters();
		String url = "/get_settings";
		params.put("ip",Config.CLIENT_IP);
		HttpUtil.requestURL(Config.WEB_URL + url, params, new IHttpCallback() {
			@Override
			public void onHttpComplete(int code, String result) {
				// TODO Auto-generated method stub
				//XLog.warn("返回信息("+(code==HttpUtil.HTTP_OK)+"):"+result,SplashActivity.class);
				actHandler.sendMessage(actHandler.obtainMessage(HttpUtil.HTTP_OK, 0, 1, result));
			}
		},"POST");
	}

	private void initView() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
		Helper.initScreenParams(this);//获取屏幕的宽高
		startTime = System.nanoTime();
		new SplashTread().start();
		new TimerThread().start();
	}


	public class SplashTread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				JSONObject devInfo=new Device().getInfo(activity);
				Config.setDeviceInfo(devInfo);

				DBUtil.initDatabase(false, activity);

				int waited = 0;
				while(_active && (waited < _splashTime) ) {
					sleep(100);
					if(_active) {
						waited += 100;
					}
				}
				while(!loadComplete){
					sleep(100);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			} finally {
				if(loadError){
					actHandler.sendMessage(actHandler.obtainMessage(0, 0, 1, "load error!"));
					return;
				}
				try{
					isRunnging=false;
					String mobile=DBUtil.getConfigVariable("local", "mobile");
					String token=DBUtil.getConfigVariable("local", "token");
					if(mobile.length()>0 && token.length()>0){
						Log.i(TAG,"使用上一次登录信息进行登录.");
						doLogin(mobile,token);
						return;
					}
					Log.i(TAG,"进入登录界面...");
					Thread.sleep(500);
					String dev_type=Config.clientInfo.getString("dev_type");
					if("03".equals(dev_type)){
						Helper.switchActivity(SplashActivity.this,ServiceActivity.class);
						SplashActivity.this.finish();
						return;
					}else if("04".equals(dev_type)){
						Helper.switchActivity(SplashActivity.this,DisplayActivity.class);
						SplashActivity.this.finish();
					}else {
						Helper.switchActivity(SplashActivity.this, MainActivity.class);
						finish();
					}

                }catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
	}

	public class TimerThread extends Thread {
		@Override
		public void run() {
			super.run();
			while(isRunnging){
				try{
					Thread.sleep(500);
				}catch(Exception ex){
				}

				if(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-startTime)>10){
					XLog.warn("登录超时",SplashActivity.class);
					isRunnging=false;
					actHandler.sendMessage(actHandler.obtainMessage(0, 0, 1, "登录超时"));

				}
			}
		}
	}


	@SuppressLint("HandlerLeak")
	 class ActHandler extends Handler {
	        @Override
	        public void handleMessage(Message msg) {
	        	if(msg.what== HttpUtil.HTTP_OK){
					 try{
					 	Log.e("ActHandler","ActHandler="+msg.obj.toString());
						 org.json.JSONObject json=new org.json.JSONObject(msg.obj.toString());

						 if(json.has("success") && json.getString("success").equals("true")){
						 	Log.e("msg","msg.obj.toString()="+msg.obj.toString());
						 	//解析会议数据
							 SharePreferenceUtil.getInstance(SplashActivity.this).setMeetingInfo(msg.obj.toString());
							 Config.meetingInfo=new org.json.JSONObject(json.getString("meeting"));
							 Config.clientInfo=json.getJSONObject("client");
							 Config.signSetting=new org.json.JSONObject(json.getString("sign_setting"));
							 Config.meetingId=Config.meetingInfo.getInt("id");
							 Config.display_atts=Config.clientInfo.getJSONObject("display_atts");
							 Config.myid= Config.clientInfo.getString("id");
							 loadComplete=true;
						 	//Helper.switchActivity(SplashActivity.this,MainActivity.class);
						 	//SplashActivity.this.finish();
						 }else{

						 }
					 }catch(Exception ex){
						 ex.printStackTrace();
						 new Handler().postDelayed(new Runnable() {
							 @Override
							 public void run() {
								 //reLogin();
							 }
						 },3000);
					 }

				 }else  if(msg.what==HttpUtil.HTTP_ERROR){
					 Toast.makeText(getApplicationContext(), "网络异常:"+msg.obj.toString(),
						     Toast.LENGTH_LONG).show();
					 new Handler().postDelayed(new Runnable() {
						 @Override
						 public void run() {
							 reLogin();
						 }
					 },3000);

				 }else{
		        	Toast.makeText(getApplicationContext(),msg.obj.toString(),
							Toast.LENGTH_LONG).show();
				 }
	        }
	 }

	 private void reLogin(){
		 String mobile=DBUtil.getConfigVariable("local", "mobile");
		 String token=DBUtil.getConfigVariable("local", "token");
		 doLogin(mobile,token);
	 }

	 private void doLogin(final String strMobile, final String strSms){
			final java.util.HashMap<String, String> params=Config.getParameters();
			params.put("mobile", strMobile);
			params.put("token", strSms);
			HttpUtil.requestURL(Config.WEB_URL+"/pdti/mobile.doLogin2", params,new IHttpCallback(){
				@Override
				public void onHttpComplete(int code, String result) {
					Log.i(TAG +":abc", code+"---"+result);
					actHandler.sendMessage(actHandler.obtainMessage(code, result));
				}});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		actHandler.removeCallbacksAndMessages(null);
	}
}
