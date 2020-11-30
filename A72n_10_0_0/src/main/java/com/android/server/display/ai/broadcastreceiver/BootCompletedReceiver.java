package com.android.server.display.ai.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";
    private static volatile BootCompletedReceiver sInstance;
    private IBootListener mBootListener;
    private Context mContext;
    private IntentFilter mIntentFilter = new IntentFilter(BrightnessConstants.ACTION_BOOT_COMPLETED);

    public interface IBootListener {
        void onBootCompleted();
    }

    private BootCompletedReceiver(Context context) {
        ColorAILog.d(TAG, "Register boot Broadcast.");
        this.mContext = context;
        context.getApplicationContext().registerReceiver(this, this.mIntentFilter);
    }

    public static BootCompletedReceiver getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BootCompletedReceiver.class) {
                if (sInstance == null) {
                    sInstance = new BootCompletedReceiver(context);
                }
            }
        }
        return sInstance;
    }

    public void unregister() {
        ColorAILog.d(TAG, "Unregister boot receiver to avoid being registered multiple times.");
        try {
            this.mContext.getApplicationContext().unregisterReceiver(sInstance);
        } catch (Exception e) {
            ColorAILog.e(TAG, "Oops! Exception on unregister: " + e.getMessage());
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent != null && BrightnessConstants.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ColorAILog.d(TAG, "Boot completed.");
            IBootListener iBootListener = this.mBootListener;
            if (iBootListener != null) {
                iBootListener.onBootCompleted();
            }
        }
    }

    public void setBootListener(IBootListener iBootListener) {
        this.mBootListener = iBootListener;
    }
}
