package com.android.server.power;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.Handler;

public interface IColorScreenOffOptimization extends IOppoCommonFeature {
    public static final IColorScreenOffOptimization DEFAULT = new IColorScreenOffOptimization() {
        /* class com.android.server.power.IColorScreenOffOptimization.AnonymousClass1 */
    };
    public static final String NAME = "IColorScreenOffOptimization";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorScreenOffOptimization;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void initArgs(Context context, Object mLock, PowerManagerService mPowerMS, Handler mHandler, IColorPowerManagerServiceInner inner) {
    }

    default void registerOppoUserPresentReceiver() {
    }

    default void onBootPhaseStep() {
    }

    default void handleScreenOffTimeOutKeyGuardLocked() {
    }

    default void readySendScreenOffTimeOffMessage() {
    }

    default void removeScreenOffTimeOutMessage() {
    }
}
