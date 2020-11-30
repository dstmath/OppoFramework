package com.android.server.am;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IColorKeyEventObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.am.ColorKeyEventUtil;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.policy.OppoPhoneWindowManager;
import java.util.Map;

public class ColorKeyEventUtil {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String TAG = "ColorKeyEventUtil";
    private static volatile ColorKeyEventUtil sInstance = null;
    private Handler mHandler = null;
    private volatile boolean mHasObserver = DEBUG;
    private Map<Integer, Integer> mKeyCodeFlagMap = new ArrayMap();
    private final Map<String, OnKeyEventObserver> mKeyEventObservers = new ArrayMap();

    public static ColorKeyEventUtil getInstance() {
        if (sInstance == null) {
            synchronized (ColorKeyEventUtil.class) {
                if (sInstance == null) {
                    sInstance = new ColorKeyEventUtil();
                }
            }
        }
        return sInstance;
    }

    private ColorKeyEventUtil() {
        HandlerThread thread = new HandlerThread("Thread for notify KeyEvent");
        thread.start();
        this.mHandler = new Handler(thread.getLooper());
        this.mKeyCodeFlagMap.put(26, 1);
        this.mKeyCodeFlagMap.put(24, 2);
        this.mKeyCodeFlagMap.put(25, 4);
        this.mKeyCodeFlagMap.put(82, 8);
        this.mKeyCodeFlagMap.put(3, 16);
        this.mKeyCodeFlagMap.put(4, 32);
        this.mKeyCodeFlagMap.put(134, 64);
        this.mKeyCodeFlagMap.put(27, Integer.valueOf((int) ColorHansRestriction.HANS_RESTRICTION_BLOCK_BINDER));
        this.mKeyCodeFlagMap.put(79, Integer.valueOf((int) OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE));
        this.mKeyCodeFlagMap.put(164, 2048);
        this.mKeyCodeFlagMap.put(187, 4096);
        this.mKeyCodeFlagMap.put(224, 8192);
        this.mKeyCodeFlagMap.put(221, 16384);
        this.mKeyCodeFlagMap.put(220, 32768);
        this.mKeyCodeFlagMap.put(6, 65536);
        this.mKeyCodeFlagMap.put(223, 131072);
    }

    public boolean registerKeyEventObserver(String observerFingerPrint, IColorKeyEventObserver observer, int listenFlag) {
        Slog.i(TAG, "registerKeyEventObserver, observerFingerPrint: " + observerFingerPrint + ", listenFlag: " + listenFlag);
        synchronized (this.mKeyEventObservers) {
            OnKeyEventObserver onKeyEventObserver = new OnKeyEventObserver(observerFingerPrint, observer, listenFlag);
            if (this.mKeyEventObservers.containsKey(observerFingerPrint)) {
                Slog.e(TAG, "registerKeyEventObserver failed, observerFingerPrint: " + observerFingerPrint);
                return DEBUG;
            }
            this.mKeyEventObservers.put(observerFingerPrint, onKeyEventObserver);
            this.mHasObserver = true;
            return true;
        }
    }

    public boolean unregisterKeyEventObserver(String observerFingerPrint, boolean isBinderDied) {
        Slog.i(TAG, "unregisterKeyEventObserver, observerFingerPrint: " + observerFingerPrint);
        synchronized (this.mKeyEventObservers) {
            OnKeyEventObserver observer = this.mKeyEventObservers.remove(observerFingerPrint);
            if (observer == null) {
                Slog.e(TAG, "unregisterKeyEventObserver failed, observerFingerPrint: " + observerFingerPrint);
                return DEBUG;
            }
            if (!isBinderDied) {
                observer.unregister();
            }
            if (this.mKeyEventObservers.isEmpty()) {
                this.mHasObserver = DEBUG;
            }
            return true;
        }
    }

    public void onKeyEvent(KeyEvent event) {
        Handler handler;
        if (DEBUG) {
            Slog.i(TAG, "onKeyEvent, event: " + event.toString());
        }
        if (this.mHasObserver && (handler = this.mHandler) != null) {
            handler.post(new Runnable(event) {
                /* class com.android.server.am.$$Lambda$ColorKeyEventUtil$wl3qvgcEyMPxhhra8ah3yMMiIM */
                private final /* synthetic */ KeyEvent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ColorKeyEventUtil.this.lambda$onKeyEvent$0$ColorKeyEventUtil(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onKeyEvent$0$ColorKeyEventUtil(KeyEvent event) {
        synchronized (this.mKeyEventObservers) {
            for (OnKeyEventObserver observer : this.mKeyEventObservers.values()) {
                observer.onKeyEvent(event);
            }
        }
    }

    /* access modifiers changed from: private */
    public class OnKeyEventObserver implements IBinder.DeathRecipient {
        private int mListenFlag = -1;
        private IColorKeyEventObserver mObserver;
        private String mObserverFingerPrint = "";

        public OnKeyEventObserver(String observerFingerPrint, IColorKeyEventObserver observer, int listenFlag) {
            this.mObserverFingerPrint = observerFingerPrint;
            this.mObserver = observer;
            this.mListenFlag = listenFlag;
            linkToDeathRecipient();
        }

        public void onKeyEvent(KeyEvent event) {
            int i;
            if (this.mObserver == null || (i = this.mListenFlag) < 0) {
                Slog.e(ColorKeyEventUtil.TAG, "OnKeyEventObserver onKeyEvent failed");
            } else if (i == 0) {
                onKeyEventInner(event);
            } else {
                int flag = ((Integer) ColorKeyEventUtil.this.mKeyCodeFlagMap.getOrDefault(Integer.valueOf(event.getKeyCode()), -1)).intValue();
                if (flag < 0) {
                    Slog.i(ColorKeyEventUtil.TAG, "should not notify undefined keys in restrict listen mode");
                } else if ((this.mListenFlag & flag) != 0) {
                    onKeyEventInner(event);
                }
            }
        }

        private void onKeyEventInner(KeyEvent event) {
            try {
                this.mObserver.onKeyEvent(event);
            } catch (RemoteException e) {
                Slog.e(ColorKeyEventUtil.TAG, "onKeyEvent failed, err: " + e);
            }
        }

        public void unregister() {
            unlinkToDeathRecipient();
        }

        private void linkToDeathRecipient() {
            try {
                if (this.mObserver != null) {
                    this.mObserver.asBinder().linkToDeath(this, 0);
                }
            } catch (RemoteException e) {
                Slog.e(ColorKeyEventUtil.TAG, "linkToDeathRecipient failed");
            }
        }

        private void unlinkToDeathRecipient() {
            ColorKeyEventUtil.this.mHandler.post(new Runnable() {
                /* class com.android.server.am.$$Lambda$ColorKeyEventUtil$OnKeyEventObserver$QaDzdXZRAtoc71tKmNChmdcNjzA */

                public final void run() {
                    ColorKeyEventUtil.OnKeyEventObserver.this.lambda$unlinkToDeathRecipient$0$ColorKeyEventUtil$OnKeyEventObserver();
                }
            });
        }

        public /* synthetic */ void lambda$unlinkToDeathRecipient$0$ColorKeyEventUtil$OnKeyEventObserver() {
            IColorKeyEventObserver iColorKeyEventObserver = this.mObserver;
            if (iColorKeyEventObserver != null) {
                iColorKeyEventObserver.asBinder().unlinkToDeath(this, 0);
            }
        }

        public void binderDied() {
            if (ColorKeyEventUtil.DEBUG) {
                Slog.i(ColorKeyEventUtil.TAG, "binderDied");
            }
            ColorKeyEventUtil.this.mHandler.post(new Runnable() {
                /* class com.android.server.am.$$Lambda$ColorKeyEventUtil$OnKeyEventObserver$KPqxvJRHAxO5qBmTAsaSvU6Lg */

                public final void run() {
                    ColorKeyEventUtil.OnKeyEventObserver.this.lambda$binderDied$1$ColorKeyEventUtil$OnKeyEventObserver();
                }
            });
        }

        public /* synthetic */ void lambda$binderDied$1$ColorKeyEventUtil$OnKeyEventObserver() {
            ColorKeyEventUtil.this.unregisterKeyEventObserver(this.mObserverFingerPrint, true);
            this.mObserver = null;
        }
    }
}
