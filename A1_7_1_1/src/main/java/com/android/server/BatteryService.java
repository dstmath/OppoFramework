package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkStats;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManagerInternal;
import android.os.BatteryProperties;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBatteryPropertiesListener.Stub;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.IOppoUsageService;
import android.os.Message;
import android.os.OppoManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemService;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.os.PowerProfile;
import com.android.server.am.BatteryStatsService;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.coloros.OppoSysStateManagerInternal;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.net.NetworkStatsService;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import oppo.util.OppoStatistics;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class BatteryService extends SystemService {
    private static final String AP_WAKEUP_SOURCE_PATH = "/sys/kernel/wakeup_reasons/ap_resume_reason_stastics";
    private static final int BATTERY_PLUGGED_NONE = 0;
    private static final int BATTERY_SCALE = 100;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static boolean DEBUG = false;
    private static boolean DEBUG_COMMAND = false;
    private static boolean DEBUG_PANIC = false;
    private static final DecimalFormat DECIMAL_FORMAT = null;
    private static final String[] DUMPSYS_ARGS = null;
    private static final String DUMPSYS_DATA_PATH = "/data/system/";
    private static final int HIGH_TEMPERATURE_THRESHOLD = 500;
    private static final String INTERRUPTS_PATH = "/proc/interrupts";
    private static final String IPO_POWER_OFF = "android.intent.action.ACTION_SHUTDOWN_IPO";
    private static final String IPO_POWER_ON = "android.intent.action.ACTION_BOOT_IPO";
    private static final String MODEM_INFO_PATH = "/sys/kernel/wakeup_reasons/modem_resume_reason_stastics";
    private static final int ONE_HOUR_MILLS = 3600000;
    private static final String TAG = null;
    private static final String TAG_LED = "BatteryLed";
    private static final String TAG_PM = "PowerMonitor";
    private static final String VIRTUAL_KEY_INTERRUPT_NAME = "VIRTUAL_KEY-eint";
    private static final int VIRTUAL_KEY_INTERRUPT_THRESHOLD = 10;
    private static final String WCN_INFO_PATH = "/sys/kernel/wakeup_reasons/wcn_resume_reason_stastics";
    private boolean LowLevelFlag;
    private long MOBILE_DATA_PER_HOUR_THRESHOLD;
    private int SAMPLE_TIME;
    private int SAMPLE_TIME_WITHOUT_BATTERYCOUNTER;
    private long WIFI_DATA_PER_HOUR_THRESHOLD;
    private boolean ipo_led_off;
    private boolean ipo_led_on;
    private boolean isServiceBinded;
    private double mAverageMobileDataPerHour;
    private double mAverageWifiDataPerHour;
    private double mBatteryCapacityDelta;
    private double mBatteryCapacityDeviation;
    private double mBatteryCapacityFromPowerProfile;
    private double mBatteryCapacityScreenOff;
    private double mBatteryCapacityScreenOn;
    private int mBatteryJumpSocDelta;
    private int mBatteryJumpSocScreenOff;
    private int mBatteryJumpSocScreenOn;
    private boolean mBatteryLevelCritical;
    private boolean mBatteryLevelLow;
    private int mBatteryNotifyCode;
    private BatteryProperties mBatteryProps;
    private final IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mBootCompleted;
    private SettingsObserver mChargeSettingsObservers;
    private ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private int mCriticalBatteryLevel;
    private String mCurrentNetwork;
    private int mCurrentSampleTime;
    private String mDataNetworkOperatorName;
    private int mDataNetworkState;
    private int mDataNetworkType;
    private Runnable mDetectAudioProcessRunnable;
    private int mDischargeStartLevel;
    private long mDischargeStartTime;
    private boolean mDownloadScene;
    private long mElapsedRealtimeDelta;
    private long mElapsedRealtimeScreenOff;
    private long mElapsedRealtimeScreenOn;
    private double mFrameworksBlockedRatio;
    private long mFrameworksBlockedTime;
    private final Handler mHandler;
    private boolean mIPOBoot;
    private boolean mIPOShutdown;
    private boolean mIPOed;
    private BroadcastReceiver mIntentReceiver;
    private int mInvalidCharger;
    private boolean mIsMusicScene;
    private boolean mIsOverTemperature;
    private boolean mIsSellModeVersion;
    private String mKeepAudioProcess;
    private int mLastBatteryHealth;
    private int mLastBatteryLevel;
    private boolean mLastBatteryLevelCritical;
    private int mLastBatteryLevel_smb;
    private int mLastBatteryNotifyCode;
    private boolean mLastBatteryPresent;
    private boolean mLastBatteryPresent_smb;
    private final BatteryProperties mLastBatteryProps;
    private int mLastBatteryStatus;
    private int mLastBatteryStatus_smb;
    private int mLastBatteryTemperature;
    private int mLastBatteryVoltage;
    private int mLastChargeCounter;
    private int mLastInvalidCharger;
    private int mLastMaxChargeCurrent;
    private int mLastMaxChargeTemperature;
    private int mLastMaxChargingCurrent;
    private int mLastMaxChargingVoltage;
    private int mLastMinChargeTemperature;
    private long mLastNetworkStatsUpdateTime;
    private int mLastOtgOnline;
    private int mLastPlugType;
    private long mLastPowerEventTime;
    private long mLastScreenOffTime;
    private Led mLed;
    private final Object mLock;
    private int mLowBatteryCloseWarningLevel;
    private int mLowBatteryWarningLevel;
    private SettingsObserver mLowPowerSettingsObservers;
    private int mMaxChargeCurrent;
    private int mMaxChargeTemperature;
    private int mMinChargeTemperature;
    private long mMobileDataDelta;
    private String mModemResumeInfo;
    private boolean mMusicScene;
    private NetworkStatsFactory mNetworkStatsFactory;
    private NetworkStats mNetworkStatsScreenOff;
    private NetworkStats mNetworkStatsScreenOn;
    private NetworkStats mNetworkStatsSubtract;
    private OppoSysStateManagerInternal mOppoSysStateManagerInternal;
    private IOppoUsageService mOppoUsageService;
    private int mOtgOnline;
    private PackageManager mPackageManager;
    private int mPlugType;
    private StringBuilder mPowerEventRecordSB;
    private double mPowerLostByTelephone;
    private double mPowerLostByWifi;
    private PowerManager mPowerManager;
    private boolean mPowerMonitorDebugMode;
    private boolean mPowerMonitorEnabled;
    private boolean mPowerMonitorTipEnabled;
    private PowerProfile mPowerProfile;
    private PowerStatusReport mPowerStatusReport;
    private String mProcessUsingAudio;
    private String mProcessUsingAudioConfirm;
    private long mScreenOffTime;
    private boolean mSentLowBatteryBroadcast;
    private int mShutdownBatteryTemperature;
    private String mSimCard1Name;
    private String mSimCard2Name;
    private int mSimCardSize;
    private double mSuspengRatio;
    private String mTelephonePowerState;
    private TelephonyManager mTelephonyManager;
    private int mTempLastPlugType;
    private int mTopApResumeCount;
    private String mTopApResumeReason;
    private String mTopApResumeReasonName;
    private ArrayMap<String, Long> mTopBlockedApp;
    private List<Entry<String, Long>> mTopBlockedAppList;
    private boolean mUpdatesStopped;
    private long mUptimeMillisDelta;
    private long mUptimeMillisScreenOff;
    private long mUptimeMillisScreenOn;
    private int mVirtualKeyInterruptsDelta;
    private int mVirtualKeyInterruptsScreenOff;
    private int mVirtualKeyInterruptsScreenOn;
    private String mWCNResumeInfo;
    private long mWifiDataDelta;
    private WifiManager mWifiManager;
    private String mWifiName;
    private String mWifiPowerEventContent;
    private String mWifiPowerEventList;
    private int mWifiState;
    private final OppoBatteryService obs;

    private final class BatteryListener extends Stub {
        /* synthetic */ BatteryListener(BatteryService this$0, BatteryListener batteryListener) {
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
        /* synthetic */ BinderService(BatteryService this$0, BinderService binderService) {
            this();
        }

        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (BatteryService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump Battery service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else if (!BatteryService.this.dynamicallyConfigBatteryServiceLogTag(fd, pw, args)) {
                if (BatteryService.this.obs != null) {
                    boolean oUpdate = BatteryService.this.obs.dumpAddition(fd, pw, args);
                }
                BatteryService.this.dumpInternal(fd, pw, args);
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, resultReceiver);
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
                Led.this.updateLightsLocked();
            }
        };
        private final Light mNotificationLightStateDetector;
        private boolean mScreenOn;

        public Led(Context context, LightsManager lights) {
            this.mBatteryLight = lights.getLight(3);
            this.mBatteryLowARGB = context.getResources().getInteger(17694810);
            this.mBatteryMediumARGB = context.getResources().getInteger(17694811);
            this.mBatteryFullARGB = context.getResources().getInteger(17694812);
            this.mBatteryLedOn = context.getResources().getInteger(17694813);
            this.mBatteryLedOff = context.getResources().getInteger(17694814);
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
                if (BatteryService.DEBUG) {
                    Slog.d(BatteryService.TAG_LED, "level = " + level + "; status = " + status);
                }
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

        private void getIpoLedStatus() {
            if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("sys.ipo.ledon"))) {
                BatteryService.this.ipo_led_on = true;
            } else if ("0".equals(SystemProperties.get("sys.ipo.ledon"))) {
                BatteryService.this.ipo_led_off = true;
            }
            if (BatteryService.DEBUG) {
                Slog.d(BatteryService.TAG, ">>>>>>>getIpoLedStatus ipo_led_on = " + BatteryService.this.ipo_led_on + ",  ipo_led_off = " + BatteryService.this.ipo_led_off + "<<<<<<<");
            }
        }

        private void updateLedStatus() {
            if ((BatteryService.this.ipo_led_off && BatteryService.this.mIPOBoot) || (BatteryService.this.LowLevelFlag && BatteryService.this.mIPOBoot)) {
                this.mBatteryLight.turnOff();
                BatteryService.this.mIPOBoot = false;
                BatteryService.this.ipo_led_off = false;
                BatteryService.this.ipo_led_on = false;
                if (BatteryService.DEBUG) {
                    Slog.d(BatteryService.TAG, ">>>>>>>updateLedStatus  LowLevelFlag = " + BatteryService.this.LowLevelFlag + "<<<<<<<");
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
        /* synthetic */ LocalService(BatteryService this$0, LocalService localService) {
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
            int -get60;
            synchronized (BatteryService.this.mLock) {
                -get60 = BatteryService.this.mPlugType;
            }
            return -get60;
        }

        public int getBatteryLevel() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mBatteryProps.batteryLevel;
            }
            return i;
        }

        public boolean getBatteryLevelLow() {
            boolean -get21;
            synchronized (BatteryService.this.mLock) {
                -get21 = BatteryService.this.mBatteryLevelLow;
            }
            return -get21;
        }

        public int getInvalidCharger() {
            int -get41;
            synchronized (BatteryService.this.mLock) {
                -get41 = BatteryService.this.mInvalidCharger;
            }
            return -get41;
        }

        public int getBatteryTemperature() {
            int i;
            synchronized (BatteryService.this.mLock) {
                i = BatteryService.this.mBatteryProps.batteryTemperature;
            }
            return i;
        }
    }

    class PowerStatusReport {
        public static final String AVERAGECURRENT = "AverageCurrent";
        public static final String BLUETOOTHCLOSED = "BluetoothClosed";
        public static final String BLUETOOTHCONNECTED = "BluetoothConnected";
        public static final String BLUETOOTHOPENED = "BluetoothOpened";
        public static final String BLUETOOTHSTATUS = "BlueToothStatus";
        public static final String DOUBLECARD = "DoubleCard";
        public static final String FRAMEWORKSBLOCKEDTIME = "FrameworksBlockedTime";
        public static final String ISSUETYPE = "IssueType";
        public static final String MOBILEDATA = "MobileData";
        public static final String MODEMRESUME = "ModemResume";
        public static final String NOSIMCARD = "NoSimCard";
        public static final String OK = "OK";
        public static final String POWERKEYRESUME = "PowerKeyResume";
        public static final String RTCRESUME = "RtcResume";
        public static final String SCREENOFFTIME = "ScreenOffTime";
        public static final String SIMCARDSTATUS = "SIMCardStatus";
        public static final String SINGLECARD = "SingleCard";
        public static final String SUSPENDRATIO = "SuspendRatio";
        private static final String TAG = "PowerStatusReport";
        public static final String WIFICLOSED = "WifiClosed";
        public static final String WIFICONNECTED = "WifiConnected";
        public static final String WIFIDATA = "WifiData";
        public static final String WIFIOPENED = "WifiOpened";
        public static final String WIFIRESUME = "WifiResume";
        public static final String WIFISTATUS = "WifiStatus";
        private static final String mEventId = "PowerMonitor";
        private static final String mLogTag = "20120";
        private final Context mContext;
        private Map mMap = new ArrayMap();

        public PowerStatusReport(Context context) {
            this.mContext = context;
        }

        public void putData(String key, String value) {
            if (key == null || value == null) {
                Slog.i(TAG, "Either key or value is null!");
            } else {
                this.mMap.put(key, value);
            }
        }

        public void report() {
            OppoStatistics.onCommon(this.mContext, "20120", mEventId, this.mMap, false);
            this.mMap.clear();
        }

        public void clear() {
            this.mMap.clear();
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
                    BatteryService.this.mLed.updateLightsLocked();
                }
            }
            if (uri == null || "oppo_breath_led_charge".equals(uri)) {
                boolean chargeEnabled = System.getInt(resolver, "oppo_breath_led_charge", 0) != 0;
                if (BatteryService.this.mLed.mChargingHint != chargeEnabled) {
                    BatteryService.this.mLed.mChargingHint = chargeEnabled;
                    BatteryService.this.mLed.updateLightsLocked();
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.BatteryService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.BatteryService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.BatteryService.<clinit>():void");
    }

    private double getBatteryRealtimeCapacity(int level) {
        return (this.mBatteryCapacityFromPowerProfile * ((double) level)) / 100.0d;
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x01cb A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e8  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x011f A:{SYNTHETIC, Splitter: B:51:0x011f} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00df A:{SYNTHETIC, Splitter: B:38:0x00df} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e8  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01cb A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getTopApResumeReason() {
        IOException e;
        Throwable th;
        String top_reason = IElsaManager.EMPTY_PACKAGE;
        int top_resume_count = 0;
        File file = new File(AP_WAKEUP_SOURCE_PATH);
        BufferedReader bufferedReader = null;
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String tempString = reader.readLine();
                        if (tempString == null) {
                            break;
                        }
                        if (DEBUG_PANIC) {
                            Slog.d(TAG_PM, "getTopApResumeReason readLine :" + tempString);
                        }
                        if (!tempString.isEmpty() && tempString.contains(":")) {
                            String[] reason_set = tempString.split(":");
                            if (reason_set.length == 2) {
                                if ("wcnss_wlan".equals(reason_set[0])) {
                                    this.mPowerStatusReport.putData(PowerStatusReport.WIFIRESUME, reason_set[1].toString());
                                } else if ("modem".equals(reason_set[0])) {
                                    this.mPowerStatusReport.putData(PowerStatusReport.MODEMRESUME, reason_set[1].toString());
                                } else if ("qpnp_rtc_alarm".equals(reason_set[0])) {
                                    this.mPowerStatusReport.putData(PowerStatusReport.RTCRESUME, reason_set[1].toString());
                                } else if ("power_key".equals(reason_set[0])) {
                                    this.mPowerStatusReport.putData(PowerStatusReport.POWERKEYRESUME, reason_set[1].toString());
                                }
                                try {
                                    int count = Integer.valueOf(reason_set[1].trim()).intValue();
                                    if (count > top_resume_count) {
                                        top_resume_count = count;
                                        top_reason = reason_set[0].trim();
                                        if (top_reason == null) {
                                            top_reason = "UNKNOWN";
                                        }
                                    }
                                } catch (NumberFormatException e2) {
                                    Slog.d(TAG_PM, "readFileByLines NumberFormatException:" + e2.getMessage());
                                }
                            }
                        }
                    } catch (IOException e3) {
                        e = e3;
                        bufferedReader = reader;
                        try {
                            Slog.d(TAG_PM, "readFileByLines io exception:" + e.getMessage());
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e1) {
                                    Slog.d(TAG_PM, "readFileByLines io close exception :" + e1.getMessage());
                                }
                            }
                            if (top_reason.length() <= 0) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (bufferedReader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader = reader;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e12) {
                                Slog.d(TAG_PM, "readFileByLines io close exception :" + e12.getMessage());
                            }
                        }
                        throw th;
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.d(TAG_PM, "readFileByLines io close exception :" + e122.getMessage());
                    }
                }
            } catch (IOException e4) {
                e = e4;
                Slog.d(TAG_PM, "readFileByLines io exception:" + e.getMessage());
                if (bufferedReader != null) {
                }
                if (top_reason.length() <= 0) {
                }
            }
            if (top_reason.length() <= 0) {
                return null;
            }
            this.mTopApResumeReasonName = top_reason;
            this.mTopApResumeCount = top_resume_count;
            return top_reason + top_resume_count;
        }
        Slog.d(TAG_PM, "/sys/kernel/wakeup_reasons/ap_resume_reason_stastics not exists");
        return null;
    }

    private String getWcnResumeInfo() {
        IOException e;
        Throwable th;
        String wcn_info;
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
                            if (DEBUG_PANIC) {
                                Slog.d(TAG_PM, "getWcnResumeInfo readLine :" + tempString);
                            }
                            if (tempString.length() != 0 && tempString.contains(":")) {
                                String[] reason_set = tempString.split(":");
                                if (reason_set.length == 2) {
                                    try {
                                        if (Integer.valueOf(reason_set[1].trim()).intValue() > 0) {
                                            wcn_resume_info.append(tempString.trim());
                                            wcn_resume_info.append(";");
                                        }
                                    } catch (NumberFormatException e2) {
                                        Slog.d(TAG_PM, "readFileByLines NumberFormatException:" + e2.getMessage());
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
                                    Slog.d(TAG_PM, "readFileByLines io close exception :" + e1.getMessage());
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
                    Slog.d(TAG_PM, "readFileByLines io exception:" + e.getMessage());
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e12) {
                            Slog.d(TAG_PM, "readFileByLines io close exception :" + e12.getMessage());
                        }
                    }
                    wcn_info = wcn_resume_info.toString();
                    if (wcn_info != null) {
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e122) {
                            Slog.d(TAG_PM, "readFileByLines io close exception :" + e122.getMessage());
                        }
                    }
                    throw th;
                }
            }
            wcn_info = wcn_resume_info.toString();
            if (wcn_info != null || wcn_info.length() <= 0) {
                return null;
            }
            return wcn_info;
        }
        Slog.d(TAG_PM, "/sys/kernel/wakeup_reasons/wcn_resume_reason_stastics not exists");
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x009d A:{SYNTHETIC, Splitter: B:26:0x009d} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00c4 A:{SYNTHETIC, Splitter: B:32:0x00c4} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getModemResumeInfo() {
        IOException e;
        Throwable th;
        File file = new File(MODEM_INFO_PATH);
        BufferedReader reader = null;
        String modem_info = null;
        if (file.exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                try {
                    String tempString = reader2.readLine();
                    if (DEBUG_PANIC) {
                        Slog.d(TAG_PM, "getModemResumeInfo readLine :" + tempString);
                    }
                    if (tempString == null || tempString.length() <= 0) {
                        modem_info = null;
                    } else {
                        modem_info = tempString.trim();
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e1) {
                            Slog.d(TAG_PM, "readFileByLines io close exception :" + e1.getMessage());
                        }
                    }
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Slog.d(TAG_PM, "readFileByLines io exception:" + e.getMessage());
                        if (reader != null) {
                        }
                        return modem_info;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e12) {
                                Slog.d(TAG_PM, "readFileByLines io close exception :" + e12.getMessage());
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
                Slog.d(TAG_PM, "readFileByLines io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.d(TAG_PM, "readFileByLines io close exception :" + e122.getMessage());
                    }
                }
                return modem_info;
            }
            return modem_info;
        }
        Slog.d(TAG_PM, "/sys/kernel/wakeup_reasons/modem_resume_reason_stastics not exists");
        return null;
    }

    private String TimeStamp2Date(long timestamp, String formats) {
        return new SimpleDateFormat(formats).format(new Date(timestamp));
    }

    private int getVirtualKeyInterrupts() {
        IOException e;
        Throwable th;
        int virtual_key_interrupts = 0;
        File file = new File(INTERRUPTS_PATH);
        BufferedReader reader = null;
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
                                    Slog.d(TAG_PM, "getVirtualKeyInterrupts io close exception :" + e1.getMessage());
                                }
                            }
                        } else if (tempString.length() != 0 && tempString.contains(VIRTUAL_KEY_INTERRUPT_NAME)) {
                            Slog.i(TAG_PM, "getVirtualKeyInterrupts : " + tempString);
                            String[] virtual_key_status = tempString.split("\\s+");
                            if (virtual_key_status.length > 0 && VIRTUAL_KEY_INTERRUPT_NAME.equals(virtual_key_status[virtual_key_status.length - 1])) {
                                try {
                                    virtual_key_interrupts = Integer.valueOf(virtual_key_status[1].trim()).intValue();
                                } catch (NumberFormatException e2) {
                                    Slog.d(TAG_PM, "getVirtualKeyInterrupts NumberFormatException:" + e2.getMessage());
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
                    Slog.d(TAG_PM, "getVirtualKeyInterrupts io exception:" + e.getMessage());
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e12) {
                            Slog.d(TAG_PM, "getVirtualKeyInterrupts io close exception :" + e12.getMessage());
                        }
                    }
                    return virtual_key_interrupts;
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e122) {
                            Slog.d(TAG_PM, "getVirtualKeyInterrupts io close exception :" + e122.getMessage());
                        }
                    }
                    throw th;
                }
            }
            return virtual_key_interrupts;
        }
        Slog.d(TAG_PM, "/proc/interrupts not exists");
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
        return DECIMAL_FORMAT.format((double) total_data) + surffix;
    }

    private ArrayList<RunningAppProcessInfo> getRunningAppList(Context context) {
        return (ArrayList) ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
    }

    private String getActiveAudioPids(Context context) {
        String pids = ((AudioManager) context.getSystemService("audio")).getParameters("get_pid");
        if (DEBUG_PANIC) {
            Slog.i(TAG_PM, "getActiveAudioPids : " + pids);
        }
        if (pids == null || pids.length() == 0) {
            return null;
        }
        return pids;
    }

    public BatteryService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mLastBatteryProps = new BatteryProperties();
        this.mLastPlugType = -1;
        this.mTempLastPlugType = -1;
        this.mMaxChargeCurrent = Integer.MIN_VALUE;
        this.mMinChargeTemperature = Integer.MAX_VALUE;
        this.mMaxChargeTemperature = Integer.MIN_VALUE;
        this.mOppoUsageService = null;
        this.isServiceBinded = false;
        this.mPowerManager = null;
        this.mFrameworksBlockedTime = 0;
        this.mFrameworksBlockedRatio = 0.0d;
        this.mSentLowBatteryBroadcast = false;
        this.mIPOShutdown = false;
        this.mIPOed = false;
        this.mIPOBoot = false;
        this.ipo_led_on = false;
        this.ipo_led_off = false;
        this.LowLevelFlag = false;
        this.mLastPowerEventTime = 0;
        this.mBatteryCapacityDeviation = 0.01d;
        this.SAMPLE_TIME = 18000000;
        this.SAMPLE_TIME_WITHOUT_BATTERYCOUNTER = 21600000;
        this.mCurrentSampleTime = this.SAMPLE_TIME;
        this.mProcessUsingAudio = null;
        this.mProcessUsingAudioConfirm = null;
        this.mMusicScene = false;
        this.mKeepAudioProcess = IElsaManager.EMPTY_PACKAGE;
        this.mPowerLostByTelephone = 0.0d;
        this.mTelephonePowerState = null;
        this.WIFI_DATA_PER_HOUR_THRESHOLD = 15728640;
        this.MOBILE_DATA_PER_HOUR_THRESHOLD = 2097152;
        this.mDownloadScene = false;
        this.mTopApResumeReason = null;
        this.mTopApResumeReasonName = null;
        this.mWCNResumeInfo = null;
        this.mModemResumeInfo = null;
        this.mPackageManager = null;
        this.mWifiManager = null;
        this.mConnectivityManager = null;
        this.mBluetoothAdapter = null;
        this.mWifiPowerEventContent = null;
        this.mWifiPowerEventList = null;
        this.mPowerLostByWifi = 0.0d;
        this.mPowerEventRecordSB = null;
        this.mNetworkStatsScreenOff = null;
        this.mNetworkStatsScreenOn = null;
        this.mNetworkStatsSubtract = null;
        this.mNetworkStatsFactory = null;
        this.mPowerMonitorEnabled = false;
        this.mPowerMonitorTipEnabled = false;
        this.mPowerMonitorDebugMode = false;
        this.mIsOverTemperature = false;
        this.mSimCardSize = 0;
        this.mSimCard1Name = IElsaManager.EMPTY_PACKAGE;
        this.mSimCard2Name = IElsaManager.EMPTY_PACKAGE;
        this.mCurrentNetwork = IElsaManager.EMPTY_PACKAGE;
        this.mWifiState = 0;
        this.mWifiName = IElsaManager.EMPTY_PACKAGE;
        this.mDataNetworkOperatorName = IElsaManager.EMPTY_PACKAGE;
        this.mDataNetworkState = -1;
        this.mDataNetworkType = 0;
        this.mIsSellModeVersion = false;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Slog.i(BatteryService.TAG, "action received : " + action);
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    if (BatteryService.this.mLed != null) {
                        BatteryService.this.mLed.handleScreenOn();
                    }
                    if (BatteryService.this.mPowerMonitorEnabled) {
                        int -get6;
                        if (BatteryService.this.mHandler.hasCallbacks(BatteryService.this.mDetectAudioProcessRunnable)) {
                            BatteryService.this.mHandler.removeCallbacks(BatteryService.this.mDetectAudioProcessRunnable);
                        }
                        BatteryService.this.mElapsedRealtimeScreenOn = SystemClock.elapsedRealtime();
                        BatteryService.this.mUptimeMillisScreenOn = SystemClock.uptimeMillis();
                        BatteryService.this.mBatteryCapacityScreenOn = BatteryService.this.obs.mBatteryRealtimeCapacity == -1 ? BatteryService.this.getBatteryRealtimeCapacity(BatteryService.this.mBatteryProps.batteryLevel) : (double) BatteryService.this.obs.mBatteryRealtimeCapacity;
                        BatteryService batteryService = BatteryService.this;
                        if (BatteryService.this.obs.mBatteryRealtimeCapacity == -1) {
                            -get6 = BatteryService.this.SAMPLE_TIME_WITHOUT_BATTERYCOUNTER;
                        } else {
                            -get6 = BatteryService.this.SAMPLE_TIME;
                        }
                        batteryService.mCurrentSampleTime = -get6;
                        BatteryService.this.mBatteryJumpSocScreenOn = BatteryService.this.obs.mBatteryJumpSocTotal;
                        BatteryService.this.mVirtualKeyInterruptsScreenOn = BatteryService.this.getVirtualKeyInterrupts();
                        if (BatteryService.DEBUG) {
                            Slog.i(BatteryService.TAG_PM, "mElapsedRealtimeScreenOn = " + BatteryService.this.mElapsedRealtimeScreenOn + ", mUptimeMillisScreenOn = " + BatteryService.this.mUptimeMillisScreenOn + ", mBatteryCapacityScreenOn = " + BatteryService.this.mBatteryCapacityScreenOn + ", mCurrentSampleTime = " + BatteryService.this.mCurrentSampleTime + ", mBatteryJumpSocScreenOn = " + BatteryService.this.mBatteryJumpSocScreenOn + ", mVirtualKeyInterruptsScreenOn = " + BatteryService.this.mVirtualKeyInterruptsScreenOn + ", mMusicScene = " + BatteryService.this.mMusicScene + ", mKeepAudioProcess = " + BatteryService.this.mKeepAudioProcess);
                        }
                        if ((BatteryService.this.mPowerMonitorDebugMode && BatteryService.this.mElapsedRealtimeScreenOn - BatteryService.this.mElapsedRealtimeScreenOff > 0) || (BatteryService.this.mScreenOffTime > 0 && BatteryService.this.mElapsedRealtimeScreenOn - BatteryService.this.mElapsedRealtimeScreenOff > ((long) BatteryService.this.mCurrentSampleTime) && BatteryService.this.mBatteryCapacityScreenOff - BatteryService.this.mBatteryCapacityScreenOn > 0.0d)) {
                            BatteryService.this.mPowerStatusReport.clear();
                            BatteryService.this.mLastScreenOffTime = BatteryService.this.mScreenOffTime;
                            BatteryService.this.mElapsedRealtimeDelta = BatteryService.this.mElapsedRealtimeScreenOn - BatteryService.this.mElapsedRealtimeScreenOff;
                            BatteryService.this.mUptimeMillisDelta = BatteryService.this.mUptimeMillisScreenOn - BatteryService.this.mUptimeMillisScreenOff;
                            BatteryService.this.mBatteryCapacityDelta = BatteryService.this.mBatteryCapacityScreenOff - BatteryService.this.mBatteryCapacityScreenOn;
                            BatteryService.this.mBatteryJumpSocDelta = BatteryService.this.mBatteryJumpSocScreenOn - BatteryService.this.mBatteryJumpSocScreenOff;
                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.SCREENOFFTIME, new DecimalFormat("#").format(((double) BatteryService.this.mElapsedRealtimeDelta) / 1000.0d));
                            try {
                                BatteryService.this.mNetworkStatsScreenOn = BatteryService.this.mNetworkStatsFactory.readNetworkStatsDetail();
                            } catch (IOException e) {
                                BatteryService.this.mNetworkStatsScreenOn = null;
                                Slog.e(BatteryService.TAG_PM, "readNetworkStatsDetail IOException --- ");
                            }
                            if (!(BatteryService.this.mNetworkStatsScreenOn == null || BatteryService.this.mNetworkStatsScreenOff == null)) {
                                BatteryService.this.mNetworkStatsSubtract = BatteryService.this.mNetworkStatsScreenOn.subtract(BatteryService.this.mNetworkStatsScreenOff);
                            }
                            BatteryService.this.mVirtualKeyInterruptsDelta = BatteryService.this.mVirtualKeyInterruptsScreenOn - BatteryService.this.mVirtualKeyInterruptsScreenOff;
                            BatteryService.this.mSuspengRatio = 100.0d - ((((double) BatteryService.this.mUptimeMillisDelta) * 100.0d) / ((double) BatteryService.this.mElapsedRealtimeDelta));
                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.SUSPENDRATIO, new DecimalFormat("#0.00").format(BatteryService.this.mSuspengRatio));
                            BatteryService.this.mIsMusicScene = BatteryService.this.mMusicScene;
                            BatteryService.this.mPowerEventRecordSB = new StringBuilder();
                            BatteryService.this.mPowerEventRecordSB.append("OffTime:");
                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.TimeStamp2Date(BatteryService.this.mLastScreenOffTime, BatteryService.DATE_FORMAT));
                            BatteryService.this.mPowerEventRecordSB.append(",Duration:");
                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.DECIMAL_FORMAT.format((double) (((float) (BatteryService.this.mElapsedRealtimeDelta / 1000)) / 3600.0f)));
                            BatteryService.this.mPowerEventRecordSB.append("h");
                            BatteryService.this.mPowerEventRecordSB.append(",DeltaBC:");
                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.mBatteryCapacityDelta);
                            BatteryService.this.mPowerEventRecordSB.append("mAh");
                            BatteryService.this.mPowerEventRecordSB.append(",Suspend:");
                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.DECIMAL_FORMAT.format(BatteryService.this.mSuspengRatio)).append("%");
                            if (BatteryService.DEBUG_PANIC) {
                                Slog.d(BatteryService.TAG_PM, BatteryService.this.mPowerEventRecordSB.toString());
                            }
                            if (SystemProperties.getBoolean("debug.mdlogger.Running", false) || SystemService.isRunning("diag_mdlog_start")) {
                                BatteryService.this.mPowerEventRecordSB.append("[MDLOG_ON]");
                                if (BatteryService.DEBUG_PANIC) {
                                    Slog.d(BatteryService.TAG_PM, "md_log on, quit monitor");
                                }
                            } else if (BatteryService.this.mIsOverTemperature) {
                                if (BatteryService.DEBUG_PANIC) {
                                    Slog.d(BatteryService.TAG_PM, "Over temperature, quit monitor");
                                }
                            } else if (BatteryService.this.mPowerMonitorDebugMode || BatteryService.this.mLastPowerEventTime == 0 || System.currentTimeMillis() - BatteryService.this.mLastPowerEventTime > 86400000) {
                                BatteryService.this.mLastPowerEventTime = System.currentTimeMillis();
                                if (BatteryService.this.mIsMusicScene) {
                                    BatteryService.this.mPowerEventRecordSB.append("[MusicScene]");
                                    if (BatteryService.DEBUG_PANIC) {
                                        Slog.d(BatteryService.TAG_PM, "[MusicScene]");
                                    }
                                }
                                if (BatteryService.this.mBatteryJumpSocDelta != 0) {
                                    BatteryService.this.mPowerEventRecordSB.append(",BATTERY_JUMP:");
                                    BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.mBatteryJumpSocDelta);
                                    if (BatteryService.DEBUG_PANIC) {
                                        Slog.d(BatteryService.TAG_PM, "BATTERY_JUMP : " + BatteryService.this.mBatteryJumpSocDelta);
                                    }
                                }
                                BatteryService.this.mTopApResumeReasonName = null;
                                BatteryService.this.mTopApResumeCount = 0;
                                BatteryService.this.mTopApResumeReason = BatteryService.this.getTopApResumeReason();
                                if (BatteryService.this.mTopApResumeReason != null) {
                                    BatteryService.this.mPowerEventRecordSB.append(",AP_TOP:");
                                    BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.mTopApResumeReason);
                                    if (BatteryService.DEBUG_PANIC) {
                                        Slog.d(BatteryService.TAG_PM, "AP_TOP : " + BatteryService.this.mTopApResumeReason);
                                    }
                                }
                                if (BatteryService.this.mVirtualKeyInterruptsDelta > 10) {
                                    BatteryService.this.mPowerEventRecordSB.append(",VIRTUAL_KEY_EXCE:");
                                    BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.mVirtualKeyInterruptsDelta);
                                    OppoManager.writeLogToPartition(OppoManager.TYPE_ANDROID_PM_EVENT_63, BatteryService.this.mPowerEventRecordSB.toString(), "ANDROID", "base_subsystem", BatteryService.this.mContext.getResources().getString(17040982));
                                    if (BatteryService.DEBUG_PANIC) {
                                        Slog.d(BatteryService.TAG_PM, "VIRTUAL_KEY_EXCE : " + BatteryService.this.mVirtualKeyInterruptsDelta);
                                    }
                                }
                                BatteryService.this.mWCNResumeInfo = BatteryService.this.getWcnResumeInfo();
                                if (BatteryService.this.mWCNResumeInfo != null) {
                                    BatteryService.this.mPowerEventRecordSB.append(",WCN_INFO:");
                                    BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.mWCNResumeInfo);
                                    if (BatteryService.DEBUG_PANIC) {
                                        Slog.d(BatteryService.TAG_PM, "WCN_INFO : " + BatteryService.this.mWCNResumeInfo);
                                    }
                                }
                                if (BatteryService.this.mWifiManager == null) {
                                    BatteryService.this.mWifiManager = (WifiManager) BatteryService.this.mContext.getSystemService("wifi");
                                }
                                if (BatteryService.this.mConnectivityManager == null) {
                                    BatteryService.this.mConnectivityManager = (ConnectivityManager) BatteryService.this.mContext.getSystemService("connectivity");
                                }
                                if (BatteryService.this.mTelephonyManager == null) {
                                    BatteryService.this.mTelephonyManager = (TelephonyManager) BatteryService.this.mContext.getSystemService("phone");
                                }
                                BatteryService.this.mWifiPowerEventContent = BatteryService.this.mWifiManager.getWifiPowerEventCode();
                                BatteryService.this.mModemResumeInfo = BatteryService.this.getModemResumeInfo();
                                if (BatteryService.this.mModemResumeInfo != null) {
                                    BatteryService.this.mPowerEventRecordSB.append(",MODEM_INFO:");
                                    BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.mModemResumeInfo);
                                    if (BatteryService.DEBUG_PANIC) {
                                        Slog.d(BatteryService.TAG_PM, "MODEM_INFO : " + BatteryService.this.mModemResumeInfo);
                                    }
                                }
                                BatteryService.this.mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        int i;
                                        int m;
                                        int retry_count = 0;
                                        while (retry_count < 5) {
                                            if (BatteryService.this.mConnectivityManager != null) {
                                                boolean telephony_ready = BatteryService.this.mConnectivityManager.isAlreadyUpdated();
                                                retry_count++;
                                                if (BatteryService.DEBUG_PANIC) {
                                                    Slog.i(BatteryService.TAG_PM, "isAlreadyUpdated : " + telephony_ready);
                                                }
                                                if (telephony_ready) {
                                                    break;
                                                }
                                                SystemClock.sleep(200);
                                            } else {
                                                Slog.e(BatteryService.TAG_PM, "mConnectivityManager is null");
                                                break;
                                            }
                                        }
                                        if (BatteryService.this.mConnectivityManager == null) {
                                            Slog.e(BatteryService.TAG_PM, "mConnectivityManager is null");
                                        } else {
                                            BatteryService.this.mPowerLostByTelephone = BatteryService.this.mConnectivityManager.getTelephonyPowerLost();
                                        }
                                        if (BatteryService.DEBUG_PANIC) {
                                            Slog.i(BatteryService.TAG_PM, "mPowerLostByTelephone : " + BatteryService.DECIMAL_FORMAT.format(BatteryService.this.mPowerLostByTelephone) + "mAh");
                                        }
                                        if (BatteryService.this.mPowerLostByTelephone > 0.0d) {
                                            BatteryService.this.mPowerEventRecordSB.append(",TelePowerLost:");
                                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.DECIMAL_FORMAT.format(BatteryService.this.mPowerLostByTelephone));
                                            BatteryService.this.mPowerEventRecordSB.append("mAh");
                                        }
                                        if (BatteryService.this.mWifiPowerEventContent == null || BatteryService.this.mWifiPowerEventContent.length() <= 0) {
                                            BatteryService.this.mWifiPowerEventContent = null;
                                            BatteryService.this.mPowerLostByWifi = 0.0d;
                                        } else {
                                            BatteryService.this.mWifiPowerEventList = BatteryService.this.mWifiPowerEventContent.substring(BatteryService.this.mWifiPowerEventContent.indexOf(61) + 1, BatteryService.this.mWifiPowerEventContent.indexOf(10));
                                            if (BatteryService.this.mWifiPowerEventList != null && BatteryService.this.mWifiPowerEventList.length() > 0) {
                                                BatteryService.this.mPowerEventRecordSB.append(",WIFI_EVENT:");
                                                BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.mWifiPowerEventList);
                                                if (BatteryService.DEBUG_PANIC) {
                                                    Slog.i(BatteryService.TAG_PM, "WIFI_EVENT : " + BatteryService.this.mWifiPowerEventList);
                                                }
                                            }
                                            String wifi_event_temp = BatteryService.this.mWifiPowerEventContent.substring(BatteryService.this.mWifiPowerEventContent.indexOf(61, BatteryService.this.mWifiPowerEventContent.indexOf(10)) + 1, BatteryService.this.mWifiPowerEventContent.length());
                                            if (BatteryService.DEBUG_PANIC) {
                                                Slog.i(BatteryService.TAG_PM, "mPowerLostByWifi : " + wifi_event_temp);
                                            }
                                            if (wifi_event_temp != null && wifi_event_temp.length() > 0) {
                                                try {
                                                    BatteryService.this.mPowerLostByWifi = Double.parseDouble(wifi_event_temp.trim());
                                                } catch (NumberFormatException e) {
                                                    Slog.d(BatteryService.TAG_PM, "mPowerLostByWifi NumberFormatException : " + e.getMessage());
                                                    BatteryService.this.mPowerLostByWifi = 0.0d;
                                                }
                                            }
                                            if (BatteryService.this.mPowerLostByWifi > 0.0d) {
                                                BatteryService.this.mPowerEventRecordSB.append(",mPowerLostByWifi:");
                                                BatteryService.this.mPowerEventRecordSB.append(BatteryService.DECIMAL_FORMAT.format(BatteryService.this.mPowerLostByWifi));
                                                BatteryService.this.mPowerEventRecordSB.append("mAh");
                                            }
                                        }
                                        double active_average_current = BatteryService.this.mPowerProfile.getBasicCurrent();
                                        int sim_count = SubscriptionManager.from(BatteryService.this.mContext).getActiveSubscriptionInfoCount();
                                        if (sim_count == 0) {
                                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.SIMCARDSTATUS, PowerStatusReport.NOSIMCARD);
                                        } else if (sim_count == 1) {
                                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.SIMCARDSTATUS, PowerStatusReport.SINGLECARD);
                                        } else if (sim_count == 2) {
                                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.SIMCARDSTATUS, PowerStatusReport.DOUBLECARD);
                                        }
                                        if (sim_count == 1) {
                                            active_average_current += BatteryService.this.mPowerProfile.getSingleSimDelta();
                                        } else if (sim_count == 2) {
                                            active_average_current += BatteryService.this.mPowerProfile.getMultiSimDelta();
                                        }
                                        boolean wifi_state = false;
                                        boolean bt_state = false;
                                        if (BatteryService.this.mBluetoothAdapter == null) {
                                            BatteryService.this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                        }
                                        if (BatteryService.this.mWifiManager != null && BatteryService.this.mWifiManager.getWifiState() == 3) {
                                            wifi_state = true;
                                        }
                                        if (BatteryService.this.mBluetoothAdapter != null && BatteryService.this.mBluetoothAdapter.getState() == 12) {
                                            bt_state = true;
                                        }
                                        if (wifi_state && bt_state) {
                                            active_average_current += BatteryService.this.mPowerProfile.getWifiBtDelta();
                                        } else if (wifi_state && !bt_state) {
                                            active_average_current += BatteryService.this.mPowerProfile.getWifiDelta();
                                        } else if (!wifi_state && bt_state) {
                                            active_average_current += BatteryService.this.mPowerProfile.getBtDelta();
                                        }
                                        SubscriptionManager subscriptionManager = SubscriptionManager.from(BatteryService.this.mContext);
                                        if (subscriptionManager != null) {
                                            List<SubscriptionInfo> list = subscriptionManager.getActiveSubscriptionInfoList();
                                            if (list != null) {
                                                BatteryService.this.mSimCardSize = list.size();
                                                if (BatteryService.this.mSimCardSize == 1) {
                                                    BatteryService.this.mSimCard1Name = ((SubscriptionInfo) list.get(0)).getCarrierName().toString();
                                                    if (BatteryService.DEBUG_PANIC) {
                                                        Slog.d(BatteryService.TAG_PM, "There is one sim card, carrier name is " + BatteryService.this.mSimCard1Name);
                                                    }
                                                } else if (BatteryService.this.mSimCardSize == 2) {
                                                    BatteryService.this.mSimCard1Name = ((SubscriptionInfo) list.get(0)).getCarrierName().toString();
                                                    BatteryService.this.mSimCard2Name = ((SubscriptionInfo) list.get(1)).getCarrierName().toString();
                                                    if (BatteryService.DEBUG_PANIC) {
                                                        Slog.d(BatteryService.TAG_PM, "There are two sim cards.");
                                                        Slog.d(BatteryService.TAG_PM, "Sim card1 is " + BatteryService.this.mSimCard1Name);
                                                        Slog.d(BatteryService.TAG_PM, "Sim card2 is " + BatteryService.this.mSimCard2Name);
                                                    }
                                                }
                                            }
                                        }
                                        if (BatteryService.this.mConnectivityManager != null) {
                                            NetworkInfo networkInfo = BatteryService.this.mConnectivityManager.getActiveNetworkInfo();
                                            if (networkInfo != null && networkInfo.isAvailable()) {
                                                BatteryService.this.mCurrentNetwork = networkInfo.getTypeName();
                                                String extraInfo = networkInfo.getExtraInfo();
                                                String state = networkInfo.getState().toString();
                                                String detailedState = networkInfo.getDetailedState().toString();
                                                if (BatteryService.DEBUG_PANIC) {
                                                    Slog.d(BatteryService.TAG_PM, "Current active network is " + BatteryService.this.mCurrentNetwork);
                                                    Slog.d(BatteryService.TAG_PM, "Active network extrainfo is " + extraInfo);
                                                    Slog.d(BatteryService.TAG_PM, "Active network state is " + state + ", detailed state is " + detailedState);
                                                }
                                            }
                                        }
                                        if (BatteryService.this.mTelephonyManager != null) {
                                            BatteryService.this.mDataNetworkState = BatteryService.this.mTelephonyManager.getDataState();
                                            BatteryService.this.mDataNetworkOperatorName = BatteryService.this.mTelephonyManager.getNetworkOperatorName();
                                            BatteryService.this.mDataNetworkType = BatteryService.this.mTelephonyManager.getNetworkType();
                                            if (BatteryService.DEBUG_PANIC) {
                                                if (!(BatteryService.this.mDataNetworkOperatorName == null || BatteryService.this.mDataNetworkOperatorName.length() == 0)) {
                                                    Slog.d(BatteryService.TAG_PM, "Data network is " + BatteryService.this.mDataNetworkOperatorName);
                                                }
                                                Slog.d(BatteryService.TAG_PM, "Data network state is " + BatteryService.this.mDataNetworkState);
                                                Slog.d(BatteryService.TAG_PM, "Data network type is " + BatteryService.this.mDataNetworkType);
                                            }
                                        }
                                        if (BatteryService.this.mWifiManager != null) {
                                            BatteryService.this.mWifiState = BatteryService.this.mWifiManager.getWifiState();
                                            if (BatteryService.DEBUG_PANIC) {
                                                Slog.d(BatteryService.TAG_PM, "Wifi state is " + BatteryService.this.mWifiState);
                                            }
                                            if (BatteryService.this.mWifiState == 3) {
                                                WifiInfo mWifiInfo = BatteryService.this.mWifiManager.getConnectionInfo();
                                                BatteryService.this.mWifiName = mWifiInfo != null ? mWifiInfo.getBSSID() : IElsaManager.EMPTY_PACKAGE;
                                                if (BatteryService.DEBUG_PANIC) {
                                                    Slog.d(BatteryService.TAG_PM, "Wifi mac is " + BatteryService.this.mWifiName);
                                                }
                                            }
                                        }
                                        int wifiCode = -1;
                                        if (BatteryService.this.mWifiState == 1 || BatteryService.this.mWifiState == 0) {
                                            wifiCode = 0;
                                        } else if (BatteryService.this.mWifiState == 3 || BatteryService.this.mWifiState == 2) {
                                            if (BatteryService.this.mWifiName == null || BatteryService.this.mWifiName.length() == 0) {
                                                wifiCode = 1;
                                            } else {
                                                wifiCode = 2;
                                            }
                                        }
                                        if (BatteryService.DEBUG_PANIC) {
                                            Slog.d(BatteryService.TAG_PM, "Wifi code is " + wifiCode);
                                        }
                                        if (wifi_state) {
                                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.WIFISTATUS, PowerStatusReport.WIFIOPENED);
                                        } else {
                                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.WIFISTATUS, PowerStatusReport.WIFICLOSED);
                                        }
                                        if (bt_state) {
                                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.BLUETOOTHSTATUS, PowerStatusReport.BLUETOOTHOPENED);
                                        } else {
                                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.BLUETOOTHSTATUS, PowerStatusReport.BLUETOOTHCLOSED);
                                        }
                                        if (BatteryService.DEBUG_PANIC) {
                                            Slog.i(BatteryService.TAG_PM, "sim_count = " + sim_count + ", wifi_state = " + wifi_state + ", bt_state = " + bt_state + ", active_average_current = " + active_average_current);
                                        }
                                        BatteryService.this.mPowerEventRecordSB.append(",Standard:");
                                        BatteryService.this.mPowerEventRecordSB.append(BatteryService.DECIMAL_FORMAT.format(active_average_current));
                                        BatteryService.this.mPowerEventRecordSB.append("mA");
                                        long wifi_increment_max = 0;
                                        long wifi_increment_max_uid = 0;
                                        StringBuilder wifi_increment_max_process = new StringBuilder();
                                        long mobile_increment_max = 0;
                                        long mobile_increment_max_uid = 0;
                                        StringBuilder mobile_increment_max_process = new StringBuilder();
                                        BatteryService.this.mWifiDataDelta = 0;
                                        BatteryService.this.mMobileDataDelta = 0;
                                        if (BatteryService.this.mNetworkStatsSubtract != null) {
                                            boolean isUsbTethering = SystemProperties.get("sys.usb.config").contains("rndis");
                                            ArrayList<String> validMobileData = new ArrayList();
                                            ArrayList<String> validWifiData = new ArrayList();
                                            String[] ifaceNames = new File("proc/net/xt_qtaguid/iface_stat").list();
                                            if (ifaceNames != null) {
                                                int index = 0;
                                                while (index < ifaceNames.length) {
                                                    if (BatteryService.DEBUG_PANIC) {
                                                        Slog.d(BatteryService.TAG_PM, "ifaceNames[" + index + "]:" + ifaceNames[index]);
                                                    }
                                                    if (ifaceNames[index].contains("wlan")) {
                                                        validWifiData.add(ifaceNames[index]);
                                                    } else if (!(ifaceNames[index].contains("lo") || (isUsbTethering && ifaceNames[index].contains("rndis")))) {
                                                        validMobileData.add(ifaceNames[index]);
                                                    }
                                                    index++;
                                                }
                                            }
                                            NetworkStats.Entry entry = null;
                                            for (i = 0; i < BatteryService.this.mNetworkStatsSubtract.size(); i++) {
                                                entry = BatteryService.this.mNetworkStatsSubtract.getValues(i, entry);
                                                if (BatteryService.DEBUG) {
                                                    Slog.i(BatteryService.TAG_PM, entry.toString());
                                                }
                                                long entry_total = entry.rxBytes + entry.txBytes;
                                                BatteryService batteryService;
                                                if (validWifiData.contains(entry.iface)) {
                                                    if (entry_total > wifi_increment_max) {
                                                        wifi_increment_max = entry_total;
                                                        wifi_increment_max_uid = (long) entry.uid;
                                                    }
                                                    batteryService = BatteryService.this;
                                                    batteryService.mWifiDataDelta = batteryService.mWifiDataDelta + entry_total;
                                                } else {
                                                    if (validMobileData.contains(entry.iface)) {
                                                        if (entry_total > mobile_increment_max) {
                                                            mobile_increment_max = entry_total;
                                                            mobile_increment_max_uid = (long) entry.uid;
                                                        }
                                                        batteryService = BatteryService.this;
                                                        batteryService.mMobileDataDelta = batteryService.mMobileDataDelta + entry_total;
                                                    }
                                                }
                                            }
                                        }
                                        if (BatteryService.this.mWifiDataDelta > 0) {
                                            BatteryService.this.mPowerEventRecordSB.append(",WIFI_DATA:");
                                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.transferDataVersion(BatteryService.this.mWifiDataDelta));
                                            if (BatteryService.DEBUG_PANIC) {
                                                Slog.d(BatteryService.TAG_PM, "WIFI_DATA : " + BatteryService.this.transferDataVersion(BatteryService.this.mWifiDataDelta));
                                            }
                                        }
                                        if (BatteryService.this.mMobileDataDelta > 0) {
                                            BatteryService.this.mPowerEventRecordSB.append(",MOBILE_DATA:");
                                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.transferDataVersion(BatteryService.this.mMobileDataDelta));
                                            if (BatteryService.DEBUG_PANIC) {
                                                Slog.d(BatteryService.TAG_PM, "MOBILE_DATA : " + BatteryService.this.transferDataVersion(BatteryService.this.mMobileDataDelta));
                                            }
                                        }
                                        BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.WIFIDATA, new DecimalFormat("#").format(BatteryService.this.mWifiDataDelta));
                                        BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.MOBILEDATA, new DecimalFormat("#").format(BatteryService.this.mMobileDataDelta));
                                        BatteryService.this.mAverageWifiDataPerHour = ((double) BatteryService.this.mWifiDataDelta) / (((double) BatteryService.this.mElapsedRealtimeDelta) / 3600000.0d);
                                        BatteryService.this.mAverageMobileDataPerHour = ((double) BatteryService.this.mMobileDataDelta) / (((double) BatteryService.this.mElapsedRealtimeDelta) / 3600000.0d);
                                        BatteryService.this.mDownloadScene = false;
                                        if (BatteryService.this.mAverageWifiDataPerHour > ((double) BatteryService.this.WIFI_DATA_PER_HOUR_THRESHOLD) || BatteryService.this.mAverageMobileDataPerHour > ((double) BatteryService.this.MOBILE_DATA_PER_HOUR_THRESHOLD)) {
                                            BatteryService.this.mPowerEventRecordSB.append("[DownloadScene]");
                                            BatteryService.this.mDownloadScene = true;
                                            if (BatteryService.DEBUG_PANIC) {
                                                Slog.d(BatteryService.TAG_PM, "[DownloadScene]");
                                            }
                                        }
                                        if (BatteryService.this.mPackageManager == null) {
                                            BatteryService.this.mPackageManager = BatteryService.this.mContext.getPackageManager();
                                        }
                                        if (BatteryService.this.mWifiDataDelta > 0 && wifi_increment_max_uid != 0) {
                                            if (wifi_increment_max_uid == 1000) {
                                                BatteryService.this.mPowerEventRecordSB.append("{SYSTEM_UID}");
                                                wifi_increment_max_process.append("SYSTEM_UID");
                                            } else {
                                                String[] wifi_uid_app_list = BatteryService.this.mPackageManager.getPackagesForUid((int) wifi_increment_max_uid);
                                                if (wifi_uid_app_list != null) {
                                                    BatteryService.this.mPowerEventRecordSB.append("{");
                                                    BatteryService.this.mPowerEventRecordSB.append(wifi_increment_max_uid);
                                                    BatteryService.this.mPowerEventRecordSB.append(":");
                                                    BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.transferDataVersion(wifi_increment_max));
                                                    BatteryService.this.mPowerEventRecordSB.append(":");
                                                    for (m = 0; m < wifi_uid_app_list.length; m++) {
                                                        wifi_increment_max_process.append(wifi_uid_app_list[m]);
                                                        wifi_increment_max_process.append("\\");
                                                        BatteryService.this.mPowerEventRecordSB.append(wifi_uid_app_list[m]);
                                                        BatteryService.this.mPowerEventRecordSB.append("\\");
                                                        if (m == 3 && wifi_uid_app_list.length > 3) {
                                                            BatteryService.this.mPowerEventRecordSB.append("...");
                                                            break;
                                                        }
                                                    }
                                                    BatteryService.this.mPowerEventRecordSB.append("}");
                                                } else {
                                                    Slog.d(BatteryService.TAG_PM, "current uid app not found");
                                                }
                                            }
                                        }
                                        if (BatteryService.DEBUG_PANIC) {
                                            Slog.i(BatteryService.TAG_PM, "mWifiDataDelta : " + BatteryService.this.mWifiDataDelta + ", wifi_increment_max_uid=" + wifi_increment_max_uid + ", wifi_increment_max_process maybe " + wifi_increment_max_process.toString() + ", wifi_increment_max : " + wifi_increment_max);
                                        }
                                        if (BatteryService.this.mMobileDataDelta > 0 && mobile_increment_max_uid != 0) {
                                            if (mobile_increment_max_uid == 1000) {
                                                BatteryService.this.mPowerEventRecordSB.append("{SYSTEM_UID}");
                                                mobile_increment_max_process.append("SYSTEM_UID");
                                            } else {
                                                String[] mobile_uid_app_list = BatteryService.this.mPackageManager.getPackagesForUid((int) mobile_increment_max_uid);
                                                if (mobile_uid_app_list != null) {
                                                    BatteryService.this.mPowerEventRecordSB.append("{");
                                                    BatteryService.this.mPowerEventRecordSB.append(mobile_increment_max_uid);
                                                    BatteryService.this.mPowerEventRecordSB.append(":");
                                                    BatteryService.this.mPowerEventRecordSB.append(BatteryService.this.transferDataVersion(mobile_increment_max));
                                                    BatteryService.this.mPowerEventRecordSB.append(":");
                                                    for (m = 0; m < mobile_uid_app_list.length; m++) {
                                                        mobile_increment_max_process.append(mobile_uid_app_list[m]);
                                                        mobile_increment_max_process.append("\\");
                                                        BatteryService.this.mPowerEventRecordSB.append(mobile_uid_app_list[m]);
                                                        BatteryService.this.mPowerEventRecordSB.append("\\");
                                                        if (m == 3 && mobile_uid_app_list.length > 3) {
                                                            BatteryService.this.mPowerEventRecordSB.append("...");
                                                            break;
                                                        }
                                                    }
                                                    BatteryService.this.mPowerEventRecordSB.append("}");
                                                } else {
                                                    Slog.d(BatteryService.TAG_PM, "current uid app not found");
                                                }
                                            }
                                        }
                                        if (BatteryService.DEBUG_PANIC) {
                                            Slog.i(BatteryService.TAG_PM, "mMobileDataDelta : " + BatteryService.this.mMobileDataDelta + ", wifi_increment_max_uid=" + mobile_increment_max_uid + ", mobile_increment_max_process maybe " + mobile_increment_max_process.toString() + ", mobile_increment_max : " + mobile_increment_max);
                                        }
                                        if (BatteryService.this.mPowerManager == null) {
                                            BatteryService.this.mPowerManager = (PowerManager) BatteryService.this.mContext.getSystemService("power");
                                        }
                                        BatteryService.this.mFrameworksBlockedTime = BatteryService.this.mPowerManager.getFrameworksBlockedTime();
                                        BatteryService.this.mFrameworksBlockedRatio = (((double) BatteryService.this.mFrameworksBlockedTime) / ((double) BatteryService.this.mElapsedRealtimeDelta)) * 100.0d;
                                        BatteryService.this.mTopBlockedApp = (ArrayMap) BatteryService.this.mPowerManager.getTopAppBlocked(3);
                                        BatteryService.this.mTopBlockedAppList = new ArrayList(new HashMap(BatteryService.this.mTopBlockedApp).entrySet());
                                        Collections.sort(BatteryService.this.mTopBlockedAppList, new Comparator<Entry<String, Long>>() {
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
                                        Slog.i(BatteryService.TAG_PM, "Total frameworks blocked time: " + BatteryService.this.mFrameworksBlockedTime + " ms, frameworks blocked ratio is " + BatteryService.this.mFrameworksBlockedRatio);
                                        for (Entry<String, Long> entry2 : BatteryService.this.mTopBlockedAppList) {
                                            Slog.i(BatteryService.TAG_PM, "app " + ((String) entry2.getKey()).toString() + " blocked suspend " + ((Long) entry2.getValue()).toString() + " ms");
                                        }
                                        double target_average_current = active_average_current + ((BatteryService.this.mBatteryCapacityFromPowerProfile * BatteryService.this.mBatteryCapacityDeviation) / (((double) BatteryService.this.mElapsedRealtimeDelta) / 3600000.0d));
                                        if (BatteryService.DEBUG_PANIC) {
                                            Slog.i(BatteryService.TAG_PM, "active_average_current is " + active_average_current + ", target_average_current is " + target_average_current);
                                        }
                                        double average_current = ((BatteryService.this.mBatteryCapacityDelta - ((double) BatteryService.this.mBatteryJumpSocDelta)) * 3600000.0d) / ((double) BatteryService.this.mElapsedRealtimeDelta);
                                        double average_current_without_telephone_wifi = ((((BatteryService.this.mBatteryCapacityDelta - BatteryService.this.mPowerLostByTelephone) - BatteryService.this.mPowerLostByWifi) - ((double) BatteryService.this.mBatteryJumpSocDelta)) * 3600000.0d) / ((double) BatteryService.this.mElapsedRealtimeDelta);
                                        BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.FRAMEWORKSBLOCKEDTIME, new DecimalFormat("#.##").format(BatteryService.this.mFrameworksBlockedTime));
                                        BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.AVERAGECURRENT, new DecimalFormat("#.##").format(average_current));
                                        Intent intent = new Intent("android.intent.action.ACTION_OPPO_POWER_STANDBY_CURRENT");
                                        intent.setPackage("com.oppo.oppopowermonitor");
                                        intent.putExtra("OffTime", BatteryService.this.mLastScreenOffTime);
                                        intent.putExtra("Duration", BatteryService.this.mElapsedRealtimeDelta);
                                        intent.putExtra("Suspend", BatteryService.this.mSuspengRatio);
                                        intent.putExtra("Average", average_current);
                                        intent.putExtra("Wifi", wifiCode);
                                        intent.putExtra("Network", BatteryService.this.mDataNetworkState);
                                        intent.putExtra("networkOperator", BatteryService.this.mDataNetworkOperatorName);
                                        intent.putExtra("SIM1", BatteryService.this.mSimCard1Name);
                                        intent.putExtra("SIM2", BatteryService.this.mSimCard2Name);
                                        boolean isClassified = false;
                                        if (average_current <= target_average_current) {
                                            BatteryService.this.mPowerEventRecordSB.append(",Average:");
                                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.DECIMAL_FORMAT.format(average_current));
                                            BatteryService.this.mPowerEventRecordSB.append("mA");
                                            BatteryService.this.mPowerEventRecordSB.append("[PASS]");
                                            intent.putExtra("Reason", 0);
                                            BatteryService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                                            BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, PowerStatusReport.OK);
                                            BatteryService.this.mPowerStatusReport.report();
                                            if (BatteryService.DEBUG_PANIC) {
                                                Slog.i(BatteryService.TAG_PM, "Average : " + BatteryService.DECIMAL_FORMAT.format(average_current) + "mA : [PASS]");
                                            }
                                        } else if (average_current_without_telephone_wifi <= target_average_current) {
                                            if (BatteryService.this.mConnectivityManager == null) {
                                                Slog.e(BatteryService.TAG_PM, "mConnectivityManager is null");
                                            } else {
                                                BatteryService.this.mTelephonePowerState = BatteryService.this.mConnectivityManager.getTelephonyPowerState();
                                                if (BatteryService.DEBUG_PANIC) {
                                                    Slog.i(BatteryService.TAG_PM, "mTelephonePowerState : " + BatteryService.this.mTelephonePowerState);
                                                }
                                                if (BatteryService.this.mTelephonePowerState != null) {
                                                    String[] telephony_event_cache = BatteryService.this.mTelephonePowerState.split(" ");
                                                    if (telephony_event_cache != null && telephony_event_cache.length > 0) {
                                                        for (i = 0; i < telephony_event_cache.length; i++) {
                                                            BatteryService.this.mPowerEventRecordSB.append(",").append(telephony_event_cache[i].trim());
                                                            String[] telephony_event = telephony_event_cache[i].split(":");
                                                            if (telephony_event != null && telephony_event.length > 0) {
                                                                try {
                                                                    if (Double.parseDouble(telephony_event[1].trim()) > 0.0d) {
                                                                        if ("DATA_CALL_COUNT".equals(telephony_event[0].trim())) {
                                                                            OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_54, BatteryService.this.mContext.getResources().getString(17040973));
                                                                            if (!isClassified) {
                                                                                intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_54);
                                                                                BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "DATA_CALL_COUNT");
                                                                                isClassified = true;
                                                                            }
                                                                        } else if ("NO_SERVICE_TIME".equals(telephony_event[0].trim())) {
                                                                            OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_55, BatteryService.this.mContext.getResources().getString(17040974));
                                                                            if (!isClassified) {
                                                                                intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_55);
                                                                                BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "NO_SERVICE_TIME");
                                                                                isClassified = true;
                                                                            }
                                                                        } else if ("RESELECT_PER_MIN".equals(telephony_event[0].trim())) {
                                                                            OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_56, BatteryService.this.mContext.getResources().getString(17040975));
                                                                            if (!isClassified) {
                                                                                intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_56);
                                                                                BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "RESELECT_PER_MIN");
                                                                                isClassified = true;
                                                                            }
                                                                        } else if ("SMS_SEND_COUNT".equals(telephony_event[0].trim())) {
                                                                            OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_57, BatteryService.this.mContext.getResources().getString(17040976));
                                                                            if (!isClassified) {
                                                                                intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_57);
                                                                                BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "SMS_SEND_COUNT");
                                                                                isClassified = true;
                                                                            }
                                                                        } else if (BatteryService.DEBUG_PANIC) {
                                                                            Slog.d(BatteryService.TAG_PM, "telephony_event not defined : " + telephony_event[0]);
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e2) {
                                                                    Slog.d(BatteryService.TAG_PM, "telephony_event NumberFormatException : " + e2.getMessage());
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (BatteryService.this.mWifiPowerEventList != null && BatteryService.this.mWifiPowerEventList.length() > 0) {
                                                if (BatteryService.this.mWifiPowerEventList.contains("SCAN_FREQUENT")) {
                                                    OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_50, BatteryService.this.mContext.getResources().getString(17040969));
                                                    if (!isClassified) {
                                                        intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_50);
                                                        BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "SCAN_FREQUENT");
                                                        isClassified = true;
                                                    }
                                                } else if (BatteryService.this.mWifiPowerEventList.contains("RENEW_FREQUENT")) {
                                                    OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_53, BatteryService.this.mContext.getResources().getString(17040972));
                                                    if (!isClassified) {
                                                        intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_53);
                                                        BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "RENEW_FREQUENT");
                                                        isClassified = true;
                                                    }
                                                } else if (BatteryService.this.mWifiPowerEventList.contains("GROUP_FREQUENT")) {
                                                    OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_52, BatteryService.this.mContext.getResources().getString(17040971));
                                                    if (!isClassified) {
                                                        intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_52);
                                                        BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "GROUP_FREQUENT");
                                                        isClassified = true;
                                                    }
                                                } else if (BatteryService.this.mWifiPowerEventList.contains("DISCONN_FREQUENT")) {
                                                    OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_51, BatteryService.this.mContext.getResources().getString(17040970));
                                                    if (!isClassified) {
                                                        intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_51);
                                                        BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "DISCONN_FREQUENT");
                                                        isClassified = true;
                                                    }
                                                }
                                            }
                                            BatteryService.this.mPowerEventRecordSB.append(",Average:");
                                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.DECIMAL_FORMAT.format(average_current_without_telephone_wifi));
                                            BatteryService.this.mPowerEventRecordSB.append("mA");
                                            BatteryService.this.mPowerEventRecordSB.append("[EX_PASS]");
                                            if (!isClassified) {
                                                intent.putExtra("Reason", OppoMultiAppManager.USER_ID);
                                                BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "OTHER");
                                            }
                                            BatteryService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                                            BatteryService.this.mPowerStatusReport.report();
                                            if (BatteryService.DEBUG_PANIC) {
                                                Slog.i(BatteryService.TAG_PM, "Average : " + BatteryService.DECIMAL_FORMAT.format(average_current_without_telephone_wifi) + "mA : [EX_PASS]");
                                            }
                                        } else {
                                            BatteryService.this.mPowerEventRecordSB.append(",Average:");
                                            BatteryService.this.mPowerEventRecordSB.append(BatteryService.DECIMAL_FORMAT.format(average_current_without_telephone_wifi));
                                            BatteryService.this.mPowerEventRecordSB.append("mA");
                                            if (BatteryService.DEBUG_PANIC) {
                                                Slog.i(BatteryService.TAG_PM, "Average : " + BatteryService.DECIMAL_FORMAT.format(average_current_without_telephone_wifi) + "mA");
                                            }
                                            if (BatteryService.this.mIsMusicScene) {
                                                OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_58, BatteryService.this.mContext.getResources().getString(17040977));
                                                if (null == null) {
                                                    intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_58);
                                                    BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "MUSICSCENE");
                                                    isClassified = true;
                                                }
                                            }
                                            if (BatteryService.this.mDownloadScene) {
                                                OppoManager.incrementCriticalData(OppoManager.TYPE_ANDROID_PM_EVENT_59, BatteryService.this.mContext.getResources().getString(17040978));
                                                if (!isClassified) {
                                                    intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_59);
                                                    BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "DOWNLOADSCENE");
                                                    isClassified = true;
                                                }
                                            }
                                            if (BatteryService.this.mDownloadScene || BatteryService.this.mIsMusicScene) {
                                                BatteryService.this.mPowerEventRecordSB.append("[IGNORE]");
                                                BatteryService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                                                BatteryService.this.mPowerStatusReport.report();
                                                if (BatteryService.DEBUG_PANIC) {
                                                    Slog.i(BatteryService.TAG_PM, "[IGNORE]");
                                                }
                                                return;
                                            }
                                            int type = 0;
                                            String desc = null;
                                            String issue_tag = IElsaManager.EMPTY_PACKAGE;
                                            if (BatteryService.this.mTopApResumeReasonName == null || ((double) BatteryService.this.mTopApResumeCount) / (((double) BatteryService.this.mElapsedRealtimeDelta) / 3600000.0d) <= 15.0d) {
                                                if (average_current > 22.0d && BatteryService.this.mSuspengRatio > 99.0d) {
                                                    type = OppoManager.TYPE_ANDROID_PM_EVENT_63;
                                                    desc = BatteryService.this.mContext.getResources().getString(17040982);
                                                    issue_tag = "base_subsystem";
                                                    if (!isClassified) {
                                                        intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_63);
                                                        BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "SUBSYSTEM");
                                                        isClassified = true;
                                                    }
                                                }
                                            } else if ("wcnss_wlan".equals(BatteryService.this.mTopApResumeReasonName)) {
                                                type = OppoManager.TYPE_ANDROID_PM_EVENT_60;
                                                desc = BatteryService.this.mContext.getResources().getString(17040979);
                                                issue_tag = "wifi_wakeup";
                                                if (!isClassified) {
                                                    intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_60);
                                                    BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "WCNSS_WLAN");
                                                    isClassified = true;
                                                }
                                            } else if ("modem".equals(BatteryService.this.mTopApResumeReasonName)) {
                                                type = OppoManager.TYPE_ANDROID_PM_EVENT_61;
                                                desc = BatteryService.this.mContext.getResources().getString(17040980);
                                                issue_tag = "modem_wakeup";
                                                if (!isClassified) {
                                                    intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_61);
                                                    BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "MODEM");
                                                    isClassified = true;
                                                }
                                            } else if ("qpnp_rtc_alarm".equals(BatteryService.this.mTopApResumeReasonName)) {
                                                type = OppoManager.TYPE_ANDROID_PM_EVENT_62;
                                                desc = BatteryService.this.mContext.getResources().getString(17040981);
                                                issue_tag = "alarm_wakeup";
                                                if (!isClassified) {
                                                    intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_62);
                                                    BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "QPNP_RTC_ALARM");
                                                    isClassified = true;
                                                }
                                            }
                                            if (!isClassified) {
                                                BatteryService.this.mPowerStatusReport.putData(PowerStatusReport.ISSUETYPE, "OTHER");
                                            }
                                            BatteryService.this.mPowerStatusReport.report();
                                            if (type == 0 || desc == null || issue_tag == null || issue_tag.isEmpty()) {
                                                type = OppoManager.TYPE_ANDROID_PM_EVENT_64;
                                                desc = BatteryService.this.mContext.getResources().getString(17040983);
                                                issue_tag = "power_other";
                                                intent.putExtra("Reason", OppoManager.TYPE_ANDROID_PM_EVENT_64);
                                            }
                                            BatteryService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                                            if (OppoManager.writeLogToPartition(type, BatteryService.this.mPowerEventRecordSB.toString(), "ANDROID", issue_tag, desc) != 1) {
                                                Slog.e(BatteryService.TAG_PM, "increment average current over event info failed!!");
                                            } else if (SystemProperties.getBoolean("persist.sys.assert.panic", false) && BatteryService.this.mPowerMonitorTipEnabled) {
                                                intent = new Intent("oppo.intent.action.AVERAGE_CURRENT_OVER");
                                                if (BatteryService.this.mTopApResumeReasonName != null) {
                                                    intent.putExtra("wakeup_source_top", BatteryService.this.mTopApResumeReasonName);
                                                }
                                                BatteryService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                                                Slog.i(BatteryService.TAG_PM, "oppo.intent.action.AVERAGE_CURRENT_OVER sended");
                                            }
                                        }
                                    }
                                }, 1000);
                            } else {
                                if (BatteryService.DEBUG_PANIC) {
                                    Slog.i(BatteryService.TAG_PM, "mLastPowerEventTime : " + BatteryService.this.TimeStamp2Date(BatteryService.this.mLastPowerEventTime, BatteryService.DATE_FORMAT) + ", so far not enough 24hours");
                                }
                            }
                        }
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    if (BatteryService.this.mLed != null) {
                        BatteryService.this.mLed.handleScreenOff();
                    }
                    if (BatteryService.this.mPowerMonitorEnabled) {
                        BatteryService.this.mScreenOffTime = System.currentTimeMillis();
                        BatteryService.this.mElapsedRealtimeScreenOff = SystemClock.elapsedRealtime();
                        BatteryService.this.mUptimeMillisScreenOff = SystemClock.uptimeMillis();
                        BatteryService.this.mBatteryCapacityScreenOff = BatteryService.this.obs.mBatteryRealtimeCapacity == -1 ? BatteryService.this.getBatteryRealtimeCapacity(BatteryService.this.mBatteryProps.batteryLevel) : (double) BatteryService.this.obs.mBatteryRealtimeCapacity;
                        BatteryService.this.mBatteryJumpSocScreenOff = BatteryService.this.obs.mBatteryJumpSocTotal;
                        if (BatteryService.this.mScreenOffTime - BatteryService.this.mLastNetworkStatsUpdateTime > 30000) {
                            try {
                                BatteryService.this.mNetworkStatsScreenOff = BatteryService.this.mNetworkStatsFactory.readNetworkStatsDetail();
                                BatteryService.this.mLastNetworkStatsUpdateTime = BatteryService.this.mScreenOffTime;
                            } catch (IOException e2) {
                                BatteryService.this.mNetworkStatsScreenOff = null;
                                Slog.e(BatteryService.TAG_PM, "readNetworkStatsDetail IOException --- ");
                            }
                        }
                        BatteryService.this.mVirtualKeyInterruptsScreenOff = BatteryService.this.getVirtualKeyInterrupts();
                        BatteryService.this.mProcessUsingAudio = BatteryService.this.getActiveAudioPids(BatteryService.this.mContext);
                        BatteryService.this.mMusicScene = false;
                        BatteryService.this.mProcessUsingAudioConfirm = null;
                        BatteryService.this.mKeepAudioProcess = IElsaManager.EMPTY_PACKAGE;
                        BatteryService.this.mIsOverTemperature = false;
                        if (BatteryService.this.mHandler.hasCallbacks(BatteryService.this.mDetectAudioProcessRunnable)) {
                            BatteryService.this.mHandler.removeCallbacks(BatteryService.this.mDetectAudioProcessRunnable);
                        }
                        BatteryService.this.mHandler.postDelayed(BatteryService.this.mDetectAudioProcessRunnable, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                        if (BatteryService.DEBUG) {
                            Slog.i(BatteryService.TAG_PM, "mLastScreenOffTime = " + BatteryService.this.mLastScreenOffTime + ", mElapsedRealtimeScreenOff = " + BatteryService.this.mElapsedRealtimeScreenOff + ", mUptimeMillisScreenOff = " + BatteryService.this.mUptimeMillisScreenOff + ", mBatteryCapacityScreenOff = " + BatteryService.this.mBatteryCapacityScreenOff + ", mBatteryJumpSocScreenOff = " + BatteryService.this.mBatteryJumpSocScreenOff + ", mVirtualKeyInterruptsScreenOff = " + BatteryService.this.mVirtualKeyInterruptsScreenOff + ", mProcessUsingAudio = " + BatteryService.this.mProcessUsingAudio);
                        }
                    }
                }
            }
        };
        this.mDetectAudioProcessRunnable = new Runnable() {
            public void run() {
                BatteryService.this.mMusicScene = false;
                BatteryService.this.mProcessUsingAudioConfirm = null;
                BatteryService.this.mKeepAudioProcess = IElsaManager.EMPTY_PACKAGE;
                BatteryService.this.mProcessUsingAudioConfirm = BatteryService.this.getActiveAudioPids(BatteryService.this.mContext);
                if (BatteryService.DEBUG_PANIC) {
                    Slog.i(BatteryService.TAG_PM, "mProcessUsingAudioConfirm : " + BatteryService.this.mProcessUsingAudioConfirm);
                }
                if (BatteryService.this.mProcessUsingAudio != null && BatteryService.this.mProcessUsingAudioConfirm != null) {
                    String[] pids = BatteryService.this.mProcessUsingAudio.split(":");
                    String[] pids_confirm = BatteryService.this.mProcessUsingAudioConfirm.split(":");
                    if (pids.length > 0 && pids_confirm.length > 0) {
                        ArrayList<RunningAppProcessInfo> appList = BatteryService.this.getRunningAppList(BatteryService.this.mContext);
                        for (Object equals : pids) {
                            for (int j = 0; j < pids_confirm.length; j++) {
                                if (pids_confirm[j].equals(equals)) {
                                    try {
                                        int pid = Integer.parseInt(pids_confirm[j].trim());
                                        BatteryService.this.mMusicScene = true;
                                        for (int k = 0; k < appList.size(); k++) {
                                            RunningAppProcessInfo appInfo = (RunningAppProcessInfo) appList.get(k);
                                            if (pid == appInfo.pid) {
                                                for (String str : appInfo.pkgList) {
                                                    BatteryService batteryService = BatteryService.this;
                                                    batteryService.mKeepAudioProcess = batteryService.mKeepAudioProcess + str;
                                                }
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        Slog.d(BatteryService.TAG_PM, "mDetectAudioProcessRunnable NumberFormatException:" + e.getMessage());
                                    }
                                }
                            }
                        }
                        if (BatteryService.DEBUG_PANIC) {
                            Slog.i(BatteryService.TAG_PM, "mKeepAudioProcess : " + BatteryService.this.mKeepAudioProcess);
                        }
                    }
                }
            }
        };
        this.mBootCompleted = false;
        this.mContext = context;
        this.mHandler = new Handler(true);
        this.mLed = new Led(context, (LightsManager) getLocalService(LightsManager.class));
        this.mBatteryStats = BatteryStatsService.getService();
        this.obs = new OppoBatteryService(context);
        this.mCriticalBatteryLevel = this.mContext.getResources().getInteger(17694804);
        this.mLowBatteryWarningLevel = this.mContext.getResources().getInteger(17694806);
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694807);
        this.mShutdownBatteryTemperature = this.mContext.getResources().getInteger(17694805);
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
        this.mPowerStatusReport = new PowerStatusReport(this.mContext);
    }

    public void onStart() {
        IntentFilter filter;
        boolean is_test_version;
        boolean z;
        boolean z2 = false;
        IBinder b = ServiceManager.getService("batteryproperties");
        if (IBatteryPropertiesRegistrar.Stub.asInterface(b) == null) {
            SystemProperties.set("ctl.restart", "healthd");
        }
        try {
            IBatteryPropertiesRegistrar.Stub.asInterface(b).registerListener(new BatteryListener(this, null));
        } catch (RemoteException e) {
        }
        if (SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_BOOT_IPO");
            filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.ACTION_BOOT_IPO".equals(intent.getAction())) {
                        BatteryService.this.mIPOShutdown = false;
                        BatteryService.this.mIPOBoot = true;
                        BatteryService.this.mLastBatteryLevel = BatteryService.this.mLowBatteryWarningLevel + 1;
                        BatteryService.this.update(BatteryService.this.mBatteryProps);
                    } else if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(intent.getAction())) {
                        BatteryService.this.mIPOShutdown = true;
                    }
                }
            }, filter);
        }
        this.mBinderService = new BinderService(this, null);
        publishBinderService("battery", this.mBinderService);
        publishLocalService(BatteryManagerInternal.class, new LocalService(this, null));
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        if (this.mPackageManager == null) {
            this.mPackageManager = this.mContext.getPackageManager();
        }
        if (SystemProperties.getBoolean("persist.sys.op_test_version", false)) {
            is_test_version = true;
        } else {
            is_test_version = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
        }
        if (is_test_version) {
            z = false;
        } else {
            z = true;
        }
        this.mPowerMonitorEnabled = z;
        if (!(SystemProperties.getBoolean("ro.build.release_type", false) || is_test_version)) {
            z2 = true;
        }
        this.mPowerMonitorTipEnabled = z2;
        Slog.i(TAG_PM, "mPowerMonitorEnabled=" + this.mPowerMonitorEnabled + ", mPowerMonitorTipEnabled=" + this.mPowerMonitorTipEnabled);
        this.mPowerProfile = new PowerProfile(this.mContext);
        this.mBatteryCapacityFromPowerProfile = this.mPowerProfile.getBatteryCapacity();
        Slog.i(TAG_PM, "mBatteryCapacityFromPowerProfile = " + this.mBatteryCapacityFromPowerProfile);
        this.mNetworkStatsFactory = new NetworkStatsFactory();
        filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        if (this.mPackageManager != null) {
            this.mIsSellModeVersion = this.mPackageManager.hasSystemFeature("oppo.specialversion.exp.sellmode");
        }
    }

    public void onBootPhase(int phase) {
        Object obj;
        if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
            obj = this.mLock;
            synchronized (obj) {
                this.mBootCompleted = true;
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
            obj = this.mLock;
            synchronized (obj) {
                AnonymousClass6 anonymousClass6 = new ContentObserver(this.mHandler) {
                    public void onChange(boolean selfChange) {
                        synchronized (BatteryService.this.mLock) {
                            BatteryService.this.mLed.updateLightsLocked();
                        }
                    }
                };
                this.mLowPowerSettingsObservers.observe();
                this.mChargeSettingsObservers.observe();
                this.mLed.updateLightsLocked();
            }
        } else {
            return;
        }
    }

    private void updateBatteryWarningLevelLocked() {
        ContentResolver resolver = this.mContext.getContentResolver();
        int defWarnLevel = this.mContext.getResources().getInteger(17694806);
        this.mLowBatteryWarningLevel = Global.getInt(resolver, "low_power_trigger_level", defWarnLevel);
        if (this.mLowBatteryWarningLevel == 0) {
            this.mLowBatteryWarningLevel = defWarnLevel;
        }
        if (this.mLowBatteryWarningLevel < this.mCriticalBatteryLevel) {
            this.mLowBatteryWarningLevel = this.mCriticalBatteryLevel;
        }
        this.mLowBatteryCloseWarningLevel = this.mLowBatteryWarningLevel + this.mContext.getResources().getInteger(17694807);
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
        if (this.mBatteryProps.batteryLevel == 0 && !isPoweredLocked(7)) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (ActivityManagerNative.isSystemReady()) {
                        if (SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                            SystemProperties.set("sys.ipo.battlow", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                        }
                        Slog.v(BatteryService.TAG, "mBatteryProps.batteryLevel = " + BatteryService.this.mBatteryProps.batteryLevel + "shutdown because of low power");
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_LOW_BATTERY_POWER_OFF, new String[0]);
                        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
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

    /* JADX WARNING: Missing block: B:36:0x0098, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void update(BatteryProperties props) {
        synchronized (this.mLock) {
            if (DEBUG_COMMAND) {
                Slog.v(TAG, "update mUpdatesStopped = " + this.mUpdatesStopped);
            }
            if (this.mUpdatesStopped) {
                this.mLastBatteryProps.set(props);
            } else {
                this.mBatteryProps = props;
                if (SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mIPOShutdown) {
                    return;
                }
                if (this.obs != null) {
                    this.obs.native_update();
                }
                this.mOtgOnline = this.obs.mOtgOnline;
                if (this.obs.mBatteryCurrent < 0 && Math.abs(this.obs.mBatteryCurrent) > this.mMaxChargeCurrent) {
                    this.mMaxChargeCurrent = Math.abs(this.obs.mBatteryCurrent);
                }
                if (this.mBatteryProps.batteryTemperature > this.mMaxChargeTemperature) {
                    this.mMaxChargeTemperature = this.mBatteryProps.batteryTemperature;
                }
                if (this.mBatteryProps.batteryTemperature < this.mMinChargeTemperature) {
                    this.mMinChargeTemperature = this.mBatteryProps.batteryTemperature;
                }
                if (this.obs != null) {
                    this.mBatteryNotifyCode = this.obs.mBatteryNotifyCode;
                }
                if (this.mBootCompleted) {
                    processValuesLocked(false);
                }
            }
        }
    }

    private void processValuesLocked(boolean force) {
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
        if (SystemProperties.get("ro.mtk_diso_support").equals("true") && this.mBatteryProps.chargerAcOnline && this.mBatteryProps.chargerUsbOnline) {
            this.mPlugType = 3;
        }
        if (this.mIsSellModeVersion && this.mPlugType != 0 && getCurrentChargeStateForSaleInternal() == 0) {
            this.mBatteryProps.batteryStatus = 2;
            if (DEBUG_PANIC) {
                Slog.d(TAG, "SellMode version batteryStatus: " + this.mBatteryProps.batteryStatus + "  mPlugType:" + this.mPlugType);
            }
        }
        if (this.mPlugType != this.mTempLastPlugType) {
            final Intent plugged_changed_intent = new Intent("oppo.intent.action.BATTERY_PLUGGED_CHANGED");
            plugged_changed_intent.addFlags(536870912);
            plugged_changed_intent.putExtra("plugged", this.mPlugType);
            if (ActivityManagerNative.isSystemReady()) {
                this.mTempLastPlugType = this.mPlugType;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        if (BatteryService.DEBUG_PANIC) {
                            Slog.d(BatteryService.TAG, "send broadcast : oppo.intent.action.BATTERY_PLUGGED_CHANGED : mPlugType" + BatteryService.this.mTempLastPlugType);
                        }
                        ActivityManagerNative.broadcastStickyIntent(plugged_changed_intent, null, -1);
                    }
                });
            }
        }
        if (this.mBatteryProps.batteryTemperature > 500) {
            this.mIsOverTemperature = true;
        }
        if (DEBUG) {
            Slog.d(TAG, "Processing new values: chargerAcOnline=" + this.mBatteryProps.chargerAcOnline + ", chargerUsbOnline=" + this.mBatteryProps.chargerUsbOnline + ", chargerWirelessOnline=" + this.mBatteryProps.chargerWirelessOnline + ", maxChargingCurrent" + this.mBatteryProps.maxChargingCurrent + ", maxChargingVoltage" + this.mBatteryProps.maxChargingVoltage + ", chargeCounter" + this.mBatteryProps.batteryChargeCounter + ", batteryStatus=" + this.mBatteryProps.batteryStatus + ", batteryHealth=" + this.mBatteryProps.batteryHealth + ", batteryPresent=" + this.mBatteryProps.batteryPresent + ", batteryLevel=" + this.mBatteryProps.batteryLevel + ", batteryTechnology=" + this.mBatteryProps.batteryTechnology + ", batteryVoltage=" + this.mBatteryProps.batteryVoltage + ", batteryTemperature=" + this.mBatteryProps.batteryTemperature + ", mBatteryLevelCritical=" + this.mBatteryLevelCritical + ", mPlugType=" + this.mPlugType);
        }
        if (this.mLastBatteryVoltage != this.mBatteryProps.batteryVoltage) {
        }
        this.mLed.updateLightsLocked();
        try {
            this.mBatteryStats.setBatteryState(this.mBatteryProps.batteryStatus, this.mBatteryProps.batteryHealth, this.mPlugType, this.mBatteryProps.batteryLevel, this.mBatteryProps.batteryTemperature, this.mBatteryProps.batteryVoltage, this.mBatteryProps.batteryChargeCounter);
        } catch (RemoteException e) {
        }
        shutdownIfNoPowerLocked();
        shutdownIfOverTempLocked();
        if (this.obs != null) {
            this.obs.processAdditionalValuesLocked(this.mBatteryProps.batteryTemperature, this.mPlugType, this.mBatteryProps.batteryLevel);
        }
        if (this.mBatteryProps.batteryStatus == 2 && !(this.mMaxChargeCurrent == this.mLastMaxChargeCurrent && this.mMaxChargeTemperature == this.mLastMaxChargeTemperature && this.mMinChargeTemperature == this.mLastMinChargeTemperature)) {
            if (this.mOppoUsageService == null) {
                IBinder serviceBinder = ServiceManager.getService("usage");
                if (serviceBinder != null) {
                    this.mOppoUsageService = IOppoUsageService.Stub.asInterface(serviceBinder);
                    try {
                        this.mLastMaxChargeTemperature = this.mOppoUsageService.getMaxChargeTemperature();
                        this.mLastMinChargeTemperature = this.mOppoUsageService.getMinChargeTemperature();
                        this.mLastMaxChargeCurrent = this.mOppoUsageService.getMaxChargeCurrent();
                        this.isServiceBinded = true;
                    } catch (RemoteException exce) {
                        Slog.e(TAG, "mOppoUsageService RemoteException!", exce);
                        this.isServiceBinded = false;
                        this.mOppoUsageService = null;
                    }
                }
            }
            if (this.isServiceBinded && this.mOppoUsageService != null) {
                if (this.mMaxChargeTemperature > this.mLastMaxChargeTemperature) {
                    try {
                        this.mOppoUsageService.updateMaxChargeTemperature(this.mMaxChargeTemperature);
                        this.mLastMaxChargeTemperature = this.mMaxChargeTemperature;
                    } catch (RemoteException exce2) {
                        Slog.e(TAG, "updateMaxChargeTemperature failed!", exce2);
                        this.isServiceBinded = false;
                        this.mOppoUsageService = null;
                    }
                }
                if (this.mMinChargeTemperature < this.mLastMinChargeTemperature) {
                    try {
                        this.mOppoUsageService.updateMinChargeTemperature(this.mMinChargeTemperature);
                        this.mLastMinChargeTemperature = this.mMinChargeTemperature;
                    } catch (RemoteException exce22) {
                        Slog.e(TAG, "updateMinChargeTemperature failed!", exce22);
                        this.isServiceBinded = false;
                        this.mOppoUsageService = null;
                    }
                }
                if (this.mMaxChargeCurrent > this.mLastMaxChargeCurrent) {
                    try {
                        this.mOppoUsageService.updateMaxChargeCurrent(this.mMaxChargeCurrent);
                        this.mLastMaxChargeCurrent = this.mMaxChargeCurrent;
                    } catch (RemoteException exce222) {
                        Slog.e(TAG, "updateMaxChargeCurrent failed!", exce222);
                        this.isServiceBinded = false;
                        this.mOppoUsageService = null;
                    }
                }
            }
        }
        if (force || this.mBatteryProps.batteryStatus != this.mLastBatteryStatus || this.mBatteryProps.batteryLevel != this.mLastBatteryLevel || this.mPlugType != this.mLastPlugType || this.mBatteryNotifyCode != this.mLastBatteryNotifyCode || this.mOtgOnline != this.mLastOtgOnline || this.mBatteryProps.batteryTemperature / 10 != this.mLastBatteryTemperature / 10 || this.mBatteryProps.batteryChargeCounter != this.mLastChargeCounter || this.mInvalidCharger != this.mLastInvalidCharger) {
            Object[] objArr;
            if (this.mPlugType != this.mLastPlugType) {
                if (this.mLastPlugType == 0) {
                    if (!(this.mDischargeStartTime == 0 || this.mDischargeStartLevel == this.mBatteryProps.batteryLevel)) {
                        dischargeDuration = SystemClock.elapsedRealtime() - this.mDischargeStartTime;
                        logOutlier = true;
                        objArr = new Object[3];
                        objArr[0] = Long.valueOf(dischargeDuration);
                        objArr[1] = Integer.valueOf(this.mDischargeStartLevel);
                        objArr[2] = Integer.valueOf(this.mBatteryProps.batteryLevel);
                        EventLog.writeEvent(EventLogTags.BATTERY_DISCHARGE, objArr);
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
                Object[] objArr2 = new Object[5];
                objArr2[0] = Integer.valueOf(this.mBatteryProps.batteryStatus);
                objArr2[1] = Integer.valueOf(this.mBatteryProps.batteryHealth);
                objArr2[2] = Integer.valueOf(this.mBatteryProps.batteryPresent ? 1 : 0);
                objArr2[3] = Integer.valueOf(this.mPlugType);
                objArr2[4] = this.mBatteryProps.batteryTechnology;
                EventLog.writeEvent(EventLogTags.BATTERY_STATUS, objArr2);
            }
            if (this.mBatteryProps.batteryLevel != this.mLastBatteryLevel) {
                objArr = new Object[3];
                objArr[0] = Integer.valueOf(this.mBatteryProps.batteryLevel);
                objArr[1] = Integer.valueOf(this.mBatteryProps.batteryVoltage);
                objArr[2] = Integer.valueOf(this.mBatteryProps.batteryTemperature);
                EventLog.writeEvent(EventLogTags.BATTERY_LEVEL, objArr);
            }
            if (this.mBatteryLevelCritical && !this.mLastBatteryLevelCritical && this.mPlugType == 0) {
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
            sendIntentLocked();
            if (this.mPlugType != 0 && this.mLastPlugType == 0) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Intent statusIntent = new Intent("android.intent.action.ACTION_POWER_CONNECTED");
                        statusIntent.setFlags(67108864);
                        statusIntent.addFlags(4194304);
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            } else if (this.mPlugType == 0 && this.mLastPlugType != 0) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Intent statusIntent = new Intent("android.intent.action.ACTION_POWER_DISCONNECTED");
                        statusIntent.setFlags(67108864);
                        statusIntent.addFlags(4194304);
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            }
            if (shouldSendBatteryLowLocked()) {
                this.mSentLowBatteryBroadcast = true;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Intent statusIntent = new Intent("android.intent.action.BATTERY_LOW");
                        statusIntent.setFlags(67108864);
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            } else if (this.mSentLowBatteryBroadcast && this.mLastBatteryLevel >= this.mLowBatteryCloseWarningLevel) {
                this.mSentLowBatteryBroadcast = false;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Intent statusIntent = new Intent("android.intent.action.BATTERY_OKAY");
                        statusIntent.setFlags(67108864);
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            }
            if (this.mBatteryProps.batteryStatus != this.mLastBatteryStatus && this.mBatteryProps.batteryStatus == 6) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        String ACTION_IGNORE_DATA_USAGE_ALERT = NetworkStatsService.ACTION_IGNORE_DATA_USAGE_ALERT;
                        Log.d(BatteryService.TAG, "sendBroadcast ACTION_IGNORE_DATA_USAGE_ALERT");
                        Intent statusIntent = new Intent(NetworkStatsService.ACTION_IGNORE_DATA_USAGE_ALERT);
                        statusIntent.addFlags(536870912);
                        BatteryService.this.mContext.sendBroadcastAsUser(statusIntent, UserHandle.ALL);
                    }
                });
            }
            if (logOutlier && dischargeDuration != 0) {
                logOutlierLocked(dischargeDuration);
            }
            this.mLastBatteryStatus = this.mBatteryProps.batteryStatus;
            this.mLastBatteryStatus_smb = this.mBatteryProps.batteryStatus_smb;
            this.mLastBatteryHealth = this.mBatteryProps.batteryHealth;
            this.mLastBatteryPresent = this.mBatteryProps.batteryPresent;
            this.mLastBatteryPresent_smb = this.mBatteryProps.batteryPresent_smb;
            this.mLastBatteryLevel = this.mBatteryProps.batteryLevel;
            this.mLastBatteryLevel_smb = this.mBatteryProps.batteryLevel_smb;
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
        intent.putExtra("status", this.mBatteryProps.batteryStatus);
        intent.putExtra("status_2nd", this.mBatteryProps.batteryStatus_smb);
        intent.putExtra("health", this.mBatteryProps.batteryHealth);
        intent.putExtra("present", this.mBatteryProps.batteryPresent);
        intent.putExtra("present_2nd", this.mBatteryProps.batteryPresent_smb);
        intent.putExtra("level", this.mBatteryProps.batteryLevel);
        intent.putExtra("level_2nd", this.mBatteryProps.batteryLevel_smb);
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
            Slog.d(TAG, "Sending ACTION_BATTERY_CHANGED.  level:" + this.mBatteryProps.batteryLevel + ", scale:" + 100 + ", status:" + this.mBatteryProps.batteryStatus + ", health:" + this.mBatteryProps.batteryHealth + ", present:" + this.mBatteryProps.batteryPresent + ", voltage: " + this.mBatteryProps.batteryVoltage + ", temperature: " + this.mBatteryProps.batteryTemperature + ", technology: " + this.mBatteryProps.batteryTechnology + ", AC powered:" + this.mBatteryProps.chargerAcOnline + ", USB powered:" + this.mBatteryProps.chargerUsbOnline + ", Wireless powered:" + this.mBatteryProps.chargerWirelessOnline + ", notify code:" + this.mBatteryNotifyCode + ", otg online:" + this.mOtgOnline + ", icon:" + icon + ", invalid charger:" + this.mInvalidCharger + ", maxChargingVoltage:" + this.mBatteryProps.maxChargingVoltage + ", status_smb:" + this.mBatteryProps.batteryStatus_smb + ", present_smb:" + this.mBatteryProps.batteryPresent_smb + ",level_smb:" + this.mBatteryProps.batteryLevel_smb + ", maxChargingCurrent:" + this.mBatteryProps.maxChargingCurrent + ", chargeCounter:" + this.mBatteryProps.batteryChargeCounter);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c4 A:{SYNTHETIC, Splitter: B:42:0x00c4} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0086 A:{SYNTHETIC, Splitter: B:30:0x0086} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00fb A:{SYNTHETIC, Splitter: B:52:0x00fb} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c4 A:{SYNTHETIC, Splitter: B:42:0x00c4} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0086 A:{SYNTHETIC, Splitter: B:30:0x0086} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00fb A:{SYNTHETIC, Splitter: B:52:0x00fb} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void logBatteryStatsLocked() {
        RemoteException e;
        IOException e2;
        Throwable th;
        IBinder batteryInfoService = ServiceManager.getService("batterystats");
        if (batteryInfoService != null) {
            DropBoxManager db = (DropBoxManager) this.mContext.getSystemService("dropbox");
            if (db != null && db.isTagEnabled("BATTERY_DISCHARGE_INFO")) {
                File dumpFile = null;
                FileOutputStream dumpStream = null;
                try {
                    File dumpFile2 = new File("/data/system/batterystats.dump");
                    try {
                        FileOutputStream dumpStream2 = new FileOutputStream(dumpFile2);
                        try {
                            batteryInfoService.dump(dumpStream2.getFD(), DUMPSYS_ARGS);
                            FileUtils.sync(dumpStream2);
                            db.addFile("BATTERY_DISCHARGE_INFO", dumpFile2, 2);
                            if (dumpStream2 != null) {
                                try {
                                    dumpStream2.close();
                                } catch (IOException e3) {
                                    Slog.e(TAG, "failed to close dumpsys output stream");
                                }
                            }
                            if (!(dumpFile2 == null || dumpFile2.delete())) {
                                Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile2.getAbsolutePath());
                            }
                            dumpFile = dumpFile2;
                        } catch (RemoteException e4) {
                            e = e4;
                            dumpStream = dumpStream2;
                            dumpFile = dumpFile2;
                            Slog.e(TAG, "failed to dump battery service", e);
                            if (dumpStream != null) {
                            }
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                        } catch (IOException e5) {
                            e2 = e5;
                            dumpStream = dumpStream2;
                            dumpFile = dumpFile2;
                            try {
                                Slog.e(TAG, "failed to write dumpsys file", e2);
                                if (dumpStream != null) {
                                }
                                Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                            } catch (Throwable th2) {
                                th = th2;
                                if (dumpStream != null) {
                                    try {
                                        dumpStream.close();
                                    } catch (IOException e6) {
                                        Slog.e(TAG, "failed to close dumpsys output stream");
                                    }
                                }
                                if (!(dumpFile == null || dumpFile.delete())) {
                                    Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            dumpStream = dumpStream2;
                            dumpFile = dumpFile2;
                            if (dumpStream != null) {
                            }
                            Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                            throw th;
                        }
                    } catch (RemoteException e7) {
                        e = e7;
                        dumpFile = dumpFile2;
                        Slog.e(TAG, "failed to dump battery service", e);
                        if (dumpStream != null) {
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                    } catch (IOException e8) {
                        e2 = e8;
                        dumpFile = dumpFile2;
                        Slog.e(TAG, "failed to write dumpsys file", e2);
                        if (dumpStream != null) {
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                    } catch (Throwable th4) {
                        th = th4;
                        dumpFile = dumpFile2;
                        if (dumpStream != null) {
                        }
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                        throw th;
                    }
                } catch (RemoteException e9) {
                    e = e9;
                    Slog.e(TAG, "failed to dump battery service", e);
                    if (dumpStream != null) {
                        try {
                            dumpStream.close();
                        } catch (IOException e10) {
                            Slog.e(TAG, "failed to close dumpsys output stream");
                        }
                    }
                    if (!(dumpFile == null || dumpFile.delete())) {
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                    }
                } catch (IOException e11) {
                    e2 = e11;
                    Slog.e(TAG, "failed to write dumpsys file", e2);
                    if (dumpStream != null) {
                        try {
                            dumpStream.close();
                        } catch (IOException e12) {
                            Slog.e(TAG, "failed to close dumpsys output stream");
                        }
                    }
                    if (!(dumpFile == null || dumpFile.delete())) {
                        Slog.e(TAG, "failed to delete temporary dumpsys file: " + dumpFile.getAbsolutePath());
                    }
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
            return 17303277;
        }
        if (this.mBatteryProps.batteryStatus == 3) {
            return 17303263;
        }
        if (this.mBatteryProps.batteryStatus == 4 || this.mBatteryProps.batteryStatus == 5) {
            return (!isPoweredLocked(7) || this.mBatteryProps.batteryLevel < 100) ? 17303263 : 17303277;
        } else {
            return 17303291;
        }
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Battery service (battery) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  set [ac|usb|wireless|status|level|invalid] <value>");
        pw.println("    Force a battery property value, freezing battery state.");
        pw.println("  unplug");
        pw.println("    Force battery unplugged, freezing battery state.");
        pw.println("  reset");
        pw.println("    Unfreeze battery state, returning to current hardware values.");
    }

    /* JADX WARNING: Missing block: B:14:0x01a4, code:
            if ("-a".equals(r5[0]) != false) goto L_0x0008;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void dumpInternal(PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            if (args != null) {
                if (args.length != 0) {
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
            pw.println("  status: " + this.mBatteryProps.batteryStatus);
            pw.println("  status: " + this.mBatteryProps.batteryStatus_smb);
            pw.println("  health: " + this.mBatteryProps.batteryHealth);
            pw.println("  present: " + this.mBatteryProps.batteryPresent);
            pw.println("  present: " + this.mBatteryProps.batteryPresent_smb);
            pw.println("  level: " + this.mBatteryProps.batteryLevel);
            pw.println("  level: " + this.mBatteryProps.batteryLevel_smb);
            pw.println("  scale: 100");
            pw.println("  voltage: " + this.mBatteryProps.batteryVoltage);
            pw.println("  temperature: " + this.mBatteryProps.batteryTemperature);
            pw.println("  technology: " + this.mBatteryProps.batteryTechnology);
        }
    }

    int onShellCommand(Shell shell, String cmd) {
        long ident;
        boolean z = true;
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        if (cmd.equals("unplug")) {
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
                processValuesLocked(false);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else if (cmd.equals("set")) {
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
                boolean z2;
                if (key.equals("ac")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    batteryProperties.chargerAcOnline = z2;
                } else if (key.equals("usb")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    batteryProperties.chargerUsbOnline = z2;
                } else if (key.equals("wireless")) {
                    batteryProperties = this.mBatteryProps;
                    if (Integer.parseInt(value) != 0) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    batteryProperties.chargerWirelessOnline = z2;
                } else if (key.equals("status")) {
                    this.mBatteryProps.batteryStatus = Integer.parseInt(value);
                } else if (key.equals("level")) {
                    this.mBatteryProps.batteryLevel = Integer.parseInt(value);
                } else if (key.equals("invalid")) {
                    this.mInvalidCharger = Integer.parseInt(value);
                } else {
                    pw.println("Unknown set option: " + key);
                    update = false;
                }
                BatteryProperties batteryProperties2;
                if ("ac".equals(key)) {
                    batteryProperties2 = this.mBatteryProps;
                    if (Integer.parseInt(value) == 0) {
                        z = false;
                    }
                    batteryProperties2.chargerAcOnline = z;
                } else if ("usb".equals(key)) {
                    batteryProperties2 = this.mBatteryProps;
                    if (Integer.parseInt(value) == 0) {
                        z = false;
                    }
                    batteryProperties2.chargerUsbOnline = z;
                } else if ("wireless".equals(key)) {
                    batteryProperties2 = this.mBatteryProps;
                    if (Integer.parseInt(value) == 0) {
                        z = false;
                    }
                    batteryProperties2.chargerWirelessOnline = z;
                } else if ("status".equals(key)) {
                    this.mBatteryProps.batteryStatus = Integer.parseInt(value);
                } else if ("status_smb".equals(key)) {
                    this.mBatteryProps.batteryStatus_smb = Integer.parseInt(value);
                } else if ("level".equals(key)) {
                    this.mBatteryProps.batteryLevel = Integer.parseInt(value);
                } else if ("level_smb".equals(key)) {
                    this.mBatteryProps.batteryLevel_smb = Integer.parseInt(value);
                } else if ("invalid".equals(key)) {
                    this.mInvalidCharger = Integer.parseInt(value);
                } else {
                    pw.println("Unknown set option: " + key);
                    update = false;
                }
                if (update) {
                    ident = Binder.clearCallingIdentity();
                    this.mUpdatesStopped = true;
                    processValuesLocked(false);
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
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            ident = Binder.clearCallingIdentity();
            try {
                if (this.mUpdatesStopped) {
                    this.mUpdatesStopped = false;
                    this.mBatteryProps.set(this.mLastBatteryProps);
                    processValuesLocked(false);
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        DEBUG_COMMAND = this.mUpdatesStopped;
        return 0;
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
                    new Shell().exec(this.mBinderService, null, fd, null, args, new ResultReceiver(null));
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
        } else if ("pm".equals(logCategoryTag)) {
            this.mPowerMonitorEnabled = on;
            this.mPowerMonitorTipEnabled = on;
            this.mScreenOffTime = System.currentTimeMillis();
            this.mElapsedRealtimeScreenOff = SystemClock.elapsedRealtime();
            this.mUptimeMillisScreenOff = SystemClock.uptimeMillis();
            this.mBatteryCapacityScreenOff = this.obs.mBatteryRealtimeCapacity == -1 ? getBatteryRealtimeCapacity(this.mBatteryProps.batteryLevel) : (double) this.obs.mBatteryRealtimeCapacity;
            this.mBatteryJumpSocScreenOff = this.obs.mBatteryJumpSocTotal;
            this.mNetworkStatsScreenOff = null;
            this.mVirtualKeyInterruptsScreenOff = getVirtualKeyInterrupts();
            this.mLastNetworkStatsUpdateTime = 0;
            this.mLastPowerEventTime = 0;
        } else if ("pm_debug".equals(logCategoryTag)) {
            this.mPowerMonitorDebugMode = true;
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
}
