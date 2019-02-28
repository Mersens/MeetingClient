package svs.meeting.adapter;

import android.content.Context;
import android.view.View;

import java.util.Collection;

import svs.meeting.app.R;
import svs.meeting.data.Friend;
import svs.meeting.data.User;
import svs.meeting.widgets.IMutlipleItem;

public class ContactAdapter extends BaseRecyclerAdapter<Friend> {
    public static final int TYPE_NEW_FRIEND = 0;
    public static final int TYPE_ITEM = 1;

    public ContactAdapter(Context context, IMutlipleItem<Friend> items, Collection<Friend> datas) {
        super(context,items,datas);
    }

    @Override
    public void bindView(BaseRecyclerHolder holder, Friend friend, int position) {
        if(holder.layoutId==R.layout.item_contact){

            holder.setText(R.id.tv_recent_name,friend==null?"未知":friend.getUname());
        }
    }

}
