package svs.meeting.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


import butterknife.BindView;
import svs.meeting.app.R;
import svs.meeting.data.MsgEntity;


/**
 * 发送的文本类型
 */
public class SendTextHolder extends BaseViewHolder implements View.OnClickListener,View.OnLongClickListener {

  @BindView(R.id.iv_avatar)
  protected ImageView iv_avatar;

  @BindView(R.id.iv_fail_resend)
  protected ImageView iv_fail_resend;

  @BindView(R.id.tv_time)
  protected TextView tv_time;

  @BindView(R.id.tv_message)
  protected TextView tv_message;
  @BindView(R.id.tv_send_status)
  protected TextView tv_send_status;

  @BindView(R.id.progress_load)
  protected ProgressBar progress_load;



  public SendTextHolder(Context context, ViewGroup root,OnRecyclerViewListener listener) {
    super(context, root, R.layout.item_chat_sent_message, listener);

  }

  @Override
  public void bindData(Object o) {
    final MsgEntity message = (MsgEntity)o;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    tv_message.setText(message.getMsg());
    tv_time.setText(dateFormat.format(new Date()));

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
