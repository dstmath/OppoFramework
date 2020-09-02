package com.coloros.deepthinker;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import com.coloros.deepthinker.sdk.aidl.platform.IAlgorithmPlatform;
import com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.IAppPredictAidlInterface;
import com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.PredictAABResult;
import com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.PredictResult;
import com.coloros.deepthinker.sdk.aidl.proton.appsort.IAppSort;
import com.coloros.deepthinker.sdk.aidl.proton.apptype.IAppType;
import com.coloros.deepthinker.sdk.aidl.proton.deepsleep.DeepSleepPredictResult;
import com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict;
import com.coloros.deepthinker.sdk.aidl.proton.deepsleep.SleepRecord;
import com.coloros.deepthinker.sdk.aidl.proton.deepsleep.TotalPredictResult;
import com.coloros.deepthinker.sdk.common.utils.SDKLog;
import com.coloros.eventhub.sdk.aidl.EventRequestConfig;
import com.coloros.eventhub.sdk.aidl.IEventCallback;
import com.coloros.eventhub.sdk.aidl.IEventHandleService;
import com.coloros.eventhub.sdk.aidl.TriggerEvent;
import java.util.List;
import java.util.Map;

public class ColorDeepThinkerManager extends ColorDummyDeepThinkerManager {
    private static final String TAG = "ColorDeepThinkerManager";
    private static volatile ColorDeepThinkerManager sInstance;
    private AlgorithmBinderPool mAlgorithmBinderPool = AlgorithmBinderPool.getInstance(this.mContext);
    private final Context mContext;
    private TriggerEventRunnable mTriggereventRunnable;

    private ColorDeepThinkerManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static ColorDeepThinkerManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ColorDeepThinkerManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDeepThinkerManager(context);
                }
            }
        }
        return sInstance;
    }

    public void registerServiceStateObserver(ServiceStateObserver observer) {
        this.mAlgorithmBinderPool.registerServiceStateObserver(observer);
    }

    public IAlgorithmPlatform getPlatformBinder() {
        return this.mAlgorithmBinderPool.getPlatformBinder();
    }

    public IAppPredictAidlInterface getAppPredictBinder() {
        return this.mAlgorithmBinderPool.getAppPredictBinder();
    }

    public IDeepSleepPredict getDeepSleepBinder() {
        return this.mAlgorithmBinderPool.getDeepSleepBinder();
    }

    public IAppType getAppTypeBinder() {
        return this.mAlgorithmBinderPool.getAppTypeBinder();
    }

    public IEventHandleService getEventHandleBinder() {
        return this.mAlgorithmBinderPool.getEventHandleBinder();
    }

    public IAppSort getAppSortBinder() {
        return this.mAlgorithmBinderPool.getAppSortBinder();
    }

    public int getAlgorithmPlatformVersion() {
        try {
            IAlgorithmPlatform platformBinder = getPlatformBinder();
            if (platformBinder != null) {
                return platformBinder.getAlgorithmPlatformVersion();
            }
            return -1;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getAlgorithmPlatformVersion", e);
            return -1;
        }
    }

    public PredictAABResult getPredictAABResult() {
        try {
            IAppPredictAidlInterface appPredictBinder = getAppPredictBinder();
            if (appPredictBinder != null) {
                return appPredictBinder.getPredictAABResult();
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getPredictAABResult", e);
            return null;
        }
    }

    public List<PredictResult> getAppPredictResultMap(String callerName) {
        try {
            IAppPredictAidlInterface appPredictBinder = getAppPredictBinder();
            if (appPredictBinder != null) {
                return appPredictBinder.getAppPredictResultMap(callerName);
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getAppPredictResultMap", e);
            return null;
        }
    }

    public PredictResult getAppPredictResult(String callerName) {
        try {
            IAppPredictAidlInterface appPredictBinder = getAppPredictBinder();
            if (appPredictBinder != null) {
                return appPredictBinder.getAppPredictResult(callerName);
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getAppPredictResult", e);
            return null;
        }
    }

    public DeepSleepPredictResult getDeepSleepPredictResult() {
        try {
            IDeepSleepPredict deepSleepBinder = getDeepSleepBinder();
            if (deepSleepBinder != null) {
                return deepSleepBinder.getDeepSleepPredictResult();
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getDeepSleepPredictResult", e);
            return null;
        }
    }

    public SleepRecord getLastDeepSleepRecord() {
        try {
            IDeepSleepPredict deepSleepBinder = getDeepSleepBinder();
            if (deepSleepBinder != null) {
                return deepSleepBinder.getLastDeepSleepRecord();
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getLastDeepSleepRecord", e);
            return null;
        }
    }

    public TotalPredictResult getDeepSleepTotalPredictResult() {
        try {
            IDeepSleepPredict deepSleepBinder = getDeepSleepBinder();
            if (deepSleepBinder != null) {
                return deepSleepBinder.getDeepSleepTotalPredictResult();
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getDeepSleepTotalPredictResult", e);
            return null;
        }
    }

    public DeepSleepPredictResult getPredictResultWithFeedBack() {
        try {
            IDeepSleepPredict deepSleepBinder = getDeepSleepBinder();
            if (deepSleepBinder != null) {
                return deepSleepBinder.getPredictResultWithFeedBack();
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getPredictResultWithFeedBack", e);
            return null;
        }
    }

    public int getAppType(String packageName) {
        try {
            IAppType appTypeBinder = getAppTypeBinder();
            if (appTypeBinder != null) {
                return appTypeBinder.getAppType(packageName);
            }
            return -1;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getAppType", e);
            return -1;
        }
    }

    public Map getAppTypeMap(List<String> packageNameList) {
        try {
            IAppType appTypeBinder = getAppTypeBinder();
            if (appTypeBinder != null) {
                return appTypeBinder.getAppTypeMap(packageNameList);
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getAppTypeMap", e);
            return null;
        }
    }

    public void triggerHookEvent(TriggerEvent triggerEvent) {
        if (triggerEvent == null) {
            SDKLog.e(TAG, "triggerHookEvent null parameter.");
            return;
        }
        try {
            IEventHandleService eventHandleBinder = getEventHandleBinder();
            if (eventHandleBinder != null) {
                eventHandleBinder.triggerHookEvent(triggerEvent);
            }
        } catch (RemoteException e) {
            SDKLog.e(TAG, "triggerHookEvent", e);
        }
    }

    public void triggerHookEvent(int eventType, int uid, String pkgName, Bundle extra) {
        triggerHookEvent(new TriggerEvent(eventType, uid, pkgName, extra));
    }

    public void triggerHookEventAsync(Handler handler, int eventID, int uid, String pkg, Bundle extra) {
        if (handler != null) {
            TriggerEventRunnable triggerEventRunnable = this.mTriggereventRunnable;
            if (triggerEventRunnable == null) {
                this.mTriggereventRunnable = new TriggerEventRunnable(eventID, uid, pkg, extra);
            } else {
                triggerEventRunnable.mEventID = eventID;
                triggerEventRunnable.mUid = uid;
                triggerEventRunnable.mPkg = pkg;
                triggerEventRunnable.mExtra = extra;
            }
            handler.post(this.mTriggereventRunnable);
        }
    }

    public boolean registerCallback(IEventCallback callback, EventRequestConfig config) {
        if (callback == null || config == null || config.getAllEvents().isEmpty()) {
            SDKLog.e(TAG, "registerCallback invalid para. null or without request config.");
            return false;
        }
        try {
            int fingerPrint = callback.hashCode();
            IEventHandleService eventHandleBinder = getEventHandleBinder();
            if (eventHandleBinder != null) {
                return eventHandleBinder.registerCallback(callback, fingerPrint, config);
            }
        } catch (RemoteException e) {
            SDKLog.e(TAG, "registerCallback", e);
        }
        return false;
    }

    public boolean unregisterCallback(IEventCallback callback) {
        if (callback == null) {
            SDKLog.e(TAG, "unRegisterCallback null parameter.");
            return false;
        }
        try {
            int fingerPrint = callback.hashCode();
            IEventHandleService eventHandleBinder = getEventHandleBinder();
            if (eventHandleBinder != null) {
                return eventHandleBinder.unregisterCallback(fingerPrint);
            }
        } catch (RemoteException e) {
            SDKLog.e(TAG, "unregisterCallback", e);
        }
        return false;
    }

    public List<String> getAppQueueSortedByTime() {
        try {
            IAppSort appSortBinder = getAppSortBinder();
            if (appSortBinder != null) {
                return appSortBinder.getAppQueueSortedByTime();
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getAppQueueSortedByTime", e);
            return null;
        }
    }

    public List<String> getAppQueueSortedByCount() {
        try {
            IAppSort appSortBinder = getAppSortBinder();
            if (appSortBinder != null) {
                return appSortBinder.getAppQueueSortedByCount();
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getAppQueueSortedByCount", e);
            return null;
        }
    }

    public List<String> getAppQueueSortedByComplex() {
        try {
            IAppSort appSortBinder = getAppSortBinder();
            if (appSortBinder != null) {
                return appSortBinder.getAppQueueSortedByComplex();
            }
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "getAppQueueSortedByComplex", e);
            return null;
        }
    }

    class TriggerEventRunnable implements Runnable {
        int mEventID;
        Bundle mExtra;
        String mPkg;
        int mUid;

        TriggerEventRunnable(int eventID, int uid, String pkg, Bundle extra) {
            this.mEventID = eventID;
            this.mUid = uid;
            this.mPkg = pkg;
            this.mExtra = extra;
        }

        public void run() {
            ColorDeepThinkerManager.this.triggerHookEvent(this.mEventID, this.mUid, this.mPkg, this.mExtra);
        }
    }
}
