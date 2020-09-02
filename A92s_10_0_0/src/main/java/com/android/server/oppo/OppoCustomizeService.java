package com.android.server.oppo;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.IOppoArmyManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.OppoCutomizeManagerInternal;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.INetworkManagementService;
import android.os.IOppoCustomizeService;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.job.controllers.JobStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OppoCustomizeService extends IOppoCustomizeService.Stub {
    private static final int ADD_APP_LIST = 1;
    private static final String COLUMN_ALLOWED = "allowed";
    private static final String COLUMN_PKG_NAME = "pkg_name";
    private static final int DELETE_APP_LIST = 2;
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final String EXTERNEL_SDCARD_PATH = "/storage/sdcard1";
    private static final int INSTALLATION_BLACK_LIST = 0;
    private static final int INSTALLATION_WHITE_LIST = 1;
    private static final String PROTECT_APP_XML_FILE = "custom_protect_app.xml";
    private static final String PROTECT_APP_XML_PATH = "/data/system";
    private static final String SAFECENTER_AUTHORITY = "com.color.provider.SafeProvider";
    private static final Uri SAFECENTER_AUTHORITY_URI = Uri.parse("content://com.color.provider.SafeProvider");
    private static final char SEPARATOR = ':';
    private static final String STATUSBAR_EXPAND_DISABLE_KEY = "statusbar_expand_disable";
    private static final String TABLE_PP_FLOAT_WINDOW = "pp_float_window";
    private static final String TAG = "OppoCustomizeService";
    private static final Uri URI_FLOAT_WINDOW = Uri.withAppendedPath(SAFECENTER_AUTHORITY_URI, TABLE_PP_FLOAT_WINDOW);
    private static final TextUtils.SimpleStringSplitter sStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
    /* access modifiers changed from: private */
    public List<String> clearList = new ArrayList();
    private ActivityManager mAm;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private CustomizePkgWhiteListConfigMgr mCustWhiteListMgr = null;
    /* access modifiers changed from: private */
    public OppoCustomizePackageManager mCustomPkgMgr = null;
    private DevicePolicyManager mDpm;
    private final INetworkManagementService mNms;
    private PackageManager mPm;
    private ArrayList<String> mProtectApplist = new ArrayList<>();

    private class PackageDataObserver extends IPackageDataObserver.Stub {
        private PackageDataObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            if (!OppoCustomizeService.this.clearList.contains(packageName)) {
                OppoCustomizeService.this.clearList.add(packageName);
            }
            Slog.d(OppoCustomizeService.TAG, "clear user data packageName: " + packageName + "; succeeded: " + succeeded);
        }
    }

    public OppoCustomizeService(Context context) {
        this.mContext = context;
        this.mAm = (ActivityManager) this.mContext.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY);
        this.mDpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mPm = this.mContext.getPackageManager();
        initProtectApps();
        initOppoCustomPkgMgr(this.mContext);
        registerOppoCutomizeManagerInternalImpl();
        this.mNms = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
    }

    public void systemReady() {
        startMdmCoreService();
    }

    public void deviceReboot() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Slog.d(TAG, "reboot device!");
            long identity = Binder.clearCallingIdentity();
            try {
                ((PowerManager) this.mContext.getSystemService("power")).reboot(null);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void deviceShutDown() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Slog.d(TAG, "shutdown device!");
            long identify = Binder.clearCallingIdentity();
            try {
                ((PowerManager) this.mContext.getSystemService("power")).shutdown(false, null, false);
            } finally {
                Binder.restoreCallingIdentity(identify);
            }
        }
    }

    public List<String> getAppRuntimeExceptionInfo() {
        List<String> list = new ArrayList<>();
        for (UsageStats value : ((UsageStatsManager) this.mContext.getSystemService("usagestats")).queryAndAggregateUsageStats(0, System.currentTimeMillis()).values()) {
            long errorCount = getAppErrorCountRef(value);
            Slog.i(TAG, "stats:" + value.getPackageName() + "   TotalTimeInForeground = " + value.getTotalTimeInForeground() + " ErrorCount = " + errorCount + StringUtils.LF);
            if (value.getTotalTimeInForeground() > 0 && errorCount > 0) {
                list.add(value.getPackageName() + ":" + errorCount);
            }
        }
        return list;
    }

    private static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        if (context == null) {
            return Collections.emptySet();
        }
        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
        if (enabledServicesSetting == null) {
            return Collections.emptySet();
        }
        Set<ComponentName> enabledServices = new HashSet<>();
        sStringColonSplitter.setString(enabledServicesSetting);
        while (sStringColonSplitter.hasNext()) {
            ComponentName enabledService = ComponentName.unflattenFromString(sStringColonSplitter.next());
            if (enabledService != null) {
                enabledServices.add(enabledService);
            }
        }
        return enabledServices;
    }

    public void setAccessibilityEnabled(ComponentName cn, boolean enabled) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            try {
                Set<ComponentName> enabledServices = getEnabledServicesFromSettings(this.mContext);
                if (enabledServices == Collections.emptySet()) {
                    enabledServices = new HashSet();
                }
                boolean accessibilityEnabled = false;
                if (enabled) {
                    enabledServices.add(cn);
                    accessibilityEnabled = true;
                } else {
                    enabledServices.remove(cn);
                    if (!enabledServices.isEmpty()) {
                        accessibilityEnabled = true;
                    }
                }
                StringBuilder enabledServicesBuilder = new StringBuilder();
                for (ComponentName enabledService : enabledServices) {
                    enabledServicesBuilder.append(enabledService.flattenToString());
                    enabledServicesBuilder.append(':');
                }
                int enabledServicesBuilderLength = enabledServicesBuilder.length();
                if (enabledServicesBuilderLength > 0) {
                    enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
                }
                Settings.Secure.putString(this.mContext.getContentResolver(), "enabled_accessibility_services", enabledServicesBuilder.toString());
                Settings.Secure.putInt(this.mContext.getContentResolver(), "accessibility_enabled", accessibilityEnabled ? 1 : 0);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void setSDCardFormatted() {
        String volumeState;
        String uuid;
        VolumeInfo volumeInfo;
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Slog.d(TAG, "format sdcard!");
            long identity = Binder.clearCallingIdentity();
            try {
                StorageManager storageManager = (StorageManager) this.mContext.getSystemService("storage");
                try {
                    volumeState = storageManager.getVolumeState(EXTERNEL_SDCARD_PATH);
                } catch (IllegalArgumentException e) {
                    volumeState = null;
                }
                if (volumeState == null) {
                    Slog.d(TAG, "no sdcard!!!");
                    return;
                }
                StorageVolume[] volumes = storageManager.getVolumeList();
                int i = 0;
                while (true) {
                    if (i >= volumes.length) {
                        break;
                    } else if (volumes[i] == null || (uuid = volumes[i].getUuid()) == null || (volumeInfo = storageManager.findVolumeByUuid(uuid)) == null || !volumeInfo.disk.isSd()) {
                        i++;
                    } else {
                        try {
                            storageManager.partitionPublic(volumeInfo.getDiskId());
                            break;
                        } catch (IllegalStateException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void killAppProcess(String packageName) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            try {
                for (ActivityManager.RunningAppProcessInfo info : this.mAm.getRunningAppProcesses()) {
                    String[] pkgList = info.pkgList;
                    int i = 0;
                    while (true) {
                        if (i < pkgList.length) {
                            if (pkgList[i].equalsIgnoreCase(packageName)) {
                                try {
                                    ActivityManagerNative.getDefault().killApplicationProcess(info.processName, info.uid);
                                } catch (RemoteException e) {
                                    Slog.d(TAG, "kill app process failed!!!");
                                }
                                return;
                            }
                            i++;
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public boolean isDeviceRoot() {
        Slog.d(TAG, "is Device Root");
        long identity = Binder.clearCallingIdentity();
        try {
            boolean ret = true;
            if (!"disabled".equals(SystemProperties.get("ro.boot.veritymode", "")) || SystemProperties.getBoolean("ro.boot.flash.locked", true)) {
                ret = false;
            }
            return ret;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void clearUserData(String packageName) {
        this.mAm.clearApplicationUserData(packageName, new PackageDataObserver());
    }

    public void clearAllUserData() {
        ArrayList<String> userAppInfoList = getUserAppPackageNameList(this.mContext);
        if (userAppInfoList != null) {
            for (int i = 0; i < userAppInfoList.size(); i++) {
                clearUserData(userAppInfoList.get(i));
            }
        }
    }

    public static ArrayList<String> getUserAppPackageNameList(Context context) {
        ArrayList<String> userAppInfoList = null;
        if (context != null) {
            try {
                List<ApplicationInfo> allAppInfoList = context.getPackageManager().getInstalledApplications(0);
                userAppInfoList = new ArrayList<>();
                for (ApplicationInfo appInfo : allAppInfoList) {
                    if ((appInfo.flags & 1) != 1 && !appInfo.packageName.equalsIgnoreCase(context.getPackageName())) {
                        userAppInfoList.add(appInfo.packageName);
                    }
                }
            } catch (Exception e) {
                Slog.d(TAG, "getInstalledApplications fail!");
                return null;
            }
        }
        return userAppInfoList;
    }

    public void clearAppData(String packageName) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (packageName.equalsIgnoreCase("*")) {
                    clearAllUserData();
                } else {
                    clearUserData(packageName);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public List<String> getClearAppName() {
        return this.clearList;
    }

    private String ListToString(List<String> list) {
        StringBuffer sb = new StringBuffer();
        for (String name : list) {
            sb.append(name);
            sb.append("|");
        }
        return sb.toString();
    }

    public void setProp(String prop, String value) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom") && prop != null && prop.length() > 0 && value != null) {
            Slog.d(TAG, "set prop:" + prop + ",value:" + value);
            long identity = Binder.clearCallingIdentity();
            try {
                SystemProperties.set(prop, value);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void setDB(String key, int value) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            if (key != null) {
                Slog.d(TAG, "set database,key:" + key + ",value:" + value);
            }
            long identity = Binder.clearCallingIdentity();
            char c = 65535;
            try {
                switch (key.hashCode()) {
                    case -1861103900:
                        if (key.equals("OTG_ENABLED")) {
                            c = 6;
                            break;
                        }
                        break;
                    case -1719770601:
                        if (key.equals("oppo_settings_manager_facelock")) {
                            c = 1;
                            break;
                        }
                        break;
                    case -1629069963:
                        if (key.equals("oppo_settings_manager_fingerprint")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -1256362148:
                        if (key.equals("oppo_settings_manager_time")) {
                            c = 2;
                            break;
                        }
                        break;
                    case -970351711:
                        if (key.equals("adb_enabled")) {
                            c = 4;
                            break;
                        }
                        break;
                    case -50521511:
                        if (key.equals("ZQ_ADB_ENABLED")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 937026755:
                        if (key.equals("MTP_TRANSFER_ENABLED")) {
                            c = 5;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        if (value == 0 || value == 1) {
                            Settings.Secure.putInt(this.mContext.getContentResolver(), key, value);
                            break;
                        }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public boolean setDeviceOwner(ComponentName cn) {
        boolean result = false;
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            result = this.mDpm.setDeviceOwner(cn);
        } catch (Exception ex) {
            Slog.d(TAG, "set Device Owner failed!", ex);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return result;
    }

    public void setEmmAdmin(ComponentName cn, boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            boolean isInstalled = true;
            String packageName = cn.getPackageName();
            Slog.d(TAG, "packagename:" + packageName);
            try {
                this.mContext.getPackageManager().getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                Slog.d(TAG, "set Device Admin failed, not install!");
                isInstalled = false;
            }
            if (isInstalled) {
                long identity = Binder.clearCallingIdentity();
                if (enable) {
                    try {
                        if (!this.mDpm.isAdminActive(cn)) {
                            Slog.d(TAG, "set special Device Admin active");
                            this.mDpm.setActiveAdmin(cn, false);
                            try {
                                ApplicationInfo ai = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.CURRENT);
                                if (ai != null) {
                                    this.mAppOpsManager.setMode(43, ai.uid, packageName, 0);
                                } else {
                                    return;
                                }
                            } catch (PackageManager.NameNotFoundException e2) {
                            }
                        } else {
                            Slog.d(TAG, "current package is active!");
                        }
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                } else if (this.mDpm.isAdminActive(cn)) {
                    Slog.d(TAG, "remove Device Admin");
                    this.mDpm.removeActiveAdmin(cn);
                }
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public Bitmap captureFullScreen() {
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.capturescreen")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getRealMetrics(dm);
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            Slog.d(TAG, "width :" + width + "height :" + height);
            return SurfaceControl.screenshot(new Rect(0, 0, width, height), width, height, display.getRotation());
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setDataEnabled(boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            try {
                TelephonyManager.getDefault().setDataEnabled(enable);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void openCloseGps(boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            if (enable) {
                try {
                    Slog.d(TAG, "open gps");
                    Settings.Secure.setLocationProviderEnabled(this.mContext.getContentResolver(), "gps", true);
                    Settings.Secure.setLocationProviderEnabled(this.mContext.getContentResolver(), "network", true);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } else {
                Slog.d(TAG, "close gps");
                Settings.Secure.setLocationProviderEnabled(this.mContext.getContentResolver(), "gps", false);
                Settings.Secure.setLocationProviderEnabled(this.mContext.getContentResolver(), "network", false);
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void openCloseNFC(boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            try {
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
                if (nfcAdapter != null && !enable) {
                    Slog.d(TAG, "open nfc");
                } else if (nfcAdapter == null || !enable) {
                    Slog.e(TAG, "error when operate nfc!!!");
                } else {
                    Slog.d(TAG, "close nfc");
                    nfcAdapter.enable();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void setSettingsRestriction(String key, boolean value) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            if (TextUtils.isEmpty(key)) {
                Slog.e(TAG, "setSettingsRestriction,type: " + key);
                return;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                ((UserManager) this.mContext.getSystemService("user")).setUserRestriction(key, value);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void updateConfiguration(Configuration config) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.updateconfig") && config != null) {
            long identity = Binder.clearCallingIdentity();
            try {
                ActivityManagerNative.getDefault().updateConfiguration(config);
            } catch (Exception ex) {
                Slog.d(TAG, "update Configuration failed!", ex);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    public void resetFactory() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.factoryreset")) {
            long identity = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.wipemedia", "2");
                SystemClock.sleep(200);
                Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
                intent.putExtra("android.intent.extra.FORCE_MASTER_CLEAR", true);
                this.mContext.sendBroadcast(intent);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void allowGetUsageStats(String packageName) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            try {
                ApplicationInfo ai = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.CURRENT);
                if (ai == null) {
                    Binder.restoreCallingIdentity(identity);
                    return;
                }
                this.mAppOpsManager.setMode(43, ai.uid, packageName, 0);
                Binder.restoreCallingIdentity(identity);
            } catch (PackageManager.NameNotFoundException e) {
                Slog.d(TAG, "allow GetUsageStats failed!", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r5v1 int: [D('e' java.lang.Exception), D('val' int)] */
    public void allowDrawOverlays(String packageName) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            AppOpsManager appOps = (AppOpsManager) this.mContext.getSystemService("appops");
            if (appOps == null) {
                Log.w(TAG, "fail to get AppOpsService.");
                return;
            }
            ApplicationInfo appInfo = null;
            try {
                PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(packageName, 4096);
                if (packageInfo != null) {
                    appInfo = packageInfo.applicationInfo;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (appInfo != null) {
                appOps.setMode(24, appInfo.uid, appInfo.packageName, 0);
            } else {
                Log.w(TAG, "fail to setMode OP_SYSTEM_ALERT_WINDOW.");
            }
            String[] selectionArgs = {packageName};
            String[] projection = {COLUMN_ALLOWED};
            ContentValues value = new ContentValues();
            Binder.clearCallingIdentity();
            Cursor cursor = null;
            int val = 0;
            try {
                Cursor cursor2 = this.mContext.getContentResolver().query(URI_FLOAT_WINDOW, projection, "pkg_name=?", selectionArgs, null);
                if (cursor2 != null && cursor2.getCount() > 0) {
                    int columnIndex = cursor2.getColumnIndex(COLUMN_ALLOWED);
                    cursor2.moveToNext();
                    val = cursor2.getInt(columnIndex);
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
            } catch (Exception e2) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            value.put(COLUMN_ALLOWED, Integer.valueOf(val | 4));
            this.mContext.getContentResolver().update(URI_FLOAT_WINDOW, value, "pkg_name=?", selectionArgs);
        }
    }

    public void setAirplaneMode(boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.set_airplane_mode")) {
            long identity = Binder.clearCallingIdentity();
            try {
                ((ConnectivityManager) this.mContext.getSystemService("connectivity")).setAirplaneMode(enable);
            } catch (Exception ex) {
                ex.printStackTrace();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void addProtectApplication(String packageName) {
        addProtectApplicationInternal(packageName, true);
    }

    public void addProtectApplicationInternal(String packageName, boolean saveToFile) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.addprotectapp")) {
            boolean result = true;
            long identity = Binder.clearCallingIdentity();
            try {
                OppoListManager.getInstance().addStageProtectInfo(packageName, TAG, JobStatus.NO_LATEST_RUNTIME);
            } catch (Exception ex) {
                Slog.d(TAG, "add protect application failed!", ex);
                result = false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
            if (result) {
                synchronized (this.mProtectApplist) {
                    if (!this.mProtectApplist.contains(packageName)) {
                        this.mProtectApplist.add(packageName);
                        if (saveToFile) {
                            saveListToFile(PROTECT_APP_XML_PATH, PROTECT_APP_XML_FILE, this.mProtectApplist);
                        }
                    }
                }
            }
        }
    }

    public void removeProtectApplication(String packageName) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.addprotectapp")) {
            boolean result = true;
            long identity = Binder.clearCallingIdentity();
            try {
                OppoListManager.getInstance().removeStageProtectInfo(packageName, TAG);
            } catch (Exception ex) {
                Slog.d(TAG, "remove protect application failed!", ex);
                result = false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
            if (result) {
                synchronized (this.mProtectApplist) {
                    this.mProtectApplist.remove(packageName);
                    saveListToFile(PROTECT_APP_XML_PATH, PROTECT_APP_XML_FILE, this.mProtectApplist);
                }
            }
        }
    }

    public List<String> getProtectApplicationList() {
        ArrayList<String> tempList = new ArrayList<>();
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.addprotectapp")) {
            return tempList;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            tempList = OppoListManager.getInstance().getStageProtectListFromPkg(TAG, 0);
        } catch (Exception ex) {
            Slog.d(TAG, "get protect application failed!", ex);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return tempList;
    }

    private void initProtectApps() {
        File file = new File(PROTECT_APP_XML_PATH, PROTECT_APP_XML_FILE);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return;
                }
            } catch (IOException e) {
                Slog.i(TAG, "failed create file " + e);
            }
        }
        ArrayList<String> list = (ArrayList) readXMLFile(PROTECT_APP_XML_PATH, PROTECT_APP_XML_FILE);
        if (list != null && !list.isEmpty()) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                addProtectApplicationInternal(it.next(), false);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r5v1 java.io.FileOutputStream: [D('e' java.io.IOException), D('fileos' java.io.FileOutputStream)] */
    public void saveListToFile(String path, String name, List<String> list) {
        StringBuilder sb;
        if (path != null && name != null && list != null) {
            File file = new File(path, name);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        return;
                    }
                } catch (IOException e) {
                    Slog.i(TAG, "failed create file " + e);
                }
            }
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(fileos2, "UTF-8");
                serializer.startDocument(null, true);
                serializer.startTag(null, "gs");
                for (int i = 0; i < list.size(); i++) {
                    String pkg = list.get(i);
                    if (pkg != null) {
                        serializer.startTag(null, "p");
                        serializer.attribute(null, "att", pkg);
                        serializer.endTag(null, "p");
                    }
                }
                serializer.endTag(null, "gs");
                serializer.endDocument();
                serializer.flush();
                try {
                    fileos2.close();
                    return;
                } catch (IOException e2) {
                    e = e2;
                    sb = new StringBuilder();
                }
            } catch (IllegalArgumentException e3) {
                Slog.i(TAG, "failed write file " + e3);
                if (fileos != null) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e4) {
                        e = e4;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IllegalStateException e5) {
                Slog.i(TAG, "failed write file " + e5);
                if (fileos != null) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e6) {
                        e = e6;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e7) {
                Slog.i(TAG, "failed write file " + e7);
                if (fileos != null) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e8) {
                        e = e8;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Exception e9) {
                Slog.i(TAG, "failed write file " + e9);
                if (fileos != null) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e10) {
                        e = e10;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e11) {
                        Slog.i(TAG, "failed close stream " + e11);
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("failed close stream ");
        sb.append(e);
        Slog.i(TAG, sb.toString());
    }

    public List<String> readXMLFile(String path, String name) {
        StringBuilder sb;
        int type;
        String pkg;
        List<String> list = new ArrayList<>();
        File file = new File(path, name);
        if (!file.exists()) {
            return list;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && "p".equals(parser.getName()) && (pkg = parser.getAttributeValue(null, "att")) != null) {
                    list.add(pkg);
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (NullPointerException e2) {
            Slog.i(TAG, "failed parsing " + e2);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e4) {
            Slog.i(TAG, "failed parsing " + e4);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            Slog.i(TAG, "failed parsing " + e6);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e8) {
            Slog.i(TAG, "failed IOException " + e8);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e9) {
                    e = e9;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e10) {
            Slog.i(TAG, "failed parsing " + e10);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    e = e11;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e12) {
                    Slog.i(TAG, "Failed to close state FileInputStream " + e12);
                }
            }
            throw th;
        }
        return list;
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Slog.i(TAG, sb.toString());
        return list;
    }

    public void setDevelopmentEnabled(boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            if (enable) {
                try {
                    Settings.Global.putInt(this.mContext.getContentResolver(), "development_settings_enabled", 1);
                } catch (Exception ex) {
                    Slog.d(TAG, "setDevelopmentEnabled failed", ex);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } else if (Settings.Global.getInt(this.mContext.getContentResolver(), "development_settings_enabled", 0) != 0) {
                Settings.Global.putInt(this.mContext.getContentResolver(), "development_settings_enabled", 0);
                Intent intent = new Intent();
                intent.setAction("com.oppo.action_dissable_development");
                intent.setPackage("com.android.settings");
                this.mContext.startService(intent);
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void addDisallowedUninstallPackages(List<String> packageNames) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.addDisallowUninstallApps(packageNames);
        } catch (Exception e) {
            Slog.d(TAG, "addDisallowunistallApps", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    public void removeDisallowedUninstallPackages(List<String> packageNames) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.removeDisallowUninstallApps(packageNames);
        } catch (Exception e) {
            Slog.d(TAG, "removeDisallowedUninstallPackages", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    public void removeAllDisallowedUninstallPackages() {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.removeDisallowUninstallApps(new ArrayList<>());
        } catch (Exception e) {
            Slog.d(TAG, "removeDisallowedUninstallPackages", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    /* JADX INFO: finally extract failed */
    public List<String> getDisallowUninstallPackageList() {
        long identity = Binder.clearCallingIdentity();
        try {
            List<String> disallowUninstallApps = this.mCustomPkgMgr.getDisallowUninstallApps();
            Binder.restoreCallingIdentity(identity);
            return disallowUninstallApps;
        } catch (Exception e) {
            Slog.d(TAG, "removeDisallowedUninstallPackages", e);
            Binder.restoreCallingIdentity(identity);
            return Collections.emptyList();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public void disableInstallSource() {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.enableInstallSource(false);
        } catch (Exception e) {
            Slog.d(TAG, "removeDisallowedUninstallPackages", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    public void enableInstallSource() {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.enableInstallSource(true);
        } catch (Exception e) {
            Slog.d(TAG, "removeDisallowedUninstallPackages", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    private IOppoArmyManager getOppoArmyManager() {
        return IOppoArmyManager.Stub.asInterface(ServiceManager.getService("oppo_army"));
    }

    public void addDisallowedRunningApp(List<String> packageNames) {
        long identify;
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            identify = Binder.clearCallingIdentity();
            if (packageNames == null || packageNames.size() <= 0) {
                Slog.d(TAG, "addDisallowedRunningApp:appPkgNamesList is empty!");
                return;
            }
            IOppoArmyManager service = getOppoArmyManager();
            if (service == null) {
                Slog.d(TAG, "addDisallowedRunningApp:IOppoArmyService is empty!");
                return;
            }
            try {
                service.addDisallowedRunningApp(packageNames);
                Binder.restoreCallingIdentity(identify);
                return;
            } catch (RemoteException e) {
                Slog.d(TAG, "addDisallowedRunningApp failed.");
            } catch (Exception e2) {
                Slog.e(TAG, "addDisallowedRunningApp failed", e2);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
        } else {
            return;
        }
        Binder.restoreCallingIdentity(identify);
    }

    private void startMdmCoreService() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.oppo.enterprise.mdmcoreservice", "com.oppo.enterprise.mdmcoreservice.service.InitService"));
            Slog.d(TAG, "Starting service: " + intent);
            this.mContext.startServiceAsUser(intent, UserHandle.SYSTEM);
        }
    }

    public void setAppInstallationPolicies(int mode, List<String> appPackageNames) {
        int addmode = appPackageNames == null ? 2 : 1;
        if (mode == 0) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.addInstallPackageBlacklist(addmode, appPackageNames);
            } catch (Exception e) {
                Slog.d(TAG, "addInstallPackageBlacklist", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        } else if (mode == 1) {
            long identity2 = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.addInstallPackageWhitelist(addmode, appPackageNames);
            } catch (Exception e2) {
                Slog.d(TAG, "addInstallPackageWhitelist", e2);
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(identity2);
                throw th2;
            }
            Binder.restoreCallingIdentity(identity2);
        }
    }

    public List<String> getAppInstallationPolicies(int mode) {
        List<String> list = new ArrayList<>();
        long identity = Binder.clearCallingIdentity();
        try {
            list = this.mCustomPkgMgr.getAppInstallationPolicies(mode);
        } catch (Exception e) {
            Slog.d(TAG, "getAppInstallationPolicies", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return list;
    }

    public void setAppUninstallationPolicies(int mode, List<String> appPackageNames) {
        int addmode = appPackageNames == null ? 2 : 1;
        if (mode == 0) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.addAppUninstallationPoliciesBlacklist(addmode, appPackageNames);
            } catch (Exception e) {
                Slog.d(TAG, "addUnInstallPackageBlacklist", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        } else if (mode == 1) {
            long identity2 = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.addAppUninstallationPoliciesWhitelist(addmode, appPackageNames);
            } catch (Exception e2) {
                Slog.d(TAG, "addUnInstallPackageWhitelist", e2);
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(identity2);
                throw th2;
            }
            Binder.restoreCallingIdentity(identity2);
        }
    }

    public List<String> getAppUninstallationPolicies(int mode) {
        List<String> list = new ArrayList<>();
        long identity = Binder.clearCallingIdentity();
        try {
            list = this.mCustomPkgMgr.getAppUninstallationPolicies(mode);
        } catch (Exception e) {
            Slog.d(TAG, "getAppUnInstallationPolicies", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return list;
    }

    private void initCustWhiteList() {
        if (this.mCustWhiteListMgr == null) {
            this.mCustWhiteListMgr = new CustomizePkgWhiteListConfigMgr();
            if (this.mCustWhiteListMgr.loadWhiteListConfig()) {
                Slog.d(TAG, "loadWhiteListConfig success.");
            } else {
                Slog.w(TAG, "loadWhiteListConfig failed.");
            }
        }
    }

    public static void initCustomizeListPath() {
        String strCustomizeListPath;
        String strCustomRoot = SystemProperties.get("ro.oppo.oppo_custom_root", "/custom");
        if (new File(strCustomRoot, "build.prop").exists()) {
            strCustomizeListPath = strCustomRoot + "/etc/oppo_customize_whitelist.xml";
        } else {
            strCustomizeListPath = "/system/etc/oppo_customize_whitelist.xml";
        }
        SystemProperties.set("sys.custom.whitelist", strCustomizeListPath);
    }

    private class CustomizePkgWhiteListConfigMgr {
        private Map<String, ArrayList<String>> mWhiteListConfigMap = new HashMap();

        public CustomizePkgWhiteListConfigMgr() {
        }

        public boolean loadWhiteListConfig() {
            int type;
            String tag;
            String value;
            File file = new File(SystemProperties.get("sys.custom.whitelist", "/system/etc/oppo_customize_whitelist.xml"));
            if (!file.exists()) {
                Slog.w(OppoCustomizeService.TAG, "customize white file not exist!!!");
                return false;
            }
            Slog.d(OppoCustomizeService.TAG, "load customize list.");
            FileInputStream listFileInputStream = null;
            boolean success = false;
            try {
                listFileInputStream = new FileInputStream(file);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(listFileInputStream, null);
                do {
                    type = parser.next();
                    if (type == 2 && (tag = parser.getName()) != null && !tag.isEmpty() && (value = parser.getAttributeValue(null, "att")) != null && !value.isEmpty()) {
                        ArrayList<String> tmpList = this.mWhiteListConfigMap.get(tag);
                        if (tmpList == null) {
                            tmpList = new ArrayList<>();
                            this.mWhiteListConfigMap.put(tag, tmpList);
                        }
                        Slog.d(OppoCustomizeService.TAG, "get tag:" + tag + ", value:" + value);
                        tmpList.add(value);
                    }
                } while (type != 1);
                success = true;
                try {
                    listFileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (NullPointerException e2) {
                Slog.w(OppoCustomizeService.TAG, "failed parsing ", e2);
                if (listFileInputStream != null) {
                    listFileInputStream.close();
                }
            } catch (NumberFormatException e3) {
                Slog.w(OppoCustomizeService.TAG, "failed parsing ", e3);
                if (listFileInputStream != null) {
                    listFileInputStream.close();
                }
            } catch (XmlPullParserException e4) {
                Slog.w(OppoCustomizeService.TAG, "failed parsing ", e4);
                if (listFileInputStream != null) {
                    listFileInputStream.close();
                }
            } catch (IOException e5) {
                Slog.w(OppoCustomizeService.TAG, "failed parsing ", e5);
                if (listFileInputStream != null) {
                    listFileInputStream.close();
                }
            } catch (IndexOutOfBoundsException e6) {
                Slog.w(OppoCustomizeService.TAG, "failed parsing ", e6);
                if (listFileInputStream != null) {
                    listFileInputStream.close();
                }
            } catch (Throwable th) {
                if (listFileInputStream != null) {
                    try {
                        listFileInputStream.close();
                    } catch (IOException e7) {
                        e7.printStackTrace();
                    }
                }
                throw th;
            }
            return success;
        }
    }

    public void addInstallPackageBlacklist(int pattern, List<String> packageNames) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identify = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.addInstallPackageBlacklist(pattern, packageNames);
            } catch (Exception e) {
                Slog.d(TAG, "addInstallPackageBlacklist", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
            Binder.restoreCallingIdentity(identify);
        }
    }

    public void addInstallPackageWhitelist(int pattern, List<String> packageNames) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identify = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.addInstallPackageWhitelist(pattern, packageNames);
            } catch (Exception e) {
                Slog.d(TAG, "addInstallPackageWhitelist", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
            Binder.restoreCallingIdentity(identify);
        }
    }

    private long getAppErrorCountRef(UsageStats stats) {
        try {
            return ((Long) Class.forName("android.app.usage.UsageStats").getDeclaredMethod("getErrorCount", new Class[0]).invoke(stats, new Object[0])).longValue();
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "getAppErrorCount failed!", e);
            return 0;
        } catch (Exception e2) {
            Slog.e(TAG, "getAppErrorCount failed", e2);
            return 0;
        }
    }

    private void initOppoCustomPkgMgr(Context context) {
        this.mCustomPkgMgr = new OppoCustomizePackageManager(context);
    }

    private void registerOppoCutomizeManagerInternalImpl() {
        LocalServices.addService(OppoCutomizeManagerInternal.class, new OppoCutomizeManagerInternalImpl());
    }

    public void addDisallowUninstallApps(List<String> packageNames) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.addDisallowUninstallApps(packageNames);
        } catch (Exception e) {
            Slog.d(TAG, "addDisallowUninstallApps", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    public void removeDisallowUninstallApps(List<String> packageNames) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.removeDisallowUninstallApps(packageNames);
        } catch (Exception e) {
            Slog.d(TAG, "removeDisallowUninstallApps", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    public List<String> getallDisallowUninstallApps() {
        long identity = Binder.clearCallingIdentity();
        try {
            Slog.d(TAG, "getDisallowUninstallApps" + String.valueOf(this.mCustomPkgMgr.getDisallowUninstallApps()));
            return this.mCustomPkgMgr.getDisallowUninstallApps();
        } catch (Exception e) {
            Slog.d(TAG, "getDisallowUninstallApps", e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private class OppoCutomizeManagerInternalImpl extends OppoCutomizeManagerInternal {
        public OppoCutomizeManagerInternalImpl() {
        }

        public List<String> getDisallowUninstallApps() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.getDisallowUninstallApps();
            }
            return null;
        }

        public boolean isInstallSourceEnable() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.isInstallSourceEnable();
            }
            return false;
        }

        public List<String> getInstallSourceList() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.getInstalledSourceList();
            }
            return null;
        }

        public void sendBroadcastForArmy() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                OppoCustomizeService.this.mCustomPkgMgr.sendBroadcastForArmy();
            }
        }

        public List<String> getAppUninstallationPolicies(int listMode) {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.getAppUninstallationPolicies(listMode);
            }
            return null;
        }
    }

    public String[] listImei() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            return null;
        }
        int phoneCount = tm.getPhoneCount();
        String[] imeiList = new String[phoneCount];
        for (int phoneId = 0; phoneId < phoneCount; phoneId++) {
            imeiList[phoneId] = tm.getImei(phoneId);
        }
        return imeiList;
    }

    public String executeShellToSetIptables(String commandline) {
        String result = null;
        try {
            result = (String) Class.forName("com.android.server.NetworkManagementService").getMethod("executeShellToSetIptables", String.class).invoke(this.mNms, commandline);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result != null) {
            return result;
        }
        Slog.d(TAG, "executeShellToSetIptables is null.");
        return null;
    }

    public void setNetworkRestriction(int pattern) {
        Slog.d("chen-liu", "setNetworkRestriction: pattern = " + pattern);
        try {
            boolean result = ((Boolean) Class.forName("com.android.server.NetworkManagementService").getMethod("setNetworkRestriction", Integer.TYPE).invoke(this.mNms, Integer.valueOf(pattern))).booleanValue();
            Slog.d("chen-liu", "setNetworkRestriction: result = " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addNetworkRestriction(int pattern, List<String> list) {
        Slog.d("chen-liu", "addNetworkRestriction: pattern = " + pattern + " , list = " + list);
        String[] iplist = (String[]) list.toArray(new String[list.size()]);
        try {
            boolean result = ((Boolean) Class.forName("com.android.server.NetworkManagementService").getMethod("addNetworkRestriction", Integer.TYPE, String[].class).invoke(this.mNms, Integer.valueOf(pattern), iplist)).booleanValue();
            Slog.d("chen-liu", "setNetworkRestriction: result = " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeNetworkRestriction(int pattern, List<String> list) {
        Slog.d("chen-liu", "removeNetworkRestriction: pattern = " + pattern + " , list = " + list);
        String[] iplist = (String[]) list.toArray(new String[list.size()]);
        try {
            boolean result = ((Boolean) Class.forName("com.android.server.NetworkManagementService").getMethod("removeNetworkRestriction", Integer.TYPE, String[].class).invoke(this.mNms, Integer.valueOf(pattern), iplist)).booleanValue();
            Slog.d("chen-liu", "setNetworkRestriction: result = " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeNetworkRestrictionAll(int pattern) {
        Slog.d("chen-liu", "removeNetworkRestriction: pattern = " + pattern);
        try {
            boolean result = ((Boolean) Class.forName("com.android.server.NetworkManagementService").getMethod("removeNetworkRestrictionAll", Integer.TYPE).invoke(this.mNms, Integer.valueOf(pattern))).booleanValue();
            Slog.d("chen-liu", "setNetworkRestriction: result = " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStatusBarExpandPanelDisabled(boolean disable) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            try {
                Settings.Secure.putInt(this.mContext.getContentResolver(), STATUSBAR_EXPAND_DISABLE_KEY, disable ? 1 : 0);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public boolean isStatusBarExpandPanelDisabled() {
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            boolean result = true;
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), STATUSBAR_EXPAND_DISABLE_KEY, 0) != 1) {
                result = false;
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }
}
