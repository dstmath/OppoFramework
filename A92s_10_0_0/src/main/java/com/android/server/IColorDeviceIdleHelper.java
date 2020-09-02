package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import java.io.PrintWriter;
import java.util.ArrayList;

public interface IColorDeviceIdleHelper extends IOppoCommonFeature {
    public static final IColorDeviceIdleHelper DEFAULT = new IColorDeviceIdleHelper() {
        /* class com.android.server.IColorDeviceIdleHelper.AnonymousClass1 */
    };
    public static final String NAME = "IColorDeviceIdleHelper";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDeviceIdleHelper;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void initArgs(Context context, DeviceIdleController controller, ContentObserver constants, IColorDeviceIdleControllerInner inner) {
    }

    default boolean isAutoPowerModesEnabled() {
        return true;
    }

    default long getTotalIntervalToIdle() {
        return 1800000;
    }

    default void onMotionDetected(int state, int typeMotion) {
    }

    default void onDeepIdleOn(ArrayList<String> arrayList) {
    }

    default void onLightIdleOn(ArrayList<String> arrayList) {
    }

    default void onIdleExit() {
    }

    default boolean onScreenOff() {
        return false;
    }

    default void onScreenOn() {
    }

    default void dump(PrintWriter pw) {
    }

    default void motionDetected(int mState, int type) {
    }

    default void updateLastLightTrafficRecord() {
    }

    default void updateLastDeepTrafficRecord() {
    }

    default boolean isDeepInTraffic() {
        return false;
    }

    default boolean isLightInTraffic() {
        return false;
    }

    default void enterDeepSleepQuickly() {
    }

    default void removePackage(Intent intent) {
    }

    default boolean isInited() {
        return false;
    }
}
