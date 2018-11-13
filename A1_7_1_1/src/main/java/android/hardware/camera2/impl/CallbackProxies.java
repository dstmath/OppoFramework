package android.hardware.camera2.impl;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.dispatch.Dispatchable;
import android.hardware.camera2.dispatch.MethodNameInvoker;
import android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback;
import android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK;
import android.view.Surface;
import com.android.internal.util.Preconditions;

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
public class CallbackProxies {

    public static class DeviceCaptureCallbackProxy extends CaptureCallback {
        private final MethodNameInvoker<CaptureCallback> mProxy;

        public DeviceCaptureCallbackProxy(Dispatchable<CaptureCallback> dispatchTarget) {
            this.mProxy = new MethodNameInvoker((Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null"), CaptureCallback.class);
        }

        public void onCaptureStarted(CameraDevice camera, CaptureRequest request, long timestamp, long frameNumber) {
            this.mProxy.invoke("onCaptureStarted", camera, request, Long.valueOf(timestamp), Long.valueOf(frameNumber));
        }

        public void onCapturePartial(CameraDevice camera, CaptureRequest request, CaptureResult result) {
            this.mProxy.invoke("onCapturePartial", camera, request, result);
        }

        public void onCaptureProgressed(CameraDevice camera, CaptureRequest request, CaptureResult partialResult) {
            this.mProxy.invoke("onCaptureProgressed", camera, request, partialResult);
        }

        public void onCaptureCompleted(CameraDevice camera, CaptureRequest request, TotalCaptureResult result) {
            this.mProxy.invoke("onCaptureCompleted", camera, request, result);
        }

        public void onCaptureFailed(CameraDevice camera, CaptureRequest request, CaptureFailure failure) {
            this.mProxy.invoke("onCaptureFailed", camera, request, failure);
        }

        public void onCaptureSequenceCompleted(CameraDevice camera, int sequenceId, long frameNumber) {
            this.mProxy.invoke("onCaptureSequenceCompleted", camera, Integer.valueOf(sequenceId), Long.valueOf(frameNumber));
        }

        public void onCaptureSequenceAborted(CameraDevice camera, int sequenceId) {
            this.mProxy.invoke("onCaptureSequenceAborted", camera, Integer.valueOf(sequenceId));
        }
    }

    public static class DeviceStateCallbackProxy extends StateCallbackKK {
        private final MethodNameInvoker<StateCallbackKK> mProxy;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.<init>(android.hardware.camera2.dispatch.Dispatchable):void, dex: 
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
        public DeviceStateCallbackProxy(android.hardware.camera2.dispatch.Dispatchable<android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.<init>(android.hardware.camera2.dispatch.Dispatchable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.<init>(android.hardware.camera2.dispatch.Dispatchable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onActive(android.hardware.camera2.CameraDevice):void, dex: 
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
        public void onActive(android.hardware.camera2.CameraDevice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onActive(android.hardware.camera2.CameraDevice):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onActive(android.hardware.camera2.CameraDevice):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onBusy(android.hardware.camera2.CameraDevice):void, dex: 
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
        public void onBusy(android.hardware.camera2.CameraDevice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onBusy(android.hardware.camera2.CameraDevice):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onBusy(android.hardware.camera2.CameraDevice):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onClosed(android.hardware.camera2.CameraDevice):void, dex: 
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
        public void onClosed(android.hardware.camera2.CameraDevice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onClosed(android.hardware.camera2.CameraDevice):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onClosed(android.hardware.camera2.CameraDevice):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onDisconnected(android.hardware.camera2.CameraDevice):void, dex: 
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
        public void onDisconnected(android.hardware.camera2.CameraDevice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onDisconnected(android.hardware.camera2.CameraDevice):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onDisconnected(android.hardware.camera2.CameraDevice):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onError(android.hardware.camera2.CameraDevice, int):void, dex: 
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
        public void onError(android.hardware.camera2.CameraDevice r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onError(android.hardware.camera2.CameraDevice, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onError(android.hardware.camera2.CameraDevice, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onIdle(android.hardware.camera2.CameraDevice):void, dex: 
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
        public void onIdle(android.hardware.camera2.CameraDevice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onIdle(android.hardware.camera2.CameraDevice):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onIdle(android.hardware.camera2.CameraDevice):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onOpened(android.hardware.camera2.CameraDevice):void, dex: 
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
        public void onOpened(android.hardware.camera2.CameraDevice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onOpened(android.hardware.camera2.CameraDevice):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onOpened(android.hardware.camera2.CameraDevice):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onUnconfigured(android.hardware.camera2.CameraDevice):void, dex: 
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
        public void onUnconfigured(android.hardware.camera2.CameraDevice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onUnconfigured(android.hardware.camera2.CameraDevice):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CallbackProxies.DeviceStateCallbackProxy.onUnconfigured(android.hardware.camera2.CameraDevice):void");
        }
    }

    public static class SessionStateCallbackProxy extends StateCallback {
        private final MethodNameInvoker<StateCallback> mProxy;

        public SessionStateCallbackProxy(Dispatchable<StateCallback> dispatchTarget) {
            this.mProxy = new MethodNameInvoker((Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null"), StateCallback.class);
        }

        public void onConfigured(CameraCaptureSession session) {
            this.mProxy.invoke("onConfigured", session);
        }

        public void onConfigureFailed(CameraCaptureSession session) {
            this.mProxy.invoke("onConfigureFailed", session);
        }

        public void onReady(CameraCaptureSession session) {
            this.mProxy.invoke("onReady", session);
        }

        public void onActive(CameraCaptureSession session) {
            this.mProxy.invoke("onActive", session);
        }

        public void onClosed(CameraCaptureSession session) {
            this.mProxy.invoke("onClosed", session);
        }

        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            this.mProxy.invoke("onSurfacePrepared", session, surface);
        }
    }

    private CallbackProxies() {
        throw new AssertionError();
    }
}
