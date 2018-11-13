package org.apache.harmony.dalvik.ddmc;

import java.util.HashMap;

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
public class DdmServer {
    public static final int CLIENT_PROTOCOL_VERSION = 1;
    private static final int CONNECTED = 1;
    private static final int DISCONNECTED = 2;
    private static HashMap<Integer, ChunkHandler> mHandlerMap;
    private static volatile boolean mRegistrationComplete;
    private static boolean mRegistrationTimedOut;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.dalvik.ddmc.DdmServer.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.harmony.dalvik.ddmc.DdmServer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.dalvik.ddmc.DdmServer.<clinit>():void");
    }

    private static native void nativeSendChunk(int i, byte[] bArr, int i2, int i3);

    private DdmServer() {
    }

    public static void registerHandler(int type, ChunkHandler handler) {
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        synchronized (mHandlerMap) {
            if (mHandlerMap.get(Integer.valueOf(type)) != null) {
                throw new RuntimeException("type " + Integer.toHexString(type) + " already registered");
            }
            mHandlerMap.put(Integer.valueOf(type), handler);
        }
    }

    public static ChunkHandler unregisterHandler(int type) {
        ChunkHandler chunkHandler;
        synchronized (mHandlerMap) {
            chunkHandler = (ChunkHandler) mHandlerMap.remove(Integer.valueOf(type));
        }
        return chunkHandler;
    }

    public static void registrationComplete() {
        synchronized (mHandlerMap) {
            mRegistrationComplete = true;
            mHandlerMap.notifyAll();
        }
    }

    public static void sendChunk(Chunk chunk) {
        nativeSendChunk(chunk.type, chunk.data, chunk.offset, chunk.length);
    }

    private static void broadcast(int event) {
        synchronized (mHandlerMap) {
            for (ChunkHandler handler : mHandlerMap.values()) {
                switch (event) {
                    case 1:
                        handler.connected();
                        break;
                    case 2:
                        handler.disconnected();
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }
    }

    private static Chunk dispatch(int type, byte[] data, int offset, int length) {
        ChunkHandler handler;
        synchronized (mHandlerMap) {
            while (!mRegistrationComplete && !mRegistrationTimedOut) {
                try {
                    mHandlerMap.wait(1000);
                    if (!mRegistrationComplete) {
                        mRegistrationTimedOut = true;
                    }
                } catch (InterruptedException e) {
                }
            }
            handler = (ChunkHandler) mHandlerMap.get(Integer.valueOf(type));
        }
        if (handler == null) {
            return null;
        }
        return handler.handleChunk(new Chunk(type, data, offset, length));
    }
}
