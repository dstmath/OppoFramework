package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IOppoCustomizeService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceApplicationManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceSettingsManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceStateManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import com.oppo.enterprise.mdmcoreservice.utils.defaultapp.apptype.Browser;
import com.oppo.enterprise.mdmcoreservice.utils.defaultapp.apptype.Dialer;
import com.oppo.enterprise.mdmcoreservice.utils.defaultapp.apptype.Message;
import com.oppo.enterprise.mdmcoreservice.utils.permission.PermissionConstants;
import com.oppo.enterprise.mdmcoreservice.utils.permission.PermissionUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DevicePackageManagerImpl extends IDevicePackageManager.Stub {
    private static List<PermissionBean> sPERMISSION_LIST = new ArrayList();
    private ActivityManager mActivityManager;
    private Context mContext;
    private InstallBroadcastReceiver mInstallBroadcastReceiver = null;
    private PackageManager mPackageManager;
    private IOppoCustomizeService mService;

    public DevicePackageManagerImpl(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
    }

    private IOppoCustomizeService getCustomizeService() {
        if (this.mService != null) {
            return this.mService;
        }
        this.mService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
        return this.mService;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00b3, code lost:
        if (r7 == null) goto L_0x00ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e7, code lost:
        if (0 != 0) goto L_0x00e9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e9, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ec, code lost:
        android.os.Binder.restoreCallingIdentity(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0107  */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void installPackage(ComponentName admin, String packagePath, int flags) {
        PermissionManager.getInstance().checkPermission();
        if (this.mPackageManager != null) {
            PackageInfo mPi = this.mPackageManager.getPackageArchiveInfo(packagePath, 1);
            if (mPi == null) {
                Log.e("DevicePackageManagerImpl", "The package could not be parsed for the given path :" + packagePath);
                return;
            }
            long identity = Binder.clearCallingIdentity();
            PackageInstaller.Session mSession = null;
            InputStream in = null;
            OutputStream out = null;
            try {
                String packageName = mPi.packageName;
                PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(1);
                params.setAppPackageName(packageName);
                params.installFlags |= flags;
                int sessionId = this.mPackageManager.getPackageInstaller().createSession(params);
                mSession = this.mPackageManager.getPackageInstaller().openSession(sessionId);
                File file = new File(packagePath);
                InputStream in2 = new FileInputStream(file);
                OutputStream out2 = mSession.openWrite("SilentPackageInstaller", 0, file.length());
                byte[] buffer = new byte[65536];
                while (true) {
                    int c = in2.read(buffer);
                    if (c == -1) {
                        break;
                    }
                    out2.write(buffer, 0, c);
                    packageName = packageName;
                }
                mSession.fsync(out2);
                out2.close();
                IntentSender intentSender = getCommitCallback(packagePath, sessionId);
                if (intentSender != null) {
                    mSession.commit(intentSender);
                }
                mSession.close();
                in2.close();
                try {
                    in2.close();
                    if (out2 != null) {
                        out2.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e2) {
                Log.d("DevicePackageManagerImpl", "installPackage" + e2);
                e2.printStackTrace();
                if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                if (0 != 0) {
                    out.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                        if (0 != 0) {
                            mSession.close();
                        }
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                }
                if (0 != 0) {
                    out.close();
                }
                if (0 != 0) {
                }
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    private IntentSender getCommitCallback(String packagePath, int sessionId) {
        String action = "com.android.cts.deviceowner.INTENT_PACKAGE_INSTALL_COMMIT." + packagePath.hashCode();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        if (this.mInstallBroadcastReceiver == null) {
            this.mInstallBroadcastReceiver = new InstallBroadcastReceiver();
        }
        this.mContext.registerReceiver(this.mInstallBroadcastReceiver, intentFilter);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, sessionId, new Intent(action), 134217728);
        if (pendingIntent == null) {
            return null;
        }
        return pendingIntent.getIntentSender();
    }

    /* access modifiers changed from: private */
    public class InstallBroadcastReceiver extends BroadcastReceiver {
        private boolean mFinished;
        private String mPackageName;
        private int mResult;

        private InstallBroadcastReceiver() {
            this.mFinished = false;
            this.mPackageName = "";
        }

        public void onReceive(Context context, Intent intent) {
            this.mResult = intent.getIntExtra("android.content.pm.extra.STATUS", -1000);
            if (this.mResult == 0) {
                DevicePackageManagerImpl.this.mContext.unregisterReceiver(this);
                this.mPackageName = intent.getStringExtra("android.content.pm.extra.PACKAGE_NAME");
            }
            this.mFinished = true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void uninstallPackage(ComponentName admin, String packageName, int flags) {
        PermissionManager.getInstance().checkPermission();
        if (this.mPackageManager != null) {
            this.mPackageManager.deletePackage(packageName, new PackageDeleteObserver2(), flags);
        }
    }

    private class PackageDeleteObserver2 extends IPackageDeleteObserver.Stub {
        private boolean mFinished;
        private String mPackageName;
        private int mResult;

        private PackageDeleteObserver2() {
            this.mFinished = false;
            this.mPackageName = "";
        }

        public void packageDeleted(String name, int status) {
            this.mFinished = true;
            this.mResult = status;
            this.mPackageName = name;
            Intent intent = new Intent();
            intent.setAction("com.android.cts.deviceowner.INTENT_PACKAGE_UNINSTALL_COMPLETE." + name);
            intent.putExtra("name", name);
            intent.putExtra("status", status);
            DevicePackageManagerImpl.this.mContext.sendBroadcast(intent);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void addDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().addDisallowedUninstallPackages(packageNames);
        } catch (RemoteException e) {
            Log.d("DevicePackageManagerImpl", "addDisallowedUninstallPackages: fail", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void removeDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().removeDisallowedUninstallPackages(packageNames);
        } catch (RemoteException e) {
            Log.d("DevicePackageManagerImpl", "removeDisallowedUninstallPackages: fail", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void removeAllDisallowedUninstallPackages(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().removeAllDisallowedUninstallPackages();
        } catch (RemoteException e) {
            Log.d("DevicePackageManagerImpl", "removeAllDisallowedUninstallPackages: fail", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public List<String> getDisallowUninstallPackageList(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            List<String> list = getCustomizeService().getDisallowUninstallPackageList();
            return list == null ? Collections.emptyList() : list;
        } catch (RemoteException e) {
            Log.d("DevicePackageManagerImpl", "getDisallowUninstallApps: fail", e);
            return Collections.emptyList();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void clearApplicationUserData(String packageName) {
        PermissionManager.getInstance().checkPermission();
        if (packageName == null || packageName.isEmpty()) {
            Log.e("DevicePackageManagerImpl", "clearApplicationUserData: invalid packageName");
            return;
        }
        try {
            getCustomizeService().clearAppData(packageName);
        } catch (RemoteException e) {
            Log.e("DevicePackageManagerImpl", "clearApplicationUserData: ", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public List<String> getClearAppName() {
        PermissionManager.getInstance().checkPermission();
        try {
            return getCustomizeService().getClearAppName();
        } catch (RemoteException e) {
            Log.d("DevicePackageManagerImpl", "get Clear App Name failed!");
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void setAdbInstallUninstallDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().setAdbInstallUninstallDisabled(disabled);
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "setAdbInstallUninstallDisabled: fail", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public boolean getAdbInstallUninstallDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return getCustomizeService().getAdbInstallUninstallDisabled();
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "getAdbInstallUninstallDisabled: fail", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void deleteApplicationCacheFiles(ComponentName admin, String packageName) {
        PermissionManager.getInstance().checkPermission();
        try {
            this.mPackageManager.deleteApplicationCacheFiles(packageName, null);
        } catch (Exception e) {
            Log.e("DevicePackageManagerImpl", "deleteApplicationCacheFiles fail", e);
        }
    }

    static {
        sPERMISSION_LIST.add(new PermissionBean("READ_IMEI", "android.permission.READ_PHONE_STATE"));
        sPERMISSION_LIST.add(new PermissionBean("START_ON_BOOT", "android.permission.RECEIVE_BOOT_COMPLETED"));
        sPERMISSION_LIST.add(new PermissionBean("CALL_PHONE", "android.permission.CALL_PHONE"));
        sPERMISSION_LIST.add(new PermissionBean("READ_CALLLOG", "android.permission.READ_CALL_LOG"));
        sPERMISSION_LIST.add(new PermissionBean("WRITE_CALLLOG", "android.permission.WRITE_CALL_LOG"));
        sPERMISSION_LIST.add(new PermissionBean("DELETE_CALLLOG", "android.permission.WRITE_CALL_LOG_DELETE"));
        sPERMISSION_LIST.add(new PermissionBean("SEND_SMS", "android.permission.SEND_SMS"));
        sPERMISSION_LIST.add(new PermissionBean("READ_MSG", "android.permission.READ_SMS"));
        sPERMISSION_LIST.add(new PermissionBean("SEND_MMS", "android.permission.SEND_MMS"));
        sPERMISSION_LIST.add(new PermissionBean("READ_MMS", "android.permission.READ_MMS"));
        sPERMISSION_LIST.add(new PermissionBean("READ_CONTACTS", "android.permission.READ_CONTACTS"));
        sPERMISSION_LIST.add(new PermissionBean("WRITE_CONTACTS", "android.permission.WRITE_CONTACTS"));
        sPERMISSION_LIST.add(new PermissionBean("OPEN_MOBILENETWORK", "android.permission.CHANGE_NETWORK_STATE"));
        sPERMISSION_LIST.add(new PermissionBean("OPEN_WLAN", "android.permission.CHANGE_WIFI_STATE"));
        sPERMISSION_LIST.add(new PermissionBean("OPEN_BLUETOOTH", "android.permission.BLUETOOTH"));
        sPERMISSION_LIST.add(new PermissionBean("OPEN_NFC", "android.permission.NFC"));
        sPERMISSION_LIST.add(new PermissionBean("LOCATION", "android.permission.ACCESS_FINE_LOCATION"));
        sPERMISSION_LIST.add(new PermissionBean("RECORD_AUDIO", "android.permission.RECORD_AUDIO"));
        sPERMISSION_LIST.add(new PermissionBean("RECORD_SCREEN", "android.permission.MANAGE_MEDIA_PROJECTION"));
        sPERMISSION_LIST.add(new PermissionBean("PHOTO", "android.permission.CAMERA"));
        sPERMISSION_LIST.add(new PermissionBean("VIDEO", "android.permission.CAMERA"));
    }

    /* access modifiers changed from: private */
    public static class PermissionBean {
        String desp;
        String permission;

        public PermissionBean(String desp2, String permission2) {
            this.desp = desp2;
            this.permission = permission2;
        }
    }

    private String transPermission(String permission) {
        for (PermissionBean bean : sPERMISSION_LIST) {
            if (bean.permission.equals(permission)) {
                return bean.desp;
            }
            if (bean.desp.equals(permission)) {
                return bean.permission;
            }
        }
        return "";
    }

    private Map<String, Set<String>> readPermissionCustomFixedFile(String path) {
        Map<String, Set<String>> map = new HashMap<>();
        File file = new File(path);
        if (file.exists()) {
            BufferedReader br = null;
            try {
                BufferedReader br2 = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = br2.readLine();
                    if (line != null) {
                        JSONArray jArray = new JSONArray(line);
                        String packageName = jArray.getJSONObject(0).getString("packageName");
                        JSONArray permJSONArray = jArray.getJSONObject(1).getJSONArray("fixed");
                        Set<String> set = new HashSet<>();
                        for (int i = 0; i < permJSONArray.length(); i++) {
                            set.add(permJSONArray.getJSONObject(i).getString("permission"));
                        }
                        if (!set.isEmpty()) {
                            map.put(packageName, set);
                        }
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                        }
                    }
                }
                br2.close();
            } catch (Exception e2) {
                Log.d("DevicePackageManagerImpl", "Failed to read permission custom fixed file.");
                if (0 != 0) {
                    br.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        br.close();
                    } catch (IOException e3) {
                    }
                }
                throw th;
            }
        }
        return map;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0030 A[SYNTHETIC, Splitter:B:17:0x0030] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00d3 A[SYNTHETIC, Splitter:B:42:0x00d3] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0121  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0133  */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public boolean setAppPermission(String appPackageName, String permissions, boolean fixed) throws RemoteException {
        Map<String, Set<String>> mapCustomFixedPermission;
        int i;
        int i2;
        int permissiontype;
        PermissionManager.getInstance().checkPermission();
        JSONArray array = null;
        boolean succeed = false;
        long identity = Binder.clearCallingIdentity();
        try {
            try {
                array = new JSONArray(permissions);
            } catch (JSONException e) {
                succeed = false;
                while (i2 < array.length()) {
                }
                ArrayList<String> requestedPermissions = PermissionConstants.getPkgAllPermissionWithFilter(PermissionConstants.getPkgAllPermissionFromDb(this.mContext, appPackageName));
                mapCustomFixedPermission = readPermissionCustomFixedFile("/data/oppo/coloros/permission/custom-fixed-permission.txt");
                while (i < array.length()) {
                }
                if (mapCustomFixedPermission.isEmpty()) {
                }
                Binder.restoreCallingIdentity(identity);
                return succeed;
            }
        } catch (JSONException e2) {
            succeed = false;
            while (i2 < array.length()) {
            }
            ArrayList<String> requestedPermissions2 = PermissionConstants.getPkgAllPermissionWithFilter(PermissionConstants.getPkgAllPermissionFromDb(this.mContext, appPackageName));
            mapCustomFixedPermission = readPermissionCustomFixedFile("/data/oppo/coloros/permission/custom-fixed-permission.txt");
            while (i < array.length()) {
            }
            if (mapCustomFixedPermission.isEmpty()) {
            }
            Binder.restoreCallingIdentity(identity);
            return succeed;
        }
        if (array != null && array.length() > 0) {
            for (i2 = 0; i2 < array.length(); i2++) {
                try {
                    JSONObject jitem = array.getJSONObject(i2);
                    String permission = transPermission(jitem.getString("permission"));
                    String mode = jitem.getString("mode");
                    if (mode == null) {
                        Log.d("DevicePackageManagerImpl", "wrong permission mode:" + mode + ",ignore!");
                    } else {
                        if (mode.equals("REMIND")) {
                            permissiontype = 2;
                        } else if (mode.equals("ALLOWED")) {
                            permissiontype = 0;
                        } else if (mode.equals("DISALLOW")) {
                            permissiontype = 1;
                        } else {
                            Log.d("DevicePackageManagerImpl", "wrong permission mode:" + mode + ",ignore!");
                        }
                        succeed = PermissionUtils.grantAppPermissionWithChoice(this.mContext, appPackageName, permission, permissiontype);
                    }
                } catch (Exception e3) {
                    succeed = false;
                    Log.d("DevicePackageManagerImpl", "Failed to parse JSON");
                }
            }
        }
        if (array != null && array.length() > 0) {
            ArrayList<String> requestedPermissions22 = PermissionConstants.getPkgAllPermissionWithFilter(PermissionConstants.getPkgAllPermissionFromDb(this.mContext, appPackageName));
            mapCustomFixedPermission = readPermissionCustomFixedFile("/data/oppo/coloros/permission/custom-fixed-permission.txt");
            for (i = 0; i < array.length(); i++) {
                try {
                    String perm = array.getJSONObject(i).getString("permission");
                    if (perm != null && !perm.isEmpty()) {
                        String permission2 = transPermission(perm);
                        if (requestedPermissions22.contains(permission2)) {
                            if (mapCustomFixedPermission.containsKey(appPackageName)) {
                                Set<String> set = mapCustomFixedPermission.get(appPackageName);
                                if (fixed) {
                                    set.add(permission2);
                                } else {
                                    set.remove(permission2);
                                }
                            } else if (fixed) {
                                Set<String> set2 = new HashSet<>();
                                set2.add(permission2);
                                mapCustomFixedPermission.put(appPackageName, set2);
                            }
                        }
                    }
                } catch (JSONException e4) {
                }
            }
            if (mapCustomFixedPermission.isEmpty()) {
                File file = new File("/data/oppo/coloros/permission/custom-fixed-permission.txt");
                if (file.exists()) {
                    file.delete();
                }
            } else {
                StringBuffer sb = new StringBuffer();
                for (Map.Entry<String, Set<String>> entry : mapCustomFixedPermission.entrySet()) {
                    JSONArray permJSONArray = new JSONArray();
                    JSONArray destJSONArray = new JSONArray();
                    try {
                        for (Iterator<String> it = entry.getValue().iterator(); it.hasNext(); it = it) {
                            permJSONArray.put(new JSONObject().put("permission", it.next()));
                        }
                        destJSONArray.put(new JSONObject().put("packageName", entry.getKey()));
                        destJSONArray.put(new JSONObject().put("fixed", permJSONArray));
                        sb.append(destJSONArray.toString());
                        sb.append("\r\n");
                    } catch (JSONException e5) {
                    }
                }
                if (sb.length() > 0) {
                    PermissionManager.writeStringToFile("DevicePackageManagerImpl", "/data/oppo/coloros/permission/custom-fixed-permission.txt", sb.toString());
                }
            }
        }
        Binder.restoreCallingIdentity(identity);
        return succeed;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public String getAppPermission(String appPackageName) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        JSONArray array = new JSONArray();
        if (this.mPackageManager == null) {
            Log.d("DevicePackageManagerImpl", "mPackageManager is null!!");
            return array.toString();
        }
        ArrayList<String> list = PermissionConstants.getPkgAllPermissionWithFilter(PermissionConstants.getPkgAllPermissionFromDb(this.mContext, appPackageName));
        if (list != null && list.size() > 0) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String s = it.next();
                String mode = "NONE";
                switch (PermissionUtils.getPermissionState(this.mContext, appPackageName, s)) {
                    case 0:
                        mode = "ALLOWED";
                        break;
                    case 1:
                        mode = "DISALLOW";
                        break;
                    case 2:
                        mode = "REMIND";
                        break;
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    String s2 = transPermission(s);
                    if (!s2.equals("")) {
                        jsonObject.put("permission", s2);
                        jsonObject.put("mode", mode);
                        array.put(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Binder.restoreCallingIdentity(identity);
        return array.toString();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void setDefaultDialer(String packageName) {
        PermissionManager.getInstance().checkPermission();
        try {
            new Dialer(this.mContext).setDefaultApp(packageName);
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "setDefaultDialer: fail");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public String getDefaultDialerPackage(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return ((TelecomManager) this.mContext.getSystemService("telecom")).getDefaultDialerPackage();
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "getDefaultDialerPackage: fail");
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public String getSystemDialerPackage(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return ((TelecomManager) this.mContext.getSystemService("telecom")).getSystemDialerPackage();
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "getSystemDialerPackage: fail");
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public boolean setDefaultMessage(String packageName) {
        PermissionManager.getInstance().checkPermission();
        try {
            return new Message(this.mContext).setDefaultApp(packageName);
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "setDefaultMessage: fail");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public String getDefaultMessage() {
        PermissionManager.getInstance().checkPermission();
        try {
            return new Message(this.mContext).getDefaultPackage(this.mContext.getPackageManager());
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "setDefaultMessage: fail");
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public boolean setDefaultBrowser(String packageName) {
        PermissionManager.getInstance().checkPermission();
        try {
            return new Browser(this.mContext).setDefaultApp(packageName);
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "setDefaultMessage: fail");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public boolean clearDefaultBrowser() {
        PermissionManager.getInstance().checkPermission();
        try {
            return new Browser(this.mContext).setDefaultApp(Browser.getCurrentSystemBrowserName(this.mContext));
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "setDefaultMessage: fail");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public String getDefaultBrowser() {
        PermissionManager.getInstance().checkPermission();
        try {
            return new Browser(this.mContext).getDefaultPackage(this.mContext.getPackageManager());
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "setDefaultMessage: fail");
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void addDisabledDeactivateMdmPackages(List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().addDisabledDeactivateMdmPackages(packageNames);
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "addDisabledDeactivateMdmPackages: failed");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void removeDisabledDeactivateMdmPackages(List<String> packageNames) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().removeDisabledDeactivateMdmPackages(packageNames);
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "removeDisabledDeactivateMdmPackages: failed");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public List<String> getDisabledDeactivateMdmPackages(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<String> disabledDeactivateMdmPackagesList = new ArrayList<>();
        try {
            return getCustomizeService().getDisabledDeactivateMdmPackages(admin);
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "getDisabledDeactivateMdmPackages: failed");
            return disabledDeactivateMdmPackagesList;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void removeAllDisabledDeactivateMdmPackages(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            getCustomizeService().removeAllDisabledDeactivateMdmPackages(admin);
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "removeDisabledDeactivateMdmPackages: failed");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void setDefaultApplication(ComponentName admin, ComponentName componentName, String type) {
        PermissionManager.getInstance().checkPermission();
        try {
            if (this.mPackageManager != null && componentName != null) {
                this.mPackageManager.setDefaultBrowserPackageNameAsUser(componentName.getPackageName(), UserHandle.myUserId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public void setSysAppList(ComponentName admin, Map maps, Bundle bundle) {
        PermissionManager.getInstance().checkPermission();
        try {
            Bundle newBundle = new Bundle();
            StringBuilder builder = new StringBuilder();
            boolean isPrivPermission = false;
            boolean isUndetachable = false;
            boolean isAddItem = true;
            if (!(this.mPackageManager == null || admin == null)) {
                if (bundle != null) {
                    isPrivPermission = bundle.getBoolean("privPermission", false);
                    isUndetachable = bundle.getBoolean("undetachable", false);
                    isAddItem = bundle.getBoolean("addItem", true);
                }
                for (Object pkgName : maps.keySet()) {
                    String pkgSig = (String) maps.get(pkgName);
                    if (!TextUtils.isEmpty((String) pkgName) && !TextUtils.isEmpty(pkgSig)) {
                        builder.append(pkgName);
                        builder.append(":");
                        builder.append(pkgSig);
                        builder.append(":");
                        builder.append(isPrivPermission ? "true" : "false");
                        builder.append(":");
                        builder.append(isUndetachable ? "true" : "false");
                        builder.append(";");
                    }
                }
                Bundle oldBundle = getCustomizeService().getInstallSysAppBundle();
                if (oldBundle != null) {
                    String oldValue = oldBundle.getString("value");
                    String newValue = formatSysAppData(builder.toString(), oldValue, isAddItem);
                    Log.d("DevicePackageManagerImpl", "oldValue: " + oldValue);
                    Log.d("DevicePackageManagerImpl", "newValue: " + newValue);
                    if (newValue != null && !newValue.equals(oldValue)) {
                        newBundle.putString("value", newValue);
                    } else {
                        return;
                    }
                } else if (isAddItem) {
                    newBundle.putString("value", builder.toString());
                } else {
                    return;
                }
                getCustomizeService().setInstallSysAppBundle(newBundle);
                Log.d("DevicePackageManagerImpl", "setSysAppList: " + newBundle.getString("value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public List<String> getSysAppList(ComponentName admin, List pkgNames) {
        PermissionManager.getInstance().checkPermission();
        if (pkgNames == null || pkgNames.size() < 1) {
            return null;
        }
        List<String> pkgList = new ArrayList<>(pkgNames.size());
        try {
            Bundle Bundle = getCustomizeService().getInstallSysAppBundle();
            Iterator it = pkgNames.iterator();
            while (it.hasNext()) {
                String foundItem = findValueFromBundle(Bundle, (String) it.next());
                if (!TextUtils.isEmpty(foundItem)) {
                    pkgList.add(foundItem);
                }
            }
        } catch (Exception e) {
            Log.d("DevicePackageManagerImpl", "getSysAppList: " + e);
        }
        Log.d("DevicePackageManagerImpl", "getSysAppList: " + pkgList.toString());
        return pkgList;
    }

    /* JADX INFO: Multiple debug info for r5v4 java.lang.String: [D('builder' java.lang.StringBuilder), D('result2' java.lang.String)] */
    private String formatSysAppData(String newValue, String currentValue, boolean isAddOrUpdate) {
        if (!TextUtils.isEmpty(currentValue)) {
            String[] split = newValue.split(";");
            int length = split.length;
            int i = 0;
            String result2 = currentValue;
            int i2 = 0;
            while (i2 < length) {
                String sysPkg = split[i2];
                String[] infoList = sysPkg.split(":");
                if (infoList.length != 4) {
                    return result2;
                }
                String pkgName = infoList[i];
                if (result2.contains(pkgName + ":" + infoList[1] + ":")) {
                    int pkgIndex = result2.indexOf(pkgName, i);
                    String singleValue = result2.substring(pkgIndex, result2.indexOf(";", pkgIndex));
                    if (isAddOrUpdate) {
                        result2 = result2.replace(singleValue, sysPkg);
                    } else {
                        result2 = result2.replace(singleValue + ";", "");
                    }
                } else if (isAddOrUpdate) {
                    result2 = result2 + sysPkg + ";";
                }
                i2++;
                i = 0;
            }
            return result2;
        } else if (isAddOrUpdate) {
            return newValue;
        } else {
            return "";
        }
    }

    private String findValueFromBundle(Bundle bundle, String pkgName) {
        String value;
        if (bundle == null || (value = bundle.getString("value")) == null) {
            return null;
        }
        for (String sysPkg : value.split(";")) {
            String[] infoList = sysPkg.split(":");
            if (infoList.length == 4 && pkgName.equals(infoList[0])) {
                return pkgName + ":" + infoList[1] + ":" + infoList[2] + ":" + infoList[3];
            }
        }
        return null;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public boolean setSuperWhiteList(ComponentName componentName, List<String> list) {
        PermissionManager.getInstance().checkPermission();
        try {
            for (String pkg : list) {
                DeviceApplicationManager.getInstance(this.mContext).setDrawOverlays(componentName, pkg, 2);
                DeviceSettingsManager.getInstance(this.mContext).setPackageNotificationEnable(pkg, false, true);
                DeviceStateManager.getInstance(this.mContext).ignoringBatteryOptimizations(componentName, pkg);
            }
            return getCustomizeService().setSuperWhiteList(list);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DevicePackageManagerImpl", "setSuperWhiteList: failed");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public List<String> getSuperWhiteList() {
        PermissionManager.getInstance().checkPermission();
        try {
            if (getCustomizeService().getSuperWhiteList() == null) {
                return null;
            }
            Log.d("DevicePackageManagerImpl", "getSuperWhiteList: success");
            return getCustomizeService().getSuperWhiteList();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DevicePackageManagerImpl", "getSuperWhiteList: failed");
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public boolean clearSuperWhiteList(ComponentName componentName, List<String> clearList) {
        PermissionManager.getInstance().checkPermission();
        try {
            for (String pkg : clearList) {
                DeviceApplicationManager.getInstance(this.mContext).setDrawOverlays(componentName, pkg, 0);
            }
            return getCustomizeService().clearSuperWhiteList(clearList);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DevicePackageManagerImpl", "clearSuperWhiteList: failed");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
    public boolean clearAllSuperWhiteList(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        try {
            List<String> allClearList = getCustomizeService().getSuperWhiteList();
            if (allClearList != null) {
                if (allClearList.size() != 0) {
                    for (String pkg : allClearList) {
                        DeviceApplicationManager.getInstance(this.mContext).setDrawOverlays(componentName, pkg, 0);
                    }
                    return getCustomizeService().clearAllSuperWhiteList();
                }
            }
            Log.d("DevicePackageManagerImpl", "SuperWhiteList is null. clearAllSuperWhiteList fail ");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DevicePackageManagerImpl", "clearAllSuperWhiteList: failed");
            return false;
        }
    }
}
