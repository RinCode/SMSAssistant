package com.rincyan.smsdelete.fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.utils.GlobalControl;

import java.util.ArrayList;

/**
 * Created by rin on 2017/6/16.
 *“高级设置-正则表达式模式”
 * 用于短信内容的正则表达式筛选
 */

public class Advance_regex extends Fragment {
    private ListView listView;//正则匹配项
    private SwitchCompat switchBtn;//模式开关
    private ArrayAdapter<String> adapter;
    private ArrayList<String> regexData;
    private SharedPreferences preferences;
    private SQLiteDatabase db;
    private GlobalControl globalControl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_advance_regex, container, false);
        listView = view.findViewById(R.id.advance_regex_list);
        switchBtn = view.findViewById(R.id.switch_button);
        getActivity().setTitle(R.string.fragment_regex_mode);
        globalControl = (GlobalControl)getActivity().getApplicationContext();
        globalControl.setFabIconAdd();
        globalControl.set_fragment_name(getResources().getString(R.string.fragment_regex_mode));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        db = getActivity().openOrCreateDatabase("smsdel.db", getActivity().MODE_PRIVATE, null);
        regexData = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getRegexData());
        listView.setAdapter(adapter);
        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //在preference中记录开关状态
                if (b) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("setting", getActivity().MODE_PRIVATE).edit();
                    editor.putBoolean("advance", true);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("setting", getActivity().MODE_PRIVATE).edit();
                    editor.putBoolean("advance", false);
                    editor.apply();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, final long l) {
                //删除匹配项
                new AlertDialog.Builder(getActivity()).setTitle(R.string.sure_delete)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int ix) {
                                try {
                                    db.execSQL("delete from regex where rule = '" + regexData.get(i) + "'");
                                    getRegexData();
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    Toast.makeText(getActivity(), R.string.delete_err, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
        setSwitch();
    }

    public ArrayList<String> getRegexData() {
        //读取数据库中的匹配项
        regexData.clear();
        Cursor c = db.rawQuery("select * from regex", null);
        if (c != null) {
            while (c.moveToNext()) {
                regexData.add(c.getString(c.getColumnIndex("rule")));
            }
            c.close();
        }
        if (regexData.isEmpty()) {
            Toast.makeText(getActivity(), R.string.fragment_regex_no_exist_regex, Toast.LENGTH_SHORT).show();
        }
        return regexData;
    }

    private void setSwitch() {
        //设置开关状态
        preferences = getActivity().getSharedPreferences("setting", getActivity().MODE_PRIVATE);
        Boolean checked = preferences.getBoolean("advance", false);
        if (checked) {
            switchBtn.setChecked(true);
        } else {
            switchBtn.setChecked(false);
        }
    }
}
