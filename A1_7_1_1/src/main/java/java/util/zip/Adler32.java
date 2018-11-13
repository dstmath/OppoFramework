package java.util.zip;

import java.nio.ByteBuffer;
import sun.nio.ch.DirectBuffer;

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
public class Adler32 implements Checksum {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f116-assertionsDisabled = false;
    private int adler;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.zip.Adler32.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.zip.Adler32.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.zip.Adler32.<clinit>():void");
    }

    private static native int update(int i, int i2);

    private static native int updateByteBuffer(int i, long j, int i2, int i3);

    private static native int updateBytes(int i, byte[] bArr, int i2, int i3);

    public Adler32() {
        this.adler = 1;
    }

    public void update(int b) {
        this.adler = update(this.adler, b);
    }

    public void update(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            this.adler = updateBytes(this.adler, b, off, len);
        }
    }

    public void update(byte[] b) {
        this.adler = updateBytes(this.adler, b, 0, b.length);
    }

    private void update(ByteBuffer buffer) {
        int pos = buffer.position();
        int limit = buffer.limit();
        if (!f116-assertionsDisabled) {
            if ((pos <= limit ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        int rem = limit - pos;
        if (rem > 0) {
            if (buffer instanceof DirectBuffer) {
                this.adler = updateByteBuffer(this.adler, ((DirectBuffer) buffer).address(), pos, rem);
            } else if (buffer.hasArray()) {
                this.adler = updateBytes(this.adler, buffer.array(), buffer.arrayOffset() + pos, rem);
            } else {
                byte[] b = new byte[rem];
                buffer.get(b);
                this.adler = updateBytes(this.adler, b, 0, b.length);
            }
            buffer.position(limit);
        }
    }

    public void reset() {
        this.adler = 1;
    }

    public long getValue() {
        return ((long) this.adler) & 4294967295L;
    }
}
