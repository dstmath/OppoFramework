package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.StorageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.INetworkStatsService;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.Binder;
import android.os.Bundle;
import android.os.IOppoCustomizeService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.ProcessCpuTracker;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager;
import com.oppo.enterprise.mdmcoreservice.help.DeviceApplicationManagerHelper;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DeviceStateManagerImpl extends IDeviceStateManager.Stub {
    private ActivityManager mAm;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private PackageManager mPm;
    private int mPreSleepTime = 86400000;
    private boolean mScreenOn = false;
    private IOppoCustomizeService mService;

    public DeviceStateManagerImpl(Context context) {
        this.mContext = context;
        this.mAm = (ActivityManager) this.mContext.getSystemService("activity");
        this.mPm = this.mContext.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
    }

    private IOppoCustomizeService getCustomizeService() {
        if (this.mService != null) {
            return this.mService;
        }
        this.mService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
        return this.mService;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public String[] getDeviceState() {
        PermissionManager.getInstance().checkPermission();
        return new String[]{getCpuUsedPercentValue(), getMemoryUsedPercentValue(this.mContext), getStorageUsedPercentValue(this.mContext)};
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public List<String> getAppRuntimeExceptionInfo() {
        PermissionManager.getInstance().checkPermission();
        try {
            return getCustomizeService().getAppRuntimeExceptionInfo();
        } catch (RemoteException e) {
            Log.w("DeviceStateManagerImpl", "getAppRuntimeExceptionInfo error");
            e.printStackTrace();
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public void keepSrceenOn() {
        PermissionManager.getInstance().checkPermission();
        this.mPreSleepTime = getScreenSleepTime();
        SharedPreferences.Editor passwdfile = this.mContext.getSharedPreferences("key_previous_sleep_time", 0).edit();
        if (passwdfile != null) {
            passwdfile.putInt("key_previous_sleep_time", this.mPreSleepTime == 86400000 ? 15000 : this.mPreSleepTime);
            passwdfile.commit();
        }
        SystemProperties.set("sys.keepscreenon.status", "true");
        setScreenSleepTime(86400000);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public void cancelSrceenOn() {
        PermissionManager.getInstance().checkPermission();
        this.mPreSleepTime = this.mContext.getSharedPreferences("key_previous_sleep_time", 0).getInt("key_previous_sleep_time", 15000);
        if (86400000 == this.mPreSleepTime) {
            this.mPreSleepTime = 15000;
        }
        setScreenSleepTime(this.mPreSleepTime);
        SystemProperties.set("sys.keepscreenon.status", "false");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public boolean getScreenState() {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getBoolean("sys.keepscreenon.status", false);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public void ignoringBatteryOptimizations(ComponentName admin, String packageName) {
        PermissionManager.getInstance().checkPermission();
        if (packageName == null || packageName.isEmpty()) {
            Log.e("DeviceStateManagerImpl", "ignoringBatteryOptimizations fail! packageName is null or empty");
            return;
        }
        ArrayList<String> whitelist = new ArrayList<>();
        whitelist.add(packageName);
        Intent intent = new Intent("coloros.intent.action.MODIFY_POWERSAVE_WHITELIST_CUSTOM");
        intent.putExtra("op", "add");
        intent.putStringArrayListExtra("packages", whitelist);
        this.mContext.sendBroadcast(intent);
    }

    private int getScreenSleepTime() {
        try {
            return Settings.System.getInt(this.mContext.getContentResolver(), "screen_off_timeout");
        } catch (Exception e) {
            return 15000;
        }
    }

    private void setScreenSleepTime(int nSleepTime) {
        Log.i("DeviceStateManagerImpl", "in setScreenSleepTime(), nSleepTime = " + nSleepTime);
        Settings.System.putInt(this.mContext.getContentResolver(), "screen_off_timeout", nSleepTime);
    }

    private String getCpuUsedPercentValue() {
        try {
            ProcessCpuTracker processCpuTracker = new ProcessCpuTracker(false);
            processCpuTracker.update();
            Log.d("DeviceStateManagerImpl", "getCpuUsedPercentValue is " + String.format("%.2f", Float.valueOf(processCpuTracker.getTotalCpuPercent())) + "%");
            return String.format("%.2f", Float.valueOf(processCpuTracker.getTotalCpuPercent())) + "%";
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("DeviceStateManagerImpl", e.getMessage());
            return null;
        }
    }

    private String getMemoryUsedPercentValue(Context context) {
        BufferedReader br = null;
        String subMemoryLine = "";
        String ret = "";
        try {
            BufferedReader br2 = new BufferedReader(new FileReader("/proc/meminfo"), 2048);
            String memoryLine = br2.readLine();
            if (memoryLine != null) {
                subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            }
            br2.close();
            long totalMemorySize = (long) Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            if (totalMemorySize != 0) {
                Log.d("DeviceStateManagerImpl", "getMemoryUsedPercentValue is " + ((int) ((((float) (totalMemorySize - (getAvailableMemory(context) / 1024))) / ((float) totalMemorySize)) * 100.0f)) + "%");
                ret = ((int) ((((float) (totalMemorySize - (getAvailableMemory(context) / 1024))) / ((float) totalMemorySize)) * 100.0f)) + "%";
            }
            try {
                br2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ret;
        } catch (IOException e2) {
            Log.w("DeviceStateManagerImpl", e2.getMessage());
            e2.printStackTrace();
            if (0 != 0) {
                try {
                    br.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            return ret;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    br.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
            return ret;
        }
    }

    private long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService("activity")).getMemoryInfo(mi);
        return mi.availMem;
    }

    private String getStorageUsedPercentValue(Context context) {
        for (VolumeInfo vol : ((StorageManager) context.getSystemService("storage")).getVolumes()) {
            if (vol.getType() == 1 && vol.isMountedReadable()) {
                long totalBytes = 0;
                long usedBytes = 0;
                File path = vol.getPath();
                if (path != null) {
                    try {
                        StorageStatsManager mStorageStats = (StorageStatsManager) context.getSystemService(StorageStatsManager.class);
                        if (mStorageStats != null) {
                            totalBytes = mStorageStats.getTotalBytes(vol.getFsUuid());
                        }
                    } catch (IOException e) {
                        Log.w("DeviceStateManagerImpl", e.getMessage());
                    }
                    if (totalBytes <= 0) {
                        totalBytes = path.getTotalSpace();
                    }
                    usedBytes = totalBytes - path.getFreeSpace();
                }
                Formatter.formatFileSize(context, usedBytes);
                Formatter.formatFileSize(context, totalBytes);
                if (totalBytes > 0) {
                    Log.d("DeviceStateManagerImpl", "getStorageUsedPercentValue is " + ((int) ((100 * usedBytes) / totalBytes)) + "%");
                    return ((int) ((100 * usedBytes) / totalBytes)) + "%";
                }
            }
        }
        Log.w("DeviceStateManagerImpl", "getStorageUsedPercentValue return null");
        return null;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public List getAppRunInfo() {
        PermissionManager.getInstance().checkPermission();
        if (this.mContext == null || this.mPm == null || this.mAm == null) {
            return new ArrayList();
        }
        return DeviceApplicationManagerHelper.getInstance().getAppRunInfo(this.mContext, this.mAm, this.mPm);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public Map<String, String> getAppPowerUsage(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        BatteryStatsHelper helper = new BatteryStatsHelper(this.mContext);
        helper.create(new Bundle());
        helper.refreshStats(1, -1);
        List<BatterySipper> batterySippers = helper.getUsageList();
        Map<String, Double> batterySummary = new HashMap<>();
        if (batterySippers != null && batterySippers.size() > 0) {
            for (int i = 0; i < batterySippers.size(); i++) {
                BatterySipper bs = batterySippers.get(i);
                if (AnonymousClass1.$SwitchMap$com$android$internal$os$BatterySipper$DrainType[bs.drainType.ordinal()] == 1) {
                    if (batterySummary.containsKey(bs.packageWithHighestDrain)) {
                        batterySummary.put(bs.packageWithHighestDrain, Double.valueOf(bs.totalPowerMah + batterySummary.get(bs.packageWithHighestDrain).doubleValue()));
                    } else {
                        batterySummary.put(bs.packageWithHighestDrain, Double.valueOf(bs.totalPowerMah));
                    }
                }
            }
        }
        Map<String, String> batteryUsage = new HashMap<>();
        for (Map.Entry<String, Double> entry : batterySummary.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().equals("noPkg")) {
                batteryUsage.put(entry.getKey(), BatteryStatsHelper.makemAh(entry.getValue().doubleValue()));
            }
        }
        return batteryUsage;
    }

    /* renamed from: com.oppo.enterprise.mdmcoreservice.service.managerimpl.DeviceStateManagerImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$os$BatterySipper$DrainType = new int[BatterySipper.DrainType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.APP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public List<String> getRunningApplication(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = this.mAm.getRunningAppProcesses();
        Set<String> packageNameSet = new HashSet<>();
        if (runningAppProcessInfos != null) {
            for (ActivityManager.RunningAppProcessInfo info : runningAppProcessInfos) {
                String[] pkgList = info.pkgList;
                if (pkgList != null) {
                    for (String pkgName : pkgList) {
                        packageNameSet.add(pkgName);
                    }
                }
            }
        }
        return new ArrayList(packageNameSet);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public void allowGetUsageStats(ComponentName admin, String packageName) {
        PermissionManager.getInstance().checkPermission();
        if (this.mAppOpsManager == null) {
            Log.d("DeviceStateManagerImpl", "allowGetUsageStats: mAppOpsManager == null");
            return;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            ApplicationInfo applicationInfo = this.mContext.getPackageManager().getApplicationInfo(packageName, 0);
            if (applicationInfo == null) {
                Log.d("DeviceStateManagerImpl", "allowGetUsageStats: applicationInfo == null");
                Binder.restoreCallingIdentity(identity);
                return;
            }
            this.mAppOpsManager.setMode(43, applicationInfo.uid, packageName, 0);
            Binder.restoreCallingIdentity(identity);
        } catch (Exception e) {
            Log.e("DeviceStateManagerImpl", "allowGetUsageStats fail", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public Map getSoftwareInfo(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<PackageInfo> packageInfos = this.mPm.getInstalledPackages(512);
        if (packageInfos == null || packageInfos.size() <= 0) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> softwareInfos = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo != null) {
                List<String> softwareInfo = new ArrayList<>();
                softwareInfo.add(packageInfo.applicationInfo.loadLabel(this.mPm).toString());
                softwareInfo.add(packageInfo.packageName);
                softwareInfo.add(sdf.format(Long.valueOf(packageInfo.firstInstallTime)));
                softwareInfo.add(sdf.format(Long.valueOf(packageInfo.lastUpdateTime)));
                softwareInfo.add(packageInfo.versionName);
                softwareInfo.add(packageInfo.versionCode + "");
                softwareInfo.add("android");
                if (packageInfo.packageName != null) {
                    softwareInfos.put(packageInfo.packageName, softwareInfo);
                }
            }
        }
        return softwareInfos;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public boolean getSystemIntegrity(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return !getCustomizeService().isDeviceRoot();
        } catch (Exception e) {
            Log.e("DeviceStateManagerImpl", "getSystemIntegrity fail!", e);
            return false;
        }
    }

    private static String getActiveSubscriberId(Context context) {
        return TelephonyManager.from(context).getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceStateManager
    public long[] getAppTrafficInfo(ComponentName componentName, int pid) throws RemoteException {
        Exception e;
        int wifisize;
        int size;
        long wifidataflow;
        String subscriberId;
        long dataflow;
        NetworkTemplate wifitemplate;
        long wifidataflow2;
        int i = pid;
        PermissionManager.getInstance().checkPermission();
        Calendar cal = Calendar.getInstance();
        int i2 = 1;
        cal.set(cal.get(1), cal.get(2), cal.get(5), 0, 0, 0);
        cal.set(5, cal.getActualMinimum(5));
        long wifidataflow3 = 0;
        long dataflow2 = 0;
        NetworkStats.Entry entry = null;
        NetworkStats.Entry wifientry = null;
        long[] str = {0, 0};
        try {
            INetworkStatsService mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
            NetworkTemplate wifitemplate2 = NetworkTemplate.buildTemplateWifi();
            try {
                NetworkStats wifistats = mStatsService.openSession().getSummaryForAllUid(wifitemplate2, cal.getTimeInMillis(), System.currentTimeMillis(), false);
                if (wifistats != null) {
                    try {
                        wifisize = wifistats.size();
                    } catch (Exception e2) {
                        e = e2;
                    }
                } else {
                    wifisize = 0;
                }
                if (wifistats != null) {
                    long wifidataflow4 = 0;
                    int j = 0;
                    while (j < wifisize) {
                        try {
                            wifientry = wifistats.getValues(j, wifientry);
                            if (i == wifientry.uid) {
                                if (wifientry.set != 0) {
                                    try {
                                        if (i2 != wifientry.set) {
                                            wifitemplate = wifitemplate2;
                                        }
                                    } catch (Exception e3) {
                                        e = e3;
                                        Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                                        return str;
                                    }
                                }
                                wifitemplate = wifitemplate2;
                                try {
                                    try {
                                        wifidataflow2 = wifidataflow4 + wifientry.rxBytes + wifientry.txBytes;
                                        Log.d("DeviceStateManagerImpl", "wifiDataflow" + wifidataflow2);
                                        j++;
                                        wifidataflow4 = wifidataflow2;
                                        wifitemplate2 = wifitemplate;
                                        i2 = 1;
                                        i = pid;
                                    } catch (Exception e4) {
                                        e = e4;
                                        Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                                        return str;
                                    }
                                } catch (Exception e5) {
                                    e = e5;
                                    Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                                    return str;
                                }
                            } else {
                                wifitemplate = wifitemplate2;
                            }
                            wifidataflow2 = wifidataflow4;
                        } catch (Exception e6) {
                            e = e6;
                            Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                            return str;
                        }
                        try {
                            Log.d("DeviceStateManagerImpl", "wifiDataflow" + wifidataflow2);
                            j++;
                            wifidataflow4 = wifidataflow2;
                            wifitemplate2 = wifitemplate;
                            i2 = 1;
                            i = pid;
                        } catch (Exception e7) {
                            e = e7;
                            Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                            return str;
                        }
                    }
                    wifidataflow3 = wifidataflow4;
                }
                try {
                    str[1] = wifidataflow3;
                    String subscriberId2 = getActiveSubscriberId(this.mContext);
                    if (subscriberId2 == null) {
                        try {
                            Log.w("DeviceStateManagerImpl", "no subscriber id!");
                            return str;
                        } catch (Exception e8) {
                            e = e8;
                            Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                            return str;
                        }
                    } else {
                        try {
                            NetworkStats stats = mStatsService.openSession().getSummaryForAllUid(NetworkTemplate.buildTemplateMobileAll(subscriberId2), cal.getTimeInMillis(), System.currentTimeMillis(), false);
                            if (stats != null) {
                                try {
                                    size = stats.size();
                                } catch (Exception e9) {
                                    e = e9;
                                }
                            } else {
                                size = 0;
                            }
                            if (stats != null) {
                                long dataflow3 = 0;
                                int i3 = 0;
                                while (i3 < size) {
                                    try {
                                        entry = stats.getValues(i3, entry);
                                        if (pid == entry.uid) {
                                            try {
                                                if (entry.set != 0) {
                                                    try {
                                                        subscriberId = subscriberId2;
                                                        if (1 != entry.set) {
                                                            wifidataflow = wifidataflow3;
                                                        }
                                                    } catch (Exception e10) {
                                                        e = e10;
                                                        Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                                                        return str;
                                                    }
                                                } else {
                                                    subscriberId = subscriberId2;
                                                }
                                                wifidataflow = wifidataflow3;
                                            } catch (Exception e11) {
                                                e = e11;
                                                Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                                                return str;
                                            }
                                            try {
                                                dataflow = dataflow3 + entry.rxBytes + entry.txBytes;
                                                Log.d("DeviceStateManagerImpl", "Dataflow" + dataflow);
                                                i3++;
                                                dataflow3 = dataflow;
                                                subscriberId2 = subscriberId;
                                                wifidataflow3 = wifidataflow;
                                            } catch (Exception e12) {
                                                e = e12;
                                                Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                                                return str;
                                            }
                                        } else {
                                            subscriberId = subscriberId2;
                                            wifidataflow = wifidataflow3;
                                        }
                                        dataflow = dataflow3;
                                    } catch (Exception e13) {
                                        e = e13;
                                        Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                                        return str;
                                    }
                                    try {
                                        Log.d("DeviceStateManagerImpl", "Dataflow" + dataflow);
                                        i3++;
                                        dataflow3 = dataflow;
                                        subscriberId2 = subscriberId;
                                        wifidataflow3 = wifidataflow;
                                    } catch (Exception e14) {
                                        e = e14;
                                        Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                                        return str;
                                    }
                                }
                                wifidataflow = wifidataflow3;
                                dataflow2 = dataflow3;
                            } else {
                                wifidataflow = wifidataflow3;
                            }
                        } catch (Exception e15) {
                            e = e15;
                            Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                            return str;
                        }
                        try {
                            str[0] = dataflow2;
                        } catch (Exception e16) {
                            e = e16;
                            Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                            return str;
                        }
                        return str;
                    }
                } catch (Exception e17) {
                    e = e17;
                    Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                    return str;
                }
            } catch (Exception e18) {
                e = e18;
                Log.e("DeviceStateManagerImpl", "getdata fail!", e);
                return str;
            }
        } catch (Exception e19) {
            e = e19;
            Log.e("DeviceStateManagerImpl", "getdata fail!", e);
            return str;
        }
    }
}
