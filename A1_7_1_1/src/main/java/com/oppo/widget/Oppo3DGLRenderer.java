package com.oppo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView.Renderer;
import com.oppo.model.lib.Matrix4f;
import com.oppo.model.lib.Vector3f;
import com.oppo.model.md2.MdModel;
import com.oppo.model.ms3d.MsModel;

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
public class Oppo3DGLRenderer implements Renderer {
    protected static final int CLOCKS_PER_SEC = 1000;
    private static final boolean LOGD = false;
    private static final String TAG = "Oppo3DGLRenderer";
    protected static final float[] mAmbient = null;
    protected static final float[] mDiffuse = null;
    protected static final float[] mEmission = null;
    protected static final float mShininess = 0.0f;
    protected static final float[] mSpecular = null;
    private int TEXTURE_BUFFER_LEN;
    protected Context mContext;
    protected boolean mEnableTexFlag;
    protected float[] mLightPos;
    protected Matrix4f mMatModel;
    protected Matrix4f mMatProject;
    protected Matrix4f mMatView;
    protected float[] mMatrixProjectArray;
    protected float mModelDistance;
    protected float mModelHeight;
    protected float mModelWidth;
    protected boolean mNeedUpdateTex;
    protected Oppo3DGLView mOppo3DGLView;
    protected Vector3f mSphereCenter;
    protected TextureInfoMap[] mTextureInfoBuffer;
    private int mTextureInfoBufferIndex;
    protected UpdateTexMap[] mUpdateTexBuffer;
    protected int[] mViewport;
    protected Vector3f mvCenter;
    protected Vector3f mvEye;

    protected class TextureInfoMap {
        public int index;
        public int textureId;
        final /* synthetic */ Oppo3DGLRenderer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.widget.Oppo3DGLRenderer.TextureInfoMap.<init>(com.oppo.widget.Oppo3DGLRenderer):void, dex: 
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
        protected TextureInfoMap(com.oppo.widget.Oppo3DGLRenderer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.widget.Oppo3DGLRenderer.TextureInfoMap.<init>(com.oppo.widget.Oppo3DGLRenderer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.TextureInfoMap.<init>(com.oppo.widget.Oppo3DGLRenderer):void");
        }
    }

    protected class UpdateTexMap {
        public Bitmap bitmap;
        public int index;
        final /* synthetic */ Oppo3DGLRenderer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.widget.Oppo3DGLRenderer.UpdateTexMap.<init>(com.oppo.widget.Oppo3DGLRenderer):void, dex: 
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
        protected UpdateTexMap(com.oppo.widget.Oppo3DGLRenderer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.widget.Oppo3DGLRenderer.UpdateTexMap.<init>(com.oppo.widget.Oppo3DGLRenderer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.UpdateTexMap.<init>(com.oppo.widget.Oppo3DGLRenderer):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.oppo.widget.Oppo3DGLRenderer.<init>(android.content.Context, com.oppo.widget.Oppo3DGLView):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public Oppo3DGLRenderer(android.content.Context r1, com.oppo.widget.Oppo3DGLView r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.oppo.widget.Oppo3DGLRenderer.<init>(android.content.Context, com.oppo.widget.Oppo3DGLView):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.<init>(android.content.Context, com.oppo.widget.Oppo3DGLView):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.LoadMdModel(android.content.Context, java.lang.String, boolean, boolean, boolean, boolean, int):com.oppo.model.md2.MdModel, dex: 
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
    public static com.oppo.model.md2.MdModel LoadMdModel(android.content.Context r1, java.lang.String r2, boolean r3, boolean r4, boolean r5, boolean r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.LoadMdModel(android.content.Context, java.lang.String, boolean, boolean, boolean, boolean, int):com.oppo.model.md2.MdModel, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.LoadMdModel(android.content.Context, java.lang.String, boolean, boolean, boolean, boolean, int):com.oppo.model.md2.MdModel");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.LoadMsModel(android.content.Context, java.lang.String, boolean):com.oppo.model.ms3d.MsModel, dex: 
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
    public static com.oppo.model.ms3d.MsModel LoadMsModel(android.content.Context r1, java.lang.String r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.LoadMsModel(android.content.Context, java.lang.String, boolean):com.oppo.model.ms3d.MsModel, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.LoadMsModel(android.content.Context, java.lang.String, boolean):com.oppo.model.ms3d.MsModel");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.ReleaseModel(com.oppo.model.md2.MdModel):void, dex: 
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
    public static void ReleaseModel(com.oppo.model.md2.MdModel r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.ReleaseModel(com.oppo.model.md2.MdModel):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.ReleaseModel(com.oppo.model.md2.MdModel):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.ReleaseModel(com.oppo.model.ms3d.MsModel):void, dex: 
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
    public static void ReleaseModel(com.oppo.model.ms3d.MsModel r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.ReleaseModel(com.oppo.model.ms3d.MsModel):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.ReleaseModel(com.oppo.model.ms3d.MsModel):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.widget.Oppo3DGLRenderer.clearUpdateTexBuffer():void, dex:  in method: com.oppo.widget.Oppo3DGLRenderer.clearUpdateTexBuffer():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.widget.Oppo3DGLRenderer.clearUpdateTexBuffer():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:752)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void clearUpdateTexBuffer() {
        /*
        // Can't load method instructions: Load method exception: null in method: com.oppo.widget.Oppo3DGLRenderer.clearUpdateTexBuffer():void, dex:  in method: com.oppo.widget.Oppo3DGLRenderer.clearUpdateTexBuffer():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.clearUpdateTexBuffer():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.widget.Oppo3DGLRenderer.findTextureInfo(int):int, dex: 
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
    private int findTextureInfo(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.widget.Oppo3DGLRenderer.findTextureInfo(int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.findTextureInfo(int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.insertTextureInfo(javax.microedition.khronos.opengles.GL10, int, android.graphics.Bitmap):int, dex: 
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
    private int insertTextureInfo(javax.microedition.khronos.opengles.GL10 r1, int r2, android.graphics.Bitmap r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.insertTextureInfo(javax.microedition.khronos.opengles.GL10, int, android.graphics.Bitmap):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.insertTextureInfo(javax.microedition.khronos.opengles.GL10, int, android.graphics.Bitmap):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.widget.Oppo3DGLRenderer.insertUpdateTexInfo(int, android.graphics.Bitmap):void, dex: 
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
    private void insertUpdateTexInfo(int r1, android.graphics.Bitmap r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.widget.Oppo3DGLRenderer.insertUpdateTexInfo(int, android.graphics.Bitmap):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.insertUpdateTexInfo(int, android.graphics.Bitmap):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.widget.Oppo3DGLRenderer.updateTextureBuffer(javax.microedition.khronos.opengles.GL10):void, dex:  in method: com.oppo.widget.Oppo3DGLRenderer.updateTextureBuffer(javax.microedition.khronos.opengles.GL10):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.widget.Oppo3DGLRenderer.updateTextureBuffer(javax.microedition.khronos.opengles.GL10):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void updateTextureBuffer(javax.microedition.khronos.opengles.GL10 r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.oppo.widget.Oppo3DGLRenderer.updateTextureBuffer(javax.microedition.khronos.opengles.GL10):void, dex:  in method: com.oppo.widget.Oppo3DGLRenderer.updateTextureBuffer(javax.microedition.khronos.opengles.GL10):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.updateTextureBuffer(javax.microedition.khronos.opengles.GL10):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.bindMaterial(javax.microedition.khronos.opengles.GL10, com.oppo.model.ms3d.MsModel, int):void, dex: 
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
    protected void bindMaterial(javax.microedition.khronos.opengles.GL10 r1, com.oppo.model.ms3d.MsModel r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.bindMaterial(javax.microedition.khronos.opengles.GL10, com.oppo.model.ms3d.MsModel, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.bindMaterial(javax.microedition.khronos.opengles.GL10, com.oppo.model.ms3d.MsModel, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.widget.Oppo3DGLRenderer.clearTextureInfo(int):void, dex: 
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
    public void clearTextureInfo(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.widget.Oppo3DGLRenderer.clearTextureInfo(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.clearTextureInfo(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.widget.Oppo3DGLRenderer.clearTextureInfoBuffer():void, dex: 
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
    public void clearTextureInfoBuffer() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.widget.Oppo3DGLRenderer.clearTextureInfoBuffer():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.clearTextureInfoBuffer():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.oppo.widget.Oppo3DGLRenderer.disableClientState(javax.microedition.khronos.opengles.GL10):void, dex: 
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
    protected void disableClientState(javax.microedition.khronos.opengles.GL10 r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.oppo.widget.Oppo3DGLRenderer.disableClientState(javax.microedition.khronos.opengles.GL10):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.disableClientState(javax.microedition.khronos.opengles.GL10):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.widget.Oppo3DGLRenderer.disableTexture(javax.microedition.khronos.opengles.GL10):void, dex: 
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
    protected void disableTexture(javax.microedition.khronos.opengles.GL10 r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.widget.Oppo3DGLRenderer.disableTexture(javax.microedition.khronos.opengles.GL10):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.disableTexture(javax.microedition.khronos.opengles.GL10):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.drawArraysData(javax.microedition.khronos.opengles.GL10, boolean, int, java.nio.FloatBuffer, java.nio.FloatBuffer):void, dex: 
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
    protected void drawArraysData(javax.microedition.khronos.opengles.GL10 r1, boolean r2, int r3, java.nio.FloatBuffer r4, java.nio.FloatBuffer r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.drawArraysData(javax.microedition.khronos.opengles.GL10, boolean, int, java.nio.FloatBuffer, java.nio.FloatBuffer):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.drawArraysData(javax.microedition.khronos.opengles.GL10, boolean, int, java.nio.FloatBuffer, java.nio.FloatBuffer):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.enableLighting(javax.microedition.khronos.opengles.GL10, boolean):void, dex: 
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
    protected void enableLighting(javax.microedition.khronos.opengles.GL10 r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.enableLighting(javax.microedition.khronos.opengles.GL10, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.enableLighting(javax.microedition.khronos.opengles.GL10, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.enableTexture(javax.microedition.khronos.opengles.GL10, int, int, java.nio.FloatBuffer):void, dex: 
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
    protected void enableTexture(javax.microedition.khronos.opengles.GL10 r1, int r2, int r3, java.nio.FloatBuffer r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.enableTexture(javax.microedition.khronos.opengles.GL10, int, int, java.nio.FloatBuffer):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.enableTexture(javax.microedition.khronos.opengles.GL10, int, int, java.nio.FloatBuffer):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.getTextureId(javax.microedition.khronos.opengles.GL10, int):int, dex: 
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
    protected int getTextureId(javax.microedition.khronos.opengles.GL10 r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.getTextureId(javax.microedition.khronos.opengles.GL10, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.getTextureId(javax.microedition.khronos.opengles.GL10, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.initTextureInfo(com.oppo.model.ms3d.MsModel, java.lang.String[], int[]):boolean, dex: 
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
    protected boolean initTextureInfo(com.oppo.model.ms3d.MsModel r1, java.lang.String[] r2, int[] r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.initTextureInfo(com.oppo.model.ms3d.MsModel, java.lang.String[], int[]):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.initTextureInfo(com.oppo.model.ms3d.MsModel, java.lang.String[], int[]):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.onDetachedFromWindow():void, dex: 
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
    public void onDetachedFromWindow() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.onDetachedFromWindow():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.onDetachedFromWindow():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.onDrawFrame(javax.microedition.khronos.opengles.GL10):void, dex: 
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
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.onDrawFrame(javax.microedition.khronos.opengles.GL10):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.onDrawFrame(javax.microedition.khronos.opengles.GL10):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int):void, dex: 
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
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.widget.Oppo3DGLRenderer.onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig):void, dex:  in method: com.oppo.widget.Oppo3DGLRenderer.onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.widget.Oppo3DGLRenderer.onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 r1, javax.microedition.khronos.egl.EGLConfig r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.oppo.widget.Oppo3DGLRenderer.onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig):void, dex:  in method: com.oppo.widget.Oppo3DGLRenderer.onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.oppo.widget.Oppo3DGLRenderer.setTextureBufferSize(int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setTextureBufferSize(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.oppo.widget.Oppo3DGLRenderer.setTextureBufferSize(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.setTextureBufferSize(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.setUpCamera(javax.microedition.khronos.opengles.GL10):void, dex: 
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
    protected void setUpCamera(javax.microedition.khronos.opengles.GL10 r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.widget.Oppo3DGLRenderer.setUpCamera(javax.microedition.khronos.opengles.GL10):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.setUpCamera(javax.microedition.khronos.opengles.GL10):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.updateRenderBuffer(com.oppo.model.ms3d.MsModel, float, boolean, float, float):void, dex: 
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
    protected void updateRenderBuffer(com.oppo.model.ms3d.MsModel r1, float r2, boolean r3, float r4, float r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.Oppo3DGLRenderer.updateRenderBuffer(com.oppo.model.ms3d.MsModel, float, boolean, float, float):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.updateRenderBuffer(com.oppo.model.ms3d.MsModel, float, boolean, float, float):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.updateTexture(int, android.graphics.Bitmap):void, dex: 
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
    public void updateTexture(int r1, android.graphics.Bitmap r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.Oppo3DGLRenderer.updateTexture(int, android.graphics.Bitmap):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.Oppo3DGLRenderer.updateTexture(int, android.graphics.Bitmap):void");
    }

    public static MdModel LoadMdModel(Context context, String modelName, boolean reverseNormal) {
        return LoadMdModel(context, modelName, reverseNormal, LOGD, LOGD, LOGD);
    }

    public static MdModel LoadMdModel(Context context, String modelName, boolean reverseNormal, boolean enableBack, boolean enableRegion, boolean enableShadow) {
        return LoadMdModel(context, modelName, reverseNormal, enableBack, enableRegion, enableShadow, 0);
    }

    public static MsModel LoadMsModel(Context context, String modelName) {
        return LoadMsModel(context, modelName, LOGD);
    }
}
