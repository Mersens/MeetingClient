package svs.meeting.service;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.MsgType;
import svs.meeting.util.DBUtil;
import svs.meeting.util.RxBus;
import svs.meeting.util.XLog;

public class MqttManagerV3 implements INetManager {
    public static final String PREFIX = "VirtualTopic.pm.";

    public static final String TYPE_CHAT = "CHAT";
    public static final String TYPE_PUB = "PUB";
    public static final String TYPE_NOTIFY = "NOTIFY";
    public static final String TYPE_SOURCE = "SOURCE";
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_SHARE = "share";

    private boolean isWorking = false;
    //private final static boolean CLEAN_START = true;
    //private final static short KEEP_ALIVE = 30;
    private String[] topics = {"svs/all", "svs/nodes/"+Config.CLIENT_IP};
    private final static int[] QOS_VALUES = {0, 0};

    private MqttClient mqttClient = null;

    private MessageListener msgListener = null;

    private static MqttManagerV3 instance = null;

    private MemoryPersistence persistence = new MemoryPersistence();

    public static String msgCenter = "VirtualTopic.pm.Local";

    public static MqttManagerV3 getInstance() {
        if (instance == null)
            instance = new MqttManagerV3();

        return instance;
    }

    private MqttManagerV3() {
        //topics[0] = "VirtualTopic.pm." + Config.myid;
        Log.e("Config.myid","Config.myid="+Config.myid);
    }

    @Override
    public void setRelayServer(String server) {
        // TODO Auto-generated method stub
    }


    @Override
    public void startManager() {
        // TODO Auto-generated method stub
        isWorking = true;

        new Thread() {
            public void run() {
                while (isWorking) {
                    if (mqttClient != null && mqttClient.isConnected()) {
                        try {
                            Thread.sleep(3000);
                        } catch (Exception ex) {
                        }
                        continue;
                    }
                    notifyConnectionEvent(INetManager.CONNECTING);
                    boolean isCreated = createClient();
                    if (isCreated && mqttClient != null) {
                        try {
                            mqttClient.subscribe(topics, QOS_VALUES);

                            XLog.log("manager starting!", MqttManagerV3.class);
                            notifyConnectionEvent(INetManager.CONNECTED);
                            MessageProcessor.getInstance().onMqttConnected();
                        } catch (MqttException e) {
                            // TODO Auto-generated catch block
                            if (mqttClient.isConnected()) {
                                try {
                                    mqttClient.disconnect();
                                } catch (MqttException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                            }
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (Exception ex) {
                    }
                }
            }
        }.start();
    }

    public void subscribe(String topic) throws MqttException {
        if (mqttClient != null) {
            mqttClient.subscribe(topic);
        }
    }

    public void unsubscribe(String topic) throws MqttException {
        if (mqttClient != null) {
            mqttClient.unsubscribe(topic);
        }
    }

    @Override
    public void setMsgListener(MessageListener l) {
        this.msgListener = l;
    }

    @Override
    public void notifyConnectionEvent(String evt) {
        if (this.msgListener != null)
            msgListener.onConnectionEvent(evt);
    }

    @Override
    public void send(String msg, String to) {
        if (mqttClient != null && mqttClient.isConnected()) {
                try {
                    mqttClient.publish(topics[0], msg.getBytes(), 0, false);
                    Log.e("mqttClient",msg);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

           /* try {
                if (msg.startsWith("REPLY")) {
                    mqttClient.publish(msgCenter, msg.getBytes(), 0, false);
                    return;
                }

                String msgType = "CHAT";
                String msgBody = msg;
                if (msg.indexOf("" + (char) 0x05) >= 0) {
                    String[] items = msg.split("" + (char) 0x05);
                    msgType = items[0];
                    msgBody = items[1];
                }

                String header = msgType + (char) 0x02 + Config.myid + (char) 0x02 + to + (char) 0x02;
                String strMsg = Config.myid + ((char) 0x03) + TYPE_CHAT + ((char) 0x03) + msgBody;
                XLog.log("发送消息:(=>" + to + "):" + strMsg, MqttManagerV3.class);
                mqttClient.publish(msgCenter, (header + strMsg).getBytes(), 0, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
*/
        }
    }

    public void deleteMsg(String msg, String to) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                String msgType = "SYSMSG";
                String msgBody = msg;
                if (msg.indexOf("" + (char) 0x05) >= 0) {
                    String[] items = msg.split("" + (char) 0x05);
                    msgType = items[0];
                    msgBody = items[1];
                }
                String header = msgType + (char) 0x02 + Config.myid + (char) 0x02 + to + (char) 0x02;
                String strMsg = Config.myid + ((char) 0x03) + TYPE_CHAT + ((char) 0x03) + msgBody;
                XLog.log("发送消息:(=>" + to + "):" + strMsg, MqttManagerV3.class);
                mqttClient.publish(msgCenter, (header + strMsg).getBytes(), 0, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void sendWithThread(final String msg, final String to) {
        new Thread() {
            public void run() {
                try {
                    send(msg, to);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }
    @Override
    public void stopManager() {
        // TODO Auto-generated method stub
        isWorking = false;
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mqttClient = null;
            }
        }
    }

    private boolean createClient() {
        try {

            String server = DBUtil.getConfigVariable("local", "server");
            String port = DBUtil.getConfigVariable("local", "port");
            String user = DBUtil.getConfigVariable("local", "user");
            String password = DBUtil.getConfigVariable("local", "password");
            String broker = "tcp://" + server + ":" + port;
            String mobile = DBUtil.getConfigVariable("local" , "mobile");
            mqttClient = new MqttClient(broker, mobile, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(user);
            connOpts.setPassword(password.toCharArray());
            System.out.println("Connecting to broker: " + broker);
            mqttClient.connect(connOpts);
            mqttClient.setCallback(new MqttCallbackHandler());

            if (QOS_VALUES.length == 0 || QOS_VALUES.length != topics.length) {
                XLog.warn("无效设置!", MqttManagerV3.class);
                return false;
            }

            //mqttClient.publish(topics[0], "keepalive".getBytes(), QOS_VALUES[0], true);
        } catch (MqttException e) {
            e.printStackTrace();
            mqttClient = null;
            return false;
        }

        return true;
    }

    class MqttCallbackHandler implements MqttCallback {
        @Override
        public void connectionLost(Throwable arg0) {
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void messageArrived(String topic, MqttMessage msg)
                throws Exception {
            String strmsg = new String(msg.getPayload(), "utf-8");
            String[] raw = strmsg.split("" + (char) 0x02);
            XLog.log("消息到达(" + topic + "):" + strmsg + ",len=" + raw.length, MqttManagerV3.class);
            if (strmsg.equals("keepalive")) {
                return;
            }
            String strs[] =strmsg.split("\\\\~\\^" );
            String msgType=strs[2];
            String content=strs[3];
            if(MsgType.MSG_CHAT.equals(msgType) || MsgType.MSG_SERVICE.equals(msgType) || MsgType.MSG_REQUEST.equals(msgType)){
                String uname=strs[0];
                String seat_no=strs[1];
                String time=strs[4];
                String ip=strs[5];
                content=content+";"+uname+";"+seat_no+";"+time+";"+ip;
            }
            MessageProcessor.getInstance().processMessage(topic, msgType, "", "", content);

            /*String msgType = "";
            String msgTime = "";
            if (raw.length > 2) {
                String rowid = raw[0];
                msgType = raw[1];
                msgTime = raw[2];
                strmsg = raw[3];
                if (rowid != null && !rowid.equals("-")) {
                    String header = "REPLY" + (char) 0x02 + Config.myid + (char) 0x02 + "*" + (char) 0x02;
                    sendWithThread(header + rowid, msgCenter);
                }
            }

            strmsg=strmsg.replaceAll("~\\^","");
            Log.e("strmsg","strmsg=="+strmsg);
            String[] items = strmsg.split("\\\\" );
            for (int i = 0; i <items.length ; i++) {
                Log.e("items","items=="+items[i]);
            }*/

            //XLog.info(">>>>>>>>>>>"+strmsg);Compere\10\info\{"action":"show","text":"会议暂停..."}\20190107143225\192.168.1.10
            /*
			if(items.length!=3){
				if(msgType.equals(MessageProcessor.TYPE_NOTIFY)){
					MessageProcessor.getInstance().processMessage(topic,msgType,msgTime,MqttManagerV3.msgCenter, msgType, strmsg);
				}else if(msgType.equals(MessageProcessor.TYPE_LOGIN)){
					MessageProcessor.getInstance().processMessage(topic,msgType,msgTime,MqttManagerV3.msgCenter, msgType, strmsg);
				}else{
					XLog.info(">>>��Ч��Ϣ:"+topic+","+strmsg);
				}
			}else{
				MessageProcessor.getInstance().processMessage(topic,msgType,msgTime,items[0], items[1], items[2]);
			}*/

        }
    };
}
