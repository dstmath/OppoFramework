package android.media;

import android.app.ActivityThread;
import android.content.Context;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IAppOpsService.Stub;

public abstract class PlayerBase {
    private final IAppOpsService mAppOps;
    private final IAppOpsCallback mAppOpsCallback;
    private final Object mAppOpsLock = new Object();
    protected AudioAttributes mAttributes;
    protected float mAuxEffectSendLevel = TonemapCurve.LEVEL_BLACK;
    private boolean mHasAppOpsPlayAudio = true;
    protected float mLeftVolume = 1.0f;
    protected float mRightVolume = 1.0f;

    abstract int playerSetAuxEffectSendLevel(float f);

    abstract void playerSetVolume(float f, float f2);

    PlayerBase(AudioAttributes attr) {
        if (attr == null) {
            throw new IllegalArgumentException("Illegal null AudioAttributes");
        }
        this.mAttributes = attr;
        this.mAppOps = Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE));
        updateAppOpsPlayAudio_sync();
        this.mAppOpsCallback = new IAppOpsCallback.Stub() {
            public void opChanged(int op, int uid, String packageName) {
                synchronized (PlayerBase.this.mAppOpsLock) {
                    if (op == 28) {
                        PlayerBase.this.updateAppOpsPlayAudio_sync();
                    }
                }
            }
        };
        try {
            this.mAppOps.startWatchingMode(28, ActivityThread.currentPackageName(), this.mAppOpsCallback);
        } catch (RemoteException e) {
            this.mHasAppOpsPlayAudio = false;
        }
    }

    void baseUpdateAudioAttributes(AudioAttributes attr) {
        if (attr == null) {
            throw new IllegalArgumentException("Illegal null AudioAttributes");
        }
        synchronized (this.mAppOpsLock) {
            this.mAttributes = attr;
            updateAppOpsPlayAudio_sync();
        }
    }

    void baseStart() {
        synchronized (this.mAppOpsLock) {
            if (isRestricted_sync()) {
                playerSetVolume(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK);
            }
        }
    }

    void baseSetVolume(float leftVolume, float rightVolume) {
        synchronized (this.mAppOpsLock) {
            this.mLeftVolume = leftVolume;
            this.mRightVolume = rightVolume;
            if (isRestricted_sync()) {
                return;
            }
            playerSetVolume(leftVolume, rightVolume);
        }
    }

    int baseSetAuxEffectSendLevel(float level) {
        synchronized (this.mAppOpsLock) {
            this.mAuxEffectSendLevel = level;
            if (isRestricted_sync()) {
                return 0;
            }
            return playerSetAuxEffectSendLevel(level);
        }
    }

    void baseRelease() {
        try {
            this.mAppOps.stopWatchingMode(this.mAppOpsCallback);
        } catch (RemoteException e) {
        }
    }

    void updateAppOpsPlayAudio_sync() {
        boolean oldHasAppOpsPlayAudio = this.mHasAppOpsPlayAudio;
        try {
            boolean z;
            if (this.mAppOps.checkAudioOperation(28, this.mAttributes.getUsage(), Process.myUid(), ActivityThread.currentPackageName()) == 0) {
                z = true;
            } else {
                z = false;
            }
            this.mHasAppOpsPlayAudio = z;
        } catch (RemoteException e) {
            this.mHasAppOpsPlayAudio = false;
        }
        try {
            if (oldHasAppOpsPlayAudio == this.mHasAppOpsPlayAudio) {
                return;
            }
            if (this.mHasAppOpsPlayAudio) {
                playerSetVolume(this.mLeftVolume, this.mRightVolume);
                playerSetAuxEffectSendLevel(this.mAuxEffectSendLevel);
                return;
            }
            playerSetVolume(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK);
            playerSetAuxEffectSendLevel(TonemapCurve.LEVEL_BLACK);
        } catch (Exception e2) {
        }
    }

    boolean isRestricted_sync() {
        if (!this.mHasAppOpsPlayAudio && (this.mAttributes.getAllFlags() & 64) == 0) {
            return true;
        }
        return false;
    }
}
