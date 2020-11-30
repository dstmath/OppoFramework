package com.android.server.wm;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.ServiceThread;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoBaseActivityManagerService;
import com.android.server.wm.ColorAppSwitchSettings;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.IColorAppSwitchObserver;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColorAppSwitchManagerService implements ColorAppSwitchSettings.OnConfigChangedListener {
    private static final String TAG = "ColorAppSwitchManagerService";
    private static volatile ColorAppSwitchManagerService sInstance;
    private ActivityManagerService mAms;
    private List<AppVisibilityObserver> mAppVisibilityObserver = new ArrayList();
    private OppoBaseActivityManagerService mBaseAms;
    private Context mContext;
    private boolean mInitialized;
    private HashMap<String, ColorAppSwitchStateDispatcher> mIntercepters = new HashMap<>();
    private ActivityRecord mLastActivity;
    private ActivityRecord mLastPausedActivity;
    private ActivityRecord mLastResumedActivity;
    private Handler mNotifyHandler;
    private ServiceThread mNotifyThread;
    private ColorAppSwitchSettings mSettings;

    public static ColorAppSwitchManagerService getInstance() {
        if (sInstance == null) {
            synchronized (ColorAppSwitchManagerService.class) {
                if (sInstance == null) {
                    sInstance = new ColorAppSwitchManagerService();
                }
            }
        }
        return sInstance;
    }

    public synchronized void init(Context context, ActivityManagerService ams) {
        Slog.i(TAG, "init");
        this.mContext = context;
        this.mAms = ams;
        this.mBaseAms = (OppoBaseActivityManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityManagerService.class, ams);
        if (this.mNotifyHandler == null) {
            this.mNotifyThread = new ServiceThread("ColorAppSwitchManagerService: dispatcher", 10, true);
            this.mNotifyThread.start();
            this.mNotifyHandler = new Handler(this.mNotifyThread.getLooper());
        }
        this.mSettings = new ColorAppSwitchSettings(this, this.mContext);
        final ColorAppSwitchSettings settings = this.mSettings;
        this.mNotifyHandler.post(new Runnable() {
            /* class com.android.server.wm.ColorAppSwitchManagerService.AnonymousClass1 */

            public void run() {
                try {
                    settings.init();
                } catch (Exception e) {
                    e.printStackTrace();
                    Slog.e(ColorAppSwitchManagerService.TAG, "ColorAppSwitchSettings init error");
                }
            }
        });
        this.mInitialized = true;
    }

    private boolean initialized() {
        return this.mInitialized;
    }

    private boolean hasProcess(ActivityRecord resumingActivity) {
        if (resumingActivity == null || ((OppoBaseActivityRecord) ColorTypeCastingHelper.typeCasting(OppoBaseActivityRecord.class, resumingActivity)).notifyHotStart) {
            return true;
        }
        Slog.i(TAG, "appswitch hasProcess " + resumingActivity);
        return false;
    }

    public synchronized boolean registerActivityVisibilityListener(AppVisibilityObserver observer) {
        if (observer != null) {
            if (initialized()) {
                if (!this.mAppVisibilityObserver.contains(observer)) {
                    this.mAppVisibilityObserver.add(observer);
                }
                return true;
            }
        }
        Slog.e(TAG, "registerActivityVisibilityListener error, observer = " + observer);
        return false;
    }

    public synchronized boolean unregisterActivityVisibilityListener(AppVisibilityObserver observer) {
        if (observer != null) {
            if (initialized()) {
                this.mAppVisibilityObserver.remove(observer);
                return true;
            }
        }
        Slog.e(TAG, "unregisterActivityVisibilityListener error, observer = " + observer);
        return false;
    }

    public synchronized boolean registerAppSwitchObserver(String pkgName, IColorAppSwitchObserver observer, ColorAppSwitchConfig config) {
        if (initialized() && !TextUtils.isEmpty(pkgName) && observer != null) {
            if (config != null) {
                String encodePkgName = pkgName.replace(".", "@@");
                Slog.i(TAG, "appswitch registerAppSwitchObserver pName = " + encodePkgName + " observer = " + observer + " config = " + config);
                ColorAppSwitchStateDispatcher dispatcher = this.mIntercepters.get(pkgName);
                if (dispatcher == null) {
                    dispatcher = new ColorAppSwitchStateDispatcher(pkgName);
                    this.mIntercepters.put(pkgName, dispatcher);
                }
                ColorAppSwitchRuleInfo info = ColorAppSwitchRuleInfo.buildDynamicRuleInfo(this.mContext, pkgName, observer, config);
                ObserverDeathRecipient deathRecipient = new ObserverDeathRecipient(pkgName, config);
                info.deathRecipient = deathRecipient;
                try {
                    observer.asBinder().linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                dispatcher.addDynamicListener(info);
                matchConfig(dispatcher);
                if (dispatcher.inBlackList) {
                    return false;
                }
                return true;
            }
        }
        Slog.e(TAG, "registerAppSwitchObserver error, pkgName = " + pkgName + " config = " + config);
        return false;
    }

    public synchronized boolean unregisterAppSwitchObserver(String pkgName, ColorAppSwitchConfig config) {
        Slog.i(TAG, "appswitch unregisterAppSwitchObserver pkgName = " + pkgName + " config = " + config);
        if (initialized() && !TextUtils.isEmpty(pkgName)) {
            if (config != null) {
                ColorAppSwitchStateDispatcher dispatcher = this.mIntercepters.get(pkgName);
                if (dispatcher == null) {
                    Slog.e(TAG, "unregisterAppSwitchObserver failed, unknown package = " + pkgName);
                    return false;
                }
                return dispatcher.removeDynamicListener(ColorAppSwitchRuleInfo.buildDynamicRuleInfo(this.mContext, pkgName, null, config));
            }
        }
        Slog.e(TAG, "unregisterAppSwitchObserver error, pkgName = " + pkgName + " config = " + config);
        return false;
    }

    private void notifyAppEnter(ActivityRecord enter, boolean firstStart) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            StringBuilder sb = new StringBuilder();
            sb.append("notifyAppEnter package = ");
            sb.append(enter.packageName);
            sb.append(" ,first = ");
            sb.append(enter.app == null);
            Slog.i(TAG, sb.toString());
        }
        if (!TextUtils.isEmpty(enter.packageName)) {
            for (ColorAppSwitchStateDispatcher dispatcher : this.mIntercepters.values()) {
                dispatcher.notifyAppEnter(enter, firstStart);
            }
        }
    }

    private void notifyAppExit(ActivityRecord pre, ActivityRecord nextResuming, boolean nextFirstStart) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.i(TAG, "notifyAppExit package = " + pre.packageName);
        }
        if (!TextUtils.isEmpty(pre.packageName)) {
            for (ColorAppSwitchStateDispatcher dispatcher : this.mIntercepters.values()) {
                dispatcher.notifyAppExit(pre.packageName, nextResuming, nextFirstStart);
            }
        }
    }

    private void notifyActivityEnter(ActivityRecord enter, boolean firstStart) {
        for (ColorAppSwitchStateDispatcher dispatcher : this.mIntercepters.values()) {
            dispatcher.notifyActivityEnter(enter, firstStart);
        }
    }

    private void notifyActivityExit(ActivityRecord pre, ActivityRecord nextResuming, boolean nextFirstStart) {
        ComponentName componentName = pre.mActivityComponent;
        if (componentName != null) {
            String className = componentName.getClassName();
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.e(TAG, "notifyActivityExit info = " + componentName + " ,className = " + className);
            }
            if (!TextUtils.isEmpty(className)) {
                for (ColorAppSwitchStateDispatcher dispatcher : this.mIntercepters.values()) {
                    dispatcher.notifyActivityExit(className, nextResuming, nextFirstStart);
                }
            }
        }
    }

    public void handleActivityPaused(final ActivityRecord r, final ActivityRecord nextResuming) {
        try {
            if (initialized()) {
                final boolean hotStart = hasProcess(nextResuming);
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(nextResuming != null ? nextResuming.processName : "NULL");
                    sb.append("  handleActivityPaused , nextFirstStart = ");
                    sb.append(!hotStart);
                    Slog.i(TAG, sb.toString());
                    new Exception().printStackTrace();
                }
                if (this.mNotifyHandler != null) {
                    this.mNotifyHandler.post(new Runnable() {
                        /* class com.android.server.wm.ColorAppSwitchManagerService.AnonymousClass2 */

                        public void run() {
                            try {
                                ColorAppSwitchManagerService.this.onActivityPaused(r, nextResuming, !hotStart);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Slog.e(ColorAppSwitchManagerService.TAG, " onActivityPaused error.");
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, " handleActivityPaused error.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActivityPaused(ActivityRecord r, ActivityRecord nextResuming, boolean nextFirstStart) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.i(TAG, "appswitch onActivityPaused, pre = " + r + " nextResuming =" + nextResuming);
        }
        boolean appSwitch = false;
        if (r != null && r.getWindowingMode() != 5) {
            if (nextResuming != null) {
                if (!r.packageName.equals(nextResuming.packageName)) {
                    appSwitch = true;
                }
                this.mLastPausedActivity = null;
            } else {
                this.mLastPausedActivity = r;
            }
            notifyActivityExit(r, nextResuming, nextFirstStart);
            if (appSwitch) {
                notifyAppExit(r, nextResuming, nextFirstStart);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActivityResumed(ActivityRecord r, boolean firstStart) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.i(TAG, "appswitch onActivityResumed, r = " + r + "     mLastResumingActivity = " + this.mLastResumedActivity + "     mLastPausedActivity = " + this.mLastPausedActivity);
        }
        boolean appEnter = false;
        boolean appExit = false;
        if (r != null && r.getWindowingMode() != 5) {
            if (this.mLastResumedActivity == null) {
                appEnter = true;
            } else if (!r.packageName.equals(this.mLastResumedActivity.packageName)) {
                appEnter = true;
            }
            ActivityRecord activityRecord = this.mLastPausedActivity;
            if (activityRecord != null && !activityRecord.packageName.equals(r.packageName)) {
                appExit = true;
            }
            this.mLastResumedActivity = r;
            if (appExit) {
                notifyAppExit(this.mLastPausedActivity, r, firstStart);
                this.mLastPausedActivity = null;
            }
            if (appEnter) {
                notifyAppEnter(r, firstStart);
            }
            notifyActivityEnter(r, firstStart);
        }
    }

    public synchronized void handleAppVisible(ActivityRecord r) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            new Exception().printStackTrace();
        }
        Slog.i(TAG, " handleAppVisible , r = " + r);
        for (AppVisibilityObserver listener : this.mAppVisibilityObserver) {
            listener.onAppVisible(r);
        }
    }

    public void handleActivityResumed(final ActivityRecord r) {
        try {
            if (initialized()) {
                final boolean firstStart = !hasProcess(r);
                if (r != null) {
                    ((OppoBaseActivityRecord) ColorTypeCastingHelper.typeCasting(OppoBaseActivityRecord.class, r)).notifyHotStart = true;
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, r.processName + " handleActivityResumed , firstStart = " + firstStart);
                    new Exception().printStackTrace();
                }
                if (this.mNotifyHandler != null) {
                    this.mNotifyHandler.post(new Runnable() {
                        /* class com.android.server.wm.ColorAppSwitchManagerService.AnonymousClass3 */

                        public void run() {
                            try {
                                ColorAppSwitchManagerService.this.onActivityResumed(r, firstStart);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Slog.e(ColorAppSwitchManagerService.TAG, " onActivityResumed error.");
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, " handleActivityResumed error.");
        }
    }

    private void updateAppSwitchConfig() {
        Set<String> blacks = new HashSet<>(this.mSettings.getBlackList());
        Set<String> whiteApps = new HashSet<>(this.mSettings.getMatchAppDefaultList());
        Set<String> whiteActivities = new HashSet<>(this.mSettings.getMatchActivityDefaultList());
        Map<String, ColorAppSwitchRuleInfo> staticRules = this.mSettings.getConfigRuleInfos();
        for (String staticPkg : staticRules.keySet()) {
            if (!this.mIntercepters.containsKey(staticPkg)) {
                ColorAppSwitchStateDispatcher dispatcher = new ColorAppSwitchStateDispatcher(staticPkg);
                dispatcher.setStaticRule(staticRules.get(staticPkg));
                this.mIntercepters.put(staticPkg, dispatcher);
            }
        }
        for (ColorAppSwitchStateDispatcher dispatcher2 : this.mIntercepters.values()) {
            matchConfig(dispatcher2, blacks, whiteApps, whiteActivities, staticRules);
        }
    }

    private void matchConfig(ColorAppSwitchStateDispatcher dispatcher) {
        matchConfig(dispatcher, new HashSet<>(this.mSettings.getBlackList()), new HashSet<>(this.mSettings.getMatchAppDefaultList()), new HashSet<>(this.mSettings.getMatchActivityDefaultList()), this.mSettings.getConfigRuleInfos());
    }

    private void matchConfig(ColorAppSwitchStateDispatcher dispatcher, Set<String> blacks, Set<String> whiteApps, Set<String> whiteActivities, Map<String, ColorAppSwitchRuleInfo> staticRules) {
        if (blacks.contains(dispatcher.pkgName)) {
            dispatcher.setBlackItem(true);
        } else {
            dispatcher.setBlackItem(false);
        }
        ColorAppSwitchRuleInfo staticRule = staticRules.get(dispatcher.pkgName);
        if (staticRule != null) {
            dispatcher.setStaticRule(staticRule);
        } else {
            dispatcher.setStaticRule(null);
        }
        dispatcher.setDefaultMatchConfig(whiteApps.contains(dispatcher.pkgName), whiteActivities.contains(dispatcher.pkgName));
    }

    @Override // com.android.server.wm.ColorAppSwitchSettings.OnConfigChangedListener
    public void onConfigChanged() {
        Slog.i(TAG, "onConfigChanged");
        updateAppSwitchConfig();
    }

    final class ObserverDeathRecipient implements IBinder.DeathRecipient {
        ColorAppSwitchConfig config;
        final String pkgName;

        ObserverDeathRecipient(String pkgName2, ColorAppSwitchConfig config2) {
            this.pkgName = pkgName2;
            this.config = config2;
        }

        public void binderDied() {
            Slog.i(ColorAppSwitchManagerService.TAG, "hanldeBinderDied");
            ColorAppSwitchManagerService.this.unregisterAppSwitchObserver(this.pkgName, this.config);
        }
    }
}
