package svs.meeting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import svs.meeting.app.R;
import svs.meeting.app.VoteBallotEntity;

public class VoteBallotAdapter extends BaseAdapter {
    private List<VoteBallotEntity> mList;
    private Context mContext;
    onOperationClickListener listener;
    private int arrClickColor;
    private int disArrClickColor;

    public VoteBallotAdapter(List<VoteBallotEntity> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    public void setList(List<VoteBallotEntity> mList) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.vote_ballot_item, null);
            holder.mTextNum = convertView.findViewById(R.id.tv_num);
            holder.mTextMsg = convertView.findViewById(R.id.tv_name);
            holder.mTextType = convertView.findViewById(R.id.tv_type);
            holder.mTextStatu = convertView.findViewById(R.id.tv_statu);
            holder.mLayoutStart = convertView.findViewById(R.id.layout_start);
            holder.mLayoutCheck = convertView.findViewById(R.id.layout_check);
            holder.mTextCheck=convertView.findViewById(R.id.tv_check);
            holder.mTextStart=convertView.findViewById(R.id.tv_start);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final VoteBallotEntity entity=mList.get(position);
        holder.mTextNum.setText((position+1)+"");
        holder.mTextMsg.setText(entity.getVote_name());

        holder.mTextType.setText(entity.getVote_mode());
        String status=entity.getStatus();
        if("00".equals(status)){
            holder.mLayoutStart.setEnabled(true);
            holder.mLayoutStart.setClickable(true);
            holder.mTextStart.setTextColor(mContext.getResources().getColor(R.color.blue));
            holder.mTextStatu.setText("待进行");
            holder.mTextStatu.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }else {
            holder.mLayoutStart.setEnabled(false);
            holder.mLayoutStart.setClickable(false);
            holder.mTextStart.setTextColor(mContext.getResources().getColor(R.color.color_line_text));
        }
        if("02".equals(status)){
            holder.mTextStatu.setText("进行中");
            holder.mTextStatu.setTextColor(mContext.getResources().getColor(R.color.blue));
        }else if("03".equals(status)){
            holder.mTextStatu.setText("已关闭");
            holder.mTextStatu.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }
        if("02".equals(status) || "03".equals(status)){
            holder.mLayoutCheck.setEnabled(true);
            holder.mLayoutCheck.setClickable(true);
            holder.mTextCheck.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }else {
            holder.mLayoutCheck.setEnabled(false);
            holder.mLayoutCheck.setClickable(false);
            holder.mTextCheck.setTextColor(mContext.getResources().getColor(R.color.color_line_text));
        }

        holder.mLayoutStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onStart(position,entity);

                }
            }
        });
        holder.mLayoutCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onCheck(position,entity);

                }
            }
        });
        return convertView;
    }

    public static class ViewHolder {
        public TextView mTextNum;
        public TextView mTextMsg;
        public TextView mTextType;
        public TextView mTextStatu;
        public RelativeLayout mLayoutStart;
        public RelativeLayout mLayoutCheck;
        public TextView mTextStart;
        public TextView mTextCheck;

    }

public void setOnOperationClickListener(onOperationClickListener listener){
        this.listener=listener;

}

    public interface onOperationClickListener{
        void onStart(int index,VoteBallotEntity entity);
        void onCheck(int index,VoteBallotEntity entity);
    }



}
