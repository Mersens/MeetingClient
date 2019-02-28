package svs.meeting.service;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.json.JSONObject;

import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.util.DBUtil;
import svs.meeting.util.HttpUtil;
import svs.meeting.util.IHttpCallback;
import svs.meeting.util.RxBus;
import svs.meeting.util.XLog;

public class MessageProcessor {


    public static final int ID_wait_task = 123456;
    public static final int ID_wait_task_zq = 1234567;
    public static final int ID_MSG_EVENT = 0x01;
    public static final int ID_MSG_EVENT_MY = 0x07;//自己发的对话
    public static final int ID_MSG_SESSION_ADDED = 0x0101; //添加好友或加入群消息,Session重新载入
    public static final int ID_MSG_SESSION_REMOVED = 0x0102;
    public static final int ID_MSG_SESSION_AGREE = 0x0103;
    public static final int ID_MSG_CONTACT_REMOVE = 0x0104;
    //public static final int ID_MSG_CHTGRP_ADDED=0x0105; //邀请加入聊天组
    //public static final int ID_MSG_CHTGRP_REMOVED=0x0106; //移出聊天组
    public static final int ID_MSG_CHTGRP_CHANGED = 0x0107; //聊天组人员变化

    public static final int ID_MSG_REFRESHED = 0x03;
    public static final int ID_MSG_ARRIVED = 0x0301;
    public static final int ID_TASK_ARRIVED = 0x0302;

    public static final int ID_NOTIFY_EVENT = 0x05;
    public static final int ID_NTY_HAS_READ = 0x0501;
    public static final int ID_NTY_HAS_REMOVED = 0x0502;
    public static final int ID_NTY_HAS_ARRIVED = 0x0503;

    public static final int ID_LOGS_LOADED = 0x04;
    public static final int ID_LOGS_HAS_READ = 0x0401;
    public static final int ID_TADK_HAS_REMOVED = 0x0402;
    public static final int ID_LOGS_COUNT_CALC = 0x0403;

    public static final int LOAD_ZQZW = 0x3301;
    public static final int ID_ADV_PUB = 0x09;
    public static final int ID_NEW_CONTACT = 0x10;
    public static final int ID_SOURCE_NOTIFY = 0x11;
    public static final int ID_RZ_NOTIFY = 0x12;
    public static final int ID_WEB_RELOAD = 0x13;

    public static final int ID_CONTACT_DELETE = 0x20;

    public static final int GLOBAL_PHOTO_CHANGED = 0x900;
    public static final int GLOBAL_CHATGROUP_NAME_CHANGED = 0x901;

    public static final String CONTACT_ITEM_NOTIFY = "__NOTIFY__";

    public static final String TYPE_CHAT = "CHAT";
    public static final String TYPE_PUB = "PUB";
    public static final String TYPE_UNION = "UNIN";
    public static final String TYPE_NOTIFY = "NOTIFY";
    public static final String TYPE_TASK = "TASK";
    public static final String SUBS_CHANGED = "SUBS_CHNGED";
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_SYSMSG = "SYSMSG";
    public static java.util.HashMap<String, String> lastMsgs = new java.util.HashMap<String, String>();

    private static MessageProcessor instance;
    private LocalService localService;

    private MessageProcessor() {

    }

    public static MessageProcessor getInstance() {
        if (instance == null)
            synchronized (MessageProcessor.class){
            if(instance == null){
                instance = new MessageProcessor();
            }
            }


        return instance;
    }

    public void initProcessor(LocalService service) {
        this.localService = service;
    }

    public void sendBroadcat(int type) {
        sendBroadcat(type, "");
    }

    public void sendBroadcat(int type, String content) {
        if (localService == null)
            return;
        localService.sendBroadcast(type, content);
    }



    public void processMessage(String topic, String msgType, String time, final String clientId, final String content) {
        Log.e("processMessage","processMessage=="+topic);
        XLog.log("收到消息==》\tmessage[" + topic + "]:" + content + ",from:" + clientId + ",type:" + msgType + ",time:" + time);
        EventEntity eventEntity = new EventEntity();
        eventEntity.type = EventEntity.MQTT_MSG;
        EventEntity.MQEntity entity = new EventEntity.MQEntity();
        entity.setTopic("svs/all");
        entity.setMsgType(msgType);
        entity.setClientId(clientId);
        entity.setTime(time);
        entity.setContent(content);
        eventEntity.setMqEntity(entity);
        RxBus.getInstance().send(eventEntity);
    }

    public void reloadContacts(final String clientId, final String content) {

    }


    public void sendNotify(String content) {
        if (localService == null)
            return;
        localService.sendBroadcast(ID_NOTIFY_EVENT, content);
    }

    public void notifyTzEvent() {
        if (localService == null)
            return;
        localService.sendBroadcast(ID_NOTIFY_EVENT, "");
    }

    public void notifyMsgRefreshed() {
        if (localService == null)
            return;
        localService.sendBroadcast(ID_MSG_REFRESHED, "");
    }

    public void notifyLogsLoaded() {
        if (localService == null)
            return;
        localService.sendBroadcast(ID_LOGS_LOADED, "");
    }


    public void onMqttConnected() {
        updateChatlogs();
        //MqttManagerV3.getInstance().sendWithThread("LOGIN"+(char)0x05+"REQ", MqttManagerV3.msgCenter);
        //依次订阅所有session的主题

    }

    public void updateChatlogs() {
        //DBUtil.executeUpdate(DBUtil.getDb(),"delete from tb_chatlogs");
        final int total = DBUtil.getTotalMsg(true);
        java.util.HashMap<String, String> params = Config.getParameters();
        params.put("flag", total == 0 ? "all" : "notread");
        HttpUtil.requestURL(Config.WEB_URL + "/pdti/mobile.loadChatLogs", params, new IHttpCallback() {
            @Override
            public void onHttpComplete(int code, String result) {

                Log.e("updateChatlogs","updateChatlogs="+result.trim());
                parseChatLogs(result.trim(), total == 0);
            }
        });

        sendBroadcat(ID_LOGS_LOADED);

    }

    public void updateChatlogsTask(String to) {
        //DBUtil.executeUpdate(DBUtil.getDb(),"delete from tb_chatlogs");
        final int total = DBUtil.getTotalMsg(true);
        java.util.HashMap<String, String> params = Config.getParameters();
        params.put("flag", "notread");
        params.put("to", to);
        HttpUtil.requestURL(Config.WEB_URL + "/pdti/mobile.loadChatLogs", params, new IHttpCallback() {
            @Override
            public void onHttpComplete(int code, String result) {
                // TODO Auto-generated method stub
                //handler.sendMessage(handler.obtainMessage(code, result));
                parseChatLogs(result.trim(), total == 0);
                XLog.log("pdti/mobile.loadChatLogs" + result.trim());
            }
        });

        sendBroadcat(ID_LOGS_LOADED);

    }

    private void parseChatLogs(String strret, boolean isAll) {
        try {
            if (strret != null && !strret.equals("")) {
                org.json.JSONObject ret = new org.json.JSONObject(strret);
                if (!ret.has("items")) {
                    return;
                }
                org.json.JSONArray rows = ret.getJSONArray("items");
                ////Config.contacts=ret.getJSONObject("contacts");

                for (int i = 0; i < rows.length(); i++) {
                    org.json.JSONObject row = rows.getJSONObject(i);
                    if (!row.has("type")) {
                        continue;
                    }
                    String topic = MqttManagerV3.PREFIX + row.getString("to");
                    String type = row.has("type") ? row.getString("type") : "CHAT";
                    String from = row.getString("from");
                    String body = row.getString("body");
                    String rowid = row.getString("rowid");
                    String time = row.getString("time");
                    String isRead = "0";
                    String flag = "flag" + Config.myid;
                    if (row.has(flag)) {
                        isRead = row.getString(flag);
                    }
                    if (type.equals("NOTIFY") || type.equals("SYSMSG"))
                        continue;

                    if (type.equals("CHAT")) {
                        String[] items = body.split("" + (char) 0x03);
                        if (items[2].startsWith("CHTGRP") || items[2].startsWith("SYS") || items[2].startsWith("CHTADD")
                                || items[2].startsWith("CHTRMV") || items[2].startsWith("NTY")
                                )
                            continue;
                        XLog.log("加入聊天信息topic：" + topic + ">from:" + from + ">items:" + items[2] + ">type:" + type + ">time:" + time + ">isRead:" + isRead);
                        DBUtil.deteleChatLog(topic, time);
                        DBUtil.addChatLog(topic, from, items[2], type, time, isRead);
                    } else if (type.equals(TYPE_TASK)) {
                        try {
                            String[] items = body.split("" + (char) 0x03);
                            //XLog.log("task body=="+body,MessageProcessor.class);
                            JSONObject task = new JSONObject(items[2]);
                            String strNewTopic = task.getString("rowid");
                            if (!isAll)
                                DBUtil.addChatLog(strNewTopic, from, items[2], TYPE_TASK, time, isRead);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    } else if (type.equals(TYPE_UNION) && from.equals("ContractsPlugin")) {
                        continue;
                    } else {
                        //XLog.log("PARSE OTHER("+row+")::"+type+",body="+body,MessageProcessor.class);
                        DBUtil.addChatLog(topic, from, body, type, time, isRead);
                    }


                    String header = "REPLY" + (char) 0x02 + Config.myid + (char) 0x02 + "*" + (char) 0x02;
                    MqttManagerV3.getInstance().sendWithThread(header + rowid, MqttManagerV3.msgCenter);
                }

                notifyLogsLoaded();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
