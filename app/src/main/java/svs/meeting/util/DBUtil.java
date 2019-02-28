package svs.meeting.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import svs.meeting.data.Config;
import svs.meeting.service.MessageProcessor;
import svs.meeting.service.MqttManagerV3;

public class DBUtil {
    private static final String tag = "DBUtil";
    private static SQLiteDatabase localDb = null;
    private static Context __context = null;
    public static String devid;
    private static int index = 0;

    private static HashMap<String, HashMap<String, String>> dictMap = new HashMap<String, HashMap<String, String>>();

    public static void parseDcitMap(String strxml) {
        if (strxml.length() == 0)
            return;
        try {
            localDb.execSQL("delete from yz_values");
            org.dom4j.Document doc = org.dom4j.DocumentHelper.parseText(strxml);
            org.dom4j.Element root = doc.getRootElement();
            @SuppressWarnings("unchecked")
            List<org.dom4j.Element> list = root.elements("category");
            for (int i = 0; i < list.size(); i++) {
                org.dom4j.Element cate = list.get(i);
                @SuppressWarnings("unchecked")
                List<org.dom4j.Element> items = cate.elements("item");
                for (int j = 0; j < items.size(); j++) {
                    org.dom4j.Element item = items.get(j);
                    String code = item.attributeValue("code");
                    String value = item.attributeValue("value");
                    localDb.execSQL("insert into yz_values values('" + cate.attributeValue("name") + "','" + code + "','" + value + "')");
                }
            }
        } catch (Exception ex) {
            Log.e("DBUtil.parseDcitMap", ex.toString());
        }
        //XLog.log("xml=="+strxml);
    }

    private static void getLocalInfo(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(__context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String deviceid = tm.getDeviceId();
        devid = deviceid;
    }

    public static void initDatabase(boolean rebuild, Context ctx) {
        __context = ctx;
        SQLiteDatabase db = getDb();
        if (rebuild) {
            clearAllTables(db);
        }

        getLocalInfo(ctx);

        try {

            if (!hasTable(db, "yz_values"))
                db.execSQL(getTableDef_values());

            if (!hasTable(db, "yz_config")) {
                db.execSQL(getTableDef_config());
            }

            if (!hasTable(db, "tb_contacts")) {
                db.execSQL(getTableDef_tb_contacts());
            }

            if (!hasTable(db, "tb_chatlogs")) {
                db.execSQL(getTableDef_tb_chatlogs());
            }


            try {
                Cursor c1 = db.rawQuery("select * from tb_chatlogs limit 0,1", null);
                String[] names = c1.getColumnNames();
                c1.close();
                boolean bFound = false;
                for (int i = 0; i < names.length; i++) {
                    if (names[i].equals("to_cid")) {
                        bFound = true;
                        break;
                    }
                }

                if (!bFound) {
                    DBUtil.executeUpdate(DBUtil.getDb(), "alter table tb_chatlogs add column to_cid varchar2(32)");
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            clearAllTables(db);
            ex.printStackTrace();
        }
    }


    private static String getTableDef_tb_chatlogs() {

        StringBuffer sb = new StringBuffer();
        sb.append("create table tb_chatlogs(");
        sb.append("chat_time varchar(20),");
        sb.append("topic varchar(64),");
        sb.append("from_id varchar(64),");
        sb.append("chat_msg varchar(256),");
        sb.append("is_read char(1),");
        sb.append("msg_type varchar(10),");
        sb.append("local_cid varchar(32),");
        sb.append("contract_id varchar(64), ");
        sb.append("CONSTRAINT 'pk_chatlogs' PRIMARY KEY (chat_time,from_id)");
        sb.append(")");
        return sb.toString();
    }

    private static String getTableDef_tb_contacts() {
        StringBuffer sb = new StringBuffer();
        sb.append("create table tb_contacts(");
        sb.append("id varchar(64),");
        sb.append("jid varchar(64),");
        sb.append("name varchar(32),CONSTRAINT 'pk_contacts' PRIMARY KEY ( id )");
        sb.append(")");
        return sb.toString();
    }

    private static String getTableDef_values() {
        StringBuffer sb = new StringBuffer();
        sb.append("create table yz_values(");
        sb.append("table_name varchar(32),");
        sb.append("codeid varchar(20),");
        sb.append("codevalue varchar(32),CONSTRAINT 'pk_abc' PRIMARY KEY ( table_name, codeid )");
        sb.append(")");
        return sb.toString();
    }


    private static String getTableDef_config() {
        StringBuffer sb = new StringBuffer();
        sb.append("create table yz_config(");
        sb.append("name varchar(32) primary key,");
        sb.append("val varchar(20),");
        sb.append("category varchar(20)");
        sb.append(")");
        return sb.toString();
    }

    public static SQLiteDatabase getDb() {
        openDb(__context);
        return localDb;
    }

    public static SQLiteDatabase openDb(Context context) {
        if (context == null)
            return null;
        if (localDb == null)
            localDb = context.openOrCreateDatabase("__hykc_db", Activity.MODE_PRIVATE, null);
        return localDb;
    }

    public static void closeDb() {
        if (localDb != null)
            localDb.close();
        localDb = null;
    }

    public static void clearAllTables(SQLiteDatabase db) {
        try {
            String sql = "SELECT name FROM sqlite_master where type='table' and name not in ('android_metadata','sqlite_sequence')";
            Cursor c1 = db.rawQuery(sql, null);
            if (c1 != null && c1.moveToFirst()) {
                do {
                    String name = c1.getString(c1.getColumnIndex("name"));
                    db.execSQL("drop table " + name);
                } while (c1.moveToNext());
            }
            c1.close();
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

    public static boolean hasValues(SQLiteDatabase db, String tableName) {
        try {
            String sql = "SELECT COUNT(*) as c FROM " + tableName;
            Cursor c1 = db.rawQuery(sql, null);
            int count = 0;
            if (c1 != null && c1.moveToFirst()) {
                count = c1.getInt(c1.getColumnIndex("c"));
            }
            c1.close();
            return count > 0;
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
        return false;
    }

    public static int getRecordsCount(SQLiteDatabase db, String tableName) {
        int count = -1;
        try {
            String sql = "SELECT COUNT(*) as c FROM tb_case_codes";
            Cursor c1 = db.rawQuery(sql, null);
            if (c1 != null && c1.moveToFirst()) {
                count = c1.getInt(c1.getColumnIndex("c"));
            }
            c1.close();
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
        return count;
    }

    public static boolean hasTable(SQLiteDatabase db, String tableName) {
        try {
            String sql = "SELECT COUNT(*) as c FROM sqlite_master where type='table' and name='" + tableName + "'";
            Cursor c1 = db.rawQuery(sql, null);
            int count = 0;
            if (c1 != null && c1.moveToFirst()) {
                count = c1.getInt(c1.getColumnIndex("c"));
            }
            c1.close();
            return count > 0;
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
        return false;
    }

    public static boolean executeUpdate(SQLiteDatabase db, String sql) {
        //XLog.log("====>"+sql);
        try {
            db.execSQL(sql);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    public static String getConfigVariable(String category, String name) {
        ArrayList<ArrayList<String>> rows = retriveDataWithSql(getDb(), "select * from yz_config where category='" + category + "' and name='" + name + "'");
        if (rows.size() > 0) {
            ArrayList<String> cols = rows.get(0);
            return cols.get(1);
        }
        return "";
    }

    public static String getAllConfigVariable(){
        ArrayList<ArrayList<String>> rows = retriveDataWithSql(getDb(), "select * from yz_config");
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < rows.size(); i++) {
            ArrayList<String> cols = rows.get(i);
            try {
                jsonObject.put(cols.get(0) , cols.get(1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject.toString();
    }

    public static void removeConfigVariable(String category, String name) {
        executeUpdate(getDb(), "delete from yz_config where category='" + category + "' and name='" + name + "'");
    }

    public static void debugConfigVariable() {
        ArrayList<ArrayList<String>> rows = retriveDataWithSql(getDb(), "select * from yz_config");
        XLog.log("=========>" + rows.toString(), DBUtil.class);
    }

    public static void putConfigVariable(String category, String name, String val) {
        executeUpdate(getDb(), "delete from yz_config where category='" + category + "' and name='" + name + "'");
        executeUpdate(getDb(), "insert into yz_config(name,val,category) values('" + name + "','" + val + "','" + category + "')");
    }

    public static ArrayList<ArrayList<String>> retriveDataWithSql(SQLiteDatabase db, String sql) {
        return retriveDataWithSql(db, sql, null);
    }

    public static ArrayList<ArrayList<String>> retriveDataWithSql(SQLiteDatabase db, String sql, String[] params) {
        ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
        try {
            Cursor c1 = db.rawQuery(sql, params);
            if (c1 != null && c1.moveToFirst()) {
                do {
                    ArrayList<String> row = new ArrayList<String>();

                    for (int i = 0; i < c1.getColumnCount(); i++) {
                        String name = c1.getColumnName(i);
                        int idx = c1.getColumnIndex(name);
                        String val = "";
                        switch (c1.getType(idx)) {
                            case Cursor.FIELD_TYPE_NULL:
                                val = String.valueOf(c1.getInt(idx));
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                val = String.valueOf(c1.getInt(idx));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                val = c1.getString(idx);
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                val = String.valueOf(c1.getFloat(idx));
                                break;
                            default:
                                val = c1.getString(idx);
                        }
                        //XLog.log("column count=="+c1.getColumnCount()+"name="+name+",idx="+idx+",value="+val);
                        row.add(val);
                    }
                    list.add(row);
                } while (c1.moveToNext());
            }
            c1.close();
        } catch (Exception ex1) {
            //ex1.printStackTrace();
            XLog.error(ex1.toString(), DBUtil.class);
        }
            return list;
    }

    public static ArrayList<JSONObject> retriveJsonDataWithSql(SQLiteDatabase db, String sql, String[] params) {
        ArrayList<JSONObject> list = new ArrayList<JSONObject>();
        try {
            Cursor c1 = db.rawQuery(sql, params);
            if (c1 != null && c1.moveToFirst()) {
                do {
                    org.json.JSONObject row = new org.json.JSONObject();


                    for (int i = 0; i < c1.getColumnCount(); i++) {
                        String name = c1.getColumnName(i);
                        int idx = c1.getColumnIndex(name);
                        String val = "";
                        switch (c1.getType(idx)) {
                            case Cursor.FIELD_TYPE_NULL:
                                val = String.valueOf(c1.getInt(idx));
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                val = String.valueOf(c1.getInt(idx));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                val = c1.getString(idx);
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                val = String.valueOf(c1.getFloat(idx));
                                break;
                            default:
                                val = c1.getString(idx);
                        }
                        //XLog.log("column count=="+c1.getColumnCount()+"name="+name+",idx="+idx+",value="+val);
                        row.put(name, val);
                    }
                    list.add(row);
                } while (c1.moveToNext());
            }
            c1.close();
        } catch (Exception ex1) {
            ex1.printStackTrace();
            XLog.error(ex1.toString() + "[" + sql + "]", DBUtil.class);
        }
        return list;
    }


    public static String getDictVersion() {
        String strVer = "0";
        ArrayList<ArrayList<String>> list = DBUtil.retriveDataWithSql(DBUtil.getDb(), "select count(*) as n from yz_values");
        String count = list.get(0).get(0);
        strVer = count + "";
        return strVer;
    }


    public static HashMap<String, String> getDictMaps(String category) {
        HashMap<String, String> itemMap = null;
        if (dictMap.size() == 0) {
            loadDictItems();
        }
        if (dictMap.containsKey(category))
            return dictMap.get(category);
        return itemMap;
    }

    private static void loadDictItems() {
        ArrayList<ArrayList<String>> datas = retriveDataWithSql(localDb, "select * from yz_values order by table_name asc");
        for (int i = 0; i < datas.size(); i++) {
            ArrayList<String> rows = datas.get(i);
            String cate = rows.get(0);
            String codeid = rows.get(1);
            String codevalue = rows.get(2);
            HashMap<String, String> itemMap = null;
            if (dictMap.containsKey(cate)) {
                itemMap = dictMap.get(cate);
            } else {
                itemMap = new HashMap<String, String>();
                dictMap.put(cate, itemMap);
            }
            itemMap.put(codeid, codevalue);
        }
    }

    public static String getDictValue(String category, String code) {
        String val = "-";
        if (dictMap.size() == 0) {
            loadDictItems();
        }
        //XLog.log("map=="+dictMap);

        if (dictMap.containsKey(category)) {
            HashMap<String, String> itemMap = dictMap.get(category);
            if (itemMap.containsKey(code))
                val = itemMap.get(code);
        }

        return val;
    }


    public static void addContacts(String rawId, String name, String rawJid) {
        String id = rawId;
        String jid = rawJid;
        if (id.indexOf("@") > 0)
            id = rawId.substring(0, rawId.indexOf("@"));
        if (jid.indexOf("/") > 0)
            jid = rawJid.substring(0, rawJid.indexOf("/"));
        ArrayList<ArrayList<String>> list = DBUtil.retriveDataWithSql(DBUtil.getDb(), "select count(*) as n from tb_contacts where id='" + id + "'");
        int count = Integer.parseInt(list.get(0).get(0));
        if (count == 0) {
            //XLog.log("\tadd user id="+id+",jid="+jid+"name="+name);
            DBUtil.executeUpdate(getDb(), "insert into tb_contacts values('" + id + "','" + jid + "','" + name + "')");
        }
    }

    public static int getTotalMsg() {
        return getTotalMsg(false);
    }

    public static int getTotalMsg(boolean isAll) {
        String sql = "";
        if (!isAll)
            sql = "select count(*) as n from tb_chatlogs where is_read='0' and  local_cid='" + Config.myid + "'";
        else
            sql = "select count(*) as n from tb_chatlogs where local_cid='" + Config.myid + "'and from_id!='" + Config.myid + "'";
        ArrayList<ArrayList<String>> list = DBUtil.retriveDataWithSql(DBUtil.getDb(), sql);
        int count;
        if (list.size() == 0){
            count = 0;
        }else{
            count = Integer.parseInt(list.get(0).get(0));
        }
        return count;
    }

    public static int getTotalOnlineMsg() {
        //java.util.ArrayList<ArrayList<String>> list=DBUtil.retriveDataWithSql(DBUtil.getDb(), "select from_id,count(*) as n from tb_chatlogs where is_read='0' group by from_id");
        int count = 0;
        /*
        for(ArrayList<String> row:list){
			if(Config.availContacts.containsKey(row.get(0))){
				count+=Integer.parseInt(row.get(1));
			}
		}*/
        return count;
    }

    public static ArrayList<ArrayList<String>> getGroupedTotalMsg() {
        StringBuffer sb = new StringBuffer();
        sb.append("select topic,count(*) as n from tb_chatlogs where is_read='0' and local_cid='" + Config.myid + "' and from_id!='" + Config.myid + "' ");
        sb.append(" and from_id<>'ContractsPlugin'");
        sb.append(" and chat_msg not like 'CHTGRP%'");
        sb.append(" and chat_msg not like 'SYS%'");
        sb.append(" and chat_msg not like 'NTY%'");
        sb.append(" and chat_msg not like 'CHTRMV%'");
        sb.append(" and chat_msg not like 'CHTADD%'");
        sb.append(" group by topic");
        ArrayList<ArrayList<String>> list = DBUtil.retriveDataWithSql(DBUtil.getDb(), sb.toString());
        return list;
    }

    public static void clearChatFlag(String sessionid) {
        clearChatFlag(sessionid, null);
    }

    public static void clearChatFlag(String sessionid, String type) {
        //XLog.info("Clear:"+jid+",type="+msgType);
        //java.util.ArrayList<org.json.JSONObject> list=DBUtil.retriveJsonDataWithSql(DBUtil.getDb(), "select * from tb_chatlogs where from_id like '"+jid+"%' and is_me='1' and is_read='0' ", null);
        //XLog.info("result:"+list);
        if (type == null)
            DBUtil.executeUpdate(DBUtil.getDb(), "update tb_chatlogs set is_read='1' where topic='" + sessionid + "'");
        else
            DBUtil.executeUpdate(DBUtil.getDb(), "update tb_chatlogs set is_read='1' where topic='" + sessionid + "' and msg_type='" + type + "'");

        MessageProcessor.getInstance().sendBroadcat(MessageProcessor.ID_LOGS_HAS_READ);
    }

    public static void clearChatLogs() {
        DBUtil.executeUpdate(DBUtil.getDb(), "delete from tb_chatlogs");
    }

    public static void deteleChatLog(String topic, String time, String txt) {
        topic = topic.substring(topic.lastIndexOf(".") + 1);
        String timesqlstr = "";
        XLog.log("time" + time + "===" + topic);
        //查询获得游标
        Cursor cursor = localDb.query("tb_chatlogs", new String[]{"chat_time"}, "topic=? and chat_msg=? ", new String[]{topic, txt}, null, null, null);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = null;
        try {
            date1 = sdf.parse(time);

            while (cursor.moveToNext()) {
                timesqlstr = cursor.getString(cursor.getColumnIndex("chat_time"));
                XLog.log("topic  sql==" + timesqlstr);

                Date date2 = sdf.parse(timesqlstr);
                long interval = (date1.getTime() - date2.getTime()) / 1000;
                if (interval > -3 && interval < 3) {
                    deteleChatLog(topic, timesqlstr);

                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void deteleChatLog(String topic, String time) {
        topic = topic.substring(topic.lastIndexOf(".") + 1);
        DBUtil.executeUpdate(DBUtil.getDb(), "delete from tb_chatlogs where topic = '" + topic + "'and chat_time = '" + time + "'");
    }

    public static String sqlChatLog(String rowid, String time) {
        String topic = "";
//        String sql= "SELECT *  from tb_chatlogs where  date = ?";
        //查询获得游标
//        String[] columns ={"topic"};
//        String selection =" name = '" + rowid + "'and date = '" + time + "'";
//        Cursor cursor = localDb.rawQuery (sql,new String[]{time});
        Cursor cursor = localDb.query("tb_chatlogs", new String[]{"topic", "chat_time"}, "chat_time=?", new String[]{time}, null, null, null);

        while (cursor.moveToNext()) {
            topic = cursor.getString(cursor.getColumnIndex("topic"));
            XLog.log("topic  sql==" + topic);
        }
        return topic;
    }


    public static void addChatLog(String topic, String from_id, String msg, String msgType, String time, String isRead) {

        String tFrom = from_id;
        String tTopic = topic;
        if (tFrom.startsWith(MqttManagerV3.PREFIX))
            tFrom = tFrom.substring(MqttManagerV3.PREFIX.length());
        if (tTopic.startsWith(MqttManagerV3.PREFIX))
            tTopic = tTopic.substring(MqttManagerV3.PREFIX.length());

        String strTime = time;
        if (strTime == null)
            strTime = Helper.formatDate("yyyy-MM-dd HH:mm:ss", new Date());

        if (msgType.equals("TASK")) {
            strTime += index++;
            if (index > 9)
                index = 0;
        }
        //XLog.log("添加消息记录:"+topic+","+msg,Config.class);
        //更新session消息begin
        /*
        if (Config.sessionsMap.containsKey(tTopic)) {
            try {
                org.json.JSONObject contact = Config.sessionsMap.get(tTopic);
                contact.put("data:create_time", time);
                String tMsg = parseLastMessage(msg, msgType);
                contact.put("data:message", tMsg);
                //XLog.log("updateed:"+contact,Config.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }*/
        //end

        try {
            StringBuffer sb = new StringBuffer();
            sb.append("insert into tb_chatlogs (chat_time,topic,from_id,chat_msg,is_read,msg_type,local_cid,contract_id)");
            sb.append("values('" + strTime + "','" + tTopic + "','" + tFrom + "','" + msg + "','" + isRead + "','" + msgType + "','" + Config.myid + "','-')");
            DBUtil.executeUpdate(getDb(), sb.toString());
        } catch (Exception ex) {
            XLog.error("addChatLog异常:" + ex.toString(), DBUtil.class);
        }
    }

    public static boolean selectChatLog(String topic, String from_id, String time) {
        String tFrom = from_id;
        String tTopic = topic;
        if (tFrom.startsWith(MqttManagerV3.PREFIX))
            tFrom = tFrom.substring(MqttManagerV3.PREFIX.length());
        if (tTopic.startsWith(MqttManagerV3.PREFIX))
            tTopic = tTopic.substring(MqttManagerV3.PREFIX.length());

        String strTime = time;
        if (strTime == null)
            strTime = Helper.formatDate("yyyy-MM-dd HH:mm:ss", new Date());

        //XLog.log("添加消息记录:"+topic+","+msg,Config.class);
        //更新session消息begin
        /*
        if (Config.sessionsMap.containsKey(tTopic)) {
            try {
                org.json.JSONObject contact = Config.sessionsMap.get(tTopic);
                contact.put("data:create_time", time);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }*/
        //end
        ArrayList<ArrayList<String>> rows = retriveDataWithSql(getDb(), "select * from tb_chatlogs where chat_time='" + strTime + "' and from_id='" + from_id + "'");
        if (rows.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    public static String parseLastMessage(String strMsg, String msgType) {
        String tMsg = strMsg;
        if (msgType.equals(MessageProcessor.TYPE_CHAT)) {
            if (tMsg.startsWith("CHTTXT"))
                tMsg = tMsg.substring(6);
            else if (tMsg.startsWith("CHTVDO"))
                tMsg = "[视频]";
            else if (tMsg.startsWith("CHTPIC"))
                tMsg = "[图片]";
            else if (tMsg.startsWith("CHTVOC"))
                tMsg = "[语音]";
            else if (tMsg.startsWith("CHTFIL")) {
                if (tMsg.indexOf(".") > 0) {
                    tMsg = tMsg.substring(tMsg.lastIndexOf(".") + 1).toUpperCase();
                    tMsg = "[" + tMsg + "文件]";
                } else
                    tMsg = "[文件]";
            }
        } else {
            tMsg = "[其它消息]";
        }
        return tMsg;
    }

}
