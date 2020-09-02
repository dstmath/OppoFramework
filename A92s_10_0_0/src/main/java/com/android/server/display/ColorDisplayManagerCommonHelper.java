package com.android.server.display;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.display.ai.utils.ColorAILog;

public class ColorDisplayManagerCommonHelper {
    protected static boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, true);
    protected final Context mContext;
    protected final DisplayManagerService mDms;

    public ColorDisplayManagerCommonHelper(Context context, DisplayManagerService dms) {
        this.mContext = context;
        this.mDms = dms;
    }
}
