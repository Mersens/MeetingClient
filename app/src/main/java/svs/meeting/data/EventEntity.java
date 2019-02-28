package svs.meeting.data;

/**
 * Created by Administrator on 2018/3/21.
 */

public class EventEntity {
    public static final String MQTT_MSG="MQTT_MSG";
    public String type;
    public String value;

    @Override
    public String toString() {
        return "EventEntity{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", mqEntity=" + mqEntity +
                '}';
    }

    private MQEntity mqEntity;
    public MQEntity getMqEntity() {
        return mqEntity;
    }
    public void setMqEntity(MQEntity mqEntity) {
        this.mqEntity = mqEntity;
    }

    public EventEntity(){

    }
    public EventEntity(String type){
        this.type=type;;
    }
    public EventEntity(String type, String value){
        this.type=type;
        this.value=value;
    }

   public static class MQEntity{
        private String topic;
        private String msgType;
        private String time;
        private String clientId;

       @Override
       public String toString() {
           return "MQEntity{" +
                   "topic='" + topic + '\'' +
                   ", msgType='" + msgType + '\'' +
                   ", time='" + time + '\'' +
                   ", clientId='" + clientId + '\'' +
                   ", content='" + content + '\'' +
                   '}';
       }

       private String content;
       public MQEntity(){}

        public MQEntity( String topic,
                String msgType,
                 String time,
                 String clientId,
                String content){

            this.clientId=clientId;
            this.topic=topic;
            this.msgType=msgType;
            this.time=time;
            this.content=content;

        }



        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getMsgType() {
            return msgType;
        }

        public void setMsgType(String msgType) {
            this.msgType = msgType;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }


    }


}
