package com.android.server.biometrics;

import android.content.ComponentName;
import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.os.IBinder;
import android.os.RemoteException;
import android.security.KeyStore;
import android.util.Slog;
import com.android.server.biometrics.BiometricServiceBase;
import java.util.ArrayList;

public abstract class AuthenticationClient extends ClientMonitor {
    public static final int LOCKOUT_NONE = 0;
    public static final int LOCKOUT_PERMANENT = 2;
    public static final int LOCKOUT_TIMED = 1;
    private final String mKeyguardPackage;
    private long mOpId;
    private final boolean mRequireConfirmation;
    protected boolean mStarted;

    public abstract int handleFailedAttempt();

    public abstract void onStart();

    public abstract void onStop();

    public abstract boolean shouldFrameworkHandleLockout();

    public abstract boolean wasUserDetected();

    public void resetFailedAttempts() {
    }

    public AuthenticationClient(Context context, Constants constants, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, long opId, boolean restricted, String owner, int cookie, boolean requireConfirmation) {
        super(context, constants, daemon, halDeviceId, token, listener, targetUserId, groupId, restricted, owner, cookie);
        this.mOpId = opId;
        this.mRequireConfirmation = requireConfirmation;
        this.mKeyguardPackage = ComponentName.unflattenFromString(context.getResources().getString(17039747)).getPackageName();
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public void binderDied() {
        super.binderDied();
        stop(false);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.LoggableMonitor
    public int statsAction() {
        return 2;
    }

    public boolean isBiometricPrompt() {
        return getCookie() != 0;
    }

    public boolean getRequireConfirmation() {
        return this.mRequireConfirmation;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.LoggableMonitor
    public boolean isCryptoOperation() {
        return this.mOpId != 0;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onError(long deviceId, int error, int vendorCode) {
        if (!shouldFrameworkHandleLockout() && (error == 3 ? wasUserDetected() || isBiometricPrompt() : error == 7 || error == 9) && this.mStarted && !isKeyguard(getOwnerString())) {
            vibrateError();
        }
        return super.onError(deviceId, error, vendorCode);
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean authenticated, ArrayList<Byte> token) {
        int errorCode;
        super.logOnAuthenticated(getContext(), authenticated, this.mRequireConfirmation, getTargetUserId(), isBiometricPrompt());
        BiometricServiceBase.ServiceListener listener = getListener();
        this.mMetricsLogger.action(this.mConstants.actionBiometricAuth(), authenticated);
        try {
            Slog.v(getLogTag(), "onAuthenticated(" + authenticated + "), ID:" + identifier.getBiometricId() + ", Owner: " + getOwnerString() + ", isBP: " + isBiometricPrompt() + ", listener: " + listener + ", requireConfirmation: " + this.mRequireConfirmation + ", user: " + getTargetUserId());
            boolean result = true;
            if (authenticated) {
                this.mAlreadyDone = true;
                boolean result2 = true;
                if (shouldFrameworkHandleLockout()) {
                    resetFailedAttempts();
                }
                onStop();
                updateOpticalFingerIcon(0);
                byte[] byteToken = new byte[token.size()];
                for (int i = 0; i < token.size(); i++) {
                    byteToken[i] = token.get(i).byteValue();
                }
                if (isBiometricPrompt() && listener != null) {
                    listener.onAuthenticationSucceededInternal(this.mRequireConfirmation, byteToken);
                } else if (isBiometricPrompt() || listener == null) {
                    Slog.w(getLogTag(), "Client not listening");
                    result2 = true;
                } else {
                    KeyStore.getInstance().addAuthToken(byteToken);
                    try {
                        if (!getIsRestricted()) {
                            listener.onAuthenticationSucceeded(getHalDeviceId(), identifier, getTargetUserId());
                        } else {
                            listener.onAuthenticationSucceeded(getHalDeviceId(), null, getTargetUserId());
                        }
                    } catch (RemoteException e) {
                        Slog.e(getLogTag(), "Remote exception", e);
                    }
                }
                if (listener == null || isKeyguard(getOwnerString())) {
                    return result2;
                }
                vibrateSuccess();
                return result2;
            }
            if (listener != null && !isKeyguard(getOwnerString())) {
                vibrateError();
            }
            updateOpticalFingerIcon(5);
            updateLcdHightLight(0);
            int lockoutMode = handleFailedAttempt();
            if (lockoutMode != 0 && shouldFrameworkHandleLockout()) {
                Slog.w(getLogTag(), "Forcing lockout (driver code should do this!), mode(" + lockoutMode + ")");
                stop(false);
                if (lockoutMode == 1) {
                    errorCode = 7;
                } else {
                    errorCode = 9;
                }
                onError(getHalDeviceId(), errorCode, 0);
            } else if (listener != null) {
                if (isBiometricPrompt()) {
                    listener.onAuthenticationFailedInternal(getCookie(), getRequireConfirmation());
                } else {
                    listener.onAuthenticationFailed(getHalDeviceId());
                }
            }
            if (lockoutMode == 0) {
                result = false;
            }
            return result;
        } catch (RemoteException e2) {
            Slog.e(getLogTag(), "Remote exception", e2);
            return true;
        }
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int start() {
        this.mStarted = true;
        onStart();
        int result = 1;
        try {
            if (getDaemonWrapper() != null) {
                result = getDaemonWrapper().authenticate(this.mOpId, getGroupId());
            }
            if (result != 0) {
                String logTag = getLogTag();
                Slog.w(logTag, "startAuthentication failed, result=" + result);
                this.mMetricsLogger.histogram(this.mConstants.tagAuthStartError(), result);
                onError(getHalDeviceId(), 1, 0);
                return result;
            }
            updateOpticalFingerIcon(1);
            String logTag2 = getLogTag();
            Slog.w(logTag2, "client " + getOwnerString() + " is authenticating...");
            return 0;
        } catch (RemoteException e) {
            Slog.e(getLogTag(), "startAuthentication failed", e);
            return 3;
        }
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int stop(boolean initiatedByClient) {
        updateOpticalFingerIcon(0);
        if (this.mAlreadyCancelled) {
            Slog.w(getLogTag(), "stopAuthentication: already cancelled!");
            return 0;
        }
        this.mStarted = false;
        onStop();
        int result = 1;
        try {
            if (getDaemonWrapper() != null) {
                result = getDaemonWrapper().cancel();
            }
            if (result != 0) {
                String logTag = getLogTag();
                Slog.w(logTag, "stopAuthentication failed, result=" + result);
                return result;
            }
            String logTag2 = getLogTag();
            Slog.w(logTag2, "client " + getOwnerString() + " is no longer authenticating");
            this.mAlreadyCancelled = true;
            return 0;
        } catch (RemoteException e) {
            Slog.e(getLogTag(), "stopAuthentication failed", e);
            return 3;
        }
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnrollResult(BiometricAuthenticator.Identifier identifier, int remaining) {
        Slog.w(getLogTag(), "onEnrollResult() called for authenticate!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) {
        Slog.w(getLogTag(), "onRemoved() called for authenticate!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnumerationResult(BiometricAuthenticator.Identifier identifier, int remaining) {
        Slog.w(getLogTag(), "onEnumerationResult() called for authenticate!");
        return true;
    }

    private boolean isKeyguard(String clientPackage) {
        String str = this.mKeyguardPackage;
        if (str != null) {
            return str.equals(clientPackage);
        }
        return false;
    }
}
