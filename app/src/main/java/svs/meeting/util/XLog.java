package svs.meeting.util;


import android.os.Environment;
import android.util.Log;

import java.io.FileNotFoundException;

public class XLog {
	private static java.io.FileOutputStream logger=null;
	public static final String TAG="XLog";
	public static void init(){
		String folder= Environment.getExternalStorageDirectory().getPath()+"/logs";
		java.io.File folderFile=new java.io.File(folder);
		if(!folderFile.exists())
			folderFile.mkdir();
		String fileName=folder+"/"+Helper.formatDate("yyyyMMddHHmmss", new java.util.Date())+".txt";;
		try {
			logger=new java.io.FileOutputStream(new java.io.File(fileName),true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e("XLOG","XLog.init():"+e.toString());
		}	
	}
	
	private static void writeLog(String msg){
		try{
			String strTime=Helper.formatDate("yyyy/MM/dd HH:mm:ss ", new java.util.Date());
			String strToWrite=strTime+msg+"\n";
			logger.write(strToWrite.getBytes("gbk"));
		}catch(Exception ex){

		}
	}
	
	public static void close(){
		try{
			logger.close();
		}catch(Exception ex){
			Log.e(TAG,"XLog.close():"+ex.toString());
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void log(String strMsg, Class cls){
		if(cls!=null){
			if(!filter(cls.getCanonicalName()))
				return;
		}
		Log.i(cls==null?"XLog":cls.getCanonicalName(), strMsg);
		writeLog("["+(cls==null?"XLog":cls.getCanonicalName())+"]"+strMsg);
	}
	public static void log(String strMsg){
		Log.i(TAG, "log: "+strMsg);
	}


	@SuppressWarnings("rawtypes")
	public static void error(String strMsg, Class cls){
		if(cls!=null){
			if(!filter(cls.getCanonicalName()))
				return;
		}
		Log.e(cls==null?"XLog":cls.getCanonicalName(), strMsg);
	}

	
	@SuppressWarnings("rawtypes")
	public static void warn(String strMsg, Class cls){
		Log.w(cls==null?"XLog":cls.getCanonicalName(), strMsg);
	}

	private static boolean filter(String clsName){
		return true;
	}


	public static void i(String tag, String msg) {  //信息太长,分段打印
		//因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
		//  把4*1024的MAX字节打印长度改为2001字符数
		int max_str_length = 2001 - tag.length();
		//大于4000时
		while (msg.length() > max_str_length) {
			Log.i(tag, msg.substring(0, max_str_length));
			msg = msg.substring(max_str_length);
		}
		//剩余部分
		Log.i(tag, msg);
	}
}
