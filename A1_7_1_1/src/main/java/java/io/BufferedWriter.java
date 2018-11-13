package java.io;

import java.security.AccessController;
import sun.security.action.GetPropertyAction;

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
public class BufferedWriter extends Writer {
    private static int defaultCharBufferSize;
    private char[] cb;
    private String lineSeparator;
    private int nChars;
    private int nextChar;
    private Writer out;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.io.BufferedWriter.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.io.BufferedWriter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.BufferedWriter.<clinit>():void");
    }

    public BufferedWriter(Writer out) {
        this(out, defaultCharBufferSize);
    }

    public BufferedWriter(Writer out, int sz) {
        super(out);
        if (sz <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        this.out = out;
        this.cb = new char[sz];
        this.nChars = sz;
        this.nextChar = 0;
        this.lineSeparator = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
    }

    private void ensureOpen() throws IOException {
        if (this.out == null) {
            throw new IOException("Stream closed");
        }
    }

    void flushBuffer() throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (this.nextChar == 0) {
                return;
            }
            this.out.write(this.cb, 0, this.nextChar);
            this.nextChar = 0;
        }
    }

    public void write(int c) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (this.nextChar >= this.nChars) {
                flushBuffer();
            }
            char[] cArr = this.cb;
            int i = this.nextChar;
            this.nextChar = i + 1;
            cArr[i] = (char) c;
        }
    }

    private int min(int a, int b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (off >= 0 && off <= cbuf.length && len >= 0) {
                if (off + len <= cbuf.length && off + len >= 0) {
                    if (len == 0) {
                        return;
                    } else if (len >= this.nChars) {
                        flushBuffer();
                        this.out.write(cbuf, off, len);
                        return;
                    } else {
                        int b = off;
                        int t = off + len;
                        while (b < t) {
                            int d = min(this.nChars - this.nextChar, t - b);
                            System.arraycopy(cbuf, b, this.cb, this.nextChar, d);
                            b += d;
                            this.nextChar += d;
                            if (this.nextChar >= this.nChars) {
                                flushBuffer();
                            }
                        }
                        return;
                    }
                }
            }
            throw new IndexOutOfBoundsException();
        }
    }

    public void write(String s, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            int b = off;
            int t = off + len;
            while (b < t) {
                int d = min(this.nChars - this.nextChar, t - b);
                s.getChars(b, b + d, this.cb, this.nextChar);
                b += d;
                this.nextChar += d;
                if (this.nextChar >= this.nChars) {
                    flushBuffer();
                }
            }
        }
    }

    public void newLine() throws IOException {
        write(this.lineSeparator);
    }

    public void flush() throws IOException {
        synchronized (this.lock) {
            flushBuffer();
            this.out.flush();
        }
    }

    public void close() throws IOException {
        synchronized (this.lock) {
            if (this.out == null) {
                return;
            }
            try {
                flushBuffer();
                this.out.close();
                this.out = null;
                this.cb = null;
            } catch (Throwable th) {
                this.out.close();
                this.out = null;
                this.cb = null;
            }
        }
    }
}
