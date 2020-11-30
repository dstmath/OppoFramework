package com.android.server.wifi;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.WifiNative;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class OppoClientModeManager2 implements ActiveModeManager {
    private static final String TAG = "OppoClientModeManager2";
    private String mClientInterfaceName;
    private final OppoClientModeImpl2 mClientModeImpl;
    private final Context mContext;
    private boolean mExpectedStop = false;
    private boolean mIfaceIsUp = false;
    private final Listener mListener;
    private final ClientModeStateMachine mStateMachine;
    private final WifiMetrics mWifiMetrics;
    private final WifiNative mWifiNative;

    public interface Listener {
        void onStateChanged(int i);
    }

    OppoClientModeManager2(Context context, Looper looper, WifiNative wifiNative, Listener listener, WifiMetrics wifiMetrics, OppoClientModeImpl2 clientModeImpl) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mListener = listener;
        this.mWifiMetrics = wifiMetrics;
        this.mClientModeImpl = clientModeImpl;
        this.mStateMachine = new ClientModeStateMachine(looper);
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void start() {
        this.mStateMachine.sendMessage(0);
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void stop() {
        Log.d(TAG, " currentstate: " + getCurrentStateName());
        this.mExpectedStop = true;
        if (this.mClientInterfaceName != null) {
            if (this.mIfaceIsUp) {
                updateWifiState(0, 3);
            } else {
                updateWifiState(0, 2);
            }
        }
        this.mStateMachine.quitNow();
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public int getScanMode() {
        return 2;
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("--Dump of ClientModeManager--");
        pw.println("current StateMachine mode: " + getCurrentStateName());
        pw.println("mClientInterfaceName: " + this.mClientInterfaceName);
        pw.println("mIfaceIsUp: " + this.mIfaceIsUp);
    }

    private String getCurrentStateName() {
        IState currentState = this.mStateMachine.getCurrentState();
        if (currentState != null) {
            return currentState.getName();
        }
        return "StateMachine not active";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateWifiState(int newState, int currentState) {
        if (!this.mExpectedStop) {
            this.mListener.onStateChanged(newState);
        } else {
            Log.d(TAG, "expected stop, not triggering callbacks: newState = " + newState);
        }
        if (newState == 4 || newState == 1) {
            this.mExpectedStop = true;
        }
        if (newState != 4) {
            this.mClientModeImpl.setWifiStateForApiCalls(newState);
        }
    }

    /* access modifiers changed from: private */
    public class ClientModeStateMachine extends StateMachine {
        public static final int CMD_INTERFACE_DESTROYED = 4;
        public static final int CMD_INTERFACE_DOWN = 5;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_START = 0;
        private final State mIdleState = new IdleState();
        private final State mStartedState = new StartedState();
        private final WifiNative.InterfaceCallback mWifiNativeInterfaceCallback = new WifiNative.InterfaceCallback() {
            /* class com.android.server.wifi.OppoClientModeManager2.ClientModeStateMachine.AnonymousClass1 */

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDestroyed(String ifaceName) {
                if (OppoClientModeManager2.this.mClientInterfaceName != null && OppoClientModeManager2.this.mClientInterfaceName.equals(ifaceName)) {
                    Log.d(OppoClientModeManager2.TAG, "STA iface " + ifaceName + " was destroyed, stopping client mode");
                    OppoClientModeManager2.this.mClientModeImpl.handleIfaceDestroyed();
                    ClientModeStateMachine.this.sendMessage(4);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onUp(String ifaceName) {
                if (OppoClientModeManager2.this.mClientInterfaceName != null && OppoClientModeManager2.this.mClientInterfaceName.equals(ifaceName)) {
                    ClientModeStateMachine.this.sendMessage(3, 1);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDown(String ifaceName) {
                if (OppoClientModeManager2.this.mClientInterfaceName != null && OppoClientModeManager2.this.mClientInterfaceName.equals(ifaceName)) {
                    ClientModeStateMachine.this.sendMessage(3, 0);
                }
            }
        };

        ClientModeStateMachine(Looper looper) {
            super(OppoClientModeManager2.TAG, looper);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }

        private class IdleState extends State {
            private IdleState() {
            }

            public void enter() {
                Log.d(OppoClientModeManager2.TAG, "entering IdleState");
                OppoClientModeManager2.this.mClientInterfaceName = null;
                OppoClientModeManager2.this.mIfaceIsUp = false;
            }

            public boolean processMessage(Message message) {
                if (message.what != 0) {
                    Log.d(OppoClientModeManager2.TAG, "received an invalid message: " + message);
                    return false;
                }
                OppoClientModeManager2.this.updateWifiState(2, 1);
                OppoClientModeManager2.this.mClientInterfaceName = OppoClientModeManager2.this.mWifiNative.setupInterfaceForClientInConnectivityMode(ClientModeStateMachine.this.mWifiNativeInterfaceCallback);
                if (TextUtils.isEmpty(OppoClientModeManager2.this.mClientInterfaceName)) {
                    Log.e(OppoClientModeManager2.TAG, "Failed to create ClientInterface. Sit in Idle");
                    OppoClientModeManager2.this.updateWifiState(4, 2);
                    OppoClientModeManager2.this.updateWifiState(1, 4);
                } else {
                    ClientModeStateMachine clientModeStateMachine = ClientModeStateMachine.this;
                    clientModeStateMachine.transitionTo(clientModeStateMachine.mStartedState);
                }
                return true;
            }
        }

        private class StartedState extends State {
            private StartedState() {
            }

            private void onUpChanged(boolean isUp) {
                if (isUp != OppoClientModeManager2.this.mIfaceIsUp) {
                    OppoClientModeManager2.this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.d(OppoClientModeManager2.TAG, "Wifi is ready to use for client mode");
                        OppoClientModeManager2.this.mClientModeImpl.setOperationalMode(1, OppoClientModeManager2.this.mClientInterfaceName);
                        OppoClientModeManager2.this.updateWifiState(3, 2);
                    } else if (!OppoClientModeManager2.this.mClientModeImpl.isConnectedMacRandomizationEnabled()) {
                        Log.d(OppoClientModeManager2.TAG, "interface down!");
                        OppoClientModeManager2.this.updateWifiState(4, 3);
                        OppoClientModeManager2.this.mStateMachine.sendMessage(5);
                    }
                }
            }

            public void enter() {
                Log.d(OppoClientModeManager2.TAG, "entering StartedState");
                OppoClientModeManager2.this.mIfaceIsUp = false;
                onUpChanged(OppoClientModeManager2.this.mWifiNative.isInterfaceUp(OppoClientModeManager2.this.mClientInterfaceName));
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i != 0) {
                    boolean isUp = false;
                    if (i == 3) {
                        if (message.arg1 == 1) {
                            isUp = true;
                        }
                        onUpChanged(isUp);
                    } else if (i == 4) {
                        Log.d(OppoClientModeManager2.TAG, "interface destroyed - client mode stopping");
                        OppoClientModeManager2.this.updateWifiState(0, 3);
                        OppoClientModeManager2.this.mClientInterfaceName = null;
                        ClientModeStateMachine clientModeStateMachine = ClientModeStateMachine.this;
                        clientModeStateMachine.transitionTo(clientModeStateMachine.mIdleState);
                    } else if (i != 5) {
                        return false;
                    } else {
                        Log.e(OppoClientModeManager2.TAG, "Detected an interface down, reporting failure to SelfRecovery");
                        OppoClientModeManager2.this.mClientModeImpl.failureDetected(2);
                        OppoClientModeManager2.this.updateWifiState(0, 4);
                        ClientModeStateMachine clientModeStateMachine2 = ClientModeStateMachine.this;
                        clientModeStateMachine2.transitionTo(clientModeStateMachine2.mIdleState);
                    }
                }
                return true;
            }

            public void exit() {
                OppoClientModeManager2.this.mClientModeImpl.setOperationalMode(4, null);
                if (OppoClientModeManager2.this.mClientInterfaceName != null) {
                    OppoClientModeManager2.this.mWifiNative.teardownInterface(OppoClientModeManager2.this.mClientInterfaceName);
                    OppoClientModeManager2.this.mClientInterfaceName = null;
                    OppoClientModeManager2.this.mIfaceIsUp = false;
                }
                OppoClientModeManager2.this.updateWifiState(1, 0);
                OppoClientModeManager2.this.mStateMachine.quitNow();
            }
        }
    }
}
