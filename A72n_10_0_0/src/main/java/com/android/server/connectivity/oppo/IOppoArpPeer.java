package com.android.server.connectivity.oppo;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.net.Network;
import java.net.Inet4Address;

public interface IOppoArpPeer extends IOppoCommonFeature {
    public static final int ARP_FIRST_RESPONSE_TIMEOUT = 2000;
    public static final IOppoArpPeer DEFAULT = new IOppoArpPeer() {
        /* class com.android.server.connectivity.oppo.IOppoArpPeer.AnonymousClass1 */
    };
    public static final String NAME = "IOppoArpPeer";

    public interface ArpPeerChangeCallback {
        void onArpReponseChanged(int i, Network network);
    }

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoIPConflictDetector;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean doDupArp(String iface, Inet4Address myAddress, Inet4Address target) {
        return false;
    }

    default void close() {
    }

    default String getIpConflictMac() {
        return null;
    }
}
