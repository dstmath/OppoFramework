package com.android.internal.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;

public class ShutdownActivity extends Activity {
    private static final String TAG = "ShutdownActivity";
    private boolean mConfirm;
    private boolean mReboot;
    private boolean mUserRequested;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        this.mReboot = "android.intent.action.REBOOT".equals(intent.getAction());
        this.mConfirm = intent.getBooleanExtra("android.intent.extra.KEY_CONFIRM", false);
        this.mUserRequested = intent.getBooleanExtra("android.intent.extra.USER_REQUESTED_SHUTDOWN", false);
        Slog.i(TAG, "onCreate(): confirm=" + this.mConfirm);
        Thread thr = new Thread(TAG) {
            public void run() {
                String str = null;
                IPowerManager pm = Stub.asInterface(ServiceManager.getService("power"));
                try {
                    if (ShutdownActivity.this.mReboot) {
                        pm.reboot(ShutdownActivity.this.mConfirm, null, false);
                        return;
                    }
                    boolean -get0 = ShutdownActivity.this.mConfirm;
                    if (ShutdownActivity.this.mUserRequested) {
                        str = "userrequested";
                    }
                    pm.shutdown(-get0, str, false);
                } catch (RemoteException e) {
                }
            }
        };
        thr.start();
        finish();
        try {
            thr.join();
        } catch (InterruptedException e) {
        }
    }
}
