package com.android.server.power;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.media.AudioAttributes;
import android.util.Log;
import com.android.server.display.IOppoBrightness;

public interface IPswShutdownFeature extends IOppoCommonFeature {
    public static final IPswShutdownFeature DEFAULT = new IPswShutdownFeature() {
        /* class com.android.server.power.IPswShutdownFeature.AnonymousClass1 */
    };
    public static final String NAME = "IPswShutdownFeature";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswShutdownFeature;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void showShutdownBacktrace(boolean spew) {
        Log.d(NAME, "default showShutdownBacktrace");
    }

    default void resetBrightnessAdj(Context context) {
        Log.d(NAME, "default resetBrightnessAdj");
    }

    default void setBeginAnimationTime(long beginAnimTime, boolean isCmcc) {
        Log.d(NAME, "default setBeginAnimationTime");
    }

    default void setBeginAnimationTimeForAtt(long beginAnimTime, boolean isAtt) {
        Log.d(NAME, "default setBeginAnimationTimeForAtt");
    }

    default void shutdownOppoService(Context context) {
        Log.d(NAME, "default shutdownOppoService");
    }

    default void checkShutdownTimeout(Context context, boolean reboot, String reason, int shutdonwVibrateInMs, AudioAttributes vibrateAttribute) {
        Log.d(NAME, "default checkShutdownTimeout");
    }

    default void delayForPlayAnimation() {
        Log.d(NAME, "default delayForPlayAnimation");
    }

    default void storeDellog() {
        Log.d(NAME, "default storeDellog");
    }

    default boolean shouldDoLowLevelShutdown() {
        Log.d(NAME, "default shouldDoLowLevelShutdown");
        return false;
    }

    default void setOppoBrightnessCallback(IOppoBrightness callback) {
        Log.d(NAME, "default setOppoBrightnessCallback");
    }
}
