package com.android.server.wm;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;
import oppo.util.OppoCommonConstants;

public class ColorWindowManagerServiceCommonHelper {
    protected static boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, true);
    protected OppoBaseWindowManagerService mBaseWms;
    protected final IColorWindowManagerServiceEx mColorWmsEx;
    protected final Context mContext;
    protected final WindowManagerService mWms;

    public ColorWindowManagerServiceCommonHelper(Context context, IColorWindowManagerServiceEx wmsEx) {
        this.mContext = context;
        this.mColorWmsEx = wmsEx;
        if (wmsEx != null) {
            this.mWms = wmsEx.getWindowManagerService();
        } else {
            this.mWms = null;
        }
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            this.mBaseWms = typeCasting(windowManagerService);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkCodeValid(int code, int type, int group) {
        return OppoCommonConstants.checkCodeValid(code, type, group);
    }

    private static OppoBaseWindowManagerService typeCasting(WindowManagerService wms) {
        if (wms != null) {
            return (OppoBaseWindowManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseWindowManagerService.class, wms);
        }
        return null;
    }
}
