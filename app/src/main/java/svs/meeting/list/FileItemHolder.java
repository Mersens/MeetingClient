package svs.meeting.list;

import android.widget.ImageView;
import android.widget.TextView;

public class FileItemHolder {
    private ImageView fileIcon;
    private TextView fileName;

    public ImageView getFileIcon() {
        return fileIcon;
    }

    public void setFileIcon(ImageView fileIcon) {
        this.fileIcon = fileIcon;
    }

    public TextView getFileName() {
        return fileName;
    }

    public void setFileName(TextView fileName) {
        this.fileName = fileName;
    }
}
