package com.android.server.am;

import android.app.IColorActivityManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import android.view.IColorAccidentallyTouchHelper;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.coloros.OppoListManager;
import com.android.server.wm.IColorAthenaManager;
import com.android.server.wm.IPswOppoAmsUtilsFeatrue;
import com.color.app.IColorHansListener;
import com.color.darkmode.IColorDarkModeManager;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorDisplayCompatData;
import com.color.util.ColorDisplayCompatUtils;
import com.color.util.ColorDisplayOptimizationData;
import com.color.util.ColorDisplayOptimizationUtils;
import com.color.util.ColorPackageFreezeData;
import com.color.util.ColorProcDependData;
import com.color.util.ColorReflectData;
import com.color.util.ColorReflectDataUtils;
import com.color.util.ColorResolveData;
import com.color.util.ColorSecureKeyboardData;
import com.color.util.ColorSecureKeyboardUtils;
import com.color.widget.ColorResolveInfoHelper;
import com.oppo.app.IOppoAppStartController;
import com.oppo.app.IOppoGameSpaceController;
import com.oppo.app.IOppoPermissionRecordController;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class ColorActivityManagerTransactionHelper extends ColorActivityManagerCommonHelper implements IColorActivityManager {
    private static final int ANDROID_M_CPU_UNIT_DEN = 10;
    private static final String CPU_FILE_PATH = ("/sys/devices/system/cpu" + File.separator);
    private static final String RUTILS_PROPERTIES = "oppo.service.rutils.enable";
    private static final String RUTILS_USED_COUNT = "oppo.rutils.used.count";
    private static final String TAG = "ColorActivityManagerTransactionHelper";
    private int mCoreNum = 1;
    private ProcessCpuTracker mCpuTracker = null;
    private final ColorDirectActivityHelper mDirectHelper = new ColorDirectActivityHelper(this.mContext, this.mAms);
    private final Object mLock = new Object();
    private IPswOppoAmsUtilsFeatrue mOppoAmsUtils = null;
    private final OppoUidCpuTimeReader mOppoUidCpuTimeReader = new OppoUidCpuTimeReader(this.mContext);
    OppoPermissionInterceptPolicy mPermissionInterceptPolicy = null;
    private long mTotalSampleInterval = 1;

    public ColorActivityManagerTransactionHelper(Context context, IColorActivityManagerServiceEx amsEx) {
        super(context, amsEx);
    }

    public void systemReady() {
        this.mPermissionInterceptPolicy = OppoPermissionInterceptPolicy.getInstance(this.mAms);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (!checkCodeValid(code, 1, 1)) {
            if (DEBUG) {
                Slog.i(TAG, "Invalid transaction code = " + code);
            }
            return false;
        } else if (this.mDirectHelper.onTransact(code, data, reply, flags)) {
            if (DEBUG) {
                Slog.i(TAG, "direct find favorite = " + code);
            }
            return true;
        } else {
            int size = -1;
            switch (code) {
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
                case 10007:
                case 10008:
                case 10011:
                case 10035:
                case 10036:
                case 10044:
                case 10048:
                case 10052:
                case 10053:
                case 10054:
                case 10055:
                case 10056:
                case 10057:
                case 10058:
                case 10064:
                case 10065:
                default:
                    return false;
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
                    ColorAccidentallyTouchData info = getAccidentallyTouchData();
                    reply.writeNoException();
                    info.writeToParcel(reply, 0);
                    return true;
                case 10016:
                    data.enforceInterface("android.app.IActivityManager");
                    setGameSpaceController(IOppoGameSpaceController.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 10017:
                    data.enforceInterface("android.app.IActivityManager");
                    List<String> list = getAppAssociatedProcess(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(list);
                    return true;
                case 10018:
                    data.enforceInterface("android.app.IActivityManager");
                    List<String> list2 = getAppAssociatedProcess(data.readString());
                    reply.writeNoException();
                    reply.writeStringList(list2);
                    return true;
                case 10019:
                    data.enforceInterface("android.app.IActivityManager");
                    ArrayList<String> list3 = getGlobalPkgWhiteList(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(list3);
                    return true;
                case 10020:
                    data.enforceInterface("android.app.IActivityManager");
                    ArrayList<String> list4 = getGlobalProcessWhiteList();
                    reply.writeNoException();
                    reply.writeStringList(list4);
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
                    ColorDisplayOptimizationData info2 = getDisplayOptimizationData();
                    reply.writeNoException();
                    info2.writeToParcel(reply, 0);
                    return true;
                case 10024:
                    data.enforceInterface("android.app.IActivityManager");
                    ColorSecureKeyboardData info3 = getSecureKeyboardData();
                    reply.writeNoException();
                    info3.writeToParcel(reply, 0);
                    return true;
                case 10025:
                    data.enforceInterface("android.app.IActivityManager");
                    ArrayList<String> list5 = getStageProtectListFromPkg(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(list5);
                    return true;
                case 10026:
                    data.enforceInterface("android.app.IActivityManager");
                    handleAppFromControlCenter(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 10027:
                    data.enforceInterface("android.app.IActivityManager");
                    ColorDisplayCompatData info4 = getDisplayCompatData();
                    reply.writeNoException();
                    info4.writeToParcel(reply, 0);
                    return true;
                case 10028:
                    data.enforceInterface("android.app.IActivityManager");
                    List<String> list6 = getAllowedMultiApp();
                    reply.writeNoException();
                    reply.writeStringList(list6);
                    return true;
                case 10029:
                    data.enforceInterface("android.app.IActivityManager");
                    List<String> list7 = getCreatedMultiApp();
                    reply.writeNoException();
                    reply.writeStringList(list7);
                    return true;
                case 10030:
                    data.enforceInterface("android.app.IActivityManager");
                    String alias = getAliasMultiApp(data.readString());
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
                    ColorResolveData info5 = getResolveData();
                    reply.writeNoException();
                    info5.writeToParcel(reply, 0);
                    return true;
                case 10037:
                    data.enforceInterface("android.app.IActivityManager");
                    ColorReflectData info6 = getReflectData();
                    reply.writeNoException();
                    info6.writeToParcel(reply, 0);
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
                    ColorHansManager.getInstance().unfreezeForKernel(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 10041:
                    data.enforceInterface("android.app.IActivityManager");
                    List<ColorPackageFreezeData> list8 = getRunningProcesses();
                    reply.writeNoException();
                    reply.writeTypedList(list8);
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
                case 10045:
                    data.enforceInterface("android.app.IActivityManager");
                    List<String> list9 = getAppAssociatedActivity(data.readString());
                    reply.writeNoException();
                    reply.writeStringList(list9);
                    return true;
                case 10046:
                    data.enforceInterface("android.app.IActivityManager");
                    addFastAppThirdLogin(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 10047:
                    data.enforceInterface("android.app.IActivityManager");
                    removeFastAppThirdLogin(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 10049:
                    data.enforceInterface("android.app.IActivityManager");
                    addBackgroundRestrictedInfo(data.readString(), data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 10050:
                    data.enforceInterface("android.app.IActivityManager");
                    setPreventIndulgeController(IOppoAppStartController.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 10051:
                    data.enforceInterface("android.app.IActivityManager");
                    addPreventIndulgeList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 10059:
                    data.enforceInterface("android.app.IActivityManager");
                    boolean enable = getIsSupportMultiApp();
                    reply.writeNoException();
                    reply.writeString(String.valueOf(enable));
                    return true;
                case 10060:
                    data.enforceInterface("android.app.IActivityManager");
                    float percent = updateCpuTracker(data.readLong());
                    reply.writeNoException();
                    reply.writeFloat(percent);
                    return true;
                case 10061:
                    data.enforceInterface("android.app.IActivityManager");
                    List<String> list10 = getCpuWorkingStats();
                    reply.writeNoException();
                    if (list10 != null) {
                        size = list10.size();
                    }
                    reply.writeInt(size);
                    for (int i = 0; i < size; i++) {
                        reply.writeString(list10.get(i));
                    }
                    return true;
                case 10062:
                    data.enforceInterface("android.app.IActivityManager");
                    boolean success = putConfigInfo(data.readString(), data.readBundle(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeString(String.valueOf(success));
                    return true;
                case 10063:
                    data.enforceInterface("android.app.IActivityManager");
                    Bundle dataBundle = getConfigInfo(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeBundle(dataBundle);
                    return true;
                case 10066:
                    data.enforceInterface("android.app.IActivityManager");
                    setPermissionRecordController(IOppoPermissionRecordController.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 10067:
                    data.enforceInterface("android.app.IActivityManager");
                    forceTrimAppMemory(data.readInt());
                    reply.writeNoException();
                    return true;
                case 10068:
                    data.enforceInterface("android.app.IActivityManager");
                    boolean success2 = OppoFeatureCache.getOrCreate(IColorDarkModeManager.DEFAULT, new Object[0]).isInUnOpenAppList(data.readString());
                    reply.writeNoException();
                    reply.writeString(String.valueOf(success2));
                    return true;
                case 10069:
                    data.enforceInterface("android.app.IActivityManager");
                    boolean result = dumpProcPerfData(data.readBundle());
                    reply.writeNoException();
                    reply.writeBoolean(result);
                    return true;
                case 10070:
                    data.enforceInterface("android.app.IActivityManager");
                    List<String> result2 = getProcCommonInfoList(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(result2);
                    return true;
                case 10071:
                    data.enforceInterface("android.app.IActivityManager");
                    List<ColorProcDependData> result3 = getProcDependency(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(result3);
                    return true;
                case 10072:
                    data.enforceInterface("android.app.IActivityManager");
                    List<ColorProcDependData> result4 = getProcDependency(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(result4);
                    return true;
                case 10073:
                    data.enforceInterface("android.app.IActivityManager");
                    List<String> result5 = getTaskPkgList(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(result5);
                    return true;
                case 10074:
                    data.enforceInterface("android.app.IActivityManager");
                    syncPermissionRecord();
                    reply.writeNoException();
                    return true;
                case 10075:
                    data.enforceInterface("android.app.IActivityManager");
                    updateUidCpuTracker();
                    reply.writeNoException();
                    return true;
                case 10076:
                    data.enforceInterface("android.app.IActivityManager");
                    List<String> list11 = getUidCpuWorkingStats();
                    reply.writeNoException();
                    if (list11 != null) {
                        size = list11.size();
                    }
                    reply.writeInt(size);
                    for (int i2 = 0; i2 < size; i2++) {
                        reply.writeString(list11.get(i2));
                    }
                    return true;
                case 10077:
                    data.enforceInterface("android.app.IActivityManager");
                    int result6 = OppoFeatureCache.getOrCreate(IColorDarkModeManager.DEFAULT, new Object[0]).getDarkModeData(data.readString());
                    reply.writeNoException();
                    reply.writeInt(result6);
                    return true;
                case 10078:
                    data.enforceInterface("android.app.IActivityManager");
                    boolean result7 = setAppFreeze(data.readString(), data.readBundle());
                    reply.writeNoException();
                    reply.writeBoolean(result7);
                    return true;
                case 10079:
                    data.enforceInterface("android.app.IActivityManager");
                    int[] pids = new int[data.readInt()];
                    data.readIntArray(pids);
                    List<String> cmdlines = getProcCmdline(pids);
                    reply.writeNoException();
                    reply.writeStringList(cmdlines);
                    return true;
                case 10080:
                    data.enforceInterface("android.app.IActivityManager");
                    int size2 = data.readInt();
                    int[] pids2 = null;
                    if (size2 > 0) {
                        pids2 = new int[size2];
                        data.readIntArray(pids2);
                    }
                    activeGc(pids2);
                    reply.writeNoException();
                    return true;
                case 10081:
                    data.enforceInterface("android.app.IActivityManager");
                    finishNotOrderReceiver(data.readStrongBinder(), data.readInt(), data.readInt(), data.readString(), data.readBundle(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 10082:
                    data.enforceInterface("android.app.IActivityManager");
                    reportSkippedFrames(data.readLong(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 10083:
                    data.enforceInterface("android.app.IActivityManager");
                    boolean result8 = registerHansListener(data.readString(), IColorHansListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeBoolean(result8);
                    return true;
                case 10084:
                    data.enforceInterface("android.app.IActivityManager");
                    boolean result9 = unregisterHansListener(data.readString(), IColorHansListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeBoolean(result9);
                    return true;
                case 10085:
                    data.enforceInterface("android.app.IActivityManager");
                    reportSkippedFrames(data.readLong(), data.readInt() != 0, data.readInt() != 0, data.readLong());
                    reply.writeNoException();
                    return true;
            }
        }
    }

    public void updateUidCpuTracker() {
        this.mOppoUidCpuTimeReader.update();
    }

    public List<String> getUidCpuWorkingStats() {
        new ArrayList();
        this.mOppoUidCpuTimeReader.update();
        this.mOppoUidCpuTimeReader.calTopUid();
        return this.mOppoUidCpuTimeReader.getUidCpuWorkingStats();
    }

    public void updatePermissionChoice(String packageName, String permission, int choice) throws RemoteException {
        try {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "updatePermissionChoice");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "updatePermissionChoice");
        }
        OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy = this.mPermissionInterceptPolicy;
        if (oppoPermissionInterceptPolicy != null) {
            oppoPermissionInterceptPolicy.updatePermissionChoice(packageName, permission, choice);
        }
    }

    public void grantOppoPermissionByGroup(String packageName, String permission) throws RemoteException {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "grantOppoPermissionByGroup");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "grantOppoPermissionByGroup");
        }
        OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy = this.mPermissionInterceptPolicy;
        if (oppoPermissionInterceptPolicy != null) {
            oppoPermissionInterceptPolicy.grantOppoPermissionsFromRuntime(packageName, permission);
        }
    }

    public void revokeOppoPermissionByGroup(String packageName, String permission) throws RemoteException {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS", "revokeOppoPermissionByGroup");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "revokeOppoPermissionByGroup");
        }
        OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy = this.mPermissionInterceptPolicy;
        if (oppoPermissionInterceptPolicy != null) {
            oppoPermissionInterceptPolicy.revokeOppoPermissionsFromRuntime(packageName, permission);
        }
    }

    public void setPermissionInterceptEnable(boolean enabled) throws RemoteException {
        try {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setPermissionInterceptEnable");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "setPermissionInterceptEnable");
        }
        OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy = this.mPermissionInterceptPolicy;
        if (oppoPermissionInterceptPolicy != null) {
            oppoPermissionInterceptPolicy.setPermissionInterceptEnable(enabled);
        }
    }

    public boolean isPermissionInterceptEnabled() throws RemoteException {
        OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy = this.mPermissionInterceptPolicy;
        if (oppoPermissionInterceptPolicy != null) {
            return oppoPermissionInterceptPolicy.isPermissionInterceptEnabled();
        }
        return false;
    }

    public void setSystemProperties(String properties, String value) throws RemoteException {
    }

    public void killPidForce(int pid) throws RemoteException {
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
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "handleAppForNotification");
        OppoListManager.getInstance().handleAppForNotification(pkgName, uid, otherInfo);
    }

    public ColorAccidentallyTouchData getAccidentallyTouchData() {
        return OppoFeatureCache.getOrCreate(IColorAccidentallyTouchHelper.DEFAULT, new Object[0]).getAccidentallyTouchData();
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
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setGameSpaceController");
        OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).setGameSpaceController(controller);
    }

    public List<ColorProcDependData> getProcDependency(int pid) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getProcDependency");
        return OppoFeatureCache.get(IColorAthenaAmManager.DEFAULT).getProcDependency(pid);
    }

    public List<ColorProcDependData> getProcDependency(String packageName, int userId) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getProcDependency");
        return OppoFeatureCache.get(IColorAthenaAmManager.DEFAULT).getProcDependency(packageName, userId);
    }

    public List<String> getTaskPkgList(int taskId) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getTaskPkgList");
        return OppoFeatureCache.get(IColorAthenaManager.DEFAULT).getTaskPkgList(taskId);
    }

    public void syncPermissionRecord() {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "syncPermissionRecord");
        OppoPermissionInterceptPolicy.getInstance(this.mAms).syncPermissionRecord();
    }

    public List<String> getAppAssociatedActivity(String packageName) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getAppAssociatedActivity");
        return OppoFeatureCache.get(IColorAthenaManager.DEFAULT).getAppAssociatedActivity(packageName);
    }

    public List<String> getAppAssociatedProcess(int pid) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getAppAssociatedProcess");
        List<ColorProcDependData> list = getProcDependency(pid);
        List<String> resultList = new ArrayList<>();
        if (list == null) {
            return resultList;
        }
        for (ColorProcDependData data : list) {
            if (!data.mServices.isEmpty()) {
                for (ColorProcDependData.ProcItem item : data.mServices) {
                    resultList.add(item.packageName);
                }
            }
        }
        return resultList;
    }

    public List<String> getAppAssociatedProcess(String pkgName) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getAppAssociatedProcess");
        List<ColorProcDependData> list = getProcDependency(pkgName, UserHandle.getCallingUserId());
        List<String> resultList = new ArrayList<>();
        if (list == null) {
            return resultList;
        }
        for (ColorProcDependData data : list) {
            if (!data.mServices.isEmpty()) {
                for (ColorProcDependData.ProcItem item : data.mServices) {
                    resultList.add(item.packageName);
                }
            }
        }
        return resultList;
    }

    public ArrayList<String> getGlobalPkgWhiteList(int type) {
        return OppoListManager.getInstance().getGlobalWhiteList(this.mContext, type);
    }

    public ArrayList<String> getGlobalProcessWhiteList() {
        return OppoListManager.getInstance().getGlobalProcessWhiteList(this.mContext);
    }

    public void addStageProtectInfo(String pkg, String fromPkg, long timeout) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "addStageProtectInfo");
        OppoListManager.getInstance().addStageProtectInfo(pkg, fromPkg, timeout);
    }

    public void removeStageProtectInfo(String pkg, String fromPkg) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "removeStageProtectInfo");
        OppoListManager.getInstance().removeStageProtectInfo(pkg, fromPkg);
    }

    public ArrayList<String> getStageProtectListFromPkg(String pkg, int type) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getStageProtectListFromPkg");
        return OppoListManager.getInstance().getStageProtectListFromPkg(pkg, type);
    }

    public void handleAppFromControlCenter(String pkgName, int uid) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "handleAppFromControlCenter");
        OppoListManager.getInstance().handleAppFromControlCenter(pkgName, uid);
    }

    public List<String> getAllowedMultiApp() {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getAllowedMultiApp();
    }

    public List<String> getCreatedMultiApp() {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCreatedMultiApp();
    }

    public String getAliasMultiApp(String pkgName) {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getAliasMultiApp(pkgName);
    }

    public boolean getIsSupportMultiApp() {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isSupportMultiApp();
    }

    public void addMiniProgramShare(String srcPkgName, String dstPkgName, String signature) {
        OppoListManager.getInstance().addMiniProgramShare(srcPkgName, dstPkgName, signature);
    }

    public void removeMiniProgramShare(String srcPkgName, String dstPkgName, String signature) {
        OppoListManager.getInstance().removeMiniProgramShare(srcPkgName, dstPkgName, signature);
    }

    public void launchRutils() {
        SystemProperties.set(RUTILS_PROPERTIES, "1");
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

    public boolean putConfigInfo(String configName, Bundle bundle, int flag, int userId) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "putConfigInfo");
        if (flag == 1) {
            return OppoListManager.getInstance().putConfigInfo(configName, bundle, userId);
        }
        return false;
    }

    public Bundle getConfigInfo(String configName, int flag, int userId) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getConfigInfo");
        if (flag == 1) {
            return OppoListManager.getInstance().getConfigInfo(configName, userId);
        }
        return null;
    }

    public List<ColorPackageFreezeData> getRunningProcesses() {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getRunningProcesses");
        return ColorCommonListManager.getInstance().getRunningProcesses();
    }

    public void setPreventStartController(IOppoAppStartController watcher) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setPreventStartController");
        ColorAutostartManager.getInstance().setPreventStartController(watcher);
    }

    public void setAppStartMonitorController(IOppoAppStartController watcher) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setAppStartMonitorController");
        OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).setAppStartMonitorController(watcher);
    }

    public void addFastAppThirdLogin(String callerPkg, String replacePkg) {
        OppoListManager.getInstance().addFastAppThirdLogin(callerPkg, replacePkg);
    }

    public void removeFastAppThirdLogin(String callerPkg, String replacePkg) {
        OppoListManager.getInstance().removeFastAppThirdLogin(callerPkg, replacePkg);
    }

    public void addBackgroundRestrictedInfo(String callerPkg, List<String> targetPkgList) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "addBackgroundRestrictedInfo");
        OppoListManager.getInstance().addBackgroundRestrictedInfo(callerPkg, targetPkgList);
    }

    public void setPreventIndulgeController(IOppoAppStartController controller) {
        OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).setPreventIndulgeController(controller);
    }

    public void addPreventIndulgeList(List<String> pkgNames) {
        OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).addPreventIndulgeList(pkgNames);
    }

    /* access modifiers changed from: package-private */
    public static class CpuFilter implements FileFilter {
        CpuFilter() {
        }

        public boolean accept(File pathname) {
            if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                return true;
            }
            return false;
        }
    }

    public float updateCpuTracker(long lastUpdateTime) {
        if (this.mCpuTracker == null) {
            this.mCpuTracker = new ProcessCpuTracker(false);
            this.mCpuTracker.init();
            this.mCoreNum = getAllCoreNum();
        }
        this.mCpuTracker.update();
        this.mTotalSampleInterval = (SystemClock.uptimeMillis() - lastUpdateTime) * ((long) this.mCoreNum);
        if (this.mTotalSampleInterval <= 0) {
            return 0.0f;
        }
        long totalBusyTime = (long) (this.mCpuTracker.getLastUserTime() + this.mCpuTracker.getLastSystemTime() + this.mCpuTracker.getLastIrqTime());
        if (Build.VERSION.SDK_INT >= 23) {
            totalBusyTime /= 10;
        }
        return (((float) totalBusyTime) * 1000.0f) / ((float) this.mTotalSampleInterval);
    }

    public List<String> getCpuWorkingStats() {
        if (this.mCpuTracker == null) {
            updateCpuTracker(0);
        }
        List<String> returnList = new ArrayList<>();
        if (this.mTotalSampleInterval <= 0) {
            return returnList;
        }
        int size = this.mCpuTracker.countWorkingStats();
        int maxSize = 10;
        if (size <= 10) {
            maxSize = size;
        }
        for (int i = 0; i < maxSize; i++) {
            ProcessCpuTracker.Stats stats = this.mCpuTracker.getWorkingStats(i);
            int relUtime = stats.rel_utime;
            int relStime = stats.rel_stime;
            if (Build.VERSION.SDK_INT >= 23) {
                relUtime /= 10;
                relStime /= 10;
            }
            int pid = stats.pid;
            String name = stats.name;
            float percent = (((float) (relUtime + relStime)) * 1000.0f) / ((float) this.mTotalSampleInterval);
            returnList.add(name + "=" + pid + "=" + percent);
        }
        return returnList;
    }

    private int getAllCoreNum() {
        try {
            File[] files = new File(CPU_FILE_PATH).listFiles(new CpuFilter());
            if (files == null) {
                Slog.d(TAG, "files is null.");
                return 1;
            }
            Slog.d(TAG, "CPU Count: " + files.length);
            return files.length;
        } catch (Exception e) {
            Slog.i(TAG, "CPU Count: Failed. e =" + e);
            return 1;
        }
    }

    public void forceTrimAppMemory(int level) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "forceTrimAppMemory");
        OppoFeatureCache.get(IColorAthenaAmManager.DEFAULT).forceTrimAppMemory(level);
    }

    public void setPermissionRecordController(IOppoPermissionRecordController watcher) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setPermissionRecordController");
        OppoPermissionInterceptPolicy.getInstance(this.mAms).setPermissionRecordController(watcher);
    }

    public boolean dumpProcPerfData(Bundle bundle) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "dumpProcPerfData");
        if (Binder.getCallingUid() != 1000) {
            throw new IllegalArgumentException("not system uid");
        } else if (bundle != null) {
            return OppoFeatureCache.get(IColorPerfManager.DEFAULT).dumpProcPerfData(bundle);
        } else {
            throw new IllegalArgumentException("empty params");
        }
    }

    public List<String> getProcCommonInfoList(int type) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getProcCommonInfoList");
        if (Binder.getCallingUid() == 1000) {
            return OppoFeatureCache.get(IColorPerfManager.DEFAULT).getProcCommonInfoList(type);
        }
        throw new IllegalArgumentException("not system uid");
    }

    public List<String> getProcCmdline(int[] pids) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getProcCmdline");
        int appId = UserHandle.getCallingAppId();
        if (appId == 0 || appId == 1000) {
            List<String> cmdLines = new ArrayList<>(pids.length);
            for (int pid : pids) {
                cmdLines.add(ColorPerfManager.getInstance().getCmdlineName(pid));
            }
            return cmdLines;
        }
        throw new SecurityException("Disallowed call for uid " + Binder.getCallingUid());
    }

    public void activeGc(int[] pid) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "activeGc");
        if (Binder.getCallingUid() == 1000) {
            ColorAthenaAmManager.getInstance().activeGc(pid);
            return;
        }
        throw new IllegalArgumentException("only allow system uid");
    }

    public void finishNotOrderReceiver(IBinder who, int hasCode, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort) {
        OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).finishNotOrderReceiver(who, hasCode, resultCode, resultData, resultExtras, resultAbort);
    }

    public void reportSkippedFrames(long currentTime, long skippedFrames) {
        String processName = null;
        synchronized (this.mAms.mPidsSelfLocked) {
            ProcessRecord proc = this.mAms.mPidsSelfLocked.get(Binder.getCallingPid());
            if (proc != null) {
                processName = proc.processName;
            }
        }
        if (this.mOppoAmsUtils == null) {
            this.mOppoAmsUtils = OppoFeatureCache.get(IPswOppoAmsUtilsFeatrue.DEFAULT);
        }
        IPswOppoAmsUtilsFeatrue iPswOppoAmsUtilsFeatrue = this.mOppoAmsUtils;
        if (iPswOppoAmsUtilsFeatrue != null) {
            iPswOppoAmsUtilsFeatrue.saveSkippedFramesRecordToList(processName, currentTime, skippedFrames);
        }
    }

    public void reportSkippedFrames(long currentTime, boolean isAnimation, boolean isForeground, long skippedFrames) {
        String processName = null;
        synchronized (this.mAms.mPidsSelfLocked) {
            ProcessRecord proc = this.mAms.mPidsSelfLocked.get(Binder.getCallingPid());
            if (proc != null) {
                processName = proc.processName;
            }
        }
        if (this.mOppoAmsUtils == null) {
            this.mOppoAmsUtils = OppoFeatureCache.get(IPswOppoAmsUtilsFeatrue.DEFAULT);
        }
        IPswOppoAmsUtilsFeatrue iPswOppoAmsUtilsFeatrue = this.mOppoAmsUtils;
        if (iPswOppoAmsUtilsFeatrue != null) {
            iPswOppoAmsUtilsFeatrue.saveSkippedFramesRecordToList(processName, currentTime, isAnimation, isForeground, skippedFrames);
        }
    }

    public boolean registerHansListener(String callerPkg, IColorHansListener listener) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "registerHansListener");
        return ColorHansManager.getInstance().registerHansListener(callerPkg, listener);
    }

    public boolean unregisterHansListener(String callerPkg, IColorHansListener listener) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "unregisterHansListener");
        return ColorHansManager.getInstance().registerHansListener(callerPkg, listener);
    }

    public boolean setAppFreeze(String callerPkg, Bundle data) {
        this.mAms.enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setAppFreeze");
        return ColorHansManager.getInstance().setAppFreeze(callerPkg, data);
    }
}
