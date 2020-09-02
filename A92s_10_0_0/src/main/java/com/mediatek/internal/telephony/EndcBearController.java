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
import android.net.TrafficStats;
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
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.util.WakeupMessage;
import com.coloros.eventhub.sdk.EventCallback;
import com.coloros.eventhub.sdk.aidl.DeviceEvent;
import com.coloros.eventhub.sdk.aidl.DeviceEventResult;
import com.coloros.eventhub.sdk.aidl.EventRequestConfig;
import com.mediatek.internal.telephony.uicc.MtkRuimRecords;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimConstants;
import vendor.mediatek.hardware.mtkradioex.V1_0.MtkApnTypes;

public class EndcBearController {
    private static final String ACTION_COMMAND_FORCE_DISABLE_ENDC = "android.intent.force_disable_endc";
    private static final String ACTION_DISABLE_ENDC_TIMER_OUT = "android.intent.disable_endc.timer";
    static final String ACTION_DOWNLOAD = "com.oppo.smart.5g.download";
    public static final String ACTION_ENDC_PWR_WORK_STATE_UPDATE = "android.intent.action.endc_pwr_workstate_update";
    public static final String ACTION_INFORM_LTE_QUALITY = "android.intent.action.Smart5g_ltequality";
    public static final String ACTION_INFORM_RAT_CHANGED = "android.intent.action.RatChange";
    public static final String ACTION_LTE_POOR_THRESHOLD_CONFIG = "android.intent.action.Smart5g_ltePoorThres";
    public static final String CHINESE_MCC = "460";
    static final String DOWNLOAD_SIZE = "size";
    protected static final int ENDC_BEAR_CONTINUOUS_T_LEV1 = 180;
    protected static final int ENDC_BEAR_CONTINUOUS_T_LEV2 = 360;
    protected static final int ENDC_BEAR_CONTINUOUS_T_LEV3 = 540;
    protected static final int ENDC_PWR_CFG_LOW_BAT_UP_LIMIT = 30;
    protected static final int ENDC_PWR_CFG_PARA_LENGTH = 18;
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
    protected static final int EVENT_CHECK_NR_BEAR_DEALLOC = 1010;
    protected static final int EVENT_CMD_CONNECT_EXT_TELEPHONY_SERV = 1000;
    protected static final int EVENT_ENABLE_ENDC_HYSTERESIS_EXP = 1013;
    protected static final int EVENT_FLOW_CHANGE = 1001;
    protected static final int EVENT_FORCE_DISABLE_ENDC_TIME_OUT = 1009;
    protected static final int EVENT_GET_DEACT_SCG_CONFIG = 1006;
    protected static final int EVENT_LARGE_FILE_DOWNLOADING = 1005;
    protected static final int EVENT_LTE_JAM_TIME_EXPIRED = 1012;
    protected static final int EVENT_NETWORK_MODE_CHANGED = 1008;
    protected static final int EVENT_QUERY_STATE = 1004;
    protected static final int EVENT_RADIO_ON = 1003;
    protected static final int EVENT_SCENE_STATE_CHANGED = 1007;
    private static final int EVENT_SERVICE_STATE_CHANGED = 1011;
    private static final long FIRST_FLOW_CHANGE_STATS_DURATION_MS = 3000;
    public static final int INVALID_INT = Integer.MAX_VALUE;
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_ENDC_PWR_OPT = "endc_pwr_opt";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCENES_INFO = "SCENES_INFO";
    protected static final int LIGHT_ENDC_PWR_CFG_PARA_LENGTH = 4;
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
    protected static final int SCENE_TYPES_NUM = 4;
    protected static final int SCENE_TYPES_OFFSET = 500;
    protected static final int SCENE_TYPE_DOWNLOAD = 504;
    protected static final int SCENE_TYPE_GAME = 502;
    protected static final int SCENE_TYPE_HOLIDAY = 505;
    protected static final int SCENE_TYPE_MAX = 505;
    protected static final int SCENE_TYPE_MIN = 500;
    protected static final int SCENE_TYPE_VIDEO = 500;
    protected static final int SCENE_TYPE_VIDEO_CALL = 501;
    protected static final int SCENE_TYPE_VIDEO_LIVE = 503;
    protected static final long STANDARD_VALUE = 1024;
    private static final int SYS_OEM_NW_DIAG_CAUSE_ENDC_PWR_OPT = 103;
    private static final int SYS_OEM_NW_DIAG_CAUSE_SCENES_INFO = 99;
    private static int backOffTimeMilliSeconds = MtkGsmCdmaPhone.EVENT_IMS_UT_DONE;
    private static int connectionRetry = 8;
    private static EndcBearController instance = null;
    public static String mNetBuildType = SystemProperties.get("persist.sys.net_build_type", "allnet");
    private static final long mSecondMs = 1000;
    private long DISABLE_ENDC_DELAY_MS = 86400000;
    private boolean Debug = true;
    private long ENABLE_ENDC_HYSTERESIS_MS = 9000;
    private long FLOW_CHANGE_STATS_DURATION_MS = FIRST_FLOW_CHANGE_STATS_DURATION_MS;
    /* access modifiers changed from: private */
    public long FORCE_DISABLE_ENDC_TIMER_SEC = 600;
    private long LIGHT_DISABLE_ENDC_DELAY_MS = 86400000;
    private long LTE_JAM_PUNISH_MS = 120000;
    private long NR_BEAR_CHECK_DEALLOC_MS = 500;
    private long REWARD_DISABLE_ENDC_DELAY_MS = 86400000;
    /* access modifiers changed from: private */
    public boolean is5gIcon = false;
    private boolean isLightSwitchOn = true;
    /* access modifiers changed from: private */
    public boolean isLtePoor = false;
    /* access modifiers changed from: private */
    public boolean isNeedForceDisableEndc = false;
    /* access modifiers changed from: private */
    public boolean isNrBearAlloc = false;
    private boolean isNrEnabled = false;
    /* access modifiers changed from: private */
    public boolean isScreenOn = true;
    private boolean isSwitchOn = true;
    private AlarmManager mAlarmManager;
    private int mBatThresAsLow = 20;
    private boolean mChargingEnable = true;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mCurrBatteryLevel = 100;
    private long mCurrentDisableEndcTimerLength = 0;
    /* access modifiers changed from: private */
    public int mCurrentRat = 0;
    private int mDefalutEnable = 0;
    /* access modifiers changed from: private */
    public int mDefaultDataSlotId = 0;
    /* access modifiers changed from: private */
    public PendingIntent mDisEdncTimerAlarmIntent = null;
    private int mDisEndcTimerOutCnt = 0;
    private int mDisableEndcCnt = 0;
    private long mDisableEndcSettingTime = 0;
    private int mDownloadProhibitEnable = 1;
    /* access modifiers changed from: private */
    public int mDownloadSizeThres = 10;
    private int mEnableEndcCnt = 0;
    private long mEnableEndcSettingTime = 0;
    private int[] mEndcBearContinuousTime = {0, 0, 0};
    private long mEndcBearDuration = 0;
    private HandlerThread mEndcThread;
    private int mGameProhibitEnable = 1;
    private long[] mGameSceneKeyInfo = {0, 0, 0, 0, 0};
    /* access modifiers changed from: private */
    public Handler mHandler;
    private int mHolidayProhibitEnable = 1;
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
                        c = 8;
                        break;
                    }
                    c = 65535;
                    break;
                case -1084684149:
                    if (action.equals(EndcBearController.ACTION_INFORM_LTE_QUALITY)) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -901500994:
                    if (action.equals(EndcBearController.ACTION_DOWNLOAD)) {
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
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 2075333766:
                    if (action.equals(EndcBearController.ACTION_COMMAND_FORCE_DISABLE_ENDC)) {
                        c = 9;
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
                    boolean unused = EndcBearController.this.isScreenOn = true;
                    break;
                case 1:
                    EndcBearController.this.log("received SCREEN OFF ");
                    boolean unused2 = EndcBearController.this.isScreenOn = false;
                    break;
                case 2:
                    int unused3 = EndcBearController.this.mCurrBatteryLevel = intent.getIntExtra("level", 100);
                    boolean mIsChargingBackup = EndcBearController.this.mIsCharging;
                    EndcBearController endcBearController = EndcBearController.this;
                    if (intent.getIntExtra("plugged", 0) == 0) {
                        z = false;
                    }
                    boolean unused4 = endcBearController.mIsCharging = z;
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
                        boolean unused5 = EndcBearController.this.mIsWifihotspotOn = true;
                    } else if (intent.getIntExtra("wifi_state", 0) == 11) {
                        boolean unused6 = EndcBearController.this.mIsWifihotspotOn = false;
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
                    }
                    EndcBearController.this.mHandler.removeMessages(1003);
                    EndcBearController.this.log("defaultSlotIndx old: " + EndcBearController.this.mDefaultDataSlotId + ", new: " + EndcBearController.this.mSubscriptionManager.getDefaultDataPhoneId());
                    EndcBearController endcBearController2 = EndcBearController.this;
                    int unused7 = endcBearController2.mDefaultDataSlotId = endcBearController2.mSubscriptionManager.getDefaultDataPhoneId();
                    SubscriptionManager unused8 = EndcBearController.this.mSubscriptionManager;
                    if (SubscriptionManager.isValidPhoneId(EndcBearController.this.mDefaultDataSlotId)) {
                        EndcBearController endcBearController3 = EndcBearController.this;
                        endcBearController3.mPhone = PhoneFactory.getPhone(endcBearController3.mDefaultDataSlotId);
                        if (EndcBearController.this.mPhone != null) {
                            EndcBearController.this.mPhone.mCi.registerForOn(EndcBearController.this.mHandler, 1003, (Object) null);
                            EndcBearController.this.registerPrefNetworkModeObserver();
                            EndcBearController.this.mPhone.registerForServiceStateChanged(EndcBearController.this.mHandler, 1011, (Object) null);
                        }
                    }
                    EndcBearController.this.actionWhenDdsOrSwitchChange();
                    return;
                case 5:
                    float mDwonloadSize = Float.parseFloat(intent.getStringExtra(EndcBearController.DOWNLOAD_SIZE));
                    EndcBearController.this.log("received LARGE_FILE_DOWNLOAD_ACTION size is " + mDwonloadSize);
                    if (mDwonloadSize >= ((float) EndcBearController.this.mDownloadSizeThres)) {
                        boolean unused9 = EndcBearController.this.mIsInLargeFileDownload = true;
                        EndcBearController.this.mHandler.removeMessages(1005);
                        EndcBearController.this.mHandler.sendEmptyMessageDelayed(1005, 30000);
                        break;
                    }
                    break;
                case 6:
                    int mPhoneId = intent.getIntExtra("PhoneId", -1);
                    boolean mltePoor = intent.getBooleanExtra("ltePoor", false);
                    EndcBearController.this.log("ACTION_INFORM_LTE_QUALITY mPhoneId=" + mPhoneId + ",mltePoor=" + mltePoor);
                    if (mPhoneId == EndcBearController.this.mDefaultDataSlotId) {
                        boolean unused10 = EndcBearController.this.isLtePoor = mltePoor;
                        return;
                    }
                    return;
                case 7:
                    int mPhoneId2 = intent.getIntExtra("PhoneId", -1);
                    int mOldRat = intent.getIntExtra("OldRat", -1);
                    int mNewRat = intent.getIntExtra("NewRat", -1);
                    EndcBearController.this.log("ACTION_INFORM_RAT_CHANGED mPhoneId=" + mPhoneId2 + ",mOldRat=" + mOldRat + ",mNewRat=" + mNewRat);
                    if (mPhoneId2 == EndcBearController.this.mDefaultDataSlotId) {
                        int unused11 = EndcBearController.this.mCurrentRat = mNewRat;
                        EndcBearController endcBearController4 = EndcBearController.this;
                        boolean mCurrentEndcBearStatus = endcBearController4.getEndcBearStatus(endcBearController4.mCurrentRat, EndcBearController.this.isNrBearAlloc);
                        if (mCurrentEndcBearStatus != EndcBearController.this.mLastEndcBearStatus) {
                            EndcBearController endcBearController5 = EndcBearController.this;
                            endcBearController5.updateEndcBearInfo(endcBearController5.mLastEndcBearStatus);
                        }
                        boolean unused12 = EndcBearController.this.mLastEndcBearStatus = mCurrentEndcBearStatus;
                        return;
                    }
                    return;
                case 8:
                    EndcBearController.this.log("ACTION_DISABLE_ENDC_TIMER_OUT");
                    EndcBearController endcBearController6 = EndcBearController.this;
                    endcBearController6.enableEndc(endcBearController6.mDefaultDataSlotId, false);
                    boolean unused13 = EndcBearController.this.mLastState = false;
                    EndcBearController.access$2108(EndcBearController.this);
                    boolean unused14 = EndcBearController.this.mIsDisableEndcTimerOut = true;
                    PendingIntent unused15 = EndcBearController.this.mDisEdncTimerAlarmIntent = null;
                    return;
                case 9:
                    int mPhoneId3 = intent.getIntExtra("PhoneId", -1);
                    EndcBearController.this.log("ACTION_COMMAND_FORCE_DISABLE_ENDC phoneid= " + mPhoneId3);
                    if (mPhoneId3 == EndcBearController.this.mDefaultDataSlotId && EndcBearController.this.mPhone != null) {
                        try {
                            if (!EndcBearController.this.mPhone.is_test_card()) {
                                String currentOperatorNumeric = EndcBearController.this.mPhone.getServiceState().getOperatorNumeric();
                                if (!TextUtils.isEmpty(currentOperatorNumeric) && !currentOperatorNumeric.startsWith("460")) {
                                    return;
                                }
                                boolean unused16 = EndcBearController.this.isNeedForceDisableEndc = true;
                                EndcBearController.this.log("start FORCE_DISABLE_ENDC_TIME_OUT in second is  " + EndcBearController.this.FORCE_DISABLE_ENDC_TIMER_SEC);
                                EndcBearController.this.mHandler.removeMessages(1009);
                                EndcBearController.this.mHandler.sendEmptyMessageDelayed(1009, EndcBearController.this.FORCE_DISABLE_ENDC_TIMER_SEC * EndcBearController.mSecondMs);
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
                default:
                    EndcBearController.this.log("Unexpected broadcast intent: " + intent);
                    return;
            }
            EndcBearController.this.actionWhenStateChange();
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsAnySceneNeedProhibit = false;
    /* access modifiers changed from: private */
    public boolean mIsCharging = false;
    /* access modifiers changed from: private */
    public boolean mIsDisableEndcTimerOut = false;
    /* access modifiers changed from: private */
    public boolean mIsInLargeFileDownload = false;
    private boolean mIsMobileDataOn = true;
    private boolean mIsServiceBound;
    /* access modifiers changed from: private */
    public boolean mIsWifihotspotOn = false;
    /* access modifiers changed from: private */
    public boolean mLargeFileProhibitEnable = true;
    private long mLastDataStallTime = 0;
    private long mLastEndcBearStatsTime = 0;
    /* access modifiers changed from: private */
    public boolean mLastEndcBearStatus = false;
    private long mLastEndcSettingChangeTime = 0;
    private long mLastEnterLteJamTime = 0;
    private long mLastKeylogRptTime = 0;
    private long mLastScenesKeylogRptTime = 0;
    /* access modifiers changed from: private */
    public boolean mLastState = false;
    private long mLastincrementPs = Long.MAX_VALUE;
    private int mLightOptBatThresAsLow = 20;
    private boolean mLightOptLowBatEnable = true;
    private int mLightOptLowBatSpeedThres = MtkRuimRecords.PHB_DELAY_SEND_TIME;
    private ContentObserver mLightOptParaObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass5 */

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
    private int mLowBatStatsAvgSpeedThres = MtkApnTypes.WAP;
    /* access modifiers changed from: private */
    public boolean mLteJam = false;
    private int mLteJamCnt = 0;
    private long mLteJamDuration = 0;
    private int mLteQualityBuffCnt = 0;
    private int[] mLteRxSpeedBuffer = new int[7];
    private int[] mLteSingalQualityBuffer = new int[7];
    private boolean mNeedRewardDisEndcTimer = false;
    /* access modifiers changed from: private */
    public int mNetworkMode = Phone.PREFERRED_NT_MODE;
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
    private int mPoorLteBwKhzThres = 1400;
    private int mPoorLteRsrpThres = -115;
    private int mPoorLteRsrqThres = -15;
    private ContentObserver mPrefNetworkModeObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass6 */

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
    private long[][] mScenesKeyInfo = {new long[]{0, 0, 0, 0, 0}, new long[]{0, 0, 0, 0, 0}, new long[]{0, 0, 0, 0, 0}, new long[]{0, 0, 0, 0, 0}};
    private int mScenesNeedProhibitMsks = 0;
    private long[] mScenesNrNotUsedBytesStats = {0, 0, 0, 0};
    private long[] mScenesNrNotUsedStartTimeStats = {0, 0, 0, 0};
    private long[] mScenesNrUsedBytesStats = {0, 0, 0, 0};
    private long[] mScenesNrUsedStartTimeStats = {0, 0, 0, 0};
    private int mScenesProhibitEnableMsks = ((((((this.mVideoProhibitEnable << 0) | (this.mVideoCallProhibitEnable << 1)) | (this.mGameProhibitEnable << 2)) | (this.mVideoLiveProhibitEnable << 3)) | (this.mDownloadProhibitEnable << 4)) | (this.mHolidayProhibitEnable << 5));
    private long[] mScenesStartTimeStats = {0, 0, 0, 0};
    private int mScenesStatusMsks = 0;
    private int mScgAddStatsAvgSpeedThres = 1024;
    private int mScgFailStatsAvgSpeedThres = 20;
    private boolean mScreenoffOnlyEnable = false;
    private boolean mSib2NoNrEnable = false;
    private boolean mSib2UpLayerInd = true;
    private boolean mStartFlowIsNR = false;
    /* access modifiers changed from: private */
    public final SubscriptionManager mSubscriptionManager;
    private ContentObserver mSwitchObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.EndcBearController.AnonymousClass2 */

        public void onChange(boolean selfChange) {
            EndcBearController.this.actionWhenDdsOrSwitchChange();
        }
    };
    private int mVideoCallProhibitEnable = 1;
    private long[] mVideoCallSceneKeyInfo = {0, 0, 0, 0, 0};
    private int mVideoLiveProhibitEnable = 1;
    private long[] mVideoLiveSceneKeyInfo = {0, 0, 0, 0, 0};
    private int mVideoProhibitEnable = 1;
    private long[] mVideoSceneKeyInfo = {0, 0, 0, 0, 0};
    private WifiManager mWifiManager;

    static /* synthetic */ int access$2108(EndcBearController x0) {
        int i = x0.mDisEndcTimerOutCnt;
        x0.mDisEndcTimerOutCnt = i + 1;
        return i;
    }

    private EndcBearController(Context context) {
        log("EndcBearController creat");
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
        filter.addAction(ACTION_DOWNLOAD);
        filter.addAction(ACTION_INFORM_LTE_QUALITY);
        filter.addAction(ACTION_INFORM_RAT_CHANGED);
        filter.addAction(ACTION_DISABLE_ENDC_TIMER_OUT);
        filter.addAction(ACTION_COMMAND_FORCE_DISABLE_ENDC);
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        registerForSwitchState();
        registerForParaSettings();
        registerForLightSwitchState();
        registerForLightOptParaSettings();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager != null) {
            this.mIsWifihotspotOn = wifiManager.getWifiApState() == 13;
            log("initial mIsWifihotspotOn: " + this.mIsWifihotspotOn);
        }
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        this.mDefaultDataSlotId = this.mSubscriptionManager.getDefaultDataPhoneId();
        SubscriptionManager subscriptionManager = this.mSubscriptionManager;
        if (SubscriptionManager.isValidPhoneId(this.mDefaultDataSlotId)) {
            this.mPhone = PhoneFactory.getPhone(this.mDefaultDataSlotId);
            Phone phone = this.mPhone;
            if (phone != null) {
                phone.mCi.registerForOn(this.mHandler, 1003, (Object) null);
                this.mIsCharging = isDeviceCharging();
                log("initial mIsCharging =  " + this.mIsCharging);
                registerPrefNetworkModeObserver();
                this.mPhone.registerForServiceStateChanged(this.mHandler, 1011, (Object) null);
            }
        }
        this.isSwitchOn = getDdsSmartfivegSwitch();
        if (isBuildTypeNeedCloseLightOptByDefault()) {
            this.isLightSwitchOn = false;
            initLightSmartfivegSwitchConfig(this.isLightSwitchOn, true);
        } else {
            initLightSmartfivegSwitchConfig(this.isLightSwitchOn, false);
        }
        this.isLightSwitchOn = getLightSmartfivegSwitch();
        updateParaFromSettings(true);
        updateLightOptParaFromSettings(true);
        this.mLastEndcBearStatsTime = SystemClock.elapsedRealtime() / mSecondMs;
        long j = this.mLastEndcBearStatsTime;
        this.mLastKeylogRptTime = j;
        this.mLastEndcSettingChangeTime = j;
        log("initial mLastEndcBearStatsTime and mLastKeylogRptTime =  " + this.mLastEndcBearStatsTime);
        this.mLastScenesKeylogRptTime = SystemClock.elapsedRealtime() / mSecondMs;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
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
    public void log(String s) {
        if (this.Debug) {
            Rlog.d("EndcBearController", s);
        }
    }

    private boolean getDdsSmartfivegSwitch() {
        int mSwitch = Settings.Global.getInt(this.mContext.getContentResolver(), "smart_fiveg", this.mDefalutEnable);
        log("getDdsSmartfivegSwitch smart_fiveg setting is  " + mSwitch);
        Phone phone = this.mPhone;
        if (phone != null) {
            int mSubId = phone.getSubId();
            if (SubscriptionManager.isValidSubscriptionId(mSubId)) {
                ContentResolver contentResolver = this.mContext.getContentResolver();
                mSwitch = Settings.Global.getInt(contentResolver, "smart_fiveg" + mSubId, this.mDefalutEnable);
                log("getDdsSmartfivegSwitch smart_fiveg on DDS subid " + mSubId + "setting is  " + mSwitch);
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
    public void actionWhenLightSwitchChange() {
        boolean mBackupIsSwitchOn = this.isLightSwitchOn;
        this.isLightSwitchOn = getLightSmartfivegSwitch();
        informWorkState(isWorkingState());
        if (this.isSwitchOn || this.isNeedForceDisableEndc) {
            log("smart5g switch is on " + this.isSwitchOn + "or forcedisable " + this.isNeedForceDisableEndc);
            return;
        }
        boolean z = this.isLightSwitchOn;
        if (mBackupIsSwitchOn == z) {
            return;
        }
        if (z) {
            startDataStall();
            return;
        }
        stopDisableEndcAlarmTimer();
        enableEndc(this.mDefaultDataSlotId, true);
        this.mLastState = true;
        stopDataStall();
    }

    /* access modifiers changed from: private */
    public void actionWhenDdsOrSwitchChange() {
        boolean mBackupIsSwitchOn = this.isSwitchOn;
        this.isSwitchOn = getDdsSmartfivegSwitch();
        informWorkState(isWorkingState());
        boolean z = this.isSwitchOn;
        if (mBackupIsSwitchOn != z) {
            if (!this.isNeedForceDisableEndc) {
                if (z) {
                    startDataStall();
                } else {
                    stopDisableEndcAlarmTimer();
                    enableEndc(this.mDefaultDataSlotId, true);
                    this.mLastState = true;
                    stopDataStall();
                    if (this.isLightSwitchOn) {
                        startDataStall();
                    }
                }
            }
            reportEndcPwrKeylog(mBackupIsSwitchOn, 1, this.isNrBearAlloc);
        }
    }

    private void registerForSwitchState() {
        log("registerForSwitchState");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("smart_fiveg"), false, this.mSwitchObserver);
    }

    private void registerForParaSettings() {
        log("registerForParaSettings");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("EndcLowPwrPara"), false, this.mParaObserver);
    }

    private void registerForLightSwitchState() {
        log("registerForLightSwitchState");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("light_smart_fiveg"), false, this.mLightSwitchObserver);
    }

    private void registerForLightOptParaSettings() {
        log("registerForLightOptParaSettings");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("LightEndcLowPwrPara"), false, this.mLightOptParaObserver);
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
        log("on5gStatus enableStatus: " + enableStatus + ", isNrEnabled: " + this.isNrEnabled + ", isSwitchOn: " + this.isSwitchOn);
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
    public void update5gState(int networkType) {
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
            case IWorldPhone.EVENT_REG_SUSPENDED_2 /*{ENCODED_INT: 31}*/:
            case 32:
            case 33:
                isEnabled = true;
                break;
        }
        on5gStatus(isEnabled);
    }

    /* access modifiers changed from: private */
    public void registerPrefNetworkModeObserver() {
        int subId = this.mPhone.getSubId();
        unregisterPrefNetworkModeObserver();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            contentResolver.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + subId), true, this.mPrefNetworkModeObserver);
        }
    }

    /* access modifiers changed from: private */
    public void unregisterPrefNetworkModeObserver() {
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mPrefNetworkModeObserver);
    }

    /* access modifiers changed from: private */
    public int getNetworkModeFromDB() {
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
    public void query5gState(int slotId) {
        log("query5gState: begin");
        if ((524288 & this.mPhone.getRadioAccessFamily()) > 0) {
            this.isNrEnabled = true;
        } else {
            this.isNrEnabled = false;
        }
        log("query5gState: end " + this.isNrEnabled);
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
    public void queryEndcState(int slotId) {
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

    public void enableEndc(int slotId, boolean enabled) {
        String atSetEndcDeactState;
        log("enableEndc: begin enabled = " + enabled);
        if ((enabled ? 0 : 1) == 0) {
            atSetEndcDeactState = "AT+EGMC=1,\"endc_deactivation\",0,0";
        } else {
            atSetEndcDeactState = "AT+EGMC=1,\"endc_deactivation\",1,0";
        }
        try {
            String[] cmdSetEndcDeactStateStr = {atSetEndcDeactState, ""};
            if (this.mPhone != null) {
                this.mPhone.invokeOemRilRequestStrings(cmdSetEndcDeactStateStr, (Message) null);
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
        log("updateAnyNrBearerAllocationStatus: begin enStatus is " + enStatus);
        if (this.mDefaultDataSlotId != slotId || this.isNrBearAlloc == enStatus) {
            return;
        }
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
    }

    public void updateSib2UpLayerInd(int slotId, boolean enStatus) {
        log("updateSib2UpLayerInd: begin enStatus is " + enStatus);
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
            boolean z = true;
            switch (msg.what) {
                case EndcBearController.EVENT_CMD_CONNECT_EXT_TELEPHONY_SERV /*{ENCODED_INT: 1000}*/:
                    EndcBearController.this.log("Event received EVENT_CMD_CONNECT_EXT_TELEPHONY_SERV");
                    return;
                case 1001:
                    EndcBearController.this.log("EVENT_FLOW_CHANGE");
                    EndcBearController.this.flowChange();
                    return;
                case ExternalSimConstants.MSG_ID_GET_PLATFORM_CAPABILITY_RESPONSE /*{ENCODED_INT: 1002}*/:
                default:
                    return;
                case 1003:
                    EndcBearController.this.log("EVENT_RADIO_ON");
                    EndcBearController.this.mHandler.removeMessages(1004);
                    EndcBearController.this.mHandler.sendEmptyMessage(1004);
                    return;
                case 1004:
                    EndcBearController.this.log("EVENT_QUERY_STATE");
                    EndcBearController endcBearController = EndcBearController.this;
                    endcBearController.query5gState(endcBearController.mDefaultDataSlotId);
                    EndcBearController endcBearController2 = EndcBearController.this;
                    endcBearController2.queryEndcState(endcBearController2.mDefaultDataSlotId);
                    return;
                case 1005:
                    boolean unused = EndcBearController.this.mIsInLargeFileDownload = false;
                    if (EndcBearController.this.mLargeFileProhibitEnable) {
                        EndcBearController.this.actionWhenStateChange();
                        return;
                    }
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
                                    EndcBearController.this.log("mScgDeactState = " + mScgDeactState);
                                }
                            } catch (Exception e) {
                                EndcBearController.this.log("Exception happen!");
                            }
                            if (mScgDeactState == 0 || mScgDeactState == 1) {
                                EndcBearController endcBearController3 = EndcBearController.this;
                                if (mScgDeactState == 1) {
                                    z = false;
                                }
                                boolean unused2 = endcBearController3.mLastState = z;
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
                    boolean mIsAnySceneNeedProhibitBack = EndcBearController.this.mIsAnySceneNeedProhibit;
                    AsyncResult asyncResult = (AsyncResult) msg.obj;
                    int mUpdateSceneType = msg.arg1;
                    int mUpdateSceneAction = msg.arg2;
                    EndcBearController.this.log("EVENT_SCENE_STATE_CHANGED mUpdateSceneType " + mUpdateSceneType + " mUpdateSceneAction " + mUpdateSceneAction);
                    EndcBearController.this.updateSceneStates(mUpdateSceneType, mUpdateSceneAction);
                    EndcBearController.this.log("EVENT_SCENE_STATE_CHANGED old prohibit " + mIsAnySceneNeedProhibitBack + " new prohibit " + EndcBearController.this.mIsAnySceneNeedProhibit);
                    if (mIsAnySceneNeedProhibitBack != EndcBearController.this.mIsAnySceneNeedProhibit) {
                        EndcBearController.this.actionWhenStateChange();
                        return;
                    }
                    return;
                case 1008:
                    int mCurrentNwMode = ((Integer) msg.obj).intValue();
                    EndcBearController.this.log("EVENT_NETWORK_MODE_CHANGED mode is " + mCurrentNwMode);
                    int unused3 = EndcBearController.this.mNetworkMode = mCurrentNwMode;
                    EndcBearController endcBearController4 = EndcBearController.this;
                    endcBearController4.update5gState(endcBearController4.mNetworkMode);
                    return;
                case 1009:
                    boolean unused4 = EndcBearController.this.isNeedForceDisableEndc = false;
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
                                boolean unused5 = EndcBearController.this.is5gIcon = true;
                            } else {
                                boolean unused6 = EndcBearController.this.is5gIcon = false;
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
                    EndcBearController endcBearController5 = EndcBearController.this;
                    endcBearController5.updateLteJamDurationTime(endcBearController5.mLteJam, false);
                    boolean unused7 = EndcBearController.this.mLteJam = false;
                    return;
                case 1013:
                    EndcBearController.this.log("EVENT_ENABLE_ENDC_HYSTERESIS_EXP ");
                    return;
            }
        }
    }

    private int getStatsSpeedThreshold() {
        int mSpeedThres = 0;
        if (this.isSwitchOn) {
            if (this.mStartFlowIsNR) {
                mSpeedThres = this.mScgFailStatsAvgSpeedThres;
            } else {
                mSpeedThres = this.mScgAddStatsAvgSpeedThres;
                if (this.mIsDisableEndcTimerOut && this.DISABLE_ENDC_DELAY_MS == this.mCurrentDisableEndcTimerLength) {
                    mSpeedThres = this.mPunishScgAddStatsAvgSpeedThres;
                }
            }
        } else if (this.isLightSwitchOn && this.mLightOptLowBatEnable && this.mCurrBatteryLevel < this.mLightOptBatThresAsLow) {
            mSpeedThres = this.mLightOptLowBatSpeedThres;
        }
        log("SpeedThreshold is " + mSpeedThres);
        return mSpeedThres;
    }

    private boolean needStatsSpeed() {
        boolean mNeedFlg = false;
        if (this.isNeedForceDisableEndc) {
            log("is forceDisable needStatsSpeed is " + false);
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

    /* access modifiers changed from: private */
    public void flowChange() {
        long totalTxBytes = TrafficStats.getMobileTxBytes();
        long totalRxBytes = TrafficStats.getMobileRxBytes();
        long incrementTxBytes = totalTxBytes - this.mOldTxBytes;
        long incrementRxBytes = totalRxBytes - this.mOldRxBytes;
        this.mLastincrementPs = Long.MAX_VALUE;
        long incrementBytes = incrementRxBytes > incrementTxBytes ? incrementRxBytes : incrementTxBytes;
        boolean mDisableEndcLater = false;
        log("flowChange mOldTxBytes: " + this.mOldTxBytes + ", totalTxBytes: " + totalTxBytes + ", mOldRxBytes: " + this.mOldRxBytes + ", totalRxBytes: " + totalRxBytes);
        long usedTime = (SystemClock.elapsedRealtime() - this.mLastDataStallTime) / mSecondMs;
        StringBuilder sb = new StringBuilder();
        sb.append("flowChange incrementBytes: ");
        sb.append(incrementBytes);
        sb.append(", usedTime: ");
        sb.append(usedTime);
        log(sb.toString());
        if (usedTime > 0) {
            long incrementPs = incrementBytes / (STANDARD_VALUE * usedTime);
            log("incrementPs = " + incrementPs + "K, mLastState = " + this.mLastState);
            this.mLastincrementPs = incrementPs;
            int currentSpeedThres = getStatsSpeedThreshold();
            if (this.mStartFlowIsNR && incrementPs >= ((long) this.mRewardDisEndcTimerStatsAvgSpeedThres)) {
                this.mNeedRewardDisEndcTimer = true;
            }
            updateLteQuality(this.mLastincrementPs);
            boolean lteDataJam = isLteDataJam();
            log("flowChange mStartFlowIsNR is " + this.mStartFlowIsNR + " mNeedRewardDisEndcTimer is " + this.mNeedRewardDisEndcTimer + "lteDataJam is " + lteDataJam);
            if (currentSpeedThres == 0 || incrementPs > ((long) currentSpeedThres) || this.isLtePoor || lteDataJam) {
                stopDisableEndcAlarmTimer();
                if (!this.mLastState) {
                    enableEndc(this.mDefaultDataSlotId, true);
                    this.mLastState = true;
                }
            } else if (!isEnableEndcHysTimerRun() && this.mLastState) {
                if (!this.isNrBearAlloc) {
                    stopDisableEndcAlarmTimer();
                    enableEndc(this.mDefaultDataSlotId, false);
                    this.mLastState = false;
                } else {
                    mDisableEndcLater = true;
                }
            }
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
        return (!this.mChargingEnable && this.mIsCharging) || (!this.mHotspotEnable && this.mIsWifihotspotOn) || ((!this.mSib2NoNrEnable && !this.mSib2UpLayerInd && this.isScreenOn && !this.is5gIcon) || isSpeicSimNeedCloseSmartFiveG() || ((this.isScreenOn && this.isSwitchOn && this.mScreenoffOnlyEnable) || ((this.mLargeFileProhibitEnable && this.mIsInLargeFileDownload) || this.mIsAnySceneNeedProhibit)));
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
    public void actionWhenStateChange() {
        log("actionWhenStateChange isNeedForceDisableEndc: " + this.isNeedForceDisableEndc + ", mIsCharging: " + this.mIsCharging + ", mIsWifihotspotOn: " + this.mIsWifihotspotOn + ", mSib2UpLayerInd: " + this.mSib2UpLayerInd + ", isScreenOn: " + this.isScreenOn + ", isNrBearAlloc: " + this.isNrBearAlloc);
        if (this.isNeedForceDisableEndc) {
            if (this.mLastState) {
                stopDisableEndcAlarmTimer();
                stopDataStall();
                enableEndc(this.mDefaultDataSlotId, false);
                this.mLastState = false;
            }
            if (this.isScreenOn) {
                reportEndcPwrKeylog(this.isSwitchOn, 2, this.isNrBearAlloc);
                return;
            }
            return;
        }
        if (isEndcPwrOptProhibited()) {
            stopDisableEndcAlarmTimer();
            stopDataStall();
            if (!this.mLastState) {
                enableEndc(this.mDefaultDataSlotId, true);
                this.mLastState = true;
            }
        } else if (!this.isScreenOn) {
            stopDataStall();
            if (isWorkingState() && (this.isSwitchOn || (this.isLightSwitchOn && this.mLightOptScreenOffEnable && this.mLastincrementPs <= ((long) this.mLightOptScreenOffSpeedThres)))) {
                if (!this.isNrBearAlloc) {
                    stopDisableEndcAlarmTimer();
                    enableEndc(this.mDefaultDataSlotId, false);
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
                    enableEndc(this.mDefaultDataSlotId, true);
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
            reportScenesKeyLogInfo();
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
        sb.append("stats_duration=");
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
        sb.append(";download_size_thres=");
        sb.append(this.mDownloadSizeThres);
        sb.append(";scenes_prohibit_masks=");
        sb.append(this.mScenesProhibitEnableMsks);
        sb.append(";");
        String mEndcLowPwrPara = sb.toString();
        log("Initial EndcLowPwrPara is " + mEndcLowPwrPara);
        Settings.Global.putString(this.mContext.getContentResolver(), "EndcLowPwrPara", mEndcLowPwrPara);
    }

    /* JADX INFO: Multiple debug info for r7v25 int: [D('mPoorLteRsrpThresBack' int), D('mLowBatAvgSpeedArray' java.lang.String[])] */
    /* JADX INFO: Multiple debug info for r10v10 boolean: [D('mPoorLteBwThresArray' java.lang.String[]), D('mLargeFileProhibitEnableBack' boolean)] */
    /* access modifiers changed from: private */
    public void updateParaFromSettings(boolean isInitial) {
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
        String mEndcLowPara = Settings.Global.getString(this.mContext.getContentResolver(), "EndcLowPwrPara");
        if (mEndcLowPara != null) {
            log("updateParaFromSettings mEndcLowPara is " + mEndcLowPara);
            String[] mParaArray = mEndcLowPara.split(";");
            if (18 != mParaArray.length) {
                log("updateParaFromSettings length is  " + mParaArray.length);
                initParaInSettings();
                return;
            }
            if (mParaArray[0].contains("stats_duration")) {
                String[] mDurationArray = mParaArray[0].split("=");
                if (2 == mDurationArray.length && isNumeric(mDurationArray[1])) {
                    int mTempPara16 = Integer.parseInt(mDurationArray[1]);
                    if (mTempPara16 >= 2 && mTempPara16 <= 10) {
                        this.FLOW_CHANGE_STATS_DURATION_MS = ((long) mTempPara16) * mSecondMs;
                    }
                }
            }
            if (mParaArray[1].contains("scg_add_speed")) {
                String[] mAvgSpeedArray = mParaArray[1].split("=");
                if (2 == mAvgSpeedArray.length && isNumeric(mAvgSpeedArray[1]) && (mTempPara15 = Integer.parseInt(mAvgSpeedArray[1])) >= 0 && mTempPara15 <= ENDC_PWR_CFG_STATS_AVG_SPEED_MAX) {
                    this.mScgAddStatsAvgSpeedThres = mTempPara15;
                }
            }
            if (mParaArray[2].contains("scg_fail_speed")) {
                String[] mAvgSpeedArray2 = mParaArray[2].split("=");
                if (2 == mAvgSpeedArray2.length && isNumeric(mAvgSpeedArray2[1]) && (mTempPara14 = Integer.parseInt(mAvgSpeedArray2[1])) >= 0 && mTempPara14 <= ENDC_PWR_CFG_STATS_AVG_SPEED_MAX) {
                    this.mScgFailStatsAvgSpeedThres = mTempPara14;
                }
            }
            if (mParaArray[3].contains("dis_endc_timer")) {
                String[] mDisEndcTimerArray = mParaArray[3].split("=");
                if (2 == mDisEndcTimerArray.length && isNumeric(mDisEndcTimerArray[1]) && (mTempPara13 = Integer.parseInt(mDisEndcTimerArray[1])) >= 0 && mTempPara13 <= ENDC_PWR_CFG_STATS_DIS_ENDC_TIMER_MAX) {
                    if (mTempPara13 == 20) {
                        this.DISABLE_ENDC_DELAY_MS = 86400000;
                    } else if (mTempPara13 == 0) {
                        this.DISABLE_ENDC_DELAY_MS = 86400000;
                    } else {
                        this.DISABLE_ENDC_DELAY_MS = ((long) mTempPara13) * mSecondMs;
                    }
                }
            }
            if (mParaArray[4].contains("reward_scg_fail_speed")) {
                String[] mAvgSpeedArray3 = mParaArray[4].split("=");
                if (2 == mAvgSpeedArray3.length && isNumeric(mAvgSpeedArray3[1]) && (mTempPara12 = Integer.parseInt(mAvgSpeedArray3[1])) >= 0 && mTempPara12 <= ENDC_PWR_CFG_STATS_AVG_SPEED_MAX) {
                    this.mRewardDisEndcTimerStatsAvgSpeedThres = mTempPara12;
                }
            }
            if (mParaArray[5].contains("reward_dis_endc_timer")) {
                String[] mDisEndcTimerArray2 = mParaArray[5].split("=");
                if (2 == mDisEndcTimerArray2.length && isNumeric(mDisEndcTimerArray2[1]) && (mTempPara11 = Integer.parseInt(mDisEndcTimerArray2[1])) >= 0 && mTempPara11 <= ENDC_PWR_CFG_STATS_DIS_ENDC_TIMER_MAX) {
                    if (mTempPara11 == 60) {
                        this.REWARD_DISABLE_ENDC_DELAY_MS = 86400000;
                    } else if (mTempPara11 == 0) {
                        this.REWARD_DISABLE_ENDC_DELAY_MS = 86400000;
                    } else {
                        this.REWARD_DISABLE_ENDC_DELAY_MS = ((long) mTempPara11) * mSecondMs;
                    }
                }
            }
            if (mParaArray[6].contains("charging_enable")) {
                String[] mChargEnableArray = mParaArray[6].split("=");
                if (2 == mChargEnableArray.length && isNumeric(mChargEnableArray[1]) && ((mTempPara10 = Integer.parseInt(mChargEnableArray[1])) == 0 || mTempPara10 == 1)) {
                    this.mChargingEnable = mTempPara10 == 1;
                }
            }
            if (mParaArray[7].contains("hotspot_enable")) {
                String[] mHotspotEnableArray = mParaArray[7].split("=");
                if (2 == mHotspotEnableArray.length && isNumeric(mHotspotEnableArray[1]) && ((mTempPara9 = Integer.parseInt(mHotspotEnableArray[1])) == 0 || mTempPara9 == 1)) {
                    this.mHotspotEnable = mTempPara9 == 1;
                }
            }
            if (mParaArray[8].contains("sib_no_nr_enable")) {
                String[] mSibNoNrEnableArray = mParaArray[8].split("=");
                if (2 == mSibNoNrEnableArray.length && isNumeric(mSibNoNrEnableArray[1]) && ((mTempPara8 = Integer.parseInt(mSibNoNrEnableArray[1])) == 0 || mTempPara8 == 1)) {
                    this.mSib2NoNrEnable = mTempPara8 == 1;
                }
            }
            if (mParaArray[9].contains("screenoff_only_enable")) {
                String[] mScreenOffEnableArray = mParaArray[9].split("=");
                if (2 == mScreenOffEnableArray.length && isNumeric(mScreenOffEnableArray[1]) && ((mTempPara7 = Integer.parseInt(mScreenOffEnableArray[1])) == 0 || mTempPara7 == 1)) {
                    this.mScreenoffOnlyEnable = mTempPara7 == 1;
                }
            }
            if (mParaArray[10].contains("lowbat_heavy_enable")) {
                String[] mLowBatEnableArray = mParaArray[10].split("=");
                if (2 == mLowBatEnableArray.length && isNumeric(mLowBatEnableArray[1]) && ((mTempPara6 = Integer.parseInt(mLowBatEnableArray[1])) == 0 || mTempPara6 == 1)) {
                    this.mLowBatHeavyOptEnable = mTempPara6 == 1;
                }
            }
            if (mParaArray[11].contains("lowbat_thres")) {
                String[] mLowBatThresArray = mParaArray[11].split("=");
                if (2 == mLowBatThresArray.length && isNumeric(mLowBatThresArray[1]) && (mTempPara5 = Integer.parseInt(mLowBatThresArray[1])) <= 30) {
                    this.mBatThresAsLow = mTempPara5;
                }
            }
            if (mParaArray[12].contains("lowbat_stats_avg_speed")) {
                String[] mLowBatAvgSpeedArray = mParaArray[12].split("=");
                if (2 == mLowBatAvgSpeedArray.length && isNumeric(mLowBatAvgSpeedArray[1]) && (mTempPara4 = Integer.parseInt(mLowBatAvgSpeedArray[1])) >= 0 && mTempPara4 <= ENDC_PWR_CFG_STATS_AVG_SPEED_MAX) {
                    this.mLowBatStatsAvgSpeedThres = mTempPara4;
                }
            }
            int mPoorLteRsrpThresBack = this.mPoorLteRsrpThres;
            int mPoorLteRsrqThresBack = this.mPoorLteRsrqThres;
            int mPoorLteBwKhzThresBack = this.mPoorLteBwKhzThres;
            if (mParaArray[13].contains("poorlte_rsrp_thres")) {
                String[] mPoorLteRsrpThresArray = mParaArray[13].split("=");
                if (2 == mPoorLteRsrpThresArray.length && (mTempPara3 = Integer.parseInt(mPoorLteRsrpThresArray[1])) >= ENDC_PWR_CFG_POOR_LTE_RSRP_THRES_MIN && mTempPara3 <= 0) {
                    this.mPoorLteRsrpThres = mTempPara3;
                }
            }
            if (mParaArray[14].contains("poorlte_rsrq_thres")) {
                String[] mPoorLteRsrqThresArray = mParaArray[14].split("=");
                if (2 == mPoorLteRsrqThresArray.length && (mTempPara2 = Integer.parseInt(mPoorLteRsrqThresArray[1])) >= -40 && mTempPara2 <= 0) {
                    this.mPoorLteRsrqThres = mTempPara2;
                }
            }
            if (mParaArray[15].contains("poorlte_bw_thres")) {
                String[] mPoorLteBwThresArray = mParaArray[15].split("=");
                if (2 == mPoorLteBwThresArray.length && isNumeric(mPoorLteBwThresArray[1]) && (mTempPara = Integer.parseInt(mPoorLteBwThresArray[1])) >= 0 && mTempPara <= ENDC_PWR_CFG_POOR_LTE_BW_THRES_MAX) {
                    this.mPoorLteBwKhzThres = mTempPara;
                }
            }
            boolean mLargeFileProhibitEnableBack = this.mLargeFileProhibitEnable;
            if (mParaArray[16].contains("download_size_thres")) {
                String[] mDownloadSizeThresArray = mParaArray[16].split("=");
                if (2 == mDownloadSizeThresArray.length && isNumeric(mDownloadSizeThresArray[1])) {
                    int mTempPara17 = Integer.parseInt(mDownloadSizeThresArray[1]);
                    if (mTempPara17 == 0) {
                        this.mLargeFileProhibitEnable = false;
                    } else {
                        this.mDownloadSizeThres = mTempPara17;
                        this.mLargeFileProhibitEnable = true;
                    }
                }
            }
            int i = this.mScenesProhibitEnableMsks;
            if (mParaArray[14].contains("scenes_prohibit_masks")) {
                String[] mScenesProMasksArray = mParaArray[14].split("=");
                if (2 == mScenesProMasksArray.length && isNumeric(mScenesProMasksArray[1])) {
                    this.mScenesProhibitEnableMsks = Integer.parseInt(mScenesProMasksArray[1]);
                }
            }
            if (!(mPoorLteRsrpThresBack == this.mPoorLteRsrpThres && mPoorLteRsrqThresBack == this.mPoorLteRsrqThres && mPoorLteBwKhzThresBack == this.mPoorLteBwKhzThres)) {
                Smart5gInfoLtePoorThresConfig(this.mPoorLteRsrpThres, this.mPoorLteRsrqThres, this.mPoorLteBwKhzThres);
            }
            log("updateParaFromSettings FLOW_CHANGE_STATS_DURATION_MS: " + this.FLOW_CHANGE_STATS_DURATION_MS + ", mScgAddStatsAvgSpeedThres: " + this.mScgAddStatsAvgSpeedThres + ", mScgFailStatsAvgSpeedThres: " + this.mScgFailStatsAvgSpeedThres + ", DISABLE_ENDC_DELAY_MS: " + this.DISABLE_ENDC_DELAY_MS + ", reward_scg_fail_speed: " + this.mRewardDisEndcTimerStatsAvgSpeedThres + ", REWARD_DISABLE_ENDC_DELAY_MS: " + this.REWARD_DISABLE_ENDC_DELAY_MS + ", mChargingEnable: " + this.mChargingEnable + ", mHotspotEnable: " + this.mHotspotEnable + ", mSib2NoNrEnable: " + this.mSib2NoNrEnable + ", screenoff_only_enable: " + this.mScreenoffOnlyEnable + ", lowbat_heavy_enable: " + this.mLowBatHeavyOptEnable + ", lowbat_thres: " + this.mBatThresAsLow + ", lowbat_stats_avg_speed: " + this.mLowBatStatsAvgSpeedThres + ", poorlte_rsrp_thres: " + this.mPoorLteRsrpThres + ", poorlte_rsrq_thres: " + this.mPoorLteRsrqThres + ", poorlte_bw_thres: " + this.mPoorLteBwKhzThres + ", download_size_thres: " + this.mDownloadSizeThres);
            if (isInitial) {
                return;
            }
            if (mChargingEnableBackup != this.mChargingEnable || mHotspotEnableBackup != this.mHotspotEnable || mSib2NoNrEnableBackup != this.mSib2NoNrEnable || mScreenoffOnlyEnableBackup != this.mScreenoffOnlyEnable || mLargeFileProhibitEnableBack != this.mLargeFileProhibitEnable) {
                actionWhenStateChange();
                return;
            }
            return;
        }
        log("updateParaFromSettings no EndcLowPara setting");
        if (isInitial) {
            initParaInSettings();
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
        String mLightEndcLowPwrPara = "screenoff_speed=" + this.mLightOptScreenOffSpeedThres + ";lowbat_speed=" + this.mLightOptLowBatSpeedThres + ";lowbat_thres=" + this.mLightOptBatThresAsLow + ";dis_endc_timer=" + mDisEndcTimerInDb;
        log("Initial LightEndcLowPwrPara is " + mLightEndcLowPwrPara);
        Settings.Global.putString(this.mContext.getContentResolver(), "LightEndcLowPwrPara", mLightEndcLowPwrPara);
    }

    /* access modifiers changed from: private */
    public void updateLightOptParaFromSettings(boolean isInitial) {
        int mTempPara;
        boolean mLightOptScreenOffEnableBackUp = this.mLightOptScreenOffEnable;
        String mLightEndcLowPara = Settings.Global.getString(this.mContext.getContentResolver(), "LightEndcLowPwrPara");
        if (mLightEndcLowPara != null) {
            log("updateLightOptParaFromSettings mLightEndcLowPara is " + mLightEndcLowPara);
            String[] mParaArray = mLightEndcLowPara.split(";");
            if (4 != mParaArray.length) {
                log("updateLightOptParaFromSettings length is  " + mParaArray.length);
                initLightParaInSettings();
                return;
            }
            if (mParaArray[0].contains("screenoff_speed")) {
                String[] mScreenOffArray = mParaArray[0].split("=");
                if (2 == mScreenOffArray.length && isNumeric(mScreenOffArray[1])) {
                    int mTempPara2 = Integer.parseInt(mScreenOffArray[1]);
                    if (mTempPara2 == 0) {
                        this.mLightOptScreenOffEnable = false;
                    } else {
                        this.mLightOptScreenOffEnable = true;
                    }
                    this.mLightOptScreenOffSpeedThres = mTempPara2;
                }
            }
            if (mParaArray[1].contains("lowbat_speed")) {
                String[] mLowBatSpeedArray = mParaArray[1].split("=");
                if (2 == mLowBatSpeedArray.length && isNumeric(mLowBatSpeedArray[1])) {
                    int mTempPara3 = Integer.parseInt(mLowBatSpeedArray[1]);
                    if (mTempPara3 == 0) {
                        this.mLightOptLowBatEnable = false;
                    } else {
                        this.mLightOptLowBatEnable = true;
                    }
                    this.mLightOptLowBatSpeedThres = mTempPara3;
                }
            }
            if (mParaArray[2].contains("lowbat_thres")) {
                String[] mLowBatArray = mParaArray[2].split("=");
                if (2 == mLowBatArray.length && isNumeric(mLowBatArray[1])) {
                    this.mLightOptBatThresAsLow = Integer.parseInt(mLowBatArray[1]);
                }
            }
            if (mParaArray[3].contains("dis_endc_timer")) {
                String[] mDisEndcTimerArray = mParaArray[3].split("=");
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
                return;
            }
            return;
        }
        log("updateLightOptParaFromSettings no LightEndcLowPara setting");
        if (isInitial) {
            initLightParaInSettings();
        }
    }

    private void reportEndcPwrKeylog(boolean reportSwitchOn, int reportCause, boolean currentNrBearAlloc) {
        String str;
        String str2;
        long mCurrentTimeSec;
        StringBuilder sb;
        if (this.mPhone != null) {
            long mCurrentTimeSec2 = SystemClock.elapsedRealtime() / mSecondMs;
            long mDuraTimeSec = mCurrentTimeSec2 - this.mLastEndcBearStatsTime;
            long mEndcSettingTimeSec = mCurrentTimeSec2 - this.mLastEndcSettingChangeTime;
            if (this.mLastEndcBearStatus) {
                str2 = ", mDisableEndcCnt:";
                str = ", mEnableEndcSettingTime:";
                this.mEndcBearDuration += mDuraTimeSec;
            } else {
                str2 = ", mDisableEndcCnt:";
                str = ", mEnableEndcSettingTime:";
                this.mNoEndcBearDuration += mDuraTimeSec;
            }
            if (!this.mLastState) {
                this.mDisableEndcSettingTime += mEndcSettingTimeSec;
            } else {
                this.mEnableEndcSettingTime += mEndcSettingTimeSec;
            }
            this.mLastEndcBearStatsTime = mCurrentTimeSec2;
            this.mLastEndcSettingChangeTime = mCurrentTimeSec2;
            try {
                String log_string = OemTelephonyUtils.getOemRes(this.mContext, "zz_oppo_critical_log_103", "");
                if (log_string.equals("")) {
                    try {
                        log("return for get log_string fail.");
                    } catch (Exception e) {
                        e = e;
                        mCurrentTimeSec = mCurrentTimeSec2;
                        log("reportEndcPwrKeylog error: " + e);
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
                    }
                } else {
                    String[] log_array = log_string.split(",");
                    int log_type = Integer.valueOf(log_array[0]).intValue();
                    String log_desc = log_array[1];
                    try {
                        sb = new StringBuilder();
                        sb.append(",Switch:");
                        sb.append(reportSwitchOn);
                        sb.append(",Cause:");
                        sb.append(reportCause);
                        sb.append(",NrBearAlloc:");
                        sb.append(currentNrBearAlloc);
                        sb.append(", mEndcBearDuration:");
                        sb.append(this.mEndcBearDuration);
                        sb.append(", mNoEndcBearDuration:");
                        sb.append(this.mNoEndcBearDuration);
                        sb.append(", mDisEndcTimerOutCnt:");
                        sb.append(this.mDisEndcTimerOutCnt);
                        sb.append(", mEndcBearConTLev1:");
                        sb.append(this.mEndcBearContinuousTime[0]);
                        sb.append(", mEndcBearConTLev2:");
                        sb.append(this.mEndcBearContinuousTime[1]);
                        sb.append(", mEndcBearConTLev3:");
                        sb.append(this.mEndcBearContinuousTime[2]);
                        sb.append(", mDisableEndcSettingTime:");
                        sb.append(this.mDisableEndcSettingTime);
                        sb.append(str);
                        sb.append(this.mEnableEndcSettingTime);
                        sb.append(str2);
                        sb.append(this.mDisableEndcCnt);
                        sb.append(", mEnableEndcCnt:");
                        sb.append(this.mEnableEndcCnt);
                        sb.append(", mLteJamCnt:");
                    } catch (Exception e2) {
                        e = e2;
                        mCurrentTimeSec = mCurrentTimeSec2;
                        log("reportEndcPwrKeylog error: " + e);
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
                    }
                    try {
                        sb.append(this.mLteJamCnt);
                        sb.append(", mLteJamDuration:");
                        mCurrentTimeSec = mCurrentTimeSec2;
                        try {
                            sb.append(this.mLteJamDuration);
                            OppoManager.writeLogToPartition(log_type, sb.toString(), "NETWORK", ISSUE_SYS_OEM_NW_DIAG_CAUSE_ENDC_PWR_OPT, log_desc);
                            log("after reportEndcPwrKeylog mLastEndcBearStatsTime =  " + this.mLastEndcBearStatsTime + ",mEndcBearDuration =  " + this.mEndcBearDuration + ",mNoEndcBearDuration =  " + this.mNoEndcBearDuration + ",reportSwitchOn =  " + reportSwitchOn + ",mDisEndcTimerOutCnt =  " + this.mDisEndcTimerOutCnt + ", mEndcBearConTLev1:" + this.mEndcBearContinuousTime[0] + ", mEndcBearConTLev2:" + this.mEndcBearContinuousTime[1] + ", mEndcBearConTLev3:" + this.mEndcBearContinuousTime[2] + ", mDisableEndcSettingTime:" + this.mDisableEndcSettingTime + str + this.mEnableEndcSettingTime + str2 + this.mDisableEndcCnt + ", mEnableEndcCnt:" + this.mEnableEndcCnt + ", mLteJamCnt:" + this.mLteJamCnt + ", mLteJamDuration:" + this.mLteJamDuration + ",current_time =  " + (SystemClock.elapsedRealtime() / mSecondMs));
                        } catch (Exception e3) {
                            e = e3;
                        }
                    } catch (Exception e4) {
                        e = e4;
                        mCurrentTimeSec = mCurrentTimeSec2;
                        log("reportEndcPwrKeylog error: " + e);
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
                    }
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
                }
            } catch (Exception e5) {
                e = e5;
                mCurrentTimeSec = mCurrentTimeSec2;
                log("reportEndcPwrKeylog error: " + e);
                this.mEndcBearDuration = 0;
                this.mNoEndcBearDuration = 0;
                this.mDisEndcTimerOutCnt = 0;
                int[] iArr2222 = this.mEndcBearContinuousTime;
                iArr2222[0] = 0;
                iArr2222[1] = 0;
                iArr2222[2] = 0;
                this.mDisableEndcSettingTime = 0;
                this.mEnableEndcSettingTime = 0;
                this.mDisableEndcCnt = 0;
                this.mEnableEndcCnt = 0;
                this.mLteJamCnt = 0;
                this.mLteJamDuration = 0;
                this.mLastKeylogRptTime = mCurrentTimeSec;
            }
        }
    }

    private void reportScenesKeyLogInfo() {
        String key_info = "";
        for (int mSceneType = MtkRuimRecords.PHB_DELAY_SEND_TIME; mSceneType <= SCENE_TYPE_VIDEO_LIVE; mSceneType++) {
            long[][] jArr = this.mScenesKeyInfo;
            if (jArr[mSceneType - 500][1] > 0) {
                jArr[mSceneType - 500][2] = jArr[mSceneType - 500][2] / (jArr[mSceneType - 500][1] * STANDARD_VALUE);
            }
            long[][] jArr2 = this.mScenesKeyInfo;
            if (jArr2[mSceneType - 500][3] > 0) {
                jArr2[mSceneType - 500][4] = jArr2[mSceneType - 500][4] / (jArr2[mSceneType - 500][3] * STANDARD_VALUE);
            }
            switch (mSceneType) {
                case MtkRuimRecords.PHB_DELAY_SEND_TIME /*{ENCODED_INT: 500}*/:
                    key_info = key_info + "video:";
                    break;
                case SCENE_TYPE_VIDEO_CALL /*{ENCODED_INT: 501}*/:
                    key_info = key_info + "video call:";
                    break;
                case SCENE_TYPE_GAME /*{ENCODED_INT: 502}*/:
                    key_info = key_info + "game:";
                    break;
                case SCENE_TYPE_VIDEO_LIVE /*{ENCODED_INT: 503}*/:
                    key_info = key_info + "video live:";
                    break;
            }
            String key_info2 = key_info + this.mScenesKeyInfo[mSceneType - 500][0];
            this.mScenesKeyInfo[mSceneType - 500][0] = 0;
            for (int mKeyInfoType = 1; mKeyInfoType <= 4; mKeyInfoType++) {
                key_info2 = key_info2 + "," + this.mScenesKeyInfo[mSceneType - 500][mKeyInfoType];
                this.mScenesKeyInfo[mSceneType - 500][mKeyInfoType] = 0;
            }
            key_info = key_info2 + ";";
        }
        log("reportScenesKeyLogInfo key_info: " + key_info);
        try {
            String log_string = OemTelephonyUtils.getOemRes(this.mContext, "zz_oppo_critical_log_99", "");
            if (log_string.equals("")) {
                log("return for get log_string fail.");
                return;
            }
            String[] log_array = log_string.split(",");
            OppoManager.writeLogToPartition(Integer.valueOf(log_array[0]).intValue(), key_info, "NETWORK", ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCENES_INFO, log_array[1]);
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

    /* access modifiers changed from: private */
    public void sendSceneStateMsg(int sceneType, int actionType) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1007, sceneType, actionType));
    }

    private void printSceneStatesInfo() {
        log("printSceneStatesInfo mIsAnySceneNeedProhibit: " + this.mIsAnySceneNeedProhibit + " mScenesProhibitEnableMsks: " + Integer.toBinaryString(this.mScenesProhibitEnableMsks) + " mScenesNeedProhibitMsks: " + Integer.toBinaryString(this.mScenesNeedProhibitMsks));
        for (int mSceneType = MtkRuimRecords.PHB_DELAY_SEND_TIME; mSceneType <= SCENE_TYPE_VIDEO_LIVE; mSceneType++) {
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
    public void updateSceneStates(int sceneType, int actionType) {
        if (isSceneTypeValid(sceneType) && isSceneActionValid(actionType)) {
            if (actionType == 1) {
                this.mScenesStatusMsks |= 1 << (sceneType - 500);
            } else {
                this.mScenesStatusMsks &= ~(1 << (sceneType - 500));
            }
            this.mScenesNeedProhibitMsks = this.mScenesProhibitEnableMsks & this.mScenesStatusMsks;
            this.mScenesNeedProhibitMsks &= 255;
            this.mIsAnySceneNeedProhibit = false;
            if (this.mScenesNeedProhibitMsks > 0) {
                this.mIsAnySceneNeedProhibit = true;
            }
        }
        updateScenesKeyLogInfo(sceneType, actionType);
    }

    private void updateScenesKeyLogInfo(int sceneType, int actionType) {
        if (isSceneTypeValidForKeyLog(sceneType) && isSceneActionValid(actionType)) {
            long mCurrentTime = SystemClock.elapsedRealtime() / mSecondMs;
            long mTotalRxBytes = TrafficStats.getMobileRxBytes();
            if (actionType == 1) {
                this.mScenesStartTimeStats[sceneType - 500] = mCurrentTime;
                if (this.isNrBearAlloc) {
                    this.mScenesNrUsedStartTimeStats[sceneType - 500] = mCurrentTime;
                    this.mScenesNrUsedBytesStats[sceneType - 500] = mTotalRxBytes;
                    return;
                }
                this.mScenesNrNotUsedStartTimeStats[sceneType - 500] = mCurrentTime;
                this.mScenesNrNotUsedBytesStats[sceneType - 500] = mTotalRxBytes;
                return;
            }
            collectScenesKeyInfo(sceneType, 0, mCurrentTime - this.mScenesStartTimeStats[sceneType - 500]);
            if (this.isNrBearAlloc) {
                collectScenesKeyInfo(sceneType, 1, mCurrentTime - this.mScenesNrUsedStartTimeStats[sceneType - 500]);
                collectScenesKeyInfo(sceneType, 2, mTotalRxBytes - this.mScenesNrUsedBytesStats[sceneType - 500]);
            } else {
                collectScenesKeyInfo(sceneType, 3, mCurrentTime - this.mScenesNrNotUsedStartTimeStats[sceneType - 500]);
                collectScenesKeyInfo(sceneType, 4, mTotalRxBytes - this.mScenesNrNotUsedBytesStats[sceneType - 500]);
            }
            printSceneStatesInfo();
        }
    }

    private void nrBearChangedUpdateSceneStates(boolean lastEndcBearStatus) {
        long mCurrentTimeSec = SystemClock.elapsedRealtime() / mSecondMs;
        long mCurrentRxBytes = TrafficStats.getMobileRxBytes();
        for (int mSceneType = MtkRuimRecords.PHB_DELAY_SEND_TIME; mSceneType <= SCENE_TYPE_VIDEO_LIVE; mSceneType++) {
            if (1 == (this.mScenesStatusMsks & (1 << (mSceneType - 500)))) {
                if (lastEndcBearStatus) {
                    collectScenesKeyInfo(mSceneType, 1, mCurrentTimeSec - this.mScenesNrUsedStartTimeStats[mSceneType - 500]);
                    collectScenesKeyInfo(mSceneType, 2, mCurrentRxBytes - this.mScenesNrUsedBytesStats[mSceneType - 500]);
                } else {
                    collectScenesKeyInfo(mSceneType, 3, mCurrentTimeSec - this.mScenesNrNotUsedStartTimeStats[mSceneType - 500]);
                    collectScenesKeyInfo(mSceneType, 4, mCurrentRxBytes - this.mScenesNrNotUsedBytesStats[mSceneType - 500]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean getEndcBearStatus(int RatTech, boolean nrBearAlloc) {
        if ((RatTech == 13 || RatTech == 19) && nrBearAlloc) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void updateEndcBearInfo(boolean lastEndcBearStatus) {
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
        if (sceneType < 500 || sceneType > 505) {
            return false;
        }
        return true;
    }

    private boolean isSceneTypeValidForKeyLog(int sceneType) {
        if (sceneType < 500 || sceneType > SCENE_TYPE_VIDEO_LIVE) {
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
                } else if (eventType == 209) {
                    EndcBearController.this.log("event SCENE_MODE_VIDEO.");
                    mSceneType = EndcBearController.SCENE_TYPE_VIDEO_CALL;
                } else if (eventType == 211) {
                    EndcBearController.this.log("event GAME.");
                    mSceneType = EndcBearController.SCENE_TYPE_GAME;
                } else if (eventType == 212) {
                    EndcBearController.this.log("event VIDEO_LIVE");
                    mSceneType = EndcBearController.SCENE_TYPE_VIDEO_LIVE;
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
            final EventRequestConfig config = new EventRequestConfig(deviceEvents);
            new Thread(new Runnable(EndcBearController.this) {
                /* class com.mediatek.internal.telephony.EndcBearController.EventListen.AnonymousClass2 */

                public void run() {
                    if (ColorFrameworkFactory.getInstance().getColorDeepThinkerManager(EndcBearController.this.mContext).registerCallback(EventListen.this.mCallBack, config)) {
                        EndcBearController.this.log("register success!");
                    } else {
                        EndcBearController.this.log("register failed!");
                    }
                }
            }).start();
        }
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
    public void actionConfirmedNrBearDeAlloc() {
        this.isNrBearAlloc = false;
        this.mStartFlowIsNR = false;
        this.mNeedRewardDisEndcTimer = false;
        if (this.mDisEdncTimerAlarmIntent != null) {
            log("actionConfirmedNrBearDeAlloc: disable ENDC immediately");
            stopDisableEndcAlarmTimer();
            enableEndc(this.mDefaultDataSlotId, false);
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
    public void unregisterForServiceStateChanged() {
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
    public void updateLteJamDurationTime(boolean lastState, boolean currentState) {
        if (lastState && !currentState) {
            this.mLteJamDuration += (SystemClock.elapsedRealtime() / mSecondMs) - this.mLastEnterLteJamTime;
            log("updateLteJamDurationTime duration is   " + this.mLteJamDuration);
        }
    }

    private void startEnableEndcHysTimer() {
        this.mHandler.removeMessages(1013);
        this.mHandler.sendEmptyMessageDelayed(1013, this.ENABLE_ENDC_HYSTERESIS_MS);
    }

    private void stopEnableEndcHysTimer() {
        this.mHandler.removeMessages(1013);
    }

    private boolean isEnableEndcHysTimerRun() {
        if (this.mHandler.hasMessages(1013)) {
            return true;
        }
        return false;
    }
}
