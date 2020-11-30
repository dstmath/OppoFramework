package com.android.server.power;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.display.ai.utils.ColorAILog;

public class ColorPowerManagerCommonHelper {
    protected static boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, true);
    protected final Context mContext;
    protected final PowerManagerService mPowerMS;

    public ColorPowerManagerCommonHelper(Context context, PowerManagerService pms) {
        this.mContext = context;
        this.mPowerMS = pms;
    }
}
