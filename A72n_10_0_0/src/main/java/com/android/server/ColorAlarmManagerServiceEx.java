package com.android.server;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;

public class ColorAlarmManagerServiceEx extends ColorDummyAlarmManagerServiceEx {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String TAG = "ColorAlarmManagerServiceEx";

    public ColorAlarmManagerServiceEx(Context context, AlarmManagerService ams) {
        super(context, ams);
        init(context, ams);
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        ColorServiceRegistry.getInstance().serviceReady(26);
    }

    public void onStart() {
        Slog.i(TAG, "onStart");
        ColorAlarmManagerServiceEx.super.onStart();
    }

    private void init(Context context, AlarmManagerService ams) {
        ColorServiceRegistry.getInstance().serviceInit(6, this);
    }
}
