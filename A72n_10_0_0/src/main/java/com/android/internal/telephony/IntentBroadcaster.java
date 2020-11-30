package com.android.internal.telephony;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IntentBroadcaster {
    private static final String TAG = "IntentBroadcaster";
    private static IntentBroadcaster sIntentBroadcaster;
    private Map<Integer, Intent> mRebroadcastIntents = new HashMap();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.IntentBroadcaster.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            OppoSimlockManager simlockMgr;
            if (intent.getAction().equals("android.intent.action.USER_UNLOCKED")) {
                synchronized (IntentBroadcaster.this.mRebroadcastIntents) {
                    Iterator iterator = IntentBroadcaster.this.mRebroadcastIntents.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry pair = (Map.Entry) iterator.next();
                        Intent i = (Intent) pair.getValue();
                        i.putExtra("rebroadcastOnUnlock", true);
                        iterator.remove();
                        if (!((IOppoUiccManager) OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0])).OppoSpecialProcessForLockState(i, ((Integer) pair.getKey()).intValue())) {
                            IntentBroadcaster intentBroadcaster = IntentBroadcaster.this;
                            intentBroadcaster.logd("Rebroadcasting intent " + i.getAction() + " " + i.getStringExtra("ss") + " for slotId " + pair.getKey());
                            ActivityManager.broadcastStickyIntent(i, -1);
                        }
                    }
                }
                IOppoUiccManager uiccManager = (IOppoUiccManager) OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0]);
                if (uiccManager != null && (simlockMgr = uiccManager.getOppoSimlockManager()) != null) {
                    simlockMgr.handleRebroadcastSimlock();
                }
            }
        }
    };

    private IntentBroadcaster(Context context) {
        context.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.USER_UNLOCKED"));
    }

    public static IntentBroadcaster getInstance(Context context) {
        if (sIntentBroadcaster == null) {
            sIntentBroadcaster = new IntentBroadcaster(context);
        }
        return sIntentBroadcaster;
    }

    public static IntentBroadcaster getInstance() {
        return sIntentBroadcaster;
    }

    public void broadcastStickyIntent(Intent intent, int slotId) {
        OppoSimlockManager simlockMgr;
        logd("Broadcasting and adding intent for rebroadcast: " + intent.getAction() + " " + intent.getStringExtra("ss") + " for slotId " + slotId);
        synchronized (this.mRebroadcastIntents) {
            ActivityManager.broadcastStickyIntent(intent, -1);
            IOppoUiccManager uiccManager = (IOppoUiccManager) OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0]);
            if (uiccManager == null || (simlockMgr = uiccManager.getOppoSimlockManager()) == null || !simlockMgr.isSimlockRebroadcast(intent, slotId)) {
                this.mRebroadcastIntents.put(Integer.valueOf(slotId), intent);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String s) {
        Log.d(TAG, s);
    }
}
