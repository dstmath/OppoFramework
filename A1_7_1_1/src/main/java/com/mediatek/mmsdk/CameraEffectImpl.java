package com.mediatek.mmsdk;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import com.mediatek.mmsdk.CameraEffect.StateCallback;
import com.mediatek.mmsdk.IEffectListener.Stub;

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
public class CameraEffectImpl extends CameraEffect {
    private static final boolean DEBUG = true;
    private static final int SUCCESS_VALUE = 0;
    private static final String TAG = "CameraEffectImpl";
    private BaseParameters mBaseParameters;
    private final Runnable mCallOnActive;
    private final Runnable mCallOnBusy;
    private final Runnable mCallOnClosed;
    private final Runnable mCallOnIdle;
    private SparseArray<CaptureCallbackHolder> mCaptureCallbackHolderMap;
    private CameraEffectSessionImpl mCurrentSession;
    private long mCurrentStartId;
    private Handler mEffectHalHandler;
    private CameraEffectStatus mEffectHalStatus;
    private StateCallback mEffectStateCallback;
    private IEffectHalClient mIEffectHalClient;
    private boolean mInError;
    private final Object mInterfaceLock;
    private DeviceStateCallback mSessionStateCallback;

    /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ CameraEffectImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.1.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
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
        AnonymousClass1(com.mediatek.mmsdk.CameraEffectImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.1.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.1.<init>(com.mediatek.mmsdk.CameraEffectImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.1.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.1.run():void");
        }
    }

    /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ CameraEffectImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.2.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
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
        AnonymousClass2(com.mediatek.mmsdk.CameraEffectImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.2.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.2.<init>(com.mediatek.mmsdk.CameraEffectImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.2.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.2.run():void");
        }
    }

    /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$3 */
    class AnonymousClass3 implements Runnable {
        private boolean isClosedOnce;
        final /* synthetic */ CameraEffectImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.3.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
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
        AnonymousClass3(com.mediatek.mmsdk.CameraEffectImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.3.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.3.<init>(com.mediatek.mmsdk.CameraEffectImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectImpl.3.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectImpl.3.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.3.run():void");
        }
    }

    /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ CameraEffectImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.4.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
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
        AnonymousClass4(com.mediatek.mmsdk.CameraEffectImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.4.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.4.<init>(com.mediatek.mmsdk.CameraEffectImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.4.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.4.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.4.run():void");
        }
    }

    /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ CameraEffectImpl this$0;
        final /* synthetic */ int val$code;
        final /* synthetic */ boolean val$isError;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.5.<init>(com.mediatek.mmsdk.CameraEffectImpl, boolean, int):void, dex: 
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
        AnonymousClass5(com.mediatek.mmsdk.CameraEffectImpl r1, boolean r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.5.<init>(com.mediatek.mmsdk.CameraEffectImpl, boolean, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.5.<init>(com.mediatek.mmsdk.CameraEffectImpl, boolean, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectImpl.5.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectImpl.5.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.5.run():void");
        }
    }

    public static abstract class CaptureCallback {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.<init>():void, dex: 
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
        public CaptureCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onCaptureFailed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onCaptureFailed(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onCaptureFailed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onCaptureFailed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onCaptureSequenceAborted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onCaptureSequenceAborted(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onCaptureSequenceAborted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onCaptureSequenceAborted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onCaptureSequenceCompleted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, long):void, dex: 
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
        public void onCaptureSequenceCompleted(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2, long r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onCaptureSequenceCompleted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onCaptureSequenceCompleted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onInputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onInputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2, com.mediatek.mmsdk.BaseParameters r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onInputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onInputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onOutputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onOutputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2, com.mediatek.mmsdk.BaseParameters r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onOutputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback.onOutputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void");
        }
    }

    private class CaptureCallbackHolder {
        private final CaptureCallback mCaptureCallback;
        private final Handler mHandler;
        final /* synthetic */ CameraEffectImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder.<init>(com.mediatek.mmsdk.CameraEffectImpl, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback, android.os.Handler):void, dex: 
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
        CaptureCallbackHolder(com.mediatek.mmsdk.CameraEffectImpl r1, com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback r2, android.os.Handler r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder.<init>(com.mediatek.mmsdk.CameraEffectImpl, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback, android.os.Handler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder.<init>(com.mediatek.mmsdk.CameraEffectImpl, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback, android.os.Handler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder.getCaptureCallback():com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback, dex: 
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
        public com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback getCaptureCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder.getCaptureCallback():com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder.getCaptureCallback():com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder.getHandler():android.os.Handler, dex: 
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
        public android.os.Handler getHandler() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder.getHandler():android.os.Handler, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder.getHandler():android.os.Handler");
        }
    }

    public static abstract class DeviceStateCallback extends StateCallback {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.<init>():void, dex: 
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
        public DeviceStateCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onActive(com.mediatek.mmsdk.CameraEffect):void, dex: 
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
        public void onActive(com.mediatek.mmsdk.CameraEffect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onActive(com.mediatek.mmsdk.CameraEffect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onActive(com.mediatek.mmsdk.CameraEffect):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onBusy(com.mediatek.mmsdk.CameraEffect):void, dex: 
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
        public void onBusy(com.mediatek.mmsdk.CameraEffect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onBusy(com.mediatek.mmsdk.CameraEffect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onBusy(com.mediatek.mmsdk.CameraEffect):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onIdle(com.mediatek.mmsdk.CameraEffect):void, dex: 
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
        public void onIdle(com.mediatek.mmsdk.CameraEffect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onIdle(com.mediatek.mmsdk.CameraEffect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onIdle(com.mediatek.mmsdk.CameraEffect):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onUnconfigured(com.mediatek.mmsdk.CameraEffect):void, dex: 
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
        public void onUnconfigured(com.mediatek.mmsdk.CameraEffect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onUnconfigured(com.mediatek.mmsdk.CameraEffect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback.onUnconfigured(com.mediatek.mmsdk.CameraEffect):void");
        }
    }

    public class EffectHalClientListener extends Stub {
        final /* synthetic */ CameraEffectImpl this$0;

        /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ EffectHalClientListener this$1;
            final /* synthetic */ CaptureCallbackHolder val$callbackHolder;
            final /* synthetic */ BaseParameters val$parameters;
            final /* synthetic */ BaseParameters val$result;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.1.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener r1, com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder r2, com.mediatek.mmsdk.BaseParameters r3, com.mediatek.mmsdk.BaseParameters r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.1.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.1.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.1.run():void");
            }
        }

        /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ EffectHalClientListener this$1;
            final /* synthetic */ CaptureCallbackHolder val$callbackHolder;
            final /* synthetic */ BaseParameters val$parameters;
            final /* synthetic */ BaseParameters val$result;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.2.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass2(com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener r1, com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder r2, com.mediatek.mmsdk.BaseParameters r3, com.mediatek.mmsdk.BaseParameters r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.2.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.2.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.2.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.2.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.2.run():void");
            }
        }

        /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener$3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ EffectHalClientListener this$1;
            final /* synthetic */ CaptureCallbackHolder val$callbackHolder;
            final /* synthetic */ int val$compleateId;
            final /* synthetic */ BaseParameters val$parameters;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.3.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass3(com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener r1, com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder r2, com.mediatek.mmsdk.BaseParameters r3, int r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.3.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.3.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.3.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.3.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.3.run():void");
            }
        }

        /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener$4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ EffectHalClientListener this$1;
            final /* synthetic */ BaseParameters val$baseParametersResult;
            final /* synthetic */ CaptureCallbackHolder val$callbackHolder;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.4.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass4(com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener r1, com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder r2, com.mediatek.mmsdk.BaseParameters r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.4.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.4.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.4.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.4.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.4.run():void");
            }
        }

        /* renamed from: com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener$5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ EffectHalClientListener this$1;
            final /* synthetic */ BaseParameters val$baseParametersResult;
            final /* synthetic */ CaptureCallbackHolder val$callbackHolder;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.5.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass5(com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener r1, com.mediatek.mmsdk.CameraEffectImpl.CaptureCallbackHolder r2, com.mediatek.mmsdk.BaseParameters r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.5.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.5.<init>(com.mediatek.mmsdk.CameraEffectImpl$EffectHalClientListener, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallbackHolder, com.mediatek.mmsdk.BaseParameters):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.5.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.5.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.5.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
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
        public EffectHalClientListener(com.mediatek.mmsdk.CameraEffectImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.<init>(com.mediatek.mmsdk.CameraEffectImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.<init>(com.mediatek.mmsdk.CameraEffectImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onAborted(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onAborted(com.mediatek.mmsdk.IEffectHalClient r1, com.mediatek.mmsdk.BaseParameters r2) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onAborted(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onAborted(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onCompleted(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters, long):void, dex: 
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
        public void onCompleted(com.mediatek.mmsdk.IEffectHalClient r1, com.mediatek.mmsdk.BaseParameters r2, long r3) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onCompleted(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onCompleted(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onFailed(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onFailed(com.mediatek.mmsdk.IEffectHalClient r1, com.mediatek.mmsdk.BaseParameters r2) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onFailed(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onFailed(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onInputFrameProcessed(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onInputFrameProcessed(com.mediatek.mmsdk.IEffectHalClient r1, com.mediatek.mmsdk.BaseParameters r2, com.mediatek.mmsdk.BaseParameters r3) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onInputFrameProcessed(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onInputFrameProcessed(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onOutputFrameProcessed(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onOutputFrameProcessed(com.mediatek.mmsdk.IEffectHalClient r1, com.mediatek.mmsdk.BaseParameters r2, com.mediatek.mmsdk.BaseParameters r3) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onOutputFrameProcessed(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onOutputFrameProcessed(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onPrepared(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onPrepared(com.mediatek.mmsdk.IEffectHalClient r1, com.mediatek.mmsdk.BaseParameters r2) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onPrepared(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.EffectHalClientListener.onPrepared(com.mediatek.mmsdk.IEffectHalClient, com.mediatek.mmsdk.BaseParameters):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get0(com.mediatek.mmsdk.CameraEffectImpl):android.util.SparseArray, dex: 
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
    /* renamed from: -get0 */
    static /* synthetic */ android.util.SparseArray m2-get0(com.mediatek.mmsdk.CameraEffectImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get0(com.mediatek.mmsdk.CameraEffectImpl):android.util.SparseArray, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.-get0(com.mediatek.mmsdk.CameraEffectImpl):android.util.SparseArray");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get1(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffectSessionImpl, dex: 
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
    /* renamed from: -get1 */
    static /* synthetic */ com.mediatek.mmsdk.CameraEffectSessionImpl m3-get1(com.mediatek.mmsdk.CameraEffectImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get1(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffectSessionImpl, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.-get1(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffectSessionImpl");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: com.mediatek.mmsdk.CameraEffectImpl.-get2(com.mediatek.mmsdk.CameraEffectImpl):long, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get2 */
    static /* synthetic */ long m4-get2(com.mediatek.mmsdk.CameraEffectImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: com.mediatek.mmsdk.CameraEffectImpl.-get2(com.mediatek.mmsdk.CameraEffectImpl):long, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.-get2(com.mediatek.mmsdk.CameraEffectImpl):long");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get3(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffectStatus, dex: 
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
    /* renamed from: -get3 */
    static /* synthetic */ com.mediatek.mmsdk.CameraEffectStatus m5-get3(com.mediatek.mmsdk.CameraEffectImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get3(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffectStatus, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.-get3(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffectStatus");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.mediatek.mmsdk.CameraEffectImpl.-get4(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffect$StateCallback, dex:  in method: com.mediatek.mmsdk.CameraEffectImpl.-get4(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffect$StateCallback, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.mediatek.mmsdk.CameraEffectImpl.-get4(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffect$StateCallback, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get4 */
    static /* synthetic */ com.mediatek.mmsdk.CameraEffect.StateCallback m6-get4(com.mediatek.mmsdk.CameraEffectImpl r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.mediatek.mmsdk.CameraEffectImpl.-get4(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffect$StateCallback, dex:  in method: com.mediatek.mmsdk.CameraEffectImpl.-get4(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffect$StateCallback, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.-get4(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffect$StateCallback");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get5(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.IEffectHalClient, dex: 
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
    /* renamed from: -get5 */
    static /* synthetic */ com.mediatek.mmsdk.IEffectHalClient m7-get5(com.mediatek.mmsdk.CameraEffectImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get5(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.IEffectHalClient, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.-get5(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.IEffectHalClient");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get6(com.mediatek.mmsdk.CameraEffectImpl):java.lang.Object, dex: 
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
    /* renamed from: -get6 */
    static /* synthetic */ java.lang.Object m8-get6(com.mediatek.mmsdk.CameraEffectImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get6(com.mediatek.mmsdk.CameraEffectImpl):java.lang.Object, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.-get6(com.mediatek.mmsdk.CameraEffectImpl):java.lang.Object");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get7(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffectImpl$DeviceStateCallback, dex: 
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
    /* renamed from: -get7 */
    static /* synthetic */ com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback m9-get7(com.mediatek.mmsdk.CameraEffectImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.-get7(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffectImpl$DeviceStateCallback, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.-get7(com.mediatek.mmsdk.CameraEffectImpl):com.mediatek.mmsdk.CameraEffectImpl$DeviceStateCallback");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.mediatek.mmsdk.CameraEffectImpl.<init>(com.mediatek.mmsdk.CameraEffect$StateCallback, android.os.Handler):void, dex: 
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
    public CameraEffectImpl(com.mediatek.mmsdk.CameraEffect.StateCallback r1, android.os.Handler r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.mediatek.mmsdk.CameraEffectImpl.<init>(com.mediatek.mmsdk.CameraEffect$StateCallback, android.os.Handler):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.<init>(com.mediatek.mmsdk.CameraEffect$StateCallback, android.os.Handler):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectImpl.checkIfCameraClosedOrInError():void, dex: 
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
    private void checkIfCameraClosedOrInError() throws com.mediatek.mmsdk.CameraEffectHalException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectImpl.checkIfCameraClosedOrInError():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.checkIfCameraClosedOrInError():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.unConfigureEffectHal():void, dex: 
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
    private void unConfigureEffectHal() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.unConfigureEffectHal():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.unConfigureEffectHal():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.unInitEffectHal():void, dex: 
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
    private void unInitEffectHal() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.unInitEffectHal():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.unInitEffectHal():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.abortCapture(com.mediatek.mmsdk.BaseParameters):void, dex: 
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
    public void abortCapture(com.mediatek.mmsdk.BaseParameters r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.abortCapture(com.mediatek.mmsdk.BaseParameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.abortCapture(com.mediatek.mmsdk.BaseParameters):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.addOutputParameter(int, com.mediatek.mmsdk.BaseParameters, long, boolean):void, dex: 
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
    public void addOutputParameter(int r1, com.mediatek.mmsdk.BaseParameters r2, long r3, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.addOutputParameter(int, com.mediatek.mmsdk.BaseParameters, long, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.addOutputParameter(int, com.mediatek.mmsdk.BaseParameters, long, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.close():void, dex: 
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
    public void close() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.close():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.close():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.closeEffect():void, dex: 
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
    public void closeEffect() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.closeEffect():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.closeEffect():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.configureOutputs(java.util.List, java.util.List):boolean, dex: 
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
    public boolean configureOutputs(java.util.List<android.view.Surface> r1, java.util.List<com.mediatek.mmsdk.BaseParameters> r2) throws com.mediatek.mmsdk.CameraEffectHalException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.configureOutputs(java.util.List, java.util.List):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.configureOutputs(java.util.List, java.util.List):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.createCaptureSession(java.util.List, java.util.List, com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, android.os.Handler):com.mediatek.mmsdk.CameraEffectSession, dex: 
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
    public com.mediatek.mmsdk.CameraEffectSession createCaptureSession(java.util.List<android.view.Surface> r1, java.util.List<com.mediatek.mmsdk.BaseParameters> r2, com.mediatek.mmsdk.CameraEffectSession.SessionStateCallback r3, android.os.Handler r4) throws com.mediatek.mmsdk.CameraEffectHalException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.createCaptureSession(java.util.List, java.util.List, com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, android.os.Handler):com.mediatek.mmsdk.CameraEffectSession, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.createCaptureSession(java.util.List, java.util.List, com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, android.os.Handler):com.mediatek.mmsdk.CameraEffectSession");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.getCaputreRequirement(com.mediatek.mmsdk.BaseParameters):java.util.List<com.mediatek.mmsdk.BaseParameters>, dex: 
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
    public java.util.List<com.mediatek.mmsdk.BaseParameters> getCaputreRequirement(com.mediatek.mmsdk.BaseParameters r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.getCaputreRequirement(com.mediatek.mmsdk.BaseParameters):java.util.List<com.mediatek.mmsdk.BaseParameters>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.getCaputreRequirement(com.mediatek.mmsdk.BaseParameters):java.util.List<com.mediatek.mmsdk.BaseParameters>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.getFrameSyncMode(boolean, int):boolean, dex: 
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
    public boolean getFrameSyncMode(boolean r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.getFrameSyncMode(boolean, int):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.getFrameSyncMode(boolean, int):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.getInputSurface():java.util.List<android.view.Surface>, dex: 
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
    public java.util.List<android.view.Surface> getInputSurface() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.getInputSurface():java.util.List<android.view.Surface>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.getInputSurface():java.util.List<android.view.Surface>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.getOutputsyncMode(int):boolean, dex: 
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
    public boolean getOutputsyncMode(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.getOutputsyncMode(int):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.getOutputsyncMode(int):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setFrameParameters(boolean, int, com.mediatek.mmsdk.BaseParameters, long, boolean):void, dex: 
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
    public void setFrameParameters(boolean r1, int r2, com.mediatek.mmsdk.BaseParameters r3, long r4, boolean r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setFrameParameters(boolean, int, com.mediatek.mmsdk.BaseParameters, long, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.setFrameParameters(boolean, int, com.mediatek.mmsdk.BaseParameters, long, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setFrameSyncMode(boolean, int, boolean):int, dex: 
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
    public int setFrameSyncMode(boolean r1, int r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setFrameSyncMode(boolean, int, boolean):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.setFrameSyncMode(boolean, int, boolean):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setOutputsyncMode(int, boolean):int, dex: 
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
    public int setOutputsyncMode(int r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setOutputsyncMode(int, boolean):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.setOutputsyncMode(int, boolean):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setParameters(com.mediatek.mmsdk.BaseParameters):void, dex: 
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
    public void setParameters(com.mediatek.mmsdk.BaseParameters r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setParameters(com.mediatek.mmsdk.BaseParameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.setParameters(com.mediatek.mmsdk.BaseParameters):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setRemoteCameraEffect(com.mediatek.mmsdk.IEffectHalClient):void, dex: 
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
    public void setRemoteCameraEffect(com.mediatek.mmsdk.IEffectHalClient r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectImpl.setRemoteCameraEffect(com.mediatek.mmsdk.IEffectHalClient):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.setRemoteCameraEffect(com.mediatek.mmsdk.IEffectHalClient):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.setRemoteCameraEffectFail(com.mediatek.mmsdk.CameraEffectHalRuntimeException):void, dex: 
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
    public void setRemoteCameraEffectFail(com.mediatek.mmsdk.CameraEffectHalRuntimeException r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.setRemoteCameraEffectFail(com.mediatek.mmsdk.CameraEffectHalRuntimeException):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.setRemoteCameraEffectFail(com.mediatek.mmsdk.CameraEffectHalRuntimeException):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.startEffectHal(android.os.Handler, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback):void, dex: 
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
    public void startEffectHal(android.os.Handler r1, com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectImpl.startEffectHal(android.os.Handler, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectImpl.startEffectHal(android.os.Handler, com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback):void");
    }

    public EffectHalClientListener getEffectHalListener() {
        return new EffectHalClientListener(this);
    }

    private Handler checkHandler(Handler handler) {
        if (handler != null) {
            return handler;
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            return new Handler(looper);
        }
        throw new IllegalArgumentException("No handler given, and current thread has no looper!");
    }

    private <T> Handler checkHandler(Handler handler, T callback) {
        if (callback != null) {
            return checkHandler(handler);
        }
        return handler;
    }
}
