package com.android.server.am;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;

public class ColorKeyLayoutManagerService implements IColorKeyLayoutManager {
    public static boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static final String TAG = "ColorKeyLayoutManagerService";
    private static volatile ColorKeyLayoutManagerService sInstance = null;
    private ActivityManagerService mAms;
    private ColorKeyLayoutManagerUtil mUtil;

    public static ColorKeyLayoutManagerService getInstance() {
        if (sInstance == null) {
            synchronized (ColorKeyLayoutManagerService.class) {
                if (sInstance == null) {
                    sInstance = new ColorKeyLayoutManagerService();
                }
            }
        }
        return sInstance;
    }

    private ColorKeyLayoutManagerService() {
        this.mAms = null;
        this.mUtil = null;
        this.mUtil = ColorKeyLayoutManagerUtil.getInstance();
    }

    public void init(ActivityManagerService ams) {
        this.mAms = ams;
    }

    public void setGimbalLaunchPkg(String pkgName) {
        if (pkgName == null) {
            Slog.w(TAG, "pkgName is null.");
        } else {
            this.mUtil.setGimbalLaunchPkg(pkgName);
        }
    }
}
