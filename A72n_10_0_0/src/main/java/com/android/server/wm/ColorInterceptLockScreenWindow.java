package com.android.server.wm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.lockscreen.IColorLockScreenCallback;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import oppo.util.OppoStatistics;

/* access modifiers changed from: package-private */
public class ColorInterceptLockScreenWindow {
    private static final String APPID_LOCK_SCREEN_INTERCEPT_TAG = "20092";
    private static final String APPLICATION_LOCK_SCREEN_INTERCEPT_TAG = "20092001";
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String ENGINE_EXP_VERSION_FEATURE = "persist.sys.oppo.region";
    private static final String EVENT_ID_LAUNCH_LOCK_SCREEN_WINDOW = "attmpt_launch_lock_screen_window";
    private static final String INTERCEPTED = "1";
    private static final String KEY_INTERCEPTED_WINDOW_TYPE = "intercepted_window_type";
    private static final String KEY_IS_INTERCEPTED = "is_intercepted";
    private static final String KEY_PKG_NAME_INTERCEPTED = "pkg_name_intercepted";
    private static final String NO_INTERCEPTED = "0";
    private static final String REGION = "CN";
    private static final String SEPARATOR = "##";
    private static final String TAG = "ColorInterceptLockScreenWindow";
    private static final String WINDOW_TYPE_ACTIVITY = "0";
    private static final String WINDOW_TYPE_FLOAT_WINDOW = "1";
    private static final Object mLock = new Object();
    private static ColorInterceptLockScreenWindow sInstance = null;
    private final int DEFAULT_ALLOW = 0;
    private final int MAX_INTERCEPT_SIZE = 5;
    private final int USER_ALLOW = 1;
    private final int USER_REFUSE = 2;
    private final int USER_REMOVE = 3;
    private Context mContext;
    private boolean mExpVersion = DEBUG;
    private final RemoteCallbackList<IColorLockScreenCallback> mIColorLockScreenCallbacks = new RemoteCallbackList<>();
    private boolean mInterceptFeature = true;
    private WindowList<WindowState> mInterceptWindowList = new WindowList<>();
    private ActivityRecord mLastInterceptActivity;
    private WindowState mLastInterceptWindow;
    private HashMap<String, Integer> mPackageConfigList = new HashMap<>();
    private PowerManager sPowerManager;
    private WindowManagerService sWMService;

    public static ColorInterceptLockScreenWindow getInstance() {
        if (sInstance == null) {
            synchronized (mLock) {
                if (sInstance == null) {
                    sInstance = new ColorInterceptLockScreenWindow();
                }
            }
        }
        return sInstance;
    }

    private ColorInterceptLockScreenWindow() {
    }

    public void systemReady(Context context) {
        this.mExpVersion = !SystemProperties.get(ENGINE_EXP_VERSION_FEATURE, REGION).equalsIgnoreCase(REGION);
        if (this.mExpVersion) {
            this.mInterceptFeature = DEBUG;
        } else {
            this.mInterceptFeature = true;
        }
        this.mContext = context;
    }

    public boolean interceptWindow(Context context, ActivityRecord r, boolean keyguardLocked, boolean showWhenLocked, boolean dismissKeyguard, boolean showDialog) {
        if (!keyguardLocked) {
            return DEBUG;
        }
        if (!showWhenLocked && !dismissKeyguard) {
            return DEBUG;
        }
        if (interceptWindow(context, r.packageName, showDialog)) {
            if (this.sPowerManager == null) {
                this.sPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
            }
            if (r.getTurnScreenOnFlag() && this.sPowerManager != null) {
                if (DEBUG) {
                    Slog.d(TAG, "interceptWindow turnScreenOn for: " + r);
                }
                this.sPowerManager.wakeUp(SystemClock.uptimeMillis(), 2, "ColorInterceptLockScreenWindow:TURN_ON");
            }
            r.makeFinishingLocked();
            reportInterceptActivity(r, "1");
            return true;
        }
        reportInterceptActivity(r, "0");
        return DEBUG;
    }

    public boolean interceptWindow(Context context, String packageName, boolean showDialog) {
        if (DEBUG) {
            Slog.d(TAG, "interceptWindow mInterceptFeature:" + this.mInterceptFeature + " packageName:" + packageName);
        }
        if (!this.mInterceptFeature || context == null || packageName == null) {
            return DEBUG;
        }
        if (isSystemApp(context, packageName)) {
            if (DEBUG) {
                Slog.d(TAG, "isSystemApp packageName: " + packageName);
            }
            return DEBUG;
        } else if (packageName.contains("android.server.cts") || packageName.contains("android.server.am") || packageName.contains("com.opposs.marketdemo") || packageName.contains("com.oppo.daydreamvideo") || packageName.contains("android.server.wm.app") || packageName.contains("com.android.compatibility.common.deviceinfo") || packageName.contains("android.server.wm.cts") || packageName.contains("com.android.cts.devicepolicy")) {
            if (DEBUG) {
                Slog.d(TAG, "is App running return! app : " + packageName);
            }
            return DEBUG;
        } else if (!this.mPackageConfigList.containsKey(packageName)) {
            if (showDialog) {
                if (DEBUG) {
                    Slog.d(TAG, "showUserSwitchDialog for " + packageName);
                }
                showDialogForIntercpet(packageName);
            }
            return true;
        } else if (this.mPackageConfigList.get(packageName).intValue() != 2) {
            return DEBUG;
        } else {
            if (DEBUG) {
                Slog.d(TAG, "interceptWindow for " + packageName);
            }
            return true;
        }
    }

    public boolean InterceptFloatWindow(WindowManagerService ws, Context context, WindowState win, boolean keyguardLocked, boolean showDialog) {
        if (keyguardLocked && (win.mAppOp == 24 || win.mAppOp == 45)) {
            boolean dismissKeyguard = (win.mAttrs.flags & 4194304) != 0;
            boolean showWhenLocked = (win.mAttrs.flags & 524288) != 0;
            if (dismissKeyguard || showWhenLocked) {
                if (interceptWindow(context, win.getOwningPackage(), showDialog)) {
                    if (DEBUG) {
                        Slog.d(TAG, "InterceptFloatWindow:" + win);
                    }
                    if (this.sWMService == null) {
                        this.sWMService = ws;
                    }
                    if (this.mInterceptWindowList.size() > 5) {
                        Slog.d(TAG, "mInterceptWindowList size:" + this.mInterceptWindowList.size());
                        this.mInterceptWindowList.clear();
                    }
                    this.mInterceptWindowList.add(win);
                    win.hideLw((boolean) DEBUG, (boolean) DEBUG);
                    reportInterceptFloatWindow(win, "1");
                    return true;
                }
                reportInterceptFloatWindow(win, "0");
            }
        }
        return DEBUG;
    }

    public boolean interceptDisableKeyguard(Context context, int uid) {
        return DEBUG;
    }

    private boolean isSystemApp(Context context, String pkg) {
        if (pkg != null) {
            try {
                PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(pkg, 0);
                if (pkgInfo != null) {
                    if (pkgInfo.applicationInfo != null) {
                        if ((pkgInfo.applicationInfo.flags & 1) != 0) {
                            return true;
                        }
                    }
                }
                return DEBUG;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return DEBUG;
    }

    public void setPackageStateToIntercept(Map<String, Integer> packageMap) {
        synchronized (this.mPackageConfigList) {
            if (packageMap.size() > 1) {
                this.mPackageConfigList.clear();
            }
            for (Map.Entry<String, Integer> entry : packageMap.entrySet()) {
                if (DEBUG) {
                    Slog.d(TAG, "setPackageStateToIntercept:" + entry.getKey() + " " + entry.getValue());
                }
                if (entry.getValue().intValue() != 3) {
                    this.mPackageConfigList.put(entry.getKey(), entry.getValue());
                } else if (this.mPackageConfigList.containsKey(entry.getKey())) {
                    this.mPackageConfigList.remove(entry.getKey());
                }
            }
        }
    }

    public boolean registerLockScreenCallback(IColorLockScreenCallback callback) {
        if (callback == null) {
            return DEBUG;
        }
        if (DEBUG) {
            Slog.d(TAG, "registerLockScreenCallback callback: " + callback);
        }
        return this.mIColorLockScreenCallbacks.register(callback);
    }

    public boolean unregisterLockScreenCallback(IColorLockScreenCallback callback) {
        if (DEBUG) {
            Slog.d(TAG, "unregisterLockScreenCallback callback: " + callback);
        }
        return this.mIColorLockScreenCallbacks.unregister(callback);
    }

    public void resolveScreenOnFlag(ActivityRecord record, boolean turnScreenOn) {
        Context context;
        if (turnScreenOn) {
            if (this.sPowerManager == null && (context = this.mContext) != null) {
                this.sPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
            }
            if (interceptWindow(this.mContext, record.packageName, DEBUG) && this.sPowerManager != null) {
                if (DEBUG) {
                    Slog.d(TAG, "resolveScreenOnFlag turnScreenOn for: " + record);
                }
                this.sPowerManager.wakeUp(SystemClock.uptimeMillis(), 2, "ColorInterceptLockScreenWindow:TURN_ON");
            }
        }
    }

    public void handleKeyguardGoingAway(boolean keyguardGoingAway) {
        WindowManagerService windowManagerService;
        if (keyguardGoingAway && (windowManagerService = this.sWMService) != null) {
            synchronized (windowManagerService.mGlobalLock) {
                this.sWMService.getDefaultDisplayContentLocked().forAllWindows(new Consumer() {
                    /* class com.android.server.wm.$$Lambda$ColorInterceptLockScreenWindow$Jw5Lw7cUzS8nRVFN3TULvW_6eAU */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ColorInterceptLockScreenWindow.this.lambda$handleKeyguardGoingAway$0$ColorInterceptLockScreenWindow((WindowState) obj);
                    }
                }, (boolean) DEBUG);
            }
            this.mInterceptWindowList.clear();
        }
    }

    public /* synthetic */ void lambda$handleKeyguardGoingAway$0$ColorInterceptLockScreenWindow(WindowState w) {
        if ((w.mAppOp == 24 || w.mAppOp == 45) && w.getOwningPackage() != null && this.mInterceptWindowList.contains(w)) {
            w.showLw((boolean) DEBUG, (boolean) DEBUG);
        }
    }

    private void showDialogForIntercpet(String packageName) {
        try {
            int size = this.mIColorLockScreenCallbacks.beginBroadcast();
            if (DEBUG) {
                Slog.d(TAG, "showDialogForIntercpet:" + packageName);
            }
            for (int i = 0; i < size; i++) {
                try {
                    this.mIColorLockScreenCallbacks.getBroadcastItem(i).showDialogForIntercpet(packageName);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error showDialogForIntercpet:", e);
                }
            }
            this.mIColorLockScreenCallbacks.finishBroadcast();
        } catch (Exception e2) {
            Slog.e(TAG, "Exception showDialogForIntercpet:", e2);
        }
    }

    private void reportInterceptFloatWindow(WindowState win, String isIntercepted) {
        WindowState windowState = this.mLastInterceptWindow;
        if ((windowState == null || windowState != win) && win.getOwningPackage() != null) {
            this.mLastInterceptWindow = win;
            Map<String, String> logMap = new HashMap<>();
            logMap.put(KEY_PKG_NAME_INTERCEPTED, win.getOwningPackage() + SEPARATOR + ((Object) win.getWindowTag()));
            logMap.put(KEY_INTERCEPTED_WINDOW_TYPE, "1");
            logMap.put(KEY_IS_INTERCEPTED, isIntercepted);
            OppoStatistics.onCommon(this.mContext, APPID_LOCK_SCREEN_INTERCEPT_TAG, APPLICATION_LOCK_SCREEN_INTERCEPT_TAG, EVENT_ID_LAUNCH_LOCK_SCREEN_WINDOW, logMap, (boolean) DEBUG);
        }
    }

    private void reportInterceptActivity(ActivityRecord r, String isIntercepted) {
        ActivityRecord activityRecord = this.mLastInterceptActivity;
        if ((activityRecord == null || activityRecord != r) && r.packageName != null) {
            this.mLastInterceptActivity = r;
            Map<String, String> logMap = new HashMap<>();
            logMap.put(KEY_PKG_NAME_INTERCEPTED, r.shortComponentName);
            logMap.put(KEY_INTERCEPTED_WINDOW_TYPE, "0");
            logMap.put(KEY_IS_INTERCEPTED, isIntercepted);
            OppoStatistics.onCommon(this.mContext, APPID_LOCK_SCREEN_INTERCEPT_TAG, APPLICATION_LOCK_SCREEN_INTERCEPT_TAG, EVENT_ID_LAUNCH_LOCK_SCREEN_WINDOW, logMap, (boolean) DEBUG);
        }
    }
}
