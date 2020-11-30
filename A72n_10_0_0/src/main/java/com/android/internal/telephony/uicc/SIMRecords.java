package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IOppoUiccManager;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SIMRecords extends AbstractSIMRecords {
    protected static final int CFF_LINE1_MASK = 15;
    protected static final int CFF_LINE1_RESET = 240;
    protected static final int CFF_UNCONDITIONAL_ACTIVE = 10;
    protected static final int CFF_UNCONDITIONAL_DEACTIVE = 5;
    protected static final int CFIS_ADN_CAPABILITY_ID_OFFSET = 14;
    protected static final int CFIS_ADN_EXTENSION_ID_OFFSET = 15;
    protected static final int CFIS_BCD_NUMBER_LENGTH_OFFSET = 2;
    protected static final int CFIS_TON_NPI_OFFSET = 3;
    private static final int CPHS_SST_MBN_ENABLED = 48;
    private static final int CPHS_SST_MBN_MASK = 48;
    private static final boolean CRASH_RIL = false;
    protected static final int EVENT_APP_LOCKED = 258;
    private static final int EVENT_APP_NETWORK_LOCKED = 259;
    protected static final int EVENT_GET_AD_DONE = 9;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    protected static final int EVENT_GET_CFF_DONE = 24;
    protected static final int EVENT_GET_CFIS_DONE = 32;
    protected static final int EVENT_GET_CPHS_MAILBOX_DONE = 11;
    protected static final int EVENT_GET_CSP_CPHS_DONE = 33;
    private static final int EVENT_GET_EHPLMN_DONE = 40;
    private static final int EVENT_GET_FPLMN_DONE = 41;
    private static final int EVENT_GET_GID1_DONE = 34;
    private static final int EVENT_GET_GID2_DONE = 36;
    private static final int EVENT_GET_HPLMN_W_ACT_DONE = 39;
    private static final int EVENT_GET_ICCID_DONE = 4;
    protected static final int EVENT_GET_IMSI_DONE = 3;
    protected static final int EVENT_GET_INFO_CPHS_DONE = 26;
    protected static final int EVENT_GET_MBDN_DONE = 6;
    protected static final int EVENT_GET_MBI_DONE = 5;
    protected static final int EVENT_GET_MSISDN_DONE = 10;
    protected static final int EVENT_GET_MWIS_DONE = 7;
    private static final int EVENT_GET_OPLMN_W_ACT_DONE = 38;
    private static final int EVENT_GET_PLMN_W_ACT_DONE = 37;
    protected static final int EVENT_GET_PNN_DONE = 15;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SPDI_DONE = 13;
    protected static final int EVENT_GET_SPN_DONE = 12;
    protected static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE = 8;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SET_CPHS_MAILBOX_DONE = 25;
    private static final int EVENT_SET_MBDN_DONE = 20;
    protected static final int EVENT_SET_MSISDN_DONE = 30;
    private static final int EVENT_SMS_ON_SIM = 21;
    protected static final int EVENT_UPDATE_DONE = 14;
    protected static final String LOG_TAG = "SIMRecords";
    private static final int SIM_RECORD_EVENT_BASE = 0;
    private static final int SYSTEM_EVENT_BASE = 256;
    protected static final int TAG_FULL_NETWORK_NAME = 67;
    protected static final int TAG_SHORT_NETWORK_NAME = 69;
    protected static final int TAG_SPDI = 163;
    protected static final int TAG_SPDI_PLMN_LIST = 128;
    private static final boolean VDBG = false;
    protected int mCallForwardingStatus;
    protected byte[] mCphsInfo = null;
    protected boolean mCspPlmnEnabled = true;
    @UnsupportedAppUsage
    byte[] mEfCPHS_MWI = null;
    @UnsupportedAppUsage
    protected byte[] mEfCff = null;
    @UnsupportedAppUsage
    protected byte[] mEfCfis = null;
    @UnsupportedAppUsage
    byte[] mEfLi = null;
    @UnsupportedAppUsage
    byte[] mEfMWIS = null;
    @UnsupportedAppUsage
    byte[] mEfPl = null;
    protected GetSpnFsmState mSpnState;
    @UnsupportedAppUsage
    protected UsimServiceTable mUsimServiceTable;
    @UnsupportedAppUsage
    protected VoiceMailConstants mVmConfig;

    /* access modifiers changed from: protected */
    public enum GetSpnFsmState {
        IDLE,
        INIT,
        READ_SPN_3GPP,
        READ_SPN_CPHS,
        READ_SPN_SHORT_CPHS
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String toString() {
        return "SimRecords: " + super.toString() + " mVmConfig" + this.mVmConfig + " callForwardingEnabled=" + this.mCallForwardingStatus + " spnState=" + this.mSpnState + " mCphsInfo=" + this.mCphsInfo + " mCspPlmnEnabled=" + this.mCspPlmnEnabled + " efMWIS=" + this.mEfMWIS + " efCPHS_MWI=" + this.mEfCPHS_MWI + " mEfCff=" + this.mEfCff + " mEfCfis=" + this.mEfCfis + " getOperatorNumeric=" + getOperatorNumeric();
    }

    public SIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mAdnCache = new AdnRecordCache(this.mFh);
        this.mVmConfig = new VoiceMailConstants();
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mRecordsToLoad = 0;
        this.mCi.setOnSmsOnSim(this, 21, null);
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, EVENT_APP_LOCKED, null);
        this.mParentApp.registerForNetworkLocked(this, EVENT_APP_NETWORK_LOCKED, null);
        log("SIMRecords X ctor this=" + this);
    }

    @Override // com.android.internal.telephony.uicc.AbstractSIMRecords, com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.AbstractBaseRecords
    public void dispose() {
        log("Disposing SIMRecords this=" + this);
        this.mCi.unSetOnSmsOnSim(this);
        this.mParentApp.unregisterForReady(this);
        this.mParentApp.unregisterForLocked(this);
        this.mParentApp.unregisterForNetworkLocked(this);
        resetRecords();
        super.dispose();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        log("finalized");
    }

    /* access modifiers changed from: protected */
    public void resetRecords() {
        this.mImsi = null;
        this.mMsisdn = null;
        this.mVoiceMailNum = null;
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mCarrierNameDisplayCondition = 0;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mSpdi = null;
        this.mPnnHomeName = null;
        this.mGid1 = null;
        this.mGid2 = null;
        this.mPlmnActRecords = null;
        this.mOplmnActRecords = null;
        this.mHplmnActRecords = null;
        this.mFplmns = null;
        this.mEhplmns = null;
        resetImpi();
        this.mAdnCache.reset();
        log("SIMRecords: onRadioOffOrNotAvailable set 'gsm.sim.operator.numeric' to operator=null");
        log("update icc_operator_numeric=" + ((Object) null));
        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), PhoneConfigurationManager.SSSS);
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), PhoneConfigurationManager.SSSS);
        this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), PhoneConfigurationManager.SSSS);
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mLoaded.set(false);
        resetMvnoState();
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public String getMsisdnNumber() {
        return this.mMsisdn;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public UsimServiceTable getUsimServiceTable() {
        return this.mUsimServiceTable;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public int getExtFromEf(int ef) {
        if (ef == 28480 && this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
            return IccConstants.EF_EXT5;
        }
        return IccConstants.EF_EXT1;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        this.mNewMsisdn = number;
        this.mNewMsisdnTag = alphaTag;
        log("Set MSISDN: " + this.mNewMsisdnTag + " " + Rlog.pii(LOG_TAG, this.mNewMsisdn));
        new AdnRecordLoader(this.mFh).updateEF(new AdnRecord(this.mNewMsisdnTag, this.mNewMsisdn), IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, null, obtainMessage(30, onComplete));
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String getMsisdnAlphaTag() {
        return this.mMsisdnTag;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public String getVoiceMailNumber() {
        return this.mVoiceMailNum;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
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

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setVoiceMessageWaiting(int line, int countWaiting) {
        if (line == 1) {
            try {
                if (this.mEfMWIS != null) {
                    this.mEfMWIS[0] = (byte) ((this.mEfMWIS[0] & 254) | (countWaiting == 0 ? 0 : 1));
                    if (countWaiting < 0) {
                        this.mEfMWIS[1] = 0;
                    } else {
                        this.mEfMWIS[1] = (byte) countWaiting;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_MWIS, 1, this.mEfMWIS, null, obtainMessage(14, IccConstants.EF_MWIS, 0));
                }
                if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
                    log("[setVoiceMessageWaiting] It is USIM card, skip write CPHS file");
                } else if (this.mEfCPHS_MWI != null) {
                    this.mEfCPHS_MWI[0] = (byte) ((this.mEfCPHS_MWI[0] & 240) | (countWaiting == 0 ? 5 : 10));
                    this.mFh.updateEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, this.mEfCPHS_MWI, obtainMessage(14, Integer.valueOf((int) IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving voice mail state to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean validEfCfis(byte[] data) {
        if (data != null) {
            if (data[0] < 1 || data[0] > 4) {
                logw("MSP byte: " + ((int) data[0]) + " is not between 1 and 4", null);
            }
            for (byte b : data) {
                if (b != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getVoiceMessageCount() {
        int countVoiceMessages = -2;
        byte[] bArr = this.mEfMWIS;
        boolean voiceMailWaiting = false;
        if (bArr != null) {
            if ((bArr[0] & 1) != 0) {
                voiceMailWaiting = true;
            }
            countVoiceMessages = this.mEfMWIS[1] & 255;
            if (voiceMailWaiting && (countVoiceMessages == 0 || countVoiceMessages == 255)) {
                countVoiceMessages = -1;
            }
            log(" VoiceMessageCount from SIM MWIS = " + countVoiceMessages);
        } else {
            byte[] bArr2 = this.mEfCPHS_MWI;
            if (bArr2 != null) {
                int indicator = bArr2[0] & 15;
                if (indicator == 10) {
                    countVoiceMessages = -1;
                } else if (indicator == 5) {
                    countVoiceMessages = 0;
                }
                log(" VoiceMessageCount from SIM CPHS = " + countVoiceMessages);
            }
        }
        return countVoiceMessages;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getVoiceCallForwardingFlag() {
        return this.mCallForwardingStatus;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void setVoiceCallForwardingFlag(int line, boolean enable, String dialNumber) {
        int i;
        if (line == 1) {
            if (enable) {
                i = 1;
            } else {
                i = 0;
            }
            this.mCallForwardingStatus = i;
            this.mRecordsEventsRegistrants.notifyResult(1);
            try {
                if (validEfCfis(this.mEfCfis)) {
                    if (enable) {
                        byte[] bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] | 1);
                    } else {
                        byte[] bArr2 = this.mEfCfis;
                        bArr2[1] = (byte) (bArr2[1] & 254);
                    }
                    log("setVoiceCallForwardingFlag: enable=" + enable + " mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                    if (enable && !TextUtils.isEmpty(dialNumber)) {
                        logv("EF_CFIS: updating cf number, " + Rlog.pii(LOG_TAG, dialNumber));
                        byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(dialNumber, 1);
                        System.arraycopy(bcdNumber, 0, this.mEfCfis, 3, bcdNumber.length);
                        this.mEfCfis[2] = (byte) bcdNumber.length;
                        this.mEfCfis[14] = -1;
                        this.mEfCfis[15] = -1;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_CFIS, 1, this.mEfCfis, null, obtainMessage(14, Integer.valueOf((int) IccConstants.EF_CFIS)));
                } else {
                    log("setVoiceCallForwardingFlag: ignoring enable=" + enable + " invalid mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                }
                if (this.mEfCff != null) {
                    if (enable) {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 10);
                    } else {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 5);
                    }
                    this.mFh.updateEFTransparent(IccConstants.EF_CFF_CPHS, this.mEfCff, obtainMessage(14, Integer.valueOf((int) IccConstants.EF_CFF_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving call forwarding flag to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchSimRecords();
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
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

    /* JADX WARNING: Removed duplicated region for block: B:252:0x0923  */
    @Override // com.android.internal.telephony.uicc.AbstractSIMRecords, com.android.internal.telephony.uicc.IccRecords
    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse;
        Throwable th;
        boolean isRecordLoadResponse2 = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "]  while being destroyed. Ignoring.");
            return;
        }
        try {
            int i = msg.what;
            boolean z = true;
            if (i == 1) {
                onReady();
            } else if (i != 30) {
                if (i != EVENT_APP_LOCKED && i != EVENT_APP_NETWORK_LOCKED) {
                    switch (i) {
                        case 3:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar = (AsyncResult) msg.obj;
                            if (ar.exception == null) {
                                setImsi((String) ar.result);
                                super.setMvnoDBState(IccRecords.oemMvnoParamState(this.mPhone.getContext(), getOperatorNumeric()));
                                break;
                            } else {
                                loge("Exception querying IMSI, Exception:" + ar.exception);
                                break;
                            }
                        case 4:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar2 = (AsyncResult) msg.obj;
                            byte[] data = (byte[]) ar2.result;
                            if (ar2.exception == null) {
                                this.mIccId = IccUtils.bcdToString(data, 0, data.length);
                                this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                                log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                                if (this.mIccId == null || this.mIccId.equals(PhoneConfigurationManager.SSSS)) {
                                    this.mIccId = "ffffffffffffffffffff";
                                }
                                setMvnoState(IccRecords.MvnoType.ICCID);
                                break;
                            }
                            break;
                        case 5:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar3 = (AsyncResult) msg.obj;
                            byte[] data2 = (byte[]) ar3.result;
                            boolean isValidMbdn = false;
                            if (ar3.exception == null) {
                                log("EF_MBI: " + IccUtils.bytesToHexString(data2));
                                this.mMailboxIndex = data2[0] & 255;
                                if (!(this.mMailboxIndex == 0 || this.mMailboxIndex == 255)) {
                                    log("Got valid mailbox number for MBDN");
                                    isValidMbdn = true;
                                }
                            }
                            this.mRecordsToLoad++;
                            if (!isValidMbdn) {
                                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                                break;
                            } else {
                                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, obtainMessage(6));
                                break;
                            }
                        case 6:
                        case 11:
                            this.mVoiceMailNum = null;
                            this.mVoiceMailTag = null;
                            isRecordLoadResponse2 = true;
                            AsyncResult ar4 = (AsyncResult) msg.obj;
                            if (ar4.exception == null) {
                                AdnRecord adn = (AdnRecord) ar4.result;
                                StringBuilder sb = new StringBuilder();
                                sb.append("VM: ");
                                sb.append(adn);
                                sb.append(msg.what == 11 ? " EF[MAILBOX]" : " EF[MBDN]");
                                log(sb.toString());
                                if (adn.isEmpty() && msg.what == 6) {
                                    this.mRecordsToLoad++;
                                    new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                                    break;
                                } else {
                                    this.mVoiceMailNum = adn.getNumber();
                                    this.mVoiceMailTag = adn.getAlphaTag();
                                    break;
                                }
                            } else {
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("Invalid or missing EF");
                                sb2.append(msg.what == 11 ? "[MAILBOX]" : "[MBDN]");
                                log(sb2.toString());
                                if (msg.what == 6) {
                                    this.mRecordsToLoad++;
                                    new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                                    break;
                                }
                            }
                            break;
                        case 7:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar5 = (AsyncResult) msg.obj;
                            byte[] data3 = (byte[]) ar5.result;
                            log("EF_MWIS : " + IccUtils.bytesToHexString(data3));
                            if (ar5.exception == null) {
                                if ((data3[0] & 255) != 255) {
                                    this.mEfMWIS = data3;
                                    break;
                                } else {
                                    log("SIMRecords: Uninitialized record MWIS");
                                    break;
                                }
                            } else {
                                log("EVENT_GET_MWIS_DONE exception = " + ar5.exception);
                                break;
                            }
                        case 8:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar6 = (AsyncResult) msg.obj;
                            byte[] data4 = (byte[]) ar6.result;
                            log("EF_CPHS_MWI: " + IccUtils.bytesToHexString(data4));
                            if (ar6.exception == null) {
                                this.mEfCPHS_MWI = data4;
                                break;
                            } else {
                                log("EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE exception = " + ar6.exception);
                                break;
                            }
                        case 9:
                            isRecordLoadResponse2 = true;
                            this.mMncLength = 0;
                            try {
                                if (!this.mCarrierTestOverride.isInTestMode()) {
                                    AsyncResult ar7 = (AsyncResult) msg.obj;
                                    byte[] data5 = (byte[]) ar7.result;
                                    if (ar7.exception == null) {
                                        log("EF_AD: " + IccUtils.bytesToHexString(data5));
                                        if (data5.length < 3) {
                                            log("Corrupt AD data on SIM");
                                        } else if (data5.length == 3) {
                                            log("MNC length not present in EF_AD");
                                        } else {
                                            int len = data5[3] & 15;
                                            if (!this.mIsTestCard && !OemConstant.isTestCard(this.mContext, data5[0])) {
                                                z = false;
                                            }
                                            this.mIsTestCard = z;
                                            log("leon mIsTestCard 2: " + this.mIsTestCard);
                                            if (len == 2 || len == 3) {
                                                this.mMncLength = len;
                                            } else {
                                                log("Received invalid or unset MNC Length=" + len);
                                            }
                                        }
                                    }
                                    break;
                                }
                                updateOperatorPlmn();
                                break;
                            } finally {
                                updateOperatorPlmn();
                            }
                        case 10:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar8 = (AsyncResult) msg.obj;
                            if (ar8.exception == null) {
                                AdnRecord adn2 = (AdnRecord) ar8.result;
                                this.mMsisdn = adn2.getNumber();
                                this.mMsisdnTag = adn2.getAlphaTag();
                                log("MSISDN: " + Rlog.pii(LOG_TAG, this.mMsisdn));
                                break;
                            } else {
                                log("Invalid or missing EF[MSISDN]");
                                break;
                            }
                        case 12:
                            isRecordLoadResponse2 = true;
                            getSpnFsm(false, (AsyncResult) msg.obj);
                            break;
                        case 13:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar9 = (AsyncResult) msg.obj;
                            byte[] data6 = (byte[]) ar9.result;
                            if (ar9.exception == null) {
                                parseEfSpdi(data6);
                                break;
                            }
                            break;
                        case 14:
                            AsyncResult ar10 = (AsyncResult) msg.obj;
                            if (ar10.exception != null) {
                                logw("update failed. ", ar10.exception);
                                break;
                            }
                            break;
                        case 15:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar11 = (AsyncResult) msg.obj;
                            byte[] data7 = (byte[]) ar11.result;
                            if (ar11.exception == null) {
                                SimTlv tlv = new SimTlv(data7, 0, data7.length);
                                while (true) {
                                    if (!tlv.isValidObject()) {
                                        break;
                                    } else if (tlv.getTag() == 67) {
                                        this.mPnnHomeName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                                        log("PNN: " + this.mPnnHomeName);
                                        break;
                                    } else {
                                        tlv.nextObject();
                                    }
                                }
                            }
                            break;
                        default:
                            switch (i) {
                                case 17:
                                    isRecordLoadResponse2 = true;
                                    AsyncResult ar12 = (AsyncResult) msg.obj;
                                    byte[] data8 = (byte[]) ar12.result;
                                    if (ar12.exception == null) {
                                        this.mUsimServiceTable = new UsimServiceTable(data8);
                                        log("SST: " + this.mUsimServiceTable);
                                        break;
                                    }
                                    break;
                                case 18:
                                    isRecordLoadResponse2 = true;
                                    AsyncResult ar13 = (AsyncResult) msg.obj;
                                    if (ar13.exception == null) {
                                        handleSmses((ArrayList) ar13.result);
                                        break;
                                    }
                                    break;
                                case 19:
                                    Rlog.i("ENF", "marked read: sms " + msg.arg1);
                                    break;
                                case 20:
                                    isRecordLoadResponse2 = false;
                                    AsyncResult ar14 = (AsyncResult) msg.obj;
                                    log("EVENT_SET_MBDN_DONE ex:" + ar14.exception);
                                    if (ar14.exception == null) {
                                        this.mVoiceMailNum = this.mNewVoiceMailNum;
                                        this.mVoiceMailTag = this.mNewVoiceMailTag;
                                    }
                                    if (!isCphsMailboxEnabled()) {
                                        if (ar14.userObj != null) {
                                            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
                                            if (ar14.exception == null || configManager == null) {
                                                AsyncResult.forMessage((Message) ar14.userObj).exception = ar14.exception;
                                            } else {
                                                PersistableBundle b = configManager.getConfigForSubId(SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mParentApp.getPhoneId()));
                                                if (b == null || !b.getBoolean("editable_voicemail_number_bool")) {
                                                    AsyncResult.forMessage((Message) ar14.userObj).exception = ar14.exception;
                                                } else {
                                                    AsyncResult.forMessage((Message) ar14.userObj).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
                                                }
                                            }
                                            ((Message) ar14.userObj).sendToTarget();
                                            break;
                                        }
                                    } else {
                                        AdnRecord adn3 = new AdnRecord(this.mVoiceMailTag, this.mVoiceMailNum);
                                        Message onCphsCompleted = (Message) ar14.userObj;
                                        if (ar14.exception == null && ar14.userObj != null) {
                                            AsyncResult.forMessage((Message) ar14.userObj).exception = null;
                                            ((Message) ar14.userObj).sendToTarget();
                                            log("Callback with MBDN successful.");
                                            onCphsCompleted = null;
                                        }
                                        new AdnRecordLoader(this.mFh).updateEF(adn3, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onCphsCompleted));
                                        break;
                                    }
                                    break;
                                case 21:
                                    isRecordLoadResponse2 = false;
                                    AsyncResult ar15 = (AsyncResult) msg.obj;
                                    Integer index = (Integer) ar15.result;
                                    if (ar15.exception == null && index != null) {
                                        log("READ EF_SMS RECORD index=" + index);
                                        this.mFh.loadEFLinearFixed(IccConstants.EF_SMS, index.intValue(), obtainMessage(22));
                                        break;
                                    } else {
                                        loge("Error on SMS_ON_SIM with exp " + ar15.exception + " index " + index);
                                        break;
                                    }
                                case 22:
                                    isRecordLoadResponse2 = false;
                                    AsyncResult ar16 = (AsyncResult) msg.obj;
                                    if (ar16.exception != null) {
                                        loge("Error on GET_SMS with exp " + ar16.exception);
                                        break;
                                    } else {
                                        handleSms((byte[]) ar16.result);
                                        break;
                                    }
                                default:
                                    switch (i) {
                                        case 24:
                                            isRecordLoadResponse2 = true;
                                            AsyncResult ar17 = (AsyncResult) msg.obj;
                                            byte[] data9 = (byte[]) ar17.result;
                                            if (ar17.exception == null) {
                                                log("EF_CFF_CPHS: " + IccUtils.bytesToHexString(data9));
                                                this.mEfCff = data9;
                                                break;
                                            } else {
                                                this.mEfCff = null;
                                                break;
                                            }
                                        case 25:
                                            isRecordLoadResponse2 = false;
                                            AsyncResult ar18 = (AsyncResult) msg.obj;
                                            if (ar18.exception == null) {
                                                this.mVoiceMailNum = this.mNewVoiceMailNum;
                                                this.mVoiceMailTag = this.mNewVoiceMailTag;
                                            } else {
                                                log("Set CPHS MailBox with exception: " + ar18.exception);
                                            }
                                            if (ar18.userObj != null) {
                                                log("Callback with CPHS MB successful.");
                                                AsyncResult.forMessage((Message) ar18.userObj).exception = ar18.exception;
                                                ((Message) ar18.userObj).sendToTarget();
                                                break;
                                            }
                                            break;
                                        case 26:
                                            isRecordLoadResponse2 = true;
                                            AsyncResult ar19 = (AsyncResult) msg.obj;
                                            if (ar19.exception == null) {
                                                this.mCphsInfo = (byte[]) ar19.result;
                                                log("iCPHS: " + IccUtils.bytesToHexString(this.mCphsInfo));
                                                break;
                                            }
                                            break;
                                        default:
                                            switch (i) {
                                                case 32:
                                                    isRecordLoadResponse2 = true;
                                                    AsyncResult ar20 = (AsyncResult) msg.obj;
                                                    byte[] data10 = (byte[]) ar20.result;
                                                    if (ar20.exception == null) {
                                                        log("EF_CFIS: " + IccUtils.bytesToHexString(data10));
                                                        this.mEfCfis = data10;
                                                        break;
                                                    } else {
                                                        this.mEfCfis = null;
                                                        break;
                                                    }
                                                case 33:
                                                    isRecordLoadResponse2 = true;
                                                    AsyncResult ar21 = (AsyncResult) msg.obj;
                                                    if (ar21.exception == null) {
                                                        byte[] data11 = (byte[]) ar21.result;
                                                        log("EF_CSP: " + IccUtils.bytesToHexString(data11));
                                                        handleEfCspData(data11);
                                                        break;
                                                    } else {
                                                        loge("Exception in fetching EF_CSP data " + ar21.exception);
                                                        break;
                                                    }
                                                case 34:
                                                    isRecordLoadResponse2 = true;
                                                    AsyncResult ar22 = (AsyncResult) msg.obj;
                                                    byte[] data12 = (byte[]) ar22.result;
                                                    DcTracker dcTracker = this.mPhone.getDcTracker(1);
                                                    boolean isNewSim = false;
                                                    if (dcTracker != null) {
                                                        isNewSim = dcTracker.informNewSimCardLoaded(this.mPhone.getPhoneId());
                                                        log("isNewSim: " + isNewSim);
                                                    }
                                                    if (ar22.exception == null) {
                                                        this.mGid1 = IccUtils.bytesToHexString(data12);
                                                        log("GID1: " + this.mGid1);
                                                        if (isNewSim) {
                                                            super.setDataAndDataroamingForOperators(this.mGid1);
                                                        }
                                                        setMvnoState(IccRecords.MvnoType.GID);
                                                        break;
                                                    } else {
                                                        loge("Exception in get GID1 " + ar22.exception);
                                                        this.mGid1 = null;
                                                        if (isNewSim) {
                                                            super.setDataAndDataroamingForOperators(this.mGid1);
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case 36:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar23 = (AsyncResult) msg.obj;
                                                            byte[] data13 = (byte[]) ar23.result;
                                                            if (ar23.exception == null) {
                                                                this.mGid2 = IccUtils.bytesToHexString(data13);
                                                                log("GID2: " + this.mGid2);
                                                                break;
                                                            } else {
                                                                loge("Exception in get GID2 " + ar23.exception);
                                                                this.mGid2 = null;
                                                                break;
                                                            }
                                                        case 37:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar24 = (AsyncResult) msg.obj;
                                                            byte[] data14 = (byte[]) ar24.result;
                                                            if (ar24.exception == null && data14 != null) {
                                                                log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data14));
                                                                this.mPlmnActRecords = PlmnActRecord.getRecords(data14);
                                                                break;
                                                            } else {
                                                                loge("Failed getting User PLMN with Access Tech Records: " + ar24.exception);
                                                                break;
                                                            }
                                                        case 38:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar25 = (AsyncResult) msg.obj;
                                                            byte[] data15 = (byte[]) ar25.result;
                                                            if (ar25.exception == null && data15 != null) {
                                                                log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data15));
                                                                this.mOplmnActRecords = PlmnActRecord.getRecords(data15);
                                                                break;
                                                            } else {
                                                                loge("Failed getting Operator PLMN with Access Tech Records: " + ar25.exception);
                                                                break;
                                                            }
                                                        case 39:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar26 = (AsyncResult) msg.obj;
                                                            byte[] data16 = (byte[]) ar26.result;
                                                            if (ar26.exception == null && data16 != null) {
                                                                log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data16));
                                                                this.mHplmnActRecords = PlmnActRecord.getRecords(data16);
                                                                log("HplmnActRecord[]=" + Arrays.toString(this.mHplmnActRecords));
                                                                break;
                                                            } else {
                                                                loge("Failed getting Home PLMN with Access Tech Records: " + ar26.exception);
                                                                break;
                                                            }
                                                        case 40:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar27 = (AsyncResult) msg.obj;
                                                            byte[] data17 = (byte[]) ar27.result;
                                                            if (ar27.exception == null && data17 != null) {
                                                                this.mEhplmns = parseBcdPlmnList(data17, "Equivalent Home");
                                                                break;
                                                            } else {
                                                                loge("Failed getting Equivalent Home PLMNs: " + ar27.exception);
                                                                break;
                                                            }
                                                        case 41:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar28 = (AsyncResult) msg.obj;
                                                            byte[] data18 = (byte[]) ar28.result;
                                                            if (ar28.exception == null && data18 != null) {
                                                                this.mFplmns = parseBcdPlmnList(data18, "Forbidden");
                                                                if (msg.arg1 == 1238273) {
                                                                    isRecordLoadResponse2 = false;
                                                                    Message response = retrievePendingResponseMessage(Integer.valueOf(msg.arg2));
                                                                    if (response == null) {
                                                                        loge("Failed to retrieve a response message for FPLMN");
                                                                        break;
                                                                    } else {
                                                                        AsyncResult.forMessage(response, Arrays.copyOf(this.mFplmns, this.mFplmns.length), (Throwable) null);
                                                                        response.sendToTarget();
                                                                        break;
                                                                    }
                                                                }
                                                            } else {
                                                                loge("Failed getting Forbidden PLMNs: " + ar28.exception);
                                                                break;
                                                            }
                                                            break;
                                                        default:
                                                            super.handleMessage(msg);
                                                            break;
                                                    }
                                            }
                                    }
                            }
                    }
                } else {
                    onLocked(msg.what);
                }
            } else {
                isRecordLoadResponse2 = false;
                AsyncResult ar29 = (AsyncResult) msg.obj;
                if (ar29.exception == null) {
                    this.mMsisdn = this.mNewMsisdn;
                    this.mMsisdnTag = this.mNewMsisdnTag;
                    log("Success to update EF[MSISDN]");
                }
                if (ar29.userObj != null) {
                    AsyncResult.forMessage((Message) ar29.userObj).exception = ar29.exception;
                    ((Message) ar29.userObj).sendToTarget();
                }
            }
            if (isRecordLoadResponse2) {
                onRecordLoaded();
            }
        } catch (RuntimeException exc) {
            isRecordLoadResponse = false;
            logw("Exception parsing SIM record", exc);
            if (0 != 0) {
                onRecordLoaded();
            }
        } catch (Throwable th2) {
            th = th2;
            if (isRecordLoadResponse) {
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public class EfPlLoaded implements IccRecords.IccRecordLoaded {
        private EfPlLoaded() {
        }

        /* synthetic */ EfPlLoaded(SIMRecords x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_PL";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfPl = (byte[]) ar.result;
            SIMRecords sIMRecords = SIMRecords.this;
            sIMRecords.log("EF_PL=" + IccUtils.bytesToHexString(SIMRecords.this.mEfPl));
        }
    }

    /* access modifiers changed from: private */
    public class EfUsimLiLoaded implements IccRecords.IccRecordLoaded {
        private EfUsimLiLoaded() {
        }

        /* synthetic */ EfUsimLiLoaded(SIMRecords x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_LI";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfLi = (byte[]) ar.result;
            SIMRecords sIMRecords = SIMRecords.this;
            sIMRecords.log("EF_LI=" + IccUtils.bytesToHexString(SIMRecords.this.mEfLi));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void handleFileUpdate(int efid) {
        if (efid != 28435) {
            if (efid == 28437) {
                this.mRecordsToLoad++;
                log("[CSP] SIM Refresh for EF_CSP_CPHS");
                this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
                return;
            } else if (efid == 28439) {
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                return;
            } else if (efid == 28475) {
                log("SIM Refresh called for EF_FDN");
                this.mParentApp.queryFdn();
                this.mAdnCache.reset();
                return;
            } else if (efid == 28480) {
                this.mRecordsToLoad++;
                log("SIM Refresh called for EF_MSISDN");
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
                return;
            } else if (efid == 28615) {
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, obtainMessage(6));
                return;
            } else if (efid != 28619) {
                this.mAdnCache.reset();
                fetchSimRecords();
                return;
            }
        }
        log("SIM Refresh called for EF_CFIS or EF_CFF_CPHS");
        loadCallForwardingRecords();
    }

    private int dispatchGsmMessage(SmsMessage message) {
        this.mNewSmsRegistrants.notifyResult(message);
        return 0;
    }

    private void handleSms(byte[] ba) {
        if (ba[0] != 0) {
            Rlog.d("ENF", "status : " + ((int) ba[0]));
        }
        if (ba[0] == 3) {
            int n = ba.length;
            byte[] pdu = new byte[(n - 1)];
            System.arraycopy(ba, 1, pdu, 0, n - 1);
            dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
        }
    }

    private void handleSmses(ArrayList<byte[]> messages) {
        int count = messages.size();
        for (int i = 0; i < count; i++) {
            byte[] ba = messages.get(i);
            if (ba[0] != 0) {
                Rlog.i("ENF", "status " + i + ": " + ((int) ba[0]));
            }
            if (ba[0] == 3) {
                int n = ba.length;
                byte[] pdu = new byte[(n - 1)];
                System.arraycopy(ba, 1, pdu, 0, n - 1);
                dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
                ba[0] = 1;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        if (getRecordsLoaded()) {
            onAllRecordsLoaded();
        } else if (getLockedRecordsLoaded() || getNetworkLockedRecordsLoaded()) {
            onLockedAllRecordsLoaded();
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setVoiceCallForwardingFlagFromSimRecords() {
        int i = 1;
        if (validEfCfis(this.mEfCfis)) {
            this.mCallForwardingStatus = this.mEfCfis[1] & 1;
            log("EF_CFIS: callForwardingEnabled=" + this.mCallForwardingStatus);
            return;
        }
        byte[] bArr = this.mEfCff;
        if (bArr != null) {
            if ((bArr[0] & 15) != 10) {
                i = 0;
            }
            this.mCallForwardingStatus = i;
            log("EF_CFF: callForwardingEnabled=" + this.mCallForwardingStatus);
            return;
        }
        this.mCallForwardingStatus = -1;
        log("EF_CFIS and EF_CFF not valid. callForwardingEnabled=" + this.mCallForwardingStatus);
    }

    /* access modifiers changed from: protected */
    public void setSimLanguageFromEF() {
        if (Resources.getSystem().getBoolean(17891565)) {
            setSimLanguage(this.mEfLi, this.mEfPl);
        } else {
            log("Not using EF LI/EF PL");
        }
    }

    private void onLockedAllRecordsLoaded() {
        setSimLanguageFromEF();
        setVoiceCallForwardingFlagFromSimRecords();
        if (this.mLockedRecordsReqReason == 1) {
            this.mLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        } else if (this.mLockedRecordsReqReason == 2) {
            this.mNetworkLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        } else {
            loge("onLockedAllRecordsLoaded: unexpected mLockedRecordsReqReason " + this.mLockedRecordsReqReason);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onAllRecordsLoaded() {
        log("record load complete");
        setSimLanguageFromEF();
        setVoiceCallForwardingFlagFromSimRecords();
        String operator = getOperatorNumeric();
        if (TextUtils.isEmpty(operator) || !checkCdma3gCard()) {
            log("onAllRecordsLoaded empty 'gsm.sim.operator.numeric' skipping");
        } else {
            log("onAllRecordsLoaded set 'gsm.sim.operator.numeric' to operator='" + operator + "'");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operator);
        }
        String imsi = getIMSI();
        if (TextUtils.isEmpty(imsi) || imsi.length() < 3 || !checkCdma3gCard()) {
            log("onAllRecordsLoaded empty imsi skipping setting mcc");
        } else {
            log("onAllRecordsLoaded set mcc imsi" + PhoneConfigurationManager.SSSS);
            this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), MccTable.countryCodeForMcc(imsi.substring(0, 3)));
        }
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp")) {
            this.mContext.sendBroadcast(new Intent("android.intent.action.INSERT_TEST_SIM"));
        }
        setVoiceMailByCountry(operator);
        this.mLoaded.set(true);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
    }

    /* access modifiers changed from: protected */
    public void setVoiceMailByCountry(String spn) {
        if (this.mVmConfig.containsCarrier(spn)) {
            this.mIsVoiceMailFixed = true;
            this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn);
            this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn);
        }
    }

    public void getForbiddenPlmns(Message response) {
        this.mFh.loadEFTransparent(IccConstants.EF_FPLMN, obtainMessage(41, 1238273, storePendingResponseMessage(response)));
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onReady() {
        fetchSimRecords();
    }

    /* access modifiers changed from: protected */
    public void onLocked(int msg) {
        int i;
        log("only fetch EF_LI, EF_PL and EF_ICCID in locked state");
        if (msg == EVENT_APP_LOCKED) {
            i = 1;
        } else {
            i = 2;
        }
        this.mLockedRecordsReqReason = i;
        loadEfLiAndEfPl();
        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(4));
        this.mRecordsToLoad++;
    }

    private void loadEfLiAndEfPl() {
        if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
            this.mFh.loadEFTransparent(IccConstants.EF_LI, obtainMessage(100, new EfUsimLiLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded(this, null)));
            this.mRecordsToLoad++;
        }
    }

    /* access modifiers changed from: protected */
    public void loadCallForwardingRecords() {
        this.mRecordsRequested = true;
        this.mFh.loadEFLinearFixed(IccConstants.EF_CFIS, 1, obtainMessage(32));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CFF_CPHS, obtainMessage(24));
        this.mRecordsToLoad++;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void fetchSimRecords() {
        this.mRecordsRequested = true;
        resetMvnoState();
        log("fetchSimRecords " + this.mRecordsToLoad);
        IOppoUiccManager oppoUiccManager = (IOppoUiccManager) OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, this);
        if (oppoUiccManager != null) {
            oppoUiccManager.enableHypnusAction();
        }
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
        fetchIMPI();
        log("fetchSimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getCarrierNameDisplayCondition() {
        return this.mCarrierNameDisplayCondition;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void getSpnFsm(boolean start, AsyncResult ar) {
        if (start) {
            if (this.mSpnState == GetSpnFsmState.READ_SPN_3GPP || this.mSpnState == GetSpnFsmState.READ_SPN_CPHS || this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS || this.mSpnState == GetSpnFsmState.INIT) {
                this.mSpnState = GetSpnFsmState.INIT;
                return;
            }
            this.mSpnState = GetSpnFsmState.INIT;
        }
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[this.mSpnState.ordinal()];
        if (i == 1) {
            setServiceProviderName(null);
            this.mFh.loadEFTransparent(IccConstants.EF_SPN, obtainMessage(12));
            this.mRecordsToLoad++;
            this.mSpnState = GetSpnFsmState.READ_SPN_3GPP;
        } else if (i == 2) {
            if (ar == null || ar.exception != null) {
                this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
            } else {
                byte[] data = (byte[]) ar.result;
                this.mCarrierNameDisplayCondition = convertSpnDisplayConditionToBitmask(data[0] & 255);
                setServiceProviderName(IccUtils.adnStringFieldToString(data, 1, data.length - 1));
                String spn = getServiceProviderName();
                if (spn == null || spn.length() == 0) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                } else {
                    log("Load EF_SPN: " + spn + " carrierNameDisplayCondition: " + this.mCarrierNameDisplayCondition);
                    oppoSetSimSpn(spn);
                    this.mSpnState = GetSpnFsmState.IDLE;
                    setMvnoState(IccRecords.MvnoType.SPN);
                }
            }
            if (this.mSpnState == GetSpnFsmState.READ_SPN_CPHS) {
                this.mFh.loadEFTransparent(IccConstants.EF_SPN_CPHS, obtainMessage(12));
                this.mRecordsToLoad++;
                this.mCarrierNameDisplayCondition = 0;
            }
        } else if (i == 3) {
            if (ar == null || ar.exception != null) {
                this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
            } else {
                byte[] data2 = (byte[]) ar.result;
                setServiceProviderName(IccUtils.adnStringFieldToString(data2, 0, data2.length));
                String spn2 = getServiceProviderName();
                if (spn2 == null || spn2.length() == 0) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                } else {
                    this.mCarrierNameDisplayCondition = 0;
                    log("Load EF_SPN_CPHS: " + spn2);
                    oppoSetSimSpn(spn2);
                    this.mSpnState = GetSpnFsmState.IDLE;
                    setMvnoState(IccRecords.MvnoType.SPN);
                }
            }
            if (this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS) {
                this.mFh.loadEFTransparent(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(12));
                this.mRecordsToLoad++;
            }
        } else if (i != 4) {
            this.mSpnState = GetSpnFsmState.IDLE;
        } else {
            if (ar == null || ar.exception != null) {
                setServiceProviderName(null);
                log("No SPN loaded in either CHPS or 3GPP");
            } else {
                byte[] data3 = (byte[]) ar.result;
                setServiceProviderName(IccUtils.adnStringFieldToString(data3, 0, data3.length));
                String spn3 = getServiceProviderName();
                if (spn3 == null || spn3.length() == 0) {
                    log("No SPN loaded in either CHPS or 3GPP");
                } else {
                    this.mCarrierNameDisplayCondition = 0;
                    log("Load EF_SPN_SHORT_CPHS: " + spn3);
                    oppoSetSimSpn(spn3);
                }
            }
            this.mSpnState = GetSpnFsmState.IDLE;
            setMvnoState(IccRecords.MvnoType.SPN);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.uicc.SIMRecords$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState = new int[GetSpnFsmState.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[GetSpnFsmState.INIT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[GetSpnFsmState.READ_SPN_3GPP.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[GetSpnFsmState.READ_SPN_CPHS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[GetSpnFsmState.READ_SPN_SHORT_CPHS.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private void parseEfSpdi(byte[] data) {
        SimTlv tlv = new SimTlv(data, 0, data.length);
        byte[] plmnEntries = null;
        while (true) {
            if (!tlv.isValidObject()) {
                break;
            }
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
            List<String> tmpSpdi = new ArrayList<>(plmnEntries.length / 3);
            for (int i = 0; i + 2 < plmnEntries.length; i += 3) {
                String plmnCode = IccUtils.bcdPlmnToString(plmnEntries, i);
                if (!TextUtils.isEmpty(plmnCode)) {
                    log("EF_SPDI PLMN: " + plmnCode);
                    tmpSpdi.add(plmnCode);
                }
            }
            this.mSpdi = (String[]) tmpSpdi.toArray();
        }
    }

    private String[] parseBcdPlmnList(byte[] data, String description) {
        log("Received " + description + " PLMNs, raw=" + IccUtils.bytesToHexString(data));
        if (data.length == 0 || data.length % 3 != 0) {
            loge("Received invalid " + description + " PLMN list");
            return null;
        }
        int numPlmns = data.length / 3;
        int numValidPlmns = 0;
        String[] parsed = new String[numPlmns];
        for (int i = 0; i < numPlmns; i++) {
            parsed[numValidPlmns] = IccUtils.bcdPlmnToString(data, i * 3);
            if (!TextUtils.isEmpty(parsed[numValidPlmns])) {
                numValidPlmns++;
            }
        }
        return (String[]) Arrays.copyOf(parsed, numValidPlmns);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isCphsMailboxEnabled() {
        byte[] bArr = this.mCphsInfo;
        if (bArr != null && (bArr[1] & 48) == 48) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void log(String s) {
        Rlog.d(LOG_TAG, "[SIMRecords] " + s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void loge(String s) {
        Rlog.e(LOG_TAG, "[SIMRecords] " + s);
    }

    /* access modifiers changed from: protected */
    public void logw(String s, Throwable tr) {
        Rlog.w(LOG_TAG, "[SIMRecords] " + s, tr);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void logv(String s) {
        Rlog.v(LOG_TAG, "[SIMRecords] " + s);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public boolean isCspPlmnEnabled() {
        return this.mCspPlmnEnabled;
    }

    private void handleEfCspData(byte[] data) {
        int usedCspGroups = data.length / 2;
        this.mCspPlmnEnabled = true;
        for (int i = 0; i < usedCspGroups; i++) {
            if (data[i * 2] == -64) {
                log("[CSP] found ValueAddedServicesGroup, value " + ((int) data[(i * 2) + 1]));
                if ((data[(i * 2) + 1] & 128) == 128) {
                    this.mCspPlmnEnabled = true;
                    return;
                }
                this.mCspPlmnEnabled = false;
                log("[CSP] Set Automatic Network Selection");
                this.mNetworkSelectionModeAutomaticRegistrants.notifyRegistrants();
                return;
            }
        }
        log("[CSP] Value Added Service Group (0xC0), not found!");
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SIMRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.println(" mVmConfig=" + this.mVmConfig);
        pw.println(" mCallForwardingStatus=" + this.mCallForwardingStatus);
        pw.println(" mSpnState=" + this.mSpnState);
        pw.println(" mCphsInfo=" + this.mCphsInfo);
        pw.println(" mCspPlmnEnabled=" + this.mCspPlmnEnabled);
        pw.println(" mEfMWIS[]=" + Arrays.toString(this.mEfMWIS));
        pw.println(" mEfCPHS_MWI[]=" + Arrays.toString(this.mEfCPHS_MWI));
        pw.println(" mEfCff[]=" + Arrays.toString(this.mEfCff));
        pw.println(" mEfCfis[]=" + Arrays.toString(this.mEfCfis));
        pw.println(" mCarrierNameDisplayCondition=" + this.mCarrierNameDisplayCondition);
        pw.println(" mSpdi[]=" + this.mSpdi);
        pw.println(" mUsimServiceTable=" + this.mUsimServiceTable);
        pw.println(" mGid1=" + this.mGid1);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeGid1=" + this.mCarrierTestOverride.getFakeGid1());
        }
        pw.println(" mGid2=" + this.mGid2);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeGid2=" + this.mCarrierTestOverride.getFakeGid2());
        }
        pw.println(" mPnnHomeName=" + this.mPnnHomeName);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakePnnHomeName=" + this.mCarrierTestOverride.getFakePnnHomeName());
        }
        pw.println(" mPlmnActRecords[]=" + Arrays.toString(this.mPlmnActRecords));
        pw.println(" mOplmnActRecords[]=" + Arrays.toString(this.mOplmnActRecords));
        pw.println(" mHplmnActRecords[]=" + Arrays.toString(this.mHplmnActRecords));
        pw.println(" mFplmns[]=" + Arrays.toString(this.mFplmns));
        pw.println(" mEhplmns[]=" + Arrays.toString(this.mEhplmns));
        pw.flush();
    }

    /* access modifiers changed from: protected */
    public boolean checkCdma3gCard() {
        return true;
    }
}
