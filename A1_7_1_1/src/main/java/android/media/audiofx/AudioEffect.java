package android.media.audiofx;

import android.app.ActivityThread;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

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
public class AudioEffect {
    public static final String ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION = "android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION";
    public static final String ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL = "android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL";
    public static final String ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION = "android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION";
    public static final int ALREADY_EXISTS = -2;
    public static final int CONTENT_TYPE_GAME = 2;
    public static final int CONTENT_TYPE_MOVIE = 1;
    public static final int CONTENT_TYPE_MUSIC = 0;
    public static final int CONTENT_TYPE_VOICE = 3;
    public static final String EFFECT_AUXILIARY = "Auxiliary";
    public static final String EFFECT_INSERT = "Insert";
    public static final String EFFECT_PRE_PROCESSING = "Pre Processing";
    public static final UUID EFFECT_TYPE_AEC = null;
    public static final UUID EFFECT_TYPE_AGC = null;
    public static final UUID EFFECT_TYPE_BASS_BOOST = null;
    public static final UUID EFFECT_TYPE_ENV_REVERB = null;
    public static final UUID EFFECT_TYPE_EQUALIZER = null;
    public static final UUID EFFECT_TYPE_LOUDNESS_ENHANCER = null;
    public static final UUID EFFECT_TYPE_NS = null;
    public static final UUID EFFECT_TYPE_NULL = null;
    public static final UUID EFFECT_TYPE_PRESET_REVERB = null;
    public static final UUID EFFECT_TYPE_VIRTUALIZER = null;
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -4;
    public static final int ERROR_DEAD_OBJECT = -7;
    public static final int ERROR_INVALID_OPERATION = -5;
    public static final int ERROR_NO_INIT = -3;
    public static final int ERROR_NO_MEMORY = -6;
    public static final String EXTRA_AUDIO_SESSION = "android.media.extra.AUDIO_SESSION";
    public static final String EXTRA_CONTENT_TYPE = "android.media.extra.CONTENT_TYPE";
    public static final String EXTRA_PACKAGE_NAME = "android.media.extra.PACKAGE_NAME";
    public static final int NATIVE_EVENT_CONTROL_STATUS = 0;
    public static final int NATIVE_EVENT_ENABLED_STATUS = 1;
    public static final int NATIVE_EVENT_PARAMETER_CHANGED = 2;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_UNINITIALIZED = 0;
    public static final int SUCCESS = 0;
    private static final String TAG = "AudioEffect-JAVA";
    private OnControlStatusChangeListener mControlChangeStatusListener;
    private Descriptor mDescriptor;
    private OnEnableStatusChangeListener mEnableStatusChangeListener;
    private int mId;
    private long mJniData;
    public final Object mListenerLock;
    private long mNativeAudioEffect;
    public NativeEventHandler mNativeEventHandler;
    private OnParameterChangeListener mParameterChangeListener;
    private int mState;
    private final Object mStateLock;

    public static class Descriptor {
        public String connectMode;
        public String implementor;
        public String name;
        public UUID type;
        public UUID uuid;

        public Descriptor(String type, String uuid, String connectMode, String name, String implementor) {
            this.type = UUID.fromString(type);
            this.uuid = UUID.fromString(uuid);
            this.connectMode = connectMode;
            this.name = name;
            this.implementor = implementor;
        }
    }

    private class NativeEventHandler extends Handler {
        private AudioEffect mAudioEffect;
        final /* synthetic */ AudioEffect this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.audiofx.AudioEffect.NativeEventHandler.<init>(android.media.audiofx.AudioEffect, android.media.audiofx.AudioEffect, android.os.Looper):void, dex: 
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
        public NativeEventHandler(android.media.audiofx.AudioEffect r1, android.media.audiofx.AudioEffect r2, android.os.Looper r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.audiofx.AudioEffect.NativeEventHandler.<init>(android.media.audiofx.AudioEffect, android.media.audiofx.AudioEffect, android.os.Looper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.AudioEffect.NativeEventHandler.<init>(android.media.audiofx.AudioEffect, android.media.audiofx.AudioEffect, android.os.Looper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.audiofx.AudioEffect.NativeEventHandler.handleMessage(android.os.Message):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.audiofx.AudioEffect.NativeEventHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.AudioEffect.NativeEventHandler.handleMessage(android.os.Message):void");
        }
    }

    public interface OnControlStatusChangeListener {
        void onControlStatusChange(AudioEffect audioEffect, boolean z);
    }

    public interface OnEnableStatusChangeListener {
        void onEnableStatusChange(AudioEffect audioEffect, boolean z);
    }

    public interface OnParameterChangeListener {
        void onParameterChange(AudioEffect audioEffect, int i, byte[] bArr, byte[] bArr2);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.audiofx.AudioEffect.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.audiofx.AudioEffect.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.AudioEffect.<clinit>():void");
    }

    private final native int native_command(int i, int i2, byte[] bArr, int i3, byte[] bArr2);

    private final native void native_finalize();

    private final native boolean native_getEnabled();

    private final native int native_getParameter(int i, byte[] bArr, int i2, byte[] bArr2);

    private final native boolean native_hasControl();

    private static final native void native_init();

    private static native Object[] native_query_effects();

    private static native Object[] native_query_pre_processing(int i);

    private final native void native_release();

    private final native int native_setEnabled(boolean z);

    private final native int native_setParameter(int i, byte[] bArr, int i2, byte[] bArr2);

    private final native int native_setup(Object obj, String str, String str2, int i, int i2, int[] iArr, Object[] objArr, String str3);

    public AudioEffect(UUID type, UUID uuid, int priority, int audioSession) throws IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        this.mState = 0;
        this.mStateLock = new Object();
        this.mEnableStatusChangeListener = null;
        this.mControlChangeStatusListener = null;
        this.mParameterChangeListener = null;
        this.mListenerLock = new Object();
        this.mNativeEventHandler = null;
        int[] id = new int[1];
        Descriptor[] desc = new Descriptor[1];
        int initResult = native_setup(new WeakReference(this), type.toString(), uuid.toString(), priority, audioSession, id, desc, ActivityThread.currentOpPackageName());
        if (initResult == 0 || initResult == -2) {
            this.mId = id[0];
            this.mDescriptor = desc[0];
            synchronized (this.mStateLock) {
                this.mState = 1;
            }
            return;
        }
        Log.e(TAG, "Error code " + initResult + " when initializing AudioEffect.");
        switch (initResult) {
            case -5:
                throw new UnsupportedOperationException("Effect library not loaded");
            case -4:
                throw new IllegalArgumentException("Effect type: " + type + " not supported.");
            default:
                throw new RuntimeException("Cannot initialize effect engine for type: " + type + " Error: " + initResult);
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

    public Descriptor getDescriptor() throws IllegalStateException {
        checkState("getDescriptor()");
        return this.mDescriptor;
    }

    public static Descriptor[] queryEffects() {
        return (Descriptor[]) native_query_effects();
    }

    public static Descriptor[] queryPreProcessings(int audioSession) {
        return (Descriptor[]) native_query_pre_processing(audioSession);
    }

    public static boolean isEffectTypeAvailable(UUID type) {
        Descriptor[] desc = queryEffects();
        if (desc == null) {
            return false;
        }
        for (Descriptor descriptor : desc) {
            if (descriptor.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public int setEnabled(boolean enabled) throws IllegalStateException {
        checkState("setEnabled()");
        return native_setEnabled(enabled);
    }

    public int setParameter(byte[] param, byte[] value) throws IllegalStateException {
        checkState("setParameter()");
        return native_setParameter(param.length, param, value.length, value);
    }

    public int setParameter(int param, int value) throws IllegalStateException {
        return setParameter(intToByteArray(param), intToByteArray(value));
    }

    public int setParameter(int param, short value) throws IllegalStateException {
        return setParameter(intToByteArray(param), shortToByteArray(value));
    }

    public int setParameter(int param, byte[] value) throws IllegalStateException {
        return setParameter(intToByteArray(param), value);
    }

    public int setParameter(int[] param, int[] value) throws IllegalStateException {
        if (param.length > 2 || value.length > 2) {
            return -4;
        }
        byte[][] bArr;
        byte[] p = intToByteArray(param[0]);
        if (param.length > 1) {
            byte[] p2 = intToByteArray(param[1]);
            bArr = new byte[2][];
            bArr[0] = p;
            bArr[1] = p2;
            p = concatArrays(bArr);
        }
        byte[] v = intToByteArray(value[0]);
        if (value.length > 1) {
            byte[] v2 = intToByteArray(value[1]);
            bArr = new byte[2][];
            bArr[0] = v;
            bArr[1] = v2;
            v = concatArrays(bArr);
        }
        return setParameter(p, v);
    }

    public int setParameter(int[] param, short[] value) throws IllegalStateException {
        if (param.length > 2 || value.length > 2) {
            return -4;
        }
        byte[][] bArr;
        byte[] p = intToByteArray(param[0]);
        if (param.length > 1) {
            byte[] p2 = intToByteArray(param[1]);
            bArr = new byte[2][];
            bArr[0] = p;
            bArr[1] = p2;
            p = concatArrays(bArr);
        }
        byte[] v = shortToByteArray(value[0]);
        if (value.length > 1) {
            byte[] v2 = shortToByteArray(value[1]);
            bArr = new byte[2][];
            bArr[0] = v;
            bArr[1] = v2;
            v = concatArrays(bArr);
        }
        return setParameter(p, v);
    }

    public int setParameter(int[] param, byte[] value) throws IllegalStateException {
        if (param.length > 2) {
            return -4;
        }
        byte[] p = intToByteArray(param[0]);
        if (param.length > 1) {
            byte[] p2 = intToByteArray(param[1]);
            byte[][] bArr = new byte[2][];
            bArr[0] = p;
            bArr[1] = p2;
            p = concatArrays(bArr);
        }
        return setParameter(p, value);
    }

    public int getParameter(byte[] param, byte[] value) throws IllegalStateException {
        checkState("getParameter()");
        return native_getParameter(param.length, param, value.length, value);
    }

    public int getParameter(int param, byte[] value) throws IllegalStateException {
        return getParameter(intToByteArray(param), value);
    }

    public int getParameter(int param, int[] value) throws IllegalStateException {
        if (value.length > 2) {
            return -4;
        }
        byte[] v = new byte[(value.length * 4)];
        int status = getParameter(intToByteArray(param), v);
        if (status == 4 || status == 8) {
            value[0] = byteArrayToInt(v);
            if (status == 8) {
                value[1] = byteArrayToInt(v, 4);
            }
            status /= 4;
        } else {
            status = -1;
        }
        return status;
    }

    public int getParameter(int param, short[] value) throws IllegalStateException {
        if (value.length > 2) {
            return -4;
        }
        byte[] v = new byte[(value.length * 2)];
        int status = getParameter(intToByteArray(param), v);
        if (status == 2 || status == 4) {
            value[0] = byteArrayToShort(v);
            if (status == 4) {
                value[1] = byteArrayToShort(v, 2);
            }
            status /= 2;
        } else {
            status = -1;
        }
        return status;
    }

    public int getParameter(int[] param, int[] value) throws IllegalStateException {
        if (param.length > 2 || value.length > 2) {
            return -4;
        }
        byte[] p = intToByteArray(param[0]);
        if (param.length > 1) {
            byte[] p2 = intToByteArray(param[1]);
            byte[][] bArr = new byte[2][];
            bArr[0] = p;
            bArr[1] = p2;
            p = concatArrays(bArr);
        }
        byte[] v = new byte[(value.length * 4)];
        int status = getParameter(p, v);
        if (status == 4 || status == 8) {
            value[0] = byteArrayToInt(v);
            if (status == 8) {
                value[1] = byteArrayToInt(v, 4);
            }
            status /= 4;
        } else {
            status = -1;
        }
        return status;
    }

    public int getParameter(int[] param, short[] value) throws IllegalStateException {
        if (param.length > 2 || value.length > 2) {
            return -4;
        }
        byte[] p = intToByteArray(param[0]);
        if (param.length > 1) {
            byte[] p2 = intToByteArray(param[1]);
            byte[][] bArr = new byte[2][];
            bArr[0] = p;
            bArr[1] = p2;
            p = concatArrays(bArr);
        }
        byte[] v = new byte[(value.length * 2)];
        int status = getParameter(p, v);
        if (status == 2 || status == 4) {
            value[0] = byteArrayToShort(v);
            if (status == 4) {
                value[1] = byteArrayToShort(v, 2);
            }
            status /= 2;
        } else {
            status = -1;
        }
        return status;
    }

    public int getParameter(int[] param, byte[] value) throws IllegalStateException {
        if (param.length > 2) {
            return -4;
        }
        byte[] p = intToByteArray(param[0]);
        if (param.length > 1) {
            byte[] p2 = intToByteArray(param[1]);
            byte[][] bArr = new byte[2][];
            bArr[0] = p;
            bArr[1] = p2;
            p = concatArrays(bArr);
        }
        return getParameter(p, value);
    }

    public int command(int cmdCode, byte[] command, byte[] reply) throws IllegalStateException {
        checkState("command()");
        return native_command(cmdCode, command.length, command, reply.length, reply);
    }

    public int getId() throws IllegalStateException {
        checkState("getId()");
        return this.mId;
    }

    public boolean getEnabled() throws IllegalStateException {
        checkState("getEnabled()");
        return native_getEnabled();
    }

    public boolean hasControl() throws IllegalStateException {
        checkState("hasControl()");
        return native_hasControl();
    }

    public void setEnableStatusListener(OnEnableStatusChangeListener listener) {
        synchronized (this.mListenerLock) {
            this.mEnableStatusChangeListener = listener;
        }
        if (listener != null && this.mNativeEventHandler == null) {
            createNativeEventHandler();
        }
    }

    public void setControlStatusListener(OnControlStatusChangeListener listener) {
        synchronized (this.mListenerLock) {
            this.mControlChangeStatusListener = listener;
        }
        if (listener != null && this.mNativeEventHandler == null) {
            createNativeEventHandler();
        }
    }

    public void setParameterListener(OnParameterChangeListener listener) {
        synchronized (this.mListenerLock) {
            this.mParameterChangeListener = listener;
        }
        if (listener != null && this.mNativeEventHandler == null) {
            createNativeEventHandler();
        }
    }

    private void createNativeEventHandler() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mNativeEventHandler = new NativeEventHandler(this, this, looper);
            return;
        }
        looper = Looper.getMainLooper();
        if (looper != null) {
            this.mNativeEventHandler = new NativeEventHandler(this, this, looper);
        } else {
            this.mNativeEventHandler = null;
        }
    }

    private static void postEventFromNative(Object effect_ref, int what, int arg1, int arg2, Object obj) {
        AudioEffect effect = (AudioEffect) ((WeakReference) effect_ref).get();
        if (!(effect == null || effect.mNativeEventHandler == null)) {
            effect.mNativeEventHandler.sendMessage(effect.mNativeEventHandler.obtainMessage(what, arg1, arg2, obj));
        }
    }

    public void checkState(String methodName) throws IllegalStateException {
        synchronized (this.mStateLock) {
            if (this.mState != 1) {
                throw new IllegalStateException(methodName + " called on uninitialized AudioEffect.");
            }
        }
    }

    public void checkStatus(int status) {
        if (isError(status)) {
            switch (status) {
                case -5:
                    throw new UnsupportedOperationException("AudioEffect: invalid parameter operation");
                case -4:
                    throw new IllegalArgumentException("AudioEffect: bad parameter value");
                default:
                    throw new RuntimeException("AudioEffect: set/get parameter error");
            }
        }
    }

    public static boolean isError(int status) {
        return status < 0;
    }

    public static int byteArrayToInt(byte[] valueBuf) {
        return byteArrayToInt(valueBuf, 0);
    }

    public static int byteArrayToInt(byte[] valueBuf, int offset) {
        ByteBuffer converter = ByteBuffer.wrap(valueBuf);
        converter.order(ByteOrder.nativeOrder());
        return converter.getInt(offset);
    }

    public static byte[] intToByteArray(int value) {
        ByteBuffer converter = ByteBuffer.allocate(4);
        converter.order(ByteOrder.nativeOrder());
        converter.putInt(value);
        return converter.array();
    }

    public static short byteArrayToShort(byte[] valueBuf) {
        return byteArrayToShort(valueBuf, 0);
    }

    public static short byteArrayToShort(byte[] valueBuf, int offset) {
        ByteBuffer converter = ByteBuffer.wrap(valueBuf);
        converter.order(ByteOrder.nativeOrder());
        return converter.getShort(offset);
    }

    public static byte[] shortToByteArray(short value) {
        ByteBuffer converter = ByteBuffer.allocate(2);
        converter.order(ByteOrder.nativeOrder());
        converter.putShort(value);
        return converter.array();
    }

    public static byte[] concatArrays(byte[]... arrays) {
        int len = 0;
        for (byte[] a : arrays) {
            len += a.length;
        }
        byte[] b = new byte[len];
        int offs = 0;
        for (byte[] a2 : arrays) {
            System.arraycopy(a2, 0, b, offs, a2.length);
            offs += a2.length;
        }
        return b;
    }
}
