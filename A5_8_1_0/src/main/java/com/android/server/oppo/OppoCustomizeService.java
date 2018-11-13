package com.android.server.oppo;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.IOppoCustomizeService.Stub;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoProcessManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.job.controllers.JobStatus;
import com.oppo.rutils.RUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OppoCustomizeService extends Stub {
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final String EXTERNEL_SDCARD_PATH = "/storage/sdcard1";
    private static final String PROTECT_APP_XML_FILE = "custom_protect_app.xml";
    private static final String PROTECT_APP_XML_PATH = "/data/system";
    private static final String TAG = "OppoCustomizeService";
    private static final SimpleStringSplitter sStringColonSplitter = new SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
    private List<String> clearList = new ArrayList();
    private ActivityManager mAm;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private DevicePolicyManager mDpm;
    private ArrayList<String> mProtectApplist = new ArrayList();

    private class PackageDataObserver extends IPackageDataObserver.Stub {
        /* synthetic */ PackageDataObserver(OppoCustomizeService this$0, PackageDataObserver -this1) {
            this();
        }

        private PackageDataObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            if (!OppoCustomizeService.this.clearList.contains(packageName)) {
                OppoCustomizeService.this.clearList.add(packageName);
            }
            Log.d(OppoCustomizeService.TAG, "clear user data packageName: " + packageName + "; succeeded: " + succeeded);
        }
    }

    public OppoCustomizeService(Context context) {
        this.mContext = context;
        this.mAm = (ActivityManager) this.mContext.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY);
        this.mDpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        initProtectApps();
    }

    public void deviceReboot() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Log.d(TAG, "reboot device!");
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
            Log.d(TAG, "shutdown device!");
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

    private static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        if (context == null) {
            return Collections.emptySet();
        }
        String enabledServicesSetting = Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
        if (enabledServicesSetting == null) {
            return Collections.emptySet();
        }
        Set<ComponentName> enabledServices = new HashSet();
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
        int i = 0;
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
                    enabledServicesBuilder.append(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
                }
                int enabledServicesBuilderLength = enabledServicesBuilder.length();
                if (enabledServicesBuilderLength > 0) {
                    enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
                }
                Secure.putString(this.mContext.getContentResolver(), "enabled_accessibility_services", enabledServicesBuilder.toString());
                ContentResolver contentResolver = this.mContext.getContentResolver();
                String str = "accessibility_enabled";
                if (accessibilityEnabled) {
                    i = 1;
                }
                Secure.putInt(contentResolver, str, i);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void setSDCardFormatted() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Log.d(TAG, "format sdcard!");
            long identity = Binder.clearCallingIdentity();
            try {
                String volumeState;
                StorageManager storageManager = (StorageManager) this.mContext.getSystemService("storage");
                try {
                    volumeState = storageManager.getVolumeState(EXTERNEL_SDCARD_PATH);
                } catch (IllegalArgumentException e) {
                    volumeState = null;
                }
                if (volumeState == null) {
                    Log.d(TAG, "no sdcard!!!");
                    Binder.restoreCallingIdentity(identity);
                    return;
                }
                StorageVolume[] volumes = storageManager.getVolumeList();
                for (int i = 0; i < volumes.length; i++) {
                    if (volumes[i] != null) {
                        String uuid = volumes[i].getUuid();
                        if (uuid != null) {
                            VolumeInfo volumeInfo = storageManager.findVolumeByUuid(uuid);
                            if (volumeInfo != null && volumeInfo.disk.isSd()) {
                                storageManager.partitionPublic(volumeInfo.getDiskId());
                                break;
                            }
                        }
                        continue;
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void killAppProcess(String packageName) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            long identity = Binder.clearCallingIdentity();
            try {
                for (RunningAppProcessInfo info : this.mAm.getRunningAppProcesses()) {
                    String[] pkgList = info.pkgList;
                    for (String equalsIgnoreCase : pkgList) {
                        if (equalsIgnoreCase.equalsIgnoreCase(packageName)) {
                            ActivityManagerNative.getDefault().killApplicationProcess(info.processName, info.uid);
                            Binder.restoreCallingIdentity(identity);
                            return;
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (RemoteException e) {
                Log.d(TAG, "kill app process failed!!!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public boolean isDeviceRoot() {
        Log.d(TAG, "is Device Root");
        boolean ret = false;
        long identity = Binder.clearCallingIdentity();
        try {
            if (RUtils.OppoRUtilsCompareSystemMD5() == -1) {
                ret = true;
            }
            Binder.restoreCallingIdentity(identity);
            return ret;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void clearUserData(String packageName) {
        this.mAm.clearApplicationUserData(packageName, new PackageDataObserver(this, null));
    }

    public void clearAllUserData() {
        ArrayList<String> userAppInfoList = getUserAppPackageNameList(this.mContext);
        if (userAppInfoList != null) {
            for (int i = 0; i < userAppInfoList.size(); i++) {
                clearUserData((String) userAppInfoList.get(i));
            }
        }
    }

    public static ArrayList<String> getUserAppPackageNameList(Context context) {
        ArrayList<String> userAppInfoList = null;
        if (context != null) {
            try {
                List<ApplicationInfo> allAppInfoList = context.getPackageManager().getInstalledApplications(0);
                userAppInfoList = new ArrayList();
                for (ApplicationInfo appInfo : allAppInfoList) {
                    if (!((appInfo.flags & 1) == 1 || (appInfo.packageName.equalsIgnoreCase(context.getPackageName()) ^ 1) == 0)) {
                        userAppInfoList.add(appInfo.packageName);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "getInstalledApplications fail!");
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
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
            Log.d(TAG, "set prop:" + prop + ",value:" + value);
            long identity = Binder.clearCallingIdentity();
            try {
                SystemProperties.set(prop, value);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    /* JADX WARNING: Missing block: B:27:0x008a, code:
            if (r6.equals("OTG_ENABLED") == false) goto L_0x0052;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDB(String key, int value) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            if (key != null) {
                Log.d(TAG, "set database,key:" + key + ",value:" + value);
            }
            long identity = Binder.clearCallingIdentity();
            try {
                if (!key.equals("oppo_settings_manager_fingerprint")) {
                    if (!key.equals("oppo_settings_manager_facelock")) {
                        if (!key.equals("oppo_settings_manager_time")) {
                            if (!key.equals("ZQ_ADB_ENABLED")) {
                                if (!key.equals("adb_enabled")) {
                                    if (!key.equals("MTP_TRANSFER_ENABLED")) {
                                    }
                                }
                            }
                        }
                    }
                }
                if (value == 0 || value == 1) {
                    Secure.putInt(this.mContext.getContentResolver(), key, value);
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public boolean setDeviceOwner(ComponentName cn) {
        boolean result = false;
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            return result;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            result = this.mDpm.setDeviceOwner(cn);
        } catch (Exception ex) {
            Log.d(TAG, "set Device Owner failed!", ex);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
        return result;
    }

    public void setEmmAdmin(ComponentName cn, boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            boolean isInstalled = true;
            String packageName = cn.getPackageName();
            try {
                this.mContext.getPackageManager().getApplicationInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Log.d(TAG, "set Device Admin failed, not install!");
                isInstalled = false;
            }
            if (isInstalled) {
                long identity = Binder.clearCallingIdentity();
                if (enable) {
                    try {
                        if (this.mDpm.isAdminActive(cn)) {
                            Log.d(TAG, "current package is active!");
                        } else {
                            Log.d(TAG, "set special Device Admin active");
                            this.mDpm.setActiveAdmin(cn, false);
                            try {
                                this.mAppOpsManager.setMode(43, this.mContext.getPackageManager().getApplicationInfo(packageName, 0).uid, packageName, 0);
                            } catch (NameNotFoundException e2) {
                            }
                        }
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                    }
                } else if (this.mDpm.isAdminActive(cn)) {
                    Log.d(TAG, "remove Device Admin");
                    this.mDpm.removeActiveAdmin(cn);
                }
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public Bitmap captureFullScreen() {
        Bitmap bitmap = null;
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.capturescreen")) {
            return bitmap;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Display display = ((WindowManager) this.mContext.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getRealMetrics(dm);
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            Log.d(TAG, "width :" + width + "height :" + height);
            bitmap = SurfaceControl.screenshot(width, height);
            return bitmap;
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
                    Log.d(TAG, "open gps");
                    Secure.setLocationProviderEnabled(this.mContext.getContentResolver(), "gps", true);
                    Secure.setLocationProviderEnabled(this.mContext.getContentResolver(), "network", true);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                Log.d(TAG, "close gps");
                Secure.setLocationProviderEnabled(this.mContext.getContentResolver(), "gps", false);
                Secure.setLocationProviderEnabled(this.mContext.getContentResolver(), "network", false);
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
                    Log.d(TAG, "open nfc");
                } else if (nfcAdapter == null || !enable) {
                    Log.e(TAG, "error when operate nfc!!!");
                } else {
                    Log.d(TAG, "close nfc");
                    nfcAdapter.enable();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void setSettingsRestriction(String key, boolean value) {
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            return;
        }
        if (TextUtils.isEmpty(key)) {
            Log.e(TAG, "setSettingsRestriction,type: " + key);
            return;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            ((UserManager) this.mContext.getSystemService("user")).setUserRestriction(key, value);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void updateConfiguration(Configuration config) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.updateconfig") && config != null) {
            long identity = Binder.clearCallingIdentity();
            try {
                ActivityManagerNative.getDefault().updateConfiguration(config);
            } catch (Exception ex) {
                Log.d(TAG, "update Configuration failed!", ex);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

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
                this.mAppOpsManager.setMode(43, this.mContext.getPackageManager().getApplicationInfo(packageName, 0).uid, packageName, 0);
            } catch (NameNotFoundException e) {
                Log.d(TAG, "allow GetUsageStats failed!", e);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void setAirplaneMode(boolean enable) {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.set_airplane_mode")) {
            long identity = Binder.clearCallingIdentity();
            try {
                ((ConnectivityManager) this.mContext.getSystemService("connectivity")).setAirplaneMode(enable);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
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
                Binder.restoreCallingIdentity(identity);
            } catch (Exception ex) {
                Log.d(TAG, "add protect application failed!", ex);
                result = false;
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            if (result) {
                synchronized (this.mProtectApplist) {
                    try {
                        if (!this.mProtectApplist.contains(packageName)) {
                            this.mProtectApplist.add(packageName);
                            if (saveToFile) {
                                saveListToFile(PROTECT_APP_XML_PATH, PROTECT_APP_XML_FILE, this.mProtectApplist);
                            }
                        }
                    } catch (Throwable th2) {
                        throw th2;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Exception ex) {
                Log.d(TAG, "remove protect application failed!", ex);
                result = false;
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            if (result) {
                synchronized (this.mProtectApplist) {
                    try {
                        this.mProtectApplist.remove(packageName);
                        saveListToFile(PROTECT_APP_XML_PATH, PROTECT_APP_XML_FILE, this.mProtectApplist);
                    } catch (Throwable th2) {
                        throw th2;
                    }
                }
            }
        }
    }

    public List<String> getProtectApplicationList() {
        ArrayList<String> tempList = new ArrayList();
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.addprotectapp")) {
            return tempList;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            tempList = OppoListManager.getInstance().getStageProtectListFromPkg(TAG, 0);
        } catch (Exception ex) {
            Log.d(TAG, "get protect application failed!", ex);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
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
                Log.i(TAG, "failed create file " + e);
            }
        }
        ArrayList<String> list = (ArrayList) readXMLFile(PROTECT_APP_XML_PATH, PROTECT_APP_XML_FILE);
        if (!(list == null || (list.isEmpty() ^ 1) == 0)) {
            for (String pkg : list) {
                addProtectApplicationInternal(pkg, false);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x01ad A:{SYNTHETIC, Splitter: B:65:0x01ad} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0188 A:{SYNTHETIC, Splitter: B:59:0x0188} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0149 A:{SYNTHETIC, Splitter: B:51:0x0149} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x010b A:{SYNTHETIC, Splitter: B:43:0x010b} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ce A:{SYNTHETIC, Splitter: B:35:0x00ce} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void saveListToFile(String path, String name, List<String> list) {
        IOException e;
        IllegalArgumentException e2;
        IllegalStateException e3;
        Exception e4;
        Throwable th;
        if (path != null && name != null && list != null) {
            File file = new File(path, name);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        return;
                    }
                } catch (IOException e5) {
                    Log.i(TAG, "failed create file " + e5);
                }
            }
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                try {
                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setOutput(fileos2, "UTF-8");
                    serializer.startDocument(null, Boolean.valueOf(true));
                    serializer.startTag(null, "gs");
                    for (int i = 0; i < list.size(); i++) {
                        String pkg = (String) list.get(i);
                        if (pkg != null) {
                            serializer.startTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                            serializer.attribute(null, "att", pkg);
                            serializer.endTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                        }
                    }
                    serializer.endTag(null, "gs");
                    serializer.endDocument();
                    serializer.flush();
                    if (fileos2 != null) {
                        try {
                            fileos2.close();
                        } catch (IOException e52) {
                            Log.i(TAG, "failed close stream " + e52);
                        }
                    }
                    fileos = fileos2;
                } catch (IllegalArgumentException e6) {
                    e2 = e6;
                    fileos = fileos2;
                    Log.i(TAG, "failed write file " + e2);
                    if (fileos != null) {
                        try {
                            fileos.close();
                        } catch (IOException e522) {
                            Log.i(TAG, "failed close stream " + e522);
                        }
                    }
                } catch (IllegalStateException e7) {
                    e3 = e7;
                    fileos = fileos2;
                    Log.i(TAG, "failed write file " + e3);
                    if (fileos != null) {
                        try {
                            fileos.close();
                        } catch (IOException e5222) {
                            Log.i(TAG, "failed close stream " + e5222);
                        }
                    }
                } catch (IOException e8) {
                    e5222 = e8;
                    fileos = fileos2;
                    Log.i(TAG, "failed write file " + e5222);
                    if (fileos != null) {
                        try {
                            fileos.close();
                        } catch (IOException e52222) {
                            Log.i(TAG, "failed close stream " + e52222);
                        }
                    }
                } catch (Exception e9) {
                    e4 = e9;
                    fileos = fileos2;
                    try {
                        Log.i(TAG, "failed write file " + e4);
                        if (fileos != null) {
                            try {
                                fileos.close();
                            } catch (IOException e522222) {
                                Log.i(TAG, "failed close stream " + e522222);
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileos != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileos = fileos2;
                    if (fileos != null) {
                        try {
                            fileos.close();
                        } catch (IOException e5222222) {
                            Log.i(TAG, "failed close stream " + e5222222);
                        }
                    }
                    throw th;
                }
            } catch (IllegalArgumentException e10) {
                e2 = e10;
                Log.i(TAG, "failed write file " + e2);
                if (fileos != null) {
                }
            } catch (IllegalStateException e11) {
                e3 = e11;
                Log.i(TAG, "failed write file " + e3);
                if (fileos != null) {
                }
            } catch (IOException e12) {
                e5222222 = e12;
                Log.i(TAG, "failed write file " + e5222222);
                if (fileos != null) {
                }
            } catch (Exception e13) {
                e4 = e13;
                Log.i(TAG, "failed write file " + e4);
                if (fileos != null) {
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x0198 A:{SYNTHETIC, Splitter: B:60:0x0198} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0155 A:{SYNTHETIC, Splitter: B:52:0x0155} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0112 A:{SYNTHETIC, Splitter: B:44:0x0112} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00cf A:{SYNTHETIC, Splitter: B:36:0x00cf} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x008e A:{SYNTHETIC, Splitter: B:28:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01bf A:{SYNTHETIC, Splitter: B:66:0x01bf} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> readXMLFile(String path, String name) {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        Throwable th;
        List<String> list = new ArrayList();
        File file = new File(path, name);
        if (!file.exists()) {
            return list;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                            String pkg = parser.getAttributeValue(null, "att");
                            if (pkg != null) {
                                list.add(pkg);
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e6) {
                        Log.i(TAG, "Failed to close state FileInputStream " + e6);
                    }
                }
                stream = stream2;
            } catch (NullPointerException e7) {
                e2 = e7;
                stream = stream2;
                Log.i(TAG, "failed parsing " + e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62) {
                        Log.i(TAG, "Failed to close state FileInputStream " + e62);
                    }
                }
                return list;
            } catch (NumberFormatException e8) {
                e3 = e8;
                stream = stream2;
                Log.i(TAG, "failed parsing " + e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622) {
                        Log.i(TAG, "Failed to close state FileInputStream " + e622);
                    }
                }
                return list;
            } catch (XmlPullParserException e9) {
                e4 = e9;
                stream = stream2;
                Log.i(TAG, "failed parsing " + e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        Log.i(TAG, "Failed to close state FileInputStream " + e6222);
                    }
                }
                return list;
            } catch (IOException e10) {
                e6222 = e10;
                stream = stream2;
                Log.i(TAG, "failed IOException " + e6222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        Log.i(TAG, "Failed to close state FileInputStream " + e62222);
                    }
                }
                return list;
            } catch (IndexOutOfBoundsException e11) {
                e5 = e11;
                stream = stream2;
                try {
                    Log.i(TAG, "failed parsing " + e5);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e622222) {
                            Log.i(TAG, "Failed to close state FileInputStream " + e622222);
                        }
                    }
                    return list;
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        Log.i(TAG, "Failed to close state FileInputStream " + e6222222);
                    }
                }
                throw th;
            }
        } catch (NullPointerException e12) {
            e2 = e12;
            Log.i(TAG, "failed parsing " + e2);
            if (stream != null) {
            }
            return list;
        } catch (NumberFormatException e13) {
            e3 = e13;
            Log.i(TAG, "failed parsing " + e3);
            if (stream != null) {
            }
            return list;
        } catch (XmlPullParserException e14) {
            e4 = e14;
            Log.i(TAG, "failed parsing " + e4);
            if (stream != null) {
            }
            return list;
        } catch (IOException e15) {
            e6222222 = e15;
            Log.i(TAG, "failed IOException " + e6222222);
            if (stream != null) {
            }
            return list;
        } catch (IndexOutOfBoundsException e16) {
            e5 = e16;
            Log.i(TAG, "failed parsing " + e5);
            if (stream != null) {
            }
            return list;
        }
        return list;
    }
}
