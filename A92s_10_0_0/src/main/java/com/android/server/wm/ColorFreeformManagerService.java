package com.android.server.wm;

import android.app.ActivityOptions;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.ActivityStack;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorFreeformManagerService extends ColorDummyFreeformManager {
    private static final String COLOROS_LAUNCHER = "com.oppo.launcher";
    private static final String COLOROS_RECENTS = "com.coloros.recents";
    private static final String COLOROS_SAFE = "com.coloros.safecenter";
    public static final String FEATURE_OPPO_FREEFORM = "oppo.freeform.unsupport";
    public static final String FREEFORM_CALLER_PKG = "com.android.systemui";
    public static final int FREEFORM_CALLER_UID = 1000;
    public static final String TAG = "ColorFreeformManagerService";
    private static final Object mLock = new Object();
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorFreeformManagerService sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private ActivityTaskManagerService mAtms = null;
    private IColorActivityTaskManagerServiceEx mColorAmsEx = null;
    private IColorWindowManagerServiceEx mColorWmsEx = null;
    boolean mDynamicDebug = false;
    private int mParentPid;
    private String mParentPkg;
    private boolean mSupportFreeform = false;
    private int mTaskId;

    public static ColorFreeformManagerService getInstance() {
        ColorFreeformManagerService colorFreeformManagerService;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new ColorFreeformManagerService();
            }
            colorFreeformManagerService = sInstance;
        }
        return colorFreeformManagerService;
    }

    public void init(IColorActivityTaskManagerServiceEx amsEx, IColorWindowManagerServiceEx wmsEx) {
        if (amsEx != null) {
            this.mColorAmsEx = amsEx;
            this.mAtms = amsEx.getActivityTaskManagerService();
        }
        if (wmsEx != null) {
            this.mColorWmsEx = wmsEx;
        }
        boolean z = true;
        this.mSupportFreeform = !this.mAtms.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_FREEFORM);
        if (this.mSupportFreeform) {
            ColorFreeformConfig.getInstance().init();
            ColorWmsFreeformHelp.getInstance().init(this.mColorWmsEx);
            if (!this.mSupportFreeform || !ColorFreeformConfig.getInstance().isFreeformEnable()) {
                z = false;
            }
            this.mSupportFreeform = z;
            registerLogModule();
        }
    }

    public boolean isSupportFreeform() {
        return this.mSupportFreeform;
    }

    public boolean inFullscreenCpnList(String cpn) {
        if (this.mSupportFreeform) {
            return ColorFreeformConfig.getInstance().inFullscreenCpnList(cpn);
        }
        return false;
    }

    public boolean inNextNeedFullscreenCpnList(String cpn) {
        if (this.mSupportFreeform) {
            return ColorFreeformConfig.getInstance().inNextNeedFullscreenCpnList(cpn);
        }
        return false;
    }

    private boolean isSpecialCpn(String cpn) {
        return ColorFreeformConfig.getInstance().isSpecialCpn(cpn);
    }

    private boolean isSecureCpn(String cpn) {
        return ColorFreeformConfig.getInstance().isSecureCpn(cpn);
    }

    /* access modifiers changed from: protected */
    public String getParentPkg() {
        return this.mParentPkg;
    }

    /* access modifiers changed from: protected */
    public int getParentPid() {
        return this.mParentPid;
    }

    public void resetParentInfo() {
        this.mParentPkg = null;
        this.mTaskId = -1;
        this.mParentPid = -1;
        Slog.v(TAG, "resetParentInfo");
    }

    public void setParentInfo(ActivityStack stack) {
        if (stack != null && stack.mResumedActivity != null && !COLOROS_SAFE.equals(stack.mResumedActivity.packageName)) {
            this.mParentPkg = stack.mResumedActivity.packageName;
            if (stack.mResumedActivity.app != null) {
                this.mParentPid = stack.mResumedActivity.app.getPid();
            }
            if (stack.mResumedActivity.getTaskRecord() != null) {
                this.mTaskId = stack.mResumedActivity.getTaskRecord().taskId;
            } else {
                this.mTaskId = -1;
            }
            Slog.v(TAG, "setParentInfo: " + this.mParentPkg);
        }
    }

    public void handleFreeformDied(boolean isFreeform, ActivityRecord r) {
    }

    public void handleParentDied(int pid) {
        int i = this.mParentPid;
        if (i != -1 && i == pid && this.mAtms != null) {
            resetParentInfo();
            ActivityStack freeformStack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(5, 1);
            if (freeformStack != null) {
                this.mAtms.mStackSupervisor.moveTasksToFullscreenStackLocked(freeformStack, false);
                Slog.v(TAG, "handleParentDied: " + pid);
            }
        }
    }

    public ActivityStack.ActivityState getParentState() {
        TaskRecord task;
        ActivityRecord top;
        ActivityTaskManagerService activityTaskManagerService = this.mAtms;
        if (activityTaskManagerService == null || (task = activityTaskManagerService.mRootActivityContainer.anyTaskForId(this.mTaskId, 2)) == null || (top = task.getTopActivity()) == null) {
            return null;
        }
        return top.getState();
    }

    public void handleApplicationSwitch(String prePkgName, String nextPkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp, boolean isPreForFreeForm, boolean isNextForFreeForm) {
        if (this.mSupportFreeform) {
            if (this.mDynamicDebug) {
                Slog.v(TAG, "handleApplicationSwitch: prePkgName = " + prePkgName + "  nextPkgName = " + nextPkgName + "  prevActivity = " + prevActivity + "  nextActivity = " + nextActivity + "  isPreMultiApp = " + isPreMultiApp + "  isNextMultiApp = " + isNextMultiApp + "  isPreForFreeForm = " + isPreForFreeForm + "  isNextForFreeForm = " + isNextForFreeForm);
            }
            if ((!isSecureCpn(prevActivity) || !isPreForFreeForm) && (!isSecureCpn(nextActivity) || !isNextForFreeForm)) {
                if ((isSpecialCpn(prevActivity) || isSpecialCpn(nextActivity)) && !"com.oppo.launcher".equals(nextPkgName)) {
                    if (this.DEBUG_SWITCH) {
                        Slog.v(TAG, "handleApplicationSwitch: return for SpecialCpn");
                    }
                } else if (!COLOROS_SAFE.equals(prePkgName)) {
                    ActivityStack stack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(5, 1);
                    if (this.mDynamicDebug) {
                        Slog.v(TAG, "handleApplicationSwitch: stack " + stack);
                    }
                    if (stack != null) {
                        boolean preIsFreeform = false;
                        boolean nextIsFreeform = false;
                        ActivityRecord record = stack.mResumedActivity;
                        if (this.DEBUG_SWITCH) {
                            Slog.v(TAG, "handleApplicationSwitch: record " + record);
                        }
                        if (!(record == null || record.packageName == null || !record.packageName.equals(prePkgName))) {
                            Slog.v(TAG, "handleApplicationSwitch: preIsFreeform");
                            preIsFreeform = true;
                        }
                        if (record != null && record.packageName != null && record.packageName.equals(nextPkgName) && !inNextNeedFullscreenCpnList(nextActivity)) {
                            Slog.v(TAG, "handleApplicationSwitch: nextIsFreeform");
                            nextIsFreeform = true;
                        }
                        if (((!preIsFreeform && !nextIsFreeform) || "com.oppo.launcher".equals(nextPkgName) || COLOROS_RECENTS.equals(nextPkgName)) && record != null && record.getTaskRecord() != null) {
                            stack.moveTaskToBackLocked(record.getTaskRecord().taskId);
                        }
                    }
                }
            } else if (this.DEBUG_SWITCH) {
                Slog.v(TAG, "handleApplicationSwitch: return for secure");
            }
        } else if (this.mDynamicDebug) {
            Slog.v(TAG, "handleApplicationSwitch: return!");
        }
    }

    public void handleActivityFromFreeformFullscreen(TaskRecord task) {
    }

    public boolean exitFreeformIfNeed(ActivityRecord resumeActivity, ActivityOptions options) {
        if (resumeActivity == null) {
            Slog.w(TAG, "exitFreeformIfNedd invalid params");
            return false;
        } else if (resumeActivity.info == null || !inFullscreenCpnList(resumeActivity.info.name)) {
            return false;
        } else {
            if (options != null) {
                Slog.i(TAG, "oppo freeform exitFreeformIfNeed move freeform to fullscreen = " + resumeActivity);
                options.setLaunchWindowingMode(1);
            }
            return true;
        }
    }

    public boolean skipPauseBackStackIfNeed(ActivityStack stack, ActivityRecord resuming) {
        if (stack == null || resuming == null) {
            Slog.i(TAG, "skipPauseBackStackIfNeed invalid params stack = " + stack + "resuming = " + resuming);
        }
        ActivityStack resumingStack = resuming.getActivityStack();
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "skipPauseBackStackIfNeed stack = " + stack + " resumingStack = " + resumingStack + " resuming = " + resuming + "resumingWindowMode = " + resuming.getWindowingMode());
        }
        if (resumingStack == null) {
            return false;
        }
        if ((stack.getWindowingMode() != 1 || !resumingStack.inFreeformWindowingMode()) && (!stack.inFreeformWindowingMode() || resumingStack.getWindowingMode() != 1)) {
            return false;
        }
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "skipPauseBackStackIfNeed skip pause stack = " + stack);
        }
        return true;
    }

    public void startFullscreenCpn(ActivityStack targetStack, ActivityRecord startActivity) {
    }

    public void oppoStartFreezingDisplayLocked() {
        if (ColorWmsFreeformHelp.getInstance() != null) {
            ColorWmsFreeformHelp.getInstance().oppoStartFreezingDisplayLocked();
        }
    }

    public void oppoStopFreezingDisplayLocked() {
        if (ColorWmsFreeformHelp.getInstance() != null) {
            ColorWmsFreeformHelp.getInstance().oppoStopFreezingDisplayLocked();
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
        ColorFreeformConfig.getInstance().setDynamicDebugSwitch();
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
            m.invoke(cls.newInstance(), ColorFreeformManagerService.class.getName());
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
