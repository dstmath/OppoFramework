package com.oppo.enterprise.mdmcoreservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class CallLimitAlarmReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "android.intent.action.call.limit.time.out".equals(intent.getAction())) {
            boolean isOutgoing = intent.getBooleanExtra("isoutgoingcall", false);
            Log.d("CallLimitAlarmReceiver", "onReceive:  isOutgoing" + isOutgoing);
            if (isOutgoing) {
                Settings.Secure.putInt(context.getContentResolver(), "oppo_phone_set_outgoing_call_limit_policy", -1);
            } else {
                Settings.Secure.putInt(context.getContentResolver(), "oppo_phone_set_incoming_call_limit_policy", -1);
            }
        }
    }
}
