package com.android.server.fingerprint;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.fingerprint.EngineeringInfo;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintInternal;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintDaemonCallback;
import android.hardware.fingerprint.IFingerprintService.Stub;
import android.hardware.fingerprint.IFingerprintServiceLockoutResetCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.Fingerprint.FingerprintSwitchHelper;
import com.android.server.Fingerprint.FingerprintSwitchHelper.ISwitchUpdateListener;
import com.android.server.LocationManagerService;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.fingerprint.keyguard.KeyguardPolicy;
import com.android.server.fingerprint.power.FingerprintPowerManager;
import com.android.server.fingerprint.sensor.IProximitySensorEventListener;
import com.android.server.fingerprint.sensor.ProximitySensorManager;
import com.android.server.fingerprint.setting.FingerprintSettings;
import com.android.server.fingerprint.setting.FingerprintUnlockSettingMonitor;
import com.android.server.fingerprint.setting.Ilistener;
import com.android.server.fingerprint.tool.ExHandler;
import com.android.server.fingerprint.touchmode.TouchEventMonitorMode;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.util.SupportUtil;
import com.android.server.fingerprint.wakeup.BackTouchSensorUnlockController;
import com.android.server.fingerprint.wakeup.IFingerprintSensorEventListener;
import com.android.server.fingerprint.wakeup.TouchSensorUnlockController;
import com.android.server.fingerprint.wakeup.UnlockController;
import com.android.server.oppo.IElsaManager;
import com.android.server.secrecy.policy.DecryptTool;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class FingerprintService extends SystemService implements DeathRecipient, Monitor, ISwitchUpdateListener {
    private static final String ACTION_LOCKOUT_RESET = "com.android.server.fingerprint.ACTION_LOCKOUT_RESET";
    public static boolean DEBUG = false;
    private static final int ENROLLMENT_TIMEOUT_MS = 60000;
    private static final long FAIL_LOCKOUT_TIMEOUT_MS = 30000;
    private static final String FINGERPRINTD = "android.hardware.fingerprint.IFingerprintDaemon";
    public static final int FINGERPRINT_HAL_MODE_ENROLL = 1;
    public static final int FINGERPRINT_HAL_MODE_IDENTIFY = 0;
    public static final int FINGERPRINT_HAL_MODE_LOCK = 3;
    public static final int FINGERPRINT_HAL_MODE_SELFTEST = 2;
    private static final String FP_DATA_DIR = "fpdata";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long MAX_TIMEOUT_WAITFOR_REBUILD_CONNECTION = 3000;
    private static final int MSG_DAEMON_CALLBACK = 11;
    private static final int MSG_MONITOR_EVENT_TRIGGERED = 15;
    private static final int MSG_SWITCH_INIT = 12;
    private static final int MSG_SWITCH_UPDATE = 13;
    private static final int MSG_UNCONSCIOUS_TOUCH_HAPPEND = 14;
    private static final int MSG_USER_SWITCHING = 10;
    private static final long MS_PER_SEC = 1000;
    private static final int OPERATION_AUTHENTICATE = 256;
    private static final int OPERATION_CANCEL_AUTHENTICATION = 257;
    private static final int OPERATION_CANCEL_ENROLLMENT = 259;
    private static final int OPERATION_CANCEL_TOUCH_EVENT = 276;
    private static final int OPERATION_CONTINUE_ENROLL = 265;
    private static final int OPERATION_CONTINUE_IDENTIFY = 275;
    private static final int OPERATION_ENROLL = 258;
    private static final int OPERATION_GET_ENROLLMENT_TOTALTIMES = 273;
    private static final int OPERATION_PAUSE_ENROLL = 264;
    private static final int OPERATION_PAUSE_IDENTIFY = 274;
    private static final int OPERATION_POST_ENROLL = 263;
    private static final int OPERATION_PRE_ENROLL = 262;
    private static final int OPERATION_REMOVE_FP = 260;
    private static final int OPERATION_RENAME_FP = 261;
    private static final int OPERATION_TOUCH_EVENT = 272;
    public static int SCREEN_STATE_OFF = 0;
    public static int SCREEN_STATE_ON = 0;
    public static final String TAG = "FingerprintService";
    public static final long TIMEOUT_GET_FINGERPRINTD = 10000;
    private final AlarmManager mAlarmManager;
    private final AppOpsManager mAppOps;
    private ClientMonitor mAuthClient;
    private Object mClientLock;
    private Context mContext;
    private long mCurrentAuthenticatorId;
    private int mCurrentUserId;
    private IFingerprintDaemon mDaemon;
    private IFingerprintDaemonCallback mDaemonCallback;
    private ConcurrentLinkedDeque<Runnable> mDaemonCallbackQueue;
    private IFingerprintDaemon mDaemonStub;
    private IFingerprintDaemon mDaemonWrapper;
    private DcsFingerprintStatisticsUtil mDcsStatisticsUtil;
    private ClientMonitor mEngineeringInfoClient;
    private ClientMonitor mEnrollClient;
    private int mEnrollmentTotalTimes;
    private int mFailedAttempts;
    private boolean mFingerprintEnabled;
    private FingerprintSwitchHelper mFingerprintSwitchHelper;
    private boolean mFingerprintUnlockEnabled;
    private FingerprintUnlockSettingMonitor mFingerprintUnlockSettingMonitor;
    private final FingerprintUtils mFingerprintUtils;
    private ClientMonitor mGlobalCycleClient;
    private long mHalDeviceId;
    private ExHandler mHandler;
    private HealthMonitor mHealthMonitor;
    private IProximitySensorEventListener mIProximitySensorEventListener;
    private IUnLocker mIUnLocker;
    private boolean mIsNearState;
    private boolean mIsRegistered;
    private final String mKeyguardPackage;
    private KeyguardPolicy mKeyguardPolicy;
    private Ilistener mListener;
    private LockPatternUtils mLockPatternUtils;
    private long mLockoutDeadline;
    private final ArrayList<FingerprintServiceLockoutResetMonitor> mLockoutMonitors;
    private final BroadcastReceiver mLockoutReceiver;
    private final PowerManager mPowerManager;
    private ClientMonitor mRemoveClient;
    private final Runnable mResetFailedAttemptsRunnable;
    private String mSensorType;
    private final ServiceThread mServiceThread;
    private boolean mSystemReady;
    private ClientMonitor mTouchClient;
    private TouchEventMonitorMode mTouchEventMonitorMode;
    private IFingerprintSensorEventListener mUnlockController;
    private final UserManager mUserManager;

    public interface IUnLocker {
        int continueIdentify();

        void dispatchMonitorEvent(int i, String str);

        void dispatchScreenOffAuthenticatedEvent(int i, int i2);

        void dispatchScreenOnAuthenticatedEvent(int i, int i2);

        void dispatchTouchEventInLockMode();

        void onScreenOff(boolean z);

        int pauseIdentify();

        void reauthentication();

        void sendUnlockTime(int i, long j);

        void setScreenState(int i);
    }

    public class AuthenticatedInfo {
        public int fingerId;
        public int groupId;

        public AuthenticatedInfo(int fid, int gid) {
            this.fingerId = fid;
            this.groupId = gid;
        }
    }

    public class ClientInfo {
        String mPackageName;
        int mPid;

        public ClientInfo(int pid, String packageName) {
            this.mPid = pid;
            this.mPackageName = packageName;
        }

        public String toString() {
            return "{ mPackageName = " + this.mPackageName + ", mPid = " + this.mPid + ", " + Integer.toHexString(System.identityHashCode(this)) + "}";
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum ClientMode {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.FingerprintService.ClientMode.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.FingerprintService.ClientMode.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.FingerprintService.ClientMode.<clinit>():void");
        }
    }

    private class ClientMonitor implements DeathRecipient {
        ClientMode clientMode = ClientMode.NONE;
        boolean isKeyguard;
        private int mGroupId;
        String packageName;
        int pid;
        IFingerprintServiceReceiver receiver;
        boolean restricted;
        IBinder token;
        int userId;

        public ClientMonitor(IBinder token, IFingerprintServiceReceiver receiver, int userId, int groupId, boolean restricted, int pid, String pkgName, ClientMode clientMode) {
            this.token = token;
            this.receiver = receiver;
            this.userId = userId;
            this.mGroupId = groupId;
            this.restricted = restricted;
            this.pid = pid;
            this.packageName = pkgName;
            this.clientMode = clientMode;
            this.isKeyguard = FingerprintService.this.mKeyguardPackage.equals(this.packageName);
            try {
                token.linkToDeath(this, 0);
            } catch (RemoteException e) {
                LogUtil.w(FingerprintService.TAG, "caught remote exception in linkToDeath: ", e);
            }
        }

        public void destroy() {
            synchronized (this) {
                if (this.token != null) {
                    try {
                        this.token.unlinkToDeath(this, 0);
                    } catch (NoSuchElementException e) {
                        LogUtil.e(FingerprintService.TAG, "destroy(): " + this + ":", new Exception("here"));
                    }
                    this.token = null;
                }
            }
            this.receiver = null;
            return;
        }

        public void binderDied() {
            FingerprintService.this.mDaemonCallbackQueue.addFirst(new Runnable() {
                public void run() {
                    LogUtil.d(FingerprintService.TAG, "binderDied, pid = " + ClientMonitor.this.pid + ", packageName = " + ClientMonitor.this.packageName);
                    if (this == FingerprintService.this.mAuthClient) {
                        FingerprintService.this.cleanUp();
                    }
                    ClientMonitor.this.token = null;
                    FingerprintService.this.removeClient(ClientMonitor.this);
                    FingerprintService.this.removeGlobalCycleClient(ClientMonitor.this);
                    ClientMonitor.this.receiver = null;
                }
            });
            if (!FingerprintService.this.mHandler.hasMessages(11)) {
                FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
            }
        }

        protected void finalize() throws Throwable {
            try {
                if (this.token != null) {
                    LogUtil.w(FingerprintService.TAG, "removing leaked reference: " + this.token);
                    FingerprintService.this.removeClient(this);
                    FingerprintService.this.removeGlobalCycleClient(this);
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }

        private boolean sendRemoved(int fingerId, int groupId) {
            boolean z = true;
            if (this.receiver == null) {
                return true;
            }
            try {
                this.receiver.onRemoved(FingerprintService.this.mHalDeviceId, fingerId, groupId);
                if (fingerId != 0) {
                    z = false;
                }
                return z;
            } catch (RemoteException e) {
                LogUtil.w(FingerprintService.TAG, "Failed to notify Removed:", e);
                return false;
            }
        }

        private boolean sendEnrollResult(int fpId, int groupId, int remaining) {
            if (this.receiver == null) {
                return true;
            }
            try {
                this.receiver.onEnrollResult(FingerprintService.this.mHalDeviceId, fpId, groupId, remaining);
                return remaining == 0;
            } catch (RemoteException e) {
                LogUtil.w(FingerprintService.TAG, "Failed to notify EnrollResult:", e);
                return true;
            }
        }

        private boolean sendAuthenticated(int fpId, int groupId) {
            boolean result = false;
            boolean authenticated = fpId != 0;
            LogUtil.d(FingerprintService.TAG, "sendAuthenticated (fpId = " + fpId + ", groupId  = " + groupId + ")");
            if (FingerprintService.this.mFingerprintEnabled) {
                if (this.receiver == null) {
                    result = true;
                } else if (authenticated) {
                    LogUtil.d(FingerprintService.TAG, "onAuthenticated(packageName=" + this.packageName + ", id=" + fpId + ", gp=" + groupId + ")");
                    this.receiver.onAuthenticationSucceeded(FingerprintService.this.mHalDeviceId, !this.restricted ? new Fingerprint(IElsaManager.EMPTY_PACKAGE, groupId, fpId, FingerprintService.this.mHalDeviceId) : null, groupId);
                } else {
                    try {
                        this.receiver.onAuthenticationFailed(FingerprintService.this.mHalDeviceId);
                    } catch (RemoteException e) {
                        LogUtil.w(FingerprintService.TAG, "Failed to notify Authenticated:", e);
                        result = true;
                    }
                }
                if (fpId == 0) {
                    result |= FingerprintService.this.handleFailedAttempt(this);
                } else {
                    result |= true;
                    FingerprintService.this.resetFailedAttempts();
                }
                return result;
            }
            LogUtil.e(FingerprintService.TAG, "fingerprint flow has been disabled, just return");
            return false;
        }

        private boolean sendAcquired(int acquiredInfo) {
            if (this.receiver == null) {
                return true;
            }
            try {
                this.receiver.onAcquired(FingerprintService.this.mHalDeviceId, acquiredInfo);
                if (acquiredInfo == 0) {
                    FingerprintService.this.mUnlockController.userActivity();
                }
                return false;
            } catch (RemoteException e) {
                LogUtil.w(FingerprintService.TAG, "Failed to invoke sendAcquired:", e);
                if (acquiredInfo == 0) {
                    FingerprintService.this.mUnlockController.userActivity();
                }
                return true;
            } catch (Throwable th) {
                if (acquiredInfo == 0) {
                    FingerprintService.this.mUnlockController.userActivity();
                }
                throw th;
            }
        }

        private boolean sendImageInfo(int type, int quality, int match_score) {
            if (this.receiver == null) {
                return true;
            }
            try {
                this.receiver.onImageInfoAcquired(type, quality, match_score);
                return false;
            } catch (RemoteException e) {
                LogUtil.w(FingerprintService.TAG, "Failed to invoke sendImageInfo:", e);
                return true;
            }
        }

        private boolean sendError(int error) {
            LogUtil.d(FingerprintService.TAG, "sendError = " + error);
            if (this.receiver != null) {
                try {
                    this.receiver.onError(FingerprintService.this.mHalDeviceId, error);
                } catch (RemoteException e) {
                    LogUtil.w(FingerprintService.TAG, "Failed to invoke sendError:", e);
                }
            }
            return true;
        }

        public int getGroupId() {
            return this.mGroupId;
        }

        public boolean isInLockOutWhiteList() {
            if (this.isKeyguard || "com.coloros.safecenter".equals(this.packageName)) {
                return true;
            }
            return "com.coloros.filemanager".equals(this.packageName);
        }

        public boolean isKeyguard() {
            return this.isKeyguard;
        }

        public String getClientPackageName() {
            return this.packageName;
        }

        public String toString() {
            return "clientMode = " + this.clientMode + ", packageName = " + this.packageName + ", pid = " + this.pid + ", userId = " + this.userId + ", restricted = " + this.restricted + ", isKeyguard = " + this.isKeyguard + ", token = " + this.token + " {" + Integer.toHexString(System.identityHashCode(this)) + "}";
        }

        public ClientMode getClientMode() {
            return this.clientMode;
        }

        private void sendMonitorEventTriggered(int type, String data) {
            if (this.receiver != null) {
                try {
                    this.receiver.onMonitorEventTriggered(type, data);
                } catch (RemoteException e) {
                    LogUtil.w(FingerprintService.TAG, "Failed to invoke sendMonitorEventTriggered.", e);
                }
            }
        }

        private void sendEngineeringInfoUpdated(EngineeringInfo info) {
            LogUtil.w(FingerprintService.TAG, "sendEngineeringInfoUpdated");
            if (this.receiver != null) {
                try {
                    this.receiver.onEngineeringInfoUpdated(info);
                } catch (RemoteException e) {
                    LogUtil.w(FingerprintService.TAG, "Failed to invoke sendEngineeringInfoUpdated.", e);
                }
            }
        }

        private boolean sendTouchDownEvent() {
            if (this.receiver != null) {
                try {
                    this.receiver.onTouchDown(FingerprintService.this.mHalDeviceId);
                } catch (RemoteException e) {
                    LogUtil.w(FingerprintService.TAG, "Failed to invoke onTouchDown:", e);
                }
            }
            return true;
        }

        private boolean sendTouchUpEvent() {
            if (this.receiver != null) {
                try {
                    this.receiver.onTouchUp(FingerprintService.this.mHalDeviceId);
                } catch (RemoteException e) {
                    LogUtil.w(FingerprintService.TAG, "Failed to invoke onTouchUp:", e);
                }
            }
            return true;
        }
    }

    private class FingerprintServiceLockoutResetMonitor {
        private final IFingerprintServiceLockoutResetCallback mCallback;
        private final Runnable mRemoveCallbackRunnable = new Runnable() {
            public void run() {
                FingerprintService.this.removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor.this);
            }
        };

        public FingerprintServiceLockoutResetMonitor(IFingerprintServiceLockoutResetCallback callback) {
            this.mCallback = callback;
        }

        public void sendLockoutReset() {
            if (this.mCallback != null) {
                try {
                    this.mCallback.onLockoutReset(FingerprintService.this.mHalDeviceId);
                } catch (DeadObjectException e) {
                    LogUtil.w(FingerprintService.TAG, "Death object while invoking onLockoutReset: ", e);
                    FingerprintService.this.mHandler.post(this.mRemoveCallbackRunnable);
                } catch (RemoteException e2) {
                    LogUtil.w(FingerprintService.TAG, "Failed to invoke onLockoutReset: ", e2);
                }
            }
        }
    }

    private final class FingerprintServiceWrapper extends Stub {
        private static final String KEYGUARD_PACKAGE = "com.android.systemui";

        /* synthetic */ FingerprintServiceWrapper(FingerprintService this$0, FingerprintServiceWrapper fingerprintServiceWrapper) {
            this();
        }

        private FingerprintServiceWrapper() {
        }

        public long preEnroll(final IBinder token) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final PendingResult<Long> r = new PendingResult(Long.valueOf(0));
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_PRE_ENROLL)) {
                        r.setResult(Long.valueOf(FingerprintService.this.startPreEnroll(token)));
                    } else {
                        r.cancel();
                    }
                }
            });
            return ((Long) r.await()).longValue();
        }

        public int postEnroll(final IBinder token) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final PendingResult<Integer> r = new PendingResult(Integer.valueOf(0));
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_POST_ENROLL)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startPostEnroll(token)));
                    } else {
                        r.cancel();
                    }
                }
            });
            return ((Integer) r.await()).intValue();
        }

        public void enroll(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (FingerprintService.this.getEnrolledFingerprints(userId).size() >= FingerprintService.this.mContext.getResources().getInteger(17694879)) {
                LogUtil.w(FingerprintService.TAG, "Too many fingerprints registered");
            } else if (FingerprintService.this.isCurrentUserOrProfile(userId)) {
                if (cryptoToken == null) {
                    LogUtil.e(FingerprintService.TAG, "token is null");
                }
                final ClientInfo info = new ClientInfo(Binder.getCallingPid(), opPackageName);
                final boolean restricted = isRestricted();
                final IBinder iBinder = token;
                final byte[] bArr = cryptoToken;
                final int i = userId;
                final String str = opPackageName;
                final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                final int i2 = flags;
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (FingerprintService.this.tryPreOperation(iBinder, FingerprintService.OPERATION_ENROLL)) {
                            FingerprintService.this.startEnrollment(iBinder, bArr, FingerprintService.this.getEffectiveUserIdRestricted(i, str), iFingerprintServiceReceiver, i2, restricted, info);
                        } else {
                            FingerprintService.this.notifyOperationCanceled(iFingerprintServiceReceiver);
                        }
                    }
                });
            }
        }

        private boolean isRestricted() {
            return !FingerprintService.this.hasPermission("android.permission.MANAGE_FINGERPRINT");
        }

        public void cancelEnrollment(final IBinder token, String opPackageName) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d(FingerprintService.TAG, "cancelEnrollment, opPackageName = " + opPackageName);
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_CANCEL_ENROLLMENT)) {
                        FingerprintService.this.stopEnrollment(token, true);
                    }
                }
            });
        }

        public void authenticate(IBinder token, long opId, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            if (FingerprintService.this.mDcsStatisticsUtil != null) {
                FingerprintService.this.mDcsStatisticsUtil.clearMap();
            }
            int callingUid = Binder.getCallingUid();
            final int callingUserId = UserHandle.getCallingUserId();
            int pid = Binder.getCallingPid();
            if (FingerprintService.this.canUseFingerprint(opPackageName, true, callingUid, pid)) {
                final boolean restricted = isRestricted();
                final ClientInfo info = new ClientInfo(pid, opPackageName);
                LogUtil.d(FingerprintService.TAG, "authenticate, opPackageName = " + opPackageName);
                final IBinder iBinder = token;
                final long j = opId;
                final int i = groupId;
                final String str = opPackageName;
                final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                final int i2 = flags;
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (FingerprintService.this.tryPreOperation(iBinder, 256)) {
                            FingerprintService.this.startAuthentication(iBinder, j, callingUserId, FingerprintService.this.getEffectiveUserIdRestricted(i, str), iFingerprintServiceReceiver, i2, restricted, info);
                        } else {
                            FingerprintService.this.notifyOperationCanceled(iFingerprintServiceReceiver);
                        }
                    }
                });
                return;
            }
            LogUtil.e(FingerprintService.TAG, "authenticate(): reject " + opPackageName);
        }

        public void cancelAuthentication(final IBinder token, String opPackageName) {
            if (FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid())) {
                LogUtil.d(FingerprintService.TAG, "cancelAuthentication, opPackageName = " + opPackageName);
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_CANCEL_AUTHENTICATION)) {
                            FingerprintService.this.stopAuthentication(token, true, false);
                        }
                    }
                });
            }
        }

        public void setActiveUser(final int userId) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    LogUtil.d(FingerprintService.TAG, "setActiveUser");
                    FingerprintService.this.updateActiveGroup(userId, null);
                }
            });
        }

        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver, String opPackageName) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final boolean restricted = isRestricted();
            LogUtil.d(FingerprintService.TAG, "remove fingerId = " + fingerId + ", groupId=  " + groupId);
            final ClientInfo info = new ClientInfo(Binder.getCallingPid(), opPackageName);
            final IBinder iBinder = token;
            final int i = fingerId;
            final int i2 = groupId;
            final String str = opPackageName;
            final int i3 = userId;
            final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.tryPreOperation(iBinder, FingerprintService.OPERATION_REMOVE_FP)) {
                        FingerprintService.this.startRemove(iBinder, i, FingerprintService.this.getEffectiveUserIdRestricted(i2, str), i3, iFingerprintServiceReceiver, restricted, info);
                    } else {
                        FingerprintService.this.notifyOperationCanceled(iFingerprintServiceReceiver);
                    }
                }
            });
        }

        public boolean isHardwareDetected(long deviceId, String opPackageName) {
            boolean z = false;
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid())) {
                return false;
            }
            if (FingerprintService.this.mHalDeviceId != 0) {
                z = true;
            }
            return z;
        }

        public void rename(final int fingerId, final int groupId, final String name) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (FingerprintService.this.isCurrentUserOrProfile(groupId)) {
                LogUtil.d(FingerprintService.TAG, " rename fingerId = " + fingerId + ", groupId = " + groupId + ", name =  " + name);
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        FingerprintService.this.mFingerprintUtils.renameFingerprintForUser(FingerprintService.this.mContext, fingerId, FingerprintService.this.getEffectiveUserIdRestricted(groupId, "none"), name);
                    }
                });
            }
        }

        public List<Fingerprint> getEnrolledFingerprints(int userId, String opPackageName) {
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid())) {
                return Collections.emptyList();
            }
            if (!FingerprintService.this.isCurrentUserOrProfile(userId)) {
                return Collections.emptyList();
            }
            LogUtil.d(FingerprintService.TAG, "getEnrolledFingerprints opPackageName = " + opPackageName);
            return FingerprintService.this.getEnrolledFingerprints(FingerprintService.this.getEffectiveUserIdRestricted(userId, opPackageName));
        }

        public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid()) || !FingerprintService.this.isCurrentUserOrProfile(userId)) {
                return false;
            }
            boolean hasFP = FingerprintService.this.hasEnrolledFingerprints(userId, opPackageName);
            LogUtil.d(FingerprintService.TAG, " hasEnrolledFingerprints opPackageName = " + opPackageName + " hasFP = " + hasFP);
            return hasFP;
        }

        public long getAuthenticatorId(String opPackageName) {
            return FingerprintService.this.getAuthenticatorId(opPackageName);
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (Binder.getCallingUid() == 1000 || FingerprintService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") == 0) {
                long ident = Binder.clearCallingIdentity();
                try {
                    FingerprintService.this.dumpInternal(fd, pw, args);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                pw.println("Permission Denial: can't dump Fingerprint from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
        }

        public void resetTimeout(byte[] token) {
            FingerprintService.this.checkPermission("android.permission.RESET_FINGERPRINT_LOCKOUT");
            LogUtil.d(FingerprintService.TAG, "resetTimeout");
            FingerprintService.this.mHandler.post(FingerprintService.this.mResetFailedAttemptsRunnable);
        }

        public void addLockoutResetCallback(final IFingerprintServiceLockoutResetCallback callback) throws RemoteException {
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.addLockoutResetMonitor(new FingerprintServiceLockoutResetMonitor(callback));
                }
            });
        }

        public int getEnrollmentTotalTimes(final IBinder token) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (FingerprintService.this.mEnrollmentTotalTimes != 0) {
                LogUtil.d(FingerprintService.TAG, "has got total times, just return it");
                return FingerprintService.this.mEnrollmentTotalTimes;
            }
            final PendingResult<Integer> r = new PendingResult(Integer.valueOf(0));
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_GET_ENROLLMENT_TOTALTIMES)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startGetEnrollmentTotalTimes(token)));
                    } else {
                        r.cancel();
                    }
                }
            });
            return ((Integer) r.await()).intValue();
        }

        public int pauseEnroll() {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final PendingResult<Integer> r = new PendingResult(Integer.valueOf(-1));
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.tryPreOperation(null, 264)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startPauseEnroll()));
                    } else {
                        r.cancel();
                    }
                }
            });
            return ((Integer) r.await()).intValue();
        }

        public int continueEnroll() {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final PendingResult<Integer> r = new PendingResult(Integer.valueOf(-1));
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.tryPreOperation(null, FingerprintService.OPERATION_CONTINUE_ENROLL)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startContinueEnroll()));
                    } else {
                        r.cancel();
                    }
                }
            });
            return ((Integer) r.await()).intValue();
        }

        public void setTouchEventListener(IBinder token, IFingerprintServiceReceiver receiver, int groupId, String opPackageName) {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            final int effectiveGroupId = FingerprintService.this.getEffectiveUserId(groupId);
            final ClientInfo info = new ClientInfo(Binder.getCallingPid(), opPackageName);
            final IBinder iBinder = token;
            final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.startSetTouchEventListener(iBinder, iFingerprintServiceReceiver, effectiveGroupId, FingerprintServiceWrapper.this.isRestricted(), info);
                }
            });
        }

        public void cancelTouchEventListener(final IBinder token, String opPackageName) {
            if (FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid())) {
                LogUtil.d(FingerprintService.TAG, "cancelTouchEventListener, opPackageName = " + opPackageName);
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_CANCEL_TOUCH_EVENT)) {
                            FingerprintService.this.stopTouchEventListener(token, true);
                        }
                    }
                });
            }
        }

        public void finishUnLockedScreen(boolean isfinished, String opPackageName) {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            long callingIdentity = Binder.clearCallingIdentity();
            try {
                FingerprintService.this.startFinishUnLockedScreen(isfinished, opPackageName);
            } finally {
                Binder.restoreCallingIdentity(callingIdentity);
            }
        }

        public int getAlikeyStatus() {
            return FingerprintService.this.startGetAlikeyStatus();
        }

        public int getEngineeringInfo(IBinder token, String opPackageName, int userId, IFingerprintServiceReceiver receiver, int type) {
            int pid = Binder.getCallingPid();
            if (FingerprintService.this.canUseFingerprint(opPackageName, true, Binder.getCallingUid(), pid)) {
                LogUtil.d(FingerprintService.TAG, "getEngineeringInfo type = " + type);
                final boolean restricted = isRestricted();
                final ClientInfo info = new ClientInfo(pid, opPackageName);
                final PendingResult<Integer> r = new PendingResult(Integer.valueOf(-1));
                final IBinder iBinder = token;
                final int i = userId;
                final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                final int i2 = type;
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        r.setResult(Integer.valueOf(FingerprintService.this.startGetEngineeringInfo(iBinder, restricted, i, iFingerprintServiceReceiver, info, i2)));
                    }
                });
                return ((Integer) r.await()).intValue();
            }
            LogUtil.d(FingerprintService.TAG, "Fingerprint getEngineeringInfo failed , " + opPackageName + " is not allowed this op");
            return -1;
        }

        public void cancelGetEngineeringInfo(final IBinder token, String opPackageName, final int type) {
            LogUtil.d(FingerprintService.TAG, "cancelGetEngineeringInfo type = " + type);
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.stopGetEngineeringInfo(token, true, type);
                }
            });
        }

        public int pauseIdentify(final IBinder token) {
            final PendingResult<Integer> r = new PendingResult(Integer.valueOf(-1));
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_PAUSE_IDENTIFY)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startPauseIdentify()));
                    } else {
                        r.cancel();
                    }
                }
            });
            return ((Integer) r.await()).intValue();
        }

        public int continueIdentify(final IBinder token) {
            final PendingResult<Integer> r = new PendingResult(Integer.valueOf(-1));
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_CONTINUE_IDENTIFY)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startContinueIdentify()));
                    } else {
                        r.cancel();
                    }
                }
            });
            return ((Integer) r.await()).intValue();
        }

        public void setFingerprintEnabled(final boolean enabled) {
            LogUtil.e(FingerprintService.TAG, "setFingerprintEnabled ( " + enabled + " )");
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.startSetFingerprintEnabled(enabled);
                }
            });
        }

        public long getLockoutAttemptDeadline(String opPackageName) {
            LogUtil.d(FingerprintService.TAG, "getLockoutAttemptDeadline opPackageName = " + opPackageName);
            final PendingResult<Long> r = new PendingResult(Long.valueOf(0));
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    r.setResult(Long.valueOf(FingerprintService.this.getLockoutAttemptDeadline()));
                }
            });
            return ((Long) r.await()).longValue();
        }

        public int getFailedAttempts(String opPackageName) {
            LogUtil.d(FingerprintService.TAG, "getFailedAttempts opPackageName = " + opPackageName);
            final PendingResult<Integer> r = new PendingResult(Integer.valueOf(0));
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    r.setResult(Integer.valueOf(FingerprintService.this.getFailedAttempts()));
                }
            });
            return ((Integer) r.await()).intValue();
        }
    }

    private class PendingResult<R> {
        private CountDownLatch mLatch = new CountDownLatch(1);
        private volatile R mResult;

        PendingResult(R defResult) {
            this.mResult = defResult;
        }

        public R await() {
            try {
                this.mLatch.await();
            } catch (InterruptedException e) {
            }
            return this.mResult;
        }

        public void setResult(R result) {
            this.mResult = result;
            this.mLatch.countDown();
        }

        public void cancel() {
            this.mLatch.countDown();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum VerifyResult {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.FingerprintService.VerifyResult.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.FingerprintService.VerifyResult.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.FingerprintService.VerifyResult.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.FingerprintService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.FingerprintService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.FingerprintService.<clinit>():void");
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mServiceThread.getLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 10:
                        FingerprintService.this.handleUserSwitching(msg.arg1);
                        return;
                    case 11:
                        break;
                    case 12:
                        FingerprintService.this.handleSwitchInit();
                        return;
                    case 13:
                        FingerprintService.this.handleSwitchUpdate();
                        return;
                    default:
                        LogUtil.w(FingerprintService.TAG, "Unknown message:" + msg.what);
                        return;
                }
                while (true) {
                    Runnable r = (Runnable) FingerprintService.this.mDaemonCallbackQueue.poll();
                    if (r != null) {
                        r.run();
                    } else {
                        return;
                    }
                }
            }
        };
    }

    public FingerprintService(Context context) {
        super(context);
        this.mLockoutMonitors = new ArrayList();
        this.mCurrentUserId = -2;
        this.mFingerprintUtils = FingerprintUtils.getInstance();
        this.mAuthClient = null;
        this.mEnrollClient = null;
        this.mRemoveClient = null;
        this.mDaemonCallbackQueue = new ConcurrentLinkedDeque();
        this.mEnrollmentTotalTimes = 0;
        this.mEngineeringInfoClient = null;
        this.mGlobalCycleClient = null;
        this.mTouchClient = null;
        this.mClientLock = new Object();
        this.mFingerprintEnabled = true;
        this.mFingerprintUnlockEnabled = false;
        this.mIsNearState = false;
        this.mIsRegistered = false;
        this.mLockoutReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (FingerprintService.ACTION_LOCKOUT_RESET.equals(intent.getAction())) {
                    LogUtil.d(FingerprintService.TAG, "onReceive");
                    FingerprintService.this.mHandler.post(FingerprintService.this.mResetFailedAttemptsRunnable);
                }
            }
        };
        this.mResetFailedAttemptsRunnable = new Runnable() {
            public void run() {
                FingerprintService.this.resetFailedAttempts();
            }
        };
        this.mDaemonCallback = new IFingerprintDaemonCallback.Stub() {
            public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
                final long j = deviceId;
                final int i = fingerId;
                final int i2 = groupId;
                final int i3 = remaining;
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.handleEnrollResult(j, i, i2, i3);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onAcquired(final long deviceId, final int acquiredInfo) {
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.handleAcquired(deviceId, acquiredInfo);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onAuthenticated(long deviceId, int fingerId, int groupId) {
                final long j = deviceId;
                final int i = fingerId;
                final int i2 = groupId;
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.handleAuthenticated(j, i, i2);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onError(final long deviceId, final int error) {
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.handleError(deviceId, error);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onRemoved(long deviceId, int fingerId, int groupId) {
                final long j = deviceId;
                final int i = fingerId;
                final int i2 = groupId;
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.handleRemoved(j, i, i2);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onEnumerate(long deviceId, int[] fingerIds, int groupId) {
                final long j = deviceId;
                final int[] iArr = fingerIds;
                final int i = groupId;
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.handleEnumerate(j, iArr, i);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onMonitorEventTriggered(final int type, final String data) {
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.dispatchMonitorEventTriggered(type, data);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onEngineeringInfoUpdated(final EngineeringInfo info) {
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.dispatchEngineeringInfoUpdated(info);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onTouchDown(final long deviceId) {
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.dispatchTouchDown(deviceId);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onTouchUp(final long deviceId) {
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.dispatchTouchUp(deviceId);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }

            public void onImageInfoAcquired(final int type, final int quality, final int match_score) {
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    public void run() {
                        FingerprintService.this.handleImageInfoAcquired(type, quality, match_score);
                    }
                });
                if (!FingerprintService.this.mHandler.hasMessages(11)) {
                    FingerprintService.this.mHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mHandler.obtainMessage(11));
                }
            }
        };
        this.mIUnLocker = new IUnLocker() {
            public void dispatchScreenOnAuthenticatedEvent(int fingerId, int groupId) {
                LogUtil.d(FingerprintService.TAG, "dispatchScreenOnAuthenticatedEvent fingerId = " + fingerId);
                FingerprintService.this.sendKeyGuardAuthenticated(fingerId, groupId, true);
            }

            public void reauthentication() {
                LogUtil.d(FingerprintService.TAG, "reauthentication");
                synchronized (FingerprintService.this.mClientLock) {
                    if (FingerprintService.this.mAuthClient != null) {
                        IBinder token = FingerprintService.this.mAuthClient.token;
                        if (FingerprintService.this.mAuthClient.sendError(8)) {
                            FingerprintService.this.stopAuthentication(token, false, false);
                        }
                    }
                }
            }

            public void dispatchScreenOffAuthenticatedEvent(int fingerId, int groupId) {
                LogUtil.d(FingerprintService.TAG, "dispatchScreenOffAuthenticatedEvent fingerId = " + fingerId);
                boolean authenticated = fingerId != 0;
                FingerprintService.this.sendKeyGuardAuthenticated(fingerId, groupId, false);
                if (authenticated) {
                    FingerprintService.this.mUnlockController.onScreenOnUnBlockedByFingerprint(authenticated);
                } else if (!authenticated) {
                    LogUtil.d(FingerprintService.TAG, "wait call back from keyguard to screenon when false");
                }
            }

            public void dispatchTouchEventInLockMode() {
                synchronized (FingerprintService.this.mClientLock) {
                    if (FingerprintService.this.mTouchClient != null) {
                        LogUtil.d(FingerprintService.TAG, "sendTouchDownEvent");
                        FingerprintService.this.mTouchClient.sendTouchDownEvent();
                    }
                }
            }

            public void dispatchMonitorEvent(int type, String data) {
                FingerprintService.this.dispatchMonitorEventTriggered(type, data);
            }

            public int pauseIdentify() {
                return FingerprintService.this.startPauseIdentify();
            }

            public int continueIdentify() {
                return FingerprintService.this.startContinueIdentify();
            }

            public void setScreenState(int state) {
                FingerprintService.this.setScreenStateInternal(state);
            }

            public void sendUnlockTime(int type, long time) {
                if (FingerprintService.this.mGlobalCycleClient != null) {
                    LogUtil.d(FingerprintService.TAG, "sendUnlockTime type = " + type + ", time = " + time);
                    FingerprintService.this.mGlobalCycleClient.sendEngineeringInfoUpdated(new EngineeringInfo(type, String.valueOf(time)));
                }
            }

            public void onScreenOff(boolean isOff) {
                FingerprintService.this.mTouchEventMonitorMode.dispatchScreenOff(isOff);
                FingerprintService.this.getPsensorManager().dispatchScreenOff(isOff);
            }
        };
        this.mSystemReady = false;
        this.mListener = new Ilistener() {
            public void onSettingChanged(String settingName, boolean isOn) {
                if (FingerprintSettings.FINGERPRINT_UNLOCK_SWITCH.equals(settingName)) {
                    LogUtil.d(FingerprintService.TAG, "onSettingChanged, settingName = " + settingName + ", isOn = " + isOn);
                    FingerprintService.this.mFingerprintUnlockEnabled = isOn;
                }
                FingerprintService.this.mUnlockController.onSettingChanged(settingName, isOn);
            }
        };
        this.mIProximitySensorEventListener = new IProximitySensorEventListener() {
            public void onSensorChanged(boolean isNearState) {
                boolean isNear = isNearState ? FingerprintService.this.mIsRegistered : false;
                if (isNear != FingerprintService.this.mIsNearState) {
                    FingerprintService.this.mIsNearState = isNear;
                    LogUtil.d(FingerprintService.TAG, "mIsNearState = " + FingerprintService.this.mIsNearState);
                    FingerprintService.this.mUnlockController.onProximitySensorChanged(FingerprintService.this.mIsNearState);
                    FingerprintService.this.mTouchEventMonitorMode.onProximitySensorChanged(FingerprintService.this.mIsNearState);
                }
            }

            public void onRegisterStateChanged(boolean isRegistered) {
                FingerprintService.this.mIsRegistered = isRegistered;
                boolean isNear = FingerprintService.this.mIsNearState ? FingerprintService.this.mIsRegistered : false;
                if (isNear != FingerprintService.this.mIsNearState) {
                    FingerprintService.this.mIsNearState = isNear;
                    LogUtil.d(FingerprintService.TAG, "mIsNearState = " + FingerprintService.this.mIsNearState);
                    FingerprintService.this.mUnlockController.onProximitySensorChanged(FingerprintService.this.mIsNearState);
                    FingerprintService.this.mTouchEventMonitorMode.onProximitySensorChanged(FingerprintService.this.mIsNearState);
                }
            }
        };
        LogUtil.d(TAG, TAG);
        this.mContext = context;
        this.mKeyguardPackage = ComponentName.unflattenFromString(context.getResources().getString(17039465)).getPackageName();
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        this.mContext.registerReceiver(this.mLockoutReceiver, new IntentFilter(ACTION_LOCKOUT_RESET), "android.permission.RESET_FINGERPRINT_LOCKOUT", null);
        this.mUserManager = UserManager.get(this.mContext);
        Watchdog.getInstance().addMonitor(this);
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mServiceThread.start();
        initHandler();
        this.mHealthMonitor = new HealthMonitor(TAG);
        this.mDcsStatisticsUtil = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(context);
        this.mKeyguardPolicy = KeyguardPolicy.getKeyguardPolicy(context);
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mSensorType = SupportUtil.getSensorType(this.mContext);
        this.mLockoutDeadline = 0;
        this.mTouchEventMonitorMode = new TouchEventMonitorMode(this.mContext, this.mIUnLocker);
        if (SupportUtil.FRONT_PRESS_SENSOR.equals(this.mSensorType)) {
            this.mUnlockController = new UnlockController(this.mContext, this.mIUnLocker);
        } else if (SupportUtil.FRONT_TOUCH_SENSOR.equals(this.mSensorType)) {
            this.mUnlockController = new TouchSensorUnlockController(this.mContext, this.mIUnLocker);
        } else if (SupportUtil.BACK_TOUCH_SENSOR.equals(this.mSensorType)) {
            this.mUnlockController = new BackTouchSensorUnlockController(this.mContext, this.mIUnLocker);
        }
        FingerprintPowerManager.initFPM(this.mContext, this.mUnlockController);
        ProximitySensorManager.initPsensorManager(context, this.mIProximitySensorEventListener);
        this.mFingerprintUnlockSettingMonitor = new FingerprintUnlockSettingMonitor(this.mContext, this.mListener, this.mServiceThread.getLooper());
        initSwitchUpdater();
        LogUtil.d(TAG, "mSensorType = " + this.mSensorType);
    }

    public void binderDied() {
        this.mDaemonCallbackQueue.add(new Runnable() {
            public void run() {
                LogUtil.d(FingerprintService.TAG, "fingerprintd died");
                FingerprintService.this.mHealthMonitor.notifyFingerprintdDied();
                FingerprintService.this.mDaemonStub = FingerprintService.this.mDaemonWrapper = FingerprintService.this.mDaemon = null;
                FingerprintService.this.mCurrentUserId = -2;
                FingerprintService.this.handleError(FingerprintService.this.mHalDeviceId, 1);
            }
        });
        if (!this.mHandler.hasMessages(11)) {
            this.mHandler.sendMessageAtFrontOfQueue(this.mHandler.obtainMessage(11));
        }
    }

    public IFingerprintDaemon getFingerprintDaemon() {
        if (this.mDaemon == null) {
            String session = String.valueOf(SystemClock.uptimeMillis());
            this.mHealthMonitor.start("asInterface", 10000, session);
            if (this.mDaemonStub == null) {
                this.mDaemonStub = IFingerprintDaemon.Stub.asInterface(ServiceManager.getService(FINGERPRINTD));
            }
            this.mHealthMonitor.stop("asInterface", session);
            if (this.mDaemonStub != null) {
                try {
                    if (this.mDaemonWrapper == null) {
                        this.mDaemonWrapper = new FingerprintDaemonWrapper(this.mDaemonStub, this.mHealthMonitor);
                        this.mDaemonWrapper.asBinder().linkToDeath(this, 0);
                        this.mDaemonWrapper.init(this.mDaemonCallback);
                    }
                    this.mHalDeviceId = this.mDaemonWrapper.openHal();
                    if (this.mHalDeviceId != 0) {
                        this.mDaemon = this.mDaemonWrapper;
                        updateActiveGroup(ActivityManager.getCurrentUser(), null);
                        this.mEnrollmentTotalTimes = this.mDaemon.getEnrollmentTotalTimes();
                    } else {
                        LogUtil.w(TAG, "Failed to open Fingerprint HAL!");
                        this.mDaemon = null;
                    }
                } catch (RemoteException e) {
                    LogUtil.e(TAG, "Failed to open fingeprintd HAL", e);
                    this.mDaemon = null;
                }
            } else {
                LogUtil.w(TAG, "fingerprint service not available");
            }
            if (this.mDaemon == null) {
                LogUtil.e(TAG, "mDaemon = null, Failed to open fingeprintd HAL");
            }
        }
        return this.mDaemon;
    }

    protected void handleEnumerate(long deviceId, int[] fingerIds, int groupId) {
        LogUtil.w(TAG, "handleEnumerate");
        syncTemplates(fingerIds, groupId);
    }

    protected void handleError(long deviceId, int error) {
        synchronized (this.mClientLock) {
            LogUtil.d(TAG, "handleError error = " + error);
            IBinder token;
            if (this.mEnrollClient != null) {
                token = this.mEnrollClient.token;
                if (this.mEnrollClient.sendError(error)) {
                    stopEnrollment(token, false);
                }
            } else if (this.mAuthClient != null) {
                token = this.mAuthClient.token;
                if (this.mAuthClient.sendError(error)) {
                    stopAuthentication(token, false, false);
                }
            } else if (this.mRemoveClient != null) {
                if (this.mRemoveClient.sendError(error)) {
                    removeClient(this.mRemoveClient);
                }
            } else if (this.mEngineeringInfoClient != null) {
                token = this.mEngineeringInfoClient.token;
                if (this.mEngineeringInfoClient.sendError(error)) {
                    stopGetEngineeringInfo(token, false, -1);
                }
            } else if (this.mTouchClient != null) {
                token = this.mTouchClient.token;
                if (this.mTouchClient.sendError(error)) {
                    stopTouchEventListener(token, false);
                }
            }
        }
    }

    protected void handleRemoved(long deviceId, int fingerId, int groupId) {
        LogUtil.d(TAG, "handleRemoved fingerId = " + fingerId);
        removeTemplateForUser(groupId, fingerId);
        synchronized (this.mClientLock) {
            ClientMonitor client = this.mRemoveClient;
            if (client != null && client.sendRemoved(fingerId, groupId)) {
                LogUtil.d(TAG, "handleRemoved remove mRemoveClient");
                removeClient(this.mRemoveClient);
            }
        }
    }

    /* JADX WARNING: Missing block: B:28:0x0092, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void handleAuthenticated(long deviceId, int fingerId, int groupId) {
        synchronized (this.mClientLock) {
            if (this.mAuthClient != null) {
                if (this.mDcsStatisticsUtil != null) {
                    this.mDcsStatisticsUtil.sendFingerId(fingerId, this.mAuthClient.getClientPackageName());
                }
                long startTime = SystemClock.uptimeMillis();
                if (fingerId != 0 && SupportUtil.FRONT_PRESS_SENSOR.equals(this.mSensorType)) {
                    this.mUnlockController.dispatchAuthForDropHomeKey();
                }
                if (this.mAuthClient.isKeyguard()) {
                    LogUtil.d(TAG, "handleAuthenticated for keyguard, fingerId = " + fingerId);
                    this.mUnlockController.dispatchAuthenticated(new AuthenticatedInfo(fingerId, groupId));
                    calculateTime("mUnlockController.dispatchAuthenticated", SystemClock.uptimeMillis() - startTime);
                    return;
                }
                LogUtil.d(TAG, "handleAuthenticated for other application");
                IBinder token = this.mAuthClient.token;
                boolean isSended = this.mAuthClient.sendAuthenticated(fingerId, groupId);
                if (isSended && !inLockoutMode()) {
                    stopAuthentication(token, false, false);
                    removeClient(this.mAuthClient);
                } else if (isSended && inLockoutMode()) {
                    stopAuthentication(token, false, true);
                    removeClient(this.mAuthClient);
                }
            }
        }
    }

    protected void handleAcquired(long deviceId, int acquiredInfo) {
        LogUtil.d(TAG, "handleAcquired acquiredInfo = " + acquiredInfo);
        long startTime = SystemClock.uptimeMillis();
        synchronized (this.mClientLock) {
            if (this.mEnrollClient != null) {
                if (this.mEnrollClient.sendAcquired(acquiredInfo)) {
                    removeClient(this.mEnrollClient);
                }
            } else if (this.mAuthClient != null) {
                if (this.mDcsStatisticsUtil != null) {
                    this.mDcsStatisticsUtil.sendAcquiredInfo(acquiredInfo, this.mAuthClient.getClientPackageName());
                }
                if (this.mAuthClient.isKeyguard()) {
                    this.mUnlockController.dispatchAcquired(acquiredInfo);
                }
                if (this.mAuthClient.sendAcquired(acquiredInfo)) {
                    removeClient(this.mAuthClient);
                }
            }
        }
        calculateTime("handleAcquired", SystemClock.uptimeMillis() - startTime);
    }

    protected void handleEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
        synchronized (this.mClientLock) {
            if (remaining == 0) {
                addTemplateForUser(groupId, fingerId);
            }
            if (this.mEnrollClient != null) {
                LogUtil.d(TAG, "handleEnrollResult , fingerId = " + fingerId + ", groupId = " + groupId + ", remaining = " + remaining);
                if (groupId != this.mEnrollClient.getGroupId()) {
                    LogUtil.w(TAG, "groupId != getGroupId(), groupId: " + groupId + " getGroupId():" + this.mEnrollClient.getGroupId());
                }
                if (this.mEnrollClient.sendEnrollResult(fingerId, groupId, remaining) && remaining == 0) {
                    removeClient(this.mEnrollClient);
                }
            }
        }
    }

    private void userActivity() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 2, 0);
    }

    void handleUserSwitching(int userId) {
        LogUtil.d(TAG, "handleUserSwitching");
        updateActiveGroup(userId, null);
    }

    /* JADX WARNING: Missing block: B:10:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void removeClient(ClientMonitor client) {
        synchronized (this.mClientLock) {
            if (client == null) {
                return;
            }
            client.destroy();
            if (client == this.mAuthClient) {
                this.mAuthClient = null;
            } else if (client == this.mEnrollClient) {
                this.mEnrollClient = null;
            } else if (client == this.mRemoveClient) {
                this.mRemoveClient = null;
            } else if (client == this.mEngineeringInfoClient) {
                this.mEngineeringInfoClient = null;
            } else if (client == this.mTouchClient) {
                this.mTouchClient = null;
            }
        }
    }

    private boolean inLockoutMode() {
        return this.mFailedAttempts >= 5;
    }

    private void scheduleLockoutReset() {
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + FAIL_LOCKOUT_TIMEOUT_MS, getLockoutResetIntent());
    }

    private void cancelLockoutReset() {
        this.mAlarmManager.cancel(getLockoutResetIntent());
    }

    private PendingIntent getLockoutResetIntent() {
        return PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_LOCKOUT_RESET), 134217728);
    }

    protected void resetFailedAttempts() {
        LogUtil.d(TAG, "resetFailedAttempts");
        if (DEBUG && inLockoutMode()) {
            LogUtil.d(TAG, "Reset fingerprint lockout");
        }
        this.mFailedAttempts = 0;
        cancelLockoutReset();
        notifyLockoutResetMonitors();
        setLockoutAttemptDeadline(0);
        LogUtil.d(TAG, "resetFailedAttempts finish");
    }

    private boolean handleFailedAttempt(ClientMonitor clientMonitor) {
        if (this.mIsNearState && clientMonitor.isKeyguard()) {
            LogUtil.e(TAG, "fail result of Keyguard in near state, don't add count");
        } else {
            this.mFailedAttempts++;
        }
        LogUtil.i(TAG, "mFailedAttempts = " + this.mFailedAttempts);
        if (!inLockoutMode()) {
            return false;
        }
        scheduleLockoutReset();
        setLockoutAttemptDeadline(SystemClock.elapsedRealtime() + FAIL_LOCKOUT_TIMEOUT_MS);
        if (!(clientMonitor == null || clientMonitor.sendError(7))) {
            LogUtil.w(TAG, "Cannot send lockout message to client");
        }
        return true;
    }

    private long getLockoutAttemptDeadline() {
        long now = SystemClock.elapsedRealtime();
        if (this.mLockoutDeadline == 0) {
            return 0;
        }
        if (this.mLockoutDeadline != 0 && this.mLockoutDeadline < now) {
            setLockoutAttemptDeadline(0);
            return 0;
        } else if (this.mLockoutDeadline <= FAIL_LOCKOUT_TIMEOUT_MS + now) {
            return this.mLockoutDeadline;
        } else {
            setLockoutAttemptDeadline(0);
            return 0;
        }
    }

    private void setLockoutAttemptDeadline(long deadline) {
        this.mLockoutDeadline = deadline;
        LogUtil.w(TAG, "setLockoutAttemptDeadline, mLockoutDeadline = " + this.mLockoutDeadline);
    }

    private int getFailedAttempts() {
        return this.mFailedAttempts;
    }

    private void removeTemplateForUser(int groupId, int fingerId) {
        this.mFingerprintUtils.removeFingerprintIdForUser(this.mContext, fingerId, groupId);
    }

    private void addTemplateForUser(int groupId, int fingerId) {
        this.mFingerprintUtils.addFingerprintForUser(this.mContext, fingerId, groupId);
    }

    private void startEnrollment(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, ClientInfo info) {
        LogUtil.d(TAG, "startEnrollment clientInfo " + info);
        updateActiveGroup(userId, info.mPackageName);
        int groupId = userId;
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            if (receiver != null) {
                try {
                    LogUtil.d(TAG, "startEnrollment: no fingeprintd! send error FINGERPRINT_ERROR_HW_UNAVAILABLE");
                    receiver.onError(-1, 1);
                } catch (RemoteException e) {
                    LogUtil.w(TAG, "Failed to invoke sendError:", e);
                }
            }
            LogUtil.w(TAG, "enroll: no fingeprintd!");
            return;
        }
        stopPendingOperations(true);
        synchronized (this.mClientLock) {
            this.mEnrollClient = new ClientMonitor(token, receiver, userId, userId, restricted, info.mPid, info.mPackageName, ClientMode.ENROLL);
        }
        try {
            int result = daemon.enroll(cryptoToken, userId, 60);
            if (result != 0) {
                LogUtil.w(TAG, "startEnroll failed, result=" + result);
                handleError(this.mHalDeviceId, 1);
            }
        } catch (RemoteException e2) {
            LogUtil.e(TAG, "startEnroll failed", e2);
        }
    }

    public long startPreEnroll(IBinder token) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startPreEnroll: no fingeprintd!");
            return 0;
        }
        try {
            LogUtil.d(TAG, "startPreEnroll");
            return daemon.preEnroll();
        } catch (RemoteException e) {
            LogUtil.e(TAG, "startPreEnroll failed", e);
            return 0;
        }
    }

    public int startPostEnroll(IBinder token) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startPostEnroll: no fingeprintd!");
            return 0;
        }
        try {
            LogUtil.d(TAG, "startPostEnroll");
            return daemon.postEnroll();
        } catch (RemoteException e) {
            LogUtil.e(TAG, "startPostEnroll failed", e);
            return 0;
        }
    }

    private void stopPendingOperations(boolean initiatedByClient) {
        LogUtil.d(TAG, "stopPendingOperations initiatedByClient = " + initiatedByClient);
        synchronized (this.mClientLock) {
            if (this.mEnrollClient != null) {
                stopEnrollment(this.mEnrollClient.token, initiatedByClient);
            }
            if (this.mAuthClient != null) {
                stopAuthentication(this.mAuthClient.token, initiatedByClient, false);
            }
            if (this.mEngineeringInfoClient != null) {
                stopGetEngineeringInfo(this.mEngineeringInfoClient.token, initiatedByClient, -1);
            }
            if (this.mTouchClient != null) {
                stopTouchEventListener(this.mTouchClient.token, initiatedByClient);
            }
        }
    }

    void stopEnrollment(IBinder token, boolean initiatedByClient) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "stopEnrollment: no fingeprintd!");
            return;
        }
        ClientMonitor client = this.mEnrollClient;
        if (client != null && client.token == token) {
            if (token == null || token.isBinderAlive()) {
                LogUtil.w(TAG, "stopEnrollment initiatedByClient = " + initiatedByClient);
                if (initiatedByClient) {
                    try {
                        int result = daemon.cancelEnrollment();
                        if (result != 0) {
                            LogUtil.w(TAG, "startEnrollCancel failed, result = " + result);
                        }
                    } catch (RemoteException e) {
                        LogUtil.e(TAG, "stopEnrollment failed", e);
                    }
                    client.sendError(5);
                }
                removeClient(this.mEnrollClient);
                return;
            }
            LogUtil.e(TAG, token + " has died, just skip this stopEnrollment");
        }
    }

    private void startAuthentication(IBinder token, long opId, int callingUserId, int groupId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, ClientInfo info) {
        updateActiveGroup(groupId, info.mPackageName);
        LogUtil.d(TAG, "startAuthentication clientInfo " + info);
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            if (receiver != null) {
                try {
                    LogUtil.w(TAG, "startAuthentication: no fingeprintd! send error FINGERPRINT_ERROR_HW_UNAVAILABLE");
                    receiver.onError(-1, 1);
                } catch (RemoteException e) {
                    LogUtil.w(TAG, "Failed to invoke sendError:", e);
                }
            }
        } else if (token == null || token.isBinderAlive()) {
            stopPendingOperations(true);
            synchronized (this.mClientLock) {
                this.mAuthClient = new ClientMonitor(token, receiver, this.mCurrentUserId, groupId, restricted, info.mPid, info.mPackageName, ClientMode.AUTHEN);
            }
            if (inLockoutMode()) {
                LogUtil.e(TAG, "In lockout mode; disallowing authentication");
                if (!this.mAuthClient.sendError(7)) {
                    LogUtil.w(TAG, "Cannot send timeout message to client");
                }
                this.mAuthClient = null;
                return;
            }
            try {
                int result = daemon.authenticate(opId, groupId);
                if (result != 0) {
                    LogUtil.w(TAG, "startAuthentication failed, result=" + result);
                    handleError(this.mHalDeviceId, 1);
                }
            } catch (RemoteException e2) {
                LogUtil.e(TAG, "startAuthentication failed", e2);
            }
        } else {
            LogUtil.e(TAG, token + " has died, just skip this startAuthentication");
        }
    }

    void stopAuthentication(IBinder token, boolean initiatedByClient, boolean initiatedInLockOutMode) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        synchronized (this.mClientLock) {
            if (this.mAuthClient != null && this.mAuthClient.isKeyguard()) {
                this.mUnlockController.reset();
            }
        }
        if (daemon == null) {
            LogUtil.w(TAG, "stopAuthentication: no fingeprintd!");
            return;
        }
        ClientMonitor client = this.mAuthClient;
        if (client == null || client.token != token) {
            LogUtil.w(TAG, "stopAuthentication, client == null");
        } else if (token == null || token.isBinderAlive()) {
            LogUtil.d(TAG, "stopAuthentication initiatedByClient = " + initiatedByClient + ", initiatedInLockOutMode = " + initiatedInLockOutMode + ", packageName = " + client.getClientPackageName());
            if (initiatedByClient || initiatedInLockOutMode) {
                try {
                    int result = daemon.cancelAuthentication();
                    if (result != 0) {
                        LogUtil.w(TAG, "stopAuthentication failed, result=" + result);
                    }
                } catch (RemoteException e) {
                    LogUtil.e(TAG, "stopAuthentication failed", e);
                }
                if (!initiatedInLockOutMode) {
                    client.sendError(5);
                }
            }
            removeClient(this.mAuthClient);
        } else {
            LogUtil.e(TAG, token + " has died, just skip this stopAuthentication");
        }
    }

    void startRemove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver, boolean restricted, ClientInfo info) {
        LogUtil.d(TAG, "startRemove clientInfo " + info);
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startRemove: no fingeprintd!");
            return;
        }
        stopPendingOperations(true);
        synchronized (this.mClientLock) {
            this.mRemoveClient = new ClientMonitor(token, receiver, userId, groupId, restricted, info.mPid, info.mPackageName, ClientMode.REMOVE);
        }
        try {
            LogUtil.w(TAG, "startRemove fingerId = " + fingerId + ", groupId = " + groupId);
            int result = daemon.remove(fingerId, groupId);
            if (result != 0) {
                LogUtil.w(TAG, "startRemove with id = " + fingerId + " failed, result=" + result);
                handleError(this.mHalDeviceId, 1);
            }
        } catch (RemoteException e) {
            LogUtil.e(TAG, "startRemove failed", e);
        }
    }

    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        LogUtil.d(TAG, "Fingerprints number = " + this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size());
        return this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId);
    }

    public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        if (this.mFingerprintUtils.getFingerprintsForUser(this.mContext, getEffectiveUserIdRestricted(userId, opPackageName)).size() > 0) {
            return true;
        }
        return false;
    }

    boolean hasPermission(String permission) {
        return getContext().checkCallingOrSelfPermission(permission) == 0;
    }

    void checkPermission(String permission) {
        getContext().enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    int getEffectiveUserIdRestricted(int userId, String opPackageName) {
        FingerprintUtils fingerprintUtils = this.mFingerprintUtils;
        if (!FingerprintUtils.isMultiApp(userId, opPackageName)) {
            return userId;
        }
        LogUtil.d(TAG, "getEffectiveUserId for " + opPackageName + " when it is MultiApp");
        return getEffectiveUserId(userId);
    }

    int getEffectiveUserId(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um != null) {
            long callingIdentity = Binder.clearCallingIdentity();
            userId = um.getCredentialOwnerProfile(userId);
            Binder.restoreCallingIdentity(callingIdentity);
            return userId;
        }
        LogUtil.e(TAG, "Unable to acquire UserManager");
        return userId;
    }

    boolean isCurrentUserOrProfile(int userId) {
        List<UserInfo> profiles = UserManager.get(this.mContext).getEnabledProfiles(userId);
        int n = profiles.size();
        for (int i = 0; i < n; i++) {
            if (((UserInfo) profiles.get(i)).id == userId) {
                return true;
            }
        }
        return false;
    }

    private boolean isForegroundActivity(int uid, int pid) {
        try {
            List<RunningAppProcessInfo> procs = ActivityManagerNative.getDefault().getRunningAppProcesses();
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                RunningAppProcessInfo proc = (RunningAppProcessInfo) procs.get(i);
                if (proc.pid == pid && proc.uid == uid && proc.importance == 100) {
                    return true;
                }
            }
        } catch (RemoteException e) {
            LogUtil.w(TAG, "am.getRunningAppProcesses() failed");
        }
        return false;
    }

    private boolean canUseFingerprint(String opPackageName, boolean foregroundOnly, int uid, int pid) {
        if ("com.excelliance.dualaid".equals(opPackageName) || "com.oppo.autotest".equals(opPackageName) || "com.oppo.engineermode".equals(opPackageName) || "com.android.engineeringmode".equals(opPackageName) || "com.android.settings".equals(opPackageName)) {
            return true;
        }
        checkPermission("android.permission.USE_FINGERPRINT");
        if (isKeyguard(opPackageName)) {
            return true;
        }
        if (!isCurrentUserOrProfile(UserHandle.getCallingUserId())) {
            LogUtil.w(TAG, "Rejecting " + opPackageName + " ; not a current user or profile");
            return false;
        } else if (this.mAppOps.noteOp(55, uid, opPackageName) != 0) {
            LogUtil.w(TAG, "Rejecting " + opPackageName + " ; permission denied");
            return false;
        } else if (!foregroundOnly || isForegroundActivity(uid, pid)) {
            return true;
        } else {
            LogUtil.w(TAG, "Rejecting " + opPackageName + " ; not in foreground");
            return false;
        }
    }

    private boolean isKeyguard(String clientPackage) {
        return this.mKeyguardPackage.equals(clientPackage);
    }

    private void addLockoutResetMonitor(FingerprintServiceLockoutResetMonitor monitor) {
        if (!this.mLockoutMonitors.contains(monitor)) {
            this.mLockoutMonitors.add(monitor);
        }
    }

    private void removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor monitor) {
        this.mLockoutMonitors.remove(monitor);
    }

    private void notifyLockoutResetMonitors() {
        for (int i = 0; i < this.mLockoutMonitors.size(); i++) {
            ((FingerprintServiceLockoutResetMonitor) this.mLockoutMonitors.get(i)).sendLockoutReset();
        }
    }

    private void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length == 2 && "<fingerprint daemon started>".equals(args[0])) {
            LogUtil.d(TAG, "received fingerprintd started msg, pid = " + args[1]);
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (FingerprintService.this.getFingerprintDaemon() == null) {
                        LogUtil.e(FingerprintService.TAG, "dumpInner: no fingeprintd!");
                    }
                }
            });
            this.mHealthMonitor.fingerprintdSystemReady(Integer.parseInt(args[1]));
            return;
        }
        int opti = 0;
        while (opti < args.length) {
            String opt = args[opti];
            if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                break;
            }
            opti++;
            if ("-h".equals(opt)) {
                pw.println("fingerprint service dump options:");
                pw.println("  [-h] [cmd] ...");
                pw.println("  cmd may be one of:");
                pw.println("    l[log]: dynamically adjust fingerprint log ");
                return;
            }
            pw.println("Unknown argument: " + opt + "; use -h for help");
        }
        if (opti < args.length) {
            String cmd = args[opti];
            opti++;
            if ("log".equals(cmd) || "l".equals(cmd)) {
                dynamicallyConfigLogTag(pw, args);
                return;
            } else if ("debug_switch".equals(cmd)) {
                pw.println("  all=" + DEBUG);
                return;
            } else if ("reset".equals(cmd)) {
                int pid = this.mHealthMonitor.getFingerprintdPid();
                if (pid != -1) {
                    Process.sendSignal(pid, 3);
                }
                return;
            }
        }
        pw.println("DEBUG = " + DEBUG);
        pw.println("LogLevel : " + LogUtil.getLevelString());
        pw.println("mSensorType = " + this.mSensorType);
        pw.println("mHalDeviceId = " + this.mHalDeviceId);
        pw.println("mFingerprintEnabled = " + this.mFingerprintEnabled);
        pw.println("mFingerprintUnlockEnabled = " + this.mFingerprintUnlockEnabled);
        pw.println("screenState = " + this.mPowerManager.getScreenState());
        pw.println("mFingerprintSwitchHelper = " + this.mFingerprintSwitchHelper.dumpToString());
        synchronized (this.mClientLock) {
            pw.println("mEnrollClient = " + this.mEnrollClient);
            pw.println("mAuthClient = " + this.mAuthClient);
            pw.println("mRemoveClient = " + this.mRemoveClient);
        }
        pw.println("mFingerprintUtils dump");
        JSONObject jsonObj = new JSONObject();
        try {
            JSONArray sets = new JSONArray();
            for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                int userId = user.getUserHandle().getIdentifier();
                int N = this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size();
                JSONObject set = new JSONObject();
                set.put(DecryptTool.UNLOCK_TYPE_ID, userId);
                set.put("count", N);
                sets.put(set);
            }
            jsonObj.put("prints", sets);
        } catch (JSONException e) {
            LogUtil.e(TAG, "jsonObj formatting failure", e);
        }
        pw.println(jsonObj);
        pw.println("mHealthMonitor dump");
        this.mHealthMonitor.dump(fd, pw, args, "  ");
        this.mUnlockController.dump(fd, pw, args);
    }

    public void onStart() {
        LogUtil.d(TAG, "onStart");
        publishBinderService("fingerprint", new FingerprintServiceWrapper(this, null));
        int fingerprintdPid = this.mHealthMonitor.getFingerprintdPid();
        if (fingerprintdPid != -1) {
            this.mHealthMonitor.fingerprintdSystemReady(fingerprintdPid);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                IFingerprintDaemon daemon = FingerprintService.this.getFingerprintDaemon();
                LogUtil.d(FingerprintService.TAG, "Fingerprint HAL id: " + FingerprintService.this.mHalDeviceId);
            }
        });
        publishLocalService(FingerprintInternal.class, getFPMS().getFingerprintLocalService());
        listenForUserSwitches();
    }

    private void updateActiveGroup(int userId, String clientPackage) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon != null) {
            try {
                userId = getUserOrWorkProfileId(clientPackage, userId);
                if (userId != this.mCurrentUserId) {
                    File fpDir = new File(Environment.getUserSystemDirectory(userId), FP_DATA_DIR);
                    if (!fpDir.exists()) {
                        if (!fpDir.mkdir()) {
                            LogUtil.e(TAG, "Cannot make directory: " + fpDir.getAbsolutePath());
                            return;
                        } else if (!SELinux.restorecon(fpDir)) {
                            LogUtil.w(TAG, "Restorecons failed. Directory will have wrong label.");
                            return;
                        }
                    }
                    LogUtil.w(TAG, "updateActiveGroup setActiveGroup userId = " + userId);
                    daemon.setActiveGroup(userId, fpDir.getAbsolutePath().getBytes());
                    this.mCurrentUserId = userId;
                }
                this.mCurrentAuthenticatorId = daemon.getAuthenticatorId();
                LogUtil.d(TAG, "updateActiveGroup mCurrentAuthenticatorId = " + this.mCurrentAuthenticatorId);
            } catch (RemoteException e) {
                LogUtil.e(TAG, "Failed to setActiveGroup():", e);
            }
        }
    }

    private int getUserOrWorkProfileId(String clientPackage, int userId) {
        if (isKeyguard(clientPackage) || !isWorkProfile(userId)) {
            return getEffectiveUserId(userId);
        }
        return userId;
    }

    private boolean isWorkProfile(int userId) {
        UserInfo info = this.mUserManager.getUserInfo(userId);
        return info != null ? info.isManagedProfile() : false;
    }

    private void listenForUserSwitches() {
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                public void onUserSwitching(int newUserId) throws RemoteException {
                    FingerprintService.this.mHandler.obtainMessage(10, newUserId, 0).sendToTarget();
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                }

                public void onForegroundProfileSwitch(int newProfileId) {
                }
            }, TAG);
        } catch (RemoteException e) {
            LogUtil.w(TAG, "Failed to listen for user switching event", e);
        }
    }

    public long getAuthenticatorId(String opPackageName) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon != null) {
            try {
                this.mCurrentAuthenticatorId = daemon.getAuthenticatorId();
            } catch (RemoteException e) {
                LogUtil.e(TAG, "getAuthenticatorId failed", e);
            }
        }
        LogUtil.w(TAG, "getAuthenticatorId, opPackageName = " + opPackageName + ", mCurrentAuthenticatorId = " + this.mCurrentAuthenticatorId);
        return this.mCurrentAuthenticatorId;
    }

    public void monitor() {
        synchronized (this.mClientLock) {
        }
    }

    private void initSwitchUpdater() {
        this.mFingerprintSwitchHelper = new FingerprintSwitchHelper(this.mContext, this);
        if (!this.mHandler.hasMessages(12)) {
            this.mHandler.sendMessageAtFrontOfQueue(this.mHandler.obtainMessage(12));
        }
    }

    public void onFingerprintSwitchUpdate() {
        if (!this.mHandler.hasMessages(13)) {
            this.mHandler.sendMessageAtFrontOfQueue(this.mHandler.obtainMessage(13));
        }
    }

    private void handleSwitchInit() {
        this.mFingerprintSwitchHelper.initConfig();
    }

    private void handleSwitchUpdate() {
        boolean psensorSwitch = this.mFingerprintSwitchHelper.getPsensorSwitch();
        boolean tpProtectSwitch = this.mFingerprintSwitchHelper.getTPProtectSwitch();
        SystemProperties.set(FingerprintSwitchHelper.PROP_NAME_PSENSOR_SWITCH, psensorSwitch ? "true" : "false");
        SystemProperties.set(FingerprintSwitchHelper.PROP_NAME_TP_PROTECT_SWITCH, tpProtectSwitch ? "true" : "false");
        LogUtil.d(TAG, "onFingerprintSwitchUpdate, psensorSwitch = " + psensorSwitch + ", tpProtectSwitch = " + tpProtectSwitch);
        if (this.mTouchEventMonitorMode != null) {
            this.mTouchEventMonitorMode.onFingerprintSwitchUpdate();
        }
    }

    protected void handleImageInfoAcquired(int type, int quality, int match_score) {
        LogUtil.d(TAG, "handleImageInfoAcquired, type = " + type + ", quality = " + quality + ", match_score = " + match_score);
        synchronized (this.mClientLock) {
            if (this.mAuthClient != null) {
                if (this.mDcsStatisticsUtil != null) {
                    this.mDcsStatisticsUtil.sendImageInfo(type, quality, match_score);
                }
                if (this.mAuthClient.isKeyguard() && type != 0) {
                    this.mUnlockController.dispatchImageDirtyAuthenticated();
                }
                if (this.mAuthClient.isInLockOutWhiteList() && this.mAuthClient.sendImageInfo(type, quality, match_score)) {
                    removeClient(this.mAuthClient);
                }
            }
        }
    }

    protected void dispatchEngineeringInfoUpdated(EngineeringInfo info) {
        LogUtil.d(TAG, "dispatchEngineeringInfoUpdated()");
        synchronized (this.mClientLock) {
            if (this.mEngineeringInfoClient != null) {
                this.mEngineeringInfoClient.sendEngineeringInfoUpdated(info);
            }
        }
    }

    protected void dispatchMonitorEventTriggered(int type, String data) {
        LogUtil.d(TAG, "dispatchMonitorEventTriggered, type = " + type + ", data = " + data);
        synchronized (this.mClientLock) {
            if (this.mAuthClient != null && this.mAuthClient.isKeyguard()) {
                this.mAuthClient.sendMonitorEventTriggered(type, data);
            }
            if (this.mTouchClient != null && this.mTouchClient.isKeyguard()) {
                this.mTouchClient.sendMonitorEventTriggered(type, data);
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:0x004c, code:
            calculateTime("mUnlockController.dispatchTouchDown", android.os.SystemClock.uptimeMillis() - r0);
     */
    /* JADX WARNING: Missing block: B:22:0x0057, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dispatchTouchDown(long deviceId) {
        LogUtil.d(TAG, "dispatchTouchDown");
        long startTime = SystemClock.uptimeMillis();
        synchronized (this.mClientLock) {
            if (this.mAuthClient != null && this.mAuthClient.isKeyguard()) {
                Trace.traceBegin(4, "FingerprintTouchDown");
                getFPMS().openAllFramesDrawForKeyguard();
                this.mUnlockController.dispatchTouchDown();
                Trace.traceEnd(4);
            } else if (this.mEnrollClient != null) {
                this.mUnlockController.userActivity();
            } else if (this.mTouchClient != null) {
                this.mTouchEventMonitorMode.dispatchTouchDown();
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:0x0046, code:
            calculateTime("dispatchTouchUp", android.os.SystemClock.uptimeMillis() - r0);
     */
    /* JADX WARNING: Missing block: B:22:0x0051, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dispatchTouchUp(long deviceId) {
        LogUtil.d(TAG, "dispatchTouchUp");
        long startTime = SystemClock.uptimeMillis();
        synchronized (this.mClientLock) {
            if (this.mAuthClient != null && this.mAuthClient.isKeyguard()) {
                this.mUnlockController.dispatchTouchUp();
            } else if (this.mEnrollClient != null) {
                LogUtil.d(TAG, "send touchup event to enroll client");
                this.mUnlockController.userActivity();
                this.mEnrollClient.sendTouchUpEvent();
            } else if (this.mTouchClient != null) {
                this.mTouchEventMonitorMode.dispatchTouchUp();
            }
        }
    }

    private void syncTemplates(int[] fingerIds, int groupId) {
        if (fingerIds == null) {
            LogUtil.w(TAG, "fingerIds and groupIds are null");
            return;
        }
        int remainingTemplates = fingerIds.length;
        if (groupId != getCurrentUserId()) {
            LogUtil.w(TAG, "template is not beylong to the current user");
            return;
        }
        int fingerprintNum = getEnrolledFingerprintNum(groupId);
        LogUtil.d(TAG, "syncTemplates started, remainingTemplates = " + remainingTemplates + ", fingerprintNum = " + fingerprintNum + ", groupId = " + groupId);
        for (int i = 0; i < remainingTemplates; i++) {
            LogUtil.d(TAG, "fingerIds[" + i + "] = " + fingerIds[i]);
        }
        if (remainingTemplates != fingerprintNum) {
            this.mFingerprintUtils.syncFingerprintIdForUser(this.mContext, fingerIds, groupId);
        } else {
            LogUtil.d(TAG, "templates are synchronized, do nothing");
        }
    }

    private int getCurrentUserId() {
        try {
            return getEffectiveUserId(ActivityManagerNative.getDefault().getCurrentUser().id);
        } catch (RemoteException e) {
            LogUtil.w(TAG, "Failed to get current user id\n");
            return -10000;
        }
    }

    public int getEnrolledFingerprintNum(int userId) {
        return this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size();
    }

    private void calculateTime(String mode, long interval) {
    }

    private int startGetEnrollmentTotalTimes(IBinder token) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startGetEnrollmentTotalTimes: no fingeprintd!");
            return 0;
        }
        try {
            LogUtil.d(TAG, "startGetEnrollmentTotalTimes");
            this.mEnrollmentTotalTimes = daemon.getEnrollmentTotalTimes();
            return this.mEnrollmentTotalTimes;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "startGetEnrollmentTotalTimes failed", e);
            return 0;
        }
    }

    private int startPauseEnroll() {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startPauseEnroll: no fingeprintd!");
            return -1;
        }
        try {
            LogUtil.d(TAG, "startPauseEnroll");
            return daemon.pauseEnroll();
        } catch (RemoteException e) {
            LogUtil.e(TAG, "pauseEnroll failed", e);
            return -1;
        }
    }

    private int startContinueEnroll() {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startContinueEnroll: no fingeprintd!");
            return -1;
        }
        try {
            LogUtil.d(TAG, "startContinueEnroll");
            return daemon.continueEnroll();
        } catch (RemoteException e) {
            LogUtil.e(TAG, "continueEnroll failed", e);
            return -1;
        }
    }

    private int startPauseIdentify() {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startPauseIdentify: no fingeprintd!");
            return -1;
        }
        try {
            LogUtil.d(TAG, "startPauseIdentify");
            return daemon.pauseIdentify();
        } catch (RemoteException e) {
            LogUtil.e(TAG, "pauseIdentify failed", e);
            return -1;
        }
    }

    private int startContinueIdentify() {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startContinueIdentify: no fingeprintd!");
            return -1;
        }
        try {
            LogUtil.d(TAG, "startContinueIdentify");
            return daemon.continueIdentify();
        } catch (RemoteException e) {
            LogUtil.e(TAG, "continueIdentify failed", e);
            return -1;
        }
    }

    private void setScreenStateInternal(int state) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "setScreenStateInternal: no fingeprintd!");
            return;
        }
        try {
            LogUtil.d(TAG, "setScreenState ( " + state + " )");
            daemon.setScreenState(state);
        } catch (RemoteException e) {
            LogUtil.e(TAG, "setScreenState failed", e);
        }
    }

    private void startSetTouchEventListener(IBinder token, IFingerprintServiceReceiver receiver, int groupId, boolean restricted, ClientInfo info) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startSetTouchEventListener: no fingeprintd!");
            return;
        }
        stopPendingOperations(true);
        synchronized (this.mClientLock) {
            this.mTouchClient = new ClientMonitor(token, receiver, this.mCurrentUserId, groupId, restricted, info.mPid, info.mPackageName, ClientMode.TOUCH_LISTEN);
        }
        if (daemon != null) {
            try {
                LogUtil.w(TAG, "startSetTouchEventListener clientinfo " + info);
                int result = daemon.setTouchEventListener();
                if (result != 0) {
                    LogUtil.w(TAG, "startSetTouchEventListener failed, result=" + result);
                }
                if (this.mTouchClient != null && this.mTouchClient.isKeyguard()) {
                    this.mTouchEventMonitorMode.startTouchMonitor();
                }
            } catch (RemoteException e) {
                LogUtil.e(TAG, "setTouchEventListener failed", e);
            }
        }
    }

    void stopTouchEventListener(IBinder token, boolean initiatedByClient) {
        synchronized (this.mClientLock) {
            if (this.mTouchClient != null && this.mTouchClient.isKeyguard()) {
                this.mTouchEventMonitorMode.stopTouchMonitor();
            }
        }
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "stopTouchEventListener: no fingeprintd!");
            return;
        }
        ClientMonitor client = this.mTouchClient;
        if (client == null || client.token != token) {
            LogUtil.w(TAG, "stopTouchEventListener, mTouchClient == null");
        } else if (token == null || token.isBinderAlive()) {
            LogUtil.d(TAG, "stopTouchEventListener, packageName = " + client.getClientPackageName());
            if (initiatedByClient) {
                try {
                    int result = daemon.cancelTouchEventListener();
                    if (result != 0) {
                        LogUtil.w(TAG, "stopTouchEventListener failed, result=" + result);
                    }
                } catch (RemoteException e) {
                    LogUtil.e(TAG, "stopTouchEventListener failed", e);
                }
                client.sendError(5);
            }
            removeClient(this.mTouchClient);
        } else {
            LogUtil.e(TAG, token + " has died, just skip this stopTouchEventListener");
        }
    }

    private void startFinishUnLockedScreen(boolean isfinished, String opPackageName) {
        LogUtil.d(TAG, "startFinishUnLockedScreen, isfinished = " + isfinished + ", opPackageName = " + opPackageName);
        boolean inKeyguardLockMode = false;
        synchronized (this.mClientLock) {
            if (this.mAuthClient != null) {
                inKeyguardLockMode = this.mAuthClient.getClientMode() == ClientMode.TOUCH_LISTEN;
            }
            if (this.mTouchClient != null) {
                inKeyguardLockMode = this.mTouchClient.getClientMode() == ClientMode.TOUCH_LISTEN;
            }
        }
        if (inKeyguardLockMode) {
            this.mTouchEventMonitorMode.notifyScreenon();
        } else {
            this.mUnlockController.onScreenOnUnBlockedByFingerprint(isfinished);
        }
    }

    public void sendKeyGuardAuthenticated(int fingerId, int groupId, boolean screenOn) {
        Trace.traceBegin(4, "FingerprintAuthenticated");
        synchronized (this.mClientLock) {
            if (this.mAuthClient != null) {
                if (screenOn && fingerId != 0) {
                    if (this.mLockPatternUtils.getVerifyPwdAttemptDeadline(this.mCurrentUserId) - SystemClock.elapsedRealtime() > 5000) {
                        this.mKeyguardPolicy.setKeyguardVisibility(false);
                    }
                    this.mIUnLocker.sendUnlockTime(1003, SystemClock.uptimeMillis());
                }
                IBinder token = this.mAuthClient.token;
                if (this.mAuthClient.sendAuthenticated(fingerId, groupId)) {
                    stopAuthentication(token, false, false);
                    removeClient(this.mAuthClient);
                }
            }
        }
        Trace.traceEnd(4);
    }

    private FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }

    String codeToString(int operationCode) {
        switch (operationCode) {
            case 256:
                return "OPERATION_AUTHENTICATE";
            case OPERATION_CANCEL_AUTHENTICATION /*257*/:
                return "OPERATION_CANCEL_AUTHENTICATION";
            case OPERATION_ENROLL /*258*/:
                return "OPERATION_ENROLL";
            case OPERATION_CANCEL_ENROLLMENT /*259*/:
                return "OPERATION_CANCEL_ENROLLMENT";
            case OPERATION_REMOVE_FP /*260*/:
                return "OPERATION_REMOVE_FP";
            case OPERATION_RENAME_FP /*261*/:
                return "OPERATION_RENAME_FP";
            case OPERATION_PRE_ENROLL /*262*/:
                return "OPERATION_PRE_ENROLL";
            case OPERATION_POST_ENROLL /*263*/:
                return "OPERATION_POST_ENROLL";
            case 264:
                return "OPERATION_PAUSE_ENROLL";
            case OPERATION_CONTINUE_ENROLL /*265*/:
                return "OPERATION_CONTINUE_ENROLL";
            case 272:
                return "OPERATION_TOUCH_EVENT";
            case OPERATION_PAUSE_IDENTIFY /*274*/:
                return "OPERATION_PAUSE_IDENTIFY";
            case OPERATION_CONTINUE_IDENTIFY /*275*/:
                return "OPERATION_CONTINUE_IDENTIFY";
            case OPERATION_CANCEL_TOUCH_EVENT /*276*/:
                return "OPERATION_CANCEL_TOUCH_EVENT";
            default:
                return Integer.toString(operationCode);
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0042, code:
            return r1;
     */
    /* JADX WARNING: Missing block: B:21:0x004f, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:35:0x006f, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:40:0x0075, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean tryPreOperation(IBinder token, int operation) {
        boolean z = true;
        LogUtil.d(TAG, "try operation " + codeToString(operation));
        if (token != null && !token.isBinderAlive()) {
            return false;
        }
        synchronized (this.mClientLock) {
            if (this.mAuthClient != null) {
                IBinder authtoken = this.mAuthClient.token;
                if (this.mAuthClient.isKeyguard()) {
                    if (token != authtoken) {
                        z = false;
                    }
                } else if (!(authtoken == token || token == null)) {
                    if (operation == OPERATION_CANCEL_AUTHENTICATION || operation == OPERATION_CANCEL_ENROLLMENT) {
                    } else {
                        stopPendingOperations(true);
                    }
                }
            }
            if (!(this.mEnrollClient == null || this.mEnrollClient.getClientMode() != ClientMode.ENROLL || this.mEnrollClient.token == token || token == null)) {
                if (operation == OPERATION_CANCEL_AUTHENTICATION || operation == OPERATION_CANCEL_ENROLLMENT) {
                } else {
                    stopPendingOperations(true);
                }
            }
        }
    }

    void notifyOperationCanceled(IFingerprintServiceReceiver receiver) {
        if (receiver != null) {
            try {
                receiver.onError(this.mHalDeviceId, 5);
            } catch (RemoteException e) {
                LogUtil.w(TAG, "Failed to send error to receiver: ", e);
            }
        }
    }

    private int startGetAlikeyStatus() {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon != null) {
            try {
                LogUtil.w(TAG, "startGetAlikeyStatus");
                return daemon.getAlikeyStatus();
            } catch (RemoteException e) {
                LogUtil.e(TAG, "startGetAlikeyStatus failed", e);
            }
        }
        return -1;
    }

    private int startGetEngineeringInfo(IBinder token, boolean restricted, int groupId, IFingerprintServiceReceiver receiver, ClientInfo info, int type) {
        if (type == 1000) {
            this.mGlobalCycleClient = new ClientMonitor(token, receiver, this.mCurrentUserId, groupId, restricted, info.mPid, info.mPackageName, ClientMode.Global);
            return -1;
        }
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "startGetEngineeringInfo: no fingeprintd!");
            return -1;
        }
        stopPendingOperations(true);
        synchronized (this.mClientLock) {
            this.mEngineeringInfoClient = new ClientMonitor(token, receiver, this.mCurrentUserId, groupId, restricted, info.mPid, info.mPackageName, ClientMode.ENGINEERING_INFO);
        }
        try {
            LogUtil.w(TAG, "startGetEngineeringInfo clientinfo " + info);
            int result = daemon.getEngineeringInfo(type);
            if (result != 0) {
                LogUtil.w(TAG, "startGetEngineeringInfo failed, result=" + result);
            }
            return result;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "startGetEngineeringInfo failed", e);
            return -1;
        }
    }

    private void stopGetEngineeringInfo(IBinder token, boolean initiatedByClient, int type) {
        if (type == 1000) {
            removeGlobalCycleClient(this.mGlobalCycleClient);
            return;
        }
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w(TAG, "stopGetEngineeringInfo: no fingeprintd!");
            return;
        }
        ClientMonitor client = this.mEngineeringInfoClient;
        if (client == null || client.token != token) {
            LogUtil.w(TAG, "stopGetEngineeringInfo, mEngineeringInfoClient == null");
        } else if (token == null || token.isBinderAlive()) {
            LogUtil.w(TAG, "stopGetEngineeringInfo initiatedByClient = " + initiatedByClient);
            if (initiatedByClient) {
                try {
                    int result = daemon.cancelGetEngineeringInfo();
                    if (result != 0) {
                        LogUtil.w(TAG, "stopGetEngineeringInfo failed, result = " + result);
                    }
                } catch (RemoteException e) {
                    LogUtil.e(TAG, "stopGetEngineeringInfo failed", e);
                }
            }
            removeClient(this.mEngineeringInfoClient);
        } else {
            LogUtil.e(TAG, token + " has died, just skip this stopGetEngineeringInfo");
        }
    }

    private void removeGlobalCycleClient(ClientMonitor client) {
        if (client == this.mGlobalCycleClient) {
            LogUtil.d(TAG, "removeGlobalCycleClient");
            this.mGlobalCycleClient = null;
        }
    }

    private void startSetFingerprintEnabled(boolean enabled) {
        this.mFingerprintEnabled = enabled;
        LogUtil.d(TAG, "startSetFingerprintEnabled( " + enabled + " )");
    }

    private void cleanUp() {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon != null) {
            try {
                LogUtil.w(TAG, "cleanUp");
                int cleanUp = daemon.cleanUp();
            } catch (RemoteException e) {
                LogUtil.e(TAG, "cleanUp failed", e);
            }
        }
    }

    public void systemReady() {
        LogUtil.d(TAG, "systemReady");
        this.mSystemReady = true;
        this.mUnlockController.notifySystemReady();
        this.mFingerprintSwitchHelper.initUpdateBroadcastReceiver();
    }

    private ProximitySensorManager getPsensorManager() {
        return ProximitySensorManager.getProximitySensorManager();
    }

    protected void dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        int i = 1;
        pw.println("dynamicallyConfigLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]: " + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        String tag = args[1];
        boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag: " + tag + ", on: " + on);
        if ("all".equals(tag)) {
            DEBUG = on;
            LogUtil.dynamicallyConfigLog(on);
            IFingerprintDaemon daemon = getFingerprintDaemon();
            if (daemon != null) {
                if (!on) {
                    i = 0;
                }
                try {
                    daemon.dynamicallyConfigLog(i);
                    pw.println("daemon.dynamicallyConfigLog on: " + on);
                } catch (RemoteException e) {
                    LogUtil.e(TAG, "dynamicallyConfigLog failed", e);
                }
            }
        }
    }

    protected void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1. open all log in FingerprintService");
        pw.println("cmd: dumpsys fingerprint log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }
}
