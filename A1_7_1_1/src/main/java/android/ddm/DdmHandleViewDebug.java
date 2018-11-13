package android.ddm;

import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.view.WindowManagerGlobal;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;

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
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DdmHandleViewDebug extends ChunkHandler {
    private static final int CHUNK_VULW = 0;
    private static final int CHUNK_VUOP = 0;
    private static final int CHUNK_VURT = 0;
    private static final int ERR_EXCEPTION = -3;
    private static final int ERR_INVALID_OP = -1;
    private static final int ERR_INVALID_PARAM = -2;
    private static final String TAG = "DdmViewDebug";
    private static final int VUOP_CAPTURE_VIEW = 1;
    private static final int VUOP_DUMP_DISPLAYLIST = 2;
    private static final int VUOP_INVOKE_VIEW_METHOD = 4;
    private static final int VUOP_PROFILE_VIEW = 3;
    private static final int VUOP_SET_LAYOUT_PARAMETER = 5;
    private static final int VURT_CAPTURE_LAYERS = 2;
    private static final int VURT_DUMP_HIERARCHY = 1;
    private static final int VURT_DUMP_THEME = 3;
    private static final DdmHandleViewDebug sInstance = null;

    /* renamed from: android.ddm.DdmHandleViewDebug$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ DdmHandleViewDebug this$0;
        final /* synthetic */ View val$rootView;
        final /* synthetic */ View val$targetView;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.ddm.DdmHandleViewDebug.1.<init>(android.ddm.DdmHandleViewDebug, android.view.View, android.view.View):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass1(android.ddm.DdmHandleViewDebug r1, android.view.View r2, android.view.View r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.ddm.DdmHandleViewDebug.1.<init>(android.ddm.DdmHandleViewDebug, android.view.View, android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleViewDebug.1.<init>(android.ddm.DdmHandleViewDebug, android.view.View, android.view.View):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.ddm.DdmHandleViewDebug.1.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.ddm.DdmHandleViewDebug.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleViewDebug.1.run():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.ddm.DdmHandleViewDebug.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.ddm.DdmHandleViewDebug.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleViewDebug.<clinit>():void");
    }

    private DdmHandleViewDebug() {
    }

    public static void register() {
        DdmServer.registerHandler(CHUNK_VULW, sInstance);
        DdmServer.registerHandler(CHUNK_VURT, sInstance);
        DdmServer.registerHandler(CHUNK_VUOP, sInstance);
    }

    public void connected() {
    }

    public void disconnected() {
    }

    public Chunk handleChunk(Chunk request) {
        int type = request.type;
        if (type == CHUNK_VULW) {
            return listWindows();
        }
        ByteBuffer in = wrapChunk(request);
        int op = in.getInt();
        View rootView = getRootView(in);
        if (rootView == null) {
            return createFailChunk(-2, "Invalid View Root");
        }
        if (type != CHUNK_VURT) {
            View targetView = getTargetView(rootView, in);
            if (targetView == null) {
                return createFailChunk(-2, "Invalid target view");
            }
            if (type == CHUNK_VUOP) {
                switch (op) {
                    case 1:
                        return captureView(rootView, targetView);
                    case 2:
                        return dumpDisplayLists(rootView, targetView);
                    case 3:
                        return profileView(rootView, targetView);
                    case 4:
                        return invokeViewMethod(rootView, targetView, in);
                    case 5:
                        return setLayoutParameter(rootView, targetView, in);
                    default:
                        return createFailChunk(-1, "Unknown view operation: " + op);
                }
            }
            throw new RuntimeException("Unknown packet " + ChunkHandler.name(type));
        } else if (op == 1) {
            return dumpHierarchy(rootView, in);
        } else {
            if (op == 2) {
                return captureLayers(rootView);
            }
            if (op == 3) {
                return dumpTheme(rootView);
            }
            return createFailChunk(-1, "Unknown view root operation: " + op);
        }
    }

    private Chunk listWindows() {
        String name;
        int i = 0;
        String[] windowNames = WindowManagerGlobal.getInstance().getViewRootNames();
        int responseLength = 4;
        for (String name2 : windowNames) {
            responseLength = (responseLength + 4) + (name2.length() * 2);
        }
        ByteBuffer out = ByteBuffer.allocate(responseLength);
        out.order(ChunkHandler.CHUNK_ORDER);
        out.putInt(windowNames.length);
        int length = windowNames.length;
        while (i < length) {
            name2 = windowNames[i];
            out.putInt(name2.length());
            putString(out, name2);
            i++;
        }
        return new Chunk(CHUNK_VULW, out);
    }

    private View getRootView(ByteBuffer in) {
        try {
            return WindowManagerGlobal.getInstance().getRootView(getString(in, in.getInt()));
        } catch (BufferUnderflowException e) {
            return null;
        }
    }

    private View getTargetView(View root, ByteBuffer in) {
        try {
            return ViewDebug.findView(root, getString(in, in.getInt()));
        } catch (BufferUnderflowException e) {
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0063 A:{ExcHandler: java.io.IOException (r2_0 'e' java.lang.Exception), Splitter: B:13:0x0028} */
    /* JADX WARNING: Missing block: B:22:0x0063, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:24:0x0081, code:
            return createFailChunk(1, "Unexpected error while obtaining view hierarchy: " + r2.getMessage());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Chunk dumpHierarchy(View rootView, ByteBuffer in) {
        boolean skipChildren = in.getInt() > 0;
        boolean includeProperties = in.getInt() > 0;
        boolean v2 = in.hasRemaining() && in.getInt() > 0;
        long start = System.currentTimeMillis();
        ByteArrayOutputStream b = new ByteArrayOutputStream(2097152);
        if (v2) {
            try {
                ViewDebug.dumpv2(rootView, b);
            } catch (Exception e) {
            }
        } else {
            ViewDebug.dump(rootView, skipChildren, includeProperties, b);
        }
        Log.d(TAG, "Time to obtain view hierarchy (ms): " + (System.currentTimeMillis() - start));
        byte[] data = b.toByteArray();
        return new Chunk(CHUNK_VURT, data, 0, data.length);
    }

    private Chunk captureLayers(View rootView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
        DataOutputStream dos = new DataOutputStream(b);
        try {
            ViewDebug.captureLayers(rootView, dos);
            try {
                dos.close();
            } catch (IOException e) {
            }
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VURT, data, 0, data.length);
        } catch (IOException e2) {
            Chunk createFailChunk = createFailChunk(1, "Unexpected error while obtaining view hierarchy: " + e2.getMessage());
            try {
                dos.close();
            } catch (IOException e3) {
            }
            return createFailChunk;
        } catch (Throwable th) {
            try {
                dos.close();
            } catch (IOException e4) {
            }
            throw th;
        }
    }

    private Chunk dumpTheme(View rootView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
        try {
            ViewDebug.dumpTheme(rootView, b);
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VURT, data, 0, data.length);
        } catch (IOException e) {
            return createFailChunk(1, "Unexpected error while dumping the theme: " + e.getMessage());
        }
    }

    private Chunk captureView(View rootView, View targetView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
        try {
            ViewDebug.capture(rootView, b, targetView);
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VUOP, data, 0, data.length);
        } catch (IOException e) {
            return createFailChunk(1, "Unexpected error while capturing view: " + e.getMessage());
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private org.apache.harmony.dalvik.ddmc.Chunk dumpDisplayLists(android.view.View r2, android.view.View r3) {
        /*
        r1 = this;
        r0 = new android.ddm.DdmHandleViewDebug$1;
        r0.<init>(r1, r2, r3);
        r2.post(r0);
        r0 = 0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.ddm.DdmHandleViewDebug.dumpDisplayLists(android.view.View, android.view.View):org.apache.harmony.dalvik.ddmc.Chunk");
    }

    private Chunk invokeViewMethod(View rootView, View targetView, ByteBuffer in) {
        Class<?>[] argTypes;
        Object[] args;
        String methodName = getString(in, in.getInt());
        if (in.hasRemaining()) {
            int nArgs = in.getInt();
            argTypes = new Class[nArgs];
            args = new Object[nArgs];
            for (int i = 0; i < nArgs; i++) {
                char c = in.getChar();
                switch (c) {
                    case 'B':
                        argTypes[i] = Byte.TYPE;
                        args[i] = Byte.valueOf(in.get());
                        break;
                    case 'C':
                        argTypes[i] = Character.TYPE;
                        args[i] = Character.valueOf(in.getChar());
                        break;
                    case 'D':
                        argTypes[i] = Double.TYPE;
                        args[i] = Double.valueOf(in.getDouble());
                        break;
                    case 'F':
                        argTypes[i] = Float.TYPE;
                        args[i] = Float.valueOf(in.getFloat());
                        break;
                    case 'I':
                        argTypes[i] = Integer.TYPE;
                        args[i] = Integer.valueOf(in.getInt());
                        break;
                    case 'J':
                        argTypes[i] = Long.TYPE;
                        args[i] = Long.valueOf(in.getLong());
                        break;
                    case 'S':
                        argTypes[i] = Short.TYPE;
                        args[i] = Short.valueOf(in.getShort());
                        break;
                    case 'Z':
                        boolean z;
                        argTypes[i] = Boolean.TYPE;
                        if (in.get() == (byte) 0) {
                            z = false;
                        } else {
                            z = true;
                        }
                        args[i] = Boolean.valueOf(z);
                        break;
                    default:
                        Log.e(TAG, "arg " + i + ", unrecognized type: " + c);
                        return createFailChunk(-2, "Unsupported parameter type (" + c + ") to invoke view method.");
                }
            }
        } else {
            argTypes = new Class[0];
            args = new Object[0];
        }
        try {
            try {
                ViewDebug.invokeViewMethod(targetView, targetView.getClass().getMethod(methodName, argTypes), args);
                return null;
            } catch (Exception e) {
                Log.e(TAG, "Exception while invoking method: " + e.getCause().getMessage());
                String msg = e.getCause().getMessage();
                if (msg == null) {
                    msg = e.getCause().toString();
                }
                return createFailChunk(-3, msg);
            }
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "No such method: " + e2.getMessage());
            return createFailChunk(-2, "No such method: " + e2.getMessage());
        }
    }

    private Chunk setLayoutParameter(View rootView, View targetView, ByteBuffer in) {
        String param = getString(in, in.getInt());
        try {
            ViewDebug.setLayoutParameter(targetView, param, in.getInt());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception setting layout parameter: " + e);
            return createFailChunk(-3, "Error accessing field " + param + ":" + e.getMessage());
        }
    }

    private Chunk profileView(View rootView, View targetView) {
        ByteArrayOutputStream b = new ByteArrayOutputStream(32768);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(b), 32768);
        try {
            ViewDebug.profileViewAndChildren(targetView, bw);
            try {
                bw.close();
            } catch (IOException e) {
            }
            byte[] data = b.toByteArray();
            return new Chunk(CHUNK_VUOP, data, 0, data.length);
        } catch (IOException e2) {
            Chunk createFailChunk = createFailChunk(1, "Unexpected error while profiling view: " + e2.getMessage());
            try {
                bw.close();
            } catch (IOException e3) {
            }
            return createFailChunk;
        } catch (Throwable th) {
            try {
                bw.close();
            } catch (IOException e4) {
            }
            throw th;
        }
    }
}
