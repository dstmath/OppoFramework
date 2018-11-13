package java.io;

import java.lang.ref.ReferenceQueue;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import sun.reflect.misc.ReflectUtil;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public class ObjectOutputStream extends OutputStream implements ObjectOutput, ObjectStreamConstants {
    private static final boolean extendedDebugInfo = false;
    private final BlockDataOutputStream bout;
    private SerialCallbackContext curContext;
    private PutFieldImpl curPut;
    private final DebugTraceInfoStack debugInfoStack;
    private int depth;
    private final boolean enableOverride;
    private boolean enableReplace;
    private final HandleTable handles;
    private byte[] primVals;
    private int protocol;
    private final ReplaceTable subs;

    /* renamed from: java.io.ObjectOutputStream$1 */
    static class AnonymousClass1 implements PrivilegedAction<Boolean> {
        final /* synthetic */ Class val$subcl;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectOutputStream.1.<init>(java.lang.Class):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(java.lang.Class r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectOutputStream.1.<init>(java.lang.Class):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.1.<init>(java.lang.Class):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.1.run():java.lang.Boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.Boolean run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.1.run():java.lang.Boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.1.run():java.lang.Boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.io.ObjectOutputStream.1.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.io.ObjectOutputStream.1.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.1.run():java.lang.Object");
        }
    }

    private static class BlockDataOutputStream extends OutputStream implements DataOutput {
        private static final int CHAR_BUF_SIZE = 256;
        private static final int MAX_BLOCK_SIZE = 1024;
        private static final int MAX_HEADER_SIZE = 5;
        private boolean blkmode;
        private final byte[] buf;
        private final char[] cbuf;
        private final DataOutputStream dout;
        private final byte[] hbuf;
        private final OutputStream out;
        private int pos;
        private boolean warnOnceWhenWriting;

        BlockDataOutputStream(OutputStream out) {
            this.buf = new byte[1024];
            this.hbuf = new byte[5];
            this.cbuf = new char[256];
            this.blkmode = false;
            this.pos = 0;
            this.out = out;
            this.dout = new DataOutputStream(this);
        }

        boolean setBlockDataMode(boolean mode) throws IOException {
            if (this.blkmode == mode) {
                return this.blkmode;
            }
            drain();
            this.blkmode = mode;
            return !this.blkmode;
        }

        boolean getBlockDataMode() {
            return this.blkmode;
        }

        private void warnIfClosed() {
            if (this.warnOnceWhenWriting) {
                System.logW("The app is relying on undefined behavior. Attempting to write to a closed ObjectOutputStream could produce corrupt output in a future release of Android.", new IOException("Stream Closed"));
                this.warnOnceWhenWriting = false;
            }
        }

        public void write(int b) throws IOException {
            if (this.pos >= 1024) {
                drain();
            }
            byte[] bArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            bArr[i] = (byte) b;
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length, false);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            write(b, off, len, false);
        }

        public void flush() throws IOException {
            drain();
            this.out.flush();
        }

        public void close() throws IOException {
            flush();
            this.out.close();
            this.warnOnceWhenWriting = true;
        }

        void write(byte[] b, int off, int len, boolean copy) throws IOException {
            if (!copy ? this.blkmode : true) {
                while (len > 0) {
                    if (this.pos >= 1024) {
                        drain();
                    }
                    if (len < 1024 || copy || this.pos != 0) {
                        int wlen = Math.min(len, 1024 - this.pos);
                        System.arraycopy(b, off, this.buf, this.pos, wlen);
                        this.pos += wlen;
                        off += wlen;
                        len -= wlen;
                    } else {
                        writeBlockHeader(1024);
                        this.out.write(b, off, 1024);
                        off += 1024;
                        len -= 1024;
                    }
                }
                warnIfClosed();
                return;
            }
            drain();
            this.out.write(b, off, len);
            warnIfClosed();
        }

        void drain() throws IOException {
            if (this.pos != 0) {
                if (this.blkmode) {
                    writeBlockHeader(this.pos);
                }
                this.out.write(this.buf, 0, this.pos);
                this.pos = 0;
                warnIfClosed();
            }
        }

        private void writeBlockHeader(int len) throws IOException {
            if (len <= 255) {
                this.hbuf[0] = ObjectStreamConstants.TC_BLOCKDATA;
                this.hbuf[1] = (byte) len;
                this.out.write(this.hbuf, 0, 2);
            } else {
                this.hbuf[0] = ObjectStreamConstants.TC_BLOCKDATALONG;
                Bits.putInt(this.hbuf, 1, len);
                this.out.write(this.hbuf, 0, 5);
            }
            warnIfClosed();
        }

        public void writeBoolean(boolean v) throws IOException {
            if (this.pos >= 1024) {
                drain();
            }
            byte[] bArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            Bits.putBoolean(bArr, i, v);
        }

        public void writeByte(int v) throws IOException {
            if (this.pos >= 1024) {
                drain();
            }
            byte[] bArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            bArr[i] = (byte) v;
        }

        public void writeChar(int v) throws IOException {
            if (this.pos + 2 <= 1024) {
                Bits.putChar(this.buf, this.pos, (char) v);
                this.pos += 2;
                return;
            }
            this.dout.writeChar(v);
        }

        public void writeShort(int v) throws IOException {
            if (this.pos + 2 <= 1024) {
                Bits.putShort(this.buf, this.pos, (short) v);
                this.pos += 2;
                return;
            }
            this.dout.writeShort(v);
        }

        public void writeInt(int v) throws IOException {
            if (this.pos + 4 <= 1024) {
                Bits.putInt(this.buf, this.pos, v);
                this.pos += 4;
                return;
            }
            this.dout.writeInt(v);
        }

        public void writeFloat(float v) throws IOException {
            if (this.pos + 4 <= 1024) {
                Bits.putFloat(this.buf, this.pos, v);
                this.pos += 4;
                return;
            }
            this.dout.writeFloat(v);
        }

        public void writeLong(long v) throws IOException {
            if (this.pos + 8 <= 1024) {
                Bits.putLong(this.buf, this.pos, v);
                this.pos += 8;
                return;
            }
            this.dout.writeLong(v);
        }

        public void writeDouble(double v) throws IOException {
            if (this.pos + 8 <= 1024) {
                Bits.putDouble(this.buf, this.pos, v);
                this.pos += 8;
                return;
            }
            this.dout.writeDouble(v);
        }

        public void writeBytes(String s) throws IOException {
            int endoff = s.length();
            int cpos = 0;
            int csize = 0;
            int off = 0;
            while (off < endoff) {
                if (cpos >= csize) {
                    cpos = 0;
                    csize = Math.min(endoff - off, 256);
                    s.getChars(off, off + csize, this.cbuf, 0);
                }
                if (this.pos >= 1024) {
                    drain();
                }
                int n = Math.min(csize - cpos, 1024 - this.pos);
                int stop = this.pos + n;
                while (this.pos < stop) {
                    byte[] bArr = this.buf;
                    int i = this.pos;
                    this.pos = i + 1;
                    int cpos2 = cpos + 1;
                    bArr[i] = (byte) this.cbuf[cpos];
                    cpos = cpos2;
                }
                off += n;
            }
        }

        public void writeChars(String s) throws IOException {
            int endoff = s.length();
            int off = 0;
            while (off < endoff) {
                int csize = Math.min(endoff - off, 256);
                s.getChars(off, off + csize, this.cbuf, 0);
                writeChars(this.cbuf, 0, csize);
                off += csize;
            }
        }

        public void writeUTF(String s) throws IOException {
            writeUTF(s, getUTFLength(s));
        }

        void writeBooleans(boolean[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (this.pos >= 1024) {
                    drain();
                }
                int stop = Math.min(endoff, (1024 - this.pos) + off);
                int off2 = off;
                while (off2 < stop) {
                    byte[] bArr = this.buf;
                    int i = this.pos;
                    this.pos = i + 1;
                    off = off2 + 1;
                    Bits.putBoolean(bArr, i, v[off2]);
                    off2 = off;
                }
                off = off2;
            }
        }

        void writeChars(char[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1022) {
                    int stop = Math.min(endoff, off2 + ((1024 - this.pos) >> 1));
                    while (off2 < stop) {
                        off = off2 + 1;
                        Bits.putChar(this.buf, this.pos, v[off2]);
                        this.pos += 2;
                        off2 = off;
                    }
                    off = off2;
                } else {
                    off = off2 + 1;
                    this.dout.writeChar(v[off2]);
                }
                off2 = off;
            }
        }

        void writeShorts(short[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1022) {
                    int stop = Math.min(endoff, off2 + ((1024 - this.pos) >> 1));
                    while (off2 < stop) {
                        off = off2 + 1;
                        Bits.putShort(this.buf, this.pos, v[off2]);
                        this.pos += 2;
                        off2 = off;
                    }
                    off = off2;
                } else {
                    off = off2 + 1;
                    this.dout.writeShort(v[off2]);
                }
                off2 = off;
            }
        }

        void writeInts(int[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1020) {
                    int stop = Math.min(endoff, off2 + ((1024 - this.pos) >> 2));
                    while (off2 < stop) {
                        off = off2 + 1;
                        Bits.putInt(this.buf, this.pos, v[off2]);
                        this.pos += 4;
                        off2 = off;
                    }
                    off = off2;
                } else {
                    off = off2 + 1;
                    this.dout.writeInt(v[off2]);
                }
                off2 = off;
            }
        }

        void writeFloats(float[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1020) {
                    int chunklen = Math.min(endoff - off2, (1024 - this.pos) >> 2);
                    ObjectOutputStream.floatsToBytes(v, off2, this.buf, this.pos, chunklen);
                    off = off2 + chunklen;
                    this.pos += chunklen << 2;
                } else {
                    off = off2 + 1;
                    this.dout.writeFloat(v[off2]);
                }
                off2 = off;
            }
        }

        void writeLongs(long[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1016) {
                    int stop = Math.min(endoff, off2 + ((1024 - this.pos) >> 3));
                    while (off2 < stop) {
                        off = off2 + 1;
                        Bits.putLong(this.buf, this.pos, v[off2]);
                        this.pos += 8;
                        off2 = off;
                    }
                    off = off2;
                } else {
                    off = off2 + 1;
                    this.dout.writeLong(v[off2]);
                }
                off2 = off;
            }
        }

        void writeDoubles(double[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                if (this.pos <= 1016) {
                    int chunklen = Math.min(endoff - off2, (1024 - this.pos) >> 3);
                    ObjectOutputStream.doublesToBytes(v, off2, this.buf, this.pos, chunklen);
                    off = off2 + chunklen;
                    this.pos += chunklen << 3;
                } else {
                    off = off2 + 1;
                    this.dout.writeDouble(v[off2]);
                }
                off2 = off;
            }
        }

        long getUTFLength(String s) {
            int len = s.length();
            long utflen = 0;
            int off = 0;
            while (off < len) {
                int csize = Math.min(len - off, 256);
                s.getChars(off, off + csize, this.cbuf, 0);
                for (int cpos = 0; cpos < csize; cpos++) {
                    long j;
                    char c = this.cbuf[cpos];
                    if (c >= 1 && c <= 127) {
                        j = 1;
                    } else if (c > 2047) {
                        j = 3;
                    } else {
                        j = 2;
                    }
                    utflen += j;
                }
                off += csize;
            }
            return utflen;
        }

        void writeUTF(String s, long utflen) throws IOException {
            if (utflen > 65535) {
                throw new UTFDataFormatException();
            }
            writeShort((int) utflen);
            if (utflen == ((long) s.length())) {
                writeBytes(s);
            } else {
                writeUTFBody(s);
            }
        }

        void writeLongUTF(String s) throws IOException {
            writeLongUTF(s, getUTFLength(s));
        }

        void writeLongUTF(String s, long utflen) throws IOException {
            writeLong(utflen);
            if (utflen == ((long) s.length())) {
                writeBytes(s);
            } else {
                writeUTFBody(s);
            }
        }

        private void writeUTFBody(String s) throws IOException {
            int len = s.length();
            int off = 0;
            while (off < len) {
                int csize = Math.min(len - off, 256);
                s.getChars(off, off + csize, this.cbuf, 0);
                for (int cpos = 0; cpos < csize; cpos++) {
                    char c = this.cbuf[cpos];
                    if (this.pos <= 1021) {
                        if (c <= 127 && c != 0) {
                            byte[] bArr = this.buf;
                            int i = this.pos;
                            this.pos = i + 1;
                            bArr[i] = (byte) c;
                        } else if (c > 2047) {
                            this.buf[this.pos + 2] = (byte) (((c >> 0) & 63) | 128);
                            this.buf[this.pos + 1] = (byte) (((c >> 6) & 63) | 128);
                            this.buf[this.pos + 0] = (byte) (((c >> 12) & 15) | 224);
                            this.pos += 3;
                        } else {
                            this.buf[this.pos + 1] = (byte) (((c >> 0) & 63) | 128);
                            this.buf[this.pos + 0] = (byte) (((c >> 6) & 31) | 192);
                            this.pos += 2;
                        }
                    } else if (c <= 127 && c != 0) {
                        write((int) c);
                    } else if (c > 2047) {
                        write(((c >> 12) & 15) | 224);
                        write(((c >> 6) & 63) | 128);
                        write(((c >> 0) & 63) | 128);
                    } else {
                        write(((c >> 6) & 31) | 192);
                        write(((c >> 0) & 63) | 128);
                    }
                }
                off += csize;
            }
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static class Caches {
        static final ConcurrentMap<WeakClassKey, Boolean> subclassAudits = null;
        static final ReferenceQueue<Class<?>> subclassAuditsQueue = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.io.ObjectOutputStream.Caches.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.io.ObjectOutputStream.Caches.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.Caches.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.io.ObjectOutputStream.Caches.<init>():void, dex: 
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
        private Caches() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.io.ObjectOutputStream.Caches.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.Caches.<init>():void");
        }
    }

    private static class DebugTraceInfoStack {
        private final List<String> stack;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        DebugTraceInfoStack() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.DebugTraceInfoStack.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.clear():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        void clear() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.clear():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.DebugTraceInfoStack.clear():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.pop():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        void pop() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.pop():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.DebugTraceInfoStack.pop():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.push(java.lang.String):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        void push(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.push(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.DebugTraceInfoStack.push(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.toString():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.DebugTraceInfoStack.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.DebugTraceInfoStack.toString():java.lang.String");
        }
    }

    private static class HandleTable {
        private final float loadFactor;
        private int[] next;
        private Object[] objs;
        private int size;
        private int[] spine;
        private int threshold;

        HandleTable(int initialCapacity, float loadFactor) {
            this.loadFactor = loadFactor;
            this.spine = new int[initialCapacity];
            this.next = new int[initialCapacity];
            this.objs = new Object[initialCapacity];
            this.threshold = (int) (((float) initialCapacity) * loadFactor);
            clear();
        }

        int assign(Object obj) {
            if (this.size >= this.next.length) {
                growEntries();
            }
            if (this.size >= this.threshold) {
                growSpine();
            }
            insert(obj, this.size);
            int i = this.size;
            this.size = i + 1;
            return i;
        }

        int lookup(Object obj) {
            if (this.size == 0) {
                return -1;
            }
            int i = this.spine[hash(obj) % this.spine.length];
            while (i >= 0) {
                if (this.objs[i] == obj) {
                    return i;
                }
                i = this.next[i];
            }
            return -1;
        }

        void clear() {
            Arrays.fill(this.spine, -1);
            Arrays.fill(this.objs, 0, this.size, null);
            this.size = 0;
        }

        int size() {
            return this.size;
        }

        private void insert(Object obj, int handle) {
            int index = hash(obj) % this.spine.length;
            this.objs[handle] = obj;
            this.next[handle] = this.spine[index];
            this.spine[index] = handle;
        }

        private void growSpine() {
            this.spine = new int[((this.spine.length << 1) + 1)];
            this.threshold = (int) (((float) this.spine.length) * this.loadFactor);
            Arrays.fill(this.spine, -1);
            for (int i = 0; i < this.size; i++) {
                insert(this.objs[i], i);
            }
        }

        private void growEntries() {
            int newLength = (this.next.length << 1) + 1;
            int[] newNext = new int[newLength];
            System.arraycopy(this.next, 0, newNext, 0, this.size);
            this.next = newNext;
            Object newObjs = new Object[newLength];
            System.arraycopy(this.objs, 0, newObjs, 0, this.size);
            this.objs = newObjs;
        }

        private int hash(Object obj) {
            return System.identityHashCode(obj) & Integer.MAX_VALUE;
        }
    }

    public static abstract class PutField {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.io.ObjectOutputStream.PutField.<init>():void, dex: 
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
        public PutField() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.io.ObjectOutputStream.PutField.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutField.<init>():void");
        }

        public abstract void put(String str, byte b);

        public abstract void put(String str, char c);

        public abstract void put(String str, double d);

        public abstract void put(String str, float f);

        public abstract void put(String str, int i);

        public abstract void put(String str, long j);

        public abstract void put(String str, Object obj);

        public abstract void put(String str, short s);

        public abstract void put(String str, boolean z);

        @Deprecated
        public abstract void write(ObjectOutput objectOutput) throws IOException;
    }

    private class PutFieldImpl extends PutField {
        private final ObjectStreamClass desc;
        private final Object[] objVals;
        private final byte[] primVals;
        final /* synthetic */ ObjectOutputStream this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectOutputStream.PutFieldImpl.<init>(java.io.ObjectOutputStream, java.io.ObjectStreamClass):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        PutFieldImpl(java.io.ObjectOutputStream r1, java.io.ObjectStreamClass r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectOutputStream.PutFieldImpl.<init>(java.io.ObjectOutputStream, java.io.ObjectStreamClass):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.<init>(java.io.ObjectOutputStream, java.io.ObjectStreamClass):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.getFieldOffset(java.lang.String, java.lang.Class):int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private int getFieldOffset(java.lang.String r1, java.lang.Class r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.getFieldOffset(java.lang.String, java.lang.Class):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.getFieldOffset(java.lang.String, java.lang.Class):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, byte):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(java.lang.String r1, byte r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, byte):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, byte):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, char):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(java.lang.String r1, char r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, char):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, char):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, double):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(java.lang.String r1, double r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, double):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, double):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, float):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(java.lang.String r1, float r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, float):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, float):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, long):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(java.lang.String r1, long r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, java.lang.Object):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(java.lang.String r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, short):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(java.lang.String r1, short r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, short):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, short):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, boolean):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(java.lang.String r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.put(java.lang.String, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.write(java.io.ObjectOutput):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void write(java.io.ObjectOutput r1) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.write(java.io.ObjectOutput):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.write(java.io.ObjectOutput):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.writeFields():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        void writeFields() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectOutputStream.PutFieldImpl.writeFields():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.PutFieldImpl.writeFields():void");
        }
    }

    private static class ReplaceTable {
        private final HandleTable htab;
        private Object[] reps;

        ReplaceTable(int initialCapacity, float loadFactor) {
            this.htab = new HandleTable(initialCapacity, loadFactor);
            this.reps = new Object[initialCapacity];
        }

        void assign(Object obj, Object rep) {
            int index = this.htab.assign(obj);
            while (index >= this.reps.length) {
                grow();
            }
            this.reps[index] = rep;
        }

        Object lookup(Object obj) {
            int index = this.htab.lookup(obj);
            return index >= 0 ? this.reps[index] : obj;
        }

        void clear() {
            Arrays.fill(this.reps, 0, this.htab.size(), null);
            this.htab.clear();
        }

        int size() {
            return this.htab.size();
        }

        private void grow() {
            Object newReps = new Object[((this.reps.length << 1) + 1)];
            System.arraycopy(this.reps, 0, newReps, 0, this.reps.length);
            this.reps = newReps;
        }
    }

    private static native void doublesToBytes(double[] dArr, int i, byte[] bArr, int i2, int i3);

    private static native void floatsToBytes(float[] fArr, int i, byte[] bArr, int i2, int i3);

    public ObjectOutputStream(OutputStream out) throws IOException {
        this.protocol = 2;
        verifySubclass();
        this.bout = new BlockDataOutputStream(out);
        this.handles = new HandleTable(10, 3.0f);
        this.subs = new ReplaceTable(10, 3.0f);
        this.enableOverride = false;
        writeStreamHeader();
        this.bout.setBlockDataMode(true);
        this.debugInfoStack = null;
    }

    protected ObjectOutputStream() throws IOException, SecurityException {
        this.protocol = 2;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        this.bout = null;
        this.handles = null;
        this.subs = null;
        this.enableOverride = true;
        this.debugInfoStack = null;
    }

    public void useProtocolVersion(int version) throws IOException {
        if (this.handles.size() != 0) {
            throw new IllegalStateException("stream non-empty");
        }
        switch (version) {
            case 1:
            case 2:
                this.protocol = version;
                return;
            default:
                throw new IllegalArgumentException("unknown version: " + version);
        }
    }

    public final void writeObject(Object obj) throws IOException {
        if (this.enableOverride) {
            writeObjectOverride(obj);
            return;
        }
        try {
            writeObject0(obj, false);
        } catch (IOException ex) {
            if (this.depth == 0) {
                try {
                    writeFatalException(ex);
                } catch (IOException e) {
                }
            }
            throw ex;
        }
    }

    protected void writeObjectOverride(Object obj) throws IOException {
        if (!this.enableOverride) {
            throw new IOException();
        }
    }

    public void writeUnshared(Object obj) throws IOException {
        try {
            writeObject0(obj, true);
        } catch (IOException ex) {
            if (this.depth == 0) {
                writeFatalException(ex);
            }
            throw ex;
        }
    }

    public void defaultWriteObject() throws IOException {
        if (this.curContext == null) {
            throw new NotActiveException("not in call to writeObject");
        }
        Object curObj = this.curContext.getObj();
        ObjectStreamClass curDesc = this.curContext.getDesc();
        this.bout.setBlockDataMode(false);
        defaultWriteFields(curObj, curDesc);
        this.bout.setBlockDataMode(true);
    }

    public PutField putFields() throws IOException {
        if (this.curPut == null) {
            if (this.curContext == null) {
                throw new NotActiveException("not in call to writeObject");
            }
            Object curObj = this.curContext.getObj();
            this.curPut = new PutFieldImpl(this, this.curContext.getDesc());
        }
        return this.curPut;
    }

    public void writeFields() throws IOException {
        if (this.curPut == null) {
            throw new NotActiveException("no current PutField object");
        }
        this.bout.setBlockDataMode(false);
        this.curPut.writeFields();
        this.bout.setBlockDataMode(true);
    }

    public void reset() throws IOException {
        if (this.depth != 0) {
            throw new IOException("stream active");
        }
        this.bout.setBlockDataMode(false);
        this.bout.writeByte(121);
        clear();
        this.bout.setBlockDataMode(true);
    }

    protected void annotateClass(Class<?> cls) throws IOException {
    }

    protected void annotateProxyClass(Class<?> cls) throws IOException {
    }

    protected Object replaceObject(Object obj) throws IOException {
        return obj;
    }

    protected boolean enableReplaceObject(boolean enable) throws SecurityException {
        if (enable == this.enableReplace) {
            return enable;
        }
        if (enable) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        this.enableReplace = enable;
        return !this.enableReplace;
    }

    protected void writeStreamHeader() throws IOException {
        this.bout.writeShort(-21267);
        this.bout.writeShort(5);
    }

    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        desc.writeNonProxy(this);
    }

    public void write(int val) throws IOException {
        this.bout.write(val);
    }

    public void write(byte[] buf) throws IOException {
        this.bout.write(buf, 0, buf.length, false);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (buf == null) {
            throw new NullPointerException();
        }
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.bout.write(buf, off, len, false);
    }

    public void flush() throws IOException {
        this.bout.flush();
    }

    protected void drain() throws IOException {
        this.bout.drain();
    }

    public void close() throws IOException {
        flush();
        this.bout.close();
    }

    public void writeBoolean(boolean val) throws IOException {
        this.bout.writeBoolean(val);
    }

    public void writeByte(int val) throws IOException {
        this.bout.writeByte(val);
    }

    public void writeShort(int val) throws IOException {
        this.bout.writeShort(val);
    }

    public void writeChar(int val) throws IOException {
        this.bout.writeChar(val);
    }

    public void writeInt(int val) throws IOException {
        this.bout.writeInt(val);
    }

    public void writeLong(long val) throws IOException {
        this.bout.writeLong(val);
    }

    public void writeFloat(float val) throws IOException {
        this.bout.writeFloat(val);
    }

    public void writeDouble(double val) throws IOException {
        this.bout.writeDouble(val);
    }

    public void writeBytes(String str) throws IOException {
        this.bout.writeBytes(str);
    }

    public void writeChars(String str) throws IOException {
        this.bout.writeChars(str);
    }

    public void writeUTF(String str) throws IOException {
        this.bout.writeUTF(str);
    }

    int getProtocolVersion() {
        return this.protocol;
    }

    void writeTypeString(String str) throws IOException {
        if (str == null) {
            writeNull();
            return;
        }
        int handle = this.handles.lookup(str);
        if (handle != -1) {
            writeHandle(handle);
        } else {
            writeString(str, false);
        }
    }

    private void verifySubclass() {
        Class cl = getClass();
        if (cl != ObjectOutputStream.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                ObjectStreamClass.processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
                WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
                Boolean result = (Boolean) Caches.subclassAudits.get(key);
                if (result == null) {
                    result = Boolean.valueOf(auditSubclass(cl));
                    Caches.subclassAudits.putIfAbsent(key, result);
                }
                if (!result.booleanValue()) {
                    sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
                }
            }
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static boolean auditSubclass(java.lang.Class r2) {
        /*
        r1 = new java.io.ObjectOutputStream$1;
        r1.<init>(r2);
        r0 = java.security.AccessController.doPrivileged(r1);
        r0 = (java.lang.Boolean) r0;
        r1 = r0.booleanValue();
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectOutputStream.auditSubclass(java.lang.Class):boolean");
    }

    private void clear() {
        this.subs.clear();
        this.handles.clear();
    }

    private void writeObject0(Object obj, boolean unshared) throws IOException {
        Throwable th;
        boolean oldMode = this.bout.setBlockDataMode(false);
        this.depth++;
        try {
            obj = this.subs.lookup(obj);
            if (obj == null) {
                writeNull();
                this.depth--;
                this.bout.setBlockDataMode(oldMode);
                return;
            }
            int h;
            Object obj2;
            if (!unshared) {
                h = this.handles.lookup(obj);
                if (h != -1) {
                    writeHandle(h);
                    this.depth--;
                    this.bout.setBlockDataMode(oldMode);
                    return;
                }
            }
            Object orig = obj;
            Class cl = obj.getClass();
            ObjectStreamClass desc = ObjectStreamClass.lookup(cl, true);
            if (desc.hasWriteReplaceMethod()) {
                obj2 = desc.invokeWriteReplace(obj);
                if (obj2 != null) {
                    try {
                        Class repCl = obj2.getClass();
                        if (repCl != cl) {
                            cl = repCl;
                            desc = ObjectStreamClass.lookup(repCl, true);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        obj = obj2;
                        this.depth--;
                        this.bout.setBlockDataMode(oldMode);
                        throw th;
                    }
                }
            }
            obj2 = obj;
            if (this.enableReplace) {
                Object rep = replaceObject(obj2);
                if (!(rep == obj2 || rep == null)) {
                    cl = rep.getClass();
                    desc = ObjectStreamClass.lookup(cl, true);
                }
                obj2 = rep;
            }
            if (obj2 != obj) {
                this.subs.assign(obj, obj2);
                if (obj2 == null) {
                    writeNull();
                    this.depth--;
                    this.bout.setBlockDataMode(oldMode);
                    return;
                } else if (!unshared) {
                    h = this.handles.lookup(obj2);
                    if (h != -1) {
                        writeHandle(h);
                        this.depth--;
                        this.bout.setBlockDataMode(oldMode);
                        return;
                    }
                }
            }
            if (obj2 instanceof Class) {
                writeClass((Class) obj2, unshared);
            } else if (obj2 instanceof ObjectStreamClass) {
                writeClassDesc((ObjectStreamClass) obj2, unshared);
            } else if (obj2 instanceof String) {
                writeString((String) obj2, unshared);
            } else if (cl.isArray()) {
                writeArray(obj2, desc, unshared);
            } else if (obj2 instanceof Enum) {
                writeEnum((Enum) obj2, desc, unshared);
            } else if (obj2 instanceof Serializable) {
                writeOrdinaryObject(obj2, desc, unshared);
            } else {
                throw new NotSerializableException(cl.getName());
            }
            this.depth--;
            this.bout.setBlockDataMode(oldMode);
        } catch (Throwable th3) {
            th = th3;
            this.depth--;
            this.bout.setBlockDataMode(oldMode);
            throw th;
        }
    }

    private void writeNull() throws IOException {
        this.bout.writeByte(112);
    }

    private void writeHandle(int handle) throws IOException {
        this.bout.writeByte(113);
        this.bout.writeInt(ObjectStreamConstants.baseWireHandle + handle);
    }

    private void writeClass(Class cl, boolean unshared) throws IOException {
        this.bout.writeByte(118);
        writeClassDesc(ObjectStreamClass.lookup(cl, true), false);
        HandleTable handleTable = this.handles;
        if (unshared) {
            cl = null;
        }
        handleTable.assign(cl);
    }

    private void writeClassDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
        if (desc == null) {
            writeNull();
            return;
        }
        if (!unshared) {
            int handle = this.handles.lookup(desc);
            if (handle != -1) {
                writeHandle(handle);
                return;
            }
        }
        if (desc.isProxy()) {
            writeProxyDesc(desc, unshared);
        } else {
            writeNonProxyDesc(desc, unshared);
        }
    }

    private boolean isCustomSubclass() {
        return getClass().getClassLoader() != ObjectOutputStream.class.getClassLoader();
    }

    private void writeProxyDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
        Object obj;
        this.bout.writeByte(125);
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = null;
        } else {
            ObjectStreamClass obj2 = desc;
        }
        handleTable.assign(obj2);
        Class cl = desc.forClass();
        Class[] ifaces = cl.getInterfaces();
        this.bout.writeInt(ifaces.length);
        for (Class name : ifaces) {
            this.bout.writeUTF(name.getName());
        }
        this.bout.setBlockDataMode(true);
        if (isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateProxyClass(cl);
        this.bout.setBlockDataMode(false);
        this.bout.writeByte(120);
        writeClassDesc(desc.getSuperDesc(), false);
    }

    private void writeNonProxyDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
        Object obj;
        this.bout.writeByte(114);
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = null;
        } else {
            ObjectStreamClass obj2 = desc;
        }
        handleTable.assign(obj2);
        if (this.protocol == 1) {
            desc.writeNonProxy(this);
        } else {
            writeClassDescriptor(desc);
        }
        Class cl = desc.forClass();
        this.bout.setBlockDataMode(true);
        if (isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateClass(cl);
        this.bout.setBlockDataMode(false);
        this.bout.writeByte(120);
        writeClassDesc(desc.getSuperDesc(), false);
    }

    private void writeString(String str, boolean unshared) throws IOException {
        Object obj;
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = null;
        } else {
            String obj2 = str;
        }
        handleTable.assign(obj2);
        long utflen = this.bout.getUTFLength(str);
        if (utflen <= 65535) {
            this.bout.writeByte(116);
            this.bout.writeUTF(str, utflen);
            return;
        }
        this.bout.writeByte(124);
        this.bout.writeLongUTF(str, utflen);
    }

    private void writeArray(Object array, ObjectStreamClass desc, boolean unshared) throws IOException {
        Object obj;
        this.bout.writeByte(117);
        writeClassDesc(desc, false);
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = null;
        } else {
            obj = array;
        }
        handleTable.assign(obj);
        Class ccl = desc.forClass().getComponentType();
        if (!ccl.isPrimitive()) {
            this.bout.writeInt(len);
            for (Object obj2 : (Object[]) array) {
                writeObject0(obj2, false);
            }
        } else if (ccl == Integer.TYPE) {
            int[] ia = (int[]) array;
            this.bout.writeInt(ia.length);
            this.bout.writeInts(ia, 0, ia.length);
        } else if (ccl == Byte.TYPE) {
            byte[] ba = (byte[]) array;
            this.bout.writeInt(ba.length);
            this.bout.write(ba, 0, ba.length, true);
        } else if (ccl == Long.TYPE) {
            long[] ja = (long[]) array;
            this.bout.writeInt(ja.length);
            this.bout.writeLongs(ja, 0, ja.length);
        } else if (ccl == Float.TYPE) {
            float[] fa = (float[]) array;
            this.bout.writeInt(fa.length);
            this.bout.writeFloats(fa, 0, fa.length);
        } else if (ccl == Double.TYPE) {
            double[] da = (double[]) array;
            this.bout.writeInt(da.length);
            this.bout.writeDoubles(da, 0, da.length);
        } else if (ccl == Short.TYPE) {
            short[] sa = (short[]) array;
            this.bout.writeInt(sa.length);
            this.bout.writeShorts(sa, 0, sa.length);
        } else if (ccl == Character.TYPE) {
            char[] ca = (char[]) array;
            this.bout.writeInt(ca.length);
            this.bout.writeChars(ca, 0, ca.length);
        } else if (ccl == Boolean.TYPE) {
            boolean[] za = (boolean[]) array;
            this.bout.writeInt(za.length);
            this.bout.writeBooleans(za, 0, za.length);
        } else {
            throw new InternalError();
        }
    }

    private void writeEnum(Enum en, ObjectStreamClass desc, boolean unshared) throws IOException {
        Object obj;
        this.bout.writeByte(126);
        ObjectStreamClass sdesc = desc.getSuperDesc();
        if (sdesc.forClass() != Enum.class) {
            desc = sdesc;
        }
        writeClassDesc(desc, false);
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = null;
        } else {
            Enum obj2 = en;
        }
        handleTable.assign(obj2);
        writeString(en.name(), false);
    }

    private void writeOrdinaryObject(Object obj, ObjectStreamClass desc, boolean unshared) throws IOException {
        Object obj2;
        desc.checkSerialize();
        this.bout.writeByte(115);
        writeClassDesc(desc, false);
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj2 = null;
        } else {
            obj2 = obj;
        }
        handleTable.assign(obj2);
        if (!desc.isExternalizable() || desc.isProxy()) {
            writeSerialData(obj, desc);
        } else {
            writeExternalData((Externalizable) obj);
        }
    }

    private void writeExternalData(Externalizable obj) throws IOException {
        PutFieldImpl oldPut = this.curPut;
        this.curPut = null;
        SerialCallbackContext oldContext = this.curContext;
        try {
            this.curContext = null;
            if (this.protocol == 1) {
                obj.writeExternal(this);
            } else {
                this.bout.setBlockDataMode(true);
                obj.writeExternal(this);
                this.bout.setBlockDataMode(false);
                this.bout.writeByte(120);
            }
            this.curContext = oldContext;
            this.curPut = oldPut;
        } catch (Throwable th) {
            this.curContext = oldContext;
        }
    }

    private void writeSerialData(Object obj, ObjectStreamClass desc) throws IOException {
        ClassDataSlot[] slots = desc.getClassDataLayout();
        for (ClassDataSlot classDataSlot : slots) {
            ObjectStreamClass slotDesc = classDataSlot.desc;
            if (slotDesc.hasWriteObjectMethod()) {
                PutFieldImpl oldPut = this.curPut;
                this.curPut = null;
                SerialCallbackContext oldContext = this.curContext;
                try {
                    this.curContext = new SerialCallbackContext(obj, slotDesc);
                    this.bout.setBlockDataMode(true);
                    slotDesc.invokeWriteObject(obj, this);
                    this.bout.setBlockDataMode(false);
                    this.bout.writeByte(120);
                    this.curPut = oldPut;
                } finally {
                    this.curContext.setUsed();
                    this.curContext = oldContext;
                }
            } else {
                defaultWriteFields(obj, slotDesc);
            }
        }
    }

    private void defaultWriteFields(Object obj, ObjectStreamClass desc) throws IOException {
        desc.checkDefaultSerialize();
        int primDataSize = desc.getPrimDataSize();
        if (this.primVals == null || this.primVals.length < primDataSize) {
            this.primVals = new byte[primDataSize];
        }
        desc.getPrimFieldValues(obj, this.primVals);
        this.bout.write(this.primVals, 0, primDataSize, false);
        ObjectStreamField[] fields = desc.getFields(false);
        Object[] objVals = new Object[desc.getNumObjFields()];
        int numPrimFields = fields.length - objVals.length;
        desc.getObjFieldValues(obj, objVals);
        for (int i = 0; i < objVals.length; i++) {
            writeObject0(objVals[i], fields[numPrimFields + i].isUnshared());
        }
    }

    private void writeFatalException(IOException ex) throws IOException {
        clear();
        boolean oldMode = this.bout.setBlockDataMode(false);
        try {
            this.bout.writeByte(123);
            writeObject0(ex, false);
            clear();
        } finally {
            this.bout.setBlockDataMode(oldMode);
        }
    }
}
