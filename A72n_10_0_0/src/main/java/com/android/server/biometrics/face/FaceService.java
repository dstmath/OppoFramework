package com.android.server.biometrics.face;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.frameworks.faceservice.V1_0.IFaceHalService;
import android.graphics.Rect;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.face.ClientMode;
import android.hardware.face.Face;
import android.hardware.face.FaceManager;
import android.hardware.face.IFaceService;
import android.hardware.face.IFaceServiceReceiver;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.NativeHandle;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.proto.ProtoOutputStream;
import android.view.Surface;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.Watchdog;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.biometrics.AuthenticationClient;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.BiometricUtils;
import com.android.server.biometrics.BiometricWakeupManagerService;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.biometrics.ClientMonitor;
import com.android.server.biometrics.Constants;
import com.android.server.biometrics.EnrollClient;
import com.android.server.biometrics.EnumerateClient;
import com.android.server.biometrics.LoggableMonitor;
import com.android.server.biometrics.RemovalClient;
import com.android.server.biometrics.face.FaceService;
import com.android.server.biometrics.face.FaceSwitchHelper;
import com.android.server.biometrics.face.dcs.DcsUtil;
import com.android.server.biometrics.face.health.HealthMonitor;
import com.android.server.biometrics.face.health.HealthState;
import com.android.server.biometrics.face.power.FaceInternal;
import com.android.server.biometrics.face.power.FacePowerManager;
import com.android.server.biometrics.face.setting.FaceSettings;
import com.android.server.biometrics.face.setting.Ilistener;
import com.android.server.biometrics.face.setting.UnlockSettingMonitor;
import com.android.server.biometrics.face.tool.ExHandler;
import com.android.server.biometrics.face.utils.LogUtil;
import com.android.server.biometrics.face.utils.TimeUtils;
import com.android.server.biometrics.face.utils.Utils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.OppoLightsService;
import com.android.server.theia.NoFocusWindow;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFace;
import vendor.oppo.hardware.biometrics.face.V1_0.RectHal;

public class FaceService extends BiometricServiceBase implements IRecognitionCallback, FacePowerManager.IPowerCallback, FaceSwitchHelper.ISwitchUpdateListener, Watchdog.Monitor {
    private static final String ACTION_LOCKOUT_RESET = "com.android.server.biometrics.face.ACTION_LOCKOUT_RESET";
    private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
    private static final int CHALLENGE_TIMEOUT_SEC = 600;
    private static final boolean DEBUG = true;
    public static boolean DEBUG_PERF = false;
    private static final String FACE_DATA_DIR = "facedata";
    private static final long FAIL_LOCKOUT_TIMEOUT_MS = 30000;
    private static final String FEATURE_FACE_CLOSEEYE_DETECT = "oppo.face.closeeye.detect";
    private static final String FEATURE_FINGERPRINT_OPTICAL = "oppo.hardware.fingerprint.optical.support";
    private static final String FEATURE_MOTOR = "oppo.hardware.motor.support";
    private static final String FEATURE_NO_POWER_BLOCK = "oppo.hardware.face.nopowerblock";
    public static final boolean IS_REALEASE_VERSION = Utils.isReleaseVersion();
    private static final String KEY_LOCKOUT_RESET_USER = "lockout_reset_user";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int MSG_DAEMON_CALLBACK = 11;
    private static final int MSG_UNBLOCK_SCREEN_ON = 13;
    private static final int MSG_USER_SWITCHING = 10;
    private static final String PROP_FACE_FEEDBACK_REASON = "persist.sys.android.face.feedback.reason";
    private static final String PROP_FACE_PERFORMANCE_HIDE = "persist.sys.face.performance.hide";
    private static final String PROP_FACE_POWER_BLOCK = "persist.sys.android.face.power.block";
    public static final String TAG = "FaceService.Main";
    public static final String TAG_NAME = "FaceService";
    public static final long TIMEOUT_CAMERA_PREVIEW_FRAME_CHECK = 1000;
    public static final long TIMEOUT_FACED_BINDERCALL_CHECK = 5000;
    public static boolean mCanAddCameraMessageToQueue = false;
    public static boolean mCanDumpCameraMessageQueue = false;
    public static boolean mCanRemoveCameraMessageFromQueue = false;
    private final AlarmManager mAlarmManager;
    private BiometricsManagerInternal mBiometricManager;
    private String mBlockScreenOnSession;
    private final Object mClientLock = new Object();
    private final Context mContext;
    private int mCurrentUserLockoutMode;
    @GuardedBy({"this"})
    private IBiometricsFace mDaemon;
    private final ConcurrentLinkedDeque<Runnable> mDaemonCallbackQueue = new ConcurrentLinkedDeque<>();
    private BiometricServiceBase.DaemonWrapper mDaemonWrapper;
    private final DcsUtil mDcsUtil;
    private final Runnable mDecreaseFailedAttemptsRunnable;
    private DisplayManagerInternal mDisplayManagerInternal;
    private int mEnrollmentTotalTimes;
    private int mFaceCloseEyeDetect;
    private final boolean mFaceCloseEyeDetectSupport;
    private final FaceConstants mFaceConstants;
    private boolean mFaceFingerprintCombineUnlockEnabled;
    private final ServiceThread mFaceServiceUnblockThread;
    private final boolean mFaceSupport;
    private final FaceSwitchHelper mFaceSwitchHelper;
    private boolean mFaceUnlockAutoWhenScreenOn;
    private boolean mFaceUnlockEnabled;
    private final FaceUtils mFaceUtils;
    private SparseIntArray mFailedAttempts;
    private boolean mFingerPrintShowWhenScreenOff;
    private boolean mFingerPrintUnlockEnabled;
    private final boolean mFingerprintOpticalSupport;
    private ExHandler mHandleResultHandler;
    private final ServiceThread mHandleResultThread;
    private ExHandler mHandler;
    private final HealthMonitor mHealthMonitor;
    private boolean mIsImageSaveEnable;
    private boolean mIsLogOpened;
    private boolean mIsScreenOff;
    private boolean mIsWakeUp;
    private final KeyguardManager mKeyguardManager;
    private final Ilistener mListener;
    private SparseLongArray mLockDeadLineArray;
    private long mLockoutDeadline;
    private final Object mLockoutDeadlineLock = new Object();
    private final BroadcastReceiver mLockoutReceiver;
    private final boolean mMotorSupport;
    private PowerState mPowerState;
    private final Runnable mResetFailedAttemptsRunnable;
    private ServiceListenerImpl mServiceListenerImpl;
    private final ServiceThread mServiceThread;
    private ExHandler mSubHandler;
    private boolean mSupportPowerBlock;
    private final UnlockSettingMonitor mUnlockSettingMonitor;
    private String mWakeUpReason;

    /* access modifiers changed from: private */
    public enum PowerState {
        WAKE_HALF(0),
        WAKE_FULNESS(1),
        SLEEP_HALF(2),
        SLEEP_FULNESS(3);
        
        final int mState;

        private PowerState(int i) {
            this.mState = i;
        }

        public int getState() {
            return this.mState;
        }
    }

    private synchronized void dispatchScreenOff(boolean isScreenOff) {
        if (this.mIsScreenOff != isScreenOff) {
            LogUtil.d(TAG, "dispatchScreenOff mIsScreenOff = " + isScreenOff);
            this.mIsScreenOff = isScreenOff;
        }
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mServiceThread.getLooper()) {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass1 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int i = msg.what;
                if (i == 10) {
                    FaceService.this.handleUserSwitching(msg.arg1);
                } else if (i != 11) {
                    LogUtil.w(FaceService.TAG, "Unknown message:" + msg.what);
                } else {
                    while (true) {
                        Runnable r = (Runnable) FaceService.this.mDaemonCallbackQueue.poll();
                        if (r != null) {
                            r.run();
                        } else {
                            return;
                        }
                    }
                }
            }
        };
        this.mSubHandler = new ExHandler(this.mFaceServiceUnblockThread.getLooper()) {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass2 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what != 13) {
                    LogUtil.w(FaceService.TAG, "Unknown message of SubHandler:" + msg.what);
                    return;
                }
                FaceService.this.handleUnBlockScreenOn((String) msg.obj);
            }
        };
        this.mHandleResultHandler = new ExHandler(this.mHandleResultThread.getLooper()) {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass3 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what != 11) {
                    LogUtil.w(FaceService.TAG, "Unknown message of mHandleResultHandler:" + msg.what);
                    return;
                }
                while (true) {
                    Runnable r = (Runnable) FaceService.this.mDaemonCallbackQueue.poll();
                    if (r != null) {
                        r.run();
                    } else {
                        return;
                    }
                }
            }
        };
    }

    public class ResetRunnable implements Runnable {
        private Intent intent;

        public ResetRunnable(Intent intent2) {
            this.intent = intent2;
        }

        public void run() {
            FaceService.this.resetFailedAttemptsForUser(this.intent.getIntExtra(FaceService.KEY_LOCKOUT_RESET_USER, 0));
        }
    }

    /* access modifiers changed from: package-private */
    public class IfaceHalService extends IFaceHalService.Stub {
        IfaceHalService() {
        }

        @Override // android.frameworks.faceservice.V1_0.IFaceHalService
        public int notifyInitFinished(int pid) {
            LogUtil.d(FaceService.TAG, "notifyInitFinished pid = " + pid);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.IfaceHalService.AnonymousClass1 */

                public void run() {
                    if (FaceService.this.getFaceDaemon() == null) {
                        LogUtil.e(FaceService.TAG, "notifyInitFinished: no faced!");
                    }
                }
            });
            FaceService.this.mHealthMonitor.demoProcessSystemReady(pid);
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void handleProgressChanged(long deviceId, int progressInfo) {
        LogUtil.d(TAG, "handleProgressChanged progressInfo = " + progressInfo);
        ClientMonitor currentClient = getCurrentClient();
        synchronized (this.mClientLock) {
            if (currentClient != null) {
                if (((currentClient instanceof AuthenticationClient) || (currentClient instanceof EnrollClient)) && this.mServiceListenerImpl != null && this.mServiceListenerImpl.onProgressChanged(progressInfo)) {
                    cancelRecognition();
                    removeClient(currentClient);
                    this.mServiceListenerImpl = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean inLockoutMode() {
        if (this.mFailedAttempts.get(ActivityManager.getCurrentUser(), 0) >= 5) {
            return true;
        }
        return false;
    }

    private void cancelLockoutResetForUser(int userId) {
        this.mAlarmManager.cancel(getLockoutResetIntentForUser(userId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleLockoutResetForUser(int userId) {
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + 30000, getLockoutResetIntentForUser(userId));
    }

    private PendingIntent getLockoutResetIntentForUser(int userId) {
        return PendingIntent.getBroadcast(getContext(), userId, new Intent(getLockoutResetIntent()).putExtra(KEY_LOCKOUT_RESET_USER, userId), 134217728);
    }

    /* access modifiers changed from: protected */
    public void resetFailedAttemptsForUser(int userId) {
        LogUtil.d(TAG, "resetFailedAttemptsForUser");
        if (inLockoutMode()) {
            LogUtil.d(TAG, "Reset face lockout");
        }
        this.mFailedAttempts.put(userId, 0);
        cancelLockoutResetForUser(userId);
        notifyLockoutResetMonitors();
        setLockoutAttemptDeadline(0, userId);
        LogUtil.d(TAG, "resetFailedAttempts finish");
    }

    /* access modifiers changed from: protected */
    public void decreaseFailedAttemptsForUser(int userId) {
        LogUtil.d(TAG, "decreaseFailedAttempts");
        if (!inLockoutMode()) {
            SparseIntArray sparseIntArray = this.mFailedAttempts;
            sparseIntArray.put(userId, sparseIntArray.get(userId, 0) - 1);
        }
        LogUtil.v(TAG, "decreaseFailedAttempts finish, mFailedAttempts = " + this.mFailedAttempts.get(userId, 0));
    }

    /* access modifiers changed from: private */
    public void startHidlService() {
        try {
            LogUtil.e(TAG, "startHidlService");
            IfaceHalService ifaceHalService = new IfaceHalService();
            IfaceHalService.configureRpcThreadpool(5, false);
            ifaceHalService.registerAsService("faceservice");
        } catch (RemoteException e) {
            LogUtil.e(TAG, "startHidlService  RemoteException " + e);
        }
    }

    /* access modifiers changed from: protected */
    public int handleVerifyFace(byte[] nv21ImageData, long nv21ImageBufferSeq, ClientMode mode) {
        LogUtil.d(TAG, "handleVerifyFace mode = " + mode);
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "handleVerifyFace: no faced!");
            return -1;
        }
        try {
            Log.w(TAG, "handleVerifyFace");
            String session = String.valueOf(SystemClock.uptimeMillis());
            try {
                this.mHealthMonitor.start(HealthState.VERIFYFACE, 5000, session);
                int result = daemon.verifyFace(nv21ImageData, nv21ImageBufferSeq, mode.getValue());
                if (result != 0) {
                    LogUtil.w(TAG, "handleVerifyFace failed, result=" + result);
                    unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_ERROR);
                    handleError(this.mHalDeviceId, 1, 0);
                }
                return result;
            } finally {
                this.mHealthMonitor.stop(HealthState.VERIFYFACE, session);
            }
        } catch (RemoteException e) {
            LogUtil.e(TAG, "handleVerifyFace failed", e);
            return -1;
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void onEnrollResult(final long deviceId, final int faceId, final int userId, final int remaining) {
        LogUtil.d(TAG, "onEnrollResult, faceId = " + faceId + ", userId = " + userId + ", remaining = " + remaining);
        this.mDaemonCallbackQueue.add(new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass7 */

            public void run() {
                FaceService.super.handleEnrollResult(new Face(FaceService.this.getBiometricUtils().getUniqueName(FaceService.this.getContext(), userId), faceId, deviceId), remaining);
            }
        });
        if (!this.mHandleResultHandler.hasMessages(11)) {
            ExHandler exHandler = this.mHandleResultHandler;
            exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(11));
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void onAcquired(final long deviceId, int userId, final int acquiredInfo, final int vendorCode) {
        LogUtil.d(TAG, "onAcquired acquiredInfo = " + acquiredInfo);
        this.mDaemonCallbackQueue.add(new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass8 */

            public void run() {
                FaceService.super.handleAcquired(deviceId, acquiredInfo, vendorCode);
            }
        });
        if (!this.mHandleResultHandler.hasMessages(11)) {
            ExHandler exHandler = this.mHandleResultHandler;
            exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(11));
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void onAuthenticated(final long deviceId, final int faceId, int userId, final ArrayList<Byte> token) {
        LogUtil.d(TAG, "onAuthenticated faceId = " + faceId + ", userId = " + userId);
        if (faceId == 1) {
            super.handleAuthenticated(new Face("", faceId, deviceId), token);
            return;
        }
        this.mDaemonCallbackQueue.add(new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass9 */

            public void run() {
                FaceService.super.handleAuthenticated(new Face("", faceId, deviceId), token);
            }
        });
        if (!this.mHandleResultHandler.hasMessages(11)) {
            ExHandler exHandler = this.mHandleResultHandler;
            exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(11));
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void onError(final long deviceId, int userId, final int error, final int vendorCode) {
        LogUtil.d(TAG, "onError error = " + error);
        this.mDaemonCallbackQueue.add(new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass10 */

            public void run() {
                ClientMonitor client = FaceService.this.getCurrentClient();
                if (client != null && ((client instanceof AuthenticationClient) || (client instanceof EnrollClient))) {
                    FaceService.this.cancelRecognition();
                }
                FaceService.this.unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_ERROR);
                FaceService.super.handleError(deviceId, error, vendorCode);
                if (error == 1) {
                    LogUtil.w(FaceService.TAG, "Got ERROR_HW_UNAVAILABLE; try reconnecting next client.");
                    synchronized (this) {
                        FaceService.this.mDaemon = null;
                        FaceService.this.mHalDeviceId = 0;
                        FaceService.this.mCurrentUserId = -10000;
                    }
                }
            }
        });
        if (!this.mHandleResultHandler.hasMessages(11)) {
            ExHandler exHandler = this.mHandleResultHandler;
            exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(11));
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void onRemoved(final long deviceId, final ArrayList<Integer> faceIds, int userId) {
        LogUtil.d(TAG, "onRemoved faceIds.size() = " + faceIds.size() + ", userId = " + userId);
        this.mDaemonCallbackQueue.add(new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass11 */

            public void run() {
                if (!faceIds.isEmpty()) {
                    for (int i = 0; i < faceIds.size(); i++) {
                        FaceService.super.handleRemoved(new Face("", ((Integer) faceIds.get(i)).intValue(), deviceId), (faceIds.size() - i) - 1);
                    }
                    return;
                }
                FaceService.super.handleRemoved(new Face("", 0, deviceId), 0);
            }
        });
        if (!this.mHandleResultHandler.hasMessages(11)) {
            ExHandler exHandler = this.mHandleResultHandler;
            exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(11));
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void onEnumerate(long deviceId, ArrayList<Integer> faceIds, int userId) throws RemoteException {
        LogUtil.w(TAG, "onEnumerate, faceIds.size() = " + faceIds.size() + ", userId = " + userId);
        this.mHandler.post(new Runnable(faceIds, deviceId, userId) {
            /* class com.android.server.biometrics.face.$$Lambda$FaceService$cPI6LakKiqC0PdlkHDKCmHaCYa8 */
            private final /* synthetic */ ArrayList f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r5;
            }

            public final void run() {
                FaceService.this.lambda$onEnumerate$0$FaceService(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$onEnumerate$0$FaceService(ArrayList faceIds, long deviceId, int userId) {
        if (!faceIds.isEmpty()) {
            int size = faceIds.size();
            int[] faceFeatureIds = new int[size];
            for (int i = 0; i < size; i++) {
                faceFeatureIds[i] = ((Integer) faceIds.get(i)).intValue();
            }
            handleSyncTemplates(deviceId, faceFeatureIds, userId);
            super.handleEnumerate(null, size);
            return;
        }
        super.handleEnumerate(null, 0);
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void onLockoutChanged(long duration) {
        LogUtil.d(TAG, "onLockoutChanged: " + duration);
        if (duration == 0) {
            this.mCurrentUserLockoutMode = 0;
        } else if (duration == JobStatus.NO_LATEST_RUNTIME) {
            this.mCurrentUserLockoutMode = 2;
        } else {
            this.mCurrentUserLockoutMode = 1;
        }
        this.mHandler.post(new Runnable(duration) {
            /* class com.android.server.biometrics.face.$$Lambda$FaceService$YpQAQYck5P0b5tyC1hdu9LcCGL8 */
            private final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FaceService.this.lambda$onLockoutChanged$1$FaceService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onLockoutChanged$1$FaceService(long duration) {
        if (duration == 0) {
            notifyLockoutResetMonitors();
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void onPreviewStarted() {
        this.mDaemonCallbackQueue.add(new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass12 */

            public void run() {
                FaceService faceService = FaceService.this;
                faceService.handleProgressChanged(faceService.mHalDeviceId, 1001);
            }
        });
        if (!this.mHandleResultHandler.hasMessages(11)) {
            ExHandler exHandler = this.mHandleResultHandler;
            exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(11));
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public int onFaceFilterSucceeded(final long nv21ImageBufferSeq, final byte[] nv21ImageData, final ClientMode mode) {
        final PendingResult<Integer> r = new PendingResult<>(-1);
        this.mHandler.post(new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass13 */

            public void run() {
                r.setResult(Integer.valueOf(FaceService.this.handleVerifyFace(nv21ImageData, nv21ImageBufferSeq, mode)));
            }
        });
        return r.await().intValue();
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void unblockScreenOn(String reason) {
        ClientMonitor currentClient;
        boolean isKeyguard;
        synchronized (this.mClientLock) {
            currentClient = getCurrentClient();
            if (currentClient == null) {
                isKeyguard = true;
            } else {
                isKeyguard = isKeyguard(currentClient.getOwnerString());
            }
        }
        Log.d(TAG, "unblockScreenOn, currentClient = " + currentClient + ", mSupportPowerBlock = " + this.mSupportPowerBlock + ", isKeyguard = " + isKeyguard + ", mIsWakeUp = " + this.mIsWakeUp + ", reason = " + reason);
        if (this.mSupportPowerBlock) {
            if (isKeyguard && this.mIsWakeUp) {
                if (!BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS.equals(reason)) {
                    this.mBiometricManager.setKeyguardOpaque("FaceService", BiometricWakeupManagerService.SET_KEYGUARD_OPAQUE_WHILE_FAIL_UNLOCK);
                } else if (this.mFingerprintOpticalSupport && this.mFingerPrintUnlockEnabled && this.mFingerPrintShowWhenScreenOff) {
                    try {
                        IBinder surfaceFlinger = ServiceManager.getService(OppoLightsService.SURFACE_FLINGER);
                        if (surfaceFlinger != null) {
                            Parcel data = Parcel.obtain();
                            data.writeInterfaceToken("android.ui.ISurfaceComposer");
                            data.writeInt(1);
                            surfaceFlinger.transact(20010, data, null, 0);
                            data.recycle();
                            LogUtil.d(TAG, "unblockScreenOn, hide fingerprint icon");
                        }
                    } catch (RemoteException e) {
                        LogUtil.d(TAG, "get SurfaceFlinger failed!");
                    }
                }
                Message msg = this.mSubHandler.obtainMessage(13);
                msg.obj = reason;
                this.mSubHandler.sendMessageDelayed(msg, 0);
                LogUtil.d(TAG, "unblockScreenOn, MSG_UNBLOCK_SCREEN_ON");
            }
        } else if (!BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS.equals(reason)) {
            this.mBiometricManager.setKeyguardOpaque("FaceService", BiometricWakeupManagerService.SET_KEYGUARD_OPAQUE_WHILE_FAIL_UNLOCK);
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public void blockScreenOn() {
        ClientMonitor currentClient;
        boolean isKeyguard;
        if (!this.mIsScreenOff) {
            Log.d(TAG, "do not blockScreenOn when screen on");
            return;
        }
        synchronized (this.mClientLock) {
            currentClient = getCurrentClient();
            isKeyguard = currentClient != null && (currentClient instanceof AuthenticationClient) && isKeyguard(currentClient.getOwnerString());
        }
        Log.d(TAG, "blockScreenOn, currentClient = " + currentClient + ", mSupportPowerBlock = " + this.mSupportPowerBlock + ", isKeyguard = " + isKeyguard + ", mIsWakeUp = " + this.mIsWakeUp + ", mWakeUpReason = " + this.mWakeUpReason);
        if (this.mSupportPowerBlock && isKeyguard && this.mIsWakeUp && isCameraServiceReady()) {
            String currentWakeUpReason = this.mWakeUpReason;
            if (currentWakeUpReason == null) {
                currentWakeUpReason = "android.policy:POWER";
            }
            this.mBlockScreenOnSession = "0" + String.valueOf(SystemClock.uptimeMillis());
            this.mHealthMonitor.start(HealthState.BLOCKSCREENON, 1000, this.mBlockScreenOnSession);
            this.mBiometricManager.blockScreenOn("FaceService", currentWakeUpReason);
        }
    }

    public void handleUnBlockScreenOn(String reason) {
        LogUtil.d(TAG, "handleUnBlockScreenOn reason = " + reason + ", mIsWakeUp = " + this.mIsWakeUp);
        this.mHealthMonitor.stop(HealthState.BLOCKSCREENON, this.mBlockScreenOnSession);
        this.mBiometricManager.unblockScreenOn("FaceService", reason, 0);
    }

    private final class FaceAuthClient extends BiometricServiceBase.AuthenticationClientImpl {
        private int mLastAcquire;

        public FaceAuthClient(Context context, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, long opId, boolean restricted, String owner, int cookie, boolean requireConfirmation) {
            super(context, daemon, halDeviceId, token, listener, targetUserId, groupId, opId, restricted, owner, cookie, requireConfirmation);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.biometrics.LoggableMonitor
        public int statsModality() {
            return FaceService.this.statsModality();
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public boolean shouldFrameworkHandleLockout() {
            return true;
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public boolean wasUserDetected() {
            return this.mLastAcquire != 11;
        }

        @Override // com.android.server.biometrics.AuthenticationClient, com.android.server.biometrics.ClientMonitor
        public boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean authenticated, ArrayList<Byte> token) {
            boolean result = super.onAuthenticated(identifier, authenticated, token);
            DcsUtil dcs = DcsUtil.getDcsUtil(FaceService.this.mContext);
            if (dcs != null) {
                dcs.sendFaceId(identifier.getBiometricId(), getOwnerString());
            }
            return result || !authenticated;
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public boolean onAcquired(int acquireInfo, int vendorCode) {
            this.mLastAcquire = acquireInfo;
            if (acquireInfo == 13) {
                String name = getContext().getString(17039990);
                String title = getContext().getString(17039991);
                String content = getContext().getString(17039989);
                Intent intent = new Intent("android.settings.FACE_SETTINGS");
                intent.setPackage("com.android.settings");
                PendingIntent pendingIntent = PendingIntent.getActivityAsUser(getContext(), 0, intent, 0, null, UserHandle.CURRENT);
                NotificationManager nm = (NotificationManager) getContext().getSystemService(NotificationManager.class);
                NotificationChannel channel = new NotificationChannel("FaceService", name, 4);
                Notification notification = new Notification.Builder(getContext(), "FaceService").setSmallIcon(17302446).setContentTitle(title).setContentText(content).setSubText(name).setOnlyAlertOnce(true).setLocalOnly(true).setAutoCancel(true).setCategory("sys").setContentIntent(pendingIntent).build();
                nm.createNotificationChannel(channel);
                nm.notifyAsUser(null, 0, notification, UserHandle.CURRENT);
            }
            return super.onAcquired(acquireInfo, vendorCode);
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public void resetFailedAttempts() {
            FaceService.this.mHandler.post(FaceService.this.mResetFailedAttemptsRunnable);
        }

        @Override // com.android.server.biometrics.AuthenticationClient, com.android.server.biometrics.BiometricServiceBase.AuthenticationClientImpl
        public int handleFailedAttempt() {
            int currentUser = ActivityManager.getCurrentUser();
            FaceService.this.mFailedAttempts.put(currentUser, FaceService.this.mFailedAttempts.get(currentUser, 0) + 1);
            if (FaceService.this.inLockoutMode()) {
                FaceService.this.scheduleLockoutResetForUser(currentUser);
                FaceService.this.setLockoutAttemptDeadline(SystemClock.elapsedRealtime() + 30000, currentUser);
            }
            return super.handleFailedAttempt();
        }

        @Override // com.android.server.biometrics.AuthenticationClient, com.android.server.biometrics.ClientMonitor
        public void binderDied() {
            FaceService.this.unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_ERROR);
            super.binderDied();
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public void destroy() {
            LogUtil.d(LoggableMonitor.TAG, "destroy!");
            super.destroy();
            if (FaceService.this.mDaemonWrapper != null) {
                ((FaceDaemonWrapper) FaceService.this.mDaemonWrapper).destroy();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class FaceServiceWrapper extends IFaceService.Stub {
        private FaceServiceWrapper() {
        }

        public void resetFaceDaemon() {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            LogUtil.d(FaceService.TAG, "resetFaceDaemon");
            FaceService.this.mDaemonWrapper = null;
            FaceService.this.mDaemon = null;
        }

        public int getFaceProcessMemory() {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            LogUtil.d(FaceService.TAG, "getFaceProcessMemory");
            return FaceService.this.mHealthMonitor.getFaceProcessMemory();
        }

        public long generateChallenge(final IBinder token) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            final PendingResult<Long> r = new PendingResult<>(0L);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass1 */

                public void run() {
                    if (FaceService.this.tryPreOperation(token, HealthState.GENERATECHALLENGE)) {
                        r.setResult(Long.valueOf(FaceService.this.startGenerateChallenge(token)));
                    } else {
                        r.cancel();
                    }
                }
            });
            return r.await().longValue();
        }

        public int revokeChallenge(final IBinder token) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            final PendingResult<Integer> r = new PendingResult<>(0);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass2 */

                public void run() {
                    if (FaceService.this.tryPreOperation(token, HealthState.REVOKECHALLENGE)) {
                        r.setResult(Integer.valueOf(FaceService.this.startRevokeChallenge(token)));
                    } else {
                        r.cancel();
                    }
                }
            });
            return r.await().intValue();
        }

        public void enroll(IBinder token, byte[] cryptoToken, IFaceServiceReceiver receiver, String opPackageName, int[] disabledFeatures) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            boolean restricted = FaceService.this.isRestricted();
            LogUtil.d(FaceService.TAG, "enroll, opPackageName = " + opPackageName + ", mCurrentUserId = " + FaceService.this.mCurrentUserId);
            Context context = FaceService.this.getContext();
            BiometricServiceBase.DaemonWrapper daemonWrapper = FaceService.this.mDaemonWrapper;
            long j = FaceService.this.mHalDeviceId;
            FaceService faceService = FaceService.this;
            BiometricServiceBase.EnrollClientImpl client = new BiometricServiceBase.EnrollClientImpl(context, daemonWrapper, j, token, faceService.mServiceListenerImpl = new ServiceListenerImpl(receiver), FaceService.this.mCurrentUserId, 0, cryptoToken, restricted, opPackageName, disabledFeatures) {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass3 */

                @Override // com.android.server.biometrics.EnrollClient
                public boolean shouldVibrate() {
                    return false;
                }

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FaceService.this.statsModality();
                }
            };
            if (FaceService.this.tryPreOperation(token, "enroll")) {
                FaceService faceService2 = FaceService.this;
                if (!faceService2.hasReachedEnrollmentLimit(faceService2.mCurrentUserId)) {
                    FaceService faceService3 = FaceService.this;
                    if (faceService3.isCurrentUserOrProfile(faceService3.mCurrentUserId)) {
                        FaceService.this.mHandler.post(new Runnable(client) {
                            /* class com.android.server.biometrics.face.$$Lambda$FaceService$FaceServiceWrapper$ONI4ojyd57e66xEiYroA4Aj30Cs */
                            private final /* synthetic */ BiometricServiceBase.EnrollClientImpl f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                FaceService.FaceServiceWrapper.this.lambda$enroll$0$FaceService$FaceServiceWrapper(this.f$1);
                            }
                        });
                        return;
                    }
                    return;
                }
                return;
            }
            FaceService.this.notifyOperationCanceled(receiver);
        }

        public /* synthetic */ void lambda$enroll$0$FaceService$FaceServiceWrapper(BiometricServiceBase.EnrollClientImpl client) {
            FaceService.this.startClient(client, true);
        }

        public void cancelEnrollment(IBinder token) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            LogUtil.d(FaceService.TAG, "cancelEnrollment");
            if (FaceService.this.tryPreOperation(token, "cancelEnrollment")) {
                FaceService.this.cancelRecognition();
                FaceService.this.mHandler.post(new Runnable(token) {
                    /* class com.android.server.biometrics.face.$$Lambda$FaceService$FaceServiceWrapper$NAgRmYm8v4HJY1gx80DjvE13pK0 */
                    private final /* synthetic */ IBinder f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        FaceService.FaceServiceWrapper.this.lambda$cancelEnrollment$1$FaceService$FaceServiceWrapper(this.f$1);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$cancelEnrollment$1$FaceService$FaceServiceWrapper(IBinder token) {
            ClientMonitor client = FaceService.this.getCurrentClient();
            if ((client instanceof EnrollClient) && client.getToken() == token) {
                Log.v(FaceService.this.getTag(), "Cancelling enrollment");
                client.stop(client.getToken() == token);
            }
        }

        public void authenticate(final IBinder token, long opId, int userId, final IFaceServiceReceiver receiver, int flags, final String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            FaceService.this.updateActiveGroup(userId, opPackageName);
            boolean restricted = FaceService.this.isRestricted();
            if (FaceService.this.mDcsUtil != null) {
                FaceService.this.mDcsUtil.clearMap();
            }
            FaceService faceService = FaceService.this;
            Context context = faceService.getContext();
            BiometricServiceBase.DaemonWrapper daemonWrapper = FaceService.this.mDaemonWrapper;
            long j = FaceService.this.mHalDeviceId;
            FaceService faceService2 = FaceService.this;
            final BiometricServiceBase.AuthenticationClientImpl client = new FaceAuthClient(context, daemonWrapper, j, token, faceService2.mServiceListenerImpl = new ServiceListenerImpl(receiver), FaceService.this.mCurrentUserId, 0, opId, restricted, opPackageName, 0, false);
            LogUtil.d(FaceService.TAG, "authenticate, opPackageName = " + opPackageName + ", userId = " + userId);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass4 */

                public void run() {
                    if (FaceService.this.tryPreOperation(token, "authenticate", opPackageName)) {
                        FaceServiceWrapper.this.startAuthentication(client, opPackageName);
                    } else {
                        FaceService.this.notifyOperationCanceled(receiver);
                    }
                }
            });
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startAuthentication(BiometricServiceBase.AuthenticationClientImpl client, String opPackageName) {
            int errorCode;
            String tag = FaceService.this.getTag();
            Log.v(tag, "startAuthentication(" + opPackageName + ")");
            int lockoutMode = FaceService.this.getLockoutMode();
            if (lockoutMode != 0) {
                String tag2 = FaceService.this.getTag();
                Log.v(tag2, "In lockout mode(" + lockoutMode + ") ; disallowing authentication");
                if (lockoutMode == 1) {
                    errorCode = 7;
                } else {
                    errorCode = 9;
                }
                if (!client.onError(FaceService.this.getHalDeviceId(), errorCode, 0)) {
                    Log.w(FaceService.this.getTag(), "Cannot send permanent lockout message to client");
                    return;
                }
                return;
            }
            FaceService.this.startClient(client, true);
        }

        public void prepareForAuthentication(boolean requireConfirmation, IBinder token, long opId, int groupId, IBiometricServiceReceiverInternal wrapperReceiver, String opPackageName, int cookie, int callingUid, int callingPid, int callingUserId) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            FaceService.this.updateActiveGroup(groupId, opPackageName);
            FaceService faceService = FaceService.this;
            FaceService.this.authenticateInternal(new FaceAuthClient(faceService.getContext(), FaceService.this.mDaemonWrapper, FaceService.this.mHalDeviceId, token, new BiometricPromptServiceListenerImpl(wrapperReceiver), FaceService.this.mCurrentUserId, 0, opId, true, opPackageName, cookie, requireConfirmation), opId, opPackageName, callingUid, callingPid, callingUserId);
        }

        public void startPreparedClient(int cookie) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.startCurrentClient(cookie);
        }

        public void cancelAuthentication(IBinder token, String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            LogUtil.d(FaceService.TAG, "cancelAuthentication, opPackageName = " + opPackageName);
            cancelAuthenticationInternal(token, opPackageName);
        }

        /* access modifiers changed from: protected */
        public void cancelAuthenticationInternal(IBinder token, String opPackageName) {
            cancelAuthenticationInternal(token, opPackageName, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId(), true);
        }

        /* access modifiers changed from: protected */
        public void cancelAuthenticationInternal(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) {
            if (!fromClient || FaceService.this.canUseBiometric(opPackageName, true, callingUid, callingPid, callingUserId)) {
                LogUtil.d(FaceService.TAG, "cancelAuthenticationInternal, start");
                FaceService.this.mSubHandler.post(new Runnable(token, fromClient) {
                    /* class com.android.server.biometrics.face.$$Lambda$FaceService$FaceServiceWrapper$yLkvsVjczbJUXRHRRTfvfiFNRzg */
                    private final /* synthetic */ IBinder f$1;
                    private final /* synthetic */ boolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        FaceService.FaceServiceWrapper.this.lambda$cancelAuthenticationInternal$3$FaceService$FaceServiceWrapper(this.f$1, this.f$2);
                    }
                });
                return;
            }
            String tag = FaceService.this.getTag();
            Log.v(tag, "---cancelAuthentication(): reject " + opPackageName);
        }

        public /* synthetic */ void lambda$cancelAuthenticationInternal$3$FaceService$FaceServiceWrapper(IBinder token, boolean fromClient) {
            ClientMonitor client = FaceService.this.getCurrentClient();
            if (client instanceof AuthenticationClient) {
                if (client.getToken() == token || !fromClient) {
                    String tag = FaceService.this.getTag();
                    Log.v(tag, "---Stopping client " + client.getOwnerString() + ", fromClient: " + fromClient);
                    client.stop(client.getToken() == token);
                    LogUtil.d(FaceService.TAG, "cancelAuthenticationInternal, unblockScreenOn");
                    FaceService.this.unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_ERROR);
                    client.onError(FaceService.this.getHalDeviceId(), 5, 0);
                    FaceService.this.mHandler.post(new Runnable(client) {
                        /* class com.android.server.biometrics.face.$$Lambda$FaceService$FaceServiceWrapper$iKOf6kG1i4hXKtnqFW6GK7F4Tbc */
                        private final /* synthetic */ ClientMonitor f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            FaceService.FaceServiceWrapper.this.lambda$cancelAuthenticationInternal$2$FaceService$FaceServiceWrapper(this.f$1);
                        }
                    });
                    return;
                }
                String tag2 = FaceService.this.getTag();
                Log.v(tag2, "---Can't stop client " + client.getOwnerString() + " since tokens don't match. fromClient: " + fromClient);
            } else if (client != null) {
                String tag3 = FaceService.this.getTag();
                Log.v(tag3, "---Can't cancel non-authenticating client " + client.getOwnerString());
            }
        }

        public /* synthetic */ void lambda$cancelAuthenticationInternal$2$FaceService$FaceServiceWrapper(ClientMonitor client) {
            FaceService.this.removeClient(client);
        }

        public void cancelAuthenticationFromService(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            cancelAuthenticationInternal(token, opPackageName, callingUid, callingPid, callingUserId, fromClient);
        }

        public void setActiveUser(final int userId) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass5 */

                public void run() {
                    LogUtil.d(FaceService.TAG, "setActiveUser, userId = " + userId);
                    FaceService.this.setActiveUserInternal(userId);
                }
            });
        }

        public void remove(final IBinder token, int faceId, int userId, final IFaceServiceReceiver receiver) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (token == null) {
                Log.w(FaceService.TAG, "remove(): token is null");
                return;
            }
            final RemovalClient client = new RemovalClient(FaceService.this.getContext(), FaceService.this.getConstants(), FaceService.this.mDaemonWrapper, FaceService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), faceId, 0, userId, FaceService.this.isRestricted(), token.toString(), FaceService.this.getBiometricUtils()) {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass6 */

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FaceService.this.statsModality();
                }
            };
            LogUtil.d(FaceService.TAG, "remove faceId = " + faceId + ", userId = " + userId);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass7 */

                public void run() {
                    if (FaceService.this.tryPreOperation(token, "remove")) {
                        FaceService.this.startClient(client, true);
                    } else {
                        FaceService.this.notifyOperationCanceled(receiver);
                    }
                }
            });
        }

        public void enumerate(IBinder token, int userId, IFaceServiceReceiver receiver) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.mHandler.post(new Runnable(new EnumerateClient(FaceService.this.getContext(), FaceService.this.getConstants(), FaceService.this.mDaemonWrapper, FaceService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), userId, userId, FaceService.this.isRestricted(), FaceService.this.getContext().getOpPackageName()) {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass8 */

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FaceService.this.statsModality();
                }
            }) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$FaceServiceWrapper$H8vebHzPJVr78j_NVWmSEDEEC0 */
                private final /* synthetic */ EnumerateClient f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    FaceService.FaceServiceWrapper.this.lambda$enumerate$4$FaceService$FaceServiceWrapper(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$enumerate$4$FaceService$FaceServiceWrapper(EnumerateClient client) {
            FaceService.this.startClient(client, true);
        }

        public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) throws RemoteException {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            FaceService.super.addLockoutResetCallback(callback);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (!DumpUtils.checkDumpPermission(FaceService.this.getContext(), FaceService.TAG, pw)) {
                LogUtil.d(FaceService.TAG, "Permission Denial: can't dump Face from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                if (args.length > 1 && "--hal".equals(args[0])) {
                    FaceService.this.dumpHal(fd, (String[]) Arrays.copyOfRange(args, 1, args.length, args.getClass()));
                } else if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                    FaceService.this.dumpInternal(pw);
                    FaceService.this.dumpInternal(pw, args);
                } else {
                    FaceService.this.dumpProto(fd);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isHardwareDetected(long deviceId, String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            boolean z = false;
            if (!FaceService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return false;
            }
            long token = Binder.clearCallingIdentity();
            try {
                if (!(FaceService.this.getFaceDaemon() == null || FaceService.this.mHalDeviceId == 0)) {
                    z = true;
                }
                return z;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void rename(final int faceId, final String name) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (FaceService.this.isCurrentUserOrProfile(UserHandle.getCallingUserId())) {
                LogUtil.d(FaceService.TAG, " rename faceId = " + faceId + ", name =  " + name);
                FaceService.this.mHandler.post(new Runnable() {
                    /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass9 */

                    public void run() {
                        FaceService.this.getBiometricUtils().renameBiometricForUser(FaceService.this.getContext(), FaceService.this.mCurrentUserId, faceId, name);
                    }
                });
            }
        }

        public List<Face> getEnrolledFaces(int userId, String opPackageName) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (!FaceService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return null;
            }
            LogUtil.d(FaceService.TAG, "getEnrolledFaces opPackageName = " + opPackageName + ", userId = " + userId);
            return FaceService.this.getEnrolledTemplates(userId);
        }

        public boolean hasEnrolledFaces(int userId, String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            if (!FaceService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return false;
            }
            boolean hasFace = FaceService.this.hasEnrolledBiometrics(userId);
            LogUtil.d(FaceService.TAG, " hasEnrolledFaces opPackageName = " + opPackageName + ", hasFace = " + hasFace + ", userId = " + userId);
            return hasFace;
        }

        public long getAuthenticatorId(String opPackageName) {
            return FaceService.this.getAuthenticatorId(opPackageName);
        }

        public void resetLockout(byte[] token) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            LogUtil.v(FaceService.TAG, "resetTimeout");
            FaceService faceService = FaceService.this;
            if (!faceService.hasEnrolledBiometrics(faceService.mCurrentUserId)) {
                Log.w(FaceService.TAG, "Ignoring lockout reset, no templates enrolled");
                return;
            }
            try {
                FaceService.this.mDaemonWrapper.resetLockout(token);
                if (token == null || token.length <= 0 || token[0] != 1) {
                    FaceService.this.mHandler.post(FaceService.this.mResetFailedAttemptsRunnable);
                } else {
                    FaceService.this.mHandler.post(FaceService.this.mDecreaseFailedAttemptsRunnable);
                }
            } catch (RemoteException e) {
                Log.e(FaceService.this.getTag(), "Unable to reset lockout", e);
            }
        }

        public void setFeature(int feature, boolean enabled, byte[] token, IFaceServiceReceiver receiver) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.mHandler.post(new Runnable(feature, token, enabled, receiver) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$FaceServiceWrapper$u8QDc0VPoyJUNlQIG9ArRdQ_o48 */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ byte[] f$2;
                private final /* synthetic */ boolean f$3;
                private final /* synthetic */ IFaceServiceReceiver f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    FaceService.FaceServiceWrapper.this.lambda$setFeature$5$FaceService$FaceServiceWrapper(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        public /* synthetic */ void lambda$setFeature$5$FaceService$FaceServiceWrapper(int feature, byte[] token, boolean enabled, IFaceServiceReceiver receiver) {
            FaceService faceService = FaceService.this;
            if (!faceService.hasEnrolledBiometrics(faceService.mCurrentUserId)) {
                Log.e(FaceService.TAG, "No enrolled biometrics while setting feature: " + feature);
                return;
            }
            ArrayList<Byte> byteToken = new ArrayList<>();
            for (byte b : token) {
                byteToken.add(Byte.valueOf(b));
            }
            int faceId = getFirstTemplateForUser(FaceService.this.mCurrentUserId);
            if (FaceService.this.mDaemon != null) {
                try {
                    String session = String.valueOf(SystemClock.uptimeMillis());
                    try {
                        FaceService.this.mHealthMonitor.start(HealthState.SETFEATURE, 5000, session);
                        receiver.onFeatureSet(FaceService.this.mDaemon.setFeature(feature, enabled, byteToken, faceId) == 0, feature);
                    } finally {
                        FaceService.this.mHealthMonitor.stop(HealthState.SETFEATURE, session);
                    }
                } catch (RemoteException e) {
                    Log.e(FaceService.this.getTag(), "Unable to set feature: " + feature + " to enabled:" + enabled, e);
                }
            }
        }

        public void getFeature(int feature, IFaceServiceReceiver receiver) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.mHandler.post(new Runnable(feature, receiver) {
                /* class com.android.server.biometrics.face.$$Lambda$FaceService$FaceServiceWrapper$oADmmfi5ICEnegRQ9k5NrA06ud4 */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ IFaceServiceReceiver f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    FaceService.FaceServiceWrapper.this.lambda$getFeature$6$FaceService$FaceServiceWrapper(this.f$1, this.f$2);
                }
            });
        }

        /* JADX INFO: finally extract failed */
        public /* synthetic */ void lambda$getFeature$6$FaceService$FaceServiceWrapper(int feature, IFaceServiceReceiver receiver) {
            FaceService faceService = FaceService.this;
            if (!faceService.hasEnrolledBiometrics(faceService.mCurrentUserId)) {
                Log.e(FaceService.TAG, "No enrolled biometrics while getting feature: " + feature);
                return;
            }
            int faceId = getFirstTemplateForUser(FaceService.this.mCurrentUserId);
            if (FaceService.this.mDaemon != null) {
                try {
                    String session = String.valueOf(SystemClock.uptimeMillis());
                    try {
                        FaceService.this.mHealthMonitor.start(HealthState.GETFEATURE, 5000, session);
                        int result = FaceService.this.mDaemon.getFeature(feature, faceId);
                        FaceService.this.mHealthMonitor.stop(HealthState.GETFEATURE, session);
                        boolean z = true;
                        boolean z2 = result == 0;
                        if (result != 0) {
                            z = false;
                        }
                        receiver.onFeatureGet(z2, feature, z);
                    } catch (Throwable th) {
                        FaceService.this.mHealthMonitor.stop(HealthState.GETFEATURE, session);
                        throw th;
                    }
                } catch (RemoteException e) {
                    Log.e(FaceService.this.getTag(), "Unable to getRequireAttention", e);
                }
            }
        }

        public void userActivity() {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (FaceService.this.mDaemon != null) {
                try {
                    String session = String.valueOf(SystemClock.uptimeMillis());
                    try {
                        FaceService.this.mHealthMonitor.start(HealthState.USERACTIVITY, 5000, session);
                        FaceService.this.mDaemon.userActivity();
                    } finally {
                        FaceService.this.mHealthMonitor.stop(HealthState.USERACTIVITY, session);
                    }
                } catch (RemoteException e) {
                    Log.e(FaceService.this.getTag(), "Unable to send userActivity", e);
                }
            }
        }

        private int getFirstTemplateForUser(int user) {
            List<Face> faces = FaceService.this.getEnrolledTemplates(user);
            if (!faces.isEmpty()) {
                return faces.get(0).getBiometricId();
            }
            return 0;
        }

        public void setPreviewFrame(final IBinder token, final Rect rect) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass10 */

                public void run() {
                    FaceService.this.startSetPreviewFrame(token, rect);
                }
            });
        }

        public int setPreviewSurface(IBinder token, final Surface surface) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            final PendingResult<Integer> r = new PendingResult<>(-1);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass11 */

                public void run() {
                    r.setResult(Integer.valueOf(FaceService.this.startSetPreviewSurface(surface)));
                }
            });
            return r.await().intValue();
        }

        public void notifyFirstUnlockWhenBoot(boolean isFirstUnlockInPasswordOnlyMode) {
            LogUtil.d(FaceService.TAG, "notifyFirstUnlockWhenBoot, mIsFirstUnlockedInPasswordOnlyMode = " + isFirstUnlockInPasswordOnlyMode);
        }

        public int getEnrollmentTotalTimes(final IBinder token) {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (FaceService.this.mEnrollmentTotalTimes != 0) {
                LogUtil.d(FaceService.TAG, "has got total times, just return it");
                return FaceService.this.mEnrollmentTotalTimes;
            }
            final PendingResult<Integer> r = new PendingResult<>(0);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass12 */

                public void run() {
                    if (FaceService.this.tryPreOperation(token, "getEnrollmentTotalTimes")) {
                        r.setResult(Integer.valueOf(FaceService.this.startGetEnrollmentTotalTimes(token)));
                    } else {
                        r.cancel();
                    }
                }
            });
            return r.await().intValue();
        }

        public long getLockoutAttemptDeadline(String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            return FaceService.this.getLockoutAttemptDeadline(opPackageName, ActivityManager.getCurrentUser());
        }

        public int getFailedAttempts(String opPackageName) {
            FaceService.this.checkPermission("android.permission.USE_BIOMETRIC_INTERNAL");
            LogUtil.d(FaceService.TAG, "getFailedAttempts opPackageName = " + opPackageName);
            final PendingResult<Integer> r = new PendingResult<>(0);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass13 */

                public void run() {
                    r.setResult(Integer.valueOf(FaceService.this.getFailedAttempts()));
                }
            });
            return r.await().intValue();
        }

        public int getPreviewWidth() {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            LogUtil.d(FaceService.TAG, "getPreviewWidth");
            final PendingResult<Integer> r = new PendingResult<>(0);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass14 */

                public void run() {
                    r.setResult(Integer.valueOf(FaceService.this.getPreViewWidth()));
                }
            });
            return r.await().intValue();
        }

        public int getPreviewHeight() {
            FaceService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            LogUtil.d(FaceService.TAG, "getPreviewHeight");
            final PendingResult<Integer> r = new PendingResult<>(0);
            FaceService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.FaceServiceWrapper.AnonymousClass15 */

                public void run() {
                    r.setResult(Integer.valueOf(FaceService.this.getPreViewHeight()));
                }
            });
            return r.await().intValue();
        }
    }

    private boolean isCameraServiceReady() {
        if (ServiceManager.getService(CAMERA_SERVICE_BINDER_NAME) != null) {
            return true;
        }
        LogUtil.d(TAG, "cameraService is not ready");
        unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_ERROR);
        handleError(this.mHalDeviceId, 0, 0);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getLockoutAttemptDeadline(String opPackageName, int userId) {
        synchronized (this.mLockoutDeadlineLock) {
            LogUtil.d(TAG, "getLockoutAttemptDeadline opPackageName = " + opPackageName + ", userId = " + userId);
            long now = SystemClock.elapsedRealtime();
            this.mLockoutDeadline = this.mLockDeadLineArray.get(userId);
            if (this.mLockoutDeadline == 0) {
                return 0;
            }
            if (this.mLockoutDeadline != 0 && this.mLockoutDeadline < now) {
                setLockoutAttemptDeadline(0, userId);
                return 0;
            } else if (this.mLockoutDeadline > 30000 + now) {
                setLockoutAttemptDeadline(0, userId);
                return 0;
            } else {
                return this.mLockoutDeadline;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setLockoutAttemptDeadline(long deadline, int userId) {
        synchronized (this.mLockoutDeadlineLock) {
            this.mLockDeadLineArray.put(userId, deadline);
            LogUtil.w(TAG, "setLockoutAttemptDeadline, mLockoutDeadline = " + this.mLockoutDeadline + ", userId:" + userId);
        }
    }

    @Override // com.android.server.biometrics.face.IRecognitionCallback
    public int getFailedAttempts() {
        return this.mFailedAttempts.get(ActivityManager.getCurrentUser(), 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int startGetEnrollmentTotalTimes(IBinder token) {
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startGetEnrollmentTotalTimes: no faced!");
            return 0;
        }
        try {
            LogUtil.d(TAG, "startGetEnrollmentTotalTimes");
            String session = String.valueOf(SystemClock.uptimeMillis());
            try {
                this.mHealthMonitor.start("getEnrollmentTotalTimes", 5000, session);
                this.mEnrollmentTotalTimes = daemon.getEnrollmentTotalTimes();
                return this.mEnrollmentTotalTimes;
            } finally {
                this.mHealthMonitor.stop("getEnrollmentTotalTimes", session);
            }
        } catch (RemoteException e) {
            LogUtil.e(TAG, "startGetEnrollmentTotalTimes failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getPreViewWidth() {
        try {
            LogUtil.d(TAG, HealthState.GET_PREIVIEW_WIDTH);
            if (this.mDaemonWrapper != null) {
                return ((FaceDaemonWrapper) this.mDaemonWrapper).getPreViewWidth();
            }
            return -1;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "getPreViewWidth failed", e);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getPreViewHeight() {
        try {
            LogUtil.d(TAG, HealthState.GET_PREVIEW_HEIGHT);
            if (this.mDaemonWrapper != null) {
                return ((FaceDaemonWrapper) this.mDaemonWrapper).getPreViewHeight();
            }
            return -1;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "getPreViewHeight failed", e);
            return -1;
        }
    }

    @Override // com.android.server.Watchdog.Monitor
    public void monitor() {
    }

    private FacePowerManager getFacePms() {
        return FacePowerManager.getFacePowerManager();
    }

    /* access modifiers changed from: package-private */
    public boolean tryPreOperation(IBinder token, String operation) {
        return tryPreOperation(token, operation, null);
    }

    /* access modifiers changed from: package-private */
    public boolean tryPreOperation(IBinder token, String operation, String opPackageName) {
        LogUtil.d(TAG, "try operation " + operation);
        if (isKeyguard(opPackageName)) {
            return true;
        }
        boolean z = false;
        if (token != null && !token.isBinderAlive()) {
            return false;
        }
        synchronized (this.mClientLock) {
            ClientMonitor currentClient = getCurrentClient();
            if (currentClient != null && (currentClient instanceof AuthenticationClient)) {
                IBinder authtoken = currentClient.getToken();
                StringBuilder sb = new StringBuilder();
                sb.append("phone is unlocked ? ");
                sb.append(!this.mKeyguardManager.isKeyguardLocked());
                LogUtil.d(TAG, sb.toString());
                if (isKeyguard(currentClient.getOwnerString()) && this.mKeyguardManager.isKeyguardLocked()) {
                    if (token == authtoken) {
                        z = true;
                    }
                    return z;
                } else if (!(authtoken == token || token == null || !operation.equals(HealthState.CANCEL_FACE))) {
                    LogUtil.d(TAG, "ignore cancel of AuthenticationClient");
                    return false;
                }
            }
            if (currentClient == null || !(currentClient instanceof EnrollClient) || currentClient.getToken() == token || token == null || !operation.equals(HealthState.CANCEL_FACE)) {
                return true;
            }
            LogUtil.d(TAG, "ignore cancel of EnrollClient");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyOperationCanceled(IFaceServiceReceiver receiver) {
        if (receiver != null) {
            try {
                receiver.onError(this.mHalDeviceId, 5, 0);
            } catch (RemoteException e) {
                LogUtil.w(TAG, "Failed to send error to receiver: ", e);
            }
        }
    }

    public void systemReady() {
        LogUtil.d(TAG, "systemReady, mFaceSupport = " + this.mFaceSupport);
        this.mBiometricManager = (BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class);
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        if (this.mFaceSupport) {
            this.mFaceSwitchHelper.initUpdateBroadcastReceiver();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int startUpdateSettingSwitchStatus(int switchType, int on) {
        LogUtil.d(TAG, "startUpdateSettingSwitchStatus switchType = " + switchType + " on = " + on);
        if (this.mDaemon != null) {
            IBiometricsFace daemon = getFaceDaemon();
            if (daemon == null) {
                LogUtil.w(TAG, "startUpdateSettingSwitchStatus: no faced!");
                return 0;
            }
            try {
                String session = String.valueOf(SystemClock.uptimeMillis());
                try {
                    this.mHealthMonitor.start(HealthState.UPDATE_SETTING_SWITCH_STATUS, 5000, session);
                    int result = daemon.updateSettingSwitchStatus(switchType, on == 1);
                    if (result != 0) {
                        LogUtil.w(TAG, "startUpdateSettingSwitchStatus failed, result=" + result);
                        unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_ERROR);
                        handleError(this.mHalDeviceId, 1, 0);
                    }
                } finally {
                    this.mHealthMonitor.stop(HealthState.UPDATE_SETTING_SWITCH_STATUS, session);
                }
            } catch (RemoteException e) {
                LogUtil.e(TAG, "startUpdateSettingSwitchStatus failed", e);
            }
        }
        return 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int startSetPreviewSurface(Surface surface) {
        LogUtil.d(TAG, "startSetPreviewSurface Surface = " + surface);
        BiometricServiceBase.DaemonWrapper daemonWrapper = this.mDaemonWrapper;
        if (daemonWrapper == null) {
            return 1;
        }
        ((FaceDaemonWrapper) daemonWrapper).setPreviewSurface(surface);
        return 1;
    }

    public void startSetPreviewFrame(IBinder token, Rect rect) {
        LogUtil.d(TAG, "startSetPreviewFrame rect = " + rect);
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startSetPreviewFrame: no faced!");
            return;
        }
        try {
            LogUtil.w(TAG, "startSetPreviewFrame");
            RectHal rectHal = new RectHal();
            rectHal.left = rect.left;
            rectHal.top = rect.top;
            rectHal.right = rect.right;
            rectHal.bottom = rect.bottom;
            String session = String.valueOf(SystemClock.uptimeMillis());
            try {
                this.mHealthMonitor.start(HealthState.SET_PREVIEW_FRAME, 5000, session);
                int result = daemon.setPreviewFrame(rectHal);
                if (result != 0) {
                    LogUtil.w(TAG, "startSetPreviewFrame with rect = " + rect + " failed, result=" + result);
                    handleError(this.mHalDeviceId, 1, 0);
                }
            } finally {
                this.mHealthMonitor.stop(HealthState.SET_PREVIEW_FRAME, session);
            }
        } catch (RemoteException e) {
            LogUtil.e(TAG, "startSetPreviewFrame failed", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startWakeUp(String wakeupReason) {
        LogUtil.d(TAG, "startWakeUp");
        if (getFaceDaemon() == null) {
            LogUtil.w(TAG, "startWakeUp: no faced!");
            return;
        }
        long startTime = SystemClock.uptimeMillis();
        this.mWakeUpReason = wakeupReason;
        if (this.mPowerState == PowerState.WAKE_HALF) {
            this.mBiometricManager.setKeyguardTransparent("FaceService", wakeupReason);
        } else if (this.mPowerState == PowerState.WAKE_FULNESS) {
            this.mBiometricManager.setKeyguardTransparent("FaceService", BiometricWakeupManagerService.KEEP_KEYGUARD_OPAQUE_WHILE_AUTO_UNLOCK);
        }
        TimeUtils.calculateTime(TAG, "startWakeUp", SystemClock.uptimeMillis() - startTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelRecognition() {
        BiometricServiceBase.DaemonWrapper daemonWrapper = this.mDaemonWrapper;
        if (daemonWrapper != null) {
            ((FaceDaemonWrapper) daemonWrapper).cancelRecognition();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startGoToSleep() {
        LogUtil.d(TAG, "startGoToSleep");
        cancelRecognition();
        this.mWakeUpReason = null;
        this.mBiometricManager.cancelFaceAuthenticateWhileScreenOff("FaceService", "cancelRecognitionByScreenOff");
    }

    @Override // com.android.server.biometrics.face.power.FacePowerManager.IPowerCallback
    public boolean isFaceAutoUnlockEnabled() {
        if (!this.mFaceSupport || !this.mFaceUnlockEnabled || !this.mFaceUnlockAutoWhenScreenOn || inLockoutMode()) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.biometrics.face.power.FacePowerManager.IPowerCallback
    public boolean isFaceFingerprintCombineUnlockEnabled() {
        return this.mFaceFingerprintCombineUnlockEnabled;
    }

    @Override // com.android.server.biometrics.face.power.FacePowerManager.IPowerCallback
    public boolean isOpticalFingerprintSupport() {
        return this.mFingerprintOpticalSupport;
    }

    @Override // com.android.server.biometrics.face.power.FacePowerManager.IPowerCallback
    public void onWakeUp(final String wakeupReason) {
        Log.d(TAG, "onWakeUp wakeupReason = " + wakeupReason);
        this.mIsWakeUp = true;
        this.mPowerState = PowerState.WAKE_HALF;
        if (isFaceAutoUnlockEnabled()) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.AnonymousClass15 */

                public void run() {
                    FaceService.this.startWakeUp(wakeupReason);
                }
            });
        }
    }

    @Override // com.android.server.biometrics.face.power.FacePowerManager.IPowerCallback
    public void onGoToSleep() {
        LogUtil.d(TAG, "onGoToSleep");
        dispatchScreenOff(true);
        if (this.mFaceSupport) {
            this.mIsWakeUp = false;
            this.mPowerState = PowerState.SLEEP_HALF;
            if (inLockoutMode()) {
                LogUtil.d(TAG, "onGoToSleep in lockout mode; disallowing closeCamera");
            } else {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.biometrics.face.FaceService.AnonymousClass16 */

                    public void run() {
                        FaceService.this.startGoToSleep();
                    }
                });
            }
        }
    }

    @Override // com.android.server.biometrics.face.power.FacePowerManager.IPowerCallback
    public void onWakeUpFinish() {
        LogUtil.d(TAG, "onWakeUpFinish");
        dispatchScreenOff(false);
        this.mPowerState = PowerState.WAKE_FULNESS;
    }

    @Override // com.android.server.biometrics.face.power.FacePowerManager.IPowerCallback
    public void onGoToSleepFinish() {
        LogUtil.d(TAG, "onGoToSleepFinish");
        dispatchScreenOff(true);
    }

    @Override // com.android.server.biometrics.face.power.FacePowerManager.IPowerCallback
    public void onScreenOnUnBlockedByOther(final String unBlockedReason) {
        LogUtil.d(TAG, "onScreenOnUnBlockedByOther, unBlockedReason = " + unBlockedReason);
        if (isFaceAutoUnlockEnabled()) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.face.FaceService.AnonymousClass17 */

                public void run() {
                    FaceService.this.startWakeUp(unBlockedReason);
                }
            });
        }
    }

    @Override // com.android.server.biometrics.face.FaceSwitchHelper.ISwitchUpdateListener
    public void onFaceSwitchUpdate() {
        LogUtil.d(TAG, "onFaceSwitchUpdate");
    }

    @Override // com.android.server.biometrics.face.FaceSwitchHelper.ISwitchUpdateListener
    public void onFaceUpdateFromProvider() {
        LogUtil.d(TAG, "onFaceUpdateFromProvider");
        if (getFaceDaemon() == null) {
            LogUtil.e(TAG, "daemon is null ,reject updateRusNativeData");
            return;
        }
        try {
            String session = String.valueOf(SystemClock.uptimeMillis());
            try {
                this.mHealthMonitor.start(HealthState.UPDATE_RUS_NATIVE_DATA, 5000, session);
                this.mDaemon.updateRusNativeData(this.mFaceSwitchHelper.getRusNativeData().mHacknessThreshold);
            } finally {
                this.mHealthMonitor.stop(HealthState.UPDATE_RUS_NATIVE_DATA, session);
            }
        } catch (RemoteException e) {
            LogUtil.e(TAG, "updateRusNativeData failed", e);
        }
    }

    private class BiometricPromptServiceListenerImpl extends BiometricServiceBase.BiometricServiceListener {
        BiometricPromptServiceListenerImpl(IBiometricServiceReceiverInternal wrapperReceiver) {
            super(wrapperReceiver);
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onAcquired(FaceManager.getMappedAcquiredInfo(acquiredInfo, vendorCode), FaceManager.getAcquiredString(FaceService.this.getContext(), acquiredInfo, vendorCode));
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onError(long deviceId, int error, int vendorCode, int cookie) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onError(cookie, error, FaceManager.getErrorString(FaceService.this.getContext(), error, vendorCode));
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.BiometricServiceListener, com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationSucceededInternal(boolean requireConfirmation, byte[] token) throws RemoteException {
            LogUtil.d(FaceService.TAG, "Receiver onAuthenticationSucceededInternal requireConfirmation:" + requireConfirmation);
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onAuthenticationSucceeded(requireConfirmation, token);
            }
            FaceService.this.cancelRecognition();
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.BiometricServiceListener, com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationFailedInternal(int cookie, boolean requireConfirmation) throws RemoteException {
            LogUtil.d(FaceService.TAG, "Receiver onAuthenticationSucceededInternal requireConfirmation:" + requireConfirmation + " cookie:" + cookie);
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onAuthenticationFailed(cookie, requireConfirmation);
            }
            FaceService.this.cancelRecognition();
        }
    }

    /* access modifiers changed from: private */
    public class ServiceListenerImpl implements BiometricServiceBase.ServiceListener {
        private IFaceServiceReceiver mFaceServiceReceiver;

        public ServiceListenerImpl(IFaceServiceReceiver receiver) {
            this.mFaceServiceReceiver = receiver;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onEnrollResult(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            if (remaining == 0) {
                FaceService.this.cancelRecognition();
            }
            if (this.mFaceServiceReceiver != null) {
                LogUtil.d(FaceService.TAG, "Receiver onEnrollResult, remaining = " + remaining);
                this.mFaceServiceReceiver.onEnrollResult(identifier.getDeviceId(), identifier.getBiometricId(), remaining);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
            if (this.mFaceServiceReceiver != null) {
                LogUtil.d(FaceService.TAG, "Receiver onAcquired, acquiredInfo = " + acquiredInfo);
                this.mFaceServiceReceiver.onAcquired(deviceId, acquiredInfo, vendorCode);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationSucceeded(long deviceId, BiometricAuthenticator.Identifier biometric, int userId) throws RemoteException {
            if (this.mFaceServiceReceiver != null) {
                if (biometric == null || (biometric instanceof Face)) {
                    Log.d(FaceService.TAG, "Receiver onAuthenticationSucceeded");
                    this.mFaceServiceReceiver.onAuthenticationSucceeded(deviceId, (Face) biometric, userId);
                } else {
                    Log.e(FaceService.TAG, "onAuthenticationSucceeded received non-face biometric");
                }
            }
            FaceService.this.cancelRecognition();
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationFailed(long deviceId) throws RemoteException {
            if (this.mFaceServiceReceiver != null) {
                Log.d(FaceService.TAG, "Receiver onAuthenticationFailed");
                this.mFaceServiceReceiver.onAuthenticationFailed(deviceId);
            }
            FaceService.this.cancelRecognition();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean onProgressChanged(int progressInfo) {
            if (this.mFaceServiceReceiver == null) {
                return true;
            }
            try {
                Log.d(FaceService.TAG, "Receiver onProgressChanged");
                this.mFaceServiceReceiver.onProgressChanged(FaceService.this.mHalDeviceId, progressInfo);
                return false;
            } catch (RemoteException e) {
                LogUtil.w(FaceService.TAG, "Failed to invoke sendProcess:", e);
                return true;
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onError(long deviceId, int error, int vendorCode, int cookie) throws RemoteException {
            if (this.mFaceServiceReceiver != null) {
                LogUtil.w(FaceService.TAG, "Receiver onError = " + error);
                this.mFaceServiceReceiver.onError(deviceId, error, vendorCode);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            if (this.mFaceServiceReceiver != null) {
                LogUtil.d(FaceService.TAG, "Receiver onRemoved, remaining = " + remaining);
                this.mFaceServiceReceiver.onRemoved(identifier.getDeviceId(), identifier.getBiometricId(), remaining);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onEnumerated(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            if (this.mFaceServiceReceiver != null) {
                LogUtil.d(FaceService.TAG, "Receiver onEnumerated, remaining = " + remaining);
                this.mFaceServiceReceiver.onEnumerated(identifier.getDeviceId(), identifier.getBiometricId(), remaining);
            }
        }
    }

    public FaceService(Context context) {
        super(context);
        boolean z = false;
        this.mSupportPowerBlock = false;
        this.mEnrollmentTotalTimes = 0;
        this.mDaemonWrapper = null;
        this.mFaceUnlockEnabled = false;
        this.mFaceUnlockAutoWhenScreenOn = false;
        this.mFingerPrintUnlockEnabled = false;
        this.mFingerPrintShowWhenScreenOff = false;
        this.mFaceFingerprintCombineUnlockEnabled = false;
        this.mFaceCloseEyeDetect = -1;
        this.mWakeUpReason = null;
        this.mIsWakeUp = true;
        this.mServiceListenerImpl = null;
        this.mIsLogOpened = false;
        this.mIsImageSaveEnable = false;
        this.mIsScreenOff = false;
        this.mFaceUtils = FaceUtils.getInstance();
        this.mLockoutReceiver = new BroadcastReceiver() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass4 */

            public void onReceive(Context context, Intent intent) {
                if (FaceService.ACTION_LOCKOUT_RESET.equals(intent.getAction())) {
                    LogUtil.v(FaceService.TAG, "onReceive");
                    FaceService.this.mHandler.post(new ResetRunnable(intent));
                }
            }
        };
        this.mResetFailedAttemptsRunnable = new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass5 */

            public void run() {
                FaceService.this.resetFailedAttemptsForUser(ActivityManager.getCurrentUser());
            }
        };
        this.mDecreaseFailedAttemptsRunnable = new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass6 */

            public void run() {
                FaceService.this.decreaseFailedAttemptsForUser(ActivityManager.getCurrentUser());
            }
        };
        this.mLockDeadLineArray = new SparseLongArray();
        this.mListener = new Ilistener() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass14 */

            @Override // com.android.server.biometrics.face.setting.Ilistener
            public void onSettingChanged(String settingName, boolean isOn) {
                LogUtil.d(FaceService.TAG, "onSettingChanged, settingName = " + settingName + ", isOn = " + isOn);
                if (FaceSettings.FACE_UNLOCK_SWITCH.equals(settingName)) {
                    FaceService.this.mFaceUnlockEnabled = isOn;
                    FaceService.this.mFaceUnlockAutoWhenScreenOn = isOn;
                } else if (FaceSettings.FACE_CLOSE_EYE_DETECT_SWITCH.equals(settingName) && FaceService.this.mFaceCloseEyeDetectSupport) {
                    FaceService.this.mFaceCloseEyeDetect = isOn ? 1 : 0;
                    FaceService faceService = FaceService.this;
                    faceService.startUpdateSettingSwitchStatus(1, faceService.mFaceCloseEyeDetect);
                } else if ("coloros_fingerprint_unlock_switch".equals(settingName)) {
                    FaceService.this.mFingerPrintUnlockEnabled = isOn;
                } else if ("show_fingerprint_when_screen_off".equals(settingName)) {
                    FaceService.this.mFingerPrintShowWhenScreenOff = isOn;
                } else if (FaceSettings.FACE_FINGERPRINT_COMBINATION_UNLOCK_SWITCH.equals(settingName)) {
                    FaceService.this.mFaceFingerprintCombineUnlockEnabled = isOn;
                }
            }
        };
        this.mFaceConstants = new FaceConstants();
        LogUtil.d(TAG, "FaceService");
        this.mContext = context;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        this.mContext.registerReceiver(this.mLockoutReceiver, new IntentFilter(ACTION_LOCKOUT_RESET), "android.permission.RESET_FACE_LOCKOUT", null);
        Watchdog.getInstance().addMonitor(this);
        this.mServiceThread = new ServiceThread("FaceServiceMain", -2, true);
        this.mFaceServiceUnblockThread = new ServiceThread("FaceServiceSub", -2, true);
        this.mHandleResultThread = new ServiceThread("FaceSerResult", -2, true);
        this.mServiceThread.start();
        this.mFaceServiceUnblockThread.start();
        this.mHandleResultThread.start();
        initHandler();
        this.mHealthMonitor = HealthMonitor.getHealthMonitor(context, this.mSubHandler);
        this.mDcsUtil = DcsUtil.getDcsUtil(context);
        FacePowerManager.initFacePms(this.mContext, this);
        this.mFaceCloseEyeDetectSupport = context.getPackageManager().hasSystemFeature(FEATURE_FACE_CLOSEEYE_DETECT);
        this.mFaceSupport = context.getPackageManager().hasSystemFeature("android.hardware.biometrics.face");
        this.mFingerprintOpticalSupport = context.getPackageManager().hasSystemFeature("oppo.hardware.fingerprint.optical.support");
        this.mMotorSupport = context.getPackageManager().hasSystemFeature(FEATURE_MOTOR);
        this.mUnlockSettingMonitor = new UnlockSettingMonitor(this.mContext, this.mListener, this.mServiceThread.getLooper());
        this.mFaceSwitchHelper = new FaceSwitchHelper(context, this);
        SystemProperties.set(PROP_FACE_POWER_BLOCK, "");
        this.mSupportPowerBlock = ((!this.mMotorSupport && !context.getPackageManager().hasSystemFeature(FEATURE_NO_POWER_BLOCK)) || SystemProperties.get(PROP_FACE_POWER_BLOCK, "").equals("on")) ? true : z;
        this.mKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        this.mFailedAttempts = new SparseIntArray();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: com.android.server.biometrics.face.FaceService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.biometrics.face.FaceService$FaceServiceWrapper, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService, com.android.server.biometrics.BiometricServiceBase
    public void onStart() {
        super.onStart();
        publishBinderService("face", new FaceServiceWrapper());
        SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.biometrics.face.$$Lambda$FaceService$0tsTTDLYSxqAAH06d26qJ1aoLx0 */

            public final void run() {
                IBiometricsFace unused = FaceService.this.getFaceDaemon();
            }
        }, "FaceService.Main.onStart");
        LogUtil.d(TAG, "onStart");
        int facedPid = this.mHealthMonitor.getDemoProcessPid(HealthMonitor.FACED_NATIVE_NAME);
        if (facedPid != -1) {
            this.mHealthMonitor.demoProcessSystemReady(facedPid);
        }
        SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.biometrics.face.$$Lambda$FaceService$w0dhbu4K4dV6Ik8g3gFEpOkQJ00 */

            public final void run() {
                FaceService.this.startHidlService();
            }
        }, "FaceService.Main.startHidlService");
        publishLocalService(FaceInternal.class, getFacePms().getFaceLocalService());
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricServiceBase.DaemonWrapper getDaemonWrapper() {
        return this.mDaemonWrapper;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricUtils getBiometricUtils() {
        return FaceUtils.getInstance();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public Constants getConstants() {
        return this.mFaceConstants;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasReachedEnrollmentLimit(int userId) {
        if (getEnrolledTemplates(userId).size() < getContext().getResources().getInteger(17694812)) {
            return false;
        }
        Log.w(TAG, "Too many faces registered, user: " + userId);
        return true;
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public void serviceDied(long cookie) {
        super.serviceDied(cookie);
        LogUtil.d(TAG, "faced died");
        this.mHealthMonitor.notifyDemoProcessDied();
        BiometricServiceBase.DaemonWrapper daemonWrapper = this.mDaemonWrapper;
        if (daemonWrapper != null) {
            ((FaceDaemonWrapper) daemonWrapper).serviceDied();
        }
        this.mDaemonWrapper = null;
        unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_ERROR);
        handleError(this.mHalDeviceId, 1, 0);
        this.mDaemon = null;
        this.mCurrentUserId = -10000;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void updateActiveGroup(int userId, String clientPackage) {
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon != null) {
            try {
                int userId2 = getUserOrWorkProfileId(clientPackage, userId);
                if (userId2 != this.mCurrentUserId) {
                    File faceDir = new File(Environment.getDataVendorDeDirectory(userId2), FACE_DATA_DIR);
                    if (!faceDir.exists()) {
                        if (!faceDir.mkdir()) {
                            Log.v(TAG, "Cannot make directory: " + faceDir.getAbsolutePath());
                            return;
                        } else if (!SELinux.restorecon(faceDir)) {
                            Log.w(TAG, "Restorecons failed. Directory will have wrong label.");
                            return;
                        }
                    }
                    LogUtil.w(TAG, "updateActiveGroup setActiveGroup userId = " + userId2 + ", clientPackage = " + clientPackage, new Throwable());
                    String session = String.valueOf(SystemClock.uptimeMillis());
                    try {
                        this.mHealthMonitor.start(HealthState.SETACTIVEUSER, 5000, session);
                        daemon.setActiveUser(userId2, faceDir.getAbsolutePath());
                        this.mHealthMonitor.stop(HealthState.SETACTIVEUSER, session);
                        this.mCurrentUserId = userId2;
                    } catch (Throwable th) {
                        this.mHealthMonitor.stop(HealthState.SETACTIVEUSER, session);
                        throw th;
                    }
                }
                String session2 = String.valueOf(SystemClock.uptimeMillis());
                try {
                    this.mHealthMonitor.start("getAuthenticatorId", 5000, session2);
                    long currentAuthenticatorId = daemon.getAuthenticatorId();
                    this.mHealthMonitor.stop("getAuthenticatorId", session2);
                    this.mAuthenticatorIds.put(Integer.valueOf(userId2), Long.valueOf(hasEnrolledBiometrics(userId2) ? currentAuthenticatorId : 0));
                    LogUtil.d(TAG, "updateActiveGroup mCurrentAuthenticatorId.value = " + currentAuthenticatorId);
                } catch (Throwable th2) {
                    this.mHealthMonitor.stop("getAuthenticatorId", session2);
                    throw th2;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to setActiveUser():", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getLockoutResetIntent() {
        return ACTION_LOCKOUT_RESET;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getLockoutBroadcastPermission() {
        return "android.permission.RESET_FACE_LOCKOUT";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void handleUserSwitching(int userId) {
        super.handleUserSwitching(userId);
        this.mCurrentUserLockoutMode = 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void enumerateInternal(EnumerateClient client) {
        this.mHandler.post(new Runnable(client) {
            /* class com.android.server.biometrics.face.$$Lambda$FaceService$SZKX_T1zEfLKa8CCbIgknzEJJs */
            private final /* synthetic */ EnumerateClient f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FaceService.this.lambda$enumerateInternal$2$FaceService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$enumerateInternal$2$FaceService(EnumerateClient client) {
        startClient(client, true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasEnrolledBiometrics(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        return getBiometricUtils().getBiometricsForUser(getContext(), userId).size() > 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getManageBiometricPermission() {
        return "android.permission.MANAGE_BIOMETRIC";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void checkUseBiometricPermission() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean checkAppOps(int uid, String opPackageName) {
        return this.mAppOps.noteOp(78, uid, opPackageName) == 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public List<Face> getEnrolledTemplates(int userId) {
        return getBiometricUtils().getBiometricsForUser(getContext(), userId);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void notifyClientActiveCallbacks(boolean isActive) {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int statsModality() {
        return 4;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int getLockoutMode() {
        int failedAttempts = this.mFailedAttempts.get(ActivityManager.getCurrentUser(), 0);
        if (failedAttempts >= 20) {
            return 2;
        }
        if (failedAttempts <= 0 || failedAttempts % 5 != 0) {
            return 0;
        }
        return 1;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public synchronized IBiometricsFace getFaceDaemon() {
        if (this.mDaemon == null) {
            Log.v(TAG, "mDaemon was null, reconnect to face");
            try {
                this.mDaemon = IBiometricsFace.getService();
            } catch (NoSuchElementException e) {
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to get biometric interface", e2);
            }
            if (this.mDaemon == null) {
                Log.w(TAG, "face HIDL not available");
                return null;
            }
            String session = String.valueOf(SystemClock.uptimeMillis());
            try {
                this.mHealthMonitor.start(HealthState.LINK_TO_DEATH, 5000, session);
                this.mDaemon.asBinder().linkToDeath(this, 0);
                try {
                    if (this.mDaemonWrapper == null) {
                        FaceHypnus.getInstance().hypnusSpeedUp(2500);
                        this.mDaemonWrapper = new FaceDaemonWrapper(this.mDaemon, this.mHealthMonitor, this.mContext, this);
                    }
                    String session2 = String.valueOf(SystemClock.uptimeMillis());
                    try {
                        this.mHealthMonitor.start(HealthState.SET_CALL_BACK, 5000, session2);
                        if (this.mDaemon != null) {
                            this.mHalDeviceId = this.mDaemon.setCallback(((FaceDaemonWrapper) this.mDaemonWrapper).getFaceDaemonCallback());
                        }
                    } finally {
                        this.mHealthMonitor.stop(HealthState.SET_CALL_BACK, session2);
                    }
                } catch (RemoteException e3) {
                    Log.e(TAG, "Failed to open face HAL", e3);
                    this.mDaemon = null;
                }
                Log.v(TAG, "Face HAL id: " + this.mHalDeviceId);
                if (this.mHalDeviceId != 0) {
                    loadAuthenticatorIds();
                    updateActiveGroup(ActivityManager.getCurrentUser(), null);
                    doTemplateCleanupForUser(ActivityManager.getCurrentUser());
                    try {
                        if (this.mDaemon != null) {
                            String session3 = String.valueOf(SystemClock.uptimeMillis());
                            try {
                                this.mHealthMonitor.start("getEnrollmentTotalTimes", 5000, session3);
                                this.mEnrollmentTotalTimes = this.mDaemon.getEnrollmentTotalTimes();
                                this.mHealthMonitor.stop("getEnrollmentTotalTimes", session3);
                                String session4 = String.valueOf(SystemClock.uptimeMillis());
                                try {
                                    this.mHealthMonitor.start(HealthState.UPDATE_RUS_NATIVE_DATA, 5000, session4);
                                    this.mDaemon.updateRusNativeData(this.mFaceSwitchHelper.getRusNativeData().mHacknessThreshold);
                                    this.mHealthMonitor.stop(HealthState.UPDATE_RUS_NATIVE_DATA, session4);
                                    String session5 = String.valueOf(SystemClock.uptimeMillis());
                                    try {
                                        this.mHealthMonitor.start(HealthState.UPDATE_SETTING_SWITCH_STATUS, 5000, session5);
                                        boolean z = false;
                                        if (this.mFaceCloseEyeDetect != -1) {
                                            IBiometricsFace iBiometricsFace = this.mDaemon;
                                            if (this.mFaceCloseEyeDetect == 1) {
                                                z = true;
                                            }
                                            iBiometricsFace.updateSettingSwitchStatus(1, z);
                                        } else {
                                            LogUtil.d(TAG, "Can not get the value of coloros_face_unlock_eyes_condition, set it to false.");
                                            this.mDaemon.updateSettingSwitchStatus(1, false);
                                        }
                                    } finally {
                                        this.mHealthMonitor.stop(HealthState.UPDATE_SETTING_SWITCH_STATUS, session5);
                                    }
                                } catch (Throwable th) {
                                    this.mHealthMonitor.stop(HealthState.UPDATE_RUS_NATIVE_DATA, session4);
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                this.mHealthMonitor.stop("getEnrollmentTotalTimes", session3);
                                throw th2;
                            }
                        }
                    } catch (RemoteException e4) {
                        LogUtil.e(TAG, "something wrong with mDaemon", e4);
                        this.mDaemon = null;
                    }
                } else {
                    Log.w(TAG, "Failed to open Face HAL!");
                    MetricsLogger.count(getContext(), "faced_openhal_error", 1);
                    this.mDaemon = null;
                }
            } finally {
                this.mHealthMonitor.stop(HealthState.LINK_TO_DEATH, session);
            }
        }
        return this.mDaemon;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long startGenerateChallenge(IBinder token) {
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon == null) {
            Log.w(TAG, "startGenerateChallenge: no face HAL!");
            return 0;
        }
        try {
            LogUtil.d(TAG, "startGenerateChallenge");
            String session = String.valueOf(SystemClock.uptimeMillis());
            try {
                this.mHealthMonitor.start(HealthState.GENERATECHALLENGE, 5000, session);
                return daemon.generateChallenge(600);
            } finally {
                this.mHealthMonitor.stop(HealthState.GENERATECHALLENGE, session);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "startGenerateChallenge failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int startRevokeChallenge(IBinder token) {
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon == null) {
            Log.w(TAG, "startRevokeChallenge: no face HAL!");
            return 0;
        }
        try {
            LogUtil.d(TAG, "startRevokeChallenge");
            String session = String.valueOf(SystemClock.uptimeMillis());
            try {
                this.mHealthMonitor.start(HealthState.GENERATECHALLENGE, 5000, session);
                return daemon.revokeChallenge();
            } finally {
                this.mHealthMonitor.stop(HealthState.GENERATECHALLENGE, session);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "startRevokeChallenge failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpInternal(PrintWriter pw) {
        JSONObject dump = new JSONObject();
        try {
            dump.put(IColorAppStartupManager.TYPE_SERVICE, "Face Manager");
            JSONArray sets = new JSONArray();
            for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                int userId = user.getUserHandle().getIdentifier();
                int N = getBiometricUtils().getBiometricsForUser(getContext(), userId).size();
                BiometricServiceBase.PerformanceStats stats = (BiometricServiceBase.PerformanceStats) this.mPerformanceMap.get(Integer.valueOf(userId));
                BiometricServiceBase.PerformanceStats cryptoStats = (BiometricServiceBase.PerformanceStats) this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
                JSONObject set = new JSONObject();
                set.put("id", userId);
                set.put("count", N);
                set.put("accept", stats != null ? stats.accept : 0);
                set.put("reject", stats != null ? stats.reject : 0);
                set.put("acquire", stats != null ? stats.acquire : 0);
                set.put("lockout", stats != null ? stats.lockout : 0);
                set.put("permanentLockout", stats != null ? stats.permanentLockout : 0);
                set.put("acceptCrypto", cryptoStats != null ? cryptoStats.accept : 0);
                set.put("rejectCrypto", cryptoStats != null ? cryptoStats.reject : 0);
                set.put("acquireCrypto", cryptoStats != null ? cryptoStats.acquire : 0);
                set.put("lockoutCrypto", cryptoStats != null ? cryptoStats.lockout : 0);
                set.put("permanentLockoutCrypto", cryptoStats != null ? cryptoStats.permanentLockout : 0);
                sets.put(set);
            }
            dump.put("prints", sets);
        } catch (JSONException e) {
            Log.e(TAG, "dump formatting failure", e);
        }
        pw.println(dump);
        pw.println("HAL Deaths: " + this.mHALDeathCount);
        this.mHALDeathCount = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        for (UserInfo user : UserManager.get(getContext()).getUsers()) {
            int userId = user.getUserHandle().getIdentifier();
            long userToken = proto.start(2246267895809L);
            proto.write(1120986464257L, userId);
            proto.write(1120986464258L, getBiometricUtils().getBiometricsForUser(getContext(), userId).size());
            BiometricServiceBase.PerformanceStats normal = (BiometricServiceBase.PerformanceStats) this.mPerformanceMap.get(Integer.valueOf(userId));
            if (normal != null) {
                long countsToken = proto.start(1146756268035L);
                proto.write(1120986464257L, normal.accept);
                proto.write(1120986464258L, normal.reject);
                proto.write(1120986464259L, normal.acquire);
                proto.write(1120986464260L, normal.lockout);
                proto.write(1120986464261L, normal.lockout);
                proto.end(countsToken);
            }
            BiometricServiceBase.PerformanceStats crypto = (BiometricServiceBase.PerformanceStats) this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
            if (crypto != null) {
                long countsToken2 = proto.start(1146756268036L);
                proto.write(1120986464257L, crypto.accept);
                proto.write(1120986464258L, crypto.reject);
                proto.write(1120986464259L, crypto.acquire);
                proto.write(1120986464260L, crypto.lockout);
                proto.write(1120986464261L, crypto.lockout);
                proto.end(countsToken2);
            }
            proto.end(userToken);
        }
        proto.flush();
        this.mPerformanceMap.clear();
        this.mCryptoPerformanceMap.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpHal(FileDescriptor fd, String[] args) {
        IBiometricsFace daemon;
        if ((Build.IS_ENG || Build.IS_USERDEBUG) && !SystemProperties.getBoolean("ro.face.disable_debug_data", false) && !SystemProperties.getBoolean("persist.face.disable_debug_data", false) && (daemon = getFaceDaemon()) != null) {
            FileOutputStream devnull = new FileOutputStream("/dev/null");
            NativeHandle handle = new NativeHandle(new FileDescriptor[]{devnull.getFD(), fd}, new int[0], false);
            String session = String.valueOf(SystemClock.uptimeMillis());
            try {
                this.mHealthMonitor.start(HealthState.DEBUG, 5000, session);
                daemon.debug(handle, new ArrayList<>(Arrays.asList(args)));
                try {
                    try {
                        devnull.close();
                    } catch (IOException e) {
                    }
                } catch (RemoteException | IOException ex) {
                    Log.d(TAG, "error while reading face debugging data", ex);
                    if (devnull != null) {
                        devnull.close();
                    }
                } catch (Throwable th) {
                    if (devnull != null) {
                        try {
                            devnull.close();
                        } catch (IOException e2) {
                        }
                    }
                    throw th;
                }
            } finally {
                this.mHealthMonitor.stop(HealthState.DEBUG, session);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpInternal(PrintWriter pw, String[] args) {
        String opt;
        if (args.length != 2 || !"<face daemon started>".equals(args[0])) {
            int opti = 0;
            while (opti < args.length && (opt = args[opti]) != null && opt.length() > 0 && opt.charAt(0) == '-') {
                opti++;
                if ("-h".equals(opt)) {
                    if (!IS_REALEASE_VERSION) {
                        pw.println("face service dump options:");
                        pw.println("  [-h] [cmd] ...");
                        pw.println("  cmd may be one of:");
                        pw.println("    l[log]: dynamically adjust face log ");
                        pw.println("    -camera iso=auto,exposure=4  ,  adjust camera parameters ");
                        pw.println("    -enrollTimeout 100  ,  adjust enroll timeout ");
                        pw.println("    -autoVerify 1/0  ,  adjust hacker funtion ");
                        return;
                    }
                    return;
                } else if ("-camera".equals(opt)) {
                    if (!Utils.canCatchLog()) {
                        return;
                    }
                    if (opti < args.length) {
                        String parameterArgs = args[opti];
                        int opti2 = opti + 1;
                        if (parameterArgs == null) {
                            return;
                        }
                        if (parameterArgs.equals("dump")) {
                            mCanDumpCameraMessageQueue = true;
                            return;
                        } else if (parameterArgs.equals("notdump")) {
                            mCanDumpCameraMessageQueue = false;
                            return;
                        } else if (parameterArgs.equals("add")) {
                            mCanAddCameraMessageToQueue = true;
                            return;
                        } else if (parameterArgs.equals("notadd")) {
                            mCanAddCameraMessageToQueue = false;
                            return;
                        } else if (parameterArgs.equals("remove")) {
                            mCanRemoveCameraMessageFromQueue = true;
                            return;
                        } else if (parameterArgs.equals("notremove")) {
                            mCanRemoveCameraMessageFromQueue = false;
                            return;
                        } else {
                            return;
                        }
                    } else {
                        pw.println("ERROR: Camera parameters is missing.");
                        return;
                    }
                } else if ("-enrollTimeout".equals(opt)) {
                    if (Utils.canCatchLog()) {
                        if (opti < args.length) {
                            String enrollTimeout = args[opti];
                            int opti3 = opti + 1;
                            return;
                        }
                        pw.println("ERROR: enroll timeout parameter is missing.");
                        return;
                    }
                    return;
                } else if ("-autoVerify".equals(opt)) {
                    if (Utils.canCatchLog()) {
                        if (opti < args.length) {
                            String on = args[opti];
                            int opti4 = opti + 1;
                            return;
                        }
                        pw.println("ERROR: autoVeify parameters is missing.");
                        return;
                    }
                    return;
                } else if ("-dropFrames".equals(opt)) {
                    if (Utils.canCatchLog()) {
                        if (opti < args.length) {
                            String frames = args[opti];
                            int opti5 = opti + 1;
                            return;
                        }
                        pw.println("ERROR: dropFrames parameters is missing.");
                        return;
                    }
                    return;
                } else if ("-powerblock".equals(opt)) {
                    if (opti < args.length) {
                        String on2 = args[opti];
                        int opti6 = opti + 1;
                        SystemProperties.set(PROP_FACE_POWER_BLOCK, on2);
                        this.mSupportPowerBlock = "on".equals(on2);
                        return;
                    }
                    pw.println("ERROR: powerblock parameters is missing.");
                    return;
                } else if ("-perf".equals(opt)) {
                    if (opti < args.length) {
                        String on3 = args[opti];
                        int opti7 = opti + 1;
                        DEBUG_PERF = "on".equals(on3);
                        updateDynamicallyLog(3, DEBUG_PERF);
                        if (!DEBUG_PERF) {
                            SystemProperties.set(PROP_FACE_PERFORMANCE_HIDE, on3);
                            return;
                        }
                        return;
                    }
                    pw.println("ERROR: parameters is missing.");
                    return;
                } else if (!"-reason".equals(opt)) {
                    pw.println("Unknown argument: " + opt + "; use -h for help");
                } else if (opti < args.length) {
                    String reason = args[opti];
                    int opti8 = opti + 1;
                    SystemProperties.set(PROP_FACE_FEEDBACK_REASON, reason);
                    return;
                } else {
                    pw.println("ERROR: parameters is missing.");
                    return;
                }
            }
            if (opti < args.length) {
                String cmd = args[opti];
                int opti9 = opti + 1;
                if ("log".equals(cmd) || "l".equals(cmd)) {
                    dynamicallyConfigLogTag(pw, args);
                    return;
                } else if ("debug_switch".equals(cmd)) {
                    pw.println("  all=" + Utils.canCatchLog());
                    return;
                } else if ("reset".equals(cmd)) {
                    int pid = this.mHealthMonitor.getDemoProcessPid(HealthMonitor.FACED_NATIVE_NAME);
                    if (pid != -1) {
                        Process.sendSignal(pid, 3);
                        return;
                    }
                    return;
                }
            }
            pw.println("Utils.canCatchLog() = " + Utils.canCatchLog());
            pw.println("DEBUG = true");
            pw.println("DEBUG_PERF = " + DEBUG_PERF);
            pw.println("LogLevel : " + LogUtil.getLevelString());
            pw.println("mFaceSupport = " + this.mFaceSupport);
            pw.println("mSupportPowerBlock = " + this.mSupportPowerBlock);
            pw.println("mFailedAttempts = " + this.mFailedAttempts);
            pw.println("mHalDeviceId = " + this.mHalDeviceId);
            pw.println("mFaceUnlockEnabled = " + this.mFaceUnlockEnabled);
            pw.println("mFaceUnlockAutoWhenScreenOn = " + this.mFaceUnlockAutoWhenScreenOn);
            pw.println("mFingerPrintUnlockEnabled = " + this.mFingerPrintUnlockEnabled);
            pw.println("mFingerPrintShowWhenScreenOff = " + this.mFingerPrintShowWhenScreenOff);
            pw.println("mFaceFingerprintCombineUnlockEnabled = " + this.mFaceFingerprintCombineUnlockEnabled);
            pw.println("mFaceCloseEyeDetect = " + this.mFaceCloseEyeDetect);
            pw.println("isKeyguardLocked = " + this.mKeyguardManager.isKeyguardLocked());
            pw.println("mFaceUtils dump");
            if (Utils.canCatchLog()) {
                this.mFaceSwitchHelper.dump(pw, args, "");
            }
            JSONObject jsonObj = new JSONObject();
            try {
                JSONArray sets = new JSONArray();
                for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                    int userId = user.getUserHandle().getIdentifier();
                    int N = this.mFaceUtils.getBiometricsForUser(this.mContext, userId).size();
                    JSONObject set = new JSONObject();
                    set.put("id", userId);
                    set.put("count", N);
                    sets.put(set);
                }
                jsonObj.put("prints", sets);
            } catch (JSONException e) {
                LogUtil.e(TAG, "jsonObj formatting failure", e);
            }
            pw.println(jsonObj);
            return;
        }
        LogUtil.d(TAG, "received faced started msg, pid = " + args[1]);
        this.mHandler.post(new Runnable() {
            /* class com.android.server.biometrics.face.FaceService.AnonymousClass18 */

            public void run() {
                if (FaceService.this.getFaceDaemon() == null) {
                    LogUtil.e(FaceService.TAG, "dumpInner: no faced!");
                }
            }
        });
        this.mHealthMonitor.demoProcessSystemReady(Integer.parseInt(args[1]));
    }

    /* access modifiers changed from: protected */
    public void dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        boolean logOn = false;
        boolean isImageSaveOn = false;
        boolean needUpdateLog = false;
        boolean needUpdateImage = false;
        pw.println("dynamicallyConfigLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]: " + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
        } else if ("all".equals(args[1])) {
            if ("0".equals(args[2])) {
                logOn = false;
                isImageSaveOn = false;
                needUpdateLog = true;
                needUpdateImage = true;
            } else if (NoFocusWindow.HUNG_CONFIG_ENABLE.equals(args[2])) {
                logOn = true;
                isImageSaveOn = true;
                needUpdateLog = true;
                needUpdateImage = true;
            } else if ("2".equals(args[2])) {
                isImageSaveOn = true;
                needUpdateImage = true;
            } else if ("3".equals(args[2])) {
                isImageSaveOn = false;
                needUpdateImage = true;
            }
            if (needUpdateLog && this.mIsLogOpened != logOn) {
                this.mIsLogOpened = logOn;
                updateDynamicallyLog(1, logOn);
            }
            if ((needUpdateLog || needUpdateImage) && !IS_REALEASE_VERSION && isImageSaveOn != this.mIsImageSaveEnable) {
                this.mIsImageSaveEnable = isImageSaveOn;
                updateDynamicallyLog(2, this.mIsImageSaveEnable);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1. open all log in FaceService");
        pw.println("cmd: dumpsys face log all 0/1/");
        pw.println("0: close the face detect log");
        pw.println("1: open the face detect log");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    private void updateDynamicallyLog(int type, boolean isOn) {
        int i = 1;
        if (type == 1) {
            LogUtil.dynamicallyConfigLog(isOn);
        }
        IBiometricsFace daemon = getFaceDaemon();
        if (daemon != null) {
            try {
                String session = String.valueOf(SystemClock.uptimeMillis());
                try {
                    this.mHealthMonitor.start("dynamicallyConfigLog", 5000, session);
                    if (!isOn) {
                        i = 0;
                    }
                    daemon.dynamicallyConfigLog(type, i);
                } finally {
                    this.mHealthMonitor.stop("dynamicallyConfigLog", session);
                }
            } catch (RemoteException e) {
                LogUtil.e(TAG, "dynamicallyConfigLog failed", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getEffectiveUserId(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um != null) {
            long callingIdentity = Binder.clearCallingIdentity();
            int userId2 = um.getCredentialOwnerProfile(userId);
            Binder.restoreCallingIdentity(callingIdentity);
            return userId2;
        }
        LogUtil.e(TAG, "Unable to acquire UserManager");
        return userId;
    }

    /* access modifiers changed from: protected */
    public int getCurrentUserId() {
        try {
            return getEffectiveUserId(ActivityManagerNative.getDefault().getCurrentUser().id);
        } catch (RemoteException e) {
            LogUtil.w(TAG, "Failed to get current user id\n");
            return -10000;
        }
    }

    /* access modifiers changed from: protected */
    public void handleSyncTemplates(long deviceId, int[] faceIds, int userId) {
        int faceNumber;
        LogUtil.w(TAG, "handleSyncTemplates");
        if (faceIds == null) {
            LogUtil.w(TAG, "faceIds and userId are null");
            return;
        }
        int remainingTemplates = faceIds.length;
        if (userId != getCurrentUserId()) {
            LogUtil.w(TAG, "template is not beylong to the current user");
            return;
        }
        List<Face> faceNumb = getEnrolledTemplates(userId);
        ArrayList<Integer> faceIdList = new ArrayList<>();
        if (faceNumb != null) {
            int faceNumber2 = faceNumb.size();
            for (int i = 0; i < faceNumb.size(); i++) {
                faceIdList.add(Integer.valueOf(faceNumb.get(i).getBiometricId()));
            }
            faceNumber = faceNumber2;
        } else {
            faceNumber = 0;
        }
        LogUtil.d(TAG, "syncTemplates started, remainingTemplates = " + remainingTemplates + ", fingerprintNum = " + faceNumber + ", userId = " + userId);
        for (int j = 0; j < remainingTemplates; j++) {
            LogUtil.d(TAG, "faceIds[" + j + "] = " + faceIds[j]);
        }
        if (faceNumber != 0) {
            for (int i2 : faceIds) {
                if (!faceIdList.contains(Integer.valueOf(i2))) {
                    this.mFaceUtils.syncFaceIdForUser(this.mContext, faceIds, userId, deviceId);
                    return;
                }
            }
            return;
        }
        this.mFaceUtils.syncFaceIdForUser(this.mContext, faceIds, userId, deviceId);
    }
}
