package android.media;

import android.media.IAudioService;
import android.media.IPlayer;
import android.media.VolumeShaper;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class PlayerBase {
    private static final boolean DEBUG;
    private static final boolean DEBUG_APP_OPS = (!"user".equals(Build.TYPE));
    private static final String TAG = "PlayerBase";
    private static final boolean USE_AUDIOFLINGER_MUTING_FOR_OP = true;
    private static IAudioService sService;
    private IAppOpsService mAppOps;
    private IAppOpsCallback mAppOpsCallback;
    protected AudioAttributes mAttributes;
    protected float mAuxEffectSendLevel = 0.0f;
    @GuardedBy({"mLock"})
    private boolean mHasAppOpsPlayAudio = true;
    private final int mImplType;
    protected float mLeftVolume = 1.0f;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private float mPanMultiplierL = 1.0f;
    @GuardedBy({"mLock"})
    private float mPanMultiplierR = 1.0f;
    private int mPlayerIId = -1;
    protected float mRightVolume = 1.0f;
    @GuardedBy({"mLock"})
    private int mStartDelayMs = 0;
    @GuardedBy({"mLock"})
    private int mState;
    @GuardedBy({"mLock"})
    private float mVolMultiplier = 1.0f;

    /* access modifiers changed from: package-private */
    public abstract int playerApplyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation);

    /* access modifiers changed from: package-private */
    public abstract VolumeShaper.State playerGetVolumeShaperState(int i);

    /* access modifiers changed from: package-private */
    public abstract void playerPause();

    /* access modifiers changed from: package-private */
    public abstract int playerSetAuxEffectSendLevel(boolean z, float f);

    /* access modifiers changed from: package-private */
    public abstract void playerSetVolume(boolean z, float f, float f2);

    /* access modifiers changed from: package-private */
    public abstract void playerStart();

    /* access modifiers changed from: package-private */
    public abstract void playerStop();

    static {
        boolean z = true;
        if (!DEBUG_APP_OPS) {
            z = false;
        }
        DEBUG = z;
    }

    PlayerBase(AudioAttributes attr, int implType) {
        if (attr != null) {
            this.mAttributes = attr;
            this.mImplType = implType;
            this.mState = 1;
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioAttributes");
    }

    /* access modifiers changed from: protected */
    public void baseRegisterPlayer() {
        try {
            this.mPlayerIId = getService().trackPlayer(new PlayerIdCard(this.mImplType, this.mAttributes, new IPlayerWrapper(this)));
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to audio service, player will not be tracked", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void baseUpdateAudioAttributes(AudioAttributes attr) {
        if (attr != null) {
            try {
                getService().playerAttributes(this.mPlayerIId, attr);
            } catch (RemoteException e) {
                Log.e(TAG, "Error talking to audio service, STARTED state will not be tracked", e);
            }
            synchronized (this.mLock) {
                boolean attributesChanged = this.mAttributes != attr;
                this.mAttributes = attr;
                updateAppOpsPlayAudio_sync(attributesChanged);
            }
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioAttributes");
    }

    private void updateState(int state) {
        int piid;
        synchronized (this.mLock) {
            this.mState = state;
            piid = this.mPlayerIId;
        }
        try {
            getService().playerEvent(piid, state);
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to audio service, " + AudioPlaybackConfiguration.toLogFriendlyPlayerState(state) + " state will not be tracked for piid=" + piid, e);
        }
    }

    /* access modifiers changed from: package-private */
    public void baseStart() {
        if (DEBUG) {
            Log.v(TAG, "baseStart() piid=" + this.mPlayerIId);
        }
        updateState(2);
        synchronized (this.mLock) {
            if (isRestricted_sync()) {
                playerSetVolume(true, 0.0f, 0.0f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void baseSetStartDelayMs(int delayMs) {
        synchronized (this.mLock) {
            this.mStartDelayMs = Math.max(delayMs, 0);
        }
    }

    /* access modifiers changed from: protected */
    public int getStartDelayMs() {
        int i;
        synchronized (this.mLock) {
            i = this.mStartDelayMs;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void basePause() {
        if (DEBUG) {
            Log.v(TAG, "basePause() piid=" + this.mPlayerIId);
        }
        updateState(3);
    }

    /* access modifiers changed from: package-private */
    public void baseStop() {
        if (DEBUG) {
            Log.v(TAG, "baseStop() piid=" + this.mPlayerIId);
        }
        updateState(4);
    }

    /* access modifiers changed from: package-private */
    public void baseSetPan(float pan) {
        float p = Math.min(Math.max(-1.0f, pan), 1.0f);
        synchronized (this.mLock) {
            if (p >= 0.0f) {
                this.mPanMultiplierL = 1.0f - p;
                this.mPanMultiplierR = 1.0f;
            } else {
                this.mPanMultiplierL = 1.0f;
                this.mPanMultiplierR = 1.0f + p;
            }
        }
        updatePlayerVolume();
    }

    private void updatePlayerVolume() {
        float finalLeftVol;
        float finalRightVol;
        boolean isRestricted;
        synchronized (this.mLock) {
            finalLeftVol = this.mVolMultiplier * this.mLeftVolume * this.mPanMultiplierL;
            finalRightVol = this.mVolMultiplier * this.mRightVolume * this.mPanMultiplierR;
            isRestricted = isRestricted_sync();
        }
        playerSetVolume(isRestricted, finalLeftVol, finalRightVol);
    }

    /* access modifiers changed from: package-private */
    public void setVolumeMultiplier(float vol) {
        synchronized (this.mLock) {
            this.mVolMultiplier = vol;
        }
        updatePlayerVolume();
    }

    /* access modifiers changed from: package-private */
    public void baseSetVolume(float leftVolume, float rightVolume) {
        synchronized (this.mLock) {
            this.mLeftVolume = leftVolume;
            this.mRightVolume = rightVolume;
        }
        updatePlayerVolume();
    }

    /* access modifiers changed from: package-private */
    public int baseSetAuxEffectSendLevel(float level) {
        synchronized (this.mLock) {
            this.mAuxEffectSendLevel = level;
            if (isRestricted_sync()) {
                return 0;
            }
            return playerSetAuxEffectSendLevel(false, level);
        }
    }

    /* access modifiers changed from: package-private */
    public void baseRelease() {
        if (DEBUG) {
            Log.v(TAG, "baseRelease() piid=" + this.mPlayerIId + " state=" + this.mState);
        }
        boolean releasePlayer = false;
        synchronized (this.mLock) {
            if (this.mState != 0) {
                releasePlayer = true;
                this.mState = 0;
            }
        }
        if (releasePlayer) {
            try {
                getService().releasePlayer(this.mPlayerIId);
            } catch (RemoteException e) {
                Log.e(TAG, "Error talking to audio service, the player will still be tracked", e);
            }
        }
        try {
            if (this.mAppOps != null) {
                this.mAppOps.stopWatchingMode(this.mAppOpsCallback);
            }
        } catch (Exception e2) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAppOpsPlayAudio() {
        synchronized (this.mLock) {
            updateAppOpsPlayAudio_sync(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateAppOpsPlayAudio_sync(boolean attributesChanged) {
    }

    /* access modifiers changed from: package-private */
    public boolean isRestricted_sync() {
        return false;
    }

    private static IAudioService getService() {
        IAudioService iAudioService = sService;
        if (iAudioService != null) {
            return iAudioService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return sService;
    }

    public void setStartDelayMs(int delayMs) {
        baseSetStartDelayMs(delayMs);
    }

    private static class IAppOpsCallbackWrapper extends IAppOpsCallback.Stub {
        private final WeakReference<PlayerBase> mWeakPB;

        public IAppOpsCallbackWrapper(PlayerBase pb) {
            this.mWeakPB = new WeakReference<>(pb);
        }

        @Override // com.android.internal.app.IAppOpsCallback
        public void opChanged(int op, int uid, String packageName) {
            if (op == 28) {
                if (PlayerBase.DEBUG_APP_OPS) {
                    Log.v(PlayerBase.TAG, "opChanged: op=PLAY_AUDIO pack=" + packageName);
                }
                PlayerBase pb = this.mWeakPB.get();
                if (pb != null) {
                    pb.updateAppOpsPlayAudio();
                }
            }
        }
    }

    private static class IPlayerWrapper extends IPlayer.Stub {
        private final WeakReference<PlayerBase> mWeakPB;

        public IPlayerWrapper(PlayerBase pb) {
            this.mWeakPB = new WeakReference<>(pb);
        }

        @Override // android.media.IPlayer
        public void start() {
            PlayerBase pb = this.mWeakPB.get();
            if (pb != null) {
                pb.playerStart();
            }
        }

        @Override // android.media.IPlayer
        public void pause() {
            PlayerBase pb = this.mWeakPB.get();
            if (pb != null) {
                pb.playerPause();
            }
        }

        @Override // android.media.IPlayer
        public void stop() {
            PlayerBase pb = this.mWeakPB.get();
            if (pb != null) {
                pb.playerStop();
            }
        }

        @Override // android.media.IPlayer
        public void setVolume(float vol) {
            PlayerBase pb = this.mWeakPB.get();
            if (pb != null) {
                pb.setVolumeMultiplier(vol);
            }
        }

        @Override // android.media.IPlayer
        public void setPan(float pan) {
            PlayerBase pb = this.mWeakPB.get();
            if (pb != null) {
                pb.baseSetPan(pan);
            }
        }

        @Override // android.media.IPlayer
        public void setStartDelayMs(int delayMs) {
            PlayerBase pb = this.mWeakPB.get();
            if (pb != null) {
                pb.baseSetStartDelayMs(delayMs);
            }
        }

        @Override // android.media.IPlayer
        public void applyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation) {
            PlayerBase pb = this.mWeakPB.get();
            if (pb != null) {
                pb.playerApplyVolumeShaper(configuration, operation);
            }
        }
    }

    public static class PlayerIdCard implements Parcelable {
        public static final int AUDIO_ATTRIBUTES_DEFINED = 1;
        public static final int AUDIO_ATTRIBUTES_NONE = 0;
        public static final Parcelable.Creator<PlayerIdCard> CREATOR = new Parcelable.Creator<PlayerIdCard>() {
            /* class android.media.PlayerBase.PlayerIdCard.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public PlayerIdCard createFromParcel(Parcel p) {
                return new PlayerIdCard(p);
            }

            @Override // android.os.Parcelable.Creator
            public PlayerIdCard[] newArray(int size) {
                return new PlayerIdCard[size];
            }
        };
        public final AudioAttributes mAttributes;
        public final IPlayer mIPlayer;
        public final int mPlayerType;

        PlayerIdCard(int type, AudioAttributes attr, IPlayer iplayer) {
            this.mPlayerType = type;
            this.mAttributes = attr;
            this.mIPlayer = iplayer;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.mPlayerType));
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mPlayerType);
            this.mAttributes.writeToParcel(dest, 0);
            IPlayer iPlayer = this.mIPlayer;
            dest.writeStrongBinder(iPlayer == null ? null : iPlayer.asBinder());
        }

        private PlayerIdCard(Parcel in) {
            this.mPlayerType = in.readInt();
            this.mAttributes = AudioAttributes.CREATOR.createFromParcel(in);
            IBinder b = in.readStrongBinder();
            this.mIPlayer = b == null ? null : IPlayer.Stub.asInterface(b);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof PlayerIdCard)) {
                return false;
            }
            PlayerIdCard that = (PlayerIdCard) o;
            if (this.mPlayerType != that.mPlayerType || !this.mAttributes.equals(that.mAttributes)) {
                return false;
            }
            return true;
        }
    }

    public static void deprecateStreamTypeForPlayback(int streamType, String className, String opName) throws IllegalArgumentException {
        if (streamType != 10) {
            Log.w(className, "Use of stream types is deprecated for operations other than volume control");
            Log.w(className, "See the documentation of " + opName + " for what to use instead with android.media.AudioAttributes to qualify your playback use case");
            return;
        }
        throw new IllegalArgumentException("Use of STREAM_ACCESSIBILITY is reserved for volume control");
    }
}
