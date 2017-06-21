package com.rincyan.smsdelete;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import com.rincyan.smsdelete.fragment.Clean;
import com.rincyan.smsdelete.fragment.Hello;
import com.rincyan.smsdelete.utils.DefaultSMS;

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
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        title = getTitle().toString();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.equals(title, "清理")) {
                    clean.deleteAll();
                } else if (Objects.equals(title, "高级选项")) {
                    addrule = new Addrule();
                    System.out.println("1");
                    title = "添加规则";
                    fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, addrule).commit();
                    fab.setImageResource(android.R.drawable.ic_menu_save);
                } else if (Objects.equals(title, "添加规则")) {
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
            if (Objects.equals(title, "添加规则")) {
                fab.setImageResource(android.R.drawable.ic_menu_add);
                title = "高级选项";
            } else if (Objects.equals(title, "添加规则")) {
                fab.setImageResource(android.R.drawable.ic_menu_add);
                title = "添加规则";
            }
            if (fragmentManager.getBackStackEntryCount() == 1) {
                DefaultSMS defaultSMS = new DefaultSMS(this);
                if (defaultSMS.isDefault()) {
                    defaultSMS.CancelDefault();
                }
                System.out.println(defaultSMS.isDefault());
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
            title = "清理";
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, clean).commit();
            fab.setImageResource(android.R.drawable.ic_menu_delete);
        } else if (id == R.id.nav_advanced) {
            advance = new Advance();
            title = "高级选项";
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content, advance).commit();
            fab.setImageResource(android.R.drawable.ic_menu_add);
        } else if (id == R.id.nav_about) {
            about = new About();
            title = "关于";
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
