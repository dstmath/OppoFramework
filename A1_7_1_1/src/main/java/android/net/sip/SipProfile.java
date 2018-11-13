package android.net.sip;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SipProfile implements Parcelable, Serializable, Cloneable {
    public static final Creator<SipProfile> CREATOR = null;
    private static final int DEFAULT_PORT = 5060;
    private static final String TCP = "TCP";
    private static final String UDP = "UDP";
    private static final long serialVersionUID = 1;
    private Address mAddress;
    private String mAuthUserName;
    private boolean mAutoRegistration;
    private transient int mCallingUid;
    private String mDomain;
    private String mPassword;
    private int mPort;
    private String mProfileName;
    private String mProtocol;
    private String mProxyAddress;
    private boolean mSendKeepAlive;

    /* renamed from: android.net.sip.SipProfile$1 */
    static class AnonymousClass1 implements Creator<SipProfile> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.sip.SipProfile.1.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        AnonymousClass1() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.sip.SipProfile.1.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.sip.SipProfile.1.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.sip.SipProfile.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object createFromParcel(android.os.Parcel r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.sip.SipProfile.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.sip.SipProfile.1.createFromParcel(android.os.Parcel):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.sip.SipProfile.1.newArray(int):java.lang.Object[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object[] newArray(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.sip.SipProfile.1.newArray(int):java.lang.Object[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.sip.SipProfile.1.newArray(int):java.lang.Object[]");
        }

        public SipProfile createFromParcel(Parcel in) {
            return new SipProfile(in, null);
        }

        public SipProfile[] newArray(int size) {
            return new SipProfile[size];
        }
    }

    public static class Builder {
        private AddressFactory mAddressFactory;
        private String mDisplayName;
        private SipProfile mProfile;
        private String mProxyAddress;
        private SipURI mUri;

        public Builder(SipProfile profile) {
            this.mProfile = new SipProfile();
            try {
                this.mAddressFactory = SipFactory.getInstance().createAddressFactory();
                if (profile == null) {
                    throw new NullPointerException();
                }
                try {
                    this.mProfile = (SipProfile) profile.clone();
                    this.mProfile.mAddress = null;
                    this.mUri = profile.getUri();
                    this.mUri.setUserPassword(profile.getPassword());
                    this.mDisplayName = profile.getDisplayName();
                    this.mProxyAddress = profile.getProxyAddress();
                    this.mProfile.mPort = profile.getPort();
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException("should not occur", e);
                }
            } catch (PeerUnavailableException e2) {
                throw new RuntimeException(e2);
            }
        }

        public Builder(String uriString) throws ParseException {
            this.mProfile = new SipProfile();
            try {
                this.mAddressFactory = SipFactory.getInstance().createAddressFactory();
                if (uriString == null) {
                    throw new NullPointerException("uriString cannot be null");
                }
                URI uri = this.mAddressFactory.createURI(fix(uriString));
                if (uri instanceof SipURI) {
                    this.mUri = (SipURI) uri;
                    this.mProfile.mDomain = this.mUri.getHost();
                    return;
                }
                throw new ParseException(uriString + " is not a SIP URI", 0);
            } catch (PeerUnavailableException e) {
                throw new RuntimeException(e);
            }
        }

        public Builder(String username, String serverDomain) throws ParseException {
            this.mProfile = new SipProfile();
            try {
                this.mAddressFactory = SipFactory.getInstance().createAddressFactory();
                if (username == null || serverDomain == null) {
                    throw new NullPointerException("username and serverDomain cannot be null");
                }
                this.mUri = this.mAddressFactory.createSipURI(username, serverDomain);
                this.mProfile.mDomain = serverDomain;
            } catch (PeerUnavailableException e) {
                throw new RuntimeException(e);
            }
        }

        private String fix(String uriString) {
            if (uriString.trim().toLowerCase().startsWith("sip:")) {
                return uriString;
            }
            return "sip:" + uriString;
        }

        public Builder setAuthUserName(String name) {
            this.mProfile.mAuthUserName = name;
            return this;
        }

        public Builder setProfileName(String name) {
            this.mProfile.mProfileName = name;
            return this;
        }

        public Builder setPassword(String password) {
            this.mUri.setUserPassword(password);
            return this;
        }

        public Builder setPort(int port) throws IllegalArgumentException {
            if (port > 65535 || port < 1000) {
                throw new IllegalArgumentException("incorrect port arugment: " + port);
            }
            this.mProfile.mPort = port;
            return this;
        }

        public Builder setProtocol(String protocol) throws IllegalArgumentException {
            if (protocol == null) {
                throw new NullPointerException("protocol cannot be null");
            }
            protocol = protocol.toUpperCase();
            if (protocol.equals(SipProfile.UDP) || protocol.equals(SipProfile.TCP)) {
                this.mProfile.mProtocol = protocol;
                return this;
            }
            throw new IllegalArgumentException("unsupported protocol: " + protocol);
        }

        public Builder setOutboundProxy(String outboundProxy) {
            this.mProxyAddress = outboundProxy;
            return this;
        }

        public Builder setDisplayName(String displayName) {
            this.mDisplayName = displayName;
            return this;
        }

        public Builder setSendKeepAlive(boolean flag) {
            this.mProfile.mSendKeepAlive = flag;
            return this;
        }

        public Builder setAutoRegistration(boolean flag) {
            this.mProfile.mAutoRegistration = flag;
            return this;
        }

        public SipProfile build() {
            this.mProfile.mPassword = this.mUri.getUserPassword();
            this.mUri.setUserPassword(null);
            try {
                if (TextUtils.isEmpty(this.mProxyAddress)) {
                    if (!this.mProfile.mProtocol.equals(SipProfile.UDP)) {
                        this.mUri.setTransportParam(this.mProfile.mProtocol);
                    }
                    if (this.mProfile.mPort != SipProfile.DEFAULT_PORT) {
                        this.mUri.setPort(this.mProfile.mPort);
                    }
                } else {
                    this.mProfile.mProxyAddress = ((SipURI) this.mAddressFactory.createURI(fix(this.mProxyAddress))).getHost();
                }
                this.mProfile.mAddress = this.mAddressFactory.createAddress(this.mDisplayName, this.mUri);
                return this.mProfile;
            } catch (InvalidArgumentException e) {
                throw new RuntimeException(e);
            } catch (ParseException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.sip.SipProfile.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.sip.SipProfile.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.sip.SipProfile.<clinit>():void");
    }

    /* synthetic */ SipProfile(SipProfile sipProfile) {
        this();
    }

    /* synthetic */ SipProfile(Parcel in, SipProfile sipProfile) {
        this(in);
    }

    private SipProfile() {
        this.mProtocol = UDP;
        this.mPort = DEFAULT_PORT;
        this.mSendKeepAlive = false;
        this.mAutoRegistration = true;
        this.mCallingUid = 0;
    }

    private SipProfile(Parcel in) {
        boolean z;
        boolean z2 = false;
        this.mProtocol = UDP;
        this.mPort = DEFAULT_PORT;
        this.mSendKeepAlive = false;
        this.mAutoRegistration = true;
        this.mCallingUid = 0;
        this.mAddress = (Address) in.readSerializable();
        this.mProxyAddress = in.readString();
        this.mPassword = in.readString();
        this.mDomain = in.readString();
        this.mProtocol = in.readString();
        this.mProfileName = in.readString();
        if (in.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mSendKeepAlive = z;
        if (in.readInt() != 0) {
            z2 = true;
        }
        this.mAutoRegistration = z2;
        this.mCallingUid = in.readInt();
        this.mPort = in.readInt();
        this.mAuthUserName = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeSerializable(this.mAddress);
        out.writeString(this.mProxyAddress);
        out.writeString(this.mPassword);
        out.writeString(this.mDomain);
        out.writeString(this.mProtocol);
        out.writeString(this.mProfileName);
        if (this.mSendKeepAlive) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.mAutoRegistration) {
            i2 = 0;
        }
        out.writeInt(i2);
        out.writeInt(this.mCallingUid);
        out.writeInt(this.mPort);
        out.writeString(this.mAuthUserName);
    }

    public int describeContents() {
        return 0;
    }

    public SipURI getUri() {
        return (SipURI) this.mAddress.getURI();
    }

    public String getUriString() {
        if (TextUtils.isEmpty(this.mProxyAddress)) {
            return getUri().toString();
        }
        return "sip:" + getUserName() + "@" + this.mDomain;
    }

    public Address getSipAddress() {
        return this.mAddress;
    }

    public String getDisplayName() {
        return this.mAddress.getDisplayName();
    }

    public String getUserName() {
        return getUri().getUser();
    }

    public String getAuthUserName() {
        return this.mAuthUserName;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public String getSipDomain() {
        return this.mDomain;
    }

    public int getPort() {
        return this.mPort;
    }

    public String getProtocol() {
        return this.mProtocol;
    }

    public String getProxyAddress() {
        return this.mProxyAddress;
    }

    public String getProfileName() {
        return this.mProfileName;
    }

    public boolean getSendKeepAlive() {
        return this.mSendKeepAlive;
    }

    public boolean getAutoRegistration() {
        return this.mAutoRegistration;
    }

    public void setCallingUid(int uid) {
        this.mCallingUid = uid;
    }

    public int getCallingUid() {
        return this.mCallingUid;
    }

    private Object readResolve() throws ObjectStreamException {
        if (this.mPort == 0) {
            this.mPort = DEFAULT_PORT;
        }
        return this;
    }
}
