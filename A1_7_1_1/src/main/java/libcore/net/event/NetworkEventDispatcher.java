package libcore.net.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class NetworkEventDispatcher {
    private static final NetworkEventDispatcher instance = null;
    private final List<NetworkEventListener> listeners;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: libcore.net.event.NetworkEventDispatcher.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: libcore.net.event.NetworkEventDispatcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.net.event.NetworkEventDispatcher.<clinit>():void");
    }

    public static NetworkEventDispatcher getInstance() {
        return instance;
    }

    protected NetworkEventDispatcher() {
        this.listeners = new CopyOnWriteArrayList();
    }

    public void addListener(NetworkEventListener toAdd) {
        if (toAdd == null) {
            throw new NullPointerException("toAdd == null");
        }
        this.listeners.add(toAdd);
    }

    public void removeListener(NetworkEventListener toRemove) {
        for (NetworkEventListener listener : this.listeners) {
            if (listener == toRemove) {
                this.listeners.remove(listener);
                return;
            }
        }
    }

    public void onNetworkConfigurationChanged() {
        for (NetworkEventListener listener : this.listeners) {
            try {
                listener.onNetworkConfigurationChanged();
            } catch (RuntimeException e) {
                System.logI("Exception thrown during network event propagation", e);
            }
        }
    }
}
