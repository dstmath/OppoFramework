package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.location.LocAppsOp;
import android.os.Looper;

public interface IPswLocationBlacklistUtil extends IOppoCommonFeature {
    public static final IPswLocationBlacklistUtil DEFAULT = new IPswLocationBlacklistUtil() {
        /* class com.android.server.location.interfaces.IPswLocationBlacklistUtil.AnonymousClass1 */
    };
    public static final String Name = "IPswLocationBlacklistUtil";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswLocationBlacklistUtil;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context context, Looper loop) {
    }

    default boolean isPackageBlocked(String packageName, String provider) {
        return false;
    }

    default void recordPackagesLocationStatus(String packageName, int packageUid, int packagePid, String locationProvider) {
    }

    default void removePackagesLocationStatus(String packageName, int packageUid, int packagePid, String locationProvider) {
    }

    default void getLocAppsOp(int flag, LocAppsOp locAppsOp) {
    }

    default void setLocAppsOp(int cmd, LocAppsOp locAppsOp) {
    }

    default boolean needChangeNotifyStatus(String packageName, boolean isBlocked) {
        return false;
    }
}
