package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.MtkRadioAccessFamily;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.android.ims.ImsManager;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.RadioCapability;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.TelephonyDevController;
import com.android.internal.telephony.cdma.CdmaMmiCode;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneMmiCode;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.mediatek.ims.internal.MtkImsManager;
import com.mediatek.internal.telephony.MtkIccCardConstants;
import com.mediatek.internal.telephony.MtkOperatorUtils;
import com.mediatek.internal.telephony.dataconnection.MtkDcHelper;
import com.mediatek.internal.telephony.dataconnection.MtkDcTracker;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.gsm.MtkGsmMmiCode;
import com.mediatek.internal.telephony.gsm.MtkSuppCrssNotification;
import com.mediatek.internal.telephony.gsm.MtkSuppServiceNotification;
import com.mediatek.internal.telephony.imsphone.MtkImsPhone;
import com.mediatek.internal.telephony.imsphone.MtkLocalPhoneNumberUtils;
import com.mediatek.internal.telephony.phb.CsimPhbUtil;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.internal.telephony.scbm.ISCBMManager;
import com.mediatek.internal.telephony.selfactivation.ISelfActivation;
import com.mediatek.internal.telephony.uicc.MtkSIMRecords;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import mediatek.telephony.MtkServiceState;

public class MtkGsmCdmaPhone extends GsmCdmaPhone {
    public static final String ACT_TYPE_GSM = "0";
    public static final String ACT_TYPE_LTE = "7";
    public static final String ACT_TYPE_UTRAN = "2";
    private static final String CFB_KEY = "CFB";
    private static final String CFNRC_KEY = "CFNRC";
    private static final String CFNR_KEY = "CFNR";
    private static final String CFU_TIME_SLOT = "persist.vendor.radio.cfu.timeslot.";
    private static final boolean DBG = true;
    protected static final int EVENT_CIPHER_INDICATION = 1000;
    protected static final int EVENT_CRSS_IND = 1003;
    protected static final int EVENT_GET_APC_INFO = 1001;
    public static final int EVENT_GET_CALL_BARRING_COMPLETE = 2006;
    public static final int EVENT_GET_CALL_FORWARD_TIME_SLOT_DONE = 109;
    public static final int EVENT_GET_CALL_WAITING_DONE = 301;
    public static final int EVENT_GET_CLIR_COMPLETE = 2004;
    public static final int EVENT_IMS_UT_CSFB = 2001;
    public static final int EVENT_IMS_UT_DONE = 2000;
    protected static final int EVENT_MTK_BASE = 1000;
    public static final int EVENT_SET_CALL_BARRING_COMPLETE = 2005;
    public static final int EVENT_SET_CALL_FORWARD_TIME_SLOT_DONE = 110;
    public static final int EVENT_SET_CALL_WAITING_DONE = 302;
    protected static final int EVENT_SET_SS_PROPERTY = 1004;
    protected static final int EVENT_SSN_EX = 1002;
    public static final int EVENT_UNSOL_RADIO_CAPABILITY_CHANGED = 111;
    public static final String GSM_INDICATOR = "2G";
    public static final String IMS_DEREG_OFF = "0";
    public static final String IMS_DEREG_ON = "1";
    public static final String IMS_DEREG_PROP = "vendor.gsm.radio.ss.imsdereg";
    public static final String LOG_TAG = "MtkGsmCdmaPhone";
    public static final String LTE_INDICATOR = "4G";
    public static final int MESSAGE_SET_CF = 1;
    public static final boolean MTK_SVLTE_SUPPORT;
    public static final int NT_MODE_LTE_GSM = 101;
    public static final int NT_MODE_LTE_TDD_ONLY = 102;
    private static final int OPERATION_TIME_OUT_MILLIS = 3000;
    private static final int OPPO_ALREADY_IN_AUTO_SELECTION = 1;
    private static final int PROPERTY_MODE_BOOL = 1;
    private static final int PROPERTY_MODE_INT = 0;
    private static final int PROPERTY_MODE_STRING = 2;
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    private static final String PROPERTY_WFC_ENABLE = "persist.vendor.mtk.wfc.enable";
    private static final String PROP_MTK_CDMA_LTE_MODE = "ro.vendor.mtk_c2k_lte_mode";
    private static final String PROP_VZW_DEVICE_TYPE = "persist.vendor.vzw_device_type";
    public static final String REASON_CARRIER_CONFIG_LOADED = "carrierConfigLoaded";
    public static final String REASON_DATA_ALLOWED = "dataAllowed";
    public static final String REASON_DATA_SETUP_SSC_MODE3 = "dataSetupSscMode3";
    public static final String REASON_FDN_DISABLED = "FdnDisabled";
    public static final String REASON_FDN_ENABLED = "FdnEnabled";
    public static final String REASON_MD_DATA_RETRY_COUNT_RESET = "modemDataCountReset";
    public static final String REASON_PCSCF_ADDRESS_FAILED = "pcscfFailed";
    public static final String REASON_RA_FAILED = "raFailed";
    public static final String REASON_RESUME_PENDING_DATA = "resumePendingData";
    private static final String SS_SERVICE_CLASS_PROP = "vendor.gsm.radio.ss.sc";
    public static final int TBCW_NOT_VOLTE_USER = 2;
    public static final int TBCW_UNKNOWN = 0;
    public static final int TBCW_VOLTE_USER = 1;
    public static final int TBCW_WITH_CS = 3;
    public static final String UTRAN_INDICATOR = "3G";
    private BroadcastReceiver mBroadcastReceiver;
    private int mCSFallbackMode;
    private AsyncResult mCachedCrssn;
    private AsyncResult mCachedSsn;
    RegistrantList mCallRelatedSuppSvcRegistrants;
    private CountDownLatch mCallbackLatch;
    protected final RegistrantList mCipherIndicationRegistrants;
    private ExecutorService mExecutorService;
    private final Object mLock;
    public MtkRIL mMtkCi;
    private MtkSSRequestDecisionMaker mMtkSSReqDecisionMaker;
    public MtkServiceStateTracker mMtkSST;
    private int mNewVoiceTech;
    private ISCBMManager mScbmManager;
    private ISelfActivation mSelfActInstance;
    /* access modifiers changed from: private */
    public int mTbcwMode;
    TelephonyDevController mTelDevController;
    private boolean mWifiIsEnabledBeforeE911;

    private boolean hasC2kOverImsModem() {
        TelephonyDevController telephonyDevController = this.mTelDevController;
        if (telephonyDevController == null || telephonyDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasC2kOverImsModem()) {
            return false;
        }
        return true;
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_MTK_CDMA_LTE_MODE, 0) == 1) {
            z = true;
        }
        MTK_SVLTE_SUPPORT = z;
    }

    private static class Cfu {
        final Message mOnComplete;
        final int mServiceClass;
        final String mSetCfNumber;

        Cfu(String cfNumber, Message onComplete, int serviceClass) {
            this.mSetCfNumber = cfNumber;
            this.mOnComplete = onComplete;
            this.mServiceClass = serviceClass;
        }
    }

    public MtkGsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, boolean unitTestMode, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        super(context, ci, notifier, unitTestMode, phoneId, precisePhoneType, telephonyComponentFactory);
        this.mNewVoiceTech = -1;
        this.mLock = new Object();
        this.mTelDevController = TelephonyDevController.getInstance();
        this.mCipherIndicationRegistrants = new RegistrantList();
        this.mCallRelatedSuppSvcRegistrants = new RegistrantList();
        this.mCachedSsn = null;
        this.mCachedCrssn = null;
        this.mSelfActInstance = null;
        this.mScbmManager = null;
        this.mTbcwMode = 0;
        this.mExecutorService = Executors.newSingleThreadExecutor();
        this.mCSFallbackMode = 0;
        this.mWifiIsEnabledBeforeE911 = false;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.mediatek.internal.telephony.MtkGsmCdmaPhone.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                intent.getAction();
                int phoneId = intent.getIntExtra("phone", -1);
                boolean isForce = intent.getBooleanExtra("IS_FORCE", true);
                Rlog.d(MtkGsmCdmaPhone.LOG_TAG, "ACTION_CARRIER_CONFIG_CHANGED : IS_FORCE " + isForce + ", phoneId " + phoneId + ", mPhoneId " + MtkGsmCdmaPhone.this.mPhoneId);
                if (isForce && phoneId == MtkGsmCdmaPhone.this.mPhoneId && intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    MtkGsmCdmaPhone mtkGsmCdmaPhone = MtkGsmCdmaPhone.this;
                    mtkGsmCdmaPhone.sendMessage(mtkGsmCdmaPhone.obtainMessage(43));
                }
            }
        };
        Rlog.d(LOG_TAG, "constructor: sub = " + phoneId);
        this.mMtkCi = (MtkRIL) ci;
        this.mMtkSST = this.mSST;
        this.mMtkSSReqDecisionMaker = new MtkSSRequestDecisionMaker(this.mContext, this);
        this.mMtkSSReqDecisionMaker.starThread();
        this.mMtkCi.registerForCipherIndication(this, 1000, null);
        OpTelephonyCustomizationFactoryBase telephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(context);
        this.mSelfActInstance = telephonyCustomizationFactory.makeSelfActivationInstance(phoneId);
        this.mSelfActInstance.setContext(context).setCommandsInterface(ci).buildParams();
        this.mScbmManager = telephonyCustomizationFactory.makeSCBMManager(context, phoneId, ci);
    }

    public MtkGsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        this(context, ci, notifier, false, phoneId, precisePhoneType, telephonyComponentFactory);
    }

    public ISelfActivation getSelfActivationInstance() {
        return this.mSelfActInstance;
    }

    public ISCBMManager getScbmManagerInstance() {
        return this.mScbmManager;
    }

    public ServiceState getServiceState() {
        MtkServiceStateTracker mtkServiceStateTracker = this.mMtkSST;
        if ((mtkServiceStateTracker == null || (mtkServiceStateTracker.mSS.getState() != 0 && this.mMtkSST.mSS.getDataRegState() == 0)) && this.mImsPhone != null) {
            MtkServiceStateTracker mtkServiceStateTracker2 = this.mMtkSST;
            return MtkServiceState.mergeMtkServiceStates(mtkServiceStateTracker2 == null ? new MtkServiceState() : (MtkServiceState) mtkServiceStateTracker2.mSS, this.mImsPhone.getServiceState());
        } else if (this.mSST != null) {
            return this.mSST.mSS;
        } else {
            return new MtkServiceState();
        }
    }

    public PhoneConstants.DataState getDataConnectionState(String apnType) {
        PhoneConstants.DataState ret = PhoneConstants.DataState.DISCONNECTED;
        MtkDcTracker dct = getDcTracker(1);
        MtkServiceState turboSS = dct.getTurboSS();
        if (this.mSST == null) {
            ret = PhoneConstants.DataState.DISCONNECTED;
        } else if (turboSS != null || this.mSST.getCurrentDataConnectionState() == 0 || (!isPhoneTypeCdma() && !isPhoneTypeCdmaLte() && (!isPhoneTypeGsm() || apnType.equals("emergency")))) {
            int currentTransport = this.mTransportManager.getCurrentTransport(ApnSetting.getApnTypesBitmaskFromString(apnType));
            if (getDcTracker(currentTransport) != null) {
                int i = AnonymousClass5.$SwitchMap$com$android$internal$telephony$DctConstants$State[getDcTracker(currentTransport).getState(apnType).ordinal()];
                ret = (i == 1 || i == 2) ? MtkDcHelper.getInstance().isDataAllowedForConcurrent(getPhoneId()) ? PhoneConstants.DataState.CONNECTED : PhoneConstants.DataState.SUSPENDED : i != 3 ? PhoneConstants.DataState.DISCONNECTED : PhoneConstants.DataState.CONNECTING;
            }
        } else if (this.mImsPhone == null || this.mImsPhone.getServiceState().getState() != 0) {
            ret = PhoneConstants.DataState.DISCONNECTED;
        } else {
            int i2 = AnonymousClass5.$SwitchMap$com$android$internal$telephony$DctConstants$State[dct.getState(apnType).ordinal()];
            if (i2 == 1 || i2 == 2) {
                ret = MtkDcHelper.getInstance().isDataAllowedForConcurrent(getPhoneId()) ? PhoneConstants.DataState.CONNECTED : PhoneConstants.DataState.SUSPENDED;
            } else if (i2 == 3) {
                ret = PhoneConstants.DataState.CONNECTING;
            }
        }
        logd("getDataConnectionState apnType=" + apnType + " ret=" + ret);
        return ret;
    }

    /* renamed from: com.mediatek.internal.telephony.MtkGsmCdmaPhone$5  reason: invalid class name */
    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$DctConstants$State = new int[DctConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.DISCONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initOnce(CommandsInterface ci) {
        MtkGsmCdmaPhone.super.initOnce(ci);
        this.mCi.registerForRadioCapabilityChanged(this, 111, (Object) null);
        if (this.mMtkCi == null) {
            this.mMtkCi = (MtkRIL) ci;
        }
        this.mMtkCi.setOnSuppServiceNotificationEx(this, 1002, null);
        this.mMtkCi.setOnCallRelatedSuppSvc(this, 1003, null);
    }

    /* access modifiers changed from: protected */
    public void switchPhoneType(int precisePhoneType) {
        synchronized (this.mLock) {
            MtkGsmCdmaPhone.super.switchPhoneType(precisePhoneType);
        }
        if (this.mIccRecords.get() != null && precisePhoneType == 1) {
            logd("Re-register registerForIccRecordEvents due to phonetype change to GSM.");
            unregisterForIccRecordEvents();
            registerForIccRecordEvents();
        }
    }

    /* access modifiers changed from: protected */
    public void onUpdateIccAvailability() {
        MtkGsmCdmaPhone.super.onUpdateIccAvailability();
        UiccCardApplication newUiccApplication = getUiccCardApplication();
        UiccCardApplication app = (UiccCardApplication) this.mUiccApplication.get();
        IccRecords newIccRecord = newUiccApplication != null ? newUiccApplication.getIccRecords() : null;
        if (app == newUiccApplication && this.mIccRecords.get() != newIccRecord) {
            if (app != null) {
                logd("Removing stale icc objects.");
                if (this.mIccRecords.get() != null) {
                    unregisterForIccRecordEvents();
                    this.mIccPhoneBookIntManager.updateIccRecords((IccRecords) null);
                }
                this.mIccRecords.set(null);
                this.mUiccApplication.set(null);
            }
            if (newUiccApplication != null) {
                logd("New Uicc application found. type = " + newUiccApplication.getType());
                this.mUiccApplication.set(newUiccApplication);
                this.mIccRecords.set(newUiccApplication.getIccRecords());
                registerForIccRecordEvents();
                this.mIccPhoneBookIntManager.updateIccRecords((IccRecords) this.mIccRecords.get());
            }
        }
        Rlog.d(LOG_TAG, "isPhoneTypeCdmaLte:" + isPhoneTypeCdmaLte() + ", phoneId: " + getPhoneId() + " isCdmaWithoutLteCard: " + isCdmaWithoutLteCard() + " mNewVoiceTech: " + this.mNewVoiceTech);
        if (this.mNewVoiceTech == -1) {
            return;
        }
        if ((isPhoneTypeCdmaLte() && isCdmaWithoutLteCard()) || (isPhoneTypeCdma() && !isCdmaWithoutLteCard())) {
            updatePhoneObject(this.mNewVoiceTech);
        }
    }

    /* access modifiers changed from: protected */
    public boolean correctPhoneTypeForCdma(boolean matchCdma, int newVoiceRadioTech) {
        boolean phoneTypeChanged = false;
        if (matchCdma && getPhoneType() == 2) {
            UiccProfile uiccProfile = getUiccProfile();
            if (uiccProfile != null) {
                uiccProfile.setVoiceRadioTech(newVoiceRadioTech);
            }
            phoneTypeChanged = true;
        }
        if ((!isPhoneTypeCdmaLte() || !isCdmaWithoutLteCard()) && (!isPhoneTypeCdma() || isCdmaWithoutLteCard())) {
            phoneTypeChanged = false;
        }
        Rlog.d(LOG_TAG, "correctPhoneTypeForCdma: change:" + phoneTypeChanged + " newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
        return phoneTypeChanged;
    }

    /* access modifiers changed from: protected */
    public void switchVoiceRadioTech(int newVoiceRadioTech) {
        if (getState() != PhoneConstants.State.IDLE) {
            Rlog.d(LOG_TAG, "Switching Voice Phone :blocked!!!");
            setPhoneTypeSwitchPending();
            return;
        }
        clearPhoneTypeSwitchPending();
        StringBuilder sb = new StringBuilder();
        sb.append("Switching Voice Phone : ");
        sb.append(getPhoneName());
        sb.append(" >>> ");
        sb.append(ServiceState.isGsm(newVoiceRadioTech) ? "GSM" : "CDMA");
        logd(sb.toString());
        if (!ServiceState.isCdma(newVoiceRadioTech) || !isCdmaWithoutLteCard()) {
            MtkGsmCdmaPhone.super.switchVoiceRadioTech(newVoiceRadioTech);
        } else {
            switchPhoneType(2);
        }
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Rlog.d(LOG_TAG, "[MtkGsmCdmaPhone] " + s);
    }

    private boolean isCdmaWithoutLteCard() {
        if (MtkTelephonyManagerEx.getDefault().getIccAppFamily(getPhoneId()) == 2) {
            return true;
        }
        return false;
    }

    public void triggerModeSwitchByEcc(int mode, Message response) {
        this.mMtkCi.triggerModeSwitchByEcc(mode, response);
    }

    public String getLocatedPlmn() {
        return this.mMtkSST.getLocatedPlmn();
    }

    public void sendSubscriptionSettings(boolean restoreNetworkSelection) {
        MtkServiceStateTracker mtkServiceStateTracker = this.mMtkSST;
        if (mtkServiceStateTracker != null) {
            mtkServiceStateTracker.setDeviceRatMode(this.mPhoneId);
        }
        boolean restoreSelection_config = !this.mContext.getResources().getBoolean(17891616);
        if (restoreNetworkSelection && restoreSelection_config) {
            restoreSavedNetworkSelection(null);
        }
    }

    /* access modifiers changed from: protected */
    public void setPreferredNetworkTypeIfSimLoaded() {
        MtkServiceStateTracker mtkServiceStateTracker;
        if (SubscriptionManager.isValidSubscriptionId(getSubId()) && (mtkServiceStateTracker = this.mMtkSST) != null) {
            mtkServiceStateTracker.setDeviceRatMode(this.mPhoneId);
        }
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        if (networkType == 102) {
            this.mCi.setPreferredNetworkType(networkType, response);
            return;
        }
        int modemRaf = getRadioAccessFamily();
        int rafFromType = MtkRadioAccessFamily.getRafFromNetworkType(networkType);
        if (modemRaf == 0 || rafFromType == 0) {
            Rlog.d(LOG_TAG, "setPreferredNetworkType: Abort, unknown RAF: " + modemRaf + " " + rafFromType);
            if (response != null) {
                AsyncResult.forMessage(response, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                response.sendToTarget();
                return;
            }
            return;
        }
        int filteredType = MtkRadioAccessFamily.getNetworkTypeFromRaf((rafFromType & modemRaf) > 0 ? rafFromType & modemRaf : modemRaf);
        Rlog.d(LOG_TAG, "setPreferredNetworkType: networkType = " + networkType + " modemRaf = " + modemRaf + " rafFromType = " + rafFromType + " filteredType = " + filteredType);
        this.mCi.setPreferredNetworkType(filteredType, response);
    }

    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
        Phone.NetworkSelectMessage nsm = new Phone.NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = network.getOperatorNumeric();
        nsm.operatorAlphaLong = network.getOperatorAlphaLong();
        nsm.operatorAlphaShort = network.getOperatorAlphaShort();
        Message msg = obtainMessage(16, nsm);
        if (isPhoneTypeGsm()) {
            Rlog.d(LOG_TAG, "MTK GSMPhone selectNetworkManuallyWithAct:" + network);
            if (network.getOperatorAlphaLong() != null && network.getOperatorAlphaLong().endsWith(GSM_INDICATOR)) {
                this.mMtkCi.setNetworkSelectionModeManualWithAct(network.getOperatorNumeric(), "0", 0, msg);
            } else if (network.getOperatorAlphaLong() != null && network.getOperatorAlphaLong().endsWith(UTRAN_INDICATOR)) {
                this.mMtkCi.setNetworkSelectionModeManualWithAct(network.getOperatorNumeric(), ACT_TYPE_UTRAN, 0, msg);
            } else if (network.getOperatorAlphaLong() == null || !network.getOperatorAlphaLong().endsWith(LTE_INDICATOR)) {
                this.mCi.setNetworkSelectionModeManual(network.getOperatorNumeric(), msg);
            } else {
                this.mMtkCi.setNetworkSelectionModeManualWithAct(network.getOperatorNumeric(), "7", 0, msg);
            }
        } else {
            this.mCi.setNetworkSelectionModeManual(network.getOperatorNumeric(), msg);
        }
        if (persistSelection) {
            updateSavedNetworkOperator(nsm);
        } else {
            clearSavedNetworkSelection();
        }
    }

    public void setNetworkSelectionModeSemiAutomatic(OperatorInfo network, Message response) {
        Phone.NetworkSelectMessage nsm = new Phone.NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = "";
        nsm.operatorAlphaLong = "";
        nsm.operatorAlphaShort = "";
        Message msg = obtainMessage(17, nsm);
        Rlog.d(LOG_TAG, "MTK GSMPhone setNetworkSelectionModeSemiAutomatic:" + network);
        String actype = "0";
        if (network.getOperatorAlphaLong() != null && network.getOperatorAlphaLong().endsWith(UTRAN_INDICATOR)) {
            actype = ACT_TYPE_UTRAN;
        } else if (network.getOperatorAlphaLong() != null && network.getOperatorAlphaLong().endsWith(LTE_INDICATOR)) {
            actype = "7";
        }
        this.mMtkCi.setNetworkSelectionModeManualWithAct(network.getOperatorNumeric(), actype, 1, msg);
    }

    public void getAvailableNetworks(Message response) {
        if (isPhoneTypeGsm() || (isPhoneTypeCdmaLte() && 11 == this.mCi.oppoGetPreferredNetworkType())) {
            this.mMtkCi.getAvailableNetworksWithAct(response);
            return;
        }
        Rlog.d(LOG_TAG, "getAvailableNetworks: not possible in CDMA");
        notifyFailure(response, CommandException.Error.REQUEST_NOT_SUPPORTED);
    }

    public synchronized void cancelAvailableNetworks(Message response) {
        Rlog.d(LOG_TAG, "cancelAvailableNetworks");
        this.mMtkCi.cancelAvailableNetworks(response);
    }

    public void getFemtoCellList(Message response) {
        Rlog.d(LOG_TAG, "getFemtoCellList()");
        this.mMtkCi.getFemtoCellList(response);
    }

    public void abortFemtoCellList(Message response) {
        Rlog.d(LOG_TAG, "abortFemtoCellList()");
        this.mMtkCi.abortFemtoCellList(response);
    }

    public void selectFemtoCell(FemtoCellInfo femtocell, Message response) {
        Rlog.d(LOG_TAG, "selectFemtoCell(): " + femtocell);
        this.mMtkCi.selectFemtoCell(femtocell, response);
    }

    public void queryFemtoCellSystemSelectionMode(Message response) {
        Rlog.d(LOG_TAG, "queryFemtoCellSystemSelectionMode()");
        this.mMtkCi.queryFemtoCellSystemSelectionMode(response);
    }

    public void setFemtoCellSystemSelectionMode(int mode, Message response) {
        Rlog.d(LOG_TAG, "setFemtoCellSystemSelectionMode(), mode=" + mode);
        this.mMtkCi.setFemtoCellSystemSelectionMode(mode, response);
    }

    /* access modifiers changed from: protected */
    public Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs, ResultReceiver wrappedCallback) throws CallStateException {
        String newDialString = PhoneNumberUtils.stripSeparators(dialString);
        if (!isPhoneTypeGsm()) {
            return this.mCT.dial(newDialString, dialArgs.intentExtras);
        }
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        MtkGsmMmiCode mmi = MtkGsmMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), this, (UiccCardApplication) this.mUiccApplication.get(), wrappedCallback);
        logd("dialInternal: dialing w/ mmi '" + mmi + "'...");
        if (mmi == null) {
            return this.mCT.dialGsm(newDialString, dialArgs.uusInfo, dialArgs.intentExtras);
        }
        if (mmi.isTemporaryModeCLIR()) {
            return this.mCT.dialGsm(mmi.mDialingNumber, mmi.getCLIRMode(), dialArgs.uusInfo, dialArgs.intentExtras);
        }
        this.mPendingMMIs.add(mmi);
        Rlog.d(LOG_TAG, "dialInternal: " + MtkSuppServHelper.encryptString(dialString) + ", mmi=" + mmi);
        dumpPendingMmi();
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
        mmi.processCode();
        return null;
    }

    public void doGeneralSimAuthentication(int sessionId, int mode, int tag, String param1, String param2, Message result) {
        if (isPhoneTypeGsm()) {
            this.mMtkCi.doGeneralSimAuthentication(sessionId, mode, tag, param1, param2, result);
        }
    }

    public String getMvnoPattern(String type) {
        String pattern = "";
        synchronized (this.mLock) {
            if (isPhoneTypeGsm() && this.mIccRecords.get() != null) {
                if (type.equals("spn")) {
                    pattern = ((MtkSIMRecords) this.mIccRecords.get()).getSpNameInEfSpn();
                } else if (type.equals("imsi")) {
                    pattern = ((MtkSIMRecords) this.mIccRecords.get()).isOperatorMvnoForImsi();
                } else if (type.equals("pnn")) {
                    pattern = ((MtkSIMRecords) this.mIccRecords.get()).isOperatorMvnoForEfPnn();
                } else if (type.equals("gid")) {
                    pattern = ((IccRecords) this.mIccRecords.get()).getGid1();
                } else {
                    Rlog.d(LOG_TAG, "getMvnoPattern: Wrong type = " + type);
                }
            }
        }
        return pattern;
    }

    public String getMvnoMatchType() {
        String type = "";
        synchronized (this.mLock) {
            if (isPhoneTypeGsm()) {
                if (this.mIccRecords.get() != null) {
                    type = ((MtkSIMRecords) this.mIccRecords.get()).getMvnoMatchType();
                }
                Rlog.d(LOG_TAG, "getMvnoMatchType: Type = " + type);
            }
        }
        return type;
    }

    /* access modifiers changed from: protected */
    public void updateImsPhone() {
        Rlog.d(LOG_TAG, "updateImsPhone");
        MtkDcHelper dcHelper = MtkDcHelper.getInstance();
        if (this.mImsServiceReady && this.mImsPhone == null) {
            MtkGsmCdmaPhone.super.updateImsPhone();
            if (dcHelper != null) {
                dcHelper.registerImsEvents(getPhoneId());
            }
        } else if (!this.mImsServiceReady && this.mImsPhone != null) {
            if (dcHelper != null) {
                dcHelper.unregisterImsEvents(getPhoneId());
            }
            MtkGsmCdmaPhone.super.updateImsPhone();
        }
    }

    public void hangupAll() throws CallStateException {
        this.mCT.hangupAll();
    }

    public Call getCSRingingCall() {
        return this.mCT.mRingingCall;
    }

    /* access modifiers changed from: package-private */
    public boolean isInCSCall() {
        return getForegroundCall().getState().isAlive() || getBackgroundCall().getState().isAlive() || getCSRingingCall().getState().isAlive();
    }

    public void registerForCipherIndication(Handler h, int what, Object obj) {
        this.mCipherIndicationRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForCipherIndication(Handler h) {
        this.mCipherIndicationRegistrants.remove(h);
    }

    public Connection dial(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
        boolean alwaysTryImsForEmergencyCarrierConfig;
        boolean allowWpsOverIms;
        boolean useImsForCall;
        Object obj;
        int i;
        if (MtkGsmCdmaPhone.super.handleCalloutControl()) {
            logd("block voice out");
            return null;
        } else if (isPhoneTypeGsm() || dialArgs.uusInfo == null) {
            boolean isEmergency = PhoneNumberUtils.isEmergencyNumber(getSubId(), dialString);
            MtkLocalPhoneNumberUtils.setIsEmergencyNumber(isEmergency);
            Phone imsPhone = this.mImsPhone;
            if (isEmergency) {
                tryTurnOffWifiForE911(isEmergency);
            }
            int imsServiceState = imsPhone != null ? imsPhone.getServiceState().getState() : 3;
            if (isEmergency) {
                alwaysTryImsForEmergencyCarrierConfig = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId()).getBoolean("carrier_use_ims_first_for_emergency_bool");
            } else {
                alwaysTryImsForEmergencyCarrierConfig = false;
            }
            boolean isWpsCall = dialString != null ? dialString.startsWith("*272") : false;
            if (isWpsCall) {
                allowWpsOverIms = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId()).getBoolean("support_wps_over_ims_bool");
            } else {
                allowWpsOverIms = true;
            }
            boolean useImsForCall2 = isImsUseEnabled() && imsPhone != null && (imsPhone.isVolteEnabled() || imsPhone.isWifiCallingEnabled() || (imsPhone.isVideoEnabled() && VideoProfile.isVideo(dialArgs.videoState))) && imsServiceState == 0 && (!isWpsCall || allowWpsOverIms);
            boolean useImsForEmergency = imsPhone != null && isEmergency && alwaysTryImsForEmergencyCarrierConfig && ImsManager.getInstance(this.mContext, this.mPhoneId).isNonTtyOrTtyOnVolteEnabled() && imsServiceState != 3;
            if (OemConstant.EXP_VERSION) {
                ServiceState st = imsPhone != null ? imsPhone.getServiceState() : new ServiceState();
                useImsForEmergency = useImsForEmergency && (st.getState() == 0 || st.isEmergencyOnly());
            }
            if (hasC2kOverImsModem()) {
                Rlog.d(LOG_TAG, "keep AOSP");
            } else if (!isPhoneTypeGsm()) {
                useImsForEmergency = false;
            }
            if (this.mPhoneId != getMainCapabilityPhoneId() && !MtkImsManager.isSupportMims()) {
                useImsForEmergency = false;
            }
            if (shouldProcessSelfActivation() || useImsForPCOChanged()) {
                logd("always use ImsPhone for self activation");
                useImsForCall = true;
            } else {
                useImsForCall = useImsForCall2;
            }
            String dialPart = PhoneNumberUtils.extractNetworkPortionAlt(PhoneNumberUtils.stripSeparators(dialString));
            boolean isUt = dialPart != null && (dialPart.startsWith("*") || dialPart.startsWith("#")) && dialPart.endsWith("#");
            boolean useImsForUt = imsPhone != null && imsPhone.isUtEnabled();
            SystemProperties.set("gsm.oppo.operator.ringtone", String.valueOf(((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId()).getInt("oppo.operator.ringtone", 0)));
            StringBuilder sb = new StringBuilder();
            sb.append("PhoneId = ");
            sb.append(this.mPhoneId);
            sb.append(", useImsForCall=");
            sb.append(useImsForCall);
            sb.append(", useImsForEmergency=");
            sb.append(useImsForEmergency);
            sb.append(", useImsForUt=");
            sb.append(useImsForUt);
            sb.append(", isUt=");
            sb.append(isUt);
            sb.append(", isWpsCall=");
            sb.append(isWpsCall);
            sb.append(", allowWpsOverIms=");
            sb.append(allowWpsOverIms);
            sb.append(", imsPhone=");
            sb.append(imsPhone);
            sb.append(", imsPhone.isVolteEnabled()=");
            Object obj2 = DataSubConstants.NO_SIM_VALUE;
            if (imsPhone != null) {
                obj = obj2;
                obj2 = Boolean.valueOf(imsPhone.isVolteEnabled());
            } else {
                obj = obj2;
            }
            sb.append(obj2);
            sb.append(", imsPhone.isVowifiEnabled()=");
            sb.append(imsPhone != null ? Boolean.valueOf(imsPhone.isWifiCallingEnabled()) : obj);
            sb.append(", imsPhone.isVideoEnabled()=");
            sb.append(imsPhone != null ? Boolean.valueOf(imsPhone.isVideoEnabled()) : obj);
            sb.append(", imsPhone.getServiceState().getState()=");
            sb.append(imsPhone != null ? Integer.valueOf(imsServiceState) : obj);
            logd(sb.toString());
            if (SubscriptionManager.getIntegerSubscriptionProperty(getSubId(), "wfc_ims_mode", 2, this.mContext) == 0) {
                Phone.checkWfcWifiOnlyModeBeforeDial(this.mImsPhone, this.mPhoneId, this.mContext);
            }
            if ((useImsForCall && !isUt && !isEmergency) || ((isUt && useImsForUt) || useImsForEmergency)) {
                if (isInCSCall()) {
                    Rlog.d(LOG_TAG, "has CS Call. Don't try IMS PS Call!");
                } else {
                    try {
                        if (dialArgs.videoState == 0) {
                            logd("Trying IMS PS call");
                            return imsPhone.dial(dialString, dialArgs);
                        } else if (SystemProperties.get("persist.vendor.vilte_support").equals("1")) {
                            logd("Trying IMS PS video call");
                            return imsPhone.dial(dialString, dialArgs);
                        } else {
                            loge("Should not be here. (isInCSCall == false, videoState=" + dialArgs.videoState);
                        }
                    } catch (CallStateException e) {
                        logd("IMS PS call exception " + e + "useImsForCall =" + useImsForCall + ", imsPhone =" + imsPhone);
                        tryTurnOnWifiForE911Finished();
                        if ("cs_fallback".equals(e.getMessage()) || isEmergency) {
                            logi("IMS call failed with Exception: " + e.getMessage() + ". Falling back to CS.");
                        } else {
                            CallStateException ce = new CallStateException(e.getError(), e.getMessage());
                            ce.setStackTrace(e.getStackTrace());
                            throw ce;
                        }
                    }
                }
            }
            if (SystemProperties.getInt("vendor.gsm.gcf.testmode", 0) == 2 || isCdmaLessDevice() || this.mSST == null || this.mSST.mSS.getState() != 1 || this.mSST.mSS.getDataRegState() == 0 || isEmergency) {
                if (this.mSST == null || this.mSST.mSS.getState() != 3) {
                    i = 2;
                } else if (VideoProfile.isVideo(dialArgs.videoState)) {
                    i = 2;
                } else if (isEmergency) {
                    i = 2;
                } else {
                    throw new CallStateException(2, "cannot dial voice call in airplane mode");
                }
                if (SystemProperties.getInt("vendor.gsm.gcf.testmode", 0) == i || isCdmaLessDevice() || this.mSST == null || this.mSST.mSS.getState() != 1 || ((this.mSST.mSS.getDataRegState() == 0 && ServiceState.isLte(this.mSST.mSS.getRilDataRadioTechnology())) || VideoProfile.isVideo(dialArgs.videoState) || isEmergency || (isUt && useImsForUt))) {
                    logd("Trying (non-IMS) CS call");
                    if (isPhoneTypeGsm()) {
                        return dialInternal(dialString, new PhoneInternalInterface.DialArgs.Builder().setIntentExtras(dialArgs.intentExtras).build());
                    }
                    return dialInternal(dialString, dialArgs);
                }
                throw new CallStateException(1, "cannot dial voice call in out of service");
            }
            throw new CallStateException("cannot dial in current state");
        } else {
            throw new CallStateException("Sending UUS information NOT supported in CDMA!");
        }
    }

    public void handleMessage(Message msg) {
        MtkSuppServHelper ssHelper;
        String errorMsg;
        MtkSuppServHelper ssHelper2;
        String errorMsg2;
        MtkSuppServHelper ssHelper3;
        String errorMsg3;
        MtkSuppServHelper ssHelper4;
        String errorMsg4;
        MtkSuppServHelper ssHelper5;
        String errorMsg5;
        MtkGsmCdmaConnection cn;
        MtkSuppServHelper ssHelper6;
        String errorMsg6;
        MtkSuppServHelper ssHelper7;
        String errorMsg7;
        MtkSuppServHelper ssHelper8;
        String errorMsg8;
        int i = msg.what;
        boolean enable = true;
        if (i == 2) {
            logd("Event EVENT_SSN Received");
            if (isPhoneTypeGsm()) {
                SuppServiceNotification not = (SuppServiceNotification) ((AsyncResult) msg.obj).result;
                if (not.notificationType == 1 && not.code == 0) {
                    logd("skip AOSP event for MT forwarded call notification");
                    return;
                }
                AsyncResult ar = new AsyncResult((Object) null, not, (Throwable) null);
                if (this.mSsnRegistrants.size() == 0) {
                    this.mCachedSsn = ar;
                }
                this.mSsnRegistrants.notifyRegistrants(ar);
            }
        } else if (i != 3) {
            boolean exception = false;
            if (i != 12) {
                if (i == 13) {
                    Rlog.d(LOG_TAG, "mPhoneId= " + this.mPhoneId + "subId=" + getSubId());
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception == null) {
                        handleCfuQueryResult((CallForwardInfo[]) ar2.result);
                    } else if (supportMdAutoSetupIms()) {
                        CommandException cmdException = ar2.exception;
                        if (cmdException.getCommandError() == CommandException.Error.OEM_ERROR_25 && cmdException.getMessage() != null && cmdException.getMessage().isEmpty() && (ssHelper2 = MtkSuppServManager.getSuppServHelper(getPhoneId())) != null && (errorMsg2 = ssHelper2.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25)) != null && !errorMsg2.isEmpty()) {
                            ar2.exception = new CommandException(CommandException.Error.OEM_ERROR_25, errorMsg2);
                        }
                    }
                    Message onComplete = (Message) ar2.userObj;
                    if (onComplete != null) {
                        AsyncResult.forMessage(onComplete, ar2.result, ar2.exception);
                        onComplete.sendToTarget();
                    }
                } else if (i == 16) {
                    MtkGsmCdmaPhone.super.handleMessage(msg);
                    if (isPhoneTypeGsm()) {
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        boolean restoreSelection = !this.mContext.getResources().getBoolean(17891616);
                        if (!(ar3 == null || ar3.exception == null)) {
                            exception = true;
                        }
                        Rlog.d(LOG_TAG, "EVENT_SET_NETWORK_MANUAL_COMPLETE, restoreSelection=" + restoreSelection + " exception=" + exception);
                        if (!restoreSelection && exception) {
                            clearSavedNetworkSelection();
                            this.mCi.setNetworkSelectionModeAutomatic((Message) null);
                        }
                    }
                } else if (i == 18) {
                    Rlog.d(LOG_TAG, "EVENT_SET_CLIR_COMPLETE");
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    if (ar4.exception == null) {
                        saveClirSetting(msg.arg1);
                    }
                    if (ar4.exception != null && (ar4.exception instanceof CommandException)) {
                        CommandException cmdException2 = ar4.exception;
                        Rlog.d(LOG_TAG, "EVENT_SET_CLIR_COMPLETE: cmdException error:" + cmdException2.getCommandError());
                        if (supportMdAutoSetupIms()) {
                            if ((isOp(MtkOperatorUtils.OPID.OP01) || isOp(MtkOperatorUtils.OPID.OP02)) && isUtError(cmdException2.getCommandError())) {
                                Rlog.d(LOG_TAG, "return REQUEST_NOT_SUPPORTED");
                                ar4.exception = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                            } else if (cmdException2.getCommandError() != CommandException.Error.OEM_ERROR_25) {
                                Rlog.d(LOG_TAG, "return Original Error");
                            } else if (!(cmdException2.getMessage() == null || !cmdException2.getMessage().isEmpty() || (ssHelper3 = MtkSuppServManager.getSuppServHelper(getPhoneId())) == null || (errorMsg3 = ssHelper3.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25)) == null || errorMsg3.isEmpty())) {
                                ar4.exception = new CommandException(CommandException.Error.OEM_ERROR_25, errorMsg3);
                            }
                        }
                    }
                    Message onComplete2 = (Message) ar4.userObj;
                    if (onComplete2 != null) {
                        AsyncResult.forMessage(onComplete2, ar4.result, ar4.exception);
                        onComplete2.sendToTarget();
                    }
                } else if (i != 25) {
                    if (i == 29) {
                        Rlog.d(LOG_TAG, "EVENT_ICC_RECORD_EVENTS");
                        processIccRecordEvents(((Integer) ((AsyncResult) msg.obj).result).intValue());
                        MtkSuppServHelper ssHelper9 = MtkSuppServManager.getSuppServHelper(getPhoneId());
                        if (ssHelper9 != null) {
                            ssHelper9.setIccRecordsReady();
                        }
                    } else if (i == 301) {
                        AsyncResult ar5 = (AsyncResult) msg.obj;
                        Rlog.d(LOG_TAG, "[EVENT_GET_CALL_WAITING_]ar.exception = " + ar5.exception);
                        Message onComplete3 = (Message) ar5.userObj;
                        if (ar5.exception == null) {
                            int[] cwArray = (int[]) ar5.result;
                            try {
                                Rlog.d(LOG_TAG, "EVENT_GET_CALL_WAITING_DONE cwArray[0]:cwArray[1] = " + cwArray[0] + ":" + cwArray[1]);
                                if (cwArray[0] != 1 || (cwArray[1] & 1) != 1) {
                                    enable = false;
                                }
                                if (!supportMdAutoSetupIms()) {
                                    setTerminalBasedCallWaiting(enable, null);
                                }
                                if (onComplete3 != null) {
                                    AsyncResult.forMessage(onComplete3, ar5.result, (Throwable) null);
                                    onComplete3.sendToTarget();
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                Rlog.e(LOG_TAG, "EVENT_GET_CALL_WAITING_DONE: improper result: err =" + e.getMessage());
                                if (onComplete3 != null) {
                                    AsyncResult.forMessage(onComplete3, ar5.result, (Throwable) null);
                                    onComplete3.sendToTarget();
                                }
                            }
                        } else {
                            if (supportMdAutoSetupIms()) {
                                CommandException cmdException3 = ar5.exception;
                                if (cmdException3.getCommandError() == CommandException.Error.OEM_ERROR_25 && cmdException3.getMessage() != null && cmdException3.getMessage().isEmpty() && (ssHelper4 = MtkSuppServManager.getSuppServHelper(getPhoneId())) != null && (errorMsg4 = ssHelper4.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25)) != null && !errorMsg4.isEmpty()) {
                                    ar5.exception = new CommandException(CommandException.Error.OEM_ERROR_25, errorMsg4);
                                }
                            }
                            if (onComplete3 != null) {
                                AsyncResult.forMessage(onComplete3, ar5.result, ar5.exception);
                                onComplete3.sendToTarget();
                            }
                        }
                    } else if (i == 302) {
                        AsyncResult ar6 = (AsyncResult) msg.obj;
                        Message onComplete4 = (Message) ar6.userObj;
                        Rlog.d(LOG_TAG, "EVENT_SET_CALL_WAITING_DONE: ar.exception=" + ar6.exception);
                        if (ar6.exception != null) {
                            if (supportMdAutoSetupIms()) {
                                CommandException cmdException4 = ar6.exception;
                                if (cmdException4.getCommandError() == CommandException.Error.OEM_ERROR_25 && cmdException4.getMessage() != null && cmdException4.getMessage().isEmpty() && (ssHelper5 = MtkSuppServManager.getSuppServHelper(getPhoneId())) != null && (errorMsg5 = ssHelper5.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25)) != null && !errorMsg5.isEmpty()) {
                                    ar6.exception = new CommandException(CommandException.Error.OEM_ERROR_25, errorMsg5);
                                }
                            }
                            if (onComplete4 != null) {
                                AsyncResult.forMessage(onComplete4, ar6.result, ar6.exception);
                                onComplete4.sendToTarget();
                            }
                        } else if (!supportMdAutoSetupIms()) {
                            if (msg.arg1 != 1) {
                                enable = false;
                            }
                            setTerminalBasedCallWaiting(enable, onComplete4);
                        } else if (onComplete4 != null) {
                            AsyncResult.forMessage(onComplete4, (Object) null, (Throwable) null);
                            onComplete4.sendToTarget();
                        }
                    } else if (i == 2000) {
                        Rlog.d(LOG_TAG, "EVENT_IMS_UT_DONE: Enter");
                        handleImsUtDone(msg);
                    } else if (i != 2001) {
                        switch (i) {
                            case 109:
                                Rlog.d(LOG_TAG, "mPhoneId = " + this.mPhoneId + ", subId = " + getSubId());
                                AsyncResult ar7 = (AsyncResult) msg.obj;
                                StringBuilder sb = new StringBuilder();
                                sb.append("[EVENT_GET_CALL_FORWARD_TIME_SLOT_DONE]ar.exception = ");
                                sb.append(ar7.exception);
                                Rlog.d(LOG_TAG, sb.toString());
                                if (ar7.exception == null) {
                                    handleCfuInTimeSlotQueryResult((MtkCallForwardInfo[]) ar7.result);
                                }
                                Rlog.d(LOG_TAG, "[EVENT_GET_CALL_FORWARD_TIME_SLOT_DONE]msg.arg1 = " + msg.arg1);
                                if (ar7.exception != null && (ar7.exception instanceof CommandException)) {
                                    CommandException cmdException5 = ar7.exception;
                                    Rlog.d(LOG_TAG, "[EVENT_GET_CALL_FORWARD_TIME_SLOT_DONE] cmdException error:" + cmdException5.getCommandError());
                                    if (msg.arg1 == 1 && cmdException5.getCommandError() == CommandException.Error.REQUEST_NOT_SUPPORTED && this.mSST != null && this.mSST.mSS != null && this.mSST.mSS.getState() == 0) {
                                        getCallForwardingOption(0, obtainMessage(13));
                                    }
                                    if (supportMdAutoSetupIms() && cmdException5.getCommandError() == CommandException.Error.OEM_ERROR_2) {
                                        Rlog.d(LOG_TAG, "return REQUEST_NOT_SUPPORTED");
                                        ar7.exception = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                                    }
                                }
                                Message onComplete5 = (Message) ar7.userObj;
                                if (onComplete5 != null) {
                                    AsyncResult.forMessage(onComplete5, ar7.result, ar7.exception);
                                    onComplete5.sendToTarget();
                                    return;
                                }
                                return;
                            case 110:
                                AsyncResult ar8 = (AsyncResult) msg.obj;
                                IccRecords records = (IccRecords) this.mIccRecords.get();
                                CfuEx cfuEx = (CfuEx) ar8.userObj;
                                if (ar8.exception == null && records != null) {
                                    if (msg.arg1 == 1) {
                                        exception = true;
                                    }
                                    records.setVoiceCallForwardingFlag(1, exception, cfuEx.mSetCfNumber);
                                    saveTimeSlot(cfuEx.mSetTimeSlot);
                                }
                                if (cfuEx.mOnComplete != null) {
                                    AsyncResult.forMessage(cfuEx.mOnComplete, ar8.result, ar8.exception);
                                    cfuEx.mOnComplete.sendToTarget();
                                    return;
                                }
                                return;
                            case 111:
                                AsyncResult ar9 = (AsyncResult) msg.obj;
                                RadioCapability rc_unsol = (RadioCapability) ar9.result;
                                if (ar9.exception != null) {
                                    Rlog.d(LOG_TAG, "RIL_UNSOL_RADIO_CAPABILITY fail, don't change capability");
                                } else {
                                    radioCapabilityUpdated(rc_unsol);
                                }
                                Rlog.d(LOG_TAG, "EVENT_UNSOL_RADIO_CAPABILITY_CHANGED: rc: " + rc_unsol);
                                return;
                            default:
                                switch (i) {
                                    case 1001:
                                        Rlog.d(LOG_TAG, "handle EVENT_GET_APC_INFO");
                                        AsyncResult ar10 = (AsyncResult) msg.obj;
                                        PseudoCellInfoResult result = (PseudoCellInfoResult) ar10.userObj;
                                        if (result == null) {
                                            Rlog.e(LOG_TAG, "EVENT_GET_APC_INFO: result return null");
                                            return;
                                        }
                                        synchronized (result.lockObj) {
                                            if (ar10.exception != null) {
                                                Rlog.d(LOG_TAG, "EVENT_GET_APC_INFO: error ret null, e=" + ar10.exception);
                                                result.infos = null;
                                            } else {
                                                result.infos = new PseudoCellInfo((int[]) ar10.result);
                                            }
                                            result.lockObj.notify();
                                        }
                                        return;
                                    case 1002:
                                        logd("Event EVENT_SSN_EX Received");
                                        if (isPhoneTypeGsm()) {
                                            MtkSuppServiceNotification not2 = (MtkSuppServiceNotification) ((AsyncResult) msg.obj).result;
                                            if (not2.notificationType != 1) {
                                                return;
                                            }
                                            if (not2.code == 0 || not2.code >= 11) {
                                                AsyncResult ar11 = new AsyncResult((Object) null, not2, (Throwable) null);
                                                if (this.mSsnRegistrants.size() == 0) {
                                                    this.mCachedSsn = ar11;
                                                }
                                                this.mSsnRegistrants.notifyRegistrants(ar11);
                                                return;
                                            }
                                            logd("Unexpected SSN_EX code:" + not2.code);
                                            return;
                                        }
                                        return;
                                    case 1003:
                                        AsyncResult ar12 = (AsyncResult) msg.obj;
                                        MtkSuppCrssNotification noti = (MtkSuppCrssNotification) ar12.result;
                                        if (noti.code == 3) {
                                            Rlog.d(LOG_TAG, "[COLP]noti.number = " + Rlog.pii(LOG_TAG, noti.number));
                                            if (!(getForegroundCall().getState() == Call.State.IDLE || (cn = (MtkGsmCdmaConnection) getForegroundCall().getConnections().get(0)) == null || cn.getAddress() == null || cn.getAddress().equals(noti.number))) {
                                                cn.setRedirectingAddress(noti.number);
                                                Rlog.d(LOG_TAG, "[COLP]Redirecting address = " + Rlog.pii(LOG_TAG, cn.getRedirectingAddress()));
                                            }
                                        }
                                        if (this.mCallRelatedSuppSvcRegistrants.size() == 0) {
                                            this.mCachedCrssn = ar12;
                                        }
                                        this.mCallRelatedSuppSvcRegistrants.notifyRegistrants(ar12);
                                        return;
                                    case 1004:
                                        AsyncResult ar13 = this.mCallbackLatch;
                                        if (ar13 != null) {
                                            ar13.countDown();
                                        }
                                        Rlog.d(LOG_TAG, "EVENT_SET_SS_PROPERTY done");
                                        return;
                                    default:
                                        switch (i) {
                                            case EVENT_GET_CLIR_COMPLETE /*{ENCODED_INT: 2004}*/:
                                                Rlog.d(LOG_TAG, "EVENT_GET_CLIR_COMPLETE");
                                                AsyncResult ar14 = (AsyncResult) msg.obj;
                                                if (ar14.exception != null && (ar14.exception instanceof CommandException)) {
                                                    CommandException cmdException6 = ar14.exception;
                                                    Rlog.d(LOG_TAG, "EVENT_GET_CLIR_COMPLETE: cmdException error:" + cmdException6.getCommandError());
                                                    if (supportMdAutoSetupIms()) {
                                                        if (isOp(MtkOperatorUtils.OPID.OP01) || isOp(MtkOperatorUtils.OPID.OP02)) {
                                                            if (isUtError(cmdException6.getCommandError())) {
                                                                Rlog.d(LOG_TAG, "return REQUEST_NOT_SUPPORTED");
                                                                ar14.exception = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                                                            } else {
                                                                Rlog.d(LOG_TAG, "return Original Error");
                                                            }
                                                        } else if (cmdException6.getCommandError() == CommandException.Error.OEM_ERROR_25) {
                                                            Rlog.d(LOG_TAG, "cmdException.getMessage():" + cmdException6.getMessage());
                                                            if (!(cmdException6.getMessage() == null || !cmdException6.getMessage().isEmpty() || (ssHelper6 = MtkSuppServManager.getSuppServHelper(getPhoneId())) == null || (errorMsg6 = ssHelper6.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25)) == null || errorMsg6.isEmpty())) {
                                                                ar14.exception = new CommandException(CommandException.Error.OEM_ERROR_25, errorMsg6);
                                                            }
                                                        }
                                                    }
                                                }
                                                Message onComplete6 = (Message) ar14.userObj;
                                                if (onComplete6 != null) {
                                                    AsyncResult.forMessage(onComplete6, ar14.result, ar14.exception);
                                                    onComplete6.sendToTarget();
                                                    return;
                                                }
                                                return;
                                            case EVENT_SET_CALL_BARRING_COMPLETE /*{ENCODED_INT: 2005}*/:
                                                Rlog.d(LOG_TAG, "EVENT_SET_CALL_BARRING_COMPLETE");
                                                AsyncResult ar15 = (AsyncResult) msg.obj;
                                                if (ar15.exception != null && (ar15.exception instanceof CommandException)) {
                                                    CommandException cmdException7 = ar15.exception;
                                                    Rlog.d(LOG_TAG, "EVENT_SET_CALL_BARRING_COMPLETE: cmdException error:" + cmdException7.getCommandError());
                                                    if (supportMdAutoSetupIms()) {
                                                        if (isOp(MtkOperatorUtils.OPID.OP01)) {
                                                            if (isUtError(cmdException7.getCommandError())) {
                                                                Rlog.d(LOG_TAG, "return REQUEST_NOT_SUPPORTED");
                                                                ar15.exception = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                                                            } else {
                                                                Rlog.d(LOG_TAG, "return Original Error");
                                                            }
                                                        } else if (cmdException7.getCommandError() == CommandException.Error.OEM_ERROR_25 && cmdException7.getMessage() != null && cmdException7.getMessage().isEmpty() && (ssHelper7 = MtkSuppServManager.getSuppServHelper(getPhoneId())) != null && (errorMsg7 = ssHelper7.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25)) != null && !errorMsg7.isEmpty()) {
                                                            ar15.exception = new CommandException(CommandException.Error.OEM_ERROR_25, errorMsg7);
                                                        }
                                                    }
                                                }
                                                Message onComplete7 = (Message) ar15.userObj;
                                                if (onComplete7 != null) {
                                                    AsyncResult.forMessage(onComplete7, ar15.result, ar15.exception);
                                                    onComplete7.sendToTarget();
                                                    return;
                                                }
                                                return;
                                            case EVENT_GET_CALL_BARRING_COMPLETE /*{ENCODED_INT: 2006}*/:
                                                Rlog.d(LOG_TAG, "EVENT_GET_CALL_BARRING_COMPLETE");
                                                AsyncResult ar16 = (AsyncResult) msg.obj;
                                                if (ar16.exception != null && (ar16.exception instanceof CommandException)) {
                                                    CommandException cmdException8 = ar16.exception;
                                                    Rlog.d(LOG_TAG, "EVENT_GET_CALL_BARRING_COMPLETE: cmdException error:" + cmdException8.getCommandError());
                                                    if (supportMdAutoSetupIms()) {
                                                        if (isOp(MtkOperatorUtils.OPID.OP01) || isOp(MtkOperatorUtils.OPID.OP09)) {
                                                            if (isUtError(cmdException8.getCommandError())) {
                                                                Rlog.d(LOG_TAG, "return REQUEST_NOT_SUPPORTED");
                                                                ar16.exception = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                                                            } else {
                                                                Rlog.d(LOG_TAG, "return Original Error");
                                                            }
                                                        } else if (cmdException8.getCommandError() == CommandException.Error.OEM_ERROR_25 && cmdException8.getMessage() != null && cmdException8.getMessage().isEmpty() && (ssHelper8 = MtkSuppServManager.getSuppServHelper(getPhoneId())) != null && (errorMsg8 = ssHelper8.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25)) != null && !errorMsg8.isEmpty()) {
                                                            ar16.exception = new CommandException(CommandException.Error.OEM_ERROR_25, errorMsg8);
                                                        }
                                                    }
                                                }
                                                Message onComplete8 = (Message) ar16.userObj;
                                                if (onComplete8 != null) {
                                                    AsyncResult.forMessage(onComplete8, ar16.result, ar16.exception);
                                                    onComplete8.sendToTarget();
                                                    return;
                                                }
                                                return;
                                            default:
                                                MtkGsmCdmaPhone.super.handleMessage(msg);
                                                return;
                                        }
                                }
                        }
                    } else {
                        handleImsUtCsfb(msg);
                    }
                } else if (!isPhoneTypeGsm()) {
                    boolean inEcm = isInEcm();
                    MtkGsmCdmaPhone.super.handleMessage(msg);
                    if (!inEcm) {
                        this.mDataEnabledSettings.setInternalDataEnabled(false);
                        notifyEmergencyCallRegistrants(true);
                    }
                } else {
                    MtkGsmCdmaPhone.super.handleMessage(msg);
                }
            } else if (supportMdAutoSetupIms()) {
                AsyncResult ar17 = (AsyncResult) msg.obj;
                IccRecords r = (IccRecords) this.mIccRecords.get();
                Cfu cfu = (Cfu) ar17.userObj;
                if (ar17.exception != null || r == null) {
                    if (supportMdAutoSetupIms()) {
                        CommandException cmdException9 = ar17.exception;
                        if (cmdException9.getCommandError() == CommandException.Error.OEM_ERROR_25 && cmdException9.getMessage() != null && cmdException9.getMessage().isEmpty() && (ssHelper = MtkSuppServManager.getSuppServHelper(getPhoneId())) != null && (errorMsg = ssHelper.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25)) != null && !errorMsg.isEmpty()) {
                            ar17.exception = new CommandException(CommandException.Error.OEM_ERROR_25, errorMsg);
                        }
                    }
                } else if ((cfu.mServiceClass & 1) != 0) {
                    if (msg.arg1 == 1) {
                        exception = true;
                    }
                    setVoiceCallForwardingFlag(1, exception, cfu.mSetCfNumber);
                }
                if (cfu.mOnComplete != null) {
                    AsyncResult.forMessage(cfu.mOnComplete, ar17.result, ar17.exception);
                    cfu.mOnComplete.sendToTarget();
                }
            } else {
                AsyncResult ar18 = (AsyncResult) msg.obj;
                IccRecords r2 = (IccRecords) this.mIccRecords.get();
                Cfu cfu2 = (Cfu) ar18.userObj;
                if (ar18.exception == null && r2 != null) {
                    if (!queryCFUAgainAfterSet()) {
                        if (msg.arg1 == 1) {
                            exception = true;
                        }
                        setVoiceCallForwardingFlag(1, exception, cfu2.mSetCfNumber);
                    } else if (ar18.result != null) {
                        CallForwardInfo[] cfinfo = (CallForwardInfo[]) ar18.result;
                        if (cfinfo == null || cfinfo.length == 0) {
                            Rlog.d(LOG_TAG, "cfinfo is null or length is 0.");
                        } else {
                            Rlog.d(LOG_TAG, "[EVENT_SET_CALL_FORWARD_DONE] check cfinfo");
                            int i2 = 0;
                            while (true) {
                                if (i2 >= cfinfo.length) {
                                    break;
                                } else if ((cfinfo[i2].serviceClass & 1) != 0) {
                                    if (cfinfo[i2].status == 1) {
                                        exception = true;
                                    }
                                    setVoiceCallForwardingFlag(1, exception, cfinfo[i2].number);
                                } else {
                                    i2++;
                                }
                            }
                        }
                    } else {
                        Rlog.e(LOG_TAG, "EVENT_SET_CALL_FORWARD_DONE: ar.result is null.");
                    }
                }
                if (cfu2.mOnComplete != null) {
                    AsyncResult.forMessage(cfu2.mOnComplete, ar18.result, ar18.exception);
                    cfu2.mOnComplete.sendToTarget();
                }
            }
        } else {
            MtkGsmCdmaPhone.super.handleMessage(msg);
            updateVoiceMail();
        }
    }

    public void setApcMode(int apcMode, boolean reportOn, int reportInterval) {
        if (isPhoneTypeGsm()) {
            this.mMtkCi.setApcMode(apcMode, reportOn, reportInterval, null);
        } else {
            Rlog.d(LOG_TAG, "setApcMode: not possible in CDMA");
        }
    }

    private class PseudoCellInfoResult {
        PseudoCellInfo infos;
        Object lockObj;

        private PseudoCellInfoResult() {
            this.infos = null;
            this.lockObj = new Object();
        }
    }

    public PseudoCellInfo getApcInfo() {
        if (isPhoneTypeGsm()) {
            PseudoCellInfoResult result = new PseudoCellInfoResult();
            synchronized (result.lockObj) {
                result.infos = null;
                this.mMtkCi.getApcInfo(obtainMessage(1001, result));
                try {
                    result.lockObj.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (result.lockObj) {
                if (result.infos != null) {
                    Rlog.d(LOG_TAG, "getApcInfo return: list.size = " + result.infos.toString());
                    PseudoCellInfo pseudoCellInfo = result.infos;
                    return pseudoCellInfo;
                }
                Rlog.d(LOG_TAG, "getApcInfo return null");
            }
        } else {
            Rlog.d(LOG_TAG, "getApcInfo: not possible in CDMA");
        }
        return null;
    }

    public void registerForCrssSuppServiceNotification(Handler h, int what, Object obj) {
        this.mCallRelatedSuppSvcRegistrants.addUnique(h, what, obj);
        AsyncResult asyncResult = this.mCachedCrssn;
        if (asyncResult != null) {
            this.mCallRelatedSuppSvcRegistrants.notifyRegistrants(asyncResult);
            this.mCachedCrssn = null;
        }
    }

    public void unregisterForCrssSuppServiceNotification(Handler h) {
        this.mCallRelatedSuppSvcRegistrants.remove(h);
        this.mCachedCrssn = null;
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrants.addUnique(h, what, obj);
        if (this.mCachedSsn != null) {
            this.mSsnRegistrants.notifyRegistrants(this.mCachedSsn);
            this.mCachedSsn = null;
        }
    }

    public void unregisterForSuppServiceNotification(Handler h) {
        this.mSsnRegistrants.remove(h);
        this.mCachedSsn = null;
    }

    public boolean handleInCallMmiCommands(String dialString) throws CallStateException {
        if (!isPhoneTypeGsm()) {
            loge("method handleInCallMmiCommands is NOT supported in CDMA!");
            return false;
        }
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null && imsPhone.getServiceState().getState() == 0 && !isInCSCall()) {
            return imsPhone.handleInCallMmiCommands(dialString);
        }
        if (!isInCall() || TextUtils.isEmpty(dialString)) {
            return false;
        }
        switch (dialString.charAt(0)) {
            case '0':
                return handleUdubIncallSupplementaryService(dialString);
            case PplMessageManager.PendingMessage.PENDING_MESSAGE_LENGTH /*{ENCODED_INT: 49}*/:
                return handleCallWaitingIncallSupplementaryService(dialString);
            case IWorldPhone.EVENT_QUERY_MODEM_TYPE /*{ENCODED_INT: 50}*/:
                return handleCallHoldIncallSupplementaryService(dialString);
            case '3':
                return handleMultipartyIncallSupplementaryService(dialString);
            case '4':
                return handleEctIncallSupplementaryService(dialString);
            case MtkWspTypeDecoder.CONTENT_TYPE_B_CONNECTIVITY /*{ENCODED_INT: 53}*/:
                return handleCcbsIncallSupplementaryService(dialString);
            default:
                return false;
        }
    }

    private boolean handleUdubIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (!(getRingingCall().getState() == Call.State.IDLE && getBackgroundCall().getState() == Call.State.IDLE)) {
            Rlog.d(LOG_TAG, "MmiCode 0: hangupWaitingOrBackground");
            this.mCT.hangupWaitingOrBackground();
        }
        return true;
    }

    public void queryPhbStorageInfo(int type, Message response) {
        if (!CsimPhbUtil.hasModemPhbEnhanceCapability(getIccFileHandler())) {
            CsimPhbUtil.getPhbRecordInfo(response);
        } else {
            this.mMtkCi.queryPhbStorageInfo(type, response);
        }
    }

    public void registerForNetworkInfo(Handler h, int what, Object obj) {
        this.mMtkCi.registerForNetworkInfo(h, what, obj);
    }

    public void unregisterForNetworkInfo(Handler h) {
        this.mMtkCi.unregisterForNetworkInfo(h);
    }

    public void setRxTestConfig(int AntType, Message result) {
        Rlog.d(LOG_TAG, "set Rx Test Config");
        this.mMtkCi.setRxTestConfig(AntType, result);
    }

    public void getRxTestResult(Message result) {
        Rlog.d(LOG_TAG, "get Rx Test Result");
        this.mMtkCi.getRxTestResult(result);
    }

    public void getPolCapability(Message onComplete) {
        this.mMtkCi.getPOLCapability(onComplete);
    }

    public void getPol(Message onComplete) {
        this.mMtkCi.getCurrentPOLList(onComplete);
    }

    public void setPolEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
        this.mMtkCi.setPOLEntry(networkWithAct.getPriority(), networkWithAct.getOperatorNumeric(), networkWithAct.getAccessTechnology(), onComplete);
    }

    public List<? extends MmiCode> getPendingMmiCodes() {
        Rlog.d(LOG_TAG, "getPendingMmiCodes");
        dumpPendingMmi();
        ImsPhone imsPhone = this.mImsPhone;
        ArrayList<MmiCode> imsphonePendingMMIs = new ArrayList<>();
        if (imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            for (ImsPhoneMmiCode mmi : imsPhone.getPendingMmiCodes()) {
                imsphonePendingMMIs.add(mmi);
            }
        }
        ArrayList<MmiCode> allPendingMMIs = new ArrayList<>(this.mPendingMMIs);
        allPendingMMIs.addAll(imsphonePendingMMIs);
        Rlog.d(LOG_TAG, "allPendingMMIs.size() = " + allPendingMMIs.size());
        int s = allPendingMMIs.size();
        for (int i = 0; i < s; i++) {
            Rlog.d(LOG_TAG, "dump allPendingMMIs: " + allPendingMMIs.get(i));
        }
        return allPendingMMIs;
    }

    public void notifyCallForwardingIndicator() {
        int simState = TelephonyManager.from(this.mContext).getSimState(this.mPhoneId);
        Rlog.d(LOG_TAG, "notifyCallForwardingIndicator: sim state = " + simState);
        if (simState == 5) {
            this.mNotifier.notifyCallForwardingChanged(this);
        }
    }

    public void notifyCallForwardingIndicatorWithoutCheckSimState() {
        Rlog.d(LOG_TAG, "notifyCallForwardingIndicatorWithoutCheckSimState");
        this.mNotifier.notifyCallForwardingChanged(this);
    }

    public boolean handlePinMmi(String dialString) {
        MmiCode mmi;
        if (isPhoneTypeGsm()) {
            mmi = MtkGsmMmiCode.newFromDialString(dialString, this, (UiccCardApplication) this.mUiccApplication.get(), null);
        } else {
            mmi = CdmaMmiCode.newFromDialString(dialString, this, (UiccCardApplication) this.mUiccApplication.get());
        }
        if (mmi == null || !mmi.isPinPukCommand()) {
            loge("Mmi is null or unrecognized!");
            return false;
        }
        this.mPendingMMIs.add(mmi);
        Rlog.d(LOG_TAG, "handlePinMmi: " + MtkSuppServHelper.encryptString(dialString) + ", mmi=" + mmi);
        dumpPendingMmi();
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
        try {
            mmi.processCode();
            return true;
        } catch (CallStateException e) {
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isImsUtEnabledOverCdma() {
        if (isGsmSsPrefer()) {
            return true;
        }
        if (!isPhoneTypeCdmaLte() || this.mImsPhone == null || !this.mImsPhone.isUtEnabled()) {
            return false;
        }
        return true;
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        getCallForwardingOptionForServiceClass(commandInterfaceCFReason, 1, onComplete);
    }

    public void getCallForwardingOptionForServiceClass(int commandInterfaceCFReason, int serviceClass, Message onComplete) {
        MtkSuppServQueueHelper ssQueueHelper = MtkSuppServManager.getSuppServQueueHelper();
        if (ssQueueHelper != null) {
            ssQueueHelper.getCallForwardingOptionForServiceClass(commandInterfaceCFReason, serviceClass, onComplete, getPhoneId());
            return;
        }
        Rlog.d(LOG_TAG, "ssQueueHelper not exist, getCallForwardingOptionForServiceClass");
        getCallForwardingOptionInternal(commandInterfaceCFReason, serviceClass, onComplete);
    }

    public void getCallForwardingOptionInternal(int commandInterfaceCFReason, int serviceClass, Message onComplete) {
        Message resp;
        Message resp2;
        if (isPhoneTypeGsm() || isImsUtEnabledOverCdma()) {
            Phone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "getCallForwardingOptionForServiceClass enter, CFReason:" + commandInterfaceCFReason + ", serviceClass:" + serviceClass);
                if (imsPhone != null && (imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    ((MtkImsPhone) imsPhone).getCallForwardingOptionForServiceClass(commandInterfaceCFReason, serviceClass, onComplete);
                } else if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                    logd("requesting call forwarding query.");
                    if (commandInterfaceCFReason == 0) {
                        resp2 = obtainMessage(13, onComplete);
                    } else {
                        resp2 = onComplete;
                    }
                    this.mCi.queryCallForwardStatus(commandInterfaceCFReason, serviceClass, (String) null, resp2);
                }
            } else if (getCsFallbackStatus() == 0 && imsPhone != null && ((imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled()) && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport())))) {
                MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(12, onComplete);
                ss.mParcel.writeInt(commandInterfaceCFReason);
                ss.mParcel.writeInt(serviceClass);
                ((MtkImsPhone) imsPhone).getCallForwardingOptionForServiceClass(commandInterfaceCFReason, serviceClass, obtainMessage(EVENT_IMS_UT_DONE, ss));
            } else if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                logd("requesting call forwarding query.");
                if (commandInterfaceCFReason == 0) {
                    resp = obtainMessage(13, onComplete);
                } else {
                    resp = onComplete;
                }
                if (getCsFallbackStatus() != 0 || !isGsmUtSupport()) {
                    if (getCsFallbackStatus() == 1) {
                        setCsFallbackStatus(0);
                    }
                    if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                    } else if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                    } else {
                        Rlog.d(LOG_TAG, "mCi.queryCallForwardStatus.");
                        this.mCi.queryCallForwardStatus(commandInterfaceCFReason, serviceClass, (String) null, resp);
                    }
                } else if (!isInCSCall() || getPhoneType() != 2) {
                    this.mMtkSSReqDecisionMaker.queryCallForwardStatus(commandInterfaceCFReason, serviceClass, null, resp);
                } else {
                    sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                }
            }
        } else {
            loge("getCallForwardingOptionForServiceClass: not possible in CDMA");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        }
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        setCallForwardingOptionForServiceClass(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, 1, onComplete);
    }

    public void setCallForwardingOptionForServiceClass(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, int serviceClass, Message onComplete) {
        MtkSuppServQueueHelper ssQueueHelper = MtkSuppServManager.getSuppServQueueHelper();
        if (ssQueueHelper != null) {
            ssQueueHelper.setCallForwardingOptionForServiceClass(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, serviceClass, onComplete, getPhoneId());
            return;
        }
        Rlog.d(LOG_TAG, "ssQueueHelper not exist, setCallForwardingOptionForServiceClass");
        setCallForwardingOptionInternal(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, serviceClass, onComplete);
    }

    /* JADX INFO: Multiple debug info for r1v21 android.os.Message: [D('cfu' com.mediatek.internal.telephony.MtkGsmCdmaPhone$Cfu), D('resp' android.os.Message)] */
    /* JADX INFO: Multiple debug info for r1v44 android.os.Message: [D('cfu' com.mediatek.internal.telephony.MtkGsmCdmaPhone$Cfu), D('resp' android.os.Message)] */
    public void setCallForwardingOptionInternal(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, int serviceClass, Message onComplete) {
        Message resp;
        Message resp2;
        if (isPhoneTypeGsm() || isImsUtEnabledOverCdma()) {
            String dialingNumber2 = MtkGsmCdmaPhone.super.handlePreCheckCFDialingNumber(dialingNumber);
            Phone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "setCallForwardingOptionForServiceClass enter, CFAction:" + commandInterfaceCFAction + ", CFReason:" + commandInterfaceCFReason + ", dialingNumber:" + Rlog.pii(LOG_TAG, dialingNumber2) + ", timerSeconds:" + timerSeconds + ", serviceClass:" + serviceClass);
                if (imsPhone != null && (imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    ((MtkImsPhone) imsPhone).setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber2, serviceClass, timerSeconds, onComplete);
                } else if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                    if (commandInterfaceCFReason == 0) {
                        resp2 = obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cfu(dialingNumber2, onComplete, serviceClass));
                    } else {
                        resp2 = onComplete;
                    }
                    this.mCi.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, serviceClass, dialingNumber2, timerSeconds, resp2);
                }
            } else if (getCsFallbackStatus() == 0 && imsPhone != null && ((imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled()) && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport())))) {
                MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(11, onComplete);
                ss.mParcel.writeInt(commandInterfaceCFAction);
                ss.mParcel.writeInt(commandInterfaceCFReason);
                ss.mParcel.writeString(dialingNumber2);
                ss.mParcel.writeInt(timerSeconds);
                ss.mParcel.writeInt(serviceClass);
                ((ImsPhone) imsPhone).setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber2, serviceClass, timerSeconds, obtainMessage(EVENT_IMS_UT_DONE, ss));
            } else if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                if (commandInterfaceCFReason == 0) {
                    resp = obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cfu(dialingNumber2, onComplete, serviceClass));
                } else {
                    resp = onComplete;
                }
                if (getCsFallbackStatus() != 0 || !isGsmUtSupport()) {
                    if (getCsFallbackStatus() == 1) {
                        setCsFallbackStatus(0);
                    }
                    if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                    } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                    } else {
                        this.mCi.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, serviceClass, dialingNumber2, timerSeconds, resp);
                    }
                } else if (!isInCSCall() || getPhoneType() != 2) {
                    this.mMtkSSReqDecisionMaker.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, serviceClass, dialingNumber2, timerSeconds, resp);
                } else {
                    sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                }
            }
        } else {
            loge("setCallForwardingOption: not possible in CDMA");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        }
    }

    private static class CfuEx {
        final Message mOnComplete;
        final String mSetCfNumber;
        final long[] mSetTimeSlot;

        CfuEx(String cfNumber, long[] cfTimeSlot, Message onComplete) {
            this.mSetCfNumber = cfNumber;
            this.mSetTimeSlot = cfTimeSlot;
            this.mOnComplete = onComplete;
        }
    }

    public void saveTimeSlot(long[] timeSlot) {
        String timeSlotKey = CFU_TIME_SLOT + this.mPhoneId;
        String timeSlotString = "";
        if (timeSlot != null && timeSlot.length == 2) {
            timeSlotString = Long.toString(timeSlot[0]) + "," + Long.toString(timeSlot[1]);
        }
        SystemProperties.set(timeSlotKey, timeSlotString);
        Rlog.d(LOG_TAG, "timeSlotString = " + timeSlotString);
    }

    public long[] getTimeSlot() {
        String timeSlotString = SystemProperties.get(CFU_TIME_SLOT + this.mPhoneId, "");
        long[] timeSlot = null;
        if (timeSlotString != null && !timeSlotString.equals("")) {
            String[] timeArray = timeSlotString.split(",");
            if (timeArray.length == 2) {
                timeSlot = new long[2];
                for (int i = 0; i < 2; i++) {
                    timeSlot[i] = Long.parseLong(timeArray[i]);
                    Calendar calenar = Calendar.getInstance(TimeZone.getDefault());
                    calenar.setTimeInMillis(timeSlot[i]);
                    int hour = calenar.get(11);
                    int min = calenar.get(12);
                    Calendar calenar2 = Calendar.getInstance(TimeZone.getDefault());
                    calenar2.set(11, hour);
                    calenar2.set(12, min);
                    timeSlot[i] = calenar2.getTimeInMillis();
                }
            }
        }
        Rlog.d(LOG_TAG, "timeSlot = " + Arrays.toString(timeSlot));
        return timeSlot;
    }

    public void getCallForwardInTimeSlot(int commandInterfaceCFReason, Message onComplete) {
        if (isPhoneTypeGsm()) {
            ImsPhone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "getCallForwardInTimeSlot enter, CFReason:" + commandInterfaceCFReason);
                if (imsPhone != null && (imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    ((MtkImsPhone) imsPhone).getCallForwardInTimeSlot(commandInterfaceCFReason, onComplete);
                } else if (commandInterfaceCFReason == 0) {
                    Rlog.d(LOG_TAG, "requesting call forwarding in time slot query.");
                    this.mMtkCi.queryCallForwardInTimeSlotStatus(commandInterfaceCFReason, 0, obtainMessage(109, onComplete));
                }
            } else if (getCsFallbackStatus() == 0 && imsPhone != null && ((imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled()) && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport())))) {
                ((MtkImsPhone) imsPhone).getCallForwardInTimeSlot(commandInterfaceCFReason, onComplete);
            } else if (commandInterfaceCFReason == 0) {
                Rlog.d(LOG_TAG, "requesting call forwarding in time slot query.");
                Message resp = obtainMessage(109, onComplete);
                if (getCsFallbackStatus() != 0 || !isGsmUtSupport()) {
                    sendErrorResponse(onComplete, CommandException.Error.REQUEST_NOT_SUPPORTED);
                } else {
                    this.mMtkSSReqDecisionMaker.queryCallForwardInTimeSlotStatus(commandInterfaceCFReason, 1, resp);
                }
            } else if (onComplete != null) {
                sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
            }
        } else {
            loge("method getCallForwardInTimeSlot is NOT supported in CDMA!");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        }
    }

    public void setCallForwardInTimeSlot(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, long[] timeSlot, Message onComplete) {
        if (isPhoneTypeGsm()) {
            ImsPhone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "setCallForwardInTimeSlot enter, CFReason:" + commandInterfaceCFReason + ", CFAction:" + commandInterfaceCFAction + ", dialingNumber:" + dialingNumber + ", timerSeconds:" + timerSeconds);
                if (imsPhone != null && (imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    ((MtkImsPhone) imsPhone).setCallForwardInTimeSlot(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, timeSlot, onComplete);
                } else if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && commandInterfaceCFReason == 0) {
                    this.mMtkCi.setCallForwardInTimeSlot(commandInterfaceCFAction, commandInterfaceCFReason, 1, dialingNumber, timerSeconds, timeSlot, obtainMessage(110, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new CfuEx(dialingNumber, timeSlot, onComplete)));
                }
            } else if (getCsFallbackStatus() == 0 && isOp(MtkOperatorUtils.OPID.OP01) && imsPhone != null && imsPhone.getServiceState().getState() == 0 && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport()))) {
                MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(17, onComplete);
                ss.mParcel.writeInt(commandInterfaceCFAction);
                ss.mParcel.writeInt(commandInterfaceCFReason);
                ss.mParcel.writeString(dialingNumber);
                ss.mParcel.writeInt(timerSeconds);
                ((MtkImsPhone) imsPhone).setCallForwardInTimeSlot(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, timeSlot, obtainMessage(EVENT_IMS_UT_DONE, ss));
            } else if (!isValidCommandInterfaceCFAction(commandInterfaceCFAction) || commandInterfaceCFReason != 0) {
                sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
            } else {
                Message resp = obtainMessage(110, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new CfuEx(dialingNumber, timeSlot, onComplete));
                if (getCsFallbackStatus() == 0) {
                    if (isGsmUtSupport()) {
                        this.mMtkSSReqDecisionMaker.setCallForwardInTimeSlot(commandInterfaceCFAction, commandInterfaceCFReason, 1, dialingNumber, timerSeconds, timeSlot, resp);
                        return;
                    }
                }
                sendErrorResponse(onComplete, CommandException.Error.REQUEST_NOT_SUPPORTED);
            }
        } else {
            loge("method setCallForwardInTimeSlot is NOT supported in CDMA!");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        }
    }

    private void handleCfuInTimeSlotQueryResult(MtkCallForwardInfo[] infos) {
        if (((IccRecords) this.mIccRecords.get()) != null) {
            boolean z = false;
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
                    saveTimeSlot(infos[i].timeSlot);
                    return;
                }
            }
        }
    }

    public int[] getSavedClirSetting() {
        int getClirResult;
        int presentationMode;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        int clirSetting = sp.getInt("clir_key" + getPhoneId(), -1);
        if (clirSetting == 0 || clirSetting == -1) {
            presentationMode = 4;
            getClirResult = 0;
        } else if (clirSetting == 1) {
            presentationMode = 3;
            getClirResult = 1;
        } else {
            presentationMode = 4;
            getClirResult = 2;
        }
        int[] getClirResponse = {getClirResult, presentationMode};
        Rlog.i(LOG_TAG, "getClirResult: " + getClirResult);
        Rlog.i(LOG_TAG, "presentationMode: " + presentationMode);
        return getClirResponse;
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        MtkSuppServQueueHelper ssQueueHelper = MtkSuppServManager.getSuppServQueueHelper();
        if (ssQueueHelper != null) {
            ssQueueHelper.getOutgoingCallerIdDisplay(onComplete, getPhoneId());
            return;
        }
        Rlog.d(LOG_TAG, "ssQueueHelper not exist, getOutgoingCallerIdDisplay");
        getOutgoingCallerIdDisplayInternal(onComplete);
    }

    public void getOutgoingCallerIdDisplayInternal(Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            Phone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "getOutgoingCallerIdDisplay enter");
                Message resp = obtainMessage(EVENT_GET_CLIR_COMPLETE, onComplete);
                if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
                    this.mCi.getCLIR(resp);
                } else {
                    imsPhone.getOutgoingCallerIdDisplay(resp);
                }
            } else if (getCsFallbackStatus() != 0 || imsPhone == null || ((imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled()) || (!imsPhone.isVolteEnabled() && (!imsPhone.isWifiCallingEnabled() || !isWFCUtSupport())))) {
                if (getCsFallbackStatus() != 0 || !isGsmUtSupport()) {
                    if (getCsFallbackStatus() == 1) {
                        setCsFallbackStatus(0);
                    }
                    if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                    } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                    } else {
                        this.mCi.getCLIR(onComplete);
                    }
                } else if (!isOpTbClir()) {
                    this.mMtkSSReqDecisionMaker.getCLIR(onComplete);
                } else if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, getSavedClirSetting(), (Throwable) null);
                    onComplete.sendToTarget();
                }
            } else if (isOpNotSupportCallIdentity()) {
                sendErrorResponse(onComplete, CommandException.Error.REQUEST_NOT_SUPPORTED);
            } else if (!isOpTbClir()) {
                imsPhone.getOutgoingCallerIdDisplay(obtainMessage(EVENT_IMS_UT_DONE, MtkSuppSrvRequest.obtain(4, onComplete)));
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete, getSavedClirSetting(), (Throwable) null);
                onComplete.sendToTarget();
            }
        } else {
            loge("getOutgoingCallerIdDisplay: not possible in CDMA");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        }
    }

    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode, Message onComplete) {
        MtkSuppServQueueHelper ssQueueHelper = MtkSuppServManager.getSuppServQueueHelper();
        if (ssQueueHelper != null) {
            ssQueueHelper.setOutgoingCallerIdDisplay(commandInterfaceCLIRMode, onComplete, getPhoneId());
            return;
        }
        Rlog.d(LOG_TAG, "ssQueueHelper not exist, setOutgoingCallerIdDisplay");
        setOutgoingCallerIdDisplayInternal(commandInterfaceCLIRMode, onComplete);
    }

    public void setOutgoingCallerIdDisplayInternal(int commandInterfaceCLIRMode, Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            Phone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "setOutgoingCallerIdDisplay enter, CLIRmode:" + commandInterfaceCLIRMode);
                Message resp = obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete);
                if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
                    this.mCi.setCLIR(commandInterfaceCLIRMode, resp);
                } else {
                    imsPhone.setOutgoingCallerIdDisplay(commandInterfaceCLIRMode, resp);
                }
            } else if (getCsFallbackStatus() != 0 || imsPhone == null || ((imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled()) || (!imsPhone.isVolteEnabled() && (!imsPhone.isWifiCallingEnabled() || !isWFCUtSupport())))) {
                if (getCsFallbackStatus() != 0 || !isGsmUtSupport()) {
                    if (getCsFallbackStatus() == 1) {
                        setCsFallbackStatus(0);
                    }
                    if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                    } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                    } else {
                        this.mCi.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
                    }
                } else if (!isOpTbClir()) {
                    this.mMtkSSReqDecisionMaker.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
                } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                } else {
                    this.mCi.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
                }
            } else if (isOpNotSupportCallIdentity()) {
                sendErrorResponse(onComplete, CommandException.Error.REQUEST_NOT_SUPPORTED);
            } else if (!isOpTbClir()) {
                MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(3, onComplete);
                ss.mParcel.writeInt(commandInterfaceCLIRMode);
                imsPhone.setOutgoingCallerIdDisplay(commandInterfaceCLIRMode, obtainMessage(EVENT_IMS_UT_DONE, ss));
            } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
            } else {
                this.mCi.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
            }
        } else {
            loge("setOutgoingCallerIdDisplay: not possible in CDMA");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        }
    }

    private void initTbcwMode() {
        if (this.mTbcwMode != 0) {
            Rlog.d(LOG_TAG, "initTbcwMode, mTbcwMode is not UNKNOWN, no need to init");
            return;
        }
        IccCard iccCard = getIccCard();
        String simType = iccCard.getIccCardType();
        if (!iccCard.hasIccCard() || simType == null || simType.equals("")) {
            Rlog.d(LOG_TAG, "initTbcwMode, IccCard is not ready. mTbcwMode ramains UNKNOWN");
        } else {
            this.mExecutorService.submit(new Runnable() {
                /* class com.mediatek.internal.telephony.MtkGsmCdmaPhone.AnonymousClass2 */

                public void run() {
                    if (MtkGsmCdmaPhone.this.isOpTbcwWithCS()) {
                        MtkGsmCdmaPhone.this.setTbcwMode(3);
                        MtkGsmCdmaPhone.this.setTbcwToEnabledOnIfDisabled();
                    } else if (!MtkGsmCdmaPhone.this.isUsimCard()) {
                        MtkGsmCdmaPhone.this.setTbcwMode(2);
                        MtkGsmCdmaPhone mtkGsmCdmaPhone = MtkGsmCdmaPhone.this;
                        mtkGsmCdmaPhone.setSSPropertyThroughHidl(mtkGsmCdmaPhone.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "disabled_tbcw");
                    }
                    Rlog.d(MtkGsmCdmaPhone.LOG_TAG, "initTbcwMode: " + MtkGsmCdmaPhone.this.mTbcwMode);
                }
            });
        }
    }

    public int getTbcwMode() {
        initTbcwMode();
        return this.mTbcwMode;
    }

    public void setTbcwMode(int newMode) {
        Rlog.d(LOG_TAG, "Set tbcwmode: " + newMode + ", phoneId: " + getPhoneId());
        this.mTbcwMode = newMode;
    }

    public void setTbcwToEnabledOnIfDisabled() {
        String tbcwMode = TelephonyManager.getTelephonyProperty(getPhoneId(), "persist.vendor.radio.terminal-based.cw", "disabled_tbcw");
        Rlog.d(LOG_TAG, "setTbcwToEnabledOnIfDisabled tbcwmode: " + tbcwMode + ", status: " + tbcwMode.equals(""));
        if ("disabled_tbcw".equals(tbcwMode) || tbcwMode.equals("")) {
            setSSPropertyThroughHidl(getPhoneId(), "persist.vendor.radio.terminal-based.cw", "enabled_tbcw_on");
        }
    }

    public void getTerminalBasedCallWaiting(final Message onComplete) {
        try {
            boolean result = ((Boolean) this.mExecutorService.submit(new Callable() {
                /* class com.mediatek.internal.telephony.MtkGsmCdmaPhone.AnonymousClass3 */

                @Override // java.util.concurrent.Callable
                public Object call() throws Exception {
                    String tbcwMode = TelephonyManager.getTelephonyProperty(MtkGsmCdmaPhone.this.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "disabled_tbcw");
                    Rlog.d(MtkGsmCdmaPhone.LOG_TAG, "getTerminalBasedCallWaiting(): tbcwMode = " + tbcwMode + ", onComplete = " + onComplete);
                    if ("enabled_tbcw_on".equals(tbcwMode)) {
                        if (onComplete != null) {
                            Thread.sleep(1000);
                            AsyncResult.forMessage(onComplete, new int[]{1, 1}, (Throwable) null);
                            onComplete.sendToTarget();
                        }
                        return true;
                    } else if (!"enabled_tbcw_off".equals(tbcwMode)) {
                        return false;
                    } else {
                        if (onComplete != null) {
                            Thread.sleep(1000);
                            int[] cwInfos = new int[2];
                            cwInfos[0] = 0;
                            AsyncResult.forMessage(onComplete, cwInfos, (Throwable) null);
                            onComplete.sendToTarget();
                        }
                        return true;
                    }
                }
            }).get()).booleanValue();
            Rlog.d(LOG_TAG, "getTerminalBasedCallWaiting future get = " + result);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "getTerminalBasedCallWaiting Exception occured");
        }
    }

    public void getCallWaiting(Message onComplete) {
        MtkSuppServQueueHelper ssQueueHelper = MtkSuppServManager.getSuppServQueueHelper();
        if (ssQueueHelper != null) {
            ssQueueHelper.getCallWaiting(onComplete, getPhoneId());
            return;
        }
        Rlog.d(LOG_TAG, "ssQueueHelper not exist, getCallWaiting");
        getCallWaitingInternal(onComplete);
    }

    public void getCallWaitingInternal(Message onComplete) {
        if (isPhoneTypeGsm() || isImsUtEnabledOverCdma()) {
            Phone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "getCallWaiting enter");
                if (imsPhone == null || (imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled())) {
                    this.mCi.queryCallWaiting(0, onComplete);
                } else {
                    imsPhone.getCallWaiting(onComplete);
                }
            } else {
                if (!isOpNwCW()) {
                    if (this.mTbcwMode == 0) {
                        initTbcwMode();
                    }
                    Rlog.d(LOG_TAG, "getCallWaiting(): mTbcwMode = " + this.mTbcwMode + ", onComplete = " + onComplete);
                    int i = this.mTbcwMode;
                    if (i == 1) {
                        getTerminalBasedCallWaiting(onComplete);
                        return;
                    } else if (i != 2) {
                        if (i == 3) {
                            if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                                sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                                return;
                            }
                            this.mCi.queryCallWaiting(0, obtainMessage(EVENT_GET_CALL_WAITING_DONE, onComplete));
                            return;
                        }
                    } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                        return;
                    } else if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                        return;
                    } else {
                        Rlog.d(LOG_TAG, "mCi.queryCallForwardStatus.");
                        this.mCi.queryCallWaiting(0, onComplete);
                        return;
                    }
                }
                if (getCsFallbackStatus() != 0 || imsPhone == null || ((imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled()) || (!imsPhone.isVolteEnabled() && (!imsPhone.isWifiCallingEnabled() || !isWFCUtSupport())))) {
                    if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                        Rlog.d(LOG_TAG, "mMtkSSReqDecisionMaker.queryCallWaiting");
                        this.mMtkSSReqDecisionMaker.queryCallWaiting(0, onComplete);
                    } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                    } else if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                    } else {
                        Rlog.d(LOG_TAG, "mCi.queryCallForwardStatus.");
                        this.mCi.queryCallWaiting(0, onComplete);
                    }
                } else if (isOpNwCW()) {
                    Rlog.d(LOG_TAG, "isOpNwCW(), getCallWaiting() by Ut interface");
                    imsPhone.getCallWaiting(obtainMessage(EVENT_IMS_UT_DONE, MtkSuppSrvRequest.obtain(14, onComplete)));
                } else {
                    Rlog.d(LOG_TAG, "isOpTbCW(), getTerminalBasedCallWaiting");
                    setTbcwMode(1);
                    setTbcwToEnabledOnIfDisabled();
                    getTerminalBasedCallWaiting(onComplete);
                }
            }
        } else {
            this.mCi.queryCallWaiting(1, onComplete);
        }
    }

    public void setTerminalBasedCallWaiting(final boolean enable, final Message onComplete) {
        this.mExecutorService.submit(new Runnable() {
            /* class com.mediatek.internal.telephony.MtkGsmCdmaPhone.AnonymousClass4 */

            public void run() {
                String tbcwMode = TelephonyManager.getTelephonyProperty(MtkGsmCdmaPhone.this.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "disabled_tbcw");
                Rlog.d(MtkGsmCdmaPhone.LOG_TAG, "setTerminalBasedCallWaiting(): tbcwMode = " + tbcwMode + ", enable = " + enable);
                if ("enabled_tbcw_on".equals(tbcwMode)) {
                    if (!enable) {
                        MtkGsmCdmaPhone mtkGsmCdmaPhone = MtkGsmCdmaPhone.this;
                        mtkGsmCdmaPhone.setSSPropertyThroughHidl(mtkGsmCdmaPhone.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "enabled_tbcw_off");
                    }
                    if (onComplete != null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        AsyncResult.forMessage(onComplete, (Object) null, (Throwable) null);
                        onComplete.sendToTarget();
                    }
                } else if ("enabled_tbcw_off".equals(tbcwMode)) {
                    if (enable) {
                        MtkGsmCdmaPhone mtkGsmCdmaPhone2 = MtkGsmCdmaPhone.this;
                        mtkGsmCdmaPhone2.setSSPropertyThroughHidl(mtkGsmCdmaPhone2.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "enabled_tbcw_on");
                    }
                    if (onComplete != null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                        AsyncResult.forMessage(onComplete, (Object) null, (Throwable) null);
                        onComplete.sendToTarget();
                    }
                } else {
                    Rlog.e(MtkGsmCdmaPhone.LOG_TAG, "setTerminalBasedCallWaiting(): ERROR: tbcwMode = " + tbcwMode);
                }
            }
        });
    }

    public void setSSPropertyThroughHidl(int phoneId, String property, String value) {
        Rlog.d(LOG_TAG, "setSSPropertyThroughHidl, phoneId = " + phoneId + ", name = " + property + ", value = " + value);
        String propVal = "";
        String[] p = null;
        String prop = SystemProperties.get(property);
        if (value == null) {
            value = "";
        }
        if (prop != null) {
            p = prop.split(",");
        }
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            Rlog.d(LOG_TAG, "setSSPropertyThroughHidl: invalid phoneId=" + phoneId + " property=" + property + " value: " + value + " prop=" + prop);
            return;
        }
        for (int i = 0; i < phoneId; i++) {
            String str = "";
            if (p != null && i < p.length) {
                str = p[i];
            }
            propVal = propVal + str + ",";
        }
        String propVal2 = propVal + value;
        if (p != null) {
            for (int i2 = phoneId + 1; i2 < p.length; i2++) {
                propVal2 = propVal2 + "," + p[i2];
            }
        }
        if (propVal2.length() > 91) {
            Rlog.d(LOG_TAG, "setSSPropertyThroughHidl: property too long phoneId=" + phoneId + " property=" + property + " value: " + value + " propVal=" + propVal2);
            return;
        }
        setSuppServProperty(property, propVal2);
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        MtkSuppServQueueHelper ssQueueHelper = MtkSuppServManager.getSuppServQueueHelper();
        if (ssQueueHelper != null) {
            ssQueueHelper.setCallWaiting(enable, onComplete, getPhoneId());
            return;
        }
        Rlog.d(LOG_TAG, "ssQueueHelper not exist, setCallWaiting");
        setCallWaitingInternal(enable, onComplete);
    }

    public void setCallWaitingInternal(boolean enable, Message onComplete) {
        if (isPhoneTypeGsm() || isImsUtEnabledOverCdma()) {
            Phone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "setCallWaiting enter, enable:" + enable);
                if (imsPhone == null || (imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled())) {
                    this.mCi.setCallWaiting(enable, 1, onComplete);
                } else {
                    imsPhone.setCallWaiting(enable, onComplete);
                }
            } else {
                if (!isOpNwCW()) {
                    if (this.mTbcwMode == 0) {
                        initTbcwMode();
                    }
                    Rlog.d(LOG_TAG, "setCallWaiting(): mTbcwMode = " + this.mTbcwMode + ", onComplete = " + onComplete);
                    int i = this.mTbcwMode;
                    if (i == 1) {
                        setTerminalBasedCallWaiting(enable, onComplete);
                        return;
                    } else if (i != 2) {
                        if (i == 3) {
                            if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                                sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                                return;
                            } else {
                                this.mCi.setCallWaiting(enable, 1, obtainMessage(EVENT_SET_CALL_WAITING_DONE, enable ? 1 : 0, 0, onComplete));
                                return;
                            }
                        }
                    } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                        return;
                    } else if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                        return;
                    } else {
                        this.mCi.setCallWaiting(enable, 1, onComplete);
                        return;
                    }
                }
                if (getCsFallbackStatus() != 0 || imsPhone == null || ((imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled()) || (!imsPhone.isVolteEnabled() && (!imsPhone.isWifiCallingEnabled() || !isWFCUtSupport())))) {
                    if (getCsFallbackStatus() != 0 || !isGsmUtSupport()) {
                        if (getCsFallbackStatus() == 1) {
                            setCsFallbackStatus(0);
                        }
                        if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                        } else if (isNotSupportUtToCS()) {
                            sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                        } else {
                            this.mCi.setCallWaiting(enable, 1, onComplete);
                        }
                    } else {
                        Rlog.d(LOG_TAG, "mMtkSSReqDecisionMaker.setCallWaiting");
                        this.mMtkSSReqDecisionMaker.setCallWaiting(enable, 1, onComplete);
                    }
                } else if (isOpNwCW()) {
                    MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(13, onComplete);
                    ss.mParcel.writeInt(enable ? 1 : 0);
                    imsPhone.setCallWaiting(enable, obtainMessage(EVENT_IMS_UT_DONE, ss));
                } else {
                    Rlog.d(LOG_TAG, "isOpTbCW(), setTerminalBasedCallWaiting(): IMS in service");
                    setTbcwMode(1);
                    setTbcwToEnabledOnIfDisabled();
                    setTerminalBasedCallWaiting(enable, onComplete);
                }
            }
        } else {
            loge("method setCallWaiting is NOT supported in CDMA!");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        }
    }

    public void getCallBarring(String facility, String password, Message onComplete, int serviceClass) {
        MtkSuppServQueueHelper ssQueueHelper = MtkSuppServManager.getSuppServQueueHelper();
        if (ssQueueHelper != null) {
            ssQueueHelper.getCallBarring(facility, password, serviceClass, onComplete, getPhoneId());
            return;
        }
        Rlog.d(LOG_TAG, "ssQueueHelper not exist, getCallBarringInternal");
        getCallBarringInternal(facility, password, onComplete, serviceClass);
    }

    public void getCallBarring(String facility, String password, Message onComplete) {
        getCallBarring(facility, password, onComplete, 1);
    }

    public void getCallBarringInternal(String facility, String password, Message onComplete, int serviceClass) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            ImsPhone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "getCallBarringInternal enter, facility:" + facility + ", serviceClass:" + serviceClass + ", password:" + password);
                Message resp = obtainMessage(EVENT_GET_CALL_BARRING_COMPLETE, onComplete);
                if (imsPhone == null || (imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled())) {
                    this.mCi.queryFacilityLock(facility, password, serviceClass, resp);
                } else {
                    imsPhone.getCallBarring(facility, password, resp, serviceClass);
                }
            } else if (getCsFallbackStatus() != 0 || imsPhone == null || ((imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled()) || (!imsPhone.isVolteEnabled() && (!imsPhone.isWifiCallingEnabled() || !isWFCUtSupport())))) {
                if (getCsFallbackStatus() != 0 || !isGsmUtSupport()) {
                    if (getCsFallbackStatus() == 1) {
                        setCsFallbackStatus(0);
                    }
                    if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                    } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                    } else {
                        CommandException checkError = checkUiccApplicationForCB();
                        if (checkError == null || onComplete == null) {
                            this.mCi.queryFacilityLockForApp(facility, password, serviceClass, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), onComplete);
                        } else {
                            sendErrorResponse(onComplete, checkError.getCommandError());
                        }
                    }
                } else {
                    this.mMtkSSReqDecisionMaker.queryFacilityLock(facility, password, serviceClass, onComplete);
                }
            } else if (isOpNotSupportOCB(facility)) {
                sendErrorResponse(onComplete, CommandException.Error.REQUEST_NOT_SUPPORTED);
            } else {
                MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(10, onComplete);
                ss.mParcel.writeString(facility);
                ss.mParcel.writeString(password);
                ss.mParcel.writeInt(serviceClass);
                imsPhone.getCallBarring(facility, password, obtainMessage(EVENT_IMS_UT_DONE, ss), serviceClass);
            }
        } else {
            loge("method getFacilityLock is NOT supported in CDMA!");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        }
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete, int serviceClass) {
        MtkSuppServQueueHelper ssQueueHelper = MtkSuppServManager.getSuppServQueueHelper();
        if (ssQueueHelper != null) {
            ssQueueHelper.setCallBarring(facility, lockState, password, serviceClass, onComplete, getPhoneId());
            return;
        }
        Rlog.d(LOG_TAG, "ssQueueHelper not exist, setCallBarring");
        setCallBarringInternal(facility, lockState, password, onComplete, serviceClass);
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete) {
        setCallBarring(facility, lockState, password, onComplete, 1);
    }

    public void setCallBarringInternal(String facility, boolean lockState, String password, Message onComplete, int serviceClass) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            ImsPhone imsPhone = this.mImsPhone;
            if (supportMdAutoSetupIms()) {
                Rlog.d(LOG_TAG, "setCallBarring enter, facility:" + facility + ", serviceClass:" + serviceClass + ", password:" + password + ", lockState:" + lockState);
                Message resp = obtainMessage(EVENT_SET_CALL_BARRING_COMPLETE, onComplete);
                if (imsPhone == null || (imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled())) {
                    this.mCi.setFacilityLock(facility, lockState, password, serviceClass, resp);
                } else {
                    imsPhone.setCallBarring(facility, lockState, password, resp, serviceClass);
                }
            } else if (getCsFallbackStatus() != 0 || imsPhone == null || ((imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled()) || (!imsPhone.isVolteEnabled() && (!imsPhone.isWifiCallingEnabled() || !isWFCUtSupport())))) {
                if (getCsFallbackStatus() != 0 || !isGsmUtSupport()) {
                    if (getCsFallbackStatus() == 1) {
                        setCsFallbackStatus(0);
                    }
                    if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, CommandException.Error.OPERATION_NOT_ALLOWED);
                    } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
                    } else {
                        CommandException checkError = checkUiccApplicationForCB();
                        if (checkError == null || onComplete == null) {
                            this.mCi.setFacilityLockForApp(facility, lockState, password, serviceClass, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), onComplete);
                        } else {
                            sendErrorResponse(onComplete, checkError.getCommandError());
                        }
                    }
                } else {
                    this.mMtkSSReqDecisionMaker.setFacilityLock(facility, lockState, password, serviceClass, onComplete);
                }
            } else if (isOpNotSupportOCB(facility)) {
                sendErrorResponse(onComplete, CommandException.Error.REQUEST_NOT_SUPPORTED);
            } else {
                MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(9, onComplete);
                ss.mParcel.writeString(facility);
                ss.mParcel.writeInt(lockState ? 1 : 0);
                ss.mParcel.writeString(password);
                ss.mParcel.writeInt(serviceClass);
                imsPhone.setCallBarring(facility, lockState, password, obtainMessage(EVENT_IMS_UT_DONE, ss), serviceClass);
            }
        } else {
            loge("method setFacilityLock is NOT supported in CDMA!");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        }
    }

    public CommandException checkUiccApplicationForCB() {
        if (this.mUiccApplication.get() != null) {
            return null;
        }
        Rlog.d(LOG_TAG, "checkUiccApplicationForCB: mUiccApplication.get() == null");
        if (isRadioAvailable() && isRadioOn()) {
            return new CommandException(CommandException.Error.GENERIC_FAILURE);
        }
        Rlog.d(LOG_TAG, "checkUiccApplicationForCB: radio not available");
        return new CommandException(CommandException.Error.RADIO_NOT_AVAILABLE);
    }

    public void changeCallBarringPassword(String facility, String oldPwd, String newPwd, Message onComplete) {
        if (!isPhoneTypeGsm()) {
            loge("method changeBarringPassword is NOT supported in CDMA!");
            sendErrorResponse(onComplete, CommandException.Error.GENERIC_FAILURE);
        } else if (!isDuringImsCall()) {
            this.mCi.changeBarringPassword(facility, oldPwd, newPwd, onComplete);
        } else if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    public MtkSSRequestDecisionMaker getMtkSSRequestDecisionMaker() {
        return this.mMtkSSReqDecisionMaker;
    }

    public boolean isDuringImsCall() {
        if (this.mImsPhone != null) {
            if (this.mImsPhone.getForegroundCall().getState().isAlive() || this.mImsPhone.getBackgroundCall().getState().isAlive() || this.mImsPhone.getRingingCall().getState().isAlive()) {
                Rlog.d(LOG_TAG, "During IMS call.");
                return true;
            }
        }
        return false;
    }

    public boolean isDuringVoLteCall() {
        boolean r = true;
        boolean isOnLtePdn = this.mImsPhone != null && this.mImsPhone.isVolteEnabled();
        if (!isDuringImsCall() || !isOnLtePdn) {
            r = false;
        }
        Rlog.d(LOG_TAG, "isDuringVoLteCall: " + r);
        return r;
    }

    public boolean isDuringImsEccCall() {
        boolean isInImsEccCall = this.mImsPhone != null && this.mImsPhone.isInEmergencyCall();
        Rlog.d(LOG_TAG, "isInImsEccCall: " + isInImsEccCall);
        return isInImsEccCall;
    }

    private void handleImsUtDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null) {
            Rlog.e(LOG_TAG, "EVENT_IMS_UT_DONE: Error AsyncResult null!");
            return;
        }
        MtkSuppSrvRequest ss = (MtkSuppSrvRequest) ar.userObj;
        if (ss == null) {
            Rlog.e(LOG_TAG, "EVENT_IMS_UT_DONE: Error SuppSrvRequest null!");
            return;
        }
        CommandException cmdException = null;
        cmdException = null;
        if (ar.exception != null && (ar.exception instanceof CommandException)) {
            cmdException = ar.exception;
        }
        if (cmdException != null && cmdException.getCommandError() == CommandException.Error.OPERATION_NOT_ALLOWED) {
            setCsFallbackStatus(2);
            if (isNotSupportUtToCS()) {
                Rlog.d(LOG_TAG, "UT_XCAP_403_FORBIDDEN.");
                ar.exception = new CommandException(CommandException.Error.OPERATION_NOT_ALLOWED);
                Message onComplete = ss.getResultCallback();
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                }
                ss.mParcel.recycle();
                return;
            }
            Rlog.d(LOG_TAG, "Csfallback next_reboot.");
            sendMessage(obtainMessage(2001, ss));
        } else if (cmdException == null || cmdException.getCommandError() != CommandException.Error.OEM_ERROR_3) {
            if (cmdException == null || cmdException.getCommandError() != CommandException.Error.NO_SUCH_ELEMENT) {
                if (cmdException != null && cmdException.getCommandError() == CommandException.Error.OEM_ERROR_25) {
                    if (!isEnableXcapHttpResponse409()) {
                        Rlog.d(LOG_TAG, "GSMPhone get UT_XCAP_409_CONFLICT, return GENERIC_FAILURE");
                        ar.exception = new CommandException(CommandException.Error.GENERIC_FAILURE);
                    } else {
                        Rlog.d(LOG_TAG, "GSMPhone get UT_XCAP_409_CONFLICT.");
                    }
                }
            } else if (!isOpTransferXcap404() || !(ss.getRequestCode() == 10 || ss.getRequestCode() == 9)) {
                ar.exception = new CommandException(CommandException.Error.GENERIC_FAILURE);
            } else {
                Rlog.d(LOG_TAG, "GSMPhone get UT_XCAP_404_NOT_FOUND.");
            }
            Message onComplete2 = ss.getResultCallback();
            if (onComplete2 != null) {
                AsyncResult.forMessage(onComplete2, ar.result, ar.exception);
                onComplete2.sendToTarget();
            }
            ss.mParcel.recycle();
        } else if (isNotSupportUtToCS()) {
            Rlog.d(LOG_TAG, "CommandException.Error.UT_UNKNOWN_HOST.");
            ar.exception = new CommandException(CommandException.Error.OPERATION_NOT_ALLOWED);
            Message onComplete3 = ss.getResultCallback();
            if (onComplete3 != null) {
                AsyncResult.forMessage(onComplete3, ar.result, ar.exception);
                onComplete3.sendToTarget();
            }
            ss.mParcel.recycle();
        } else {
            Rlog.d(LOG_TAG, "Csfallback once.");
            setCsFallbackStatus(1);
            sendMessage(obtainMessage(2001, ss));
        }
    }

    private void handleImsUtCsfb(Message msg) {
        MtkSuppSrvRequest ss = (MtkSuppSrvRequest) msg.obj;
        if (ss == null) {
            Rlog.e(LOG_TAG, "handleImsUtCsfb: Error MtkSuppSrvRequest null!");
            return;
        }
        boolean enable = true;
        boolean enable2 = false;
        if (isDuringVoLteCall() || isDuringImsEccCall()) {
            Message resultCallback = ss.getResultCallback();
            if (resultCallback != null) {
                AsyncResult.forMessage(resultCallback, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                resultCallback.sendToTarget();
            }
            if (getCsFallbackStatus() == 1) {
                setCsFallbackStatus(0);
            }
            ss.setResultCallback(null);
            ss.mParcel.recycle();
            return;
        }
        int requestCode = ss.getRequestCode();
        ss.mParcel.setDataPosition(0);
        if (requestCode == 3) {
            Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_SET_CLIR");
            setOutgoingCallerIdDisplayInternal(ss.mParcel.readInt(), ss.getResultCallback());
        } else if (requestCode != 4) {
            switch (requestCode) {
                case 9:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_SET_CB");
                    String facility = ss.mParcel.readString();
                    if (ss.mParcel.readInt() != 0) {
                        enable2 = true;
                    }
                    setCallBarringInternal(facility, enable2, ss.mParcel.readString(), ss.getResultCallback(), ss.mParcel.readInt());
                    break;
                case 10:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_GET_CB");
                    getCallBarringInternal(ss.mParcel.readString(), ss.mParcel.readString(), ss.getResultCallback(), ss.mParcel.readInt());
                    break;
                case 11:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_SET_CF");
                    setCallForwardingOptionInternal(ss.mParcel.readInt(), ss.mParcel.readInt(), ss.mParcel.readString(), ss.mParcel.readInt(), ss.mParcel.readInt(), ss.getResultCallback());
                    break;
                case 12:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_GET_CF");
                    getCallForwardingOptionInternal(ss.mParcel.readInt(), ss.mParcel.readInt(), ss.getResultCallback());
                    break;
                case 13:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_SET_CW");
                    if (ss.mParcel.readInt() == 0) {
                        enable = false;
                    }
                    setCallWaitingInternal(enable, ss.getResultCallback());
                    break;
                case 14:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_GET_CW");
                    getCallWaitingInternal(ss.getResultCallback());
                    break;
                case 15:
                    String dialString = ss.mParcel.readString();
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_MMI_CODE: dialString = " + dialString);
                    try {
                        dial(dialString, new PhoneInternalInterface.DialArgs.Builder().build());
                        break;
                    } catch (CallStateException ex) {
                        Rlog.e(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_MMI_CODE: CallStateException!");
                        ex.printStackTrace();
                        break;
                    }
                default:
                    Rlog.e(LOG_TAG, "handleImsUtCsfb: invalid requestCode = " + requestCode);
                    break;
            }
        } else {
            Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_GET_CLIR");
            getOutgoingCallerIdDisplayInternal(ss.getResultCallback());
        }
        ss.setResultCallback(null);
        ss.mParcel.recycle();
    }

    public void dumpPendingMmi() {
        int size = this.mPendingMMIs.size();
        if (size == 0) {
            Rlog.d(LOG_TAG, "dumpPendingMmi: none");
            return;
        }
        for (int i = 0; i < size; i++) {
            Rlog.d(LOG_TAG, "dumpPendingMmi: " + this.mPendingMMIs.get(i));
        }
    }

    /* access modifiers changed from: protected */
    public void onIncomingUSSD(int ussdMode, String ussdMessage) {
        if (!isPhoneTypeGsm()) {
            loge("onIncomingUSSD: not expected on GSM");
        }
        boolean isUssdRequest = ussdMode == 1;
        boolean isUssdError = (ussdMode == 0 || ussdMode == 1) ? false : true;
        if (ussdMode == 2) {
        }
        GsmMmiCode found = null;
        int i = 0;
        int s = this.mPendingMMIs.size();
        while (true) {
            if (i >= s) {
                break;
            } else if (((GsmMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                found = (GsmMmiCode) this.mPendingMMIs.get(i);
                break;
            } else {
                i++;
            }
        }
        if (found != null) {
            MtkGsmCdmaPhone.super.onIncomingUSSD(ussdMode, ussdMessage);
        } else if (!isUssdError && ussdMessage != null) {
            MtkGsmCdmaPhone.super.onIncomingUSSD(ussdMode, ussdMessage);
        } else if (isUssdError) {
            onNetworkInitiatedUssd(MtkGsmMmiCode.newNetworkInitiatedUssdError(ussdMessage, isUssdRequest, this, (UiccCardApplication) this.mUiccApplication.get()));
        }
    }

    public void setServiceClass(int serviceClass) {
        Rlog.d(LOG_TAG, "setServiceClass: " + serviceClass);
        SystemProperties.set(SS_SERVICE_CLASS_PROP, String.valueOf(serviceClass));
    }

    public boolean isGsmUtSupport() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isGsmUtSupport, ssConf is null, return false");
            return false;
        }
        boolean isRoaming = getServiceState().getRoaming();
        if (!SystemProperties.get("persist.vendor.ims_support").equals("1") || !SystemProperties.get("persist.vendor.volte_support").equals("1") || !ssConf.isGsmUtSupport(getOperatorNumeric()) || !isUsimCard()) {
            return false;
        }
        boolean isWfcEnable = this.mImsPhone != null && this.mImsPhone.isWifiCallingEnabled();
        boolean isWfcUtSupport = isWFCUtSupport();
        logd("in isGsmUtSupport isWfcEnable -->" + isWfcEnable + ",isWfcUtSupport-->" + isWfcUtSupport);
        if (ssConf.isNeedCheckImsWhenRoaming(getOperatorNumeric()) && isRoaming && !isIMSRegistered()) {
            logd("in isGsmUtSupport isRoaming -->" + isRoaming + ",isIMSRegistered-->" + isIMSRegistered());
            return false;
        } else if (!isWfcEnable || isWfcUtSupport) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isWFCUtSupport() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isWFCUtSupport, ssConf is null, return false");
            return false;
        } else if (!SystemProperties.get("persist.vendor.ims_support").equals("1") || !SystemProperties.get("persist.vendor.mtk_wfc_support").equals("1") || ssConf.isNotSupportWFCUt(getOperatorNumeric())) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean isUsimCard() {
        if (!isPhoneTypeGsm() || isOp(MtkOperatorUtils.OPID.OP09)) {
            String[] values = null;
            int slotId = SubscriptionManager.getSlotIndex(MtkSubscriptionManager.getSubIdUsingPhoneId(getPhoneId()));
            if (slotId >= 0) {
                String[] strArr = PROPERTY_RIL_FULL_UICC_TYPE;
                if (slotId < strArr.length) {
                    String prop = SystemProperties.get(strArr[slotId], "");
                    if (!prop.equals("") && prop.length() > 0) {
                        values = prop.split(",");
                    }
                    Rlog.d(LOG_TAG, "isUsimCard PhoneId = " + getPhoneId() + " cardType = " + Arrays.toString(values));
                    if (values == null) {
                        return false;
                    }
                    for (String s : values) {
                        if (s.equals("USIM")) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
        }
        boolean r = false;
        String iccCardType = PhoneFactory.getPhone(getPhoneId()).getIccCard().getIccCardType();
        if (iccCardType != null && iccCardType.equals("USIM")) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isUsimCard: " + r + ", " + iccCardType);
        return r;
    }

    private boolean isOp(MtkOperatorUtils.OPID id) {
        return MtkOperatorUtils.isOperator(getOperatorNumeric(), id);
    }

    public boolean isOpNotSupportOCB(String facility) {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isOpNotSupportOCB, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        boolean isOcb = false;
        if (facility.equals("AO") || facility.equals("OI") || facility.equals("OX")) {
            isOcb = true;
        }
        if (isOcb && ssConf.isNotSupportOCB(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpNotSupportOCB: " + r + ", facility=" + facility);
        return r;
    }

    public boolean isOpTbcwWithCS() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isOpTbcwWithCS, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        if (ssConf.isNotSupportXcap(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpTbcwWithCS: " + r);
        return r;
    }

    public boolean isOpTbClir() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isOpTbClir, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        if (ssConf.isTbClir(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpTbClir: " + r);
        return r;
    }

    public boolean isOpNwCW() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isOpNwCW, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        if (ssConf.isImsNwCW(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpNwCW():" + r);
        return r;
    }

    public boolean isEnableXcapHttpResponse409() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isEnableXcapHttpResponse409, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        if (ssConf.isEnableXcapHttpResponse409(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isEnableXcapHttpResponse409: " + r);
        return r;
    }

    public boolean isOpTransferXcap404() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isOpTransferXcap404, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        if (ssConf.isTransferXcap404(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpTransferXcap404: " + r);
        return r;
    }

    public boolean isOpNotSupportCallIdentity() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isOpNotSupportCallIdentity, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        if (ssConf.isNotSupportCallIdentity(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpNotSupportCallIdentity: " + r);
        return r;
    }

    public boolean isOpReregisterForCF() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isOpReregisterForCF, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        if (ssConf.isReregisterForCF(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpReregisterForCF: " + r);
        return r;
    }

    private boolean isIccCardMncMccAvailable(int phoneId) {
        IccRecords iccRecords = UiccController.getInstance().getIccRecords(phoneId, 1);
        if (iccRecords != null) {
            String mccMnc = iccRecords.getOperatorNumeric();
            Rlog.d(LOG_TAG, "isIccCardMncMccAvailable(): mccMnc is " + mccMnc);
            if (mccMnc != null) {
                return true;
            }
            return false;
        }
        Rlog.d(LOG_TAG, "isIccCardMncMccAvailable(): false");
        return false;
    }

    public boolean isNotSupportUtToCS() {
        boolean r = false;
        if (((SystemProperties.getInt("persist.vendor.mtk_ct_volte_support", 0) != 0 && isOp(MtkOperatorUtils.OPID.OP09) && isUsimCard()) || isOp(MtkOperatorUtils.OPID.OP117)) && !getServiceState().getRoaming()) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isNotSupportUtToCS: " + r);
        return r;
    }

    private boolean supportMdAutoSetupIms() {
        if (SystemProperties.get("ro.vendor.md_auto_setup_ims").equals("1")) {
            return true;
        }
        return false;
    }

    private boolean isUtError(CommandException.Error error) {
        if (error == CommandException.Error.OEM_ERROR_25 || error == CommandException.Error.OEM_ERROR_3 || error == CommandException.Error.OEM_ERROR_4 || error == CommandException.Error.OEM_ERROR_6) {
            return true;
        }
        return false;
    }

    public boolean isSupportSaveCFNumber() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isSupportSaveCFNumber, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        if (ssConf.isSupportSaveCFNumber(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isSupportSaveCFNumber: " + r);
        return r;
    }

    public void clearCFSharePreference(int cfReason) {
        String key;
        if (cfReason == 1) {
            key = "CFB_" + String.valueOf(this.mPhoneId);
        } else if (cfReason == 2) {
            key = "CFNR_" + String.valueOf(this.mPhoneId);
        } else if (cfReason != 3) {
            Rlog.e(LOG_TAG, "No need to store cfreason: " + cfReason);
            return;
        } else {
            key = "CFNRC_" + String.valueOf(this.mPhoneId);
        }
        Rlog.e(LOG_TAG, "Read to clear the key: " + key);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.remove(key);
        if (!editor.commit()) {
            Rlog.e(LOG_TAG, "failed to commit the removal of CF preference: " + key);
            return;
        }
        Rlog.e(LOG_TAG, "Commit the removal of CF preference: " + key);
    }

    public boolean applyCFSharePreference(int cfReason, String setNumber) {
        String key;
        if (cfReason == 1) {
            key = "CFB_" + String.valueOf(this.mPhoneId);
        } else if (cfReason == 2) {
            key = "CFNR_" + String.valueOf(this.mPhoneId);
        } else if (cfReason != 3) {
            Rlog.d(LOG_TAG, "No need to store cfreason: " + cfReason);
            return false;
        } else {
            key = "CFNRC_" + String.valueOf(this.mPhoneId);
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r == null) {
            Rlog.d(LOG_TAG, "No iccRecords");
            return false;
        }
        String currentImsi = r.getIMSI();
        if (currentImsi == null || currentImsi.isEmpty()) {
            Rlog.d(LOG_TAG, "currentImsi is empty");
            return false;
        } else if (setNumber == null || setNumber.isEmpty()) {
            Rlog.d(LOG_TAG, "setNumber is empty");
            return false;
        } else {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            String content = currentImsi + ";" + setNumber;
            if (content == null || content.isEmpty()) {
                Rlog.e(LOG_TAG, "imsi or content are empty or null.");
                return false;
            }
            Rlog.e(LOG_TAG, "key: " + key);
            Rlog.e(LOG_TAG, "content: " + content);
            editor.putString(key, content);
            editor.apply();
            return true;
        }
    }

    public String getCFPreviousDialNumber(int cfReason) {
        String key;
        if (cfReason == 1) {
            key = "CFB_" + String.valueOf(this.mPhoneId);
        } else if (cfReason == 2) {
            key = "CFNR_" + String.valueOf(this.mPhoneId);
        } else if (cfReason != 3) {
            Rlog.d(LOG_TAG, "No need to do the reason: " + cfReason);
            return null;
        } else {
            key = "CFNRC_" + String.valueOf(this.mPhoneId);
        }
        Rlog.d(LOG_TAG, "key: " + key);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r == null) {
            Rlog.d(LOG_TAG, "No iccRecords");
            return null;
        }
        String currentImsi = r.getIMSI();
        if (currentImsi == null || currentImsi.isEmpty()) {
            Rlog.d(LOG_TAG, "currentImsi is empty");
            return null;
        }
        Rlog.d(LOG_TAG, "currentImsi: " + currentImsi);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String info = sp.getString(key, null);
        if (info == null) {
            Rlog.d(LOG_TAG, "Sharedpref not with: " + key);
            return null;
        }
        String[] infoAry = info.split(";");
        if (infoAry == null || infoAry.length < 2) {
            Rlog.d(LOG_TAG, "infoAry.length < 2");
            return null;
        }
        String imsi = infoAry[0];
        String number = infoAry[1];
        if (imsi == null || imsi.isEmpty()) {
            Rlog.d(LOG_TAG, "Sharedpref imsi is empty.");
            return null;
        } else if (number == null || number.isEmpty()) {
            Rlog.d(LOG_TAG, "Sharedpref number is empty.");
            return null;
        } else {
            Rlog.d(LOG_TAG, "Sharedpref imsi: " + imsi);
            Rlog.d(LOG_TAG, "Sharedpref number: " + number);
            if (currentImsi.equals(imsi)) {
                Rlog.d(LOG_TAG, "Get dial number from sharepref: " + number);
                return number;
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(key);
            if (!editor.commit()) {
                Rlog.e(LOG_TAG, "failed to commit the removal of CF preference: " + key);
            }
            return null;
        }
    }

    public boolean queryCFUAgainAfterSet() {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "queryCFUAgainAfterSet, ssConf is null, return false");
            return false;
        }
        boolean r = false;
        if (ssConf.isQueryCFUAgainAfterSet(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "queryCFUAgainAfterSet: " + r);
        return r;
    }

    public boolean isSupportCFUTimeSlot() {
        boolean r = false;
        if (isOp(MtkOperatorUtils.OPID.OP01)) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isSupportCFUTimeSlot: " + r);
        return r;
    }

    public boolean isNotSupportUtToCSforCFUQuery() {
        return isNotSupportUtToCS();
    }

    public boolean isNoNeedToCSFBWhenIMSRegistered() {
        return isOp(MtkOperatorUtils.OPID.OP01) || isOp(MtkOperatorUtils.OPID.OP02);
    }

    public boolean isResetCSFBStatusAfterFlightMode() {
        return isOp(MtkOperatorUtils.OPID.OP02);
    }

    /* access modifiers changed from: protected */
    public void processIccRecordEvents(int eventCode) {
        if (eventCode != 1) {
            MtkGsmCdmaPhone.super.processIccRecordEvents(eventCode);
            return;
        }
        Rlog.d(LOG_TAG, "processIccRecordEvents");
        notifyCallForwardingIndicator();
    }

    /* access modifiers changed from: package-private */
    public void sendErrorResponse(Message onComplete, CommandException.Error error) {
        Rlog.d(LOG_TAG, "sendErrorResponse" + error);
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, new CommandException(error));
            onComplete.sendToTarget();
        }
    }

    private boolean isAllowXcapIfDataRoaming(String mccMnc) {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isAllowXcapIfDataRoaming, ssConf is null, return false");
            return false;
        } else if (!getServiceState().getDataRoaming()) {
            Rlog.d(LOG_TAG, "isAllowXcapIfDataRoaming: true (not roaming state)");
            return true;
        } else if (!ssConf.isNeedCheckDataRoaming(mccMnc)) {
            Rlog.d(LOG_TAG, "isAllowXcapIfDataRoaming: true (ignore roaming state)");
            return true;
        } else {
            Rlog.d(LOG_TAG, "isAllowXcapIfDataRoaming: false (roaming state, block SS)");
            return false;
        }
    }

    private boolean isAllowXcapIfDataEnabled(String mccMnc) {
        MtkSuppServConf ssConf = MtkSuppServManager.getSuppServConf(getPhoneId());
        if (ssConf == null) {
            Rlog.d(LOG_TAG, "isAllowXcapIfDataEnabled, ssConf is null, return false");
            return false;
        } else if (!ssConf.isNeedCheckDataEnabled(mccMnc)) {
            return true;
        } else {
            if (this.mDataEnabledSettings.isDataEnabled()) {
                Rlog.d(LOG_TAG, "isAllowXcapIfDataEnabled: true");
                return true;
            }
            Rlog.d(LOG_TAG, "isAllowXcapIfDataEnabled: false");
            return false;
        }
    }

    public int getCsFallbackStatus() {
        if (!isAllowXcapIfDataEnabled(getOperatorNumeric())) {
            this.mCSFallbackMode = 1;
        }
        if (!isAllowXcapIfDataRoaming(getOperatorNumeric())) {
            this.mCSFallbackMode = 1;
        }
        Rlog.d(LOG_TAG, "getCsFallbackStatus is " + this.mCSFallbackMode);
        return this.mCSFallbackMode;
    }

    public void setCsFallbackStatus(int newStatus) {
        Rlog.d(LOG_TAG, "setCsFallbackStatus to " + newStatus);
        this.mCSFallbackMode = newStatus;
    }

    private int getMainCapabilityPhoneId() {
        int phoneId = SystemProperties.getInt("persist.vendor.radio.simswitch", 1) - 1;
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            return -1;
        }
        return phoneId;
    }

    public Connection dial(List<String> numbers, int videoState) throws CallStateException {
        if (!(this.mImsPhone instanceof MtkImsPhone)) {
            Rlog.d(LOG_TAG, "mImsPhone must be MtkImsPhone to make enhanced conference dial");
            return null;
        }
        boolean imsUseEnabled = false;
        MtkLocalPhoneNumberUtils.setIsEmergencyNumber(false);
        MtkImsPhone imsPhone = this.mImsPhone;
        if (isImsUseEnabled() && imsPhone != null && ((imsPhone.isVolteEnabled() || imsPhone.isWifiCallingEnabled() || (imsPhone.isVideoEnabled() && VideoProfile.isVideo(videoState))) && imsPhone.getServiceState().getState() == 0)) {
            imsUseEnabled = true;
        }
        if (!imsUseEnabled) {
            Rlog.w(LOG_TAG, "IMS is disabled and can not dial conference call directly.");
            return null;
        }
        if (imsPhone != null) {
            Rlog.w(LOG_TAG, "service state = " + imsPhone.getServiceState().getState());
        }
        if (imsUseEnabled && imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            try {
                Rlog.d(LOG_TAG, "Trying IMS PS conference call");
                return imsPhone.dial(numbers, videoState);
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "IMS PS conference call exception " + e);
                if (!"cs_fallback".equals(e.getMessage())) {
                    CallStateException ce = new CallStateException(e.getMessage());
                    ce.setStackTrace(e.getStackTrace());
                    throw ce;
                }
            }
        }
        return null;
    }

    public boolean isIMSRegistered() {
        MtkSuppServHelper ssHelper = MtkSuppServManager.getSuppServHelper(getPhoneId());
        if (ssHelper != null) {
            return ssHelper.getIMSRegistered();
        }
        return false;
    }

    public int getCdmaSubscriptionActStatus() {
        if (this.mCdmaSSM != null) {
            return this.mCdmaSSM.getActStatus();
        }
        return 0;
    }

    public void setRoamingEnable(int[] config, Message response) {
        Rlog.d(LOG_TAG, "set roaming enable");
        config[0] = this.mPhoneId;
        this.mMtkCi.setRoamingEnable(config, response);
    }

    public void getRoamingEnable(Message response) {
        Rlog.d(LOG_TAG, "get roaming enable");
        this.mMtkCi.getRoamingEnable(this.mPhoneId, response);
    }

    public boolean isGsmSsPrefer() {
        if ((SystemProperties.getInt("persist.vendor.mtk_ct_volte_support", 0) == 0 || !isOp(MtkOperatorUtils.OPID.OP09)) && !isOp(MtkOperatorUtils.OPID.OP117)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onCheckForNetworkSelectionModeAutomatic(Message fromRil) {
        AsyncResult ar = (AsyncResult) fromRil.obj;
        Message response = (Message) ar.userObj;
        boolean doAutomatic = true;
        doAutomatic = true;
        doAutomatic = true;
        doAutomatic = true;
        if (ar.exception == null && ar.result != null) {
            try {
                if (((int[]) ar.result)[0] == 0) {
                    doAutomatic = false;
                }
            } catch (Exception e) {
            }
        }
        Phone.NetworkSelectMessage nsm = new Phone.NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = "";
        nsm.operatorAlphaLong = "";
        nsm.operatorAlphaShort = "";
        if (doAutomatic) {
            this.mCi.setNetworkSelectionModeAutomatic(obtainMessage(17, nsm));
        } else {
            Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomatic - already auto, ignoring");
            if (nsm.message != null) {
                nsm.message.arg1 = 1;
            }
            ar.userObj = nsm;
            handleSetSelectNetwork(ar);
        }
        updateSavedNetworkOperator(nsm);
    }

    /* access modifiers changed from: protected */
    public String getOperatorNumeric() {
        if (!isPhoneTypeGsm()) {
            this.mCdmaSubscriptionSource = this.mCdmaSSM.getCdmaSubscriptionSource();
        }
        return MtkGsmCdmaPhone.super.getOperatorNumeric();
    }

    public Message getCFCallbackMessage() {
        return obtainMessage(13);
    }

    public Message getCFTimeSlotCallbackMessage() {
        return obtainMessage(109);
    }

    public boolean isImsUseEnabled() {
        ImsManager imsManager = ImsManager.getInstance(this.mContext, this.mPhoneId);
        boolean isEnhanced4gLteModeSettingEnabledByUser = imsManager.isEnhanced4gLteModeSettingEnabledByUser();
        boolean isWfcEnabledByUser = true;
        boolean isNonTtyOrTtyOnVolteEnabled = true;
        if (!isEnhanced4gLteModeSettingEnabledByUser) {
            isWfcEnabledByUser = imsManager.isWfcEnabledByUser();
            isNonTtyOrTtyOnVolteEnabled = imsManager.isNonTtyOrTtyOnVolteEnabled();
        }
        boolean imsUseEnabled = isEnhanced4gLteModeSettingEnabledByUser || (isWfcEnabledByUser && isNonTtyOrTtyOnVolteEnabled);
        Rlog.d(LOG_TAG, "isImsUseEnabled() VolteEnableByUser: " + isEnhanced4gLteModeSettingEnabledByUser + ", WfcEnableByUser: " + isWfcEnabledByUser + ", isNonTtyOrTtyOnVolteEnabled: " + isNonTtyOrTtyOnVolteEnabled);
        return imsUseEnabled;
    }

    public void cleanCallForwardingIndicatorFromSharedPref() {
        setCallForwardingIndicatorInSharedPref(false);
    }

    public boolean shouldProcessSelfActivation() {
        int selfActivateState = getSelfActivationInstance().getSelfActivateState();
        Rlog.d(LOG_TAG, "shouldProcessSelfActivation() state: " + selfActivateState);
        return selfActivateState == 2;
    }

    public boolean useImsForPCOChanged() {
        int pcoState = getSelfActivationInstance().getPCO520State();
        Rlog.d(LOG_TAG, "pcoState() state: " + pcoState);
        return pcoState == 1;
    }

    /* access modifiers changed from: protected */
    public boolean needResetPhbIntMgr() {
        return false;
    }

    public String getFullIccSerialNumber() {
        String iccId = MtkGsmCdmaPhone.super.getFullIccSerialNumber();
        if (iccId != null) {
            return iccId;
        }
        if (!isPhoneTypeGsm() && this.mUiccController != null) {
            IccRecords r = this.mUiccController.getIccRecords(this.mPhoneId, 1);
            iccId = r != null ? r.getFullIccId() : null;
            if (iccId != null) {
                return iccId;
            }
        }
        long identity = Binder.clearCallingIdentity();
        try {
            SubscriptionInfo subInfo = SubscriptionManager.from(getContext()).getActiveSubscriptionInfo(getSubId());
            if (subInfo != null) {
                iccId = subInfo.getIccId();
            }
            return iccId;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: protected */
    public void initRatSpecific(int precisePhoneType) {
        MtkGsmCdmaPhone.super.initRatSpecific(precisePhoneType);
        if (isPhoneTypeGsm()) {
            this.mIsPhoneInEcmState = getInEcmMode();
            if (this.mIsPhoneInEcmState) {
                this.mCi.exitEmergencyCallbackMode(obtainMessage(26));
            }
        }
    }

    public void exitEmergencyCallbackMode() {
        Rlog.d(LOG_TAG, "exitEmergencyCallbackMode: mImsPhone=" + this.mImsPhone + " isPhoneTypeGsm=" + isPhoneTypeGsm());
        Rlog.d(LOG_TAG, "exitEmergencyCallbackMode()");
        tryTurnOnWifiForE911Finished();
        if (!isPhoneTypeGsm()) {
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            this.mCi.exitEmergencyCallbackMode((Message) null);
        } else if (this.mImsPhone != null) {
            this.mImsPhone.exitEmergencyCallbackMode();
        }
    }

    public void sendExitEmergencyCallbackModeMessage() {
        Rlog.d(LOG_TAG, "sendExitEmergencyCallbackModeMessage()");
        Message message = obtainMessage(26);
        AsyncResult.forMessage(message);
        sendMessage(message);
    }

    private void tryTurnOffWifiForE911(boolean isEcc) {
        if (this.mContext != null) {
            boolean turnOffWifiForEcc = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId()).getBoolean("mtk_carrier_turn_off_wifi_before_e911");
            boolean isWfcEnabled = true;
            if (SystemProperties.getInt(PROPERTY_WFC_ENABLE, 0) != 1) {
                isWfcEnabled = false;
            }
            Rlog.d(LOG_TAG, "tryTurnOffWifiForEcc() carrierConfig: " + turnOffWifiForEcc + " isECC: " + isEcc + " isWfcEnable: " + isWfcEnabled);
            if (isEcc && turnOffWifiForEcc && !isWfcEnabled) {
                WifiManager wifiMngr = (WifiManager) this.mContext.getSystemService("wifi");
                this.mWifiIsEnabledBeforeE911 = wifiMngr.isWifiEnabled();
                Rlog.d(LOG_TAG, "tryTurnOffWifiForEcc() wifiEnabled: " + this.mWifiIsEnabledBeforeE911);
                if (this.mWifiIsEnabledBeforeE911) {
                    wifiMngr.setWifiEnabled(false);
                }
            }
        }
    }

    private void tryTurnOnWifiForE911Finished() {
        if (this.mContext != null && ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId()).getBoolean("mtk_carrier_turn_off_wifi_before_e911")) {
            WifiManager wifiMngr = (WifiManager) this.mContext.getSystemService("wifi");
            Rlog.d(LOG_TAG, "tryTurnOnWifiForEcbmFinished() wifiEnabled: " + this.mWifiIsEnabledBeforeE911);
            if (this.mWifiIsEnabledBeforeE911) {
                wifiMngr.setWifiEnabled(true);
            }
        }
    }

    public String getDeviceSvn() {
        if (isPhoneTypeGsm() || isPhoneTypeCdmaLte() || isPhoneTypeCdma()) {
            return this.mImeiSv;
        }
        loge("getDeviceSvn(): return 0");
        return "0";
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        this.mMtkCi.invokeOemRilRequestRaw(data, response);
    }

    /* access modifiers changed from: protected */
    public void phoneObjectUpdater(int newVoiceRadioTech) {
        this.mNewVoiceTech = newVoiceRadioTech;
        MtkGsmCdmaPhone.super.phoneObjectUpdater(newVoiceRadioTech);
    }

    public void setDisable2G(boolean mode, Message result) {
        Rlog.d(LOG_TAG, "setDisable2G " + mode);
        this.mMtkCi.setDisable2G(mode, result);
    }

    public void getDisable2G(Message result) {
        Rlog.d(LOG_TAG, "getDisable2G");
        this.mMtkCi.getDisable2G(result);
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
        IccRecords r;
        MtkGsmCdmaPhone.super.setVoiceCallForwardingFlag(line, enable, number);
        if (getPhoneType() == 2 && isGsmSsPrefer()) {
            if (!(this.mUiccController == null || (r = this.mUiccController.getIccRecords(this.mPhoneId, 1)) == null)) {
                r.setVoiceCallForwardingFlag(line, enable, number);
            }
            notifyCallForwardingIndicator();
        }
    }

    public boolean getCallForwardingIndicator() {
        if (getPhoneType() != 2 || !isGsmSsPrefer()) {
            return MtkGsmCdmaPhone.super.getCallForwardingIndicator();
        }
        IccRecords r = null;
        if (this.mUiccController != null) {
            r = this.mUiccController.getIccRecords(this.mPhoneId, 1);
        }
        int callForwardingIndicator = -1;
        if (r != null) {
            callForwardingIndicator = r.getVoiceCallForwardingFlag();
            Rlog.v(LOG_TAG, "getCallForwardingIndicator: from icc record = " + callForwardingIndicator);
        }
        if (callForwardingIndicator == -1) {
            callForwardingIndicator = getCallForwardingIndicatorFromSharedPref();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getCallForwardingIndicator: iccForwardingFlag=");
        sb.append(r != null ? Integer.valueOf(r.getVoiceCallForwardingFlag()) : "null");
        sb.append(", sharedPrefFlag=");
        sb.append(getCallForwardingIndicatorFromSharedPref());
        Rlog.v(LOG_TAG, sb.toString());
        if (callForwardingIndicator == 1) {
            return true;
        }
        return false;
    }

    public boolean isCdmaLessDevice() {
        boolean isCdmaLess = false;
        if ("3".equals(SystemProperties.get(PROP_VZW_DEVICE_TYPE, "0")) || "4".equals(SystemProperties.get(PROP_VZW_DEVICE_TYPE, "0"))) {
            isCdmaLess = true;
        }
        Rlog.d(LOG_TAG, "isCdmaLess: " + isCdmaLess);
        return isCdmaLess;
    }

    public void invokeOemRilRequestStrings(String[] strings, Message response) {
        this.mMtkCi.invokeOemRilRequestStrings(strings, response);
    }

    public void setSuppServProperty(String name, String value) {
        Rlog.d(LOG_TAG, "setSuppServProperty, name = " + name + ", value = " + value);
        this.mCallbackLatch = new CountDownLatch(1);
        this.mMtkCi.setSuppServProperty(name, value, obtainMessage(1004));
        if (!isCallbackDone()) {
            Rlog.e(LOG_TAG, "waitForCallback: callback is not done!");
        }
    }

    private boolean isCallbackDone() {
        boolean isDone;
        try {
            isDone = this.mCallbackLatch.await(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            isDone = false;
        }
        Rlog.d(LOG_TAG, "waitForCallback: isDone=" + isDone);
        return isDone;
    }

    public void setIsInEcm(boolean isInEcm) {
        MtkGsmCdmaPhone.super.setIsInEcm(isInEcm);
        setSystemProperty("vendor.ril.cdma.inecmmode_by_slot", String.valueOf(isInEcm));
    }

    public PhoneConstants.State getState() {
        PhoneConstants.State imsState;
        if (this.mImsPhone != null && (imsState = this.mImsPhone.getState()) != PhoneConstants.State.IDLE) {
            return imsState;
        }
        if (((GsmCdmaCallTracker) this.mCT).mState != PhoneConstants.State.IDLE || this.mCT.getHandoverConnectionSize() <= 0) {
            return this.mCT.mState;
        }
        return PhoneConstants.State.OFFHOOK;
    }

    public String getLine1PhoneNumber() {
        String str;
        if (!isPhoneTypeGsm()) {
            return this.mSST.getMdnNumber();
        }
        String optr = SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR);
        if (optr == null || !"OP20".equals(optr)) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getMsisdnNumber();
            }
            return null;
        }
        MtkIccCardConstants.CardType mCdmaCardType = MtkTelephonyManagerEx.getDefault().getCdmaCardType(getPhoneId());
        boolean isCdma4g = false;
        if (mCdmaCardType != null) {
            isCdma4g = mCdmaCardType.is4GCard();
        }
        if (isCdma4g) {
            RuimRecords rr = UiccController.getInstance().getIccRecords(getPhoneId(), 2);
            IccRecords r2 = (IccRecords) this.mIccRecords.get();
            StringBuilder sb = new StringBuilder();
            sb.append("getLine1PhoneNumber, number = ");
            if (rr == null || rr.getMdn() == null || rr.getMdn().isEmpty()) {
                str = r2 != null ? r2.getMsisdnNumber() : null;
            } else {
                str = rr.getMdn();
            }
            sb.append(str);
            sb.append(", slot = ");
            sb.append(getPhoneId());
            logd(sb.toString());
            if (rr != null && rr.getMdn() != null && !rr.getMdn().isEmpty()) {
                return rr.getMdn();
            }
            if (r2 != null) {
                return r2.getMsisdnNumber();
            }
            return null;
        }
        IccRecords r3 = (IccRecords) this.mIccRecords.get();
        if (r3 != null) {
            return r3.getMsisdnNumber();
        }
        return null;
    }

    public void notifyMtkServiceStateChanged(MtkServiceState ss) {
        this.mNotifier.notifyMtkServiceState(this, ss);
        MtkDcTracker dct = getDcTracker(1);
        if (dct != null) {
            dct.notifyMtkServiceStateChanged(ss);
        }
    }

    public void notifyMtkSignalStrength(SignalStrength ss) {
        this.mNotifier.notifyMtkSignalStrength(this, ss);
    }

    public String getSubscriberId() {
        String subscriberId = null;
        MtkDcTracker dct = getDcTracker(1);
        if (dct != null) {
            subscriberId = dct.getImsi();
        }
        if (TextUtils.isEmpty(subscriberId)) {
            return MtkGsmCdmaPhone.super.getSubscriberId();
        }
        return subscriberId;
    }

    public void iwlanSetRegisterCellularQualityReport(int qualityRegister, int type, int[] values, Message result) {
        this.mMtkCi.iwlanSetRegisterCellularQualityReport(qualityRegister, type, values, result);
    }

    public void getSuggestedPlmnList(int rat, int num, int timer, Message onCompleted) {
        this.mMtkCi.getSuggestedPlmnList(rat, num, timer, onCompleted);
    }

    public AsyncResult getCachedCrss() {
        Rlog.e(LOG_TAG, "getCachedCrss()");
        return this.mCachedCrssn;
    }

    public void resetCachedCrss() {
        Rlog.e(LOG_TAG, "ResetCachedCrss()");
        this.mCachedCrssn = null;
    }
}
