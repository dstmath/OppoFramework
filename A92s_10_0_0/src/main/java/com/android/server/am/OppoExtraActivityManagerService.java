package com.android.server.am;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.Intent;
import com.android.server.IColorAlarmManagerHelper;

public class OppoExtraActivityManagerService {
    private static final String ACTION_MODE_LOCK = "com.oppo.intent.action.KEY_LOCK_MODE";
    private static final int KEY_LOCK_MODE_NORMAL = 0;

    public static void setKeyLockModeNormal(Context context, String processName, boolean systemReady) {
        if (systemReady) {
            Intent intent = new Intent(ACTION_MODE_LOCK);
            intent.putExtra("KeyLockMode", 0);
            intent.putExtra("ProcessName", processName);
            context.sendBroadcast(intent);
            OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).processDied(processName);
        }
    }
}
