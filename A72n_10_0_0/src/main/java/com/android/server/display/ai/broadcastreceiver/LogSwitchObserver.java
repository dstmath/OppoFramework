package com.android.server.display.ai.broadcastreceiver;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.server.display.ai.utils.ColorAILog;

public class LogSwitchObserver extends ContentObserver {
    private static final String TAG = "LogSwitchObserver";
    private static volatile LogSwitchObserver sInstance;
    private Context mContext;
    private Uri mUri = Settings.System.getUriFor("log_switch_type");

    public LogSwitchObserver(Context context) {
        super(null);
        this.mContext = context;
    }

    public static LogSwitchObserver getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LogSwitchObserver.class) {
                if (sInstance == null) {
                    sInstance = new LogSwitchObserver(context);
                }
            }
        }
        return sInstance;
    }

    public void register() {
        try {
            this.mContext.getContentResolver().registerContentObserver(this.mUri, true, this);
        } catch (Exception e) {
            ColorAILog.e(TAG, "Oops! Exception on register: " + e.getMessage());
        }
    }

    public void unregister() {
        try {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        } catch (Exception e) {
            ColorAILog.e(TAG, "Oops! Exception on unregister: " + e.getMessage());
        }
    }

    public void onChange(boolean selfChange) {
        ColorAILog.sIsLogOn = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
        Log.i(TAG, "refreshLogSwitch: new log state = " + ColorAILog.sIsLogOn);
    }
}
