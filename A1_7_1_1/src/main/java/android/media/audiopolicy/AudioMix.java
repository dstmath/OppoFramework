package android.media.audiopolicy;

import android.media.AudioFormat;
import java.util.Objects;

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
public class AudioMix {
    private static final int CALLBACK_FLAGS_ALL = 1;
    public static final int CALLBACK_FLAG_NOTIFY_ACTIVITY = 1;
    public static final int MIX_STATE_DISABLED = -1;
    public static final int MIX_STATE_IDLE = 0;
    public static final int MIX_STATE_MIXING = 1;
    public static final int MIX_TYPE_INVALID = -1;
    public static final int MIX_TYPE_PLAYERS = 0;
    public static final int MIX_TYPE_RECORDERS = 1;
    public static final int ROUTE_FLAG_LOOP_BACK = 2;
    public static final int ROUTE_FLAG_RENDER = 1;
    private static final int ROUTE_FLAG_SUPPORTED = 3;
    int mCallbackFlags;
    String mDeviceAddress;
    final int mDeviceSystemType;
    private AudioFormat mFormat;
    int mMixState;
    private int mMixType;
    private int mRouteFlags;
    private AudioMixingRule mRule;

    public static class Builder {
        private int mCallbackFlags;
        private String mDeviceAddress;
        private int mDeviceSystemType;
        private AudioFormat mFormat;
        private int mRouteFlags;
        private AudioMixingRule mRule;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.audiopolicy.AudioMix.Builder.<init>():void, dex: 
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
        Builder() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.audiopolicy.AudioMix.Builder.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMix.Builder.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.audiopolicy.AudioMix.Builder.<init>(android.media.audiopolicy.AudioMixingRule):void, dex: 
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
        public Builder(android.media.audiopolicy.AudioMixingRule r1) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.audiopolicy.AudioMix.Builder.<init>(android.media.audiopolicy.AudioMixingRule):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMix.Builder.<init>(android.media.audiopolicy.AudioMixingRule):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.media.audiopolicy.AudioMix.Builder.build():android.media.audiopolicy.AudioMix, dex:  in method: android.media.audiopolicy.AudioMix.Builder.build():android.media.audiopolicy.AudioMix, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.media.audiopolicy.AudioMix.Builder.build():android.media.audiopolicy.AudioMix, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public android.media.audiopolicy.AudioMix build() throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: null in method: android.media.audiopolicy.AudioMix.Builder.build():android.media.audiopolicy.AudioMix, dex:  in method: android.media.audiopolicy.AudioMix.Builder.build():android.media.audiopolicy.AudioMix, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMix.Builder.build():android.media.audiopolicy.AudioMix");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.media.audiopolicy.AudioMix.Builder.setCallbackFlags(int):android.media.audiopolicy.AudioMix$Builder, dex:  in method: android.media.audiopolicy.AudioMix.Builder.setCallbackFlags(int):android.media.audiopolicy.AudioMix$Builder, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.media.audiopolicy.AudioMix.Builder.setCallbackFlags(int):android.media.audiopolicy.AudioMix$Builder, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        android.media.audiopolicy.AudioMix.Builder setCallbackFlags(int r1) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: null in method: android.media.audiopolicy.AudioMix.Builder.setCallbackFlags(int):android.media.audiopolicy.AudioMix$Builder, dex:  in method: android.media.audiopolicy.AudioMix.Builder.setCallbackFlags(int):android.media.audiopolicy.AudioMix$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMix.Builder.setCallbackFlags(int):android.media.audiopolicy.AudioMix$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.media.audiopolicy.AudioMix.Builder.setDevice(int, java.lang.String):android.media.audiopolicy.AudioMix$Builder, dex:  in method: android.media.audiopolicy.AudioMix.Builder.setDevice(int, java.lang.String):android.media.audiopolicy.AudioMix$Builder, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.media.audiopolicy.AudioMix.Builder.setDevice(int, java.lang.String):android.media.audiopolicy.AudioMix$Builder, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        android.media.audiopolicy.AudioMix.Builder setDevice(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.media.audiopolicy.AudioMix.Builder.setDevice(int, java.lang.String):android.media.audiopolicy.AudioMix$Builder, dex:  in method: android.media.audiopolicy.AudioMix.Builder.setDevice(int, java.lang.String):android.media.audiopolicy.AudioMix$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMix.Builder.setDevice(int, java.lang.String):android.media.audiopolicy.AudioMix$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMix.Builder.setDevice(android.media.AudioDeviceInfo):android.media.audiopolicy.AudioMix$Builder, dex: 
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
        public android.media.audiopolicy.AudioMix.Builder setDevice(android.media.AudioDeviceInfo r1) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMix.Builder.setDevice(android.media.AudioDeviceInfo):android.media.audiopolicy.AudioMix$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMix.Builder.setDevice(android.media.AudioDeviceInfo):android.media.audiopolicy.AudioMix$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.audiopolicy.AudioMix.Builder.setFormat(android.media.AudioFormat):android.media.audiopolicy.AudioMix$Builder, dex: 
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
        public android.media.audiopolicy.AudioMix.Builder setFormat(android.media.AudioFormat r1) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.audiopolicy.AudioMix.Builder.setFormat(android.media.AudioFormat):android.media.audiopolicy.AudioMix$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMix.Builder.setFormat(android.media.AudioFormat):android.media.audiopolicy.AudioMix$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.audiopolicy.AudioMix.Builder.setMixingRule(android.media.audiopolicy.AudioMixingRule):android.media.audiopolicy.AudioMix$Builder, dex: 
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
        android.media.audiopolicy.AudioMix.Builder setMixingRule(android.media.audiopolicy.AudioMixingRule r1) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.audiopolicy.AudioMix.Builder.setMixingRule(android.media.audiopolicy.AudioMixingRule):android.media.audiopolicy.AudioMix$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMix.Builder.setMixingRule(android.media.audiopolicy.AudioMixingRule):android.media.audiopolicy.AudioMix$Builder");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMix.Builder.setRouteFlags(int):android.media.audiopolicy.AudioMix$Builder, dex: 
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
        public android.media.audiopolicy.AudioMix.Builder setRouteFlags(int r1) throws java.lang.IllegalArgumentException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.audiopolicy.AudioMix.Builder.setRouteFlags(int):android.media.audiopolicy.AudioMix$Builder, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.audiopolicy.AudioMix.Builder.setRouteFlags(int):android.media.audiopolicy.AudioMix$Builder");
        }
    }

    /* synthetic */ AudioMix(AudioMixingRule rule, AudioFormat format, int routeFlags, int callbackFlags, int deviceType, String deviceAddress, AudioMix audioMix) {
        this(rule, format, routeFlags, callbackFlags, deviceType, deviceAddress);
    }

    private AudioMix(AudioMixingRule rule, AudioFormat format, int routeFlags, int callbackFlags, int deviceType, String deviceAddress) {
        this.mMixType = -1;
        this.mMixState = -1;
        this.mRule = rule;
        this.mFormat = format;
        this.mRouteFlags = routeFlags;
        this.mMixType = rule.getTargetMixType();
        this.mCallbackFlags = callbackFlags;
        this.mDeviceSystemType = deviceType;
        if (deviceAddress == null) {
            deviceAddress = new String("");
        }
        this.mDeviceAddress = deviceAddress;
    }

    public int getMixState() {
        return this.mMixState;
    }

    int getRouteFlags() {
        return this.mRouteFlags;
    }

    AudioFormat getFormat() {
        return this.mFormat;
    }

    AudioMixingRule getRule() {
        return this.mRule;
    }

    public int getMixType() {
        return this.mMixType;
    }

    void setRegistration(String regId) {
        this.mDeviceAddress = regId;
    }

    public String getRegistration() {
        return this.mDeviceAddress;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mRouteFlags), this.mRule, Integer.valueOf(this.mMixType), this.mFormat});
    }
}
