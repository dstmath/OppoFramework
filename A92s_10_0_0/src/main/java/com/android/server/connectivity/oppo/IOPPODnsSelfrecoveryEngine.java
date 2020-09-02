package com.android.server.connectivity.oppo;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import com.android.server.connectivity.NetworkAgentInfo;

public interface IOPPODnsSelfrecoveryEngine extends IOppoCommonFeature {
    public static final IOPPODnsSelfrecoveryEngine DEFAULT = new IOPPODnsSelfrecoveryEngine() {
        /* class com.android.server.connectivity.oppo.IOPPODnsSelfrecoveryEngine.AnonymousClass1 */
    };
    public static final String NAME = "IOPPODnsSelfrecoveryEngine";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOPPODnsSelfrecoveryEngine;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void onNetworkConnected(NetworkAgentInfo nai, int trigerType) {
    }
}
