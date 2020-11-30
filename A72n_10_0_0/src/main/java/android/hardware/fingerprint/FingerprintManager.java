package android.hardware.fingerprint;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.BiometricFingerprintConstants;
import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintCommandCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.hardware.fingerprint.IOppoFingerprintManagerEx;
import android.hardware.fingerprint.IOpticalFingerprintListener;
import android.hardware.fingerprint.OppoFingerprintManagerEx;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.R;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import java.security.Signature;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.Mac;

@Deprecated
public class FingerprintManager implements BiometricAuthenticator, BiometricFingerprintConstants, IOppoFingerprintManagerEx {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String KEYGUARD_PACKAGENAME = "com.android.keyguard";
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_ENGINEERING_INFO = 1005;
    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ENUMERATED = 106;
    private static final int MSG_ERROR = 104;
    private static final int MSG_IMAGE_INFO_ACQUIRED = 1004;
    private static final int MSG_MONITOR_EVENT_TRIGGERED = 1003;
    private static final int MSG_REMOVED = 105;
    private static final int MSG_TOUCHDOWN_EVNET = 1001;
    private static final int MSG_TOUCHUP_EVNET = 1002;
    public static final int OPTICAL_FINGERPRINT_HIDE = 0;
    public static final int OPTICAL_FINGERPRINT_SHOW = 1;
    private static final String TAG = "FingerprintManager";
    private AuthenticationCallback mAuthenticationCallback;
    private Context mContext;
    private CryptoObject mCryptoObject;
    private EngineeringInfoCallback mEngineeringInfoCallback;
    private EnrollmentCallback mEnrollmentCallback;
    private EnumerateCallback mEnumerateCallback;
    private FingerprintInputCallback mFingerprintInputCallback;
    private Handler mHandler;
    private MonitorEventCallback mMonitorEventCallback;
    private RemovalCallback mRemovalCallback;
    private Fingerprint mRemovalFingerprint;
    private IFingerprintService mService;
    private IFingerprintServiceReceiver mServiceReceiver = new IFingerprintServiceReceiver.Stub() {
        /* class android.hardware.fingerprint.FingerprintManager.AnonymousClass6 */

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintManager.this.mHandler.obtainMessage(100, remaining, 0, new Fingerprint(null, groupId, fingerId, deviceId)).sendToTarget();
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onAcquired(long deviceId, int acquireInfo, int vendorCode) {
            if (FingerprintManager.DEBUG) {
                Slog.d(FingerprintManager.TAG, "onAcquired");
            }
            FingerprintManager.this.mHandler.obtainMessage(101, acquireInfo, vendorCode, Long.valueOf(deviceId)).sendToTarget();
            if (FingerprintManager.DEBUG) {
                Slog.d(FingerprintManager.TAG, "onAcquired finished");
            }
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) {
            if (FingerprintManager.DEBUG) {
                Slog.d(FingerprintManager.TAG, "onAuthenticationSucceeded");
            }
            if ("com.android.systemui".equals(FingerprintManager.this.mContext.getOpPackageName())) {
                FingerprintManager.this.sendAuthenticatedSucceeded(fp, userId);
            } else {
                FingerprintManager.this.mHandler.obtainMessage(102, userId, 0, fp).sendToTarget();
            }
            if (FingerprintManager.DEBUG) {
                Slog.d(FingerprintManager.TAG, "onAuthenticationSucceeded finished");
            }
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onAuthenticationFailed(long deviceId) {
            if (FingerprintManager.DEBUG) {
                Slog.d(FingerprintManager.TAG, "onAuthenticationFailed");
            }
            FingerprintManager.this.mHandler.obtainMessage(103).sendToTarget();
            if (FingerprintManager.DEBUG) {
                Slog.d(FingerprintManager.TAG, "onAuthenticationFailed finished");
            }
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onError(long deviceId, int error, int vendorCode) {
            FingerprintManager.this.mHandler.obtainMessage(104, error, vendorCode, Long.valueOf(deviceId)).sendToTarget();
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) {
            if (FingerprintManager.DEBUG) {
                Slog.d(FingerprintManager.TAG, "onRemoved");
            }
            FingerprintManager.this.mHandler.obtainMessage(105, remaining, 0, new Fingerprint(null, groupId, fingerId, deviceId)).sendToTarget();
            if (FingerprintManager.DEBUG) {
                Slog.d(FingerprintManager.TAG, "onRemoved finished");
            }
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onEngineeringInfoUpdated(EngineeringInfo info) {
            FingerprintManager.this.mHandler.obtainMessage(1005, 0, 0, info).sendToTarget();
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onTouchDown(long deviceId) {
            FingerprintManager.this.mHandler.obtainMessage(1001, 0, 0, Long.valueOf(deviceId)).sendToTarget();
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onTouchUp(long deviceId) {
            FingerprintManager.this.mHandler.obtainMessage(1002, 0, 0, Long.valueOf(deviceId)).sendToTarget();
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onMonitorEventTriggered(int type, String data) {
            FingerprintManager.this.mHandler.obtainMessage(1003, 0, type, data).sendToTarget();
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onImageInfoAcquired(int type, int quality, int matchScore) {
            FingerprintManager.this.mHandler.obtainMessage(1004, 0, 0, new FingerprintImageInfo(type, quality, matchScore)).sendToTarget();
        }

        @Override // android.hardware.fingerprint.IFingerprintServiceReceiver
        public void onEnumerated(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintManager.this.mHandler.obtainMessage(106, fingerId, groupId, Long.valueOf(deviceId)).sendToTarget();
        }
    };
    private IBinder mToken = new Binder();

    public interface EngineeringInfoCallback {
        void onEngineeringInfoUpdated(EngineeringInfo engineeringInfo);

        void onError(int i, CharSequence charSequence);
    }

    public interface FingerprintCommandCallback {
        void onFingerprintCmd(int i, byte[] bArr);
    }

    public interface FingerprintInputCallback {
        void onTouchDown();
    }

    public interface MonitorEventCallback {
        void onMonitorEventTriggered(int i, String str);
    }

    private class OnEnrollCancelListener implements CancellationSignal.OnCancelListener {
        private OnEnrollCancelListener() {
        }

        @Override // android.os.CancellationSignal.OnCancelListener
        public void onCancel() {
            FingerprintManager.this.cancelEnrollment();
        }
    }

    /* access modifiers changed from: private */
    public class OnAuthenticationCancelListener implements CancellationSignal.OnCancelListener {
        private android.hardware.biometrics.CryptoObject mCrypto;

        public OnAuthenticationCancelListener(android.hardware.biometrics.CryptoObject crypto) {
            this.mCrypto = crypto;
        }

        @Override // android.os.CancellationSignal.OnCancelListener
        public void onCancel() {
            FingerprintManager.this.cancelAuthentication(this.mCrypto);
        }
    }

    @Deprecated
    public static final class CryptoObject extends android.hardware.biometrics.CryptoObject {
        public CryptoObject(Signature signature) {
            super(signature);
        }

        public CryptoObject(Cipher cipher) {
            super(cipher);
        }

        public CryptoObject(Mac mac) {
            super(mac);
        }

        @Override // android.hardware.biometrics.CryptoObject
        public Signature getSignature() {
            return super.getSignature();
        }

        @Override // android.hardware.biometrics.CryptoObject
        public Cipher getCipher() {
            return super.getCipher();
        }

        @Override // android.hardware.biometrics.CryptoObject
        public Mac getMac() {
            return super.getMac();
        }
    }

    @Deprecated
    public static class AuthenticationResult {
        private CryptoObject mCryptoObject;
        private Fingerprint mFingerprint;
        private int mUserId;

        public AuthenticationResult(CryptoObject crypto, Fingerprint fingerprint, int userId) {
            this.mCryptoObject = crypto;
            this.mFingerprint = fingerprint;
            this.mUserId = userId;
        }

        public CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }

        @UnsupportedAppUsage
        public Fingerprint getFingerprint() {
            return this.mFingerprint;
        }

        public int getUserId() {
            return this.mUserId;
        }
    }

    @Deprecated
    public static abstract class AuthenticationCallback extends BiometricAuthenticator.AuthenticationCallback {
        @Override // android.hardware.biometrics.BiometricAuthenticator.AuthenticationCallback
        public void onAuthenticationError(int errorCode, CharSequence errString) {
        }

        @Override // android.hardware.biometrics.BiometricAuthenticator.AuthenticationCallback
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        }

        public void onAuthenticationSucceeded(AuthenticationResult result) {
        }

        @Override // android.hardware.biometrics.BiometricAuthenticator.AuthenticationCallback
        public void onAuthenticationFailed() {
        }

        @Override // android.hardware.biometrics.BiometricAuthenticator.AuthenticationCallback
        public void onAuthenticationAcquired(int acquireInfo) {
        }

        public void onImageInfoAcquired(OppoFingerprintManagerEx.FingerprintImageInfoBase info) {
        }

        public void onTouchDown() {
        }

        public void onTouchUp() {
        }
    }

    public class FingerprintImageInfo extends OppoFingerprintManagerEx.FingerprintImageInfoBase {
        public FingerprintImageInfo(int type, int quality, int matchScore) {
            super(type, quality, matchScore);
        }
    }

    public static abstract class EnrollmentCallback {
        public void onEnrollmentError(int errMsgId, CharSequence errString) {
        }

        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
        }

        public void onEnrollmentProgress(int remaining) {
        }

        public void onTouchUp() {
        }
    }

    public static abstract class RemovalCallback {
        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
        }

        public void onRemovalSucceeded(Fingerprint fp, int remaining) {
        }
    }

    public static abstract class EnumerateCallback {
        public void onEnumerateError(int errMsgId, CharSequence errString) {
        }

        public void onEnumerate(Fingerprint fingerprint) {
        }
    }

    public static abstract class LockoutResetCallback {
        public void onLockoutReset() {
        }
    }

    public static abstract class OpticalFingerprintListener {
        public void onOpticalFingerprintUpdate(int status) {
        }
    }

    @Deprecated
    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler) {
        ((IColorAntiVirusBehaviorManager) OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0])).setAction(148, Binder.getCallingUid());
        authenticate(crypto, cancel, flags, callback, handler, this.mContext.getUserId());
    }

    private void useHandler(Handler handler) {
        if (KEYGUARD_PACKAGENAME.equals(this.mContext.getOpPackageName())) {
            if (handler != null) {
                if (DEBUG) {
                    Slog.d(TAG, "keyguard Handler");
                }
                this.mHandler = new MyHandler(handler.getLooper(), null, true);
                return;
            }
            if (DEBUG) {
                Slog.d(TAG, "new Handler for keyguard");
            }
            this.mHandler = new MyHandler(this.mContext.getMainLooper(), null, true);
        } else if (handler != null) {
            this.mHandler = new MyHandler(handler.getLooper());
        } else if (this.mHandler.getLooper() != this.mContext.getMainLooper()) {
            this.mHandler = new MyHandler(this.mContext.getMainLooper());
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler, int userId) {
        if (callback != null) {
            if (cancel != null) {
                if (cancel.isCanceled()) {
                    Slog.w(TAG, "authentication already canceled");
                    return;
                }
                cancel.setOnCancelListener(new OnAuthenticationCancelListener(crypto));
            }
            if (this.mService != null) {
                try {
                    useHandler(handler);
                    this.mFingerprintInputCallback = null;
                    this.mAuthenticationCallback = callback;
                    this.mCryptoObject = crypto;
                    this.mService.authenticate(this.mToken, crypto != null ? crypto.getOpId() : 0, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    Slog.w(TAG, "Remote exception while authenticating: ", e);
                    callback.onAuthenticationError(1, getErrorString(this.mContext, 1, 0));
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an authentication callback");
        }
    }

    public void enroll(byte[] token, CancellationSignal cancel, int flags, int userId, EnrollmentCallback callback) {
        if (userId == -2) {
            userId = getCurrentUserId();
        }
        if (callback != null) {
            if (cancel != null) {
                if (cancel.isCanceled()) {
                    Slog.w(TAG, "enrollment already canceled");
                    return;
                }
                cancel.setOnCancelListener(new OnEnrollCancelListener());
            }
            IFingerprintService iFingerprintService = this.mService;
            if (iFingerprintService != null) {
                try {
                    this.mEnrollmentCallback = callback;
                    iFingerprintService.enroll(this.mToken, token, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    Slog.w(TAG, "Remote exception in enroll: ", e);
                    callback.onEnrollmentError(1, getErrorString(this.mContext, 1, 0));
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an enrollment callback");
        }
    }

    public long preEnroll() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService == null) {
            return 0;
        }
        try {
            return iFingerprintService.preEnroll(this.mToken);
        } catch (RemoteException e) {
            Slog.w(TAG, "Remote exception in pre enroll: ", e);
            return 0;
        }
    }

    public boolean pauseEnroll() {
        int result = 0;
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                result = iFingerprintService.pauseEnroll();
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in pauseEnroll: ", e);
            }
        }
        return result >= 0;
    }

    public boolean continueEnroll() {
        int result = 0;
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                result = iFingerprintService.continueEnroll();
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in continueEnroll: ", e);
            }
        }
        return result >= 0;
    }

    public int postEnroll() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService == null) {
            return 0;
        }
        try {
            return iFingerprintService.postEnroll(this.mToken);
        } catch (RemoteException e) {
            Slog.w(TAG, "Remote exception in post enroll: ", e);
            return 0;
        }
    }

    @Override // android.hardware.biometrics.BiometricAuthenticator
    public void setActiveUser(int userId) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.setActiveUser(userId);
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in setActiveUser: ", e);
            }
        }
    }

    public void remove(Fingerprint fp, int userId, RemovalCallback callback) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                this.mRemovalCallback = callback;
                this.mRemovalFingerprint = fp;
                iFingerprintService.remove(this.mToken, fp.getBiometricId(), fp.getGroupId(), userId, this.mServiceReceiver, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in remove: ", e);
                if (callback != null) {
                    callback.onRemovalError(fp, 1, getErrorString(this.mContext, 1, 0));
                }
            }
        }
    }

    public void enumerate(int userId, EnumerateCallback callback) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                this.mEnumerateCallback = callback;
                iFingerprintService.enumerate(this.mToken, userId, this.mServiceReceiver, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in enumerate: ", e);
                if (callback != null) {
                    callback.onEnumerateError(1, getErrorString(this.mContext, 1, 0));
                }
            }
        }
    }

    public void rename(int fpId, int userId, String newName) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.rename(fpId, userId, newName);
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in rename(): ", e);
            }
        } else {
            Slog.w(TAG, "rename(): Service not connected!");
        }
    }

    @UnsupportedAppUsage
    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService == null) {
            return null;
        }
        try {
            return iFingerprintService.getEnrolledFingerprints(userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            Slog.w(TAG, "Remote exception in getEnrolledFingerprints: ", e);
            return null;
        }
    }

    @UnsupportedAppUsage
    public List<Fingerprint> getEnrolledFingerprints() {
        return getEnrolledFingerprints(this.mContext.getUserId());
    }

    @Override // android.hardware.biometrics.BiometricAuthenticator
    public boolean hasEnrolledTemplates() {
        return hasEnrolledFingerprints();
    }

    @Override // android.hardware.biometrics.BiometricAuthenticator
    public boolean hasEnrolledTemplates(int userId) {
        return hasEnrolledFingerprints(userId);
    }

    @Deprecated
    public boolean hasEnrolledFingerprints() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService == null) {
            return false;
        }
        try {
            return iFingerprintService.hasEnrolledFingerprints(this.mContext.getUserId(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            Slog.w(TAG, "Remote exception in hasEnrolledFingerprints: ", e);
            return false;
        }
    }

    public boolean hasEnrolledFingerprints(int userId) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService == null) {
            return false;
        }
        try {
            return iFingerprintService.hasEnrolledFingerprints(userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            Slog.w(TAG, "Remote exception in hasEnrolledFingerprints: ", e);
            return false;
        }
    }

    @Override // android.hardware.biometrics.BiometricAuthenticator
    @Deprecated
    public boolean isHardwareDetected() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.isHardwareDetected(0, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in isFingerprintHardwareDetected: ", e);
                return false;
            }
        } else {
            Slog.w(TAG, "isFingerprintHardwareDetected(): Service not connected!");
            return false;
        }
    }

    @UnsupportedAppUsage
    public long getAuthenticatorId() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.getAuthenticatorId(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in getAuthenticatorId(): ", e);
                return 0;
            }
        } else {
            Slog.w(TAG, "getAuthenticatorId(): Service not connected!");
            return 0;
        }
    }

    public void addLockoutResetCallback(final LockoutResetCallback callback) {
        if (this.mService != null) {
            try {
                Slog.d(TAG, "[addLockoutResetCallback] opPackageName = " + this.mContext.getOpPackageName());
                final PowerManager powerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
                this.mService.addLockoutResetCallback(new IBiometricServiceLockoutResetCallback.Stub() {
                    /* class android.hardware.fingerprint.FingerprintManager.AnonymousClass1 */

                    @Override // android.hardware.biometrics.IBiometricServiceLockoutResetCallback
                    public void onLockoutReset(long deviceId, IRemoteCallback serverCallback) throws RemoteException {
                        try {
                            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(1, "lockoutResetCallback");
                            wakeLock.acquire();
                            FingerprintManager.this.mHandler.post(new Runnable(wakeLock) {
                                /* class android.hardware.fingerprint.$$Lambda$FingerprintManager$1$4i3tUU8mafgvA9HaB2UPD31L6UY */
                                private final /* synthetic */ PowerManager.WakeLock f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void run() {
                                    FingerprintManager.AnonymousClass1.lambda$onLockoutReset$0(FingerprintManager.LockoutResetCallback.this, this.f$1);
                                }
                            });
                        } finally {
                            serverCallback.sendResult(null);
                        }
                    }

                    static /* synthetic */ void lambda$onLockoutReset$0(LockoutResetCallback callback, PowerManager.WakeLock wakeLock) {
                        try {
                            callback.onLockoutReset();
                        } finally {
                            wakeLock.release();
                        }
                    }
                });
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in addLockoutResetCallback(): ", e);
            }
        } else {
            Slog.w(TAG, "addLockoutResetCallback(): Service not connected!");
        }
    }

    public int getEnrollmentTotalTimes() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService == null) {
            return 0;
        }
        try {
            return iFingerprintService.getEnrollmentTotalTimes(this.mToken);
        } catch (RemoteException e) {
            Slog.w(TAG, "Remote exception in enroll: ", e);
            return 0;
        }
    }

    private class OnTouchEventMonitorCancelListener implements CancellationSignal.OnCancelListener {
        private OnTouchEventMonitorCancelListener() {
        }

        @Override // android.os.CancellationSignal.OnCancelListener
        public void onCancel() {
            FingerprintManager.this.cancelTouchEventListener();
        }
    }

    public void setFingerKeymode(int enable) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.setFingerKeymode(this.mToken, UserHandle.myUserId(), this.mContext.getOpPackageName(), enable);
            } catch (RemoteException e) {
                Slog.e(TAG, "Remote exception in setFingerKeymode ", e);
            }
        }
    }

    public void setTouchEventListener(FingerprintInputCallback callback, CancellationSignal cancel) {
        if (callback != null) {
            if (cancel != null) {
                if (cancel.isCanceled()) {
                    Slog.w(TAG, "setTouchEventListener already canceled");
                    return;
                }
                cancel.setOnCancelListener(new OnTouchEventMonitorCancelListener());
            }
            IFingerprintService iFingerprintService = this.mService;
            if (iFingerprintService != null) {
                this.mAuthenticationCallback = null;
                this.mFingerprintInputCallback = callback;
                try {
                    iFingerprintService.setTouchEventListener(this.mToken, this.mServiceReceiver, UserHandle.myUserId(), this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    Slog.e(TAG, "Remote exception in setTouchEventListener(): ", e);
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an setTouchEventListener callback");
        }
    }

    public void cancelTouchEventListener() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.cancelTouchEventListener(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Slog.w(TAG, "Remote exception while canceling touchevent");
                }
            }
        }
    }

    public void setMonitorEventListener(MonitorEventCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an setMonitorEventListener callback");
        } else if (this.mService != null) {
            this.mMonitorEventCallback = callback;
        }
    }

    public void pauseIdentify() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.pauseIdentify(this.mToken);
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in pauseIdentify: ", e);
            }
        }
    }

    public void continueIdentify() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.continueIdentify(this.mToken);
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in continueIdentify: ", e);
            }
        }
    }

    public void finishUnLockedScreen(boolean authenticated) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.finishUnLockedScreen(authenticated, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.e(TAG, "Remote exception in finishUnLockedScreen(): ", e);
            }
        } else {
            Slog.w(TAG, "finishUnLockedScreen(): Service not connected!");
        }
    }

    public int getAlikeyStatus() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.getAlikeyStatus();
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in getAlikeyStatus(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "getAlikeyStatus(): Service not connected!");
            return -1;
        }
    }

    public int getEngineeringInfo(EngineeringInfoCallback callback, int type) {
        if (callback != null) {
            IFingerprintService iFingerprintService = this.mService;
            if (iFingerprintService != null) {
                try {
                    this.mEngineeringInfoCallback = callback;
                    return iFingerprintService.getEngineeringInfo(this.mToken, this.mContext.getOpPackageName(), UserHandle.myUserId(), this.mServiceReceiver, type);
                } catch (RemoteException e) {
                    Slog.v(TAG, "Remote exception in getEngineeringInfo(): ", e);
                    return -1;
                }
            } else {
                Slog.w(TAG, "getEngineeringInfo(): Service not connected!");
                return -1;
            }
        } else {
            throw new IllegalArgumentException("Must supply an getEngineeringInfo callback");
        }
    }

    public void cancelGetEngineeringInfo(int type) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.cancelGetEngineeringInfo(this.mToken, this.mContext.getOpPackageName(), type);
                this.mEngineeringInfoCallback = null;
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in cancelgetEngineeringInfo(): ", e);
            }
        } else {
            Slog.w(TAG, "cancelgetEngineeringInfo(): Service not connected!");
        }
    }

    public byte[] alipayInvokeCommand(byte[] inbuf) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.alipayInvokeCommand(inbuf);
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in alipayInvokeCommand(): ", e);
                return null;
            }
        } else {
            Slog.w(TAG, "alipayInvokeCommand(): Service not connected!");
            return null;
        }
    }

    public int touchDown() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.touchDown();
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in touchDown(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "touchDown(): Service not connected!");
            return -1;
        }
    }

    public int touchUp() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.touchUp();
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in touchUp(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "touchUp(): Service not connected!");
            return -1;
        }
    }

    public int sendFingerprintCmd(int cmdId, byte[] inbuf) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.sendFingerprintCmd(cmdId, inbuf);
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in sendFingerprintCmd(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "sendFingerprintCmd(): Service not connected!");
            return -1;
        }
    }

    public byte[] getFingerprintAuthToken() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.getFingerprintAuthToken(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in getFingerprintAuthToken(): ", e);
                return null;
            }
        } else {
            Slog.w(TAG, "getFingerprintAuthToken(): Service not connected!");
            return null;
        }
    }

    public int regsiterFingerprintCmdCallback(final FingerprintCommandCallback callback) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.regsiterFingerprintCmdCallback(new IFingerprintCommandCallback.Stub() {
                    /* class android.hardware.fingerprint.FingerprintManager.AnonymousClass2 */

                    @Override // android.hardware.fingerprint.IFingerprintCommandCallback
                    public void onFingerprintCmd(int cmdId, byte[] result) {
                        callback.onFingerprintCmd(cmdId, result);
                    }
                });
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in regsiterFingerprintCmdCallback(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "regsiterFingerprintCmdCallback(): Service not connected!");
            return -1;
        }
    }

    public int unregsiterFingerprintCmdCallback(final FingerprintCommandCallback callback) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.unregsiterFingerprintCmdCallback(new IFingerprintCommandCallback.Stub() {
                    /* class android.hardware.fingerprint.FingerprintManager.AnonymousClass3 */

                    @Override // android.hardware.fingerprint.IFingerprintCommandCallback
                    public void onFingerprintCmd(int cmdId, byte[] result) {
                        callback.onFingerprintCmd(cmdId, result);
                    }
                });
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in unregsiterFingerprintCmdCallback(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "unregsiterFingerprintCmdCallback(): Service not connected!");
            return -1;
        }
    }

    @Override // android.hardware.fingerprint.IOppoFingerprintManagerEx
    public int regsiterOpticalFingerprintListener(final IOppoFingerprintManagerEx.OpticalFingerprintListener listener) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.regsiterOpticalFingerprintListener(new IOpticalFingerprintListener.Stub() {
                    /* class android.hardware.fingerprint.FingerprintManager.AnonymousClass4 */

                    @Override // android.hardware.fingerprint.IOpticalFingerprintListener
                    public void onOpticalFingerprintUpdate(int status) {
                        listener.onOpticalFingerprintUpdate(status);
                    }
                });
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in regsiterOpticalFingerprintListener(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "regsiterOpticalFingerprintListener(): Service not connected!");
            return -1;
        }
    }

    public int regsiterOpticalFingerprintListener(final OpticalFingerprintListener listener) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.regsiterOpticalFingerprintListener(new IOpticalFingerprintListener.Stub() {
                    /* class android.hardware.fingerprint.FingerprintManager.AnonymousClass5 */

                    @Override // android.hardware.fingerprint.IOpticalFingerprintListener
                    public void onOpticalFingerprintUpdate(int status) {
                        listener.onOpticalFingerprintUpdate(status);
                    }
                });
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in regsiterOpticalFingerprintListener(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "regsiterOpticalFingerprintListener(): Service not connected!");
            return -1;
        }
    }

    public int unregsiterOpticalFingerprintListener() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.unregsiterOpticalFingerprintListener();
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in unregsiterOpticalFingerprintListener(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "unregsiterOpticalFingerprintListener(): Service not connected!");
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        private MyHandler(Looper looper, Handler.Callback callback, boolean async) {
            super(looper, callback, async);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            switch (i) {
                case 100:
                    FingerprintManager.this.sendEnrollResult((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 101:
                    FingerprintManager.this.sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 102:
                    FingerprintManager.this.sendAuthenticatedSucceeded((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 103:
                    FingerprintManager.this.sendAuthenticatedFailed();
                    return;
                case 104:
                    FingerprintManager.this.sendErrorResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 105:
                    Fingerprint fingerprint = (Fingerprint) msg.obj;
                    FingerprintManager.this.sendRemovedResult(fingerprint.getDeviceId(), fingerprint.getBiometricId(), fingerprint.getGroupId(), msg.arg1);
                    return;
                case 106:
                    FingerprintManager.this.sendEnumeratedResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                default:
                    switch (i) {
                        case 1001:
                            FingerprintManager.this.sendTouchDownEvent(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                            return;
                        case 1002:
                            FingerprintManager.this.sendTouchUpEvent(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                            return;
                        case 1003:
                            FingerprintManager.this.sendMonitorEventTriggered(msg.arg2, (String) msg.obj);
                            return;
                        case 1004:
                            FingerprintManager.this.sendImageInfo((OppoFingerprintManagerEx.FingerprintImageInfoBase) msg.obj);
                            return;
                        case 1005:
                            FingerprintManager.this.sendEngineeringInfo((EngineeringInfo) msg.obj);
                            return;
                        default:
                            return;
                    }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEngineeringInfo(EngineeringInfo info) {
        EngineeringInfoCallback engineeringInfoCallback = this.mEngineeringInfoCallback;
        if (engineeringInfoCallback != null) {
            engineeringInfoCallback.onEngineeringInfoUpdated(info);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendTouchDownEvent(long deviceId, int fingerId, int groupId) {
        FingerprintInputCallback fingerprintInputCallback = this.mFingerprintInputCallback;
        if (fingerprintInputCallback != null) {
            fingerprintInputCallback.onTouchDown();
        }
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onTouchDown();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendTouchUpEvent(long deviceId, int fingerId, int groupId) {
        EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onTouchUp();
        }
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onTouchUp();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMonitorEventTriggered(int type, String data) {
        MonitorEventCallback monitorEventCallback = this.mMonitorEventCallback;
        if (monitorEventCallback != null) {
            monitorEventCallback.onMonitorEventTriggered(type, data);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendImageInfo(OppoFingerprintManagerEx.FingerprintImageInfoBase info) {
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onImageInfoAcquired(info);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendRemovedResult(long deviceId, int fingerId, int groupId, int remaining) {
        if (this.mRemovalCallback != null) {
            int reqFingerId = this.mRemovalFingerprint.getBiometricId();
            int reqGroupId = this.mRemovalFingerprint.getGroupId();
            if (reqFingerId != 0 && fingerId != 0 && fingerId != reqFingerId) {
                Slog.w(TAG, "Finger id didn't match: " + fingerId + " != " + reqFingerId);
            } else if (groupId != reqGroupId) {
                Slog.w(TAG, "Group id didn't match: " + groupId + " != " + reqGroupId);
            } else {
                if (DEBUG) {
                    Slog.d(TAG, "onRemovalSucceeded");
                }
                this.mRemovalCallback.onRemovalSucceeded(new Fingerprint(null, groupId, fingerId, deviceId), remaining);
            }
        }
    }

    private void sendRemovedResult(Fingerprint fingerprint, int remaining) {
        if (this.mRemovalCallback != null) {
            if (fingerprint == null) {
                Slog.e(TAG, "Received MSG_REMOVED, but fingerprint is null");
                return;
            }
            int fingerId = fingerprint.getBiometricId();
            int reqFingerId = this.mRemovalFingerprint.getBiometricId();
            if (reqFingerId == 0 || fingerId == 0 || fingerId == reqFingerId) {
                int groupId = fingerprint.getGroupId();
                int reqGroupId = this.mRemovalFingerprint.getGroupId();
                if (groupId != reqGroupId) {
                    Slog.w(TAG, "Group id didn't match: " + groupId + " != " + reqGroupId);
                    return;
                }
                this.mRemovalCallback.onRemovalSucceeded(fingerprint, remaining);
                return;
            }
            Slog.w(TAG, "Finger id didn't match: " + fingerId + " != " + reqFingerId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEnumeratedResult(long deviceId, int fingerId, int groupId) {
        EnumerateCallback enumerateCallback = this.mEnumerateCallback;
        if (enumerateCallback != null) {
            enumerateCallback.onEnumerate(new Fingerprint(null, groupId, fingerId, deviceId));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEnrollResult(Fingerprint fp, int remaining) {
        EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollmentProgress(remaining);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAuthenticatedSucceeded(Fingerprint fp, int userId) {
        if (DEBUG) {
            Slog.d(TAG, "sendAuthenticatedSucceeded");
        }
        if (this.mAuthenticationCallback != null) {
            this.mAuthenticationCallback.onAuthenticationSucceeded(new AuthenticationResult(this.mCryptoObject, fp, userId));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAuthenticatedFailed() {
        if (DEBUG) {
            Slog.d(TAG, "sendAuthenticatedFailed");
        }
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationFailed();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAcquiredResult(long deviceId, int acquireInfo, int vendorCode) {
        if (DEBUG) {
            Slog.d(TAG, "sendAcquiredResult");
        }
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationAcquired(acquireInfo);
        }
        String msg = getAcquiredString(this.mContext, acquireInfo, vendorCode);
        if (msg != null) {
            int clientInfo = acquireInfo == 6 ? vendorCode + 1000 : acquireInfo;
            EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
            if (enrollmentCallback != null) {
                enrollmentCallback.onEnrollmentHelp(clientInfo, msg);
                return;
            }
            AuthenticationCallback authenticationCallback2 = this.mAuthenticationCallback;
            if (authenticationCallback2 != null) {
                authenticationCallback2.onAuthenticationHelp(clientInfo, msg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendErrorResult(long deviceId, int errMsgId, int vendorCode) {
        int clientErrMsgId = errMsgId == 8 ? vendorCode + 1000 : errMsgId;
        EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollmentError(clientErrMsgId, getErrorString(this.mContext, errMsgId, vendorCode));
            return;
        }
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationError(clientErrMsgId, getErrorString(this.mContext, errMsgId, vendorCode));
            return;
        }
        RemovalCallback removalCallback = this.mRemovalCallback;
        if (removalCallback != null) {
            removalCallback.onRemovalError(this.mRemovalFingerprint, clientErrMsgId, getErrorString(this.mContext, errMsgId, vendorCode));
            return;
        }
        EnumerateCallback enumerateCallback = this.mEnumerateCallback;
        if (enumerateCallback != null) {
            enumerateCallback.onEnumerateError(clientErrMsgId, getErrorString(this.mContext, errMsgId, vendorCode));
        }
    }

    public FingerprintManager(Context context, IFingerprintService service) {
        this.mContext = context;
        this.mService = service;
        if (this.mService == null) {
            Slog.v(TAG, "FingerprintManagerService was null");
        }
        this.mHandler = new MyHandler(context);
    }

    private int getCurrentUserId() {
        try {
            return ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to get current user id\n");
            return -10000;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelEnrollment() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.cancelEnrollment(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Slog.w(TAG, "Remote exception while canceling enrollment");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelAuthentication(android.hardware.biometrics.CryptoObject cryptoObject) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.cancelAuthentication(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Slog.w(TAG, "Remote exception while canceling enrollment");
                }
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static String getErrorString(Context context, int errMsg, int vendorCode) {
        switch (errMsg) {
            case 1:
                return context.getString(R.string.fingerprint_error_hw_not_available);
            case 2:
                return context.getString(R.string.fingerprint_error_unable_to_process);
            case 3:
                return context.getString(R.string.fingerprint_error_timeout);
            case 4:
                return context.getString(R.string.fingerprint_error_no_space);
            case 5:
                return context.getString(R.string.fingerprint_error_canceled);
            case 7:
                return context.getString(R.string.fingerprint_error_lockout);
            case 8:
                String[] msgArray = context.getResources().getStringArray(R.array.fingerprint_error_vendor);
                if (vendorCode < msgArray.length) {
                    return msgArray[vendorCode];
                }
                break;
            case 9:
                return context.getString(R.string.fingerprint_error_lockout_permanent);
            case 10:
                return context.getString(R.string.fingerprint_error_user_canceled);
            case 11:
                return context.getString(R.string.fingerprint_error_no_fingerprints);
            case 12:
                return context.getString(R.string.fingerprint_error_hw_not_present);
            case 15:
                return "restart authenticate";
        }
        Slog.w(TAG, "Invalid error message: " + errMsg + ", " + vendorCode);
        return null;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static String getAcquiredString(Context context, int acquireInfo, int vendorCode) {
        if (acquireInfo == 1001) {
            return "acquared too similar";
        }
        if (acquireInfo == 1002) {
            return "already enrolled finger";
        }
        switch (acquireInfo) {
            case 0:
                return null;
            case 1:
                return context.getString(R.string.fingerprint_acquired_partial);
            case 2:
                return context.getString(R.string.fingerprint_acquired_insufficient);
            case 3:
                return context.getString(R.string.fingerprint_acquired_imager_dirty);
            case 4:
                return context.getString(R.string.fingerprint_acquired_too_slow);
            case 5:
                return context.getString(R.string.fingerprint_acquired_too_fast);
            case 6:
                String[] msgArray = context.getResources().getStringArray(R.array.fingerprint_acquired_vendor);
                if (vendorCode < msgArray.length) {
                    return msgArray[vendorCode];
                }
                break;
        }
        Slog.w(TAG, "Invalid acquired message: " + acquireInfo + ", " + vendorCode);
        return null;
    }

    @Override // android.hardware.fingerprint.IOppoFingerprintManagerEx
    public long getLockoutAttemptDeadline() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.getLockoutAttemptDeadline(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in getLockoutAttemptDeadline(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "getLockoutAttemptDeadline(): Service not connected!");
            return -1;
        }
    }

    @Override // android.hardware.fingerprint.IOppoFingerprintManagerEx
    public int getFailedAttempts() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.getFailedAttempts(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in getFailedAttempts(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "getFailedAttempts(): Service not connected!");
            return -1;
        }
    }

    @Override // android.hardware.fingerprint.IOppoFingerprintManagerEx
    public void showFingerprintIcon() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                iFingerprintService.showFingerprintIcon(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in showFingerprintIcon(): ", e);
            }
        } else {
            Slog.w(TAG, "showFingerprintIcon(): Service not connected!");
        }
    }

    @Override // android.hardware.fingerprint.IOppoFingerprintManagerEx
    public void hideFingerprintIcon() {
        if (!isFingerprintIconHided()) {
            IFingerprintService iFingerprintService = this.mService;
            if (iFingerprintService != null) {
                try {
                    iFingerprintService.hideFingerprintIcon(0, this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    Slog.v(TAG, "Remote exception in hideFingerprintIcon(): ", e);
                }
            } else {
                Slog.w(TAG, "hideFingerprintIcon(): Service not connected!");
            }
        }
    }

    private boolean isFingerprintIconHided() {
        if (getCurrentIconStatus() == 0) {
            return true;
        }
        return false;
    }

    public int getCurrentIconStatus() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                return iFingerprintService.getCurrentIconStatus();
            } catch (RemoteException e) {
                Slog.v(TAG, "Remote exception in updateOpticalFingerprintIcon(): ", e);
                return -1;
            }
        } else {
            Slog.w(TAG, "updateOpticalFingerprintIcon(): Service not connected!");
            return -1;
        }
    }
}
