package com.rincyan.smsdelete.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.utils.DefaultSMS;
import com.rincyan.smsdelete.utils.GlobalControl;
import com.rincyan.smsdelete.utils.SMSHandler;

import java.util.ArrayList;

/**
 * Created by rin on 2017/08/01.
 *“高级设置”页面
 * 用于选取匹配模式
 *
 */

public class Advance extends Fragment {
    private ListView advance_method;//匹配模式列表
    private ArrayAdapter<String> adapter;
    private ArrayList<String> methodData;
    private Advance_regex advance_regex;//正则表达式模式
    private Advance_other advance_other;//其他模式
    private FragmentManager fragmentManager;
    private GlobalControl globalControl;
    private SMSHandler smsHandler;
    private ProgressDialog progressDialog;
    private DefaultSMS defaultSMS;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_advance, container, false);
        advance_method = view.findViewById(R.id.advancedmethod);
        fragmentManager = getActivity().getSupportFragmentManager();
        getActivity().setTitle(getResources().getString(R.string.fragment_advance));
        globalControl = (GlobalControl) getActivity().getApplicationContext();
        globalControl.setFabIconCancle();
        globalControl.set_fragment_name(getResources().getString(R.string.fragment_advance));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        final GlobalControl globalControl = (GlobalControl) getActivity().getApplicationContext();
        smsHandler = new SMSHandler(getActivity());
        defaultSMS = new DefaultSMS(getActivity());
        methodData = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getMethod());
        advance_method.setAdapter(adapter);
        advance_method.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        advance_regex = new Advance_regex();
                        globalControl.set_fragment_name(getResources().getString(R.string.fragment_regex_mode));
                        globalControl.setFragment(advance_regex);
                        fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, advance_regex).commit();
                        break;
                    case 1:
                        advance_other = new Advance_other();
                        globalControl.set_fragment_name(getResources().getString(R.string.fragment_other_mode));
                        globalControl.setFragment(advance_other);
                        fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, advance_other).commit();
                        break;
                    case 2:
                        if(defaultSMS.isDefault()) {
                            progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.setTitle("处理中");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            new Thread() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    ReadAll readAll = new ReadAll();
                                    readAll.execute();
                                    Looper.loop();
                                }

                            }.start();
                        }else {
                            Toast.makeText(getActivity(),"请设置为默认短信应用",Toast.LENGTH_SHORT).show();
                            defaultSMS.SetDefault();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        super.onActivityCreated(savedInstanceState);
    }
    
    private ArrayList<String> getMethod(){
        methodData.clear();
        methodData.add(getResources().getString(R.string.fragment_regex_mode));
        methodData.add(getResources().getString(R.string.fragment_other_mode));
        methodData.add("标记所有短信已读");
        return methodData;
    }

    private class ReadAll extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            return smsHandler.readAll();
        }

        @Override
        protected void onPostExecute(Object o) {
            if ((boolean) o){
                Toast.makeText(getActivity(),"成功",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getActivity(),"出现错误",Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
            defaultSMS.CancelDefault();
        }
    }
}
