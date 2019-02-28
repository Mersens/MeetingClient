package svs.meeting.app;

import java.io.Serializable;

public class VoteBallotEntity implements Serializable {
    private String id;
    private String vote_name;
    private String vote_mode;
    private String status;
    private String duration;
    private String content;
    private String atts;
    private String total_count;
    private String signed_count;
    private String meeting_id;
    private String sign_rate_fact;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAtts() {
        return atts;
    }

    public void setAtts(String atts) {
        this.atts = atts;
    }

    public String getTotal_count() {
        return total_count;
    }

    public void setTotal_count(String total_count) {
        this.total_count = total_count;
    }

    public String getSigned_count() {
        return signed_count;
    }

    public void setSigned_count(String signed_count) {
        this.signed_count = signed_count;
    }

    public String getMeeting_id() {
        return meeting_id;
    }

    public void setMeeting_id(String meeting_id) {
        this.meeting_id = meeting_id;
    }

    public String getSign_rate_fact() {
        return sign_rate_fact;
    }

    public void setSign_rate_fact(String sign_rate_fact) {
        this.sign_rate_fact = sign_rate_fact;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVote_name() {
        return vote_name;
    }

    public void setVote_name(String vote_name) {
        this.vote_name = vote_name;
    }

    public String getVote_mode() {
        return vote_mode;
    }

    public void setVote_mode(String vote_mode) {
        this.vote_mode = vote_mode;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



}
