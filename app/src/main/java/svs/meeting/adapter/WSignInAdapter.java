package svs.meeting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import svs.meeting.app.R;
import svs.meeting.data.User;

public class WSignInAdapter extends BaseAdapter {
    private List<User> mList;
    private Context mContext;
   public WSignInAdapter(List<User> mList, Context mContext){
       this.mList=mList;
       this.mContext=mContext;
   }
   public void setList(List<User> mList){
       this.mList=mList;
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
           convertView=LayoutInflater.from(mContext).inflate(R.layout.ysignin_item,null);
           holder.mTextName=convertView.findViewById(R.id.tv_name);
           convertView.setTag(holder);
       }else {
           holder=(ViewHolder)convertView.getTag();
       }
       final  User user=mList.get(position);
       holder.mTextName.setText(user.getUsername());
       return convertView;
    }

    public static class ViewHolder{
       public TextView mTextName;

    }


}
