package com.android.server.power;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.view.Display;
import com.android.server.LocalServices;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.biometrics.BiometricWakeupManagerService;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.wakeup.BackTouchSensorUnlockController;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.lights.OppoLightsService;
import com.android.server.oppo.OppoUsageService;
import com.android.server.oppo.ScreenOnCpuBoostHelper;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.power.PowerManagerService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

/* access modifiers changed from: package-private */
public class OppoPowerManagerHelper {
    private static final String AOD_LIGHT_BRIGHT = "0";
    private static final String AOD_LIGHT_DARK = "1";
    private static String AOD_LIGHT_MODE_PATH = "/sys/kernel/oppo_display/aod_light_mode_set";
    static boolean DEBUG = false;
    static boolean DEBUG_PANIC = false;
    static boolean DEBUG_SPEW = DEBUG;
    private static final int DUMP_WAKE_LOCKS_LIST_DELAY = 1800000;
    private static final int DURATION_FOR_SENSOR_CALIBRATION = 10000;
    private static final int FLAG_MOTION_GAME_APP_FOREGROUND = 1;
    private static final int FLAG_MOTION_GAME_DISPLAY_READY = 2;
    private static final int FLAG_MOTION_GAME_TOF_CAM_READY = 4;
    private static final int MSG_BIOMETRICS_SET_ALPHA_TIMEOUT = 202;
    private static final int MSG_CHECK_MOTION_GAME_CHARGE_STATE = 111;
    private static final int MSG_CHECK_MOTION_GAME_DISPLAY_STATE = 110;
    private static final int MSG_DUMP_WAKE_LOCKS_LIST = 203;
    private static final int MSG_FIRST_INDEX = 1111;
    private static final int MSG_OPPO_PHONE_HEADSET_HANGUP = 201;
    private static final int MSG_PROCESS_AOD_LIGHT_EVENT = 113;
    private static final int MSG_SCREEN_ON_UNBLOCKED_BY_BIOMETRICS = 6;
    private static final int MSG_SENSOR_CALIBRATION_TIMEOUT = 10;
    private static final int MSG_UPDATE_MOTION_DISPLAY_REFRESHRATE = 112;
    private static final int MSG_WAIT_PROXIMITY_REPORT = 114;
    private static final int SCREEN_BRIGHTNESS_MODE_DEFAULT = -1;
    public static final String TAG = "OppoPowerManagerHelper";
    public static final String UNBLOCK_REASON_GO_TO_SLEEP = "UNBLOCK_REASON_GO_TO_SLEEP";
    private static final int VENDOR_MSG_INDEX = 200;
    private static final int WAIT_PROXIMITY_REPORT_TIMEOUT = 500;
    public static OppoBrightUtils mOppoBrightUtils;
    /* access modifiers changed from: private */
    public static boolean mOppoShutdownIng = false;
    /* access modifiers changed from: private */
    public long MOTION_GAME_SCREEN_OFF_TIME_OUT = 5000;
    private PowerManager.WakeLock calibrateWakelock = null;
    private boolean isMtk = Build.HARDWARE.startsWith("mt");
    private BiometricsManagerInternal mBiometricsManager;
    private String mBiometricsWakeupReason = "";
    private boolean mCalibrateProximitySensorEnabled = false;
    private boolean mCallEventConsumed = true;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.server.power.OppoPowerManagerHelper.AnonymousClass7 */

        public void onDisplayChanged(int displayId) {
            Slog.d(OppoPowerManagerHelper.TAG, "onDisplayChanged:" + displayId);
        }

        public void onDisplayRemoved(int displayId) {
            Slog.d(OppoPowerManagerHelper.TAG, "onDisplayRemoved:" + displayId);
            if (OppoPowerManagerHelper.this.mMotionDisplayId == displayId) {
                synchronized (OppoPowerManagerHelper.this.mLock) {
                    OppoPowerManagerHelper.access$1472(OppoPowerManagerHelper.this, -3);
                    OppoPowerManagerHelper.this.scheduleUpdateMotionGameDisplatStateTask(0);
                    OppoPowerManagerHelper.this.scheduleUpdateMotionGameChargeStateTask(0);
                    int unused = OppoPowerManagerHelper.this.mMotionDisplayId = -1;
                }
            }
        }

        public void onDisplayAdded(int displayId) {
            Slog.d(OppoPowerManagerHelper.TAG, "onDisplayAdded:" + displayId);
            Display display = OppoPowerManagerHelper.this.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                int flags = display.getFlags();
                int type = display.getType();
                Slog.d(OppoPowerManagerHelper.TAG, "Display infor:" + display.toString());
                Slog.d(OppoPowerManagerHelper.TAG, "display FLAGS:" + flags);
                if ((flags & 8) != 0 && type == 2) {
                    synchronized (OppoPowerManagerHelper.this.mLock) {
                        Display unused = OppoPowerManagerHelper.this.mMotionDisplay = display;
                        int unused2 = OppoPowerManagerHelper.this.mMotionDisplayId = displayId;
                        OppoPowerManagerHelper.access$1476(OppoPowerManagerHelper.this, 2);
                        OppoPowerManagerHelper.this.scheduleUpdateMotionGameDisplatStateTask(OppoPowerManagerHelper.this.checkIsMotionGameModeLocked() ? OppoPowerManagerHelper.this.MOTION_GAME_SCREEN_OFF_TIME_OUT : 0);
                        OppoPowerManagerHelper.this.scheduleUpdateMotionGameChargeStateTask(0);
                        OppoPowerManagerHelper.this.scheduleUpdateMotionDisplayRefreshRateTask(0);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public DisplayManager mDisplayManager;
    /* access modifiers changed from: private */
    public DisplayManagerInternal mDisplayManagerInternal;
    private int mDisplayState = -1;
    private Sensor mDozeLightSensor;
    /* access modifiers changed from: private */
    public boolean mDozeLightSensorEnabled = false;
    private final SensorEventListener mDozeLightSensorListener = new SensorEventListener() {
        /* class com.android.server.power.OppoPowerManagerHelper.AnonymousClass3 */

        public void onSensorChanged(SensorEvent event) {
            if (OppoPowerManagerHelper.this.mDozeLightSensorEnabled) {
                long time = SystemClock.uptimeMillis();
                float lux = event.values[0];
                Slog.d(OppoPowerManagerHelper.TAG, "light event lux:" + lux);
                OppoPowerManagerHelper.this.handleLightSensorEvent(time, lux);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public OppoPowerManagerHelperHandler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsPowered;
    private boolean mIsSellModeVersion = false;
    private boolean mLastIsPowered;
    private int mLastScreenBrightness;
    private int mLastScreenBrightnessModeSetting = -1;
    private int mLastWakeLockSummary;
    private LightsManager mLightsManager;
    /* access modifiers changed from: private */
    public final Object mLock;
    /* access modifiers changed from: private */
    public Display mMotionDisplay;
    /* access modifiers changed from: private */
    public int mMotionDisplayId = -1;
    /* access modifiers changed from: private */
    public int mMotionFlags;
    private String mMotionGameAppPackageName = "";
    private int mMotionGameDisplayState = -1;
    private boolean mMotionGameModeEnabled;
    private Light mMotionLcdLight;
    private boolean mOppoButtonReady = true;
    /* access modifiers changed from: private */
    public final PowerManagerService mPms;
    private boolean mProximityLockFromInCallUi = false;
    private Sensor mProximitySensor;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        /* class com.android.server.power.OppoPowerManagerHelper.AnonymousClass2 */

        public void onSensorChanged(SensorEvent event) {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public int mScreenOffReason = 0;
    ScreenOnCpuBoostHelper mScreenOnCpuBoostHelper = null;
    private final Runnable mSensorCalibrationRunnable = new Runnable() {
        /* class com.android.server.power.OppoPowerManagerHelper.AnonymousClass4 */

        public void run() {
            boolean enableCalibrate;
            if (OppoPowerManagerHelper.DEBUG_PANIC) {
                Slog.d(OppoPowerManagerHelper.TAG, "postting ProximitySensorCalibration");
            }
            if (OppoPowerManagerHelper.this.mWakefulness == 0 || OppoPowerManagerHelper.this.mWakefulness == 3) {
                enableCalibrate = true;
            } else {
                enableCalibrate = false;
            }
            OppoPowerManagerHelper.this.updateSensorCalibrationWakeLockStatus(enableCalibrate);
            OppoPowerManagerHelper.this.setProximitySensorCalibrationEnabled(enableCalibrate);
        }
    };
    private SensorManager mSensorManagerCalibrate;
    private boolean mStartGoToSleep = false;
    private final Runnable mStartTrackDozeLightChangeRunnable = new Runnable() {
        /* class com.android.server.power.OppoPowerManagerHelper.AnonymousClass5 */

        public void run() {
            if (OppoPowerManagerHelper.DEBUG_PANIC) {
                Slog.d(OppoPowerManagerHelper.TAG, "start doze light adjust task");
            }
            OppoPowerManagerHelper.this.setDozeLightSensorEnabled(true);
        }
    };
    private boolean mStopChargeInMotionMode = false;
    private final Runnable mStopTrackDozeLightChangeRunnable = new Runnable() {
        /* class com.android.server.power.OppoPowerManagerHelper.AnonymousClass6 */

        public void run() {
            if (OppoPowerManagerHelper.DEBUG_PANIC) {
                Slog.d(OppoPowerManagerHelper.TAG, "stop doze light adjust task");
            }
            OppoPowerManagerHelper.this.setDozeLightSensorEnabled(false);
        }
    };
    private boolean mSystemInCallTop = false;
    private final ArrayList<PowerManagerService.WakeLock> mWakeLocks;
    /* access modifiers changed from: private */
    public int mWakefulness;
    private boolean useProximityForceSuspend = false;

    static /* synthetic */ int access$1472(OppoPowerManagerHelper x0, int x1) {
        int i = x0.mMotionFlags & x1;
        x0.mMotionFlags = i;
        return i;
    }

    static /* synthetic */ int access$1476(OppoPowerManagerHelper x0, int x1) {
        int i = x0.mMotionFlags | x1;
        x0.mMotionFlags = i;
        return i;
    }

    public void setProximityLockFromInCallUiValueLocked(boolean isCallUi) {
        this.mProximityLockFromInCallUi = isCallUi;
    }

    public boolean getProximityLockFromInCallUiValueLocked() {
        return this.mProximityLockFromInCallUi;
    }

    public void setUseProximityForceSuspendValueLocked(boolean suspend) {
        this.useProximityForceSuspend = suspend;
    }

    public boolean getUseProximityForceSuspendValueLocked() {
        return this.useProximityForceSuspend;
    }

    public boolean isOppoProximityPositiveSuspend() {
        if (!this.useProximityForceSuspend || !this.mPms.getProximityPositiveValueLocked()) {
            return false;
        }
        return true;
    }

    public boolean isOppoProximityNegativeSuspend() {
        if (!this.useProximityForceSuspend || this.mPms.getProximityPositiveValueLocked()) {
            return false;
        }
        return true;
    }

    public void onProximityScreenOffWakeLockAcquiredLocked(PowerManagerService.WakeLock wakeLock) {
        if (wakeLock.mPackageName.equals("com.android.incallui")) {
            this.mProximityLockFromInCallUi = true;
        }
    }

    public boolean isOppoShutdownIng() {
        return mOppoShutdownIng;
    }

    public void setLastWakeLockSummary(int sum) {
        this.mLastWakeLockSummary = sum;
    }

    public static String filterRebootReason(String reason) {
        HashSet<String> validReasons = new HashSet<>();
        validReasons.add("recovery");
        validReasons.add("rf");
        validReasons.add("wlan");
        validReasons.add("mos");
        validReasons.add("ftm");
        validReasons.add("silence");
        validReasons.add("sau");
        if (validReasons.contains(reason)) {
            return reason;
        }
        Slog.w(TAG, "ignore unknown reboot reason [" + reason + "]");
        return "";
    }

    public OppoPowerManagerHelper(ArrayList<PowerManagerService.WakeLock> wakeLocks, Context context, Object lock, PowerManagerService pms, Handler handler) {
        this.mHandler = new OppoPowerManagerHelperHandler(handler.getLooper());
        this.mLock = lock;
        this.mContext = context;
        this.mPms = pms;
        this.mWakeLocks = wakeLocks;
        this.mScreenOnCpuBoostHelper = new ScreenOnCpuBoostHelper();
        mOppoBrightUtils = OppoBrightUtils.getInstance();
        mOppoBrightUtils.init(this.mContext);
    }

    public void onSystemReady(DisplayManagerInternal dms) {
        this.mBiometricsManager = (BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class);
        this.mLightsManager = (LightsManager) LocalServices.getService(LightsManager.class);
        this.mDisplayManagerInternal = dms;
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.specialversion.exp.sellmode")) {
            this.mIsSellModeVersion = true;
        }
        this.mSensorManagerCalibrate = (SensorManager) this.mContext.getSystemService("sensor");
        SensorManager sensorManager = this.mSensorManagerCalibrate;
        if (sensorManager != null) {
            this.mProximitySensor = sensorManager.getDefaultSensor(8);
            if (this.isMtk) {
                this.mDozeLightSensor = this.mSensorManagerCalibrate.getDefaultSensor(65610);
            } else {
                this.mDozeLightSensor = this.mSensorManagerCalibrate.getDefaultSensor(33171032);
            }
        }
        new IntentFilter();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(new OppoShutDownReceiver(), filter, null, this.mHandler);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("oppo.action.phone.headset.hangup");
        this.mContext.registerReceiver(new OppoPhoneHeadsetReceiver(), filter2, null, this.mHandler);
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction("com.oppo.msp.tofstatus");
        this.mContext.registerReceiver(new OppoMotionGameReceiver(), filter3, null, this.mHandler);
        mOppoBrightUtils.initAIBrightness(this.mContext);
    }

    public void onBootPhaseCompleted() {
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
        this.mMotionLcdLight = this.mLightsManager.getLight(0);
        resetMotionGameModeLocked("init");
    }

    public void updateDebugSwitch(boolean on) {
        DEBUG_PANIC = on;
    }

    public boolean oppoIntercepetWakUpEarly(String reason) {
        return false;
    }

    public boolean oppoIntercepetWakeUpMeantimeLocked(long eventTime, String reason, int reasonUid, String opPackageName, int opUid) {
        BiometricsManagerInternal biometricsManagerInternal;
        if (!"oppo.wakeup.systemui:clean up".equals(reason) || !mOppoShutdownIng) {
            if (this.mBiometricsManager != null && this.mContext.getPackageManager().hasSystemFeature(FingerprintService.SIDE_FINGERPRINT_FEATURE)) {
                if (reason.equals("android.policy:POWER")) {
                    BackTouchSensorUnlockController.mPressTouchReason = "wakeup";
                } else if (reason.equals("android.service.fingerprint:WAKEUP")) {
                    BackTouchSensorUnlockController.mPressTouchReason = "wakeupbyfingerprint";
                } else {
                    BackTouchSensorUnlockController.mPressTouchReason = "wakeupbyother";
                }
            }
            if ("android.server.wm:SCREEN_ON_FLAG".equals(reason)) {
                if (this.mSystemInCallTop) {
                    Slog.i(TAG, "windowmanager try to wakeup device while proximity not report");
                    this.mPms.oppoUserActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 1, reasonUid);
                    if (!this.mHandler.hasMessages(114)) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(114), 500);
                    }
                    this.mSystemInCallTop = false;
                    return true;
                } else if (this.useProximityForceSuspend && this.mPms.getProximityPositiveValueLocked()) {
                    if (this.mHandler.hasMessages(114)) {
                        this.mHandler.removeMessages(114);
                    }
                    Slog.i(TAG, "windowmanager try to wakeup device while proximity positive");
                    this.mPms.oppoUserActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 1, reasonUid);
                    return true;
                }
            } else if (isBiometricsBlockReason(reason)) {
                this.mScreenOnCpuBoostHelper.acquireCpuBoost(1000);
            } else if ("android.service.fingerprint:DOUBLE_HOME".equals(reason) || "android.policy.wakeup.slient".equals(reason)) {
                this.mScreenOnCpuBoostHelper.acquireCpuBoost(500);
            }
            if (isFingerprintBlockReason(reason)) {
                if (this.mBiometricsManager != null && this.mDisplayManagerInternal.isBlockDisplayByBiometrics()) {
                    Slog.d(TAG, "onWakeUp, not the first block by different biometrics");
                    this.mBiometricsManager.onWakeUp(reason);
                    return true;
                }
            } else if (isFaceBlockReason(reason) && (biometricsManagerInternal = this.mBiometricsManager) != null && biometricsManagerInternal.isFaceAutoUnlockEnabled()) {
                if (hasBlockedByOtherBiometrics(reason)) {
                    Slog.d(TAG, "onWakeUp, not the first block by different biometrics");
                    this.mBiometricsManager.onWakeUp(reason);
                    Context context = this.mContext;
                    if (context == null || context.getPackageManager() == null || !this.mContext.getPackageManager().hasSystemFeature("oppo.hardware.motor.support")) {
                        return true;
                    }
                    if (!this.mDisplayManagerInternal.isBlockDisplayByBiometrics() || this.mBiometricsManager == null) {
                        if (DEBUG_PANIC) {
                            Slog.d(TAG, "unblockScreenOnByBiometrics, reason = " + reason);
                        }
                        this.mDisplayManagerInternal.unblockScreenOnByBiometrics(reason);
                        this.mPms.scheduleNotifySfUnBlockScreenOn();
                    } else {
                        Slog.d(TAG, "screenOnUnBlockedByOther, delay 400ms for alpha change");
                        this.mBiometricsManager.onScreenOnUnBlockedByOther(reason);
                        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
                        PowerManager.WakeLock partial = null;
                        if (power != null) {
                            partial = power.newWakeLock(1, "fingerprint_delay");
                            partial.acquire(500);
                        }
                        Message msg = this.mHandler.obtainMessage();
                        msg.what = MSG_BIOMETRICS_SET_ALPHA_TIMEOUT;
                        msg.obj = partial;
                        this.mHandler.sendEmptyMessageDelayed(MSG_BIOMETRICS_SET_ALPHA_TIMEOUT, 400);
                        return true;
                    }
                }
                this.mPms.scheduleNotifyStopDream();
            } else if (!this.mDisplayManagerInternal.isBlockDisplayByBiometrics() || this.mBiometricsManager == null) {
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "unblockScreenOnByBiometrics, reason = " + reason);
                }
                this.mDisplayManagerInternal.unblockScreenOnByBiometrics(reason);
                this.mPms.scheduleNotifySfUnBlockScreenOn();
                this.mPms.scheduleNotifyStopDream();
            } else {
                Slog.d(TAG, "screenOnUnBlockedByOther, delay 400ms for alpha change");
                this.mBiometricsManager.onScreenOnUnBlockedByOther(reason);
                PowerManager power2 = (PowerManager) this.mContext.getSystemService("power");
                PowerManager.WakeLock partial2 = null;
                if (power2 != null) {
                    partial2 = power2.newWakeLock(1, "fingerprint_delay");
                    partial2.acquire(500);
                }
                Message msg2 = this.mHandler.obtainMessage();
                msg2.what = MSG_BIOMETRICS_SET_ALPHA_TIMEOUT;
                msg2.obj = partial2;
                this.mHandler.sendEmptyMessageDelayed(MSG_BIOMETRICS_SET_ALPHA_TIMEOUT, 400);
                return true;
            }
            return false;
        }
        Slog.d(TAG, "skip system ui clean wake up while shutdowning.");
        return true;
    }

    public boolean oppoIntercepetGoToSleepEarly(long eventTime, int reason, int flags, int uid) {
        boolean needIntercepet = false;
        if (reason == 10 && !this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
            Slog.d(TAG, "oppoIntercepetGoToSleepEarly GO_TO_SLEEP_REASON_FINGERPRINT, reason = " + reason);
            needIntercepet = true;
        }
        if (this.mBiometricsManager != null && this.mContext.getPackageManager().hasSystemFeature(FingerprintService.SIDE_FINGERPRINT_FEATURE)) {
            if (reason == 4) {
                needIntercepet = this.mBiometricsManager.notifyPowerKeyPressed("gotosleep");
                Slog.d(TAG, "oppoIntercepetGoToSleepEarly gotosleep, reason = " + reason + ";needIntercepet=" + needIntercepet);
            } else {
                needIntercepet = this.mBiometricsManager.notifyPowerKeyPressed("gotosleepbyother");
                Slog.d(TAG, "oppoIntercepetGoToSleepEarly gotosleep, reason = " + reason + ";needIntercepet=" + needIntercepet);
            }
        }
        if (reason == 4 && hasBlockedByFingerprint()) {
            Slog.d(TAG, "unblockScreenOnByBiometrics, reason = POWER-GotoSleep");
            this.mDisplayManagerInternal.unblockScreenOnByBiometrics("android.server.power:POWER");
            BiometricsManagerInternal biometricsManagerInternal = this.mBiometricsManager;
            if (biometricsManagerInternal != null) {
                biometricsManagerInternal.notifyPowerKeyPressed();
            }
            Slog.d(TAG, "Not goTosleep( " + reason + " ) due to fingerPrint", new Throwable("FP DEBUG"));
            needIntercepet = true;
        }
        if (reason != 4 || !hasBlockedByFace()) {
            return needIntercepet;
        }
        Slog.d(TAG, "ignore power key while block by face");
        return true;
    }

    public boolean oppoIntercepetGoToSleepMeantimeLocked(long eventTime, int reason, int flags, int uid) {
        boolean needIntercepet = false;
        if (this.mBiometricsManager != null && this.mContext.getPackageManager().hasSystemFeature(FingerprintService.SIDE_FINGERPRINT_FEATURE)) {
            if (reason == 4) {
                BackTouchSensorUnlockController.mPressTouchReason = "gotosleep";
            } else {
                this.mBiometricsManager.notifyPowerKeyPressed("gotosleepbyother");
            }
        }
        if (this.mHandler.hasMessages(114)) {
            this.mHandler.removeMessages(114);
        }
        if (checkIsMotionGameModeLocked() && this.mMotionGameDisplayState == 0 && reason == 4) {
            Slog.d(TAG, "screen on for Motion Game when go to sleep,screen off after " + this.MOTION_GAME_SCREEN_OFF_TIME_OUT + "ms");
            setDisplayStateOnForMotionGameLocked(this.mPms.getLastUpdatedBrightness());
            scheduleUpdateMotionGameDisplatStateTask(this.MOTION_GAME_SCREEN_OFF_TIME_OUT);
            return false;
        }
        this.mHandler.removeMessages(MSG_CHECK_MOTION_GAME_DISPLAY_STATE);
        if (mOppoShutdownIng) {
            Slog.d(TAG, "goToSleepNoUpdateLocked: Not go to sleep when shutdown!!!");
            needIntercepet = true;
        }
        Slog.d(TAG, "goToSleepNoUpdateLocked  needIntercepet: " + needIntercepet);
        return needIntercepet;
    }

    public void onWakefulnessChangeFinished(int wakefulness) {
        BiometricsManagerInternal biometricsManagerInternal = this.mBiometricsManager;
        if (biometricsManagerInternal != null && wakefulness == 1) {
            biometricsManagerInternal.onWakeUpFinish();
        }
    }

    public void onWakeUpNoUpdatedLockedBegin(long eventTime, String reason, int uid, String opPackageName, int opUid) {
        this.mStartGoToSleep = false;
        if (DEBUG_SPEW) {
            Slog.i(TAG, "BiometricsService.onWakeUp in");
        }
        if ("android.service.fingerprint:WAKEUP".equals(reason)) {
            this.mDisplayManagerInternal.blockScreenOnByBiometrics(reason);
        }
        BiometricsManagerInternal biometricsManagerInternal = this.mBiometricsManager;
        if (biometricsManagerInternal != null) {
            biometricsManagerInternal.onWakeUp(reason);
        }
    }

    public void onGoToSleepNoUpdateLockedBegin(long eventTime, int reason, int flags, int uid) {
        this.mStartGoToSleep = true;
        if (this.mBiometricsManager != null) {
            if (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
                this.mDisplayManagerInternal.unblockScreenOnByBiometrics("UNBLOCK_REASON_GO_TO_SLEEP");
            }
            this.mBiometricsManager.onGoToSleep();
        }
    }

    public void onWakefulnessChangeStarted(int wakefulness, int reason, long eventTime) {
        this.mWakefulness = wakefulness;
        if (this.mWakefulness != 3 || reason != 10) {
            this.mHandler.post(this.mSensorCalibrationRunnable);
        }
    }

    public void onUpdateWakeLockSummaryLocked() {
    }

    public void onAcquireWakeLockInternalEarly() {
    }

    public void onUserActivityNoUpdateLocked(int wakeFullness) {
        if (wakeFullness == 1 && checkIsMotionGameModeLocked() && this.mMotionGameDisplayState == 2) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "Reset motion game display screenoff timeout for useractivity");
            }
            scheduleUpdateMotionGameDisplatStateTask(this.MOTION_GAME_SCREEN_OFF_TIME_OUT);
        }
    }

    public void onDisplayStateChange(int state) {
        if (DEBUG_PANIC) {
            Slog.i(TAG, "onDisplayStateChange state = " + state + "  mDisplayState = " + this.mDisplayState);
        }
        this.mMotionGameDisplayState = state;
        this.mDisplayState = state;
        if (this.mDisplayState == 1 && this.mDozeLightSensorEnabled) {
            setDozeLightSensorEnabled(false);
        }
    }

    public void onBatteryStatusChanged(boolean ispowered, int plugType, int level) {
        this.mIsPowered = this.mIsPowered;
        boolean z = this.mIsPowered;
        if (z != this.mLastIsPowered) {
            this.mLastIsPowered = z;
            scheduleUpdateMotionGameChargeStateTask(0);
        }
    }

    public void recordScreenOffReason(int reason) {
        this.mScreenOffReason = reason;
        this.mHandler.post(new Runnable() {
            /* class com.android.server.power.OppoPowerManagerHelper.AnonymousClass1 */

            public void run() {
                if (OppoPowerManagerHelper.DEBUG_PANIC) {
                    Slog.i(OppoPowerManagerHelper.TAG, "update sys.power.screenoff.reason");
                }
                SystemProperties.set("sys.power.screenoff.reason", "" + OppoPowerManagerHelper.this.mScreenOffReason);
            }
        });
    }

    public void dumpWakeLockLocked() {
        int numWakeLocks = this.mWakeLocks.size();
        if (numWakeLocks > 0) {
            Slog.d(TAG, "wakelock list dump: mLocks.size=" + numWakeLocks + ":");
            for (int i = 0; i < numWakeLocks; i++) {
                PowerManagerService.WakeLock wakeLock = this.mWakeLocks.get(i);
                String type = wakeLock.getLockLevelString();
                long total_time = SystemClock.uptimeMillis() - wakeLock.mActiveSince;
                Slog.d(TAG, "No." + i + ": " + type + " '" + wakeLock.mTag + "'activated(flags=" + wakeLock.mFlags + ", uid=" + wakeLock.mOwnerUid + ", pid=" + wakeLock.mOwnerPid + ") total=" + total_time + "ms)");
            }
        }
    }

    /* access modifiers changed from: private */
    public class OppoPowerManagerHelperHandler extends Handler {
        public OppoPowerManagerHelperHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Slog.d(OppoPowerManagerHelper.TAG, "handleMessage:" + msg.what);
            int i = msg.what;
            if (i != 10) {
                switch (i) {
                    case OppoPowerManagerHelper.MSG_CHECK_MOTION_GAME_DISPLAY_STATE /*{ENCODED_INT: 110}*/:
                        OppoPowerManagerHelper.this.handleMotionGameDisplayState();
                        return;
                    case 111:
                        synchronized (OppoPowerManagerHelper.this.mLock) {
                            OppoPowerManagerHelper.this.updateChargeStateInMotionModeLocked(OppoPowerManagerHelper.this.mIsPowered);
                        }
                        return;
                    case 112:
                        synchronized (OppoPowerManagerHelper.this.mLock) {
                            if (!((OppoPowerManagerHelper.this.mMotionFlags & 2) == 0 || (OppoPowerManagerHelper.this.mMotionFlags & 1) == 0)) {
                                OppoPowerManagerHelper.this.setMotionDisplayRefreshRate(60.0f);
                            }
                        }
                        return;
                    case 113:
                        if (OppoPowerManagerHelper.DEBUG_PANIC) {
                            Slog.d(OppoPowerManagerHelper.TAG, "MSG_PROCESS_AOD_LIGHT_EVENT");
                        }
                        OppoPowerManagerHelper.this.processLightSensorEvent(((Float) msg.obj).floatValue());
                        return;
                    case 114:
                        Slog.d(OppoPowerManagerHelper.TAG, "wakeup from wms after delay:500");
                        OppoPowerManagerHelper.this.mPms.oppoWakeUpInternal(SystemClock.uptimeMillis(), 102, "android.server.wm:SCREEN_ON_FLAG", 1000, OppoPowerManagerHelper.this.mContext.getOpPackageName(), 1000);
                        return;
                    default:
                        switch (i) {
                            case OppoPowerManagerHelper.MSG_OPPO_PHONE_HEADSET_HANGUP /*{ENCODED_INT: 201}*/:
                                Slog.d(OppoPowerManagerHelper.TAG, "PowerMS OppoPhoneHeadsetReceiver: HANDLE MES");
                                OppoPowerManagerHelper.this.mPms.oppoGoToSleepInternal(SystemClock.uptimeMillis(), 4, 0, 1000);
                                return;
                            case OppoPowerManagerHelper.MSG_BIOMETRICS_SET_ALPHA_TIMEOUT /*{ENCODED_INT: 202}*/:
                                if (OppoPowerManagerHelper.this.mDisplayManagerInternal != null) {
                                    if (OppoPowerManagerHelper.DEBUG) {
                                        Slog.d(OppoPowerManagerHelper.TAG, "unblockScreenOnByBiometrics, alpha has been changed");
                                    }
                                    OppoPowerManagerHelper.this.mDisplayManagerInternal.unblockScreenOnByBiometrics("MSG_BIOMETRICS_SET_ALPHA_TIMEOUT");
                                }
                                OppoPowerManagerHelper.this.mPms.oppoWakeUpInternal(SystemClock.uptimeMillis(), 2, "android.server.power:POWER", 1000, OppoPowerManagerHelper.this.mContext.getOpPackageName(), 1000);
                                PowerManager.WakeLock partial = (PowerManager.WakeLock) msg.obj;
                                if (partial != null) {
                                    partial.release();
                                    return;
                                }
                                return;
                            case OppoPowerManagerHelper.MSG_DUMP_WAKE_LOCKS_LIST /*{ENCODED_INT: 203}*/:
                                synchronized (OppoPowerManagerHelper.this.mLock) {
                                    OppoPowerManagerHelper.this.dumpWakeLockLocked();
                                }
                                OppoPowerManagerHelper.this.tryToTrackWakelocks();
                                return;
                            default:
                                return;
                        }
                }
            } else {
                if (OppoPowerManagerHelper.DEBUG_PANIC) {
                    Slog.d(OppoPowerManagerHelper.TAG, "MSG_SENSOR_CALIBRATION_TIMEOUT");
                }
                OppoPowerManagerHelper.this.setProximitySensorCalibrationEnabled(false);
                OppoPowerManagerHelper.this.updateSensorCalibrationWakeLockStatus(false);
            }
        }
    }

    public void updateOppoProximityScreenoffPolicyLocked(int wakeLockSummary, int wakefulness) {
        if (!this.mProximityLockFromInCallUi && wakefulness == 1) {
            this.mDisplayManagerInternal.setUseProximityForceSuspend(false);
        } else if (this.mProximityLockFromInCallUi) {
            this.mDisplayManagerInternal.setUseProximityForceSuspend(true);
            this.useProximityForceSuspend = true;
        }
    }

    public boolean isInDozeWakeLockDisableWhiteList(PowerManagerService.WakeLock wakeLock) {
        if (wakeLock.mPackageName == null || !wakeLock.mPackageName.equals("com.mobiletools.systemhelper")) {
            return false;
        }
        return true;
    }

    public String handleWakeUpReasonEarly(String reason, int uid, String opPkg, int opUid) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "handleWakeUpReasonEarly:" + reason);
        }
        if (this.mBiometricsManager != null && this.mContext.getPackageManager().hasSystemFeature(FingerprintService.SIDE_FINGERPRINT_FEATURE)) {
            if (reason.equals("android.policy:POWER")) {
                this.mBiometricsManager.notifyPowerKeyPressed("wakeup");
            } else if (reason.equals("android.service.fingerprint:WAKEUP")) {
                this.mBiometricsManager.notifyPowerKeyPressed("wakeupbyfingerprint");
            } else {
                this.mBiometricsManager.notifyPowerKeyPressed("wakeupbyother");
            }
        }
        if (reason == null || !reason.contains("oppoincall") || uid != 1000) {
            this.mSystemInCallTop = false;
        } else {
            this.mSystemInCallTop = true;
        }
        if (this.mSystemInCallTop) {
            return "android.server.wm:SCREEN_ON_FLAG";
        }
        return reason;
    }

    public int getScreenStateInternal() {
        int result = 0;
        DisplayManagerInternal displayManagerInternal = this.mDisplayManagerInternal;
        if (displayManagerInternal != null && displayManagerInternal.getScreenState() == 1 && !this.mStartGoToSleep) {
            result = 1;
        }
        if (DEBUG_PANIC || DEBUG) {
            Slog.d(TAG, "get Screen State, result = " + result + ", start sleep = " + this.mStartGoToSleep);
        }
        return result;
    }

    public void scheduleBiometricsSetAlphaTimeoutCheck(String reason) {
        Slog.d(TAG, "screenOnUnBlockedByOther, delay 400ms for alpha change");
        this.mBiometricsManager.onScreenOnUnBlockedByOther(reason);
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        PowerManager.WakeLock partial = null;
        if (power != null) {
            partial = power.newWakeLock(1, "fingerprint_delay");
            partial.acquire(500);
        }
        Message msg = this.mHandler.obtainMessage();
        msg.what = MSG_BIOMETRICS_SET_ALPHA_TIMEOUT;
        msg.obj = partial;
        this.mHandler.sendEmptyMessageDelayed(MSG_BIOMETRICS_SET_ALPHA_TIMEOUT, 400);
    }

    public boolean hasBiometricsSetAlphaTimeoutCheck() {
        if (this.mHandler.hasMessages(MSG_BIOMETRICS_SET_ALPHA_TIMEOUT)) {
            return true;
        }
        return false;
    }

    public boolean isStartGoToSleep() {
        return this.mStartGoToSleep;
    }

    public boolean isBiometricsBlockReason(String reason) {
        return "android.service.fingerprint:WAKEUP".equals(reason) || "android.policy:POWER".equals(reason) || "oppo.wakeup.gesture:DOUBLE_TAP_SCREEN".equals(reason) || "oppo.wakeup.gesture:LIFT_HAND".equals(reason);
    }

    public boolean isFingerprintBlockReason(String reason) {
        return "android.service.fingerprint:WAKEUP".equals(reason);
    }

    public boolean isFaceBlockReason(String reason) {
        return "android.policy:POWER".equals(reason) || "oppo.wakeup.gesture:DOUBLE_TAP_SCREEN".equals(reason) || "oppo.wakeup.gesture:LIFT_HAND".equals(reason);
    }

    public boolean hasBlockedByFace() {
        return this.mBiometricsManager != null && this.mDisplayManagerInternal.isBlockDisplayByBiometrics() && (this.mDisplayManagerInternal.hasBiometricsBlockedReason("android.policy:POWER") || this.mDisplayManagerInternal.hasBiometricsBlockedReason("oppo.wakeup.gesture:DOUBLE_TAP_SCREEN") || this.mDisplayManagerInternal.hasBiometricsBlockedReason("oppo.wakeup.gesture:LIFT_HAND"));
    }

    public boolean hasBlockedByFingerprint() {
        return this.mBiometricsManager != null && this.mDisplayManagerInternal.isBlockDisplayByBiometrics() && this.mDisplayManagerInternal.hasBiometricsBlockedReason("android.service.fingerprint:WAKEUP");
    }

    public boolean hasBlockedByOtherBiometrics(String reason) {
        if (isFingerprintBlockReason(reason)) {
            if (hasBlockedByFingerprint() || !hasBlockedByFace()) {
                return false;
            }
            return true;
        } else if (!isFaceBlockReason(reason) || hasBlockedByFace() || !hasBlockedByFingerprint()) {
            return false;
        } else {
            return true;
        }
    }

    public void unblockScreenOn(String reason) {
        Slog.d(TAG, "unblockScreenOn reason = " + reason + StringUtils.SPACE + hasBlockedByFingerprint() + StringUtils.SPACE + hasBlockedByFace() + StringUtils.SPACE + this.mBiometricsManager.isFaceFingerprintCombineUnlockEnabled());
        if ((BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_TIMEOUT.equals(reason) || BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_FACE_AUTHENTICATE_FAIL.equals(reason) || BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_CAMERA_TIMEOUT.equals(reason) || BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_ERROR.equals(reason)) && hasBlockedByFingerprint() && !isFaceBlockReason(this.mBiometricsWakeupReason) && this.mBiometricsManager.isFaceFingerprintCombineUnlockEnabled() && this.mBiometricsManager.isOpticalFingerprintSupport()) {
            DisplayManagerInternal displayManagerInternal = this.mDisplayManagerInternal;
            if (displayManagerInternal != null) {
                displayManagerInternal.removeFaceBlockReasonFromBlockReasonList();
            } else {
                Slog.w(TAG, "mDisplayManagerInternal = null");
            }
        } else {
            startUnblockScreenOn(reason);
        }
    }

    public void wakeUpAndBlockScreenOn(String reason) {
        Slog.d(TAG, "wakeUpAndBlockScreenOn reason= " + reason);
        this.mBiometricsWakeupReason = reason;
        startWakeUpAndBlockScreenOn(reason);
    }

    private void startWakeUpAndBlockScreenOn(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "startWakeUpAndBlockScreenOn, reason = " + reason);
        }
        if ("android.service.fingerprint:WAKEUP".equals(reason)) {
            this.mPms.oppoWakeUpInternal(SystemClock.uptimeMillis(), 98, reason, 1000, this.mContext.getOpPackageName(), 1000);
        } else {
            this.mDisplayManagerInternal.blockScreenOnByBiometrics(reason);
        }
    }

    public void startUnblockScreenOn(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "startUnblockScreenOn, reason = " + reason);
        }
        this.mStartGoToSleep = false;
        this.mPms.removeScreenBrightnessBoost();
        DisplayManagerInternal displayManagerInternal = this.mDisplayManagerInternal;
        if (displayManagerInternal != null) {
            displayManagerInternal.unblockScreenOnByBiometrics(reason);
        }
        this.mPms.oppoWakeUpInternal(SystemClock.uptimeMillis(), 98, reason, 1000, this.mContext.getOpPackageName(), 1000);
    }

    public void gotoSleepWhenScreenOnBlocked(String reason) {
        Slog.d(TAG, "gotoSleepWhenScreenOnBlocked reason= " + reason);
        startGotoSleepWhenScreenOnBlocked(reason);
    }

    private void startGotoSleepWhenScreenOnBlocked(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "startGotoSleepWhenScreenOnBlocked, reason = " + reason);
        }
        if (this.mDisplayManagerInternal.isBlockScreenOnByBiometrics()) {
            if (this.mHandler.hasMessages(MSG_BIOMETRICS_SET_ALPHA_TIMEOUT)) {
                Slog.d(TAG, "Screen turning on for fingerprint, ignore verify failed");
            } else if (this.mPms.isShouldEnterOppoAod()) {
                this.mPms.oppoGoToSleepInternal(SystemClock.uptimeMillis(), 10, 0, 1000);
            } else {
                this.mPms.oppoGoToSleepInternal(SystemClock.uptimeMillis(), 10, 1, 1000);
            }
        }
    }

    private final class OppoPhoneHeadsetReceiver extends BroadcastReceiver {
        private OppoPhoneHeadsetReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Slog.d(OppoPowerManagerHelper.TAG, "PowerMS OppoPhoneHeadsetReceiver: oppo.action.phone.headset.hangup");
            if (OppoPowerManagerHelper.this.mPms.getProximityPositiveValueLocked() && !OppoPowerManagerHelper.this.mPms.checkProximityScreenOffWakeLockAcquired()) {
                Message msg = OppoPowerManagerHelper.this.mHandler.obtainMessage(OppoPowerManagerHelper.MSG_OPPO_PHONE_HEADSET_HANGUP);
                msg.setAsynchronous(true);
                Slog.d(OppoPowerManagerHelper.TAG, "PowerMS OppoPhoneHeadsetReceiver: SEND MES");
                OppoPowerManagerHelper.this.mHandler.sendMessageDelayed(msg, 2000);
                Slog.d(OppoPowerManagerHelper.TAG, "PowerMS OppoPhoneHeadsetReceiver: SEND MES DONE");
            }
        }
    }

    private final class OppoMotionGameReceiver extends BroadcastReceiver {
        private OppoMotionGameReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                synchronized (OppoPowerManagerHelper.this.mLock) {
                    boolean tofCamReady = intent.getBooleanExtra("status", false);
                    Slog.d(OppoPowerManagerHelper.TAG, "OppoMotionGameReceiver: tofCamReady=" + tofCamReady);
                    if (!tofCamReady) {
                        OppoPowerManagerHelper.access$1472(OppoPowerManagerHelper.this, -5);
                    } else {
                        OppoPowerManagerHelper.access$1476(OppoPowerManagerHelper.this, 4);
                    }
                    OppoPowerManagerHelper.this.scheduleUpdateMotionGameDisplatStateTask(OppoPowerManagerHelper.this.checkIsMotionGameModeLocked() ? OppoPowerManagerHelper.this.MOTION_GAME_SCREEN_OFF_TIME_OUT : 0);
                    OppoPowerManagerHelper.this.scheduleUpdateMotionGameChargeStateTask(0);
                }
            }
        }
    }

    private final class OppoShutDownReceiver extends BroadcastReceiver {
        private OppoShutDownReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (OppoPowerManagerHelper.DEBUG_PANIC) {
                Slog.d(OppoPowerManagerHelper.TAG, "PowerMS NotSleepingWhenShutdowning: received a shutdown broadcast");
            }
            boolean unused = OppoPowerManagerHelper.mOppoShutdownIng = true;
            OppoPowerManagerHelper.this.mPms.setOppoShutdownIngStatus(OppoPowerManagerHelper.mOppoShutdownIng);
            OppoPowerManagerHelper.this.mPms.turnOffButtonLight();
        }
    }

    /* JADX INFO: Multiple debug info for r0v6 java.lang.String: [D('index' int), D('logCategoryTag' java.lang.String)] */
    public boolean dynamicallyConfigPowerManagerServiceLogTag(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length < 1 || !"log".equals(args[0])) {
            return false;
        }
        if (args.length != 3) {
            pw.println("Invalid argument! Get detail help as bellow:");
            logOutPowerManagerServiceLogTagHelp(pw);
            return true;
        }
        pw.println("dynamicallyConfigPowerManagerServiceLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigPowerManagerServiceLogTag, args[" + index + "]:" + args[index]);
        }
        String logCategoryTag = args[1];
        boolean on = AOD_LIGHT_DARK.equals(args[2]);
        pw.println("dynamicallyConfigPowerManagerServiceLogTag, logCategoryTag:" + logCategoryTag + ", on:" + on);
        if ("all".equals(logCategoryTag)) {
            DEBUG_PANIC = on;
            DEBUG = on;
            DEBUG_SPEW = on;
            PowerManagerService powerManagerService = this.mPms;
            powerManagerService.DEBUG_PANIC = on;
            powerManagerService.DEBUG = on;
            powerManagerService.DEBUG_SPEW = on;
            OppoLightsService.DEBUG = on;
            Notifier.DEBUG_PANIC = on;
        } else {
            pw.println("Invalid log tag argument! Get detail help as bellow:");
            logOutPowerManagerServiceLogTagHelp(pw);
        }
        return true;
    }

    public void logOutPowerManagerServiceLogTagHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 All PowerManagerService log");
        pw.println("cmd: dumpsys power log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    public boolean isCPULock(PowerManagerService.WakeLock wakeLock) {
        if ((wakeLock.mFlags & 1) == 0 && (wakeLock.mFlags & 128) == 0) {
            return false;
        }
        return true;
    }

    public void tryToTrackWakelocks() {
        if (!this.mHandler.hasMessages(MSG_DUMP_WAKE_LOCKS_LIST)) {
            Message msg = this.mHandler.obtainMessage(MSG_DUMP_WAKE_LOCKS_LIST);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, 1800000);
        }
    }

    public void stopTrackWakelocks() {
        if (this.mHandler.hasMessages(MSG_DUMP_WAKE_LOCKS_LIST)) {
            this.mHandler.removeMessages(MSG_DUMP_WAKE_LOCKS_LIST);
        }
    }

    /* access modifiers changed from: private */
    public void setProximitySensorCalibrationEnabled(boolean enable) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "setProximitySensorCalibrationEnabled:" + enable);
        }
        SensorManager sensorManager = this.mSensorManagerCalibrate;
        if (sensorManager != null) {
            if (enable) {
                if (!this.mCalibrateProximitySensorEnabled) {
                    this.mCalibrateProximitySensorEnabled = true;
                    sensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, 3, this.mHandler);
                    this.mHandler.removeMessages(10);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(10), 10000);
                } else if (DEBUG_PANIC) {
                    Slog.d(TAG, "already set !!! mCalibrateProximitySensorEnabled: " + this.mCalibrateProximitySensorEnabled);
                }
            } else if (this.mCalibrateProximitySensorEnabled) {
                this.mCalibrateProximitySensorEnabled = false;
                sensorManager.unregisterListener(this.mProximitySensorListener);
            } else if (DEBUG_PANIC) {
                Slog.d(TAG, "already cancel !!! mCalibrateProximitySensorEnabled:" + this.mCalibrateProximitySensorEnabled);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDozeLightSensorEnabled(boolean enable) {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "setDozeLightSensorEnabled:" + enable);
        }
        if (this.mSensorManagerCalibrate != null) {
            if (enable) {
                if (!this.mDozeLightSensorEnabled) {
                    this.mDozeLightSensorEnabled = true;
                    this.mHandler.removeMessages(113);
                    this.mSensorManagerCalibrate.registerListener(this.mDozeLightSensorListener, this.mDozeLightSensor, 3, this.mHandler);
                } else if (DEBUG_PANIC) {
                    Slog.d(TAG, "already set !!! mDozeLightSensorEnabled: " + this.mDozeLightSensorEnabled);
                }
            } else if (this.mDozeLightSensorEnabled) {
                this.mDozeLightSensorEnabled = false;
                this.mHandler.removeMessages(113);
                this.mSensorManagerCalibrate.unregisterListener(this.mDozeLightSensorListener);
            } else if (DEBUG_PANIC) {
                Slog.d(TAG, "already cancel !!! mDozeLightSensorEnabled:" + this.mDozeLightSensorEnabled);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleLightSensorEvent(long time, float lux) {
        this.mHandler.removeMessages(113);
        Message msg = this.mHandler.obtainMessage(113);
        msg.obj = Float.valueOf(lux);
        this.mHandler.sendMessageDelayed(msg, 500);
    }

    /* access modifiers changed from: private */
    public void processLightSensorEvent(float lux) {
        Slog.d(TAG, "processLightSensorEvent: lux" + lux);
        if (lux == 1.0f) {
            setDozeDisplayBrightness(AOD_LIGHT_BRIGHT);
        } else if (lux == OppoBrightUtils.MIN_LUX_LIMITI) {
            setDozeDisplayBrightness(AOD_LIGHT_DARK);
        }
    }

    private void setDozeDisplayBrightness(String value) {
        File file = new File(AOD_LIGHT_MODE_PATH);
        Slog.d(TAG, "write value to AOD_LIGHT_MODE_PATH:" + value);
        if (file.exists()) {
            OutputStream outstream = null;
            try {
                outstream = new FileOutputStream(file);
                outstream.write(value.getBytes());
                outstream.flush();
                try {
                    outstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                if (outstream != null) {
                    outstream.close();
                }
            } catch (IOException e3) {
                e3.printStackTrace();
                if (outstream != null) {
                    outstream.close();
                }
            } catch (Throwable th) {
                if (outstream != null) {
                    try {
                        outstream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                throw th;
            }
        } else {
            Slog.d(TAG, "AOD_LIGHT_MODE_PATH no exist");
        }
    }

    /* access modifiers changed from: private */
    public void updateSensorCalibrationWakeLockStatus(boolean enable) {
        PowerManager power;
        if (this.calibrateWakelock == null && (power = (PowerManager) this.mContext.getSystemService("power")) != null) {
            this.calibrateWakelock = power.newWakeLock(1, "Sensor Calibration");
        }
        if (enable) {
            PowerManager.WakeLock wakeLock = this.calibrateWakelock;
            if (wakeLock != null && !wakeLock.isHeld()) {
                this.calibrateWakelock.acquire();
                return;
            }
            return;
        }
        PowerManager.WakeLock wakeLock2 = this.calibrateWakelock;
        if (wakeLock2 != null && wakeLock2.isHeld()) {
            this.calibrateWakelock.release();
        }
    }

    public void scheduleStartAutomaticAodBacklightAdjustment() {
        if (!this.mDozeLightSensorEnabled) {
            this.mHandler.post(this.mStartTrackDozeLightChangeRunnable);
        }
    }

    public void scheduleStopAutomaticAodBacklightAdjustment() {
        if (this.mDozeLightSensorEnabled) {
            this.mHandler.post(this.mStopTrackDozeLightChangeRunnable);
        }
    }

    public int getMotionFlags() {
        return this.mMotionFlags;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0066, code lost:
        return;
     */
    public void handleMotionGameDisplayState() {
        synchronized (this.mLock) {
            Slog.d(TAG, "handleMotionGameDisplayState...");
            boolean isMotionMode = checkIsMotionGameModeLocked();
            Slog.d(TAG, "mMotionFlags=" + this.mMotionFlags + " isMotionMode=" + isMotionMode + "  mWakefulness=" + this.mWakefulness + "  mLastScreenBrightness=" + this.mLastScreenBrightness);
            if (this.mWakefulness != 0) {
                if (this.mWakefulness != 3) {
                    if (isMotionMode) {
                        this.mHandler.post(new Runnable() {
                            /* class com.android.server.power.OppoPowerManagerHelper.AnonymousClass8 */

                            public void run() {
                                int unused = OppoPowerManagerHelper.this.setDisplayStateOffForMotionGameLocked();
                            }
                        });
                    } else {
                        this.mHandler.post(new Runnable() {
                            /* class com.android.server.power.OppoPowerManagerHelper.AnonymousClass9 */

                            public void run() {
                                int unused = OppoPowerManagerHelper.this.setDisplayStateOnForMotionGameLocked(OppoPowerManagerHelper.this.mPms.getLastUpdatedBrightness());
                            }
                        });
                    }
                }
            }
            if (isMotionMode) {
                Slog.d(TAG, "force wake up due to motion events");
                ((PowerManager) this.mContext.getSystemService("power")).wakeUp(SystemClock.uptimeMillis(), "android.server.power:Motion");
                scheduleUpdateMotionGameDisplatStateTask(this.MOTION_GAME_SCREEN_OFF_TIME_OUT);
                return;
            }
            Slog.d(TAG, "do nothing whe device is sleeping...");
        }
    }

    public void notifyMotionGameAppForegroundLocked(String packageName, boolean foreground) {
        Slog.d(TAG, "notifyMotionGameAppForeground: " + packageName + "  foreground:" + foreground);
        if (!foreground) {
            this.mMotionFlags &= -2;
        } else {
            this.mMotionFlags |= 1;
            this.mMotionGameAppPackageName = packageName;
        }
        scheduleUpdateMotionGameDisplatStateTask(checkIsMotionGameModeLocked() ? this.MOTION_GAME_SCREEN_OFF_TIME_OUT : 0);
        scheduleUpdateMotionGameChargeStateTask(0);
        scheduleUpdateMotionDisplayRefreshRateTask(0);
    }

    /* access modifiers changed from: private */
    public void scheduleUpdateMotionGameDisplatStateTask(long delay) {
        this.mHandler.removeMessages(MSG_CHECK_MOTION_GAME_DISPLAY_STATE);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_CHECK_MOTION_GAME_DISPLAY_STATE), delay);
    }

    /* access modifiers changed from: private */
    public void scheduleUpdateMotionGameChargeStateTask(long delay) {
        this.mHandler.removeMessages(111);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(111), delay);
    }

    /* access modifiers changed from: private */
    public void updateChargeStateInMotionModeLocked(boolean isPowered) {
        Slog.d(TAG, "updateChargeStateInMotionMode...");
        String topApp = getTopAppPkgName();
        int mmi_charging_enable = getCurrentChargeStateForSaleInternal();
        boolean isMotionMode = checkIsMotionGameModeLocked();
        Slog.d(TAG, "mIsPowered=" + this.mIsPowered + " mStopChargeInMotionMode=" + this.mStopChargeInMotionMode + " mMotionFlags=" + this.mMotionFlags + "  mmi_charging_enable=" + mmi_charging_enable);
        if (!isPowered || this.mStopChargeInMotionMode) {
            if (!isPowered || !this.mStopChargeInMotionMode) {
                this.mStopChargeInMotionMode = false;
            } else if (isMotionMode) {
            } else {
                if (mmi_charging_enable == 0) {
                    Slog.d(TAG, "resume charge...");
                    SystemProperties.set("sys.oppo.kinect.game", AOD_LIGHT_BRIGHT);
                    this.mStopChargeInMotionMode = false;
                    return;
                }
                Slog.d(TAG, "already resume");
            }
        } else if (!isMotionMode) {
        } else {
            if (mmi_charging_enable != 1) {
                Slog.d(TAG, "already stop");
            } else if (topApp == null || "".equals(topApp) || !topApp.equals(this.mMotionGameAppPackageName)) {
                Slog.d(TAG, "topApp:" + topApp + "   game app:" + this.mMotionGameAppPackageName);
            } else {
                Slog.d(TAG, "stop charge...");
                SystemProperties.set("sys.oppo.kinect.game", AOD_LIGHT_DARK);
                this.mStopChargeInMotionMode = true;
            }
        }
    }

    private String getTopAppPkgName() {
        ComponentName cn = ((ActivityManager) this.mContext.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY)).getTopAppName();
        return cn != null ? cn.getPackageName() : "";
    }

    private void resetMotionGameModeLocked(String reason) {
        Slog.d(TAG, "resetMotionGameModeLocked,reason:" + reason);
        this.mHandler.removeMessages(MSG_CHECK_MOTION_GAME_DISPLAY_STATE);
        this.mMotionFlags = 0;
        this.mMotionGameDisplayState = -1;
        this.mStopChargeInMotionMode = false;
    }

    public int getMotionGameDisplayStateLocked() {
        return this.mMotionGameDisplayState;
    }

    /* access modifiers changed from: private */
    public int setDisplayStateOnForMotionGameLocked(int brightness) {
        try {
            this.mLastScreenBrightness = brightness;
            Slog.d(TAG, "setDisplayStateOnForMotionGame... brightness:" + brightness);
            if (this.mMotionGameDisplayState == 2) {
                Slog.d(TAG, "already turned on , mMotionGameDisplayState:" + this.mMotionGameDisplayState);
                return -1;
            }
            this.mMotionGameDisplayState = 2;
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInt(0);
                data.writeInt(2);
                flinger.transact(20000, data, reply, 0);
                int result = reply.readInt();
                Slog.d(TAG, "result:" + result);
                data.recycle();
                reply.recycle();
                Parcel data1 = Parcel.obtain();
                Parcel reply1 = Parcel.obtain();
                data1.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(20003, data1, reply1, 0);
                flinger.transact(OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_RECOVERY_INFO, data1, reply1, 0);
                int result1 = reply1.readInt();
                Slog.d(TAG, "result:" + result1);
                data1.recycle();
                reply1.recycle();
                if (this.mMotionLcdLight != null) {
                    this.mMotionLcdLight.setBrightness(this.mLastScreenBrightness);
                } else {
                    Slog.e(TAG, "mMotionLcdLight is null");
                }
                return result;
            }
            return -1;
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    public int setDisplayStateOffForMotionGameLocked() {
        try {
            Slog.d(TAG, "setDisplayStateOffForMotionGame...");
            if (this.mMotionGameDisplayState == 0) {
                Slog.d(TAG, "already turned off , mMotionGameDisplayState:" + this.mMotionGameDisplayState);
                return -1;
            }
            this.mMotionGameDisplayState = 0;
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInt(0);
                data.writeInt(0);
                flinger.transact(20000, data, reply, 0);
                int result = reply.readInt();
                data.recycle();
                reply.recycle();
                Slog.d(TAG, "result:" + result);
                if (this.mMotionLcdLight != null) {
                    this.mMotionLcdLight.setBrightness(0);
                } else {
                    Slog.e(TAG, "mMotionLcdLight is null");
                }
                return result;
            }
            return -1;
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    public boolean checkIsMotionGameModeLocked() {
        int i = this.mMotionFlags;
        if ((i & 2) == 0 || (i & 1) == 0 || (i & 4) == 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void scheduleUpdateMotionDisplayRefreshRateTask(long delay) {
        this.mHandler.removeMessages(112);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(112), delay);
    }

    /* access modifiers changed from: private */
    public void setMotionDisplayRefreshRate(float requestedRefreshRate) {
        DisplayManagerInternal displayManagerInternal;
        Slog.d(TAG, "setMotionDisplayRefreshRate:" + requestedRefreshRate);
        int displayID = this.mMotionDisplayId;
        DisplayManager displayManager = this.mDisplayManager;
        if (displayManager != null) {
            Display display = displayManager.getDisplay(displayID);
            if (display == null) {
                Slog.e(TAG, "display is null");
                return;
            }
            float curRef = display.getRefreshRate();
            Slog.d(TAG, "displayID:" + displayID + "  curRef:" + curRef + "  requestRef:" + requestedRefreshRate);
            if (requestedRefreshRate != curRef && (displayManagerInternal = this.mDisplayManagerInternal) != null) {
                displayManagerInternal.setDisplayProperties(displayID, true, requestedRefreshRate, 0, true);
            }
        }
    }

    public void updateChargeStateForSaleInternal(boolean enable) {
        if (!this.mIsSellModeVersion) {
            Slog.d(TAG, "Only support for sell mode version");
        } else if (!enable) {
            disableCharge();
        } else {
            enableCharge();
        }
    }

    private void disableCharge() {
        SystemProperties.set("vendor.oppo.engineermode.chargeswitch", TemperatureProvider.SWITCH_OFF);
        Slog.d(TAG, "disableCharge");
    }

    private void enableCharge() {
        SystemProperties.set("vendor.oppo.engineermode.chargeswitch", TemperatureProvider.SWITCH_ON);
        Slog.d(TAG, "enableCharge");
    }

    public boolean isSellModeVersion() {
        return this.mIsSellModeVersion;
    }

    public int getCurrentChargeStateForSaleInternal() {
        String tempString;
        StringBuilder sb;
        File file = new File("/sys/class/power_supply/battery/mmi_charging_enable");
        BufferedReader reader = null;
        try {
            if (file.exists()) {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                tempString = reader2.readLine();
                try {
                    reader2.close();
                } catch (IOException e) {
                    e1 = e;
                    sb = new StringBuilder();
                }
                if (tempString != null || tempString.trim().length() == 0) {
                    return -1;
                }
                try {
                    return Integer.valueOf(tempString).intValue();
                } catch (NumberFormatException e2) {
                    Slog.e(TAG, "readIntFromFile NumberFormatException:" + e2.getMessage());
                    return -1;
                }
            } else {
                Slog.e(TAG, "mmi_charging_enable is no existed");
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        Slog.e(TAG, "readIntFromFile io close exception :" + e1.getMessage());
                    }
                }
                return -2;
            }
            sb.append("readIntFromFile io close exception :");
            sb.append(e1.getMessage());
            Slog.e(TAG, sb.toString());
            if (tempString != null) {
            }
            return -1;
        } catch (IOException e3) {
            tempString = null;
            Slog.e(TAG, "readIntFromFile io exception:" + e3.getMessage());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    e1 = e4;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e12) {
                    Slog.e(TAG, "readIntFromFile io close exception :" + e12.getMessage());
                }
            }
            throw th;
        }
    }

    public void updateSettingsLocked() {
        int screenBrightnessModeSetting = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2);
        int i = this.mLastScreenBrightnessModeSetting;
        if (!(i == -1 || i == screenBrightnessModeSetting || screenBrightnessModeSetting != 0)) {
            mOppoBrightUtils.resetAIBrightness();
        }
        this.mLastScreenBrightnessModeSetting = screenBrightnessModeSetting;
    }
}
