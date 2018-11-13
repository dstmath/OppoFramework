package com.android.server.face.power;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import com.android.server.face.utils.TimeUtils;

public class FacePowerManager {
    private static final String TAG = "FaceService.PowerManager";
    private static Object sMutex = new Object();
    private static FacePowerManager sSingleInstance;
    private final Context mContext;
    private final IPowerCallback mIPowerCallback;
    private final LocalService mLocalService = new LocalService(this, null);

    public interface IPowerCallback {
        boolean isFaceAutoUnlockEnabled();

        void onGoToSleep();

        void onGoToSleepFinish();

        void onScreenOnUnBlockedByOther(String str);

        void onWakeUp(String str);

        void onWakeUpFinish();
    }

    private final class LocalService extends FaceInternal {
        /* synthetic */ LocalService(FacePowerManager this$0, LocalService -this1) {
            this();
        }

        private LocalService() {
        }

        public void onWakeUp(String wakupReason) {
            long startTime = SystemClock.uptimeMillis();
            FacePowerManager.this.mIPowerCallback.onWakeUp(wakupReason);
            TimeUtils.calculateTime(FacePowerManager.TAG, "onWakeUp", SystemClock.uptimeMillis() - startTime);
        }

        public void onGoToSleep() {
            FacePowerManager.this.mIPowerCallback.onGoToSleep();
        }

        public void onWakeUpFinish() {
            FacePowerManager.this.mIPowerCallback.onWakeUpFinish();
        }

        public void onGoToSleepFinish() {
            FacePowerManager.this.mIPowerCallback.onGoToSleepFinish();
        }

        public void onScreenOnUnBlockedByOther(String unBlockedReason) {
            FacePowerManager.this.mIPowerCallback.onScreenOnUnBlockedByOther(unBlockedReason);
        }

        public boolean isFaceAutoUnlockEnabled() {
            return FacePowerManager.this.mIPowerCallback.isFaceAutoUnlockEnabled();
        }
    }

    public FacePowerManager(Context context, IPowerCallback powerCallback) {
        this.mContext = context;
        this.mIPowerCallback = powerCallback;
    }

    public static void initFacePms(Context c, IPowerCallback powerCallback) {
        getFacePowerManager(c, powerCallback);
    }

    public static FacePowerManager getFacePowerManager(Context c, IPowerCallback powerCallback) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new FacePowerManager(c, powerCallback);
            }
        }
        return sSingleInstance;
    }

    public static FacePowerManager getFacePowerManager() {
        return sSingleInstance;
    }

    public boolean isScreenOFF() {
        if (((PowerManager) this.mContext.getSystemService("power")).getScreenState() == 0) {
            return true;
        }
        return false;
    }

    public LocalService getFaceLocalService() {
        return this.mLocalService;
    }
}
