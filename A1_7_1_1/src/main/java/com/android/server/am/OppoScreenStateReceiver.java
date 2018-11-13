package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class OppoScreenStateReceiver extends BroadcastReceiver {
    private String TAG = "OppoProcessManager";

    public OppoScreenStateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        context.registerReceiver(this, filter);
    }

    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                OppoProcessManager.getInstance().sendBpmEmptyMessage(120, 0);
            } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                OppoProcessManager.getInstance().sendBpmEmptyMessage(121, 0);
                OppoGameSpaceManager.getInstance().sendGameSpaceEmptyMessage(121, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
