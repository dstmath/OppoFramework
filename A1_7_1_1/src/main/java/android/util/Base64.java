package android.util;

import java.io.UnsupportedEncodingException;

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
public class Base64 {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f2-assertionsDisabled = false;
    public static final int CRLF = 4;
    public static final int DEFAULT = 0;
    public static final int NO_CLOSE = 16;
    public static final int NO_PADDING = 1;
    public static final int NO_WRAP = 2;
    public static final int URL_SAFE = 8;

    static abstract class Coder {
        public int op;
        public byte[] output;

        public abstract int maxOutputSize(int i);

        public abstract boolean process(byte[] bArr, int i, int i2, boolean z);

        Coder() {
        }
    }

    static class Decoder extends Coder {
        private static final int[] DECODE = null;
        private static final int[] DECODE_WEBSAFE = null;
        private static final int EQUALS = -2;
        private static final int SKIP = -1;
        private final int[] alphabet;
        private int state;
        private int value;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.util.Base64.Decoder.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.util.Base64.Decoder.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.Base64.Decoder.<clinit>():void");
        }

        public Decoder(int flags, byte[] output) {
            this.output = output;
            this.alphabet = (flags & 8) == 0 ? DECODE : DECODE_WEBSAFE;
            this.state = 0;
            this.value = 0;
        }

        public int maxOutputSize(int len) {
            return ((len * 3) / 4) + 10;
        }

        /* JADX WARNING: Removed duplicated region for block: B:76:0x005c A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x00fd  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x005f  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean process(byte[] input, int offset, int len, boolean finish) {
            if (this.state == 6) {
                return false;
            }
            int op;
            int p = offset;
            len += offset;
            int state = this.state;
            int value = this.value;
            int op2 = 0;
            byte[] output = this.output;
            int[] alphabet = this.alphabet;
            while (p < len) {
                if (state == 0) {
                    while (p + 4 <= len) {
                        value = (((alphabet[input[p] & 255] << 18) | (alphabet[input[p + 1] & 255] << 12)) | (alphabet[input[p + 2] & 255] << 6)) | alphabet[input[p + 3] & 255];
                        if (value >= 0) {
                            output[op2 + 2] = (byte) value;
                            output[op2 + 1] = (byte) (value >> 8);
                            output[op2] = (byte) (value >> 16);
                            op2 += 3;
                            p += 4;
                        } else if (p >= len) {
                            op = op2;
                            if (finish) {
                                this.state = state;
                                this.value = value;
                                this.op = op;
                                return true;
                            }
                            switch (state) {
                                case 0:
                                    op2 = op;
                                    break;
                                case 1:
                                    this.state = 6;
                                    return false;
                                case 2:
                                    op2 = op + 1;
                                    output[op] = (byte) (value >> 4);
                                    break;
                                case 3:
                                    op2 = op + 1;
                                    output[op] = (byte) (value >> 10);
                                    op = op2 + 1;
                                    output[op2] = (byte) (value >> 2);
                                    op2 = op;
                                    break;
                                case 4:
                                    this.state = 6;
                                    return false;
                                case 5:
                                    op2 = op;
                                    break;
                                default:
                                    op2 = op;
                                    break;
                            }
                            this.state = state;
                            this.op = op2;
                            return true;
                        }
                    }
                    if (p >= len) {
                    }
                }
                int p2 = p + 1;
                int d = alphabet[input[p] & 255];
                switch (state) {
                    case 0:
                        if (d < 0) {
                            if (d == -1) {
                                break;
                            }
                            this.state = 6;
                            return false;
                        }
                        value = d;
                        state++;
                        break;
                    case 1:
                        if (d < 0) {
                            if (d == -1) {
                                break;
                            }
                            this.state = 6;
                            return false;
                        }
                        value = (value << 6) | d;
                        state++;
                        break;
                    case 2:
                        if (d < 0) {
                            if (d != -2) {
                                if (d == -1) {
                                    break;
                                }
                                this.state = 6;
                                return false;
                            }
                            op = op2 + 1;
                            output[op2] = (byte) (value >> 4);
                            state = 4;
                            op2 = op;
                            break;
                        }
                        value = (value << 6) | d;
                        state++;
                        break;
                    case 3:
                        if (d < 0) {
                            if (d != -2) {
                                if (d == -1) {
                                    break;
                                }
                                this.state = 6;
                                return false;
                            }
                            output[op2 + 1] = (byte) (value >> 2);
                            output[op2] = (byte) (value >> 10);
                            op2 += 2;
                            state = 5;
                            break;
                        }
                        value = (value << 6) | d;
                        output[op2 + 2] = (byte) value;
                        output[op2 + 1] = (byte) (value >> 8);
                        output[op2] = (byte) (value >> 16);
                        op2 += 3;
                        state = 0;
                        break;
                    case 4:
                        if (d != -2) {
                            if (d == -1) {
                                break;
                            }
                            this.state = 6;
                            return false;
                        }
                        state++;
                        break;
                    case 5:
                        if (d == -1) {
                            break;
                        }
                        this.state = 6;
                        return false;
                    default:
                        break;
                }
                p = p2;
            }
            op = op2;
            if (finish) {
            }
        }
    }

    static class Encoder extends Coder {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f3-assertionsDisabled = false;
        private static final byte[] ENCODE = null;
        private static final byte[] ENCODE_WEBSAFE = null;
        public static final int LINE_GROUPS = 19;
        private final byte[] alphabet;
        private int count;
        public final boolean do_cr;
        public final boolean do_newline;
        public final boolean do_padding;
        private final byte[] tail;
        int tailLen;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.util.Base64.Encoder.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.util.Base64.Encoder.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.Base64.Encoder.<clinit>():void");
        }

        public Encoder(int flags, byte[] output) {
            boolean z;
            int i;
            boolean z2 = true;
            this.output = output;
            if ((flags & 1) == 0) {
                z = true;
            } else {
                z = false;
            }
            this.do_padding = z;
            if ((flags & 2) == 0) {
                z = true;
            } else {
                z = false;
            }
            this.do_newline = z;
            if ((flags & 4) == 0) {
                z2 = false;
            }
            this.do_cr = z2;
            this.alphabet = (flags & 8) == 0 ? ENCODE : ENCODE_WEBSAFE;
            this.tail = new byte[2];
            this.tailLen = 0;
            if (this.do_newline) {
                i = 19;
            } else {
                i = -1;
            }
            this.count = i;
        }

        public int maxOutputSize(int len) {
            return ((len * 8) / 5) + 10;
        }

        /* JADX WARNING: Removed duplicated region for block: B:88:0x021c  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x00ff  */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x0057  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x00ff  */
        /* JADX WARNING: Removed duplicated region for block: B:88:0x021c  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean process(byte[] input, int offset, int len, boolean finish) {
            int p;
            int i;
            byte[] alphabet = this.alphabet;
            byte[] output = this.output;
            int op = 0;
            int count = this.count;
            int p2 = offset;
            len += offset;
            int v = -1;
            switch (this.tailLen) {
                case 1:
                    if (offset + 2 <= len) {
                        p2 = offset + 1;
                        p = p2 + 1;
                        v = (((this.tail[0] & 255) << 16) | ((input[offset] & 255) << 8)) | (input[p2] & 255);
                        this.tailLen = 0;
                        p2 = p;
                        break;
                    }
                    break;
                case 2:
                    if (offset + 1 <= len) {
                        p2 = offset + 1;
                        v = (((this.tail[0] & 255) << 16) | ((this.tail[1] & 255) << 8)) | (input[offset] & 255);
                        this.tailLen = 0;
                        break;
                    }
                    break;
            }
            if (v != -1) {
                output[0] = alphabet[(v >> 18) & 63];
                i = 1 + 1;
                output[1] = alphabet[(v >> 12) & 63];
                op = i + 1;
                output[i] = alphabet[(v >> 6) & 63];
                i = op + 1;
                output[op] = alphabet[v & 63];
                count--;
                if (count == 0) {
                    if (this.do_cr) {
                        op = i + 1;
                        output[i] = (byte) 13;
                    } else {
                        op = i;
                    }
                    i = op + 1;
                    output[op] = (byte) 10;
                    count = 19;
                    p = p2;
                } else {
                    p = p2;
                }
                if (p + 3 > len) {
                    v = (((input[p] & 255) << 16) | ((input[p + 1] & 255) << 8)) | (input[p + 2] & 255);
                    output[i] = alphabet[(v >> 18) & 63];
                    output[i + 1] = alphabet[(v >> 12) & 63];
                    output[i + 2] = alphabet[(v >> 6) & 63];
                    output[i + 3] = alphabet[v & 63];
                    p2 = p + 3;
                    op = i + 4;
                    count--;
                    if (count == 0) {
                        if (this.do_cr) {
                            i = op + 1;
                            output[op] = (byte) 13;
                            op = i;
                        }
                        i = op + 1;
                        output[op] = (byte) 10;
                        count = 19;
                        op = i;
                    }
                }
                int i2;
                byte[] bArr;
                if (finish) {
                    int t;
                    int i3;
                    if (p - this.tailLen == len - 1) {
                        t = 0;
                        if (this.tailLen > 0) {
                            t = 1;
                            i3 = this.tail[0];
                            p2 = p;
                        } else {
                            p2 = p + 1;
                            i3 = input[p];
                        }
                        v = (i3 & 255) << 4;
                        this.tailLen -= t;
                        op = i + 1;
                        output[i] = alphabet[(v >> 6) & 63];
                        i = op + 1;
                        output[op] = alphabet[v & 63];
                        if (this.do_padding) {
                            op = i + 1;
                            output[i] = (byte) 61;
                            i = op + 1;
                            output[op] = (byte) 61;
                            op = i;
                        } else {
                            op = i;
                        }
                        if (this.do_newline) {
                            if (this.do_cr) {
                                i = op + 1;
                                output[op] = (byte) 13;
                                op = i;
                            }
                            i = op + 1;
                            output[op] = (byte) 10;
                            op = i;
                        }
                    } else if (p - this.tailLen == len - 2) {
                        t = 0;
                        if (this.tailLen > 1) {
                            t = 1;
                            i3 = this.tail[0];
                            p2 = p;
                        } else {
                            p2 = p + 1;
                            i3 = input[p];
                        }
                        i2 = (i3 & 255) << 10;
                        if (this.tailLen > 0) {
                            int t2 = t + 1;
                            i3 = this.tail[t];
                            t = t2;
                        } else {
                            p = p2 + 1;
                            i3 = input[p2];
                            p2 = p;
                        }
                        v = i2 | ((i3 & 255) << 2);
                        this.tailLen -= t;
                        op = i + 1;
                        output[i] = alphabet[(v >> 12) & 63];
                        i = op + 1;
                        output[op] = alphabet[(v >> 6) & 63];
                        op = i + 1;
                        output[i] = alphabet[v & 63];
                        if (this.do_padding) {
                            i = op + 1;
                            output[op] = (byte) 61;
                            op = i;
                        }
                        if (this.do_newline) {
                            if (this.do_cr) {
                                i = op + 1;
                                output[op] = (byte) 13;
                                op = i;
                            }
                            i = op + 1;
                            output[op] = (byte) 10;
                            op = i;
                        }
                    } else if (!this.do_newline || i <= 0 || count == 19) {
                        p2 = p;
                        op = i;
                    } else {
                        if (this.do_cr) {
                            op = i + 1;
                            output[i] = (byte) 13;
                        } else {
                            op = i;
                        }
                        i = op + 1;
                        output[op] = (byte) 10;
                        p2 = p;
                        op = i;
                    }
                    if (!f3-assertionsDisabled) {
                        Object obj;
                        if (this.tailLen == 0) {
                            obj = 1;
                        } else {
                            obj = null;
                        }
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    if (!f3-assertionsDisabled) {
                        if ((p2 == len ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                } else if (p == len - 1) {
                    bArr = this.tail;
                    i2 = this.tailLen;
                    this.tailLen = i2 + 1;
                    bArr[i2] = input[p];
                    p2 = p;
                    op = i;
                } else if (p == len - 2) {
                    bArr = this.tail;
                    i2 = this.tailLen;
                    this.tailLen = i2 + 1;
                    bArr[i2] = input[p];
                    bArr = this.tail;
                    i2 = this.tailLen;
                    this.tailLen = i2 + 1;
                    bArr[i2] = input[p + 1];
                    p2 = p;
                    op = i;
                } else {
                    op = i;
                }
                this.op = op;
                this.count = count;
                return true;
            }
            p = p2;
            i = op;
            if (p + 3 > len) {
                if (finish) {
                }
            }
            if (finish) {
            }
            this.op = op;
            this.count = count;
            return true;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.util.Base64.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.util.Base64.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.Base64.<clinit>():void");
    }

    public static byte[] decode(String str, int flags) {
        return decode(str.getBytes(), flags);
    }

    public static byte[] decode(byte[] input, int flags) {
        return decode(input, 0, input.length, flags);
    }

    public static byte[] decode(byte[] input, int offset, int len, int flags) {
        Decoder decoder = new Decoder(flags, new byte[((len * 3) / 4)]);
        if (!decoder.process(input, offset, len, true)) {
            throw new IllegalArgumentException("bad base-64");
        } else if (decoder.op == decoder.output.length) {
            return decoder.output;
        } else {
            byte[] temp = new byte[decoder.op];
            System.arraycopy(decoder.output, 0, temp, 0, decoder.op);
            return temp;
        }
    }

    public static String encodeToString(byte[] input, int flags) {
        try {
            return new String(encode(input, flags), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static String encodeToString(byte[] input, int offset, int len, int flags) {
        try {
            return new String(encode(input, offset, len, flags), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static byte[] encode(byte[] input, int flags) {
        return encode(input, 0, input.length, flags);
    }

    public static byte[] encode(byte[] input, int offset, int len, int flags) {
        boolean z = true;
        Encoder encoder = new Encoder(flags, null);
        int output_len = (len / 3) * 4;
        if (!encoder.do_padding) {
            switch (len % 3) {
                case 1:
                    output_len += 2;
                    break;
                case 2:
                    output_len += 3;
                    break;
            }
        } else if (len % 3 > 0) {
            output_len += 4;
        }
        if (encoder.do_newline && len > 0) {
            int i;
            int i2 = ((len - 1) / 57) + 1;
            if (encoder.do_cr) {
                i = 2;
            } else {
                i = 1;
            }
            output_len += i * i2;
        }
        encoder.output = new byte[output_len];
        encoder.process(input, offset, len, true);
        if (!f2-assertionsDisabled) {
            if (encoder.op != output_len) {
                z = false;
            }
            if (!z) {
                throw new AssertionError();
            }
        }
        return encoder.output;
    }

    private Base64() {
    }
}
