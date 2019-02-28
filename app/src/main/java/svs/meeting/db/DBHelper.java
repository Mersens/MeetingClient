package svs.meeting.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import svs.meeting.data.MsgEntity;


/**
 * Created by zzu on 2016/4/6.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 2;
    private static final String NAME = "LOCAL_MSG.db";
    private static final String SQL_MSG_CREAT = "create table "+ MsgEntity.TABLE_NAME+"("+MsgEntity.ID+" integer primary key autoincrement,"
           +MsgEntity.PID+" text,"+MsgEntity.MSG_TIME+" text,"+MsgEntity.MSG_TYPE+" text,"+MsgEntity.MSG+" text,"+MsgEntity.TOPIC+" text,"
            +MsgEntity.FROM_NAME+" text,"+MsgEntity.FROM_SEAT+" text,"+MsgEntity.MEETING_ID+" text,"+MsgEntity.SID+" text,"+MsgEntity.TYPE+" text,"+
            MsgEntity.OID+" text)";
    private static final String SQL_MSG_DROP = "drop table if exists "+MsgEntity.TABLE_NAME;
    public static DBHelper helper = null;
    public static Context mContext;
    private DBHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (helper == null) {
            synchronized (DBHelper.class) {
                if (helper == null) {
                    helper = new DBHelper(context.getApplicationContext());
                }
            }
        }
        mContext = context;
        return helper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_MSG_CREAT);
    }

    /**
     * 当数据库更新时，调用该方法
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        clearCache(db);
    }

    /**
     * 清空数据缓存
     *
     * @param db
     */
    public void clearCache(SQLiteDatabase db) {
        db.execSQL(SQL_MSG_DROP);
        db.execSQL(SQL_MSG_CREAT);
    }
}
