package com.android.internal.telephony.uicc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hardware.radio.V1_0.LastCallFailCause;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.gsm.NetworkInfoWithAcT;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.regionlock.RegionLockConstant;
import com.android.internal.telephony.test.SimulatedCommands;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import org.codeaurora.ims.utils.QtiImsExtUtils;

public class SIMRecords extends IccRecords {
    /* renamed from: -com-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues */
    private static final /* synthetic */ int[] f10x102e4fe = null;
    static final int CFF_LINE1_MASK = 15;
    static final int CFF_LINE1_RESET = 240;
    static final int CFF_UNCONDITIONAL_ACTIVE = 10;
    static final int CFF_UNCONDITIONAL_DEACTIVE = 5;
    private static final int CFIS_ADN_CAPABILITY_ID_OFFSET = 14;
    private static final int CFIS_ADN_EXTENSION_ID_OFFSET = 15;
    private static final int CFIS_BCD_NUMBER_LENGTH_OFFSET = 2;
    private static final int CFIS_TON_NPI_OFFSET = 3;
    private static final String CHANGE_TO_REGION = "change_to_region";
    private static final int CPHS_SST_MBN_ENABLED = 48;
    private static final int CPHS_SST_MBN_MASK = 48;
    private static final boolean CRASH_RIL = false;
    private static final int EVENT_APP_LOCKED = 258;
    private static final int EVENT_CARRIER_CONFIG_CHANGED = 257;
    private static final int EVENT_GET_AD_DONE = 9;
    private static final int EVENT_GET_ALL_OPL_DONE = 501;
    private static final int EVENT_GET_ALL_PNN_DONE = 500;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    private static final int EVENT_GET_CFF_DONE = 24;
    private static final int EVENT_GET_CFIS_DONE = 32;
    private static final int EVENT_GET_CPHSONS_DONE = 502;
    private static final int EVENT_GET_CPHS_MAILBOX_DONE = 11;
    private static final int EVENT_GET_CSP_CPHS_DONE = 33;
    private static final int EVENT_GET_EHPLMN_DONE = 40;
    private static final int EVENT_GET_FPLMN_DONE = 41;
    private static final int EVENT_GET_GID1_DONE = 34;
    private static final int EVENT_GET_GID2_DONE = 36;
    private static final int EVENT_GET_HPLMN_W_ACT_DONE = 39;
    private static final int EVENT_GET_ICCID_DONE = 4;
    private static final int EVENT_GET_IMSI_DONE = 3;
    private static final int EVENT_GET_INFO_CPHS_DONE = 26;
    private static final int EVENT_GET_MBDN_DONE = 6;
    private static final int EVENT_GET_MBI_DONE = 5;
    private static final int EVENT_GET_MSISDN_DONE = 10;
    private static final int EVENT_GET_MWIS_DONE = 7;
    private static final int EVENT_GET_OPLMN_W_ACT_DONE = 38;
    private static final int EVENT_GET_PLMN_LIST_DONE = 102;
    private static final int EVENT_GET_PLMN_W_ACT_DONE = 37;
    private static final int EVENT_GET_PNN_DONE = 15;
    private static final int EVENT_GET_POL_DONE = 99;
    private static final int EVENT_GET_SHORT_CPHSONS_DONE = 503;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SPDI_DONE = 13;
    private static final int EVENT_GET_SPN_DONE = 12;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE = 8;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SET_CPHS_MAILBOX_DONE = 25;
    private static final int EVENT_SET_MBDN_DONE = 20;
    private static final int EVENT_SET_MSISDN_DONE = 30;
    private static final int EVENT_SET_POL_DONE = 88;
    private static final int EVENT_SMS_ON_SIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    private static final int EVENT_UPDATE_PLMN_LIST_DONE = 101;
    protected static final String LOG_TAG = "SIMRecords";
    private static final String[] MCCMNC_CODES_HAVING_3DIGITS_MNC = new String[]{"302370", "302720", SimulatedCommands.FAKE_MCC_MNC, "405025", "405026", "405027", "405028", "405029", "405030", "405031", "405032", "405033", "405034", "405035", "405036", "405037", "405038", "405039", "405040", "405041", "405042", "405043", "405044", "405045", "405046", "405047", "405750", "405751", "405752", "405753", "405754", "405755", "405756", "405799", "405800", "405801", "405802", "405803", "405804", "405805", "405806", "405807", "405808", "405809", "405810", "405811", "405812", "405813", "405814", "405815", "405816", "405817", "405818", "405819", "405820", "405821", "405822", "405823", "405824", "405825", "405826", "405827", "405828", "405829", "405830", "405831", "405832", "405833", "405834", "405835", "405836", "405837", "405838", "405839", "405840", "405841", "405842", "405843", "405844", "405845", "405846", "405847", "405848", "405849", "405850", "405851", "405852", "405853", QtiImsExtUtils.CARRIER_ONE_DEFAULT_MCC_MNC, "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874", "405875", "405876", "405877", "405878", "405879", "405880", "405881", "405882", "405883", "405884", "405885", "405886", "405908", "405909", "405910", "405911", "405912", "405913", "405914", "405915", "405916", "405917", "405918", "405919", "405920", "405921", "405922", "405923", "405924", "405925", "405926", "405927", "405928", "405929", "405930", "405931", "405932", "502142", "502143", "502145", "502146", "502147", "502148"};
    private static final int POL_TECH_CDMA2000 = 2;
    private static final int POL_TECH_E_UTRAN = 4;
    private static final int POL_TECH_GSM = 1;
    private static final int POL_TECH_UNKNOW = 0;
    private static final int POL_TECH_UTRAN = 3;
    private static final int SIM_RECORD_EVENT_BASE = 0;
    private static final int SIM_STATUE_LOCK = 1;
    private static final int SIM_STATUE_LOCK_PROCESSED = 2;
    private static final int SIM_STATUE_READY = 3;
    private static final int SIM_STATUE_UNKNOW = 0;
    private static final int SYSTEM_EVENT_BASE = 256;
    static final int TAG_FULL_NETWORK_NAME = 67;
    static final int TAG_SHORT_NETWORK_NAME = 69;
    static final int TAG_SPDI = 163;
    static final int TAG_SPDI_PLMN_LIST = 128;
    private static final boolean VDBG = false;
    String cphsOnsl;
    String cphsOnss;
    private boolean isNeedSetSpnLater;
    private AppType mApptype;
    private int mCallForwardingStatus;
    private byte[] mCphsInfo;
    boolean mCspPlmnEnabled;
    byte[] mEfCPHS_MWI;
    byte[] mEfCff;
    byte[] mEfCfis;
    byte[] mEfLi;
    byte[] mEfMWIS;
    byte[] mEfPl;
    byte[] mEfSST;
    byte[] mEfpol;
    private boolean mIsSimLoaded;
    String[] mOperatorAlphaName;
    private ArrayList<OplRecord> mOperatorList;
    String[] mOperatorNumeric;
    private Phone mPhone;
    int[] mPlmn;
    public int mPlmnNumber;
    String mPnnHomeName;
    private ArrayList<OperatorName> mPnnNetworkNames;
    private String mPrlVersion;
    byte[] mReadBuffer;
    private final BroadcastReceiver mReceiver;
    protected ServiceStateTracker mSST;
    private volatile int mSimState;
    private String mSpNameInEfSpn;
    ArrayList<String> mSpdiNetworks;
    int mSpnDisplayCondition;
    SpnOverride mSpnOverride;
    private GetSpnFsmState mSpnState;
    int[] mTech;
    public int mUsedPlmnNumber;
    UsimServiceTable mUsimServiceTable;
    VoiceMailConstants mVmConfig;
    byte[] mWriteBuffer;
    protected Message onCompleteMsg;

    private class EfCsimEprlLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimEprlLoaded(SIMRecords this$0, EfCsimEprlLoaded -this1) {
            this();
        }

        private EfCsimEprlLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_EPRL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.onGetCSimEprlDone(ar);
        }
    }

    private class EfPlLoaded implements IccRecordLoaded {
        /* synthetic */ EfPlLoaded(SIMRecords this$0, EfPlLoaded -this1) {
            this();
        }

        private EfPlLoaded() {
        }

        public String getEfName() {
            return "EF_PL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfPl = (byte[]) ar.result;
            SIMRecords.this.log("EF_PL=" + IccUtils.bytesToHexString(SIMRecords.this.mEfPl));
        }
    }

    private class EfUsimLiLoaded implements IccRecordLoaded {
        /* synthetic */ EfUsimLiLoaded(SIMRecords this$0, EfUsimLiLoaded -this1) {
            this();
        }

        private EfUsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfLi = (byte[]) ar.result;
            SIMRecords.this.log("EF_LI=" + IccUtils.bytesToHexString(SIMRecords.this.mEfLi));
        }
    }

    private enum GetSpnFsmState {
        IDLE,
        INIT,
        READ_SPN_3GPP,
        READ_SPN_CPHS,
        READ_SPN_SHORT_CPHS
    }

    public static class OperatorName {
        public String sFullName;
        public String sShortName;
    }

    public static class OplRecord {
        public int nMaxLAC;
        public int nMinLAC;
        public int nPnnIndex;
        public String sPlmn;
    }

    /* renamed from: -getcom-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues */
    private static /* synthetic */ int[] m11xf89fffa2() {
        if (f10x102e4fe != null) {
            return f10x102e4fe;
        }
        int[] iArr = new int[GetSpnFsmState.values().length];
        try {
            iArr[GetSpnFsmState.IDLE.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GetSpnFsmState.INIT.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_3GPP.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_CPHS.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_SHORT_CPHS.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        f10x102e4fe = iArr;
        return iArr;
    }

    public String toString() {
        return "SimRecords: " + super.toString() + " mVmConfig" + this.mVmConfig + " mSpnOverride=" + this.mSpnOverride + " callForwardingEnabled=" + this.mCallForwardingStatus + " spnState=" + this.mSpnState + " mCphsInfo=" + this.mCphsInfo + " mCspPlmnEnabled=" + this.mCspPlmnEnabled + " efMWIS=" + this.mEfMWIS + " efCPHS_MWI=" + this.mEfCPHS_MWI + " mEfCff=" + this.mEfCff + " mEfCfis=" + this.mEfCfis + " getOperatorNumeric=" + getOperatorNumeric();
    }

    public SIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mEfSST = null;
        this.mSpNameInEfSpn = null;
        this.mOperatorList = null;
        this.mPnnNetworkNames = null;
        this.mIsSimLoaded = false;
        this.mCphsInfo = null;
        this.mCspPlmnEnabled = true;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mEfCff = null;
        this.mEfCfis = null;
        this.mEfLi = null;
        this.mEfPl = null;
        this.mSpdiNetworks = null;
        this.mPnnHomeName = null;
        this.isNeedSetSpnLater = false;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    SIMRecords.this.sendMessage(SIMRecords.this.obtainMessage(257));
                }
            }
        };
        this.mPrlVersion = SpnOverride.MVNO_TYPE_NONE;
        this.mAdnCache = new AdnRecordCache(this.mFh);
        this.mVmConfig = new VoiceMailConstants();
        this.mSpnOverride = SpnOverride.getInstance();
        this.mRecordsRequested = false;
        this.cphsOnsl = null;
        this.cphsOnss = null;
        this.mPhone = PhoneFactory.getPhone(app.getPhoneId());
        this.mIsTestCard = false;
        this.mRecordsToLoad = 0;
        this.mCi.setOnSmsOnSim(this, 21, null);
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, 258, null);
        log("SIMRecords X ctor this=" + this);
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        c.registerReceiver(this.mReceiver, intentfilter);
        this.mApptype = app.getType();
        log("mApptype = " + this.mApptype);
    }

    public void dispose() {
        log("Disposing SIMRecords this=" + this);
        this.mCi.unSetOnSmsOnSim(this);
        this.mParentApp.unregisterForReady(this);
        this.mParentApp.unregisterForLocked(this);
        resetRecords();
        super.dispose();
    }

    protected void finalize() {
        log("finalized");
    }

    protected void resetRecords() {
        this.mImsi = null;
        this.mMsisdn = null;
        this.mVoiceMailNum = null;
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mSpnDisplayCondition = -1;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mSpdiNetworks = null;
        this.mPnnHomeName = null;
        this.mGid1 = null;
        this.mGid2 = null;
        this.mPlmnActRecords = null;
        this.mOplmnActRecords = null;
        this.mHplmnActRecords = null;
        this.mFplmns = null;
        this.mEhplmns = null;
        this.mAdnCache.reset();
        log("SIMRecords: onRadioOffOrNotAvailable set 'gsm.sim.operator.numeric' to operator=null");
        log("update icc_operator_numeric=" + null);
        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), SpnOverride.MVNO_TYPE_NONE);
        setSimState(0);
        this.isNeedSetSpnLater = false;
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), SpnOverride.MVNO_TYPE_NONE);
        this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), SpnOverride.MVNO_TYPE_NONE);
        this.mRecordsRequested = false;
    }

    public String getMsisdnNumber() {
        return this.mMsisdn;
    }

    public UsimServiceTable getUsimServiceTable() {
        return this.mUsimServiceTable;
    }

    private int getExtFromEf(int ef) {
        switch (ef) {
            case IccConstants.EF_MSISDN /*28480*/:
                if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                    return IccConstants.EF_EXT5;
                }
                return IccConstants.EF_EXT1;
            default:
                return IccConstants.EF_EXT1;
        }
    }

    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        this.mNewMsisdn = number;
        this.mNewMsisdnTag = alphaTag;
        log("Set MSISDN: " + this.mNewMsisdnTag + " " + Rlog.pii(LOG_TAG, this.mNewMsisdn));
        new AdnRecordLoader(this.mFh).updateEF(new AdnRecord(this.mNewMsisdnTag, this.mNewMsisdn), IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, null, obtainMessage(30, onComplete));
    }

    public String getMsisdnAlphaTag() {
        return this.mMsisdnTag;
    }

    public String getVoiceMailNumber() {
        return this.mVoiceMailNum;
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        if (this.mIsVoiceMailFixed) {
            AsyncResult.forMessage(onComplete).exception = new IccVmFixedException("Voicemail number is fixed by operator");
            onComplete.sendToTarget();
            return;
        }
        this.mNewVoiceMailNum = voiceNumber;
        this.mNewVoiceMailTag = alphaTag;
        AdnRecord adn = new AdnRecord(this.mNewVoiceMailTag, this.mNewVoiceMailNum);
        if (this.mMailboxIndex != 0 && this.mMailboxIndex != 255) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, null, obtainMessage(20, onComplete));
        } else if (isCphsMailboxEnabled()) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onComplete));
        } else {
            AsyncResult.forMessage(onComplete).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
            onComplete.sendToTarget();
        }
    }

    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        int i = 0;
        if (line == 1) {
            try {
                if (this.mEfMWIS != null) {
                    byte[] bArr = this.mEfMWIS;
                    int i2 = this.mEfMWIS[0] & LastCallFailCause.RADIO_LINK_FAILURE;
                    if (countWaiting != 0) {
                        i = 1;
                    }
                    bArr[0] = (byte) (i | i2);
                    if (countWaiting < 0) {
                        this.mEfMWIS[1] = (byte) 0;
                    } else {
                        this.mEfMWIS[1] = (byte) countWaiting;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_MWIS, 1, this.mEfMWIS, null, obtainMessage(14, IccConstants.EF_MWIS, 0));
                }
                if (this.mEfCPHS_MWI != null) {
                    this.mEfCPHS_MWI[0] = (byte) ((countWaiting == 0 ? 5 : 10) | (this.mEfCPHS_MWI[0] & 240));
                    this.mFh.updateEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, this.mEfCPHS_MWI, obtainMessage(14, Integer.valueOf(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving voice mail state to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    private boolean validEfCfis(byte[] data) {
        if (data == null) {
            return false;
        }
        if (data[0] < (byte) 1 || data[0] > (byte) 4) {
            logw("MSP byte: " + data[0] + " is not between 1 and 4", null);
        }
        return true;
    }

    public int getVoiceMessageCount() {
        int countVoiceMessages = -2;
        if (this.mEfMWIS != null) {
            countVoiceMessages = this.mEfMWIS[1] & 255;
            if (((this.mEfMWIS[0] & 1) != 0) && (countVoiceMessages == 0 || countVoiceMessages == 255)) {
                countVoiceMessages = -1;
            }
            log(" VoiceMessageCount from SIM MWIS = " + countVoiceMessages);
        } else if (this.mEfCPHS_MWI != null) {
            int indicator = this.mEfCPHS_MWI[0] & 15;
            if (indicator == 10) {
                countVoiceMessages = -1;
            } else if (indicator == 5) {
                countVoiceMessages = 0;
            }
            log(" VoiceMessageCount from SIM CPHS = " + countVoiceMessages);
        }
        return countVoiceMessages;
    }

    public int getVoiceCallForwardingFlag() {
        return this.mCallForwardingStatus;
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String dialNumber) {
        int i = 0;
        if (line == 1) {
            if (enable) {
                i = 1;
            }
            this.mCallForwardingStatus = i;
            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(1));
            try {
                if (validEfCfis(this.mEfCfis)) {
                    byte[] bArr;
                    if (enable) {
                        bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] | 1);
                    } else {
                        bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] & LastCallFailCause.RADIO_LINK_FAILURE);
                    }
                    log("setVoiceCallForwardingFlag: enable=" + enable + " mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                    if (enable && (TextUtils.isEmpty(dialNumber) ^ 1) != 0) {
                        logv("EF_CFIS: updating cf number, " + Rlog.pii(LOG_TAG, dialNumber));
                        byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(dialNumber);
                        System.arraycopy(bcdNumber, 0, this.mEfCfis, 3, bcdNumber.length);
                        this.mEfCfis[2] = (byte) bcdNumber.length;
                        this.mEfCfis[14] = (byte) -1;
                        this.mEfCfis[15] = (byte) -1;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_CFIS, 1, this.mEfCfis, null, obtainMessage(14, Integer.valueOf(IccConstants.EF_CFIS)));
                } else {
                    log("setVoiceCallForwardingFlag: ignoring enable=" + enable + " invalid mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                }
                if (this.mEfCff != null) {
                    if (enable) {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 10);
                    } else {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 5);
                    }
                    this.mFh.updateEFTransparent(IccConstants.EF_CFF_CPHS, this.mEfCff, obtainMessage(14, Integer.valueOf(IccConstants.EF_CFF_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving call forwarding flag to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchSimRecords();
        }
    }

    public String getOperatorNumeric() {
        String imsi = getIMSI();
        if (imsi == null) {
            log("getOperatorNumeric: IMSI == null");
            return null;
        } else if (this.mMncLength == -1 || this.mMncLength == 0) {
            log("getSIMOperatorNumeric: bad mncLength");
            return null;
        } else if (imsi.length() >= this.mMncLength + 3) {
            return imsi.substring(0, this.mMncLength + 3);
        } else {
            return null;
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.internal.telephony.uicc.SIMRecords.handleMessage(android.os.Message):void, dom blocks: [B:162:0x0677, B:235:0x08f6]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void handleMessage(android.os.Message r27) {
        /*
        r26 = this;
        r16 = 0;
        r0 = r26;
        r2 = r0.mDestroyed;
        r2 = r2.get();
        if (r2 == 0) goto L_0x0045;
    L_0x000c:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r4 = "Received message ";
        r2 = r2.append(r4);
        r0 = r27;
        r2 = r2.append(r0);
        r4 = "[";
        r2 = r2.append(r4);
        r0 = r27;
        r4 = r0.what;
        r2 = r2.append(r4);
        r4 = "] ";
        r2 = r2.append(r4);
        r4 = " while being destroyed. Ignoring.";
        r2 = r2.append(r4);
        r2 = r2.toString();
        r0 = r26;
        r0.loge(r2);
        return;
    L_0x0045:
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        switch(r2) {
            case 1: goto L_0x0055;
            case 3: goto L_0x0073;
            case 4: goto L_0x05fe;
            case 5: goto L_0x032b;
            case 6: goto L_0x03bf;
            case 7: goto L_0x054b;
            case 8: goto L_0x05ae;
            case 9: goto L_0x065f;
            case 10: goto L_0x049d;
            case 11: goto L_0x03bf;
            case 12: goto L_0x0e20;
            case 13: goto L_0x0e98;
            case 14: goto L_0x0eaf;
            case 15: goto L_0x0ec5;
            case 17: goto L_0x10f1;
            case 18: goto L_0x101c;
            case 19: goto L_0x1033;
            case 20: goto L_0x1167;
            case 21: goto L_0x1053;
            case 22: goto L_0x10bd;
            case 24: goto L_0x0e5e;
            case 25: goto L_0x1245;
            case 26: goto L_0x1130;
            case 30: goto L_0x050e;
            case 32: goto L_0x129e;
            case 33: goto L_0x12d8;
            case 34: goto L_0x1329;
            case 36: goto L_0x1382;
            case 37: goto L_0x13db;
            case 38: goto L_0x1431;
            case 39: goto L_0x1487;
            case 40: goto L_0x14fe;
            case 41: goto L_0x153c;
            case 88: goto L_0x1672;
            case 99: goto L_0x15b5;
            case 101: goto L_0x16f4;
            case 102: goto L_0x16ba;
            case 257: goto L_0x004f;
            case 258: goto L_0x0068;
            case 500: goto L_0x0f49;
            case 501: goto L_0x0f7d;
            case 502: goto L_0x0f96;
            case 503: goto L_0x0fd9;
            default: goto L_0x004c;
        };	 Catch:{ RuntimeException -> 0x0059 }
    L_0x004c:
        super.handleMessage(r27);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x004f:
        if (r16 == 0) goto L_0x0054;
    L_0x0051:
        r26.onRecordLoaded();
    L_0x0054:
        return;
    L_0x0055:
        r26.onReady();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
    L_0x0059:
        r13 = move-exception;
        r2 = "Exception parsing SIM record";	 Catch:{ all -> 0x006c }
        r0 = r26;	 Catch:{ all -> 0x006c }
        r0.logw(r2, r13);	 Catch:{ all -> 0x006c }
        if (r16 == 0) goto L_0x0054;
    L_0x0064:
        r26.onRecordLoaded();
        goto L_0x0054;
    L_0x0068:
        r26.onLocked();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
    L_0x006c:
        r2 = move-exception;
        if (r16 == 0) goto L_0x0072;
    L_0x006f:
        r26.onRecordLoaded();
    L_0x0072:
        throw r2;
    L_0x0073:
        r16 = 1;
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x009b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x007f:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception querying IMSI, Exception:";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x009b:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (java.lang.String) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mImsi = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x00e2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x00a9:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x00c0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x00b4:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 15;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 <= r4) goto L_0x00e2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x00c0:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "invalid IMSI ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mImsi = r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x00e2:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "IMSI: mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x011d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0105:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "50218";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.startsWith(r4);	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x011d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0112:
        r2 = "gsm.sim.operator.numeric";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "50218";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.setSystemProperty(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x011d:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x013b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0123:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "46605";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.startsWith(r4);	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x013b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0130:
        r2 = "gsm.sim.operator.numeric";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "46605";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.setSystemProperty(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x013b:
        r2 = android.telephony.TelephonyManager.getDefault();	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.isMultiSimEnabled();	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0178;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0145:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0178;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x014b:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "52018";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.startsWith(r4);	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0165;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0158:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "52005";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.startsWith(r4);	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0178;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0165:
        r2 = "DTAC";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mSpn = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = "gsm.sim.operator.alpha";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mSpn;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.setSystemProperty(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0178:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.OemConstant.isTestCard(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mIsTestCard = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "leon mIsTestCard: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mIsTestCard;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = "persist.sys.net_build_type";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "allnet";	 Catch:{ RuntimeException -> 0x0059 }
        r22 = android.os.SystemProperties.get(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = "allnetcmcctest";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r22;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.equals(r0);	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x01c3;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x01b6:
        r2 = "Force set any card as test card";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mIsTestCard = r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x01c3:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mParentApp;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.getPhoneId();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.PhoneFactory.getPhone(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mIsTestCard;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x02ff;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x01d5:
        r2 = 2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x01d6:
        r4.oppoSetSimType(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0221;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x01df:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0221;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x01ea:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "IMSI: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.substring(r5, r6);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "SIMRecords";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5.substring(r6);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = android.telephony.Rlog.pii(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0221:
        r14 = r26.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0232;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x022b:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0271;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0232:
        if (r14 == 0) goto L_0x0271;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0234:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0271;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x023b:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0245:
        if (r2 >= r5) goto L_0x0271;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0247:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0302;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x024f:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "IMSI: setting1 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0271:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x02a6;
    L_0x0277:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0306 }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0306 }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0306 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0306 }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0306 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0306 }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0306 }
        r4 = "setting2 mMncLength=";	 Catch:{ NumberFormatException -> 0x0306 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0306 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0306 }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0306 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0306 }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0306 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0306 }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0306 }
    L_0x02a6:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x02f6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x02ac:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x02f6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x02b3:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x02f6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x02bf:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x02f6:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsiReadyRegistrants;	 Catch:{ RuntimeException -> 0x0059 }
        r2.notifyRegistrants();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x02ff:
        r2 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x01d6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0302:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0245;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0306:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting3 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x02a6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x032b:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r17 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x037b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x033d:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_MBI: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 & 255;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMailboxIndex = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMailboxIndex;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x037b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0369:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMailboxIndex;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x037b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0371:
        r2 = "Got valid mailbox number for MBDN";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r17 = 1;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x037b:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mRecordsToLoad;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mRecordsToLoad = r2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r17 == 0) goto L_0x03a4;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0387:
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMailboxIndex;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.obtainMessage(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28615; // 0x6fc7 float:4.0098E-41 double:1.41377E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 28616; // 0x6fc8 float:4.01E-41 double:1.4138E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadFromEF(r6, r7, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03a4:
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.obtainMessage(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 28439; // 0x6f17 float:3.9852E-41 double:1.40507E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28490; // 0x6f4a float:3.9923E-41 double:1.4076E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadFromEF(r5, r6, r7, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03bf:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailNum = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0429;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03d5:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Invalid or missing EF";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r5) goto L_0x0425;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03e9:
        r2 = "[MAILBOX]";	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03ec:
        r2 = r4.append(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0400:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mRecordsToLoad;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mRecordsToLoad = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.obtainMessage(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 28439; // 0x6f17 float:3.9852E-41 double:1.40507E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28490; // 0x6f4a float:3.9923E-41 double:1.4076E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadFromEF(r5, r6, r7, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0425:
        r2 = "[MBDN]";	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x03ec;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0429:
        r3 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r3 = (com.android.internal.telephony.uicc.AdnRecord) r3;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "VM: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r2.append(r3);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r5) goto L_0x0487;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0445:
        r2 = " EF[MAILBOX]";	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0448:
        r2 = r4.append(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r3.isEmpty();	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x048b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x045b:
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x048b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0462:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mRecordsToLoad;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mRecordsToLoad = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.obtainMessage(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 28439; // 0x6f17 float:3.9852E-41 double:1.40507E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28490; // 0x6f4a float:3.9923E-41 double:1.4076E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadFromEF(r5, r6, r7, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0487:
        r2 = " EF[MBDN]";	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0448;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x048b:
        r2 = r3.getNumber();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailNum = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r3.getAlphaTag();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x049d:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x04b3;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x04a9:
        r2 = "Invalid or missing EF[MSISDN]";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x04b3:
        r3 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r3 = (com.android.internal.telephony.uicc.AdnRecord) r3;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r3.getNumber();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMsisdn = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r3.getAlphaTag();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMsisdnTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MSISDN: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "SIMRecords";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMsisdn;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = android.telephony.Rlog.pii(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MSISDN is empty: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMsisdn;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = android.text.TextUtils.isEmpty(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x050e:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0532;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x051a:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewMsisdn;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMsisdn = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewMsisdnTag;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMsisdnTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = "Success to update EF[MSISDN]";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0532:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0536:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x054b:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_MWIS : ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0595;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0578:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_GET_MWIS_DONE exception = ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0595:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 & 255;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x05a8;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x059e:
        r2 = "SIMRecords: Uninitialized record MWIS";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05a8:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfMWIS = r11;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05ae:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_CPHS_MWI: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x05f8;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05db:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE exception = ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05f8:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCPHS_MWI = r11;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05fe:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x060e:
        r2 = r11.length;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.IccUtils.bcdToString(r11, r4, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mIccId = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11.length;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.IccUtils.bchToString(r11, r4, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mFullIccId = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "iccid: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFullIccId;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = android.telephony.SubscriptionInfo.givePrintableIccid(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mIccId;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0656;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0649:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mIccId;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.equals(r4);	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0656:
        r2 = "ffffffffffffffffffff";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mIccId = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
    L_0x065f:
        r16 = 1;
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r2 = r0.mCarrierTestOverride;	 Catch:{ all -> 0x0800 }
        r2 = r2.isInTestMode();	 Catch:{ all -> 0x0800 }
        if (r2 == 0) goto L_0x08f6;	 Catch:{ all -> 0x0800 }
    L_0x066b:
        r2 = r26.getIMSI();	 Catch:{ all -> 0x0800 }
        if (r2 == 0) goto L_0x08f6;	 Catch:{ all -> 0x0800 }
    L_0x0671:
        r14 = r26.getIMSI();	 Catch:{ all -> 0x0800 }
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x07db }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x07db }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x07db }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x07db }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x07db }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x07db }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x07db }
        r4 = "[TestMode] mMncLength=";	 Catch:{ NumberFormatException -> 0x07db }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x07db }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x07db }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x07db }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x07db }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x07db }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x07db }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x07db }
    L_0x06a4:
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r2 = r0.mMncLength;	 Catch:{ all -> 0x0800 }
        r4 = 15;	 Catch:{ all -> 0x0800 }
        if (r2 != r4) goto L_0x0d42;	 Catch:{ all -> 0x0800 }
    L_0x06ac:
        r2 = 0;	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.mMncLength = r2;	 Catch:{ all -> 0x0800 }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0800 }
        r2.<init>();	 Catch:{ all -> 0x0800 }
        r4 = "setting5 mMncLength=";	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r4 = r0.mMncLength;	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r2 = r2.toString();	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.log(r2);	 Catch:{ all -> 0x0800 }
    L_0x06ce:
        r14 = r26.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x06df;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x06d9:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0d74;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x06df:
        if (r14 == 0) goto L_0x0739;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x06e1:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0739;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x06e8:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x070d:
        if (r2 >= r5) goto L_0x0739;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x070f:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0d7d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0717:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0739:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0746;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x073f:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;
        if (r2 != r4) goto L_0x0777;
    L_0x0746:
        if (r14 == 0) goto L_0x0da6;
    L_0x0748:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0d81 }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0d81 }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0d81 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0d81 }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0d81 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0d81 }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0d81 }
        r4 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0d81 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0d81 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0d81 }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0d81 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0d81 }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0d81 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0d81 }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0d81 }
    L_0x0777:
        if (r14 == 0) goto L_0x07c2;
    L_0x0779:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x07c2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x077f:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x07c2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x078b:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x07c2:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.isNeedSetSpnLater;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x07c8:
        r2 = r26.getServiceProviderName();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.oppoSetSimSpn(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = "set spn again.";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
    L_0x07db:
        r12 = move-exception;
        r2 = 0;
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.mMncLength = r2;	 Catch:{ all -> 0x0800 }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0800 }
        r2.<init>();	 Catch:{ all -> 0x0800 }
        r4 = "[TestMode] Corrupt IMSI! mMncLength=";	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r4 = r0.mMncLength;	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r2 = r2.toString();	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.loge(r2);	 Catch:{ all -> 0x0800 }
        goto L_0x06a4;
    L_0x0800:
        r2 = move-exception;
        r14 = r26.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 == r5) goto L_0x0812;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x080c:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 != 0) goto L_0x0dca;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0812:
        if (r14 == 0) goto L_0x086c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0814:
        r4 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 < r5) goto L_0x086c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x081b:
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r5.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0840:
        if (r4 >= r6) goto L_0x086c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0842:
        r19 = r5[r4];	 Catch:{ RuntimeException -> 0x0059 }
        r7 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r7 == 0) goto L_0x0dd3;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x084a:
        r4 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r4);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x086c:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 == 0) goto L_0x0879;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0872:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = -1;
        if (r4 != r5) goto L_0x08aa;
    L_0x0879:
        if (r14 == 0) goto L_0x0dfc;
    L_0x087b:
        r4 = 0;
        r5 = 3;
        r4 = r14.substring(r4, r5);	 Catch:{ NumberFormatException -> 0x0dd7 }
        r18 = java.lang.Integer.parseInt(r4);	 Catch:{ NumberFormatException -> 0x0dd7 }
        r4 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0dd7 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0dd7 }
        r0.mMncLength = r4;	 Catch:{ NumberFormatException -> 0x0dd7 }
        r4 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0dd7 }
        r4.<init>();	 Catch:{ NumberFormatException -> 0x0dd7 }
        r5 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0dd7 }
        r4 = r4.append(r5);	 Catch:{ NumberFormatException -> 0x0dd7 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0dd7 }
        r5 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0dd7 }
        r4 = r4.append(r5);	 Catch:{ NumberFormatException -> 0x0dd7 }
        r4 = r4.toString();	 Catch:{ NumberFormatException -> 0x0dd7 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0dd7 }
        r0.log(r4);	 Catch:{ NumberFormatException -> 0x0dd7 }
    L_0x08aa:
        if (r14 == 0) goto L_0x08f5;
    L_0x08ac:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 == 0) goto L_0x08f5;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x08b2:
        r4 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 < r5) goto L_0x08f5;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x08be:
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r14.substring(r6, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r14.substring(r6, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r4, r5, r6);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x08f5:
        throw r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x08f6:
        r0 = r27;	 Catch:{ all -> 0x0800 }
        r9 = r0.obj;	 Catch:{ all -> 0x0800 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ all -> 0x0800 }
        r11 = r9.result;	 Catch:{ all -> 0x0800 }
        r11 = (byte[]) r11;	 Catch:{ all -> 0x0800 }
        r2 = r9.exception;	 Catch:{ all -> 0x0800 }
        if (r2 == 0) goto L_0x0a4f;
    L_0x0904:
        r14 = r26.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x0915;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x090f:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x09fa;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0915:
        if (r14 == 0) goto L_0x096f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0917:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x096f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x091e:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0943:
        if (r2 >= r5) goto L_0x096f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0945:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0a03;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x094d:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x096f:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x097c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0975:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;
        if (r2 != r4) goto L_0x09ad;
    L_0x097c:
        if (r14 == 0) goto L_0x0a2b;
    L_0x097e:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0a07 }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0a07 }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0a07 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0a07 }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0a07 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0a07 }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0a07 }
        r4 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0a07 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0a07 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0a07 }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0a07 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0a07 }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0a07 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0a07 }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0a07 }
    L_0x09ad:
        if (r14 == 0) goto L_0x004f;
    L_0x09af:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09b5:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09c1:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09fa:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x096f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a01:
        goto L_0x0915;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a03:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0943;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a07:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x09ad;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a2b:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x09ad;
    L_0x0a4f:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0800 }
        r2.<init>();	 Catch:{ all -> 0x0800 }
        r4 = "EF_AD: ";	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r2 = r2.toString();	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.log(r2);	 Catch:{ all -> 0x0800 }
        r2 = r11.length;	 Catch:{ all -> 0x0800 }
        r4 = 3;	 Catch:{ all -> 0x0800 }
        if (r2 >= r4) goto L_0x0bc3;	 Catch:{ all -> 0x0800 }
    L_0x0a70:
        r2 = "Corrupt AD data on SIM";	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.log(r2);	 Catch:{ all -> 0x0800 }
        r14 = r26.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x0a89;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a83:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0b6e;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a89:
        if (r14 == 0) goto L_0x0ae3;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a8b:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0ae3;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a92:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ab7:
        if (r2 >= r5) goto L_0x0ae3;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ab9:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0b77;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ac1:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ae3:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0af0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ae9:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;
        if (r2 != r4) goto L_0x0b21;
    L_0x0af0:
        if (r14 == 0) goto L_0x0b9f;
    L_0x0af2:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0b7b }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0b7b }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0b7b }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0b7b }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0b7b }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0b7b }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0b7b }
        r4 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0b7b }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0b7b }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0b7b }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0b7b }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0b7b }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0b7b }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0b7b }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0b7b }
    L_0x0b21:
        if (r14 == 0) goto L_0x004f;
    L_0x0b23:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b29:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b35:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b6e:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0ae3;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b75:
        goto L_0x0a89;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b77:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0ab7;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b7b:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0b21;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b9f:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0b21;
    L_0x0bc3:
        r2 = r11.length;	 Catch:{ all -> 0x0800 }
        r4 = 3;	 Catch:{ all -> 0x0800 }
        if (r2 != r4) goto L_0x0d1a;	 Catch:{ all -> 0x0800 }
    L_0x0bc7:
        r2 = "MNC length not present in EF_AD";	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.log(r2);	 Catch:{ all -> 0x0800 }
        r14 = r26.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x0be0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0bda:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0cc5;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0be0:
        if (r14 == 0) goto L_0x0c3a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0be2:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0c3a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0be9:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c0e:
        if (r2 >= r5) goto L_0x0c3a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c10:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0cce;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c18:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c3a:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0c47;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c40:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;
        if (r2 != r4) goto L_0x0c78;
    L_0x0c47:
        if (r14 == 0) goto L_0x0cf6;
    L_0x0c49:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0cd2 }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0cd2 }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0cd2 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0cd2 }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0cd2 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0cd2 }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0cd2 }
        r4 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0cd2 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0cd2 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0cd2 }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0cd2 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0cd2 }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0cd2 }
        r0 = r26;	 Catch:{ NumberFormatException -> 0x0cd2 }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0cd2 }
    L_0x0c78:
        if (r14 == 0) goto L_0x004f;
    L_0x0c7a:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c80:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c8c:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0cc5:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0c3a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ccc:
        goto L_0x0be0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0cce:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0c0e;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0cd2:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0c78;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0cf6:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0c78;
    L_0x0d1a:
        r2 = 3;
        r2 = r11[r2];	 Catch:{ all -> 0x0800 }
        r2 = r2 & 15;	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.mMncLength = r2;	 Catch:{ all -> 0x0800 }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0800 }
        r2.<init>();	 Catch:{ all -> 0x0800 }
        r4 = "setting4 mMncLength=";	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r4 = r0.mMncLength;	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r2 = r2.toString();	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.log(r2);	 Catch:{ all -> 0x0800 }
        goto L_0x06a4;	 Catch:{ all -> 0x0800 }
    L_0x0d42:
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r2 = r0.mMncLength;	 Catch:{ all -> 0x0800 }
        r4 = 2;	 Catch:{ all -> 0x0800 }
        if (r2 == r4) goto L_0x06ce;	 Catch:{ all -> 0x0800 }
    L_0x0d49:
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r2 = r0.mMncLength;	 Catch:{ all -> 0x0800 }
        r4 = 3;	 Catch:{ all -> 0x0800 }
        if (r2 == r4) goto L_0x06ce;	 Catch:{ all -> 0x0800 }
    L_0x0d50:
        r2 = -1;	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.mMncLength = r2;	 Catch:{ all -> 0x0800 }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0800 }
        r2.<init>();	 Catch:{ all -> 0x0800 }
        r4 = "setting5 mMncLength=";	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r4 = r0.mMncLength;	 Catch:{ all -> 0x0800 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0800 }
        r2 = r2.toString();	 Catch:{ all -> 0x0800 }
        r0 = r26;	 Catch:{ all -> 0x0800 }
        r0.log(r2);	 Catch:{ all -> 0x0800 }
        goto L_0x06ce;
    L_0x0d74:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0739;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d7b:
        goto L_0x06df;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d7d:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x070d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d81:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0777;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0da6:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0777;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0dca:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 != r5) goto L_0x086c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0dd1:
        goto L_0x0812;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0dd3:
        r4 = r4 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0840;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0dd7:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x08aa;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0dfc:
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x08aa;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e20:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        if (r9 == 0) goto L_0x0e56;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e2a:
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0e56;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e2e:
        r2 = r26.getServiceProviderName();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mSpNameInEfSpn = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mSpNameInEfSpn;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0e56;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e3c:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mSpNameInEfSpn;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.equals(r4);	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0e56;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e49:
        r2 = "set spNameInEfSpn to null because parsing result is empty";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mSpNameInEfSpn = r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e56:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.getSpnFsm(r2, r9);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e5e:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0e75;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e6e:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCff = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e75:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_CFF_CPHS: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCff = r11;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e98:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ea8:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.parseEfSpdi(r11);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0eaf:
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0eb9:
        r2 = "update failed. ";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.logw(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ec5:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ed5:
        r25 = new com.android.internal.telephony.gsm.SimTlv;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11.length;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r0.<init>(r11, r4, r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ede:
        r2 = r25.isValidObject();	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ee4:
        r2 = r25.getTag();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 67;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0f45;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0eec:
        r2 = r25.getData();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r25.getData();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.IccUtils.networkNameToString(r2, r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mPnnHomeName = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_GET_PNN_DONE tlv.getData() = ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r25.getData();	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_GET_PNN_DONE mPnnHomeName = ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mPnnHomeName;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.telephony.OppoTelephonyConstant.PROPERTY_ICC_OPERATOR_PNN_NAME;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mPnnHomeName;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.setSystemProperty(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f45:
        r25.nextObject();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0ede;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f49:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0f72;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f55:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_GET_ALL_PNN_DONE exception = ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f72:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (java.util.ArrayList) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.parseEFpnn(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f7d:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        if (r9 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f87:
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f8b:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (java.util.ArrayList) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.parseEFopl(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f96:
        r2 = "handleMessage (EVENT_GET_CPHSONS_DONE)";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        if (r9 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0fa8:
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0fac:
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11.length;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.IccUtils.adnStringFieldToString(r11, r4, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.cphsOnsl = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Load EF_SPN_CPHS: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.cphsOnsl;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0fd9:
        r2 = "handleMessage (EVENT_GET_SHORT_CPHSONS_DONE)";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        if (r9 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0feb:
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0fef:
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11.length;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.IccUtils.adnStringFieldToString(r11, r4, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.cphsOnss = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Load EF_SPN_SHORT_CPHS: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.cphsOnss;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x101c:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1028:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (java.util.ArrayList) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handleSmses(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1033:
        r2 = "ENF";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "marked read: sms ";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.arg1;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        android.telephony.Rlog.i(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1053:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r15 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r15 = (java.lang.Integer) r15;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x1065;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1063:
        if (r15 != 0) goto L_0x108d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1065:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Error on SMS_ON_SIM with exp ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = " index ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r15);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x108d:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "READ EF_SMS RECORD index=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r15);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r15.intValue();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 22;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.obtainMessage(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28476; // 0x6f3c float:3.9903E-41 double:1.4069E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadEFLinearFixed(r6, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x10bd:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x10d4;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x10c9:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (byte[]) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handleSms(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x10d4:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Error on GET_SMS with exp ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x10f1:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1101:
        r2 = new com.android.internal.telephony.uicc.UsimServiceTable;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mUsimServiceTable = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "SST: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mUsimServiceTable;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfSST = r11;	 Catch:{ RuntimeException -> 0x0059 }
        r26.fetchPnnAndOpl();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1130:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x113c:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (byte[]) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mCphsInfo = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "iCPHS: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mCphsInfo;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1167:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_SET_MBDN_DONE ex:";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x119e;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x118e:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewVoiceMailNum;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailNum = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewVoiceMailTag;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x119e:
        r2 = r26.isCphsMailboxEnabled();	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x11f9;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x11a4:
        r3 = new com.android.internal.telephony.uicc.AdnRecord;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mVoiceMailTag;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mVoiceMailNum;	 Catch:{ RuntimeException -> 0x0059 }
        r3.<init>(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r23 = r0;	 Catch:{ RuntimeException -> 0x0059 }
        r23 = (android.os.Message) r23;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x11db;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x11bb:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x11db;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x11bf:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        r2 = "Callback with MBDN successful.";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r23 = 0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x11db:
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 25;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r1 = r23;	 Catch:{ RuntimeException -> 0x0059 }
        r8 = r0.obtainMessage(r4, r1);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 28439; // 0x6f17 float:3.9852E-41 double:1.40507E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 28490; // 0x6f4a float:3.9923E-41 double:1.4076E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2.updateEF(r3, r4, r5, r6, r7, r8);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x11f9:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x11fd:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "carrier_config";	 Catch:{ RuntimeException -> 0x0059 }
        r10 = r2.getSystemService(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r10 = (android.telephony.CarrierConfigManager) r10;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x1238;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x120e:
        if (r10 == 0) goto L_0x1238;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1210:
        r2 = r10.getConfig();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "editable_voicemail_number_bool";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.getBoolean(r4);	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x1238;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x121d:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new com.android.internal.telephony.uicc.IccVmNotSupportedException;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "Update SIM voice mailbox error";	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x122f:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1238:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x122f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1245:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x1282;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1251:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewVoiceMailNum;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailNum = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewVoiceMailTag;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1261:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1265:
        r2 = "Callback with CPHS MB successful.";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1282:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Set CPHS MailBox with exception: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x1261;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x129e:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x12b5;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x12ae:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCfis = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x12b5:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_CFIS: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCfis = r11;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x12d8:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x1301;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x12e4:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception in fetching EF_CSP data ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1301:
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_CSP: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handleEfCspData(r11);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1329:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x135b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1339:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception in get GID1 ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mGid1 = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x135b:
        r2 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mGid1 = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "GID1: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mGid1;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1382:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x13b4;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1392:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception in get GID2 ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mGid2 = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x13b4:
        r2 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mGid2 = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "GID2: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mGid2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x13db:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x13ed;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x13eb:
        if (r11 != 0) goto L_0x140a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x13ed:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting User PLMN with Access Tech Records: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x140a:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Received a PlmnActRecord, raw=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.PlmnActRecord.getRecords(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mPlmnActRecords = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1431:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x1443;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1441:
        if (r11 != 0) goto L_0x1460;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1443:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting Operator PLMN with Access Tech Records: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1460:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Received a PlmnActRecord, raw=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.PlmnActRecord.getRecords(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mOplmnActRecords = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1487:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x1499;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1497:
        if (r11 != 0) goto L_0x14b6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1499:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting Home PLMN with Access Tech Records: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x14b6:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Received a PlmnActRecord, raw=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.PlmnActRecord.getRecords(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mHplmnActRecords = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "HplmnActRecord[]=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mHplmnActRecords;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = java.util.Arrays.toString(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x14fe:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x1510;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x150e:
        if (r11 != 0) goto L_0x152d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1510:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting Equivalent Home PLMNs: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x152d:
        r2 = "Equivalent Home";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.parseBcdPlmnList(r11, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEhplmns = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x153c:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x154e;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x154c:
        if (r11 != 0) goto L_0x156b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x154e:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting Forbidden PLMNs: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x156b:
        r2 = "Forbidden";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.parseBcdPlmnList(r11, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mFplmns = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.arg1;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 1238273; // 0x12e501 float:1.73519E-39 double:6.11788E-318;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1581:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.arg2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = java.lang.Integer.valueOf(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r24 = r0.retrievePendingResponseMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        if (r24 == 0) goto L_0x15ab;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1593:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mFplmns;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFplmns;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = java.util.Arrays.copyOf(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        android.os.AsyncResult.forMessage(r0, r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r24.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x15ab:
        r2 = "Failed to retrieve a response message for FPLMN";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x15b5:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_GET_POL_DONE fileid:";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.arg1;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x162e;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x15dc:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception in fetching EF POL data ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.arg1;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 28464; // 0x6f30 float:3.9887E-41 double:1.4063E-319;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x1619;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x15ff:
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.onCompleteMsg;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 99;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28464; // 0x6f30 float:3.9887E-41 double:1.4063E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.obtainMessage(r5, r6, r7, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 28464; // 0x6f30 float:3.9887E-41 double:1.4063E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadEFTransparent(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1619:
        r0 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r24 = r0;	 Catch:{ RuntimeException -> 0x0059 }
        r24 = (android.os.Message) r24;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r1 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handlePlmnListData(r1, r11, r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x162e:
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_GET_POL_DONE data ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1653:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r21 = r0;	 Catch:{ RuntimeException -> 0x0059 }
        r21 = (android.os.Message) r21;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.arg1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r1 = r21;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handleEfPOLResponse(r2, r11, r1);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1672:
        r2 = "wjp_pol EVENT_SET_POL_DONE";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x169f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1684:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception in EVENT_SET_POL_DONE EF POL data ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x169f:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x16a3:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r21 = r0;	 Catch:{ RuntimeException -> 0x0059 }
        r21 = (android.os.Message) r21;	 Catch:{ RuntimeException -> 0x0059 }
        r21.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x16ba:
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x16df;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x16c4:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception get PLMN data ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x16df:
        r0 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r24 = r0;	 Catch:{ RuntimeException -> 0x0059 }
        r24 = (android.os.Message) r24;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r1 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handlePlmnListData(r1, r11, r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x16f4:
        r0 = r27;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x1719;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x16fe:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception update PLMN data ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1719:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r26;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handlePlmnListData(r2, r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.handleMessage(android.os.Message):void");
    }

    protected void handleFileUpdate(int efid) {
        switch (efid) {
            case IccConstants.EF_CFF_CPHS /*28435*/:
            case IccConstants.EF_CFIS /*28619*/:
                log("SIM Refresh called for EF_CFIS or EF_CFF_CPHS");
                loadCallForwardingRecords();
                return;
            case IccConstants.EF_CSP_CPHS /*28437*/:
                this.mRecordsToLoad++;
                log("[CSP] SIM Refresh for EF_CSP_CPHS");
                this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
                return;
            case IccConstants.EF_MAILBOX_CPHS /*28439*/:
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                return;
            case IccConstants.EF_FDN /*28475*/:
                log("SIM Refresh called for EF_FDN");
                this.mParentApp.queryFdn();
                this.mAdnCache.reset();
                return;
            case IccConstants.EF_MSISDN /*28480*/:
                this.mRecordsToLoad++;
                log("SIM Refresh called for EF_MSISDN");
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
                return;
            case IccConstants.EF_MBDN /*28615*/:
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, obtainMessage(6));
                return;
            default:
                this.mAdnCache.reset();
                fetchSimRecords();
                return;
        }
    }

    private int dispatchGsmMessage(SmsMessage message) {
        this.mNewSmsRegistrants.notifyResult(message);
        return 0;
    }

    private void handleSms(byte[] ba) {
        if (ba[0] != (byte) 0) {
            Rlog.d("ENF", "status : " + ba[0]);
        }
        if (ba[0] == (byte) 3) {
            int n = ba.length;
            byte[] pdu = new byte[(n - 1)];
            System.arraycopy(ba, 1, pdu, 0, n - 1);
            dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
        }
    }

    private void handleSmses(ArrayList<byte[]> messages) {
        int count = messages.size();
        for (int i = 0; i < count; i++) {
            byte[] ba = (byte[]) messages.get(i);
            if (ba[0] != (byte) 0) {
                Rlog.i("ENF", "status " + i + ": " + ba[0]);
            }
            if (ba[0] == (byte) 3) {
                int n = ba.length;
                byte[] pdu = new byte[(n - 1)];
                System.arraycopy(ba, 1, pdu, 0, n - 1);
                dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
                ba[0] = (byte) 1;
            }
        }
    }

    protected void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            onAllRecordsLoaded();
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    private void setVoiceCallForwardingFlagFromSimRecords() {
        int i = 1;
        if (validEfCfis(this.mEfCfis)) {
            this.mCallForwardingStatus = this.mEfCfis[1] & 1;
            log("EF_CFIS: callForwardingEnabled=" + this.mCallForwardingStatus);
        } else if (this.mEfCff != null) {
            if ((this.mEfCff[0] & 15) != 10) {
                i = 0;
            }
            this.mCallForwardingStatus = i;
            log("EF_CFF: callForwardingEnabled=" + this.mCallForwardingStatus);
        } else {
            this.mCallForwardingStatus = -1;
            log("EF_CFIS and EF_CFF not valid. callForwardingEnabled=" + this.mCallForwardingStatus);
        }
    }

    public static boolean isNeedToChangeRegion(Context context) {
        if (Global.getInt(context.getContentResolver(), CHANGE_TO_REGION, 0) == 0) {
            return true;
        }
        return false;
    }

    public static void ChangeRegion(Context context, boolean isOn) {
        Global.putInt(context.getContentResolver(), CHANGE_TO_REGION, isOn ? 0 : 1);
    }

    public static boolean isNetLockRegionMachine() {
        if (SystemProperties.get(RegionLockConstant.NETLOCK_VERSION, "NULL").equals("NULL")) {
            return false;
        }
        return true;
    }

    protected void onAllRecordsLoaded() {
        log("record load complete");
        this.mIsSimLoaded = true;
        if (Resources.getSystem().getBoolean(17957055)) {
            setSimLanguage(this.mEfLi, this.mEfPl);
        } else {
            log("Not using EF LI/EF PL");
        }
        if (getSimState() == 1) {
            log("trigger by onlock(),so retrun");
            return;
        }
        setVoiceCallForwardingFlagFromSimRecords();
        if (this.mParentApp.getState() == AppState.APPSTATE_PIN || this.mParentApp.getState() == AppState.APPSTATE_PUK || getSimState() == 1) {
            this.mRecordsRequested = false;
            if (getSimState() == 1) {
                log("trigger by onlock(),so retrun");
                setSimState(2);
            }
            return;
        }
        String operator = getOperatorNumeric();
        if (TextUtils.isEmpty(operator)) {
            log("onAllRecordsLoaded empty 'gsm.sim.operator.numeric' skipping");
        } else {
            log("onAllRecordsLoaded set 'gsm.sim.operator.numeric' to operator='" + operator + "'");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operator);
        }
        String imsi = getIMSI();
        if (TextUtils.isEmpty(imsi) || imsi.length() < 3) {
            log("onAllRecordsLoaded empty imsi skipping setting mcc");
        } else {
            log("onAllRecordsLoaded set mcc imsi" + SpnOverride.MVNO_TYPE_NONE);
            this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), MccTable.countryCodeForMcc(Integer.parseInt(imsi.substring(0, 3))));
        }
        if (SystemProperties.get("ro.oppo.version", "CN").equals("US")) {
            String region = SystemProperties.get("persist.sys.oppo.region", "CN");
            String operatorVersion = SystemProperties.get("ro.oppo.operator", SpnOverride.MVNO_TYPE_NONE);
            boolean isNetLockRegion = isNetLockRegionMachine();
            String country = this.mTelephonyManager.getSimCountryIsoForPhone(this.mParentApp.getPhoneId());
            String upperCountry = null;
            if (isNeedToChangeRegion(this.mContext) && (isNetLockRegion ^ 1) != 0 && TextUtils.isEmpty(operatorVersion)) {
                if (!TextUtils.isEmpty(country)) {
                    upperCountry = country.toUpperCase();
                }
                if (!TextUtils.isEmpty(upperCountry) && upperCountry.equals("CN")) {
                    upperCountry = "OC";
                }
                log("upperCountry = " + upperCountry);
                if (!(TextUtils.isEmpty(upperCountry) || (upperCountry.equals(region) ^ 1) == 0)) {
                    log("Need to change region");
                    boolean result = this.mPhone != null ? this.mPhone.getContext().getPackageManager().loadRegionFeature(upperCountry) : false;
                    log("result " + result);
                    if (result) {
                        SystemProperties.set("persist.sys.oppo.region", upperCountry);
                        this.mContext.sendBroadcast(new Intent("android.settings.OPPO_REGION_CHANGED"));
                    }
                }
                ChangeRegion(this.mContext, false);
            }
        }
        setVoiceMailByCountry(operator);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
    }

    private void handleCarrierNameOverride() {
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configLoader == null || !configLoader.getConfig().getBoolean("carrier_name_override_bool")) {
            setSpnFromConfig(getOperatorNumeric());
        } else {
            String carrierName = configLoader.getConfig().getString("carrier_name_string");
            setServiceProviderName(carrierName);
            this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), carrierName);
        }
        setDisplayName();
    }

    private void setDisplayName() {
        SubscriptionManager subManager = SubscriptionManager.from(this.mContext);
        int[] subId = SubscriptionManager.getSubId(this.mParentApp.getPhoneId());
        if (subId == null || subId.length <= 0) {
            log("subId not valid for Phone " + this.mParentApp.getPhoneId());
            return;
        }
        SubscriptionInfo subInfo = subManager.getActiveSubscriptionInfo(subId[0]);
        if (subInfo == null || subInfo.getNameSource() == 2) {
            log("SUB[" + this.mParentApp.getPhoneId() + "] " + subId[0] + " SubInfo not created yet");
        } else {
            CharSequence oldSubName = subInfo.getDisplayName();
            String newCarrierName = this.mTelephonyManager.getSimOperatorName(subId[0]);
            if (!(TextUtils.isEmpty(newCarrierName) || (newCarrierName.equals(oldSubName) ^ 1) == 0)) {
                log("sim name[" + this.mParentApp.getPhoneId() + "] = " + newCarrierName);
                SubscriptionController.getInstance().setDisplayName(newCarrierName, subId[0]);
            }
        }
    }

    private void setSpnFromConfig(String carrier) {
        boolean isCnList = isInCnList(this.mSpn);
        if (isCnList || TextUtils.isEmpty(this.mSpn) || (this.mSpn != null && this.mSpn.startsWith("460"))) {
            if (isCnList && "20404".equals(carrier)) {
                carrier = "46011";
            }
            String operator = SubscriptionController.getOemOperator(this.mContext, carrier);
            if (!TextUtils.isEmpty(operator)) {
                setServiceProviderName(operator);
            }
        }
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), getServiceProviderName());
    }

    private void setVoiceMailByCountry(String spn) {
        if (this.mVmConfig.containsCarrier(spn)) {
            this.mIsVoiceMailFixed = true;
            this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn);
            this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn);
        }
    }

    public void getForbiddenPlmns(Message response) {
        this.mFh.loadEFTransparent(IccConstants.EF_FPLMN, obtainMessage(41, 1238273, storePendingResponseMessage(response)));
    }

    public void onReady() {
        setSimState(3);
        fetchSimRecords();
    }

    private void onLocked() {
        log("only fetch EF_LI and EF_PL in lock state");
        if (getSimState() == 0) {
            setSimState(1);
        }
        loadEfLiAndEfPl();
    }

    private void loadEfLiAndEfPl() {
        if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
            this.mRecordsRequested = true;
            this.mFh.loadEFTransparent(IccConstants.EF_LI, obtainMessage(100, new EfUsimLiLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded(this, null)));
            this.mRecordsToLoad++;
        }
    }

    private void loadCallForwardingRecords() {
        this.mRecordsRequested = true;
        this.mFh.loadEFLinearFixed(IccConstants.EF_CFIS, 1, obtainMessage(32));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CFF_CPHS, obtainMessage(24));
        this.mRecordsToLoad++;
    }

    protected void fetchSimRecords() {
        this.mRecordsRequested = true;
        log("fetchSimRecords " + this.mRecordsToLoad);
        this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(3));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(4));
        this.mRecordsToLoad++;
        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_MBI, 1, obtainMessage(5));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_AD, obtainMessage(9));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_MWIS, 1, obtainMessage(7));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, obtainMessage(8));
        this.mRecordsToLoad++;
        loadCallForwardingRecords();
        getSpnFsm(true, null);
        this.mFh.loadEFTransparent(IccConstants.EF_SPDI, obtainMessage(13));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_PNN, 1, obtainMessage(15));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_SST, obtainMessage(17));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_INFO_CPHS, obtainMessage(26));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_GID1, obtainMessage(34));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_GID2, obtainMessage(36));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28512, obtainMessage(37));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28513, obtainMessage(38));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_HPLMN_W_ACT, obtainMessage(39));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_EHPLMN, obtainMessage(40));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_FPLMN, obtainMessage(41, 1238272, -1));
        this.mRecordsToLoad++;
        loadEfLiAndEfPl();
        this.mFh.getEFLinearRecordSize(IccConstants.EF_SMS, obtainMessage(28));
        fetchCPHSOns();
        fetchCdmaPrl();
        log("fetchSimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    public int getDisplayRule(String plmn) {
        if (this.mParentApp != null && this.mParentApp.getUiccCard() != null && this.mParentApp.getUiccCard().getOperatorBrandOverride() != null) {
            return 2;
        }
        if (TextUtils.isEmpty(getServiceProviderName()) || this.mSpnDisplayCondition == -1) {
            return 2;
        }
        if (isOnMatchingPlmn(plmn)) {
            if ((this.mSpnDisplayCondition & 1) == 1) {
                return 3;
            }
            return 1;
        } else if ((this.mSpnDisplayCondition & 2) == 0) {
            return 3;
        } else {
            return 2;
        }
    }

    private boolean isOnMatchingPlmn(String plmn) {
        if (plmn == null) {
            return false;
        }
        if (plmn.equals(getOperatorNumeric())) {
            return true;
        }
        if (this.mSpdiNetworks != null) {
            for (String spdiNet : this.mSpdiNetworks) {
                if (plmn.equals(spdiNet)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getSpnFsm(boolean start, AsyncResult ar) {
        if (start) {
            if (this.mSpnState == GetSpnFsmState.READ_SPN_3GPP || this.mSpnState == GetSpnFsmState.READ_SPN_CPHS || this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS || this.mSpnState == GetSpnFsmState.INIT) {
                this.mSpnState = GetSpnFsmState.INIT;
                return;
            }
            this.mSpnState = GetSpnFsmState.INIT;
        }
        byte[] data;
        String spn;
        switch (m11xf89fffa2()[this.mSpnState.ordinal()]) {
            case 1:
                setServiceProviderName(null);
                this.mFh.loadEFTransparent(IccConstants.EF_SPN, obtainMessage(12));
                this.mRecordsToLoad++;
                this.mSpnState = GetSpnFsmState.READ_SPN_3GPP;
                break;
            case 2:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                } else {
                    data = ar.result;
                    this.mSpnDisplayCondition = data[0] & 255;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 1, data.length - 1));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                    } else {
                        log("Load EF_SPN: " + spn + " spnDisplayCondition: " + this.mSpnDisplayCondition);
                        oppoSetSimSpn(spn);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_CPHS) {
                    this.mFh.loadEFTransparent(IccConstants.EF_SPN_CPHS, obtainMessage(12));
                    this.mRecordsToLoad++;
                    this.mSpnDisplayCondition = -1;
                    break;
                }
                break;
            case 3:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                } else {
                    data = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 0, data.length));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_CPHS: " + spn);
                        oppoSetSimSpn(spn);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS) {
                    this.mFh.loadEFTransparent(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(12));
                    this.mRecordsToLoad++;
                    break;
                }
                break;
            case 4:
                if (ar == null || ar.exception != null) {
                    setServiceProviderName(null);
                    oppoSetSimSpn(this.mSpn);
                    log("No SPN loaded in either CHPS or 3GPP");
                } else {
                    data = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 0, data.length));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        log("No SPN loaded in either CHPS or 3GPP");
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_SHORT_CPHS: " + spn);
                        oppoSetSimSpn(spn);
                    }
                }
                this.mSpnState = GetSpnFsmState.IDLE;
                break;
            default:
                this.mSpnState = GetSpnFsmState.IDLE;
                break;
        }
    }

    private void parseEfSpdi(byte[] data) {
        SimTlv tlv = new SimTlv(data, 0, data.length);
        byte[] plmnEntries = null;
        while (tlv.isValidObject()) {
            if (tlv.getTag() == 163) {
                tlv = new SimTlv(tlv.getData(), 0, tlv.getData().length);
            }
            if (tlv.getTag() == 128) {
                plmnEntries = tlv.getData();
                break;
            }
            tlv.nextObject();
        }
        if (plmnEntries != null) {
            this.mSpdiNetworks = new ArrayList(plmnEntries.length / 3);
            for (int i = 0; i + 2 < plmnEntries.length; i += 3) {
                String plmnCode = IccUtils.bcdToString(plmnEntries, i, 3);
                if (plmnCode.length() >= 5) {
                    log("EF_SPDI network: " + plmnCode);
                    this.mSpdiNetworks.add(plmnCode);
                }
            }
        }
    }

    private String[] parseBcdPlmnList(byte[] data, String description) {
        log("Received " + description + " PLMNs, raw=" + IccUtils.bytesToHexString(data));
        if (data.length == 0 || data.length % 3 != 0) {
            loge("Received invalid " + description + " PLMN list");
            return null;
        }
        int numPlmns = data.length / 3;
        String[] ret = new String[numPlmns];
        for (int i = 0; i < numPlmns; i++) {
            ret[i] = IccUtils.bcdPlmnToString(data, i * 3);
        }
        return ret;
    }

    private boolean isCphsMailboxEnabled() {
        boolean z = true;
        if (this.mCphsInfo == null) {
            return false;
        }
        if ((this.mCphsInfo[1] & 48) != 48) {
            z = false;
        }
        return z;
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, "[SIMRecords] " + s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, "[SIMRecords] " + s);
    }

    protected void logw(String s, Throwable tr) {
        Rlog.w(LOG_TAG, "[SIMRecords] " + s, tr);
    }

    protected void logv(String s) {
        Rlog.v(LOG_TAG, "[SIMRecords] " + s);
    }

    public boolean isCspPlmnEnabled() {
        return this.mCspPlmnEnabled;
    }

    private void handleEfCspData(byte[] data) {
        int usedCspGroups = data.length / 2;
        this.mCspPlmnEnabled = true;
        for (int i = 0; i < usedCspGroups; i++) {
            if (data[i * 2] == (byte) -64) {
                log("[CSP] found ValueAddedServicesGroup, value " + data[(i * 2) + 1]);
                if ((data[(i * 2) + 1] & 128) == 128) {
                    this.mCspPlmnEnabled = true;
                } else {
                    this.mCspPlmnEnabled = false;
                    log("[CSP] Set Automatic Network Selection");
                    this.mNetworkSelectionModeAutomaticRegistrants.notifyRegistrants();
                }
                return;
            }
        }
        log("[CSP] Value Added Service Group (0xC0), not found!");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SIMRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.println(" mVmConfig=" + this.mVmConfig);
        pw.println(" mSpnOverride=" + this.mSpnOverride);
        pw.println(" mCallForwardingStatus=" + this.mCallForwardingStatus);
        pw.println(" mSpnState=" + this.mSpnState);
        pw.println(" mCphsInfo=" + this.mCphsInfo);
        pw.println(" mCspPlmnEnabled=" + this.mCspPlmnEnabled);
        pw.println(" mEfMWIS[]=" + Arrays.toString(this.mEfMWIS));
        pw.println(" mEfCPHS_MWI[]=" + Arrays.toString(this.mEfCPHS_MWI));
        pw.println(" mEfCff[]=" + Arrays.toString(this.mEfCff));
        pw.println(" mEfCfis[]=" + Arrays.toString(this.mEfCfis));
        pw.println(" mSpnDisplayCondition=" + this.mSpnDisplayCondition);
        pw.println(" mSpdiNetworks[]=" + this.mSpdiNetworks);
        pw.println(" mPnnHomeName=" + this.mPnnHomeName);
        pw.println(" mUsimServiceTable=" + this.mUsimServiceTable);
        pw.println(" mGid1=" + this.mGid1);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeGid1=" + (this.mFakeGid1 != null ? this.mFakeGid1 : "null"));
        }
        pw.println(" mGid2=" + this.mGid2);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeGid2=" + (this.mFakeGid2 != null ? this.mFakeGid2 : "null"));
        }
        pw.println(" mPlmnActRecords[]=" + Arrays.toString(this.mPlmnActRecords));
        pw.println(" mOplmnActRecords[]=" + Arrays.toString(this.mOplmnActRecords));
        pw.println(" mHplmnActRecords[]=" + Arrays.toString(this.mHplmnActRecords));
        pw.println(" mFplmns[]=" + Arrays.toString(this.mFplmns));
        pw.println(" mEhplmns[]=" + Arrays.toString(this.mEhplmns));
        pw.flush();
    }

    public int getSpnDisplayCondition() {
        return this.mSpnDisplayCondition;
    }

    public int getplmn(byte data0, byte data1, byte data2) {
        int mnc_digit_1 = data2 & 15;
        int mnc_digit_2 = (data2 >> 4) & 15;
        int mnc_digit_3 = (data1 >> 4) & 15;
        int mcc = (((data0 & 15) * 100) + (((data0 >> 4) & 15) * 10)) + (data1 & 15);
        if (mnc_digit_3 == 15) {
            return (mcc * 100) + ((mnc_digit_1 * 10) + mnc_digit_2);
        }
        return (mcc * 1000) + (((mnc_digit_1 * 100) + (mnc_digit_2 * 10)) + mnc_digit_3);
    }

    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i = (byte) (i - 1)) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    private Object responseNetworkInfoWithActs(int fileid, byte[] data) {
        log("responseNetworkInfoWithActs enter fileid:" + fileid);
        int offset = 5;
        if (fileid == 28464) {
            offset = 3;
        }
        this.mPlmnNumber = data.length / offset;
        log("responseNetworkInfoWithActs mPlmnNumber:" + this.mPlmnNumber);
        this.mPlmn = new int[this.mPlmnNumber];
        this.mTech = new int[this.mPlmnNumber];
        this.mOperatorAlphaName = new String[this.mPlmnNumber];
        this.mOperatorNumeric = new String[this.mPlmnNumber];
        byte[] mTechBit1 = new byte[8];
        byte[] mTechBit2 = new byte[8];
        this.mReadBuffer = new byte[data.length];
        this.mReadBuffer = data;
        this.mUsedPlmnNumber = 0;
        int i = 0;
        while (i < this.mPlmnNumber) {
            if (data[i * offset] == (byte) -1 && data[(i * offset) + 1] == (byte) -1 && data[(i * offset) + 2] == (byte) -1) {
                this.mUsedPlmnNumber = i;
                log("responseNetworkInfoWithActs mccmnc is FFFFFF ,then break ============mUsedPlmnNumber:" + this.mUsedPlmnNumber);
                break;
            }
            this.mPlmn[i] = getplmn(data[i * offset], data[(i * offset) + 1], data[(i * offset) + 2]);
            this.mOperatorNumeric[i] = Integer.toString(this.mPlmn[i]);
            log("responseNetworkInfoWithActs plmn:" + this.mOperatorNumeric[i]);
            this.mOperatorAlphaName[i] = oppoGeOperatorByPlmn(this.mContext, this.mOperatorNumeric[i]);
            log("responseNetworkInfoWithActs plmn name:" + this.mOperatorAlphaName[i]);
            this.mTech[i] = 0;
            this.mUsedPlmnNumber++;
            if (fileid != 28464) {
                mTechBit1 = getBooleanArray(data[(i * offset) + 3]);
                mTechBit2 = getBooleanArray(data[(i * offset) + 4]);
                if ((mTechBit1[0] == (byte) 1 || mTechBit1[1] == (byte) 1) && (mTechBit2[0] == (byte) 1 || mTechBit2[1] == (byte) 1)) {
                    log("responseNetworkInfoWithActs plmn:[" + i + "]:" + this.mPlmn[i] + "        tech is gsm and utran  ");
                    this.mTech[i] = 0;
                } else if (mTechBit1[0] == (byte) 1) {
                    log("responseNetworkInfoWithActs plmn:[" + i + "]:" + this.mPlmn[i] + "        tech is UTRAN  ");
                    this.mTech[i] = 3;
                } else if (mTechBit1[1] == (byte) 1) {
                    log("responseNetworkInfoWithActs plmn:[" + i + "]:" + this.mPlmn[i] + "        tech is E-UTRAN  ");
                    this.mTech[i] = 4;
                } else if (mTechBit2[0] == (byte) 1 || mTechBit2[1] == (byte) 1) {
                    log("responseNetworkInfoWithActs plmn:[" + i + "]:" + this.mPlmn[i] + "    tech is gsm  ");
                    this.mTech[i] = 1;
                } else if (mTechBit2[2] == (byte) 1 || mTechBit2[3] == (byte) 1) {
                    log("responseNetworkInfoWithActs plmn:[" + i + "]:" + this.mPlmn[i] + "        tech is cdma  ");
                    this.mTech[i] = 2;
                }
            }
            i++;
        }
        ArrayList<NetworkInfoWithAcT> ret = new ArrayList(this.mUsedPlmnNumber);
        for (i = 0; i < this.mUsedPlmnNumber; i++) {
            if (this.mOperatorNumeric[i] != null) {
                log("responseNetworkInfoWithActs  add mOperatorAlphaName" + this.mOperatorAlphaName[i]);
                ret.add(new NetworkInfoWithAcT(this.mOperatorAlphaName[i], this.mOperatorNumeric[i], this.mTech[i], i));
            } else {
                log("responseNetworkInfoWithActs: invalid oper. i is " + i);
            }
        }
        return ret;
    }

    private void handleEfPOLResponse(int fileid, byte[] data, Message msg) {
        log("wjp_pol handle response============");
        AsyncResult.forMessage(msg, responseNetworkInfoWithActs(fileid, data), null);
        msg.sendToTarget();
    }

    public void getPreferedOperatorList(Message onComplete, ServiceStateTracker msst) {
        log("wjp_pol simrecord getPreferedOperatorList ============");
        this.mSST = msst;
        this.onCompleteMsg = onComplete;
        this.mPlmnNumber = 0;
        this.mUsedPlmnNumber = 0;
        this.mFh.loadEFTransparent(28512, obtainMessage(99, 28512, 0, onComplete));
    }

    public byte[] formPlmnToByte(String plmn) {
        boolean mnc_includes_pcs_digit;
        int mcc;
        int mnc;
        int mnc_digit_1;
        int mnc_digit_2;
        int mnc_digit_3;
        byte[] ret = new byte[3];
        log("wjp_pol formPlmnToByte plmn:" + plmn);
        int plmnvalue = Integer.parseInt(plmn);
        if (plmnvalue > 99999) {
            log("wjp_pol mnc_includes_pcs_digit true");
            mnc_includes_pcs_digit = true;
        } else {
            log("wjp_pol mnc_includes_pcs_digit false");
            mnc_includes_pcs_digit = false;
        }
        if (mnc_includes_pcs_digit) {
            mcc = plmnvalue / 1000;
            mnc = plmnvalue - (mcc * 1000);
        } else {
            mcc = plmnvalue / 100;
            mnc = plmnvalue - (mcc * 100);
        }
        log("wjp_pol mcc:" + mcc + "   mnc" + mnc);
        int mcc_digit_1 = mcc / 100;
        int mcc_digit_2 = (mcc - (mcc_digit_1 * 100)) / 10;
        int mcc_digit_3 = (mcc - (mcc_digit_1 * 100)) - (mcc_digit_2 * 10);
        if (mnc_includes_pcs_digit) {
            mnc_digit_1 = mnc / 100;
            mnc_digit_2 = (mnc - (mnc_digit_1 * 100)) / 10;
            mnc_digit_3 = (mnc - (mnc_digit_1 * 100)) - (mnc_digit_2 * 10);
        } else {
            mnc_digit_1 = mnc / 10;
            mnc_digit_2 = mnc - (mnc_digit_1 * 10);
            mnc_digit_3 = 15;
        }
        log("wjp_pol mcc_digit_1:" + mcc_digit_1 + "   mcc_digit_2:" + mcc_digit_2 + "   mcc_digit_3:" + mcc_digit_3);
        log("wjp_pol mnc_digit_1:" + mnc_digit_1 + "   mnc_digit_2:" + mnc_digit_2 + "   mnc_digit_3:" + mnc_digit_3);
        ret[0] = (byte) ((mcc_digit_2 << 4) + mcc_digit_1);
        ret[1] = (byte) ((mnc_digit_3 << 4) + mcc_digit_3);
        ret[2] = (byte) ((mnc_digit_2 << 4) + mnc_digit_1);
        log("wjp_pol ret[0]:" + ret[0] + "   ret[1]:" + ret[1] + "   ret[2]:" + ret[2]);
        return ret;
    }

    public byte[] formRatToByte(int rat) {
        log("wjp_pol formRatToByte rat:" + rat);
        byte[] ret = new byte[2];
        if (rat == 0) {
            ret[0] = (byte) -64;
            ret[1] = Byte.MIN_VALUE;
            log("wjp_pol gsm+td+lte rat:" + rat);
        } else if (rat == 1) {
            ret[0] = (byte) 0;
            ret[1] = Byte.MIN_VALUE;
            log("wjp_pol gsm rat:" + rat);
        } else if (rat == 3) {
            ret[0] = Byte.MIN_VALUE;
            ret[1] = (byte) 0;
            log("wjp_pol td rat:" + rat);
        } else if (rat == 4) {
            ret[0] = (byte) 64;
            ret[1] = (byte) 0;
            log("wjp_pol lte rat:" + rat);
        } else {
            ret[0] = (byte) 0;
            ret[1] = (byte) 0;
            log("wjp_pol unknow rat:" + rat);
        }
        return ret;
    }

    public void setPOLEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
        log("wjp_pol simrecord setPOLEntry ============");
        String plmn = networkWithAct.getOperatorNumeric();
        int act = networkWithAct.getAccessTechnology();
        int priority = networkWithAct.getPriority();
        this.mWriteBuffer = new byte[(this.mPlmnNumber * 5)];
        this.mWriteBuffer = this.mReadBuffer;
        if (priority >= this.mPlmnNumber) {
            onComplete.sendToTarget();
            return;
        }
        boolean bUsim = this.mApptype == AppType.APPTYPE_USIM;
        log("setPOLEntry bUsim: " + bUsim);
        int offset = 5;
        if (!bUsim) {
            offset = 3;
        }
        if (plmn == null) {
            log("wjp_pol  setPOLEntry plmn is null , delete============");
            this.mWriteBuffer[(this.mUsedPlmnNumber - 1) * offset] = (byte) -1;
            this.mWriteBuffer[((this.mUsedPlmnNumber - 1) * offset) + 1] = (byte) -1;
            this.mWriteBuffer[((this.mUsedPlmnNumber - 1) * offset) + 2] = (byte) -1;
            if (bUsim) {
                this.mWriteBuffer[((this.mUsedPlmnNumber - 1) * offset) + 3] = (byte) 0;
                this.mWriteBuffer[((this.mUsedPlmnNumber - 1) * offset) + 4] = (byte) 0;
            }
        } else {
            change = new byte[5];
            byte[] bplmn = new byte[3];
            byte[] brat = new byte[2];
            bplmn = formPlmnToByte(plmn);
            brat = formRatToByte(act);
            this.mWriteBuffer[priority * offset] = bplmn[0];
            this.mWriteBuffer[(priority * offset) + 1] = bplmn[1];
            this.mWriteBuffer[(priority * offset) + 2] = bplmn[2];
            if (bUsim) {
                this.mWriteBuffer[(priority * offset) + 3] = brat[0];
                this.mWriteBuffer[(priority * offset) + 4] = brat[1];
            }
        }
        if (SystemProperties.get("ro.oppo.version", "CN").equals("US")) {
            log("setPOLEntry isLastItem: " + networkWithAct.isLastItem());
            if (!networkWithAct.isLastItem()) {
                onComplete.sendToTarget();
                return;
            }
        }
        if (bUsim) {
            this.mFh.updateEFTransparent(28512, this.mWriteBuffer, obtainMessage(88, onComplete));
        } else {
            this.mFh.updateEFTransparent(28464, this.mWriteBuffer, obtainMessage(88, onComplete));
        }
    }

    private void handlePlmnListData(Message response, byte[] result, Throwable ex) {
        AsyncResult.forMessage(response, result, ex);
        response.sendToTarget();
    }

    private void fetchCdmaPrl() {
        UiccCard card = UiccController.getInstance().getUiccCard(0);
        if (card != null) {
            int numApps = card.getNumApplications();
            UiccCardApplication app = null;
            AppType type = AppType.APPTYPE_UNKNOWN;
            for (int i = 0; i < numApps; i++) {
                app = card.getApplicationIndex(i);
                if (app != null) {
                    type = app.getType();
                    log("fetchCdmaPrl type=" + type);
                    String path;
                    if (type == AppType.APPTYPE_RUIM) {
                        path = "3F007F25";
                        break;
                    } else if (type == AppType.APPTYPE_CSIM) {
                        path = "3F007FFF";
                        break;
                    }
                }
            }
            if (app != null && (type == AppType.APPTYPE_RUIM || type == AppType.APPTYPE_CSIM)) {
                IccFileHandler handler = app.getIccFileHandler();
                if (handler != null) {
                    handler.loadEFTransparent(IccConstants.EF_CSIM_EPRL, 4, obtainMessage(100, new EfCsimEprlLoaded(this, null)));
                }
            }
        }
    }

    private void onGetCSimEprlDone(AsyncResult ar) {
        byte[] data = ar.result;
        log("CSIM_EPRL=" + IccUtils.bytesToHexString(data));
        if (data.length > 3) {
            this.mPrlVersion = Integer.toString(((data[2] & 255) << 8) | (data[3] & 255));
        }
        log("CSIM PRL version=" + this.mPrlVersion);
    }

    public String getPrlVersion() {
        return this.mPrlVersion;
    }

    private void setSimState(int state) {
        this.mSimState = state;
    }

    private int getSimState() {
        log("getSimState:" + this.mSimState);
        return this.mSimState;
    }

    private void oppoSetSimSpn(String spn) {
        if (SubscriptionController.getInstance().isHasSoftSimCard() && SubscriptionController.getInstance().getSoftSimCardSlotId() == this.mParentApp.getPhoneId()) {
            this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
            return;
        }
        log("Load EF_SPN oppo edit for not softsim");
        if (this.mMncLength == -1 || this.mMncLength == 0) {
            this.isNeedSetSpnLater = true;
            log("can not get operatorNumeric due to mMncLength:" + this.mMncLength);
        } else {
            this.isNeedSetSpnLater = false;
            log("mMncLength is valid,no need set it later");
        }
        setSpnFromConfig(getOperatorNumeric());
    }

    public String getPnnFromSimCard() {
        return this.mPnnHomeName;
    }

    private void parseEFopl(ArrayList messages) {
        int count = messages.size();
        log("parseEFopl(): opl has " + count + " records");
        this.mOperatorList = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            byte[] data = (byte[]) messages.get(i);
            log("parseEFopl(): opl record " + i + " content is " + IccUtils.bytesToHexString(data));
            OplRecord oplRec = new OplRecord();
            oplRec.sPlmn = IccUtils.parsePlmnToStringForEfOpl(data, 0, 3);
            log("parseEFopl(): opl sPlmn = " + oplRec.sPlmn);
            oplRec.nMinLAC = Integer.parseInt(IccUtils.bytesToHexString(new byte[]{data[3], data[4]}), 16);
            log("parseEFopl(): opl nMinLAC = " + oplRec.nMinLAC);
            oplRec.nMaxLAC = Integer.parseInt(IccUtils.bytesToHexString(new byte[]{data[5], data[6]}), 16);
            log("parseEFopl(): opl nMaxLAC = " + oplRec.nMaxLAC);
            oplRec.nPnnIndex = Integer.parseInt(IccUtils.bytesToHexString(data).substring(14), 16);
            log("parseEFopl(): opl nPnnIndex = " + oplRec.nPnnIndex);
            this.mOperatorList.add(oplRec);
        }
    }

    public String getEonsIfExist(String plmn, int nLac, boolean bLongNameRequired) {
        log("EONS getEonsIfExist: plmn is " + plmn + " nLac is " + nLac + " bLongNameRequired: " + bLongNameRequired);
        if (plmn == null || this.mPnnNetworkNames == null || this.mPnnNetworkNames.size() == 0) {
            return null;
        }
        int nPnnIndex = -1;
        boolean isHPLMN = isHPlmn(plmn);
        if (this.mOperatorList != null) {
            int i = 0;
            while (i < this.mOperatorList.size()) {
                OplRecord oplRec = (OplRecord) this.mOperatorList.get(i);
                log("EONS getEonsIfExist: record number is " + i + " sPlmn: " + oplRec.sPlmn + " nMinLAC: " + oplRec.nMinLAC + " nMaxLAC: " + oplRec.nMaxLAC + " PnnIndex " + oplRec.nPnnIndex);
                if (!isMatchingPlmnForEfOpl(oplRec.sPlmn, plmn) || (!(oplRec.nMinLAC == 0 && oplRec.nMaxLAC == 65534) && (oplRec.nMinLAC > nLac || oplRec.nMaxLAC < nLac))) {
                    i++;
                } else {
                    log("EONS getEonsIfExist: find it in EF_OPL");
                    if (oplRec.nPnnIndex == 0) {
                        log("EONS getEonsIfExist: oplRec.nPnnIndex is 0 indicates that the name is to be taken from other sources");
                        return null;
                    }
                    nPnnIndex = oplRec.nPnnIndex;
                }
            }
        } else if (isHPLMN) {
            log("EONS getEonsIfExist: Plmn is HPLMN, but no mOperatorList, return PNN's first record");
            nPnnIndex = 1;
        } else {
            log("EONS getEonsIfExist: Plmn is not HPLMN, and no mOperatorList, return null");
            return null;
        }
        if (nPnnIndex == -1 && isHPLMN && this.mOperatorList.size() == 1) {
            log("EONS getEonsIfExist: not find it in EF_OPL, but Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else if (nPnnIndex > 1 && nPnnIndex > this.mPnnNetworkNames.size() && isHPLMN) {
            log("EONS getEonsIfExist: find it in EF_OPL, but index in EF_OPL > EF_PNN list length & Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else if (nPnnIndex > 1 && nPnnIndex > this.mPnnNetworkNames.size() && (isHPLMN ^ 1) != 0) {
            log("EONS getEonsIfExist: find it in EF_OPL, but index in EF_OPL > EF_PNN list length & Plmn is not HPLMN, return PNN's first record");
            nPnnIndex = -1;
        }
        String sEons = null;
        if (nPnnIndex >= 1) {
            OperatorName opName = (OperatorName) this.mPnnNetworkNames.get(nPnnIndex - 1);
            if (bLongNameRequired) {
                if (opName.sFullName != null) {
                    sEons = new String(opName.sFullName);
                } else if (opName.sShortName != null) {
                    sEons = new String(opName.sShortName);
                }
            } else if (!bLongNameRequired) {
                if (opName.sShortName != null) {
                    sEons = new String(opName.sShortName);
                } else if (opName.sFullName != null) {
                    sEons = new String(opName.sFullName);
                }
            }
        }
        log("EONS getEonsIfExist: sEons is " + sEons);
        return sEons;
    }

    public boolean isHPlmn(String plmn) {
        ServiceStateTracker sst = this.mPhone != null ? this.mPhone.getServiceStateTracker() : null;
        if (sst != null) {
            return sst.isHPlmn(plmn);
        }
        log("can't get sst");
        return false;
    }

    private boolean isMatchingPlmnForEfOpl(String simPlmn, String bcchPlmn) {
        if (simPlmn == null || simPlmn.equals(SpnOverride.MVNO_TYPE_NONE) || bcchPlmn == null || bcchPlmn.equals(SpnOverride.MVNO_TYPE_NONE)) {
            return false;
        }
        log("isMatchingPlmnForEfOpl(): simPlmn = " + simPlmn + ", bcchPlmn = " + bcchPlmn);
        int simPlmnLen = simPlmn.length();
        int bcchPlmnLen = bcchPlmn.length();
        if (simPlmnLen < 5 || bcchPlmnLen < 5) {
            return false;
        }
        int i = 0;
        while (i < 5) {
            if (simPlmn.charAt(i) != 'd' && simPlmn.charAt(i) != bcchPlmn.charAt(i)) {
                return false;
            }
            i++;
        }
        if (simPlmnLen == 6 && bcchPlmnLen == 6) {
            return simPlmn.charAt(5) == 'd' || simPlmn.charAt(5) == bcchPlmn.charAt(5);
        } else {
            if (bcchPlmnLen != 6 || bcchPlmn.charAt(5) == '0' || bcchPlmn.charAt(5) == 'd') {
                return simPlmnLen != 6 || simPlmn.charAt(5) == '0' || simPlmn.charAt(5) == 'd';
            } else {
                return false;
            }
        }
    }

    public String getSIMCPHSOns() {
        if (this.cphsOnsl != null) {
            return this.cphsOnsl;
        }
        return this.cphsOnss;
    }

    private void fetchCPHSOns() {
        log("fetchCPHSOns()");
        this.cphsOnsl = null;
        this.cphsOnss = null;
        this.mFh.loadEFTransparent(IccConstants.EF_SPN_CPHS, obtainMessage(502));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(503));
        this.mRecordsToLoad++;
    }

    public String getSpNameInEfSpn() {
        log("getSpNameInEfSpn(): " + this.mSpNameInEfSpn);
        return this.mSpNameInEfSpn;
    }

    public String isOperatorMvnoForImsi() {
        String imsiPattern = SpnOverride.getInstance().isOperatorMvnoForImsi(getOperatorNumeric(), getIMSI());
        String mccmnc = getOperatorNumeric();
        log("isOperatorMvnoForImsi(), imsiPattern: " + imsiPattern + ", mccmnc: " + mccmnc);
        if (imsiPattern == null || mccmnc == null) {
            return null;
        }
        String result = imsiPattern.substring(mccmnc.length(), imsiPattern.length());
        log("isOperatorMvnoForImsi(): " + result);
        return result;
    }

    public String isOperatorMvnoForEfPnn() {
        String MCCMNC = getOperatorNumeric();
        String PNN = getFirstFullNameInEfPnn();
        log("isOperatorMvnoForEfPnn(): mccmnc = " + MCCMNC + ", pnn = " + PNN);
        if (SpnOverride.getInstance().getSpnByEfPnn(MCCMNC, PNN) != null) {
            return PNN;
        }
        return null;
    }

    public String getFirstFullNameInEfPnn() {
        if (this.mPnnNetworkNames == null || this.mPnnNetworkNames.size() == 0) {
            log("getFirstFullNameInEfPnn(): empty");
            return null;
        }
        OperatorName opName = (OperatorName) this.mPnnNetworkNames.get(0);
        log("getFirstFullNameInEfPnn(): first fullname: " + opName.sFullName);
        if (opName.sFullName != null) {
            return new String(opName.sFullName);
        }
        return null;
    }

    private void fetchPnnAndOpl() {
        log("fetchPnnAndOpl()");
        boolean bPnnActive = false;
        boolean bOplActive = false;
        if (this.mEfSST != null) {
            if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                if (this.mEfSST.length >= 6) {
                    bPnnActive = (this.mEfSST[5] & 16) == 16;
                    if (bPnnActive) {
                        bOplActive = (this.mEfSST[5] & 32) == 32;
                    }
                }
            } else if (this.mEfSST.length >= 13) {
                bPnnActive = (this.mEfSST[12] & 48) == 48;
                if (bPnnActive) {
                    bOplActive = (this.mEfSST[12] & 192) == 192;
                }
            }
        }
        log("bPnnActive = " + bPnnActive + ", bOplActive = " + bOplActive);
        boolean pnnDebug = SystemProperties.getBoolean("persist.sys.pnn.debug", false);
        if (this.mFh == null) {
            return;
        }
        if (bPnnActive || pnnDebug) {
            log("start get pnn all");
            this.mFh.loadEFLinearFixedAll(IccConstants.EF_PNN, obtainMessage(EVENT_GET_ALL_PNN_DONE));
            this.mRecordsToLoad++;
            if (bOplActive) {
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_OPL, obtainMessage(501));
                this.mRecordsToLoad++;
            }
        }
    }

    public boolean isSimLoadedCompleted() {
        return this.mIsSimLoaded;
    }

    private void parseEFpnn(ArrayList messages) {
        int count = messages.size();
        log("parseEFpnn(): pnn has " + count + " records");
        this.mPnnNetworkNames = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            byte[] data = (byte[]) messages.get(i);
            log("parseEFpnn(): pnn record " + i + " content is " + IccUtils.bytesToHexString(data));
            SimTlv tlv = new SimTlv(data, 0, data.length);
            OperatorName opName = new OperatorName();
            while (tlv.isValidObject()) {
                if (tlv.getTag() == TAG_FULL_NETWORK_NAME) {
                    opName.sFullName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                    log("parseEFpnn(): pnn sFullName is " + opName.sFullName);
                } else if (tlv.getTag() == 69) {
                    opName.sShortName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                    log("parseEFpnn(): pnn sShortName is " + opName.sShortName);
                }
                tlv.nextObject();
            }
            this.mPnnNetworkNames.add(opName);
        }
    }

    public OperatorName getEFpnnNetworkNames(int index) {
        if (this.mPnnNetworkNames == null || index >= this.mPnnNetworkNames.size()) {
            return null;
        }
        return (OperatorName) this.mPnnNetworkNames.get(index);
    }
}
