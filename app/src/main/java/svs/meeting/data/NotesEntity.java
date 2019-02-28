package svs.meeting.data;

import java.io.Serializable;

public class NotesEntity implements Serializable {
    private String id;
    private String meeting_id;
    private String seat_no;
    private String note_title;
    private String note_content;
    private String modified;
    private String note_type;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMeeting_id() {
        return meeting_id;
    }

    public void setMeeting_id(String meeting_id) {
        this.meeting_id = meeting_id;
    }

    public String getSeat_no() {
        return seat_no;
    }

    public void setSeat_no(String seat_no) {
        this.seat_no = seat_no;
    }

    public String getNote_title() {
        return note_title;
    }

    public void setNote_title(String note_title) {
        this.note_title = note_title;
    }

    public String getNote_content() {
        return note_content;
    }

    public void setNote_content(String note_content) {
        this.note_content = note_content;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getNote_type() {
        return note_type;
    }

    public void setNote_type(String note_type) {
        this.note_type = note_type;
    }


}
