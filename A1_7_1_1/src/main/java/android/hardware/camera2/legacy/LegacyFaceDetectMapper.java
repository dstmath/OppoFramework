package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;

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
public class LegacyFaceDetectMapper {
    private static final boolean DEBUG = false;
    private static String TAG;
    private final Camera mCamera;
    private boolean mFaceDetectEnabled;
    private boolean mFaceDetectReporting;
    private boolean mFaceDetectScenePriority;
    private final boolean mFaceDetectSupported;
    private Face[] mFaces;
    private Face[] mFacesPrev;
    private final Object mLock;

    /* renamed from: android.hardware.camera2.legacy.LegacyFaceDetectMapper$1 */
    class AnonymousClass1 implements FaceDetectionListener {
        final /* synthetic */ LegacyFaceDetectMapper this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.1.<init>(android.hardware.camera2.legacy.LegacyFaceDetectMapper):void, dex: 
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
        AnonymousClass1(android.hardware.camera2.legacy.LegacyFaceDetectMapper r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.1.<init>(android.hardware.camera2.legacy.LegacyFaceDetectMapper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.1.<init>(android.hardware.camera2.legacy.LegacyFaceDetectMapper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.1.onFaceDetection(android.hardware.Camera$Face[], android.hardware.Camera):void, dex: 
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
        public void onFaceDetection(android.hardware.Camera.Face[] r1, android.hardware.Camera r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.1.onFaceDetection(android.hardware.Camera$Face[], android.hardware.Camera):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.1.onFaceDetection(android.hardware.Camera$Face[], android.hardware.Camera):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get2(android.hardware.camera2.legacy.LegacyFaceDetectMapper):boolean, dex:  in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get2(android.hardware.camera2.legacy.LegacyFaceDetectMapper):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get2(android.hardware.camera2.legacy.LegacyFaceDetectMapper):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get2 */
    static /* synthetic */ boolean m237-get2(android.hardware.camera2.legacy.LegacyFaceDetectMapper r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get2(android.hardware.camera2.legacy.LegacyFaceDetectMapper):boolean, dex:  in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get2(android.hardware.camera2.legacy.LegacyFaceDetectMapper):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get2(android.hardware.camera2.legacy.LegacyFaceDetectMapper):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get3(android.hardware.camera2.legacy.LegacyFaceDetectMapper):java.lang.Object, dex:  in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get3(android.hardware.camera2.legacy.LegacyFaceDetectMapper):java.lang.Object, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get3(android.hardware.camera2.legacy.LegacyFaceDetectMapper):java.lang.Object, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get3 */
    static /* synthetic */ java.lang.Object m238-get3(android.hardware.camera2.legacy.LegacyFaceDetectMapper r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get3(android.hardware.camera2.legacy.LegacyFaceDetectMapper):java.lang.Object, dex:  in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get3(android.hardware.camera2.legacy.LegacyFaceDetectMapper):java.lang.Object, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-get3(android.hardware.camera2.legacy.LegacyFaceDetectMapper):java.lang.Object");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-set0(android.hardware.camera2.legacy.LegacyFaceDetectMapper, android.hardware.Camera$Face[]):android.hardware.Camera$Face[], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -set0 */
    static /* synthetic */ android.hardware.Camera.Face[] m239-set0(android.hardware.camera2.legacy.LegacyFaceDetectMapper r1, android.hardware.Camera.Face[] r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-set0(android.hardware.camera2.legacy.LegacyFaceDetectMapper, android.hardware.Camera$Face[]):android.hardware.Camera$Face[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.-set0(android.hardware.camera2.legacy.LegacyFaceDetectMapper, android.hardware.Camera$Face[]):android.hardware.Camera$Face[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.<init>(android.hardware.Camera, android.hardware.camera2.CameraCharacteristics):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public LegacyFaceDetectMapper(android.hardware.Camera r1, android.hardware.camera2.CameraCharacteristics r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.<init>(android.hardware.Camera, android.hardware.camera2.CameraCharacteristics):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.<init>(android.hardware.Camera, android.hardware.camera2.CameraCharacteristics):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.mapResultFaces(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.legacy.LegacyRequest):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void mapResultFaces(android.hardware.camera2.impl.CameraMetadataNative r1, android.hardware.camera2.legacy.LegacyRequest r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.mapResultFaces(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.legacy.LegacyRequest):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.mapResultFaces(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.legacy.LegacyRequest):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.processFaceDetectMode(android.hardware.camera2.CaptureRequest, android.hardware.Camera$Parameters):void, dex: 
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
    public void processFaceDetectMode(android.hardware.camera2.CaptureRequest r1, android.hardware.Camera.Parameters r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyFaceDetectMapper.processFaceDetectMode(android.hardware.camera2.CaptureRequest, android.hardware.Camera$Parameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFaceDetectMapper.processFaceDetectMode(android.hardware.camera2.CaptureRequest, android.hardware.Camera$Parameters):void");
    }
}
