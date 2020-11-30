package com.android.server.coloros;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

public class OppoListManager {
    private static final String CLASSNAME = "com.android.server.ColorListManagerImpl";
    private static final String TAG = "OppoListManager";
    private static OppoListManager sInstance = null;

    public static OppoListManager getInstance() {
        if (sInstance == null) {
            synchronized (OppoListManager.class) {
                try {
                    if (sInstance == null) {
                        sInstance = (OppoListManager) newInstance();
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "OppoListManager reflect exception getInstance: " + e.toString());
                    if (sInstance == null) {
                        sInstance = new OppoListManager();
                    }
                }
            }
        }
        return sInstance;
    }

    private static Object newInstance() throws Exception {
        return Class.forName(CLASSNAME).getConstructor(Context.class).newInstance(null);
    }

    public void init() {
    }

    public void initCtx(Context context) {
    }

    public boolean isAppStartForbidden(String pkgName) {
        return false;
    }

    public boolean isBootStartFirbid(String pkgName) {
        return false;
    }

    public List<String> getAutoBootWhiteList(int userId) {
        return new ArrayList();
    }

    public boolean isInAutoBootWhiteList(String pkgName) {
        return false;
    }

    public boolean isInAutoBootWhiteList(String pkgName, int userId) {
        return false;
    }

    public boolean isAutoStartWhiteList(String pkgName, int userId) {
        return false;
    }

    public List<String> getBrowserWhiteList() {
        return null;
    }

    public boolean isInBrowserWhiteList(String pkgName) {
        return false;
    }

    public boolean isCtaPackage(String pkgName) {
        return false;
    }

    public boolean inProtectForeList(String pkg) {
        return false;
    }

    public boolean inProtectForeNetList(String pkg) {
        return false;
    }

    public List<String> getProtectForeList() {
        return new ArrayList();
    }

    public List<String> getProtectForeNetList() {
        return new ArrayList();
    }

    public List<String> getSecurePayActivityList() {
        return new ArrayList();
    }

    public List<String> getNotificationServiceList() {
        return new ArrayList();
    }

    public ArrayList<String> getGlobalWhiteList(Context context) {
        return new ArrayList<>();
    }

    public ArrayList<String> getGlobalWhiteList(Context context, int type) {
        return new ArrayList<>();
    }

    public ArrayList<String> getGlobalCmccWhiteList(Context context) {
        return new ArrayList<>();
    }

    public ArrayList<String> getGlobalCmccCdsTestWhiteList(Context context) {
        return new ArrayList<>();
    }

    public ArrayList<String> getGlobalProcessWhiteList(Context context) {
        return new ArrayList<>();
    }

    public ArrayList<String> getOppoTestToolList(Context context) {
        return new ArrayList<>();
    }

    public boolean isOppoTestTool(String pkgName, int uid) {
        return false;
    }

    public ArrayList<String> getRemoveTaskFilterProcessList(Context context) {
        return new ArrayList<>();
    }

    public ArrayList<String> getRemoveTaskFilterPkgList(Context context) {
        return new ArrayList<>();
    }

    public ArrayList<String> getKillRestartServicePkgList(Context context) {
        return new ArrayList<>();
    }

    public ArrayList<String> getRedundentTaskClassList() {
        return new ArrayList<>();
    }

    public boolean isRedundentActivity(String activity) {
        return false;
    }

    public boolean getClearRedundentTaskSwitch() {
        return false;
    }

    public boolean getPreventRedundentStartSwitch() {
        return false;
    }

    public ArrayList<String> getBackKeyFilterList() {
        return new ArrayList<>();
    }

    public boolean getBackKeyKillSwitch() {
        return false;
    }

    public ArrayList<String> getBackClipInterceptWhiteList() {
        return new ArrayList<>();
    }

    public ArrayList<String> getJobScheduleTimeoutWhiteList() {
        return new ArrayList<>();
    }

    public List<String> getAllowManifestNetBroList() {
        return new ArrayList();
    }

    public void addStageProtectInfo(String pkg, String fromPkg, long timeout) {
    }

    public void removeStageProtectInfo(String pkg, String fromPkg) {
    }

    public void removeStageProtectInfoInternal(String pkg, String fromPkg, boolean fromBinder) {
    }

    public ArrayList<String> getStageProtectList() {
        return new ArrayList<>();
    }

    public ArrayList<String> getStageProtectListFromPkg(String pkg, int type) {
        return new ArrayList<>();
    }

    public boolean inPaySafePkgList(String pkg) {
        return false;
    }

    public boolean isInstalledAppWidget(String pkgName) {
        return false;
    }

    public boolean isInstalledAppWidget(String pkgName, int userId) {
        return false;
    }

    public boolean isFromNotifyPkg(String pkgName) {
        return false;
    }

    public void addFromNotifyPkgList(String pkgName) {
    }

    public void removeFromNotifyPkgList(String pkgName) {
    }

    public List<String> getFromNotifyPkgList() {
        return new ArrayList();
    }

    public void handleAppForNotification(String pkgName, int uid, int otherInfo) {
    }

    public boolean isFromControlCenterPkg(String pkgName) {
        return false;
    }

    public void handleAppFromControlCenter(String pkgName, int uid) {
    }

    public boolean isAppPhoneCpn(String cpn) {
        return false;
    }

    public void readBackKeyCleanupFilterFile() {
    }

    public void readAccountSyncWhiteListFile() {
    }

    public List<String> getAccountSyncWhiteList() {
        return new ArrayList();
    }

    public boolean isInAccountSyncWhiteList(String pkgName, int userId) {
        return false;
    }

    public List<String> getCustomWhiteList() {
        return new ArrayList();
    }

    public boolean isInCustomWhiteList(String pkgName) {
        return false;
    }

    public boolean isSystemApp(String packageName) {
        return false;
    }

    public boolean isOppoApp(String packageName) {
        return false;
    }

    public void addMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) {
    }

    public void removeMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) {
    }

    public String getMiniProgramSignature(String pkgName) {
        return null;
    }

    public void addFastAppWechatPay(String originAppCpn, String fastAppCpn) {
    }

    public void removeFastAppWechatPay(String originAppCpn, String fastAppCpn) {
    }

    public boolean isFastAppWechatPayCpn(String originAppCpn) {
        return false;
    }

    public ComponentName replaceFastAppWechatPayCpn(String originAppCpn) {
        return null;
    }

    public void addFastAppThirdLogin(String callerPkg, String replacePkg) {
    }

    public void removeFastAppThirdLogin(String callerPkg, String replacePkg) {
    }

    public boolean isFastAppThirdLoginPkg(String resultPkg) {
        return false;
    }

    public String replaceFastAppThirdLoginPkg(String callerPkg) {
        return null;
    }

    public void dump(String arg) {
    }

    public void registerLogModule() {
    }

    public void addBackgroundRestrictedInfo(String callerPkg, List<String> list) {
    }

    public boolean isOnBackgroundServiceWhitelist(String packageName) {
        return false;
    }

    public boolean isAllowBackgroundBroadcastAction(String action, String pkgName) {
        return false;
    }

    public boolean isSkipBroadcastFlagRestricted(int callingUid, String callerPackage, ApplicationInfo info) {
        return false;
    }

    public boolean isExpVersion() {
        return false;
    }

    public boolean isInBootSmartWhiteList(String pkgName) {
        return false;
    }

    public boolean putConfigInfo(String configName, Bundle bundle) {
        return false;
    }

    public boolean putConfigInfo(String configName, Bundle bundle, int userId) {
        return false;
    }

    public Bundle getConfigInfo(String configName) {
        return null;
    }

    public Bundle getConfigInfo(String configName, int userId) {
        return null;
    }

    public ArrayList<String> getQuickRestartProcList() {
        return null;
    }

    public ArrayList<String> getAmsEmptyKillFilterList() {
        return null;
    }

    public ArrayList<String> getAmsEmptyKillBootUpFilterList() {
        return null;
    }

    public boolean isSystemDumpHeapEnable() {
        return false;
    }

    public void putCurLiveWallpaper(String packageName, int userId) {
    }

    public boolean isLiveWallpaper(String packageName, int userId) {
        return false;
    }

    public boolean isCustomizeAmsCleanupEnable() {
        return true;
    }
}
