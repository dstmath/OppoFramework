package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Slog;

public class OppoBpmReceiver extends BroadcastReceiver {
    private String TAG = "OppoProcessManager";

    public OppoBpmReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.DATE_CHANGED");
        context.registerReceiver(this, filter);
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.DATE_CHANGED")) {
            Slog.i(this.TAG, "ACTION_DATE_CHANGED in OppoBpmReceiver");
            OppoProcessManager.getInstance().handleUploadInfo();
        }
    }
}
