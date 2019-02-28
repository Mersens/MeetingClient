package svs.meeting.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import svs.meeting.app.R;
import svs.meeting.util.XLog;
import svs.meeting.widgets.CommonHeader;

public class FirstFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout view=(LinearLayout)inflater.inflate(R.layout.fragment_first, container, false);
        CommonHeader header=(CommonHeader)this.getChildFragmentManager().findFragmentById(R.id.header);
        header.setTitle("Test1");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }

}
