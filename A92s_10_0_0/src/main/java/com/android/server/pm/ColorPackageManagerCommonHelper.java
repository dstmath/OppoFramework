package com.android.server.pm;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.display.ai.utils.ColorAILog;

public class ColorPackageManagerCommonHelper {
    protected static boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, true);
    protected final Context mContext;
    protected final PackageManagerService mPms;

    public ColorPackageManagerCommonHelper(Context context, PackageManagerService pms) {
        this.mContext = context;
        this.mPms = pms;
    }
}
