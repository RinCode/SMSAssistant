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
 * “清理”页面
 */

public class Clean extends Fragment {
    private List<SMS> smsData;//短信列表
    private RecyclerViewAdapter adapter;
    private RecyclerView smsList;
    private Context context;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SMSHandler smsHandler;
    private DefaultSMS defaultSMS;//默认短信app
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
            //如果从advanced_other界面调用
            arg = getArguments();
            method = arg.getString("method");
            if (Objects.equals(method, "time")) {
                start_time = arg.getLong("start_time");
                end_time = arg.getLong("end_time");
            } else if (Objects.equals(method, "contact")) {
                regex = arg.getString("regex");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().setTitle(R.string.fragment_clean);
        fragmentControl = (FragmentControl) getActivity().getApplicationContext();
        fragmentControl.setFabIconDel();
        fragmentControl.set_fragment_name(getResources().getString(R.string.fragment_clean));
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
                    new AlertDialog.Builder(context).setTitle(R.string.smslist_detail)
                            .setView(layout)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (smsHandler.deleteSms(smsData.get(position).getId().toString()) == 1) {
                                        smsData.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(context, R.string.delete_success, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, R.string.no_sms_try_again, Toast.LENGTH_SHORT).show();
                                        defaultSMS.SetDefault();
                                    }
                                }
                            })
                            .setNeutralButton(R.string.remove_from_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    smsData.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                } else {
                    Toast.makeText(context, R.string.no_data_del, Toast.LENGTH_SHORT).show();
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
            new AlertDialog.Builder(context).setTitle(R.string.warning)
                    .setMessage(getResources().getString(R.string.fragment_clean_willdel1) + " " + smsData.size() + " " + getResources().getString(R.string.fragment_clean_willdel2))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            progressDialog = new ProgressDialog(context);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle(R.string.deleting);
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
                                            Toast.makeText(context, R.string.fragment_clean_error, Toast.LENGTH_SHORT).show();
                                            defaultSMS.SetDefault();
                                            Looper.loop();
                                            return;
                                        }
                                        count += 1;
                                        progressDialog.setProgress((int) ((float) count / (float) smsData.size() * 100));
                                    }
                                    progressDialog.dismiss();
                                    Toast.makeText(context, R.string.fragment_clean_success, Toast.LENGTH_SHORT).show();
                                    defaultSMS.CancelDefault();
                                    Update update = new Update();
                                    update.execute();
                                    Looper.loop();
                                }
                            }.start();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        } else {
            Toast.makeText(context, R.string.no_data_del, Toast.LENGTH_SHORT).show();
        }
    }

    private class Update extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            SMSHandler smsHandler = new SMSHandler(context);
            if (Objects.equals(method, "time")) {
                smsHandler.setTime(start_time, end_time);
            }
            if (Objects.equals(method, "contact")) {
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
