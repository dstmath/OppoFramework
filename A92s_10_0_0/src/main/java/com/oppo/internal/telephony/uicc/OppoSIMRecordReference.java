package com.oppo.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gsm.NetworkInfoWithAcT;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import com.android.internal.telephony.uicc.IOppoSIMRecords;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.OppoRIL;
import com.oppo.internal.telephony.OppoUiccManagerImpl;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import java.util.ArrayList;

public class OppoSIMRecordReference implements IOppoSIMRecords {
    protected static final String CHANGE_TO_REGION = "change_to_region";
    protected static final String CHINA_VERSION_VALUE = "CN";
    protected static final String FOREIGN_VERSION_VALUE = "US";
    protected static final String LOG_TAG = "OppoSIMRecord";
    private static final int POL_TECH_CDMA2000 = 2;
    private static final int POL_TECH_E_UTRAN = 4;
    private static final int POL_TECH_GSM = 1;
    private static final int POL_TECH_UNKNOW = 0;
    private static final int POL_TECH_UTRAN = 3;
    protected static final String PROPERTY_NETLOCK = "ro.oppo.region.netlock";
    protected static final String PROPERTY_OPERATOR = "ro.oppo.operator";
    protected static final String PROPERTY_REGION = "persist.sys.oppo.region";
    protected static final String PROPERTY_VERSION = "ro.oppo.version";
    private static final int TAG_FULL_NETWORK_NAME = 67;
    private static final int TAG_SHORT_NETWORK_NAME = 69;
    protected static final int UNINITIALIZED = -1;
    protected static final int UNKNOWN = 0;
    protected Context mContext;
    byte[] mEfpol;
    protected IccFileHandler mFh;
    String[] mOperatorAlphaName;
    private ArrayList<OplRecord> mOperatorList = null;
    String[] mOperatorNumeric;
    protected UiccCardApplication mParentApp;
    private Phone mPhone;
    int[] mPlmn;
    public int mPlmnNumber;
    private ArrayList<AbstractSIMRecords.OperatorName> mPnnNetworkNames = null;
    private String mPrlVersion = "";
    byte[] mReadBuffer;
    private SIMRecords mSIMRecords;
    int[] mTech;
    protected TelephonyManager mTelephonyManager;
    public int mUsedPlmnNumber;
    byte[] mWriteBuffer;

    public static class OplRecord {
        public int nMaxLAC;
        public int nMinLAC;
        public int nPnnIndex;
        public String sPlmn;
    }

    public OppoSIMRecordReference(AbstractSIMRecords simRecords) {
        this.mSIMRecords = (SIMRecords) OemTelephonyUtils.typeCasting(SIMRecords.class, simRecords);
        this.mTelephonyManager = TelephonyManager.getDefault();
        this.mPhone = simRecords.mPhone;
        this.mContext = simRecords.getContext();
        this.mFh = simRecords.getFh();
        this.mParentApp = simRecords.getParentApp();
    }

    public static boolean isNeedToChangeRegion(Context context) {
        if (Settings.Global.getInt(context.getContentResolver(), CHANGE_TO_REGION, 0) == 0) {
            return true;
        }
        return false;
    }

    public static void ChangeRegion(Context context, boolean isOn) {
        int state = 1;
        if (isOn) {
            state = 0;
        }
        Settings.Global.putInt(context.getContentResolver(), CHANGE_TO_REGION, state);
    }

    public static boolean isNetLockRegionMachine() {
        if (!SystemProperties.get("ro.oppo.region.netlock", "NULL").equals("NULL")) {
            return true;
        }
        return false;
    }

    public void oppoProcessChangeRegion(Context context, int slotId) {
        String version = SystemProperties.get(PROPERTY_VERSION, CHINA_VERSION_VALUE);
        if (!version.equals(FOREIGN_VERSION_VALUE)) {
            log("oppoProcessChangeRegion, return, version:" + version);
            return;
        }
        String operatorVersion = SystemProperties.get(PROPERTY_OPERATOR, "");
        if (isNeedToChangeRegion(context) && !isNetLockRegionMachine() && TextUtils.isEmpty(operatorVersion)) {
            String country = this.mTelephonyManager.getSimCountryIsoForPhone(slotId);
            String upperCountry = null;
            if (!TextUtils.isEmpty(country)) {
                upperCountry = country.toUpperCase();
            }
            if (!TextUtils.isEmpty(upperCountry) && upperCountry.equals(CHINA_VERSION_VALUE)) {
                upperCountry = "OC";
            }
            logd("upperCountry = " + upperCountry);
            String region = SystemProperties.get(PROPERTY_REGION, CHINA_VERSION_VALUE);
            if (!TextUtils.isEmpty(upperCountry) && !upperCountry.equals(region)) {
                Object loadRegionFeature = ReflectionHelper.callMethod(context.getPackageManager(), "android.content.pm.PackageManager", "loadRegionFeature", new Class[]{String.class}, new Object[]{upperCountry});
                boolean result = loadRegionFeature == null ? false : ((Boolean) loadRegionFeature).booleanValue();
                log("Need to change region,result " + result);
                if (result) {
                    SystemProperties.set(PROPERTY_REGION, upperCountry);
                    context.sendBroadcastAsUser(new Intent("android.settings.OPPO_REGION_CHANGED"), UserHandle.ALL);
                }
            }
            ChangeRegion(context, false);
        }
    }

    private int getplmn(byte data0, byte data1, byte data2) {
        int mnc_digit_1 = data2 & 15;
        int mnc_digit_2 = (data2 >> 4) & 15;
        int mnc_digit_3 = (data1 >> 4) & 15;
        int mcc = ((data0 & 15) * 100) + (((data0 >> 4) & 15) * 10) + (data1 & 15);
        if (mnc_digit_3 == 15) {
            return (mcc * 100) + (mnc_digit_1 * 10) + mnc_digit_2;
        }
        return (mcc * 1000) + (mnc_digit_1 * 100) + (mnc_digit_2 * 10) + mnc_digit_3;
    }

    private static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (byte i = 7; i >= 0; i = (byte) (i - 1)) {
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
            if (i2 < this.mPlmnNumber) {
                if (data[i2 * offset] == -1 && data[(i2 * offset) + 1] == -1 && data[(i2 * offset) + 2] == -1) {
                    this.mUsedPlmnNumber = i2;
                    log("responseNetworkInfoWithActs mccmnc is FFFFFF ,then break ============mUsedPlmnNumber:" + this.mUsedPlmnNumber);
                    break;
                }
                this.mPlmn[i2] = getplmn(data[i2 * offset], data[(i2 * offset) + 1], data[(i2 * offset) + 2]);
                this.mOperatorNumeric[i2] = Integer.toString(this.mPlmn[i2]);
                log("responseNetworkInfoWithActs plmn:" + this.mOperatorNumeric[i2]);
                this.mOperatorAlphaName[i2] = OppoPhoneUtil.oppoGeOperatorByPlmn(this.mContext, this.mOperatorNumeric[i2]);
                log("responseNetworkInfoWithActs plmn name:" + this.mOperatorAlphaName[i2]);
                this.mTech[i2] = 0;
                this.mUsedPlmnNumber = this.mUsedPlmnNumber + 1;
                if (fileid != 28464) {
                    byte[] mTechBit1 = getBooleanArray(data[(i2 * offset) + 3]);
                    byte[] mTechBit2 = getBooleanArray(data[(i2 * offset) + 4]);
                    if ((mTechBit1[0] == 1 || mTechBit1[1] == 1) && (mTechBit2[0] == 1 || mTechBit2[1] == 1)) {
                        log("responseNetworkInfoWithActs plmn:[" + i2 + "]:" + this.mPlmn[i2] + "        tech is gsm and utran  ");
                        this.mTech[i2] = 0;
                    } else if (mTechBit1[0] == 1) {
                        log("responseNetworkInfoWithActs plmn:[" + i2 + "]:" + this.mPlmn[i2] + "        tech is UTRAN  ");
                        this.mTech[i2] = 3;
                    } else if (mTechBit1[1] == 1) {
                        log("responseNetworkInfoWithActs plmn:[" + i2 + "]:" + this.mPlmn[i2] + "        tech is E-UTRAN  ");
                        this.mTech[i2] = 4;
                    } else if (mTechBit2[0] == 1 || mTechBit2[1] == 1) {
                        log("responseNetworkInfoWithActs plmn:[" + i2 + "]:" + this.mPlmn[i2] + "    tech is gsm  ");
                        this.mTech[i2] = 1;
                    } else if (mTechBit2[2] == 1 || mTechBit2[3] == 1) {
                        log("responseNetworkInfoWithActs plmn:[" + i2 + "]:" + this.mPlmn[i2] + "        tech is cdma  ");
                        this.mTech[i2] = 2;
                    }
                }
                i2++;
            } else {
                break;
            }
        }
        ArrayList<NetworkInfoWithAcT> ret = new ArrayList<>(this.mUsedPlmnNumber);
        for (int i3 = 0; i3 < this.mUsedPlmnNumber; i3++) {
            if (this.mOperatorNumeric[i3] != null) {
                log("responseNetworkInfoWithActs  add mOperatorAlphaName" + this.mOperatorAlphaName[i3]);
                ret.add(new NetworkInfoWithAcT(this.mOperatorAlphaName[i3], this.mOperatorNumeric[i3], this.mTech[i3], i3));
            } else {
                log("responseNetworkInfoWithActs: invalid oper. i is " + i3);
            }
        }
        return ret;
    }

    public void handleEfPOLResponse(int fileid, byte[] data, Message msg) {
        log("wjp_pol handle response============");
        AsyncResult.forMessage(msg, responseNetworkInfoWithActs(fileid, data), (Throwable) null);
        msg.sendToTarget();
    }

    public void getPreferedOperatorList(Message onComplete) {
        log("wjp_pol simrecord getPreferedOperatorList ============");
        ((AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, this.mSIMRecords)).onCompleteMsg = onComplete;
        this.mPlmnNumber = 0;
        this.mUsedPlmnNumber = 0;
        this.mFh.loadEFTransparent(28512, this.mSIMRecords.obtainMessage(99, 28512, 0, onComplete));
    }

    private byte[] formPlmnToByte(String plmn) {
        boolean mnc_includes_pcs_digit;
        int mnc;
        int mcc;
        int mnc_digit_3;
        int mnc_digit_2;
        int mnc_digit_1;
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
        log("wjp_pol ret[0]:" + ((int) ret[0]) + "   ret[1]:" + ((int) ret[1]) + "   ret[2]:" + ((int) ret[2]));
        return ret;
    }

    private byte[] formRatToByte(int rat) {
        log("wjp_pol formRatToByte rat:" + rat);
        byte[] ret = new byte[2];
        if (rat == 0) {
            ret[0] = -64;
            ret[1] = Byte.MIN_VALUE;
            log("wjp_pol gsm+td+lte rat:" + rat);
        } else if (rat == 1) {
            ret[0] = 0;
            ret[1] = Byte.MIN_VALUE;
            log("wjp_pol gsm rat:" + rat);
        } else if (rat == 3) {
            ret[0] = Byte.MIN_VALUE;
            ret[1] = 0;
            log("wjp_pol td rat:" + rat);
        } else if (rat == 4) {
            ret[0] = 64;
            ret[1] = 0;
            log("wjp_pol lte rat:" + rat);
        } else {
            ret[0] = 0;
            ret[1] = 0;
            log("wjp_pol unknow rat:" + rat);
        }
        return ret;
    }

    public void setPOLEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
        log("wjp_pol simrecord setPOLEntry ============");
        String plmn = networkWithAct.getOperatorNumeric();
        int act = networkWithAct.getAccessTechnology();
        int priority = networkWithAct.getPriority();
        int i = this.mPlmnNumber;
        this.mWriteBuffer = new byte[(i * 5)];
        this.mWriteBuffer = this.mReadBuffer;
        if (priority >= i) {
            onComplete.sendToTarget();
            return;
        }
        boolean bUsim = ((AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, this.mSIMRecords)).mApptype == IccCardApplicationStatus.AppType.APPTYPE_USIM;
        log("setPOLEntry bUsim: " + bUsim);
        int offset = 5;
        if (!bUsim) {
            offset = 3;
        }
        if (plmn == null) {
            log("wjp_pol  setPOLEntry plmn is null , delete============");
            byte[] bArr = this.mWriteBuffer;
            int i2 = this.mUsedPlmnNumber;
            bArr[(i2 - 1) * offset] = -1;
            bArr[((i2 - 1) * offset) + 1] = -1;
            bArr[((i2 - 1) * offset) + 2] = -1;
            if (bUsim) {
                bArr[((i2 - 1) * offset) + 3] = 0;
                bArr[((i2 - 1) * offset) + 4] = 0;
            }
        } else {
            byte[] bArr2 = new byte[5];
            byte[] bArr3 = new byte[3];
            byte[] bArr4 = new byte[2];
            byte[] bplmn = formPlmnToByte(plmn);
            byte[] brat = formRatToByte(act);
            byte[] bArr5 = this.mWriteBuffer;
            bArr5[priority * offset] = bplmn[0];
            bArr5[(priority * offset) + 1] = bplmn[1];
            bArr5[(priority * offset) + 2] = bplmn[2];
            if (bUsim) {
                bArr5[(priority * offset) + 3] = brat[0];
                bArr5[(priority * offset) + 4] = brat[1];
            }
        }
        if (SystemProperties.get(PROPERTY_VERSION, CHINA_VERSION_VALUE).equals(FOREIGN_VERSION_VALUE)) {
            log("setPOLEntry isLastItem: " + networkWithAct.isLastItem());
            if (!networkWithAct.isLastItem()) {
                onComplete.sendToTarget();
                return;
            }
        }
        if (bUsim) {
            this.mFh.updateEFTransparent(28512, this.mWriteBuffer, this.mSIMRecords.obtainMessage(88, onComplete));
        } else {
            this.mFh.updateEFTransparent(28464, this.mWriteBuffer, this.mSIMRecords.obtainMessage(88, onComplete));
        }
    }

    public void handlePlmnListData(Message response, byte[] result, Throwable ex) {
        AsyncResult.forMessage(response, result, ex);
        response.sendToTarget();
    }

    public void fetchCdmaPrl() {
        IccFileHandler handler;
        UiccCard card = UiccController.getInstance().getUiccCard(0);
        if (card != null) {
            int numApps = card.getNumApplications();
            UiccCardApplication app = null;
            IccCardApplicationStatus.AppType type = IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN;
            int i = 0;
            while (true) {
                if (i >= numApps) {
                    break;
                }
                app = card.getApplicationIndex(i);
                if (app != null) {
                    type = app.getType();
                    log("fetchCdmaPrl type=" + type);
                    if (type == IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
                        break;
                    } else if (type == IccCardApplicationStatus.AppType.APPTYPE_CSIM) {
                        break;
                    }
                }
                i++;
            }
            if (app == null) {
                return;
            }
            if ((type == IccCardApplicationStatus.AppType.APPTYPE_RUIM || type == IccCardApplicationStatus.AppType.APPTYPE_CSIM) && (handler = app.getIccFileHandler()) != null) {
                handler.loadEFTransparent(28506, 4, this.mSIMRecords.obtainMessage(100, new EfCsimEprlLoaded()));
            }
        }
    }

    private class EfCsimEprlLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimEprlLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_EPRL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            OppoSIMRecordReference.this.onGetCSimEprlDone(ar);
        }
    }

    /* access modifiers changed from: private */
    public void onGetCSimEprlDone(AsyncResult ar) {
        byte[] data = (byte[]) ar.result;
        log("CSIM_EPRL=" + IccUtils.bytesToHexString(data));
        if (data.length > 3) {
            this.mPrlVersion = Integer.toString(((data[2] & 255) << 8) | (data[3] & OppoRIL.MAX_MODEM_CRASH_CAUSE_LEN));
        }
        log("CSIM PRL version=" + this.mPrlVersion);
    }

    public String getPrlVersion() {
        return this.mPrlVersion;
    }

    public void oppoSetSimSpn(String spn) {
        if (!OppoUiccManagerImpl.getInstance().isHasSoftSimCard() || OppoUiccManagerImpl.getInstance().getSoftSimCardSlotId() != this.mParentApp.getPhoneId()) {
            log("Load EF_SPN oppo edit for not softsim");
            AbstractSIMRecords tmpSimRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, this.mSIMRecords);
            if (tmpSimRecords.getMncLength() == -1 || tmpSimRecords.getMncLength() == 0) {
                tmpSimRecords.isNeedSetSpnLater = true;
                log("can not get operatorNumeric due to mMncLength:" + tmpSimRecords.getMncLength());
            } else {
                tmpSimRecords.isNeedSetSpnLater = false;
                log("mMncLength is valid,no need set it later");
            }
            setSpnFromConfig(this.mSIMRecords.getOperatorNumeric());
            return;
        }
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
    }

    public void parseEFopl(ArrayList messages) {
        int count = messages.size();
        logd("parseEFopl(): opl has " + count + " records");
        this.mOperatorList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            byte[] data = (byte[]) messages.get(i);
            logd("parseEFopl(): opl record " + i + " content is " + IccUtils.bytesToHexString(data));
            OplRecord oplRec = new OplRecord();
            oplRec.sPlmn = parsePlmnToStringForEfOpl(data, 0, 3);
            logd("parseEFopl(): opl sPlmn = " + oplRec.sPlmn);
            oplRec.nMinLAC = Integer.parseInt(IccUtils.bytesToHexString(new byte[]{data[3], data[4]}), 16);
            logd("parseEFopl(): opl nMinLAC = " + oplRec.nMinLAC);
            oplRec.nMaxLAC = Integer.parseInt(IccUtils.bytesToHexString(new byte[]{data[5], data[6]}), 16);
            logd("parseEFopl(): opl nMaxLAC = " + oplRec.nMaxLAC);
            oplRec.nPnnIndex = Integer.parseInt(IccUtils.bytesToHexString(data).substring(14), 16);
            logd("parseEFopl(): opl nPnnIndex = " + oplRec.nPnnIndex);
            this.mOperatorList.add(oplRec);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00b4, code lost:
        logd("EONS getEonsIfExist: find it in EF_OPL");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00bb, code lost:
        if (r4.nPnnIndex != 0) goto L_0x00c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00bd, code lost:
        log("EONS getEonsIfExist: oplRec.nPnnIndex is 0 indicates that the name is to be taken from other sources");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00c2, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00c3, code lost:
        r1 = r4.nPnnIndex;
     */
    public String getEonsIfExist(String plmn, int nLac, boolean bLongNameRequired) {
        ArrayList<AbstractSIMRecords.OperatorName> arrayList;
        logd("EONS getEonsIfExist: plmn is " + plmn + " nLac is " + nLac + " bLongNameRequired: " + bLongNameRequired);
        if (plmn == null || (arrayList = this.mPnnNetworkNames) == null || arrayList.size() == 0) {
            return null;
        }
        int nPnnIndex = -1;
        boolean isHPLMN = isHPlmn(plmn);
        if (this.mOperatorList != null) {
            int i = 0;
            while (true) {
                if (i >= this.mOperatorList.size()) {
                    break;
                }
                OplRecord oplRec = this.mOperatorList.get(i);
                logd("EONS getEonsIfExist: record number is " + i + " sPlmn: " + oplRec.sPlmn + " nMinLAC: " + oplRec.nMinLAC + " nMaxLAC: " + oplRec.nMaxLAC + " PnnIndex " + oplRec.nPnnIndex);
                if (!isMatchingPlmnForEfOpl(oplRec.sPlmn, plmn) || (!(oplRec.nMinLAC == 0 && oplRec.nMaxLAC == 65534) && (oplRec.nMinLAC > nLac || oplRec.nMaxLAC < nLac))) {
                    i++;
                }
            }
        } else if (isHPLMN) {
            logd("EONS getEonsIfExist: Plmn is HPLMN, but no mOperatorList, return PNN's first record");
            nPnnIndex = 1;
        } else {
            logd("EONS getEonsIfExist: Plmn is not HPLMN, and no mOperatorList, return null");
            return null;
        }
        if (nPnnIndex == -1 && isHPLMN && this.mOperatorList.size() == 1) {
            logd("EONS getEonsIfExist: not find it in EF_OPL, but Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else if (nPnnIndex > 1 && nPnnIndex > this.mPnnNetworkNames.size() && isHPLMN) {
            logd("EONS getEonsIfExist: find it in EF_OPL, but index in EF_OPL > EF_PNN list length & Plmn is HPLMN, return PNN's first record");
            nPnnIndex = 1;
        } else if (nPnnIndex > 1 && nPnnIndex > this.mPnnNetworkNames.size() && !isHPLMN) {
            logd("EONS getEonsIfExist: find it in EF_OPL, but index in EF_OPL > EF_PNN list length & Plmn is not HPLMN, return PNN's first record");
            nPnnIndex = -1;
        }
        String sEons = null;
        if (nPnnIndex >= 1) {
            AbstractSIMRecords.OperatorName opName = this.mPnnNetworkNames.get(nPnnIndex - 1);
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
            String spn = this.mSIMRecords.getServiceProviderName();
            String simCardMccMnc = this.mSIMRecords.getOperatorNumeric();
            log("getEonsIfExist spn = " + spn + ", simCardMccMnc " + simCardMccMnc);
            if (!TextUtils.isEmpty(spn) && "50503".equals(simCardMccMnc) && "50503".equals(plmn)) {
                sEons = spn;
                log("sEons = " + sEons);
            }
        }
        logd("EONS getEonsIfExist: sEons is " + sEons);
        return sEons;
    }

    private boolean isHPlmn(String plmn) {
        boolean isHplmn = false;
        if (plmn != null) {
            String simNumeric = this.mTelephonyManager.getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
            logd("simNumeric: " + simNumeric);
            if (simNumeric != null && (simNumeric.equals(plmn) || (plmn.length() == 5 && simNumeric.length() == 6 && plmn.equals(simNumeric.substring(0, 5))))) {
                isHplmn = true;
            }
        }
        logd("plmn: " + plmn + "  isHplmn: " + isHplmn);
        return isHplmn;
    }

    private boolean isMatchingPlmnForEfOpl(String simPlmn, String bcchPlmn) {
        if (simPlmn == null || simPlmn.equals("") || bcchPlmn == null || bcchPlmn.equals("")) {
            return false;
        }
        logd("isMatchingPlmnForEfOpl(): simPlmn = " + simPlmn + ", bcchPlmn = " + bcchPlmn);
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

    private String parsePlmnToStringForEfOpl(byte[] data, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        int v = data[offset] & 15;
        if (v < 0 || v > 9) {
            if (v == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v + 48));
        int v2 = (data[offset] >> 4) & 15;
        if (v2 < 0 || v2 > 9) {
            if (v2 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v2 + 48));
        int v3 = data[offset + 1] & 15;
        if (v3 < 0 || v3 > 9) {
            if (v3 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v3 + 48));
        int v4 = data[offset + 2] & 15;
        if (v4 < 0 || v4 > 9) {
            if (v4 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v4 + 48));
        int v5 = (data[offset + 2] >> 4) & 15;
        if (v5 < 0 || v5 > 9) {
            if (v5 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v5 + 48));
        int v6 = (data[offset + 1] >> 4) & 15;
        if (v6 < 0 || v6 > 9) {
            if (v6 == 13) {
                ret.append('d');
            }
            return ret.toString();
        }
        ret.append((char) (v6 + 48));
        return ret.toString();
    }

    public String getSIMCPHSOns() {
        AbstractSIMRecords tmpSimRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, this.mSIMRecords);
        if (tmpSimRecords.cphsOnsl != null) {
            return tmpSimRecords.cphsOnsl;
        }
        return tmpSimRecords.cphsOnss;
    }

    public void fetchCPHSOns() {
        logd("fetchCPHSOns()");
        AbstractSIMRecords tmpSimRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, this.mSIMRecords);
        int recordsToLoad = tmpSimRecords.getRecordsToLoad();
        tmpSimRecords.cphsOnsl = null;
        tmpSimRecords.cphsOnss = null;
        this.mFh.loadEFTransparent(28436, this.mSIMRecords.obtainMessage(502));
        this.mFh.loadEFTransparent(28440, this.mSIMRecords.obtainMessage(503));
        tmpSimRecords.setRecordsToLoad(recordsToLoad + 1 + 1);
    }

    public String getSpNameInEfSpn() {
        AbstractSIMRecords tmpSimRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, this.mSIMRecords);
        logd("getSpNameInEfSpn(): " + tmpSimRecords.mSpNameInEfSpn);
        return tmpSimRecords.mSpNameInEfSpn;
    }

    public String getFirstFullNameInEfPnn() {
        ArrayList<AbstractSIMRecords.OperatorName> arrayList = this.mPnnNetworkNames;
        if (arrayList == null || arrayList.size() == 0) {
            logd("getFirstFullNameInEfPnn(): empty");
            return null;
        }
        AbstractSIMRecords.OperatorName opName = this.mPnnNetworkNames.get(0);
        logd("getFirstFullNameInEfPnn(): first fullname: " + opName.sFullName);
        if (opName.sFullName != null) {
            return new String(opName.sFullName);
        }
        return null;
    }

    public void fetchPnnAndOpl() {
        logd("fetchPnnAndOpl()");
        boolean bPnnActive = false;
        boolean bOplActive = false;
        AbstractSIMRecords tmpSimRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, this.mSIMRecords);
        if (tmpSimRecords.mEfSST != null) {
            if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
                if (tmpSimRecords.mEfSST.length >= 6) {
                    bPnnActive = (tmpSimRecords.mEfSST[5] & 16) == 16;
                    if (bPnnActive) {
                        bOplActive = (tmpSimRecords.mEfSST[5] & 32) == 32;
                    }
                }
            } else if (tmpSimRecords.mEfSST.length >= 13) {
                bPnnActive = (tmpSimRecords.mEfSST[12] & 48) == 48;
                if (bPnnActive) {
                    bOplActive = (tmpSimRecords.mEfSST[12] & 192) == 192;
                }
            }
        }
        logd("bPnnActive = " + bPnnActive + ", bOplActive = " + bOplActive);
        boolean pnnDebug = SystemProperties.getBoolean("persist.sys.pnn.debug", false);
        if (this.mFh == null) {
            return;
        }
        if (bPnnActive || pnnDebug) {
            int recordsToLoad = tmpSimRecords.getRecordsToLoad();
            logd("start get pnn all");
            this.mFh.loadEFLinearFixedAll(28613, this.mSIMRecords.obtainMessage(500));
            int recordsToLoad2 = recordsToLoad + 1;
            if (bOplActive) {
                this.mFh.loadEFLinearFixedAll(28614, this.mSIMRecords.obtainMessage(501));
                recordsToLoad2++;
            }
            tmpSimRecords.setRecordsToLoad(recordsToLoad2);
        }
    }

    public void parseEFpnn(ArrayList messages) {
        int count = messages.size();
        logd("parseEFpnn(): pnn has " + count + " records");
        this.mPnnNetworkNames = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            byte[] data = (byte[]) messages.get(i);
            logd("parseEFpnn(): pnn record " + i + " content is " + IccUtils.bytesToHexString(data));
            SimTlv tlv = new SimTlv(data, 0, data.length);
            AbstractSIMRecords.OperatorName opName = new AbstractSIMRecords.OperatorName();
            while (tlv.isValidObject()) {
                if (tlv.getTag() == 67) {
                    opName.sFullName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                    logd("parseEFpnn(): pnn sFullName is " + opName.sFullName);
                } else if (tlv.getTag() == 69) {
                    opName.sShortName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                    logd("parseEFpnn(): pnn sShortName is " + opName.sShortName);
                }
                tlv.nextObject();
            }
            this.mPnnNetworkNames.add(opName);
        }
    }

    public AbstractSIMRecords.OperatorName getEFpnnNetworkNames(int index) {
        ArrayList<AbstractSIMRecords.OperatorName> arrayList = this.mPnnNetworkNames;
        if (arrayList == null || index >= arrayList.size()) {
            return null;
        }
        return this.mPnnNetworkNames.get(index);
    }

    public void setSpnFromConfig(String carrier) {
        String spn = this.mSIMRecords.getServiceProviderName();
        AbstractSIMRecords tmpSimRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, this.mSIMRecords);
        boolean isCnList = OemTelephonyUtils.isInCnList(this.mContext, spn);
        if (isCnList || TextUtils.isEmpty(spn) || (spn != null && spn.startsWith("460"))) {
            if (isCnList && "20404".equals(carrier)) {
                carrier = "46011";
            }
            String operator = OppoUiccManagerImpl.getInstance().getOemOperator(this.mContext, carrier);
            if (!TextUtils.isEmpty(operator)) {
                tmpSimRecords.setServiceProviderName(operator);
            }
        }
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), this.mSIMRecords.getServiceProviderName());
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x003b, code lost:
        if (r0.equals("50218") != false) goto L_0x005d;
     */
    public void processImsiReadComplete(String imsi) {
        AbstractSIMRecords tmpRecords = (AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, this.mSIMRecords);
        int i = 2;
        if (imsi != null) {
            boolean z = false;
            String mccMnc = imsi.substring(0, 4);
            switch (mccMnc.hashCode()) {
                case 47832434:
                    if (mccMnc.equals("26006")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 47832715:
                    if (mccMnc.equals("26098")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 49685241:
                    if (mccMnc.equals("46605")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 50426206:
                    break;
                case 50483832:
                    if (mccMnc.equals("52005")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 50483866:
                    if (mccMnc.equals("52018")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                default:
                    z = true;
                    break;
            }
            if (!z || z || z || z) {
                setSystemProperty("gsm.sim.operator.numeric", mccMnc);
            } else if ((z || z) && !TelephonyManager.getDefault().isMultiSimEnabled()) {
                tmpRecords.setServiceProviderName("DTAC");
                setSystemProperty("gsm.sim.operator.alpha", "DTAC");
            }
        }
        tmpRecords.mIsTestCard = OemTelephonyUtils.isTestCard(imsi);
        log("mIsTestCard: " + tmpRecords.mIsTestCard);
        String netBuildType = SystemProperties.get("persist.sys.net_build_type", "allnet");
        String nw_lab_antpos = SystemProperties.get("persist.sys.nw_lab_antpos", "ant_pos_default");
        if ("allnetcmcctest".equals(netBuildType) || "ant_pos_down".equals(nw_lab_antpos)) {
            log("Force set any card as test card");
            tmpRecords.mIsTestCard = true;
        }
        AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, this.mPhone);
        if (!tmpRecords.mIsTestCard) {
            i = 1;
        }
        tmpPhone.oppoSetSimType(i);
    }

    /* access modifiers changed from: protected */
    public void setSystemProperty(String key, String val) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(this.mParentApp.getPhoneId(), key, val);
        log("[key, value]=" + key + ", " + val);
    }

    public void dispose() {
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(LOG_TAG, s + ",phone:" + this.mParentApp.getPhoneId());
        }
    }

    /* access modifiers changed from: package-private */
    public void log(String s) {
        Rlog.d(LOG_TAG, s + ",phone:" + this.mParentApp.getPhoneId());
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(LOG_TAG, s + ",phone:" + this.mParentApp.getPhoneId());
    }
}
