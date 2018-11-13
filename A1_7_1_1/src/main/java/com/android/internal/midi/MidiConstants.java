package com.android.internal.midi;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public final class MidiConstants {
    public static final int[] CHANNEL_BYTE_LENGTHS = null;
    public static final byte STATUS_ACTIVE_SENSING = (byte) -2;
    public static final byte STATUS_CHANNEL_MASK = (byte) 15;
    public static final byte STATUS_CHANNEL_PRESSURE = (byte) -48;
    public static final byte STATUS_COMMAND_MASK = (byte) -16;
    public static final byte STATUS_CONTINUE = (byte) -5;
    public static final byte STATUS_CONTROL_CHANGE = (byte) -80;
    public static final byte STATUS_END_SYSEX = (byte) -9;
    public static final byte STATUS_MIDI_TIME_CODE = (byte) -15;
    public static final byte STATUS_NOTE_OFF = Byte.MIN_VALUE;
    public static final byte STATUS_NOTE_ON = (byte) -112;
    public static final byte STATUS_PITCH_BEND = (byte) -32;
    public static final byte STATUS_POLYPHONIC_AFTERTOUCH = (byte) -96;
    public static final byte STATUS_PROGRAM_CHANGE = (byte) -64;
    public static final byte STATUS_RESET = (byte) -1;
    public static final byte STATUS_SONG_POSITION = (byte) -14;
    public static final byte STATUS_SONG_SELECT = (byte) -13;
    public static final byte STATUS_START = (byte) -6;
    public static final byte STATUS_STOP = (byte) -4;
    public static final byte STATUS_SYSTEM_EXCLUSIVE = (byte) -16;
    public static final byte STATUS_TIMING_CLOCK = (byte) -8;
    public static final byte STATUS_TUNE_REQUEST = (byte) -10;
    public static final int[] SYSTEM_BYTE_LENGTHS = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.midi.MidiConstants.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.midi.MidiConstants.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.midi.MidiConstants.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.midi.MidiConstants.<init>():void, dex: 
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
    public MidiConstants() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.midi.MidiConstants.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.midi.MidiConstants.<init>():void");
    }

    public static int getBytesPerMessage(byte statusByte) {
        int statusInt = statusByte & 255;
        if (statusInt >= 240) {
            return SYSTEM_BYTE_LENGTHS[statusInt & 15];
        }
        if (statusInt >= 128) {
            return CHANNEL_BYTE_LENGTHS[(statusInt >> 4) - 8];
        }
        return 0;
    }

    public static boolean isAllActiveSensing(byte[] msg, int offset, int count) {
        int goodBytes = 0;
        for (int i = 0; i < count; i++) {
            if (msg[offset + i] != (byte) -2) {
                goodBytes++;
            }
        }
        if (goodBytes == 0) {
            return true;
        }
        return false;
    }

    public static boolean allowRunningStatus(byte command) {
        return command >= STATUS_NOTE_OFF && command < (byte) -16;
    }

    public static boolean cancelsRunningStatus(byte command) {
        return command >= (byte) -16 && command <= (byte) -9;
    }
}
