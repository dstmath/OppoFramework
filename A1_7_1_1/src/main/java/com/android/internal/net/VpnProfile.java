package com.android.internal.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

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
    */
public class VpnProfile implements Cloneable, Parcelable {
    public static final Creator<VpnProfile> CREATOR = null;
    private static final String TAG = "VpnProfile";
    public static final int TYPE_IPSEC_HYBRID_RSA = 5;
    public static final int TYPE_IPSEC_XAUTH_PSK = 3;
    public static final int TYPE_IPSEC_XAUTH_RSA = 4;
    public static final int TYPE_L2TP_IPSEC_PSK = 1;
    public static final int TYPE_L2TP_IPSEC_RSA = 2;
    public static final int TYPE_MAX = 5;
    public static final int TYPE_PPTP = 0;
    public String dnsServers;
    public String ipsecCaCert;
    public String ipsecIdentifier;
    public String ipsecSecret;
    public String ipsecServerCert;
    public String ipsecUserCert;
    public final String key;
    public String l2tpSecret;
    public boolean mppe;
    public String name;
    public String password;
    public String routes;
    public boolean saveLogin;
    public String searchDomains;
    public String server;
    public int type;
    public String username;

    /* renamed from: com.android.internal.net.VpnProfile$1 */
    static class AnonymousClass1 implements Creator<VpnProfile> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.net.VpnProfile.1.<init>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.net.VpnProfile.1.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.net.VpnProfile.1.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.net.VpnProfile.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.net.VpnProfile.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.net.VpnProfile.1.createFromParcel(android.os.Parcel):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.net.VpnProfile.1.newArray(int):java.lang.Object[], dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.net.VpnProfile.1.newArray(int):java.lang.Object[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.net.VpnProfile.1.newArray(int):java.lang.Object[]");
        }

        public VpnProfile createFromParcel(Parcel in) {
            return new VpnProfile(in);
        }

        public VpnProfile[] newArray(int size) {
            return new VpnProfile[size];
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.net.VpnProfile.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.net.VpnProfile.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.net.VpnProfile.<clinit>():void");
    }

    public VpnProfile(String key) {
        this.name = PhoneConstants.MVNO_TYPE_NONE;
        this.type = 0;
        this.server = PhoneConstants.MVNO_TYPE_NONE;
        this.username = PhoneConstants.MVNO_TYPE_NONE;
        this.password = PhoneConstants.MVNO_TYPE_NONE;
        this.dnsServers = PhoneConstants.MVNO_TYPE_NONE;
        this.searchDomains = PhoneConstants.MVNO_TYPE_NONE;
        this.routes = PhoneConstants.MVNO_TYPE_NONE;
        this.mppe = true;
        this.l2tpSecret = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecIdentifier = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecSecret = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecUserCert = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecCaCert = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecServerCert = PhoneConstants.MVNO_TYPE_NONE;
        this.saveLogin = false;
        this.key = key;
    }

    public VpnProfile(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.name = PhoneConstants.MVNO_TYPE_NONE;
        this.type = 0;
        this.server = PhoneConstants.MVNO_TYPE_NONE;
        this.username = PhoneConstants.MVNO_TYPE_NONE;
        this.password = PhoneConstants.MVNO_TYPE_NONE;
        this.dnsServers = PhoneConstants.MVNO_TYPE_NONE;
        this.searchDomains = PhoneConstants.MVNO_TYPE_NONE;
        this.routes = PhoneConstants.MVNO_TYPE_NONE;
        this.mppe = true;
        this.l2tpSecret = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecIdentifier = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecSecret = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecUserCert = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecCaCert = PhoneConstants.MVNO_TYPE_NONE;
        this.ipsecServerCert = PhoneConstants.MVNO_TYPE_NONE;
        this.saveLogin = false;
        this.key = in.readString();
        this.name = in.readString();
        this.type = in.readInt();
        this.server = in.readString();
        this.username = in.readString();
        this.password = in.readString();
        this.dnsServers = in.readString();
        this.searchDomains = in.readString();
        this.routes = in.readString();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mppe = z;
        this.l2tpSecret = in.readString();
        this.ipsecIdentifier = in.readString();
        this.ipsecSecret = in.readString();
        this.ipsecUserCert = in.readString();
        this.ipsecCaCert = in.readString();
        this.ipsecServerCert = in.readString();
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.saveLogin = z2;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeString(this.key);
        out.writeString(this.name);
        out.writeInt(this.type);
        out.writeString(this.server);
        out.writeString(this.username);
        out.writeString(this.password);
        out.writeString(this.dnsServers);
        out.writeString(this.searchDomains);
        out.writeString(this.routes);
        if (this.mppe) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeString(this.l2tpSecret);
        out.writeString(this.ipsecIdentifier);
        out.writeString(this.ipsecSecret);
        out.writeString(this.ipsecUserCert);
        out.writeString(this.ipsecCaCert);
        out.writeString(this.ipsecServerCert);
        if (!this.saveLogin) {
            i2 = 0;
        }
        out.writeInt(i2);
    }

    public static VpnProfile decode(String key, byte[] value) {
        boolean z = true;
        if (key == null) {
            return null;
        }
        try {
            String[] values = new String(value, StandardCharsets.UTF_8).split("\u0000", -1);
            if (values.length < 14 || values.length > 15) {
                return null;
            }
            VpnProfile profile = new VpnProfile(key);
            profile.name = values[0];
            profile.type = Integer.parseInt(values[1]);
            if (profile.type < 0 || profile.type > 5) {
                return null;
            }
            profile.server = values[2];
            profile.username = values[3];
            profile.password = values[4];
            profile.dnsServers = values[5];
            profile.searchDomains = values[6];
            profile.routes = values[7];
            profile.mppe = Boolean.valueOf(values[8]).booleanValue();
            profile.l2tpSecret = values[9];
            profile.ipsecIdentifier = values[10];
            profile.ipsecSecret = values[11];
            profile.ipsecUserCert = values[12];
            profile.ipsecCaCert = values[13];
            profile.ipsecServerCert = values.length > 14 ? values[14] : PhoneConstants.MVNO_TYPE_NONE;
            if (profile.username.isEmpty() && profile.password.isEmpty()) {
                z = false;
            }
            profile.saveLogin = z;
            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] encode() {
        StringBuilder builder = new StringBuilder(this.name);
        builder.append(0).append(this.type);
        builder.append(0).append(this.server);
        builder.append(0).append(this.saveLogin ? this.username : PhoneConstants.MVNO_TYPE_NONE);
        builder.append(0).append(this.saveLogin ? this.password : PhoneConstants.MVNO_TYPE_NONE);
        builder.append(0).append(this.dnsServers);
        builder.append(0).append(this.searchDomains);
        builder.append(0).append(this.routes);
        builder.append(0).append(this.mppe);
        builder.append(0).append(this.l2tpSecret);
        builder.append(0).append(this.ipsecIdentifier);
        builder.append(0).append(this.ipsecSecret);
        builder.append(0).append(this.ipsecUserCert);
        builder.append(0).append(this.ipsecCaCert);
        builder.append(0).append(this.ipsecServerCert);
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValidLockdownProfile() {
        if (this.type == 0) {
            return false;
        }
        try {
            InetAddress.parseNumericAddress(this.server);
            for (String dnsServer : this.dnsServers.split(" +")) {
                InetAddress.parseNumericAddress(this.dnsServers);
            }
            if (!TextUtils.isEmpty(this.dnsServers)) {
                return true;
            }
            Log.w(TAG, "DNS required");
            return false;
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid address", e);
            return false;
        }
    }

    public int describeContents() {
        return 0;
    }
}
