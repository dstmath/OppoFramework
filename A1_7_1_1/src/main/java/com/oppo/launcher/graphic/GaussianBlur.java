package com.oppo.launcher.graphic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.SurfaceControl;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

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
public class GaussianBlur {
    private static final boolean DEBUG = false;
    private static final float DEFAULT_BRIGHTNESS = 0.8f;
    private static final int DEFAULT_RADIUS = 25;
    private static final ExecutorService EXECUTOR = null;
    private static final int EXECUTOR_THREADS = 0;
    private static final String TAG = "GaussianBlur";
    private static GaussianBlur mGaussianBlur;
    public static int mScreenHeight;
    public static int mScreenWidth;

    private class BlurTask implements Callable<Void> {
        private final int core;
        private final int cores;
        private final boolean hasAlpha;
        private final int height;
        private final int[] inout;
        private final int radius;
        private final int step;
        final /* synthetic */ GaussianBlur this$0;
        private final int width;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.launcher.graphic.GaussianBlur.BlurTask.<init>(com.oppo.launcher.graphic.GaussianBlur, int[], int, int, int, int, int, int, boolean):void, dex: 
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
        public BlurTask(com.oppo.launcher.graphic.GaussianBlur r1, int[] r2, int r3, int r4, int r5, int r6, int r7, int r8, boolean r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.launcher.graphic.GaussianBlur.BlurTask.<init>(com.oppo.launcher.graphic.GaussianBlur, int[], int, int, int, int, int, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.BlurTask.<init>(com.oppo.launcher.graphic.GaussianBlur, int[], int, int, int, int, int, int, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.BlurTask.call():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object call() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.BlurTask.call():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.BlurTask.call():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.launcher.graphic.GaussianBlur.BlurTask.call():java.lang.Void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.Void call() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.launcher.graphic.GaussianBlur.BlurTask.call():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.BlurTask.call():java.lang.Void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.launcher.graphic.GaussianBlur.<init>():void, dex: 
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
    private GaussianBlur() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.launcher.graphic.GaussianBlur.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.scaleBitmap(android.graphics.Bitmap, float):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static android.graphics.Bitmap scaleBitmap(android.graphics.Bitmap r1, float r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.scaleBitmap(android.graphics.Bitmap, float):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.scaleBitmap(android.graphics.Bitmap, float):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.setScreenWidth(android.content.Context):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static void setScreenWidth(android.content.Context r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.setScreenWidth(android.content.Context):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.setScreenWidth(android.content.Context):void");
    }

    public native void blurBrightness_native(int[] iArr, int i, int i2, float f);

    public native void blurIterationAlpha_native(int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    public native void blurIteration_native(int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.oppo.launcher.graphic.GaussianBlur.blurIteration_native(int[], int, int, int, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void blurIteration_native(int[] r1, int r2, int r3, int r4, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.oppo.launcher.graphic.GaussianBlur.blurIteration_native(int[], int, int, int, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.blurIteration_native(int[], int, int, int, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.blurIteration_nativeTask(int[], int, int, int, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void blurIteration_nativeTask(int[] r1, int r2, int r3, int r4, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.blurIteration_nativeTask(int[], int, int, int, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.blurIteration_nativeTask(int[], int, int, int, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, float, boolean):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public android.graphics.Bitmap generateGaussianBitmap(android.graphics.Bitmap r1, float r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, float, boolean):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, float, boolean):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, int, float, boolean):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public android.graphics.Bitmap generateGaussianBitmap(android.graphics.Bitmap r1, int r2, float r3, boolean r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, int, float, boolean):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, int, float, boolean):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, int, float, boolean, boolean):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public android.graphics.Bitmap generateGaussianBitmap(android.graphics.Bitmap r1, int r2, float r3, boolean r4, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, int, float, boolean, boolean):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, int, float, boolean, boolean):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, boolean):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public android.graphics.Bitmap generateGaussianBitmap(android.graphics.Bitmap r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, boolean):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmap(android.graphics.Bitmap, boolean):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmapTask(android.graphics.Bitmap, int, float, boolean, boolean):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public android.graphics.Bitmap generateGaussianBitmapTask(android.graphics.Bitmap r1, int r2, float r3, boolean r4, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmapTask(android.graphics.Bitmap, int, float, boolean, boolean):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.generateGaussianBitmapTask(android.graphics.Bitmap, int, float, boolean, boolean):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianScreenshot(android.content.Context, float, int, float):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public android.graphics.Bitmap generateGaussianScreenshot(android.content.Context r1, float r2, int r3, float r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianScreenshot(android.content.Context, float, int, float):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.generateGaussianScreenshot(android.content.Context, float, int, float):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianWallpaper(android.content.Context, float, int, float):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public android.graphics.Bitmap generateGaussianWallpaper(android.content.Context r1, float r2, int r3, float r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.launcher.graphic.GaussianBlur.generateGaussianWallpaper(android.content.Context, float, int, float):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.generateGaussianWallpaper(android.content.Context, float, int, float):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.launcher.graphic.GaussianBlur.setParameter(int, int, int):void, dex: 
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
    public void setParameter(int r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.launcher.graphic.GaussianBlur.setParameter(int, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.launcher.graphic.GaussianBlur.setParameter(int, int, int):void");
    }

    public static GaussianBlur getInstance() {
        if (mGaussianBlur == null) {
            mGaussianBlur = new GaussianBlur();
        }
        return mGaussianBlur;
    }

    public static Bitmap scaleBitmap(Bitmap bm) {
        return scaleBitmap(bm, 0.25f);
    }

    public Bitmap captureScreen(Context context, int width, int height, float scale, int minLayer, int maxLayer) {
        int w = width;
        int h = height;
        if (width == -1 || height == -1) {
            setScreenWidth(context);
            w = mScreenWidth;
            h = mScreenHeight;
        }
        if (minLayer < 0 || maxLayer < 0) {
            return SurfaceControl.screenshot((int) (((float) w) * scale), (int) (((float) h) * scale));
        }
        return SurfaceControl.screenshot(new Rect(0, 0, w, h), (int) (((float) w) * scale), (int) (((float) h) * scale), minLayer, maxLayer, false, 0);
    }
}
