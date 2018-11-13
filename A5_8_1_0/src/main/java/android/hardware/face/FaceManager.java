package android.hardware.face;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.face.IFaceServiceReceiver.Stub;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.security.keystore.AndroidKeyStoreProvider;
import android.util.Log;
import android.util.Slog;
import android.view.Surface;
import java.security.Signature;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class FaceManager {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final int FACE_ACQUIRED_BRIGHT = 106;
    public static final int FACE_ACQUIRED_CAMERA_PREVIEW = 1001;
    public static final int FACE_ACQUIRED_DARK = 103;
    public static final int FACE_ACQUIRED_DOWN = 110;
    public static final int FACE_ACQUIRED_FAR_FACE = 6;
    public static final int FACE_ACQUIRED_GOOD = 0;
    public static final int FACE_ACQUIRED_HACKER = 104;
    public static final int FACE_ACQUIRED_IMAGER_DIRTY = 3;
    public static final int FACE_ACQUIRED_INSUFFICIENT = 2;
    public static final int FACE_ACQUIRED_LEFT = 107;
    public static final int FACE_ACQUIRED_LOW_SIMILARITY = 105;
    public static final int FACE_ACQUIRED_NEAR_FACE = 7;
    public static final int FACE_ACQUIRED_NO_FACE = 101;
    public static final int FACE_ACQUIRED_PARTIAL = 1;
    public static final int FACE_ACQUIRED_RIGHT = 108;
    public static final int FACE_ACQUIRED_SHIFTING = 102;
    public static final int FACE_ACQUIRED_TOO_FAST = 5;
    public static final int FACE_ACQUIRED_TOO_SLOW = 4;
    public static final int FACE_ACQUIRED_UP = 109;
    public static final int FACE_ACQUIRED_VENDOR_BASE = 1000;
    public static final int FACE_AUTHENTICATE_AUTO = 0;
    public static final int FACE_AUTHENTICATE_BY_USER = 1;
    public static final int FACE_AUTHENTICATE_BY_USER_WITH_ANIM = 2;
    public static final int FACE_ERROR_CAMERA_UNAVAILABLE = 8;
    public static final int FACE_ERROR_CANCELED = 5;
    public static final int FACE_ERROR_HW_UNAVAILABLE = 1;
    public static final int FACE_ERROR_LOCKOUT = 7;
    public static final int FACE_ERROR_NO_SPACE = 4;
    public static final int FACE_ERROR_TIMEOUT = 3;
    public static final int FACE_ERROR_UNABLE_TO_PROCESS = 2;
    public static final int FACE_ERROR_UNABLE_TO_REMOVE = 6;
    public static final int FACE_ERROR_VENDOR_BASE = 1000;
    public static final String FACE_KEYGUARD_CANCELED_BY_SCREEN_OFF = "cancelRecognitionByScreenOff";
    public static final int FACE_WITH_EYES_CLOSED = 111;
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_COMMOND_RESULT = 1000;
    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ERROR = 104;
    private static final int MSG_PROGRESS_CHANGED = 106;
    private static final int MSG_REMOVED = 105;
    private static final String TAG = "FaceManager";
    private AuthenticationCallback mAuthenticationCallback;
    private CommandResultCallback mCommandResultCallback;
    private Context mContext;
    private CryptoObject mCryptoObject;
    private EnrollmentCallback mEnrollmentCallback;
    private Handler mHandler;
    private RemovalCallback mRemovalCallback;
    private FaceFeature mRemovalFaceFeature;
    private IFaceService mService;
    private IFaceServiceReceiver mServiceReceiver = new Stub() {
        public void onEnrollResult(long deviceId, int faceFeatureId, int groupId, int remaining) {
            FaceManager.this.mHandler.obtainMessage(100, remaining, 0, new FaceFeature(null, groupId, faceFeatureId, deviceId)).sendToTarget();
        }

        public void onAcquired(long deviceId, int acquireInfo) {
            FaceManager.this.mHandler.obtainMessage(101, acquireInfo, 0, Long.valueOf(deviceId)).sendToTarget();
        }

        public void onAuthenticationSucceeded(long deviceId, FaceFeature faceFeature, int userId) {
            FaceManager.this.mHandler.obtainMessage(102, userId, 0, faceFeature).sendToTarget();
        }

        public void onAuthenticationFailed(long deviceId) {
            FaceManager.this.mHandler.obtainMessage(103).sendToTarget();
        }

        public void onError(long deviceId, int error) {
            FaceManager.this.mHandler.obtainMessage(104, error, 0, Long.valueOf(deviceId)).sendToTarget();
        }

        public void onRemoved(long deviceId, int faceFeatureId, int groupId) {
            FaceManager.this.mHandler.obtainMessage(105, faceFeatureId, groupId, Long.valueOf(deviceId)).sendToTarget();
        }

        public void onCommandResult(CommandResult info) {
            FaceManager.this.mHandler.obtainMessage(1000, 0, 0, info).sendToTarget();
        }

        public void onProgressChanged(long deviceId, int progressInfo) {
            FaceManager.this.mHandler.obtainMessage(106, progressInfo, 0, Long.valueOf(deviceId)).sendToTarget();
        }
    };
    private IBinder mToken = new Binder();

    public static abstract class AuthenticationCallback {
        public void onAuthenticationError(int errorCode, CharSequence errString) {
        }

        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        }

        public void onAuthenticationSucceeded(AuthenticationResult result) {
        }

        public void onAuthenticationFailed() {
        }

        public void onAuthenticationAcquired(int acquireInfo) {
        }

        public void onProgressChanged(int progressInfo) {
        }
    }

    public static class AuthenticationResult {
        private CryptoObject mCryptoObject;
        private FaceFeature mFaceFeature;
        private int mUserId;

        public AuthenticationResult(CryptoObject crypto, FaceFeature faceFeature, int userId) {
            this.mCryptoObject = crypto;
            this.mFaceFeature = faceFeature;
            this.mUserId = userId;
        }

        public CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }

        public FaceFeature getFaceFeature() {
            return this.mFaceFeature;
        }

        public int getUserId() {
            return this.mUserId;
        }
    }

    public interface CommandResultCallback {
        void onCommandResult(CommandResult commandResult);

        void onError(int i, CharSequence charSequence);
    }

    public class CommandType {
        public static final int GET_IMAGE_SNR = 0;
    }

    public static final class CryptoObject {
        private final Object mCrypto;

        public CryptoObject(Signature signature) {
            this.mCrypto = signature;
        }

        public CryptoObject(Cipher cipher) {
            this.mCrypto = cipher;
        }

        public CryptoObject(Mac mac) {
            this.mCrypto = mac;
        }

        public Signature getSignature() {
            return this.mCrypto instanceof Signature ? (Signature) this.mCrypto : null;
        }

        public Cipher getCipher() {
            return this.mCrypto instanceof Cipher ? (Cipher) this.mCrypto : null;
        }

        public Mac getMac() {
            return this.mCrypto instanceof Mac ? (Mac) this.mCrypto : null;
        }

        public long getOpId() {
            return this.mCrypto != null ? AndroidKeyStoreProvider.getKeyStoreOperationHandle(this.mCrypto) : 0;
        }
    }

    public static abstract class EnrollmentCallback {
        public void onEnrollmentError(int errMsgId, CharSequence errString) {
        }

        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
        }

        public void onEnrollmentProgress(int remaining) {
        }

        public void onProgressChanged(int progressInfo) {
        }
    }

    public static abstract class LockoutResetCallback {
        public void onLockoutReset() {
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    sendEnrollResult((FaceFeature) msg.obj, msg.arg1);
                    return;
                case 101:
                    sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1);
                    return;
                case 102:
                    sendAuthenticatedSucceeded((FaceFeature) msg.obj, msg.arg1);
                    return;
                case 103:
                    sendAuthenticatedFailed();
                    return;
                case 104:
                    sendErrorResult(((Long) msg.obj).longValue(), msg.arg1);
                    return;
                case 105:
                    sendRemovedResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 106:
                    sendProgressResult(((Long) msg.obj).longValue(), msg.arg1);
                    return;
                case 1000:
                    sendCommandResult((CommandResult) msg.obj);
                    return;
                default:
                    return;
            }
        }

        private void sendCommandResult(CommandResult info) {
            if (FaceManager.this.mCommandResultCallback != null) {
                FaceManager.this.mCommandResultCallback.onCommandResult(info);
            }
        }

        private void sendRemovedResult(long deviceId, int faceFeatureId, int groupId) {
            if (FaceManager.this.mRemovalCallback != null) {
                int reqFaceId = FaceManager.this.mRemovalFaceFeature.getFaceFeatureId();
                int reqGroupId = FaceManager.this.mRemovalFaceFeature.getGroupId();
                if (reqFaceId != 0 && faceFeatureId != 0 && faceFeatureId != reqFaceId) {
                    Log.w(FaceManager.TAG, "faceFeature id didn't match: " + faceFeatureId + " != " + reqFaceId);
                } else if (groupId != reqGroupId) {
                    Log.w(FaceManager.TAG, "Group id didn't match: " + groupId + " != " + reqGroupId);
                } else {
                    if (FaceManager.DEBUG) {
                        Log.d(FaceManager.TAG, "onRemovalSucceeded");
                    }
                    FaceManager.this.mRemovalCallback.onRemovalSucceeded(new FaceFeature(null, groupId, faceFeatureId, deviceId));
                }
            }
        }

        private void sendErrorResult(long deviceId, int errMsgId) {
            if (FaceManager.DEBUG) {
                Log.d(FaceManager.TAG, "sendErrorResult, errMsgId = " + FaceManager.this.errMsgIdToString(errMsgId));
            }
            if (FaceManager.this.mEnrollmentCallback != null) {
                FaceManager.this.mEnrollmentCallback.onEnrollmentError(errMsgId, FaceManager.this.getErrorString(errMsgId));
            } else if (FaceManager.this.mAuthenticationCallback != null) {
                FaceManager.this.mAuthenticationCallback.onAuthenticationError(errMsgId, FaceManager.this.getErrorString(errMsgId));
            } else if (FaceManager.this.mRemovalCallback != null) {
                FaceManager.this.mRemovalCallback.onRemovalError(FaceManager.this.mRemovalFaceFeature, errMsgId, FaceManager.this.getErrorString(errMsgId));
            }
        }

        private void sendEnrollResult(FaceFeature faceFeature, int remaining) {
            if (FaceManager.DEBUG) {
                Log.d(FaceManager.TAG, "sendEnrollResult, faceFeature = " + faceFeature + " , remaining = " + remaining);
            }
            if (FaceManager.this.mEnrollmentCallback != null) {
                FaceManager.this.mEnrollmentCallback.onEnrollmentProgress(remaining);
            }
        }

        private void sendAuthenticatedSucceeded(FaceFeature faceFeature, int userId) {
            if (FaceManager.DEBUG) {
                Log.d(FaceManager.TAG, "sendAuthenticatedSucceeded");
            }
            if (FaceManager.this.mAuthenticationCallback != null) {
                FaceManager.this.mAuthenticationCallback.onAuthenticationSucceeded(new AuthenticationResult(FaceManager.this.mCryptoObject, faceFeature, userId));
            }
        }

        private void sendAuthenticatedFailed() {
            if (FaceManager.DEBUG) {
                Log.d(FaceManager.TAG, "sendAuthenticatedFailed");
            }
            if (FaceManager.this.mAuthenticationCallback != null) {
                FaceManager.this.mAuthenticationCallback.onAuthenticationFailed();
            }
        }

        private void sendAcquiredResult(long deviceId, int acquireInfo) {
            if (FaceManager.DEBUG) {
                Log.d(FaceManager.TAG, "sendAcquiredResult, acquireInfo = " + FaceManager.this.acquireInfoToString(acquireInfo));
            }
            if (FaceManager.this.mAuthenticationCallback != null) {
                FaceManager.this.mAuthenticationCallback.onAuthenticationAcquired(acquireInfo);
            }
            String msg = FaceManager.this.getAcquiredString(acquireInfo);
            if (FaceManager.this.mEnrollmentCallback != null) {
                FaceManager.this.mEnrollmentCallback.onEnrollmentHelp(acquireInfo, msg);
            } else if (FaceManager.this.mAuthenticationCallback != null) {
                FaceManager.this.mAuthenticationCallback.onAuthenticationHelp(acquireInfo, msg);
            }
        }

        private void sendProgressResult(long deviceId, int progressInfo) {
            if (FaceManager.DEBUG) {
                Log.d(FaceManager.TAG, "sendProgressResult");
            }
            if (FaceManager.this.mEnrollmentCallback != null) {
                FaceManager.this.mEnrollmentCallback.onProgressChanged(progressInfo);
            } else if (FaceManager.this.mAuthenticationCallback != null) {
                FaceManager.this.mAuthenticationCallback.onProgressChanged(progressInfo);
            }
        }
    }

    private class OnAuthenticationCancelListener implements OnCancelListener {
        private CryptoObject mCrypto;

        public OnAuthenticationCancelListener(CryptoObject crypto) {
            this.mCrypto = crypto;
        }

        public void onCancel() {
            FaceManager.this.cancelAuthentication(this.mCrypto);
        }
    }

    private class OnEnrollCancelListener implements OnCancelListener {
        /* synthetic */ OnEnrollCancelListener(FaceManager this$0, OnEnrollCancelListener -this1) {
            this();
        }

        private OnEnrollCancelListener() {
        }

        public void onCancel() {
            FaceManager.this.cancelEnrollment();
        }
    }

    public static abstract class RemovalCallback {
        public void onRemovalError(FaceFeature faceFeature, int errMsgId, CharSequence errString) {
        }

        public void onRemovalSucceeded(FaceFeature faceFeature) {
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler, int type) {
        authenticate(crypto, cancel, flags, callback, handler, UserHandle.myUserId(), type);
    }

    private void useHandler(Handler handler) {
        if (handler != null) {
            this.mHandler = new MyHandler(this, handler.getLooper(), null);
        } else if (this.mHandler.getLooper() != this.mContext.getMainLooper()) {
            this.mHandler = new MyHandler(this, this.mContext.getMainLooper(), null);
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler, int userId, int type) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an authentication callback");
        }
        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "authentication already canceled");
                return;
            }
            cancel.setOnCancelListener(new OnAuthenticationCancelListener(crypto));
        }
        if (this.mService != null) {
            try {
                useHandler(handler);
                this.mAuthenticationCallback = callback;
                this.mCryptoObject = crypto;
                this.mService.authenticate(this.mToken, crypto != null ? crypto.getOpId() : 0, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName(), type);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while authenticating: ", e);
                if (callback != null) {
                    callback.onAuthenticationError(1, getErrorString(1));
                }
            }
        }
    }

    public void enroll(byte[] token, CancellationSignal cancel, int flags, int userId, EnrollmentCallback callback) {
        if (userId == -2) {
            userId = getCurrentUserId();
        }
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an enrollment callback");
        }
        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "enrollment already canceled");
                return;
            }
            cancel.setOnCancelListener(new OnEnrollCancelListener(this, null));
        }
        if (this.mService != null) {
            try {
                this.mEnrollmentCallback = callback;
                this.mService.enroll(this.mToken, token, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in enroll: ", e);
                if (callback != null) {
                    callback.onEnrollmentError(1, getErrorString(1));
                }
            }
        }
    }

    public long preEnroll() {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.preEnroll(this.mToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int postEnroll() {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.postEnroll(this.mToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setActiveUser(int userId) {
        if (this.mService != null) {
            try {
                this.mService.setActiveUser(userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void remove(FaceFeature faceFeature, int userId, RemovalCallback callback) {
        if (this.mService != null) {
            try {
                this.mRemovalCallback = callback;
                this.mRemovalFaceFeature = faceFeature;
                this.mService.remove(this.mToken, faceFeature.getFaceFeatureId(), faceFeature.getGroupId(), userId, this.mServiceReceiver, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in remove: ", e);
                if (callback != null) {
                    callback.onRemovalError(faceFeature, 1, getErrorString(1));
                }
            }
        }
    }

    public void rename(int faceFeatureId, int userId, String newName) {
        if (this.mService != null) {
            try {
                this.mService.rename(faceFeatureId, userId, newName);
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "rename(): Service not connected!");
    }

    public List<FaceFeature> getEnrolledFaces(int userId) {
        if (this.mService == null) {
            return null;
        }
        try {
            return this.mService.getEnrolledFaces(userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void notifyFirstUnlockWhenBoot(boolean isFirstUnlockInPasswordOnlyMode) {
        if (this.mService != null) {
            try {
                this.mService.notifyFirstUnlockWhenBoot(isFirstUnlockInPasswordOnlyMode);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public List<FaceFeature> getEnrolledFaces() {
        return getEnrolledFaces(UserHandle.myUserId());
    }

    public boolean hasEnrolledFaces() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasEnrolledFaces(UserHandle.myUserId(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasEnrolledFaces(int userId) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasEnrolledFaces(userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isHardwareDetected() {
        if (this.mService != null) {
            try {
                return this.mService.isHardwareDetected(0, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "isFaceHardwareDetected(): Service not connected!");
        return false;
    }

    public long getAuthenticatorId() {
        if (this.mService != null) {
            try {
                return this.mService.getAuthenticatorId(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "getAuthenticatorId(): Service not connected!");
        return 0;
    }

    public void resetTimeout(byte[] token) {
        if (this.mService != null) {
            try {
                this.mService.resetTimeout(token);
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "resetTimeout(): Service not connected!");
    }

    public void addLockoutResetCallback(final LockoutResetCallback callback) {
        if (this.mService != null) {
            try {
                final PowerManager powerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
                this.mService.addLockoutResetCallback(new IFaceServiceLockoutResetCallback.Stub() {
                    public void onLockoutReset(long deviceId) throws RemoteException {
                        final WakeLock wakeLock = powerManager.newWakeLock(1, "lockoutResetCallback");
                        wakeLock.acquire();
                        Handler -get5 = FaceManager.this.mHandler;
                        final LockoutResetCallback lockoutResetCallback = callback;
                        -get5.post(new Runnable() {
                            public void run() {
                                try {
                                    lockoutResetCallback.onLockoutReset();
                                } finally {
                                    wakeLock.release();
                                }
                            }
                        });
                    }
                });
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        Log.w(TAG, "addLockoutResetCallback(): Service not connected!");
    }

    public void setPreviewFrame(Rect rect) {
        if (this.mService != null) {
            try {
                this.mService.setPreviewFrame(this.mToken, rect);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in setPreviewFrame: ", e);
            }
        }
    }

    public int setPreviewSurface(Surface surface) {
        if (this.mService == null) {
            return -1;
        }
        try {
            return this.mService.setPreviewSurface(this.mToken, surface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPreviewWidth() {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPreviewWidth();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPreviewHeight() {
        if (this.mService == null) {
            return 0;
        }
        try {
            return this.mService.getPreviewHeight();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int get(int type) {
        if (this.mService != null) {
            try {
                return this.mService.get(this.mToken, type, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in get: ", e);
            }
        }
        return -1;
    }

    public int executeCommand(CommandResultCallback callback, int type) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an executeCommand callback");
        } else if (this.mService != null) {
            try {
                this.mCommandResultCallback = callback;
                return this.mService.executeCommand(this.mToken, this.mContext.getOpPackageName(), UserHandle.myUserId(), this.mServiceReceiver, type);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in executeCommand: ", e);
                return -1;
            }
        } else {
            Log.w(TAG, "executeCommand: Service not connected!");
            return -1;
        }
    }

    public void cancelCommand(int type) {
        if (this.mService != null) {
            try {
                this.mCommandResultCallback = null;
                this.mService.cancelCommand(this.mToken, this.mContext.getOpPackageName(), type);
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in cancelCommand(): ", e);
                return;
            }
        }
        Log.w(TAG, "cancelCommand(): Service not connected!");
    }

    public long getLockoutAttemptDeadline() {
        if (this.mService != null) {
            try {
                return this.mService.getLockoutAttemptDeadline(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getLockoutAttemptDeadline(): ", e);
            }
        } else {
            Log.w(TAG, "getLockoutAttemptDeadline(): Service not connected!");
            return -1;
        }
    }

    public int getFailedAttempts() {
        if (this.mService != null) {
            try {
                return this.mService.getFailedAttempts(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getFailedAttempts(): ", e);
            }
        } else {
            Log.w(TAG, "getFailedAttempts(): Service not connected!");
            return -1;
        }
    }

    public int getEnrollmentTotalTimes() {
        int result = 0;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.getEnrollmentTotalTimes(this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in getEnrollmentTotalTimes: ", e);
            return result;
        }
    }

    public void notifyStopAuthenticationWhenWakeup(String reason) {
        if (this.mService != null) {
            try {
                this.mService.notifyStopAuthenticationWhenWakeup(reason);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in notifyStopAuthenticationWhenWakeup: ", e);
            }
        }
    }

    private String errMsgIdToString(int errMsgId) {
        switch (errMsgId) {
            case 1:
                return "FACE_ERROR_HW_UNAVAILABLE";
            case 2:
                return "FACE_ERROR_UNABLE_TO_PROCESS";
            case 3:
                return "FACE_ERROR_TIMEOUT";
            case 4:
                return "FACE_ERROR_NO_SPACE";
            case 5:
                return "FACE_ERROR_CANCELED";
            case 6:
                return "FACE_ERROR_UNABLE_TO_REMOVE";
            case 7:
                return "FACE_ERROR_LOCKOUT";
            case 8:
                return "FACE_ERROR_CAMERA_UNAVAILABLE";
            case 1000:
                return "FACE_ERROR_VENDOR_BASE";
            default:
                return "Unknown-Code";
        }
    }

    private String acquireInfoToString(int acquireInfo) {
        switch (acquireInfo) {
            case 0:
                return "FACE_ACQUIRED_GOOD";
            case 1:
                return "FACE_ACQUIRED_PARTIAL";
            case 2:
                return "FACE_ACQUIRED_INSUFFICIENT";
            case 3:
                return "FACE_ACQUIRED_IMAGER_DIRTY";
            case 4:
                return "FACE_ACQUIRED_TOO_SLOW";
            case 5:
                return "FACE_ACQUIRED_TOO_FAST";
            case 6:
                return "FACE_ACQUIRED_FAR_FACE";
            case 7:
                return "FACE_ACQUIRED_NEAR_FACE";
            case 101:
                return "FACE_ACQUIRED_NO_FACE";
            case 102:
                return "FACE_ACQUIRED_SHIFTING";
            case 103:
                return "FACE_ACQUIRED_DARK";
            case 104:
                return "FACE_ACQUIRED_HACKER";
            case 105:
                return "FACE_ACQUIRED_LOW_SIMILARITY";
            case 111:
                return "FACE_WITH_EYES_CLOSED";
            case 1000:
                return "FACE_ACQUIRED_VENDOR_BASE";
            case 1001:
                return "FACE_ACQUIRED_CAMERA_PREVIEW";
            default:
                return "Unknown-Code";
        }
    }

    public FaceManager(Context context, IFaceService service) {
        this.mContext = context;
        this.mService = service;
        if (this.mService == null) {
            Slog.v(TAG, "FaceManagerService was null");
        }
        this.mHandler = new MyHandler(this, context, null);
    }

    private int getCurrentUserId() {
        try {
            return ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void cancelEnrollment() {
        if (DEBUG) {
            Log.d(TAG, "cancelEnrollment", new Throwable("Kevin_DEBUG"));
        }
        if (this.mService != null) {
            try {
                this.mService.cancelEnrollment(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private void cancelAuthentication(CryptoObject cryptoObject) {
        if (this.mService != null) {
            try {
                this.mService.cancelAuthentication(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private String getErrorString(int errMsg) {
        return null;
    }

    private String getAcquiredString(int acquireInfo) {
        return null;
    }
}
