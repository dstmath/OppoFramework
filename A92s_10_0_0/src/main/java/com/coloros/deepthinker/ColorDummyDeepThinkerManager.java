package com.coloros.deepthinker;

import android.content.Context;
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

public class ColorDummyDeepThinkerManager implements IColorDeepThinkerManager {
    private static volatile ColorDummyDeepThinkerManager sInstance;

    public static ColorDummyDeepThinkerManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ColorDummyDeepThinkerManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyDeepThinkerManager();
                }
            }
        }
        return sInstance;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public void registerServiceStateObserver(ServiceStateObserver observer) {
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public int getAlgorithmPlatformVersion() {
        return -1;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public PredictAABResult getPredictAABResult() {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public List<PredictResult> getAppPredictResultMap(String callerName) {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public PredictResult getAppPredictResult(String callerName) {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public DeepSleepPredictResult getDeepSleepPredictResult() {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public SleepRecord getLastDeepSleepRecord() {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public TotalPredictResult getDeepSleepTotalPredictResult() {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public DeepSleepPredictResult getPredictResultWithFeedBack() {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public int getAppType(String packageName) {
        return -1;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public Map getAppTypeMap(List<String> list) {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public void triggerHookEvent(TriggerEvent triggerEvent) {
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public void triggerHookEvent(int eventType, int uid, String pkgName, Bundle extra) {
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public void triggerHookEventAsync(Handler handler, int eventID, int uid, String pkg, Bundle extra) {
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public boolean registerCallback(IEventCallback callback, EventRequestConfig config) {
        return false;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public boolean unregisterCallback(IEventCallback callback) {
        return false;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public List<String> getAppQueueSortedByTime() {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public List<String> getAppQueueSortedByCount() {
        return null;
    }

    @Override // com.coloros.deepthinker.IColorDeepThinkerManager
    public List<String> getAppQueueSortedByComplex() {
        return null;
    }
}
