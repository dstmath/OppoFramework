package com.mediatek.internal.telephony;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.common.ColorFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.OppoManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Pair;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.util.WakeupMessage;
import com.color.app.ColorAppEnterInfo;
import com.color.app.ColorAppExitInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.ColorAppSwitchManager;
import com.coloros.deepthinker.IColorDeepThinkerManager;
import com.coloros.deepthinker.ServiceStateObserver;
import com.coloros.eventhub.sdk.EventCallback;
import com.coloros.eventhub.sdk.aidl.DeviceEvent;
import com.coloros.eventhub.sdk.aidl.DeviceEventResult;
import com.coloros.eventhub.sdk.aidl.EventRequestConfig;
import com.mediatek.internal.telephony.uicc.MtkRuimRecords;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimConstants;
import java.util.ArrayList;
import java.util.Arrays;

public class EndcBearController {
    private static final String ACTION_COMMAND_FORCE_DISABLE_ENDC = "android.intent.force_disable_endc";
    private static final String ACTION_DISABLE_ENDC_TIMER_OUT = "android.intent.disable_endc.timer";
    public static final String ACTION_ENDC_PWR_WORK_STATE_UPDATE = "android.intent.action.endc_pwr_workstate_update";
    public static final String ACTION_INFORM_LTE_QUALITY = "android.intent.action.Smart5g_ltequality";
    public static final String ACTION_INFORM_RAT_CHANGED = "android.intent.action.RatChange";
    public static final String ACTION_LTE_POOR_THRESHOLD_CONFIG = "android.intent.action.Smart5g_ltePoorThres";
    public static final String ACTION_SMART5G_KEY_INFO = "oppo.intent.action.SMART5G_KEYINFO";
    private static final String ACTION_THERMAL_THROTTLING = "oppo.intent.action.THERMAL_THROTTLING_5G";
    protected static final int AVG_SAMPLE_SPEED_LEV0 = 0;
    protected static final int AVG_SAMPLE_SPEED_LEV1 = 500;
    protected static final int AVG_SAMPLE_SPEED_LEV2 = 1024;
    protected static final int AVG_SAMPLE_SPEED_LEV3 = 1500;
    protected static final int AVG_SAMPLE_SPEED_LEV4 = 2048;
    protected static final int AVG_SAMPLE_SPEED_ROW_NUM = 2;
    public static final String CHINESE_MCC = "460";
    protected static final int DISABLE_ENDC_DIS_TIMER_OUT = 0;
    protected static final int DISABLE_ENDC_FORCE_DISABLE = 2;
    protected static final int DISABLE_ENDC_SCREEN_OFF = 3;
    protected static final int DISABLE_ENDC_SPEED_LOW = 1;
    private static final int DIS_ENDC_CAUSE_LEN = 4;
    protected static final int DORECOVERY_DISABLE_ENDC_INDEX = 0;
    private static final int ENABLE_ENDC_CAUSE_LEN = 6;
    protected static final int ENABLE_ENDC_LTE_JAM = 3;
    protected static final int ENABLE_ENDC_LTE_POOR = 2;
    protected static final int ENABLE_ENDC_OTHER = 5;
    protected static final int ENABLE_ENDC_SPEED_HIGH = 0;
    protected static final int ENABLE_ENDC_STATE_PROHIBIT = 4;
    protected static final int ENABLE_ENDC_SWITCH_OFF = 1;
    protected static final int ENDC_BEAR_CONTINUOUS_T_LEV1 = 180;
    protected static final int ENDC_BEAR_CONTINUOUS_T_LEV2 = 360;
    protected static final int ENDC_BEAR_CONTINUOUS_T_LEV3 = 540;
    protected static final int ENDC_PWR_APK_PARA_LENGTH = 5;
    protected static final int ENDC_PWR_CFG_LOW_BAT_UP_LIMIT = 30;
    protected static final int ENDC_PWR_CFG_PARA_LENGTH = 17;
    protected static final int ENDC_PWR_CFG_POOR_LTE_BW_THRES_MAX = 20000;
    protected static final int ENDC_PWR_CFG_POOR_LTE_BW_THRES_MIN = 0;
    protected static final int ENDC_PWR_CFG_POOR_LTE_RSRP_THRES_MAX = 0;
    protected static final int ENDC_PWR_CFG_POOR_LTE_RSRP_THRES_MIN = -160;
    protected static final int ENDC_PWR_CFG_POOR_LTE_RSRQ_THRES_MAX = 0;
    protected static final int ENDC_PWR_CFG_POOR_LTE_RSRQ_THRES_MIN = -40;
    protected static final int ENDC_PWR_CFG_STATS_AVG_SPEED_MAX = 10240;
    protected static final int ENDC_PWR_CFG_STATS_AVG_SPEED_MIN = 0;
    protected static final int ENDC_PWR_CFG_STATS_DIS_ENDC_TIMER_MAX = 600;
    protected static final int ENDC_PWR_CFG_STATS_DIS_ENDC_TIMER_MIN = 0;
    protected static final int ENDC_PWR_CFG_STATS_DURATION_MAX = 10;
    protected static final int ENDC_PWR_CFG_STATS_DURATION_MIN = 2;
    private static final long ENDC_PWR_OPT_KEY_LOG_RPT_PERIOD_SEC = 43200;
    protected static final int ENDC_PWR_SCENES_PARA_LENGTH = 4;
    protected static final int EVENT_CHECK_NR_BEAR_DEALLOC = 1010;
    protected static final int EVENT_CMD_CONNECT_EXT_TELEPHONY_SERV = 1000;
    private static final int EVENT_DATA_ENABLED_CHANGED = 1015;
    protected static final int EVENT_ENABLE_ENDC_HYSTERESIS_EXP = 1013;
    protected static final int EVENT_FLOW_CHANGE = 1001;
    protected static final int EVENT_FORCE_DISABLE_ENDC_TIME_OUT = 1009;
    protected static final int EVENT_GET_DEACT_SCG_CONFIG = 1006;
    protected static final int EVENT_LTE_JAM_TIME_EXPIRED = 1012;
    protected static final int EVENT_MONITOR_APK_STATE_CHANGED = 1014;
    protected static final int EVENT_NETWORK_MODE_CHANGED = 1008;
    protected static final int EVENT_QUERY_STATE = 1004;
    protected static final int EVENT_RADIO_ON = 1003;
    protected static final int EVENT_SCENE_STATE_CHANGED = 1007;
    private static final int EVENT_SERVICE_STATE_CHANGED = 1011;
    private static final long FIRST_FLOW_CHANGE_STATS_DURATION_MS = 3000;
    public static final int INVALID_INT = Integer.MAX_VALUE;
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_ENDC_PWR_OPT = "endc_pwr_opt";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCENES_INFO = "SCENES_INFO";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_THERMAL_KPIS = "thermal_kpis";
    private static final int KEY_INFO_BROCAST_BAT_THRES = 5;
    protected static final int LIGHT_ENDC_PWR_CFG_PARA_LENGTH = 5;
    protected static final int LOG_RPT_CAUSE_FORCE_DISABLE = 2;
    protected static final int LOG_RPT_CAUSE_SWITCH_CHANE = 1;
    protected static final int LOG_RPT_CAUSE_TIME_REACH = 0;
    protected static final int LTE_DATA_QUALITY_THRES_HIGH = 500;
    protected static final int LTE_DATA_QUALITY_THRES_LOW = 40;
    protected static final int LTE_QUALITY_BUFFER_SIZE = 7;
    protected static final int LTE_SINAGL_QUALITY_BAD_THRES = 5;
    protected static final int LTE_SINAGL_QUALITY_P_THRES = -90;
    protected static final int LTE_SINAGL_QUALITY_S_THRES = 0;
    protected static final int SCENE_BYTES_NO_NR_ID = 4;
    protected static final int SCENE_BYTES_WITH_NR_ID = 2;
    protected static final int SCENE_IN = 1;
    protected static final int SCENE_NO_NR_TIME_ID = 3;
    protected static final int SCENE_NR_TIME_ID = 1;
    protected static final int SCENE_OUT = 0;
    protected static final int SCENE_TIME_ID = 0;
    protected static final int SCENE_TYPES_OFFSET = 500;
    protected static final int SCENE_TYPE_FILE_TRANSFER = 504;
    protected static final int SCENE_TYPE_GAME = 502;
    protected static final int SCENE_TYPE_MAX = 504;
    protected static final int SCENE_TYPE_MIN = 500;
    protected static final int SCENE_TYPE_VIDEO = 500;
    protected static final int SCENE_TYPE_VIDEO_CALL = 501;
    protected static final int SCENE_TYPE_VIDEO_LIVE = 503;
    protected static final int SPEED_LEV0_ID = 0;
    protected static final int SPEED_LEV1_ID = 1;
    protected static final int SPEED_LEV2_ID = 2;
    protected static final int SPEED_LEV3_ID = 3;
    protected static final int SPEED_LEV4_ID = 4;
    protected static final long STANDARD_VALUE = 1024;
    private static final int SYS_OEM_NW_DIAG_CAUSE_ENDC_PWR_OPT = 103;
    private static final int SYS_OEM_NW_DIAG_CAUSE_SCENES_INFO = 99;
    private static final int SYS_OEM_NW_DIAG_CAUSE_THERMAL_KPIS = 133;
    protected static final int THERMAL_ACTION_TYPE_SIZE = 5;
    protected static final int TH_COUNT_COLUMN_ID = 0;
    protected static final int TH_DISABLE_ENDC_INDEX = 1;
    protected static final int TH_DURA_COLUMN_ID = 1;
    protected static final int TH_FALLBACK_ROW_ID = 4;
    protected static final int TH_START_TIMR_COLUMN_ID = 2;
    private static int backOffTimeMilliSeconds = MtkGsmCdmaPhone.EVENT_IMS_UT_DONE;
    private static int connectionRetry = 8;
    private static EndcBearController instance = null;
    private static final String mEuexCountry = SystemProperties.get("ro.oppo.euex.country", "INVALID");
    private static final boolean mExpVersion = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    public static String mNetBuildType = SystemProperties.get("persist.sys.net_build_type", "allnet");
    private static final String mOperatorName = SystemProperties.get("ro.oppo.operator", "oppo");
    private static final String mRegionMark = SystemProperties.get("ro.oppo.regionmark", "");
    private static final long mSecondMs = 1000;
    private static final boolean mSmart5g2Enable = SystemProperties.get("persist.oppo.nw.smart5g_2", "0").equals("1");
    private long DISABLE_ENDC_DELAY_MS = 86400000;
    private boolean Debug = true;
    private long ENABLE_ENDC_HYSTERESIS_MS = 9000;
    private long ENABLE_ENDC_HYSTERESIS_MS_RADIO_ON = 300000;
    private long FLOW_CHANGE_STATS_DURATION_MS = FIRST_FLOW_CHANGE_STATS_DURATION_MS;
    private long FORCE_DISABLE_ENDC_TIMER_SEC = 600;
    private long LIGHT_DISABLE_ENDC_DELAY_MS = 86400000;
    private long LTE_JAM_PUNISH_MS = 120000;
    private long NR_BEAR_CHECK_DEALLOC_MS = 500;
    private long REWARD_DISABLE_ENDC_DELAY_MS = 86400000;
    private boolean is5gIcon = false;
    private boolean isCmccEval = isCmccEvalBuild();
    private boolean isFirstEnableEndc = true;
    private boolean isLightSwitchOn = true;
    private boolean isLtePoor = false;
    private boolean isNrBearAlloc = false;
    private boolean isNrEnabled = false;
    private boolean isRadioOnEndcHys = true;
    private boolean isScreenOn = true;
    private boolean isSwitchOn = true;
    private int[][] mAgSpeedLevCnt = {new int[]{0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0}};
    private AlarmManager mAlarmManager;
    int mApkMonitorVersionN = 2;
    private int mBatThresAsLow = 20;
    private boolean mChargingEnable = true;
    private String mCmccBlackApkStr = "com.network.test,org.zwanoo.android.speedtest,com.splashpadmobile.speedtest,com.cmcc.cmvideo,com.tencent.tmgp.pubgmhd";
    private String mCmccEndcBlackApkStr = "com.cmcc.cmvideo,com.tencent.tmgp.pubgmhd";
    private Context mContext;
    private int mCurrBatteryLevel = 100;
    private ArrayList<String> mCurrentApks = new ArrayList<>();
    private ArrayList<String> mCurrentBlackApks = new ArrayList<>();
    private long mCurrentDisableEndcTimerLength = 0;
    private ArrayList<String> mCurrentEndcBlackApks = new ArrayList<>();
    private int mCurrentRat = 0;
    private int mDefalutEnable = 0;
    private int mDefaultDataSlotId = 0;
    private PendingIntent mDisEdncTimerAlarmIntent = null;
    private int mDisEndcTimerOutCnt = 0;
    private int[] mDisableEndcCausetCnt = new int[4];
    private int mDisableEndcCnt = 0;
    private long mDisableEndcSettingTime = 0;
    private final ColorAppSwitchManager.OnAppSwitchObserver mDynamicObserver = new ColorAppSwitchManager.OnAppSwitchObserver() {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass10 */

        public void onAppEnter(ColorAppEnterInfo info) {
            if (info != null && info.extension != null) {
                EndcBearController endcBearController = EndcBearController.this;
                endcBearController.log("onAppEnter " + info.targetName);
                if (info.targetName == null) {
                    return;
                }
                if (!EndcBearController.this.isCmccEval || -1 == EndcBearController.this.mCmccEndcBlackApkStr.indexOf(info.targetName)) {
                    EndcBearController.this.mCurrentApks.add(info.targetName);
                    EndcBearController endcBearController2 = EndcBearController.this;
                    endcBearController2.log("current apks:" + Arrays.toString(EndcBearController.this.mCurrentApks.toArray()) + "size: " + EndcBearController.this.mCurrentApks.size());
                    if (-1 != EndcBearController.this.mEndcLowPwrBlackApkStr.indexOf(info.targetName)) {
                        EndcBearController.this.mCurrentBlackApks.add(info.targetName);
                    }
                    EndcBearController.this.sendMonitorApkStateChangedMsg();
                    return;
                }
                EndcBearController.this.mCurrentEndcBlackApks.add(info.targetName);
                EndcBearController.this.mEndcBlackApkRunning = true;
            }
        }

        public void onAppExit(ColorAppExitInfo info) {
            if (info != null && info.extension != null) {
                EndcBearController endcBearController = EndcBearController.this;
                endcBearController.log("onAppExit " + info.targetName);
                if (!EndcBearController.this.isCmccEval || -1 == EndcBearController.this.mCmccEndcBlackApkStr.indexOf(info.targetName)) {
                    EndcBearController.this.mCurrentApks.remove(info.targetName);
                    EndcBearController endcBearController2 = EndcBearController.this;
                    endcBearController2.log("current apks:" + Arrays.toString(EndcBearController.this.mCurrentApks.toArray()));
                    if (-1 != EndcBearController.this.mEndcLowPwrBlackApkStr.indexOf(info.targetName)) {
                        EndcBearController.this.mCurrentBlackApks.remove(info.targetName);
                    }
                    EndcBearController.this.sendMonitorApkStateChangedMsg();
                    return;
                }
                EndcBearController.this.mCurrentEndcBlackApks.remove(info.targetName);
                if (EndcBearController.this.mCurrentEndcBlackApks.isEmpty()) {
                    EndcBearController.this.mEndcBlackApkRunning = false;
                }
            }
        }

        public void onActivityEnter(ColorAppEnterInfo info) {
            EndcBearController endcBearController = EndcBearController.this;
            endcBearController.log("onActivityEnter " + info.targetName);
        }

        public void onActivityExit(ColorAppExitInfo info) {
            EndcBearController endcBearController = EndcBearController.this;
            endcBearController.log("onActivityExit " + info.targetName);
        }
    };
    private int[] mEnableEndcCausetCnt = new int[6];
    private int mEnableEndcCnt = 0;
    private long mEnableEndcSettingTime = 0;
    private int[] mEndcBearContinuousTime = {0, 0, 0};
    private long mEndcBearDuration = 0;
    private boolean mEndcBlackApkRunning = false;
    private String mEndcLowPwrBlackApkStr = "com.heytap.market,com.oppo.market,org.zwanoo.android.speedtest,com.splashpadmobile.speedtest";
    private ContentObserver mEndcLowPwrMonitorApkLst = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass9 */

        public void onChange(boolean selfChange) {
            EndcBearController.this.updateMonitorApkList();
        }
    };
    private String mEndcLowPwrRestrictApkStr = "com.antutu.ABenchMark,com.tencent.qqlive,com.qiyi.video,com.youku.phone,air.tv.douyu.android,com.duowan.kiwi,com.heytap.yoli,com.heytap.yoli:media";
    private ContentObserver mEndcLowPwrScenesConfig = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass8 */

        public void onChange(boolean selfChange) {
            EndcBearController.this.updateScenesConfig();
        }
    };
    int mEndcLowPwrVersionN = 2;
    private HandlerThread mEndcThread;
    private ServiceStateObserver mEventHubServiceStateObserver;
    private int mFileTransferRestrictEnable = 1;
    private int mGameRestrictEnable = 1;
    private Handler mHandler;
    private boolean mHotspotEnable = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass1 */

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            boolean z = true;
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1896134962:
                    if (action.equals(EndcBearController.ACTION_THERMAL_THROTTLING)) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -1538406691:
                    if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1397837751:
                    if (action.equals(EndcBearController.ACTION_DISABLE_ENDC_TIMER_OUT)) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1172645946:
                    if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case -1084684149:
                    if (action.equals(EndcBearController.ACTION_INFORM_LTE_QUALITY)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -25388475:
                    if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 409953495:
                    if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1184034032:
                    if (action.equals(EndcBearController.ACTION_INFORM_RAT_CHANGED)) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 2075333766:
                    if (action.equals(EndcBearController.ACTION_COMMAND_FORCE_DISABLE_ENDC)) {
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
                    EndcBearController.this.log("received SCREEN ON ");
                    EndcBearController.this.isScreenOn = true;
                    break;
                case 1:
                    EndcBearController.this.log("received SCREEN OFF ");
                    EndcBearController.this.isScreenOn = false;
                    break;
                case 2:
                    EndcBearController.this.mCurrBatteryLevel = intent.getIntExtra("level", 100);
                    if (EndcBearController.this.mLastBrocastkeyInfoBatLev == 0) {
                        EndcBearController endcBearController = EndcBearController.this;
                        endcBearController.mLastBrocastkeyInfoBatLev = endcBearController.mCurrBatteryLevel;
                    } else if (EndcBearController.this.mLastBrocastkeyInfoBatLev - EndcBearController.this.mCurrBatteryLevel >= 5) {
                        EndcBearController endcBearController2 = EndcBearController.this;
                        endcBearController2.broadcastSmart5gKeylog(endcBearController2.isSwitchOn, EndcBearController.this.mCurrBatteryLevel);
                    }
                    boolean mIsChargingBackup = EndcBearController.this.mIsCharging;
                    EndcBearController endcBearController3 = EndcBearController.this;
                    if (intent.getIntExtra("plugged", 0) == 0) {
                        z = false;
                    }
                    endcBearController3.mIsCharging = z;
                    if (mIsChargingBackup != EndcBearController.this.mIsCharging) {
                        EndcBearController.this.log("received ChargingState change ");
                        break;
                    } else {
                        return;
                    }
                case 3:
                    boolean mIsWifihotspotOnBackup = EndcBearController.this.mIsWifihotspotOn;
                    EndcBearController.this.log("WIFI_AP_STATE_CHANGED_ACTION: state  " + intent.getIntExtra("wifi_state", 0));
                    if (intent.getIntExtra("wifi_state", 0) == 13) {
                        EndcBearController.this.mIsWifihotspotOn = true;
                    } else if (intent.getIntExtra("wifi_state", 0) == 11) {
                        EndcBearController.this.mIsWifihotspotOn = false;
                    } else {
                        return;
                    }
                    if (mIsWifihotspotOnBackup == EndcBearController.this.mIsWifihotspotOn) {
                        return;
                    }
                    break;
                case 4:
                    if (EndcBearController.this.mPhone != null) {
                        EndcBearController.this.mPhone.mCi.unregisterForOn(EndcBearController.this.mHandler);
                        EndcBearController.this.unregisterPrefNetworkModeObserver();
                        EndcBearController.this.unregisterForServiceStateChanged();
                        EndcBearController.this.unregisterForMobileDataSwitchState();
                    }
                    EndcBearController.this.mHandler.removeMessages(1003);
                    EndcBearController.this.log("defaultSlotIndx old: " + EndcBearController.this.mDefaultDataSlotId + ", new: " + EndcBearController.this.mSubscriptionManager.getDefaultDataPhoneId());
                    SubscriptionManager unused = EndcBearController.this.mSubscriptionManager;
                    if (SubscriptionManager.isValidPhoneId(EndcBearController.this.mSubscriptionManager.getDefaultDataPhoneId())) {
                        EndcBearController endcBearController4 = EndcBearController.this;
                        endcBearController4.mDefaultDataSlotId = endcBearController4.mSubscriptionManager.getDefaultDataPhoneId();
                        EndcBearController endcBearController5 = EndcBearController.this;
                        endcBearController5.mPhone = PhoneFactory.getPhone(endcBearController5.mDefaultDataSlotId);
                        if (EndcBearController.this.mPhone != null) {
                            EndcBearController.this.mPhone.mCi.registerForOn(EndcBearController.this.mHandler, 1003, (Object) null);
                            EndcBearController.this.registerPrefNetworkModeObserver();
                            EndcBearController.this.mPhone.registerForServiceStateChanged(EndcBearController.this.mHandler, 1011, (Object) null);
                            EndcBearController.this.registerForMobileDataSwitchState();
                        }
                    }
                    EndcBearController.this.actionWhenDdsOrSwitchChange();
                    return;
                case 5:
                    int mPhoneId = intent.getIntExtra("PhoneId", -1);
                    boolean mltePoor = intent.getBooleanExtra("ltePoor", false);
                    EndcBearController.this.log("ACTION_INFORM_LTE_QUALITY mPhoneId=" + mPhoneId + ",mltePoor=" + mltePoor);
                    if (mPhoneId == EndcBearController.this.mDefaultDataSlotId) {
                        EndcBearController.this.isLtePoor = mltePoor;
                        return;
                    }
                    return;
                case 6:
                    int mPhoneId2 = intent.getIntExtra("PhoneId", -1);
                    int oldRat = intent.getIntExtra("OldRat", -1);
                    int newRat = intent.getIntExtra("NewRat", -1);
                    EndcBearController.this.log("ACTION_INFORM_RAT_CHANGED mPhoneId=" + mPhoneId2 + ",OldRat=" + oldRat + ",NewRat=" + newRat);
                    if (mPhoneId2 == EndcBearController.this.mDefaultDataSlotId) {
                        EndcBearController.this.mCurrentRat = newRat;
                        EndcBearController endcBearController6 = EndcBearController.this;
                        boolean currentEndcBearStatus = endcBearController6.getEndcBearStatus(endcBearController6.mCurrentRat, EndcBearController.this.isNrBearAlloc);
                        if (currentEndcBearStatus != EndcBearController.this.mLastEndcBearStatus) {
                            EndcBearController endcBearController7 = EndcBearController.this;
                            endcBearController7.updateEndcBearInfo(endcBearController7.mLastEndcBearStatus);
                        }
                        EndcBearController.this.mLastEndcBearStatus = currentEndcBearStatus;
                        return;
                    }
                    return;
                case 7:
                    EndcBearController.this.log("ACTION_DISABLE_ENDC_TIMER_OUT");
                    EndcBearController endcBearController8 = EndcBearController.this;
                    endcBearController8.enableEndc(endcBearController8.mDefaultDataSlotId, false, 0);
                    EndcBearController.this.mLastState = false;
                    EndcBearController.access$2408(EndcBearController.this);
                    EndcBearController.this.mIsDisableEndcTimerOut = true;
                    EndcBearController.this.mDisEdncTimerAlarmIntent = null;
                    return;
                case '\b':
                    int mPhoneId3 = intent.getIntExtra("PhoneId", -1);
                    EndcBearController.this.log("ACTION_COMMAND_FORCE_DISABLE_ENDC phoneid= " + mPhoneId3);
                    if (mPhoneId3 == EndcBearController.this.mDefaultDataSlotId && EndcBearController.this.mPhone != null) {
                        try {
                            if (!EndcBearController.this.mPhone.is_test_card()) {
                                String currentOperatorNumeric = EndcBearController.this.mPhone.getServiceState().getOperatorNumeric();
                                if (!TextUtils.isEmpty(currentOperatorNumeric) && !currentOperatorNumeric.startsWith("460")) {
                                    return;
                                }
                                EndcBearController.this.mNeedForceDisableEndcMsk |= 1;
                                EndcBearController.this.log("start FORCE_DISABLE_ENDC_TIME_OUT in second is  " + EndcBearController.this.FORCE_DISABLE_ENDC_TIMER_SEC);
                                EndcBearController.this.mHandler.removeMessages(1009);
                                EndcBearController.this.mHandler.sendEmptyMessageDelayed(1009, EndcBearController.this.FORCE_DISABLE_ENDC_TIMER_SEC * EndcBearController.mSecondMs);
                                EndcBearController endcBearController9 = EndcBearController.this;
                                endcBearController9.reportEndcPwrKeylog(endcBearController9.isSwitchOn, 2, EndcBearController.this.isNrBearAlloc);
                                break;
                            } else {
                                return;
                            }
                        } catch (Exception e) {
                            EndcBearController.this.log("get operator numeric error: " + e);
                        }
                    } else {
                        return;
                    }
                    break;
                case '\t':
                    int tempValue = Integer.parseInt(intent.getStringExtra("Request"));
                    long currTime = SystemClock.elapsedRealtime() / EndcBearController.mSecondMs;
                    EndcBearController.this.log("received ACTION_THERMAL_THROTTLING Request value is  " + tempValue);
                    if (tempValue == 0) {
                        for (int actionIndex = 0; actionIndex < EndcBearController.this.mThermalActionName.length; actionIndex++) {
                            try {
                                String tempStr = intent.getStringExtra(EndcBearController.this.mThermalActionName[actionIndex]);
                                if (tempStr != null) {
                                    int tempValue2 = Integer.parseInt(tempStr);
                                    if (4 == actionIndex && tempValue2 == 0) {
                                        EndcBearController.this.mNeedForceDisableEndcMsk &= -3;
                                    }
                                    EndcBearController.this.updateThermalKeyInfo(actionIndex, currTime, tempValue2);
                                    EndcBearController.this.log("Thermal action is " + EndcBearController.this.mThermalActionName[actionIndex] + " value is  " + tempValue2);
                                }
                            } catch (Exception e2) {
                                EndcBearController.this.log("parser thermal info error: " + e2);
                                break;
                            }
                        }
                        break;
                    } else {
                        String tempStr2 = intent.getStringExtra(EndcBearController.this.mThermalActionName[4]);
                        if (tempStr2 != null) {
                            int tempValue3 = Integer.parseInt(tempStr2);
                            EndcBearController.this.updateThermalKeyInfo(4, currTime, tempValue3);
                            EndcBearController.this.log("Thermal action is " + EndcBearController.this.mThermalActionName[4] + " value is  " + tempValue3);
                            if (1 == tempValue3) {
                                EndcBearController.this.mNeedForceDisableEndcMsk = 2 | EndcBearController.this.mNeedForceDisableEndcMsk;
                                break;
                            }
                        }
                    }
                    break;
                case '\n':
                    EndcBearController.this.log("ConnectivityManager.CONNECTIVITY_ACTION ");
                    EndcBearController endcBearController10 = EndcBearController.this;
                    endcBearController10.mIsWifiConnected = endcBearController10.checkConnectedByType(1);
                    EndcBearController endcBearController11 = EndcBearController.this;
                    endcBearController11.updateMobileDataUsed(endcBearController11.mIsWifiConnected, EndcBearController.this.mIsMobileDataSwitchOn);
                    return;
                default:
                    EndcBearController.this.log("Unexpected broadcast intent: " + intent);
                    return;
            }
            EndcBearController.this.actionWhenStateChange();
        }
    };
    private boolean mIsAnyApkRunning = false;
    private boolean mIsAnyBlackApkRunning = false;
    private boolean mIsAnySceneNeedRestrict = false;
    private boolean mIsCharging = false;
    private boolean mIsDisableEndcTimerOut = false;
    private boolean mIsMobileDataSwitchOn = false;
    private boolean mIsMobileDataUsed = false;
    private boolean mIsServiceBound;
    private boolean mIsWifiConnected = false;
    private boolean mIsWifihotspotOn = false;
    private int mLastBrocastkeyInfoBatLev = 0;
    private long mLastDataStallTime = 0;
    private long mLastEndcBearStatsTime = 0;
    private boolean mLastEndcBearStatus = false;
    private long mLastEndcSettingChangeTime = 0;
    private long mLastEnterLteJamTime = 0;
    private long mLastKeylogRptTime = 0;
    private long mLastScenesKeylogRptTime = 0;
    private boolean mLastState = false;
    private long mLastThermalKeylogRptTime = 0;
    private long mLastincrementPs = Long.MAX_VALUE;
    int mLightEndcLowPwrVersionN = 1;
    private int mLightOptBatThresAsLow = 20;
    private boolean mLightOptLowBatEnable = true;
    private int mLightOptLowBatSpeedThres = MtkRuimRecords.PHB_DELAY_SEND_TIME;
    private ContentObserver mLightOptParaObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass7 */

        public void onChange(boolean selfChange) {
            EndcBearController.this.updateLightOptParaFromSettings(false);
        }
    };
    private boolean mLightOptScreenOffEnable = true;
    private int mLightOptScreenOffSpeedThres = 204800;
    private ContentObserver mLightSwitchObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass4 */

        public void onChange(boolean selfChange) {
            EndcBearController.this.actionWhenLightSwitchChange();
        }
    };
    private boolean mLowBatHeavyOptEnable = true;
    private int mLowBatStatsAvgSpeedThres = 3072;
    private boolean mLteJam = false;
    private int mLteJamCnt = 0;
    private long mLteJamDuration = 0;
    private int mLteQualityBuffCnt = 0;
    private int[] mLteRxSpeedBuffer = new int[7];
    private int[] mLteSingalQualityBuffer = new int[7];
    private IColorDeepThinkerManager mManager;
    private ContentObserver mMobileDataSwitchObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass6 */

        public void onChange(boolean selfChange) {
            EndcBearController.this.actionWhenMobileDataSwitchChange();
        }
    };
    private int mNeedForceDisableEndcMsk = 0;
    private boolean mNeedRewardDisEndcTimer = false;
    private int mNetworkMode = Phone.PREFERRED_NT_MODE;
    private long mNoEndcBearDuration = 0;
    private long mOldRxBytes = 0;
    private long mOldTxBytes = 0;
    private String mPackageName;
    private ContentObserver mParaObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass3 */

        public void onChange(boolean selfChange) {
            EndcBearController.this.updateParaFromSettings(false);
        }
    };
    protected Phone mPhone = null;
    private int mPoorLteBwKhzThres = 0;
    private int mPoorLteRsrpThres = ENDC_PWR_CFG_POOR_LTE_RSRP_THRES_MIN;
    private int mPoorLteRsrqThres = -40;
    private ContentObserver mPrefNetworkModeObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass5 */

        public void onChange(boolean selfChange) {
            int networkMode = EndcBearController.this.getNetworkModeFromDB();
            EndcBearController endcBearController = EndcBearController.this;
            endcBearController.log("PrefNetworkModeObserver networkMode:" + networkMode);
            EndcBearController.this.mHandler.sendMessage(EndcBearController.this.mHandler.obtainMessage(1008, Integer.valueOf(networkMode)));
        }
    };
    private int mPunishScgAddStatsAvgSpeedThres = (this.mScgAddStatsAvgSpeedThres + 1024);
    private WakeupMessage mRetryAlarm;
    private int mRewardDisEndcTimerStatsAvgSpeedThres = 1024;
    int mSceneParaVersionN = 2;
    private long[][] mScenesKeyInfo = {new long[]{0, 0, 0, 0, 0}, new long[]{0, 0, 0, 0, 0}, new long[]{0, 0, 0, 0, 0}, new long[]{0, 0, 0, 0, 0}, new long[]{0, 0, 0, 0, 0}};
    private int mScenesNeedRestrictMsks = 1;
    private long[] mScenesNrNotUsedBytesStats = {0, 0, 0, 0, 0};
    private long[] mScenesNrNotUsedStartTimeStats = {0, 0, 0, 0, 0};
    private long[] mScenesNrUsedBytesStats = {0, 0, 0, 0, 0};
    private long[] mScenesNrUsedStartTimeStats = {0, 0, 0, 0, 0};
    private int mScenesRestrictEnableMsks = (((((this.mVideoRestrictEnable << 0) | (this.mVideoCallRestrictEnable << 1)) | (this.mGameRestrictEnable << 2)) | (this.mVideoLiveRestrictEnable << 3)) | (this.mFileTransferRestrictEnable << 4));
    private long[] mScenesStartTimeStats = {0, 0, 0, 0, 0};
    private int mScenesStatusMsks = 0;
    private int mScgAddSpeedForApk = 2048;
    private int mScgAddSpeedForScene = 2048;
    private int mScgAddSpeedLowbatForApk = 3072;
    private int mScgAddSpeedLowbatForScene = 3072;
    private int mScgAddStatsAvgSpeedThres = 2048;
    private int mScgFailSpeedForEndcBlackApk = 1024000;
    private int mScgFailStatsAvgSpeedThres = 20;
    private int mScgSpeedForEndcBlackApk = 1024000;
    private boolean mScreenoffOnlyEnable = false;
    private boolean mSib2NoNrEnable = false;
    private boolean mSib2UpLayerInd = true;
    private boolean mStartFlowIsNR = false;
    private final SubscriptionManager mSubscriptionManager;
    private ContentObserver mSwitchObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass2 */

        public void onChange(boolean selfChange) {
            EndcBearController.this.actionWhenDdsOrSwitchChange();
        }
    };
    private int mThermalActionMsk = 0;
    private String[] mThermalActionName = {"Uplink", "Downlink", "TxPower", "DropCell", "FallBack"};
    private long[][] mThermalKeyInfo = {new long[]{0, 0, 0}, new long[]{0, 0, 0}, new long[]{0, 0, 0}, new long[]{0, 0, 0}, new long[]{0, 0, 0}};
    private int mVideoCallRestrictEnable = 1;
    private int mVideoLiveRestrictEnable = 1;
    private int mVideoRestrictEnable = 1;
    private WifiManager mWifiManager;

    static /* synthetic */ int access$2408(EndcBearController x0) {
        int i = x0.mDisEndcTimerOutCnt;
        x0.mDisEndcTimerOutCnt = i + 1;
        return i;
    }

    private EndcBearController(Context context) {
        logM("EndcBearController creat");
        this.mContext = context;
        this.mEndcThread = new HandlerThread("EndcThread");
        this.mEndcThread.start();
        this.mHandler = new MyHandler(this.mEndcThread.getLooper());
        this.mRetryAlarm = new WakeupMessage(this.mContext, this.mHandler, "RETRY", (int) EVENT_CMD_CONNECT_EXT_TELEPHONY_SERV);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(EVENT_CMD_CONNECT_EXT_TELEPHONY_SERV));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        filter.addAction(ACTION_INFORM_LTE_QUALITY);
        filter.addAction(ACTION_INFORM_RAT_CHANGED);
        filter.addAction(ACTION_DISABLE_ENDC_TIMER_OUT);
        filter.addAction(ACTION_COMMAND_FORCE_DISABLE_ENDC);
        filter.addAction(ACTION_THERMAL_THROTTLING);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        registerForSwitchState();
        registerForParaSettings();
        registerForLightSwitchState();
        registerForLightOptParaSettings();
        registerForMonitorApkListSettings();
        registerForScenesSettings();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager != null) {
            this.mIsWifihotspotOn = wifiManager.getWifiApState() == 13;
            log("initial mIsWifihotspotOn: " + this.mIsWifihotspotOn);
        }
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        if (SubscriptionManager.isValidPhoneId(this.mSubscriptionManager.getDefaultDataPhoneId())) {
            this.mDefaultDataSlotId = this.mSubscriptionManager.getDefaultDataPhoneId();
            this.mPhone = PhoneFactory.getPhone(this.mDefaultDataSlotId);
            Phone phone = this.mPhone;
            if (phone != null) {
                phone.mCi.registerForOn(this.mHandler, 1003, (Object) null);
                this.mIsCharging = isDeviceCharging();
                log("initial mIsCharging =  " + this.mIsCharging);
                registerPrefNetworkModeObserver();
                this.mPhone.registerForServiceStateChanged(this.mHandler, 1011, (Object) null);
                registerForMobileDataSwitchState();
            }
        }
        logM("initial mSmart5g2Enable = " + mSmart5g2Enable);
        if (!mSmart5g2Enable) {
            this.mScenesRestrictEnableMsks = 0;
            this.mEndcLowPwrRestrictApkStr = "";
            this.mEndcLowPwrBlackApkStr = "";
        }
        if (this.isCmccEval) {
            this.isLightSwitchOn = false;
            this.DISABLE_ENDC_DELAY_MS = 21000;
            this.REWARD_DISABLE_ENDC_DELAY_MS = 61000;
            this.mScgAddStatsAvgSpeedThres = 2048;
            this.mLowBatStatsAvgSpeedThres = 3072;
            this.mPoorLteRsrpThres = ENDC_PWR_CFG_POOR_LTE_RSRP_THRES_MIN;
            this.mPoorLteRsrqThres = -40;
            this.mPoorLteBwKhzThres = 0;
            this.mEndcLowPwrBlackApkStr = this.mCmccBlackApkStr;
        }
        this.isSwitchOn = getDdsSmartfivegSwitch();
        if (isBuildTypeNeedCloseLightOptByDefault() || mExpVersion) {
            this.isLightSwitchOn = false;
            initLightSmartfivegSwitchConfig(this.isLightSwitchOn, true);
        } else {
            initLightSmartfivegSwitchConfig(this.isLightSwitchOn, false);
        }
        this.isLightSwitchOn = getLightSmartfivegSwitch();
        if (isOperatorNeedCloseChargingEnableOptByDefault()) {
            this.mChargingEnable = false;
        }
        updateParaFromSettings(true);
        updateLightOptParaFromSettings(true);
        this.mLastEndcBearStatsTime = SystemClock.elapsedRealtime() / mSecondMs;
        long j = this.mLastEndcBearStatsTime;
        this.mLastKeylogRptTime = j;
        this.mLastEndcSettingChangeTime = j;
        this.mLastThermalKeylogRptTime = j;
        log("initial mLastEndcBearStatsTime and mLastKeylogRptTime =  " + this.mLastEndcBearStatsTime);
        new EventListen();
        updateScenesConfig();
        this.mLastScenesKeylogRptTime = this.mLastEndcBearStatsTime;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        apkSwitchObserverRegDeterminnation(isWorkingState(), false);
        updateMonitorApkList();
        updateNoOpExpEnableSmart5gInd();
    }

    public static EndcBearController makeEndcBearController(Context context) {
        synchronized (EndcBearController.class) {
            if (instance == null) {
                instance = new EndcBearController(context);
            }
        }
        return instance;
    }

    public static EndcBearController getInstance() {
        return instance;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        if (this.Debug) {
            Rlog.d("EndcBearController", s);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logM(String s) {
        Rlog.d("EndcBearController", s);
    }

    private boolean getDdsSmartfivegSwitch() {
        int mSwitch = Settings.Global.getInt(this.mContext.getContentResolver(), "smart_fiveg", this.mDefalutEnable);
        logM("getDdsSmartfivegSwitch smart_fiveg setting is  " + mSwitch);
        Phone phone = this.mPhone;
        if (phone != null) {
            int mSubId = phone.getSubId();
            if (SubscriptionManager.isValidSubscriptionId(mSubId)) {
                ContentResolver contentResolver = this.mContext.getContentResolver();
                mSwitch = Settings.Global.getInt(contentResolver, "smart_fiveg" + mSubId, this.mDefalutEnable);
                logM("getDdsSmartfivegSwitch smart_fiveg on DDS subid " + mSubId + "setting is  " + mSwitch);
            }
        }
        if (mSwitch == 1) {
            return true;
        }
        return false;
    }

    private void initLightSmartfivegSwitchConfig(boolean lightSwitchOn, boolean isForceUpdate) {
        int mSwitchValue = Settings.Global.getInt(this.mContext.getContentResolver(), "light_smart_fiveg", 221);
        int mSwitchValue2 = 1;
        if (isForceUpdate || !(mSwitchValue == 0 || mSwitchValue == 1)) {
            if (!lightSwitchOn) {
                mSwitchValue2 = 0;
            }
            Settings.Global.putInt(this.mContext.getContentResolver(), "light_smart_fiveg", mSwitchValue2);
            log("initLightSmartfivegSwitchConfig set value " + mSwitchValue2 + " isforce " + isForceUpdate);
            return;
        }
        log("initLightSmartfivegSwitchConfig already configed is  " + mSwitchValue);
    }

    private boolean getLightSmartfivegSwitch() {
        int mLightSwitch = Settings.Global.getInt(this.mContext.getContentResolver(), "light_smart_fiveg", 1);
        log("getLightSmartfivegSwitch setting is  " + mLightSwitch);
        if (mLightSwitch == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void actionWhenLightSwitchChange() {
        boolean mBackupIsLightSwitchOn = this.isLightSwitchOn;
        this.isLightSwitchOn = getLightSmartfivegSwitch();
        if (mBackupIsLightSwitchOn == this.isLightSwitchOn) {
            log("lightsmart5g switch is same,return ");
            return;
        }
        informWorkState(isWorkingState());
        apkSwitchObserverRegDeterminnation(isWorkingState(), this.isNrEnabled && (this.isSwitchOn || mBackupIsLightSwitchOn));
        reportScenesKeyLogInfo(this.isSwitchOn, mBackupIsLightSwitchOn);
        if (this.isSwitchOn || this.mNeedForceDisableEndcMsk != 0) {
            log("smart5g switch is on " + this.isSwitchOn + "or forcedisablemask " + this.mNeedForceDisableEndcMsk);
        } else if (this.isLightSwitchOn) {
            startDataStall();
        } else {
            stopDisableEndcAlarmTimer();
            enableEndc(this.mDefaultDataSlotId, true, 1);
            this.mLastState = true;
            stopDataStall();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void actionWhenDdsOrSwitchChange() {
        boolean mBackupIsSwitchOn = this.isSwitchOn;
        this.isSwitchOn = getDdsSmartfivegSwitch();
        informWorkState(isWorkingState());
        if (mBackupIsSwitchOn != this.isSwitchOn) {
            apkSwitchObserverRegDeterminnation(isWorkingState(), this.isNrEnabled && (mBackupIsSwitchOn || this.isLightSwitchOn));
            if (this.mNeedForceDisableEndcMsk != 0) {
                if (this.isSwitchOn) {
                    startDataStall();
                } else {
                    stopDisableEndcAlarmTimer();
                    enableEndc(this.mDefaultDataSlotId, true, 1);
                    this.mLastState = true;
                    stopDataStall();
                    if (this.isLightSwitchOn) {
                        startDataStall();
                    }
                }
            }
            reportEndcPwrKeylog(mBackupIsSwitchOn, 1, this.isNrBearAlloc);
            reportScenesKeyLogInfo(mBackupIsSwitchOn, this.isLightSwitchOn);
        }
    }

    private void registerForSwitchState() {
        log("registerForSwitchState");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("smart_fiveg"), false, this.mSwitchObserver);
    }

    private void registerForParaSettings() {
        log("registerForParaSettings");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("EndcLowPwrBasicCfgPara"), false, this.mParaObserver);
    }

    private void registerForLightSwitchState() {
        log("registerForLightSwitchState");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("light_smart_fiveg"), false, this.mLightSwitchObserver);
    }

    public void informWorkState(boolean currentState) {
        Phone phone = this.mPhone;
        if (phone != null) {
            int mPhoneId = phone.getPhoneId();
            log("informWorkState currentState " + currentState + " phoneId " + mPhoneId);
            Intent intent = new Intent(ACTION_ENDC_PWR_WORK_STATE_UPDATE);
            intent.putExtra("phoneId", mPhoneId);
            intent.putExtra("state", currentState);
            this.mPhone.getContext().sendBroadcast(intent);
        }
    }

    public void on5gStatus(boolean enableStatus) {
        logM("on5gStatus enableStatus: " + enableStatus + ", isNrEnabled: " + this.isNrEnabled + ", isSwitchOn: " + this.isSwitchOn);
        if (this.isNrEnabled != enableStatus) {
            this.isNrEnabled = enableStatus;
            informWorkState(isWorkingState());
            if (!this.isSwitchOn && !this.isLightSwitchOn) {
                return;
            }
            if (this.isNrEnabled) {
                startDataStall();
            } else {
                stopDataStall();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void update5gState(int networkType) {
        boolean isEnabled = false;
        switch (networkType) {
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case IWorldPhone.EVENT_REG_SUSPENDED_2 /* 31 */:
            case 32:
            case 33:
                isEnabled = true;
                break;
        }
        on5gStatus(isEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerPrefNetworkModeObserver() {
        int subId = this.mPhone.getSubId();
        unregisterPrefNetworkModeObserver();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            contentResolver.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + subId), true, this.mPrefNetworkModeObserver);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterPrefNetworkModeObserver() {
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mPrefNetworkModeObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getNetworkModeFromDB() {
        int networkMode = Phone.PREFERRED_NT_MODE;
        int subId = this.mPhone.getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            return Settings.Global.getInt(contentResolver, "preferred_network_mode" + subId, Phone.PREFERRED_NT_MODE);
        }
        try {
            return TelephonyManager.getIntAtIndex(this.mPhone.getContext().getContentResolver(), "preferred_network_mode", this.mPhone.getPhoneId());
        } catch (Exception e) {
            log("getNetworkModeFromDB error: " + e);
            return networkMode;
        }
    }

    private void resetAvgSampleSpeedKeyLogInfo() {
        for (int rowId = 0; rowId < 2; rowId++) {
            for (int columId = 0; columId <= 4; columId++) {
                log("AvgSampleSpeedKeyLogInfo [" + rowId + "][" + columId + "]=" + this.mAgSpeedLevCnt[rowId][columId]);
                this.mAgSpeedLevCnt[rowId][columId] = 0;
            }
        }
    }

    private void updateAvgSampleSpeedKeylogInfo(int inputSpeed) {
        int destIndex = 0;
        if (this.mIsMobileDataUsed) {
            if (this.mStartFlowIsNR) {
                destIndex = 1;
            }
            if (inputSpeed > 2048) {
                int[] iArr = this.mAgSpeedLevCnt[destIndex];
                iArr[4] = iArr[4] + 1;
            } else if (inputSpeed > AVG_SAMPLE_SPEED_LEV3) {
                int[] iArr2 = this.mAgSpeedLevCnt[destIndex];
                iArr2[3] = iArr2[3] + 1;
            } else if (inputSpeed > 1024) {
                int[] iArr3 = this.mAgSpeedLevCnt[destIndex];
                iArr3[2] = iArr3[2] + 1;
            } else if (inputSpeed > 500) {
                int[] iArr4 = this.mAgSpeedLevCnt[destIndex];
                iArr4[1] = iArr4[1] + 1;
            } else {
                int[] iArr5 = this.mAgSpeedLevCnt[destIndex];
                iArr5[0] = iArr5[0] + 1;
            }
        }
    }

    public boolean checkConnectedByType(int connection_type) {
        NetworkInfo netInfo;
        if (connection_type <= -1 || connection_type >= 28 || (netInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(connection_type)) == null || !netInfo.isConnected()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMobileDataUsed(boolean wifiConnted, boolean mobileDataOn) {
        if (!mobileDataOn || wifiConnted) {
            this.mIsMobileDataUsed = false;
        } else {
            this.mIsMobileDataUsed = true;
        }
        logM("updateMobileDataUsed wifiConnted is " + wifiConnted + " mobileDataSwitch is " + mobileDataOn);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void actionWhenMobileDataSwitchChange() {
        log("actionWhenMobileDataSwitchChange");
        int enabled = 0;
        Phone phone = this.mPhone;
        if (phone != null) {
            boolean z = true;
            try {
                int subId = phone.getSubId();
                if (TelephonyManager.getDefault().getPhoneCount() <= 1 || !SubscriptionManager.isValidSubscriptionId(subId)) {
                    enabled = Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data");
                } else {
                    ContentResolver contentResolver = this.mContext.getContentResolver();
                    enabled = Settings.Global.getInt(contentResolver, "mobile_data" + subId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (enabled == 0) {
                z = false;
            }
            this.mIsMobileDataSwitchOn = z;
            updateMobileDataUsed(this.mIsWifiConnected, this.mIsMobileDataSwitchOn);
        }
    }

    private Uri setUpDataUri(int subId) {
        Uri dataUri;
        if (TelephonyManager.getDefault().getPhoneCount() <= 1 || !SubscriptionManager.isValidSubscriptionId(subId)) {
            dataUri = Settings.Global.getUriFor("mobile_data");
        } else {
            dataUri = Settings.Global.getUriFor("mobile_data" + subId);
        }
        log("setUpDataUri():dataUri:" + dataUri.toString());
        return dataUri;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerForMobileDataSwitchState() {
        log("registerForMobileDataSwitchState");
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.getDataEnabledSettings().registerForDataEnabledChanged(this.mHandler, (int) EVENT_DATA_ENABLED_CHANGED, (Object) null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterForMobileDataSwitchState() {
        log("unregisterForMobileDataSwitchState");
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.getDataEnabledSettings().unregisterForDataEnabledChanged(this.mHandler);
            this.mHandler.removeMessages(1011);
        }
    }

    private void registerForLightOptParaSettings() {
        log("registerForLightOptParaSettings");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("EndcLowPwrLightPara"), false, this.mLightOptParaObserver);
    }

    private void startDataStall() {
        log("startDataStall: screen on:" + this.isScreenOn + ",mScreenoffOnlyEnable:" + this.mScreenoffOnlyEnable + ",EndcPwrOptProhibited:" + isEndcPwrOptProhibited());
        if (needStatsSpeed() && !isEndcPwrOptProhibited()) {
            this.mLastDataStallTime = SystemClock.elapsedRealtime();
            this.mOldTxBytes = TrafficStats.getMobileTxBytes();
            this.mOldRxBytes = TrafficStats.getMobileRxBytes();
            if (this.isNrBearAlloc) {
                this.mStartFlowIsNR = true;
            } else {
                this.mStartFlowIsNR = false;
            }
            this.mHandler.removeMessages(1001);
            stopDisableEndcAlarmTimer();
            this.mHandler.sendEmptyMessageDelayed(1001, FIRST_FLOW_CHANGE_STATS_DURATION_MS);
        }
    }

    private void stopDataStall() {
        log("stopDataStall");
        this.mHandler.removeMessages(1001);
        stopEnableEndcHysTimer();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void query5gState(int slotId) {
        log("query5gState: begin");
        boolean z = false;
        if ((524288 & this.mPhone.getRadioAccessFamily()) > 0) {
            if (this.isSwitchOn || this.isLightSwitchOn) {
                z = true;
            }
            apkSwitchObserverRegDeterminnation(z, isWorkingState());
            this.isNrEnabled = true;
        } else {
            apkSwitchObserverRegDeterminnation(false, isWorkingState());
            this.isNrEnabled = false;
        }
        logM("query5gState: end " + this.isNrEnabled);
        if (!this.isSwitchOn && !this.isLightSwitchOn) {
            return;
        }
        if (this.isNrEnabled) {
            startDataStall();
        } else {
            stopDataStall();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void queryEndcState(int slotId) {
        log("queryEndcState: begin");
        try {
            String[] cmdQueryEndcDeactStateStr = {"AT+EGMC=0,\"endc_deactivation\"", "+EGMC:"};
            if (this.mPhone != null) {
                this.mPhone.invokeOemRilRequestStrings(cmdQueryEndcDeactStateStr, this.mHandler.obtainMessage(1006));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableEndc(int slotId, boolean enabled, int causeId) {
        String atSetEndcDeactState;
        log("enableEndc: begin enabled = " + enabled);
        if ((enabled ? 0 : 1) == 0) {
            atSetEndcDeactState = "AT+EGMC=1,\"endc_deactivation\",0,1";
        } else {
            atSetEndcDeactState = "AT+EGMC=1,\"endc_deactivation\",1,1";
        }
        try {
            String[] cmdSetEndcDeactStateStr = {atSetEndcDeactState, ""};
            if (this.mPhone != null) {
                this.mPhone.invokeOemRilRequestStrings(cmdSetEndcDeactStateStr, (Message) null);
            }
            if (enabled) {
                logM("enableEndc: enabled = " + enabled + " enable cause = " + causeId);
                int[] iArr = this.mEnableEndcCausetCnt;
                iArr[causeId] = iArr[causeId] + 1;
            } else {
                logM("enableEndc: enabled = " + enabled + " disable cause = " + causeId);
                int[] iArr2 = this.mDisableEndcCausetCnt;
                iArr2[causeId] = iArr2[causeId] + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (enabled) {
            this.mEnableEndcCnt++;
            startEnableEndcHysTimer();
        } else {
            this.mDisableEndcCnt++;
            stopEnableEndcHysTimer();
        }
        if (this.mLastState != enabled) {
            long mCurrentTime = SystemClock.elapsedRealtime() / mSecondMs;
            long j = this.mLastEndcSettingChangeTime;
            if (mCurrentTime - j > 0) {
                if (!this.mLastState) {
                    this.mDisableEndcSettingTime += mCurrentTime - j;
                } else {
                    this.mEnableEndcSettingTime += mCurrentTime - j;
                }
            }
            this.mLastEndcSettingChangeTime = mCurrentTime;
        }
    }

    public void updateAnyNrBearerAllocationStatus(int slotId, boolean enStatus) {
        logM("updateAnyNrBearerAllocationStatus: begin enStatus is " + enStatus);
        if (this.mDefaultDataSlotId == slotId && this.isNrBearAlloc != enStatus) {
            if (true == enStatus) {
                this.mHandler.removeMessages(1010);
                this.isNrBearAlloc = true;
                updateNrBearKeyInfo(this.isNrBearAlloc);
                this.mCurrentDisableEndcTimerLength = 0;
                this.mIsDisableEndcTimerOut = false;
            } else if (this.isScreenOn) {
                this.mHandler.removeMessages(1010);
                this.mHandler.sendEmptyMessageDelayed(1010, this.NR_BEAR_CHECK_DEALLOC_MS);
            } else {
                this.mHandler.removeMessages(1010);
                actionConfirmedNrBearDeAlloc();
            }
            boolean currentEndcBearStatus = getEndcBearStatus(this.mCurrentRat, this.isNrBearAlloc);
            boolean z = this.mLastEndcBearStatus;
            if (currentEndcBearStatus != z) {
                updateEndcBearInfo(z);
            }
            this.mLastEndcBearStatus = currentEndcBearStatus;
        }
    }

    public void updateSib2UpLayerInd(int slotId, boolean enStatus) {
        logM("updateSib2UpLayerInd: begin enStatus is " + enStatus);
        if (this.mDefaultDataSlotId == slotId && this.mSib2UpLayerInd != enStatus) {
            this.mSib2UpLayerInd = enStatus;
            if (!this.mSib2NoNrEnable) {
                actionWhenStateChange();
            }
        }
    }

    private boolean isSpeicSimNeedCloseSmartFiveG() {
        Phone phone = this.mPhone;
        if (phone == null || !phone.is_test_card()) {
            return false;
        }
        return true;
    }

    public void dispose() {
        this.mHandler.removeCallbacksAndMessages(null);
        this.mHandler = null;
        this.mEndcThread.stop();
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            switch (msg.what) {
                case EndcBearController.EVENT_CMD_CONNECT_EXT_TELEPHONY_SERV /* 1000 */:
                    EndcBearController.this.log("Event received EVENT_CMD_CONNECT_EXT_TELEPHONY_SERV");
                    return;
                case 1001:
                    EndcBearController.this.log("EVENT_FLOW_CHANGE");
                    EndcBearController.this.flowChange();
                    return;
                case ExternalSimConstants.MSG_ID_GET_PLATFORM_CAPABILITY_RESPONSE /* 1002 */:
                case ExternalSimConstants.MSG_ID_UICC_APDU_REQUEST /* 1005 */:
                default:
                    return;
                case 1003:
                    EndcBearController.this.log("EVENT_RADIO_ON");
                    EndcBearController.this.mHandler.removeMessages(1004);
                    EndcBearController.this.mHandler.sendEmptyMessage(1004);
                    if (EndcBearController.this.isCmccEval) {
                        EndcBearController.this.log("receive EVENT_RADIO_ON isCmccEval is   " + EndcBearController.this.isCmccEval);
                        EndcBearController endcBearController = EndcBearController.this;
                        endcBearController.enableEndc(endcBearController.mDefaultDataSlotId, true, 5);
                        EndcBearController.this.mLastState = true;
                        EndcBearController.this.isFirstEnableEndc = true;
                        EndcBearController.this.startEnableEndcHysTimer();
                        return;
                    }
                    return;
                case 1004:
                    EndcBearController.this.log("EVENT_QUERY_STATE");
                    EndcBearController endcBearController2 = EndcBearController.this;
                    endcBearController2.query5gState(endcBearController2.mDefaultDataSlotId);
                    EndcBearController endcBearController3 = EndcBearController.this;
                    endcBearController3.queryEndcState(endcBearController3.mDefaultDataSlotId);
                    return;
                case 1006:
                    EndcBearController.this.log("EVENT_GET_DEACT_SCG_CONFIG");
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        String[] recData = (String[]) ar.result;
                        if (recData == null) {
                            EndcBearController.this.log("Received data is null!");
                            return;
                        } else if (recData.length > 0) {
                            EndcBearController.this.log("receiveDate[0] = " + recData[0]);
                            int mScgDeactState = -1;
                            try {
                                String[] mGetVal = recData[0].split(",");
                                for (int mSubStrIndx = 0; mSubStrIndx < mGetVal.length; mSubStrIndx++) {
                                    EndcBearController.this.log("SubStringIndex= " + mSubStrIndx + "SunString is: " + mGetVal[mSubStrIndx]);
                                }
                                if (mGetVal.length >= 2 && mGetVal[1] != null && !mGetVal[1].equals("")) {
                                    mScgDeactState = Integer.parseInt(mGetVal[1].trim());
                                    EndcBearController.this.logM("mScgDeactState = " + mScgDeactState);
                                }
                            } catch (Exception e) {
                                EndcBearController.this.log("Exception happen!");
                            }
                            if (mScgDeactState == 0 || mScgDeactState == 1) {
                                EndcBearController endcBearController4 = EndcBearController.this;
                                if (mScgDeactState != 1) {
                                    z = true;
                                }
                                endcBearController4.mLastState = z;
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    } else {
                        EndcBearController.this.log("Send AT cmd failed.!");
                        return;
                    }
                case 1007:
                    boolean isAnySceneNeedRestrictBack = EndcBearController.this.mIsAnySceneNeedRestrict;
                    AsyncResult asyncResult = (AsyncResult) msg.obj;
                    int updateSceneType = msg.arg1;
                    int updateSceneAction = msg.arg2;
                    EndcBearController.this.log("EVENT_SCENE_STATE_CHANGED mUpdateSceneType " + updateSceneType + " mUpdateSceneAction " + updateSceneAction);
                    EndcBearController.this.updateSceneStates(updateSceneType, updateSceneAction);
                    EndcBearController.this.log("EVENT_SCENE_STATE_CHANGED old restrict " + isAnySceneNeedRestrictBack + " new restrict " + EndcBearController.this.mIsAnySceneNeedRestrict);
                    return;
                case 1008:
                    int mCurrentNwMode = ((Integer) msg.obj).intValue();
                    EndcBearController.this.log("EVENT_NETWORK_MODE_CHANGED mode is " + mCurrentNwMode);
                    EndcBearController.this.mNetworkMode = mCurrentNwMode;
                    EndcBearController endcBearController5 = EndcBearController.this;
                    endcBearController5.update5gState(endcBearController5.mNetworkMode);
                    return;
                case 1009:
                    EndcBearController.this.mNeedForceDisableEndcMsk &= -2;
                    EndcBearController.this.log("EVENT_FORCE_DISABLE_ENDC_TIME_OUT");
                    EndcBearController.this.actionWhenStateChange();
                    return;
                case 1010:
                    EndcBearController.this.log("EVENT_CHECK_NR_BEAR_DEALLOC isNrBearAlloc is " + EndcBearController.this.isNrBearAlloc);
                    EndcBearController.this.actionConfirmedNrBearDeAlloc();
                    return;
                case 1011:
                    if (((AsyncResult) msg.obj).exception == null && EndcBearController.this.mPhone != null) {
                        try {
                            boolean last5gIcon = EndcBearController.this.is5gIcon;
                            if (EndcBearController.this.mPhone.getServiceState().getNrState() >= 2) {
                                EndcBearController.this.is5gIcon = true;
                            } else {
                                EndcBearController.this.is5gIcon = false;
                            }
                            if (EndcBearController.this.is5gIcon != last5gIcon) {
                                EndcBearController.this.log(" old 5gIcon is " + last5gIcon + " new5gIcon is " + EndcBearController.this.is5gIcon);
                                EndcBearController.this.actionWhenStateChange();
                                return;
                            }
                            return;
                        } catch (Exception e2) {
                            EndcBearController.this.log("get ss nr state error: " + e2);
                            return;
                        }
                    } else {
                        return;
                    }
                case 1012:
                    EndcBearController.this.log("EVENT_LTE_JAM_TIME_EXPIRED ");
                    EndcBearController endcBearController6 = EndcBearController.this;
                    endcBearController6.updateLteJamDurationTime(endcBearController6.mLteJam, false);
                    EndcBearController.this.mLteJam = false;
                    return;
                case 1013:
                    EndcBearController.this.log("EVENT_ENABLE_ENDC_HYSTERESIS_EXP ");
                    return;
                case 1014:
                    if (((EndcBearController) EndcBearController.this).mCurrentApks.isEmpty()) {
                        EndcBearController.this.mIsAnyApkRunning = false;
                    } else {
                        EndcBearController.this.mIsAnyApkRunning = true;
                    }
                    if (EndcBearController.this.mCurrentBlackApks.isEmpty()) {
                        EndcBearController.this.mIsAnyBlackApkRunning = false;
                    } else {
                        EndcBearController.this.mIsAnyBlackApkRunning = true;
                    }
                    EndcBearController.this.log("EVENT_MONITOR_APK_STATE_CHANGED mIsAnyApkRunning " + EndcBearController.this.mIsAnyApkRunning + ", mIsAnyBlackApkRunning " + EndcBearController.this.mIsAnyBlackApkRunning);
                    return;
                case EndcBearController.EVENT_DATA_ENABLED_CHANGED /* 1015 */:
                    EndcBearController.this.log("EVENT_DATA_ENABLED_CHANGED ");
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.result instanceof Pair) {
                        EndcBearController.this.mIsMobileDataSwitchOn = ((Boolean) ((Pair) ar2.result).first).booleanValue();
                        EndcBearController endcBearController7 = EndcBearController.this;
                        endcBearController7.updateMobileDataUsed(endcBearController7.mIsWifiConnected, EndcBearController.this.mIsMobileDataSwitchOn);
                        return;
                    }
                    return;
            }
        }
    }

    private int getStatsSpeedThreshold() {
        int mSpeedThres = 0;
        if (this.isSwitchOn) {
            if (this.isCmccEval && this.mEndcBlackApkRunning) {
                int mSpeedThres2 = this.mScgSpeedForEndcBlackApk;
                log("mEndcBlackApkRunning is true SpeedThreshold is " + mSpeedThres2);
                return mSpeedThres2;
            } else if (this.mStartFlowIsNR) {
                mSpeedThres = this.mScgFailStatsAvgSpeedThres;
            } else {
                boolean isLowbatMode = this.mLowBatHeavyOptEnable && this.mCurrBatteryLevel < this.mBatThresAsLow;
                if (this.mIsAnyBlackApkRunning) {
                    mSpeedThres = 0;
                } else if (this.mIsAnyApkRunning) {
                    mSpeedThres = true == isLowbatMode ? this.mScgAddSpeedLowbatForApk : this.mScgAddSpeedForApk;
                } else if (this.mIsAnySceneNeedRestrict) {
                    mSpeedThres = true == isLowbatMode ? this.mScgAddSpeedLowbatForScene : this.mScgAddSpeedForScene;
                } else {
                    mSpeedThres = true == isLowbatMode ? this.mLowBatStatsAvgSpeedThres : this.mScgAddStatsAvgSpeedThres;
                    if (this.mIsDisableEndcTimerOut && this.DISABLE_ENDC_DELAY_MS == this.mCurrentDisableEndcTimerLength) {
                        mSpeedThres = this.mPunishScgAddStatsAvgSpeedThres;
                    }
                }
            }
        } else if (this.isLightSwitchOn && this.mLightOptLowBatEnable && this.mCurrBatteryLevel < this.mLightOptBatThresAsLow) {
            mSpeedThres = this.mLightOptLowBatSpeedThres;
            if (this.mIsAnyBlackApkRunning) {
                mSpeedThres = 0;
            } else if (this.mIsAnyApkRunning) {
                int i = this.mScgAddSpeedLowbatForApk;
                int i2 = this.mLightOptLowBatSpeedThres;
                if (i >= i2) {
                    i = i2;
                }
                mSpeedThres = i;
            } else if (this.mIsAnySceneNeedRestrict) {
                int i3 = this.mScgAddSpeedLowbatForScene;
                int i4 = this.mLightOptLowBatSpeedThres;
                if (i3 >= i4) {
                    i3 = i4;
                }
                mSpeedThres = i3;
            }
        }
        log("SpeedThreshold is " + mSpeedThres);
        return mSpeedThres;
    }

    private boolean needStatsSpeed() {
        boolean mNeedFlg = false;
        if (this.mNeedForceDisableEndcMsk != 0) {
            log("is forceDisable needStatsSpeed is false");
            return false;
        }
        if (this.isScreenOn && this.isNrEnabled) {
            if (this.isSwitchOn) {
                mNeedFlg = true;
                if (this.mScreenoffOnlyEnable) {
                    mNeedFlg = false;
                }
            } else if (this.isLightSwitchOn) {
                mNeedFlg = true;
            }
        }
        log("needStatsSpeed is " + mNeedFlg);
        return mNeedFlg;
    }

    /* JADX INFO: Multiple debug info for r9v1 long: [D('currentSpeedThres' int), D('dlIncrementPs' long)] */
    /* JADX INFO: Multiple debug info for r7v3 long: [D('ulIncrementPs' long), D('incrementRxBytes' long)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void flowChange() {
        boolean mDisableEndcLater;
        int setCause;
        long totalTxBytes = TrafficStats.getMobileTxBytes();
        long totalRxBytes = TrafficStats.getMobileRxBytes();
        long incrementTxBytes = totalTxBytes - this.mOldTxBytes;
        long incrementRxBytes = totalRxBytes - this.mOldRxBytes;
        this.mLastincrementPs = Long.MAX_VALUE;
        log("flowChange mOldTxBytes: " + this.mOldTxBytes + ", totalTxBytes: " + totalTxBytes + ", mOldRxBytes: " + this.mOldRxBytes + ", totalRxBytes: " + totalRxBytes);
        long usedTime = (SystemClock.elapsedRealtime() - this.mLastDataStallTime) / mSecondMs;
        if (usedTime > 0) {
            mDisableEndcLater = false;
            long dlIncrementPs = incrementRxBytes / (usedTime * STANDARD_VALUE);
            long incrementRxBytes2 = incrementTxBytes / (STANDARD_VALUE * usedTime);
            log("dlIncrementPs = " + dlIncrementPs + "KB,ulIncrementPs = " + incrementRxBytes2 + "KB, mLastState = " + this.mLastState);
            long incrementPs = dlIncrementPs > incrementRxBytes2 ? dlIncrementPs : incrementRxBytes2;
            this.mLastincrementPs = incrementPs;
            updateAvgSampleSpeedKeylogInfo((int) incrementPs);
            int currentSpeedThres = getStatsSpeedThreshold();
            if (this.mStartFlowIsNR) {
                if (incrementPs >= ((long) this.mRewardDisEndcTimerStatsAvgSpeedThres)) {
                    this.mNeedRewardDisEndcTimer = true;
                }
            }
            updateLteQuality(this.mLastincrementPs);
            boolean lteDataJam = isLteDataJam();
            log("flowChange mStartFlowIsNR is " + this.mStartFlowIsNR + " mNeedRewardDisEndcTimer is " + this.mNeedRewardDisEndcTimer + "lteDataJam is " + lteDataJam);
            if (currentSpeedThres == 0 || incrementPs > ((long) currentSpeedThres) || this.isLtePoor || lteDataJam) {
                stopDisableEndcAlarmTimer();
                if (!this.mLastState) {
                    if (currentSpeedThres == 0) {
                        setCause = 4;
                    } else if (incrementPs > ((long) currentSpeedThres)) {
                        setCause = 0;
                    } else if (this.isLtePoor) {
                        setCause = 2;
                    } else {
                        setCause = 3;
                    }
                    enableEndc(this.mDefaultDataSlotId, true, setCause);
                    this.mLastState = true;
                }
            } else if (!isEnableEndcHysTimerRun() && this.mLastState) {
                if (!this.isNrBearAlloc) {
                    stopDisableEndcAlarmTimer();
                    enableEndc(this.mDefaultDataSlotId, false, 1);
                    this.mLastState = false;
                } else {
                    mDisableEndcLater = true;
                }
            }
        } else {
            mDisableEndcLater = false;
        }
        this.mOldTxBytes = totalTxBytes;
        this.mOldRxBytes = totalRxBytes;
        this.mLastDataStallTime = SystemClock.elapsedRealtime();
        if (needStatsSpeed()) {
            if (this.isNrBearAlloc) {
                this.mStartFlowIsNR = true;
            } else {
                this.mStartFlowIsNR = false;
            }
            this.mHandler.removeMessages(1001);
            this.mHandler.sendEmptyMessageDelayed(1001, this.FLOW_CHANGE_STATS_DURATION_MS);
            if (mDisableEndcLater && this.mDisEdncTimerAlarmIntent == null) {
                log("flowChange start disbaleEndc timer");
                startDisableEndcAlarmTimer(getDisableEndcTimerLength());
            }
        }
    }

    public boolean isWorkingState() {
        return this.isNrEnabled && (this.isSwitchOn || this.isLightSwitchOn);
    }

    private boolean isEndcPwrOptProhibited() {
        return !isWorkingState() || (!this.mChargingEnable && this.mIsCharging) || ((!this.mHotspotEnable && this.mIsWifihotspotOn) || ((!this.mSib2NoNrEnable && !this.mSib2UpLayerInd && this.isScreenOn && !this.is5gIcon) || isSpeicSimNeedCloseSmartFiveG() || (this.isScreenOn && this.isSwitchOn && this.mScreenoffOnlyEnable)));
    }

    private boolean isDeviceCharging() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return ((BatteryManager) phone.getContext().getSystemService("batterymanager")).isCharging();
        }
        log("Phone is null ");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void actionWhenStateChange() {
        log("actionWhenStateChange mNeedForceDisableEndcMsk: " + this.mNeedForceDisableEndcMsk + ", mIsCharging: " + this.mIsCharging + ", mIsWifihotspotOn: " + this.mIsWifihotspotOn + ", mSib2UpLayerInd: " + this.mSib2UpLayerInd + ", isScreenOn: " + this.isScreenOn + ", isNrBearAlloc: " + this.isNrBearAlloc);
        if (this.mNeedForceDisableEndcMsk == 0) {
            if (isEndcPwrOptProhibited()) {
                stopDisableEndcAlarmTimer();
                stopDataStall();
                if (!this.mLastState) {
                    enableEndc(this.mDefaultDataSlotId, true, 4);
                    this.mLastState = true;
                }
            } else if (!this.isScreenOn) {
                stopDataStall();
                if (isWorkingState() && (this.isSwitchOn || (this.isLightSwitchOn && this.mLightOptScreenOffEnable && this.mLastincrementPs <= ((long) this.mLightOptScreenOffSpeedThres)))) {
                    if (!this.isNrBearAlloc) {
                        stopDisableEndcAlarmTimer();
                        enableEndc(this.mDefaultDataSlotId, false, 3);
                        this.mLastState = false;
                    } else if (this.mDisEdncTimerAlarmIntent == null) {
                        log("actionWhenStateChange start disbaleEndc timer");
                        startDisableEndcAlarmTimer(getDisableEndcTimerLength());
                    }
                }
            } else if (isWorkingState()) {
                if (!this.isSwitchOn) {
                    stopDisableEndcAlarmTimer();
                    if (!this.mLastState) {
                        enableEndc(this.mDefaultDataSlotId, true, 1);
                        this.mLastState = true;
                    }
                }
                if (!this.mHandler.hasMessages(1001)) {
                    startDataStall();
                }
            }
            if (this.isScreenOn && (SystemClock.elapsedRealtime() / mSecondMs) - this.mLastKeylogRptTime >= ENDC_PWR_OPT_KEY_LOG_RPT_PERIOD_SEC) {
                reportEndcPwrKeylog(this.isSwitchOn, 0, this.isNrBearAlloc);
            }
            if (this.isScreenOn && this.mScenesStatusMsks == 0 && (SystemClock.elapsedRealtime() / mSecondMs) - this.mLastScenesKeylogRptTime >= ENDC_PWR_OPT_KEY_LOG_RPT_PERIOD_SEC) {
                reportScenesKeyLogInfo(this.isSwitchOn, this.isLightSwitchOn);
            }
            if (this.isScreenOn && this.mThermalActionMsk == 0 && (SystemClock.elapsedRealtime() / mSecondMs) - this.mLastThermalKeylogRptTime >= ENDC_PWR_OPT_KEY_LOG_RPT_PERIOD_SEC) {
                reportThermalKeylog();
            }
        } else if (this.mLastState) {
            stopDisableEndcAlarmTimer();
            stopDataStall();
            enableEndc(this.mDefaultDataSlotId, false, 2);
            this.mLastState = false;
        }
    }

    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int i = str.length();
        do {
            i--;
            if (i < 0) {
                if (str.isEmpty()) {
                    return false;
                }
                return true;
            }
        } while (Character.isDigit(str.charAt(i)));
        return false;
    }

    private void initParaInSettings() {
        int mDisEndcTimerInDb;
        int mRewardDisEndcTimerInDb;
        long j = this.DISABLE_ENDC_DELAY_MS;
        if (j == 86400000) {
            mDisEndcTimerInDb = 0;
        } else {
            mDisEndcTimerInDb = (int) (j / mSecondMs);
        }
        long j2 = this.REWARD_DISABLE_ENDC_DELAY_MS;
        if (j2 == 86400000) {
            mRewardDisEndcTimerInDb = 0;
        } else {
            mRewardDisEndcTimerInDb = (int) (j2 / mSecondMs);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ver_num=");
        sb.append(this.mEndcLowPwrVersionN);
        sb.append(";stats_duration=");
        sb.append(this.FLOW_CHANGE_STATS_DURATION_MS / mSecondMs);
        sb.append(";scg_add_speed=");
        sb.append(this.mScgAddStatsAvgSpeedThres);
        sb.append(";scg_fail_speed=");
        sb.append(this.mScgFailStatsAvgSpeedThres);
        sb.append(";dis_endc_timer=");
        sb.append(mDisEndcTimerInDb);
        sb.append(";reward_scg_fail_speed=");
        sb.append(this.mRewardDisEndcTimerStatsAvgSpeedThres);
        sb.append(";reward_dis_endc_timer=");
        sb.append(mRewardDisEndcTimerInDb);
        sb.append(";charging_enable=");
        int i = 0;
        sb.append(this.mChargingEnable ? 1 : 0);
        sb.append(";hotspot_enable=");
        sb.append(this.mHotspotEnable ? 1 : 0);
        sb.append(";sib_no_nr_enable=");
        sb.append(this.mSib2NoNrEnable ? 1 : 0);
        sb.append(";screenoff_only_enable=");
        sb.append(this.mScreenoffOnlyEnable ? 1 : 0);
        sb.append(";lowbat_heavy_enable=");
        if (this.mLowBatHeavyOptEnable) {
            i = 1;
        }
        sb.append(i);
        sb.append(";lowbat_thres=");
        sb.append(this.mBatThresAsLow);
        sb.append(";lowbat_stats_avg_speed=");
        sb.append(this.mLowBatStatsAvgSpeedThres);
        sb.append(";poorlte_rsrp_thres=");
        sb.append(this.mPoorLteRsrpThres);
        sb.append(";poorlte_rsrq_thres=");
        sb.append(this.mPoorLteRsrqThres);
        sb.append(";poorlte_bw_thres=");
        sb.append(this.mPoorLteBwKhzThres);
        String basicCfgPara = sb.toString();
        log("Initial BasicCfgPara is " + basicCfgPara);
        Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrBasicCfgPara", basicCfgPara);
    }

    /* JADX INFO: Multiple debug info for r7v23 int: [D('mPoorLteRsrpThresBack' int), D('mLowBatAvgSpeedArray' java.lang.String[])] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateParaFromSettings(boolean isInitial) {
        int mTempPara;
        int mTempPara2;
        int mTempPara3;
        int mTempPara4;
        int mTempPara5;
        int mTempPara6;
        int mTempPara7;
        int mTempPara8;
        int mTempPara9;
        int mTempPara10;
        int mTempPara11;
        int mTempPara12;
        int mTempPara13;
        int mTempPara14;
        int mTempPara15;
        boolean mChargingEnableBackup = this.mChargingEnable;
        boolean mHotspotEnableBackup = this.mHotspotEnable;
        boolean mSib2NoNrEnableBackup = this.mSib2NoNrEnable;
        boolean mScreenoffOnlyEnableBackup = this.mScreenoffOnlyEnable;
        String basicCfgPara = Settings.Global.getString(this.mContext.getContentResolver(), "EndcLowPwrBasicCfgPara");
        Smart5gInfoLtePoorThresConfig(this.mPoorLteRsrpThres, this.mPoorLteRsrqThres, this.mPoorLteBwKhzThres);
        if (basicCfgPara != null) {
            log("updateParaFromSettings BasicCfgPara is " + basicCfgPara);
            String[] mParaArray = basicCfgPara.split(";");
            if (17 != mParaArray.length) {
                log("updateParaFromSettings length is  " + mParaArray.length);
                initParaInSettings();
            } else if (mParaArray[0].contains("ver_num")) {
                String[] mVerNumArray = mParaArray[0].split("=");
                if (2 == mVerNumArray.length && isNumeric(mVerNumArray[1])) {
                    int mTempPara16 = Integer.parseInt(mVerNumArray[1]);
                    log("updateParaFromSettings code ver is " + this.mEndcLowPwrVersionN + "setting ver is " + mTempPara16);
                    int i = this.mEndcLowPwrVersionN;
                    if (i > mTempPara16) {
                        initParaInSettings();
                        return;
                    } else if (i == mTempPara16) {
                        log("basic_cfg: same version no need update");
                        return;
                    } else {
                        this.mEndcLowPwrVersionN = mTempPara16;
                    }
                }
                if (mParaArray[1].contains("stats_duration")) {
                    String[] mDurationArray = mParaArray[1].split("=");
                    if (2 == mDurationArray.length && isNumeric(mDurationArray[1])) {
                        int mTempPara17 = Integer.parseInt(mDurationArray[1]);
                        if (mTempPara17 >= 2 && mTempPara17 <= 10) {
                            this.FLOW_CHANGE_STATS_DURATION_MS = ((long) mTempPara17) * mSecondMs;
                        }
                    }
                }
                if (mParaArray[2].contains("scg_add_speed")) {
                    String[] mAvgSpeedArray = mParaArray[2].split("=");
                    if (2 == mAvgSpeedArray.length && isNumeric(mAvgSpeedArray[1]) && (mTempPara15 = Integer.parseInt(mAvgSpeedArray[1])) >= 0 && mTempPara15 <= ENDC_PWR_CFG_STATS_AVG_SPEED_MAX) {
                        this.mScgAddStatsAvgSpeedThres = mTempPara15;
                    }
                }
                if (mParaArray[3].contains("scg_fail_speed")) {
                    String[] mAvgSpeedArray2 = mParaArray[3].split("=");
                    if (2 == mAvgSpeedArray2.length && isNumeric(mAvgSpeedArray2[1]) && (mTempPara14 = Integer.parseInt(mAvgSpeedArray2[1])) >= 0 && mTempPara14 <= ENDC_PWR_CFG_STATS_AVG_SPEED_MAX) {
                        this.mScgFailStatsAvgSpeedThres = mTempPara14;
                    }
                }
                if (mParaArray[4].contains("dis_endc_timer")) {
                    String[] mDisEndcTimerArray = mParaArray[4].split("=");
                    if (2 == mDisEndcTimerArray.length && isNumeric(mDisEndcTimerArray[1]) && (mTempPara13 = Integer.parseInt(mDisEndcTimerArray[1])) >= 0 && mTempPara13 <= ENDC_PWR_CFG_STATS_DIS_ENDC_TIMER_MAX) {
                        if (mTempPara13 == 0) {
                            this.DISABLE_ENDC_DELAY_MS = 86400000;
                        } else {
                            this.DISABLE_ENDC_DELAY_MS = ((long) mTempPara13) * mSecondMs;
                        }
                    }
                }
                if (mParaArray[5].contains("reward_scg_fail_speed")) {
                    String[] mAvgSpeedArray3 = mParaArray[5].split("=");
                    if (2 == mAvgSpeedArray3.length && isNumeric(mAvgSpeedArray3[1]) && (mTempPara12 = Integer.parseInt(mAvgSpeedArray3[1])) >= 0 && mTempPara12 <= ENDC_PWR_CFG_STATS_AVG_SPEED_MAX) {
                        this.mRewardDisEndcTimerStatsAvgSpeedThres = mTempPara12;
                    }
                }
                if (mParaArray[6].contains("reward_dis_endc_timer")) {
                    String[] mDisEndcTimerArray2 = mParaArray[6].split("=");
                    if (2 == mDisEndcTimerArray2.length && isNumeric(mDisEndcTimerArray2[1]) && (mTempPara11 = Integer.parseInt(mDisEndcTimerArray2[1])) >= 0 && mTempPara11 <= ENDC_PWR_CFG_STATS_DIS_ENDC_TIMER_MAX) {
                        if (mTempPara11 == 0) {
                            this.REWARD_DISABLE_ENDC_DELAY_MS = 86400000;
                        } else {
                            this.REWARD_DISABLE_ENDC_DELAY_MS = ((long) mTempPara11) * mSecondMs;
                        }
                    }
                }
                if (mParaArray[7].contains("charging_enable")) {
                    String[] mChargEnableArray = mParaArray[7].split("=");
                    if (2 == mChargEnableArray.length && isNumeric(mChargEnableArray[1]) && ((mTempPara10 = Integer.parseInt(mChargEnableArray[1])) == 0 || mTempPara10 == 1)) {
                        this.mChargingEnable = mTempPara10 == 1;
                    }
                }
                if (mParaArray[8].contains("hotspot_enable")) {
                    String[] mHotspotEnableArray = mParaArray[8].split("=");
                    if (2 == mHotspotEnableArray.length && isNumeric(mHotspotEnableArray[1]) && ((mTempPara9 = Integer.parseInt(mHotspotEnableArray[1])) == 0 || mTempPara9 == 1)) {
                        this.mHotspotEnable = mTempPara9 == 1;
                    }
                }
                if (mParaArray[9].contains("sib_no_nr_enable")) {
                    String[] mSibNoNrEnableArray = mParaArray[9].split("=");
                    if (2 == mSibNoNrEnableArray.length && isNumeric(mSibNoNrEnableArray[1]) && ((mTempPara8 = Integer.parseInt(mSibNoNrEnableArray[1])) == 0 || mTempPara8 == 1)) {
                        this.mSib2NoNrEnable = mTempPara8 == 1;
                    }
                }
                if (mParaArray[10].contains("screenoff_only_enable")) {
                    String[] mScreenOffEnableArray = mParaArray[10].split("=");
                    if (2 == mScreenOffEnableArray.length && isNumeric(mScreenOffEnableArray[1]) && ((mTempPara7 = Integer.parseInt(mScreenOffEnableArray[1])) == 0 || mTempPara7 == 1)) {
                        this.mScreenoffOnlyEnable = mTempPara7 == 1;
                    }
                }
                if (mParaArray[11].contains("lowbat_heavy_enable")) {
                    String[] mLowBatEnableArray = mParaArray[11].split("=");
                    if (2 == mLowBatEnableArray.length && isNumeric(mLowBatEnableArray[1]) && ((mTempPara6 = Integer.parseInt(mLowBatEnableArray[1])) == 0 || mTempPara6 == 1)) {
                        this.mLowBatHeavyOptEnable = mTempPara6 == 1;
                    }
                }
                if (mParaArray[12].contains("lowbat_thres")) {
                    String[] mLowBatThresArray = mParaArray[12].split("=");
                    if (2 == mLowBatThresArray.length && isNumeric(mLowBatThresArray[1]) && (mTempPara5 = Integer.parseInt(mLowBatThresArray[1])) <= 30) {
                        this.mBatThresAsLow = mTempPara5;
                    }
                }
                if (mParaArray[13].contains("lowbat_stats_avg_speed")) {
                    String[] mLowBatAvgSpeedArray = mParaArray[13].split("=");
                    if (2 == mLowBatAvgSpeedArray.length && isNumeric(mLowBatAvgSpeedArray[1]) && (mTempPara4 = Integer.parseInt(mLowBatAvgSpeedArray[1])) >= 0 && mTempPara4 <= ENDC_PWR_CFG_STATS_AVG_SPEED_MAX) {
                        this.mLowBatStatsAvgSpeedThres = mTempPara4;
                    }
                }
                int mPoorLteRsrpThresBack = this.mPoorLteRsrpThres;
                int mPoorLteRsrqThresBack = this.mPoorLteRsrqThres;
                int mPoorLteBwKhzThresBack = this.mPoorLteBwKhzThres;
                if (mParaArray[14].contains("poorlte_rsrp_thres")) {
                    String[] mPoorLteRsrpThresArray = mParaArray[14].split("=");
                    if (2 == mPoorLteRsrpThresArray.length && (mTempPara3 = Integer.parseInt(mPoorLteRsrpThresArray[1])) >= ENDC_PWR_CFG_POOR_LTE_RSRP_THRES_MIN && mTempPara3 <= 0) {
                        this.mPoorLteRsrpThres = mTempPara3;
                    }
                }
                if (mParaArray[15].contains("poorlte_rsrq_thres")) {
                    String[] mPoorLteRsrqThresArray = mParaArray[15].split("=");
                    if (2 == mPoorLteRsrqThresArray.length && (mTempPara2 = Integer.parseInt(mPoorLteRsrqThresArray[1])) >= -40 && mTempPara2 <= 0) {
                        this.mPoorLteRsrqThres = mTempPara2;
                    }
                }
                if (mParaArray[16].contains("poorlte_bw_thres")) {
                    String[] mPoorLteBwThresArray = mParaArray[16].split("=");
                    if (2 == mPoorLteBwThresArray.length && isNumeric(mPoorLteBwThresArray[1]) && (mTempPara = Integer.parseInt(mPoorLteBwThresArray[1])) >= 0 && mTempPara <= ENDC_PWR_CFG_POOR_LTE_BW_THRES_MAX) {
                        this.mPoorLteBwKhzThres = mTempPara;
                    }
                }
                if (!(mPoorLteRsrpThresBack == this.mPoorLteRsrpThres && mPoorLteRsrqThresBack == this.mPoorLteRsrqThres && mPoorLteBwKhzThresBack == this.mPoorLteBwKhzThres)) {
                    Smart5gInfoLtePoorThresConfig(this.mPoorLteRsrpThres, this.mPoorLteRsrqThres, this.mPoorLteBwKhzThres);
                }
                log("updateParaFromSettings FLOW_CHANGE_STATS_DURATION_MS: " + this.FLOW_CHANGE_STATS_DURATION_MS + ", mScgAddStatsAvgSpeedThres: " + this.mScgAddStatsAvgSpeedThres + ", mScgFailStatsAvgSpeedThres: " + this.mScgFailStatsAvgSpeedThres + ", DISABLE_ENDC_DELAY_MS: " + this.DISABLE_ENDC_DELAY_MS + ", reward_scg_fail_speed: " + this.mRewardDisEndcTimerStatsAvgSpeedThres + ", REWARD_DISABLE_ENDC_DELAY_MS: " + this.REWARD_DISABLE_ENDC_DELAY_MS + ", mChargingEnable: " + this.mChargingEnable + ", mHotspotEnable: " + this.mHotspotEnable + ", mSib2NoNrEnable: " + this.mSib2NoNrEnable + ", screenoff_only_enable: " + this.mScreenoffOnlyEnable + ", lowbat_heavy_enable: " + this.mLowBatHeavyOptEnable + ", lowbat_thres: " + this.mBatThresAsLow + ", lowbat_stats_avg_speed: " + this.mLowBatStatsAvgSpeedThres + ", poorlte_rsrp_thres: " + this.mPoorLteRsrpThres + ", poorlte_rsrq_thres: " + this.mPoorLteRsrqThres + ", poorlte_bw_thres: " + this.mPoorLteBwKhzThres);
                if (isInitial) {
                    return;
                }
                if (mChargingEnableBackup != this.mChargingEnable || mHotspotEnableBackup != this.mHotspotEnable || mSib2NoNrEnableBackup != this.mSib2NoNrEnable || mScreenoffOnlyEnableBackup != this.mScreenoffOnlyEnable) {
                    actionWhenStateChange();
                }
            } else {
                initParaInSettings();
            }
        } else {
            log("updateParaFromSettings no BasicCfgPara setting");
            if (isInitial) {
                initParaInSettings();
            }
        }
    }

    private void initLightParaInSettings() {
        int mDisEndcTimerInDb;
        long j = this.LIGHT_DISABLE_ENDC_DELAY_MS;
        if (j == 86400000) {
            mDisEndcTimerInDb = 0;
        } else {
            mDisEndcTimerInDb = (int) (j / mSecondMs);
        }
        String mLightEndcLowPwrPara = "ver_num=" + this.mLightEndcLowPwrVersionN + ";screenoff_speed=" + this.mLightOptScreenOffSpeedThres + ";lowbat_speed=" + this.mLightOptLowBatSpeedThres + ";lowbat_thres=" + this.mLightOptBatThresAsLow + ";dis_endc_timer=" + mDisEndcTimerInDb;
        log("Initial LightEndcLowPwrPara is " + mLightEndcLowPwrPara);
        Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrLightPara", mLightEndcLowPwrPara);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLightOptParaFromSettings(boolean isInitial) {
        int mTempPara;
        boolean mLightOptScreenOffEnableBackUp = this.mLightOptScreenOffEnable;
        String mLightEndcLowPara = Settings.Global.getString(this.mContext.getContentResolver(), "EndcLowPwrLightPara");
        if (mLightEndcLowPara != null) {
            log("updateLightOptParaFromSettings mLightEndcLowPara is " + mLightEndcLowPara);
            String[] mParaArray = mLightEndcLowPara.split(";");
            if (5 != mParaArray.length) {
                log("updateLightOptParaFromSettings length is  " + mParaArray.length);
                initLightParaInSettings();
            } else if (mParaArray[0].contains("ver_num")) {
                String[] mVerNumArray = mParaArray[0].split("=");
                if (2 == mVerNumArray.length && isNumeric(mVerNumArray[1])) {
                    int mTempPara2 = Integer.parseInt(mVerNumArray[1]);
                    int i = this.mLightEndcLowPwrVersionN;
                    if (i > mTempPara2) {
                        initLightParaInSettings();
                        return;
                    } else if (i == mTempPara2) {
                        log("light_cfg: same version no need update");
                        return;
                    } else {
                        this.mLightEndcLowPwrVersionN = mTempPara2;
                    }
                }
                if (mParaArray[1].contains("screenoff_speed")) {
                    String[] mScreenOffArray = mParaArray[1].split("=");
                    if (2 == mScreenOffArray.length && isNumeric(mScreenOffArray[1])) {
                        int mTempPara3 = Integer.parseInt(mScreenOffArray[1]);
                        if (mTempPara3 == 0) {
                            this.mLightOptScreenOffEnable = false;
                        } else {
                            this.mLightOptScreenOffEnable = true;
                        }
                        this.mLightOptScreenOffSpeedThres = mTempPara3;
                    }
                }
                if (mParaArray[2].contains("lowbat_speed")) {
                    String[] mLowBatSpeedArray = mParaArray[2].split("=");
                    if (2 == mLowBatSpeedArray.length && isNumeric(mLowBatSpeedArray[1])) {
                        int mTempPara4 = Integer.parseInt(mLowBatSpeedArray[1]);
                        if (mTempPara4 == 0) {
                            this.mLightOptLowBatEnable = false;
                        } else {
                            this.mLightOptLowBatEnable = true;
                        }
                        this.mLightOptLowBatSpeedThres = mTempPara4;
                    }
                }
                if (mParaArray[3].contains("lowbat_thres")) {
                    String[] mLowBatArray = mParaArray[3].split("=");
                    if (2 == mLowBatArray.length && isNumeric(mLowBatArray[1])) {
                        this.mLightOptBatThresAsLow = Integer.parseInt(mLowBatArray[1]);
                    }
                }
                if (mParaArray[4].contains("dis_endc_timer")) {
                    String[] mDisEndcTimerArray = mParaArray[4].split("=");
                    if (2 == mDisEndcTimerArray.length && isNumeric(mDisEndcTimerArray[1]) && (mTempPara = Integer.parseInt(mDisEndcTimerArray[1])) >= 0 && mTempPara <= ENDC_PWR_CFG_STATS_DIS_ENDC_TIMER_MAX) {
                        if (mTempPara == 0) {
                            this.LIGHT_DISABLE_ENDC_DELAY_MS = 86400000;
                        } else {
                            this.LIGHT_DISABLE_ENDC_DELAY_MS = ((long) mTempPara) * mSecondMs;
                        }
                    }
                }
                log("updateLightOptParaFromSettings screenoff_speed: " + this.mLightOptScreenOffSpeedThres + ", lowbat_speed: " + this.mLightOptLowBatSpeedThres + ", lowbat_thres: " + this.mLightOptBatThresAsLow + ", dis_endc_timer: " + this.LIGHT_DISABLE_ENDC_DELAY_MS);
                if (!isInitial && mLightOptScreenOffEnableBackUp != this.mLightOptScreenOffEnable) {
                    actionWhenStateChange();
                }
            } else {
                initLightParaInSettings();
            }
        } else {
            log("updateLightOptParaFromSettings no LightEndcLowPara setting");
            if (isInitial) {
                initLightParaInSettings();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void broadcastSmart5gKeylog(boolean reportSwitchOn, int currentBatteryLev) {
        try {
            logM("broadcastSmart5gKeylog Switch:" + reportSwitchOn + ",EndcDura:" + this.mEndcBearDuration + ",NoEndcDura:" + this.mNoEndcBearDuration + ",EnEndcTime:" + this.mEnableEndcSettingTime + ",DisEndcTime:" + this.mDisableEndcSettingTime + ",LteSpeedLev0:" + this.mAgSpeedLevCnt[0][0] + ", LteSpeedLev1:" + this.mAgSpeedLevCnt[0][1] + ",LteSpeedLev2:" + this.mAgSpeedLevCnt[0][2] + ", LteSpeedLev3:" + this.mAgSpeedLevCnt[0][3] + ",LteSpeedLev3:" + this.mAgSpeedLevCnt[0][4] + ",EnEndcSpeedHigh:" + this.mEnableEndcCausetCnt[0] + ",EnEndcSwitchOff:" + this.mEnableEndcCausetCnt[1] + ",EnEndcLtePoor:" + this.mEnableEndcCausetCnt[2] + ",EnEndcLteJam:" + this.mEnableEndcCausetCnt[3] + ",EnEndcProhibit:" + this.mEnableEndcCausetCnt[4]);
            Intent intent = new Intent(ACTION_SMART5G_KEY_INFO);
            intent.putExtra("Switch", reportSwitchOn);
            intent.putExtra("EndcDura", this.mEndcBearDuration);
            intent.putExtra("NoEndcDura", this.mNoEndcBearDuration);
            intent.putExtra("EnEndcTime", this.mEnableEndcSettingTime);
            intent.putExtra("DisEndcTime", this.mDisableEndcSettingTime);
            intent.putExtra("LteSpeedCntL0", this.mAgSpeedLevCnt[0][0]);
            intent.putExtra("LteSpeedCntL1", this.mAgSpeedLevCnt[0][1]);
            intent.putExtra("LteSpeedCntL2", this.mAgSpeedLevCnt[0][2]);
            intent.putExtra("LteSpeedCntL3", this.mAgSpeedLevCnt[0][3]);
            intent.putExtra("LteSpeedCntL4", this.mAgSpeedLevCnt[0][4]);
            intent.putExtra("EnEndcSpeedHighCnt", this.mEnableEndcCausetCnt[0]);
            intent.putExtra("EnEndcSwitchOffCnt", this.mEnableEndcCausetCnt[1]);
            intent.putExtra("EnEndcLtePoorCnt", this.mEnableEndcCausetCnt[2]);
            intent.putExtra("EnEndcLteJamCnt", this.mEnableEndcCausetCnt[3]);
            intent.putExtra("EnEndcProhibitCnt", this.mEnableEndcCausetCnt[4]);
            this.mContext.sendBroadcast(intent);
            this.mLastBrocastkeyInfoBatLev = currentBatteryLev;
        } catch (Exception e) {
            log("broadcastSmart5gKeylog error: " + e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportEndcPwrKeylog(boolean reportSwitchOn, int reportCause, boolean currentNrBearAlloc) {
        Exception e;
        if (this.mPhone != null) {
            long mCurrentTimeSec = SystemClock.elapsedRealtime() / mSecondMs;
            long mDuraTimeSec = mCurrentTimeSec - this.mLastEndcBearStatsTime;
            long mEndcSettingTimeSec = mCurrentTimeSec - this.mLastEndcSettingChangeTime;
            if (this.mLastEndcBearStatus) {
                this.mEndcBearDuration += mDuraTimeSec;
            } else {
                this.mNoEndcBearDuration += mDuraTimeSec;
            }
            if (!this.mLastState) {
                this.mDisableEndcSettingTime += mEndcSettingTimeSec;
            } else {
                this.mEnableEndcSettingTime += mEndcSettingTimeSec;
            }
            this.mLastEndcBearStatsTime = mCurrentTimeSec;
            this.mLastEndcSettingChangeTime = mCurrentTimeSec;
            try {
                String log_string = OemTelephonyUtils.getOemRes(this.mContext, "zz_oppo_critical_log_103", "");
                if (log_string.equals("")) {
                    try {
                        log("return for get log_string fail.");
                    } catch (Exception e2) {
                        e = e2;
                        log("reportEndcPwrKeylog error: " + e);
                        broadcastSmart5gKeylog(reportSwitchOn, this.mCurrBatteryLevel);
                        this.mEndcBearDuration = 0;
                        this.mNoEndcBearDuration = 0;
                        this.mDisEndcTimerOutCnt = 0;
                        int[] iArr = this.mEndcBearContinuousTime;
                        iArr[0] = 0;
                        iArr[1] = 0;
                        iArr[2] = 0;
                        this.mDisableEndcSettingTime = 0;
                        this.mEnableEndcSettingTime = 0;
                        this.mDisableEndcCnt = 0;
                        this.mEnableEndcCnt = 0;
                        this.mLteJamCnt = 0;
                        this.mLteJamDuration = 0;
                        this.mLastKeylogRptTime = mCurrentTimeSec;
                        resetAvgSampleSpeedKeyLogInfo();
                        resetEndcCauseCntInfo();
                    }
                } else {
                    String[] log_array = log_string.split(",");
                    int log_type = Integer.valueOf(log_array[0]).intValue();
                    String log_desc = log_array[1];
                    float nrRatio = 0.0f;
                    if (this.mEndcBearDuration + this.mNoEndcBearDuration > 0) {
                        try {
                            nrRatio = ((float) this.mEndcBearDuration) / ((float) (this.mEndcBearDuration + this.mNoEndcBearDuration));
                        } catch (Exception e3) {
                            e = e3;
                            log("reportEndcPwrKeylog error: " + e);
                            broadcastSmart5gKeylog(reportSwitchOn, this.mCurrBatteryLevel);
                            this.mEndcBearDuration = 0;
                            this.mNoEndcBearDuration = 0;
                            this.mDisEndcTimerOutCnt = 0;
                            int[] iArr2 = this.mEndcBearContinuousTime;
                            iArr2[0] = 0;
                            iArr2[1] = 0;
                            iArr2[2] = 0;
                            this.mDisableEndcSettingTime = 0;
                            this.mEnableEndcSettingTime = 0;
                            this.mDisableEndcCnt = 0;
                            this.mEnableEndcCnt = 0;
                            this.mLteJamCnt = 0;
                            this.mLteJamDuration = 0;
                            this.mLastKeylogRptTime = mCurrentTimeSec;
                            resetAvgSampleSpeedKeyLogInfo();
                            resetEndcCauseCntInfo();
                        }
                    }
                    String strNrRatio = String.format("%.3f", Float.valueOf(nrRatio));
                    StringBuilder sb = new StringBuilder();
                    sb.append(",Switch:");
                    sb.append(reportSwitchOn);
                    sb.append(",Cause:");
                    try {
                        sb.append(reportCause);
                        sb.append(",mEndcDura:");
                        sb.append(this.mEndcBearDuration);
                        sb.append(",NoEndcDura:");
                        sb.append(this.mNoEndcBearDuration);
                        sb.append(",NrRatio:");
                        sb.append(strNrRatio);
                        sb.append(",DisEndcTimerOutCnt:");
                        sb.append(this.mDisEndcTimerOutCnt);
                        sb.append(",EndcConTLev1:");
                        sb.append(this.mEndcBearContinuousTime[0]);
                        sb.append(",EndcConTLev2:");
                        sb.append(this.mEndcBearContinuousTime[1]);
                        sb.append(",EndcBearConTLev3:");
                        sb.append(this.mEndcBearContinuousTime[2]);
                        sb.append(",DisableEndcSettingTime:");
                        sb.append(this.mDisableEndcSettingTime);
                        sb.append(",EnableEndcSettingTime:");
                        sb.append(this.mEnableEndcSettingTime);
                        sb.append(",LteSpeedLev:");
                        sb.append(this.mAgSpeedLevCnt[0][0]);
                        sb.append(":");
                        sb.append(this.mAgSpeedLevCnt[0][1]);
                        sb.append(":");
                        sb.append(this.mAgSpeedLevCnt[0][2]);
                        sb.append(":");
                        sb.append(this.mAgSpeedLevCnt[0][3]);
                        sb.append(":");
                        sb.append(this.mAgSpeedLevCnt[0][4]);
                        sb.append(",EndcSpeedLev:");
                        sb.append(this.mAgSpeedLevCnt[1][0]);
                        sb.append(":");
                        sb.append(this.mAgSpeedLevCnt[1][1]);
                        sb.append(":");
                        sb.append(this.mAgSpeedLevCnt[1][2]);
                        sb.append(":");
                        sb.append(this.mAgSpeedLevCnt[1][3]);
                        sb.append(":");
                        sb.append(this.mAgSpeedLevCnt[1][4]);
                        sb.append(",DisableEndcCnt:");
                        sb.append(this.mDisableEndcCnt);
                        sb.append(",EnableEndcCnt:");
                        sb.append(this.mEnableEndcCnt);
                        sb.append(",LteJamCnt:");
                        sb.append(this.mLteJamCnt);
                        sb.append(",LteJamDuration:");
                        sb.append(this.mLteJamDuration);
                        OppoManager.writeLogToPartition(log_type, sb.toString(), "NETWORK", ISSUE_SYS_OEM_NW_DIAG_CAUSE_ENDC_PWR_OPT, log_desc);
                        logM("after reportEndcPwrKeylog mLastEndcBearStatsTime =  " + this.mLastEndcBearStatsTime + ",mEndcBearDuration =  " + this.mEndcBearDuration + ",mNoEndcBearDuration =  " + this.mNoEndcBearDuration + ",reportSwitchOn =  " + reportSwitchOn + ",mDisEndcTimerOutCnt =  " + this.mDisEndcTimerOutCnt + ", mEndcBearConTLev1:" + this.mEndcBearContinuousTime[0] + ", mEndcBearConTLev2:" + this.mEndcBearContinuousTime[1] + ", mEndcBearConTLev3:" + this.mEndcBearContinuousTime[2] + ", mDisableEndcSettingTime:" + this.mDisableEndcSettingTime + ", mEnableEndcSettingTime:" + this.mEnableEndcSettingTime + ", mDisableEndcCnt:" + this.mDisableEndcCnt + ", mEnableEndcCnt:" + this.mEnableEndcCnt + ", mLteJamCnt:" + this.mLteJamCnt + ", mLteJamDuration:" + this.mLteJamDuration + ",current_time =  " + (SystemClock.elapsedRealtime() / mSecondMs));
                    } catch (Exception e4) {
                        e = e4;
                    }
                    broadcastSmart5gKeylog(reportSwitchOn, this.mCurrBatteryLevel);
                    this.mEndcBearDuration = 0;
                    this.mNoEndcBearDuration = 0;
                    this.mDisEndcTimerOutCnt = 0;
                    int[] iArr22 = this.mEndcBearContinuousTime;
                    iArr22[0] = 0;
                    iArr22[1] = 0;
                    iArr22[2] = 0;
                    this.mDisableEndcSettingTime = 0;
                    this.mEnableEndcSettingTime = 0;
                    this.mDisableEndcCnt = 0;
                    this.mEnableEndcCnt = 0;
                    this.mLteJamCnt = 0;
                    this.mLteJamDuration = 0;
                    this.mLastKeylogRptTime = mCurrentTimeSec;
                    resetAvgSampleSpeedKeyLogInfo();
                    resetEndcCauseCntInfo();
                }
            } catch (Exception e5) {
                e = e5;
                log("reportEndcPwrKeylog error: " + e);
                broadcastSmart5gKeylog(reportSwitchOn, this.mCurrBatteryLevel);
                this.mEndcBearDuration = 0;
                this.mNoEndcBearDuration = 0;
                this.mDisEndcTimerOutCnt = 0;
                int[] iArr222 = this.mEndcBearContinuousTime;
                iArr222[0] = 0;
                iArr222[1] = 0;
                iArr222[2] = 0;
                this.mDisableEndcSettingTime = 0;
                this.mEnableEndcSettingTime = 0;
                this.mDisableEndcCnt = 0;
                this.mEnableEndcCnt = 0;
                this.mLteJamCnt = 0;
                this.mLteJamDuration = 0;
                this.mLastKeylogRptTime = mCurrentTimeSec;
                resetAvgSampleSpeedKeyLogInfo();
                resetEndcCauseCntInfo();
            }
        }
    }

    private void reportScenesKeyLogInfo(boolean reportSwitchOn, boolean reportLightSwitch) {
        String key_info;
        String key_info2 = "Switch:" + reportSwitchOn + ", LightSwitch: " + reportLightSwitch + ";";
        for (int mSceneType = MtkRuimRecords.PHB_DELAY_SEND_TIME; mSceneType <= 504; mSceneType++) {
            long[][] jArr = this.mScenesKeyInfo;
            if (jArr[mSceneType - 500][1] > 0) {
                jArr[mSceneType - 500][2] = jArr[mSceneType - 500][2] / (jArr[mSceneType - 500][1] * STANDARD_VALUE);
            }
            long[][] jArr2 = this.mScenesKeyInfo;
            if (jArr2[mSceneType - 500][3] > 0) {
                jArr2[mSceneType - 500][4] = jArr2[mSceneType - 500][4] / (jArr2[mSceneType - 500][3] * STANDARD_VALUE);
            }
            switch (mSceneType) {
                case MtkRuimRecords.PHB_DELAY_SEND_TIME /* 500 */:
                    key_info = key_info2 + "video:";
                    break;
                case SCENE_TYPE_VIDEO_CALL /* 501 */:
                    key_info = key_info2 + "video call:";
                    break;
                case SCENE_TYPE_GAME /* 502 */:
                    key_info = key_info2 + "game:";
                    break;
                case SCENE_TYPE_VIDEO_LIVE /* 503 */:
                    key_info = key_info2 + "video live:";
                    break;
                case 504:
                    key_info = key_info2 + "file transfer:";
                    break;
                default:
                    log("no such SceneType: " + mSceneType + " return");
                    return;
            }
            String key_info3 = key_info + this.mScenesKeyInfo[mSceneType - 500][0];
            this.mScenesKeyInfo[mSceneType - 500][0] = 0;
            for (int mKeyInfoType = 1; mKeyInfoType <= 4; mKeyInfoType++) {
                key_info3 = key_info3 + "," + this.mScenesKeyInfo[mSceneType - 500][mKeyInfoType];
                this.mScenesKeyInfo[mSceneType - 500][mKeyInfoType] = 0;
            }
            key_info2 = key_info3 + ";";
        }
        log("reportScenesKeyLogInfo key_info: " + key_info2);
        try {
            String log_string = OemTelephonyUtils.getOemRes(this.mContext, "zz_oppo_critical_log_99", "");
            if (log_string.equals("")) {
                log("return for get log_string fail.");
                return;
            }
            String[] log_array = log_string.split(",");
            OppoManager.writeLogToPartition(Integer.valueOf(log_array[0]).intValue(), key_info2, "NETWORK", ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCENES_INFO, log_array[1]);
            this.mLastScenesKeylogRptTime = SystemClock.elapsedRealtime() / mSecondMs;
        } catch (Exception e) {
            log("reportEndcPwrKeylog error: " + e);
        }
    }

    private void Smart5gInfoLtePoorThresConfig(int poorLteRsrpThres, int poorLteRsrqThres, int poorLteBwThres) {
        log("Smart5gInfoLtePoorThresConfig phoneId ");
        Intent intent = new Intent(ACTION_LTE_POOR_THRESHOLD_CONFIG);
        intent.putExtra("ltePoorRsrpThres", poorLteRsrpThres);
        intent.putExtra("ltePoorRsrqThres", poorLteRsrqThres);
        intent.putExtra("ltePoorBwThres", poorLteBwThres);
        this.mContext.sendBroadcast(intent);
    }

    private long getDisableEndcTimerLength() {
        long j = this.LIGHT_DISABLE_ENDC_DELAY_MS;
        if (!this.isSwitchOn) {
            return this.LIGHT_DISABLE_ENDC_DELAY_MS;
        }
        long mDisEndcTimer = this.DISABLE_ENDC_DELAY_MS;
        if (this.mNeedRewardDisEndcTimer) {
            return this.REWARD_DISABLE_ENDC_DELAY_MS;
        }
        return mDisEndcTimer;
    }

    private void startDisableEndcAlarmTimer(long delay) {
        this.mDisEdncTimerAlarmIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_DISABLE_ENDC_TIMER_OUT), 134217728);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, this.mDisEdncTimerAlarmIntent);
        log("startDisableEndcAlarmTimer-time:" + SystemClock.elapsedRealtime() + " delay " + delay);
        this.mCurrentDisableEndcTimerLength = delay;
        this.mIsDisableEndcTimerOut = false;
    }

    private void stopDisableEndcAlarmTimer() {
        PendingIntent pendingIntent = this.mDisEdncTimerAlarmIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
            this.mDisEdncTimerAlarmIntent = null;
            this.mIsDisableEndcTimerOut = false;
            log("stopDisableEndcAlarmTimer-time:" + SystemClock.elapsedRealtime());
        }
    }

    private boolean isBuildTypeNeedCloseLightOptByDefault() {
        log(" NetBuildType = " + mNetBuildType);
        if ("cta".equals(mNetBuildType) || "allnetcmcctest".equals(mNetBuildType) || "allnetcmccfield".equals(mNetBuildType) || "allnetcutest".equals(mNetBuildType) || "allnetcufield".equals(mNetBuildType) || "allnetcttest".equals(mNetBuildType) || "allnetctfield".equals(mNetBuildType)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void actionConfirmedNrBearDeAlloc() {
        this.isNrBearAlloc = false;
        this.mStartFlowIsNR = false;
        this.mNeedRewardDisEndcTimer = false;
        if (this.mDisEdncTimerAlarmIntent != null) {
            log("actionConfirmedNrBearDeAlloc: disable ENDC immediately");
            stopDisableEndcAlarmTimer();
            enableEndc(this.mDefaultDataSlotId, false, 1);
            this.mLastState = false;
        }
        updateNrBearKeyInfo(this.isNrBearAlloc);
    }

    private void updateNrBearKeyInfo(boolean currNrBearAlloc) {
        long mDuraTimeSec = (SystemClock.elapsedRealtime() / mSecondMs) - this.mLastEndcBearStatsTime;
        if (this.mLastEndcBearStatus) {
            this.mEndcBearDuration += mDuraTimeSec;
            if (mDuraTimeSec >= 540) {
                int[] iArr = this.mEndcBearContinuousTime;
                iArr[2] = iArr[2] + 1;
            } else if (mDuraTimeSec >= 360) {
                int[] iArr2 = this.mEndcBearContinuousTime;
                iArr2[1] = iArr2[1] + 1;
            } else if (mDuraTimeSec >= 180) {
                int[] iArr3 = this.mEndcBearContinuousTime;
                iArr3[0] = iArr3[0] + 1;
            }
        } else {
            this.mNoEndcBearDuration += mDuraTimeSec;
        }
        this.mLastEndcBearStatsTime = SystemClock.elapsedRealtime() / mSecondMs;
        this.mLastEndcBearStatus = currNrBearAlloc;
        log("after onAnyNrBearerAllocation mLastEndcBearStatsTime =  " + this.mLastEndcBearStatsTime + ",mDuraTimeSec =  " + mDuraTimeSec + ",mEndcBearDuration =  " + this.mEndcBearDuration + ",mNoEndcBearDuration =  " + this.mNoEndcBearDuration + ",mLastEndcBearStatus =  " + this.mLastEndcBearStatus);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterForServiceStateChanged() {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.unregisterForServiceStateChanged(this.mHandler);
        }
        this.mHandler.removeMessages(1011);
    }

    private void updateLteQuality(long rxSpeed) {
        SignalStrength signalStrength;
        if (this.isNrBearAlloc) {
            this.mLteQualityBuffCnt = 0;
            return;
        }
        Phone phone = this.mPhone;
        if (!(phone == null || (signalStrength = phone.getSignalStrength()) == null)) {
            int rsrp = signalStrength.getLteRsrp();
            int snr = signalStrength.getLteRssnr();
            log("updateLteQuality rsrp =  " + rsrp + ",snr =  " + snr);
            if (!(Integer.MAX_VALUE == rsrp || Integer.MAX_VALUE == snr)) {
                if (rsrp < LTE_SINAGL_QUALITY_P_THRES || snr > 0) {
                    this.mLteSingalQualityBuffer[this.mLteQualityBuffCnt % 7] = 0;
                } else {
                    this.mLteSingalQualityBuffer[this.mLteQualityBuffCnt % 7] = 1;
                }
                if (rxSpeed < 40 || rxSpeed > 500) {
                    this.mLteRxSpeedBuffer[this.mLteQualityBuffCnt % 7] = 0;
                } else {
                    this.mLteRxSpeedBuffer[this.mLteQualityBuffCnt % 7] = 1;
                }
                this.mLteQualityBuffCnt++;
                log("updateLteQuality mLteQualityBuffCnt =  " + this.mLteQualityBuffCnt);
                if (rxSpeed >= mSecondMs) {
                    this.mHandler.removeMessages(1012);
                    updateLteJamDurationTime(this.mLteJam, false);
                    this.mLteJam = false;
                    return;
                }
                return;
            }
        }
        this.mLteQualityBuffCnt = 0;
    }

    private boolean isLteDataJam() {
        int lteSingalQualityBadCnt = 0;
        int lteRxQualityBadCnt = 0;
        boolean isLteJam = false;
        if (this.mLteQualityBuffCnt < 7) {
            return false;
        }
        for (int lteSampleIndex = 0; lteSampleIndex < 7; lteSampleIndex++) {
            if (1 == this.mLteSingalQualityBuffer[lteSampleIndex]) {
                lteSingalQualityBadCnt++;
            }
            if (1 == this.mLteRxSpeedBuffer[lteSampleIndex]) {
                lteRxQualityBadCnt++;
            }
        }
        if (lteSingalQualityBadCnt >= 5 && lteRxQualityBadCnt >= 7) {
            this.mHandler.removeMessages(1012);
            this.mHandler.sendEmptyMessageDelayed(1012, this.LTE_JAM_PUNISH_MS);
            this.mLteJam = true;
            isLteJam = true;
            this.mLastEnterLteJamTime = SystemClock.elapsedRealtime() / mSecondMs;
        }
        if (this.mLteJam) {
            return true;
        }
        return isLteJam;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLteJamDurationTime(boolean lastState, boolean currentState) {
        if (lastState && !currentState) {
            this.mLteJamDuration += (SystemClock.elapsedRealtime() / mSecondMs) - this.mLastEnterLteJamTime;
            log("updateLteJamDurationTime duration is   " + this.mLteJamDuration);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startEnableEndcHysTimer() {
        this.mHandler.removeMessages(1013);
        if (this.isCmccEval) {
            this.isRadioOnEndcHys = false;
            if (this.isFirstEnableEndc) {
                log("startEnableEndcHysTimer length is   " + this.ENABLE_ENDC_HYSTERESIS_MS_RADIO_ON);
                this.mHandler.sendEmptyMessageDelayed(1013, this.ENABLE_ENDC_HYSTERESIS_MS_RADIO_ON);
                this.isFirstEnableEndc = false;
                this.isRadioOnEndcHys = true;
                return;
            }
        }
        this.mHandler.sendEmptyMessageDelayed(1013, this.ENABLE_ENDC_HYSTERESIS_MS);
    }

    private void stopEnableEndcHysTimer() {
        if (!this.isCmccEval) {
            this.mHandler.removeMessages(1013);
        } else if (!this.isRadioOnEndcHys) {
            this.mHandler.removeMessages(1013);
        }
    }

    private boolean isEnableEndcHysTimerRun() {
        if (this.mHandler.hasMessages(1013)) {
            return true;
        }
        return false;
    }

    public boolean isNoExpOperatorNeedDisable() {
        if ("TH".equals(mRegionMark) || "IN".equals(mRegionMark) || "AU".equals(mRegionMark)) {
            return true;
        }
        if ("EUEX".equals(mRegionMark) && "INVALID".equals(mEuexCountry)) {
            return true;
        }
        if ((!"EUEX".equals(mRegionMark) || !"RO".equals(mEuexCountry)) && !"AE".equals(mRegionMark) && !"JP".equals(mRegionMark) && !"EG".equals(mRegionMark)) {
            return false;
        }
        return true;
    }

    public void updateNoOpExpEnableSmart5gInd() {
        int value;
        boolean z = mExpVersion;
        if (!z || (z && !"oppo".equals(mOperatorName))) {
            value = -1;
        } else if (isNoExpOperatorNeedDisable()) {
            value = 0;
        } else {
            value = 1;
        }
        log("NoOpExpEnableSmart5g set to value  " + value);
        Settings.Global.putInt(this.mContext.getContentResolver(), "NoOpExpEnableSmart5g", value);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSceneStateMsg(int sceneType, int actionType) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1007, sceneType, actionType));
    }

    private void printSceneStatesInfo() {
        log("printSceneStatesInfo mIsAnySceneNeedRestrict: " + this.mIsAnySceneNeedRestrict + " mScenesRestrictEnableMsks: " + Integer.toBinaryString(this.mScenesRestrictEnableMsks) + " mScenesNeedRestrictMsks: " + Integer.toBinaryString(this.mScenesNeedRestrictMsks));
        for (int mSceneType = MtkRuimRecords.PHB_DELAY_SEND_TIME; mSceneType <= 504; mSceneType++) {
            log("printSceneStatesInfo scene type is : " + mSceneType);
            for (int mKeyType = 0; mKeyType <= 4; mKeyType++) {
                log("printSceneStatesInfo key type is : " + mKeyType + " value is " + this.mScenesKeyInfo[mSceneType - 500][mKeyType]);
            }
        }
    }

    private void collectScenesKeyInfo(int sceneType, int keyTypeId, long value) {
        if (value > 0) {
            long[] jArr = this.mScenesKeyInfo[sceneType - 500];
            jArr[keyTypeId] = jArr[keyTypeId] + value;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSceneStates(int sceneType, int actionType) {
        if (isSceneTypeValid(sceneType) && isSceneActionValid(actionType)) {
            if (actionType == 1) {
                this.mScenesStatusMsks |= 1 << (sceneType - 500);
            } else {
                int i = this.mScenesStatusMsks;
                if (((1 << (sceneType - 500)) | i) != 0) {
                    this.mScenesStatusMsks = i & (~(1 << (sceneType - 500)));
                } else {
                    log("error updateSceneStates scene type  " + actionType + " no enter before");
                    return;
                }
            }
            this.mScenesNeedRestrictMsks = this.mScenesRestrictEnableMsks & this.mScenesStatusMsks;
            this.mScenesNeedRestrictMsks &= 255;
            this.mIsAnySceneNeedRestrict = false;
            if (this.mScenesNeedRestrictMsks > 0) {
                this.mIsAnySceneNeedRestrict = true;
            }
            updateScenesKeyLogInfo(sceneType, actionType);
        }
    }

    private void updateScenesKeyLogInfo(int sceneType, int actionType) {
        if (isSceneTypeValidForKeyLog(sceneType) && isSceneActionValid(actionType)) {
            long mCurrentTime = SystemClock.elapsedRealtime() / mSecondMs;
            long mTotalRxBytes = TrafficStats.getMobileRxBytes();
            log("updateScenesKeyLogInfo TotalRxBytes is : " + mTotalRxBytes + " CurrentTime is : " + mCurrentTime);
            if (actionType == 1 && this.mIsMobileDataUsed) {
                this.mScenesStartTimeStats[sceneType - 500] = mCurrentTime;
                if (this.isNrBearAlloc) {
                    this.mScenesNrUsedStartTimeStats[sceneType - 500] = mCurrentTime;
                    this.mScenesNrUsedBytesStats[sceneType - 500] = mTotalRxBytes;
                    return;
                }
                this.mScenesNrNotUsedStartTimeStats[sceneType - 500] = mCurrentTime;
                this.mScenesNrNotUsedBytesStats[sceneType - 500] = mTotalRxBytes;
            } else if (actionType == 0 && this.mIsMobileDataUsed) {
                long[] jArr = this.mScenesStartTimeStats;
                if (0 != jArr[sceneType - 500]) {
                    collectScenesKeyInfo(sceneType, 0, mCurrentTime - jArr[sceneType - 500]);
                    if (this.isNrBearAlloc) {
                        collectScenesKeyInfo(sceneType, 1, mCurrentTime - this.mScenesNrUsedStartTimeStats[sceneType - 500]);
                        collectScenesKeyInfo(sceneType, 2, mTotalRxBytes - this.mScenesNrUsedBytesStats[sceneType - 500]);
                    } else {
                        collectScenesKeyInfo(sceneType, 3, mCurrentTime - this.mScenesNrNotUsedStartTimeStats[sceneType - 500]);
                        collectScenesKeyInfo(sceneType, 4, mTotalRxBytes - this.mScenesNrNotUsedBytesStats[sceneType - 500]);
                    }
                    this.mScenesStartTimeStats[sceneType - 500] = 0;
                    printSceneStatesInfo();
                }
            }
        }
    }

    private void nrBearChangedUpdateSceneStates(boolean lastEndcBearStatus) {
        long mCurrentTimeSec = SystemClock.elapsedRealtime() / mSecondMs;
        long mCurrentRxBytes = TrafficStats.getMobileRxBytes();
        for (int mSceneType = MtkRuimRecords.PHB_DELAY_SEND_TIME; mSceneType <= 504; mSceneType++) {
            if (1 == (this.mScenesStatusMsks & (1 << (mSceneType - 500)))) {
                if (lastEndcBearStatus) {
                    collectScenesKeyInfo(mSceneType, 1, mCurrentTimeSec - this.mScenesNrUsedStartTimeStats[mSceneType - 500]);
                    collectScenesKeyInfo(mSceneType, 2, mCurrentRxBytes - this.mScenesNrUsedBytesStats[mSceneType - 500]);
                    this.mScenesNrNotUsedStartTimeStats[mSceneType - 500] = mCurrentTimeSec;
                    this.mScenesNrNotUsedBytesStats[mSceneType - 500] = mCurrentRxBytes;
                } else {
                    collectScenesKeyInfo(mSceneType, 3, mCurrentTimeSec - this.mScenesNrNotUsedStartTimeStats[mSceneType - 500]);
                    collectScenesKeyInfo(mSceneType, 4, mCurrentRxBytes - this.mScenesNrNotUsedBytesStats[mSceneType - 500]);
                    this.mScenesNrUsedStartTimeStats[mSceneType - 500] = mCurrentTimeSec;
                    this.mScenesNrUsedBytesStats[mSceneType - 500] = mCurrentRxBytes;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getEndcBearStatus(int RatTech, boolean nrBearAlloc) {
        if ((RatTech == 13 || RatTech == 19) && nrBearAlloc) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateEndcBearInfo(boolean lastEndcBearStatus) {
        long mDuraTimeSec = (SystemClock.elapsedRealtime() / mSecondMs) - this.mLastEndcBearStatsTime;
        if (this.mLastEndcBearStatus) {
            this.mEndcBearDuration += mDuraTimeSec;
            if (mDuraTimeSec >= 540) {
                int[] iArr = this.mEndcBearContinuousTime;
                iArr[2] = iArr[2] + 1;
            } else if (mDuraTimeSec >= 360) {
                int[] iArr2 = this.mEndcBearContinuousTime;
                iArr2[1] = iArr2[1] + 1;
            } else if (mDuraTimeSec >= 180) {
                int[] iArr3 = this.mEndcBearContinuousTime;
                iArr3[0] = iArr3[0] + 1;
            }
        } else {
            this.mNoEndcBearDuration += mDuraTimeSec;
        }
        nrBearChangedUpdateSceneStates(this.mLastEndcBearStatus);
        this.mLastEndcBearStatsTime = SystemClock.elapsedRealtime() / mSecondMs;
        log("after updateEndcBearInfo mLastEndcBearStatsTime =  " + this.mLastEndcBearStatsTime + ",mDuraTimeSec =  " + mDuraTimeSec + ",mEndcBearDuration =  " + this.mEndcBearDuration + ",mNoEndcBearDuration =  " + this.mNoEndcBearDuration + ",mLastNrBearStatus =  " + this.mLastEndcBearStatus);
    }

    private boolean isSceneTypeValid(int sceneType) {
        if (sceneType < 500 || sceneType > 504) {
            return false;
        }
        return true;
    }

    private boolean isSceneTypeValidForKeyLog(int sceneType) {
        if (sceneType < 500 || sceneType > 504) {
            return false;
        }
        return true;
    }

    private boolean isSceneActionValid(int actionType) {
        if (actionType == 0 || actionType == 1) {
            return true;
        }
        return false;
    }

    private class EventListen {
        public EventCallback mCallBack = new EventCallback() {
            /* class com.mediatek.internal.telephony.EndcBearController.EventListen.AnonymousClass1 */

            public void onEventStateChanged(DeviceEventResult deviceEventResult) {
                EndcBearController.this.log(deviceEventResult.toString());
                int mActionType = -1;
                int mSceneType = -1;
                int status = deviceEventResult.getEventStateType();
                if (status == 0) {
                    EndcBearController.this.log("event enter.");
                    mActionType = 1;
                } else if (status == 1) {
                    EndcBearController.this.log("event exit.");
                    mActionType = 0;
                } else {
                    EndcBearController.this.log("event without status.");
                }
                int eventType = deviceEventResult.getEventType();
                if (eventType == 205) {
                    EndcBearController.this.log("event VIDEO.");
                    mSceneType = MtkRuimRecords.PHB_DELAY_SEND_TIME;
                } else if (eventType != 214) {
                    switch (eventType) {
                        case 209:
                            EndcBearController.this.log("event SCENE_MODE_VIDEO.");
                            mSceneType = EndcBearController.SCENE_TYPE_VIDEO_CALL;
                            break;
                        case 210:
                            EndcBearController.this.log("event FILE_DOWNLOAD");
                            mSceneType = 504;
                            break;
                        case 211:
                            EndcBearController.this.log("event GAME.");
                            mSceneType = EndcBearController.SCENE_TYPE_GAME;
                            break;
                        case 212:
                            EndcBearController.this.log("event VIDEO_LIVE");
                            mSceneType = EndcBearController.SCENE_TYPE_VIDEO_LIVE;
                            break;
                    }
                } else {
                    EndcBearController.this.log("event FILE_UPLOAD");
                    mSceneType = 504;
                }
                if (mActionType == -1 || mSceneType == -1) {
                    EndcBearController endcBearController = EndcBearController.this;
                    endcBearController.log("event invalid mActionType " + mActionType + "mSceneType " + mSceneType);
                    return;
                }
                EndcBearController.this.sendSceneStateMsg(mSceneType, mActionType);
            }
        };

        public EventListen() {
            ArraySet<DeviceEvent> deviceEvents = new ArraySet<>();
            deviceEvents.add(new DeviceEvent.Builder().setEventType(205).setEventStateType(0).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(205).setEventStateType(1).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(209).setEventStateType(0).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(209).setEventStateType(1).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(211).setEventStateType(0).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(211).setEventStateType(1).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(212).setEventStateType(0).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(212).setEventStateType(1).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(210).setEventStateType(0).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(210).setEventStateType(1).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(214).setEventStateType(0).build());
            deviceEvents.add(new DeviceEvent.Builder().setEventType(214).setEventStateType(1).build());
            final EventRequestConfig config = new EventRequestConfig(deviceEvents);
            EndcBearController.this.mManager = ColorFrameworkFactory.getInstance().getColorDeepThinkerManager(EndcBearController.this.mContext);
            new Thread(new Runnable(EndcBearController.this) {
                /* class com.mediatek.internal.telephony.EndcBearController.EventListen.AnonymousClass2 */

                public void run() {
                    if (EndcBearController.this.mManager.registerCallback(EventListen.this.mCallBack, config)) {
                        EndcBearController.this.log("DeepThinkerManager register success!");
                    } else {
                        EndcBearController.this.log("DeepThinkerManager register failed!");
                    }
                    EndcBearController.this.mEventHubServiceStateObserver = new ServiceStateObserver() {
                        /* class com.mediatek.internal.telephony.EndcBearController.EventListen.AnonymousClass2.AnonymousClass1 */

                        public void onServiceDied() {
                            EndcBearController.this.log("onServiceDied register again!");
                            if (EndcBearController.this.mManager.registerCallback(EventListen.this.mCallBack, config)) {
                                EndcBearController.this.log("register success!");
                            } else {
                                EndcBearController.this.log("register failed!");
                            }
                        }
                    };
                    EndcBearController.this.mManager.registerServiceStateObserver(EndcBearController.this.mEventHubServiceStateObserver);
                }
            }).start();
        }
    }

    private void initScenesConfig() {
        String sceneConfig = "ver_num=" + this.mSceneParaVersionN + ";scg_add_speed=" + this.mScgAddSpeedForScene + ";scg_add_speed_lowbat=" + this.mScgAddSpeedLowbatForScene + ";scenes_prohibit_masks=" + this.mScenesRestrictEnableMsks;
        log("initSceneConfig is " + sceneConfig);
        Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrScenesPara", sceneConfig);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScenesConfig() {
        String scenesPara = Settings.Global.getString(this.mContext.getContentResolver(), "EndcLowPwrScenesPara");
        if (scenesPara != null) {
            log("updateScenesConfig org is " + scenesPara);
            String[] paraArray = scenesPara.split(";");
            if (4 != paraArray.length) {
                log("updateScenesConfig length is  " + paraArray.length);
                initScenesConfig();
                return;
            }
            if (paraArray[0].contains("ver_num")) {
                String[] verNumArray = paraArray[0].split("=");
                if (2 == verNumArray.length && isNumeric(verNumArray[1])) {
                    int tempPara = Integer.parseInt(verNumArray[1]);
                    int i = this.mSceneParaVersionN;
                    if (i > tempPara) {
                        initScenesConfig();
                        return;
                    } else if (i == tempPara) {
                        log("scenes_cfg: same version no need update");
                        return;
                    } else {
                        this.mSceneParaVersionN = tempPara;
                    }
                }
            }
            if (paraArray[1].contains("scg_add_speed")) {
                String[] scgAddSpeed = paraArray[1].split("=");
                if (2 == scgAddSpeed.length && isNumeric(scgAddSpeed[1])) {
                    this.mScgAddSpeedForScene = Integer.parseInt(scgAddSpeed[1]);
                }
            }
            if (paraArray[2].contains("scg_add_speed_lowbat")) {
                String[] scgAddSpeedLow = paraArray[2].split("=");
                if (2 == scgAddSpeedLow.length && isNumeric(scgAddSpeedLow[1])) {
                    this.mScgAddSpeedLowbatForScene = Integer.parseInt(scgAddSpeedLow[1]);
                }
            }
            if (paraArray[3].contains("scenes_prohibit_masks")) {
                String[] sceneMask = paraArray[3].split("=");
                if (2 == sceneMask.length && isNumeric(sceneMask[1])) {
                    this.mScenesRestrictEnableMsks = Integer.parseInt(sceneMask[1]);
                    return;
                }
                return;
            }
            return;
        }
        log("updateScenesConfig no list");
        initScenesConfig();
    }

    private void registerForScenesSettings() {
        log("registerForScenesSettings");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("EndcLowPwrScenesPara"), false, this.mEndcLowPwrScenesConfig);
    }

    private void initMonitorApkConfig() {
        String apkMonitorCfg = ("ver_num=" + this.mApkMonitorVersionN + ";scg_add_speed=" + this.mScgAddSpeedForApk + ";scg_add_speed_lowbat=" + this.mScgAddSpeedLowbatForApk + ";restrict=" + this.mEndcLowPwrRestrictApkStr + ";black=" + this.mEndcLowPwrBlackApkStr).replaceAll(" ", "");
        StringBuilder sb = new StringBuilder();
        sb.append("Initial initMonitorApkConfig is ");
        sb.append(apkMonitorCfg);
        log(sb.toString());
        Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrApkPara", apkMonitorCfg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMonitorApkList() {
        String apkConfig = Settings.Global.getString(this.mContext.getContentResolver(), "EndcLowPwrApkPara");
        if (apkConfig != null) {
            log("updateMonitorApkList org is " + apkConfig);
            String[] paraArray = apkConfig.split(";");
            if (5 != paraArray.length) {
                log("updateMonitorApkList length is  " + paraArray.length);
                initMonitorApkConfig();
                return;
            }
            if (paraArray[0].contains("ver_num")) {
                String[] verNumArray = paraArray[0].split("=");
                if (2 == verNumArray.length && isNumeric(verNumArray[1])) {
                    int tempPara = Integer.parseInt(verNumArray[1]);
                    int i = this.mApkMonitorVersionN;
                    if (i > tempPara) {
                        initMonitorApkConfig();
                        return;
                    } else if (i == tempPara) {
                        log("apk_cfg: same version no need update");
                        return;
                    } else {
                        this.mApkMonitorVersionN = tempPara;
                    }
                }
            }
            if (paraArray[1].contains("scg_add_speed")) {
                String[] scgAddSpeed = paraArray[1].split("=");
                if (2 == scgAddSpeed.length && isNumeric(scgAddSpeed[1])) {
                    this.mScgAddSpeedForApk = Integer.parseInt(scgAddSpeed[1]);
                }
            }
            if (paraArray[2].contains("scg_add_speed_lowbat")) {
                String[] scgAddSpeedLow = paraArray[2].split("=");
                if (2 == scgAddSpeedLow.length && isNumeric(scgAddSpeedLow[1])) {
                    this.mScgAddSpeedLowbatForApk = Integer.parseInt(scgAddSpeedLow[1]);
                }
            }
            if (paraArray[3].contains("restrict")) {
                String[] restrictStr = paraArray[3].split("=");
                if (2 == restrictStr.length) {
                    this.mEndcLowPwrRestrictApkStr = restrictStr[1];
                } else if (1 == restrictStr.length) {
                    this.mEndcLowPwrRestrictApkStr = "";
                }
                log("apk list parse info restrict apk=" + this.mEndcLowPwrRestrictApkStr);
            }
            if (paraArray[4].contains("black")) {
                String[] blackStr = paraArray[4].split("=");
                log("apk list parse info black str length = " + blackStr.length);
                if (2 == blackStr.length) {
                    this.mEndcLowPwrBlackApkStr = blackStr[1];
                } else if (1 == blackStr.length) {
                    this.mEndcLowPwrBlackApkStr = "";
                }
                log("apk list parse info blackApk=" + this.mEndcLowPwrBlackApkStr);
            }
            unregisterForAppSwitchObserver();
            registerForAppSwitchObserver();
            return;
        }
        log("updateMonitorApkList no list");
        initMonitorApkConfig();
    }

    private void registerForMonitorApkListSettings() {
        log("registerForMonitorApkListSettings");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("EndcLowPwrApkPara"), false, this.mEndcLowPwrMonitorApkLst);
    }

    private void apkSwitchObserverRegDeterminnation(boolean newWorkState, boolean oldWorkState) {
        if (newWorkState && !oldWorkState) {
            registerForAppSwitchObserver();
        }
        if (oldWorkState && !newWorkState) {
            unregisterForAppSwitchObserver();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMonitorApkStateChangedMsg() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1014));
    }

    private void registerForAppSwitchObserver() {
        log("registerForAppSwitchObserver");
        String monitorApkStr = null;
        if (!this.mEndcLowPwrRestrictApkStr.isEmpty()) {
            monitorApkStr = this.mEndcLowPwrRestrictApkStr;
        }
        if (!this.mEndcLowPwrBlackApkStr.isEmpty()) {
            monitorApkStr = monitorApkStr + "," + this.mEndcLowPwrBlackApkStr;
        }
        if (monitorApkStr == null) {
            return;
        }
        if (monitorApkStr.isEmpty()) {
            log("registerForAppSwitchObserver apk list is empty ");
            return;
        }
        log("registerForAppSwitchObserver apk list is " + monitorApkStr);
        String[] monitorApkArray = monitorApkStr.split(",");
        ColorAppSwitchConfig config = new ColorAppSwitchConfig();
        config.addAppConfig(2, Arrays.asList(monitorApkArray));
        ColorAppSwitchManager.getInstance().registerAppSwitchObserver(this.mContext, this.mDynamicObserver, config);
    }

    private void unregisterForAppSwitchObserver() {
        ColorAppSwitchManager.getInstance().unregisterAppSwitchObserver(this.mContext, this.mDynamicObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateThermalKeyInfo(int actionId, long currTime, int updateValue) {
        if (actionId >= 5) {
            log("error updateThermalKeyInfo action is " + actionId);
            return;
        }
        if (1 == updateValue) {
            long[][] jArr = this.mThermalKeyInfo;
            jArr[actionId][2] = currTime;
            jArr[actionId][0] = jArr[actionId][0] + 1;
            this.mThermalActionMsk |= 1 << actionId;
        } else {
            int i = this.mThermalActionMsk;
            if (((1 << actionId) & i) != 0) {
                long[][] jArr2 = this.mThermalKeyInfo;
                jArr2[actionId][1] = jArr2[actionId][1] + (currTime - jArr2[actionId][2]);
                this.mThermalActionMsk = i & (~(1 << actionId));
            } else {
                log("error updateThermalKeyInfo action id  " + actionId + "had no enter before");
                return;
            }
        }
        log("after updateThermalKeyInfo mask is B" + Integer.toBinaryString(this.mThermalActionMsk) + ",actionId:" + actionId + ",Count:" + this.mThermalKeyInfo[actionId][0] + ",currTime:" + this.mThermalKeyInfo[actionId][2] + ",Duration:" + this.mThermalKeyInfo[actionId][1]);
    }

    private void reportThermalKeylog() {
        long totalCount = 0;
        String key_info = "thermal kpis:";
        for (int thermalType = 0; thermalType < 5; thermalType++) {
            key_info = (key_info + this.mThermalActionName[thermalType] + ":") + this.mThermalKeyInfo[thermalType][0] + "," + this.mThermalKeyInfo[thermalType][1] + ";";
            long[][] jArr = this.mThermalKeyInfo;
            totalCount += jArr[thermalType][0];
            jArr[thermalType][0] = 0;
            jArr[thermalType][1] = 0;
        }
        if (0 != totalCount) {
            log("reportThermalKeylog key_info: " + key_info);
            try {
                String log_string = OemTelephonyUtils.getOemRes(this.mContext, "zz_oppo_critical_log_133", "");
                if (log_string.equals("")) {
                    log("return for get log_string fail.");
                    return;
                }
                String[] log_array = log_string.split(",");
                OppoManager.writeLogToPartition(Integer.valueOf(log_array[0]).intValue(), key_info, "NETWORK", ISSUE_SYS_OEM_NW_DIAG_CAUSE_THERMAL_KPIS, log_array[1]);
                this.mLastThermalKeylogRptTime = SystemClock.elapsedRealtime() / mSecondMs;
            } catch (Exception e) {
                log("reportEndcPwrKeylog error: " + e);
            }
        }
    }

    private boolean isOperatorNeedCloseChargingEnableOptByDefault() {
        log("operator is  = " + mOperatorName);
        if ("TELSTRA_POSTPAID".equals(mOperatorName)) {
            return true;
        }
        return false;
    }

    private boolean isCmccEvalBuild() {
        if ("allnetcmcctest".equals(mNetBuildType) || "allnetcmccfield".equals(mNetBuildType)) {
            return true;
        }
        return false;
    }

    private void resetEndcCauseCntInfo() {
        for (int Index = 0; Index < 6; Index++) {
            this.mEnableEndcCausetCnt[Index] = 0;
        }
        for (int Index2 = 0; Index2 < 4; Index2++) {
            this.mDisableEndcCausetCnt[Index2] = 0;
        }
    }
}
