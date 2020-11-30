package com.android.server.biometrics.face.power;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import com.android.server.biometrics.face.utils.TimeUtils;

public class FacePowerManager {
    private static final String TAG = "FaceService.PowerManager";
    private static Object sMutex = new Object();
    private static FacePowerManager sSingleInstance;
    private final Context mContext;
    private final IPowerCallback mIPowerCallback;
    private final LocalService mLocalService = new LocalService();

    public interface IPowerCallback {
        boolean isFaceAutoUnlockEnabled();

        boolean isFaceFingerprintCombineUnlockEnabled();

        boolean isOpticalFingerprintSupport();

        void onGoToSleep();

        void onGoToSleepFinish();

        void onScreenOnUnBlockedByOther(String str);

        void onWakeUp(String str);

        void onWakeUpFinish();
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
        return ((PowerManager) this.mContext.getSystemService("power")).getScreenState() == 0;
    }

    public LocalService getFaceLocalService() {
        return this.mLocalService;
    }

    /* access modifiers changed from: private */
    public final class LocalService extends FaceInternal {
        private LocalService() {
        }

        @Override // com.android.server.biometrics.face.power.FaceInternal
        public void onWakeUp(String wakupReason) {
            long startTime = SystemClock.uptimeMillis();
            FacePowerManager.this.mIPowerCallback.onWakeUp(wakupReason);
            TimeUtils.calculateTime(FacePowerManager.TAG, "onWakeUp", SystemClock.uptimeMillis() - startTime);
        }

        @Override // com.android.server.biometrics.face.power.FaceInternal
        public void onGoToSleep() {
            FacePowerManager.this.mIPowerCallback.onGoToSleep();
        }

        @Override // com.android.server.biometrics.face.power.FaceInternal
        public void onWakeUpFinish() {
            FacePowerManager.this.mIPowerCallback.onWakeUpFinish();
        }

        @Override // com.android.server.biometrics.face.power.FaceInternal
        public void onGoToSleepFinish() {
            FacePowerManager.this.mIPowerCallback.onGoToSleepFinish();
        }

        @Override // com.android.server.biometrics.face.power.FaceInternal
        public void onScreenOnUnBlockedByOther(String unBlockedReason) {
            FacePowerManager.this.mIPowerCallback.onScreenOnUnBlockedByOther(unBlockedReason);
        }

        @Override // com.android.server.biometrics.face.power.FaceInternal
        public boolean isFaceAutoUnlockEnabled() {
            return FacePowerManager.this.mIPowerCallback.isFaceAutoUnlockEnabled();
        }

        @Override // com.android.server.biometrics.face.power.FaceInternal
        public boolean isFaceFingerprintCombineUnlockEnabled() {
            return FacePowerManager.this.mIPowerCallback.isFaceFingerprintCombineUnlockEnabled();
        }

        @Override // com.android.server.biometrics.face.power.FaceInternal
        public boolean isOpticalFingerprintSupport() {
            return FacePowerManager.this.mIPowerCallback.isOpticalFingerprintSupport();
        }
    }
}
