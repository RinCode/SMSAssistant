package com.rincyan.smsdelete.utils;

import android.app.Application;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;


/**
 * Created by rin on 2017/08/01.
 */

public class FragmentControl extends Application {
    private String now_fragment = null;
    private FloatingActionButton fab;
    private Fragment fragment;

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
}
