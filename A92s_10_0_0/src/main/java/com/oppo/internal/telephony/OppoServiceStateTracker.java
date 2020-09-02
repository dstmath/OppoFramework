package com.oppo.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.OppoSignalStrength;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.AbstractServiceStateTracker;
import com.android.internal.telephony.AbstractSubscriptionController;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IOppoServiceStateTracker;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.dataconnection.OppoDcTrackerReference;
import com.oppo.internal.telephony.explock.OemLockUtils;
import com.oppo.internal.telephony.explock.OemRegionLockMonitorManager;
import com.oppo.internal.telephony.explock.OemServiceRegDurationState;
import com.oppo.internal.telephony.explock.RegionLockPlmnListParser;
import com.oppo.internal.telephony.nrNetwork.OppoNrStateUpdater;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseService;
import com.oppo.internal.telephony.nwdiagnose.OppoPhoneStateMonitor;
import com.oppo.internal.telephony.utils.ConnectivityManagerHelper;
import com.oppo.internal.telephony.utils.OppoManagerHelper;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import com.oppo.internal.telephony.utils.OppoServiceStateTrackerUtil;
import java.util.List;
import java.util.Random;

public class OppoServiceStateTracker extends Handler implements IOppoServiceStateTracker {
    private static final String ACTION_WORLD_MODE_CHANGED = "mediatek.intent.action.ACTION_WORLD_MODE_CHANGED";
    protected static final int ALLOWED_NO_SERVICE_INTERVAL = 40000;
    private static final float ALPHA_FACTOR_1 = 0.65f;
    private static final float ALPHA_FACTOR_2 = 0.75f;
    private static final float ALPHA_FACTOR_3 = 0.8f;
    public static final int DELAYTIME_0S = 0;
    public static final int DELAYTIME_10S = 10000;
    public static final int DELAYTIME_18S = 18000;
    public static final int DELAYTIME_3S = 3000;
    public static final int DELAYTIME_40S = 40000;
    public static final int DELAYTIME_60S = 60000;
    public static final int DELAYTIME_NOUPDATE = -1;
    protected static final int EVDO_ALLOWED_NO_SERVICE_INTERVAL = 45000;
    private static final int EVENT_CHECK_NO_SERVICE = 1;
    protected static final int EVENT_GET_SIGNAL_STRENGTH_ONCE = 3000;
    private static final int EVENT_MODEM_RESET = 5;
    private static final int EVENT_MODEM_RESET_SMOOTH = 6;
    private static final int EVENT_OEM_SCREEN_CHANGED = 3;
    protected static final int EVENT_OEM_SET_SCREEN_STATE = 3;
    private static final int EVENT_OEM_SMOOTH = 2;
    private static final int EVENT_RADIO_STATE_CHANGED = 4;
    protected static final int GSM_ALLOWED_NO_SERVICE_INTERVAL = 38000;
    private static int KEY_LOG_SCREEN_ON_TRY_NW_SRCH_RPT_THRES = 4;
    private static final int LEVEL_DIFF1 = 1;
    private static final int LEVEL_DIFF2 = 2;
    private static final int LEVEL_DIFF3 = 3;
    protected static final int LTE_ALLOWED_NO_SERVICE_INTERVAL = Integer.parseInt(SystemProperties.get("persist.vendor.radio.lte_oos_interval", "65000"));
    private static final int MIN_DELAY_RESEND = 1000;
    public static final int NT_CDMA = 5;
    public static final int NT_EVDO = 6;
    public static final int NT_GSM = 1;
    public static final int NT_LTE = 3;
    public static final int NT_NR = 7;
    public static final int NT_TDS = 2;
    public static final int NT_UNKNOWN = 0;
    public static final int NT_WCDMA = 4;
    private static final String PROP_IWLAN_STATE = "persist.vendor.radio.wfc_state";
    private static final int SIGNAL_RANGE = 5;
    private static final int SIGNAL_RANGE2 = 3;
    protected static final int SIM_TYPE_CMCC = 2;
    protected static final int SIM_TYPE_CU = 3;
    protected static final int SIM_TYPE_OTHER = -1;
    protected static final int TDS_ALLOWED_NO_SERVICE_INTERVAL = 40000;
    private static final int THREHOLD_LEVEL2 = -82;
    private static final int THREHOLD_LEVEL3 = -86;
    private static final int THREHOLD_LEVEL4 = -92;
    private static final int THREHOLD_LEVELASU2 = 16;
    protected static final int W_ALLOWED_NO_SERVICE_INTERVAL = 30000;
    public static int mSreenOnTryNwSrchCnt = 0;
    private String LOG_TAG = "OppoSST";
    protected long OosStartTime = -1;
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mCtStatus = false;
    protected boolean mDuplexModeChangeOnGoing = false;
    private boolean mInSwitchingDdsState = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.OppoServiceStateTracker.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            int wmState;
            if (intent.getAction().equals(OppoServiceStateTracker.ACTION_WORLD_MODE_CHANGED) && (wmState = intent.getIntExtra("worldModeState", -1)) == 0 && OppoServiceStateTracker.this.mPhone.getServiceStateTracker().getDesiredPowerState()) {
                OppoServiceStateTracker oppoServiceStateTracker = OppoServiceStateTracker.this;
                oppoServiceStateTracker.mDuplexModeChangeOnGoing = true;
                oppoServiceStateTracker.logd("ACTION_WORLD_MODE_CHANGED: wmState=" + wmState + ",mDuplexModeChangeOnGoing=" + OppoServiceStateTracker.this.mDuplexModeChangeOnGoing);
            }
        }
    };
    private boolean mIsFake = false;
    private boolean mIsModemRestarting = false;
    protected boolean mIsScreenOn = true;
    private long mLastUpdateSingalTime = 0;
    protected String mNewPlmn = null;
    private OemServiceRegDurationState mOemServiceRegDurState;
    protected String mOemSpn = "";
    protected int mOosDelayState = 0;
    protected int mOosReportTime = DELAYTIME_40S;
    String mOperatorStatus = SystemProperties.get("ro.oppo.operator", "ex");
    private OppoNrStateUpdater mOppoNrStateUpdater;
    private OppoRIL mOppoRIL;
    private SignalStrength mOrigSignalStrength = new SignalStrength();
    private SignalStrength mPendingSignalStrength = new SignalStrength();
    /* access modifiers changed from: private */
    public GsmCdmaPhone mPhone;
    private OppoNetworkPowerState mPowerState;
    private ServiceStateTracker mSST;
    protected boolean mShowPlmn;
    protected boolean mShowSPn;
    private SubscriptionController mSubscriptionController;
    protected boolean needCheckCuSs = false;
    protected String operator = SystemProperties.get("ro.oppo.operator", "ex");
    private boolean oppoSignalUpdate = false;
    protected String romFeature = SystemProperties.get("ro.rom.featrue", "allnet");
    private String temPlmn = null;
    private boolean temShowPlmn;
    private boolean temShowSpn;
    private String temSpn = null;

    public OppoServiceStateTracker(ServiceStateTracker sst, GsmCdmaPhone phone) {
        this.mSST = sst;
        this.mPhone = phone;
        this.LOG_TAG += "/" + this.mPhone.getPhoneId();
        this.mContext = this.mPhone.getContext();
        OppoTelephonyController oppoTelephony = OppoTelephonyController.getInstance(this.mContext);
        oppoTelephony.registerForOemScreenChanged(this, 3, null);
        this.mOppoRIL = oppoTelephony.getOppoRIL(this.mPhone.getPhoneId());
        this.mSubscriptionController = SubscriptionController.getInstance();
        this.mPowerState = new OppoNetworkPowerState(this.mContext, this.mPhone, this.mSST, this.mOppoRIL);
        this.mOppoNrStateUpdater = new OppoNrStateUpdater(this.mContext, this.mPhone, this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WORLD_MODE_CHANGED);
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        if (OemConstant.EXP_VERSION && !OemConstant.RM_VERSION) {
            if (OemLockUtils.isRegionLock() && OemLockUtils.getRegionLockStatus()) {
                this.mOemServiceRegDurState = new OemServiceRegDurationState(this.mContext, this.mPhone);
            }
            if (OemLockUtils.isEnableUpRlock()) {
                OemRegionLockMonitorManager.make(this.mContext);
            }
        }
        this.mPhone.mCi.registerForRadioStateChanged(this, 4, (Object) null);
        this.mPhone.mCi.registerForModemReset(this, 5, (Object) null);
    }

    public void handleMessage(Message msg) {
        logd("EventHandler:" + msg.what);
        switch (msg.what) {
            case 1:
                this.mSST.pollState();
                return;
            case 2:
                smoothSignalStrength((SignalStrength) msg.obj);
                return;
            case 3:
                AsyncResult arscreen = (AsyncResult) msg.obj;
                if (arscreen != null) {
                    this.mIsScreenOn = ((Boolean) arscreen.result).booleanValue();
                } else {
                    logd("leon EVENT_OEM_SCREEN_CHANGED error");
                }
                if (true == this.mIsScreenOn) {
                    this.mPowerState.screenOn();
                } else {
                    this.mPowerState.screenOff();
                }
                if (this.oppoSignalUpdate && OppoTelephonyController.getInstance(this.mContext).isScreenOn()) {
                    logd("screen on update signal strength when necessary");
                    this.oppoSignalUpdate = false;
                    oppoNotifySignalStrength();
                }
                if (this.mIsScreenOn) {
                    if ((this.mOosDelayState == 1 || getOemRegState(this.mSST.mSS) == 1 || getOemRegState(this.mSST.mSS) == 2) && this.mPhone.mCi.getRadioState() == 1 && this.mPhone.getIccCard().hasIccCard() && !((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, this.mPhone)).is_test_card()) {
                        mSreenOnTryNwSrchCnt++;
                        if (mSreenOnTryNwSrchCnt >= KEY_LOG_SCREEN_ON_TRY_NW_SRCH_RPT_THRES) {
                            OppoManagerHelper.writeLogToPartition(this.mContext, "zz_oppo_critical_log_91", "screen on nw srch cnt:" + mSreenOnTryNwSrchCnt, OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_NW_SRCH);
                            mSreenOnTryNwSrchCnt = 0;
                        }
                        logd("screen on nw srch try ");
                        return;
                    }
                    return;
                }
                return;
            case 4:
                if (this.mDuplexModeChangeOnGoing && this.mPhone.isRadioOn() && this.mPhone.getServiceStateTracker().getDesiredPowerState()) {
                    this.mDuplexModeChangeOnGoing = false;
                    logd("handleMessage: EVENT_RADIO_STATE_CHANGED, When RADIO_ON set mDuplexModeChangeOnGoing=false");
                    return;
                }
                return;
            case 5:
                if (this.mOppoNrStateUpdater.isModemResetSmoothEnabled()) {
                    logd("EVENT_MODEM_RESET Received");
                    this.mIsModemRestarting = true;
                    removeMessages(6);
                    sendEmptyMessageDelayed(6, (long) this.mOppoNrStateUpdater.getModemResetSmoothTime());
                    return;
                }
                return;
            case 6:
                logd("EVENT_MODEM_RESET_SMOOTH Received");
                resetModemStatus();
                this.mSST.pollState();
                return;
            default:
                return;
        }
    }

    private void resetModemStatus() {
        this.mIsModemRestarting = false;
        removeMessages(6);
    }

    public boolean getSwitchingDdsState() {
        return this.mInSwitchingDdsState;
    }

    public void setSwitchingDdsState(boolean state) {
        this.mInSwitchingDdsState = state;
    }

    public void oppoResetOosDelayState() {
        logd("oppoResetOosDelayState mOosDelayState:" + this.mOosDelayState);
        this.mOosDelayState = 0;
    }

    public boolean oppoIsInDelayOOSState() {
        if (1 == this.mOosDelayState && oppoGetCombRegState(this.mSST.mSS) == 0) {
            return true;
        }
        return false;
    }

    public int oppoOosDelayState(ServiceState mNewSS) {
        int i;
        int oldDelayState = this.mOosDelayState;
        boolean isOos = ((getOemRegState(mNewSS) == 1 || getOemRegState(mNewSS) == 2) && this.mPhone.isRadioOn() && this.mSST.mSS.getState() != 3) || (this.mPhone.getRadioPowerState() == 0 && this.mDuplexModeChangeOnGoing) || ((getOemRegState(mNewSS) == 1 && this.mIsModemRestarting) || (getOemRegState(mNewSS) == 3 && this.mIsModemRestarting));
        int regState = mNewSS.getVoiceRegState();
        AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, this.mPhone);
        boolean isOosInVowifi = (regState == 1 || regState == 2) && this.mPhone.isRadioOn() && this.mSST.mSS.getState() != 3 && (tmpPhone != null ? tmpPhone.getVowifiRegStatus() : false);
        boolean inSwitchingDss = getSwitchingDdsState();
        int mSimState = TelephonyManager.getDefault().getSimState(this.mPhone.getPhoneId());
        long currentTime = System.currentTimeMillis();
        logd("inSwitchingDss = " + inSwitchingDss + "  mSimState = " + mSimState);
        if (isOos || isOosInVowifi) {
            if (isOtherPhoneIncall()) {
                i = 1;
            } else if (this.mPhone.getState() != PhoneConstants.State.IDLE) {
                i = 1;
            } else if (!this.mIsModemRestarting && (inSwitchingDss || mSimState != 5 || !twoCTcardNeedOosDelay())) {
                logd("oppoOosDelayState should not delay oos");
                this.mOosDelayState = 0;
                removeMessages(1);
            } else if (this.mOosDelayState != 1) {
                logd("oppoOosDelayState send EVENT_CHECK_NO_SERVICE");
                this.mOosReportTime = getOosReportTimeBySS();
                sendEmptyMessageDelayed(1, (long) this.mOosReportTime);
                this.OosStartTime = currentTime;
                this.mOosDelayState = 1;
            } else if (currentTime >= this.OosStartTime + ((long) this.mOosReportTime)) {
                logd("oppoDelayOosState currentTime: " + currentTime + "  OosStartTime:" + this.OosStartTime);
                this.mOosDelayState = 2;
                removeMessages(1);
            }
            logd("oppoOosDelayState Other Phone is in call, do delay");
            removeMessages(i);
            this.mOosReportTime = getOosReportTimeBySS();
            sendEmptyMessageDelayed(i, (long) this.mOosReportTime);
            this.OosStartTime = currentTime;
            this.mOosDelayState = i;
        } else {
            this.mOosDelayState = 0;
            removeMessages(1);
            resetModemStatus();
        }
        if (inSwitchingDss) {
            setSwitchingDdsState(false);
        }
        logd("oppoOosDelayState: mOosDelayState = " + this.mOosDelayState + ", oldstate=" + oldDelayState);
        boolean isForce = true;
        if (!(oldDelayState == 1 && this.mOosDelayState == 0)) {
            isForce = false;
        }
        updateSignalStrengthForServiceState(mNewSS, isForce);
        if (2 == this.mOosDelayState) {
            logd("key_log:OOS_DELAY_TIMEOUT");
            int testMode = SystemProperties.getInt("vendor.gsm.gcf.testmode", 0);
            logd("test mode = " + testMode);
            if (testMode == 0) {
                oppoSetAutoNetworkSelect(mNewSS);
            }
            writeLogToPartitionForOOS();
        }
        return this.mOosDelayState;
    }

    private void writeLogToPartitionForOOS() {
        int size = OppoManagerHelper.writeLogToPartition(this.mContext, "zz_oppo_critical_log_80", " Out of service event ", OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_SRV_OOS);
        logd("key_log: Out of service event Write log, return: = " + size);
    }

    private boolean isOtherPhoneIncall() {
        Phone oPhone = PhoneFactory.getPhone(1 - this.mPhone.getPhoneId());
        if (oPhone == null || oPhone.getState() == PhoneConstants.State.IDLE) {
            return false;
        }
        return true;
    }

    public void oppoSetAutoNetworkSelect(ServiceState ss) {
        if (this.mPhone.getIccCard().hasIccCard()) {
            AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, this.mPhone);
            if (ss.getIsManualSelection() && !tmpPhone.getManualSearchingStatus()) {
                logd("manual search and oos time out, set auto");
                this.mPhone.setNetworkSelectionModeAutomatic((Message) null);
            }
        }
    }

    public int getOemRegState(ServiceState ss) {
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        int mIwlanState = 0;
        try {
            mIwlanState = Integer.valueOf(TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), PROP_IWLAN_STATE, "0")).intValue();
        } catch (Exception e) {
            loge("getOemRegState: INVALID PROP_IWLAN_STATE");
        }
        if (mIwlanState != 0) {
            logd("getOemRegState: mIwlanState =" + mIwlanState + ", ignore dataRegState if Iwlan in service");
        }
        return (regState == 1 && dataRegState == 0 && regCodeToServiceState(mIwlanState) != 0) ? dataRegState : regState;
    }

    private int regCodeToServiceState(int code) {
        if (code == 1 || code == 5) {
            return 0;
        }
        return 1;
    }

    private boolean twoCTcardNeedOosDelay() {
        if (TelephonyManager.getDefault().getPhoneCount() != 2) {
            return true;
        }
        if (!OppoPhoneUtil.isCtCard(PhoneFactory.getPhone(1 - this.mPhone.getPhoneId())) || !OppoPhoneUtil.isCtCard((Phone) this.mPhone)) {
            logd("ct simcard check:non-ct, need smooth ");
            return true;
        } else if (this.mPhone.getServiceState().getDataRoaming()) {
            logd("ct simcard check:roaming, need smooth ");
            return true;
        } else if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId() || this.mPhone.isImsRegistered()) {
            logd("ct simcard check: need smooth");
            return true;
        } else {
            logd("ct simcard check:non-DDS id ,not registered ims,  DONT need smooth ");
            return false;
        }
    }

    private PersistableBundle getCarrierConfig() {
        PersistableBundle config;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager == null || (config = configManager.getConfigForSubId(this.mPhone.getSubId())) == null) {
            return CarrierConfigManager.getDefaultConfig();
        }
        return config;
    }

    private class AlphaFilterResult {
        int delay;
        SignalStrength nextSignalStrength;

        private AlphaFilterResult() {
        }
    }

    public void updateSignalStrengthLevel(SignalStrength signalStrength) {
        OppoSignalStrengthStandard.getSignalStrengthLevel(signalStrength, needShowZero());
    }

    public boolean onSignalStrengthResultEx(AsyncResult ar, SignalStrength oldSignalStrength) {
        if (ar.exception != null || ar.result == null) {
            logd("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            removeSmoothMessage();
            this.mPendingSignalStrength = new SignalStrength();
            OppoServiceStateTrackerUtil.copyFrom(oldSignalStrength, this.mPendingSignalStrength);
            if (OppoTelephonyController.getInstance(this.mContext).isScreenOn()) {
                oppoNotifySignalStrength();
            }
            return true;
        }
        SignalStrength signalStrength = (SignalStrength) ar.result;
        signalStrength.updateLevel(getCarrierConfig(), this.mSST.mSS);
        OppoSignalStrengthStandard.getSignalStrengthLevel(signalStrength, needShowZero());
        OppoServiceStateTrackerUtil.copyFrom(this.mOrigSignalStrength, signalStrength);
        logd("onSignalStrengthResultEx,oldSignalStrength:" + oldSignalStrength);
        logd("mOrigSignalStrength:" + this.mOrigSignalStrength);
        int oldSignalOEMLevel_0 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, oldSignalStrength)).getOEMLevel_0();
        int pendingSignalOEMLevel_0 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mPendingSignalStrength)).getOEMLevel_0();
        OppoSignalStrength tempSignalStrength = (OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, signalStrength);
        if (!signalStrength.equals(oldSignalStrength)) {
            updatePhoneMonitorSignalStrength(signalStrength);
        }
        if (this.mSST.mSS != null && true == this.mPhone.isInCall() && (6 == this.mSST.mSS.getRilVoiceRadioTechnology() || 4 == this.mSST.mSS.getRilVoiceRadioTechnology() || 5 == this.mSST.mSS.getRilVoiceRadioTechnology())) {
            tempSignalStrength.setOEMLevel(OppoSignalStrengthStandard.getCdmaRelatedSignalStrength(signalStrength), -1);
            String str = this.LOG_TAG;
            Rlog.d(str, "mOEMLevel_0=" + tempSignalStrength.getOEMLevel_0() + "for CT card");
        }
        if (getOemRegState(this.mSST.mSS) != 0 && !this.mIsModemRestarting) {
            logd("no service fix signal...");
            this.mPendingSignalStrength = signalStrength;
            OppoServiceStateTrackerUtil.copyFrom(oldSignalStrength, this.mPendingSignalStrength);
            if (OppoTelephonyController.getInstance(this.mContext).isScreenOn()) {
                oppoNotifySignalStrength();
            }
            return true;
        } else if (isOtherPhoneIncall() && oldSignalOEMLevel_0 > tempSignalStrength.getOEMLevel_0()) {
            return true;
        } else {
            fixSignalStrength(signalStrength, oldSignalStrength);
            if (!this.mPendingSignalStrength.equals(signalStrength) || tempSignalStrength.getOEMLevel_0() != pendingSignalOEMLevel_0) {
                this.mPendingSignalStrength = signalStrength;
                if (!OppoTelephonyController.getInstance(this.mContext).isScreenOn()) {
                    logd("return because screen is off");
                    this.oppoSignalUpdate = true;
                    OppoServiceStateTrackerUtil.copyFrom(oldSignalStrength, this.mPendingSignalStrength);
                    return true;
                }
                boolean ret = updateOEMSmooth(this.mSST.mSS, oldSignalStrength);
                if (ret) {
                    oppoNotifySignalStrength();
                }
                return ret;
            }
            logd("updatesignal no change.");
            if (pendingSignalOEMLevel_0 > oldSignalOEMLevel_0) {
                removeSmoothMessage();
                OppoServiceStateTrackerUtil.copyFrom(oldSignalStrength, this.mPendingSignalStrength);
                oppoNotifySignalStrength();
            }
            return true;
        }
    }

    private int smoothdelay(int oldLevel, int newLevel) {
        if (newLevel > oldLevel) {
            return 0;
        }
        int diff = oldLevel - newLevel;
        if (this.mOosDelayState == 1) {
            if (diff == 0) {
                return 3000;
            }
            return DELAYTIME_10S;
        } else if (diff == 1) {
            return DELAYTIME_18S;
        } else {
            if (diff == 2) {
                return DELAYTIME_40S;
            }
            if (diff >= 3) {
                return DELAYTIME_60S;
            }
            return 3000;
        }
    }

    private boolean isLteSignalValid(int lteRsrp) {
        if (lteRsrp > -44 || lteRsrp < -140) {
            return false;
        }
        return true;
    }

    private boolean isGSMSignalValid(int value) {
        return (value == 99 || value == -1 || value == Integer.MAX_VALUE || value == 0 || value == 1) ? false : true;
    }

    private boolean isCDMASignalValid(int value) {
        return (value == 99 || value == -1 || value == -120 || value == Integer.MAX_VALUE || value == 0 || value == 1) ? false : true;
    }

    private boolean isTdsSignalValid(int tdScdmaDbm) {
        if (tdScdmaDbm > -25 || tdScdmaDbm == Integer.MAX_VALUE) {
            return false;
        }
        return true;
    }

    private boolean isWcdmaSignalValid(int wcdmaDbm) {
        if (wcdmaDbm < -120 || wcdmaDbm > -24) {
            return false;
        }
        return true;
    }

    private boolean isValidSignalStrength(SignalStrength signalStrength) {
        if (isLteSignalValid(OppoSignalStrengthStandard.getNrRsrp(signalStrength)) || isLteSignalValid(signalStrength.getLteRsrp()) || isGSMSignalValid(signalStrength.getGsmDbm()) || isWcdmaSignalValid(signalStrength.getWcdmaDbm()) || isTdsSignalValid(signalStrength.getTdScdmaDbm()) || isCDMASignalValid(signalStrength.getCdmaDbm()) || isCDMASignalValid(signalStrength.getEvdoDbm())) {
            return true;
        }
        return false;
    }

    private AlphaFilterResult alphaFiltering(SignalStrength oldSignalStrength) {
        int nextNrRsrp;
        int curNrRsrp;
        int curLteRsrp;
        int nextGsmSignalStrength;
        int curLteRsrp2;
        int nextTdScdmaRscp;
        AlphaFilterResult result;
        int oldSignalOEMLevel_0;
        AlphaFilterResult result2 = new AlphaFilterResult();
        logd("alphaFiltering mSignalStrength:" + oldSignalStrength);
        logd("alphaFiltering mPendingSignalStrength:" + this.mPendingSignalStrength);
        int oldSignalOEMLevel_02 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, oldSignalStrength)).getOEMLevel_0();
        int pendingSignalOEMLevel_0 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mPendingSignalStrength)).getOEMLevel_0();
        if (!oldSignalStrength.equals(this.mPendingSignalStrength) || oldSignalOEMLevel_02 != pendingSignalOEMLevel_0) {
            float alphaFactor = ALPHA_FACTOR_1;
            if (oldSignalOEMLevel_02 - pendingSignalOEMLevel_0 >= 3) {
                alphaFactor = ALPHA_FACTOR_3;
            } else if (oldSignalOEMLevel_02 - pendingSignalOEMLevel_0 == 2) {
                alphaFactor = ALPHA_FACTOR_2;
            }
            int curGsmSignalStrength = oldSignalStrength.getGsmDbm();
            int nextGsmSignalStrength2 = this.mPendingSignalStrength.getGsmDbm();
            int curCdmaDbm = oldSignalStrength.getCdmaDbm();
            int nextCdmaDbm = this.mPendingSignalStrength.getCdmaDbm();
            int curEvdoDbm = oldSignalStrength.getEvdoDbm();
            int nextEvdoDbm = this.mPendingSignalStrength.getEvdoDbm();
            boolean notifyNow = false;
            int curTdScdmaRscp = oldSignalStrength.getTdScdmaDbm();
            int nextTdScdmaRscp2 = this.mPendingSignalStrength.getTdScdmaDbm();
            int curWcdmaRscp = oldSignalStrength.getWcdmaDbm();
            int nextWcdmaRscp = this.mPendingSignalStrength.getWcdmaDbm();
            int curLteRsrp3 = oldSignalStrength.getLteRsrp();
            int nextLteRsrp = this.mPendingSignalStrength.getLteRsrp();
            int curNrRsrp2 = OppoSignalStrengthStandard.getNrRsrp(oldSignalStrength);
            int nextNrRsrp2 = OppoSignalStrengthStandard.getNrRsrp(this.mPendingSignalStrength);
            if (curGsmSignalStrength == nextGsmSignalStrength2) {
                curNrRsrp = curNrRsrp2;
                curLteRsrp = curLteRsrp3;
                nextNrRsrp = nextNrRsrp2;
            } else if (!isGSMSignalValid(curGsmSignalStrength) || !isGSMSignalValid(nextGsmSignalStrength2)) {
                curNrRsrp = curNrRsrp2;
                curLteRsrp = curLteRsrp3;
                nextNrRsrp = nextNrRsrp2;
                curGsmSignalStrength = nextGsmSignalStrength2;
            } else if (nextGsmSignalStrength2 > curGsmSignalStrength) {
                curGsmSignalStrength = nextGsmSignalStrength2;
                curNrRsrp = curNrRsrp2;
                nextNrRsrp = nextNrRsrp2;
                notifyNow = true;
                curLteRsrp = curLteRsrp3;
            } else {
                curNrRsrp = curNrRsrp2;
                if (curGsmSignalStrength >= 16) {
                    curLteRsrp = curLteRsrp3;
                    nextNrRsrp = nextNrRsrp2;
                    curGsmSignalStrength = 16 - new Random().nextInt(3);
                } else {
                    curLteRsrp = curLteRsrp3;
                    nextNrRsrp = nextNrRsrp2;
                    curGsmSignalStrength = (int) ((((float) curGsmSignalStrength) * alphaFactor) + (((float) nextGsmSignalStrength2) * (1.0f - alphaFactor)));
                    if (curGsmSignalStrength == curGsmSignalStrength && nextGsmSignalStrength2 != curGsmSignalStrength) {
                        curGsmSignalStrength = nextGsmSignalStrength2;
                    }
                }
            }
            if (curCdmaDbm == nextCdmaDbm) {
                nextGsmSignalStrength = nextGsmSignalStrength2;
            } else if (!isCDMASignalValid(curCdmaDbm) || !isCDMASignalValid(nextCdmaDbm)) {
                nextGsmSignalStrength = nextGsmSignalStrength2;
                curCdmaDbm = nextCdmaDbm;
            } else if (nextCdmaDbm > curCdmaDbm) {
                curCdmaDbm = nextCdmaDbm;
                notifyNow = true;
                nextGsmSignalStrength = nextGsmSignalStrength2;
            } else if (curCdmaDbm >= THREHOLD_LEVEL2) {
                curCdmaDbm = -82 - new Random().nextInt(5);
                nextGsmSignalStrength = nextGsmSignalStrength2;
            } else {
                nextGsmSignalStrength = nextGsmSignalStrength2;
                curCdmaDbm = (int) Math.floor((double) ((((float) curCdmaDbm) * alphaFactor) + (((float) nextCdmaDbm) * (1.0f - alphaFactor))));
                if (curCdmaDbm == curCdmaDbm && curCdmaDbm != nextCdmaDbm) {
                    curCdmaDbm = nextCdmaDbm;
                }
            }
            if (curEvdoDbm != nextEvdoDbm) {
                if (!isCDMASignalValid(curEvdoDbm) || !isCDMASignalValid(nextEvdoDbm)) {
                    curEvdoDbm = nextEvdoDbm;
                } else if (nextEvdoDbm > curEvdoDbm) {
                    curEvdoDbm = nextEvdoDbm;
                    notifyNow = true;
                } else if (curEvdoDbm >= THREHOLD_LEVEL3) {
                    curEvdoDbm = -86 - new Random().nextInt(5);
                } else {
                    curEvdoDbm = (int) Math.floor((double) ((((float) curEvdoDbm) * alphaFactor) + (((float) nextEvdoDbm) * (1.0f - alphaFactor))));
                    if (curEvdoDbm == curEvdoDbm && curEvdoDbm != nextEvdoDbm) {
                        curEvdoDbm = nextEvdoDbm;
                    }
                }
            }
            if (curTdScdmaRscp != nextTdScdmaRscp2) {
                if (!isTdsSignalValid(curTdScdmaRscp) || !isTdsSignalValid(nextTdScdmaRscp2)) {
                    curTdScdmaRscp = nextTdScdmaRscp2;
                } else if (nextTdScdmaRscp2 > curTdScdmaRscp) {
                    curTdScdmaRscp = nextTdScdmaRscp2;
                    notifyNow = true;
                } else if (curTdScdmaRscp >= THREHOLD_LEVEL3) {
                    curTdScdmaRscp = -86 - new Random().nextInt(5);
                } else {
                    curTdScdmaRscp = (int) Math.floor((double) ((((float) curTdScdmaRscp) * alphaFactor) + (((float) nextTdScdmaRscp2) * (1.0f - alphaFactor))));
                    if (curTdScdmaRscp == curTdScdmaRscp && curTdScdmaRscp != nextTdScdmaRscp2) {
                        curTdScdmaRscp = nextTdScdmaRscp2;
                    }
                }
            }
            if (curWcdmaRscp != nextWcdmaRscp) {
                if (!isWcdmaSignalValid(curWcdmaRscp) || !isWcdmaSignalValid(nextWcdmaRscp)) {
                    curWcdmaRscp = nextWcdmaRscp;
                } else if (nextWcdmaRscp > curWcdmaRscp) {
                    curWcdmaRscp = nextWcdmaRscp;
                    notifyNow = true;
                } else if (curWcdmaRscp >= THREHOLD_LEVEL3) {
                    curWcdmaRscp = -86 - new Random().nextInt(5);
                } else {
                    curWcdmaRscp = (int) Math.floor((double) ((((float) curWcdmaRscp) * alphaFactor) + (((float) nextWcdmaRscp) * (1.0f - alphaFactor))));
                    if (curWcdmaRscp == curWcdmaRscp && curWcdmaRscp != nextWcdmaRscp) {
                        curWcdmaRscp = nextWcdmaRscp;
                    }
                }
            }
            if (curLteRsrp == nextLteRsrp) {
                curLteRsrp2 = curLteRsrp;
            } else if (!isLteSignalValid(curLteRsrp) || !isLteSignalValid(nextLteRsrp)) {
                curLteRsrp2 = nextLteRsrp;
            } else if (nextLteRsrp > curLteRsrp) {
                notifyNow = true;
                curLteRsrp2 = nextLteRsrp;
            } else if (curLteRsrp >= THREHOLD_LEVEL4) {
                curLteRsrp2 = -92 - (new Random().nextInt(5) + 1);
            } else {
                curLteRsrp2 = (int) Math.floor((double) ((((float) curLteRsrp) * alphaFactor) + (((float) nextLteRsrp) * (1.0f - alphaFactor))));
                if (curLteRsrp == curLteRsrp2 && curLteRsrp2 != nextLteRsrp) {
                    curLteRsrp2 = nextLteRsrp;
                }
            }
            int curNrRsrp3 = curNrRsrp;
            if (curNrRsrp3 == nextNrRsrp) {
                nextTdScdmaRscp = nextWcdmaRscp;
            } else if (!isLteSignalValid(curNrRsrp3) || !isLteSignalValid(nextNrRsrp)) {
                nextTdScdmaRscp = nextWcdmaRscp;
                curNrRsrp3 = nextNrRsrp;
            } else if (nextNrRsrp > curNrRsrp3) {
                curNrRsrp3 = nextNrRsrp;
                notifyNow = true;
                nextTdScdmaRscp = nextWcdmaRscp;
            } else if (curNrRsrp3 >= THREHOLD_LEVEL4) {
                curNrRsrp3 = -92 - (new Random().nextInt(5) + 1);
                nextTdScdmaRscp = nextWcdmaRscp;
            } else {
                nextTdScdmaRscp = nextWcdmaRscp;
                curNrRsrp3 = (int) Math.floor((double) ((((float) curNrRsrp3) * alphaFactor) + (((float) nextNrRsrp) * (1.0f - alphaFactor))));
                if (curNrRsrp3 == curNrRsrp3 && curNrRsrp3 != nextNrRsrp) {
                    curNrRsrp3 = nextNrRsrp;
                }
            }
            SignalStrength nextSignalStrength = new SignalStrength(new CellSignalStrengthCdma(curCdmaDbm, Integer.MAX_VALUE, curEvdoDbm, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthGsm(curGsmSignalStrength, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthWcdma(Integer.MAX_VALUE, Integer.MAX_VALUE, curWcdmaRscp, Integer.MAX_VALUE), new CellSignalStrengthTdscdma(Integer.MAX_VALUE, Integer.MAX_VALUE, curTdScdmaRscp), new CellSignalStrengthLte(Integer.MAX_VALUE, curLteRsrp2, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthNr(curNrRsrp3, Integer.MAX_VALUE, Integer.MAX_VALUE, curNrRsrp3, Integer.MAX_VALUE, Integer.MAX_VALUE));
            OppoSignalStrengthStandard.getSignalStrengthLevel(nextSignalStrength, needShowZero());
            OppoSignalStrength tmpNextSignalStrength = (OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, nextSignalStrength);
            if (this.mSST.mSS != null) {
                if (true == this.mPhone.isInCall()) {
                    if (6 == this.mSST.mSS.getRilVoiceRadioTechnology() || 4 == this.mSST.mSS.getRilVoiceRadioTechnology() || 5 == this.mSST.mSS.getRilVoiceRadioTechnology()) {
                        tmpNextSignalStrength.setOEMLevel(OppoSignalStrengthStandard.getCdmaRelatedSignalStrength(nextSignalStrength), -1);
                        Rlog.d(this.LOG_TAG, "mOEMLevel_0=" + tmpNextSignalStrength.getOEMLevel_0() + ", for CT card");
                    }
                }
            }
            logd("after nextSignalStrength:" + nextSignalStrength);
            if (notifyNow) {
                oldSignalOEMLevel_0 = oldSignalOEMLevel_02;
                if (tmpNextSignalStrength.getOEMLevel_0() >= oldSignalOEMLevel_0) {
                    logd("alphaFiltering step2.");
                    result2.delay = 0;
                    result2.nextSignalStrength = nextSignalStrength;
                    return result2;
                }
                result = result2;
            } else {
                result = result2;
                oldSignalOEMLevel_0 = oldSignalOEMLevel_02;
            }
            if (oldSignalOEMLevel_0 - tmpNextSignalStrength.getOEMLevel_0() > 1) {
                logd("alphaFiltering step3.");
                tmpNextSignalStrength.setOEMLevel(oldSignalOEMLevel_0 - 1, oldSignalOEMLevel_0 - 1);
            }
            result.delay = smoothdelay(oldSignalOEMLevel_0, tmpNextSignalStrength.getOEMLevel_0());
            result.nextSignalStrength = nextSignalStrength;
            return result;
        }
        logd("alphaFiltering step1.");
        result2.delay = -1;
        return result2;
    }

    private void sendSmoothMessage(int delay, SignalStrength signalStength) {
        removeMessages(2);
        Message msg = Message.obtain();
        msg.what = 2;
        msg.obj = signalStength;
        sendMessageDelayed(msg, (long) delay);
    }

    private int getSignalStrengthType(SignalStrength signalStength) {
        if (signalStength.isGsm()) {
            if (isLteSignalValid(OppoSignalStrengthStandard.getNrRsrp(signalStength))) {
                return 7;
            }
            if (isLteSignalValid(signalStength.getLteRsrp())) {
                return 3;
            }
            if (isTdsSignalValid(signalStength.getTdScdmaDbm())) {
                return 2;
            }
            if (isWcdmaSignalValid(signalStength.getWcdmaDbm())) {
                return 4;
            }
            if (isGSMSignalValid(signalStength.getGsmDbm())) {
                return 1;
            }
            return 0;
        } else if (isCDMASignalValid(signalStength.getCdmaDbm())) {
            return 5;
        } else {
            if (isCDMASignalValid(signalStength.getEvdoDbm())) {
                return 6;
            }
            return 0;
        }
    }

    private int simulateGsmSignal(int level, int orig) {
        Random random = new Random();
        if (level == 4) {
            return random.nextInt(4) + 14;
        }
        if (level == 3) {
            return random.nextInt(4) + 8;
        }
        if (level == 2) {
            return random.nextInt(2) + 5;
        }
        return orig;
    }

    private int simulateWcdmaSignal(int level, int orig) {
        Random random = new Random();
        if (level == 4) {
            return random.nextInt(4) - 90;
        }
        if (level == 3) {
            return random.nextInt(4) - 97;
        }
        if (level == 2) {
            return random.nextInt(8) + OppoSignalStrengthStandard.MIN_GSM_VALUE;
        }
        if (level == 1) {
            return random.nextInt(5) - 126;
        }
        return orig;
    }

    private int simulateLteSignal(int level, int orig) {
        Random random = new Random();
        if (level == 4) {
            return random.nextInt(4) - 96;
        }
        if (level == 3) {
            return random.nextInt(11) - 110;
        }
        if (level == 2) {
            return random.nextInt(6) - 118;
        }
        if (level == 1) {
            return random.nextInt(6) - 126;
        }
        return orig;
    }

    private int simulateCdmaSignal(int level, int orig) {
        Random random = new Random();
        if (level == 4) {
            return random.nextInt(4) - 88;
        }
        if (level == 3) {
            return random.nextInt(9) - 100;
        }
        if (level == 2) {
            return random.nextInt(4) - 106;
        }
        return orig;
    }

    private void fixSignalStrength(SignalStrength signalStrength, SignalStrength oldSignalStrength) {
        int evdoDbm;
        int tdScdmaRscp;
        int wcdmaRscp;
        int lteRsrp;
        int curNrRsrp;
        int curEvdoDbm;
        int curTdScdmaRscp;
        int curWcdmaRscp;
        int curLteRsrp;
        boolean needZero;
        int curNrRsrp2;
        int oldSignalOEMLevel_0 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, oldSignalStrength)).getOEMLevel_0();
        OppoSignalStrength tempSignalStrength = (OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, signalStrength);
        if (getOemRegState(this.mSST.mSS) == 0) {
            if (!isValidSignalStrength(signalStrength) || this.mOosDelayState == 1) {
                logd("fixSignalStrength.11.");
                boolean needZero2 = false;
                int curGsmSignalStrength = oldSignalStrength.getGsmDbm();
                if (isGSMSignalValid(curGsmSignalStrength)) {
                    curGsmSignalStrength = OppoSignalStrengthStandard.MIN_GSM_VALUE;
                }
                int curCdmaDbm = oldSignalStrength.getCdmaDbm();
                if (isCDMASignalValid(curCdmaDbm)) {
                    curCdmaDbm = OppoSignalStrengthStandard.MIN_CDMA_VALUE;
                }
                int curEvdoDbm2 = oldSignalStrength.getEvdoDbm();
                if (isCDMASignalValid(curEvdoDbm2)) {
                    curEvdoDbm = -119;
                } else {
                    curEvdoDbm = curEvdoDbm2;
                }
                int curTdScdmaRscp2 = oldSignalStrength.getTdScdmaDbm();
                if (isTdsSignalValid(curTdScdmaRscp2)) {
                    needZero2 = true;
                    curTdScdmaRscp = -120;
                } else {
                    curTdScdmaRscp = curTdScdmaRscp2;
                }
                int curWcdmaRscp2 = oldSignalStrength.getWcdmaDbm();
                if (isWcdmaSignalValid(curWcdmaRscp2)) {
                    needZero2 = true;
                    curWcdmaRscp = -120;
                } else {
                    curWcdmaRscp = curWcdmaRscp2;
                }
                int curLteRsrp2 = oldSignalStrength.getLteRsrp();
                if (isLteSignalValid(curLteRsrp2)) {
                    needZero2 = true;
                    curLteRsrp = -140;
                } else {
                    curLteRsrp = curLteRsrp2;
                }
                int curNrRsrp3 = OppoSignalStrengthStandard.getNrRsrp(oldSignalStrength);
                if (isLteSignalValid(curNrRsrp3)) {
                    needZero = true;
                    curNrRsrp2 = -140;
                } else {
                    needZero = needZero2;
                    curNrRsrp2 = curNrRsrp3;
                }
                OppoServiceStateTrackerUtil.copyFrom(signalStrength, new SignalStrength(new CellSignalStrengthCdma(curCdmaDbm, Integer.MAX_VALUE, curEvdoDbm, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthGsm(curGsmSignalStrength, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthWcdma(Integer.MAX_VALUE, Integer.MAX_VALUE, curWcdmaRscp, Integer.MAX_VALUE), new CellSignalStrengthTdscdma(Integer.MAX_VALUE, Integer.MAX_VALUE, curTdScdmaRscp), new CellSignalStrengthLte(Integer.MAX_VALUE, curLteRsrp, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthNr(curNrRsrp2, Integer.MAX_VALUE, Integer.MAX_VALUE, curNrRsrp2, Integer.MAX_VALUE, Integer.MAX_VALUE)));
                if (needZero) {
                    tempSignalStrength.setOEMLevel(0, 0);
                    return;
                } else {
                    tempSignalStrength.setOEMLevel(1, 1);
                    return;
                }
            }
        }
        int pendingType = getSignalStrengthType(signalStrength);
        int nowType = getSignalStrengthType(oldSignalStrength);
        if (pendingType != 0 && nowType != 0 && pendingType != nowType) {
            logd("fixSignalStrength pendingType." + pendingType + ",nowType:" + nowType);
            if (oldSignalOEMLevel_0 - tempSignalStrength.getOEMLevel_0() > 0) {
                int gsmSignalStrength = signalStrength.getGsmDbm();
                if (pendingType == 1) {
                    gsmSignalStrength = simulateGsmSignal(oldSignalOEMLevel_0, gsmSignalStrength);
                }
                int cdmaDbm = signalStrength.getCdmaDbm();
                if (pendingType == 5) {
                    cdmaDbm = simulateCdmaSignal(oldSignalOEMLevel_0, cdmaDbm);
                }
                int evdoDbm2 = signalStrength.getEvdoDbm();
                if (pendingType == 6) {
                    evdoDbm = simulateCdmaSignal(oldSignalOEMLevel_0, evdoDbm2);
                } else {
                    evdoDbm = evdoDbm2;
                }
                int tdScdmaRscp2 = signalStrength.getTdScdmaDbm();
                if (pendingType == 2) {
                    tdScdmaRscp = simulateWcdmaSignal(oldSignalOEMLevel_0, tdScdmaRscp2);
                } else {
                    tdScdmaRscp = tdScdmaRscp2;
                }
                int wcdmaRscp2 = signalStrength.getWcdmaDbm();
                if (pendingType == 4) {
                    wcdmaRscp = simulateWcdmaSignal(oldSignalOEMLevel_0, wcdmaRscp2);
                } else {
                    wcdmaRscp = wcdmaRscp2;
                }
                int lteRsrp2 = signalStrength.getLteRsrp();
                if (pendingType == 3) {
                    lteRsrp = simulateLteSignal(oldSignalOEMLevel_0, lteRsrp2);
                } else {
                    lteRsrp = lteRsrp2;
                }
                int curNrRsrp4 = OppoSignalStrengthStandard.getNrRsrp(signalStrength);
                if (pendingType == 7) {
                    curNrRsrp = simulateLteSignal(oldSignalOEMLevel_0, curNrRsrp4);
                } else {
                    curNrRsrp = curNrRsrp4;
                }
                OppoServiceStateTrackerUtil.copyFrom(signalStrength, new SignalStrength(new CellSignalStrengthCdma(cdmaDbm, Integer.MAX_VALUE, evdoDbm, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthGsm(gsmSignalStrength, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthWcdma(Integer.MAX_VALUE, Integer.MAX_VALUE, wcdmaRscp, Integer.MAX_VALUE), new CellSignalStrengthTdscdma(Integer.MAX_VALUE, Integer.MAX_VALUE, tdScdmaRscp), new CellSignalStrengthLte(Integer.MAX_VALUE, lteRsrp, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthNr(curNrRsrp, Integer.MAX_VALUE, Integer.MAX_VALUE, curNrRsrp, Integer.MAX_VALUE, Integer.MAX_VALUE)));
                tempSignalStrength.setOEMLevel(oldSignalOEMLevel_0, oldSignalOEMLevel_0);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean updateOEMSmooth(ServiceState st, SignalStrength oldSignalStrength) {
        wlanAssistantBySignalStrength();
        int oldSignalOEMLevel_0 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, oldSignalStrength)).getOEMLevel_0();
        int pendingSignalOEMLevel_0 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mPendingSignalStrength)).getOEMLevel_0();
        if (pendingSignalOEMLevel_0 >= 4 || pendingSignalOEMLevel_0 > oldSignalOEMLevel_0) {
            OppoServiceStateTrackerUtil.copyFrom(oldSignalStrength, this.mPendingSignalStrength);
            removeMessages(2);
            logd("updateOEMSmooth:direct update..");
            return true;
        }
        AlphaFilterResult ret = alphaFiltering(oldSignalStrength);
        if (ret.delay == -1) {
            logd("updateOEMSmooth:signalstrength no change..");
            removeMessages(2);
            return false;
        } else if (ret.delay == 0) {
            logd("updateOEMSmooth:signalstrength update now..");
            OppoServiceStateTrackerUtil.copyFrom(oldSignalStrength, this.mPendingSignalStrength);
            removeMessages(2);
            return true;
        } else {
            long delay = (long) ret.delay;
            if (hasMessages(2)) {
                removeMessages(2);
                long delay2 = (this.mLastUpdateSingalTime + delay) - SystemClock.elapsedRealtime();
                logd("hasMessages delay:" + delay2);
                if (delay2 <= 0) {
                    smoothSignalStrength(ret.nextSignalStrength);
                } else {
                    if (delay2 > 18000) {
                        delay2 = 18000;
                    }
                    sendSmoothMessage((int) delay2, ret.nextSignalStrength);
                }
                return false;
            }
            this.mLastUpdateSingalTime = SystemClock.elapsedRealtime();
            logd("noMessages delay:" + delay);
            sendSmoothMessage((int) delay, ret.nextSignalStrength);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void removeSmoothMessage() {
        removeMessages(2);
    }

    private boolean needShowZero() {
        int nt = this.mSST.mSS.getRilDataRadioTechnology();
        if (nt == 0) {
            nt = this.mSST.mSS.getRilVoiceRadioTechnology();
        }
        if (nt == 1 || nt == 2 || nt == 16 || nt == 4 || nt == 5 || nt == 6 || nt == 7 || nt == 8 || nt == 12 || nt == 13) {
            return false;
        }
        return true;
    }

    private void smoothSignalStrength(SignalStrength signalStrength) {
        OppoSignalStrength tmpPendingSignalStrength = (OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mPendingSignalStrength);
        OppoSignalStrength tempSignalStrength = (OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, signalStrength);
        if (signalStrength == null || tmpPendingSignalStrength.getOEMLevel_0() > tempSignalStrength.getOEMLevel_0()) {
            setSignalStrength(this.mPendingSignalStrength);
        } else {
            setSignalStrength(signalStrength);
        }
        oppoNotifySignalStrength();
        AlphaFilterResult ret = alphaFiltering(this.mSST.getSignalStrength());
        if (ret.delay == -1) {
            removeMessages(2);
        } else if (ret.delay == 0) {
            setSignalStrength(this.mPendingSignalStrength);
            oppoNotifySignalStrength();
        } else {
            sendSmoothMessage(ret.delay, ret.nextSignalStrength);
        }
    }

    /* access modifiers changed from: protected */
    public boolean oppoNotifySignalStrength() {
        this.mLastUpdateSingalTime = SystemClock.elapsedRealtime();
        try {
            this.mPhone.notifySignalStrength();
            logd("oppoNotifySignalStrength, mSignalStrength: " + this.mSST.getSignalStrength());
            return true;
        } catch (NullPointerException ex) {
            loge("oppoNotifySignalStrength() Phone already destroyed: " + ex + "SignalStrength not notified");
            return false;
        }
    }

    private void fixSignalStrengthType() {
        OppoSignalStrengthStandard.getSignalStrengthLevel(this.mPendingSignalStrength, needShowZero());
        OppoSignalStrengthStandard.getSignalStrengthLevel(this.mOrigSignalStrength, needShowZero());
        if (((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mPendingSignalStrength)).getOEMLevel_0() >= ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mSST.getSignalStrength())).getOEMLevel_0()) {
            removeMessages(2);
            setSignalStrength(this.mPendingSignalStrength);
        }
    }

    private void updateSignalStrengthForServiceState(ServiceState newSS, boolean isForce) {
        boolean hasRilDataRadioTechnologyChanged = true;
        boolean hasRegistered = this.mSST.mSS.getVoiceRegState() != 0 && newSS.getVoiceRegState() == 0;
        boolean hasDataAttached = this.mSST.mSS.getDataRegState() != 0 && newSS.getDataRegState() == 0;
        boolean hasRilVoiceRadioTechnologyChanged = this.mSST.mSS.getRilVoiceRadioTechnology() != newSS.getRilVoiceRadioTechnology();
        if (this.mSST.mSS.getRilDataRadioTechnology() == newSS.getRilDataRadioTechnology()) {
            hasRilDataRadioTechnologyChanged = false;
        }
        if (hasRegistered || hasDataAttached || isForce) {
            OppoServiceStateTrackerUtil.copyFrom(this.mPendingSignalStrength, this.mOrigSignalStrength);
            setSignalStrength(this.mOrigSignalStrength);
            removeMessages(2);
            oppoNotifySignalStrength();
        }
        if (hasRilVoiceRadioTechnologyChanged || hasRilDataRadioTechnologyChanged) {
            fixSignalStrengthType();
            this.mLastUpdateSingalTime = SystemClock.elapsedRealtime();
        }
    }

    private int getNetworkModeBySS(int nt) {
        if (nt == 1 || nt == 2 || nt == 16) {
            return 1;
        }
        if (nt == 3 || nt == 9 || nt == 10 || nt == 11 || nt == 15) {
            return 4;
        }
        if (nt == 4 || nt == 5 || nt == 6) {
            return 5;
        }
        if (nt == 7 || nt == 8 || nt == 12 || nt == 13) {
            return 6;
        }
        if (nt == 17) {
            return 2;
        }
        if (nt == 14 || nt == 19) {
            return 3;
        }
        return 0;
    }

    private int getOosReportTimeBySS() {
        int delay = DELAYTIME_40S;
        int regRat = this.mSST.mSS.getRilDataRadioTechnology();
        if (regRat == 0) {
            regRat = this.mSST.mSS.getRilVoiceRadioTechnology();
        }
        int nwType = getNetworkModeBySS(regRat);
        if (nwType == 3) {
            delay = LTE_ALLOWED_NO_SERVICE_INTERVAL;
        } else if (nwType == 6) {
            delay = EVDO_ALLOWED_NO_SERVICE_INTERVAL;
        } else if (nwType == 4) {
            delay = W_ALLOWED_NO_SERVICE_INTERVAL;
        } else if (nwType == 2) {
            delay = DELAYTIME_40S;
        } else if (nwType == 1) {
            delay = GSM_ALLOWED_NO_SERVICE_INTERVAL;
        }
        logd("==getOosReportTimeBySS regRat=" + regRat + "delay=" + delay);
        return delay;
    }

    public String oppoGetPlmn() {
        GsmCdmaPhone gsmCdmaPhone = this.mPhone;
        if (gsmCdmaPhone == null || !gsmCdmaPhone.isPhoneTypeGsm()) {
            return null;
        }
        GsmCdmaPhone gsmCdmaPhone2 = this.mPhone;
        IccRecords iccRecords = gsmCdmaPhone2 != null ? gsmCdmaPhone2.getIccRecords() : null;
        String simOperatorNumeric = iccRecords != null ? iccRecords.getOperatorNumeric() : "";
        String strNumPlmn = this.mSST.mSS.getOperatorNumeric();
        if (OppoServiceStateTrackerUtil.isGT4GSimCardCheck(simOperatorNumeric) || OppoServiceStateTrackerUtil.isNationalRoaming(strNumPlmn, simOperatorNumeric) || "50501".equals(strNumPlmn) || "50502".equals(strNumPlmn)) {
            logd("TW GT or NationalRoaming case, don't process language name");
            return null;
        } else if (this.mSST.mSS == null) {
            return null;
        } else {
            String languageName = OppoServiceStateTrackerUtil.oppoGetPlmnOverride(this.mPhone.getContext(), this.mSST.mSS.getOperatorNumeric(), this.mSST.mSS);
            logd("updateSpnDisplay: languageName = " + languageName);
            if (TextUtils.isEmpty(languageName) || languageName.equals(this.mSST.mSS.getOperatorNumeric())) {
                return null;
            }
            return languageName;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x017c  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x01d9  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01dc  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01e4  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0202  */
    public String getPnnName(String plmn, boolean longName) {
        String pnnName;
        String simPlmn;
        String simPlmn2;
        String simPlmn3;
        String str;
        logd("getPnnName begin");
        String pnnName2 = null;
        pnnName = null;
        pnnName = null;
        pnnName = null;
        pnnName = null;
        pnnName = null;
        pnnName = null;
        String pnnName3 = null;
        GsmCdmaPhone gsmCdmaPhone = this.mPhone;
        if (gsmCdmaPhone != null && gsmCdmaPhone.isPhoneTypeGsm()) {
            SIMRecords simRecords = null;
            GsmCdmaPhone gsmCdmaPhone2 = this.mPhone;
            IccRecords r = gsmCdmaPhone2 != null ? gsmCdmaPhone2.getIccRecords() : null;
            if (r != null) {
                simRecords = (SIMRecords) r;
            }
            String eons = null;
            eons = null;
            if (this.mSST.mSS != null) {
                this.mSST.mSS.getOperatorNumeric();
            }
            String simCardMccMnc = r != null ? r.getOperatorNumeric() : null;
            String spn = r != null ? r.getServiceProviderName() : null;
            AbstractSIMRecords tmpSimRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, simRecords);
            logd("getPnnFromEons spn = " + spn);
            if (OppoServiceStateTrackerUtil.isVodafoneOperatorCheck(simCardMccMnc)) {
                logd("getPnnFromEons isVodafoneOperatorCheck");
                if (OppoServiceStateTrackerUtil.isVodafoneHomePlmn(plmn, simCardMccMnc)) {
                    if (!TextUtils.isEmpty(spn)) {
                        pnnName3 = spn;
                        logd("getPnnName from spn, pnnName=" + pnnName3);
                    } else if (!(tmpSimRecords == null || tmpSimRecords.getEFpnnNetworkNames(0) == null)) {
                        pnnName3 = tmpSimRecords.getEFpnnNetworkNames(0).sFullName;
                        logd("getPnnName from pnn[0], pnnName=" + pnnName3);
                    }
                } else if (!OppoServiceStateTrackerUtil.isVodafoneNationalRoaming(this.mSST.mSS.getOperatorNumeric(), simCardMccMnc) || TextUtils.isEmpty(plmn) || !plmn.equals("50502")) {
                    if (!(this.mSST.mSS.getVoiceRegState() == 0 || 18 != this.mSST.mSS.getRilDataRadioTechnology() || tmpSimRecords.getEFpnnNetworkNames(0) == null)) {
                        pnnName3 = tmpSimRecords.getEFpnnNetworkNames(0).sFullName;
                        logd("getPnnName from pnn[0] for IWLAN, pnnName=" + pnnName3);
                    }
                } else if (tmpSimRecords.getEFpnnNetworkNames(1) != null) {
                    pnnName3 = tmpSimRecords.getEFpnnNetworkNames(1).sFullName;
                    logd("getPnnName from pnn[1], pnnName=" + pnnName3);
                }
            }
            if (OppoServiceStateTrackerUtil.isGT4GSimCardCheck(simCardMccMnc)) {
                logd("getPnnFromEons isGT4GSimCardCheck");
                if (!TextUtils.isEmpty(spn)) {
                    logd("getPnnName from spn, pnnName=" + spn);
                    pnnName = spn;
                } else if (!(tmpSimRecords == null || tmpSimRecords.getEFpnnNetworkNames(0) == null)) {
                    String pnnName4 = tmpSimRecords.getEFpnnNetworkNames(0).sFullName;
                    logd("getPnnName from pnn[0], pnnName=" + pnnName4);
                    pnnName = pnnName4;
                }
                CellLocation mCellLoc = this.mSST.getCellLocation();
                if (plmn != 0) {
                    if (!plmn.equals("46605") && !plmn.equals("46697")) {
                        simPlmn2 = null;
                        simPlmn = simPlmn2;
                        if ("46605".equals(plmn)) {
                        }
                        pnnName2 = pnnName;
                        logd("getPnnName simPlmn = " + simPlmn);
                    } else if (OppoServiceStateTrackerUtil.isGT4GSimCardCheck(simCardMccMnc)) {
                        if (mCellLoc != null) {
                            if (tmpSimRecords != null) {
                                try {
                                    try {
                                        str = tmpSimRecords.getEonsIfExist(plmn, ((GsmCellLocation) mCellLoc).getLac(), longName);
                                        simPlmn3 = null;
                                    } catch (RuntimeException e) {
                                        ex = e;
                                        StringBuilder sb = new StringBuilder();
                                        simPlmn3 = null;
                                        sb.append("Exception while getEonsIfExist. ");
                                        sb.append(ex);
                                        loge(sb.toString());
                                        if (eons != null) {
                                        }
                                        if (TextUtils.isEmpty(simPlmn)) {
                                        }
                                        pnnName = simPlmn;
                                        if ("46605".equals(plmn)) {
                                        }
                                        pnnName2 = pnnName;
                                        logd("getPnnName simPlmn = " + simPlmn);
                                        return pnnName2;
                                    }
                                } catch (RuntimeException e2) {
                                    ex = e2;
                                    StringBuilder sb2 = new StringBuilder();
                                    simPlmn3 = null;
                                    sb2.append("Exception while getEonsIfExist. ");
                                    sb2.append(ex);
                                    loge(sb2.toString());
                                    if (eons != null) {
                                    }
                                    if (TextUtils.isEmpty(simPlmn)) {
                                    }
                                    pnnName = simPlmn;
                                    if ("46605".equals(plmn)) {
                                    }
                                    pnnName2 = pnnName;
                                    logd("getPnnName simPlmn = " + simPlmn);
                                    return pnnName2;
                                }
                            } else {
                                simPlmn3 = null;
                                str = null;
                            }
                            eons = str;
                        } else {
                            simPlmn3 = null;
                        }
                        if (eons != null) {
                            simPlmn = eons;
                        } else {
                            simPlmn = simPlmn3;
                        }
                        if (TextUtils.isEmpty(simPlmn)) {
                            logd("No matched eons and No CPHS ONS");
                            if (plmn.equals(simCardMccMnc)) {
                                logd("Home PLMN, get CPHS ons");
                            }
                        }
                        pnnName = simPlmn;
                        if ("46605".equals(plmn) || !"46692".equals(simCardMccMnc)) {
                            pnnName2 = pnnName;
                        } else {
                            pnnName2 = "GT 4G";
                        }
                        logd("getPnnName simPlmn = " + simPlmn);
                    }
                }
                simPlmn2 = null;
                simPlmn = simPlmn2;
                if ("46605".equals(plmn)) {
                }
                pnnName2 = pnnName;
                logd("getPnnName simPlmn = " + simPlmn);
            }
            pnnName = pnnName3;
            CellLocation mCellLoc2 = this.mSST.getCellLocation();
            if (plmn != 0) {
            }
            simPlmn2 = null;
            simPlmn = simPlmn2;
            if ("46605".equals(plmn)) {
            }
            pnnName2 = pnnName;
            logd("getPnnName simPlmn = " + simPlmn);
        }
        return pnnName2;
    }

    public String getPlmnResult() {
        return this.temPlmn;
    }

    public String getSpnResult() {
        return this.temSpn;
    }

    public boolean getShowPlmnResult() {
        return this.temShowPlmn;
    }

    public boolean getShowSpnResult() {
        return this.temShowSpn;
    }

    /* access modifiers changed from: protected */
    public void oppoSetOperatorAlpha(String val) {
        try {
            TelephonyManager.getDefault();
            TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "gsm.sim.operator.spn", val);
        } catch (Exception ex) {
            logd("leon gsm.sim.operator.spn= ex." + ex.getMessage());
        }
        this.mOemSpn = val;
        logd("leon OemSpn=" + val);
    }

    /* access modifiers changed from: protected */
    public void oppoVirtualSimCheck(String operator2, String plmn, String spn, boolean showplmn, boolean showspn) {
        this.mShowPlmn = showplmn;
        this.mShowSPn = showspn;
        this.mNewPlmn = null;
        boolean isinCnlist = OemTelephonyUtils.isInCnList(this.mContext, spn);
        if (this.mSST.mSS.getRoaming()) {
            this.mShowPlmn = true;
            this.mShowSPn = false;
            if (OemTelephonyUtils.isInCmccList(this.mContext, spn) && ("45412".equals(operator2) || "45413".equals(operator2))) {
                plmn = "CMHK";
                this.mNewPlmn = plmn;
            }
        } else if (isinCnlist && !TextUtils.isEmpty(plmn)) {
            this.mShowPlmn = true;
            this.mShowSPn = false;
        }
        if (!this.mShowPlmn && !this.mShowSPn) {
            this.mShowPlmn = !TextUtils.isEmpty(plmn);
            this.mShowSPn = !this.mShowPlmn && !TextUtils.isEmpty(spn);
        }
        AbstractSubscriptionController tmpSubCtr = (AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, this.mSubscriptionController);
        if (tmpSubCtr.isHasSoftSimCard() && tmpSubCtr.getSoftSimCardSlotId() == this.mPhone.getPhoneId() && !TextUtils.isEmpty(spn)) {
            this.mShowPlmn = false;
            this.mShowSPn = true;
        }
        if (this.mShowPlmn) {
            oppoSetOperatorAlpha(plmn);
            this.mShowSPn = false;
        } else if (!this.mShowSPn || TextUtils.isEmpty(spn)) {
            oppoSetOperatorAlpha(plmn);
            this.mShowSPn = false;
        } else {
            oppoSetOperatorAlpha(spn);
        }
    }

    /* access modifiers changed from: protected */
    public int oppoGetCombRegState(ServiceState ss) {
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        if ((regState != 1 && regState != 3) || dataRegState != 0) {
            return regState;
        }
        logd("getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
    }

    /* access modifiers changed from: protected */
    public boolean needShowPlmnSpn() {
        logd("mOperatorStatus: " + this.mOperatorStatus);
        boolean mNeedShowPlmnSpn = false;
        if ("ORANGE".equals(this.mOperatorStatus) || "MOVISTAR".equals(this.mOperatorStatus)) {
            mNeedShowPlmnSpn = true;
        }
        logd("Need display plmn+spn: " + mNeedShowPlmnSpn);
        return mNeedShowPlmnSpn;
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00fd  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x010a A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x014e  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0153  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01c5  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x01df  */
    public void oppoUpdateGsmSpnDisplay(String plmn, String spn, boolean showPlmn, boolean showSpn) {
        String spn2;
        String plmn2;
        boolean showSpn2;
        boolean showPlmn2;
        GsmCdmaPhone gsmCdmaPhone;
        String iccid;
        boolean showSpn3;
        boolean showPlmn3;
        boolean ruleShowPlmn;
        boolean ruleShowSpn;
        boolean showSpn4;
        boolean showPlmn4;
        String plmn3;
        String simCardMccMnc;
        boolean[] showRules;
        GsmCdmaPhone gsmCdmaPhone2 = this.mPhone;
        IccRecords iccRecords = gsmCdmaPhone2 != null ? gsmCdmaPhone2.getIccRecords() : null;
        int cRegState = oppoGetCombRegState(this.mSST.mSS);
        String simOperatorNumeric = iccRecords != null ? iccRecords.getOperatorNumeric() : "";
        boolean z = true;
        if (this.mSST.mSS == null || (gsmCdmaPhone = this.mPhone) == null || !gsmCdmaPhone.isPhoneTypeGsm()) {
            plmn2 = plmn;
            spn2 = spn;
            showPlmn2 = showPlmn;
            showSpn2 = showSpn;
        } else {
            String plmn4 = OppoServiceStateTrackerUtil.oppoExpDisplayFormatting(this.mSST.mSS, plmn, simOperatorNumeric);
            String spn3 = OppoServiceStateTrackerUtil.oppoExpDisplayFormatting(this.mSST.mSS, spn, simOperatorNumeric);
            if (!OppoServiceStateTrackerUtil.isAUOperatorCheck(simOperatorNumeric) || TextUtils.isEmpty(spn3)) {
                spn2 = spn3;
            } else {
                spn2 = spn3.replaceAll("[\\n\\r]", " ");
            }
            try {
                iccid = this.mPhone.getIccSerialNumber().substring(0, 7);
            } catch (Exception e) {
                iccid = null;
            }
            if (!((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, this.mPhone)).getVowifiRegStatus() || (!OppoServiceStateTrackerUtil.isGT4GSimCardCheck(simOperatorNumeric) && (!"8988605".equals(iccid) || !"52505".equals(simOperatorNumeric)))) {
                showPlmn3 = showPlmn;
                showSpn3 = showSpn;
            } else {
                plmn4 = "GT WIFI";
                showPlmn3 = true;
                showSpn3 = false;
                logd("updateSpnDisplay: TW GT iwlanName =" + plmn4);
            }
            if (showPlmn3 && showSpn3 && !TextUtils.isEmpty(spn2) && cRegState == 0 && !this.mSST.mSS.getRoaming()) {
                logd("display spn first for non-roaming case, spn = " + spn2);
                showPlmn3 = false;
                showSpn3 = true;
            }
            ServiceStateTracker serviceStateTracker = this.mSST;
            int rule = serviceStateTracker.getCarrierNameDisplayBitmask(serviceStateTracker.mSS);
            if (!TextUtils.isEmpty(plmn4)) {
                ServiceStateTracker serviceStateTracker2 = this.mSST;
                if ((rule & 2) == 2) {
                    ruleShowPlmn = true;
                    if (!TextUtils.isEmpty(spn2)) {
                        ServiceStateTracker serviceStateTracker3 = this.mSST;
                        if ((rule & 1) == 1) {
                            ruleShowSpn = true;
                            if (ruleShowPlmn || !ruleShowSpn || !needShowPlmnSpn()) {
                                plmn3 = plmn4;
                                showPlmn4 = showPlmn3;
                                showSpn4 = showSpn3;
                            } else {
                                plmn3 = plmn4 + "|" + spn2;
                                showPlmn4 = true;
                                showSpn4 = false;
                            }
                            oppoVirtualSimCheck(((ServiceStateTracker) this.mSST).mSS.getOperatorNumeric(), plmn3, spn2, showPlmn4, showSpn4);
                            showPlmn2 = this.mShowPlmn;
                            showSpn2 = this.mShowSPn;
                            simCardMccMnc = iccRecords != null ? iccRecords.getOperatorNumeric() : null;
                            if (simCardMccMnc != null && simCardMccMnc.startsWith("466") && !TextUtils.isEmpty(plmn3) && !this.mSST.mSS.getRoaming()) {
                                showSpn2 = false;
                                showPlmn2 = true;
                                logd("oppoSetOemSpn is TW region , MCCMnc is " + simCardMccMnc + ", need to show plmn");
                            }
                            plmn2 = plmn3;
                            showRules = OppoServiceStateTrackerUtil.oppoExpDisplayRules(this.mSST.mSS, plmn2, spn2, simOperatorNumeric);
                            if (showRules[0] || showRules[1]) {
                                showPlmn2 = showRules[0];
                                showSpn2 = showRules[1];
                            }
                            AbstractSubscriptionController tmpSubCtr = (AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, this.mSubscriptionController);
                            if (tmpSubCtr.isHasSoftSimCard() && tmpSubCtr.getSoftSimCardSlotId() == this.mPhone.getPhoneId() && !TextUtils.isEmpty(spn2)) {
                                showPlmn2 = false;
                                showSpn2 = true;
                            }
                            if (!showPlmn2 && !TextUtils.isEmpty(plmn2)) {
                                oppoSetOperatorAlpha(plmn2);
                            } else if (showSpn2 && !TextUtils.isEmpty(spn2)) {
                                oppoSetOperatorAlpha(spn2);
                            }
                            if (this.mNewPlmn != null) {
                                plmn2 = this.mNewPlmn;
                            }
                        }
                    }
                    ruleShowSpn = false;
                    if (ruleShowPlmn) {
                    }
                    plmn3 = plmn4;
                    showPlmn4 = showPlmn3;
                    showSpn4 = showSpn3;
                    oppoVirtualSimCheck(((ServiceStateTracker) this.mSST).mSS.getOperatorNumeric(), plmn3, spn2, showPlmn4, showSpn4);
                    showPlmn2 = this.mShowPlmn;
                    showSpn2 = this.mShowSPn;
                    if (iccRecords != null) {
                    }
                    showSpn2 = false;
                    showPlmn2 = true;
                    logd("oppoSetOemSpn is TW region , MCCMnc is " + simCardMccMnc + ", need to show plmn");
                    plmn2 = plmn3;
                    showRules = OppoServiceStateTrackerUtil.oppoExpDisplayRules(this.mSST.mSS, plmn2, spn2, simOperatorNumeric);
                    showPlmn2 = showRules[0];
                    showSpn2 = showRules[1];
                    AbstractSubscriptionController tmpSubCtr2 = (AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, this.mSubscriptionController);
                    showPlmn2 = false;
                    showSpn2 = true;
                    if (!showPlmn2) {
                    }
                    oppoSetOperatorAlpha(spn2);
                    if (this.mNewPlmn != null) {
                    }
                }
            }
            ruleShowPlmn = false;
            if (!TextUtils.isEmpty(spn2)) {
            }
            ruleShowSpn = false;
            if (ruleShowPlmn) {
            }
            plmn3 = plmn4;
            showPlmn4 = showPlmn3;
            showSpn4 = showSpn3;
            oppoVirtualSimCheck(((ServiceStateTracker) this.mSST).mSS.getOperatorNumeric(), plmn3, spn2, showPlmn4, showSpn4);
            showPlmn2 = this.mShowPlmn;
            showSpn2 = this.mShowSPn;
            if (iccRecords != null) {
            }
            showSpn2 = false;
            showPlmn2 = true;
            logd("oppoSetOemSpn is TW region , MCCMnc is " + simCardMccMnc + ", need to show plmn");
            plmn2 = plmn3;
            showRules = OppoServiceStateTrackerUtil.oppoExpDisplayRules(this.mSST.mSS, plmn2, spn2, simOperatorNumeric);
            showPlmn2 = showRules[0];
            showSpn2 = showRules[1];
            AbstractSubscriptionController tmpSubCtr22 = (AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, this.mSubscriptionController);
            showPlmn2 = false;
            showSpn2 = true;
            if (!showPlmn2) {
            }
            oppoSetOperatorAlpha(spn2);
            if (this.mNewPlmn != null) {
            }
        }
        if (!showPlmn2 && !showSpn2) {
            showPlmn2 = !TextUtils.isEmpty(plmn2);
            if (showPlmn2 || TextUtils.isEmpty(spn2)) {
                z = false;
            }
            showSpn2 = z;
        }
        String strNumPlmn = this.mSST.mSS != null ? this.mSST.mSS.getOperatorNumeric() : null;
        if (cRegState == 0) {
            GsmCdmaPhone gsmCdmaPhone3 = this.mPhone;
            String iccId = gsmCdmaPhone3 != null ? gsmCdmaPhone3.getFullIccSerialNumber() : null;
            if (iccId != null && ((iccId.startsWith("896603") || iccId.startsWith("896601")) && "52015".equals(strNumPlmn))) {
                spn2 = "AIS-T";
                plmn2 = "AIS-T";
                showPlmn2 = true;
                showSpn2 = false;
                oppoSetOperatorAlpha(spn2);
                logd("display AIS-T for 52015");
            }
        }
        this.temPlmn = plmn2;
        this.temSpn = spn2;
        this.temShowPlmn = showPlmn2;
        this.temShowSpn = showSpn2;
        this.mPowerState.updateNoServiceTime();
    }

    public void oppoUpdateCdmaSpnDisplay(String plmn, String spn, boolean showPlmn, boolean showSpn) {
        GsmCdmaPhone gsmCdmaPhone;
        GsmCdmaPhone gsmCdmaPhone2 = this.mPhone;
        IccRecords iccRecords = gsmCdmaPhone2 != null ? gsmCdmaPhone2.getIccRecords() : null;
        oppoGetCombRegState(this.mSST.mSS);
        String simOperatorNumeric = iccRecords != null ? iccRecords.getOperatorNumeric() : "";
        if (!(this.mSST.mSS == null || (gsmCdmaPhone = this.mPhone) == null || gsmCdmaPhone.isPhoneTypeGsm())) {
            oppoVirtualSimCheck("", plmn, "", showPlmn, false);
            showPlmn = this.mShowPlmn;
            showSpn = this.mShowSPn;
            AbstractSubscriptionController tmpSubCtr = (AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, this.mSubscriptionController);
            if (tmpSubCtr.isHasSoftSimCard() && tmpSubCtr.getSoftSimCardSlotId() == this.mPhone.getPhoneId()) {
                spn = OemTelephonyUtils.getReadTeaServiceProviderName(this.mPhone.getContext(), simOperatorNumeric);
                if (!TextUtils.isEmpty(spn)) {
                    showSpn = true;
                    showPlmn = false;
                    oppoSetOperatorAlpha(spn);
                }
            }
        }
        if (!showPlmn && !showSpn) {
            boolean z = true;
            showPlmn = !TextUtils.isEmpty(plmn);
            if (showPlmn || TextUtils.isEmpty(spn)) {
                z = false;
            }
            showSpn = z;
        }
        this.temPlmn = plmn;
        this.temSpn = spn;
        this.temShowPlmn = showPlmn;
        this.temShowSpn = showSpn;
        this.mPowerState.updateNoServiceTime();
    }

    /* access modifiers changed from: protected */
    public void setOemSpn(String val) {
        this.mOemSpn = val;
        logd("leon OemSpn=" + this.mOemSpn);
    }

    public String getOemSpn() {
        return this.mOemSpn;
    }

    public boolean isCtCard(Phone mPhone2) {
        return OppoPhoneUtil.isCtCard(mPhone2);
    }

    public boolean isTelstraVersion() {
        if ("TELSTRA".equals(this.operator) || "TELSTRA_PREPAID".equals(this.operator) || "TELSTRA_POSTPAID".equals(this.operator)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(this.LOG_TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }

    private void wlanAssistantBySignalStrength() {
        if (OppoPhoneUtil.getWlanAssistantEnable(this.mContext)) {
            int oldSignalOEMLevel_0 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mSST.getSignalStrength())).getOEMLevel_0();
            final int pendingSignalOEMLevel_0 = ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mPendingSignalStrength)).getOEMLevel_0();
            SubscriptionManager.from(this.mContext);
            boolean myMeasureDataState = true;
            boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId();
            boolean signalLevelChanged = oldSignalOEMLevel_0 != pendingSignalOEMLevel_0;
            if (signalLevelChanged && isDefaultDataPhone) {
                StringBuilder sb = new StringBuilder();
                sb.append("WLAN+ EVENT_SIGNAL_UPDATE_CHANGED:signalLevelChanged = ");
                sb.append(signalLevelChanged);
                sb.append(" mMeasureDataState=");
                sb.append(OppoDcTrackerReference.mMeasureDataState);
                sb.append(" Roaming=");
                sb.append(this.mSST.mSS.getRoaming());
                sb.append(" DataEnabled=");
                sb.append(this.mPhone.isUserDataEnabled() || haveVsimIgnoreUserDataSetting());
                sb.append(" isDefaultDataPhone");
                sb.append(isDefaultDataPhone);
                logd(sb.toString());
                if (!OppoDcTrackerReference.mMeasureDataState || OppoDcTrackerReference.mDelayMeasure || this.mSST.mSS.getRoaming() || (!this.mPhone.isUserDataEnabled() && !haveVsimIgnoreUserDataSetting())) {
                    myMeasureDataState = false;
                }
                if (myMeasureDataState) {
                    new Thread() {
                        /* class com.oppo.internal.telephony.OppoServiceStateTracker.AnonymousClass2 */

                        public void run() {
                            NetworkRequest request;
                            ConnectivityManager connectivityManager = (ConnectivityManager) OppoServiceStateTracker.this.mContext.getSystemService("connectivity");
                            if (!ConnectivityManagerHelper.measureDataState(connectivityManager, pendingSignalOEMLevel_0) && (request = ConnectivityManagerHelper.getCelluarNetworkRequest(connectivityManager)) != null) {
                                if (OppoDcTrackerReference.mMeasureDCCallback != null) {
                                    OppoServiceStateTracker oppoServiceStateTracker = OppoServiceStateTracker.this;
                                    oppoServiceStateTracker.logd("WLAN+ EVENT_SIGNAL_UPDATE_CHANGED release DC befor request: mMeasureDataState=" + OppoDcTrackerReference.mMeasureDataState);
                                    try {
                                        connectivityManager.unregisterNetworkCallback(OppoDcTrackerReference.mMeasureDCCallback);
                                    } catch (IllegalArgumentException e) {
                                        OppoServiceStateTracker oppoServiceStateTracker2 = OppoServiceStateTracker.this;
                                        oppoServiceStateTracker2.loge("WLAN+ " + e.toString());
                                    } catch (Exception e2) {
                                        OppoServiceStateTracker oppoServiceStateTracker3 = OppoServiceStateTracker.this;
                                        oppoServiceStateTracker3.loge("WLAN+ Exception:" + e2.toString());
                                    }
                                }
                                OppoDcTrackerReference.mMeasureDCCallback = new ConnectivityManager.NetworkCallback();
                                connectivityManager.requestNetwork(request, OppoDcTrackerReference.mMeasureDCCallback);
                                ConnectivityManagerHelper.measureDataState(connectivityManager, pendingSignalOEMLevel_0);
                            }
                        }
                    }.start();
                }
            }
        }
    }

    public int getSignalLevel() {
        return ((OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, this.mPendingSignalStrength)).getOEMLevel_0();
    }

    public boolean haveVsimIgnoreUserDataSetting() {
        return OppoPhoneUtil.isVsimIgnoreUserDataSetting(this.mContext) && OppoUiccManagerImpl.getInstance().getSoftSimCardSlotId() == this.mPhone.getPhoneId();
    }

    private void updatePhoneMonitorSignalStrength(SignalStrength signal) {
        OppoPhoneStateMonitor stateMonitor;
        NetworkDiagnoseService diagnoseService = NetworkDiagnoseService.getInstance();
        if (diagnoseService != null && (stateMonitor = diagnoseService.getPhoneStateMonitor(this.mPhone.getPhoneId())) != null) {
            stateMonitor.onSignalStrengthChanged(signal);
        }
    }

    private void setSignalStrength(SignalStrength signalStrength) {
        ((AbstractServiceStateTracker) OemTelephonyUtils.typeCasting(AbstractServiceStateTracker.class, this.mSST)).setSignalStrength(signalStrength);
    }

    public SignalStrength getOrigSignalStrength() {
        return this.mOrigSignalStrength;
    }

    public boolean updateOperatorRoaming(ServiceState ss, String simMccmnc, boolean roaming) {
        boolean oRoaming = roaming;
        String serviceMccmnc = ss.getOperatorNumeric();
        if (OppoServiceStateTrackerUtil.isVodafoneNationalRoaming(serviceMccmnc, simMccmnc)) {
            oRoaming = false;
        } else if (OppoServiceStateTrackerUtil.isVodafoneVersion()) {
            if (TextUtils.isEmpty(simMccmnc)) {
                if (OppoServiceStateTrackerUtil.isVodafoneOperatorCheck(serviceMccmnc)) {
                    oRoaming = false;
                }
            } else if (OppoServiceStateTrackerUtil.isVodafoneOperatorCheck(simMccmnc) && (TextUtils.isEmpty(serviceMccmnc) || OppoServiceStateTrackerUtil.isVodafoneOperatorCheck(serviceMccmnc))) {
                oRoaming = false;
            }
            if (TextUtils.isEmpty(serviceMccmnc) && TextUtils.isEmpty(simMccmnc)) {
                oRoaming = false;
            }
        }
        logd("serviceMccmnc = " + serviceMccmnc + ",simMccmnc = " + simMccmnc + " ,setRoaming to " + oRoaming);
        return oRoaming;
    }

    public void oppoAddDataCallCount() {
        OppoNetworkPowerState.addDataCallCount();
    }

    public void oppoAddSmsSendCount() {
        OppoNetworkPowerState.addSmsSendCount();
    }

    public void oppoAddNitzCount() {
        OppoNetworkPowerState.addNitzCount();
    }

    public void oppoUpdateVoiceRegState(int state) {
        this.mPowerState.updateVoiceRegState(state);
    }

    public boolean checkCtMacauSimRoamingState(ServiceState mNewSS) {
        String tmpSimMccMnc = TelephonyManager.from(this.mPhone.getContext()).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if ("45507".equals(tmpSimMccMnc) || "45502".equals(tmpSimMccMnc)) {
            String tmpOprMccMnc = mNewSS.getVoiceOperatorNumeric();
            if (tmpOprMccMnc == null) {
                tmpOprMccMnc = mNewSS.getDataOperatorNumeric();
            }
            if ("46011".equals(tmpOprMccMnc) || "46003".equals(tmpOprMccMnc)) {
                logd("Force Macau CT card to show roaming icon when roaming in China CT network for MTK platform");
                return true;
            }
        }
        if (!"46011".equals(tmpSimMccMnc) && !"46003".equals(tmpSimMccMnc)) {
            return false;
        }
        String tmpOprMccMnc2 = mNewSS.getVoiceOperatorNumeric();
        if (tmpOprMccMnc2 == null) {
            tmpOprMccMnc2 = mNewSS.getDataOperatorNumeric();
        }
        if (!"45507".equals(tmpOprMccMnc2) && !"45502".equals(tmpOprMccMnc2)) {
            return false;
        }
        logd("Force China CT card to show roaming icon when roaming in Macau CT network for MTK platform");
        return true;
    }

    public void oppoPollStateDone(ServiceState mNewSS) {
        updateForTwoCTCard(mNewSS);
        logd("romFeature = " + this.romFeature);
        if (("allnetcmccdeep".equals(this.romFeature) || "allnetcmccmp".equals(this.romFeature)) && oppoIsCMCCRatCustEnabled() && this.mPhone.getSubId() != SubscriptionManager.getDefaultDataSubscriptionId() && oppoIsCmccCu3G(mNewSS)) {
            mNewSS.setRilVoiceRadioTechnology(16);
            if (mNewSS.getDataRegState() == 0) {
                mNewSS.setRilDataRadioTechnology(2);
            }
        }
    }

    public void updateForTwoCTCard(ServiceState mNewSS) {
        try {
            if (OemConstant.isCtCard(this.mPhone)) {
                int currentSubId = this.mPhone.getSubId();
                int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
                boolean isNotCurrentSubId = SubscriptionManager.isUsableSubIdValue(currentSubId) && SubscriptionManager.isUsableSubIdValue(defaultDataSubId) && currentSubId != defaultDataSubId;
                ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
                int enabledVolte = Settings.Global.getInt(contentResolver, "volte_vt_enabled" + currentSubId, 0);
                logd("pollStateDone currentSubId = " + currentSubId + " defaultDataSubId = " + defaultDataSubId + " enabledVolte = " + enabledVolte);
                Phone imsPhone = this.mPhone.getImsPhone();
                Phone dataPhone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(defaultDataSubId));
                logd("pollStateDone: dataPhone subId = " + defaultDataSubId + ", isCtCard = " + OppoPhoneUtil.isCtCard(dataPhone));
                if (imsPhone != null && OppoPhoneUtil.isCtCard(dataPhone)) {
                    logd("pollStateDone imsPhone.isImsRegistered = " + imsPhone.isImsRegistered());
                    if (!isNotCurrentSubId || ((enabledVolte != 0 && imsPhone.isImsRegistered()) || mNewSS.getRoaming() || mNewSS.getRilDataRadioTechnology() != 14)) {
                        this.mCtStatus = false;
                        return;
                    }
                    mNewSS.setDataRegState(1);
                    this.mCtStatus = true;
                }
            }
        } catch (Exception e) {
        }
    }

    public void onSubscriptionsChangedForOppo(int subId) {
        if (this.needCheckCuSs) {
            int oSimType = oppoGetIconTint(1 - this.mPhone.getPhoneId());
            if (oSimType != -1) {
                this.needCheckCuSs = false;
            }
            if (oSimType == 2) {
                this.mSST.mSS.setRilVoiceRadioTechnology(16);
                if (this.mSST.mSS.getDataRegState() == 0) {
                    this.mSST.mSS.setRilDataRadioTechnology(2);
                }
                logd("onSubscriptionsChanged  notify cu 2G ss ");
                this.mPhone.notifyServiceStateChanged(this.mSST.mSS);
                return;
            }
        }
        if (this.mSubscriptionController.isActiveSubId(subId) && getOemRegState(this.mSST.mSS) == 0) {
            Rlog.d(this.LOG_TAG, "onSubscriptionsChanged  notify again");
            this.mPhone.notifyServiceStateChanged(this.mSST.mSS);
        }
    }

    /* access modifiers changed from: protected */
    public boolean oppoIsCmccCu3G(ServiceState ss) {
        boolean isCmccCu3G = false;
        boolean isCu3G = false;
        this.mSST.getSystemProperty("gsm.sim.operator.numeric", "");
        if (oppoGetSimType(ss.getOperatorNumeric()) == 3 && !ss.getRoaming() && ss.getRilVoiceRadioTechnology() == 3) {
            isCu3G = true;
        }
        logd("oppoIsCmccCu3G  isCu3G = " + isCu3G);
        if (isCu3G) {
            int oPhoneid = 1 - this.mPhone.getPhoneId();
            Phone oPhone = PhoneFactory.getPhone(oPhoneid);
            if (oPhone != null) {
                ServiceState oSS = oPhone.getServiceState();
                String oOperatorNumeric = oSS != null ? oSS.getOperatorNumeric() : null;
                TelephonyManager.getTelephonyProperty(oPhoneid, "gsm.sim.operator.numeric", "");
                if (oppoGetIconTint(oPhoneid) == 2 || oppoGetSimType(oOperatorNumeric) == 2) {
                    this.needCheckCuSs = false;
                    isCmccCu3G = true;
                } else if (oppoGetIconTint(oPhoneid) == -1) {
                    this.needCheckCuSs = true;
                }
            }
            logd("oppoIsCmccCu3G  isCmccCu3G = " + isCmccCu3G);
        }
        return isCmccCu3G;
    }

    /* access modifiers changed from: protected */
    public int oppoGetSimType(String operatorNumeric) {
        int simType = -1;
        if (!TextUtils.isEmpty(operatorNumeric)) {
            if (operatorNumeric.equals("46000") || operatorNumeric.equals("46002") || operatorNumeric.equals("46004") || operatorNumeric.equals("46007") || operatorNumeric.equals("46008")) {
                simType = 2;
            } else if (operatorNumeric.equals("46001") || operatorNumeric.equals("46009")) {
                simType = 3;
            }
        }
        logd("oppoGetSimType  operatorNumeric = " + operatorNumeric + " SIM_TYPE = " + simType);
        return simType;
    }

    /* access modifiers changed from: protected */
    public int oppoGetIconTint(int phoneId) {
        int iconTint = -1;
        SubscriptionInfo sir = SubscriptionManager.from(this.mPhone.getContext()).getActiveSubscriptionInfoForSimSlotIndex(phoneId);
        if (sir != null) {
            iconTint = sir.getIconTint();
        }
        logd("oppoGetIconTint  iconTint = " + iconTint);
        return iconTint;
    }

    /* access modifiers changed from: protected */
    public boolean oppoIsCMCCRatCustEnabled() {
        if (SystemProperties.get("persist.radio.cmcc.ratcust", "0").equals("1")) {
            return true;
        }
        return false;
    }

    public boolean isNeedupdateNitzTime() {
        try {
            boolean hascard = this.mPhone.getIccCard().hasIccCard();
            if (hascard) {
                return true;
            }
            String iccId = SystemProperties.get(new String[]{"vendor.ril.iccid.sim1", "vendor.ril.iccid.sim2", "vendor.ril.iccid.sim3", "vendor.ril.iccid.sim4"}[this.mPhone.getPhoneId()]);
            if (iccId != null && !iccId.equals("") && !iccId.equals("N/A")) {
                hascard = true;
            }
            if (hascard) {
                return true;
            }
            logd("no card stop nitz");
            return false;
        } catch (Exception e) {
            logd("uknown error");
            return true;
        }
    }

    public void broadcastMccChange(String plmn) {
        int phoneId = this.mPhone.getPhoneId();
        int phoneId2 = phoneId >= 10 ? phoneId - 10 : phoneId;
        if (plmn == null || TextUtils.isEmpty(plmn)) {
            logd("broadcastMccChange  plmn is null, do not broadcast");
            return;
        }
        String pMcc = plmn.substring(0, 3);
        setMccProperties(phoneId2, pMcc);
        Intent intent = new Intent("android.telephony.action.mcc_change");
        intent.putExtra(RegionLockPlmnListParser.PlmnCodeEntry.MCC_ATTR, pMcc);
        intent.putExtra(OppoSubscriptionController.INTENT_KEY_SLOT_ID, phoneId2);
        intent.addFlags(16777216);
        this.mPhone.getContext().sendBroadcast(intent);
        String sysMcc = SystemProperties.get("android.telephony.mcc_change", "");
        String sysMcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        logd("broadcastMccChange  sysMcc:" + sysMcc + "  sysMcc2:" + sysMcc2);
    }

    public void cleanMccProperties(int phoneId) {
        String sysMcc = SystemProperties.get("android.telephony.mcc_change", "");
        String sysMcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (!TextUtils.isEmpty(sysMcc) && !TextUtils.isEmpty(sysMcc2)) {
            logd("cleanMccProperties phoneId:" + phoneId + " sysMcc:" + sysMcc + " sysMcc2:" + sysMcc2);
            if (phoneId == 1 || phoneId == 11) {
                SystemProperties.set("android.telephony.mcc_change2", "");
            } else {
                SystemProperties.set("android.telephony.mcc_change", "");
            }
        }
    }

    public void setMccProperties(int phoneId, String mcc) {
        logd("setMccProperties: phoneId = " + phoneId + "  mcc:" + mcc);
        if (this.mSubscriptionController.getActiveSubInfoCount(getClass().getPackage().getName()) <= 1) {
            SystemProperties.set("android.telephony.mcc_change", mcc);
            SystemProperties.set("android.telephony.mcc_change2", mcc);
        } else if (phoneId == 1 || phoneId == 11) {
            SystemProperties.set("android.telephony.mcc_change2", mcc);
        } else {
            SystemProperties.set("android.telephony.mcc_change", mcc);
        }
    }

    public CellLocation oppoGetCTLteCellLocation() {
        List<CellInfo> result = this.mSST.getAllCellInfo();
        logd("guix oppoGetCTLteCellLocation(): result =" + result);
        if (result == null) {
            return null;
        }
        GsmCellLocation cellLocOther = new GsmCellLocation();
        for (CellInfo ci : result) {
            if (ci instanceof CellInfoGsm) {
                CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) ci).getCellIdentity();
                cellLocOther.setLacAndCid(cellIdentityGsm.getLac(), cellIdentityGsm.getCid());
                cellLocOther.setPsc(cellIdentityGsm.getPsc());
                logd("guix getCellLocation(): X ret GSM info=" + cellLocOther);
                return cellLocOther;
            } else if (ci instanceof CellInfoWcdma) {
                CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) ci).getCellIdentity();
                cellLocOther.setLacAndCid(cellIdentityWcdma.getLac(), cellIdentityWcdma.getCid());
                cellLocOther.setPsc(cellIdentityWcdma.getPsc());
                logd("guix getCellLocation(): X ret WCDMA info=" + cellLocOther);
                return cellLocOther;
            } else if ((ci instanceof CellInfoLte) && (cellLocOther.getLac() < 0 || cellLocOther.getCid() < 0)) {
                CellIdentityLte cellIdentityLte = ((CellInfoLte) ci).getCellIdentity();
                if (!(cellIdentityLte.getTac() == Integer.MAX_VALUE || cellIdentityLte.getCi() == Integer.MAX_VALUE)) {
                    cellLocOther.setLacAndCid(cellIdentityLte.getTac(), cellIdentityLte.getCi());
                    cellLocOther.setPsc(0);
                    logd("guix getCellLocation(): possible LTE cellLocOther=" + cellLocOther);
                    return cellLocOther;
                }
            }
        }
        logd("guix getCellLocation(): X ret best answer cellLocOther=" + cellLocOther);
        return cellLocOther;
    }

    public boolean checkDeepSleepStatus(Context context, List<CellInfo> lastCellInfoList, Message rspMsg) {
        boolean result = false;
        if (OemTelephonyUtils.isInDeepSleepStatus(context)) {
            if (rspMsg != null) {
                if (lastCellInfoList != null) {
                    logd("checkDeepSleepStatus(): return last cell info list in deep sleep state");
                    AsyncResult.forMessage(rspMsg, lastCellInfoList, (Throwable) null);
                } else {
                    logd("checkDeepSleepStatus(): return with exception when last cell info list is null in deep sleep state");
                    AsyncResult.forMessage(rspMsg, (Object) null, new Exception("can not get cell info in deep sleep status"));
                }
                rspMsg.sendToTarget();
            } else {
                loge("SST.requestAllCellInfo(): return with no response when in deep sleep state");
            }
            result = true;
        }
        logd("checkDeepSleepStatus(): result:" + result);
        return result;
    }

    public boolean isOppoRegionLockedState(ServiceState mNewSS, CellIdentity id, boolean hasLocationChanged) {
        OemServiceRegDurationState oemServiceRegDurationState = this.mOemServiceRegDurState;
        if (oemServiceRegDurationState == null) {
            return false;
        }
        return oemServiceRegDurationState.isRegionLockedState(mNewSS, id, hasLocationChanged);
    }

    public int oppoUpdateNrState(int newNrState, boolean hasNrSecondaryServingCell, ServiceState ss) {
        return this.mOppoNrStateUpdater.getNrState(newNrState, hasNrSecondaryServingCell, ss);
    }
}
