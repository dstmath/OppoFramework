package com.android.server.connectivity.oppo;

import android.content.Context;
import com.android.server.connectivity.oppo.IOppoArpPeer;

public abstract class OppoBaseNetworkAgentInfo {
    private final Context mContext;
    public IOppoGatewayState mGatewayState = null;

    public OppoBaseNetworkAgentInfo(Context context) {
        this.mContext = context;
    }

    public void startGatewayDetector(IOppoArpPeer.ArpPeerChangeCallback callback) {
        IOppoGatewayState iOppoGatewayState = this.mGatewayState;
        if (iOppoGatewayState != null) {
            iOppoGatewayState.startGatewayDetector(callback);
        }
    }
}
