package com.android.server.net;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.net.NetworkPolicyManager;
import android.os.Handler;
import android.util.SparseIntArray;
import com.android.server.ColorAppActionTracker;
import java.util.ArrayList;

public interface IColorDozeNetworkOptimization extends IOppoCommonFeature {
    public static final IColorDozeNetworkOptimization DEFAULT = new IColorDozeNetworkOptimization() {
        /* class com.android.server.net.IColorDozeNetworkOptimization.AnonymousClass1 */
    };
    public static final String NAME = "IColorDozeNetworkOptimization";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDozeNetworkOptimization;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void initArgs(Context context, NetworkPolicyManagerService npms, Handler handler) {
    }

    default boolean effective() {
        return false;
    }

    default ArrayList<ColorAppActionTracker> getAppActionTracker() {
        return new ArrayList<>();
    }

    default boolean colorUpdateRulesForDeviceIdle(SparseIntArray uidRules, int uid, int uidState, boolean notAllowed) {
        return false;
    }

    default boolean colorUpdateWhitelist(boolean deviceIdle, int appId, boolean oldValue) {
        return oldValue;
    }

    default boolean colorUpdateRulesForWhitelistedPowerSave(int uid, int chain, int uidState, boolean isWhitelisted, boolean notAllowed) {
        return false;
    }

    default boolean colorGetStateForPowerRestrictions(boolean deviceIdle, int uid, int uidState, boolean notAllowed) {
        return NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState);
    }
}
