package com.android.server.am;

import android.bluetooth.BluetoothActivityEnergyInfo;
import android.content.Context;
import android.content.OppoBatteryStatsInternal;
import android.net.wifi.WifiActivityEnergyInfo;
import android.os.Handler;
import android.os.OppoThermalState;
import android.os.SystemClock;
import android.os.UserManagerInternal;
import android.os.WorkSource;
import android.telephony.ModemActivityInfo;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.OppoBaseBatteryStatsImpl;
import com.android.internal.os.OppoBatteryStatsImpl;
import com.android.internal.os.PowerProfile;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.coloros.OppoSysStateManagerInternal;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class OppoBatteryStatsService extends BatteryStatsService {
    private static boolean DBG = false;
    private static String TAG = "OppoBatteryStatsService";
    private final Context mContext;
    final OppoBatteryStatsImpl mOppoStats;
    private OppoSysStateManagerInternal mOppoSysStateManagerInternal;
    final OppoBatteryStatsImpl.UserInfoProvider mOppoUserManagerUserInfoProvider = new OppoBatteryStatsImpl.UserInfoProvider() {
        /* class com.android.server.am.OppoBatteryStatsService.AnonymousClass1 */
        private UserManagerInternal umi;

        public int[] getUserIds() {
            if (this.umi == null) {
                this.umi = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
            }
            UserManagerInternal userManagerInternal = this.umi;
            if (userManagerInternal != null) {
                return userManagerInternal.getUserIds();
            }
            return null;
        }
    };
    final OppoBatteryExternalStatsWorker mOppoWorker;

    OppoBatteryStatsService(Context context, File systemDir, Handler handler) {
        super(context, systemDir, handler);
        this.mContext = context;
        this.mOppoStats = new OppoBatteryStatsImpl(systemDir, handler, this.mOppoUserManagerUserInfoProvider);
        this.mOppoWorker = new OppoBatteryExternalStatsWorker(context, this.mOppoStats);
        this.mOppoStats.setExternalStatsSyncLocked(this.mOppoWorker);
        this.mOppoStats.setRadioScanningTimeoutLocked(((long) this.mContext.getResources().getInteger(17694877)) * 1000);
        this.mOppoStats.setPowerProfileLocked(new PowerProfile(context));
        registerOppoBatteryStatsInternalImpl();
    }

    @Override // com.android.server.am.BatteryStatsService
    public void initPowerManagement() {
        super.initPowerManagement();
        OppoSysStateManager.getInstance();
        this.mOppoSysStateManagerInternal = (OppoSysStateManagerInternal) LocalServices.getService(OppoSysStateManagerInternal.class);
    }

    public void registerOppoBatteryStatsInternalImpl() {
        LocalServices.addService(OppoBatteryStatsInternal.class, new OppoBatteryStatsInternalImpl());
    }

    /* access modifiers changed from: private */
    public class OppoBatteryStatsInternalImpl extends OppoBatteryStatsInternal {
        private OppoBatteryStatsInternalImpl() {
        }

        public List<String> getUidPowerListImpl() {
            return OppoBatteryStatsService.this.getUidPowerList();
        }

        public List<String> getUid0ProcessListImpl() {
            return OppoBatteryStatsService.this.getUid0ProcessList();
        }

        public List<String> getUid1kProcessListImpl() {
            return OppoBatteryStatsService.this.getUid1kProcessList();
        }

        public void restOpppBatteryStatsImpl() {
            OppoBatteryStatsService.this.restOpppBatteryStats();
        }

        public void setThermalStateImpl(OppoThermalState thermalState) {
            if (OppoBatteryStatsService.DBG) {
                Slog.d(OppoBatteryStatsService.TAG, "InternalImpl:setThermalStateImpl");
            }
            OppoBatteryStatsService.this.setThermalStateInternal(thermalState);
        }

        public void setThermalConfigImpl() {
            if (OppoBatteryStatsService.DBG) {
                Slog.d(OppoBatteryStatsService.TAG, "InternalImpl:setThermalConfig");
            }
            OppoBatteryStatsService.this.setThermalConfigInternal();
        }

        public void noteScreenBrightnessModeChangedImpl(boolean isAuto) {
            if (OppoBatteryStatsService.DBG) {
                Slog.d(OppoBatteryStatsService.TAG, "InternalImpl:noteScreenBrightnessModeChanged");
            }
            OppoBatteryStatsService.this.noteScreenBrightnessModeChangedInternal(isAuto);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void systemServicesReady() {
        super.systemServicesReady();
        this.mOppoStats.systemServicesReady(this.mContext);
    }

    private static void awaitUninterruptibly(Future<?> future) {
        while (true) {
            try {
                future.get();
                return;
            } catch (ExecutionException e) {
                return;
            } catch (InterruptedException e2) {
            }
        }
    }

    private void syncStats(String reason, int flags) {
        awaitUninterruptibly(this.mOppoWorker.scheduleSync(reason, flags));
    }

    @Override // com.android.server.am.BatteryStatsService
    public void shutdown() {
        super.shutdown();
        syncStats("shutdown", 31);
        synchronized (this.mOppoStats) {
            this.mOppoStats.shutdownLocked();
        }
        this.mOppoWorker.shutdown();
    }

    public OppoBatteryStatsImpl getActiveOppoStatistics() {
        return this.mOppoStats;
    }

    @Override // com.android.server.am.BatteryStatsService
    public void scheduleWriteToDisk() {
        super.scheduleWriteToDisk();
        this.mOppoWorker.scheduleWrite();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void removeUid(int uid) {
        super.removeUid(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.removeUidStatsLocked(uid);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void onCleanupUser(int userId) {
        super.onCleanupUser(userId);
        synchronized (this.mOppoStats) {
            this.mOppoStats.onCleanupUserLocked(userId);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void onUserRemoved(int userId) {
        super.onUserRemoved(userId);
        synchronized (this.mOppoStats) {
            this.mOppoStats.onUserRemovedLocked(userId);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void addIsolatedUid(int isolatedUid, int appUid) {
        super.addIsolatedUid(isolatedUid, appUid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.addIsolatedUidLocked(isolatedUid, appUid);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void removeIsolatedUid(int isolatedUid, int appUid) {
        super.removeIsolatedUid(isolatedUid, appUid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.scheduleRemoveIsolatedUidLocked(isolatedUid, appUid);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void noteProcessStart(String name, int uid) {
        super.noteProcessStart(name, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteProcessStartLocked(name, uid);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void noteProcessCrash(String name, int uid) {
        super.noteProcessCrash(name, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteProcessCrashLocked(name, uid);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void noteProcessAnr(String name, int uid) {
        super.noteProcessAnr(name, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteProcessAnrLocked(name, uid);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void noteProcessFinish(String name, int uid) {
        super.noteProcessFinish(name, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteProcessFinishLocked(name, uid);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void noteUidProcessState(int uid, int state) {
        super.noteUidProcessState(uid, state);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteUidProcessStateLocked(uid, state);
        }
    }

    public boolean isOppoStatsCharging() {
        boolean isCharging;
        synchronized (this.mOppoStats) {
            isCharging = this.mOppoStats.isCharging();
        }
        return isCharging;
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteEvent(int code, String name, int uid) {
        super.noteEvent(code, name, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteEventLocked(code, name, uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteSyncStart(String name, int uid) {
        super.noteSyncStart(name, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteSyncStartLocked(name, uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteSyncFinish(String name, int uid) {
        super.noteSyncFinish(name, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteSyncFinishLocked(name, uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteJobStart(String name, int uid, int standbyBucket, int jobid) {
        super.noteJobStart(name, uid, standbyBucket, jobid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteJobStartLocked(name, uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteJobFinish(String name, int uid, int stopReason, int standbyBucket, int jobid) {
        super.noteJobFinish(name, uid, stopReason, standbyBucket, jobid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteJobFinishLocked(name, uid, stopReason);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.am.BatteryStatsService
    public void noteJobsDeferred(int uid, int numDeferred, long sinceLast) {
        super.noteJobsDeferred(uid, numDeferred, sinceLast);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteJobsDeferredLocked(uid, numDeferred, sinceLast);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWakupAlarm(String name, int uid, WorkSource workSource, String tag) {
        super.noteWakupAlarm(name, uid, workSource, tag);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWakupAlarmLocked(name, uid, workSource, tag);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteAlarmStart(String name, WorkSource workSource, int uid) {
        super.noteAlarmStart(name, workSource, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteAlarmStartLocked(name, workSource, uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteAlarmFinish(String name, WorkSource workSource, int uid) {
        super.noteAlarmFinish(name, workSource, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteAlarmFinishLocked(name, workSource, uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStartWakelock(int uid, int pid, String name, String historyName, int type, boolean unimportantForLogging) {
        super.noteStartWakelock(uid, pid, name, historyName, type, unimportantForLogging);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteStartWakeLocked(uid, pid, (WorkSource.WorkChain) null, name, historyName, type, unimportantForLogging, SystemClock.elapsedRealtime(), SystemClock.uptimeMillis());
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStopWakelock(int uid, int pid, String name, String historyName, int type) {
        super.noteStopWakelock(uid, pid, name, historyName, type);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteStopWakeLocked(uid, pid, (WorkSource.WorkChain) null, name, historyName, type, SystemClock.elapsedRealtime(), SystemClock.uptimeMillis());
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStartWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type, boolean unimportantForLogging) {
        super.noteStartWakelockFromSource(ws, pid, name, historyName, type, unimportantForLogging);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteStartWakeFromSourceLocked(ws, pid, name, historyName, type, unimportantForLogging);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteChangeWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type, WorkSource newWs, int newPid, String newName, String newHistoryName, int newType, boolean newUnimportantForLogging) {
        super.noteChangeWakelockFromSource(ws, pid, name, historyName, type, newWs, newPid, newName, newHistoryName, newType, newUnimportantForLogging);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteChangeWakelockFromSourceLocked(ws, pid, name, historyName, type, newWs, newPid, newName, newHistoryName, newType, newUnimportantForLogging);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStopWakelockFromSource(WorkSource ws, int pid, String name, String historyName, int type) {
        super.noteStopWakelockFromSource(ws, pid, name, historyName, type);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteStopWakeFromSourceLocked(ws, pid, name, historyName, type);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteLongPartialWakelockStart(String name, String historyName, int uid) {
        super.noteLongPartialWakelockStart(name, historyName, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteLongPartialWakelockStart(name, historyName, uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteLongPartialWakelockStartFromSource(String name, String historyName, WorkSource workSource) {
        super.noteLongPartialWakelockStartFromSource(name, historyName, workSource);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteLongPartialWakelockStartFromSource(name, historyName, workSource);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteLongPartialWakelockFinish(String name, String historyName, int uid) {
        super.noteLongPartialWakelockFinish(name, historyName, uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteLongPartialWakelockFinish(name, historyName, uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteLongPartialWakelockFinishFromSource(String name, String historyName, WorkSource workSource) {
        super.noteLongPartialWakelockFinishFromSource(name, historyName, workSource);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteLongPartialWakelockFinishFromSource(name, historyName, workSource);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStartSensor(int uid, int sensor) {
        super.noteStartSensor(uid, sensor);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteStartSensorLocked(uid, sensor);
        }
        this.mOppoSysStateManagerInternal.noteStartSensor(uid, sensor);
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStopSensor(int uid, int sensor) {
        super.noteStopSensor(uid, sensor);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteStopSensorLocked(uid, sensor);
        }
        this.mOppoSysStateManagerInternal.noteStopSensor(uid, sensor);
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteVibratorOn(int uid, long durationMillis) {
        super.noteVibratorOn(uid, durationMillis);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteVibratorOnLocked(uid, durationMillis);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteVibratorOff(int uid) {
        super.noteVibratorOff(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteVibratorOffLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteGpsChanged(WorkSource oldWs, WorkSource newWs) {
        super.noteGpsChanged(oldWs, newWs);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteGpsChangedLocked(oldWs, newWs);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteGpsSignalQuality(int signalLevel) {
        super.noteGpsSignalQuality(signalLevel);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteGpsSignalQualityLocked(signalLevel);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteScreenState(int state) {
        super.noteScreenState(state);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteScreenStateLocked(state);
        }
        if (DBG) {
            Slog.d(TAG, "end noteScreenState");
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteScreenBrightness(int brightness) {
        super.noteScreenBrightness(brightness);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteScreenBrightnessLocked(brightness);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteUserActivity(int uid, int event) {
        super.noteUserActivity(uid, event);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteUserActivityLocked(uid, event);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWakeUp(String reason, int reasonUid) {
        super.noteWakeUp(reason, reasonUid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWakeUpLocked(reason, reasonUid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteInteractive(boolean interactive) {
        super.noteInteractive(interactive);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteInteractiveLocked(interactive);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteConnectivityChanged(int type, String extra) {
        super.noteConnectivityChanged(type, extra);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteConnectivityChangedLocked(type, extra);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteMobileRadioPowerState(int powerState, long timestampNs, int uid) {
        boolean updateOppoStats;
        super.noteMobileRadioPowerState(powerState, timestampNs, uid);
        synchronized (this.mOppoStats) {
            updateOppoStats = this.mOppoStats.noteMobileRadioPowerStateLocked(powerState, timestampNs, uid);
        }
        if (updateOppoStats) {
            this.mOppoWorker.scheduleSync("modem-data", 4);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void notePhoneOn() {
        super.notePhoneOn();
        synchronized (this.mOppoStats) {
            this.mOppoStats.notePhoneOnLocked();
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void notePhoneOff() {
        super.notePhoneOff();
        synchronized (this.mOppoStats) {
            this.mOppoStats.notePhoneOffLocked();
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void notePhoneSignalStrength(SignalStrength signalStrength) {
        super.notePhoneSignalStrength(signalStrength);
        synchronized (this.mOppoStats) {
            this.mOppoStats.notePhoneSignalStrengthLocked(signalStrength);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void notePhoneDataConnectionState(int dataType, boolean hasData) {
        super.notePhoneDataConnectionState(dataType, hasData);
        synchronized (this.mOppoStats) {
            this.mOppoStats.notePhoneDataConnectionStateLocked(dataType, hasData);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void notePhoneState(int state) {
        super.notePhoneState(state);
        int simState = TelephonyManager.getDefault().getSimState();
        synchronized (this.mOppoStats) {
            this.mOppoStats.notePhoneStateLocked(state, simState);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiOn() {
        super.noteWifiOn();
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiOnLocked();
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiOff() {
        super.noteWifiOff();
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiOffLocked();
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStartAudio(int uid) {
        super.noteStartAudio(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteAudioOnLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStopAudio(int uid) {
        super.noteStopAudio(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteAudioOffLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStartVideo(int uid) {
        super.noteStartVideo(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteVideoOnLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStopVideo(int uid) {
        super.noteStopVideo(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteVideoOffLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteResetAudio() {
        super.noteResetAudio();
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteResetAudioLocked();
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteResetVideo() {
        super.noteResetVideo();
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteResetVideoLocked();
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteFlashlightOn(int uid) {
        super.noteFlashlightOn(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteFlashlightOnLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteFlashlightOff(int uid) {
        super.noteFlashlightOff(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteFlashlightOffLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStartCamera(int uid) {
        super.noteStartCamera(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteCameraOnLocked(uid);
        }
        this.mOppoSysStateManagerInternal.noteCameraOnOff(uid, true);
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteStopCamera(int uid) {
        super.noteStopCamera(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteCameraOffLocked(uid);
        }
        this.mOppoSysStateManagerInternal.noteCameraOnOff(uid, false);
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteResetCamera() {
        super.noteResetCamera();
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteResetCameraLocked();
        }
        this.mOppoSysStateManagerInternal.noteCameraReset();
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteResetFlashlight() {
        super.noteResetFlashlight();
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteResetFlashlightLocked();
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiRadioPowerState(int powerState, long tsNanos, int uid) {
        String type;
        super.noteWifiRadioPowerState(powerState, tsNanos, uid);
        synchronized (this.mOppoStats) {
            if (this.mOppoStats.isOnBattery()) {
                if (powerState != 3) {
                    if (powerState != 2) {
                        type = "inactive";
                        OppoBatteryExternalStatsWorker oppoBatteryExternalStatsWorker = this.mOppoWorker;
                        oppoBatteryExternalStatsWorker.scheduleSync("wifi-data: " + type, 2);
                    }
                }
                type = "active";
                OppoBatteryExternalStatsWorker oppoBatteryExternalStatsWorker2 = this.mOppoWorker;
                oppoBatteryExternalStatsWorker2.scheduleSync("wifi-data: " + type, 2);
            }
            this.mOppoStats.noteWifiRadioPowerState(powerState, tsNanos, uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiRunning(WorkSource ws) {
        super.noteWifiRunning(ws);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiRunningLocked(ws);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiRunningChanged(WorkSource oldWs, WorkSource newWs) {
        super.noteWifiRunningChanged(oldWs, newWs);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiRunningChangedLocked(oldWs, newWs);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiStopped(WorkSource ws) {
        super.noteWifiStopped(ws);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiStoppedLocked(ws);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiState(int wifiState, String accessPoint) {
        super.noteWifiState(wifiState, accessPoint);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiStateLocked(wifiState, accessPoint);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiSupplicantStateChanged(int supplState, boolean failedAuth) {
        super.noteWifiSupplicantStateChanged(supplState, failedAuth);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiSupplicantStateChangedLocked(supplState, failedAuth);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiRssiChanged(int newRssi) {
        super.noteWifiRssiChanged(newRssi);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiRssiChangedLocked(newRssi);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteFullWifiLockAcquired(int uid) {
        super.noteFullWifiLockAcquired(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteFullWifiLockAcquiredLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteFullWifiLockReleased(int uid) {
        super.noteFullWifiLockReleased(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteFullWifiLockReleasedLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiScanStarted(int uid) {
        super.noteWifiScanStarted(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiScanStartedLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiScanStopped(int uid) {
        super.noteWifiScanStopped(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiScanStoppedLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiMulticastEnabled(int uid) {
        super.noteWifiMulticastEnabled(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiMulticastEnabledLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiMulticastDisabled(int uid) {
        super.noteWifiMulticastDisabled(uid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiMulticastDisabledLocked(uid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteFullWifiLockAcquiredFromSource(WorkSource ws) {
        super.noteFullWifiLockAcquiredFromSource(ws);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteFullWifiLockAcquiredFromSourceLocked(ws);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteFullWifiLockReleasedFromSource(WorkSource ws) {
        super.noteFullWifiLockReleasedFromSource(ws);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteFullWifiLockReleasedFromSourceLocked(ws);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiScanStartedFromSource(WorkSource ws) {
        super.noteWifiScanStartedFromSource(ws);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiScanStartedFromSourceLocked(ws);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiScanStoppedFromSource(WorkSource ws) {
        super.noteWifiScanStoppedFromSource(ws);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiScanStoppedFromSourceLocked(ws);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiBatchedScanStartedFromSource(WorkSource ws, int csph) {
        super.noteWifiBatchedScanStartedFromSource(ws, csph);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiBatchedScanStartedFromSourceLocked(ws, csph);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiBatchedScanStoppedFromSource(WorkSource ws) {
        super.noteWifiBatchedScanStoppedFromSource(ws);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteWifiBatchedScanStoppedFromSourceLocked(ws);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteNetworkInterfaceType(String iface, int networkType) {
        super.noteNetworkInterfaceType(iface, networkType);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteNetworkInterfaceTypeLocked(iface, networkType);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteNetworkStatsEnabled() {
        super.noteNetworkStatsEnabled();
        this.mOppoWorker.scheduleSync("network-stats-enabled", 6);
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteDeviceIdleMode(int mode, String activeReason, int activeUid) {
        super.noteDeviceIdleMode(mode, activeReason, activeUid);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteDeviceIdleModeLocked(mode, activeReason, activeUid);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void notePackageInstalled(String pkgName, long versionCode) {
        super.notePackageInstalled(pkgName, versionCode);
        synchronized (this.mOppoStats) {
            this.mOppoStats.notePackageInstalledLocked(pkgName, versionCode);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void notePackageUninstalled(String pkgName) {
        super.notePackageUninstalled(pkgName);
        synchronized (this.mOppoStats) {
            this.mOppoStats.notePackageUninstalledLocked(pkgName);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteBleScanStarted(WorkSource ws, boolean isUnoptimized) {
        super.noteBleScanStarted(ws, isUnoptimized);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteBluetoothScanStartedFromSourceLocked(ws, isUnoptimized);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteBleScanStopped(WorkSource ws, boolean isUnoptimized) {
        super.noteBleScanStopped(ws, isUnoptimized);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteBluetoothScanStoppedFromSourceLocked(ws, isUnoptimized);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteResetBleScan() {
        super.noteResetBleScan();
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteResetBluetoothScanLocked();
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteBleScanResults(WorkSource ws, int numNewResults) {
        super.noteBleScanResults(ws, numNewResults);
        synchronized (this.mOppoStats) {
            this.mOppoStats.noteBluetoothScanResultsFromSourceLocked(ws, numNewResults);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteWifiControllerActivity(WifiActivityEnergyInfo info) {
        super.noteWifiControllerActivity(info);
        synchronized (this.mOppoStats) {
            this.mOppoStats.updateWifiState(info);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteBluetoothControllerActivity(BluetoothActivityEnergyInfo info) {
        super.noteBluetoothControllerActivity(info);
        if (info == null || !info.isValid()) {
            String str = TAG;
            Slog.e(str, "invalid bluetooth data given: " + info);
            return;
        }
        synchronized (this.mOppoStats) {
            this.mOppoStats.updateBluetoothStateLocked(info);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public void noteModemControllerActivity(ModemActivityInfo info) {
        super.noteModemControllerActivity(info);
        if (info == null || !info.isValid()) {
            String str = TAG;
            Slog.e(str, "invalid modem data given: " + info);
            return;
        }
        synchronized (this.mOppoStats) {
            this.mOppoStats.updateMobileRadioState(info);
        }
    }

    @Override // com.android.server.am.BatteryStatsService
    public boolean isOnBattery() {
        return this.mStats.isOnBattery();
    }

    @Override // com.android.server.am.BatteryStatsService
    public void setBatteryState(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh, int chargeFullUAh) {
        super.setBatteryState(status, health, plugType, level, temp, volt, chargeUAh, chargeFullUAh);
        this.mOppoWorker.scheduleRunnable(new Runnable(plugType, status, health, level, temp, volt, chargeUAh, chargeFullUAh) {
            /* class com.android.server.am.$$Lambda$OppoBatteryStatsService$0pywW26xej1hz04u8U3_PqlF4Hk */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ int f$4;
            private final /* synthetic */ int f$5;
            private final /* synthetic */ int f$6;
            private final /* synthetic */ int f$7;
            private final /* synthetic */ int f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            public final void run() {
                OppoBatteryStatsService.this.lambda$setBatteryState$1$OppoBatteryStatsService(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
    }

    public /* synthetic */ void lambda$setBatteryState$1$OppoBatteryStatsService(int plugType, int status, int health, int level, int temp, int volt, int chargeUAh, int chargeFullUAh) {
        synchronized (this.mOppoStats) {
            if (this.mOppoStats.isOnBattery() == OppoBatteryStatsImpl.isOnBattery(plugType, status)) {
                this.mOppoStats.setBatteryStateLocked(this.mContext, status, health, plugType, level, temp, volt, chargeUAh, chargeFullUAh);
                return;
            }
            this.mOppoWorker.scheduleSync("oppoBattery-state", 31);
            this.mOppoWorker.scheduleRunnable(new Runnable(status, health, plugType, level, temp, volt, chargeUAh, chargeFullUAh) {
                /* class com.android.server.am.$$Lambda$OppoBatteryStatsService$MTHCyeNb6Ur7GkLJTPwItbafcM */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ int f$3;
                private final /* synthetic */ int f$4;
                private final /* synthetic */ int f$5;
                private final /* synthetic */ int f$6;
                private final /* synthetic */ int f$7;
                private final /* synthetic */ int f$8;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                    this.f$7 = r8;
                    this.f$8 = r9;
                }

                public final void run() {
                    OppoBatteryStatsService.this.lambda$setBatteryState$0$OppoBatteryStatsService(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
                }
            });
        }
    }

    public /* synthetic */ void lambda$setBatteryState$0$OppoBatteryStatsService(int status, int health, int plugType, int level, int temp, int volt, int chargeUAh, int chargeFullUAh) {
        synchronized (this.mOppoStats) {
            this.mOppoStats.setBatteryStateLocked(this.mContext, status, health, plugType, level, temp, volt, chargeUAh, chargeFullUAh);
        }
    }

    public void setThermalState(OppoThermalState thermalState) {
        setThermalStateInternal(thermalState);
    }

    public List<String> getUidPowerList() {
        return this.mOppoStats.getUidPowerList(this.mContext);
    }

    public List<String> getUid0ProcessList() {
        return this.mOppoStats.getUid0ProcessList();
    }

    public List<String> getUid1kProcessList() {
        return this.mOppoStats.getUid1kProcessList();
    }

    public void restOpppBatteryStats() {
        synchronized (this.mOppoStats) {
            this.mOppoStats.resetAllStatsCmdLocked();
        }
        this.mOppoWorker.scheduleSync("dump", 31);
    }

    private OppoBaseBatteryStatsImpl typeCasting(BatteryStatsImpl stats) {
        return (OppoBaseBatteryStatsImpl) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStatsImpl.class, stats);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setThermalStateInternal(OppoThermalState thermalState) {
        enforceCallingPermission();
        OppoBaseBatteryStatsImpl baseBatteryStatsImpl = typeCasting(this.mStats);
        if (baseBatteryStatsImpl == null) {
            Slog.e(TAG, "setThermalStateInternal, typeCasting mStats failed.");
            return;
        }
        BatteryExternalStatsWorker worker = getStatsWorkerOfSuper();
        if (worker == null) {
            Slog.e(TAG, "fatal exception for get worker failed!", new RuntimeException().fillInStackTrace());
        } else {
            worker.scheduleRunnable(new Runnable(thermalState, baseBatteryStatsImpl, worker) {
                /* class com.android.server.am.$$Lambda$OppoBatteryStatsService$WD8KJrsApzvgcRbu8gPekTi4e90 */
                private final /* synthetic */ OppoThermalState f$1;
                private final /* synthetic */ OppoBaseBatteryStatsImpl f$2;
                private final /* synthetic */ BatteryExternalStatsWorker f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    OppoBatteryStatsService.this.lambda$setThermalStateInternal$3$OppoBatteryStatsService(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    public /* synthetic */ void lambda$setThermalStateInternal$3$OppoBatteryStatsService(OppoThermalState thermalState, OppoBaseBatteryStatsImpl baseBatteryStatsImpl, BatteryExternalStatsWorker worker) {
        synchronized (this.mStats) {
            if (this.mStats.isOnBattery() == (thermalState.getPlugType() == 0)) {
                baseBatteryStatsImpl.setThermalState(thermalState);
                return;
            }
            worker.scheduleSync("battery-state", 31);
            worker.scheduleRunnable(new Runnable(baseBatteryStatsImpl, thermalState) {
                /* class com.android.server.am.$$Lambda$OppoBatteryStatsService$BQ4MIJNK6s6Mix3Nlskcv_Ye34 */
                private final /* synthetic */ OppoBaseBatteryStatsImpl f$1;
                private final /* synthetic */ OppoThermalState f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    OppoBatteryStatsService.this.lambda$setThermalStateInternal$2$OppoBatteryStatsService(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$setThermalStateInternal$2$OppoBatteryStatsService(OppoBaseBatteryStatsImpl baseBatteryStatsImpl, OppoThermalState thermalState) {
        synchronized (this.mStats) {
            Slog.d(TAG, "setThermalStateInternal, now setThermalState process sync external state.");
            baseBatteryStatsImpl.setThermalState(thermalState);
        }
    }

    private BatteryExternalStatsWorker getStatsWorkerOfSuper() {
        return (BatteryExternalStatsWorker) OppoMirrorBatteryStatsService.mWorker.get(this);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setThermalConfigInternal() {
        enforceCallingPermission();
        OppoBaseBatteryStatsImpl baseBatteryStatsImpl = typeCasting(this.mStats);
        if (baseBatteryStatsImpl == null) {
            Slog.e(TAG, "setThermalConfigInternal, typeCasting mStats failed.");
            return;
        }
        synchronized (this.mStats) {
            baseBatteryStatsImpl.setThermalConfig();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void noteScreenBrightnessModeChangedInternal(boolean isAuto) {
        Slog.d(TAG, "noteScreenBrightnessModeChangedInternal");
        OppoBaseBatteryStatsImpl baseBatteryStatsImpl = typeCasting(this.mStats);
        if (baseBatteryStatsImpl == null) {
            Slog.e(TAG, "setThermalConfigInternal, typeCasting mStats failed.");
        } else {
            baseBatteryStatsImpl.noteScreenBrightnessModeChangedLock(isAuto);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.BatteryStatsService
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw)) {
            OppoBaseBatteryStatsImpl oppoBaseBSImpl = typeCasting(this.mStats);
            if (args != null) {
                boolean oppoCheckinHistoryParse = false;
                for (String arg : args) {
                    if ("--oppoCheckin".equals(arg)) {
                        oppoCheckinHistoryParse = true;
                    } else if ("--thermal".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.dumpThemalLocked(pw, -1);
                            }
                        }
                        return;
                    } else if ("--thermalclear".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.clearThermalAllHistory();
                            }
                        }
                        return;
                    } else if (arg.startsWith("--debug:")) {
                        synchronized (this.mStats) {
                            try {
                                int on = Integer.parseInt(arg.split(":")[1]);
                                if (oppoBaseBSImpl != null) {
                                    oppoBaseBSImpl.toggleThermalDebugSwith(pw, on);
                                }
                            } catch (Exception e) {
                                pw.println("Battery set heatthreshold error.");
                            }
                        }
                        return;
                    } else if (arg.startsWith("--update_cpu")) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                try {
                                    oppoBaseBSImpl.updateCpuStatsNow(pw);
                                } catch (Exception e2) {
                                    pw.println("Battery set heatthreshold error.");
                                }
                            }
                        }
                        return;
                    } else if (arg.startsWith("--heatThreshold:")) {
                        synchronized (this.mStats) {
                            String[] thr = arg.split(":");
                            if (oppoBaseBSImpl != null) {
                                try {
                                    oppoBaseBSImpl.setThermalHeatThreshold(pw, Integer.parseInt(thr[1]));
                                } catch (Exception e3) {
                                    pw.println("Battery set heatthreshold error.");
                                }
                            }
                        }
                        return;
                    } else if (arg.startsWith("--heatThreshold")) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.printThermalHeatThreshold(pw);
                            }
                        }
                        return;
                    } else if (arg.startsWith("--heatBetweenTime:")) {
                        synchronized (this.mStats) {
                            String[] thr2 = arg.split(":");
                            if (oppoBaseBSImpl != null) {
                                try {
                                    oppoBaseBSImpl.setHeatBetweenTime(pw, Integer.parseInt(thr2[1]));
                                } catch (Exception e4) {
                                    pw.println("Battery set heatBetweenTime error.");
                                }
                            }
                        }
                        return;
                    } else if (arg.startsWith("--monitorAppLimitTime:")) {
                        synchronized (this.mStats) {
                            String[] thr3 = arg.split(":");
                            if (oppoBaseBSImpl != null) {
                                try {
                                    oppoBaseBSImpl.setMonitorAppLimitTime(pw, Integer.parseInt(thr3[1]));
                                } catch (Exception e5) {
                                    pw.println("Battery set monitorAppLimitTime error.");
                                }
                            }
                        }
                        return;
                    } else if (arg.startsWith("--monitorApp")) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                try {
                                    oppoBaseBSImpl.getMonitorAppLocked(pw);
                                } catch (Exception e6) {
                                    pw.println("Battery dump monitorApp error.");
                                }
                            }
                        }
                        return;
                    } else if ("--thermalraw".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.dumpThemalRawLocked(pw, -1);
                            }
                        }
                        return;
                    } else if ("--thermalrec".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.backupThermalStatsFile();
                            }
                        }
                        return;
                    } else if ("--thermallog".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.backupThermalLogFile();
                            }
                        }
                        return;
                    } else if ("--heatreason".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.dumpThemalHeatDetailLocked(pw);
                            }
                        }
                        return;
                    } else if ("--phonetemp".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.getPhoneTemp(pw);
                            }
                        }
                        return;
                    } else if ("--uploadtemp".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.printThermalUploadTemp(pw);
                            }
                        }
                        return;
                    } else if ("--uploadcharge".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.printChargeMapLocked(pw);
                            }
                        }
                        return;
                    } else if ("--screenoffIdle".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.dumpScreenOffIdleLocked(this.mContext, pw);
                            }
                        }
                        return;
                    } else if ("--oppoLogOn".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.oppoLogSwitch(true);
                            }
                        }
                        pw.println("oppoLog is On!");
                        return;
                    } else if ("--oppoLogOff".equals(arg)) {
                        synchronized (this.mStats) {
                            if (oppoBaseBSImpl != null) {
                                oppoBaseBSImpl.oppoLogSwitch(false);
                            }
                        }
                        pw.println("oppoLog is Off!");
                        return;
                    }
                }
            }
            super.dump(fd, pw, args);
        }
    }
}
