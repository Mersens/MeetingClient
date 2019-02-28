package svs.meeting.data;

public class MsgEntity {
    public static final String TABLE_NAME="localmsg";
    public static final String ID="_id";
    public static final String PID="pid";
    public static final String MSG_TIME="msg_time";
    public static final String MSG_TYPE="msg_type";
    public static final String MSG="msg";
    public static final String TOPIC="topic";
    public static final String FROM_NAME="from_name";
    public static final String FROM_SEAT="from_seat";
    public static final String MEETING_ID="meeting_id";
    public static final String SID="sid";
    public static final String OID="oid";
    public static final String TYPE="mtype";
    private String id;
    private String pid;
    private String msg_time;
    private String msg_type;
    private String msg;
    private String topic;
    private String from_name;
    private String from_seat;
    private String meeting_id;
    private String sid;
    private String oid;
    private int type;
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getMsg_time() {
        return msg_time;
    }

    public void setMsg_time(String msg_time) {
        this.msg_time = msg_time;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getFrom_seat() {
        return from_seat;
    }

    public void setFrom_seat(String from_seat) {
        this.from_seat = from_seat;
    }

    public String getMeeting_id() {
        return meeting_id;
    }

    public void setMeeting_id(String meeting_id) {
        this.meeting_id = meeting_id;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}
