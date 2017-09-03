package com.rincyan.smsdelete.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.utils.FragmentControl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rin on 2017/08/01.
 * “高级设置-其他模式”
 * 用于用户以时间段对短信进行筛选
 */

public class Advance_other extends Fragment {
    private TextView start_time;
    private TextView end_time;
    private Button time_search;//时间段模式
    private EditText regex;
    private Button regex_search;//联系人正则表达式模式
    private FragmentManager fragmentManager;
    private FragmentControl fragmentControl;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_advance_other, container, false);
        getActivity().setTitle(R.string.fragment_other_mode);
        start_time = view.findViewById(R.id.start_time);
        end_time = view.findViewById(R.id.end_time);
        time_search = view.findViewById(R.id.time_search);
        regex = view.findViewById(R.id.contact_rule);
        regex_search = view.findViewById(R.id.contact_search);
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentControl = (FragmentControl) getActivity().getApplicationContext();
        fragmentControl.setFabIconCancle();
        fragmentControl.set_fragment_name(getResources().getString(R.string.fragment_other_mode));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        start_time.setText(i + "-" + (i1 + 1) + "-" + i2);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        end_time.setText(i + "-" + (i1 + 1) + "-" + i2);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        time_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
                Date d;
                try {
                    d = sdf.parse(start_time.getText().toString());
                    long start = d.getTime();
                    d = sdf.parse(end_time.getText().toString());
                    long end = d.getTime();
                    if (end > start) {
                        Clean clean = new Clean();
                        Bundle bundle = new Bundle();
                        bundle.putString("method", "time");
                        bundle.putLong("start_time",start);
                        bundle.putLong("end_time",end);
                        clean.setArguments(bundle);
                        fragmentControl.setFragment(clean);
                        fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, clean).commit();
                    } else {
                        Toast.makeText(getActivity(), R.string.fragment_other_time_timeerr, Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    Toast.makeText(getActivity(), R.string.fragment_other_time_crash, Toast.LENGTH_SHORT).show();
                }
            }
        });
        regex_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Pattern p = Pattern.compile(regex.getText().toString());
                    Matcher m = p.matcher("");
                    m.find();
                    Clean clean = new Clean();
                    Bundle bundle = new Bundle();
                    bundle.putString("method", "contact");
                    bundle.putString("regex",regex.getText().toString());
                    clean.setArguments(bundle);
                    fragmentControl.setFragment(clean);
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(regex.getWindowToken(),0);
                    fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, clean).commit();
                }catch (Exception e){
                    Toast.makeText(getActivity(),"语法错误",Toast.LENGTH_SHORT).show();
                }
            }
        });
        super.onActivityCreated(savedInstanceState);
    }
}
