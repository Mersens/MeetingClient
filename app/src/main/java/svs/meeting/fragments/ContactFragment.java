package svs.meeting.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.promeg.pinyinhelper.Pinyin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.activity.ServiceActivity;
import svs.meeting.adapter.ContactAdapter;
import svs.meeting.adapter.OnRecyclerViewListener;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.Friend;
import svs.meeting.data.User;

import svs.meeting.listener.onUserSelectListener;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.widgets.IMutlipleItem;

public class ContactFragment extends Fragment {
    private ContactAdapter adapter;
    private RecyclerView mRecyclerView;
    LinearLayoutManager layoutManager;
    private List<Friend> list = new ArrayList<>();
    ServiceActivity activity;
    onUserSelectListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity=(ServiceActivity)context;
        listener=(onUserSelectListener)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact,null);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }
    private void initViews(View view){
        mRecyclerView = view.findViewById(R.id.rc_view);
        initEvent();
        getSignInfo();
    }

    private void getSignInfo() {
        try {
            String id = Config.meetingInfo.getString("id");
            String sql = "select * from logins where login_type<>'02' and meeting_id=" + id;
            Map<String, String> map = Config.getParameters();
            map.put("type", "hql");
            map.put("ql", sql);
            RequestManager.getInstance()
                    .mServiceStore
                    .do_query(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ResultObserver(new RequestManager.onRequestCallBack() {
                        @Override
                        public void onSuccess(String msg) {
                            Log.e("getSignInfo onSuccess", msg);
                            if (!TextUtils.isEmpty(msg)) {
                                try {
                                    JSONObject json = new JSONObject(msg);
                                    if (json.getBoolean("success")) {
                                        JSONArray array = new JSONArray(json.getString("rows"));
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject object = array.getJSONObject(i);
                                            Friend friend = new Friend();
                                            friend.setId(object.getString("id"));
                                            friend.setIp_addr(object.getString("ip_addr"));
                                            friend.setLogin_type(object.getString("login_type"));
                                            friend.setMeeting_id(object.getString("meeting_id"));
                                            friend.setSeat_no(object.getString("seat_no"));
                                            friend.setUname(object.getString("uname"));
                                            User user = new User();
                                            user.setUsername(object.getString("uname"));
                                            friend.setFriendUser(user);
                                            String pinyin = Pinyin.toPinyin(object.getString("uname").charAt(0));
                                            friend.setPinyin(pinyin.substring(0, 1).toUpperCase());
                                            list.add(friend);
                                        }
                                        Friend friend = new Friend();
                                        friend.setUname("所有人");
                                        User user = new User();
                                        user.setUsername("所有人");
                                        friend.setFriendUser(user);
                                        String pinyin = Pinyin.toPinyin("所有人".charAt(0));
                                        friend.setPinyin(pinyin.substring(0, 1).toUpperCase());
                                        list.add(0,friend);
                                        adapter.bindDatas(list);
                                        adapter.notifyDataSetChanged();

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(String msg) {
                            Log.e("doQuery onError", msg);
                        }
                    }));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initEvent() {
        IMutlipleItem<Friend> mutlipleItem = new IMutlipleItem<Friend>() {

            @Override
            public int getItemViewType(int postion, Friend friend) {
                return ContactAdapter.TYPE_ITEM;
            }

            @Override
            public int getItemLayoutId(int viewtype) {
                return R.layout.item_contact;
            }

            @Override
            public int getItemCount(List<Friend> list) {
                return list.size();
            }
        };
        adapter = new ContactAdapter(getActivity(), mutlipleItem, null);
        mRecyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        setListener();
    }
    private void setListener() {
        adapter.setOnRecyclerViewListener(new OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
/*                    Friend friend = adapter.getItem(position);
                    User user = friend.getFriendUser();
                    BmobIMUserInfo info = new BmobIMUserInfo(user.getObjectId(), user.getUsername(), user.getAvatar());
                    //TODO 会话：4.1、创建一个常态会话入口，好友聊天
                    BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("c", conversationEntrance);
                    startActivity(ChatActivity.class, bundle);*/

                final Friend friend = adapter.getItem(position);
                if(listener!=null){
                    listener.onUserSelect(friend);
                }

            }

            @Override
            public boolean onItemLongClick(final int position) {
                if (position == 0) {
                    return true;
                }

                return true;
            }
        });
    }
    public static ContactFragment getInstance(){
        ContactFragment fragment=new ContactFragment();
        return fragment;
    }

}
