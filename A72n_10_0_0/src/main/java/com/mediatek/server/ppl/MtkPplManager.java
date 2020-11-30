package com.mediatek.server.ppl;

import android.content.Context;
import android.content.IntentFilter;

public class MtkPplManager {
    public static final String PPL_LOCK = "com.mediatek.ppl.NOTIFY_LOCK";
    public static final String PPL_UNLOCK = "com.mediatek.ppl.NOTIFY_UNLOCK";

    public IntentFilter registerPplIntents() {
        return null;
    }

    public int calculateStatusBarStatus(boolean pplStatus) {
        return 0;
    }

    public boolean getPplLockStatus() {
        return false;
    }

    public boolean filterPplAction(String action) {
        return false;
    }

    public void registerPplReceiver(Context context) {
    }
}
