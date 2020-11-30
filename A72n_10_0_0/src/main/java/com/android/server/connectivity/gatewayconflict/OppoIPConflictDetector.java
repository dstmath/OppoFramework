package com.android.server.connectivity.gatewayconflict;

import android.content.Context;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.gatewayconflict.OppoArpPeer;

public class OppoIPConflictDetector extends OppoArpPeer {
    private static final int DUP_ARP_COUNT = 1;
    private static final String TAG = "OppoIPConflictDetector";

    public OppoIPConflictDetector(Context context, NetworkAgentInfo networkInfo, OppoArpPeer.ArpPeerChangeCallback callback) {
        super(context, networkInfo, callback, 1);
        this.mIsIpDetector = true;
    }

    public String getIpConflictMac() {
        if (this.mDupTarget == null || this.mDupTarget.size() <= 0) {
            return null;
        }
        return byteArrayToHex((byte[]) this.mDupTarget.get(0));
    }
}
