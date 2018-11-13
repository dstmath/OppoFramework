package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import java.util.Objects;

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
public final class DisconnectCause implements Parcelable {
    public static final int ANSWERED_ELSEWHERE = 11;
    public static final int BUSY = 7;
    public static final int CALL_PULLED = 12;
    public static final int CANCELED = 4;
    public static final int CONNECTION_MANAGER_NOT_SUPPORTED = 10;
    public static final Creator<DisconnectCause> CREATOR = null;
    public static final int ERROR = 1;
    public static final int LOCAL = 2;
    public static final int MISSED = 5;
    public static final int OTHER = 9;
    public static final int REJECTED = 6;
    public static final int REMOTE = 3;
    public static final int RESTRICTED = 8;
    public static final int SIP_INVITE_ERROR = 14;
    public static final int UNKNOWN = 0;
    public static final int WFC_CALL_ERROR = 13;
    private int mDisconnectCode;
    private CharSequence mDisconnectDescription;
    private CharSequence mDisconnectLabel;
    private String mDisconnectReason;
    private int mToneToPlay;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telecom.DisconnectCause.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telecom.DisconnectCause.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.DisconnectCause.<clinit>():void");
    }

    public DisconnectCause(int code) {
        this(code, null, null, null, -1);
    }

    public DisconnectCause(int code, String reason) {
        this(code, null, null, reason, -1);
    }

    public DisconnectCause(int code, CharSequence label, CharSequence description, String reason) {
        this(code, label, description, reason, -1);
    }

    public DisconnectCause(int code, CharSequence label, CharSequence description, String reason, int toneToPlay) {
        this.mDisconnectCode = code;
        this.mDisconnectLabel = label;
        this.mDisconnectDescription = description;
        this.mDisconnectReason = reason;
        this.mToneToPlay = toneToPlay;
    }

    public int getCode() {
        return this.mDisconnectCode;
    }

    public CharSequence getLabel() {
        return this.mDisconnectLabel;
    }

    public CharSequence getDescription() {
        return this.mDisconnectDescription;
    }

    public String getReason() {
        return this.mDisconnectReason;
    }

    public int getTone() {
        return this.mToneToPlay;
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeInt(this.mDisconnectCode);
        TextUtils.writeToParcel(this.mDisconnectLabel, destination, flags);
        TextUtils.writeToParcel(this.mDisconnectDescription, destination, flags);
        destination.writeString(this.mDisconnectReason);
        destination.writeInt(this.mToneToPlay);
    }

    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return (((Objects.hashCode(Integer.valueOf(this.mDisconnectCode)) + Objects.hashCode(this.mDisconnectLabel)) + Objects.hashCode(this.mDisconnectDescription)) + Objects.hashCode(this.mDisconnectReason)) + Objects.hashCode(Integer.valueOf(this.mToneToPlay));
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof DisconnectCause)) {
            return false;
        }
        DisconnectCause d = (DisconnectCause) o;
        if (Objects.equals(Integer.valueOf(this.mDisconnectCode), Integer.valueOf(d.getCode())) && Objects.equals(this.mDisconnectLabel, d.getLabel()) && Objects.equals(this.mDisconnectDescription, d.getDescription()) && Objects.equals(this.mDisconnectReason, d.getReason())) {
            z = Objects.equals(Integer.valueOf(this.mToneToPlay), Integer.valueOf(d.getTone()));
        }
        return z;
    }

    public String toString() {
        String code = PhoneConstants.MVNO_TYPE_NONE;
        switch (this.mDisconnectCode) {
            case 0:
                code = IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
                break;
            case 1:
                code = "ERROR";
                break;
            case 2:
                code = "LOCAL";
                break;
            case 3:
                code = "REMOTE";
                break;
            case 4:
                code = "CANCELED";
                break;
            case 5:
                code = "MISSED";
                break;
            case 6:
                code = "REJECTED";
                break;
            case 7:
                code = "BUSY";
                break;
            case 8:
                code = "RESTRICTED";
                break;
            case 9:
                code = "OTHER";
                break;
            case 10:
                code = "CONNECTION_MANAGER_NOT_SUPPORTED";
                break;
            case 11:
                code = "ANSWERED_ELSEWHERE";
                break;
            case 12:
                code = "CALL_PULLED";
                break;
            case 13:
                code = "WFC_CALL_ERROR";
                break;
            default:
                code = "invalid code: " + this.mDisconnectCode;
                break;
        }
        String label = this.mDisconnectLabel == null ? PhoneConstants.MVNO_TYPE_NONE : this.mDisconnectLabel.toString();
        return "DisconnectCause [ Code: (" + code + ")" + " Label: (" + label + ")" + " Description: (" + (this.mDisconnectDescription == null ? PhoneConstants.MVNO_TYPE_NONE : this.mDisconnectDescription.toString()) + ")" + " Reason: (" + (this.mDisconnectReason == null ? PhoneConstants.MVNO_TYPE_NONE : this.mDisconnectReason) + ")" + " Tone: (" + this.mToneToPlay + ") ]";
    }
}
