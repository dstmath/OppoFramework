package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.util.BitwiseInputStream;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

public class RuimRecords extends AbstractRuimRecords {
    protected static final int EVENT_APP_LOCKED = 32;
    private static final int EVENT_APP_NETWORK_LOCKED = 33;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    protected static final int EVENT_GET_CDMA_SUBSCRIPTION_DONE = 10;
    private static final int EVENT_GET_DEVICE_IDENTITY_DONE = 4;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_IMSI_DONE = 3;
    private static final int EVENT_GET_SMS_DONE = 22;
    protected static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SMS_ON_RUIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    static final String LOG_TAG = "RuimRecords";
    protected boolean mCsimSpnDisplayCondition = false;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public byte[] mEFli = null;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public byte[] mEFpl = null;
    /* access modifiers changed from: private */
    public String mHomeNetworkId;
    /* access modifiers changed from: private */
    public String mHomeSystemId;
    /* access modifiers changed from: private */
    public String mMdn;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public String mMin;
    protected String mMin2Min1;
    protected String mMyMobileNumber;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public String mNai;
    private boolean mOtaCommited = false;
    protected String mPrlVersion;

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String toString() {
        return "RuimRecords: " + super.toString() + " m_ota_commited" + this.mOtaCommited + " mMyMobileNumber=xxxx mMin2Min1=" + this.mMin2Min1 + " mPrlVersion=" + this.mPrlVersion + " mEFpl=" + this.mEFpl + " mEFli=" + this.mEFli + " mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition + " mMdn=" + this.mMdn + " mMin=" + this.mMin + " mHomeSystemId=" + this.mHomeSystemId + " mHomeNetworkId=" + this.mHomeNetworkId;
    }

    public RuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mAdnCache = new AdnRecordCache(this.mFh);
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mRecordsToLoad = 0;
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, 32, null);
        this.mParentApp.registerForNetworkLocked(this, 33, null);
        log("RuimRecords X ctor this=" + this);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.AbstractRuimRecords, com.android.internal.telephony.uicc.AbstractBaseRecords
    public void dispose() {
        log("Disposing RuimRecords " + this);
        this.mParentApp.unregisterForReady(this);
        this.mParentApp.unregisterForLocked(this);
        this.mParentApp.unregisterForNetworkLocked(this);
        resetRecords();
        super.dispose();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        log("RuimRecords finalized");
    }

    /* access modifiers changed from: protected */
    public void resetRecords() {
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mAdnCache.reset();
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mLoaded.set(false);
        resetMvnoState();
    }

    @UnsupportedAppUsage
    public String getMdnNumber() {
        return this.mMyMobileNumber;
    }

    public String getCdmaMin() {
        return this.mMin2Min1;
    }

    public String getPrlVersion() {
        return this.mPrlVersion;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String getNAI() {
        return this.mNai;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        AsyncResult.forMessage(onComplete).exception = new IccException("setVoiceMailNumber not implemented");
        onComplete.sendToTarget();
        loge("method setVoiceMailNumber is not implemented");
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchRuimRecords();
        }
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public int adjstMinDigits(int digits) {
        int digits2 = digits + 111;
        int digits3 = digits2 % 10 == 0 ? digits2 - 10 : digits2;
        int digits4 = (digits3 / 10) % 10 == 0 ? digits3 - 100 : digits3;
        return (digits4 / 100) % 10 == 0 ? digits4 - 1000 : digits4;
    }

    @UnsupportedAppUsage
    public String getRUIMOperatorNumeric() {
        String imsi = getIMSI();
        if (imsi == null) {
            return null;
        }
        if (this.mMncLength != -1 && this.mMncLength != 0) {
            return imsi.substring(0, this.mMncLength + 3);
        }
        try {
            return imsi.substring(0, MccTable.smallestDigitsMccForMnc(Integer.parseInt(imsi.substring(0, 3))) + 3);
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
            return null;
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
            return null;
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String getOperatorNumeric() {
        return getRUIMOperatorNumeric();
    }

    private class EfPlLoaded implements IccRecords.IccRecordLoaded {
        private EfPlLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_PL";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] unused = RuimRecords.this.mEFpl = (byte[]) ar.result;
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.log("EF_PL=" + IccUtils.bytesToHexString(RuimRecords.this.mEFpl));
        }
    }

    private class EfCsimLiLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimLiLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_LI";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] unused = RuimRecords.this.mEFli = (byte[]) ar.result;
            for (int i = 0; i < RuimRecords.this.mEFli.length; i += 2) {
                switch (RuimRecords.this.mEFli[i + 1]) {
                    case 1:
                        RuimRecords.this.mEFli[i] = 101;
                        RuimRecords.this.mEFli[i + 1] = 110;
                        break;
                    case 2:
                        RuimRecords.this.mEFli[i] = 102;
                        RuimRecords.this.mEFli[i + 1] = 114;
                        break;
                    case 3:
                        RuimRecords.this.mEFli[i] = 101;
                        RuimRecords.this.mEFli[i + 1] = 115;
                        break;
                    case 4:
                        RuimRecords.this.mEFli[i] = 106;
                        RuimRecords.this.mEFli[i + 1] = 97;
                        break;
                    case 5:
                        RuimRecords.this.mEFli[i] = 107;
                        RuimRecords.this.mEFli[i + 1] = 111;
                        break;
                    case 6:
                        RuimRecords.this.mEFli[i] = 122;
                        RuimRecords.this.mEFli[i + 1] = 104;
                        break;
                    case 7:
                        RuimRecords.this.mEFli[i] = 104;
                        RuimRecords.this.mEFli[i + 1] = 101;
                        break;
                    default:
                        RuimRecords.this.mEFli[i] = 32;
                        RuimRecords.this.mEFli[i + 1] = 32;
                        break;
                }
            }
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.log("EF_LI=" + IccUtils.bytesToHexString(RuimRecords.this.mEFli));
        }
    }

    private class EfCsimSpnLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimSpnLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_SPN";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.log("CSIM_SPN=" + IccUtils.bytesToHexString(data));
            RuimRecords.this.mCsimSpnDisplayCondition = (data[0] & 1) != 0;
            byte b = data[1];
            byte b2 = data[2];
            int len = 32;
            byte[] spnData = new byte[32];
            if (data.length - 3 < 32) {
                len = data.length - 3;
            }
            System.arraycopy(data, 3, spnData, 0, len);
            int numBytes = 0;
            while (numBytes < spnData.length && (spnData[numBytes] & 255) != 255) {
                numBytes++;
            }
            if (numBytes == 0) {
                RuimRecords.this.setServiceProviderName(PhoneConfigurationManager.SSSS);
                return;
            }
            if (b != 0) {
                if (b != 2) {
                    if (b != 3) {
                        if (b == 4) {
                            RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, CharacterSets.MIMENAME_UTF_16));
                        } else if (b != 8) {
                            if (b != 9) {
                                try {
                                    RuimRecords.this.log("SPN encoding not supported");
                                } catch (Exception e) {
                                    RuimRecords ruimRecords2 = RuimRecords.this;
                                    ruimRecords2.log("spn decode error: " + e);
                                }
                            }
                        }
                    }
                    RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                } else {
                    String spn = new String(spnData, 0, numBytes, "US-ASCII");
                    if (TextUtils.isPrintableAsciiOnly(spn)) {
                        RuimRecords.this.setServiceProviderName(spn);
                    } else {
                        RuimRecords ruimRecords3 = RuimRecords.this;
                        ruimRecords3.log("Some corruption in SPN decoding = " + spn);
                        RuimRecords.this.log("Using ENCODING_GSM_7BIT_ALPHABET scheme...");
                        RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                    }
                }
                RuimRecords ruimRecords4 = RuimRecords.this;
                ruimRecords4.log("spn=" + RuimRecords.this.getServiceProviderName());
                RuimRecords ruimRecords5 = RuimRecords.this;
                ruimRecords5.log("spnCondition=" + RuimRecords.this.mCsimSpnDisplayCondition);
                RuimRecords.this.mTelephonyManager.setSimOperatorNameForPhone(RuimRecords.this.mParentApp.getPhoneId(), RuimRecords.this.getServiceProviderName());
                RuimRecords.this.setMvnoState(IccRecords.MvnoType.SPN);
            }
            RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, "ISO-8859-1"));
            RuimRecords ruimRecords42 = RuimRecords.this;
            ruimRecords42.log("spn=" + RuimRecords.this.getServiceProviderName());
            RuimRecords ruimRecords52 = RuimRecords.this;
            ruimRecords52.log("spnCondition=" + RuimRecords.this.mCsimSpnDisplayCondition);
            RuimRecords.this.mTelephonyManager.setSimOperatorNameForPhone(RuimRecords.this.mParentApp.getPhoneId(), RuimRecords.this.getServiceProviderName());
            RuimRecords.this.setMvnoState(IccRecords.MvnoType.SPN);
        }
    }

    private class EfCsimMdnLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimMdnLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_MDN";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.log("CSIM_MDN=" + IccUtils.bytesToHexString(data));
            String unused = RuimRecords.this.mMdn = IccUtils.cdmaBcdToString(data, 1, data[0] & 15);
            RuimRecords ruimRecords2 = RuimRecords.this;
            ruimRecords2.log("CSIM MDN=" + RuimRecords.this.mMdn);
        }
    }

    private class EfCsimImsimLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimImsimLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_IMSIM";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            boolean provisioned = (data[7] & 128) == 128;
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.mIsTestCard = OemConstant.isTestCard(ruimRecords.mImsi);
            RuimRecords ruimRecords2 = RuimRecords.this;
            ruimRecords2.log("leon mIsTestCard 3: " + RuimRecords.this.mIsTestCard);
            if (provisioned) {
                int first3digits = ((data[2] & 3) << 8) + (data[1] & 255);
                int second3digits = (((data[5] & 255) << 8) | (data[4] & 255)) >> 6;
                int digit7 = (data[4] >> 2) & 15;
                if (digit7 > 9) {
                    digit7 = 0;
                }
                int last3digits = (data[3] & 255) | ((data[4] & 3) << 8);
                int first3digits2 = RuimRecords.this.adjstMinDigits(first3digits);
                int second3digits2 = RuimRecords.this.adjstMinDigits(second3digits);
                int last3digits2 = RuimRecords.this.adjstMinDigits(last3digits);
                String unused = RuimRecords.this.mMin = String.format(Locale.US, "%03d", Integer.valueOf(first3digits2)) + String.format(Locale.US, "%03d", Integer.valueOf(second3digits2)) + String.format(Locale.US, "%d", Integer.valueOf(digit7)) + String.format(Locale.US, "%03d", Integer.valueOf(last3digits2));
                RuimRecords ruimRecords3 = RuimRecords.this;
                StringBuilder sb = new StringBuilder();
                sb.append("min present=");
                sb.append(Rlog.pii(RuimRecords.LOG_TAG, RuimRecords.this.mMin));
                ruimRecords3.log(sb.toString());
                return;
            }
            RuimRecords.this.log("min not present");
        }
    }

    private class EfCsimCdmaHomeLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimCdmaHomeLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_CDMAHOME";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> dataList = (ArrayList) ar.result;
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.log("CSIM_CDMAHOME data size=" + dataList.size());
            if (!dataList.isEmpty()) {
                StringBuilder sidBuf = new StringBuilder();
                StringBuilder nidBuf = new StringBuilder();
                Iterator<byte[]> it = dataList.iterator();
                while (it.hasNext()) {
                    byte[] data = it.next();
                    if (data.length == 5) {
                        sidBuf.append(((data[1] & 255) << 8) | (data[0] & 255));
                        sidBuf.append(',');
                        nidBuf.append(((data[3] & 255) << 8) | (data[2] & 255));
                        nidBuf.append(',');
                    }
                }
                sidBuf.setLength(sidBuf.length() - 1);
                nidBuf.setLength(nidBuf.length() - 1);
                String unused = RuimRecords.this.mHomeSystemId = sidBuf.toString();
                String unused2 = RuimRecords.this.mHomeNetworkId = nidBuf.toString();
            }
        }
    }

    public class EfAdLoaded implements IccRecords.IccRecordLoaded {
        public EfAdLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_AD";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            if (ar.exception == null) {
                RuimRecords ruimRecords = RuimRecords.this;
                ruimRecords.log("yangli EF_AD: " + IccUtils.bytesToHexString(data));
                if (data.length < 3) {
                    RuimRecords.this.log("Corrupt AD data on SIM");
                } else if (data.length == 3) {
                    RuimRecords.this.log("MNC length not present in EF_AD");
                } else {
                    RuimRecords ruimRecords2 = RuimRecords.this;
                    boolean z = false;
                    if (ruimRecords2.mIsTestCard || OemConstant.isTestCard(RuimRecords.this.mContext, data[0])) {
                        z = true;
                    }
                    ruimRecords2.mIsTestCard = z;
                    RuimRecords ruimRecords3 = RuimRecords.this;
                    ruimRecords3.log("leon mIsTestCard 4: " + RuimRecords.this.mIsTestCard);
                    RuimRecords.this.updateNrModeTestCard();
                }
            }
        }
    }

    private class EfCsimEprlLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimEprlLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_EPRL";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.onGetCSimEprlDone(ar);
        }
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public void onGetCSimEprlDone(AsyncResult ar) {
        byte[] data = (byte[]) ar.result;
        log("CSIM_EPRL=" + IccUtils.bytesToHexString(data));
        if (data.length > 3) {
            this.mPrlVersion = Integer.toString(((data[2] & 255) << 8) | (data[3] & 255));
        }
        log("CSIM PRL version=" + this.mPrlVersion);
    }

    private class EfCsimMipUppLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimMipUppLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_MIPUPP";
        }

        /* access modifiers changed from: package-private */
        public boolean checkLengthLegal(int length, int expectLength) {
            if (length >= expectLength) {
                return true;
            }
            Log.e(RuimRecords.LOG_TAG, "CSIM MIPUPP format error, length = " + length + "expected length at least =" + expectLength);
            return false;
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            if (data.length < 1) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read error");
                return;
            }
            BitwiseInputStream bitStream = new BitwiseInputStream(data);
            int i = 8;
            try {
                int mipUppLength = bitStream.read(8) << 3;
                if (checkLengthLegal(mipUppLength, 1)) {
                    int mipUppLength2 = mipUppLength - 1;
                    if (bitStream.read(1) == 1) {
                        if (checkLengthLegal(mipUppLength2, 11)) {
                            bitStream.skip(11);
                            mipUppLength2 -= 11;
                        } else {
                            return;
                        }
                    }
                    if (checkLengthLegal(mipUppLength2, 4)) {
                        int numNai = bitStream.read(4);
                        int mipUppLength3 = mipUppLength2 - 4;
                        int index = 0;
                        while (index < numNai && checkLengthLegal(mipUppLength3, 4)) {
                            int naiEntryIndex = bitStream.read(4);
                            int mipUppLength4 = mipUppLength3 - 4;
                            if (checkLengthLegal(mipUppLength4, i)) {
                                int naiLength = bitStream.read(i);
                                int mipUppLength5 = mipUppLength4 - 8;
                                if (naiEntryIndex == 0) {
                                    if (checkLengthLegal(mipUppLength5, naiLength << 3)) {
                                        char[] naiCharArray = new char[naiLength];
                                        for (int index1 = 0; index1 < naiLength; index1++) {
                                            naiCharArray[index1] = (char) (bitStream.read(i) & 255);
                                        }
                                        String unused = RuimRecords.this.mNai = new String(naiCharArray);
                                        if (Log.isLoggable(RuimRecords.LOG_TAG, 2)) {
                                            Log.v(RuimRecords.LOG_TAG, "MIPUPP Nai = " + RuimRecords.this.mNai);
                                            return;
                                        }
                                        return;
                                    }
                                    return;
                                } else if (checkLengthLegal(mipUppLength5, (naiLength << 3) + 102)) {
                                    bitStream.skip((naiLength << 3) + 101);
                                    int mipUppLength6 = mipUppLength5 - ((naiLength << 3) + 102);
                                    if (bitStream.read(1) == 1) {
                                        if (checkLengthLegal(mipUppLength6, 32)) {
                                            bitStream.skip(32);
                                            mipUppLength6 -= 32;
                                        } else {
                                            return;
                                        }
                                    }
                                    if (checkLengthLegal(mipUppLength6, 5)) {
                                        bitStream.skip(4);
                                        mipUppLength3 = (mipUppLength6 - 4) - 1;
                                        if (bitStream.read(1) == 1) {
                                            if (checkLengthLegal(mipUppLength3, 32)) {
                                                bitStream.skip(32);
                                                mipUppLength3 -= 32;
                                            } else {
                                                return;
                                            }
                                        }
                                        index++;
                                        i = 8;
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read Exception error!");
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.AbstractRuimRecords
    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
            int i = msg.what;
            if (i == 1) {
                onReady();
            } else if (i == 10) {
                AsyncResult ar = (AsyncResult) msg.obj;
                String[] localTemp = (String[]) ar.result;
                if (ar.exception == null) {
                    this.mMyMobileNumber = localTemp[0];
                    this.mMin2Min1 = localTemp[3];
                    this.mPrlVersion = localTemp[4];
                    log("MDN: " + this.mMyMobileNumber + " MIN: " + this.mMin2Min1);
                }
            } else if (i == 14) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception != null) {
                    Rlog.i(LOG_TAG, "RuimRecords update failed", ar2.exception);
                }
            } else if (i == 3) {
                isRecordLoadResponse = true;
                AsyncResult ar3 = (AsyncResult) msg.obj;
                if (ar3.exception != null) {
                    loge("Exception querying IMSI, Exception:" + ar3.exception);
                } else {
                    this.mImsi = (String) ar3.result;
                    if (this.mImsi != null && (this.mImsi.length() < 6 || this.mImsi.length() > 15)) {
                        loge("invalid IMSI " + this.mImsi);
                        this.mImsi = null;
                    }
                    String operatorNumeric = getRUIMOperatorNumeric();
                    log("NO update mccmnc=" + operatorNumeric);
                    onGetImsiDone(this.mImsi);
                }
            } else if (i == 4) {
                log("Event EVENT_GET_DEVICE_IDENTITY_DONE Received");
            } else if (i != 5) {
                if (!(i == 21 || i == 22)) {
                    if (i != 32 && i != 33) {
                        switch (i) {
                            case 17:
                                log("Event EVENT_GET_SST_DONE Received");
                                break;
                            case 18:
                            case 19:
                                break;
                            default:
                                super.handleMessage(msg);
                                break;
                        }
                    } else {
                        onLocked(msg.what);
                    }
                }
                Rlog.w(LOG_TAG, "Event not supported: " + msg.what);
            } else {
                isRecordLoadResponse = true;
                AsyncResult ar4 = (AsyncResult) msg.obj;
                byte[] data = (byte[]) ar4.result;
                if (ar4.exception == null) {
                    this.mIccId = IccUtils.bcdToString(data, 0, data.length);
                    this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                    log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                    setMvnoState(IccRecords.MvnoType.ICCID);
                }
            }
            if (!isRecordLoadResponse) {
                return;
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing RUIM record", exc);
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

    @UnsupportedAppUsage
    private static String[] getAssetLanguages(Context ctx) {
        String[] locales = ctx.getAssets().getLocales();
        String[] localeLangs = new String[locales.length];
        for (int i = 0; i < locales.length; i++) {
            String localeStr = locales[i];
            int separator = localeStr.indexOf(45);
            if (separator < 0) {
                localeLangs[i] = localeStr;
            } else {
                localeLangs[i] = localeStr.substring(0, separator);
            }
        }
        return localeLangs;
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

    private void onLockedAllRecordsLoaded() {
        if (this.mLockedRecordsReqReason == 1) {
            this.mLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        } else if (this.mLockedRecordsReqReason == 2) {
            this.mNetworkLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        } else {
            loge("onLockedAllRecordsLoaded: unexpected mLockedRecordsReqReason " + this.mLockedRecordsReqReason);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.AbstractRuimRecords
    public void onAllRecordsLoaded() {
        log("record load complete");
        if (Resources.getSystem().getBoolean(17891565)) {
            setSimLanguage(this.mEFli, this.mEFpl);
        }
        this.mLoaded.set(true);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        if (!TextUtils.isEmpty(this.mMdn)) {
            int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mParentApp.getUiccProfile().getPhoneId());
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                SubscriptionManager.from(this.mContext).setDisplayNumber(this.mMdn, subId);
            } else {
                log("Cannot call setDisplayNumber: invalid subId");
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onReady() {
        fetchRuimRecords();
        this.mCi.getCDMASubscription(obtainMessage(10));
    }

    /* access modifiers changed from: protected */
    public void onLocked(int msg) {
        int i;
        log("only fetch EF_ICCID in locked state");
        if (msg == 32) {
            i = 1;
        } else {
            i = 2;
        }
        this.mLockedRecordsReqReason = i;
        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(5));
        this.mRecordsToLoad++;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void fetchRuimRecords() {
        this.mRecordsRequested = true;
        resetMvnoState();
        log("fetchRuimRecords " + this.mRecordsToLoad);
        this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(3));
        this.mRecordsToLoad = this.mRecordsToLoad + 1;
        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(5));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_AD, obtainMessage(100, new EfAdLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28474, obtainMessage(100, new EfCsimLiLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28481, obtainMessage(100, new EfCsimSpnLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_CSIM_MDN, 1, obtainMessage(100, new EfCsimMdnLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSIM_IMSIM, obtainMessage(100, new EfCsimImsimLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixedAll(IccConstants.EF_CSIM_CDMAHOME, obtainMessage(100, new EfCsimCdmaHomeLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSIM_EPRL, 4, obtainMessage(100, new EfCsimEprlLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSIM_MIPUPP, obtainMessage(100, new EfCsimMipUppLoaded()));
        this.mRecordsToLoad++;
        log("fetchRuimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public boolean isProvisioned() {
        if (SystemProperties.getBoolean("persist.radio.test-csim", false)) {
            return true;
        }
        if (this.mParentApp == null) {
            return false;
        }
        return (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_CSIM && (this.mMdn == null || this.mMin == null)) ? false : true;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setVoiceMessageWaiting(int line, int countWaiting) {
        log("RuimRecords:setVoiceMessageWaiting - NOP for CDMA");
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getVoiceMessageCount() {
        log("RuimRecords:getVoiceMessageCount - NOP for CDMA");
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void handleFileUpdate(int efid) {
        this.mAdnCache.reset();
        fetchRuimRecords();
    }

    @UnsupportedAppUsage
    public String getMdn() {
        return this.mMdn;
    }

    public String getMin() {
        return this.mMin;
    }

    public String getSid() {
        return this.mHomeSystemId;
    }

    public String getNid() {
        return this.mHomeNetworkId;
    }

    @UnsupportedAppUsage
    public boolean getCsimSpnDisplayCondition() {
        return this.mCsimSpnDisplayCondition;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void log(String s) {
        Rlog.d(LOG_TAG, "[RuimRecords] " + s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void loge(String s) {
        Rlog.e(LOG_TAG, "[RuimRecords] " + s);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("RuimRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.println(" mOtaCommited=" + this.mOtaCommited);
        pw.println(" mMyMobileNumber=" + this.mMyMobileNumber);
        pw.println(" mMin2Min1=" + this.mMin2Min1);
        pw.println(" mPrlVersion=" + this.mPrlVersion);
        pw.println(" mEFpl[]=" + Arrays.toString(this.mEFpl));
        pw.println(" mEFli[]=" + Arrays.toString(this.mEFli));
        pw.println(" mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition);
        pw.println(" mMdn=" + this.mMdn);
        pw.println(" mMin=" + this.mMin);
        pw.println(" mHomeSystemId=" + this.mHomeSystemId);
        pw.println(" mHomeNetworkId=" + this.mHomeNetworkId);
        pw.flush();
    }

    /* access modifiers changed from: protected */
    public void onGetImsiDone(String imsi) {
    }
}
