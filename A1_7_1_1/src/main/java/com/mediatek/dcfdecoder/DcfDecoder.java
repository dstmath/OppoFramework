package com.mediatek.dcfdecoder;

import android.util.Log;
import java.io.FileDescriptor;

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
public class DcfDecoder {
    private static final int ACTION_DECODE_FULL_IMAGE = 0;
    private static final int ACTION_JUST_DECODE_BOUND = 1;
    private static final int ACTION_JUST_DECODE_THUMBNAIL = 2;
    private static final int DECODE_THUMBNAIL_FLAG = 256;
    private static final int HEADER_BUFFER_SIZE = 128;
    private static final String TAG = "DRM/DcfDecoder";
    private static final int THUMBNAIL_TARGET_SIZE = 96;
    private static boolean sIsOmaDrmEnabled;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.dcfdecoder.DcfDecoder.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.dcfdecoder.DcfDecoder.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.dcfdecoder.DcfDecoder.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.dcfdecoder.DcfDecoder.<init>():void, dex: 
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
    public DcfDecoder() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.dcfdecoder.DcfDecoder.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.dcfdecoder.DcfDecoder.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImage(java.io.FileDescriptor, int, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private static android.graphics.Bitmap decodeDrmImage(java.io.FileDescriptor r1, int r2, android.graphics.BitmapFactory.Options r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImage(java.io.FileDescriptor, int, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImage(java.io.FileDescriptor, int, android.graphics.BitmapFactory$Options):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImageIfNeeded(java.io.FileDescriptor, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public static android.graphics.Bitmap decodeDrmImageIfNeeded(java.io.FileDescriptor r1, android.graphics.BitmapFactory.Options r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImageIfNeeded(java.io.FileDescriptor, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImageIfNeeded(java.io.FileDescriptor, android.graphics.BitmapFactory$Options):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImageIfNeeded(byte[], android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public static android.graphics.Bitmap decodeDrmImageIfNeeded(byte[] r1, android.graphics.BitmapFactory.Options r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImageIfNeeded(byte[], android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImageIfNeeded(byte[], android.graphics.BitmapFactory$Options):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImageIfNeeded(byte[], java.io.InputStream, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public static android.graphics.Bitmap decodeDrmImageIfNeeded(byte[] r1, java.io.InputStream r2, android.graphics.BitmapFactory.Options r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImageIfNeeded(byte[], java.io.InputStream, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.dcfdecoder.DcfDecoder.decodeDrmImageIfNeeded(byte[], java.io.InputStream, android.graphics.BitmapFactory$Options):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.dcfdecoder.DcfDecoder.isDrmFile(byte[]):boolean, dex: 
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
    private static boolean isDrmFile(byte[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.dcfdecoder.DcfDecoder.isDrmFile(byte[]):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.dcfdecoder.DcfDecoder.isDrmFile(byte[]):boolean");
    }

    private static native byte[] nativeDecryptDcfFile(FileDescriptor fileDescriptor, int i, int i2);

    private native byte[] nativeForceDecryptFile(String str, boolean z);

    public byte[] forceDecryptFile(String pathName, boolean consume) {
        if (pathName != null) {
            return nativeForceDecryptFile(pathName, consume);
        }
        Log.e(TAG, "forceDecryptFile: find null file name!");
        return null;
    }
}
