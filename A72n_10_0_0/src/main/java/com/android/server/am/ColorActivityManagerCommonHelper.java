package com.android.server.am;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.display.ai.utils.ColorAILog;
import oppo.util.OppoCommonConstants;

public class ColorActivityManagerCommonHelper {
    protected static boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, true);
    protected final ActivityManagerService mAms;
    protected final IColorActivityManagerServiceEx mColorAmsEx;
    protected final Context mContext;

    public ColorActivityManagerCommonHelper(Context context, IColorActivityManagerServiceEx amsEx) {
        this.mContext = context;
        this.mColorAmsEx = amsEx;
        if (amsEx != null) {
            this.mAms = amsEx.getActivityManagerService();
        } else {
            this.mAms = null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkCodeValid(int code, int type, int group) {
        return OppoCommonConstants.checkCodeValid(code, type, group);
    }
}
