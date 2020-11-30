package com.android.server.net;

import android.content.Context;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class ColorDummyNetworkPolicyManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IColorNetworkPolicyManagerServiceEx {
    private final NetworkPolicyManagerService mNetworkPolicyMS;

    public ColorDummyNetworkPolicyManagerServiceEx(Context context, NetworkPolicyManagerService npms) {
        super(context);
        this.mNetworkPolicyMS = npms;
    }

    @Override // com.android.server.net.IColorNetworkPolicyManagerServiceEx
    public NetworkPolicyManagerService getNetworkPolicyManagerService() {
        return this.mNetworkPolicyMS;
    }
}
