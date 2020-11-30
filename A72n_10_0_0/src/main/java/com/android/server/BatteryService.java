package com.android.server;

import android.app.ActivityManagerInternal;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.hardware.health.V1_0.HealthInfo;
import android.hardware.health.V2_0.IHealth;
import android.hardware.health.V2_0.IHealthInfoCallback;
import android.hardware.health.V2_0.Result;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.metrics.LogMaker;
import android.os.BatteryManagerInternal;
import android.os.BatteryProperty;
import android.os.Binder;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.EventLog;
import android.util.MutableInt;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.DumpUtils;
import com.android.server.BatteryService;
import com.android.server.am.BatteryStatsService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.pm.DumpState;
import com.android.server.storage.DeviceStorageMonitorService;
import com.android.server.theia.NoFocusWindow;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class BatteryService extends OppoBaseBatteryService {
    private static final String ACTION_OPPO_USB_IMPEDANCE_SATUS = "oppo.intent.action.USB_IMPEDANCE_STATUS";
    private static final long BATTERY_LEVEL_CHANGE_THROTTLE_MS = 60000;
    private static final int BATTERY_PLUGGED_NONE = 0;
    private static final int BATTERY_SCALE = 100;
    static boolean DEBUG = false;
    private static final String[] DUMPSYS_ARGS = {"--checkin", "--unplugged"};
    private static final String DUMPSYS_DATA_PATH = "/data/system/";
    private static final long HEALTH_HAL_WAIT_MS = 1000;
    private static final int MAX_BATTERY_LEVELS_QUEUE_SIZE = 100;
    static final int OPTION_FORCE_UPDATE = 1;
    private static final String TAG = BatteryService.class.getSimpleName();
    private ActivityManagerInternal mActivityManagerInternal;
    private boolean mBatteryLevelCritical;
    private boolean mBatteryLevelLow;
    private ArrayDeque<Bundle> mBatteryLevelsEventQueue;
    private BatteryPropertiesRegistrar mBatteryPropertiesRegistrar;
    private final IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private int mChargeStartLevel;
    private long mChargeStartTime;
    private final Context mContext;
    private int mCriticalBatteryLevel;
    private int mDefaultUsbImpedance = -1;
    private int mDischargeStartLevel;
    private long mDischargeStartTime;
    private final Handler mHandler;
    private HealthHalCallback mHealthHalCallback;
    private HealthInfo mHealthInfo;
    private HealthServiceWrapper mHealthServiceWrapper;
    private int mInvalidCharger;
    private int mLastBatteryHealth;
    private int mLastBatteryLevel;
    private long mLastBatteryLevelChangedSentMs;
    private boolean mLastBatteryLevelCritical;
    private boolean mLastBatteryPresent;
    private int mLastBatteryStatus;
    private int mLastBatteryTemperature;
    private int mLastBatteryVoltage;
    private int mLastChargeCounter;
    private final HealthInfo mLastHealthInfo = new HealthInfo();
    private int mLastInvalidCharger;
    private int mLastMaxChargingCurrent;
    private int mLastMaxChargingVoltage;
    private int mLastPlugType = -1;
    private int mLastUsbImpedance;
    private Led mLed;
    private final Object mLock = new Object();
    private int mLowBatteryCloseWarningLevel;
    private int mLowBatteryWarningLevel;
    private MetricsLogger mMetricsLogger;
    private int mPlugType;
    private boolean mSentLowBatteryBroadcast = false;
    private int mSequence = 1;
    private int mShutdownBatteryTemperature;
    private boolean mUpdatesStopped;
    private int mUsbImpedance;
    private int mUsbStatus;

    public BatteryService(Context context) {
        super(context);
        this.mContext = context;
        this.mHandler = new Handler(true);
        this.mLed = new Led(context, (LightsManager) getLocalService(LightsManager.class));
        this.mBatteryStats = BatteryStatsService.getService();
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mCriticalBatteryLevel = this.mContext.getResources().getInteger(17694763);
        this.mLowBatteryWarningLevel = this.mContext.getResources().getInteger(17694829);
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694828);
        this.mShutdownBatteryTemperature = this.mContext.getResources().getInteger(17694894);
        this.mBatteryLevelsEventQueue = new ArrayDeque<>();
        this.mMetricsLogger = new MetricsLogger();
        if (new File("/sys/devices/virtual/switch/invalid_charger/state").exists()) {
            new UEventObserver() {
                /* class com.android.server.BatteryService.AnonymousClass1 */

                public void onUEvent(UEventObserver.UEvent event) {
                    boolean equals = NoFocusWindow.HUNG_CONFIG_ENABLE.equals(event.get("SWITCH_STATE"));
                    synchronized (BatteryService.this.mLock) {
                        if (BatteryService.this.mInvalidCharger != equals) {
                            BatteryService.this.mInvalidCharger = equals ? 1 : 0;
                        }
                    }
                }
            }.startObserving("DEVPATH=/devices/virtual/switch/invalid_charger");
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.BatteryService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v3, types: [com.android.server.BatteryService$BatteryPropertiesRegistrar, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService, com.android.server.OppoBaseBatteryService
    public void onStart() {
        super.onStart();
        registerHealthCallback();
        this.mBinderService = new BinderService();
        publishBinderService("battery", this.mBinderService);
        this.mBatteryPropertiesRegistrar = new BatteryPropertiesRegistrar();
        publishBinderService("batteryproperties", this.mBatteryPropertiesRegistrar);
        publishLocalService(BatteryManagerInternal.class, new LocalService());
    }

    @Override // com.android.server.SystemService, com.android.server.OppoBaseBatteryService
    public void onBootPhase(int phase) {
        synchronized (this.mLock) {
            super.onBootPhase(phase);
        }
        if (phase == 550) {
            synchronized (this.mLock) {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("low_power_trigger_level"), false, new ContentObserver(this.mHandler) {
                    /* class com.android.server.BatteryService.AnonymousClass2 */

                    public void onChange(boolean selfChange) {
                        synchronized (BatteryService.this.mLock) {
                            BatteryService.this.updateBatteryWarningLevelLocked();
                        }
                    }
                }, -1);
                updateBatteryWarningLevelLocked();
            }
        }
    }

    private void registerHealthCallback() {
        traceBegin("HealthInitWrapper");
        this.mHealthServiceWrapper = new HealthServiceWrapper();
        this.mHealthHalCallback = new HealthHalCallback();
        try {
            this.mHealthServiceWrapper.init(this.mHealthHalCallback, new HealthServiceWrapper.IServiceManagerSupplier() {
                /* class com.android.server.BatteryService.AnonymousClass3 */
            }, new HealthServiceWrapper.IHealthSupplier() {
                /* class com.android.server.BatteryService.AnonymousClass4 */
            });
            traceEnd();
            traceBegin("HealthInitWaitUpdate");
            long beforeWait = SystemClock.uptimeMillis();
            synchronized (this.mLock) {
                while (this.mHealthInfo == null) {
                    String str = TAG;
                    Slog.i(str, "health: Waited " + (SystemClock.uptimeMillis() - beforeWait) + "ms for callbacks. Waiting another 1000 ms...");
                    try {
                        this.mLock.wait(1000);
                    } catch (InterruptedException e) {
                        Slog.i(TAG, "health: InterruptedException when waiting for update.  Continuing...");
                    }
                }
            }
            String str2 = TAG;
            Slog.i(str2, "health: Waited " + (SystemClock.uptimeMillis() - beforeWait) + "ms and received the update.");
            traceEnd();
        } catch (RemoteException ex) {
            Slog.e(TAG, "health: cannot register callback. (RemoteException)");
            throw ex.rethrowFromSystemServer();
        } catch (NoSuchElementException ex2) {
            Slog.e(TAG, "health: cannot register callback. (no supported health HAL service)");
            throw ex2;
        } catch (Throwable th) {
            traceEnd();
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBatteryWarningLevelLocked() {
        ContentResolver resolver = this.mContext.getContentResolver();
        int defWarnLevel = this.mContext.getResources().getInteger(17694829);
        this.mLowBatteryWarningLevel = Settings.Global.getInt(resolver, "low_power_trigger_level", defWarnLevel);
        if (this.mLowBatteryWarningLevel == 0) {
            this.mLowBatteryWarningLevel = defWarnLevel;
        }
        int i = this.mLowBatteryWarningLevel;
        int i2 = this.mCriticalBatteryLevel;
        if (i < i2) {
            this.mLowBatteryWarningLevel = i2;
        }
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694828);
        processValuesLocked(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPoweredLocked(int plugTypeSet) {
        if (this.mHealthInfo.batteryStatus == 1) {
            return true;
        }
        if ((plugTypeSet & 1) != 0 && this.mHealthInfo.chargerAcOnline) {
            return true;
        }
        if ((plugTypeSet & 2) != 0 && this.mHealthInfo.chargerUsbOnline) {
            return true;
        }
        if ((plugTypeSet & 4) == 0 || !this.mHealthInfo.chargerWirelessOnline) {
            return false;
        }
        return true;
    }

    private boolean shouldSendBatteryLowLocked() {
        int i;
        boolean plugged = this.mPlugType != 0;
        boolean oldPlugged = this.mLastPlugType != 0;
        if (plugged || this.mHealthInfo.batteryStatus == 1 || this.mHealthInfo.batteryLevel > (i = this.mLowBatteryWarningLevel)) {
            return false;
        }
        return oldPlugged || this.mLastBatteryLevel > i;
    }

    private boolean shouldShutdownLocked() {
        if (this.mHealthInfo.batteryLevel <= 0 && this.mHealthInfo.batteryPresent && this.mHealthInfo.batteryStatus != 2) {
            return true;
        }
        return false;
    }

    private void shutdownIfNoPowerLocked() {
        if (shouldShutdownLocked()) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.BatteryService.AnonymousClass5 */

                public void run() {
                    if (BatteryService.this.mActivityManagerInternal.isSystemReady()) {
                        String str = BatteryService.TAG;
                        Slog.v(str, "mHealthInfo.batteryLevel = " + BatteryService.this.mHealthInfo.batteryLevel + "shutdown because of low power");
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_LOW_BATTERY_POWER_OFF, new String[0]);
                        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                        intent.putExtra("android.intent.extra.REASON", "battery");
                        intent.setFlags(268435456);
                        BatteryService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                        BatteryService.this.mLed.turnOffBatteryLights();
                    }
                }
            });
        }
    }

    private void shutdownIfOverTempLocked() {
        if (!ignoreShutdownIfOverTempByOppoLocked() && this.mHealthInfo.batteryTemperature > this.mShutdownBatteryTemperature) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.BatteryService.AnonymousClass6 */

                public void run() {
                    if (BatteryService.this.mActivityManagerInternal.isSystemReady()) {
                        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                        intent.putExtra("android.intent.extra.REASON", "thermal,battery");
                        intent.setFlags(268435456);
                        BatteryService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void update(android.hardware.health.V2_0.HealthInfo info) {
        traceBegin("HealthInfoUpdate");
        Trace.traceCounter(131072, "BatteryChargeCounter", info.legacy.batteryChargeCounter);
        Trace.traceCounter(131072, "BatteryCurrent", info.legacy.batteryCurrent);
        synchronized (this.mLock) {
            if (DEBUG_COMMAND) {
                String str = TAG;
                Slog.v(str, "update mUpdatesStopped = " + this.mUpdatesStopped);
            }
            if (!this.mUpdatesStopped) {
                this.mHealthInfo = info.legacy;
                updateBatteryService();
                processValuesLocked(false);
                this.mLock.notifyAll();
            } else {
                copy(this.mLastHealthInfo, info.legacy);
            }
        }
        traceEnd();
    }

    private static void copy(HealthInfo dst, HealthInfo src) {
        dst.chargerAcOnline = src.chargerAcOnline;
        dst.chargerUsbOnline = src.chargerUsbOnline;
        dst.chargerWirelessOnline = src.chargerWirelessOnline;
        dst.maxChargingCurrent = src.maxChargingCurrent;
        dst.maxChargingVoltage = src.maxChargingVoltage;
        dst.batteryStatus = src.batteryStatus;
        dst.batteryHealth = src.batteryHealth;
        dst.batteryPresent = src.batteryPresent;
        dst.batteryLevel = src.batteryLevel;
        dst.batteryVoltage = src.batteryVoltage;
        dst.batteryTemperature = src.batteryTemperature;
        dst.batteryCurrent = src.batteryCurrent;
        dst.batteryCycleCount = src.batteryCycleCount;
        dst.batteryFullCharge = src.batteryFullCharge;
        dst.batteryChargeCounter = src.batteryChargeCounter;
        dst.batteryTechnology = src.batteryTechnology;
    }

    private void processValuesLocked(boolean force) {
        boolean logOutlier = false;
        long dischargeDuration = 0;
        this.mBatteryLevelCritical = this.mHealthInfo.batteryStatus != 1 && this.mHealthInfo.batteryLevel <= this.mCriticalBatteryLevel;
        if (this.mHealthInfo.chargerAcOnline) {
            this.mPlugType = 1;
        } else if (this.mHealthInfo.chargerUsbOnline) {
            this.mPlugType = 2;
        } else if (this.mHealthInfo.chargerWirelessOnline) {
            this.mPlugType = 4;
        } else {
            this.mPlugType = 0;
        }
        processValuesForOppoLocked(force, this.mPlugType, this.mHealthInfo);
        if (DEBUG) {
            Slog.d(TAG, "Processing new values: info=" + this.mHealthInfo + ", mBatteryLevelCritical=" + this.mBatteryLevelCritical + ", mPlugType=" + this.mPlugType);
        }
        try {
            this.mBatteryStats.setBatteryState(this.mHealthInfo.batteryStatus, this.mHealthInfo.batteryHealth, this.mPlugType, this.mHealthInfo.batteryLevel, this.mHealthInfo.batteryTemperature, this.mHealthInfo.batteryVoltage, this.mHealthInfo.batteryChargeCounter, this.mHealthInfo.batteryFullCharge);
        } catch (RemoteException e) {
        }
        shutdownIfNoPowerLocked();
        shutdownIfOverTempLocked();
        if (force || this.mHealthInfo.batteryStatus != this.mLastBatteryStatus || this.mHealthInfo.batteryHealth != this.mLastBatteryHealth || this.mHealthInfo.batteryPresent != this.mLastBatteryPresent || this.mHealthInfo.batteryLevel != this.mLastBatteryLevel || this.mPlugType != this.mLastPlugType || this.mHealthInfo.batteryVoltage != this.mLastBatteryVoltage || shouldUpdateChargingState(this.mHealthInfo.batteryTemperature, this.mLastBatteryTemperature) || this.mHealthInfo.maxChargingCurrent != this.mLastMaxChargingCurrent || this.mHealthInfo.maxChargingVoltage != this.mLastMaxChargingVoltage || this.mHealthInfo.batteryChargeCounter != this.mLastChargeCounter || this.mInvalidCharger != this.mLastInvalidCharger) {
            int i = this.mPlugType;
            int i2 = this.mLastPlugType;
            if (i != i2) {
                if (i2 == 0) {
                    this.mChargeStartLevel = this.mHealthInfo.batteryLevel;
                    this.mChargeStartTime = SystemClock.elapsedRealtime();
                    LogMaker builder = new LogMaker(1417);
                    builder.setType(4);
                    builder.addTaggedData(1421, Integer.valueOf(this.mPlugType));
                    builder.addTaggedData(1418, Integer.valueOf(this.mHealthInfo.batteryLevel));
                    this.mMetricsLogger.write(builder);
                    if (!(this.mDischargeStartTime == 0 || this.mDischargeStartLevel == this.mHealthInfo.batteryLevel)) {
                        dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
                        logOutlier = true;
                        EventLog.writeEvent((int) EventLogTags.BATTERY_DISCHARGE, Long.valueOf(dischargeDuration), Integer.valueOf(this.mDischargeStartLevel), Integer.valueOf(this.mHealthInfo.batteryLevel));
                        this.mDischargeStartTime = 0;
                    }
                } else if (i == 0) {
                    this.mDischargeStartTime = SystemClock.elapsedRealtime();
                    this.mDischargeStartLevel = this.mHealthInfo.batteryLevel;
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    long j = this.mChargeStartTime;
                    long chargeDuration = elapsedRealtime - j;
                    if (!(j == 0 || chargeDuration == 0)) {
                        LogMaker builder2 = new LogMaker(1417);
                        builder2.setType(5);
                        builder2.addTaggedData(1421, Integer.valueOf(this.mLastPlugType));
                        builder2.addTaggedData(1420, Long.valueOf(chargeDuration));
                        builder2.addTaggedData(1418, Integer.valueOf(this.mChargeStartLevel));
                        builder2.addTaggedData(1419, Integer.valueOf(this.mHealthInfo.batteryLevel));
                        this.mMetricsLogger.write(builder2);
                    }
                    this.mChargeStartTime = 0;
                }
                onPlugChangedForOppoSysStateManager(this.mPlugType);
            }
            if (!(this.mHealthInfo.batteryStatus == this.mLastBatteryStatus && this.mHealthInfo.batteryHealth == this.mLastBatteryHealth && this.mHealthInfo.batteryPresent == this.mLastBatteryPresent && this.mPlugType == this.mLastPlugType)) {
                EventLog.writeEvent((int) EventLogTags.BATTERY_STATUS, Integer.valueOf(this.mHealthInfo.batteryStatus), Integer.valueOf(this.mHealthInfo.batteryHealth), Integer.valueOf(this.mHealthInfo.batteryPresent ? 1 : 0), Integer.valueOf(this.mPlugType), this.mHealthInfo.batteryTechnology);
            }
            if (this.mHealthInfo.batteryLevel != this.mLastBatteryLevel) {
                EventLog.writeEvent((int) EventLogTags.BATTERY_LEVEL, Integer.valueOf(this.mHealthInfo.batteryLevel), Integer.valueOf(this.mHealthInfo.batteryVoltage), Integer.valueOf(this.mHealthInfo.batteryTemperature));
            }
            if (this.mBatteryLevelCritical && !this.mLastBatteryLevelCritical && this.mPlugType == 0) {
                logOutlier = true;
                dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
            }
            if (!this.mBatteryLevelLow) {
                if (this.mPlugType == 0 && this.mHealthInfo.batteryStatus != 1 && this.mHealthInfo.batteryLevel <= this.mLowBatteryWarningLevel) {
                    this.mBatteryLevelLow = true;
                }
            } else if (this.mPlugType != 0) {
                this.mBatteryLevelLow = false;
            } else if (this.mHealthInfo.batteryLevel >= this.mLowBatteryCloseWarningLevel) {
                this.mBatteryLevelLow = false;
            } else if (force && this.mHealthInfo.batteryLevel >= this.mLowBatteryWarningLevel) {
                this.mBatteryLevelLow = false;
            }
            this.mSequence++;
            if (this.mPlugType != 0 && this.mLastPlugType == 0) {
                final Intent statusIntent = new Intent("android.intent.action.ACTION_POWER_CONNECTED");
                statusIntent.setFlags(67108864);
                statusIntent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                appendFlagToStatusIntent(statusIntent, 17825792);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.BatteryService.AnonymousClass7 */

                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            } else if (this.mPlugType == 0 && this.mLastPlugType != 0) {
                final Intent statusIntent2 = new Intent("android.intent.action.ACTION_POWER_DISCONNECTED");
                statusIntent2.setFlags(67108864);
                statusIntent2.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                appendFlagToStatusIntent(statusIntent2, DumpState.DUMP_DEXOPT);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.BatteryService.AnonymousClass8 */

                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent2, UserHandle.ALL);
                    }
                });
            }
            if (shouldSendBatteryLowLocked()) {
                this.mSentLowBatteryBroadcast = true;
                final Intent statusIntent3 = new Intent("android.intent.action.BATTERY_LOW");
                statusIntent3.setFlags(67108864);
                statusIntent3.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.BatteryService.AnonymousClass9 */

                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent3, UserHandle.ALL);
                    }
                });
            } else if (this.mSentLowBatteryBroadcast && this.mHealthInfo.batteryLevel >= this.mLowBatteryCloseWarningLevel) {
                this.mSentLowBatteryBroadcast = false;
                final Intent statusIntent4 = new Intent("android.intent.action.BATTERY_OKAY");
                statusIntent4.setFlags(67108864);
                statusIntent4.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.BatteryService.AnonymousClass10 */

                    public void run() {
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent4, UserHandle.ALL);
                    }
                });
            }
            sendBatteryChangedIntentLocked();
            if (!(this.mLastBatteryLevel == this.mHealthInfo.batteryLevel && this.mLastPlugType == this.mPlugType)) {
                sendBatteryLevelChangedIntentLocked();
            }
            this.mLed.updateLightsLocked();
            if (logOutlier && dischargeDuration != 0) {
                logOutlierLocked(dischargeDuration);
            }
            this.mLastBatteryStatus = this.mHealthInfo.batteryStatus;
            this.mLastBatteryHealth = this.mHealthInfo.batteryHealth;
            this.mLastBatteryPresent = this.mHealthInfo.batteryPresent;
            this.mLastBatteryLevel = this.mHealthInfo.batteryLevel;
            this.mLastPlugType = this.mPlugType;
            this.mLastBatteryVoltage = this.mHealthInfo.batteryVoltage;
            this.mLastBatteryTemperature = this.mHealthInfo.batteryTemperature;
            this.mLastMaxChargingCurrent = this.mHealthInfo.maxChargingCurrent;
            this.mLastMaxChargingVoltage = this.mHealthInfo.maxChargingVoltage;
            this.mLastChargeCounter = this.mHealthInfo.batteryChargeCounter;
            this.mLastBatteryLevelCritical = this.mBatteryLevelCritical;
            this.mLastInvalidCharger = this.mInvalidCharger;
            saveLastStatsAfterValuesChanged();
        }
    }

    private void sendBatteryChangedIntentLocked() {
        Intent intent = new Intent("android.intent.action.BATTERY_CHANGED");
        intent.addFlags(1610612736);
        int icon = getIconLocked(this.mHealthInfo.batteryLevel);
        intent.putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
        intent.putExtra("status", this.mHealthInfo.batteryStatus);
        intent.putExtra("health", this.mHealthInfo.batteryHealth);
        intent.putExtra("present", this.mHealthInfo.batteryPresent);
        intent.putExtra("level", this.mHealthInfo.batteryLevel);
        intent.putExtra("battery_low", this.mSentLowBatteryBroadcast);
        intent.putExtra("scale", 100);
        intent.putExtra("icon-small", icon);
        intent.putExtra("plugged", this.mPlugType);
        intent.putExtra("voltage", this.mHealthInfo.batteryVoltage);
        intent.putExtra("temperature", this.mHealthInfo.batteryTemperature);
        intent.putExtra("technology", this.mHealthInfo.batteryTechnology);
        intent.putExtra("invalid_charger", this.mInvalidCharger);
        intent.putExtra("max_charging_current", this.mHealthInfo.maxChargingCurrent);
        intent.putExtra("max_charging_voltage", this.mHealthInfo.maxChargingVoltage);
        intent.putExtra("charge_counter", this.mHealthInfo.batteryChargeCounter);
        appendExtraToBatteryStatusChangedIntend(intent);
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "Sending ACTION_BATTERY_CHANGED. scale:100, info:" + this.mHealthInfo.toString() + getBatteryStatusStrForDebug());
        }
        this.mHandler.post(new Runnable(intent) {
            /* class com.android.server.$$Lambda$BatteryService$2x73lvpB0jctMSVP4qb9sHAqRPw */
            private final /* synthetic */ Intent f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                BatteryService.lambda$sendBatteryChangedIntentLocked$0(this.f$0);
            }
        });
    }

    private void sendBatteryLevelChangedIntentLocked() {
        Bundle event = new Bundle();
        long now = SystemClock.elapsedRealtime();
        event.putInt(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSequence);
        event.putInt("status", this.mHealthInfo.batteryStatus);
        event.putInt("health", this.mHealthInfo.batteryHealth);
        event.putBoolean("present", this.mHealthInfo.batteryPresent);
        event.putInt("level", this.mHealthInfo.batteryLevel);
        event.putBoolean("battery_low", this.mSentLowBatteryBroadcast);
        event.putInt("scale", 100);
        event.putInt("plugged", this.mPlugType);
        event.putInt("voltage", this.mHealthInfo.batteryVoltage);
        event.putLong("android.os.extra.EVENT_TIMESTAMP", now);
        boolean queueWasEmpty = this.mBatteryLevelsEventQueue.isEmpty();
        this.mBatteryLevelsEventQueue.add(event);
        if (this.mBatteryLevelsEventQueue.size() > 100) {
            this.mBatteryLevelsEventQueue.removeFirst();
        }
        if (queueWasEmpty) {
            long j = this.mLastBatteryLevelChangedSentMs;
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.$$Lambda$BatteryService$D1kwd7L7yyqN5niz3KWkTepVmUk */

                public final void run() {
                    BatteryService.lambda$D1kwd7L7yyqN5niz3KWkTepVmUk(BatteryService.this);
                }
            }, now - j > 60000 ? 0 : (j + 60000) - now);
        }
    }

    /* access modifiers changed from: private */
    public void sendEnqueuedBatteryLevelChangedEvents() {
        ArrayList<Bundle> events;
        synchronized (this.mLock) {
            events = new ArrayList<>(this.mBatteryLevelsEventQueue);
            this.mBatteryLevelsEventQueue.clear();
        }
        Intent intent = new Intent("android.intent.action.BATTERY_LEVEL_CHANGED");
        intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
        intent.putParcelableArrayListExtra("android.os.extra.EVENTS", events);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.BATTERY_STATS");
        this.mLastBatteryLevelChangedSentMs = SystemClock.elapsedRealtime();
    }

    private void logBatteryStatsLocked() {
        DropBoxManager db;
        String str;
        StringBuilder sb;
        IBinder batteryInfoService = ServiceManager.getService("batterystats");
        if (batteryInfoService != null && (db = (DropBoxManager) this.mContext.getSystemService("dropbox")) != null && db.isTagEnabled("BATTERY_DISCHARGE_INFO")) {
            File dumpFile = null;
            FileOutputStream dumpStream = null;
            try {
                dumpFile = new File("/data/system/batterystats.dump");
                dumpStream = new FileOutputStream(dumpFile);
                batteryInfoService.dump(dumpStream.getFD(), DUMPSYS_ARGS);
                FileUtils.sync(dumpStream);
                db.addFile("BATTERY_DISCHARGE_INFO", dumpFile, 2);
                try {
                    dumpStream.close();
                } catch (IOException e) {
                    Slog.e(TAG, "failed to close dumpsys output stream");
                }
                if (!dumpFile.delete()) {
                    str = TAG;
                    sb = new StringBuilder();
                    sb.append("failed to delete temporary dumpsys file: ");
                    sb.append(dumpFile.getAbsolutePath());
                    Slog.e(str, sb.toString());
                }
            } catch (RemoteException e2) {
                Slog.e(TAG, "failed to dump battery service", e2);
                if (dumpStream != null) {
                    try {
                        dumpStream.close();
                    } catch (IOException e3) {
                        Slog.e(TAG, "failed to close dumpsys output stream");
                    }
                }
                if (dumpFile != null && !dumpFile.delete()) {
                    str = TAG;
                    sb = new StringBuilder();
                }
            } catch (IOException e4) {
                Slog.e(TAG, "failed to write dumpsys file", e4);
                if (dumpStream != null) {
                    try {
                        dumpStream.close();
                    } catch (IOException e5) {
                        Slog.e(TAG, "failed to close dumpsys output stream");
                    }
                }
                if (dumpFile != null && !dumpFile.delete()) {
                    str = TAG;
                    sb = new StringBuilder();
                }
            } catch (Throwable th) {
                if (dumpStream != null) {
                    try {
                        dumpStream.close();
                    } catch (IOException e6) {
                        Slog.e(TAG, "failed to close dumpsys output stream");
                    }
                }
                if (dumpFile != null && !dumpFile.delete()) {
                    String str2 = TAG;
                    Slog.e(str2, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                }
                throw th;
            }
        }
    }

    private void logOutlierLocked(long duration) {
        ContentResolver cr = this.mContext.getContentResolver();
        String dischargeThresholdString = Settings.Global.getString(cr, "battery_discharge_threshold");
        String durationThresholdString = Settings.Global.getString(cr, "battery_discharge_duration_threshold");
        if (dischargeThresholdString != null && durationThresholdString != null) {
            try {
                long durationThreshold = Long.parseLong(durationThresholdString);
                int dischargeThreshold = Integer.parseInt(dischargeThresholdString);
                if (duration <= durationThreshold && this.mDischargeStartLevel - this.mHealthInfo.batteryLevel >= dischargeThreshold) {
                    logBatteryStatsLocked();
                }
                if (DEBUG) {
                    String str = TAG;
                    Slog.v(str, "duration threshold: " + durationThreshold + " discharge threshold: " + dischargeThreshold);
                }
                if (DEBUG) {
                    String str2 = TAG;
                    Slog.v(str2, "duration: " + duration + " discharge: " + (this.mDischargeStartLevel - this.mHealthInfo.batteryLevel));
                }
            } catch (NumberFormatException e) {
                String str3 = TAG;
                Slog.e(str3, "Invalid DischargeThresholds GService string: " + durationThresholdString + " or " + dischargeThresholdString);
            }
        }
    }

    private int getIconLocked(int level) {
        if (this.mHealthInfo.batteryStatus == 2) {
            return 17303535;
        }
        if (this.mHealthInfo.batteryStatus == 3) {
            return 17303521;
        }
        if (this.mHealthInfo.batteryStatus != 4 && this.mHealthInfo.batteryStatus != 5) {
            return 17303549;
        }
        if (!isPoweredLocked(7) || this.mHealthInfo.batteryLevel < 100) {
            return 17303521;
        }
        return 17303535;
    }

    /* access modifiers changed from: package-private */
    public class Shell extends ShellCommand {
        Shell() {
        }

        public int onCommand(String cmd) {
            return BatteryService.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            BatteryService.dumpHelp(getOutPrintWriter());
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

    /* access modifiers changed from: package-private */
    public int parseOptions(Shell shell) {
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x01c2  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a  */
    public int onShellCommand(Shell shell, String cmd) {
        boolean z;
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        int hashCode = cmd.hashCode();
        char c = 2;
        if (hashCode != -840325209) {
            if (hashCode != 113762) {
                if (hashCode == 108404047 && cmd.equals("reset")) {
                    z = true;
                    if (z) {
                        int opts = parseOptions(shell);
                        getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                        if (!this.mUpdatesStopped) {
                            copy(this.mLastHealthInfo, this.mHealthInfo);
                        }
                        HealthInfo healthInfo = this.mHealthInfo;
                        healthInfo.chargerAcOnline = false;
                        healthInfo.chargerUsbOnline = false;
                        healthInfo.chargerWirelessOnline = false;
                        long ident = Binder.clearCallingIdentity();
                        try {
                            this.mUpdatesStopped = true;
                            processValuesFromShellLocked(pw, opts);
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    } else if (z) {
                        int opts2 = parseOptions(shell);
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
                                copy(this.mLastHealthInfo, this.mHealthInfo);
                            }
                            boolean update = true;
                            switch (key.hashCode()) {
                                case -1000044642:
                                    if (key.equals("wireless")) {
                                        c = 3;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -892481550:
                                    if (key.equals("status")) {
                                        c = 4;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -318277445:
                                    if (key.equals("present")) {
                                        c = 0;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 3106:
                                    if (key.equals("ac")) {
                                        c = 1;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 116100:
                                    if (key.equals("usb")) {
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 3556308:
                                    if (key.equals("temp")) {
                                        c = 7;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 102865796:
                                    if (key.equals("level")) {
                                        c = 5;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 957830652:
                                    if (key.equals("counter")) {
                                        c = 6;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1959784951:
                                    if (key.equals("invalid")) {
                                        c = '\b';
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                default:
                                    c = 65535;
                                    break;
                            }
                            switch (c) {
                                case 0:
                                    this.mHealthInfo.batteryPresent = Integer.parseInt(value) != 0;
                                    break;
                                case 1:
                                    this.mHealthInfo.chargerAcOnline = Integer.parseInt(value) != 0;
                                    break;
                                case 2:
                                    this.mHealthInfo.chargerUsbOnline = Integer.parseInt(value) != 0;
                                    break;
                                case 3:
                                    this.mHealthInfo.chargerWirelessOnline = Integer.parseInt(value) != 0;
                                    break;
                                case 4:
                                    this.mHealthInfo.batteryStatus = Integer.parseInt(value);
                                    break;
                                case 5:
                                    this.mHealthInfo.batteryLevel = Integer.parseInt(value);
                                    break;
                                case 6:
                                    this.mHealthInfo.batteryChargeCounter = Integer.parseInt(value);
                                    break;
                                case 7:
                                    this.mHealthInfo.batteryTemperature = Integer.parseInt(value);
                                    break;
                                case '\b':
                                    this.mInvalidCharger = Integer.parseInt(value);
                                    break;
                                default:
                                    pw.println("Unknown set option: " + key);
                                    update = false;
                                    break;
                            }
                            if (update) {
                                long ident2 = Binder.clearCallingIdentity();
                                try {
                                    this.mUpdatesStopped = true;
                                    processValuesFromShellLocked(pw, opts2);
                                } finally {
                                    Binder.restoreCallingIdentity(ident2);
                                }
                            }
                        } catch (NumberFormatException e) {
                            pw.println("Bad value: " + value);
                            return -1;
                        }
                    } else if (!z) {
                        return shell.handleDefaultCommands(cmd);
                    } else {
                        int opts3 = parseOptions(shell);
                        getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                        long ident3 = Binder.clearCallingIdentity();
                        try {
                            if (this.mUpdatesStopped) {
                                this.mUpdatesStopped = false;
                                copy(this.mHealthInfo, this.mLastHealthInfo);
                                processValuesFromShellLocked(pw, opts3);
                            }
                        } finally {
                            Binder.restoreCallingIdentity(ident3);
                        }
                    }
                    return 0;
                }
            } else if (cmd.equals("set")) {
                z = true;
                if (z) {
                }
                return 0;
            }
        } else if (cmd.equals("unplug")) {
            z = false;
            if (z) {
            }
            return 0;
        }
        z = true;
        if (z) {
        }
        return 0;
    }

    private void processValuesFromShellLocked(PrintWriter pw, int opts) {
        processValuesLocked((opts & 1) != 0);
        if ((opts & 1) != 0) {
            pw.println(this.mSequence);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            if (dumpInternalBase(fd, pw, args)) {
                if (!(args == null || args.length == 0)) {
                    if (!"-a".equals(args[0])) {
                        new Shell().exec(this.mBinderService, null, fd, null, args, null, new ResultReceiver(null));
                    }
                }
                pw.println("Current Battery Service state:");
                if (this.mUpdatesStopped) {
                    pw.println("  (UPDATES STOPPED -- use 'reset' to restart)");
                }
                pw.println("  AC powered: " + this.mHealthInfo.chargerAcOnline);
                pw.println("  USB powered: " + this.mHealthInfo.chargerUsbOnline);
                pw.println("  Wireless powered: " + this.mHealthInfo.chargerWirelessOnline);
                pw.println("  Max charging current: " + this.mHealthInfo.maxChargingCurrent);
                pw.println("  Max charging voltage: " + this.mHealthInfo.maxChargingVoltage);
                pw.println("  Charge counter: " + this.mHealthInfo.batteryChargeCounter);
                pw.println("  status: " + this.mHealthInfo.batteryStatus);
                pw.println("  health: " + this.mHealthInfo.batteryHealth);
                pw.println("  present: " + this.mHealthInfo.batteryPresent);
                pw.println("  level: " + this.mHealthInfo.batteryLevel);
                pw.println("  scale: 100");
                pw.println("  voltage: " + this.mHealthInfo.batteryVoltage);
                pw.println("  temperature: " + this.mHealthInfo.batteryTemperature);
                pw.println("  technology: " + this.mHealthInfo.batteryTechnology);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mLock) {
            proto.write(1133871366145L, this.mUpdatesStopped);
            int batteryPluggedValue = 0;
            if (this.mHealthInfo.chargerAcOnline) {
                batteryPluggedValue = 1;
            } else if (this.mHealthInfo.chargerUsbOnline) {
                batteryPluggedValue = 2;
            } else if (this.mHealthInfo.chargerWirelessOnline) {
                batteryPluggedValue = 4;
            }
            proto.write(1159641169922L, batteryPluggedValue);
            proto.write(1120986464259L, this.mHealthInfo.maxChargingCurrent);
            proto.write(1120986464260L, this.mHealthInfo.maxChargingVoltage);
            proto.write(1120986464261L, this.mHealthInfo.batteryChargeCounter);
            proto.write(1159641169926L, this.mHealthInfo.batteryStatus);
            proto.write(1159641169927L, this.mHealthInfo.batteryHealth);
            proto.write(1133871366152L, this.mHealthInfo.batteryPresent);
            proto.write(1120986464265L, this.mHealthInfo.batteryLevel);
            proto.write(1120986464266L, 100);
            proto.write(1120986464267L, this.mHealthInfo.batteryVoltage);
            proto.write(1120986464268L, this.mHealthInfo.batteryTemperature);
            proto.write(1138166333453L, this.mHealthInfo.batteryTechnology);
        }
        proto.flush();
    }

    /* access modifiers changed from: private */
    public static void traceBegin(String name) {
        Trace.traceBegin(524288, name);
    }

    /* access modifiers changed from: private */
    public static void traceEnd() {
        Trace.traceEnd(524288);
    }

    /* access modifiers changed from: protected */
    public final class Led extends OppoBaseLed {
        private final int mBatteryFullARGB;
        private final int mBatteryLedOff;
        private final int mBatteryLedOn;
        private final Light mBatteryLight;
        private final int mBatteryLowARGB;
        private final int mBatteryMediumARGB;

        public Led(Context context, LightsManager lights) {
            super(context, lights);
            this.mBatteryLight = lights.getLight(3);
            this.mBatteryLowARGB = context.getResources().getInteger(17694866);
            this.mBatteryMediumARGB = context.getResources().getInteger(17694867);
            this.mBatteryFullARGB = context.getResources().getInteger(17694863);
            this.mBatteryLedOn = context.getResources().getInteger(17694865);
            this.mBatteryLedOff = context.getResources().getInteger(17694864);
        }

        public void updateLightsLocked() {
            if (!isIgnoreUpdateLights(BatteryService.this.mHealthInfo)) {
                int level = BatteryService.this.mHealthInfo.batteryLevel;
                int status = BatteryService.this.mHealthInfo.batteryStatus;
                if (level < BatteryService.this.mLowBatteryWarningLevel) {
                    if (status == 2) {
                        this.mBatteryLight.setColor(this.mBatteryLowARGB);
                    } else {
                        this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, this.mBatteryLedOn, this.mBatteryLedOff);
                    }
                } else if (status != 2 && status != 5) {
                    this.mBatteryLight.turnOff();
                } else if (status == 5 || level >= 90) {
                    this.mBatteryLight.setColor(this.mBatteryFullARGB);
                } else {
                    this.mBatteryLight.setColor(this.mBatteryMediumARGB);
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.OppoBaseLed
        public Light getBatteryLightInstance() {
            return this.mBatteryLight;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.OppoBaseLed
        public void onUpdateLights() {
            synchronized (BatteryService.this.mLock) {
                updateLightsLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class HealthHalCallback extends IHealthInfoCallback.Stub implements HealthServiceWrapper.Callback {
        private HealthHalCallback() {
        }

        @Override // android.hardware.health.V2_0.IHealthInfoCallback
        public void healthInfoChanged(android.hardware.health.V2_0.HealthInfo props) {
            BatteryService.this.update(props);
        }

        @Override // com.android.server.BatteryService.HealthServiceWrapper.Callback
        public void onRegistration(IHealth oldService, IHealth newService, String instance) {
            if (newService != null) {
                BatteryService.traceBegin("HealthUnregisterCallback");
                if (oldService != null) {
                    try {
                        int r = oldService.unregisterCallback(this);
                        if (r != 0) {
                            String str = BatteryService.TAG;
                            Slog.w(str, "health: cannot unregister previous callback: " + Result.toString(r));
                        }
                    } catch (RemoteException ex) {
                        String str2 = BatteryService.TAG;
                        Slog.w(str2, "health: cannot unregister previous callback (transaction error): " + ex.getMessage());
                    } catch (Throwable th) {
                        BatteryService.traceEnd();
                        throw th;
                    }
                }
                BatteryService.traceEnd();
                BatteryService.traceBegin("HealthRegisterCallback");
                try {
                    int r2 = newService.registerCallback(this);
                    if (r2 != 0) {
                        String str3 = BatteryService.TAG;
                        Slog.w(str3, "health: cannot register callback: " + Result.toString(r2));
                        BatteryService.traceEnd();
                        return;
                    }
                    newService.update();
                    BatteryService.traceEnd();
                } catch (RemoteException ex2) {
                    String str4 = BatteryService.TAG;
                    Slog.e(str4, "health: cannot register callback (transaction error): " + ex2.getMessage());
                } catch (Throwable th2) {
                    BatteryService.traceEnd();
                    throw th2;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class BinderService extends Binder {
        private BinderService() {
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(BatteryService.this.mContext, BatteryService.TAG, pw)) {
                if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                    BatteryService.this.dumpInternal(fd, pw, args);
                } else {
                    BatteryService.this.dumpProto(fd);
                }
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, callback, resultReceiver);
        }
    }

    /* access modifiers changed from: private */
    public final class BatteryPropertiesRegistrar extends IBatteryPropertiesRegistrar.Stub {
        private BatteryPropertiesRegistrar() {
        }

        public int getProperty(int id, BatteryProperty prop) throws RemoteException {
            BatteryService.traceBegin("HealthGetProperty");
            try {
                IHealth service = BatteryService.this.mHealthServiceWrapper.getLastService();
                if (service != null) {
                    MutableInt outResult = new MutableInt(1);
                    switch (id) {
                        case 1:
                            service.getChargeCounter(new IHealth.getChargeCounterCallback(outResult, prop) {
                                /* class com.android.server.$$Lambda$BatteryService$BatteryPropertiesRegistrar$7YB9O7NDYgUY9hQvFzC2FQ2V5w */
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.health.V2_0.IHealth.getChargeCounterCallback
                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$0(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                        case 2:
                            service.getCurrentNow(new IHealth.getCurrentNowCallback(outResult, prop) {
                                /* class com.android.server.$$Lambda$BatteryService$BatteryPropertiesRegistrar$JTQ79fl14NyImudsJhxMp1dJI8 */
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.health.V2_0.IHealth.getCurrentNowCallback
                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$1(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                        case 3:
                            service.getCurrentAverage(new IHealth.getCurrentAverageCallback(outResult, prop) {
                                /* class com.android.server.$$Lambda$BatteryService$BatteryPropertiesRegistrar$KZAu97wwr_7_MI0awCjQTzdIuAI */
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.health.V2_0.IHealth.getCurrentAverageCallback
                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$2(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                        case 4:
                            service.getCapacity(new IHealth.getCapacityCallback(outResult, prop) {
                                /* class com.android.server.$$Lambda$BatteryService$BatteryPropertiesRegistrar$DM4ow6LCJYWBfhHp2f1JW8nww */
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.health.V2_0.IHealth.getCapacityCallback
                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$3(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                        case 5:
                            service.getEnergyCounter(new IHealth.getEnergyCounterCallback(outResult, prop) {
                                /* class com.android.server.$$Lambda$BatteryService$BatteryPropertiesRegistrar$9z3zqgxtPzBN8Qoni5nHVb0m8EY */
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.health.V2_0.IHealth.getEnergyCounterCallback
                                public final void onValues(int i, long j) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$5(this.f$0, this.f$1, i, j);
                                }
                            });
                            break;
                        case 6:
                            service.getChargeStatus(new IHealth.getChargeStatusCallback(outResult, prop) {
                                /* class com.android.server.$$Lambda$BatteryService$BatteryPropertiesRegistrar$hInbvsihGvN2hXqvdcoFYzdeqHw */
                                private final /* synthetic */ MutableInt f$0;
                                private final /* synthetic */ BatteryProperty f$1;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                }

                                @Override // android.hardware.health.V2_0.IHealth.getChargeStatusCallback
                                public final void onValues(int i, int i2) {
                                    BatteryService.BatteryPropertiesRegistrar.lambda$getProperty$4(this.f$0, this.f$1, i, i2);
                                }
                            });
                            break;
                    }
                    return outResult.value;
                }
                throw new RemoteException("no health service");
            } finally {
                BatteryService.traceEnd();
            }
        }

        static /* synthetic */ void lambda$getProperty$0(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$1(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$2(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$3(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$4(MutableInt outResult, BatteryProperty prop, int result, int value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong((long) value);
            }
        }

        static /* synthetic */ void lambda$getProperty$5(MutableInt outResult, BatteryProperty prop, int result, long value) {
            outResult.value = result;
            if (result == 0) {
                prop.setLong(value);
            }
        }

        public void scheduleUpdate() throws RemoteException {
            BatteryService.traceBegin("HealthScheduleUpdate");
            try {
                IHealth service = BatteryService.this.mHealthServiceWrapper.getLastService();
                if (service != null) {
                    service.update();
                    return;
                }
                throw new RemoteException("no health service");
            } finally {
                BatteryService.traceEnd();
            }
        }
    }

    private final class LocalService extends BatteryManagerInternal {
        private LocalService() {
        }

        public boolean isPowered(int plugTypeSet) {
            boolean isPoweredLocked;
            synchronized (BatteryService.this.mLock) {
                isPoweredLocked = BatteryService.this.isPoweredLocked(plugTypeSet);
            }
            return isPoweredLocked;
        }

        public int getPlugType() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mPlugType;
            }
            return i;
        }

        public int getBatteryLevel() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mHealthInfo.batteryLevel;
            }
            return i;
        }

        public int getBatteryChargeCounter() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mHealthInfo.batteryChargeCounter;
            }
            return i;
        }

        public int getBatteryFullCharge() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mHealthInfo.batteryFullCharge;
            }
            return i;
        }

        public boolean getBatteryLevelLow() {
            boolean z;
            synchronized (BatteryService.this.mLock) {
                z = BatteryService.this.mBatteryLevelLow;
            }
            return z;
        }

        public int getInvalidCharger() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mInvalidCharger;
            }
            return i;
        }

        public int getBatteryTemperature() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mHealthInfo.batteryTemperature;
            }
            return i;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static final class HealthServiceWrapper {
        public static final String INSTANCE_HEALTHD = "backup";
        public static final String INSTANCE_VENDOR = "default";
        private static final String TAG = "HealthServiceWrapper";
        private static final List<String> sAllInstances = Arrays.asList(INSTANCE_VENDOR, INSTANCE_HEALTHD);
        private Callback mCallback;
        private final HandlerThread mHandlerThread = new HandlerThread("HealthServiceRefresh");
        private IHealthSupplier mHealthSupplier;
        private String mInstanceName;
        private final AtomicReference<IHealth> mLastService = new AtomicReference<>();
        private final IServiceNotification mNotification = new Notification();

        /* access modifiers changed from: package-private */
        public interface Callback {
            void onRegistration(IHealth iHealth, IHealth iHealth2, String str);
        }

        HealthServiceWrapper() {
        }

        /* access modifiers changed from: package-private */
        public IHealth getLastService() {
            return this.mLastService.get();
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public void init(Callback callback, IServiceManagerSupplier managerSupplier, IHealthSupplier healthSupplier) throws RemoteException, NoSuchElementException, NullPointerException {
            if (callback == null || managerSupplier == null || healthSupplier == null) {
                throw new NullPointerException();
            }
            this.mCallback = callback;
            this.mHealthSupplier = healthSupplier;
            IHealth newService = null;
            Iterator<String> it = sAllInstances.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String name = it.next();
                BatteryService.traceBegin("HealthInitGetService_" + name);
                try {
                    newService = healthSupplier.get(name);
                } catch (NoSuchElementException e) {
                } catch (Throwable th) {
                    BatteryService.traceEnd();
                    throw th;
                }
                BatteryService.traceEnd();
                if (newService != null) {
                    this.mInstanceName = name;
                    this.mLastService.set(newService);
                    break;
                }
            }
            String str = this.mInstanceName;
            if (str == null || newService == null) {
                throw new NoSuchElementException(String.format("No IHealth service instance among %s is available. Perhaps no permission?", sAllInstances.toString()));
            }
            this.mCallback.onRegistration(null, newService, str);
            BatteryService.traceBegin("HealthInitRegisterNotification");
            this.mHandlerThread.start();
            try {
                managerSupplier.get().registerForNotifications(IHealth.kInterfaceName, this.mInstanceName, this.mNotification);
                BatteryService.traceEnd();
                Slog.i(TAG, "health: HealthServiceWrapper listening to instance " + this.mInstanceName);
            } catch (Throwable th2) {
                BatteryService.traceEnd();
                throw th2;
            }
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public HandlerThread getHandlerThread() {
            return this.mHandlerThread;
        }

        /* access modifiers changed from: package-private */
        public interface IServiceManagerSupplier {
            default IServiceManager get() throws NoSuchElementException, RemoteException {
                return IServiceManager.getService();
            }
        }

        /* access modifiers changed from: package-private */
        public interface IHealthSupplier {
            default IHealth get(String name) throws NoSuchElementException, RemoteException {
                return IHealth.getService(name, true);
            }
        }

        private class Notification extends IServiceNotification.Stub {
            private Notification() {
            }

            @Override // android.hidl.manager.V1_0.IServiceNotification
            public final void onRegistration(String interfaceName, String instanceName, boolean preexisting) {
                if (IHealth.kInterfaceName.equals(interfaceName) && HealthServiceWrapper.this.mInstanceName.equals(instanceName)) {
                    HealthServiceWrapper.this.mHandlerThread.getThreadHandler().post(new Runnable() {
                        /* class com.android.server.BatteryService.HealthServiceWrapper.Notification.AnonymousClass1 */

                        public void run() {
                            try {
                                IHealth newService = HealthServiceWrapper.this.mHealthSupplier.get(HealthServiceWrapper.this.mInstanceName);
                                IHealth oldService = (IHealth) HealthServiceWrapper.this.mLastService.getAndSet(newService);
                                if (!Objects.equals(newService, oldService)) {
                                    Slog.i(HealthServiceWrapper.TAG, "health: new instance registered " + HealthServiceWrapper.this.mInstanceName);
                                    HealthServiceWrapper.this.mCallback.onRegistration(oldService, newService, HealthServiceWrapper.this.mInstanceName);
                                }
                            } catch (RemoteException | NoSuchElementException ex) {
                                Slog.e(HealthServiceWrapper.TAG, "health: Cannot get instance '" + HealthServiceWrapper.this.mInstanceName + "': " + ex.getMessage() + ". Perhaps no permission?");
                            }
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.OppoBaseBatteryService
    public void setDebugSwitchState(boolean on) {
        super.setDebugSwitchState(on);
        DEBUG = on;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.OppoBaseBatteryService
    public OppoBaseLed getLedInstance() {
        return this.mLed;
    }
}
