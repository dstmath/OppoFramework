package sun.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class BASE64Decoder extends CharacterDecoder {
    private static final char[] pem_array = null;
    private static final byte[] pem_convert_array = null;
    byte[] decode_buffer;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.misc.BASE64Decoder.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.misc.BASE64Decoder.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.BASE64Decoder.<clinit>():void");
    }

    public BASE64Decoder() {
        this.decode_buffer = new byte[4];
    }

    protected int bytesPerAtom() {
        return 4;
    }

    protected int bytesPerLine() {
        return 72;
    }

    /* JADX WARNING: Missing block: B:25:0x0056, code:
            switch(r15) {
                case 2: goto L_0x0083;
                case 3: goto L_0x0091;
                case 4: goto L_0x00ac;
                default: goto L_0x0059;
            };
     */
    /* JADX WARNING: Missing block: B:27:0x0064, code:
            r2 = pem_convert_array[r12.decode_buffer[2] & 255];
     */
    /* JADX WARNING: Missing block: B:28:0x006e, code:
            r1 = pem_convert_array[r12.decode_buffer[1] & 255];
            r0 = pem_convert_array[r12.decode_buffer[0] & 255];
     */
    /* JADX WARNING: Missing block: B:29:0x0083, code:
            r14.write((byte) (((r0 << 2) & 252) | ((r1 >>> 4) & 3)));
     */
    /* JADX WARNING: Missing block: B:30:0x0091, code:
            r14.write((byte) (((r0 << 2) & 252) | ((r1 >>> 4) & 3)));
            r14.write((byte) (((r1 << 4) & 240) | ((r2 >>> 2) & 15)));
     */
    /* JADX WARNING: Missing block: B:31:0x00ac, code:
            r14.write((byte) (((r0 << 2) & 252) | ((r1 >>> 4) & 3)));
            r14.write((byte) (((r1 << 4) & 240) | ((r2 >>> 2) & 15)));
            r14.write((byte) (((r2 << 6) & 192) | (r3 & 63)));
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:39:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:40:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void decodeAtom(PushbackInputStream inStream, OutputStream outStream, int rem) throws IOException {
        int a = -1;
        int b = -1;
        int c = -1;
        int d = -1;
        if (rem >= 2) {
            while (true) {
                int i = inStream.read();
                if (i != -1) {
                    if (i != 10 && i != 13) {
                        this.decode_buffer[0] = (byte) i;
                        if (readFully(inStream, this.decode_buffer, 1, rem - 1) != -1) {
                            if (rem > 3 && this.decode_buffer[3] == (byte) 61) {
                                rem = 3;
                            }
                            if (rem > 2 && this.decode_buffer[2] == (byte) 61) {
                                rem = 2;
                            }
                            switch (rem) {
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    d = pem_convert_array[this.decode_buffer[3] & 255];
                                    break;
                            }
                        }
                        throw new CEStreamExhausted();
                    }
                } else {
                    throw new CEStreamExhausted();
                }
            }
        }
        throw new CEFormatException("BASE64Decoder: Not enough bytes for an atom.");
    }
}
