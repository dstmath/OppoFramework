package android.hardware.camera2.legacy;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.legacy.CameraDeviceState.CameraDeviceStateListener;
import android.hardware.camera2.legacy.LegacyExceptionUtils.BufferQueueAbandonedException;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.utils.ArrayUtils;
import android.hardware.camera2.utils.SubmitInfo;
import android.opengl.GLES11;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ServiceSpecificException;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public class LegacyCameraDevice implements AutoCloseable {
    private static final boolean DEBUG = false;
    private static final int GRALLOC_USAGE_HW_COMPOSER = 2048;
    private static final int GRALLOC_USAGE_HW_RENDER = 512;
    private static final int GRALLOC_USAGE_HW_TEXTURE = 256;
    private static final int GRALLOC_USAGE_HW_VIDEO_ENCODER = 65536;
    private static final int GRALLOC_USAGE_RENDERSCRIPT = 1048576;
    private static final int GRALLOC_USAGE_SW_READ_OFTEN = 3;
    private static final int ILLEGAL_VALUE = -1;
    public static final int MAX_DIMEN_FOR_ROUNDING = 1920;
    public static final int NATIVE_WINDOW_SCALING_MODE_SCALE_TO_WINDOW = 1;
    private final String TAG;
    private final Handler mCallbackHandler;
    private final HandlerThread mCallbackHandlerThread;
    private final int mCameraId;
    private boolean mClosed;
    private SparseArray<Surface> mConfiguredSurfaces;
    private final ICameraDeviceCallbacks mDeviceCallbacks;
    private final CameraDeviceState mDeviceState;
    private final ConditionVariable mIdle;
    private final RequestThreadManager mRequestThreadManager;
    private final Handler mResultHandler;
    private final HandlerThread mResultThread;
    private final CameraDeviceStateListener mStateListener;
    private final CameraCharacteristics mStaticCharacteristics;

    /* renamed from: android.hardware.camera2.legacy.LegacyCameraDevice$1 */
    class AnonymousClass1 implements CameraDeviceStateListener {
        final /* synthetic */ LegacyCameraDevice this$0;

        /* renamed from: android.hardware.camera2.legacy.LegacyCameraDevice$1$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ AnonymousClass1 this$1;
            final /* synthetic */ int val$errorCode;
            final /* synthetic */ CaptureResultExtras val$extras;
            final /* synthetic */ RequestHolder val$holder;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.1.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, android.hardware.camera2.legacy.RequestHolder, int, android.hardware.camera2.impl.CaptureResultExtras):void, dex: 
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
            AnonymousClass1(android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1 r1, android.hardware.camera2.legacy.RequestHolder r2, int r3, android.hardware.camera2.impl.CaptureResultExtras r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.1.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, android.hardware.camera2.legacy.RequestHolder, int, android.hardware.camera2.impl.CaptureResultExtras):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.1.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, android.hardware.camera2.legacy.RequestHolder, int, android.hardware.camera2.impl.CaptureResultExtras):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.1.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.1.run():void");
            }
        }

        /* renamed from: android.hardware.camera2.legacy.LegacyCameraDevice$1$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ AnonymousClass1 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.2.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1):void, dex: 
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
            AnonymousClass2(android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1 r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.2.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.2.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.2.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.2.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.2.run():void");
            }
        }

        /* renamed from: android.hardware.camera2.legacy.LegacyCameraDevice$1$3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ AnonymousClass1 this$1;
            final /* synthetic */ CaptureResultExtras val$extras;
            final /* synthetic */ RequestHolder val$holder;
            final /* synthetic */ long val$timestamp;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.3.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, android.hardware.camera2.legacy.RequestHolder, android.hardware.camera2.impl.CaptureResultExtras, long):void, dex: 
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
            AnonymousClass3(android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1 r1, android.hardware.camera2.legacy.RequestHolder r2, android.hardware.camera2.impl.CaptureResultExtras r3, long r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.3.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, android.hardware.camera2.legacy.RequestHolder, android.hardware.camera2.impl.CaptureResultExtras, long):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.3.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, android.hardware.camera2.legacy.RequestHolder, android.hardware.camera2.impl.CaptureResultExtras, long):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.3.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.3.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.3.run():void");
            }
        }

        /* renamed from: android.hardware.camera2.legacy.LegacyCameraDevice$1$4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ AnonymousClass1 this$1;
            final /* synthetic */ CaptureResultExtras val$extras;
            final /* synthetic */ RequestHolder val$holder;
            final /* synthetic */ CameraMetadataNative val$result;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.4.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, android.hardware.camera2.legacy.RequestHolder, android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.impl.CaptureResultExtras):void, dex: 
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
            AnonymousClass4(android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1 r1, android.hardware.camera2.legacy.RequestHolder r2, android.hardware.camera2.impl.CameraMetadataNative r3, android.hardware.camera2.impl.CaptureResultExtras r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.4.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, android.hardware.camera2.legacy.RequestHolder, android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.impl.CaptureResultExtras):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.4.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, android.hardware.camera2.legacy.RequestHolder, android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.impl.CaptureResultExtras):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.4.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.4.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.4.run():void");
            }
        }

        /* renamed from: android.hardware.camera2.legacy.LegacyCameraDevice$1$5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ AnonymousClass1 this$1;
            final /* synthetic */ long val$lastFrameNumber;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.5.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, long):void, dex: 
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
            AnonymousClass5(android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1 r1, long r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.5.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, long):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.5.<init>(android.hardware.camera2.legacy.LegacyCameraDevice$1, long):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.5.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.5.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.5.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.<init>(android.hardware.camera2.legacy.LegacyCameraDevice):void, dex: 
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
        AnonymousClass1(android.hardware.camera2.legacy.LegacyCameraDevice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.<init>(android.hardware.camera2.legacy.LegacyCameraDevice):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.<init>(android.hardware.camera2.legacy.LegacyCameraDevice):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onBusy():void, dex: 
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
        public void onBusy() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onBusy():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.onBusy():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onCaptureResult(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.legacy.RequestHolder):void, dex: 
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
        public void onCaptureResult(android.hardware.camera2.impl.CameraMetadataNative r1, android.hardware.camera2.legacy.RequestHolder r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onCaptureResult(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.legacy.RequestHolder):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.onCaptureResult(android.hardware.camera2.impl.CameraMetadataNative, android.hardware.camera2.legacy.RequestHolder):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onCaptureStarted(android.hardware.camera2.legacy.RequestHolder, long):void, dex: 
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
        public void onCaptureStarted(android.hardware.camera2.legacy.RequestHolder r1, long r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onCaptureStarted(android.hardware.camera2.legacy.RequestHolder, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.onCaptureStarted(android.hardware.camera2.legacy.RequestHolder, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onConfiguring():void, dex: 
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
        public void onConfiguring() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onConfiguring():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.onConfiguring():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onError(int, java.lang.Object, android.hardware.camera2.legacy.RequestHolder):void, dex: 
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
        public void onError(int r1, java.lang.Object r2, android.hardware.camera2.legacy.RequestHolder r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onError(int, java.lang.Object, android.hardware.camera2.legacy.RequestHolder):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.onError(int, java.lang.Object, android.hardware.camera2.legacy.RequestHolder):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onIdle():void, dex: 
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
        public void onIdle() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onIdle():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.onIdle():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onRepeatingRequestError(long):void, dex: 
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
        public void onRepeatingRequestError(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.camera2.legacy.LegacyCameraDevice.1.onRepeatingRequestError(long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.1.onRepeatingRequestError(long):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.camera2.legacy.LegacyCameraDevice.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.camera2.legacy.LegacyCameraDevice.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyCameraDevice.<clinit>():void");
    }

    private static native int nativeConnectSurface(Surface surface);

    private static native int nativeDetectSurfaceDataspace(Surface surface);

    private static native int nativeDetectSurfaceDimens(Surface surface, int[] iArr);

    private static native int nativeDetectSurfaceType(Surface surface);

    private static native int nativeDetectSurfaceUsageFlags(Surface surface);

    private static native int nativeDetectTextureDimens(SurfaceTexture surfaceTexture, int[] iArr);

    private static native int nativeDisconnectSurface(Surface surface);

    static native int nativeGetJpegFooterSize();

    private static native long nativeGetSurfaceId(Surface surface);

    private static native int nativeProduceFrame(Surface surface, byte[] bArr, int i, int i2, int i3);

    private static native int nativeSetNextTimestamp(Surface surface, long j);

    private static native int nativeSetScalingMode(Surface surface, int i);

    private static native int nativeSetSurfaceDimens(Surface surface, int i, int i2);

    private static native int nativeSetSurfaceFormat(Surface surface, int i);

    private static native int nativeSetSurfaceOrientation(Surface surface, int i, int i2);

    private CaptureResultExtras getExtrasFromRequest(RequestHolder holder) {
        return getExtrasFromRequest(holder, -1, null);
    }

    private CaptureResultExtras getExtrasFromRequest(RequestHolder holder, int errorCode, Object errorArg) {
        int errorStreamId = -1;
        if (errorCode == 5) {
            int indexOfTarget = this.mConfiguredSurfaces.indexOfValue((Surface) errorArg);
            if (indexOfTarget < 0) {
                Log.e(this.TAG, "Buffer drop error reported for unknown Surface");
            } else {
                errorStreamId = this.mConfiguredSurfaces.keyAt(indexOfTarget);
            }
        }
        if (holder == null) {
            return new CaptureResultExtras(-1, -1, -1, -1, -1, -1, -1);
        }
        return new CaptureResultExtras(holder.getRequestId(), holder.getSubsequeceId(), 0, 0, holder.getFrameNumber(), 1, errorStreamId);
    }

    static boolean needsConversion(Surface s) throws BufferQueueAbandonedException {
        int nativeType = detectSurfaceType(s);
        if (nativeType == 35 || nativeType == ImageFormat.YV12 || nativeType == 17) {
            return true;
        }
        return false;
    }

    public LegacyCameraDevice(int cameraId, Camera camera, CameraCharacteristics characteristics, ICameraDeviceCallbacks callbacks) {
        this.mDeviceState = new CameraDeviceState();
        this.mClosed = false;
        this.mIdle = new ConditionVariable(true);
        this.mResultThread = new HandlerThread("ResultThread");
        this.mCallbackHandlerThread = new HandlerThread("CallbackThread");
        this.mStateListener = new AnonymousClass1(this);
        this.mCameraId = cameraId;
        this.mDeviceCallbacks = callbacks;
        Object[] objArr = new Object[1];
        objArr[0] = Integer.valueOf(this.mCameraId);
        this.TAG = String.format("CameraDevice-%d-LE", objArr);
        this.mResultThread.start();
        this.mResultHandler = new Handler(this.mResultThread.getLooper());
        this.mCallbackHandlerThread.start();
        this.mCallbackHandler = new Handler(this.mCallbackHandlerThread.getLooper());
        this.mDeviceState.setCameraDeviceCallbacks(this.mCallbackHandler, this.mStateListener);
        this.mStaticCharacteristics = characteristics;
        this.mRequestThreadManager = new RequestThreadManager(cameraId, camera, characteristics, this.mDeviceState);
        this.mRequestThreadManager.start();
    }

    public int configureOutputs(SparseArray<Surface> outputs) {
        List<Pair<Surface, Size>> sizedSurfaces = new ArrayList();
        if (outputs != null) {
            int count = outputs.size();
            int i = 0;
            while (i < count) {
                Surface output = (Surface) outputs.valueAt(i);
                if (output == null) {
                    Log.e(this.TAG, "configureOutputs - null outputs are not allowed");
                    return LegacyExceptionUtils.BAD_VALUE;
                } else if (output.isValid()) {
                    StreamConfigurationMap streamConfigurations = (StreamConfigurationMap) this.mStaticCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    try {
                        Size s = getSurfaceSize(output);
                        int surfaceType = detectSurfaceType(output);
                        boolean flexibleConsumer = isFlexibleConsumer(output);
                        Object[] sizes = streamConfigurations.getOutputSizes(surfaceType);
                        if (sizes == null) {
                            if (surfaceType >= 1 && surfaceType <= 5) {
                                sizes = streamConfigurations.getOutputSizes(35);
                            } else if (surfaceType == 33) {
                                sizes = streamConfigurations.getOutputSizes(256);
                            }
                        }
                        if (ArrayUtils.contains(sizes, (Object) s)) {
                            sizedSurfaces.add(new Pair(output, s));
                        } else {
                            String reason;
                            if (flexibleConsumer) {
                                s = findClosestSize(s, sizes);
                                if (s != null) {
                                    sizedSurfaces.add(new Pair(output, s));
                                }
                            }
                            if (sizes == null) {
                                reason = "format is invalid.";
                            } else {
                                reason = "size not in valid set: " + Arrays.toString(sizes);
                            }
                            String str = this.TAG;
                            Object[] objArr = new Object[4];
                            objArr[0] = Integer.valueOf(s.getWidth());
                            objArr[1] = Integer.valueOf(s.getHeight());
                            objArr[2] = Integer.valueOf(surfaceType);
                            objArr[3] = reason;
                            Log.e(str, String.format("Surface with size (w=%d, h=%d) and format 0x%x is not valid, %s", objArr));
                            return LegacyExceptionUtils.BAD_VALUE;
                        }
                        setSurfaceDimens(output, s.getWidth(), s.getHeight());
                        i++;
                    } catch (BufferQueueAbandonedException e) {
                        Log.e(this.TAG, "Surface bufferqueue is abandoned, cannot configure as output: ", e);
                        return LegacyExceptionUtils.BAD_VALUE;
                    }
                } else {
                    Log.e(this.TAG, "configureOutputs - invalid output surfaces are not allowed");
                    return LegacyExceptionUtils.BAD_VALUE;
                }
            }
        }
        boolean success = false;
        if (this.mDeviceState.setConfiguring()) {
            this.mRequestThreadManager.configure(sizedSurfaces);
            success = this.mDeviceState.setIdle();
        }
        if (!success) {
            return LegacyExceptionUtils.INVALID_OPERATION;
        }
        this.mConfiguredSurfaces = outputs;
        return 0;
    }

    public SubmitInfo submitRequestList(CaptureRequest[] requestList, boolean repeating) {
        if (requestList == null || requestList.length == 0) {
            Log.e(this.TAG, "submitRequestList - Empty/null requests are not allowed");
            throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - Empty/null requests are not allowed");
        }
        try {
            List<Long> surfaceIds;
            if (this.mConfiguredSurfaces == null) {
                surfaceIds = new ArrayList();
            } else {
                surfaceIds = getSurfaceIds(this.mConfiguredSurfaces);
            }
            for (CaptureRequest request : requestList) {
                if (request.getTargets().isEmpty()) {
                    Log.e(this.TAG, "submitRequestList - Each request must have at least one Surface target");
                    throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - Each request must have at least one Surface target");
                }
                for (Surface surface : request.getTargets()) {
                    if (surface == null) {
                        Log.e(this.TAG, "submitRequestList - Null Surface targets are not allowed");
                        throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - Null Surface targets are not allowed");
                    } else if (this.mConfiguredSurfaces == null) {
                        Log.e(this.TAG, "submitRequestList - must configure  device with valid surfaces before submitting requests");
                        throw new ServiceSpecificException(LegacyExceptionUtils.INVALID_OPERATION, "submitRequestList - must configure  device with valid surfaces before submitting requests");
                    } else if (!containsSurfaceId(surface, surfaceIds)) {
                        Log.e(this.TAG, "submitRequestList - cannot use a surface that wasn't configured");
                        throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - cannot use a surface that wasn't configured");
                    }
                }
            }
            this.mIdle.close();
            return this.mRequestThreadManager.submitCaptureRequests(requestList, repeating);
        } catch (BufferQueueAbandonedException e) {
            throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - configured surface is abandoned.");
        }
    }

    public SubmitInfo submitRequest(CaptureRequest request, boolean repeating) {
        CaptureRequest[] requestList = new CaptureRequest[1];
        requestList[0] = request;
        return submitRequestList(requestList, repeating);
    }

    public long cancelRequest(int requestId) {
        return this.mRequestThreadManager.cancelRepeating(requestId);
    }

    public void waitUntilIdle() {
        this.mIdle.block();
    }

    public long flush() {
        long lastFrame = this.mRequestThreadManager.flush();
        waitUntilIdle();
        return lastFrame;
    }

    public boolean isClosed() {
        return this.mClosed;
    }

    public void close() {
        String str;
        Object[] objArr;
        this.mRequestThreadManager.quit();
        this.mCallbackHandlerThread.quitSafely();
        this.mResultThread.quitSafely();
        try {
            this.mCallbackHandlerThread.join();
        } catch (InterruptedException e) {
            str = this.TAG;
            objArr = new Object[2];
            objArr[0] = this.mCallbackHandlerThread.getName();
            objArr[1] = Long.valueOf(this.mCallbackHandlerThread.getId());
            Log.e(str, String.format("Thread %s (%d) interrupted while quitting.", objArr));
        }
        try {
            this.mResultThread.join();
        } catch (InterruptedException e2) {
            str = this.TAG;
            objArr = new Object[2];
            objArr[0] = this.mResultThread.getName();
            objArr[1] = Long.valueOf(this.mResultThread.getId());
            Log.e(str, String.format("Thread %s (%d) interrupted while quitting.", objArr));
        }
        this.mClosed = true;
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } catch (ServiceSpecificException e) {
            Log.e(this.TAG, "Got error while trying to finalize, ignoring: " + e.getMessage());
        } finally {
            super.finalize();
        }
    }

    static long findEuclidDistSquare(Size a, Size b) {
        long d0 = (long) (a.getWidth() - b.getWidth());
        long d1 = (long) (a.getHeight() - b.getHeight());
        return (d0 * d0) + (d1 * d1);
    }

    static Size findClosestSize(Size size, Size[] supportedSizes) {
        if (size == null || supportedSizes == null) {
            return null;
        }
        Size bestSize = null;
        for (Size s : supportedSizes) {
            if (s.equals(size)) {
                return size;
            }
            if (s.getWidth() <= MAX_DIMEN_FOR_ROUNDING && (bestSize == null || findEuclidDistSquare(size, s) < findEuclidDistSquare(bestSize, s))) {
                bestSize = s;
            }
        }
        return bestSize;
    }

    public static Size getSurfaceSize(Surface surface) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        int[] dimens = new int[2];
        LegacyExceptionUtils.throwOnError(nativeDetectSurfaceDimens(surface, dimens));
        return new Size(dimens[0], dimens[1]);
    }

    public static boolean isFlexibleConsumer(Surface output) {
        int usageFlags = detectSurfaceUsageFlags(output);
        if ((1114112 & usageFlags) != 0 || (usageFlags & 2307) == 0) {
            return false;
        }
        return true;
    }

    public static boolean isPreviewConsumer(Surface output) {
        int usageFlags = detectSurfaceUsageFlags(output);
        boolean previewConsumer = (1114115 & usageFlags) == 0 ? (usageFlags & GLES11.GL_CURRENT_COLOR) != 0 : false;
        try {
            int surfaceFormat = detectSurfaceType(output);
            return previewConsumer;
        } catch (BufferQueueAbandonedException e) {
            throw new IllegalArgumentException("Surface was abandoned", e);
        }
    }

    public static boolean isVideoEncoderConsumer(Surface output) {
        int usageFlags = detectSurfaceUsageFlags(output);
        boolean videoEncoderConsumer = (1050883 & usageFlags) == 0 ? (65536 & usageFlags) != 0 : false;
        try {
            int surfaceFormat = detectSurfaceType(output);
            return videoEncoderConsumer;
        } catch (BufferQueueAbandonedException e) {
            throw new IllegalArgumentException("Surface was abandoned", e);
        }
    }

    static int detectSurfaceUsageFlags(Surface surface) {
        Preconditions.checkNotNull(surface);
        return nativeDetectSurfaceUsageFlags(surface);
    }

    public static int detectSurfaceType(Surface surface) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        return LegacyExceptionUtils.throwOnError(nativeDetectSurfaceType(surface));
    }

    public static int detectSurfaceDataspace(Surface surface) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        return LegacyExceptionUtils.throwOnError(nativeDetectSurfaceDataspace(surface));
    }

    static void connectSurface(Surface surface) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeConnectSurface(surface));
    }

    static void disconnectSurface(Surface surface) throws BufferQueueAbandonedException {
        if (surface != null) {
            LegacyExceptionUtils.throwOnError(nativeDisconnectSurface(surface));
        }
    }

    static void produceFrame(Surface surface, byte[] pixelBuffer, int width, int height, int pixelFormat) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        Preconditions.checkNotNull(pixelBuffer);
        Preconditions.checkArgumentPositive(width, "width must be positive.");
        Preconditions.checkArgumentPositive(height, "height must be positive.");
        LegacyExceptionUtils.throwOnError(nativeProduceFrame(surface, pixelBuffer, width, height, pixelFormat));
    }

    static void setSurfaceFormat(Surface surface, int pixelFormat) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeSetSurfaceFormat(surface, pixelFormat));
    }

    static void setSurfaceDimens(Surface surface, int width, int height) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        Preconditions.checkArgumentPositive(width, "width must be positive.");
        Preconditions.checkArgumentPositive(height, "height must be positive.");
        LegacyExceptionUtils.throwOnError(nativeSetSurfaceDimens(surface, width, height));
    }

    static long getSurfaceId(Surface surface) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        try {
            return nativeGetSurfaceId(surface);
        } catch (IllegalArgumentException e) {
            throw new BufferQueueAbandonedException();
        }
    }

    static List<Long> getSurfaceIds(SparseArray<Surface> surfaces) throws BufferQueueAbandonedException {
        if (surfaces == null) {
            throw new NullPointerException("Null argument surfaces");
        }
        List<Long> surfaceIds = new ArrayList();
        int count = surfaces.size();
        for (int i = 0; i < count; i++) {
            long id = getSurfaceId((Surface) surfaces.valueAt(i));
            if (id == 0) {
                throw new IllegalStateException("Configured surface had null native GraphicBufferProducer pointer!");
            }
            surfaceIds.add(Long.valueOf(id));
        }
        return surfaceIds;
    }

    static List<Long> getSurfaceIds(Collection<Surface> surfaces) throws BufferQueueAbandonedException {
        if (surfaces == null) {
            throw new NullPointerException("Null argument surfaces");
        }
        List<Long> surfaceIds = new ArrayList();
        for (Surface s : surfaces) {
            long id = getSurfaceId(s);
            if (id == 0) {
                throw new IllegalStateException("Configured surface had null native GraphicBufferProducer pointer!");
            }
            surfaceIds.add(Long.valueOf(id));
        }
        return surfaceIds;
    }

    static boolean containsSurfaceId(Surface s, Collection<Long> ids) {
        try {
            return ids.contains(Long.valueOf(getSurfaceId(s)));
        } catch (BufferQueueAbandonedException e) {
            return false;
        }
    }

    static void setSurfaceOrientation(Surface surface, int facing, int sensorOrientation) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeSetSurfaceOrientation(surface, facing, sensorOrientation));
    }

    static Size getTextureSize(SurfaceTexture surfaceTexture) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surfaceTexture);
        int[] dimens = new int[2];
        LegacyExceptionUtils.throwOnError(nativeDetectTextureDimens(surfaceTexture, dimens));
        return new Size(dimens[0], dimens[1]);
    }

    static void setNextTimestamp(Surface surface, long timestamp) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeSetNextTimestamp(surface, timestamp));
    }

    static void setScalingMode(Surface surface, int mode) throws BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeSetScalingMode(surface, mode));
    }
}
