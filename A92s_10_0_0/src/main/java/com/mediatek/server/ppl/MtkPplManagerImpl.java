package com.mediatek.server.ppl;

import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class MtkPplManagerImpl extends MtkPplManager {
    private static final String TAG = "MtkPplManager";
    private final BroadcastReceiver mPPLReceiver = new BroadcastReceiver() {
        /* class com.mediatek.server.ppl.MtkPplManagerImpl.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            MtkPplManagerImpl mtkPplManagerImpl = MtkPplManagerImpl.this;
            mtkPplManagerImpl.pplEnable(context, mtkPplManagerImpl.filterPplAction(intent.getAction()));
        }
    };
    private boolean mPplStatus = false;
    private StatusBarManager mStatusBarManager;

    public IntentFilter registerPplIntents() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mediatek.ppl.NOTIFY_LOCK");
        filter.addAction("com.mediatek.ppl.NOTIFY_UNLOCK");
        return filter;
    }

    public int calculateStatusBarStatus(boolean pplStatus) {
        if (pplStatus) {
            return 983040;
        }
        return 0;
    }

    public boolean getPplLockStatus() {
        return this.mPplStatus;
    }

    public boolean filterPplAction(String action) {
        if (action.equals("com.mediatek.ppl.NOTIFY_LOCK")) {
            Log.d(TAG, "filterPplAction, recevier action = " + action);
            this.mPplStatus = true;
        } else if (action.equals("com.mediatek.ppl.NOTIFY_UNLOCK")) {
            Log.d(TAG, "filterPplAction, recevier action = " + action);
            this.mPplStatus = false;
        }
        return this.mPplStatus;
    }

    public void registerPplReceiver(Context context) {
        context.registerReceiver(this.mPPLReceiver, registerPplIntents());
    }

    /* access modifiers changed from: private */
    public void pplEnable(Context context, boolean enable) {
        int what = calculateStatusBarStatus(enable);
        if (this.mStatusBarManager == null) {
            this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        }
        this.mStatusBarManager.disable(what);
    }
}
