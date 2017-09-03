package com.rincyan.smsdelete;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
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

import java.util.Locale;
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

        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = Locale.getDefault();
        resources.updateConfiguration(config, dm);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        fragmentControl = (FragmentControl) this.getApplicationContext();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fragmentControl.setFab(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //悬浮按钮动作
                if (Objects.equals(fragmentControl.get_fragment_name(), getResources().getString(R.string.fragment_clean))) {
                    clean = (Clean) fragmentControl.getFragment();
                    clean.deleteAll();
                } else if (Objects.equals(fragmentControl.get_fragment_name(), getResources().getString(R.string.fragment_regex_mode))) {
                    addrule = new Addrule();
                    fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, addrule).commit();
                } else if (Objects.equals(fragmentControl.get_fragment_name(), getResources().getString(R.string.fragment_addrule))) {
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
            if (Objects.equals(fragmentControl.get_fragment_name(), getResources().getString(R.string.fragment_regex_mode))) {
                fragmentControl.set_fragment_name(getResources().getString(R.string.fragment_advance));
            } else if (Objects.equals(fragmentControl.get_fragment_name(), getResources().getString(R.string.addrule_rule))) {
                fragmentControl.set_fragment_name(getResources().getString(R.string.fragment_regex_mode));
            }
            if (fragmentManager.getBackStackEntryCount() == 1) {
                //退出前检查是否已还原默认短信应用
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
            new AlertDialog.Builder(this).setTitle(R.string.tos)
                    .setMessage(R.string.usage)
                    .setCancelable(false)
                    .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = getSharedPreferences("tos", MODE_PRIVATE).edit();
                            editor.putBoolean("accept", true);
                            editor.apply();
                        }
                    })
                    .setNegativeButton(R.string.refuse, new DialogInterface.OnClickListener() {

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
                Toast.makeText(this, R.string.no_sms_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private View getFocus() {
        return this.getCurrentFocus();
    }


}
