package svs.meeting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import svs.meeting.app.R;
import svs.meeting.data.NotesEntity;

public class NotesAdapter extends BaseAdapter {
    private List<NotesEntity> mList;
    private Context mContext;
    OnButtonClickListener listener;
    public NotesAdapter(List<NotesEntity> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    public void setList(List<NotesEntity> mList) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.notes_item, null);
            holder.mTextNum = convertView.findViewById(R.id.tv_num);
            holder.mTextTitle = convertView.findViewById(R.id.tv_title);
            holder.mTexttime = convertView.findViewById(R.id.tv_time);
            holder.mTextDel = convertView.findViewById(R.id.tv_del);
            holder.mTextUpdate = convertView.findViewById(R.id.tv_update);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final NotesEntity entity=mList.get(position);
        holder.mTextNum.setText((position+1)+"");
        holder.mTextTitle.setText(entity.getNote_title());
        holder.mTexttime.setText(entity.getModified());
        holder.mTextDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onDelClick(position,entity);
                }
            }
        });
        holder.mTextUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onUpdateClick(position,entity);

                }
            }
        });
        return convertView;
    }

    public static class ViewHolder {
        public TextView mTextNum;
        public TextView mTextTitle;
        public TextView mTexttime;
        public TextView mTextDel;
        public TextView mTextUpdate;
    }

    public void setOnButtonClickListener(OnButtonClickListener listener){
        this.listener=listener;
    }

    public interface OnButtonClickListener{
        void onUpdateClick(int pos,NotesEntity entity);
        void onDelClick(int pos,NotesEntity entity);
    }

    public void remove(int pos){
        mList.remove(pos);
        notifyDataSetChanged();
    }




}
