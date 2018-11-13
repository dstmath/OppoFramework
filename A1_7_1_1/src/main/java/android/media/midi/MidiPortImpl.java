package android.media.midi;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class MidiPortImpl {
    private static final int DATA_PACKET_OVERHEAD = 9;
    public static final int MAX_PACKET_DATA_SIZE = 1015;
    public static final int MAX_PACKET_SIZE = 1024;
    public static final int PACKET_TYPE_DATA = 1;
    public static final int PACKET_TYPE_FLUSH = 2;
    private static final String TAG = "MidiPort";
    private static final int TIMESTAMP_SIZE = 8;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.midi.MidiPortImpl.<init>():void, dex: 
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
    MidiPortImpl() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.midi.MidiPortImpl.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.midi.MidiPortImpl.<init>():void");
    }

    public static int packData(byte[] message, int offset, int size, long timestamp, byte[] dest) {
        if (size > MAX_PACKET_DATA_SIZE) {
            size = MAX_PACKET_DATA_SIZE;
        }
        dest[0] = (byte) 1;
        System.arraycopy(message, offset, dest, 1, size);
        int i = 0;
        int length = size + 1;
        while (i < 8) {
            int length2 = length + 1;
            dest[length] = (byte) ((int) timestamp);
            timestamp >>= 8;
            i++;
            length = length2;
        }
        return length;
    }

    public static int packFlush(byte[] dest) {
        dest[0] = (byte) 2;
        return 1;
    }

    public static int getPacketType(byte[] buffer, int bufferLength) {
        return buffer[0];
    }

    public static int getDataOffset(byte[] buffer, int bufferLength) {
        return 1;
    }

    public static int getDataSize(byte[] buffer, int bufferLength) {
        return bufferLength - 9;
    }

    public static long getPacketTimestamp(byte[] buffer, int bufferLength) {
        int offset = bufferLength;
        long timestamp = 0;
        for (int i = 0; i < 8; i++) {
            offset--;
            timestamp = (timestamp << 8) | ((long) (buffer[offset] & 255));
        }
        return timestamp;
    }
}
