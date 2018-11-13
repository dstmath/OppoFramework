package android.view.animation;

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
public class OppoAccelerateDecelerateInterpolator extends BaseInterpolator {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "OppoAccelerateDecelerateInterpolator";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.animation.OppoAccelerateDecelerateInterpolator.<init>():void, dex: 
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
    public OppoAccelerateDecelerateInterpolator() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.animation.OppoAccelerateDecelerateInterpolator.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.animation.OppoAccelerateDecelerateInterpolator.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.animation.OppoAccelerateDecelerateInterpolator.<init>(android.content.Context, android.util.AttributeSet):void, dex: 
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
    public OppoAccelerateDecelerateInterpolator(android.content.Context r1, android.util.AttributeSet r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.animation.OppoAccelerateDecelerateInterpolator.<init>(android.content.Context, android.util.AttributeSet):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.animation.OppoAccelerateDecelerateInterpolator.<init>(android.content.Context, android.util.AttributeSet):void");
    }

    public float getInterpolation(float input) {
        if (input < 0.5f) {
            return (float) ((Math.cos((Math.sin((((double) input) * 3.141592653589793d) / 2.0d) + 1.0d) * 3.141592653589793d) + 1.0d) / 2.0d);
        }
        return (float) ((Math.cos((Math.sqrt((double) input) + 1.0d) * 3.141592653589793d) + 1.0d) / 2.0d);
    }
}
