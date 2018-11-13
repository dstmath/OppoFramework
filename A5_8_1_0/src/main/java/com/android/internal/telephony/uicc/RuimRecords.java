package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.cdma.CdmaNetworkInfoWithAcT;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded;
import com.android.internal.util.BitwiseInputStream;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class RuimRecords extends IccRecords {
    private static final int CSIM_IMSI_MNC_LENGTH = 2;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    private static final int EVENT_GET_CDMA_SUBSCRIPTION_DONE = 10;
    private static final int EVENT_GET_DEVICE_IDENTITY_DONE = 4;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_POL_DONE = 99;
    private static final int EVENT_GET_POL_ERROR = 77;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SET_POL_DONE = 88;
    private static final int EVENT_SET_POL_ERROR = 66;
    private static final int EVENT_SMS_ON_RUIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    static final String LOG_TAG = "RuimRecords";
    private static final int POL_TECH_CDMA2000 = 2;
    private static final int POL_TECH_E_UTRAN = 4;
    private static final int POL_TECH_GSM = 1;
    private static final int POL_TECH_UNKNOW = 0;
    private static final int POL_TECH_UTRAN = 3;
    private boolean eventGetPolError = false;
    private boolean eventSetPolError = false;
    boolean mCsimSpnDisplayCondition = false;
    private byte[] mEFli = null;
    private byte[] mEFpl = null;
    byte[] mEfpol;
    protected IccFileHandler mFh;
    private String mHomeNetworkId;
    private String mHomeSystemId;
    private String mMdn;
    private String mMin;
    private String mMin2Min1;
    private String mMyMobileNumber;
    private String mNai;
    String[] mOperatorAlphaName;
    String[] mOperatorNumeric;
    private boolean mOtaCommited = false;
    int[] mPlmn;
    public int mPlmnNumber;
    private String mPrlVersion;
    byte[] mReadBuffer;
    protected ServiceStateTracker mSST;
    int[] mTech;
    public int mUsedPlmnNumber;
    byte[] mWriteBuffer;
    protected Message onCompleteMsg;

    private class EfAdLoaded implements IccRecordLoaded {
        private EfAdLoaded() {
        }

        public String getEfName() {
            return "EF_AD";
        }

        public void onRecordLoaded(AsyncResult ar) {
            int i = 1;
            byte[] data = ar.result;
            if (ar.exception == null) {
                RuimRecords.this.log("yangli EF_AD: " + IccUtils.bytesToHexString(data));
                if (data.length < 3) {
                    RuimRecords.this.log("Corrupt AD data on SIM");
                } else if (data.length == 3) {
                    RuimRecords.this.log("MNC length not present in EF_AD");
                } else {
                    boolean z;
                    RuimRecords ruimRecords = RuimRecords.this;
                    if (RuimRecords.this.mIsTestCard) {
                        z = true;
                    } else {
                        z = OemConstant.isTestCard(RuimRecords.this.mContext, data[0]);
                    }
                    ruimRecords.mIsTestCard = z;
                    if ("allnetcmcctest".equals(SystemProperties.get("persist.sys.net_build_type", "allnet"))) {
                        RuimRecords.this.log("Force set any card as test card");
                        RuimRecords.this.mIsTestCard = true;
                    }
                    Phone phone = PhoneFactory.getPhone(RuimRecords.this.mParentApp.getPhoneId());
                    if (RuimRecords.this.mIsTestCard) {
                        i = 2;
                    }
                    phone.oppoSetSimType(i);
                    RuimRecords.this.log("leon mIsTestCard 4: " + RuimRecords.this.mIsTestCard);
                }
            }
        }
    }

    private class EfCsimCdmaHomeLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimCdmaHomeLoaded(RuimRecords this$0, EfCsimCdmaHomeLoaded -this1) {
            this();
        }

        private EfCsimCdmaHomeLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_CDMAHOME";
        }

        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> dataList = ar.result;
            RuimRecords.this.log("CSIM_CDMAHOME data size=" + dataList.size());
            if (!dataList.isEmpty()) {
                StringBuilder sidBuf = new StringBuilder();
                StringBuilder nidBuf = new StringBuilder();
                for (byte[] data : dataList) {
                    if (data.length == 5) {
                        int nid = ((data[3] & 255) << 8) | (data[2] & 255);
                        sidBuf.append(((data[1] & 255) << 8) | (data[0] & 255)).append(',');
                        nidBuf.append(nid).append(',');
                    }
                }
                sidBuf.setLength(sidBuf.length() - 1);
                nidBuf.setLength(nidBuf.length() - 1);
                RuimRecords.this.mHomeSystemId = sidBuf.toString();
                RuimRecords.this.mHomeNetworkId = nidBuf.toString();
            }
        }
    }

    private class EfCsimEprlLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimEprlLoaded(RuimRecords this$0, EfCsimEprlLoaded -this1) {
            this();
        }

        private EfCsimEprlLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_EPRL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.onGetCSimEprlDone(ar);
        }
    }

    private class EfCsimImsimLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimImsimLoaded(RuimRecords this$0, EfCsimImsimLoaded -this1) {
            this();
        }

        private EfCsimImsimLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_IMSIM";
        }

        public void onRecordLoaded(AsyncResult ar) {
            int i = 1;
            byte[] data = ar.result;
            if (data == null || data.length < 10) {
                RuimRecords.this.log("Invalid IMSI from EF_CSIM_IMSIM " + IccUtils.bytesToHexString(data));
                RuimRecords.this.mImsi = null;
                RuimRecords.this.mMin = null;
                return;
            }
            RuimRecords.this.log("CSIM_IMSIM=" + IccUtils.bytesToHexString(data));
            if ((data[7] & 128) == 128) {
                RuimRecords.this.mImsi = RuimRecords.this.decodeImsi(data);
                if (RuimRecords.this.mImsi != null) {
                    RuimRecords.this.mMin = RuimRecords.this.mImsi.substring(5, 15);
                }
                RuimRecords.this.log("IMSI: " + RuimRecords.this.mImsi.substring(0, 5) + "xxxxxxxxx");
            } else {
                RuimRecords.this.log("IMSI not provisioned in card");
            }
            RuimRecords.this.mIsTestCard = OemConstant.isTestCard(RuimRecords.this.mImsi);
            RuimRecords.this.log("leon mIsTestCard 3: " + RuimRecords.this.mIsTestCard);
            if ("allnetcmcctest".equals(SystemProperties.get("persist.sys.net_build_type", "allnet"))) {
                RuimRecords.this.log("Force set any card as test card");
                RuimRecords.this.mIsTestCard = true;
            }
            Phone phone = PhoneFactory.getPhone(RuimRecords.this.mParentApp.getPhoneId());
            if (RuimRecords.this.mIsTestCard) {
                i = 2;
            }
            phone.oppoSetSimType(i);
            String operatorNumeric = RuimRecords.this.getOperatorNumeric();
            if (operatorNumeric != null && operatorNumeric.length() <= 6) {
                MccTable.updateMccMncConfiguration(RuimRecords.this.mContext, operatorNumeric, false);
            }
            RuimRecords.this.mImsiReadyRegistrants.notifyRegistrants();
        }
    }

    private class EfCsimLiLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimLiLoaded(RuimRecords this$0, EfCsimLiLoaded -this1) {
            this();
        }

        private EfCsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.mEFli = (byte[]) ar.result;
            for (int i = 0; i < RuimRecords.this.mEFli.length; i += 2) {
                switch (RuimRecords.this.mEFli[i + 1]) {
                    case (byte) 1:
                        RuimRecords.this.mEFli[i] = (byte) 101;
                        RuimRecords.this.mEFli[i + 1] = (byte) 110;
                        break;
                    case (byte) 2:
                        RuimRecords.this.mEFli[i] = (byte) 102;
                        RuimRecords.this.mEFli[i + 1] = (byte) 114;
                        break;
                    case (byte) 3:
                        RuimRecords.this.mEFli[i] = (byte) 101;
                        RuimRecords.this.mEFli[i + 1] = (byte) 115;
                        break;
                    case (byte) 4:
                        RuimRecords.this.mEFli[i] = (byte) 106;
                        RuimRecords.this.mEFli[i + 1] = (byte) 97;
                        break;
                    case (byte) 5:
                        RuimRecords.this.mEFli[i] = (byte) 107;
                        RuimRecords.this.mEFli[i + 1] = (byte) 111;
                        break;
                    case (byte) 6:
                        RuimRecords.this.mEFli[i] = (byte) 122;
                        RuimRecords.this.mEFli[i + 1] = (byte) 104;
                        break;
                    case (byte) 7:
                        RuimRecords.this.mEFli[i] = (byte) 104;
                        RuimRecords.this.mEFli[i + 1] = (byte) 101;
                        break;
                    default:
                        RuimRecords.this.mEFli[i] = (byte) 32;
                        RuimRecords.this.mEFli[i + 1] = (byte) 32;
                        break;
                }
            }
            RuimRecords.this.log("EF_LI=" + IccUtils.bytesToHexString(RuimRecords.this.mEFli));
        }
    }

    private class EfCsimMdnLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimMdnLoaded(RuimRecords this$0, EfCsimMdnLoaded -this1) {
            this();
        }

        private EfCsimMdnLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_MDN";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            RuimRecords.this.log("CSIM_MDN=" + IccUtils.bytesToHexString(data));
            RuimRecords.this.mMdn = IccUtils.cdmaBcdToString(data, 1, data[0] & 15);
            RuimRecords.this.log("CSIM MDN=" + RuimRecords.this.mMdn);
        }
    }

    private class EfCsimMipUppLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimMipUppLoaded(RuimRecords this$0, EfCsimMipUppLoaded -this1) {
            this();
        }

        private EfCsimMipUppLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_MIPUPP";
        }

        boolean checkLengthLegal(int length, int expectLength) {
            if (length >= expectLength) {
                return true;
            }
            Log.e(RuimRecords.LOG_TAG, "CSIM MIPUPP format error, length = " + length + "expected length at least =" + expectLength);
            return false;
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            if (data.length < 1) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read error");
                return;
            }
            BitwiseInputStream bitStream = new BitwiseInputStream(data);
            try {
                int mipUppLength = bitStream.read(8) << 3;
                if (checkLengthLegal(mipUppLength, 1)) {
                    mipUppLength--;
                    if (bitStream.read(1) == 1) {
                        if (checkLengthLegal(mipUppLength, 11)) {
                            bitStream.skip(11);
                            mipUppLength -= 11;
                        } else {
                            return;
                        }
                    }
                    if (checkLengthLegal(mipUppLength, 4)) {
                        int numNai = bitStream.read(4);
                        mipUppLength -= 4;
                        int index = 0;
                        while (index < numNai && checkLengthLegal(mipUppLength, 4)) {
                            int naiEntryIndex = bitStream.read(4);
                            mipUppLength -= 4;
                            if (checkLengthLegal(mipUppLength, 8)) {
                                int naiLength = bitStream.read(8);
                                mipUppLength -= 8;
                                if (naiEntryIndex == 0) {
                                    if (checkLengthLegal(mipUppLength, naiLength << 3)) {
                                        char[] naiCharArray = new char[naiLength];
                                        for (int index1 = 0; index1 < naiLength; index1++) {
                                            naiCharArray[index1] = (char) (bitStream.read(8) & 255);
                                        }
                                        RuimRecords.this.mNai = new String(naiCharArray);
                                        if (Log.isLoggable(RuimRecords.LOG_TAG, 2)) {
                                            Log.v(RuimRecords.LOG_TAG, "MIPUPP Nai = " + RuimRecords.this.mNai);
                                        }
                                        return;
                                    }
                                    return;
                                }
                                if (checkLengthLegal(mipUppLength, (naiLength << 3) + 102)) {
                                    bitStream.skip((naiLength << 3) + 101);
                                    mipUppLength -= (naiLength << 3) + 102;
                                    if (bitStream.read(1) == 1) {
                                        if (checkLengthLegal(mipUppLength, 32)) {
                                            bitStream.skip(32);
                                            mipUppLength -= 32;
                                        } else {
                                            return;
                                        }
                                    }
                                    if (checkLengthLegal(mipUppLength, 5)) {
                                        bitStream.skip(4);
                                        mipUppLength = (mipUppLength - 4) - 1;
                                        if (bitStream.read(1) == 1) {
                                            if (checkLengthLegal(mipUppLength, 32)) {
                                                bitStream.skip(32);
                                                mipUppLength -= 32;
                                            } else {
                                                return;
                                            }
                                        }
                                        index++;
                                    } else {
                                        return;
                                    }
                                }
                                return;
                            }
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read Exception error!");
            }
        }
    }

    private class EfCsimSpnLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimSpnLoaded(RuimRecords this$0, EfCsimSpnLoaded -this1) {
            this();
        }

        private EfCsimSpnLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_SPN";
        }

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onRecordLoaded(AsyncResult ar) {
            boolean z;
            byte[] data = ar.result;
            RuimRecords.this.log("CSIM_SPN=" + IccUtils.bytesToHexString(data));
            RuimRecords ruimRecords = RuimRecords.this;
            if ((data[0] & 1) != 0) {
                z = true;
            } else {
                z = false;
            }
            ruimRecords.mCsimSpnDisplayCondition = z;
            int encoding = data[1];
            int language = data[2];
            byte[] spnData = new byte[32];
            System.arraycopy(data, 3, spnData, 0, data.length + -3 < 32 ? data.length - 3 : 32);
            int numBytes = 0;
            while (numBytes < spnData.length && (spnData[numBytes] & 255) != 255) {
                numBytes++;
            }
            if (numBytes == 0) {
                RuimRecords.this.setServiceProviderName(SpnOverride.MVNO_TYPE_NONE);
                return;
            }
            switch (encoding) {
                case 0:
                case 8:
                    RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, "ISO-8859-1"));
                    break;
                case 2:
                    String spn = new String(spnData, 0, numBytes, "US-ASCII");
                    if (!TextUtils.isPrintableAsciiOnly(spn)) {
                        RuimRecords.this.log("Some corruption in SPN decoding = " + spn);
                        RuimRecords.this.log("Using ENCODING_GSM_7BIT_ALPHABET scheme...");
                        RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                        break;
                    }
                    RuimRecords.this.setServiceProviderName(spn);
                    break;
                case 3:
                case 9:
                    RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                    break;
                case 4:
                    RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, CharacterSets.MIMENAME_UTF_16));
                    break;
                default:
                    try {
                        RuimRecords.this.log("SPN encoding not supported");
                        break;
                    } catch (Exception e) {
                        RuimRecords.this.log("spn decode error: " + e);
                        break;
                    }
            }
            RuimRecords.this.log("spn=" + RuimRecords.this.getServiceProviderName());
            RuimRecords.this.log("spnCondition=" + RuimRecords.this.mCsimSpnDisplayCondition);
            RuimRecords.this.mTelephonyManager.setSimOperatorNameForPhone(RuimRecords.this.mParentApp.getPhoneId(), RuimRecords.this.getServiceProviderName());
        }
    }

    private class EfPlLoaded implements IccRecordLoaded {
        /* synthetic */ EfPlLoaded(RuimRecords this$0, EfPlLoaded -this1) {
            this();
        }

        private EfPlLoaded() {
        }

        public String getEfName() {
            return "EF_PL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.mEFpl = (byte[]) ar.result;
            RuimRecords.this.log("EF_PL=" + IccUtils.bytesToHexString(RuimRecords.this.mEFpl));
        }
    }

    public String toString() {
        return "RuimRecords: " + super.toString() + " m_ota_commited" + this.mOtaCommited + " mMyMobileNumber=" + "xxxx" + " mMin2Min1=" + this.mMin2Min1 + " mPrlVersion=" + this.mPrlVersion + " mEFpl=" + this.mEFpl + " mEFli=" + this.mEFli + " mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition + " mMdn=" + this.mMdn + " mMin=" + this.mMin + " mHomeSystemId=" + this.mHomeSystemId + " mHomeNetworkId=" + this.mHomeNetworkId;
    }

    public RuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mFh = app.getIccFileHandler();
        log("yangli sim type=" + app.getType());
        this.mAdnCache = new AdnRecordCache(this.mFh);
        this.mRecordsRequested = false;
        this.mRecordsToLoad = 0;
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        log("RuimRecords X ctor this=" + this);
    }

    public void dispose() {
        log("Disposing RuimRecords " + this);
        this.mParentApp.unregisterForReady(this);
        resetRecords();
        log("RuimRecords: set 'gsm.sim.operator.numeric' to operator=null");
        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), SpnOverride.MVNO_TYPE_NONE);
        super.dispose();
    }

    protected void finalize() {
        log("RuimRecords finalized");
    }

    protected void resetRecords() {
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mAdnCache.reset();
        this.mRecordsRequested = false;
    }

    public String getMdnNumber() {
        return this.mMyMobileNumber;
    }

    public String getCdmaMin() {
        return this.mMin2Min1;
    }

    public String getPrlVersion() {
        return this.mPrlVersion;
    }

    public String getNAI() {
        return this.mNai;
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        AsyncResult.forMessage(onComplete).exception = new IccException("setVoiceMailNumber not implemented");
        onComplete.sendToTarget();
        loge("method setVoiceMailNumber is not implemented");
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchRuimRecords();
        }
    }

    private int decodeImsiDigits(int digits, int length) {
        int i;
        int constant = 0;
        for (i = 0; i < length; i++) {
            constant = (constant * 10) + 1;
        }
        digits += constant;
        int denominator = 1;
        for (i = 0; i < length; i++) {
            if ((digits / denominator) % 10 == 0) {
                digits -= denominator * 10;
            }
            denominator *= 10;
        }
        return digits;
    }

    private String decodeImsi(byte[] data) {
        int mcc = decodeImsiDigits(((data[9] & 3) << 8) | (data[8] & 255), 3);
        int digits_11_12 = decodeImsiDigits(data[6] & 127, 2);
        int first3digits = ((data[2] & 3) << 8) + (data[1] & 255);
        int second3digits = (((data[5] & 255) << 8) | (data[4] & 255)) >> 6;
        int digit7 = (data[4] >> 2) & 15;
        if (digit7 > 9) {
            digit7 = 0;
        }
        int last3digits = ((data[4] & 3) << 8) | (data[3] & 255);
        first3digits = decodeImsiDigits(first3digits, 3);
        second3digits = decodeImsiDigits(second3digits, 3);
        last3digits = decodeImsiDigits(last3digits, 3);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(mcc)}));
        builder.append(String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(digits_11_12)}));
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(first3digits)}));
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(second3digits)}));
        builder.append(String.format(Locale.US, "%d", new Object[]{Integer.valueOf(digit7)}));
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(last3digits)}));
        return builder.toString();
    }

    public String getOperatorNumeric() {
        return getRUIMOperatorNumeric();
    }

    public String getRUIMOperatorNumeric() {
        String imsi = getIMSI();
        if (imsi == null) {
            return null;
        }
        if (this.mMncLength == -1 || this.mMncLength == 0) {
            return this.mImsi.substring(0, 5);
        }
        return imsi.substring(0, this.mMncLength + 3);
    }

    private void onGetCSimEprlDone(AsyncResult ar) {
        byte[] data = ar.result;
        log("CSIM_EPRL=" + IccUtils.bytesToHexString(data));
        if (data.length > 3) {
            this.mPrlVersion = Integer.toString(((data[2] & 255) << 8) | (data[3] & 255));
        }
        log("CSIM PRL version=" + this.mPrlVersion);
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
            AsyncResult ar;
            byte[] data;
            switch (msg.what) {
                case 1:
                    onReady();
                    break;
                case 4:
                    log("Event EVENT_GET_DEVICE_IDENTITY_DONE Received");
                    break;
                case 5:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = ar.result;
                    if (ar.exception == null) {
                        this.mIccId = IccUtils.bcdToString(data, 0, data.length);
                        this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                        log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                        break;
                    }
                    break;
                case 10:
                    ar = msg.obj;
                    String[] localTemp = ar.result;
                    if (ar.exception == null) {
                        this.mMyMobileNumber = localTemp[0];
                        this.mMin2Min1 = localTemp[3];
                        this.mPrlVersion = localTemp[4];
                        log("MDN: " + this.mMyMobileNumber + " MIN: " + this.mMin2Min1);
                        break;
                    }
                    break;
                case 14:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        Rlog.i(LOG_TAG, "RuimRecords update failed", ar.exception);
                        break;
                    }
                    break;
                case 17:
                    log("Event EVENT_GET_SST_DONE Received");
                    break;
                case 18:
                case 19:
                case 21:
                case 22:
                    Rlog.w(LOG_TAG, "Event not supported: " + msg.what);
                    break;
                case 66:
                    this.eventSetPolError = true;
                    break;
                case 77:
                    this.eventGetPolError = true;
                    break;
                case 88:
                    Rlog.d("yangli", "EVENT_SET_POL_DONE");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        if (ar.userObj != null) {
                            AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                            ((Message) ar.userObj).sendToTarget();
                            break;
                        }
                    }
                    Rlog.d("yangli", "Exception in EVENT_SET_POL_DONE EF POL data" + ar.exception);
                    loge("Exception in EVENT_SET_POL_DONE EF POL data " + ar.exception);
                    break;
                    break;
                case 99:
                    Rlog.d("yangli", "EVENT_GET_POL_DONE");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        data = (byte[]) ar.result;
                        Rlog.d("yangli", "EVENT_GET_POL_DONE data " + IccUtils.bytesToHexString(data));
                        if (ar.userObj != null) {
                            AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                            handleEfPOLResponse(data, ar.userObj);
                            break;
                        }
                    }
                    Rlog.d("yangli", "Exception in fetching EF POL data" + ar.exception);
                    loge("Exception in fetching EF POL data " + ar.exception);
                    this.mFh.loadEFTransparent(28464, obtainMessage(99, this.onCompleteMsg));
                    break;
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing RUIM record", exc);
            if (null != null) {
                onRecordLoaded();
            }
        } catch (Throwable th) {
            if (null != null) {
                onRecordLoaded();
            }
        }
    }

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

    protected void onAllRecordsLoaded() {
        log("record load complete");
        if (this.mParentApp != null && this.mParentApp.getType() == AppType.APPTYPE_RUIM) {
            String operator = getRUIMOperatorNumeric();
            try {
                Phone tPhoneBase = PhoneFactory.getPhone(this.mParentApp.getPhoneId());
                TelephonyManager.getDefault();
                boolean isRoaming = Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(this.mParentApp.getPhoneId(), "gsm.operator.isroaming", null));
                log("update icc_operator_numeric for isRoaming:" + isRoaming);
                if (isRoaming && ("46003".equals(operator) || "46011".equals(operator) || "45502".equals(operator))) {
                    String ims = tPhoneBase.getLteCdmaImsi(this.mParentApp.getPhoneId())[1];
                    if (ims != null && ims.length() >= 5) {
                        operator = ims.substring(0, 5);
                        log("update icc_operator_numeric for roaming:" + operator);
                    }
                }
            } catch (Exception e) {
            }
            if (TextUtils.isEmpty(operator)) {
                log("onAllRecordsLoaded empty 'gsm.sim.operator.numeric' skipping");
            } else {
                log("onAllRecordsLoaded set 'gsm.sim.operator.numeric' to operator='" + operator + "'");
                log("update icc_operator_numeric=" + operator);
                this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operator);
            }
            String imsi = getIMSI();
            if (TextUtils.isEmpty(imsi)) {
                log("onAllRecordsLoaded empty imsi skipping setting mcc");
            } else {
                log("onAllRecordsLoaded set mcc imsi=" + SpnOverride.MVNO_TYPE_NONE);
                this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), MccTable.countryCodeForMcc(Integer.parseInt(imsi.substring(0, 3))));
            }
        }
        if (Resources.getSystem().getBoolean(17957055)) {
            setSimLanguage(this.mEFli, this.mEFpl);
        }
        setOemSpnFromConfig(getOperatorNumeric());
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        if (!TextUtils.isEmpty(this.mMdn)) {
            int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mParentApp.getUiccCard().getPhoneId());
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                SubscriptionManager.from(this.mContext).setDisplayNumber(this.mMdn, subId);
            } else {
                log("Cannot call setDisplayNumber: invalid subId");
            }
        }
    }

    public void onReady() {
        fetchRuimRecords();
        this.mCi.getCDMASubscription(obtainMessage(10));
    }

    private void fetchRuimRecords() {
        if (this.mParentApp == null) {
            log("fetchRuimRecords: mParentApp == null");
            return;
        }
        this.mRecordsRequested = true;
        log("fetchRuimRecords " + this.mRecordsToLoad);
        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(5));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28474, obtainMessage(100, new EfCsimLiLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28481, obtainMessage(100, new EfCsimSpnLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_CSIM_MDN, 1, obtainMessage(100, new EfCsimMdnLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSIM_IMSIM, obtainMessage(100, new EfCsimImsimLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixedAll(IccConstants.EF_CSIM_CDMAHOME, obtainMessage(100, new EfCsimCdmaHomeLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSIM_EPRL, 4, obtainMessage(100, new EfCsimEprlLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSIM_MIPUPP, obtainMessage(100, new EfCsimMipUppLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.getEFLinearRecordSize(IccConstants.EF_SMS, obtainMessage(28));
        log("fetchRuimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    public int getDisplayRule(String plmn) {
        return 0;
    }

    public boolean isProvisioned() {
        if (SystemProperties.getBoolean("persist.radio.test-csim", false)) {
            return true;
        }
        if (this.mParentApp == null) {
            return false;
        }
        return (this.mParentApp.getType() == AppType.APPTYPE_CSIM && (this.mMdn == null || this.mMin == null)) ? false : true;
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        log("RuimRecords:setVoiceMessageWaiting - NOP for CDMA");
    }

    public int getVoiceMessageCount() {
        log("RuimRecords:getVoiceMessageCount - NOP for CDMA");
        return 0;
    }

    protected void handleFileUpdate(int efid) {
        this.mAdnCache.reset();
        fetchRuimRecords();
    }

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

    public boolean getCsimSpnDisplayCondition() {
        return this.mCsimSpnDisplayCondition;
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, "[RuimRecords] " + s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, "[RuimRecords] " + s);
    }

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

    private Object responseNetworkInfoWithActs(byte[] data) {
        this.mPlmnNumber = data.length / 5;
        Rlog.d("yangli", "mPlmnNumber:" + this.mPlmnNumber);
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
            if (data[i * 5] == (byte) -1 && data[(i * 5) + 1] == (byte) -1 && data[(i * 5) + 2] == (byte) -1) {
                this.mUsedPlmnNumber = i;
                Rlog.d("yangli", "now break ============mUsedPlmnNumber:" + this.mUsedPlmnNumber);
                break;
            }
            this.mPlmn[i] = getplmn(data[i * 5], data[(i * 5) + 1], data[(i * 5) + 2]);
            this.mOperatorNumeric[i] = Integer.toString(this.mPlmn[i]);
            Rlog.d("yangli", "plmn:" + this.mOperatorNumeric[i]);
            this.mOperatorAlphaName[i] = oppoGeOperatorByPlmn(this.mContext, this.mOperatorNumeric[i]);
            Rlog.d("yangli", "plmn name:" + this.mOperatorAlphaName[i]);
            mTechBit1 = getBooleanArray(data[(i * 5) + 3]);
            mTechBit2 = getBooleanArray(data[(i * 5) + 4]);
            this.mTech[i] = 0;
            if ((mTechBit1[0] == (byte) 1 || mTechBit1[1] == (byte) 1) && (mTechBit2[0] == (byte) 1 || mTechBit2[1] == (byte) 1)) {
                Rlog.d("yangli", "plmn:[" + i + "]:" + this.mPlmn[i] + "        tech is gsm and utran  ");
                this.mTech[i] = 0;
            } else if (mTechBit1[0] == (byte) 1) {
                Rlog.d("yangli", "plmn:[" + i + "]:" + this.mPlmn[i] + "        tech is UTRAN  ");
                this.mTech[i] = 3;
            } else if (mTechBit1[1] == (byte) 1) {
                Rlog.d("yangli", "plmn:[" + i + "]:" + this.mPlmn[i] + "        tech is E-UTRAN  ");
                this.mTech[i] = 4;
            } else if (mTechBit2[0] == (byte) 1 || mTechBit2[1] == (byte) 1) {
                Rlog.d("yangli", "plmn:[" + i + "]:" + this.mPlmn[i] + "    tech is gsm  ");
                this.mTech[i] = 1;
            } else if (mTechBit2[2] == (byte) 1 || mTechBit2[3] == (byte) 1) {
                Rlog.d("yangli", "plmn:[" + i + "]:" + this.mPlmn[i] + "        tech is cdma  ");
                this.mTech[i] = 2;
            }
            this.mUsedPlmnNumber++;
            i++;
        }
        ArrayList<CdmaNetworkInfoWithAcT> ret = new ArrayList(this.mUsedPlmnNumber);
        for (i = 0; i < this.mUsedPlmnNumber; i++) {
            if (this.mOperatorNumeric[i] != null) {
                Rlog.d("yangli", "CdmaNetworkInfoWithAcT add mOperatorAlphaName" + this.mOperatorAlphaName[i]);
                ret.add(new CdmaNetworkInfoWithAcT(this.mOperatorAlphaName[i], this.mOperatorNumeric[i], this.mTech[i], i));
            } else {
                Rlog.d(LOG_TAG, "responseNetworkInfoWithActs: invalid oper. i is " + i);
            }
        }
        return ret;
    }

    private void handleEfPOLResponse(byte[] data, Message msg) {
        Rlog.d("yangli", "handle response============");
        AsyncResult.forMessage(msg, responseNetworkInfoWithActs(data), null);
        msg.sendToTarget();
    }

    public void getPreferedOperatorList(Message onComplete, ServiceStateTracker msst) {
        Rlog.d("yangli", "simrecord getPreferedOperatorList ============");
        this.mSST = msst;
        this.onCompleteMsg = onComplete;
        this.mFh.loadEFTransparent(28512, obtainMessage(77, onComplete));
        Rlog.d("yangli", "eventGetPolError:" + this.eventGetPolError);
        if (this.eventGetPolError) {
            Rlog.d("yangli", "EFPLMNsel entry------");
            this.mFh.loadEFTransparent(28464, obtainMessage(99, onComplete));
            return;
        }
        Rlog.d("yangli", "EF_PLMNWACT entry------");
        this.mFh.loadEFTransparent(28512, obtainMessage(99, onComplete));
    }

    public byte[] formPlmnToByte(String plmn) {
        boolean mnc_includes_pcs_digit;
        int mcc;
        int mnc;
        int mnc_digit_1;
        int mnc_digit_2;
        int mnc_digit_3;
        byte[] ret = new byte[3];
        Rlog.d("yangli", "formPlmnToByte plmn:" + plmn);
        int plmnvalue = Integer.parseInt(plmn);
        if (plmnvalue > 99999) {
            Rlog.d("yangli", "mnc_includes_pcs_digit true");
            mnc_includes_pcs_digit = true;
        } else {
            Rlog.d("yangli", "mnc_includes_pcs_digit false");
            mnc_includes_pcs_digit = false;
        }
        if (mnc_includes_pcs_digit) {
            mcc = plmnvalue / 1000;
            mnc = plmnvalue - (mcc * 1000);
        } else {
            mcc = plmnvalue / 100;
            mnc = plmnvalue - (mcc * 100);
        }
        Rlog.d("yangli", "mcc:" + mcc + "   mnc" + mnc);
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
        Rlog.d("yangli", "mcc_digit_1:" + mcc_digit_1 + "   mcc_digit_2:" + mcc_digit_2 + "   mcc_digit_3:" + mcc_digit_3);
        Rlog.d("yangli", "mnc_digit_1:" + mnc_digit_1 + "   mnc_digit_2:" + mnc_digit_2 + "   mnc_digit_3:" + mnc_digit_3);
        ret[0] = (byte) ((mcc_digit_2 << 4) + mcc_digit_1);
        ret[1] = (byte) ((mnc_digit_3 << 4) + mcc_digit_3);
        ret[2] = (byte) ((mnc_digit_2 << 4) + mnc_digit_1);
        Rlog.d("yangli", "ret[0]:" + ret[0] + "   ret[1]:" + ret[1] + "   ret[2]:" + ret[2]);
        return ret;
    }

    public byte[] formRatToByte(int rat) {
        Rlog.d("yangli", "formRatToByte rat:" + rat);
        byte[] ret = new byte[2];
        if (rat == 0) {
            ret[0] = (byte) -64;
            ret[1] = Byte.MIN_VALUE;
            Rlog.d("yangli", "gsm+td+lte rat:" + rat);
        } else if (rat == 1) {
            ret[0] = (byte) 0;
            ret[1] = Byte.MIN_VALUE;
            Rlog.d("yangli", "gsm rat:" + rat);
        } else if (rat == 3) {
            ret[0] = Byte.MIN_VALUE;
            ret[1] = (byte) 0;
            Rlog.d("yangli", "td rat:" + rat);
        } else if (rat == 4) {
            ret[0] = (byte) 64;
            ret[1] = (byte) 0;
            Rlog.d("yangli", "lte rat:" + rat);
        } else {
            ret[0] = (byte) 0;
            ret[1] = (byte) 0;
            Rlog.d("yangli", "unknow rat:" + rat);
        }
        return ret;
    }

    public void setPOLEntry(CdmaNetworkInfoWithAcT networkWithAct, Message onComplete) {
        Rlog.d("yangli", "simrecord setPOLEntry ============");
        String plmn = networkWithAct.getOperatorNumeric();
        int act = networkWithAct.getAccessTechnology();
        int priority = networkWithAct.getPriority();
        this.mWriteBuffer = new byte[(this.mPlmnNumber * 5)];
        this.mWriteBuffer = this.mReadBuffer;
        if (priority < this.mPlmnNumber) {
            if (plmn == null) {
                Rlog.d("yangli", " setPOLEntry plmn is null , delete============");
                this.mWriteBuffer[(this.mUsedPlmnNumber - 1) * 5] = (byte) -1;
                this.mWriteBuffer[((this.mUsedPlmnNumber - 1) * 5) + 1] = (byte) -1;
                this.mWriteBuffer[((this.mUsedPlmnNumber - 1) * 5) + 2] = (byte) -1;
                this.mWriteBuffer[((this.mUsedPlmnNumber - 1) * 5) + 3] = (byte) 0;
                this.mWriteBuffer[((this.mUsedPlmnNumber - 1) * 5) + 4] = (byte) 0;
            } else {
                change = new byte[5];
                byte[] bplmn = new byte[3];
                byte[] brat = new byte[2];
                bplmn = formPlmnToByte(plmn);
                brat = formRatToByte(act);
                this.mWriteBuffer[priority * 5] = bplmn[0];
                this.mWriteBuffer[(priority * 5) + 1] = bplmn[1];
                this.mWriteBuffer[(priority * 5) + 2] = bplmn[2];
                this.mWriteBuffer[(priority * 5) + 3] = brat[0];
                this.mWriteBuffer[(priority * 5) + 4] = brat[1];
            }
            this.mFh.updateEFTransparent(28512, this.mWriteBuffer, obtainMessage(66, onComplete));
            Rlog.d("yangli", "eventSetPolError:" + this.eventSetPolError);
            if (this.eventSetPolError) {
                Rlog.d("yangli", "EFPLMNsel entry------");
                this.mFh.updateEFTransparent(28464, this.mWriteBuffer, obtainMessage(88, onComplete));
            } else {
                Rlog.d("yangli", "EF_PLMNWACT entry------");
                this.mFh.updateEFTransparent(28512, this.mWriteBuffer, obtainMessage(88, onComplete));
            }
        }
    }
}
