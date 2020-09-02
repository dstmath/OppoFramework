package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.OppoBaseIntent;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.ColorLocalServices;
import com.android.server.IColorStrictModeManager;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.IColorGameSpaceManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.IColorAppSwitchManager;
import com.color.util.ColorTypeCastingHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ColorAppSwitchManager implements IColorAppSwitchManager {
    public static final long INTERVAL = 30000;
    public static final String NEXT_ACTIVITY = "next_activity";
    public static final String NEXT_IS_MULTIAPP = "next_is_multiapp";
    public static final String NEXT_PKG = "next_app_pkgname";
    public static final String OPPO_CPN_SAFE_PERMISSIONS = "oppo.permission.OPPO_COMPONENT_SAFE";
    public static final String OPPO_ROM_APP_CHANGE = "oppo.intent.action.ROM_APP_CHANGE";
    public static final String PRE_ACTIVITY = "pre_activity";
    public static final String PRE_IS_MULTIAPP = "pre_is_multiapp";
    public static final String PRE_PKG = "pre_app_pkgname";
    public static final String TAG = "OppoAppSwitchManager";
    private static long sActivateTime = 0;
    private static long sBeginTime = 0;
    private static ColorAppSwitchManager sColorAppSwitchManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static long sLastTime = 0;
    private static int sTaskId = 0;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private List<IColorAppSwitchManager.ActivityChangedListener> mActivityChangedListenerList = new ArrayList();
    private boolean mBroadcastCloseFeature = SystemProperties.getBoolean("persist.sys.assert.panic.appswitch.broadcast.close", false);
    private IColorFreeformManager mColorFreeformManager = null;
    boolean mDynamicDebug = false;
    public String mNext_PkgName = "";
    public String mPre_PkgName = "";

    private ColorAppSwitchManager() {
    }

    public static final ColorAppSwitchManager getInstance() {
        if (sColorAppSwitchManager == null) {
            sColorAppSwitchManager = new ColorAppSwitchManager();
        }
        return sColorAppSwitchManager;
    }

    public String getPrePkgName() {
        return this.mPre_PkgName;
    }

    public String getNextPkgName() {
        return this.mNext_PkgName;
    }

    public void init() {
        registerLogModule();
    }

    public void handleActivitySwitch(Context context, ActivityRecord prevTopActivity, ActivityRecord mResumedActivity, boolean userLeaving) {
        OppoBaseActivityStack baseActivityStack = typeCasting(prevTopActivity.getActivityStack());
        if (baseActivityStack != null) {
            handleActivitySwitch(context, prevTopActivity, mResumedActivity, (mResumedActivity.info.flags & 16384) != 0 && !prevTopActivity.checkEnterPictureInPictureState("resumeTopActivity", userLeaving), baseActivityStack.mComponentName);
        }
    }

    public void handleActivitySwitch(Context context, ActivityRecord prev, ActivityRecord next, boolean dontWaitForPause, ComponentName lastCpn) {
        boolean isFreeFormMMAppBrandUI;
        boolean isNextMultiApp;
        boolean isNextForFreeForm;
        int nextAppUid;
        String nextActivity;
        String nextActivity2;
        String prevActivity;
        String nextActivity3;
        if (this.mDynamicDebug) {
            Slog.i(TAG, "handleActivitySwitch prev = " + prev + "  next = " + next + "  lastCpn = " + lastCpn);
        }
        if (next != null) {
            boolean isFreeFormMMAppBrandUI2 = false;
            if (next.inFreeformWindowingMode()) {
                if (!(prev == null || next.info == null || next.info.name == null || prev.info == null || prev.info.name == null || !"com.tencent.mm.plugin.appbrand.launching.AppBrandLaunchProxyUI".equals(next.info.name) || prev.info.name.equals(next.info.name))) {
                    Slog.i(TAG, "oppo freeform:handleActivitySwitch isFreeFormMMAppBrandUI");
                    isFreeFormMMAppBrandUI2 = true;
                }
                if (isFreeFormMMAppBrandUI2) {
                    if (this.mDynamicDebug) {
                        Slog.i(TAG, "oppo freeform:handleActivitySwitch next is AppBrandLaunchProxyUI!");
                    }
                    isFreeFormMMAppBrandUI = isFreeFormMMAppBrandUI2;
                } else if (sDebugfDetail) {
                    Slog.i(TAG, "oppo freeform:handleActivitySwitch next is freeform app return!");
                    return;
                } else {
                    return;
                }
            } else {
                isFreeFormMMAppBrandUI = false;
            }
            boolean isPreMultiApp = false;
            if (next.mUserId == 999) {
                isNextMultiApp = true;
            } else {
                isNextMultiApp = false;
            }
            boolean isPreForFreeForm = false;
            OppoBaseIntent nextBaseIntent = typeCasting(next.intent);
            if (nextBaseIntent == null || nextBaseIntent.getIsForFreeForm() != 1) {
                isNextForFreeForm = false;
            } else {
                isNextForFreeForm = true;
            }
            int preAppUid = -1;
            if (next.info == null || next.info.name == null) {
                nextActivity = "";
                nextAppUid = -1;
            } else {
                String nextActivity4 = next.info.name;
                if (next.info.applicationInfo != null) {
                    nextActivity = nextActivity4;
                    nextAppUid = next.info.applicationInfo.uid;
                } else {
                    nextActivity = nextActivity4;
                    nextAppUid = -1;
                }
            }
            String lastPkg = "";
            String lastPkg2 = null;
            if (prev == null) {
                if (lastCpn != null) {
                    lastPkg2 = lastCpn.getPackageName();
                }
                String lastPkg3 = lastPkg2 != null ? lastPkg2 : lastPkg;
                if (lastCpn == null || lastCpn.getClassName() == null) {
                    nextActivity2 = nextActivity;
                } else if (OppoListManager.getInstance().isRedundentActivity(lastCpn.getClassName())) {
                    nextActivity2 = nextActivity;
                    if (!OppoListManager.getInstance().isRedundentActivity(nextActivity2)) {
                        notifyExitWechatMiniProgram(context);
                    }
                } else {
                    nextActivity2 = nextActivity;
                }
                notifyActivityChanged(context, next.packageName, lastPkg3, "", nextActivity2, false, isNextMultiApp, false, isNextForFreeForm, -1, nextAppUid);
            } else if (next.packageName != null) {
                if (prev.info == null || prev.info.name == null) {
                    prevActivity = "";
                } else {
                    String prevActivity2 = prev.info.name;
                    if (prev.info.applicationInfo != null) {
                        prevActivity = prevActivity2;
                        preAppUid = prev.info.applicationInfo.uid;
                    } else {
                        prevActivity = prevActivity2;
                    }
                }
                if (prev.mUserId == 999) {
                    isPreMultiApp = true;
                }
                OppoBaseIntent preBaseIntent = typeCasting(prev.intent);
                if (preBaseIntent != null && preBaseIntent.getIsForFreeForm() == 1) {
                    isPreForFreeForm = true;
                }
                if (prev.packageName != null) {
                    String lastPkg4 = prev.packageName;
                    if (dontWaitForPause) {
                        if (lastCpn != null) {
                            lastPkg2 = lastCpn.getPackageName();
                        }
                        lastPkg4 = lastPkg2;
                    }
                    if (lastPkg4 != null) {
                        lastPkg = lastPkg4;
                    }
                    if (!next.packageName.equals(lastPkg) || OppoListManager.getInstance().isRedundentActivity(prevActivity) || OppoListManager.getInstance().isRedundentActivity(nextActivity) || isFreeFormMMAppBrandUI) {
                        if (this.mDynamicDebug) {
                            Slog.i(TAG, "handleActivitySwitch: " + next.packageName);
                        }
                        if (OppoListManager.getInstance().isRedundentActivity(prevActivity) && !OppoListManager.getInstance().isRedundentActivity(nextActivity)) {
                            notifyExitWechatMiniProgram(context);
                        } else if (!OppoListManager.getInstance().isRedundentActivity(prevActivity) && OppoListManager.getInstance().isRedundentActivity(nextActivity)) {
                            notifyEntryWechatMiniProgram(next.getTaskRecord().taskId);
                        }
                        nextActivity3 = nextActivity;
                        notifyActivityChanged(context, next.packageName, lastPkg, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp, isPreForFreeForm, isNextForFreeForm, preAppUid, nextAppUid);
                    } else if (this.mDynamicDebug) {
                        Slog.i(TAG, "handleActivitySwitch the same packageName!");
                    }
                } else {
                    nextActivity3 = nextActivity;
                    if (lastCpn != null) {
                        lastPkg2 = lastCpn.getPackageName();
                    }
                    notifyActivityChanged(context, next.packageName, lastPkg2 != null ? lastPkg2 : lastPkg, prevActivity, nextActivity3, isPreMultiApp, isNextMultiApp, isPreForFreeForm, isNextForFreeForm, preAppUid, nextAppUid);
                }
            } else if (sDebugfDetail) {
                Slog.i(TAG, "handleActivitySwitch: next.packageName is null");
            }
        } else if (sDebugfDetail) {
            Slog.i(TAG, "handleActivitySwitch next is null");
        }
    }

    private void notifyEntryWechatMiniProgram(int taskId) {
        sBeginTime = System.currentTimeMillis();
        if (sTaskId != taskId || sBeginTime - sLastTime > INTERVAL) {
            sActivateTime = sBeginTime;
        }
        sTaskId = taskId;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        return;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    private void notifyExitWechatMiniProgram(Context context) {
        sLastTime = System.currentTimeMillis();
        long duration = sLastTime - sBeginTime;
        ActivityManager.TaskDescription td = ActivityTaskManager.getService().getTaskDescription(sTaskId);
        if (td != null && td.getLabel() != null) {
            OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).collectWechatInfo(td.getLabel(), String.valueOf(duration), String.valueOf(sActivateTime));
        }
    }

    private void notifyActivityChanged(Context context, String nextPkgName, String prePkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp, boolean isPreForFreeForm, boolean isNextForFreeForm, int prePkgUid, int nextPkgUid) {
        this.mPre_PkgName = prePkgName;
        this.mNext_PkgName = nextPkgName;
        onActivityChanged(prePkgName, nextPkgName);
        OppoFeatureCache.get(IColorStrictModeManager.DEFAULT).handleApplicationSwitch(prePkgUid, prePkgName, nextPkgUid, nextPkgName);
        OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).handleApplicationSwitch(prePkgName, nextPkgName, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp, isPreForFreeForm, isNextForFreeForm);
        if (getColorFreeformManager() != null) {
            getColorFreeformManager().handleApplicationSwitch(prePkgName, nextPkgName, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp, isPreForFreeForm, isNextForFreeForm);
        }
    }

    public void sendAppSwitchBroadcast(Context context, String nextPkgName, String prePkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp) {
        if (!this.mBroadcastCloseFeature) {
            if (nextPkgName == null) {
                if (sDebugfDetail) {
                    Slog.i(TAG, "sendAppSwitchBroadcast: nextPkgName is null");
                }
                nextPkgName = "";
            }
            if (this.mDynamicDebug) {
                Slog.i(TAG, "sendAppSwitchBroadcast: prePkgName = " + prePkgName + "    nextPkgName = " + nextPkgName);
            }
            Intent intent = new Intent(OPPO_ROM_APP_CHANGE);
            intent.putExtra(NEXT_PKG, nextPkgName);
            intent.putExtra(PRE_PKG, prePkgName);
            if (OppoListManager.getInstance().isRedundentActivity(prevActivity) || OppoListManager.getInstance().isRedundentActivity(nextActivity)) {
                intent.putExtra(PRE_ACTIVITY, prevActivity);
                intent.putExtra(NEXT_ACTIVITY, nextActivity);
            }
            intent.putExtra(NEXT_IS_MULTIAPP, isNextMultiApp);
            intent.putExtra(PRE_IS_MULTIAPP, isPreMultiApp);
            context.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
        }
    }

    private void onActivityChanged(String prePkg, String nextPkg) {
        List<IColorAppSwitchManager.ActivityChangedListener> list = this.mActivityChangedListenerList;
        if (list != null) {
            for (IColorAppSwitchManager.ActivityChangedListener listener : list) {
                if (listener != null) {
                    listener.onActivityChanged(prePkg, nextPkg);
                }
            }
        }
    }

    public void setActivityChangedListener(IColorAppSwitchManager.ActivityChangedListener activityChangedListener) {
        Slog.i(TAG, "setActivityChangedListener");
        this.mActivityChangedListenerList.add(activityChangedListener);
    }

    public void removeActivityChangedListener(IColorAppSwitchManager.ActivityChangedListener activityChangedListener) {
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
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorAppSwitchManager.class.getName());
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

    private IColorFreeformManager getColorFreeformManager() {
        if (this.mColorFreeformManager == null) {
            this.mColorFreeformManager = (IColorFreeformManager) ColorLocalServices.getService(IColorFreeformManager.class);
        }
        return this.mColorFreeformManager;
    }

    private static OppoBaseIntent typeCasting(Intent intent) {
        if (intent != null) {
            return (OppoBaseIntent) ColorTypeCastingHelper.typeCasting(OppoBaseIntent.class, intent);
        }
        return null;
    }

    private static OppoBaseActivityStack typeCasting(ActivityStack stack) {
        if (stack != null) {
            return (OppoBaseActivityStack) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStack.class, stack);
        }
        return null;
    }
}
