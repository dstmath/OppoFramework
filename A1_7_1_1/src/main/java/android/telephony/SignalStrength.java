package android.telephony;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import com.android.internal.R;
import com.mediatek.common.telephony.IServiceStateExt;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SignalStrength implements Parcelable {
    public static final Creator<SignalStrength> CREATOR = null;
    private static final boolean DBG = false;
    public static final int INVALID = Integer.MAX_VALUE;
    private static final boolean IS_BSP_PACKAGE = true;
    private static final boolean IS_ENG_LOAD = false;
    private static final String LOG_TAG = "SignalStrength";
    public static final int NT_CDMA = 5;
    public static final int NT_EVDO = 6;
    public static final int NT_GSM = 1;
    public static final int NT_LTE = 3;
    public static final int NT_TDS = 2;
    public static final int NT_UNKNOWN = 0;
    public static final int NT_WCDMA = 4;
    public static final int NUM_SIGNAL_STRENGTH_BINS = 5;
    protected static final boolean ODBG = true;
    public static int OEM_LEVLE = 0;
    private static final int[] RSRP_THRESH_LENIENT = null;
    private static final int[] RSRP_THRESH_STRICT = null;
    private static final int RSRP_THRESH_TYPE_STRICT = 0;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final String[] SIGNAL_STRENGTH_NAMES = null;
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;
    private static IServiceStateExt mServiceStateExt;
    private final boolean VENDOR_EDIT;
    private boolean isGsm;
    private int mCdmaDbm;
    private int mCdmaEcio;
    private int mEvdoDbm;
    private int mEvdoEcio;
    private int mEvdoSnr;
    private int mGsmBitErrorRate;
    private int mGsmEcn0Qdbm;
    private int mGsmRscpQdbm;
    private int mGsmRssiQdbm;
    private int mGsmSignalStrength;
    private int mLteCqi;
    private int mLteRsrp;
    private int mLteRsrq;
    private int mLteRssnr;
    private int mLteSignalStrength;
    public int mOEMLevel_0;
    public int mOEMLevel_1;
    private int mTdScdmaRscp;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telephony.SignalStrength.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telephony.SignalStrength.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SignalStrength.<clinit>():void");
    }

    public static SignalStrength newFromBundle(Bundle m) {
        SignalStrength ret = new SignalStrength();
        ret.setFromNotifierBundle(m);
        return ret;
    }

    public SignalStrength() {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.VENDOR_EDIT = true;
        this.mGsmSignalStrength = 99;
        this.mGsmBitErrorRate = -1;
        this.mCdmaDbm = -1;
        this.mCdmaEcio = -1;
        this.mEvdoDbm = -1;
        this.mEvdoEcio = -1;
        this.mEvdoSnr = -1;
        this.mLteSignalStrength = 99;
        this.mLteRsrp = Integer.MAX_VALUE;
        this.mLteRsrq = Integer.MAX_VALUE;
        this.mLteRssnr = Integer.MAX_VALUE;
        this.mLteCqi = Integer.MAX_VALUE;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = true;
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
    }

    public SignalStrength(boolean gsmFlag) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.VENDOR_EDIT = true;
        this.mGsmSignalStrength = 99;
        this.mGsmBitErrorRate = -1;
        this.mCdmaDbm = -1;
        this.mCdmaEcio = -1;
        this.mEvdoDbm = -1;
        this.mEvdoEcio = -1;
        this.mEvdoSnr = -1;
        this.mLteSignalStrength = 99;
        this.mLteRsrp = Integer.MAX_VALUE;
        this.mLteRsrq = Integer.MAX_VALUE;
        this.mLteRssnr = Integer.MAX_VALUE;
        this.mLteCqi = Integer.MAX_VALUE;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = gsmFlag;
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, boolean gsmFlag) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.VENDOR_EDIT = true;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, gsmFlag);
        this.mTdScdmaRscp = tdScdmaRscp;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, boolean gsmFlag) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.VENDOR_EDIT = true;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, gsmFlag);
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, boolean gsmFlag, int gsmRssiQdbm, int gsmRscpQdbm, int gsmEcn0Qdbm) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.VENDOR_EDIT = true;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, gsmFlag);
        this.mGsmRssiQdbm = gsmRssiQdbm;
        this.mGsmRscpQdbm = gsmRscpQdbm;
        this.mGsmEcn0Qdbm = gsmEcn0Qdbm;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, boolean gsmFlag) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.VENDOR_EDIT = true;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, gsmFlag);
    }

    public SignalStrength(SignalStrength s) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.VENDOR_EDIT = true;
        copyFrom(s);
    }

    public void initialize(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, boolean gsm) {
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, gsm);
    }

    public void initialize(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, boolean gsm) {
        this.mGsmSignalStrength = gsmSignalStrength;
        this.mGsmBitErrorRate = gsmBitErrorRate;
        this.mCdmaDbm = cdmaDbm;
        this.mCdmaEcio = cdmaEcio;
        this.mEvdoDbm = evdoDbm;
        this.mEvdoEcio = evdoEcio;
        this.mEvdoSnr = evdoSnr;
        this.mLteSignalStrength = lteSignalStrength;
        this.mLteRsrp = lteRsrp;
        this.mLteRsrq = lteRsrq;
        this.mLteRssnr = lteRssnr;
        this.mLteCqi = lteCqi;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = gsm;
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        if (DBG) {
            log("initialize: " + toString());
        }
    }

    private static IServiceStateExt getPlugInInstance() {
        log("BSP package should not use plug in");
        return mServiceStateExt;
    }

    protected void copyFrom(SignalStrength s) {
        this.mGsmSignalStrength = s.mGsmSignalStrength;
        this.mGsmBitErrorRate = s.mGsmBitErrorRate;
        this.mCdmaDbm = s.mCdmaDbm;
        this.mCdmaEcio = s.mCdmaEcio;
        this.mEvdoDbm = s.mEvdoDbm;
        this.mEvdoEcio = s.mEvdoEcio;
        this.mEvdoSnr = s.mEvdoSnr;
        this.mLteSignalStrength = s.mLteSignalStrength;
        this.mLteRsrp = s.mLteRsrp;
        this.mLteRsrq = s.mLteRsrq;
        this.mLteRssnr = s.mLteRssnr;
        this.mLteCqi = s.mLteCqi;
        this.mTdScdmaRscp = s.mTdScdmaRscp;
        this.isGsm = s.isGsm;
        this.mGsmRssiQdbm = s.mGsmRssiQdbm;
        this.mGsmRscpQdbm = s.mGsmRscpQdbm;
        this.mGsmEcn0Qdbm = s.mGsmEcn0Qdbm;
        this.mOEMLevel_0 = s.mOEMLevel_0;
        this.mOEMLevel_1 = s.mOEMLevel_1;
    }

    public SignalStrength(Parcel in) {
        boolean z = true;
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.VENDOR_EDIT = true;
        this.mGsmSignalStrength = in.readInt();
        this.mGsmBitErrorRate = in.readInt();
        this.mCdmaDbm = in.readInt();
        this.mCdmaEcio = in.readInt();
        this.mEvdoDbm = in.readInt();
        this.mEvdoEcio = in.readInt();
        this.mEvdoSnr = in.readInt();
        this.mLteSignalStrength = in.readInt();
        this.mLteRsrp = in.readInt();
        this.mLteRsrq = in.readInt();
        this.mLteRssnr = in.readInt();
        this.mLteCqi = in.readInt();
        this.mTdScdmaRscp = in.readInt();
        if (in.readInt() == 0) {
            z = false;
        }
        this.isGsm = z;
        this.mGsmRssiQdbm = in.readInt();
        this.mGsmRscpQdbm = in.readInt();
        this.mGsmEcn0Qdbm = in.readInt();
        this.mOEMLevel_0 = in.readInt();
        this.mOEMLevel_1 = in.readInt();
    }

    public static SignalStrength makeSignalStrengthFromRilParcel(Parcel in) {
        SignalStrength ss = new SignalStrength();
        ss.mGsmSignalStrength = in.readInt();
        ss.mGsmBitErrorRate = in.readInt();
        ss.mCdmaDbm = in.readInt();
        ss.mCdmaEcio = in.readInt();
        ss.mEvdoDbm = in.readInt();
        ss.mEvdoEcio = in.readInt();
        ss.mEvdoSnr = in.readInt();
        ss.mLteSignalStrength = in.readInt();
        ss.mLteRsrp = in.readInt();
        ss.mLteRsrq = in.readInt();
        ss.mLteRssnr = in.readInt();
        ss.mLteCqi = in.readInt();
        ss.mTdScdmaRscp = in.readInt();
        ss.mGsmRssiQdbm = in.readInt();
        ss.mGsmRscpQdbm = in.readInt();
        ss.mGsmEcn0Qdbm = in.readInt();
        ss.mOEMLevel_0 = in.readInt();
        ss.mOEMLevel_1 = in.readInt();
        if (SystemProperties.get("persist.sys.oppo.region", "CN").equals("RU")) {
            if (ss.mTdScdmaRscp >= 105 && ss.mTdScdmaRscp <= 108) {
                ss.mTdScdmaRscp = (ss.mTdScdmaRscp - 104) + ss.mTdScdmaRscp;
            } else if (ss.mTdScdmaRscp > 108 && ss.mTdScdmaRscp <= 116) {
                ss.mTdScdmaRscp += 4;
                log("makeSignalStrengthFromRilParcel mTdScdmaRscp:" + ss.mTdScdmaRscp);
            } else if (ss.mTdScdmaRscp > 116 && ss.mTdScdmaRscp <= 120) {
                ss.mTdScdmaRscp = 120;
            }
        }
        return ss;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mGsmSignalStrength);
        out.writeInt(this.mGsmBitErrorRate);
        out.writeInt(this.mCdmaDbm);
        out.writeInt(this.mCdmaEcio);
        out.writeInt(this.mEvdoDbm);
        out.writeInt(this.mEvdoEcio);
        out.writeInt(this.mEvdoSnr);
        out.writeInt(this.mLteSignalStrength);
        out.writeInt(this.mLteRsrp);
        out.writeInt(this.mLteRsrq);
        out.writeInt(this.mLteRssnr);
        out.writeInt(this.mLteCqi);
        out.writeInt(this.mTdScdmaRscp);
        out.writeInt(this.isGsm ? 1 : 0);
        out.writeInt(this.mGsmRssiQdbm);
        out.writeInt(this.mGsmRscpQdbm);
        out.writeInt(this.mGsmEcn0Qdbm);
        out.writeInt(this.mOEMLevel_0);
        out.writeInt(this.mOEMLevel_1);
    }

    public int describeContents() {
        return 0;
    }

    public void validateInput() {
        int i;
        int i2 = 99;
        int i3 = -1;
        int i4 = -120;
        int i5 = Integer.MAX_VALUE;
        if (DBG) {
            log("Signal before validate=" + this);
        }
        if (this.mGsmSignalStrength >= 0) {
            i = this.mGsmSignalStrength;
        } else {
            i = 99;
        }
        this.mGsmSignalStrength = i;
        if (this.mCdmaDbm > 0) {
            i = -this.mCdmaDbm;
        } else {
            i = -120;
        }
        this.mCdmaDbm = i;
        this.mCdmaEcio = this.mCdmaEcio > 0 ? -this.mCdmaEcio : -160;
        if (this.mEvdoDbm > 0) {
            i4 = -this.mEvdoDbm;
        }
        this.mEvdoDbm = i4;
        if (this.mEvdoEcio >= 0) {
            i = -this.mEvdoEcio;
        } else {
            i = -1;
        }
        this.mEvdoEcio = i;
        if (this.mEvdoSnr > 0 && this.mEvdoSnr <= 8) {
            i3 = this.mEvdoSnr;
        }
        this.mEvdoSnr = i3;
        if (this.mLteSignalStrength >= 0) {
            i2 = this.mLteSignalStrength;
        }
        this.mLteSignalStrength = i2;
        if (this.mLteRsrp < 44 || this.mLteRsrp > 140) {
            i = Integer.MAX_VALUE;
        } else {
            i = -this.mLteRsrp;
        }
        this.mLteRsrp = i;
        if (this.mLteRsrq < 3 || this.mLteRsrq > 20) {
            i = Integer.MAX_VALUE;
        } else {
            i = -this.mLteRsrq;
        }
        this.mLteRsrq = i;
        if (this.mLteRssnr < -200 || this.mLteRssnr > 300) {
            i = Integer.MAX_VALUE;
        } else {
            i = this.mLteRssnr;
        }
        this.mLteRssnr = i;
        if (this.mTdScdmaRscp >= 25 && this.mTdScdmaRscp <= 120) {
            i5 = -this.mTdScdmaRscp;
        }
        this.mTdScdmaRscp = i5;
        if (DBG) {
            log("Signal after validate=" + this);
        }
    }

    public void setGsm(boolean gsmFlag) {
        this.isGsm = gsmFlag;
    }

    public int getGsmSignalStrength() {
        if (getTdScdmaDbm() == Integer.MAX_VALUE || this.mGsmSignalStrength != 99) {
            return this.mGsmSignalStrength;
        }
        int tdsValue = (getTdScdmaDbm() + 113) / 2;
        if (tdsValue == -1) {
            tdsValue = 0;
        }
        return tdsValue;
    }

    public int getGsmBitErrorRate() {
        return this.mGsmBitErrorRate;
    }

    public int getCdmaDbm() {
        return this.mCdmaDbm;
    }

    public int getCdmaEcio() {
        return this.mCdmaEcio;
    }

    public int getEvdoDbm() {
        return this.mEvdoDbm;
    }

    public int getEvdoEcio() {
        return this.mEvdoEcio;
    }

    public int getEvdoSnr() {
        return this.mEvdoSnr;
    }

    public int getLteSignalStrength() {
        return this.mLteSignalStrength;
    }

    public int getLteRsrp() {
        return this.mLteRsrp;
    }

    public int getLteRsrq() {
        return this.mLteRsrq;
    }

    public int getLteRssnr() {
        return this.mLteRssnr;
    }

    public int getLteCqi() {
        return this.mLteCqi;
    }

    public int getLevel() {
        int level;
        if (this.isGsm) {
            level = getLteLevel();
            if (level == 0) {
                level = getTdScdmaLevel();
                if (level == 0) {
                    level = getGsmLevel();
                }
            }
        } else {
            int cdmaLevel = getCdmaLevel();
            int evdoLevel = getEvdoLevel();
            level = evdoLevel == 0 ? cdmaLevel : cdmaLevel == 0 ? evdoLevel : cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
        }
        if (DBG) {
            log("getLevel=" + level);
        }
        return level;
    }

    public int getAsuLevel() {
        int asuLevel;
        if (this.isGsm) {
            asuLevel = getLteLevel() == 0 ? getTdScdmaLevel() == 0 ? getGsmAsuLevel() : getTdScdmaAsuLevel() : getLteAsuLevel();
        } else {
            int cdmaAsuLevel = getCdmaAsuLevel();
            int evdoAsuLevel = getEvdoAsuLevel();
            asuLevel = evdoAsuLevel == 0 ? cdmaAsuLevel : cdmaAsuLevel == 0 ? evdoAsuLevel : cdmaAsuLevel < evdoAsuLevel ? cdmaAsuLevel : evdoAsuLevel;
        }
        if (DBG) {
            log("getAsuLevel=" + asuLevel);
        }
        return asuLevel;
    }

    public int getDbm() {
        if (isGsm()) {
            int dBm = getLteDbm();
            if (dBm == Integer.MAX_VALUE) {
                if (getTdScdmaLevel() == 0) {
                    dBm = getGsmDbm();
                } else {
                    dBm = getTdScdmaDbm();
                }
            }
            if (DBG) {
                log("getDbm=" + dBm);
            }
            return dBm;
        }
        int cdmaDbm = getCdmaDbm();
        int evdoDbm = getEvdoDbm();
        if (evdoDbm != -120) {
            if (cdmaDbm == -120) {
                cdmaDbm = evdoDbm;
            } else if (cdmaDbm >= evdoDbm) {
                cdmaDbm = evdoDbm;
            }
        }
        return cdmaDbm;
    }

    public int getGsmDbm() {
        int asu;
        int dBm;
        int gsmSignalStrength = getGsmSignalStrength();
        if (gsmSignalStrength == 99) {
            asu = -1;
        } else {
            asu = gsmSignalStrength;
        }
        if (asu != -1) {
            log("mapGsmSignalDbm() mGsmRscpQdbm=" + this.mGsmRscpQdbm + " asu=" + asu);
            if (this.mGsmRscpQdbm < 0) {
                dBm = this.mGsmRscpQdbm / 4;
            } else if (getTdScdmaDbm() == Integer.MAX_VALUE || this.mGsmSignalStrength != 99) {
                dBm = (asu * 2) - 113;
            } else {
                dBm = getTdScdmaDbm();
            }
        } else {
            dBm = -1;
        }
        if (DBG) {
            log("getGsmDbm=" + dBm);
        }
        return dBm;
    }

    public int getGsmLevel() {
        int asu = getGsmSignalStrength();
        int level;
        if (this.mGsmRscpQdbm < 0) {
            if (this.mGsmRscpQdbm > -25 || this.mGsmRscpQdbm == Integer.MAX_VALUE) {
                level = 0;
            } else if (this.mGsmRscpQdbm >= -91) {
                level = 4;
            } else if (this.mGsmRscpQdbm >= -97) {
                level = 3;
            } else if (this.mGsmRscpQdbm >= -99) {
                level = 2;
            } else if (this.mGsmRscpQdbm >= -120) {
                level = 1;
            } else {
                level = 0;
            }
            return level;
        }
        if (asu <= 2 || asu == 99) {
            level = 0;
        } else if (asu >= 13) {
            level = 4;
        } else if (asu >= 10) {
            level = 3;
        } else if (asu >= 7) {
            level = 2;
        } else {
            level = 1;
        }
        return level;
    }

    public int getGsmAsuLevel() {
        int level = getGsmSignalStrength();
        if (DBG) {
            log("getGsmAsuLevel=" + level);
        }
        return level;
    }

    public int getCdmaLevel() {
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        if (cdmaDbm >= -89) {
            return 4;
        }
        if (cdmaDbm >= -100) {
            return 3;
        }
        if (cdmaDbm >= -106) {
            return 2;
        }
        if (cdmaDbm >= -109) {
            return 1;
        }
        return 0;
    }

    public int getCdmaAsuLevel() {
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        if (cdmaDbm >= -75) {
            return 16;
        }
        if (cdmaDbm >= -82) {
            return 8;
        }
        if (cdmaDbm >= -90) {
            return 4;
        }
        if (cdmaDbm >= -95) {
            return 2;
        }
        if (cdmaDbm >= -100) {
            return 1;
        }
        return 99;
    }

    public int getEvdoLevel() {
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        if (evdoDbm >= -89) {
            return 4;
        }
        if (evdoDbm >= -100) {
            return 3;
        }
        if (evdoDbm >= -106) {
            return 2;
        }
        if (evdoDbm >= -110) {
            return 1;
        }
        return 0;
    }

    public int getEvdoAsuLevel() {
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        if (evdoDbm >= -65) {
            return 16;
        }
        if (evdoDbm >= -75) {
            return 8;
        }
        if (evdoDbm >= -85) {
            return 4;
        }
        if (evdoDbm >= -95) {
            return 2;
        }
        if (evdoDbm >= -105) {
            return 1;
        }
        return 99;
    }

    public int getLteDbm() {
        return this.mLteRsrp;
    }

    public int getLteLevel() {
        int rsrpIconLevel;
        int rssiIconLevel = 0;
        int snrIconLevel = -1;
        int rsrpThreshType = Resources.getSystem().getInteger(R.integer.config_LTE_RSRP_threshold_type);
        int threshLevel3 = OEM_LEVLE == 0 ? -105 : -110;
        int threshLevel2 = OEM_LEVLE == 0 ? -113 : -115;
        int[] threshRsrp;
        if (rsrpThreshType == 0) {
            threshRsrp = RSRP_THRESH_STRICT;
        } else {
            threshRsrp = RSRP_THRESH_LENIENT;
        }
        if (this.mLteRsrp > -44) {
            rsrpIconLevel = -1;
        } else if (this.mLteRsrp >= -97) {
            rsrpIconLevel = 4;
        } else if (this.mLteRsrp >= threshLevel3) {
            rsrpIconLevel = 3;
        } else if (this.mLteRsrp >= threshLevel2) {
            rsrpIconLevel = 2;
        } else if (this.mLteRsrp >= -120) {
            rsrpIconLevel = 1;
        } else {
            rsrpIconLevel = 0;
        }
        if (rsrpIconLevel != -1) {
            return rsrpIconLevel;
        }
        if (this.mLteRssnr > 300) {
            snrIconLevel = -1;
        } else if (this.mLteRssnr >= 130) {
            snrIconLevel = 4;
        } else if (this.mLteRssnr >= 45) {
            snrIconLevel = 3;
        } else if (this.mLteRssnr >= 10) {
            snrIconLevel = 2;
        } else if (this.mLteRssnr >= -30) {
            snrIconLevel = 1;
        } else if (this.mLteRssnr >= -200) {
            snrIconLevel = 0;
        }
        if (DBG) {
            log("getLTELevel - rsrp:" + this.mLteRsrp + " snr:" + this.mLteRssnr + " rsrpIconLevel:" + rsrpIconLevel + " snrIconLevel:" + snrIconLevel);
        }
        if (snrIconLevel != -1 && rsrpIconLevel != -1) {
            if (rsrpIconLevel >= snrIconLevel) {
                rsrpIconLevel = snrIconLevel;
            }
            return rsrpIconLevel;
        } else if (snrIconLevel != -1) {
            return snrIconLevel;
        } else {
            if (rsrpIconLevel != -1) {
                return rsrpIconLevel;
            }
            if (this.mLteSignalStrength > 63) {
                rssiIconLevel = 0;
            } else if (this.mLteSignalStrength >= 12) {
                rssiIconLevel = 4;
            } else if (this.mLteSignalStrength >= 8) {
                rssiIconLevel = 3;
            } else if (this.mLteSignalStrength >= 5) {
                rssiIconLevel = 2;
            } else if (this.mLteSignalStrength >= 0) {
                rssiIconLevel = 1;
            }
            if (DBG) {
                log("getLTELevel - rssi:" + this.mLteSignalStrength + " rssiIconLevel:" + rssiIconLevel);
            }
            return rssiIconLevel;
        }
    }

    public int getLteAsuLevel() {
        int lteAsuLevel;
        int lteDbm = getLteDbm();
        if (lteDbm == Integer.MAX_VALUE) {
            lteAsuLevel = 255;
        } else {
            lteAsuLevel = lteDbm + 140;
        }
        if (DBG) {
            log("Lte Asu level: " + lteAsuLevel);
        }
        return lteAsuLevel;
    }

    public boolean isGsm() {
        return this.isGsm;
    }

    public int getTdScdmaDbm() {
        return this.mTdScdmaRscp;
    }

    public int getTdScdmaLevel() {
        int tdScdmaDbm = getTdScdmaDbm();
        IServiceStateExt ssExt = getPlugInInstance();
        if (ssExt != null) {
            return ssExt.mapUmtsSignalLevel(tdScdmaDbm);
        }
        int level;
        log("[getTdScdmaLevel] null plug-in instance");
        if (tdScdmaDbm > -25 || tdScdmaDbm == Integer.MAX_VALUE) {
            level = 0;
        } else if (tdScdmaDbm >= -91) {
            level = 4;
        } else if (tdScdmaDbm >= -97) {
            level = 3;
        } else if (tdScdmaDbm >= -99) {
            level = 2;
        } else if (tdScdmaDbm >= -120) {
            level = 1;
        } else {
            level = 0;
        }
        if (DBG) {
            log("getTdScdmaLevel = " + level);
        }
        return level;
    }

    public int getTdScdmaAsuLevel() {
        int tdScdmaAsuLevel;
        int tdScdmaDbm = getTdScdmaDbm();
        if (tdScdmaDbm == Integer.MAX_VALUE) {
            tdScdmaAsuLevel = 255;
        } else {
            tdScdmaAsuLevel = tdScdmaDbm + 120;
        }
        if (DBG) {
            log("TD-SCDMA Asu level: " + tdScdmaAsuLevel);
        }
        return tdScdmaAsuLevel;
    }

    public int hashCode() {
        int i;
        int i2 = (this.mTdScdmaRscp * 31) + ((((((((((((this.mGsmSignalStrength * 31) + (this.mGsmBitErrorRate * 31)) + (this.mCdmaDbm * 31)) + (this.mCdmaEcio * 31)) + (this.mEvdoDbm * 31)) + (this.mEvdoEcio * 31)) + (this.mEvdoSnr * 31)) + (this.mLteSignalStrength * 31)) + (this.mLteRsrp * 31)) + (this.mLteRsrq * 31)) + (this.mLteRssnr * 31)) + (this.mLteCqi * 31));
        if (this.isGsm) {
            i = 1;
        } else {
            i = 0;
        }
        return i + i2;
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            SignalStrength s = (SignalStrength) o;
            if (o == null) {
                return false;
            }
            if (this.mGsmSignalStrength == s.mGsmSignalStrength && this.mGsmBitErrorRate == s.mGsmBitErrorRate && this.mCdmaDbm == s.mCdmaDbm && this.mCdmaEcio == s.mCdmaEcio && this.mEvdoDbm == s.mEvdoDbm && this.mEvdoEcio == s.mEvdoEcio && this.mEvdoSnr == s.mEvdoSnr && this.mLteSignalStrength == s.mLteSignalStrength && this.mLteRsrp == s.mLteRsrp && this.mLteRsrq == s.mLteRsrq && this.mLteRssnr == s.mLteRssnr && this.mLteCqi == s.mLteCqi && this.mTdScdmaRscp == s.mTdScdmaRscp && this.isGsm == s.isGsm && this.mGsmRscpQdbm == s.mGsmRscpQdbm) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("SignalStrength: ").append(this.mGsmSignalStrength).append(" ").append(this.mGsmBitErrorRate).append(" ").append(this.mCdmaDbm).append(" ").append(this.mCdmaEcio).append(" ").append(this.mEvdoDbm).append(" ").append(this.mEvdoEcio).append(" ").append(this.mEvdoSnr).append(" ").append(this.mLteSignalStrength).append(" ").append(this.mLteRsrp).append(" ").append(this.mLteRsrq).append(" ").append(this.mLteRssnr).append(" ").append(this.mLteCqi).append(" ").append(this.mTdScdmaRscp).append(" ").append(this.mOEMLevel_0).append(" ").append(this.mOEMLevel_1).append(" ");
        if (this.isGsm) {
            str = "gsm|lte";
        } else {
            str = "cdma";
        }
        return append.append(str).append(" ").append(this.mGsmRssiQdbm).append(" ").append(this.mGsmRscpQdbm).append(" ").append(this.mGsmEcn0Qdbm).toString();
    }

    private void setFromNotifierBundle(Bundle m) {
        this.mGsmSignalStrength = m.getInt("GsmSignalStrength");
        this.mGsmBitErrorRate = m.getInt("GsmBitErrorRate");
        this.mCdmaDbm = m.getInt("CdmaDbm");
        this.mCdmaEcio = m.getInt("CdmaEcio");
        this.mEvdoDbm = m.getInt("EvdoDbm");
        this.mEvdoEcio = m.getInt("EvdoEcio");
        this.mEvdoSnr = m.getInt("EvdoSnr");
        this.mLteSignalStrength = m.getInt("LteSignalStrength");
        this.mLteRsrp = m.getInt("LteRsrp");
        this.mLteRsrq = m.getInt("LteRsrq");
        this.mLteRssnr = m.getInt("LteRssnr");
        this.mLteCqi = m.getInt("LteCqi");
        this.mTdScdmaRscp = m.getInt("TdScdma");
        this.isGsm = m.getBoolean("isGsm");
        this.mGsmRssiQdbm = m.getInt("RssiQdbm");
        this.mGsmRscpQdbm = m.getInt("RscpQdbm");
        this.mGsmEcn0Qdbm = m.getInt("Ecn0Qdbm");
        this.mOEMLevel_0 = m.getInt("OEMLevel_0");
        this.mOEMLevel_1 = m.getInt("OEMLevel_1");
    }

    public void fillInNotifierBundle(Bundle m) {
        m.putInt("GsmSignalStrength", this.mGsmSignalStrength);
        m.putInt("GsmBitErrorRate", this.mGsmBitErrorRate);
        m.putInt("CdmaDbm", this.mCdmaDbm);
        m.putInt("CdmaEcio", this.mCdmaEcio);
        m.putInt("EvdoDbm", this.mEvdoDbm);
        m.putInt("EvdoEcio", this.mEvdoEcio);
        m.putInt("EvdoSnr", this.mEvdoSnr);
        m.putInt("LteSignalStrength", this.mLteSignalStrength);
        m.putInt("LteRsrp", this.mLteRsrp);
        m.putInt("LteRsrq", this.mLteRsrq);
        m.putInt("LteRssnr", this.mLteRssnr);
        m.putInt("LteCqi", this.mLteCqi);
        m.putInt("TdScdma", this.mTdScdmaRscp);
        m.putBoolean("isGsm", Boolean.valueOf(this.isGsm).booleanValue());
        m.putInt("RssiQdbm", this.mGsmRssiQdbm);
        m.putInt("RscpQdbm", this.mGsmRscpQdbm);
        m.putInt("Ecn0Qdbm", this.mGsmEcn0Qdbm);
        m.putInt("OEMLevel_0", this.mOEMLevel_0);
        m.putInt("OEMLevel_1", this.mOEMLevel_1);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }

    public int getGsmRssiQdbm() {
        return this.mGsmRssiQdbm;
    }

    public int getGsmRscpQdbm() {
        return this.mGsmRscpQdbm;
    }

    public int getGsmEcn0Qdbm() {
        return this.mGsmEcn0Qdbm;
    }

    public int getGsmSignalStrengthDbm() {
        int asu;
        int gsmSignalStrength = this.mGsmSignalStrength;
        if (gsmSignalStrength == 99) {
            asu = -1;
        } else {
            asu = gsmSignalStrength;
        }
        if (asu != -1) {
            return (asu * 2) - 113;
        }
        return -1;
    }

    private static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    public int[] getColorOSLevel() {
        int[] iArr = new int[2];
        iArr[0] = this.mOEMLevel_0;
        iArr[1] = this.mOEMLevel_1;
        return iArr;
    }

    public int getOEMLevel(int networktype) {
        int level = OEM_LEVLE;
        if (networktype == 1) {
            level = getGsmLevel(false);
            if (level <= 0) {
                level = OEM_LEVLE;
            }
        } else if (networktype == 2) {
            level = getGsmLevel();
            if (OEM_LEVLE == 1 && level <= 1) {
                level = 2;
            }
        } else if (networktype == 3) {
            level = getLteLevel();
            if (level <= 0) {
                level = OEM_LEVLE;
            }
        } else if (networktype == 4) {
            level = getGsmLevel(true);
            if (OEM_LEVLE == 1 && level <= 1) {
                level = 2;
            }
        } else if (networktype == 5) {
            level = getCdmaLevel();
            if (level <= 0) {
                level = OEM_LEVLE;
            }
        } else if (networktype == 6) {
            level = getEvdoLevel();
            if (level <= 0) {
                level = OEM_LEVLE;
            }
        } else if (networktype == 0) {
            level = 0;
        }
        log("leon getOEMLevel networktype=" + networktype + ",level=" + level);
        return level;
    }

    public int getGsmLevel(boolean wcdma) {
        int level;
        int asu = getGsmSignalStrength();
        if (asu < 0 || asu == 99) {
            level = 0;
        } else if (wcdma ? asu >= 11 : asu >= 13) {
            level = 4;
        } else if (wcdma ? asu >= 8 : asu >= 8) {
            level = 3;
        } else if (wcdma ? asu >= 7 : asu >= 5) {
            level = 2;
        } else {
            level = 1;
        }
        if (DBG) {
            log("getGsmLevel=" + level);
        }
        return level;
    }
}
