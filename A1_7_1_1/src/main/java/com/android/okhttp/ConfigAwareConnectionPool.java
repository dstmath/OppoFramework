package com.android.okhttp;

import libcore.net.event.NetworkEventDispatcher;
import libcore.net.event.NetworkEventListener;

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
public class ConfigAwareConnectionPool {
    private static final long CONNECTION_POOL_DEFAULT_KEEP_ALIVE_DURATION_MS = 300000;
    private static final long CONNECTION_POOL_KEEP_ALIVE_DURATION_MS = 0;
    private static final int CONNECTION_POOL_MAX_IDLE_CONNECTIONS = 0;
    private static final ConfigAwareConnectionPool instance = null;
    private ConnectionPool connectionPool;
    private final NetworkEventDispatcher networkEventDispatcher;
    private boolean networkEventListenerRegistered;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.okhttp.ConfigAwareConnectionPool.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.okhttp.ConfigAwareConnectionPool.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.ConfigAwareConnectionPool.<clinit>():void");
    }

    protected ConfigAwareConnectionPool(NetworkEventDispatcher networkEventDispatcher) {
        this.networkEventDispatcher = networkEventDispatcher;
    }

    private ConfigAwareConnectionPool() {
        this.networkEventDispatcher = NetworkEventDispatcher.getInstance();
    }

    public static ConfigAwareConnectionPool getInstance() {
        return instance;
    }

    public synchronized ConnectionPool get() {
        if (this.connectionPool == null) {
            if (!this.networkEventListenerRegistered) {
                this.networkEventDispatcher.addListener(new NetworkEventListener() {
                    public void onNetworkConfigurationChanged() {
                        synchronized (ConfigAwareConnectionPool.this) {
                            ConfigAwareConnectionPool.this.connectionPool = null;
                        }
                    }
                });
                this.networkEventListenerRegistered = true;
            }
            this.connectionPool = new ConnectionPool(CONNECTION_POOL_MAX_IDLE_CONNECTIONS, CONNECTION_POOL_KEEP_ALIVE_DURATION_MS);
        }
        return this.connectionPool;
    }
}
