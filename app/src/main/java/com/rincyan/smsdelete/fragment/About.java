package com.rincyan.smsdelete.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.utils.GlobalControl;

/**
 * Created by rin on 2017/6/16.
 * “关于”页面
 *
 */

public class About extends Fragment {
    private GlobalControl globalControl;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about, container, false);
        getActivity().setTitle(R.string.fragment_about);
        globalControl = (GlobalControl) getActivity().getApplicationContext();
        globalControl.setFabIconCancle();
        globalControl.set_fragment_name(getResources().getString(R.string.fragment_about));
        return view;
    }
}

