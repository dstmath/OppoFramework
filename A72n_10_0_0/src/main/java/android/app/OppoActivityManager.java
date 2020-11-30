package android.app;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IColorKeyEventObserver;
import android.os.RemoteException;
import android.util.Log;
import com.color.app.ColorAppInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.IColorAppSwitchObserver;
import com.color.app.IColorFreeformConfigChangedListener;
import com.color.app.IColorHansListener;
import com.color.app.IColorZoomWindowConfigChangedListener;
import com.color.favorite.IColorFavoriteQueryCallback;
import com.color.lockscreen.IColorLockScreenCallback;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorDisplayCompatData;
import com.color.util.ColorDisplayOptimizationData;
import com.color.util.ColorPackageFreezeData;
import com.color.util.ColorProcDependData;
import com.color.util.ColorReflectData;
import com.color.util.ColorResolveData;
import com.color.util.ColorSecureKeyboardData;
import com.color.zoomwindow.ColorZoomWindowInfo;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import com.color.zoomwindow.IColorZoomWindowObserver;
import com.oppo.app.IOppoAppStartController;
import com.oppo.app.IOppoGameSpaceController;
import com.oppo.app.IOppoPermissionRecordController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OppoActivityManager implements IOppoActivityManager, IColorDirectActivityManager {
    private static final String TAG = "OppoActivityManager";
    private final ColorActivityManager mColorAm = new ColorActivityManager();
    private final ColorActivityTaskManager mColorAtm = new ColorActivityTaskManager();

    public interface ITaskStackListenerWrapper {
        void onActivityPinned(String str, int i, int i2, int i3);

        void onActivityUnpinned();

        void onTaskSnapshotChanged(int i, TaskSnapshotWrapper taskSnapshotWrapper);
    }

    @Override // android.app.IColorActivityManager
    public void updatePermissionChoice(String packageName, String permission, int choice) throws RemoteException {
        this.mColorAm.updatePermissionChoice(packageName, permission, choice);
    }

    @Override // android.app.IColorActivityManager
    public void setPermissionInterceptEnable(boolean enabled) throws RemoteException {
        this.mColorAm.setPermissionInterceptEnable(enabled);
    }

    @Override // android.app.IColorActivityManager
    public boolean isPermissionInterceptEnabled() throws RemoteException {
        return this.mColorAm.isPermissionInterceptEnabled();
    }

    @Override // android.app.IColorActivityManager
    public void setSystemProperties(String properties, String value) throws RemoteException {
        this.mColorAm.setSystemProperties(properties, value);
    }

    @Override // android.app.IColorActivityManager
    public void killPidForce(int pid) throws RemoteException {
        this.mColorAm.killPidForce(pid);
    }

    @Override // android.app.IColorActivityManager
    public void increaseRutilsUsedCount() throws RemoteException {
        this.mColorAm.increaseRutilsUsedCount();
    }

    @Override // android.app.IColorActivityManager
    public void decreaseRutilsUsedCount() throws RemoteException {
        this.mColorAm.decreaseRutilsUsedCount();
    }

    @Override // android.app.IColorActivityManager
    public void grantOppoPermissionByGroup(String packageName, String permission) throws RemoteException {
        this.mColorAm.grantOppoPermissionByGroup(packageName, permission);
    }

    @Override // android.app.IColorActivityManager
    public void revokeOppoPermissionByGroup(String packageName, String permission) throws RemoteException {
        this.mColorAm.revokeOppoPermissionByGroup(packageName, permission);
    }

    @Override // android.app.IColorActivityManager
    public void handleAppForNotification(String pkgName, int uid, int otherInfo) throws RemoteException {
        this.mColorAm.handleAppForNotification(pkgName, uid, otherInfo);
    }

    @Override // android.app.IColorActivityManager
    public ColorAccidentallyTouchData getAccidentallyTouchData() throws RemoteException {
        return this.mColorAm.getAccidentallyTouchData();
    }

    @Override // android.app.IColorActivityManager
    public void setGameSpaceController(IOppoGameSpaceController watcher) throws RemoteException {
        this.mColorAm.setGameSpaceController(watcher);
    }

    @Override // android.app.IColorActivityManager
    public List<String> getAppAssociatedProcess(int pid) throws RemoteException {
        return this.mColorAm.getAppAssociatedProcess(pid);
    }

    @Override // android.app.IColorActivityManager
    public List<String> getAppAssociatedProcess(String pkgName) throws RemoteException {
        return this.mColorAm.getAppAssociatedProcess(pkgName);
    }

    @Override // android.app.IColorActivityManager
    public ArrayList<String> getGlobalPkgWhiteList(int type) throws RemoteException {
        return this.mColorAm.getGlobalPkgWhiteList(type);
    }

    @Override // android.app.IColorActivityManager
    public ArrayList<String> getGlobalProcessWhiteList() throws RemoteException {
        return this.mColorAm.getGlobalProcessWhiteList();
    }

    @Override // android.app.IColorActivityManager
    public void addStageProtectInfo(String pkg, String fromPkg, long timeout) throws RemoteException {
        this.mColorAm.addStageProtectInfo(pkg, fromPkg, timeout);
    }

    @Override // android.app.IColorActivityManager
    public void removeStageProtectInfo(String pkg, String fromPkg) throws RemoteException {
        this.mColorAm.removeStageProtectInfo(pkg, fromPkg);
    }

    @Override // android.app.IColorActivityManager
    public ColorDisplayOptimizationData getDisplayOptimizationData() throws RemoteException {
        return this.mColorAm.getDisplayOptimizationData();
    }

    @Override // android.app.IColorActivityManager
    public ColorSecureKeyboardData getSecureKeyboardData() throws RemoteException {
        return this.mColorAm.getSecureKeyboardData();
    }

    @Override // android.app.IColorActivityManager
    public ArrayList<String> getStageProtectListFromPkg(String pkg, int type) throws RemoteException {
        return this.mColorAm.getStageProtectListFromPkg(pkg, type);
    }

    @Override // android.app.IColorActivityManager
    public void handleAppFromControlCenter(String pkgName, int uid) throws RemoteException {
        this.mColorAm.handleAppFromControlCenter(pkgName, uid);
    }

    @Override // android.app.IColorActivityManager
    public ColorDisplayCompatData getDisplayCompatData() throws RemoteException {
        return this.mColorAm.getDisplayCompatData();
    }

    @Override // android.app.IColorActivityManager
    public List<String> getAllowedMultiApp() throws RemoteException {
        return this.mColorAm.getAllowedMultiApp();
    }

    @Override // android.app.IColorActivityManager
    public List<String> getCreatedMultiApp() throws RemoteException {
        return this.mColorAm.getCreatedMultiApp();
    }

    @Override // android.app.IColorActivityManager
    public String getAliasMultiApp(String pkgName) throws RemoteException {
        return this.mColorAm.getAliasMultiApp(pkgName);
    }

    @Override // android.app.IColorActivityManager
    public void addMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) throws RemoteException {
        this.mColorAm.addMiniProgramShare(shareAppPkgName, miniProgramPkgName, miniProgramSignature);
    }

    @Override // android.app.IColorActivityManager
    public void removeMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) throws RemoteException {
        this.mColorAm.removeMiniProgramShare(shareAppPkgName, miniProgramPkgName, miniProgramSignature);
    }

    @Override // android.app.IColorActivityManager
    public void launchRutils() throws RemoteException {
        this.mColorAm.launchRutils();
    }

    @Override // android.app.IColorActivityManager
    public ColorResolveData getResolveData() throws RemoteException {
        return this.mColorAm.getResolveData();
    }

    @Override // android.app.IColorActivityManager
    public ColorReflectData getReflectData() throws RemoteException {
        return this.mColorAm.getReflectData();
    }

    @Override // android.app.IColorActivityManager
    public void addFastAppWechatPay(String originAppCpn, String fastAppCpn) throws RemoteException {
        this.mColorAm.addFastAppWechatPay(originAppCpn, fastAppCpn);
    }

    @Override // android.app.IColorActivityManager
    public void removeFastAppWechatPay(String originAppCpn, String fastAppCpn) throws RemoteException {
        this.mColorAm.removeFastAppWechatPay(originAppCpn, fastAppCpn);
    }

    @Override // android.app.IColorActivityManager
    public List<ColorPackageFreezeData> getRunningProcesses() throws RemoteException {
        return this.mColorAm.getRunningProcesses();
    }

    @Override // android.app.IColorActivityManager
    public void setPreventStartController(IOppoAppStartController watcher) throws RemoteException {
        this.mColorAm.setPreventStartController(watcher);
    }

    @Override // android.app.IColorActivityManager
    public void setAppStartMonitorController(IOppoAppStartController watcher) throws RemoteException {
        this.mColorAm.setAppStartMonitorController(watcher);
    }

    @Override // android.app.IColorActivityManager
    public List<String> getAppAssociatedActivity(String pkgName) throws RemoteException {
        return this.mColorAm.getAppAssociatedActivity(pkgName);
    }

    @Override // android.app.IColorActivityManager
    public void addFastAppThirdLogin(String callerPkg, String replacePkg) throws RemoteException {
        this.mColorAm.addFastAppThirdLogin(callerPkg, replacePkg);
    }

    @Override // android.app.IColorActivityManager
    public void removeFastAppThirdLogin(String callerPkg, String replacePkg) throws RemoteException {
        this.mColorAm.removeFastAppThirdLogin(callerPkg, replacePkg);
    }

    @Override // android.app.IColorDirectActivityManager
    public void favoriteQueryRule(String packageName, IColorFavoriteQueryCallback callback) throws RemoteException {
        this.mColorAm.favoriteQueryRule(packageName, callback);
    }

    @Override // android.app.IColorActivityManager
    public void addBackgroundRestrictedInfo(String callerPkg, List<String> targetPkgList) throws RemoteException {
        this.mColorAm.addBackgroundRestrictedInfo(callerPkg, targetPkgList);
    }

    @Override // android.app.IColorActivityManager
    public void setPreventIndulgeController(IOppoAppStartController controller) throws RemoteException {
        this.mColorAm.setPreventIndulgeController(controller);
    }

    @Override // android.app.IColorActivityManager
    public void addPreventIndulgeList(List<String> pkgNames) throws RemoteException {
        this.mColorAm.addPreventIndulgeList(pkgNames);
    }

    @Override // android.app.IColorActivityTaskManager
    public void swapDockedFullscreenStack() throws RemoteException {
        this.mColorAtm.swapDockedFullscreenStack();
    }

    public boolean getIsSupportMultiApp() throws RemoteException {
        return this.mColorAm.getIsSupportMultiApp();
    }

    @Override // android.app.IColorActivityManager
    public boolean putConfigInfo(String configName, Bundle bundle, int flag, int userId) throws RemoteException {
        return this.mColorAm.putConfigInfo(configName, bundle, flag, userId);
    }

    @Override // android.app.IColorActivityManager
    public Bundle getConfigInfo(String configName, int flag, int userId) throws RemoteException {
        return this.mColorAm.getConfigInfo(configName, flag, userId);
    }

    public float updateCpuTracker(long lastUpdateTime) throws RemoteException {
        return this.mColorAm.updateCpuTracker(lastUpdateTime);
    }

    public List<String> getCpuWorkingStats() throws RemoteException {
        return this.mColorAm.getCpuWorkingStats();
    }

    public void updateUidCpuTracker() throws RemoteException {
        this.mColorAm.updateUidCpuTracker();
    }

    public List<String> getUidCpuWorkingStats() throws RemoteException {
        return this.mColorAm.getUidCpuWorkingStats();
    }

    @Override // android.app.IColorActivityManager
    public void forceTrimAppMemory(int level) throws RemoteException {
        this.mColorAm.forceTrimAppMemory(level);
    }

    @Override // android.app.IColorActivityManager
    public void setPermissionRecordController(IOppoPermissionRecordController watcher) throws RemoteException {
        this.mColorAm.setPermissionRecordController(watcher);
    }

    public boolean isInDarkModeUnOpenAppList(String packageName) throws RemoteException {
        return this.mColorAm.isInDarkModeUnOpenAppList(packageName);
    }

    public int getDarkModeData(String packageName) throws RemoteException {
        return this.mColorAm.getDarkModeData(packageName);
    }

    @Override // android.app.IColorActivityManager
    public boolean dumpProcPerfData(Bundle bundle) throws RemoteException {
        return this.mColorAm.dumpProcPerfData(bundle);
    }

    @Override // android.app.IColorActivityManager
    public List<String> getProcCommonInfoList(int type) throws RemoteException {
        return this.mColorAm.getProcCommonInfoList(type);
    }

    @Override // android.app.IColorActivityManager
    public List<ColorProcDependData> getProcDependency(int pid) throws RemoteException {
        return this.mColorAm.getProcDependency(pid);
    }

    @Override // android.app.IColorActivityManager
    public List<ColorProcDependData> getProcDependency(String packageName, int userId) throws RemoteException {
        return this.mColorAm.getProcDependency(packageName, userId);
    }

    @Override // android.app.IColorActivityManager
    public List<String> getTaskPkgList(int taskId) throws RemoteException {
        return this.mColorAm.getTaskPkgList(taskId);
    }

    @Override // android.app.IColorActivityManager
    public void syncPermissionRecord() throws RemoteException {
        this.mColorAm.syncPermissionRecord();
    }

    @Override // android.app.IColorActivityManager
    public List<String> getProcCmdline(int[] pids) throws RemoteException {
        return this.mColorAm.getProcCmdline(pids);
    }

    @Override // android.app.IColorActivityManager
    public void activeGc(int[] pid) throws RemoteException {
        this.mColorAm.activeGc(pid);
    }

    @Override // android.app.IColorActivityManager
    public void finishNotOrderReceiver(IBinder who, int hasCode, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort) throws RemoteException {
        this.mColorAm.finishNotOrderReceiver(who, hasCode, resultCode, resultData, resultExtras, resultAbort);
    }

    @Override // android.app.IColorActivityManager
    public boolean registerHansListener(String callerPkg, IColorHansListener listener) throws RemoteException {
        return this.mColorAm.registerHansListener(callerPkg, listener);
    }

    @Override // android.app.IColorActivityManager
    public boolean unregisterHansListener(String callerPkg, IColorHansListener listener) throws RemoteException {
        return this.mColorAm.unregisterHansListener(callerPkg, listener);
    }

    @Override // android.app.IColorActivityManager
    public boolean setAppFreeze(String callerPkg, Bundle data) throws RemoteException {
        return this.mColorAm.setAppFreeze(callerPkg, data);
    }

    @Override // android.app.IColorActivityTaskManager
    public void setSecureController(IActivityController controller) throws RemoteException {
        this.mColorAtm.setSecureController(controller);
    }

    @Override // android.app.IColorActivityTaskManager
    public ComponentName getTopActivityComponentName() throws RemoteException {
        return this.mColorAtm.getTopActivityComponentName();
    }

    @Override // android.app.IColorActivityTaskManager
    public ApplicationInfo getTopApplicationInfo() throws RemoteException {
        return this.mColorAtm.getTopApplicationInfo();
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean isAppCallRefuseMode() throws RemoteException {
        return this.mColorAtm.isAppCallRefuseMode();
    }

    @Override // android.app.IColorActivityTaskManager
    public void setAppCallRefuseMode(boolean enable) throws RemoteException {
        this.mColorAtm.setAppCallRefuseMode(enable);
    }

    @Override // android.app.IColorActivityTaskManager
    public List<ColorAppInfo> getAllTopAppInfos() throws RemoteException {
        return this.mColorAtm.getAllTopAppInfos();
    }

    @Override // android.app.IColorActivityTaskManager
    public List<String> getFreeformConfigList(int type) throws RemoteException {
        return this.mColorAtm.getFreeformConfigList(type);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean isFreeformEnabled() throws RemoteException {
        return this.mColorAtm.isFreeformEnabled();
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean addFreeformConfigChangedListener(IColorFreeformConfigChangedListener listener) throws RemoteException {
        return this.mColorAtm.addFreeformConfigChangedListener(listener);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean removeFreeformConfigChangedListener(IColorFreeformConfigChangedListener listener) throws RemoteException {
        return this.mColorAtm.removeFreeformConfigChangedListener(listener);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean registerAppSwitchObserver(String pkgName, IColorAppSwitchObserver observer, ColorAppSwitchConfig config) throws RemoteException {
        return this.mColorAtm.registerAppSwitchObserver(pkgName, observer, config);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean unregisterAppSwitchObserver(String pkgName, ColorAppSwitchConfig config) throws RemoteException {
        return this.mColorAtm.unregisterAppSwitchObserver(pkgName, config);
    }

    @Override // android.app.IColorActivityTaskManager
    public int getSplitScreenState(Intent intent) throws RemoteException {
        return this.mColorAtm.getSplitScreenState(intent);
    }

    public ComponentName getDockTopAppName() {
        return null;
    }

    public List<String> getAllTopPkgName() {
        return null;
    }

    public ApplicationInfo getFreeFormAppInfo() {
        return null;
    }

    public final int startActivityForFreeform(Intent intent, Bundle bOptions, int userId, String callPkg) {
        return -1;
    }

    public final void exitColorosFreeform(Bundle bOptions) {
    }

    @Override // android.app.IColorActivityTaskManager
    public void setChildSpaceMode(boolean mode) throws RemoteException {
        this.mColorAtm.setChildSpaceMode(mode);
    }

    @Override // android.app.IColorActivityTaskManager
    public void setAllowLaunchApps(List<String> allowLaunchApps) throws RemoteException {
        this.mColorAtm.setAllowLaunchApps(allowLaunchApps);
    }

    @Override // android.app.IColorActivityTaskManager
    public int startZoomWindow(Intent intent, Bundle options, int userId, String callPkg) throws RemoteException {
        return this.mColorAtm.startZoomWindow(intent, options, userId, callPkg);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean registerZoomWindowObserver(IColorZoomWindowObserver observer) throws RemoteException {
        return this.mColorAtm.registerZoomWindowObserver(observer);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean unregisterZoomWindowObserver(IColorZoomWindowObserver observer) throws RemoteException {
        return this.mColorAtm.unregisterZoomWindowObserver(observer);
    }

    @Override // android.app.IColorActivityTaskManager
    public ColorZoomWindowInfo getCurrentZoomWindowState() throws RemoteException {
        return this.mColorAtm.getCurrentZoomWindowState();
    }

    @Override // android.app.IColorActivityTaskManager
    public void setBubbleMode(boolean inBubbleMode) throws RemoteException {
        this.mColorAtm.setBubbleMode(inBubbleMode);
    }

    @Override // android.app.IColorActivityTaskManager
    public void hideZoomWindow(int flag) throws RemoteException {
        this.mColorAtm.hideZoomWindow(flag);
    }

    @Override // android.app.IColorActivityTaskManager
    public List<String> getZoomAppConfigList(int type) throws RemoteException {
        return this.mColorAtm.getZoomAppConfigList(type);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean isSupportZoomWindowMode() throws RemoteException {
        return this.mColorAtm.isSupportZoomWindowMode();
    }

    @Override // android.app.IColorActivityTaskManager
    public ColorZoomWindowRUSConfig getZoomWindowConfig() throws RemoteException {
        return this.mColorAtm.getZoomWindowConfig();
    }

    @Override // android.app.IColorActivityTaskManager
    public void setZoomWindowConfig(ColorZoomWindowRUSConfig config) throws RemoteException {
        this.mColorAtm.setZoomWindowConfig(config);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean addZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener listener) throws RemoteException {
        return this.mColorAtm.addZoomWindowConfigChangedListener(listener);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean removeZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener listener) throws RemoteException {
        return this.mColorAtm.removeZoomWindowConfigChangedListener(listener);
    }

    public static class TaskSnapshotWrapper {
        ActivityManager.TaskSnapshot mTaskSnapshot;

        public TaskSnapshotWrapper(ActivityManager.TaskSnapshot taskSnapshot) {
            this.mTaskSnapshot = taskSnapshot;
        }

        public void destroy() {
            GraphicBuffer snapshotInfo;
            try {
                if (this.mTaskSnapshot != null && (snapshotInfo = this.mTaskSnapshot.getSnapshot()) != null) {
                    snapshotInfo.destroy();
                }
            } catch (Exception e) {
                System.gc();
            }
        }

        public Bitmap getSnapshotBitmap() {
            return Bitmap.wrapHardwareBuffer(this.mTaskSnapshot.getSnapshot(), this.mTaskSnapshot.getColorSpace());
        }
    }

    public static void registerTaskStackListener(final ITaskStackListenerWrapper listener) {
        try {
            ActivityTaskManager.getService().registerTaskStackListener(new TaskStackListener() {
                /* class android.app.OppoActivityManager.AnonymousClass1 */

                @Override // android.app.TaskStackListener, android.app.ITaskStackListener
                public void onTaskSnapshotChanged(int taskId, ActivityManager.TaskSnapshot snapshot) {
                    ITaskStackListenerWrapper.this.onTaskSnapshotChanged(taskId, new TaskSnapshotWrapper(snapshot));
                }

                @Override // android.app.TaskStackListener, android.app.ITaskStackListener
                public void onActivityUnpinned() {
                    ITaskStackListenerWrapper.this.onActivityUnpinned();
                }

                @Override // android.app.TaskStackListener, android.app.ITaskStackListener
                public void onActivityPinned(String packageName, int userId, int taskId, int stackId) {
                    ITaskStackListenerWrapper.this.onActivityPinned(packageName, userId, taskId, stackId);
                }
            });
        } catch (RemoteException e) {
            Log.w(TAG, "registerTaskStackListener failed.");
        }
    }

    public static List<ActivityManager.RunningTaskInfo> getFilteredTasks(int num, int ignoreActivityType, int ignoreWindowingMode) {
        try {
            return ActivityTaskManager.getService().getFilteredTasks(num, ignoreActivityType, ignoreWindowingMode);
        } catch (RemoteException e) {
            Log.w(TAG, "getFilteredTasks failed.");
            return null;
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean writeEdgeTouchPreventParam(String callPkg, String scenePkg, List<String> paramCmdList) throws RemoteException {
        return this.mColorAtm.writeEdgeTouchPreventParam(callPkg, scenePkg, paramCmdList);
    }

    @Override // android.app.IColorActivityTaskManager
    public void setDefaultEdgeTouchPreventParam(String callPkg, List<String> paramCmdList) throws RemoteException {
        this.mColorAtm.setDefaultEdgeTouchPreventParam(callPkg, paramCmdList);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean resetDefaultEdgeTouchPreventParam(String callPkg) throws RemoteException {
        return this.mColorAtm.resetDefaultEdgeTouchPreventParam(callPkg);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean isSupportEdgeTouchPrevent() throws RemoteException {
        return this.mColorAtm.isSupportEdgeTouchPrevent();
    }

    @Override // android.app.IColorActivityTaskManager
    public void setEdgeTouchCallRules(String callPkg, Map<String, List<String>> rulesMap) throws RemoteException {
        this.mColorAtm.setEdgeTouchCallRules(callPkg, rulesMap);
    }

    @Override // android.app.IColorActivityTaskManager
    public int splitScreenForEdgePanel(Intent intent, int userId) throws RemoteException {
        return this.mColorAtm.splitScreenForEdgePanel(intent, userId);
    }

    @Override // android.app.IColorActivityManager
    public void reportSkippedFrames(long currentTime, long skippedFrames) throws RemoteException {
        this.mColorAm.reportSkippedFrames(currentTime, skippedFrames);
    }

    @Override // android.app.IColorActivityManager
    public void reportSkippedFrames(long currentTime, boolean isAnimation, boolean isForeground, long skippedFrames) throws RemoteException {
        this.mColorAm.reportSkippedFrames(currentTime, isAnimation, isForeground, skippedFrames);
    }

    public void setPackagesState(Map<String, Integer> packageMap) throws RemoteException {
        this.mColorAtm.setPackagesState(packageMap);
    }

    public boolean registerLockScreenCallback(IColorLockScreenCallback callback) throws RemoteException {
        return this.mColorAtm.registerLockScreenCallback(callback);
    }

    public boolean unregisterLockScreenCallback(IColorLockScreenCallback callback) throws RemoteException {
        return this.mColorAtm.unregisterLockScreenCallback(callback);
    }

    @Override // android.app.IColorActivityTaskManager
    public void setGimbalLaunchPkg(String pkgName) throws RemoteException {
        this.mColorAtm.setGimbalLaunchPkg(pkgName);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean registerKeyEventObserver(String observerFingerPrint, IColorKeyEventObserver observer, int listenFlag) throws RemoteException {
        return this.mColorAtm.registerKeyEventObserver(observerFingerPrint, observer, listenFlag);
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean unregisterKeyEventObserver(String observerFingerPrint) throws RemoteException {
        return this.mColorAtm.unregisterKeyEventObserver(observerFingerPrint);
    }
}
