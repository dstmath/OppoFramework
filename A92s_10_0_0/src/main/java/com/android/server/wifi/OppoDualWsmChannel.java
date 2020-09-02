package com.android.server.wifi;

public class OppoDualWsmChannel {
    private static final String TAG = "OppoDualWsmChannel";
    private static OppoDualWsmChannel mInstance;
    private ClientModeImpl mWifiStateMachine1 = WifiInjector.getInstance().getClientModeImpl();
    private OppoClientModeImpl2 mWifiStateMachine2 = WifiInjector.getInstance().getOppoClientModeImpl2();

    private OppoDualWsmChannel() {
    }

    public static OppoDualWsmChannel getInstance() {
        synchronized (OppoDualWsmChannel.class) {
            if (mInstance == null) {
                mInstance = new OppoDualWsmChannel();
            }
        }
        return mInstance;
    }

    public boolean notifyRemoveNetwork(int networkId) {
        this.mWifiStateMachine2.notifyRemoveNetwork(networkId);
        return true;
    }
}
