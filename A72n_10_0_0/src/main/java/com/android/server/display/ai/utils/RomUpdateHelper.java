package com.android.server.display.ai.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import java.util.ArrayList;

public class RomUpdateHelper {
    private static final String ACTION_RUS = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String KEY_LIST_NAME = "sys_proton_brightness_list";
    private static final String TAG = "RomUpdateHelper";
    private static volatile RomUpdateHelper sRomUpdateHelper;
    private Handler mRUSChangeHandler;
    private boolean mRegistered = false;
    private RusReceiver mRomUpdateReceiver;

    private RomUpdateHelper() {
    }

    public static RomUpdateHelper getInstance() {
        if (sRomUpdateHelper == null) {
            synchronized (RomUpdateHelper.class) {
                if (sRomUpdateHelper == null) {
                    sRomUpdateHelper = new RomUpdateHelper();
                }
            }
        }
        return sRomUpdateHelper;
    }

    public void register(Context context) {
        if (!this.mRegistered) {
            if (this.mRomUpdateReceiver == null) {
                this.mRomUpdateReceiver = new RusReceiver();
            }
            this.mRomUpdateReceiver.register(context, new IntentFilter(ACTION_RUS));
            this.mRegistered = true;
        }
    }

    public void unregister(Context context) {
        RusReceiver rusReceiver = this.mRomUpdateReceiver;
        if (rusReceiver != null) {
            rusReceiver.unregister(context);
        }
    }

    public void setRUSChangeHandler(Handler handler) {
        this.mRUSChangeHandler = handler;
    }

    public class RusReceiver extends BroadcastReceiver {
        private static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
        private static final String TAG = "RomUpdateReceiver";

        public RusReceiver() {
        }

        public void onReceive(final Context context, Intent intent) {
            if (intent != null && RomUpdateHelper.ACTION_RUS.equals(intent.getAction())) {
                ColorAILog.d(TAG, "onReceive : oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
                ArrayList<String> changeTableNameList = intent.getStringArrayListExtra(ROM_UPDATE_CONFIG_LIST);
                if (changeTableNameList != null && changeTableNameList.contains("sys_proton_brightness_list")) {
                    new Thread(new Runnable() {
                        /* class com.android.server.display.ai.utils.RomUpdateHelper.RusReceiver.AnonymousClass1 */

                        public void run() {
                            ModelConfigUtil.getInstance().initialize(context);
                            Handler handler = RomUpdateHelper.this.mRUSChangeHandler;
                            if (handler != null) {
                                handler.sendEmptyMessage(3);
                            }
                        }
                    }, TAG).start();
                }
            }
        }

        public synchronized void register(Context context, IntentFilter filter) {
            ColorAILog.d(TAG, "Register RUS receiver!");
            context.getApplicationContext().registerReceiver(this, filter);
        }

        public synchronized void unregister(Context context) {
            ColorAILog.d(TAG, "Unregister RUS receiver!");
            try {
                context.getApplicationContext().unregisterReceiver(this);
                RomUpdateHelper.this.mRegistered = false;
            } catch (Exception e) {
                ColorAILog.d(TAG, "Fail to unregister RUS receiver!" + e.getMessage());
            }
        }
    }
}
