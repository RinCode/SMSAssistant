package com.rincyan.smsdelete.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.utils.GlobalControl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by rin on 2017/6/16.
 * “高级设置-正则表达式模式”
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
    private Button checkupdate;
    private Button cleanall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_advance_regex, container, false);
        listView = view.findViewById(R.id.advance_regex_list);
        switchBtn = view.findViewById(R.id.switch_button);
        checkupdate = view.findViewById(R.id.checkupdate);
        cleanall = view.findViewById(R.id.clean);
        getActivity().setTitle(R.string.fragment_regex_mode);
        globalControl = (GlobalControl) getActivity().getApplicationContext();
        globalControl.setFabIconAdd();
        globalControl.set_fragment_name(getResources().getString(R.string.fragment_regex_mode));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        regexData = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, regexData);
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
                                    db = getActivity().openOrCreateDatabase("smsdel.db", getActivity().MODE_PRIVATE, null);
                                    db.execSQL("delete from regex where rule = '" + regexData.get(i) + "'");
                                    db.close();
                                    getRegexData();
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
        checkupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUpdate();
            }
        });
        cleanall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.warning)
                        .setMessage(R.string.advance_regex_clear_rules)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    db = getActivity().openOrCreateDatabase("smsdel.db", getActivity().MODE_PRIVATE, null);
                                    db.execSQL("delete from regex");
                                    db.close();
                                    getRegexData();
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
        getRegexData();
    }

    public void getRegexData() {
        //读取数据库中的匹配项
        regexData.clear();
        db = getActivity().openOrCreateDatabase("smsdel.db", getActivity().MODE_PRIVATE, null);
        Cursor c = db.rawQuery("select * from regex", null);
        if (c != null) {
            while (c.moveToNext()) {
                regexData.add(c.getString(c.getColumnIndex("rule")));
            }
            c.close();
        }
//        if (regexData.isEmpty()) {
//            Toast.makeText(getActivity(), R.string.fragment_regex_no_exist_regex, Toast.LENGTH_SHORT).show();
//        }
        db.close();
        adapter.notifyDataSetChanged();
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

    public void checkUpdate() {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest request = new JsonObjectRequest("https://api.rincyan.com/app/sms_rule.php", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Message msg = Message.obtain();
                msg.obj = jsonObject;
                mGetVersionHandler.sendMessage(msg);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), R.string.advance_regex_network_error, Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(request);
    }

    @SuppressLint("HandlerLeak")
    private Handler mGetVersionHandler = new Handler() {
        public void handleMessage(Message msg) {
            JSONObject jsonObject = (JSONObject) msg.obj;
            try {
                String error = jsonObject.getString("error");
                if (error.equals("1")) {
                    Toast.makeText(getActivity(), R.string.advance_regex_server_error, Toast.LENGTH_SHORT).show();
                } else {
                    JSONArray data = jsonObject.getJSONArray("data");
                    db = getActivity().openOrCreateDatabase("smsdel.db", getActivity().MODE_PRIVATE, null);
                    System.out.print(data);
                    for (int i =0;i<data.length();i++){
                        String value = data.getString(i);
                        if (!regexData.contains(value)) {
                            try {
                                db.execSQL("insert into regex(rule) values('" + value + "')");
                            } catch (Exception e) {
                                Toast.makeText(getActivity(), R.string.fragment_regex_save_failed, Toast.LENGTH_SHORT).show();
                                db.close();
                                return;
                            }
                        }
                    }
                    db.close();
                    Toast.makeText(getActivity(), R.string.advance_regex_online_rules_updated, Toast.LENGTH_SHORT).show();
                    getRegexData();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
