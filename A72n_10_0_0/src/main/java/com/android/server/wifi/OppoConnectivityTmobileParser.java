package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;

public class OppoConnectivityTmobileParser {
    private static final boolean DBG = true;
    private static final String TAG = "OppoConnectivityTmobileParser";
    private static OppoConnectivityTmobileParser sInstance;
    private final Context mContext;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread = new HandlerThread(TAG);

    public OppoConnectivityTmobileParser(Context context) {
        Log.i(TAG, TAG);
        this.mContext = context;
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        registerForBroadcastReceiver();
    }

    public static OppoConnectivityTmobileParser getInstance(Context context) {
        if (sInstance == null) {
            synchronized (OppoConnectivityTmobileParser.class) {
                if (sInstance == null) {
                    sInstance = new OppoConnectivityTmobileParser(context);
                }
            }
        }
        return sInstance;
    }

    private void registerForBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.OPPO_START_CUSTOMIZE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoConnectivityTmobileParser.AnonymousClass1 */

            public void onReceive(final Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("oppo.intent.action.OPPO_START_CUSTOMIZE")) {
                    boolean match = false;
                    String operator = intent.getStringExtra("operator");
                    String region = intent.getStringExtra("region");
                    Log.i(OppoConnectivityTmobileParser.TAG, action + " received, operator:" + operator + ", region:" + region);
                    if (operator == null || region == null) {
                        Log.e(OppoConnectivityTmobileParser.TAG, action + ", operator or region does not exist!");
                        return;
                    }
                    if ((operator.equals("CS") && region.equals("DE")) || ((operator.equals("TMOBILE") && region.equals("NL")) || ((operator.equals("TELE2") && region.equals("NL")) || ((operator.equals("TMOBILE") && region.equals("PL")) || (operator.equals("TMOBILE") && region.equals("DE")))))) {
                        match = true;
                    }
                    if (!match) {
                        Log.e(OppoConnectivityTmobileParser.TAG, action + ", operator or region does not match!");
                        return;
                    }
                    OppoConnectivityTmobileParser.this.mHandler.post(new Runnable() {
                        /* class com.android.server.wifi.OppoConnectivityTmobileParser.AnonymousClass1.AnonymousClass1 */

                        public void run() {
                            ContentResolver contentResolver = context.getContentResolver();
                            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                            connectivityManager.stopTethering(0);
                            connectivityManager.stopTethering(2);
                            if (contentResolver != null) {
                                Settings.Global.putInt(contentResolver, "wifi_scan_always_enabled", 0);
                            }
                        }
                    });
                }
            }
        }, new IntentFilter(filter));
    }
}
