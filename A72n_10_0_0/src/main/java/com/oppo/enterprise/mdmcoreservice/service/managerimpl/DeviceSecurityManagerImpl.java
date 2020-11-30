package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IOppoCustomizeService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager;
import com.oppo.enterprise.mdmcoreservice.aidl.OppoSimContactEntry;
import com.oppo.enterprise.mdmcoreservice.certificate.OppoCertificateVerifier;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceConnectivityManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceRestrictionManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceSettingsManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceVpnManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import com.oppo.enterprise.mdmcoreservice.utils.permission.PermissionUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceSecurityManagerImpl extends IDeviceSecurityManager.Stub {
    private static boolean DEBUG = false;
    private static boolean NOCONTROL = true;
    DeviceConnectivityManager deviceConnectivityManager;
    DeviceRestrictionManager deviceRestrictionManager;
    DeviceSettingsManager deviceSettingsManager;
    DeviceVpnManager deviceVpnManager;
    private boolean mAccessEnabled = false;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private DevicePolicyManager mDpm;
    private PackageManager mPm;
    private IOppoCustomizeService mService;
    private ServiceHandler mServiceHandler;

    public DeviceSecurityManagerImpl(Context context) {
        this.mContext = context;
        if (this.mContext != null) {
            this.mPm = this.mContext.getPackageManager();
            this.mDpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
            this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
            this.deviceRestrictionManager = DeviceRestrictionManager.getInstance(this.mContext);
            this.deviceSettingsManager = DeviceSettingsManager.getInstance(this.mContext);
            this.deviceConnectivityManager = DeviceConnectivityManager.getInstance(this.mContext);
            this.deviceVpnManager = DeviceVpnManager.getInstance(this.mContext);
        }
        HandlerThread thread = new HandlerThread("CustomizeControler");
        thread.start();
        if (thread.getLooper() != null) {
            this.mServiceHandler = new ServiceHandler(thread.getLooper());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IOppoCustomizeService getCustomizeService() {
        if (this.mService != null) {
            return this.mService;
        }
        this.mService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
        return this.mService;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void setEmmAdmin(ComponentName cn, boolean enable) {
        PermissionManager.getInstance().checkCritialPermission("261");
        boolean isInstalled = true;
        String packageName = null;
        if (cn != null) {
            packageName = cn.getPackageName();
        }
        try {
            this.mContext.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("DeviceSecurityManager", "set Device Admin failed, not install!");
            isInstalled = false;
        }
        if (isInstalled) {
            long identity = Binder.clearCallingIdentity();
            if (enable) {
                try {
                    if (!this.mDpm.isAdminActive(cn)) {
                        Log.d("DeviceSecurityManager", "set special Device Admin active");
                        this.mDpm.setActiveAdmin(cn, false);
                        try {
                            ApplicationInfo ai = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, -2);
                            if (ai == null) {
                                Binder.restoreCallingIdentity(identity);
                                return;
                            }
                            this.mAppOpsManager.setMode(43, ai.uid, packageName, 0);
                        } catch (PackageManager.NameNotFoundException e2) {
                        }
                    } else {
                        Log.d("DeviceSecurityManager", "current package is active!");
                    }
                } catch (Exception e3) {
                    Log.d("DeviceSecurityManager", "set emm admin failed!");
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } else if (this.mDpm.isAdminActive(cn)) {
                Log.d("DeviceSecurityManager", "remove Device Admin");
                this.mDpm.removeActiveAdmin(cn);
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public List<ComponentName> getEmmAdmin(ComponentName admin) {
        PermissionManager.getInstance().checkCritialPermission("2011");
        List<ComponentName> oppoActiveAdminList = new ArrayList<>();
        long identity = Binder.clearCallingIdentity();
        try {
            oppoActiveAdminList = this.mDpm.getActiveAdmins();
        } catch (Exception e) {
            Log.d("DeviceSecurityManager", "getEmmAdmin: fail", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return oppoActiveAdminList;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public boolean setDeviceOwner(ComponentName admin) {
        PermissionManager.getInstance().checkCritialPermission("104");
        try {
            setEmmAdmin(admin, true);
            return this.mDpm.setDeviceOwner(admin);
        } catch (Exception ex) {
            Log.d("DeviceSecurityManager", "set Device Owner failed!", ex);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public ComponentName getDeviceOwner() {
        PermissionManager.getInstance().checkCritialPermission("2002");
        ComponentName result = null;
        long identity = Binder.clearCallingIdentity();
        try {
            result = this.mDpm.getDeviceOwnerComponentOnCallingUser();
        } catch (Exception ex) {
            Log.d("DeviceSecurityManager", "get DeviceOwner failed!", ex);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void clearDeviceOwner(String packageName) {
        PermissionManager.getInstance().checkCritialPermission("2001");
        long identity = Binder.clearCallingIdentity();
        try {
            this.mDpm.clearDeviceOwnerApp(packageName);
        } catch (Exception ex) {
            Log.d("DeviceSecurityManager", "clear DeviceOwner failed!", ex);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0094, code lost:
        if (r1 == null) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0096, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b7, code lost:
        if (0 == 0) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00ba, code lost:
        return r0;
     */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public List<OppoSimContactEntry> getSimContacts(ComponentName componentName, int slotId) {
        PermissionManager.getInstance().checkCritialPermission("342");
        List<OppoSimContactEntry> oppoSimContactEntryList = new ArrayList<>();
        Cursor cursor = null;
        try {
            Uri uri = getCustomizeService().getSimContactsUri(slotId);
            if (uri == null) {
                if (0 != 0) {
                    cursor.close();
                }
                return oppoSimContactEntryList;
            }
            cursor = this.mContext.getContentResolver().query(uri, new String[]{"_id", "name", "number", "emails", "additionalNumber"}, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    OppoSimContactEntry oppoSimContactEntry = new OppoSimContactEntry();
                    ContentValues contentValues = new ContentValues();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        int columnValueType = checkSimContactsColumnValueType(cursor.getColumnName(i));
                        if (columnValueType == 1) {
                            contentValues.put(cursor.getColumnName(i), Integer.valueOf(cursor.getInt(i)));
                        } else if (columnValueType == 2) {
                            contentValues.put(cursor.getColumnName(i), cursor.getString(i));
                        }
                    }
                    oppoSimContactEntry.setContentValues(contentValues);
                    oppoSimContactEntryList.add(oppoSimContactEntry);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DeviceSecurityManager", "getSimContacts msg: " + e.getMessage());
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    public int checkSimContactsColumnValueType(String columnKey) {
        if (columnKey == null) {
            return -1;
        }
        if (TextUtils.equals(columnKey, "_id")) {
            return 1;
        }
        if (TextUtils.equals(columnKey, "name") || TextUtils.equals(columnKey, "number") || TextUtils.equals(columnKey, "emails") || TextUtils.equals(columnKey, "emails") || TextUtils.equals(columnKey, "additionalNumber")) {
            return 2;
        }
        return -1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void startRecordPolicy(String dirPath) {
        PermissionManager.getInstance().checkCritialPermission("345");
        Log.d("DeviceSecurityManager", "startRecordPolicy dirPath = " + dirPath);
        Intent intent = new Intent("oppo.action.third.record.policy");
        if (TextUtils.isEmpty(dirPath)) {
            intent.putExtra("policy_status", 0);
        } else {
            intent.putExtra("policy_status", 1);
        }
        intent.putExtra("record_dir_path", dirPath);
        this.mContext.sendBroadcast(intent);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void stopRecordPolicy() {
        PermissionManager.getInstance().checkCritialPermission("346");
        Log.d("DeviceSecurityManager", "stopRecordPolicy");
        Intent intent = new Intent("oppo.action.third.record.policy");
        intent.putExtra("policy_status", 0);
        this.mContext.sendBroadcast(intent);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void startRecord(String filePath) {
        int lastSeparator;
        PermissionManager.getInstance().checkCritialPermission("347");
        Log.d("DeviceSecurityManager", "startRecord filePath = " + filePath);
        if (!TextUtils.isEmpty(filePath) && (lastSeparator = filePath.lastIndexOf(File.separator)) != -1) {
            String fileDirPath = filePath.substring(0, lastSeparator);
            Log.d("DeviceSecurityManager", "startRecord fileDirPath = " + fileDirPath);
            File filedDir = new File(fileDirPath);
            if (!filedDir.exists()) {
                try {
                    filedDir.mkdir();
                } catch (Exception e) {
                    Log.e("DeviceSecurityManager", "startRecord", e);
                }
                if (!filedDir.exists()) {
                    Log.d("DeviceSecurityManager", "filedDir is not exist!");
                    return;
                }
            }
            try {
                Intent intent = new Intent("oppo.action.third.record.start");
                intent.setPackage("com.android.incallui");
                intent.putExtra("record_path", filePath);
                this.mContext.startService(intent);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void stopRecord() {
        PermissionManager.getInstance().checkCritialPermission("348");
        try {
            Intent intent = new Intent("oppo.action.third.record.stop");
            intent.setPackage("com.android.incallui");
            this.mContext.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void grantAllRuntimePermission(ComponentName admin, String packageName) {
        PermissionManager.getInstance().checkCritialPermission("53");
        long identity = Binder.clearCallingIdentity();
        try {
            PermissionUtils.grantOrForbidAllRuntimePermissions(this.mContext, packageName, 0);
        } catch (Exception e) {
            Log.e("DeviceSecurityManager", "grantOrForbidAllRuntimePermissions fail", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public boolean setDeviceLocked(ComponentName cn) {
        try {
            PermissionManager.getInstance().checkCritialPermission("441");
            Log.d("DeviceSecurityManager", "setDeviceLocked");
            if (Build.VERSION.SDK_INT <= 28) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), "lock_dead_state", 1);
            }
            Settings.Secure.putInt(this.mContext.getContentResolver(), "lock_dead_state_type", 1);
            this.mContext.sendBroadcast(new Intent("oppo.intent.action.lock_lockdead_keyguard"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public boolean setDeviceUnLocked(ComponentName cn) {
        try {
            PermissionManager.getInstance().checkCritialPermission("442");
            Log.d("DeviceSecurityManager", "setDeviceUnLocked");
            if (Build.VERSION.SDK_INT <= 28) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), "lock_dead_state", 0);
            }
            Settings.Secure.putInt(this.mContext.getContentResolver(), "lock_dead_state_type", 0);
            this.mContext.sendBroadcast(new Intent("oppo.intent.action.unlock_lockdead_keyguard"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public List<String> getDeviceInfo(ComponentName admin) {
        PermissionManager.getInstance().checkCritialPermission("94");
        Log.d("DeviceSecurityManager", "getDeviceInfo()");
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        List<String> devInfo = new ArrayList<>();
        devInfo.add(tm.getImei(0));
        devInfo.add(tm.getImei(1));
        devInfo.add(getRamMemorySize());
        devInfo.add(getInternalTotalSize());
        devInfo.add(getScreenInfo());
        devInfo.add(Build.BRAND);
        devInfo.add(Build.MODEL);
        devInfo.add(SystemProperties.get("ro.build.kernel.id", "none"));
        devInfo.add(SystemProperties.get("sys.build.display.id", "none"));
        devInfo.add("none");
        devInfo.add(Build.VERSION.SECURITY_PATCH);
        int[] subIdPrimary = SubscriptionManager.getSubId(0);
        int[] subIdSecondary = SubscriptionManager.getSubId(1);
        devInfo.add((subIdPrimary == null || subIdPrimary.length <= 0) ? "none" : tm.getSimSerialNumber(subIdPrimary[0]));
        devInfo.add((subIdSecondary == null || subIdSecondary.length <= 0) ? "none" : tm.getSimSerialNumber(subIdSecondary[0]));
        devInfo.add((subIdPrimary == null || subIdPrimary.length <= 0) ? "none" : tm.getSubscriberId(subIdPrimary[0]));
        devInfo.add((subIdSecondary == null || subIdSecondary.length <= 0) ? "none" : tm.getSubscriberId(subIdSecondary[0]));
        devInfo.add(getCpuName());
        devInfo.add(SystemProperties.get("ro.build.network_type", "none"));
        devInfo.add(SystemProperties.get("ro.build.chipset_wifi", "none"));
        devInfo.add(SystemProperties.get("ro.build.chipset_bt", "none"));
        devInfo.add(SystemProperties.get("ro.build.chipset_nfc", "none"));
        devInfo.add(SystemProperties.get("ro.build.chipset_gps", "none"));
        return devInfo;
    }

    private String getRamMemorySize() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) this.mContext.getSystemService("activity")).getMemoryInfo(memInfo);
        int mul = (int) (memInfo.totalMem / 1073741824);
        if (((int) (memInfo.totalMem % 1073741824)) > 0) {
            mul++;
        }
        return String.valueOf(mul) + "GB";
    }

    private String getInternalTotalSize() {
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            int mul = (int) ((stat.getBlockCountLong() * stat.getBlockSizeLong()) / 1073741824);
            if (mul < 32) {
                return "32GB";
            }
            if (mul < 64) {
                return "64GB";
            }
            if (mul < 128) {
                return "128GB";
            }
            if (mul < 256) {
                return "256GB";
            }
            return "64GB";
        } catch (Exception e) {
            Log.e("DeviceSecurityManager", "getInternalTotalSize exception occur!", e);
            return "64GB";
        }
    }

    private String getScreenInfo() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(dm);
        return String.valueOf(dm.heightPixels) + "*" + String.valueOf(dm.widthPixels);
    }

    private String getCpuNameByProp() {
        return SystemProperties.get("ro.product.cpuinfo");
    }

    private String getCpuName() {
        String cpuName = getCpuNameByProp();
        if (!TextUtils.isEmpty(cpuName)) {
            return cpuName;
        }
        LinkedList<String> regList = new LinkedList<>();
        regList.add("MSM\\s*\\w+");
        regList.add("SDM\\w*\\s*\\w+");
        regList.add("SM\\s*\\w+");
        regList.add("MT\\s*\\w+");
        regList.add("TRINKET");
        FileReader fr = null;
        BufferedReader br = null;
        try {
            FileReader fr2 = new FileReader("/proc/cpuinfo");
            BufferedReader br2 = new BufferedReader(fr2);
            while (true) {
                String text = br2.readLine();
                if (text == null) {
                    try {
                        break;
                    } catch (IOException e) {
                    }
                } else if (text.contains("Hardware") && (text.contains("MSM") || text.contains("MT") || text.contains("SDM") || text.contains("SM") || text.contains("TRINKET"))) {
                    Iterator<String> it = regList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Matcher mat = Pattern.compile(it.next()).matcher(text);
                        if (mat.find()) {
                            cpuName = mat.group(0).trim();
                            break;
                        }
                    }
                }
            }
            fr2.close();
            br2.close();
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (0 != 0) {
                fr.close();
            }
            if (0 != 0) {
                br.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (0 != 0) {
                fr.close();
            }
            if (0 != 0) {
                br.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fr.close();
                } catch (IOException e4) {
                    throw th;
                }
            }
            if (0 != 0) {
                br.close();
            }
            throw th;
        }
        if (TextUtils.isEmpty(cpuName)) {
            return "MT6765";
        }
        return cpuName;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public String[] listIccid(ComponentName componentName) {
        PermissionManager.getInstance().checkCritialPermission("84");
        Log.d("DeviceSecurityManager", "listIccid");
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            return null;
        }
        int phoneCount = tm.getPhoneCount();
        String[] iccidList = new String[phoneCount];
        for (int phoneId = 0; phoneId < phoneCount; phoneId++) {
            String iccId = null;
            int[] subId = SubscriptionManager.getSubId(phoneId);
            if (subId != null && subId.length > 0) {
                SubscriptionInfo subInfo = SubscriptionManager.from(this.mContext).getActiveSubscriptionInfo(subId[0]);
                Log.d("DeviceSecurityManager", "subInfo : " + subInfo);
                if (subInfo != null) {
                    iccId = subInfo.getIccId();
                }
                iccidList[phoneId] = iccId;
            }
        }
        return iccidList;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public String[] listImei(ComponentName componentName) {
        PermissionManager.getInstance().checkCritialPermission("85");
        Log.d("DeviceSecurityManager", "listImei");
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

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public String getPhoneNumber(ComponentName admin, int slot) {
        PermissionManager.getInstance().checkCritialPermission("352");
        String phoneNum = null;
        try {
            phoneNum = getCustomizeService().getPhoneNumber(getSubId(slot));
        } catch (Exception e) {
            Log.d("DeviceSecurityManager", "getPhoneNumber:err", e);
        }
        Log.d("DeviceSecurityManager", "getPhoneNumber phoneNum = " + phoneNum);
        return phoneNum;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public String executeShellToSetIptables(ComponentName componentName, String commandline) {
        PermissionManager.getInstance().checkCritialPermission("443");
        Log.d("DeviceSecurityManager", "executeShellToSetIptables: commandline is " + commandline);
        String result = null;
        try {
            result = getCustomizeService().executeShellToSetIptables(commandline);
        } catch (Exception e) {
            Log.d("DeviceSecurityManager", "executeShellToSetIptables:err!");
            e.printStackTrace();
        }
        if (result == null) {
            return "null";
        }
        return result;
    }

    public static int getSubId(int slotId) {
        int vRetSubId;
        int[] subId = SubscriptionManager.getSubId(slotId);
        if (subId == null || subId.length <= 0) {
            vRetSubId = -1000;
        } else {
            vRetSubId = subId[0];
        }
        Log.d("DeviceSecurityManager", "SubId=" + vRetSubId);
        return vRetSubId;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public Bundle getMobilePerpheralSettings(ComponentName admin, String business, String setting) {
        PermissionManager.getInstance().checkCritialPermission("109");
        Bundle bundle = new Bundle();
        if (setting != null) {
            Log.d("DeviceSecurityManager", "getMobilePerpheralSettings " + business + "::" + setting);
            String upperCase = business.toUpperCase();
            char c = 65535;
            int hashCode = upperCase.hashCode();
            if (hashCode != -1733499378) {
                if (hashCode != 902263164) {
                    if (hashCode == 1731749696 && upperCase.equals("SECURITY")) {
                        c = 2;
                    }
                } else if (upperCase.equals("PERIPHERAL")) {
                    c = 1;
                }
            } else if (upperCase.equals("NETWORK")) {
                c = 0;
            }
            switch (c) {
                case 0:
                    getNetworkSettings(admin, setting, bundle);
                    break;
                case 1:
                    getPeripheralSettings(admin, setting, bundle);
                    break;
                case 2:
                    getSecuritySettings(admin, setting, bundle);
                    break;
                default:
                    Log.e("DeviceSecurityManager", "error we can't find the business : " + business);
                    break;
            }
        } else {
            Log.d("DeviceSecurityManager", "getMobilePerpheralSettings setting is null");
        }
        return bundle;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void setMobilePerpheralSettings(ComponentName admin, String business, Bundle settingbundle) {
        PermissionManager.getInstance().checkCritialPermission("108");
        if (settingbundle != null) {
            Log.d("DeviceSecurityManager", "setMobileSettings business :: " + business);
            String upperCase = business.toUpperCase();
            char c = 65535;
            int hashCode = upperCase.hashCode();
            if (hashCode != -1733499378) {
                if (hashCode != 902263164) {
                    if (hashCode == 1731749696 && upperCase.equals("SECURITY")) {
                        c = 2;
                    }
                } else if (upperCase.equals("PERIPHERAL")) {
                    c = 1;
                }
            } else if (upperCase.equals("NETWORK")) {
                c = 0;
            }
            switch (c) {
                case 0:
                    setNetworkSettings(admin, settingbundle);
                    return;
                case 1:
                    setPeripheralSettings(admin, settingbundle);
                    return;
                case 2:
                    setSecuritySettings(admin, settingbundle);
                    return;
                default:
                    Log.e("DeviceSecurityManager", "error we can't find the business : " + business);
                    return;
            }
        } else {
            Log.d("DeviceSecurityManager", "setMobilePerpheralSettings Bundle is null");
        }
    }

    private void setNetworkSettings(ComponentName conponentname, Bundle bundle) {
        int wifiValue = bundle.getInt("WIFI", -1);
        boolean vpnValue = bundle.getBoolean("VPN", NOCONTROL);
        boolean tetherValue = bundle.getBoolean("TETHER", NOCONTROL);
        int bluetoothValue = bundle.getInt("BLUETOOTH", -1);
        if (wifiValue == 0) {
            try {
                this.deviceConnectivityManager.setWlanPolicies(conponentname, 0);
                Log.d("DeviceSecurityManager", "WIFI after: " + String.valueOf(wifiValue));
            } catch (Exception ex) {
                Log.e("DeviceSecurityManager", "WIFI error :" + ex.getMessage());
            }
        } else if (wifiValue == 1) {
            this.deviceConnectivityManager.setWlanPolicies(conponentname, 2);
            Log.d("DeviceSecurityManager", "WIFI after: " + String.valueOf(wifiValue));
        } else if (wifiValue == 2) {
            this.deviceConnectivityManager.setWlanPolicies(conponentname, 3);
            Log.d("DeviceSecurityManager", "WIFI after: " + String.valueOf(wifiValue));
        } else if (wifiValue == 3) {
            this.deviceConnectivityManager.setWlanPolicies(conponentname, 4);
            Log.d("DeviceSecurityManager", "WIFI after: " + String.valueOf(wifiValue));
        } else if (wifiValue == 4) {
            this.deviceConnectivityManager.setWlanPolicies(conponentname, 5);
            Log.d("DeviceSecurityManager", "WIFI after: " + String.valueOf(wifiValue));
        }
        try {
            this.deviceVpnManager.setVpnDisabled(conponentname, !vpnValue);
            Log.d("DeviceSecurityManager", "VPN after: " + String.valueOf(vpnValue));
        } catch (Exception ex2) {
            Log.e("DeviceSecurityManager", "VPN error :" + ex2.getMessage());
        }
        try {
            this.deviceSettingsManager.setTetherEnable(tetherValue);
            Log.d("DeviceSecurityManager", "THETHER after: " + String.valueOf(tetherValue));
        } catch (Exception ex3) {
            Log.e("DeviceSecurityManager", "VPN error :" + ex3.getMessage());
        }
        if (bluetoothValue == 0) {
            try {
                this.deviceConnectivityManager.setBluetoothPolicies(conponentname, 0);
                Log.d("DeviceSecurityManager", "BLUETOOTH after: " + String.valueOf(bluetoothValue));
            } catch (Exception ex4) {
                Log.e("DeviceSecurityManager", "BLUETOOTH error :" + ex4.getMessage());
            }
        } else if (bluetoothValue == 1) {
            this.deviceConnectivityManager.setBluetoothPolicies(conponentname, 2);
            Log.d("DeviceSecurityManager", "BLUETOOTH after: " + String.valueOf(bluetoothValue));
        } else if (bluetoothValue == 2) {
            this.deviceConnectivityManager.setBluetoothPolicies(conponentname, 4);
            Log.d("DeviceSecurityManager", "BLUETOOTH after: " + String.valueOf(bluetoothValue));
        } else if (bluetoothValue == 3) {
            this.deviceConnectivityManager.setBluetoothPolicies(conponentname, 5);
            Log.d("DeviceSecurityManager", "BLUETOOTH after: " + String.valueOf(bluetoothValue));
        } else if (bluetoothValue == 4) {
            this.deviceConnectivityManager.setBluetoothPolicies(conponentname, 3);
            Log.d("DeviceSecurityManager", "BLUETOOTH after: " + String.valueOf(bluetoothValue));
        }
    }

    private void setPeripheralSettings(ComponentName componentName, Bundle bundle) {
        int gpsValue = bundle.getInt("LOCATION", -1);
        int camera_Value = bundle.getInt("CAMERA", -1);
        int microphone_Value = bundle.getInt("MICROPHONE", -1);
        int screen_Value = bundle.getInt("SCREEN", -1);
        int sdcard_Value = bundle.getInt("SDCARD", -1);
        int usbtransfer_Value = bundle.getInt("USBTRANSFER", -1);
        int nfc_Value = bundle.getInt("NFC", -1);
        int otg_Value = bundle.getInt("OTG", -1);
        if (gpsValue == 0) {
            try {
                this.deviceRestrictionManager.setGpsPolicies(componentName, 0);
                Log.d("DeviceSecurityManager", "LOCATION after: " + String.valueOf(gpsValue));
            } catch (Exception ex) {
                Log.e("DeviceSecurityManager", "LOCATION error :" + ex.getMessage());
            }
        } else if (gpsValue == 1) {
            this.deviceRestrictionManager.setGpsPolicies(componentName, 2);
            Log.d("DeviceSecurityManager", "LOCATION after: " + String.valueOf(gpsValue));
        } else if (gpsValue == 2) {
            this.deviceRestrictionManager.setGpsPolicies(componentName, 3);
            Log.d("DeviceSecurityManager", "LOCATION after: " + String.valueOf(gpsValue));
        } else if (gpsValue == 3) {
            this.deviceRestrictionManager.setGpsPolicies(componentName, 4);
            Log.d("DeviceSecurityManager", "LOCATION after: " + String.valueOf(gpsValue));
        } else if (gpsValue == 4) {
            this.deviceRestrictionManager.setGpsPolicies(componentName, 1);
            Log.d("DeviceSecurityManager", "LOCATION after: " + String.valueOf(gpsValue));
        }
        if (camera_Value == 0 || camera_Value == 1) {
            try {
                this.deviceRestrictionManager.setCameraPolicies(camera_Value);
                Log.d("DeviceSecurityManager", "CAMERA after: " + String.valueOf(camera_Value));
            } catch (Exception ex2) {
                Log.e("DeviceSecurityManager", "CAMERA error :" + ex2.getMessage());
            }
        }
        if (microphone_Value == 0 || microphone_Value == 1) {
            try {
                this.deviceRestrictionManager.setMicrophonePolicies(componentName, microphone_Value);
                Log.d("DeviceSecurityManager", "MICROPHONE after: " + String.valueOf(microphone_Value));
            } catch (Exception ex3) {
                Log.e("DeviceSecurityManager", "MICROPHONE error :" + ex3.getMessage());
            }
        }
        if (screen_Value != -1) {
            if (screen_Value == 0) {
                try {
                    this.deviceRestrictionManager.setScreenCaptureDisabled(componentName, true);
                } catch (Exception ex4) {
                    Log.e("DeviceSecurityManager", "SCREEN error :" + ex4.getMessage());
                }
            } else if (screen_Value == 1) {
                this.deviceRestrictionManager.setScreenCaptureDisabled(componentName, false);
            }
            Log.d("DeviceSecurityManager", "SCREEN after: " + String.valueOf(screen_Value));
        }
        if (sdcard_Value != -1) {
            if (sdcard_Value == 0) {
                try {
                    this.deviceRestrictionManager.setExternalStorageDisabled(componentName, true);
                } catch (Exception ex5) {
                    Log.e("DeviceSecurityManager", "SDCARD error :" + ex5.getMessage());
                }
            } else if (sdcard_Value == 1) {
                this.deviceRestrictionManager.setExternalStorageDisabled(componentName, false);
            }
            Log.d("DeviceSecurityManager", "SDCARD after: " + String.valueOf(sdcard_Value));
        }
        if (usbtransfer_Value != -1) {
            if (usbtransfer_Value == 0) {
                try {
                    this.deviceRestrictionManager.setUSBDataDisabled(componentName, true);
                } catch (Exception ex6) {
                    Log.e("DeviceSecurityManager", "USBTRANSFER error :" + ex6.getMessage());
                }
            } else if (usbtransfer_Value == 1) {
                this.deviceRestrictionManager.setUSBDataDisabled(componentName, false);
            }
            Log.d("DeviceSecurityManager", "USBTRANSFER after: " + String.valueOf(usbtransfer_Value));
        }
        if (nfc_Value != -1) {
            if (nfc_Value == 0) {
                try {
                    this.deviceConnectivityManager.setNfcPolicies(componentName, nfc_Value);
                } catch (Exception ex7) {
                    Log.e("DeviceSecurityManager", "NFC error :" + ex7.getMessage());
                }
            } else if (nfc_Value == 1 || nfc_Value == 2) {
                this.deviceConnectivityManager.setNfcPolicies(componentName, 3 - nfc_Value);
            }
            Log.d("DeviceSecurityManager", "NFC after: " + String.valueOf(nfc_Value));
        }
        if (otg_Value != -1) {
            if (otg_Value == 0) {
                try {
                    this.deviceRestrictionManager.setUSBOtgDisabled(componentName, true);
                } catch (Exception ex8) {
                    Log.e("DeviceSecurityManager", "OTG error :" + ex8.getMessage());
                    return;
                }
            } else if (otg_Value == 1) {
                this.deviceRestrictionManager.setUSBOtgDisabled(componentName, false);
            }
            Log.d("DeviceSecurityManager", "OTG after: " + String.valueOf(otg_Value));
        }
    }

    private void setSecuritySettings(ComponentName componentName, Bundle bundle) {
        boolean fingerprintValue = bundle.getBoolean("FINGERPRINT", NOCONTROL);
        boolean usbdebugValue = bundle.getBoolean("USBDEBUG", NOCONTROL);
        boolean factroryresetValue = bundle.getBoolean("FACTORYRESET", NOCONTROL);
        boolean restoreValue = bundle.getBoolean("RESTORE", NOCONTROL);
        boolean timeValue = bundle.getBoolean("TIME", NOCONTROL);
        int flight_Value = bundle.getInt("FLIGHT", -1);
        try {
            this.deviceRestrictionManager.setUnlockByFingerprintDisabled(componentName, !fingerprintValue);
            Log.d("DeviceSecurityManager", "FINGERPRINT after: " + String.valueOf(fingerprintValue));
        } catch (Exception ex) {
            Log.e("DeviceSecurityManager", "FINGERPRINT error :" + ex.getMessage());
        }
        try {
            this.deviceRestrictionManager.setAdbDisabled(componentName, !usbdebugValue);
            Log.d("DeviceSecurityManager", "USERDEBUG after: " + String.valueOf(usbdebugValue));
        } catch (Exception ex2) {
            Log.e("DeviceSecurityManager", "USERDEBUG error :" + ex2.getMessage());
        }
        try {
            this.deviceSettingsManager.setRestoreFactoryDisabled(componentName, !factroryresetValue);
            Log.d("DeviceSecurityManager", "FACTORY after: " + String.valueOf(factroryresetValue));
        } catch (Exception ex3) {
            Log.e("DeviceSecurityManager", "FACTORY error :" + ex3.getMessage());
        }
        if (factroryresetValue) {
            try {
                this.deviceSettingsManager.setRestoreFactoryDisabled(componentName, !restoreValue);
                Log.d("DeviceSecurityManager", "RESTORE after: " + String.valueOf(restoreValue));
            } catch (Exception ex4) {
                Log.e("DeviceSecurityManager", "RESTORE error :" + ex4.getMessage());
            }
        }
        try {
            this.deviceSettingsManager.setTimeAndDateSetDisabled(componentName, !timeValue);
            Log.d("DeviceSecurityManager", "TIME after: " + String.valueOf(timeValue));
        } catch (Exception ex5) {
            Log.e("DeviceSecurityManager", "TIME error :" + ex5.getMessage());
        }
        if (flight_Value == 0) {
            try {
                this.deviceRestrictionManager.setAirplanePolices(componentName, 0);
                Log.d("DeviceSecurityManager", "FLIGHT after: " + String.valueOf(flight_Value));
            } catch (Exception ex6) {
                Log.e("DeviceSecurityManager", "FLIGHT error :" + ex6.getMessage());
            }
        } else if (flight_Value == 1) {
            this.deviceRestrictionManager.setAirplanePolices(componentName, 2);
            Log.d("DeviceSecurityManager", "FLIGHT after: " + String.valueOf(flight_Value));
        } else if (flight_Value == 2) {
            this.deviceRestrictionManager.setAirplanePolices(componentName, 3);
            Log.d("DeviceSecurityManager", "FLIGHT after: " + String.valueOf(flight_Value));
        } else if (flight_Value == 3) {
            this.deviceRestrictionManager.setAirplanePolices(componentName, 4);
            Log.d("DeviceSecurityManager", "FLIGHT after: " + String.valueOf(flight_Value));
        } else if (flight_Value == 4) {
            this.deviceRestrictionManager.setAirplanePolices(componentName, 1);
            Log.d("DeviceSecurityManager", "FLIGHT after: " + String.valueOf(flight_Value));
        }
    }

    private int getNetworkSettings(ComponentName conponentname, String setting, Bundle bundle) {
        int ret = -1;
        Log.d("DeviceSecurityManager", "getNetworkSettings: " + setting);
        if (setting.equalsIgnoreCase("WIFI")) {
            ret = this.deviceConnectivityManager.getWlanPolicies(conponentname);
            if (ret > 0) {
                ret--;
            }
            bundle.putInt(setting, ret);
            Log.d("DeviceSecurityManager", "wifi_ret after: " + String.valueOf(ret));
        } else if (setting.equalsIgnoreCase("VPN")) {
            boolean ret1 = !this.deviceVpnManager.isVpnDisabled(conponentname);
            bundle.putBoolean(setting, ret1);
            Log.d("DeviceSecurityManager", "vpn_ret1 after: " + String.valueOf(ret1));
        } else if (setting.equalsIgnoreCase("TETHER")) {
            boolean ret12 = this.deviceSettingsManager.getTetherEnable(this.mContext);
            bundle.putBoolean(setting, ret12);
            Log.d("DeviceSecurityManager", "tether_ret1 after: " + String.valueOf(ret12));
        } else if (setting.equalsIgnoreCase("BLUETOOTH")) {
            ret = this.deviceConnectivityManager.getBluetoothPolicies(conponentname);
            if (ret > 0) {
                if (ret == 2) {
                    ret--;
                } else if (ret == 3) {
                    ret++;
                } else if (ret == 4 || ret == 5) {
                    ret -= 2;
                }
            }
            bundle.putInt(setting, ret);
            Log.d("DeviceSecurityManager", "bluetooth_ret after: " + String.valueOf(ret));
        }
        return ret;
    }

    private int getPeripheralSettings(ComponentName conponentname, String setting, Bundle bundle) {
        int ret = -1;
        Log.d("DeviceSecurityManager", "getPeripheralSettings: " + setting);
        if (setting.equalsIgnoreCase("CAMERA")) {
            Log.d("DeviceSecurityManager", "camera_ret before: " + String.valueOf(-1));
            ret = this.deviceRestrictionManager.getCameraPolicies();
            Log.d("DeviceSecurityManager", "camera_ret after: " + String.valueOf(ret));
        } else if (setting.equalsIgnoreCase("MICROPHONE")) {
            ret = this.deviceRestrictionManager.getMicrophonePolicies(conponentname);
            Log.d("DeviceSecurityManager", "micophone_ret after: " + String.valueOf(ret));
        } else if (setting.equalsIgnoreCase("SCREEN")) {
            ret = !this.deviceRestrictionManager.isScreenCaptureDisabled(conponentname);
            Log.d("DeviceSecurityManager", "screen_ret after: " + String.valueOf(ret));
        } else if (setting.equalsIgnoreCase("SDCARD")) {
            ret = !this.deviceRestrictionManager.isExternalStorageDisabled(conponentname);
            Log.d("DeviceSecurityManager", "sdcard_ret after: " + String.valueOf(ret));
        } else if (setting.equalsIgnoreCase("USBTRANSFER")) {
            ret = !this.deviceRestrictionManager.isUSBDataDisabled(conponentname);
            Log.d("DeviceSecurityManager", "usbtransfer_ret after: " + String.valueOf(ret));
        } else if (setting.equalsIgnoreCase("NFC")) {
            ret = this.deviceConnectivityManager.getNfcPolicies(conponentname);
            if (ret > 0) {
                ret = 3 - ret;
            }
            Log.d("DeviceSecurityManager", "nfc_ret after: " + String.valueOf(ret));
        } else if (setting.equalsIgnoreCase("OTG")) {
            ret = !this.deviceRestrictionManager.isUSBOtgDisabled(conponentname);
            Log.d("DeviceSecurityManager", "otg_ret after: " + String.valueOf(ret));
        } else if (setting.equalsIgnoreCase("LOCATION")) {
            ret = this.deviceRestrictionManager.getGpsPolicies(conponentname);
            if (ret == 1) {
                ret = 4;
            } else if (ret == 2 || ret == 3 || ret == 4) {
                ret--;
            }
            Log.d("DeviceSecurityManager", "gps_ret1 after: " + String.valueOf(ret));
        }
        bundle.putInt(setting, ret);
        return ret;
    }

    private boolean getSecuritySettings(ComponentName conponentname, String setting, Bundle bundle) {
        if (setting.equalsIgnoreCase("FINGERPRINT")) {
            boolean ret1 = !this.deviceRestrictionManager.isUnlockByFingerprintDisabled(conponentname);
            Log.d("DeviceSecurityManager", "fingerprint_ret1 after: " + String.valueOf(ret1));
            bundle.putBoolean(setting, ret1);
            return ret1;
        } else if (setting.equalsIgnoreCase("USBDEBUG")) {
            boolean ret12 = !this.deviceRestrictionManager.isAdbDisabled(conponentname);
            Log.d("DeviceSecurityManager", "usbdebug_ret1 after: " + String.valueOf(ret12));
            bundle.putBoolean(setting, ret12);
            return ret12;
        } else if (setting.equalsIgnoreCase("FACTORYRESET")) {
            boolean ret13 = !this.deviceSettingsManager.isRestoreFactoryDisabled(conponentname);
            Log.d("DeviceSecurityManager", "factory_ret1 after: " + String.valueOf(ret13));
            bundle.putBoolean(setting, ret13);
            return ret13;
        } else if (setting.equalsIgnoreCase("RESTORE")) {
            boolean ret14 = !this.deviceSettingsManager.isRestoreFactoryDisabled(conponentname);
            Log.d("DeviceSecurityManager", "restore_ret1 after: " + String.valueOf(ret14));
            bundle.putBoolean(setting, ret14);
            return ret14;
        } else if (setting.equalsIgnoreCase("TIME")) {
            boolean ret15 = !this.deviceSettingsManager.isTimeAndDateSetDisabled(conponentname);
            Log.d("DeviceSecurityManager", "time_ret1 after: " + String.valueOf(ret15));
            bundle.putBoolean(setting, ret15);
            return ret15;
        } else if (!setting.equalsIgnoreCase("FLIGHT")) {
            return true;
        } else {
            int ret = this.deviceRestrictionManager.getAirplanePolices(conponentname);
            if (ret == 1) {
                ret = 4;
            } else if (ret == 2 || ret == 3 || ret == 4) {
                ret--;
            }
            Log.d("DeviceSecurityManager", "flight_ret after: " + String.valueOf(ret));
            bundle.putInt(setting, ret);
            return true;
        }
    }

    private static String propGetEnable(String prop, String defval, String type) {
        try {
            if (DEBUG) {
                Log.d("DeviceSecurityManager", "propGetEnable " + prop + ": " + defval);
            }
            return SystemProperties.get(prop, defval);
        } catch (Exception ex) {
            Log.e("DeviceSecurityManager", "getProp error :" + ex.getMessage());
            return defval;
        }
    }

    private static boolean propSetEnable(String prop, String defval) {
        try {
            Log.d("DeviceSecurityManager", "propSetEnable " + prop + ": " + defval);
            SystemProperties.set(prop, defval);
            return true;
        } catch (Exception ex) {
            Log.e("DeviceSecurityManager", "setProp error :" + ex.getMessage());
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public Bundle getMobileCommSettings(ComponentName admin, String business, String setting) {
        int value;
        PermissionManager.getInstance().checkCritialPermission("353");
        try {
            if (DEBUG) {
                Log.d("DeviceSecurityManager", "getMobileCommSettings " + business + ":" + setting);
            }
            if (!business.equals("")) {
                Bundle bundle = new Bundle();
                if (business.equalsIgnoreCase("CT-VOICE")) {
                    value = getCTVoice(setting);
                } else if (business.equalsIgnoreCase("CT-SMS")) {
                    value = getCTSMS(setting);
                } else if (business.equalsIgnoreCase("CT-DATA")) {
                    value = getCTData(setting);
                } else if (business.equalsIgnoreCase("NCT-VOICE")) {
                    value = getNCTVoice(setting);
                } else if (business.equalsIgnoreCase("NCT-SMS")) {
                    value = getNCTSMS(setting);
                } else if (business.equalsIgnoreCase("NCT-DATA")) {
                    value = getNCTData(setting);
                } else {
                    Log.d("DeviceSecurityManager", "we don't known the " + business);
                    return null;
                }
                if (value != -1) {
                    bundle.putInt(setting, value);
                    return bundle;
                }
                Log.d("DeviceSecurityManager", "we can't find the " + setting);
                return null;
            }
            Log.e("DeviceSecurityManager", "error! business is null");
            return null;
        } catch (SecurityException e) {
            Log.d("DeviceSecurityManager", "getMobileCommSettings,SecurityException");
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void setMobileCommSettings(ComponentName admin, String business, Bundle bundle) {
        PermissionManager.getInstance().checkCritialPermission("354");
        try {
            if (DEBUG) {
                Log.d("DeviceSecurityManager", "setMobileCommSettings " + business);
            }
            if (business.equals("") || bundle == null) {
                Log.e("DeviceSecurityManager", "error! business is null");
            } else if (business.equalsIgnoreCase("CT-VOICE")) {
                int value1 = bundle.getInt("CALLIN", -1);
                int value2 = bundle.getInt("CALLOUT", -1);
                if (value1 != -1) {
                    setCTVoice("CALLIN", value1);
                }
                if (value2 != -1) {
                    setCTVoice("CALLOUT", value2);
                }
            } else if (business.equalsIgnoreCase("CT-SMS")) {
                int value12 = bundle.getInt("RECEIVE", -1);
                int value22 = bundle.getInt("SEND", -1);
                if (value12 != -1) {
                    setCTSMS("RECEIVE", value12);
                }
                if (value22 != -1) {
                    setCTSMS("SEND", value22);
                }
            } else if (business.equalsIgnoreCase("CT-DATA")) {
                int value13 = bundle.getInt("BLOCK", -1);
                int value23 = bundle.getInt("CONNECT", -1);
                if (value13 != -1) {
                    setCTData("BLOCK", value13);
                }
                if (value23 != -1) {
                    setCTData("CONNECT", value23);
                }
            } else if (business.equalsIgnoreCase("NCT-VOICE")) {
                int value14 = bundle.getInt("BLOCK", -1);
                if (value14 != -1) {
                    setNCTVoice("BLOCK", value14);
                }
            } else if (business.equalsIgnoreCase("NCT-SMS")) {
                int value15 = bundle.getInt("BLOCK", -1);
                if (value15 != -1) {
                    setNCTSMS("BLOCK", value15);
                }
            } else if (business.equalsIgnoreCase("NCT-DATA")) {
                int value16 = bundle.getInt("BLOCK", -1);
                if (value16 != -1) {
                    setNCTData("BLOCK", value16);
                }
            } else {
                Log.d("DeviceSecurityManager", "we don't known the " + business);
            }
        } catch (SecurityException e) {
            Log.d("DeviceSecurityManager", "setMobileCommSettings,SecurityException");
        }
    }

    private void setCTData(String key, int value) {
        if (key != null) {
            if (key.equals("BLOCK")) {
                setCTEnable("db", value);
            } else if (key.equals("CONNECT")) {
                setCTEnable("dc", value);
            } else {
                return;
            }
            if (this.mServiceHandler != null) {
                if (getCTData("BLOCK") == 0 || (getCTData("BLOCK") != 0 && getCTData("CONNECT") == 0)) {
                    Message msg = Message.obtain();
                    msg.what = 2;
                    this.mServiceHandler.sendMessage(msg);
                    return;
                }
                Message msg2 = Message.obtain();
                msg2.what = 1;
                this.mServiceHandler.sendMessage(msg2);
            }
        }
    }

    private int getCTData(String setting) {
        if (setting == null) {
            return -1;
        }
        if (setting.equals("BLOCK")) {
            return Integer.parseInt(getCTEnable("db", "-1"));
        }
        if (setting.equals("CONNECT")) {
            return Integer.parseInt(getCTEnable("dc", "-1"));
        }
        return -1;
    }

    private void setNCTData(String key, int value) {
        if (key != null) {
            if (DEBUG) {
                Log.d("DeviceSecurityManager", "setNCTData " + key + ": " + value);
            }
            if (key.equals("BLOCK")) {
                setNONCTEnable("db", value);
                if (getNCTData("BLOCK") == 0) {
                    Message msg = Message.obtain();
                    msg.what = 2;
                    this.mServiceHandler.sendMessage(msg);
                    return;
                }
                Message msg2 = Message.obtain();
                msg2.what = 1;
                this.mServiceHandler.sendMessage(msg2);
            }
        }
    }

    private int getNCTData(String setting) {
        if (setting != null && setting.equals("BLOCK")) {
            return Integer.parseInt(getNONCTEnable("db", "-1"));
        }
        return -1;
    }

    private void setCTVoice(String key, int value) {
        if (key != null) {
            if (DEBUG) {
                Log.d("DeviceSecurityManager", "setCTVoice " + key + ": " + value);
            }
            if (key.equals("CALLIN")) {
                setCTEnable("vi", value);
            } else if (key.equals("CALLOUT")) {
                setCTEnable("vo", value);
            }
        }
    }

    private int getCTVoice(String setting) {
        if (setting == null) {
            return -1;
        }
        if (DEBUG) {
            Log.d("DeviceSecurityManager", "getCTVoice " + setting);
        }
        if (setting.equals("CALLIN")) {
            return Integer.parseInt(getCTEnable("vi", "-1"));
        }
        if (setting.equals("CALLOUT")) {
            return Integer.parseInt(getCTEnable("vo", "-1"));
        }
        return -1;
    }

    private void setNCTVoice(String key, int value) {
        if (key != null) {
            if (DEBUG) {
                Log.d("DeviceSecurityManager", "setNCTVoice " + key + " : " + value);
            }
            if (key.equals("BLOCK")) {
                setNONCTEnable("vb", value);
            }
        }
    }

    private int getNCTVoice(String setting) {
        if (setting == null) {
            return -1;
        }
        if (DEBUG) {
            Log.d("DeviceSecurityManager", "getNCTVoice " + setting);
        }
        if (setting.equals("BLOCK")) {
            return Integer.parseInt(getNONCTEnable("vb", "-1"));
        }
        return -1;
    }

    private void setCTSMS(String key, int value) {
        if (key != null) {
            if (key.equals("RECEIVE")) {
                setCTEnable("sr", value);
            } else if (key.equals("SEND")) {
                setCTEnable("ss", value);
            }
        }
    }

    private int getCTSMS(String setting) {
        if (setting == null) {
            return -1;
        }
        if (setting.equals("RECEIVE")) {
            return Integer.parseInt(getCTEnable("sr", "-1"));
        }
        if (setting.equals("SEND")) {
            return Integer.parseInt(getCTEnable("ss", "-1"));
        }
        return -1;
    }

    private void setNCTSMS(String key, int value) {
        if (key != null && key.equals("BLOCK")) {
            setNONCTEnable("sb", value);
        }
    }

    private int getNCTSMS(String setting) {
        if (setting != null && setting.equals("BLOCK")) {
            return Integer.parseInt(getNONCTEnable("sb", "-1"));
        }
        return -1;
    }

    private static void setCTEnable(String key, int value) {
        propSetEnable("persist.sys.oem_ct_" + key, "" + value);
    }

    private static void setNONCTEnable(String key, int value) {
        propSetEnable("persist.sys.oem_nct_" + key, "" + value);
    }

    private static String getCTEnable(String key, String defval) {
        return propGetEnable("persist.sys.oem_ct_" + key, defval, null);
    }

    private static String getNONCTEnable(String key, String defval) {
        return propGetEnable("persist.sys.oem_nct_" + key, defval, null);
    }

    /* access modifiers changed from: private */
    public final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                switch (msg.what) {
                    case 1:
                        TelephonyManager.getDefault().setDataEnabled(true);
                        return;
                    case 2:
                        TelephonyManager.getDefault().setDataEnabled(false);
                        return;
                    case 3:
                        try {
                            DeviceSecurityManagerImpl.this.getCustomizeService().activateSubId(DeviceSecurityManagerImpl.getSubId(1));
                            return;
                        } catch (Exception e) {
                            Log.d("DeviceSecurityManager", "activateSubId:err!");
                            return;
                        }
                    case 4:
                        try {
                            DeviceSecurityManagerImpl.this.getCustomizeService().deactivateSubId(DeviceSecurityManagerImpl.getSubId(1));
                            return;
                        } catch (Exception e2) {
                            Log.d("DeviceSecurityManager", "deactivateSubId:err!");
                            return;
                        }
                    default:
                        Log.w("DeviceSecurityManager", "what=" + msg.what);
                        return;
                }
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public Bitmap captureScreen(ComponentName componentName) throws RemoteException {
        PermissionManager.getInstance().checkCritialPermission("2101");
        IOppoCustomizeService customService = getCustomizeService();
        if (customService == null) {
            Log.d("DeviceSecurityManager", "OppoCustomizeService init failed!");
            return null;
        }
        try {
            return customService.captureFullScreen();
        } catch (Throwable th) {
            Log.d("DeviceSecurityManager", "capture Screen failed!");
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public boolean setTestServerState(boolean enable) {
        PermissionManager.getInstance().checkCritialPermission("2111");
        propSetEnable("persist.sys.testenv.enable", String.valueOf(enable));
        OppoCertificateVerifier.getInstance(this.mContext).setTestServerState(enable);
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public boolean getTestServerState() {
        PermissionManager.getInstance().checkCritialPermission("2112");
        SystemProperties.getBoolean("persist.sys.testenv.enable", false);
        return OppoCertificateVerifier.getInstance(this.mContext).getTestServerState();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSecurityManager
    public void backupAppData(String src, String packageName, String dest, int requestId) {
        PermissionManager.getInstance().checkCritialPermission("2121");
        try {
            Log.d("DeviceSecurityManager", "backupAppData:src-" + src + " packageName-" + packageName + " requestId-" + requestId);
            getCustomizeService().backupAppData(src, packageName, dest, requestId);
        } catch (Exception e) {
            Log.d("DeviceSecurityManager", "backupAppData: " + e);
        }
    }
}
