package com.coloros.deepthinker;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Bundle;
import android.os.Handler;
import com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.PredictAABResult;
import com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.PredictResult;
import com.coloros.deepthinker.sdk.aidl.proton.deepsleep.DeepSleepPredictResult;
import com.coloros.deepthinker.sdk.aidl.proton.deepsleep.SleepRecord;
import com.coloros.deepthinker.sdk.aidl.proton.deepsleep.TotalPredictResult;
import com.coloros.eventhub.sdk.aidl.EventRequestConfig;
import com.coloros.eventhub.sdk.aidl.IEventCallback;
import com.coloros.eventhub.sdk.aidl.TriggerEvent;
import java.util.List;
import java.util.Map;

public interface IColorDeepThinkerManager extends IOppoCommonFeature {
    public static final IColorDeepThinkerManager DEFAULT = new IColorDeepThinkerManager() {
        /* class com.coloros.deepthinker.IColorDeepThinkerManager.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorDeepThinkerManager getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDeepThinkerManager;
    }

    default void registerServiceStateObserver(ServiceStateObserver observer) {
    }

    default int getAlgorithmPlatformVersion() {
        return -1;
    }

    default PredictAABResult getPredictAABResult() {
        return null;
    }

    default List<PredictResult> getAppPredictResultMap(String callerName) {
        return null;
    }

    default PredictResult getAppPredictResult(String callerName) {
        return null;
    }

    default DeepSleepPredictResult getDeepSleepPredictResult() {
        return null;
    }

    default SleepRecord getLastDeepSleepRecord() {
        return null;
    }

    default TotalPredictResult getDeepSleepTotalPredictResult() {
        return null;
    }

    default DeepSleepPredictResult getPredictResultWithFeedBack() {
        return null;
    }

    default int getAppType(String packageName) {
        return -1;
    }

    default Map getAppTypeMap(List<String> list) {
        return null;
    }

    default void triggerHookEvent(TriggerEvent triggerEvent) {
    }

    default void triggerHookEvent(int eventType, int uid, String pkgName, Bundle extra) {
    }

    default void triggerHookEventAsync(Handler handler, int eventID, int uid, String pkg, Bundle extra) {
    }

    default boolean registerCallback(IEventCallback callback, EventRequestConfig config) {
        return false;
    }

    default boolean unregisterCallback(IEventCallback callback) {
        return false;
    }

    default List<String> getAppQueueSortedByTime() {
        return null;
    }

    default List<String> getAppQueueSortedByCount() {
        return null;
    }

    default List<String> getAppQueueSortedByComplex() {
        return null;
    }
}
