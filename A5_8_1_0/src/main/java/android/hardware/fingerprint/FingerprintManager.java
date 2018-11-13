package android.hardware.fingerprint;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.fingerprint.IFingerprintServiceReceiver.Stub;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.IRemoteCallback;
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
import java.security.Signature;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class FingerprintManager {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final int FINGERPRINT_ACQUIRED_ALREADY_ENROLLED = 1002;
    public static final int FINGERPRINT_ACQUIRED_GOOD = 0;
    public static final int FINGERPRINT_ACQUIRED_IMAGER_DIRTY = 3;
    public static final int FINGERPRINT_ACQUIRED_INSUFFICIENT = 2;
    public static final int FINGERPRINT_ACQUIRED_PARTIAL = 1;
    public static final int FINGERPRINT_ACQUIRED_TOO_FAST = 5;
    public static final int FINGERPRINT_ACQUIRED_TOO_SIMILAR = 1001;
    public static final int FINGERPRINT_ACQUIRED_TOO_SLOW = 4;
    public static final int FINGERPRINT_ACQUIRED_VENDOR = 6;
    public static final int FINGERPRINT_ACQUIRED_VENDOR_BASE = 1000;
    public static final int FINGERPRINT_ERROR_CANCELED = 5;
    public static final int FINGERPRINT_ERROR_HW_UNAVAILABLE = 1;
    public static final int FINGERPRINT_ERROR_LOCKOUT = 7;
    public static final int FINGERPRINT_ERROR_LOCKOUT_PERMANENT = 9;
    public static final int FINGERPRINT_ERROR_NO_SPACE = 4;
    public static final int FINGERPRINT_ERROR_TIMEOUT = 3;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_PROCESS = 2;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_REMOVE = 6;
    public static final int FINGERPRINT_ERROR_USER_CANCELED = 10;
    public static final int FINGERPRINT_ERROR_VENDOR = 8;
    public static final int FINGERPRINT_ERROR_VENDOR_BASE = 1000;
    public static final int FINGERPRINT_MONITOR_TYPE_ERROR = 1;
    public static final int FINGERPRINT_MONITOR_TYPE_POWER = 0;
    public static final int FINGERPRINT_MONITOR_TYPE_TP_PROTECT = 2;
    public static final int FINGERPRINT_SCREENOFF_CANCELED = 11;
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
    private IFingerprintServiceReceiver mServiceReceiver = new Stub() {
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintManager.this.mHandler.obtainMessage(100, remaining, 0, new Fingerprint(null, groupId, fingerId, deviceId)).sendToTarget();
        }

        public void onAcquired(long deviceId, int acquireInfo, int vendorCode) {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "onAcquired");
            }
            FingerprintManager.this.mHandler.obtainMessage(101, acquireInfo, vendorCode, Long.valueOf(deviceId)).sendToTarget();
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "onAcquired finished");
            }
        }

        public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "onAuthenticationSucceeded");
            }
            FingerprintManager.this.mHandler.obtainMessage(102, userId, 0, fp).sendToTarget();
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "onAuthenticationSucceeded finished");
            }
        }

        public void onAuthenticationFailed(long deviceId) {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "onAuthenticationFailed");
            }
            FingerprintManager.this.mHandler.obtainMessage(103).sendToTarget();
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "onAuthenticationFailed finished");
            }
        }

        public void onError(long deviceId, int error, int vendorCode) {
            FingerprintManager.this.mHandler.obtainMessage(104, error, vendorCode, Long.valueOf(deviceId)).sendToTarget();
        }

        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "onRemoved");
            }
            FingerprintManager.this.mHandler.obtainMessage(105, remaining, 0, new Fingerprint(null, groupId, fingerId, deviceId)).sendToTarget();
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "onRemoved finished");
            }
        }

        public void onEngineeringInfoUpdated(EngineeringInfo info) {
            FingerprintManager.this.mHandler.obtainMessage(1005, 0, 0, info).sendToTarget();
        }

        public void onTouchDown(long deviceId) {
            FingerprintManager.this.mHandler.obtainMessage(1001, 0, 0, Long.valueOf(deviceId)).sendToTarget();
        }

        public void onTouchUp(long deviceId) {
            FingerprintManager.this.mHandler.obtainMessage(1002, 0, 0, Long.valueOf(deviceId)).sendToTarget();
        }

        public void onMonitorEventTriggered(int type, String data) {
            FingerprintManager.this.mHandler.obtainMessage(1003, 0, type, data).sendToTarget();
        }

        public void onImageInfoAcquired(int type, int quality, int matchScore) {
            FingerprintManager.this.mHandler.obtainMessage(1004, 0, 0, new FingerprintImageInfo(type, quality, matchScore)).sendToTarget();
        }

        public void onEnumerated(long deviceId, int fingerId, int groupId, int remaining) {
            FingerprintManager.this.mHandler.obtainMessage(106, fingerId, groupId, Long.valueOf(deviceId)).sendToTarget();
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

        public void onImageInfoAcquired(FingerprintImageInfo info) {
        }

        public void onTouchDown() {
        }
    }

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

        public Fingerprint getFingerprint() {
            return this.mFingerprint;
        }

        public int getUserId() {
            return this.mUserId;
        }
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

    public interface EngineeringInfoCallback {
        void onEngineeringInfoUpdated(EngineeringInfo engineeringInfo);

        void onError(int i, CharSequence charSequence);
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

    public static abstract class EnumerateCallback {
        public void onEnumerateError(int errMsgId, CharSequence errString) {
        }

        public void onEnumerate(Fingerprint fingerprint) {
        }
    }

    public class FingerprintImageInfo {
        public int mQuality;
        public int mScore;
        public int mType;

        public FingerprintImageInfo(int type, int quality, int matchScore) {
            this.mType = type;
            this.mQuality = quality;
            this.mScore = matchScore;
        }
    }

    public static abstract class FingerprintInputCallback {
        public void onTouchDown() {
        }
    }

    public static abstract class LockoutResetCallback {
        public void onLockoutReset() {
        }
    }

    public static abstract class MonitorEventCallback {
        public void onMonitorEventTriggered(int type, String data) {
        }
    }

    private class MyHandler extends Handler {
        /* synthetic */ MyHandler(FingerprintManager this$0, Looper looper, Callback callback, boolean async, MyHandler -this4) {
            this(looper, callback, async);
        }

        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        private MyHandler(Looper looper, Callback callback, boolean async) {
            super(looper, callback, async);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    sendEnrollResult((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 101:
                    sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 102:
                    sendAuthenticatedSucceeded((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 103:
                    sendAuthenticatedFailed();
                    return;
                case 104:
                    sendErrorResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 105:
                    Fingerprint fingerprint = msg.obj;
                    sendRemovedResult(fingerprint.getDeviceId(), fingerprint.getFingerId(), fingerprint.getGroupId(), msg.arg1);
                    return;
                case 106:
                    sendEnumeratedResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 1001:
                    sendTouchDownEvent(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 1002:
                    sendTouchUpEvent(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 1003:
                    sendMonitorEventTriggered(msg.arg2, (String) msg.obj);
                    return;
                case 1004:
                    sendImageInfo((FingerprintImageInfo) msg.obj);
                    return;
                case 1005:
                    sendEngineeringInfo((EngineeringInfo) msg.obj);
                    return;
                default:
                    return;
            }
        }

        private void sendEngineeringInfo(EngineeringInfo info) {
            if (FingerprintManager.this.mEngineeringInfoCallback != null) {
                FingerprintManager.this.mEngineeringInfoCallback.onEngineeringInfoUpdated(info);
            }
        }

        private void sendTouchDownEvent(long deviceId, int fingerId, int groupId) {
            if (FingerprintManager.this.mFingerprintInputCallback != null) {
                FingerprintManager.this.mFingerprintInputCallback.onTouchDown();
            }
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onTouchDown();
            }
        }

        private void sendTouchUpEvent(long deviceId, int fingerId, int groupId) {
            if (FingerprintManager.this.mEnrollmentCallback != null) {
                FingerprintManager.this.mEnrollmentCallback.onTouchUp();
            }
        }

        private void sendMonitorEventTriggered(int type, String data) {
            if (FingerprintManager.this.mMonitorEventCallback != null) {
                FingerprintManager.this.mMonitorEventCallback.onMonitorEventTriggered(type, data);
            }
        }

        private void sendImageInfo(FingerprintImageInfo info) {
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onImageInfoAcquired(info);
            }
        }

        private void sendRemovedResult(long deviceId, int fingerId, int groupId, int remaining) {
            if (FingerprintManager.this.mRemovalCallback != null) {
                int reqFingerId = FingerprintManager.this.mRemovalFingerprint.getFingerId();
                int reqGroupId = FingerprintManager.this.mRemovalFingerprint.getGroupId();
                if (reqFingerId != 0 && fingerId != 0 && fingerId != reqFingerId) {
                    Log.w(FingerprintManager.TAG, "Finger id didn't match: " + fingerId + " != " + reqFingerId);
                } else if (groupId != reqGroupId) {
                    Log.w(FingerprintManager.TAG, "Group id didn't match: " + groupId + " != " + reqGroupId);
                } else {
                    if (FingerprintManager.DEBUG) {
                        Log.d(FingerprintManager.TAG, "onRemovalSucceeded");
                    }
                    FingerprintManager.this.mRemovalCallback.onRemovalSucceeded(new Fingerprint(null, groupId, fingerId, deviceId), remaining);
                }
            }
        }

        private void sendEnumeratedResult(long deviceId, int fingerId, int groupId) {
            if (FingerprintManager.this.mEnumerateCallback != null) {
                FingerprintManager.this.mEnumerateCallback.onEnumerate(new Fingerprint(null, groupId, fingerId, deviceId));
            }
        }

        private void sendErrorResult(long deviceId, int errMsgId, int vendorCode) {
            int clientErrMsgId = errMsgId == 8 ? vendorCode + 1000 : errMsgId;
            if (FingerprintManager.this.mEnrollmentCallback != null) {
                FingerprintManager.this.mEnrollmentCallback.onEnrollmentError(errMsgId, FingerprintManager.this.getErrorString(errMsgId, vendorCode));
            } else if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationError(errMsgId, FingerprintManager.this.getErrorString(errMsgId, vendorCode));
            } else if (FingerprintManager.this.mRemovalCallback != null) {
                FingerprintManager.this.mRemovalCallback.onRemovalError(FingerprintManager.this.mRemovalFingerprint, errMsgId, FingerprintManager.this.getErrorString(errMsgId, vendorCode));
            } else if (FingerprintManager.this.mEnumerateCallback != null) {
                FingerprintManager.this.mEnumerateCallback.onEnumerateError(clientErrMsgId, FingerprintManager.this.getErrorString(errMsgId, vendorCode));
            }
        }

        private void sendEnrollResult(Fingerprint fp, int remaining) {
            if (FingerprintManager.this.mEnrollmentCallback != null) {
                FingerprintManager.this.mEnrollmentCallback.onEnrollmentProgress(remaining);
            }
        }

        private void sendAuthenticatedSucceeded(Fingerprint fp, int userId) {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "sendAuthenticatedSucceeded");
            }
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationSucceeded(new AuthenticationResult(FingerprintManager.this.mCryptoObject, fp, userId));
            }
        }

        private void sendAuthenticatedFailed() {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "sendAuthenticatedFailed");
            }
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationFailed();
            }
        }

        private void sendAcquiredResult(long deviceId, int acquireInfo, int vendorCode) {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "sendAcquiredResult");
            }
            if (FingerprintManager.this.mAuthenticationCallback != null) {
                FingerprintManager.this.mAuthenticationCallback.onAuthenticationAcquired(acquireInfo);
            }
            String msg = FingerprintManager.this.getAcquiredString(acquireInfo, vendorCode);
            if (msg != null) {
                int clientInfo = acquireInfo == 6 ? vendorCode + 1000 : acquireInfo;
                if (FingerprintManager.this.mEnrollmentCallback != null) {
                    FingerprintManager.this.mEnrollmentCallback.onEnrollmentHelp(clientInfo, msg);
                } else if (FingerprintManager.this.mAuthenticationCallback != null) {
                    FingerprintManager.this.mAuthenticationCallback.onAuthenticationHelp(clientInfo, msg);
                }
            }
        }
    }

    private class OnAuthenticationCancelListener implements OnCancelListener {
        private CryptoObject mCrypto;

        public OnAuthenticationCancelListener(CryptoObject crypto) {
            this.mCrypto = crypto;
        }

        public void onCancel() {
            FingerprintManager.this.cancelAuthentication(this.mCrypto);
        }
    }

    private class OnEnrollCancelListener implements OnCancelListener {
        /* synthetic */ OnEnrollCancelListener(FingerprintManager this$0, OnEnrollCancelListener -this1) {
            this();
        }

        private OnEnrollCancelListener() {
        }

        public void onCancel() {
            FingerprintManager.this.cancelEnrollment();
        }
    }

    private class OnTouchEventMonitorCancelListener implements OnCancelListener {
        /* synthetic */ OnTouchEventMonitorCancelListener(FingerprintManager this$0, OnTouchEventMonitorCancelListener -this1) {
            this();
        }

        private OnTouchEventMonitorCancelListener() {
        }

        public void onCancel() {
            FingerprintManager.this.cancelTouchEventListener();
        }
    }

    public static abstract class RemovalCallback {
        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
        }

        public void onRemovalSucceeded(Fingerprint fingerprint, int remaining) {
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler) {
        authenticate(crypto, cancel, flags, callback, handler, UserHandle.myUserId());
    }

    private void useHandler(Handler handler) {
        if (KEYGUARD_PACKAGENAME.equals(this.mContext.getOpPackageName())) {
            if (handler != null) {
                if (DEBUG) {
                    Log.d(TAG, "keyguard Handler");
                }
                this.mHandler = new MyHandler(this, handler.getLooper(), null, true, null);
            } else {
                if (DEBUG) {
                    Log.d(TAG, "new Handler for keyguard");
                }
                this.mHandler = new MyHandler(this, this.mContext.getMainLooper(), null, true, null);
            }
            return;
        }
        if (handler != null) {
            this.mHandler = new MyHandler(this, handler.getLooper(), null);
        } else if (this.mHandler.getLooper() != this.mContext.getMainLooper()) {
            this.mHandler = new MyHandler(this, this.mContext.getMainLooper(), null);
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler, int userId) {
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
                this.mFingerprintInputCallback = null;
                this.mAuthenticationCallback = callback;
                this.mCryptoObject = crypto;
                this.mService.authenticate(this.mToken, crypto != null ? crypto.getOpId() : 0, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while authenticating: ", e);
                if (callback != null) {
                    callback.onAuthenticationError(1, getErrorString(1, 0));
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
                    callback.onEnrollmentError(1, getErrorString(1, 0));
                }
            }
        }
    }

    public long preEnroll() {
        long result = 0;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.preEnroll(this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in enroll: ", e);
            return result;
        }
    }

    public boolean pauseEnroll() {
        int result = 0;
        if (this.mService != null) {
            try {
                result = this.mService.pauseEnroll();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in pauseEnroll: ", e);
            }
        }
        if (result < 0) {
            return false;
        }
        return true;
    }

    public boolean continueEnroll() {
        int result = 0;
        if (this.mService != null) {
            try {
                result = this.mService.continueEnroll();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in continueEnroll: ", e);
            }
        }
        if (result < 0) {
            return false;
        }
        return true;
    }

    public int postEnroll() {
        int result = 0;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.postEnroll(this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in post enroll: ", e);
            return result;
        }
    }

    public void setActiveUser(int userId) {
        if (this.mService != null) {
            try {
                this.mService.setActiveUser(userId);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in setActiveUser: ", e);
            }
        }
    }

    public void remove(Fingerprint fp, int userId, RemovalCallback callback) {
        if (this.mService != null) {
            try {
                this.mRemovalCallback = callback;
                this.mRemovalFingerprint = fp;
                this.mService.remove(this.mToken, fp.getFingerId(), fp.getGroupId(), userId, this.mServiceReceiver, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in remove: ", e);
                if (callback != null) {
                    callback.onRemovalError(fp, 1, getErrorString(1, 0));
                }
            }
        }
    }

    public void enumerate(int userId, EnumerateCallback callback) {
        if (this.mService != null) {
            try {
                this.mEnumerateCallback = callback;
                this.mService.enumerate(this.mToken, userId, this.mServiceReceiver, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in enumerate: ", e);
                if (callback != null) {
                    callback.onEnumerateError(1, getErrorString(1, 0));
                }
            }
        }
    }

    public void rename(int fpId, int userId, String newName) {
        if (this.mService != null) {
            try {
                this.mService.rename(fpId, userId, newName);
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in rename(): ", e);
                return;
            }
        }
        Log.w(TAG, "rename(): Service not connected!");
    }

    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        if (this.mService != null) {
            try {
                return this.mService.getEnrolledFingerprints(userId, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getEnrolledFingerprints: ", e);
            }
        }
        return null;
    }

    public List<Fingerprint> getEnrolledFingerprints() {
        return getEnrolledFingerprints(UserHandle.myUserId());
    }

    public boolean hasEnrolledFingerprints() {
        if (this.mService != null) {
            try {
                return this.mService.hasEnrolledFingerprints(UserHandle.myUserId(), this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in hasEnrolledFingerprints: ", e);
            }
        }
        return false;
    }

    public boolean hasEnrolledFingerprints(int userId) {
        if (this.mService != null) {
            try {
                return this.mService.hasEnrolledFingerprints(userId, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in hasEnrolledFingerprints: ", e);
            }
        }
        return false;
    }

    public boolean isHardwareDetected() {
        if (this.mService != null) {
            try {
                return this.mService.isHardwareDetected(0, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in isFingerprintHardwareDetected(): ", e);
            }
        } else {
            Log.w(TAG, "isFingerprintHardwareDetected(): Service not connected!");
            return false;
        }
    }

    public long getAuthenticatorId() {
        if (this.mService != null) {
            try {
                return this.mService.getAuthenticatorId(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getAuthenticatorId(): ", e);
            }
        } else {
            Log.w(TAG, "getAuthenticatorId(): Service not connected!");
            return 0;
        }
    }

    public void resetTimeout(byte[] token) {
        if (this.mService != null) {
            try {
                Log.w(TAG, "resetTimeout, packageName = " + this.mContext.getOpPackageName());
                this.mService.resetTimeout(token);
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in resetTimeout(): ", e);
                return;
            }
        }
        Log.w(TAG, "resetTimeout(): Service not connected!");
    }

    public void addLockoutResetCallback(final LockoutResetCallback callback) {
        if (this.mService != null) {
            try {
                final PowerManager powerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
                this.mService.addLockoutResetCallback(new IFingerprintServiceLockoutResetCallback.Stub() {
                    public void onLockoutReset(long deviceId, IRemoteCallback serverCallback) throws RemoteException {
                        try {
                            final WakeLock wakeLock = powerManager.newWakeLock(1, "lockoutResetCallback");
                            wakeLock.acquire();
                            Handler -get7 = FingerprintManager.this.mHandler;
                            final LockoutResetCallback lockoutResetCallback = callback;
                            -get7.post(new Runnable() {
                                public void run() {
                                    try {
                                        lockoutResetCallback.onLockoutReset();
                                    } finally {
                                        wakeLock.release();
                                    }
                                }
                            });
                        } finally {
                            serverCallback.sendResult(null);
                        }
                    }
                });
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in addLockoutResetCallback(): ", e);
                return;
            }
        }
        Log.w(TAG, "addLockoutResetCallback(): Service not connected!");
    }

    public int getEnrollmentTotalTimes() {
        int result = 0;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.getEnrollmentTotalTimes(this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in enroll: ", e);
            return result;
        }
    }

    public void setTouchEventListener(FingerprintInputCallback callback, CancellationSignal cancel) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an setTouchEventListener callback");
        }
        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "setTouchEventListener already canceled");
                return;
            }
            cancel.setOnCancelListener(new OnTouchEventMonitorCancelListener(this, null));
        }
        if (this.mService != null) {
            this.mAuthenticationCallback = null;
            this.mFingerprintInputCallback = callback;
            try {
                this.mService.setTouchEventListener(this.mToken, this.mServiceReceiver, UserHandle.myUserId(), this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.e(TAG, "Remote exception in setTouchEventListener(): ", e);
            }
        }
    }

    private void cancelTouchEventListener() {
        if (this.mService != null) {
            try {
                this.mService.cancelTouchEventListener(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.w(TAG, "Remote exception while canceling touchevent");
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
        if (this.mService != null) {
            try {
                int result = this.mService.pauseIdentify(this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in pauseIdentify: ", e);
            }
        }
    }

    public void continueIdentify() {
        if (this.mService != null) {
            try {
                int result = this.mService.continueIdentify(this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in continueIdentify: ", e);
            }
        }
    }

    public void finishUnLockedScreen(boolean authenticated) {
        if (this.mService != null) {
            try {
                this.mService.finishUnLockedScreen(authenticated, this.mContext.getOpPackageName());
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "Remote exception in finishUnLockedScreen(): ", e);
                return;
            }
        }
        Log.w(TAG, "finishUnLockedScreen(): Service not connected!");
    }

    public int getAlikeyStatus() {
        if (this.mService != null) {
            try {
                return this.mService.getAlikeyStatus();
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getAlikeyStatus(): ", e);
            }
        } else {
            Log.w(TAG, "getAlikeyStatus(): Service not connected!");
            return -1;
        }
    }

    public int getEngineeringInfo(EngineeringInfoCallback callback, int type) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an getEngineeringInfo callback");
        } else if (this.mService != null) {
            try {
                this.mEngineeringInfoCallback = callback;
                return this.mService.getEngineeringInfo(this.mToken, this.mContext.getOpPackageName(), UserHandle.myUserId(), this.mServiceReceiver, type);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getEngineeringInfo(): ", e);
            }
        } else {
            Log.w(TAG, "getEngineeringInfo(): Service not connected!");
            return -1;
        }
    }

    public void cancelGetEngineeringInfo(int type) {
        if (this.mService != null) {
            try {
                this.mService.cancelGetEngineeringInfo(this.mToken, this.mContext.getOpPackageName(), type);
                this.mEngineeringInfoCallback = null;
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in cancelgetEngineeringInfo(): ", e);
                return;
            }
        }
        Log.w(TAG, "cancelgetEngineeringInfo(): Service not connected!");
    }

    public byte[] alipayInvokeCommand(byte[] inbuf) {
        byte[] outbuf = null;
        if (this.mService != null) {
            try {
                return this.mService.alipayInvokeCommand(inbuf);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in alipayInvokeCommand(): ", e);
                return outbuf;
            }
        }
        Log.w(TAG, "alipayInvokeCommand(): Service not connected!");
        return outbuf;
    }

    public FingerprintManager(Context context, IFingerprintService service) {
        this.mContext = context;
        this.mService = service;
        if (this.mService == null) {
            Slog.v(TAG, "FingerprintManagerService was null");
        }
        this.mHandler = new MyHandler(this, context, null);
    }

    private int getCurrentUserId() {
        try {
            return ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get current user id\n");
            return -10000;
        }
    }

    private void cancelEnrollment() {
        if (this.mService != null) {
            try {
                this.mService.cancelEnrollment(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.w(TAG, "Remote exception while canceling enrollment");
                }
            }
        }
    }

    private void cancelAuthentication(CryptoObject cryptoObject) {
        if (this.mService != null) {
            try {
                this.mService.cancelAuthentication(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.w(TAG, "Remote exception while canceling enrollment");
                }
            }
        }
    }

    private String getErrorString(int errMsg, int vendorCode) {
        switch (errMsg) {
            case 1:
                return this.mContext.getString(17039913);
            case 2:
                return this.mContext.getString(17039918);
            case 3:
                return this.mContext.getString(17039917);
            case 4:
                return this.mContext.getString(17039916);
            case 5:
                return this.mContext.getString(17039912);
            case 7:
                return this.mContext.getString(17039914);
            case 8:
                String[] msgArray = this.mContext.getResources().getStringArray(17236056);
                if (vendorCode < msgArray.length) {
                    return msgArray[vendorCode];
                }
                break;
            case 9:
                return this.mContext.getString(17039915);
            case 11:
                return "restart authenticate";
        }
        Slog.w(TAG, "Invalid error message: " + errMsg + ", " + vendorCode);
        return null;
    }

    private String getAcquiredString(int acquireInfo, int vendorCode) {
        switch (acquireInfo) {
            case 0:
                return null;
            case 1:
                return this.mContext.getString(17039909);
            case 2:
                return this.mContext.getString(17039908);
            case 3:
                return this.mContext.getString(17039907);
            case 4:
                return this.mContext.getString(17039911);
            case 5:
                return this.mContext.getString(17039910);
            case 6:
                String[] msgArray = this.mContext.getResources().getStringArray(17236055);
                if (vendorCode < msgArray.length) {
                    return msgArray[vendorCode];
                }
                break;
            case 1002:
                return "already enrolled finger";
        }
        Slog.w(TAG, "Invalid acquired message: " + acquireInfo + ", " + vendorCode);
        return null;
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
}
