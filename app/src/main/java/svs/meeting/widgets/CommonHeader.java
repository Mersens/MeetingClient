package svs.meeting.widgets;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import svs.meeting.app.R;
import svs.meeting.util.Helper;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommonHeader extends Fragment {
    private LinearLayout leftArea;
    private LinearLayout rightArea;
    private TextView txtTitle;
    private TextView txtTask;
    private java.util.HashMap<Integer,View> buttonsMap=new java.util.HashMap<Integer, View>();
    private ProgressBar myProgress;
    private RelativeLayout myTask;
    private RelativeLayout layout;

    public CommonHeader() {
        // Required empty public constructor
    }

    public LinearLayout getLeftArea() {
        return leftArea;
    }

    public LinearLayout getRightArea() {
        return rightArea;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_common_header, container, false);
        Helper.setPaddingForStatusBar(view,this.getActivity());
        rightArea=(LinearLayout)view.findViewById(R.id.area_right);
        leftArea=(LinearLayout)view.findViewById(R.id.area_left);
        txtTitle=(TextView)view.findViewById(R.id.header_title);
        txtTask=(TextView)view.findViewById(R.id.tv_task);
        myTask= (RelativeLayout) view.findViewById(R.id.rl_task);
        myProgress = (ProgressBar)view.findViewById(R.id.pb_progressbar);
        layout = (RelativeLayout) view.findViewById(R.id.header_layout);
        showProgress(false);
        return view;
    }


    public void setTitle(String title){
        txtTitle.setText(title);
    }

    public void setMsgTask(String msg, View.OnClickListener listener){
        myTask.setVisibility(View.VISIBLE);
        txtTask.setText(msg);
        if (msg.equals("0")){
            txtTask.setVisibility(View.GONE);
        }
        myTask.setOnClickListener(listener);
    }

    public void addButtonToLeft(int imgId, View.OnClickListener listener){
        ImageButton btn=new ImageButton(this.getActivity());
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(Helper.Dp2Px(this.getActivity(),15),0,0,0);
        btn.setImageResource(imgId);
        btn.setLayoutParams(params);
        btn.setBackgroundResource(R.drawable.main_btn_bg);
        btn.setOnClickListener(listener);
        buttonsMap.put(imgId,btn);
        leftArea.addView(btn);
    }

    public ImageButton addButtonToRight(int imgId, View.OnClickListener listener){
        if(buttonsMap.containsKey(imgId)) {
            View _btn=buttonsMap.get(imgId);
            return (ImageButton) _btn;
        }
        ImageButton btn=new ImageButton(this.getActivity());
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,Helper.Dp2Px(this.getActivity(),0),0);
        if(imgId>10) {
            btn.setImageResource(imgId);
        }
        params.width=Helper.Dp2Px(this.getActivity(),40);
        params.height=Helper.Dp2Px(this.getActivity(),40);
        btn.setLayoutParams(params);
        btn.setBackgroundResource(R.drawable.main_btn_bg);
        btn.setOnClickListener(listener);
        buttonsMap.put(imgId,btn);
        rightArea.addView(btn);
        return btn;
    }

    public void addButton(int buttonId, String text, View.OnClickListener listener){
        if(buttonsMap.containsKey(buttonId))
            return;

        Button btn=new Button(this.getActivity());
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,Helper.Dp2Px(this.getActivity(),0),0);
        btn.setPadding(0,0,0,0);
        params.width=Helper.Dp2Px(this.getActivity(),40);
        params.height=Helper.Dp2Px(this.getActivity(),40);
        btn.setText(text);
        btn.setTextSize(16);
        btn.setLayoutParams(params);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundResource(R.drawable.main_btn_bg);
        btn.setOnClickListener(listener);
        buttonsMap.put(buttonId,btn);
        rightArea.addView(btn);
    }

    public void removeButton(int imgId){
        if(!buttonsMap.containsKey(imgId))
            return;
        try{
            View btn=buttonsMap.get(imgId);
            LinearLayout parent=(LinearLayout)btn.getParent();
            if(parent!=null)
                parent.removeView(btn);
            buttonsMap.remove(imgId);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    public void setProgress(int val){
        this.myProgress.setProgress(val);
    }

    public void showProgress(boolean visible){
        this.myProgress.setVisibility(visible? View.VISIBLE: View.GONE);
    }

    public RelativeLayout getLayout() {
        return layout;
    }

    public void setLayout(RelativeLayout layout) {
        this.layout = layout;
    }
}
