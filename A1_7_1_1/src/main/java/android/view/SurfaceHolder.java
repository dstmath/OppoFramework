package android.view;

import android.graphics.Canvas;
import android.graphics.Rect;

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
public interface SurfaceHolder {
    @Deprecated
    public static final int SURFACE_TYPE_GPU = 2;
    @Deprecated
    public static final int SURFACE_TYPE_HARDWARE = 1;
    @Deprecated
    public static final int SURFACE_TYPE_NORMAL = 0;
    @Deprecated
    public static final int SURFACE_TYPE_PUSH_BUFFERS = 3;

    public static class BadSurfaceTypeException extends RuntimeException {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.SurfaceHolder.BadSurfaceTypeException.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public BadSurfaceTypeException() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.SurfaceHolder.BadSurfaceTypeException.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.SurfaceHolder.BadSurfaceTypeException.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.SurfaceHolder.BadSurfaceTypeException.<init>(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public BadSurfaceTypeException(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.SurfaceHolder.BadSurfaceTypeException.<init>(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.SurfaceHolder.BadSurfaceTypeException.<init>(java.lang.String):void");
        }
    }

    public interface Callback {
        void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3);

        void surfaceCreated(SurfaceHolder surfaceHolder);

        void surfaceDestroyed(SurfaceHolder surfaceHolder);
    }

    public interface Callback2 extends Callback {
        void surfaceRedrawNeeded(SurfaceHolder surfaceHolder);
    }

    void addCallback(Callback callback);

    Surface getSurface();

    Rect getSurfaceFrame();

    boolean isCreating();

    Canvas lockCanvas();

    Canvas lockCanvas(Rect rect);

    void removeCallback(Callback callback);

    void setFixedSize(int i, int i2);

    void setFormat(int i);

    void setKeepScreenOn(boolean z);

    void setSizeFromLayout();

    @Deprecated
    void setType(int i);

    void unlockCanvasAndPost(Canvas canvas);
}
