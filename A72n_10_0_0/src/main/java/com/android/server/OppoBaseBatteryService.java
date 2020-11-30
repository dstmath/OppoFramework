package com.android.server;

import android.app.ActivityManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.OppoBatteryStatsInternal;
import android.hardware.health.V1_0.HealthInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.OppoThermalState;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.coloros.OppoSysStateManagerInternal;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public abstract class OppoBaseBatteryService extends SystemService {
    private static final int BATTERY_PLUGGED_NONE = 0;
    protected static boolean DEBUG_COMMAND = false;
    protected static boolean DEBUG_PANIC = false;
    private static final int MIN_CHARGE_TIME = 600000;
    private static final int MIN_RESET_TIME = 30000;
    private static final String TAG = "OppoBaseBatteryService";
    private int last_batTemp = -1;
    private int last_level = -1;
    private int last_phoneTemp = -1;
    private ActivityManagerInternal mActivityManagerInternal;
    private int mBatteryHwStatus;
    private int mBatteryNotifyCode;
    private Context mContext;
    private final Handler mExtHandler;
    private final Handler mHandler;
    private int mLastBatteryNotifyCode;
    private int mLastOtgOnline;
    protected int mLastPhoneTemp;
    private final Runnable mNotifyTempChanged = new Runnable() {
        /* class com.android.server.OppoBaseBatteryService.AnonymousClass3 */

        public void run() {
            if (OppoBaseBatteryService.this.mOppoThermalService != null && OppoBaseBatteryService.this.mOppoThermalService.isFeatureOn()) {
                OppoBaseBatteryService.this.mOppoThermalService.update();
                OppoBaseBatteryService oppoBaseBatteryService = OppoBaseBatteryService.this;
                oppoBaseBatteryService.mPhoneTemp = oppoBaseBatteryService.mOppoThermalService.getPhoneTemp(0);
            }
        }
    };
    public OppoBatteryService mOppoBatteryService;
    private OppoBatteryStatsInternal mOppoBatteryStatsInt;
    private OppoSysStateManagerInternal mOppoSysStateManagerInternal;
    public OppoThermalService mOppoThermalService;
    private int mOtgOnline;
    protected int mPhoneTemp;
    protected int mTempLastPlugType = -1;
    private long reset_time = 1;

    /* access modifiers changed from: protected */
    public abstract OppoBaseLed getLedInstance();

    public OppoBaseBatteryService(Context context) {
        super(context);
        this.mContext = context;
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mHandler = new Handler(true);
        this.mOppoBatteryService = new OppoBatteryService(context, this, this.mActivityManagerInternal);
        this.mOppoThermalService = new OppoThermalService(context);
        OppoSysStateManager.getInstance();
        this.mOppoSysStateManagerInternal = (OppoSysStateManagerInternal) LocalServices.getService(OppoSysStateManagerInternal.class);
        HandlerThread handlerThreadtemp = new HandlerThread("getTemperature");
        handlerThreadtemp.start();
        this.mExtHandler = new Handler(handlerThreadtemp.getLooper());
    }

    /* access modifiers changed from: protected */
    public OppoBatteryStatsInternal getBatteryStatsInternal() {
        if (this.mOppoBatteryStatsInt == null) {
            this.mOppoBatteryStatsInt = (OppoBatteryStatsInternal) LocalServices.getService(OppoBatteryStatsInternal.class);
        }
        return this.mOppoBatteryStatsInt;
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            OppoBaseLed led = getLedInstance();
            if (led != null) {
                Slog.d(TAG, "onBootPhase PHASE_SYSTEM_SERVICES_READY: inform led systemReady.");
                led.systemReady();
                return;
            }
            Slog.e(TAG, "onBootPhase PHASE_SYSTEM_SERVICES_READY: led is uninit!");
        } else if (phase == 550) {
            this.mOppoThermalService.systemReady();
        }
    }

    private void getChargeStats(final int plugType, int lastPlugType, HealthInfo healthInfo, boolean fastChange, int phoneTmp) {
        if (plugType != lastPlugType) {
            final Intent plugged_changed_intent = new Intent("oppo.intent.action.BATTERY_PLUGGED_CHANGED");
            plugged_changed_intent.putExtra("plugged", plugType);
            plugged_changed_intent.putExtra("FCC", healthInfo.batteryFullCharge);
            plugged_changed_intent.putExtra("phonetemp", phoneTmp);
            plugged_changed_intent.putExtra("batterytemp", healthInfo.batteryTemperature);
            plugged_changed_intent.putExtra("level", healthInfo.batteryLevel);
            plugged_changed_intent.putExtra("charge_counter", healthInfo.batteryCycleCount);
            if (this.mActivityManagerInternal.isSystemReady()) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.OppoBaseBatteryService.AnonymousClass1 */

                    public void run() {
                        if (plugType != 0) {
                            long cur_time = SystemClock.elapsedRealtime();
                            if (cur_time - OppoBaseBatteryService.this.reset_time > 30000) {
                                OppoBaseBatteryService.this.reset_time = cur_time;
                                OppoBaseBatteryService.this.getBatteryStatsInternal().restOpppBatteryStatsImpl();
                                Slog.d(OppoBaseBatteryService.TAG, "reset oppo battery stats");
                            }
                        } else if (OppoBaseBatteryService.this.reset_time != -1 && SystemClock.elapsedRealtime() - OppoBaseBatteryService.this.reset_time > 600000) {
                            List<String> list0 = OppoBaseBatteryService.this.getBatteryStatsInternal().getUidPowerListImpl();
                            List<String> list1 = OppoBaseBatteryService.this.getBatteryStatsInternal().getUid0ProcessListImpl();
                            List<String> list2 = OppoBaseBatteryService.this.getBatteryStatsInternal().getUid1kProcessListImpl();
                            if (list0 != null) {
                                for (int i = 0; i < list0.size(); i++) {
                                    Intent intent = plugged_changed_intent;
                                    intent.putExtra("UidTop" + i, list0.get(i));
                                }
                                if (list1 != null) {
                                    for (int i2 = 0; i2 < list1.size(); i2++) {
                                        Intent intent2 = plugged_changed_intent;
                                        intent2.putExtra("Uid0PrcTop" + i2, list1.get(i2));
                                    }
                                } else {
                                    Slog.d(OppoBaseBatteryService.TAG, "list1 == null");
                                }
                                if (list2 != null) {
                                    for (int i3 = 0; i3 < list2.size(); i3++) {
                                        Intent intent3 = plugged_changed_intent;
                                        intent3.putExtra("Uid1kPrcTop" + i3, list2.get(i3));
                                    }
                                } else {
                                    Slog.d(OppoBaseBatteryService.TAG, "list2 == null");
                                }
                            } else {
                                Slog.d(OppoBaseBatteryService.TAG, "list0 == null");
                            }
                            Slog.d(OppoBaseBatteryService.TAG, "get battery stats 111");
                        }
                        plugged_changed_intent.putExtra("Version", 2);
                        plugged_changed_intent.setPackage("com.coloros.oppoguardelf");
                        Slog.d(OppoBaseBatteryService.TAG, "send broadcast : oppo.intent.action.BATTERY_PLUGGED_CHANGED : plugType = " + plugType);
                        OppoBaseBatteryService.this.mContext.sendBroadcast(plugged_changed_intent, "oppo.permission.OPPO_COMPONENT_SAFE");
                    }
                });
            }
        } else if (plugType == 0) {
        } else {
            if (this.last_batTemp != healthInfo.batteryTemperature || this.last_level != healthInfo.batteryLevel) {
                this.last_batTemp = healthInfo.batteryTemperature;
                this.last_level = healthInfo.batteryLevel;
                final Intent data_update_intent = new Intent("oppo.intent.action.BATTERY_DATA_UPDATE");
                data_update_intent.putExtra("phonetemp", phoneTmp);
                data_update_intent.putExtra("batterytemp", healthInfo.batteryTemperature);
                data_update_intent.putExtra("level", healthInfo.batteryLevel);
                data_update_intent.putExtra("fastCharger", fastChange);
                data_update_intent.setPackage("com.coloros.oppoguardelf");
                if (this.mActivityManagerInternal.isSystemReady()) {
                    this.mHandler.post(new Runnable() {
                        /* class com.android.server.OppoBaseBatteryService.AnonymousClass2 */

                        public void run() {
                            Slog.d(OppoBaseBatteryService.TAG, "send broadcast : oppo.intent.action.BATTERY_DATA_UPDATE");
                            OppoBaseBatteryService.this.mContext.sendBroadcast(data_update_intent, "oppo.permission.OPPO_COMPONENT_SAFE");
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void getChargeStats(int plugType, int lastPlugType, HealthInfo healthInfo) {
        getChargeStats(plugType, lastPlugType, healthInfo, false, 0);
    }

    /* access modifiers changed from: protected */
    public boolean dumpInternalBase(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mOppoBatteryService.dynamicallyConfigBatteryServiceLogTag(fd, pw, args)) {
            return false;
        }
        this.mOppoBatteryService.dumpAddition(fd, pw, args);
        this.mOppoBatteryService.printArgs(args);
        if (args != null && args.length != 0 && !"-a".equals(args[0])) {
            return true;
        }
        this.mOppoBatteryService.printOppoBatteryFeature(pw);
        pw.println("  PhoneTemp: " + this.mOppoThermalService.getPhoneTemp(0));
        pw.println("  ThermalFeatureOn: " + this.mOppoThermalService.isFeatureOn());
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean ignoreShutdownIfOverTempByOppoLocked() {
        OppoBatteryService oppoBatteryService = this.mOppoBatteryService;
        if (oppoBatteryService != null) {
            return oppoBatteryService.ignoreShutdownIfOverTempByOppoLocked();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleScreenState(boolean screenon) {
        OppoBaseLed led = getLedInstance();
        if (led != null) {
            led.handleScreenState(screenon);
        }
    }

    /* access modifiers changed from: protected */
    public void setDebugSwitchState(boolean on) {
        DEBUG_PANIC = on;
        OppoBaseLed led = getLedInstance();
        if (led != null) {
            led.setDebugSwitch(on);
        }
    }

    /* access modifiers changed from: protected */
    public void updateBatteryService() {
        OppoBatteryService oppoBatteryService = this.mOppoBatteryService;
        if (oppoBatteryService != null) {
            oppoBatteryService.native_update();
            notifyTempChanged();
            this.mOtgOnline = this.mOppoBatteryService.mOtgOnline;
            this.mBatteryNotifyCode = this.mOppoBatteryService.mBatteryNotifyCode;
            this.mBatteryHwStatus = this.mOppoBatteryService.mBatteryHwStatus;
        }
    }

    /* access modifiers changed from: protected */
    public void saveLastStatsAfterValuesChanged() {
        this.mLastBatteryNotifyCode = this.mBatteryNotifyCode;
        this.mLastOtgOnline = this.mOtgOnline;
        this.mLastPhoneTemp = this.mPhoneTemp;
        OppoBatteryService oppoBatteryService = this.mOppoBatteryService;
        oppoBatteryService.mBattCbStatus = oppoBatteryService.mLastBattCbStatus;
    }

    /* access modifiers changed from: protected */
    public void processValuesForOppoLocked(boolean force, int plugType, HealthInfo healthInfo) {
        if (healthInfo == null) {
            Slog.e(TAG, "processValuesForOppoLocked, healthInfo empty!");
            return;
        }
        this.mOppoBatteryService.processOppoBatteryPluggedChanedLocked(plugType, this.mTempLastPlugType, healthInfo);
        getChargeStats(plugType, this.mTempLastPlugType, healthInfo, this.mOppoBatteryService.mChargeFastCharger, this.mOppoThermalService.getPhoneTemp(0));
        if (this.mTempLastPlugType != plugType) {
            this.mTempLastPlugType = plugType;
        }
        this.mOppoBatteryService.processOppoBatteryHwStatusChanedLocked();
        if (this.mOppoBatteryService.mBatteryRealtimeCapacity == -1) {
            OppoBatteryService oppoBatteryService = this.mOppoBatteryService;
            oppoBatteryService.mBatteryRealtimeCapacity = oppoBatteryService.mBatteryFcc * healthInfo.batteryLevel;
        }
        setThermalState(new OppoThermalState(plugType, this.mOppoBatteryService.mBatteryFcc, this.mOppoBatteryService.mBatteryRealtimeCapacity, this.mOppoThermalService.getPhoneTemp(0), this.mOppoThermalService.getPhoneTemp(1), this.mOppoThermalService.getPhoneTemp(2), this.mOppoThermalService.getPhoneTemp(3), this.mOppoBatteryService.mFast2Normal, this.mOppoBatteryService.mChargeIdVoltage, this.mOppoBatteryService.mChargeFastCharger, this.mOppoBatteryService.mLastBatteryCurrent, healthInfo.batteryLevel, healthInfo.batteryTemperature));
        this.mOppoBatteryService.processAdditionalValuesLocked(healthInfo.batteryTemperature, plugType, healthInfo.batteryLevel);
    }

    /* access modifiers changed from: protected */
    public void setThermalState(OppoThermalState thermalState) {
        getBatteryStatsInternal().setThermalStateImpl(thermalState);
    }

    /* access modifiers changed from: protected */
    public boolean shouldUpdateChargingState(int batteryTemperature, int lastBatteryTemperature) {
        boolean shouldUpdate = false;
        boolean shouldUpdate2 = (this.mBatteryNotifyCode == this.mLastBatteryNotifyCode && this.mOtgOnline == this.mLastOtgOnline && batteryTemperature / 10 == lastBatteryTemperature / 10 && this.mPhoneTemp / 10 == this.mLastPhoneTemp / 10) ? false : true;
        boolean isWirelessChargeStateChanged = this.mOppoBatteryService.isWirelessStatChange();
        if (shouldUpdate2 || isWirelessChargeStateChanged) {
            shouldUpdate = true;
        }
        return shouldUpdate;
    }

    /* access modifiers changed from: protected */
    public void onPlugChangedForOppoSysStateManager(int plugType) {
        OppoSysStateManagerInternal oppoSysStateManagerInternal = this.mOppoSysStateManagerInternal;
        if (oppoSysStateManagerInternal != null) {
            oppoSysStateManagerInternal.onPlugChanged(plugType);
        } else {
            Slog.d(TAG, "mOppoSysStateManagerInternal is null!!!");
        }
    }

    /* access modifiers changed from: protected */
    public void appendFlagToStatusIntent(Intent statusIntent, int flag) {
        statusIntent.addFlags(flag);
    }

    /* access modifiers changed from: protected */
    public void appendExtraToBatteryStatusChangedIntend(Intent intent) {
        intent.putExtra("notifycode", this.mBatteryNotifyCode);
        intent.putExtra("otgonline", this.mOtgOnline);
        intent.putExtra("phoneTemp", this.mPhoneTemp);
        intent.putExtra("wireless_reverse_chg_type", this.mOppoBatteryService.mWirelessReserve);
        intent.putExtra("wireless_deviated_chg_type", this.mOppoBatteryService.mWirelessDeviated);
        intent.putExtra("battery_charge_balance_type", this.mOppoBatteryService.mBattCbStatus);
        intent.putExtra("battery_now_voltage_type", this.mOppoBatteryService.mVoltageNow);
        intent.putExtra("battery_min_voltage_type", this.mOppoBatteryService.mVoltageMin);
        intent.putExtra("flashTemp", this.mOppoThermalService.getFlashThermTemp());
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.power.thermal.control")) {
            intent.putExtra("battery_quiet_therm_type", this.mOppoThermalService.getQuietThermTemp());
            intent.putExtra("environment_temp_type", this.mOppoThermalService.getEnvironmentTempType());
        }
    }

    /* access modifiers changed from: protected */
    public String getBatteryStatusStrForDebug() {
        return ", notify code:" + this.mBatteryNotifyCode + ", otg online:" + this.mOtgOnline + ", mPhoneTemp:" + this.mPhoneTemp;
    }

    /* access modifiers changed from: protected */
    public void notifyTempChanged() {
        if (!this.mOppoThermalService.isUpdateTempAsync()) {
            OppoThermalService oppoThermalService = this.mOppoThermalService;
            if (oppoThermalService != null && oppoThermalService.isFeatureOn()) {
                this.mOppoThermalService.update();
                this.mPhoneTemp = this.mOppoThermalService.getPhoneTemp(0);
            }
        } else if (!this.mExtHandler.hasCallbacks(this.mNotifyTempChanged)) {
            this.mExtHandler.post(this.mNotifyTempChanged);
        }
    }
}
