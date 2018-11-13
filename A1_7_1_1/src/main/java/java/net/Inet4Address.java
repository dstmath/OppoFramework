package java.net;

import android.system.OsConstants;
import java.io.ObjectStreamException;

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
public final class Inet4Address extends InetAddress {
    public static final InetAddress ALL = null;
    public static final InetAddress ANY = null;
    static final int INADDRSZ = 4;
    public static final InetAddress LOOPBACK = null;
    private static final int loopback = 2130706433;
    private static final long serialVersionUID = 3286316764910316507L;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.net.Inet4Address.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.net.Inet4Address.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.Inet4Address.<clinit>():void");
    }

    Inet4Address() {
        holder().hostName = null;
        holder().address = 0;
        holder().family = OsConstants.AF_INET;
    }

    Inet4Address(String hostName, byte[] addr) {
        holder().hostName = hostName;
        holder().family = OsConstants.AF_INET;
        if (addr != null && addr.length == 4) {
            holder().address = (((addr[3] & 255) | ((addr[2] << 8) & 65280)) | ((addr[1] << 16) & 16711680)) | ((addr[0] << 24) & -16777216);
        }
    }

    Inet4Address(String hostName, int address) {
        holder().hostName = hostName;
        holder().family = OsConstants.AF_INET;
        holder().address = address;
    }

    private Object writeReplace() throws ObjectStreamException {
        InetAddress inet = new InetAddress();
        inet.holder().hostName = holder().getHostName();
        inet.holder().address = holder().getAddress();
        inet.holder().family = 2;
        return inet;
    }

    public boolean isMulticastAddress() {
        return (holder().getAddress() & -268435456) == -536870912;
    }

    public boolean isAnyLocalAddress() {
        return holder().getAddress() == 0;
    }

    public boolean isLoopbackAddress() {
        if (getAddress()[0] == Byte.MAX_VALUE) {
            return true;
        }
        return false;
    }

    public boolean isLinkLocalAddress() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) == 169 && ((address >>> 16) & 255) == 254) {
            return true;
        }
        return false;
    }

    public boolean isSiteLocalAddress() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) == 10) {
            return true;
        }
        if (((address >>> 24) & 255) == 172 && ((address >>> 16) & 240) == 16) {
            return true;
        }
        if (((address >>> 24) & 255) != 192) {
            return false;
        }
        if (((address >>> 16) & 255) != 168) {
            return false;
        }
        return true;
    }

    public boolean isMCGlobal() {
        byte[] byteAddr = getAddress();
        if ((byteAddr[0] & 255) < 224 || (byteAddr[0] & 255) > 238) {
            return false;
        }
        if ((byteAddr[0] & 255) == 224 && byteAddr[1] == (byte) 0 && byteAddr[2] == (byte) 0) {
            return false;
        }
        return true;
    }

    public boolean isMCNodeLocal() {
        return false;
    }

    public boolean isMCLinkLocal() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) == 224 && ((address >>> 16) & 255) == 0 && ((address >>> 8) & 255) == 0) {
            return true;
        }
        return false;
    }

    public boolean isMCSiteLocal() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) == 239 && ((address >>> 16) & 255) == 255) {
            return true;
        }
        return false;
    }

    public boolean isMCOrgLocal() {
        int address = holder().getAddress();
        if (((address >>> 24) & 255) != 239 || ((address >>> 16) & 255) < 192 || ((address >>> 16) & 255) > 195) {
            return false;
        }
        return true;
    }

    public byte[] getAddress() {
        int address = holder().getAddress();
        byte[] addr = new byte[4];
        addr[0] = (byte) ((address >>> 24) & 255);
        addr[1] = (byte) ((address >>> 16) & 255);
        addr[2] = (byte) ((address >>> 8) & 255);
        addr[3] = (byte) (address & 255);
        return addr;
    }

    public String getHostAddress() {
        return numericToTextFormat(getAddress());
    }

    public int hashCode() {
        return holder().getAddress();
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof Inet4Address) && ((InetAddress) obj).holder().getAddress() == holder().getAddress()) {
            return true;
        }
        return false;
    }

    static String numericToTextFormat(byte[] src) {
        return (src[0] & 255) + "." + (src[1] & 255) + "." + (src[2] & 255) + "." + (src[3] & 255);
    }
}
