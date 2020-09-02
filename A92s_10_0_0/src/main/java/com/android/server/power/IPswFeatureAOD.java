package com.android.server.power;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.ContentResolver;

public interface IPswFeatureAOD extends IOppoCommonFeature {
    public static final IPswFeatureAOD DEFAULT = new IPswFeatureAOD() {
        /* class com.android.server.power.IPswFeatureAOD.AnonymousClass1 */
    };
    public static final String NAME = "IPswFeatureAOD";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswFeatureAOD;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void testfunc() {
    }

    default void init(IPswPowerManagerServiceAODInner inner) {
    }

    default void setDozeOverride(int screenState, int screenBrightness) {
    }

    default void setDozeOverrideFromDreamManager(int screenState, int screenBrightness) {
    }

    default int setDozeOverrideFromDreamManagerInternal(int screenState, int screenBrightness) {
        return screenState;
    }

    default void systemReady(ContentResolver resolver) {
    }

    default boolean isShouldGoAod() {
        return false;
    }

    default void getAodSettingStatus() {
    }

    default void handleAodChanged() {
    }

    default void onDisplayStateChange(int state) {
    }

    default void notifySfUnBlockScreenOn() {
    }
}
