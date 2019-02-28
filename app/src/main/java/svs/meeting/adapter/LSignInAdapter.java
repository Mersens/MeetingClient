package svs.meeting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import svs.meeting.app.R;
import svs.meeting.data.SignInfoEntity;

public class LSignInAdapter extends BaseAdapter {
    private List<SignInfoEntity> mList;
    private Context mContext;

    public LSignInAdapter(List<SignInfoEntity> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    public void setList(List<SignInfoEntity> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.ysignin_item, null);
            holder.mTextName = convertView.findViewById(R.id.tv_name);
            holder.mTextStatu = convertView.findViewById(R.id.tv_statu);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final SignInfoEntity entity = mList.get(position);
        holder.mTextName.setText(entity.getUname());
        holder.mTextStatu.setText("已离开");
        return convertView;
    }

    public static class ViewHolder {
        public TextView mTextName;
        public TextView mTextStatu;


    }


}
