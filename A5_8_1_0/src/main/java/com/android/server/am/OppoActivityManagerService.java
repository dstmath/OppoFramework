package com.android.server.am;

import android.app.IActivityController;
import android.app.IActivityController.Stub;
import android.app.IOppoActivityManager;
import android.app.IOppoActivityManager.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.coloros.OppoListManager;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorAccidentallyTouchUtils;
import com.color.util.ColorDisplayCompatData;
import com.color.util.ColorDisplayCompatUtils;
import com.color.util.ColorDisplayOptimizationData;
import com.color.util.ColorDisplayOptimizationUtils;
import com.color.util.ColorFormatterCompatibilityData;
import com.color.util.ColorFormatterCompatibilityUtils;
import com.color.util.ColorPackageFreezeData;
import com.color.util.ColorReflectData;
import com.color.util.ColorReflectDataUtils;
import com.color.util.ColorResolveData;
import com.color.util.ColorSecureKeyboardData;
import com.color.util.ColorSecureKeyboardUtils;
import com.color.widget.ColorResolveInfoHelper;
import com.oppo.app.IOppoAppFreezeController;
import com.oppo.app.IOppoAppStartController;
import com.oppo.app.IOppoGameSpaceController;
import java.util.ArrayList;
import java.util.List;

public class OppoActivityManagerService extends ActivityManagerService implements IOppoActivityManager, Service {
    private static final String RUTILS_PROPERTIES = "oppo.service.rutils.enable";
    private static final String RUTILS_USED_COUNT = "oppo.rutils.used.count";
    private static final String TAG_OPPO = "OppoActivityManagerService";
    private final Object mLock = new Object();
    OppoPermissionInterceptPolicy mPermissionInterceptPolicy = null;

    public OppoActivityManagerService(Context context) {
        super(context);
    }

    public void permissionInterceptPolicyReady() {
        this.mPermissionInterceptPolicy = OppoPermissionInterceptPolicy.getInstance(this);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        List<String> list;
        int size;
        int i;
        ArrayList<String> list2;
        switch (code) {
            case 10002:
                data.enforceInterface("android.app.IActivityManager");
                setSecureController(Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 10003:
                data.enforceInterface("android.app.IActivityManager");
                updatePermissionChoice(data.readString(), data.readString(), data.readInt());
                reply.writeNoException();
                return true;
            case 10004:
                data.enforceInterface("android.app.IActivityManager");
                setPermissionInterceptEnable(Boolean.valueOf(data.readString()).booleanValue());
                reply.writeNoException();
                return true;
            case 10005:
                data.enforceInterface("android.app.IActivityManager");
                boolean enabled = isPermissionInterceptEnabled();
                reply.writeNoException();
                reply.writeString(String.valueOf(enabled));
                return true;
            case 10006:
                data.enforceInterface("android.app.IActivityManager");
                setSystemProperties(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10007:
                data.enforceInterface("android.app.IActivityManager");
                ComponentName name = getTopActivityComponentName();
                reply.writeNoException();
                ComponentName.writeToParcel(name, reply);
                return true;
            case 10008:
                data.enforceInterface("android.app.IActivityManager");
                killPidForce(data.readInt());
                reply.writeNoException();
                return true;
            case 10009:
                data.enforceInterface("android.app.IActivityManager");
                increaseRutilsUsedCount();
                reply.writeNoException();
                return true;
            case 10010:
                data.enforceInterface("android.app.IActivityManager");
                decreaseRutilsUsedCount();
                reply.writeNoException();
                return true;
            case 10011:
                data.enforceInterface("android.app.IActivityManager");
                ApplicationInfo info = getTopApplicationInfo();
                reply.writeNoException();
                info.writeToParcel(reply, 0);
                return true;
            case 10012:
                data.enforceInterface("android.app.IActivityManager");
                grantOppoPermissionByGroup(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10013:
                data.enforceInterface("android.app.IActivityManager");
                revokeOppoPermissionByGroup(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10014:
                data.enforceInterface("android.app.IActivityManager");
                handleAppForNotification(data.readString(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            case 10015:
                data.enforceInterface("android.app.IActivityManager");
                ColorAccidentallyTouchData info2 = getAccidentallyTouchData();
                reply.writeNoException();
                info2.writeToParcel(reply, 0);
                return true;
            case 10016:
                data.enforceInterface("android.app.IActivityManager");
                setGameSpaceController(IOppoGameSpaceController.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 10017:
                data.enforceInterface("android.app.IActivityManager");
                list = getAppAssociatedProcess(data.readInt());
                reply.writeNoException();
                size = list != null ? list.size() : -1;
                reply.writeInt(size);
                for (i = 0; i < size; i++) {
                    reply.writeString((String) list.get(i));
                }
                return true;
            case 10018:
                data.enforceInterface("android.app.IActivityManager");
                list = getAppAssociatedProcess(data.readString());
                reply.writeNoException();
                size = list != null ? list.size() : -1;
                reply.writeInt(size);
                for (i = 0; i < size; i++) {
                    reply.writeString((String) list.get(i));
                }
                return true;
            case 10019:
                data.enforceInterface("android.app.IActivityManager");
                list2 = getGlobalPkgWhiteList(data.readInt());
                reply.writeNoException();
                reply.writeStringList(list2);
                return true;
            case 10020:
                data.enforceInterface("android.app.IActivityManager");
                list2 = getGlobalProcessWhiteList();
                reply.writeNoException();
                reply.writeStringList(list2);
                return true;
            case 10021:
                data.enforceInterface("android.app.IActivityManager");
                addStageProtectInfo(data.readString(), data.readString(), data.readLong());
                reply.writeNoException();
                return true;
            case 10022:
                data.enforceInterface("android.app.IActivityManager");
                removeStageProtectInfo(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10023:
                data.enforceInterface("android.app.IActivityManager");
                ColorDisplayOptimizationData info3 = getDisplayOptimizationData();
                reply.writeNoException();
                info3.writeToParcel(reply, 0);
                return true;
            case 10024:
                data.enforceInterface("android.app.IActivityManager");
                ColorSecureKeyboardData info4 = getSecureKeyboardData();
                reply.writeNoException();
                info4.writeToParcel(reply, 0);
                return true;
            case 10025:
                data.enforceInterface("android.app.IActivityManager");
                list2 = getStageProtectListFromPkg(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeStringList(list2);
                return true;
            case 10026:
                data.enforceInterface("android.app.IActivityManager");
                handleAppFromControlCenter(data.readString(), data.readInt());
                reply.writeNoException();
                return true;
            case 10027:
                data.enforceInterface("android.app.IActivityManager");
                ColorDisplayCompatData info5 = getDisplayCompatData();
                reply.writeNoException();
                info5.writeToParcel(reply, 0);
                return true;
            case 10028:
                data.enforceInterface("android.app.IActivityManager");
                list = getAllowedMultiApp();
                reply.writeNoException();
                reply.writeStringList(list);
                return true;
            case 10029:
                data.enforceInterface("android.app.IActivityManager");
                list = getCreatedMultiApp();
                reply.writeNoException();
                reply.writeStringList(list);
                return true;
            case 10030:
                data.enforceInterface("android.app.IActivityManager");
                String alias = getAliasByPackage(data.readString());
                reply.writeNoException();
                reply.writeString(alias);
                return true;
            case 10031:
                data.enforceInterface("android.app.IActivityManager");
                addMiniProgramShare(data.readString(), data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10032:
                data.enforceInterface("android.app.IActivityManager");
                removeMiniProgramShare(data.readString(), data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10033:
                data.enforceInterface("android.app.IActivityManager");
                launchRutils();
                reply.writeNoException();
                return true;
            case 10034:
                data.enforceInterface("android.app.IActivityManager");
                ColorResolveData info6 = getResolveData();
                reply.writeNoException();
                info6.writeToParcel(reply, 0);
                return true;
            case 10035:
                data.enforceInterface("android.app.IActivityManager");
                boolean enable = isAppCallRefuseMode();
                reply.writeNoException();
                reply.writeString(String.valueOf(enable));
                return true;
            case 10036:
                data.enforceInterface("android.app.IActivityManager");
                setAppCallRefuseMode(Boolean.valueOf(data.readString()).booleanValue());
                reply.writeNoException();
                return true;
            case 10037:
                data.enforceInterface("android.app.IActivityManager");
                ColorReflectData info7 = getReflectData();
                Log.i("ColorReflectWidget", "oppoAMS,DIRECT_REFLECT_TRANSACTION, info: " + info7);
                reply.writeNoException();
                info7.writeToParcel(reply, 0);
                return true;
            case 10038:
                data.enforceInterface("android.app.IActivityManager");
                addFastAppWechatPay(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10039:
                data.enforceInterface("android.app.IActivityManager");
                removeFastAppWechatPay(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10040:
                data.enforceInterface("android.app.IActivityManager");
                ColorFormatterCompatibilityData info8 = getFormatterCompatData();
                reply.writeNoException();
                info8.writeToParcel(reply, 0);
                return true;
            case 10041:
                data.enforceInterface("android.app.IActivityManager");
                List<ColorPackageFreezeData> list3 = getRunningProcesses();
                reply.writeNoException();
                reply.writeTypedList(list3);
                return true;
            case 10042:
                data.enforceInterface("android.app.IActivityManager");
                setPreventStartController(IOppoAppStartController.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 10043:
                data.enforceInterface("android.app.IActivityManager");
                setAppStartMonitorController(IOppoAppStartController.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 10044:
                data.enforceInterface("android.app.IActivityManager");
                setAppFreezeController(IOppoAppFreezeController.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 10045:
                data.enforceInterface("android.app.IActivityManager");
                list = getAppAssociatedActivity(data.readString());
                reply.writeNoException();
                size = list != null ? list.size() : -1;
                reply.writeInt(size);
                for (i = 0; i < size; i++) {
                    reply.writeString((String) list.get(i));
                }
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public void setSecureController(IActivityController controller) {
        enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setActivityController()");
        synchronized (this) {
            OppoSecureProtectUtils.setSecureControllerLocked(this, controller);
        }
    }

    public void updatePermissionChoice(String packageName, String permission, int choice) {
        try {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "updatePermissionChoice");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "updatePermissionChoice");
        }
        if (this.mPermissionInterceptPolicy != null) {
            this.mPermissionInterceptPolicy.updatePermissionChoice(packageName, permission, choice);
        }
    }

    public void grantOppoPermissionByGroup(String packageName, String permission) {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "grantOppoPermissionByGroup");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "grantOppoPermissionByGroup");
        }
        if (this.mPermissionInterceptPolicy != null) {
            this.mPermissionInterceptPolicy.grantOppoPermissionsFromRuntime(packageName, permission);
        }
    }

    public void revokeOppoPermissionByGroup(String packageName, String permission) {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS", "revokeOppoPermissionByGroup");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "revokeOppoPermissionByGroup");
        }
        if (this.mPermissionInterceptPolicy != null) {
            this.mPermissionInterceptPolicy.revokeOppoPermissionsFromRuntime(packageName, permission);
        }
    }

    public void setPermissionInterceptEnable(boolean enabled) {
        try {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setPermissionInterceptEnable");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "setPermissionInterceptEnable");
        }
        if (this.mPermissionInterceptPolicy != null) {
            this.mPermissionInterceptPolicy.setPermissionInterceptEnable(enabled);
        }
    }

    public boolean isPermissionInterceptEnabled() {
        if (this.mPermissionInterceptPolicy != null) {
            return this.mPermissionInterceptPolicy.isPermissionInterceptEnabled();
        }
        return false;
    }

    public void setSystemProperties(String properties, String value) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setSystemProperties");
        SystemProperties.set(properties, value);
    }

    public ComponentName getTopActivityComponentName() {
        try {
            ActivityRecord top = this.mStackSupervisor.topRunningActivityLocked();
            ComponentName name = new ComponentName("", "");
            if (top != null) {
                name = new ComponentName(top.info.packageName, top.info.name);
            }
            return name;
        } catch (Exception e) {
            return new ComponentName("", "");
        }
    }

    public ApplicationInfo getTopApplicationInfo() {
        try {
            ActivityRecord top = this.mStackSupervisor.topRunningActivityLocked();
            ApplicationInfo info = new ApplicationInfo();
            if (!(top == null || top.appInfo == null)) {
                info = top.appInfo;
            }
            return info;
        } catch (Exception e) {
            return new ApplicationInfo();
        }
    }

    public void killPidForce(int pid) {
        enforceCallingPermission("android.permission.FORCE_STOP_PACKAGES", "killPidForce");
        Process.sendSignal(pid, 9);
    }

    public void increaseRutilsUsedCount() {
        synchronized (this.mLock) {
            SystemProperties.set(RUTILS_USED_COUNT, Integer.toString(SystemProperties.getInt(RUTILS_USED_COUNT, 0) + 1));
        }
    }

    public void decreaseRutilsUsedCount() {
        synchronized (this.mLock) {
            int count = SystemProperties.getInt(RUTILS_USED_COUNT, 0);
            if (count > 0) {
                count--;
            }
            SystemProperties.set(RUTILS_USED_COUNT, Integer.toString(count));
        }
    }

    public void handleAppForNotification(String pkgName, int uid, int otherInfo) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "handleAppForNotification");
        OppoListManager.getInstance().handleAppForNotification(pkgName, uid, otherInfo);
        if (OppoProcessManagerHelper.isFrozingByUid(uid)) {
            OppoProcessManagerHelper.setPackageResume(uid, pkgName, OppoProcessManager.RESUME_REASON_NOTIFY_STR);
        }
    }

    public ColorAccidentallyTouchData getAccidentallyTouchData() {
        return ColorAccidentallyTouchUtils.getInstance().getTouchData();
    }

    public ColorSecureKeyboardData getSecureKeyboardData() {
        return ColorSecureKeyboardUtils.getInstance().getData();
    }

    public ColorResolveData getResolveData() {
        return ColorResolveInfoHelper.getInstance(this.mContext).getData();
    }

    public ColorDisplayOptimizationData getDisplayOptimizationData() {
        return ColorDisplayOptimizationUtils.getInstance().getOptimizationData();
    }

    public ColorDisplayCompatData getDisplayCompatData() {
        return ColorDisplayCompatUtils.getInstance().getDisplayCompatData();
    }

    public void setGameSpaceController(IOppoGameSpaceController controller) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setGameSpaceController");
        OppoGameSpaceManager.getInstance().setGameSpaceController(controller);
    }

    public List<String> getAppAssociatedProcess(String pkgName) {
        if (pkgName == null) {
            return null;
        }
        ProcessRecord proc = null;
        boolean found = false;
        synchronized (this) {
            for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                proc = (ProcessRecord) this.mLruProcesses.get(i);
                if (pkgName.equals(proc.processName)) {
                    found = true;
                    break;
                }
            }
        }
        List<String> list = null;
        if (found && proc != null) {
            list = getAppAssociatedProcess(proc);
        }
        return list;
    }

    public List<String> getAppAssociatedProcess(int pid) {
        ProcessRecord proc = null;
        boolean found = false;
        synchronized (this) {
            for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                proc = (ProcessRecord) this.mLruProcesses.get(i);
                if (proc.pid == pid) {
                    found = true;
                    break;
                }
            }
        }
        if (!found || proc == null) {
            return null;
        }
        return getAppAssociatedProcess(proc);
    }

    private List<String> getAppAssociatedProcess(ProcessRecord app) {
        List<String> associatedAppList = new ArrayList();
        if (app == null) {
            return associatedAppList;
        }
        try {
            ProcessRecord proc;
            if (app.connections.size() > 0) {
                for (ConnectionRecord cr : app.connections) {
                    proc = cr.binding.service.app;
                    if (!(proc == null || proc.processName == null)) {
                        associatedAppList.add(proc.processName);
                    }
                }
            }
            if (app.conProviders.size() > 0) {
                for (ContentProviderConnection cc : app.conProviders) {
                    proc = cc.provider.proc;
                    if (!(proc == null || proc.processName == null)) {
                        associatedAppList.add(proc.processName);
                    }
                }
            }
            if (app.adjSource instanceof ProcessRecord) {
                proc = (ProcessRecord) app.adjSource;
                if (!(proc == null || proc.processName == null)) {
                    associatedAppList.add(proc.processName);
                }
            }
        } catch (Exception e) {
            Slog.w(TAG_OPPO, "getAppAssociatedProcess    " + e);
        }
        return associatedAppList;
    }

    public ArrayList<String> getGlobalPkgWhiteList(int type) {
        return OppoListManager.getInstance().getGlobalWhiteList(this.mContext, type);
    }

    public ArrayList<String> getGlobalProcessWhiteList() {
        return OppoListManager.getInstance().getGlobalProcessWhiteList(this.mContext);
    }

    public void addStageProtectInfo(String pkg, String fromPkg, long timeout) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "addStageProtectInfo");
        OppoListManager.getInstance().addStageProtectInfo(pkg, fromPkg, timeout);
    }

    public void removeStageProtectInfo(String pkg, String fromPkg) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "removeStageProtectInfo");
        OppoListManager.getInstance().removeStageProtectInfo(pkg, fromPkg);
    }

    public ArrayList<String> getStageProtectListFromPkg(String pkg, int type) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getStageProtectListFromPkg");
        return OppoListManager.getInstance().getStageProtectListFromPkg(pkg, type);
    }

    public void handleAppFromControlCenter(String pkgName, int uid) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "handleAppFromControlCenter");
        OppoListManager.getInstance().handleAppFromControlCenter(pkgName, uid);
    }

    public List<String> getAllowedMultiApp() {
        return OppoMultiAppManagerUtil.getInstance().getAllowedMultiApp();
    }

    public List<String> getCreatedMultiApp() {
        return OppoMultiAppManagerUtil.getInstance().getCreatedMultiApp();
    }

    public String getAliasByPackage(String pkgName) {
        return OppoMultiAppManagerUtil.getInstance().getAliasByPackage(pkgName);
    }

    public void addMiniProgramShare(String srcPkgName, String dstPkgName, String signature) {
        OppoListManager.getInstance().addMiniProgramShare(srcPkgName, dstPkgName, signature);
    }

    public void removeMiniProgramShare(String srcPkgName, String dstPkgName, String signature) {
        OppoListManager.getInstance().removeMiniProgramShare(srcPkgName, dstPkgName, signature);
    }

    public void launchRutils() {
        SystemProperties.set(RUTILS_PROPERTIES, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }

    public boolean isAppCallRefuseMode() {
        return OppoAppPhoneManager.getInstance().isAppPhoneRefuseMode();
    }

    public void setAppCallRefuseMode(boolean enable) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setAppCallRefuseMode");
        OppoAppPhoneManager.getInstance().setAppPhoneRefuseMode(enable);
    }

    public void addFastAppWechatPay(String originAppCpn, String fastAppCpn) {
        OppoListManager.getInstance().addFastAppWechatPay(originAppCpn, fastAppCpn);
    }

    public void removeFastAppWechatPay(String originAppCpn, String fastAppCpn) {
        OppoListManager.getInstance().removeFastAppWechatPay(originAppCpn, fastAppCpn);
    }

    public ColorReflectData getReflectData() {
        return ColorReflectDataUtils.getInstance().getData();
    }

    public ColorFormatterCompatibilityData getFormatterCompatData() {
        return ColorFormatterCompatibilityUtils.getInstance().getOptimizationData();
    }

    public List<ColorPackageFreezeData> getRunningProcesses() {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getRunningProcesses");
        return OppoProcessManager.getInstance().getRunningProcesses();
    }

    public void setPreventStartController(IOppoAppStartController watcher) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setPreventStartController");
        OppoAutostartManager.getInstance().setPreventStartController(watcher);
    }

    public void setAppStartMonitorController(IOppoAppStartController watcher) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setAppStartMonitorController");
        OppoAppStartupManager.getInstance().setAppStartMonitorController(watcher);
    }

    public void setAppFreezeController(IOppoAppFreezeController watcher) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setAppFreezeController");
        OppoProcessManager.getInstance().setAppFreezeController(watcher);
    }

    private List<String> getChildListFromTaskRecord(TaskRecord tr) {
        tr.updateTaskDescription();
        List<String> rti = new ArrayList();
        if (!(tr.mActivities == null || (tr.mActivities.isEmpty() ^ 1) == 0)) {
            for (int i = 0; i < tr.mActivities.size(); i++) {
                ActivityRecord tmp = (ActivityRecord) tr.mActivities.get(i);
                if (!(tmp == null || tmp.finishing || rti.contains(tmp.packageName))) {
                    rti.add(tmp.packageName);
                }
            }
        }
        return rti;
    }

    public List<String> getAppAssociatedActivity(String packageName) {
        List<String> res = new ArrayList();
        int recentTasksCount = this.mRecentTasks.size();
        if (recentTasksCount <= 0) {
            return res;
        }
        TaskRecord targetTaskRecord = null;
        for (int i = 0; i < recentTasksCount; i++) {
            TaskRecord tr = (TaskRecord) this.mRecentTasks.get(i);
            if (!(tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null)) {
                String pkg = tr.getBaseIntent().getComponent().getPackageName();
                if (!TextUtils.isEmpty(pkg) && pkg.equals(packageName)) {
                    targetTaskRecord = tr;
                    break;
                }
            }
        }
        if (targetTaskRecord != null) {
            List<String> childList = getChildListFromTaskRecord(targetTaskRecord);
            if (!(childList == null || (childList.isEmpty() ^ 1) == 0)) {
                res.addAll(childList);
            }
        }
        return res;
    }
}
