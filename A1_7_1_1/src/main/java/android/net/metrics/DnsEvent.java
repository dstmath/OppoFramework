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
public final class DnsEvent implements Parcelable {
    public static final Creator<DnsEvent> CREATOR = null;
    public final byte[] eventTypes;
    public final int[] latenciesMs;
    public final int netId;
    public final byte[] returnCodes;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.metrics.DnsEvent.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.metrics.DnsEvent.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.metrics.DnsEvent.<clinit>():void");
    }

    /* synthetic */ DnsEvent(Parcel in, DnsEvent dnsEvent) {
        this(in);
    }

    public DnsEvent(int netId, byte[] eventTypes, byte[] returnCodes, int[] latenciesMs) {
        this.netId = netId;
        this.eventTypes = eventTypes;
        this.returnCodes = returnCodes;
        this.latenciesMs = latenciesMs;
    }

    private DnsEvent(Parcel in) {
        this.netId = in.readInt();
        this.eventTypes = in.createByteArray();
        this.returnCodes = in.createByteArray();
        this.latenciesMs = in.createIntArray();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.netId);
        out.writeByteArray(this.eventTypes);
        out.writeByteArray(this.returnCodes);
        out.writeIntArray(this.latenciesMs);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(this.netId);
        objArr[1] = Integer.valueOf(this.eventTypes.length);
        return String.format("DnsEvent(%d, %d events)", objArr);
    }

    public static void logEvent(int netId, byte[] eventTypes, byte[] returnCodes, int[] latenciesMs) {
    }
}
