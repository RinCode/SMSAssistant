package com.rincyan.smsdelete.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rin on 2017/6/15.
 * 验证码过滤
 */

public class isCapture {
    private SQLiteDatabase db;
    private Context context;
    private ArrayList<String> rules;

    public isCapture(Context context) {
        db = context.openOrCreateDatabase("smsdel.db", context.MODE_PRIVATE, null);
        rules = new ArrayList<String>();
        this.context = context;
        Cursor c = db.rawQuery("select * from regex", null);
        if (c != null) {
            while (c.moveToNext()) {
                rules.add(c.getString(c.getColumnIndex("rule")));
            }
            c.close();
        }
    }

    public boolean simpleDetect(String origin) {
        String pattern = ".*验证码.*";
        return Pattern.matches(pattern, origin);
    }

    public boolean advanceDetect(String origin) {
        for (String tmp : rules) {
            Pattern p = Pattern.compile(tmp);
            Matcher m = p.matcher(origin);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }

    public boolean timeDetect(long s, long e, long now) {
        return now >= s && now <= e;
    }

    public boolean contactDetect(String address,String regex){
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(address);
        return m.find();
    }
}
