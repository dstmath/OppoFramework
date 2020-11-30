package com.coloros.deepthinker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import com.coloros.deepthinker.sdk.aidl.platform.IAlgorithmBinderPool;
import com.coloros.deepthinker.sdk.aidl.platform.IAlgorithmPlatform;
import com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.IAppPredictAidlInterface;
import com.coloros.deepthinker.sdk.aidl.proton.appsort.IAppSort;
import com.coloros.deepthinker.sdk.aidl.proton.apptype.IAppType;
import com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict;
import com.coloros.deepthinker.sdk.common.utils.SDKLog;
import com.coloros.eventhub.sdk.aidl.IEventHandleService;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AlgorithmBinderPool {
    private static final int LAZY_RETRY_BASE = 100;
    private static final int RECONNECT_SERVICE_TIMEOUT_MILLS = 2000;
    private static final String SERVER_ACTION = "com.coloros.deepthinker.services.AIAlgorithmService";
    private static final String SERVER_PACKAGE = "com.coloros.deepthinker";
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_DISCONNECTED = 3;
    private static final int STATE_INIT = 0;
    private static final String TAG = "AlgorithmBinderPool";
    private static volatile AlgorithmBinderPool mInstance;
    private final Object LOCK = new Object();
    private final Object OBSERVER_LOCK = new Object();
    private IAppPredictAidlInterface mAppPredictBinder;
    private IAppSort mAppSortBinder;
    private IAppType mAppTypeBinder;
    private IAlgorithmBinderPool mBinderPool;
    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        /* class com.coloros.deepthinker.AlgorithmBinderPool.AnonymousClass2 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            SDKLog.d(AlgorithmBinderPool.TAG, "on Service Connected");
            AlgorithmBinderPool.this.mBinderPool = IAlgorithmBinderPool.Stub.asInterface(service);
            AlgorithmBinderPool.this.setState(2);
            try {
                AlgorithmBinderPool.this.mBinderPool.asBinder().linkToDeath(AlgorithmBinderPool.this.mDeathRecipient, 0);
            } catch (RemoteException e) {
                SDKLog.e(AlgorithmBinderPool.TAG, "on Service linkToDeath error: ", e);
            }
            if (AlgorithmBinderPool.this.mCountDownLatch != null) {
                AlgorithmBinderPool.this.mCountDownLatch.countDown();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            SDKLog.d(AlgorithmBinderPool.TAG, "on Service Disconnected");
            boolean legalState = false;
            synchronized (AlgorithmBinderPool.this.OBSERVER_LOCK) {
                if (AlgorithmBinderPool.this.getState() == 2) {
                    AlgorithmBinderPool.this.setState(3);
                    legalState = true;
                }
            }
            if (legalState) {
                AlgorithmBinderPool.this.mBinderPool = null;
                AlgorithmBinderPool.this.onServiceDied();
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("onServiceDisconnected: current illeagalState ");
            AlgorithmBinderPool algorithmBinderPool = AlgorithmBinderPool.this;
            sb.append(algorithmBinderPool.logState(algorithmBinderPool.getState()));
            SDKLog.w(AlgorithmBinderPool.TAG, sb.toString());
        }
    };
    private final Context mContext;
    private CountDownLatch mCountDownLatch;
    private int mCurrentRetryIndex;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.coloros.deepthinker.AlgorithmBinderPool.AnonymousClass1 */

        public void binderDied() {
            SDKLog.d(AlgorithmBinderPool.TAG, "Invoke binder died ");
            boolean legalState = false;
            synchronized (AlgorithmBinderPool.this.OBSERVER_LOCK) {
                if (AlgorithmBinderPool.this.getState() == 2) {
                    AlgorithmBinderPool.this.setState(3);
                    legalState = true;
                }
            }
            if (legalState) {
                if (AlgorithmBinderPool.this.mBinderPool != null) {
                    AlgorithmBinderPool.this.mBinderPool.asBinder().unlinkToDeath(AlgorithmBinderPool.this.mDeathRecipient, 0);
                    AlgorithmBinderPool.this.mBinderPool = null;
                }
                AlgorithmBinderPool.this.tryConnectBinder();
                AlgorithmBinderPool.this.onServiceDied();
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("binderDied: current illeagalState ");
            AlgorithmBinderPool algorithmBinderPool = AlgorithmBinderPool.this;
            sb.append(algorithmBinderPool.logState(algorithmBinderPool.getState()));
            SDKLog.w(AlgorithmBinderPool.TAG, sb.toString());
        }
    };
    private IDeepSleepPredict mDeepSleepBinder;
    private IEventHandleService mEventHandleBinder;
    private boolean mLazyRetry;
    private IAlgorithmPlatform mPlatformBinder;
    private volatile int mState = 0;
    private WeakHashMap<ServiceStateObserver, Object> mStateObservers = new WeakHashMap<>();

    private void invalid() {
        if (this.mState == 3) {
            this.mPlatformBinder = null;
            this.mEventHandleBinder = null;
            this.mAppPredictBinder = null;
            this.mDeepSleepBinder = null;
            this.mAppTypeBinder = null;
            this.mAppSortBinder = null;
        }
    }

    public static AlgorithmBinderPool getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AlgorithmBinderPool.class) {
                if (mInstance == null) {
                    mInstance = new AlgorithmBinderPool(context);
                }
            }
        }
        return mInstance;
    }

    private AlgorithmBinderPool(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void registerServiceStateObserver(ServiceStateObserver observer) {
        this.mStateObservers.putIfAbsent(observer, null);
    }

    public boolean isAvaliable() {
        return getState() == 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onServiceDied() {
        if (isOnMainThread()) {
            new Thread(new Runnable() {
                /* class com.coloros.deepthinker.AlgorithmBinderPool.AnonymousClass3 */

                public void run() {
                    AlgorithmBinderPool.this.deliveryOnServiceDied();
                }
            }).start();
        } else {
            deliveryOnServiceDied();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deliveryOnServiceDied() {
        ServiceStateObserver[] serviceStateObserverArr = (ServiceStateObserver[]) this.mStateObservers.keySet().toArray(new ServiceStateObserver[0]);
        for (ServiceStateObserver observer : serviceStateObserverArr) {
            if (observer != null) {
                observer.onServiceDied();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0064, code lost:
        if (getState() != 2) goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0066, code lost:
        setState(3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0069, code lost:
        setLazyRetryTag(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x007b, code lost:
        if (getState() == 2) goto L_0x0069;
     */
    private void connectBinderPoolService() {
        if (isOnMainThread()) {
            SDKLog.w(TAG, "bind service on main thread, ignore ileagal usage.");
        } else if (getState() == 2 || getState() == 1) {
            SDKLog.w(TAG, "Already connected or connecting, do not reconnect again.");
        } else if (!shouldDoConnect()) {
            SDKLog.w(TAG, "connectBinderPoolService: connect request intercept by lazyRetry.");
        } else {
            SDKLog.d(TAG, "start connect Binder PoolService");
            setState(1);
            Intent intent = new Intent();
            intent.setAction(SERVER_ACTION);
            intent.setPackage(SERVER_PACKAGE);
            if (this.mContext.bindService(intent, this.mBinderPoolConnection, 1)) {
                this.mCountDownLatch = new CountDownLatch(1);
                try {
                    this.mCountDownLatch.await(2000, TimeUnit.MILLISECONDS);
                    this.mCountDownLatch = null;
                } catch (InterruptedException e) {
                    SDKLog.e(TAG, "connectBinderPoolService error: ", e);
                    this.mCountDownLatch = null;
                } catch (Throwable th) {
                    this.mCountDownLatch = null;
                    if (getState() != 2) {
                        setState(3);
                    }
                    setLazyRetryTag(false);
                    throw th;
                }
            } else {
                setState(3);
                setLazyRetryTag(true);
                SDKLog.e(TAG, "Error: Bind Algorithm Service Failed, not exist!");
            }
            SDKLog.d(TAG, "end connect Binder PoolService");
        }
    }

    public IBinder queryBinder(int binderCode) {
        SDKLog.i(TAG, "queryBinder: code " + binderCode);
        int state = getState();
        if (state == 1) {
            SDKLog.w(TAG, "queryBinder: query when connecting!");
            return null;
        }
        if (state == 3 || state == 0) {
            tryConnectBinder();
        }
        try {
            if (this.mBinderPool != null) {
                return this.mBinderPool.queryBinder(binderCode);
            }
            SDKLog.d(TAG, "queryBinder: Binder pool is null");
            return null;
        } catch (RemoteException e) {
            SDKLog.e(TAG, "queryBinder error: ", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryConnectBinder() {
        Thread thread = new Thread(new Runnable() {
            /* class com.coloros.deepthinker.AlgorithmBinderPool.AnonymousClass4 */

            public void run() {
                SDKLog.d(AlgorithmBinderPool.TAG, "tryConnectBinder : start connect on async thread.");
                AlgorithmBinderPool.this.connectBinderPoolService();
                SDKLog.d(AlgorithmBinderPool.TAG, "tryConnectBinder : end connect on async thread.");
            }
        });
        thread.start();
        if (!isOnMainThread()) {
            try {
                thread.join(2000);
            } catch (InterruptedException e) {
                SDKLog.e(TAG, "tryConnectBinder: interrupted.", e);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("tryConnectBinder end. On Async Thread, connect ");
            sb.append(getState() == 2 ? "sucess." : "timeout.");
            SDKLog.d(TAG, sb.toString());
            return;
        }
        SDKLog.d(TAG, "tryConnectBinder end. On Main Thread, reutrn directly.");
    }

    private void setLazyRetryTag(boolean lazyRetry) {
        this.mLazyRetry = lazyRetry;
        if (!lazyRetry) {
            this.mCurrentRetryIndex = 0;
        }
    }

    private boolean shouldDoConnect() {
        if (this.mLazyRetry) {
            int random = new Random(System.currentTimeMillis()).nextInt(LAZY_RETRY_BASE);
            int i = this.mCurrentRetryIndex;
            this.mCurrentRetryIndex = i + 1;
            if (random > i) {
                return false;
            }
        }
        return true;
    }

    private boolean isOnMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setState(int state) {
        synchronized (this.LOCK) {
            this.mState = state;
            invalid();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getState() {
        int i;
        synchronized (this.LOCK) {
            i = this.mState;
        }
        return i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String logState(int state) {
        if (state == 0) {
            return "InitState";
        }
        if (state == 1) {
            return "ConnectingState";
        }
        if (state == 2) {
            return "ConnectedState";
        }
        if (state != 3) {
            return "NoSuchState";
        }
        return "DisconnectedState";
    }

    private boolean isAlive(IBinder binder) {
        return binder != null && binder.isBinderAlive() && binder.pingBinder();
    }

    public IAlgorithmPlatform getPlatformBinder() {
        if (this.mPlatformBinder != null && isAvaliable() && isAlive(this.mPlatformBinder.asBinder())) {
            return this.mPlatformBinder;
        }
        this.mPlatformBinder = IAlgorithmPlatform.Stub.asInterface(queryBinder(1));
        return this.mPlatformBinder;
    }

    public IEventHandleService getEventHandleBinder() {
        if (this.mEventHandleBinder != null && isAvaliable() && isAlive(this.mEventHandleBinder.asBinder())) {
            return this.mEventHandleBinder;
        }
        this.mEventHandleBinder = IEventHandleService.Stub.asInterface(queryBinder(5));
        return this.mEventHandleBinder;
    }

    public IAppPredictAidlInterface getAppPredictBinder() {
        if (this.mAppPredictBinder != null && isAvaliable() && isAlive(this.mAppPredictBinder.asBinder())) {
            return this.mAppPredictBinder;
        }
        this.mAppPredictBinder = IAppPredictAidlInterface.Stub.asInterface(queryBinder(2));
        return this.mAppPredictBinder;
    }

    public IDeepSleepPredict getDeepSleepBinder() {
        if (this.mDeepSleepBinder != null && isAvaliable() && isAlive(this.mDeepSleepBinder.asBinder())) {
            return this.mDeepSleepBinder;
        }
        this.mDeepSleepBinder = IDeepSleepPredict.Stub.asInterface(queryBinder(3));
        return this.mDeepSleepBinder;
    }

    public IAppType getAppTypeBinder() {
        if (this.mAppTypeBinder != null && isAvaliable() && isAlive(this.mAppTypeBinder.asBinder())) {
            return this.mAppTypeBinder;
        }
        this.mAppTypeBinder = IAppType.Stub.asInterface(queryBinder(4));
        return this.mAppTypeBinder;
    }

    public IAppSort getAppSortBinder() {
        if (this.mAppSortBinder != null && isAvaliable() && isAlive(this.mAppSortBinder.asBinder())) {
            return this.mAppSortBinder;
        }
        this.mAppSortBinder = IAppSort.Stub.asInterface(queryBinder(6));
        return this.mAppSortBinder;
    }
}
