package com.color.splitscreen;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.AppGlobals;
import android.app.ColorActivityTaskManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.WindowManagerGlobal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class ColorSplitScreenManager {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String EVENT_ID_SPLIT_SCREEN_LAUNCH = "split_screen_launch";
    private static final String KEY_LAUNCH_STYLE = "start_style";
    private static final int NAV_BAR_POS_INVALID = -1;
    private static final String OPPO_FORBID_SPLITSCREEN_FEATURE = "coloros.customize.splitscreen.disable";
    private static final String SPLIT_SCREEN_APPID = "20232";
    public static final int SPLIT_SCREEN_FROM_FLOAT_ASSISTANT = 4;
    public static final int SPLIT_SCREEN_FROM_MENU = 2;
    public static final int SPLIT_SCREEN_FROM_NONE = -1;
    public static final int SPLIT_SCREEN_FROM_RECENT = 3;
    public static final int SPLIT_SCREEN_FROM_SERVICE = 1;
    private static final String SPLIT_SCREEN_STATISTIC_ID = "20232001";
    public static final int STATE_APP_NOT_SUPPORT = 1006;
    public static final int STATE_BLACK_LIST = 1004;
    public static final int STATE_CHILDREN_MODE = 1005;
    public static final int STATE_FORBID_SPECIAL_APP = 1008;
    public static final int STATE_FORCE_FULLSCREEN = 1007;
    public static final int STATE_FROBID_LOCKSTACK = 1009;
    public static final int STATE_INVALID = 1000;
    public static final int STATE_SINGLE_HAND = 1003;
    public static final int STATE_SNAPSHOT = 1002;
    public static final int STATE_SUPPORT = 1001;
    private static final String TAG = "ColorSplitScreenManager";
    private static ColorSplitScreenManager sInstance;
    private final String OPPO_ENTERPRISE_DEVELOPMENT_FEATURE = "oppo.business.custom";
    private final String OPPO_SETTINGS_FORBID_SPLITSCREEN = "forbid_splitscreen_by_ep";
    private Display mDisplay = ((DisplayManager) AppGlobals.getInitialApplication().getSystemService(DisplayManager.class)).getDisplay(0);
    private ColorActivityTaskManager mOAms = new ColorActivityTaskManager();

    private ColorSplitScreenManager() {
    }

    public static ColorSplitScreenManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorSplitScreenManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorSplitScreenManager();
                }
            }
        }
        return sInstance;
    }

    public int getSplitScreenState(Intent intent) {
        if (intent != null) {
            if (DEBUG) {
                Log.i(TAG, "getSplitScreenState");
            }
            try {
                return this.mOAms.getSplitScreenState(intent);
            } catch (RemoteException e) {
                Log.e(TAG, "getSplitScreenState remoteException ");
                e.printStackTrace();
                return 1000;
            }
        } else {
            throw new IllegalArgumentException("getSplitScreenState intent=null");
        }
    }

    public int getTopAppSplitScreenState() {
        try {
            return this.mOAms.getSplitScreenState(null);
        } catch (RemoteException e) {
            Log.e(TAG, "getTopAppSplitScreenState remoteException ");
            e.printStackTrace();
            return 1000;
        }
    }

    public void swapDockedFullscreenStack() {
        if (DEBUG) {
            Log.i(TAG, "swapDockedFullscreenStack");
        }
        try {
            this.mOAms.swapDockedFullscreenStack();
        } catch (RemoteException e) {
            Log.e(TAG, "swapDockedFullscreenStack remoteException ");
            e.printStackTrace();
        }
    }

    public boolean splitScreenForTopApp(int type) {
        int activityType;
        if (type != 3) {
            if (DEBUG) {
                Log.i(TAG, "splitWindowForTopApp type:" + type);
            }
            Point realSize = new Point();
            Rect initialBounds = new Rect();
            ((DisplayManager) AppGlobals.getInitialApplication().getSystemService(DisplayManager.class)).getDisplay(0).getRealSize(realSize);
            initialBounds.set(0, 0, realSize.x, realSize.y);
            ActivityManager.RunningTaskInfo runningTask = getRunningTask();
            if (runningTask != null) {
                activityType = runningTask.configuration.windowConfiguration.getActivityType();
            } else {
                activityType = 0;
            }
            boolean isRunningTaskInHomeOrRecentsStack = activityType == 2 || activityType == 3;
            if (runningTask == null || isRunningTaskInHomeOrRecentsStack || !runningTask.supportsSplitScreenMultiWindow) {
                return false;
            }
            try {
                if (ActivityTaskManager.getService().setTaskWindowingModeSplitScreenPrimary(runningTask.id, 0, true, false, initialBounds, true)) {
                    onSplitScreenLaunched(type);
                    return true;
                }
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "setTaskWindowingModeSplitScreenPrimary failed");
                return false;
            }
        } else {
            throw new IllegalArgumentException("splitWindowForTopApp type is abnormal");
        }
    }

    public boolean splitScreenForRecentTasks(int taskId) {
        if (DEBUG) {
            Log.i(TAG, "splitWindowForRecentTasks taskId:" + taskId);
        }
        if (hasEnterpriseForbidScreenScreenFeature()) {
            Log.i(TAG, "splitScreenForRecentTasks is disabled for enterprise order");
            return false;
        }
        try {
            if (hasEnterpriseDevelopmentPlatformFeature() && Settings.Secure.getInt(AppGlobals.getInitialApplication().getContentResolver(), "forbid_splitscreen_by_ep", 0) == 1) {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Enterprise development Platform: splitScreenForRecentTasks exception");
        }
        int navBarPosition = getNavBarPosition();
        if (navBarPosition == -1) {
            return false;
        }
        try {
            ActivityManager.getService().startActivityFromRecents(taskId, makeSplitScreenOptions(navBarPosition != 1).toBundle());
            onSplitScreenLaunched(3);
            return true;
        } catch (Exception e2) {
            Log.e(TAG, "startActivityFromRecents failed");
            return false;
        }
    }

    private ActivityManager.RunningTaskInfo getRunningTask() {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ActivityTaskManager.getService().getFilteredTasks(1, 3, 2);
            if (tasks.isEmpty()) {
                return null;
            }
            return tasks.get(0);
        } catch (RemoteException e) {
            Log.e(TAG, "getRunningTask failed");
            return null;
        }
    }

    private ActivityOptions makeSplitScreenOptions(boolean dockTopLeft) {
        int i;
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchWindowingMode(3);
        if (dockTopLeft) {
            i = 0;
        } else {
            i = 1;
        }
        options.setSplitScreenCreateMode(i);
        return options;
    }

    private int getNavBarPosition() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getNavBarPosition(this.mDisplay.getDisplayId());
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get nav bar position");
            return -1;
        }
    }

    private void onSplitScreenLaunched(int type) {
        HashMap<String, String> logMap = new HashMap<>();
        logMap.put(KEY_LAUNCH_STYLE, type + "");
        OppoStatistics.onCommon((Context) AppGlobals.getInitialApplication(), SPLIT_SCREEN_APPID, SPLIT_SCREEN_STATISTIC_ID, EVENT_ID_SPLIT_SCREEN_LAUNCH, (Map<String, String>) logMap, false);
        if (DEBUG) {
            Log.i(TAG, "onSplitScreenLaunched logMap:" + logMap);
        }
    }

    public int splitScreenForEdgePanel(Intent intent, int userId) {
        if (intent != null) {
            if (DEBUG) {
                Log.i(TAG, "splitWindowForTopApp intent:" + intent);
            }
            if (hasEnterpriseForbidScreenScreenFeature()) {
                Log.i(TAG, "splitScreenForEdgePanel is disabled for enterprise order");
                return 0;
            }
            try {
                if (hasEnterpriseDevelopmentPlatformFeature() && Settings.Secure.getInt(AppGlobals.getInitialApplication().getContentResolver(), "forbid_splitscreen_by_ep", 0) == 1) {
                    return 0;
                }
            } catch (Exception e) {
                Log.e(TAG, "Enterprise development Platform: splitScreenForEdgePanel exception");
            }
            try {
                return this.mOAms.splitScreenForEdgePanel(intent, userId);
            } catch (RemoteException e2) {
                Log.e(TAG, "splitScreenForEdgePanel failed");
                return 0;
            }
        } else {
            throw new IllegalArgumentException("getSplitScreenState intent=null");
        }
    }

    public int getVersion() {
        return 1;
    }

    private boolean hasEnterpriseForbidScreenScreenFeature() {
        try {
            return AppGlobals.getPackageManager().hasSystemFeature(OPPO_FORBID_SPLITSCREEN_FEATURE, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "hasEnterpriseForbidScreenScreenFeature RemoteException");
            return false;
        }
    }

    private boolean hasEnterpriseDevelopmentPlatformFeature() {
        try {
            return AppGlobals.getPackageManager().hasSystemFeature("oppo.business.custom", 0);
        } catch (Exception e) {
            Log.e(TAG, "hasEnterpriseDevelopmentPlatformFeature RemoteException");
            return false;
        }
    }
}
