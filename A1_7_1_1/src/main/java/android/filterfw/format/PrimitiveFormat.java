package android.filterfw.format;

import android.filterfw.core.MutableFrameFormat;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class PrimitiveFormat {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.filterfw.format.PrimitiveFormat.<init>():void, dex: 
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
    public PrimitiveFormat() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.filterfw.format.PrimitiveFormat.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterfw.format.PrimitiveFormat.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.filterfw.format.PrimitiveFormat.createFormat(int, int):android.filterfw.core.MutableFrameFormat, dex: 
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
    private static android.filterfw.core.MutableFrameFormat createFormat(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.filterfw.format.PrimitiveFormat.createFormat(int, int):android.filterfw.core.MutableFrameFormat, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterfw.format.PrimitiveFormat.createFormat(int, int):android.filterfw.core.MutableFrameFormat");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.filterfw.format.PrimitiveFormat.createFormat(int, int, int):android.filterfw.core.MutableFrameFormat, dex: 
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
    private static android.filterfw.core.MutableFrameFormat createFormat(int r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.filterfw.format.PrimitiveFormat.createFormat(int, int, int):android.filterfw.core.MutableFrameFormat, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterfw.format.PrimitiveFormat.createFormat(int, int, int):android.filterfw.core.MutableFrameFormat");
    }

    public static MutableFrameFormat createByteFormat(int count, int target) {
        return createFormat(2, count, target);
    }

    public static MutableFrameFormat createInt16Format(int count, int target) {
        return createFormat(3, count, target);
    }

    public static MutableFrameFormat createInt32Format(int count, int target) {
        return createFormat(4, count, target);
    }

    public static MutableFrameFormat createFloatFormat(int count, int target) {
        return createFormat(5, count, target);
    }

    public static MutableFrameFormat createDoubleFormat(int count, int target) {
        return createFormat(6, count, target);
    }

    public static MutableFrameFormat createByteFormat(int target) {
        return createFormat(2, target);
    }

    public static MutableFrameFormat createInt16Format(int target) {
        return createFormat(3, target);
    }

    public static MutableFrameFormat createInt32Format(int target) {
        return createFormat(4, target);
    }

    public static MutableFrameFormat createFloatFormat(int target) {
        return createFormat(5, target);
    }

    public static MutableFrameFormat createDoubleFormat(int target) {
        return createFormat(6, target);
    }
}
