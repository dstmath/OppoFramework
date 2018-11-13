package com.android.server.am;

import android.os.SystemProperties;
import android.util.Slog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class OppoFreeFormManagerService {
    private static final String COLOROS_LAUNCHER = "com.oppo.launcher";
    private static final String COLOROS_SAFE = "com.coloros.safecenter";
    public static final String FEATURE_OPPO_FREEFORM = "oppo.freeform.unsupport";
    public static final String FREEFORM_CALLER_PKG = "com.android.systemui";
    public static final int FREEFORM_CALLER_UID = 1000;
    public static final String TAG = "OppoFreeFormManagerService";
    private static final Object mLock = new Object();
    public static boolean sDebugfDetail = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static OppoFreeFormManagerService sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private ActivityManagerService mAms = null;
    boolean mDynamicDebug = false;
    private int mParentPid;
    private String mParentPkg;
    private boolean mSupportFreeform = false;
    private int mTaskId;

    public static OppoFreeFormManagerService getInstance() {
        OppoFreeFormManagerService oppoFreeFormManagerService;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new OppoFreeFormManagerService();
            }
            oppoFreeFormManagerService = sInstance;
        }
        return oppoFreeFormManagerService;
    }

    public void init(ActivityManagerService ams) {
        this.mAms = ams;
        this.mSupportFreeform = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_FREEFORM) ^ 1;
        if (this.mSupportFreeform) {
            OppoFreeFormManagerUtils.getInstance().init();
            this.mSupportFreeform = this.mSupportFreeform ? OppoFreeFormManagerUtils.getInstance().isFreeformEnable() : false;
            registerLogModule();
        }
    }

    public boolean isSupportFreeform() {
        return this.mSupportFreeform;
    }

    public boolean inFullscreenCpnList(String cpn) {
        if (this.mSupportFreeform) {
            return OppoFreeFormManagerUtils.getInstance().inFullscreenCpnList(cpn);
        }
        return false;
    }

    public boolean inNextNeedFullscreenCpnList(String cpn) {
        if (this.mSupportFreeform) {
            return OppoFreeFormManagerUtils.getInstance().inNextNeedFullscreenCpnList(cpn);
        }
        return false;
    }

    private boolean isSpecialCpn(String cpn) {
        return OppoFreeFormManagerUtils.getInstance().isSpecialCpn(cpn);
    }

    private boolean isSecureCpn(String cpn) {
        return OppoFreeFormManagerUtils.getInstance().isSecureCpn(cpn);
    }

    protected String getParentPkg() {
        return this.mParentPkg;
    }

    protected int getParentPid() {
        return this.mParentPid;
    }

    protected void resetParentInfo() {
        this.mParentPkg = null;
        this.mTaskId = -1;
        this.mParentPid = -1;
        Slog.v(TAG, "resetParentInfo");
    }

    protected void setParentInfo(ActivityStack stack) {
        if (stack != null && stack.mResumedActivity != null && !COLOROS_SAFE.equals(stack.mResumedActivity.packageName)) {
            this.mParentPkg = stack.mResumedActivity.packageName;
            if (stack.mResumedActivity.app != null) {
                this.mParentPid = stack.mResumedActivity.app.pid;
            }
            if (stack.mResumedActivity.getTask() != null) {
                this.mTaskId = stack.mResumedActivity.getTask().taskId;
            } else {
                this.mTaskId = -1;
            }
            Slog.v(TAG, "setParentInfo: " + this.mParentPkg);
        }
    }

    protected void handleActivitySwitch(ActivityManagerService ams, ActivityRecord next, int stackId) {
        if (stackId != 2 && ams.mStackSupervisor.getStack(2) != null && next != null && this.mTaskId != -1 && next.packageName != null && (next.packageName.equals(this.mParentPkg) ^ 1) != 0 && !COLOROS_SAFE.equals(next.packageName)) {
            TaskRecord task = ams.mStackSupervisor.anyTaskForIdLocked(this.mTaskId, 2, 1);
            if (task != null) {
                ActivityRecord top = task.getTopActivity();
                if ((top != null && (top.visible ^ 1) != 0) || top == null) {
                    resetParentInfo();
                    ams.mStackSupervisor.resizeStackLocked(2, null, null, null, false, true, false);
                    ams.mStackSupervisor.moveTasksToFullscreenStackLocked(2, false);
                    Slog.v(TAG, "handleActivitySwitch: " + next + " ,top: " + top);
                }
            }
        }
    }

    protected void handleParentDied(int pid) {
        if (this.mParentPid != -1 && this.mParentPid == pid && this.mAms != null) {
            resetParentInfo();
            this.mAms.mStackSupervisor.moveTasksToFullscreenStackLocked(2, false);
            Slog.v(TAG, "handleParentDied: " + pid);
        }
    }

    protected ActivityState getParentState() {
        if (this.mAms != null) {
            TaskRecord task = this.mAms.mStackSupervisor.anyTaskForIdLocked(this.mTaskId, 2, 1);
            if (task != null) {
                ActivityRecord top = task.getTopActivity();
                if (top != null) {
                    return top.state;
                }
            }
        }
        return null;
    }

    public void handleApplicationSwitch(String prePkgName, String nextPkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp, boolean isPreForFreeForm, boolean isNextForFreeForm) {
        if (this.mSupportFreeform) {
            if (this.mDynamicDebug) {
                Slog.v(TAG, "handleApplicationSwitch: prePkgName = " + prePkgName + "  nextPkgName = " + nextPkgName + "  prevActivity = " + prevActivity + "  nextActivity = " + nextActivity + "  isPreMultiApp = " + isPreMultiApp + "  isNextMultiApp = " + isNextMultiApp + "  isPreForFreeForm = " + isPreForFreeForm + "  isNextForFreeForm = " + isNextForFreeForm);
            }
            if ((isSecureCpn(prevActivity) && isPreForFreeForm) || (isSecureCpn(nextActivity) && isNextForFreeForm)) {
                if (this.DEBUG_SWITCH) {
                    Slog.v(TAG, "handleApplicationSwitch: return for secure");
                }
                return;
            } else if ((isSpecialCpn(prevActivity) || isSpecialCpn(nextActivity)) && ("com.oppo.launcher".equals(nextPkgName) ^ 1) != 0) {
                if (this.DEBUG_SWITCH) {
                    Slog.v(TAG, "handleApplicationSwitch: return for SpecialCpn");
                }
                return;
            } else if (!COLOROS_SAFE.equals(prePkgName)) {
                ActivityStack stack = this.mAms.mStackSupervisor.getStack(2);
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
                    if (!(record == null || record.packageName == null || !record.packageName.equals(nextPkgName) || (inNextNeedFullscreenCpnList(nextActivity) ^ 1) == 0)) {
                        Slog.v(TAG, "handleApplicationSwitch: nextIsFreeform");
                        nextIsFreeform = true;
                    }
                    if (!(preIsFreeform || (nextIsFreeform ^ 1) == 0)) {
                        this.mAms.mStackSupervisor.resizeStackLocked(2, null, null, null, false, true, false);
                        this.mAms.mStackSupervisor.moveTasksToFullscreenStackLocked(2, false);
                    }
                }
                return;
            } else {
                return;
            }
        }
        if (this.mDynamicDebug) {
            Slog.v(TAG, "handleApplicationSwitch: return!");
        }
    }

    public void handleActivityFromFreeformFullscreen(TaskRecord task) {
        if (this.mDynamicDebug) {
            Slog.v(TAG, "oppo freeform handleActivityFromFreeformFullscreen: task " + task);
        }
        if (task != null && task.mStack != null) {
            ArrayList<ActivityRecord> activities = task.mActivities;
            if (this.mDynamicDebug) {
                Slog.v(TAG, "oppo freeform handleActivityFromFreeformFullscreen: activities " + activities);
            }
            if (!activities.isEmpty()) {
                ActivityRecord r = (ActivityRecord) activities.get(activities.size() - 1);
                boolean isFreeformFullscreen = false;
                if (r != null && r.mIsFreeformFullscreen) {
                    if (this.DEBUG_SWITCH) {
                        Slog.v(TAG, "oppo freeform handleActivityFromFreeformFullscreen: r " + r);
                    }
                    isFreeformFullscreen = true;
                }
                if (isFreeformFullscreen) {
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord record = (ActivityRecord) activities.get(activityNdx);
                        if (record != null) {
                            if (this.DEBUG_SWITCH) {
                                Slog.v(TAG, "oppo freeform requestFinishActivityLocked: record " + record);
                            }
                            task.mStack.requestFinishActivityLocked(record.appToken, 0, null, "handle-freeform-fullscreen", true);
                        }
                    }
                }
            }
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
        OppoFreeFormManagerUtils.getInstance().setDynamicDebugSwitch();
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
            m.invoke(cls.newInstance(), new Object[]{OppoFreeFormManagerService.class.getName()});
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
