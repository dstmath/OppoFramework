package com.android.server.wifi;

import android.app.Notification.Builder;
import android.content.Context;
import android.net.InterfaceConfiguration;
import android.net.wifi.IApInterface;
import android.net.wifi.IClientInterface;
import android.net.wifi.IInterfaceEventCallback.Stub;
import android.net.wifi.IWificond;
import android.net.wifi.WifiConfiguration;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.wifi.util.ApConfigUtil;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SoftApManager implements ActiveModeManager {
    private static final boolean DBG = true;
    private static final int DNSMASQ_POLLING_INTERVAL = 1000;
    private static final int DNSMASQ_POLLING_MAX_TIMES = 10;
    private static final String TAG = "SoftApManager";
    private WifiConfiguration mApConfig;
    private final IApInterface mApInterface;
    private Context mContext;
    private final String mCountryCode;
    private boolean mDualSapMode = false;
    private int mLastSoftApNotificationId = 0;
    private final Listener mListener;
    private final INetworkManagementService mNwService;
    private int mSoftApChannel = 0;
    private final SoftApStateMachine mStateMachine = null;
    private final WifiApConfigStore mWifiApConfigStore;
    private final WifiMetrics mWifiMetrics;
    private final WifiNative mWifiNative;
    private WifiSoftApNotificationManager mWifiSoftApNotificationManager;
    private Builder softApNotificationBuilder;

    private static class InterfaceEventHandler extends Stub {
        private SoftApStateMachine mSoftApStateMachine;

        InterfaceEventHandler(SoftApStateMachine stateMachine) {
            this.mSoftApStateMachine = stateMachine;
        }

        public void OnClientTorndownEvent(IClientInterface networkInterface) {
        }

        public void OnClientInterfaceReady(IClientInterface networkInterface) {
        }

        public void OnApTorndownEvent(IApInterface networkInterface) {
        }

        public void OnApInterfaceReady(IApInterface networkInterface) {
        }

        public void OnSoftApClientEvent(byte[] mac_address, boolean connect_status) {
            int i = 1;
            StringBuilder sb = new StringBuilder(18);
            for (byte b : mac_address) {
                if (sb.length() > 0) {
                    sb.append(':');
                }
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(b)}));
            }
            Log.d(SoftApManager.TAG, "Client mac_addr = " + sb.toString() + " status = " + connect_status);
            Message msg = Message.obtain();
            msg.obj = sb.toString();
            SoftApStateMachine softApStateMachine = this.mSoftApStateMachine;
            if (!connect_status) {
                i = 0;
            }
            softApStateMachine.sendMessage(4, i, 0, msg.obj);
        }
    }

    public interface Listener {
        void onStateChanged(int i, int i2);
    }

    private class SoftApStateMachine extends StateMachine {
        public static final int CMD_AP_INTERFACE_BINDER_DEATH = 2;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_SOFTAP_CLIENT_CONNECT_STATUS_CHANGED = 4;
        public static final int CMD_START = 0;
        public static final int CMD_STOP = 1;
        private final StateMachineDeathRecipient mDeathRecipient = new StateMachineDeathRecipient(this, 2);
        private final State mIdleState = new IdleState(this, null);
        private InterfaceEventHandler mInterfaceEventHandler;
        private NetworkObserver mNetworkObserver;
        private final State mStartedState = new StartedState(this, null);
        private WifiInjector mWifiInjector;
        private IWificond mWificond;

        private class IdleState extends State {
            /* synthetic */ IdleState(SoftApStateMachine this$1, IdleState -this1) {
                this();
            }

            private IdleState() {
            }

            public void enter() {
                SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                unregisterObserver();
                SoftApStateMachine.this.mWificond = SoftApStateMachine.this.mWifiInjector.makeWificond();
                if (SoftApStateMachine.this.mWificond == null) {
                    Log.w(SoftApManager.TAG, "Failed to get wificond binder handler");
                } else {
                    try {
                        SoftApStateMachine.this.mWificond.RegisterCallback(SoftApStateMachine.this.mInterfaceEventHandler);
                    } catch (RemoteException e) {
                    }
                }
            }

            public boolean processMessage(Message message) {
                switch (message.what) {
                    case 0:
                        SoftApManager.this.updateApState(12, 0);
                        if (!SoftApStateMachine.this.mDeathRecipient.linkToDeath(SoftApManager.this.mApInterface.asBinder())) {
                            SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                            SoftApManager.this.updateApState(14, 0);
                            SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, 0);
                            break;
                        }
                        try {
                            SoftApStateMachine.this.mNetworkObserver = new NetworkObserver(SoftApManager.this.mApInterface.getInterfaceName());
                            SoftApManager.this.mNwService.registerObserver(SoftApStateMachine.this.mNetworkObserver);
                            int result = SoftApManager.this.startSoftAp((WifiConfiguration) message.obj);
                            if (result == 0) {
                                SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mStartedState);
                                break;
                            }
                            int failureReason = 0;
                            if (result == 1) {
                                failureReason = 1;
                            }
                            SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                            unregisterObserver();
                            SoftApManager.this.updateApState(14, failureReason);
                            SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, failureReason);
                            break;
                        } catch (RemoteException e) {
                            SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                            unregisterObserver();
                            SoftApManager.this.updateApState(14, 0);
                            SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, 0);
                            break;
                        }
                }
                return true;
            }

            private void unregisterObserver() {
                if (SoftApStateMachine.this.mNetworkObserver != null) {
                    try {
                        SoftApManager.this.mNwService.unregisterObserver(SoftApStateMachine.this.mNetworkObserver);
                    } catch (RemoteException e) {
                    }
                    SoftApStateMachine.this.mNetworkObserver = null;
                }
            }
        }

        private class NetworkObserver extends BaseNetworkObserver {
            private final String mIfaceName;

            NetworkObserver(String ifaceName) {
                this.mIfaceName = ifaceName;
            }

            public void interfaceLinkStateChanged(String iface, boolean up) {
                if (this.mIfaceName.equals(iface)) {
                    int i;
                    SoftApStateMachine softApStateMachine = SoftApStateMachine.this;
                    if (up) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    softApStateMachine.sendMessage(3, i, 0, this);
                }
            }
        }

        private class StartedState extends State {
            private boolean mIfaceIsUp;

            /* synthetic */ StartedState(SoftApStateMachine this$1, StartedState -this1) {
                this();
            }

            private StartedState() {
            }

            private void onUpChanged(boolean isUp) {
                if (isUp != this.mIfaceIsUp) {
                    this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.d(SoftApManager.TAG, "SoftAp is ready for use");
                        SoftApManager.this.updateApState(13, 0);
                        SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(true, 0);
                    }
                }
            }

            public void enter() {
                this.mIfaceIsUp = false;
                InterfaceConfiguration config = null;
                try {
                    config = SoftApManager.this.mNwService.getInterfaceConfig(SoftApManager.this.mApInterface.getInterfaceName());
                } catch (RemoteException e) {
                }
                if (config != null) {
                    onUpChanged(config.isActive());
                }
            }

            public boolean processMessage(Message message) {
                switch (message.what) {
                    case 0:
                        break;
                    case 1:
                    case 2:
                        SoftApManager.this.updateApState(10, 0);
                        SoftApManager.this.stopSoftAp();
                        if (message.what == 2) {
                            SoftApManager.this.updateApState(14, 0);
                        } else {
                            SoftApManager.this.updateApState(11, 0);
                        }
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mIdleState);
                        SoftApManager.this.mWifiSoftApNotificationManager.clearSoftApClientsNotification();
                        SoftApManager.this.mWifiSoftApNotificationManager.clearConnectedDevice();
                        try {
                            if (SoftApStateMachine.this.mWificond != null) {
                                SoftApStateMachine.this.mWificond.UnregisterCallback(SoftApStateMachine.this.mInterfaceEventHandler);
                            }
                        } catch (RemoteException e) {
                        }
                        SoftApStateMachine.this.mInterfaceEventHandler = null;
                        break;
                    case 3:
                        if (message.obj == SoftApStateMachine.this.mNetworkObserver) {
                            onUpChanged(message.arg1 == 1);
                            break;
                        }
                        break;
                    case 4:
                        SoftApManager.this.mWifiSoftApNotificationManager.connectionStatusChange(message);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        SoftApStateMachine(Looper looper, WifiInjector wifiInjector) {
            super(SoftApManager.TAG, looper);
            this.mWifiInjector = wifiInjector;
            this.mInterfaceEventHandler = new InterfaceEventHandler(this);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }
    }

    public SoftApManager(Looper looper, WifiNative wifiNative, String countryCode, Listener listener, IApInterface apInterface, INetworkManagementService nms, WifiApConfigStore wifiApConfigStore, WifiConfiguration config, WifiMetrics wifiMetrics, WifiInjector wifiInjector, Context context) {
        this.mWifiSoftApNotificationManager = WifiSoftApNotificationManager.getInstance(context);
        this.mWifiNative = wifiNative;
        if (countryCode == null) {
            this.mCountryCode = "CN";
        } else {
            this.mCountryCode = countryCode;
        }
        this.mListener = listener;
        this.mApInterface = apInterface;
        this.mNwService = nms;
        this.mWifiApConfigStore = wifiApConfigStore;
        if (config == null) {
            this.mApConfig = this.mWifiApConfigStore.getApConfiguration();
        } else {
            this.mApConfig = config;
        }
        this.mWifiMetrics = wifiMetrics;
        this.mContext = context;
    }

    public void start() {
        this.mStateMachine.sendMessage(0, this.mApConfig);
    }

    public void stop() {
        this.mStateMachine.sendMessage(1);
    }

    private void updateApState(int state, int reason) {
        if (this.mListener != null) {
            this.mListener.onStateChanged(state, reason);
        }
    }

    public void setDualSapMode(boolean enable) {
        this.mDualSapMode = enable;
    }

    public void setSapChannel(int channel) {
        this.mSoftApChannel = channel;
    }

    private boolean writeDualHostapdConfig(WifiConfiguration wifiConfig) {
        String[] dualApInterfaces = this.mWifiApConfigStore.getDualSapInterfaces();
        if (dualApInterfaces == null || dualApInterfaces.length != 2) {
            Log.e(TAG, " dualApInterfaces is not set or length is not 2");
            return false;
        }
        String authStr;
        String hexSsid = String.format("%x", new Object[]{new BigInteger(1, wifiConfig.SSID.getBytes(StandardCharsets.UTF_8))});
        switch (wifiConfig.getAuthType()) {
            case 1:
                authStr = "wpa-psk " + wifiConfig.preSharedKey;
                break;
            case 4:
                authStr = "wpa2-psk " + wifiConfig.preSharedKey;
                break;
            default:
                authStr = "open";
                break;
        }
        try {
            return this.mWifiNative.runQsapCmd(new StringBuilder().append("softap setsoftap dual2g ").append(dualApInterfaces[0]).append(" ").append(hexSsid).append(" visible 0 ").append(authStr).toString(), "") && this.mWifiNative.runQsapCmd("softap setsoftap dual5g " + dualApInterfaces[1] + " " + hexSsid + " visible 0 " + authStr, "") && this.mWifiNative.runQsapCmd("softap qccmd set dual2g hw_mode=", "g") && this.mWifiNative.runQsapCmd("softap qccmd set dual5g hw_mode=", "a") && this.mWifiNative.runQsapCmd("softap qccmd set dual2g bridge=", this.mApInterface.getInterfaceName()) && this.mWifiNative.runQsapCmd("softap qccmd set dual5g bridge=", this.mApInterface.getInterfaceName());
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in configuring softap for dual mode: " + e);
        }
    }

    private int startDualSoftAp(WifiConfiguration config) {
        WifiConfiguration localConfig = new WifiConfiguration(config);
        if (this.mCountryCode == null || this.mWifiNative.setCountryCodeHal(this.mCountryCode.toUpperCase(Locale.ROOT))) {
            try {
                if (writeDualHostapdConfig(localConfig)) {
                    boolean success = this.mApInterface.startHostapd(this.mDualSapMode);
                    this.mWifiNative.runQsapCmd("softap bridge up ", this.mApInterface.getInterfaceName());
                    if (!success) {
                        Log.e(TAG, "Failed to start hostapd.");
                        return 2;
                    }
                    Log.d(TAG, "Dual Soft AP is started");
                    return 0;
                }
                Log.e(TAG, "Failed to write dual hostapd configuration");
                return 2;
            } catch (RemoteException e) {
                Log.e(TAG, "Exception in starting dual soft AP: " + e);
            }
        } else {
            Log.e(TAG, "Failed to set country code, required for setting up soft ap in 5GHz");
            return 2;
        }
    }

    private int startSoftAp(WifiConfiguration config) {
        if (config == null || config.SSID == null) {
            Log.e(TAG, "Unable to start soft AP without valid configuration");
            return 2;
        } else if (this.mDualSapMode) {
            return startDualSoftAp(config);
        } else {
            WifiConfiguration localConfig = new WifiConfiguration(config);
            int result = ApConfigUtil.updateApChannelConfig(this.mWifiNative, this.mCountryCode, this.mWifiApConfigStore.getAllowed2GChannel(), localConfig);
            if (result != 0) {
                Log.e(TAG, "Failed to update AP band and channel");
                return result;
            } else if (this.mCountryCode == null || this.mWifiNative.setCountryCodeHal(this.mCountryCode.toUpperCase(Locale.ROOT)) || config.apBand != 1) {
                int encryptionType = getIApInterfaceEncryptionType(localConfig);
                if (localConfig.hiddenSSID) {
                    Log.d(TAG, "SoftAP is a hidden network");
                }
                try {
                    byte[] bytes;
                    if (!(localConfig.apBand == 1 || this.mSoftApChannel == 0)) {
                        localConfig.apBand = 0;
                        localConfig.apChannel = this.mSoftApChannel;
                        config.apChannel = localConfig.apChannel;
                    }
                    IApInterface iApInterface = this.mApInterface;
                    byte[] bytes2 = localConfig.SSID.getBytes(StandardCharsets.UTF_8);
                    boolean z = localConfig.hiddenSSID;
                    int i = localConfig.apChannel;
                    if (localConfig.preSharedKey != null) {
                        bytes = localConfig.preSharedKey.getBytes(StandardCharsets.UTF_8);
                    } else {
                        bytes = new byte[0];
                    }
                    if (iApInterface.writeHostapdConfig(bytes2, z, i, encryptionType, bytes)) {
                        if (!this.mApInterface.startHostapd(false)) {
                            Log.e(TAG, "Failed to start hostapd.");
                            return 2;
                        }
                        Log.d(TAG, "Soft AP is started");
                        return 0;
                    }
                    Log.e(TAG, "Failed to write hostapd configuration");
                    return 2;
                } catch (RemoteException e) {
                    Log.e(TAG, "Exception in starting soft AP: " + e);
                }
            } else {
                Log.e(TAG, "Failed to set country code, required for setting up soft ap in 5GHz");
                return 2;
            }
        }
    }

    private static int getIApInterfaceEncryptionType(WifiConfiguration localConfig) {
        switch (localConfig.getAuthType()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 4:
                return 2;
            default:
                return 0;
        }
    }

    private void stopSoftAp() {
        try {
            this.mApConfig.apChannel = 0;
            this.mApInterface.stopHostapd(this.mDualSapMode);
            Log.d(TAG, "Soft AP is stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in stopping soft AP: " + e);
        }
    }
}
