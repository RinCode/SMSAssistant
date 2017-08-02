package com.rincyan.smsdelete;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.rincyan.smsdelete.fragment.About;
import com.rincyan.smsdelete.fragment.Addrule;
import com.rincyan.smsdelete.fragment.Advance;
import com.rincyan.smsdelete.fragment.Advance_regex;
import com.rincyan.smsdelete.fragment.Clean;
import com.rincyan.smsdelete.fragment.Hello;
import com.rincyan.smsdelete.utils.DefaultSMS;
import com.rincyan.smsdelete.utils.FragmentControl;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Hello hello;
    private Clean clean;
    private Advance advance;
    private Addrule addrule;
    private About about;
    private FragmentManager fragmentManager;
    private FloatingActionButton fab;
    private FragmentControl fragmentControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        fragmentControl = (FragmentControl) this.getApplicationContext();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fragmentControl.setFab(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.equals(fragmentControl.get_fragment_name(), "清理")) {
                    clean = (Clean) fragmentControl.getFragment();
                    clean.deleteAll();
                } else if (Objects.equals(fragmentControl.get_fragment_name(), "正则表达式模式")) {
                    addrule = new Addrule();
                    fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, addrule).commit();
                } else if (Objects.equals(fragmentControl.get_fragment_name(), "添加规则")) {
                    addrule.save();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                View view = getFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        hello = new Hello();
        fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, hello).commit();
        requestPermissions();
        createDb();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (Objects.equals(fragmentControl.get_fragment_name(), "正则表达式模式")) {
                fragmentControl.set_fragment_name("高级选项");
            } else if (Objects.equals(fragmentControl.get_fragment_name(), "添加规则")) {
                fragmentControl.set_fragment_name("正则表达式模式");
            }
            if (fragmentManager.getBackStackEntryCount() == 1) {
                DefaultSMS defaultSMS = new DefaultSMS(this);
                if (defaultSMS.isDefault()) {
                    defaultSMS.CancelDefault();
                }
                finish();
            }
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            about = new About();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, about).commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_clean) {
            clean = new Clean();
            fragmentControl.setFragment(clean);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, clean).commit();
        } else if (id == R.id.nav_advanced) {
            advance = new Advance();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, advance).commit();
        } else if (id == R.id.nav_about) {
            about = new About();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, about).commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createDb() {
        SQLiteDatabase db = openOrCreateDatabase("smsdel.db", MODE_PRIVATE, null);
        db.execSQL("create table if not exists regex (id integer primary key autoincrement,rule text not null)");
        db.close();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 0);
        }
        SharedPreferences preferences = this.getSharedPreferences("tos", MODE_PRIVATE);
        Boolean accept = preferences.getBoolean("accept", false);
        if (!accept) {
            new AlertDialog.Builder(this).setTitle("使用协议及说明")
                    .setMessage(" • 本软件的正常工作需要授予短信读/写权限，并会申请临时变更此软件为默认短信应用。\n" +
                            " • 基础清理功能为清理短信中含有“验证码”字符串的短信，由于匹配机制的问题，不保证能全部筛选出来。\n" +
                            " • 高级清理功能中，您可以自己书写Java正则表达式进行匹配筛选。\n" +
                            " • 重要：在进行任何删除操作前，请保证您已进行过原短信的备份。对于误删重要信息造成的后果请自负。")
                    .setCancelable(false)
                    .setPositiveButton("接受", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = getSharedPreferences("tos", MODE_PRIVATE).edit();
                            editor.putBoolean("accept", true);
                            editor.apply();
                        }
                    })
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    }).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "无法获取短信权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private View getFocus() {
        return this.getCurrentFocus();
    }


}
