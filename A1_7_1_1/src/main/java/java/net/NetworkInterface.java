package java.net;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

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
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
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
public final class NetworkInterface {
    private static final int defaultIndex = 0;
    private static final NetworkInterface defaultInterface = null;
    private InetAddress[] addrs;
    private InterfaceAddress[] bindings;
    private NetworkInterface[] childs;
    private String displayName;
    private byte[] hardwareAddr;
    private int index;
    private String name;
    private NetworkInterface parent;
    private boolean virtual;

    /* renamed from: java.net.NetworkInterface$1subIFs */
    class AnonymousClass1subIFs implements Enumeration<NetworkInterface> {
        private int i;
        final /* synthetic */ NetworkInterface this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.net.NetworkInterface.1subIFs.<init>(java.net.NetworkInterface):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1subIFs(java.net.NetworkInterface r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.net.NetworkInterface.1subIFs.<init>(java.net.NetworkInterface):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.NetworkInterface.1subIFs.<init>(java.net.NetworkInterface):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.net.NetworkInterface.1subIFs.hasMoreElements():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean hasMoreElements() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.net.NetworkInterface.1subIFs.hasMoreElements():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.NetworkInterface.1subIFs.hasMoreElements():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.NetworkInterface.1subIFs.nextElement():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.NetworkInterface.1subIFs.nextElement():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.NetworkInterface.1subIFs.nextElement():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.net.NetworkInterface.1subIFs.nextElement():java.net.NetworkInterface, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.net.NetworkInterface nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.net.NetworkInterface.1subIFs.nextElement():java.net.NetworkInterface, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.NetworkInterface.1subIFs.nextElement():java.net.NetworkInterface");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.NetworkInterface.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.NetworkInterface.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.NetworkInterface.<clinit>():void");
    }

    private static native NetworkInterface[] getAll() throws SocketException;

    private static native NetworkInterface getByIndex0(int i) throws SocketException;

    private static native NetworkInterface getByInetAddress0(InetAddress inetAddress) throws SocketException;

    private static native NetworkInterface getByName0(String str) throws SocketException;

    private static native int getMTU0(String str, int i) throws SocketException;

    private static native boolean isLoopback0(String str, int i) throws SocketException;

    private static native boolean isP2P0(String str, int i) throws SocketException;

    private static native boolean isUp0(String str, int i) throws SocketException;

    private static native boolean supportsMulticast0(String str, int i) throws SocketException;

    NetworkInterface() {
        this.parent = null;
        this.virtual = false;
    }

    NetworkInterface(String name, int index, InetAddress[] addrs) {
        this.parent = null;
        this.virtual = false;
        this.name = name;
        this.index = index;
        this.addrs = addrs;
    }

    public String getName() {
        return this.name;
    }

    public Enumeration<InetAddress> getInetAddresses() {
        return new Enumeration<InetAddress>() {
            private int count = 0;
            private int i = 0;
            private InetAddress[] local_addrs;

            /* JADX WARNING: Removed duplicated region for block: B:7:0x002d A:{SKIP} */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            {
                this.local_addrs = new InetAddress[NetworkInterface.this.addrs.length];
                boolean trusted = true;
                SecurityManager sec = System.getSecurityManager();
                if (sec != null) {
                    try {
                        sec.checkPermission(new NetPermission("getNetworkInformation"));
                    } catch (SecurityException e) {
                        trusted = false;
                    }
                }
                int j = 0;
                if (j >= NetworkInterface.this.addrs.length) {
                    if (!(sec == null || trusted)) {
                        sec.checkConnect(NetworkInterface.this.addrs[j].getHostAddress(), -1);
                    }
                    try {
                    } catch (SecurityException e2) {
                    }
                    InetAddress[] inetAddressArr = this.local_addrs;
                    int i = this.count;
                    this.count = i + 1;
                    inetAddressArr[i] = NetworkInterface.this.addrs[j];
                    j++;
                    if (j >= NetworkInterface.this.addrs.length) {
                    }
                }
            }

            public InetAddress nextElement() {
                if (this.i < this.count) {
                    InetAddress[] inetAddressArr = this.local_addrs;
                    int i = this.i;
                    this.i = i + 1;
                    return inetAddressArr[i];
                }
                throw new NoSuchElementException();
            }

            public boolean hasMoreElements() {
                return this.i < this.count;
            }
        };
    }

    public List<InterfaceAddress> getInterfaceAddresses() {
        List<InterfaceAddress> lst = new ArrayList(1);
        SecurityManager sec = System.getSecurityManager();
        for (int j = 0; j < this.bindings.length; j++) {
            if (sec != null) {
                try {
                    sec.checkConnect(this.bindings[j].getAddress().getHostAddress(), -1);
                } catch (SecurityException e) {
                }
            }
            lst.add(this.bindings[j]);
        }
        return lst;
    }

    public Enumeration<NetworkInterface> getSubInterfaces() {
        return new AnonymousClass1subIFs(this);
    }

    public NetworkInterface getParent() {
        return this.parent;
    }

    public int getIndex() {
        return this.index;
    }

    public String getDisplayName() {
        return "".equals(this.displayName) ? null : this.displayName;
    }

    public static NetworkInterface getByName(String name) throws SocketException {
        if (name != null) {
            return getByName0(name);
        }
        throw new NullPointerException();
    }

    public static NetworkInterface getByIndex(int index) throws SocketException {
        if (index >= 0) {
            return getByIndex0(index);
        }
        throw new IllegalArgumentException("Interface index can't be negative");
    }

    public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
        if (addr == null) {
            throw new NullPointerException();
        }
        if (!(addr instanceof Inet4Address) ? addr instanceof Inet6Address : true) {
            return getByInetAddress0(addr);
        }
        throw new IllegalArgumentException("invalid address type");
    }

    public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
        final NetworkInterface[] netifs = getAll();
        if (netifs == null) {
            return null;
        }
        return new Enumeration<NetworkInterface>() {
            private int i = 0;

            public NetworkInterface nextElement() {
                if (netifs == null || this.i >= netifs.length) {
                    throw new NoSuchElementException();
                }
                NetworkInterface[] networkInterfaceArr = netifs;
                int i = this.i;
                this.i = i + 1;
                return networkInterfaceArr[i];
            }

            public boolean hasMoreElements() {
                return netifs != null && this.i < netifs.length;
            }
        };
    }

    public boolean isUp() throws SocketException {
        return isUp0(this.name, this.index);
    }

    public boolean isLoopback() throws SocketException {
        return isLoopback0(this.name, this.index);
    }

    public boolean isPointToPoint() throws SocketException {
        return isP2P0(this.name, this.index);
    }

    public boolean supportsMulticast() throws SocketException {
        return supportsMulticast0(this.name, this.index);
    }

    public byte[] getHardwareAddress() throws SocketException {
        NetworkInterface ni = getByName0(this.name);
        if (ni != null) {
            return ni.hardwareAddr;
        }
        throw new SocketException("NetworkInterface doesn't exist anymore");
    }

    public int getMTU() throws SocketException {
        return getMTU0(this.name, this.index);
    }

    public boolean isVirtual() {
        return this.virtual;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface that = (NetworkInterface) obj;
        if (this.name != null) {
            if (!this.name.equals(that.name)) {
                return false;
            }
        } else if (that.name != null) {
            return false;
        }
        if (this.addrs == null) {
            if (that.addrs != null) {
                z = false;
            }
            return z;
        } else if (that.addrs == null || this.addrs.length != that.addrs.length) {
            return false;
        } else {
            for (int i = 0; i < count; i++) {
                boolean found = false;
                for (Object equals : that.addrs) {
                    if (this.addrs[i].equals(equals)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        return this.name == null ? 0 : this.name.hashCode();
    }

    public String toString() {
        String result = "name:" + (this.name == null ? "null" : this.name);
        if (this.displayName != null) {
            return result + " (" + this.displayName + ")";
        }
        return result;
    }

    static NetworkInterface getDefault() {
        return defaultInterface;
    }
}
