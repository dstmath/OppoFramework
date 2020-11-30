package com.android.server.power;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.ColorServiceRegistry;
import com.android.server.display.ai.utils.ColorAILog;

public final class ColorPowerManagerServiceEx extends ColorDummyPowerManagerServiceEx {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String TAG = "ColorPowerManagerServiceEx";
    private Context mContext;

    public ColorPowerManagerServiceEx(Context context, PowerManagerService pms) {
        super(context, pms);
        this.mContext = context;
        init(context, pms);
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        ColorServiceRegistry.getInstance().serviceReady(23);
    }

    public Context getContext() {
        return this.mContext;
    }

    public void onStart() {
        Slog.i(TAG, "onStart");
        ColorPowerManagerServiceEx.super.onStart();
    }

    private void init(Context context, PowerManagerService pms) {
        ColorServiceRegistry.getInstance().serviceInit(3, this);
    }
}
