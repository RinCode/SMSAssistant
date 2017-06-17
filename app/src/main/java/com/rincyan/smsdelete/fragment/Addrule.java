package com.rincyan.smsdelete.fragment;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rincyan.smsdelete.R;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rin on 2017/6/17.
 */

public class Addrule extends Fragment {
    private Button testBtn;
    private EditText rule;
    private EditText text;
    private TextView result;
    private SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_addrule, container, false);
        testBtn = view.findViewById(R.id.testBtn);
        rule = view.findViewById(R.id.rule);
        text = view.findViewById(R.id.test_text);
        result = view.findViewById(R.id.result);
        db = getActivity().openOrCreateDatabase("smsdel.db", getActivity().MODE_PRIVATE, null);
        getActivity().setTitle("添加规则");
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
        String result = "";
        try {
            if (rule.getText().toString().isEmpty()) {
                return "请输入正则表达式";
            } else {
                Pattern p = Pattern.compile(rule.getText().toString());
                Matcher m = p.matcher(text.getText().toString());
                while (m.find()) {
                    result += "匹配到结果：" + m.group() + "\n";
                }
                if (!Objects.equals(result, "")) {
                    return result;
                } else {
                    return "无匹配";
                }
            }
        } catch (Exception e) {
            return "正则表达式可能语法错误";
        }
    }

    public void save() {
        String srule = rule.getText().toString();
        if (srule.isEmpty()) {
            result.setText("请输入正则表达式");
        }else {
            try {
                Pattern p = Pattern.compile(rule.getText().toString());
                Matcher m = p.matcher(text.getText().toString());
                m.find();
                try {
                    db.execSQL("insert into regex(rule) values('" + rule.getText().toString() + "')");
                    Toast.makeText(getActivity(),"保存成功",Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(getActivity(),"保存失败",Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                result.setText("正则表达式不正确");
            }
        }
    }
}
