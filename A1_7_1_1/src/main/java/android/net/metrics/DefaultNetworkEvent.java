package android.net.metrics;

import android.net.NetworkCapabilities;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.security.keystore.KeyProperties;

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
public final class DefaultNetworkEvent implements Parcelable {
    public static final Creator<DefaultNetworkEvent> CREATOR = null;
    public final int netId;
    public final boolean prevIPv4;
    public final boolean prevIPv6;
    public final int prevNetId;
    public final int[] transportTypes;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.metrics.DefaultNetworkEvent.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.metrics.DefaultNetworkEvent.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.DefaultNetworkEvent.<clinit>():void");
    }

    /* synthetic */ DefaultNetworkEvent(Parcel in, DefaultNetworkEvent defaultNetworkEvent) {
        this(in);
    }

    public DefaultNetworkEvent(int netId, int[] transportTypes, int prevNetId, boolean prevIPv4, boolean prevIPv6) {
        this.netId = netId;
        this.transportTypes = transportTypes;
        this.prevNetId = prevNetId;
        this.prevIPv4 = prevIPv4;
        this.prevIPv6 = prevIPv6;
    }

    private DefaultNetworkEvent(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.netId = in.readInt();
        this.transportTypes = in.createIntArray();
        this.prevNetId = in.readInt();
        if (in.readByte() > (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.prevIPv4 = z;
        if (in.readByte() <= (byte) 0) {
            z2 = false;
        }
        this.prevIPv6 = z2;
    }

    public void writeToParcel(Parcel out, int flags) {
        byte b;
        byte b2 = (byte) 1;
        out.writeInt(this.netId);
        out.writeIntArray(this.transportTypes);
        out.writeInt(this.prevNetId);
        if (this.prevIPv4) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        out.writeByte(b);
        if (!this.prevIPv6) {
            b2 = (byte) 0;
        }
        out.writeByte(b2);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        String prevNetwork = String.valueOf(this.prevNetId);
        String newNetwork = String.valueOf(this.netId);
        if (this.prevNetId != 0) {
            prevNetwork = prevNetwork + ":" + ipSupport();
        }
        if (this.netId != 0) {
            newNetwork = newNetwork + ":" + NetworkCapabilities.transportNamesOf(this.transportTypes);
        }
        Object[] objArr = new Object[2];
        objArr[0] = prevNetwork;
        objArr[1] = newNetwork;
        return String.format("DefaultNetworkEvent(%s -> %s)", objArr);
    }

    private String ipSupport() {
        if (this.prevIPv4 && this.prevIPv6) {
            return "DUAL";
        }
        if (this.prevIPv6) {
            return "IPv6";
        }
        if (this.prevIPv4) {
            return "IPv4";
        }
        return KeyProperties.DIGEST_NONE;
    }

    public static void logEvent(int netId, int[] transports, int prevNetId, boolean hadIPv4, boolean hadIPv6) {
    }
}
