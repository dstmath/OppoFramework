package com.android.server.biometrics.fingerprint;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.frameworks.fingerprintservice.V1_0.IFingerprintHalService;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.fingerprint.EngineeringInfo;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintClientActiveCallback;
import android.hardware.fingerprint.IFingerprintCommandCallback;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.hardware.fingerprint.IOpticalFingerprintListener;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Message;
import android.os.Parcel;
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
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import android.view.WindowManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.DumpUtils;
import com.android.server.ServiceThread;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.biometrics.AuthenticationClient;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.BiometricUtils;
import com.android.server.biometrics.ClientMonitor;
import com.android.server.biometrics.Constants;
import com.android.server.biometrics.EngineeringClient;
import com.android.server.biometrics.EnumerateClient;
import com.android.server.biometrics.LoggableMonitor;
import com.android.server.biometrics.RemovalClient;
import com.android.server.biometrics.TouchEventClient;
import com.android.server.biometrics.fingerprint.FingerprintSwitchHelper;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.biometrics.fingerprint.optical.IOpticalFingerprintEventListener;
import com.android.server.biometrics.fingerprint.optical.OpticalFingerprintManager;
import com.android.server.biometrics.fingerprint.power.FingerprintInternal;
import com.android.server.biometrics.fingerprint.power.FingerprintPowerManager;
import com.android.server.biometrics.fingerprint.sensor.IProximitySensorEventListener;
import com.android.server.biometrics.fingerprint.sensor.ProximitySensorManager;
import com.android.server.biometrics.fingerprint.setting.FingerprintUnlockSettingMonitor;
import com.android.server.biometrics.fingerprint.setting.Ilistener;
import com.android.server.biometrics.fingerprint.tool.ExHandler;
import com.android.server.biometrics.fingerprint.tool.HealthMonitor;
import com.android.server.biometrics.fingerprint.tool.HealthState;
import com.android.server.biometrics.fingerprint.touchmode.TouchEventMonitorMode;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.biometrics.fingerprint.util.SecrecyServiceHelper;
import com.android.server.biometrics.fingerprint.util.SupportUtil;
import com.android.server.biometrics.fingerprint.wakeup.BackTouchSensorUnlockController;
import com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener;
import com.android.server.biometrics.fingerprint.wakeup.TouchSensorUnlockController;
import com.android.server.biometrics.fingerprint.wakeup.UnlockController;
import com.android.server.oppo.OppoUsageService;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.utils.PriorityDump;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback;
import vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IFingerprintPay;

public class FingerprintService extends BiometricServiceBase implements FingerprintSwitchHelper.ISwitchUpdateListener {
    private static final String ACTION_LOCKOUT_RESET = "com.android.server.biometrics.fingerprint.ACTION_LOCKOUT_RESET";
    private static final boolean DEBUG = true;
    private static final int ENROLLMENT_TIMEOUT_MS = 60000;
    private static final int ERROR_RESET_FINGERPRINTD_REQUEST = 2001;
    private static final long FAIL_LOCKOUT_TIMEOUT_MS = 30000;
    public static final int FINGERPRINT_HAL_MODE_ENROLL = 1;
    public static final int FINGERPRINT_HAL_MODE_IDENTIFY = 0;
    public static final int FINGERPRINT_HAL_MODE_LOCK = 3;
    public static final int FINGERPRINT_HAL_MODE_SELFTEST = 2;
    public static boolean FINGER_DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final int FINGER_LAYER_HIDE = 1;
    public static final int FINGER_LAYER_SHOW = 0;
    private static final String FP_DATA_DIR = "fpdata";
    private static final String KEY_LOCKOUT_RESET_USER = "lockout_reset_user";
    public static final int LCD_HIGHLIGHT_OFF = 0;
    public static final int LCD_HIGHLIGHT_ON = 1;
    private static final String LCD_HIGHLIGHT_PATH = "/sys/kernel/oppo_display/notify_fppress";
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_PERMANENT = 20;
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_TIMED = 5;
    private static final int MSG_DAEMON_CALLBACK = 11;
    private static final int MSG_MONITOR_EVENT_TRIGGERED = 15;
    private static final int MSG_SIDE_FINGERPRINT_PRESSTOUCH_APP = 17;
    private static final int MSG_SIDE_FINGERPRINT_PRESSTOUCH_APP_DELAY = 16;
    private static final int MSG_SWITCH_INIT = 12;
    private static final int MSG_SWITCH_UPDATE = 13;
    private static final int MSG_UNCONSCIOUS_TOUCH_HAPPEND = 14;
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
    public static final int OPTICAL_FINGERPRINT_ENROLL_SHOW = 4;
    public static final int OPTICAL_FINGERPRINT_FAIL = 5;
    public static final String OPTICAL_FINGERPRINT_FEATURE = "oppo.hardware.fingerprint.optical.support";
    public static final int OPTICAL_FINGERPRINT_HIDE = 0;
    public static final int OPTICAL_FINGERPRINT_SHOW = 1;
    public static final int OPTICAL_FINGERPRINT_TOUCHDOWN = 2;
    public static final int OPTICAL_FINGERPRINT_TOUCHUP = 3;
    public static final int SCREEN_STATE_OFF = 0;
    public static final int SCREEN_STATE_ON = 1;
    public static final String SIDE_FINGERPRINT_FEATURE = "oppo.side.touch.fingerprint.sensor";
    public static final String TAG = "FingerprintService";
    public static final long TIMEOUT_GET_FINGERPRINTD = 10000;
    private static int mCurrentIconStatus = 0;
    public static boolean mPressTouchApp = false;
    public static boolean mPressTouchAppShouldClose = true;
    public static boolean mPressTouchAuthenticated = false;
    public static boolean mPressTouchEnable = false;
    public static boolean mPressTouchEnrolling = false;
    public static Fingerprint mPressTouchFp = null;
    public static ArrayList<Byte> mPressTouchTokenByte = null;
    /* access modifiers changed from: private */
    public boolean isTouchDownState = false;
    private final AlarmManager mAlarmManager;
    private IHwBinder.DeathRecipient mAliPayServiceDeathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass8 */

        public void serviceDied(long cookie) {
            LogUtil.d("FingerprintService", "fingerprintAlipayService died");
            IFingerprintPay unused = FingerprintService.this.mFingerprintPay = null;
        }
    };
    /* access modifiers changed from: private */
    public final CopyOnWriteArrayList<IFingerprintClientActiveCallback> mClientActiveCallbacks = new CopyOnWriteArrayList<>();
    /* access modifiers changed from: private */
    public Context mContext;
    private EngineeringClient mCurrentEngineerClient;
    /* access modifiers changed from: private */
    @GuardedBy({"this"})
    public IBiometricsFingerprint mDaemon;
    private IBiometricsFingerprintClientCallback mDaemonCallback = new IBiometricsFingerprintClientCallback.Stub() {
        /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3 */

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onEnrollResult(final long deviceId, final int fingerId, final int groupId, final int remaining) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass1 */

                public void run() {
                    LogUtil.d("FingerprintService", "onEnrollResult , fingerId = " + fingerId + ", groupId = " + groupId + ", remaining = " + remaining + ", deviceId=" + deviceId);
                    Fingerprint fingerprint = new Fingerprint(FingerprintService.this.getBiometricUtils().getUniqueName(FingerprintService.this.getContext(), groupId), groupId, fingerId, deviceId);
                    ClientMonitor client = FingerprintService.this.getCurrentClient();
                    if (remaining == 0 && client != null && (client instanceof BiometricServiceBase.EnrollClientImpl)) {
                        FingerprintService.this.updateOpticalFingerprintIcon(0);
                    }
                    AnonymousClass3.super.handleEnrollResult(fingerprint, remaining);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onAcquired(final long deviceId, final int acquiredInfo, final int vendorCode) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass2 */

                public void run() {
                    FingerprintService.this.handleAcquired(deviceId, acquiredInfo, vendorCode);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onAuthenticated(final long deviceId, final int fingerId, final int groupId, final ArrayList<Byte> token) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass3 */

                public void run() {
                    FingerprintService.this.handleAuthenticated(deviceId, fingerId, groupId, token);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onError(final long deviceId, final int error, final int vendorCode) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass4 */

                public void run() {
                    LogUtil.d("FingerprintService", "handleError error = " + error);
                    if (FingerprintService.ERROR_RESET_FINGERPRINTD_REQUEST == error) {
                        int pid = FingerprintService.this.mHealthMonitor.getFingerprintdPid();
                        if (pid != -1) {
                            LogUtil.e("FingerprintService", "ERROR_RESET_FINGERPRINTD_REQUEST called and kill pid=" + pid);
                            Process.sendSignal(pid, 3);
                            return;
                        }
                        return;
                    }
                    AnonymousClass3.super.handleError(deviceId, error, vendorCode);
                    if (error == 1) {
                        LogUtil.w("FingerprintService", "Got ERROR_HW_UNAVAILABLE; try reconnecting next client.");
                        synchronized (this) {
                            IBiometricsFingerprint unused = FingerprintService.this.mDaemon = null;
                            long unused2 = FingerprintService.this.mHalDeviceId = 0;
                            int unused3 = FingerprintService.this.mCurrentUserId = -10000;
                        }
                    }
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onRemoved(final long deviceId, final int fingerId, final int groupId, final int remaining) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass5 */

                public void run() {
                    LogUtil.d("FingerprintService", "handleRemoved fingerId = " + fingerId + " remaining:" + remaining + " groupId:" + groupId);
                    ClientMonitor client = FingerprintService.this.getCurrentClient();
                    if (client != null && fingerId == 0) {
                        FingerprintService.this.removeTemplateForUser(client.getTargetUserId(), fingerId);
                    }
                    AnonymousClass3.super.handleRemoved(new Fingerprint("", groupId, fingerId, deviceId), remaining);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onEnumerate(final long deviceId, final int fingerId, final int groupId, final int remaining) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass6 */

                public void run() {
                    AnonymousClass3.super.handleEnumerate(new Fingerprint("", groupId, fingerId, deviceId), remaining);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onSyncTemplates(final long deviceId, final ArrayList<Integer> fingerIdsArray, final int groupId) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass7 */

                public void run() {
                    int size = fingerIdsArray.size();
                    int[] fingerIds = new int[size];
                    for (int i = 0; i < size; i++) {
                        fingerIds[i] = ((Integer) fingerIdsArray.get(i)).intValue();
                    }
                    FingerprintService.this.handleSyncTemplates(deviceId, fingerIds, groupId);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onMonitorEventTriggered(final int type, final String data) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass8 */

                public void run() {
                    FingerprintService.this.dispatchMonitorEventTriggered(type, data);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onEngineeringInfoUpdated(int length, ArrayList<Integer> keys, ArrayList<String> values) {
            final EngineeringInfo info = new EngineeringInfo(length, keys, values);
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass9 */

                public void run() {
                    FingerprintService.this.dispatchEngineeringInfoUpdated(info);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onTouchDown(final long deviceId) {
            boolean unused = FingerprintService.this.mTouching = true;
            if (FingerprintService.this.mSideFingerprintSupport && FingerprintService.mPressTouchApp && !FingerprintService.this.mHandlerSub.hasMessages(17)) {
                FingerprintService.this.mHandlerSub.sendMessage(17);
            }
            boolean unused2 = FingerprintService.this.isTouchDownState = true;
            if (!FingerprintService.this.mOpticalFingerprintSupport || FingerprintService.this.mWindowMgr == null || FingerprintService.this.mWindowMgr.getDefaultDisplay() == null || FingerprintService.this.mWindowMgr.getDefaultDisplay().getState() != 1) {
                if (FingerprintService.this.mOpticalFingerprintIsShowing) {
                    if (FingerprintService.this.mLCDHighLightFileExist) {
                        FingerprintService.this.writeLcdHighLightPath(1);
                    } else {
                        int unused3 = FingerprintService.this.notifyDispFingerLayer(0);
                    }
                    FingerprintService.this.updateOpticalFingerprintIcon(2);
                }
                FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                    /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass10 */

                    public void run() {
                        FingerprintService.this.dispatchTouchDown(deviceId);
                    }
                });
                if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                    FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
                    return;
                }
                return;
            }
            LogUtil.e("FingerprintService", "onTouchDown called but state = Display.STATE_OFF so return");
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onTouchUp(final long deviceId) {
            boolean unused = FingerprintService.this.mTouching = false;
            boolean unused2 = FingerprintService.this.isTouchDownState = false;
            if (FingerprintService.this.mOpticalFingerprintIsShowing) {
                if (FingerprintService.this.mLCDHighLightFileExist) {
                    FingerprintService.this.writeLcdHighLightPath(0);
                } else {
                    int unused3 = FingerprintService.this.notifyDispFingerLayer(1);
                }
                FingerprintService.this.updateOpticalFingerprintIcon(3);
            } else if (FingerprintService.this.mOpticalFingerprintIsHighLight && !FingerprintService.this.mOpticalFingerprintIsShowing) {
                FingerprintService.this.updateOpticalFingerprintIcon(0);
            }
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass11 */

                public void run() {
                    FingerprintService.this.dispatchTouchUp(deviceId);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onImageInfoAcquired(final int type, final int quality, final int matchScore) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass12 */

                public void run() {
                    FingerprintService.this.handleImageInfoAcquired(type, quality, matchScore);
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }

        @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback
        public void onFingerprintCmd(final int cmdId, final ArrayList<Byte> result, final int resultLen) {
            FingerprintService.this.mDaemonCallbackQueue.add(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass3.AnonymousClass13 */

                public void run() {
                    if (FingerprintService.this.mFingerprintCommandCallback != null) {
                        byte[] result_byte = new byte[resultLen];
                        for (int i = 0; i < resultLen; i++) {
                            result_byte[i] = ((Byte) result.get(i)).byteValue();
                        }
                        try {
                            FingerprintService.this.mFingerprintCommandCallback.onFingerprintCmd(cmdId, result_byte);
                        } catch (RemoteException e) {
                            LogUtil.e("FingerprintService", "faied to call onFingerprintCmd, e:" + e);
                        }
                    } else {
                        LogUtil.e("FingerprintService", "mFingerprintCommandCallback is null");
                    }
                }
            });
            if (!FingerprintService.this.mExHandler.hasMessages(11)) {
                FingerprintService.this.mExHandler.sendMessageAtFrontOfQueue(FingerprintService.this.mExHandler.obtainMessage(11));
            }
        }
    };
    /* access modifiers changed from: private */
    public ConcurrentLinkedDeque<Runnable> mDaemonCallbackQueue = new ConcurrentLinkedDeque<>();
    private IBiometricsFingerprint mDaemonStub;
    /* access modifiers changed from: private */
    public final BiometricServiceBase.DaemonWrapper mDaemonWrapper = new BiometricServiceBase.DaemonWrapper() {
        /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass4 */

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int authenticate(long operationId, int groupId) throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon != null) {
                return daemon.authenticate(operationId, groupId);
            }
            Slog.w("FingerprintService", "authenticate(): no fingerprint HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int cancel() throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon != null) {
                return daemon.cancel();
            }
            Slog.w("FingerprintService", "cancel(): no fingerprint HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int remove(int groupId, int biometricId) throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon != null) {
                return daemon.remove(groupId, biometricId);
            }
            Slog.w("FingerprintService", "remove(): no fingerprint HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int enumerate() throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon != null) {
                return daemon.enumerate();
            }
            Slog.w("FingerprintService", "enumerate(): no fingerprint HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public int enroll(byte[] cryptoToken, int groupId, int timeout, ArrayList<Integer> arrayList) throws RemoteException {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon != null) {
                return daemon.enroll(cryptoToken, groupId, timeout);
            }
            Slog.w("FingerprintService", "enroll(): no fingerprint HAL!");
            return 3;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
        public void resetLockout(byte[] token) throws RemoteException {
            Slog.e("FingerprintService", "Not supported");
        }
    };
    /* access modifiers changed from: private */
    public DcsFingerprintStatisticsUtil mDcsStatisticsUtil;
    /* access modifiers changed from: private */
    public int mEnrollmentTotalTimes = 0;
    /* access modifiers changed from: private */
    public ExHandler mExHandler;
    /* access modifiers changed from: private */
    public final SparseIntArray mFailedAttempts;
    private IBiometricsFingerprint mFingerDaemonWrapper;
    public byte[] mFingerprintAuthToken = null;
    /* access modifiers changed from: private */
    public IFingerprintCommandCallback mFingerprintCommandCallback = null;
    private final FingerprintConstants mFingerprintConstants = new FingerprintConstants();
    private boolean mFingerprintEnabled = true;
    /* access modifiers changed from: private */
    public IFingerprintPay mFingerprintPay = null;
    private FingerprintSwitchHelper mFingerprintSwitchHelper;
    /* access modifiers changed from: private */
    public boolean mFingerprintUnlockEnabled = false;
    private FingerprintUnlockSettingMonitor mFingerprintUnlockSettingMonitor;
    private final FingerprintUtils mFingerprintUtils = FingerprintUtils.getInstance();
    /* access modifiers changed from: private */
    public ExHandler mHandlerSub;
    /* access modifiers changed from: private */
    public HealthMonitor mHealthMonitor;
    private IProximitySensorEventListener mIProximitySensorEventListener = new IProximitySensorEventListener() {
        /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass10 */

        @Override // com.android.server.biometrics.fingerprint.sensor.IProximitySensorEventListener
        public void onSensorChanged(boolean isNearState) {
            boolean isNear = isNearState && FingerprintService.this.mIsRegistered;
            if (isNear != FingerprintService.this.mIsNearState) {
                boolean unused = FingerprintService.this.mIsNearState = isNear;
                LogUtil.d("FingerprintService", "mIsNearState = " + FingerprintService.this.mIsNearState);
                FingerprintService.this.mUnlockController.onProximitySensorChanged(FingerprintService.this.mIsNearState);
                FingerprintService.this.mTouchEventMonitorMode.onProximitySensorChanged(FingerprintService.this.mIsNearState);
            }
        }

        @Override // com.android.server.biometrics.fingerprint.sensor.IProximitySensorEventListener
        public void onRegisterStateChanged(boolean isRegistered) {
            boolean unused = FingerprintService.this.mIsRegistered = isRegistered;
            boolean isNear = FingerprintService.this.mIsNearState && FingerprintService.this.mIsRegistered;
            if (isNear != FingerprintService.this.mIsNearState) {
                boolean unused2 = FingerprintService.this.mIsNearState = isNear;
                LogUtil.d("FingerprintService", "mIsNearState = " + FingerprintService.this.mIsNearState);
                FingerprintService.this.mUnlockController.onProximitySensorChanged(FingerprintService.this.mIsNearState);
                FingerprintService.this.mTouchEventMonitorMode.onProximitySensorChanged(FingerprintService.this.mIsNearState);
            }
        }
    };
    private IUnLocker mIUnLocker = new IUnLocker() {
        /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass9 */

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public void pressTouchChannel() {
            if (FingerprintService.this.mSideFingerprintSupport) {
                FingerprintService.mPressTouchAppShouldClose = !FingerprintService.this.isAppClientInTopPackage();
                if (FingerprintService.mPressTouchApp && FingerprintService.mPressTouchAppShouldClose && !FingerprintService.this.mHandlerSub.hasMessages(16)) {
                    FingerprintService.mPressTouchApp = false;
                }
            }
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public void dispatchScreenOnAuthenticatedEvent(Fingerprint fingerInfo, ArrayList<Byte> token) {
            LogUtil.d("FingerprintService", "dispatchScreenOnAuthenticatedEvent fingerId = " + fingerInfo.getBiometricId());
            FingerprintService.this.sendKeyGuardAuthenticated(fingerInfo, token, true);
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public void reauthentication(Fingerprint fingerInfo, ArrayList<Byte> token) {
            LogUtil.d("FingerprintService", "reauthentication");
            ClientMonitor client = FingerprintService.this.getCurrentClient();
            if (client != null && (client instanceof AuthenticationClient)) {
                FingerprintService.super.handleAuthenticated(fingerInfo, token);
            }
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public void dispatchScreenOffAuthenticatedEvent(Fingerprint fingerInfo, ArrayList<Byte> token) {
            LogUtil.d("FingerprintService", "dispatchScreenOffAuthenticatedEvent fingerId = " + fingerInfo.getBiometricId());
            boolean authenticated = fingerInfo.getBiometricId() != 0;
            FingerprintService.this.sendKeyGuardAuthenticated(fingerInfo, token, false);
            if (authenticated) {
                FingerprintService.this.mUnlockController.onScreenOnUnBlockedByFingerprint(authenticated);
            } else if (!authenticated) {
                LogUtil.d("FingerprintService", "wait call back from keyguard to screenon when false");
                FingerprintService.this.mUnlockController.dispatchScreenOnTimeOut();
            }
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public void dispatchTouchEventInLockMode() {
            ClientMonitor client = FingerprintService.this.getCurrentClient();
            if (client != null && (client instanceof TouchEventClient)) {
                LogUtil.d("FingerprintService", "sendTouchDownEvent");
                ((TouchEventClient) client).sendTouchDownEvent();
            }
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public void dispatchMonitorEvent(int type, String data) {
            FingerprintService.this.dispatchMonitorEventTriggered(type, data);
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public int pauseIdentify() {
            return FingerprintService.this.startPauseIdentify();
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public int continueIdentify() {
            return FingerprintService.this.startContinueIdentify();
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public void setScreenState(int state) {
            FingerprintService.this.setScreenStateInternal(state);
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public void sendUnlockTime(int type, long time) {
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public void onScreenOff(boolean isOff) {
            FingerprintService.this.mTouchEventMonitorMode.dispatchScreenOff(isOff);
            FingerprintService.this.getPsensorManager().dispatchScreenOff(isOff);
        }

        @Override // com.android.server.biometrics.fingerprint.FingerprintService.IUnLocker
        public boolean needsUnblockDelay() {
            return FingerprintService.this.mOpticalFingerprintSupport && !FingerprintService.this.mLCDHighLightFileExist;
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsNearState = false;
    /* access modifiers changed from: private */
    public boolean mIsRegistered = false;
    private KeyguardManager mKeyguardManager = null;
    /* access modifiers changed from: private */
    public boolean mLCDHighLightFileExist = false;
    private int mLastKeymodeEnable = 0;
    private Ilistener mListener = new Ilistener() {
        /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass11 */

        @Override // com.android.server.biometrics.fingerprint.setting.Ilistener
        public void onSettingChanged(String settingName, boolean isOn) {
            if ("coloros_fingerprint_unlock_switch".equals(settingName)) {
                LogUtil.d("FingerprintService", "onSettingChanged, settingName = " + settingName + ", isOn = " + isOn);
                boolean unused = FingerprintService.this.mFingerprintUnlockEnabled = isOn;
            }
            FingerprintService.this.mUnlockController.onSettingChanged(settingName, isOn);
        }
    };
    private long mLockoutDeadline;
    private final LockoutReceiver mLockoutReceiver = new LockoutReceiver();
    private IOpticalFingerprintEventListener mOpticalFingerprintEventListener;
    /* access modifiers changed from: private */
    public boolean mOpticalFingerprintIsHighLight = false;
    /* access modifiers changed from: private */
    public boolean mOpticalFingerprintIsShowing = false;
    /* access modifiers changed from: private */
    public IOpticalFingerprintListener mOpticalFingerprintListener = null;
    /* access modifiers changed from: private */
    public boolean mOpticalFingerprintSupport = false;
    private final PowerManager mPowerManager;
    protected final ResetFailedAttemptsForUserRunnable mResetFailedAttemptsForCurrentUserRunnable = new ResetFailedAttemptsForUserRunnable();
    private String mSensorType;
    private final ServiceThread mServiceSubThread;
    private final ServiceThread mServiceThread;
    /* access modifiers changed from: private */
    public boolean mSideFingerprintSupport = false;
    private boolean mSystemReady = false;
    /* access modifiers changed from: private */
    public ArrayList<JSONObject> mTagList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final SparseBooleanArray mTimedLockoutCleared;
    /* access modifiers changed from: private */
    public TouchEventMonitorMode mTouchEventMonitorMode;
    /* access modifiers changed from: private */
    public boolean mTouching = false;
    /* access modifiers changed from: private */
    public IFingerprintSensorEventListener mUnlockController;
    /* access modifiers changed from: private */
    public final WindowManager mWindowMgr;

    public interface IUnLocker {
        int continueIdentify();

        void dispatchMonitorEvent(int i, String str);

        void dispatchScreenOffAuthenticatedEvent(Fingerprint fingerprint, ArrayList<Byte> arrayList);

        void dispatchScreenOnAuthenticatedEvent(Fingerprint fingerprint, ArrayList<Byte> arrayList);

        void dispatchTouchEventInLockMode();

        boolean needsUnblockDelay();

        void onScreenOff(boolean z);

        int pauseIdentify();

        void pressTouchChannel();

        void reauthentication(Fingerprint fingerprint, ArrayList<Byte> arrayList);

        void sendUnlockTime(int i, long j);

        void setScreenState(int i);
    }

    private void initHandler() {
        this.mExHandler = new ExHandler(this.mServiceThread.getLooper()) {
            /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass1 */

            @Override // com.android.server.biometrics.fingerprint.tool.ExHandler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 11:
                        while (true) {
                            Runnable r = (Runnable) FingerprintService.this.mDaemonCallbackQueue.poll();
                            if (r != null) {
                                r.run();
                            } else {
                                return;
                            }
                        }
                    case 12:
                        FingerprintService.this.handleSwitchInit();
                        return;
                    case 13:
                        FingerprintService.this.handleSwitchUpdate();
                        return;
                    default:
                        LogUtil.w("FingerprintService", "Unknown message:" + msg.what);
                        return;
                }
            }
        };
        this.mHandlerSub = new ExHandler(this.mServiceSubThread.getLooper()) {
            /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass2 */

            @Override // com.android.server.biometrics.fingerprint.tool.ExHandler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int i = msg.what;
                if (i == 16) {
                    FingerprintService.mPressTouchApp = false;
                } else if (i != 17) {
                    LogUtil.w("FingerprintService", "Unknown message:" + msg.what);
                } else if (!FingerprintService.this.isAppClientInTopPackage()) {
                    FingerprintService.mPressTouchApp = false;
                }
            }
        };
    }

    /* access modifiers changed from: private */
    public boolean isAppClientInTopPackage() {
        if (getCurrentClient() != null && !isKeyguard(getCurrentClient().getOwnerString())) {
            String currentClient = getCurrentClient().getOwnerString();
            try {
                List<ActivityManager.RunningTaskInfo> runningTasks = this.mActivityTaskManager.getTasks(1);
                if (!runningTasks.isEmpty() && runningTasks.get(0).topActivity.getPackageName().contentEquals(currentClient)) {
                    LogUtil.w("FingerprintService", "currentClient is in TopPackage, currentClient= " + currentClient);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    class IfingerprintHalService extends IFingerprintHalService.Stub {
        IfingerprintHalService() {
        }

        @Override // android.frameworks.fingerprintservice.V1_0.IFingerprintHalService
        public int notifyInitFinished(int pid) {
            LogUtil.d("FingerprintService", "notifyInitFinished pid = " + pid);
            FingerprintService.this.mExHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.IfingerprintHalService.AnonymousClass1 */

                public void run() {
                    if (FingerprintService.this.getFingerprintDaemon() == null) {
                        LogUtil.e("FingerprintService", "notifyInitFinished: no fingerprintd!");
                    }
                }
            });
            FingerprintService.this.mHealthMonitor.fingerprintdSystemReady(pid);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public void startHidlService() {
        try {
            Slog.e("FingerprintService", "startHidlService");
            IfingerprintHalService ifingerprintHalService = new IfingerprintHalService();
            IfingerprintHalService.configureRpcThreadpool(5, false);
            ifingerprintHalService.registerAsService("fingerprintservice");
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "startHidlService  RemoteException " + e);
        }
    }

    private final class ResetFailedAttemptsForUserRunnable implements Runnable {
        private ResetFailedAttemptsForUserRunnable() {
        }

        public void run() {
            FingerprintService.this.resetFailedAttemptsForUser(true, ActivityManager.getCurrentUser());
        }
    }

    private final class LockoutReceiver extends BroadcastReceiver {
        private LockoutReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String tag = FingerprintService.this.getTag();
            Slog.v(tag, "Resetting lockout: " + intent.getAction());
            if (FingerprintService.this.getLockoutResetIntent().equals(intent.getAction())) {
                FingerprintService.this.resetFailedAttemptsForUser(false, intent.getIntExtra(FingerprintService.KEY_LOCKOUT_RESET_USER, 0));
            }
        }
    }

    private final class FingerprintAuthClient extends BiometricServiceBase.AuthenticationClientImpl {
        private long mOpId;

        /* access modifiers changed from: protected */
        @Override // com.android.server.biometrics.BiometricServiceBase.AuthenticationClientImpl
        public boolean isFingerprint() {
            return true;
        }

        public FingerprintAuthClient(Context context, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, long opId, boolean restricted, String owner, int cookie, boolean requireConfirmation) {
            super(context, daemon, halDeviceId, token, listener, targetUserId, groupId, opId, restricted, owner, cookie, requireConfirmation);
            this.mOpId = opId;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.biometrics.LoggableMonitor
        public int statsModality() {
            return FingerprintService.this.statsModality();
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public void resetFailedAttempts() {
            FingerprintService.this.resetFailedAttemptsForUser(true, ActivityManager.getCurrentUser());
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public boolean shouldFrameworkHandleLockout() {
            return true;
        }

        @Override // com.android.server.biometrics.AuthenticationClient
        public boolean wasUserDetected() {
            return false;
        }

        @Override // com.android.server.biometrics.AuthenticationClient, com.android.server.biometrics.BiometricServiceBase.AuthenticationClientImpl
        public int handleFailedAttempt() {
            int currentUser = ActivityManager.getCurrentUser();
            ClientMonitor client = FingerprintService.this.getCurrentClient();
            if (FingerprintService.this.mIsNearState && client != null && FingerprintService.this.isKeyguard(client.getOwnerString())) {
                LogUtil.e(LoggableMonitor.TAG, "fail result of Keyguard in near state, don't add count");
            } else if (FingerprintService.this.mOpticalFingerprintSupport) {
                FingerprintService.this.mFailedAttempts.put(currentUser, FingerprintService.this.mFailedAttempts.get(currentUser, 0) + 1);
            } else {
                FingerprintService.this.mFailedAttempts.put(currentUser, FingerprintService.this.mFailedAttempts.get(currentUser, 0) + 1);
            }
            LogUtil.i(LoggableMonitor.TAG, "At currentUser mFailedAttempts = " + FingerprintService.this.mFailedAttempts.get(currentUser, 0));
            FingerprintService.this.mTimedLockoutCleared.put(ActivityManager.getCurrentUser(), false);
            if (FingerprintService.this.getLockoutMode() != 0) {
                FingerprintService.this.scheduleLockoutResetForUser(currentUser);
                if (FingerprintService.this.mDcsStatisticsUtil != null) {
                    LogUtil.d(LoggableMonitor.TAG, "handleFailedAttempt sendLockoutMode");
                    FingerprintService.this.mDcsStatisticsUtil.sendLockoutMode();
                }
                FingerprintService.this.setLockoutAttemptDeadline(SystemClock.elapsedRealtime() + 30000);
            }
            return super.handleFailedAttempt();
        }

        public void sendMonitorEventTriggered(int type, String data) {
            try {
                if (getListener() != null) {
                    ((ServiceListenerImpl) getListener()).onMonitorEventTriggered(type, data);
                }
            } catch (RemoteException e) {
                LogUtil.w(LoggableMonitor.TAG, "Failed to notify onMonitorEventTriggered:", e);
            }
        }

        public boolean sendTouchDownEvent() {
            try {
                if (getListener() == null) {
                    return true;
                }
                ((ServiceListenerImpl) getListener()).onTouchDown();
                return true;
            } catch (RemoteException e) {
                LogUtil.w(LoggableMonitor.TAG, "Failed to notify onTouchDown:", e);
                return true;
            }
        }

        public boolean sendTouchUpEvent() {
            try {
                if (getListener() == null) {
                    return true;
                }
                ((ServiceListenerImpl) getListener()).onTouchUp();
                return true;
            } catch (RemoteException e) {
                LogUtil.w(LoggableMonitor.TAG, "Failed to notify onTouchUp:", e);
                return true;
            }
        }

        public boolean isInLockOutWhiteList() {
            return FingerprintService.this.isKeyguard(getOwnerString()) || "com.coloros.safecenter".equals(getOwnerString()) || "com.coloros.filemanager".equals(getOwnerString());
        }

        /* access modifiers changed from: private */
        public boolean sendImageInfo(int type, int quality, int matchScore) {
            try {
                if (getListener() == null) {
                    return true;
                }
                ((ServiceListenerImpl) getListener()).onImageInfoAcquired(type, quality, matchScore);
                return false;
            } catch (RemoteException e) {
                LogUtil.w(LoggableMonitor.TAG, "Failed to notify sendImageInfo:", e);
                return true;
            }
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public void updateOpticalFingerIcon(int status) {
            FingerprintService.this.updateOpticalFingerprintIcon(status);
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public void updateLcdHightLight(int values) {
            FingerprintService.this.updateLcdHightLight(values);
        }

        public boolean isFingerprintPay(String mPackageName) {
            return "com.eg.android.AlipayGphone".equals(mPackageName) || "com.tencent.mm".equals(mPackageName);
        }

        @Override // com.android.server.biometrics.AuthenticationClient, com.android.server.biometrics.ClientMonitor
        public int start() {
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon == null) {
                LogUtil.w(LoggableMonitor.TAG, "startAuthentication: no fingerprintd!");
                onError(getHalDeviceId(), 1, 0);
                return -1;
            }
            int fingerprintAuthType = 3;
            if (FingerprintService.this.isKeyguard(getOwnerString())) {
                fingerprintAuthType = 1;
            } else if (isFingerprintPay(getOwnerString())) {
                fingerprintAuthType = 2;
            }
            this.mStarted = true;
            onStart();
            try {
                int result = daemon.authenticateAsType(this.mOpId, getGroupId(), fingerprintAuthType);
                if (result != 0) {
                    LogUtil.w(LoggableMonitor.TAG, "startAuthentication failed, result=" + result);
                    this.mMetricsLogger.histogram(this.mConstants.tagAuthStartError(), result);
                    onError(getHalDeviceId(), 1, 0);
                    return result;
                }
                updateOpticalFingerIcon(1);
                LogUtil.w(LoggableMonitor.TAG, "client " + getOwnerString() + " is authenticateing asType...");
                return 0;
            } catch (RemoteException e) {
                Slog.e(getLogTag(), "startAuthentication failed", e);
                return 3;
            }
        }
    }

    private final class FingerprintServiceWrapper extends IFingerprintService.Stub {
        private FingerprintServiceWrapper() {
        }

        public long preEnroll(IBinder token) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            return FingerprintService.this.startPreEnroll(token);
        }

        public int postEnroll(IBinder token) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            return FingerprintService.this.startPostEnroll(token);
        }

        public void enroll(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (cryptoToken == null) {
                LogUtil.e("FingerprintService", "token is null");
            }
            LogUtil.d("FingerprintService", "enroll opPackageName:" + opPackageName);
            if (!FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_ENROLL)) {
                FingerprintService.this.notifyOperationCanceled(receiver);
                return;
            }
            FingerprintEnrollClient client = new FingerprintEnrollClient(FingerprintService.this.getContext(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), FingerprintService.this.mCurrentUserId, FingerprintService.this.getEffectiveUserIdRestricted(userId, opPackageName), cryptoToken, FingerprintService.this.isRestricted(), opPackageName, new int[0]) {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass1 */

                @Override // com.android.server.biometrics.EnrollClient
                public boolean shouldVibrate() {
                    return true;
                }

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FingerprintService.this.statsModality();
                }
            };
            FingerprintService.mPressTouchEnrolling = true;
            FingerprintService.this.enrollInternal(client, userId);
        }

        public void cancelEnrollment(IBinder token, String opPackageName) {
            LogUtil.d("FingerprintService", "cancelEnrollment, opPackageName = " + opPackageName);
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_CANCEL_ENROLLMENT)) {
                FingerprintService.mPressTouchEnrolling = false;
                FingerprintService.this.cancelEnrollmentInternal(token);
            }
        }

        public void authenticate(IBinder token, long opId, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            if (FingerprintService.this.mDcsStatisticsUtil != null) {
                FingerprintService.this.mDcsStatisticsUtil.clearMap();
            }
            LogUtil.d("FingerprintService", "authenticate, opPackageName = " + opPackageName);
            if (!FingerprintService.this.tryPreOperation(token, 256, opPackageName)) {
                FingerprintService.this.notifyOperationCanceled(receiver);
                return;
            }
            FingerprintService.this.updateActiveGroup(groupId, opPackageName);
            boolean restricted = FingerprintService.this.isRestricted();
            FingerprintService fingerprintService = FingerprintService.this;
            BiometricServiceBase.AuthenticationClientImpl client = new FingerprintAuthClient(fingerprintService.getContext(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), FingerprintService.this.mCurrentUserId, FingerprintService.this.getEffectiveUserIdRestricted(groupId, opPackageName), opId, restricted, opPackageName, 0, false);
            if (!FingerprintService.this.isKeyguard(opPackageName)) {
                if (FingerprintService.this.mHandlerSub.hasMessages(16)) {
                    FingerprintService.this.mHandlerSub.removeMessage(16);
                }
                FingerprintService.mPressTouchApp = true;
            }
            FingerprintService.this.authenticateInternal(client, opId, opPackageName);
        }

        public void prepareForAuthentication(IBinder token, long opId, int groupId, IBiometricServiceReceiverInternal wrapperReceiver, String opPackageName, int cookie, int callingUid, int callingPid, int callingUserId) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            if (FingerprintService.this.mDcsStatisticsUtil != null) {
                FingerprintService.this.mDcsStatisticsUtil.clearMap();
            }
            LogUtil.d("FingerprintService", "prepareForAuthentication, opPackageName = " + opPackageName);
            FingerprintService.this.updateActiveGroup(groupId, opPackageName);
            FingerprintService fingerprintService = FingerprintService.this;
            BiometricServiceBase.AuthenticationClientImpl client = new FingerprintAuthClient(fingerprintService.getContext(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new BiometricPromptServiceListenerImpl(wrapperReceiver), FingerprintService.this.mCurrentUserId, groupId, opId, true, opPackageName, cookie, false);
            if (!FingerprintService.this.isKeyguard(opPackageName)) {
                if (FingerprintService.this.mHandlerSub.hasMessages(16)) {
                    FingerprintService.this.mHandlerSub.removeMessage(16);
                }
                FingerprintService.mPressTouchApp = true;
            }
            FingerprintService.this.authenticateInternal(client, opId, opPackageName, callingUid, callingPid, callingUserId);
        }

        public void startPreparedClient(int cookie) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            LogUtil.d("FingerprintService", "startPreparedClient, cookie = " + cookie);
            FingerprintService.this.startCurrentClient(cookie);
        }

        public void cancelAuthentication(IBinder token, String opPackageName) {
            LogUtil.d("FingerprintService", "cancelAuthentication, opPackageName = " + opPackageName);
            if (FingerprintService.this.tryPreOperation(token, 257, opPackageName)) {
                if (FingerprintService.this.mSideFingerprintSupport && !FingerprintService.this.isKeyguard(opPackageName) && !FingerprintService.this.mHandlerSub.hasMessages(16)) {
                    FingerprintService.this.mHandlerSub.sendMessageDelayed(FingerprintService.this.mHandlerSub.obtainMessage(16), 500);
                }
                FingerprintService.this.cancelAuthenticationInternal(token, opPackageName);
                if (FingerprintService.this.isTouchDownState && FingerprintService.this.mUnlockController != null && FingerprintService.this.isKeyguard(opPackageName)) {
                    FingerprintService.this.mUnlockController.dispatchTouchUp();
                }
            }
        }

        public void cancelAuthenticationFromService(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_BIOMETRIC");
            LogUtil.d("FingerprintService", "cancelAuthenticationFromService, opPackageName = " + opPackageName);
            if (FingerprintService.this.mSideFingerprintSupport && !FingerprintService.this.isKeyguard(opPackageName) && !FingerprintService.this.mHandlerSub.hasMessages(16)) {
                FingerprintService.this.mHandlerSub.sendMessageDelayed(FingerprintService.this.mHandlerSub.obtainMessage(16), 500);
            }
            FingerprintService.this.cancelAuthenticationInternal(token, opPackageName, callingUid, callingPid, callingUserId, fromClient);
        }

        public void setActiveUser(int userId) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.setActiveUserInternal(userId);
        }

        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver, String opPackageName) {
            LogUtil.d("FingerprintService", "remove fingerId = " + fingerId + ", groupId=  " + groupId + " opPackageName= " + opPackageName);
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (!FingerprintService.this.tryPreOperation(token, 260)) {
                FingerprintService.this.notifyOperationCanceled(receiver);
            } else if (token == null) {
                Slog.w("FingerprintService", "remove(): token is null");
            } else {
                FingerprintService.this.removeInternal(new RemovalClient(FingerprintService.this.getContext(), FingerprintService.this.getConstants(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), fingerId, FingerprintService.this.getEffectiveUserIdRestricted(groupId, opPackageName), userId, FingerprintService.this.isRestricted(), token.toString(), FingerprintService.this.getBiometricUtils()) {
                    /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass2 */

                    /* access modifiers changed from: protected */
                    @Override // com.android.server.biometrics.LoggableMonitor
                    public int statsModality() {
                        return FingerprintService.this.statsModality();
                    }
                });
            }
        }

        public void enumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver, String opPackageName) {
            LogUtil.d("FingerprintService", "enumerate  opPackageName= " + opPackageName);
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.enumerateInternal(new EnumerateClient(FingerprintService.this.getContext(), FingerprintService.this.getConstants(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), userId, userId, FingerprintService.this.isRestricted(), FingerprintService.this.getContext().getOpPackageName()) {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass3 */

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FingerprintService.this.statsModality();
                }
            });
        }

        public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) throws RemoteException {
            FingerprintService.super.addLockoutResetCallback(callback);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(FingerprintService.this.getContext(), "FingerprintService", pw)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                        FingerprintService.this.dumpInternal(pw);
                        FingerprintService.this.dumpInternal(fd, pw, args);
                    } else {
                        FingerprintService.this.dumpProto(fd);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public boolean isHardwareDetected(long deviceId, String opPackageName) {
            boolean z = false;
            if (!FingerprintService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return false;
            }
            long token = Binder.clearCallingIdentity();
            try {
                if (!(FingerprintService.this.getFingerprintDaemon() == null || FingerprintService.this.mHalDeviceId == 0)) {
                    z = true;
                }
                return z;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void rename(final int fingerId, final int groupId, final String name) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d("FingerprintService", "[rename] fingerId = " + fingerId + ", groupId = " + groupId + ", name = " + name);
            if (FingerprintService.this.isCurrentUserOrProfile(groupId)) {
                FingerprintService.this.mExHandler.post(new Runnable() {
                    /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass4 */

                    public void run() {
                        FingerprintService.this.getBiometricUtils().renameBiometricForUser(FingerprintService.this.getContext(), FingerprintService.this.getEffectiveUserIdRestricted(groupId, "none"), fingerId, name);
                    }
                });
            }
        }

        public List<Fingerprint> getEnrolledFingerprints(int userId, String opPackageName) {
            if (!FingerprintService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return Collections.emptyList();
            }
            FingerprintService fingerprintService = FingerprintService.this;
            return fingerprintService.getEnrolledTemplates(fingerprintService.getEffectiveUserIdRestricted(userId, opPackageName));
        }

        public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
            if (!FingerprintService.this.canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return false;
            }
            FingerprintService fingerprintService = FingerprintService.this;
            return fingerprintService.hasEnrolledBiometrics(fingerprintService.getEffectiveUserIdRestricted(userId, opPackageName));
        }

        public long getAuthenticatorId(String opPackageName) {
            return FingerprintService.super.getAuthenticatorId(opPackageName);
        }

        public void resetTimeout(byte[] token) {
            FingerprintService.this.checkPermission("android.permission.RESET_FINGERPRINT_LOCKOUT");
            FingerprintService fingerprintService = FingerprintService.this;
            if (!fingerprintService.hasEnrolledBiometrics(fingerprintService.mCurrentUserId)) {
                Slog.w("FingerprintService", "Ignoring lockout reset, no templates enrolled");
            } else {
                FingerprintService.this.mExHandler.post(FingerprintService.this.mResetFailedAttemptsForCurrentUserRunnable);
            }
        }

        public boolean isClientActive() {
            boolean z;
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            synchronized (FingerprintService.this) {
                if (FingerprintService.this.getCurrentClient() == null) {
                    if (FingerprintService.this.getPendingClient() == null) {
                        z = false;
                    }
                }
                z = true;
            }
            return z;
        }

        public void addClientActiveCallback(IFingerprintClientActiveCallback callback) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mClientActiveCallbacks.add(callback);
        }

        public void removeClientActiveCallback(IFingerprintClientActiveCallback callback) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mClientActiveCallbacks.remove(callback);
        }

        public int getEngineeringInfo(IBinder token, String opPackageName, int userId, IFingerprintServiceReceiver receiver, int type) {
            int pid = Binder.getCallingPid();
            int effectiveGroupId = FingerprintService.this.getEffectiveUserId(userId);
            if (!FingerprintService.this.canUseBiometric(opPackageName, true, Binder.getCallingUid(), pid, userId)) {
                LogUtil.d("FingerprintService", "Fingerprint getEngineeringInfo failed , " + opPackageName + " is not allowed this op");
                return -1;
            }
            LogUtil.d("FingerprintService", "getEngineeringInfo type = " + type);
            return FingerprintService.this.engineerInternal(new EngineeringClient(FingerprintService.this.getContext(), FingerprintService.this.getConstants(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), FingerprintService.this.mCurrentUserId, effectiveGroupId, FingerprintService.this.isRestricted(), opPackageName, 0, type, FingerprintService.this.getFingerprintDaemon()) {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass5 */

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FingerprintService.this.statsModality();
                }

                @Override // com.android.server.biometrics.EngineeringClient
                public void sendEngineeringInfoUpdated(EngineeringInfo info) {
                    LogUtil.w(TAG, "sendEngineeringInfoUpdated");
                    try {
                        if (getListener() != null) {
                            ((ServiceListenerImpl) getListener()).onEngineeringInfoUpdated(info);
                        }
                    } catch (RemoteException e) {
                        LogUtil.w(TAG, "Failed to notify onEngineeringInfoUpdated:", e);
                    }
                }
            });
        }

        public void cancelGetEngineeringInfo(IBinder token, String opPackageName, int type) {
            LogUtil.d("FingerprintService", "cancelGetEngineeringInfo type = " + type);
            FingerprintService.this.stopGetEngineeringInfo(token, true, type);
        }

        public int getAlikeyStatus() {
            return FingerprintService.this.startGetAlikeyStatus();
        }

        private boolean isSystemApp(String packageName) {
            PackageManager manager;
            if (FingerprintService.this.mContext == null || (manager = FingerprintService.this.mContext.getPackageManager()) == null) {
                return false;
            }
            try {
                if ((manager.getPackageInfoAsUser(packageName, 0, UserHandle.myUserId()).applicationInfo.flags & 1) != 0) {
                    return true;
                }
                return false;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void showFingerprintIcon(String opPackageName) {
            if (!isSystemApp(opPackageName)) {
                LogUtil.e("FingerprintService", "op is not system app  opPackageName = " + opPackageName);
                return;
            }
            LogUtil.d("FingerprintService", "[showFingerprintIcon] opPackageName = " + opPackageName);
            deleteTag(opPackageName);
            ClientMonitor client = FingerprintService.this.getCurrentClient();
            if (client != null) {
                LogUtil.d("FingerprintService", "[showFingerprintIcon] client = " + client.getOwnerString());
            }
            if (client == null || client.getOwnerString() == null || getCurrentIconStatus() == 0) {
                if (client == null || client.getOwnerString() == null || !client.getOwnerString().equalsIgnoreCase(opPackageName)) {
                    if (FingerprintService.this.mTagList.size() > 0) {
                        return;
                    }
                } else if (FingerprintService.this.mTagList != null && FingerprintService.this.mTagList.size() > 0) {
                    FingerprintService.this.mTagList.clear();
                }
                FingerprintService.this.updateOpticalFingerprintIcon(getCurrentIconStatus(), false);
                return;
            }
            if (FingerprintService.this.mTagList != null && FingerprintService.this.mTagList.size() > 0) {
                FingerprintService.this.mTagList.clear();
            }
            if (client instanceof FingerprintEnrollClient) {
                FingerprintService.this.updateOpticalFingerprintIcon(4, false);
            } else {
                FingerprintService.this.updateOpticalFingerprintIcon(1, false);
            }
        }

        public void hideFingerprintIcon(int status, String opPackageName) {
            if (!isSystemApp(opPackageName)) {
                LogUtil.e("FingerprintService", "op is not system app  opPackageName = " + opPackageName);
                return;
            }
            LogUtil.d("FingerprintService", "[hideFingerprintIcon] opPackageName = " + opPackageName);
            addTag(opPackageName);
            FingerprintService.this.updateOpticalFingerprintIcon(status, false);
        }

        public int getCurrentIconStatus() {
            return FingerprintService.this.getCurrentIconStatus();
        }

        private void addTag(String opPackageName) {
            if (FingerprintService.this.mTagList != null && FingerprintService.this.mTagList.size() > 0) {
                int i = 0;
                while (i < FingerprintService.this.mTagList.size()) {
                    LogUtil.d("FingerprintService", "addTag opPackageName = " + opPackageName);
                    try {
                        if (!opPackageName.equalsIgnoreCase(((JSONObject) FingerprintService.this.mTagList.get(i)).getString(opPackageName))) {
                            i++;
                        } else {
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            JSONObject tagItem = new JSONObject();
            try {
                tagItem.put(opPackageName, opPackageName);
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            FingerprintService.this.mTagList.add(tagItem);
        }

        private void deleteTag(String opPackageName) {
            if (FingerprintService.this.mTagList != null && FingerprintService.this.mTagList.size() > 0) {
                for (int i = 0; i < FingerprintService.this.mTagList.size(); i++) {
                    LogUtil.d("FingerprintService", "[deleteTag] opPackageName = " + opPackageName);
                    try {
                        if (opPackageName.equalsIgnoreCase(((JSONObject) FingerprintService.this.mTagList.get(i)).getString(opPackageName))) {
                            FingerprintService.this.mTagList.remove(i);
                        }
                        LogUtil.d("FingerprintService", "[deleteTag] mTagList = " + FingerprintService.this.mTagList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void setFingerprintEnabled(final boolean enabled) {
            LogUtil.d("FingerprintService", "setFingerprintEnabled ( " + enabled + " )");
            FingerprintService.this.mExHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass6 */

                public void run() {
                    FingerprintService.this.startSetFingerprintEnabled(enabled);
                }
            });
        }

        public int getEnrollmentTotalTimes(final IBinder token) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (FingerprintService.this.mEnrollmentTotalTimes != 0) {
                LogUtil.d("FingerprintService", "has got total times, just return it");
                return FingerprintService.this.mEnrollmentTotalTimes;
            }
            final PendingResult<Integer> r = new PendingResult<>(0);
            FingerprintService.this.mExHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass7 */

                public void run() {
                    if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_GET_ENROLLMENT_TOTALTIMES)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startGetEnrollmentTotalTimes(token)));
                    } else {
                        r.cancel();
                    }
                }
            });
            return r.await().intValue();
        }

        public int pauseEnroll() {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final PendingResult<Integer> r = new PendingResult<>(-1);
            FingerprintService.this.mExHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass8 */

                public void run() {
                    if (FingerprintService.this.tryPreOperation(null, FingerprintService.OPERATION_PAUSE_ENROLL)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startPauseEnroll()));
                    } else {
                        r.cancel();
                    }
                }
            });
            return r.await().intValue();
        }

        public int continueEnroll() {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final PendingResult<Integer> r = new PendingResult<>(-1);
            FingerprintService.this.mExHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass9 */

                public void run() {
                    if (FingerprintService.this.tryPreOperation(null, FingerprintService.OPERATION_CONTINUE_ENROLL)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startContinueEnroll()));
                    } else {
                        r.cancel();
                    }
                }
            });
            return r.await().intValue();
        }

        public void setTouchEventListener(IBinder token, IFingerprintServiceReceiver receiver, int groupId, String opPackageName) {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            LogUtil.d("FingerprintService", "setTouchEventListener, opPackageName = " + opPackageName);
            FingerprintService.this.getEffectiveUserId(groupId);
            Binder.getCallingPid();
            FingerprintService.this.touchEventInternal(new TouchEventClient(FingerprintService.this.getContext(), FingerprintService.this.getConstants(), FingerprintService.this.mDaemonWrapper, FingerprintService.this.mHalDeviceId, token, new ServiceListenerImpl(receiver), FingerprintService.this.mCurrentUserId, groupId, FingerprintService.this.isRestricted(), opPackageName, 0, FingerprintService.this.isKeyguard(opPackageName), FingerprintService.this.mTouchEventMonitorMode, FingerprintService.this.getFingerprintDaemon()) {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass10 */

                /* access modifiers changed from: protected */
                @Override // com.android.server.biometrics.LoggableMonitor
                public int statsModality() {
                    return FingerprintService.this.statsModality();
                }

                @Override // com.android.server.biometrics.TouchEventClient
                public void sendMonitorEventTriggered(int type, String data) {
                    try {
                        if (getListener() != null) {
                            ((ServiceListenerImpl) getListener()).onMonitorEventTriggered(type, data);
                        }
                    } catch (RemoteException e) {
                        LogUtil.w(TAG, "Failed to notify onMonitorEventTriggered:", e);
                    }
                }

                @Override // com.android.server.biometrics.TouchEventClient
                public boolean sendTouchDownEvent() {
                    try {
                        if (getListener() == null) {
                            return true;
                        }
                        ((ServiceListenerImpl) getListener()).onTouchDown();
                        return true;
                    } catch (RemoteException e) {
                        LogUtil.w(TAG, "Failed to notify onTouchDown:", e);
                        return true;
                    }
                }

                @Override // com.android.server.biometrics.TouchEventClient
                public boolean sendTouchUpEvent() {
                    try {
                        if (getListener() == null) {
                            return true;
                        }
                        ((ServiceListenerImpl) getListener()).onTouchUp();
                        return true;
                    } catch (RemoteException e) {
                        LogUtil.w(TAG, "Failed to notify onTouchUp:", e);
                        return true;
                    }
                }
            });
        }

        public void setFingerKeymode(final IBinder token, int groupId, String opPackageName, final int enable) {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            final int effectiveGroupId = FingerprintService.this.getEffectiveUserId(groupId);
            FingerprintService.this.mHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass11 */

                public void run() {
                    FingerprintService.this.startSetFingerKeymode(token, effectiveGroupId, FingerprintService.this.isRestricted(), enable);
                }
            });
        }

        public void cancelTouchEventListener(IBinder token, String opPackageName) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d("FingerprintService", "cancelTouchEventListener, opPackageName = " + opPackageName);
            FingerprintService.this.cancelTouchEventInternal(token);
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

        public int pauseIdentify(final IBinder token) {
            final PendingResult<Integer> r = new PendingResult<>(-1);
            FingerprintService.this.mExHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass12 */

                public void run() {
                    if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_PAUSE_IDENTIFY)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startPauseIdentify()));
                    } else {
                        r.cancel();
                    }
                }
            });
            return r.await().intValue();
        }

        public int continueIdentify(final IBinder token) {
            final PendingResult<Integer> r = new PendingResult<>(-1);
            FingerprintService.this.mExHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass13 */

                public void run() {
                    if (FingerprintService.this.tryPreOperation(token, FingerprintService.OPERATION_CONTINUE_IDENTIFY)) {
                        r.setResult(Integer.valueOf(FingerprintService.this.startContinueIdentify()));
                    } else {
                        r.cancel();
                    }
                }
            });
            return r.await().intValue();
        }

        public long getLockoutAttemptDeadline(String opPackageName) {
            LogUtil.d("FingerprintService", "getLockoutAttemptDeadline opPackageName = " + opPackageName);
            return FingerprintService.this.getLockoutAttemptDeadline();
        }

        public int getFailedAttempts(String opPackageName) {
            LogUtil.d("FingerprintService", "getFailedAttempts opPackageName = " + opPackageName);
            final PendingResult<Integer> r = new PendingResult<>(0);
            FingerprintService.this.mExHandler.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.FingerprintServiceWrapper.AnonymousClass14 */

                public void run() {
                    r.setResult(Integer.valueOf(FingerprintService.this.getFailedAttempts()));
                }
            });
            return r.await().intValue();
        }

        public byte[] alipayInvokeCommand(byte[] param) {
            if (FingerprintService.this.mFingerprintPay == null) {
                FingerprintService fingerprintService = FingerprintService.this;
                IFingerprintPay unused = fingerprintService.mFingerprintPay = fingerprintService.getAliPayService();
            }
            if (FingerprintService.this.mFingerprintPay == null) {
                LogUtil.w("FingerprintService", "alipayInvokeCommand: no FingerprintPayService!");
                return null;
            }
            byte[] receiveBuffer = null;
            try {
                ArrayList<Byte> paramByteArray = new ArrayList<>();
                for (byte b : param) {
                    paramByteArray.add(new Byte(b));
                }
                new ArrayList();
                ArrayList<Byte> receiveBufferByteArray = FingerprintService.this.mFingerprintPay.alipay_invoke_command(paramByteArray);
                receiveBuffer = new byte[receiveBufferByteArray.size()];
                for (int i = 0; i < receiveBufferByteArray.size(); i++) {
                    receiveBuffer[i] = receiveBufferByteArray.get(i).byteValue();
                }
            } catch (RemoteException e) {
                LogUtil.e("FingerprintService", "alipay_invoke_command failed", e);
            }
            return receiveBuffer;
        }

        public int touchDown() {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d("FingerprintService", HealthState.TOUCHDOWN);
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon == null) {
                return -1;
            }
            try {
                return daemon.touchDown();
            } catch (RemoteException e) {
                LogUtil.e("FingerprintService", "Failed to touchDown():", e);
                return -1;
            }
        }

        public int touchUp() {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d("FingerprintService", HealthState.TOUCHUP);
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon == null) {
                return -1;
            }
            try {
                return daemon.touchUp();
            } catch (RemoteException e) {
                LogUtil.e("FingerprintService", "Failed to touchUp():", e);
                return -1;
            }
        }

        public int sendFingerprintCmd(int cmdId, byte[] inbuf) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d("FingerprintService", HealthState.SENDFINGRTPRINTCMD);
            IBiometricsFingerprint daemon = FingerprintService.this.getFingerprintDaemon();
            if (daemon == null) {
                return -1;
            }
            try {
                ArrayList<Byte> inbufByteArrayList = new ArrayList<>();
                for (byte b : inbuf) {
                    inbufByteArrayList.add(new Byte(b));
                }
                return daemon.sendFingerprintCmd(cmdId, inbufByteArrayList);
            } catch (RemoteException e) {
                LogUtil.e("FingerprintService", "Failed to sendFingerprintCmd():", e);
                return -1;
            }
        }

        public byte[] getFingerprintAuthToken(String opPackageName) {
            return FingerprintService.this.getFingerprintAuthToken(opPackageName);
        }

        public int regsiterFingerprintCmdCallback(IFingerprintCommandCallback callback) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d("FingerprintService", "regsiterFingerprintCmdCallback");
            IFingerprintCommandCallback unused = FingerprintService.this.mFingerprintCommandCallback = callback;
            return 0;
        }

        public int unregsiterFingerprintCmdCallback(IFingerprintCommandCallback callback) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d("FingerprintService", "unregsiterFingerprintCmdCallback");
            IFingerprintCommandCallback unused = FingerprintService.this.mFingerprintCommandCallback = null;
            return 0;
        }

        public int regsiterOpticalFingerprintListener(IOpticalFingerprintListener listener) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d("FingerprintService", "regsiterOpticalFingerprintListener");
            IOpticalFingerprintListener unused = FingerprintService.this.mOpticalFingerprintListener = listener;
            return 0;
        }

        public int unregsiterOpticalFingerprintListener() {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            LogUtil.d("FingerprintService", "unregsiterOpticalFingerprintListener");
            IOpticalFingerprintListener unused = FingerprintService.this.mOpticalFingerprintListener = null;
            return 0;
        }
    }

    private class BiometricPromptServiceListenerImpl extends BiometricServiceBase.BiometricServiceListener {
        BiometricPromptServiceListenerImpl(IBiometricServiceReceiverInternal wrapperReceiver) {
            super(wrapperReceiver);
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onAcquired(acquiredInfo, FingerprintManager.getAcquiredString(FingerprintService.this.getContext(), acquiredInfo, vendorCode));
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onError(long deviceId, int error, int vendorCode, int cookie) throws RemoteException {
            if (getWrapperReceiver() != null) {
                getWrapperReceiver().onError(cookie, error, FingerprintManager.getErrorString(FingerprintService.this.getContext(), error, vendorCode));
            }
        }
    }

    private class ServiceListenerImpl implements BiometricServiceBase.ServiceListener {
        private IFingerprintServiceReceiver mFingerprintServiceReceiver;

        public ServiceListenerImpl(IFingerprintServiceReceiver receiver) {
            this.mFingerprintServiceReceiver = receiver;
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onEnrollResult(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                Fingerprint fp = (Fingerprint) identifier;
                iFingerprintServiceReceiver.onEnrollResult(fp.getDeviceId(), fp.getBiometricId(), fp.getGroupId(), remaining);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onAcquired(deviceId, acquiredInfo, vendorCode);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationSucceeded(long deviceId, BiometricAuthenticator.Identifier biometric, int userId) throws RemoteException {
            if (this.mFingerprintServiceReceiver == null) {
                return;
            }
            if (biometric == null || (biometric instanceof Fingerprint)) {
                this.mFingerprintServiceReceiver.onAuthenticationSucceeded(deviceId, (Fingerprint) biometric, userId);
            } else {
                Slog.e("FingerprintService", "onAuthenticationSucceeded received non-fingerprint biometric");
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onAuthenticationFailed(long deviceId) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onAuthenticationFailed(deviceId);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onError(long deviceId, int error, int vendorCode, int cookie) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onError(deviceId, error, vendorCode);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                Fingerprint fp = (Fingerprint) identifier;
                iFingerprintServiceReceiver.onRemoved(fp.getDeviceId(), fp.getBiometricId(), fp.getGroupId(), remaining);
            }
        }

        @Override // com.android.server.biometrics.BiometricServiceBase.ServiceListener
        public void onEnumerated(BiometricAuthenticator.Identifier identifier, int remaining) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                Fingerprint fp = (Fingerprint) identifier;
                iFingerprintServiceReceiver.onEnumerated(fp.getDeviceId(), fp.getBiometricId(), fp.getGroupId(), remaining);
            }
        }

        public void onMonitorEventTriggered(int type, String data) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onMonitorEventTriggered(type, data);
            }
        }

        public void onTouchDown() throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onTouchDown(FingerprintService.this.mHalDeviceId);
            }
        }

        public void onTouchUp() throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onTouchUp(FingerprintService.this.mHalDeviceId);
            }
        }

        public void onImageInfoAcquired(int type, int quality, int matchScore) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onImageInfoAcquired(type, quality, matchScore);
            }
        }

        public void onEngineeringInfoUpdated(EngineeringInfo info) throws RemoteException {
            IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
            if (iFingerprintServiceReceiver != null) {
                iFingerprintServiceReceiver.onEngineeringInfoUpdated(info);
            }
        }
    }

    public FingerprintService(Context context) {
        super(context);
        this.mContext = context;
        this.mHealthMonitor = new HealthMonitor(context, "FingerprintService");
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mServiceThread = new ServiceThread("FingerprintService", -2, true);
        this.mServiceThread.start();
        this.mServiceSubThread = new ServiceThread("FingerprintService.sub", -2, true);
        this.mServiceSubThread.start();
        initHandler();
        initSwitchUpdater();
        this.mLockoutDeadline = 0;
        this.mSensorType = SupportUtil.getSensorType(this.mContext);
        this.mTouchEventMonitorMode = new TouchEventMonitorMode(this.mContext, this.mIUnLocker);
        LogUtil.d("FingerprintService", "mSensorType = " + this.mSensorType);
        this.mDcsStatisticsUtil = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(context);
        this.mSideFingerprintSupport = this.mContext.getPackageManager().hasSystemFeature(SIDE_FINGERPRINT_FEATURE);
        mPressTouchEnable = this.mSideFingerprintSupport;
        this.mOpticalFingerprintSupport = this.mContext.getPackageManager().hasSystemFeature(OPTICAL_FINGERPRINT_FEATURE);
        if (this.mOpticalFingerprintSupport) {
            this.mOpticalFingerprintEventListener = new IOpticalFingerprintEventListener() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass5 */

                @Override // com.android.server.biometrics.fingerprint.optical.IOpticalFingerprintEventListener
                public void onAppSwitch(String packageName) {
                    LogUtil.d("FingerprintService", "onAppSwitch " + packageName);
                    if (FingerprintService.this.isFingerprintClient(packageName)) {
                        FingerprintService.this.updateOpticalFingerprintIcon(0);
                    }
                }
            };
            OpticalFingerprintManager.initOFM(this.mContext, this.mOpticalFingerprintEventListener);
        }
        this.mLCDHighLightFileExist = new File(LCD_HIGHLIGHT_PATH).exists();
        this.mWindowMgr = (WindowManager) this.mContext.getSystemService("window");
        this.mKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        this.mTimedLockoutCleared = new SparseBooleanArray();
        this.mFailedAttempts = new SparseIntArray();
        this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        context.registerReceiver(this.mLockoutReceiver, new IntentFilter(getLockoutResetIntent()), getLockoutBroadcastPermission(), null);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.biometrics.fingerprint.FingerprintService$FingerprintServiceWrapper, android.os.IBinder] */
    @Override // com.android.server.SystemService, com.android.server.biometrics.BiometricServiceBase
    public void onStart() {
        super.onStart();
        publishBinderService("fingerprint", new FingerprintServiceWrapper());
        SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$jy4Z2f2mnuUmDYS9y9uFyAj4ss */

            public final void run() {
                IBiometricsFingerprint unused = FingerprintService.this.getFingerprintDaemon();
            }
        }, "FingerprintService.onStart");
        LogUtil.d("FingerprintService", "onStart");
        int fingerprintdPid = this.mHealthMonitor.getFingerprintdPid();
        if (fingerprintdPid != -1) {
            this.mHealthMonitor.fingerprintdSystemReady(fingerprintdPid);
        }
        SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$XXYiJqNOZeDlGzYLTflHO5YKjVM */

            public final void run() {
                FingerprintService.this.startHidlService();
            }
        }, "FingerprintService.startHidlService");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getTag() {
        return "FingerprintService";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricServiceBase.DaemonWrapper getDaemonWrapper() {
        return this.mDaemonWrapper;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricUtils getBiometricUtils() {
        return FingerprintUtils.getInstance();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public Constants getConstants() {
        return this.mFingerprintConstants;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasReachedEnrollmentLimit(int userId) {
        if (getEnrolledTemplates(userId).size() < getContext().getResources().getInteger(17694813)) {
            return false;
        }
        Slog.w("FingerprintService", "Too many fingerprints registered");
        return true;
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public void serviceDied(long cookie) {
        super.serviceDied(cookie);
        this.mDaemon = null;
        this.mDaemonStub = null;
        this.mFingerDaemonWrapper = null;
        this.mDaemonCallbackQueue.add(new Runnable() {
            /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass6 */

            public void run() {
                LogUtil.d("FingerprintService", "FingerPrintDaemon died");
                FingerprintService.this.mHealthMonitor.notifyFingerprintdDied();
            }
        });
        if (!this.mExHandler.hasMessages(11)) {
            ExHandler exHandler = this.mExHandler;
            exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(11));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void updateActiveGroup(int userId, String clientPackage) {
        File baseDir;
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon != null) {
            try {
                int userId2 = getUserOrWorkProfileId(clientPackage, userId);
                if (userId2 != this.mCurrentUserId) {
                    int currentSdkInt = Build.VERSION.SDK_INT;
                    if (currentSdkInt < 1) {
                        Slog.e("FingerprintService", "Current SDK version " + currentSdkInt + " is invalid; must be at least VERSION_CODES.BASE");
                    }
                    if (currentSdkInt <= 27) {
                        baseDir = Environment.getUserSystemDirectory(userId2);
                    } else {
                        baseDir = Environment.getDataVendorDeDirectory(userId2);
                    }
                    File fpDir = new File(baseDir, FP_DATA_DIR);
                    if (!fpDir.exists()) {
                        if (!fpDir.mkdir()) {
                            Slog.v("FingerprintService", "Cannot make directory: " + fpDir.getAbsolutePath());
                            return;
                        } else if (!SELinux.restorecon(fpDir)) {
                            Slog.w("FingerprintService", "Restorecons failed. Directory will have wrong label.");
                            return;
                        }
                    }
                    daemon.setActiveGroup(userId2, fpDir.getAbsolutePath());
                    this.mCurrentUserId = userId2;
                }
                long authId = daemon.getAuthenticatorId();
                LogUtil.d("FingerprintService", "[updateActiveGroup] authId = " + authId);
                this.mAuthenticatorIds.put(Integer.valueOf(userId2), Long.valueOf(hasEnrolledBiometrics(userId2) ? authId : 0));
            } catch (RemoteException e) {
                Slog.e("FingerprintService", "Failed to setActiveGroup():", e);
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
        return "android.permission.RESET_FINGERPRINT_LOCKOUT";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasEnrolledBiometrics(int userId) {
        if (userId == 999 || UserHandle.getCallingUserId() == 999) {
            userId = 0;
        } else if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        return getBiometricUtils().getBiometricsForUser(getContext(), userId).size() > 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getManageBiometricPermission() {
        return "android.permission.MANAGE_FINGERPRINT";
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public void cancelErrorSendAndRemoveClient(ClientMonitor client) {
        if (client != null) {
            client.onError(getHalDeviceId(), 5, 0);
            removeClient(client);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void checkUseBiometricPermission() {
        if (getContext().checkCallingPermission("android.permission.USE_FINGERPRINT") != 0) {
            checkPermission("android.permission.USE_BIOMETRIC");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean checkAppOps(int uid, String opPackageName) {
        if (this.mAppOps.noteOp(78, uid, opPackageName) == 0 || this.mAppOps.noteOp(55, uid, opPackageName) == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public List<Fingerprint> getEnrolledTemplates(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        return getBiometricUtils().getBiometricsForUser(getContext(), userId);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void notifyClientActiveCallbacks(boolean isActive) {
        List<IFingerprintClientActiveCallback> callbacks = this.mClientActiveCallbacks;
        for (int i = 0; i < callbacks.size(); i++) {
            try {
                callbacks.get(i).onClientActiveChanged(isActive);
            } catch (RemoteException e) {
                this.mClientActiveCallbacks.remove(callbacks.get(i));
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int statsModality() {
        return 1;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int getLockoutMode() {
        int currentUser = ActivityManager.getCurrentUser();
        int failedAttempts = this.mFailedAttempts.get(currentUser, 0);
        if (failedAttempts >= 20) {
            return 2;
        }
        if (failedAttempts <= 0 || this.mTimedLockoutCleared.get(currentUser, false) || failedAttempts % 5 != 0) {
            return 0;
        }
        return 1;
    }

    /* access modifiers changed from: private */
    public synchronized IBiometricsFingerprint getFingerprintDaemon() {
        if (this.mDaemon == null) {
            Slog.v("FingerprintService", "mDaemon was null, reconnect to fingerprint");
            String session = String.valueOf(SystemClock.uptimeMillis());
            this.mHealthMonitor.start("asInterface", 10000, session);
            if (this.mDaemonStub == null) {
                try {
                    this.mDaemonStub = IBiometricsFingerprint.getService();
                } catch (NoSuchElementException e) {
                } catch (RemoteException e2) {
                    LogUtil.e("FingerprintService", "Failed to get biometric interface", e2);
                }
            }
            this.mHealthMonitor.stop("asInterface", session);
            if (this.mDaemonStub != null) {
                try {
                    if (this.mFingerDaemonWrapper == null) {
                        this.mFingerDaemonWrapper = new FingerprintDaemonWrapper(this.mDaemonStub, this.mHealthMonitor);
                        this.mFingerDaemonWrapper.asBinder().linkToDeath(this, 0);
                        this.mHalDeviceId = this.mFingerDaemonWrapper.setNotify(this.mDaemonCallback);
                    }
                    LogUtil.v("FingerprintService", "Fingerprint HAL id: " + this.mHalDeviceId);
                    if (this.mHalDeviceId != 0) {
                        this.mDaemon = this.mFingerDaemonWrapper;
                        loadAuthenticatorIds();
                        updateActiveGroup(ActivityManager.getCurrentUser(), null);
                        this.mEnrollmentTotalTimes = this.mDaemon.getEnrollmentTotalTimes();
                    } else {
                        Slog.w("FingerprintService", "Failed to open Fingerprint HAL!");
                        MetricsLogger.count(getContext(), "fingerprintd_openhal_error", 1);
                        this.mDaemon = null;
                        this.mDaemonStub = null;
                        this.mFingerDaemonWrapper = null;
                    }
                } catch (RemoteException e3) {
                    LogUtil.e("FingerprintService", "Failed to open fingerprintd HAL", e3);
                    this.mDaemon = null;
                    this.mDaemonStub = null;
                    this.mFingerDaemonWrapper = null;
                } catch (NullPointerException e4) {
                    LogUtil.e("FingerprintService", "Null pointer exception occurred: ", e4);
                }
            } else {
                LogUtil.w("FingerprintService", "fingerprint service not available");
            }
        }
        return this.mDaemon;
    }

    /* access modifiers changed from: private */
    public long startPreEnroll(IBinder token) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "startPreEnroll: no fingerprint HAL!");
            return 0;
        }
        try {
            return daemon.preEnroll();
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "startPreEnroll failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public int startPostEnroll(IBinder token) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "startPostEnroll: no fingerprint HAL!");
            return 0;
        }
        try {
            return daemon.postEnroll();
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "startPostEnroll failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public void resetFailedAttemptsForUser(boolean clearAttemptCounter, int userId) {
        if (getLockoutMode() != 0) {
            String tag = getTag();
            Slog.v(tag, "Reset biometric lockout, clearAttemptCounter=" + clearAttemptCounter);
        }
        if (clearAttemptCounter) {
            this.mFailedAttempts.put(userId, 0);
        }
        this.mTimedLockoutCleared.put(userId, true);
        cancelLockoutResetForUser(userId);
        setLockoutAttemptDeadline(0);
        notifyLockoutResetMonitors();
    }

    private void cancelLockoutResetForUser(int userId) {
        this.mAlarmManager.cancel(getLockoutResetIntentForUser(userId));
    }

    /* access modifiers changed from: private */
    public void scheduleLockoutResetForUser(int userId) {
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + 30000, getLockoutResetIntentForUser(userId));
    }

    private PendingIntent getLockoutResetIntentForUser(int userId) {
        return PendingIntent.getBroadcast(getContext(), userId, new Intent(getLockoutResetIntent()).putExtra(KEY_LOCKOUT_RESET_USER, userId), 134217728);
    }

    /* access modifiers changed from: private */
    public void dumpInternal(PrintWriter pw) {
        JSONObject dump = new JSONObject();
        try {
            dump.put(IColorAppStartupManager.TYPE_SERVICE, "Fingerprint Manager");
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
            Slog.e("FingerprintService", "dump formatting failure", e);
        }
        pw.println(dump);
        pw.println("HAL Deaths: " + this.mHALDeathCount);
        this.mHALDeathCount = 0;
    }

    /* access modifiers changed from: private */
    public void dumpProto(FileDescriptor fd) {
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
                proto.write(1120986464261L, normal.permanentLockout);
                proto.end(countsToken);
            }
            BiometricServiceBase.PerformanceStats crypto = (BiometricServiceBase.PerformanceStats) this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
            if (crypto != null) {
                long countsToken2 = proto.start(1146756268036L);
                proto.write(1120986464257L, crypto.accept);
                proto.write(1120986464258L, crypto.reject);
                proto.write(1120986464259L, crypto.acquire);
                proto.write(1120986464260L, crypto.lockout);
                proto.write(1120986464261L, crypto.permanentLockout);
                proto.end(countsToken2);
            }
            proto.end(userToken);
        }
        proto.flush();
        this.mPerformanceMap.clear();
        this.mCryptoPerformanceMap.clear();
    }

    /* access modifiers changed from: private */
    public boolean isFingerprintClient(String packageName) {
        LogUtil.d("FingerprintService", "isFingerprintClient " + packageName);
        if (packageName == null || !packageName.equals(this.mCurrentClientPackageName)) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public void updateOpticalFingerprintIcon(int status) {
        updateOpticalFingerprintIcon(status, true);
    }

    public void updateOpticalFingerprintIcon(int status, boolean iconStatusShouldChange) {
        if (!this.mOpticalFingerprintSupport) {
            LogUtil.d("FingerprintService", "[updateOpticalFingerprintIcon] Sensor is not optical ");
            return;
        }
        try {
            if (this.mOpticalFingerprintListener != null) {
                this.mOpticalFingerprintListener.onOpticalFingerprintUpdate(status);
                if (iconStatusShouldChange) {
                    mCurrentIconStatus = status;
                    LogUtil.d("FingerprintService", "[updateOpticalFingerprintIcon] mCurrentIconStatus = " + mCurrentIconStatus);
                }
                LogUtil.d("FingerprintService", "[updateOpticalFingerprintIcon] optical fingerprint state is " + status);
                if (!(status == 1 || status == 4)) {
                    if (status != 2) {
                        if (status == 0) {
                            this.mOpticalFingerprintIsShowing = false;
                        }
                    }
                }
                this.mOpticalFingerprintIsShowing = true;
            } else {
                LogUtil.e("FingerprintService", "mOpticalFingerprintListener is null");
            }
        } catch (RemoteException e) {
            LogUtil.e("FingerprintService", "onOpticalFingerprintUpdate failed", e);
        }
        if (status != 0) {
            return;
        }
        if (this.mLCDHighLightFileExist) {
            writeLcdHighLightPath(0);
        } else {
            notifyDispFingerLayer(1);
        }
    }

    @Override // com.android.server.biometrics.BiometricServiceBase
    public int getCurrentIconStatus() {
        LogUtil.d("FingerprintService", "[getCurrentIconStatus] mCurrentIconStatus = " + mCurrentIconStatus);
        return mCurrentIconStatus;
    }

    /* access modifiers changed from: private */
    public void writeLcdHighLightPath(final int value) {
        if (this.mOpticalFingerprintSupport) {
            this.mHandlerSub.post(new Runnable() {
                /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass7 */

                public void run() {
                    LogUtil.d("FingerprintService", "writeLcdHighLightPath " + value);
                    BufferedWriter bufWriter = null;
                    try {
                        BufferedWriter bufWriter2 = new BufferedWriter(new FileWriter(FingerprintService.LCD_HIGHLIGHT_PATH));
                        bufWriter2.write("" + value);
                        if (value == 1) {
                            boolean unused = FingerprintService.this.mOpticalFingerprintIsHighLight = true;
                        } else {
                            boolean unused2 = FingerprintService.this.mOpticalFingerprintIsHighLight = false;
                        }
                        try {
                            bufWriter2.close();
                        } catch (IOException e) {
                            LogUtil.e("FingerprintService", "can't close the /sys/kernel/oppo_display/notify_fppress");
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        LogUtil.e("FingerprintService", "can't write the /sys/kernel/oppo_display/notify_fppress");
                        if (bufWriter != null) {
                            bufWriter.close();
                        }
                    } catch (Throwable th) {
                        if (bufWriter != null) {
                            try {
                                bufWriter.close();
                            } catch (IOException e3) {
                                LogUtil.e("FingerprintService", "can't close the /sys/kernel/oppo_display/notify_fppress");
                            }
                        }
                        throw th;
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public int notifyDispFingerLayer(int value) {
        int result;
        if (!this.mOpticalFingerprintSupport) {
            LogUtil.d("FingerprintService", "[notifyDispFingerLayer] Sensor is not optical ");
            return -1;
        }
        LogUtil.d("FingerprintService", "notifyDispFingerLayer " + value);
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                synchronized (flinger) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInterfaceToken("android.ui.ISurfaceComposer");
                    data.writeInt(value);
                    flinger.transact(20004, data, reply, 0);
                    result = reply.readInt();
                    data.recycle();
                    reply.recycle();
                    if (value == 0) {
                        this.mOpticalFingerprintIsHighLight = true;
                    } else {
                        this.mOpticalFingerprintIsHighLight = false;
                    }
                }
                return result;
            }
        } catch (RemoteException ex) {
            LogUtil.e("FingerprintService", "notifyDispFingerLayer exception " + ex);
        }
        return -1;
    }

    public void updateLcdHightLight(int isOpen) {
        if (isOpen == 0) {
            if (this.mLCDHighLightFileExist) {
                writeLcdHighLightPath(0);
            } else {
                notifyDispFingerLayer(1);
            }
        } else if (this.mLCDHighLightFileExist) {
            writeLcdHighLightPath(1);
        } else {
            notifyDispFingerLayer(0);
        }
    }

    public IFingerprintPay getAliPayService() {
        IFingerprintPay fingerprintPay;
        try {
            fingerprintPay = IFingerprintPay.getService();
            fingerprintPay.asBinder().linkToDeath(this.mAliPayServiceDeathRecipient, 0);
        } catch (RemoteException e) {
            LogUtil.e("FingerprintService", "Failed to open fingerprintAlipayService HAL", e);
            fingerprintPay = null;
        }
        if (fingerprintPay == null) {
            LogUtil.e("FingerprintService", "alipayService = null, Failed to fingerprintAlipayService HAL");
        }
        return fingerprintPay;
    }

    @Override // com.android.server.biometrics.fingerprint.FingerprintSwitchHelper.ISwitchUpdateListener
    public void onFingerprintSwitchUpdate() {
        if (!this.mExHandler.hasMessages(13)) {
            ExHandler exHandler = this.mExHandler;
            exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(13));
        }
    }

    private void initSwitchUpdater() {
        this.mFingerprintSwitchHelper = new FingerprintSwitchHelper(this.mContext, this);
        if (!this.mExHandler.hasMessages(12)) {
            ExHandler exHandler = this.mExHandler;
            exHandler.sendMessageAtFrontOfQueue(exHandler.obtainMessage(12));
        }
    }

    /* access modifiers changed from: private */
    public void handleSwitchInit() {
        this.mFingerprintSwitchHelper.initConfig();
    }

    /* access modifiers changed from: private */
    public void handleSwitchUpdate() {
        boolean psensorSwitch = this.mFingerprintSwitchHelper.getPsensorSwitch();
        boolean tpProtectSwitch = this.mFingerprintSwitchHelper.getTPProtectSwitch();
        String str = TemperatureProvider.SWITCH_ON;
        SystemProperties.set(FingerprintSwitchHelper.PROP_NAME_PSENSOR_SWITCH, psensorSwitch ? str : TemperatureProvider.SWITCH_OFF);
        if (!tpProtectSwitch) {
            str = TemperatureProvider.SWITCH_OFF;
        }
        SystemProperties.set(FingerprintSwitchHelper.PROP_NAME_TP_PROTECT_SWITCH, str);
        LogUtil.d("FingerprintService", "onFingerprintSwitchUpdate, psensorSwitch = " + psensorSwitch + ", tpProtectSwitch = " + tpProtectSwitch);
        TouchEventMonitorMode touchEventMonitorMode = this.mTouchEventMonitorMode;
        if (touchEventMonitorMode != null) {
            touchEventMonitorMode.onFingerprintSwitchUpdate();
        }
    }

    /* access modifiers changed from: private */
    public long getLockoutAttemptDeadline() {
        long now = SystemClock.elapsedRealtime();
        long j = this.mLockoutDeadline;
        if (j == 0) {
            return 0;
        }
        if (j == 0 || j >= now) {
            long j2 = this.mLockoutDeadline;
            if (j2 <= 30000 + now) {
                return j2;
            }
            setLockoutAttemptDeadline(0);
            return 0;
        }
        setLockoutAttemptDeadline(0);
        return 0;
    }

    /* access modifiers changed from: private */
    public void setLockoutAttemptDeadline(long deadline) {
        this.mLockoutDeadline = deadline;
        LogUtil.w("FingerprintService", "setLockoutAttemptDeadline, mLockoutDeadline = " + this.mLockoutDeadline);
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

    /* access modifiers changed from: package-private */
    public String codeToString(int operationCode) {
        if (operationCode == 272) {
            return "OPERATION_TOUCH_EVENT";
        }
        switch (operationCode) {
            case 256:
                return "OPERATION_AUTHENTICATE";
            case 257:
                return "OPERATION_CANCEL_AUTHENTICATION";
            case OPERATION_ENROLL /*{ENCODED_INT: 258}*/:
                return "OPERATION_ENROLL";
            case OPERATION_CANCEL_ENROLLMENT /*{ENCODED_INT: 259}*/:
                return "OPERATION_CANCEL_ENROLLMENT";
            case 260:
                return "OPERATION_REMOVE_FP";
            case OPERATION_RENAME_FP /*{ENCODED_INT: 261}*/:
                return "OPERATION_RENAME_FP";
            case OPERATION_PRE_ENROLL /*{ENCODED_INT: 262}*/:
                return "OPERATION_PRE_ENROLL";
            case OPERATION_POST_ENROLL /*{ENCODED_INT: 263}*/:
                return "OPERATION_POST_ENROLL";
            case OPERATION_PAUSE_ENROLL /*{ENCODED_INT: 264}*/:
                return "OPERATION_PAUSE_ENROLL";
            case OPERATION_CONTINUE_ENROLL /*{ENCODED_INT: 265}*/:
                return "OPERATION_CONTINUE_ENROLL";
            default:
                switch (operationCode) {
                    case OPERATION_PAUSE_IDENTIFY /*{ENCODED_INT: 274}*/:
                        return "OPERATION_PAUSE_IDENTIFY";
                    case OPERATION_CONTINUE_IDENTIFY /*{ENCODED_INT: 275}*/:
                        return "OPERATION_CONTINUE_IDENTIFY";
                    case OPERATION_CANCEL_TOUCH_EVENT /*{ENCODED_INT: 276}*/:
                        return "OPERATION_CANCEL_TOUCH_EVENT";
                    default:
                        return Integer.toString(operationCode);
                }
        }
    }

    /* access modifiers changed from: protected */
    public void dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        boolean on = false;
        pw.println("dynamicallyConfigLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]: " + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        int i = 1;
        String tag = args[1];
        if ("all".equals(tag)) {
            if ("0".equals(args[2])) {
                on = false;
            } else if ("1".equals(args[2])) {
                on = true;
            }
            FINGER_DEBUG = on;
            LogUtil.dynamicallyConfigLog(on);
            pw.println("dynamicallyConfigLogTag, tag: " + tag + ", on: " + on);
            IBiometricsFingerprint daemon = getFingerprintDaemon();
            if (daemon != null) {
                try {
                    if ("2".equals(args[2])) {
                        if (!SecrecyServiceHelper.getAdbDecryptStatus(2)) {
                            daemon.dynamicallyConfigLog(2);
                            pw.println("daemon.dynamicallyConfigLog 2");
                            return;
                        }
                        pw.println("error : EncryptApp");
                    }
                    if (!on) {
                        i = 0;
                    }
                    daemon.dynamicallyConfigLog(i);
                    pw.println("daemon.dynamicallyConfigLog on: " + on);
                } catch (RemoteException e) {
                    LogUtil.e("FingerprintService", "dynamicallyConfigLog failed", e);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void enumerateInternal(EnumerateClient client) {
        LogUtil.d("FingerprintService", "enumerateInternal not execute becase fingerprint no need");
    }

    /* access modifiers changed from: protected */
    public void handleSyncTemplates(long deviceId, int[] fingerIds, int groupId) {
        LogUtil.d("FingerprintService", "handleSyncTemplates");
        syncTemplates(fingerIds, groupId);
    }

    private void syncTemplates(int[] fingerIds, int groupId) {
        if (fingerIds == null) {
            LogUtil.w("FingerprintService", "fingerIds and groupIds are null");
            return;
        }
        int remainingTemplates = fingerIds.length;
        if (groupId != getCurrentUserId()) {
            LogUtil.w("FingerprintService", "template is not beylong to the current user");
            return;
        }
        int fingerprintNum = getEnrolledFingerprintNum(groupId);
        LogUtil.d("FingerprintService", "syncTemplates started, remainingTemplates = " + remainingTemplates + ", fingerprintNum = " + fingerprintNum + ", groupId = " + groupId);
        for (int i = 0; i < remainingTemplates; i++) {
            LogUtil.d("FingerprintService", "fingerIds[" + i + "] = " + fingerIds[i]);
        }
        if (remainingTemplates != fingerprintNum) {
            this.mFingerprintUtils.syncFingerprintIdForUser(this.mContext, fingerIds, groupId);
        } else {
            LogUtil.d("FingerprintService", "templates are synchronized, do nothing");
        }
    }

    public int getEnrolledFingerprintNum(int userId) {
        return this.mFingerprintUtils.getBiometricsForUser(this.mContext, userId).size();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int getEffectiveUserId(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um != null) {
            long callingIdentity = Binder.clearCallingIdentity();
            int userId2 = um.getCredentialOwnerProfile(userId);
            Binder.restoreCallingIdentity(callingIdentity);
            return userId2;
        }
        LogUtil.e("FingerprintService", "Unable to acquire UserManager");
        return userId;
    }

    /* access modifiers changed from: package-private */
    public int getEffectiveUserIdRestricted(int userId, String opPackageName) {
        FingerprintUtils fingerprintUtils = this.mFingerprintUtils;
        if (!FingerprintUtils.isMultiApp(userId, opPackageName)) {
            return userId;
        }
        LogUtil.d("FingerprintService", "getEffectiveUserId for " + opPackageName + " when it is MultiApp");
        return getEffectiveUserId(userId);
    }

    /* access modifiers changed from: protected */
    public int getCurrentUserId() {
        try {
            return getEffectiveUserId(ActivityManagerNative.getDefault().getCurrentUser().id);
        } catch (RemoteException e) {
            LogUtil.w("FingerprintService", "Failed to get current user id\n");
            return -10000;
        }
    }

    private void calculateTime(String mode, long interval) {
    }

    /* access modifiers changed from: private */
    public void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        String opt;
        if (args.length != 2 || !"<fingerprint daemon started>".equals(args[0])) {
            int opti = 0;
            while (opti < args.length && (opt = args[opti]) != null && opt.length() > 0 && opt.charAt(0) == '-') {
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
                int opti2 = opti + 1;
                if ("log".equals(cmd) || "l".equals(cmd)) {
                    dynamicallyConfigLogTag(pw, args);
                    return;
                } else if ("debug_switch".equals(cmd)) {
                    pw.println("  all=true");
                    return;
                } else if ("reset".equals(cmd)) {
                    int pid = this.mHealthMonitor.getFingerprintdPid();
                    if (pid != -1) {
                        Process.sendSignal(pid, 3);
                        return;
                    }
                    return;
                }
            }
            pw.println("DEBUG = true");
            pw.println("LogLevel : " + LogUtil.getLevelString());
            pw.println("mSensorType = " + this.mSensorType);
            pw.println("mHalDeviceId = " + this.mHalDeviceId);
            pw.println("mFingerprintEnabled = " + this.mFingerprintEnabled);
            pw.println("mFingerprintUnlockEnabled = " + this.mFingerprintUnlockEnabled);
            pw.println("screenState = " + this.mPowerManager.getScreenState());
            pw.println("mFingerprintSwitchHelper = " + this.mFingerprintSwitchHelper.dumpToString());
            pw.println("mFingerprintUtils dump");
            JSONObject jsonObj = new JSONObject();
            try {
                JSONArray sets = new JSONArray();
                for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                    int userId = user.getUserHandle().getIdentifier();
                    int N = this.mFingerprintUtils.getBiometricsForUser(this.mContext, userId).size();
                    JSONObject set = new JSONObject();
                    set.put("id", userId);
                    set.put("count", N);
                    sets.put(set);
                }
                jsonObj.put("prints", sets);
            } catch (JSONException e) {
                LogUtil.e("FingerprintService", "jsonObj formatting failure", e);
            }
            pw.println(jsonObj);
            pw.println("mHealthMonitor dump");
            this.mHealthMonitor.dump(fd, pw, args, "  ");
            this.mUnlockController.dump(fd, pw, args);
            return;
        }
        LogUtil.d("FingerprintService", "received fingerprintd started msg, pid = " + args[1]);
        this.mExHandler.post(new Runnable() {
            /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass12 */

            public void run() {
                if (FingerprintService.this.getFingerprintDaemon() == null) {
                    LogUtil.e("FingerprintService", "dumpInner: no fingerprintd!");
                }
            }
        });
        this.mHealthMonitor.fingerprintdSystemReady(Integer.parseInt(args[1]));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void handleAcquired(long deviceId, int acquiredInfo, int vendorCode) {
        LogUtil.d("FingerprintService", "handleAcquired acquiredInfo = " + acquiredInfo);
        long startTime = SystemClock.uptimeMillis();
        ClientMonitor client = getCurrentClient();
        if (client != null && (client instanceof AuthenticationClient)) {
            if (this.mUnlockController != null && isKeyguard(client.getOwnerString())) {
                this.mUnlockController.dispatchAcquired(acquiredInfo);
            }
            DcsFingerprintStatisticsUtil dcsFingerprintStatisticsUtil = this.mDcsStatisticsUtil;
            if (dcsFingerprintStatisticsUtil != null) {
                dcsFingerprintStatisticsUtil.sendAcquiredInfo(acquiredInfo, client.getOwnerString());
            }
        }
        if (client != null && (client instanceof FingerprintEnrollClient)) {
            FingerprintEnrollClient fingerprintEnrollClient = (FingerprintEnrollClient) client;
            FingerprintEnrollClient.mIsAbleToUpdate = acquiredInfo != 1002;
        }
        super.handleAcquired(deviceId, acquiredInfo, vendorCode);
        calculateTime("handleAcquired", SystemClock.uptimeMillis() - startTime);
    }

    /* access modifiers changed from: protected */
    public byte[] getFingerprintAuthToken(String opPackageName) {
        LogUtil.d("FingerprintService", "getFingerprintAuthToken  opPackageName:" + opPackageName);
        if (!canUseBiometric(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
            return null;
        }
        return this.mFingerprintAuthToken;
    }

    /* access modifiers changed from: protected */
    public void handleAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> tokenByte) {
        LogUtil.d("FingerprintService", "handleAuthenticated (deviceId =" + deviceId + " fpId = " + fingerId + ", groupId  = " + groupId + ")");
        if (fingerId != 0) {
            byte[] byteToken = new byte[tokenByte.size()];
            for (int i = 0; i < tokenByte.size(); i++) {
                byteToken[i] = tokenByte.get(i).byteValue();
            }
            this.mFingerprintAuthToken = byteToken;
        }
        ClientMonitor client = getCurrentClient();
        Fingerprint fp = new Fingerprint("", groupId, fingerId, deviceId);
        if (client != null && (client instanceof AuthenticationClient)) {
            DcsFingerprintStatisticsUtil dcsFingerprintStatisticsUtil = this.mDcsStatisticsUtil;
            if (dcsFingerprintStatisticsUtil != null) {
                dcsFingerprintStatisticsUtil.sendFingerId(fingerId, client.getOwnerString());
            }
            long startTime = SystemClock.uptimeMillis();
            if (fingerId != 0 && SupportUtil.FRONT_PRESS_SENSOR.equals(this.mSensorType)) {
                this.mUnlockController.dispatchAuthForDropHomeKey();
            }
            if (this.mUnlockController != null && !isKeyguard(client.getOwnerString()) && this.mSideFingerprintSupport && (this.mUnlockController instanceof BackTouchSensorUnlockController) && fingerId != 0 && !this.mHandlerSub.hasMessages(16)) {
                ExHandler exHandler = this.mHandlerSub;
                exHandler.sendMessageDelayed(exHandler.obtainMessage(16), 500);
            }
            if (this.mUnlockController == null || !isKeyguard(client.getOwnerString())) {
                LogUtil.d("FingerprintService", "handleAuthenticated for other application");
            } else {
                LogUtil.d("FingerprintService", "handleAuthenticated for keyguard, fingerId = " + fingerId);
                if (this.mSideFingerprintSupport && (this.mUnlockController instanceof BackTouchSensorUnlockController)) {
                    mPressTouchFp = null;
                    mPressTouchTokenByte = null;
                    if (fingerId == 0) {
                        LogUtil.d("FingerprintService", "mSideFingerprintSupport wait a while for confirm failed touching");
                        try {
                            Thread.currentThread();
                            Thread.sleep(200);
                        } catch (Exception e) {
                        }
                        if (!this.mTouching) {
                            return;
                        }
                    } else if (!BackTouchSensorUnlockController.mIsScreenOff && BackTouchSensorUnlockController.mPressTouchReason.equals("gotosleep")) {
                        LogUtil.d("FingerprintService", "mSideFingerprintSupport wait for confirm successed touching finished , fp = " + fp);
                        mPressTouchFp = fp;
                        mPressTouchTokenByte = tokenByte;
                        return;
                    }
                }
                if (!mPressTouchEnable || !(this.mUnlockController instanceof BackTouchSensorUnlockController)) {
                    this.mUnlockController.dispatchAuthenticated(fp, tokenByte);
                } else {
                    mPressTouchAuthenticated = true;
                    if (BackTouchSensorUnlockController.mInWakeupByPower || !BackTouchSensorUnlockController.mIsScreenOff) {
                        this.mUnlockController.dispatchAuthenticated(fp, tokenByte);
                    } else {
                        mPressTouchFp = fp;
                        mPressTouchTokenByte = tokenByte;
                    }
                }
                calculateTime("mUnlockController.dispatchAuthenticated", SystemClock.uptimeMillis() - startTime);
                return;
            }
        }
        super.handleAuthenticated(fp, tokenByte);
    }

    public void sendKeyGuardAuthenticated(Fingerprint fingerInfo, ArrayList<Byte> token, boolean screenOn) {
        Trace.traceBegin(4, "FingerprintAuthenticated");
        ClientMonitor client = getCurrentClient();
        if (client != null && (client instanceof AuthenticationClient)) {
            LogUtil.d("FingerprintService", "sendKeyGuardAuthenticated");
            int fingerId = fingerInfo.getBiometricId();
            if (screenOn && fingerId != 0) {
                this.mIUnLocker.sendUnlockTime(OppoUsageService.IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_RADIO, SystemClock.uptimeMillis());
            }
            handleAuthenticated(fingerInfo, token);
            if (!screenOn && fingerId == 0) {
                getFPMS().gotoSleepDelay(2000);
            }
        }
        Trace.traceEnd(4);
    }

    /* access modifiers changed from: protected */
    public void dispatchTouchDown(long deviceId) {
        LogUtil.d("FingerprintService", "dispatchTouchDown");
        long startTime = SystemClock.uptimeMillis();
        ClientMonitor client = getCurrentClient();
        if (client != null && (client instanceof AuthenticationClient) && isKeyguard(client.getOwnerString())) {
            Trace.traceBegin(4, "FingerprintTouchDown");
            getFPMS().openAllFramesDrawForKeyguard();
            this.mUnlockController.dispatchTouchDown();
            Trace.traceEnd(4);
            ((FingerprintAuthClient) client).sendTouchDownEvent();
        } else if (client == null || !(client instanceof BiometricServiceBase.EnrollClientImpl)) {
            if (client != null && (client instanceof TouchEventClient)) {
                this.mTouchEventMonitorMode.dispatchTouchDown();
            }
            calculateTime("mUnlockController.dispatchTouchDown", SystemClock.uptimeMillis() - startTime);
        } else {
            this.mUnlockController.userActivity();
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchTouchUp(long deviceId) {
        LogUtil.d("FingerprintService", "dispatchTouchUp");
        long startTime = SystemClock.uptimeMillis();
        ClientMonitor client = getCurrentClient();
        if (client != null && (client instanceof AuthenticationClient) && isKeyguard(client.getOwnerString())) {
            this.mUnlockController.dispatchTouchUp();
            ((FingerprintAuthClient) client).sendTouchUpEvent();
            if (this.mSideFingerprintSupport && (this.mUnlockController instanceof BackTouchSensorUnlockController)) {
                LogUtil.d("FingerprintService", "mPressTouchFp = " + mPressTouchFp);
                if (mPressTouchFp != null) {
                    LogUtil.d("FingerprintService", "mPressTouchFp.getBiometricId = " + mPressTouchFp.getBiometricId());
                }
                Fingerprint fingerprint = mPressTouchFp;
                if (!(fingerprint == null || fingerprint.getBiometricId() == 0)) {
                    handleError(this.mHalDeviceId, 16, 0);
                }
                mPressTouchAuthenticated = false;
                mPressTouchFp = null;
                mPressTouchTokenByte = null;
            }
        } else if (client == null || !(client instanceof BiometricServiceBase.EnrollClientImpl)) {
            if (client != null && (client instanceof TouchEventClient)) {
                this.mTouchEventMonitorMode.dispatchTouchUp();
            }
            calculateTime("dispatchTouchUp", SystemClock.uptimeMillis() - startTime);
        } else {
            LogUtil.d("FingerprintService", "send touchup event to enroll client");
            this.mUnlockController.userActivity();
            ((FingerprintEnrollClient) client).sendTouchUpEvent();
        }
    }

    /* access modifiers changed from: protected */
    public void handleImageInfoAcquired(int type, int quality, int matchScore) {
        LogUtil.d("FingerprintService", "handleImageInfoAcquired, type = " + type + ", quality = " + quality + ", matchScore = " + matchScore);
        ClientMonitor client = getCurrentClient();
        if (client != null && (client instanceof AuthenticationClient)) {
            DcsFingerprintStatisticsUtil dcsFingerprintStatisticsUtil = this.mDcsStatisticsUtil;
            if (dcsFingerprintStatisticsUtil != null) {
                dcsFingerprintStatisticsUtil.sendImageInfo(type, quality, matchScore);
            }
            if (!(this.mUnlockController == null || !isKeyguard(client.getOwnerString()) || type == 0)) {
                this.mUnlockController.dispatchImageDirtyAuthenticated();
            }
            if (((FingerprintAuthClient) client).isInLockOutWhiteList() && ((FingerprintAuthClient) client).sendImageInfo(type, quality, matchScore)) {
                client.stop(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void startSetFingerprintEnabled(boolean enabled) {
        this.mFingerprintEnabled = enabled;
        LogUtil.d("FingerprintService", "startSetFingerprintEnabled( " + enabled + " )");
    }

    /* access modifiers changed from: private */
    public int startGetAlikeyStatus() {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            return -1;
        }
        try {
            LogUtil.w("FingerprintService", "startGetAlikeyStatus");
            return daemon.getAlikeyStatus();
        } catch (RemoteException e) {
            LogUtil.e("FingerprintService", "startGetAlikeyStatus failed", e);
            return -1;
        }
    }

    protected abstract class FingerprintEnrollClient extends BiometricServiceBase.EnrollClientImpl {
        public FingerprintEnrollClient(Context context, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int userId, int groupId, byte[] cryptoToken, boolean restricted, String owner, int[] disabledFeatures) {
            super(context, daemon, halDeviceId, token, listener, userId, groupId, cryptoToken, restricted, owner, disabledFeatures);
        }

        public boolean sendTouchDownEvent() {
            try {
                if (getListener() == null) {
                    return true;
                }
                ((ServiceListenerImpl) getListener()).onTouchDown();
                return true;
            } catch (RemoteException e) {
                LogUtil.w(LoggableMonitor.TAG, "Failed to notify onTouchDown:", e);
                return true;
            }
        }

        public boolean sendTouchUpEvent() {
            try {
                if (getListener() == null) {
                    return true;
                }
                ((ServiceListenerImpl) getListener()).onTouchUp();
                return true;
            } catch (RemoteException e) {
                LogUtil.w(LoggableMonitor.TAG, "Failed to notify onTouchUp:", e);
                return true;
            }
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public void updateOpticalFingerIcon(int status) {
            FingerprintService.this.updateOpticalFingerprintIcon(status);
        }

        @Override // com.android.server.biometrics.ClientMonitor
        public void updateLcdHightLight(int values) {
            FingerprintService.this.updateLcdHightLight(values);
        }
    }

    private FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }

    /* access modifiers changed from: private */
    public int getFailedAttempts() {
        return this.mFailedAttempts.get(ActivityManager.getCurrentUser(), 0);
    }

    /* access modifiers changed from: private */
    public int startGetEnrollmentTotalTimes(IBinder token) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w("FingerprintService", "startGetEnrollmentTotalTimes: no fingerprintd!");
            return 0;
        }
        try {
            LogUtil.d("FingerprintService", "startGetEnrollmentTotalTimes");
            this.mEnrollmentTotalTimes = daemon.getEnrollmentTotalTimes();
            return this.mEnrollmentTotalTimes;
        } catch (RemoteException e) {
            LogUtil.e("FingerprintService", "startGetEnrollmentTotalTimes failed", e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public int startPauseEnroll() {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w("FingerprintService", "startPauseEnroll: no fingerprintd!");
            return -1;
        }
        updateOpticalFingerprintIcon(0);
        try {
            LogUtil.d("FingerprintService", "startPauseEnroll");
            return daemon.pauseEnroll();
        } catch (RemoteException e) {
            LogUtil.e("FingerprintService", "pauseEnroll failed", e);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public int startContinueEnroll() {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w("FingerprintService", "startContinueEnroll: no fingerprintd!");
            return -1;
        }
        ClientMonitor client = getCurrentClient();
        if (client != null && (client instanceof FingerprintEnrollClient)) {
            FingerprintEnrollClient fingerprintEnrollClient = (FingerprintEnrollClient) client;
            FingerprintEnrollClient.mIsAbleToUpdate = true;
        }
        updateOpticalFingerprintIcon(4);
        try {
            LogUtil.d("FingerprintService", "startContinueEnroll");
            return daemon.continueEnroll();
        } catch (RemoteException e) {
            LogUtil.e("FingerprintService", "continueEnroll failed", e);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public int startPauseIdentify() {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w("FingerprintService", "startPauseIdentify: no fingerprintd!");
            return -1;
        }
        try {
            LogUtil.d("FingerprintService", "startPauseIdentify");
            return daemon.pauseIdentify();
        } catch (RemoteException e) {
            LogUtil.e("FingerprintService", "pauseIdentify failed", e);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public int startContinueIdentify() {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w("FingerprintService", "startContinueIdentify: no fingerprintd!");
            return -1;
        }
        try {
            LogUtil.d("FingerprintService", "startContinueIdentify");
            return daemon.continueIdentify();
        } catch (RemoteException e) {
            LogUtil.e("FingerprintService", "continueIdentify failed", e);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public void setScreenStateInternal(int state) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w("FingerprintService", "setScreenStateInternal: no fingerprintd!");
            return;
        }
        try {
            LogUtil.d("FingerprintService", "setScreenState ( " + state + " )");
            daemon.setScreenState(state);
        } catch (RemoteException e) {
            LogUtil.e("FingerprintService", "setScreenState failed", e);
        }
    }

    /* access modifiers changed from: private */
    public void startSetFingerKeymode(IBinder token, int groupId, boolean restricted, int enable) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            LogUtil.w("FingerprintService", "startSetFingerKeymode: no fingeprintd!");
            return;
        }
        ClientMonitor client = getCurrentClient();
        if (client == null || !(client instanceof AuthenticationClient) || !isKeyguard(client.getOwnerString())) {
            try {
                if (this.mLastKeymodeEnable != enable) {
                    int result_keymode = daemon.setFingerKeymode(enable);
                    if (result_keymode == 0) {
                        LogUtil.w("FingerprintService", "startSetFingerKeymode enable = " + enable);
                        this.mLastKeymodeEnable = enable;
                        return;
                    }
                    LogUtil.w("FingerprintService", "startSetFingerKeymode failed, origin number is -2, result=" + result_keymode);
                }
            } catch (RemoteException e) {
                LogUtil.e("FingerprintService", "startSetFingerKeymode failed", e);
            }
        } else {
            LogUtil.w("FingerprintService", "AuthenticationClient isKeyguard!");
        }
    }

    /* access modifiers changed from: private */
    public ProximitySensorManager getPsensorManager() {
        return ProximitySensorManager.getProximitySensorManager();
    }

    /* access modifiers changed from: package-private */
    public boolean tryPreOperation(IBinder token, int operation) {
        return tryPreOperation(token, operation, null);
    }

    /* access modifiers changed from: package-private */
    public boolean tryPreOperation(IBinder token, int operation, String opPackageName) {
        ClientMonitor client;
        LogUtil.d("FingerprintService", "try operation " + codeToString(operation));
        boolean isSameToken = false;
        if (token == null || token.isBinderAlive()) {
            if (!isKeyguard(opPackageName) && (client = getCurrentClient()) != null && (client instanceof AuthenticationClient) && this.mKeyguardManager != null) {
                IBinder authtoken = client.getToken();
                if (isKeyguard(client.getOwnerString()) && this.mKeyguardManager.isKeyguardLocked()) {
                    if (token == authtoken) {
                        isSameToken = true;
                    }
                    if (!isSameToken) {
                        LogUtil.d("FingerprintService", "tryPreOperation keyguard auth but otherClient try to access");
                    }
                    return isSameToken;
                }
            }
            return true;
        }
        LogUtil.e("FingerprintService", "Client has died! and token = " + token + " token.isBinderAlive() = " + token.isBinderAlive());
        return false;
    }

    /* access modifiers changed from: protected */
    public ClientMonitor getCurrentEngineerClient() {
        return this.mCurrentEngineerClient;
    }

    /* access modifiers changed from: protected */
    public void dispatchEngineeringInfoUpdated(EngineeringInfo info) {
        ClientMonitor client = getCurrentEngineerClient();
        LogUtil.d("FingerprintService", "dispatchEngineeringInfoUpdated() info = " + info + " client = " + client);
        if (client != null && (client instanceof EngineeringClient)) {
            ((EngineeringClient) client).sendEngineeringInfoUpdated(info);
        }
    }

    /* access modifiers changed from: protected */
    public void touchEventInternal(TouchEventClient client) {
        this.mHandler.post(new Runnable(client) {
            /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$BTaref2viVxh2dlXFmTEtgoUvbM */
            private final /* synthetic */ TouchEventClient f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FingerprintService.this.lambda$touchEventInternal$0$FingerprintService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$touchEventInternal$0$FingerprintService(TouchEventClient client) {
        startClient(client, true);
    }

    /* access modifiers changed from: protected */
    public int engineerInternal(final EngineeringClient client) {
        final PendingResult<Integer> r = new PendingResult<>(-1);
        this.mHandler.post(new Runnable() {
            /* class com.android.server.biometrics.fingerprint.FingerprintService.AnonymousClass13 */

            public void run() {
                r.setResult(Integer.valueOf(FingerprintService.this.startEngineerClient(client)));
            }
        });
        return r.await().intValue();
    }

    public int startEngineerClient(EngineeringClient newClient) {
        if (this.mCurrentEngineerClient != null) {
            String tag = getTag();
            Slog.v(tag, "request stop current client " + this.mCurrentEngineerClient.getOwnerString());
            this.mCurrentEngineerClient.stop(true);
            this.mCurrentEngineerClient = newClient;
            return newClient.start();
        } else if (newClient == null) {
            return -1;
        } else {
            this.mCurrentEngineerClient = newClient;
            return newClient.start();
        }
    }

    /* access modifiers changed from: protected */
    public void cancelTouchEventInternal(IBinder token) {
        this.mHandler.post(new Runnable(token) {
            /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$hFqZFpgPn3LNc58gTxjAT8ibU74 */
            private final /* synthetic */ IBinder f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FingerprintService.this.lambda$cancelTouchEventInternal$1$FingerprintService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$cancelTouchEventInternal$1$FingerprintService(IBinder token) {
        ClientMonitor client = getCurrentClient();
        if ((client instanceof TouchEventClient) && client.getToken() == token) {
            LogUtil.d("FingerprintService", "Cancelling TouchEventClient");
            client.stop(client.getToken() == token);
        }
    }

    /* access modifiers changed from: private */
    public void stopGetEngineeringInfo(IBinder token, boolean initiatedByClient, int type) {
        this.mHandler.post(new Runnable(token) {
            /* class com.android.server.biometrics.fingerprint.$$Lambda$FingerprintService$Z2SiP9VSSRNCDv4JgklNpW9xO3g */
            private final /* synthetic */ IBinder f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FingerprintService.this.lambda$stopGetEngineeringInfo$2$FingerprintService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$stopGetEngineeringInfo$2$FingerprintService(IBinder token) {
        if (this.mCurrentEngineerClient != null) {
            LogUtil.d("FingerprintService", "Cancelling EngineeringClient");
            EngineeringClient engineeringClient = this.mCurrentEngineerClient;
            engineeringClient.stop(engineeringClient.getToken() == token);
            this.mCurrentEngineerClient = null;
            return;
        }
        LogUtil.d("FingerprintService", "mCurrentEngineerClient is null  and Cancelling EngineeringClient fail");
    }

    /* access modifiers changed from: package-private */
    public void notifyOperationCanceled(IFingerprintServiceReceiver receiver) {
        if (receiver != null) {
            try {
                receiver.onError(this.mHalDeviceId, 5, 0);
            } catch (RemoteException e) {
                LogUtil.w("FingerprintService", "Failed to send error to receiver: ", e);
            }
        }
    }

    public void systemReady() {
        LogUtil.d("FingerprintService", "systemReady");
        this.mSystemReady = true;
        if (SupportUtil.FRONT_PRESS_SENSOR.equals(this.mSensorType)) {
            this.mUnlockController = new UnlockController(this.mContext, this.mIUnLocker);
        } else if (SupportUtil.FRONT_TOUCH_SENSOR.equals(this.mSensorType)) {
            this.mUnlockController = new TouchSensorUnlockController(this.mContext, this.mIUnLocker);
        } else {
            this.mUnlockController = new BackTouchSensorUnlockController(this.mContext, this.mIUnLocker);
        }
        FingerprintPowerManager.initFPM(this.mContext, this.mUnlockController);
        ProximitySensorManager.initPsensorManager(this.mContext, this.mIProximitySensorEventListener);
        this.mFingerprintUnlockSettingMonitor = new FingerprintUnlockSettingMonitor(this.mContext, this.mListener, this.mServiceThread.getLooper());
        publishLocalService(FingerprintInternal.class, getFPMS().getFingerprintLocalService());
        this.mUnlockController.notifySystemReady();
        this.mFingerprintSwitchHelper.initUpdateBroadcastReceiver();
    }

    /* access modifiers changed from: private */
    public void startFinishUnLockedScreen(boolean isfinished, String opPackageName) {
        LogUtil.d("FingerprintService", "startFinishUnLockedScreen, isfinished = " + isfinished + ", opPackageName = " + opPackageName);
        ClientMonitor client = getCurrentClient();
        if (client == null || !(client instanceof AuthenticationClient)) {
            this.mTouchEventMonitorMode.notifyScreenon();
        } else {
            this.mUnlockController.onScreenOnUnBlockedByFingerprint(isfinished);
        }
    }

    /* access modifiers changed from: protected */
    public void removeTemplateForUser(int userId, int fingerId) {
        this.mFingerprintUtils.removeBiometricForUser(this.mContext, userId, fingerId);
    }

    /* access modifiers changed from: protected */
    public void dispatchMonitorEventTriggered(int type, String data) {
        LogUtil.d("FingerprintService", "dispatchMonitorEventTriggered, type = " + type + ", data = " + data);
        ClientMonitor client = getCurrentClient();
        if (client != null && (client instanceof AuthenticationClient) && isKeyguard(client.getOwnerString())) {
            ((FingerprintAuthClient) client).sendMonitorEventTriggered(type, data);
        }
        if (client != null && (client instanceof TouchEventClient) && isKeyguard(client.getOwnerString())) {
            ((TouchEventClient) client).sendMonitorEventTriggered(type, data);
        }
    }

    private static Object invokeDeclaredMethod(Object target, String clsName, String methodName, Class[] parameterTypes, Object[] args) {
        LogUtil.i("FingerprintService", target + " invokeDeclaredMethod : " + clsName + "." + methodName);
        try {
            Method method = Class.forName(clsName).getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ClassNotFoundException e) {
            Slog.i("FingerprintService", "ClassNotFoundException : " + e.getMessage());
            return null;
        } catch (NoSuchMethodException e2) {
            Slog.i("FingerprintService", "NoSuchMethodException : " + e2.getMessage());
            return null;
        } catch (IllegalAccessException e3) {
            Slog.i("FingerprintService", "IllegalAccessException : " + e3.getMessage());
            return null;
        } catch (InvocationTargetException e4) {
            Slog.i("FingerprintService", "InvocationTargetException : " + e4.getMessage());
            return null;
        } catch (SecurityException e5) {
            Slog.i("FingerprintService", "SecurityException : " + e5.getMessage());
            return null;
        } catch (Exception e6) {
            Slog.e("FingerprintService", "Exception : " + e6.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1. open all log in FingerprintService");
        pw.println("cmd: dumpsys fingerprint log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean canUseBiometric(String opPackageName, boolean requireForeground, int uid, int pid, int userId) {
        FingerprintSwitchHelper fingerprintSwitchHelper;
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.feature.18381") || (fingerprintSwitchHelper = this.mFingerprintSwitchHelper) == null || fingerprintSwitchHelper.isInOverSeasWhiteList(opPackageName)) {
            return super.canUseBiometric(opPackageName, requireForeground, uid, pid, userId);
        }
        LogUtil.d("FingerprintService", "Forbidden opPackageName:" + opPackageName + " use Fingerprint service because of oversea");
        return false;
    }
}
