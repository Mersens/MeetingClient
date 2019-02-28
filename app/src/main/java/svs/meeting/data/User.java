package svs.meeting.data;

import java.io.Serializable;

public class User implements Serializable {
    private String avatar;
    private String username;

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    private String tid;
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public User(){}

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
