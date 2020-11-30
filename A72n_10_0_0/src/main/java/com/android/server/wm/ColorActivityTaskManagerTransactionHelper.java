package com.android.server.wm;

import android.app.IActivityController;
import android.app.IColorActivityTaskManager;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IColorKeyEventObserver;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.am.IColorEdgeTouchManager;
import com.android.server.am.IColorKeyEventManager;
import com.android.server.am.IColorKeyLayoutManager;
import com.color.app.ColorAppInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.IColorAppSwitchObserver;
import com.color.app.IColorFreeformConfigChangedListener;
import com.color.app.IColorZoomWindowConfigChangedListener;
import com.color.lockscreen.IColorLockScreenCallback;
import com.color.zoomwindow.ColorZoomWindowInfo;
import com.color.zoomwindow.ColorZoomWindowManager;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import com.color.zoomwindow.IColorZoomWindowObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ColorActivityTaskManagerTransactionHelper extends ColorActivityTaskManagerCommonHelper implements IColorActivityTaskManager {
    private static final String TAG = "ColorActivityTaskManagerTransactionHelper";

    public ColorActivityTaskManagerTransactionHelper(Context context, IColorActivityTaskManagerServiceEx atms) {
        super(context, atms);
    }

    public void systemReady() {
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (!checkCodeValid(code, 1, 1)) {
            if (DEBUG) {
                Slog.i(TAG, "Invalid transaction code = " + code);
            }
            return false;
        } else if (code == 10002) {
            data.enforceInterface("android.app.IActivityTaskManager");
            setSecureController(IActivityController.Stub.asInterface(data.readStrongBinder()));
            reply.writeNoException();
            return true;
        } else if (code == 10007) {
            data.enforceInterface("android.app.IActivityTaskManager");
            ComponentName name = getTopActivityComponentName();
            reply.writeNoException();
            ComponentName.writeToParcel(name, reply);
            return true;
        } else if (code == 10011) {
            data.enforceInterface("android.app.IActivityTaskManager");
            ApplicationInfo info = getTopApplicationInfo();
            reply.writeNoException();
            info.writeToParcel(reply, 0);
            return true;
        } else if (code == 10035) {
            data.enforceInterface("android.app.IActivityTaskManager");
            boolean enable = isAppCallRefuseMode();
            reply.writeNoException();
            reply.writeString(String.valueOf(enable));
            return true;
        } else if (code != 10036) {
            switch (code) {
                case 10052:
                    data.enforceInterface("android.app.IActivityTaskManager");
                    swapDockedFullscreenStack();
                    reply.writeNoException();
                    return true;
                case 10053:
                    data.enforceInterface("android.app.IActivityTaskManager");
                    List<ColorAppInfo> list = getAllTopAppInfos();
                    reply.writeNoException();
                    reply.writeTypedList(list);
                    return true;
                case 10054:
                    data.enforceInterface("android.app.IActivityTaskManager");
                    List<String> list2 = getFreeformConfigList(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(list2);
                    return true;
                case 10055:
                    data.enforceInterface("android.app.IActivityTaskManager");
                    boolean enable2 = isFreeformEnabled();
                    reply.writeNoException();
                    reply.writeString(String.valueOf(enable2));
                    return true;
                case 10056:
                    data.enforceInterface("android.app.IActivityTaskManager");
                    boolean success = addFreeformConfigChangedListener(IColorFreeformConfigChangedListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeString(String.valueOf(success));
                    return true;
                case 10057:
                    data.enforceInterface("android.app.IActivityTaskManager");
                    boolean success2 = removeFreeformConfigChangedListener(IColorFreeformConfigChangedListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeString(String.valueOf(success2));
                    return true;
                default:
                    switch (code) {
                        case 10064:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success3 = registerAppSwitchObserver(data.readString(), IColorAppSwitchObserver.Stub.asInterface(data.readStrongBinder()), (ColorAppSwitchConfig) ColorAppSwitchConfig.CREATOR.createFromParcel(data));
                            reply.writeNoException();
                            reply.writeString(String.valueOf(success3));
                            return true;
                        case 10065:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success4 = unregisterAppSwitchObserver(data.readString(), (ColorAppSwitchConfig) ColorAppSwitchConfig.CREATOR.createFromParcel(data));
                            reply.writeNoException();
                            reply.writeString(String.valueOf(success4));
                            return true;
                        case 10066:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            setChildSpaceMode(Boolean.valueOf(data.readString()).booleanValue());
                            reply.writeNoException();
                            return true;
                        case 10067:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            setAllowLaunchApps(data.createStringArrayList());
                            reply.writeNoException();
                            return true;
                        case 10068:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            Intent intent = null;
                            if (data.readInt() != 0) {
                                intent = (Intent) Intent.CREATOR.createFromParcel(data);
                            }
                            Bundle dataBundle = null;
                            if (data.readInt() != 0) {
                                dataBundle = data.readBundle();
                            }
                            int success5 = startZoomWindow(intent, dataBundle, data.readInt(), data.readString());
                            reply.writeNoException();
                            reply.writeInt(success5);
                            return true;
                        case 10069:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success6 = registerZoomWindowObserver(IColorZoomWindowObserver.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            reply.writeString(String.valueOf(success6));
                            return true;
                        case 10070:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success7 = unregisterZoomWindowObserver(IColorZoomWindowObserver.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            reply.writeString(String.valueOf(success7));
                            return true;
                        case 10071:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            ColorZoomWindowInfo info2 = getCurrentZoomWindowState();
                            reply.writeNoException();
                            info2.writeToParcel(reply, 0);
                            return true;
                        case 10072:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            setBubbleMode(Boolean.valueOf(data.readString()).booleanValue());
                            reply.writeNoException();
                            return true;
                        case 10073:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            hideZoomWindow(data.readInt());
                            reply.writeNoException();
                            return true;
                        case 10074:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            Intent intent2 = null;
                            if (1 == data.readInt()) {
                                intent2 = (Intent) Intent.CREATOR.createFromParcel(data);
                            }
                            int result = getSplitScreenState(intent2);
                            reply.writeNoException();
                            reply.writeString(String.valueOf(result));
                            return true;
                        case 10075:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            List<String> list3 = getZoomAppConfigList(data.readInt());
                            reply.writeNoException();
                            reply.writeStringList(list3);
                            return true;
                        case 10076:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            ColorZoomWindowRUSConfig config = getZoomWindowConfig();
                            reply.writeNoException();
                            reply.writeParcelable(config, 0);
                            return true;
                        case 10077:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            reply.writeNoException();
                            setZoomWindowConfig((ColorZoomWindowRUSConfig) data.readParcelable(ColorZoomWindowRUSConfig.class.getClassLoader()));
                            return true;
                        case 10078:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean isZoomWindowEnabled = isSupportZoomWindowMode();
                            reply.writeNoException();
                            reply.writeBoolean(isZoomWindowEnabled);
                            return true;
                        case 10079:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success8 = addZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            reply.writeString(String.valueOf(success8));
                            return true;
                        case 10080:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success9 = removeZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            reply.writeString(String.valueOf(success9));
                            return true;
                        case 10081:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            startLockDeviceMode(data.readString(), data.createStringArray());
                            reply.writeNoException();
                            return true;
                        case 10082:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            stopLockDeviceMode();
                            reply.writeNoException();
                            return true;
                        case 10083:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            String callPkg = data.readString();
                            String scenePkg = data.readString();
                            List<String> paramCmdList = new ArrayList<>();
                            data.readStringList(paramCmdList);
                            boolean success10 = writeEdgeTouchPreventParam(callPkg, scenePkg, paramCmdList);
                            reply.writeNoException();
                            reply.writeBoolean(success10);
                            return true;
                        case 10084:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            String callPkg2 = data.readString();
                            List<String> paramCmdList2 = new ArrayList<>();
                            data.readStringList(paramCmdList2);
                            setDefaultEdgeTouchPreventParam(callPkg2, paramCmdList2);
                            reply.writeNoException();
                            return true;
                        case 10085:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success11 = resetDefaultEdgeTouchPreventParam(data.readString());
                            reply.writeNoException();
                            reply.writeBoolean(success11);
                            return true;
                        case 10086:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success12 = isSupportEdgeTouchPrevent();
                            reply.writeNoException();
                            reply.writeBoolean(success12);
                            return true;
                        case 10087:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            String callPkg3 = data.readString();
                            Map<String, List<String>> rulesMap = new HashMap<>();
                            data.readMap(rulesMap, Map.class.getClassLoader());
                            setEdgeTouchCallRules(callPkg3, rulesMap);
                            reply.writeNoException();
                            return true;
                        case 10088:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            int result2 = splitScreenForEdgePanel((Intent) Intent.CREATOR.createFromParcel(data), data.readInt());
                            reply.writeNoException();
                            reply.writeString(String.valueOf(result2));
                            return true;
                        case 10089:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            Map<String, Integer> packageMap = new HashMap<>();
                            data.readMap(packageMap, Map.class.getClassLoader());
                            setAppStateForIntercept(packageMap);
                            reply.writeNoException();
                            return true;
                        case 10090:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success13 = registerLockScreenCallback(IColorLockScreenCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            reply.writeString(String.valueOf(success13));
                            return true;
                        case 10091:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean success14 = unregisterLockScreenCallback(IColorLockScreenCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            reply.writeString(String.valueOf(success14));
                            return true;
                        case 10092:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            setGimbalLaunchPkg(data.readString());
                            reply.writeNoException();
                            return true;
                        case 10093:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean result3 = registerKeyEventObserver(data.readString(), IColorKeyEventObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                            reply.writeNoException();
                            reply.writeString(String.valueOf(result3));
                            return true;
                        case 10094:
                            data.enforceInterface("android.app.IActivityTaskManager");
                            boolean result4 = unregisterKeyEventObserver(data.readString());
                            reply.writeNoException();
                            reply.writeString(String.valueOf(result4));
                            return true;
                        default:
                            return false;
                    }
            }
        } else {
            data.enforceInterface("android.app.IActivityTaskManager");
            setAppCallRefuseMode(Boolean.valueOf(data.readString()).booleanValue());
            reply.writeNoException();
            return true;
        }
    }

    public void setSecureController(IActivityController controller) {
        this.mAtms.mAmInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setActivityController()");
        synchronized (this.mAtms) {
            OppoSecureProtectUtils.setSecureControllerLocked(this.mAtms, controller);
        }
    }

    public ComponentName getTopActivityComponentName() {
        try {
            ActivityRecord top = this.mAtms.mRootActivityContainer.getDefaultDisplay().topRunningActivity();
            ComponentName name = new ComponentName("", "");
            if (top != null) {
                return new ComponentName(top.info.packageName, top.info.name);
            }
            return name;
        } catch (Exception e) {
            return new ComponentName("", "");
        }
    }

    public ApplicationInfo getTopApplicationInfo() {
        try {
            ActivityRecord top = this.mAtms.mRootActivityContainer.getDefaultDisplay().topRunningActivity();
            ApplicationInfo info = new ApplicationInfo();
            if (top == null || top.appInfo == null) {
                return info;
            }
            return top.appInfo;
        } catch (Exception e) {
            return new ApplicationInfo();
        }
    }

    public List<ColorAppInfo> getAllTopAppInfos() {
        List<ColorAppInfo> allTopAppInfosLocked;
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getAllTopAppInfos");
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.d(TAG, "getAllTopAppInfos uid : " + Binder.getCallingUid() + " pid : " + Binder.getCallingPid());
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mAtms.mGlobalLock) {
                allTopAppInfosLocked = getAllTopAppInfosLocked();
            }
            return allTopAppInfosLocked;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0192, code lost:
        if (com.android.server.wm.ActivityTaskManagerDebugConfig.DEBUG_STACK != false) goto L_0x0198;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0196, code lost:
        if (com.android.server.wm.ActivityTaskManagerDebugConfig.DEBUG_TASKS == false) goto L_0x01d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0198, code lost:
        android.util.Slog.i(com.android.server.wm.ColorActivityTaskManagerTransactionHelper.TAG, "getAllTopAppInfos have done");
     */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0169  */
    private List<ColorAppInfo> getAllTopAppInfosLocked() {
        ActivityDisplay display2;
        int displayId;
        ColorAppInfo moreVisAppInfo;
        List<ColorAppInfo> list = new ArrayList<>();
        int displayId2 = 0;
        int i = 0;
        ActivityDisplay display3 = this.mAtms.mRootActivityContainer.getActivityDisplay(0);
        if (display3 != null) {
            boolean primary = false;
            boolean second = false;
            boolean fullscreen = false;
            boolean zoomscreen = false;
            int stackNdx = display3.getChildCount() - 1;
            while (true) {
                if (stackNdx < 0) {
                    break;
                }
                ActivityStack stack = display3.getChildAt(stackNdx);
                ActivityRecord top = stack.getTopActivity();
                int activityType = stack.getActivityType();
                int windowingMode = stack.getWindowingMode();
                ColorAppInfo appInfo = new ColorAppInfo();
                appInfo.displayId = i;
                if (top != null) {
                    appInfo.topActivity = top.mActivityComponent;
                    appInfo.appInfo = top.appInfo;
                    appInfo.taskId = top.getTaskRecord() != null ? top.getTaskRecord().taskId : -1;
                    appInfo.activityType = activityType;
                    appInfo.windowingMode = windowingMode;
                    appInfo.appBounds = top.getWindowConfiguration().getBounds();
                    if (top.mAppWindowToken != null) {
                        appInfo.orientation = top.mAppWindowToken.getOrientationIgnoreVisibility();
                    }
                    appInfo.userId = top.mUserId;
                    appInfo.launchedFromPackage = top.launchedFromPackage;
                    TaskRecord taskRecord = top.getTaskRecord();
                    if (taskRecord == null || taskRecord.getRootActivity() == null) {
                        displayId = displayId2;
                        display2 = display3;
                    } else {
                        displayId = displayId2;
                        if (top.mActivityComponent != null) {
                            display2 = display3;
                            if (top.mActivityComponent.equals(((ActivityRecord) taskRecord.getRootActivity()).mActivityComponent)) {
                                appInfo.isRootActivity = true;
                            }
                        } else {
                            display2 = display3;
                        }
                    }
                    if (windowingMode != 2) {
                        if (windowingMode != 5) {
                            if (windowingMode == 1) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.i(TAG, "getAllTopAppInfos fullscreen top = " + top);
                                }
                                list.add(appInfo);
                                fullscreen = true;
                            } else if (windowingMode == 3) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.i(TAG, "getAllTopAppInfos split screen primary top = " + top);
                                }
                                if (!primary) {
                                    list.add(appInfo);
                                    primary = true;
                                }
                            } else if (windowingMode == 4) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.i(TAG, "getAllTopAppInfos split screen second top = " + top);
                                }
                                if (!second) {
                                    list.add(appInfo);
                                    second = true;
                                }
                            } else if (windowingMode == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.i(TAG, "getAllTopAppInfos zoom window top = " + top);
                                }
                                if (!zoomscreen) {
                                    list.add(appInfo);
                                    zoomscreen = true;
                                }
                            }
                            moreVisAppInfo = getMoreVisibleAppInfoLocked(stack, top);
                            if (moreVisAppInfo != null) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.i(TAG, "getAllTopAppInfos more  top = " + moreVisAppInfo.topActivity);
                                }
                                list.add(moreVisAppInfo);
                            }
                            if ((second || !primary) && !fullscreen) {
                            }
                        }
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        Slog.i(TAG, "getAllTopAppInfos pinned or freeform top = " + top);
                    }
                    list.add(appInfo);
                    moreVisAppInfo = getMoreVisibleAppInfoLocked(stack, top);
                    if (moreVisAppInfo != null) {
                    }
                    if (second) {
                    }
                } else {
                    displayId = displayId2;
                    display2 = display3;
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        Slog.i(TAG, "skip for null top stack = " + stack);
                    }
                }
                stackNdx--;
                displayId2 = displayId;
                display3 = display2;
                i = 0;
            }
        }
        return list;
    }

    private ColorAppInfo getMoreVisibleAppInfoLocked(ActivityStack stack, ActivityRecord top) {
        ColorAppInfo nextAppInfo = null;
        if (stack == null || top == null) {
            Slog.w(TAG, "getMoreVisibleAppInfoLocked: invalid params");
            return null;
        }
        if (!top.fullscreen) {
            List<TaskRecord> allTasks = stack.getAllTasks();
            boolean found = false;
            for (int taskNdx = allTasks.size() - 1; taskNdx >= 0; taskNdx--) {
                TaskRecord tr = allTasks.get(taskNdx);
                int activityNdx = tr.mActivities.size() - 1;
                while (true) {
                    if (activityNdx < 0) {
                        break;
                    }
                    ActivityRecord ar = (ActivityRecord) tr.mActivities.get(activityNdx);
                    if (ar != top) {
                        if (ar.visible && !ar.finishing) {
                            nextAppInfo = new ColorAppInfo();
                            nextAppInfo.topActivity = ar.mActivityComponent;
                            nextAppInfo.appInfo = ar.appInfo;
                            nextAppInfo.taskId = tr.taskId;
                            nextAppInfo.activityType = stack.getActivityType();
                            nextAppInfo.windowingMode = stack.getWindowingMode();
                            nextAppInfo.appBounds = ar.getWindowConfiguration().getBounds();
                        }
                        if (ar.fullscreen) {
                            found = true;
                            break;
                        }
                    }
                    activityNdx--;
                }
                if (found) {
                    break;
                }
            }
        }
        return nextAppInfo;
    }

    public boolean isAppCallRefuseMode() {
        if (DEBUG) {
            Slog.v(TAG, "isAppCallRefuseMode");
        }
        return OppoFeatureCache.get(IColorAppPhoneManager.DEFAULT).isAppPhoneRefuseMode();
    }

    public void setAppCallRefuseMode(boolean enable) {
        if (DEBUG) {
            Slog.v(TAG, "setAppCallRefuseMode enable = " + enable);
        }
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setAppCallRefuseMode");
        OppoFeatureCache.get(IColorAppPhoneManager.DEFAULT).setAppPhoneRefuseMode(enable);
    }

    public List<String> getFreeformConfigList(int type) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getFreeformConfigList");
        return ColorFreeformConfig.getInstance().getConfigList(type);
    }

    public boolean isFreeformEnabled() {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "isFreeformEnabled");
        return ColorFreeformConfig.getInstance().isEnabled();
    }

    public boolean addFreeformConfigChangedListener(IColorFreeformConfigChangedListener listener) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "addFreeformOnConfigChangedListener");
        return ColorFreeformConfig.getInstance().addConfigChangedListener(listener);
    }

    public boolean removeFreeformConfigChangedListener(IColorFreeformConfigChangedListener listener) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "removeFreeformOnConfigChangedListener");
        return ColorFreeformConfig.getInstance().removeConfigChangedListener(listener);
    }

    public boolean registerAppSwitchObserver(String pkgName, IColorAppSwitchObserver observer, ColorAppSwitchConfig config) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "registerAppSwitchObserver");
        return ColorAppSwitchManagerService.getInstance().registerAppSwitchObserver(pkgName, observer, config);
    }

    public boolean unregisterAppSwitchObserver(String pkgName, ColorAppSwitchConfig config) throws RemoteException {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "unregisterAppSwitchObserver");
        return ColorAppSwitchManagerService.getInstance().unregisterAppSwitchObserver(pkgName, config);
    }

    public void swapDockedFullscreenStack() throws RemoteException {
        this.mAtms.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "swapDockedFullscreenStack()");
        synchronized (this.mAtms.mGlobalLock) {
            long ident = Binder.clearCallingIdentity();
            try {
                OppoFeatureCache.get(IColorSplitWindowManager.DEFAULT).swapDockedFullscreenStack();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public int getSplitScreenState(Intent intent) throws RemoteException {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getSplitScreenState()");
        return OppoFeatureCache.get(IColorSplitWindowManager.DEFAULT).getSplitScreenState(intent);
    }

    public void setChildSpaceMode(boolean mode) throws RemoteException {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setChildSpaceMode");
        OppoFeatureCache.get(IColorAppChildrenSpaceManager.DEFAULT).setChildSpaceMode(mode);
    }

    public void setAllowLaunchApps(List<String> allowLaunchApps) throws RemoteException {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setAllowLaunchApps");
        OppoFeatureCache.get(IColorAppChildrenSpaceManager.DEFAULT).setAllowLaunchApps(allowLaunchApps);
    }

    public int startZoomWindow(Intent intent, Bundle options, int userId, String callPkg) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "startZoomWindow");
        return OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).startZoomWindow(intent, options, userId, callPkg);
    }

    public boolean registerZoomWindowObserver(IColorZoomWindowObserver observer) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "registerZoomWindowObserver");
        return OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).registerZoomWindowObserver(observer);
    }

    public boolean unregisterZoomWindowObserver(IColorZoomWindowObserver observer) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "unregisterZoomWindowObserver");
        return OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).unregisterZoomWindowObserver(observer);
    }

    public ColorZoomWindowInfo getCurrentZoomWindowState() {
        return OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).getCurrentZoomWindowState();
    }

    public void setBubbleMode(boolean inBubbleMode) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setBubbleMode");
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).setBubbleMode(inBubbleMode);
    }

    public void hideZoomWindow(int flag) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "hideZoomWindow");
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).hideZoomWindow(flag);
    }

    public List<String> getZoomAppConfigList(int type) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getZoomAppConfigList");
        return OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).getZoomAppConfigList(type);
    }

    public ColorZoomWindowRUSConfig getZoomWindowConfig() {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getZoomWindowConfig");
        return OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).getZoomWindowConfig();
    }

    public void setZoomWindowConfig(ColorZoomWindowRUSConfig config) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setZoomWindowConfig");
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).setZoomWindowConfig(config);
    }

    public boolean isSupportZoomWindowMode() {
        return OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).isSupportZoomWindowMode();
    }

    public boolean addZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener listener) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "addZoomWindowConfigChangedListener");
        return ColorZoomWindowConfig.getInstance().addConfigChangedListener(listener);
    }

    public boolean removeZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener listener) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "removeZoomWindowConfigChangedListener");
        return ColorZoomWindowConfig.getInstance().removeConfigChangedListener(listener);
    }

    public void startLockDeviceMode(String rootPkg, String[] packages) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "startLockDeviceMode");
        OppoFeatureCache.get(IColorLockTaskController.DEFAULT).startLockDeviceMode(UserHandle.getCallingUserId(), rootPkg, packages);
    }

    public void stopLockDeviceMode() {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "stopLockDeviceMode");
        OppoFeatureCache.get(IColorLockTaskController.DEFAULT).stopLockDeviceMode();
    }

    public boolean writeEdgeTouchPreventParam(String callPkg, String scenePkg, List<String> paramCmdList) {
        this.mAtms.mAmInternal.enforceCallingPermission("com.oppo.permission.safe.SECURITY", "writeEdgeTouchPreventParam");
        return OppoFeatureCache.get(IColorEdgeTouchManager.DEFAULT).writeEdgeTouchPreventParam(callPkg, scenePkg, paramCmdList);
    }

    public void setDefaultEdgeTouchPreventParam(String callPkg, List<String> paramCmdList) {
        this.mAtms.mAmInternal.enforceCallingPermission("com.oppo.permission.safe.SECURITY", "setDefaultEdgeTouchPreventParam");
        OppoFeatureCache.get(IColorEdgeTouchManager.DEFAULT).setDefaultEdgeTouchPreventParam(callPkg, paramCmdList);
    }

    public boolean resetDefaultEdgeTouchPreventParam(String callPkg) {
        this.mAtms.mAmInternal.enforceCallingPermission("com.oppo.permission.safe.SECURITY", "resetDefaultEdgeTouchPreventParam");
        return OppoFeatureCache.get(IColorEdgeTouchManager.DEFAULT).resetDefaultEdgeTouchPreventParam(callPkg);
    }

    public boolean isSupportEdgeTouchPrevent() {
        this.mAtms.mAmInternal.enforceCallingPermission("com.oppo.permission.safe.SECURITY", "isSupportEdgeTouchPrevent");
        return OppoFeatureCache.get(IColorEdgeTouchManager.DEFAULT).isSupportEdgeTouchPrevent();
    }

    public void setEdgeTouchCallRules(String callPkg, Map<String, List<String>> rulesMap) {
        this.mAtms.mAmInternal.enforceCallingPermission("com.oppo.permission.safe.SECURITY", "setEdgeTouchCallRules");
        OppoFeatureCache.get(IColorEdgeTouchManager.DEFAULT).setCallRules(callPkg, rulesMap);
    }

    public int splitScreenForEdgePanel(Intent intent, int userId) throws RemoteException {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "splitScreenForEdgePanel()");
        return ColorSplitWindowManagerService.getInstance().splitScreenForEdgePanel(intent, userId);
    }

    private void setAppStateForIntercept(Map<String, Integer> packageMap) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setAppStateForIntercept");
        ColorInterceptLockScreenWindow.getInstance().setPackageStateToIntercept(packageMap);
    }

    private boolean registerLockScreenCallback(IColorLockScreenCallback callback) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "registerLockScreenCallback");
        return ColorInterceptLockScreenWindow.getInstance().registerLockScreenCallback(callback);
    }

    private boolean unregisterLockScreenCallback(IColorLockScreenCallback callback) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "unregisterLockScreenCallback");
        return ColorInterceptLockScreenWindow.getInstance().unregisterLockScreenCallback(callback);
    }

    public boolean registerKeyEventObserver(String observerFingerPrint, IColorKeyEventObserver observer, int listenFlag) throws RemoteException {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "registerKeyEventObserver");
        return OppoFeatureCache.get(IColorKeyEventManager.DEFAULT).registerKeyEventObserver(observerFingerPrint, observer, listenFlag);
    }

    public boolean unregisterKeyEventObserver(String observerFingerPrint) throws RemoteException {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "unregisterKeyEventObserver");
        return OppoFeatureCache.get(IColorKeyEventManager.DEFAULT).unregisterKeyEventObserver(observerFingerPrint);
    }

    public void setGimbalLaunchPkg(String pkgName) {
        this.mAtms.mAmInternal.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setGimbalLaunchPkg");
        OppoFeatureCache.get(IColorKeyLayoutManager.DEFAULT).setGimbalLaunchPkg(pkgName);
    }
}
