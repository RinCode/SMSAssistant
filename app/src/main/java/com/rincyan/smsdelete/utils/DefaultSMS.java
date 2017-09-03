package com.rincyan.smsdelete.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.widget.Toast;

import java.util.Objects;

/**
 * Created by rin on 2017/6/16.
 * 用于设置及取消默认短信应用
 */

public class DefaultSMS {
    private Context context;
    private String defaultSmsApp;

    public DefaultSMS(Context context) {
        this.context = context;
    }

    public boolean isDefault() {
        return Objects.equals(Telephony.Sms.getDefaultSmsPackage(context), context.getPackageName());
    }

    public void SetDefault() {
        defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);
        Intent intent =
                new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                context.getPackageName());
        context.startActivity(intent);
    }

    public void CancelDefault() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
        context.startActivity(intent);
    }

}
