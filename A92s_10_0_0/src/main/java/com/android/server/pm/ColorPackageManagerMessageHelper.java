package com.android.server.pm;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Message;
import android.util.Slog;

public final class ColorPackageManagerMessageHelper extends ColorPackageManagerCommonHelper {
    static final int COLOR_PMS_MSG_INDEX = 1001;
    public static final int MSG_CACHE_ICON_INIT = 1003;
    public static final int MSG_PACAKGE_ADDED = 1001;
    public static final int MSG_PACKAGE_REMOVED = 1002;
    private static final String TAG = "ColorPackageManagerMessageHelper";

    public ColorPackageManagerMessageHelper(Context context, PackageManagerService pms) {
        super(context, pms);
    }

    public boolean handleMessage(Message msg, int whichHandler) {
        if (whichHandler == 1) {
            return handleMainMessage(msg);
        }
        if (whichHandler == 2) {
            return handleUiMessage(msg);
        }
        if (whichHandler == 3) {
            return handleBgMessage(msg);
        }
        if (whichHandler == 4) {
            return handleKillMessage(msg);
        }
        Slog.i(TAG, "Unknow handle = " + whichHandler);
        return false;
    }

    private boolean handleMainMessage(Message msg) {
        int what = msg.what;
        if (what != 199) {
            switch (what) {
                case 1001:
                    OppoFeatureCache.get(IColorIconCachesManager.DEFAULT).onPackageAdded((String) msg.obj);
                    return true;
                case 1002:
                    OppoFeatureCache.get(IColorIconCachesManager.DEFAULT).onPackageRemoved((String) msg.obj);
                    return true;
                case 1003:
                    OppoFeatureCache.get(IColorIconCachesManager.DEFAULT).cacheActivityIconsData((String) null);
                    OppoFeatureCache.get(IColorIconCachesManager.DEFAULT).cacheAppIconsData();
                    return true;
                default:
                    return false;
            }
        } else {
            OppoBasePackageManagerService.mColorPmsEx.handleOpooMarketMesage(msg);
            return false;
        }
    }

    private boolean handleUiMessage(Message msg) {
        return false;
    }

    private boolean handleBgMessage(Message msg) {
        return false;
    }

    private boolean handleKillMessage(Message msg) {
        return false;
    }
}
