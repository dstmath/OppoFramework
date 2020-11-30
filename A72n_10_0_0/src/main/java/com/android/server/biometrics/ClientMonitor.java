package com.android.server.biometrics;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.media.AudioAttributes;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public abstract class ClientMonitor extends LoggableMonitor implements IBinder.DeathRecipient {
    protected static final boolean DEBUG = true;
    protected static final int ERROR_ESRCH = 3;
    private static final AudioAttributes FINGERPRINT_SONFICATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    protected boolean mAlreadyCancelled;
    protected boolean mAlreadyDone;
    protected final Constants mConstants;
    private final Context mContext;
    private final int mCookie;
    private final BiometricServiceBase.DaemonWrapper mDaemon;
    private final VibrationEffect mErrorVibrationEffect = VibrationEffect.get(1);
    private final int mGroupId;
    private final long mHalDeviceId;
    private final boolean mIsRestricted;
    private BiometricServiceBase.ServiceListener mListener;
    protected final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final String mOwner;
    private final VibrationEffect mSuccessVibrationEffect = VibrationEffect.get(0);
    private final int mTargetUserId;
    private IBinder mToken;

    public abstract void notifyUserActivity();

    public abstract boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean z, ArrayList<Byte> arrayList);

    public abstract boolean onEnrollResult(BiometricAuthenticator.Identifier identifier, int i);

    public abstract boolean onEnumerationResult(BiometricAuthenticator.Identifier identifier, int i);

    public abstract boolean onRemoved(BiometricAuthenticator.Identifier identifier, int i);

    public abstract int start();

    public abstract int stop(boolean z);

    public ClientMonitor(Context context, Constants constants, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int userId, int groupId, boolean restricted, String owner, int cookie) {
        this.mContext = context;
        this.mConstants = constants;
        this.mDaemon = daemon;
        this.mHalDeviceId = halDeviceId;
        this.mToken = token;
        this.mListener = listener;
        this.mTargetUserId = userId;
        this.mGroupId = groupId;
        this.mIsRestricted = restricted;
        this.mOwner = owner;
        this.mCookie = cookie;
        if (token != null) {
            try {
                token.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Slog.w(getLogTag(), "caught remote exception in linkToDeath: ", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getLogTag() {
        return this.mConstants.logTag();
    }

    public int getCookie() {
        return this.mCookie;
    }

    public int[] getAcquireIgnorelist() {
        return new int[0];
    }

    public int[] getAcquireVendorIgnorelist() {
        return new int[0];
    }

    private boolean blacklistContains(int acquiredInfo, int vendorCode) {
        if (acquiredInfo == this.mConstants.acquireVendorCode()) {
            for (int i = 0; i < getAcquireVendorIgnorelist().length; i++) {
                if (getAcquireVendorIgnorelist()[i] == vendorCode) {
                    String logTag = getLogTag();
                    Slog.v(logTag, "Ignoring vendor message: " + vendorCode);
                    return true;
                }
            }
            return false;
        }
        for (int i2 = 0; i2 < getAcquireIgnorelist().length; i2++) {
            if (getAcquireIgnorelist()[i2] == acquiredInfo) {
                String logTag2 = getLogTag();
                Slog.v(logTag2, "Ignoring message: " + acquiredInfo);
                return true;
            }
        }
        return false;
    }

    public boolean isAlreadyDone() {
        return this.mAlreadyDone;
    }

    public boolean onAcquired(int acquiredInfo, int vendorCode) {
        super.logOnAcquired(this.mContext, acquiredInfo, vendorCode, getTargetUserId());
        String logTag = getLogTag();
        Slog.v(logTag, "Acquired: " + acquiredInfo + StringUtils.SPACE + vendorCode);
        try {
            if (this.mListener != null && !blacklistContains(acquiredInfo, vendorCode)) {
                this.mListener.onAcquired(getHalDeviceId(), acquiredInfo, vendorCode);
            }
            if (acquiredInfo == 0) {
                notifyUserActivity();
            }
            return false;
        } catch (RemoteException e) {
            Slog.w(getLogTag(), "Failed to invoke sendAcquired", e);
            if (acquiredInfo == 0) {
                notifyUserActivity();
            }
            return true;
        } catch (NullPointerException e2) {
            Slog.w(getLogTag(), "onAcquired NullPointerException has occurred!", e2);
            if (acquiredInfo == 0) {
                notifyUserActivity();
            }
            return true;
        } catch (Throwable th) {
            if (acquiredInfo == 0) {
                notifyUserActivity();
            }
            throw th;
        }
    }

    public boolean onError(long deviceId, int error, int vendorCode) {
        super.logOnError(this.mContext, error, vendorCode, getTargetUserId());
        try {
            if (this.mListener == null) {
                return true;
            }
            this.mListener.onError(deviceId, error, vendorCode, getCookie());
            return true;
        } catch (RemoteException e) {
            Slog.w(getLogTag(), "Failed to invoke sendError", e);
            return true;
        } catch (NullPointerException e2) {
            Slog.w(getLogTag(), "onError NullPointerException has occurred!", e2);
            return true;
        }
    }

    public void destroy() {
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            try {
                iBinder.unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
                String logTag = getLogTag();
                Slog.e(logTag, "destroy(): " + this + ":", new Exception("here"));
            }
            this.mToken = null;
        }
        this.mListener = null;
    }

    public void binderDied() {
        Slog.e(getLogTag(), "Binder died, cancelling client");
        if (getContext().getPackageManager().hasSystemFeature(FingerprintService.SIDE_FINGERPRINT_FEATURE)) {
            FingerprintService.mPressTouchApp = false;
            FingerprintService.mPressTouchEnrolling = false;
        }
        stop(false);
        this.mToken = null;
        this.mListener = null;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mToken != null) {
                String logTag = getLogTag();
                Slog.w(logTag, "removing leaked reference: " + this.mToken);
                onError(getHalDeviceId(), 1, 0);
            }
        } finally {
            super.finalize();
        }
    }

    public final Context getContext() {
        return this.mContext;
    }

    public final long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    public final String getOwnerString() {
        return this.mOwner;
    }

    public final BiometricServiceBase.ServiceListener getListener() {
        return this.mListener;
    }

    public final BiometricServiceBase.DaemonWrapper getDaemonWrapper() {
        return this.mDaemon;
    }

    public final boolean getIsRestricted() {
        return this.mIsRestricted;
    }

    public final int getTargetUserId() {
        return this.mTargetUserId;
    }

    public final int getGroupId() {
        return this.mGroupId;
    }

    public final IBinder getToken() {
        return this.mToken;
    }

    public final void vibrateSuccess() {
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(this.mSuccessVibrationEffect, FINGERPRINT_SONFICATION_ATTRIBUTES);
        }
    }

    public final void vibrateError() {
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(this.mErrorVibrationEffect, FINGERPRINT_SONFICATION_ATTRIBUTES);
        }
    }

    public void updateOpticalFingerIcon(int status) {
    }

    public void updateLcdHightLight(int values) {
    }
}
