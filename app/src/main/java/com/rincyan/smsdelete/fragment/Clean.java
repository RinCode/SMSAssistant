package com.rincyan.smsdelete.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.recyclerview.RecyclerViewAdapter;
import com.rincyan.smsdelete.recyclerview.SMS;
import com.rincyan.smsdelete.utils.GlobalControl;
import com.rincyan.smsdelete.utils.SMSHandler;

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
    private String method = "null";
    private Long start_time = null;
    private Long end_time = null;
    private String regex = "";
    private Bundle arg;
    private GlobalControl globalControl;
    private SQLiteDatabase db;
    private int deleteWay;
    private int clickPos;
    private int tryTimes = 0;
    private String defaultSmsApp;


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
        globalControl = (GlobalControl) getActivity().getApplicationContext();
        globalControl.setFabIconDel();
        globalControl.set_fragment_name(getResources().getString(R.string.fragment_clean));
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
                                    deleteWay = 0;
                                    clickPos = position;
                                    singleDel(position);
                                }
                            })
                            .setNeutralButton(smsData.get(position).getWhitelist() ? R.string.remove_from_whitelist : R.string.add_to_whitelist, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    db = getActivity().openOrCreateDatabase("smsdel.db", getActivity().MODE_PRIVATE, null);
                                    if (smsData.get(position).getWhitelist()) {
                                        db.execSQL("delete from whitelist where textid=" + String.valueOf(smsData.get(position).getId()));
                                        smsData.get(position).setWhitelist(false);
                                    } else {
                                        db.execSQL("insert into whitelist ('textid') values ('" + String.valueOf(smsData.get(position).getId()) + "')");
                                        smsData.get(position).setWhitelist(true);
                                    }
                                    db.close();
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
                    .setMessage(getResources().getString(R.string.fragment_clean_willdel))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteWay = 1;
                            allDel();
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

    private void singleDel(int position) {
        if(Objects.equals(Telephony.Sms.getDefaultSmsPackage(context), context.getPackageName())) {
            if (smsHandler.deleteSms(smsData.get(position).getId().toString()) == 1) {
                smsData.remove(position);
                db = getActivity().openOrCreateDatabase("smsdel.db", getActivity().MODE_PRIVATE, null);
                if (smsData.get(position).getWhitelist()) {
                    db.execSQL("delete from whitelist where textid=" + String.valueOf(smsData.get(position).getId()));
                }
                db.close();
                adapter.notifyDataSetChanged();
                Toast.makeText(context, R.string.delete_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.no_sms_try_again, Toast.LENGTH_SHORT).show();
            }
            cancelDefault();
        }else {
            setDefault();
        }
    }

    private void allDel() {
        if(Objects.equals(Telephony.Sms.getDefaultSmsPackage(context), context.getPackageName())) {
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
                        if (!tmp.getWhitelist()) {
                            flag = smsHandler.deleteSms(tmp.getId().toString());
                            if (flag != 1) {
                                progressDialog.dismiss();
                                Toast.makeText(context, R.string.fragment_clean_error, Toast.LENGTH_SHORT).show();
                                setDefault();
                                Looper.loop();
                                return;
                            }
                        }
                        count += 1;
                        progressDialog.setProgress((int) ((float) count / (float) smsData.size() * 100));
                    }
                    progressDialog.dismiss();
                    Toast.makeText(context, R.string.fragment_clean_success, Toast.LENGTH_SHORT).show();
                    Update update = new Update();
                    update.execute();
                    Looper.loop();
                    cancelDefault();
                }
            }.start();
        }else {
            setDefault();
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

    private void setDefault() {
        defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);
        Intent intent =
                new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                context.getPackageName());
        startActivityForResult(intent, 0);
    }

    private void cancelDefault() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
        context.startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (tryTimes < 2) {
                if (resultCode != Activity.RESULT_OK) {
                    setDefault();
                } else {
                    if (deleteWay == 0) {
                        singleDel(clickPos);
                    } else {
                        allDel();
                    }
                }
                tryTimes++;
            } else {
                tryTimes = 0;
            }
        }
    }
}
