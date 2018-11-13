package com.mediatek.mmsdk;

import android.os.Handler;
import android.os.Looper;
import com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback;
import com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback;
import com.mediatek.mmsdk.CameraEffectSession.SessionStateCallback;

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
public class CameraEffectSessionImpl extends CameraEffectSession {
    private static final String TAG = "CameraEffectSessionImpl";
    private static final boolean VERBOSE = true;
    private volatile boolean mAborting;
    private CameraEffectImpl mCameraMmEffectImpl;
    private boolean mClosed;
    private final Runnable mConfiguredFailRunnable;
    private final Runnable mConfiguredRunnable;
    private final Handler mDeviceHandler;
    private final SessionStateCallback mStateCallback;
    private final Handler mStateHandler;

    /* renamed from: com.mediatek.mmsdk.CameraEffectSessionImpl$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ CameraEffectSessionImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.1.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl):void, dex: 
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
        AnonymousClass1(com.mediatek.mmsdk.CameraEffectSessionImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.1.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.1.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.1.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.1.run():void");
        }
    }

    /* renamed from: com.mediatek.mmsdk.CameraEffectSessionImpl$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ CameraEffectSessionImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.2.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl):void, dex: 
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
        AnonymousClass2(com.mediatek.mmsdk.CameraEffectSessionImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.2.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.2.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.2.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.2.run():void");
        }
    }

    /* renamed from: com.mediatek.mmsdk.CameraEffectSessionImpl$3 */
    class AnonymousClass3 extends DeviceStateCallback {
        private boolean mActive;
        private boolean mBusy;
        final /* synthetic */ CameraEffectSessionImpl this$0;
        final /* synthetic */ CameraEffectSession val$session;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl, com.mediatek.mmsdk.CameraEffectSession):void, dex: 
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
        AnonymousClass3(com.mediatek.mmsdk.CameraEffectSessionImpl r1, com.mediatek.mmsdk.CameraEffectSession r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl, com.mediatek.mmsdk.CameraEffectSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.3.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl, com.mediatek.mmsdk.CameraEffectSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onActive(com.mediatek.mmsdk.CameraEffect):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onActive(com.mediatek.mmsdk.CameraEffect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onActive(com.mediatek.mmsdk.CameraEffect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onActive(com.mediatek.mmsdk.CameraEffect):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onBusy(com.mediatek.mmsdk.CameraEffect):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onBusy(com.mediatek.mmsdk.CameraEffect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onBusy(com.mediatek.mmsdk.CameraEffect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onBusy(com.mediatek.mmsdk.CameraEffect):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onDisconnected(com.mediatek.mmsdk.CameraEffect):void, dex: 
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
        public void onDisconnected(com.mediatek.mmsdk.CameraEffect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onDisconnected(com.mediatek.mmsdk.CameraEffect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onDisconnected(com.mediatek.mmsdk.CameraEffect):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onError(com.mediatek.mmsdk.CameraEffect, int):void, dex: 
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
        public void onError(com.mediatek.mmsdk.CameraEffect r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onError(com.mediatek.mmsdk.CameraEffect, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onError(com.mediatek.mmsdk.CameraEffect, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onIdle(com.mediatek.mmsdk.CameraEffect):void, dex: 
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
        public void onIdle(com.mediatek.mmsdk.CameraEffect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onIdle(com.mediatek.mmsdk.CameraEffect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onIdle(com.mediatek.mmsdk.CameraEffect):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onUnconfigured(com.mediatek.mmsdk.CameraEffect):void, dex: 
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
        public void onUnconfigured(com.mediatek.mmsdk.CameraEffect r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onUnconfigured(com.mediatek.mmsdk.CameraEffect):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.3.onUnconfigured(com.mediatek.mmsdk.CameraEffect):void");
        }
    }

    /* renamed from: com.mediatek.mmsdk.CameraEffectSessionImpl$4 */
    class AnonymousClass4 extends CaptureCallback {
        final /* synthetic */ CameraEffectSessionImpl this$0;
        final /* synthetic */ CameraEffectSession.CaptureCallback val$callback;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl, com.mediatek.mmsdk.CameraEffectSession$CaptureCallback):void, dex: 
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
        AnonymousClass4(com.mediatek.mmsdk.CameraEffectSessionImpl r1, com.mediatek.mmsdk.CameraEffectSession.CaptureCallback r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl, com.mediatek.mmsdk.CameraEffectSession$CaptureCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.4.<init>(com.mediatek.mmsdk.CameraEffectSessionImpl, com.mediatek.mmsdk.CameraEffectSession$CaptureCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onCaptureFailed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onCaptureFailed(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onCaptureFailed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onCaptureFailed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onCaptureSequenceAborted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onCaptureSequenceAborted(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onCaptureSequenceAborted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onCaptureSequenceAborted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onCaptureSequenceCompleted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, long):void, dex: 
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
        public void onCaptureSequenceCompleted(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2, long r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onCaptureSequenceCompleted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onCaptureSequenceCompleted(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onInputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onInputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2, com.mediatek.mmsdk.BaseParameters r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onInputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onInputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onOutputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
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
        public void onOutputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession r1, com.mediatek.mmsdk.BaseParameters r2, com.mediatek.mmsdk.BaseParameters r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onOutputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.4.onOutputFrameProcessed(com.mediatek.mmsdk.CameraEffectSession, com.mediatek.mmsdk.BaseParameters, com.mediatek.mmsdk.BaseParameters):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.mediatek.mmsdk.CameraEffectSessionImpl.-get1(com.mediatek.mmsdk.CameraEffectSessionImpl):com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, dex:  in method: com.mediatek.mmsdk.CameraEffectSessionImpl.-get1(com.mediatek.mmsdk.CameraEffectSessionImpl):com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.mediatek.mmsdk.CameraEffectSessionImpl.-get1(com.mediatek.mmsdk.CameraEffectSessionImpl):com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, dex: 
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
    /* renamed from: -get1 */
    static /* synthetic */ com.mediatek.mmsdk.CameraEffectSession.SessionStateCallback m1-get1(com.mediatek.mmsdk.CameraEffectSessionImpl r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.mediatek.mmsdk.CameraEffectSessionImpl.-get1(com.mediatek.mmsdk.CameraEffectSessionImpl):com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, dex:  in method: com.mediatek.mmsdk.CameraEffectSessionImpl.-get1(com.mediatek.mmsdk.CameraEffectSessionImpl):com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.-get1(com.mediatek.mmsdk.CameraEffectSessionImpl):com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.mediatek.mmsdk.CameraEffectSessionImpl.<init>(com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, android.os.Handler, com.mediatek.mmsdk.CameraEffectImpl, android.os.Handler, boolean):void, dex: 
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
    public CameraEffectSessionImpl(com.mediatek.mmsdk.CameraEffectSession.SessionStateCallback r1, android.os.Handler r2, com.mediatek.mmsdk.CameraEffectImpl r3, android.os.Handler r4, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.mediatek.mmsdk.CameraEffectSessionImpl.<init>(com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, android.os.Handler, com.mediatek.mmsdk.CameraEffectImpl, android.os.Handler, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.<init>(com.mediatek.mmsdk.CameraEffectSession$SessionStateCallback, android.os.Handler, com.mediatek.mmsdk.CameraEffectImpl, android.os.Handler, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectSessionImpl.checkNotClosed():void, dex: 
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
    private void checkNotClosed() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectSessionImpl.checkNotClosed():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.checkNotClosed():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectSessionImpl.close():void, dex: 
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
    public void close() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.mediatek.mmsdk.CameraEffectSessionImpl.close():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.close():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.closeSession():void, dex: 
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
    public void closeSession() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.closeSession():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.closeSession():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.getFrameSyncMode(boolean, int):boolean, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.getFrameSyncMode(boolean, int):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.getFrameSyncMode(boolean, int):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.replaceSessionClose():void, dex: 
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
    void replaceSessionClose() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.replaceSessionClose():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.replaceSessionClose():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.setFrameParameters(boolean, int, com.mediatek.mmsdk.BaseParameters, long, boolean):void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.setFrameParameters(boolean, int, com.mediatek.mmsdk.BaseParameters, long, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.setFrameParameters(boolean, int, com.mediatek.mmsdk.BaseParameters, long, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.setFrameSyncMode(boolean, int, boolean):int, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.setFrameSyncMode(boolean, int, boolean):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.setFrameSyncMode(boolean, int, boolean):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.startCapture(com.mediatek.mmsdk.CameraEffectSession$CaptureCallback, android.os.Handler):void, dex: 
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
    public void startCapture(com.mediatek.mmsdk.CameraEffectSession.CaptureCallback r1, android.os.Handler r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.startCapture(com.mediatek.mmsdk.CameraEffectSession$CaptureCallback, android.os.Handler):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.startCapture(com.mediatek.mmsdk.CameraEffectSession$CaptureCallback, android.os.Handler):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.stopCapture(com.mediatek.mmsdk.BaseParameters):void, dex: 
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
    public void stopCapture(com.mediatek.mmsdk.BaseParameters r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.mmsdk.CameraEffectSessionImpl.stopCapture(com.mediatek.mmsdk.BaseParameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.stopCapture(com.mediatek.mmsdk.BaseParameters):void");
    }

    private static <T> Handler checkHandler(Handler handler, T callback) {
        if (callback != null) {
            return checkHandler(handler);
        }
        return handler;
    }

    private static Handler checkHandler(Handler handler) {
        if (handler != null) {
            return handler;
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            return new Handler(looper);
        }
        throw new IllegalArgumentException("No handler given, and current thread has no looper!");
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    com.mediatek.mmsdk.CameraEffectImpl.DeviceStateCallback getDeviceStateCallback() {
        /*
        r2 = this;
        r0 = r2;
        r1 = new com.mediatek.mmsdk.CameraEffectSessionImpl$3;
        r1.<init>(r2, r2);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.getDeviceStateCallback():com.mediatek.mmsdk.CameraEffectImpl$DeviceStateCallback");
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private com.mediatek.mmsdk.CameraEffectImpl.CaptureCallback createCaptureCallback(android.os.Handler r2, com.mediatek.mmsdk.CameraEffectSession.CaptureCallback r3) {
        /*
        r1 = this;
        r0 = new com.mediatek.mmsdk.CameraEffectSessionImpl$4;
        r0.<init>(r1, r3);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.mmsdk.CameraEffectSessionImpl.createCaptureCallback(android.os.Handler, com.mediatek.mmsdk.CameraEffectSession$CaptureCallback):com.mediatek.mmsdk.CameraEffectImpl$CaptureCallback");
    }
}
