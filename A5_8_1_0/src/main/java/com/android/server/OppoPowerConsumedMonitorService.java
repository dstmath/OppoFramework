package com.android.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoFlashLightManager;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemService;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.os.BackgroundThread;
import com.android.server.AlarmManagerService.AlarmCount;
import com.android.server.AlarmManagerService.LocalService;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.am.OppoProcessManager;
import com.android.server.backup.RefactoredBackupManagerService;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.display.OppoBrightUtils;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.oppo.NetWakeManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import oppo.util.OppoStatistics;

public final class OppoPowerConsumedMonitorService extends SystemService {
    private static final String AOD_ENABLE_IMEDIATE = "Setting_AodEnableImediate";
    private static final String AOD_SETTIME_BEGIN_HOUR = "Setting_AodSetTimeBeginHour";
    private static final String AOD_SETTIME_BEGIN_MIN = "Setting_AodSetTimeBeginMin";
    private static final String AOD_SETTIME_END_HOUR = "Setting_AodSetTimeEndHour";
    private static final String AOD_SETTIME_END_MIN = "Setting_AodSetTimeEndMin";
    private static final String AOD_USER_SETTIME = "Setting_AodUserSetTime";
    private static final String AP_WAKEUP_SOURCE_PATH = "/sys/kernel/wakeup_reasons/ap_resume_reason_stastics";
    private static final String BATTERY_JUMP_SOC = "/sys/class/power_supply/battery/batt_jump_soc";
    private static final String BATTERY_MAGIC_FCC = "/sys/class/power_supply/battery/batt_fcc";
    private static final String BATTERY_REALTIME_CAPACITY = "/sys/class/power_supply/battery/batt_rm";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static boolean DEBUG_PANIC = false;
    private static final String INTERRUPTS_PATH = "/proc/interrupts";
    private static final String KERNEL_WAKELOCK_SOURCE_PATH = "/sys/kernel/wakelock_profiler/active_max";
    private static final String KERNEL_WAKELOCK_TIME_PATH = "/sys/kernel/wakelock_profiler/kernel_time";
    private static final String MODEM_INFO_PATH = "/sys/kernel/wakeup_reasons/modem_resume_reason_stastics";
    static final int MSG_DETECT_AOD_STATUS = 6;
    static final int MSG_DETECT_AUDIO_PROCESS = 4;
    static final int MSG_DETECT_FLASHLIGHT_STATUS = 7;
    static final int MSG_DETECT_MODEM_LOG_STATUS = 5;
    static final int MSG_OVER_CURRENT = 3;
    static final int MSG_SCREEN_OFF = 2;
    static final int MSG_SCREEN_ON = 1;
    static final int MSG_UPDATE_CONFIG = 0;
    private static final long ONE_DAY_IN_MIN = 1440;
    private static final String OPPO_RPM_MASTER_STATS = "/d/oppo_rpm_master_stats";
    private static final String OPPO_RPM_STATS = "/d/oppo_rpm_stats";
    private static final String POWER_LOG_PATH = "data/system/dropbox/powermonitor/";
    private static final String TAG = "OppoPowerMonitor";
    private static final String VIRTUAL_KEY_INTERRUPT_NAME = "VIRTUAL_KEY-eint";
    private static final int VIRTUAL_KEY_INTERRUPT_THRESHOLD = 10;
    private static final String WCN_INFO_PATH = "/sys/kernel/wakeup_reasons/wcn_resume_reason_stastics";
    private static final String WCN_WAKEUP_LOG_PATH = "/proc/wlan_wakeup_log";
    private static boolean mIsReleaseType = false;
    private int CHECK_AUDIO_ACTIVE_COUNT = 5;
    private int CHECK_AUDIO_MAX_COUNT = 10;
    private long MOBILE_DATA_PER_HOUR_THRESHOLD = 2097152;
    private final long ONE_HOUR_MILLS = 3600000;
    private long WIFI_DATA_PER_HOUR_THRESHOLD = 15728640;
    private int checkAudioCount = 0;
    private int mAdspStatsScreenDelta = 0;
    private int mAdspStatsScreenOff = 0;
    private int mAdspStatsScreenOn = 0;
    private long mAdspStatsTimeScreenDelta = 0;
    private long mAdspStatsTimeScreenOff = 0;
    private long mAdspStatsTimeScreenOn = 0;
    private double mAdspSuspendRatio = 0.0d;
    private int mAdspWakeupCount = 0;
    private int mAodBeginHour = -1;
    private int mAodBeginMin = -1;
    private int mAodDurationSetted = 0;
    private int mAodEndHour = -1;
    private int mAodEndMin = -1;
    private double mAodOnDelta = 35.0d;
    private int mAodState = 0;
    private String mApResumeReason = null;
    private List<AlarmCount> mAppsAlarmWakeupTimes = null;
    private int mApssStatsScreenDelta = 0;
    private int mApssStatsScreenOff = 0;
    private int mApssStatsScreenOn = 0;
    private long mApssStatsTimeScreenDelta = 0;
    private long mApssStatsTimeScreenOff = 0;
    private long mApssStatsTimeScreenOn = 0;
    private double mApssSuspendRatio = 0.0d;
    private Map<String, Integer> mAudioProcessMap = new HashMap();
    private AudioProcessRunable mAudioProcessRunable = new AudioProcessRunable(this, null);
    private double mBaseCurrent = 3.7d;
    private int mBatteryCapacity = OppoBrightUtils.HIGH_BRIGHTNESS_LUX_STEP;
    private int mBatteryCapacityDelta = 0;
    private int mBatteryCapacityScreenOff = 0;
    private int mBatteryCapacityScreenOn = 0;
    private int mBatteryFcc = -1;
    private int mBatteryJumpSocDelta = 0;
    private int mBatteryJumpSocScreenOff = 0;
    private int mBatteryJumpSocScreenOn = 0;
    private int mBatteryLevel = 1000;
    private BinderService mBinderService;
    private double mBluetoothDelta = 1.64d;
    private int mBluetoothState = 10;
    private boolean mBootCompleted = false;
    private final BroadcastReceiver mBroadcastReceiver;
    private int mCdspStatsScreenDelta = 0;
    private int mCdspStatsScreenOff = 0;
    private int mCdspStatsScreenOn = 0;
    private long mCdspStatsTimeScreenDelta = 0;
    private long mCdspStatsTimeScreenOff = 0;
    private long mCdspStatsTimeScreenOn = 0;
    private int mChargeBatteryLevel = 0;
    private Long mChargeElapsedRealTime = Long.valueOf(0);
    private final Context mContext;
    private Long mCurBatteryElapsedRealTime = Long.valueOf(0);
    private Long mCurBatteryUpTime = Long.valueOf(0);
    private String mCurrentNetwork = "";
    private String mDataNetworkOperatorName = "";
    private int mDataNetworkState = -1;
    private int mDataNetworkType = 0;
    private int mDischargeBatteryLevel = 0;
    private Long mDischargeElapsedRealTime = Long.valueOf(0);
    private double mDoubleSimCardDelta = 5.5d;
    private boolean mDownloadScene = false;
    private long mElapsedRealTimeDelta = 0;
    private String mExtraInfo = "";
    private double mFlashLightOnDelta = 130.0d;
    private int mFlashLightState = 0;
    private double mFloateDelta = 5.0d;
    private int mFrameworkAlarmWakeupTimes = 0;
    private double mFrameworksBlockedRatio = 0.0d;
    private long mFrameworksBlockedTime = 0;
    private final Handler mHandler;
    private boolean mHasCatchQxdmLog = false;
    private double mHighCurrentThresholdScreenOff = 50.0d;
    private double mHighCurrentThresholdScreenOn = 800.0d;
    private int mHighTemperatureTheshold = 500;
    private double mIgnoreThreshold = 21.6d;
    private boolean mIsDebugMode = false;
    private boolean mIsModemLogOn = false;
    private boolean mIsOverTemperature = false;
    private boolean mIsPowerMonitoring = false;
    private boolean mIsPowered = false;
    private boolean mIsScreenOn = true;
    private boolean mIsSpecialVersion = false;
    private int mIssueType = 0;
    private String mKeepAudioProcess = "";
    private String mKernelMaxWakelockRate = null;
    private String mKernelWakelockReason = null;
    private Long mKernelWakelockTime = Long.valueOf(0);
    private LocalService mLocalAlarmManager = null;
    private final Object mLock = new Object();
    private long mMobileDataDelta = 0;
    private long mMobileIncrementMax = 0;
    private StringBuilder mMobileIncrementMaxProcess = new StringBuilder();
    private long mMobileIncrementMaxUid = 0;
    private String mModemInfo = null;
    private String mModemRilTopMsg = null;
    private String mModemTopWakeUpReason = null;
    private int mModemWakeupCount = 0;
    private int mMpssStatsScreenDelta = 0;
    private int mMpssStatsScreenOff = 0;
    private int mMpssStatsScreenOn = 0;
    private long mMpssStatsTimeScreenDelta = 0;
    private long mMpssStatsTimeScreenOff = 0;
    private long mMpssStatsTimeScreenOn = 0;
    private double mMpssSubSystemExepCurrentThreshold = 200.0d;
    private double mMpssSuspendRatio = 0.0d;
    private boolean mMusicScene = false;
    private String mNetWakeupMaxApp = null;
    private double mNetworkDelta = 5.0d;
    private NetworkStatsFactory mNetworkStatsFactory = null;
    private NetworkStats mNetworkStatsScreenOff = null;
    private NetworkStats mNetworkStatsScreenOn = null;
    private NetworkStats mNetworkStatsSubtract = null;
    private double mNoSimCardDelta = 2.5d;
    private double mPowerLostByAod = 0.0d;
    private double mPowerLostByTelephone = 0.0d;
    private double mPowerLostByWifi = 0.0d;
    private Long mPreBatteryElapsedRealTime = Long.valueOf(0);
    private int mPreBatteryLevel = 0;
    private Long mPreBatteryUpTime = Long.valueOf(0);
    private long mPreMpssStatsTimeScreenDelta = 0;
    private double mRealCurrent = 0.0d;
    private double mRealCurrentWithoutPowerLostKnown = 0.0d;
    private int mRtcAlarmWakeupCount = 0;
    private long mSampleTimeThreshold = 18000000;
    private long mScreenOffCurrentTime = 0;
    private long mScreenOffElapsedRealTime = 0;
    private long mScreenOffUpTime = 0;
    private long mScreenOnCurrentTime = 0;
    private long mScreenOnElapseRealTime = 0;
    private long mScreenOnUpTime = 0;
    private String mSimCard1Name = "";
    private String mSimCard2Name = "";
    private int mSimCardCount = 0;
    private double mSingleSimCardDelta = 3.5d;
    private int mSpsWakeupCount = 0;
    private double mSubSystemExepCurrentThreshold = 500.0d;
    private double mSuspendRatio = 0.0d;
    private double mTargetCurrent = 16.6d;
    private int mTopApResumeCount = 0;
    private String mTopApResumeReasonName = null;
    private List<Entry<String, Long>> mTopBlockedAppList;
    private int mTzStatsScreenDelta = 0;
    private int mTzStatsScreenOff = 0;
    private int mTzStatsScreenOn = 0;
    private long mTzStatsTimeScreenDelta = 0;
    private long mTzStatsTimeScreenOff = 0;
    private long mTzStatsTimeScreenOn = 0;
    private long mUpTimeDelta = 0;
    private String mVersion = "0.01";
    private int mVirtualKeyInterrupts = 0;
    private int mVirtualKeyInterruptsScreenOff = 0;
    private int mVirtualKeyInterruptsScreenOn = 0;
    private int mVlowStatsScreenDelta = 0;
    private int mVlowStatsScreenOff = 0;
    private int mVlowStatsScreenOn = 0;
    private long mVlowStatsTimeScreenDelta = 0;
    private long mVlowStatsTimeScreenOff = 0;
    private long mVlowStatsTimeScreenOn = 0;
    private int mVminStatsScreenDelta = 0;
    private int mVminStatsScreenOff = 0;
    private int mVminStatsScreenOn = 0;
    private long mVminStatsTimeScreenDelta = 0;
    private long mVminStatsTimeScreenOff = 0;
    private long mVminStatsTimeScreenOn = 0;
    private String mWcnInfo = null;
    private int mWcnssWakeupCount = 0;
    private double mWifiBluetoothDelta = 7.4d;
    private long mWifiDataDelta = 0;
    private double mWifiDelta = 5.74d;
    private long mWifiIncrementMax = 0;
    private StringBuilder mWifiIncrementMaxProcess = new StringBuilder();
    private long mWifiIncrementMaxUid = 0;
    private String mWifiName = "";
    private String mWifiPowerEventList = null;
    private int mWifiState = 1;
    private long mWlanBcastDelta = 0;
    private long mWlanBcastScreenOffCount = 0;
    private long mWlanBcastScreenOnCount = 0;
    private long mWlanUcastDelta = 0;
    private long mWlanUcastScreenOffCount = 0;
    private long mWlanUcastScreenOnCount = 0;

    private class AudioProcessRunable implements Runnable {
        /* synthetic */ AudioProcessRunable(OppoPowerConsumedMonitorService this$0, AudioProcessRunable -this1) {
            this();
        }

        private AudioProcessRunable() {
        }

        public void run() {
            String mProcessUsingAudio = OppoPowerConsumedMonitorService.this.getActiveAudioPids();
            if (!TextUtils.isEmpty(mProcessUsingAudio)) {
                String[] pids_confirm = mProcessUsingAudio.split(":");
                for (int i = 0; i < pids_confirm.length; i++) {
                    if (!TextUtils.isEmpty(pids_confirm[i])) {
                        if (OppoPowerConsumedMonitorService.this.mAudioProcessMap.containsKey(pids_confirm[i])) {
                            OppoPowerConsumedMonitorService.this.mAudioProcessMap.put(pids_confirm[i], Integer.valueOf(((Integer) OppoPowerConsumedMonitorService.this.mAudioProcessMap.get(pids_confirm[i])).intValue() + 1));
                        } else {
                            OppoPowerConsumedMonitorService.this.mAudioProcessMap.put(pids_confirm[i], Integer.valueOf(1));
                        }
                    }
                }
            }
            for (Entry<String, Integer> entry : OppoPowerConsumedMonitorService.this.mAudioProcessMap.entrySet()) {
                String key = (String) entry.getKey();
                if (((Integer) entry.getValue()).intValue() > OppoPowerConsumedMonitorService.this.CHECK_AUDIO_ACTIVE_COUNT) {
                    OppoPowerConsumedMonitorService.this.mMusicScene = true;
                    OppoPowerConsumedMonitorService.this.mKeepAudioProcess = OppoPowerConsumedMonitorService.this.getAudioProcess(key);
                    Slog.i(OppoPowerConsumedMonitorService.TAG, "AudioProcessRunable mMusicScene=" + OppoPowerConsumedMonitorService.this.mMusicScene + " mKeepAudioProcess=" + OppoPowerConsumedMonitorService.this.mKeepAudioProcess + " key = " + key);
                    break;
                }
            }
            if (!(OppoPowerConsumedMonitorService.this.mMusicScene || OppoPowerConsumedMonitorService.this.checkAudioCount >= OppoPowerConsumedMonitorService.this.CHECK_AUDIO_MAX_COUNT || (OppoPowerConsumedMonitorService.this.mIsScreenOn ^ 1) == 0)) {
                OppoPowerConsumedMonitorService.this.mHandler.postDelayed(OppoPowerConsumedMonitorService.this.mAudioProcessRunable, RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL);
            }
            OppoPowerConsumedMonitorService oppoPowerConsumedMonitorService = OppoPowerConsumedMonitorService.this;
            oppoPowerConsumedMonitorService.checkAudioCount = oppoPowerConsumedMonitorService.checkAudioCount + 1;
        }
    }

    private final class BinderService extends Binder {
        /* synthetic */ BinderService(OppoPowerConsumedMonitorService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (OppoPowerConsumedMonitorService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump Battery service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else {
                OppoPowerConsumedMonitorService.this.dumpInternal(fd, pw, args);
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, callback, resultReceiver);
        }
    }

    final class MyBroadcastReceiver extends BroadcastReceiver {
        MyBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (OppoPowerConsumedMonitorService.this.mBootCompleted) {
                String action = intent.getAction();
                if (OppoPowerConsumedMonitorService.DEBUG_PANIC || OppoPowerConsumedMonitorService.this.mIsDebugMode) {
                    Slog.d(OppoPowerConsumedMonitorService.TAG, "Receive broadcast " + action);
                }
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    OppoPowerConsumedMonitorService.this.mIsPowerMonitoring = false;
                    OppoPowerConsumedMonitorService.this.mIsScreenOn = true;
                    OppoPowerConsumedMonitorService.this.mHandler.removeCallbacks(OppoPowerConsumedMonitorService.this.mAudioProcessRunable);
                    OppoPowerConsumedMonitorService.this.mHandler.sendMessage(OppoPowerConsumedMonitorService.this.mHandler.obtainMessage(1));
                    if (OppoPowerConsumedMonitorService.this.mHandler.hasMessages(5)) {
                        OppoPowerConsumedMonitorService.this.mHandler.removeMessages(5);
                    }
                    if (OppoPowerConsumedMonitorService.this.mHandler.hasMessages(6)) {
                        OppoPowerConsumedMonitorService.this.mHandler.removeMessages(6);
                    }
                    if (OppoPowerConsumedMonitorService.this.mHandler.hasMessages(7)) {
                        OppoPowerConsumedMonitorService.this.mHandler.removeMessages(7);
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    OppoPowerConsumedMonitorService.this.mIsScreenOn = false;
                    OppoPowerConsumedMonitorService.this.mHandler.sendMessage(OppoPowerConsumedMonitorService.this.mHandler.obtainMessage(2));
                } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                    OppoPowerConsumedMonitorService.this.mBatteryLevel = intent.getIntExtra("level", -1);
                    int batteryTemperature = intent.getIntExtra("temperature", -1);
                    if (batteryTemperature != -1 && batteryTemperature > OppoPowerConsumedMonitorService.this.mHighTemperatureTheshold) {
                        OppoPowerConsumedMonitorService.this.mIsOverTemperature = true;
                    }
                    if (OppoPowerConsumedMonitorService.this.mPreBatteryLevel > OppoPowerConsumedMonitorService.this.mBatteryLevel && (OppoPowerConsumedMonitorService.this.mIsScreenOn ^ 1) != 0 && OppoPowerConsumedMonitorService.this.allowMonitor()) {
                        if (OppoPowerConsumedMonitorService.this.mIsPowerMonitoring) {
                            OppoPowerConsumedMonitorService.this.mCurBatteryElapsedRealTime = Long.valueOf(SystemClock.elapsedRealtime());
                            OppoPowerConsumedMonitorService.this.mCurBatteryUpTime = Long.valueOf(SystemClock.uptimeMillis());
                            if (((double) (((OppoPowerConsumedMonitorService.this.mBatteryCapacity * 10) * 60) * 60)) / ((double) (OppoPowerConsumedMonitorService.this.mCurBatteryElapsedRealTime.longValue() - OppoPowerConsumedMonitorService.this.mPreBatteryElapsedRealTime.longValue())) > OppoPowerConsumedMonitorService.this.mHighCurrentThresholdScreenOff) {
                                OppoPowerConsumedMonitorService.this.getOppoRpmStatsScreenOn();
                                OppoPowerConsumedMonitorService.this.getOppoRpmMasterStatsScreenOn();
                                OppoPowerConsumedMonitorService.this.getOppoRpmStatsDelta();
                                OppoPowerConsumedMonitorService.this.getOppoRpmMasterStatsDelta();
                                boolean mpssNotSuspend = false;
                                if (OppoPowerConsumedMonitorService.this.mPreMpssStatsTimeScreenDelta != OppoPowerConsumedMonitorService.this.mMpssStatsTimeScreenDelta || (OppoPowerConsumedMonitorService.this.mHasCatchQxdmLog ^ 1) == 0) {
                                    OppoPowerConsumedMonitorService.this.mPreMpssStatsTimeScreenDelta = OppoPowerConsumedMonitorService.this.mMpssStatsTimeScreenDelta;
                                } else {
                                    mpssNotSuspend = true;
                                    OppoPowerConsumedMonitorService.this.mHasCatchQxdmLog = true;
                                }
                                final Intent saveLogIntent = new Intent("oppo.intent.action.ACTION_OPPO_STANDBY_SAVE_LOG");
                                saveLogIntent.setPackage("com.oppo.oppopowermonitor");
                                saveLogIntent.putExtra("mpssNotSuspend", mpssNotSuspend);
                                new Thread(new Runnable() {
                                    public void run() {
                                        OppoPowerConsumedMonitorService.this.mContext.sendBroadcast(saveLogIntent);
                                    }
                                }).start();
                            }
                            OppoPowerConsumedMonitorService.this.mPreBatteryElapsedRealTime = OppoPowerConsumedMonitorService.this.mCurBatteryElapsedRealTime;
                            OppoPowerConsumedMonitorService.this.mPreBatteryUpTime = OppoPowerConsumedMonitorService.this.mCurBatteryUpTime;
                        } else {
                            OppoPowerConsumedMonitorService.this.mHasCatchQxdmLog = false;
                            OppoPowerConsumedMonitorService.this.mIsPowerMonitoring = true;
                            OppoPowerConsumedMonitorService.this.mPreBatteryElapsedRealTime = Long.valueOf(SystemClock.elapsedRealtime());
                            OppoPowerConsumedMonitorService.this.mPreBatteryUpTime = Long.valueOf(SystemClock.uptimeMillis());
                        }
                    }
                    OppoPowerConsumedMonitorService.this.mPreBatteryLevel = OppoPowerConsumedMonitorService.this.mBatteryLevel;
                } else if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                    OppoPowerConsumedMonitorService.this.mIsPowered = true;
                    OppoPowerConsumedMonitorService.this.mChargeBatteryLevel = OppoPowerConsumedMonitorService.this.mBatteryLevel;
                    OppoPowerConsumedMonitorService.this.mChargeElapsedRealTime = Long.valueOf(SystemClock.elapsedRealtime());
                    int batteryLost = OppoPowerConsumedMonitorService.this.mDischargeBatteryLevel - OppoPowerConsumedMonitorService.this.mChargeBatteryLevel;
                    Long batteryLifeElapsedRealTime = Long.valueOf(OppoPowerConsumedMonitorService.this.mChargeElapsedRealTime.longValue() - OppoPowerConsumedMonitorService.this.mDischargeElapsedRealTime.longValue());
                    if (batteryLost > 5 && batteryLost < 100) {
                        if (OppoPowerConsumedMonitorService.this.mBatteryFcc > 0 && OppoPowerConsumedMonitorService.this.mBatteryFcc < 9999) {
                            OppoPowerConsumedMonitorService.this.mBatteryCapacity = OppoPowerConsumedMonitorService.this.mBatteryFcc;
                        }
                        OppoPowerConsumedMonitorService.this.reportBatteryLife(batteryLost, batteryLifeElapsedRealTime, OppoPowerConsumedMonitorService.this.mBatteryFcc);
                    }
                } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                    OppoPowerConsumedMonitorService.this.mIsPowered = false;
                    OppoPowerConsumedMonitorService.this.mDischargeBatteryLevel = OppoPowerConsumedMonitorService.this.mBatteryLevel;
                    OppoPowerConsumedMonitorService.this.mDischargeElapsedRealTime = Long.valueOf(SystemClock.elapsedRealtime());
                } else if (!action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                    Slog.d(OppoPowerConsumedMonitorService.TAG, "There is no matched action!");
                }
            }
        }
    }

    final class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            if (OppoPowerConsumedMonitorService.DEBUG_PANIC) {
                Slog.d(OppoPowerConsumedMonitorService.TAG, "handleMessage(" + msg.what + ")");
            }
            switch (msg.what) {
                case 0:
                    OppoPowerConsumedMonitorService.this.onUpdate();
                    return;
                case 1:
                    OppoPowerConsumedMonitorService.this.onScreenOn();
                    return;
                case 2:
                    OppoPowerConsumedMonitorService.this.onScreenOff();
                    return;
                case 5:
                    OppoPowerConsumedMonitorService oppoPowerConsumedMonitorService = OppoPowerConsumedMonitorService.this;
                    if (!(SystemProperties.getBoolean("debug.mdlogger.Running", false) || SystemService.isRunning("diag_mdlog_start"))) {
                        z = SystemService.isRunning("diag_mdlog_nrt");
                    }
                    oppoPowerConsumedMonitorService.mIsModemLogOn = z;
                    return;
                case 6:
                    OppoPowerConsumedMonitorService.this.mAodState = Secure.getInt(OppoPowerConsumedMonitorService.this.mContext.getContentResolver(), OppoPowerConsumedMonitorService.AOD_ENABLE_IMEDIATE, 0);
                    OppoPowerConsumedMonitorService.this.mAodDurationSetted = Secure.getInt(OppoPowerConsumedMonitorService.this.mContext.getContentResolver(), OppoPowerConsumedMonitorService.AOD_USER_SETTIME, 0);
                    if (OppoPowerConsumedMonitorService.this.mAodDurationSetted == 1) {
                        OppoPowerConsumedMonitorService.this.mAodBeginHour = Secure.getIntForUser(OppoPowerConsumedMonitorService.this.mContext.getContentResolver(), OppoPowerConsumedMonitorService.AOD_SETTIME_BEGIN_HOUR, 0, -2);
                        OppoPowerConsumedMonitorService.this.mAodBeginMin = Secure.getIntForUser(OppoPowerConsumedMonitorService.this.mContext.getContentResolver(), OppoPowerConsumedMonitorService.AOD_SETTIME_BEGIN_MIN, 0, -2);
                        OppoPowerConsumedMonitorService.this.mAodEndHour = Secure.getIntForUser(OppoPowerConsumedMonitorService.this.mContext.getContentResolver(), OppoPowerConsumedMonitorService.AOD_SETTIME_END_HOUR, 0, -2);
                        OppoPowerConsumedMonitorService.this.mAodEndMin = Secure.getIntForUser(OppoPowerConsumedMonitorService.this.mContext.getContentResolver(), OppoPowerConsumedMonitorService.AOD_SETTIME_END_MIN, 0, -2);
                        return;
                    }
                    return;
                case 7:
                    try {
                        OppoPowerConsumedMonitorService.this.mFlashLightState = Integer.parseInt(OppoFlashLightManager.getOppoFlashLightManager().getFlashLightState());
                        return;
                    } catch (Exception e) {
                        Slog.w(OppoPowerConsumedMonitorService.TAG, "getFlashLightState failed ");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    class Shell extends ShellCommand {
        Shell() {
        }

        public int onCommand(String cmd) {
            return OppoPowerConsumedMonitorService.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            OppoPowerConsumedMonitorService.dumpHelp(getOutPrintWriter());
        }
    }

    public OppoPowerConsumedMonitorService(Context context) {
        super(context);
        this.mContext = context;
        this.mHandler = new MyHandler(BackgroundThread.getHandler().getLooper());
        this.mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public void onStart() {
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        mIsReleaseType = SystemProperties.getBoolean("ro.build.release_type", false);
        isSpecialVersion();
        this.mNetworkStatsFactory = new NetworkStatsFactory();
        this.mBinderService = new BinderService(this, null);
        publishBinderService("power_monitor", this.mBinderService);
    }

    public void onBootPhase(int phase) {
        synchronized (this.mLock) {
            if (phase == 500) {
                this.mLocalAlarmManager = (LocalService) getLocalService(LocalService.class);
            } else if (phase == 1000) {
                this.mBootCompleted = true;
                this.mScreenOffCurrentTime = System.currentTimeMillis();
                this.mScreenOffElapsedRealTime = SystemClock.elapsedRealtime();
                this.mScreenOffUpTime = SystemClock.uptimeMillis();
                this.mScreenOnCurrentTime = System.currentTimeMillis();
                this.mScreenOnElapseRealTime = SystemClock.elapsedRealtime();
                this.mScreenOnUpTime = SystemClock.uptimeMillis();
                this.mBatteryFcc = getBatteryFcc();
                if (this.mBatteryFcc > 0 && this.mBatteryFcc < 9999) {
                    this.mBatteryCapacity = this.mBatteryFcc;
                }
            }
        }
    }

    private void getTopApResumeReason() {
        IOException e;
        Throwable th;
        StringBuilder topApResumeReason = new StringBuilder();
        File file = new File(AP_WAKEUP_SOURCE_PATH);
        BufferedReader bufferedReader = null;
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String tempString = reader.readLine();
                        if (tempString != null) {
                            if (DEBUG_PANIC || this.mIsDebugMode) {
                                Slog.d(TAG, "getTopApResumeReason readLine :" + tempString);
                            }
                            if (!tempString.isEmpty() && (tempString.contains(":") ^ 1) == 0) {
                                String[] reason_set = tempString.split(":");
                                if (reason_set.length == 2) {
                                    try {
                                        String resume_source = reason_set[0].trim();
                                        int resume_count = Integer.valueOf(reason_set[1].trim()).intValue();
                                        if (resume_source != null) {
                                            if (resume_source.equals("qpnp_rtc_alarm")) {
                                                this.mRtcAlarmWakeupCount = resume_count;
                                            } else if (resume_source.equals("wcnss_wlan")) {
                                                this.mWcnssWakeupCount = resume_count;
                                            } else if (resume_source.equals("modem")) {
                                                this.mModemWakeupCount = resume_count;
                                            } else if (resume_source.equals("adsp")) {
                                                this.mAdspWakeupCount = resume_count;
                                            } else if (resume_source.equals("sps")) {
                                                this.mSpsWakeupCount = resume_count;
                                            }
                                            topApResumeReason.append(resume_source).append(":").append(resume_count).append("; ");
                                        }
                                    } catch (NumberFormatException e2) {
                                        Slog.d(TAG, "readFileByLines NumberFormatException:" + e2.getMessage());
                                    }
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e1) {
                                    Slog.d(TAG, "readFileByLines io close exception :" + e1.getMessage());
                                }
                            }
                        }
                    } catch (IOException e3) {
                        e = e3;
                        bufferedReader = reader;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = reader;
                    }
                }
            } catch (IOException e4) {
                e = e4;
                try {
                    Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e12) {
                            Slog.d(TAG, "readFileByLines io close exception :" + e12.getMessage());
                        }
                    }
                    this.mApResumeReason = topApResumeReason.toString();
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e122) {
                            Slog.d(TAG, "readFileByLines io close exception :" + e122.getMessage());
                        }
                    }
                    throw th;
                }
            }
            this.mApResumeReason = topApResumeReason.toString();
            return;
        }
        Slog.d(TAG, "/sys/kernel/wakeup_reasons/ap_resume_reason_stastics not exists");
    }

    private void getWcnResumeInfo() {
        IOException e;
        Throwable th;
        StringBuilder wcn_resume_info = new StringBuilder();
        File file = new File(WCN_INFO_PATH);
        BufferedReader reader = null;
        if (file.exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String tempString = reader2.readLine();
                        if (tempString != null) {
                            if (DEBUG_PANIC || this.mIsDebugMode) {
                                Slog.d(TAG, "getWcnResumeInfo readLine :" + tempString);
                            }
                            if (tempString.length() != 0 && (tempString.contains(":") ^ 1) == 0) {
                                String[] reason_set = tempString.split(":");
                                if (reason_set.length == 2) {
                                    try {
                                        if (Integer.valueOf(reason_set[1].trim()).intValue() > 0) {
                                            wcn_resume_info.append(tempString.trim());
                                            wcn_resume_info.append(";");
                                        }
                                    } catch (NumberFormatException e2) {
                                        Slog.d(TAG, "readFileByLines NumberFormatException:" + e2.getMessage());
                                    }
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            if (reader2 != null) {
                                try {
                                    reader2.close();
                                } catch (IOException e1) {
                                    Slog.d(TAG, "readFileByLines io close exception :" + e1.getMessage());
                                }
                            }
                        }
                    } catch (IOException e3) {
                        e = e3;
                        reader = reader2;
                    } catch (Throwable th2) {
                        th = th2;
                        reader = reader2;
                    }
                }
            } catch (IOException e4) {
                e = e4;
                try {
                    Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e12) {
                            Slog.d(TAG, "readFileByLines io close exception :" + e12.getMessage());
                        }
                    }
                    this.mWcnInfo = wcn_resume_info.toString();
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e122) {
                            Slog.d(TAG, "readFileByLines io close exception :" + e122.getMessage());
                        }
                    }
                    throw th;
                }
            }
            this.mWcnInfo = wcn_resume_info.toString();
            return;
        }
        Slog.d(TAG, "/sys/kernel/wakeup_reasons/wcn_resume_reason_stastics not exists");
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00c4 A:{SYNTHETIC, Splitter: B:31:0x00c4} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00eb A:{SYNTHETIC, Splitter: B:37:0x00eb} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getModemResumeInfo() {
        IOException e;
        Throwable th;
        File file = new File(MODEM_INFO_PATH);
        BufferedReader reader = null;
        StringBuilder modemInfo = new StringBuilder();
        this.mModemTopWakeUpReason = null;
        if (file.exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                try {
                    String tempString = reader2.readLine();
                    if (DEBUG_PANIC || this.mIsDebugMode) {
                        Slog.d(TAG, "getModemResumeInfo readLine :" + tempString);
                    }
                    if (tempString != null && tempString.length() > 0) {
                        modemInfo.append(tempString.trim());
                        if (this.mModemTopWakeUpReason == null) {
                            this.mModemTopWakeUpReason = tempString.split(":")[0];
                        }
                        modemInfo.append(", ");
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e1) {
                            Slog.d(TAG, "readFileByLines io close exception :" + e1.getMessage());
                        }
                    }
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                        if (reader != null) {
                        }
                        this.mModemInfo = modemInfo.toString();
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e12) {
                                Slog.d(TAG, "readFileByLines io close exception :" + e12.getMessage());
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.d(TAG, "readFileByLines io close exception :" + e122.getMessage());
                    }
                }
                this.mModemInfo = modemInfo.toString();
                return;
            }
            this.mModemInfo = modemInfo.toString();
            return;
        }
        Slog.d(TAG, "/sys/kernel/wakeup_reasons/modem_resume_reason_stastics not exists");
    }

    private String TimeStamp2Date(long timestamp, String formats) {
        return new SimpleDateFormat(formats).format(new Date(timestamp));
    }

    private int getVirtualKeyInterrupts() {
        IOException e;
        Throwable th;
        File file = new File(INTERRUPTS_PATH);
        BufferedReader reader = null;
        int virtualKeyInterrupts = 0;
        if (file.exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String tempString = reader2.readLine();
                        if (tempString == null) {
                            if (reader2 != null) {
                                try {
                                    reader2.close();
                                } catch (IOException e1) {
                                    Slog.d(TAG, "getVirtualKeyInterrupts io close exception :" + e1.getMessage());
                                }
                            }
                        } else if (tempString.length() != 0 && (tempString.contains(VIRTUAL_KEY_INTERRUPT_NAME) ^ 1) == 0) {
                            if (DEBUG_PANIC || this.mIsDebugMode) {
                                Slog.i(TAG, "getVirtualKeyInterrupts : " + tempString);
                            }
                            String[] virtual_key_status = tempString.split("\\s+");
                            if (virtual_key_status.length > 0 && VIRTUAL_KEY_INTERRUPT_NAME.equals(virtual_key_status[virtual_key_status.length - 1])) {
                                try {
                                    virtualKeyInterrupts = Integer.valueOf(virtual_key_status[1].trim()).intValue();
                                } catch (NumberFormatException e2) {
                                    Slog.d(TAG, "getVirtualKeyInterrupts NumberFormatException:" + e2.getMessage());
                                }
                            }
                        }
                    } catch (IOException e3) {
                        e = e3;
                        reader = reader2;
                    } catch (Throwable th2) {
                        th = th2;
                        reader = reader2;
                    }
                }
            } catch (IOException e4) {
                e = e4;
                try {
                    Slog.d(TAG, "getVirtualKeyInterrupts io exception:" + e.getMessage());
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e12) {
                            Slog.d(TAG, "getVirtualKeyInterrupts io close exception :" + e12.getMessage());
                        }
                    }
                    return virtualKeyInterrupts;
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e122) {
                            Slog.d(TAG, "getVirtualKeyInterrupts io close exception :" + e122.getMessage());
                        }
                    }
                    throw th;
                }
            }
            return virtualKeyInterrupts;
        }
        Slog.d(TAG, "/proc/interrupts not exists");
        return 0;
    }

    private String transferDataVersion(long data) {
        String surffix = "B";
        float total_data = ((float) data) * 1.0f;
        if (data <= 0) {
            return "0";
        }
        if (total_data > 1024.0f) {
            total_data /= 1024.0f;
            surffix = "KB";
        }
        if (total_data > 1024.0f) {
            total_data /= 1024.0f;
            surffix = "MB";
        }
        if (total_data > 1024.0f) {
            total_data /= 1024.0f;
            surffix = "GB";
        }
        return String.format("%.2f%s", new Object[]{Float.valueOf(total_data), surffix});
    }

    private String getActiveAudioPids() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        String pids = null;
        if (audioManager != null) {
            pids = audioManager.getParameters("get_pid");
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.i(TAG, "getActiveAudioPids : " + pids);
            }
        }
        if (pids == null || pids.length() == 0) {
            return null;
        }
        return pids;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x004e A:{SYNTHETIC, Splitter: B:15:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0053 A:{SYNTHETIC, Splitter: B:18:0x0053} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x004e A:{SYNTHETIC, Splitter: B:15:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0053 A:{SYNTHETIC, Splitter: B:18:0x0053} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x008d A:{SYNTHETIC, Splitter: B:43:0x008d} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0092 A:{SYNTHETIC, Splitter: B:46:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x008d A:{SYNTHETIC, Splitter: B:43:0x008d} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0092 A:{SYNTHETIC, Splitter: B:46:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0097  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getAudioProcess(String strPid) {
        IOException e;
        Throwable th;
        StringBuilder result = new StringBuilder();
        InputStreamReader mIsReader = null;
        BufferedReader mBfReader = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("cat /proc/" + strPid + "/cmdline");
            InputStreamReader mIsReader2 = new InputStreamReader(process.getInputStream(), "utf-8");
            try {
                BufferedReader mBfReader2 = new BufferedReader(mIsReader2);
                while (true) {
                    try {
                        String tempInfo = mBfReader2.readLine();
                        if (tempInfo == null) {
                            break;
                        }
                        result.append(tempInfo);
                    } catch (IOException e2) {
                        e = e2;
                        mBfReader = mBfReader2;
                        mIsReader = mIsReader2;
                        try {
                            e.printStackTrace();
                            if (mIsReader != null) {
                            }
                            if (mBfReader != null) {
                            }
                            if (process != null) {
                            }
                            return result.toString().trim();
                        } catch (Throwable th2) {
                            th = th2;
                            if (mIsReader != null) {
                            }
                            if (mBfReader != null) {
                            }
                            if (process != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        mBfReader = mBfReader2;
                        mIsReader = mIsReader2;
                        if (mIsReader != null) {
                        }
                        if (mBfReader != null) {
                        }
                        if (process != null) {
                        }
                        throw th;
                    }
                }
                if (mIsReader2 != null) {
                    try {
                        mIsReader2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                if (mBfReader2 != null) {
                    try {
                        mBfReader2.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                if (process != null) {
                    process.destroy();
                }
                mBfReader = mBfReader2;
            } catch (IOException e4) {
                e32 = e4;
                mIsReader = mIsReader2;
                e32.printStackTrace();
                if (mIsReader != null) {
                }
                if (mBfReader != null) {
                }
                if (process != null) {
                }
                return result.toString().trim();
            } catch (Throwable th4) {
                th = th4;
                mIsReader = mIsReader2;
                if (mIsReader != null) {
                    try {
                        mIsReader.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                if (mBfReader != null) {
                    try {
                        mBfReader.close();
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
                if (process != null) {
                    process.destroy();
                }
                throw th;
            }
        } catch (IOException e5) {
            e3222 = e5;
            e3222.printStackTrace();
            if (mIsReader != null) {
                try {
                    mIsReader.close();
                } catch (IOException e32222) {
                    e32222.printStackTrace();
                }
            }
            if (mBfReader != null) {
                try {
                    mBfReader.close();
                } catch (IOException e322222) {
                    e322222.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
            return result.toString().trim();
        }
        return result.toString().trim();
    }

    private void onUpdate() {
    }

    private void parseConfig() {
    }

    private void getWakeLockInfo() {
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        if (powerManager != null) {
            this.mFrameworksBlockedTime = powerManager.getFrameworksBlockedTime();
            Map<String, Long> topBlockedApp = powerManager.getTopAppBlocked(3);
            if (this.mIsDebugMode) {
                Slog.d(TAG, "mFrameworksBlockedTime = " + this.mFrameworksBlockedTime);
                for (Entry<String, Long> entry : topBlockedApp.entrySet()) {
                    Slog.d(TAG, "key = " + ((String) entry.getKey()) + ", value = " + entry.getValue());
                }
            }
            this.mFrameworksBlockedRatio = (((double) this.mFrameworksBlockedTime) / ((double) this.mElapsedRealTimeDelta)) * 100.0d;
            if (topBlockedApp != null) {
                this.mTopBlockedAppList = new ArrayList(new HashMap(topBlockedApp).entrySet());
                Collections.sort(this.mTopBlockedAppList, new Comparator<Entry<String, Long>>() {
                    public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                        long v2 = ((Long) o2.getValue()).longValue();
                        long v1 = ((Long) o1.getValue()).longValue();
                        if (v2 > v1) {
                            return 1;
                        }
                        if (v1 > v2) {
                            return -1;
                        }
                        return 0;
                    }
                });
                if (this.mIsDebugMode) {
                    Slog.d(TAG, "mFrameworksBlockedTime = " + this.mFrameworksBlockedTime + ", mFrameworksBlockedRatio = " + this.mFrameworksBlockedRatio);
                    Slog.d(TAG, "Android frameworks blocked suspend app are:");
                    for (Entry<String, Long> entry2 : this.mTopBlockedAppList) {
                        Slog.d(TAG, "app " + ((String) entry2.getKey()).toString() + " blocked suspend " + ((Long) entry2.getValue()).toString() + " ms\n");
                    }
                }
            }
        }
    }

    private void getRtcAlarmInfo() {
        this.mFrameworkAlarmWakeupTimes = this.mLocalAlarmManager.getFrameworkAlarmWakeupTimes();
        this.mAppsAlarmWakeupTimes = this.mLocalAlarmManager.getAppsAlarmWakeupTimes();
    }

    private void screenOffClear() {
        this.mIsOverTemperature = false;
        this.mIsModemLogOn = false;
        this.mPreMpssStatsTimeScreenDelta = 0;
    }

    private void getScreenOffTimeInfo() {
        this.mScreenOffCurrentTime = System.currentTimeMillis();
        this.mScreenOffElapsedRealTime = SystemClock.elapsedRealtime();
        this.mScreenOffUpTime = SystemClock.uptimeMillis();
        if (this.mIsDebugMode) {
            Slog.d(TAG, "mScreenOffCurrentTime = " + this.mScreenOffCurrentTime + ", mScreenOffElapsedRealTime = " + this.mScreenOffElapsedRealTime + ", mScreenOffUpTime = " + this.mScreenOffUpTime);
        }
    }

    private void getScreenOffVirtualKeyInterruptsInfo() {
        this.mVirtualKeyInterruptsScreenOff = getVirtualKeyInterrupts();
    }

    private void getScreenOffNetworkStatsInfo() {
        try {
            this.mNetworkStatsScreenOff = this.mNetworkStatsFactory.readNetworkStatsDetail();
        } catch (IOException e) {
            this.mNetworkStatsScreenOff = null;
            Slog.e(TAG, "readNetworkStatsDetail IOException --- ");
        }
    }

    private void getScreenOffAudioProcessInfo() {
        this.mAudioProcessMap.clear();
        this.mMusicScene = false;
        this.checkAudioCount = 0;
        this.mHandler.removeCallbacks(this.mAudioProcessRunable);
        this.mHandler.postDelayed(this.mAudioProcessRunable, RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL);
    }

    private void getScreenOffModemLogStatusDetect() {
        if (this.mHandler.hasMessages(5)) {
            this.mHandler.removeMessages(5);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5), 1800000);
    }

    private void getScreenOffAodStatus() {
        this.mAodState = 0;
        if (this.mHandler.hasMessages(6)) {
            this.mHandler.removeMessages(6);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 60000);
    }

    private void getScreenOffFlashLightStatus() {
        this.mFlashLightState = 0;
        if (this.mHandler.hasMessages(7)) {
            this.mHandler.removeMessages(7);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(7), 60000);
    }

    private void getScreenOffBatteryInfo() {
        this.mBatteryCapacityScreenOff = getBatteryRealtimeCapacity();
        this.mBatteryJumpSocScreenOff = getBatteryJumpSoc();
        if (this.mIsDebugMode) {
            Slog.d(TAG, "mBatteryLevel = " + this.mBatteryLevel);
            Slog.d(TAG, "mBatteryCapacityScreenOff = " + this.mBatteryCapacityScreenOff);
            Slog.d(TAG, "mBatteryJumpSocScreenOff = " + this.mBatteryJumpSocScreenOff);
        }
    }

    private void screenOnClear() {
    }

    private void getScreenOnTimeInfo() {
        this.mScreenOnCurrentTime = System.currentTimeMillis();
        this.mScreenOnElapseRealTime = SystemClock.elapsedRealtime();
        this.mScreenOnUpTime = SystemClock.uptimeMillis();
        if (this.mIsDebugMode) {
            Slog.d(TAG, "mScreenOnCurrentTime = " + this.mScreenOnCurrentTime + ", mScreenOnElapseRealTime = " + this.mScreenOnElapseRealTime + ", mScreenOnUpTime = " + this.mScreenOnUpTime);
        }
    }

    private void getScreenOnVirtualKeyInterruptsInfo() {
        this.mVirtualKeyInterruptsScreenOn = getVirtualKeyInterrupts();
    }

    private void getScreenOnNetworkStatsInfo() {
        try {
            this.mNetworkStatsScreenOn = this.mNetworkStatsFactory.readNetworkStatsDetail();
        } catch (IOException e) {
            this.mNetworkStatsScreenOn = null;
            Slog.e(TAG, "readNetworkStatsDetail IOException --- ");
        }
    }

    private void getScreenOnBatteryInfo() {
        this.mBatteryCapacityScreenOn = getBatteryRealtimeCapacity();
        this.mBatteryJumpSocScreenOn = getBatteryJumpSoc();
        if (this.mIsDebugMode) {
            Slog.d(TAG, "mBatteryLevel = " + this.mBatteryLevel);
            Slog.d(TAG, "mBatteryCapacityScreenOn = " + this.mBatteryCapacityScreenOn);
            Slog.d(TAG, "mBatteryJumpSocScreenOn = " + this.mBatteryJumpSocScreenOn);
        }
        int retry_count = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null) {
            while (retry_count < 5) {
                boolean telephony_ready = connectivityManager.isAlreadyUpdated();
                retry_count++;
                if (DEBUG_PANIC || this.mIsDebugMode) {
                    Slog.i(TAG, "isAlreadyUpdated : " + telephony_ready);
                }
                if (telephony_ready) {
                    break;
                }
                SystemClock.sleep(200);
            }
            this.mPowerLostByTelephone = connectivityManager.getTelephonyPowerLost();
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.i(TAG, String.format("mPowerLostByTelephone: %.2fmAh", new Object[]{Double.valueOf(this.mPowerLostByTelephone)}));
            }
        }
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            String wifiPowerEventContent = wifiManager.getWifiPowerEventCode();
            if (wifiPowerEventContent == null || wifiPowerEventContent.length() <= 0) {
                this.mPowerLostByWifi = 0.0d;
            } else {
                String wifi_power_lost_temp = wifiPowerEventContent.substring(wifiPowerEventContent.indexOf(61, wifiPowerEventContent.indexOf(10)) + 1, wifiPowerEventContent.length());
                if (DEBUG_PANIC || this.mIsDebugMode) {
                    Slog.i(TAG, "mPowerLostByWifi : " + wifi_power_lost_temp);
                }
                if (wifi_power_lost_temp != null && wifi_power_lost_temp.length() > 0) {
                    try {
                        this.mPowerLostByWifi = Double.parseDouble(wifi_power_lost_temp.trim());
                    } catch (NumberFormatException e) {
                        Slog.d(TAG, "mPowerLostByWifi NumberFormatException : " + e.getMessage());
                        this.mPowerLostByWifi = 0.0d;
                    }
                }
            }
        }
        if (this.mAodDurationSetted == 1) {
            long aodOnDurationInMin = 0;
            long aodBeginTimeInMin = (long) ((this.mAodBeginHour * 60) + this.mAodBeginMin);
            long aodEndTimeInMin = (long) ((this.mAodEndHour * 60) + this.mAodEndMin);
            long screenOffDurationInMin = (this.mScreenOnElapseRealTime / 60000) - (this.mScreenOffElapsedRealTime / 60000);
            long screenOffDurationInDay = screenOffDurationInMin / ONE_DAY_IN_MIN;
            long screenOffDurationElseInMin = screenOffDurationInMin % ONE_DAY_IN_MIN;
            long screenOnCurrentTimeInMin = getNowTimeInMinute();
            long mayScreenOffCurrentTimeInMin = screenOnCurrentTimeInMin - screenOffDurationElseInMin;
            long aodStartTime = mayScreenOffCurrentTimeInMin;
            long aodEndTime = screenOnCurrentTimeInMin;
            if (aodEndTimeInMin < aodBeginTimeInMin) {
                aodEndTimeInMin += ONE_DAY_IN_MIN;
            }
            long aodOnDurationOneDayInMin = aodEndTimeInMin - aodBeginTimeInMin;
            if (screenOffDurationInDay > 0) {
                aodOnDurationInMin = screenOffDurationInDay * aodOnDurationOneDayInMin;
            }
            if (this.mIsDebugMode) {
                Slog.d(TAG, "screenOffDurationInMin = " + screenOffDurationInMin + "\n" + "screenOffDurationInDay = " + screenOffDurationInDay + "\n" + "screenOffDurationElseInMin = " + screenOffDurationElseInMin + "\n" + "screenOnCurrentTimeInMin = " + screenOnCurrentTimeInMin + "\n" + "mayScreenOffCurrentTimeInMin = " + mayScreenOffCurrentTimeInMin + "\n" + "aodBeginTimeInMin = " + aodBeginTimeInMin + "\n" + "aodEndTimeInMin = " + aodEndTimeInMin);
            }
            if (mayScreenOffCurrentTimeInMin < aodBeginTimeInMin) {
                aodStartTime = aodBeginTimeInMin;
            }
            if (screenOnCurrentTimeInMin > aodEndTimeInMin) {
                aodEndTime = aodEndTimeInMin;
            }
            if (aodStartTime >= aodBeginTimeInMin && aodEndTime <= aodEndTimeInMin && aodStartTime < aodEndTime) {
                aodOnDurationInMin += aodEndTime - aodStartTime;
            }
            screenOnCurrentTimeInMin += ONE_DAY_IN_MIN;
            mayScreenOffCurrentTimeInMin += ONE_DAY_IN_MIN;
            aodStartTime = mayScreenOffCurrentTimeInMin;
            aodEndTime = screenOnCurrentTimeInMin;
            if (mayScreenOffCurrentTimeInMin < aodBeginTimeInMin) {
                aodStartTime = aodBeginTimeInMin;
            }
            if (screenOnCurrentTimeInMin > aodEndTimeInMin) {
                aodEndTime = aodEndTimeInMin;
            }
            if (aodStartTime >= aodBeginTimeInMin && aodEndTime <= aodEndTimeInMin && aodStartTime < aodEndTime) {
                aodOnDurationInMin += aodEndTime - aodStartTime;
            }
            this.mPowerLostByAod = (((double) aodOnDurationInMin) * this.mAodOnDelta) / 60.0d;
        }
    }

    private long getNowTimeInMinute() {
        int hour = 0;
        int minute = 0;
        Calendar cal = Calendar.getInstance();
        if (cal != null) {
            hour = cal.get(11);
            minute = cal.get(12);
        }
        return (long) ((hour * 60) + minute);
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0054 A:{SYNTHETIC, Splitter: B:19:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0082 A:{SYNTHETIC, Splitter: B:32:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:77:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x009d A:{SYNTHETIC, Splitter: B:46:0x009d} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ae A:{SYNTHETIC, Splitter: B:54:0x00ae} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getOppoRpmStatsScreenOff() {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        NumberFormatException e3;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(OPPO_RPM_STATS)));
            while (true) {
                try {
                    String tempString = reader2.readLine();
                    if (tempString == null) {
                        break;
                    } else if (tempString.length() != 0) {
                        String[] rpmStats = tempString.split(":");
                        if (rpmStats.length == 3) {
                            if (rpmStats[0].equals("vlow")) {
                                this.mVlowStatsScreenOff = Integer.parseInt(rpmStats[1], 16);
                                this.mVlowStatsTimeScreenOff = Long.parseLong(rpmStats[2], 16);
                            } else if (rpmStats[0].equals("vmin")) {
                                this.mVminStatsScreenOff = Integer.parseInt(rpmStats[1], 16);
                                this.mVminStatsTimeScreenOff = Long.parseLong(rpmStats[2], 16);
                            }
                        }
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    reader = reader2;
                    try {
                        e.printStackTrace();
                        if (reader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e2 = e5;
                    reader = reader2;
                    e2.printStackTrace();
                    if (reader == null) {
                    }
                } catch (NumberFormatException e6) {
                    e3 = e6;
                    reader = reader2;
                    e3.printStackTrace();
                    if (reader == null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e2222) {
                    e2222.printStackTrace();
                }
            }
        } catch (IOException e8) {
            e2222 = e8;
            e2222.printStackTrace();
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e22222) {
                    e22222.printStackTrace();
                }
            }
        } catch (NumberFormatException e9) {
            e3 = e9;
            e3.printStackTrace();
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e222222) {
                    e222222.printStackTrace();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x00e7 A:{SYNTHETIC, Splitter: B:60:0x00e7} */
    /* JADX WARNING: Removed duplicated region for block: B:94:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0054 A:{SYNTHETIC, Splitter: B:19:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0082 A:{SYNTHETIC, Splitter: B:32:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:97:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b6 A:{SYNTHETIC, Splitter: B:47:0x00b6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getOppoRpmMasterStatsScreenOff() {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        NumberFormatException e3;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(OPPO_RPM_MASTER_STATS)));
            while (true) {
                try {
                    String tempString = reader.readLine();
                    if (tempString == null) {
                        break;
                    } else if (tempString.length() != 0) {
                        String[] rpmMasterStats = tempString.split(":");
                        if (rpmMasterStats.length == 3) {
                            if (rpmMasterStats[0].equals("APSS")) {
                                this.mApssStatsScreenOff = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mApssStatsTimeScreenOff = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("MPSS")) {
                                this.mMpssStatsScreenOff = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mMpssStatsTimeScreenOff = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("ADSP")) {
                                this.mAdspStatsScreenOff = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mAdspStatsTimeScreenOff = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("CDSP")) {
                                this.mCdspStatsScreenOff = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mCdspStatsTimeScreenOff = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("TZ")) {
                                this.mTzStatsScreenOff = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mTzStatsTimeScreenOff = Long.parseLong(rpmMasterStats[2], 16);
                            }
                        }
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    bufferedReader = reader;
                    try {
                        e.printStackTrace();
                        if (bufferedReader == null) {
                            try {
                                bufferedReader.close();
                                return;
                            } catch (IOException e22) {
                                e22.printStackTrace();
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e22 = e5;
                    bufferedReader = reader;
                    e22.printStackTrace();
                    if (bufferedReader == null) {
                        try {
                            bufferedReader.close();
                            return;
                        } catch (IOException e222) {
                            e222.printStackTrace();
                            return;
                        }
                    }
                    return;
                } catch (NumberFormatException e6) {
                    e3 = e6;
                    bufferedReader = reader;
                    e3.printStackTrace();
                    if (bufferedReader == null) {
                        try {
                            bufferedReader.close();
                            return;
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                            return;
                        }
                    }
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = reader;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e222222) {
                    e222222.printStackTrace();
                }
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (bufferedReader == null) {
            }
        } catch (IOException e8) {
            e222222 = e8;
            e222222.printStackTrace();
            if (bufferedReader == null) {
            }
        } catch (NumberFormatException e9) {
            e3 = e9;
            e3.printStackTrace();
            if (bufferedReader == null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0054 A:{SYNTHETIC, Splitter: B:19:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0082 A:{SYNTHETIC, Splitter: B:32:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:77:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x009d A:{SYNTHETIC, Splitter: B:46:0x009d} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ae A:{SYNTHETIC, Splitter: B:54:0x00ae} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getOppoRpmStatsScreenOn() {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        NumberFormatException e3;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(OPPO_RPM_STATS)));
            while (true) {
                try {
                    String tempString = reader2.readLine();
                    if (tempString == null) {
                        break;
                    } else if (tempString.length() != 0) {
                        String[] rpmStats = tempString.split(":");
                        if (rpmStats.length == 3) {
                            if (rpmStats[0].equals("vlow")) {
                                this.mVlowStatsScreenOn = Integer.parseInt(rpmStats[1], 16);
                                this.mVlowStatsTimeScreenOn = Long.parseLong(rpmStats[2], 16);
                            } else if (rpmStats[0].equals("vmin")) {
                                this.mVminStatsScreenOn = Integer.parseInt(rpmStats[1], 16);
                                this.mVminStatsTimeScreenOn = Long.parseLong(rpmStats[2], 16);
                            }
                        }
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    reader = reader2;
                    try {
                        e.printStackTrace();
                        if (reader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e2 = e5;
                    reader = reader2;
                    e2.printStackTrace();
                    if (reader == null) {
                    }
                } catch (NumberFormatException e6) {
                    e3 = e6;
                    reader = reader2;
                    e3.printStackTrace();
                    if (reader == null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e2222) {
                    e2222.printStackTrace();
                }
            }
        } catch (IOException e8) {
            e2222 = e8;
            e2222.printStackTrace();
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e22222) {
                    e22222.printStackTrace();
                }
            }
        } catch (NumberFormatException e9) {
            e3 = e9;
            e3.printStackTrace();
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e222222) {
                    e222222.printStackTrace();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x00e7 A:{SYNTHETIC, Splitter: B:60:0x00e7} */
    /* JADX WARNING: Removed duplicated region for block: B:94:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0054 A:{SYNTHETIC, Splitter: B:19:0x0054} */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0082 A:{SYNTHETIC, Splitter: B:32:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:97:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b6 A:{SYNTHETIC, Splitter: B:47:0x00b6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getOppoRpmMasterStatsScreenOn() {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        NumberFormatException e3;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(OPPO_RPM_MASTER_STATS)));
            while (true) {
                try {
                    String tempString = reader.readLine();
                    if (tempString == null) {
                        break;
                    } else if (tempString.length() != 0) {
                        String[] rpmMasterStats = tempString.split(":");
                        if (rpmMasterStats.length == 3) {
                            if (rpmMasterStats[0].equals("APSS")) {
                                this.mApssStatsScreenOn = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mApssStatsTimeScreenOn = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("MPSS")) {
                                this.mMpssStatsScreenOn = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mMpssStatsTimeScreenOn = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("ADSP")) {
                                this.mAdspStatsScreenOn = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mAdspStatsTimeScreenOn = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("CDSP")) {
                                this.mCdspStatsScreenOn = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mCdspStatsTimeScreenOn = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("TZ")) {
                                this.mTzStatsScreenOn = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mTzStatsTimeScreenOn = Long.parseLong(rpmMasterStats[2], 16);
                            }
                        }
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    bufferedReader = reader;
                    try {
                        e.printStackTrace();
                        if (bufferedReader == null) {
                            try {
                                bufferedReader.close();
                                return;
                            } catch (IOException e22) {
                                e22.printStackTrace();
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e22 = e5;
                    bufferedReader = reader;
                    e22.printStackTrace();
                    if (bufferedReader == null) {
                        try {
                            bufferedReader.close();
                            return;
                        } catch (IOException e222) {
                            e222.printStackTrace();
                            return;
                        }
                    }
                    return;
                } catch (NumberFormatException e6) {
                    e3 = e6;
                    bufferedReader = reader;
                    e3.printStackTrace();
                    if (bufferedReader == null) {
                        try {
                            bufferedReader.close();
                            return;
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                            return;
                        }
                    }
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = reader;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e222222) {
                    e222222.printStackTrace();
                }
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (bufferedReader == null) {
            }
        } catch (IOException e8) {
            e222222 = e8;
            e222222.printStackTrace();
            if (bufferedReader == null) {
            }
        } catch (NumberFormatException e9) {
            e3 = e9;
            e3.printStackTrace();
            if (bufferedReader == null) {
            }
        }
    }

    private void getOppoRpmStatsDelta() {
        this.mVlowStatsScreenDelta = this.mVlowStatsScreenOn - this.mVlowStatsScreenOff;
        Slog.d(TAG, "mVlowStatsScreenDelta :" + this.mVlowStatsScreenDelta);
        this.mVminStatsScreenDelta = this.mVminStatsScreenOn - this.mVminStatsScreenOff;
        Slog.d(TAG, "mVminStatsScreenDelta :" + this.mVminStatsScreenDelta);
        this.mVlowStatsTimeScreenDelta = this.mVlowStatsTimeScreenOn - this.mVlowStatsTimeScreenOff;
        Slog.d(TAG, "mVlowStatsTimeScreenDelta :" + this.mVlowStatsTimeScreenDelta);
        this.mVminStatsTimeScreenDelta = this.mVminStatsTimeScreenOn - this.mVminStatsTimeScreenOff;
        Slog.d(TAG, "mVminStatsTimeScreenDelta :" + this.mVminStatsTimeScreenDelta);
    }

    private void getOppoRpmMasterStatsDelta() {
        this.mApssStatsScreenDelta = this.mApssStatsScreenOn - this.mApssStatsScreenOff;
        Slog.d(TAG, "mApssStatsScreenDelta :" + this.mApssStatsScreenDelta);
        this.mMpssStatsScreenDelta = this.mMpssStatsScreenOn - this.mMpssStatsScreenOff;
        Slog.d(TAG, "mMpssStatsScreenDelta :" + this.mMpssStatsScreenDelta);
        this.mAdspStatsScreenDelta = this.mAdspStatsScreenOn - this.mAdspStatsScreenOff;
        Slog.d(TAG, "mAdspStatsScreenDelta :" + this.mAdspStatsScreenDelta);
        this.mCdspStatsScreenDelta = this.mCdspStatsScreenOn - this.mCdspStatsScreenOff;
        Slog.d(TAG, "mCdspStatsScreenDelta :" + this.mCdspStatsScreenDelta);
        this.mTzStatsScreenDelta = this.mTzStatsScreenOn - this.mTzStatsScreenOff;
        Slog.d(TAG, "mTzStatsScreenDelta :" + this.mTzStatsScreenDelta);
        this.mApssStatsTimeScreenDelta = this.mApssStatsTimeScreenOn - this.mApssStatsTimeScreenOff;
        Slog.d(TAG, "mApssStatsTimeScreenDelta :" + this.mApssStatsTimeScreenDelta);
        this.mMpssStatsTimeScreenDelta = this.mMpssStatsTimeScreenOn - this.mMpssStatsTimeScreenOff;
        Slog.d(TAG, "mMpssStatsTimeScreenDelta :" + this.mMpssStatsTimeScreenDelta);
        this.mAdspStatsTimeScreenDelta = this.mAdspStatsTimeScreenOn - this.mAdspStatsTimeScreenOff;
        Slog.d(TAG, "mAdspStatsTimeScreenDelta :" + this.mAdspStatsTimeScreenDelta);
        this.mCdspStatsTimeScreenDelta = this.mCdspStatsTimeScreenOn - this.mCdspStatsTimeScreenOff;
        Slog.d(TAG, "mCdspStatsTimeScreenDelta :" + this.mCdspStatsTimeScreenDelta);
        this.mTzStatsTimeScreenDelta = this.mTzStatsTimeScreenOn - this.mTzStatsTimeScreenOff;
        Slog.d(TAG, "mTzStatsTimeScreenDelta :" + this.mTzStatsTimeScreenDelta);
    }

    private void getPhoneStateInfo() {
        BluetoothManager bluetoothManager = (BluetoothManager) this.mContext.getSystemService(OppoProcessManager.RESUME_REASON_BLUETOOTH_STR);
        if (bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter != null) {
                this.mBluetoothState = bluetoothAdapter.getState();
            }
        }
        SubscriptionManager subscriptionManager = SubscriptionManager.from(this.mContext);
        if (subscriptionManager != null) {
            List<SubscriptionInfo> list = subscriptionManager.getActiveSubscriptionInfoList();
            if (list != null) {
                this.mSimCardCount = list.size();
                if (this.mSimCardCount == 1) {
                    this.mSimCard1Name = ((SubscriptionInfo) list.get(0)).getCarrierName().toString();
                    if (DEBUG_PANIC || this.mIsDebugMode) {
                        Slog.d(TAG, "There is one sim card, carrier name is " + this.mSimCard1Name);
                    }
                } else if (this.mSimCardCount == 2) {
                    this.mSimCard1Name = ((SubscriptionInfo) list.get(0)).getCarrierName().toString();
                    this.mSimCard2Name = ((SubscriptionInfo) list.get(1)).getCarrierName().toString();
                    if (DEBUG_PANIC || this.mIsDebugMode) {
                        Slog.d(TAG, "There are two sim cards.");
                        Slog.d(TAG, "Sim card1 is " + this.mSimCard1Name);
                        Slog.d(TAG, "Sim card2 is " + this.mSimCard2Name);
                    }
                }
            }
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                this.mCurrentNetwork = networkInfo.getTypeName();
                String extraInfo = networkInfo.getExtraInfo();
                String state = networkInfo.getState().toString();
                String detailedState = networkInfo.getDetailedState().toString();
                if (DEBUG_PANIC || this.mIsDebugMode) {
                    Slog.d(TAG, "Current active network is " + this.mCurrentNetwork);
                    Slog.d(TAG, "Active network extrainfo is " + extraInfo);
                    Slog.d(TAG, "Active network state is " + state + ", detailed state is " + detailedState);
                }
            }
        }
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager != null) {
            this.mDataNetworkState = telephonyManager.getDataState();
            this.mDataNetworkOperatorName = telephonyManager.getNetworkOperatorName();
            this.mDataNetworkType = telephonyManager.getNetworkType();
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, "Data network is " + this.mDataNetworkOperatorName);
                Slog.d(TAG, "Data network state is " + this.mDataNetworkState);
                Slog.d(TAG, "Data network type is " + this.mDataNetworkType);
            }
        }
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            this.mWifiState = wifiManager.getWifiState();
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, "Wifi state is " + this.mWifiState);
            }
            if (this.mWifiState == 3) {
                WifiInfo mWifiInfo = wifiManager.getConnectionInfo();
                this.mWifiName = mWifiInfo != null ? mWifiInfo.getBSSID() : "";
                if (DEBUG_PANIC || this.mIsDebugMode) {
                    Slog.d(TAG, "Wifi mac is " + this.mWifiName);
                }
            }
        }
    }

    private void computePowerMonitorResult() {
        this.mUpTimeDelta = this.mScreenOnUpTime - this.mScreenOffUpTime;
        this.mElapsedRealTimeDelta = this.mScreenOnElapseRealTime - this.mScreenOffElapsedRealTime;
        this.mSuspendRatio = 100.0d - ((((double) this.mUpTimeDelta) / ((double) this.mElapsedRealTimeDelta)) * 100.0d);
        this.mApssSuspendRatio = (((double) this.mApssStatsTimeScreenDelta) / ((double) this.mElapsedRealTimeDelta)) * 100.0d;
        this.mMpssSuspendRatio = (((double) this.mMpssStatsTimeScreenDelta) / ((double) this.mElapsedRealTimeDelta)) * 100.0d;
        this.mAdspSuspendRatio = (((double) this.mAdspStatsTimeScreenDelta) / ((double) this.mElapsedRealTimeDelta)) * 100.0d;
        this.mVirtualKeyInterrupts = this.mVirtualKeyInterruptsScreenOn - this.mVirtualKeyInterruptsScreenOff;
        PackageManager packageManager = this.mContext.getPackageManager();
        if (!(this.mNetworkStatsScreenOn == null || this.mNetworkStatsScreenOff == null)) {
            this.mNetworkStatsSubtract = this.mNetworkStatsScreenOn.subtract(this.mNetworkStatsScreenOff);
        }
        if (this.mNetworkStatsSubtract != null) {
            boolean isUsbTethering = SystemProperties.get("sys.usb.config").contains("rndis");
            ArrayList<String> validMobileData = new ArrayList();
            ArrayList<String> validWifiData = new ArrayList();
            String[] ifaceNames = new File("proc/net/xt_qtaguid/iface_stat").list();
            if (ifaceNames != null) {
                int index = 0;
                while (index < ifaceNames.length) {
                    if (ifaceNames[index].contains("wlan")) {
                        validWifiData.add(ifaceNames[index]);
                    } else if (!(ifaceNames[index].contains("lo") || (isUsbTethering && (ifaceNames[index].contains("rndis") ^ 1) == 0))) {
                        validMobileData.add(ifaceNames[index]);
                    }
                    index++;
                }
            }
            NetworkStats.Entry entry = null;
            for (int i = 0; i < this.mNetworkStatsSubtract.size(); i++) {
                entry = this.mNetworkStatsSubtract.getValues(i, entry);
                long entry_total = entry.rxBytes + entry.txBytes;
                if (validWifiData.contains(entry.iface)) {
                    if (entry_total > this.mWifiIncrementMax) {
                        this.mWifiIncrementMax = entry_total;
                        this.mWifiIncrementMaxUid = (long) entry.uid;
                    }
                    this.mWifiDataDelta += entry_total;
                } else {
                    if (validMobileData.contains(entry.iface)) {
                        if (entry_total > this.mMobileIncrementMax) {
                            this.mMobileIncrementMax = entry_total;
                            this.mMobileIncrementMaxUid = (long) entry.uid;
                        }
                        this.mMobileDataDelta += entry_total;
                    }
                }
            }
        }
        if (this.mWifiDataDelta > 0 && this.mWifiIncrementMaxUid != 0) {
            this.mWifiIncrementMaxProcess = new StringBuilder();
            if (this.mWifiIncrementMaxUid == 1000) {
                this.mWifiIncrementMaxProcess.append("SYSTEM_UID\\");
            } else if (packageManager != null) {
                String[] wifi_uid_app_list = packageManager.getPackagesForUid((int) this.mWifiIncrementMaxUid);
                if (wifi_uid_app_list != null) {
                    for (String append : wifi_uid_app_list) {
                        this.mWifiIncrementMaxProcess.append(append);
                        this.mWifiIncrementMaxProcess.append("\\");
                    }
                } else {
                    Slog.d(TAG, "current uid app not found");
                }
            }
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.i(TAG, "mWifiDataDelta : " + this.mWifiDataDelta + ", mWifiIncrementMaxUid=" + this.mWifiIncrementMaxUid + ", mWifiIncrementMaxProcess maybe " + this.mWifiIncrementMaxProcess.toString() + ", mWifiIncrementMax : " + this.mWifiIncrementMax);
            }
        }
        if (this.mMobileDataDelta > 0 && this.mMobileIncrementMaxUid != 0) {
            this.mMobileIncrementMaxProcess = new StringBuilder();
            if (this.mMobileIncrementMaxUid == 1000) {
                this.mMobileIncrementMaxProcess.append("SYSTEM_UID\\");
            } else if (packageManager != null) {
                String[] mobile_uid_app_list = packageManager.getPackagesForUid((int) this.mMobileIncrementMaxUid);
                if (mobile_uid_app_list != null) {
                    for (String append2 : mobile_uid_app_list) {
                        this.mMobileIncrementMaxProcess.append(append2);
                        this.mMobileIncrementMaxProcess.append("\\");
                    }
                } else {
                    Slog.d(TAG, "current uid app not found");
                }
            }
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.i(TAG, "mMobileDataDelta : " + this.mMobileDataDelta + ", mMobileIncrementMaxUid = " + this.mMobileIncrementMaxUid + ", mMobileIncrementMaxProcess maybe " + this.mMobileIncrementMaxProcess.toString() + ", mMobileIncrementMax : " + this.mMobileIncrementMax);
            }
        }
        double averageMobileDataPerHour = ((double) this.mMobileDataDelta) / (((double) this.mElapsedRealTimeDelta) / 3600000.0d);
        if (((double) this.mWifiDataDelta) / (((double) this.mElapsedRealTimeDelta) / 3600000.0d) > ((double) this.WIFI_DATA_PER_HOUR_THRESHOLD) || averageMobileDataPerHour > ((double) this.MOBILE_DATA_PER_HOUR_THRESHOLD)) {
            this.mDownloadScene = true;
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, "[DownloadScene]");
            }
        } else {
            this.mDownloadScene = false;
        }
        this.mBatteryCapacityDelta = this.mBatteryCapacityScreenOff - this.mBatteryCapacityScreenOn;
        this.mBatteryJumpSocDelta = this.mBatteryJumpSocScreenOn - this.mBatteryJumpSocScreenOff;
        this.mRealCurrent = (double) ((((long) (this.mBatteryCapacityDelta - this.mBatteryJumpSocDelta)) * 3600000) / this.mElapsedRealTimeDelta);
        this.mRealCurrentWithoutPowerLostKnown = ((((((double) (this.mBatteryCapacityDelta - this.mBatteryJumpSocDelta)) - this.mPowerLostByTelephone) - this.mPowerLostByWifi) - this.mPowerLostByAod) * 3600000.0d) / ((double) this.mElapsedRealTimeDelta);
        this.mTargetCurrent = this.mBaseCurrent;
        if (this.mSimCardCount == 0) {
            this.mTargetCurrent += this.mNoSimCardDelta;
        } else if (this.mSimCardCount == 1) {
            this.mTargetCurrent += this.mSingleSimCardDelta;
        } else if (this.mSimCardCount == 2) {
            this.mTargetCurrent += this.mDoubleSimCardDelta;
        }
        if (this.mWifiState == 3) {
            this.mTargetCurrent += this.mWifiDelta;
        } else if (this.mDataNetworkState != 0) {
            this.mTargetCurrent += this.mNetworkDelta;
        }
        if (this.mBluetoothState == 12) {
            this.mTargetCurrent += this.mBluetoothDelta;
        }
        if (this.mAodState == 1) {
            this.mTargetCurrent += this.mAodOnDelta;
        }
        if (this.mFlashLightState == 1) {
            this.mTargetCurrent += this.mFlashLightOnDelta;
        }
        this.mIgnoreThreshold = this.mTargetCurrent + this.mFloateDelta;
    }

    /* JADX WARNING: Removed duplicated region for block: B:183:0x064b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getIssueType() {
        IOException e;
        Throwable th;
        String str;
        if (this.mRealCurrent <= this.mTargetCurrent) {
            this.mIssueType = 0;
        } else if (this.mMusicScene) {
            this.mIssueType = 1;
        } else if (this.mDownloadScene) {
            this.mIssueType = 2;
        } else if (isModemLogOn()) {
            this.mIssueType = 3;
        } else if (this.mRealCurrentWithoutPowerLostKnown <= this.mTargetCurrent) {
            boolean classified = false;
            ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            String telephonePowerState = null;
            if (connectivityManager != null) {
                telephonePowerState = connectivityManager.getTelephonyPowerState();
            }
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.i(TAG, "telephonePowerState : " + telephonePowerState);
            }
            if (telephonePowerState != null && this.mPowerLostByTelephone >= this.mPowerLostByWifi) {
                String[] telephony_event_cache = telephonePowerState.split(" ");
                if (telephony_event_cache != null && telephony_event_cache.length > 0) {
                    for (String str2 : telephony_event_cache) {
                        String[] telephony_event = str2.split(":");
                        if (telephony_event != null && telephony_event.length > 0) {
                            try {
                                if (Double.parseDouble(telephony_event[1].trim()) > 0.0d) {
                                    if ("DATA_CALL_COUNT".equals(telephony_event[0].trim())) {
                                        this.mIssueType = 11;
                                        classified = true;
                                    } else if ("NO_SERVICE_TIME".equals(telephony_event[0].trim())) {
                                        this.mIssueType = 12;
                                        classified = true;
                                    } else if ("RESELECT_PER_MIN".equals(telephony_event[0].trim())) {
                                        this.mIssueType = 13;
                                        classified = true;
                                    } else if ("SMS_SEND_COUNT".equals(telephony_event[0].trim())) {
                                        this.mIssueType = 14;
                                        classified = true;
                                    } else if ("NITZ_COUNT".equals(telephony_event[0].trim())) {
                                        this.mIssueType = 15;
                                        classified = true;
                                    }
                                }
                            } catch (NumberFormatException e2) {
                                Slog.d(TAG, "telephony_event NumberFormatException : " + e2.getMessage());
                            }
                        }
                    }
                }
            }
            if (this.mPowerLostByTelephone < this.mPowerLostByWifi) {
                WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
                if (wifiManager != null) {
                    String wifiPowerEventContent = wifiManager.getWifiPowerEventCode();
                    if (wifiPowerEventContent != null && wifiPowerEventContent.length() > 0) {
                        this.mWifiPowerEventList = wifiPowerEventContent.substring(wifiPowerEventContent.indexOf(61) + 1, wifiPowerEventContent.indexOf(10));
                    }
                    if (this.mWifiPowerEventList != null && this.mWifiPowerEventList.length() > 0) {
                        if (this.mWifiPowerEventList.contains("SCAN_FREQUENT")) {
                            this.mIssueType = 21;
                            classified = true;
                        } else if (this.mWifiPowerEventList.contains("RENEW_FREQUENT")) {
                            this.mIssueType = 22;
                            classified = true;
                        } else if (this.mWifiPowerEventList.contains("GROUP_FREQUENT")) {
                            this.mIssueType = 23;
                            classified = true;
                        } else if (this.mWifiPowerEventList.contains("DISCONN_FREQUENT")) {
                            this.mIssueType = 24;
                            classified = true;
                        }
                    }
                }
            }
            if (!classified) {
                this.mIssueType = 99;
            }
        } else if (((this.mRealCurrentWithoutPowerLostKnown - this.mTargetCurrent) * ((double) this.mElapsedRealTimeDelta)) / ((double) this.mUpTimeDelta) > this.mSubSystemExepCurrentThreshold) {
            Slog.d(TAG, "mMpssSuspendRatio = " + this.mMpssSuspendRatio + "; mAdspSuspendRatio = " + this.mAdspSuspendRatio);
            long mpssUptime = this.mElapsedRealTimeDelta - this.mMpssStatsTimeScreenDelta;
            if (mpssUptime <= 0) {
                mpssUptime = 1;
            }
            double subCurrent = (((double) (((long) (this.mBatteryCapacityDelta - this.mBatteryJumpSocDelta)) * 3600000)) - (this.mTargetCurrent * ((double) this.mElapsedRealTimeDelta))) / ((double) mpssUptime);
            if (this.mMpssSuspendRatio < 70.0d || subCurrent < this.mMpssSubSystemExepCurrentThreshold) {
                if (this.mMpssSuspendRatio < 1.0d) {
                    this.mIssueType = 1410;
                    return;
                }
                if (this.mModemWakeupCount > this.mWcnssWakeupCount) {
                    this.mIssueType = 1411;
                    if (this.mModemTopWakeUpReason != null) {
                        str2 = this.mModemTopWakeUpReason;
                        if (str2.equals("QMI_WS")) {
                            this.mModemRilTopMsg = getModemRilTopMsg();
                            return;
                        } else if (str2.equals("IPA_WS")) {
                            this.mIssueType = 1412;
                            return;
                        } else {
                            return;
                        }
                    }
                    return;
                }
                this.mIssueType = 1413;
            } else if (this.mAdspSuspendRatio < 90.0d) {
                this.mIssueType = 142;
            } else {
                this.mIssueType = 140;
                Slog.d(TAG, "mVlowStatsTimeScreenDelta = " + this.mVlowStatsTimeScreenDelta + "; mVminStatsTimeScreenDelta = " + this.mVminStatsTimeScreenDelta);
                Slog.d(TAG, "mApssStatsTimeScreenDelta = " + this.mApssStatsTimeScreenDelta + "; mMpssStatsTimeScreenDelta = " + this.mMpssStatsTimeScreenDelta);
                Slog.d(TAG, "mAdspStatsTimeScreenDelta = " + this.mAdspStatsTimeScreenDelta + "; mCdspStatsTimeScreenDelta = " + this.mCdspStatsTimeScreenDelta);
            }
        } else {
            String top_reason = "";
            int top_resume_count = 0;
            File file = new File(AP_WAKEUP_SOURCE_PATH);
            BufferedReader reader = null;
            if (!file.exists()) {
                Slog.d(TAG, "/sys/kernel/wakeup_reasons/ap_resume_reason_stastics not exists");
            }
            if (this.mFrameworksBlockedTime * 2 > this.mUpTimeDelta && this.mSuspendRatio < 80.0d) {
                this.mIssueType = OppoProcessManager.MSG_UPLOAD;
            } else if ((this.mKernelWakelockTime.longValue() - this.mFrameworksBlockedTime) * 2 <= this.mUpTimeDelta || (this.mKernelWakelockReason.equals("alarmtimer") ^ 1) == 0 || (this.mKernelWakelockReason.equals("qcom_rx_wakelock") ^ 1) == 0 || (this.mKernelWakelockReason.equals("PowerManagerService.WakeLocks") ^ 1) == 0 || (this.mKernelWakelockReason.equals("netmgr_wl") ^ 1) == 0 || this.mSuspendRatio >= 80.0d) {
                try {
                    BufferedReader reader2 = new BufferedReader(new FileReader(file));
                    while (true) {
                        try {
                            String tempString = reader2.readLine();
                            if (tempString != null) {
                                if (DEBUG_PANIC || this.mIsDebugMode) {
                                    Slog.d(TAG, "getTopApResumeReason readLine :" + tempString);
                                }
                                if (!tempString.isEmpty() && (tempString.contains(":") ^ 1) == 0) {
                                    String[] reason_set = tempString.split(":");
                                    if (reason_set.length == 2) {
                                        try {
                                            int count;
                                            if (reason_set[0].trim().equals("qpnp_rtc_alarm")) {
                                                count = Integer.valueOf(reason_set[1].trim()).intValue();
                                                int underLayerCount = count - this.mFrameworkAlarmWakeupTimes;
                                                if (underLayerCount < 0) {
                                                    this.mFrameworkAlarmWakeupTimes = count;
                                                    underLayerCount = 0;
                                                }
                                                if (underLayerCount > top_resume_count) {
                                                    top_resume_count = underLayerCount;
                                                    top_reason = "under_layer_wakeup";
                                                }
                                                if (this.mFrameworkAlarmWakeupTimes > top_resume_count) {
                                                    top_resume_count = this.mFrameworkAlarmWakeupTimes;
                                                    top_reason = "qpnp_rtc_alarm";
                                                }
                                            } else {
                                                count = Integer.valueOf(reason_set[1].trim()).intValue();
                                                if (count > top_resume_count) {
                                                    top_resume_count = count;
                                                    top_reason = reason_set[0].trim();
                                                    if (top_reason == null) {
                                                        top_reason = "UNKNOWN";
                                                    }
                                                }
                                            }
                                        } catch (NumberFormatException e22) {
                                            Slog.d(TAG, "readFileByLines NumberFormatException:" + e22.getMessage());
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                            } else {
                                if (reader2 != null) {
                                    try {
                                        reader2.close();
                                    } catch (IOException e1) {
                                        Slog.d(TAG, "readFileByLines io close exception :" + e1.getMessage());
                                    }
                                }
                            }
                        } catch (IOException e3) {
                            e = e3;
                            reader = reader2;
                        } catch (Throwable th2) {
                            th = th2;
                            reader = reader2;
                        }
                    }
                } catch (IOException e4) {
                    e = e4;
                    try {
                        Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e12) {
                                Slog.d(TAG, "readFileByLines io close exception :" + e12.getMessage());
                            }
                        }
                        if (top_reason.length() > 0) {
                        }
                        if (this.mTopApResumeReasonName != null) {
                        }
                        this.mIssueType = OppoMultiAppManager.USER_ID;
                    } catch (Throwable th3) {
                        th = th3;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e122) {
                                Slog.d(TAG, "readFileByLines io close exception :" + e122.getMessage());
                            }
                        }
                        throw th;
                    }
                }
                if (top_reason.length() > 0) {
                    this.mTopApResumeReasonName = top_reason;
                    this.mTopApResumeCount = top_resume_count;
                }
                if (this.mTopApResumeReasonName != null || ((double) this.mTopApResumeCount) / (((double) this.mElapsedRealTimeDelta) / 3600000.0d) <= 20.0d) {
                    this.mIssueType = OppoMultiAppManager.USER_ID;
                } else if ("wcnss_wlan".equals(this.mTopApResumeReasonName)) {
                    if (this.mWlanUcastDelta > this.mWlanBcastDelta) {
                        this.mIssueType = 111;
                    } else if (this.mWlanUcastDelta < this.mWlanBcastDelta) {
                        this.mIssueType = 112;
                    } else {
                        this.mIssueType = 110;
                    }
                } else if ("modem".equals(this.mTopApResumeReasonName)) {
                    this.mIssueType = 120;
                    if (this.mModemTopWakeUpReason != null) {
                        str2 = this.mModemTopWakeUpReason;
                        if (str2.equals("DIAG_WS")) {
                            this.mIssueType = 3;
                        } else if (str2.equals("QMI_WS")) {
                            this.mModemRilTopMsg = getModemRilTopMsg();
                        } else if (str2.equals("IPA_WS")) {
                            this.mIssueType = 122;
                        }
                    }
                } else if ("qpnp_rtc_alarm".equals(this.mTopApResumeReasonName)) {
                    this.mIssueType = 130;
                } else if ("adsp".equals(this.mTopApResumeReasonName)) {
                    this.mIssueType = OppoProcessManager.MSG_READY_ENTER_STRICTMODE;
                } else if ("sps".equals(this.mTopApResumeReasonName)) {
                    this.mIssueType = 160;
                } else if ("under_layer_wakeup".equals(this.mTopApResumeReasonName)) {
                    this.mIssueType = 170;
                }
            } else {
                this.mIssueType = 190;
            }
        }
    }

    private String getModemRilTopMsg() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        String telephonePowerState = null;
        if (connectivityManager != null) {
            telephonePowerState = connectivityManager.getTelephonyPowerState();
        }
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.i(TAG, "telephonePowerState : " + telephonePowerState);
        }
        if (telephonePowerState == null) {
            return null;
        }
        String[] telephony_event_cache = telephonePowerState.split(" ");
        if (telephony_event_cache == null || telephony_event_cache.length <= 0) {
            return null;
        }
        for (String split : telephony_event_cache) {
            String[] telephony_event = split.split(":");
            if (telephony_event != null && telephony_event.length > 0 && "RILJ_TOP".equals(telephony_event[0].trim())) {
                return telephony_event[1].trim();
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:186:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x0aa0 A:{SYNTHETIC, Splitter: B:159:0x0aa0} */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x0aad A:{SYNTHETIC, Splitter: B:166:0x0aad} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveResult() {
        IOException e;
        Throwable th;
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("OPCM/Standby/").append(this.mVersion).append("\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "OPCM/Standby/" + this.mVersion);
        }
        resultBuilder.append(String.format("Actual current is %.2fmA, target current is %.2fmA.\n", new Object[]{Double.valueOf(this.mRealCurrent), Double.valueOf(this.mIgnoreThreshold)}));
        Object[] objArr = new Object[4];
        objArr[0] = Double.valueOf(this.mSuspendRatio);
        objArr[1] = TimeStamp2Date(this.mScreenOffCurrentTime, DATE_FORMAT);
        objArr[2] = TimeStamp2Date(this.mScreenOnCurrentTime, DATE_FORMAT);
        objArr[3] = Double.valueOf(((((double) this.mElapsedRealTimeDelta) / 60.0d) / 60.0d) / 1000.0d);
        resultBuilder.append(String.format("Suspend ratio is %.2f%%, screen off time is %s, screen on time is %s, duration is %.2fh\n", objArr));
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, String.format("Actual current is %.2fmA, target current is %.2fmA.", new Object[]{Double.valueOf(this.mRealCurrent), Double.valueOf(this.mIgnoreThreshold)}));
            String str = TAG;
            Object[] objArr2 = new Object[4];
            objArr2[0] = Double.valueOf(this.mSuspendRatio);
            objArr2[1] = TimeStamp2Date(this.mScreenOffCurrentTime, DATE_FORMAT);
            objArr2[2] = TimeStamp2Date(this.mScreenOnCurrentTime, DATE_FORMAT);
            objArr2[3] = Double.valueOf(((((double) this.mElapsedRealTimeDelta) / 60.0d) / 60.0d) / 1000.0d);
            Slog.d(str, String.format("Suspend ratio is %.2f%%, screen off time is %s, screen on time is %s, duration is %.2fh", objArr2));
        }
        resultBuilder.append(String.format("DeltaBC is %dmAh, real current witout power lost known is %.2fmA, power lost by telephone is %.2fmAh, power lost by wifi is %.2fmAh.\n", new Object[]{Integer.valueOf(this.mBatteryCapacityDelta - this.mBatteryJumpSocDelta), Double.valueOf(this.mRealCurrentWithoutPowerLostKnown), Double.valueOf(this.mPowerLostByTelephone), Double.valueOf(this.mPowerLostByWifi)}));
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, String.format("DeltaBC is %dmAh, real current witout power lost known is %.2fmA, power lost by telephone is %.2fmAh, power lost by wifi is %.2fmAh.", new Object[]{Integer.valueOf(this.mBatteryCapacityDelta - this.mBatteryJumpSocDelta), Double.valueOf(this.mRealCurrentWithoutPowerLostKnown), Double.valueOf(this.mPowerLostByTelephone), Double.valueOf(this.mPowerLostByWifi)}));
        }
        if (this.mSimCardCount == 0) {
            resultBuilder.append("There is no SIM card\n");
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, "There is no SIM card");
            }
        } else if (this.mSimCardCount == 1) {
            resultBuilder.append("There is 1 SIM card\n");
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, "There is 1 SIM card");
            }
            resultBuilder.append("The SIM card is ").append(this.mSimCard1Name).append("\n");
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, "The SIM card is " + this.mSimCard1Name);
            }
        } else if (this.mSimCardCount == 2) {
            resultBuilder.append("There are 2 SIM cards\n");
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, "There are 2 SIM cards");
            }
            resultBuilder.append("The SIM card 1 is ").append(this.mSimCard1Name).append("\n");
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, "The SIM card 1 is " + this.mSimCard1Name);
            }
            resultBuilder.append("The SIM card 2 is ").append(this.mSimCard2Name).append("\n");
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, "The SIM card 2 is " + this.mSimCard2Name);
            }
        }
        resultBuilder.append("Current active network is ").append(this.mCurrentNetwork).append("\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "Current active network is " + this.mCurrentNetwork);
        }
        resultBuilder.append("Wifi mac is ").append(this.mWifiName).append(" and state is ").append(this.mWifiState).append("\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "Wifi mac is " + this.mWifiName + " and state is " + this.mWifiState);
        }
        resultBuilder.append("Data network is ").append(this.mDataNetworkOperatorName).append(", state is ").append(this.mDataNetworkState).append(", and type is ").append(this.mDataNetworkType).append("\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "Data network is " + this.mDataNetworkOperatorName + ", state is " + this.mDataNetworkState + ", and type is " + this.mDataNetworkType);
        }
        resultBuilder.append("Ap resume information ").append(this.mApResumeReason).append("\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "Ap resume information " + this.mApResumeReason);
        }
        resultBuilder.append("Wifi resume information ").append(this.mWcnInfo).append("\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "Wifi resume information " + this.mWcnInfo);
        }
        resultBuilder.append("Modem resume information ").append(this.mModemInfo).append("\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "Modem resume information " + this.mModemInfo);
        }
        resultBuilder.append(String.format("mMobileDataDelta = %d, mMobileIncrementMaxUid = %d, mMobileIncrementMaxProcess maybe %s, mMobileIncrementMax = %d. mWifiDataDelta = %d, mWifiIncrementMaxUid = %d, mWifiIncrementMaxProcess maybe %s, mWifiIncrementMax = %d.\n", new Object[]{Long.valueOf(this.mMobileDataDelta), Long.valueOf(this.mMobileIncrementMaxUid), this.mMobileIncrementMaxProcess.toString(), Long.valueOf(this.mMobileIncrementMax), Long.valueOf(this.mWifiDataDelta), Long.valueOf(this.mWifiIncrementMaxUid), this.mWifiIncrementMaxProcess.toString(), Long.valueOf(this.mWifiIncrementMax)}));
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, String.format("mMobileDataDelta = %d, mMobileIncrementMaxUid = %d, mMobileIncrementMaxProcess maybe %s, mMobileIncrementMax = %d. mWifiDataDelta = %d, mWifiIncrementMaxUid = %d, mWifiIncrementMaxProcess maybe %s, mWifiIncrementMax = %d.", new Object[]{Long.valueOf(this.mMobileDataDelta), Long.valueOf(this.mMobileIncrementMaxUid), this.mMobileIncrementMaxProcess.toString(), Long.valueOf(this.mMobileIncrementMax), Long.valueOf(this.mWifiDataDelta), Long.valueOf(this.mWifiIncrementMaxUid), this.mWifiIncrementMaxProcess.toString(), Long.valueOf(this.mWifiIncrementMax)}));
        }
        resultBuilder.append(String.format("mVlowStatsScreenDelta = %d, mVminStatsScreenDelta = %d, mApssStatsScreenDelta = %d, mMpssStatsScreenDelta = %d, mAdspStatsScreenDelta = %d, mCdspStatsScreenDelta = %d, mTzStatsScreenDelta = %d.\n", new Object[]{Integer.valueOf(this.mVlowStatsScreenDelta), Integer.valueOf(this.mVminStatsScreenDelta), Integer.valueOf(this.mApssStatsScreenDelta), Integer.valueOf(this.mMpssStatsScreenDelta), Integer.valueOf(this.mAdspStatsScreenDelta), Integer.valueOf(this.mCdspStatsScreenDelta), Integer.valueOf(this.mTzStatsScreenDelta)}));
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, String.format("mVlowStatsScreenDelta = %d, mVminStatsScreenDelta = %d, mApssStatsScreenDelta = %d, mMpssStatsScreenDelta = %d, mAdspStatsScreenDelta = %d, mCdspStatsScreenDelta = %d, mTzStatsScreenDelta = %d.", new Object[]{Integer.valueOf(this.mVlowStatsScreenDelta), Integer.valueOf(this.mVminStatsScreenDelta), Integer.valueOf(this.mApssStatsScreenDelta), Integer.valueOf(this.mMpssStatsScreenDelta), Integer.valueOf(this.mAdspStatsScreenDelta), Integer.valueOf(this.mCdspStatsScreenDelta), Integer.valueOf(this.mTzStatsScreenDelta)}));
        }
        resultBuilder.append(String.format("Android frameworks blocked system suspend total %dms, and android framework blocked ratio is %.2f%%.\n", new Object[]{Long.valueOf(this.mFrameworksBlockedTime), Double.valueOf(this.mFrameworksBlockedRatio)}));
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, String.format("Android frameworks blocked system suspend total %dms, and android framework blocked ratio is %.2f%%.", new Object[]{Long.valueOf(this.mFrameworksBlockedTime), Double.valueOf(this.mFrameworksBlockedRatio)}));
        }
        resultBuilder.append("Top blocked app are:\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "Top blocked app are:");
        }
        if (this.mTopBlockedAppList != null) {
            for (Entry<String, Long> entry : this.mTopBlockedAppList) {
                resultBuilder.append(String.format("App %s blocked suspend %dms.\n", new Object[]{entry.getKey(), entry.getValue()}));
                if (DEBUG_PANIC || this.mIsDebugMode) {
                    Slog.d(TAG, String.format("App %s blocked suspend %dms.", new Object[]{entry.getKey(), entry.getValue()}));
                }
            }
        }
        resultBuilder.append(String.format("Android frameworks totally wake up system %d tims.\n", new Object[]{Integer.valueOf(this.mFrameworkAlarmWakeupTimes)}));
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, String.format("Android frameworks totally wake up system %d tims.", new Object[]{Integer.valueOf(this.mFrameworkAlarmWakeupTimes)}));
        }
        resultBuilder.append("Apps wake up detail are:\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "Apps wake up detail are:");
        }
        long time_minute = (this.mElapsedRealTimeDelta / 60) / 1000;
        for (AlarmCount alarmCount : this.mAppsAlarmWakeupTimes) {
            resultBuilder.append(String.format("App %s wake up system %d times, every %f minute(s) one time\n.", new Object[]{alarmCount.mPackageName, Integer.valueOf(alarmCount.mCount), Double.valueOf((((double) alarmCount.mCount) * 1.0d) / ((double) time_minute))}));
            if (DEBUG_PANIC || this.mIsDebugMode) {
                Slog.d(TAG, String.format("App %s wake up system %d times, every %f minute(s) one time.", new Object[]{alarmCount.mPackageName, Integer.valueOf(alarmCount.mCount), Double.valueOf((((double) alarmCount.mCount) * 1.0d) / ((double) time_minute))}));
            }
        }
        resultBuilder.append("Issue type maybe ").append(this.mIssueType).append("\n");
        if (DEBUG_PANIC || this.mIsDebugMode) {
            Slog.d(TAG, "Issue type maybe " + this.mIssueType);
        }
        File logPath = new File(POWER_LOG_PATH);
        if (!(logPath.exists() && (logPath.isDirectory() ^ 1) == 0)) {
            logPath.mkdirs();
        }
        FileWriter output = null;
        try {
            FileWriter output2 = new FileWriter("data/system/dropbox/powermonitor/standby_" + (this.mIssueType > 9 ? "abnormal" : "normal") + "@" + System.currentTimeMillis() + ".txt");
            try {
                output2.write(resultBuilder.toString());
                output2.flush();
                if (output2 != null) {
                    try {
                        output2.close();
                        return;
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            } catch (IOException e3) {
                e2 = e3;
                output = output2;
                try {
                    e2.printStackTrace();
                    if (output == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                output = output2;
                if (output != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e22 = e4;
            e22.printStackTrace();
            if (output == null) {
                try {
                    output.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
        }
    }

    private void addPkg2EventMap(String tag, List<String> list, Map<String, String> eventMap, StringBuilder sb) {
        if (list != null && !list.isEmpty()) {
            sb.setLength(0);
            for (String pkg : list) {
                sb.append("[");
                sb.append(pkg);
                sb.append("], ");
            }
            eventMap.put(tag, sb.toString());
        }
    }

    private void reportResult() {
        String logTag = DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG;
        String eventId = "PowerMonitor";
        ArrayMap map = new ArrayMap();
        if (this.mIsDebugMode) {
            Slog.d(TAG, "begin: dumpScreenoffBatteryStats.");
        }
        try {
            if (!dumpScreenoffBatteryStats(map)) {
                Slog.d(TAG, "ScreenOffData: dumpScreenoffBatteryStats invalid.");
                map.clear();
            }
            if (this.mIsDebugMode) {
                Slog.d(TAG, "end: dumpScreenoffBatteryStats.");
            }
            List<String> listWhitelist = OppoSysStateManager.getInstance().getOppoGuardElfWhiteList();
            List<String> listPlayback = OppoSysStateManager.getInstance().getOppoGuardElfPlayback();
            List<String> listNavigation = OppoSysStateManager.getInstance().getOppoGuardElfNavigation();
            StringBuilder sb = new StringBuilder(128);
            addPkg2EventMap("notRestrictPkg", listWhitelist, map, sb);
            addPkg2EventMap("playbackPkg", listPlayback, map, sb);
            addPkg2EventMap("navigationPkg", listNavigation, map, sb);
            map.put("AverageCurrent", String.format("%.2f", new Object[]{Double.valueOf(this.mRealCurrent)}));
            map.put("StandCurrent", String.format("%.2f", new Object[]{Double.valueOf(this.mIgnoreThreshold)}));
            map.put("SuspendRatio", String.format("%.2f", new Object[]{Double.valueOf(this.mSuspendRatio)}));
            map.put("ScreenOffTime", String.format("%d", new Object[]{Long.valueOf(this.mScreenOffElapsedRealTime)}));
            map.put("ScreenOnTime", String.format("%d", new Object[]{Long.valueOf(this.mScreenOnElapseRealTime)}));
            double screenoffDuration = ((((double) this.mElapsedRealTimeDelta) / 60.0d) / 60.0d) / 1000.0d;
            map.put("screenoffDuration", String.format("%.2f", new Object[]{Double.valueOf(screenoffDuration)}));
            map.put("DeltaBC", String.format("%d", new Object[]{Integer.valueOf(this.mBatteryCapacityDelta - this.mBatteryJumpSocDelta)}));
            map.put("SimCardCount", String.format("%d", new Object[]{Integer.valueOf(this.mSimCardCount)}));
            map.put("SimCard1", this.mSimCard1Name);
            map.put("SimCard2", this.mSimCard2Name);
            map.put("ActiveNetwork", this.mCurrentNetwork);
            map.put("WifiState", String.format("%d", new Object[]{Integer.valueOf(this.mWifiState)}));
            map.put("BluetoothState", String.format("%d", new Object[]{Integer.valueOf(this.mBluetoothState)}));
            map.put("DataNetwork", this.mDataNetworkOperatorName);
            map.put("DataNetworkState", String.format("%d", new Object[]{Integer.valueOf(this.mDataNetworkState)}));
            map.put("DataNetworkType", String.format("%d", new Object[]{Integer.valueOf(this.mDataNetworkType)}));
            map.put("IssueType", Integer.toString(this.mIssueType));
            map.put("ExtraInfo", this.mExtraInfo);
            map.put("TargetCurrent", String.format("%.2f", new Object[]{Double.valueOf(this.mTargetCurrent)}));
            map.put("WifiDataDelta", String.format("%d", new Object[]{Long.valueOf(this.mWifiDataDelta)}));
            map.put("MobileDataDelta", String.format("%d", new Object[]{Long.valueOf(this.mMobileDataDelta)}));
            map.put("KernelWakelockTime", String.format("%d", new Object[]{this.mKernelWakelockTime}));
            map.put("AodState", String.format("%d", new Object[]{Integer.valueOf(this.mAodState)}));
            map.put("AodDurationSetted", String.format("%d", new Object[]{Integer.valueOf(this.mAodDurationSetted)}));
            map.put("AodBeginHour", String.format("%d", new Object[]{Integer.valueOf(this.mAodBeginHour)}));
            map.put("AodBeginMin", String.format("%d", new Object[]{Integer.valueOf(this.mAodBeginMin)}));
            map.put("AodEndHour", String.format("%d", new Object[]{Integer.valueOf(this.mAodEndHour)}));
            map.put("AodEndMin", String.format("%d", new Object[]{Integer.valueOf(this.mAodEndMin)}));
            map.put("PowerLostByAod", String.format("%.2f", new Object[]{Double.valueOf(this.mPowerLostByAod)}));
            map.put("FlashLightState", String.format("%d", new Object[]{Integer.valueOf(this.mFlashLightState)}));
            map.put("KernelWakelockReason", this.mKernelWakelockReason);
            map.put("KernelMaxWakelockRate", this.mKernelMaxWakelockRate);
            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
            map.put("LogIsOn", Boolean.valueOf(DEBUG_PANIC));
            map.put("PowerLostByWifi", Double.valueOf(this.mPowerLostByWifi));
            map.put("PowerLostByTelephone", Double.valueOf(this.mPowerLostByTelephone));
            map.put("ModemTopWakeUpReason", this.mModemTopWakeUpReason);
            map.put("VminCount", String.format("%d", new Object[]{Integer.valueOf(this.mVminStatsScreenDelta)}));
            map.put("ModemSubsysSuspend", String.format("%d", new Object[]{Integer.valueOf(this.mMpssStatsScreenDelta)}));
            map.put("AdspSubsysSuspend", String.format("%d", new Object[]{Integer.valueOf(this.mAdspStatsScreenDelta)}));
            map.put("ApssSubsysSuspendRatio", String.format("%.2f", new Object[]{Double.valueOf(this.mApssSuspendRatio)}));
            map.put("ModemSubsysSuspendRatio", String.format("%.2f", new Object[]{Double.valueOf(this.mMpssSuspendRatio)}));
            map.put("AdspSubsysSuspendRatio", String.format("%.2f", new Object[]{Double.valueOf(this.mAdspSuspendRatio)}));
            map.put("AndroidFrameworkBlockedTime", String.format("%d", new Object[]{Long.valueOf(this.mFrameworksBlockedTime)}));
            map.put("AndroidFrameworkBlockedRatio", String.format("%.2f", new Object[]{Double.valueOf(this.mFrameworksBlockedRatio)}));
            String topBlockedApp = "";
            if (this.mTopBlockedAppList.size() > 0) {
                topBlockedApp = (String) ((Entry) this.mTopBlockedAppList.get(0)).getKey();
            }
            map.put("TopBlockedApp", topBlockedApp);
            map.put("MobileDataDelta", String.format("%d", new Object[]{Long.valueOf(this.mMobileDataDelta)}));
            map.put("MobileIncrementMaxProcess", this.mMobileIncrementMaxProcess.toString());
            map.put("WifiDataDelta", String.format("%d", new Object[]{Long.valueOf(this.mWifiDataDelta)}));
            map.put("WifiIncrementMaxProcess", this.mWifiIncrementMaxProcess.toString());
            map.put("alarmResumeCycle", String.format("%.2f", new Object[]{Double.valueOf(((double) this.mRtcAlarmWakeupCount) / screenoffDuration)}));
            map.put("wlanResumeCycle", String.format("%.2f", new Object[]{Double.valueOf(((double) this.mWcnssWakeupCount) / screenoffDuration)}));
            map.put("modemResumeCycle", String.format("%.2f", new Object[]{Double.valueOf(((double) this.mModemWakeupCount) / screenoffDuration)}));
            map.put("AdspResumeCycle", String.format("%.2f", new Object[]{Double.valueOf(((double) this.mAdspWakeupCount) / screenoffDuration)}));
            map.put("SpsResumeCycle", String.format("%.2f", new Object[]{Double.valueOf(((double) this.mSpsWakeupCount) / screenoffDuration)}));
            map.put("AlarmWakeupCycle", String.format("%.2f", new Object[]{Double.valueOf(((double) this.mFrameworkAlarmWakeupTimes) / screenoffDuration)}));
            map.put("NetWakeupMaxApp", this.mNetWakeupMaxApp);
            for (int i = 0; i < map.size(); i++) {
                Slog.i(TAG, map.keyAt(i) + ":" + map.valueAt(i));
            }
            OppoStatistics.onCommon(this.mContext, logTag, eventId, map, false);
        } catch (Exception e) {
            Slog.w(TAG, "reportResult Exception");
            map.clear();
        }
    }

    private void reportBatteryLife(int batteryLost, Long batteryTime, int batteryFcc) {
        String logTag = DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG;
        String eventId = "BatteryLifeMonitor";
        ArrayMap map = new ArrayMap();
        if (this.mIsDebugMode) {
            Slog.d(TAG, "reportBatteryLife: " + batteryTime + " ms lost " + batteryLost + "; batteryFcc = " + batteryFcc);
        }
        try {
            map.put("batteryLost", String.format("%d", new Object[]{Integer.valueOf(batteryLost)}));
            map.put("batteryFcc", String.format("%d", new Object[]{Integer.valueOf(batteryFcc)}));
            double batteryTimeDuration = ((((double) batteryTime.longValue()) / 60.0d) / 60.0d) / 1000.0d;
            map.put("batteryTimeDuration", String.format("%.2f", new Object[]{Double.valueOf(batteryTimeDuration)}));
            map.put("AodState", String.format("%d", new Object[]{Integer.valueOf(this.mAodState)}));
            map.put("AodDurationSetted", String.format("%d", new Object[]{Integer.valueOf(this.mAodDurationSetted)}));
            map.put("AodBeginHour", String.format("%d", new Object[]{Integer.valueOf(this.mAodBeginHour)}));
            map.put("AodBeginMin", String.format("%d", new Object[]{Integer.valueOf(this.mAodBeginMin)}));
            map.put("AodEndHour", String.format("%d", new Object[]{Integer.valueOf(this.mAodEndHour)}));
            map.put("AodEndMin", String.format("%d", new Object[]{Integer.valueOf(this.mAodEndMin)}));
            DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
            map.put("LogIsOn", Boolean.valueOf(DEBUG_PANIC));
            if (this.mIsDebugMode) {
                for (int i = 0; i < map.size(); i++) {
                    Slog.i(TAG, map.keyAt(i) + ":" + map.valueAt(i));
                }
            }
            OppoStatistics.onCommon(this.mContext, logTag, eventId, map, false);
        } catch (Exception e) {
            Slog.w(TAG, "reportResult Exception");
            map.clear();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00d6 A:{SYNTHETIC, Splitter: B:38:0x00d6} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00db A:{SYNTHETIC, Splitter: B:41:0x00db} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00b1 A:{SYNTHETIC, Splitter: B:23:0x00b1} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00b6 A:{SYNTHETIC, Splitter: B:26:0x00b6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean dumpScreenoffBatteryStats(Map<String, String> eventMap) {
        IOException e;
        Throwable th;
        BufferedReader bufferedReader = null;
        Process process = null;
        boolean isDumpValid = true;
        try {
            process = Runtime.getRuntime().exec("dumpsys batterystats --screenoffIdle");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            boolean frameworkSupported = false;
            while (true) {
                try {
                    String lineText = reader.readLine();
                    if (lineText == null) {
                        break;
                    }
                    if (this.mIsDebugMode) {
                        Slog.d(TAG, "dumpScreenoffBatteryStats: " + lineText);
                    }
                    if (frameworkSupported && lineText.contains(":  ")) {
                        int split = lineText.indexOf(":  ");
                        String mapVal = lineText.substring(split + 3);
                        String mapKey = lineText.substring(0, split);
                        if (!(mapKey == null || mapVal == null)) {
                            eventMap.put(mapKey, mapVal);
                            Slog.d(TAG, "dumpScreenoffBS: " + mapKey + ":  " + mapVal);
                        }
                    } else if ("ScreenOffBatteryStats".equals(lineText)) {
                        frameworkSupported = true;
                        Slog.d(TAG, "dumpScreenoffBS: frameworkSupported");
                    } else if ("ScreenOffIntervalShort".equals(lineText)) {
                        Slog.d(TAG, "dumpScreenoffBS: ScreenOffIntervalShort");
                        isDumpValid = false;
                    }
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = reader;
                    try {
                        Slog.e(TAG, "failed parsing batterystats  " + e);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                                Slog.e(TAG, "batterystats. failed closing reader  " + e3);
                            }
                        }
                        if (process != null) {
                            try {
                                process.waitFor();
                            } catch (InterruptedException e4) {
                                Slog.e(TAG, "batterystats. failed process waitfor " + e4);
                            }
                            process.destroy();
                        }
                        return isDumpValid;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e32) {
                                Slog.e(TAG, "batterystats. failed closing reader  " + e32);
                            }
                        }
                        if (process != null) {
                            try {
                                process.waitFor();
                            } catch (InterruptedException e42) {
                                Slog.e(TAG, "batterystats. failed process waitfor " + e42);
                            }
                            process.destroy();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = reader;
                    if (bufferedReader != null) {
                    }
                    if (process != null) {
                    }
                    throw th;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e322) {
                    Slog.e(TAG, "batterystats. failed closing reader  " + e322);
                }
            }
            if (process != null) {
                try {
                    process.waitFor();
                } catch (InterruptedException e422) {
                    Slog.e(TAG, "batterystats. failed process waitfor " + e422);
                }
                process.destroy();
            }
        } catch (IOException e5) {
            e322 = e5;
            Slog.e(TAG, "failed parsing batterystats  " + e322);
            if (bufferedReader != null) {
            }
            if (process != null) {
            }
            return isDumpValid;
        }
        return isDumpValid;
    }

    private void onScreenOff() {
        if (allowMonitor()) {
            screenOffClear();
            getScreenOffTimeInfo();
            getScreenOffVirtualKeyInterruptsInfo();
            getScreenOffNetworkStatsInfo();
            getScreenOffAudioProcessInfo();
            getScreenOffModemLogStatusDetect();
            getScreenOffAodStatus();
            getScreenOffFlashLightStatus();
            getScreenOffBatteryInfo();
            getOppoRpmStatsScreenOff();
            getOppoRpmMasterStatsScreenOff();
            getWlanWakeupLogScreenOff();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00be A:{SYNTHETIC, Splitter: B:32:0x00be} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00e5 A:{SYNTHETIC, Splitter: B:38:0x00e5} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getBatteryFcc() {
        IOException e;
        Throwable th;
        File file = new File(BATTERY_MAGIC_FCC);
        BufferedReader reader = null;
        if (file.exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                try {
                    String tempString = reader2.readLine();
                    if (tempString != null) {
                        if (DEBUG_PANIC || this.mIsDebugMode) {
                            Slog.d(TAG, "getBatteryFcc readLine :" + tempString);
                        }
                        int parseInt = Integer.parseInt(tempString);
                        if (reader2 != null) {
                            try {
                                reader2.close();
                            } catch (IOException e1) {
                                Slog.d(TAG, "readFileByLines io close exception :" + e1.getMessage());
                            }
                        }
                        return parseInt;
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e12) {
                            Slog.d(TAG, "readFileByLines io close exception :" + e12.getMessage());
                        }
                    }
                    reader = reader2;
                    return -1;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                        if (reader != null) {
                        }
                        return -1;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e122) {
                                Slog.d(TAG, "readFileByLines io close exception :" + e122.getMessage());
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1222) {
                        Slog.d(TAG, "readFileByLines io close exception :" + e1222.getMessage());
                    }
                }
                return -1;
            }
        }
        Slog.d(TAG, "/sys/class/power_supply/battery/batt_fcc not exists");
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0078 A:{SYNTHETIC, Splitter: B:21:0x0078} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00c6 A:{SYNTHETIC, Splitter: B:34:0x00c6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getKernelWakeLockTime() {
        IOException e;
        Throwable th;
        File file = new File(KERNEL_WAKELOCK_TIME_PATH);
        BufferedReader reader = null;
        if (file.exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String tempString = reader2.readLine();
                        if (tempString == null) {
                            break;
                        }
                        if (DEBUG_PANIC || this.mIsDebugMode) {
                            Slog.d(TAG, "getKernelWakeLockTime readLine :" + tempString);
                        }
                        this.mKernelWakelockTime = Long.valueOf(Long.parseLong(tempString));
                    } catch (IOException e2) {
                        e = e2;
                        reader = reader2;
                        try {
                            Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                            if (reader != null) {
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e1) {
                                    Slog.d(TAG, "readFileByLines io close exception :" + e1.getMessage());
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        reader = reader2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e12) {
                        Slog.d(TAG, "readFileByLines io close exception :" + e12.getMessage());
                    }
                }
            } catch (IOException e3) {
                e = e3;
                Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.d(TAG, "readFileByLines io close exception :" + e122.getMessage());
                    }
                }
                return;
            }
            return;
        }
        Slog.d(TAG, "/sys/kernel/wakelock_profiler/kernel_time not exists");
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00e8 A:{SYNTHETIC, Splitter: B:36:0x00e8} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a3 A:{SYNTHETIC, Splitter: B:28:0x00a3} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0110 A:{SYNTHETIC, Splitter: B:42:0x0110} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getWlanWakeupLogScreenOn() {
        IOException e;
        NumberFormatException e2;
        Throwable th;
        File file = new File(WCN_WAKEUP_LOG_PATH);
        BufferedReader reader = null;
        if (file.exists()) {
            try {
                String[] wlanCount;
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                do {
                    try {
                        String tempString = reader2.readLine();
                        if (tempString == null) {
                            break;
                        }
                        if (DEBUG_PANIC || this.mIsDebugMode) {
                            Slog.d(TAG, "getWlanWakeupLogScreenOn readLine :" + tempString);
                        }
                        wlanCount = tempString.split(":");
                    } catch (IOException e3) {
                        e = e3;
                        reader = reader2;
                        Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e1) {
                                Slog.d(TAG, "readFileByLines io close exception :" + e1.getMessage());
                            }
                        }
                        return;
                    } catch (NumberFormatException e4) {
                        e2 = e4;
                        reader = reader2;
                        try {
                            e2.printStackTrace();
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e12) {
                                    Slog.d(TAG, "readFileByLines io close exception :" + e12.getMessage());
                                }
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (reader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        reader = reader2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e122) {
                                Slog.d(TAG, "readFileByLines io close exception :" + e122.getMessage());
                            }
                        }
                        throw th;
                    }
                } while (wlanCount.length != 2);
                this.mWlanUcastScreenOnCount = Long.parseLong(wlanCount[0]);
                this.mWlanBcastScreenOnCount = Long.parseLong(wlanCount[1]);
                this.mWlanUcastDelta = this.mWlanUcastScreenOnCount - this.mWlanUcastScreenOffCount;
                this.mWlanBcastDelta = this.mWlanBcastScreenOnCount - this.mWlanBcastScreenOffCount;
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e1222) {
                        Slog.d(TAG, "readFileByLines io close exception :" + e1222.getMessage());
                    }
                }
                reader = reader2;
            } catch (IOException e5) {
                e = e5;
                Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                if (reader != null) {
                }
                return;
            } catch (NumberFormatException e6) {
                e2 = e6;
                e2.printStackTrace();
                if (reader != null) {
                }
                return;
            }
            return;
        }
        Slog.d(TAG, "/proc/wlan_wakeup_log not exists");
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00da A:{SYNTHETIC, Splitter: B:36:0x00da} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0095 A:{SYNTHETIC, Splitter: B:28:0x0095} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0102 A:{SYNTHETIC, Splitter: B:42:0x0102} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getWlanWakeupLogScreenOff() {
        IOException e;
        NumberFormatException e2;
        Throwable th;
        File file = new File(WCN_WAKEUP_LOG_PATH);
        BufferedReader reader = null;
        if (file.exists()) {
            try {
                String[] wlanCount;
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                do {
                    try {
                        String tempString = reader2.readLine();
                        if (tempString == null) {
                            break;
                        }
                        if (DEBUG_PANIC || this.mIsDebugMode) {
                            Slog.d(TAG, "getWlanWakeupLogScreenOff readLine :" + tempString);
                        }
                        wlanCount = tempString.split(":");
                    } catch (IOException e3) {
                        e = e3;
                        reader = reader2;
                        Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                        if (reader != null) {
                        }
                        return;
                    } catch (NumberFormatException e4) {
                        e2 = e4;
                        reader = reader2;
                        try {
                            e2.printStackTrace();
                            if (reader != null) {
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (reader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        reader = reader2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e1) {
                                Slog.d(TAG, "readFileByLines io close exception :" + e1.getMessage());
                            }
                        }
                        throw th;
                    }
                } while (wlanCount.length != 2);
                this.mWlanUcastScreenOffCount = Long.parseLong(wlanCount[0]);
                this.mWlanBcastScreenOffCount = Long.parseLong(wlanCount[1]);
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e12) {
                        Slog.d(TAG, "readFileByLines io close exception :" + e12.getMessage());
                    }
                }
                reader = reader2;
            } catch (IOException e5) {
                e = e5;
                Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.d(TAG, "readFileByLines io close exception :" + e122.getMessage());
                    }
                }
                return;
            } catch (NumberFormatException e6) {
                e2 = e6;
                e2.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1222) {
                        Slog.d(TAG, "readFileByLines io close exception :" + e1222.getMessage());
                    }
                }
                return;
            }
            return;
        }
        Slog.d(TAG, "/proc/wlan_wakeup_log not exists");
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0090 A:{SYNTHETIC, Splitter: B:23:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00de A:{SYNTHETIC, Splitter: B:36:0x00de} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getKernelMaxWakeLock() {
        IOException e;
        Throwable th;
        File file = new File(KERNEL_WAKELOCK_SOURCE_PATH);
        BufferedReader reader = null;
        this.mKernelMaxWakelockRate = null;
        this.mKernelWakelockReason = null;
        if (file.exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String tempString = reader2.readLine();
                        if (tempString == null) {
                            break;
                        }
                        if (DEBUG_PANIC || this.mIsDebugMode) {
                            Slog.d(TAG, "getTopKernelWakeupReason readLine :" + tempString);
                        }
                        String[] reason_set = tempString.split("\t");
                        if (reason_set.length == 3) {
                            this.mKernelWakelockReason = reason_set[0].trim();
                            this.mKernelMaxWakelockRate = reason_set[2].trim();
                        }
                    } catch (IOException e2) {
                        e = e2;
                        reader = reader2;
                        try {
                            Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e1) {
                                    Slog.d(TAG, "readFileByLines io close exception :" + e1.getMessage());
                                }
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (reader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        reader = reader2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e12) {
                                Slog.d(TAG, "readFileByLines io close exception :" + e12.getMessage());
                            }
                        }
                        throw th;
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e122) {
                        Slog.d(TAG, "readFileByLines io close exception :" + e122.getMessage());
                    }
                }
            } catch (IOException e3) {
                e = e3;
                Slog.d(TAG, "readFileByLines io exception:" + e.getMessage());
                if (reader != null) {
                }
                return;
            }
            return;
        }
        Slog.d(TAG, "/sys/kernel/wakelock_profiler/active_max not exists");
    }

    private void onScreenOn() {
        if (isTimeEnough() && (allowMonitor() ^ 1) == 0) {
            getScreenOnTimeInfo();
            getScreenOnVirtualKeyInterruptsInfo();
            getScreenOnNetworkStatsInfo();
            getScreenOnBatteryInfo();
            getPhoneStateInfo();
            getTopApResumeReason();
            getWcnResumeInfo();
            getModemResumeInfo();
            getRtcAlarmInfo();
            getOppoRpmStatsScreenOn();
            getOppoRpmMasterStatsScreenOn();
            getOppoRpmStatsDelta();
            getOppoRpmMasterStatsDelta();
            computePowerMonitorResult();
            getKernelWakeLockTime();
            getWakeLockInfo();
            getKernelMaxWakeLock();
            this.mNetWakeupMaxApp = NetWakeManager.getMaxWakeupApp();
            getWlanWakeupLogScreenOn();
            getIssueType();
            this.mExtraInfo = "";
            Intent statusNotifyIntent = new Intent("oppo.intent.action.ACTION_OPPO_POWER_STANDBY_CURRENT");
            statusNotifyIntent.putExtra("OffTime", this.mScreenOffCurrentTime);
            statusNotifyIntent.putExtra("Duration", this.mElapsedRealTimeDelta);
            statusNotifyIntent.putExtra("Suspend", this.mSuspendRatio);
            statusNotifyIntent.putExtra("Average", this.mRealCurrent);
            statusNotifyIntent.putExtra("Wifi", this.mWifiState);
            statusNotifyIntent.putExtra("Network", this.mDataNetworkState);
            statusNotifyIntent.putExtra("NetworkOperator", this.mDataNetworkOperatorName);
            statusNotifyIntent.putExtra("SIM1", this.mSimCard1Name);
            statusNotifyIntent.putExtra("SIM2", this.mSimCard2Name);
            statusNotifyIntent.putExtra("Reason", this.mIssueType);
            if (this.mIssueType == 1) {
                this.mExtraInfo = this.mKeepAudioProcess;
            } else if (this.mIssueType == 2) {
                String downloadProcess = "";
                if (this.mWifiDataDelta > 0) {
                    downloadProcess = this.mWifiIncrementMaxProcess.toString();
                } else if (this.mMobileDataDelta > 0) {
                    downloadProcess = this.mMobileIncrementMaxProcess.toString();
                }
                Slog.d(TAG, "downloadProcess is  :" + downloadProcess);
                this.mExtraInfo = downloadProcess;
            } else if (this.mIssueType == 130) {
                if (this.mAppsAlarmWakeupTimes.size() > 0) {
                    this.mExtraInfo = ((AlarmCount) this.mAppsAlarmWakeupTimes.get(0)).mPackageName;
                }
            } else if (this.mIssueType == OppoProcessManager.MSG_UPLOAD) {
                if (this.mTopBlockedAppList.size() > 0) {
                    this.mExtraInfo = (String) ((Entry) this.mTopBlockedAppList.get(0)).getKey();
                }
            } else if (this.mIssueType == 120 || this.mIssueType == 1411) {
                this.mExtraInfo = this.mModemRilTopMsg;
            } else if (this.mIssueType == OppoMultiAppManager.USER_ID) {
                this.mExtraInfo = this.mKernelWakelockReason;
            } else if (this.mIssueType == 190) {
                this.mExtraInfo = this.mKernelWakelockReason;
            } else if (this.mIssueType == 122 || this.mIssueType == 1412 || this.mIssueType == 1413 || this.mIssueType == 110 || this.mIssueType == 111) {
                this.mExtraInfo = this.mNetWakeupMaxApp;
            }
            statusNotifyIntent.putExtra("ExtraInfo", this.mExtraInfo);
            this.mContext.sendBroadcastAsUser(statusNotifyIntent, UserHandle.ALL);
            Slog.i(TAG, "oppo.intent.action.ACTION_OPPO_POWER_STANDBY_CURRENT");
            saveResult();
            reportResult();
            if (needTip()) {
                boolean needDoSomethingOther = needDoSomethingOther();
                screenOnClear();
                return;
            }
            screenOnClear();
        }
    }

    private boolean allowMonitor() {
        return (isModemLogOn() || (this.mIsSpecialVersion ^ 1) == 0) ? false : SystemProperties.getBoolean("persist.sys.oppopcm.enable", true);
    }

    private boolean isTimeEnough() {
        return SystemClock.elapsedRealtime() - this.mScreenOffElapsedRealTime > this.mSampleTimeThreshold;
    }

    private boolean needTip() {
        if (!mIsReleaseType && this.mRealCurrent > this.mIgnoreThreshold) {
            return true;
        }
        return false;
    }

    private boolean needDoSomethingOther() {
        if (this.mSuspendRatio <= 97.0d || this.mRealCurrent - this.mTargetCurrent <= 20.0d) {
            return false;
        }
        return true;
    }

    private boolean isModemLogOn() {
        if (this.mIsModemLogOn) {
            Slog.d(TAG, "md_log on...");
        }
        return this.mIsModemLogOn;
    }

    private boolean isSpecialVersion() {
        PackageManager packageManager = this.mContext.getPackageManager();
        boolean equals = (packageManager.hasSystemFeature("oppo.cta.support") || packageManager.hasSystemFeature("oppo.cmcc.test") || packageManager.hasSystemFeature("oppo.all.cutest") || packageManager.hasSystemFeature("oppo.allnet.cttest") || packageManager.hasSystemFeature("oppo.all.cttest") || packageManager.hasSystemFeature("oppo.all.cmcctest")) ? true : LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
        this.mIsSpecialVersion = equals;
        return this.mIsSpecialVersion;
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x007b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x007b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x007b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0066 A:{SYNTHETIC, Splitter: B:43:0x0066} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x007b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0056 A:{SYNTHETIC, Splitter: B:34:0x0056} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x007b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0046 A:{SYNTHETIC, Splitter: B:25:0x0046} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x007b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0072 A:{SYNTHETIC, Splitter: B:49:0x0072} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getBatteryRealtimeCapacity() {
        FileNotFoundException e;
        IOException e2;
        NumberFormatException e3;
        Throwable th;
        int realtimeBatteryCapacity = 0;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(BATTERY_REALTIME_CAPACITY)));
            while (true) {
                try {
                    String tempString = reader2.readLine();
                    if (tempString == null) {
                        break;
                    } else if (tempString.length() != 0) {
                        realtimeBatteryCapacity = Integer.parseInt(tempString.trim());
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    reader = reader2;
                    e.printStackTrace();
                    realtimeBatteryCapacity = -1;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (realtimeBatteryCapacity == -1) {
                    }
                } catch (IOException e5) {
                    e22 = e5;
                    reader = reader2;
                    e22.printStackTrace();
                    realtimeBatteryCapacity = -1;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    if (realtimeBatteryCapacity == -1) {
                    }
                } catch (NumberFormatException e6) {
                    e3 = e6;
                    reader = reader2;
                    try {
                        e3.printStackTrace();
                        realtimeBatteryCapacity = -1;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        if (realtimeBatteryCapacity == -1) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e222222) {
                    e222222.printStackTrace();
                }
            }
            reader = reader2;
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            realtimeBatteryCapacity = -1;
            if (reader != null) {
            }
            if (realtimeBatteryCapacity == -1) {
            }
        } catch (IOException e8) {
            e222222 = e8;
            e222222.printStackTrace();
            realtimeBatteryCapacity = -1;
            if (reader != null) {
            }
            if (realtimeBatteryCapacity == -1) {
            }
        } catch (NumberFormatException e9) {
            e3 = e9;
            e3.printStackTrace();
            realtimeBatteryCapacity = -1;
            if (reader != null) {
            }
            if (realtimeBatteryCapacity == -1) {
            }
        }
        if (realtimeBatteryCapacity == -1) {
            return (this.mBatteryCapacity * this.mBatteryLevel) / 100;
        }
        return realtimeBatteryCapacity;
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0059 A:{SYNTHETIC, Splitter: B:37:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x004a A:{SYNTHETIC, Splitter: B:29:0x004a} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003b A:{SYNTHETIC, Splitter: B:21:0x003b} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0065 A:{SYNTHETIC, Splitter: B:43:0x0065} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getBatteryJumpSoc() {
        FileNotFoundException e;
        IOException e2;
        NumberFormatException e3;
        Throwable th;
        int batteryJumpSoc = 0;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(BATTERY_JUMP_SOC)));
            while (true) {
                try {
                    String tempString = reader2.readLine();
                    if (tempString == null) {
                        break;
                    } else if (tempString.length() != 0) {
                        batteryJumpSoc = Integer.parseInt(tempString.trim());
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    reader = reader2;
                    e.printStackTrace();
                    if (reader != null) {
                    }
                    return batteryJumpSoc;
                } catch (IOException e5) {
                    e2 = e5;
                    reader = reader2;
                    e2.printStackTrace();
                    if (reader != null) {
                    }
                    return batteryJumpSoc;
                } catch (NumberFormatException e6) {
                    e3 = e6;
                    reader = reader2;
                    try {
                        e3.printStackTrace();
                        if (reader != null) {
                        }
                        return batteryJumpSoc;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
            reader = reader2;
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2222) {
                    e2222.printStackTrace();
                }
            }
            return batteryJumpSoc;
        } catch (IOException e8) {
            e2222 = e8;
            e2222.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e22222) {
                    e22222.printStackTrace();
                }
            }
            return batteryJumpSoc;
        } catch (NumberFormatException e9) {
            e3 = e9;
            e3.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e222222) {
                    e222222.printStackTrace();
                }
            }
            return batteryJumpSoc;
        }
        return batteryJumpSoc;
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Power Monitor service (power_monitor) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  get");
        pw.println("    Get value of the key");
        pw.println("  set");
        pw.println("    Set value of the key");
    }

    private void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            if (args != null) {
                if (args.length != 0) {
                    if (!"-a".equals(args[0])) {
                        new Shell().exec(this.mBinderService, null, fd, null, args, null, new ResultReceiver(null));
                    }
                }
            }
            pw.println("Current Power Monitor Service state:");
            pw.println("  DEBUG_PANIC = " + DEBUG_PANIC);
            pw.println("  mBootCompleted = " + this.mBootCompleted);
            pw.println("  mIsReleaseType = " + mIsReleaseType);
            pw.println("  mIsPowerMonitoring = " + this.mIsPowerMonitoring);
            pw.println("  mIsScreenOn = " + this.mIsScreenOn);
            pw.println("  mIsSpecialVersion = " + this.mIsSpecialVersion);
            pw.println("  mIsDebugMode = " + this.mIsDebugMode);
            pw.println("  mHighTemperatureTheshold = " + this.mHighTemperatureTheshold);
            pw.println("  mHighCurrentThresholdScreenOff = " + this.mHighCurrentThresholdScreenOff);
            pw.println("  mHighCurrentThresholdScreenOn = " + this.mHighCurrentThresholdScreenOn);
            pw.println("  mBatteryCapacity = " + this.mBatteryCapacity);
            pw.println("  mBatteryLevel = " + this.mBatteryLevel);
            pw.println("  mIsPowered = " + this.mIsPowered);
            pw.println("  mIsOverTemperature = " + this.mIsOverTemperature);
            pw.println("  mSampleTimeThreshold = " + this.mSampleTimeThreshold);
            pw.println("  mScreenOffElapsedRealTime = " + this.mScreenOffElapsedRealTime);
            pw.println("  mScreenOffUpTime = " + this.mScreenOffUpTime);
            pw.println("  mScreenOnElapseRealTime = " + this.mScreenOnElapseRealTime);
            pw.println("  mScreenOnUpTime = " + this.mScreenOnUpTime);
            pw.println("  mElapsedRealTimeDelta = " + this.mElapsedRealTimeDelta);
            pw.println("  mUpTimeDelta = " + this.mUpTimeDelta);
            pw.println("  mSuspendRatio = " + this.mSuspendRatio);
            pw.println("  mVirtualKeyInterruptsScreenOff = " + this.mVirtualKeyInterruptsScreenOff);
            pw.println("  mVirtualKeyInterruptsScreenOn = " + this.mVirtualKeyInterruptsScreenOn);
            pw.println("  mVirtualKeyInterrupts = " + this.mVirtualKeyInterrupts);
            pw.println("  mApResumeReason = " + this.mApResumeReason);
            pw.println("  mTopApResumeReasonName = " + this.mTopApResumeReasonName);
            pw.println("  mTopApResumeCount = " + this.mTopApResumeCount);
            pw.println("  mModemInfo = " + this.mModemInfo);
            pw.println("  mWcnInfo = " + this.mWcnInfo);
            pw.println("  mWifiPowerEventList = " + this.mWifiPowerEventList);
            pw.println("  mWifiIncrementMax = " + this.mWifiIncrementMax);
            pw.println("  mWifiIncrementMaxUid = " + this.mWifiIncrementMaxUid);
            pw.println("  mWifiIncrementMaxProcess = " + this.mWifiIncrementMaxProcess.toString());
            pw.println("  mMobileIncrementMax = " + this.mMobileIncrementMax);
            pw.println("  mMobileIncrementMaxUid = " + this.mMobileIncrementMaxUid);
            pw.println("  mMobileIncrementMaxProcess = " + this.mMobileIncrementMaxProcess.toString());
            pw.println("  mWifiDataDelta = " + this.mWifiDataDelta);
            pw.println("  mMobileDataDelta = " + this.mMobileDataDelta);
            pw.println("  mDownloadScene = " + this.mDownloadScene);
            pw.println("  mMusicScene = " + this.mMusicScene);
            pw.println("  mKeepAudioProcess = " + this.mKeepAudioProcess);
            pw.println("  mBatteryCapacityScreenOff = " + this.mBatteryCapacityScreenOff);
            pw.println("  mBatteryJumpSocScreenOff = " + this.mBatteryJumpSocScreenOff);
            pw.println("  mBatteryCapacityScreenOn = " + this.mBatteryCapacityScreenOn);
            pw.println("  mBatteryJumpSocScreenOn = " + this.mBatteryJumpSocScreenOn);
            pw.println("  mRealCurrent = " + this.mRealCurrent);
            pw.println("  mBatteryCapacityDelta = " + this.mBatteryCapacityDelta);
            pw.println("  mBatteryJumpSocDelta = " + this.mBatteryJumpSocDelta);
            pw.println("  mPowerLostByTelephone = " + this.mPowerLostByTelephone);
            pw.println("  mPowerLostByWifi = " + this.mPowerLostByWifi);
            pw.println("  mRealCurrentWithoutPowerLostKnown = " + this.mRealCurrentWithoutPowerLostKnown);
            pw.println("  mSimCardCount = " + this.mSimCardCount);
            pw.println("  mSimCard1Name = " + this.mSimCard1Name);
            pw.println("  mSimCard2Name = " + this.mSimCard2Name);
            pw.println("  mCurrentNetwork = " + this.mCurrentNetwork);
            pw.println("  mDataNetworkOperatorName = " + this.mDataNetworkOperatorName);
            pw.println("  mDataNetworkState = " + this.mDataNetworkState);
            pw.println("  mDataNetworkType = " + this.mDataNetworkType);
            pw.println("  mWifiName = " + this.mWifiName);
            pw.println("  mWifiState = " + this.mWifiState);
            pw.println("  mBluetoothState = " + this.mBluetoothState);
            pw.println("  mVersion = " + this.mVersion);
            pw.println("  mBaseCurrent = " + this.mBaseCurrent);
            pw.println("  mNoSimCardDelta = " + this.mNoSimCardDelta);
            pw.println("  mSingleSimCardDelta = " + this.mSingleSimCardDelta);
            pw.println("  mDoubleSimCardDelta = " + this.mDoubleSimCardDelta);
            pw.println("  mWifiDelta = " + this.mWifiDelta);
            pw.println("  mBluetoothDelta = " + this.mBluetoothDelta);
            pw.println("  mWifiBluetoothDelta = " + this.mWifiBluetoothDelta);
            pw.println("  mFloateDelta = " + this.mFloateDelta);
            pw.println("  mTargetCurrent = " + this.mTargetCurrent);
            pw.println("  mIgnoreThreshold = " + this.mIgnoreThreshold);
            pw.println("  mIssueType = " + this.mIssueType);
        }
    }

    int onShellCommand(Shell shell, String cmd) {
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        if (cmd.equals("set")) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            String key = shell.getNextArg();
            if (key == null) {
                pw.println("No key specified");
                return -1;
            }
            String value = shell.getNextArg();
            if (value == null) {
                pw.println("No value specified");
                return -1;
            } else if (key.equals("mIsDebugMode")) {
                this.mIsDebugMode = Boolean.parseBoolean(value);
            } else if (key.equals("mIsPowerMonitoring")) {
                this.mIsPowerMonitoring = Boolean.parseBoolean(value);
            } else if (key.equals("mIsScreenOn")) {
                this.mIsScreenOn = Boolean.parseBoolean(value);
            } else if (key.equals("mIsSpecialVersion")) {
                this.mIsSpecialVersion = Boolean.parseBoolean(value);
            } else if (key.equals("mBatteryCapacity")) {
                this.mBatteryCapacity = Integer.parseInt(value);
            } else if (key.equals("mBatteryLevel")) {
                this.mBatteryLevel = Integer.parseInt(value);
            } else if (key.equals("mIsOverTemperature")) {
                this.mIsOverTemperature = Boolean.parseBoolean(value);
            } else if (key.equals("mSampleTimeThreshold")) {
                this.mSampleTimeThreshold = (Long.parseLong(value) * 60) * 1000;
                this.WIFI_DATA_PER_HOUR_THRESHOLD *= 10;
                this.MOBILE_DATA_PER_HOUR_THRESHOLD *= 10;
            } else {
                pw.println("Unknown set option: " + key);
            }
        } else if (!cmd.equals("call")) {
            return shell.handleDefaultCommands(cmd);
        } else {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            String function = shell.getNextArg();
            if (function == null) {
                pw.println("No function specified");
                return -1;
            } else if (function.equals("allowMonitor")) {
                allowMonitor();
            } else if (function.equals("screenOffClear")) {
                screenOffClear();
            } else if (function.equals("getScreenOffTimeInfo")) {
                getScreenOffTimeInfo();
            } else if (function.equals("getScreenOffVirtualKeyInterruptsInfo")) {
                getScreenOffVirtualKeyInterruptsInfo();
            } else if (function.equals("getScreenOffNetworkStatsInfo")) {
                getScreenOffNetworkStatsInfo();
            } else if (function.equals("getScreenOffAudioProcessInfo")) {
                getScreenOffAudioProcessInfo();
            } else if (function.equals("getScreenOffBatteryInfo")) {
                getScreenOffBatteryInfo();
            } else if (function.equals("isTimeEnough")) {
                isTimeEnough();
            } else if (function.equals("getScreenOnTimeInfo")) {
                getScreenOnTimeInfo();
            } else if (function.equals("getScreenOnVirtualKeyInterruptsInfo")) {
                getScreenOnVirtualKeyInterruptsInfo();
            } else if (function.equals("getScreenOnNetworkStatsInfo")) {
                getScreenOnNetworkStatsInfo();
            } else if (function.equals("getScreenOnBatteryInfo")) {
                getScreenOnBatteryInfo();
            } else if (function.equals("getPhoneStateInfo")) {
                getPhoneStateInfo();
            } else if (function.equals("computePowerMonitorResult")) {
                computePowerMonitorResult();
            } else if (function.equals("getIssueType")) {
                getIssueType();
            } else if (function.equals("reportResult")) {
                reportResult();
            } else if (function.equals("saveResult")) {
                saveResult();
            } else if (function.equals("needTip")) {
                needTip();
            } else if (function.equals("needDoSomethingOther")) {
                needDoSomethingOther();
            } else if (function.equals("onScreenOff")) {
                onScreenOff();
            } else if (function.equals("onScreenOn")) {
                onScreenOn();
            } else if (function.equals("screenOnClear")) {
                screenOnClear();
            } else if (function.equals("getTopApResumeReason")) {
                getTopApResumeReason();
            } else if (function.equals("getWcnResumeInfo")) {
                getWcnResumeInfo();
            } else if (function.equals("getModemResumeInfo")) {
                getModemResumeInfo();
            } else if (function.equals("isModemLogOn")) {
                isModemLogOn();
            } else if (function.equals("isSpecialVersion")) {
                isSpecialVersion();
            } else {
                pw.println("Unknown call option: " + function);
            }
        }
        return 0;
    }
}
