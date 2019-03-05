package svs.meeting.widgets;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import svs.meeting.app.R;


/**
 * Created by Administrator on 2018/3/22.
 */

public class TipsDialogFragment extends DialogFragment {

    private TextView mTextOk;
    private TextView mTextCancel;
    private TextView mTextMsg;
    private OnDialogClickListener listener;
    private String okText;
    private String cancelText;

    public static TipsDialogFragment getInstance(String msg,String cancelText,String okText){
        TipsDialogFragment fragment=new TipsDialogFragment();
        Bundle bundle=new Bundle();
        bundle.putString("msg",msg);
        bundle.putString("cancelText",cancelText);
        bundle.putString("okText",okText);
        fragment.setArguments(bundle);
        return fragment;
    }
    public static TipsDialogFragment getInstance(String msg){
        return TipsDialogFragment.getInstance(msg,"","");
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        return inflater.inflate(R.layout.layout_tips,container,true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        mTextMsg=(TextView)view.findViewById(R.id.tv_msg);

        mTextOk=(TextView)view.findViewById(R.id.tv_ok);
        mTextCancel=(TextView)view.findViewById(R.id.tv_cancel);
        mTextOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null){
                    listener.onClickOk();
                }
            }
        });
        mTextCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null){
                    listener.onClickCancel();
                }
            }
        });
        Bundle bundle=getArguments();
        if(bundle!=null){
            String msg=bundle.getString("msg");
            okText=bundle.getString("okText");
            if(!TextUtils.isEmpty(okText)){
                mTextOk.setText(okText);
            }

            cancelText=bundle.getString("cancelText");
            if(!TextUtils.isEmpty(cancelText)){
                mTextCancel.setText(cancelText);
            }
            if(!TextUtils.isEmpty(msg)){
                mTextMsg.setText(msg);
            }
        }
    }

    public void setCancelText(String str){

    }
    public void setOkText(String str){
        mTextOk.setText(str);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    public void setOnDialogClickListener(OnDialogClickListener listener){
        this.listener=listener;
    }
    public interface OnDialogClickListener {
        void onClickCancel();
        void onClickOk();

    }

}
