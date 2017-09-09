package com.rincyan.smsdelete.receiver;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.rincyan.smsdelete.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Object[] smsextras = (Object[]) extras.get("pdus");

            for (int i = 0; i < smsextras.length; i++) {
                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);
                String strMsgBody = smsmsg.getMessageBody();
                Pattern pattern = Pattern.compile("\\d{4,}");
                Matcher matcher = pattern.matcher(strMsgBody);
                String code = "";
                int j = 0;
                while (matcher.find()) {
                    code += matcher.group();
                    i++;
                }
                if (!Objects.equals(code, "")) {
                    if (i == 1) {
                        Toast.makeText(context, context.getResources().getString(R.string.fragment_recognized_text1) + code + context.getResources().getString(R.string.fragment_recognized_text2), Toast.LENGTH_SHORT).show();
                        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setText(code);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.fragment_recognized_failed), Toast.LENGTH_SHORT).show();
                    }
                    abortBroadcast();
                }
            }

        }

    }
}
