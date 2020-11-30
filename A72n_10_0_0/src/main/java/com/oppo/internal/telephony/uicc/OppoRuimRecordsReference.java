package com.oppo.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.cdma.CdmaNetworkInfoWithAcT;
import com.android.internal.telephony.uicc.AbstractRuimRecords;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import com.android.internal.telephony.uicc.IOppoRuimRecords;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import java.util.ArrayList;

public class OppoRuimRecordsReference implements IOppoRuimRecords {
    protected static final boolean DBG = true;
    protected static final String LOG_TAG = "OppoRuimRecords";
    private static final int POL_TECH_CDMA2000 = 2;
    private static final int POL_TECH_E_UTRAN = 4;
    private static final int POL_TECH_GSM = 1;
    private static final int POL_TECH_UNKNOW = 0;
    private static final int POL_TECH_UTRAN = 3;
    protected static final boolean VDBG = false;
    private boolean eventGetPolError = false;
    private boolean eventSetPolError = false;
    protected Context mContext;
    byte[] mEfpol;
    protected IccFileHandler mFh;
    String[] mOperatorAlphaName;
    String[] mOperatorNumeric;
    protected UiccCardApplication mParentApp;
    int[] mPlmn;
    public int mPlmnNumber;
    byte[] mReadBuffer;
    private RuimRecords mRuimRecords;
    int[] mTech;
    protected TelephonyManager mTelephonyManager;
    public int mUsedPlmnNumber;
    byte[] mWriteBuffer;
    protected Message onCompleteMsg;

    public OppoRuimRecordsReference(AbstractRuimRecords ruimRecords) {
        this.mRuimRecords = (RuimRecords) OemTelephonyUtils.typeCasting(RuimRecords.class, ruimRecords);
        this.mParentApp = ruimRecords.getApp();
        this.mContext = ruimRecords.getContext();
        this.mFh = ruimRecords.getFh();
        log("sim type=" + this.mParentApp.getType());
        this.mTelephonyManager = TelephonyManager.getDefault();
    }

    public void onEfCsimImsimRecordLoaded(String mImsi) {
        int i = 1;
        UiccCardApplication usimApp = UiccController.getInstance().getUiccCardApplication(this.mParentApp.getPhoneId(), 1);
        IccRecords simRecord = null;
        if (usimApp != null) {
            simRecord = usimApp.getIccRecords();
            log("dual modem sim card");
        }
        AbstractSIMRecords tmpSimRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, simRecord);
        AbstractRuimRecords tmpRuimRecords = (AbstractRuimRecords) OemTelephonyUtils.typeCasting(AbstractRuimRecords.class, this.mRuimRecords);
        if (tmpSimRecords != null && tmpSimRecords.mIsTestCard) {
            tmpRuimRecords.mIsTestCard = tmpSimRecords.mIsTestCard;
            log("leon mIsTestCard 33: " + tmpRuimRecords.mIsTestCard);
        } else if (mImsi != null) {
            tmpRuimRecords.mIsTestCard = OemTelephonyUtils.isTestCard(mImsi);
            log("leon mIsTestCard 3: " + tmpRuimRecords.mIsTestCard);
        } else {
            tmpRuimRecords.mIsTestCard = false;
            log("leon mIsTestCard 333,can not check the TestCard,set to false");
        }
        String netBuildType = SystemProperties.get("persist.sys.net_build_type", "allnet");
        String nw_lab_antpos = SystemProperties.get("persist.sys.nw_lab_antpos", "ant_pos_default");
        if ("allnetcmcctest".equals(netBuildType) || "ant_pos_down".equals(nw_lab_antpos)) {
            log("Force set any card as test card");
            tmpRuimRecords.mIsTestCard = DBG;
        }
        AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, PhoneFactory.getPhone(this.mParentApp.getPhoneId()));
        if (tmpRuimRecords.mIsTestCard) {
            i = 2;
        }
        tmpPhone.oppoSetSimType(i);
    }

    public void handleMessage(Message msg) {
        try {
            int i = msg.what;
            if (i == 66) {
                this.eventSetPolError = DBG;
            } else if (i == 77) {
                this.eventGetPolError = DBG;
            } else if (i == 88) {
                Rlog.d(LOG_TAG, "EVENT_SET_POL_DONE");
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.d(LOG_TAG, "Exception in EVENT_SET_POL_DONE EF POL data" + ar.exception);
                    loge("Exception in EVENT_SET_POL_DONE EF POL data " + ar.exception);
                } else if (ar.userObj != null) {
                    AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                    ((Message) ar.userObj).sendToTarget();
                }
            } else if (i != 99) {
                loge("Unexpected message " + msg.what);
            } else {
                Rlog.d(LOG_TAG, "EVENT_GET_POL_DONE");
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception != null) {
                    Rlog.d(LOG_TAG, "Exception in fetching EF POL data" + ar2.exception);
                    loge("Exception in fetching EF POL data " + ar2.exception);
                    this.mFh.loadEFTransparent(28464, this.mRuimRecords.obtainMessage(99, this.onCompleteMsg));
                    return;
                }
                byte[] data = (byte[]) ar2.result;
                Rlog.d(LOG_TAG, "EVENT_GET_POL_DONE data " + IccUtils.bytesToHexString(data));
                if (ar2.userObj != null) {
                    AsyncResult.forMessage((Message) ar2.userObj).exception = ar2.exception;
                    handleEfPOLResponse(data, (Message) ar2.userObj);
                }
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing RUIM record", exc);
        }
    }

    public void onAllRecordsLoaded() {
        String ims;
        UiccCardApplication uiccCardApplication = this.mParentApp;
        if (uiccCardApplication != null && uiccCardApplication.getType() == IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
            String operator = this.mRuimRecords.getRUIMOperatorNumeric();
            try {
                Phone tPhoneBase = PhoneFactory.getPhone(this.mParentApp.getPhoneId());
                boolean isRoaming = false;
                TelephonyManager.getDefault();
                String telephonyProperty = TelephonyManager.getTelephonyProperty(this.mParentApp.getPhoneId(), "gsm.operator.isroaming", null);
                if (telephonyProperty != null) {
                    isRoaming = Boolean.parseBoolean(telephonyProperty);
                }
                log("update icc_operator_numeric for isRoaming:" + isRoaming);
                if (isRoaming && (("46003".equals(operator) || "46011".equals(operator) || "45502".equals(operator)) && (ims = ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, tPhoneBase)).getLteCdmaImsi(this.mParentApp.getPhoneId())[1]) != null && ims.length() >= 5)) {
                    operator = ims.substring(0, 5);
                    log("update icc_operator_numeric for roaming:" + operator);
                }
            } catch (Exception e) {
            }
            if (!TextUtils.isEmpty(operator)) {
                log("onAllRecordsLoaded set 'gsm.sim.operator.numeric' to operator='" + operator + "'");
                StringBuilder sb = new StringBuilder();
                sb.append("update icc_operator_numeric=");
                sb.append(operator);
                log(sb.toString());
                this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operator);
            } else {
                log("onAllRecordsLoaded empty 'gsm.sim.operator.numeric' skipping");
            }
            String imsi = this.mRuimRecords.getIMSI();
            if (!TextUtils.isEmpty(imsi)) {
                log("onAllRecordsLoaded set mcc imsi=");
                this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), MccTable.countryCodeForMcc(imsi.substring(0, 3)));
                return;
            }
            log("onAllRecordsLoaded empty imsi skipping setting mcc");
        }
    }

    private class EfAdLoaded implements IccRecords.IccRecordLoaded {
        private EfAdLoaded() {
        }

        public String getEfName() {
            return "EF_AD";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            if (ar.exception == null) {
                OppoRuimRecordsReference.this.log("EF_AD: " + IccUtils.bytesToHexString(data));
                if (data.length < 3) {
                    OppoRuimRecordsReference.this.log("Corrupt AD data on SIM");
                } else if (data.length == 3) {
                    OppoRuimRecordsReference.this.log("MNC length not present in EF_AD");
                } else {
                    AbstractRuimRecords tmpRuimRecords = (AbstractRuimRecords) OemTelephonyUtils.typeCasting(AbstractRuimRecords.class, OppoRuimRecordsReference.this.mRuimRecords);
                    boolean z = false;
                    int i = 1;
                    if (tmpRuimRecords.mIsTestCard || OemTelephonyUtils.isTestCard(OppoRuimRecordsReference.this.mContext, data[0])) {
                        z = true;
                    }
                    tmpRuimRecords.mIsTestCard = z;
                    String netBuildType = SystemProperties.get("persist.sys.net_build_type", "allnet");
                    String nw_lab_antpos = SystemProperties.get("persist.sys.nw_lab_antpos", "ant_pos_default");
                    if ("allnetcmcctest".equals(netBuildType) || "ant_pos_down".equals(nw_lab_antpos)) {
                        OppoRuimRecordsReference.this.log("Force set any card as test card");
                        tmpRuimRecords.mIsTestCard = OppoRuimRecordsReference.DBG;
                    }
                    AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, PhoneFactory.getPhone(OppoRuimRecordsReference.this.mParentApp.getPhoneId()));
                    if (tmpRuimRecords.mIsTestCard) {
                        i = 2;
                    }
                    tmpPhone.oppoSetSimType(i);
                    OppoRuimRecordsReference.this.log("leon mIsTestCard 4: " + tmpRuimRecords.mIsTestCard);
                }
            }
        }
    }

    public int getplmn(byte data0, byte data1, byte data2) {
        int mnc_digit_1 = data2 & 15;
        int mnc_digit_2 = (data2 >> 4) & 15;
        int mnc_digit_3 = (data1 >> 4) & 15;
        int mcc = ((data0 & 15) * 100) + (((data0 >> 4) & 15) * 10) + (data1 & 15);
        if (mnc_digit_3 == 15) {
            return (mcc * 100) + (mnc_digit_1 * 10) + mnc_digit_2;
        }
        return (mcc * 1000) + (mnc_digit_1 * 100) + (mnc_digit_2 * 10) + mnc_digit_3;
    }

    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (byte i = 7; i >= 0; i = (byte) (i - 1)) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    private Object responseNetworkInfoWithActs(byte[] data) {
        this.mPlmnNumber = data.length / 5;
        Rlog.d(LOG_TAG, "mPlmnNumber:" + this.mPlmnNumber);
        int i = this.mPlmnNumber;
        this.mPlmn = new int[i];
        this.mTech = new int[i];
        this.mOperatorAlphaName = new String[i];
        this.mOperatorNumeric = new String[i];
        byte[] bArr = new byte[8];
        byte[] bArr2 = new byte[8];
        this.mReadBuffer = new byte[data.length];
        this.mReadBuffer = data;
        this.mUsedPlmnNumber = 0;
        int i2 = 0;
        while (true) {
            if (i2 >= this.mPlmnNumber) {
                break;
            }
            if (data[i2 * 5] == -1 && data[(i2 * 5) + 1] == -1 && data[(i2 * 5) + 2] == -1) {
                this.mUsedPlmnNumber = i2;
                Rlog.d(LOG_TAG, "now break ============mUsedPlmnNumber:" + this.mUsedPlmnNumber);
                break;
            }
            this.mPlmn[i2] = getplmn(data[i2 * 5], data[(i2 * 5) + 1], data[(i2 * 5) + 2]);
            this.mOperatorNumeric[i2] = Integer.toString(this.mPlmn[i2]);
            Rlog.d(LOG_TAG, "plmn:" + this.mOperatorNumeric[i2]);
            this.mOperatorAlphaName[i2] = OppoPhoneUtil.oppoGeOperatorByPlmn(this.mContext, this.mOperatorNumeric[i2]);
            Rlog.d(LOG_TAG, "plmn name:" + this.mOperatorAlphaName[i2]);
            byte[] mTechBit1 = getBooleanArray(data[(i2 * 5) + 3]);
            byte[] mTechBit2 = getBooleanArray(data[(i2 * 5) + 4]);
            this.mTech[i2] = 0;
            if ((mTechBit1[0] == 1 || mTechBit1[1] == 1) && (mTechBit2[0] == 1 || mTechBit2[1] == 1)) {
                Rlog.d(LOG_TAG, "plmn:[" + i2 + "]:" + this.mPlmn[i2] + "        tech is gsm and utran  ");
                this.mTech[i2] = 0;
            } else if (mTechBit1[0] == 1) {
                Rlog.d(LOG_TAG, "plmn:[" + i2 + "]:" + this.mPlmn[i2] + "        tech is UTRAN  ");
                this.mTech[i2] = 3;
            } else if (mTechBit1[1] == 1) {
                Rlog.d(LOG_TAG, "plmn:[" + i2 + "]:" + this.mPlmn[i2] + "        tech is E-UTRAN  ");
                this.mTech[i2] = 4;
            } else if (mTechBit2[0] == 1 || mTechBit2[1] == 1) {
                Rlog.d(LOG_TAG, "plmn:[" + i2 + "]:" + this.mPlmn[i2] + "    tech is gsm  ");
                this.mTech[i2] = 1;
            } else if (mTechBit2[2] == 1 || mTechBit2[3] == 1) {
                Rlog.d(LOG_TAG, "plmn:[" + i2 + "]:" + this.mPlmn[i2] + "        tech is cdma  ");
                this.mTech[i2] = 2;
            }
            this.mUsedPlmnNumber++;
            i2++;
        }
        ArrayList<CdmaNetworkInfoWithAcT> ret = new ArrayList<>(this.mUsedPlmnNumber);
        for (int i3 = 0; i3 < this.mUsedPlmnNumber; i3++) {
            if (this.mOperatorNumeric[i3] != null) {
                Rlog.d(LOG_TAG, "CdmaNetworkInfoWithAcT add mOperatorAlphaName" + this.mOperatorAlphaName[i3]);
                ret.add(new CdmaNetworkInfoWithAcT(this.mOperatorAlphaName[i3], this.mOperatorNumeric[i3], this.mTech[i3], i3));
            } else {
                Rlog.d(LOG_TAG, "responseNetworkInfoWithActs: invalid oper. i is " + i3);
            }
        }
        return ret;
    }

    private void handleEfPOLResponse(byte[] data, Message msg) {
        Rlog.d(LOG_TAG, "handle response============");
        AsyncResult.forMessage(msg, responseNetworkInfoWithActs(data), (Throwable) null);
        msg.sendToTarget();
    }

    public void getPreferedOperatorList(Message onComplete) {
        Rlog.d(LOG_TAG, "simrecord getPreferedOperatorList ============");
        this.onCompleteMsg = onComplete;
        this.mFh.loadEFTransparent(28512, this.mRuimRecords.obtainMessage(77, onComplete));
        Rlog.d(LOG_TAG, "eventGetPolError:" + this.eventGetPolError);
        if (!this.eventGetPolError) {
            Rlog.d(LOG_TAG, "EF_PLMNWACT entry------");
            this.mFh.loadEFTransparent(28512, this.mRuimRecords.obtainMessage(99, onComplete));
            return;
        }
        Rlog.d(LOG_TAG, "OemConstant.EFPLMNsel entry------");
        this.mFh.loadEFTransparent(28464, this.mRuimRecords.obtainMessage(99, onComplete));
    }

    public byte[] formPlmnToByte(String plmn) {
        boolean mnc_includes_pcs_digit;
        int mnc;
        int mcc;
        int mnc_digit_3;
        int mnc_digit_2;
        int mnc_digit_1;
        byte[] ret = new byte[3];
        Rlog.d(LOG_TAG, "formPlmnToByte plmn:" + plmn);
        int plmnvalue = Integer.parseInt(plmn);
        if (plmnvalue > 99999) {
            Rlog.d(LOG_TAG, "mnc_includes_pcs_digit true");
            mnc_includes_pcs_digit = DBG;
        } else {
            Rlog.d(LOG_TAG, "mnc_includes_pcs_digit false");
            mnc_includes_pcs_digit = false;
        }
        if (mnc_includes_pcs_digit) {
            mcc = plmnvalue / 1000;
            mnc = plmnvalue - (mcc * 1000);
        } else {
            mcc = plmnvalue / 100;
            mnc = plmnvalue - (mcc * 100);
        }
        Rlog.d(LOG_TAG, "mcc:" + mcc + "   mnc" + mnc);
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
        Rlog.d(LOG_TAG, "mcc_digit_1:" + mcc_digit_1 + "   mcc_digit_2:" + mcc_digit_2 + "   mcc_digit_3:" + mcc_digit_3);
        Rlog.d(LOG_TAG, "mnc_digit_1:" + mnc_digit_1 + "   mnc_digit_2:" + mnc_digit_2 + "   mnc_digit_3:" + mnc_digit_3);
        ret[0] = (byte) ((mcc_digit_2 << 4) + mcc_digit_1);
        ret[1] = (byte) ((mnc_digit_3 << 4) + mcc_digit_3);
        ret[2] = (byte) ((mnc_digit_2 << 4) + mnc_digit_1);
        Rlog.d(LOG_TAG, "ret[0]:" + ((int) ret[0]) + "   ret[1]:" + ((int) ret[1]) + "   ret[2]:" + ((int) ret[2]));
        return ret;
    }

    public byte[] formRatToByte(int rat) {
        Rlog.d(LOG_TAG, "formRatToByte rat:" + rat);
        byte[] ret = new byte[2];
        if (rat == 0) {
            ret[0] = -64;
            ret[1] = Byte.MIN_VALUE;
            Rlog.d(LOG_TAG, "gsm+td+lte rat:" + rat);
        } else if (rat == 1) {
            ret[0] = 0;
            ret[1] = Byte.MIN_VALUE;
            Rlog.d(LOG_TAG, "gsm rat:" + rat);
        } else if (rat == 3) {
            ret[0] = Byte.MIN_VALUE;
            ret[1] = 0;
            Rlog.d(LOG_TAG, "td rat:" + rat);
        } else if (rat == 4) {
            ret[0] = 64;
            ret[1] = 0;
            Rlog.d(LOG_TAG, "lte rat:" + rat);
        } else {
            ret[0] = 0;
            ret[1] = 0;
            Rlog.d(LOG_TAG, "unknow rat:" + rat);
        }
        return ret;
    }

    public void setPOLEntry(CdmaNetworkInfoWithAcT networkWithAct, Message onComplete) {
        Rlog.d(LOG_TAG, "simrecord setPOLEntry ============");
        String plmn = networkWithAct.getOperatorNumeric();
        int act = networkWithAct.getAccessTechnology();
        int priority = networkWithAct.getPriority();
        int i = this.mPlmnNumber;
        this.mWriteBuffer = new byte[(i * 5)];
        this.mWriteBuffer = this.mReadBuffer;
        if (priority < i) {
            if (plmn == null) {
                Rlog.d(LOG_TAG, " setPOLEntry plmn is null , delete============");
                byte[] bArr = this.mWriteBuffer;
                int i2 = this.mUsedPlmnNumber;
                bArr[(i2 - 1) * 5] = -1;
                bArr[((i2 - 1) * 5) + 1] = -1;
                bArr[((i2 - 1) * 5) + 2] = -1;
                bArr[((i2 - 1) * 5) + 3] = 0;
                bArr[((i2 - 1) * 5) + 4] = 0;
            } else {
                byte[] bArr2 = new byte[5];
                byte[] bArr3 = new byte[3];
                byte[] bArr4 = new byte[2];
                byte[] bplmn = formPlmnToByte(plmn);
                byte[] brat = formRatToByte(act);
                byte[] bArr5 = this.mWriteBuffer;
                bArr5[priority * 5] = bplmn[0];
                bArr5[(priority * 5) + 1] = bplmn[1];
                bArr5[(priority * 5) + 2] = bplmn[2];
                bArr5[(priority * 5) + 3] = brat[0];
                bArr5[(priority * 5) + 4] = brat[1];
            }
            this.mFh.updateEFTransparent(28512, this.mWriteBuffer, this.mRuimRecords.obtainMessage(66, onComplete));
            Rlog.d(LOG_TAG, "eventSetPolError:" + this.eventSetPolError);
            if (!this.eventSetPolError) {
                Rlog.d(LOG_TAG, "EF_PLMNWACT entry------");
                this.mFh.updateEFTransparent(28512, this.mWriteBuffer, this.mRuimRecords.obtainMessage(88, onComplete));
                return;
            }
            Rlog.d(LOG_TAG, "OemConstant.EFPLMNsel entry------");
            this.mFh.updateEFTransparent(28464, this.mWriteBuffer, this.mRuimRecords.obtainMessage(88, onComplete));
        }
    }

    public void dispose() {
        log("RuimRecords: set 'gsm.sim.operator.numeric' to operator=null");
        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), "");
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(LOG_TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}
