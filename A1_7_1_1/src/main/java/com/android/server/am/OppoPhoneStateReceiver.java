package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Slog;

public class OppoPhoneStateReceiver extends BroadcastReceiver {
    public static final String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
    public static final String STATE_KEY = "state";
    private String TAG = "OppoProcessManager";

    public OppoPhoneStateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PHONE_STATE_CHANGED);
        context.registerReceiver(this, filter);
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_PHONE_STATE_CHANGED)) {
            Slog.i(this.TAG, "ACTION_PHONE_STATE_CHANGED in OppoPhoneStateReceiver");
            OppoProcessManager.getInstance().setPhoneState(intent.getStringExtra("state"));
        }
    }
}
