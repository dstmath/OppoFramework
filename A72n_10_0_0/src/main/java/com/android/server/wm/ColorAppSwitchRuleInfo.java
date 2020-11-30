package com.android.server.wm;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import com.color.app.ColorAppEnterInfo;
import com.color.app.ColorAppExitInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.IColorAppSwitchObserver;
import com.color.zoomwindow.ColorZoomWindowManager;
import java.util.HashSet;

public class ColorAppSwitchRuleInfo {
    private static final String SYS_PKG_NAME = "android";
    private static final String TAG = "ColorAppSwitchRuleInfo";
    public ColorAppSwitchConfig config;
    private Context context;
    public IBinder.DeathRecipient deathRecipient;
    public boolean defaultMatchActivity;
    public boolean defaultMatchApp;
    public boolean enable = true;
    public boolean isStatic;
    public IColorAppSwitchObserver observer;
    public String pkgName;

    private ColorAppSwitchRuleInfo(Context context2) {
        this.context = context2;
        this.isStatic = true;
        this.config = new ColorAppSwitchConfig();
    }

    public static ColorAppSwitchRuleInfo buildStaticRuleInfo(Context context2) {
        return new ColorAppSwitchRuleInfo(context2);
    }

    public static ColorAppSwitchRuleInfo buildDynamicRuleInfo(Context context2, String pkgName2, IColorAppSwitchObserver observer2, ColorAppSwitchConfig config2) {
        return new ColorAppSwitchRuleInfo(context2, pkgName2, observer2, config2);
    }

    private ColorAppSwitchRuleInfo(Context context2, String pkgName2, IColorAppSwitchObserver observer2, ColorAppSwitchConfig config2) {
        this.context = context2;
        this.isStatic = false;
        this.pkgName = pkgName2;
        this.observer = observer2;
        this.config = config2;
        if ("android".equalsIgnoreCase(pkgName2)) {
            this.defaultMatchApp = true;
        }
    }

    public void setStaticPackageAndObserver(String pkg) {
        this.pkgName = pkg;
        this.observer = new ColorStaticBroadcastObserver(this.pkgName, this.context);
    }

    public boolean notifyActivityEnter(ActivityRecord enter, boolean firstStart) {
        boolean z = true;
        boolean match = matchConfig(1, enter.mActivityComponent.getClassName());
        if (match) {
            ColorAppEnterInfo info = new ColorAppEnterInfo();
            info.intent = enter.intent;
            info.targetName = enter.mActivityComponent.getClassName();
            info.windowMode = adjustWindowMode(enter);
            info.launchedFromPackage = enter.launchedFromPackage;
            info.firstStart = firstStart;
            if (enter.mUserId == 0) {
                z = false;
            }
            info.multiApp = z;
            info.extension.putInt(WatchlistLoggingHandler.WatchlistEventKeys.UID, enter.getUid());
            if (enter.getTaskRecord() != null) {
                info.extension.putInt(ActivityTaskManagerInternal.ASSIST_TASK_ID, enter.getTaskRecord().taskId);
            } else {
                info.extension.putInt(ActivityTaskManagerInternal.ASSIST_TASK_ID, -1);
            }
            try {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "switchtestlog  notifyActivityEnter , config matched, isStatic = " + this.isStatic + " package =" + this.pkgName);
                }
                this.observer.onActivityEnter(info);
            } catch (RemoteException e) {
                e.printStackTrace();
                Slog.e(TAG, "notifyActivityEnter error, pkgName = " + this.pkgName + " , ActivityRecord = " + enter);
            }
        }
        return match;
    }

    public boolean notifyActivityExit(String activityName, ActivityRecord nextResuming, boolean nextFirstStart) {
        boolean z = true;
        boolean match = matchConfig(1, activityName);
        if (match) {
            try {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "switchtestlog  notifyActivityExit , config matched, isStatic = " + this.isStatic + " package =" + this.pkgName);
                }
                ColorAppExitInfo info = new ColorAppExitInfo();
                info.targetName = activityName;
                if (nextResuming != null) {
                    info.hasResumingActivity = true;
                    info.resumingActivityName = nextResuming.mActivityComponent.getClassName();
                    info.resumingPackageName = nextResuming.packageName;
                    info.resumingWindowMode = adjustWindowMode(nextResuming);
                    info.isResumingFirstStart = nextFirstStart;
                    if (nextResuming.mUserId == 0) {
                        z = false;
                    }
                    info.isResumingMultiApp = z;
                    info.extension.putInt(WatchlistLoggingHandler.WatchlistEventKeys.UID, nextResuming.getUid());
                    if (nextResuming.getTaskRecord() != null) {
                        info.extension.putInt(ActivityTaskManagerInternal.ASSIST_TASK_ID, nextResuming.getTaskRecord().taskId);
                    } else {
                        info.extension.putInt(ActivityTaskManagerInternal.ASSIST_TASK_ID, -1);
                    }
                } else {
                    info.hasResumingActivity = false;
                }
                this.observer.onActivityExit(info);
            } catch (RemoteException e) {
                e.printStackTrace();
                Slog.e(TAG, "notifyActivityEnter error, pkgName = " + this.pkgName + " , activityName = " + activityName);
            }
        }
        return match;
    }

    public boolean notifyAppEnter(ActivityRecord enter, boolean firstStart) {
        boolean match = matchConfig(2, enter.packageName);
        if (match) {
            ColorAppEnterInfo info = new ColorAppEnterInfo();
            info.intent = enter.intent;
            info.targetName = enter.packageName;
            info.windowMode = adjustWindowMode(enter);
            info.launchedFromPackage = enter.launchedFromPackage;
            info.firstStart = firstStart;
            info.multiApp = enter.mUserId != 0;
            info.extension.putInt(WatchlistLoggingHandler.WatchlistEventKeys.UID, enter.getUid());
            if (enter.getTaskRecord() != null) {
                info.extension.putInt(ActivityTaskManagerInternal.ASSIST_TASK_ID, enter.getTaskRecord().taskId);
            } else {
                info.extension.putInt(ActivityTaskManagerInternal.ASSIST_TASK_ID, -1);
            }
            try {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "switchtestlog  notifyAppEnter , config matched, isStatic = " + this.isStatic + " package =" + this.pkgName);
                }
                this.observer.onAppEnter(info);
            } catch (RemoteException e) {
                e.printStackTrace();
                Slog.e(TAG, "notifyActivityEnter error, pkgName = " + this.pkgName + " ,activityRecord =" + enter);
            }
        }
        return match;
    }

    public boolean notifyAppExit(String pkgName2, ActivityRecord nextResuming, boolean nextFirstStart) {
        boolean match = matchConfig(2, pkgName2);
        if (match) {
            try {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "switchtestlog  notifyAppExit , config matched, isStatic = " + this.isStatic + " package =" + this.pkgName);
                }
                ColorAppExitInfo info = new ColorAppExitInfo();
                info.targetName = pkgName2;
                boolean z = false;
                if (nextResuming != null) {
                    info.hasResumingActivity = true;
                    info.resumingActivityName = nextResuming.mActivityComponent.getClassName();
                    info.resumingPackageName = nextResuming.packageName;
                    info.resumingWindowMode = adjustWindowMode(nextResuming);
                    info.isResumingFirstStart = nextFirstStart;
                    if (nextResuming.mUserId != 0) {
                        z = true;
                    }
                    info.isResumingMultiApp = z;
                    info.extension.putInt(WatchlistLoggingHandler.WatchlistEventKeys.UID, nextResuming.getUid());
                    if (nextResuming.getTaskRecord() != null) {
                        info.extension.putInt(ActivityTaskManagerInternal.ASSIST_TASK_ID, nextResuming.getTaskRecord().taskId);
                    } else {
                        info.extension.putInt(ActivityTaskManagerInternal.ASSIST_TASK_ID, -1);
                    }
                } else {
                    info.hasResumingActivity = false;
                }
                this.observer.onAppExit(info);
            } catch (RemoteException e) {
                e.printStackTrace();
                Slog.e(TAG, "notifyAppExit error, pkgName = " + pkgName2);
            }
        }
        return match;
    }

    private HashSet<String> getConfigSet(int type) {
        if (type == 1) {
            return this.config.mActivitySet;
        }
        return this.config.mPackageSet;
    }

    private boolean hasConfig(int category) {
        return getConfigSet(category).size() > 0;
    }

    private boolean matchConfig(int category, String keyword) {
        if (!hasConfig(category)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.i(TAG, "matchConfig match NOTHING, category = " + category + " ,keyword= " + keyword);
            }
            return category == 1 ? this.defaultMatchActivity : this.defaultMatchApp;
        } else if (getConfigSet(category).contains(keyword)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.i(TAG, "matchConfig match SUCCESSED, category = " + category + " ,keyword= " + keyword);
            }
            return true;
        } else if (!ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            return false;
        } else {
            Slog.i(TAG, "matchConfig match FAILED, category = " + category + " ,keyword= " + keyword);
            return false;
        }
    }

    public void setDefaultMatchConfig(boolean defaultMatchApp2, boolean defaultMatchActivity2) {
        if ("android".equalsIgnoreCase(this.pkgName)) {
            this.defaultMatchApp = true;
        } else {
            this.defaultMatchApp = defaultMatchApp2;
        }
        this.defaultMatchActivity = defaultMatchActivity2;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ColorAppSwitchRuleInfo)) {
            return super.equals(obj);
        }
        ColorAppSwitchRuleInfo info = (ColorAppSwitchRuleInfo) obj;
        if (!this.pkgName.equalsIgnoreCase(info.pkgName) || this.config.observerFingerPrint != info.config.observerFingerPrint) {
            return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ColorAppSwitchRuleInfo = { ");
        sb.append(" observer = " + this.observer);
        sb.append(" observerFingerPrint = " + this.config.observerFingerPrint);
        sb.append("}");
        return sb.toString();
    }

    private int adjustWindowMode(ActivityRecord enter) {
        if (enter.getTaskRecord() == null || enter.getTaskRecord().getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            return enter.getWindowingMode();
        }
        return ColorZoomWindowManager.WINDOWING_MODE_ZOOM;
    }
}
