package com.android.server.connectivity.oppo;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import com.android.server.connectivity.oppo.IOppoArpPeer;

public interface IOppoGatewayState extends IOppoCommonFeature {
    public static final IOppoGatewayState DEFAULT = new IOppoGatewayState() {
        /* class com.android.server.connectivity.oppo.IOppoGatewayState.AnonymousClass1 */
    };
    public static final String NAME = "IOppoGatewayState";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoGatewayState;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void stopGatewayDetector() {
    }

    default boolean needWaitGatewayDetector() {
        return false;
    }

    default boolean needWaitReevaluateNetwork() {
        return false;
    }

    default void startGatewayDetector(IOppoArpPeer.ArpPeerChangeCallback callback) {
    }

    default void setDuplicateGatewayStatics() {
    }

    default boolean needReevaluateNetwork() {
        return false;
    }

    default void reevaluateNetwork() {
    }

    default void startGatewayProbe() {
    }

    default void restoreLastGatewayState() {
    }

    default void setGatewayStateDone() {
    }

    default boolean isGatewayStateDone() {
        return false;
    }
}
