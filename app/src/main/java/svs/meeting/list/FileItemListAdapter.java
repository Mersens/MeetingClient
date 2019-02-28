package svs.meeting.list;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import svs.meeting.app.R;

public class FileItemListAdapter extends AbstractListAdapter<FileItem> {
    private final static String TAG = "FileItemtListViewAdapter";
    private Handler handler;

    public FileItemListAdapter(Context context, ListView listView, Handler handler) {
        super(context, listView);
        this.handler = handler;
    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final FileItemHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.lv_file_item, null);
            TextView txtName = (TextView) convertView.findViewById(R.id.file_name);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.image);
            RelativeLayout lv_contact_view = (RelativeLayout) convertView.findViewById(R.id.lv_contact_view);

            //Button btnEdit = (Button) convertView.findViewById(R.id.btn_edit);
            //Button btnRemove = (Button) convertView.findViewById(R.id.btn_remove);


            holder = new FileItemHolder();
            holder.setFileName(txtName);
            holder.setFileIcon(imgView);
            convertView.setTag(holder);


        } else {
            holder = (FileItemHolder) convertView.getTag();
        }

        FileItem info = list.get(position);
        if (info != null) {
            holder.getFileName().setText(info.getFile_name());
            /*
            holder.getTxtType().setText(info.getTypeName());
            holder.getTxtAcct().setText(info.getAccount());
            holder.getTxtName().setText(info.getName());

            holder.getBtnEdit().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.obtainMessage(111, position,-1).sendToTarget();
                }
            });
            holder.getBtnRemove().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.obtainMessage(222, position,-1).sendToTarget();
                }
            });
            holder.getLv_contact_view().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.obtainMessage(333, position,-1).sendToTarget();
                }
            });
            switch (info.getTypeName()) {
                case "中国工商银行":
                    holder.getImage().setImageResource(R.drawable.gs);
                    break;
                case "中国农业银行":
                    holder.getImage().setImageResource(R.drawable.ny);
                    break;
                case "中国银行":
                    holder.getImage().setImageResource(R.drawable.zg);
                    break;
                case "中国建设银行":
                    holder.getImage().setImageResource(R.drawable.js);
                    break;
                case "中国交通银行":
                    holder.getImage().setImageResource(R.drawable.jt);
                    break;
                case "中国邮政银行":
                    holder.getImage().setImageResource(R.drawable.yz);
                    break;
                case "平安银行":
                    holder.getImage().setImageResource(R.drawable.pa);
                    break;
                case "招商银行":
                    holder.getImage().setImageResource(R.drawable.zs);
                    break;
                case "浦发银行":
                    holder.getImage().setImageResource(R.drawable.pf);
                    break;
                case "民生银行":
                    holder.getImage().setImageResource(R.drawable.ms);
                    break;
                case "光大银行":
                    holder.getImage().setImageResource(R.drawable.gd);
                    break;
                case "支付宝":
                    holder.getImage().setImageResource(R.drawable.zfb);
                    break;
                case "微信":
                    holder.getImage().setImageResource(R.drawable.wx);
                    break;
                case "中信银行":
                    holder.getImage().setImageResource(R.drawable.zx);
                    break;
                case "广发银行":
                    holder.getImage().setImageResource(R.drawable.gf);
                    break;
            }*/
        }

        return convertView;
    }


}
