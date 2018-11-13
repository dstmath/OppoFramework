package com.android.server.am;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.coloros.OppoListManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class OppoAppSwitchManager {
    public static final String NEXT_ACTIVITY = "next_activity";
    public static final String NEXT_IS_MULTIAPP = "next_is_multiapp";
    public static final String NEXT_PKG = "next_app_pkgname";
    public static final String OPPO_ROM_APP_CHANGE = "oppo.intent.action.ROM_APP_CHANGE";
    public static final String PRE_ACTIVITY = "pre_activity";
    public static final String PRE_IS_MULTIAPP = "pre_is_multiapp";
    public static final String PRE_PKG = "pre_app_pkgname";
    public static final String TAG = "OppoAppSwitchManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static OppoAppSwitchManager sOppoAppSwitchManager = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private List<ActivityChangedListener> mActivityChangedListenerList = new ArrayList();
    boolean mDynamicDebug = false;
    public String mNext_PkgName = "";
    public String mPre_PkgName = "";

    public interface ActivityChangedListener {
        void onActivityChanged(String str, String str2);
    }

    public static final OppoAppSwitchManager getInstance() {
        if (sOppoAppSwitchManager == null) {
            sOppoAppSwitchManager = new OppoAppSwitchManager();
        }
        return sOppoAppSwitchManager;
    }

    public void init(Context context) {
        registerLogModule();
    }

    public void handleActivitySwitch(Context context, ActivityRecord prev, ActivityRecord next, boolean dontWaitForPause, ComponentName lastCpn) {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "handleActivitySwitch prev = " + prev + "  next = " + next + "  lastCpn = " + lastCpn);
        }
        if (next == null) {
            if (sDebugfDetail) {
                Slog.i(TAG, "handleActivitySwitch next is null");
            }
            return;
        }
        boolean isFreeFormMMAppBrandUI = false;
        if (next.isFreeform()) {
            if (!(prev == null || next == null || next.info == null || next.info.name == null || prev.info == null || prev.info.name == null || !"com.tencent.mm.plugin.appbrand.launching.AppBrandLaunchProxyUI".equals(next.info.name) || (prev.info.name.equals(next.info.name) ^ 1) == 0)) {
                Slog.i(TAG, "oppo freeform:handleActivitySwitch isFreeFormMMAppBrandUI");
                isFreeFormMMAppBrandUI = true;
            }
            if (!isFreeFormMMAppBrandUI) {
                if (sDebugfDetail) {
                    Slog.i(TAG, "oppo freeform:handleActivitySwitch next is freeform app return!");
                }
                return;
            } else if (this.mDynamicDebug) {
                Slog.i(TAG, "oppo freeform:handleActivitySwitch next is AppBrandLaunchProxyUI!");
            }
        }
        boolean isPreMultiApp = false;
        boolean isNextMultiApp = false;
        if (next.userId == OppoMultiAppManager.USER_ID) {
            isNextMultiApp = true;
        }
        boolean isPreForFreeForm = false;
        boolean isNextForFreeForm = false;
        if (next.intent != null && next.intent.getIsForFreeForm() == 1) {
            isNextForFreeForm = true;
        }
        String prevActivity = "";
        String nextActivity = "";
        int preAppUid = -1;
        int nextAppUid = -1;
        if (!(next.info == null || next.info.name == null)) {
            nextActivity = next.info.name;
            if (next.info.applicationInfo != null) {
                nextAppUid = next.info.applicationInfo.uid;
            }
        }
        String lastPkg;
        if (prev == null) {
            lastPkg = lastCpn != null ? lastCpn.getPackageName() : null;
            if (lastPkg == null) {
                lastPkg = "";
            }
            notifyActivityChanged(context, next.packageName, lastPkg, prevActivity, nextActivity, false, isNextMultiApp, false, isNextForFreeForm, -1, nextAppUid);
        } else if (next.packageName == null) {
            if (sDebugfDetail) {
                Slog.i(TAG, "handleActivitySwitch: next.packageName is null");
            }
        } else {
            if (!(prev.info == null || prev.info.name == null)) {
                prevActivity = prev.info.name;
                if (prev.info.applicationInfo != null) {
                    preAppUid = prev.info.applicationInfo.uid;
                }
            }
            if (prev.userId == OppoMultiAppManager.USER_ID) {
                isPreMultiApp = true;
            }
            if (prev.intent != null && prev.intent.getIsForFreeForm() == 1) {
                isPreForFreeForm = true;
            }
            if (prev.packageName != null) {
                lastPkg = prev.packageName;
                if (dontWaitForPause) {
                    lastPkg = lastCpn != null ? lastCpn.getPackageName() : null;
                }
                if (lastPkg == null) {
                    lastPkg = "";
                }
                if (!next.packageName.equals(lastPkg) || (OppoListManager.getInstance().isRedundentActivity(prevActivity) ^ 1) == 0 || (OppoListManager.getInstance().isRedundentActivity(nextActivity) ^ 1) == 0 || (isFreeFormMMAppBrandUI ^ 1) == 0) {
                    if (this.mDynamicDebug) {
                        Slog.i(TAG, "handleActivitySwitch: " + next.packageName);
                    }
                    notifyActivityChanged(context, next.packageName, lastPkg, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp, isPreForFreeForm, isNextForFreeForm, preAppUid, nextAppUid);
                } else {
                    if (this.mDynamicDebug) {
                        Slog.i(TAG, "handleActivitySwitch the same packageName!");
                    }
                    return;
                }
            }
            lastPkg = lastCpn != null ? lastCpn.getPackageName() : null;
            if (lastPkg == null) {
                lastPkg = "";
            }
            notifyActivityChanged(context, next.packageName, lastPkg, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp, isPreForFreeForm, isNextForFreeForm, preAppUid, nextAppUid);
        }
    }

    private void notifyActivityChanged(Context context, String nextPkgName, String prePkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp, boolean isPreForFreeForm, boolean isNextForFreeForm, int prePkgUid, int nextPkgUid) {
        sendAppSwitchBroadcast(context, nextPkgName, prePkgName, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp);
        onActivityChanged(prePkgName, nextPkgName);
        OppoProcessManager.getInstance().handleApplicationSwitch(prePkgUid, prePkgName, nextPkgUid, nextPkgName);
        OppoGameSpaceManager.getInstance().handleApplicationSwitch(prePkgName, nextPkgName, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp, isPreForFreeForm, isNextForFreeForm);
        OppoFreeFormManagerService.getInstance().handleApplicationSwitch(prePkgName, nextPkgName, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp, isPreForFreeForm, isNextForFreeForm);
    }

    public void sendAppSwitchBroadcast(Context context, String nextPkgName, String prePkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp) {
        if (nextPkgName == null) {
            if (sDebugfDetail) {
                Slog.i(TAG, "sendAppSwitchBroadcast: nextPkgName is null");
            }
            nextPkgName = "";
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "sendAppSwitchBroadcast: prePkgName = " + prePkgName + "    nextPkgName = " + nextPkgName);
        }
        this.mPre_PkgName = prePkgName;
        this.mNext_PkgName = nextPkgName;
        Intent intent = new Intent(OPPO_ROM_APP_CHANGE);
        intent.putExtra(NEXT_PKG, nextPkgName);
        intent.putExtra(PRE_PKG, prePkgName);
        if (OppoListManager.getInstance().isRedundentActivity(prevActivity) || OppoListManager.getInstance().isRedundentActivity(nextActivity)) {
            intent.putExtra(PRE_ACTIVITY, prevActivity);
            intent.putExtra(NEXT_ACTIVITY, nextActivity);
        }
        intent.putExtra(NEXT_IS_MULTIAPP, isNextMultiApp);
        intent.putExtra(PRE_IS_MULTIAPP, isPreMultiApp);
        context.sendBroadcast(intent);
    }

    private void onActivityChanged(String prePkg, String nextPkg) {
        if (this.mActivityChangedListenerList != null) {
            for (ActivityChangedListener listener : this.mActivityChangedListenerList) {
                if (listener != null) {
                    listener.onActivityChanged(prePkg, nextPkg);
                }
            }
        }
    }

    public void setActivityChangedListener(ActivityChangedListener activityChangedListener) {
        Slog.i(TAG, "setActivityChangedListener");
        this.mActivityChangedListenerList.add(activityChangedListener);
    }

    public void removeActivityChangedListener(ActivityChangedListener activityChangedListener) {
        this.mActivityChangedListenerList.remove(activityChangedListener);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", new Class[]{String.class});
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), new Object[]{OppoAppSwitchManager.class.getName()});
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }
}
