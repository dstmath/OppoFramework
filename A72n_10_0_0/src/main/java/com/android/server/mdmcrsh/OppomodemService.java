package com.android.server.mdmcrsh;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IOppoModemService;
import android.os.SystemProperties;
import android.util.Log;

public final class OppomodemService extends IOppoModemService.Stub {
    static final boolean DEBUG = true;
    public static final int DELAY_TIME = 36000000;
    public static final String FILTER_NAME = "criticallog_config";
    private static final String TAG = "OppomodemService";
    private HandlerThread handlerThread = new HandlerThread(TAG);
    private AlertDialog mCheckNetworkDialog = null;
    private Context mContext;
    private Handler mHandler;
    private ModemcrashLogObserver mModemcrashLogObserver;

    public OppomodemService(Context context) {
        this.mContext = context;
        this.handlerThread.start();
        this.mHandler = this.handlerThread.getThreadHandler();
        this.mModemcrashLogObserver = new ModemcrashLogObserver(this.mContext, this.handlerThread.getLooper());
        this.mModemcrashLogObserver.init();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        OppomodemService.super.finalize();
    }

    public void systemReady() {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.android.server.mdmcrsh.OppomodemService.AnonymousClass1 */

            public void run() {
                Log.v(OppomodemService.TAG, "systemReady initLogCoreService");
                if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                    Log.v(OppomodemService.TAG, "systemReady test initLogCoreService");
                    return;
                }
                Log.v(OppomodemService.TAG, "check customer log tag");
                if (SystemProperties.getBoolean("persist.sys.log.customer", false)) {
                    Log.v(OppomodemService.TAG, "customer test logging");
                }
            }
        }, 20000);
    }
}
