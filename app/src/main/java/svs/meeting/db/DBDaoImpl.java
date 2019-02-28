package svs.meeting.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import svs.meeting.data.MsgEntity;

/**
 * Created by Administrator on 2018/3/23.
 */

public class DBDaoImpl implements DBDao {
    private DBHelper helper;
    private Context context;

    public DBDaoImpl(Context context) {
        helper = DBHelper.getInstance(context);
        this.context = context;
    }

    @Override
    public List<MsgEntity> findAllMsg() {
        List<MsgEntity> list = new ArrayList<MsgEntity>();
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from " + MsgEntity.TABLE_NAME,
                null);
        while (cursor.moveToNext()) {
            MsgEntity entity = new MsgEntity();
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            entity.setId(id+"");
            String pid=cursor.getString(cursor.getColumnIndex(MsgEntity.PID));
            entity.setPid(pid);
            String msg_time=cursor.getString(cursor.getColumnIndex(MsgEntity.MSG_TIME));
            entity.setMsg_time(msg_time);
            String msg_type=cursor.getString(cursor.getColumnIndex(MsgEntity.MSG_TYPE));
            entity.setMsg_type(msg_type);
            String msg=cursor.getString(cursor.getColumnIndex(MsgEntity.MSG));
            entity.setMsg(msg);
            String topic=cursor.getString(cursor.getColumnIndex(MsgEntity.TOPIC));
            entity.setTopic(topic);
            String from_name=cursor.getString(cursor.getColumnIndex(MsgEntity.FROM_NAME));
            entity.setFrom_name(from_name);
            String from_seat=cursor.getString(cursor.getColumnIndex(MsgEntity.FROM_SEAT));
            entity.setFrom_seat(from_seat);
            String meeting_id=cursor.getString(cursor.getColumnIndex(MsgEntity.MEETING_ID));
            entity.setMeeting_id(meeting_id);
            String sid=cursor.getString(cursor.getColumnIndex(MsgEntity.SID));
            entity.setSid(sid);
            String oid=cursor.getString(cursor.getColumnIndex(MsgEntity.OID));
            entity.setOid(oid);
            int mtype=cursor.getInt(cursor.getColumnIndex(MsgEntity.TYPE));
            entity.setType(mtype);
            list.add(entity);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public List<MsgEntity> findMsgById(String topic,String seat_no,String msg_type,String meeting_id) {
        List<MsgEntity> list = new ArrayList<MsgEntity>();
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql="select * from " + MsgEntity.TABLE_NAME+" where "+MsgEntity.TOPIC+"=? and "+MsgEntity.FROM_SEAT+"=? and "+MsgEntity.MSG_TYPE+"= ? and "+MsgEntity.MEETING_ID+"=?";
        Cursor cursor = db.rawQuery(
                sql,
                new String[]{topic,seat_no,msg_type,meeting_id});
        while (cursor.moveToNext()) {
            MsgEntity entity = new MsgEntity();
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            entity.setId(id+"");
            String pid=cursor.getString(cursor.getColumnIndex(MsgEntity.PID));
            entity.setPid(pid);
            String msg_time=cursor.getString(cursor.getColumnIndex(MsgEntity.MSG_TIME));
            entity.setMsg_time(msg_time);
            String msg_type1=cursor.getString(cursor.getColumnIndex(MsgEntity.MSG_TYPE));
            entity.setMsg_type(msg_type1);
            String msg=cursor.getString(cursor.getColumnIndex(MsgEntity.MSG));
            entity.setMsg(msg);
            String topic1=cursor.getString(cursor.getColumnIndex(MsgEntity.TOPIC));
            entity.setTopic(topic1);
            String from_name=cursor.getString(cursor.getColumnIndex(MsgEntity.FROM_NAME));
            entity.setFrom_name(from_name);
            String from_seat=cursor.getString(cursor.getColumnIndex(MsgEntity.FROM_SEAT));
            entity.setFrom_seat(from_seat);
            String meeting_id1=cursor.getString(cursor.getColumnIndex(MsgEntity.MEETING_ID));
            entity.setMeeting_id(meeting_id1);
            String sid=cursor.getString(cursor.getColumnIndex(MsgEntity.SID));
            entity.setSid(sid);
            String oid=cursor.getString(cursor.getColumnIndex(MsgEntity.OID));
            entity.setOid(oid);
            int mtype=cursor.getInt(cursor.getColumnIndex(MsgEntity.TYPE));
            entity.setType(mtype);
            list.add(entity);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public void delMsgById(String id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from " + MsgEntity.TABLE_NAME + " where _id=?",
                new Object[]{id});
        db.close();
    }

    @Override
    public void delAllMsg() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from " + MsgEntity.TABLE_NAME
               );
        db.close();
    }

    @Override
    public void addMsgInfo(MsgEntity entity) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(
                "insert into " + MsgEntity.TABLE_NAME + "(" + MsgEntity.PID + ","+MsgEntity.TYPE+","
                        +MsgEntity.MSG_TIME+","+MsgEntity.MSG_TYPE+","+MsgEntity.MSG+","
                        +MsgEntity.TOPIC+","+MsgEntity.FROM_NAME+","+MsgEntity.FROM_SEAT+","
                        +MsgEntity.MEETING_ID+","+MsgEntity.SID+","+MsgEntity.OID+")" +
                        " values(?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{entity.getPid(),entity.getType(),entity.getMsg_time(),entity.getMsg_type(),entity.getMsg(),
                        entity.getTopic(),entity.getFrom_name(),entity.getFrom_seat(),entity.getMeeting_id(),
                        entity.getSid(),entity.getOid()});
        db.close();
    }
}
