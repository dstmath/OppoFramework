package com.mediatek.mtklogger.c2klogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OpenReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.v(C2KLogUtils.TAG_APP, "I am open receiver in saber");
        if (intent.getAction().equals("android.provider.Telephony.SECRET_CODE")) {
            Log.i(C2KLogUtils.TAG_APP, "User call saber");
            if (!C2KLogUtils.isServiceRunning(context, C2KLogService.class.getName())) {
                Intent mIntentStartSaber = new Intent(context, C2KLogService.class);
                mIntentStartSaber.addFlags(268435456);
                context.startActivity(mIntentStartSaber);
            }
        }
    }
}
