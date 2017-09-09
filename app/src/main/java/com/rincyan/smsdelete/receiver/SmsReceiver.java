package com.rincyan.smsdelete.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private String code;
    private static MessageListener mMessageListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage msg = null;
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                msg = SmsMessage.createFromPdu((byte[]) object);
                Date date = new Date(msg.getTimestampMillis());//时间
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String receiveTime = format.format(date);
                Pattern pattern = Pattern.compile("\\d{4,}");
                Matcher matcher = pattern.matcher(msg.getDisplayMessageBody());
                String code = "";
                int i = 0;
                while (matcher.find()) {
                    code += matcher.group();
                    i++;
                }
                if (!Objects.equals(code, "")) {
                    if(i == 1) {
                        mMessageListener.onReceived(code);
                    }else {
                        mMessageListener.onReceived("-1");
                    }
                    abortBroadcast();
                }
            }
        }
    }

    public interface MessageListener {
        void onReceived(String message);
    }

    public void setOnReceivedMessageListener(MessageListener messageListener) {
        Log.i("set", "set");
        mMessageListener = messageListener;
    }
}
