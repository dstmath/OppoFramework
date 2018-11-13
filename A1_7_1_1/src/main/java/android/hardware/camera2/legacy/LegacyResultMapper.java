package android.hardware.camera2.legacy;

import android.hardware.camera2.impl.CameraMetadataNative;

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
public class LegacyResultMapper {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_DUMP = false;
    private static final String TAG = "LegacyResultMapper";
    private LegacyRequest mCachedRequest;
    private CameraMetadataNative mCachedResult;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.camera2.legacy.LegacyResultMapper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.camera2.legacy.LegacyResultMapper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyResultMapper.<init>():void, dex: 
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
    public LegacyResultMapper() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyResultMapper.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.convertLegacyAfMode(java.lang.String):int, dex: 
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
    private static int convertLegacyAfMode(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.convertLegacyAfMode(java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.convertLegacyAfMode(java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.convertLegacyAwbMode(java.lang.String):int, dex: 
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
    private static int convertLegacyAwbMode(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.convertLegacyAwbMode(java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.convertLegacyAwbMode(java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyResultMapper.convertResultMetadata(android.hardware.camera2.legacy.LegacyRequest):android.hardware.camera2.impl.CameraMetadataNative, dex: 
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
    private static android.hardware.camera2.impl.CameraMetadataNative convertResultMetadata(android.hardware.camera2.legacy.LegacyRequest r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyResultMapper.convertResultMetadata(android.hardware.camera2.legacy.LegacyRequest):android.hardware.camera2.impl.CameraMetadataNative, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.convertResultMetadata(android.hardware.camera2.legacy.LegacyRequest):android.hardware.camera2.impl.CameraMetadataNative");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.getMeteringRectangles(android.graphics.Rect, android.hardware.camera2.legacy.ParameterUtils$ZoomData, java.util.List, java.lang.String):android.hardware.camera2.params.MeteringRectangle[], dex: 
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
    private static android.hardware.camera2.params.MeteringRectangle[] getMeteringRectangles(android.graphics.Rect r1, android.hardware.camera2.legacy.ParameterUtils.ZoomData r2, java.util.List<android.hardware.Camera.Area> r3, java.lang.String r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.getMeteringRectangles(android.graphics.Rect, android.hardware.camera2.legacy.ParameterUtils$ZoomData, java.util.List, java.lang.String):android.hardware.camera2.params.MeteringRectangle[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.getMeteringRectangles(android.graphics.Rect, android.hardware.camera2.legacy.ParameterUtils$ZoomData, java.util.List, java.lang.String):android.hardware.camera2.params.MeteringRectangle[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapAe(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.CameraCharacteristics, android.hardware.camera2.CaptureRequest, android.graphics.Rect, android.hardware.camera2.legacy.ParameterUtils$ZoomData, android.hardware.Camera$Parameters):void, dex: 
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
    private static void mapAe(android.hardware.camera2.impl.CameraMetadataNative r1, android.hardware.camera2.CameraCharacteristics r2, android.hardware.camera2.CaptureRequest r3, android.graphics.Rect r4, android.hardware.camera2.legacy.ParameterUtils.ZoomData r5, android.hardware.Camera.Parameters r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapAe(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.CameraCharacteristics, android.hardware.camera2.CaptureRequest, android.graphics.Rect, android.hardware.camera2.legacy.ParameterUtils$ZoomData, android.hardware.Camera$Parameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.mapAe(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.CameraCharacteristics, android.hardware.camera2.CaptureRequest, android.graphics.Rect, android.hardware.camera2.legacy.ParameterUtils$ZoomData, android.hardware.Camera$Parameters):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapAeAndFlashMode(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.CameraCharacteristics, android.hardware.Camera$Parameters):void, dex: 
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
    private static void mapAeAndFlashMode(android.hardware.camera2.impl.CameraMetadataNative r1, android.hardware.camera2.CameraCharacteristics r2, android.hardware.Camera.Parameters r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapAeAndFlashMode(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.CameraCharacteristics, android.hardware.Camera$Parameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.mapAeAndFlashMode(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.CameraCharacteristics, android.hardware.Camera$Parameters):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapAf(android.hardware.camera2.impl.CameraMetadataNative, android.graphics.Rect, android.hardware.camera2.legacy.ParameterUtils$ZoomData, android.hardware.Camera$Parameters):void, dex: 
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
    private static void mapAf(android.hardware.camera2.impl.CameraMetadataNative r1, android.graphics.Rect r2, android.hardware.camera2.legacy.ParameterUtils.ZoomData r3, android.hardware.Camera.Parameters r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapAf(android.hardware.camera2.impl.CameraMetadataNative, android.graphics.Rect, android.hardware.camera2.legacy.ParameterUtils$ZoomData, android.hardware.Camera$Parameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.mapAf(android.hardware.camera2.impl.CameraMetadataNative, android.graphics.Rect, android.hardware.camera2.legacy.ParameterUtils$ZoomData, android.hardware.Camera$Parameters):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapAwb(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.Camera$Parameters):void, dex: 
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
    private static void mapAwb(android.hardware.camera2.impl.CameraMetadataNative r1, android.hardware.Camera.Parameters r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapAwb(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.Camera$Parameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.mapAwb(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.Camera$Parameters):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapScaler(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.legacy.ParameterUtils$ZoomData, android.hardware.Camera$Parameters):void, dex: 
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
    private static void mapScaler(android.hardware.camera2.impl.CameraMetadataNative r1, android.hardware.camera2.legacy.ParameterUtils.ZoomData r2, android.hardware.Camera.Parameters r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyResultMapper.mapScaler(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.legacy.ParameterUtils$ZoomData, android.hardware.Camera$Parameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.mapScaler(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.legacy.ParameterUtils$ZoomData, android.hardware.Camera$Parameters):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 6 in method: android.hardware.camera2.legacy.LegacyResultMapper.cachedConvertResultMetadata(android.hardware.camera2.legacy.LegacyRequest, long):android.hardware.camera2.impl.CameraMetadataNative, dex:  in method: android.hardware.camera2.legacy.LegacyResultMapper.cachedConvertResultMetadata(android.hardware.camera2.legacy.LegacyRequest, long):android.hardware.camera2.impl.CameraMetadataNative, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 6 in method: android.hardware.camera2.legacy.LegacyResultMapper.cachedConvertResultMetadata(android.hardware.camera2.legacy.LegacyRequest, long):android.hardware.camera2.impl.CameraMetadataNative, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: 6
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public android.hardware.camera2.impl.CameraMetadataNative cachedConvertResultMetadata(android.hardware.camera2.legacy.LegacyRequest r1, long r2) {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: 6 in method: android.hardware.camera2.legacy.LegacyResultMapper.cachedConvertResultMetadata(android.hardware.camera2.legacy.LegacyRequest, long):android.hardware.camera2.impl.CameraMetadataNative, dex:  in method: android.hardware.camera2.legacy.LegacyResultMapper.cachedConvertResultMetadata(android.hardware.camera2.legacy.LegacyRequest, long):android.hardware.camera2.impl.CameraMetadataNative, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyResultMapper.cachedConvertResultMetadata(android.hardware.camera2.legacy.LegacyRequest, long):android.hardware.camera2.impl.CameraMetadataNative");
    }
}
