package svs.meeting.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import svs.meeting.app.R;
import svs.meeting.util.XLog;

public class ImageListAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater inflater;

    private String[] imageUrls;

    public ImageListAdapter(Context context, String[] imageUrls) {
        super(context, R.layout.lv_pic_item, imageUrls);

        this.context = context;
        this.imageUrls = imageUrls;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.lv_pic_item, parent, false);
        }
        //XLog.log("\tloading..."+imageUrls[position]);
        Glide
                .with(context)
                .load(imageUrls[position])
                .into((ImageView) convertView);

        return convertView;
    }
}
