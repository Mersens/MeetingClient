package svs.meeting.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import svs.meeting.app.R;
import svs.meeting.util.Calculator;

public class CalculatorActivity extends BaseActivity {
    private Toolbar mToolbar;
    private String downStr = "";
    private boolean last_input = false;// 上一次输入是否为等号
    private static final String TAG = "MainActivity";

    @BindViews({R.id.num_0, R.id.num_1, R.id.num_2, R.id.num_3,
            R.id.num_4, R.id.num_5, R.id.num_6, R.id.num_7,
            R.id.num_8, R.id.num_9, R.id.num_dot, R.id.sum,
            R.id.sub, R.id.multi, R.id.divider, R.id.equl,
            R.id.clear, R.id.delete})
    List<Button> buttons;

    @BindView(R.id.editText2)
    EditText downText;// 显示计算的结果

    @BindView(R.id.editText1)
    EditText upText;// 显示计算的式子

    @BindView(R.id.keyboard_area)
    LinearLayout keyboardLayout;

    @BindView(R.id.layout_display)
    LinearLayout displayLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_calculator);
        ButterKnife.bind(this);
        init();
        if (savedInstanceState != null) {
            upText.setText(savedInstanceState.getString("upText"));
            downText.setText(savedInstanceState.getString("downText"));
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("upText", upText.getText().toString());
        outState.putString("downText", downText.getText().toString());
    }
    private void init() {
        initActionBar();
        initViews();
        initEvent();
        initDatas();
    }

    private void initEvent() {
        setListenerNumberBtn();
        setListenerFunctionBtn();
        setButtonsStyle();
    }

    private void initActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("计算器");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    private void initViews() {
    }

    private void initDatas() {
    }

    /**
     * 为数字按钮设置监听器
     */
    public void setListenerNumberBtn() {
        // 初始化数字按钮
        for (int i = 0; i < 10; i++) {
            final int index = i;
            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (last_input) {// 如果上一次输入的是等号，则再按数字键表示计算新的式子
                        downStr = "";
                        last_input = false;
                    }
                    downStr += buttons.get(index).getText();
                    downText.setText(downStr);
                    downText.setSelection(downStr.length());
                }
            });
        }
    }

    public void setListenerFunctionBtn() {
        // 清除
        buttons.get(16).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downStr = "";
                downText.setText("0");
                upText.setText("");
                last_input = false;
            }
        });

        // 退格
        buttons.get(17).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downStr.length() == 0) {
                    return;
                }
                downStr = downStr.substring(0, downStr.length() - 1);
                downText.setText(downStr);
                downText.setSelection(downStr.length());
                last_input = false;
            }
        });

        // 除法
        buttons.get(14).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downStr += buttons.get(14).getText();
                downText.setText(downStr);
                downText.setSelection(downStr.length());
                last_input = false;
            }
        });

        // 乘法
        buttons.get(13).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downStr += buttons.get(13).getText();
                downText.setText(downStr);
                downText.setSelection(downStr.length());
                last_input = false;
            }
        });

        // 减法
        buttons.get(12).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downStr += buttons.get(12).getText();
                downText.setText(downStr);
                downText.setSelection(downStr.length());
                last_input = false;
            }
        });

        // 加法
        buttons.get(11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downStr += buttons.get(11).getText();
                downText.setText(downStr);
                downText.setSelection(downStr.length());
                last_input = false;
            }
        });

        // 点
        buttons.get(10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downStr += buttons.get(10).getText();
                downText.setText(downStr);
                downText.setSelection(downStr.length());
                last_input = false;
            }
        });

        // 等号
        buttons.get(15).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (last_input) {// 若上一次输入的是等号，则不做任何操作
                    return;
                }

                // 小动画
                AnimationSet animationSet = new AnimationSet(true);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                alphaAnimation.setDuration(200);
                TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -150f);
                translateAnimation.setDuration(200);
                animationSet.addAnimation(alphaAnimation);
                animationSet.addAnimation(translateAnimation);
                downText.startAnimation(animationSet);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // 在上面的editText显示计算表达式
                        upText.setText(downStr + "=");
                        upText.setSelection(downStr.length() + 1);
                        try {
                            downStr = Calculator.calculate(downStr);
                            downText.setText(downStr);
                            downText.setSelection(downStr.length());
                        } catch (Exception e) {
                            downText.setText("算式错误");
                            downStr = "";
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                last_input = true;
            }
        });
    }

    /**
     * 设置按钮的样式 主要是大小的适配
     */
    public void setButtonsStyle() {

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int screen_height = wm.getDefaultDisplay().getHeight();

        keyboardLayout.getLayoutParams().height = screen_height / 2;
        displayLayout.getLayoutParams().height = screen_height / 3;

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setWidth(keyboardLayout.getWidth() / 4);
            buttons.get(i).setHeight(keyboardLayout.getHeight() / 5);
            Log.d(TAG, "setButtonsStyle: " + buttons.get(i).getWidth());
            Log.d(TAG, "setButtonsStyle: " + buttons.get(i).getHeight());
        }

        buttons.get(0).setWidth( keyboardLayout.getWidth() / 2);
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
