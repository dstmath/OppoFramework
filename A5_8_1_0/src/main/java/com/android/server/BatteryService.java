package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.BatteryManagerInternal;
import android.os.BatteryProperties;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBatteryPropertiesListener.Stub;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.EventLog;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.DumpUtils;
import com.android.server.am.BatteryStatsService;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.coloros.OppoSysStateManagerInternal;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.storage.DeviceStorageMonitorService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public final class BatteryService extends SystemService {
    private static final int BATTERY_PLUGGED_NONE = 0;
    private static final int BATTERY_SCALE = 100;
    private static boolean DEBUG = false;
    private static boolean DEBUG_COMMAND = false;
    private static boolean DEBUG_PANIC = false;
    private static final String[] DUMPSYS_ARGS = new String[]{"--checkin", "--unplugged"};
    private static final String DUMPSYS_DATA_PATH = "/data/system/";
    static final int OPTION_FORCE_UPDATE = 1;
    private static final String TAG = BatteryService.class.getSimpleName();
    private static final String TAG_LED = "BatteryLed";
    private ActivityManagerInternal mActivityManagerInternal;
    private int mBatteryHwStatus;
    private boolean mBatteryLevelCritical;
    private boolean mBatteryLevelLow;
    private int mBatteryNotifyCode;
    private BatteryProperties mBatteryProps;
    private final IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private SettingsObserver mChargeSettingsObservers;
    private final Context mContext;
    private int mCriticalBatteryLevel;
    private int mDischargeStartLevel;
    private long mDischargeStartTime;
    private final Handler mHandler;
    private int mHwStatusIsSet;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.i(BatteryService.TAG, "action received : " + action);
            if (action.equals("android.intent.action.SCREEN_ON")) {
                if (BatteryService.this.mLed != null) {
                    BatteryService.this.mLed.handleScreenOn();
                }
            } else if (action.equals("android.intent.action.SCREEN_OFF") && BatteryService.this.mLed != null) {
                BatteryService.this.mLed.handleScreenOff();
            }
        }
    };
    private int mInvalidCharger;
    private boolean mIsSellModeVersion = false;
    private int mLastBatteryHealth;
    private int mLastBatteryHwStatus;
    private int mLastBatteryLevel;
    private boolean mLastBatteryLevelCritical;
    private int mLastBatteryNotifyCode;
    private boolean mLastBatteryPresent;
    private final BatteryProperties mLastBatteryProps = new BatteryProperties();
    private int mLastBatteryStatus;
    private int mLastBatteryTemperature;
    private int mLastBatteryVoltage;
    private int mLastChargeCounter;
    private int mLastInvalidCharger;
    private int mLastMaxChargingCurrent;
    private int mLastMaxChargingVoltage;
    private int mLastOtgOnline;
    private int mLastPlugType = -1;
    private Led mLed;
    private final Object mLock = new Object();
    private int mLowBatteryCloseWarningLevel;
    private int mLowBatteryWarningLevel;
    private SettingsObserver mLowPowerSettingsObservers;
    private OppoSysStateManagerInternal mOppoSysStateManagerInternal;
    private int mOtgOnline;
    private PackageManager mPackageManager = null;
    private int mPlugType;
    private boolean mSentLowBatteryBroadcast = false;
    private int mSequence = 1;
    private int mShutdownBatteryTemperature;
    private int mTempLastPlugType = -1;
    private boolean mUpdatesStopped;
    private final OppoBatteryService obs;

    private final class BatteryListener extends Stub {
        /* synthetic */ BatteryListener(BatteryService this$0, BatteryListener -this1) {
            this();
        }

        private BatteryListener() {
        }

        public void batteryPropertiesChanged(BatteryProperties props) {
            long identity = Binder.clearCallingIdentity();
            try {
                BatteryService.this.update(props);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private final class BinderService extends Binder {
        /* synthetic */ BinderService(BatteryService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(BatteryService.this.mContext, BatteryService.TAG, pw)) {
                if (args.length > 0 && "--proto".equals(args[0])) {
                    BatteryService.this.dumpProto(fd);
                } else if (!BatteryService.this.dynamicallyConfigBatteryServiceLogTag(fd, pw, args)) {
                    if (BatteryService.this.obs != null) {
                        boolean oUpdate = BatteryService.this.obs.dumpAddition(fd, pw, args);
                    }
                    BatteryService.this.dumpInternal(fd, pw, args);
                }
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, callback, resultReceiver);
        }
    }

    private final class Led {
        private static final int DELAY_UPDATE_LIGHT = 500;
        private static final int MSG_UPDATE_LIGHT = 1;
        private final int mBatteryFullARGB;
        private final int mBatteryLedOff;
        private final int mBatteryLedOn;
        private final Light mBatteryLight;
        private final int mBatteryLowARGB;
        public boolean mBatteryLowHint;
        private final int mBatteryMediumARGB;
        public boolean mChargingHint;
        private final LightsManager mLight;
        private Handler mLightHandler = new Handler() {
            public void handleMessage(Message msg) {
                synchronized (BatteryService.this.mLock) {
                    Led.this.updateLightsLocked();
                }
            }
        };
        private final Light mNotificationLightStateDetector;
        private boolean mScreenOn;

        public Led(Context context, LightsManager lights) {
            this.mBatteryLight = lights.getLight(3);
            this.mBatteryLowARGB = context.getResources().getInteger(17694831);
            this.mBatteryMediumARGB = context.getResources().getInteger(17694832);
            this.mBatteryFullARGB = context.getResources().getInteger(17694828);
            this.mBatteryLedOn = context.getResources().getInteger(17694830);
            this.mBatteryLedOff = context.getResources().getInteger(17694829);
            this.mLight = lights;
            this.mNotificationLightStateDetector = lights.getLight(4);
            Handler handler = new Handler();
            BatteryService.this.mLowPowerSettingsObservers = new SettingsObserver(handler);
            BatteryService.this.mChargeSettingsObservers = new SettingsObserver(handler);
            this.mScreenOn = true;
        }

        public void updateLightsLocked() {
            if (BatteryService.DEBUG) {
                Slog.d(BatteryService.TAG_LED, "ScreenOn = " + this.mScreenOn + ";mBatteryLowHint = " + this.mBatteryLowHint + ";mChargingHint = " + this.mChargingHint);
            }
            if (this.mScreenOn) {
                this.mBatteryLight.turnOff();
            } else if (!this.mLight.getLightState(4)) {
                if (BatteryService.this.mBatteryProps == null) {
                    if (BatteryService.DEBUG) {
                        Slog.d(BatteryService.TAG_LED, "mBatteryProps is null!!!");
                    }
                    return;
                }
                int level = BatteryService.this.mBatteryProps.batteryLevel;
                int status = BatteryService.this.mBatteryProps.batteryStatus;
                if (status == 2) {
                    if (!this.mChargingHint) {
                        this.mBatteryLight.turnOff();
                    } else if (level < 100) {
                        this.mBatteryLight.setFlashing(this.mBatteryMediumARGB, 1, this.mBatteryLedOn, this.mBatteryLedOff);
                    } else {
                        this.mBatteryLight.setColor(this.mBatteryFullARGB);
                    }
                } else if (status != 5 || level < 100) {
                    if (!this.mBatteryLowHint || level >= BatteryService.this.mLowBatteryWarningLevel) {
                        this.mBatteryLight.turnOff();
                    } else {
                        this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, this.mBatteryLedOn, this.mBatteryLedOff);
                    }
                } else if (this.mChargingHint) {
                    this.mBatteryLight.setColor(this.mBatteryFullARGB);
                } else {
                    this.mBatteryLight.turnOff();
                }
            }
        }

        public void turnOffBatteryLights() {
            if (this.mBatteryLight != null) {
                this.mBatteryLight.turnOff();
            }
        }

        public void handleScreenOn() {
            this.mScreenOn = true;
            this.mLightHandler.removeMessages(1);
            BatteryService.this.mLowPowerSettingsObservers.update(null);
            BatteryService.this.mChargeSettingsObservers.update(null);
            this.mBatteryLight.turnOff();
        }

        public void handleScreenOff() {
            this.mScreenOn = false;
            BatteryService.this.mLowPowerSettingsObservers.update(null);
            BatteryService.this.mChargeSettingsObservers.update(null);
            this.mLightHandler.sendEmptyMessageDelayed(1, 500);
        }
    }

    private final class LocalService extends BatteryManagerInternal {
        /* synthetic */ LocalService(BatteryService this$0, LocalService -this1) {
            this();
        }

        private LocalService() {
        }

        public boolean isPowered(int plugTypeSet) {
            boolean -wrap0;
            synchronized (BatteryService.this.mLock) {
                -wrap0 = BatteryService.this.isPoweredLocked(plugTypeSet);
            }
            return -wrap0;
        }

        public int getPlugType() {
            int -get14;
            synchronized (BatteryService.this.mLock) {
                -get14 = BatteryService.this.mPlugType;
            }
            return -get14;
        }

        public int getBatteryLevel() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mBatteryProps.batteryLevel;
            }
            return i;
        }

        public boolean getBatteryLevelLow() {
            boolean -get5;
            synchronized (BatteryService.this.mLock) {
                -get5 = BatteryService.this.mBatteryLevelLow;
            }
            return -get5;
        }

        public int getInvalidCharger() {
            int -get9;
            synchronized (BatteryService.this.mLock) {
                -get9 = BatteryService.this.mInvalidCharger;
            }
            return -get9;
        }

        public int getBatteryTemperature() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mBatteryProps.batteryTemperature;
            }
            return i;
        }
    }

    class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = BatteryService.this.mContext.getContentResolver();
            resolver.registerContentObserver(System.getUriFor("oppo_breath_led_low_power"), false, this);
            resolver.registerContentObserver(System.getUriFor("oppo_breath_led_charge"), false, this);
            update(null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver resolver = BatteryService.this.mContext.getContentResolver();
            if (uri == null || "oppo_breath_led_low_power".equals(uri)) {
                boolean lowePowerEnabled = System.getInt(resolver, "oppo_breath_led_low_power", 0) != 0;
                if (BatteryService.this.mLed.mBatteryLowHint != lowePowerEnabled) {
                    BatteryService.this.mLed.mBatteryLowHint = lowePowerEnabled;
                    synchronized (BatteryService.this.mLock) {
                        BatteryService.this.mLed.updateLightsLocked();
                    }
                }
            }
            if (uri == null || "oppo_breath_led_charge".equals(uri)) {
                boolean chargeEnabled = System.getInt(resolver, "oppo_breath_led_charge", 0) != 0;
                if (BatteryService.this.mLed.mChargingHint != chargeEnabled) {
                    BatteryService.this.mLed.mChargingHint = chargeEnabled;
                    synchronized (BatteryService.this.mLock) {
                        BatteryService.this.mLed.updateLightsLocked();
                    }
                }
            }
        }
    }

    class Shell extends ShellCommand {
        Shell() {
        }

        public int onCommand(String cmd) {
            return BatteryService.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            BatteryService.dumpHelp(getOutPrintWriter());
        }
    }

    public BatteryService(Context context) {
        super(context);
        this.mContext = context;
        this.mHandler = new Handler(true);
        this.mLed = new Led(context, (LightsManager) getLocalService(LightsManager.class));
        this.mBatteryStats = BatteryStatsService.getService();
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.obs = new OppoBatteryService(context);
        try {
            this.mHwStatusIsSet = Integer.parseInt(SystemProperties.get("sys.hw_status"));
        } catch (NumberFormatException e) {
            Slog.i(TAG, "mHwStatusIsSet ", e);
        }
        this.mCriticalBatteryLevel = this.mContext.getResources().getInteger(17694756);
        this.mLowBatteryWarningLevel = this.mContext.getResources().getInteger(17694804);
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694803);
        this.mShutdownBatteryTemperature = this.mContext.getResources().getInteger(17694855);
        if (new File("/sys/devices/virtual/switch/invalid_charger/state").exists()) {
            new UEventObserver() {
                public void onUEvent(UEvent event) {
                    int invalidCharger = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(event.get("SWITCH_STATE")) ? 1 : 0;
                    synchronized (BatteryService.this.mLock) {
                        if (BatteryService.this.mInvalidCharger != invalidCharger) {
                            BatteryService.this.mInvalidCharger = invalidCharger;
                        }
                    }
                }
            }.startObserving("DEVPATH=/devices/virtual/switch/invalid_charger");
        }
        OppoSysStateManager.getInstance();
        this.mOppoSysStateManagerInternal = (OppoSysStateManagerInternal) LocalServices.getService(OppoSysStateManagerInternal.class);
    }

    public void onStart() {
        IBinder b = ServiceManager.getService("batteryproperties");
        if (IBatteryPropertiesRegistrar.Stub.asInterface(b) == null) {
            SystemProperties.set("ctl.restart", "healthd");
        }
        IBatteryPropertiesRegistrar batteryPropertiesRegistrar = IBatteryPropertiesRegistrar.Stub.asInterface(b);
        if (batteryPropertiesRegistrar != null) {
            try {
                batteryPropertiesRegistrar.registerListener(new BatteryListener(this, null));
            } catch (RemoteException e) {
            }
        }
        this.mBinderService = new BinderService(this, null);
        publishBinderService("battery", this.mBinderService);
        publishLocalService(BatteryManagerInternal.class, new LocalService(this, null));
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        if (this.mPackageManager == null) {
            this.mPackageManager = this.mContext.getPackageManager();
        }
        if (this.mPackageManager != null) {
            this.mIsSellModeVersion = this.mPackageManager.hasSystemFeature("oppo.specialversion.exp.sellmode");
        }
    }

    public void onBootPhase(int phase) {
        Object obj;
        if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
            obj = this.mLock;
            synchronized (obj) {
                this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("low_power_trigger_level"), false, new ContentObserver(this.mHandler) {
                    public void onChange(boolean selfChange) {
                        synchronized (BatteryService.this.mLock) {
                            BatteryService.this.updateBatteryWarningLevelLocked();
                        }
                    }
                }, -1);
                updateBatteryWarningLevelLocked();
            }
        } else if (phase == 500) {
            this.mLowPowerSettingsObservers.observe();
            this.mChargeSettingsObservers.observe();
            obj = this.mLock;
            synchronized (obj) {
                this.mLed.updateLightsLocked();
            }
        } else {
            return;
        }
    }

    private void updateBatteryWarningLevelLocked() {
        ContentResolver resolver = this.mContext.getContentResolver();
        int defWarnLevel = this.mContext.getResources().getInteger(17694804);
        this.mLowBatteryWarningLevel = Global.getInt(resolver, "low_power_trigger_level", defWarnLevel);
        if (this.mLowBatteryWarningLevel == 0) {
            this.mLowBatteryWarningLevel = defWarnLevel;
        }
        if (this.mLowBatteryWarningLevel < this.mCriticalBatteryLevel) {
            this.mLowBatteryWarningLevel = this.mCriticalBatteryLevel;
        }
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694803);
        processValuesLocked(true);
    }

    private boolean isPoweredLocked(int plugTypeSet) {
        if (this.mBatteryProps.batteryStatus == 1) {
            return true;
        }
        if ((plugTypeSet & 1) != 0 && this.mBatteryProps.chargerAcOnline) {
            return true;
        }
        if ((plugTypeSet & 2) == 0 || !this.mBatteryProps.chargerUsbOnline) {
            return (plugTypeSet & 4) != 0 && this.mBatteryProps.chargerWirelessOnline;
        } else {
            return true;
        }
    }

    private boolean shouldSendBatteryLowLocked() {
        boolean plugged = this.mPlugType != 0;
        boolean oldPlugged = this.mLastPlugType != 0;
        if (plugged || this.mBatteryProps.batteryStatus == 1 || this.mBatteryProps.batteryLevel > this.mLowBatteryWarningLevel) {
            return false;
        }
        if (oldPlugged || this.mLastBatteryLevel > this.mLowBatteryWarningLevel) {
            return true;
        }
        return false;
    }

    private void shutdownIfNoPowerLocked() {
        if (this.mBatteryProps.batteryLevel == 0 && (isPoweredLocked(7) ^ 1) != 0) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (BatteryService.this.mActivityManagerInternal.isSystemReady()) {
                        Slog.v(BatteryService.TAG, "mBatteryProps.batteryLevel = " + BatteryService.this.mBatteryProps.batteryLevel + "shutdown because of low power");
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_LOW_BATTERY_POWER_OFF, new String[0]);
                        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                        intent.setFlags(268435456);
                        BatteryService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                        BatteryService.this.mLed.turnOffBatteryLights();
                    }
                }
            });
        }
    }

    private void shutdownIfOverTempLocked() {
    }

    private void update(BatteryProperties props) {
        synchronized (this.mLock) {
            if (DEBUG_COMMAND) {
                Slog.v(TAG, "update mUpdatesStopped = " + this.mUpdatesStopped);
            }
            if (this.mUpdatesStopped) {
                this.mLastBatteryProps.set(props);
            } else {
                this.mBatteryProps = props;
                if (this.obs != null) {
                    this.obs.native_update();
                    this.mOtgOnline = this.obs.mOtgOnline;
                    this.mBatteryNotifyCode = this.obs.mBatteryNotifyCode;
                }
                if (this.obs != null) {
                    this.mBatteryHwStatus = this.obs.mBatteryHwStatus;
                }
                processValuesLocked(false);
            }
        }
    }

    private void processValuesLocked(boolean force) {
        Intent intent;
        final Intent intent2;
        boolean logOutlier = false;
        long dischargeDuration = 0;
        this.mBatteryLevelCritical = this.mBatteryProps.batteryLevel <= this.mCriticalBatteryLevel;
        if (this.mBatteryProps.chargerAcOnline) {
            this.mPlugType = 1;
        } else if (this.mBatteryProps.chargerUsbOnline) {
            this.mPlugType = 2;
        } else if (this.mBatteryProps.chargerWirelessOnline) {
            this.mPlugType = 4;
        } else {
            this.mPlugType = 0;
        }
        if (this.mIsSellModeVersion && this.mPlugType != 0 && getCurrentChargeStateForSaleInternal() == 0) {
            this.mBatteryProps.batteryStatus = 2;
            if (DEBUG_PANIC) {
                Slog.d(TAG, "SellMode version batteryStatus: " + this.mBatteryProps.batteryStatus + "  mPlugType:" + this.mPlugType);
            }
        }
        if (this.mPlugType != this.mTempLastPlugType) {
            intent = new Intent("oppo.intent.action.BATTERY_PLUGGED_CHANGED");
            intent.addFlags(536870912);
            intent.putExtra("plugged", this.mPlugType);
            if (this.mActivityManagerInternal.isSystemReady()) {
                this.mTempLastPlugType = this.mPlugType;
                intent2 = intent;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        if (BatteryService.DEBUG_PANIC) {
                            Slog.d(BatteryService.TAG, "send broadcast : oppo.intent.action.BATTERY_PLUGGED_CHANGED : mPlugType = " + BatteryService.this.mTempLastPlugType);
                        }
                        ActivityManager.broadcastStickyIntent(intent2, -1);
                    }
                });
            }
        }
        if (!(this.mBatteryHwStatus == this.mLastBatteryHwStatus && this.mHwStatusIsSet == this.mBatteryHwStatus)) {
            try {
                this.mHwStatusIsSet = Integer.parseInt(SystemProperties.get("sys.hw_status"));
            } catch (NumberFormatException e) {
                Slog.i(TAG, "mHwStatusIsSet ", e);
            }
            this.mLastBatteryHwStatus = this.mBatteryHwStatus;
            final Intent hw_status_intent = new Intent("oppo.intent.action.BATTERY_HW_STATUS");
            hw_status_intent.putExtra("hw_status_extra", this.mBatteryHwStatus);
            if (this.mActivityManagerInternal.isSystemReady()) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        if (BatteryService.DEBUG_PANIC) {
                            Slog.d(BatteryService.TAG, "send broadcast : oppo.intent.action.BATTERY_HW_STATUS : mBatteryHwStatus" + BatteryService.this.mBatteryHwStatus);
                        }
                        ActivityManager.broadcastStickyIntent(hw_status_intent, -1);
                    }
                });
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "Processing new values: chargerAcOnline=" + this.mBatteryProps.chargerAcOnline + ", chargerUsbOnline=" + this.mBatteryProps.chargerUsbOnline + ", chargerWirelessOnline=" + this.mBatteryProps.chargerWirelessOnline + ", maxChargingCurrent" + this.mBatteryProps.maxChargingCurrent + ", maxChargingVoltage" + this.mBatteryProps.maxChargingVoltage + ", batteryStatus=" + this.mBatteryProps.batteryStatus + ", batteryHealth=" + this.mBatteryProps.batteryHealth + ", batteryPresent=" + this.mBatteryProps.batteryPresent + ", batteryLevel=" + this.mBatteryProps.batteryLevel + ", batteryTechnology=" + this.mBatteryProps.batteryTechnology + ", batteryVoltage=" + this.mBatteryProps.batteryVoltage + ", batteryChargeCounter=" + this.mBatteryProps.batteryChargeCounter + ", batteryFullCharge=" + this.mBatteryProps.batteryFullCharge + ", batteryTemperature=" + this.mBatteryProps.batteryTemperature + ", mBatteryLevelCritical=" + this.mBatteryLevelCritical + ", mPlugType=" + this.mPlugType);
        }
        try {
            this.mBatteryStats.setBatteryState(this.mBatteryProps.batteryStatus, this.mBatteryProps.batteryHealth, this.mPlugType, this.mBatteryProps.batteryLevel, this.mBatteryProps.batteryTemperature, this.mBatteryProps.batteryVoltage, this.mBatteryProps.batteryChargeCounter, this.mBatteryProps.batteryFullCharge);
        } catch (RemoteException e2) {
        }
        shutdownIfNoPowerLocked();
        shutdownIfOverTempLocked();
        if (this.obs != null) {
            this.obs.processAdditionalValuesLocked(this.mBatteryProps.batteryTemperature, this.mPlugType, this.mBatteryProps.batteryLevel);
        }
        if (force || this.mBatteryProps.batteryStatus != this.mLastBatteryStatus || this.mBatteryProps.batteryLevel != this.mLastBatteryLevel || this.mPlugType != this.mLastPlugType || this.mBatteryNotifyCode != this.mLastBatteryNotifyCode || this.mOtgOnline != this.mLastOtgOnline || this.mBatteryProps.batteryTemperature / 10 != this.mLastBatteryTemperature / 10 || this.mBatteryProps.batteryChargeCounter != this.mLastChargeCounter || this.mInvalidCharger != this.mLastInvalidCharger) {
            if (this.mPlugType != this.mLastPlugType) {
                if (this.mLastPlugType == 0) {
                    if (!(this.mDischargeStartTime == 0 || this.mDischargeStartLevel == this.mBatteryProps.batteryLevel)) {
                        dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
                        logOutlier = true;
                        EventLog.writeEvent(EventLogTags.BATTERY_DISCHARGE, new Object[]{Long.valueOf(dischargeDuration), Integer.valueOf(this.mDischargeStartLevel), Integer.valueOf(this.mBatteryProps.batteryLevel)});
                        this.mDischargeStartTime = 0;
                    }
                } else if (this.mPlugType == 0) {
                    this.mDischargeStartTime = SystemClock.elapsedRealtime();
                    this.mDischargeStartLevel = this.mBatteryProps.batteryLevel;
                }
                if (this.mOppoSysStateManagerInternal != null) {
                    this.mOppoSysStateManagerInternal.onPlugChanged(this.mPlugType);
                } else {
                    Slog.d(TAG, "mOppoSysStateManagerInternal is null!!!");
                }
            }
            if (!(this.mBatteryProps.batteryStatus == this.mLastBatteryStatus && this.mBatteryProps.batteryHealth == this.mLastBatteryHealth && this.mBatteryProps.batteryPresent == this.mLastBatteryPresent && this.mPlugType == this.mLastPlugType)) {
                Object[] objArr = new Object[5];
                objArr[0] = Integer.valueOf(this.mBatteryProps.batteryStatus);
                objArr[1] = Integer.valueOf(this.mBatteryProps.batteryHealth);
                objArr[2] = Integer.valueOf(this.mBatteryProps.batteryPresent ? 1 : 0);
                objArr[3] = Integer.valueOf(this.mPlugType);
                objArr[4] = this.mBatteryProps.batteryTechnology;
                EventLog.writeEvent(EventLogTags.BATTERY_STATUS, objArr);
            }
            if (this.mBatteryProps.batteryLevel != this.mLastBatteryLevel) {
                EventLog.writeEvent(EventLogTags.BATTERY_LEVEL, new Object[]{Integer.valueOf(this.mBatteryProps.batteryLevel), Integer.valueOf(this.mBatteryProps.batteryVoltage), Integer.valueOf(this.mBatteryProps.batteryTemperature)});
            }
            if (this.mBatteryLevelCritical && (this.mLastBatteryLevelCritical ^ 1) != 0 && this.mPlugType == 0) {
                dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
                logOutlier = true;
            }
            if (this.mBatteryLevelLow) {
                if (this.mPlugType != 0) {
                    this.mBatteryLevelLow = false;
                } else if (this.mBatteryProps.batteryLevel >= this.mLowBatteryCloseWarningLevel) {
                    this.mBatteryLevelLow = false;
                } else if (force && this.mBatteryProps.batteryLevel >= this.mLowBatteryWarningLevel) {
                    this.mBatteryLevelLow = false;
                }
            } else if (this.mPlugType == 0 && this.mBatteryProps.batteryLevel <= this.mLowBatteryWarningLevel) {
                this.mBatteryLevelLow = true;
            }
            this.mSequence++;
            if (this.mPlugType != 0 && this.mLastPlugType == 0) {
                intent = new Intent("android.intent.action.ACTION_POWER_CONNECTED");
                intent.setFlags(67108864);
                intent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                intent.addFlags(DumpState.DUMP_DEXOPT);
                intent2 = intent;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(intent2, UserHandle.ALL);
                    }
                });
            } else if (this.mPlugType == 0 && this.mLastPlugType != 0) {
                intent = new Intent("android.intent.action.ACTION_POWER_DISCONNECTED");
                intent.setFlags(67108864);
                intent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                intent.addFlags(DumpState.DUMP_DEXOPT);
                intent2 = intent;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(intent2, UserHandle.ALL);
                    }
                });
            }
            if (shouldSendBatteryLowLocked()) {
                this.mSentLowBatteryBroadcast = true;
                intent = new Intent("android.intent.action.BATTERY_LOW");
                intent.setFlags(67108864);
                intent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                intent2 = intent;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(intent2, UserHandle.ALL);
                    }
                });
            } else if (this.mSentLowBatteryBroadcast && this.mBatteryProps.batteryLevel >= this.mLowBatteryCloseWarningLevel) {
                this.mSentLowBatteryBroadcast = false;
                intent = new Intent("android.intent.action.BATTERY_OKAY");
                intent.setFlags(67108864);
                intent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                intent2 = intent;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(intent2, UserHandle.ALL);
                    }
                });
            }
            sendIntentLocked();
            this.mLed.updateLightsLocked();
            if (logOutlier && dischargeDuration != 0) {
                logOutlierLocked(dischargeDuration);
            }
            this.mLastBatteryStatus = this.mBatteryProps.batteryStatus;
            this.mLastBatteryHealth = this.mBatteryProps.batteryHealth;
            this.mLastBatteryPresent = this.mBatteryProps.batteryPresent;
            this.mLastBatteryLevel = this.mBatteryProps.batteryLevel;
            this.mLastPlugType = this.mPlugType;
            this.mLastBatteryVoltage = this.mBatteryProps.batteryVoltage;
            this.mLastBatteryTemperature = this.mBatteryProps.batteryTemperature;
            this.mLastMaxChargingCurrent = this.mBatteryProps.maxChargingCurrent;
            this.mLastMaxChargingVoltage = this.mBatteryProps.maxChargingVoltage;
            this.mLastChargeCounter = this.mBatteryProps.batteryChargeCounter;
            this.mLastBatteryLevelCritical = this.mBatteryLevelCritical;
            this.mLastInvalidCharger = this.mInvalidCharger;
            this.mLastBatteryNotifyCode = this.mBatteryNotifyCode;
            this.mLastOtgOnline = this.mOtgOnline;
        }
    }

    private void sendIntentLocked() {
        final Intent intent = new Intent("android.intent.action.BATTERY_CHANGED");
        intent.addFlags(1610612736);
        int icon = getIconLocked(this.mBatteryProps.batteryLevel);
        intent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
        intent.putExtra("status", this.mBatteryProps.batteryStatus);
        intent.putExtra("health", this.mBatteryProps.batteryHealth);
        intent.putExtra("present", this.mBatteryProps.batteryPresent);
        intent.putExtra("level", this.mBatteryProps.batteryLevel);
        intent.putExtra("scale", 100);
        intent.putExtra("icon-small", icon);
        intent.putExtra("plugged", this.mPlugType);
        intent.putExtra("voltage", this.mBatteryProps.batteryVoltage);
        intent.putExtra("temperature", this.mBatteryProps.batteryTemperature);
        intent.putExtra("technology", this.mBatteryProps.batteryTechnology);
        intent.putExtra("invalid_charger", this.mInvalidCharger);
        intent.putExtra("max_charging_current", this.mBatteryProps.maxChargingCurrent);
        intent.putExtra("max_charging_voltage", this.mBatteryProps.maxChargingVoltage);
        intent.putExtra("charge_counter", this.mBatteryProps.batteryChargeCounter);
        intent.putExtra("notifycode", this.mBatteryNotifyCode);
        if (DEBUG) {
            Slog.d(TAG, "Sending ACTION_BATTERY_CHANGED.  level:" + this.mBatteryProps.batteryLevel + ", scale:" + 100 + ", status:" + this.mBatteryProps.batteryStatus + ", health:" + this.mBatteryProps.batteryHealth + ", present:" + this.mBatteryProps.batteryPresent + ", voltage: " + this.mBatteryProps.batteryVoltage + ", temperature: " + this.mBatteryProps.batteryTemperature + ", technology: " + this.mBatteryProps.batteryTechnology + ", AC powered:" + this.mBatteryProps.chargerAcOnline + ", USB powered:" + this.mBatteryProps.chargerUsbOnline + ", Wireless powered:" + this.mBatteryProps.chargerWirelessOnline + ", notify code:" + this.mBatteryNotifyCode + ", otg online:" + this.mOtgOnline + ", icon:" + icon + ", invalid charger:" + this.mInvalidCharger + ", maxChargingCurrent:" + this.mBatteryProps.maxChargingCurrent + ", maxChargingVoltage:" + this.mBatteryProps.maxChargingVoltage + ", chargeCounter:" + this.mBatteryProps.batteryChargeCounter);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                ActivityManager.broadcastStickyIntent(intent, -1);
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c9 A:{SYNTHETIC, Splitter: B:42:0x00c9} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0089 A:{SYNTHETIC, Splitter: B:30:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c9 A:{SYNTHETIC, Splitter: B:42:0x00c9} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0089 A:{SYNTHETIC, Splitter: B:30:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0101 A:{SYNTHETIC, Splitter: B:52:0x0101} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0101 A:{SYNTHETIC, Splitter: B:52:0x0101} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void logBatteryStatsLocked() {
        RemoteException e;
        IOException e2;
        Throwable th;
        IBinder batteryInfoService = ServiceManager.getService("batterystats");
        if (batteryInfoService != null) {
            DropBoxManager db = (DropBoxManager) this.mContext.getSystemService("dropbox");
            if (db != null && (db.isTagEnabled("BATTERY_DISCHARGE_INFO") ^ 1) == 0) {
                File dumpFile = null;
                FileOutputStream dumpStream = null;
                try {
                    FileOutputStream dumpStream2;
                    File dumpFile2 = new File("/data/system/batterystats.dump");
                    try {
                        dumpStream2 = new FileOutputStream(dumpFile2);
                    } catch (RemoteException e3) {
                        e = e3;
                        dumpFile = dumpFile2;
                        Slog.e(TAG, "failed to dump battery service", e);
                        if (dumpStream != null) {
                            try {
                                dumpStream.close();
                            } catch (IOException e4) {
                                Slog.e(TAG, "failed to close dumpsys output stream");
                            }
                        }
                        if (!(dumpFile == null || (dumpFile.delete() ^ 1) == 0)) {
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                        }
                    } catch (IOException e5) {
                        e2 = e5;
                        dumpFile = dumpFile2;
                        try {
                            Slog.e(TAG, "failed to write dumpsys file", e2);
                            if (dumpStream != null) {
                                try {
                                    dumpStream.close();
                                } catch (IOException e6) {
                                    Slog.e(TAG, "failed to close dumpsys output stream");
                                }
                            }
                            if (!(dumpFile == null || (dumpFile.delete() ^ 1) == 0)) {
                                Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (dumpStream != null) {
                            }
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        dumpFile = dumpFile2;
                        if (dumpStream != null) {
                            try {
                                dumpStream.close();
                            } catch (IOException e7) {
                                Slog.e(TAG, "failed to close dumpsys output stream");
                            }
                        }
                        if (!(dumpFile == null || (dumpFile.delete() ^ 1) == 0)) {
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                        }
                        throw th;
                    }
                    try {
                        batteryInfoService.dump(dumpStream2.getFD(), DUMPSYS_ARGS);
                        FileUtils.sync(dumpStream2);
                        db.addFile("BATTERY_DISCHARGE_INFO", dumpFile2, 2);
                        if (dumpStream2 != null) {
                            try {
                                dumpStream2.close();
                            } catch (IOException e8) {
                                Slog.e(TAG, "failed to close dumpsys output stream");
                            }
                        }
                        if (!(dumpFile2 == null || (dumpFile2.delete() ^ 1) == 0)) {
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile2.getAbsolutePath());
                        }
                        dumpFile = dumpFile2;
                    } catch (RemoteException e9) {
                        e = e9;
                        dumpStream = dumpStream2;
                        dumpFile = dumpFile2;
                        Slog.e(TAG, "failed to dump battery service", e);
                        if (dumpStream != null) {
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                    } catch (IOException e10) {
                        e2 = e10;
                        dumpStream = dumpStream2;
                        dumpFile = dumpFile2;
                        Slog.e(TAG, "failed to write dumpsys file", e2);
                        if (dumpStream != null) {
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                    } catch (Throwable th4) {
                        th = th4;
                        dumpStream = dumpStream2;
                        dumpFile = dumpFile2;
                        if (dumpStream != null) {
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                        throw th;
                    }
                } catch (RemoteException e11) {
                    e = e11;
                    Slog.e(TAG, "failed to dump battery service", e);
                    if (dumpStream != null) {
                    }
                    Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                } catch (IOException e12) {
                    e2 = e12;
                    Slog.e(TAG, "failed to write dumpsys file", e2);
                    if (dumpStream != null) {
                    }
                    Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                }
            }
        }
    }

    private void logOutlierLocked(long duration) {
        ContentResolver cr = this.mContext.getContentResolver();
        String dischargeThresholdString = Global.getString(cr, "battery_discharge_threshold");
        String durationThresholdString = Global.getString(cr, "battery_discharge_duration_threshold");
        if (dischargeThresholdString != null && durationThresholdString != null) {
            try {
                long durationThreshold = Long.parseLong(durationThresholdString);
                int dischargeThreshold = Integer.parseInt(dischargeThresholdString);
                if (duration <= durationThreshold && this.mDischargeStartLevel - this.mBatteryProps.batteryLevel >= dischargeThreshold) {
                    logBatteryStatsLocked();
                }
                if (DEBUG) {
                    Slog.v(TAG, "duration threshold: " + durationThreshold + " discharge threshold: " + dischargeThreshold);
                }
                if (DEBUG) {
                    Slog.v(TAG, "duration: " + duration + " discharge: " + (this.mDischargeStartLevel - this.mBatteryProps.batteryLevel));
                }
            } catch (NumberFormatException e) {
                Slog.e(TAG, "Invalid DischargeThresholds GService string: " + durationThresholdString + " or " + dischargeThresholdString);
            }
        }
    }

    private int getIconLocked(int level) {
        if (this.mBatteryProps.batteryStatus == 2) {
            return 17303444;
        }
        if (this.mBatteryProps.batteryStatus == 3) {
            return 17303430;
        }
        if (this.mBatteryProps.batteryStatus == 4 || this.mBatteryProps.batteryStatus == 5) {
            return (!isPoweredLocked(7) || this.mBatteryProps.batteryLevel < 100) ? 17303430 : 17303444;
        } else {
            return 17303458;
        }
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Battery service (battery) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  set [-f] [ac|usb|wireless|status|level|temp|present|invalid] <value>");
        pw.println("    Force a battery property value, freezing battery state.");
        pw.println("    -f: force a battery change broadcast be sent, prints new sequence.");
        pw.println("  unplug [-f]");
        pw.println("    Force battery unplugged, freezing battery state.");
        pw.println("    -f: force a battery change broadcast be sent, prints new sequence.");
        pw.println("  reset [-f]");
        pw.println("    Unfreeze battery state, returning to current hardware values.");
        pw.println("    -f: force a battery change broadcast be sent, prints new sequence.");
    }

    int parseOptions(Shell shell) {
        int opts = 0;
        while (true) {
            String opt = shell.getNextOption();
            if (opt == null) {
                return opts;
            }
            if ("-f".equals(opt)) {
                opts |= 1;
            }
        }
    }

    int onShellCommand(Shell shell, String cmd) {
        long ident;
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        int opts;
        if (cmd.equals("unplug")) {
            opts = parseOptions(shell);
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            if (!this.mUpdatesStopped) {
                this.mLastBatteryProps.set(this.mBatteryProps);
            }
            this.mBatteryProps.chargerAcOnline = false;
            this.mBatteryProps.chargerUsbOnline = false;
            this.mBatteryProps.chargerWirelessOnline = false;
            ident = Binder.clearCallingIdentity();
            try {
                this.mUpdatesStopped = true;
                processValuesFromShellLocked(pw, opts);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else if (cmd.equals("set")) {
            opts = parseOptions(shell);
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            String key = shell.getNextArg();
            if (key == null) {
                pw.println("No property specified");
                return -1;
            }
            String value = shell.getNextArg();
            if (value == null) {
                pw.println("No value specified");
                return -1;
            }
            try {
                if (!this.mUpdatesStopped) {
                    this.mLastBatteryProps.set(this.mBatteryProps);
                }
                boolean update = true;
                BatteryProperties batteryProperties;
                boolean z;
                if (key.equals("present")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    batteryProperties.batteryPresent = z;
                } else if (key.equals("ac")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    batteryProperties.chargerAcOnline = z;
                } else if (key.equals("usb")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    batteryProperties.chargerUsbOnline = z;
                } else if (key.equals("wireless")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    batteryProperties.chargerWirelessOnline = z;
                } else if (key.equals("status")) {
                    this.mBatteryProps.batteryStatus = Integer.parseInt(value);
                } else if (key.equals("level")) {
                    this.mBatteryProps.batteryLevel = Integer.parseInt(value);
                } else if (key.equals("temp")) {
                    this.mBatteryProps.batteryTemperature = Integer.parseInt(value);
                } else if (key.equals("invalid")) {
                    this.mInvalidCharger = Integer.parseInt(value);
                } else {
                    pw.println("Unknown set option: " + key);
                    update = false;
                }
                if (update) {
                    ident = Binder.clearCallingIdentity();
                    this.mUpdatesStopped = true;
                    processValuesFromShellLocked(pw, opts);
                    Binder.restoreCallingIdentity(ident);
                }
            } catch (NumberFormatException e) {
                pw.println("Bad value: " + value);
                return -1;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else if (!cmd.equals("reset")) {
            return shell.handleDefaultCommands(cmd);
        } else {
            opts = parseOptions(shell);
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            ident = Binder.clearCallingIdentity();
            try {
                if (this.mUpdatesStopped) {
                    this.mUpdatesStopped = false;
                    this.mBatteryProps.set(this.mLastBatteryProps);
                    processValuesFromShellLocked(pw, opts);
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        DEBUG_COMMAND = this.mUpdatesStopped;
        return 0;
    }

    private void processValuesFromShellLocked(PrintWriter pw, int opts) {
        boolean z = false;
        if ((opts & 1) != 0) {
            z = true;
        }
        processValuesLocked(z);
        if ((opts & 1) != 0) {
            pw.println(this.mSequence);
        }
    }

    private void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            if (args != null) {
                if (args.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (String str : args) {
                        sb.append(str);
                        sb.append("    ");
                    }
                    Slog.i(TAG, "dumpInternal args is : " + sb.toString().trim());
                }
            }
            if (!(args == null || args.length == 0)) {
                if (!"-a".equals(args[0])) {
                    new Shell().exec(this.mBinderService, null, fd, null, args, null, new ResultReceiver(null));
                }
            }
            pw.println("Current Battery Service state:");
            if (this.mUpdatesStopped) {
                pw.println("  (UPDATES STOPPED -- use 'reset' to restart)");
            }
            pw.println("  AC powered: " + this.mBatteryProps.chargerAcOnline);
            pw.println("  USB powered: " + this.mBatteryProps.chargerUsbOnline);
            pw.println("  Wireless powered: " + this.mBatteryProps.chargerWirelessOnline);
            pw.println("  Max charging current: " + this.mBatteryProps.maxChargingCurrent);
            pw.println("  Max charging voltage: " + this.mBatteryProps.maxChargingVoltage);
            pw.println("  Charge counter: " + this.mBatteryProps.batteryChargeCounter);
            pw.println("  status: " + this.mBatteryProps.batteryStatus);
            pw.println("  health: " + this.mBatteryProps.batteryHealth);
            pw.println("  present: " + this.mBatteryProps.batteryPresent);
            pw.println("  level: " + this.mBatteryProps.batteryLevel);
            pw.println("  scale: 100");
            pw.println("  voltage: " + this.mBatteryProps.batteryVoltage);
            pw.println("  temperature: " + this.mBatteryProps.batteryTemperature);
            pw.println("  technology: " + this.mBatteryProps.batteryTechnology);
            if (this.mIsSellModeVersion) {
                pw.println("  IsSellModeVersion: " + this.mIsSellModeVersion);
            }
        }
    }

    protected boolean dynamicallyConfigBatteryServiceLogTag(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (!"log".equals(args[0])) {
            return false;
        }
        if (args.length != 3) {
            pw.println("Invalid argument! Get detail help as bellow:");
            logOutBatteryServiceLogTagHelp(pw);
            return true;
        }
        pw.println("dynamicallyConfigBatteryServiceLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigBatteryServiceLogTag, args[" + index + "]:" + args[index]);
        }
        String logCategoryTag = args[1];
        boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
        pw.println("dynamicallyConfigBatteryServiceLogTag, logCategoryTag:" + logCategoryTag + ", on:" + on);
        if ("all".equals(logCategoryTag)) {
            DEBUG = on;
            DEBUG_PANIC = on;
            OppoBatteryService.DEBUG = on;
        } else {
            pw.println("Invalid log tag argument! Get detail help as bellow:");
            logOutBatteryServiceLogTagHelp(pw);
        }
        return true;
    }

    protected void logOutBatteryServiceLogTagHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 All BatteryService log");
        pw.println("cmd: dumpsys battery log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a4 A:{SYNTHETIC, Splitter: B:33:0x00a4} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getCurrentChargeStateForSaleInternal() {
        IOException e;
        Throwable th;
        File file = new File("/sys/class/power_supply/battery/mmi_charging_enable");
        BufferedReader reader = null;
        String tempString;
        int result;
        try {
            if (file.exists()) {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                try {
                    tempString = reader2.readLine();
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e1) {
                            Slog.e(TAG, "readIntFromFile io close exception :" + e1.getMessage());
                        }
                    }
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
                if (tempString != null || tempString.trim().length() == 0) {
                    result = -1;
                } else {
                    try {
                        result = Integer.valueOf(tempString).intValue();
                    } catch (NumberFormatException e3) {
                        result = -1;
                        Slog.e(TAG, "readIntFromFile NumberFormatException:" + e3.getMessage());
                    }
                }
                return result;
            }
            Slog.e(TAG, "mmi_charging_enable is no existed");
            return -2;
        } catch (IOException e4) {
            e = e4;
            tempString = null;
            try {
                Slog.e(TAG, "readIntFromFile io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "readIntFromFile io close exception :" + e12.getMessage());
                    }
                }
                if (tempString != null) {
                }
                result = -1;
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.e(TAG, "readIntFromFile io close exception :" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
    }

    private void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mLock) {
            proto.write(1155346202625L, this.mUpdatesStopped);
            int batteryPluggedValue = 0;
            if (this.mBatteryProps.chargerAcOnline) {
                batteryPluggedValue = 1;
            } else if (this.mBatteryProps.chargerUsbOnline) {
                batteryPluggedValue = 2;
            } else if (this.mBatteryProps.chargerWirelessOnline) {
                batteryPluggedValue = 4;
            }
            proto.write(1168231104514L, batteryPluggedValue);
            proto.write(1112396529667L, this.mBatteryProps.maxChargingCurrent);
            proto.write(1112396529668L, this.mBatteryProps.maxChargingVoltage);
            proto.write(1112396529669L, this.mBatteryProps.batteryChargeCounter);
            proto.write(1168231104518L, this.mBatteryProps.batteryStatus);
            proto.write(1168231104519L, this.mBatteryProps.batteryHealth);
            proto.write(1155346202632L, this.mBatteryProps.batteryPresent);
            proto.write(1112396529673L, this.mBatteryProps.batteryLevel);
            proto.write(1112396529674L, 100);
            proto.write(1112396529675L, this.mBatteryProps.batteryVoltage);
            proto.write(1112396529676L, this.mBatteryProps.batteryTemperature);
            proto.write(1159641169933L, this.mBatteryProps.batteryTechnology);
        }
        proto.flush();
    }
}
