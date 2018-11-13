package com.android.server.oppo;

import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.app.IHypnusService.Stub;
import com.oppo.hypnus.Hypnus;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import oppo.util.OppoStatistics;

public class HypnusService extends Stub {
    private static final String ACTION_OPPO_DCS_PERIOD_UPLOAD = "oppo.intent.action.OPPO_DCS_PERIOD_UPLOAD";
    private static final int DISABLE_WIFI_POWER_SAVE_MODE = 1;
    private static final int ENABLE_WIFI_POWER_SAVE_MODE = 0;
    private static final String HYPNUS_EVENT_ID = "hypnus_action";
    private static final String HYPNUS_LOG_TAG = "hypnus";
    public static final Boolean HYPNUS_PERMISSIONS_ON = Boolean.valueOf(SystemProperties.getBoolean("persist.sys.hypnus.permission", true));
    private static final String TAG = "HypnusService";
    private boolean debug = SystemProperties.getBoolean("persist.debug.hypnus", false);
    private Context mContext;
    private Hypnus mHyp;
    private final PackageManager mPackageManager;
    private BroadcastReceiver mStatisticsReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (HypnusService.ACTION_OPPO_DCS_PERIOD_UPLOAD.equals(intent.getAction()) && HypnusService.this.mHyp != null) {
                HypnusService hypnusService = HypnusService.this;
                HypnusService.this.mHyp;
                HashMap<String, String> result = hypnusService.convertMaps(Hypnus.staticsCount);
                if (result != null && (result.isEmpty() ^ 1) != 0) {
                    OppoStatistics.onCommon(HypnusService.this.mContext, HypnusService.HYPNUS_LOG_TAG, HypnusService.HYPNUS_EVENT_ID, result, false);
                    String str = HypnusService.TAG;
                    StringBuilder append = new StringBuilder().append("hypnus_action_statics:");
                    HypnusService.this.mHyp;
                    Log.e(str, append.append(Hypnus.staticsCount).toString());
                    HypnusService.this.mHyp;
                    Hypnus.staticsCount.clear();
                }
            }
        }
    };
    private Timer mTimer = null;
    private WifiManager mWifiManger = null;

    private class WifiTimerTask extends TimerTask {
        /* synthetic */ WifiTimerTask(HypnusService this$0, WifiTimerTask -this1) {
            this();
        }

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
        this.mContext.registerReceiver(this.mStatisticsReceiver, filter, null, null);
    }

    private HashMap<String, String> convertMaps(HashMap<String, Long> attributes) {
        HashMap<String, String> result = new HashMap();
        for (Entry<String, Long> entry : attributes.entrySet()) {
            try {
                result.put((String) entry.getKey(), String.valueOf(entry.getValue()));
            } catch (ClassCastException e) {
                Log.e(TAG, "convertMaps exception");
                e.printStackTrace();
            }
        }
        return result;
    }

    public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) {
        if (this.mHyp != null) {
            this.mHyp.hypnusSetNotification(msg_src, msg_type, msg_time, pid, v0, v1);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public void hypnusSetScene(int pid, String processName) {
        if (this.mHyp != null) {
            this.mHyp.hypnusSetScene(pid, processName);
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
                        Log.w(TAG, String.format("Could not find package [%s]", new Object[]{name}), e);
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
        } else if (Hypnus.HYPNUS_STATICS_ON.booleanValue()) {
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
        } else {
            this.mHyp.hypnusSetAction(action, timeout, null);
            if (this.debug) {
                Log.d(TAG, "hypnus statics has closed");
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
        if (this.mHyp != null) {
            this.mHyp.hypnusSetBurst(tid, type, timeout);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public boolean isHypnusOK() {
        if (this.mHyp != null) {
            return this.mHyp.isHypnusOK();
        }
        Log.e(TAG, "mHyp is not initialized!");
        return false;
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
            this.mTimer.schedule(new WifiTimerTask(this, null), (long) timeout);
        }
        if (this.debug) {
            Log.i(TAG, "hypnusSetWifiPowerSaveMode is " + type);
        }
    }

    private void disableWifiPowerSaveMode() {
        if (this.mWifiManger != null) {
            this.mWifiManger.setPowerSavingMode(false);
        }
        if (this.debug) {
            Log.i(TAG, "disableWifiPowerSaveMode");
        }
    }

    private void enableWifiPowerSaveMode() {
        if (this.mWifiManger != null) {
            this.mWifiManger.setPowerSavingMode(true);
        }
        if (this.debug) {
            Log.i(TAG, "enableWifiPowerSaveMode");
        }
    }
}
