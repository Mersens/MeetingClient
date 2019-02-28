package svs.meeting.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import svs.meeting.app.R;
import svs.meeting.util.XLog;
import svs.meeting.widgets.CommonHeader;

public class SecondFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_second, container, false);
        CommonHeader header=(CommonHeader)this.getChildFragmentManager().findFragmentById(R.id.header);
        header.setTitle("Test2");

        return view;
    }

}
