package android.net.metrics;

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
public final class DhcpClientEvent implements Parcelable {
    public static final Creator<DhcpClientEvent> CREATOR = null;
    public static final String INITIAL_BOUND = "InitialBoundState";
    public static final String RENEWING_BOUND = "RenewingBoundState";
    public final int durationMs;
    public final String ifName;
    public final String msg;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.metrics.DhcpClientEvent.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.metrics.DhcpClientEvent.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.DhcpClientEvent.<clinit>():void");
    }

    /* synthetic */ DhcpClientEvent(Parcel in, DhcpClientEvent dhcpClientEvent) {
        this(in);
    }

    public DhcpClientEvent(String ifName, String msg, int durationMs) {
        this.ifName = ifName;
        this.msg = msg;
        this.durationMs = durationMs;
    }

    private DhcpClientEvent(Parcel in) {
        this.ifName = in.readString();
        this.msg = in.readString();
        this.durationMs = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.ifName);
        out.writeString(this.msg);
        out.writeInt(this.durationMs);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        Object[] objArr = new Object[3];
        objArr[0] = this.ifName;
        objArr[1] = this.msg;
        objArr[2] = Integer.valueOf(this.durationMs);
        return String.format("DhcpClientEvent(%s, %s, %dms)", objArr);
    }

    public static void logStateEvent(String ifName, String state) {
    }
}
