package com.android.server;

public interface IColorNetworkPolicyManagerServiceInner {
    public static final IColorNetworkPolicyManagerServiceInner DEFAULT = new IColorNetworkPolicyManagerServiceInner() {
        /* class com.android.server.IColorNetworkPolicyManagerServiceInner.AnonymousClass1 */
    };

    default void updateRulesForAllAppsUL() {
    }

    default void setUidFirewallRule(int chain, int uid, int rule) {
    }
}
