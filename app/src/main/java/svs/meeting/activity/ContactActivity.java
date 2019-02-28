package svs.meeting.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import com.github.promeg.pinyinhelper.Pinyin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.adapter.ContactAdapter;
import svs.meeting.adapter.OnRecyclerViewListener;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.Friend;
import svs.meeting.data.User;
import svs.meeting.util.Helper;
import svs.meeting.util.RequestManager;
import svs.meeting.util.ResultObserver;
import svs.meeting.widgets.IMutlipleItem;

public class ContactActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ContactAdapter adapter;
    private RecyclerView mRecyclerView;
    LinearLayoutManager layoutManager;
    SwipeRefreshLayout sw_refresh;
    private List<Friend> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_contact);
        init();
    }

    private void init() {
        initActionBar();
        initViews();
        initEvent();
        initDatas();

    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("即时交流");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initViews() {
        mRecyclerView = findViewById(R.id.rc_view);
        sw_refresh = findViewById(R.id.sw_refresh);
    }

    private void initDatas() {
        //query();

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
                                        sw_refresh.setRefreshing(false);

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
        adapter = new ContactAdapter(this, mutlipleItem, null);
        mRecyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        setListener();
    }

    private void setListener() {
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                sw_refresh.setRefreshing(true);
                query();
            }
        });
        sw_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                query();
            }
        });
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
                Bundle bundle = new Bundle();
                Friend friend = adapter.getItem(position);
                bundle.putSerializable("friend", friend);
                Helper.switchActivity(ContactActivity.this, ChatActivity.class, bundle);
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

    private void query() {
        list.clear();
        getSignInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
