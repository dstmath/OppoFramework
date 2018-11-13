package java.io;

import dalvik.system.VMStack;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;
import sun.reflect.misc.ReflectUtil;

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
public class ObjectInputStream extends InputStream implements ObjectInput, ObjectStreamConstants {
    private static final int NULL_HANDLE = -1;
    private static final HashMap<String, Class<?>> primClasses = null;
    private static final Object unsharedMarker = null;
    private final BlockDataInputStream bin;
    private boolean closed;
    private SerialCallbackContext curContext;
    private boolean defaultDataEnd;
    private int depth;
    private final boolean enableOverride;
    private boolean enableResolve;
    private final HandleTable handles;
    private int passHandle;
    private byte[] primVals;
    private final ValidationList vlist;

    /* renamed from: java.io.ObjectInputStream$1 */
    static class AnonymousClass1 implements PrivilegedAction<Boolean> {
        final /* synthetic */ Class val$subcl;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectInputStream.1.<init>(java.lang.Class):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectInputStream.1.<init>(java.lang.Class):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.1.<init>(java.lang.Class):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.1.run():java.lang.Boolean, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.1.run():java.lang.Boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.1.run():java.lang.Boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.io.ObjectInputStream.1.run():java.lang.Object, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.io.ObjectInputStream.1.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.1.run():java.lang.Object");
        }
    }

    private class BlockDataInputStream extends InputStream implements DataInput {
        private static final int CHAR_BUF_SIZE = 256;
        private static final int HEADER_BLOCKED = -2;
        private static final int MAX_BLOCK_SIZE = 1024;
        private static final int MAX_HEADER_SIZE = 5;
        private boolean blkmode;
        private final byte[] buf;
        private final char[] cbuf;
        private final DataInputStream din;
        private int end;
        private final byte[] hbuf;
        private final PeekInputStream in;
        private int pos;
        final /* synthetic */ ObjectInputStream this$0;
        private int unread;

        BlockDataInputStream(ObjectInputStream this$0, InputStream in) {
            this.this$0 = this$0;
            this.buf = new byte[1024];
            this.hbuf = new byte[5];
            this.cbuf = new char[256];
            this.blkmode = false;
            this.pos = 0;
            this.end = -1;
            this.unread = 0;
            this.in = new PeekInputStream(in);
            this.din = new DataInputStream(this);
        }

        boolean setBlockDataMode(boolean newmode) throws IOException {
            boolean z = false;
            if (this.blkmode == newmode) {
                return this.blkmode;
            }
            if (newmode) {
                this.pos = 0;
                this.end = 0;
                this.unread = 0;
            } else if (this.pos < this.end) {
                throw new IllegalStateException("unread block data");
            }
            this.blkmode = newmode;
            if (!this.blkmode) {
                z = true;
            }
            return z;
        }

        boolean getBlockDataMode() {
            return this.blkmode;
        }

        void skipBlockData() throws IOException {
            if (this.blkmode) {
                while (this.end >= 0) {
                    refill();
                }
                return;
            }
            throw new IllegalStateException("not in block data mode");
        }

        private int readBlockHeader(boolean canBlock) throws IOException {
            if (this.this$0.defaultDataEnd) {
                return -1;
            }
            while (true) {
                int avail = canBlock ? Integer.MAX_VALUE : this.in.available();
                if (avail == 0) {
                    return -2;
                }
                int tc = this.in.peek();
                switch (tc) {
                    case 119:
                        if (avail < 2) {
                            return -2;
                        }
                        this.in.readFully(this.hbuf, 0, 2);
                        return this.hbuf[1] & 255;
                    case 121:
                        try {
                            this.in.read();
                            this.this$0.handleReset();
                        } catch (EOFException e) {
                            throw new StreamCorruptedException("unexpected EOF while reading block data header");
                        }
                    case 122:
                        if (avail < 5) {
                            return -2;
                        }
                        this.in.readFully(this.hbuf, 0, 5);
                        int len = Bits.getInt(this.hbuf, 1);
                        if (len >= 0) {
                            return len;
                        }
                        throw new StreamCorruptedException("illegal block data header length: " + len);
                    default:
                        if (tc < 0 || (tc >= 112 && tc <= 126)) {
                            return -1;
                        }
                        Object[] objArr = new Object[1];
                        objArr[0] = Integer.valueOf(tc);
                        throw new StreamCorruptedException(String.format("invalid type code: %02X", objArr));
                }
                throw new StreamCorruptedException("unexpected EOF while reading block data header");
            }
        }

        private void refill() throws IOException {
            do {
                try {
                    this.pos = 0;
                    int n;
                    if (this.unread > 0) {
                        n = this.in.read(this.buf, 0, Math.min(this.unread, 1024));
                        if (n >= 0) {
                            this.end = n;
                            this.unread -= n;
                        } else {
                            throw new StreamCorruptedException("unexpected EOF in middle of data block");
                        }
                    }
                    n = readBlockHeader(true);
                    if (n >= 0) {
                        this.end = 0;
                        this.unread = n;
                    } else {
                        this.end = -1;
                        this.unread = 0;
                    }
                } catch (IOException ex) {
                    this.pos = 0;
                    this.end = -1;
                    this.unread = 0;
                    throw ex;
                }
            } while (this.pos == this.end);
        }

        int currentBlockRemaining() {
            if (!this.blkmode) {
                throw new IllegalStateException();
            } else if (this.end >= 0) {
                return (this.end - this.pos) + this.unread;
            } else {
                return 0;
            }
        }

        int peek() throws IOException {
            if (!this.blkmode) {
                return this.in.peek();
            }
            if (this.pos == this.end) {
                refill();
            }
            return this.end >= 0 ? this.buf[this.pos] & 255 : -1;
        }

        byte peekByte() throws IOException {
            int val = peek();
            if (val >= 0) {
                return (byte) val;
            }
            throw new EOFException();
        }

        public int read() throws IOException {
            if (!this.blkmode) {
                return this.in.read();
            }
            int i;
            if (this.pos == this.end) {
                refill();
            }
            if (this.end >= 0) {
                byte[] bArr = this.buf;
                int i2 = this.pos;
                this.pos = i2 + 1;
                i = bArr[i2] & 255;
            } else {
                i = -1;
            }
            return i;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            return read(b, off, len, false);
        }

        public long skip(long len) throws IOException {
            long remain = len;
            while (remain > 0) {
                int nread;
                if (!this.blkmode) {
                    nread = this.in.read(this.buf, 0, (int) Math.min(remain, 1024));
                    if (nread < 0) {
                        break;
                    }
                    remain -= (long) nread;
                } else {
                    if (this.pos == this.end) {
                        refill();
                    }
                    if (this.end < 0) {
                        break;
                    }
                    nread = (int) Math.min(remain, (long) (this.end - this.pos));
                    remain -= (long) nread;
                    this.pos += nread;
                }
            }
            return len - remain;
        }

        public int available() throws IOException {
            int i = 0;
            if (!this.blkmode) {
                return this.in.available();
            }
            if (this.pos == this.end && this.unread == 0) {
                int n;
                do {
                    n = readBlockHeader(false);
                } while (n == 0);
                switch (n) {
                    case -2:
                        break;
                    case -1:
                        this.pos = 0;
                        this.end = -1;
                        break;
                    default:
                        this.pos = 0;
                        this.end = 0;
                        this.unread = n;
                        break;
                }
            }
            int unreadAvail = this.unread > 0 ? Math.min(this.in.available(), this.unread) : 0;
            if (this.end >= 0) {
                i = (this.end - this.pos) + unreadAvail;
            }
            return i;
        }

        public void close() throws IOException {
            if (this.blkmode) {
                this.pos = 0;
                this.end = -1;
                this.unread = 0;
            }
            this.in.close();
        }

        int read(byte[] b, int off, int len, boolean copy) throws IOException {
            if (len == 0) {
                return 0;
            }
            int nread;
            if (this.blkmode) {
                if (this.pos == this.end) {
                    refill();
                }
                if (this.end < 0) {
                    return -1;
                }
                nread = Math.min(len, this.end - this.pos);
                System.arraycopy(this.buf, this.pos, b, off, nread);
                this.pos += nread;
                return nread;
            } else if (!copy) {
                return this.in.read(b, off, len);
            } else {
                nread = this.in.read(this.buf, 0, Math.min(len, 1024));
                if (nread > 0) {
                    System.arraycopy(this.buf, 0, b, off, nread);
                }
                return nread;
            }
        }

        public void readFully(byte[] b) throws IOException {
            readFully(b, 0, b.length, false);
        }

        public void readFully(byte[] b, int off, int len) throws IOException {
            readFully(b, off, len, false);
        }

        public void readFully(byte[] b, int off, int len, boolean copy) throws IOException {
            while (len > 0) {
                int n = read(b, off, len, copy);
                if (n < 0) {
                    throw new EOFException();
                }
                off += n;
                len -= n;
            }
        }

        public int skipBytes(int n) throws IOException {
            return this.din.skipBytes(n);
        }

        public boolean readBoolean() throws IOException {
            int v = read();
            if (v < 0) {
                throw new EOFException();
            } else if (v != 0) {
                return true;
            } else {
                return false;
            }
        }

        public byte readByte() throws IOException {
            int v = read();
            if (v >= 0) {
                return (byte) v;
            }
            throw new EOFException();
        }

        public int readUnsignedByte() throws IOException {
            int v = read();
            if (v >= 0) {
                return v;
            }
            throw new EOFException();
        }

        public char readChar() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 2);
            } else if (this.end - this.pos < 2) {
                return this.din.readChar();
            }
            char v = Bits.getChar(this.buf, this.pos);
            this.pos += 2;
            return v;
        }

        public short readShort() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 2);
            } else if (this.end - this.pos < 2) {
                return this.din.readShort();
            }
            short v = Bits.getShort(this.buf, this.pos);
            this.pos += 2;
            return v;
        }

        public int readUnsignedShort() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 2);
            } else if (this.end - this.pos < 2) {
                return this.din.readUnsignedShort();
            }
            int v = Bits.getShort(this.buf, this.pos) & 65535;
            this.pos += 2;
            return v;
        }

        public int readInt() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 4);
            } else if (this.end - this.pos < 4) {
                return this.din.readInt();
            }
            int v = Bits.getInt(this.buf, this.pos);
            this.pos += 4;
            return v;
        }

        public float readFloat() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 4);
            } else if (this.end - this.pos < 4) {
                return this.din.readFloat();
            }
            float v = Bits.getFloat(this.buf, this.pos);
            this.pos += 4;
            return v;
        }

        public long readLong() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 8);
            } else if (this.end - this.pos < 8) {
                return this.din.readLong();
            }
            long v = Bits.getLong(this.buf, this.pos);
            this.pos += 8;
            return v;
        }

        public double readDouble() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 8);
            } else if (this.end - this.pos < 8) {
                return this.din.readDouble();
            }
            double v = Bits.getDouble(this.buf, this.pos);
            this.pos += 8;
            return v;
        }

        public String readUTF() throws IOException {
            return readUTFBody((long) readUnsignedShort());
        }

        public String readLine() throws IOException {
            return this.din.readLine();
        }

        void readBooleans(boolean[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, 1024);
                    this.in.readFully(this.buf, 0, span);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 1) {
                    off = off2 + 1;
                    v[off2] = this.din.readBoolean();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, (this.end + off2) - this.pos);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    byte[] bArr = this.buf;
                    int i = this.pos;
                    this.pos = i + 1;
                    v[off2] = Bits.getBoolean(bArr, i);
                    off2 = off;
                }
            }
        }

        void readChars(char[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, 512);
                    this.in.readFully(this.buf, 0, span << 1);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 2) {
                    off = off2 + 1;
                    v[off2] = this.din.readChar();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, ((this.end - this.pos) >> 1) + off2);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    v[off2] = Bits.getChar(this.buf, this.pos);
                    this.pos += 2;
                    off2 = off;
                }
            }
        }

        void readShorts(short[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, 512);
                    this.in.readFully(this.buf, 0, span << 1);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 2) {
                    off = off2 + 1;
                    v[off2] = this.din.readShort();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, ((this.end - this.pos) >> 1) + off2);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    v[off2] = Bits.getShort(this.buf, this.pos);
                    this.pos += 2;
                    off2 = off;
                }
            }
        }

        void readInts(int[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, 256);
                    this.in.readFully(this.buf, 0, span << 2);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 4) {
                    off = off2 + 1;
                    v[off2] = this.din.readInt();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, ((this.end - this.pos) >> 2) + off2);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    v[off2] = Bits.getInt(this.buf, this.pos);
                    this.pos += 4;
                    off2 = off;
                }
            }
        }

        void readFloats(float[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int span;
                if (!this.blkmode) {
                    span = Math.min(endoff - off2, 256);
                    this.in.readFully(this.buf, 0, span << 2);
                    this.pos = 0;
                } else if (this.end - this.pos < 4) {
                    off = off2 + 1;
                    v[off2] = this.din.readFloat();
                    off2 = off;
                } else {
                    span = Math.min(endoff - off2, (this.end - this.pos) >> 2);
                }
                ObjectInputStream.bytesToFloats(this.buf, this.pos, v, off2, span);
                off = off2 + span;
                this.pos += span << 2;
                off2 = off;
            }
        }

        void readLongs(long[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, 128);
                    this.in.readFully(this.buf, 0, span << 3);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 8) {
                    off = off2 + 1;
                    v[off2] = this.din.readLong();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, ((this.end - this.pos) >> 3) + off2);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    v[off2] = Bits.getLong(this.buf, this.pos);
                    this.pos += 8;
                    off2 = off;
                }
            }
        }

        void readDoubles(double[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int span;
                if (!this.blkmode) {
                    span = Math.min(endoff - off2, 128);
                    this.in.readFully(this.buf, 0, span << 3);
                    this.pos = 0;
                } else if (this.end - this.pos < 8) {
                    off = off2 + 1;
                    v[off2] = this.din.readDouble();
                    off2 = off;
                } else {
                    span = Math.min(endoff - off2, (this.end - this.pos) >> 3);
                }
                ObjectInputStream.bytesToDoubles(this.buf, this.pos, v, off2, span);
                off = off2 + span;
                this.pos += span << 3;
                off2 = off;
            }
        }

        String readLongUTF() throws IOException {
            return readUTFBody(readLong());
        }

        private String readUTFBody(long utflen) throws IOException {
            StringBuilder sbuf = new StringBuilder();
            if (!this.blkmode) {
                this.pos = 0;
                this.end = 0;
            }
            while (utflen > 0) {
                int avail = this.end - this.pos;
                if (avail >= 3 || ((long) avail) == utflen) {
                    utflen -= readUTFSpan(sbuf, utflen);
                } else if (this.blkmode) {
                    utflen -= (long) readUTFChar(sbuf, utflen);
                } else {
                    if (avail > 0) {
                        System.arraycopy(this.buf, this.pos, this.buf, 0, avail);
                    }
                    this.pos = 0;
                    this.end = (int) Math.min(1024, utflen);
                    this.in.readFully(this.buf, avail, this.end - avail);
                }
            }
            return sbuf.toString();
        }

        private long readUTFSpan(StringBuilder sbuf, long utflen) throws IOException {
            int i;
            Throwable th;
            int cpos = 0;
            int start = this.pos;
            int avail = Math.min(this.end - this.pos, 256);
            int i2 = this.pos;
            if (utflen > ((long) avail)) {
                i = avail - 2;
            } else {
                i = (int) utflen;
            }
            int stop = i2 + i;
            while (true) {
                int cpos2;
                try {
                    cpos2 = cpos;
                    if (this.pos < stop) {
                        byte[] bArr = this.buf;
                        i2 = this.pos;
                        this.pos = i2 + 1;
                        int b1 = bArr[i2] & 255;
                        int b2;
                        switch (b1 >> 4) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                                cpos = cpos2 + 1;
                                try {
                                    this.cbuf[cpos2] = (char) b1;
                                    break;
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                    break;
                                }
                            case 12:
                            case 13:
                                bArr = this.buf;
                                i2 = this.pos;
                                this.pos = i2 + 1;
                                b2 = bArr[i2];
                                if ((b2 & 192) == 128) {
                                    cpos = cpos2 + 1;
                                    this.cbuf[cpos2] = (char) (((b1 & 31) << 6) | ((b2 & 63) << 0));
                                    break;
                                }
                                throw new UTFDataFormatException();
                            case 14:
                                int b3 = this.buf[this.pos + 1];
                                b2 = this.buf[this.pos + 0];
                                this.pos += 2;
                                if ((b2 & 192) != 128 || (b3 & 192) != 128) {
                                    break;
                                }
                                cpos = cpos2 + 1;
                                this.cbuf[cpos2] = (char) ((((b1 & 15) << 12) | ((b2 & 63) << 6)) | ((b3 & 63) << 0));
                                break;
                                break;
                            default:
                                throw new UTFDataFormatException();
                        }
                    } else if (null != null || ((long) (this.pos - start)) > utflen) {
                        this.pos = ((int) utflen) + start;
                        throw new UTFDataFormatException();
                    } else {
                        cpos = cpos2;
                    }
                } catch (ArrayIndexOutOfBoundsException e2) {
                    cpos = cpos2;
                } catch (Throwable th3) {
                    th = th3;
                    cpos = cpos2;
                }
            }
            throw new UTFDataFormatException();
            if (null != null || ((long) (this.pos - start)) > utflen) {
                this.pos = ((int) utflen) + start;
                throw new UTFDataFormatException();
            }
            throw th;
            if (true || ((long) (this.pos - start)) > utflen) {
                this.pos = ((int) utflen) + start;
                throw new UTFDataFormatException();
            }
            sbuf.append(this.cbuf, 0, cpos);
            return (long) (this.pos - start);
        }

        private int readUTFChar(StringBuilder sbuf, long utflen) throws IOException {
            int b1 = readByte() & 255;
            int b2;
            switch (b1 >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    sbuf.append((char) b1);
                    return 1;
                case 12:
                case 13:
                    if (utflen < 2) {
                        throw new UTFDataFormatException();
                    }
                    b2 = readByte();
                    if ((b2 & 192) != 128) {
                        throw new UTFDataFormatException();
                    }
                    sbuf.append((char) (((b1 & 31) << 6) | ((b2 & 63) << 0)));
                    return 2;
                case 14:
                    if (utflen < 3) {
                        if (utflen == 2) {
                            readByte();
                        }
                        throw new UTFDataFormatException();
                    }
                    b2 = readByte();
                    int b3 = readByte();
                    if ((b2 & 192) == 128 && (b3 & 192) == 128) {
                        sbuf.append((char) ((((b1 & 15) << 12) | ((b2 & 63) << 6)) | ((b3 & 63) << 0)));
                        return 3;
                    }
                    throw new UTFDataFormatException();
                default:
                    throw new UTFDataFormatException();
            }
        }
    }

    private static class Caches {
        static final ConcurrentMap<WeakClassKey, Boolean> subclassAudits = null;
        static final ReferenceQueue<Class<?>> subclassAuditsQueue = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.io.ObjectInputStream.Caches.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.io.ObjectInputStream.Caches.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.Caches.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.io.ObjectInputStream.Caches.<init>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.io.ObjectInputStream.Caches.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.Caches.<init>():void");
        }
    }

    public static abstract class GetField {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.io.ObjectInputStream.GetField.<init>():void, dex: 
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
        public GetField() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.io.ObjectInputStream.GetField.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetField.<init>():void");
        }

        public abstract boolean defaulted(String str) throws IOException;

        public abstract byte get(String str, byte b) throws IOException;

        public abstract char get(String str, char c) throws IOException;

        public abstract double get(String str, double d) throws IOException;

        public abstract float get(String str, float f) throws IOException;

        public abstract int get(String str, int i) throws IOException;

        public abstract long get(String str, long j) throws IOException;

        public abstract Object get(String str, Object obj) throws IOException;

        public abstract short get(String str, short s) throws IOException;

        public abstract boolean get(String str, boolean z) throws IOException;

        public abstract ObjectStreamClass getObjectStreamClass();
    }

    private class GetFieldImpl extends GetField {
        private final ObjectStreamClass desc;
        private final int[] objHandles;
        private final Object[] objVals;
        private final byte[] primVals;
        final /* synthetic */ ObjectInputStream this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectInputStream.GetFieldImpl.<init>(java.io.ObjectInputStream, java.io.ObjectStreamClass):void, dex: 
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
        GetFieldImpl(java.io.ObjectInputStream r1, java.io.ObjectStreamClass r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectInputStream.GetFieldImpl.<init>(java.io.ObjectInputStream, java.io.ObjectStreamClass):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.<init>(java.io.ObjectInputStream, java.io.ObjectStreamClass):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.getFieldOffset(java.lang.String, java.lang.Class):int, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.getFieldOffset(java.lang.String, java.lang.Class):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.getFieldOffset(java.lang.String, java.lang.Class):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, byte):byte, dex: 
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
        public byte get(java.lang.String r1, byte r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, byte):byte, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, byte):byte");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, char):char, dex: 
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
        public char get(java.lang.String r1, char r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, char):char, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, char):char");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, double):double, dex: 
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
        public double get(java.lang.String r1, double r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, double):double, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, double):double");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, float):float, dex: 
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
        public float get(java.lang.String r1, float r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, float):float, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, float):float");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, int):int, dex: 
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
        public int get(java.lang.String r1, int r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, long):long, dex: 
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
        public long get(java.lang.String r1, long r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, long):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, long):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, java.lang.Object):java.lang.Object, dex: 
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
        public java.lang.Object get(java.lang.String r1, java.lang.Object r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, java.lang.Object):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, java.lang.Object):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, short):short, dex: 
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
        public short get(java.lang.String r1, short r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, short):short, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, short):short");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, boolean):boolean, dex: 
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
        public boolean get(java.lang.String r1, boolean r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, boolean):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.get(java.lang.String, boolean):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.getObjectStreamClass():java.io.ObjectStreamClass, dex: 
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
        public java.io.ObjectStreamClass getObjectStreamClass() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.getObjectStreamClass():java.io.ObjectStreamClass, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.getObjectStreamClass():java.io.ObjectStreamClass");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.readFields():void, dex: 
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
        void readFields() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.GetFieldImpl.readFields():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.GetFieldImpl.readFields():void");
        }

        public boolean defaulted(String name) throws IOException {
            return getFieldOffset(name, null) < 0;
        }
    }

    private static class HandleTable {
        private static final byte STATUS_EXCEPTION = (byte) 3;
        private static final byte STATUS_OK = (byte) 1;
        private static final byte STATUS_UNKNOWN = (byte) 2;
        HandleList[] deps;
        Object[] entries;
        int lowDep;
        int size;
        byte[] status;

        private static class HandleList {
            private int[] list;
            private int size;

            public HandleList() {
                this.list = new int[4];
                this.size = 0;
            }

            public void add(int handle) {
                if (this.size >= this.list.length) {
                    int[] newList = new int[(this.list.length << 1)];
                    System.arraycopy(this.list, 0, newList, 0, this.list.length);
                    this.list = newList;
                }
                int[] iArr = this.list;
                int i = this.size;
                this.size = i + 1;
                iArr[i] = handle;
            }

            public int get(int index) {
                if (index < this.size) {
                    return this.list[index];
                }
                throw new ArrayIndexOutOfBoundsException();
            }

            public int size() {
                return this.size;
            }
        }

        HandleTable(int initialCapacity) {
            this.lowDep = -1;
            this.size = 0;
            this.status = new byte[initialCapacity];
            this.entries = new Object[initialCapacity];
            this.deps = new HandleList[initialCapacity];
        }

        int assign(Object obj) {
            if (this.size >= this.entries.length) {
                grow();
            }
            this.status[this.size] = (byte) 2;
            this.entries[this.size] = obj;
            int i = this.size;
            this.size = i + 1;
            return i;
        }

        void markDependency(int dependent, int target) {
            if (dependent != -1 && target != -1) {
                switch (this.status[dependent]) {
                    case (byte) 2:
                        switch (this.status[target]) {
                            case (byte) 1:
                                break;
                            case (byte) 2:
                                if (this.deps[target] == null) {
                                    this.deps[target] = new HandleList();
                                }
                                this.deps[target].add(dependent);
                                if (this.lowDep < 0 || this.lowDep > target) {
                                    this.lowDep = target;
                                    break;
                                }
                            case (byte) 3:
                                markException(dependent, (ClassNotFoundException) this.entries[target]);
                                break;
                            default:
                                throw new InternalError();
                        }
                        break;
                    case (byte) 3:
                        break;
                    default:
                        throw new InternalError();
                }
            }
        }

        void markException(int handle, ClassNotFoundException ex) {
            switch (this.status[handle]) {
                case (byte) 2:
                    this.status[handle] = (byte) 3;
                    this.entries[handle] = ex;
                    HandleList dlist = this.deps[handle];
                    if (dlist != null) {
                        int ndeps = dlist.size();
                        for (int i = 0; i < ndeps; i++) {
                            markException(dlist.get(i), ex);
                        }
                        this.deps[handle] = null;
                        return;
                    }
                    return;
                case (byte) 3:
                    return;
                default:
                    throw new InternalError();
            }
        }

        void finish(int handle) {
            int end;
            if (this.lowDep < 0) {
                end = handle + 1;
            } else if (this.lowDep >= handle) {
                end = this.size;
                this.lowDep = -1;
            } else {
                return;
            }
            for (int i = handle; i < end; i++) {
                switch (this.status[i]) {
                    case (byte) 1:
                    case (byte) 3:
                        break;
                    case (byte) 2:
                        this.status[i] = (byte) 1;
                        this.deps[i] = null;
                        break;
                    default:
                        throw new InternalError();
                }
            }
        }

        void setObject(int handle, Object obj) {
            switch (this.status[handle]) {
                case (byte) 1:
                case (byte) 2:
                    this.entries[handle] = obj;
                    return;
                case (byte) 3:
                    return;
                default:
                    throw new InternalError();
            }
        }

        Object lookupObject(int handle) {
            return (handle == -1 || this.status[handle] == (byte) 3) ? null : this.entries[handle];
        }

        ClassNotFoundException lookupException(int handle) {
            return (handle == -1 || this.status[handle] != (byte) 3) ? null : (ClassNotFoundException) this.entries[handle];
        }

        void clear() {
            Arrays.fill(this.status, 0, this.size, (byte) 0);
            Arrays.fill(this.entries, 0, this.size, null);
            Arrays.fill(this.deps, 0, this.size, null);
            this.lowDep = -1;
            this.size = 0;
        }

        int size() {
            return this.size;
        }

        private void grow() {
            int newCapacity = (this.entries.length << 1) + 1;
            byte[] newStatus = new byte[newCapacity];
            Object newEntries = new Object[newCapacity];
            Object newDeps = new HandleList[newCapacity];
            System.arraycopy(this.status, 0, newStatus, 0, this.size);
            System.arraycopy(this.entries, 0, newEntries, 0, this.size);
            System.arraycopy(this.deps, 0, newDeps, 0, this.size);
            this.status = newStatus;
            this.entries = newEntries;
            this.deps = newDeps;
        }
    }

    private static class PeekInputStream extends InputStream {
        private final InputStream in;
        private int peekb;

        PeekInputStream(InputStream in) {
            this.peekb = -1;
            this.in = in;
        }

        int peek() throws IOException {
            if (this.peekb >= 0) {
                return this.peekb;
            }
            int read = this.in.read();
            this.peekb = read;
            return read;
        }

        public int read() throws IOException {
            if (this.peekb < 0) {
                return this.in.read();
            }
            int v = this.peekb;
            this.peekb = -1;
            return v;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            if (this.peekb < 0) {
                return this.in.read(b, off, len);
            }
            int off2 = off + 1;
            b[off] = (byte) this.peekb;
            len--;
            this.peekb = -1;
            int n = this.in.read(b, off2, len);
            return n >= 0 ? n + 1 : 1;
        }

        void readFully(byte[] b, int off, int len) throws IOException {
            int n = 0;
            while (n < len) {
                int count = read(b, off + n, len - n);
                if (count < 0) {
                    throw new EOFException();
                }
                n += count;
            }
        }

        public long skip(long n) throws IOException {
            if (n <= 0) {
                return 0;
            }
            int skipped = 0;
            if (this.peekb >= 0) {
                this.peekb = -1;
                skipped = 1;
                n--;
            }
            return ((long) skipped) + skip(n);
        }

        public int available() throws IOException {
            int i = 0;
            int available = this.in.available();
            if (this.peekb >= 0) {
                i = 1;
            }
            return i + available;
        }

        public void close() throws IOException {
            this.in.close();
        }
    }

    private static class ValidationList {
        private Callback list;

        /* renamed from: java.io.ObjectInputStream$ValidationList$1 */
        class AnonymousClass1 implements PrivilegedExceptionAction<Void> {
            final /* synthetic */ ValidationList this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectInputStream.ValidationList.1.<init>(java.io.ObjectInputStream$ValidationList):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass1(java.io.ObjectInputStream.ValidationList r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectInputStream.ValidationList.1.<init>(java.io.ObjectInputStream$ValidationList):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.ValidationList.1.<init>(java.io.ObjectInputStream$ValidationList):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.io.ObjectInputStream.ValidationList.1.run():java.lang.Object, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public /* bridge */ /* synthetic */ java.lang.Object run() throws java.lang.Exception {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.io.ObjectInputStream.ValidationList.1.run():java.lang.Object, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.ValidationList.1.run():java.lang.Object");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.ValidationList.1.run():java.lang.Void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public java.lang.Void run() throws java.io.InvalidObjectException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.io.ObjectInputStream.ValidationList.1.run():java.lang.Void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.ValidationList.1.run():java.lang.Void");
            }
        }

        private static class Callback {
            final AccessControlContext acc;
            Callback next;
            final ObjectInputValidation obj;
            final int priority;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectInputStream.ValidationList.Callback.<init>(java.io.ObjectInputValidation, int, java.io.ObjectInputStream$ValidationList$Callback, java.security.AccessControlContext):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            Callback(java.io.ObjectInputValidation r1, int r2, java.io.ObjectInputStream.ValidationList.Callback r3, java.security.AccessControlContext r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.io.ObjectInputStream.ValidationList.Callback.<init>(java.io.ObjectInputValidation, int, java.io.ObjectInputStream$ValidationList$Callback, java.security.AccessControlContext):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.ValidationList.Callback.<init>(java.io.ObjectInputValidation, int, java.io.ObjectInputStream$ValidationList$Callback, java.security.AccessControlContext):void");
            }
        }

        ValidationList() {
        }

        void register(ObjectInputValidation obj, int priority) throws InvalidObjectException {
            if (obj == null) {
                throw new InvalidObjectException("null callback");
            }
            Callback prev = null;
            Callback cur = this.list;
            while (cur != null && priority < cur.priority) {
                prev = cur;
                cur = cur.next;
            }
            AccessControlContext acc = AccessController.getContext();
            if (prev != null) {
                prev.next = new Callback(obj, priority, cur, acc);
            } else {
                this.list = new Callback(obj, priority, this.list, acc);
            }
        }

        void doCallbacks() throws InvalidObjectException {
            while (this.list != null) {
                try {
                    AccessController.doPrivileged(new AnonymousClass1(this), this.list.acc);
                    this.list = this.list.next;
                } catch (PrivilegedActionException ex) {
                    this.list = null;
                    throw ((InvalidObjectException) ex.getException());
                }
            }
        }

        public void clear() {
            this.list = null;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.io.ObjectInputStream.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.io.ObjectInputStream.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.<clinit>():void");
    }

    private static native void bytesToDoubles(byte[] bArr, int i, double[] dArr, int i2, int i3);

    private static native void bytesToFloats(byte[] bArr, int i, float[] fArr, int i2, int i3);

    public ObjectInputStream(InputStream in) throws IOException {
        this.passHandle = -1;
        this.defaultDataEnd = false;
        verifySubclass();
        this.bin = new BlockDataInputStream(this, in);
        this.handles = new HandleTable(10);
        this.vlist = new ValidationList();
        this.enableOverride = false;
        readStreamHeader();
        this.bin.setBlockDataMode(true);
    }

    protected ObjectInputStream() throws IOException, SecurityException {
        this.passHandle = -1;
        this.defaultDataEnd = false;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        this.bin = null;
        this.handles = null;
        this.vlist = null;
        this.enableOverride = true;
    }

    public final Object readObject() throws IOException, ClassNotFoundException {
        if (this.enableOverride) {
            return readObjectOverride();
        }
        int outerHandle = this.passHandle;
        try {
            Object obj = readObject0(false);
            this.handles.markDependency(outerHandle, this.passHandle);
            ClassNotFoundException ex = this.handles.lookupException(this.passHandle);
            if (ex != null) {
                throw ex;
            }
            if (this.depth == 0) {
                this.vlist.doCallbacks();
            }
            this.passHandle = outerHandle;
            if (this.closed && this.depth == 0) {
                clear();
            }
            return obj;
        } catch (Throwable th) {
            this.passHandle = outerHandle;
            if (this.closed && this.depth == 0) {
                clear();
            }
        }
    }

    protected Object readObjectOverride() throws IOException, ClassNotFoundException {
        return null;
    }

    public Object readUnshared() throws IOException, ClassNotFoundException {
        int outerHandle = this.passHandle;
        try {
            Object obj = readObject0(true);
            this.handles.markDependency(outerHandle, this.passHandle);
            ClassNotFoundException ex = this.handles.lookupException(this.passHandle);
            if (ex != null) {
                throw ex;
            }
            if (this.depth == 0) {
                this.vlist.doCallbacks();
            }
            this.passHandle = outerHandle;
            if (this.closed && this.depth == 0) {
                clear();
            }
            return obj;
        } catch (Throwable th) {
            this.passHandle = outerHandle;
            if (this.closed && this.depth == 0) {
                clear();
            }
        }
    }

    public void defaultReadObject() throws IOException, ClassNotFoundException {
        if (this.curContext == null) {
            throw new NotActiveException("not in call to readObject");
        }
        Object curObj = this.curContext.getObj();
        ObjectStreamClass curDesc = this.curContext.getDesc();
        this.bin.setBlockDataMode(false);
        defaultReadFields(curObj, curDesc);
        this.bin.setBlockDataMode(true);
        if (!curDesc.hasWriteObjectData()) {
            this.defaultDataEnd = true;
        }
        ClassNotFoundException ex = this.handles.lookupException(this.passHandle);
        if (ex != null) {
            throw ex;
        }
    }

    public GetField readFields() throws IOException, ClassNotFoundException {
        if (this.curContext == null) {
            throw new NotActiveException("not in call to readObject");
        }
        Object curObj = this.curContext.getObj();
        ObjectStreamClass curDesc = this.curContext.getDesc();
        this.bin.setBlockDataMode(false);
        GetFieldImpl getField = new GetFieldImpl(this, curDesc);
        getField.readFields();
        this.bin.setBlockDataMode(true);
        if (!curDesc.hasWriteObjectData()) {
            this.defaultDataEnd = true;
        }
        return getField;
    }

    public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException, InvalidObjectException {
        if (this.depth == 0) {
            throw new NotActiveException("stream inactive");
        }
        this.vlist.register(obj, prio);
    }

    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String name = desc.getName();
        try {
            return Class.forName(name, false, latestUserDefinedLoader());
        } catch (ClassNotFoundException ex) {
            Class<?> cl = (Class) primClasses.get(name);
            if (cl != null) {
                return cl;
            }
            throw ex;
        }
    }

    protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        ClassLoader latestLoader = latestUserDefinedLoader();
        ClassLoader nonPublicLoader = null;
        boolean hasNonPublicInterface = false;
        Class[] classObjs = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class cl = Class.forName(interfaces[i], false, latestLoader);
            if ((cl.getModifiers() & 1) == 0) {
                if (!hasNonPublicInterface) {
                    nonPublicLoader = cl.getClassLoader();
                    hasNonPublicInterface = true;
                } else if (nonPublicLoader != cl.getClassLoader()) {
                    throw new IllegalAccessError("conflicting non-public interface class loaders");
                }
            }
            classObjs[i] = cl;
        }
        if (!hasNonPublicInterface) {
            nonPublicLoader = latestLoader;
        }
        try {
            return Proxy.getProxyClass(nonPublicLoader, classObjs);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    protected Object resolveObject(Object obj) throws IOException {
        return obj;
    }

    protected boolean enableResolveObject(boolean enable) throws SecurityException {
        if (enable == this.enableResolve) {
            return enable;
        }
        if (enable) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        this.enableResolve = enable;
        return !this.enableResolve;
    }

    protected void readStreamHeader() throws IOException, StreamCorruptedException {
        short s0 = this.bin.readShort();
        short s1 = this.bin.readShort();
        if (s0 != ObjectStreamConstants.STREAM_MAGIC || s1 != (short) 5) {
            Object[] objArr = new Object[2];
            objArr[0] = Short.valueOf(s0);
            objArr[1] = Short.valueOf(s1);
            throw new StreamCorruptedException(String.format("invalid stream header: %04X%04X", objArr));
        }
    }

    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass desc = new ObjectStreamClass();
        desc.readNonProxy(this);
        return desc;
    }

    public int read() throws IOException {
        return this.bin.read();
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        if (buf == null) {
            throw new NullPointerException();
        }
        int endoff = off + len;
        if (off >= 0 && len >= 0 && endoff <= buf.length && endoff >= 0) {
            return this.bin.read(buf, off, len, false);
        }
        throw new IndexOutOfBoundsException();
    }

    public int available() throws IOException {
        return this.bin.available();
    }

    public void close() throws IOException {
        this.closed = true;
        if (this.depth == 0) {
            clear();
        }
        this.bin.close();
    }

    public boolean readBoolean() throws IOException {
        return this.bin.readBoolean();
    }

    public byte readByte() throws IOException {
        return this.bin.readByte();
    }

    public int readUnsignedByte() throws IOException {
        return this.bin.readUnsignedByte();
    }

    public char readChar() throws IOException {
        return this.bin.readChar();
    }

    public short readShort() throws IOException {
        return this.bin.readShort();
    }

    public int readUnsignedShort() throws IOException {
        return this.bin.readUnsignedShort();
    }

    public int readInt() throws IOException {
        return this.bin.readInt();
    }

    public long readLong() throws IOException {
        return this.bin.readLong();
    }

    public float readFloat() throws IOException {
        return this.bin.readFloat();
    }

    public double readDouble() throws IOException {
        return this.bin.readDouble();
    }

    public void readFully(byte[] buf) throws IOException {
        this.bin.readFully(buf, 0, buf.length, false);
    }

    public void readFully(byte[] buf, int off, int len) throws IOException {
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.bin.readFully(buf, off, len, false);
    }

    public int skipBytes(int len) throws IOException {
        return this.bin.skipBytes(len);
    }

    @Deprecated
    public String readLine() throws IOException {
        return this.bin.readLine();
    }

    public String readUTF() throws IOException {
        return this.bin.readUTF();
    }

    private void verifySubclass() {
        Class cl = getClass();
        if (cl != ObjectInputStream.class) {
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
    private static boolean auditSubclass(java.lang.Class<?> r2) {
        /*
        r1 = new java.io.ObjectInputStream$1;
        r1.<init>(r2);
        r0 = java.security.AccessController.doPrivileged(r1);
        r0 = (java.lang.Boolean) r0;
        r1 = r0.booleanValue();
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.auditSubclass(java.lang.Class):boolean");
    }

    private void clear() {
        this.handles.clear();
        this.vlist.clear();
    }

    private Object readObject0(boolean unshared) throws IOException {
        byte tc;
        boolean oldMode = this.bin.getBlockDataMode();
        if (oldMode) {
            int remain = this.bin.currentBlockRemaining();
            if (remain > 0) {
                throw new OptionalDataException(remain);
            } else if (this.defaultDataEnd) {
                throw new OptionalDataException(true);
            } else {
                this.bin.setBlockDataMode(false);
            }
        }
        while (true) {
            tc = this.bin.peekByte();
            if (tc != ObjectStreamConstants.TC_RESET) {
                break;
            }
            this.bin.readByte();
            handleReset();
        }
        this.depth++;
        Object readNull;
        switch (tc) {
            case (byte) 112:
                readNull = readNull();
                this.depth--;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 113:
                readNull = readHandle(unshared);
                this.depth--;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 114:
            case (byte) 125:
                ObjectStreamClass readClassDesc = readClassDesc(unshared);
                this.depth--;
                this.bin.setBlockDataMode(oldMode);
                return readClassDesc;
            case (byte) 115:
                readNull = checkResolve(readOrdinaryObject(unshared));
                this.depth--;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 116:
            case (byte) 124:
                readNull = checkResolve(readString(unshared));
                this.depth--;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 117:
                readNull = checkResolve(readArray(unshared));
                this.depth--;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 118:
                Class readClass = readClass(unshared);
                this.depth--;
                this.bin.setBlockDataMode(oldMode);
                return readClass;
            case (byte) 119:
            case (byte) 122:
                if (oldMode) {
                    this.bin.setBlockDataMode(true);
                    this.bin.peek();
                    throw new OptionalDataException(this.bin.currentBlockRemaining());
                }
                throw new StreamCorruptedException("unexpected block data");
            case (byte) 120:
                if (oldMode) {
                    throw new OptionalDataException(true);
                }
                throw new StreamCorruptedException("unexpected end of block data");
            case (byte) 123:
                throw new WriteAbortedException("writing aborted", readFatalException());
            case (byte) 126:
                readNull = checkResolve(readEnum(unshared));
                this.depth--;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            default:
                try {
                    Object[] objArr = new Object[1];
                    objArr[0] = Byte.valueOf(tc);
                    throw new StreamCorruptedException(String.format("invalid type code: %02X", objArr));
                } catch (Throwable th) {
                    this.depth--;
                    this.bin.setBlockDataMode(oldMode);
                }
        }
        this.depth--;
        this.bin.setBlockDataMode(oldMode);
    }

    private Object checkResolve(Object obj) throws IOException {
        if (!this.enableResolve || this.handles.lookupException(this.passHandle) != null) {
            return obj;
        }
        Object rep = resolveObject(obj);
        if (rep != obj) {
            this.handles.setObject(this.passHandle, rep);
        }
        return rep;
    }

    String readTypeString() throws IOException {
        String str;
        int oldHandle = this.passHandle;
        try {
            byte tc = this.bin.peekByte();
            switch (tc) {
                case (byte) 112:
                    str = (String) readNull();
                    break;
                case (byte) 113:
                    str = (String) readHandle(false);
                    this.passHandle = oldHandle;
                    return str;
                case (byte) 116:
                case (byte) 124:
                    str = readString(false);
                    this.passHandle = oldHandle;
                    return str;
                default:
                    Object[] objArr = new Object[1];
                    objArr[0] = Byte.valueOf(tc);
                    throw new StreamCorruptedException(String.format("invalid type code: %02X", objArr));
            }
        } finally {
            this.passHandle = oldHandle;
        }
        this.passHandle = oldHandle;
        return str;
    }

    private Object readNull() throws IOException {
        if (this.bin.readByte() != (byte) 112) {
            throw new InternalError();
        }
        this.passHandle = -1;
        return null;
    }

    private Object readHandle(boolean unshared) throws IOException {
        if (this.bin.readByte() != ObjectStreamConstants.TC_REFERENCE) {
            throw new InternalError();
        }
        this.passHandle = this.bin.readInt() - ObjectStreamConstants.baseWireHandle;
        if (this.passHandle < 0 || this.passHandle >= this.handles.size()) {
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(this.passHandle + ObjectStreamConstants.baseWireHandle);
            throw new StreamCorruptedException(String.format("invalid handle value: %08X", objArr));
        } else if (unshared) {
            throw new InvalidObjectException("cannot read back reference as unshared");
        } else {
            Object obj = this.handles.lookupObject(this.passHandle);
            if (obj != unsharedMarker) {
                return obj;
            }
            throw new InvalidObjectException("cannot read back reference to unshared object");
        }
    }

    private Class readClass(boolean unshared) throws IOException {
        if (this.bin.readByte() != ObjectStreamConstants.TC_CLASS) {
            throw new InternalError();
        }
        Object obj;
        ObjectStreamClass desc = readClassDesc(false);
        Class cl = desc.forClass();
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            Class obj2 = cl;
        }
        this.passHandle = handleTable.assign(obj2);
        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            this.handles.markException(this.passHandle, resolveEx);
        }
        this.handles.finish(this.passHandle);
        return cl;
    }

    private ObjectStreamClass readClassDesc(boolean unshared) throws IOException {
        byte tc = this.bin.peekByte();
        switch (tc) {
            case (byte) 112:
                return (ObjectStreamClass) readNull();
            case (byte) 113:
                return (ObjectStreamClass) readHandle(unshared);
            case (byte) 114:
                return readNonProxyDesc(unshared);
            case (byte) 125:
                return readProxyDesc(unshared);
            default:
                Object[] objArr = new Object[1];
                objArr[0] = Byte.valueOf(tc);
                throw new StreamCorruptedException(String.format("invalid type code: %02X", objArr));
        }
    }

    private boolean isCustomSubclass() {
        return getClass().getClassLoader() != ObjectInputStream.class.getClassLoader();
    }

    private ObjectStreamClass readProxyDesc(boolean unshared) throws IOException {
        if (this.bin.readByte() != ObjectStreamConstants.TC_PROXYCLASSDESC) {
            throw new InternalError();
        }
        Object obj;
        ObjectStreamClass desc = new ObjectStreamClass();
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            ObjectStreamClass obj2 = desc;
        }
        int descHandle = handleTable.assign(obj2);
        this.passHandle = -1;
        int numIfaces = this.bin.readInt();
        String[] ifaces = new String[numIfaces];
        for (int i = 0; i < numIfaces; i++) {
            ifaces[i] = this.bin.readUTF();
        }
        Class cl = null;
        ClassNotFoundException resolveEx = null;
        this.bin.setBlockDataMode(true);
        try {
            cl = resolveProxyClass(ifaces);
            if (cl == null) {
                resolveEx = new ClassNotFoundException("null class");
            } else if (Proxy.isProxyClass(cl)) {
                ReflectUtil.checkProxyPackageAccess(getClass().getClassLoader(), cl.getInterfaces());
            } else {
                throw new InvalidClassException("Not a proxy");
            }
        } catch (ClassNotFoundException ex) {
            resolveEx = ex;
        }
        skipCustomData();
        desc.initProxy(cl, resolveEx, readClassDesc(false));
        this.handles.finish(descHandle);
        this.passHandle = descHandle;
        return desc;
    }

    private ObjectStreamClass readNonProxyDesc(boolean unshared) throws IOException {
        if (this.bin.readByte() != ObjectStreamConstants.TC_CLASSDESC) {
            throw new InternalError();
        }
        Object obj;
        ObjectStreamClass desc = new ObjectStreamClass();
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            ObjectStreamClass obj2 = desc;
        }
        int descHandle = handleTable.assign(obj2);
        this.passHandle = -1;
        try {
            ObjectStreamClass readDesc = readClassDescriptor();
            Class cl = null;
            ClassNotFoundException resolveEx = null;
            this.bin.setBlockDataMode(true);
            boolean checksRequired = isCustomSubclass();
            try {
                cl = resolveClass(readDesc);
                if (cl == null) {
                    resolveEx = new ClassNotFoundException("null class");
                } else if (checksRequired) {
                    ReflectUtil.checkPackageAccess(cl);
                }
            } catch (ClassNotFoundException ex) {
                resolveEx = ex;
            }
            skipCustomData();
            desc.initNonProxy(readDesc, cl, resolveEx, readClassDesc(false));
            this.handles.finish(descHandle);
            this.passHandle = descHandle;
            return desc;
        } catch (ClassNotFoundException ex2) {
            throw ((IOException) new InvalidClassException("failed to read class descriptor").initCause(ex2));
        }
    }

    private String readString(boolean unshared) throws IOException {
        String str;
        Object obj;
        byte tc = this.bin.readByte();
        switch (tc) {
            case (byte) 116:
                str = this.bin.readUTF();
                break;
            case (byte) 124:
                str = this.bin.readLongUTF();
                break;
            default:
                Object[] objArr = new Object[1];
                objArr[0] = Byte.valueOf(tc);
                throw new StreamCorruptedException(String.format("invalid type code: %02X", objArr));
        }
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            String obj2 = str;
        }
        this.passHandle = handleTable.assign(obj2);
        this.handles.finish(this.passHandle);
        return str;
    }

    private Object readArray(boolean unshared) throws IOException {
        if (this.bin.readByte() != ObjectStreamConstants.TC_ARRAY) {
            throw new InternalError();
        }
        Object obj;
        ObjectStreamClass desc = readClassDesc(false);
        int len = this.bin.readInt();
        Object array = null;
        Class ccl = null;
        Class cl = desc.forClass();
        if (cl != null) {
            ccl = cl.getComponentType();
            array = Array.newInstance(ccl, len);
        }
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            obj = array;
        }
        int arrayHandle = handleTable.assign(obj);
        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            this.handles.markException(arrayHandle, resolveEx);
        }
        int i;
        if (ccl == null) {
            for (i = 0; i < len; i++) {
                readObject0(false);
            }
        } else if (!ccl.isPrimitive()) {
            Object[] oa = (Object[]) array;
            for (i = 0; i < len; i++) {
                oa[i] = readObject0(false);
                this.handles.markDependency(arrayHandle, this.passHandle);
            }
        } else if (ccl == Integer.TYPE) {
            this.bin.readInts((int[]) array, 0, len);
        } else if (ccl == Byte.TYPE) {
            this.bin.readFully((byte[]) array, 0, len, true);
        } else if (ccl == Long.TYPE) {
            this.bin.readLongs((long[]) array, 0, len);
        } else if (ccl == Float.TYPE) {
            this.bin.readFloats((float[]) array, 0, len);
        } else if (ccl == Double.TYPE) {
            this.bin.readDoubles((double[]) array, 0, len);
        } else if (ccl == Short.TYPE) {
            this.bin.readShorts((short[]) array, 0, len);
        } else if (ccl == Character.TYPE) {
            this.bin.readChars((char[]) array, 0, len);
        } else if (ccl == Boolean.TYPE) {
            this.bin.readBooleans((boolean[]) array, 0, len);
        } else {
            throw new InternalError();
        }
        this.handles.finish(arrayHandle);
        this.passHandle = arrayHandle;
        return array;
    }

    private Enum readEnum(boolean unshared) throws IOException {
        Object obj = null;
        if (this.bin.readByte() != (byte) 126) {
            throw new InternalError();
        }
        Object desc = readClassDesc(false);
        if (desc.isEnum()) {
            HandleTable handleTable = this.handles;
            if (unshared) {
                obj = unsharedMarker;
            }
            int enumHandle = handleTable.assign(obj);
            ClassNotFoundException resolveEx = desc.getResolveException();
            if (resolveEx != null) {
                this.handles.markException(enumHandle, resolveEx);
            }
            String name = readString(false);
            Enum en = null;
            Object cl = desc.forClass();
            if (cl != null) {
                try {
                    en = Enum.valueOf(cl, name);
                    if (!unshared) {
                        this.handles.setObject(enumHandle, en);
                    }
                } catch (IllegalArgumentException ex) {
                    throw ((IOException) new InvalidObjectException("enum constant " + name + " does not exist in " + cl).initCause(ex));
                }
            }
            this.handles.finish(enumHandle);
            this.passHandle = enumHandle;
            return en;
        }
        throw new InvalidClassException("non-enum class: " + desc);
    }

    private Object readOrdinaryObject(boolean unshared) throws IOException {
        if (this.bin.readByte() != ObjectStreamConstants.TC_OBJECT) {
            throw new InternalError();
        }
        ObjectStreamClass desc = readClassDesc(false);
        desc.checkDeserialize();
        Class<?> cl = desc.forClass();
        if (cl == String.class || cl == Class.class || cl == ObjectStreamClass.class) {
            throw new InvalidClassException("invalid class descriptor");
        }
        try {
            Object obj;
            Object obj2 = desc.isInstantiable() ? desc.newInstance() : null;
            HandleTable handleTable = this.handles;
            if (unshared) {
                obj = unsharedMarker;
            } else {
                obj = obj2;
            }
            this.passHandle = handleTable.assign(obj);
            ClassNotFoundException resolveEx = desc.getResolveException();
            if (resolveEx != null) {
                this.handles.markException(this.passHandle, resolveEx);
            }
            if (desc.isExternalizable()) {
                readExternalData((Externalizable) obj2, desc);
            } else {
                readSerialData(obj2, desc);
            }
            this.handles.finish(this.passHandle);
            if (obj2 == null || this.handles.lookupException(this.passHandle) != null || !desc.hasReadResolveMethod()) {
                return obj2;
            }
            Object rep = desc.invokeReadResolve(obj2);
            if (unshared && rep.getClass().isArray()) {
                rep = cloneArray(rep);
            }
            if (rep == obj2) {
                return obj2;
            }
            obj2 = rep;
            this.handles.setObject(this.passHandle, rep);
            return obj2;
        } catch (Exception ex) {
            throw ((IOException) new InvalidClassException(desc.forClass().getName(), "unable to create instance").initCause(ex));
        }
    }

    private void readExternalData(Externalizable obj, ObjectStreamClass desc) throws IOException {
        boolean blocked;
        SerialCallbackContext oldContext = this.curContext;
        this.curContext = null;
        try {
            blocked = desc.hasBlockExternalData();
            if (blocked) {
                this.bin.setBlockDataMode(true);
            }
            if (obj != null) {
                obj.readExternal(this);
            }
        } catch (ClassNotFoundException ex) {
            this.handles.markException(this.passHandle, ex);
        } catch (Throwable th) {
            this.curContext = oldContext;
        }
        if (blocked) {
            skipCustomData();
        }
        this.curContext = oldContext;
    }

    private void readSerialData(Object obj, ObjectStreamClass desc) throws IOException {
        ClassDataSlot[] slots = desc.getClassDataLayout();
        for (int i = 0; i < slots.length; i++) {
            ObjectStreamClass slotDesc = slots[i].desc;
            if (slots[i].hasData) {
                if (obj != null && slotDesc.hasReadObjectMethod() && this.handles.lookupException(this.passHandle) == null) {
                    SerialCallbackContext oldContext = this.curContext;
                    try {
                        this.curContext = new SerialCallbackContext(obj, slotDesc);
                        this.bin.setBlockDataMode(true);
                        slotDesc.invokeReadObject(obj, this);
                        this.curContext.setUsed();
                    } catch (ClassNotFoundException ex) {
                        this.handles.markException(this.passHandle, ex);
                        this.curContext.setUsed();
                    } catch (Throwable th) {
                        this.curContext.setUsed();
                        this.curContext = oldContext;
                    }
                    this.curContext = oldContext;
                    this.defaultDataEnd = false;
                } else {
                    defaultReadFields(obj, slotDesc);
                }
                if (slotDesc.hasWriteObjectData()) {
                    skipCustomData();
                } else {
                    this.bin.setBlockDataMode(false);
                }
            } else if (obj != null && slotDesc.hasReadObjectNoDataMethod() && this.handles.lookupException(this.passHandle) == null) {
                slotDesc.invokeReadObjectNoData(obj);
            }
        }
    }

    private void skipCustomData() throws IOException {
        int oldHandle = this.passHandle;
        while (true) {
            if (this.bin.getBlockDataMode()) {
                this.bin.skipBlockData();
                this.bin.setBlockDataMode(false);
            }
            switch (this.bin.peekByte()) {
                case (byte) 119:
                case (byte) 122:
                    this.bin.setBlockDataMode(true);
                    break;
                case (byte) 120:
                    this.bin.readByte();
                    this.passHandle = oldHandle;
                    return;
                default:
                    readObject0(false);
                    break;
            }
        }
    }

    private void defaultReadFields(Object obj, ObjectStreamClass desc) throws IOException {
        Class cl = desc.forClass();
        if (cl == null || obj == null || cl.isInstance(obj)) {
            int primDataSize = desc.getPrimDataSize();
            if (this.primVals == null || this.primVals.length < primDataSize) {
                this.primVals = new byte[primDataSize];
            }
            this.bin.readFully(this.primVals, 0, primDataSize, false);
            if (obj != null) {
                desc.setPrimFieldValues(obj, this.primVals);
            }
            int objHandle = this.passHandle;
            ObjectStreamField[] fields = desc.getFields(false);
            Object[] objVals = new Object[desc.getNumObjFields()];
            int numPrimFields = fields.length - objVals.length;
            for (int i = 0; i < objVals.length; i++) {
                ObjectStreamField f = fields[numPrimFields + i];
                objVals[i] = readObject0(f.isUnshared());
                if (f.getField() != null) {
                    this.handles.markDependency(objHandle, this.passHandle);
                }
            }
            if (obj != null) {
                desc.setObjFieldValues(obj, objVals);
            }
            this.passHandle = objHandle;
            return;
        }
        throw new ClassCastException();
    }

    private IOException readFatalException() throws IOException {
        if (this.bin.readByte() != ObjectStreamConstants.TC_EXCEPTION) {
            throw new InternalError();
        }
        clear();
        IOException e = (IOException) readObject0(false);
        clear();
        return e;
    }

    private void handleReset() throws StreamCorruptedException {
        if (this.depth > 0) {
            throw new StreamCorruptedException("unexpected reset; recursion depth: " + this.depth);
        }
        clear();
    }

    private static ClassLoader latestUserDefinedLoader() {
        return VMStack.getClosestUserClassLoader();
    }

    private static Object cloneArray(Object array) {
        if (array instanceof Object[]) {
            return ((Object[]) array).clone();
        }
        if (array instanceof boolean[]) {
            return ((boolean[]) array).clone();
        }
        if (array instanceof byte[]) {
            return ((byte[]) array).clone();
        }
        if (array instanceof char[]) {
            return ((char[]) array).clone();
        }
        if (array instanceof double[]) {
            return ((double[]) array).clone();
        }
        if (array instanceof float[]) {
            return ((float[]) array).clone();
        }
        if (array instanceof int[]) {
            return ((int[]) array).clone();
        }
        if (array instanceof long[]) {
            return ((long[]) array).clone();
        }
        if (array instanceof short[]) {
            return ((short[]) array).clone();
        }
        throw new AssertionError();
    }
}
