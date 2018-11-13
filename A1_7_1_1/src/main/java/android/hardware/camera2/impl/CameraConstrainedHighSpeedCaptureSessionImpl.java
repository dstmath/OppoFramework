package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.view.Surface;

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
public class CameraConstrainedHighSpeedCaptureSessionImpl extends CameraConstrainedHighSpeedCaptureSession implements CameraCaptureSessionCore {
    private final CameraCharacteristics mCharacteristics;
    private final CameraCaptureSessionImpl mSessionImpl;

    private class WrapperCallback extends StateCallback {
        private final StateCallback mCallback;
        final /* synthetic */ CameraConstrainedHighSpeedCaptureSessionImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.<init>(android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl, android.hardware.camera2.CameraCaptureSession$StateCallback):void, dex: 
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
        public WrapperCallback(android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl r1, android.hardware.camera2.CameraCaptureSession.StateCallback r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.<init>(android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl, android.hardware.camera2.CameraCaptureSession$StateCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.<init>(android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl, android.hardware.camera2.CameraCaptureSession$StateCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onActive(android.hardware.camera2.CameraCaptureSession):void, dex: 
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
        public void onActive(android.hardware.camera2.CameraCaptureSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onActive(android.hardware.camera2.CameraCaptureSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onActive(android.hardware.camera2.CameraCaptureSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onClosed(android.hardware.camera2.CameraCaptureSession):void, dex: 
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
        public void onClosed(android.hardware.camera2.CameraCaptureSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onClosed(android.hardware.camera2.CameraCaptureSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onClosed(android.hardware.camera2.CameraCaptureSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onConfigureFailed(android.hardware.camera2.CameraCaptureSession):void, dex: 
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
        public void onConfigureFailed(android.hardware.camera2.CameraCaptureSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onConfigureFailed(android.hardware.camera2.CameraCaptureSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onConfigureFailed(android.hardware.camera2.CameraCaptureSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onConfigured(android.hardware.camera2.CameraCaptureSession):void, dex: 
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
        public void onConfigured(android.hardware.camera2.CameraCaptureSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onConfigured(android.hardware.camera2.CameraCaptureSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onConfigured(android.hardware.camera2.CameraCaptureSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onReady(android.hardware.camera2.CameraCaptureSession):void, dex: 
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
        public void onReady(android.hardware.camera2.CameraCaptureSession r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onReady(android.hardware.camera2.CameraCaptureSession):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onReady(android.hardware.camera2.CameraCaptureSession):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onSurfacePrepared(android.hardware.camera2.CameraCaptureSession, android.view.Surface):void, dex: 
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
        public void onSurfacePrepared(android.hardware.camera2.CameraCaptureSession r1, android.view.Surface r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onSurfacePrepared(android.hardware.camera2.CameraCaptureSession, android.view.Surface):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.WrapperCallback.onSurfacePrepared(android.hardware.camera2.CameraCaptureSession, android.view.Surface):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.<init>(int, java.util.List, android.hardware.camera2.CameraCaptureSession$StateCallback, android.os.Handler, android.hardware.camera2.impl.CameraDeviceImpl, android.os.Handler, boolean, android.hardware.camera2.CameraCharacteristics):void, dex: 
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
    CameraConstrainedHighSpeedCaptureSessionImpl(int r1, java.util.List<android.view.Surface> r2, android.hardware.camera2.CameraCaptureSession.StateCallback r3, android.os.Handler r4, android.hardware.camera2.impl.CameraDeviceImpl r5, android.os.Handler r6, boolean r7, android.hardware.camera2.CameraCharacteristics r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.<init>(int, java.util.List, android.hardware.camera2.CameraCaptureSession$StateCallback, android.os.Handler, android.hardware.camera2.impl.CameraDeviceImpl, android.os.Handler, boolean, android.hardware.camera2.CameraCharacteristics):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.<init>(int, java.util.List, android.hardware.camera2.CameraCaptureSession$StateCallback, android.os.Handler, android.hardware.camera2.impl.CameraDeviceImpl, android.os.Handler, boolean, android.hardware.camera2.CameraCharacteristics):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.isConstrainedHighSpeedRequestList(java.util.List):boolean, dex: 
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
    private boolean isConstrainedHighSpeedRequestList(java.util.List<android.hardware.camera2.CaptureRequest> r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.isConstrainedHighSpeedRequestList(java.util.List):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.isConstrainedHighSpeedRequestList(java.util.List):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.abortCaptures():void, dex: 
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
    public void abortCaptures() throws android.hardware.camera2.CameraAccessException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.abortCaptures():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.abortCaptures():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.captureBurst(java.util.List, android.hardware.camera2.CameraCaptureSession$CaptureCallback, android.os.Handler):int, dex: 
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
    public int captureBurst(java.util.List<android.hardware.camera2.CaptureRequest> r1, android.hardware.camera2.CameraCaptureSession.CaptureCallback r2, android.os.Handler r3) throws android.hardware.camera2.CameraAccessException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.captureBurst(java.util.List, android.hardware.camera2.CameraCaptureSession$CaptureCallback, android.os.Handler):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.captureBurst(java.util.List, android.hardware.camera2.CameraCaptureSession$CaptureCallback, android.os.Handler):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.close():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.close():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.close():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.createHighSpeedRequestList(android.hardware.camera2.CaptureRequest):java.util.List<android.hardware.camera2.CaptureRequest>, dex: 
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
    public java.util.List<android.hardware.camera2.CaptureRequest> createHighSpeedRequestList(android.hardware.camera2.CaptureRequest r1) throws android.hardware.camera2.CameraAccessException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.createHighSpeedRequestList(android.hardware.camera2.CaptureRequest):java.util.List<android.hardware.camera2.CaptureRequest>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.createHighSpeedRequestList(android.hardware.camera2.CaptureRequest):java.util.List<android.hardware.camera2.CaptureRequest>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.finishDeferredConfiguration(java.util.List):void, dex: 
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
    public void finishDeferredConfiguration(java.util.List<android.hardware.camera2.params.OutputConfiguration> r1) throws android.hardware.camera2.CameraAccessException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.finishDeferredConfiguration(java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.finishDeferredConfiguration(java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.getDevice():android.hardware.camera2.CameraDevice, dex: 
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
    public android.hardware.camera2.CameraDevice getDevice() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.getDevice():android.hardware.camera2.CameraDevice, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.getDevice():android.hardware.camera2.CameraDevice");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.getDeviceStateCallback():android.hardware.camera2.impl.CameraDeviceImpl$StateCallbackKK, dex: 
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
    public android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK getDeviceStateCallback() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.getDeviceStateCallback():android.hardware.camera2.impl.CameraDeviceImpl$StateCallbackKK, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.getDeviceStateCallback():android.hardware.camera2.impl.CameraDeviceImpl$StateCallbackKK");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.isAborting():boolean, dex: 
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
    public boolean isAborting() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.isAborting():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.isAborting():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.prepare(int, android.view.Surface):void, dex: 
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
    public void prepare(int r1, android.view.Surface r2) throws android.hardware.camera2.CameraAccessException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.prepare(int, android.view.Surface):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.prepare(int, android.view.Surface):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.prepare(android.view.Surface):void, dex: 
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
    public void prepare(android.view.Surface r1) throws android.hardware.camera2.CameraAccessException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.prepare(android.view.Surface):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.prepare(android.view.Surface):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.replaceSessionClose():void, dex: 
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
    public void replaceSessionClose() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.replaceSessionClose():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.replaceSessionClose():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.setRepeatingBurst(java.util.List, android.hardware.camera2.CameraCaptureSession$CaptureCallback, android.os.Handler):int, dex: 
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
    public int setRepeatingBurst(java.util.List<android.hardware.camera2.CaptureRequest> r1, android.hardware.camera2.CameraCaptureSession.CaptureCallback r2, android.os.Handler r3) throws android.hardware.camera2.CameraAccessException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.setRepeatingBurst(java.util.List, android.hardware.camera2.CameraCaptureSession$CaptureCallback, android.os.Handler):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.setRepeatingBurst(java.util.List, android.hardware.camera2.CameraCaptureSession$CaptureCallback, android.os.Handler):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.stopRepeating():void, dex: 
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
    public void stopRepeating() throws android.hardware.camera2.CameraAccessException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.stopRepeating():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.stopRepeating():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.tearDown(android.view.Surface):void, dex:  in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.tearDown(android.view.Surface):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.tearDown(android.view.Surface):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:72)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void tearDown(android.view.Surface r1) throws android.hardware.camera2.CameraAccessException {
        /*
        // Can't load method instructions: Load method exception: null in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.tearDown(android.view.Surface):void, dex:  in method: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.tearDown(android.view.Surface):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl.tearDown(android.view.Surface):void");
    }

    public int capture(CaptureRequest request, CaptureCallback listener, Handler handler) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    public int setRepeatingRequest(CaptureRequest request, CaptureCallback listener, Handler handler) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    public Surface getInputSurface() {
        return null;
    }

    public boolean isReprocessable() {
        return false;
    }
}
