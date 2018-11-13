package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.IApInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Pair;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.util.ApConfigUtil;
import com.mediatek.server.wifi.WifiApStateMachine;
import java.util.concurrent.atomic.AtomicInteger;

public class SoftApStateMachine extends StateMachine {
    static final int BASE = 131072;
    static final int CMD_AP_STOPPED = 131096;
    public static final int CMD_BOOT_COMPLETED = 131206;
    static final int CMD_START_AP = 131093;
    static final int CMD_START_AP_FAILURE = 131094;
    static final int CMD_STOP_AP = 131095;
    private static boolean DBG = false;
    private static final String TAG = "SoftApStateMachine";
    private final IBatteryStats mBatteryStats;
    private ConnectivityManager mCm;
    private Context mContext;
    private State mInitialState = new InitialState();
    private String mInterfaceName = null;
    private INetworkManagementService mNwService;
    private int mSoftApChannel = 0;
    private SoftApManager mSoftApManager;
    private State mSoftApState = new SoftApState();
    private WifiApConfigStore mWifiApConfigStore;
    private final AtomicInteger mWifiApState = new AtomicInteger(11);
    private WifiConfigManager mWifiConfigManager;
    private WifiInjector mWifiInjector;
    private WifiNative mWifiNative;
    private WifiStateTracker mWifiStateTracker;

    class InitialState extends State {
        InitialState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case SoftApStateMachine.CMD_START_AP /*131093*/:
                    SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mSoftApState);
                    return true;
                default:
                    return false;
            }
        }
    }

    class SoftApState extends State {
        private String mIfaceName;
        private int mMode;

        private class SoftApListener implements Listener {
            /* synthetic */ SoftApListener(SoftApState this$1, SoftApListener -this1) {
                this();
            }

            private SoftApListener() {
            }

            public void onStateChanged(int state, int reason) {
                if (state == 11) {
                    SoftApStateMachine.this.mWifiNative.addOrRemoveInterface(SoftApStateMachine.this.mInterfaceName, false, false);
                    SoftApStateMachine.this.sendMessage(SoftApStateMachine.CMD_AP_STOPPED);
                } else if (state == 14) {
                    SoftApStateMachine.this.mWifiNative.addOrRemoveInterface(SoftApStateMachine.this.mInterfaceName, false, false);
                    SoftApStateMachine.this.sendMessage(SoftApStateMachine.CMD_START_AP_FAILURE);
                }
                SoftApStateMachine.this.setWifiApState(state, reason, SoftApState.this.mIfaceName, SoftApState.this.mMode);
            }
        }

        SoftApState() {
        }

        public void enter() {
            Message message = SoftApStateMachine.this.getCurrentMessage();
            if (message.what != SoftApStateMachine.CMD_START_AP) {
                throw new RuntimeException("Illegal transition to SoftApState: " + message);
            }
            SoftApModeConfiguration config = message.obj;
            this.mMode = config.getTargetMode();
            IApInterface iApInterface = null;
            Pair<Integer, IApInterface> statusAndInterface = SoftApStateMachine.this.mWifiNative.setupForSoftApMode(SoftApStateMachine.this.mInterfaceName, false);
            if (((Integer) statusAndInterface.first).intValue() == 0) {
                iApInterface = statusAndInterface.second;
            } else {
                Log.e(SoftApStateMachine.TAG, "Error in wifi onFailure due to HAL");
            }
            if (iApInterface == null) {
                SoftApStateMachine.this.mWifiNative.addOrRemoveInterface(SoftApStateMachine.this.mInterfaceName, false, false);
                SoftApStateMachine.this.setWifiApState(14, 0, null, this.mMode);
                SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mInitialState);
                return;
            }
            try {
                this.mIfaceName = iApInterface.getInterfaceName();
            } catch (RemoteException e) {
            }
            WifiStateMachine mWifiStateMachine = SoftApStateMachine.this.mWifiInjector.getWifiStateMachine();
            WifiConfiguration currentStaConfig = mWifiStateMachine.getCurrentWifiConfiguration();
            if (currentStaConfig != null && currentStaConfig.shareThisAp) {
                config = new SoftApModeConfiguration(this.mMode, currentStaConfig);
                currentStaConfig = SoftApStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithPassword(currentStaConfig.networkId);
                config.mConfig.SSID = ApConfigUtil.removeDoubleQuotes(currentStaConfig.SSID);
                config.mConfig.apBand = currentStaConfig.apBand;
                config.mConfig.apChannel = currentStaConfig.apChannel;
                config.mConfig.hiddenSSID = currentStaConfig.hiddenSSID;
                config.mConfig.preSharedKey = ApConfigUtil.removeDoubleQuotes(currentStaConfig.preSharedKey);
                config.mConfig.allowedKeyManagement = currentStaConfig.allowedKeyManagement;
                SoftApStateMachine.this.mSoftApChannel = currentStaConfig.apChannel;
                WifiInfo mWifiInfo = mWifiStateMachine.getWifiInfo();
                if (mWifiInfo != null && config.mConfig.apChannel == 0) {
                    config.mConfig.apChannel = ApConfigUtil.convertFrequencyToChannel(mWifiInfo.getFrequency());
                    SoftApStateMachine.this.mSoftApChannel = config.mConfig.apChannel;
                }
            } else if (!(currentStaConfig == null || currentStaConfig.shareThisAp)) {
                SoftApStateMachine.this.mSoftApChannel = 0;
            }
            SoftApStateMachine.this.checkAndSetConnectivityInstance();
            SoftApStateMachine.this.mSoftApManager = SoftApStateMachine.this.mWifiInjector.makeSoftApManager(SoftApStateMachine.this.mNwService, new SoftApListener(this, null), iApInterface, config.getWifiConfiguration());
            if (SoftApStateMachine.this.mSoftApChannel != 0) {
                SoftApStateMachine.this.mSoftApManager.setSapChannel(SoftApStateMachine.this.mSoftApChannel);
            }
            SoftApStateMachine.this.mSoftApManager.start();
            SoftApStateMachine.this.mWifiStateTracker.updateState(4);
        }

        public void exit() {
            SoftApStateMachine.this.mSoftApManager = null;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case SoftApStateMachine.CMD_START_AP /*131093*/:
                    break;
                case SoftApStateMachine.CMD_START_AP_FAILURE /*131094*/:
                    SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mInitialState);
                    break;
                case SoftApStateMachine.CMD_STOP_AP /*131095*/:
                    SoftApStateMachine.this.mSoftApManager.stop();
                    break;
                case SoftApStateMachine.CMD_AP_STOPPED /*131096*/:
                    SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mInitialState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    public SoftApStateMachine(Context context, WifiInjector wifiInjector, WifiNative wifiNative, INetworkManagementService NwService, IBatteryStats BatteryStats) {
        super(TAG);
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mWifiStateTracker = wifiInjector.getWifiStateTracker();
        this.mWifiConfigManager = wifiInjector.getWifiConfigManager();
        this.mWifiApConfigStore = wifiInjector.getWifiApConfigStore();
        this.mWifiNative = wifiNative;
        this.mNwService = NwService;
        this.mBatteryStats = BatteryStats;
        addState(this.mInitialState);
        addState(this.mSoftApState, this.mInitialState);
        setInitialState(this.mInitialState);
        start();
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
    }

    public boolean isExtendingNetworkCoverage() {
        WifiConfiguration currentStaConfig = this.mWifiInjector.getWifiStateMachine().getCurrentWifiConfiguration();
        return currentStaConfig != null ? currentStaConfig.shareThisAp : false;
    }

    public void setSoftApInterfaceName(String iface) {
        this.mInterfaceName = iface;
    }

    public void setSoftApChannel(int channel) {
        this.mSoftApChannel = channel;
    }

    public void setHostApRunning(SoftApModeConfiguration softApConfig, boolean enable) {
        if (enable) {
            sendMessage(CMD_START_AP, softApConfig);
        } else {
            sendMessage(CMD_STOP_AP);
        }
    }

    public int syncGetWifiApState() {
        return this.mWifiApState.get();
    }

    public String syncGetWifiApStateByName() {
        switch (this.mWifiApState.get()) {
            case 10:
                return "disabling";
            case 11:
                return "disabled";
            case 12:
                return "enabling";
            case 13:
                return "enabled";
            case 14:
                return "failed";
            default:
                return "[invalid state]";
        }
    }

    private void setWifiApState(int wifiApState, int reason, String ifaceName, int mode) {
        int previousWifiApState = this.mWifiApState.get();
        if (wifiApState == 13) {
            try {
                this.mBatteryStats.noteWifiOn();
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to note battery stats in wifi");
            }
        } else if (wifiApState == 11) {
            this.mBatteryStats.noteWifiOff();
        }
        if (wifiApState == 11 || wifiApState == 14) {
            boolean skipUnload = false;
            WifiStateMachine mWifiStateMachine = this.mWifiInjector.getWifiStateMachine();
            int wifiState = mWifiStateMachine.syncGetWifiState();
            int operMode = mWifiStateMachine.getOperationalMode();
            if (wifiState == 2 || wifiState == 3 || operMode == 3) {
                Log.d(TAG, "Avoid unload driver, WIFI_STATE is enabled/enabling");
                skipUnload = true;
            }
            if (skipUnload) {
                this.mWifiNative.tearDownAp();
            } else {
                mWifiStateMachine.cleanup();
            }
        }
        this.mWifiApState.set(wifiApState);
        if (DBG) {
            Log.d(TAG, "setWifiApState: " + syncGetWifiApStateByName());
        }
        Intent intent = new Intent("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", wifiApState);
        intent.putExtra("previous_wifi_state", previousWifiApState);
        if (wifiApState == 14) {
            intent.putExtra("wifi_ap_error_code", reason);
        }
        if (ifaceName == null) {
            Log.e(TAG, "Updating wifiApState with a null iface name");
        }
        intent.putExtra("wifi_ap_interface_name", ifaceName);
        intent.putExtra("wifi_ap_mode", mode);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    public boolean processSoftApStateMessage(Message message, Context context) {
        if (this.mSoftApManager != null && WifiApStateMachine.processSoftApStateMessage(message, context, this.mSoftApManager)) {
            return true;
        }
        return false;
    }

    private Message obtainMessageWithWhatAndArg2(Message srcMsg, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg2 = srcMsg.arg2;
        return msg;
    }
}
