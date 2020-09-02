package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Message;
import android.util.Slog;

public final class ColorWindowManagerServiceMessageHelper extends ColorWindowManagerServiceCommonHelper {
    static final int COLOR_WMS_MSG_INDEX = 1001;
    static final int OPPO_FREEZE_TIMEOUT = 1001;
    static final int SHOW_TALKBACK_WATER_MARK = 1002;
    private static final String TAG = "ColorWindowManagerServiceMessageHelper";

    public ColorWindowManagerServiceMessageHelper(Context context, IColorWindowManagerServiceEx wmsEx) {
        super(context, wmsEx);
    }

    public boolean handleMessage(Message msg, int whichHandler) {
        if (!checkCodeValid(msg.what, 2, 1)) {
            if (DEBUG) {
                Slog.i(TAG, "Invalid message = " + msg);
            }
            return false;
        } else if (whichHandler == 1) {
            return handleMainMessage(msg);
        } else {
            Slog.i(TAG, "Unknow handle = " + whichHandler);
            return false;
        }
    }

    private boolean handleMainMessage(Message msg) {
        int i = msg.what;
        if (i == 1001) {
            WindowManagerService windowManagerService = this.mWms;
            return true;
        } else if (i == 1002) {
            OppoFeatureCache.get(IColorWatermarkManager.DEFAULT).showWatermarkIfNeeded(((Boolean) msg.obj).booleanValue());
            return true;
        } else if (msg.what >= 1308 || msg.what <= 1301) {
            return false;
        } else {
            OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).hanleFullScreenDisplayMessage(msg);
            return false;
        }
    }
}
