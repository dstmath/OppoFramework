package com.android.server;

public interface INetworkManagementServiceInner {
    default void closeSocketsForHans(int chain, String chainName) {
    }
}
