package svs.meeting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;

import java.util.List;

import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.MsgEntity;

public class CallServiceAdapter extends BaseAdapter {
    private List<MsgEntity> mList;
    private Context mContext;
   public CallServiceAdapter(List<MsgEntity> mList, Context mContext){
       this.mList=mList;
       this.mContext=mContext;
   }
   public void setList(List<MsgEntity> mList){
       this.mList=mList;
       notifyDataSetChanged();
   }
   public void addMsg(MsgEntity msg){
       this.mList.add(msg);
       notifyDataSetChanged();

   }

    @Override
    public int getCount() {
        return mList==null?0:mList.size();
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
        ViewHolder holder=null;
       if(convertView==null){
           holder=new ViewHolder();
           convertView=LayoutInflater.from(mContext).inflate(R.layout.signin_item,null);
           holder.mTextName=convertView.findViewById(R.id.tv_name);
           holder.mTextStatu=convertView.findViewById(R.id.tv_statu);
           convertView.setTag(holder);
       }else {
           holder=(ViewHolder)convertView.getTag();
       }
        final MsgEntity entity=mList.get(position);
        String msg=null;
        try {
            final String uname=Config.clientInfo.getString("name");
            if(uname.equals(entity.getFrom_name())){
                msg="我 "+entity.getMsg_time()+" "+entity.getMsg();
            }else {
                msg=entity.getFrom_name()+" "+entity.getMsg_time()+" "+entity.getMsg();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.mTextName.setText(msg);
        if(entity.getType()==2){
            holder.mTextName.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }else {
            holder.mTextName.setTextColor(mContext.getResources().getColor(R.color.actionbar_color));
        }
        holder.mTextStatu.setVisibility(View.GONE);
        return convertView;
    }

    public static class ViewHolder{
       public TextView mTextName;
       public TextView mTextStatu;

    }


}
