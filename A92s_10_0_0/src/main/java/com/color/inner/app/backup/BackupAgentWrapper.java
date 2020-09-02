package com.color.inner.app.backup;

import android.app.backup.BackupAgent;
import android.content.Context;
import android.util.Log;

public class BackupAgentWrapper {
    private static final String TAG = "BackupAgentWrapper";

    private BackupAgentWrapper() {
    }

    public static void attach(BackupAgent backupAgent, Context context) {
        try {
            backupAgent.attach(context);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }
}
