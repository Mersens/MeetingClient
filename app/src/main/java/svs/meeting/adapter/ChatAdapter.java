package svs.meeting.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import svs.meeting.data.MsgEntity;

/**
 * @author :smile
 * @project:ChatAdapter
 * @date :2016-01-22-14:18
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    //文本
    private final int TYPE_RECEIVER_TXT = 0;
    private final int TYPE_SEND_TXT = 1;
    //图片
    private final int TYPE_SEND_IMAGE = 2;
    private final int TYPE_RECEIVER_IMAGE = 3;
    //位置
    private final int TYPE_SEND_LOCATION = 4;
    private final int TYPE_RECEIVER_LOCATION = 5;
    //语音
    private final int TYPE_SEND_VOICE =6;
    private final int TYPE_RECEIVER_VOICE = 7;
    //视频
    private final int TYPE_SEND_VIDEO =8;
    private final int TYPE_RECEIVER_VIDEO = 9;

    //同意添加好友成功后的样式
    private final int TYPE_AGREE = 10;

    /**
     * 显示时间间隔:10分钟
     */
    private final long TIME_INTERVAL = 10 * 60 * 1000;
    
    private List<MsgEntity> msgs = new ArrayList<>();

    private String currentUid="";


    public ChatAdapter(Context context) {

    }
    public int findPosition(String message) {
        int index = this.getCount();
        int position = -1;
        while(index-- > 0) {
            if(message.equals(this.getItem(index))) {
                position = index;
                break;
            }
        }
        return position;
    }

    public int findPosition(long id) {
        int index = this.getCount();
        int position = -1;
        while(index-- > 0) {
            if(this.getItemId(index) == id) {
                position = index;
                break;
            }
        }
        return position;
    }

    public int getCount() {
        return this.msgs == null?0:this.msgs.size();
    }

    public void addMessages(List<MsgEntity> messages) {
        msgs.addAll(0, messages);
        notifyDataSetChanged();
    }

    public void addMessage(MsgEntity message) {
        msgs.addAll(Arrays.asList(message));
        notifyDataSetChanged();
    }

    /**获取消息
     * @param position
     * @return
     */
    public MsgEntity getItem(int position){
        return this.msgs == null?null:(position >= this.msgs.size()?null:this.msgs.get(position));
    }

    /**移除消息
     * @param position
     */
    public void remove(int position){
        msgs.remove(position);
        notifyDataSetChanged();
    }

    public MsgEntity getFirstMessage() {
        if (null != msgs && msgs.size() > 0) {
            return msgs.get(0);
        } else {
            return null;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e("onCreateViewHolder","type=="+viewType);
          if (viewType == TYPE_SEND_TXT) {
            return new SendTextHolder(parent.getContext(), parent,onRecyclerViewListener);
        } else if (viewType == TYPE_RECEIVER_TXT) {
            return new ReceiveTextHolder(parent.getContext(), parent,onRecyclerViewListener);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BaseViewHolder)holder).bindData(msgs.get(position));
        if (holder instanceof SendTextHolder) {
            ((SendTextHolder)holder).showTime(shouldShowTime(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        MsgEntity message = msgs.get(position);
        Log.e("getItemViewType","type=="+message.getType());
        if (message.getType()==1){
            return TYPE_SEND_TXT;
        }else if(message.getType()==2){
            return TYPE_RECEIVER_TXT;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return msgs.size();
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    private boolean shouldShowTime(int position) {

        return false;
    }
}
