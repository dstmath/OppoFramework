package com.android.server.oppo;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityTaskManager;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IOppoArmyManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.common.OppoFeatureCache;
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
import android.net.OppoNetworkingControlManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Bundle;
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
import android.telephony.ColorOSTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.util.Xml;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.server.IColorAlarmManagerHelper;
import com.android.server.LocalServices;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.enterprise.OppoEnterpriseServiceHelper;
import com.android.server.job.controllers.JobStatus;
import com.android.server.oppo.OppoCustomizeNotificationHelper;
import com.android.server.pm.DumpState;
import com.color.view.inputmethod.ColorInputMethodManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import oppo.net.wifi.HotspotClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OppoCustomizeService extends IOppoCustomizeService.Stub {
    private static final int ADD_APP_LIST = 1;
    private static final String BLUETOOTH_POLICY = "persist.sys.bluetooth_policy";
    private static final String BSSID_BLACK = "BSSID_BLACK";
    private static final String BSSID_WHITE = "BSSID_WHITE";
    private static final String BT_BLACKLIST_TAG = "B";
    private static final String BT_CONF_NAME = "bt_customize_list.xml";
    private static final String BT_WHITELIST_TAG = "W";
    private static final String DATA_BLACKLIST = "network_restriction_blacklist.xml";
    private static final String DATA_WHITELIST = "network_restriction_whitelist.xml";
    private static final int DELETE_APP_LIST = 2;
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final String EXTERNEL_SDCARD_PATH = "/storage/sdcard1";
    private static final int INSTALLATION_BLACK_LIST = 0;
    private static final int INSTALLATION_WHITE_LIST = 1;
    private static final String METERED_DATA_BLACKLIST = "metered_data_blacklist.xml";
    private static final int MODE_BLACKLIST = 0;
    private static final int MODE_WHITELIST = 1;
    private static final String NETWORK_POLICY_BLACKLIST_TAG = "b";
    private static final String OPPO_CSIM = "CSIM";
    private static final String OPPO_CUSTOMIZE_SERVICE_VERSION = "2.0";
    private static final String OPPO_RUIM = "RUIM";
    private static final String OPPO_SIM = "SIM";
    private static final String OPPO_USIM = "USIM";
    public static final int PHONE_GEMINI_SIM_1 = 0;
    public static final int PHONE_GEMINI_SIM_2 = 1;
    private static final int POLICY_AllOW_MOBILEDATA_REJECT_WIFI = 2;
    private static final int POLICY_NONE = 0;
    private static final int POLICY_REJECT_ALL = 4;
    private static final int POLICY_REJECT_MOBILEDATA_AllOW_WIFI = 1;
    private static final String PROTECT_APP_XML_FILE = "custom_protect_app.xml";
    private static final String PROTECT_APP_XML_PATH = "/data/system";
    private static final String SSID_BLACK = "SSID_BLACK";
    private static final String SSID_WHITE = "SSID_WHITE";
    private static final String TAG = "OppoCustomizeService";
    private static final int TYPE_METERED_DATA = 0;
    private static final int TYPE_WIFI_DATA = 1;
    private static final String WLAN_DATA_BLACKLIST = "wlan_data_blacklist.xml";
    private static boolean sMtkGeminiSupport = false;
    private static boolean sOppoHwMtk = false;
    private static boolean sQualcommGeminiSupport = false;
    private static final TextUtils.SimpleStringSplitter sStringColonSplitter = new TextUtils.SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
    private final int BIT_IS_INSTALLED = 8;
    private final int BIT_USER_CFG = 4;
    private final String COLUMN_ALLOWED = "allowed";
    private final String COLUMN_PKG_NAME = "pkg_name";
    private final String CUSTOMIZE_OVERLAY_MENU_INVALID_LIST = "oppo_customize_overlay_menu_invalid_list";
    private final String CUSTOMIZE_SYSTEM_ALERT_ALLOWED_LIST = "customize_system_alert_allowed_list";
    private final String CUSTOMIZE_SYSTEM_ALERT_FORBIDDEN_LIST = "customize_system_alert_forbidden_list";
    private final String CUSTOMIZE_SYSTEM_ALERT_KEY_SEPARATOR = ":";
    private final int MODE_ALLOW_SYSTEM_ALERT_PERMISSION = 2;
    private final int MODE_FORBID_SYSTEM_ALERT_PERMISSION = 1;
    private final int MODE_RELEASE_SYSTEM_ALERT_PERMISSION = 0;
    private final Uri SAFECENTER_AUTHORITY_URI = Uri.parse("content://com.color.provider.SafeProvider");
    private final String TABLE_PP_FLOAT_WINDOW = "pp_float_window";
    private final Uri URI_FLOAT_WINDOW = Uri.withAppendedPath(this.SAFECENTER_AUTHORITY_URI, "pp_float_window");
    private List<String> clearList = new ArrayList();
    private ActivityManager mAm;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private CustomizePkgWhiteListConfigMgr mCustWhiteListMgr = null;
    private OppoCustomizePackageManager mCustomPkgMgr = null;
    private DevicePolicyManager mDpm;
    private final INetworkManagementService mNms;
    private OppoNetworkingControlManager mOppoNetworkingControlManager;
    private PackageManager mPm;
    private ArrayList<String> mProtectApplist = new ArrayList<>();
    private WifiManager mWifiManager;
    private List<String> systemAlertWindowAllowedList = Collections.synchronizedList(new ArrayList());
    private List<String> systemAlertWindowForbiddenList = Collections.synchronizedList(new ArrayList());
    private List<String> systemAlertWindowMenuInvalidList = Collections.synchronizedList(new ArrayList());

    /* access modifiers changed from: private */
    public static class SimUris {
        public static final Uri ALL_SPACE_URI = Uri.parse("content://icc/all_space");
        public static final Uri MTK_USIM_URI = Uri.parse("content://icc/pbr");
        public static final Uri MTK_USIM_URI1 = Uri.parse("content://icc/pbr/subId/");
        public static final Uri SAME_NAME_LENGTH_URI = Uri.parse("content://icc/sim_name_length");
        public static final Uri SDN_URI = Uri.parse("content://icc/sdn/");
        public static final Uri SIM_URI = Uri.parse("content://icc/adn");
        public static final Uri SPACE_URI = Uri.parse("content://icc/adn_capacity/subId/");
        public static final Uri SUBID_SDN_URI = Uri.parse("content://icc/sdn/subId");
        public static final Uri SUBID_SIM_URI = Uri.parse("content://icc/adn/subId/");
        public static final Uri USED_SPACE_URI = Uri.parse("content://icc/used_space");

        private SimUris() {
        }
    }

    /* access modifiers changed from: private */
    public class PackageDataObserver extends IPackageDataObserver.Stub {
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
        this.mNms = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        initEnterpriseService(context);
        initSystemAlertWindowControlList();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        initProtectApps();
        initOppoCustomPkgMgr(this.mContext);
        registerOppoCutomizeManagerInternalImpl();
        OppoBusinessFlashBackHelper.getInstance(context, this);
        this.mOppoNetworkingControlManager = OppoNetworkingControlManager.getOppoNetworkingControlManager();
        OppoCustomizeNotificationHelper.getInstance().init(context);
    }

    private void initEnterpriseService(Context context) {
        OppoEnterpriseServiceHelper.getInstance().init(context);
    }

    public void systemReady() {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            Slog.d(TAG, "systemReady startMdmCoreService");
            startMdmCoreService();
        }
    }

    public void deviceReboot() {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            Slog.d(TAG, "shutdown device!");
            long identity = Binder.clearCallingIdentity();
            try {
                Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                intent.setFlags(268435456);
                this.mContext.startActivity(intent);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public List<String> getAppRuntimeExceptionInfo() {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return null;
        }
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
                    enabledServicesBuilder.append(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
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

    public int getAccessibilityStatusFromSettings() {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return Settings.Secure.getInt(this.mContext.getContentResolver(), "accessibility_enabled", 0);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX INFO: finally extract failed */
    public List<ComponentName> getAccessiblityEnabledListFromSettings() {
        new HashSet();
        List<ComponentName> accessiblityEnabledAppList = new ArrayList<>();
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return accessiblityEnabledAppList;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Set<ComponentName> list = getEnabledServicesFromSettings(this.mContext);
            Binder.restoreCallingIdentity(identity);
            accessiblityEnabledAppList.addAll(list);
            return accessiblityEnabledAppList;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public void setSDCardFormatted() {
        String volumeState;
        String uuid;
        VolumeInfo volumeInfo;
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM) && prop != null && prop.length() > 0 && value != null) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
                                ApplicationInfo ai = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.SYSTEM);
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            long identity = Binder.clearCallingIdentity();
            try {
                TelephonyManager.getDefault().setDataEnabled(enable);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void openCloseGps(boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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

    public void resetFactory() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.factoryreset")) {
            long identity = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.wipemedia", "2");
                SystemClock.sleep(200);
                Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
                intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
                intent.putExtra("formatdata_backup", true);
                intent.putExtra("android.intent.extra.FORCE_MASTER_CLEAR", true);
                this.mContext.sendBroadcast(intent);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00a4 A[Catch:{ Exception -> 0x00b6, all -> 0x00b2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00d4 A[SYNTHETIC, Splitter:B:53:0x00d4] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00e0  */
    public boolean setDrawOverlays(String packageName, int mode) {
        ApplicationInfo applicationInfo;
        Exception e;
        int val;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM) || this.mAppOpsManager == null || (applicationInfo = checkPermissionAndGetApplicationInfo(packageName)) == null) {
            return false;
        }
        setPackageSystemAlertListByMode(packageName, mode);
        if (mode == 0) {
            return true;
        }
        long identity = Binder.clearCallingIdentity();
        String[] selectionArgs = {packageName};
        String[] projection = {"allowed"};
        ContentValues value = new ContentValues();
        Cursor cursor = null;
        try {
            try {
                Cursor cursor2 = this.mContext.getContentResolver().query(this.URI_FLOAT_WINDOW, projection, "pkg_name=?", selectionArgs, null);
                if (cursor2 != null) {
                    try {
                        if (cursor2.getCount() > 0) {
                            int columnIndex = cursor2.getColumnIndex("allowed");
                            cursor2.moveToNext();
                            int val2 = cursor2.getInt(columnIndex);
                            if (mode != 1) {
                                if (mode == 2) {
                                    value.put("allowed", Integer.valueOf(val2 | 4));
                                    val = 0;
                                    this.mContext.getContentResolver().update(this.URI_FLOAT_WINDOW, value, "pkg_name=?", selectionArgs);
                                    if (val != -1) {
                                        this.mAppOpsManager.setMode(24, applicationInfo.uid, applicationInfo.packageName, val);
                                    }
                                }
                            } else if ((val2 & 4) != 0) {
                                value.put("allowed", Integer.valueOf(val2 - 4));
                                val = 1;
                                this.mContext.getContentResolver().update(this.URI_FLOAT_WINDOW, value, "pkg_name=?", selectionArgs);
                                if (val != -1) {
                                }
                            }
                            val = -1;
                            try {
                                this.mContext.getContentResolver().update(this.URI_FLOAT_WINDOW, value, "pkg_name=?", selectionArgs);
                                if (val != -1) {
                                }
                            } catch (Exception e2) {
                                cursor = cursor2;
                                if (cursor == null) {
                                }
                                Binder.restoreCallingIdentity(identity);
                                return false;
                            } catch (Throwable th) {
                                e = th;
                                Binder.restoreCallingIdentity(identity);
                                throw e;
                            }
                        }
                    } catch (Exception e3) {
                        cursor = cursor2;
                        if (cursor == null) {
                        }
                        Binder.restoreCallingIdentity(identity);
                        return false;
                    } catch (Throwable th2) {
                        e = th2;
                        Binder.restoreCallingIdentity(identity);
                        throw e;
                    }
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
            } catch (Exception e4) {
                if (cursor == null) {
                }
                Binder.restoreCallingIdentity(identity);
                return false;
            }
        } catch (Exception e5) {
            if (cursor == null) {
                try {
                    cursor.close();
                } catch (Throwable th3) {
                    e = th3;
                    Binder.restoreCallingIdentity(identity);
                    throw e;
                }
            }
            Binder.restoreCallingIdentity(identity);
            return false;
        } catch (Throwable th4) {
            e = th4;
            Binder.restoreCallingIdentity(identity);
            throw e;
        }
        Binder.restoreCallingIdentity(identity);
        return false;
    }

    public int getDrawOverlays(String packageName) {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return -1;
        }
        if (this.systemAlertWindowAllowedList.contains(packageName)) {
            return 2;
        }
        if (this.systemAlertWindowForbiddenList.contains(packageName)) {
            return 1;
        }
        return 0;
    }

    private void initSystemAlertWindowControlList() {
        initSystemAlertWindowControlListByKey(this.systemAlertWindowAllowedList, "customize_system_alert_allowed_list");
        initSystemAlertWindowControlListByKey(this.systemAlertWindowForbiddenList, "customize_system_alert_forbidden_list");
        initSystemAlertWindowControlListByKey(this.systemAlertWindowMenuInvalidList, "oppo_customize_overlay_menu_invalid_list");
    }

    private void initSystemAlertWindowControlListByKey(List<String> list, String settingsKey) {
        String[] packageNames;
        String packageNamesStr = Settings.Secure.getString(this.mContext.getContentResolver(), settingsKey);
        if (!(TextUtils.isEmpty(packageNamesStr) || (packageNames = packageNamesStr.split(":")) == null || packageNames.length <= 0)) {
            for (String packageName : packageNames) {
                if (!TextUtils.isEmpty(packageName)) {
                    list.add(packageName);
                }
            }
        }
    }

    private void setPackageSystemAlertListByMode(String packageName, int mode) {
        if (mode == 0) {
            if (this.systemAlertWindowAllowedList.contains(packageName)) {
                this.systemAlertWindowAllowedList.remove(packageName);
                syncSystemAlertWindowControlListWithSettings("customize_system_alert_allowed_list", this.systemAlertWindowAllowedList);
            }
            if (this.systemAlertWindowForbiddenList.contains(packageName)) {
                this.systemAlertWindowForbiddenList.remove(packageName);
                syncSystemAlertWindowControlListWithSettings("customize_system_alert_forbidden_list", this.systemAlertWindowForbiddenList);
            }
            if (this.systemAlertWindowMenuInvalidList.contains(packageName)) {
                this.systemAlertWindowMenuInvalidList.remove(packageName);
                syncSystemAlertWindowControlListWithSettings("oppo_customize_overlay_menu_invalid_list", this.systemAlertWindowMenuInvalidList);
            }
        } else if (mode == 1) {
            if (!this.systemAlertWindowForbiddenList.contains(packageName)) {
                this.systemAlertWindowForbiddenList.add(packageName);
                syncSystemAlertWindowControlListWithSettings("customize_system_alert_forbidden_list", this.systemAlertWindowForbiddenList);
            }
            if (this.systemAlertWindowAllowedList.contains(packageName)) {
                this.systemAlertWindowAllowedList.remove(packageName);
                syncSystemAlertWindowControlListWithSettings("customize_system_alert_allowed_list", this.systemAlertWindowAllowedList);
            }
            if (!this.systemAlertWindowMenuInvalidList.contains(packageName)) {
                this.systemAlertWindowMenuInvalidList.add(packageName);
                syncSystemAlertWindowControlListWithSettings("oppo_customize_overlay_menu_invalid_list", this.systemAlertWindowMenuInvalidList);
            }
        } else if (mode == 2) {
            if (!this.systemAlertWindowAllowedList.contains(packageName)) {
                this.systemAlertWindowAllowedList.add(packageName);
                syncSystemAlertWindowControlListWithSettings("customize_system_alert_allowed_list", this.systemAlertWindowAllowedList);
            }
            if (this.systemAlertWindowForbiddenList.contains(packageName)) {
                this.systemAlertWindowForbiddenList.remove(packageName);
                syncSystemAlertWindowControlListWithSettings("customize_system_alert_forbidden_list", this.systemAlertWindowForbiddenList);
            }
            if (!this.systemAlertWindowMenuInvalidList.contains(packageName)) {
                this.systemAlertWindowMenuInvalidList.add(packageName);
                syncSystemAlertWindowControlListWithSettings("oppo_customize_overlay_menu_invalid_list", this.systemAlertWindowMenuInvalidList);
            }
        }
    }

    private void syncSystemAlertWindowControlListWithSettings(String settingsKey, List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        synchronized (list) {
            for (String packageName : list) {
                stringBuilder.append(packageName);
                stringBuilder.append(":");
            }
        }
        String listStr = stringBuilder.toString();
        if (!stringBuilder.toString().isEmpty()) {
            listStr = stringBuilder.substring(0, stringBuilder.lastIndexOf(":"));
        }
        Settings.Secure.putString(this.mContext.getContentResolver(), settingsKey, listStr);
    }

    private ApplicationInfo checkPermissionAndGetApplicationInfo(String packageName) {
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(packageName, 4096);
            if (packageInfo != null) {
                String[] permissionInfos = packageInfo.requestedPermissions;
                boolean requestPermissionFlag = false;
                if (permissionInfos != null && permissionInfos.length > 0) {
                    int length = permissionInfos.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        } else if ("android.permission.SYSTEM_ALERT_WINDOW".equals(permissionInfos[i])) {
                            requestPermissionFlag = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                if (!requestPermissionFlag) {
                    return null;
                }
                return packageInfo.applicationInfo;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "setDrawOverlays: NameNotFoundException");
        }
        return null;
    }

    public void allowGetUsageStats(String packageName) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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

    public void setAdbInstallUninstallDisabled(boolean disabled) {
        Context context = this.mContext;
        if (context == null) {
            Slog.d(TAG, "setAdbInstallUninstallDisabled: mContext==null");
        } else if (!context.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM) || SystemProperties.getBoolean(OppoEnterpriseServiceHelper.ADB_INSTALL_UNINSTALL_POLICY_PROPERTY, false) == disabled) {
        } else {
            if (disabled) {
                SystemProperties.set(OppoEnterpriseServiceHelper.ADB_INSTALL_UNINSTALL_POLICY_PROPERTY, TemperatureProvider.SWITCH_ON);
            } else {
                SystemProperties.set(OppoEnterpriseServiceHelper.ADB_INSTALL_UNINSTALL_POLICY_PROPERTY, TemperatureProvider.SWITCH_OFF);
            }
        }
    }

    public boolean getAdbInstallUninstallDisabled() {
        return SystemProperties.getBoolean(OppoEnterpriseServiceHelper.ADB_INSTALL_UNINSTALL_POLICY_PROPERTY, false);
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
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.addprotectapp") || SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.addprotectapp") || SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
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
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.addprotectapp") && !SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
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

    /* JADX INFO: Multiple debug info for r5v1 java.io.FileOutputStream: [D('fileos' java.io.FileOutputStream), D('e' java.io.IOException)] */
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
                if (0 != 0) {
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
                if (0 != 0) {
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
                if (0 != 0) {
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
                if (0 != 0) {
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
                if (0 != 0) {
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
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e4) {
            Slog.i(TAG, "failed parsing " + e4);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            Slog.i(TAG, "failed parsing " + e6);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e8) {
            Slog.i(TAG, "failed IOException " + e8);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e9) {
                    e = e9;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e10) {
            Slog.i(TAG, "failed parsing " + e10);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    e = e11;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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

    private void setBLBlackOrWhiteList(List<String> addressList, int btCustomizeMode, boolean enable) {
        String tail = btCustomizeMode == 1 ? BT_WHITELIST_TAG : BT_BLACKLIST_TAG;
        HashSet<String> hashSet = new HashSet<>(readXMLFile(PROTECT_APP_XML_PATH, BT_CONF_NAME));
        hashSet.remove(BT_BLACKLIST_TAG);
        hashSet.remove(BT_WHITELIST_TAG);
        if (enable) {
            Iterator<String> it = addressList.iterator();
            while (it.hasNext()) {
                hashSet.add(it.next() + "," + tail);
            }
        } else {
            Iterator<String> it2 = addressList.iterator();
            while (it2.hasNext()) {
                hashSet.remove(it2.next() + "," + tail);
            }
        }
        hashSet.add(tail);
        saveListToFile(PROTECT_APP_XML_PATH, BT_CONF_NAME, new ArrayList(hashSet));
        File bt_conf = new File(PROTECT_APP_XML_PATH, BT_CONF_NAME);
        if (bt_conf.exists()) {
            bt_conf.setReadable(true, false);
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.getState() == 12) {
            try {
                Method setBLBlackOrWhiteList = Class.forName("android.bluetooth.BluetoothAdapter").getMethod("setBLBlackOrWhiteList", List.class, Integer.TYPE, Boolean.TYPE);
                setBLBlackOrWhiteList.setAccessible(true);
                setBLBlackOrWhiteList.invoke(bluetoothAdapter, addressList, Integer.valueOf(btCustomizeMode), Boolean.valueOf(enable));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> getBLBlackOrWhiteList(int btCustomizeMode) {
        String tail;
        List<String> resolveList = new ArrayList<>();
        if (btCustomizeMode == 1) {
            tail = ",W";
        } else {
            tail = ",B";
        }
        try {
            List<String> xmlList = readXMLFile(PROTECT_APP_XML_PATH, BT_CONF_NAME);
            if (xmlList != null) {
                if (xmlList.size() != 0) {
                    for (String strLine : xmlList) {
                        String[] strSplit = strLine.split(",");
                        if (strLine.indexOf(tail) > -1 && strSplit.length > 1) {
                            resolveList.add(strSplit[0]);
                        }
                    }
                    return resolveList;
                }
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void addBluetoothDevicesToWhiteList(List<String> addressList) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            setBLBlackOrWhiteList(addressList, 1, true);
        }
    }

    public void addBluetoothDevicesToBlackList(List<String> addressList) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            setBLBlackOrWhiteList(addressList, 0, true);
        }
    }

    public void removeBluetoothDevicesFromWhiteList(List<String> addressList) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            setBLBlackOrWhiteList(addressList, 1, false);
        }
    }

    public void removeBluetoothDevicesFromBlackList(List<String> addressList) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            setBLBlackOrWhiteList(addressList, 0, false);
        }
    }

    public List<String> getBluetoothDevicesFromWhiteLists() {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return null;
        }
        return getBLBlackOrWhiteList(1);
    }

    public List<String> getBluetoothDevicesFromBlackLists() {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return null;
        }
        return getBLBlackOrWhiteList(0);
    }

    public void setBLWhiteList(ComponentName admin, List<String> addressList, boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            setBLBlackOrWhiteList(addressList, 1, enable);
        }
    }

    public void setBLBlackList(ComponentName admin, List<String> addressList, boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            setBLBlackOrWhiteList(addressList, 0, enable);
        }
    }

    public void setWlanApClientWhiteList(List<String> list) {
    }

    public List<String> getWlanApClientWhiteList() {
        return null;
    }

    public void removeWlanApClientWhiteList(List<String> list) {
    }

    public void setWlanApClientBlackList(List<String> list) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
            long identity = Binder.clearCallingIdentity();
            try {
                List<String> whiteList = getWlanApClientBlackList();
                if (whiteList != null && !whiteList.isEmpty()) {
                    removeWlanApClientBlackList(whiteList);
                    Thread.sleep(100);
                }
                for (String address : list) {
                    if (address.length() > 0) {
                        HotspotClient hotspotClient = new HotspotClient(address, true);
                        Class.forName("android.net.wifi.WifiManager").getMethod("blockClient", Class.forName("oppo.net.wifi.HotspotClient")).invoke(this.mWifiManager, hotspotClient);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX INFO: finally extract failed */
    public List<String> getWlanApClientBlackList() {
        List<String> hotspotList = new ArrayList<>();
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return hotspotList;
        }
        this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            List<HotspotClient> hotspotClients = (List) Class.forName("android.net.wifi.WifiManager").getMethod("getBlockedHotspotClients", new Class[0]).invoke(this.mWifiManager, new Object[0]);
            if (hotspotClients != null && hotspotClients.size() > 0) {
                for (HotspotClient hotspotClient : hotspotClients) {
                    hotspotList.add(hotspotClient.deviceAddress);
                }
            }
            Binder.restoreCallingIdentity(identity);
            return hotspotList;
        } catch (Exception ex) {
            ex.printStackTrace();
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public void removeWlanApClientBlackList(List<String> list) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
            long identity = Binder.clearCallingIdentity();
            try {
                Method method = Class.forName("android.net.wifi.WifiManager").getMethod("unblockClient", Class.forName("oppo.net.wifi.HotspotClient"));
                for (String address : list) {
                    if (address.length() > 0) {
                        HotspotClient hotspotClient = new HotspotClient(address, true);
                        method.invoke(this.mWifiManager, hotspotClient);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void startMdmCoreService() {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM) && this.mCustWhiteListMgr == null) {
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

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        int opti = 0;
        while (opti < args.length) {
            String cmd = opti < args.length ? args[opti] : "";
            opti++;
            if ("version".equals(cmd)) {
                fout.println(OPPO_CUSTOMIZE_SERVICE_VERSION);
            } else if ("business".equals(cmd)) {
                OppoBusinessFlashBackHelper.dump(fout);
            }
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

    /* access modifiers changed from: private */
    public class OppoCutomizeManagerInternalImpl extends OppoCutomizeManagerInternal {
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

        public List<String> getInstalledAppBlackList() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.getInstalledAppBlackList();
            }
            return null;
        }

        public List<String> getInstalledAppWhiteList() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.getInstalledAppWhiteList();
            }
            return null;
        }

        public List<String> getPrivInstallSysAppList() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.getPrivInstallSysAppList();
            }
            return null;
        }

        public List<String> getDetachableInstallSysAppList() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.getDetachableInstallSysAppList();
            }
            return null;
        }

        public List<String> getAllInstallSysAppList() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.getAllInstallSysAppList();
            }
            return null;
        }

        public List<String> getAccessibilityServiceWhiteList() {
            if (OppoCustomizeService.this.mCustomPkgMgr != null) {
                return OppoCustomizeService.this.mCustomPkgMgr.getAccessibilityServiceWhiteList();
            }
            return null;
        }
    }

    private static boolean colorIsQcomSubActive(Context context, int slotId) {
        ColorOSTelephonyManager telephonyManager = ColorOSTelephonyManager.getDefault(context);
        Slog.d(TAG, "colorIsQcomSubActive");
        try {
            return telephonyManager.colorIsQcomSubActive(slotId) && !isSoftSimCard(context, slotId);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasIccCardGemini(Context context, int slotId) {
        ColorOSTelephonyManager mColorTelephonyManager = ColorOSTelephonyManager.getDefault(context);
        Slog.d(TAG, "hasIccCardGemini");
        if (mColorTelephonyManager == null || !mColorTelephonyManager.hasIccCardGemini(slotId) || isSoftSimCard(context, slotId)) {
            return false;
        }
        return true;
    }

    private static boolean hasIccCard(Context context) {
        Slog.d(TAG, "hasIccCard");
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager == null || !telephonyManager.hasIccCard() || hasSoftSimCard(context)) {
            return false;
        }
        return true;
    }

    private static boolean isSimCardAvailable(Context context) {
        return (isSimCardAvailable(context, 0) && !isSoftSimCard(context, 0) && !isSimCardAbsent(context, 0)) || (isSimCardAvailable(context, 1) && !isSoftSimCard(context, 1) && !isSimCardAbsent(context, 1));
    }

    private static boolean isSimCardAvailable(Context context, int slotId) {
        boolean bIccCard = false;
        Slog.d(TAG, "isSimCardAvailable slotId:" + slotId);
        if (sQualcommGeminiSupport) {
            return colorIsQcomSubActive(context, slotId);
        }
        if (isGeminiSupported()) {
            try {
                bIccCard = sMtkGeminiSupport ? hasIccCardGemini(context, slotId) : colorIsQcomSubActive(context, slotId);
            } catch (Exception e) {
            }
        } else {
            hasIccCard(context);
        }
        Slog.d(TAG, "isSimCardAvailable bIccCard:" + bIccCard);
        return bIccCard;
    }

    private static boolean isSimCardAbsent(Context context, int slotId) {
        int nowSimState = 0;
        Slog.d(TAG, "isSimCardAbsent");
        if (isGeminiSupported()) {
            try {
                nowSimState = ColorOSTelephonyManager.getDefault(context).getSimStateGemini(slotId);
            } catch (Exception e) {
            }
        } else {
            try {
                nowSimState = TelephonyManager.getDefault().getSimState();
            } catch (Exception e2) {
            }
        }
        return 1 == nowSimState;
    }

    private static Uri getResultUri(Uri uri, int subId, int slotId) {
        Slog.d(TAG, "getResultUri");
        return Uri.withAppendedPath(uri, String.valueOf(subId));
    }

    private static Uri resolveIntent(Context context, int slotId) {
        Uri uri = null;
        ColorOSTelephonyManager telManager = ColorOSTelephonyManager.getDefault(context);
        Slog.d(TAG, "resolveIntent slotId:" + slotId);
        try {
            if (!sOppoHwMtk || !isGeminiSupported()) {
                if (sOppoHwMtk) {
                    if (TextUtils.equals(telManager.getIccCardTypeGemini(0), OPPO_USIM)) {
                        uri = SimUris.MTK_USIM_URI;
                    } else {
                        uri = SimUris.SIM_URI;
                    }
                } else if (sQualcommGeminiSupport) {
                    uri = getResultUri(SimUris.SUBID_SIM_URI, ColorOSTelephonyManager.colorgetSubId(context, slotId), slotId);
                } else {
                    uri = SimUris.SIM_URI;
                }
                Slog.d(TAG, "getResultUri uri:" + uri);
                return uri;
            }
            int subIds = ColorOSTelephonyManager.colorgetSubId(context, slotId);
            String cardType = "";
            if (telManager != null) {
                cardType = telManager.getIccCardTypeGemini(slotId);
            }
            if (TextUtils.equals(cardType, OPPO_USIM) || TextUtils.equals(cardType, OPPO_CSIM)) {
                uri = getResultUri(SimUris.MTK_USIM_URI1, subIds, slotId);
            } else {
                uri = getResultUri(SimUris.SUBID_SIM_URI, subIds, slotId);
            }
            Slog.d(TAG, "getResultUri uri:" + uri);
            return uri;
        } catch (Exception e) {
        }
    }

    private static boolean hasSoftSimCard(Context context) {
        if (context == null) {
            Slog.e(TAG, "hasSoftSimCard false, context is null");
            return false;
        }
        boolean result = ColorOSTelephonyManager.getDefault(context).isColorHasSoftSimCard();
        Slog.d(TAG, "hasSoftSimCard " + result);
        return result;
    }

    private static boolean isSoftSimCard(Context context, int slotIdx) {
        if (context == null) {
            Slog.e(TAG, "isSoftSimCard false, context is null");
            return false;
        } else if (slotIdx != ColorOSTelephonyManager.getDefault(context).colorGetSoftSimCardSlotId()) {
            return false;
        } else {
            Slog.d(TAG, "isSoftSimCard true, slot " + slotIdx);
            return true;
        }
    }

    private static boolean isGeminiSupported() {
        return sMtkGeminiSupport || sQualcommGeminiSupport;
    }

    public Uri getSimContactsUri(int slotId) {
        Slog.d(TAG, "getSimContactsUri");
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            Slog.d(TAG, "getSimContactsUri no feature return null");
            return null;
        }
        sMtkGeminiSupport = this.mContext.getPackageManager().hasSystemFeature("mtk.gemini.support");
        sQualcommGeminiSupport = this.mContext.getPackageManager().hasSystemFeature("oppo.qualcomm.gemini.support");
        sOppoHwMtk = this.mContext.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
        long identity = Binder.clearCallingIdentity();
        Uri uri = null;
        try {
            boolean isAvailable = isSimCardAvailable(this.mContext);
            Slog.d(TAG, "getSimContactsUri slotId " + slotId + ",isAvailable " + isAvailable);
            if ((slotId == 0 || slotId == 1) && isAvailable) {
                if (sQualcommGeminiSupport) {
                    uri = Uri.withAppendedPath(SimUris.SUBID_SIM_URI, String.valueOf(ColorOSTelephonyManager.colorgetSubId(this.mContext, slotId)));
                } else if (sMtkGeminiSupport) {
                    uri = resolveIntent(this.mContext, slotId);
                } else {
                    uri = SimUris.SIM_URI;
                }
                Binder.restoreCallingIdentity(identity);
                return uri;
            }
            Slog.d(TAG, "getSimContacts args invalid or simCard not available");
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Exception e) {
            Slog.d(TAG, "getSimContactsUri error");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public void addDisabledDeactivateMdmPackages(List<String> packageNames) {
        if (OppoEnterpriseServiceHelper.getInstance().hasOppoEnterpriseFeature()) {
            try {
                OppoEnterpriseServiceHelper.getInstance().saveListToFile(OppoEnterpriseServiceHelper.INTERACTIVE_CONTROL_LIST_PATH, OppoEnterpriseServiceHelper.DISABLED_DEACTIVATE_CONTROL_FILENAME, packageNames);
            } catch (Exception e) {
                Slog.d("addDisabledDeactivateMdmPackages: ", "failed");
            }
        }
    }

    public void removeDisabledDeactivateMdmPackages(List<String> packageNames) {
        if (OppoEnterpriseServiceHelper.getInstance().hasOppoEnterpriseFeature()) {
            try {
                List<String> mdmPackageNames = OppoEnterpriseServiceHelper.getInstance().readXMLFile(OppoEnterpriseServiceHelper.INTERACTIVE_CONTROL_LIST_PATH, OppoEnterpriseServiceHelper.DISABLED_DEACTIVATE_CONTROL_FILENAME);
                mdmPackageNames.removeAll(packageNames);
                OppoEnterpriseServiceHelper.getInstance().saveListToFile(OppoEnterpriseServiceHelper.INTERACTIVE_CONTROL_LIST_PATH, OppoEnterpriseServiceHelper.DISABLED_DEACTIVATE_CONTROL_FILENAME, mdmPackageNames);
            } catch (Exception e) {
                Slog.d("removeDisabledDeactivateMdmPackages:", "failed");
            }
        }
    }

    public List<String> getDisabledDeactivateMdmPackages(ComponentName admin) {
        if (!OppoEnterpriseServiceHelper.getInstance().hasOppoEnterpriseFeature()) {
            return null;
        }
        List<String> mdmPackageNames = new ArrayList<>();
        try {
            return OppoEnterpriseServiceHelper.getInstance().readXMLFile(OppoEnterpriseServiceHelper.INTERACTIVE_CONTROL_LIST_PATH, OppoEnterpriseServiceHelper.DISABLED_DEACTIVATE_CONTROL_FILENAME);
        } catch (Exception e) {
            Slog.d("getDisabledDeactivateMdmPackages:", "failed");
            return mdmPackageNames;
        }
    }

    public void removeAllDisabledDeactivateMdmPackages(ComponentName admin) {
        if (OppoEnterpriseServiceHelper.getInstance().hasOppoEnterpriseFeature()) {
            try {
                List<String> mdmPackageNames = OppoEnterpriseServiceHelper.getInstance().readXMLFile(OppoEnterpriseServiceHelper.INTERACTIVE_CONTROL_LIST_PATH, OppoEnterpriseServiceHelper.DISABLED_DEACTIVATE_CONTROL_FILENAME);
                if (mdmPackageNames != null && !mdmPackageNames.isEmpty()) {
                    mdmPackageNames.clear();
                    OppoEnterpriseServiceHelper.getInstance().saveListToFile(OppoEnterpriseServiceHelper.INTERACTIVE_CONTROL_LIST_PATH, OppoEnterpriseServiceHelper.DISABLED_DEACTIVATE_CONTROL_FILENAME, mdmPackageNames);
                }
            } catch (Exception e) {
                Slog.d("removeAllDisabledDeactivateMdmPackages:", "failed");
            }
        }
    }

    public boolean setWifiSsidWhiteList(List<String> list) {
        boolean ret = false;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = ((Boolean) Class.forName("android.net.wifi.WifiManager").getMethod("setWifiRestrictionList", String.class, List.class, String.class).invoke(this.mWifiManager, packageName, list, SSID_WHITE)).booleanValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public List<String> getWifiSsidWhiteList() {
        List<String> ret = new ArrayList<>();
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return ret;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = (List) Class.forName("android.net.wifi.WifiManager").getMethod("getWifiRestrictionList", String.class, String.class).invoke(this.mWifiManager, packageName, SSID_WHITE);
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    public boolean setWifiBssidWhiteList(List<String> list) {
        boolean ret = false;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = ((Boolean) Class.forName("android.net.wifi.WifiManager").getMethod("setWifiRestrictionList", String.class, List.class, String.class).invoke(this.mWifiManager, packageName, list, BSSID_WHITE)).booleanValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public List<String> getWifiBssidWhiteList() {
        List<String> ret = new ArrayList<>();
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return ret;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = (List) Class.forName("android.net.wifi.WifiManager").getMethod("getWifiRestrictionList", String.class, String.class).invoke(this.mWifiManager, packageName, BSSID_WHITE);
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    public boolean setWifiSsidBlackList(List<String> list) {
        boolean ret = false;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = ((Boolean) Class.forName("android.net.wifi.WifiManager").getMethod("setWifiRestrictionList", String.class, List.class, String.class).invoke(this.mWifiManager, packageName, list, SSID_BLACK)).booleanValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public List<String> getWifiSsidBlackList() {
        List<String> ret = new ArrayList<>();
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return ret;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = (List) Class.forName("android.net.wifi.WifiManager").getMethod("getWifiRestrictionList", String.class, String.class).invoke(this.mWifiManager, packageName, SSID_BLACK);
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    public boolean setWifiBssidBlackList(List<String> list) {
        boolean ret = false;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = ((Boolean) Class.forName("android.net.wifi.WifiManager").getMethod("setWifiRestrictionList", String.class, List.class, String.class).invoke(this.mWifiManager, packageName, list, BSSID_BLACK)).booleanValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public List<String> getWifiBssidBlackList() {
        List<String> ret = new ArrayList<>();
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return ret;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = (List) Class.forName("android.net.wifi.WifiManager").getMethod("getWifiRestrictionList", String.class, String.class).invoke(this.mWifiManager, packageName, BSSID_BLACK);
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    public boolean isWhiteListedSSID(String ssid) {
        new ArrayList();
        List<String> ssidList = getWifiSsidWhiteList();
        if (ssid == null || ssidList == null || ssidList.isEmpty()) {
            return false;
        }
        for (String id : ssidList) {
            if (ssid.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlackListedSSID(String ssid) {
        new ArrayList();
        List<String> ssidList = getWifiSsidBlackList();
        if (ssid == null || ssidList == null || ssidList.isEmpty()) {
            return false;
        }
        for (String id : ssidList) {
            if (ssid.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWhiteListedBSSID(String bssid) {
        new ArrayList();
        List<String> bssidList = getWifiBssidWhiteList();
        if (bssid == null || bssidList == null || bssidList.isEmpty()) {
            return false;
        }
        for (String id : bssidList) {
            if (bssid.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlackListedBSSID(String bssid) {
        new ArrayList();
        List<String> bssidList = getWifiBssidBlackList();
        if (bssid == null || bssidList == null || bssidList.isEmpty()) {
            return false;
        }
        for (String id : bssidList) {
            if (bssid.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean removeSSIDFromWhiteList(List<String> ssids) {
        boolean ret = false;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = ((Boolean) Class.forName("android.net.wifi.WifiManager").getMethod("removeFromRestrictionList", String.class, List.class, String.class).invoke(this.mWifiManager, packageName, ssids, SSID_WHITE)).booleanValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    public boolean removeSSIDFromBlackList(List<String> ssids) {
        boolean ret = false;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = ((Boolean) Class.forName("android.net.wifi.WifiManager").getMethod("removeFromRestrictionList", String.class, List.class, String.class).invoke(this.mWifiManager, packageName, ssids, SSID_BLACK)).booleanValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    public boolean removeBSSIDFromWhiteList(List<String> bssids) {
        boolean ret = false;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = ((Boolean) Class.forName("android.net.wifi.WifiManager").getMethod("removeFromRestrictionList", String.class, List.class, String.class).invoke(this.mWifiManager, packageName, bssids, BSSID_WHITE)).booleanValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    public boolean removeBSSIDFromBlackList(List<String> bssids) {
        boolean ret = false;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        long identity = Binder.clearCallingIdentity();
        try {
            ret = ((Boolean) Class.forName("android.net.wifi.WifiManager").getMethod("removeFromRestrictionList", String.class, List.class, String.class).invoke(this.mWifiManager, packageName, bssids, BSSID_BLACK)).booleanValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
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
            setNetworkRestrictionList(list, pattern, true);
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
            setNetworkRestrictionList(list, pattern, false);
            boolean result = ((Boolean) Class.forName("com.android.server.NetworkManagementService").getMethod("removeNetworkRestriction", Integer.TYPE, String[].class).invoke(this.mNms, Integer.valueOf(pattern), iplist)).booleanValue();
            Slog.d("chen-liu", "setNetworkRestriction: result = " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeNetworkRestrictionAll(int pattern) {
        Slog.d("chen-liu", "removeNetworkRestriction: pattern = " + pattern);
        try {
            clearNetworkRestrictionList(pattern);
            boolean result = ((Boolean) Class.forName("com.android.server.NetworkManagementService").getMethod("removeNetworkRestrictionAll", Integer.TYPE).invoke(this.mNms, Integer.valueOf(pattern))).booleanValue();
            Slog.d("chen-liu", "setNetworkRestriction: result = " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNetworkRestrictionList(List<String> list, int pattern, boolean isAdd) {
        Slog.d(TAG, "setNetworkRestrictionList start");
        String fileName = "";
        if (pattern == 1) {
            fileName = DATA_BLACKLIST;
        } else if (pattern == 2) {
            fileName = DATA_WHITELIST;
        }
        HashSet<String> hashSet = new HashSet<>(readXMLFile(PROTECT_APP_XML_PATH, fileName));
        hashSet.remove(NETWORK_POLICY_BLACKLIST_TAG);
        if (isAdd) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                hashSet.add(it.next() + "," + NETWORK_POLICY_BLACKLIST_TAG);
            }
        } else {
            Iterator<String> it2 = list.iterator();
            while (it2.hasNext()) {
                hashSet.remove(it2.next() + "," + NETWORK_POLICY_BLACKLIST_TAG);
            }
        }
        hashSet.add(NETWORK_POLICY_BLACKLIST_TAG);
        saveListToFile(PROTECT_APP_XML_PATH, fileName, new ArrayList(hashSet));
        Slog.d(TAG, "setNetworkRestrictionList end");
    }

    private void clearNetworkRestrictionList(int pattern) {
        Slog.d(TAG, "clearNetworkRestrictionList start");
        String fileName = "";
        if (pattern == 1) {
            fileName = DATA_BLACKLIST;
        } else if (pattern == 2) {
            fileName = DATA_WHITELIST;
        }
        HashSet<String> hashSet = new HashSet<>(readXMLFile(PROTECT_APP_XML_PATH, fileName));
        hashSet.clear();
        saveListToFile(PROTECT_APP_XML_PATH, fileName, new ArrayList(hashSet));
        Slog.d(TAG, "clearNetworkRestrictionList end");
    }

    public List<String> getNetworkRestrictionList(int pattern) {
        Slog.d(TAG, "getNetworkRestrictionList start");
        List<String> resolveList = new ArrayList<>();
        String fileName = "";
        if (pattern == 1) {
            fileName = DATA_BLACKLIST;
        } else if (pattern == 2) {
            fileName = DATA_WHITELIST;
        }
        try {
            List<String> xmlList = readXMLFile(PROTECT_APP_XML_PATH, fileName);
            if (xmlList != null) {
                if (xmlList.size() != 0) {
                    for (String strPkg : xmlList) {
                        String[] strSplit = strPkg.split(",");
                        if (strPkg.indexOf(NETWORK_POLICY_BLACKLIST_TAG) > -1 && strSplit.length > 1) {
                            resolveList.add(strSplit[0]);
                        }
                    }
                    Slog.d(TAG, "getNetworkRestrictionList end");
                    return resolveList;
                }
            }
            Slog.d(TAG, "getNetworkRestrictionList null");
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public int getVpnServiceState() {
        int result = 0;
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            if (nets == null) {
                return 0;
            }
            Iterator it = Collections.list(nets).iterator();
            while (it.hasNext()) {
                NetworkInterface netint = (NetworkInterface) it.next();
                if (netint.isUp()) {
                    if (netint.getInterfaceAddresses().size() != 0) {
                        if ("tun0".equals(netint.getName()) || "ppp0".equals(netint.getName())) {
                            result = 1;
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            Slog.d(TAG, "getVpnServiceState", e);
            return -1;
        }
    }

    public String getPhoneNumber(int subId) {
        try {
            Class cx = Class.forName("android.telephony.SubscriptionManager");
            return (String) cx.getMethod("getPhoneNumber", Integer.TYPE, Context.class).invoke(cx, Integer.valueOf(subId), this.mContext);
        } catch (Exception e) {
            Slog.d(TAG, "getPhoneNumber", e);
            return "";
        }
    }

    public void disconnectAllVpn() {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            try {
                Class.forName("android.net.ConnectivityManager").getMethod("disconnectAllVpn", new Class[0]).invoke((ConnectivityManager) this.mContext.getSystemService("connectivity"), new Object[0]);
            } catch (Exception e) {
                Slog.d(TAG, "disconnectAllVpn", e);
            }
        }
    }

    public void activateSubId(int subId) {
        try {
            Class cx = Class.forName("android.telephony.SubscriptionManager");
            cx.getMethod("activateSubId", Integer.TYPE).invoke(cx, Integer.valueOf(subId));
        } catch (Exception e) {
            Slog.d(TAG, "activateSubId catch error" + e);
        }
    }

    public void deactivateSubId(int subId) {
        try {
            Class cx = Class.forName("android.telephony.SubscriptionManager");
            cx.getMethod("deactivateSubId", Integer.TYPE).invoke(cx, Integer.valueOf(subId));
        } catch (Exception e) {
            Slog.d(TAG, "deactivateSubId catch error" + e);
        }
    }

    public void addInstallSource(String packageName) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.addInstallSource(packageName);
        } catch (Exception e) {
            Slog.d(TAG, "addInstallSource", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    /* JADX INFO: finally extract failed */
    public List<String> getInstallSourceList() {
        long identity = Binder.clearCallingIdentity();
        try {
            List<String> installedSourceList = this.mCustomPkgMgr.getInstalledSourceList();
            Binder.restoreCallingIdentity(identity);
            return installedSourceList;
        } catch (Exception e) {
            Slog.d(TAG, "getInstallSourceList", e);
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public boolean isInstallSourceEnable() {
        long identity = Binder.clearCallingIdentity();
        boolean ret = false;
        try {
            ret = this.mCustomPkgMgr.isInstallSourceEnable();
        } catch (Exception e) {
            Slog.d(TAG, "isInstallSourceEnable", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return ret;
    }

    public void deleteInstallSource(String pkgName) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.deleteInstallSource(pkgName);
        } catch (Exception e) {
            Slog.d(TAG, "deleteInstallSource", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    public void enableInstallSource(boolean enable) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.enableInstallSource(enable);
        } catch (Exception e) {
            Slog.d(TAG, "enableInstallSource", e);
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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

    public void removeDisallowedRunningApp(List<String> packageNames) {
        long identify;
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            identify = Binder.clearCallingIdentity();
            if (packageNames == null || packageNames.size() <= 0) {
                Slog.d(TAG, "removeDisallowedRunningApp:appPkgNamesList is empty!");
                return;
            }
            IOppoArmyManager service = getOppoArmyManager();
            if (service == null) {
                Slog.d(TAG, "removeDisallowedRunningApp:IOppoArmyService is empty!");
                return;
            }
            try {
                service.removeDisallowedRunningApp(packageNames);
                Binder.restoreCallingIdentity(identify);
                return;
            } catch (RemoteException e) {
                Slog.d(TAG, "removeDisallowedRunningApp failed.");
            } catch (Exception e2) {
                Slog.e(TAG, "removeDisallowedRunningApp failed", e2);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
        } else {
            return;
        }
        Binder.restoreCallingIdentity(identify);
    }

    public List<String> getDisallowedRunningApp() {
        long identify;
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return null;
        }
        identify = Binder.clearCallingIdentity();
        IOppoArmyManager service = getOppoArmyManager();
        if (service == null) {
            Slog.d(TAG, "getDisallowedRunningApp:IOppoArmyService is empty!");
            return null;
        }
        try {
            List<String> disallowedRunningApp = service.getDisallowedRunningApp();
            Binder.restoreCallingIdentity(identify);
            return disallowedRunningApp;
        } catch (RemoteException e) {
            Slog.d(TAG, "getDisallowedRunningApp failed.");
        } catch (Exception e2) {
            Slog.e(TAG, "getDisallowedRunningApp failed", e2);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
        Binder.restoreCallingIdentity(identify);
        return null;
    }

    public void addAppAlarmWhiteList(List<String> packageNames) {
        OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).addAppAlarmWhiteList(packageNames);
    }

    public List<String> getAppAlarmWhiteList() {
        return OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).getAppAlarmWhiteList();
    }

    public boolean removeAppAlarmWhiteList(List<String> packageNames) {
        return OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).removeAppAlarmWhiteList(packageNames);
    }

    public boolean removeAllAppAlarmWhiteList() {
        return OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).removeAllAppAlarmWhiteList();
    }

    public void addInstallPackageBlacklist(int pattern, List<String> packageNames) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
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

    public void addAccessibilityServiceToWhiteList(List<String> serviceList) {
        Slog.d(TAG, "addAccessibilityServiceToWhiteList serviceList:" + serviceList);
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            long identify = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.addAccessibilityServiceToWhiteList(serviceList);
            } catch (Exception e) {
                Slog.d(TAG, "addAccessibilityServiceToWhiteList", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
            Binder.restoreCallingIdentity(identify);
        }
    }

    public void removeAccessibilityServiceFromWhiteList(List<String> serviceList) {
        Slog.d(TAG, "removeAccessibilityServiceFromWhiteList serviceList:" + serviceList);
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            long identify = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.removeAccessibilityServiceFromWhiteList(serviceList);
            } catch (Exception e) {
                Slog.d(TAG, "removeAccessibilityServiceFromWhiteList", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
            Binder.restoreCallingIdentity(identify);
        }
    }

    public List<String> getAccessibilityServiceWhiteList() {
        Slog.d(TAG, "getAccessibilityServiceWhiteList");
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return null;
        }
        long identify = Binder.clearCallingIdentity();
        try {
            return this.mCustomPkgMgr.getAccessibilityServiceWhiteList();
        } catch (Exception e) {
            Slog.d(TAG, "getAccessibilityServiceWhiteList", e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    public void deleteAccessibilityServiceWhiteList() {
        Slog.d(TAG, "deleteAccessibilityServiceWhiteList");
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            long identify = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.deleteAccessibilityServiceWhiteList();
            } catch (Exception e) {
                Slog.d(TAG, "deleteAccessibilityServiceWhiteList", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
            Binder.restoreCallingIdentity(identify);
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean forceStopPackage(List<String> pkgs, int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            IActivityManager am = ActivityManager.getService();
            if (am != null) {
                try {
                    for (String pkgName : pkgs) {
                        List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasks(100, 0, userId).getList();
                        if (recentTasks != null) {
                            for (ActivityManager.RecentTaskInfo recentTask : recentTasks) {
                                if (pkgName != null && pkgName.equals(recentTask.realActivity.getPackageName()) && recentTask.id >= 0) {
                                    ActivityTaskManager.getService().removeTask(recentTask.id);
                                }
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Slog.d(TAG, "force stop app failed!!!");
                    Binder.restoreCallingIdentity(token);
                    return false;
                }
            }
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public void backupAppData(String src, String packageName, String dest, int requestId) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            Slog.d(TAG, "backupAppData src:" + src + " packageName:" + packageName + " dest:" + dest + " requestId:" + requestId);
            long identify = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.backupDataApp(src, packageName, dest, requestId);
            } catch (Exception e) {
                Slog.d(TAG, "backupAppData", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
            Binder.restoreCallingIdentity(identify);
        }
    }

    public void setDefaultInputMethod(String methodId) {
        long identify = Binder.clearCallingIdentity();
        try {
            new ColorInputMethodManager().setDefaultInputMethod(methodId);
        } catch (Exception e) {
            Slog.e(TAG, "setDefaultInputMethod failed", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
        Binder.restoreCallingIdentity(identify);
    }

    public String getDefaultInputMethod() {
        long identify = Binder.clearCallingIdentity();
        String method = "";
        try {
            method = new ColorInputMethodManager().getDefaultInputMethod();
        } catch (Exception e) {
            Slog.e(TAG, "getDefaultInputMethod failed", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
        Binder.restoreCallingIdentity(identify);
        return method;
    }

    public void clearDefaultInputMethod() {
        long identify = Binder.clearCallingIdentity();
        try {
            new ColorInputMethodManager().clearDefaultInputMethod();
        } catch (Exception e) {
            Slog.e(TAG, "clearDefaultInputMethod failed", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
        Binder.restoreCallingIdentity(identify);
    }

    public void addAppMeteredDataBlackList(List<String> pkgs) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            setNetPolicyBlackList(pkgs, 0, true);
        }
    }

    public void addAppWlanDataBlackList(List<String> pkgs) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            setNetPolicyBlackList(pkgs, 1, true);
        }
    }

    public void removeAppMeteredDataBlackList(List<String> pkgs) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            if (pkgs == null) {
                pkgs = getAppMeteredDataBlackList();
            }
            setNetPolicyBlackList(pkgs, 0, false);
        }
    }

    public void removeAppWlanDataBlackList(List<String> pkgs) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            if (pkgs == null) {
                pkgs = getAppWlanDataBlackList();
            }
            setNetPolicyBlackList(pkgs, 1, false);
        }
    }

    public List<String> getAppMeteredDataBlackList() {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return null;
        }
        return getNetPolicyBlackList(0);
    }

    public List<String> getAppWlanDataBlackList() {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return null;
        }
        return getNetPolicyBlackList(1);
    }

    public Bundle getInstallSysAppBundle() {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        Bundle bundle = null;
        try {
            bundle = this.mCustomPkgMgr.getInstallSysAppBundle();
        } catch (Exception e) {
            Slog.d(TAG, "getInstallSysAppBundle", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return bundle;
    }

    public void setInstallSysAppBundle(Bundle bundle) {
        if (this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mCustomPkgMgr.setInstallSysAppBundle(bundle);
            } catch (Exception e) {
                Slog.d(TAG, "setInstallSysAppBundle", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX INFO: finally extract failed */
    private void setNetPolicyBlackList(List<String> pkgs, int networkType, boolean enable) {
        Slog.d(TAG, "setNetPolicyBlackList start");
        if (pkgs != null && !pkgs.isEmpty()) {
            String fileName = "";
            if (networkType == 0) {
                fileName = METERED_DATA_BLACKLIST;
            } else if (networkType == 1) {
                fileName = WLAN_DATA_BLACKLIST;
            }
            HashSet<String> hashSet = new HashSet<>(readXMLFile(PROTECT_APP_XML_PATH, fileName));
            hashSet.remove(NETWORK_POLICY_BLACKLIST_TAG);
            if (enable) {
                Iterator<String> it = pkgs.iterator();
                while (it.hasNext()) {
                    hashSet.add(it.next() + "," + NETWORK_POLICY_BLACKLIST_TAG);
                }
            } else {
                Iterator<String> it2 = pkgs.iterator();
                while (it2.hasNext()) {
                    hashSet.remove(it2.next() + "," + NETWORK_POLICY_BLACKLIST_TAG);
                }
            }
            hashSet.add(NETWORK_POLICY_BLACKLIST_TAG);
            saveListToFile(PROTECT_APP_XML_PATH, fileName, new ArrayList(hashSet));
            long identify = Binder.clearCallingIdentity();
            try {
                if (this.mOppoNetworkingControlManager != null) {
                    for (String strPkg : pkgs) {
                        try {
                            this.mOppoNetworkingControlManager.setUidPolicy(getUidForPkgName(strPkg), getNetworkPolicyForBlackListedApp(strPkg));
                        } catch (Exception e) {
                            Slog.e(TAG, "setUidPolicy failed", e);
                        }
                    }
                }
                Binder.restoreCallingIdentity(identify);
                Slog.d(TAG, "setNetPolicyBlackList end");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
        }
    }

    private int getUidForPkgName(String pkgName) {
        int uid = -1;
        if (TextUtils.isEmpty(pkgName)) {
            return -1;
        }
        try {
            uid = this.mPm.getApplicationInfo(pkgName, 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.d(TAG, "Exception = " + e);
        }
        Slog.d(TAG, "getUidForPkgName:" + uid);
        return uid;
    }

    private int getNetworkPolicyForBlackListedApp(String pkgName) {
        int policy = 0;
        if (!TextUtils.isEmpty(pkgName)) {
            if (isMeteredDataBlackListedApp(pkgName) && isWlanDataBlackListedApp(pkgName)) {
                policy = 4;
            } else if (!isMeteredDataBlackListedApp(pkgName) && isWlanDataBlackListedApp(pkgName)) {
                policy = 1;
            } else if (!isMeteredDataBlackListedApp(pkgName) || isWlanDataBlackListedApp(pkgName)) {
                policy = 0;
            } else {
                policy = 2;
            }
        }
        Slog.d(TAG, "getNetworkPolicyForBlackListedApp:" + pkgName + "policy: " + policy);
        return policy;
    }

    private List<String> getNetPolicyBlackList(int networkType) {
        List<String> resolveList = new ArrayList<>();
        String fileName = "";
        if (networkType == 0) {
            fileName = METERED_DATA_BLACKLIST;
        } else if (networkType == 1) {
            fileName = WLAN_DATA_BLACKLIST;
        }
        try {
            List<String> xmlList = readXMLFile(PROTECT_APP_XML_PATH, fileName);
            if (xmlList != null) {
                if (xmlList.size() != 0) {
                    for (String strPkg : xmlList) {
                        String[] strSplit = strPkg.split(",");
                        if (strPkg.indexOf(NETWORK_POLICY_BLACKLIST_TAG) > -1 && strSplit.length > 1) {
                            resolveList.add(strSplit[0]);
                        }
                    }
                    return resolveList;
                }
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean isMeteredDataBlackListedApp(String pkgName) {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        return isNetPolicyBlackListedApp(pkgName, 1);
    }

    private boolean isWlanDataBlackListedApp(String pkgName) {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        return isNetPolicyBlackListedApp(pkgName, 0);
    }

    private boolean isNetPolicyBlackListedApp(String pkgName, int networkType) {
        List<String> pkgList = new ArrayList<>();
        if (networkType == 0) {
            pkgList = getAppMeteredDataBlackList();
        } else if (networkType == 1) {
            pkgList = getAppWlanDataBlackList();
        }
        if (pkgName == null || pkgList == null || pkgList.isEmpty()) {
            return false;
        }
        for (String pkg : pkgList) {
            if (pkgName.equalsIgnoreCase(pkg)) {
                return true;
            }
        }
        return false;
    }

    public boolean setSuperWhiteList(List<String> list) {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        long identify = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.changeSuperWhiteList(0, list);
            return true;
        } catch (Exception e) {
            Slog.d(TAG, "setSuperWhiteList", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    public List<String> getSuperWhiteList() {
        new ArrayList();
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return null;
        }
        long identify = Binder.clearCallingIdentity();
        try {
            return this.mCustomPkgMgr.getSuperWhiteList();
        } catch (Exception e) {
            Slog.d(TAG, "getSuperWhiteList", e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    public boolean clearSuperWhiteList(List<String> clearList2) {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        long identify = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.changeSuperWhiteList(1, clearList2);
            return true;
        } catch (Exception e) {
            Slog.d(TAG, "clearSuperWhiteList", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    public boolean clearAllSuperWhiteList() {
        if (!this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM)) {
            return false;
        }
        long identify = Binder.clearCallingIdentity();
        try {
            this.mCustomPkgMgr.changeSuperWhiteList(2, null);
            return true;
        } catch (Exception e) {
            Slog.d(TAG, "clearAllSuperWhiteList", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }
}
