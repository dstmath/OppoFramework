package android.telecom;

import android.net.Uri;
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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class Voicemail implements Parcelable {
    public static final Creator<Voicemail> CREATOR = null;
    private final Long mDuration;
    private final Boolean mHasContent;
    private final Long mId;
    private final Boolean mIsRead;
    private final String mNumber;
    private final PhoneAccountHandle mPhoneAccount;
    private final String mProviderData;
    private final String mSource;
    private final Long mTimestamp;
    private final String mTranscription;
    private final Uri mUri;

    /* renamed from: android.telecom.Voicemail$1 */
    static class AnonymousClass1 implements Creator<Voicemail> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telecom.Voicemail.1.<init>():void, dex: 
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
        AnonymousClass1() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telecom.Voicemail.1.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.Voicemail.1.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telecom.Voicemail.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object createFromParcel(android.os.Parcel r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telecom.Voicemail.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.Voicemail.1.createFromParcel(android.os.Parcel):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.telecom.Voicemail.1.newArray(int):java.lang.Object[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object[] newArray(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.telecom.Voicemail.1.newArray(int):java.lang.Object[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.Voicemail.1.newArray(int):java.lang.Object[]");
        }

        public Voicemail createFromParcel(Parcel in) {
            return new Voicemail(in, null);
        }

        public Voicemail[] newArray(int size) {
            return new Voicemail[size];
        }
    }

    public static class Builder {
        private Long mBuilderDuration;
        private boolean mBuilderHasContent;
        private Long mBuilderId;
        private Boolean mBuilderIsRead;
        private String mBuilderNumber;
        private PhoneAccountHandle mBuilderPhoneAccount;
        private String mBuilderSourceData;
        private String mBuilderSourcePackage;
        private Long mBuilderTimestamp;
        private String mBuilderTranscription;
        private Uri mBuilderUri;

        /* synthetic */ Builder(Builder builder) {
            this();
        }

        private Builder() {
        }

        public Builder setNumber(String number) {
            this.mBuilderNumber = number;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            this.mBuilderTimestamp = Long.valueOf(timestamp);
            return this;
        }

        public Builder setPhoneAccount(PhoneAccountHandle phoneAccount) {
            this.mBuilderPhoneAccount = phoneAccount;
            return this;
        }

        public Builder setId(long id) {
            this.mBuilderId = Long.valueOf(id);
            return this;
        }

        public Builder setDuration(long duration) {
            this.mBuilderDuration = Long.valueOf(duration);
            return this;
        }

        public Builder setSourcePackage(String sourcePackage) {
            this.mBuilderSourcePackage = sourcePackage;
            return this;
        }

        public Builder setSourceData(String sourceData) {
            this.mBuilderSourceData = sourceData;
            return this;
        }

        public Builder setUri(Uri uri) {
            this.mBuilderUri = uri;
            return this;
        }

        public Builder setIsRead(boolean isRead) {
            this.mBuilderIsRead = Boolean.valueOf(isRead);
            return this;
        }

        public Builder setHasContent(boolean hasContent) {
            this.mBuilderHasContent = hasContent;
            return this;
        }

        public Builder setTranscription(String transcription) {
            this.mBuilderTranscription = transcription;
            return this;
        }

        public Voicemail build() {
            long j = 0;
            this.mBuilderId = Long.valueOf(this.mBuilderId == null ? -1 : this.mBuilderId.longValue());
            this.mBuilderTimestamp = Long.valueOf(this.mBuilderTimestamp == null ? 0 : this.mBuilderTimestamp.longValue());
            if (this.mBuilderDuration != null) {
                j = this.mBuilderDuration.longValue();
            }
            this.mBuilderDuration = Long.valueOf(j);
            this.mBuilderIsRead = Boolean.valueOf(this.mBuilderIsRead == null ? false : this.mBuilderIsRead.booleanValue());
            return new Voicemail(this.mBuilderTimestamp, this.mBuilderNumber, this.mBuilderPhoneAccount, this.mBuilderId, this.mBuilderDuration, this.mBuilderSourcePackage, this.mBuilderSourceData, this.mBuilderUri, this.mBuilderIsRead, Boolean.valueOf(this.mBuilderHasContent), this.mBuilderTranscription, null);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telecom.Voicemail.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telecom.Voicemail.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.Voicemail.<clinit>():void");
    }

    /* synthetic */ Voicemail(Parcel in, Voicemail voicemail) {
        this(in);
    }

    /* synthetic */ Voicemail(Long timestamp, String number, PhoneAccountHandle phoneAccountHandle, Long id, Long duration, String source, String providerData, Uri uri, Boolean isRead, Boolean hasContent, String transcription, Voicemail voicemail) {
        this(timestamp, number, phoneAccountHandle, id, duration, source, providerData, uri, isRead, hasContent, transcription);
    }

    private Voicemail(Long timestamp, String number, PhoneAccountHandle phoneAccountHandle, Long id, Long duration, String source, String providerData, Uri uri, Boolean isRead, Boolean hasContent, String transcription) {
        this.mTimestamp = timestamp;
        this.mNumber = number;
        this.mPhoneAccount = phoneAccountHandle;
        this.mId = id;
        this.mDuration = duration;
        this.mSource = source;
        this.mProviderData = providerData;
        this.mUri = uri;
        this.mIsRead = isRead;
        this.mHasContent = hasContent;
        this.mTranscription = transcription;
    }

    public static Builder createForInsertion(long timestamp, String number) {
        return new Builder().setNumber(number).setTimestamp(timestamp);
    }

    public static Builder createForUpdate(long id, String sourceData) {
        return new Builder().setId(id).setSourceData(sourceData);
    }

    public long getId() {
        return this.mId.longValue();
    }

    public String getNumber() {
        return this.mNumber;
    }

    public PhoneAccountHandle getPhoneAccount() {
        return this.mPhoneAccount;
    }

    public long getTimestampMillis() {
        return this.mTimestamp.longValue();
    }

    public long getDuration() {
        return this.mDuration.longValue();
    }

    public String getSourcePackage() {
        return this.mSource;
    }

    public String getSourceData() {
        return this.mProviderData;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public boolean isRead() {
        return this.mIsRead.booleanValue();
    }

    public boolean hasContent() {
        return this.mHasContent.booleanValue();
    }

    public String getTranscription() {
        return this.mTranscription;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mTimestamp.longValue());
        dest.writeCharSequence(this.mNumber);
        if (this.mPhoneAccount == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.mPhoneAccount.writeToParcel(dest, flags);
        }
        dest.writeLong(this.mId.longValue());
        dest.writeLong(this.mDuration.longValue());
        dest.writeCharSequence(this.mSource);
        dest.writeCharSequence(this.mProviderData);
        if (this.mUri == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.mUri.writeToParcel(dest, flags);
        }
        if (this.mIsRead.booleanValue()) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        if (this.mHasContent.booleanValue()) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeCharSequence(this.mTranscription);
    }

    private Voicemail(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mTimestamp = Long.valueOf(in.readLong());
        this.mNumber = (String) in.readCharSequence();
        if (in.readInt() > 0) {
            this.mPhoneAccount = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(in);
        } else {
            this.mPhoneAccount = null;
        }
        this.mId = Long.valueOf(in.readLong());
        this.mDuration = Long.valueOf(in.readLong());
        this.mSource = (String) in.readCharSequence();
        this.mProviderData = (String) in.readCharSequence();
        if (in.readInt() > 0) {
            this.mUri = (Uri) Uri.CREATOR.createFromParcel(in);
        } else {
            this.mUri = null;
        }
        if (in.readInt() > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsRead = Boolean.valueOf(z);
        if (in.readInt() <= 0) {
            z2 = false;
        }
        this.mHasContent = Boolean.valueOf(z2);
        this.mTranscription = (String) in.readCharSequence();
    }
}
