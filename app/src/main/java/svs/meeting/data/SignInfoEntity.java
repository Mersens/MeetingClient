package svs.meeting.data;

import java.io.Serializable;

public class SignInfoEntity implements Serializable {

    private String id;
    private String ip_addr;
    private String seat_no;
    private String uname;
    private String login_type;
    private String meeting_id;

    public String getLogin_type() {
        return login_type;
    }

    public void setLogin_type(String login_type) {
        this.login_type = login_type;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp_addr() {
        return ip_addr;
    }

    public void setIp_addr(String ip_addr) {
        this.ip_addr = ip_addr;
    }

    public String getSeat_no() {
        return seat_no;
    }

    public void setSeat_no(String seat_no) {
        this.seat_no = seat_no;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }


    public String getMeeting_id() {
        return meeting_id;
    }

    public void setMeeting_id(String meeting_id) {
        this.meeting_id = meeting_id;
    }





}
