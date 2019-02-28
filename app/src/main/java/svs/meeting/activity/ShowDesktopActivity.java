package svs.meeting.activity;

import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;

import svs.meeting.app.R;
import svs.meeting.data.Config;
import svs.meeting.util.Helper;

public class ShowDesktopActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ImageView mImg;
    private TextView mTextName;
    private TextView mTextDept;
    private TextView mTextRose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_showdesktop);
        init();
    }

    private void init() {
        initActionBar();
        initViews();
        initDatas();
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("桌面显示");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initViews() {
        mImg = findViewById(R.id.img_bg);
        mTextName = findViewById(R.id.tv_name);
        mTextDept = findViewById(R.id.tv_dept);
        mTextRose = findViewById(R.id.tv_role);
    }

    private void initDatas() {
        try {
            final String url = Config.WEB_URL + "/" + Config.meetingInfo.getString("logo");
            Glide.with(ShowDesktopActivity.this)
                    .load(url)
                    .into(mImg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String name = Config.display_atts.getString("cardName");
            String dept = Config.display_atts.getString("cardDept");
            String rose = Config.display_atts.getString("cardRole");
            mTextName.setText(name);
            mTextDept.setText(dept);
            mTextRose.setText(rose);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.sign_statu == 0) {
                    Helper.switchActivity(ShowDesktopActivity.this, SignInActivity.class);
                } else {
                    finish();
                }
            }
        });
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
