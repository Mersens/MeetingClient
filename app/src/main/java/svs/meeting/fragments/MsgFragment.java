package svs.meeting.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.adapter.ChatAdapter;
import svs.meeting.adapter.OnRecyclerViewListener;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.Friend;
import svs.meeting.data.MsgEntity;
import svs.meeting.data.MsgType;
import svs.meeting.db.DBDao;
import svs.meeting.db.DBDaoImpl;
import svs.meeting.service.MqttManagerV3;
import svs.meeting.util.RxBus;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class MsgFragment extends Fragment {
    private CompositeDisposable mCompositeDisposable;
     TextView mTextTitle;

    @BindView(R.id.ll_chat)
    LinearLayout ll_chat;

    @BindView(R.id.rc_view)
    RecyclerView rc_view;

    @BindView(R.id.edit_msg)
    EditText edit_msg;

    @BindView(R.id.btn_chat_add)
    Button btn_chat_add;
    @BindView(R.id.btn_chat_emo)
    Button btn_chat_emo;
    @BindView(R.id.btn_speak)
    Button btn_speak;
    @BindView(R.id.btn_chat_voice)
    Button btn_chat_voice;
    @BindView(R.id.btn_chat_keyboard)
    Button btn_chat_keyboard;
    @BindView(R.id.btn_chat_send)
    Button btn_chat_send;

    @BindView(R.id.layout_more)
    LinearLayout layout_more;
    @BindView(R.id.layout_add)
    LinearLayout layout_add;
    @BindView(R.id.layout_emo)
    LinearLayout layout_emo;

    // 语音有关
    @BindView(R.id.layout_record)
    RelativeLayout layout_record;
    @BindView(R.id.tv_voice_tips)
    TextView tv_voice_tips;
    @BindView(R.id.iv_record)
    ImageView iv_record;
    private Drawable[] drawable_Anims;// 话筒动画
    protected LinearLayoutManager layoutManager;
    ChatAdapter adapter;
    private String topic="svs/all";
    private String from_seat;
    private String meeting_id;
    private String uname;
    private DBDao dao;
    List<MsgEntity> msgList=new ArrayList<>();
    Friend friend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_chat,null);
        ButterKnife.bind(this,v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view){
        friend=(Friend)getArguments().getSerializable("friend");
        String name=friend.getUname();
        mTextTitle=view.findViewById(R.id.tv_title);
        mTextTitle.setText("与"+name+"的对话");
        dao=new DBDaoImpl(getActivity());
        try {
            from_seat=friend.getSeat_no();
            if(TextUtils.isEmpty(from_seat)){
                from_seat="";
            }
            meeting_id=Config.meetingInfo.getString("id");
            uname=Config.clientInfo.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initSwipeLayout();
        initBottomView();
        initRxbus();
        initDatas();
    }

    private void initDatas() {
        msgList.clear();
        List<MsgEntity> list=dao.findMsgById(topic,from_seat,MsgType.MSG_SERVICE,meeting_id);
        if(list!=null){
            if(list.size()>0){
                msgList.addAll(list);
                adapter.addMessages(msgList);
                layoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
            }
        }


    }

    private void initRxbus() {
        mCompositeDisposable = new CompositeDisposable();
        //监听订阅事件
        Disposable d = RxBus.getInstance().toObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (o instanceof EventEntity) {
                            EventEntity e = (EventEntity) o;
                            String type = e.type;
                            String v = e.value;
                            if (type.equals(EventEntity.MQTT_MSG)) {
                                EventEntity.MQEntity mqEntity= e.getMqEntity();
                                if(MsgType.MSG_SERVICE.equals(mqEntity.getMsgType())){
                                    String name=Config.clientInfo.getString("name");
                                    String content=mqEntity.getContent();
                                    String strs[]=content.split(";");
                                    String uname=strs[1];
                                    if(name.equals(uname)){
                                        return;
                                    }
                                    String seat_no=strs[2];
                                    String time=strs[3];
                                    String ip=strs[4];
                                    String c=strs[0];
                                    JSONObject object=new JSONObject(c);
                                    String str=object.getString("strContent");
                                    MsgEntity entity=new MsgEntity();
                                    entity.setPid(seat_no);
                                    entity.setMsg_time(getNowTime());
                                    entity.setMsg_type(MsgType.MSG_SERVICE);
                                    entity.setMsg(str);
                                    entity.setTopic(mqEntity.getTopic());
                                    entity.setFrom_name(uname);
                                    entity.setFrom_seat(seat_no);
                                    entity.setMeeting_id(meeting_id);
                                    entity.setSid(time);
                                    entity.setOid("");
                                    entity.setType(2);
                                    dao.addMsgInfo(entity);
                                    adapter.addMessage(entity);
                                    scrollToBottom();
                                }
                            }
                        }
                    }
                });
        //subscription交给compositeSubscription进行管理，防止内存溢出
        mCompositeDisposable.add(d);
    }

    private void initSwipeLayout() {
        layoutManager = new LinearLayoutManager(getActivity());
        rc_view.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(getActivity());
        rc_view.setAdapter(adapter);
        ll_chat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ll_chat.getViewTreeObserver().removeGlobalOnLayoutListener(this);

            }
        });

        //设置RecyclerView的点击事件
        adapter.setOnRecyclerViewListener(new OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public boolean onItemLongClick(int position) {
                //TODO 消息：5.3、删除指定聊天消息
                //adapter.remove(position);
                return true;
            }
        });
    }
    @OnClick(R.id.btn_chat_send)
    public void onSendClick(View view) {
        sendMessage();
    }
    private void sendMessage() {
        String text = edit_msg.getText().toString();
        if (TextUtils.isEmpty(text.trim())) {
            return;
        }
        MsgEntity entity=new MsgEntity();
        entity.setMsg(text);
        entity.setType(1);
        adapter.addMessage(entity);
        saveDataToLoc(text);
        edit_msg.setText("");
        scrollToBottom();
    }

    private void saveDataToLoc(String msg){
        MsgEntity entity=new MsgEntity();
        entity.setPid(from_seat);
        entity.setMsg_time(getNowTime());
        entity.setMsg_type(MsgType.MSG_SERVICE);
        entity.setMsg(msg);
        entity.setTopic(topic);
        entity.setFrom_name(uname);
        entity.setFrom_seat(from_seat);
        entity.setMeeting_id(meeting_id);
        entity.setSid(getSid());
        entity.setOid("");
        entity.setType(1);
        dao.addMsgInfo(entity);
        sendInfo(entity);
    }

    private void sendInfo(MsgEntity entity){
        String split = "\\~^";
        MqttManagerV3 mqtt = MqttManagerV3.getInstance();
        JSONObject object = new JSONObject();
        try {
            object.put("strContent",entity.getMsg());
            object.put("type",friend.getIp_addr()==null?"all":friend.getIp_addr());
            String ip=friend.getIp_addr()==null?"":friend.getIp_addr();
            Log.e("sendInfo ip","ip=="+ip);
            String strMsg = uname + split + from_seat + split + MsgType.MSG_SERVICE + split + object.toString() + split + new Date().getTime() + split +ip ;
            mqtt.send(strMsg, "");
            Log.e("sendInfo","strMsg=="+strMsg);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initBottomView() {
        edit_msg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
                    scrollToBottom();
                }
                return false;
            }
        });
        edit_msg.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                scrollToBottom();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    btn_chat_send.setVisibility(View.VISIBLE);
                    btn_chat_keyboard.setVisibility(View.GONE);
                    btn_chat_voice.setVisibility(View.GONE);
                } else {
                    if (btn_chat_voice.getVisibility() != View.VISIBLE) {
                        btn_chat_voice.setVisibility(View.VISIBLE);
                        btn_chat_send.setVisibility(View.GONE);
                        btn_chat_keyboard.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    @OnClick(R.id.btn_chat_keyboard)
    public void onKeyClick(View view) {
        showEditState(false);
    }
    private String getSid(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date());
    }
    private String getNowTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private void showEditState(boolean isEmo) {
        edit_msg.setVisibility(View.VISIBLE);
        btn_chat_keyboard.setVisibility(View.GONE);
        btn_chat_voice.setVisibility(View.VISIBLE);
        btn_speak.setVisibility(View.GONE);
        edit_msg.requestFocus();
        if (isEmo) {
            layout_more.setVisibility(View.VISIBLE);
            layout_more.setVisibility(View.VISIBLE);
            layout_emo.setVisibility(View.VISIBLE);
            layout_add.setVisibility(View.GONE);
            hideSoftInputView();
        } else {
            layout_more.setVisibility(View.GONE);
            showSoftInputView();
        }
    }

    private void scrollToBottom() {
        layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
    }
    /**隐藏软键盘-一般是EditText.getWindowToken()
     * @param token
     */
    public void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager)getActivity(). getSystemService(INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    /**
     * 显示软键盘
     */
    public void showSoftInputView() {
        if (getActivity().getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                ((InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE))
                        .showSoftInput(edit_msg, 0);
        }
    }
    /**
     * 隐藏软键盘
     */
    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    public static MsgFragment getInstance(Friend friend){
        MsgFragment fragment=new MsgFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable("friend",friend);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }
}
