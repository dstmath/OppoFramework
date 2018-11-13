package android.media.audiofx;

import android.app.ActivityThread;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.lang.ref.WeakReference;

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
public class Visualizer {
    public static final int ALREADY_EXISTS = -2;
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -4;
    public static final int ERROR_DEAD_OBJECT = -7;
    public static final int ERROR_INVALID_OPERATION = -5;
    public static final int ERROR_NO_INIT = -3;
    public static final int ERROR_NO_MEMORY = -6;
    public static final int MEASUREMENT_MODE_NONE = 0;
    public static final int MEASUREMENT_MODE_PEAK_RMS = 1;
    private static final int NATIVE_EVENT_FFT_CAPTURE = 1;
    private static final int NATIVE_EVENT_PCM_CAPTURE = 0;
    private static final int NATIVE_EVENT_SERVER_DIED = 2;
    public static final int SCALING_MODE_AS_PLAYED = 1;
    public static final int SCALING_MODE_NORMALIZED = 0;
    public static final int STATE_ENABLED = 2;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_UNINITIALIZED = 0;
    public static final int SUCCESS = 0;
    private static final String TAG = "Visualizer-JAVA";
    private OnDataCaptureListener mCaptureListener;
    private int mId;
    private long mJniData;
    private final Object mListenerLock;
    private NativeEventHandler mNativeEventHandler;
    private long mNativeVisualizer;
    private OnServerDiedListener mServerDiedListener;
    private int mState;
    private final Object mStateLock;

    public static final class MeasurementPeakRms {
        public int mPeak;
        public int mRms;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.audiofx.Visualizer.MeasurementPeakRms.<init>():void, dex: 
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
        public MeasurementPeakRms() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.audiofx.Visualizer.MeasurementPeakRms.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.Visualizer.MeasurementPeakRms.<init>():void");
        }
    }

    private class NativeEventHandler extends Handler {
        private Visualizer mVisualizer;
        final /* synthetic */ Visualizer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.audiofx.Visualizer.NativeEventHandler.<init>(android.media.audiofx.Visualizer, android.media.audiofx.Visualizer, android.os.Looper):void, dex: 
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
        public NativeEventHandler(android.media.audiofx.Visualizer r1, android.media.audiofx.Visualizer r2, android.os.Looper r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.audiofx.Visualizer.NativeEventHandler.<init>(android.media.audiofx.Visualizer, android.media.audiofx.Visualizer, android.os.Looper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.Visualizer.NativeEventHandler.<init>(android.media.audiofx.Visualizer, android.media.audiofx.Visualizer, android.os.Looper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 9 in method: android.media.audiofx.Visualizer.NativeEventHandler.handleCaptureMessage(android.os.Message):void, dex:  in method: android.media.audiofx.Visualizer.NativeEventHandler.handleCaptureMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 9 in method: android.media.audiofx.Visualizer.NativeEventHandler.handleCaptureMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: 9
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private void handleCaptureMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: 9 in method: android.media.audiofx.Visualizer.NativeEventHandler.handleCaptureMessage(android.os.Message):void, dex:  in method: android.media.audiofx.Visualizer.NativeEventHandler.handleCaptureMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.Visualizer.NativeEventHandler.handleCaptureMessage(android.os.Message):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 9 in method: android.media.audiofx.Visualizer.NativeEventHandler.handleServerDiedMessage(android.os.Message):void, dex:  in method: android.media.audiofx.Visualizer.NativeEventHandler.handleServerDiedMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 9 in method: android.media.audiofx.Visualizer.NativeEventHandler.handleServerDiedMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: 9
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private void handleServerDiedMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: 9 in method: android.media.audiofx.Visualizer.NativeEventHandler.handleServerDiedMessage(android.os.Message):void, dex:  in method: android.media.audiofx.Visualizer.NativeEventHandler.handleServerDiedMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.Visualizer.NativeEventHandler.handleServerDiedMessage(android.os.Message):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.audiofx.Visualizer.NativeEventHandler.handleMessage(android.os.Message):void, dex: 
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
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.audiofx.Visualizer.NativeEventHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.Visualizer.NativeEventHandler.handleMessage(android.os.Message):void");
        }
    }

    public interface OnDataCaptureListener {
        void onFftDataCapture(Visualizer visualizer, byte[] bArr, int i);

        void onWaveFormDataCapture(Visualizer visualizer, byte[] bArr, int i);
    }

    public interface OnServerDiedListener {
        void onServerDied();
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.audiofx.Visualizer.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.audiofx.Visualizer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.Visualizer.<clinit>():void");
    }

    public static native int[] getCaptureSizeRange();

    public static native int getMaxCaptureRate();

    private final native void native_finalize();

    private final native int native_getCaptureSize();

    private final native boolean native_getEnabled();

    private final native int native_getFft(byte[] bArr);

    private final native int native_getMeasurementMode();

    private final native int native_getPeakRms(MeasurementPeakRms measurementPeakRms);

    private final native int native_getSamplingRate();

    private final native int native_getScalingMode();

    private final native int native_getWaveForm(byte[] bArr);

    private static final native void native_init();

    private final native void native_release();

    private final native int native_setCaptureSize(int i);

    private final native int native_setEnabled(boolean z);

    private final native int native_setMeasurementMode(int i);

    private final native int native_setPeriodicCapture(int i, boolean z, boolean z2);

    private final native int native_setScalingMode(int i);

    private final native int native_setup(Object obj, int i, int[] iArr, String str);

    public Visualizer(int audioSession) throws UnsupportedOperationException, RuntimeException {
        this.mState = 0;
        this.mStateLock = new Object();
        this.mListenerLock = new Object();
        this.mNativeEventHandler = null;
        this.mCaptureListener = null;
        this.mServerDiedListener = null;
        int[] id = new int[1];
        synchronized (this.mStateLock) {
            this.mState = 0;
            int result = native_setup(new WeakReference(this), audioSession, id, ActivityThread.currentOpPackageName());
            if (result == 0 || result == -2) {
                this.mId = id[0];
                if (native_getEnabled()) {
                    this.mState = 2;
                } else {
                    this.mState = 1;
                }
            } else {
                Log.e(TAG, "Error code " + result + " when initializing Visualizer.");
                switch (result) {
                    case -5:
                        throw new UnsupportedOperationException("Effect library not loaded");
                    default:
                        throw new RuntimeException("Cannot initialize Visualizer engine, error: " + result);
                }
            }
        }
    }

    public void release() {
        synchronized (this.mStateLock) {
            native_release();
            this.mState = 0;
        }
    }

    protected void finalize() {
        native_finalize();
    }

    /* JADX WARNING: Missing block: B:14:0x002d, code:
            if (r5.mState == 1) goto L_0x002f;
     */
    /* JADX WARNING: Missing block: B:15:0x002f, code:
            r0 = native_setEnabled(r6);
     */
    /* JADX WARNING: Missing block: B:16:0x0033, code:
            if (r0 != 0) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:17:0x0035, code:
            if (r6 == false) goto L_0x0042;
     */
    /* JADX WARNING: Missing block: B:18:0x0037, code:
            r5.mState = r1;
     */
    /* JADX WARNING: Missing block: B:24:0x003f, code:
            if (r5.mState == 2) goto L_0x002f;
     */
    /* JADX WARNING: Missing block: B:25:0x0042, code:
            r1 = 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int setEnabled(boolean enabled) throws IllegalStateException {
        int status;
        int i = 2;
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                throw new IllegalStateException("setEnabled() called in wrong state: " + this.mState);
            }
            status = 0;
            if (enabled) {
            }
            if (!enabled) {
            }
        }
        return status;
    }

    public boolean getEnabled() {
        boolean native_getEnabled;
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                throw new IllegalStateException("getEnabled() called in wrong state: " + this.mState);
            }
            native_getEnabled = native_getEnabled();
        }
        return native_getEnabled;
    }

    public int setCaptureSize(int size) throws IllegalStateException {
        int native_setCaptureSize;
        synchronized (this.mStateLock) {
            if (this.mState != 1) {
                throw new IllegalStateException("setCaptureSize() called in wrong state: " + this.mState);
            }
            native_setCaptureSize = native_setCaptureSize(size);
        }
        return native_setCaptureSize;
    }

    public int getCaptureSize() throws IllegalStateException {
        int native_getCaptureSize;
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                throw new IllegalStateException("getCaptureSize() called in wrong state: " + this.mState);
            }
            native_getCaptureSize = native_getCaptureSize();
        }
        return native_getCaptureSize;
    }

    public int setScalingMode(int mode) throws IllegalStateException {
        int native_setScalingMode;
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                throw new IllegalStateException("setScalingMode() called in wrong state: " + this.mState);
            }
            native_setScalingMode = native_setScalingMode(mode);
        }
        return native_setScalingMode;
    }

    public int getScalingMode() throws IllegalStateException {
        int native_getScalingMode;
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                throw new IllegalStateException("getScalingMode() called in wrong state: " + this.mState);
            }
            native_getScalingMode = native_getScalingMode();
        }
        return native_getScalingMode;
    }

    public int setMeasurementMode(int mode) throws IllegalStateException {
        int native_setMeasurementMode;
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                throw new IllegalStateException("setMeasurementMode() called in wrong state: " + this.mState);
            }
            native_setMeasurementMode = native_setMeasurementMode(mode);
        }
        return native_setMeasurementMode;
    }

    public int getMeasurementMode() throws IllegalStateException {
        int native_getMeasurementMode;
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                throw new IllegalStateException("getMeasurementMode() called in wrong state: " + this.mState);
            }
            native_getMeasurementMode = native_getMeasurementMode();
        }
        return native_getMeasurementMode;
    }

    public int getSamplingRate() throws IllegalStateException {
        int native_getSamplingRate;
        synchronized (this.mStateLock) {
            if (this.mState == 0) {
                throw new IllegalStateException("getSamplingRate() called in wrong state: " + this.mState);
            }
            native_getSamplingRate = native_getSamplingRate();
        }
        return native_getSamplingRate;
    }

    public int getWaveForm(byte[] waveform) throws IllegalStateException {
        int native_getWaveForm;
        synchronized (this.mStateLock) {
            if (this.mState != 2) {
                throw new IllegalStateException("getWaveForm() called in wrong state: " + this.mState);
            }
            native_getWaveForm = native_getWaveForm(waveform);
        }
        return native_getWaveForm;
    }

    public int getFft(byte[] fft) throws IllegalStateException {
        int native_getFft;
        synchronized (this.mStateLock) {
            if (this.mState != 2) {
                throw new IllegalStateException("getFft() called in wrong state: " + this.mState);
            }
            native_getFft = native_getFft(fft);
        }
        return native_getFft;
    }

    public int getMeasurementPeakRms(MeasurementPeakRms measurement) {
        if (measurement == null) {
            Log.e(TAG, "Cannot store measurements in a null object");
            return -4;
        }
        int native_getPeakRms;
        synchronized (this.mStateLock) {
            if (this.mState != 2) {
                throw new IllegalStateException("getMeasurementPeakRms() called in wrong state: " + this.mState);
            }
            native_getPeakRms = native_getPeakRms(measurement);
        }
        return native_getPeakRms;
    }

    public int setDataCaptureListener(OnDataCaptureListener listener, int rate, boolean waveform, boolean fft) {
        synchronized (this.mListenerLock) {
            this.mCaptureListener = listener;
        }
        if (listener == null) {
            waveform = false;
            fft = false;
        }
        int status = native_setPeriodicCapture(rate, waveform, fft);
        if (status != 0 || listener == null || this.mNativeEventHandler != null) {
            return status;
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mNativeEventHandler = new NativeEventHandler(this, this, looper);
            return status;
        }
        looper = Looper.getMainLooper();
        if (looper != null) {
            this.mNativeEventHandler = new NativeEventHandler(this, this, looper);
            return status;
        }
        this.mNativeEventHandler = null;
        return -3;
    }

    public int setServerDiedListener(OnServerDiedListener listener) {
        synchronized (this.mListenerLock) {
            this.mServerDiedListener = listener;
        }
        return 0;
    }

    private static void postEventFromNative(Object effect_ref, int what, int arg1, int arg2, Object obj) {
        Visualizer visu = (Visualizer) ((WeakReference) effect_ref).get();
        if (!(visu == null || visu.mNativeEventHandler == null)) {
            visu.mNativeEventHandler.sendMessage(visu.mNativeEventHandler.obtainMessage(what, arg1, arg2, obj));
        }
    }
}
