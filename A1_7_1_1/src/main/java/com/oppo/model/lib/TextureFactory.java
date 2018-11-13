package com.oppo.model.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES10;
import android.util.Log;
import javax.microedition.khronos.opengles.GL10;

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
public class TextureFactory {
    private static final boolean LOGD = false;
    private static final String TAG = "TextureFactory";
    private static final int TEX_DEFAULT_BORDER = 0;
    private static final int TEX_DEFAULT_LEVEL = 0;
    private static final int TEX_ID_ARRAR_LEN = 1;
    private static final int TEX_ID_BASE_VALUE = 0;
    private static final int TEX_ID_INDEX = 0;
    private static final int TEX_ID_INVALID_VALUE = -1;
    private static final int TEX_ID_OFFSET = 0;
    private static final int TEX_SIZE_INVALID = 0;
    private static final int TEX_SIZE_MAX = 1024;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.model.lib.TextureFactory.<init>():void, dex: 
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
    public TextureFactory() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.model.lib.TextureFactory.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.createImage(android.content.Context, int):android.graphics.Bitmap, dex: 
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
    private static android.graphics.Bitmap createImage(android.content.Context r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.createImage(android.content.Context, int):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.createImage(android.content.Context, int):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.createImage(android.content.Context, java.lang.String):android.graphics.Bitmap, dex: 
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
    private static android.graphics.Bitmap createImage(android.content.Context r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.createImage(android.content.Context, java.lang.String):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.createImage(android.content.Context, java.lang.String):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.createScaledBitmap(android.graphics.Bitmap):android.graphics.Bitmap, dex: 
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
    private static android.graphics.Bitmap createScaledBitmap(android.graphics.Bitmap r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.createScaledBitmap(android.graphics.Bitmap):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.createScaledBitmap(android.graphics.Bitmap):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.createTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, android.graphics.Bitmap, int, int, int):int, dex: 
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
    private static int createTexture(android.content.Context r1, javax.microedition.khronos.opengles.GL10 r2, android.graphics.Bitmap r3, int r4, int r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.createTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, android.graphics.Bitmap, int, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.createTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, android.graphics.Bitmap, int, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.getTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, int, int, int):int, dex: 
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
    public static int getTexture(android.content.Context r1, javax.microedition.khronos.opengles.GL10 r2, int r3, int r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.getTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, int, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.getTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, int, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.getTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, java.lang.String, int, int):int, dex: 
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
    public static int getTexture(android.content.Context r1, javax.microedition.khronos.opengles.GL10 r2, java.lang.String r3, int r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.getTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, java.lang.String, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.getTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, java.lang.String, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.getTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, java.lang.String, int, int, int):int, dex: 
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
    public static int getTexture(android.content.Context r1, javax.microedition.khronos.opengles.GL10 r2, java.lang.String r3, int r4, int r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.getTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, java.lang.String, int, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.getTexture(android.content.Context, javax.microedition.khronos.opengles.GL10, java.lang.String, int, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.model.lib.TextureFactory.hasTexturePadding(android.graphics.Rect):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static boolean hasTexturePadding(android.graphics.Rect r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.model.lib.TextureFactory.hasTexturePadding(android.graphics.Rect):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.hasTexturePadding(android.graphics.Rect):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.modifyBitmapSize(int):int, dex: 
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
    private static int modifyBitmapSize(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.modifyBitmapSize(int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.modifyBitmapSize(int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.modifyTexture(android.graphics.Bitmap, android.graphics.Rect):android.graphics.Bitmap, dex: 
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
    public static android.graphics.Bitmap modifyTexture(android.graphics.Bitmap r1, android.graphics.Rect r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.modifyTexture(android.graphics.Bitmap, android.graphics.Rect):android.graphics.Bitmap, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.modifyTexture(android.graphics.Bitmap, android.graphics.Rect):android.graphics.Bitmap");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.needResize(android.content.Context, android.graphics.Bitmap, android.graphics.Rect):boolean, dex: 
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
    public static boolean needResize(android.content.Context r1, android.graphics.Bitmap r2, android.graphics.Rect r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.model.lib.TextureFactory.needResize(android.content.Context, android.graphics.Bitmap, android.graphics.Rect):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.model.lib.TextureFactory.needResize(android.content.Context, android.graphics.Bitmap, android.graphics.Rect):boolean");
    }

    public static int getTexture(Context context, GL10 gl, String textureName) {
        return getTexture(context, gl, textureName, 33071, 33071);
    }

    public static int getTexture(Context context, GL10 gl, String textureName, int texEnvMode) {
        return getTexture(context, gl, textureName, 33071, 33071, texEnvMode);
    }

    public static int getTexture(Context context, GL10 gl, int idx) {
        return getTexture(context, gl, idx, 33071, 33071);
    }

    public static int getTexture(Context context, GL10 gl, Bitmap bitmap) {
        return getTexture(context, gl, bitmap, 33071, 33071);
    }

    public static int getTexture(Context context, GL10 gl, Bitmap bitmap, int wrap_s_mode, int wrap_t_mode) {
        return createTexture(context, gl, bitmap, wrap_s_mode, wrap_t_mode, 7681);
    }

    public static int getTexture(Context context, GL10 gl, Bitmap bitmap, int texEnvMode) {
        return getTexture(context, gl, bitmap, 33071, 33071, texEnvMode);
    }

    public static int getTexture(Context context, GL10 gl, Bitmap bitmap, int wrap_s_mode, int wrap_t_mode, int texEnvMode) {
        return createTexture(context, gl, bitmap, wrap_s_mode, wrap_t_mode, texEnvMode);
    }

    public static boolean deleteTexture(GL10 gl, int textureId) {
        if (gl == null) {
            Log.e(TAG, "deleteTexture() gl null error.");
            return LOGD;
        } else if (textureId <= 0) {
            return LOGD;
        } else {
            gl.glDeleteTextures(1, new int[]{textureId}, 0);
            return true;
        }
    }

    public static boolean deleteTexture(int textureId) {
        if (textureId <= 0) {
            return LOGD;
        }
        GLES10.glDeleteTextures(1, new int[]{textureId}, 0);
        return true;
    }

    private static boolean needResize(int width, int height) {
        if (ModelUtils.isPowerOf2(width) && ModelUtils.isPowerOf2(height)) {
            return LOGD;
        }
        return true;
    }
}
