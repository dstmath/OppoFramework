package com.android.server.connectivity.oppo;

import android.app.KeyguardManager;
import android.app.PendingIntent;
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

public class OppoDualWifiSta2CaptivePortalControl {
    private static final int CAPTIVE_DELAY_MS = 10000;
    private static final int CAPTIVE_DIRECT_TO_BROWSER = 1;
    private static final int CAPTIVE_DIRECT_TO_NOTHING = 3;
    private static final int CAPTIVE_DIRECT_TO_NOTIFY = 2;
    private static boolean DEBUG = true;
    private static final int DELAY_SHOW_NOITFICATION_MS = 10000;
    private static final String DIRECT_TO_BROWSER = "direct_to_browser";
    private static final int EVENT_DELAY_SHOW_NOITFICATION = 4;
    private static final int EVENT_NETWORK_MONITOR_BROWSER = 2;
    private static final int EVENT_NETWORK_MONITOR_CAPTIVE = 1;
    private static final int EVENT_USER_PRESENT_BROADCAST = 3;
    private static final String KEY_NETWORK_MONITOR_AVAILABLE = "oppo.comm.network.monitor.available";
    private static final String KEY_NETWORK_MONITOR_PORTAL = "oppo.comm.network.monitor.portal";
    private static final String KEY_NETWORK_MONITOR_SSID = "oppo.comm.network.monitor.ssid";
    public static final int NETWORK_TEST_RESUL_CAPTIVE_PORTAL = 1;
    public static final int NETWORK_TEST_RESUL_ENTER_BROWSER = 2;
    private static final int RETURN_AUTO_CAPTIVE_CODE = 1;
    private static final int RETURN_NORMAL_CODE = 0;
    private static final int START_APP_DELAY_MS = 1500;
    private static final int STATE_CAPTIVE = 4;
    private static final int STATE_INVALID = -1;
    private static final int STATE_MAX = 5;
    private static final int STATE_SCRREN_LOCK = 2;
    private static final int STATE_SHOW_NOTIFY = 3;
    private static final int STATE_STARTED = 1;
    private static final String TAG = "OppoDualWifiSta2CaptivePortalControl";
    private static final int TEN_MINUTES_MS = 600000;
    private static boolean mIsClickedPreNotification = false;
    private static String mPreShowNotificationConfigKey = null;
    private static long mPreShowNotificationTime = 0;
    private final Context mContext;
    private ConnectivityService mCs;
    private boolean mDelayShowNotification = true;
    private int mDelayShowNotificationTime = 10000;
    private int mDupShowNotificationCoolTime = TEN_MINUTES_MS;
    private boolean mEverDispalyAppLogin = false;
    /* access modifiers changed from: private */
    public final InternalHandler mHandler;
    private final HandlerThread mHandlerThread;
    private KeyguardManager mKeyguardManager;
    /* access modifiers changed from: private */
    public NetworkAgentInfo mNai = null;
    /* access modifiers changed from: private */
    public Object mNaiLock = new Object();
    /* access modifiers changed from: private */
    public HashMap<Integer, Boolean> mNetworkCaptiveValues = new HashMap<>();
    private NetworkNotificationManager mNotifier;
    private PowerManager mPowerManager;
    private boolean mRestrictDupShowNotification = true;
    private int mState;
    private BroadcastReceiver mUserPresentReceiver = null;
    private WifiManager mWifiManager;
    private WifiRomUpdateHelper mWifiRomUpdateForNet;

    public OppoDualWifiSta2CaptivePortalControl(Context ctx, PowerManager powerManager, NetworkNotificationManager notifier) {
        setState(-1);
        this.mContext = ctx;
        this.mPowerManager = powerManager;
        this.mNotifier = notifier;
        this.mWifiRomUpdateForNet = WifiRomUpdateHelper.getInstance(this.mContext);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHandlerThread = new HandlerThread("OppoDualWifiSta2CaptivePortalControlThread");
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
    }

    public void checkNetworkTestStatus(NetworkAgentInfo nai, int flag, Bundle redirectUrlBundle) {
        if (nai == null || !(nai.networkInfo == null || nai.networkInfo.getType() == 1)) {
            logd("checkNetworkTestStatus invalid nai");
        }
        if (flag == 1) {
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessage(internalHandler.obtainMessage(1, nai));
        } else if (flag == 2) {
            InternalHandler internalHandler2 = this.mHandler;
            internalHandler2.sendMessage(internalHandler2.obtainMessage(2));
        } else {
            logd("Err NetworkTest");
            notifyPrepareCaptiveResponse(nai, 0);
        }
    }

    public void setAutoCaptiveControlStateToNotify(NetworkAgentInfo nai) {
        if (nai == null) {
            logd("setAutoCaptiveControlStateToNotify nai=null !!");
            return;
        }
        setState(3);
        Settings.Global.putString(this.mContext.getContentResolver(), KEY_NETWORK_MONITOR_SSID, nai.networkInfo.getExtraInfo());
        Settings.Global.putInt(this.mContext.getContentResolver(), KEY_NETWORK_MONITOR_AVAILABLE, 0);
        Settings.Global.putInt(this.mContext.getContentResolver(), KEY_NETWORK_MONITOR_PORTAL, 1);
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

    public void showCaptiveNotify(NetworkAgentInfo nai, PendingIntent intent, int netId) {
        setShowParam();
        if (this.mDelayShowNotification) {
            synchronized (this.mNaiLock) {
                this.mNai = nai;
            }
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessageDelayed(internalHandler.obtainMessage(4, netId, 0, intent), (long) this.mDelayShowNotificationTime);
            return;
        }
        showNotification(nai, intent, netId);
        setAutoCaptiveControlStateToNotify(nai);
    }

    /* access modifiers changed from: private */
    public void showNotification(NetworkAgentInfo nai, PendingIntent intent, int netId) {
        NetworkNotificationManager networkNotificationManager;
        if (nai == null) {
            logd("ShowNotification from Unknown NetworkMonitor");
        } else if (isDupShowNotificationWithinCoolTime()) {
            logd("DupShowNotificationWithin30Mins");
        } else {
            updatePreShowNotificationTime();
            updatePreShowNotificationConfigKey();
            updatePreNotificationClickedFlag(false);
            if (!nai.networkMisc.provisioningNotificationDisabled && (networkNotificationManager = this.mNotifier) != null) {
                networkNotificationManager.showOppoNotification(netId, NetworkNotificationManager.NotificationType.SIGN_IN, nai, null, intent, true);
                logd("Show Notifying in Phone");
            }
        }
    }

    /* access modifiers changed from: private */
    public void setAutoCaptiveControlStateToCaptive() {
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
    public boolean enterAutoCaptiveControl() {
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
        return true;
    }

    public void exitCaptiveControl(int netId) {
        HashMap<Integer, Boolean> hashMap = this.mNetworkCaptiveValues;
        if (hashMap != null && hashMap.containsKey(Integer.valueOf(netId))) {
            this.mNetworkCaptiveValues.remove(Integer.valueOf(netId));
        }
        synchronized (this.mNaiLock) {
            if (this.mNai != null && netId == this.mNai.network.netId) {
                if (-1 != getState()) {
                    this.mNai = null;
                    clearAllNotification();
                    setState(-1);
                    resetRegisterInfo();
                    return;
                }
            }
            logd("exitAutoCaptiveControl invalid netId:" + netId);
        }
    }

    private void notifyNetworkAgent(int status, Bundle redirectUrlBundle) {
        synchronized (this.mNaiLock) {
            if (this.mNai != null) {
                this.mNai.asyncChannel.sendMessage(528391, status, 0, redirectUrlBundle);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyNetworkMonitorReEvaluation() {
        synchronized (this.mNaiLock) {
            if (!(this.mNai == null || this.mNai.networkMonitor() == null)) {
                this.mNai.networkMonitor().forceReevaluation(1000);
            }
        }
    }

    private void startCaptivePortalApp() {
        synchronized (this.mNaiLock) {
            if (!(this.mNai == null || this.mNai.networkMonitor() == null)) {
                this.mNai.networkMonitor().launchCaptivePortalApp();
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyPrepareCaptiveResponse(NetworkAgentInfo nai, int returnCode) {
        if (nai != null && nai.networkMonitor() != null) {
            nai.networkMonitor().notifyPrepareCaptiveResponse(returnCode);
        }
    }

    private void registerUserPresentReceiver() {
        this.mUserPresentReceiver = new BroadcastReceiver() {
            /* class com.android.server.connectivity.oppo.OppoDualWifiSta2CaptivePortalControl.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                OppoDualWifiSta2CaptivePortalControl.this.mHandler.sendMessage(OppoDualWifiSta2CaptivePortalControl.this.mHandler.obtainMessage(3));
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
            int i = msg.what;
            if (i == 1) {
                NetworkAgentInfo nai = (NetworkAgentInfo) msg.obj;
                if (OppoDualWifiSta2CaptivePortalControl.this.enterAutoCaptiveControl()) {
                    synchronized (OppoDualWifiSta2CaptivePortalControl.this.mNaiLock) {
                        NetworkAgentInfo unused = OppoDualWifiSta2CaptivePortalControl.this.mNai = nai;
                    }
                    OppoDualWifiSta2CaptivePortalControl.this.notifyPrepareCaptiveResponse(nai, 1);
                } else {
                    OppoDualWifiSta2CaptivePortalControl.this.notifyPrepareCaptiveResponse(nai, 0);
                }
                if (OppoDualWifiSta2CaptivePortalControl.this.mNetworkCaptiveValues != null && !OppoDualWifiSta2CaptivePortalControl.this.mNetworkCaptiveValues.containsKey(Integer.valueOf(nai.network.netId))) {
                    OppoDualWifiSta2CaptivePortalControl.this.mNetworkCaptiveValues.put(Integer.valueOf(nai.network.netId), true);
                }
            } else if (i == 2) {
                OppoDualWifiSta2CaptivePortalControl.this.setAutoCaptiveControlStateToCaptive();
            } else if (i != 3) {
                if (i == 4) {
                    int netId = msg.arg1;
                    synchronized (OppoDualWifiSta2CaptivePortalControl.this.mNaiLock) {
                        if (OppoDualWifiSta2CaptivePortalControl.this.needShowOppoNotify(netId)) {
                            OppoDualWifiSta2CaptivePortalControl.this.showNotification(OppoDualWifiSta2CaptivePortalControl.this.mNai, (PendingIntent) msg.obj, netId);
                            OppoDualWifiSta2CaptivePortalControl.this.setAutoCaptiveControlStateToNotify(OppoDualWifiSta2CaptivePortalControl.this.mNai);
                        }
                    }
                }
            } else if (OppoDualWifiSta2CaptivePortalControl.this.getState() == 2) {
                OppoDualWifiSta2CaptivePortalControl.this.logd("ForeceReEvaluate from User Present");
                OppoDualWifiSta2CaptivePortalControl.this.notifyNetworkMonitorReEvaluation();
                OppoDualWifiSta2CaptivePortalControl.this.setState(-1);
                OppoDualWifiSta2CaptivePortalControl.this.resetRegisterInfo();
            }
        }
    }

    private static class SettingsObserver extends ContentObserver {
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
            Log.e(OppoDualWifiSta2CaptivePortalControl.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
                return;
            }
            Log.e(OppoDualWifiSta2CaptivePortalControl.TAG, "No matching event to send for URI= " + uri);
        }
    }

    /* access modifiers changed from: private */
    public int getState() {
        return this.mState;
    }

    /* access modifiers changed from: private */
    public void setState(int newState) {
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
    public void resetRegisterInfo() {
        setCaptiveStyleInDeviceConfig(Integer.toString(1));
        updatePreNotificationClickedFlag(true);
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

    private boolean getRomUpdateBooleanValue(String key, boolean defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateForNet;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getBooleanValue(key, defaultVal);
        }
        return defaultVal;
    }

    private boolean isDupShowNotificationWithinCoolTime() {
        if (!this.mRestrictDupShowNotification) {
            return false;
        }
        if (mPreShowNotificationTime == 0) {
            logd("the first showNotification");
            return false;
        }
        String curShowNotificationConfigKey = this.mWifiManager.getOppoSta2CurConfigKey();
        logd("curShowNotificationConfigKey = " + curShowNotificationConfigKey);
        String str = mPreShowNotificationConfigKey;
        if (str != null && str.equals(curShowNotificationConfigKey)) {
            long curShowNotificationTime = System.currentTimeMillis();
            logd("step isDupShowNotificationWithinCoolTime, curShowNotificationConfigKey = " + curShowNotificationConfigKey + "curShowNotificationTime = " + curShowNotificationTime + " mPreShowNotificationTime =" + mPreShowNotificationTime);
            if (curShowNotificationTime - mPreShowNotificationTime > ((long) this.mDupShowNotificationCoolTime) || mIsClickedPreNotification) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void updatePreShowNotificationTime() {
        mPreShowNotificationTime = System.currentTimeMillis();
        logd("the updatePreShowNotificationTime ShowNotificationTime = " + mPreShowNotificationTime);
    }

    private void updatePreShowNotificationConfigKey() {
        mPreShowNotificationConfigKey = this.mWifiManager.getOppoSta2CurConfigKey();
        logd("the updatePreShowNotificationConfigKey configKey = " + mPreShowNotificationConfigKey);
    }

    private void updatePreNotificationClickedFlag(boolean hasClickedOrNot) {
        mIsClickedPreNotification = hasClickedOrNot;
        logd("the updatePreNotificationClickedFlag hasClickedOrNot = " + hasClickedOrNot);
    }

    private void setShowParam() {
        this.mRestrictDupShowNotification = getRomUpdateBooleanValue("OPPO_DUAL_STA_RESTRICT_DUP_NOTIFICATION", true);
        this.mDupShowNotificationCoolTime = getRomUpdateIntegerValue("OPPO_DUAL_STA_DUP_NOTIFICATION_PERIOD", Integer.valueOf((int) TEN_MINUTES_MS)).intValue();
        this.mDelayShowNotification = getRomUpdateBooleanValue("OPPO_DUAL_STA_NOTIFICATION_SHOW_DELAY", true);
        this.mDelayShowNotificationTime = getRomUpdateIntegerValue("OPPO_DUAL_STA_NOTIFICATION_SHOW_DELAY_MS", 10000).intValue();
    }

    /* access modifiers changed from: private */
    public void logd(String str) {
        if (DEBUG) {
            Log.d("OppoDualWifiSta2CaptivePortalControl/", str);
        }
    }
}
