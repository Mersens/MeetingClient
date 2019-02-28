package svs.meeting.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.promeg.pinyinhelper.Pinyin;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import svs.meeting.app.MainActivity;
import svs.meeting.app.MyApplication;
import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.data.EventEntity;
import svs.meeting.data.Friend;
import svs.meeting.data.MsgEntity;
import svs.meeting.data.MsgType;
import svs.meeting.data.User;
import svs.meeting.db.DBDao;
import svs.meeting.db.DBDaoImpl;
import svs.meeting.fragments.ContactFragment;
import svs.meeting.fragments.MsgFragment;
import svs.meeting.listener.onUserSelectListener;
import svs.meeting.widgets.TipsDialogFragment;

public class ServiceActivity extends BaseActivity implements onUserSelectListener {
    private Toolbar mToolbar;
    private Fragment fragments[];
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_service);
        init();
    }

    private void init(){
        initActionBar();
        initViews();
    }

    private String getNowTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }
    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("服务端");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initViews() {
        fragments=new Fragment[2];
        fragments[0]=ContactFragment.getInstance();
        fragments[1]=MsgFragment.getInstance(getDefault());
        //联系人
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        ft.replace(R.id.frame_contact,fragments[0]);
        //聊天信息
        ft.replace(R.id.frame_msg,fragments[1]);
        ft.commitAllowingStateLoss();
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
            showTipsView("确定退出系统？");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showTipsView(String msg){
        final TipsDialogFragment dialogFragment=TipsDialogFragment.getInstance(msg);
        dialogFragment.show(getSupportFragmentManager(),"showTipsView");
        dialogFragment.setOnDialogClickListener(new TipsDialogFragment.OnDialogClickListener() {
            @Override
            public void onClickCancel() {
                dialogFragment.dismissAllowingStateLoss();
            }
            @Override
            public void onClickOk() {
                dialogFragment.dismissAllowingStateLoss();
                MyApplication.getInstance().exit();
            }
        });

    }
    @Override
    public void onBackPressed() {
        showTipsView("确定退出系统？");
    }
    @Override
    public void onUserSelect(Friend friend) {
        //联系人
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        //聊天信息
        ft.replace(R.id.frame_msg,MsgFragment.getInstance(friend));
        ft.commitAllowingStateLoss();
    }

    private Friend getDefault(){
        Friend friend = new Friend();
        friend.setUname("所有人");
        User user = new User();
        user.setUsername("所有人");
        friend.setFriendUser(user);
        String pinyin = Pinyin.toPinyin("所有人".charAt(0));
        friend.setPinyin(pinyin.substring(0, 1).toUpperCase());
        return friend;
    }


}
