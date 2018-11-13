package android.media.audiofx;

import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringTokenizer;

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
public class Virtualizer extends AudioEffect {
    private static final boolean DEBUG = false;
    public static final int PARAM_FORCE_VIRTUALIZATION_MODE = 3;
    public static final int PARAM_STRENGTH = 1;
    public static final int PARAM_STRENGTH_SUPPORTED = 0;
    public static final int PARAM_VIRTUALIZATION_MODE = 4;
    public static final int PARAM_VIRTUAL_SPEAKER_ANGLES = 2;
    private static final String TAG = "Virtualizer";
    public static final int VIRTUALIZATION_MODE_AUTO = 1;
    public static final int VIRTUALIZATION_MODE_BINAURAL = 2;
    public static final int VIRTUALIZATION_MODE_OFF = 0;
    public static final int VIRTUALIZATION_MODE_TRANSAURAL = 3;
    private BaseParameterListener mBaseParamListener;
    private OnParameterChangeListener mParamListener;
    private final Object mParamListenerLock;
    private boolean mStrengthSupported;

    private class BaseParameterListener implements android.media.audiofx.AudioEffect.OnParameterChangeListener {
        final /* synthetic */ Virtualizer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.audiofx.Virtualizer.BaseParameterListener.<init>(android.media.audiofx.Virtualizer):void, dex: 
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
        private BaseParameterListener(android.media.audiofx.Virtualizer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.audiofx.Virtualizer.BaseParameterListener.<init>(android.media.audiofx.Virtualizer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.Virtualizer.BaseParameterListener.<init>(android.media.audiofx.Virtualizer):void");
        }

        /* synthetic */ BaseParameterListener(Virtualizer this$0, BaseParameterListener baseParameterListener) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.audiofx.Virtualizer.BaseParameterListener.onParameterChange(android.media.audiofx.AudioEffect, int, byte[], byte[]):void, dex: 
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
        public void onParameterChange(android.media.audiofx.AudioEffect r1, int r2, byte[] r3, byte[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.audiofx.Virtualizer.BaseParameterListener.onParameterChange(android.media.audiofx.AudioEffect, int, byte[], byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.Virtualizer.BaseParameterListener.onParameterChange(android.media.audiofx.AudioEffect, int, byte[], byte[]):void");
        }
    }

    public interface OnParameterChangeListener {
        void onParameterChange(Virtualizer virtualizer, int i, int i2, short s);
    }

    public static class Settings {
        public short strength;

        public Settings() {
        }

        public Settings(String settings) {
            StringTokenizer st = new StringTokenizer(settings, "=;");
            int tokens = st.countTokens();
            if (st.countTokens() != 3) {
                throw new IllegalArgumentException("settings: " + settings);
            }
            String key = st.nextToken();
            if (key.equals(Virtualizer.TAG)) {
                try {
                    key = st.nextToken();
                    if (key.equals("strength")) {
                        this.strength = Short.parseShort(st.nextToken());
                        return;
                    }
                    throw new IllegalArgumentException("invalid key name: " + key);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("invalid value for key: " + key);
                }
            }
            throw new IllegalArgumentException("invalid settings for Virtualizer: " + key);
        }

        public String toString() {
            return new String("Virtualizer;strength=" + Short.toString(this.strength));
        }
    }

    public Virtualizer(int priority, int audioSession) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        boolean z = true;
        super(EFFECT_TYPE_VIRTUALIZER, EFFECT_TYPE_NULL, priority, audioSession);
        this.mStrengthSupported = false;
        this.mParamListener = null;
        this.mBaseParamListener = null;
        this.mParamListenerLock = new Object();
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching a Virtualizer to global output mix is deprecated!");
        }
        int[] value = new int[1];
        checkStatus(getParameter(0, value));
        if (value[0] == 0) {
            z = false;
        }
        this.mStrengthSupported = z;
    }

    public boolean getStrengthSupported() {
        return this.mStrengthSupported;
    }

    public void setStrength(short strength) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(1, strength));
    }

    public short getRoundedStrength() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] value = new short[1];
        checkStatus(getParameter(1, value));
        return value[0];
    }

    private boolean getAnglesInt(int inputChannelMask, int deviceType, int[] angles) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        if (inputChannelMask == 0) {
            throw new IllegalArgumentException("Virtualizer: illegal CHANNEL_INVALID channel mask");
        }
        int channelMask;
        if (inputChannelMask == 1) {
            channelMask = 12;
        } else {
            channelMask = inputChannelMask;
        }
        int nbChannels = AudioFormat.channelCountFromOutChannelMask(channelMask);
        if (angles == null || angles.length >= nbChannels * 3) {
            ByteBuffer paramsConverter = ByteBuffer.allocate(12);
            paramsConverter.order(ByteOrder.nativeOrder());
            paramsConverter.putInt(2);
            paramsConverter.putInt(AudioFormat.convertChannelOutMaskToNativeMask(channelMask));
            paramsConverter.putInt(AudioDeviceInfo.convertDeviceTypeToInternalDevice(deviceType));
            byte[] result = new byte[((nbChannels * 4) * 3)];
            int status = getParameter(paramsConverter.array(), result);
            if (status >= 0) {
                if (angles != null) {
                    ByteBuffer resultConverter = ByteBuffer.wrap(result);
                    resultConverter.order(ByteOrder.nativeOrder());
                    for (int i = 0; i < nbChannels; i++) {
                        angles[i * 3] = AudioFormat.convertNativeChannelMaskToOutMask(resultConverter.getInt((i * 4) * 3));
                        angles[(i * 3) + 1] = resultConverter.getInt(((i * 4) * 3) + 4);
                        angles[(i * 3) + 2] = resultConverter.getInt(((i * 4) * 3) + 8);
                    }
                }
                return true;
            } else if (status == -4) {
                return false;
            } else {
                checkStatus(status);
                Log.e(TAG, "unexpected status code " + status + " after getParameter(PARAM_VIRTUAL_SPEAKER_ANGLES)");
                return false;
            }
        }
        Log.e(TAG, "Size of array for angles cannot accomodate number of channels in mask (" + nbChannels + ")");
        throw new IllegalArgumentException("Virtualizer: array for channel / angle pairs is too small: is " + angles.length + ", should be " + (nbChannels * 3));
    }

    private static int getDeviceForModeQuery(int virtualizationMode) throws IllegalArgumentException {
        switch (virtualizationMode) {
            case 2:
                return 4;
            case 3:
                return 2;
            default:
                throw new IllegalArgumentException("Virtualizer: illegal virtualization mode " + virtualizationMode);
        }
    }

    private static int getDeviceForModeForce(int virtualizationMode) throws IllegalArgumentException {
        if (virtualizationMode == 1) {
            return 0;
        }
        return getDeviceForModeQuery(virtualizationMode);
    }

    private static int deviceToMode(int deviceType) {
        switch (deviceType) {
            case 1:
            case 3:
            case 4:
            case 7:
                return 2;
            case 2:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 19:
                return 3;
            default:
                return 0;
        }
    }

    public boolean canVirtualize(int inputChannelMask, int virtualizationMode) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        return getAnglesInt(inputChannelMask, getDeviceForModeQuery(virtualizationMode), null);
    }

    public boolean getSpeakerAngles(int inputChannelMask, int virtualizationMode, int[] angles) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        if (angles != null) {
            return getAnglesInt(inputChannelMask, getDeviceForModeQuery(virtualizationMode), angles);
        }
        throw new IllegalArgumentException("Virtualizer: illegal null channel / angle array");
    }

    public boolean forceVirtualizationMode(int virtualizationMode) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        int status = setParameter(3, AudioDeviceInfo.convertDeviceTypeToInternalDevice(getDeviceForModeForce(virtualizationMode)));
        if (status >= 0) {
            return true;
        }
        if (status == -4) {
            return false;
        }
        checkStatus(status);
        Log.e(TAG, "unexpected status code " + status + " after setParameter(PARAM_FORCE_VIRTUALIZATION_MODE)");
        return false;
    }

    public int getVirtualizationMode() throws IllegalStateException, UnsupportedOperationException {
        int[] value = new int[1];
        int status = getParameter(4, value);
        if (status >= 0) {
            return deviceToMode(AudioDeviceInfo.convertInternalDeviceToDeviceType(value[0]));
        }
        if (status == -4) {
            return 0;
        }
        checkStatus(status);
        Log.e(TAG, "unexpected status code " + status + " after getParameter(PARAM_VIRTUALIZATION_MODE)");
        return 0;
    }

    public void setParameterListener(OnParameterChangeListener listener) {
        synchronized (this.mParamListenerLock) {
            if (this.mParamListener == null) {
                this.mParamListener = listener;
                this.mBaseParamListener = new BaseParameterListener(this, null);
                super.setParameterListener(this.mBaseParamListener);
            }
        }
    }

    public Settings getProperties() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        Settings settings = new Settings();
        short[] value = new short[1];
        checkStatus(getParameter(1, value));
        settings.strength = value[0];
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(1, settings.strength));
    }
}
