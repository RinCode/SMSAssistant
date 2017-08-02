package com.rincyan.smsdelete.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.recyclerview.RecyclerViewAdapter;
import com.rincyan.smsdelete.recyclerview.SMS;
import com.rincyan.smsdelete.utils.DefaultSMS;
import com.rincyan.smsdelete.utils.FragmentControl;
import com.rincyan.smsdelete.utils.SMSHandler;
import com.rincyan.smsdelete.utils.isCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by rin on 2017/6/16.
 * 清理界面
 */

public class Clean extends Fragment {
    private List<SMS> smsData;
    private RecyclerViewAdapter adapter;
    private RecyclerView smsList;
    private Context context;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SMSHandler smsHandler;
    private DefaultSMS defaultSMS;
    private String method = "null";
    private Long start_time = null;
    private Long end_time = null;
    private String regex = "";
    private Bundle arg;
    private FragmentControl fragmentControl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_clean, container, false);
        context = getActivity();
        smsList = view.findViewById(R.id.smsList);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        smsData = new ArrayList<>();
        adapter = new RecyclerViewAdapter((smsData));
        smsList.setAdapter(adapter);
        smsList.setLayoutManager(new LinearLayoutManager(context));
        smsHandler = new SMSHandler(context);
        defaultSMS = new DefaultSMS(context);
        try {
            arg = getArguments();
            method = arg.getString("method");
            if (Objects.equals(method, "time")) {
                start_time = arg.getLong("start_time");
                end_time = arg.getLong("end_time");
            }else if(Objects.equals(method, "contact")){
                regex = arg.getString("regex");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().setTitle("清理");
        fragmentControl = (FragmentControl) getActivity().getApplicationContext();
        fragmentControl.setFabIconDel();
        fragmentControl.set_fragment_name("清理");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                if (!checkEmpty()) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View layout = inflater.inflate(R.layout.detail_dialog_layout, null);
                    TextView detail_num = layout.findViewById(R.id.detail_num);
                    TextView detail_date = layout.findViewById(R.id.detail_date);
                    TextView detail_body = layout.findViewById(R.id.detail_body);
                    detail_num.setText(smsData.get(position).getNum());
                    detail_date.setText(smsData.get(position).getDate());
                    detail_body.setText(smsData.get(position).getBody());
                    detail_body.setMovementMethod(ScrollingMovementMethod.getInstance());
                    new AlertDialog.Builder(context).setTitle("详细信息")
                            .setView(layout)
                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (smsHandler.deleteSms(smsData.get(position).getId().toString()) == 1) {
                                        smsData.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "请设置为默认短信应用后重新尝试", Toast.LENGTH_SHORT).show();
                                        defaultSMS.SetDefault();
                                    }
                                }
                            })
                            .setNeutralButton("移出删除列表", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    smsData.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                } else {
                    Toast.makeText(context, "没有数据可以删除", Toast.LENGTH_SHORT).show();
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Update update = new Update();
                update.execute();
            }
        });
        Update update = new Update();
        swipeRefreshLayout.setRefreshing(true);
        update.execute();
    }

    public void deleteAll() {
        if (!checkEmpty()) {
            new AlertDialog.Builder(context).setTitle("警告")
                    .setMessage("本操作将删除表内全部内容，共"+smsData.size()+"项，确认删除？")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            progressDialog = new ProgressDialog(context);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle("正在删除");
                            progressDialog.setMax(100);
                            progressDialog.setProgress(0);
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            new Thread() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    int flag = 0;
                                    int count = 0;
                                    SMSHandler smsHandler = new SMSHandler(context);
                                    for (SMS tmp : smsData) {
                                        flag = smsHandler.deleteSms(tmp.getId().toString());
                                        if (flag != 1) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "删除失败，请设置为默认短信应用后重试", Toast.LENGTH_SHORT).show();
                                            defaultSMS.SetDefault();
                                            Looper.loop();
                                            return;
                                        }
                                        count += 1;
                                        progressDialog.setProgress((int) ((float) count / (float) smsData.size() * 100));
                                    }
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "删除成功，退出应用前请恢复默认短信应用", Toast.LENGTH_SHORT).show();
                                    defaultSMS.CancelDefault();
                                    Update update = new Update();
                                    update.execute();
                                    Looper.loop();
                                }
                            }.start();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        } else {
            Toast.makeText(context, "没有数据可以删除", Toast.LENGTH_SHORT).show();
        }
    }

    private class Update extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            SMSHandler smsHandler = new SMSHandler(context);
            if (Objects.equals(method, "time")) {
                smsHandler.setTime(start_time, end_time);
            }if(Objects.equals(method,"contact")){
                smsHandler.setRegex(regex);
            }
            return smsHandler.getSMS(method);

        }

        @Override
        protected void onPostExecute(Object o) {
            smsData.clear();
            smsData.addAll((List<SMS>) o);
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private boolean checkEmpty() {
        return smsData.size() == 0 || smsData.size() == 1 & smsData.get(0).getId() == -1;
    }
}
