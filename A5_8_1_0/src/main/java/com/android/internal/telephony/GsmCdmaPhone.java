package com.android.internal.telephony;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Telephony.Carriers;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.telephony.UssdResponse;
import android.telephony.cdma.CdmaCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.DctConstants.Activity;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneInternalInterface.DataActivityState;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.cdma.CdmaMmiCode;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.NetworkInfoWithAcT;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.IccException;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccVmNotSupportedException;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.IsimUiccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.util.NotificationChannelController;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codeaurora.ims.QtiCallConstants;
import org.codeaurora.internal.IExtTelephony.Stub;

public class GsmCdmaPhone extends Phone {
    /* renamed from: -com-android-internal-telephony-DctConstants$ActivitySwitchesValues */
    private static final /* synthetic */ int[] f11xfa7940f = null;
    /* renamed from: -com-android-internal-telephony-DctConstants$StateSwitchesValues */
    private static final /* synthetic */ int[] f12-com-android-internal-telephony-DctConstants$StateSwitchesValues = null;
    public static final int CANCEL_ECM_TIMER = 1;
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    private static final String[][] DEFAULT_VM_BY_MCCMNC;
    private static final int IMEI_14_DIGIT = 14;
    private static final int INVALID_SYSTEM_SELECTION_CODE = -1;
    private static final String IS683A_FEATURE_CODE = "*228";
    private static final int IS683A_FEATURE_CODE_NUM_DIGITS = 4;
    private static final int IS683A_SYS_SEL_CODE_NUM_DIGITS = 2;
    private static final int IS683A_SYS_SEL_CODE_OFFSET = 4;
    private static final int IS683_CONST_1900MHZ_A_BLOCK = 2;
    private static final int IS683_CONST_1900MHZ_B_BLOCK = 3;
    private static final int IS683_CONST_1900MHZ_C_BLOCK = 4;
    private static final int IS683_CONST_1900MHZ_D_BLOCK = 5;
    private static final int IS683_CONST_1900MHZ_E_BLOCK = 6;
    private static final int IS683_CONST_1900MHZ_F_BLOCK = 7;
    private static final int IS683_CONST_800MHZ_A_BAND = 0;
    private static final int IS683_CONST_800MHZ_B_BAND = 1;
    public static final String LOG_TAG = "GsmCdmaPhone";
    public static final String PROPERTY_CDMA_HOME_OPERATOR_NUMERIC = "ro.cdma.home.operator.numeric";
    public static final int RESTART_ECM_TIMER = 0;
    private static final boolean VDBG = false;
    private static final String VM_NUMBER = "vm_number_key";
    private static final String VM_NUMBER_CDMA = "vm_number_key_cdma";
    private static final String VM_SIM_IMSI = "vm_sim_imsi_key";
    private static Pattern pOtaSpNumSchema = Pattern.compile("[,\\s]+");
    private boolean mBroadcastEmergencyCallStateChanges;
    private BroadcastReceiver mBroadcastReceiver;
    private CarrierKeyDownloadManager mCDM;
    public GsmCdmaCallTracker mCT;
    private String mCarrierOtaSpNumSchema;
    private CdmaSubscriptionSourceManager mCdmaSSM;
    public int mCdmaSubscriptionSource;
    private Registrant mEcmExitRespRegistrant;
    private final RegistrantList mEcmTimerResetRegistrants;
    private final RegistrantList mEriFileLoadedRegistrants;
    public EriManager mEriManager;
    private String mEsn;
    private Runnable mExitEcmRunnable;
    private IccCardProxy mIccCardProxy;
    private IccPhoneBookInterfaceManager mIccPhoneBookIntManager;
    private IccSmsInterfaceManager mIccSmsInterfaceManager;
    private String mImei;
    private String mImeiSv;
    private IsimUiccRecords mIsimUiccRecords;
    private String mMeid;
    private ArrayList<MmiCode> mPendingMMIs;
    private int mPrecisePhoneType;
    private boolean mResetModemOnRadioTechnologyChange;
    private int mRilVersion;
    public ServiceStateTracker mSST;
    private SIMRecords mSimRecords;
    private RegistrantList mSsnRegistrants;
    private String mVmNumber;
    private WakeLock mWakeLock;

    private static class Cfu {
        final Message mOnComplete;
        final String mSetCfNumber;

        Cfu(String cfNumber, Message onComplete) {
            this.mSetCfNumber = cfNumber;
            this.mOnComplete = onComplete;
        }
    }

    /* renamed from: -getcom-android-internal-telephony-DctConstants$ActivitySwitchesValues */
    private static /* synthetic */ int[] m12xd0f730eb() {
        if (f11xfa7940f != null) {
            return f11xfa7940f;
        }
        int[] iArr = new int[Activity.values().length];
        try {
            iArr[Activity.DATAIN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Activity.DATAINANDOUT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Activity.DATAOUT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Activity.DORMANT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Activity.NONE.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        f11xfa7940f = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-DctConstants$StateSwitchesValues */
    private static /* synthetic */ int[] m13xf0fbc33d() {
        if (f12-com-android-internal-telephony-DctConstants$StateSwitchesValues != null) {
            return f12-com-android-internal-telephony-DctConstants$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.FAILED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.RETRYING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.SCANNING.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        f12-com-android-internal-telephony-DctConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    static {
        r0 = new String[4][];
        r0[0] = new String[]{"53024", "+64222022002"};
        r0[1] = new String[]{"50501", "+61101"};
        r0[2] = new String[]{"50571", "+61101"};
        r0[3] = new String[]{"50572", "+61101"};
        DEFAULT_VM_BY_MCCMNC = r0;
    }

    public GsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        this(context, ci, notifier, false, phoneId, precisePhoneType, telephonyComponentFactory);
    }

    public GsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, boolean unitTestMode, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        String str;
        if (precisePhoneType == 1) {
            str = "GSM";
        } else {
            str = "CDMA";
        }
        super(str, notifier, context, ci, unitTestMode, phoneId, telephonyComponentFactory);
        this.mSsnRegistrants = new RegistrantList();
        this.mCdmaSubscriptionSource = -1;
        this.mEriFileLoadedRegistrants = new RegistrantList();
        this.mExitEcmRunnable = new Runnable() {
            public void run() {
                GsmCdmaPhone.this.exitEmergencyCallbackMode();
            }
        };
        this.mPendingMMIs = new ArrayList();
        this.mEcmTimerResetRegistrants = new RegistrantList();
        this.mResetModemOnRadioTechnologyChange = false;
        this.mBroadcastEmergencyCallStateChanges = false;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* JADX WARNING: Missing block: B:4:0x006a, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(Context context, Intent intent) {
                Rlog.d(GsmCdmaPhone.LOG_TAG, "mBroadcastReceiver: action " + intent.getAction());
                int phoneId = intent.getIntExtra("phone", -1);
                boolean isForce = intent.getBooleanExtra("IS_FORCE", true);
                Rlog.d(GsmCdmaPhone.LOG_TAG, "ACTION_CARRIER_CONFIG_CHANGED : IS_FORCE " + isForce + ", phoneId " + phoneId + ", mPhoneId " + GsmCdmaPhone.this.mPhoneId);
                if (isForce && phoneId == GsmCdmaPhone.this.mPhoneId && intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED") && intent.getExtras().getInt("phone") == GsmCdmaPhone.this.mPhoneId) {
                    GsmCdmaPhone.this.sendMessage(GsmCdmaPhone.this.obtainMessage(43));
                }
            }
        };
        this.mPrecisePhoneType = precisePhoneType;
        initOnce(ci);
        initRatSpecific(precisePhoneType);
        this.mCarrierActionAgent = this.mTelephonyComponentFactory.makeCarrierActionAgent(this);
        this.mCarrierSignalAgent = this.mTelephonyComponentFactory.makeCarrierSignalAgent(this);
        this.mSST = this.mTelephonyComponentFactory.makeServiceStateTracker(this, this.mCi);
        this.mDcTracker = this.mTelephonyComponentFactory.makeDcTracker(this);
        this.mSST.registerForNetworkAttached(this, 19, null);
        logd("GsmCdmaPhone: constructor: sub = " + this.mPhoneId);
    }

    private void initOnce(CommandsInterface ci) {
        if (ci instanceof SimulatedRadioControl) {
            this.mSimulatedRadioControl = (SimulatedRadioControl) ci;
        }
        this.mCT = this.mTelephonyComponentFactory.makeGsmCdmaCallTracker(this);
        this.mIccPhoneBookIntManager = this.mTelephonyComponentFactory.makeIccPhoneBookInterfaceManager(this);
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mIccSmsInterfaceManager = this.mTelephonyComponentFactory.makeIccSmsInterfaceManager(this);
        this.mIccCardProxy = this.mTelephonyComponentFactory.makeIccCardProxy(this.mContext, this.mCi, this.mPhoneId);
        this.mCi.registerForAvailable(this, 1, null);
        this.mCi.registerForOffOrNotAvailable(this, 8, null);
        this.mCi.registerForOn(this, 5, null);
        this.mCi.setOnSuppServiceNotification(this, 2, null);
        this.mCi.setOnUSSD(this, 7, null);
        this.mCi.setOnSs(this, 36, null);
        this.mCdmaSSM = this.mTelephonyComponentFactory.getCdmaSubscriptionSourceManagerInstance(this.mContext, this.mCi, this, 27, null);
        this.mEriManager = this.mTelephonyComponentFactory.makeEriManager(this, this.mContext, 0);
        this.mCi.setEmergencyCallbackMode(this, 25, null);
        this.mCi.registerForExitEmergencyCallbackMode(this, 26, null);
        this.mCi.registerForModemReset(this, 45, null);
        this.mCarrierOtaSpNumSchema = TelephonyManager.from(this.mContext).getOtaSpNumberSchemaForPhone(getPhoneId(), SpnOverride.MVNO_TYPE_NONE);
        this.mResetModemOnRadioTechnologyChange = SystemProperties.getBoolean("persist.radio.reset_on_switch", false);
        this.mCi.registerForRilConnected(this, 41, null);
        this.mCi.registerForVoiceRadioTechChanged(this, 39, null);
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
        this.mCDM = new CarrierKeyDownloadManager(this);
    }

    private void initRatSpecific(int precisePhoneType) {
        this.mPendingMMIs.clear();
        this.mIccPhoneBookIntManager.updateIccRecords(null);
        this.mEsn = null;
        this.mMeid = null;
        this.mPrecisePhoneType = precisePhoneType;
        logd("Precise phone type " + this.mPrecisePhoneType);
        TelephonyManager tm = TelephonyManager.from(this.mContext);
        if (isPhoneTypeGsm()) {
            this.mCi.setPhoneType(1);
            tm.setPhoneType(getPhoneId(), 1);
            this.mIccCardProxy.setVoiceRadioTech(3);
            return;
        }
        this.mCdmaSubscriptionSource = this.mCdmaSSM.getCdmaSubscriptionSource();
        this.mIsPhoneInEcmState = Phone.getInEcmMode();
        if (this.mIsPhoneInEcmState) {
            this.mCi.exitEmergencyCallbackMode(obtainMessage(26));
        }
        this.mCi.setPhoneType(2);
        tm.setPhoneType(getPhoneId(), 2);
        this.mIccCardProxy.setVoiceRadioTech(6);
        String operatorAlpha = SystemProperties.get("ro.cdma.home.operator.alpha");
        String operatorNumeric = SystemProperties.get(PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
        logd("init: operatorAlpha='" + operatorAlpha + "' operatorNumeric='" + operatorNumeric + "'");
        if (this.mUiccController.getUiccCardApplication(this.mPhoneId, 1) == null || isPhoneTypeCdmaLte() || isPhoneTypeCdma()) {
            if (!TextUtils.isEmpty(operatorAlpha)) {
                logd("init: set 'gsm.sim.operator.alpha' to operator='" + operatorAlpha + "'");
                tm.setSimOperatorNameForPhone(this.mPhoneId, operatorAlpha);
            }
            if (!TextUtils.isEmpty(operatorNumeric)) {
                logd("init: set 'gsm.sim.operator.numeric' to operator='" + operatorNumeric + "'");
                logd("update icc_operator_numeric=" + operatorNumeric);
                tm.setSimOperatorNumericForPhone(this.mPhoneId, operatorNumeric);
                SubscriptionController.getInstance().setMccMnc(operatorNumeric, getSubId());
                setIsoCountryProperty(operatorNumeric);
                logd("update mccmnc=" + operatorNumeric);
                MccTable.updateMccMncConfiguration(this.mContext, operatorNumeric, false);
            }
        }
        updateCurrentCarrierInProvider(operatorNumeric);
    }

    private void setIsoCountryProperty(String operatorNumeric) {
        TelephonyManager tm = TelephonyManager.from(this.mContext);
        if (TextUtils.isEmpty(operatorNumeric)) {
            logd("setIsoCountryProperty: clear 'gsm.sim.operator.iso-country'");
            tm.setSimCountryIsoForPhone(this.mPhoneId, SpnOverride.MVNO_TYPE_NONE);
            return;
        }
        String iso = SpnOverride.MVNO_TYPE_NONE;
        try {
            iso = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
        } catch (NumberFormatException ex) {
            Rlog.e(LOG_TAG, "setIsoCountryProperty: countryCodeForMcc error", ex);
        } catch (StringIndexOutOfBoundsException ex2) {
            Rlog.e(LOG_TAG, "setIsoCountryProperty: countryCodeForMcc error", ex2);
        }
        logd("setIsoCountryProperty: set 'gsm.sim.operator.iso-country' to iso=" + iso);
        tm.setSimCountryIsoForPhone(this.mPhoneId, iso);
    }

    public boolean isPhoneTypeGsm() {
        return this.mPrecisePhoneType == 1;
    }

    public boolean isPhoneTypeCdma() {
        return this.mPrecisePhoneType == 2;
    }

    public boolean isPhoneTypeCdmaLte() {
        return this.mPrecisePhoneType == 6;
    }

    private void switchPhoneType(int precisePhoneType) {
        removeCallbacks(this.mExitEcmRunnable);
        initRatSpecific(precisePhoneType);
        this.mSST.updatePhoneType();
        setPhoneName(precisePhoneType == 1 ? "GSM" : "CDMA");
        onUpdateIccAvailability();
        this.mCT.updatePhoneType();
        RadioState radioState = this.mCi.getRadioState();
        if (radioState.isAvailable()) {
            handleRadioAvailable();
            if (radioState.isOn()) {
                handleRadioOn();
            }
        }
        if (!radioState.isAvailable() || (radioState.isOn() ^ 1) != 0) {
            handleRadioOffOrNotAvailable();
        }
    }

    protected void finalize() {
        if (DBG) {
            logd("GsmCdmaPhone finalized");
        }
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            Rlog.e(LOG_TAG, "UNEXPECTED; mWakeLock is held when finalizing.");
            this.mWakeLock.release();
        }
    }

    public ServiceState getServiceState() {
        if ((this.mSST == null || this.mSST.mSS.getState() != 0) && this.mImsPhone != null) {
            return ServiceState.mergeServiceStates(this.mSST == null ? new ServiceState() : this.mSST.mSS, this.mImsPhone.getServiceState());
        } else if (this.mSST != null) {
            return this.mSST.mSS;
        } else {
            return new ServiceState();
        }
    }

    public CellLocation getCellLocation(WorkSource workSource) {
        if (isPhoneTypeGsm()) {
            return this.mSST.getCellLocation(workSource);
        }
        CdmaCellLocation loc = this.mSST.mCellLoc;
        if (loc != null) {
            try {
                if (!(!loc.isEmpty() || this.mSST == null || this.mSST.mNewCellLoc == null)) {
                    CdmaCellLocation locNew = this.mSST.mNewCellLoc;
                    if (!(locNew == null || (locNew.isEmpty() ^ 1) == 0)) {
                        loc = (CdmaCellLocation) this.mSST.mNewCellLoc;
                    }
                }
            } catch (Exception e) {
                Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "getCellLocation--error");
            }
        }
        if (Secure.getInt(getContext().getContentResolver(), "location_mode", 0) == 0) {
            CdmaCellLocation privateLoc = new CdmaCellLocation();
            privateLoc.setCellLocationData(loc.getBaseStationId(), Integer.MAX_VALUE, Integer.MAX_VALUE, loc.getSystemId(), loc.getNetworkId());
            loc = privateLoc;
        }
        return loc;
    }

    public PhoneConstants.State getState() {
        if (this.mImsPhone != null) {
            PhoneConstants.State imsState = this.mImsPhone.getState();
            if (imsState != PhoneConstants.State.IDLE) {
                return imsState;
            }
        }
        return this.mCT.mState;
    }

    public int getPhoneType() {
        if (this.mPrecisePhoneType == 1) {
            return 1;
        }
        return 2;
    }

    public ServiceStateTracker getServiceStateTracker() {
        return this.mSST;
    }

    public CallTracker getCallTracker() {
        return this.mCT;
    }

    public void updateVoiceMail() {
        if (isPhoneTypeGsm()) {
            int countVoiceMessages = 0;
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                countVoiceMessages = r.getVoiceMessageCount();
            }
            if (countVoiceMessages == -2) {
                countVoiceMessages = getStoredVoiceMessageCount();
            }
            logd("updateVoiceMail countVoiceMessages = " + countVoiceMessages + " subId " + getSubId());
            setVoiceMessageCount(countVoiceMessages);
            return;
        }
        setVoiceMessageCount(getStoredVoiceMessageCount());
    }

    public List<? extends MmiCode> getPendingMmiCodes() {
        return this.mPendingMMIs;
    }

    public DataState getDataConnectionState(String apnType) {
        DataState ret = DataState.DISCONNECTED;
        if (this.mSST == null) {
            ret = DataState.DISCONNECTED;
        } else if (this.mSST.getCurrentDataConnectionState() == 0 || !(isPhoneTypeCdma() || isPhoneTypeCdmaLte() || (isPhoneTypeGsm() && (apnType.equals("emergency") ^ 1) != 0))) {
            switch (m13xf0fbc33d()[this.mDcTracker.getState(apnType).ordinal()]) {
                case 1:
                case 3:
                    if (this.mCT.mState == PhoneConstants.State.IDLE || (this.mSST.isConcurrentVoiceAndDataAllowed() ^ 1) == 0) {
                        ret = DataState.CONNECTED;
                    } else {
                        ret = DataState.SUSPENDED;
                    }
                    if (TelephonyManager.getDefault().getMultiSimConfiguration() != MultiSimVariants.DSDA) {
                        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                            Phone phone = PhoneFactory.getPhone(i);
                            if (!(phone == null || phone.getState() == PhoneConstants.State.IDLE || phone.getPhoneId() == getPhoneId())) {
                                ret = DataState.SUSPENDED;
                            }
                        }
                        break;
                    }
                    break;
                case 2:
                case 7:
                    ret = DataState.CONNECTING;
                    break;
                case 4:
                case 5:
                case 6:
                    ret = DataState.DISCONNECTED;
                    break;
            }
        } else {
            ret = DataState.DISCONNECTED;
        }
        logd("getDataConnectionState apnType=" + apnType + " ret=" + ret);
        return ret;
    }

    public DataActivityState getDataActivityState() {
        DataActivityState ret = DataActivityState.NONE;
        if (this.mSST.getCurrentDataConnectionState() != 0) {
            return ret;
        }
        switch (m12xd0f730eb()[this.mDcTracker.getActivity().ordinal()]) {
            case 1:
                return DataActivityState.DATAIN;
            case 2:
                return DataActivityState.DATAINANDOUT;
            case 3:
                return DataActivityState.DATAOUT;
            case 4:
                return DataActivityState.DORMANT;
            default:
                return DataActivityState.NONE;
        }
    }

    public void notifyPhoneStateChanged() {
        this.mNotifier.notifyPhoneState(this);
    }

    public void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChangedP();
    }

    public void notifyNewRingingConnection(Connection c) {
        super.notifyNewRingingConnectionP(c);
    }

    public void notifyDisconnect(Connection cn) {
        this.mDisconnectRegistrants.notifyResult(cn);
        this.mNotifier.notifyDisconnectCause(cn.getDisconnectCause(), cn.getPreciseDisconnectCause());
    }

    public void notifyUnknownConnection(Connection cn) {
        super.notifyUnknownConnectionP(cn);
    }

    public boolean isInEmergencyCall() {
        if (isPhoneTypeGsm()) {
            return false;
        }
        return this.mCT.isInEmergencyCall();
    }

    protected void setIsInEmergencyCall() {
        if (!isPhoneTypeGsm()) {
            this.mCT.setIsInEmergencyCall();
        }
    }

    private void sendEmergencyCallbackModeChange() {
        Intent intent = new Intent("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intent.putExtra("phoneinECMState", isInEcm());
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
        ActivityManager.broadcastStickyIntent(intent, -1);
        if (DBG) {
            logd("sendEmergencyCallbackModeChange");
        }
    }

    public void sendEmergencyCallStateChange(boolean callActive) {
        if (this.mBroadcastEmergencyCallStateChanges) {
            Intent intent = new Intent("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
            intent.putExtra("phoneInEmergencyCall", callActive);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
            ActivityManager.broadcastStickyIntent(intent, -1);
            if (DBG) {
                Rlog.d(LOG_TAG, "sendEmergencyCallStateChange: callActive " + callActive);
            }
        }
    }

    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
        this.mBroadcastEmergencyCallStateChanges = broadcast;
    }

    public void notifySuppServiceFailed(SuppService code) {
        this.mSuppServiceFailedRegistrants.notifyResult(code);
    }

    public void notifyServiceStateChanged(ServiceState ss) {
        super.notifyServiceStateChangedP(ss);
    }

    public void notifyLocationChanged() {
        this.mNotifier.notifyCellLocation(this);
    }

    public void notifyCallForwardingIndicator() {
        this.mNotifier.notifyCallForwardingChanged(this);
    }

    public void setSystemProperty(String property, String value) {
        if (!getUnitTestMode()) {
            if (isPhoneTypeGsm() || isPhoneTypeCdmaLte() || isPhoneTypeCdma()) {
                TelephonyManager.setTelephonyProperty(this.mPhoneId, property, value);
            } else {
                super.setSystemProperty(property, value);
            }
        }
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrants.addUnique(h, what, obj);
        if (this.mSsnRegistrants.size() == 1) {
            this.mCi.setSuppServiceNotifications(true, null);
        }
    }

    public void unregisterForSuppServiceNotification(Handler h) {
        this.mSsnRegistrants.remove(h);
        if (this.mSsnRegistrants.size() == 0) {
            this.mCi.setSuppServiceNotifications(false, null);
        }
    }

    public void registerForSimRecordsLoaded(Handler h, int what, Object obj) {
        this.mSimRecordsLoadedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSimRecordsLoaded(Handler h) {
        this.mSimRecordsLoadedRegistrants.remove(h);
    }

    public void acceptCall(int videoState) throws CallStateException {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone == null || !imsPhone.getRingingCall().isRinging()) {
            this.mCT.acceptCall();
        } else {
            imsPhone.acceptCall(videoState);
        }
    }

    public void rejectCall() throws CallStateException {
        this.mCT.rejectCall();
    }

    public void switchHoldingAndActive() throws CallStateException {
        this.mCT.switchWaitingOrHoldingAndActive();
    }

    public String getIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (!isPhoneTypeGsm() && r == null) {
            r = this.mUiccController.getIccRecords(this.mPhoneId, 1);
        }
        if (r != null) {
            return r.getIccId();
        }
        return null;
    }

    public String getFullIccSerialNumber() {
        return oemGetFullIccSerialNumber();
    }

    public boolean canConference() {
        if (this.mImsPhone != null && this.mImsPhone.canConference()) {
            return true;
        }
        if (isPhoneTypeGsm()) {
            return this.mCT.canConference();
        }
        loge("canConference: not possible in CDMA");
        return false;
    }

    public void conference() {
        if (this.mImsPhone == null || !this.mImsPhone.canConference()) {
            if (isPhoneTypeGsm()) {
                this.mCT.conference();
            } else {
                loge("conference: not possible in CDMA");
            }
            return;
        }
        logd("conference() - delegated to IMS phone");
        try {
            this.mImsPhone.conference();
        } catch (CallStateException e) {
            loge(e.toString());
        }
    }

    public void enableEnhancedVoicePrivacy(boolean enable, Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("enableEnhancedVoicePrivacy: not expected on GSM");
        } else {
            this.mCi.setPreferredVoicePrivacy(enable, onComplete);
        }
    }

    public void getEnhancedVoicePrivacy(Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("getEnhancedVoicePrivacy: not expected on GSM");
        } else {
            this.mCi.getPreferredVoicePrivacy(onComplete);
        }
    }

    public void clearDisconnected() {
        this.mCT.clearDisconnected();
    }

    public boolean canTransfer() {
        if (isPhoneTypeGsm()) {
            return this.mCT.canTransfer();
        }
        loge("canTransfer: not possible in CDMA");
        return false;
    }

    public void explicitCallTransfer() {
        if (isPhoneTypeGsm()) {
            this.mCT.explicitCallTransfer();
        } else {
            loge("explicitCallTransfer: not possible in CDMA");
        }
    }

    public GsmCdmaCall getForegroundCall() {
        return this.mCT.mForegroundCall;
    }

    public GsmCdmaCall getBackgroundCall() {
        return this.mCT.mBackgroundCall;
    }

    public Call getRingingCall() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone == null || !imsPhone.getRingingCall().isRinging()) {
            return this.mCT.mRingingCall;
        }
        return imsPhone.getRingingCall();
    }

    private boolean handleCallDeflectionIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (getRingingCall().getState() != Call.State.IDLE) {
            if (DBG) {
                logd("MmiCode 0: rejectCall");
            }
            try {
                this.mCT.rejectCall();
            } catch (CallStateException e) {
                if (DBG) {
                    Rlog.d(LOG_TAG, "reject failed", e);
                }
                notifySuppServiceFailed(SuppService.REJECT);
            }
        } else if (getBackgroundCall().getState() != Call.State.IDLE) {
            if (DBG) {
                logd("MmiCode 0: hangupWaitingOrBackground");
            }
            this.mCT.hangupWaitingOrBackground();
        }
        return true;
    }

    private boolean handleCallWaitingIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        GsmCdmaCall call = getForegroundCall();
        if (len > 1) {
            try {
                int callIndex = dialString.charAt(1) - 48;
                if (callIndex >= 1 && callIndex <= 19) {
                    if (DBG) {
                        logd("MmiCode 1: hangupConnectionByIndex " + callIndex);
                    }
                    this.mCT.hangupConnectionByIndex(call, callIndex);
                }
            } catch (CallStateException e) {
                if (DBG) {
                    Rlog.d(LOG_TAG, "hangup failed", e);
                }
                notifySuppServiceFailed(SuppService.HANGUP);
            }
        } else if (call.getState() != Call.State.IDLE) {
            if (DBG) {
                logd("MmiCode 1: hangup foreground");
            }
            this.mCT.hangup(call);
        } else {
            if (DBG) {
                logd("MmiCode 1: switchWaitingOrHoldingAndActive");
            }
            this.mCT.switchWaitingOrHoldingAndActive();
        }
        return true;
    }

    private boolean handleCallHoldIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        GsmCdmaCall call = getForegroundCall();
        if (len > 1) {
            try {
                int callIndex = dialString.charAt(1) - 48;
                GsmCdmaConnection conn = this.mCT.getConnectionByIndex(call, callIndex);
                if (conn == null || callIndex < 1 || callIndex > 19) {
                    if (DBG) {
                        logd("separate: invalid call index " + callIndex);
                    }
                    notifySuppServiceFailed(SuppService.SEPARATE);
                } else {
                    if (DBG) {
                        logd("MmiCode 2: separate call " + callIndex);
                    }
                    this.mCT.separate(conn);
                }
            } catch (CallStateException e) {
                if (DBG) {
                    Rlog.d(LOG_TAG, "separate failed", e);
                }
                notifySuppServiceFailed(SuppService.SEPARATE);
            }
        } else {
            try {
                if (getRingingCall().getState() != Call.State.IDLE) {
                    if (DBG) {
                        logd("MmiCode 2: accept ringing call");
                    }
                    this.mCT.acceptCall();
                } else {
                    if (DBG) {
                        logd("MmiCode 2: switchWaitingOrHoldingAndActive");
                    }
                    this.mCT.switchWaitingOrHoldingAndActive();
                }
            } catch (CallStateException e2) {
                if (DBG) {
                    Rlog.d(LOG_TAG, "switch failed", e2);
                }
                notifySuppServiceFailed(SuppService.SWITCH);
            }
        }
        return true;
    }

    private boolean handleMultipartyIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (DBG) {
            logd("MmiCode 3: merge calls");
        }
        conference();
        return true;
    }

    private boolean handleEctIncallSupplementaryService(String dialString) {
        if (dialString.length() != 1) {
            return false;
        }
        if (DBG) {
            logd("MmiCode 4: explicit call transfer");
        }
        explicitCallTransfer();
        return true;
    }

    private boolean handleCcbsIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        Rlog.i(LOG_TAG, "MmiCode 5: CCBS not supported!");
        notifySuppServiceFailed(SuppService.UNKNOWN);
        return true;
    }

    public boolean handleInCallMmiCommands(String dialString) throws CallStateException {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && imsPhone.getServiceState().getState() == 0 && !isInCall()) {
                return imsPhone.handleInCallMmiCommands(dialString);
            }
            if (!isInCall() || TextUtils.isEmpty(dialString)) {
                return false;
            }
            boolean result = false;
            switch (dialString.charAt(0)) {
                case '0':
                    result = handleCallDeflectionIncallSupplementaryService(dialString);
                    break;
                case '1':
                    result = handleCallWaitingIncallSupplementaryService(dialString);
                    break;
                case '2':
                    result = handleCallHoldIncallSupplementaryService(dialString);
                    break;
                case '3':
                    result = handleMultipartyIncallSupplementaryService(dialString);
                    break;
                case '4':
                    result = handleEctIncallSupplementaryService(dialString);
                    break;
                case '5':
                    result = handleCcbsIncallSupplementaryService(dialString);
                    break;
            }
            return result;
        }
        loge("method handleInCallMmiCommands is NOT supported in CDMA!");
        return false;
    }

    public boolean isInCall() {
        Call.State foregroundCallState = getForegroundCall().getState();
        Call.State backgroundCallState = getBackgroundCall().getState();
        Call.State ringingCallState = getRingingCall().getState();
        if (foregroundCallState.isAlive() || backgroundCallState.isAlive()) {
            return true;
        }
        return ringingCallState.isAlive();
    }

    protected boolean shallDialOnCircuitSwitch(Bundle extras) {
        return extras != null && extras.getInt(QtiCallConstants.EXTRA_CALL_DOMAIN, 0) == 1;
    }

    public Connection dial(String dialString, int videoState) throws CallStateException {
        return dial(dialString, null, videoState, null);
    }

    public Connection dial(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras) throws CallStateException {
        if (!OemConstant.isCallOutEnable(this)) {
            Rlog.d(LOG_TAG, "ctmm vo block");
            return null;
        } else if (!OemConstant.isCallOutEnableExp(this) && (isEmergencyNumber(dialString) ^ 1) != 0) {
            Rlog.d(LOG_TAG, "th device lock block");
            return null;
        } else if (isPhoneTypeGsm() || uusInfo == null) {
            boolean imsUseEnabled;
            boolean useImsForEmergency;
            boolean isUt;
            boolean isEmergency = isEmergencyNumber(dialString);
            Phone imsPhone = this.mImsPhone;
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            boolean alwaysTryImsForEmergencyCarrierConfig = configManager.getConfigForSubId(getSubId()).getBoolean("carrier_use_ims_first_for_emergency_bool");
            if (isImsUseEnabled() && imsPhone != null && ((imsPhone.isVolteEnabled() || imsPhone.isWifiCallingEnabled() || (imsPhone.isVideoEnabled() && VideoProfile.isVideo(videoState))) && imsPhone.getServiceState().getState() == 0)) {
                imsUseEnabled = shallDialOnCircuitSwitch(intentExtras) ^ 1;
            } else {
                imsUseEnabled = false;
            }
            int imsFeatureState = 0;
            if (imsPhone != null) {
                try {
                    imsFeatureState = ImsManager.getInstance(imsPhone.getContext(), imsPhone.getPhoneId()).getImsServiceStatus();
                } catch (ImsException e) {
                    Log.e(LOG_TAG, "Got ImsException for phoneId " + imsPhone.getPhoneId());
                }
            }
            if (imsPhone != null && imsFeatureState == 2 && isEmergency && alwaysTryImsForEmergencyCarrierConfig && ImsManager.getInstance(this.mContext, this.mPhoneId).isNonTtyOrTtyOnVolteEnabledForSlot()) {
                useImsForEmergency = imsPhone.isImsAvailable();
            } else {
                useImsForEmergency = false;
            }
            String dialPart = PhoneNumberUtils.extractNetworkPortionAlt(PhoneNumberUtils.stripSeparators(dialString));
            if (dialPart.startsWith(CharacterSets.MIMENAME_ANY_CHARSET) || dialPart.startsWith("#")) {
                isUt = dialPart.endsWith("#");
            } else {
                isUt = false;
            }
            int useImsForUt = imsPhone != null ? imsPhone.isUtEnabled() : false;
            String mImsi = getSubscriberId();
            String mOperatorNumeric = getOperatorNumeric();
            if (DBG) {
                logd("dial: mImsi = " + mImsi + " , mOperatorNumeric = " + mOperatorNumeric);
            }
            if (!(mImsi == null || mOperatorNumeric == null || ((!mImsi.startsWith("46692") || !mOperatorNumeric.startsWith("46692")) && (!mImsi.startsWith("46697") || !mOperatorNumeric.startsWith("46697"))))) {
                GsmMmiCode mmi = GsmMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(dialString), this, (UiccCardApplication) this.mUiccApplication.get(), null);
                logd("dial: dialing w/ mmi '" + mmi + "'...");
                if (mmi != null && mmi.isServiceCodeCallBarring()) {
                    logd("dial:call barring always use GSM in order to send passwd to modem");
                    useImsForUt = false;
                }
            }
            SystemProperties.set("gsm.oppo.operator.ringtone", String.valueOf(configManager.getConfigForSubId(getSubId()).getInt("oppo.operator.ringtone", 0)));
            if (!OemConstant.EXP_VERSION) {
                useImsForEmergency = false;
            }
            if (this.mCT.getState() != PhoneConstants.State.IDLE) {
                imsUseEnabled = false;
                useImsForEmergency = false;
                if (DBG) {
                    Rlog.d(LOG_TAG, "imsUseEnabled = false, useImsForEmergency = false, for gsm call exist ");
                }
            }
            if (DBG) {
                Boolean valueOf;
                Integer valueOf2;
                StringBuilder append = new StringBuilder().append("imsUseEnabled=").append(imsUseEnabled).append(", useImsForEmergency=").append(useImsForEmergency).append(", useImsForUt=").append(useImsForUt).append(", isUt=").append(isUt).append(", imsPhone=").append(imsPhone).append(", imsPhone.isVolteEnabled()=");
                if (imsPhone != null) {
                    valueOf = Boolean.valueOf(imsPhone.isVolteEnabled());
                } else {
                    valueOf = "N/A";
                }
                append = append.append(valueOf).append(", imsPhone.isVowifiEnabled()=");
                if (imsPhone != null) {
                    valueOf = Boolean.valueOf(imsPhone.isWifiCallingEnabled());
                } else {
                    valueOf = "N/A";
                }
                append = append.append(valueOf).append(", imsPhone.isVideoEnabled()=");
                if (imsPhone != null) {
                    valueOf = Boolean.valueOf(imsPhone.isVideoEnabled());
                } else {
                    valueOf = "N/A";
                }
                append = append.append(valueOf).append(", imsPhone.getServiceState().getState()=");
                if (imsPhone != null) {
                    valueOf2 = Integer.valueOf(imsPhone.getServiceState().getState());
                } else {
                    valueOf2 = "N/A";
                }
                logd(append.append(valueOf2).append(", imsphone feature state = ").append(imsFeatureState).toString());
            }
            checkWfcWifiOnlyModeBeforeDial();
            if (!imsUseEnabled) {
                videoState = 0;
                if (DBG) {
                    Rlog.d(LOG_TAG, "Change video state to audio_only");
                }
            }
            if ((imsUseEnabled && (isUt ^ 1) != 0) || ((isUt && useImsForUt == true) || useImsForEmergency)) {
                try {
                    if (DBG) {
                        logd("Trying IMS PS call");
                    }
                    return imsPhone.dial(dialString, uusInfo, videoState, intentExtras);
                } catch (CallStateException e2) {
                    if (DBG) {
                        logd("IMS PS call exception " + e2 + "imsUseEnabled =" + imsUseEnabled + ", imsPhone =" + imsPhone);
                    }
                    if (Phone.CS_FALLBACK.equals(e2.getMessage()) || isEmergency) {
                        logi("IMS call failed with Exception: " + e2.getMessage() + ". Falling back " + "to CS.");
                    } else {
                        CallStateException ce = new CallStateException(e2.getMessage());
                        ce.setStackTrace(e2.getStackTrace());
                        throw ce;
                    }
                }
            }
            if (this.mSST == null || this.mSST.mSS.getState() != 1 || this.mSST.mSS.getDataRegState() == 0 || (isEmergency ^ 1) == 0) {
                if (!(this.mSST == null || this.mSST.mSS.getState() != 3 || (VideoProfile.isVideo(videoState) ^ 1) == 0 || (isEmergency ^ 1) == 0)) {
                    if (!isUt) {
                        useImsForUt = 0;
                    }
                    if ((useImsForUt ^ 1) != 0) {
                        throw new CallStateException(2, "cannot dial voice call in airplane mode");
                    }
                }
                if (this.mSST != null && this.mSST.mSS.getState() == 1) {
                    int isLte;
                    if (this.mSST.mSS.getDataRegState() == 0) {
                        isLte = ServiceState.isLte(this.mSST.mSS.getRilDataRadioTechnology());
                    } else {
                        isLte = 0;
                    }
                    if (!((isLte ^ 1) == 0 || (VideoProfile.isVideo(videoState) ^ 1) == 0 || (isEmergency ^ 1) == 0)) {
                        throw new CallStateException(1, "cannot dial voice call in out of service");
                    }
                }
                if (DBG) {
                    logd("Trying (non-IMS) CS call");
                }
                if (isPhoneTypeGsm()) {
                    return dialInternal(dialString, null, 0, intentExtras);
                }
                return dialInternal(dialString, null, videoState, intentExtras);
            }
            throw new CallStateException("cannot dial in current state");
        } else {
            throw new CallStateException("Sending UUS information NOT supported in CDMA!");
        }
    }

    public boolean isNotificationOfWfcCallRequired(String dialString) {
        boolean shouldNotifyInternationalCallOnWfc;
        PersistableBundle config = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId());
        if (config != null) {
            shouldNotifyInternationalCallOnWfc = config.getBoolean("notify_international_call_on_wfc_bool");
        } else {
            shouldNotifyInternationalCallOnWfc = false;
        }
        if (!shouldNotifyInternationalCallOnWfc) {
            return false;
        }
        boolean shouldConfirmCall;
        Phone imsPhone = this.mImsPhone;
        boolean isEmergency = PhoneNumberUtils.isEmergencyNumber(getSubId(), dialString);
        if (!isImsUseEnabled() || imsPhone == null || (imsPhone.isVolteEnabled() ^ 1) == 0 || !imsPhone.isWifiCallingEnabled() || (isEmergency ^ 1) == 0) {
            shouldConfirmCall = false;
        } else {
            shouldConfirmCall = PhoneNumberUtils.isInternationalNumber(dialString, getCountryIso());
        }
        return shouldConfirmCall;
    }

    protected Connection dialInternal(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras) throws CallStateException {
        return dialInternal(dialString, uusInfo, videoState, intentExtras, null);
    }

    protected Connection dialInternal(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras, ResultReceiver wrappedCallback) throws CallStateException {
        String newDialString = PhoneNumberUtils.stripSeparators(dialString);
        if (!isPhoneTypeGsm()) {
            return this.mCT.dial(newDialString);
        }
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        GsmMmiCode mmi = GsmMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), this, (UiccCardApplication) this.mUiccApplication.get(), wrappedCallback);
        if (DBG) {
            logd("dialInternal: dialing w/ mmi '" + mmi + "'...");
        }
        if (mmi == null) {
            return this.mCT.dial(newDialString, uusInfo, intentExtras);
        }
        if (mmi.isTemporaryModeCLIR()) {
            return this.mCT.dial(mmi.mDialingNumber, mmi.getCLIRMode(), uusInfo, intentExtras);
        }
        this.mPendingMMIs.add(mmi);
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        mmi.processCode();
        return null;
    }

    public boolean handlePinMmi(String dialString) {
        MmiCode mmi;
        if (isPhoneTypeGsm()) {
            mmi = GsmMmiCode.newFromDialString(dialString, this, (UiccCardApplication) this.mUiccApplication.get());
        } else {
            mmi = CdmaMmiCode.newFromDialString(dialString, this, (UiccCardApplication) this.mUiccApplication.get());
        }
        if (mmi == null || !mmi.isPinPukCommand()) {
            loge("Mmi is null or unrecognized!");
            return false;
        }
        this.mPendingMMIs.add(mmi);
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        try {
            mmi.processCode();
        } catch (CallStateException e) {
        }
        return true;
    }

    private void sendUssdResponse(String ussdRequest, CharSequence message, int returnCode, ResultReceiver wrappedCallback) {
        UssdResponse response = new UssdResponse(ussdRequest, message);
        Bundle returnData = new Bundle();
        returnData.putParcelable("USSD_RESPONSE", response);
        wrappedCallback.send(returnCode, returnData);
    }

    public boolean handleUssdRequest(String ussdRequest, ResultReceiver wrappedCallback) {
        if (!isPhoneTypeGsm() || this.mPendingMMIs.size() > 0) {
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            return true;
        }
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null && (imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
            try {
                logd("handleUssdRequest: attempting over IMS");
                return imsPhone.handleUssdRequest(ussdRequest, wrappedCallback);
            } catch (CallStateException cse) {
                if (!Phone.CS_FALLBACK.equals(cse.getMessage())) {
                    return false;
                }
                logd("handleUssdRequest: fallback to CS required");
            }
        }
        try {
            dialInternal(ussdRequest, null, 0, null, wrappedCallback);
            return true;
        } catch (Exception e) {
            logd("handleUssdRequest: exception" + e);
            return false;
        }
    }

    public void sendUssdResponse(String ussdMessge) {
        if (isPhoneTypeGsm()) {
            GsmMmiCode mmi = GsmMmiCode.newFromUssdUserInput(ussdMessge, this, (UiccCardApplication) this.mUiccApplication.get());
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            mmi.sendUssd(ussdMessge);
            return;
        }
        loge("sendUssdResponse: not possible in CDMA");
    }

    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            loge("sendDtmf called with invalid character '" + c + "'");
        } else if (this.mCT.mState == PhoneConstants.State.OFFHOOK) {
            this.mCi.sendDtmf(c, null);
        }
    }

    public void startDtmf(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            this.mCi.startDtmf(c, null);
        } else {
            loge("startDtmf called with invalid character '" + c + "'");
        }
    }

    public void stopDtmf() {
        this.mCi.stopDtmf(null);
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("[GsmCdmaPhone] sendBurstDtmf() is a CDMA method");
            return;
        }
        boolean check = true;
        for (int itr = 0; itr < dtmfString.length(); itr++) {
            if (!PhoneNumberUtils.is12Key(dtmfString.charAt(itr))) {
                Rlog.e(LOG_TAG, "sendDtmf called with invalid character '" + dtmfString.charAt(itr) + "'");
                check = false;
                break;
            }
        }
        if (this.mCT.mState == PhoneConstants.State.OFFHOOK && check) {
            this.mCi.sendBurstDtmf(dtmfString, on, off, onComplete);
        }
    }

    public void addParticipant(String dialString) throws CallStateException {
        Phone imsPhone = this.mImsPhone;
        boolean imsUseEnabled = (isImsUseEnabled() && imsPhone != null && (imsPhone.isVolteEnabled() || imsPhone.isWifiCallingEnabled() || imsPhone.isVideoEnabled())) ? imsPhone.getServiceState().getState() == 0 : false;
        if (imsUseEnabled) {
            try {
                logd("addParticipant :: Trying to add participant in IMS call");
                imsPhone.addParticipant(dialString);
                return;
            } catch (CallStateException e) {
                loge("addParticipant :: IMS PS call exception " + e);
                return;
            }
        }
        loge("addParticipant :: IMS is disabled so unable to add participant with IMS call");
    }

    public void setRadioPower(boolean power) {
        this.mSST.setRadioPower(power);
    }

    private void storeVoiceMailNumber(String number) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        if (isPhoneTypeGsm()) {
            editor.putString(VM_NUMBER + getPhoneId(), number);
            editor.apply();
            setVmSimImsi(getSubscriberId());
            return;
        }
        editor.putString(VM_NUMBER_CDMA + getPhoneId(), number);
        editor.apply();
    }

    public String getVoiceMailNumber() {
        String number;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            number = r != null ? r.getVoiceMailNumber() : SpnOverride.MVNO_TYPE_NONE;
            if (TextUtils.isEmpty(number)) {
                number = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(VM_NUMBER + getPhoneId(), null);
            }
        } else {
            number = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(VM_NUMBER_CDMA + getPhoneId(), null);
        }
        if (TextUtils.isEmpty(number)) {
            PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
            if (b != null) {
                String defaultVmNumber = b.getString("default_vm_number_string");
                if (!TextUtils.isEmpty(defaultVmNumber)) {
                    number = defaultVmNumber;
                }
            }
        }
        if (!isPhoneTypeGsm() && TextUtils.isEmpty(number) && getContext().getResources().getBoolean(17957042)) {
            return getLine1Number();
        }
        return number;
    }

    private String getVmSimImsi() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getString(VM_SIM_IMSI + getPhoneId(), null);
    }

    private void setVmSimImsi(String imsi) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putString(VM_SIM_IMSI + getPhoneId(), imsi);
        editor.apply();
    }

    public String getVoiceMailAlphaTag() {
        String ret = SpnOverride.MVNO_TYPE_NONE;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            ret = r != null ? r.getVoiceMailAlphaTag() : SpnOverride.MVNO_TYPE_NONE;
        }
        if (ret == null || ret.length() == 0) {
            return this.mContext.getText(17039364).toString();
        }
        return ret;
    }

    public String getDeviceId() {
        boolean force_imei = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId()).getBoolean("force_imei_bool");
        if (isPhoneTypeGsm() || force_imei) {
            return getImei();
        }
        String id = getMeid();
        if (id == null || id.matches("^0*$")) {
            loge("getDeviceId(): MEID is not initialized use ESN");
            id = getEsn();
        }
        return id;
    }

    public String getDeviceSvn() {
        if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
            return this.mImeiSv;
        }
        loge("getDeviceSvn(): return 0");
        return "0";
    }

    public IsimRecords getIsimRecords() {
        return this.mIsimUiccRecords;
    }

    public String getImei() {
        return this.mImei;
    }

    public String getEsn() {
        if (!isPhoneTypeGsm()) {
            return this.mEsn;
        }
        loge("[GsmCdmaPhone] getEsn() is a CDMA method");
        return "0";
    }

    public String getMeid() {
        Rlog.v(LOG_TAG, "getMeid is " + this.mMeid);
        if (TextUtils.isEmpty(this.mMeid)) {
            return "0";
        }
        return this.mMeid;
    }

    public String getNai() {
        IccRecords r = this.mUiccController.getIccRecords(this.mPhoneId, 2);
        if (Log.isLoggable(LOG_TAG, 2)) {
            Rlog.v(LOG_TAG, "IccRecords is " + r);
        }
        if (r != null) {
            return r.getNAI();
        }
        return null;
    }

    public String getSubscriberId() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getIMSI();
            }
            return str;
        } else if (isPhoneTypeCdma()) {
            return this.mSST.getImsi();
        } else {
            return this.mSimRecords != null ? this.mSimRecords.getIMSI() : SpnOverride.MVNO_TYPE_NONE;
        }
    }

    public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int keyType) {
        return CarrierInfoManager.getCarrierInfoForImsiEncryption(keyType, this.mContext);
    }

    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo) {
        CarrierInfoManager.setCarrierInfoForImsiEncryption(imsiEncryptionInfo, this.mContext);
    }

    public String getGroupIdLevel1() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getGid1();
            }
            return str;
        } else if (isPhoneTypeCdma()) {
            loge("GID1 is not available in CDMA");
            return null;
        } else {
            return this.mSimRecords != null ? this.mSimRecords.getGid1() : SpnOverride.MVNO_TYPE_NONE;
        }
    }

    public String getGroupIdLevel2() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getGid2();
            }
            return str;
        } else if (isPhoneTypeCdma()) {
            loge("GID2 is not available in CDMA");
            return null;
        } else {
            return this.mSimRecords != null ? this.mSimRecords.getGid2() : SpnOverride.MVNO_TYPE_NONE;
        }
    }

    public String getLine1Number() {
        String str = null;
        if (!isPhoneTypeGsm()) {
            return this.mSST.getMdnNumber();
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            str = r.getMsisdnNumber();
        }
        return str;
    }

    public String getCdmaPrlVersion() {
        return this.mSST.getPrlVersion();
    }

    public String getCdmaMin() {
        return this.mSST.getCdmaMin();
    }

    public boolean isMinInfoReady() {
        return this.mSST.isMinInfoReady();
    }

    public String getMsisdn() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getMsisdnNumber();
            }
            return str;
        } else if (isPhoneTypeCdmaLte()) {
            if (this.mSimRecords != null) {
                str = this.mSimRecords.getMsisdnNumber();
            }
            return str;
        } else {
            loge("getMsisdn: not expected on CDMA");
            return null;
        }
    }

    public String getLine1AlphaTag() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getMsisdnAlphaTag();
            }
            return str;
        }
        loge("getLine1AlphaTag: not possible in CDMA");
        return null;
    }

    public boolean setLine1Number(String alphaTag, String number, Message onComplete) {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r == null) {
                return false;
            }
            r.setMsisdnNumber(alphaTag, number, onComplete);
            return true;
        }
        loge("setLine1Number: not possible in CDMA");
        return false;
    }

    public void setVoiceMailNumber(String alphaTag, String voiceMailNumber, Message onComplete) {
        this.mVmNumber = voiceMailNumber;
        Message resp = obtainMessage(20, 0, 0, onComplete);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.setVoiceMailNumber(alphaTag, this.mVmNumber, resp);
        }
    }

    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    public String getSystemProperty(String property, String defValue) {
        if (!isPhoneTypeGsm() && !isPhoneTypeCdmaLte() && !isPhoneTypeCdma()) {
            return super.getSystemProperty(property, defValue);
        }
        if (getUnitTestMode()) {
            return null;
        }
        return TelephonyManager.getTelephonyProperty(this.mPhoneId, property, defValue);
    }

    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        switch (commandInterfaceCFAction) {
            case 0:
            case 1:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    private boolean isImsUtEnabledOverCdma() {
        if (!isPhoneTypeCdmaLte() || this.mImsPhone == null) {
            return false;
        }
        return this.mImsPhone.isUtEnabled();
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        getCallForwardingOption(commandInterfaceCFReason, 0, onComplete);
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, int commandInterfaceServiceClass, Message onComplete) {
        if (isPhoneTypeGsm() || isImsUtEnabledOverCdma()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && (imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                imsPhone.getCallForwardingOption(commandInterfaceCFReason, commandInterfaceServiceClass, onComplete);
            } else if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                Message resp;
                if (DBG) {
                    logd("requesting call forwarding query.");
                }
                if (commandInterfaceCFReason == 0) {
                    resp = obtainMessage(13, onComplete);
                } else {
                    resp = onComplete;
                }
                this.mCi.queryCallForwardStatus(commandInterfaceCFReason, commandInterfaceServiceClass, null, resp);
            }
        } else {
            loge("getCallForwardingOption: not possible in CDMA without IMS");
            loge("getCallForwardingOption: send error response");
            if (onComplete != null) {
                loge("getCallForwardingOption: onComplete");
                AsyncResult.forMessage(onComplete, null, new CommandException(Error.GENERIC_FAILURE));
                onComplete.sendToTarget();
            }
        }
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, 1, timerSeconds, onComplete);
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int commandInterfaceServiceClass, int timerSeconds, Message onComplete) {
        if (isPhoneTypeGsm() || isImsUtEnabledOverCdma()) {
            String operator = SystemProperties.get("ro.oppo.operator", "US");
            String region = SystemProperties.get("persist.sys.oppo.region", "US");
            Rlog.d(LOG_TAG, "operator and region : " + operator + " " + region);
            if (operator.equals("SINGTEL") && region.equals("SG") && dialingNumber != null && (dialingNumber.startsWith("+") ^ 1) != 0) {
                dialingNumber = "+65" + dialingNumber;
                Rlog.d(LOG_TAG, "Singtel version add +65!");
            }
            if (operator.equals("VODAFONE") && region.equals("AU") && dialingNumber != null && dialingNumber.startsWith("0")) {
                dialingNumber = "+61" + dialingNumber.substring(1, dialingNumber.length());
                Rlog.d(LOG_TAG, "AU VDF version force to use +61 international format!");
            }
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && (imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                imsPhone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, commandInterfaceServiceClass, timerSeconds, onComplete);
            } else if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                Message resp;
                if (commandInterfaceCFReason == 0) {
                    resp = obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cfu(dialingNumber, onComplete));
                } else {
                    resp = onComplete;
                }
                this.mCi.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, commandInterfaceServiceClass, dialingNumber, timerSeconds, resp);
            }
        } else {
            loge("setCallForwardingOption: not possible in CDMA without IMS");
        }
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                this.mCi.getCLIR(onComplete);
            } else {
                imsPhone.getOutgoingCallerIdDisplay(onComplete);
                return;
            }
        }
        loge("getOutgoingCallerIdDisplay: not possible in CDMA");
    }

    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode, Message onComplete) {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                this.mCi.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
            } else {
                imsPhone.setOutgoingCallerIdDisplay(commandInterfaceCLIRMode, onComplete);
                return;
            }
        }
        loge("setOutgoingCallerIdDisplay: not possible in CDMA");
    }

    public void getCallWaiting(Message onComplete) {
        if (isPhoneTypeGsm() || isImsUtEnabledOverCdma()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                this.mCi.queryCallWaiting(0, onComplete);
            } else {
                imsPhone.getCallWaiting(onComplete);
                return;
            }
        }
        this.mCi.queryCallWaiting(1, onComplete);
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        if (isPhoneTypeGsm() || isImsUtEnabledOverCdma()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                this.mCi.setCallWaiting(enable, 1, onComplete);
            } else {
                imsPhone.setCallWaiting(enable, onComplete);
                return;
            }
        }
        loge("method setCallWaiting is NOT supported in CDMA without IMS!");
    }

    public void getAvailableNetworks(Message response) {
        if (isManualSelectNetworksAllowed()) {
            this.mCi.getAvailableNetworks(response);
            return;
        }
        loge("getAvailableNetworks: not possible in CDMA");
        if (response != null) {
            AsyncResult.forMessage(response).exception = new CommandException(Error.REQUEST_NOT_SUPPORTED);
            response.sendToTarget();
        }
    }

    public void startNetworkScan(NetworkScanRequest nsr, Message response) {
        this.mCi.startNetworkScan(nsr, response);
    }

    public void stopNetworkScan(Message response) {
        this.mCi.stopNetworkScan(response);
    }

    public void getNeighboringCids(Message response, WorkSource workSource) {
        if (isPhoneTypeGsm()) {
            this.mCi.getNeighboringCids(response, workSource);
        } else if (response != null) {
            AsyncResult.forMessage(response).exception = new CommandException(Error.REQUEST_NOT_SUPPORTED);
            response.sendToTarget();
        }
    }

    public void setTTYMode(int ttyMode, Message onComplete) {
        super.setTTYMode(ttyMode, onComplete);
        if (this.mImsPhone != null) {
            this.mImsPhone.setTTYMode(ttyMode, onComplete);
        }
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        if (this.mImsPhone != null) {
            this.mImsPhone.setUiTTYMode(uiTtyMode, onComplete);
        }
    }

    public void setMute(boolean muted) {
        this.mCT.setMute(muted);
    }

    public boolean getMute() {
        return this.mCT.getMute();
    }

    public void getDataCallList(Message response) {
        this.mCi.getDataCallList(response);
    }

    public void updateServiceLocation() {
        this.mSST.enableSingleLocationUpdate();
    }

    public void enableLocationUpdates() {
        this.mSST.enableLocationUpdates();
    }

    public void disableLocationUpdates() {
        this.mSST.disableLocationUpdates();
    }

    public boolean getDataRoamingEnabled() {
        return this.mDcTracker.getDataRoamingEnabled();
    }

    public void setDataRoamingEnabled(boolean enable) {
        this.mDcTracker.setDataRoamingEnabledByUser(enable);
    }

    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
        this.mCi.registerForCdmaOtaProvision(h, what, obj);
    }

    public void unregisterForCdmaOtaStatusChange(Handler h) {
        this.mCi.unregisterForCdmaOtaProvision(h);
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        this.mSST.registerForSubscriptionInfoReady(h, what, obj);
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mSST.unregisterForSubscriptionInfoReady(h);
    }

    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
        this.mEcmExitRespRegistrant = new Registrant(h, what, obj);
    }

    public void unsetOnEcbModeExitResponse(Handler h) {
        this.mEcmExitRespRegistrant.clear();
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
        this.mCT.registerForCallWaiting(h, what, obj);
    }

    public void unregisterForCallWaiting(Handler h) {
        this.mCT.unregisterForCallWaiting(h);
    }

    public boolean getDataEnabled() {
        return this.mDcTracker.getDataEnabled();
    }

    public void setDataEnabled(boolean enable) {
        this.mDcTracker.setDataEnabled(enable);
    }

    public void onMMIDone(MmiCode mmi) {
        if (this.mPendingMMIs.remove(mmi) || (isPhoneTypeGsm() && (mmi.isUssdRequest() || ((GsmMmiCode) mmi).isSsInfo()))) {
            ResultReceiver receiverCallback = mmi.getUssdCallbackReceiver();
            if (receiverCallback != null) {
                Rlog.i(LOG_TAG, "onMMIDone: invoking callback: " + mmi);
                sendUssdResponse(mmi.getDialString(), mmi.getMessage(), mmi.getState() == MmiCode.State.COMPLETE ? 100 : -1, receiverCallback);
                return;
            }
            Rlog.i(LOG_TAG, "onMMIDone: notifying registrants: " + mmi);
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            return;
        }
        Rlog.i(LOG_TAG, "onMMIDone: invalid response or already handled; ignoring: " + mmi);
    }

    public boolean supports3gppCallForwardingWhileRoaming() {
        PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfig();
        if (b != null) {
            return b.getBoolean("support_3gpp_call_forwarding_while_roaming_bool", true);
        }
        return true;
    }

    private void onNetworkInitiatedUssd(MmiCode mmi) {
        Rlog.v(LOG_TAG, "onNetworkInitiatedUssd: mmi=" + mmi);
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
    }

    private void onIncomingUSSD(int ussdMode, String ussdMessage) {
        if (!isPhoneTypeGsm()) {
            loge("onIncomingUSSD: not expected on GSM");
        }
        boolean isUssdRequest = ussdMode == 1;
        boolean isUssdError = ussdMode != 0 ? ussdMode != 1 : false;
        boolean isUssdRelease = ussdMode == 2;
        GsmMmiCode gsmMmiCode = null;
        int s = this.mPendingMMIs.size();
        for (int i = 0; i < s; i++) {
            if (((GsmMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                gsmMmiCode = (GsmMmiCode) this.mPendingMMIs.get(i);
                break;
            }
        }
        if (gsmMmiCode != null) {
            if (isUssdRelease) {
                gsmMmiCode.onUssdRelease();
            } else if (isUssdError) {
                gsmMmiCode.onUssdFinishedError();
            } else {
                gsmMmiCode.onUssdFinished(ussdMessage, isUssdRequest);
            }
        } else if (!isUssdError && ussdMessage != null) {
            onNetworkInitiatedUssd(GsmMmiCode.newNetworkInitiatedUssd(ussdMessage, isUssdRequest, this, (UiccCardApplication) this.mUiccApplication.get()));
        }
    }

    private void syncClirSetting() {
        int clirSetting = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(Phone.CLIR_KEY + getPhoneId(), -1);
        Rlog.i(LOG_TAG, "syncClirSetting: clir_key" + getPhoneId() + "=" + clirSetting);
        if (clirSetting >= 0) {
            this.mCi.setCLIR(clirSetting, null);
        }
    }

    private void handleRadioAvailable() {
        this.mCi.getBasebandVersion(obtainMessage(6));
        this.mCi.getDeviceIdentity(obtainMessage(21));
        this.mCi.getRadioCapability(obtainMessage(35));
        startLceAfterRadioIsAvailable();
    }

    private void handleRadioOn() {
        this.mCi.getVoiceRadioTechnology(obtainMessage(40));
        if (!isPhoneTypeGsm()) {
            this.mCdmaSubscriptionSource = this.mCdmaSSM.getCdmaSubscriptionSource();
        }
        setPreferredNetworkTypeIfSimLoaded();
        if (this.mImsPhone != null && this.mImsPhone.getServiceState().getState() == 3) {
            Log.i(LOG_TAG, "setting radio state out of service from power off ");
            this.mImsPhone.getServiceState().setState(1);
        }
    }

    private void handleRadioOffOrNotAvailable() {
        if (isPhoneTypeGsm()) {
            for (int i = this.mPendingMMIs.size() - 1; i >= 0; i--) {
                if (((GsmMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                    ((GsmMmiCode) this.mPendingMMIs.get(i)).onUssdFinishedError();
                }
            }
        }
        this.mRadioOffOrNotAvailableRegistrants.notifyRegistrants();
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        Message onComplete;
        switch (msg.what) {
            case 1:
                handleRadioAvailable();
                return;
            case 2:
                logd("Event EVENT_SSN Received");
                if (isPhoneTypeGsm()) {
                    ar = (AsyncResult) msg.obj;
                    SuppServiceNotification not = ar.result;
                    this.mSsnRegistrants.notifyRegistrants(ar);
                    return;
                }
                return;
            case 3:
                updateCurrentCarrierInProvider();
                String imsi = getVmSimImsi();
                String imsiFromSIM = getSubscriberId();
                if (!((isPhoneTypeGsm() && imsi == null) || imsiFromSIM == null || (imsiFromSIM.equals(imsi) ^ 1) == 0)) {
                    storeVoiceMailNumber(null);
                    setVmSimImsi(null);
                    setVideoCallForwardingPreference(false);
                }
                String imsiNew = getVmSimImsi();
                logd("imsiNew = " + imsiNew + " imsiFromSIM = " + imsiFromSIM);
                String defaultVM = getDefaultVMByImsi(imsiFromSIM);
                if (imsiNew == null && defaultVM != null) {
                    storeVoiceMailNumber(defaultVM);
                    setVmSimImsi(imsiFromSIM);
                }
                this.mSimRecordsLoadedRegistrants.notifyRegistrants();
                return;
            case 5:
                logd("Event EVENT_RADIO_ON Received");
                handleRadioOn();
                return;
            case 6:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    if (DBG) {
                        logd("Baseband version: " + ar.result);
                    }
                    TelephonyManager.from(this.mContext).setBasebandVersionForPhone(getPhoneId(), (String) ar.result);
                    return;
                }
                return;
            case 7:
                String[] ussdResult = (String[]) ((AsyncResult) msg.obj).result;
                if (ussdResult.length > 1) {
                    try {
                        onIncomingUSSD(Integer.parseInt(ussdResult[0]), ussdResult[1]);
                        return;
                    } catch (NumberFormatException e) {
                        Rlog.w(LOG_TAG, "error parsing USSD");
                        return;
                    }
                }
                return;
            case 8:
                logd("Event EVENT_RADIO_OFF_OR_NOT_AVAILABLE Received");
                handleRadioOffOrNotAvailable();
                return;
            case 9:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mImei = (String) ar.result;
                    return;
                }
                return;
            case 10:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mImeiSv = (String) ar.result;
                    return;
                }
                return;
            case 12:
                ar = (AsyncResult) msg.obj;
                IccRecords r = (IccRecords) this.mIccRecords.get();
                Cfu cfu = ar.userObj;
                if (ar.exception == null && r != null) {
                    setVoiceCallForwardingFlag(1, msg.arg1 == 1, cfu.mSetCfNumber);
                }
                if (cfu.mOnComplete != null) {
                    AsyncResult.forMessage(cfu.mOnComplete, ar.result, ar.exception);
                    cfu.mOnComplete.sendToTarget();
                    return;
                }
                return;
            case 13:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    handleCfuQueryResult((CallForwardInfo[]) ar.result);
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            case 18:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    saveClirSetting(msg.arg1);
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            case 19:
                logd("Event EVENT_REGISTERED_TO_NETWORK Received");
                if (isPhoneTypeGsm()) {
                    syncClirSetting();
                    return;
                }
                return;
            case 20:
                ar = (AsyncResult) msg.obj;
                if ((isPhoneTypeGsm() && IccVmNotSupportedException.class.isInstance(ar.exception)) || (!isPhoneTypeGsm() && IccException.class.isInstance(ar.exception))) {
                    storeVoiceMailNumber(this.mVmNumber);
                    ar.exception = null;
                }
                onComplete = ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            case 21:
                ar = msg.obj;
                if (ar.exception == null) {
                    String[] respId = (String[]) ar.result;
                    this.mImei = respId[0];
                    this.mImeiSv = respId[1];
                    this.mEsn = respId[2];
                    this.mMeid = respId[3];
                    if (DBG) {
                        Rlog.v(LOG_TAG, "getMeid inited " + this.mMeid + "/" + this.mImei);
                        return;
                    }
                    return;
                }
                return;
            case 22:
                logd("Event EVENT_RUIM_RECORDS_LOADED Received");
                updateDataConnectionTracker();
                return;
            case 25:
                handleEnterEmergencyCallbackMode(msg);
                return;
            case 26:
                handleExitEmergencyCallbackMode(msg);
                return;
            case 27:
                logd("EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED");
                this.mCdmaSubscriptionSource = this.mCdmaSSM.getCdmaSubscriptionSource();
                return;
            case 28:
                ar = (AsyncResult) msg.obj;
                if (this.mSST.mSS.getIsManualSelection()) {
                    setNetworkSelectionModeAutomatic((Message) ar.result);
                    logd("SET_NETWORK_SELECTION_AUTOMATIC: set to automatic");
                    return;
                }
                logd("SET_NETWORK_SELECTION_AUTOMATIC: already automatic, ignore");
                return;
            case 29:
                processIccRecordEvents(((Integer) ((AsyncResult) msg.obj).result).intValue());
                return;
            case 35:
                ar = (AsyncResult) msg.obj;
                RadioCapability rc = ar.result;
                if (ar.exception != null) {
                    Rlog.d(LOG_TAG, "get phone radio capability fail, no need to change mRadioCapability");
                } else {
                    radioCapabilityUpdated(rc);
                }
                Rlog.d(LOG_TAG, "EVENT_GET_RADIO_CAPABILITY: phone rc: " + rc);
                return;
            case 36:
                ar = (AsyncResult) msg.obj;
                logd("Event EVENT_SS received");
                if (isPhoneTypeGsm()) {
                    new GsmMmiCode(this, (UiccCardApplication) this.mUiccApplication.get()).processSsData(ar);
                    return;
                }
                return;
            case 39:
            case 40:
                String what = msg.what == 39 ? "EVENT_VOICE_RADIO_TECH_CHANGED" : "EVENT_REQUEST_VOICE_RADIO_TECH_DONE";
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    loge(what + ": exception=" + ar.exception);
                    return;
                } else if (ar.result == null || ((int[]) ar.result).length == 0) {
                    loge(what + ": has no tech!");
                    return;
                } else {
                    int newVoiceTech = ((int[]) ar.result)[0];
                    logd(what + ": newVoiceTech=" + newVoiceTech);
                    phoneObjectUpdater(newVoiceTech);
                    return;
                }
            case 41:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null || ar.result == null) {
                    logd("Unexpected exception on EVENT_RIL_CONNECTED");
                    this.mRilVersion = -1;
                    return;
                }
                this.mRilVersion = ((Integer) ar.result).intValue();
                return;
            case 42:
                phoneObjectUpdater(msg.arg1);
                return;
            case 43:
                if (!this.mContext.getResources().getBoolean(17957040)) {
                    this.mCi.getVoiceRadioTechnology(obtainMessage(40));
                }
                if (getIccRecordsLoaded()) {
                    ImsManager.getInstance(this.mContext, this.mPhoneId).updateImsServiceConfigForSlot(true);
                } else {
                    logw("received EVENT_CARRIER_CONFIG_CHANGED but IccRecords are not loaded");
                }
                PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
                if (b != null) {
                    boolean broadcastEmergencyCallStateChanges = b.getBoolean("broadcast_emergency_call_state_changes_bool");
                    logd("broadcastEmergencyCallStateChanges = " + broadcastEmergencyCallStateChanges);
                    setBroadcastEmergencyCallStateChanges(broadcastEmergencyCallStateChanges);
                } else {
                    loge("didn't get broadcastEmergencyCallStateChanges from carrier config");
                }
                if (b != null) {
                    int config_cdma_roaming_mode = b.getInt("cdma_roaming_mode_int");
                    int current_cdma_roaming_mode = Global.getInt(getContext().getContentResolver(), "roaming_settings", -1);
                    switch (config_cdma_roaming_mode) {
                        case -1:
                            if (current_cdma_roaming_mode != config_cdma_roaming_mode) {
                                logd("cdma_roaming_mode is going to changed to " + current_cdma_roaming_mode);
                                setCdmaRoamingPreference(current_cdma_roaming_mode, obtainMessage(44));
                                break;
                            }
                            break;
                        case 0:
                        case 1:
                        case 2:
                            logd("cdma_roaming_mode is going to changed to " + config_cdma_roaming_mode);
                            setCdmaRoamingPreference(config_cdma_roaming_mode, obtainMessage(44));
                            break;
                    }
                    loge("Invalid cdma_roaming_mode settings: " + config_cdma_roaming_mode);
                } else {
                    loge("didn't get the cdma_roaming_mode changes from the carrier config.");
                }
                prepareEri();
                if (!isPhoneTypeGsm()) {
                    this.mSST.pollState();
                    return;
                }
                return;
            case 44:
                logd("cdma_roaming_mode change is done");
                return;
            case 45:
                logd("Event EVENT_MODEM_RESET Received isInEcm = " + isInEcm() + " isPhoneTypeGsm = " + isPhoneTypeGsm() + " mImsPhone = " + this.mImsPhone);
                if (!isInEcm()) {
                    return;
                }
                if (!isPhoneTypeGsm()) {
                    handleExitEmergencyCallbackMode(msg);
                    return;
                } else if (this.mImsPhone != null) {
                    this.mImsPhone.handleExitEmergencyCallbackMode();
                    return;
                } else {
                    return;
                }
            default:
                super.handleMessage(msg);
                return;
        }
    }

    public UiccCardApplication getUiccCardApplication() {
        if (isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
    }

    protected void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            UiccCardApplication newUiccApplication;
            this.mIccPhoneBookIntManager.SetIccCardTypeFromApp(this.mUiccController.getUiccCard(this.mPhoneId));
            if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
                newUiccApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 3);
                IsimUiccRecords newIsimUiccRecords = null;
                if (newUiccApplication != null) {
                    newIsimUiccRecords = (IsimUiccRecords) newUiccApplication.getIccRecords();
                    if (DBG) {
                        logd("New ISIM application found");
                    }
                }
                this.mIsimUiccRecords = newIsimUiccRecords;
            }
            if (this.mSimRecords != null) {
                this.mSimRecords.unregisterForRecordsLoaded(this);
            }
            if (isPhoneTypeCdmaLte() || isPhoneTypeCdma()) {
                newUiccApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
                SIMRecords sIMRecords = null;
                if (newUiccApplication != null) {
                    sIMRecords = (SIMRecords) newUiccApplication.getIccRecords();
                }
                this.mSimRecords = sIMRecords;
                if (this.mSimRecords != null) {
                    this.mSimRecords.registerForRecordsLoaded(this, 3, null);
                }
            } else {
                this.mSimRecords = null;
            }
            newUiccApplication = getUiccCardApplication();
            if (!isPhoneTypeGsm() && newUiccApplication == null) {
                logd("can't find 3GPP2 application; trying APP_FAM_3GPP");
                newUiccApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
            }
            UiccCardApplication app = (UiccCardApplication) this.mUiccApplication.get();
            if (app != newUiccApplication) {
                if (app != null) {
                    if (DBG) {
                        logd("Removing stale icc objects.");
                    }
                    if (this.mIccRecords.get() != null) {
                        unregisterForIccRecordEvents();
                        this.mIccPhoneBookIntManager.updateIccRecords(null);
                    }
                    this.mIccRecords.set(null);
                    this.mUiccApplication.set(null);
                }
                if (newUiccApplication != null) {
                    if (DBG) {
                        logd("New Uicc application found. type = " + newUiccApplication.getType());
                    }
                    this.mUiccApplication.set(newUiccApplication);
                    this.mIccRecords.set(newUiccApplication.getIccRecords());
                    logd("mIccRecords = " + this.mIccRecords);
                    registerForIccRecordEvents();
                    this.mIccPhoneBookIntManager.updateIccRecords((IccRecords) this.mIccRecords.get());
                }
            }
        }
    }

    public SIMRecords getSIMRecords() {
        return this.mSimRecords;
    }

    private void processIccRecordEvents(int eventCode) {
        switch (eventCode) {
            case 1:
                logi("processIccRecordEvents: EVENT_CFI");
                notifyCallForwardingIndicator();
                return;
            default:
                return;
        }
    }

    public boolean updateCurrentCarrierInProvider() {
        if (!isPhoneTypeGsm() && !isPhoneTypeCdmaLte() && !isPhoneTypeCdma()) {
            return true;
        }
        long currentDds = (long) SubscriptionManager.getDefaultDataSubscriptionId();
        String operatorNumeric = getOperatorNumeric();
        logd("updateCurrentCarrierInProvider: mSubId = " + getSubId() + " currentDds = " + currentDds + " operatorNumeric = " + operatorNumeric);
        if (!TextUtils.isEmpty(operatorNumeric) && ((long) getSubId()) == currentDds) {
            try {
                Uri uri = Uri.withAppendedPath(Carriers.CONTENT_URI, "current");
                ContentValues map = new ContentValues();
                map.put("numeric", operatorNumeric);
                this.mContext.getContentResolver().insert(uri, map);
                return true;
            } catch (SQLException e) {
                Rlog.e(LOG_TAG, "Can't store current operator", e);
            }
        }
        return false;
    }

    private boolean updateCurrentCarrierInProvider(String operatorNumeric) {
        if (isPhoneTypeCdma() || (isPhoneTypeCdmaLte() && this.mUiccController.getUiccCardApplication(this.mPhoneId, 1) == null)) {
            logd("CDMAPhone: updateCurrentCarrierInProvider called");
            if (!TextUtils.isEmpty(operatorNumeric)) {
                try {
                    Uri uri = Uri.withAppendedPath(Carriers.CONTENT_URI, "current");
                    ContentValues map = new ContentValues();
                    map.put("numeric", operatorNumeric);
                    logd("updateCurrentCarrierInProvider from system: numeric=" + operatorNumeric);
                    getContext().getContentResolver().insert(uri, map);
                    logd("update mccmnc=" + operatorNumeric);
                    MccTable.updateMccMncConfiguration(this.mContext, operatorNumeric, false);
                    return true;
                } catch (SQLException e) {
                    Rlog.e(LOG_TAG, "Can't store current operator", e);
                }
            }
            return false;
        }
        if (DBG) {
            logd("updateCurrentCarrierInProvider not updated X retVal=true");
        }
        return true;
    }

    private void handleCfuQueryResult(CallForwardInfo[] infos) {
        boolean z = false;
        if (((IccRecords) this.mIccRecords.get()) == null) {
            return;
        }
        if (infos == null || infos.length == 0) {
            setVoiceCallForwardingFlag(1, false, null);
            return;
        }
        int s = infos.length;
        for (int i = 0; i < s; i++) {
            if ((infos[i].serviceClass & 1) != 0) {
                if (infos[i].status == 1) {
                    z = true;
                }
                setVoiceCallForwardingFlag(1, z, infos[i].number);
                return;
            }
        }
    }

    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return this.mIccPhoneBookIntManager;
    }

    public void registerForEriFileLoaded(Handler h, int what, Object obj) {
        this.mEriFileLoadedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEriFileLoaded(Handler h) {
        this.mEriFileLoadedRegistrants.remove(h);
    }

    public void prepareEri() {
        if (this.mEriManager == null) {
            Rlog.e(LOG_TAG, "PrepareEri: Trying to access stale objects");
            return;
        }
        this.mEriManager.loadEriFile();
        if (this.mEriManager.isEriFileLoaded()) {
            logd("ERI read, notify registrants");
            this.mEriFileLoadedRegistrants.notifyRegistrants();
        }
    }

    public boolean isEriFileLoaded() {
        return this.mEriManager.isEriFileLoaded();
    }

    public void activateCellBroadcastSms(int activate, Message response) {
        loge("[GsmCdmaPhone] activateCellBroadcastSms() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    public void getCellBroadcastSmsConfig(Message response) {
        loge("[GsmCdmaPhone] getCellBroadcastSmsConfig() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        loge("[GsmCdmaPhone] setCellBroadcastSmsConfig() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    public boolean needsOtaServiceProvisioning() {
        boolean z = false;
        if (isPhoneTypeGsm()) {
            return false;
        }
        if (this.mSST.getOtasp() != 3) {
            z = true;
        }
        return z;
    }

    public boolean isCspPlmnEnabled() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        return r != null ? r.isCspPlmnEnabled() : false;
    }

    public boolean shouldForceAutoNetworkSelect() {
        int nwMode = Phone.PREFERRED_NT_MODE;
        int subId = getSubId();
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return false;
        }
        nwMode = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode" + subId, nwMode);
        logd("shouldForceAutoNetworkSelect in mode = " + nwMode);
        if (isManualSelProhibitedInGlobalMode() && (nwMode == 10 || nwMode == 7)) {
            logd("Should force auto network select mode = " + nwMode);
            return true;
        }
        logd("Should not force auto network select mode = " + nwMode);
        return false;
    }

    private boolean isManualSelProhibitedInGlobalMode() {
        boolean isProhibited = false;
        String configString = getContext().getResources().getString(17040730);
        if (!TextUtils.isEmpty(configString)) {
            String[] configArray = configString.split(";");
            if (configArray != null && ((configArray.length == 1 && configArray[0].equalsIgnoreCase("true")) || (configArray.length == 2 && (TextUtils.isEmpty(configArray[1]) ^ 1) != 0 && configArray[0].equalsIgnoreCase("true") && isMatchGid(configArray[1])))) {
                isProhibited = true;
            }
        }
        logd("isManualNetSelAllowedInGlobal in current carrier is " + isProhibited);
        return isProhibited;
    }

    private void registerForIccRecordEvents() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            if (isPhoneTypeGsm()) {
                r.registerForNetworkSelectionModeAutomatic(this, 28, null);
                r.registerForRecordsEvents(this, 29, null);
                r.registerForRecordsLoaded(this, 3, null);
            } else {
                r.registerForRecordsLoaded(this, 22, null);
                if (isPhoneTypeCdmaLte()) {
                    r.registerForRecordsLoaded(this, 3, null);
                }
            }
        }
    }

    private void unregisterForIccRecordEvents() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.unregisterForNetworkSelectionModeAutomatic(this);
            r.unregisterForRecordsEvents(this);
            r.unregisterForRecordsLoaded(this);
        }
    }

    public void exitEmergencyCallbackMode() {
        if (DBG) {
            Rlog.d(LOG_TAG, "exitEmergencyCallbackMode: mImsPhone=" + this.mImsPhone + " isPhoneTypeGsm=" + isPhoneTypeGsm());
        }
        if (!isPhoneTypeGsm()) {
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            this.mCi.exitEmergencyCallbackMode(obtainMessage(26));
        } else if (this.mImsPhone != null) {
            this.mImsPhone.exitEmergencyCallbackMode();
        }
    }

    private void handleEnterEmergencyCallbackMode(Message msg) {
        if (DBG) {
            Rlog.d(LOG_TAG, "handleEnterEmergencyCallbackMode, isInEcm()=" + isInEcm());
        }
        if (!isInEcm()) {
            setIsInEcm(true);
            sendEmergencyCallbackModeChange();
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", RIL.DEFAULT_DUP_SMS_KEPP_PERIOD));
            this.mWakeLock.acquire();
        }
    }

    private void handleExitEmergencyCallbackMode(Message msg) {
        AsyncResult ar = msg.obj;
        if (DBG) {
            Rlog.d(LOG_TAG, "handleExitEmergencyCallbackMode,ar.exception , isInEcm=" + ar.exception + isInEcm());
        }
        removeCallbacks(this.mExitEcmRunnable);
        if (this.mEcmExitRespRegistrant != null) {
            this.mEcmExitRespRegistrant.notifyRegistrant(ar);
        }
        if (ar.exception == null) {
            if (isInEcm()) {
                setIsInEcm(false);
            }
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            sendEmergencyCallbackModeChange();
            this.mDcTracker.setInternalDataEnabled(true);
            notifyEmergencyCallRegistrants(false);
        }
    }

    public void notifyEmergencyCallRegistrants(boolean started) {
        this.mEmergencyCallToggledRegistrants.notifyResult(Integer.valueOf(started ? 1 : 0));
    }

    public void handleTimerInEmergencyCallbackMode(int action) {
        switch (action) {
            case 0:
                postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", RIL.DEFAULT_DUP_SMS_KEPP_PERIOD));
                this.mEcmTimerResetRegistrants.notifyResult(Boolean.FALSE);
                return;
            case 1:
                removeCallbacks(this.mExitEcmRunnable);
                this.mEcmTimerResetRegistrants.notifyResult(Boolean.TRUE);
                return;
            default:
                Rlog.e(LOG_TAG, "handleTimerInEmergencyCallbackMode, unsupported action " + action);
                return;
        }
    }

    private static boolean isIs683OtaSpDialStr(String dialStr) {
        if (dialStr.length() != 4) {
            switch (extractSelCodeFromOtaSpNum(dialStr)) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    return true;
                default:
                    return false;
            }
        } else if (dialStr.equals(IS683A_FEATURE_CODE)) {
            return true;
        } else {
            return false;
        }
    }

    private static int extractSelCodeFromOtaSpNum(String dialStr) {
        int dialStrLen = dialStr.length();
        int sysSelCodeInt = -1;
        if (dialStr.regionMatches(0, IS683A_FEATURE_CODE, 0, 4) && dialStrLen >= 6) {
            sysSelCodeInt = Integer.parseInt(dialStr.substring(4, 6));
        }
        if (DBG) {
            Rlog.d(LOG_TAG, "extractSelCodeFromOtaSpNum " + sysSelCodeInt);
        }
        return sysSelCodeInt;
    }

    private static boolean checkOtaSpNumBasedOnSysSelCode(int sysSelCodeInt, String[] sch) {
        try {
            int selRc = Integer.parseInt(sch[1]);
            int i = 0;
            while (i < selRc) {
                if (!(TextUtils.isEmpty(sch[i + 2]) || (TextUtils.isEmpty(sch[i + 3]) ^ 1) == 0)) {
                    int selMin = Integer.parseInt(sch[i + 2]);
                    int selMax = Integer.parseInt(sch[i + 3]);
                    if (sysSelCodeInt >= selMin && sysSelCodeInt <= selMax) {
                        return true;
                    }
                }
                i++;
            }
            return false;
        } catch (NumberFormatException ex) {
            Rlog.e(LOG_TAG, "checkOtaSpNumBasedOnSysSelCode, error", ex);
            return false;
        }
    }

    private boolean isCarrierOtaSpNum(String dialStr) {
        boolean isOtaSpNum = false;
        int sysSelCodeInt = extractSelCodeFromOtaSpNum(dialStr);
        if (sysSelCodeInt == -1) {
            return false;
        }
        if (!TextUtils.isEmpty(this.mCarrierOtaSpNumSchema)) {
            Matcher m = pOtaSpNumSchema.matcher(this.mCarrierOtaSpNumSchema);
            if (DBG) {
                Rlog.d(LOG_TAG, "isCarrierOtaSpNum,schema" + this.mCarrierOtaSpNumSchema);
            }
            if (m.find()) {
                String[] sch = pOtaSpNumSchema.split(this.mCarrierOtaSpNumSchema);
                if (TextUtils.isEmpty(sch[0]) || !sch[0].equals("SELC")) {
                    if (!TextUtils.isEmpty(sch[0]) && sch[0].equals("FC")) {
                        if (dialStr.regionMatches(0, sch[2], 0, Integer.parseInt(sch[1]))) {
                            isOtaSpNum = true;
                        } else if (DBG) {
                            Rlog.d(LOG_TAG, "isCarrierOtaSpNum,not otasp number");
                        }
                    } else if (DBG) {
                        Rlog.d(LOG_TAG, "isCarrierOtaSpNum,ota schema not supported" + sch[0]);
                    }
                } else if (sysSelCodeInt != -1) {
                    isOtaSpNum = checkOtaSpNumBasedOnSysSelCode(sysSelCodeInt, sch);
                } else if (DBG) {
                    Rlog.d(LOG_TAG, "isCarrierOtaSpNum,sysSelCodeInt is invalid");
                }
            } else if (DBG) {
                Rlog.d(LOG_TAG, "isCarrierOtaSpNum,ota schema pattern not right" + this.mCarrierOtaSpNumSchema);
            }
        } else if (DBG) {
            Rlog.d(LOG_TAG, "isCarrierOtaSpNum,ota schema pattern empty");
        }
        return isOtaSpNum;
    }

    public boolean isOtaSpNumber(String dialStr) {
        if (isPhoneTypeGsm()) {
            return super.isOtaSpNumber(dialStr);
        }
        boolean isOtaSpNum = false;
        String dialableStr = PhoneNumberUtils.extractNetworkPortionAlt(dialStr);
        if (dialableStr != null) {
            isOtaSpNum = isIs683OtaSpDialStr(dialableStr);
            if (!isOtaSpNum) {
                isOtaSpNum = isCarrierOtaSpNum(dialableStr);
            }
        }
        if (DBG) {
            Rlog.d(LOG_TAG, "isOtaSpNumber " + isOtaSpNum);
        }
        return isOtaSpNum;
    }

    public int getCdmaEriIconIndex() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriIconIndex();
        }
        return getServiceState().getCdmaEriIconIndex();
    }

    public int getCdmaEriIconMode() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriIconMode();
        }
        return getServiceState().getCdmaEriIconMode();
    }

    public String getCdmaEriText() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriText();
        }
        return this.mEriManager.getCdmaEriText(getServiceState().getCdmaRoamingIndicator(), getServiceState().getCdmaDefaultRoamingIndicator());
    }

    protected void phoneObjectUpdater(int newVoiceRadioTech) {
        logd("phoneObjectUpdater: newVoiceRadioTech=" + newVoiceRadioTech);
        if (ServiceState.isLte(newVoiceRadioTech) || newVoiceRadioTech == 0) {
            PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
            if (b != null) {
                int volteReplacementRat = b.getInt("volte_replacement_rat_int");
                logd("phoneObjectUpdater: volteReplacementRat=" + volteReplacementRat);
                if (volteReplacementRat != 0) {
                    newVoiceRadioTech = volteReplacementRat;
                }
            } else {
                loge("phoneObjectUpdater: didn't get volteReplacementRat from carrier config");
            }
        }
        if (this.mRilVersion == 6 && getLteOnCdmaMode() == 1) {
            if (getPhoneType() == 2) {
                logd("phoneObjectUpdater: LTE ON CDMA property is set. Use CDMA Phone newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
                return;
            } else {
                logd("phoneObjectUpdater: LTE ON CDMA property is set. Switch to CDMALTEPhone newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
                newVoiceRadioTech = 6;
            }
        } else if (isShuttingDown()) {
            logd("Device is shutting down. No need to switch phone now.");
            return;
        } else {
            boolean matchCdma = ServiceState.isCdma(newVoiceRadioTech);
            boolean matchGsm = ServiceState.isGsm(newVoiceRadioTech);
            if ((matchCdma && getPhoneType() == 2) || (matchGsm && getPhoneType() == 1)) {
                if ((matchGsm && this.mIccCardProxy.getCurrentAppType() == 2) || (matchCdma && this.mIccCardProxy.getCurrentAppType() == 1)) {
                    this.mIccCardProxy.setVoiceRadioTech(newVoiceRadioTech);
                }
                logd("phoneObjectUpdater: No change ignore, newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
                return;
            } else if (!(matchCdma || (matchGsm ^ 1) == 0)) {
                loge("phoneObjectUpdater: newVoiceRadioTech=" + newVoiceRadioTech + " doesn't match either CDMA or GSM - error! No phone change");
                return;
            }
        }
        if (newVoiceRadioTech == 0) {
            logd("phoneObjectUpdater: Unknown rat ignore,  newVoiceRadioTech=Unknown. mActivePhone=" + getPhoneName());
            return;
        }
        boolean oldPowerState = false;
        if (this.mResetModemOnRadioTechnologyChange && this.mCi.getRadioState().isOn()) {
            oldPowerState = true;
            logd("phoneObjectUpdater: Setting Radio Power to Off");
            this.mCi.setRadioPower(false, null);
        }
        switchVoiceRadioTech(newVoiceRadioTech);
        if (this.mResetModemOnRadioTechnologyChange && oldPowerState) {
            logd("phoneObjectUpdater: Resetting Radio");
            this.mCi.setRadioPower(oldPowerState, null);
        }
        this.mIccCardProxy.setVoiceRadioTech(newVoiceRadioTech);
        Intent intent = new Intent("android.intent.action.RADIO_TECHNOLOGY");
        intent.putExtra("phoneName", getPhoneName());
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhoneId);
        ActivityManager.broadcastStickyIntent(intent, -1);
    }

    private void switchVoiceRadioTech(int newVoiceRadioTech) {
        if (this.mCT.isOemInEcm() || this.mCT.isOemInEmergencyCall() || this.mCT.getState() != PhoneConstants.State.IDLE) {
            Rlog.d(LOG_TAG, "Switching Voice Phone :blocked!!!");
            return;
        }
        logd("Switching Voice Phone : " + getPhoneName() + " >>> " + (ServiceState.isGsm(newVoiceRadioTech) ? "GSM" : "CDMA"));
        if (ServiceState.isCdma(newVoiceRadioTech)) {
            UiccCardApplication cdmaApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
            if (cdmaApplication == null || cdmaApplication.getType() != AppType.APPTYPE_RUIM) {
                switchPhoneType(6);
            } else {
                switchPhoneType(2);
            }
        } else if (ServiceState.isGsm(newVoiceRadioTech)) {
            switchPhoneType(1);
        } else {
            loge("deleteAndCreatePhone: newVoiceRadioTech=" + newVoiceRadioTech + " is not CDMA or GSM (error) - aborting!");
        }
    }

    private boolean isManualSelectNetworksAllowed() {
        if (this.mSST == null || this.mSST.mSS == null) {
            loge("isManualSelectNetworkAllowed[false]: mSST/ mSST.mSS is null");
            return false;
        }
        boolean roaming = this.mSST.mSS.getRoaming();
        if (!(isPhoneTypeCdmaLte() && 11 == this.mCi.oppoGetPreferredNetworkType()) && (!(isPhoneTypeCdma() && 11 == this.mCi.oppoGetPreferredNetworkType()) && (!(OemConstant.isCtCard(this) && roaming) && (!isPhoneTypeGsm() || (OemConstant.isCtCard(this) ^ 1) == 0)))) {
            loge("isManualSelectNetworkAllowed[false]: ct card:=" + OemConstant.isCtCard(this) + " roaming:" + roaming);
            return false;
        }
        loge("isManualSelectNetworkAllowed[true]. with ct card:=" + OemConstant.isCtCard(this));
        return true;
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManager() {
        return this.mIccSmsInterfaceManager;
    }

    public void updatePhoneObject(int voiceRadioTech) {
        logd("updatePhoneObject: radioTechnology=" + voiceRadioTech);
        sendMessage(obtainMessage(42, voiceRadioTech, 0, null));
    }

    public void setImsRegistrationState(boolean registered) {
        this.mSST.setImsRegistrationState(registered);
    }

    public boolean getIccRecordsLoaded() {
        return this.mIccCardProxy.getIccRecordsLoaded();
    }

    public IccCard getIccCard() {
        return this.mIccCardProxy;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("GsmCdmaPhone extends:");
        super.dump(fd, pw, args);
        pw.println(" mPrecisePhoneType=" + this.mPrecisePhoneType);
        pw.println(" mSimRecords=" + this.mSimRecords);
        pw.println(" mIsimUiccRecords=" + this.mIsimUiccRecords);
        pw.println(" mCT=" + this.mCT);
        pw.println(" mSST=" + this.mSST);
        pw.println(" mPendingMMIs=" + this.mPendingMMIs);
        pw.println(" mIccPhoneBookIntManager=" + this.mIccPhoneBookIntManager);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mCdmaSubscriptionSource=" + this.mCdmaSubscriptionSource);
        pw.println(" mEriManager=" + this.mEriManager);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" isInEcm()=" + isInEcm());
        pw.println(" mCarrierOtaSpNumSchema=" + this.mCarrierOtaSpNumSchema);
        if (!isPhoneTypeGsm()) {
            pw.println(" getCdmaEriIconIndex()=" + getCdmaEriIconIndex());
            pw.println(" getCdmaEriIconMode()=" + getCdmaEriIconMode());
            pw.println(" getCdmaEriText()=" + getCdmaEriText());
            pw.println(" isMinInfoReady()=" + isMinInfoReady());
        }
        pw.println(" isCspPlmnEnabled()=" + isCspPlmnEnabled());
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        try {
            this.mIccCardProxy.dump(fd, pw, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("DeviceStateMonitor:");
        this.mDeviceStateMonitor.dump(fd, pw, args);
        pw.println("++++++++++++++++++++++++++++++++");
    }

    public boolean setOperatorBrandOverride(String brand) {
        if (this.mUiccController == null) {
            return false;
        }
        UiccCard card = this.mUiccController.getUiccCard(getPhoneId());
        if (card == null) {
            return false;
        }
        boolean status = card.setOperatorBrandOverride(brand);
        if (status) {
            IccRecords iccRecords = (IccRecords) this.mIccRecords.get();
            if (iccRecords != null) {
                TelephonyManager.from(this.mContext).setSimOperatorNameForPhone(getPhoneId(), iccRecords.getServiceProviderName());
            }
            if (this.mSST != null) {
                this.mSST.pollState();
            }
        }
        return status;
    }

    public String getOperatorNumeric() {
        Object obj = null;
        String operatorNumeric = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getOperatorNumeric();
            }
            return null;
        }
        IccRecords curIccRecords = null;
        if (this.mCdmaSubscriptionSource == 1) {
            operatorNumeric = SystemProperties.get(PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
        } else if (this.mCdmaSubscriptionSource == 0) {
            UiccCardApplication uiccCardApplication = (UiccCardApplication) this.mUiccApplication.get();
            if (uiccCardApplication == null || uiccCardApplication.getType() != AppType.APPTYPE_RUIM) {
                curIccRecords = this.mSimRecords;
            } else {
                logd("Legacy RUIM app present");
                curIccRecords = (IccRecords) this.mIccRecords.get();
            }
            if (curIccRecords == null || curIccRecords != this.mSimRecords) {
                curIccRecords = (IccRecords) this.mIccRecords.get();
                if (curIccRecords != null && (curIccRecords instanceof RuimRecords)) {
                    operatorNumeric = ((RuimRecords) curIccRecords).getRUIMOperatorNumeric();
                }
            } else {
                operatorNumeric = curIccRecords.getOperatorNumeric();
            }
        }
        if (operatorNumeric == null) {
            StringBuilder append = new StringBuilder().append("getOperatorNumeric: Cannot retrieve operatorNumeric: mCdmaSubscriptionSource = ").append(this.mCdmaSubscriptionSource).append(" mIccRecords = ");
            if (curIccRecords != null) {
                obj = Boolean.valueOf(curIccRecords.getRecordsLoaded());
            }
            loge(append.append(obj).toString());
        }
        logd("getOperatorNumeric: mCdmaSubscriptionSource = " + this.mCdmaSubscriptionSource + " operatorNumeric = " + operatorNumeric);
        return operatorNumeric;
    }

    public String getCountryIso() {
        SubscriptionInfo subInfo = SubscriptionManager.from(getContext()).getActiveSubscriptionInfo(getSubId());
        if (subInfo == null) {
            return null;
        }
        return subInfo.getCountryIso().toUpperCase();
    }

    public void notifyEcbmTimerReset(Boolean flag) {
        this.mEcmTimerResetRegistrants.notifyResult(flag);
    }

    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
        this.mEcmTimerResetRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForEcmTimerReset(Handler h) {
        this.mEcmTimerResetRegistrants.remove(h);
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                r.setVoiceMessageWaiting(line, countWaiting);
                return;
            } else {
                logd("SIM Records not found, MWI not updated");
                return;
            }
        }
        setVoiceMessageCount(countWaiting);
    }

    private void logd(String s) {
        Rlog.d(LOG_TAG, "[GsmCdmaPhone] " + s);
    }

    private void logi(String s) {
        Rlog.i(LOG_TAG, "[GsmCdmaPhone] " + s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[GsmCdmaPhone] " + s);
    }

    private void logw(String s) {
        Rlog.w(LOG_TAG, "[GsmCdmaPhone] " + s);
    }

    public boolean isUtEnabled() {
        if (OemConstant.isCTA(this.mContext) && OemConstant.isCtCard(this)) {
            return false;
        }
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null) {
            return imsPhone.isUtEnabled();
        }
        logd("isUtEnabled: called for GsmCdma");
        return false;
    }

    public String getDtmfToneDelayKey() {
        if (isPhoneTypeGsm()) {
            return "gsm_dtmf_tone_delay_int";
        }
        return "cdma_dtmf_tone_delay_int";
    }

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public int getLteOnCdmaMode() {
        int currentConfig = super.getLteOnCdmaMode();
        int lteOnCdmaModeDynamicValue = currentConfig;
        UiccCardApplication cdmaApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
        if (cdmaApplication != null && cdmaApplication.getType() == AppType.APPTYPE_RUIM && currentConfig == 1) {
            return 0;
        }
        return currentConfig;
    }

    private boolean isEmergencyNumber(String address) {
        boolean result = false;
        try {
            return Stub.asInterface(ServiceManager.getService("extphone")).isEmergencyNumber(address);
        } catch (RemoteException ex) {
            loge("RemoteException" + ex);
            return result;
        } catch (NullPointerException ex2) {
            loge("NullPointerException" + ex2);
            return result;
        }
    }

    public void getPreferedOperatorList(Message onComplete) {
        Rlog.d(LOG_TAG, "getPreferedOperatorList enter =====================");
        if (this.mIccRecords != null) {
            IccRecords mSIMRecords = (IccRecords) this.mIccRecords.get();
            if (mSIMRecords != null) {
                mSIMRecords.getPreferedOperatorList(onComplete, this.mSST);
                Rlog.d(LOG_TAG, "getPreferedOperatorList ok level =====================");
                return;
            }
            Rlog.d(LOG_TAG, "getPreferedOperatorList mSIMRecords is null =====================");
            return;
        }
        Rlog.d(LOG_TAG, "getPreferedOperatorList mIccRecords is null =====================");
    }

    public void setPOLEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
        Rlog.d(LOG_TAG, "setPOLEntry enter =====================");
        if (this.mIccRecords != null) {
            IccRecords mSIMRecords = (IccRecords) this.mIccRecords.get();
            if (mSIMRecords != null) {
                mSIMRecords.setPOLEntry(networkWithAct, onComplete);
                Rlog.d(LOG_TAG, "setPOLEntry ok  =====================");
                return;
            }
            Rlog.d(LOG_TAG, "setPOLEntry mSIMRecords is null =====================");
            return;
        }
        Rlog.d(LOG_TAG, "setPOLEntry mIccRecords is null =====================");
    }

    public void oppoGetRadioInfo(Message response) {
        this.mCi.oppoGetRadioInfo(response);
    }

    public String colorGetIccCardType() {
        return getIccPhoneBookInterfaceManager().colorGetIccCardType();
    }

    public void notifySuppService(SuppServiceNotification supp) {
        if (this.mSsnRegistrants.size() == 1) {
            Rlog.d(LOG_TAG, "notifySuppService ims to gcphone");
            this.mSsnRegistrants.notifyRegistrants(new AsyncResult(null, supp, null));
        }
    }

    public String[] getPnnFromEons(String plmn, boolean longName) {
        return this.mSST.getPnnFromEons(plmn, longName);
    }

    public String getMvnoPattern(String type) {
        String pattern = SpnOverride.MVNO_TYPE_NONE;
        Rlog.d(LOG_TAG, "getMvnoPattern:Type = " + type);
        if (!(this.mIccRecords.get() == null || (TextUtils.isEmpty(type) ^ 1) == 0)) {
            if (type.equals(SpnOverride.MVNO_TYPE_SPN)) {
                pattern = ((IccRecords) this.mIccRecords.get()).getSpNameInEfSpn();
            } else if (type.equals(SpnOverride.MVNO_TYPE_IMSI)) {
                pattern = ((IccRecords) this.mIccRecords.get()).isOperatorMvnoForImsi();
            } else if (type.equals(SpnOverride.MVNO_TYPE_PNN)) {
                pattern = ((IccRecords) this.mIccRecords.get()).isOperatorMvnoForEfPnn();
            } else if (type.equals(SpnOverride.MVNO_TYPE_GID)) {
                pattern = ((IccRecords) this.mIccRecords.get()).getGid1();
            } else {
                Rlog.d(LOG_TAG, "getMvnoPattern: Wrong type.");
            }
        }
        Rlog.d(LOG_TAG, "getMvnoPattern: pattern = " + pattern);
        return pattern;
    }

    public String getDefaultVMByImsi(String imsi) {
        if (imsi == null || DEFAULT_VM_BY_MCCMNC == null) {
            return null;
        }
        String defaultVM = null;
        for (int i = 0; i < DEFAULT_VM_BY_MCCMNC.length; i++) {
            if (imsi.startsWith(DEFAULT_VM_BY_MCCMNC[i][0])) {
                defaultVM = DEFAULT_VM_BY_MCCMNC[i][1];
                logd("getDefaultVMByImsi: defaultVM = " + defaultVM);
                break;
            }
        }
        return defaultVM;
    }

    public String oemGetFullIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String ret = r != null ? r.getFullIccId() : null;
        if (!TextUtils.isEmpty(ret)) {
            return ret;
        }
        if (!(isPhoneTypeGsm() || this.mUiccController == null)) {
            r = this.mUiccController.getIccRecords(this.mPhoneId, 1);
            ret = r != null ? r.getFullIccId() : null;
            if (!TextUtils.isEmpty(ret)) {
                return ret;
            }
        }
        long identity = Binder.clearCallingIdentity();
        try {
            SubscriptionInfo subInfo = SubscriptionManager.from(getContext()).getActiveSubscriptionInfo(getSubId());
            if (subInfo != null) {
                ret = subInfo.getIccId();
            }
            Binder.restoreCallingIdentity(identity);
            return ret;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public String colorGetImei() {
        return this.mImei;
    }

    public ImsPhone getPricseImsPhone() {
        if (this.mImsPhone == null || !(this.mImsPhone instanceof ImsPhone)) {
            return null;
        }
        return (ImsPhone) this.mImsPhone;
    }
}
