package svs.meeting.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import svs.meeting.app.R;
import svs.meeting.data.MsgEntity;

public class ReceiveTextHolder extends BaseViewHolder {
    @BindView(R.id.iv_avatar)
    protected ImageView iv_avatar;

    @BindView(R.id.tv_time)
    protected TextView tv_time;

    @BindView(R.id.tv_message)
    protected TextView tv_message;

    public ReceiveTextHolder(Context context, ViewGroup root, OnRecyclerViewListener onRecyclerViewListener) {
        super(context, root, R.layout.item_chat_received_message,onRecyclerViewListener);
    }

    public void onAvatarClick(View view) {

    }

    @Override
    public void bindData(Object o) {
        final MsgEntity message = (MsgEntity)o;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String time = dateFormat.format(new Date());
        tv_time.setText(time);
        tv_message.setText(message.getMsg());
        tv_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(onRecyclerViewListener!=null){
                    onRecyclerViewListener.onItemClick(getAdapterPosition());
                }
            }
        });

        tv_message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onRecyclerViewListener != null) {
                    onRecyclerViewListener.onItemLongClick(getAdapterPosition());
                }
                return true;
            }
        });
    }

    public void showTime(boolean isShow) {
        tv_time.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

}
