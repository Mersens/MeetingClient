package svs.meeting.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Created by ranjunchao on 2017-10-31.
 */

public abstract class AbstractListAdapter<T> extends BaseAdapter {
    private Context context;
    protected List<T> list;
    protected LayoutInflater mInflater;
    protected ListView listView;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public AbstractListAdapter(Context context, ListView listView){
        this.context=context;
        this.listView=listView;
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list==null?0:list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}
