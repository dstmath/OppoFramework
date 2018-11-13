package com.android.server.secrecy.work;

import android.content.Context;

public class LogEncryptWork extends SecrecyWork {
    private static String TAG = "SecrecyService.LogEncryptWork";

    public LogEncryptWork(Context context, String name) {
        super(context, name);
    }

    public LogEncryptWork(Context context) {
        this(context, TAG);
    }
}
