package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IPswLbsRepairer extends IOppoCommonFeature {
    public static final IPswLbsRepairer DEFAULT = new IPswLbsRepairer() {
        /* class com.android.server.location.interfaces.IPswLbsRepairer.AnonymousClass1 */
    };
    public static final String Name = "IPswLbsRepairer";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswLbsRepairer;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void onAddMockProvider(String packageName, String providerName) {
    }

    default void onRemoveMockProvider(String packageName, String providerName) {
    }

    default int getRec() {
        return -1;
    }

    default boolean ignoreDisabled(String name, boolean enabled) {
        return false;
    }

    default void updateSettings(String name, int uid) {
    }

    default void getProviderStatus(String providerName, boolean enable, boolean useable, boolean allowed, boolean forceShow, int uid) {
    }

    default boolean isForegroundActivity(int uidImportance) {
        return false;
    }
}
