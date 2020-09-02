package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.OppoActiveModeWarden2;
import com.android.server.wifi.OppoClientModeManager2;
import com.android.server.wifi.ScanOnlyModeManager;
import com.android.server.wifi.WifiNative;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;

public class OppoActiveModeWarden2 {
    static final int BASE = 131072;
    static final int CMD_AP_STOPPED = 131096;
    static final int CMD_CLIENT_MODE_FAILED = 131376;
    static final int CMD_CLIENT_MODE_STOPPED = 131375;
    static final int CMD_SCAN_ONLY_MODE_FAILED = 131276;
    static final int CMD_SCAN_ONLY_MODE_STOPPED = 131275;
    static final int CMD_START_AP = 131093;
    static final int CMD_START_AP_FAILURE = 131094;
    static final int CMD_START_CLIENT_MODE = 131372;
    static final int CMD_START_CLIENT_MODE_FAILURE = 131373;
    static final int CMD_START_SCAN_ONLY_MODE = 131272;
    static final int CMD_START_SCAN_ONLY_MODE_FAILURE = 131273;
    static final int CMD_STOP_AP = 131095;
    static final int CMD_STOP_CLIENT_MODE = 131374;
    static final int CMD_STOP_SCAN_ONLY_MODE = 131274;
    private static final String TAG = "WifiActiveModeWarden2";
    /* access modifiers changed from: private */
    public final ArraySet<ActiveModeManager> mActiveModeManagers = new ArraySet<>();
    private final IBatteryStats mBatteryStats;
    /* access modifiers changed from: private */
    public OppoClientModeManager2.Listener mClientModeCallback;
    private final Context mContext;
    private DefaultModeManager mDefaultModeManager;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final Looper mLooper;
    /* access modifiers changed from: private */
    public ModeStateMachine mModeStateMachine;
    private ScanOnlyModeManager.Listener mScanOnlyCallback;
    /* access modifiers changed from: private */
    public final ScanRequestProxy mScanRequestProxy;
    private final SelfRecovery mSelfRecovery;
    private WifiManager.SoftApCallback mSoftApCallback;
    /* access modifiers changed from: private */
    public BaseWifiDiagnostics mWifiDiagnostics;
    /* access modifiers changed from: private */
    public final WifiInjector mWifiInjector;
    private final WifiNative mWifiNative;
    private WifiNative.StatusListener mWifiNativeStatusListener;

    public void registerClientModeCallback(OppoClientModeManager2.Listener callback) {
        this.mClientModeCallback = callback;
    }

    OppoActiveModeWarden2(WifiInjector wifiInjector, Context context, Looper looper, WifiNative wifiNative, DefaultModeManager defaultModeManager, IBatteryStats batteryStats) {
        this.mWifiInjector = wifiInjector;
        this.mContext = context;
        this.mLooper = looper;
        this.mHandler = new Handler(looper);
        this.mWifiNative = wifiNative;
        this.mDefaultModeManager = defaultModeManager;
        this.mBatteryStats = batteryStats;
        this.mSelfRecovery = this.mWifiInjector.getSelfRecovery();
        this.mWifiDiagnostics = this.mWifiInjector.getWifiDiagnostics();
        this.mScanRequestProxy = this.mWifiInjector.getScanRequestProxy();
        this.mModeStateMachine = new ModeStateMachine();
    }

    public void enterClientMode() {
        changeMode(0);
    }

    public void disableWifi() {
        changeMode(3);
    }

    public void shutdownWifi() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.wifi.$$Lambda$OppoActiveModeWarden2$KYsA68IXFr2dYZnDbNzsKNX4d7Q */

            public final void run() {
                OppoActiveModeWarden2.this.lambda$shutdownWifi$0$OppoActiveModeWarden2();
            }
        });
    }

    public /* synthetic */ void lambda$shutdownWifi$0$OppoActiveModeWarden2() {
        Iterator<ActiveModeManager> it = this.mActiveModeManagers.iterator();
        while (it.hasNext()) {
            it.next().stop();
        }
        updateBatteryStatsWifiState(false);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiActiveModeWarden2");
        pw.println("Current wifi mode: " + getCurrentMode());
        pw.println("NumActiveModeManagers: " + this.mActiveModeManagers.size());
        Iterator<ActiveModeManager> it = this.mActiveModeManagers.iterator();
        while (it.hasNext()) {
            it.next().dump(fd, pw, args);
        }
    }

    /* access modifiers changed from: protected */
    public String getCurrentMode() {
        return this.mModeStateMachine.getCurrentMode();
    }

    private void changeMode(int newMode) {
        this.mModeStateMachine.sendMessage(newMode);
    }

    private class ModeCallback {
        ActiveModeManager mActiveManager;

        private ModeCallback() {
        }

        /* access modifiers changed from: package-private */
        public void setActiveModeManager(ActiveModeManager manager) {
            this.mActiveManager = manager;
        }

        /* access modifiers changed from: package-private */
        public ActiveModeManager getActiveModeManager() {
            return this.mActiveManager;
        }
    }

    /* access modifiers changed from: private */
    public class ModeStateMachine extends StateMachine {
        public static final int CMD_DISABLE_WIFI = 3;
        public static final int CMD_START_CLIENT_MODE = 0;
        public static final int CMD_START_SCAN_ONLY_MODE = 1;
        private final State mClientModeActiveState = new ClientModeActiveState();
        /* access modifiers changed from: private */
        public final State mWifiDisabledState = new WifiDisabledState();

        ModeStateMachine() {
            super(OppoActiveModeWarden2.TAG, OppoActiveModeWarden2.this.mLooper);
            addState(this.mClientModeActiveState);
            addState(this.mWifiDisabledState);
            Log.d(OppoActiveModeWarden2.TAG, "Starting Wifi in WifiDisabledState");
            setInitialState(this.mWifiDisabledState);
            start();
        }

        /* access modifiers changed from: private */
        public String getCurrentMode() {
            return getCurrentState().getName();
        }

        /* access modifiers changed from: private */
        public boolean checkForAndHandleModeChange(Message message) {
            int i = message.what;
            if (i == 0) {
                Log.d(OppoActiveModeWarden2.TAG, "Switching from " + getCurrentMode() + " to ClientMode");
                OppoActiveModeWarden2.this.mModeStateMachine.transitionTo(this.mClientModeActiveState);
                return true;
            } else if (i != 3) {
                return false;
            } else {
                Log.d(OppoActiveModeWarden2.TAG, "Switching from " + getCurrentMode() + " to WifiDisabled");
                OppoActiveModeWarden2.this.mModeStateMachine.transitionTo(this.mWifiDisabledState);
                return true;
            }
        }

        class ModeActiveState extends State {
            ActiveModeManager mManager;

            ModeActiveState() {
            }

            public boolean processMessage(Message message) {
                return false;
            }

            public void exit() {
                ActiveModeManager activeModeManager = this.mManager;
                if (activeModeManager != null) {
                    activeModeManager.stop();
                    OppoActiveModeWarden2.this.mActiveModeManagers.remove(this.mManager);
                }
                OppoActiveModeWarden2.this.updateBatteryStatsWifiState(false);
            }

            public void onModeActivationComplete() {
                updateScanMode();
            }

            private void updateScanMode() {
                boolean scanEnabled = false;
                boolean scanningForHiddenNetworksEnabled = false;
                Iterator it = OppoActiveModeWarden2.this.mActiveModeManagers.iterator();
                while (it.hasNext()) {
                    int scanState = ((ActiveModeManager) it.next()).getScanMode();
                    if (scanState != 0) {
                        if (scanState == 1) {
                            scanEnabled = true;
                        } else if (scanState == 2) {
                            scanEnabled = true;
                            scanningForHiddenNetworksEnabled = true;
                        }
                    }
                }
                OppoActiveModeWarden2.this.mScanRequestProxy.enableScanning(scanEnabled, scanningForHiddenNetworksEnabled);
            }
        }

        class WifiDisabledState extends ModeActiveState {
            WifiDisabledState() {
                super();
            }

            public void enter() {
                Log.d(OppoActiveModeWarden2.TAG, "Entering WifiDisabledState");
            }

            @Override // com.android.server.wifi.OppoActiveModeWarden2.ModeStateMachine.ModeActiveState
            public boolean processMessage(Message message) {
                Log.d(OppoActiveModeWarden2.TAG, "received a message in WifiDisabledState: " + message);
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                return false;
            }

            @Override // com.android.server.wifi.OppoActiveModeWarden2.ModeStateMachine.ModeActiveState
            public void exit() {
            }
        }

        class ClientModeActiveState extends ModeActiveState {
            ClientListener mListener;

            ClientModeActiveState() {
                super();
            }

            private class ClientListener implements OppoClientModeManager2.Listener {
                private ClientListener() {
                }

                @Override // com.android.server.wifi.OppoClientModeManager2.Listener
                public void onStateChanged(int state) {
                    if (this != ClientModeActiveState.this.mListener) {
                        Log.d(OppoActiveModeWarden2.TAG, "Client mode state change from previous manager");
                        return;
                    }
                    Log.d(OppoActiveModeWarden2.TAG, "State changed from client mode. state = " + state);
                    if (state == 4) {
                        OppoActiveModeWarden2.this.mModeStateMachine.sendMessage(OppoActiveModeWarden2.CMD_CLIENT_MODE_FAILED, this);
                    } else if (state == 1) {
                        OppoActiveModeWarden2.this.mModeStateMachine.sendMessage(OppoActiveModeWarden2.CMD_CLIENT_MODE_STOPPED, this);
                    } else if (state == 3) {
                        Log.d(OppoActiveModeWarden2.TAG, "client mode active");
                    }
                }
            }

            public void enter() {
                Log.d(OppoActiveModeWarden2.TAG, "Entering ClientModeActiveState");
                this.mListener = new ClientListener();
                this.mManager = OppoActiveModeWarden2.this.mWifiInjector.makeOppoClientModeManager2(this.mListener);
                this.mManager.start();
                OppoActiveModeWarden2.this.mActiveModeManagers.add(this.mManager);
                OppoActiveModeWarden2.this.updateBatteryStatsWifiState(true);
            }

            @Override // com.android.server.wifi.OppoActiveModeWarden2.ModeStateMachine.ModeActiveState
            public void exit() {
                super.exit();
                this.mListener = null;
            }

            @Override // com.android.server.wifi.OppoActiveModeWarden2.ModeStateMachine.ModeActiveState
            public boolean processMessage(Message message) {
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                int i = message.what;
                if (i != 0) {
                    switch (i) {
                        case OppoActiveModeWarden2.CMD_CLIENT_MODE_STOPPED /*{ENCODED_INT: 131375}*/:
                            if (this.mListener == message.obj) {
                                Log.d(OppoActiveModeWarden2.TAG, "ClientMode stopped, return to WifiDisabledState.");
                                OppoActiveModeWarden2.this.mClientModeCallback.onStateChanged(1);
                                OppoActiveModeWarden2.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mWifiDisabledState);
                                break;
                            } else {
                                Log.d(OppoActiveModeWarden2.TAG, "Client mode state change from previous manager");
                                return true;
                            }
                        case OppoActiveModeWarden2.CMD_CLIENT_MODE_FAILED /*{ENCODED_INT: 131376}*/:
                            if (this.mListener == message.obj) {
                                Log.d(OppoActiveModeWarden2.TAG, "ClientMode failed, return to WifiDisabledState.");
                                OppoActiveModeWarden2.this.mClientModeCallback.onStateChanged(4);
                                OppoActiveModeWarden2.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mWifiDisabledState);
                                break;
                            } else {
                                Log.d(OppoActiveModeWarden2.TAG, "Client mode state change from previous manager");
                                return true;
                            }
                        default:
                            return false;
                    }
                } else {
                    Log.d(OppoActiveModeWarden2.TAG, "Received CMD_START_CLIENT_MODE when active - drop");
                }
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateBatteryStatsWifiState(boolean enabled) {
        if (enabled) {
            try {
                if (this.mActiveModeManagers.size() == 1) {
                    this.mBatteryStats.noteWifiOn();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to note battery stats in wifi");
            }
        } else if (this.mActiveModeManagers.size() == 0) {
            this.mBatteryStats.noteWifiOff();
        }
    }

    private void updateBatteryStatsScanModeActive() {
        try {
            this.mBatteryStats.noteWifiState(1, (String) null);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to note battery stats in wifi");
        }
    }

    /* access modifiers changed from: private */
    public final class WifiNativeStatusListener implements WifiNative.StatusListener {
        private WifiNativeStatusListener() {
        }

        @Override // com.android.server.wifi.WifiNative.StatusListener
        public void onStatusChanged(boolean isReady) {
            if (!isReady) {
                OppoActiveModeWarden2.this.mHandler.post(new Runnable() {
                    /* class com.android.server.wifi.$$Lambda$OppoActiveModeWarden2$WifiNativeStatusListener$9tQiVQkkQCPyB3X9ox6M6H1U1Ek */

                    public final void run() {
                        OppoActiveModeWarden2.WifiNativeStatusListener.this.lambda$onStatusChanged$0$OppoActiveModeWarden2$WifiNativeStatusListener();
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onStatusChanged$0$OppoActiveModeWarden2$WifiNativeStatusListener() {
            Log.e(OppoActiveModeWarden2.TAG, "One of the native daemons died. Triggering recovery");
            OppoActiveModeWarden2.this.mWifiDiagnostics.captureBugReportData(8);
            OppoActiveModeWarden2.this.mWifiInjector.getSelfRecovery().trigger(1);
        }
    }
}
