package com.android.server.power;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.color.util.ColorTypeCastingHelper;

public class PswFeatureAOD implements IPswFeatureAOD {
    private static final String TAG = "PswFeatureAOD";
    private static Context mContext = null;
    private static PowerManagerService mPMS = null;
    private static OppoBasePowerManagerService mPmsBase;
    private static PswFeatureAOD sInstance = null;
    public boolean mFingerprintOpticalSupport = false;
    private IPswPowerManagerServiceAODInner mInner;
    public boolean mmOppoAodSupport = false;

    public static PswFeatureAOD getInstance(Object... vars) {
        PswFeatureAOD pswFeatureAOD;
        Log.d(TAG, "PowerManagerService getInstance");
        mContext = (Context) vars[0];
        mPMS = (PowerManagerService) vars[1];
        mPmsBase = (OppoBasePowerManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePowerManagerService.class, mPMS);
        synchronized (PswFeatureAOD.class) {
            if (sInstance == null) {
                sInstance = new PswFeatureAOD();
            }
            pswFeatureAOD = sInstance;
        }
        return pswFeatureAOD;
    }

    public PswFeatureAOD() {
        Log.i(TAG, "create");
    }

    public void testfunc() {
        Log.i(TAG, "testFunc");
    }

    public void init(IPswPowerManagerServiceAODInner inner) {
        Log.i(TAG, "init");
        this.mInner = inner;
        if (mContext.getPackageManager().hasSystemFeature("oppo.aod.support")) {
            OppoBasePowerManagerService oppoBasePowerManagerService = mPmsBase;
            OppoBasePowerManagerService.mOppoAodSupport = true;
            this.mInner.setDozeAfterScreenOff(true);
            this.mInner.setDecoupleHalAutoSuspendModeFromDisplayConfig(true);
            this.mInner.setDecoupleHalInteractiveModeFromDisplayConfig(true);
            StringBuilder sb = new StringBuilder();
            sb.append("mOppoAodSupport = ");
            OppoBasePowerManagerService oppoBasePowerManagerService2 = mPmsBase;
            sb.append(OppoBasePowerManagerService.mOppoAodSupport);
            Log.d(TAG, sb.toString());
        }
        if (mContext.getPackageManager().hasSystemFeature("oppo.hardware.fingerprint.optical.support")) {
            OppoBasePowerManagerService oppoBasePowerManagerService3 = mPmsBase;
            oppoBasePowerManagerService3.mFingerprintOpticalSupport = true;
            this.mFingerprintOpticalSupport = oppoBasePowerManagerService3.mFingerprintOpticalSupport;
            Log.d(TAG, "mFingerprintOpticalSupport = " + this.mFingerprintOpticalSupport);
        }
    }

    public void setDozeOverride(int screenState, int screenBrightness) {
        Slog.d(TAG, "setDozeOverride screenState:" + screenState + " screenBrightness:" + screenBrightness + " mScreenState:" + mPmsBase.mScreenState);
        if (mPmsBase.mScreenState != 2) {
            int value = 0;
            if (screenState == 1) {
                value = 0;
            } else if (screenState == 2) {
                value = 3;
            } else if (screenState == 3) {
                value = 2;
            } else if (screenState == 4) {
                value = 1;
            }
            int pid = Binder.getCallingPid();
            synchronized (mPmsBase.mMapLock) {
                mPmsBase.mDozeStateMap.put(Integer.valueOf(pid), Integer.valueOf(value));
                if (mPmsBase.mDozeStateMap.size() > 2) {
                    Slog.d(TAG, "systemUI have been killed, clear map");
                    mPmsBase.mDozeStateMap.clear();
                    mPmsBase.mDozeStateMap.put(Integer.valueOf(pid), Integer.valueOf(value));
                }
            }
        }
    }

    public void setDozeOverrideFromDreamManager(int screenState, int screenBrightness) {
        int value = 0;
        if (screenState == 1) {
            value = 0;
        } else if (screenState == 2) {
            value = 3;
        } else if (screenState == 3) {
            value = 2;
        } else if (screenState == 4) {
            value = 1;
        }
        int pid = Binder.getCallingPid();
        synchronized (mPmsBase.mMapLock) {
            mPmsBase.mDozeStateMap.put(Integer.valueOf(pid), Integer.valueOf(value));
        }
    }

    public int setDozeOverrideFromDreamManagerInternal(int screenState, int screenBrightness) {
        int value = 0;
        if (screenState == 1) {
            value = 0;
        } else if (screenState == 2) {
            value = 3;
        } else if (screenState == 3) {
            value = 2;
        } else if (screenState == 4) {
            value = 1;
        }
        synchronized (mPmsBase.mMapLock) {
            for (Integer num : mPmsBase.mDozeStateMap.keySet()) {
                int key = num.intValue();
                int tmp = ((Integer) mPmsBase.mDozeStateMap.get(Integer.valueOf(key))).intValue();
                Slog.d(TAG, "key:" + key + " tmp:" + tmp);
                if (tmp > value) {
                    value = tmp;
                }
            }
        }
        if (value == 0) {
            return 1;
        }
        if (value == 1) {
            return 4;
        }
        if (value == 2) {
            return 3;
        }
        if (value != 3) {
            return screenState;
        }
        return 2;
    }

    public void systemReady(ContentResolver resolver) {
        this.mInner.systemReady(resolver);
        mPmsBase.mFlinger = ServiceManager.getService("SurfaceFlinger");
        if (mPmsBase.mFlinger != null) {
            Slog.d(TAG, "get SurfaceFlinger Service sucess");
        } else {
            Slog.d(TAG, "get SurfaceFlinger Service failed");
        }
    }

    public boolean isShouldGoAod() {
        if (this.mFingerprintOpticalSupport) {
            if (mPmsBase.mAodUserSetEnable == 1 || (mPmsBase.mFingerprintUnlockswitch == 1 && mPmsBase.mFingerprintUnlock == 1)) {
                return true;
            }
            return false;
        } else if (mPmsBase.mAodUserSetEnable == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void getAodSettingStatus() {
        if (isShouldGoAod()) {
            this.mInner.setDreamsEnabledSetting(true);
            this.mInner.setDecoupleHalAutoSuspendModeFromDisplayConfig(true);
            this.mInner.setDreamsActivateOnSleepSetting(true);
            return;
        }
        this.mInner.setDreamsEnabledSetting(false);
        this.mInner.setDecoupleHalAutoSuspendModeFromDisplayConfig(false);
        this.mInner.setDreamsActivateOnSleepSetting(false);
    }

    public void handleAodChanged() {
        ContentResolver resolver = mContext.getContentResolver();
        mPmsBase.mAodUserSetEnable = Settings.Secure.getIntForUser(resolver, "Setting_AodEnable", 0, -2);
        mPmsBase.mFingerprintUnlock = Settings.Secure.getIntForUser(resolver, "show_fingerprint_when_screen_off", 0, -2);
        mPmsBase.mFingerprintUnlockswitch = Settings.Secure.getIntForUser(resolver, "coloros_fingerprint_unlock_switch", 0, -2);
    }

    public void onDisplayStateChange(int state) {
        this.mInner.onDisplayStateChange(state);
    }

    public void notifySfUnBlockScreenOn() {
        Slog.d(TAG, "notifySfUnBlockScreenOn");
        if (mPmsBase.mFlinger == null) {
            Slog.d(TAG, "notifySfUnBlockScreenOn, mFlinger is null ");
            mPmsBase.mFlinger = ServiceManager.getService("SurfaceFlinger");
        }
        try {
            if (mPmsBase.mFlinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                mPmsBase.mFlinger.transact(20003, data, null, 1);
                data.recycle();
            }
        } catch (RemoteException e) {
            Slog.d(TAG, "get SurfaceFlinger Service failed");
        }
    }
}
