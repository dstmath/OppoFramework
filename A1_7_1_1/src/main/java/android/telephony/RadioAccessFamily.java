package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class RadioAccessFamily implements Parcelable {
    private static final int CDMA = 112;
    public static final Creator<RadioAccessFamily> CREATOR = null;
    private static final int EVDO = 12672;
    private static final int GSM = 65542;
    private static final int HS = 36352;
    private static final int LTE = 540672;
    public static final int RAF_1xRTT = 64;
    public static final int RAF_EDGE = 4;
    public static final int RAF_EHRPD = 8192;
    public static final int RAF_EVDO_0 = 128;
    public static final int RAF_EVDO_A = 256;
    public static final int RAF_EVDO_B = 4096;
    public static final int RAF_GPRS = 2;
    public static final int RAF_GSM = 65536;
    public static final int RAF_HSDPA = 512;
    public static final int RAF_HSPA = 2048;
    public static final int RAF_HSPAP = 32768;
    public static final int RAF_HSUPA = 1024;
    public static final int RAF_IS95A = 16;
    public static final int RAF_IS95B = 32;
    public static final int RAF_LTE = 16384;
    public static final int RAF_LTE_CA = 524288;
    public static final int RAF_TD_SCDMA = 131072;
    public static final int RAF_UMTS = 8;
    public static final int RAF_UNKNOWN = 1;
    private static final int WCDMA = 167432;
    private int mPhoneId;
    private int mRadioAccessFamily;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.RadioAccessFamily.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.RadioAccessFamily.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.RadioAccessFamily.<clinit>():void");
    }

    public RadioAccessFamily(int phoneId, int radioAccessFamily) {
        this.mPhoneId = phoneId;
        this.mRadioAccessFamily = radioAccessFamily;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public int getRadioAccessFamily() {
        return this.mRadioAccessFamily;
    }

    public String toString() {
        return "{ mPhoneId = " + this.mPhoneId + ", mRadioAccessFamily = " + this.mRadioAccessFamily + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeInt(this.mPhoneId);
        outParcel.writeInt(this.mRadioAccessFamily);
    }

    public static int getRafFromNetworkType(int type) {
        switch (type) {
            case 0:
                return 232974;
            case 1:
                return GSM;
            case 2:
                return WCDMA;
            case 3:
                return 232974;
            case 4:
                return 12784;
            case 5:
                return 112;
            case 6:
                return EVDO;
            case 7:
                return 245758;
            case 8:
                return 553456;
            case 9:
                return 773646;
            case 10:
                return 786430;
            case 11:
                return 540672;
            case 12:
                return 708104;
            case 13:
                return 131072;
            case 14:
                return WCDMA;
            case 15:
                return 671744;
            case 16:
                return 196614;
            case 17:
                return 737286;
            case 18:
                return 232974;
            case 19:
                return 708104;
            case 20:
                return 773646;
            case 21:
                return 245758;
            case 22:
                return 786430;
            case 30:
                return 81926;
            case 32:
                return 65654;
            case 33:
                return 78326;
            case 34:
                return 94710;
            default:
                return 1;
        }
    }

    private static int getAdjustedRaf(int raf) {
        if ((GSM & raf) > 0) {
            raf |= GSM;
        }
        if ((WCDMA & raf) > 0) {
            raf |= WCDMA;
        }
        if ((raf & 112) > 0) {
            raf |= 112;
        }
        if ((raf & EVDO) > 0) {
            raf |= EVDO;
        }
        if ((540672 & raf) > 0) {
            return raf | 540672;
        }
        return raf;
    }

    public static int getHighestRafCapability(int raf) {
        if ((540672 & raf) > 0) {
            return 3;
        }
        if ((180104 & raf) > 0) {
            return 2;
        }
        if ((65654 & raf) > 0) {
            return 1;
        }
        return 0;
    }

    public static int getNetworkTypeFromRaf(int raf) {
        switch (getAdjustedRaf(raf)) {
            case 112:
                return 5;
            case EVDO /*12672*/:
                return 6;
            case 12784:
                return 4;
            case GSM /*65542*/:
                return 1;
            case 65654:
                return 32;
            case 78326:
                return 33;
            case 131072:
                return 13;
            case Protocol.BASE_WIFI_MONITOR /*147456*/:
            case 671744:
                return 15;
            case WCDMA /*167432*/:
                return 2;
            case 196614:
                return 16;
            case 232974:
                return 0;
            case 245758:
                return 7;
            case 540672:
                return 11;
            case 553456:
                return 8;
            case 606214:
                return 30;
            case 618998:
                return 34;
            case 708104:
                return 12;
            case 737286:
                return 17;
            case 773646:
                return 9;
            case 786430:
                return 10;
            default:
                return RILConstants.PREFERRED_NETWORK_MODE;
        }
    }

    public static int singleRafTypeFromString(String rafString) {
        if (rafString.equals("GPRS")) {
            return 2;
        }
        if (rafString.equals("EDGE")) {
            return 4;
        }
        if (rafString.equals("UMTS")) {
            return 8;
        }
        if (rafString.equals("IS95A")) {
            return 16;
        }
        if (rafString.equals("IS95B")) {
            return 32;
        }
        if (rafString.equals("1XRTT")) {
            return 64;
        }
        if (rafString.equals("EVDO_0")) {
            return 128;
        }
        if (rafString.equals("EVDO_A")) {
            return 256;
        }
        if (rafString.equals("HSDPA")) {
            return 512;
        }
        if (rafString.equals("HSUPA")) {
            return 1024;
        }
        if (rafString.equals("HSPA")) {
            return 2048;
        }
        if (rafString.equals("EVDO_B")) {
            return 4096;
        }
        if (rafString.equals("EHRPD")) {
            return 8192;
        }
        if (rafString.equals("LTE")) {
            return 16384;
        }
        if (rafString.equals("HSPAP")) {
            return 32768;
        }
        if (rafString.equals("GSM")) {
            return 65536;
        }
        if (rafString.equals("TD_SCDMA")) {
            return 131072;
        }
        if (rafString.equals("HS")) {
            return HS;
        }
        if (rafString.equals("CDMA")) {
            return 112;
        }
        if (rafString.equals("EVDO")) {
            return EVDO;
        }
        if (rafString.equals("WCDMA")) {
            return WCDMA;
        }
        if (rafString.equals("LTE_CA")) {
            return 524288;
        }
        return 1;
    }

    public static int rafTypeFromString(String rafList) {
        int result = 0;
        for (String raf : rafList.toUpperCase().split("\\|")) {
            int rafType = singleRafTypeFromString(raf.trim());
            if (rafType == 1) {
                return rafType;
            }
            result |= rafType;
        }
        return result;
    }
}
