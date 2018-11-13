package android_maps_conflict_avoidance.com.google.android.gsf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

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
public class LoginData implements Parcelable {
    public static final Creator<LoginData> CREATOR = null;
    public String mAuthtoken;
    public String mCaptchaAnswer;
    public byte[] mCaptchaData;
    public String mCaptchaMimeType;
    public String mCaptchaToken;
    public String mEncryptedPassword;
    public int mFlags;
    public String mJsonString;
    public String mOAuthAccessToken;
    public String mPassword;
    public String mService;
    public String mSid;
    public Status mStatus;
    public String mUsername;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum Status {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.Status.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.Status.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.Status.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.android.gsf.LoginData.<clinit>():void");
    }

    /* synthetic */ LoginData(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    public LoginData() {
        this.mUsername = null;
        this.mEncryptedPassword = null;
        this.mPassword = null;
        this.mService = null;
        this.mCaptchaToken = null;
        this.mCaptchaData = null;
        this.mCaptchaMimeType = null;
        this.mCaptchaAnswer = null;
        this.mFlags = 0;
        this.mStatus = null;
        this.mJsonString = null;
        this.mSid = null;
        this.mAuthtoken = null;
        this.mOAuthAccessToken = null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mUsername);
        out.writeString(this.mEncryptedPassword);
        out.writeString(this.mPassword);
        out.writeString(this.mService);
        out.writeString(this.mCaptchaToken);
        if (this.mCaptchaData == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(this.mCaptchaData.length);
            out.writeByteArray(this.mCaptchaData);
        }
        out.writeString(this.mCaptchaMimeType);
        out.writeString(this.mCaptchaAnswer);
        out.writeInt(this.mFlags);
        if (this.mStatus == null) {
            out.writeString(null);
        } else {
            out.writeString(this.mStatus.name());
        }
        out.writeString(this.mJsonString);
        out.writeString(this.mSid);
        out.writeString(this.mAuthtoken);
        out.writeString(this.mOAuthAccessToken);
    }

    private LoginData(Parcel in) {
        this.mUsername = null;
        this.mEncryptedPassword = null;
        this.mPassword = null;
        this.mService = null;
        this.mCaptchaToken = null;
        this.mCaptchaData = null;
        this.mCaptchaMimeType = null;
        this.mCaptchaAnswer = null;
        this.mFlags = 0;
        this.mStatus = null;
        this.mJsonString = null;
        this.mSid = null;
        this.mAuthtoken = null;
        this.mOAuthAccessToken = null;
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.mUsername = in.readString();
        this.mEncryptedPassword = in.readString();
        this.mPassword = in.readString();
        this.mService = in.readString();
        this.mCaptchaToken = in.readString();
        int len = in.readInt();
        if (len == -1) {
            this.mCaptchaData = null;
        } else {
            this.mCaptchaData = new byte[len];
            in.readByteArray(this.mCaptchaData);
        }
        this.mCaptchaMimeType = in.readString();
        this.mCaptchaAnswer = in.readString();
        this.mFlags = in.readInt();
        String status = in.readString();
        if (status == null) {
            this.mStatus = null;
        } else {
            this.mStatus = Status.valueOf(status);
        }
        this.mJsonString = in.readString();
        this.mSid = in.readString();
        this.mAuthtoken = in.readString();
        this.mOAuthAccessToken = in.readString();
    }
}
