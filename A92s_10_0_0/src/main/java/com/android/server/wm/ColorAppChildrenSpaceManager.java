package com.android.server.wm;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.OppoDynamicLogManager;
import java.util.Arrays;
import java.util.List;

public class ColorAppChildrenSpaceManager implements IColorAppChildrenSpaceManager {
    static final String ACTION_CHILDREN_SPACE_LAUNCH = "oppo.intent.action.CHILDREN_SPACE_LAUNCH";
    static final String CHILDREN_SPACE_PKG = "com.coloros.childrenspace";
    private static volatile ColorAppChildrenSpaceManager sInstance;
    private final String TAG = getClass().getSimpleName();
    private boolean isChildSpaceMode;
    private List<String> mAllowLaunchApps;
    private ActivityTaskManagerService mAtms;
    private IColorActivityTaskManagerServiceEx mColorAtmsEx;
    private boolean mDynamicDebug;

    public static ColorAppChildrenSpaceManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorAppChildrenSpaceManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorAppChildrenSpaceManager();
                }
            }
        }
        return sInstance;
    }

    private ColorAppChildrenSpaceManager() {
    }

    public void init(IColorActivityTaskManagerServiceEx atmsEx) {
        registerLogModule();
        if (atmsEx != null) {
            this.mColorAtmsEx = atmsEx;
            this.mAtms = atmsEx.getActivityTaskManagerService();
        }
    }

    public boolean isChildSpaceMode() {
        return this.isChildSpaceMode;
    }

    public void setChildSpaceMode(boolean childSpaceMode) {
        if (this.mDynamicDebug) {
            String str = this.TAG;
            Slog.d(str, "setChildSpaceMode childSpaceMode = " + childSpaceMode);
        }
        this.isChildSpaceMode = childSpaceMode;
    }

    public List<String> getAllowLaunchApps() {
        return this.mAllowLaunchApps;
    }

    public void setAllowLaunchApps(List<String> allowLaunchApps) {
        if (this.mDynamicDebug && allowLaunchApps != null) {
            String str = this.TAG;
            Slog.d(str, "setChildSpaceMode allowLaunchApps = " + Arrays.toString(allowLaunchApps.toArray()) + ",size = " + allowLaunchApps.size());
        }
        if (!isChildSpaceMode() || !(allowLaunchApps == null || allowLaunchApps.size() == 0)) {
            this.mAllowLaunchApps = allowLaunchApps;
            return;
        }
        throw new IllegalArgumentException("bad value, list is null or without data in childrenspace mode");
    }

    public boolean handleChildrenSpaceAppLaunch(ActivityInfo info) {
        if (this.mDynamicDebug) {
            String str = this.TAG;
            Slog.v(str, "handleChildrenSpaceAppLaunch aInfo = " + info);
        }
        if (info == null || info.name == null) {
            return false;
        }
        if (!isChildSpaceMode()) {
            if (this.mDynamicDebug) {
                Slog.v(this.TAG, "handleChildrenSpaceAppLaunch return");
            }
            return false;
        }
        List<String> list = this.mAllowLaunchApps;
        if (list == null) {
            Slog.e(this.TAG, "call error: client set empty data in childrenspace");
            return false;
        } else if (list.contains(info.packageName)) {
            return false;
        } else {
            String str2 = this.TAG;
            Slog.v(str2, "handleChildrenSpaceAppLaunch info = " + info);
            notifyChildrenSpace();
            return true;
        }
    }

    private void notifyChildrenSpace() {
        Intent intent = new Intent(ACTION_CHILDREN_SPACE_LAUNCH);
        intent.setPackage(CHILDREN_SPACE_PKG);
        if (this.mDynamicDebug) {
            String str = this.TAG;
            Slog.v(str, "notifyChildrenSpace intent = " + intent);
        }
        ActivityTaskManagerService activityTaskManagerService = this.mAtms;
        if (activityTaskManagerService != null && activityTaskManagerService.mH != null) {
            this.mAtms.mH.post(new Runnable(intent) {
                /* class com.android.server.wm.$$Lambda$ColorAppChildrenSpaceManager$TOi07P9Usw9wYZdRe1Pyb4Gk */
                private final /* synthetic */ Intent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ColorAppChildrenSpaceManager.this.lambda$notifyChildrenSpace$0$ColorAppChildrenSpaceManager(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$notifyChildrenSpace$0$ColorAppChildrenSpaceManager(Intent intent) {
        this.mAtms.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    public void registerLogModule() {
        OppoDynamicLogManager.getInstance().registerLogModule(getClass().getName());
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
    }

    public void openLog(boolean on) {
        Slog.i(this.TAG, "#####openlog####");
        String str = this.TAG;
        Slog.i(str, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        String str2 = this.TAG;
        Slog.i(str2, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }
}
