package com.rincyan.smsdelete.fragment;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.utils.FragmentControl;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rin on 2017/6/17.
 * “添加规则”页面
 * 用于添加正则表达式规则
 */

public class Addrule extends Fragment {
    private Button testBtn;//测试正则表达式的按钮
    private EditText rule;//规则
    private EditText text;//待匹配内容
    private TextView result;//匹配结果
    private SQLiteDatabase db;
    private FragmentControl fragmentControl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_addrule, container, false);
        testBtn = view.findViewById(R.id.testBtn);
        rule = view.findViewById(R.id.rule);
        text = view.findViewById(R.id.test_text);
        result = view.findViewById(R.id.result);
        db = getActivity().openOrCreateDatabase("smsdel.db", getActivity().MODE_PRIVATE, null);
        getActivity().setTitle(R.string.fragment_addrule);
        fragmentControl = (FragmentControl) getActivity().getApplicationContext();
        fragmentControl.setFabIconSava();
        fragmentControl.set_fragment_name(getResources().getString(R.string.fragment_addrule));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText(check());
            }
        });
    }

    private String check() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            //收回键盘
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        String result = "";
        try {
            if (rule.getText().toString().isEmpty()) {
                return getResources().getString(R.string.fragment_regex_no_regex);
            } else {
                Pattern p = Pattern.compile(rule.getText().toString());
                Matcher m = p.matcher(text.getText().toString());
                while (m.find()) {
                    result += getResources().getString(R.string.fragment_regex_find_result) + m.group() + "\n";
                }
                if (!Objects.equals(result, "")) {
                    return result;
                } else {
                    return getResources().getString(R.string.fragment_regex_no_match);
                }
            }
        } catch (Exception e) {
            return getResources().getString(R.string.fragment_regex_error_regex);
        }
    }

    public void save() {
        String srule = rule.getText().toString();
        if (srule.isEmpty()) {
            result.setText(R.string.fragment_regex_no_regex);
        }else {
            try {
                Pattern p = Pattern.compile(rule.getText().toString());
                Matcher m = p.matcher(text.getText().toString());
                m.find();
                try {
                    db.execSQL("insert into regex(rule) values('" + rule.getText().toString() + "')");
                    Toast.makeText(getActivity(),R.string.fragment_regex_save_successful,Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(getActivity(),R.string.fragment_regex_save_failed,Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                result.setText(R.string.fragment_regex_error_regex);
            }
        }
    }
}
