package com.android.server.connectivity.oppo;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.Log;
import com.android.server.ConnectivityService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkNotificationManager;
import java.util.HashMap;

public class OppoAutoConCaptivePortalControl {
    private static final int CAPTIVE_DIRECT_TO_BROWSER = 1;
    private static final int CAPTIVE_DIRECT_TO_NOTHING = 3;
    private static final int CAPTIVE_DIRECT_TO_NOTIFY = 2;
    private static boolean DEBUG = true;
    private static final String DIRECT_TO_BROWSER = "direct_to_browser";
    private static final int EVENT_NETWORK_MONITOR_BROWSER = 5;
    private static final int EVENT_NETWORK_MONITOR_CAPTIVE = 4;
    private static final int EVENT_USER_PRESENT_BROADCAST = 6;
    private static final int EVENT_WIFI_ASSISTANT_ROMUPDATE = 2;
    private static final int EVENT_WIFI_ASSISTANT_SWITCH = 1;
    private static final int EVENT_WIFI_SETTING_ENTER = 3;
    public static final int NETWORK_TEST_RESUL_CAPTIVE_PORTAL = 1;
    public static final int NETWORK_TEST_RESUL_ENTER_BROWSER = 2;
    private static final int RETURN_AUTO_CAPTIVE_CODE = 1;
    private static final int RETURN_NORMAL_CODE = 0;
    private static final int STATE_CAPTIVE = 4;
    private static final int STATE_INVALID = -1;
    private static final int STATE_MAX = 5;
    private static final int STATE_SCRREN_LOCK = 2;
    private static final int STATE_SHOW_NOTIFY = 3;
    private static final int STATE_STARTED = 1;
    private static final String TAG = "OppoAutoConCaptivePortalControl";
    private static final String WIFI_ASSISTANT_ROMUPDATE = "rom.update.wifi.assistant";
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final String WIFI_SETTING_ENTER = "oppo.comm.wifi.monitor.setting";
    private final Context mContext;
    private ConnectivityService mCs;
    private final InternalHandler mHandler;
    private final HandlerThread mHandlerThread;
    private KeyguardManager mKeyguardManager;
    private NetworkAgentInfo mNai = null;
    private Object mNaiLock = new Object();
    private HashMap<Integer, Boolean> mNetworkCaptiveValues = new HashMap<>();
    private NetworkNotificationManager mNotifier;
    private PowerManager mPowerManager;
    private final SettingsObserver mSettingsObserver;
    private int mState;
    private BroadcastReceiver mUserPresentReceiver = null;
    private WifiManager mWifiManager;
    private WifiRomUpdateHelper mWifiRomUpdateForNet;

    public OppoAutoConCaptivePortalControl(Context ctx, PowerManager powerManager, NetworkNotificationManager notifier) {
        setState(-1);
        this.mContext = ctx;
        this.mPowerManager = powerManager;
        this.mNotifier = notifier;
        this.mWifiRomUpdateForNet = WifiRomUpdateHelper.getInstance(this.mContext);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHandlerThread = new HandlerThread("OppoAutoConCaptivePortalControlThread");
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        this.mSettingsObserver = new SettingsObserver(this.mContext, this.mHandler);
    }

    public void checkNetworkTestStatus(NetworkAgentInfo nai, int flag, Bundle redirectUrlBundle) {
        if (nai == null || !(nai.networkInfo == null || nai.networkInfo.getType() == 1)) {
            logd("checkNetworkTestStatus invalid nai");
            notifyPrepareCaptiveResponse(nai, 0);
        } else if (flag == 1) {
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessage(internalHandler.obtainMessage(4, nai));
        } else if (flag == 2) {
            InternalHandler internalHandler2 = this.mHandler;
            internalHandler2.sendMessage(internalHandler2.obtainMessage(5));
        } else {
            logd("Err NetworkTest");
            notifyPrepareCaptiveResponse(nai, 0);
        }
    }

    public boolean needShowOppoNotify(int netId) {
        boolean z;
        synchronized (this.mNaiLock) {
            z = true;
            if (getState() != 1 || this.mNai == null || netId != this.mNai.network.netId) {
                z = false;
            }
        }
        return z;
    }

    public void setAutoCaptiveControlStateToNotify() {
        boolean mEnterWifiSetting = true;
        if (getState() != 1) {
            logd("setAutoCaptiveControlStateToNotify invalid state:" + getState());
            return;
        }
        setState(3);
        if (Settings.Global.getInt(this.mContext.getContentResolver(), WIFI_SETTING_ENTER, 0) != 1) {
            mEnterWifiSetting = false;
        }
        if (mEnterWifiSetting) {
            startCaptivePortalApp();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAutoCaptiveControlStateToCaptive() {
        synchronized (this.mNaiLock) {
            if (getState() == 3) {
                if (this.mNai != null) {
                    clearAllNotification();
                    setState(4);
                    resetRegisterInfo();
                    notifyNetworkAgent(3, new Bundle());
                    return;
                }
            }
            logd("setAutoCaptiveControlStateToCaptive invalid state:" + getState());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean enterAutoCaptiveControl() {
        if (getState() != -1) {
            logd("enterAutoCaptiveControl invalid state:" + getState());
            return false;
        }
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        }
        if (!this.mPowerManager.isScreenOn() || this.mKeyguardManager.inKeyguardRestrictedInputMode()) {
            setCaptiveStyleInDeviceConfig(Integer.toString(3));
            setState(2);
            registerUserPresentReceiver();
        } else {
            setCaptiveStyleInDeviceConfig(Integer.toString(2));
            setState(1);
            setCaptivePortalMode(1);
        }
        registerSettingsCallbacks();
        return true;
    }

    public void exitAutoCaptiveControl(int netId) {
        HashMap<Integer, Boolean> hashMap = this.mNetworkCaptiveValues;
        if (hashMap != null && hashMap.containsKey(Integer.valueOf(netId))) {
            this.mNetworkCaptiveValues.remove(Integer.valueOf(netId));
        }
        synchronized (this.mNaiLock) {
            if (this.mNai != null && netId == this.mNai.network.netId) {
                if (-1 != getState()) {
                    clearAllNotification();
                    setState(-1);
                    resetRegisterInfo();
                    return;
                }
            }
            logd("exitAutoCaptiveControl invalid netId:" + netId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyNetworkAgent(int status, Bundle redirectUrlBundle) {
        synchronized (this.mNaiLock) {
            if (this.mNai != null) {
                this.mNai.asyncChannel.sendMessage(528391, status, 0, redirectUrlBundle);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyNetworkMonitorReEvaluation() {
        synchronized (this.mNaiLock) {
            if (this.mNai != null) {
                if (this.mNai.networkMonitor() != null) {
                    this.mNai.networkMonitor().forceReevaluation(1000);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startCaptivePortalApp() {
        synchronized (this.mNaiLock) {
            if (this.mNai != null) {
                if (this.mNai.networkMonitor() != null) {
                    this.mNai.networkMonitor().launchCaptivePortalApp();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPrepareCaptiveResponse(NetworkAgentInfo nai, int returnCode) {
        if (nai != null && nai.networkMonitor() != null) {
            nai.networkMonitor().notifyPrepareCaptiveResponse(returnCode);
        }
    }

    private void registerSettingsCallbacks() {
        this.mSettingsObserver.observe(Settings.Global.getUriFor(WIFI_AUTO_CHANGE_ACCESS_POINT), 1);
        this.mSettingsObserver.observe(Settings.Global.getUriFor(WIFI_ASSISTANT_ROMUPDATE), 2);
        this.mSettingsObserver.observe(Settings.Global.getUriFor(WIFI_SETTING_ENTER), 3);
    }

    private void unRegisterSettingsCallbacks() {
        this.mSettingsObserver.unObserve(Settings.Global.getUriFor(WIFI_AUTO_CHANGE_ACCESS_POINT));
        this.mSettingsObserver.unObserve(Settings.Global.getUriFor(WIFI_ASSISTANT_ROMUPDATE));
        this.mSettingsObserver.unObserve(Settings.Global.getUriFor(WIFI_SETTING_ENTER));
    }

    private void registerUserPresentReceiver() {
        this.mUserPresentReceiver = new BroadcastReceiver() {
            /* class com.android.server.connectivity.oppo.OppoAutoConCaptivePortalControl.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                OppoAutoConCaptivePortalControl.this.mHandler.sendMessage(OppoAutoConCaptivePortalControl.this.mHandler.obtainMessage(6));
            }
        };
        BroadcastReceiver broadcastReceiver = this.mUserPresentReceiver;
        if (broadcastReceiver != null) {
            this.mContext.registerReceiverAsUser(broadcastReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.USER_PRESENT"), null, null);
        }
    }

    private void unregisterUserPresentReceiver() {
        BroadcastReceiver broadcastReceiver = this.mUserPresentReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mUserPresentReceiver = null;
        }
    }

    /* access modifiers changed from: private */
    public class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean mEnterWifiSetting = false;
            switch (msg.what) {
                case 1:
                    if (Settings.Global.getInt(OppoAutoConCaptivePortalControl.this.mContext.getContentResolver(), OppoAutoConCaptivePortalControl.WIFI_AUTO_CHANGE_ACCESS_POINT, 1) == 1) {
                        mEnterWifiSetting = true;
                    }
                    if (!mEnterWifiSetting && OppoAutoConCaptivePortalControl.this.getState() != -1) {
                        OppoAutoConCaptivePortalControl.this.logd("WlanAssist Switch Disable for Disconnet AP");
                        OppoAutoConCaptivePortalControl.this.notifyNetworkAgent(2, new Bundle());
                        return;
                    }
                    return;
                case 2:
                    if (Settings.Global.getInt(OppoAutoConCaptivePortalControl.this.mContext.getContentResolver(), OppoAutoConCaptivePortalControl.WIFI_ASSISTANT_ROMUPDATE, 1) == 1) {
                        mEnterWifiSetting = true;
                    }
                    if (!mEnterWifiSetting && OppoAutoConCaptivePortalControl.this.getState() != -1) {
                        OppoAutoConCaptivePortalControl.this.logd("WlanAssist RUS Disable for Disconnet AP");
                        OppoAutoConCaptivePortalControl.this.notifyNetworkAgent(2, new Bundle());
                        return;
                    }
                    return;
                case 3:
                    if (Settings.Global.getInt(OppoAutoConCaptivePortalControl.this.mContext.getContentResolver(), OppoAutoConCaptivePortalControl.WIFI_SETTING_ENTER, 0) == 1) {
                        mEnterWifiSetting = true;
                    }
                    if (mEnterWifiSetting && OppoAutoConCaptivePortalControl.this.getState() == 3) {
                        OppoAutoConCaptivePortalControl.this.logd("WifiSetting Enter for Direct Browser");
                        OppoAutoConCaptivePortalControl.this.startCaptivePortalApp();
                        return;
                    }
                    return;
                case 4:
                    NetworkAgentInfo nai = (NetworkAgentInfo) msg.obj;
                    if (!OppoAutoConCaptivePortalControl.this.needAutoCaptiveControl() || !OppoAutoConCaptivePortalControl.this.enterAutoCaptiveControl()) {
                        OppoAutoConCaptivePortalControl.this.notifyPrepareCaptiveResponse(nai, 0);
                    } else {
                        synchronized (OppoAutoConCaptivePortalControl.this.mNaiLock) {
                            OppoAutoConCaptivePortalControl.this.mNai = nai;
                        }
                        OppoAutoConCaptivePortalControl.this.notifyPrepareCaptiveResponse(nai, 1);
                    }
                    if (OppoAutoConCaptivePortalControl.this.mNetworkCaptiveValues != null && !OppoAutoConCaptivePortalControl.this.mNetworkCaptiveValues.containsKey(Integer.valueOf(nai.network.netId))) {
                        OppoAutoConCaptivePortalControl.this.mNetworkCaptiveValues.put(Integer.valueOf(nai.network.netId), true);
                        return;
                    }
                    return;
                case 5:
                    OppoAutoConCaptivePortalControl.this.setAutoCaptiveControlStateToCaptive();
                    return;
                case 6:
                    if (OppoAutoConCaptivePortalControl.this.getState() == 2) {
                        OppoAutoConCaptivePortalControl.this.logd("ForeceReEvaluate from User Present");
                        OppoAutoConCaptivePortalControl.this.notifyNetworkMonitorReEvaluation();
                        OppoAutoConCaptivePortalControl.this.setState(-1);
                        OppoAutoConCaptivePortalControl.this.resetRegisterInfo();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap = new HashMap<>();

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mContext = context;
            this.mHandler = handler;
        }

        /* access modifiers changed from: package-private */
        public void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        /* access modifiers changed from: package-private */
        public void unObserve(Uri uri) {
            this.mUriEventMap.remove(uri);
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            Log.e(OppoAutoConCaptivePortalControl.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
                return;
            }
            Log.e(OppoAutoConCaptivePortalControl.TAG, "No matching event to send for URI= " + uri);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getState() {
        return this.mState;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setState(int newState) {
        logd("set auto captive State = " + newState + " [" + newState + "]");
        if (newState >= 5) {
            logd("invalid state:" + newState);
            return;
        }
        if (newState == -1) {
            synchronized (this.mNaiLock) {
                this.mNai = null;
            }
        }
        this.mState = newState;
    }

    private void setCaptiveStyleInDeviceConfig(String mValue) {
        try {
            DeviceConfig.setProperty("connectivity", DIRECT_TO_BROWSER, mValue, false);
        } catch (SecurityException e) {
            logd("WRITE_DEVICE_CONFIG permission fail");
        }
        logd("setCaptiveStyleInDeviceConfig:" + mValue);
    }

    private void setCaptivePortalMode(int mode) {
        ContentResolver cr = this.mContext.getContentResolver();
        if (cr != null) {
            Settings.Global.putInt(cr, "captive_portal_mode", mode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetRegisterInfo() {
        setCaptiveStyleInDeviceConfig(Integer.toString(1));
        unRegisterSettingsCallbacks();
        unregisterUserPresentReceiver();
    }

    private void clearAllNotification() {
        synchronized (this.mNaiLock) {
            if (!(this.mNai == null || this.mNotifier == null)) {
                this.mNotifier.clearNotification(this.mNai.network.netId);
            }
        }
    }

    private Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateForNet;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    private boolean isAutoConnectCaptiveNoitfy() {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateForNet;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getBooleanValue("OPPO_WIFI_AUTO_CONNECT_PORTAL_FEATURE", true);
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean needAutoCaptiveControl() {
        WifiManager wifiManager;
        if (!isSwitchEnable() || !isAutoConnectCaptiveNoitfy() || (wifiManager = this.mWifiManager) == null || !wifiManager.getNetworkEverCaptiveState()) {
            return false;
        }
        logd("needAutoCaptiveControl true");
        return true;
    }

    public void setAutoCaptiveSucStatus(int netId) {
        HashMap<Integer, Boolean> hashMap = this.mNetworkCaptiveValues;
        if (hashMap != null && hashMap.containsKey(Integer.valueOf(netId))) {
            this.mNetworkCaptiveValues.remove(Integer.valueOf(netId));
            WifiManager wifiManager = this.mWifiManager;
            if (wifiManager != null) {
                wifiManager.setNetworkCaptiveState(true);
            }
        }
    }

    private boolean isSwitchEnable() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        boolean isSwitchEnable = true;
        if (Settings.Global.getInt(context.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
            isSwitchEnable = false;
        }
        return isSwitchEnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String str) {
        if (DEBUG) {
            Log.d("OppoAutoConCaptivePortalControl/", str);
        }
    }
}
