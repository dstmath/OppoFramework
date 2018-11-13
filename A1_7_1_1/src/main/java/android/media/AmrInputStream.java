package android.media;

import java.io.IOException;
import java.io.InputStream;

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
public final class AmrInputStream extends InputStream {
    private static final int SAMPLES_PER_FRAME = 160;
    private static final String TAG = "AmrInputStream";
    private final byte[] mBuf;
    private int mBufIn;
    private int mBufOut;
    private long mGae;
    private InputStream mInputStream;
    private byte[] mOneByte;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.AmrInputStream.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.AmrInputStream.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.AmrInputStream.<clinit>():void");
    }

    private static native void GsmAmrEncoderCleanup(long j);

    private static native void GsmAmrEncoderDelete(long j);

    private static native int GsmAmrEncoderEncode(long j, byte[] bArr, int i, byte[] bArr2, int i2) throws IOException;

    private static native void GsmAmrEncoderInitialize(long j);

    private static native long GsmAmrEncoderNew();

    public AmrInputStream(InputStream inputStream) {
        this.mBuf = new byte[320];
        this.mBufIn = 0;
        this.mBufOut = 0;
        this.mOneByte = new byte[1];
        this.mInputStream = inputStream;
        this.mGae = GsmAmrEncoderNew();
        GsmAmrEncoderInitialize(this.mGae);
    }

    public int read() throws IOException {
        return read(this.mOneByte, 0, 1) == 1 ? this.mOneByte[0] & 255 : -1;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int offset, int length) throws IOException {
        if (this.mGae == 0) {
            throw new IllegalStateException("not open");
        }
        if (this.mBufOut >= this.mBufIn) {
            this.mBufOut = 0;
            this.mBufIn = 0;
            int i = 0;
            while (i < 320) {
                int n = this.mInputStream.read(this.mBuf, i, 320 - i);
                if (n == -1) {
                    return -1;
                }
                i += n;
            }
            this.mBufIn = GsmAmrEncoderEncode(this.mGae, this.mBuf, 0, this.mBuf, 0);
        }
        if (length > this.mBufIn - this.mBufOut) {
            length = this.mBufIn - this.mBufOut;
        }
        System.arraycopy(this.mBuf, this.mBufOut, b, offset, length);
        this.mBufOut += length;
        return length;
    }

    public void close() throws IOException {
        try {
            if (this.mInputStream != null) {
                this.mInputStream.close();
            }
            this.mInputStream = null;
            try {
                if (this.mGae != 0) {
                    GsmAmrEncoderCleanup(this.mGae);
                }
                try {
                    if (this.mGae != 0) {
                        GsmAmrEncoderDelete(this.mGae);
                    }
                    this.mGae = 0;
                } catch (Throwable th) {
                    this.mGae = 0;
                }
            } catch (Throwable th2) {
                this.mGae = 0;
            }
        } catch (Throwable th3) {
            this.mGae = 0;
        }
    }

    protected void finalize() throws Throwable {
        if (this.mGae != 0) {
            close();
            throw new IllegalStateException("someone forgot to close AmrInputStream");
        }
    }
}
