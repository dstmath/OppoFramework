package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.util.ApConfigUtil;
import java.util.ArrayList;
import java.util.Locale;

public class SoftApManager {
    private static final String TAG = "SoftApManager";
    private final ArrayList<Integer> mAllowed2GChannels;
    private final String mCountryCode;
    private final String mInterfaceName;
    private final Listener mListener;
    private final INetworkManagementService mNmService;
    private final SoftApStateMachine mStateMachine;
    private final WifiNative mWifiNative;

    public interface Listener {
        void onStateChanged(int i, int i2);
    }

    private class SoftApStateMachine extends StateMachine {
        public static final int CMD_START = 0;
        public static final int CMD_STOP = 1;
        private final State mIdleState = new IdleState(this, null);
        private final State mStartedState = new StartedState(this, null);

        private class IdleState extends State {
            /* synthetic */ IdleState(SoftApStateMachine this$1, IdleState idleState) {
                this();
            }

            private IdleState() {
            }

            public void enter() {
                Log.d(SoftApManager.TAG, getName());
            }

            public boolean processMessage(Message message) {
                switch (message.what) {
                    case 0:
                        SoftApManager.this.updateApState(12, 0);
                        int result = SoftApManager.this.startSoftAp((WifiConfiguration) message.obj);
                        if (result != 0) {
                            int reason = 0;
                            if (result == 1) {
                                reason = 1;
                            }
                            SoftApManager.this.updateApState(14, reason);
                            break;
                        }
                        SoftApManager.this.updateApState(13, 0);
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mStartedState);
                        break;
                }
                return true;
            }
        }

        private class StartedState extends State {
            /* synthetic */ StartedState(SoftApStateMachine this$1, StartedState startedState) {
                this();
            }

            private StartedState() {
            }

            public void enter() {
                Log.d(SoftApManager.TAG, getName());
            }

            public boolean processMessage(Message message) {
                switch (message.what) {
                    case 0:
                        break;
                    case 1:
                        SoftApManager.this.updateApState(10, 0);
                        SoftApManager.this.stopSoftAp();
                        SoftApManager.this.updateApState(11, 0);
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mIdleState);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        SoftApStateMachine(Looper looper) {
            super(SoftApManager.TAG, looper);
            addState(this.mIdleState);
            addState(this.mStartedState, this.mIdleState);
            setInitialState(this.mIdleState);
            start();
        }
    }

    public SoftApManager(Looper looper, WifiNative wifiNative, INetworkManagementService nmService, String countryCode, ArrayList<Integer> allowed2GChannels, Listener listener) {
        this.mStateMachine = new SoftApStateMachine(looper);
        this.mNmService = nmService;
        this.mWifiNative = wifiNative;
        if (countryCode == null) {
            this.mCountryCode = "CN";
        } else {
            this.mCountryCode = countryCode;
        }
        this.mAllowed2GChannels = allowed2GChannels;
        this.mListener = listener;
        this.mInterfaceName = this.mWifiNative.getInterfaceName();
    }

    public void start(WifiConfiguration config) {
        this.mStateMachine.sendMessage(0, config);
    }

    public void stop() {
        this.mStateMachine.sendMessage(1);
    }

    private void updateApState(int state, int reason) {
        if (this.mListener != null) {
            this.mListener.onStateChanged(state, reason);
        }
    }

    private int startSoftAp(WifiConfiguration config) {
        if (config == null) {
            Log.e(TAG, "Unable to start soft AP without configuration");
            return 2;
        }
        WifiConfiguration localConfig = new WifiConfiguration(config);
        if (this.mCountryCode == null || this.mWifiNative.setCountryCodeHal(this.mCountryCode.toUpperCase(Locale.ROOT)) || config.apBand != 1) {
            int result = ApConfigUtil.updateApChannelConfig(this.mWifiNative, this.mCountryCode, this.mAllowed2GChannels, localConfig);
            if (result != 0) {
                Log.e(TAG, "Failed to update AP band and channel");
                return result;
            }
            try {
                this.mNmService.startAccessPoint(localConfig, this.mInterfaceName);
                Log.d(TAG, "Soft AP is started");
                return 0;
            } catch (Exception e) {
                Log.e(TAG, "Exception in starting soft AP: " + e);
                return 2;
            }
        }
        Log.e(TAG, "Failed to set country code, required for setting up soft ap in 5GHz");
        return 2;
    }

    private void stopSoftAp() {
        try {
            this.mNmService.stopAccessPoint(this.mInterfaceName);
            Log.d(TAG, "Soft AP is stopped");
        } catch (Exception e) {
            Log.e(TAG, "Exception in stopping soft AP: " + e);
        }
    }
}
