package com.oppo.internal.telephony.rus;

import android.content.Context;
import android.telephony.Rlog;

public class RusInitProcess {
    private static final String TAG = "RusInitProcess";

    public static void execute(Context context) {
        if (context == null) {
            Rlog.d(TAG, "RusInitProcess  execute() context == null");
            return;
        }
        Rlog.d(TAG, "RusInitProcess  execute()");
        RusFactory rusFactory = RusFactory.getInstance(context);
        if (rusFactory != null) {
            Rlog.d(TAG, "rusFactory.startRebootThread()");
            rusFactory.startRebootThread();
        }
    }
}
