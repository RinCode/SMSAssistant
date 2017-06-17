package com.rincyan.smsdelete.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rincyan.smsdelete.R;

/**
 * Created by rin on 2017/6/16.
 *
 */

public class About extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about, container, false);
        getActivity().setTitle("关于");
        return view;
    }
}
