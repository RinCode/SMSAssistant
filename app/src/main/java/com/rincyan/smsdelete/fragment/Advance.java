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

import java.util.ArrayList;

/**
 * Created by rin on 2017/6/16.
 *
 */

public class Advance extends Fragment {
    private ListView listView;
    private SwitchCompat switchBtn;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> regexData;
    private SharedPreferences preferences;
    private SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_advance, container, false);
        listView = view.findViewById(R.id.advance_list);
        switchBtn = view.findViewById(R.id.switch_button);
        getActivity().setTitle("高级选项");
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
                new AlertDialog.Builder(getActivity()).setTitle("确认删除？")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int ix) {
                                try {
                                    db.execSQL("delete from regex where rule = '" + regexData.get(i) + "'");
                                    getRegexData();
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
        setSwitch();
    }

    public ArrayList<String> getRegexData() {
        regexData.clear();
        Cursor c = db.rawQuery("select * from regex", null);
        if (c != null) {
            while (c.moveToNext()) {
                regexData.add(c.getString(c.getColumnIndex("rule")));
            }
            c.close();
        }
        if (regexData.isEmpty()) {
            Toast.makeText(getActivity(), "没有规则，请点击右下角添加", Toast.LENGTH_SHORT).show();
        }
        return regexData;
    }

    private void setSwitch() {
        preferences = getActivity().getSharedPreferences("setting", getActivity().MODE_PRIVATE);
        Boolean checked = preferences.getBoolean("advance", false);
        if (checked) {
            switchBtn.setChecked(true);
        } else {
            switchBtn.setChecked(false);
        }
    }
}
