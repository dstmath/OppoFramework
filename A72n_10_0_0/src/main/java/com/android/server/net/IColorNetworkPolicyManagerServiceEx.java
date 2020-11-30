package com.android.server.net;

import com.android.server.IOppoCommonManagerServiceEx;

public interface IColorNetworkPolicyManagerServiceEx extends IOppoCommonManagerServiceEx {
    default NetworkPolicyManagerService getNetworkPolicyManagerService() {
        return null;
    }
}
