package com.rincyan.smsdelete.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.utils.GlobalControl;

/**
 * Created by rin on 2017/09/09.
 *
 */

public class Recognize extends Fragment {
    private GlobalControl globalControl;
    private SwitchCompat switchBtn;
    private SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_recognize, container, false);
        getActivity().setTitle(R.string.fragment_recognize);
        switchBtn = view.findViewById(R.id.switch_button);
        globalControl = (GlobalControl) getActivity().getApplicationContext();
        globalControl.setFabIconCancle();
        globalControl.set_fragment_name(getResources().getString(R.string.fragment_recognize));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSwitch();
        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //在preference中记录开关状态
                if (b) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("setting", getActivity().MODE_PRIVATE).edit();
                    editor.putBoolean("recognize", true);
                    editor.apply();
                    globalControl.startListen();
                } else {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("setting", getActivity().MODE_PRIVATE).edit();
                    editor.putBoolean("recognize", false);
                    editor.apply();
                    globalControl.stopListen();
                }
            }
        });
    }

    private void setSwitch() {
        //设置开关状态
        preferences = getActivity().getSharedPreferences("setting", getActivity().MODE_PRIVATE);
        Boolean checked = preferences.getBoolean("recognize", false);
        if (checked) {
            switchBtn.setChecked(true);
        } else {
            switchBtn.setChecked(false);
        }
    }
}
