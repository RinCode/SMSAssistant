package com.rincyan.smsdelete.utils;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Telephony;
import android.widget.Toast;

import com.rincyan.smsdelete.R;
import com.rincyan.smsdelete.recyclerview.SMS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Created by rin on 2017/6/15.
 * 短信操作
 */

public class SMSHandler {
    private SQLiteDatabase db;
    private long start_time;
    private long end_time;
    private String regex;
    private Context context;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private static final Uri SMS_URI = Uri.parse("content://sms/inbox");
    private static final String[] ALL_THREADS_PROJECTION = {
            "_id", "address", "person", "body",
            "date", "type", "thread_id"};

    public SMSHandler(Context context) {
        this.context = context;
    }

    public ArrayList getSMS(String method) {
        final ArrayList smsData = new ArrayList<>();
        boolean whitelist = false;
        try {
            db = context.openOrCreateDatabase("smsdel.db", context.MODE_PRIVATE, null);
            ContentResolver resolver = context.getContentResolver();
            final Cursor cursor = resolver.query(SMS_URI, ALL_THREADS_PROJECTION,
                    null, null, "date desc");
            isCapture ic = new isCapture(context);
            assert cursor != null;
            while ((cursor.moveToNext())) {
                whitelist = false;
                int indexBody = cursor.getColumnIndex("body");
                int indexAddress = cursor.getColumnIndex("address");
                int indexDate = cursor.getColumnIndex("date");
                int indexId = cursor.getColumnIndex("_id");
                Long id = cursor.getLong(indexId);
                String body = cursor.getString(indexBody);
                String address = cursor.getString(indexAddress);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd hh:mm");
                Date formatDate = new Date(Long.parseLong(cursor.getString(indexDate)));
                String date = dateFormat.format(formatDate);

                Cursor c = db.rawQuery("select * from whitelist where textid=" + String.valueOf(id), null);
                while(c.moveToNext()){
                    c.close();
                    whitelist = true;
                }
                c.close();

                if (Objects.equals(method, "time")) {
                    if (ic.timeDetect(start_time, end_time, Long.parseLong(cursor.getString(indexDate)))) {
                        smsData.add(new SMS(address, body, date, id, whitelist));
                    }
                } else if (Objects.equals(method, "contact")) {
                    if (ic.contactDetect(address, regex)) {
                        smsData.add(new SMS(address, body, date, id, whitelist));
                    }
                } else {
                    preferences = context.getSharedPreferences("setting", context.MODE_PRIVATE);
                    Boolean checked = preferences.getBoolean("advance", false);
                    if (!checked) {
                        if (ic.simpleDetect(body)) {
                            smsData.add(new SMS(address, body, date, id, whitelist));
                        }
                    } else {
                        if (ic.advanceDetect(body)) {
                            smsData.add(new SMS(address, body, date, id, whitelist));
                        }
                    }
                }
            }
            if (smsData.isEmpty()) {
                smsData.add(new SMS(context.getResources().getString(R.string.no_sms_detected), "", "", (long) -1, whitelist));
            }
            cursor.close();

        } catch (Exception e) {
            smsData.add(new SMS(context.getResources().getString(R.string.no_sms_permission), "", "", (long) -1, whitelist));
        }
        return smsData;
    }

    public int deleteSms(String smsId) {
        if (!Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName()) | Objects.equals(smsId, "-1")) {
            return 0;
        }
        String uri = "content://sms/" + smsId;
        return context.getContentResolver().delete(Uri.parse(uri),
                null, null);
    }

    public void setTime(long l1, long l2) {
        this.start_time = l1;
        this.end_time = l2;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
