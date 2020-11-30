package com.android.server.display.ai.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.server.display.ai.MonotoneSplineManager;
import com.android.server.display.ai.utils.ColorAILog;

public class ScreenOffReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenOffReceiver";
    private static volatile ScreenOffReceiver sInstance;
    private Context mContext;
    private boolean mIsRegistered = false;
    private IScreenListener mScreenListener;

    public interface IScreenListener {
        void onScreenOff();
    }

    private ScreenOffReceiver() {
    }

    public static ScreenOffReceiver getInstance() {
        if (sInstance == null) {
            synchronized (ScreenOffReceiver.class) {
                if (sInstance == null) {
                    sInstance = new ScreenOffReceiver();
                }
            }
        }
        return sInstance;
    }

    public void register(Context context) {
        this.mContext = context;
        if (!this.mIsRegistered) {
            ColorAILog.d(TAG, "Register ScreenOffReceiver Broadcast.");
            context.getApplicationContext().registerReceiver(this, new IntentFilter("android.intent.action.SCREEN_OFF"), "oppo.permission.OPPO_COMPONENT_SAFE", null);
            this.mIsRegistered = true;
        }
    }

    public void unregister() {
        ColorAILog.d(TAG, "Unregister screen receiver to avoid being registered multiple times.");
        try {
            if (this.mContext != null) {
                this.mContext.getApplicationContext().unregisterReceiver(sInstance);
            }
            this.mIsRegistered = false;
        } catch (Exception e) {
            ColorAILog.e(TAG, "Oops! Exception on unregister: " + e.getMessage());
        }
    }

    public void onReceive(Context context, Intent intent) {
        String action;
        if (intent != null && (action = intent.getAction()) != null) {
            char c = 65535;
            if (action.hashCode() == -2128145023 && action.equals("android.intent.action.SCREEN_OFF")) {
                c = 0;
            }
            if (c == 0) {
                if (this.mScreenListener != null) {
                    ColorAILog.i(TAG, "onReceive, ACTION_SCREEN_OFF do onScreenOff event.");
                    this.mScreenListener.onScreenOff();
                }
                try {
                    if (!AIBrightnessTrainSwitch.getInstance().isTrainEnable()) {
                        MonotoneSplineManager.getInstance(context).resetSplines();
                    }
                } catch (Exception e) {
                    ColorAILog.e(TAG, e.getMessage());
                }
            }
        }
    }

    public void setScreenListener(IScreenListener listener) {
        this.mScreenListener = listener;
    }
}
