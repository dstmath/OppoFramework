package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.ICameraDeviceCallbacks.Stub;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.utils.SubmitInfo;
import android.hardware.camera2.utils.SurfaceUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class CameraDeviceImpl extends CameraDevice implements DeathRecipient {
    private static final int REQUEST_ID_NONE = -1;
    private final boolean DEBUG;
    private final String TAG;
    private final Runnable mCallOnActive;
    private final Runnable mCallOnBusy;
    private final Runnable mCallOnClosed;
    private final Runnable mCallOnDisconnected;
    private final Runnable mCallOnIdle;
    private final Runnable mCallOnOpened;
    private final Runnable mCallOnUnconfigured;
    private final CameraDeviceCallbacks mCallbacks;
    private final String mCameraId;
    private final SparseArray<CaptureCallbackHolder> mCaptureCallbackMap;
    private final CameraCharacteristics mCharacteristics;
    private final AtomicBoolean mClosing;
    private SimpleEntry<Integer, InputConfiguration> mConfiguredInput;
    private final SparseArray<OutputConfiguration> mConfiguredOutputs;
    private CameraCaptureSessionCore mCurrentSession;
    private final StateCallback mDeviceCallback;
    private final Handler mDeviceHandler;
    private final FrameNumberTracker mFrameNumberTracker;
    private boolean mIdle;
    private boolean mInError;
    final Object mInterfaceLock;
    private int mNextSessionId;
    private ICameraDeviceUserWrapper mRemoteDevice;
    private int mRepeatingRequestId;
    private final List<RequestLastFrameNumbersHolder> mRequestLastFrameNumbersList;
    private volatile StateCallbackKK mSessionStateCallback;
    private final int mTotalPartialCount;

    public static abstract class CaptureCallback {
        public static final int NO_FRAMES_CAPTURED = -1;

        public void onCaptureStarted(CameraDevice camera, CaptureRequest request, long timestamp, long frameNumber) {
        }

        public void onCapturePartial(CameraDevice camera, CaptureRequest request, CaptureResult result) {
        }

        public void onCaptureProgressed(CameraDevice camera, CaptureRequest request, CaptureResult partialResult) {
        }

        public void onCaptureCompleted(CameraDevice camera, CaptureRequest request, TotalCaptureResult result) {
        }

        public void onCaptureFailed(CameraDevice camera, CaptureRequest request, CaptureFailure failure) {
        }

        public void onCaptureSequenceCompleted(CameraDevice camera, int sequenceId, long frameNumber) {
        }

        public void onCaptureSequenceAborted(CameraDevice camera, int sequenceId) {
        }

        public void onCaptureBufferLost(CameraDevice camera, CaptureRequest request, Surface target, long frameNumber) {
        }
    }

    public static abstract class StateCallbackKK extends StateCallback {
        public void onUnconfigured(CameraDevice camera) {
        }

        public void onActive(CameraDevice camera) {
        }

        public void onBusy(CameraDevice camera) {
        }

        public void onIdle(CameraDevice camera) {
        }

        public void onSurfacePrepared(Surface surface) {
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$11 */
    class AnonymousClass11 implements Runnable {
        final /* synthetic */ CameraDeviceImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraDeviceImpl.11.<init>(android.hardware.camera2.impl.CameraDeviceImpl):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass11(android.hardware.camera2.impl.CameraDeviceImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraDeviceImpl.11.<init>(android.hardware.camera2.impl.CameraDeviceImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.11.<init>(android.hardware.camera2.impl.CameraDeviceImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraDeviceImpl.11.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraDeviceImpl.11.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.11.run():void");
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ CameraDeviceImpl this$0;

        AnonymousClass1(CameraDeviceImpl this$0) {
            this.this$0 = this$0;
        }

        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onOpened(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            android.hardware.camera2.impl.CameraDeviceImpl.-get6(r3.this$0).onOpened(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:13:0x0029, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = this.this$0.mSessionStateCallback;
            }
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ CameraDeviceImpl this$0;

        AnonymousClass2(CameraDeviceImpl this$0) {
            this.this$0 = this$0;
        }

        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onUnconfigured(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = this.this$0.mSessionStateCallback;
            }
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ CameraDeviceImpl this$0;

        AnonymousClass3(CameraDeviceImpl this$0) {
            this.this$0 = this$0;
        }

        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onActive(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = this.this$0.mSessionStateCallback;
            }
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ CameraDeviceImpl this$0;

        AnonymousClass4(CameraDeviceImpl this$0) {
            this.this$0 = this$0;
        }

        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onBusy(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = this.this$0.mSessionStateCallback;
            }
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$5 */
    class AnonymousClass5 implements Runnable {
        private boolean mClosedOnce;
        final /* synthetic */ CameraDeviceImpl this$0;

        AnonymousClass5(CameraDeviceImpl this$0) {
            this.this$0 = this$0;
            this.mClosedOnce = false;
        }

        public void run() {
            if (this.mClosedOnce) {
                throw new AssertionError("Don't post #onClosed more than once");
            }
            StateCallbackKK sessionCallback;
            synchronized (this.this$0.mInterfaceLock) {
                sessionCallback = this.this$0.mSessionStateCallback;
            }
            if (sessionCallback != null) {
                sessionCallback.onClosed(this.this$0);
            }
            this.this$0.mDeviceCallback.onClosed(this.this$0);
            this.mClosedOnce = true;
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ CameraDeviceImpl this$0;

        AnonymousClass6(CameraDeviceImpl this$0) {
            this.this$0 = this$0;
        }

        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onIdle(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = this.this$0.mSessionStateCallback;
            }
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$7 */
    class AnonymousClass7 implements Runnable {
        final /* synthetic */ CameraDeviceImpl this$0;

        AnonymousClass7(CameraDeviceImpl this$0) {
            this.this$0 = this$0;
        }

        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onDisconnected(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            android.hardware.camera2.impl.CameraDeviceImpl.-get6(r3.this$0).onDisconnected(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:13:0x0029, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = this.this$0.mSessionStateCallback;
            }
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ CameraDeviceImpl this$0;
        final /* synthetic */ int val$code;
        final /* synthetic */ boolean val$isError;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraDeviceImpl.8.<init>(android.hardware.camera2.impl.CameraDeviceImpl, boolean, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass8(android.hardware.camera2.impl.CameraDeviceImpl r1, boolean r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraDeviceImpl.8.<init>(android.hardware.camera2.impl.CameraDeviceImpl, boolean, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.8.<init>(android.hardware.camera2.impl.CameraDeviceImpl, boolean, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.hardware.camera2.impl.CameraDeviceImpl.8.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.hardware.camera2.impl.CameraDeviceImpl.8.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.8.run():void");
        }
    }

    public class CameraDeviceCallbacks extends Stub {
        final /* synthetic */ CameraDeviceImpl this$0;

        /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl$CameraDeviceCallbacks$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ CameraDeviceCallbacks this$1;
            final /* synthetic */ int val$publicErrorCode;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.1.<init>(android.hardware.camera2.impl.CameraDeviceImpl$CameraDeviceCallbacks, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            AnonymousClass1(android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks r1, int r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.1.<init>(android.hardware.camera2.impl.CameraDeviceImpl$CameraDeviceCallbacks, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.1.<init>(android.hardware.camera2.impl.CameraDeviceImpl$CameraDeviceCallbacks, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.1.run():void");
            }
        }

        public CameraDeviceCallbacks(CameraDeviceImpl this$0) {
            this.this$0 = this$0;
        }

        public IBinder asBinder() {
            return this;
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
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public void onDeviceError(int r8, android.hardware.camera2.impl.CaptureResultExtras r9) {
            /*
            r7 = this;
            r6 = 1;
            r2 = r7.this$0;
            r3 = r2.mInterfaceLock;
            monitor-enter(r3);
            r2 = r7.this$0;	 Catch:{ all -> 0x0059 }
            r2 = r2.mRemoteDevice;	 Catch:{ all -> 0x0059 }
            if (r2 != 0) goto L_0x0010;
        L_0x000e:
            monitor-exit(r3);
            return;
        L_0x0010:
            switch(r8) {
                case 0: goto L_0x0049;
                case 1: goto L_0x0030;
                case 2: goto L_0x0030;
                case 3: goto L_0x005e;
                case 4: goto L_0x005e;
                case 5: goto L_0x005e;
                default: goto L_0x0013;
            };
        L_0x0013:
            r2 = r7.this$0;	 Catch:{ all -> 0x0059 }
            r2 = r2.TAG;	 Catch:{ all -> 0x0059 }
            r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0059 }
            r4.<init>();	 Catch:{ all -> 0x0059 }
            r5 = "Unknown error from camera device: ";	 Catch:{ all -> 0x0059 }
            r4 = r4.append(r5);	 Catch:{ all -> 0x0059 }
            r4 = r4.append(r8);	 Catch:{ all -> 0x0059 }
            r4 = r4.toString();	 Catch:{ all -> 0x0059 }
            android.util.Log.e(r2, r4);	 Catch:{ all -> 0x0059 }
        L_0x0030:
            r2 = r7.this$0;	 Catch:{ all -> 0x0059 }
            r4 = 1;	 Catch:{ all -> 0x0059 }
            r2.mInError = r4;	 Catch:{ all -> 0x0059 }
            if (r8 != r6) goto L_0x005c;	 Catch:{ all -> 0x0059 }
        L_0x0038:
            r0 = 4;	 Catch:{ all -> 0x0059 }
        L_0x0039:
            r1 = new android.hardware.camera2.impl.CameraDeviceImpl$CameraDeviceCallbacks$1;	 Catch:{ all -> 0x0059 }
            r1.<init>(r7, r0);	 Catch:{ all -> 0x0059 }
            r2 = r7.this$0;	 Catch:{ all -> 0x0059 }
            r2 = r2.mDeviceHandler;	 Catch:{ all -> 0x0059 }
            r2.post(r1);	 Catch:{ all -> 0x0059 }
        L_0x0047:
            monitor-exit(r3);
            return;
        L_0x0049:
            r2 = r7.this$0;	 Catch:{ all -> 0x0059 }
            r2 = r2.mDeviceHandler;	 Catch:{ all -> 0x0059 }
            r4 = r7.this$0;	 Catch:{ all -> 0x0059 }
            r4 = r4.mCallOnDisconnected;	 Catch:{ all -> 0x0059 }
            r2.post(r4);	 Catch:{ all -> 0x0059 }
            goto L_0x0047;
        L_0x0059:
            r2 = move-exception;
            monitor-exit(r3);
            throw r2;
        L_0x005c:
            r0 = 5;
            goto L_0x0039;
        L_0x005e:
            r7.onCaptureErrorLocked(r8, r9);	 Catch:{ all -> 0x0059 }
            goto L_0x0047;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.onDeviceError(int, android.hardware.camera2.impl.CaptureResultExtras):void");
        }

        /* JADX WARNING: Missing block: B:8:0x0017, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onRepeatingRequestError(long lastFrameNumber) {
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null || this.this$0.mRepeatingRequestId == -1) {
                } else {
                    this.this$0.checkEarlyTriggerSequenceComplete(this.this$0.mRepeatingRequestId, lastFrameNumber);
                    this.this$0.mRepeatingRequestId = -1;
                }
            }
        }

        public void onDeviceIdle() {
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null) {
                    return;
                }
                if (!this.this$0.mIdle) {
                    this.this$0.mDeviceHandler.post(this.this$0.mCallOnIdle);
                }
                this.this$0.mIdle = true;
            }
        }

        public void onCaptureStarted(CaptureResultExtras resultExtras, long timestamp) {
            int requestId = resultExtras.getRequestId();
            final long frameNumber = resultExtras.getFrameNumber();
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null) {
                    return;
                }
                final CaptureCallbackHolder holder = (CaptureCallbackHolder) this.this$0.mCaptureCallbackMap.get(requestId);
                if (holder == null) {
                } else if (this.this$0.isClosed()) {
                } else {
                    final CaptureResultExtras captureResultExtras = resultExtras;
                    final long j = timestamp;
                    holder.getHandler().post(new Runnable(this) {
                        final /* synthetic */ CameraDeviceCallbacks this$1;

                        public void run() {
                            if (!this.this$1.this$0.isClosed()) {
                                holder.getCallback().onCaptureStarted(this.this$1.this$0, holder.getRequest(captureResultExtras.getSubsequenceId()), j, frameNumber);
                            }
                        }
                    });
                }
            }
        }

        /* JADX WARNING: Missing block: B:31:0x00c5, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras) throws RemoteException {
            int requestId = resultExtras.getRequestId();
            long frameNumber = resultExtras.getFrameNumber();
            synchronized (this.this$0.mInterfaceLock) {
                if (this.this$0.mRemoteDevice == null) {
                    return;
                }
                result.set(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE, (Size) this.this$0.getCharacteristics().get(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE));
                final CaptureCallbackHolder holder = (CaptureCallbackHolder) this.this$0.mCaptureCallbackMap.get(requestId);
                if (holder == null) {
                    this.this$0.mFrameNumberTracker.updateTracker(frameNumber, null, false, false);
                    return;
                }
                final CaptureRequest request = holder.getRequest(resultExtras.getSubsequenceId());
                boolean isPartialResult = resultExtras.getPartialResultCount() < this.this$0.mTotalPartialCount;
                boolean isReprocess = request.isReprocess();
                if (this.this$0.isClosed()) {
                    this.this$0.mFrameNumberTracker.updateTracker(frameNumber, null, isPartialResult, isReprocess);
                    return;
                }
                CaptureResult finalResult;
                Runnable anonymousClass3;
                if (isPartialResult) {
                    CaptureResult captureResult = new CaptureResult(result, request, resultExtras);
                    final CaptureResult captureResult2 = captureResult;
                    anonymousClass3 = new Runnable(this) {
                        final /* synthetic */ CameraDeviceCallbacks this$1;

                        public void run() {
                            if (!this.this$1.this$0.isClosed()) {
                                holder.getCallback().onCaptureProgressed(this.this$1.this$0, request, captureResult2);
                            }
                        }
                    };
                    finalResult = captureResult;
                } else {
                    final CaptureResult resultAsCapture = new TotalCaptureResult(result, request, resultExtras, this.this$0.mFrameNumberTracker.popPartialResults(frameNumber), holder.getSessionId());
                    anonymousClass3 = new Runnable(this) {
                        final /* synthetic */ CameraDeviceCallbacks this$1;

                        public void run() {
                            if (!this.this$1.this$0.isClosed()) {
                                holder.getCallback().onCaptureCompleted(this.this$1.this$0, request, resultAsCapture);
                            }
                        }
                    };
                    finalResult = resultAsCapture;
                }
                holder.getHandler().post(resultDispatch);
                this.this$0.mFrameNumberTracker.updateTracker(frameNumber, finalResult, isPartialResult, isReprocess);
                if (!isPartialResult) {
                    this.this$0.checkAndFireSequenceComplete();
                }
            }
        }

        public void onPrepared(int streamId) {
            OutputConfiguration output;
            StateCallbackKK sessionCallback;
            synchronized (this.this$0.mInterfaceLock) {
                output = (OutputConfiguration) this.this$0.mConfiguredOutputs.get(streamId);
                sessionCallback = this.this$0.mSessionStateCallback;
            }
            if (sessionCallback != null) {
                if (output == null) {
                    Log.w(this.this$0.TAG, "onPrepared invoked for unknown output Surface");
                } else {
                    sessionCallback.onSurfacePrepared(output.getSurface());
                }
            }
        }

        private void onCaptureErrorLocked(int errorCode, CaptureResultExtras resultExtras) {
            Runnable failureDispatch;
            int requestId = resultExtras.getRequestId();
            int subsequenceId = resultExtras.getSubsequenceId();
            final long frameNumber = resultExtras.getFrameNumber();
            final CaptureCallbackHolder holder = (CaptureCallbackHolder) this.this$0.mCaptureCallbackMap.get(requestId);
            final CaptureRequest request = holder.getRequest(subsequenceId);
            if (errorCode == 5) {
                final Surface outputSurface = ((OutputConfiguration) this.this$0.mConfiguredOutputs.get(resultExtras.getErrorStreamId())).getSurface();
                failureDispatch = new Runnable(this) {
                    final /* synthetic */ CameraDeviceCallbacks this$1;

                    public void run() {
                        if (!this.this$1.this$0.isClosed()) {
                            holder.getCallback().onCaptureBufferLost(this.this$1.this$0, request, outputSurface, frameNumber);
                        }
                    }
                };
            } else {
                int reason;
                boolean mayHaveBuffers = errorCode == 4;
                if (this.this$0.mCurrentSession == null || !this.this$0.mCurrentSession.isAborting()) {
                    reason = 0;
                } else {
                    reason = 1;
                }
                final CaptureFailure failure = new CaptureFailure(request, reason, mayHaveBuffers, requestId, frameNumber);
                failureDispatch = new Runnable(this) {
                    final /* synthetic */ CameraDeviceCallbacks this$1;

                    public void run() {
                        if (!this.this$1.this$0.isClosed()) {
                            holder.getCallback().onCaptureFailed(this.this$1.this$0, request, failure);
                        }
                    }
                };
                this.this$0.mFrameNumberTracker.updateTracker(frameNumber, true, request.isReprocess());
                this.this$0.checkAndFireSequenceComplete();
            }
            holder.getHandler().post(failureDispatch);
        }
    }

    static class CaptureCallbackHolder {
        private final CaptureCallback mCallback;
        private final Handler mHandler;
        private final boolean mRepeating;
        private final List<CaptureRequest> mRequestList;
        private final int mSessionId;

        CaptureCallbackHolder(CaptureCallback callback, List<CaptureRequest> requestList, Handler handler, boolean repeating, int sessionId) {
            if (callback == null || handler == null) {
                throw new UnsupportedOperationException("Must have a valid handler and a valid callback");
            }
            this.mRepeating = repeating;
            this.mHandler = handler;
            this.mRequestList = new ArrayList(requestList);
            this.mCallback = callback;
            this.mSessionId = sessionId;
        }

        public boolean isRepeating() {
            return this.mRepeating;
        }

        public CaptureCallback getCallback() {
            return this.mCallback;
        }

        public CaptureRequest getRequest(int subsequenceId) {
            if (subsequenceId >= this.mRequestList.size()) {
                throw new IllegalArgumentException(String.format("Requested subsequenceId %d is larger than request list size %d.", new Object[]{Integer.valueOf(subsequenceId), Integer.valueOf(this.mRequestList.size())}));
            } else if (subsequenceId >= 0) {
                return (CaptureRequest) this.mRequestList.get(subsequenceId);
            } else {
                throw new IllegalArgumentException(String.format("Requested subsequenceId %d is negative", new Object[]{Integer.valueOf(subsequenceId)}));
            }
        }

        public CaptureRequest getRequest() {
            return getRequest(0);
        }

        public Handler getHandler() {
            return this.mHandler;
        }

        public int getSessionId() {
            return this.mSessionId;
        }
    }

    public class FrameNumberTracker {
        private long mCompletedFrameNumber;
        private long mCompletedReprocessFrameNumber;
        private final TreeMap<Long, Boolean> mFutureErrorMap;
        private final HashMap<Long, List<CaptureResult>> mPartialResults;
        private final LinkedList<Long> mSkippedRegularFrameNumbers;
        private final LinkedList<Long> mSkippedReprocessFrameNumbers;
        final /* synthetic */ CameraDeviceImpl this$0;

        public FrameNumberTracker(CameraDeviceImpl this$0) {
            this.this$0 = this$0;
            this.mCompletedFrameNumber = -1;
            this.mCompletedReprocessFrameNumber = -1;
            this.mSkippedRegularFrameNumbers = new LinkedList();
            this.mSkippedReprocessFrameNumbers = new LinkedList();
            this.mFutureErrorMap = new TreeMap();
            this.mPartialResults = new HashMap();
        }

        private void update() {
            Iterator iter = this.mFutureErrorMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry pair = (Entry) iter.next();
                Long errorFrameNumber = (Long) pair.getKey();
                Boolean reprocess = (Boolean) pair.getValue();
                Boolean removeError = Boolean.valueOf(true);
                if (reprocess.booleanValue()) {
                    if (errorFrameNumber.longValue() == this.mCompletedReprocessFrameNumber + 1) {
                        this.mCompletedReprocessFrameNumber = errorFrameNumber.longValue();
                    } else if (this.mSkippedReprocessFrameNumbers.isEmpty() || errorFrameNumber != this.mSkippedReprocessFrameNumbers.element()) {
                        removeError = Boolean.valueOf(false);
                    } else {
                        this.mCompletedReprocessFrameNumber = errorFrameNumber.longValue();
                        this.mSkippedReprocessFrameNumbers.remove();
                    }
                } else if (errorFrameNumber.longValue() == this.mCompletedFrameNumber + 1) {
                    this.mCompletedFrameNumber = errorFrameNumber.longValue();
                } else if (this.mSkippedRegularFrameNumbers.isEmpty() || errorFrameNumber != this.mSkippedRegularFrameNumbers.element()) {
                    removeError = Boolean.valueOf(false);
                } else {
                    this.mCompletedFrameNumber = errorFrameNumber.longValue();
                    this.mSkippedRegularFrameNumbers.remove();
                }
                if (removeError.booleanValue()) {
                    iter.remove();
                }
            }
        }

        public void updateTracker(long frameNumber, boolean isError, boolean isReprocess) {
            if (isError) {
                this.mFutureErrorMap.put(Long.valueOf(frameNumber), Boolean.valueOf(isReprocess));
            } else if (isReprocess) {
                try {
                    updateCompletedReprocessFrameNumber(frameNumber);
                } catch (IllegalArgumentException e) {
                    Log.e(this.this$0.TAG, e.getMessage());
                }
            } else {
                updateCompletedFrameNumber(frameNumber);
            }
            update();
        }

        public void updateTracker(long frameNumber, CaptureResult result, boolean partial, boolean isReprocess) {
            if (!partial) {
                updateTracker(frameNumber, false, isReprocess);
            } else if (result != null) {
                List<CaptureResult> partials = (List) this.mPartialResults.get(Long.valueOf(frameNumber));
                if (partials == null) {
                    partials = new ArrayList();
                    this.mPartialResults.put(Long.valueOf(frameNumber), partials);
                }
                partials.add(result);
            }
        }

        public List<CaptureResult> popPartialResults(long frameNumber) {
            return (List) this.mPartialResults.remove(Long.valueOf(frameNumber));
        }

        public long getCompletedFrameNumber() {
            return this.mCompletedFrameNumber;
        }

        public long getCompletedReprocessFrameNumber() {
            return this.mCompletedReprocessFrameNumber;
        }

        private void updateCompletedFrameNumber(long frameNumber) throws IllegalArgumentException {
            if (frameNumber <= this.mCompletedFrameNumber) {
                throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
            }
            if (frameNumber > this.mCompletedReprocessFrameNumber) {
                for (long i = Math.max(this.mCompletedFrameNumber, this.mCompletedReprocessFrameNumber) + 1; i < frameNumber; i++) {
                    this.mSkippedReprocessFrameNumbers.add(Long.valueOf(i));
                }
            } else if (this.mSkippedRegularFrameNumbers.isEmpty() || frameNumber < ((Long) this.mSkippedRegularFrameNumbers.element()).longValue()) {
                throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
            } else if (frameNumber > ((Long) this.mSkippedRegularFrameNumbers.element()).longValue()) {
                throw new IllegalArgumentException("frame number " + frameNumber + " comes out of order. Expecting " + this.mSkippedRegularFrameNumbers.element());
            } else {
                this.mSkippedRegularFrameNumbers.remove();
            }
            this.mCompletedFrameNumber = frameNumber;
        }

        private void updateCompletedReprocessFrameNumber(long frameNumber) throws IllegalArgumentException {
            if (frameNumber < this.mCompletedReprocessFrameNumber) {
                throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
            }
            if (frameNumber >= this.mCompletedFrameNumber) {
                for (long i = Math.max(this.mCompletedFrameNumber, this.mCompletedReprocessFrameNumber) + 1; i < frameNumber; i++) {
                    this.mSkippedRegularFrameNumbers.add(Long.valueOf(i));
                }
            } else if (this.mSkippedReprocessFrameNumbers.isEmpty() || frameNumber < ((Long) this.mSkippedReprocessFrameNumbers.element()).longValue()) {
                throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
            } else if (frameNumber > ((Long) this.mSkippedReprocessFrameNumbers.element()).longValue()) {
                throw new IllegalArgumentException("frame number " + frameNumber + " comes out of order. Expecting " + this.mSkippedReprocessFrameNumbers.element());
            } else {
                this.mSkippedReprocessFrameNumbers.remove();
            }
            this.mCompletedReprocessFrameNumber = frameNumber;
        }
    }

    static class RequestLastFrameNumbersHolder {
        private final long mLastRegularFrameNumber;
        private final long mLastReprocessFrameNumber;
        private final int mRequestId;

        public RequestLastFrameNumbersHolder(List<CaptureRequest> requestList, SubmitInfo requestInfo) {
            long lastRegularFrameNumber = -1;
            long lastReprocessFrameNumber = -1;
            long frameNumber = requestInfo.getLastFrameNumber();
            if (requestInfo.getLastFrameNumber() < ((long) (requestList.size() - 1))) {
                throw new IllegalArgumentException("lastFrameNumber: " + requestInfo.getLastFrameNumber() + " should be at least " + (requestList.size() - 1) + " for the number of " + " requests in the list: " + requestList.size());
            }
            for (int i = requestList.size() - 1; i >= 0; i--) {
                CaptureRequest request = (CaptureRequest) requestList.get(i);
                if (request.isReprocess() && lastReprocessFrameNumber == -1) {
                    lastReprocessFrameNumber = frameNumber;
                } else if (!request.isReprocess() && lastRegularFrameNumber == -1) {
                    lastRegularFrameNumber = frameNumber;
                }
                if (lastReprocessFrameNumber != -1 && lastRegularFrameNumber != -1) {
                    break;
                }
                frameNumber--;
            }
            this.mLastRegularFrameNumber = lastRegularFrameNumber;
            this.mLastReprocessFrameNumber = lastReprocessFrameNumber;
            this.mRequestId = requestInfo.getRequestId();
        }

        public RequestLastFrameNumbersHolder(int requestId, long lastRegularFrameNumber) {
            this.mLastRegularFrameNumber = lastRegularFrameNumber;
            this.mLastReprocessFrameNumber = -1;
            this.mRequestId = requestId;
        }

        public long getLastRegularFrameNumber() {
            return this.mLastRegularFrameNumber;
        }

        public long getLastReprocessFrameNumber() {
            return this.mLastReprocessFrameNumber;
        }

        public long getLastFrameNumber() {
            return Math.max(this.mLastRegularFrameNumber, this.mLastReprocessFrameNumber);
        }

        public int getRequestId() {
            return this.mRequestId;
        }
    }

    public CameraDeviceImpl(String cameraId, StateCallback callback, Handler handler, CameraCharacteristics characteristics) {
        this.DEBUG = false;
        this.mInterfaceLock = new Object();
        this.mCallbacks = new CameraDeviceCallbacks(this);
        this.mClosing = new AtomicBoolean();
        this.mInError = false;
        this.mIdle = true;
        this.mCaptureCallbackMap = new SparseArray();
        this.mRepeatingRequestId = -1;
        this.mConfiguredInput = new SimpleEntry(Integer.valueOf(-1), null);
        this.mConfiguredOutputs = new SparseArray();
        this.mRequestLastFrameNumbersList = new ArrayList();
        this.mFrameNumberTracker = new FrameNumberTracker(this);
        this.mNextSessionId = 0;
        this.mCallOnOpened = new AnonymousClass1(this);
        this.mCallOnUnconfigured = new AnonymousClass2(this);
        this.mCallOnActive = new AnonymousClass3(this);
        this.mCallOnBusy = new AnonymousClass4(this);
        this.mCallOnClosed = new AnonymousClass5(this);
        this.mCallOnIdle = new AnonymousClass6(this);
        this.mCallOnDisconnected = new AnonymousClass7(this);
        if (cameraId == null || callback == null || handler == null || characteristics == null) {
            throw new IllegalArgumentException("Null argument given");
        }
        this.mCameraId = cameraId;
        this.mDeviceCallback = callback;
        this.mDeviceHandler = handler;
        this.mCharacteristics = characteristics;
        String tag = String.format("CameraDevice-JV-%s", new Object[]{this.mCameraId});
        if (tag.length() > 23) {
            tag = tag.substring(0, 23);
        }
        this.TAG = tag;
        Integer partialCount = (Integer) this.mCharacteristics.get(CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT);
        if (partialCount == null) {
            this.mTotalPartialCount = 1;
        } else {
            this.mTotalPartialCount = partialCount.intValue();
        }
    }

    public CameraDeviceCallbacks getCallbacks() {
        return this.mCallbacks;
    }

    public void setRemoteDevice(ICameraDeviceUser remoteDevice) throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            if (this.mInError) {
                return;
            }
            this.mRemoteDevice = new ICameraDeviceUserWrapper(remoteDevice);
            IBinder remoteDeviceBinder = remoteDevice.asBinder();
            if (remoteDeviceBinder != null) {
                try {
                    remoteDeviceBinder.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    this.mDeviceHandler.post(this.mCallOnDisconnected);
                    throw new CameraAccessException(2, "The camera device has encountered a serious error");
                }
            }
            this.mDeviceHandler.post(this.mCallOnOpened);
            this.mDeviceHandler.post(this.mCallOnUnconfigured);
        }
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void setRemoteFailure(android.os.ServiceSpecificException r8) {
        /*
        r7 = this;
        r1 = 4;
        r2 = 1;
        r4 = r8.errorCode;
        switch(r4) {
            case 4: goto L_0x0044;
            case 5: goto L_0x0007;
            case 6: goto L_0x0042;
            case 7: goto L_0x003e;
            case 8: goto L_0x0040;
            case 9: goto L_0x0007;
            case 10: goto L_0x0046;
            default: goto L_0x0007;
        };
    L_0x0007:
        r4 = r7.TAG;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Unexpected failure in opening camera device: ";
        r5 = r5.append(r6);
        r6 = r8.errorCode;
        r5 = r5.append(r6);
        r6 = r8.getMessage();
        r5 = r5.append(r6);
        r5 = r5.toString();
        android.util.Log.e(r4, r5);
    L_0x002a:
        r0 = r1;
        r3 = r2;
        r5 = r7.mInterfaceLock;
        monitor-enter(r5);
        r4 = 1;
        r7.mInError = r4;	 Catch:{ all -> 0x0048 }
        r4 = r7.mDeviceHandler;	 Catch:{ all -> 0x0048 }
        r6 = new android.hardware.camera2.impl.CameraDeviceImpl$8;	 Catch:{ all -> 0x0048 }
        r6.<init>(r7, r3, r0);	 Catch:{ all -> 0x0048 }
        r4.post(r6);	 Catch:{ all -> 0x0048 }
        monitor-exit(r5);
        return;
    L_0x003e:
        r1 = 1;
        goto L_0x002a;
    L_0x0040:
        r1 = 2;
        goto L_0x002a;
    L_0x0042:
        r1 = 3;
        goto L_0x002a;
    L_0x0044:
        r2 = 0;
        goto L_0x002a;
    L_0x0046:
        r1 = 4;
        goto L_0x002a;
    L_0x0048:
        r4 = move-exception;
        monitor-exit(r5);
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.setRemoteFailure(android.os.ServiceSpecificException):void");
    }

    public String getId() {
        return this.mCameraId;
    }

    public void configureOutputs(List<Surface> outputs) throws CameraAccessException {
        ArrayList<OutputConfiguration> outputConfigs = new ArrayList(outputs.size());
        for (Surface s : outputs) {
            outputConfigs.add(new OutputConfiguration(s));
        }
        configureStreamsChecked(null, outputConfigs, false);
    }

    public boolean configureStreamsChecked(InputConfiguration inputConfig, List<OutputConfiguration> outputs, boolean isConstrainedHighSpeed) throws CameraAccessException {
        if (outputs == null) {
            outputs = new ArrayList();
        }
        if (outputs.size() != 0 || inputConfig == null) {
            checkInputConfiguration(inputConfig);
            synchronized (this.mInterfaceLock) {
                OutputConfiguration outConfig;
                checkIfCameraClosedOrInError();
                HashSet<OutputConfiguration> addSet = new HashSet(outputs);
                List<Integer> deleteList = new ArrayList();
                for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                    int streamId = this.mConfiguredOutputs.keyAt(i);
                    outConfig = (OutputConfiguration) this.mConfiguredOutputs.valueAt(i);
                    if (!outputs.contains(outConfig) || outConfig.isDeferredConfiguration()) {
                        deleteList.add(Integer.valueOf(streamId));
                    } else {
                        addSet.remove(outConfig);
                    }
                }
                this.mDeviceHandler.post(this.mCallOnBusy);
                stopRepeating();
                try {
                    waitUntilIdle();
                    this.mRemoteDevice.beginConfigure();
                    InputConfiguration currentInputConfig = (InputConfiguration) this.mConfiguredInput.getValue();
                    if (inputConfig != currentInputConfig && (inputConfig == null || !inputConfig.equals(currentInputConfig))) {
                        if (currentInputConfig != null) {
                            this.mRemoteDevice.deleteStream(((Integer) this.mConfiguredInput.getKey()).intValue());
                            this.mConfiguredInput = new SimpleEntry(Integer.valueOf(-1), null);
                        }
                        if (inputConfig != null) {
                            this.mConfiguredInput = new SimpleEntry(Integer.valueOf(this.mRemoteDevice.createInputStream(inputConfig.getWidth(), inputConfig.getHeight(), inputConfig.getFormat())), inputConfig);
                        }
                    }
                    for (Integer streamId2 : deleteList) {
                        this.mRemoteDevice.deleteStream(streamId2.intValue());
                        this.mConfiguredOutputs.delete(streamId2.intValue());
                    }
                    for (OutputConfiguration outConfig2 : outputs) {
                        if (addSet.contains(outConfig2)) {
                            this.mConfiguredOutputs.put(this.mRemoteDevice.createStream(outConfig2), outConfig2);
                        }
                    }
                    this.mRemoteDevice.endConfigure(isConstrainedHighSpeed);
                    if (1 != null) {
                        if (outputs.size() > 0) {
                            this.mDeviceHandler.post(this.mCallOnIdle);
                        }
                    }
                    this.mDeviceHandler.post(this.mCallOnUnconfigured);
                } catch (IllegalArgumentException e) {
                    Log.w(this.TAG, "Stream configuration failed due to: " + e.getMessage());
                    if (null != null) {
                        if (outputs.size() > 0) {
                            this.mDeviceHandler.post(this.mCallOnIdle);
                            return false;
                        }
                    }
                    this.mDeviceHandler.post(this.mCallOnUnconfigured);
                    return false;
                } catch (CameraAccessException e2) {
                    if (e2.getReason() == 4) {
                        throw new IllegalStateException("The camera is currently busy. You must wait until the previous operation completes.", e2);
                    }
                    throw e2;
                } catch (Throwable th) {
                    if (null != null) {
                        if (outputs.size() > 0) {
                            this.mDeviceHandler.post(this.mCallOnIdle);
                        }
                    }
                    this.mDeviceHandler.post(this.mCallOnUnconfigured);
                }
            }
            return true;
        }
        throw new IllegalArgumentException("cannot configure an input stream without any output streams");
    }

    public void createCaptureSession(List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        List<OutputConfiguration> outConfigurations = new ArrayList(outputs.size());
        for (Surface surface : outputs) {
            outConfigurations.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(null, outConfigurations, callback, handler, false);
    }

    public void createCaptureSessionByOutputConfigurations(List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        createCaptureSessionInternal(null, new ArrayList(outputConfigurations), callback, handler, false);
    }

    public void createReprocessableCaptureSession(InputConfiguration inputConfig, List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (inputConfig == null) {
            throw new IllegalArgumentException("inputConfig cannot be null when creating a reprocessable capture session");
        }
        List<OutputConfiguration> outConfigurations = new ArrayList(outputs.size());
        for (Surface surface : outputs) {
            outConfigurations.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(inputConfig, outConfigurations, callback, handler, false);
    }

    public void createReprocessableCaptureSessionByConfigurations(InputConfiguration inputConfig, List<OutputConfiguration> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (inputConfig == null) {
            throw new IllegalArgumentException("inputConfig cannot be null when creating a reprocessable capture session");
        } else if (outputs == null) {
            throw new IllegalArgumentException("Output configurations cannot be null when creating a reprocessable capture session");
        } else {
            List<OutputConfiguration> currentOutputs = new ArrayList();
            for (OutputConfiguration output : outputs) {
                currentOutputs.add(new OutputConfiguration(output));
            }
            createCaptureSessionInternal(inputConfig, currentOutputs, callback, handler, false);
        }
    }

    public void createConstrainedHighSpeedCaptureSession(List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (outputs == null || outputs.size() == 0 || outputs.size() > 2) {
            throw new IllegalArgumentException("Output surface list must not be null and the size must be no more than 2");
        }
        SurfaceUtils.checkConstrainedHighSpeedSurfaces(outputs, null, (StreamConfigurationMap) getCharacteristics().get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP));
        List<OutputConfiguration> outConfigurations = new ArrayList(outputs.size());
        for (Surface surface : outputs) {
            outConfigurations.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(null, outConfigurations, callback, handler, true);
    }

    private void createCaptureSessionInternal(InputConfiguration inputConfig, List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Handler handler, boolean isConstrainedHighSpeed) throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (!isConstrainedHighSpeed || inputConfig == null) {
                boolean configureSuccess;
                CameraCaptureSessionCore newSession;
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.replaceSessionClose();
                }
                CameraAccessException pendingException = null;
                Surface input = null;
                try {
                    configureSuccess = configureStreamsChecked(inputConfig, outputConfigurations, isConstrainedHighSpeed);
                    if (configureSuccess && inputConfig != null) {
                        input = this.mRemoteDevice.getInputSurface();
                    }
                } catch (CameraAccessException e) {
                    configureSuccess = false;
                    pendingException = e;
                    input = null;
                }
                List<Surface> outSurfaces = new ArrayList(outputConfigurations.size());
                for (OutputConfiguration config : outputConfigurations) {
                    outSurfaces.add(config.getSurface());
                }
                if (isConstrainedHighSpeed) {
                    int i = this.mNextSessionId;
                    this.mNextSessionId = i + 1;
                    newSession = new CameraConstrainedHighSpeedCaptureSessionImpl(i, outSurfaces, callback, handler, this, this.mDeviceHandler, configureSuccess, this.mCharacteristics);
                } else {
                    int i2 = this.mNextSessionId;
                    this.mNextSessionId = i2 + 1;
                    CameraCaptureSessionCore cameraCaptureSessionImpl = new CameraCaptureSessionImpl(i2, input, outSurfaces, callback, handler, this, this.mDeviceHandler, configureSuccess);
                }
                this.mCurrentSession = newSession;
                if (pendingException != null) {
                    throw pendingException;
                }
                this.mSessionStateCallback = this.mCurrentSession.getDeviceStateCallback();
            } else {
                throw new IllegalArgumentException("Constrained high speed session doesn't support input configuration yet.");
            }
        }
    }

    public void setSessionListener(StateCallbackKK sessionCallback) {
        synchronized (this.mInterfaceLock) {
            this.mSessionStateCallback = sessionCallback;
        }
    }

    public Builder createCaptureRequest(int templateType) throws CameraAccessException {
        Builder builder;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            builder = new Builder(this.mRemoteDevice.createDefaultRequest(templateType), false, -1);
        }
        return builder;
    }

    public Builder createReprocessCaptureRequest(TotalCaptureResult inputResult) throws CameraAccessException {
        Builder builder;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            builder = new Builder(new CameraMetadataNative(inputResult.getNativeCopy()), true, inputResult.getSessionId());
        }
        return builder;
    }

    public void prepare(Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        }
        synchronized (this.mInterfaceLock) {
            int streamId = -1;
            for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                if (surface == ((OutputConfiguration) this.mConfiguredOutputs.valueAt(i)).getSurface()) {
                    streamId = this.mConfiguredOutputs.keyAt(i);
                    break;
                }
            }
            if (streamId == -1) {
                throw new IllegalArgumentException("Surface is not part of this session");
            }
            this.mRemoteDevice.prepare(streamId);
        }
    }

    public void prepare(int maxCount, Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        } else if (maxCount <= 0) {
            throw new IllegalArgumentException("Invalid maxCount given: " + maxCount);
        } else {
            synchronized (this.mInterfaceLock) {
                int streamId = -1;
                for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                    if (surface == ((OutputConfiguration) this.mConfiguredOutputs.valueAt(i)).getSurface()) {
                        streamId = this.mConfiguredOutputs.keyAt(i);
                        break;
                    }
                }
                if (streamId == -1) {
                    throw new IllegalArgumentException("Surface is not part of this session");
                }
                this.mRemoteDevice.prepare2(maxCount, streamId);
            }
        }
    }

    public void tearDown(Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        }
        synchronized (this.mInterfaceLock) {
            int streamId = -1;
            for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                if (surface == ((OutputConfiguration) this.mConfiguredOutputs.valueAt(i)).getSurface()) {
                    streamId = this.mConfiguredOutputs.keyAt(i);
                    break;
                }
            }
            if (streamId == -1) {
                throw new IllegalArgumentException("Surface is not part of this session");
            }
            this.mRemoteDevice.tearDown(streamId);
        }
    }

    public void finishDeferredConfig(List<OutputConfiguration> deferredConfigs) throws CameraAccessException {
        if (deferredConfigs == null || deferredConfigs.size() == 0) {
            throw new IllegalArgumentException("deferred config is null or empty");
        }
        synchronized (this.mInterfaceLock) {
            for (OutputConfiguration config : deferredConfigs) {
                int streamId = -1;
                for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                    if (config.equals(this.mConfiguredOutputs.valueAt(i))) {
                        streamId = this.mConfiguredOutputs.keyAt(i);
                        break;
                    }
                }
                if (streamId == -1) {
                    throw new IllegalArgumentException("Deferred config is not part of this session");
                } else if (config.getSurface() == null) {
                    throw new IllegalArgumentException("The deferred config for stream " + streamId + " must have a non-null surface");
                } else {
                    this.mRemoteDevice.setDeferredConfiguration(streamId, config);
                }
            }
        }
    }

    public int capture(CaptureRequest request, CaptureCallback callback, Handler handler) throws CameraAccessException {
        List<CaptureRequest> requestList = new ArrayList();
        requestList.add(request);
        return submitCaptureRequest(requestList, callback, handler, false);
    }

    public int captureBurst(List<CaptureRequest> requests, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (requests != null && !requests.isEmpty()) {
            return submitCaptureRequest(requests, callback, handler, false);
        }
        throw new IllegalArgumentException("At least one request must be given");
    }

    private void checkEarlyTriggerSequenceComplete(final int requestId, long lastFrameNumber) {
        CaptureCallbackHolder holder = null;
        if (lastFrameNumber == -1) {
            int index = this.mCaptureCallbackMap.indexOfKey(requestId);
            if (index >= 0) {
                holder = (CaptureCallbackHolder) this.mCaptureCallbackMap.valueAt(index);
            }
            if (holder != null) {
                this.mCaptureCallbackMap.removeAt(index);
            }
            if (holder != null) {
                holder.getHandler().post(new Runnable(this) {
                    final /* synthetic */ CameraDeviceImpl this$0;

                    public void run() {
                        if (!this.this$0.isClosed()) {
                            holder.getCallback().onCaptureSequenceAborted(this.this$0, requestId);
                        }
                    }
                });
                return;
            }
            Log.w(this.TAG, String.format("did not register callback to request %d", new Object[]{Integer.valueOf(requestId)}));
            return;
        }
        this.mRequestLastFrameNumbersList.add(new RequestLastFrameNumbersHolder(requestId, lastFrameNumber));
        checkAndFireSequenceComplete();
    }

    private int submitCaptureRequest(List<CaptureRequest> requestList, CaptureCallback callback, Handler handler, boolean repeating) throws CameraAccessException {
        int requestId;
        handler = checkHandler(handler, callback);
        for (CaptureRequest request : requestList) {
            if (request.getTargets().isEmpty()) {
                throw new IllegalArgumentException("Each request must have at least one Surface target");
            }
            for (Surface surface : request.getTargets()) {
                if (surface == null) {
                    throw new IllegalArgumentException("Null Surface targets are not allowed");
                }
            }
        }
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (repeating) {
                stopRepeating();
            }
            SubmitInfo requestInfo = this.mRemoteDevice.submitRequestList((CaptureRequest[]) requestList.toArray(new CaptureRequest[requestList.size()]), repeating);
            if (callback != null) {
                SparseArray sparseArray = this.mCaptureCallbackMap;
                int requestId2 = requestInfo.getRequestId();
                sparseArray.put(requestId2, new CaptureCallbackHolder(callback, requestList, handler, repeating, this.mNextSessionId - 1));
            }
            if (repeating) {
                if (this.mRepeatingRequestId != -1) {
                    checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, requestInfo.getLastFrameNumber());
                }
                this.mRepeatingRequestId = requestInfo.getRequestId();
            } else {
                this.mRequestLastFrameNumbersList.add(new RequestLastFrameNumbersHolder((List) requestList, requestInfo));
            }
            if (this.mIdle) {
                this.mDeviceHandler.post(this.mCallOnActive);
            }
            this.mIdle = false;
            requestId = requestInfo.getRequestId();
        }
        return requestId;
    }

    public int setRepeatingRequest(CaptureRequest request, CaptureCallback callback, Handler handler) throws CameraAccessException {
        List<CaptureRequest> requestList = new ArrayList();
        requestList.add(request);
        return submitCaptureRequest(requestList, callback, handler, true);
    }

    public int setRepeatingBurst(List<CaptureRequest> requests, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (requests != null && !requests.isEmpty()) {
            return submitCaptureRequest(requests, callback, handler, true);
        }
        throw new IllegalArgumentException("At least one request must be given");
    }

    public void stopRepeating() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (this.mRepeatingRequestId != -1) {
                int requestId = this.mRepeatingRequestId;
                this.mRepeatingRequestId = -1;
                try {
                    checkEarlyTriggerSequenceComplete(requestId, this.mRemoteDevice.cancelRequest(requestId));
                } catch (IllegalArgumentException e) {
                    return;
                }
            }
        }
    }

    private void waitUntilIdle() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (this.mRepeatingRequestId != -1) {
                throw new IllegalStateException("Active repeating request ongoing");
            }
            this.mRemoteDevice.waitUntilIdle();
        }
    }

    /* JADX WARNING: Missing block: B:13:0x002e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void flush() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            this.mDeviceHandler.post(this.mCallOnBusy);
            if (this.mIdle) {
                this.mDeviceHandler.post(this.mCallOnIdle);
                return;
            }
            long lastFrameNumber = this.mRemoteDevice.flush();
            if (this.mRepeatingRequestId != -1) {
                checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, lastFrameNumber);
                this.mRepeatingRequestId = -1;
            }
        }
    }

    public void close() {
        synchronized (this.mInterfaceLock) {
            if (this.mClosing.getAndSet(true)) {
                return;
            }
            if (this.mRemoteDevice != null) {
                this.mRemoteDevice.disconnect();
                this.mRemoteDevice.unlinkToDeath(this, 0);
            }
            if (this.mRemoteDevice != null || this.mInError) {
                this.mDeviceHandler.post(this.mCallOnClosed);
            }
            this.mRemoteDevice = null;
        }
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private void checkInputConfiguration(InputConfiguration inputConfig) {
        int i = 0;
        if (inputConfig != null) {
            int length;
            StreamConfigurationMap configMap = (StreamConfigurationMap) this.mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            boolean validFormat = false;
            for (int format : configMap.getInputFormats()) {
                if (format == inputConfig.getFormat()) {
                    validFormat = true;
                }
            }
            if (validFormat) {
                boolean validSize = false;
                Size[] inputSizes = configMap.getInputSizes(inputConfig.getFormat());
                length = inputSizes.length;
                while (i < length) {
                    Size s = inputSizes[i];
                    if (inputConfig.getWidth() == s.getWidth() && inputConfig.getHeight() == s.getHeight()) {
                        validSize = true;
                    }
                    i++;
                }
                if (!validSize) {
                    throw new IllegalArgumentException("input size " + inputConfig.getWidth() + "x" + inputConfig.getHeight() + " is not valid");
                }
                return;
            }
            throw new IllegalArgumentException("input format " + inputConfig.getFormat() + " is not valid");
        }
    }

    /* JADX WARNING: Missing block: B:22:0x008e, code:
            if (r6 == null) goto L_0x0092;
     */
    /* JADX WARNING: Missing block: B:23:0x0090, code:
            if (r17 == false) goto L_0x0095;
     */
    /* JADX WARNING: Missing block: B:24:0x0092, code:
            r9.remove();
     */
    /* JADX WARNING: Missing block: B:25:0x0095, code:
            if (r17 == false) goto L_0x001f;
     */
    /* JADX WARNING: Missing block: B:26:0x0097, code:
            r6.getHandler().post(new android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass10(r21));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkAndFireSequenceComplete() {
        long completedFrameNumber = this.mFrameNumberTracker.getCompletedFrameNumber();
        long completedReprocessFrameNumber = this.mFrameNumberTracker.getCompletedReprocessFrameNumber();
        Iterator<RequestLastFrameNumbersHolder> iter = this.mRequestLastFrameNumbersList.iterator();
        while (iter.hasNext()) {
            final RequestLastFrameNumbersHolder requestLastFrameNumbers = (RequestLastFrameNumbersHolder) iter.next();
            boolean sequenceCompleted = false;
            final int requestId = requestLastFrameNumbers.getRequestId();
            synchronized (this.mInterfaceLock) {
                if (this.mRemoteDevice == null) {
                    Log.w(this.TAG, "Camera closed while checking sequences");
                    return;
                }
                int index = this.mCaptureCallbackMap.indexOfKey(requestId);
                final CaptureCallbackHolder holder = index >= 0 ? (CaptureCallbackHolder) this.mCaptureCallbackMap.valueAt(index) : null;
                if (holder != null) {
                    long lastRegularFrameNumber = requestLastFrameNumbers.getLastRegularFrameNumber();
                    long lastReprocessFrameNumber = requestLastFrameNumbers.getLastReprocessFrameNumber();
                    if (lastRegularFrameNumber <= completedFrameNumber && lastReprocessFrameNumber <= completedReprocessFrameNumber) {
                        sequenceCompleted = true;
                        this.mCaptureCallbackMap.removeAt(index);
                    }
                }
            }
        }
    }

    static Handler checkHandler(Handler handler) {
        if (handler != null) {
            return handler;
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            return new Handler(looper);
        }
        throw new IllegalArgumentException("No handler given, and current thread has no looper!");
    }

    static <T> Handler checkHandler(Handler handler, T callback) {
        if (callback != null) {
            return checkHandler(handler);
        }
        return handler;
    }

    private void checkIfCameraClosedOrInError() throws CameraAccessException {
        if (this.mRemoteDevice == null) {
            throw new IllegalStateException("CameraDevice was already closed");
        } else if (this.mInError) {
            throw new CameraAccessException(3, "The camera device has encountered a serious error");
        }
    }

    private boolean isClosed() {
        return this.mClosing.get();
    }

    private CameraCharacteristics getCharacteristics() {
        return this.mCharacteristics;
    }

    public void binderDied() {
        Log.w(this.TAG, "CameraDevice " + this.mCameraId + " died unexpectedly");
        if (this.mRemoteDevice != null) {
            this.mInError = true;
            this.mDeviceHandler.post(new AnonymousClass11(this));
        }
    }
}
