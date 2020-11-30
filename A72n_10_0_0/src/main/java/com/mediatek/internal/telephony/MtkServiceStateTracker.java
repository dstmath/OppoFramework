package com.mediatek.internal.telephony;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hardware.radio.V1_2.VoiceRegStateResult;
import android.hardware.radio.V1_4.DataRegStateResult;
import android.hardware.radio.V1_4.NrIndicators;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.DataSpecificRegistrationInfo;
import android.telephony.LteVopsSupportInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.OppoTelephonyFunction;
import android.telephony.PhysicalChannelConfig;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoiceSpecificRegistrationInfo;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.SparseBooleanArray;
import android.util.StatsLog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HbpcdUtils;
import com.android.internal.telephony.IOppoNetworkManager;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.NetworkRegistrationManager;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OemFeature;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RatRatcheter;
import com.android.internal.telephony.RestrictedState;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdnr.CarrierDisplayNameData;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils;
import com.mediatek.internal.telephony.cdma.pluscode.PlusCodeProcessor;
import com.mediatek.internal.telephony.uicc.MtkSpnOverride;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import mediatek.telephony.MtkServiceState;
import mediatek.telephony.MtkSignalStrength;

public class MtkServiceStateTracker extends ServiceStateTracker {
    private static final boolean DBG = true;
    protected static final int EVENT_CHECK_POLL_SERVICE = 123;
    protected static final int EVENT_CS_NETWORK_STATE_CHANGED = 100;
    protected static final int EVENT_FEMTO_CELL_INFO = 102;
    protected static final int EVENT_ICC_REFRESH = 106;
    protected static final int EVENT_IMEI_LOCK = 107;
    protected static final int EVENT_INVALID_SIM_INFO = 101;
    protected static final int EVENT_MODULATION_INFO = 105;
    private static final int EVENT_MTK_GET_CELL_INFO_LIST = 1;
    protected static final int EVENT_NETWORK_EVENT = 104;
    protected static final int EVENT_PS_NETWORK_STATE_CHANGED = 103;
    protected static final int EVENT_RESTART_TURBO = 122;
    protected static final int EVENT_RIL_READY = 120;
    protected static final int EVENT_SIM_OPL_LOADED = 119;
    protected static final int EVENT_UPDATE_PLMN = 121;
    private static final String LOG_TAG = "MTKSST";
    private static final long MTK_LAST_CELL_INFO_LIST_MAX_AGE_MS = 1000;
    protected static final String PROP_IWLAN_STATE = "persist.vendor.radio.wfc_state";
    protected static final String PROP_MTK_DATA_TYPE = "persist.vendor.radio.mtk_data_type";
    public static final int REJECT_NOTIFICATION = 890;
    private static final boolean USE_SIM_SPN_ONLY = true;
    private static final boolean VDBG = true;
    private MtkPlmn curr_mtkPlmn = null;
    public boolean hasPendingPollState = false;
    private boolean isCsInvalidCard = false;
    private String mCsgId = null;
    private RegistrantList mDataRoamingTypeChangedRegistrants = new RegistrantList();
    private boolean mEnableERI = false;
    private EndcBearController mEndcBearControl;
    private boolean mEriTriggeredPollState = false;
    private boolean mEverIVSR = false;
    private int mFemtocellDomain = 0;
    private boolean mForceBroadcastServiceState = false;
    private String mHhbName = null;
    private int mIsFemtocell = 0;
    private boolean mIsImeiLock = false;
    private Object mLastCellInfoListLock = new Object();
    private String mLastPSRegisteredPLMN = null;
    private int mLastPhoneGetNitz = -1;
    private String mLastRegisteredPLMN = null;
    private String mLocatedPlmn = null;
    private BroadcastReceiver mMtkIntentReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkServiceStateTracker.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                if (intent.getExtras().getInt("android.telephony.extra.SLOT_INDEX") == MtkServiceStateTracker.this.mPhone.getPhoneId()) {
                    MtkServiceStateTracker.this.sendEmptyMessage(57);
                }
            } else if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
                if (MtkServiceStateTracker.this.mCellIdentity != null) {
                    MtkServiceStateTracker mtkServiceStateTracker = MtkServiceStateTracker.this;
                    mtkServiceStateTracker.refreshSpn(mtkServiceStateTracker.mSS, MtkServiceStateTracker.this.mCellIdentity.asCellLocation(), false);
                } else {
                    MtkServiceStateTracker mtkServiceStateTracker2 = MtkServiceStateTracker.this;
                    mtkServiceStateTracker2.refreshSpn(mtkServiceStateTracker2.mSS, null, false);
                }
                MtkServiceStateTracker.this.updateSpnDisplay();
                if (MtkServiceStateTracker.this.mForceBroadcastServiceState) {
                    MtkServiceStateTracker.this.pollState();
                }
            } else if (intent.getAction().equals("android.intent.action.ACTION_RADIO_OFF")) {
                MtkServiceStateTracker.this.mAlarmSwitch = false;
                MtkServiceStateTracker.this.powerOffRadioSafely();
            } else if (intent.getAction().equals("android.telephony.action.SIM_CARD_STATE_CHANGED")) {
                int simState = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
                int slotId = intent.getIntExtra("phone", -1);
                if (slotId == MtkServiceStateTracker.this.mPhone.getPhoneId()) {
                    MtkServiceStateTracker mtkServiceStateTracker3 = MtkServiceStateTracker.this;
                    mtkServiceStateTracker3.log("SIM state change, slotId: " + slotId + " simState[" + simState + "]");
                    if (!MtkServiceStateTracker.this.mPhone.isPhoneTypeGsm()) {
                        if (1 == simState) {
                            MtkServiceStateTracker.this.mMdn = null;
                        }
                    } else if (simState == 1) {
                        MtkServiceStateTracker.this.mLastRegisteredPLMN = null;
                        MtkServiceStateTracker.this.mLastPSRegisteredPLMN = null;
                    }
                }
            } else if (intent.getAction().equals("android.telephony.action.SIM_APPLICATION_STATE_CHANGED")) {
                int slotId2 = intent.getIntExtra("phone", -1);
                int simState2 = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
                MtkServiceStateTracker mtkServiceStateTracker4 = MtkServiceStateTracker.this;
                mtkServiceStateTracker4.log("ACTION_SIM_APPLICATION_STATE_CHANGED, slotId: " + slotId2 + " simState[" + simState2 + "]");
                if (slotId2 == MtkServiceStateTracker.this.mPhone.getPhoneId() && simState2 == 10) {
                    MtkServiceStateTracker mtkServiceStateTracker5 = MtkServiceStateTracker.this;
                    mtkServiceStateTracker5.setDeviceRatMode(mtkServiceStateTracker5.mPhone.getPhoneId());
                }
            } else if (intent.getAction().equals("com.mediatek.common.carrierexpress.operator_config_changed")) {
                try {
                    MtkServiceStateTracker.this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(MtkServiceStateTracker.this.mPhone.getContext());
                    MtkServiceStateTracker.this.mServiceStateTrackerExt = MtkServiceStateTracker.this.mTelephonyCustomizationFactory.makeServiceStateTrackerExt(MtkServiceStateTracker.this.mPhone.getContext());
                    MtkServiceStateTracker.this.log("mServiceStateTrackerExt reload success");
                } catch (Exception e) {
                    MtkServiceStateTracker.this.log("mServiceStateTrackerExt init fail");
                    e.printStackTrace();
                }
            }
        }
    };
    private RegistrantList mMtkNrStateChangedRegistrants = new RegistrantList();
    private boolean mMtkSpnUpdatePending = false;
    private boolean mMtkVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17891571);
    private boolean mNetworkExsit = false;
    private CellIdentity mNewPSCellIdentity;
    private Notification mNotification;
    private Notification.Builder mNotificationBuilder;
    private boolean mOppoNeedNotify = false;
    private IPlusCodeUtils mPlusCodeUtils = PlusCodeProcessor.getPlusCodeUtils();
    private int mPsRegState = 1;
    private int mPsRegStateRaw = 0;
    private String mSavedGuessTimeZone = null;
    private IServiceStateTrackerExt mServiceStateTrackerExt = null;
    private OpTelephonyCustomizationFactoryBase mTelephonyCustomizationFactory = null;
    private String[][] mTimeZoneIdOfCapitalCity = {new String[]{"au", "Australia/Sydney"}, new String[]{"br", "America/Sao_Paulo"}, new String[]{"ca", "America/Toronto"}, new String[]{"cl", "America/Santiago"}, new String[]{"es", "Europe/Madrid"}, new String[]{"fm", "Pacific/Ponape"}, new String[]{"gl", "America/Godthab"}, new String[]{"kz", "Asia/Almaty"}, new String[]{"mn", "Asia/Ulaanbaatar"}, new String[]{"mx", "America/Mexico_City"}, new String[]{"pf", "Pacific/Tahiti"}, new String[]{"pt", "Europe/Lisbon"}, new String[]{"us", "America/New_York"}, new String[]{"ec", "America/Guayaquil"}, new String[]{"cn", "Asia/Shanghai"}};
    private Handler mtkHandler;
    private HandlerThread mtkHandlerThread = new HandlerThread("MtkHandlerThread");
    private final MtkSubscriptionsChangedListener mtkOnSubscriptionsChangedListener = new MtkSubscriptionsChangedListener();
    private int oosFlag = -1;
    private MtkServiceState turboSS = new MtkServiceState();
    private SignalStrength turboSig = null;

    private class MtkSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        private MtkSubscriptionsChangedListener() {
        }

        public void onSubscriptionsChanged() {
            MtkServiceStateTracker.this.log("MtkSubscriptionListener.onSubscriptionInfoChanged");
            if (SubscriptionManager.isValidSubscriptionId(MtkServiceStateTracker.this.mPhone.getSubId()) && MtkServiceStateTracker.this.mMtkSpnUpdatePending && MtkServiceStateTracker.this.curr_mtkPlmn != null) {
                Handler handler = MtkServiceStateTracker.this.mtkHandler;
                MtkServiceStateTracker mtkServiceStateTracker = MtkServiceStateTracker.this;
                handler.sendMessage(mtkServiceStateTracker.obtainMessage(MtkServiceStateTracker.EVENT_UPDATE_PLMN, mtkServiceStateTracker.curr_mtkPlmn));
                MtkServiceStateTracker.this.mMtkSpnUpdatePending = false;
            }
        }
    }

    private class MtkCellInfoResult {
        List<CellInfo> list;
        Object lockObj = new Object();

        private MtkCellInfoResult() {
        }
    }

    /* access modifiers changed from: private */
    public class MtkPlmn {
        String plmn;
        boolean showPlmn;
        boolean showSpn;
        String spn;

        public MtkPlmn(boolean showPlmn2, String plmn2, boolean showSpn2, String spn2) {
            this.showPlmn = showPlmn2;
            String str = null;
            this.plmn = plmn2 == null ? null : new String(plmn2);
            this.showSpn = showSpn2;
            this.spn = spn2 != null ? new String(spn2) : str;
        }

        public String toString() {
            return "MtkPlmn showPlmn=" + this.showPlmn + " plmn=" + this.plmn + " showSpn=" + this.showSpn + " spn=" + this.spn;
        }
    }

    private void updateTurboPLMN() {
        updatePLMN(false, null, false, null);
    }

    private void updatePLMN(boolean showPlmn, String plmn, boolean showSpn, String spn) {
        this.mtkHandler.sendMessage(obtainMessage(EVENT_UPDATE_PLMN, new MtkPlmn(showPlmn, plmn, showSpn, spn)));
    }

    private class MtkHandler extends Handler {
        private boolean pending = false;
        private boolean polling = false;
        private boolean stop = false;

        public MtkHandler(Looper looper) {
            super(looper);
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 2) {
                int[] mtkLteRssnrThreshold = null;
                if (i == 3) {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    boolean mtkRsrpOnly = (MtkServiceStateTracker.this.mServiceStateTrackerExt != null ? Boolean.valueOf(MtkServiceStateTracker.this.mServiceStateTrackerExt.getMtkRsrpOnly()) : null).booleanValue();
                    int[] mtkLteRsrpThreshold = MtkServiceStateTracker.this.mServiceStateTrackerExt != null ? MtkServiceStateTracker.this.mServiceStateTrackerExt.getMtkLteRsrpThreshold() : null;
                    if (MtkServiceStateTracker.this.mServiceStateTrackerExt != null) {
                        mtkLteRssnrThreshold = MtkServiceStateTracker.this.mServiceStateTrackerExt.getMtkLteRssnrThreshold();
                    }
                    if (ar.exception != null || ar.result == null) {
                        MtkServiceStateTracker mtkServiceStateTracker = MtkServiceStateTracker.this;
                        mtkServiceStateTracker.log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
                        return;
                    }
                    MtkServiceStateTracker mtkServiceStateTracker2 = MtkServiceStateTracker.this;
                    mtkServiceStateTracker2.turboSig = new MtkSignalStrength(mtkServiceStateTracker2.mPhone.getPhoneId(), (SignalStrength) ar.result);
                    PersistableBundle config = MtkServiceStateTracker.this.getCarrierConfig();
                    MtkServiceStateTracker.this.turboSig.updateLevel(config, MtkServiceStateTracker.this.mSS);
                    MtkSignalStrength mtkSignal = MtkServiceStateTracker.this.turboSig;
                    mtkSignal.setMtkRsrpOnly(mtkRsrpOnly);
                    mtkSignal.setMtkLteRsrpThreshold(mtkLteRsrpThreshold);
                    mtkSignal.setMtkLteRssnrThreshold(mtkLteRssnrThreshold);
                    mtkSignal.updateMtkLevel(config, MtkServiceStateTracker.this.mSS);
                    MtkServiceStateTracker mtkServiceStateTracker3 = MtkServiceStateTracker.this;
                    mtkServiceStateTracker3.log("notifyMtkSignalStrength level:" + MtkServiceStateTracker.this.turboSig.getLevel() + " raw:" + MtkServiceStateTracker.this.turboSig);
                    MtkServiceStateTracker.this.mPhone.notifyMtkSignalStrength(MtkServiceStateTracker.this.turboSig);
                    return;
                } else if (i == 4) {
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception != null || ar2.result == null || !(ar2.result instanceof VoiceRegStateResult)) {
                        MtkServiceStateTracker.this.log("Turbo stop due to wrong object");
                        return;
                    }
                    VoiceRegStateResult voiceRegState = (VoiceRegStateResult) ar2.result;
                    int networkType = ServiceState.rilRadioTechnologyToNetworkType(voiceRegState.rat);
                    if (networkType == 19) {
                        networkType = 13;
                    }
                    NetworkRegistrationInfo networkRegState = new NetworkRegistrationInfo(1, 1, voiceRegState.regState, networkType, voiceRegState.reasonForDenial, false, null, null, voiceRegState.cssSupported, voiceRegState.roamingIndicator, voiceRegState.systemIsInPrl, voiceRegState.defaultRoamingIndicator);
                    MtkServiceStateTracker.this.turboSS.setVoiceRegState(MtkServiceStateTracker.this.regCodeToServiceState(networkRegState.getRegistrationState()));
                    MtkServiceStateTracker.this.turboSS.addNetworkRegistrationInfo(networkRegState);
                    if ((MtkServiceStateTracker.this.turboSS.getVoiceRegState() == 0 && !MtkServiceStateTracker.this.turboSS.getVoiceRoaming()) || (MtkServiceStateTracker.this.turboSS.getDataRegState() == 0 && !MtkServiceStateTracker.this.turboSS.getDataRoaming())) {
                        MtkServiceStateTracker mtkServiceStateTracker4 = MtkServiceStateTracker.this;
                        mtkServiceStateTracker4.log("MtkHandler turboSS=" + MtkServiceStateTracker.this.turboSS);
                        MtkServiceStateTracker.this.mPhone.notifyMtkServiceStateChanged(MtkServiceStateTracker.this.turboSS);
                        if (MtkServiceStateTracker.this.turboSS.getDataRegState() == 0) {
                            this.pending = false;
                        }
                    }
                    this.polling = false;
                    if (this.pending) {
                        this.pending = false;
                        MtkServiceStateTracker.this.mtkHandler.sendEmptyMessage(MtkServiceStateTracker.EVENT_RESTART_TURBO);
                        return;
                    }
                    return;
                } else if (i == 5) {
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    if (ar3.exception != null || ar3.result == null || !(ar3.result instanceof DataRegStateResult)) {
                        MtkServiceStateTracker.this.log("Turbo stop due to wrong object");
                        return;
                    }
                    DataRegStateResult dataRegState = (DataRegStateResult) ar3.result;
                    boolean isUsingCarrierAggregation = false;
                    int networkType2 = ServiceState.rilRadioTechnologyToNetworkType(dataRegState.base.rat);
                    if (networkType2 == 19) {
                        isUsingCarrierAggregation = true;
                        networkType2 = 13;
                    }
                    NrIndicators nrIndicators = dataRegState.nrIndicators;
                    NetworkRegistrationInfo networkRegState2 = new NetworkRegistrationInfo(2, 1, dataRegState.base.regState, networkType2, dataRegState.base.reasonDataDenied, false, null, null, dataRegState.base.maxDataCalls, nrIndicators.isDcNrRestricted, nrIndicators.isNrAvailable, nrIndicators.isEndcAvailable, new LteVopsSupportInfo(1, 1), isUsingCarrierAggregation);
                    MtkServiceStateTracker.this.turboSS.setDataRegState(MtkServiceStateTracker.this.regCodeToServiceState(networkRegState2.getRegistrationState()));
                    MtkServiceStateTracker.this.turboSS.addNetworkRegistrationInfo(networkRegState2);
                    return;
                } else if (i != 7) {
                    switch (i) {
                        case MtkServiceStateTracker.EVENT_RIL_READY /* 120 */:
                            MtkServiceStateTracker.this.mCi.registerForNetworkStateChanged(MtkServiceStateTracker.this.mtkHandler, 2, (Object) null);
                            break;
                        case MtkServiceStateTracker.EVENT_UPDATE_PLMN /* 121 */:
                            int sid = MtkServiceStateTracker.this.mPhone.getSubId();
                            if (((NetworkRegistrationManager) MtkServiceStateTracker.this.mRegStateManagers.get(1)).isServiceConnected()) {
                                MtkPlmn mtkplmn = (MtkPlmn) msg.obj;
                                if (mtkplmn == null || (!mtkplmn.showPlmn && !mtkplmn.showSpn)) {
                                    MtkServiceStateTracker mtkServiceStateTracker5 = MtkServiceStateTracker.this;
                                    mtkServiceStateTracker5.log("should not be happened here " + mtkplmn);
                                    return;
                                }
                                MtkServiceStateTracker mtkServiceStateTracker6 = MtkServiceStateTracker.this;
                                mtkServiceStateTracker6.log("MtkHandler: EVENT_UPDATE_PLMN " + mtkplmn);
                                if (!MtkServiceStateTracker.this.mSubscriptionController.setPlmnSpn(MtkServiceStateTracker.this.mPhone.getPhoneId(), mtkplmn.showPlmn, mtkplmn.plmn, mtkplmn.showSpn, mtkplmn.spn)) {
                                    MtkServiceStateTracker.this.mMtkSpnUpdatePending = true;
                                    MtkServiceStateTracker.this.curr_mtkPlmn = mtkplmn;
                                    return;
                                }
                                return;
                            } else if (this.stop || !SubscriptionManager.isValidSubscriptionId(sid)) {
                                return;
                            } else {
                                if ((MtkServiceStateTracker.this.turboSS.getVoiceRegState() == 0 && !MtkServiceStateTracker.this.turboSS.getVoiceRoaming()) || (MtkServiceStateTracker.this.turboSS.getDataRegState() == 0 && !MtkServiceStateTracker.this.turboSS.getDataRoaming())) {
                                    MtkServiceStateTracker mtkServiceStateTracker7 = MtkServiceStateTracker.this;
                                    mtkServiceStateTracker7.log("updateTurboPLMN SubId=" + sid + " turboSS=" + MtkServiceStateTracker.this.turboSS);
                                    MtkServiceStateTracker.this.mSubscriptionController.setPlmnSpn(MtkServiceStateTracker.this.mPhone.getPhoneId(), true, MtkServiceStateTracker.this.turboSS.getOperatorAlpha(), false, "");
                                    MtkServiceStateTracker.this.mPhone.notifyMtkServiceStateChanged(MtkServiceStateTracker.this.turboSS);
                                    MtkServiceStateTracker.this.log("MtkHandler: stop MTK turbo");
                                    this.stop = true;
                                    MtkServiceStateTracker.this.mCi.unregisterForNetworkStateChanged(MtkServiceStateTracker.this.mtkHandler);
                                    MtkServiceStateTracker.this.turboSS = new MtkServiceState();
                                    MtkServiceStateTracker.this.log("MtkHandler: sending EVENT_CHECK_POLL_SERVICE");
                                    MtkServiceStateTracker.this.mtkHandler.sendEmptyMessageDelayed(MtkServiceStateTracker.EVENT_CHECK_POLL_SERVICE, MtkServiceStateTracker.MTK_LAST_CELL_INFO_LIST_MAX_AGE_MS);
                                    return;
                                }
                                return;
                            }
                        case MtkServiceStateTracker.EVENT_RESTART_TURBO /* 122 */:
                            break;
                        case MtkServiceStateTracker.EVENT_CHECK_POLL_SERVICE /* 123 */:
                            boolean connected = ((NetworkRegistrationManager) MtkServiceStateTracker.this.mRegStateManagers.get(1)).isServiceConnected();
                            MtkServiceStateTracker mtkServiceStateTracker8 = MtkServiceStateTracker.this;
                            mtkServiceStateTracker8.log("MtkHandler: EVENT_CHECK_POLL_SERVICE connected=" + connected);
                            if (!connected) {
                                MtkServiceStateTracker.this.mtkHandler.sendEmptyMessageDelayed(MtkServiceStateTracker.EVENT_CHECK_POLL_SERVICE, MtkServiceStateTracker.MTK_LAST_CELL_INFO_LIST_MAX_AGE_MS);
                                return;
                            } else {
                                MtkServiceStateTracker.this.sendEmptyMessage(2);
                                return;
                            }
                        default:
                            MtkServiceStateTracker mtkServiceStateTracker9 = MtkServiceStateTracker.this;
                            mtkServiceStateTracker9.loge("Should not be here msg.what=" + msg.what);
                            return;
                    }
                } else {
                    String[] opNames = (String[]) ((AsyncResult) msg.obj).result;
                    if (opNames != null && opNames.length >= 3) {
                        MtkServiceStateTracker.this.turboSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                        return;
                    }
                    return;
                }
            }
            if (((NetworkRegistrationManager) ((MtkServiceStateTracker) MtkServiceStateTracker.this).mRegStateManagers.get(1)).isServiceConnected() || this.stop) {
                MtkServiceStateTracker.this.log("MtkHandler: stop MTK turbo");
                MtkServiceStateTracker.this.mCi.unregisterForNetworkStateChanged(MtkServiceStateTracker.this.mtkHandler);
                return;
            }
            if (msg.what == MtkServiceStateTracker.EVENT_RIL_READY) {
                MtkServiceStateTracker.this.log("MtkHandler: EVENT_RIL_READY");
            }
            if (msg.what == MtkServiceStateTracker.EVENT_RESTART_TURBO) {
                MtkServiceStateTracker.this.log("MtkHandler: EVENT_RESTART_TURBO");
            } else {
                MtkServiceStateTracker.this.log("MtkHandler: EVENT_NETWORK_STATE_CHANGED");
                if (this.polling) {
                    this.pending = true;
                    return;
                }
            }
            this.polling = true;
            MtkServiceStateTracker.this.mCi.getOperator(MtkServiceStateTracker.this.mtkHandler.obtainMessage(7, MtkServiceStateTracker.this.mPollingContext));
            MtkServiceStateTracker.this.mCi.getDataRegistrationState(MtkServiceStateTracker.this.mtkHandler.obtainMessage(5, MtkServiceStateTracker.this.mPollingContext));
            MtkServiceStateTracker.this.mCi.getVoiceRegistrationState(MtkServiceStateTracker.this.mtkHandler.obtainMessage(4, MtkServiceStateTracker.this.mPollingContext));
            MtkServiceStateTracker.this.mCi.getSignalStrength(MtkServiceStateTracker.this.mtkHandler.obtainMessage(3));
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: com.mediatek.internal.telephony.MtkServiceStateTracker */
    /* JADX WARN: Multi-variable type inference failed */
    public MtkServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        super(phone, ci);
        this.mtkHandlerThread.start();
        this.mtkHandler = new MtkHandler(this.mtkHandlerThread.getLooper());
        Context context = this.mPhone.getContext();
        context.unregisterReceiver(this.mIntentReceiver);
        if (this.mRegStateManagers.get(2) != null) {
            ((NetworkRegistrationManager) this.mRegStateManagers.get(2)).setIwlanNetworkServiceClassName("com.mediatek.internal.telephony.IWlanNetworkService");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        context.registerReceiver(this.mMtkIntentReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.ACTION_RADIO_OFF");
        context.registerReceiver(this.mMtkIntentReceiver, filter2);
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        context.registerReceiver(this.mMtkIntentReceiver, filter3);
        IntentFilter filter4 = new IntentFilter();
        filter4.addAction("android.telephony.action.SIM_APPLICATION_STATE_CHANGED");
        context.registerReceiver(this.mMtkIntentReceiver, filter4);
        IntentFilter filter5 = new IntentFilter();
        filter5.addAction("android.telephony.action.SIM_CARD_STATE_CHANGED");
        context.registerReceiver(this.mMtkIntentReceiver, filter5);
        IntentFilter filter6 = new IntentFilter();
        filter6.addAction("com.mediatek.common.carrierexpress.operator_config_changed");
        context.registerReceiver(this.mMtkIntentReceiver, filter6);
        try {
            this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(this.mPhone.getContext());
            this.mServiceStateTrackerExt = this.mTelephonyCustomizationFactory.makeServiceStateTrackerExt(this.mPhone.getContext());
        } catch (Exception e) {
            log("mServiceStateTrackerExt init fail");
            e.printStackTrace();
        }
        ((MtkRIL) this.mCi).registerForCsNetworkStateChanged(this, 100, null);
        this.mCi.registerForPsNetworkStateChanged(this, EVENT_PS_NETWORK_STATE_CHANGED, null);
        this.mCi.registerForRilConnected(this.mtkHandler, (int) EVENT_RIL_READY, (Object) null);
        SubscriptionManager.from(phone.getContext()).addOnSubscriptionsChangedListener(this.mtkOnSubscriptionsChangedListener);
        this.mEndcBearControl = EndcBearController.makeEndcBearController(this.mPhone.getContext());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: com.mediatek.internal.telephony.MtkServiceStateTracker */
    /* JADX WARN: Multi-variable type inference failed */
    public void updatePhoneType() {
        NetworkRegistrationInfo nrs;
        if (this.mSS != null && this.mSS.getVoiceRoaming()) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (this.mSS != null && this.mSS.getDataRoaming()) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (this.mSS != null && this.mSS.getVoiceRegState() == 0) {
            this.mNetworkDetachedRegistrants.notifyRegistrants();
        }
        int[] availableTransports = this.mTransportManager.getAvailableTransports();
        for (int transport : availableTransports) {
            if (!(this.mSS == null || (nrs = this.mSS.getNetworkRegistrationInfo(2, transport)) == null || !nrs.isInService() || this.mDetachedRegistrants.get(transport) == null)) {
                ((RegistrantList) this.mDetachedRegistrants.get(transport)).notifyRegistrants();
            }
        }
        this.mSS = new MtkServiceState();
        this.mSS.setStateOutOfService();
        this.mNewSS = new MtkServiceState();
        this.mLastCellInfoReqTime = 0;
        this.mLastCellInfoList = null;
        this.mSignalStrength = new MtkSignalStrength(this.mPhone.getPhoneId());
        this.mRestrictedState = new RestrictedState();
        this.mStartedGprsRegCheck = false;
        this.mReportedGprsNoReg = false;
        this.mMdn = null;
        this.mMin = null;
        this.mPrlVersion = null;
        this.mIsMinInfoReady = false;
        this.mNitzState.handleNetworkCountryCodeUnavailable();
        this.mCellIdentity = null;
        this.mNewCellIdentity = null;
        this.mNewPSCellIdentity = null;
        cancelPollState();
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mCdmaSSM != null) {
                this.mCdmaSSM.dispose(this);
            }
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
            this.mCi.setInvalidSimInfo(this, 101, null);
            this.mCi.registerForIccRefresh(this, EVENT_ICC_REFRESH, null);
            this.mCi.registerForNetworkEvent(this, 104, null);
            this.mCi.registerForModulation(this, 105, null);
            if (SystemProperties.get("ro.vendor.mtk_femto_cell_support").equals("1")) {
                this.mCi.registerForFemtoCellInfo(this, 102, null);
            }
            try {
                if (this.mServiceStateTrackerExt != null && this.mServiceStateTrackerExt.isImeiLocked()) {
                    this.mCi.registerForIMEILock(this, EVENT_IMEI_LOCK, null);
                }
            } catch (RuntimeException e) {
                loge("No isImeiLocked");
            }
        } else {
            this.mCi.unregisterForAvailable(this);
            this.mCi.unSetOnRestrictedStateChanged(this);
            this.mPsRestrictDisabledRegistrants.notifyRegistrants();
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
            this.mCi.unregisterForIccRefresh(this);
            this.mCi.unSetInvalidSimInfo(this);
            this.mCi.unregisterForNetworkEvent(this);
            this.mCi.unregisterForModulation(this);
            try {
                if (this.mServiceStateTrackerExt != null && this.mServiceStateTrackerExt.isImeiLocked()) {
                    this.mCi.unregisterForIMEILock(this);
                }
            } catch (RuntimeException e2) {
                loge("No isImeiLocked");
            }
            if (this.mPhone.isPhoneTypeCdmaLte()) {
                this.mPhone.registerForSimRecordsLoaded(this, 16, (Object) null);
            }
            this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(this.mPhone.getContext(), this.mCi, this, 39, (Object) null);
            this.mIsSubscriptionFromRuim = this.mCdmaSSM.getCdmaSubscriptionSource() == 0;
            this.mCi.registerForCdmaPrlChanged(this, 40, (Object) null);
            this.mCi.registerForCdmaOtaProvision(this, 37, (Object) null);
            this.mHbpcdUtils = new HbpcdUtils(this.mPhone.getContext());
            updateOtaspState();
        }
        onUpdateIccAvailability();
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(0));
        this.mCi.getSignalStrength(obtainMessage(3));
        sendMessage(obtainMessage(50));
        logPhoneTypeChange();
        notifyVoiceRegStateRilRadioTechnologyChanged();
        for (int transport2 : this.mTransportManager.getAvailableTransports()) {
            notifyDataRegStateRilRadioTechnologyChanged(transport2);
        }
    }

    public void registerForDataRoamingTypeChange(Handler h, int what, Object obj) {
        this.mDataRoamingTypeChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDataRoamingTypeChange(Handler h) {
        this.mDataRoamingTypeChangedRegistrants.remove(h);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.mediatek.internal.telephony.MtkServiceStateTracker */
    /* JADX WARN: Multi-variable type inference failed */
    public void dispose() {
        MtkServiceStateTracker.super.dispose();
        this.mCi.unregisterForCsNetworkStateChanged(this);
        this.mCi.unregisterForPsNetworkStateChanged(this);
        this.mtkHandlerThread.quit();
        if (this.mPhone.isPhoneTypeGsm()) {
            this.mCi.unregisterForIccRefresh(this);
            this.mCi.unSetInvalidSimInfo(this);
            this.mCi.unregisterForNetworkEvent(this);
            this.mCi.unregisterForModulation(this);
            try {
                if (this.mServiceStateTrackerExt.isImeiLocked()) {
                    this.mCi.unregisterForIMEILock(this);
                }
            } catch (RuntimeException e) {
                loge("No isImeiLocked");
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r10v0, resolved type: com.mediatek.internal.telephony.MtkServiceStateTracker */
    /* JADX WARN: Multi-variable type inference failed */
    public void handleMessage(Message msg) {
        int[] subIds;
        logv("received event " + msg.what);
        int i = msg.what;
        boolean z = true;
        if (i != 1) {
            if (i == 11) {
                String operatorNumeric = this.mSS.getOperatorNumeric();
                if (!TextUtils.isEmpty(operatorNumeric) && operatorNumeric.startsWith("001")) {
                    log("EVENT_NITZ_TIME we have operatorNumeric " + this.mSS.getOperatorNumeric());
                    this.mNitzState.handleNetworkCountryCodeSet(true);
                }
                this.mLastPhoneGetNitz = this.mPhone.getPhoneId();
                AsyncResult ar = (AsyncResult) msg.obj;
                final String nitzString = (String) ((Object[]) ar.result)[0];
                final long nitzReceiveTime = ((Long) ((Object[]) ar.result)[1]).longValue();
                new Thread(new Runnable() {
                    /* class com.mediatek.internal.telephony.MtkServiceStateTracker.AnonymousClass2 */

                    public void run() {
                        MtkServiceStateTracker.this.setTimeFromNITZString(nitzString, nitzReceiveTime);
                    }
                }).start();
                return;
            } else if (i == 16) {
                if (this.mPhone.isPhoneTypeGsm()) {
                    if (this.mCellIdentity != null) {
                        refreshSpn(this.mSS, this.mCellIdentity.asCellLocation(), false);
                    } else {
                        refreshSpn(this.mSS, null, false);
                    }
                }
                MtkServiceStateTracker.super.handleMessage(msg);
                if (this.mForceBroadcastServiceState) {
                    pollState();
                    return;
                }
                return;
            } else if (i == 26) {
                if (this.mPhone.isPhoneTypeCdmaLte()) {
                    log("Receive EVENT_RUIM_READY");
                    pollState();
                } else {
                    log("Receive EVENT_RUIM_READY and Send Request getCDMASubscription.");
                    getSubscriptionInfoAndStartPollingThreads();
                }
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                return;
            } else if (i == 53) {
                log("EVENT_IMS_SERVICE_STATE_CHANGED");
                if (this.mSS.getState() != 0 && this.mSS.getDataRegState() == 0) {
                    this.mPhone.notifyServiceStateChanged(this.mPhone.getServiceState());
                    return;
                }
                return;
            } else if (i == 55) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                boolean connected = ((NetworkRegistrationManager) this.mRegStateManagers.get(1)).isServiceConnected();
                boolean isMdInService = this.mSS.getCellularRegState() == 0;
                if (!connected || !isMdInService) {
                    if (!connected) {
                        log("Skip PHYSICAL_CHANNEL_CONFIG because service is not ready");
                    } else {
                        log("Skip PHYSICAL_CHANNEL_CONFIG because md is not in service");
                    }
                    if (ar2.exception == null) {
                        this.mLastPhysicalChannelConfigList = (List) ar2.result;
                        return;
                    }
                    return;
                }
                if (ar2.exception == null) {
                    List<PhysicalChannelConfig> list = (List) ar2.result;
                    log("EVENT_PHYSICAL_CHANNEL_CONFIG: size=" + list.size() + " list=" + list);
                    this.mPhone.notifyPhysicalChannelConfiguration(list);
                    this.mLastPhysicalChannelConfigList = list;
                    boolean hasChanged = updateNrFrequencyRangeFromPhysicalChannelConfigs(list, this.mSS);
                    boolean nrStateChange = updateNrStateFromPhysicalChannelConfigs(list, this.mSS);
                    if (nrStateChange) {
                        log("nrStateChange = true, notify mMtkNrStateChangedRegistrants");
                        this.mMtkNrStateChangedRegistrants.notifyRegistrants();
                    }
                    if ((hasChanged || nrStateChange) || RatRatcheter.updateBandwidths(getMtkBandwidthsFromConfigs(list), this.mSS)) {
                        this.mPhone.notifyServiceStateChanged(this.mSS);
                    }
                }
                EndcBearController endcBearController = this.mEndcBearControl;
                int phoneId = this.mPhone.getPhoneId();
                if (3 != this.mSS.getNrState()) {
                    z = false;
                }
                endcBearController.updateAnyNrBearerAllocationStatus(phoneId, z);
                return;
            } else if (i == EVENT_SIM_OPL_LOADED) {
                AsyncResult ar3 = (AsyncResult) msg.obj;
                if (ar3 == null || ar3.result == null) {
                    loge("EVENT_SIM_OPL_LOADED obj is null");
                    return;
                } else if (((Integer) ar3.result).intValue() != 101) {
                    return;
                } else {
                    if (this.mPhone.isPhoneTypeGsm()) {
                        log("EVENT_SIM_OPL_LOADED: EVENT_OPL");
                        if (this.mCellIdentity != null) {
                            refreshSpn(this.mSS, this.mCellIdentity.asCellLocation(), false);
                        } else {
                            refreshSpn(this.mSS, null, false);
                        }
                        if (this.mForceBroadcastServiceState) {
                            pollState();
                            return;
                        }
                        return;
                    }
                    loge("EVENT_SIM_OPL_LOADED should not be here");
                    return;
                }
            } else if (i == 43 || i == 44) {
                if (msg.obj != null) {
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    if (ar4.exception != null) {
                        this.mLastCellInfoList = null;
                    } else if (ar4.result == null) {
                        this.mLastCellInfoList = null;
                    }
                }
                MtkServiceStateTracker.super.handleMessage(msg);
                return;
            } else if (i == 49) {
                if (SubscriptionManager.getDefaultDataSubscriptionId() == -1 && (subIds = SubscriptionManager.getSubId(RadioCapabilitySwitchUtil.getMainCapabilityPhoneId())) != null && subIds.length > 0) {
                    ProxyController.getInstance().unregisterForAllDataDisconnected(subIds[0], this);
                }
                MtkServiceStateTracker.super.handleMessage(msg);
                return;
            } else if (i != 50) {
                switch (i) {
                    case 100:
                        onNetworkStateChangeResult((AsyncResult) msg.obj);
                        return;
                    case 101:
                        if (this.mPhone.isPhoneTypeGsm()) {
                            onInvalidSimInfoReceived((AsyncResult) msg.obj);
                            return;
                        }
                        return;
                    case 102:
                        onFemtoCellInfoResult((AsyncResult) msg.obj);
                        return;
                    case EVENT_PS_NETWORK_STATE_CHANGED /* 103 */:
                        onPsNetworkStateChangeResult((AsyncResult) msg.obj);
                        return;
                    case 104:
                        if (this.mPhone.isPhoneTypeGsm()) {
                            onNetworkEventReceived((AsyncResult) msg.obj);
                            return;
                        }
                        return;
                    case 105:
                        if (this.mPhone.isPhoneTypeGsm()) {
                            onModulationInfoReceived((AsyncResult) msg.obj);
                            return;
                        }
                        return;
                    case EVENT_ICC_REFRESH /* 106 */:
                        if (this.mPhone.isPhoneTypeGsm()) {
                            AsyncResult ar5 = (AsyncResult) msg.obj;
                            if (ar5.exception == null) {
                                IccRefreshResponse res = (IccRefreshResponse) ar5.result;
                                if (res == null) {
                                    log("IccRefreshResponse is null");
                                    return;
                                }
                                int i2 = res.refreshResult;
                                if (i2 != 0) {
                                    if (i2 != 4) {
                                        if (i2 != 5) {
                                            if (i2 != 6) {
                                                log("GSST EVENT_ICC_REFRESH IccRefreshResponse =" + res);
                                                return;
                                            }
                                        }
                                    }
                                    this.mLastRegisteredPLMN = null;
                                    this.mLastPSRegisteredPLMN = null;
                                    log("Reset mLastRegisteredPLMN/mLastPSRegisteredPLMNfor ICC refresh");
                                    return;
                                }
                                if (res.efId == 28423) {
                                    this.mLastRegisteredPLMN = null;
                                    this.mLastPSRegisteredPLMN = null;
                                    log("Reset flag of IVSR for IMSI update");
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                        return;
                    case EVENT_IMEI_LOCK /* 107 */:
                        if (this.mPhone.isPhoneTypeGsm()) {
                            log("handle EVENT_IMEI_LOCK GSM");
                            this.mIsImeiLock = true;
                            return;
                        }
                        return;
                    default:
                        MtkServiceStateTracker.super.handleMessage(msg);
                        return;
                }
            }
        }
        log("handle EVENT_RADIO_STATE_CHANGED");
        if (!this.mPhone.isPhoneTypeGsm() && this.mCi.getRadioState() == 1) {
            handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
            queueNextSignalStrengthPoll();
        }
        RadioManager.getInstance().setRadioPower(this.mDesiredPowerState, this.mPhone.getPhoneId());
        modemTriggeredPollState();
    }

    /* access modifiers changed from: protected */
    public void handlePollStateResultMessage(int what, AsyncResult ar) {
        if (what == 4) {
            NetworkRegistrationInfo networkRegState = (NetworkRegistrationInfo) ar.result;
            VoiceSpecificRegistrationInfo voiceSpecificStates = networkRegState.getVoiceSpecificInfo();
            int registrationState = networkRegState.getRegistrationState();
            boolean z = voiceSpecificStates.cssSupported;
            ServiceState.networkTypeToRilRadioTechnology(networkRegState.getAccessNetworkTechnology());
            this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
            MtkServiceStateTracker.super.oppoUpdateVoiceRegState(regCodeToServiceState(registrationState));
            this.mNewSS.setCssIndicator(z ? 1 : 0);
            this.mNewSS.addNetworkRegistrationInfo(networkRegState);
            setPhyCellInfoFromCellIdentity(this.mNewSS, networkRegState.getCellIdentity());
            this.mNewSS.setRilVoiceRegState(registrationState);
            int reasonForDenial = networkRegState.getRejectCause();
            this.mEmergencyOnly = networkRegState.isEmergencyEnabled();
            if (this.mPhone.isPhoneTypeGsm()) {
                this.mGsmRoaming = regCodeIsRoaming(registrationState);
                this.mPhone.getContext().getResources().getBoolean(17891571);
            } else {
                int roamingIndicator = voiceSpecificStates.roamingIndicator;
                int systemIsInPrl = voiceSpecificStates.systemIsInPrl;
                int defaultRoamingIndicator = voiceSpecificStates.defaultRoamingIndicator;
                this.mRegistrationState = registrationState;
                boolean cdmaRoaming = regCodeIsRoaming(registrationState) && !isRoamIndForHomeSystem(roamingIndicator);
                this.mNewSS.setVoiceRoaming(cdmaRoaming);
                if (cdmaRoaming) {
                    this.mNewSS.setRilVoiceRegState(5);
                }
                this.mRoamingIndicator = roamingIndicator;
                this.mIsInPrl = systemIsInPrl != 0;
                this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                int systemId = 0;
                int networkId = 0;
                CellIdentity cellIdentity = networkRegState.getCellIdentity();
                if (cellIdentity != null && cellIdentity.getType() == 2) {
                    systemId = ((CellIdentityCdma) cellIdentity).getSystemId();
                    networkId = ((CellIdentityCdma) cellIdentity).getNetworkId();
                }
                this.mNewSS.setCdmaSystemAndNetworkId(systemId, networkId);
                if (reasonForDenial == 0) {
                    this.mRegistrationDeniedReason = "General";
                } else if (reasonForDenial == 1) {
                    this.mRegistrationDeniedReason = "Authentication Failure";
                } else {
                    this.mRegistrationDeniedReason = "";
                }
                if (this.mRegistrationState == 3) {
                    log("Registration denied, " + this.mRegistrationDeniedReason);
                }
            }
            this.mNewCellIdentity = networkRegState.getCellIdentity();
        } else if (what == 5) {
            NetworkRegistrationInfo networkRegState2 = (NetworkRegistrationInfo) ar.result;
            this.mNewSS.addNetworkRegistrationInfo(networkRegState2);
            DataSpecificRegistrationInfo dataSpecificStates = networkRegState2.getDataSpecificInfo();
            int registrationState2 = networkRegState2.getRegistrationState();
            int serviceState = regCodeToServiceState(registrationState2);
            int newDataRat = ServiceState.networkTypeToRilRadioTechnology(networkRegState2.getAccessNetworkTechnology());
            log("handlePollStateResultMessage: PS cellular. " + networkRegState2);
            if (serviceState == 1) {
                this.mLastPhysicalChannelConfigList = null;
                updateNrFrequencyRangeFromPhysicalChannelConfigs(null, this.mNewSS);
            }
            setPhyCellInfoFromCellIdentity(this.mNewSS, networkRegState2.getCellIdentity());
            this.mNewSS.setRilDataRegState(registrationState2);
            int mtk_data_type = 0;
            try {
                mtk_data_type = Integer.valueOf(TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), PROP_MTK_DATA_TYPE, "0")).intValue();
            } catch (Exception e) {
                loge("INVALID PROP_MTK_DATA_TYPE");
            }
            this.mNewSS.setProprietaryDataRadioTechnology(mtk_data_type);
            if (this.mPhone.isPhoneTypeGsm()) {
                this.mNewReasonDataDenied = networkRegState2.getRejectCause();
                this.mNewMaxDataCalls = dataSpecificStates.maxDataCalls;
                this.mDataRoaming = regCodeIsRoaming(registrationState2);
            } else if (this.mPhone.isPhoneTypeCdma()) {
                this.mNewSS.setDataRoaming(regCodeIsRoaming(registrationState2));
                log("handlPollStateResultMessage: cdma dataServiceState=" + serviceState + " regState=" + registrationState2 + " dataRadioTechnology=" + newDataRat);
            } else {
                int oldDataRAT = this.mSS.getRilDataRadioTechnology();
                if ((oldDataRAT == 0 && newDataRat != 0) || ((ServiceState.isCdma(oldDataRAT) && ServiceState.isLte(newDataRat)) || (ServiceState.isLte(oldDataRAT) && ServiceState.isCdma(newDataRat)))) {
                    this.mCi.getSignalStrength(obtainMessage(3));
                }
                if (regCodeIsRoaming(registrationState2)) {
                    this.mNewSS.setRilDataRegState(5);
                }
                this.mNewSS.setDataRoaming(regCodeIsRoaming(registrationState2));
                log("handlPollStateResultMessage: CdmaLteSST dataServiceState=" + serviceState + " registrationState=" + registrationState2 + " dataRadioTechnology=" + newDataRat);
            }
            this.mNewPSCellIdentity = networkRegState2.getCellIdentity();
            updateServiceStateLteEarfcnBoost(this.mNewSS, getLteEarfcn(networkRegState2.getCellIdentity()));
        } else if (what == 6) {
            NetworkRegistrationInfo networkRegState3 = (NetworkRegistrationInfo) ar.result;
            this.mNewSS.addNetworkRegistrationInfo(networkRegState3);
            log("handlePollStateResultMessage: PS IWLAN. " + networkRegState3);
        } else if (what == 7) {
            String brandOverride = getOperatorBrandOverride();
            this.mCdnr.updateEfForBrandOverride(brandOverride);
            if (this.mPhone.isPhoneTypeGsm()) {
                String[] opNames = (String[]) ar.result;
                if (opNames != null && opNames.length >= 3) {
                    this.mNewSS.setOperatorAlphaLongRaw(opNames[0]);
                    this.mNewSS.setOperatorAlphaShortRaw(opNames[1]);
                    if (brandOverride != null) {
                        log("EVENT_POLL_STATE_OPERATOR: use brandOverride=" + brandOverride);
                        this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                        return;
                    }
                    this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                    return;
                }
                return;
            }
            String[] opNames2 = (String[]) ar.result;
            if (opNames2 == null || opNames2.length < 3) {
                log("EVENT_POLL_STATE_OPERATOR_CDMA: error parsing opNames");
                return;
            }
            if (opNames2[2] == null || opNames2[2].length() < 5 || "00000".equals(opNames2[2]) || "N/AN/A".equals(opNames2[2])) {
                opNames2[2] = SystemProperties.get("ro.cdma.home.operator.numeric", "");
                log("RIL_REQUEST_OPERATOR.response[2], the numeric,  is bad. Using SystemProperties 'ro.cdma.home.operator.numeric'= " + opNames2[2]);
            }
            String numeric = opNames2[2];
            boolean plusCode = false;
            if (numeric != null && numeric.startsWith("2134") && numeric.length() == 7) {
                String tempStr = this.mPlusCodeUtils.checkMccBySidLtmOff(numeric);
                if (!tempStr.equals("0")) {
                    opNames2[2] = tempStr + numeric.substring(4);
                    log("EVENT_POLL_STATE_OPERATOR_CDMA: checkMccBySidLtmOff: numeric =" + tempStr + ", plmn =" + opNames2[2]);
                }
                plusCode = true;
            }
            if (!this.mIsSubscriptionFromRuim) {
                if (plusCode) {
                    opNames2[1] = lookupOperatorName(this.mPhone.getContext(), this.mPhone.getSubId(), opNames2[2], false);
                }
                this.mNewSS.setOperatorName(null, opNames2[1], opNames2[2]);
            } else if (brandOverride != null) {
                log("EVENT_POLL_STATE_OPERATOR_CDMA: use brand=" + brandOverride);
                this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames2[2]);
            } else {
                this.mNewSS.setOperatorName(opNames2[0], opNames2[1], opNames2[2]);
            }
        } else if (what != 14) {
            loge("handlePollStateResultMessage: Unexpected RIL response received: " + what);
        } else {
            int[] ints = (int[]) ar.result;
            this.mNewSS.setIsManualSelection(ints[0] == 1);
            if (ints[0] == 1 && this.mPhone.shouldForceAutoNetworkSelect()) {
                this.mPhone.setNetworkSelectionModeAutomatic((Message) null);
                log(" Forcing Automatic Network Selection, manual selection is not allowed");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateRoamingState() {
        PersistableBundle bundle = getCarrierConfig();
        boolean z = false;
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mGsmRoaming || this.mDataRoaming) {
                z = true;
            }
            boolean roaming = z;
            if (roaming && !isOperatorConsideredRoaming(this.mNewSS) && (isSameNamedOperators(this.mNewSS) || isOperatorConsideredNonRoaming(this.mNewSS))) {
                roaming = false;
            }
            if (alwaysOnHomeNetwork(bundle)) {
                log("updateRoamingState: carrier config override always on home network");
                roaming = false;
            } else if (isNonRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric())) {
                log("updateRoamingState: carrier config override set non roaming:" + this.mNewSS.getOperatorNumeric());
                roaming = false;
            } else if (isRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric())) {
                log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric());
                roaming = true;
            }
            boolean roaming2 = updateOperatorRoaming(this.mNewSS, getHomeOperatorNumeric(), roaming);
            if (SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
                log("Vsim is Enabled, set roaming = false.");
                roaming2 = false;
            }
            if (MtkServiceStateTracker.super.checkCtMacauSimRoamingState(this.mNewSS)) {
                roaming2 = true;
            }
            this.mNewSS.setVoiceRoaming(roaming2);
            this.mNewSS.setDataRoaming(roaming2);
            return;
        }
        String systemId = Integer.toString(this.mNewSS.getCdmaSystemId());
        if (alwaysOnHomeNetwork(bundle)) {
            log("updateRoamingState: carrier config override always on home network");
            setRoamingOff();
        } else if (isNonRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric()) || isNonRoamingInCdmaNetwork(bundle, systemId)) {
            log("updateRoamingState: carrier config override set non-roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
            setRoamingOff();
        } else if (isRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric()) || isRoamingInCdmaNetwork(bundle, systemId)) {
            log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
            setRoamingOn();
        }
        if (MtkServiceStateTracker.super.checkCtMacauSimRoamingState(this.mNewSS)) {
            setRoamingOn();
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("telephony.test.forceRoaming", false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
    }

    /* access modifiers changed from: protected */
    public void handlePollStateResult(int what, AsyncResult ar) {
        boolean isRoamingBetweenOperators;
        if (ar.userObj == this.mPollingContext) {
            if (ar.exception != null) {
                CommandException.Error err = null;
                if (ar.exception instanceof IllegalStateException) {
                    log("handlePollStateResult exception " + ar.exception);
                }
                if (ar.exception instanceof CommandException) {
                    err = ar.exception.getCommandError();
                }
                if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
                    cancelPollState();
                    if (this.hasPendingPollState) {
                        this.hasPendingPollState = false;
                        pollState();
                        loge("handlePollStateResult trigger pending pollState()");
                        return;
                    } else if (this.mCi.getRadioState() != 1) {
                        if (this.mCi.getRadioState() == 2) {
                            this.mNewSS.setStateOutOfService();
                        } else {
                            this.mNewSS.setStateOff();
                        }
                        this.mNewCellIdentity = null;
                        this.mNewPSCellIdentity = null;
                        setSignalStrengthDefaultValues();
                        this.mPsRegStateRaw = 0;
                        pollStateDone();
                        loge("Mlog: pollStateDone to notify RADIO_NOT_AVAILABLE");
                        return;
                    } else {
                        return;
                    }
                } else if (err != CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
                    loge("RIL implementation has returned an error where it must succeed" + ar.exception);
                }
            } else {
                try {
                    handlePollStateResultMessage(what, ar);
                } catch (RuntimeException ex) {
                    loge("Exception while polling service state. Probably malformed RIL response." + ex);
                }
            }
            int[] iArr = this.mPollingContext;
            iArr[0] = iArr[0] - 1;
            if (this.mPollingContext[0] == 0) {
                this.mNewSS.keepCellularDataServiceState();
                if (this.mTransportManager.isInLegacyMode()) {
                    NetworkRegistrationInfo wwanPsNri = this.mNewSS.getNetworkRegistrationInfo(2, 1);
                    try {
                        if (Integer.valueOf(TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), PROP_IWLAN_STATE, "0")).intValue() > 0 && wwanPsNri != null) {
                            log("isInLegacyMode and IWLAN is actived, overwrite (PS, WWAN)");
                            this.mNewSS.addNetworkRegistrationInfo(new NetworkRegistrationInfo(2, 1, 1, 18, wwanPsNri.getRejectCause(), wwanPsNri.isEmergencyEnabled(), wwanPsNri.getAvailableServices(), wwanPsNri.getCellIdentity(), wwanPsNri.getDataSpecificInfo().maxDataCalls, wwanPsNri.getDataSpecificInfo().isDcNrRestricted, wwanPsNri.getDataSpecificInfo().isNrAvailable, wwanPsNri.getDataSpecificInfo().isEnDcAvailable, wwanPsNri.getDataSpecificInfo().getLteVopsSupportInfo(), wwanPsNri.getDataSpecificInfo().isUsingCarrierAggregation()));
                        }
                    } catch (Exception e) {
                    }
                }
                combinePsRegistrationStates(this.mNewSS);
                updateOperatorNameForServiceState(this.mNewSS);
                if (this.mPhone.isPhoneTypeGsm()) {
                    boolean in_service = this.mNewSS.getVoiceRegState() == 0 || this.mNewSS.getDataRegState() == 0;
                    boolean radioOffwithIwlan = this.mCi.getRadioState() == 0 && (this.mNewSS.getRilDataRadioTechnology() == 18 || this.mNewSS.getIwlanRegState() == 0);
                    String oper = this.mNewSS.getOperatorNumeric();
                    if (((in_service || TextUtils.isEmpty(oper)) && (!in_service || !TextUtils.isEmpty(oper))) || radioOffwithIwlan || !this.hasPendingPollState) {
                        updateRoamingState();
                        if (this.mCi.getHalVersion().lessOrEqual(RIL.RADIO_HAL_VERSION_1_2)) {
                            boolean isImsEccOnly = getImsEccOnly();
                            if (!in_service && isImsEccOnly) {
                                this.mEmergencyOnly = true;
                            }
                        }
                        this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                    } else {
                        loge("Temporary service state, need restart PollState");
                        this.hasPendingPollState = false;
                        cancelPollState();
                        modemTriggeredPollState();
                        return;
                    }
                } else {
                    boolean namMatch = false;
                    if (!isSidsAllZeros() && isHomeSid(this.mNewSS.getCdmaSystemId())) {
                        namMatch = true;
                    }
                    if (this.mIsSubscriptionFromRuim && (isRoamingBetweenOperators = isRoamingBetweenOperators(this.mNewSS.getVoiceRoaming(), this.mNewSS)) != this.mNewSS.getVoiceRoaming()) {
                        log("isRoamingBetweenOperators=" + isRoamingBetweenOperators + ". Override CDMA voice roaming to " + isRoamingBetweenOperators);
                        this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators);
                    }
                    if (ServiceState.isCdma(this.mNewSS.getRilDataRadioTechnology())) {
                        if (this.mNewSS.getVoiceRegState() == 0) {
                            boolean isVoiceRoaming = this.mNewSS.getVoiceRoaming();
                            if (this.mNewSS.getDataRoaming() != isVoiceRoaming) {
                                log("Data roaming != Voice roaming. Override data roaming to " + isVoiceRoaming);
                                this.mNewSS.setDataRoaming(isVoiceRoaming);
                            }
                        } else {
                            boolean isRoamIndForHomeSystem = isRoamIndForHomeSystem(this.mRoamingIndicator);
                            boolean dataRoamingState = this.mNewSS.getDataRoaming();
                            if (this.mNewSS.getDataRoaming() == isRoamIndForHomeSystem) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("isRoamIndForHomeSystem=");
                                sb.append(isRoamIndForHomeSystem);
                                sb.append(", override data roaming to ");
                                sb.append(!isRoamIndForHomeSystem);
                                log(sb.toString());
                                this.mNewSS.setDataRoaming(!isRoamIndForHomeSystem);
                            }
                            int[] homeRoamIndicators = getCarrierConfig().getIntArray("cdma_enhanced_roaming_indicator_for_home_network_int_array");
                            if (!dataRoamingState && !isRoamIndForHomeSystem && homeRoamIndicators != null && homeRoamIndicators.length == 0) {
                                log("isRoamIndForHomeSystem=" + isRoamIndForHomeSystem + ", override data roaming to false");
                                this.mNewSS.setDataRoaming(false);
                            }
                        }
                    }
                    if (!this.mEmergencyOnly && this.mCi.getRadioState() == 1) {
                        if (this.mNewSS.getVoiceRegState() == 1 && this.mNewSS.getDataRegState() == 1 && this.mNetworkExsit) {
                            this.mEmergencyOnly = true;
                        }
                        this.mEmergencyOnly = mergeEmergencyOnlyCdmaIms(this.mEmergencyOnly);
                    }
                    this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                    this.mNewSS.setCdmaDefaultRoamingIndicator(this.mDefaultRoamingIndicator);
                    this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                    boolean isPrlLoaded = true;
                    if (TextUtils.isEmpty(this.mPrlVersion)) {
                        isPrlLoaded = false;
                    }
                    if (!isPrlLoaded || this.mNewSS.getRilVoiceRadioTechnology() == 0) {
                        logv("Turn off roaming indicator if !isPrlLoaded or voice RAT is unknown");
                        this.mNewSS.setCdmaRoamingIndicator(1);
                    } else if (!isSidsAllZeros()) {
                        if (!namMatch && !this.mIsInPrl) {
                            this.mNewSS.setCdmaRoamingIndicator(this.mDefaultRoamingIndicator);
                        } else if (!namMatch || this.mIsInPrl) {
                            if (!namMatch && this.mIsInPrl) {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            } else if (this.mRoamingIndicator <= 2) {
                                this.mNewSS.setCdmaRoamingIndicator(1);
                            } else {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            }
                        } else if (ServiceState.isLte(this.mNewSS.getRilVoiceRadioTechnology())) {
                            log("Turn off roaming indicator as voice is LTE");
                            this.mNewSS.setCdmaRoamingIndicator(1);
                        } else {
                            this.mNewSS.setCdmaRoamingIndicator(2);
                        }
                    }
                    int roamingIndicator = this.mNewSS.getCdmaRoamingIndicator();
                    this.mNewSS.setCdmaEriIconIndex(this.mEriManager.getCdmaEriIconIndex(roamingIndicator, this.mDefaultRoamingIndicator));
                    this.mNewSS.setCdmaEriIconMode(this.mEriManager.getCdmaEriIconMode(roamingIndicator, this.mDefaultRoamingIndicator));
                    log("Set CDMA Roaming Indicator to: " + this.mNewSS.getCdmaRoamingIndicator() + ". voiceRoaming = " + this.mNewSS.getVoiceRoaming() + ". dataRoaming = " + this.mNewSS.getDataRoaming() + ", isPrlLoaded = " + isPrlLoaded + ". namMatch = " + namMatch + " , mIsInPrl = " + this.mIsInPrl + ", mRoamingIndicator = " + this.mRoamingIndicator + ", mDefaultRoamingIndicator= " + this.mDefaultRoamingIndicator + ", set mEmergencyOnly=" + this.mEmergencyOnly + ", mNetworkExsit=" + this.mNetworkExsit);
                }
                pollStateDone();
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x02d7  */
    public void updateSpnDisplayLegacy() {
        String dataSpn;
        boolean noService;
        boolean showSpn;
        String spn;
        String plmn;
        String plmn2;
        boolean z;
        String plmn3;
        boolean showPlmn;
        String plmn4;
        boolean showPlmn2;
        String plmn5;
        String dataSpn2;
        String spn2;
        boolean showPlmn3;
        logv("updateSpnDisplayLegacy+");
        String wfcVoiceSpnFormat = null;
        String wfcDataSpnFormat = null;
        String wfcFlightSpnFormat = null;
        int combinedRegState = getCombinedRegState(this.mSS);
        if (this.mPhone.getImsPhone() == null || !this.mPhone.getImsPhone().isWifiCallingEnabled() || combinedRegState != 0) {
            dataSpn = null;
        } else {
            PersistableBundle bundle = getCarrierConfig();
            int voiceIdx = bundle.getInt("wfc_spn_format_idx_int");
            int dataIdx = bundle.getInt("wfc_data_spn_format_idx_int");
            int flightModeIdx = bundle.getInt("wfc_flight_mode_spn_format_idx_int");
            String[] wfcSpnFormats = SubscriptionManager.getResourcesForSubId(this.mPhone.getContext(), this.mPhone.getSubId(), bundle.getBoolean("wfc_spn_use_root_locale")).getStringArray(17236133);
            if (voiceIdx < 0 || voiceIdx >= wfcSpnFormats.length) {
                StringBuilder sb = new StringBuilder();
                dataSpn = null;
                sb.append("updateSpnDisplay: KEY_WFC_SPN_FORMAT_IDX_INT out of bounds: ");
                sb.append(voiceIdx);
                loge(sb.toString());
                voiceIdx = 0;
            } else {
                dataSpn = null;
            }
            if (dataIdx < 0 || dataIdx >= wfcSpnFormats.length) {
                loge("updateSpnDisplay: KEY_WFC_DATA_SPN_FORMAT_IDX_INT out of bounds: " + dataIdx);
                dataIdx = 0;
            }
            if (flightModeIdx < 0 || flightModeIdx >= wfcSpnFormats.length) {
                flightModeIdx = voiceIdx;
            }
            wfcVoiceSpnFormat = wfcSpnFormats[voiceIdx];
            wfcDataSpnFormat = wfcSpnFormats[dataIdx];
            wfcFlightSpnFormat = wfcSpnFormats[flightModeIdx];
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            String simNumeric = TelephonyManager.from(this.mPhone.getContext()).getSimOperatorNumericForPhone(getPhoneId());
            IccRecords iccRecords = this.mIccRecords;
            int rule = getCarrierNameDisplayBitmask(this.mSS);
            boolean noService2 = false;
            if (combinedRegState == 1 || combinedRegState == 2) {
                boolean forceDisplayNoService = this.mPhone.getContext().getResources().getBoolean(17891414) && !this.mIsSimReady;
                if (!this.mEmergencyOnly || forceDisplayNoService) {
                    plmn4 = Resources.getSystem().getText(17040247).toString();
                    noService2 = true;
                } else {
                    plmn4 = Resources.getSystem().getText(17039896).toString();
                }
                log("updateSpnDisplay: radio is on but out of service, set plmn='" + plmn4 + "'");
                showPlmn2 = true;
                showPlmn = noService2;
            } else if (combinedRegState == 0) {
                this.mSS.getOperatorAlpha();
                String plmn6 = MtkServiceStateTracker.super.oppoGetPlmn();
                boolean showPlmn4 = !TextUtils.isEmpty(plmn6) && (rule & 2) == 2;
                log("updateSpnDisplay: rawPlmn = " + plmn6);
                showPlmn = false;
                plmn4 = plmn6;
                showPlmn2 = showPlmn4;
            } else {
                showPlmn2 = true;
                plmn4 = Resources.getSystem().getText(17040247).toString();
                log("updateSpnDisplay: radio is off w/ showPlmn=true plmn=" + plmn4);
                showPlmn = false;
            }
            if (this.mIsImeiLock) {
                plmn4 = Resources.getSystem().getText(134545510).toString();
            }
            String spn3 = getServiceProviderName();
            if (this.mSubscriptionController.isHasSoftSimCard() && this.mSubscriptionController.getSoftSimCardSlotId() == this.mPhone.getPhoneId()) {
                spn3 = OemTelephonyUtils.getReadTeaServiceProviderName(this.mPhone.getContext(), spn3);
            }
            if (!TextUtils.isEmpty(plmn4) || !TextUtils.isEmpty(spn3)) {
                plmn5 = plmn4;
            } else {
                log("PLMN and SPN both null, simNumeric = " + simNumeric);
                String plmn7 = OppoTelephonyFunction.oppoGetPlmnOverride(this.mPhone.getContext(), simNumeric, this.mSS);
                if (!TextUtils.isEmpty(plmn7) || !isVowifiRegistered(this.mPhone.getPhoneId())) {
                    plmn5 = plmn7;
                } else {
                    int tmplac = this.mPhone.getPhoneType() == 1 ? getLac() : -1;
                    plmn5 = this.mCi.lookupOperatorName(this.mPhone.getSubId(), simNumeric, true, tmplac >= 0 ? tmplac : 0);
                }
            }
            boolean showSpn2 = !showPlmn && !TextUtils.isEmpty(spn3) && (rule & 1) == 1;
            log("updateSpnDisplay: rawSpn = " + spn3);
            if (TextUtils.isEmpty(spn3) || TextUtils.isEmpty(wfcVoiceSpnFormat)) {
                dataSpn2 = spn3;
            } else if (!TextUtils.isEmpty(wfcDataSpnFormat)) {
                if (this.mSS.getVoiceRegState() == 3) {
                    wfcVoiceSpnFormat = wfcFlightSpnFormat;
                }
                String originalSpn = spn3.trim();
                spn3 = String.format(wfcVoiceSpnFormat, originalSpn);
                showSpn2 = true;
                showPlmn2 = false;
                dataSpn2 = String.format(wfcDataSpnFormat, originalSpn);
                if (this.mSS.getVoiceRegState() != 0 || this.mSS.getDataRegState() == 0) {
                    spn2 = spn3;
                } else {
                    showSpn2 = false;
                    spn2 = null;
                }
                try {
                    if (this.mServiceStateTrackerExt.needSpnRuleShowPlmnOnly() && !TextUtils.isEmpty(plmn5)) {
                        log("origin showSpn:" + showSpn2 + " showPlmn:" + showPlmn2 + " rule:" + rule);
                        showPlmn2 = true;
                        rule = 2;
                        showSpn2 = false;
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                try {
                    if (!this.mServiceStateTrackerExt.allowSpnDisplayed() && rule == 3) {
                        spn2 = null;
                        showSpn2 = false;
                    }
                } catch (RuntimeException e2) {
                    e2.printStackTrace();
                }
                MtkServiceStateTracker.super.oppoUpdateGsmSpnDisplay(plmn5, spn2, showPlmn2, showSpn2);
                String plmn8 = MtkServiceStateTracker.super.getPlmnResult();
                String spn4 = MtkServiceStateTracker.super.getSpnResult();
                showPlmn3 = MtkServiceStateTracker.super.getShowPlmnResult();
                showSpn = MtkServiceStateTracker.super.getShowSpnResult();
                if ("52003".equals(simNumeric) && !TextUtils.equals(spn4, plmn8)) {
                    showPlmn3 = true;
                    showSpn = false;
                }
                if (!showPlmn3 && !showSpn && !TextUtils.isEmpty(plmn8)) {
                    showPlmn3 = true;
                }
                noService = showPlmn3;
                plmn = spn4;
                spn = plmn8;
                plmn2 = dataSpn2;
            } else {
                dataSpn2 = spn3;
            }
            if (TextUtils.isEmpty(plmn5) || TextUtils.isEmpty(wfcVoiceSpnFormat)) {
                if (this.mSS.getVoiceRegState() == 3 || (showPlmn2 && TextUtils.equals(spn3, plmn5))) {
                    spn3 = null;
                    showSpn2 = false;
                }
                if (this.mSS.getVoiceRegState() != 0) {
                }
                spn2 = spn3;
                log("origin showSpn:" + showSpn2 + " showPlmn:" + showPlmn2 + " rule:" + rule);
                showPlmn2 = true;
                rule = 2;
                showSpn2 = false;
                spn2 = null;
                showSpn2 = false;
                MtkServiceStateTracker.super.oppoUpdateGsmSpnDisplay(plmn5, spn2, showPlmn2, showSpn2);
                String plmn82 = MtkServiceStateTracker.super.getPlmnResult();
                String spn42 = MtkServiceStateTracker.super.getSpnResult();
                showPlmn3 = MtkServiceStateTracker.super.getShowPlmnResult();
                showSpn = MtkServiceStateTracker.super.getShowSpnResult();
                showPlmn3 = true;
                showSpn = false;
                showPlmn3 = true;
                noService = showPlmn3;
                plmn = spn42;
                spn = plmn82;
                plmn2 = dataSpn2;
            } else {
                plmn5 = String.format(wfcVoiceSpnFormat, plmn5.trim());
                if (this.mSS.getVoiceRegState() != 0) {
                }
                spn2 = spn3;
                log("origin showSpn:" + showSpn2 + " showPlmn:" + showPlmn2 + " rule:" + rule);
                showPlmn2 = true;
                rule = 2;
                showSpn2 = false;
                spn2 = null;
                showSpn2 = false;
                MtkServiceStateTracker.super.oppoUpdateGsmSpnDisplay(plmn5, spn2, showPlmn2, showSpn2);
                String plmn822 = MtkServiceStateTracker.super.getPlmnResult();
                String spn422 = MtkServiceStateTracker.super.getSpnResult();
                showPlmn3 = MtkServiceStateTracker.super.getShowPlmnResult();
                showSpn = MtkServiceStateTracker.super.getShowSpnResult();
                showPlmn3 = true;
                showSpn = false;
                showPlmn3 = true;
                noService = showPlmn3;
                plmn = spn422;
                spn = plmn822;
                plmn2 = dataSpn2;
            }
        } else {
            String eriText = getOperatorNameFromEri();
            if (eriText != null) {
                this.mSS.setOperatorAlphaLong(eriText);
            }
            updateOperatorNameFromCarrierConfig();
            String plmn9 = OppoTelephonyFunction.oppoGetPlmnOverride(this.mPhone.getContext(), this.mSS.getOperatorNumeric(), this.mSS);
            log("updateSpnDisplay: cdma rawPlmn = " + plmn9);
            boolean showPlmn5 = plmn9 != null;
            if (plmn9 != null && plmn9.equals("")) {
                plmn9 = null;
            }
            if (TextUtils.isEmpty(plmn9) || TextUtils.isEmpty(wfcVoiceSpnFormat)) {
                z = false;
                if (this.mCi.getRadioState() == 0) {
                    logv("updateSpnDisplay: overwriting plmn from " + plmn9 + " to null as radio state is off");
                    plmn9 = null;
                }
            } else {
                z = false;
                plmn9 = String.format(wfcVoiceSpnFormat, plmn9.trim());
            }
            if (combinedRegState == 1 || combinedRegState == 3) {
                showPlmn5 = true;
                String plmn10 = Resources.getSystem().getText(17040247).toString();
                log("updateSpnDisplay: radio is on but out of svc, set plmn='" + plmn10 + "'");
                plmn3 = plmn10;
            } else {
                plmn3 = plmn9;
            }
            String spn5 = "";
            boolean showSpn3 = false;
            try {
                if (this.mServiceStateTrackerExt.allowSpnDisplayed()) {
                    int rule2 = getCarrierNameDisplayBitmask(this.mSS);
                    spn5 = getServiceProviderName();
                    showSpn3 = (TextUtils.isEmpty(spn5) || (rule2 & 1) != 1 || this.mSS.getVoiceRegState() == 3 || this.mSS.getRoaming()) ? z : true;
                    logv("[CDMA]updateSpnDisplay: rule=" + rule2 + ", spn=" + spn5 + ", showSpn=" + showSpn3);
                }
                if (plmn3 != null) {
                    z = true;
                }
                showPlmn5 = z;
                if (this.mServiceStateTrackerExt.allowSpnDisplayed()) {
                    if (this.mSS.getVoiceRegState() == 3 || this.mSS.getVoiceRegState() == 1 || this.mSS.getRoaming() || TextUtils.isEmpty(spn5)) {
                        showSpn3 = false;
                        showPlmn5 = true;
                    } else {
                        showSpn3 = true;
                        showPlmn5 = false;
                    }
                }
            } catch (RuntimeException e3) {
                e3.printStackTrace();
            }
            if (TextUtils.equals(plmn3, "")) {
                plmn3 = this.mCurPlmn;
            }
            MtkServiceStateTracker.super.oppoUpdateCdmaSpnDisplay(plmn3, spn5, showPlmn5, showSpn3);
            String plmn11 = MtkServiceStateTracker.super.getPlmnResult();
            plmn = MtkServiceStateTracker.super.getSpnResult();
            spn = plmn11;
            plmn2 = dataSpn;
            noService = MtkServiceStateTracker.super.getShowPlmnResult();
            showSpn = MtkServiceStateTracker.super.getShowSpnResult();
        }
        notifySpnDisplayUpdate(new CarrierDisplayNameData.Builder().setSpn(plmn).setDataSpn(plmn2).setShowSpn(showSpn).setPlmn(spn).setShowPlmn(noService).build());
        logv("updateSpnDisplayLegacy-");
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        if (this.mPhone.isPhoneTypeGsm()) {
            Rlog.d(LOG_TAG, "[GsmSST" + this.mPhone.getPhoneId() + "] " + s);
        } else if (this.mPhone.isPhoneTypeCdma()) {
            Rlog.d(LOG_TAG, "[CdmaSST" + this.mPhone.getPhoneId() + "] " + s);
        } else {
            Rlog.d(LOG_TAG, "[CdmaLteSST" + this.mPhone.getPhoneId() + "] " + s);
        }
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        if (this.mPhone.isPhoneTypeGsm()) {
            Rlog.e(LOG_TAG, "[GsmSST" + this.mPhone.getPhoneId() + "] " + s);
        } else if (this.mPhone.isPhoneTypeCdma()) {
            Rlog.e(LOG_TAG, "[CdmaSST" + this.mPhone.getPhoneId() + "] " + s);
        } else {
            Rlog.e(LOG_TAG, "[CdmaLteSST" + this.mPhone.getPhoneId() + "] " + s);
        }
    }

    /* access modifiers changed from: protected */
    public void logv(String s) {
        if (this.mPhone.isPhoneTypeGsm()) {
            Rlog.v(LOG_TAG, "[GsmSST" + this.mPhone.getPhoneId() + "] " + s);
        } else if (this.mPhone.isPhoneTypeCdma()) {
            Rlog.v(LOG_TAG, "[CdmaSST" + this.mPhone.getPhoneId() + "] " + s);
        } else {
            Rlog.v(LOG_TAG, "[CdmaLteSST" + this.mPhone.getPhoneId() + "] " + s);
        }
    }

    private void onNetworkStateChangeResult(AsyncResult ar) {
        try {
            if (ar.exception == null) {
                if (ar.result != null) {
                    String[] info = (String[]) ar.result;
                    boolean z = false;
                    if (this.mPhone.isPhoneTypeGsm()) {
                        int Act = -1;
                        int cause = -1;
                        if (info.length > 0) {
                            int state = Integer.parseInt(info[0]);
                            if (info[1] != null && info[1].length() > 0) {
                                Integer.parseInt(info[1], 16);
                            }
                            if (info[2] != null && info[2].length() > 0) {
                                if (info[2].equals("FFFFFFFF") || info[2].equals("ffffffff")) {
                                    log("Invalid cid:" + info[2]);
                                    info[2] = "0000ffff";
                                }
                                Integer.parseInt(info[2], 16);
                            }
                            if (info[3] != null && info[3].length() > 0) {
                                Act = Integer.parseInt(info[3]);
                            }
                            if (info[4] != null && info[4].length() > 0) {
                                cause = Integer.parseInt(info[4]);
                            }
                            log("onNetworkStateChangeResult state:" + state + " Act:" + Act + " cause:" + cause);
                            try {
                                if (this.mServiceStateTrackerExt.needRejectCauseNotification(cause)) {
                                    setRejectCauseNotification(cause);
                                    return;
                                }
                                return;
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                                return;
                            }
                        } else {
                            log("onNetworkStateChangeResult length zero");
                            return;
                        }
                    } else if (info.length <= 5) {
                        log("onCdmaNetworkExistStateChanged Network existence not reported");
                        return;
                    } else if (info[5] != null && info[5].length() > 0) {
                        if (1 == Integer.parseInt(info[5])) {
                            z = true;
                        }
                        this.mNetworkExsit = z;
                        return;
                    } else {
                        return;
                    }
                }
            }
            loge("onNetworkStateChangeResult exception");
        } catch (NumberFormatException e2) {
            Rlog.d(LOG_TAG, e2.toString());
        } catch (Exception e3) {
            Rlog.d(LOG_TAG, e3.toString());
        }
    }

    private void onPsNetworkStateChangeResult(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("onPsNetworkStateChangeResult exception");
            return;
        }
        int[] info = (int[]) ar.result;
        if (info.length < 6) {
            this.mPsRegStateRaw = info[0];
            String operator_plmn = String.valueOf(info[1]);
            if (operator_plmn == null || operator_plmn.length() < 5) {
                updateLocatedPlmn(null);
            } else {
                updateLocatedPlmn(operator_plmn);
            }
        } else if (info.length != 7) {
            loge("onPsNetworkStateChangeResult wrong size");
        } else if (info[0] == 4) {
            this.mPhone.notifyMtkFakeServiceStateChanged(null);
        } else if (info[0] != 1) {
            this.mPhone.notifyMtkFakeServiceStateChanged(null);
            this.mPhone.notifyServiceStateChanged(this.mSS);
        } else {
            boolean isUsingCarrierAggregation = false;
            boolean isCs = false;
            MtkServiceState fakeSS = new MtkServiceState();
            int networkType = ServiceState.rilRadioTechnologyToNetworkType(info[1]);
            if (info[1] == 3 || info[1] == 16) {
                isCs = true;
            }
            if (networkType == 19) {
                isUsingCarrierAggregation = true;
                networkType = 13;
            }
            NetworkRegistrationInfo networkVoiceRegState = new NetworkRegistrationInfo(1, 1, isCs ? info[0] : 0, isCs ? networkType : 0, 0, false, null, null, false, 0, 0, 0);
            fakeSS.setVoiceRegState(regCodeToServiceState(networkVoiceRegState.getRegistrationState()));
            fakeSS.addNetworkRegistrationInfo(networkVoiceRegState);
            NetworkRegistrationInfo networkDataRegState = new NetworkRegistrationInfo(2, 1, !isCs ? info[0] : 0, !isCs ? networkType : 0, 0, false, null, null, 0, info[4] == 1, info[5] == 2, info[6] == 1, new LteVopsSupportInfo(1, 1), isUsingCarrierAggregation);
            fakeSS.setDataRegState(regCodeToServiceState(networkDataRegState.getRegistrationState()));
            fakeSS.addNetworkRegistrationInfo(networkDataRegState);
            log("broadcast fakeSS:" + fakeSS);
            this.mPhone.notifyMtkFakeServiceStateChanged(fakeSS);
        }
    }

    public void pollState(boolean modemTriggered) {
        boolean connected = ((NetworkRegistrationManager) this.mRegStateManagers.get(1)).isServiceConnected();
        int support_ap_iwlan = this.mRegStateManagers.get(2) != null ? 1 : 0;
        StringBuilder sb = new StringBuilder();
        sb.append("pollState: modemTriggered=");
        sb.append(modemTriggered);
        sb.append(", mPollingContext=");
        sb.append(this.mPollingContext != null ? this.mPollingContext[0] : -1);
        sb.append(", RadioState=");
        sb.append(this.mCi.getRadioState());
        sb.append(", connected=");
        sb.append(connected);
        sb.append(", support_ap_iwlan=");
        sb.append(support_ap_iwlan);
        log(sb.toString());
        if (this.mPollingContext == null || this.mCi.getRadioState() == 2 || ((!this.mPhone.isPhoneTypeGsm() || this.mPollingContext[0] != support_ap_iwlan + 4) && (this.mPhone.isPhoneTypeGsm() || this.mPollingContext[0] != support_ap_iwlan + 3))) {
            this.mPollingContext = new int[1];
            this.mPollingContext[0] = 0;
            int radioState = this.mCi.getRadioState();
            if (radioState == 0) {
                this.mNewSS.setStateOff();
                this.mNewCellIdentity = null;
                this.mNewPSCellIdentity = null;
                setSignalStrengthDefaultValues();
                this.mNitzState.handleNetworkCountryCodeUnavailable();
                if (modemTriggered || 18 == this.mSS.getRilDataRadioTechnology()) {
                    cleanMccProperties(this.mPhone.getPhoneId());
                } else {
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mPsRegStateRaw = 0;
                    }
                    pollStateDone();
                    return;
                }
            } else if (radioState == 2) {
                this.mNewSS.setStateOutOfService();
                this.mNewCellIdentity = null;
                this.mNewPSCellIdentity = null;
                setSignalStrengthDefaultValues();
                this.mNitzState.handleNetworkCountryCodeUnavailable();
                if (this.mPhone.isPhoneTypeGsm()) {
                    this.mPsRegStateRaw = 0;
                }
                pollStateDone();
                return;
            }
            if (!connected) {
                log("Skip pollState due to disconnection of service");
                updateTurboPLMN();
                return;
            }
            int[] iArr = this.mPollingContext;
            iArr[0] = iArr[0] + 1;
            this.mCi.getOperator(obtainMessage(7, this.mPollingContext));
            int[] iArr2 = this.mPollingContext;
            iArr2[0] = iArr2[0] + 1;
            ((NetworkRegistrationManager) this.mRegStateManagers.get(1)).requestNetworkRegistrationInfo(2, obtainMessage(5, this.mPollingContext));
            int[] iArr3 = this.mPollingContext;
            iArr3[0] = iArr3[0] + 1;
            ((NetworkRegistrationManager) this.mRegStateManagers.get(1)).requestNetworkRegistrationInfo(1, obtainMessage(4, this.mPollingContext));
            if (this.mRegStateManagers.get(2) != null) {
                int[] iArr4 = this.mPollingContext;
                iArr4[0] = iArr4[0] + 1;
                ((NetworkRegistrationManager) this.mRegStateManagers.get(2)).requestNetworkRegistrationInfo(2, obtainMessage(6, this.mPollingContext));
            }
            if (this.mPhone.isPhoneTypeGsm()) {
                int[] iArr5 = this.mPollingContext;
                iArr5[0] = iArr5[0] + 1;
                this.mCi.getNetworkSelectionMode(obtainMessage(14, this.mPollingContext));
                return;
            }
            return;
        }
        this.hasPendingPollState = true;
    }

    /* JADX INFO: Multiple debug info for r8v20 android.telephony.CellIdentity: [D('mMtkNewSS' mediatek.telephony.MtkServiceState), D('tempCellId' android.telephony.CellIdentity)] */
    /* JADX INFO: Multiple debug info for r4v35 int: [D('hasPlmnChange' boolean), D('transport' int)] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:186:0x03ce, code lost:
        if (r54.mNewSS.getRilDataRadioTechnology() == 13) goto L_0x03d3;
     */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0464  */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x057c A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x058c  */
    /* JADX WARNING: Removed duplicated region for block: B:243:0x0590  */
    /* JADX WARNING: Removed duplicated region for block: B:247:0x05d9  */
    /* JADX WARNING: Removed duplicated region for block: B:260:0x0604  */
    /* JADX WARNING: Removed duplicated region for block: B:267:0x0644  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x06ca  */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x06ff  */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x0714  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x075f  */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x0779  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0780  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0799  */
    /* JADX WARNING: Removed duplicated region for block: B:303:0x07aa  */
    /* JADX WARNING: Removed duplicated region for block: B:311:0x07f0  */
    /* JADX WARNING: Removed duplicated region for block: B:312:0x0813  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x085f  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0866  */
    /* JADX WARNING: Removed duplicated region for block: B:332:0x08a4  */
    /* JADX WARNING: Removed duplicated region for block: B:338:0x0914  */
    /* JADX WARNING: Removed duplicated region for block: B:344:0x092d  */
    /* JADX WARNING: Removed duplicated region for block: B:346:0x0944  */
    /* JADX WARNING: Removed duplicated region for block: B:349:0x0957  */
    /* JADX WARNING: Removed duplicated region for block: B:372:0x09cb  */
    /* JADX WARNING: Removed duplicated region for block: B:374:0x09d0  */
    /* JADX WARNING: Removed duplicated region for block: B:385:0x09ef  */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x09f6  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x09fd  */
    /* JADX WARNING: Removed duplicated region for block: B:391:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0a0b  */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0a4e  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0a5b  */
    /* JADX WARNING: Removed duplicated region for block: B:408:0x0a6c  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x0ab4  */
    /* JADX WARNING: Removed duplicated region for block: B:420:0x0abb  */
    /* JADX WARNING: Removed duplicated region for block: B:423:0x0acb  */
    /* JADX WARNING: Removed duplicated region for block: B:433:? A[RETURN, SYNTHETIC] */
    public void pollStateDone() {
        boolean hasVoiceRegStateChanged;
        boolean hasNrStateChanged;
        boolean hasNrStateChanged2;
        boolean hasDataRoamingOff;
        boolean hasLostMultiApnSupport;
        boolean hasCssIndicatorChanged;
        boolean hasMultiApnSupport;
        boolean has4gHandoff;
        boolean has4gHandoff2;
        boolean hasVoiceRoamingTypeChange;
        boolean hasVoiceRoamingTypeChange2;
        boolean hasDataRoamingTypeChange;
        boolean hasPlmnChange;
        boolean hasChanged;
        boolean hasDataRoamingTypeChange2;
        boolean hasVoiceRoamingOff;
        int newOosFlag;
        ServiceState oldMergedSS;
        SparseBooleanArray hasDataDetached;
        boolean hasVoiceRoamingOn;
        boolean shouldLogAttachedChange;
        int length;
        boolean shouldLogRatChange;
        boolean shouldLogAttachedChange2;
        int i;
        boolean z;
        String str;
        boolean hasRegistered;
        String operatorNumeric;
        boolean z2;
        int i2;
        boolean has4gHandoff3;
        boolean hasMultiApnSupport2;
        int oldRAT;
        int newRAT;
        int oldRegState;
        int newRegState;
        MtkServiceState mMtkSS = this.mSS;
        MtkServiceState mMtkNewSS = this.mNewSS;
        if (!OemFeature.FEATURE_NW_REG_SWITCH_SMOOTH || MtkServiceStateTracker.super.oppoOosDelayState(this.mNewSS) != 1) {
            MtkServiceStateTracker.super.oppoPollStateDone(this.mNewSS);
            if (this.mNewCellIdentity != null) {
                refreshSpn(this.mNewSS, this.mNewCellIdentity.asCellLocation(), true);
            } else if (this.mNewPSCellIdentity != null) {
                log("use mNewPSCellIdentity " + this.mNewPSCellIdentity);
                refreshSpn(this.mNewSS, this.mNewPSCellIdentity.asCellLocation(), true);
                this.mNewCellIdentity = this.mNewPSCellIdentity;
            } else {
                refreshSpn(this.mNewSS, null, true);
            }
            updateNrStateFromPhysicalChannelConfigs(this.mLastPhysicalChannelConfigList, this.mNewSS);
            if (!this.mPhone.isPhoneTypeGsm()) {
                updateRoamingState();
            }
            if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("telephony.test.forceRoaming", false)) {
                this.mNewSS.setVoiceRoaming(true);
                this.mNewSS.setDataRoaming(true);
            }
            useDataRegStateForDataOnlyDevices();
            processIwlanRegistrationInfo();
            MtkServiceState mFinalMtkNewSS = new MtkServiceState(this.mNewSS);
            setRoamingType(mFinalMtkNewSS);
            log("Poll ServiceState done:  oldSS=[" + this.mSS + "] newSS=[" + mFinalMtkNewSS + "] oldMaxDataCalls=" + this.mMaxDataCalls + " mNewMaxDataCalls=" + this.mNewMaxDataCalls + " oldReasonDataDenied=" + this.mReasonDataDenied + " mNewReasonDataDenied=" + this.mNewReasonDataDenied + " isImsEccOnly= " + getImsEccOnly());
            boolean hasRegistered2 = this.mSS.getVoiceRegState() != 0 && this.mNewSS.getVoiceRegState() == 0;
            boolean hasDeregistered = this.mSS.getVoiceRegState() == 0 && this.mNewSS.getVoiceRegState() != 0;
            boolean oppoHasDataAttached = this.mSS.getDataRegState() != 0 && this.mNewSS.getDataRegState() == 0;
            boolean hasAirplaneModeOnChanged = this.mSS.getVoiceRegState() != 3 && this.mNewSS.getVoiceRegState() == 3;
            SparseBooleanArray hasDataAttached = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
            SparseBooleanArray hasDataDetached2 = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
            SparseBooleanArray hasRilDataRadioTechnologyChanged = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
            SparseBooleanArray hasDataRegStateChanged = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
            boolean anyDataRatChanged = false;
            int[] availableTransports = this.mTransportManager.getAvailableTransports();
            int length2 = availableTransports.length;
            boolean anyDataRegChanged = false;
            int i3 = 0;
            while (i3 < length2) {
                int transport = availableTransports[i3];
                NetworkRegistrationInfo oldNrs = this.mSS.getNetworkRegistrationInfo(2, transport);
                NetworkRegistrationInfo newNrs = this.mNewSS.getNetworkRegistrationInfo(2, transport);
                hasDataAttached.put(transport, (oldNrs == null || !oldNrs.isInService() || hasAirplaneModeOnChanged) && newNrs != null && newNrs.isInService());
                hasDataDetached2.put(transport, oldNrs != null && oldNrs.isInService() && (newNrs == null || !newNrs.isInService()));
                if (oldNrs != null) {
                    oldRAT = oldNrs.getAccessNetworkTechnology();
                } else {
                    oldRAT = 0;
                }
                if (newNrs != null) {
                    newRAT = newNrs.getAccessNetworkTechnology();
                } else {
                    newRAT = 0;
                }
                hasRilDataRadioTechnologyChanged.put(transport, oldRAT != newRAT);
                if (oldRAT != newRAT) {
                    anyDataRatChanged = true;
                }
                if (oldNrs != null) {
                    oldRegState = oldNrs.getRegistrationState();
                } else {
                    oldRegState = 4;
                }
                if (newNrs != null) {
                    newRegState = newNrs.getRegistrationState();
                } else {
                    newRegState = 4;
                }
                hasDataRegStateChanged.put(transport, oldRegState != newRegState);
                if (oldRegState != newRegState) {
                    anyDataRegChanged = true;
                }
                i3++;
                mMtkSS = mMtkSS;
                availableTransports = availableTransports;
                oppoHasDataAttached = oppoHasDataAttached;
                length2 = length2;
                hasAirplaneModeOnChanged = hasAirplaneModeOnChanged;
            }
            boolean hasVoiceRegStateChanged2 = this.mSS.getVoiceRegState() != this.mNewSS.getVoiceRegState();
            boolean hasNrFrequencyRangeChanged = this.mSS.getNrFrequencyRange() != this.mNewSS.getNrFrequencyRange();
            boolean hasNrStateChanged3 = isNrStateChanged(this.mSS.getNetworkRegistrationInfo(2, 1), this.mNewSS.getNetworkRegistrationInfo(2, 1));
            if (hasNrStateChanged3) {
                this.mEndcBearControl.updateAnyNrBearerAllocationStatus(this.mPhone.getPhoneId(), 3 == this.mNewSS.getNrState());
            }
            if (isSib2UriChange(this.mSS.getNetworkRegistrationInfo(2, 1), this.mNewSS.getNetworkRegistrationInfo(2, 1))) {
                hasVoiceRegStateChanged = hasVoiceRegStateChanged2;
                this.mEndcBearControl.updateSib2UpLayerInd(this.mPhone.getPhoneId(), getSib2Uri(this.mNewSS.getNetworkRegistrationInfo(2, 1)));
            } else {
                hasVoiceRegStateChanged = hasVoiceRegStateChanged2;
            }
            boolean hasLocationChanged = !Objects.equals(this.mNewCellIdentity, this.mCellIdentity);
            if (this.mNewSS.getDataRegState() == 0) {
                mMtkNewSS.getIwlanRegState();
            }
            boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
            boolean hasChanged2 = !mFinalMtkNewSS.equals(this.mSS) || this.mForceBroadcastServiceState;
            boolean hasVoiceRoamingOn2 = !this.mSS.getVoiceRoaming() && this.mNewSS.getVoiceRoaming();
            boolean hasVoiceRoamingOff2 = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
            if (mMtkNewSS.getIwlanRegState() == 0) {
                hasNrStateChanged = hasNrStateChanged3;
                hasNrStateChanged2 = false;
            } else {
                hasNrStateChanged = hasNrStateChanged3;
                hasNrStateChanged2 = !this.mSS.getDataRoaming() && this.mNewSS.getDataRoaming();
            }
            if (mMtkNewSS.getIwlanRegState() == 0) {
                hasDataRoamingOff = this.mSS.getDataRoaming();
            } else {
                hasDataRoamingOff = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
            }
            boolean hasRejectCauseChanged = this.mRejectCode != this.mNewRejectCode;
            boolean hasCssIndicatorChanged2 = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
            if (this.mPhone.isPhoneTypeCdmaLte()) {
                hasCssIndicatorChanged = hasCssIndicatorChanged2;
                boolean has4gHandoff4 = this.mNewSS.getDataRegState() == 0 && ((ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mNewSS.getRilDataRadioTechnology() == 13) || (this.mSS.getRilDataRadioTechnology() == 13 && ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology())));
                if (!ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology())) {
                    has4gHandoff3 = has4gHandoff4;
                } else {
                    has4gHandoff3 = has4gHandoff4;
                }
                if (!ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mSS.getRilDataRadioTechnology() != 13) {
                    hasMultiApnSupport2 = true;
                    hasMultiApnSupport = hasMultiApnSupport2;
                    hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() < 4 && this.mNewSS.getRilDataRadioTechnology() <= 8;
                    has4gHandoff = has4gHandoff3;
                }
                hasMultiApnSupport2 = false;
                hasMultiApnSupport = hasMultiApnSupport2;
                hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() < 4 && this.mNewSS.getRilDataRadioTechnology() <= 8;
                has4gHandoff = has4gHandoff3;
            } else {
                hasCssIndicatorChanged = hasCssIndicatorChanged2;
                hasMultiApnSupport = false;
                hasLostMultiApnSupport = false;
                has4gHandoff = false;
            }
            if (!this.mSS.getVoiceRoaming() || !mFinalMtkNewSS.getVoiceRoaming()) {
                has4gHandoff2 = has4gHandoff;
            } else {
                has4gHandoff2 = has4gHandoff;
                if (this.mSS.getVoiceRoamingType() != mFinalMtkNewSS.getVoiceRoamingType()) {
                    hasVoiceRoamingTypeChange = true;
                    if (this.mSS.getDataRoaming() || !mFinalMtkNewSS.getDataRoaming()) {
                        hasVoiceRoamingTypeChange2 = hasVoiceRoamingTypeChange;
                    } else {
                        hasVoiceRoamingTypeChange2 = hasVoiceRoamingTypeChange;
                        if (this.mSS.getDataRoamingType() != mFinalMtkNewSS.getDataRoamingType()) {
                            hasDataRoamingTypeChange = true;
                            if ((this.mSS.getOperatorNumeric() == null || mFinalMtkNewSS.getOperatorNumeric() != null) && ((this.mSS.getOperatorNumeric() != null && mFinalMtkNewSS.getOperatorNumeric() == null) || ((this.mSS.getOperatorNumeric() == null && mFinalMtkNewSS.getOperatorNumeric() != null) || !this.mSS.getOperatorNumeric().equals(mFinalMtkNewSS.getOperatorNumeric())))) {
                                hasPlmnChange = true;
                            } else {
                                hasPlmnChange = false;
                            }
                            StringBuilder sb = new StringBuilder();
                            sb.append("pollStateDone: hasRegistered = ");
                            sb.append(hasRegistered2);
                            sb.append(" hasDeregistered = ");
                            sb.append(hasDeregistered);
                            sb.append(" hasDataAttached = ");
                            sb.append(hasDataAttached);
                            sb.append(" hasDataDetached = ");
                            sb.append(hasDataDetached2);
                            sb.append(" hasDataRegStateChanged = ");
                            sb.append(hasDataRegStateChanged);
                            sb.append(" hasRilVoiceRadioTechnologyChanged = ");
                            sb.append(hasRilVoiceRadioTechnologyChanged);
                            sb.append(" hasRilDataRadioTechnologyChanged = ");
                            sb.append(hasRilDataRadioTechnologyChanged);
                            sb.append(" hasChanged = ");
                            sb.append(hasChanged2);
                            sb.append(" hasVoiceRoamingOn = ");
                            sb.append(hasVoiceRoamingOn2);
                            sb.append(" hasVoiceRoamingOff = ");
                            sb.append(hasVoiceRoamingOff2);
                            sb.append(" hasDataRoamingOn =");
                            sb.append(hasNrStateChanged2);
                            sb.append(" hasDataRoamingOff = ");
                            sb.append(hasDataRoamingOff);
                            sb.append(" hasLocationChanged = ");
                            sb.append(hasLocationChanged);
                            hasChanged = hasChanged2;
                            sb.append(" has4gHandoff = ");
                            sb.append(has4gHandoff2);
                            sb.append(" hasMultiApnSupport = ");
                            sb.append(hasMultiApnSupport);
                            sb.append(" hasLostMultiApnSupport = ");
                            sb.append(hasLostMultiApnSupport);
                            sb.append(" hasCssIndicatorChanged = ");
                            sb.append(hasCssIndicatorChanged);
                            sb.append(" hasNrFrequencyRangeChanged = ");
                            sb.append(hasNrFrequencyRangeChanged);
                            sb.append(" hasNrStateChanged = ");
                            sb.append(hasNrStateChanged);
                            sb.append(" hasVoiceRoamingTypeChange = ");
                            sb.append(hasVoiceRoamingTypeChange2);
                            sb.append(" hasDataRoamingTypeChange = ");
                            sb.append(hasDataRoamingTypeChange);
                            sb.append(" hasAirplaneModeOnlChanged = ");
                            sb.append(hasAirplaneModeOnChanged);
                            sb.append(" hasPlmnChange = ");
                            sb.append(hasPlmnChange);
                            log(sb.toString());
                            if (!hasVoiceRegStateChanged || anyDataRegChanged) {
                                if (this.mPhone.isPhoneTypeGsm()) {
                                    i2 = 50114;
                                } else {
                                    i2 = 50116;
                                }
                                hasDataRoamingTypeChange2 = hasDataRoamingTypeChange;
                                hasVoiceRoamingOff = hasVoiceRoamingOff2;
                                EventLog.writeEvent(i2, Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState()));
                            } else {
                                hasDataRoamingTypeChange2 = hasDataRoamingTypeChange;
                                hasVoiceRoamingOff = hasVoiceRoamingOff2;
                            }
                            if (OemConstant.EXP_VERSION || OemConstant.RM_VERSION || !MtkServiceStateTracker.super.isOppoRegionLockedState(this.mNewSS, this.mNewCellIdentity, hasLocationChanged)) {
                                newOosFlag = 0;
                                if (this.mNewSS.getVoiceRegState() == 1 && !this.mNewSS.isEmergencyOnly()) {
                                    newOosFlag = 1;
                                }
                                if (newOosFlag != this.oosFlag) {
                                    SystemProperties.set("gsm.oppo.oos" + this.mPhone.getPhoneId(), newOosFlag == 1 ? "1" : "0");
                                    this.oosFlag = newOosFlag;
                                    log("newOosFlag " + newOosFlag);
                                }
                                if (this.mPhone.isPhoneTypeGsm()) {
                                    if (hasRilVoiceRadioTechnologyChanged) {
                                        int cid = getCidFromCellIdentity(this.mNewCellIdentity);
                                        EventLog.writeEvent(50123, Integer.valueOf(cid), Integer.valueOf(this.mSS.getRilVoiceRadioTechnology()), Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology()));
                                        log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
                                    }
                                    if (hasCssIndicatorChanged) {
                                        this.mPhone.notifyDataConnection();
                                    }
                                    this.mReasonDataDenied = this.mNewReasonDataDenied;
                                    this.mMaxDataCalls = this.mNewMaxDataCalls;
                                    this.mRejectCode = this.mNewRejectCode;
                                }
                                oldMergedSS = new ServiceState(this.mPhone.getServiceState());
                                int oldRilDataRadioTechnology = this.mSS.getRilDataRadioTechnology();
                                ServiceState tss = this.mSS;
                                this.mSS = this.mNewSS;
                                this.mNewSS = tss;
                                MtkServiceState mMtkSS2 = this.mSS;
                                MtkServiceState mtkServiceState = (MtkServiceState) this.mNewSS;
                                CellIdentity tempCellId = this.mCellIdentity;
                                this.mCellIdentity = this.mNewCellIdentity;
                                this.mNewCellIdentity = tempCellId;
                                if (hasRilVoiceRadioTechnologyChanged) {
                                    updatePhoneObject();
                                }
                                TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                                if (anyDataRatChanged) {
                                    hasVoiceRoamingOn = hasVoiceRoamingOn2;
                                    tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                                    hasDataDetached = hasDataDetached2;
                                    StatsLog.write(76, ServiceState.rilRadioTechnologyToNetworkType(this.mSS.getRilDataRadioTechnology()), this.mPhone.getPhoneId());
                                    if (this.mPhone.isPhoneTypeCdmaLte() && (oldRilDataRadioTechnology == 14 || this.mSS.getRilDataRadioTechnology() == 14)) {
                                        log("[CDMALTE]pollStateDone: update signal for RAT switch between diff group");
                                        sendMessage(obtainMessage(10));
                                    }
                                } else {
                                    hasDataDetached = hasDataDetached2;
                                    hasVoiceRoamingOn = hasVoiceRoamingOn2;
                                }
                                if (hasRegistered2) {
                                    this.mNetworkAttachedRegistrants.notifyRegistrants();
                                    this.mLastRegisteredPLMN = this.mSS.getOperatorNumeric();
                                    this.mNitzState.handleNetworkAvailable();
                                }
                                if (hasDeregistered) {
                                    this.mNetworkDetachedRegistrants.notifyRegistrants();
                                }
                                if (hasRejectCauseChanged) {
                                    setNotification(2001);
                                }
                                if (this.mPhone.isPhoneTypeCdmaLte() && this.mEriTriggeredPollState) {
                                    this.mEriTriggeredPollState = false;
                                    hasChanged = true;
                                }
                                if (!hasChanged) {
                                    if (!this.mOppoNeedNotify) {
                                        this.mSS = mFinalMtkNewSS;
                                        shouldLogAttachedChange = false;
                                        if (hasRegistered2 || hasDeregistered) {
                                            shouldLogAttachedChange = true;
                                        }
                                        if (has4gHandoff2) {
                                            ((RegistrantList) this.mAttachedRegistrants.get(1)).notifyRegistrants();
                                            shouldLogAttachedChange = true;
                                            this.mLastPSRegisteredPLMN = this.mSS.getOperatorNumeric();
                                        }
                                        if (hasRilVoiceRadioTechnologyChanged) {
                                            logRatChange();
                                            notifySignalStrength();
                                        }
                                        int[] availableTransports2 = this.mTransportManager.getAvailableTransports();
                                        length = availableTransports2.length;
                                        shouldLogRatChange = false;
                                        shouldLogAttachedChange2 = shouldLogAttachedChange;
                                        i = 0;
                                        while (i < length) {
                                            int transport2 = availableTransports2[i];
                                            if (hasRilDataRadioTechnologyChanged.get(transport2)) {
                                                shouldLogRatChange = true;
                                                notifySignalStrength();
                                            }
                                            if (hasDataRegStateChanged.get(transport2) || hasRilDataRadioTechnologyChanged.get(transport2)) {
                                                notifyDataRegStateRilRadioTechnologyChanged(transport2);
                                                hasRegistered = hasRegistered2;
                                                this.mPhone.notifyDataConnection();
                                            } else {
                                                hasRegistered = hasRegistered2;
                                            }
                                            if (hasDataAttached.get(transport2)) {
                                                shouldLogAttachedChange2 = true;
                                                if (this.mAttachedRegistrants.get(transport2) != null) {
                                                    ((RegistrantList) this.mAttachedRegistrants.get(transport2)).notifyRegistrants();
                                                }
                                            }
                                            if (hasDataDetached.get(transport2)) {
                                                if (this.mDetachedRegistrants.get(transport2) != null) {
                                                    ((RegistrantList) this.mDetachedRegistrants.get(transport2)).notifyRegistrants();
                                                }
                                                shouldLogAttachedChange2 = true;
                                            }
                                            i++;
                                            hasDataDetached = hasDataDetached;
                                            hasPlmnChange = hasPlmnChange;
                                            hasRegistered2 = hasRegistered;
                                        }
                                        if (shouldLogAttachedChange2) {
                                            logAttachChange();
                                        }
                                        if (shouldLogRatChange) {
                                            logRatChange();
                                        }
                                        if (hasVoiceRegStateChanged || hasRilVoiceRadioTechnologyChanged) {
                                            notifyVoiceRegStateRilRadioTechnologyChanged();
                                        }
                                        this.mPsRegState = this.mSS.getDataRegState();
                                        if (hasVoiceRoamingOn || hasVoiceRoamingOff || hasNrStateChanged2 || hasDataRoamingOff) {
                                            logRoamingChange();
                                        }
                                        if (hasVoiceRoamingOn) {
                                            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                                        }
                                        if (hasVoiceRoamingOff) {
                                            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                                        }
                                        if (hasNrStateChanged2) {
                                            this.mDataRoamingOnRegistrants.notifyRegistrants();
                                        }
                                        if (hasDataRoamingOff) {
                                            this.mDataRoamingOffRegistrants.notifyRegistrants();
                                        }
                                        if (hasDataRoamingTypeChange2) {
                                            log("notify roaming type change.");
                                            this.mDataRoamingTypeChangedRegistrants.notifyRegistrants();
                                        }
                                        tm.setNetworkRoamingTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoamingType());
                                        if (mMtkSS2.getCellularRegState() == 0 && ((str = this.mLocatedPlmn) == null || !str.equals(mMtkSS2.getOperatorNumeric()))) {
                                            updateLocatedPlmn(mMtkSS2.getOperatorNumeric());
                                        }
                                        if (this.mPhone.getDcTracker(1).getPendingDataCallFlag()) {
                                            this.mPhone.getDcTracker(1).processPendingSetupData(this);
                                        }
                                        if (hasLocationChanged) {
                                            this.mPhone.notifyLocationChanged(getCellLocation());
                                        }
                                        if (this.mPhone.isPhoneTypeGsm()) {
                                            z = false;
                                        } else if (isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                                            z = false;
                                            this.mReportedGprsNoReg = false;
                                        } else if (this.mStartedGprsRegCheck || this.mReportedGprsNoReg) {
                                            z = false;
                                        } else {
                                            this.mStartedGprsRegCheck = true;
                                            sendMessageDelayed(obtainMessage(22), (long) Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", 60000));
                                            z = false;
                                        }
                                        if (this.hasPendingPollState) {
                                            this.hasPendingPollState = z;
                                            modemTriggeredPollState();
                                        }
                                        this.mNewSS.setStateOutOfService();
                                        if (!notifySignalStrength()) {
                                            log("PollStateDone with signal notification, level =" + this.mSignalStrength.getLevel());
                                            return;
                                        }
                                        return;
                                    }
                                }
                                this.mOppoNeedNotify = false;
                                updateSpnDisplay();
                                this.mForceBroadcastServiceState = false;
                                tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlpha());
                                operatorNumeric = this.mSS.getOperatorNumeric();
                                if (!this.mPhone.isPhoneTypeGsm() && isInvalidOperatorNumeric(operatorNumeric)) {
                                    operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getCdmaSystemId());
                                }
                                tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                                if (isInvalidOperatorNumeric(operatorNumeric)) {
                                    log("operatorNumeric " + operatorNumeric + " is invalid");
                                    this.mLocaleTracker.updateOperatorNumeric("");
                                } else if (!(this.mSS.getRilDataRadioTechnology() == 18 || mMtkSS2.getIwlanRegState() == 0)) {
                                    if (!this.mPhone.isPhoneTypeGsm()) {
                                        setOperatorIdd(operatorNumeric);
                                    }
                                    if (hasPlmnChange) {
                                        this.mLocaleTracker.updateOperatorNumeric(operatorNumeric);
                                        log("handleNetworkCountryCodeSet operatorNumeric " + operatorNumeric);
                                        this.mNitzState.handleNetworkCountryCodeSet(true);
                                    }
                                }
                                int phoneId = this.mPhone.getPhoneId();
                                if (this.mPhone.isPhoneTypeGsm()) {
                                    z2 = this.mSS.getVoiceRoaming();
                                } else {
                                    z2 = this.mSS.getVoiceRoaming() || this.mSS.getDataRoaming();
                                }
                                tm.setNetworkRoamingForPhone(phoneId, z2);
                                setRoamingType(this.mSS);
                                log("Broadcasting ServiceState : " + this.mSS);
                                if (!oldMergedSS.equals(this.mPhone.getServiceState())) {
                                    if (MtkServiceStateTracker.super.isNotNotifyMergeServiceOperator()) {
                                        this.mPhone.notifyServiceStateChanged(this.mSS);
                                    } else {
                                        this.mPhone.notifyServiceStateChanged(this.mPhone.getServiceState());
                                    }
                                }
                                this.mCi.setServiceStateToModem(this.mSS.getVoiceRegState(), mMtkSS2.getCellularDataRegState(), this.mSS.getVoiceRoamingType(), mMtkSS2.getCellularDataRoamingType(), mMtkSS2.getRilVoiceRegState(), mMtkSS2.getRilCellularDataRegState(), null);
                                this.mPhone.getContext().getContentResolver().insert(Telephony.ServiceStateTable.getUriForSubscriptionId(this.mPhone.getSubId()), Telephony.ServiceStateTable.getContentValuesForServiceState(this.mSS));
                                TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
                                if (has4gHandoff2) {
                                    log("CdmaLte has4gHandoff so the value of isGSM will change");
                                    this.mCi.getSignalStrength(obtainMessage(3));
                                }
                                shouldLogAttachedChange = false;
                                shouldLogAttachedChange = true;
                                if (has4gHandoff2) {
                                }
                                if (hasRilVoiceRadioTechnologyChanged) {
                                }
                                int[] availableTransports22 = this.mTransportManager.getAvailableTransports();
                                length = availableTransports22.length;
                                shouldLogRatChange = false;
                                shouldLogAttachedChange2 = shouldLogAttachedChange;
                                i = 0;
                                while (i < length) {
                                }
                                if (shouldLogAttachedChange2) {
                                }
                                if (shouldLogRatChange) {
                                }
                                notifyVoiceRegStateRilRadioTechnologyChanged();
                                this.mPsRegState = this.mSS.getDataRegState();
                                logRoamingChange();
                                if (hasVoiceRoamingOn) {
                                }
                                if (hasVoiceRoamingOff) {
                                }
                                if (hasNrStateChanged2) {
                                }
                                if (hasDataRoamingOff) {
                                }
                                if (hasDataRoamingTypeChange2) {
                                }
                                tm.setNetworkRoamingTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoamingType());
                                updateLocatedPlmn(mMtkSS2.getOperatorNumeric());
                                if (this.mPhone.getDcTracker(1).getPendingDataCallFlag()) {
                                }
                                if (hasLocationChanged) {
                                }
                                if (this.mPhone.isPhoneTypeGsm()) {
                                }
                                if (this.hasPendingPollState) {
                                }
                                this.mNewSS.setStateOutOfService();
                                if (!notifySignalStrength()) {
                                }
                            } else {
                                log("has region locked");
                                return;
                            }
                        }
                    }
                    hasDataRoamingTypeChange = false;
                    if (this.mSS.getOperatorNumeric() == null) {
                    }
                    hasPlmnChange = true;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("pollStateDone: hasRegistered = ");
                    sb2.append(hasRegistered2);
                    sb2.append(" hasDeregistered = ");
                    sb2.append(hasDeregistered);
                    sb2.append(" hasDataAttached = ");
                    sb2.append(hasDataAttached);
                    sb2.append(" hasDataDetached = ");
                    sb2.append(hasDataDetached2);
                    sb2.append(" hasDataRegStateChanged = ");
                    sb2.append(hasDataRegStateChanged);
                    sb2.append(" hasRilVoiceRadioTechnologyChanged = ");
                    sb2.append(hasRilVoiceRadioTechnologyChanged);
                    sb2.append(" hasRilDataRadioTechnologyChanged = ");
                    sb2.append(hasRilDataRadioTechnologyChanged);
                    sb2.append(" hasChanged = ");
                    sb2.append(hasChanged2);
                    sb2.append(" hasVoiceRoamingOn = ");
                    sb2.append(hasVoiceRoamingOn2);
                    sb2.append(" hasVoiceRoamingOff = ");
                    sb2.append(hasVoiceRoamingOff2);
                    sb2.append(" hasDataRoamingOn =");
                    sb2.append(hasNrStateChanged2);
                    sb2.append(" hasDataRoamingOff = ");
                    sb2.append(hasDataRoamingOff);
                    sb2.append(" hasLocationChanged = ");
                    sb2.append(hasLocationChanged);
                    hasChanged = hasChanged2;
                    sb2.append(" has4gHandoff = ");
                    sb2.append(has4gHandoff2);
                    sb2.append(" hasMultiApnSupport = ");
                    sb2.append(hasMultiApnSupport);
                    sb2.append(" hasLostMultiApnSupport = ");
                    sb2.append(hasLostMultiApnSupport);
                    sb2.append(" hasCssIndicatorChanged = ");
                    sb2.append(hasCssIndicatorChanged);
                    sb2.append(" hasNrFrequencyRangeChanged = ");
                    sb2.append(hasNrFrequencyRangeChanged);
                    sb2.append(" hasNrStateChanged = ");
                    sb2.append(hasNrStateChanged);
                    sb2.append(" hasVoiceRoamingTypeChange = ");
                    sb2.append(hasVoiceRoamingTypeChange2);
                    sb2.append(" hasDataRoamingTypeChange = ");
                    sb2.append(hasDataRoamingTypeChange);
                    sb2.append(" hasAirplaneModeOnlChanged = ");
                    sb2.append(hasAirplaneModeOnChanged);
                    sb2.append(" hasPlmnChange = ");
                    sb2.append(hasPlmnChange);
                    log(sb2.toString());
                    if (!hasVoiceRegStateChanged) {
                    }
                    if (this.mPhone.isPhoneTypeGsm()) {
                    }
                    hasDataRoamingTypeChange2 = hasDataRoamingTypeChange;
                    hasVoiceRoamingOff = hasVoiceRoamingOff2;
                    EventLog.writeEvent(i2, Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState()));
                    if (OemConstant.EXP_VERSION) {
                    }
                    newOosFlag = 0;
                    newOosFlag = 1;
                    if (newOosFlag != this.oosFlag) {
                    }
                    if (this.mPhone.isPhoneTypeGsm()) {
                    }
                    oldMergedSS = new ServiceState(this.mPhone.getServiceState());
                    int oldRilDataRadioTechnology2 = this.mSS.getRilDataRadioTechnology();
                    ServiceState tss2 = this.mSS;
                    this.mSS = this.mNewSS;
                    this.mNewSS = tss2;
                    MtkServiceState mMtkSS22 = this.mSS;
                    MtkServiceState mtkServiceState2 = (MtkServiceState) this.mNewSS;
                    CellIdentity tempCellId2 = this.mCellIdentity;
                    this.mCellIdentity = this.mNewCellIdentity;
                    this.mNewCellIdentity = tempCellId2;
                    if (hasRilVoiceRadioTechnologyChanged) {
                    }
                    TelephonyManager tm2 = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                    if (anyDataRatChanged) {
                    }
                    if (hasRegistered2) {
                    }
                    if (hasDeregistered) {
                    }
                    if (hasRejectCauseChanged) {
                    }
                    this.mEriTriggeredPollState = false;
                    hasChanged = true;
                    if (!hasChanged) {
                    }
                    this.mOppoNeedNotify = false;
                    updateSpnDisplay();
                    this.mForceBroadcastServiceState = false;
                    tm2.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlpha());
                    operatorNumeric = this.mSS.getOperatorNumeric();
                    operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getCdmaSystemId());
                    tm2.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                    if (isInvalidOperatorNumeric(operatorNumeric)) {
                    }
                    int phoneId2 = this.mPhone.getPhoneId();
                    if (this.mPhone.isPhoneTypeGsm()) {
                    }
                    tm2.setNetworkRoamingForPhone(phoneId2, z2);
                    setRoamingType(this.mSS);
                    log("Broadcasting ServiceState : " + this.mSS);
                    if (!oldMergedSS.equals(this.mPhone.getServiceState())) {
                    }
                    this.mCi.setServiceStateToModem(this.mSS.getVoiceRegState(), mMtkSS22.getCellularDataRegState(), this.mSS.getVoiceRoamingType(), mMtkSS22.getCellularDataRoamingType(), mMtkSS22.getRilVoiceRegState(), mMtkSS22.getRilCellularDataRegState(), null);
                    this.mPhone.getContext().getContentResolver().insert(Telephony.ServiceStateTable.getUriForSubscriptionId(this.mPhone.getSubId()), Telephony.ServiceStateTable.getContentValuesForServiceState(this.mSS));
                    TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
                    if (has4gHandoff2) {
                    }
                    shouldLogAttachedChange = false;
                    shouldLogAttachedChange = true;
                    if (has4gHandoff2) {
                    }
                    if (hasRilVoiceRadioTechnologyChanged) {
                    }
                    int[] availableTransports222 = this.mTransportManager.getAvailableTransports();
                    length = availableTransports222.length;
                    shouldLogRatChange = false;
                    shouldLogAttachedChange2 = shouldLogAttachedChange;
                    i = 0;
                    while (i < length) {
                    }
                    if (shouldLogAttachedChange2) {
                    }
                    if (shouldLogRatChange) {
                    }
                    notifyVoiceRegStateRilRadioTechnologyChanged();
                    this.mPsRegState = this.mSS.getDataRegState();
                    logRoamingChange();
                    if (hasVoiceRoamingOn) {
                    }
                    if (hasVoiceRoamingOff) {
                    }
                    if (hasNrStateChanged2) {
                    }
                    if (hasDataRoamingOff) {
                    }
                    if (hasDataRoamingTypeChange2) {
                    }
                    tm2.setNetworkRoamingTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoamingType());
                    updateLocatedPlmn(mMtkSS22.getOperatorNumeric());
                    if (this.mPhone.getDcTracker(1).getPendingDataCallFlag()) {
                    }
                    if (hasLocationChanged) {
                    }
                    if (this.mPhone.isPhoneTypeGsm()) {
                    }
                    if (this.hasPendingPollState) {
                    }
                    this.mNewSS.setStateOutOfService();
                    if (!notifySignalStrength()) {
                    }
                }
            }
            hasVoiceRoamingTypeChange = false;
            if (this.mSS.getDataRoaming()) {
            }
            hasVoiceRoamingTypeChange2 = hasVoiceRoamingTypeChange;
            hasDataRoamingTypeChange = false;
            if (this.mSS.getOperatorNumeric() == null) {
            }
            hasPlmnChange = true;
            StringBuilder sb22 = new StringBuilder();
            sb22.append("pollStateDone: hasRegistered = ");
            sb22.append(hasRegistered2);
            sb22.append(" hasDeregistered = ");
            sb22.append(hasDeregistered);
            sb22.append(" hasDataAttached = ");
            sb22.append(hasDataAttached);
            sb22.append(" hasDataDetached = ");
            sb22.append(hasDataDetached2);
            sb22.append(" hasDataRegStateChanged = ");
            sb22.append(hasDataRegStateChanged);
            sb22.append(" hasRilVoiceRadioTechnologyChanged = ");
            sb22.append(hasRilVoiceRadioTechnologyChanged);
            sb22.append(" hasRilDataRadioTechnologyChanged = ");
            sb22.append(hasRilDataRadioTechnologyChanged);
            sb22.append(" hasChanged = ");
            sb22.append(hasChanged2);
            sb22.append(" hasVoiceRoamingOn = ");
            sb22.append(hasVoiceRoamingOn2);
            sb22.append(" hasVoiceRoamingOff = ");
            sb22.append(hasVoiceRoamingOff2);
            sb22.append(" hasDataRoamingOn =");
            sb22.append(hasNrStateChanged2);
            sb22.append(" hasDataRoamingOff = ");
            sb22.append(hasDataRoamingOff);
            sb22.append(" hasLocationChanged = ");
            sb22.append(hasLocationChanged);
            hasChanged = hasChanged2;
            sb22.append(" has4gHandoff = ");
            sb22.append(has4gHandoff2);
            sb22.append(" hasMultiApnSupport = ");
            sb22.append(hasMultiApnSupport);
            sb22.append(" hasLostMultiApnSupport = ");
            sb22.append(hasLostMultiApnSupport);
            sb22.append(" hasCssIndicatorChanged = ");
            sb22.append(hasCssIndicatorChanged);
            sb22.append(" hasNrFrequencyRangeChanged = ");
            sb22.append(hasNrFrequencyRangeChanged);
            sb22.append(" hasNrStateChanged = ");
            sb22.append(hasNrStateChanged);
            sb22.append(" hasVoiceRoamingTypeChange = ");
            sb22.append(hasVoiceRoamingTypeChange2);
            sb22.append(" hasDataRoamingTypeChange = ");
            sb22.append(hasDataRoamingTypeChange);
            sb22.append(" hasAirplaneModeOnlChanged = ");
            sb22.append(hasAirplaneModeOnChanged);
            sb22.append(" hasPlmnChange = ");
            sb22.append(hasPlmnChange);
            log(sb22.toString());
            if (!hasVoiceRegStateChanged) {
            }
            if (this.mPhone.isPhoneTypeGsm()) {
            }
            hasDataRoamingTypeChange2 = hasDataRoamingTypeChange;
            hasVoiceRoamingOff = hasVoiceRoamingOff2;
            EventLog.writeEvent(i2, Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState()));
            if (OemConstant.EXP_VERSION) {
            }
            newOosFlag = 0;
            newOosFlag = 1;
            if (newOosFlag != this.oosFlag) {
            }
            if (this.mPhone.isPhoneTypeGsm()) {
            }
            oldMergedSS = new ServiceState(this.mPhone.getServiceState());
            int oldRilDataRadioTechnology22 = this.mSS.getRilDataRadioTechnology();
            ServiceState tss22 = this.mSS;
            this.mSS = this.mNewSS;
            this.mNewSS = tss22;
            MtkServiceState mMtkSS222 = this.mSS;
            MtkServiceState mtkServiceState22 = (MtkServiceState) this.mNewSS;
            CellIdentity tempCellId22 = this.mCellIdentity;
            this.mCellIdentity = this.mNewCellIdentity;
            this.mNewCellIdentity = tempCellId22;
            if (hasRilVoiceRadioTechnologyChanged) {
            }
            TelephonyManager tm22 = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
            if (anyDataRatChanged) {
            }
            if (hasRegistered2) {
            }
            if (hasDeregistered) {
            }
            if (hasRejectCauseChanged) {
            }
            this.mEriTriggeredPollState = false;
            hasChanged = true;
            if (!hasChanged) {
            }
            this.mOppoNeedNotify = false;
            updateSpnDisplay();
            this.mForceBroadcastServiceState = false;
            tm22.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlpha());
            operatorNumeric = this.mSS.getOperatorNumeric();
            operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getCdmaSystemId());
            tm22.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            if (isInvalidOperatorNumeric(operatorNumeric)) {
            }
            int phoneId22 = this.mPhone.getPhoneId();
            if (this.mPhone.isPhoneTypeGsm()) {
            }
            tm22.setNetworkRoamingForPhone(phoneId22, z2);
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            if (!oldMergedSS.equals(this.mPhone.getServiceState())) {
            }
            this.mCi.setServiceStateToModem(this.mSS.getVoiceRegState(), mMtkSS222.getCellularDataRegState(), this.mSS.getVoiceRoamingType(), mMtkSS222.getCellularDataRoamingType(), mMtkSS222.getRilVoiceRegState(), mMtkSS222.getRilCellularDataRegState(), null);
            this.mPhone.getContext().getContentResolver().insert(Telephony.ServiceStateTable.getUriForSubscriptionId(this.mPhone.getSubId()), Telephony.ServiceStateTable.getContentValuesForServiceState(this.mSS));
            TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
            if (has4gHandoff2) {
            }
            shouldLogAttachedChange = false;
            shouldLogAttachedChange = true;
            if (has4gHandoff2) {
            }
            if (hasRilVoiceRadioTechnologyChanged) {
            }
            int[] availableTransports2222 = this.mTransportManager.getAvailableTransports();
            length = availableTransports2222.length;
            shouldLogRatChange = false;
            shouldLogAttachedChange2 = shouldLogAttachedChange;
            i = 0;
            while (i < length) {
            }
            if (shouldLogAttachedChange2) {
            }
            if (shouldLogRatChange) {
            }
            notifyVoiceRegStateRilRadioTechnologyChanged();
            this.mPsRegState = this.mSS.getDataRegState();
            logRoamingChange();
            if (hasVoiceRoamingOn) {
            }
            if (hasVoiceRoamingOff) {
            }
            if (hasNrStateChanged2) {
            }
            if (hasDataRoamingOff) {
            }
            if (hasDataRoamingTypeChange2) {
            }
            tm22.setNetworkRoamingTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoamingType());
            updateLocatedPlmn(mMtkSS222.getOperatorNumeric());
            if (this.mPhone.getDcTracker(1).getPendingDataCallFlag()) {
            }
            if (hasLocationChanged) {
            }
            if (this.mPhone.isPhoneTypeGsm()) {
            }
            if (this.hasPendingPollState) {
            }
            this.mNewSS.setStateOutOfService();
            if (!notifySignalStrength()) {
            }
        } else if (this.hasPendingPollState) {
            this.hasPendingPollState = false;
            modemTriggeredPollState();
        }
    }

    private final boolean isConcurrentVoiceAndDataAllowedForIwlan() {
        if (this.mSS.getDataRegState() == 0 && this.mSS.getRilDataRadioTechnology() == 18 && getImsServiceState() == 0) {
            return true;
        }
        return false;
    }

    public boolean isConcurrentVoiceAndDataAllowed() {
        if (this.mSS.getCssIndicator() == 1) {
            return true;
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            if (isConcurrentVoiceAndDataAllowedForVolte() || isConcurrentVoiceAndDataAllowedForIwlan()) {
                return true;
            }
            if (this.mSS.getRilVoiceRadioTechnology() == 16 || this.mSS.getRilDataRadioTechnology() == 16) {
                log("isConcurrentVoiceAndDataAllowedmSS.getRilDataRadioTechnology() = " + this.mSS.getRilDataRadioTechnology());
                return false;
            } else if (this.mSS.getRilDataRadioTechnology() >= 3) {
                return true;
            } else {
                return false;
            }
        } else if (this.mPhone.isPhoneTypeCdma()) {
            return false;
        } else {
            if ((SystemProperties.getInt("ro.vendor.mtk_c2k_lte_mode", 0) == 1 && this.mSS.getRilDataRadioTechnology() == 14) || isConcurrentVoiceAndDataAllowedForVolte() || this.mSS.getCssIndicator() == 1) {
                return true;
            }
            return false;
        }
    }

    private int calculateDeviceRatMode(int phoneId) {
        if (this.mPhone.isPhoneTypeGsm()) {
            try {
                if (this.mServiceStateTrackerExt.isSupportRatBalancing()) {
                    log("networkType is controlled by RAT Blancing, no need to set network type");
                    return -1;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        PhoneFactory.calculatePreferredNetworkType(this.mPhone.getContext(), this.mPhone.getSubId());
        int networkType = OppoTelephonyFactory.getInstance().getFeature(IOppoNetworkManager.DEFAULT, new Object[0]).calculatePreferredNetworkTypeWithPhoneId(this.mPhone.getContext(), this.mPhone.getSubId(), phoneId);
        log("calculateDeviceRatMode=" + networkType);
        return networkType;
    }

    public void setDeviceRatMode(int phoneId) {
        if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
            int networkType = calculateDeviceRatMode(phoneId);
            if (networkType >= 0) {
                this.mPhone.setPreferredNetworkType(networkType, (Message) null);
                return;
            }
            return;
        }
        log("Invalid subId, skip setDeviceRatMode!");
    }

    public boolean willLocatedPlmnChange() {
        MtkServiceState mMtkSS = this.mSS;
        if (mMtkSS == null || this.mLocatedPlmn == null || mMtkSS.getCellularRegState() != 0 || this.mSS.getOperatorNumeric().equals(this.mLocatedPlmn)) {
            return false;
        }
        return true;
    }

    public String getLocatedPlmn() {
        return this.mLocatedPlmn;
    }

    private void updateLocatedPlmn(String plmn) {
        String str;
        if ((this.mLocatedPlmn == null && plmn != null) || ((this.mLocatedPlmn != null && plmn == null) || !((str = this.mLocatedPlmn) == null || plmn == null || str.equals(plmn)))) {
            log("updateLocatedPlmn(),previous plmn= " + this.mLocatedPlmn + " ,update to: " + plmn);
            Intent intent = new Intent("com.mediatek.intent.action.LOCATED_PLMN_CHANGED");
            if (TelephonyManager.getDefault().getPhoneCount() == 1) {
                intent.addFlags(536870912);
            }
            intent.putExtra("plmn", plmn);
            if (plmn != null) {
                try {
                    intent.putExtra("iso", MccTable.countryCodeForMcc(Integer.parseInt(plmn.substring(0, 3))));
                } catch (NumberFormatException ex) {
                    loge("updateLocatedPlmn: countryCodeForMcc error" + ex);
                    intent.putExtra("iso", "");
                } catch (StringIndexOutOfBoundsException ex2) {
                    loge("updateLocatedPlmn: countryCodeForMcc error" + ex2);
                    intent.putExtra("iso", "");
                } catch (Exception e) {
                    loge("updateLocatedPlmn: countryCodeForMcc error" + e);
                    intent.putExtra("iso", "");
                }
                this.mLocatedPlmn = plmn;
            } else {
                intent.putExtra("iso", "");
            }
            broadcastMccChange(plmn);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
        this.mLocatedPlmn = plmn;
    }

    /* access modifiers changed from: protected */
    public void refreshSpn(ServiceState ss, CellLocation cellLoc, boolean fromPollState) {
        String strOperatorShort;
        String strOperatorLong;
        String brandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
        if (brandOverride != null) {
            log("refreshSpn: use brandOverride" + brandOverride);
            strOperatorLong = brandOverride;
            strOperatorShort = brandOverride;
        } else {
            int lac = -1;
            if (cellLoc instanceof GsmCellLocation) {
                lac = ((GsmCellLocation) cellLoc).getLac();
            } else {
                boolean z = cellLoc instanceof CdmaCellLocation;
            }
            strOperatorLong = this.mCi.lookupOperatorName(this.mPhone.getSubId(), ss.getOperatorNumeric(), true, lac);
            strOperatorShort = this.mCi.lookupOperatorName(this.mPhone.getSubId(), ss.getOperatorNumeric(), false, lac);
        }
        if (!TextUtils.equals(strOperatorLong, ss.getOperatorAlphaLong()) || !TextUtils.equals(strOperatorShort, ss.getOperatorAlphaShort())) {
            this.mForceBroadcastServiceState = true;
            if (fromPollState) {
                ss.setOperatorName(strOperatorLong, strOperatorShort, ss.getOperatorNumeric());
            }
        }
        log("refreshSpn: " + strOperatorLong + ", " + strOperatorShort + ", fromPollState=" + fromPollState + ", mForceBroadcastServiceState=" + this.mForceBroadcastServiceState);
    }

    /* access modifiers changed from: protected */
    public void setPowerStateToDesired() {
        String tmpLog = "mDeviceShuttingDown=" + this.mDeviceShuttingDown + ", mDesiredPowerState=" + this.mDesiredPowerState + ", getRadioState=" + this.mCi.getRadioState() + ", mPowerOffDelayNeed=" + this.mPowerOffDelayNeed + ", mAlarmSwitch=" + this.mAlarmSwitch + ", mRadioDisabledByCarrier=" + this.mRadioDisabledByCarrier;
        log(tmpLog);
        this.mRadioPowerLog.log(tmpLog);
        if (this.mPhone.isPhoneTypeGsm() && this.mAlarmSwitch) {
            log("mAlarmSwitch == true");
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
        }
        if (this.mDesiredPowerState && !this.mRadioDisabledByCarrier && this.mCi.getRadioState() == 0) {
            if (this.mPhone.isPhoneTypeGsm()) {
                setDeviceRatMode(this.mPhone.getPhoneId());
            }
            RadioManager.getInstance();
            RadioManager.sendRequestBeforeSetRadioPower(true, this.mPhone.getPhoneId());
            this.mCi.setRadioPower(true, (Message) null);
        } else if ((this.mDesiredPowerState && !this.mRadioDisabledByCarrier) || this.mCi.getRadioState() != 1) {
        } else {
            if (!this.mPhone.isPhoneTypeGsm() || !this.mPowerOffDelayNeed) {
                powerOffRadioSafely();
            } else if (!this.mImsRegistrationOnOff || this.mAlarmSwitch) {
                powerOffRadioSafely();
            } else {
                log("mImsRegistrationOnOff == true");
                Context context = this.mPhone.getContext();
                this.mRadioOffIntent = PendingIntent.getBroadcast(context, 0, new Intent("android.intent.action.ACTION_RADIO_OFF"), 0);
                this.mAlarmSwitch = true;
                log("Alarm setting");
                ((AlarmManager) context.getSystemService("alarm")).set(2, SystemClock.elapsedRealtime() + 3000, this.mRadioOffIntent);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void hangupAndPowerOff() {
        if (!this.mPhone.isPhoneTypeGsm() || this.mPhone.isInCall()) {
            this.mPhone.mCT.mRingingCall.hangupIfAlive();
            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
        }
        hangupAllImsCall();
        RadioManager.getInstance();
        RadioManager.sendRequestBeforeSetRadioPower(false, this.mPhone.getPhoneId());
        this.mCi.setRadioPower(false, obtainMessage(54));
    }

    private void onFemtoCellInfoResult(AsyncResult ar) {
        int isCsgCell = 0;
        try {
            if (ar.exception == null) {
                if (ar.result != null) {
                    String[] info = (String[]) ar.result;
                    if (info.length > 0) {
                        if (info[0] != null && info[0].length() > 0) {
                            this.mFemtocellDomain = Integer.parseInt(info[0]);
                            log("onFemtoCellInfo: mFemtocellDomain set to " + this.mFemtocellDomain);
                        }
                        if (info[5] != null && info[5].length() > 0) {
                            isCsgCell = Integer.parseInt(info[5]);
                        }
                        this.mIsFemtocell = isCsgCell;
                        log("onFemtoCellInfo: domain= " + this.mFemtocellDomain + ",isCsgCell= " + isCsgCell);
                        if (isCsgCell == 1) {
                            if (info[6] != null && info[6].length() > 0) {
                                this.mCsgId = info[6];
                                log("onFemtoCellInfo: mCsgId set to " + this.mCsgId);
                            }
                            if (info[8] == null || info[8].length() <= 0) {
                                this.mHhbName = null;
                                log("onFemtoCellInfo: mHhbName is not available ,set to null");
                            } else {
                                this.mHhbName = new String(IccUtils.hexStringToBytes(info[8]));
                                log("onFemtoCellInfo: mHhbName set from " + info[8] + " to " + this.mHhbName);
                            }
                        } else {
                            this.mCsgId = null;
                            this.mHhbName = null;
                            log("onFemtoCellInfo: csgId and hnbName are cleared");
                        }
                        if (!(isCsgCell == 2 || info[1] == null || info[1].length() <= 0 || info[9] == null || info[0].length() <= 0)) {
                            int state = Integer.parseInt(info[1]);
                            int cause = Integer.parseInt(info[9]);
                            try {
                                if (this.mServiceStateTrackerExt.needIgnoreFemtocellUpdate(state, cause)) {
                                    log("needIgnoreFemtocellUpdate due to state= " + state + ",cause= " + cause);
                                    return;
                                }
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                            }
                        }
                        Intent intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
                        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
                        if (TelephonyManager.getDefault().getPhoneCount() == 1) {
                            intent.addFlags(536870912);
                        }
                        intent.putExtra("showSpn", this.mCurShowSpn);
                        intent.putExtra("spn", this.mCurSpn);
                        intent.putExtra("showPlmn", this.mCurShowPlmn);
                        intent.putExtra("plmn", this.mCurPlmn);
                        intent.putExtra("hnbName", this.mHhbName);
                        intent.putExtra("csgId", this.mCsgId);
                        intent.putExtra("domain", this.mFemtocellDomain);
                        intent.putExtra("femtocell", this.mIsFemtocell);
                        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                        int phoneId = this.mPhone.getPhoneId();
                        String plmn = this.mCurPlmn;
                        if (this.mHhbName == null && this.mCsgId != null) {
                            try {
                                if (this.mServiceStateTrackerExt.needToShowCsgId()) {
                                    plmn = (plmn + " - ") + this.mCsgId;
                                }
                            } catch (RuntimeException e2) {
                                e2.printStackTrace();
                            }
                        } else if (this.mHhbName != null) {
                            plmn = (plmn + " - ") + this.mHhbName;
                        }
                        if (!this.mSubscriptionController.setPlmnSpn(phoneId, this.mCurShowPlmn, plmn, this.mCurShowSpn, this.mCurSpn)) {
                            this.mSpnUpdatePending = true;
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
            loge("onFemtoCellInfo exception");
        } catch (NumberFormatException e3) {
            Rlog.d(LOG_TAG, e3.toString());
        } catch (Exception e4) {
            Rlog.d(LOG_TAG, e4.toString());
        }
    }

    private void onInvalidSimInfoReceived(AsyncResult ar) {
        try {
            String[] InvalidSimInfo = (String[]) ar.result;
            String plmn = InvalidSimInfo[0];
            int cs_invalid = Integer.parseInt(InvalidSimInfo[1]);
            int ps_invalid = Integer.parseInt(InvalidSimInfo[2]);
            int cause = Integer.parseInt(InvalidSimInfo[3]);
            int testMode = SystemProperties.getInt("vendor.gsm.gcf.testmode", 0);
            log("onInvalidSimInfoReceived testMode:" + testMode + " cause:" + cause + " cs_invalid:" + cs_invalid + " ps_invalid:" + ps_invalid + " plmn:" + plmn + " mEverIVSR:" + this.mEverIVSR);
            if (testMode != 0) {
                log("InvalidSimInfo received during test mode: " + testMode);
            } else if (this.mServiceStateTrackerExt.isNeedDisableIVSR()) {
                log("Disable IVSR");
            } else {
                if (cs_invalid == 1) {
                    this.isCsInvalidCard = true;
                }
                if (this.mMtkVoiceCapable && cs_invalid == 1 && this.mLastRegisteredPLMN != null && plmn.equals(this.mLastRegisteredPLMN)) {
                    log("InvalidSimInfo reset SIM due to CS invalid");
                    setEverIVSR(true);
                    this.mLastRegisteredPLMN = null;
                    this.mLastPSRegisteredPLMN = null;
                    this.mCi.setSimPower(2, null);
                } else if (ps_invalid == 1 && isAllowRecoveryOnIvsr(ar) && this.mLastPSRegisteredPLMN != null && plmn.equals(this.mLastPSRegisteredPLMN)) {
                    log("InvalidSimInfo reset SIM due to PS invalid ");
                    setEverIVSR(true);
                    this.mLastRegisteredPLMN = null;
                    this.mLastPSRegisteredPLMN = null;
                    this.mCi.setSimPower(2, null);
                }
            }
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
        }
    }

    private void onNetworkEventReceived(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("onNetworkEventReceived exception");
            return;
        }
        int nwEventType = ((int[]) ar.result)[1];
        log("[onNetworkEventReceived] event_type:" + nwEventType);
        Intent intent = new Intent("com.mediatek.intent.action.ACTION_NETWORK_EVENT");
        intent.addFlags(536870912);
        intent.putExtra("eventType", nwEventType + 1);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void onModulationInfoReceived(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("onModulationInfoReceived exception");
            return;
        }
        int modulation = ((int[]) ar.result)[0];
        log("[onModulationInfoReceived] modulation:" + modulation);
        Intent intent = new Intent("com.mediatek.intent.action.ACTION_NOTIFY_MODULATION_INFO");
        intent.addFlags(536870912);
        intent.putExtra("modulation_info", modulation);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean isAllowRecoveryOnIvsr(AsyncResult ar) {
        if (this.mPhone.isInCall()) {
            log("[isAllowRecoveryOnIvsr] isInCall()=true");
            Message msg = obtainMessage();
            msg.what = 101;
            msg.obj = ar;
            sendMessageDelayed(msg, 10000);
            return false;
        }
        log("isAllowRecoveryOnIvsr() return true");
        return true;
    }

    private void setEverIVSR(boolean value) {
        log("setEverIVSR:" + value);
        this.mEverIVSR = value;
        if (value) {
            Intent intent = new Intent("com.mediatek.intent.action.IVSR_NOTIFY");
            intent.putExtra("action", "start");
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            if (TelephonyManager.getDefault().getPhoneCount() == 1) {
                intent.addFlags(536870912);
            }
            log("broadcast ACTION_IVSR_NOTIFY intent");
            this.mPhone.getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void setNullState() {
        this.isCsInvalidCard = false;
    }

    /* access modifiers changed from: protected */
    public final boolean IsInternationalRoamingException(String operatorNumeric) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configManager == null) {
            Rlog.e(LOG_TAG, "Carrier config service is not available");
            return false;
        }
        PersistableBundle b = configManager.getConfigForSubId(this.mPhone.getSubId());
        if (b == null) {
            Rlog.e(LOG_TAG, "Can't get the config. subId = " + this.mPhone.getSubId());
            return false;
        }
        String[] operatorRoamingException = b.getStringArray("carrier_international_roaming_exception_list_strings");
        if (operatorRoamingException == null) {
            Rlog.e(LOG_TAG, "carrier_international_roaming_exception_list_strings is not available. subId = " + this.mPhone.getSubId());
            return false;
        }
        HashSet<String> internationalRoamingSet = new HashSet<>(Arrays.asList(operatorRoamingException));
        Rlog.d(LOG_TAG, "For subId = " + this.mPhone.getSubId() + ", international roaming exceptions are " + Arrays.toString(internationalRoamingSet.toArray()) + ", operatorNumeric = " + operatorNumeric);
        if (internationalRoamingSet.contains(operatorNumeric)) {
            Rlog.d(LOG_TAG, operatorNumeric + " in list.");
            return true;
        }
        Rlog.d(LOG_TAG, operatorNumeric + " is not in list.");
        return false;
    }

    /* access modifiers changed from: protected */
    public void setRoamingType(ServiceState currentServiceState) {
        boolean isVoiceInService = currentServiceState.getVoiceRegState() == 0;
        if (isVoiceInService) {
            if (!currentServiceState.getVoiceRoaming()) {
                currentServiceState.setVoiceRoamingType(0);
            } else if (this.mPhone.isPhoneTypeGsm()) {
                if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                    currentServiceState.setVoiceRoamingType(2);
                } else {
                    currentServiceState.setVoiceRoamingType(3);
                }
                if (IsInternationalRoamingException(currentServiceState.getVoiceOperatorNumeric())) {
                    log(currentServiceState.getVoiceOperatorNumeric() + " is in operator defined international roaming list");
                    currentServiceState.setVoiceRoamingType(3);
                }
            } else {
                int[] intRoamingIndicators = this.mPhone.getContext().getResources().getIntArray(17236002);
                if (intRoamingIndicators != null && intRoamingIndicators.length > 0) {
                    currentServiceState.setVoiceRoamingType(2);
                    int curRoamingIndicator = currentServiceState.getCdmaRoamingIndicator();
                    int i = 0;
                    while (true) {
                        if (i >= intRoamingIndicators.length) {
                            break;
                        } else if (curRoamingIndicator == intRoamingIndicators[i]) {
                            currentServiceState.setVoiceRoamingType(3);
                            break;
                        } else {
                            i++;
                        }
                    }
                } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                    currentServiceState.setVoiceRoamingType(2);
                } else {
                    currentServiceState.setVoiceRoamingType(3);
                }
            }
        }
        boolean isDataInService = currentServiceState.getDataRegState() == 0;
        int dataRegType = currentServiceState.getRilDataRadioTechnology();
        if (!isDataInService) {
            return;
        }
        if (!currentServiceState.getDataRoaming()) {
            currentServiceState.setDataRoamingType(0);
        } else if (this.mPhone.isPhoneTypeGsm()) {
            if (!ServiceState.isGsm(dataRegType)) {
                currentServiceState.setDataRoamingType(1);
            } else if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                    currentServiceState.setDataRoamingType(2);
                } else {
                    currentServiceState.setDataRoamingType(3);
                }
                if (IsInternationalRoamingException(currentServiceState.getVoiceOperatorNumeric())) {
                    log(currentServiceState.getVoiceOperatorNumeric() + " is in operator defined international roaming list");
                    currentServiceState.setDataRoamingType(3);
                }
            }
        } else if (ServiceState.isCdma(dataRegType)) {
            if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(1);
            }
        } else if (inSameCountry(currentServiceState.getDataOperatorNumeric())) {
            currentServiceState.setDataRoamingType(2);
        } else {
            currentServiceState.setDataRoamingType(3);
        }
    }

    private final boolean isConcurrentVoiceAndDataAllowedForVolte() {
        if (this.mSS.getDataRegState() == 0 && ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && getImsServiceState() == 0) {
            return true;
        }
        return false;
    }

    private final int getImsServiceState() {
        Phone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone != null) {
            return imsPhone.getServiceState().getState();
        }
        return 1;
    }

    private final boolean mergeEmergencyOnlyCdmaIms(boolean baseEmergencyOnly) {
        Phone imsPhone;
        if (!baseEmergencyOnly && this.mNewSS.getVoiceRegState() == 1 && this.mNewSS.getDataRegState() == 1 && (imsPhone = this.mPhone.getImsPhone()) != null) {
            return imsPhone.getServiceState().isEmergencyOnly();
        }
        return baseEmergencyOnly;
    }

    private void setRejectCauseNotification(int cause) {
        log("setRejectCauseNotification: create notification " + cause);
        Context context = this.mPhone.getContext();
        this.mNotificationBuilder = new Notification.Builder(context);
        this.mNotificationBuilder.setWhen(System.currentTimeMillis());
        this.mNotificationBuilder.setAutoCancel(true);
        this.mNotificationBuilder.setSmallIcon(17301642);
        this.mNotificationBuilder.setChannel("alert");
        this.mNotificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 134217728));
        CharSequence details = "";
        CharSequence title = context.getText(134545511);
        if (cause == 2) {
            details = context.getText(134545512);
        } else if (cause == 3) {
            details = context.getText(134545513);
        } else if (cause == 5) {
            details = context.getText(134545520);
        } else if (cause == 6) {
            details = context.getText(134545521);
        } else if (cause == 13) {
            details = context.getText(134545525);
        }
        log("setRejectCauseNotification: put notification " + ((Object) title) + " / " + ((Object) details));
        this.mNotificationBuilder.setContentTitle(title);
        this.mNotificationBuilder.setContentText(details);
        this.mNotification = this.mNotificationBuilder.build();
        ((NotificationManager) context.getSystemService("notification")).notify(REJECT_NOTIFICATION, this.mNotification);
    }

    /* access modifiers changed from: protected */
    public boolean isOperatorConsideredNonRoaming(ServiceState s) {
        boolean result = MtkServiceStateTracker.super.isOperatorConsideredNonRoaming(s);
        if (result) {
            log("isOperatorConsideredNonRoaming true");
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean isOperatorConsideredRoaming(ServiceState s) {
        boolean result;
        String simNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId());
        String operatorNumeric = s.getOperatorNumeric();
        if (TextUtils.isEmpty(simNumeric) || ((!simNumeric.substring(0, 3).equals("404") && !simNumeric.substring(0, 3).equals("405")) || TextUtils.isEmpty(operatorNumeric) || (!operatorNumeric.substring(0, 3).equals("404") && !operatorNumeric.substring(0, 3).equals("405")))) {
            result = MtkServiceStateTracker.super.isOperatorConsideredRoaming(s);
        } else {
            result = true;
        }
        if (result) {
            log("isOperatorConsideredRoaming true");
        }
        return result;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: com.mediatek.internal.telephony.MtkServiceStateTracker */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            IccCardApplicationStatus.AppState newUiccApplication = getUiccCardApplication();
            if ((this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) && newUiccApplication != null) {
                IccCardApplicationStatus.AppState appState = newUiccApplication.getState();
                if ((appState == IccCardApplicationStatus.AppState.APPSTATE_PIN || appState == IccCardApplicationStatus.AppState.APPSTATE_PUK) && this.mNetworkExsit) {
                    this.mEmergencyOnly = true;
                } else {
                    this.mEmergencyOnly = false;
                }
                this.mEmergencyOnly = mergeEmergencyOnlyCdmaIms(this.mEmergencyOnly);
                log("[CDMA]onUpdateIccAvailability, appstate=" + appState + ", mNetworkExsit=" + this.mNetworkExsit + ", mEmergencyOnly=" + this.mEmergencyOnly);
            }
            if (this.mUiccApplcation != newUiccApplication) {
                if (this.mIccRecords instanceof SIMRecords) {
                    this.mCdnr.updateEfFromUsim((SIMRecords) null);
                } else if (this.mIccRecords instanceof RuimRecords) {
                    this.mCdnr.updateEfFromRuim((RuimRecords) null);
                }
                if (this.mUiccApplcation != null) {
                    log("Removing stale icc objects.");
                    this.mUiccApplcation.unregisterForReady(this);
                    if (this.mIccRecords != null) {
                        this.mIccRecords.unregisterForRecordsLoaded(this);
                        if (this.mPhone.isPhoneTypeGsm()) {
                            this.mIccRecords.unregisterForRecordsEvents(this);
                        }
                    }
                    this.mIccRecords = null;
                    this.mUiccApplcation = null;
                    this.mOppoNeedNotify = true;
                }
                if (newUiccApplication != null) {
                    logv("New card found");
                    this.mUiccApplcation = newUiccApplication;
                    this.mIccRecords = this.mUiccApplcation.getIccRecords();
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mUiccApplcation.registerForReady(this, 17, (Object) null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 16, (Object) null);
                            this.mIccRecords.registerForRecordsEvents(this, (int) EVENT_SIM_OPL_LOADED, (Object) null);
                        }
                    } else if (this.mIsSubscriptionFromRuim) {
                        this.mUiccApplcation.registerForReady(this, 26, (Object) null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 27, (Object) null);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getOperatorNameFromEri() {
        String eriText = null;
        if (this.mPhone.isPhoneTypeCdma()) {
            if (this.mCi.getRadioState() != 1 || this.mIsSubscriptionFromRuim) {
                return null;
            }
            if (this.mSS.getVoiceRegState() == 0) {
                return this.mPhone.getCdmaEriText();
            }
            return this.mPhone.getContext().getText(17040956).toString();
        } else if (!this.mPhone.isPhoneTypeCdmaLte()) {
            return null;
        } else {
            String simMccMnc = TelephonyManager.from(this.mPhone.getContext()).getSimOperatorNumericForPhone(getPhoneId());
            boolean z = false;
            if (simMccMnc == null || (!simMccMnc.equals("310120") && !simMccMnc.equals("310009") && !simMccMnc.equals("311490") && !simMccMnc.equals("311870"))) {
                this.mEnableERI = false;
            } else {
                this.mEnableERI = true;
            }
            if (!((this.mUiccController.getUiccCard(getPhoneId()) == null || this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) ? false : true) && this.mCi.getRadioState() == 1 && this.mEriManager.isEriFileLoaded() && ((!ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology()) || this.mPhone.getContext().getResources().getBoolean(17891336)) && !this.mIsSubscriptionFromRuim && this.mEnableERI)) {
                eriText = this.mSS.getOperatorAlpha();
                if (this.mSS.getVoiceRegState() == 0) {
                    if (TextUtils.isEmpty(eriText)) {
                        eriText = this.mPhone.getCdmaEriText();
                    } else if (!(TextUtils.isEmpty(this.mPhone.getCdmaEriText()) || this.mSS.getCdmaRoamingIndicator() == 1 || this.mSS.getCdmaRoamingIndicator() == 160)) {
                        log("Append ERI text to PLMN String");
                        eriText = this.mSS.getOperatorAlphaLong() + "- " + this.mPhone.getCdmaEriText();
                    }
                } else if (this.mSS.getVoiceRegState() == 3) {
                    eriText = getServiceProviderName();
                    if (TextUtils.isEmpty(eriText)) {
                        eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                    }
                } else if (this.mSS.getDataRegState() != 0) {
                    eriText = this.mPhone.getContext().getText(17040956).toString();
                }
            }
            if (this.mUiccApplcation == null || this.mUiccApplcation.getState() != IccCardApplicationStatus.AppState.APPSTATE_READY || this.mIccRecords == null || getCombinedRegState(this.mSS) != 0 || ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology())) {
                return eriText;
            }
            boolean showSpn = this.mIccRecords.getCsimSpnDisplayCondition();
            if (showSpn) {
                try {
                    if (this.mServiceStateTrackerExt.allowSpnDisplayed()) {
                        z = true;
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            showSpn = z;
            int iconIndex = this.mSS.getCdmaEriIconIndex();
            if (!showSpn || iconIndex != 1 || !isInHomeSidNid(this.mSS.getCdmaSystemId(), this.mSS.getCdmaNetworkId()) || this.mIccRecords == null) {
                return eriText;
            }
            return getServiceProviderName();
        }
    }

    /* access modifiers changed from: protected */
    public void setOperatorIdd(String operatorNumeric) {
        String idd = "";
        try {
            idd = this.mHbpcdUtils.getIddByMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
        } catch (NumberFormatException ex) {
            loge("setOperatorIdd: idd error" + ex);
        } catch (StringIndexOutOfBoundsException ex2) {
            loge("setOperatorIdd: idd error" + ex2);
        } catch (Exception e) {
            loge("setOperatorIdd: idd error" + e);
        }
        if (idd == null || idd.isEmpty()) {
            this.mPhone.setGlobalSystemProperty("gsm.operator.idpstring", "+");
        } else {
            this.mPhone.setGlobalSystemProperty("gsm.operator.idpstring", idd);
        }
    }

    public void setRadioPowerFromCarrier(boolean enable) {
        this.mRadioDisabledByCarrier = !enable;
        RadioManager.getInstance().setRadioPower(enable, this.mPhone.getPhoneId());
    }

    /* access modifiers changed from: protected */
    public int mtkReplaceDdsIfUnset(int dds) {
        int[] subIds;
        if (dds != -1 || (subIds = SubscriptionManager.getSubId(RadioCapabilitySwitchUtil.getMainCapabilityPhoneId())) == null || subIds.length <= 0) {
            return dds;
        }
        log("powerOffRadioSafely: replace dds with main protocol sub ");
        return subIds[0];
    }

    /* access modifiers changed from: protected */
    public boolean mtkPowerOffNonDdsPhone() {
        boolean isAirplaneModeOn = TextUtils.equals(SystemProperties.get("persist.radio.airplane_mode_on", ""), "1");
        boolean allDataDisconnected = this.mPhone.areAllDataDisconnected();
        log("powerOffRadioSafely: apm:" + isAirplaneModeOn + ", allDataDisconnected:" + allDataDisconnected + ", mSubId:" + this.mPhone.getSubId() + ", shutdown:" + isDeviceShuttingDown());
        if (!allDataDisconnected || isAirplaneModeOn || isDeviceShuttingDown()) {
            return false;
        }
        log("Non-dds data disconnected, turn off radio right away.");
        hangupAndPowerOff();
        this.mPendingRadioPowerOffAfterDataOff = false;
        return true;
    }

    /* access modifiers changed from: protected */
    public int mtkReplaceDisconnectTimer() {
        return 5000;
    }

    protected static final String lookupOperatorName(Context context, int subId, String numeric, boolean desireLongName) {
        if (PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId)) == null) {
            Rlog.e(LOG_TAG, "lookupOperatorName getPhone null");
            return numeric;
        }
        String operName = MtkSpnOverride.getInstance().getSpnByPattern(subId, numeric);
        boolean isChinaTelecomMvno = isChinaTelecomMvno(context, subId, numeric, operName);
        if (operName == null || isChinaTelecomMvno) {
            operName = MtkSpnOverride.getInstance().getSpnByNumeric(numeric, desireLongName, context);
        }
        return operName == null ? numeric : operName;
    }

    private static final boolean isChinaTelecomMvno(Context context, int subId, String numeric, String mvnoOperName) {
        String ctName = context.getText(134545503).toString();
        String simCarrierName = TelephonyManager.from(context).getSimOperatorName(subId);
        if (ctName != null && ctName.equals(mvnoOperName)) {
            return true;
        }
        if (("20404".equals(numeric) || "45403".equals(numeric)) && ctName != null && ctName.equals(simCarrierName)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onSignalStrengthResult(AsyncResult ar) {
        if (OemFeature.FEATURE_NW_REG_SWITCH_SMOOTH && this.mReference != null) {
            return MtkServiceStateTracker.super.onSignalStrengthResultEx(ar, this.mSignalStrength);
        }
        String mlog = "";
        IServiceStateTrackerExt iServiceStateTrackerExt = this.mServiceStateTrackerExt;
        int[] mtkLteRssnrThreshold = null;
        boolean mtkRsrpOnly = (iServiceStateTrackerExt != null ? Boolean.valueOf(iServiceStateTrackerExt.getMtkRsrpOnly()) : null).booleanValue();
        IServiceStateTrackerExt iServiceStateTrackerExt2 = this.mServiceStateTrackerExt;
        int[] mtkLteRsrpThreshold = iServiceStateTrackerExt2 != null ? iServiceStateTrackerExt2.getMtkLteRsrpThreshold() : null;
        IServiceStateTrackerExt iServiceStateTrackerExt3 = this.mServiceStateTrackerExt;
        if (iServiceStateTrackerExt3 != null) {
            mtkLteRssnrThreshold = iServiceStateTrackerExt3.getMtkLteRssnrThreshold();
        }
        if (this.mSignalStrength != null) {
            mlog = "old:{level:" + this.mSignalStrength.getLevel() + ", raw:" + this.mSignalStrength.toString() + "}, ";
        }
        if (ar.exception != null || ar.result == null) {
            log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            this.mSignalStrength = new MtkSignalStrength(this.mPhone.getPhoneId());
        } else {
            this.mSignalStrength = new MtkSignalStrength(this.mPhone.getPhoneId(), (SignalStrength) ar.result);
            PersistableBundle config = getCarrierConfig();
            this.mSignalStrength.updateLevel(config, this.mSS);
            MtkSignalStrength mtkSignal = this.mSignalStrength;
            mtkSignal.setMtkRsrpOnly(mtkRsrpOnly);
            mtkSignal.setMtkLteRsrpThreshold(mtkLteRsrpThreshold);
            mtkSignal.setMtkLteRssnrThreshold(mtkLteRssnrThreshold);
            mtkSignal.updateMtkLevel(config, this.mSS);
            MtkServiceStateTracker.super.updateSignalStrengthLevel(this.mSignalStrength);
        }
        if (this.mSignalStrength != null) {
            mlog = mlog + "new:{level:" + this.mSignalStrength.getLevel() + ", raw:" + this.mSignalStrength.toString() + "}";
        }
        log(mlog);
        return notifySignalStrength();
    }

    /* access modifiers changed from: protected */
    public boolean currentMccEqualsSimMcc(ServiceState s) {
        try {
            return ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId()).substring(0, 3).equals(s.getOperatorNumeric().substring(0, 3));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean getImsEccOnly() {
        Phone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone != null) {
            return imsPhone.getServiceState().isEmergencyOnly();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new MtkSignalStrength(this.mPhone.getPhoneId());
    }

    /* access modifiers changed from: protected */
    public void hangupAllImsCall() {
        Phone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone == null) {
            return;
        }
        if (!imsPhone.isWifiCallingEnabled() || this.mDeviceShuttingDown) {
            imsPhone.getForegroundCall().hangupIfAlive();
            imsPhone.getBackgroundCall().hangupIfAlive();
            imsPhone.getRingingCall().hangupIfAlive();
            log("hangupAndPowerOff: hangup VoLTE call.");
        }
    }

    /* access modifiers changed from: protected */
    public void onCarrierConfigChanged() {
        PersistableBundle config = getCarrierConfig();
        log("CarrierConfigChange " + config);
        this.mEriManager.loadEriFile();
        this.mCdnr.updateEfForEri(getOperatorNameFromEri());
        this.mEriTriggeredPollState = true;
        updateLteEarfcnLists(config);
        updateReportingCriteria(config);
        updateOperatorNamePattern(config);
        this.mCdnr.updateEfFromCarrierConfig(config);
        pollState();
    }

    public int getLacFromCellIdentity(CellIdentity id) {
        if (id == null) {
            return -1;
        }
        int lac = -1;
        int type = id.getType();
        if (type == 1) {
            lac = ((CellIdentityGsm) id).getLac();
        } else if (type == 3) {
            lac = ((CellIdentityLte) id).getTac();
        } else if (type == 4) {
            lac = ((CellIdentityWcdma) id).getLac();
        } else if (type == 5) {
            lac = ((CellIdentityTdscdma) id).getLac();
        }
        if (lac == Integer.MAX_VALUE) {
            return -1;
        }
        return lac;
    }

    public int getLac() {
        return getLacFromCellIdentity(this.mCellIdentity);
    }

    /* access modifiers changed from: protected */
    public void notifySpnDisplayUpdate(CarrierDisplayNameData data) {
        int subId = this.mPhone.getSubId();
        if (this.mSubId != subId || data.shouldShowPlmn() != this.mCurShowPlmn || data.shouldShowSpn() != this.mCurShowSpn || !TextUtils.equals(data.getSpn(), this.mCurSpn) || !TextUtils.equals(data.getDataSpn(), this.mCurDataSpn) || !TextUtils.equals(data.getPlmn(), this.mCurPlmn)) {
            String log = String.format("updateSpnDisplay: changed sending intent, rule=%d, showPlmn='%b', plmn='%s', showSpn='%b', spn='%s', dataSpn='%s', subId='%d'", Integer.valueOf(getCarrierNameDisplayBitmask(this.mSS)), Boolean.valueOf(data.shouldShowPlmn()), data.getPlmn(), Boolean.valueOf(data.shouldShowSpn()), data.getSpn(), data.getDataSpn(), Integer.valueOf(subId));
            log("updateSpnDisplay: " + log);
            Intent intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
            intent.putExtra("showSpn", data.shouldShowSpn());
            intent.putExtra("spn", data.getSpn());
            intent.putExtra("spnData", data.getDataSpn());
            intent.putExtra("showPlmn", data.shouldShowPlmn());
            intent.putExtra("plmn", data.getPlmn());
            intent.putExtra("hnbName", this.mHhbName);
            intent.putExtra("csgId", this.mCsgId);
            intent.putExtra("domain", this.mFemtocellDomain);
            intent.putExtra("femtocell", this.mIsFemtocell);
            if (TelephonyManager.getDefault().getPhoneCount() == 1) {
                intent.addFlags(536870912);
            }
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            updatePLMN(data.shouldShowPlmn(), data.getPlmn(), data.shouldShowSpn(), data.getSpn());
        }
        this.mSubId = subId;
        this.mCurShowSpn = data.shouldShowSpn();
        this.mCurShowPlmn = data.shouldShowPlmn();
        this.mCurSpn = data.getSpn();
        this.mCurDataSpn = data.getDataSpn();
        this.mCurPlmn = data.getPlmn();
    }

    public void registerForMtkNrStateChanged(Handler h, int what, Object obj) {
        this.mMtkNrStateChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForMtkNrStateChanged(Handler h) {
        this.mMtkNrStateChangedRegistrants.remove(h);
    }

    private int[] getMtkBandwidthsFromConfigs(List<PhysicalChannelConfig> list) {
        return list.stream().map($$Lambda$WWHOcG5P4jgjzPPgLwmwN15OM.INSTANCE).mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
    }

    public String getServiceProviderName() {
        String default_spn = MtkServiceStateTracker.super.getServiceProviderName();
        if (default_spn.equals("carrier_a")) {
            return default_spn;
        }
        String carrierName = this.mIccRecords != null ? this.mIccRecords.getServiceProviderName() : "";
        log("USE_SIM_SPN_ONLY " + carrierName);
        return carrierName;
    }

    public boolean isVowifiRegistered(int slot) {
        boolean isVowifi = SystemProperties.get("gsm.ims.type" + slot, "volte").equals("vowifi");
        log("isVowifiRegistered " + isVowifi);
        return isVowifi;
    }

    public void setSignalStrength(SignalStrength signalStrength) {
        log("setSignalStrength : " + signalStrength);
        this.mSignalStrength.copyFrom(signalStrength);
    }

    /* access modifiers changed from: protected */
    public boolean isSib2UriChange(NetworkRegistrationInfo oldRegState, NetworkRegistrationInfo newRegState) {
        if (oldRegState == null || newRegState == null) {
            return oldRegState != newRegState;
        }
        DataSpecificRegistrationInfo oldDataSpecificStates = oldRegState.getDataSpecificInfo();
        DataSpecificRegistrationInfo NewDataSpecificStates = newRegState.getDataSpecificInfo();
        if (oldDataSpecificStates == null || NewDataSpecificStates == null) {
            return oldDataSpecificStates != NewDataSpecificStates;
        }
        log("isSib2UriChange : old " + oldDataSpecificStates.isEnDcAvailable + "new " + NewDataSpecificStates.isEnDcAvailable);
        return oldDataSpecificStates.isEnDcAvailable != NewDataSpecificStates.isEnDcAvailable;
    }

    /* access modifiers changed from: protected */
    public boolean getSib2Uri(NetworkRegistrationInfo regState) {
        DataSpecificRegistrationInfo dataSpecificStates;
        if (regState == null || (dataSpecificStates = regState.getDataSpecificInfo()) == null) {
            return false;
        }
        return dataSpecificStates.isEnDcAvailable;
    }
}
