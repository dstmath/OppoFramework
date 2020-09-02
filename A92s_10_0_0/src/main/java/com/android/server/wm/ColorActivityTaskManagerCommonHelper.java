package com.android.server.wm;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.display.ai.utils.ColorAILog;
import oppo.util.OppoCommonConstants;

public class ColorActivityTaskManagerCommonHelper {
    protected static boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, true);
    protected final ActivityTaskManagerService mAtms;
    protected final IColorActivityTaskManagerServiceEx mColorAmsEx;
    protected final Context mContext;

    public ColorActivityTaskManagerCommonHelper(Context context, IColorActivityTaskManagerServiceEx atmsEx) {
        this.mContext = context;
        this.mColorAmsEx = atmsEx;
        if (atmsEx != null) {
            this.mAtms = atmsEx.getActivityTaskManagerService();
        } else {
            this.mAtms = null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkCodeValid(int code, int type, int group) {
        return OppoCommonConstants.checkCodeValid(code, type, group);
    }
}
