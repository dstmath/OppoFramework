package com.android.server.location;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.LocAppsOp;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.location.interfaces.IPswLocationBlacklistUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class OppoLocationBlacklistUtil implements IPswLocationBlacklistUtil {
    public static final String ACTION_UPDATE_REQUIREMENTS = "oppo.location.blacklist.update.gps.requirements";
    private static final String ADMIN_PERM = "android.permission.WRITE_SECURE_SETTINGS";
    private static final String ADMIN_PERM_ERROR = "WRITE_SECURE_SETTINGS permission required";
    private static boolean DEBUG = false;
    private static final int FOREGROUND_UI_IMPORTANCE = 100;
    private static final int MSG_OP_CHANGED = 2;
    private static final String TAG = "OppoLocationBlacklistUtil";
    /* access modifiers changed from: private */
    public ActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public Context mContext;
    private FakeLocationListener mFakeLocationListener;
    private Handler mHandler;
    private LocationManager mLocationManager;
    /* access modifiers changed from: private */
    public final Object mLock;
    /* access modifiers changed from: private */
    public OppoLocationBlacklist mOppoBlackList;
    /* access modifiers changed from: private */
    public HashMap<String, PackageStatusInLocationService> mPackageLocationStatusRecordMap;
    private boolean mWorkingFlag;

    private OppoLocationBlacklistUtil() {
        this.mLock = new Object();
        this.mPackageLocationStatusRecordMap = null;
        this.mWorkingFlag = false;
    }

    private static class GenerateSingletonInstance {
        /* access modifiers changed from: private */
        public static final OppoLocationBlacklistUtil INSTANCE = new OppoLocationBlacklistUtil();

        private GenerateSingletonInstance() {
        }
    }

    public static OppoLocationBlacklistUtil getInstance() {
        return GenerateSingletonInstance.INSTANCE;
    }

    public void init(Context context, Looper loop) {
        this.mContext = context;
        this.mHandler = new OppoLocationBlacklistUtilHandler(loop);
        this.mPackageLocationStatusRecordMap = new HashMap<>();
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mFakeLocationListener = new FakeLocationListener();
        this.mOppoBlackList = new OppoLocationBlacklist(this.mContext, this.mHandler);
        this.mActivityManager.addOnUidImportanceListener(new ActivityManager.OnUidImportanceListener() {
            /* class com.android.server.location.$$Lambda$OppoLocationBlacklistUtil$vKmoPrKGFSTDdrUAECT7xhtg4IE */

            public final void onUidImportance(int i, int i2) {
                OppoLocationBlacklistUtil.this.lambda$init$1$OppoLocationBlacklistUtil(i, i2);
            }
        }, FOREGROUND_UI_IMPORTANCE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.location.OppoLocationBlacklistUtil.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction()) && OppoLocationBlacklistUtil.this.mOppoBlackList != null) {
                    OppoLocationBlacklistUtil.this.mOppoBlackList.shutdown();
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mHandler);
    }

    public /* synthetic */ void lambda$init$1$OppoLocationBlacklistUtil(int uid, int importance) {
        this.mHandler.post(new Runnable(uid, importance) {
            /* class com.android.server.location.$$Lambda$OppoLocationBlacklistUtil$IxWSkLWZmQNYLzgUgWYPkr1Vz0E */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                OppoLocationBlacklistUtil.this.lambda$init$0$OppoLocationBlacklistUtil(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$init$0$OppoLocationBlacklistUtil(int uid, int importance) {
        synchronized (this.mLock) {
            onUidImportanceChangedLocked(uid, importance);
        }
    }

    public void recordPackagesLocationStatus(String packageName, int packageUid, int packagePid, String locationProvider) {
        PackageStatusInLocationService packageStatusInLocationService;
        synchronized (this.mLock) {
            if (this.mPackageLocationStatusRecordMap.containsKey(packageName)) {
                packageStatusInLocationService = this.mPackageLocationStatusRecordMap.get(packageName);
                packageStatusInLocationService.updateInfo(packageName, packageUid, packagePid, locationProvider);
            } else {
                packageStatusInLocationService = new PackageStatusInLocationService(packageName, packageUid, packagePid, locationProvider);
                this.mPackageLocationStatusRecordMap.put(packageName, packageStatusInLocationService);
            }
            if (DEBUG) {
                Log.v(TAG, "recordPackagesLocationStatus:" + packageStatusInLocationService.toString());
            }
        }
    }

    public void removePackagesLocationStatus(String packageName, int packageUid, int packagePid, String locationProvider) {
        synchronized (this.mLock) {
            if (this.mPackageLocationStatusRecordMap.containsKey(packageName)) {
                PackageStatusInLocationService packageStatusInLocationService = this.mPackageLocationStatusRecordMap.get(packageName);
                if (locationProvider.equals("gps")) {
                    packageStatusInLocationService.mGpsRequestNum--;
                } else {
                    packageStatusInLocationService.mNetworkRequestNum--;
                }
                if (packageStatusInLocationService.mGpsRequestNum == 0 && packageStatusInLocationService.mNetworkRequestNum == 0) {
                    this.mPackageLocationStatusRecordMap.remove(packageName);
                } else {
                    this.mPackageLocationStatusRecordMap.get(packageName).updateInfo(packageName, packageUid, packagePid, locationProvider);
                }
            } else if (DEBUG) {
                Log.e(TAG, "remove a package,that don't exist in record map:" + packageName);
            }
        }
    }

    public boolean isPackageBlocked(String packageName, String provider) {
        boolean z;
        boolean isBlockFlag = false;
        synchronized (this.mLock) {
            z = true;
            if (this.mPackageLocationStatusRecordMap.containsKey(packageName)) {
                PackageStatusInLocationService packageStatusInLocationService = this.mPackageLocationStatusRecordMap.get(packageName);
                int i = packageStatusInLocationService.mPackagePermission;
                if (i == 1) {
                    isBlockFlag = false;
                } else if (i == 2) {
                    isBlockFlag = packageStatusInLocationService.mBackgroundFlag;
                } else if (i != 3) {
                    isBlockFlag = false;
                } else {
                    isBlockFlag = true;
                }
            }
        }
        if (isBlockFlag) {
            z = false;
        }
        updateHighPowerMonitor(packageName, z);
        Log.v(TAG, "block status:" + packageName + "=" + isBlockFlag);
        return isBlockFlag;
    }

    public void getLocAppsOp(int flag, LocAppsOp locAppsOp) {
        Context context = this.mContext;
        if (context != null) {
            context.enforceCallingOrSelfPermission(ADMIN_PERM, ADMIN_PERM_ERROR);
        }
        if (locAppsOp == null || locAppsOp.getAppsOp() == null) {
            throw new IllegalArgumentException("invalid LocAppsOp");
        }
        this.mOppoBlackList.getLocAppsOp(flag, locAppsOp);
    }

    public void setLocAppsOp(int cmd, LocAppsOp locAppsOp) {
        Context context = this.mContext;
        if (context != null) {
            context.enforceCallingOrSelfPermission(ADMIN_PERM, ADMIN_PERM_ERROR);
        }
        if (locAppsOp == null || locAppsOp.getAppsOp() == null) {
            throw new IllegalArgumentException("invalid LocAppsOp");
        }
        this.mOppoBlackList.setLocAppsOp(cmd, locAppsOp);
        this.mHandler.removeMessages(2);
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 2));
    }

    private void updateHighPowerMonitor(String packageName, boolean start) {
        synchronized (this.mLock) {
            if (this.mPackageLocationStatusRecordMap.containsKey(packageName) && start != this.mPackageLocationStatusRecordMap.get(packageName).highPowerMonitorFlag) {
                this.mPackageLocationStatusRecordMap.get(packageName).highPowerMonitorFlag = start;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003a, code lost:
        return r1;
     */
    public boolean needChangeNotifyStatus(String packageName, boolean isBlocked) {
        synchronized (this.mLock) {
            boolean z = false;
            if (!this.mPackageLocationStatusRecordMap.containsKey(packageName)) {
                return false;
            }
            boolean needChange = isBlocked != this.mPackageLocationStatusRecordMap.get(packageName).highPowerMonitorFlag;
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("need change high monitor to:");
                if (!isBlocked) {
                    z = true;
                }
                sb.append(z);
                Log.v(TAG, sb.toString());
            }
        }
    }

    public boolean getHighPowerMonitor(String packageName) {
        synchronized (this.mLock) {
            if (!this.mPackageLocationStatusRecordMap.containsKey(packageName)) {
                return true;
            }
            boolean z = this.mPackageLocationStatusRecordMap.get(packageName).highPowerMonitorFlag;
            return z;
        }
    }

    public void setWorkingFlag(boolean workingFlag) {
        this.mWorkingFlag = workingFlag;
    }

    public boolean getWorkingFlag() {
        return this.mWorkingFlag;
    }

    public static void setDebug(boolean isDebug) {
        DEBUG = isDebug;
        OppoLocationBlacklist.setDebug(isDebug);
    }

    /* access modifiers changed from: private */
    public void startActiveRequest(String providerName) {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_REQUIREMENTS);
        this.mContext.sendBroadcast(intent);
        if (DEBUG) {
            Log.v(TAG, "send broadcast to locationmanagerservice:oppo.location.blacklist.update.gps.requirements");
        }
    }

    private void onUidImportanceChangedLocked(int uid, int importance) {
        boolean background = !isImportanceFgUI(importance);
        HashSet<String> affectedProviders = new HashSet<>();
        synchronized (this.mLock) {
            for (String packageName : this.mPackageLocationStatusRecordMap.keySet()) {
                PackageStatusInLocationService packageStatusInLocationService = this.mPackageLocationStatusRecordMap.get(packageName);
                if (packageStatusInLocationService.mPackageUid == uid && packageStatusInLocationService.mBackgroundFlag != background) {
                    packageStatusInLocationService.mBackgroundFlag = background;
                    if (packageStatusInLocationService.mPackagePermission == 2) {
                        if ((packageStatusInLocationService.mRequestLocationMethods & 2) != 0) {
                            affectedProviders.add("gps");
                        }
                        if ((packageStatusInLocationService.mRequestLocationMethods & 1) != 0) {
                            affectedProviders.add("network");
                        }
                    }
                }
            }
        }
        Iterator<String> it = affectedProviders.iterator();
        while (it.hasNext()) {
            String provider = it.next();
            if (provider.equals("gps")) {
                startActiveRequest(provider);
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean isImportanceFgUI(int importance) {
        return importance <= FOREGROUND_UI_IMPORTANCE;
    }

    private class OppoLocationBlacklistUtilHandler extends Handler {
        public OppoLocationBlacklistUtilHandler(Looper looper) {
            super(looper, null);
        }

        public void handleMessage(Message msg) {
            int op;
            if (msg.what == 2) {
                synchronized (OppoLocationBlacklistUtil.this.mLock) {
                    boolean needUpdate = false;
                    for (String packageName : OppoLocationBlacklistUtil.this.mPackageLocationStatusRecordMap.keySet()) {
                        PackageStatusInLocationService packageStatusInLocationService = (PackageStatusInLocationService) OppoLocationBlacklistUtil.this.mPackageLocationStatusRecordMap.get(packageName);
                        if (packageStatusInLocationService.mGpsRequestNum > 0 && packageStatusInLocationService.mPackagePermission != (op = OppoLocationBlacklistUtil.this.mOppoBlackList.getAppOp(packageName))) {
                            needUpdate = true;
                            packageStatusInLocationService.mPackagePermission = op;
                        }
                    }
                    if (needUpdate) {
                        OppoLocationBlacklistUtil.this.startActiveRequest("gps");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class PackageStatusInLocationService {
        public static final int REQUEST_GPS_LOCATION = 2;
        public static final int REQUEST_NETWORK_LOCATION = 1;
        public static final int REQUEST_OTHER_LOCATION = 4;
        public boolean highPowerMonitorFlag = true;
        public boolean mBackgroundFlag = false;
        public int mDefaultPermission = 1;
        public int mGpsRequestNum = 0;
        public int mNetworkRequestNum = 0;
        public String mPackageName;
        public int mPackagePermission;
        public int mPackagePid;
        public int mPackageUid;
        public int mRequestLocationMethods;
        public boolean preHighPowerMonitorFlag = true;

        public PackageStatusInLocationService(String packageName, int packageUid, int packagePid, String locationProvider) {
            updateInfo(packageName, packageUid, packagePid, locationProvider);
            this.highPowerMonitorFlag = true;
            this.preHighPowerMonitorFlag = true;
        }

        public void updateInfo(String packageName, int packageUid, int packagePid, String locationProvider) {
            int i;
            setMyParameter(packageName, packageUid, packagePid, locationProvider);
            if (isSystemApp(packageName)) {
                i = this.mDefaultPermission;
            } else {
                i = OppoLocationBlacklistUtil.this.mOppoBlackList.getAppOp(packageName);
            }
            this.mPackagePermission = i;
            this.mBackgroundFlag = !OppoLocationBlacklistUtil.isImportanceFgUI(OppoLocationBlacklistUtil.this.mActivityManager.getPackageImportance(packageName));
        }

        public String toString() {
            return "packageName:" + this.mPackageName + ":uid:" + this.mPackageUid + ":pid:" + this.mPackagePid + ":permission:" + this.mPackagePermission + ":lastRequestLocationMethods:" + this.mRequestLocationMethods + ":gpsRequestNum:" + this.mGpsRequestNum + ":networkRequestNum:" + this.mNetworkRequestNum + ":isBackgroundApp:" + this.mBackgroundFlag;
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0041  */
        private void setMyParameter(String packageName, int packageUid, int packagePid, String locationProvider) {
            char c;
            this.mPackageName = packageName;
            this.mPackageUid = packageUid;
            this.mPackagePid = packagePid;
            int hashCode = locationProvider.hashCode();
            if (hashCode != 102570) {
                if (hashCode == 1843485230 && locationProvider.equals("network")) {
                    c = 1;
                    if (c == 0) {
                        this.mRequestLocationMethods |= 2;
                        this.mGpsRequestNum++;
                        return;
                    } else if (c != 1) {
                        this.mRequestLocationMethods |= 4;
                        return;
                    } else {
                        this.mRequestLocationMethods |= 1;
                        this.mNetworkRequestNum++;
                        return;
                    }
                }
            } else if (locationProvider.equals("gps")) {
                c = 0;
                if (c == 0) {
                }
            }
            c = 65535;
            if (c == 0) {
            }
        }

        private boolean isSystemApp(String packageName) {
            try {
                ApplicationInfo info = OppoLocationBlacklistUtil.this.mContext.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
                if (info == null || (info.flags & 1) == 0) {
                    return false;
                }
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
    }

    private class FakeLocationListener implements LocationListener {
        private FakeLocationListener() {
        }

        public void onLocationChanged(Location location) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }
}
