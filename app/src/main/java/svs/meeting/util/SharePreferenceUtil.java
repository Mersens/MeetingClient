package svs.meeting.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Mersens
 * @title SharePreferenceUtil
 * @description:SharePreference工具类，数据存储
 * @time 2019年1月16日
 */
public class SharePreferenceUtil {

    private static final String PREFERENCE_NAME = "_sharedinfo";
    private static final String IS_FIRST = "is_first";
    private static final String MEETINGINFO = "meetingInfo";
    private static SharePreferenceUtil sp;
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor editor;
    private SharePreferenceUtil() {

    }

    public static Boolean getIsFirst() {
        return mSharedPreferences.getBoolean(IS_FIRST, true);
    }

    public static void setIsFirst(Boolean isIsFirst) {
        editor.putBoolean(IS_FIRST, isIsFirst);
        editor.commit();
    }

    public static synchronized SharePreferenceUtil getInstance(Context context) {
        if (sp == null) {
            sp = new SharePreferenceUtil();
            mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            editor = mSharedPreferences.edit();
        }
        return sp;
    }

    public String getMeetingInfo(){
        return mSharedPreferences.getString(MEETINGINFO,null);
    }
    public void setMeetingInfo(String info){
        editor.putString(MEETINGINFO, info);
        editor.commit();
    }

}
