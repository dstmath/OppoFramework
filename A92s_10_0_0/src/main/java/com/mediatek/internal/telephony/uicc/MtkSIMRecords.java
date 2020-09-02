package com.mediatek.internal.telephony.uicc;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordLoader;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.mediatek.internal.telephony.MtkIccUtils;
import com.mediatek.internal.telephony.MtkSubscriptionManager;
import com.mediatek.internal.telephony.OpTelephonyCustomizationFactoryBase;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.phb.MtkAdnRecordCache;
import com.mediatek.internal.telephony.ppl.PplControlData;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.internal.telephony.uicc.IccServiceInfo;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

public class MtkSIMRecords extends SIMRecords {
    public static final String ATT_OPID = "7";
    public static final String CRICKET_OPID = "145";
    public static final int EF_RAT_FOR_OTHER_CASE = 512;
    public static final int EF_RAT_NOT_EXIST_IN_USIM = 256;
    public static final int EF_RAT_UNDEFINED = -256;
    protected static final boolean ENGDEBUG = TextUtils.equals(Build.TYPE, "eng");
    private static final int EVENT_CFU_IND = 1021;
    private static final int EVENT_DELAYED_SEND_PHB_CHANGE = 1026;
    private static final int EVENT_DUAL_IMSI_READY = 1004;
    private static final int EVENT_EF_CSP_PLMN_MODE_BIT_CHANGED = 1013;
    private static final int EVENT_GET_ALL_OPL_DONE = 1008;
    private static final int EVENT_GET_ALL_PNN_DONE = 1028;
    private static final int EVENT_GET_CPHSONS_DONE = 1009;
    private static final int EVENT_GET_EF_ICCID_DONE = 1024;
    private static final int EVENT_GET_GBABP_DONE = 1019;
    private static final int EVENT_GET_GBANL_DONE = 1020;
    private static final int EVENT_GET_NEW_MSISDN_DONE = 1016;
    private static final int EVENT_GET_PSISMSC_DONE = 1017;
    private static final int EVENT_GET_RAT_DONE = 1014;
    private static final int EVENT_GET_SHORT_CPHSONS_DONE = 1010;
    private static final int EVENT_GET_SMSP_DONE = 1018;
    private static final int EVENT_IMSI_REFRESH_QUERY = 1022;
    private static final int EVENT_IMSI_REFRESH_QUERY_DONE = 1023;
    public static final int EVENT_MSISDN = 100;
    public static final int EVENT_OPL = 101;
    private static final int EVENT_PHB_READY = 1027;
    public static final int EVENT_PNN = 102;
    private static final int EVENT_QUERY_ICCID_DONE = 1011;
    private static final int EVENT_QUERY_ICCID_DONE_FOR_HOT_SWAP = 1015;
    private static final int EVENT_QUERY_MENU_TITLE_DONE = 1005;
    private static final int EVENT_RADIO_AVAILABLE = 1001;
    private static final int EVENT_RADIO_STATE_CHANGED = 1012;
    private static final int EVENT_RSU_SIM_LOCK_CHANGED = 1029;
    private static final int GSM_PHB_NOT_READY = 0;
    private static final int GSM_PHB_READY = 1;
    private static final String KEY_SIM_ID = "SIM_ID";
    private static final String[] LANGUAGE_CODE_FOR_LP = {"de", "en", "it", "fr", "es", "nl", "sv", "da", "pt", "fi", "no", "el", "tr", "hu", "pl", "", "cs", "he", "ar", "ru", "is", "", "", "", "", "", "", "", "", "", "", ""};
    protected static final String LOG_TAG_EX = "MtkSIMRecords";
    private static final int MTK_SIM_RECORD_EVENT_BASE = 1000;
    private static final String SIMRECORD_PROPERTY_RIL_PHB_READY = "vendor.gsm.sim.ril.phbready";
    static final String[] SIMRECORD_PROPERTY_RIL_PUK1 = {"vendor.gsm.sim.retry.puk1", "vendor.gsm.sim.retry.puk1.2", "vendor.gsm.sim.retry.puk1.3", "vendor.gsm.sim.retry.puk1.4"};
    protected static final boolean USERDEBUG = TextUtils.equals(Build.TYPE, DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER);
    private static final int[] simServiceNumber = {1, 17, 51, 52, 54, 55, 56, 0, 12, 3, 7, 0, 0};
    private static final int[] usimServiceNumber = {0, 19, 45, 46, 48, 49, 51, 71, 12, 2, 0, 42, 0};
    private String[] SIM_RECORDS_PROPERTY_MCC_MNC = {"vendor.gsm.ril.uicc.mccmnc", "vendor.gsm.ril.uicc.mccmnc.1", "vendor.gsm.ril.uicc.mccmnc.2", "vendor.gsm.ril.uicc.mccmnc.3"};
    String cphsOnsl;
    String cphsOnss;
    private int efLanguageToLoad = 0;
    private boolean hasQueryIccId;
    private int iccIdQueryState = -1;
    private boolean isDispose = false;
    private boolean isValidMBI = false;
    private byte[] mEfELP = null;
    private ArrayList<byte[]> mEfGbanlList;
    private byte[] mEfPsismsc = null;
    private byte[] mEfRat = null;
    private boolean mEfRatLoaded = false;
    private byte[] mEfSST = null;
    private byte[] mEfSmsp = null;
    private String mGbabp;
    private String[] mGbanl;
    private boolean mIsPhbEfResetDone = false;
    private String mMenuTitleFromEf = null;
    private IMtkSimHandler mMtkSimHandler = null;
    protected String mOldMccMnc = "";
    private String mOldOperatorDefaultName = null;
    private ArrayList<OplRecord> mOperatorList = null;
    /* access modifiers changed from: private */
    public boolean mPendingPhbNotify = false;
    /* access modifiers changed from: private */
    public boolean mPhbReady = false;
    private PhbBroadCastReceiver mPhbReceiver;
    /* access modifiers changed from: private */
    public boolean mPhbWaitSub = false;
    /* access modifiers changed from: private */
    public Phone mPhone;
    private ArrayList<OperatorName> mPnnNetworkNames = null;
    private boolean mReadingOpl = false;
    private String mSimImsi = null;
    private BroadcastReceiver mSimReceiver;
    protected int mSlotId;
    private String mSpNameInEfSpn = null;
    private MtkSpnOverride mSpnOverride = null;
    private int mSubId = -1;
    private OpTelephonyCustomizationFactoryBase mTelephonyCustomizationFactory = null;
    private UiccCard mUiccCard;
    private UiccController mUiccController;

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

    public MtkSIMRecords(MtkUiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        mtkLog("MtkSIMRecords constructor");
        this.mSlotId = app.getPhoneId();
        this.mUiccController = UiccController.getInstance();
        this.mUiccCard = this.mUiccController.getUiccCard(this.mSlotId);
        mtkLog("mUiccCard Instance = " + this.mUiccCard);
        this.mPhone = PhoneFactory.getPhone(app.getPhoneId());
        this.mSpnOverride = MtkSpnOverride.getInstance();
        this.cphsOnsl = null;
        this.cphsOnss = null;
        this.hasQueryIccId = false;
        this.mCi.registerForCallForwardingInfo(this, EVENT_CFU_IND, null);
        this.mCi.registerForRadioStateChanged(this, 1012, (Object) null);
        this.mCi.registerForAvailable(this, 1001, (Object) null);
        this.mCi.registerForImsiRefreshDone(this, EVENT_IMSI_REFRESH_QUERY, null);
        this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(c);
        OpTelephonyCustomizationFactoryBase opTelephonyCustomizationFactoryBase = this.mTelephonyCustomizationFactory;
        if (opTelephonyCustomizationFactoryBase != null) {
            this.mMtkSimHandler = opTelephonyCustomizationFactoryBase.makeMtkSimHandler(c, ci);
            this.mMtkSimHandler.setPhoneId(this.mSlotId);
        }
        this.mSimReceiver = new SIMBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mSimReceiver, filter);
        this.mAdnCache = new MtkAdnRecordCache(this.mFh, ci, app);
        this.mCi.registerForPhbReady(this, EVENT_PHB_READY, null);
        this.mPhbReceiver = new PhbBroadCastReceiver();
        IntentFilter phbFilter = new IntentFilter();
        phbFilter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        phbFilter.addAction("android.intent.action.RADIO_TECHNOLOGY");
        phbFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mPhbReceiver, phbFilter);
        mtkLog("SIMRecords updateIccRecords");
        Phone phone = this.mPhone;
        if (!(phone == null || phone.getIccPhoneBookInterfaceManager() == null)) {
            this.mPhone.getIccPhoneBookInterfaceManager().updateIccRecords(this);
        }
        if (isPhbReady()) {
            mtkLog("Phonebook is ready.");
            this.mPhbReady = true;
            broadcastPhbStateChangedIntent(this.mPhbReady, false);
        }
        this.mCi.registerForRsuSimLockChanged(this, EVENT_RSU_SIM_LOCK_CHANGED, null);
    }

    public void dispose() {
        mtkLog("Disposing MtkSIMRecords this=" + this);
        this.isDispose = true;
        IMtkSimHandler iMtkSimHandler = this.mMtkSimHandler;
        if (iMtkSimHandler != null) {
            iMtkSimHandler.dispose();
        }
        this.mCi.unregisterForCallForwardingInfo(this);
        this.mCi.unregisterForRadioStateChanged(this);
        this.mContext.unregisterReceiver(this.mSimReceiver);
        this.mIccId = null;
        this.mImsi = null;
        this.mCi.unregisterForPhbReady(this);
        this.mContext.unregisterReceiver(this.mPhbReceiver);
        this.mPhbWaitSub = false;
        if (this.mPhbReady || this.mPendingPhbNotify) {
            mtkLog("MtkSIMRecords Disposing  set PHB unready mPendingPhbNotify=" + this.mPendingPhbNotify + ", mPhbReady=" + this.mPhbReady);
            this.mPhbReady = false;
            this.mPendingPhbNotify = false;
            broadcastPhbStateChangedIntent(this.mPhbReady, false);
        }
        this.mPhone.getIccPhoneBookInterfaceManager().dispose();
        this.mCallForwardingStatus = 0;
        new Thread(new Runnable() {
            /* class com.mediatek.internal.telephony.uicc.MtkSIMRecords.AnonymousClass1 */

            public void run() {
                MtkSIMRecords.this.mPhone.notifyCallForwardingIndicatorWithoutCheckSimState();
            }
        }).start();
        this.mCi.unregisterForRsuSimLockChanged(this);
        MtkSIMRecords.super.dispose();
    }

    /* access modifiers changed from: protected */
    public void resetRecords() {
        MtkSIMRecords.super.resetRecords();
        setSystemProperty("vendor.gsm.sim.operator.default-name", null);
    }

    public boolean checkEfCfis() {
        boolean z = true;
        boolean isValid = this.mEfCfis != null && this.mEfCfis.length == 16;
        StringBuilder sb = new StringBuilder();
        sb.append("mEfCfis is null? = ");
        if (this.mEfCfis != null) {
            z = false;
        }
        sb.append(z);
        mtkLog(sb.toString());
        return isValid;
    }

    public String getVoiceMailNumber() {
        mtkLog("getVoiceMailNumber " + MtkIccUtilsEx.getPrintableString(this.mVoiceMailNum, 8));
        return MtkSIMRecords.super.getVoiceMailNumber();
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        mtkLog("setVoiceMailNumber, mIsVoiceMailFixed:" + this.mIsVoiceMailFixed + ", mMailboxIndex:" + this.mMailboxIndex + ", isCphsMailboxEnabled:" + isCphsMailboxEnabled() + ", alphaTag:" + alphaTag + ", voiceNumber:" + MtkIccUtilsEx.getPrintableString(voiceNumber, 8));
        MtkSIMRecords.super.setVoiceMailNumber(alphaTag, voiceNumber, onComplete);
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String dialNumber) {
        int i;
        Rlog.d("SIMRecords", "setVoiceCallForwardingFlag: " + enable);
        if (line == 1) {
            if (enable) {
                i = 1;
            } else {
                i = 0;
            }
            this.mCallForwardingStatus = i;
            mtkLog(" mRecordsEventsRegistrants: size=" + this.mRecordsEventsRegistrants.size());
            this.mRecordsEventsRegistrants.notifyResult(1);
            try {
                if (checkEfCfis()) {
                    if (enable) {
                        byte[] bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] | 1);
                    } else {
                        byte[] bArr2 = this.mEfCfis;
                        bArr2[1] = (byte) (bArr2[1] & 254);
                    }
                    mtkLog("setVoiceCallForwardingFlag: enable=" + enable + " mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                    if (enable && !TextUtils.isEmpty(dialNumber)) {
                        logv("EF_CFIS: updating cf number, " + Rlog.pii("SIMRecords", dialNumber));
                        byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(convertNumberIfContainsPrefix(dialNumber));
                        System.arraycopy(bcdNumber, 0, this.mEfCfis, 3, bcdNumber.length);
                        this.mEfCfis[2] = (byte) bcdNumber.length;
                        this.mEfCfis[14] = -1;
                        this.mEfCfis[15] = -1;
                    }
                    if (this.mFh != null) {
                        this.mFh.updateEFLinearFixed(28619, 1, this.mEfCfis, (String) null, obtainMessage(14, 28619));
                    } else {
                        log("setVoiceCallForwardingFlag: mFh is null, skip update EF_CFIS");
                    }
                } else {
                    mtkLog("setVoiceCallForwardingFlag: ignoring enable=" + enable + " invalid mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                }
                if (this.mEfCff != null) {
                    if (enable) {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 10);
                    } else {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 5);
                    }
                    if (this.mFh != null) {
                        this.mFh.updateEFTransparent(28435, this.mEfCff, obtainMessage(14, 28435));
                    } else {
                        log("setVoiceCallForwardingFlag: mFh is null, skip update EF_CFF_CPHS");
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving call forwarding flag to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    public String getSIMCPHSOns() {
        String str = this.cphsOnsl;
        if (str != null) {
            return str;
        }
        return this.cphsOnss;
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            if (msg.what != 90) {
                mtkLoge("Received message " + msg + "[" + msg.what + "]  while being destroyed. Ignoring.");
                return;
            }
            mtkLoge("Received message " + msg + "[" + msg.what + "]  while being destroyed. Keep going!");
        }
        try {
            int i = msg.what;
            boolean isSimLocked = true;
            if (i != 1) {
                if (i != 5) {
                    if (i == 1013) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar != null && ar.exception == null) {
                            processEfCspPlmnModeBitUrc(((int[]) ar.result)[0]);
                        }
                    } else if (i != 1014) {
                        switch (i) {
                            case 5:
                                break;
                            case 10:
                                isRecordLoadResponse = true;
                                AsyncResult ar2 = (AsyncResult) msg.obj;
                                if (ar2.exception == null) {
                                    AdnRecord adn = (AdnRecord) ar2.result;
                                    this.mMsisdn = adn.getNumber();
                                    this.mMsisdnTag = adn.getAlphaTag();
                                    this.mRecordsEventsRegistrants.notifyResult(100);
                                    mtkLog("MSISDN: " + MtkIccUtilsEx.getPrintableString(this.mMsisdn, 8));
                                    break;
                                } else {
                                    mtkLoge("Invalid or missing EF[MSISDN]");
                                    break;
                                }
                            case 12:
                                isRecordLoadResponse = true;
                                AsyncResult ar3 = (AsyncResult) msg.obj;
                                this.mSpnState = SIMRecords.GetSpnFsmState.IDLE;
                                if (ar3 != null && ar3.exception == null) {
                                    byte[] data = (byte[]) ar3.result;
                                    int displayCondition = data[0] & 255;
                                    this.mCarrierNameDisplayCondition = 0;
                                    if ((displayCondition & 1) == 1) {
                                        this.mCarrierNameDisplayCondition |= 1;
                                    }
                                    if ((displayCondition & 2) == 0) {
                                        this.mCarrierNameDisplayCondition |= 2;
                                    }
                                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 1, data.length - 1));
                                    this.mSpNameInEfSpn = getServiceProviderName();
                                    if (this.mSpNameInEfSpn != null && this.mSpNameInEfSpn.equals("")) {
                                        mtkLog("set mSpNameInEfSpn to null as parsing result is empty");
                                        this.mSpNameInEfSpn = null;
                                    }
                                    log("Load EF_SPN: " + getServiceProviderName() + " carrierNameDisplayCondition: " + this.mCarrierNameDisplayCondition);
                                    if (!OemConstant.EXP_VERSION) {
                                        if (this.mMncLength != 0 && this.mMncLength != -1 && SubscriptionManager.isUsimWithCsim(this.mSlotId) && OemConstant.isCtCard(this.mPhone)) {
                                            String spn = getServiceProviderName();
                                            String operName = MtkSpnOverride.getInstance().getSpnByEfSpn("20404", spn);
                                            mtkLog("SPN loaded, spn=" + spn + "   operName = " + operName);
                                            if (spn == null || spn.equals("") || !spn.equals(operName)) {
                                                updateConfiguration(this.mImsi.substring(0, this.mMncLength + 3));
                                                mtkLog("SPN loaded, update mccmnc =" + this.mImsi.substring(0, this.mMncLength + 3));
                                            } else {
                                                updateConfiguration("46011");
                                                mtkLog("SPN loaded, update 46011 to set language");
                                            }
                                        }
                                        setSpnFromConfig(getOperatorNumeric());
                                        break;
                                    } else {
                                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), getServiceProviderName());
                                        break;
                                    }
                                } else {
                                    mtkLoge("Read EF_SPN fail!");
                                    this.mCarrierNameDisplayCondition = 0;
                                    break;
                                }
                                break;
                            case 15:
                                isRecordLoadResponse = false;
                                AsyncResult ar4 = (AsyncResult) msg.obj;
                                if (ar4.exception == null) {
                                    parseEFpnn((ArrayList) ar4.result);
                                    if (!this.mReadingOpl) {
                                        this.mRecordsEventsRegistrants.notifyResult(101);
                                        break;
                                    }
                                }
                                break;
                            case 17:
                                isRecordLoadResponse = true;
                                AsyncResult ar5 = (AsyncResult) msg.obj;
                                byte[] data2 = (byte[]) ar5.result;
                                if (ar5.exception == null) {
                                    this.mUsimServiceTable = new UsimServiceTable(data2);
                                    mtkLog("SST: " + this.mUsimServiceTable);
                                    this.mEfSST = data2;
                                    break;
                                }
                                break;
                            case 26:
                                isRecordLoadResponse = true;
                                AsyncResult ar6 = (AsyncResult) msg.obj;
                                if (ar6.exception == null) {
                                    this.mCphsInfo = (byte[]) ar6.result;
                                    mtkLog("iCPHS: " + IccUtils.bytesToHexString(this.mCphsInfo));
                                    if (!this.isValidMBI && isCphsMailboxEnabled()) {
                                        this.mRecordsToLoad++;
                                        new AdnRecordLoader(this.mFh).loadFromEF(28439, 28490, 1, obtainMessage(11));
                                        break;
                                    }
                                }
                                break;
                            case IWorldPhone.EVENT_REG_SUSPENDED_1 /*{ENCODED_INT: 30}*/:
                                isRecordLoadResponse = false;
                                AsyncResult ar7 = (AsyncResult) msg.obj;
                                if (ar7.exception == null) {
                                    this.mMsisdn = this.mNewMsisdn;
                                    this.mMsisdnTag = this.mNewMsisdnTag;
                                    this.mRecordsEventsRegistrants.notifyResult(100);
                                    mtkLog("Success to update EF[MSISDN]");
                                }
                                if (ar7.userObj != null) {
                                    AsyncResult.forMessage((Message) ar7.userObj).exception = ar7.exception;
                                    ((Message) ar7.userObj).sendToTarget();
                                    break;
                                }
                                break;
                            case 258:
                                MtkSIMRecords.super.handleMessage(msg);
                                break;
                            case 1001:
                                this.mMsisdn = "";
                                this.mRecordsEventsRegistrants.notifyResult(100);
                                break;
                            case 1005:
                                mtkLog("[sume receive response message");
                                isRecordLoadResponse = true;
                                AsyncResult ar8 = (AsyncResult) msg.obj;
                                if (ar8 != null && ar8.exception == null) {
                                    byte[] data3 = (byte[]) ar8.result;
                                    if (data3 != null && data3.length >= 2) {
                                        int len = data3[1] & 255;
                                        mtkLog("[sume tag = " + (data3[0] & 255) + ", len = " + len);
                                        this.mMenuTitleFromEf = IccUtils.adnStringFieldToString(data3, 2, len);
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("[sume menu title is ");
                                        sb.append(this.mMenuTitleFromEf);
                                        mtkLog(sb.toString());
                                        break;
                                    }
                                } else {
                                    mtkLog("[sume null AsyncResult or exception.");
                                    this.mMenuTitleFromEf = null;
                                    break;
                                }
                                break;
                            default:
                                switch (i) {
                                    case 1008:
                                        isRecordLoadResponse = false;
                                        AsyncResult ar9 = (AsyncResult) msg.obj;
                                        if (ar9.exception == null) {
                                            parseEFopl((ArrayList) ar9.result);
                                            this.mRecordsEventsRegistrants.notifyResult(101);
                                            break;
                                        }
                                        break;
                                    case 1009:
                                        mtkLog("handleMessage (EVENT_GET_CPHSONS_DONE)");
                                        isRecordLoadResponse = false;
                                        AsyncResult ar10 = (AsyncResult) msg.obj;
                                        if (ar10 != null && ar10.exception == null) {
                                            byte[] data4 = (byte[]) ar10.result;
                                            this.cphsOnsl = IccUtils.adnStringFieldToString(data4, 0, data4.length);
                                            mtkLog("Load EF_SPN_CPHS: " + this.cphsOnsl);
                                            break;
                                        }
                                    case 1010:
                                        mtkLog("handleMessage (EVENT_GET_SHORT_CPHSONS_DONE)");
                                        isRecordLoadResponse = false;
                                        AsyncResult ar11 = (AsyncResult) msg.obj;
                                        if (ar11 != null && ar11.exception == null) {
                                            byte[] data5 = (byte[]) ar11.result;
                                            this.cphsOnss = IccUtils.adnStringFieldToString(data5, 0, data5.length);
                                            mtkLog("Load EF_SPN_SHORT_CPHS: " + this.cphsOnss);
                                            break;
                                        }
                                    default:
                                        switch (i) {
                                            case 1017:
                                                AsyncResult ar12 = (AsyncResult) msg.obj;
                                                byte[] data6 = (byte[]) ar12.result;
                                                if (ar12.exception == null) {
                                                    mtkLog("EF_PSISMSC: " + IccUtils.bytesToHexString(data6));
                                                    if (data6 != null) {
                                                        this.mEfPsismsc = data6;
                                                        break;
                                                    }
                                                }
                                                break;
                                            case 1018:
                                                AsyncResult ar13 = (AsyncResult) msg.obj;
                                                byte[] data7 = (byte[]) ar13.result;
                                                if (ar13.exception == null) {
                                                    mtkLog("EF_SMSP: " + IccUtils.bytesToHexString(data7));
                                                    if (data7 != null) {
                                                        this.mEfSmsp = data7;
                                                        break;
                                                    }
                                                }
                                                break;
                                            case 1019:
                                                AsyncResult ar14 = (AsyncResult) msg.obj;
                                                if (ar14.exception != null) {
                                                    mtkLoge("Error on GET_GBABP with exp " + ar14.exception);
                                                    break;
                                                } else {
                                                    this.mGbabp = IccUtils.bytesToHexString((byte[]) ar14.result);
                                                    mtkLog("EF_GBABP=" + this.mGbabp);
                                                    break;
                                                }
                                            case 1020:
                                                AsyncResult ar15 = (AsyncResult) msg.obj;
                                                if (ar15.exception != null) {
                                                    mtkLoge("Error on GET_GBANL with exp " + ar15.exception);
                                                    break;
                                                } else {
                                                    this.mEfGbanlList = (ArrayList) ar15.result;
                                                    mtkLog("GET_GBANL record count: " + this.mEfGbanlList.size());
                                                    break;
                                                }
                                            case EVENT_CFU_IND /*{ENCODED_INT: 1021}*/:
                                                AsyncResult ar16 = (AsyncResult) msg.obj;
                                                if (!(ar16 == null || ar16.exception != null || ar16.result == null)) {
                                                    mtkLog("handle EVENT_CFU_IND: " + ((int[]) ar16.result)[0]);
                                                    break;
                                                }
                                            case EVENT_IMSI_REFRESH_QUERY /*{ENCODED_INT: 1022}*/:
                                                if (USERDEBUG) {
                                                    mtkLog("handleMessage (EVENT_IMSI_REFRESH_QUERY)");
                                                } else {
                                                    mtkLog("handleMessage (EVENT_IMSI_REFRESH_QUERY) mImsi= " + getIMSI());
                                                }
                                                this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(1023));
                                                break;
                                            case 1023:
                                                mtkLog("handleMessage (EVENT_IMSI_REFRESH_QUERY_DONE)");
                                                AsyncResult ar17 = (AsyncResult) msg.obj;
                                                if (ar17.exception == null) {
                                                    this.mImsi = IccUtils.stripTrailingFs((String) ar17.result);
                                                    if (!Objects.equals(this.mImsi, (String) ar17.result)) {
                                                        loge("Invalid IMSI padding digits received.");
                                                    }
                                                    if (TextUtils.isEmpty(this.mImsi)) {
                                                        this.mImsi = null;
                                                    }
                                                    if (this.mImsi != null && !this.mImsi.matches("[0-9]+")) {
                                                        loge("Invalid non-numeric IMSI digits received.");
                                                        this.mImsi = null;
                                                    }
                                                    if (this.mImsi != null && (this.mImsi.length() < 6 || this.mImsi.length() > 15)) {
                                                        loge("invalid IMSI " + this.mImsi);
                                                        this.mImsi = null;
                                                    }
                                                    log("IMSI: mMncLength=" + this.mMncLength);
                                                    if (this.mImsi != null && this.mImsi.length() >= 6) {
                                                        log("IMSI: " + this.mImsi.substring(0, 6) + Rlog.pii(false, this.mImsi.substring(6)));
                                                    }
                                                    updateOperatorPlmn();
                                                    if (!this.mImsi.equals(this.mSimImsi)) {
                                                        this.mSimImsi = this.mImsi;
                                                        this.mImsiReadyRegistrants.notifyRegistrants();
                                                        mtkLog("SimRecords: mImsiReadyRegistrants.notifyRegistrants");
                                                    }
                                                    if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
                                                        onAllRecordsLoaded();
                                                        break;
                                                    }
                                                } else {
                                                    loge("Exception querying IMSI, Exception:" + ar17.exception);
                                                    break;
                                                }
                                            default:
                                                switch (i) {
                                                    case EVENT_DELAYED_SEND_PHB_CHANGE /*{ENCODED_INT: 1026}*/:
                                                        this.mPhbReady = isPhbReady();
                                                        mtkLog("[EVENT_DELAYED_SEND_PHB_CHANGE] isReady : " + this.mPhbReady);
                                                        broadcastPhbStateChangedIntent(this.mPhbReady, false);
                                                        break;
                                                    case EVENT_PHB_READY /*{ENCODED_INT: 1027}*/:
                                                        AsyncResult ar18 = (AsyncResult) msg.obj;
                                                        if (!(ar18 == null || ar18.exception != null || ar18.result == null)) {
                                                            int[] phbReadyState = (int[]) ar18.result;
                                                            int curSimState = SubscriptionController.getInstance().getSimStateForSlotIndex(this.mSlotId);
                                                            if (!(curSimState == 4 || curSimState == 2)) {
                                                                isSimLocked = false;
                                                            }
                                                            mtkLog("phbReadyState=" + phbReadyState[0] + ",curSimState = " + curSimState + ", isSimLocked = " + isSimLocked);
                                                            updatePHBStatus(phbReadyState[0], isSimLocked);
                                                            updateIccFdnStatus();
                                                            break;
                                                        }
                                                    case EVENT_GET_ALL_PNN_DONE /*{ENCODED_INT: 1028}*/:
                                                        isRecordLoadResponse = false;
                                                        AsyncResult ar19 = (AsyncResult) msg.obj;
                                                        if (ar19.exception == null) {
                                                            parseEFpnn((ArrayList) ar19.result);
                                                            this.mRecordsEventsRegistrants.notifyResult(102);
                                                            if (!this.mReadingOpl) {
                                                                this.mRecordsEventsRegistrants.notifyResult(101);
                                                                break;
                                                            }
                                                        }
                                                        break;
                                                    case EVENT_RSU_SIM_LOCK_CHANGED /*{ENCODED_INT: 1029}*/:
                                                        log("[RSU-SIMLOCK] handleMessage (EVENT_RSU_SIM_LOCK_CHANGED)");
                                                        AsyncResult ar20 = (AsyncResult) msg.obj;
                                                        if (!(ar20 == null || ar20.exception != null || ar20.result == null)) {
                                                            int[] simMelockEvent = (int[]) ar20.result;
                                                            log("[RSU-SIMLOCK] sim melock event = " + simMelockEvent[0]);
                                                            RebootClickListener listener = new RebootClickListener();
                                                            if (simMelockEvent[0] == 0) {
                                                                AlertDialog alertDialog = new AlertDialog.Builder(this.mContext).setTitle("Unlock Phone").setMessage("Please restart the phone now since unlock setting has changed.").setPositiveButton("OK", listener).create();
                                                                alertDialog.setCancelable(false);
                                                                alertDialog.setCanceledOnTouchOutside(false);
                                                                alertDialog.getWindow().setType(2003);
                                                                alertDialog.show();
                                                                break;
                                                            }
                                                        }
                                                        break;
                                                    default:
                                                        MtkSIMRecords.super.handleMessage(msg);
                                                        break;
                                                }
                                                break;
                                        }
                                        break;
                                }
                                break;
                        }
                    } else {
                        mtkLog("handleMessage (EVENT_GET_RAT_DONE)");
                        AsyncResult ar21 = (AsyncResult) msg.obj;
                        this.mEfRatLoaded = true;
                        if (ar21 == null || ar21.exception != null) {
                            mtkLog("load EF_RAT fail");
                            this.mEfRat = null;
                            if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
                                boradcastEfRatContentNotify(256);
                            } else {
                                boradcastEfRatContentNotify(512);
                            }
                        } else {
                            this.mEfRat = (byte[]) ar21.result;
                            mtkLog("load EF_RAT complete: " + ((int) this.mEfRat[0]));
                            boradcastEfRatContentNotify(512);
                        }
                    }
                }
                isRecordLoadResponse = true;
                AsyncResult ar22 = (AsyncResult) msg.obj;
                byte[] data8 = (byte[]) ar22.result;
                boolean isValidMbdn = false;
                if (ar22.exception == null) {
                    mtkLog("EF_MBI: " + IccUtils.bytesToHexString(data8));
                    this.mMailboxIndex = data8[0] & PplMessageManager.Type.INVALID;
                    if (!(this.mMailboxIndex == 0 || this.mMailboxIndex == 255)) {
                        mtkLog("Got valid mailbox number for MBDN");
                        isValidMbdn = true;
                        this.isValidMBI = true;
                    }
                }
                this.mRecordsToLoad++;
                if (isValidMbdn) {
                    mtkLog("EVENT_GET_MBI_DONE, to load EF_MBDN");
                    new AdnRecordLoader(this.mFh).loadFromEF(28615, 28616, this.mMailboxIndex, obtainMessage(6));
                } else if (isCphsMailboxEnabled()) {
                    mtkLog("EVENT_GET_MBI_DONE, to load EF_MAILBOX_CPHS");
                    new AdnRecordLoader(this.mFh).loadFromEF(28439, 28490, 1, obtainMessage(11));
                } else {
                    mtkLog("EVENT_GET_MBI_DONE, do nothing");
                    this.mRecordsToLoad--;
                }
            } else {
                onReady();
            }
            if (!isRecordLoadResponse) {
                return;
            }
        } catch (RuntimeException exc) {
            logw("Exception parsing SIM record", exc);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                onRecordLoaded();
            }
            throw th;
        }
        onRecordLoaded();
    }

    /* access modifiers changed from: protected */
    public void handleFileUpdate(int efid) {
        if (efid != 20272) {
            if (efid == 28435) {
                this.mRecordsToLoad++;
                mtkLog("SIM Refresh called for EF_CFF_CPHS");
                this.mFh.loadEFTransparent(28435, obtainMessage(24));
                return;
            } else if (efid == 28437) {
                this.mRecordsToLoad++;
                mtkLog("[CSP] SIM Refresh for EF_CSP_CPHS");
                this.mFh.loadEFTransparent(28437, obtainMessage(33));
                return;
            } else if (efid == 28439) {
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(28439, 28490, 1, obtainMessage(11));
                return;
            } else if (efid == 28480) {
                this.mRecordsToLoad++;
                mtkLog("SIM Refresh called for EF_MSISDN");
                new AdnRecordLoader(this.mFh).loadFromEF(28480, getExtFromEf(28480), 1, obtainMessage(10));
                return;
            } else if (efid != 28489) {
                if (efid == 28615) {
                    this.mRecordsToLoad++;
                    new AdnRecordLoader(this.mFh).loadFromEF(28615, 28616, this.mMailboxIndex, obtainMessage(6));
                    return;
                } else if (efid == 28619) {
                    this.mRecordsToLoad++;
                    mtkLog("SIM Refresh called for EF_CFIS");
                    this.mFh.loadEFLinearFixed(28619, 1, obtainMessage(32));
                    return;
                } else if (efid != 28474) {
                    if (efid != 28475) {
                        mtkLog("handleFileUpdate default");
                        if (this.mAdnCache.isUsimPhbEfAndNeedReset(efid) && !this.mIsPhbEfResetDone) {
                            this.mIsPhbEfResetDone = true;
                            this.mAdnCache.reset();
                            setPhbReady(false);
                        }
                        this.mLoaded.set(false);
                        fetchSimRecords();
                        return;
                    }
                    mtkLog("SIM Refresh called for EF_FDN");
                    this.mParentApp.queryFdn();
                }
            }
        }
        if (!this.mIsPhbEfResetDone) {
            this.mIsPhbEfResetDone = true;
            this.mAdnCache.reset();
            mtkLog("handleFileUpdate ADN like");
            setPhbReady(false);
        }
    }

    /* access modifiers changed from: protected */
    public void handleRefresh(IccRefreshResponse refreshResponse) {
        if (refreshResponse == null) {
            mtkLog("handleSimRefresh received without input");
        } else if (refreshResponse.aid == null || TextUtils.isEmpty(refreshResponse.aid) || refreshResponse.aid.equals(this.mParentApp.getAid()) || refreshResponse.refreshResult == 4) {
            int i = refreshResponse.refreshResult;
            if (i == 0) {
                mtkLog("handleRefresh with SIM_REFRESH_FILE_UPDATED");
                handleFileUpdate(refreshResponse.efId);
                this.mIsPhbEfResetDone = false;
            } else if (i == 1) {
                mtkLog("handleRefresh with SIM_REFRESH_INIT");
                handleFileUpdate(-1);
            } else if (i == 2) {
                mtkLog("handleRefresh with SIM_REFRESH_RESET");
                if (!SystemProperties.get("ro.vendor.sim_refresh_reset_by_modem").equals("1")) {
                    mtkLog("sim_refresh_reset_by_modem false");
                    if (this.mCi != null) {
                        this.mCi.restartRILD(null);
                    }
                } else {
                    mtkLog("Sim reset by modem!");
                }
                setPhbReady(false);
            } else if (i == 4) {
                mtkLog("handleRefresh with REFRESH_INIT_FULL_FILE_UPDATED");
                setPhbReady(false);
                handleFileUpdate(-1);
            } else if (i == 5) {
                mtkLog("handleRefresh with REFRESH_INIT_FILE_UPDATED, EFID = " + refreshResponse.efId);
                handleFileUpdate(refreshResponse.efId);
                this.mIsPhbEfResetDone = false;
                if (this.mParentApp.getState() == IccCardApplicationStatus.AppState.APPSTATE_READY) {
                    sendMessage(obtainMessage(1));
                }
            } else if (i != 6) {
                mtkLog("handleSimRefresh callback to parent");
                MtkSIMRecords.super.handleRefresh(refreshResponse);
            } else {
                mtkLog("handleSimRefresh with REFRESH_SESSION_RESET");
                handleFileUpdate(-1);
            }
        } else {
            mtkLog("handleRefresh, refreshResponse.aid = " + refreshResponse.aid + ", mParentApp.getAid() = " + this.mParentApp.getAid());
        }
    }

    private String findBestLanguage(byte[] languages) {
        String[] locales = this.mContext.getAssets().getLocales();
        if (languages == null || locales == null) {
            return null;
        }
        int i = 0;
        while (i + 1 < languages.length) {
            try {
                String lang = new String(languages, i, 2, "ISO-8859-1");
                mtkLog("languages from sim = " + lang);
                for (int j = 0; j < locales.length; j++) {
                    if (locales[j] != null && locales[j].length() >= 2 && locales[j].substring(0, 2).equalsIgnoreCase(lang)) {
                        return lang;
                    }
                }
                if (0 != 0) {
                    break;
                }
                i += 2;
            } catch (UnsupportedEncodingException e) {
                mtkLog("Failed to parse USIM language records" + e);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void onAllRecordsLoaded() {
        if (this.mParentApp.getState() == IccCardApplicationStatus.AppState.APPSTATE_SUBSCRIPTION_PERSO) {
            this.mRecordsRequested = false;
            return;
        }
        MtkSIMRecords.super.onAllRecordsLoaded();
        if (this.mParentApp.getState() == IccCardApplicationStatus.AppState.APPSTATE_PIN || this.mParentApp.getState() == IccCardApplicationStatus.AppState.APPSTATE_PUK) {
            this.mRecordsRequested = false;
            return;
        }
        MtkSIMRecords.super.oppoProcessChangeRegion(this.mContext, this.mParentApp.getPhoneId());
        setSpnFromConfig(getOperatorNumeric());
        String operator = getOperatorNumeric();
        mtkLog("onAllRecordsLoaded operator = " + operator + ", imsi = " + MtkIccUtilsEx.getPrintableString(getIMSI(), 10));
        if (operator != null) {
            if (operator.equals("46002") || operator.equals("46007")) {
                operator = "46000";
            }
            setSystemProperty("vendor.gsm.sim.operator.default-name", MtkSpnOverride.getInstance().lookupOperatorName(MtkSubscriptionManager.getSubIdUsingPhoneId(this.mParentApp.getPhoneId()), operator, true, this.mContext));
        }
        fetchPnnAndOpl();
        fetchCPHSOns();
        fetchRatBalancing();
        fetchSmsp();
        fetchGbaRecords();
        if (this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.version.exp")) {
            this.mContext.sendBroadcast(new Intent("android.intent.action.INSERT_TEST_SIM"));
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkCdma3gCard() {
        boolean result = MtkIccUtilsEx.checkCdma3gCard(this.mSlotId) <= 0;
        log("checkCdma3gCard result: " + result);
        return result;
    }

    /* access modifiers changed from: protected */
    public void setSystemProperty(String key, String val) {
        String str;
        if ("vendor.gsm.sim.operator.default-name".equals(key)) {
            if (!(this.mOldOperatorDefaultName == null && val == null) && ((str = this.mOldOperatorDefaultName) == null || !str.equals(val))) {
                this.mOldOperatorDefaultName = val;
            } else {
                log("set PROPERTY_ICC_OPERATOR_DEFAULT_NAME same value. val:" + val);
                return;
            }
        }
        MtkSIMRecords.super.setSystemProperty(key, val);
    }

    /* access modifiers changed from: protected */
    public void setVoiceMailByCountry(String spn) {
        MtkSIMRecords.super.setVoiceMailByCountry(spn);
        if (this.mVmConfig.containsCarrier(spn)) {
            mtkLog("setVoiceMailByCountry");
        }
    }

    /* access modifiers changed from: protected */
    public void fetchSimRecords() {
        MtkSIMRecords.super.fetchSimRecords();
        this.mFh.loadEFLinearFixed(28617, 1, obtainMessage(5));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(28618, 1, obtainMessage(7));
        this.mRecordsToLoad++;
        getSpnFsm(true, null);
    }

    /* access modifiers changed from: protected */
    public boolean isSpnActive() {
        getServiceProviderName();
        if (this.mEfSST == null || this.mParentApp == null) {
            return false;
        }
        if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
            byte[] bArr = this.mEfSST;
            if (bArr.length < 3 || (bArr[2] & 4) != 4) {
                return false;
            }
            mtkLog("isSpnActive USIM mEfSST is " + IccUtils.bytesToHexString(this.mEfSST) + " set bSpnActive to true");
            return true;
        }
        byte[] bArr2 = this.mEfSST;
        if (bArr2.length < 5 || (bArr2[4] & 2) != 2) {
            return false;
        }
        mtkLog("isSpnActive SIM mEfSST is " + IccUtils.bytesToHexString(this.mEfSST) + " set bSpnActive to true");
        return true;
    }

    public String getSpNameInEfSpn() {
        mtkLog("getSpNameInEfSpn(): " + this.mSpNameInEfSpn);
        return this.mSpNameInEfSpn;
    }

    public String isOperatorMvnoForImsi() {
        String imsiPattern = MtkSpnOverride.getInstance().isOperatorMvnoForImsi(getOperatorNumeric(), getIMSI());
        String mccmnc = getOperatorNumeric();
        mtkLog("isOperatorMvnoForImsi(), imsiPattern: " + imsiPattern + ", mccmnc: " + mccmnc);
        if (imsiPattern == null || mccmnc == null) {
            return null;
        }
        String result = imsiPattern.substring(mccmnc.length(), imsiPattern.length());
        mtkLog("isOperatorMvnoForImsi(): " + result);
        return result;
    }

    public String getFirstFullNameInEfPnn() {
        ArrayList<OperatorName> arrayList = this.mPnnNetworkNames;
        if (arrayList == null || arrayList.size() == 0) {
            mtkLog("getFirstFullNameInEfPnn(): empty");
            return null;
        }
        OperatorName opName = this.mPnnNetworkNames.get(0);
        mtkLog("getFirstFullNameInEfPnn(): first fullname: " + opName.sFullName);
        if (opName.sFullName != null) {
            return new String(opName.sFullName);
        }
        return null;
    }

    public String isOperatorMvnoForEfPnn() {
        String MCCMNC = getOperatorNumeric();
        String PNN = getFirstFullNameInEfPnn();
        mtkLog("isOperatorMvnoForEfPnn(): mccmnc = " + MCCMNC + ", pnn = " + PNN);
        if (MtkSpnOverride.getInstance().getSpnByEfPnn(MCCMNC, PNN) != null) {
            return PNN;
        }
        return null;
    }

    public String getMvnoMatchType() {
        String IMSI = getIMSI();
        String SPN = getSpNameInEfSpn();
        String PNN = getFirstFullNameInEfPnn();
        String GID1 = getGid1();
        String MCCMNC = getOperatorNumeric();
        if (USERDEBUG) {
            mtkLog("getMvnoMatchType(): imsi = ***, mccmnc = " + MCCMNC + ", spn = " + SPN);
        } else {
            mtkLog("getMvnoMatchType(): imsi = " + IMSI + ", mccmnc = " + MCCMNC + ", spn = " + SPN);
        }
        if (MtkSpnOverride.getInstance().getSpnByEfSpn(MCCMNC, SPN) != null) {
            return "spn";
        }
        if (MtkSpnOverride.getInstance().getSpnByImsi(MCCMNC, IMSI) != null) {
            return "imsi";
        }
        if (MtkSpnOverride.getInstance().getSpnByEfPnn(MCCMNC, PNN) != null) {
            return "pnn";
        }
        if (MtkSpnOverride.getInstance().getSpnByEfGid1(MCCMNC, GID1) != null) {
            return "gid";
        }
        return "";
    }

    private class SIMBroadCastReceiver extends BroadcastReceiver {
        private SIMBroadCastReceiver() {
        }

        public void onReceive(Context content, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                String reasonExtra = intent.getStringExtra(DataSubConstants.EXTRA_MOBILE_DATA_ENABLE_REASON);
                int id = intent.getIntExtra("phone", 0);
                String simState = intent.getStringExtra("ss");
                MtkSIMRecords mtkSIMRecords = MtkSIMRecords.this;
                mtkSIMRecords.mtkLog("SIM_STATE_CHANGED: phone id = " + id + ",reason = " + reasonExtra + ", simState = " + simState);
                if ("PUK".equals(reasonExtra) && id == MtkSIMRecords.this.mSlotId) {
                    String strPuk1Count = SystemProperties.get(MtkSIMRecords.SIMRECORD_PROPERTY_RIL_PUK1[MtkSIMRecords.this.mSlotId], "0");
                    MtkSIMRecords mtkSIMRecords2 = MtkSIMRecords.this;
                    mtkSIMRecords2.mtkLog("SIM_STATE_CHANGED: strPuk1Count = " + strPuk1Count);
                    String unused = MtkSIMRecords.this.mMsisdn = "";
                    MtkSIMRecords.this.mRecordsEventsRegistrants.notifyResult(100);
                }
                if (id == MtkSIMRecords.this.mSlotId) {
                    String strPhbReady = TelephonyManager.getTelephonyProperty(MtkSIMRecords.this.mSlotId, MtkSIMRecords.SIMRECORD_PROPERTY_RIL_PHB_READY, "false");
                    MtkSIMRecords mtkSIMRecords3 = MtkSIMRecords.this;
                    mtkSIMRecords3.mtkLog("sim state: " + simState + ", mPhbReady: " + MtkSIMRecords.this.mPhbReady + ",strPhbReady: " + strPhbReady);
                    if (!"READY".equals(simState)) {
                        return;
                    }
                    if (!MtkSIMRecords.this.mPhbReady && strPhbReady.equals("true")) {
                        boolean unused2 = MtkSIMRecords.this.mPhbReady = true;
                        MtkSIMRecords mtkSIMRecords4 = MtkSIMRecords.this;
                        mtkSIMRecords4.broadcastPhbStateChangedIntent(mtkSIMRecords4.mPhbReady, false);
                    } else if (true == MtkSIMRecords.this.mPhbWaitSub && strPhbReady.equals("true")) {
                        MtkSIMRecords mtkSIMRecords5 = MtkSIMRecords.this;
                        mtkSIMRecords5.mtkLog("mPhbWaitSub is " + MtkSIMRecords.this.mPhbWaitSub + ", broadcast if need");
                        boolean unused3 = MtkSIMRecords.this.mPhbWaitSub = false;
                        MtkSIMRecords mtkSIMRecords6 = MtkSIMRecords.this;
                        mtkSIMRecords6.broadcastPhbStateChangedIntent(mtkSIMRecords6.mPhbReady, false);
                    }
                }
            }
        }
    }

    private void updateConfiguration(String numeric) {
        if (TextUtils.isEmpty(numeric) || this.mOldMccMnc.equals(numeric)) {
            mtkLog("Do not update configuration if mcc mnc no change.");
            return;
        }
        this.mOldMccMnc = numeric;
        MccTable.updateMccMncConfiguration(this.mContext, this.mOldMccMnc);
    }

    private void parseEFpnn(ArrayList messages) {
        int count = messages.size();
        mtkLog("parseEFpnn(): pnn has " + count + " records");
        this.mPnnNetworkNames = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            byte[] data = (byte[]) messages.get(i);
            mtkLog("parseEFpnn(): pnn record " + i + " content is " + IccUtils.bytesToHexString(data));
            SimTlv tlv = new SimTlv(data, 0, data.length);
            OperatorName opName = new OperatorName();
            while (tlv.isValidObject()) {
                if (tlv.getTag() == 67) {
                    opName.sFullName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                    mtkLog("parseEFpnn(): pnn sFullName is " + opName.sFullName);
                } else if (tlv.getTag() == 69) {
                    opName.sShortName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                    mtkLog("parseEFpnn(): pnn sShortName is " + opName.sShortName);
                }
                tlv.nextObject();
            }
            this.mPnnNetworkNames.add(opName);
        }
    }

    private void fetchPnnAndOpl() {
        log("fetchPnnAndOpl()");
        boolean bPnnActive = false;
        boolean z = false;
        this.mReadingOpl = false;
        if (this.mEfSST != null) {
            if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
                byte[] bArr = this.mEfSST;
                if (bArr.length >= 6) {
                    bPnnActive = (bArr[5] & PplControlData.STATUS_WIPE_REQUESTED) == 16;
                    if (bPnnActive) {
                        if ((this.mEfSST[5] & 32) == 32) {
                            z = true;
                        }
                        this.mReadingOpl = z;
                    }
                }
            } else {
                byte[] bArr2 = this.mEfSST;
                if (bArr2.length >= 13) {
                    bPnnActive = (bArr2[12] & 48) == 48;
                    if (bPnnActive) {
                        if ((this.mEfSST[12] & 192) == 192) {
                            z = true;
                        }
                        this.mReadingOpl = z;
                    }
                }
            }
        }
        log("bPnnActive = " + bPnnActive + ", bOplActive = " + this.mReadingOpl);
        if (bPnnActive) {
            this.mFh.loadEFLinearFixedAll(28613, obtainMessage(EVENT_GET_ALL_PNN_DONE));
            if (this.mReadingOpl) {
                this.mFh.loadEFLinearFixedAll(28614, obtainMessage(1008));
            }
        }
    }

    private void fetchSpn() {
        mtkLog("fetchSpn()");
        if (getSIMServiceStatus(IccServiceInfo.IccService.SPN) == IccServiceInfo.IccServiceStatus.ACTIVATED) {
            setServiceProviderName(null);
            this.mFh.loadEFTransparent(28486, obtainMessage(12));
            this.mRecordsToLoad++;
            return;
        }
        mtkLog("[SIMRecords] SPN service is not activated  ");
    }

    public IccServiceInfo.IccServiceStatus getSIMServiceStatus(IccServiceInfo.IccService enService) {
        int nbit;
        int nbit2;
        int nServiceNum = enService.getIndex();
        IccServiceInfo.IccServiceStatus simServiceStatus = IccServiceInfo.IccServiceStatus.UNKNOWN;
        mtkLog("getSIMServiceStatus enService is " + enService + " Service Index is " + nServiceNum);
        if (nServiceNum >= 0 && nServiceNum < IccServiceInfo.IccService.UNSUPPORTED_SERVICE.getIndex() && this.mEfSST != null) {
            if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
                int nUSTIndex = usimServiceNumber[nServiceNum];
                if (nUSTIndex <= 0) {
                    simServiceStatus = IccServiceInfo.IccServiceStatus.NOT_EXIST_IN_USIM;
                } else {
                    int nbyte = nUSTIndex / 8;
                    int nbit3 = nUSTIndex % 8;
                    if (nbit3 == 0) {
                        nbit2 = 7;
                        nbyte--;
                    } else {
                        nbit2 = nbit3 - 1;
                    }
                    mtkLog("getSIMServiceStatus USIM nbyte: " + nbyte + " nbit: " + nbit2);
                    byte[] bArr = this.mEfSST;
                    simServiceStatus = (bArr.length <= nbyte || (bArr[nbyte] & (1 << nbit2)) <= 0) ? IccServiceInfo.IccServiceStatus.INACTIVATED : IccServiceInfo.IccServiceStatus.ACTIVATED;
                }
            } else {
                int nSSTIndex = simServiceNumber[nServiceNum];
                if (nSSTIndex <= 0) {
                    simServiceStatus = IccServiceInfo.IccServiceStatus.NOT_EXIST_IN_SIM;
                } else {
                    int nbyte2 = nSSTIndex / 4;
                    int nbit4 = nSSTIndex % 4;
                    if (nbit4 == 0) {
                        nbit = 3;
                        nbyte2--;
                    } else {
                        nbit = nbit4 - 1;
                    }
                    int nMask = 2 << (nbit * 2);
                    mtkLog("getSIMServiceStatus SIM nbyte: " + nbyte2 + " nbit: " + nbit + " nMask: " + nMask);
                    byte[] bArr2 = this.mEfSST;
                    simServiceStatus = (bArr2.length <= nbyte2 || (bArr2[nbyte2] & nMask) != nMask) ? IccServiceInfo.IccServiceStatus.INACTIVATED : IccServiceInfo.IccServiceStatus.ACTIVATED;
                }
            }
        }
        mtkLog("getSIMServiceStatus simServiceStatus: " + simServiceStatus);
        return simServiceStatus;
    }

    private void fetchSmsp() {
        mtkLog("fetchSmsp()");
        if (this.mUsimServiceTable != null && this.mParentApp.getType() != IccCardApplicationStatus.AppType.APPTYPE_SIM && this.mUsimServiceTable.isAvailable(UsimServiceTable.UsimService.SM_SERVICE_PARAMS)) {
            mtkLog("SMSP support.");
            this.mFh.loadEFLinearFixed((int) MtkIccConstants.EF_SMSP, 1, obtainMessage(1018));
            if (this.mUsimServiceTable.isAvailable(UsimServiceTable.UsimService.SM_OVER_IP)) {
                mtkLog("PSISMSP support.");
                this.mFh.loadEFLinearFixed((int) MtkIccConstants.EF_PSISMSC, 1, obtainMessage(1017));
            }
        }
    }

    private void fetchGbaRecords() {
        mtkLog("fetchGbaRecords");
        if (this.mUsimServiceTable != null && this.mParentApp.getType() != IccCardApplicationStatus.AppType.APPTYPE_SIM && this.mUsimServiceTable.isAvailable(UsimServiceTable.UsimService.GBA)) {
            mtkLog("GBA support.");
            this.mFh.loadEFTransparent((int) MtkIccConstants.EF_ISIM_GBABP, obtainMessage(1019));
            this.mFh.loadEFLinearFixedAll((int) MtkIccConstants.EF_ISIM_GBANL, obtainMessage(1020));
        }
    }

    private void fetchMbiRecords() {
        mtkLog("fetchMbiRecords");
        if (this.mUsimServiceTable != null && this.mParentApp.getType() != IccCardApplicationStatus.AppType.APPTYPE_SIM && this.mUsimServiceTable.isAvailable(UsimServiceTable.UsimService.MBDN)) {
            mtkLog("MBI/MBDN support.");
            this.mFh.loadEFLinearFixed(28617, 1, obtainMessage(5));
            this.mRecordsToLoad++;
        }
    }

    private void fetchMwisRecords() {
        mtkLog("fetchMwisRecords");
        if (this.mUsimServiceTable != null && this.mParentApp.getType() != IccCardApplicationStatus.AppType.APPTYPE_SIM && this.mUsimServiceTable.isAvailable(UsimServiceTable.UsimService.MWI_STATUS)) {
            mtkLog("MWIS support.");
            this.mFh.loadEFLinearFixed(28618, 1, obtainMessage(7));
            this.mRecordsToLoad++;
        }
    }

    private void parseEFopl(ArrayList messages) {
        try {
            int count = messages.size();
            mtkLog("parseEFopl(): opl has " + count + " records");
            this.mOperatorList = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                byte[] data = (byte[]) messages.get(i);
                OplRecord oplRec = new OplRecord();
                oplRec.sPlmn = MtkIccUtils.parsePlmnToStringForEfOpl(data, 0, 3);
                oplRec.nMinLAC = Integer.parseInt(IccUtils.bytesToHexString(new byte[]{data[3], data[4]}), 16);
                oplRec.nMaxLAC = Integer.parseInt(IccUtils.bytesToHexString(new byte[]{data[5], data[6]}), 16);
                oplRec.nPnnIndex = Integer.parseInt(IccUtils.bytesToHexString(new byte[]{data[7]}), 16);
                mtkLog("parseEFopl(): record=" + i + " content=" + IccUtils.bytesToHexString(data) + " sPlmn=" + oplRec.sPlmn + " nMinLAC=" + oplRec.nMinLAC + " nMaxLAC=" + oplRec.nMaxLAC + " nPnnIndex=" + oplRec.nPnnIndex);
                this.mOperatorList.add(oplRec);
            }
        } catch (NumberFormatException e) {
            Rlog.d("SIMRecords", e.toString());
        } catch (Exception e2) {
            Rlog.d("SIMRecords", e2.toString());
        }
    }

    private void boradcastEfRatContentNotify(int item) {
        Intent intent = new Intent("com.mediatek.phone.ACTION_EF_RAT_CONTENT_NOTIFY");
        intent.putExtra("ef_rat_status", item);
        intent.putExtra("slot", this.mSlotId);
        mtkLog("broadCast intent ACTION_EF_RAT_CONTENT_NOTIFY: item: " + item + ", simId: " + this.mSlotId);
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    private void processEfCspPlmnModeBitUrc(int bit) {
        mtkLog("processEfCspPlmnModeBitUrc: bit = " + bit);
        if (bit == 0) {
            this.mCspPlmnEnabled = false;
        } else {
            this.mCspPlmnEnabled = true;
        }
        Intent intent = new Intent("com.mediatek.phone.ACTION_EF_CSP_CONTENT_NOTIFY");
        intent.putExtra("plmn_mode_bit", bit);
        intent.putExtra("slot", this.mSlotId);
        mtkLog("broadCast intent ACTION_EF_CSP_CONTENT_NOTIFY, EXTRA_PLMN_MODE_BIT: " + bit);
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    public String getMenuTitleFromEf() {
        return this.mMenuTitleFromEf;
    }

    private void fetchCPHSOns() {
        mtkLog("fetchCPHSOns()");
        this.cphsOnsl = null;
        this.cphsOnss = null;
        this.mFh.loadEFTransparent(28436, obtainMessage(1009));
        this.mFh.loadEFTransparent(28440, obtainMessage(1010));
    }

    private void fetchRatBalancing() {
        if (!isFetchRatBalancingAndEnsFile(this.mSlotId)) {
            mtkLog("Not support MTK_RAT_BALANCING");
        } else if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
            mtkLog("start loading EF_RAT");
            this.mFh.loadEFTransparent((int) MtkIccConstants.EF_RAT, obtainMessage(1014));
        } else if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_SIM) {
            mtkLog("loading EF_RAT fail, because of SIM");
            this.mEfRatLoaded = false;
            this.mEfRat = null;
            boradcastEfRatContentNotify(512);
        } else {
            mtkLog("loading EF_RAT fail, because of +EUSIM");
        }
    }

    public int getEfRatBalancing() {
        StringBuilder sb = new StringBuilder();
        sb.append("getEfRatBalancing: iccCardType = ");
        sb.append(this.mParentApp.getType());
        sb.append(", mEfRatLoaded = ");
        sb.append(this.mEfRatLoaded);
        sb.append(", mEfRat is null = ");
        sb.append(this.mEfRat == null);
        mtkLog(sb.toString());
        if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM && this.mEfRatLoaded && this.mEfRat == null) {
            return 256;
        }
        return 512;
    }

    private boolean isMatchingPlmnForEfOpl(String simPlmn, String bcchPlmn) {
        if (simPlmn == null || simPlmn.equals("") || bcchPlmn == null || bcchPlmn.equals("")) {
            return false;
        }
        mtkLog("isMatchingPlmnForEfOpl(): simPlmn = " + simPlmn + ", bcchPlmn = " + bcchPlmn);
        int simPlmnLen = simPlmn.length();
        int bcchPlmnLen = bcchPlmn.length();
        if (simPlmnLen < 5 || bcchPlmnLen < 5) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            if (simPlmn.charAt(i) != 'd' && simPlmn.charAt(i) != bcchPlmn.charAt(i)) {
                return false;
            }
        }
        if (simPlmnLen == 6 && bcchPlmnLen == 6) {
            if (simPlmn.charAt(5) == 'd' || simPlmn.charAt(5) == bcchPlmn.charAt(5)) {
                return true;
            }
            return false;
        } else if (bcchPlmnLen == 6 && bcchPlmn.charAt(5) != '0' && bcchPlmn.charAt(5) != 'd') {
            return false;
        } else {
            if (simPlmnLen != 6 || simPlmn.charAt(5) == '0' || simPlmn.charAt(5) == 'd') {
                return true;
            }
            return false;
        }
    }

    private boolean isPlmnEqualsSimNumeric(String plmn) {
        String mccmnc = getOperatorNumeric();
        if (plmn == null) {
            return false;
        }
        if (mccmnc == null || mccmnc.equals("")) {
            mtkLog("isPlmnEqualsSimNumeric: getOperatorNumeric error: " + mccmnc);
            return false;
        } else if (plmn.equals(mccmnc)) {
            return true;
        } else {
            if (plmn.length() == 5 && mccmnc.length() == 6 && plmn.equals(mccmnc.substring(0, 5))) {
                return true;
            }
            return false;
        }
    }

    public String getEonsIfExist(String plmn, int nLac, boolean bLongNameRequired) {
        ArrayList<OperatorName> arrayList;
        OplRecord oplRec;
        StringBuilder lac_sb = new StringBuilder(Integer.toHexString(nLac));
        if (lac_sb.length() == 1 || lac_sb.length() == 2) {
            lac_sb.setCharAt(0, '*');
        } else {
            for (int i = 0; i < lac_sb.length() / 2; i++) {
                lac_sb.setCharAt(i, '*');
            }
        }
        mtkLog("EONS getEonsIfExist: plmn is " + plmn + " nLac is " + lac_sb.toString() + " bLongNameRequired: " + bLongNameRequired);
        if (plmn == null || (arrayList = this.mPnnNetworkNames) == null || arrayList.size() == 0) {
            return null;
        }
        int nPnnIndex = -1;
        boolean isHPLMN = isPlmnEqualsSimNumeric(plmn);
        if (this.mOperatorList != null) {
            int i2 = 0;
            while (true) {
                if (i2 >= this.mOperatorList.size()) {
                    break;
                }
                oplRec = this.mOperatorList.get(i2);
                if (!isMatchingPlmnForEfOpl(oplRec.sPlmn, plmn) || (!(oplRec.nMinLAC == 0 && oplRec.nMaxLAC == 65534) && (oplRec.nMinLAC > nLac || oplRec.nMaxLAC < nLac))) {
                    i2++;
                }
            }
            mtkLog("getEonsIfExist: find it in EF_OPL");
            if (oplRec.nPnnIndex == 0) {
                mtkLog("getEonsIfExist: oplRec.nPnnIndex is 0, from other sources");
                return null;
            }
            nPnnIndex = oplRec.nPnnIndex;
        } else if (isHPLMN) {
            mtkLog("getEonsIfExist: Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else {
            mtkLog("getEonsIfExist: Plmn is not HPLMN and no mOperatorList, return null");
            return null;
        }
        if (nPnnIndex == -1 && isHPLMN && this.mOperatorList.size() == 1) {
            mtkLog("getEonsIfExist: not find it in EF_OPL, but Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else if (nPnnIndex > 1 && nPnnIndex > this.mPnnNetworkNames.size() && isHPLMN) {
            mtkLog("getEonsIfExist: find it in EF_OPL, but index in EF_OPL > EF_PNN list length & Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else if (nPnnIndex > 1 && nPnnIndex > this.mPnnNetworkNames.size() && !isHPLMN) {
            mtkLog("getEonsIfExist: find it in EF_OPL, but index in EF_OPL > EF_PNN list length & Plmn is not HPLMN, return PNN's first record");
            nPnnIndex = -1;
        }
        String sEons = null;
        if (nPnnIndex >= 1) {
            OperatorName opName = this.mPnnNetworkNames.get(nPnnIndex - 1);
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
            String spn = getServiceProviderName();
            String simCardMccMnc = getOperatorNumeric();
            mtkLog("getEonsIfExist spn = " + spn + ", simCardMccMnc " + simCardMccMnc);
            if (!TextUtils.isEmpty(spn) && "50503".equals(simCardMccMnc) && "50503".equals(plmn)) {
                sEons = spn;
                mtkLog("sEons = " + sEons);
            }
        }
        mtkLog("getEonsIfExist: sEons is " + sEons);
        return sEons;
    }

    public String getEfGbabp() {
        mtkLog("GBABP = " + this.mGbabp);
        return this.mGbabp;
    }

    public void setEfGbabp(String gbabp, Message onComplete) {
        this.mFh.updateEFTransparent((int) MtkIccConstants.EF_GBABP, IccUtils.hexStringToBytes(gbabp), onComplete);
    }

    public byte[] getEfPsismsc() {
        return this.mEfPsismsc;
    }

    public byte[] getEfSmsp() {
        return this.mEfSmsp;
    }

    public int getMncLength() {
        mtkLog("mncLength = " + this.mMncLength);
        return this.mMncLength;
    }

    private class RebootClickListener implements DialogInterface.OnClickListener {
        private RebootClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            MtkSIMRecords.this.log("[RSU-SIMLOCK] Unlock Phone onClick");
            ((PowerManager) MtkSIMRecords.this.mContext.getSystemService("power")).reboot("Unlock state changed");
        }
    }

    public boolean isRadioAvailable() {
        if (this.mCi == null || this.mCi.getRadioState() == 2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void updateIccFdnStatus() {
    }

    /* access modifiers changed from: private */
    public void broadcastPhbStateChangedIntent(boolean isReady, boolean isForceSendIntent) {
        Phone phone = this.mPhone;
        if (phone == null || phone.getPhoneType() == 1 || (this.isDispose && !isReady)) {
            mtkLog("broadcastPhbStateChangedIntent, mPhbReady " + this.mPhbReady + ", " + this.mSubId);
            if (isReady) {
                this.mSubId = MtkSubscriptionManager.getSubIdUsingPhoneId(this.mSlotId);
                int curSimState = SubscriptionController.getInstance().getSimStateForSlotIndex(this.mSlotId);
                if (this.mSubId <= 0 || curSimState == 0) {
                    mtkLog("broadcastPhbStateChangedIntent, mSubId " + this.mSubId + ", sim state " + curSimState);
                    this.mPhbWaitSub = true;
                    return;
                }
            } else {
                if (isForceSendIntent && this.mPhbReady) {
                    this.mSubId = MtkSubscriptionManager.getSubIdUsingPhoneId(this.mSlotId);
                }
                if (this.mSubId <= 0) {
                    mtkLog("broadcastPhbStateChangedIntent, isReady == false and mSubId <= 0");
                    return;
                }
            }
            boolean isUnlock = ((UserManager) this.mContext.getSystemService(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER)).isUserUnlocked();
            if (!SystemProperties.get("sys.boot_completed").equals("1") || !isUnlock) {
                mtkLog("broadcastPhbStateChangedIntent, boot not completed, isUnlock:" + isUnlock);
                this.mPendingPhbNotify = true;
                return;
            }
            Intent intent = new Intent("mediatek.intent.action.PHB_STATE_CHANGED");
            intent.putExtra("ready", isReady);
            intent.putExtra("subscription", this.mSubId);
            mtkLog("Broadcasting intent ACTION_PHB_STATE_CHANGED " + isReady + " sub id " + this.mSubId + " phoneId " + this.mParentApp.getPhoneId());
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            Intent bootIntent = new Intent("mediatek.intent.action.PHB_STATE_CHANGED");
            bootIntent.putExtra("ready", isReady);
            bootIntent.putExtra("subscription", this.mSubId);
            bootIntent.setPackage("com.mediatek.simprocessor");
            mtkLog("Broadcasting ACTION_PHB_STATE_CHANGED to package: simprocessor");
            this.mContext.sendBroadcastAsUser(bootIntent, UserHandle.ALL);
            if (!isReady) {
                this.mSubId = -1;
                return;
            }
            return;
        }
        this.mPendingPhbNotify = true;
        mtkLog("broadcastPhbStateChangedIntent, No active Phone, will notfiy when dispose");
    }

    public boolean isPhbReady() {
        String strCurSimState = "";
        StringBuilder sb = new StringBuilder();
        sb.append("phbReady(): cached mPhbReady = ");
        sb.append(this.mPhbReady ? "true" : "false");
        mtkLog(sb.toString());
        if (this.mParentApp == null || this.mPhone == null) {
            return false;
        }
        String strPhbReady = TelephonyManager.getTelephonyProperty(this.mSlotId, SIMRECORD_PROPERTY_RIL_PHB_READY, "false");
        if (SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild")) {
            mtkLog("phbReady(): strPhbReady = " + strPhbReady);
            return strPhbReady.equals("true");
        }
        String strAllSimState = SystemProperties.get("gsm.sim.state");
        if (strAllSimState != null && strAllSimState.length() > 0) {
            String[] values = strAllSimState.split(",");
            int i = this.mSlotId;
            if (i >= 0 && i < values.length && values[i] != null) {
                strCurSimState = values[i];
            }
        }
        boolean isSimLocked = strCurSimState.equals("NETWORK_LOCKED") || strCurSimState.equals("PIN_REQUIRED");
        mtkLog("phbReady(): strPhbReady = " + strPhbReady + ", strAllSimState = " + strAllSimState);
        if (!strPhbReady.equals("true") || isSimLocked) {
            return false;
        }
        return true;
    }

    public void setPhbReady(boolean isReady) {
        StringBuilder sb = new StringBuilder();
        sb.append("setPhbReady(): isReady = ");
        sb.append(isReady ? "true" : "false");
        mtkLog(sb.toString());
        if (this.mPhbReady != isReady) {
            this.mPhbReady = isReady;
            if (isReady) {
                this.mCi.setPhonebookReady(1, null);
            } else if (!isReady) {
                this.mCi.setPhonebookReady(0, null);
            }
            broadcastPhbStateChangedIntent(this.mPhbReady, false);
        }
    }

    private class PhbBroadCastReceiver extends BroadcastReceiver {
        private PhbBroadCastReceiver() {
        }

        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (MtkSIMRecords.this.mPhbWaitSub && action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                MtkSIMRecords.this.mtkLog("SubBroadCastReceiver receive ACTION_SUBINFO_RECORD_UPDATED");
                boolean unused = MtkSIMRecords.this.mPhbWaitSub = false;
                MtkSIMRecords mtkSIMRecords = MtkSIMRecords.this;
                mtkSIMRecords.broadcastPhbStateChangedIntent(mtkSIMRecords.mPhbReady, false);
            } else if (action.equals("android.intent.action.RADIO_TECHNOLOGY")) {
                int phoneid = intent.getIntExtra("phone", -1);
                MtkSIMRecords mtkSIMRecords2 = MtkSIMRecords.this;
                mtkSIMRecords2.mtkLog("[ACTION_RADIO_TECHNOLOGY_CHANGED] phoneid : " + phoneid);
                if (MtkSIMRecords.this.mParentApp != null && MtkSIMRecords.this.mParentApp.getPhoneId() == phoneid) {
                    String activePhoneName = intent.getStringExtra("phoneName");
                    int subid = intent.getIntExtra("subscription", -1);
                    MtkSIMRecords mtkSIMRecords3 = MtkSIMRecords.this;
                    mtkSIMRecords3.mtkLog("[ACTION_RADIO_TECHNOLOGY_CHANGED] activePhoneName : " + activePhoneName + " | subid : " + subid);
                    if (!"CDMA".equals(activePhoneName)) {
                        MtkSIMRecords.this.broadcastPhbStateChangedIntent(false, true);
                        MtkSIMRecords mtkSIMRecords4 = MtkSIMRecords.this;
                        mtkSIMRecords4.sendMessageDelayed(mtkSIMRecords4.obtainMessage(MtkSIMRecords.EVENT_DELAYED_SEND_PHB_CHANGE), (long) MtkRuimRecords.PHB_DELAY_SEND_TIME);
                        MtkSIMRecords.this.mAdnCache.reset();
                    }
                }
            } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                MtkSIMRecords mtkSIMRecords5 = MtkSIMRecords.this;
                mtkSIMRecords5.mtkLog("[onReceive] ACTION_BOOT_COMPLETED mPendingPhbNotify : " + MtkSIMRecords.this.mPendingPhbNotify);
                if (MtkSIMRecords.this.mPendingPhbNotify) {
                    MtkSIMRecords mtkSIMRecords6 = MtkSIMRecords.this;
                    mtkSIMRecords6.broadcastPhbStateChangedIntent(mtkSIMRecords6.isPhbReady(), false);
                    boolean unused2 = MtkSIMRecords.this.mPendingPhbNotify = false;
                }
            }
        }
    }

    private void updatePHBStatus(int status, boolean isSimLocked) {
        boolean simLockedState;
        mtkLog("[PHBStatus] status : " + status + " | isSimLocked : " + isSimLocked + " | mPhbReady : " + this.mPhbReady);
        if (SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild")) {
            simLockedState = false;
        } else {
            simLockedState = isSimLocked;
        }
        if (status == 1) {
            if (simLockedState) {
                mtkLog("phb ready but sim is not ready.");
            } else if (!this.mPhbReady) {
                this.mPhbReady = true;
                broadcastPhbStateChangedIntent(this.mPhbReady, false);
            }
        } else if (status == 0 && this.mPhbReady) {
            this.mAdnCache.reset();
            this.mPhbReady = false;
            broadcastPhbStateChangedIntent(this.mPhbReady, false);
        }
    }

    /* access modifiers changed from: protected */
    public void setVoiceCallForwardingFlagFromSimRecords() {
        int i = 1;
        if (checkEfCfis()) {
            this.mCallForwardingStatus = this.mEfCfis[1] & 1;
            mtkLog("EF_CFIS2: callForwardingEnabled=" + this.mCallForwardingStatus);
            this.mRecordsEventsRegistrants.notifyResult(1);
        } else if (this.mEfCff != null) {
            if ((this.mEfCff[0] & 15) != 10) {
                i = 0;
            }
            this.mCallForwardingStatus = i;
            mtkLog("EF_CFF2: callForwardingEnabled=" + this.mCallForwardingStatus);
            this.mRecordsEventsRegistrants.notifyResult(1);
        } else {
            this.mCallForwardingStatus = -1;
            mtkLog("EF_CFIS and EF_CFF not valid. callForwardingEnabled=" + this.mCallForwardingStatus);
            this.mRecordsEventsRegistrants.notifyResult(1);
        }
    }

    private String convertNumberIfContainsPrefix(String dialNumber) {
        if (dialNumber == null) {
            return dialNumber;
        }
        if (!dialNumber.startsWith("tel:") && !dialNumber.startsWith("sip:") && !dialNumber.startsWith("sips:")) {
            return dialNumber;
        }
        String r = dialNumber.substring(dialNumber.indexOf(":") + 1);
        Rlog.d("SIMRecords", "convertNumberIfContainsPrefix: dialNumber = " + dialNumber);
        return r;
    }

    public boolean isFetchRatBalancingAndEnsFile(int phoneId) {
        String strPropOperatorId = "persist.vendor.radio.sim.opid";
        boolean isFetch = false;
        if (phoneId > 0) {
            strPropOperatorId = strPropOperatorId + "_" + phoneId;
        }
        if (TextUtils.equals(SystemProperties.get(strPropOperatorId), "7") || TextUtils.equals(SystemProperties.get(strPropOperatorId), CRICKET_OPID)) {
            isFetch = true;
        }
        mtkLog("isFetchRatBalancingAndEnsFile is " + isFetch);
        return isFetch;
    }

    /* access modifiers changed from: protected */
    public void mtkLog(String s) {
        Rlog.d(LOG_TAG_EX, "[SIMRecords] " + s + " (slot " + this.mSlotId + ")");
    }

    /* access modifiers changed from: protected */
    public void mtkLoge(String s) {
        Rlog.e(LOG_TAG_EX, "[SIMRecords] " + s + " (slot " + this.mSlotId + ")");
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d("SIMRecords", "[SIMRecords] " + s + " (slot " + this.mSlotId + ")");
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e("SIMRecords", "[SIMRecords] " + s + " (slot " + this.mSlotId + ")");
    }

    /* access modifiers changed from: protected */
    public void logw(String s, Throwable tr) {
        Rlog.w("SIMRecords", "[SIMRecords] " + s + " (slot " + this.mSlotId + ")", tr);
    }

    /* access modifiers changed from: protected */
    public void logv(String s) {
        Rlog.v("SIMRecords", "[SIMRecords] " + s + " (slot " + this.mSlotId + ")");
    }

    public void onReady() {
        this.mLockedRecordsReqReason = 0;
        MtkSIMRecords.super.onReady();
    }

    /* access modifiers changed from: protected */
    public void onLocked(int msg) {
        int i;
        this.mRecordsRequested = false;
        this.mLoaded.set(false);
        if (this.mLockedRecordsReqReason != 0) {
            if (msg == 258) {
                i = 1;
            } else {
                i = 2;
            }
            this.mLockedRecordsReqReason = i;
            this.mRecordsToLoad++;
            onRecordLoaded();
            return;
        }
        MtkSIMRecords.super.onLocked(msg);
    }

    /* access modifiers changed from: protected */
    public void updateOperatorPlmn() {
        String imsi = getIMSI();
        if (imsi != null) {
            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && imsi.length() >= 6) {
                String mccmncCode = imsi.substring(0, 6);
                String[] strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (strArr[i].equals(mccmncCode)) {
                        this.mMncLength = 3;
                        log("IMSI: setting1 mMncLength=" + this.mMncLength);
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (this.mMncLength == -1 || this.mMncLength == 0) {
                try {
                    this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(imsi.substring(0, 3)));
                    log("setting2 mMncLength=" + this.mMncLength);
                } catch (NumberFormatException e) {
                    loge("Corrupt IMSI! setting3 mMncLength=" + this.mMncLength);
                }
            }
            if (this.mMncLength != 0 && this.mMncLength != -1 && imsi.length() >= this.mMncLength + 3) {
                log("update mccmnc=" + imsi.substring(0, this.mMncLength + 3));
                updateConfiguration(imsi.substring(0, this.mMncLength + 3));
            }
        }
    }

    public static String convertMccmncAsAospConfig(String mccmnc) {
        String result = null;
        if (mccmnc == null || mccmnc.length() != 6) {
            result = mccmnc;
        } else {
            String[] strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (strArr[i].equals(mccmnc)) {
                    result = mccmnc;
                    break;
                } else {
                    i++;
                }
            }
            if (result == null) {
                result = mccmnc.substring(0, 5);
            }
        }
        Rlog.d("SIMRecords", "convertMccmncAsAospConfig: mccmnc:" + mccmnc + " result:" + result);
        return result;
    }
}
