package svs.meeting.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import svs.meeting.util.DBUtil;
import svs.meeting.util.XLog;


public class LocalService extends Service {
	public static final int XMPP_MSG=0x99;
	public static final String ACTION = Service.class.getCanonicalName();
	
	private INetManager manager=null;
	
	@Override
	public IBinder onBind(Intent intent) {
		XLog.log("onBind本地服务器");
		return null;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		XLog.log("onUnbind本地服务器");
		this.stopSelf();
		return super.onUnbind(intent);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		XLog.log("onCreate本地服务器");
		try{
			MessageProcessor.getInstance().initProcessor(this);
			startManager();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

	@Override
	public void onDestroy() {
		XLog.log("onDestroy本地服务器");
		if(manager!=null)
			manager.stopManager();
		DBUtil.closeDb();
		super.onDestroy();
		
	}
	
	private void startManager(){
		new Thread(){
			public void run(){
				//manager=SocketManager.getInstance();
				if(manager==null){
					manager=MqttManagerV3.getInstance();
					manager.startManager();
				}
			}
		}.start();
	}
	
	public void sendBroadcast(int msgId,Object param){
		Intent intent = new Intent();
		intent.setAction(ACTION);
		intent.putExtra("msgid", msgId);
		if(param!=null)
			intent.putExtra("data", param.toString());
		this.getBaseContext().sendBroadcast(intent);
	}
}
