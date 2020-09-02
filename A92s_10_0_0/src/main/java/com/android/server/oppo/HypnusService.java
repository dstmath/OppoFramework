package com.android.server.oppo;

import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.app.IHypnusService;
import com.oppo.hypnus.Hypnus;
import com.oppo.oiface.OifaceProxyUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import oppo.util.OppoStatistics;

public class HypnusService extends IHypnusService.Stub {
    private static final String ACTION_BOOTUP = "oppo.intent.action.BOOT_COMPLETED";
    private static final String ACTION_OPPO_DCS_PERIOD_UPLOAD = "oppo.intent.action.OPPO_DCS_PERIOD_UPLOAD";
    private static final int DISABLE_WIFI_POWER_SAVE_MODE = 1;
    private static final int ENABLE_WIFI_POWER_SAVE_MODE = 0;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String HYPNUS_EVENT_ID = "hypnus_action";
    private static final String HYPNUS_LOG_TAG = "hypnus";
    public static final Boolean HYPNUS_PERMISSIONS_ON = Boolean.valueOf(SystemProperties.getBoolean("persist.sys.hypnus.permission", true));
    private static final String TAG = "HypnusService";
    /* access modifiers changed from: private */
    public boolean debug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Hypnus mHyp;
    private final PackageManager mPackageManager;
    private BroadcastReceiver mStatisticsReceiver = new BroadcastReceiver() {
        /* class com.android.server.oppo.HypnusService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            NetworkInfo info;
            String action = intent.getAction();
            if (HypnusService.ACTION_OPPO_DCS_PERIOD_UPLOAD.equals(action) && HypnusService.this.mHyp != null) {
                HypnusService hypnusService = HypnusService.this;
                Hypnus unused = hypnusService.mHyp;
                HashMap<String, String> result = hypnusService.convertMaps(Hypnus.staticsCount);
                if (result != null && !result.isEmpty()) {
                    OppoStatistics.onCommon(HypnusService.this.mContext, HypnusService.HYPNUS_LOG_TAG, HypnusService.HYPNUS_EVENT_ID, result, false);
                    StringBuilder sb = new StringBuilder();
                    sb.append("hypnus_action_statics:");
                    Hypnus unused2 = HypnusService.this.mHyp;
                    sb.append(Hypnus.staticsCount);
                    Log.e(HypnusService.TAG, sb.toString());
                    Hypnus unused3 = HypnusService.this.mHyp;
                    Hypnus.staticsCount.clear();
                }
            } else if (HypnusService.ACTION_BOOTUP.equals(action)) {
                if (HypnusService.this.debug) {
                    Log.d(HypnusService.TAG, "oiface receive oppo boot up");
                }
                OifaceProxyUtils.getInstance().initNetworkState(HypnusService.this.mContext);
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction()) && (info = (NetworkInfo) intent.getParcelableExtra("networkInfo")) != null) {
                if (NetworkInfo.State.CONNECTED != info.getState() || !info.isAvailable()) {
                    if (HypnusService.this.debug) {
                        Log.i(HypnusService.TAG, "dis conn");
                    }
                    OifaceProxyUtils.getInstance().currentNetwork(1);
                } else if (info.getType() == 1) {
                    if (HypnusService.this.debug) {
                        Log.i(HypnusService.TAG, "wifi connected");
                    }
                    OifaceProxyUtils.getInstance().currentNetwork(0);
                } else if (info.getType() == 0) {
                    if (HypnusService.this.debug) {
                        Log.i(HypnusService.TAG, "data connected");
                    }
                    OifaceProxyUtils.getInstance().currentNetwork(2);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Timer mTimer = null;
    private WifiManager mWifiManger = null;

    public HypnusService(Context context) {
        this.mContext = context;
        this.mHyp = Hypnus.getHypnus();
        this.mWifiManger = (WifiManager) this.mContext.getSystemService("wifi");
        if (this.mWifiManger == null) {
            Log.e(TAG, "Get WIFI_SERVICE failed!");
        }
        regiestHypnusReceiver();
        this.mPackageManager = this.mContext.getPackageManager();
    }

    private void regiestHypnusReceiver() {
        Log.d(TAG, "regiestHypnusReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OPPO_DCS_PERIOD_UPLOAD);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction(ACTION_BOOTUP);
        this.mContext.registerReceiver(this.mStatisticsReceiver, filter, null, null);
    }

    /* access modifiers changed from: private */
    public HashMap<String, String> convertMaps(HashMap<String, Long> attributes) {
        HashMap<String, String> result = new HashMap<>();
        for (Map.Entry<String, Long> entry : attributes.entrySet()) {
            try {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
            } catch (ClassCastException e) {
                Log.e(TAG, "convertMaps exception");
                e.printStackTrace();
            }
        }
        return result;
    }

    public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) {
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            hypnus.hypnusSetNotification(msg_src, msg_type, msg_time, pid, v0, v1);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public void hypnusSetScene(int pid, String processName) {
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            hypnus.hypnusSetScene(pid, processName);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public void HypnusSetDisplayState(int state) {
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            hypnus.HypnusSetDisplayState(state);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public void hypnusSetAction(int action, int timeout) {
        if (!HYPNUS_PERMISSIONS_ON.booleanValue()) {
            dohypnusSetAction(action, timeout);
        } else if (Binder.getCallingUid() < 10000) {
            dohypnusSetAction(action, timeout);
        } else {
            checkSystemAppDoAction(action, timeout, Binder.getCallingUid());
        }
    }

    private boolean checkSystemAppDoAction(int action, int timeout, int callingUid) {
        try {
            String[] packages = AppGlobals.getPackageManager().getPackagesForUid(callingUid);
            if (packages != null) {
                IPackageManager pm = AppGlobals.getPackageManager();
                for (String name : packages) {
                    try {
                        if (this.debug) {
                            Log.d(TAG, "Pkgname:" + name);
                        }
                        PackageInfo packageInfo = pm.getPackageInfo(name, 0, UserHandle.getCallingUserId());
                        if (!(packageInfo == null || (packageInfo.applicationInfo.flags & 129) == 0)) {
                            if (this.debug) {
                                Log.d(TAG, "Pkgname:" + name + " is systemapp");
                            }
                            if (this.mHyp != null) {
                                this.mHyp.hypnusSetAction(action, timeout, name);
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, String.format("Could not find package [%s]", name), e);
                    }
                }
                Log.d(TAG, packages + " has no permission to call hypnus");
            } else {
                Log.w(TAG, "No known packages with uid " + callingUid);
            }
            return false;
        } catch (Exception e2) {
            Log.w(TAG, "getPackagesForUid failed " + callingUid);
            return false;
        }
    }

    private boolean checkSystemApp(int callingUid) {
        try {
            String[] packages = AppGlobals.getPackageManager().getPackagesForUid(callingUid);
            if (packages != null) {
                IPackageManager pm = AppGlobals.getPackageManager();
                for (String name : packages) {
                    try {
                        if (this.debug) {
                            Log.d(TAG, "Pkgname:" + name);
                        }
                        PackageInfo packageInfo = pm.getPackageInfo(name, 0, UserHandle.getCallingUserId());
                        if (!(packageInfo == null || (packageInfo.applicationInfo.flags & 129) == 0)) {
                            if (this.debug) {
                                Log.d(TAG, "Pkgname:" + name + " is systemapp");
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, String.format("Could not find package [%s]", name), e);
                    }
                }
                Log.d(TAG, packages + " has no permission to call hypnus");
            } else {
                Log.w(TAG, "No known packages with uid " + callingUid);
            }
            return false;
        } catch (Exception e2) {
            Log.w(TAG, "getPackagesForUid failed " + callingUid);
            return false;
        }
    }

    private void dohypnusSetAction(int action, int timeout) {
        if (this.mHyp == null) {
            Log.e(TAG, "mHyp is not initialized!");
        } else if (!Hypnus.HYPNUS_STATICS_ON.booleanValue() || !this.debug) {
            this.mHyp.hypnusSetAction(action, timeout, (String) null);
            if (this.debug) {
                Log.d(TAG, "hypnus statics has closed");
            }
        } else {
            try {
                String pkgnameinfo = AppGlobals.getPackageManager().getNameForUid(Binder.getCallingUid());
                if (pkgnameinfo == null) {
                    pkgnameinfo = "nopackagename";
                }
                int splitIndex = pkgnameinfo.indexOf(58);
                if (splitIndex > 0) {
                    this.mHyp.hypnusSetAction(action, timeout, pkgnameinfo.substring(0, splitIndex));
                } else {
                    this.mHyp.hypnusSetAction(action, timeout, pkgnameinfo);
                }
            } catch (RemoteException e) {
                this.mHyp.hypnusSetAction(action, timeout, "exception");
                e.printStackTrace();
            }
        }
    }

    public void hypnusSetSignatureAction(int action, int timeout, String signatureinfo) {
        Hypnus hypnus = this.mHyp;
        if (signatureinfo.equals(Hypnus.getLocalSignature())) {
            dohypnusSetAction(action, timeout);
        } else {
            Log.e(TAG, "this process has no permission to call hypnus");
        }
    }

    public void hypnusSetBurst(int tid, int type, int timeout) {
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            hypnus.hypnusSetBurst(tid, type, timeout);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public boolean isHypnusOK() {
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            return hypnus.isHypnusOK();
        }
        Log.e(TAG, "mHyp is not initialized!");
        return false;
    }

    public String hypnusGetHighPerfModeState() {
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            return hypnus.hypnusGetHighPerfModeState();
        }
        Log.e(TAG, "mHyp is not initialized!");
        return null;
    }

    public String hypnusGetPMState() {
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            return hypnus.hypnusGetPMState();
        }
        Log.e(TAG, "mHyp is not initialized!");
        return null;
    }

    public String hypnusGetBenchModeState() {
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            return hypnus.hypnusGetBenchModeState();
        }
        Log.e(TAG, "mHyp is not initialized!");
        return null;
    }

    public String getPackageHashInfo(String packageName) {
        if (Binder.getCallingUid() != 1000) {
            Log.w(TAG, "no permission to getPackageHashInfo");
            return null;
        } else if (packageName == null) {
            Log.w(TAG, "getSignatures packageName is null");
            return null;
        } else {
            try {
                PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(packageName, 64);
                if (packageInfo == null) {
                    Log.w(TAG, "getSignatures packageInfo is null");
                    return null;
                }
                Signature[] signatures = packageInfo.signatures;
                if (signatures == null || signatures.length == 0 || signatures[0] == null) {
                    return null;
                }
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
                    if (messageDigest == null) {
                        return null;
                    }
                    if (this.debug) {
                        Log.d(TAG, "getPackageHashInfo toCharsString is : " + signatures[0].toCharsString());
                    }
                    byte[] pkgNameByteArray = packageName.getBytes();
                    byte[] signaturesByteArray = signatures[0].toByteArray();
                    byte[] resultBytes = new byte[(signaturesByteArray.length + pkgNameByteArray.length)];
                    System.arraycopy(pkgNameByteArray, 0, resultBytes, 0, pkgNameByteArray.length);
                    System.arraycopy(signaturesByteArray, 0, resultBytes, pkgNameByteArray.length, signaturesByteArray.length);
                    messageDigest.update(resultBytes);
                    byte[] digest = messageDigest.digest();
                    if (digest == null) {
                        return null;
                    }
                    if (digest.length == 0) {
                        return null;
                    }
                    int digestLength = digest.length;
                    char[] chars = new char[(digestLength * 2)];
                    for (int i = 0; i < digestLength; i++) {
                        int byteHex = digest[i] & 255;
                        chars[i * 2] = HEX_ARRAY[byteHex >>> 4];
                        chars[(i * 2) + 1] = HEX_ARRAY[byteHex & 15];
                    }
                    String sha256 = new String(chars);
                    if (this.debug) {
                        Log.d(TAG, "getPackageHashInfo sha256 is : " + sha256);
                    }
                    return sha256;
                } catch (NoSuchAlgorithmException e) {
                    Log.w(TAG, "getSignatures NoSuchAlgorithmException");
                    return null;
                }
            } catch (PackageManager.NameNotFoundException e2) {
                Log.w(TAG, "getSignatures error: " + e2);
                return null;
            }
        }
    }

    public int HypnusSetPerfData(int small_max, int small_min, int small_cores, int big_max, int big_min, int big_cores, int gpu_max, int gpu_min, int gpu_cores, int flags) {
        if (!checkSystemApp(Binder.getCallingUid())) {
            return -1;
        }
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            return hypnus.HypnusSetPerfData(small_max, small_min, small_cores, big_max, big_min, big_cores, gpu_max, gpu_min, gpu_cores, flags);
        }
        Log.e(TAG, "mHyp is not initialized!");
        return -1;
    }

    public void gameStatusBroadCast(String action, String pkgName, String className, Map extArgs) {
        Set keys;
        if (this.debug) {
            Log.d(TAG, "gameStatusBroadcast Binder.getCallingUid():" + Binder.getCallingUid());
        }
        if (Binder.getCallingUid() == 1000) {
            Intent intent = new Intent();
            if (!(extArgs == null || extArgs.isEmpty() || (keys = extArgs.keySet()) == null)) {
                for (Object key : keys) {
                    Object value = extArgs.get(key);
                    if (this.debug) {
                        Log.d(TAG, "gameStatusBroadcast extArgs:" + key + ":" + value);
                    }
                    if (!(key == null || value == null)) {
                        intent.putExtra(String.valueOf(key), String.valueOf(value));
                    }
                }
            }
            if (!TextUtils.isEmpty(pkgName) && !TextUtils.isEmpty(className)) {
                if (this.debug) {
                    Log.d(TAG, "gameStatusBroadcast setClassName:" + pkgName + ":" + className);
                }
                intent.setClassName(pkgName, className);
                try {
                    this.mContext.startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "ofiace start Activity has Exception : " + e);
                }
            } else if (!TextUtils.isEmpty(action)) {
                if (this.debug) {
                    Log.d(TAG, "gameStatusBroadcast sendBroadcast:" + action);
                }
                intent.setAction(action);
                this.mContext.sendBroadcast(intent);
            }
        }
    }

    public int HypnusClrPerfData() {
        if (!checkSystemApp(Binder.getCallingUid())) {
            return -1;
        }
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            return hypnus.HypnusClrPerfData();
        }
        Log.e(TAG, "mHyp is not initialized!");
        return -1;
    }

    public int HypnusSetScenePerfData(int scene) {
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            return hypnus.HypnusSetScenePerfData(scene);
        }
        Log.e(TAG, "mHyp is not initialized!");
        return -1;
    }

    public void hypnusSetWifiPowerSaveMode(int type, int timeout) {
        if (this.mWifiManger == null) {
            Log.e(TAG, "Get wifiService failed!");
        }
        if (type == 1) {
            disableWifiPowerSaveMode();
        } else if (type == 0) {
            enableWifiPowerSaveMode();
        }
        if (timeout > 0 && type == 1) {
            this.mTimer = new Timer();
            this.mTimer.schedule(new WifiTimerTask(), (long) timeout);
        }
        if (this.debug) {
            Log.i(TAG, "hypnusSetWifiPowerSaveMode is " + type);
        }
    }

    private void disableWifiPowerSaveMode() {
        if (this.mWifiManger != null && this.debug) {
            Log.i(TAG, "disableWifiPowerSaveMode");
        }
    }

    /* access modifiers changed from: private */
    public void enableWifiPowerSaveMode() {
        if (this.mWifiManger != null && this.debug) {
            Log.i(TAG, "enableWifiPowerSaveMode");
        }
    }

    private class WifiTimerTask extends TimerTask {
        private WifiTimerTask() {
        }

        public void run() {
            HypnusService.this.enableWifiPowerSaveMode();
            if (HypnusService.this.mTimer != null) {
                HypnusService.this.mTimer.cancel();
                HypnusService.this.mTimer.purge();
            }
            if (HypnusService.this.debug) {
                Log.i(HypnusService.TAG, "timer run enableWifiPowerSaveMode");
            }
        }
    }
}
