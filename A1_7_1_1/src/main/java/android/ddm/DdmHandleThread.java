package android.ddm;

import java.nio.ByteBuffer;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;
import org.apache.harmony.dalvik.ddmc.DdmVmInternal;

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
public class DdmHandleThread extends ChunkHandler {
    public static final int CHUNK_STKL = 0;
    public static final int CHUNK_THCR = 0;
    public static final int CHUNK_THDE = 0;
    public static final int CHUNK_THEN = 0;
    public static final int CHUNK_THST = 0;
    private static DdmHandleThread mInstance;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.ddm.DdmHandleThread.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.ddm.DdmHandleThread.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleThread.<clinit>():void");
    }

    private DdmHandleThread() {
    }

    public static void register() {
        DdmServer.registerHandler(CHUNK_THEN, mInstance);
        DdmServer.registerHandler(CHUNK_THST, mInstance);
        DdmServer.registerHandler(CHUNK_STKL, mInstance);
    }

    public void connected() {
    }

    public void disconnected() {
    }

    public Chunk handleChunk(Chunk request) {
        int type = request.type;
        if (type == CHUNK_THEN) {
            return handleTHEN(request);
        }
        if (type == CHUNK_THST) {
            return handleTHST(request);
        }
        if (type == CHUNK_STKL) {
            return handleSTKL(request);
        }
        throw new RuntimeException("Unknown packet " + ChunkHandler.name(type));
    }

    private Chunk handleTHEN(Chunk request) {
        boolean enable = false;
        if (wrapChunk(request).get() != (byte) 0) {
            enable = true;
        }
        DdmVmInternal.threadNotify(enable);
        return null;
    }

    private Chunk handleTHST(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        byte[] status = DdmVmInternal.getThreadStats();
        if (status != null) {
            return new Chunk(CHUNK_THST, status, 0, status.length);
        }
        return createFailChunk(1, "Can't build THST chunk");
    }

    private Chunk handleSTKL(Chunk request) {
        int threadId = wrapChunk(request).getInt();
        StackTraceElement[] trace = DdmVmInternal.getStackTraceById(threadId);
        if (trace == null) {
            return createFailChunk(1, "Stack trace unavailable");
        }
        return createStackChunk(trace, threadId);
    }

    private Chunk createStackChunk(StackTraceElement[] trace, int threadId) {
        int bufferSize = (4 + 4) + 4;
        for (StackTraceElement elem : trace) {
            bufferSize = ((bufferSize + ((elem.getClassName().length() * 2) + 4)) + ((elem.getMethodName().length() * 2) + 4)) + 4;
            if (elem.getFileName() != null) {
                bufferSize += elem.getFileName().length() * 2;
            }
            bufferSize += 4;
        }
        ByteBuffer out = ByteBuffer.allocate(bufferSize);
        out.putInt(0);
        out.putInt(threadId);
        out.putInt(trace.length);
        for (StackTraceElement elem2 : trace) {
            out.putInt(elem2.getClassName().length());
            putString(out, elem2.getClassName());
            out.putInt(elem2.getMethodName().length());
            putString(out, elem2.getMethodName());
            if (elem2.getFileName() != null) {
                out.putInt(elem2.getFileName().length());
                putString(out, elem2.getFileName());
            } else {
                out.putInt(0);
            }
            out.putInt(elem2.getLineNumber());
        }
        return new Chunk(CHUNK_STKL, out);
    }
}
