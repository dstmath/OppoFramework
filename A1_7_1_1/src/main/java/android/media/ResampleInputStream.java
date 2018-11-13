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
public final class ResampleInputStream extends InputStream {
    private static final String TAG = "ResampleInputStream";
    private static final int mFirLength = 29;
    private byte[] mBuf;
    private int mBufCount;
    private InputStream mInputStream;
    private final byte[] mOneByte;
    private final int mRateIn;
    private final int mRateOut;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.ResampleInputStream.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.ResampleInputStream.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.ResampleInputStream.<clinit>():void");
    }

    private static native void fir21(byte[] bArr, int i, byte[] bArr2, int i2, int i3);

    public ResampleInputStream(InputStream inputStream, int rateIn, int rateOut) {
        this.mOneByte = new byte[1];
        if (rateIn != rateOut * 2) {
            throw new IllegalArgumentException("only support 2:1 at the moment");
        }
        this.mInputStream = inputStream;
        this.mRateIn = 2;
        this.mRateOut = 1;
    }

    public int read() throws IOException {
        return read(this.mOneByte, 0, 1) == 1 ? this.mOneByte[0] & 255 : -1;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int offset, int length) throws IOException {
        if (this.mInputStream == null) {
            throw new IllegalStateException("not open");
        }
        int nIn = ((((length / 2) * this.mRateIn) / this.mRateOut) + 29) * 2;
        if (this.mBuf == null) {
            this.mBuf = new byte[nIn];
        } else if (nIn > this.mBuf.length) {
            byte[] bf = new byte[nIn];
            System.arraycopy(this.mBuf, 0, bf, 0, this.mBufCount);
            this.mBuf = bf;
        }
        while (true) {
            int len = ((((this.mBufCount / 2) - 29) * this.mRateOut) / this.mRateIn) * 2;
            if (len > 0) {
                length = len < length ? len : (length / 2) * 2;
                fir21(this.mBuf, 0, b, offset, length / 2);
                int nFwd = (this.mRateIn * length) / this.mRateOut;
                this.mBufCount -= nFwd;
                if (this.mBufCount > 0) {
                    System.arraycopy(this.mBuf, nFwd, this.mBuf, 0, this.mBufCount);
                }
                return length;
            }
            int n = this.mInputStream.read(this.mBuf, this.mBufCount, this.mBuf.length - this.mBufCount);
            if (n == -1) {
                return -1;
            }
            this.mBufCount += n;
        }
    }

    public void close() throws IOException {
        try {
            if (this.mInputStream != null) {
                this.mInputStream.close();
            }
            this.mInputStream = null;
        } catch (Throwable th) {
            this.mInputStream = null;
        }
    }

    protected void finalize() throws Throwable {
        if (this.mInputStream != null) {
            close();
            throw new IllegalStateException("someone forgot to close ResampleInputStream");
        }
    }
}
