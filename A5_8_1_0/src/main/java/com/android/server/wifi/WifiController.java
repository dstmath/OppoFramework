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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.util.Slog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class WifiController extends StateMachine {
    private static final String ACTION_DEVICE_IDLE = "com.android.server.WifiManager.action.DEVICE_IDLE";
    private static final int BASE = 155648;
    static final int CMD_AIRPLANE_TOGGLED = 155657;
    private static final int CMD_AP_STARTED = 155669;
    static final int CMD_AP_START_FAILURE = 155661;
    static final int CMD_AP_STOPPED = 155663;
    private static final int CMD_AP_STOP_FAILURE = 155670;
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
    private static final int CMD_WIFI_DISABLED = 155668;
    private static final int CMD_WIFI_ENABLED = 155667;
    private static final int CMD_WIFI_STOP_FAILURE = 155671;
    static final int CMD_WIFI_TOGGLED = 155656;
    private static boolean DBG = false;
    private static final long DEFAULT_IDLE_MS = 300000;
    private static final long DEFAULT_REENABLE_DELAY_MS = 500;
    private static final long DEFER_MARGIN_MS = 5;
    private static final int IDLE_REQUEST = 0;
    private static final String TAG = "WifiController";
    private AlarmManager mAlarmManager;
    private ApEnabledState mApEnabledState = new ApEnabledState();
    private ApStaDisabledState mApStaDisabledState = new ApStaDisabledState();
    private Context mContext;
    private int mCount = 0;
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
    private QcApDisablingState mQcApDisablingState = new QcApDisablingState();
    private QcApEnablingState mQcApEnablingState = new QcApEnablingState();
    private QcApStaDisablingState mQcApStaDisablingState = new QcApStaDisablingState();
    private QcApStaEnabledState mQcApStaEnabledState = new QcApStaEnabledState();
    private QcApStaEnablingState mQcApStaEnablingState = new QcApStaEnablingState();
    private QcStaDisablingState mQcStaDisablingState = new QcStaDisablingState();
    private QcStaEnablingState mQcStaEnablingState = new QcStaEnablingState();
    private long mReEnableDelayMillis;
    private boolean mRestartStaSapStack = false;
    private ScanOnlyLockHeldState mScanOnlyLockHeldState = new ScanOnlyLockHeldState();
    private boolean mScreenOff;
    private final WifiSettingsStore mSettingsStore;
    private int mSleepPolicy;
    private SoftApStateMachine mSoftApStateMachine = null;
    private boolean mStaAndApConcurrency = false;
    private StaDisabledWithScanState mStaDisabledWithScanState = new StaDisabledWithScanState();
    private StaEnabledState mStaEnabledState = new StaEnabledState();
    private int mStayAwakeConditions;
    private final WorkSource mTmpWorkSource = new WorkSource();
    private final WifiApConfigStore mWifiApConfigStore;
    private final WifiLockManager mWifiLockManager;
    private final WifiStateMachine mWifiStateMachine;
    private boolean mWifiTethering = false;

    class ApEnabledState extends State {
        private State mPendingState = null;

        ApEnabledState() {
        }

        private State getNextWifiState() {
            if ((WifiController.this.mSettingsStore.getWifiSavedState() == 1 || WifiController.this.mSettingsStore.isWifiToggleEnabled()) && !WifiController.this.mStaAndApConcurrency) {
                return WifiController.this.mDeviceActiveState;
            }
            if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                return WifiController.this.mStaDisabledWithScanState;
            }
            return WifiController.this.mApStaDisabledState;
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
            switch (msg.what) {
                case WifiController.CMD_EMERGENCY_MODE_CHANGED /*155649*/:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*155662*/:
                    if (msg.arg1 == 1) {
                        if (WifiController.this.mStaAndApConcurrency) {
                            WifiController.this.mSoftApStateMachine.setHostApRunning(null, false);
                        } else {
                            WifiController.this.mWifiStateMachine.setHostApRunning(null, false);
                        }
                        this.mPendingState = WifiController.this.mEcmState;
                        break;
                    }
                    break;
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    if (WifiController.this.mStaAndApConcurrency) {
                        if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                            WifiController.this.mWifiStateMachine.setOperationalMode(1);
                            WifiController.this.mWifiStateMachine.setSupplicantRunning(false);
                            break;
                        }
                        WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
                        WifiController.this.mWifiStateMachine.setOperationalMode(3);
                        break;
                    }
                    return false;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.mStaAndApConcurrency) {
                            if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                                Slog.d(WifiController.TAG, "ApEnabledState:CMD_WIFI_TOGGLED:->QcApStaEnablingState");
                                WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
                                WifiController.this.transitionTo(WifiController.this.mQcApStaEnablingState);
                                break;
                            }
                            WifiController.this.mWifiStateMachine.setOperationalMode(1);
                            WifiController.this.transitionTo(WifiController.this.mQcApStaEnabledState);
                            break;
                        }
                        WifiController.this.mWifiStateMachine.setHostApRunning(null, false);
                        this.mPendingState = WifiController.this.mDeviceActiveState;
                        break;
                    }
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                        if (!WifiController.this.mStaAndApConcurrency) {
                            WifiController.this.mWifiStateMachine.setHostApRunning(null, false);
                            this.mPendingState = WifiController.this.mApStaDisabledState;
                            break;
                        }
                        WifiController.this.transitionTo(WifiController.this.mQcApDisablingState);
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    int mWifiApState;
                    if (msg.arg1 != 0) {
                        if (msg.arg1 == 1) {
                            mWifiApState = WifiController.this.mWifiStateMachine.syncGetWifiApState();
                            if (12 != mWifiApState && 13 != mWifiApState) {
                                WifiController.this.sendMessageDelayed(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj), 100);
                                this.mPendingState = WifiController.this.mApStaDisabledState;
                                break;
                            }
                            if (WifiController.DBG) {
                                Slog.d(WifiController.TAG, "Enabling SoftAp in progress, ignore CMD_SET_AP 1");
                            }
                            return true;
                        }
                    }
                    mWifiApState = WifiController.this.mWifiStateMachine.syncGetWifiApState();
                    if (10 != mWifiApState && 11 != mWifiApState) {
                        if (!WifiController.this.mStaAndApConcurrency) {
                            WifiController.this.mWifiStateMachine.setHostApRunning(null, false);
                            this.mPendingState = getNextWifiState();
                            break;
                        }
                        WifiController.this.transitionTo(WifiController.this.mQcApDisablingState);
                        break;
                    }
                    if (WifiController.DBG) {
                        Slog.d(WifiController.TAG, "Disabling SoftAp in progress, ignore CMD_SET_AP 0");
                    }
                    return true;
                    break;
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                    this.mPendingState = getNextWifiState();
                    if (WifiController.this.mWifiApConfigStore.getStaSapConcurrency()) {
                        WifiController.this.mWifiApConfigStore.setDualSapStatus(false);
                        WifiController.this.mWifiStateMachine.setDualSapMode(false);
                        WifiController.this.setSoftApStateMachine(WifiController.this.mWifiStateMachine.getSoftApStateMachine(), true);
                    } else if (WifiController.this.mWifiApConfigStore.getApConfiguration().apBand == 2) {
                        WifiController.this.mWifiApConfigStore.setDualSapStatus(false);
                        WifiController.this.mWifiStateMachine.setDualSapMode(false);
                    }
                    WifiController.this.transitionTo(this.mPendingState);
                    break;
                case WifiController.CMD_AP_STOPPED /*155663*/:
                    if (WifiController.this.mWifiApConfigStore.getDualSapStatus()) {
                        WifiController.this.mWifiApConfigStore.setDualSapStatus(false);
                        WifiController.this.mWifiStateMachine.setDualSapMode(false);
                        if (WifiController.this.mWifiApConfigStore.getStaSapConcurrency()) {
                            WifiController.this.setSoftApStateMachine(WifiController.this.mWifiStateMachine.getSoftApStateMachine(), true);
                        }
                    }
                    if (this.mPendingState == null) {
                        this.mPendingState = getNextWifiState();
                    }
                    if (this.mPendingState != WifiController.this.mDeviceActiveState || !WifiController.this.mDeviceIdle) {
                        WifiController.this.transitionTo(this.mPendingState);
                        break;
                    }
                    WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                    break;
                    break;
                case WifiController.CMD_AP_STARTED /*155669*/:
                    if (!WifiController.this.mStaAndApConcurrency && WifiController.this.mWifiApConfigStore.getApConfiguration().apBand == 2) {
                        WifiController.this.mWifiApConfigStore.setDualSapStatus(true);
                        break;
                    }
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
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "-->" + getName());
            }
            if (!WifiController.this.mStaAndApConcurrency) {
                WifiController.this.mWifiStateMachine.setSupplicantRunning(false);
                this.mDisabledTimestamp = SystemClock.elapsedRealtime();
                this.mDeferredEnableSerialNumber++;
                this.mHaveDeferredEnable = false;
                WifiController.this.mWifiStateMachine.clearANQPCache();
            }
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                            WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                            break;
                        }
                    } else if (!doDeferEnable(msg)) {
                        if (!WifiController.this.mDeviceIdle) {
                            if (!WifiController.this.mStaAndApConcurrency) {
                                WifiController.this.mWifiStateMachine.setOperationalMode(1);
                                WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                                break;
                            }
                            WifiController.this.transitionTo(WifiController.this.mQcStaEnablingState);
                            break;
                        }
                        WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        if (msg.arg2 == 0) {
                            WifiController.this.mSettingsStore.setWifiSavedState(0);
                        }
                        if (!WifiController.this.mWifiTethering || WifiController.this.mCount >= 5) {
                            WifiController.this.mCount = 0;
                            if (!WifiController.this.mStaAndApConcurrency) {
                                WifiController.this.mWifiStateMachine.setHostApRunning((SoftApModeConfiguration) msg.obj, true);
                                WifiController.this.transitionTo(WifiController.this.mApEnabledState);
                                break;
                            }
                            WifiController.this.transitionTo(WifiController.this.mQcApEnablingState);
                            break;
                        }
                        WifiController wifiController = WifiController.this;
                        wifiController.mCount = wifiController.mCount + 1;
                        WifiController.this.loge("start SoftAp while wifi still tethering, sleep 100ms, times: " + WifiController.this.mCount);
                        if (!(WifiController.this.getHandler() == null || (WifiController.this.getHandler().hasMessages(WifiController.CMD_SET_AP) ^ 1) == 0)) {
                            WifiController.this.loge("Message Queue doesn't have CMD_SET_AP, send Message Delayed!");
                            WifiController.this.sendMessageDelayed(Message.obtain(msg), 100);
                        }
                        return true;
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
                    if (!WifiController.this.mStaAndApConcurrency) {
                        WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                        break;
                    }
                    WifiController.this.log("ApStaDisabledState: CMD_RESTART_WIFI_CONTINUE -> mQcStaEnablingState");
                    if (WifiController.this.mRestartStaSapStack) {
                        WifiController.this.deferMessage(msg);
                    }
                    WifiController.this.transitionTo(WifiController.this.mQcStaEnablingState);
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
            WifiController.this.logStateAndMessage(msg, this);
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
                case WifiController.CMD_WIFI_ENABLED /*155667*/:
                case WifiController.CMD_WIFI_DISABLED /*155668*/:
                case WifiController.CMD_AP_STARTED /*155669*/:
                case WifiController.CMD_AP_STOP_FAILURE /*155670*/:
                case WifiController.CMD_WIFI_STOP_FAILURE /*155671*/:
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
                        if (WifiController.DBG) {
                            Slog.d(WifiController.TAG, "set idle timer: " + WifiController.this.mIdleMillis + " ms");
                        }
                        WifiController.this.mAlarmManager.set(0, System.currentTimeMillis() + WifiController.this.mIdleMillis, WifiController.this.mIdleIntent);
                        break;
                    }
                    break;
                case WifiController.CMD_BATTERY_CHANGED /*155652*/:
                    int pluggedType = msg.arg1;
                    if (WifiController.DBG) {
                        Slog.d(WifiController.TAG, "battery changed pluggedType: " + pluggedType);
                    }
                    if (WifiController.this.mScreenOff && WifiController.this.shouldWifiStayAwake(WifiController.this.mPluggedType) && (WifiController.this.shouldWifiStayAwake(pluggedType) ^ 1) != 0) {
                        long triggerTime = System.currentTimeMillis() + WifiController.this.mIdleMillis;
                        if (WifiController.DBG) {
                            Slog.d(WifiController.TAG, "set idle timer for " + WifiController.this.mIdleMillis + "ms");
                        }
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
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "-->" + getName());
            }
            WifiController.this.mLastOpenTime = System.currentTimeMillis();
            WifiController.this.mWifiStateMachine.setOperationalMode(1);
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(false);
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, getName() + ",msg.what= " + msg.what + ", mDeviceIdle= " + WifiController.this.mDeviceIdle);
            }
            WifiController.this.logStateAndMessage(msg, this);
            if (msg.what == WifiController.CMD_DEVICE_IDLE) {
                if (!WifiController.this.mScreenOff && (WifiController.this.mDeviceIdle ^ 1) != 0) {
                    return true;
                }
                WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
            } else if (msg.what == WifiController.CMD_USER_PRESENT) {
                if (!WifiController.this.mFirstUserSignOnSeen) {
                    WifiController.this.mWifiStateMachine.reloadTlsNetworksAndReconnect();
                }
                WifiController.this.mFirstUserSignOnSeen = true;
                return true;
            } else if (msg.what == WifiController.CMD_RESTART_WIFI && !WifiController.this.mStaAndApConcurrency) {
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

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
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
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "-->" + getName());
            }
            WifiController.this.mWifiStateMachine.setSupplicantRunning(false);
            WifiController.this.mWifiStateMachine.clearANQPCache();
            this.mEcmEntryCount = 1;
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
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
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "-->" + getName());
            }
            WifiController.this.mWifiStateMachine.setOperationalMode(1);
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(true);
        }
    }

    class FullLockHeldState extends State {
        FullLockHeldState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "-->" + getName());
            }
            WifiController.this.mWifiStateMachine.setOperationalMode(1);
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(false);
        }
    }

    class NoLockHeldState extends State {
        NoLockHeldState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "-->" + getName());
            }
            WifiController.this.mWifiStateMachine.setOperationalMode(4);
        }
    }

    class QcApDisablingState extends State {
        QcApDisablingState() {
        }

        public void enter() {
            WifiController.this.mSoftApStateMachine.setHostApRunning((SoftApModeConfiguration) WifiController.this.getCurrentMessage().obj, false);
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    WifiController.this.log("QcApDisablingState: CMD_SCAN_ALWAYS_MODE_CHANGED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    WifiController.this.log("QcApDisablingState: CMD_WIFI_TOGGLED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    WifiController.this.log("QcApDisablingState: CMD_AIRPLANE_TOGGLED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    WifiController.this.log("QcApDisablingState: CMD_SET_AP defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_AP_STOPPED /*155663*/:
                    if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                        if (WifiController.this.mSettingsStore.isAirplaneModeOn() && WifiController.this.mWifiStateMachine.getOperationalMode() != 1) {
                            WifiController.this.log("ApDisablingState: CMD_AP_STOPPED->mQcStaDisablingState");
                            WifiController.this.mWifiStateMachine.setOperationalMode(1);
                            WifiController.this.transitionTo(WifiController.this.mQcStaDisablingState);
                            break;
                        }
                        WifiController.this.log("QcApDisablingState: CMD_AP_STOPPED->mApStaDisabledState");
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    WifiController.this.log("QcApDisablingState: CMD_AP_STOPPED->mStaDisabledWithScanState");
                    WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                    break;
                    break;
                case WifiController.CMD_AP_STOP_FAILURE /*155670*/:
                    if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                        WifiController.this.log("QcApDisablingState: CMD_AP_STOP_FAILURE->mApStaDisabledState");
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    WifiController.this.log("QcApDisablingState: CMD_AP_STOP_FAILURE->mStaDisabledWithScanState");
                    WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Slog.d(WifiController.TAG, "QcApDisablingState: exit()");
        }
    }

    class QcApEnablingState extends State {
        QcApEnablingState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcApEnablingState enter");
            }
            SoftApModeConfiguration mSamc = WifiController.this.getCurrentMessage().obj;
            if (mSamc != null) {
                WifiController.this.mSoftApStateMachine.setHostApRunning(mSamc, true);
            } else {
                Slog.d(WifiController.TAG, "QcApEnablingState enter: mSamc == null!!!");
            }
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    WifiController.this.log("QcApEnablingState: CMD_SCAN_ALWAYS_MODE_CHANGED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    WifiController.this.log("QcApEnablingState: CMD_WIFI_TOGGLED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    WifiController.this.log("QcApEnablingState: CMD_AIRPLANE_TOGGLED defered");
                    WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    WifiController.this.log("QcApEnablingState: CMD_SET_AP defered");
                    WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                    break;
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                    WifiController.this.log("QcApEnablingState: CMD_AP_START_FAILURE->mApStaDisabledState");
                    WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                    break;
                case WifiController.CMD_AP_STARTED /*155669*/:
                    WifiController.this.log("QcApEnablingState: CMD_AP_STARTED->mApEnabledState");
                    WifiController.this.transitionTo(WifiController.this.mApEnabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcApEnablingState exit");
            }
        }
    }

    class QcApStaDisablingState extends State {
        QcApStaDisablingState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcApStaDisablingState enter");
            }
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    WifiController.this.log("QcApStaDisablingState: CMD_SCAN_ALWAYS_MODE_CHANGED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    WifiController.this.log("QcApStaDisablingState defer CMD_WIFI_TOGGLED");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    WifiController.this.log("QcApStaDisablingState defer CMD_AIRPLANE_TOGGLED");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    WifiController.this.log("QcApStaDisablingState defer CMD_SET_AP");
                    WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                    break;
                case WifiController.CMD_AP_STOPPED /*155663*/:
                    WifiController.this.log("QcApStaDisablingState: CMD_AP_STOPPED->StaEnabledState");
                    WifiController.this.transitionTo(WifiController.this.mStaEnabledState);
                    break;
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    WifiController.this.log("QcApStaDisablingState: CMD_STA_START_FAILURE dropped");
                    break;
                case WifiController.CMD_RESTART_WIFI /*155665*/:
                    WifiController.this.log("QcApStaDisablingState defer CMD_RESTART_WIFI");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_WIFI_ENABLED /*155667*/:
                    WifiController.this.log("QcApStaDisablingState CMD_WIFI_ENABLED ignored");
                    break;
                case WifiController.CMD_WIFI_DISABLED /*155668*/:
                    WifiController.this.log("QcApStaDisablingState: CMD_WIFI_DISABLED-> mApEnabledState");
                    WifiController.this.transitionTo(WifiController.this.mApEnabledState);
                    break;
                case WifiController.CMD_AP_STARTED /*155669*/:
                    WifiController.this.log("QcApStaDisablingState CMD_AP_STARTED ignored");
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcApStaDisablingState exit");
            }
        }
    }

    class QcApStaEnabledState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;
        private State mPendingState = null;

        QcApStaEnabledState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcApStaEnabledState enter");
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!doDeferEnable(msg)) {
                            if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                                WifiController.this.log("QcApStaEnabledState:CMD_WIFI_TOGGLED:set:SCAN_ONLY_WITH_WIFI_OFF_MODE");
                                WifiController.this.mWifiStateMachine.setOperationalMode(3);
                            } else {
                                WifiController.this.log("QcApStaEnabledState:CMD_WIFI_TOGGLED:setSupplicantRunning(false)");
                                WifiController.this.mWifiStateMachine.setOperationalMode(1);
                                WifiController.this.mWifiStateMachine.setSupplicantRunning(false);
                            }
                            WifiController.this.transitionTo(WifiController.this.mQcApStaDisablingState);
                            break;
                        }
                        if (this.mHaveDeferredEnable) {
                            this.mDeferredEnableSerialNumber++;
                        }
                        this.mHaveDeferredEnable ^= 1;
                        break;
                    }
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    WifiController.this.log("QcApStaEnabledState: CMD_AIRPLANE_TOGGLED-> mQcApStaDisablingState");
                    WifiController.this.mSoftApStateMachine.setHostApRunning(null, false);
                    WifiController.this.deferMessage(msg);
                    WifiController.this.transitionTo(WifiController.this.mQcApStaDisablingState);
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 0) {
                        if (WifiController.DBG) {
                            Slog.d(WifiController.TAG, "QcApStaEnabledState:CMD_SET_AP:setHostApRunning(false)-> mQcApStaDisablingState");
                        }
                        WifiController.this.mSoftApStateMachine.setHostApRunning(null, false);
                        WifiController.this.transitionTo(WifiController.this.mQcApStaDisablingState);
                        break;
                    }
                    break;
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                    WifiController.this.transitionTo(WifiController.this.mStaEnabledState);
                    break;
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    WifiController.this.transitionTo(WifiController.this.mApEnabledState);
                    break;
                case WifiController.CMD_RESTART_WIFI /*155665*/:
                    WifiController.this.log("QcApStaEnabledState: CMD_RESTART_WIFI -> setHostApRunning(false) -> mQcApStaDisablingState");
                    WifiController.this.mSoftApStateMachine.setHostApRunning(null, false);
                    WifiController.this.mRestartStaSapStack = true;
                    WifiController.this.deferMessage(msg);
                    WifiController.this.transitionTo(WifiController.this.mQcApStaDisablingState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcApStaEnabledState exit");
            }
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

    class QcApStaEnablingState extends State {
        QcApStaEnablingState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcApStaEnablingState enter");
            }
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    WifiController.this.log("QcApStaEnablingState: CMD_SCAN_ALWAYS_MODE_CHANGED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    WifiController.this.log("QcApStaEnablingState defer CMD_WIFI_TOGGLED");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    WifiController.this.log("QcApStaEnablingState defer CMD_AIRPLANE_TOGGLED");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    WifiController.this.log("QcApStaEnablingState defer CMD_SET_AP");
                    WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                    break;
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                    WifiController.this.log("QcApStaEnablingState: CMD_AP_START_FAILURE-> mStaEnabledState");
                    WifiController.this.transitionTo(WifiController.this.mStaEnabledState);
                    break;
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    WifiController.this.log("QcApStaEnablingState: CMD_STA_START_FAILURE-> mApEnabledState");
                    WifiController.this.transitionTo(WifiController.this.mApEnabledState);
                    break;
                case WifiController.CMD_WIFI_ENABLED /*155667*/:
                    WifiController.this.log("QcApStaEnablingState: CMD_WIFI_ENABLED-> mQcApStaEnabledState");
                    WifiController.this.transitionTo(WifiController.this.mQcApStaEnabledState);
                    break;
                case WifiController.CMD_AP_STARTED /*155669*/:
                    WifiController.this.log("QcApStaEnablingState: CMD_AP_STARTED-> mQcApStaEnabledState");
                    WifiController.this.transitionTo(WifiController.this.mQcApStaEnabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcApStaEnablingState exit");
            }
        }
    }

    class QcStaDisablingState extends State {
        QcStaDisablingState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcStaDisablingState enter");
            }
            WifiController.this.mWifiStateMachine.setSupplicantRunning(false);
            WifiController.this.mWifiStateMachine.clearANQPCache();
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    WifiController.this.log("QcStaDisablingState: CMD_SCAN_ALWAYS_MODE_CHANGED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    WifiController.this.log("QcStaDisablingState: CMD_WIFI_TOGGLED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    WifiController.this.log("QcStaDisablingState: CMD_AIRPLANE_TOGGLED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    WifiController.this.log("QcStaDisablingState: CMD_SET_AP defered");
                    WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                    break;
                case WifiController.CMD_RESTART_WIFI_CONTINUE /*155666*/:
                    WifiController.this.log("QcStaDisablingState: CMD_RESTART_WIFI_CONTINUE defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_WIFI_DISABLED /*155668*/:
                case WifiController.CMD_WIFI_STOP_FAILURE /*155671*/:
                    WifiController.this.log("QcStaDisablingState: CMD_WIFI_DISABLED->mApStaDisabledState");
                    WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcStaDisablingState exit");
            }
        }
    }

    class QcStaEnablingState extends State {
        QcStaEnablingState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcStaEnablingState enter");
            }
            WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    WifiController.this.log("QcStaEnablingState: CMD_SCAN_ALWAYS_MODE_CHANGED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    WifiController.this.log("QcStaEnablingState: CMD_WIFI_TOGGLED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    WifiController.this.log("QcStaEnablingState: CMD_AIRPLANE_TOGGLED defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    WifiController.this.log("QcStaEnablingState: CMD_SET_AP defered");
                    WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                    break;
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    WifiController.this.log("QcStaEnablingState: CMD_STA_START_FAILURE->mApStaDisabledState");
                    WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                    break;
                case WifiController.CMD_RESTART_WIFI_CONTINUE /*155666*/:
                    WifiController.this.log("QcStaEnablingState: CMD_RESTART_WIFI_CONTINUE defered");
                    WifiController.this.deferMessage(msg);
                    break;
                case WifiController.CMD_WIFI_ENABLED /*155667*/:
                    WifiController.this.log("QcStaEnablingState: CMD_WIFI_ENABLED->mDeviceActiveState");
                    WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "QcStaEnablingState exit");
            }
        }
    }

    class ScanOnlyLockHeldState extends State {
        ScanOnlyLockHeldState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "-->" + getName());
            }
            WifiController.this.mWifiStateMachine.setOperationalMode(2);
        }
    }

    class StaDisabledWithScanState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;

        StaDisabledWithScanState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "-->" + getName());
            }
            WifiController.this.mWifiStateMachine.setOperationalMode(3);
            WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = false;
            WifiController.this.mWifiStateMachine.clearANQPCache();
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                        if (!WifiController.this.mStaAndApConcurrency) {
                            WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                            break;
                        }
                        WifiController.this.mWifiStateMachine.setOperationalMode(1);
                        WifiController.this.transitionTo(WifiController.this.mQcStaDisablingState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled() && !doDeferEnable(msg)) {
                        if (!WifiController.this.mDeviceIdle) {
                            WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                            break;
                        }
                        WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                        break;
                    }
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn() && (WifiController.this.mSettingsStore.isWifiToggleEnabled() ^ 1) != 0) {
                        if (!WifiController.this.mStaAndApConcurrency) {
                            WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                            break;
                        }
                        WifiController.this.transitionTo(WifiController.this.mQcStaDisablingState);
                        break;
                    }
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        if (!WifiController.this.mStaAndApConcurrency) {
                            WifiController.this.mSettingsStore.setWifiSavedState(0);
                            WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                            WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                            break;
                        }
                        WifiController.this.transitionTo(WifiController.this.mQcApEnablingState);
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
            if (WifiController.DBG) {
                Slog.d(WifiController.TAG, "-->" + getName());
            }
            if (!WifiController.this.mStaAndApConcurrency) {
                WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
            }
        }

        public boolean processMessage(Message msg) {
            WifiController.this.logStateAndMessage(msg, this);
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
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                            if (!WifiController.this.mStaAndApConcurrency) {
                                WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                                break;
                            }
                            WifiController.this.transitionTo(WifiController.this.mQcStaDisablingState);
                            break;
                        }
                        if (System.currentTimeMillis() - WifiController.this.mLastOpenTime <= ((long) WifiController.this.mWifiStateMachine.getRomUpdateIntegerValue("BASIC_WIFI_OPEN_TIME", Integer.valueOf(300)).intValue()) * 1000) {
                            WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                            break;
                        }
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        WifiController.this.sendMessageDelayed(WifiController.this.obtainMessage(WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED), 2000);
                        break;
                    }
                    int mWifiState = WifiController.this.mWifiStateMachine.syncGetWifiState();
                    Slog.e(WifiController.TAG, "Calling setWifiEnabled(true) in StaEnabledState!! mWifiState=" + mWifiState);
                    if (!(mWifiState == 2 || mWifiState == 3)) {
                        if (WifiController.DBG) {
                            Slog.d(WifiController.TAG, "Mismatch in the state " + mWifiState);
                        }
                        WifiController.this.sendMessage(WifiController.CMD_RESTART_WIFI);
                        break;
                    }
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!WifiController.this.mStaAndApConcurrency) {
                            WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                            break;
                        }
                        WifiController.this.transitionTo(WifiController.this.mQcStaDisablingState);
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        if (!WifiController.this.mStaAndApConcurrency) {
                            WifiController.this.mSettingsStore.setWifiSavedState(1);
                            WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                            WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                            break;
                        }
                        Slog.d(WifiController.TAG, "StaEnabledState:CMD_SET_AP:setHostApRunning(true)-> mApStaEnableState");
                        WifiController.this.mSoftApStateMachine.setHostApRunning((SoftApModeConfiguration) msg.obj, true);
                        WifiController.this.transitionTo(WifiController.this.mQcApStaEnablingState);
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
                case WifiController.CMD_RESTART_WIFI /*155665*/:
                    if (WifiController.this.mStaAndApConcurrency) {
                        WifiController.this.log("StaEnabledState:CMD_RESTART_WIFI ->QcStaDisablingState");
                        WifiController.this.deferMessage(WifiController.this.obtainMessage(WifiController.CMD_RESTART_WIFI_CONTINUE));
                        WifiController.this.transitionTo(WifiController.this.mQcStaDisablingState);
                        break;
                    }
                    break;
                case WifiController.CMD_RESTART_WIFI_CONTINUE /*155666*/:
                    if (WifiController.this.mStaAndApConcurrency && WifiController.this.mRestartStaSapStack) {
                        if (msg.obj != null) {
                            WifiController.this.log("StaEnabledState:CMD_RESTART_WIFI_CONTINUE ->mQcApStaEnablingState");
                            WifiController.this.mSoftApStateMachine.setHostApRunning((SoftApModeConfiguration) msg.obj, true);
                            WifiController.this.transitionTo(WifiController.this.mQcApStaEnablingState);
                        } else {
                            WifiController.this.log("StaEnabledState:CMD_RESTART_WIFI_CONTINUE: SoftApConfig obj null. Do nothing");
                        }
                        WifiController.this.mRestartStaSapStack = false;
                        break;
                    }
                default:
                    return false;
            }
            return true;
        }
    }

    WifiController(Context context, WifiStateMachine wsm, WifiSettingsStore wss, WifiLockManager wifiLockManager, Looper looper, FrameworkFacade f, WifiApConfigStore wacs) {
        super(TAG, looper);
        this.mFacade = f;
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mSettingsStore = wss;
        this.mWifiLockManager = wifiLockManager;
        this.mWifiApConfigStore = wacs;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mIdleIntent = this.mFacade.getBroadcast(this.mContext, 0, new Intent(ACTION_DEVICE_IDLE, null), 0);
        addState(this.mDefaultState);
        addState(this.mApStaDisabledState, this.mDefaultState);
        addState(this.mStaEnabledState, this.mDefaultState);
        addState(this.mQcStaEnablingState, this.mDefaultState);
        addState(this.mQcStaDisablingState, this.mDefaultState);
        addState(this.mQcApEnablingState, this.mDefaultState);
        addState(this.mQcApDisablingState, this.mDefaultState);
        addState(this.mQcApStaEnablingState, this.mDefaultState);
        addState(this.mQcApStaDisablingState, this.mDefaultState);
        addState(this.mQcApStaEnabledState, this.mDefaultState);
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
        filter.addAction("android.net.conn.TETHER_STATE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int state;
                if (action.equals(WifiController.ACTION_DEVICE_IDLE)) {
                    WifiController.this.sendMessage(WifiController.CMD_DEVICE_IDLE);
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    WifiController.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                    state = intent.getIntExtra("wifi_state", 14);
                    if (state == 14) {
                        WifiController.this.loge("WifiControllerSoftAP start failed");
                        WifiController.this.sendMessage(WifiController.CMD_AP_START_FAILURE);
                    } else if (state == 11) {
                        WifiController.this.sendMessage(WifiController.CMD_AP_STOPPED);
                    } else if (state == 13) {
                        WifiController.this.sendMessage(WifiController.CMD_AP_STARTED);
                    }
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    state = intent.getIntExtra("wifi_state", 4);
                    if (state == 4) {
                        WifiController.this.loge("WifiControllerWifi turn on failed");
                        WifiController.this.sendMessage(WifiController.CMD_STA_START_FAILURE);
                    } else if (state == 3) {
                        WifiController.this.sendMessage(WifiController.CMD_WIFI_ENABLED);
                    } else if (state == 1) {
                        WifiController.this.sendMessage(WifiController.CMD_WIFI_DISABLED);
                    }
                } else if ("android.net.conn.TETHER_STATE_CHANGED".equals(action)) {
                    ArrayList<String> active = intent.getStringArrayListExtra("tetherArray");
                    if (active == null) {
                        WifiController.this.loge("WifiController ACTION_TETHER_STATE_CHANGED but active is null");
                        return;
                    }
                    WifiController.this.mWifiTethering = false;
                    String interfaceName = WifiInjector.getInstance().getWifiNative().getInterfaceName();
                    for (String tmp : active) {
                        WifiController.this.loge("WifiController active list: " + tmp);
                        if (tmp.equals(interfaceName)) {
                            WifiController.this.mWifiTethering = true;
                        }
                    }
                    WifiController.this.loge("WifiController ACTION_TETHER_STATE_CHANGED " + interfaceName + " " + WifiController.this.mWifiTethering);
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
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.cmcc.test")) {
            this.mSleepPolicy = this.mFacade.getIntegerSetting(this.mContext, "wifi_sleep_policy", 2);
        } else {
            this.mSleepPolicy = 2;
        }
    }

    private void readWifiReEnableDelay() {
        this.mReEnableDelayMillis = this.mFacade.getLongSetting(this.mContext, "wifi_reenable_delay", DEFAULT_REENABLE_DELAY_MS);
    }

    private void registerForStayAwakeModeChange(Handler handler) {
        this.mFacade.registerContentObserver(this.mContext, Global.getUriFor("stay_on_while_plugged_in"), false, new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                WifiController.this.readStayAwakeConditions();
            }
        });
    }

    private void registerForWifiIdleTimeChange(Handler handler) {
        this.mFacade.registerContentObserver(this.mContext, Global.getUriFor("wifi_idle_ms"), false, new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                WifiController.this.readWifiIdleTime();
            }
        });
    }

    private void registerForWifiSleepPolicyChange(Handler handler) {
        this.mFacade.registerContentObserver(this.mContext, Global.getUriFor("wifi_sleep_policy"), false, new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                WifiController.this.readWifiSleepPolicy();
            }
        });
    }

    private boolean shouldWifiStayAwake(int pluggedType) {
        if (this.mSleepPolicy == 2) {
            return true;
        }
        if (this.mSleepPolicy != 1 || pluggedType == 0) {
            return shouldDeviceStayAwake(pluggedType);
        }
        return true;
    }

    private boolean shouldDeviceStayAwake(int pluggedType) {
        return (this.mStayAwakeConditions & pluggedType) != 0;
    }

    private void updateBatteryWorkSource() {
        this.mTmpWorkSource.clear();
        if (this.mDeviceIdle) {
            this.mTmpWorkSource.add(this.mWifiLockManager.createMergedWorkSource());
        }
        this.mWifiStateMachine.updateBatteryWorkSource(this.mTmpWorkSource);
    }

    private void logStateAndMessage(Message message, State state) {
        if (DBG) {
            logd(" " + state.getClass().getSimpleName() + ", message: " + message);
        }
    }

    public void setSoftApStateMachine(SoftApStateMachine machine, boolean enable) {
        this.mSoftApStateMachine = machine;
        this.mStaAndApConcurrency = enable;
        this.mWifiStateMachine.setStaSoftApConcurrency(enable);
        Slog.d(TAG, "set mStaAndApConcurrency=" + this.mStaAndApConcurrency);
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
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
