package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.util.Slog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.mediatek.common.MPlugin;
import com.mediatek.common.wifi.IWifiFwkExt;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiController extends StateMachine {
    private static final String ACTION_DEVICE_IDLE = "com.android.server.WifiManager.action.DEVICE_IDLE";
    private static final int BASE = 155648;
    static final int CMD_AIRPLANE_TOGGLED = 155657;
    static final int CMD_AP_START_FAILURE = 155661;
    static final int CMD_AP_STOPPED = 155663;
    static final int CMD_BATTERY_CHANGED = 155652;
    static final int CMD_DEFERRED_TOGGLE = 155659;
    static final int CMD_DEVICE_IDLE = 155653;
    static final int CMD_EMERGENCY_CALL_STATE_CHANGED = 155662;
    static final int CMD_EMERGENCY_MODE_CHANGED = 155649;
    static final int CMD_LOCKS_CHANGED = 155654;
    static final int CMD_RESTART_WIFI = 155665;
    private static final int CMD_RESTART_WIFI_CONTINUE = 155666;
    static final int CMD_SCAN_ALWAYS_MODE_CHANGED = 155655;
    static final int CMD_SCREEN_OFF = 155651;
    static final int CMD_SCREEN_ON = 155650;
    static final int CMD_SET_AP = 155658;
    static final int CMD_STA_START_FAILURE = 155664;
    static final int CMD_USER_PRESENT = 155660;
    static final int CMD_WIFI_TOGGLED = 155656;
    private static final boolean DBG = true;
    private static final long DEFAULT_IDLE_MS = 300000;
    private static final long DEFAULT_REENABLE_DELAY_MS = 500;
    private static final long DEFER_MARGIN_MS = 5;
    public static final int EPDG_UID = 0;
    private static final int IDLE_REQUEST = 0;
    private static final String TAG = "WifiController";
    static final int WIFI_TOGGLED_DELAYED_IPOOFF = 2;
    static final int WIFI_TOGGLED_IPOOFF = 1;
    private AlarmManager mAlarmManager;
    private ApEnabledState mApEnabledState = new ApEnabledState();
    private ApStaDisabledState mApStaDisabledState = new ApStaDisabledState();
    private Context mContext;
    private DefaultState mDefaultState = new DefaultState();
    private DeviceActiveState mDeviceActiveState = new DeviceActiveState();
    private boolean mDeviceIdle;
    private DeviceInactiveState mDeviceInactiveState = new DeviceInactiveState();
    private EcmState mEcmState = new EcmState();
    private FrameworkFacade mFacade;
    private boolean mFirstUserSignOnSeen = false;
    private FullHighPerfLockHeldState mFullHighPerfLockHeldState = new FullHighPerfLockHeldState();
    private FullLockHeldState mFullLockHeldState = new FullLockHeldState();
    private PendingIntent mIdleIntent;
    private long mIdleMillis;
    private long mLastOpenTime;
    NetworkInfo mNetworkInfo = new NetworkInfo(1, 0, "WIFI", "");
    private NoLockHeldState mNoLockHeldState = new NoLockHeldState();
    private int mPluggedType;
    private long mReEnableDelayMillis;
    private ScanOnlyLockHeldState mScanOnlyLockHeldState = new ScanOnlyLockHeldState();
    private boolean mScreenOff;
    private final WifiSettingsStore mSettingsStore;
    private int mSleepPolicy;
    private StaDisabledWithScanState mStaDisabledWithScanState = new StaDisabledWithScanState();
    private StaEnabledState mStaEnabledState = new StaEnabledState();
    private int mStayAwakeConditions;
    private final WorkSource mTmpWorkSource = new WorkSource();
    private IWifiFwkExt mWifiFwkExt;
    private boolean mWifiIpoOff = false;
    private final WifiLockManager mWifiLockManager;
    private final WifiStateMachine mWifiStateMachine;

    class ApEnabledState extends State {
        private State mPendingState = null;

        ApEnabledState() {
        }

        private State getNextWifiState() {
            if (WifiController.this.mSettingsStore.getWifiSavedState() == 1) {
                WifiController.this.mSettingsStore.setWifiSavedState(0);
                return WifiController.this.mDeviceActiveState;
            } else if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                return WifiController.this.mStaDisabledWithScanState;
            } else {
                return WifiController.this.mApStaDisabledState;
            }
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            this.mPendingState = null;
        }

        public boolean processMessage(Message msg) {
            Slog.d(WifiController.TAG, getName() + msg.toString() + "\n");
            switch (msg.what) {
                case WifiController.CMD_EMERGENCY_MODE_CHANGED /*155649*/:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*155662*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mWifiStateMachine.setHostApRunning(null, false);
                        this.mPendingState = WifiController.this.mEcmState;
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        WifiController.this.mWifiStateMachine.setHostApRunning(null, false);
                        this.mPendingState = WifiController.this.mDeviceActiveState;
                        break;
                    }
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                        WifiController.this.mWifiStateMachine.setHostApRunning(null, false);
                        this.mPendingState = WifiController.this.mApStaDisabledState;
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    int mWifiApState;
                    if (msg.arg1 != 0) {
                        if (msg.arg1 != 1 || this.mPendingState != WifiController.this.mDeviceActiveState) {
                            if (msg.arg1 == 1) {
                                mWifiApState = WifiController.this.mWifiStateMachine.syncGetWifiApState();
                                if (12 != mWifiApState && 13 != mWifiApState) {
                                    WifiController.this.sendMessageDelayed(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj), 100);
                                    this.mPendingState = WifiController.this.mApStaDisabledState;
                                    break;
                                }
                                Slog.d(WifiController.TAG, "Enabling SoftAp in progress, ignore CMD_SET_AP 1");
                                return true;
                            }
                        }
                        WifiController.this.loge("Defer the message, Wi-Fi and Wi-Fi Hotspot are switching quickly");
                        WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                        break;
                    }
                    mWifiApState = WifiController.this.mWifiStateMachine.syncGetWifiApState();
                    if (10 != mWifiApState && 11 != mWifiApState) {
                        if (this.mPendingState != WifiController.this.mDeviceActiveState) {
                            WifiController.this.mWifiStateMachine.setHostApRunning(null, false);
                            this.mPendingState = getNextWifiState();
                            break;
                        }
                        WifiController.this.loge("Defer the message, new turn off req should be handledin next time because wi-fi statemachine is under turning off AP");
                        WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                        break;
                    }
                    Slog.d(WifiController.TAG, "Disabling SoftAp in progress, ignore CMD_SET_AP 0");
                    return true;
                    break;
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                    WifiController.this.transitionTo(getNextWifiState());
                    break;
                case WifiController.CMD_AP_STOPPED /*155663*/:
                    if (this.mPendingState == null) {
                        this.mPendingState = getNextWifiState();
                    }
                    if (this.mPendingState != WifiController.this.mDeviceActiveState || !WifiController.this.mDeviceIdle) {
                        WifiController.this.loge("Receive CMD_AP_STOPPED and then ransition to mPendingState = " + this.mPendingState);
                        WifiController.this.transitionTo(this.mPendingState);
                        break;
                    }
                    WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                    break;
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class ApStaDisabledState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;

        ApStaDisabledState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            WifiController.this.mWifiStateMachine.setSupplicantRunning(false);
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = false;
            WifiController.this.mWifiStateMachine.clearANQPCache();
        }

        public boolean processMessage(Message msg) {
            boolean z = false;
            Slog.d(WifiController.TAG, getName() + msg.toString() + "\n");
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    boolean wifiIpoOff = msg.arg1 == 1;
                    boolean ipoStateChange = false;
                    if (WifiController.this.mWifiIpoOff != wifiIpoOff) {
                        ipoStateChange = true;
                    }
                    WifiController.this.mWifiIpoOff = wifiIpoOff;
                    if (!wifiIpoOff) {
                        if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                            if ((ipoStateChange || msg.what == WifiController.CMD_AIRPLANE_TOGGLED) && WifiController.this.mSettingsStore.isScanAlwaysAvailable() && !WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                                Slog.d(WifiController.TAG, "ipoStateChange = " + ipoStateChange + "isAirplaneModeOn= " + WifiController.this.mSettingsStore.isAirplaneModeOn());
                                WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                                break;
                            }
                        } else if (!doDeferEnable(msg)) {
                            if (!WifiController.this.mDeviceIdle) {
                                WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                                break;
                            }
                            WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                            break;
                        } else {
                            if (this.mHaveDeferredEnable) {
                                this.mDeferredEnableSerialNumber++;
                            }
                            if (!this.mHaveDeferredEnable) {
                                z = true;
                            }
                            this.mHaveDeferredEnable = z;
                            break;
                        }
                    }
                    Slog.d(WifiController.TAG, "ipooff  don't enable wifi\n");
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        if (msg.arg2 == 0) {
                            WifiController.this.mSettingsStore.setWifiSavedState(0);
                        }
                        WifiController.this.mWifiStateMachine.setHostApRunning((WifiConfiguration) msg.obj, true);
                        WifiController.this.transitionTo(WifiController.this.mApEnabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_DEFERRED_TOGGLE /*155659*/:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.log("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    }
                    WifiController.this.log("DEFERRED_TOGGLE ignored due to serial mismatch");
                    break;
                case WifiController.CMD_RESTART_WIFI_CONTINUE /*155666*/:
                    WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean doDeferEnable(Message msg) {
            long delaySoFar = SystemClock.elapsedRealtime() - this.mDisabledTimestamp;
            if (delaySoFar >= WifiController.this.mReEnableDelayMillis) {
                return false;
            }
            WifiController.this.log("WifiController msg " + msg + " deferred for " + (WifiController.this.mReEnableDelayMillis - delaySoFar) + "ms");
            Message deferredMsg = WifiController.this.obtainMessage(WifiController.CMD_DEFERRED_TOGGLE);
            deferredMsg.obj = Message.obtain(msg);
            int i = this.mDeferredEnableSerialNumber + 1;
            this.mDeferredEnableSerialNumber = i;
            deferredMsg.arg1 = i;
            WifiController.this.sendMessageDelayed(deferredMsg, (WifiController.this.mReEnableDelayMillis - delaySoFar) + WifiController.DEFER_MARGIN_MS);
            return true;
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message msg) {
            Slog.d(WifiController.TAG, getName() + msg.toString() + "\n");
            switch (msg.what) {
                case WifiController.CMD_EMERGENCY_MODE_CHANGED /*155649*/:
                case WifiController.CMD_LOCKS_CHANGED /*155654*/:
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                case WifiController.CMD_SET_AP /*155658*/:
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*155662*/:
                case WifiController.CMD_AP_STOPPED /*155663*/:
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                case WifiController.CMD_RESTART_WIFI /*155665*/:
                case WifiController.CMD_RESTART_WIFI_CONTINUE /*155666*/:
                    break;
                case WifiController.CMD_SCREEN_ON /*155650*/:
                    WifiController.this.mAlarmManager.cancel(WifiController.this.mIdleIntent);
                    WifiController.this.mScreenOff = false;
                    WifiController.this.mDeviceIdle = false;
                    WifiController.this.updateBatteryWorkSource();
                    break;
                case WifiController.CMD_SCREEN_OFF /*155651*/:
                    WifiController.this.mScreenOff = true;
                    if (!WifiController.this.shouldWifiStayAwake(WifiController.this.mPluggedType)) {
                        if (WifiController.this.mNetworkInfo.getDetailedState() != DetailedState.CONNECTED) {
                            WifiController.this.sendMessage(WifiController.CMD_DEVICE_IDLE);
                            break;
                        }
                        Slog.d(WifiController.TAG, "set idle timer: " + WifiController.this.mIdleMillis + " ms");
                        WifiController.this.mAlarmManager.set(0, System.currentTimeMillis() + WifiController.this.mIdleMillis, WifiController.this.mIdleIntent);
                        break;
                    }
                    break;
                case WifiController.CMD_BATTERY_CHANGED /*155652*/:
                    int pluggedType = msg.arg1;
                    Slog.d(WifiController.TAG, "battery changed pluggedType: " + pluggedType);
                    if (WifiController.this.mScreenOff && WifiController.this.shouldWifiStayAwake(WifiController.this.mPluggedType) && !WifiController.this.shouldWifiStayAwake(pluggedType)) {
                        long triggerTime = System.currentTimeMillis() + WifiController.this.mIdleMillis;
                        Slog.d(WifiController.TAG, "set idle timer for " + WifiController.this.mIdleMillis + "ms");
                        WifiController.this.mAlarmManager.set(0, triggerTime, WifiController.this.mIdleIntent);
                    }
                    WifiController.this.mPluggedType = pluggedType;
                    break;
                case WifiController.CMD_DEVICE_IDLE /*155653*/:
                    WifiController.this.mDeviceIdle = true;
                    WifiController.this.updateBatteryWorkSource();
                    break;
                case WifiController.CMD_DEFERRED_TOGGLE /*155659*/:
                    WifiController.this.log("DEFERRED_TOGGLE ignored due to state change");
                    break;
                case WifiController.CMD_USER_PRESENT /*155660*/:
                    WifiController.this.mFirstUserSignOnSeen = true;
                    break;
                default:
                    throw new RuntimeException("WifiController.handleMessage " + msg.what);
            }
            return true;
        }
    }

    class DeviceActiveState extends State {
        DeviceActiveState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            WifiController.this.mLastOpenTime = System.currentTimeMillis();
            WifiController.this.mWifiStateMachine.setOperationalMode(1);
            WifiController.this.mWifiStateMachine.setDriverStart(true);
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(false);
        }

        public boolean processMessage(Message msg) {
            Slog.d(WifiController.TAG, getName() + msg.toString() + "\n");
            if (msg.what == WifiController.CMD_DEVICE_IDLE) {
                if (!WifiController.this.mScreenOff) {
                    return true;
                }
                WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
            } else if (msg.what == WifiController.CMD_USER_PRESENT) {
                if (!WifiController.this.mFirstUserSignOnSeen) {
                    WifiController.this.mWifiStateMachine.reloadTlsNetworksAndReconnect();
                }
                WifiController.this.mFirstUserSignOnSeen = true;
                return true;
            } else if (msg.what == WifiController.CMD_RESTART_WIFI) {
                WifiController.this.deferMessage(WifiController.this.obtainMessage(WifiController.CMD_RESTART_WIFI_CONTINUE));
                WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                return true;
            }
            return false;
        }
    }

    class DeviceInactiveState extends State {
        DeviceInactiveState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
        }

        public boolean processMessage(Message msg) {
            Slog.d(WifiController.TAG, getName() + msg.toString() + "\n");
            switch (msg.what) {
                case WifiController.CMD_SCREEN_ON /*155650*/:
                    WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                    return false;
                case WifiController.CMD_LOCKS_CHANGED /*155654*/:
                    WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                    WifiController.this.updateBatteryWorkSource();
                    return true;
                default:
                    return false;
            }
        }
    }

    class EcmState extends State {
        private int mEcmEntryCount;

        EcmState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            WifiController.this.mWifiStateMachine.setSupplicantRunning(false);
            WifiController.this.mWifiStateMachine.clearANQPCache();
            this.mEcmEntryCount = 1;
        }

        public boolean processMessage(Message msg) {
            Slog.d(WifiController.TAG, getName() + msg.toString() + "\n");
            if (msg.what == WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED) {
                if (msg.arg1 == 1) {
                    this.mEcmEntryCount++;
                } else if (msg.arg1 == 0) {
                    decrementCountAndReturnToAppropriateState();
                }
                return true;
            } else if (msg.what != WifiController.CMD_EMERGENCY_MODE_CHANGED) {
                return false;
            } else {
                if (msg.arg1 == 1) {
                    this.mEcmEntryCount++;
                } else if (msg.arg1 == 0) {
                    decrementCountAndReturnToAppropriateState();
                }
                return true;
            }
        }

        private void decrementCountAndReturnToAppropriateState() {
            boolean exitEcm = false;
            if (this.mEcmEntryCount == 0) {
                WifiController.this.loge("mEcmEntryCount is 0; exiting Ecm");
                exitEcm = true;
            } else {
                int i = this.mEcmEntryCount - 1;
                this.mEcmEntryCount = i;
                if (i == 0) {
                    exitEcm = true;
                }
            }
            if (!exitEcm) {
                return;
            }
            if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                if (WifiController.this.mDeviceIdle) {
                    WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                } else {
                    WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                }
            } else if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
            } else {
                WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
            }
        }
    }

    class FullHighPerfLockHeldState extends State {
        FullHighPerfLockHeldState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            WifiController.this.mWifiStateMachine.setOperationalMode(1);
            WifiController.this.mWifiStateMachine.setDriverStart(true);
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(true);
        }
    }

    class FullLockHeldState extends State {
        FullLockHeldState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            WifiController.this.mWifiStateMachine.setOperationalMode(1);
            WifiController.this.mWifiStateMachine.setDriverStart(true);
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(false);
        }
    }

    class NoLockHeldState extends State {
        NoLockHeldState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            WifiController.this.mWifiStateMachine.setDriverStart(false);
        }
    }

    class ScanOnlyLockHeldState extends State {
        ScanOnlyLockHeldState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            WifiController.this.mWifiStateMachine.setOperationalMode(2);
            WifiController.this.mWifiStateMachine.setDriverStart(true);
        }
    }

    class StaDisabledWithScanState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;

        StaDisabledWithScanState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
            WifiController.this.mWifiStateMachine.setOperationalMode(3);
            WifiController.this.mWifiStateMachine.setDriverStart(true);
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = false;
            WifiController.this.mWifiStateMachine.clearANQPCache();
        }

        public boolean processMessage(Message msg) {
            Slog.d(WifiController.TAG, getName() + msg.toString() + "\n");
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    boolean wifiIpoOff = msg.arg1 == 1;
                    WifiController.this.mWifiIpoOff = wifiIpoOff;
                    if (!wifiIpoOff) {
                        if (WifiController.this.mSettingsStore.isWifiToggleEnabled() && !doDeferEnable(msg)) {
                            if (!WifiController.this.mDeviceIdle) {
                                WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                                break;
                            }
                            WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                            break;
                        }
                    }
                    WifiController.this.log("WifiIpoOff true disable wifi \n");
                    WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn() && !WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(0);
                        WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_DEFERRED_TOGGLE /*155659*/:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.logd("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    }
                    WifiController.this.log("DEFERRED_TOGGLE ignored due to serial mismatch");
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean doDeferEnable(Message msg) {
            long delaySoFar = SystemClock.elapsedRealtime() - this.mDisabledTimestamp;
            if (delaySoFar >= WifiController.this.mReEnableDelayMillis) {
                return false;
            }
            WifiController.this.log("WifiController msg " + msg + " deferred for " + (WifiController.this.mReEnableDelayMillis - delaySoFar) + "ms");
            Message deferredMsg = WifiController.this.obtainMessage(WifiController.CMD_DEFERRED_TOGGLE);
            deferredMsg.obj = Message.obtain(msg);
            int i = this.mDeferredEnableSerialNumber + 1;
            this.mDeferredEnableSerialNumber = i;
            deferredMsg.arg1 = i;
            WifiController.this.sendMessageDelayed(deferredMsg, (WifiController.this.mReEnableDelayMillis - delaySoFar) + WifiController.DEFER_MARGIN_MS);
            return true;
        }
    }

    class StaEnabledState extends State {
        StaEnabledState() {
        }

        public void enter() {
            Slog.d(WifiController.TAG, getName() + "\n");
            WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
        }

        public boolean processMessage(Message msg) {
            Slog.d(WifiController.TAG, getName() + msg.toString() + "\n");
            switch (msg.what) {
                case WifiController.CMD_EMERGENCY_MODE_CHANGED /*155649*/:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*155662*/:
                    boolean getConfigWiFiDisableInECBM = WifiController.this.mFacade.getConfigWiFiDisableInECBM(WifiController.this.mContext);
                    WifiController.this.log("WifiController msg " + msg + " getConfigWiFiDisableInECBM " + getConfigWiFiDisableInECBM);
                    if (msg.arg1 == 1 && getConfigWiFiDisableInECBM) {
                        WifiController.this.transitionTo(WifiController.this.mEcmState);
                        break;
                    }
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    boolean wifiIpoOff = msg.arg1 == 1;
                    WifiController.this.mWifiIpoOff = wifiIpoOff;
                    boolean needResendIpoEnable = msg.arg1 == 2;
                    if (!wifiIpoOff) {
                        if (!needResendIpoEnable) {
                            if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                                if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                                    WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                                    break;
                                }
                                if (System.currentTimeMillis() - WifiController.this.mLastOpenTime <= ((long) WifiController.this.mWifiStateMachine.getRomUpdateIntegerValue("BASIC_WIFI_OPEN_TIME", Integer.valueOf(300)).intValue()) * 1000) {
                                    WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                                    break;
                                }
                                WifiController.this.mWifiStateMachine.setOperationalMode(3);
                                WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                                WifiController.this.sendMessageDelayed(WifiController.this.obtainMessage(WifiController.CMD_WIFI_TOGGLED), 2000);
                                break;
                            }
                            int mWifiState = WifiController.this.mWifiStateMachine.syncGetWifiState();
                            Slog.e(WifiController.TAG, "Calling setWifiEnabled(true) in StaEnabledState!! mWifiState=" + mWifiState);
                            if (!(mWifiState == 2 || mWifiState == 3)) {
                                Slog.d(WifiController.TAG, "Mismatch in the state " + mWifiState);
                                WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
                                WifiController.this.mWifiStateMachine.setOperationalMode(1);
                                WifiController.this.mWifiStateMachine.setDriverStart(true);
                                WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(false);
                                break;
                            }
                        }
                        Slog.d(WifiController.TAG, "goto wifi off and send CMD_WIFI_TOGGLED");
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        WifiController.this.sendMessage(WifiController.CMD_WIFI_TOGGLED);
                        break;
                    }
                    WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    if (msg.arg1 == 1) {
                        Slog.d(WifiController.TAG, "setWifiDisabled is delayed. And airplane mode is off now");
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        Slog.d(WifiController.TAG, "goto wifi off and send CMD_AIRPLANE_TOGGLED");
                        WifiController.this.sendMessage(WifiController.CMD_AIRPLANE_TOGGLED);
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(1);
                        WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    WifiController(Context context, WifiStateMachine wsm, WifiSettingsStore wss, WifiLockManager wifiLockManager, Looper looper, FrameworkFacade f) {
        super(TAG, looper);
        this.mFacade = f;
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mSettingsStore = wss;
        this.mWifiLockManager = wifiLockManager;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mIdleIntent = this.mFacade.getBroadcast(this.mContext, 0, new Intent(ACTION_DEVICE_IDLE, null), 0);
        this.mWifiFwkExt = (IWifiFwkExt) MPlugin.createInstance(IWifiFwkExt.class.getName(), this.mContext);
        addState(this.mDefaultState);
        addState(this.mApStaDisabledState, this.mDefaultState);
        addState(this.mStaEnabledState, this.mDefaultState);
        addState(this.mDeviceActiveState, this.mStaEnabledState);
        addState(this.mDeviceInactiveState, this.mStaEnabledState);
        addState(this.mScanOnlyLockHeldState, this.mDeviceInactiveState);
        addState(this.mFullLockHeldState, this.mDeviceInactiveState);
        addState(this.mFullHighPerfLockHeldState, this.mDeviceInactiveState);
        addState(this.mNoLockHeldState, this.mDeviceInactiveState);
        addState(this.mStaDisabledWithScanState, this.mDefaultState);
        addState(this.mApEnabledState, this.mDefaultState);
        addState(this.mEcmState, this.mDefaultState);
        boolean isAirplaneModeOn = this.mSettingsStore.isAirplaneModeOn();
        boolean isWifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        boolean isScanningAlwaysAvailable = this.mSettingsStore.isScanAlwaysAvailable();
        log("isAirplaneModeOn = " + isAirplaneModeOn + ", isWifiEnabled = " + isWifiEnabled + ", isScanningAvailable = " + isScanningAlwaysAvailable);
        if (isScanningAlwaysAvailable) {
            setInitialState(this.mStaDisabledWithScanState);
        } else {
            setInitialState(this.mApStaDisabledState);
        }
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DEVICE_IDLE);
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(WifiController.ACTION_DEVICE_IDLE)) {
                    WifiController.this.sendMessage(WifiController.CMD_DEVICE_IDLE);
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    WifiController.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                    int state = intent.getIntExtra("wifi_state", 14);
                    if (state == 14) {
                        WifiController.this.loge("WifiControllerSoftAP start failed");
                        WifiController.this.sendMessage(WifiController.CMD_AP_START_FAILURE);
                    } else if (state == 11) {
                        WifiController.this.sendMessage(WifiController.CMD_AP_STOPPED);
                    }
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED") && intent.getIntExtra("wifi_state", 4) == 4) {
                    WifiController.this.loge("WifiControllerWifi turn on failed");
                    WifiController.this.sendMessage(WifiController.CMD_STA_START_FAILURE);
                }
            }
        }, new IntentFilter(filter));
        initializeAndRegisterForSettingsChange(looper);
    }

    private void initializeAndRegisterForSettingsChange(Looper looper) {
        Handler handler = new Handler(looper);
        readStayAwakeConditions();
        registerForStayAwakeModeChange(handler);
        readWifiIdleTime();
        registerForWifiIdleTimeChange(handler);
        if (this.mWifiFwkExt != null) {
            this.mWifiFwkExt.setCustomizedWifiSleepPolicy(this.mContext);
        }
        readWifiSleepPolicy();
        registerForWifiSleepPolicyChange(handler);
        readWifiReEnableDelay();
    }

    private void readStayAwakeConditions() {
        this.mStayAwakeConditions = this.mFacade.getIntegerSetting(this.mContext, "stay_on_while_plugged_in", 0);
    }

    private void readWifiIdleTime() {
        this.mIdleMillis = this.mFacade.getLongSetting(this.mContext, "wifi_idle_ms", DEFAULT_IDLE_MS);
    }

    private void readWifiSleepPolicy() {
        if (this.mWifiStateMachine.hasCustomizedAutoConnect()) {
            this.mSleepPolicy = this.mFacade.getIntegerSetting(this.mContext, "wifi_sleep_policy", 2);
        } else {
            this.mSleepPolicy = 2;
        }
    }

    private void readWifiReEnableDelay() {
        this.mReEnableDelayMillis = this.mFacade.getLongSetting(this.mContext, "wifi_reenable_delay", DEFAULT_REENABLE_DELAY_MS);
    }

    private void registerForStayAwakeModeChange(Handler handler) {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("stay_on_while_plugged_in"), false, new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                WifiController.this.readStayAwakeConditions();
            }
        });
    }

    private void registerForWifiIdleTimeChange(Handler handler) {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_idle_ms"), false, new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                WifiController.this.readWifiIdleTime();
            }
        });
    }

    private void registerForWifiSleepPolicyChange(Handler handler) {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_sleep_policy"), false, new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                WifiController.this.readWifiSleepPolicy();
            }
        });
    }

    private boolean shouldWifiStayAwake(int pluggedType) {
        Slog.d(TAG, "wifiSleepPolicy:" + this.mSleepPolicy + ", means: " + (this.mSleepPolicy == 0 ? "Default, check mStayAwakeConditions" : "") + (this.mSleepPolicy == 1 ? "Never sleep while plugged" : "") + (this.mSleepPolicy == 2 ? "Never sleep" : ""));
        if (this.mSleepPolicy == 2) {
            return true;
        }
        if (this.mSleepPolicy != 1 || pluggedType == 0) {
            return shouldDeviceStayAwake(pluggedType);
        }
        return true;
    }

    private boolean shouldDeviceStayAwake(int pluggedType) {
        Slog.d(TAG, "mStayAwakeConditions: " + this.mStayAwakeConditions + ", pluggedType: " + pluggedType);
        if ((this.mStayAwakeConditions & pluggedType) != 0) {
            return true;
        }
        return false;
    }

    private void updateBatteryWorkSource() {
        this.mTmpWorkSource.clear();
        if (this.mDeviceIdle) {
            this.mTmpWorkSource.add(this.mWifiLockManager.createMergedWorkSource());
        }
        this.mWifiStateMachine.updateBatteryWorkSource(this.mTmpWorkSource);
    }

    private void checkLocksAndTransitionWhenDeviceIdle() {
        switch (this.mWifiLockManager.getStrongestLockMode()) {
            case 0:
                if (this.mSettingsStore.isScanAlwaysAvailable()) {
                    transitionTo(this.mScanOnlyLockHeldState);
                    return;
                } else {
                    transitionTo(this.mNoLockHeldState);
                    return;
                }
            case 1:
                transitionTo(this.mFullLockHeldState);
                return;
            case 2:
                transitionTo(this.mScanOnlyLockHeldState);
                return;
            case 3:
                transitionTo(this.mFullHighPerfLockHeldState);
                return;
            default:
                return;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        pw.println("mScreenOff " + this.mScreenOff);
        pw.println("mDeviceIdle " + this.mDeviceIdle);
        pw.println("mPluggedType " + this.mPluggedType);
        pw.println("mIdleMillis " + this.mIdleMillis);
        pw.println("mSleepPolicy " + this.mSleepPolicy);
    }
}
