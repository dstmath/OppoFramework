package android.hardware.camera2;

import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.os.Handler;
import android.view.Surface;
import java.util.List;

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
public abstract class CameraDevice implements AutoCloseable {
    public static final int TEMPLATE_MANUAL = 6;
    public static final int TEMPLATE_PREVIEW = 1;
    public static final int TEMPLATE_RECORD = 3;
    public static final int TEMPLATE_STILL_CAPTURE = 2;
    public static final int TEMPLATE_VIDEO_SNAPSHOT = 4;
    public static final int TEMPLATE_ZERO_SHUTTER_LAG = 5;

    public static abstract class StateCallback {
        public static final int ERROR_CAMERA_DEVICE = 4;
        public static final int ERROR_CAMERA_DISABLED = 3;
        public static final int ERROR_CAMERA_IN_USE = 1;
        public static final int ERROR_CAMERA_SERVICE = 5;
        public static final int ERROR_MAX_CAMERAS_IN_USE = 2;

        public abstract void onDisconnected(CameraDevice cameraDevice);

        public abstract void onError(CameraDevice cameraDevice, int i);

        public abstract void onOpened(CameraDevice cameraDevice);

        public void onClosed(CameraDevice camera) {
        }
    }

    public static abstract class StateListener extends StateCallback {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.camera2.CameraDevice.StateListener.<init>():void, dex: 
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
        public StateListener() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.camera2.CameraDevice.StateListener.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.CameraDevice.StateListener.<init>():void");
        }
    }

    public abstract void close();

    public abstract Builder createCaptureRequest(int i) throws CameraAccessException;

    public abstract void createCaptureSession(List<Surface> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract void createCaptureSessionByOutputConfigurations(List<OutputConfiguration> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract void createConstrainedHighSpeedCaptureSession(List<Surface> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract Builder createReprocessCaptureRequest(TotalCaptureResult totalCaptureResult) throws CameraAccessException;

    public abstract void createReprocessableCaptureSession(InputConfiguration inputConfiguration, List<Surface> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract void createReprocessableCaptureSessionByConfigurations(InputConfiguration inputConfiguration, List<OutputConfiguration> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract String getId();
}
