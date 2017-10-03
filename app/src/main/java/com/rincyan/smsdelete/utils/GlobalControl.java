package com.rincyan.smsdelete.utils;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.receiver.SmsReceiver;


/**
 * Created by rin on 2017/08/01.
 * 用于判断当前展示的fragment是哪个
 */

public class GlobalControl extends Application {
    private String now_fragment = null;
    private FloatingActionButton fab;
    private Fragment fragment;
    private SmsReceiver smsReceiver;
    private IntentFilter mIntentFilter;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public String get_fragment_name() {
        return now_fragment;
    }

    public void set_fragment_name(String title) {
        this.now_fragment = title;
    }

    public void setFragment(Fragment fragment){
        this.fragment = fragment;
    }

    public Fragment getFragment(){
        return this.fragment;
    }

    public void setFab(FloatingActionButton fab) {
        this.fab = fab;
    }

    public void setFabIconSava() {
        this.fab.setImageResource(android.R.drawable.ic_menu_save);
    }

    public void setFabIconCancle() {
        this.fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
    }

    public void setFabIconAdd() {
        this.fab.setImageResource(android.R.drawable.ic_menu_add);
    }

    public void setFabIconDel() {
        this.fab.setImageResource(android.R.drawable.ic_menu_delete);
    }


    //smslistening
    public boolean startListen(){
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                SharedPreferences preferences = getSharedPreferences("setting", MODE_PRIVATE);
                Boolean accept = preferences.getBoolean("recognize", false);
                if (accept) {
                    smsReceiver = new SmsReceiver();
                    mIntentFilter = new IntentFilter();
                    mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
                    registerReceiver(smsReceiver, mIntentFilter);
                    Toast.makeText(this, R.string.start_listening,Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean stopListen(){
        try{
            unregisterReceiver(smsReceiver);
            Toast.makeText(this, R.string.stop_listening,Toast.LENGTH_SHORT).show();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
