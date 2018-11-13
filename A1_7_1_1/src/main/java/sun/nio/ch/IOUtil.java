package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

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
class IOUtil {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f141-assertionsDisabled = false;
    static final int IOV_MAX = 0;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.IOUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.IOUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.IOUtil.<clinit>():void");
    }

    static native void configureBlocking(FileDescriptor fileDescriptor, boolean z) throws IOException;

    static native boolean drain(int i) throws IOException;

    static native int fdLimit();

    static native int fdVal(FileDescriptor fileDescriptor);

    static native int iovMax();

    static native long makePipe(boolean z);

    static native boolean randomBytes(byte[] bArr);

    static native void setfdVal(FileDescriptor fileDescriptor, int i);

    private IOUtil() {
    }

    static int write(FileDescriptor fd, ByteBuffer src, long position, NativeDispatcher nd) throws IOException {
        int rem = 0;
        if (src instanceof DirectBuffer) {
            return writeFromNativeBuffer(fd, src, position, nd);
        }
        int pos = src.position();
        int lim = src.limit();
        if (!f141-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (pos <= lim) {
            rem = lim - pos;
        }
        ByteBuffer bb = Util.getTemporaryDirectBuffer(rem);
        try {
            bb.put(src);
            bb.flip();
            src.position(pos);
            int n = writeFromNativeBuffer(fd, bb, position, nd);
            if (n > 0) {
                src.position(pos + n);
            }
            Util.offerFirstTemporaryDirectBuffer(bb);
            return n;
        } catch (Throwable th) {
            Util.offerFirstTemporaryDirectBuffer(bb);
        }
    }

    private static int writeFromNativeBuffer(FileDescriptor fd, ByteBuffer bb, long position, NativeDispatcher nd) throws IOException {
        int rem;
        int pos = bb.position();
        int lim = bb.limit();
        if (!f141-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (pos <= lim) {
            rem = lim - pos;
        } else {
            rem = 0;
        }
        if (rem == 0) {
            return 0;
        }
        int written;
        if (position != -1) {
            written = nd.pwrite(fd, ((long) pos) + ((DirectBuffer) bb).address(), rem, position);
        } else {
            written = nd.write(fd, ((DirectBuffer) bb).address() + ((long) pos), rem);
        }
        if (written > 0) {
            bb.position(pos + written);
        }
        return written;
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, NativeDispatcher nd) throws IOException {
        return write(fd, bufs, 0, bufs.length, nd);
    }

    static long write(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        ByteBuffer buf;
        int pos;
        int rem;
        ByteBuffer shadow;
        int j;
        IOVecWrapper vec = IOVecWrapper.get(length);
        int iov_len = 0;
        int count = offset + length;
        int i = offset;
        while (i < count) {
            try {
                if (iov_len >= IOV_MAX) {
                    break;
                }
                buf = bufs[i];
                pos = buf.position();
                int lim = buf.limit();
                if (!f141-assertionsDisabled) {
                    Object obj;
                    if (pos <= lim) {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                rem = pos <= lim ? lim - pos : 0;
                if (rem > 0) {
                    vec.setBuffer(iov_len, buf, pos, rem);
                    if (!(buf instanceof DirectBuffer)) {
                        shadow = Util.getTemporaryDirectBuffer(rem);
                        shadow.put(buf);
                        shadow.flip();
                        vec.setShadow(iov_len, shadow);
                        buf.position(pos);
                        buf = shadow;
                        pos = shadow.position();
                    }
                    vec.putBase(iov_len, ((DirectBuffer) buf).address() + ((long) pos));
                    vec.putLen(iov_len, (long) rem);
                    iov_len++;
                }
                i++;
            } catch (Throwable th) {
                if (null == null) {
                    for (j = 0; j < iov_len; j++) {
                        shadow = vec.getShadow(j);
                        if (shadow != null) {
                            Util.offerLastTemporaryDirectBuffer(shadow);
                        }
                        vec.clearRefs(j);
                    }
                }
            }
        }
        if (iov_len == 0) {
            if (null == null) {
                for (j = 0; j < iov_len; j++) {
                    shadow = vec.getShadow(j);
                    if (shadow != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow);
                    }
                    vec.clearRefs(j);
                }
            }
            return 0;
        }
        long bytesWritten = nd.writev(fd, vec.address, iov_len);
        long left = bytesWritten;
        for (j = 0; j < iov_len; j++) {
            if (left > 0) {
                buf = vec.getBuffer(j);
                pos = vec.getPosition(j);
                rem = vec.getRemaining(j);
                int n = left > ((long) rem) ? rem : (int) left;
                buf.position(pos + n);
                left -= (long) n;
            }
            shadow = vec.getShadow(j);
            if (shadow != null) {
                Util.offerLastTemporaryDirectBuffer(shadow);
            }
            vec.clearRefs(j);
        }
        if (!true) {
            for (j = 0; j < iov_len; j++) {
                shadow = vec.getShadow(j);
                if (shadow != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow);
                }
                vec.clearRefs(j);
            }
        }
        return bytesWritten;
    }

    static int read(FileDescriptor fd, ByteBuffer dst, long position, NativeDispatcher nd) throws IOException {
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        } else if (dst instanceof DirectBuffer) {
            return readIntoNativeBuffer(fd, dst, position, nd);
        } else {
            ByteBuffer bb = Util.getTemporaryDirectBuffer(dst.remaining());
            try {
                int n = readIntoNativeBuffer(fd, bb, position, nd);
                bb.flip();
                if (n > 0) {
                    dst.put(bb);
                }
                Util.offerFirstTemporaryDirectBuffer(bb);
                return n;
            } catch (Throwable th) {
                Util.offerFirstTemporaryDirectBuffer(bb);
            }
        }
    }

    private static int readIntoNativeBuffer(FileDescriptor fd, ByteBuffer bb, long position, NativeDispatcher nd) throws IOException {
        int rem;
        int pos = bb.position();
        int lim = bb.limit();
        if (!f141-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (pos <= lim) {
            rem = lim - pos;
        } else {
            rem = 0;
        }
        if (rem == 0) {
            return 0;
        }
        int n;
        if (position != -1) {
            n = nd.pread(fd, ((long) pos) + ((DirectBuffer) bb).address(), rem, position);
        } else {
            n = nd.read(fd, ((DirectBuffer) bb).address() + ((long) pos), rem);
        }
        if (n > 0) {
            bb.position(pos + n);
        }
        return n;
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, NativeDispatcher nd) throws IOException {
        return read(fd, bufs, 0, bufs.length, nd);
    }

    static long read(FileDescriptor fd, ByteBuffer[] bufs, int offset, int length, NativeDispatcher nd) throws IOException {
        ByteBuffer buf;
        int rem;
        ByteBuffer shadow;
        int j;
        IOVecWrapper vec = IOVecWrapper.get(length);
        int iov_len = 0;
        int count = offset + length;
        int i = offset;
        while (i < count) {
            try {
                if (iov_len >= IOV_MAX) {
                    break;
                }
                buf = bufs[i];
                if (buf.isReadOnly()) {
                    throw new IllegalArgumentException("Read-only buffer");
                }
                int pos = buf.position();
                int lim = buf.limit();
                if (!f141-assertionsDisabled) {
                    if ((pos <= lim ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                rem = pos <= lim ? lim - pos : 0;
                if (rem > 0) {
                    vec.setBuffer(iov_len, buf, pos, rem);
                    if (!(buf instanceof DirectBuffer)) {
                        shadow = Util.getTemporaryDirectBuffer(rem);
                        vec.setShadow(iov_len, shadow);
                        buf = shadow;
                        pos = shadow.position();
                    }
                    vec.putBase(iov_len, ((DirectBuffer) buf).address() + ((long) pos));
                    vec.putLen(iov_len, (long) rem);
                    iov_len++;
                }
                i++;
            } catch (Throwable th) {
                if (null == null) {
                    for (j = 0; j < iov_len; j++) {
                        shadow = vec.getShadow(j);
                        if (shadow != null) {
                            Util.offerLastTemporaryDirectBuffer(shadow);
                        }
                        vec.clearRefs(j);
                    }
                }
            }
        }
        if (iov_len == 0) {
            if (null == null) {
                for (j = 0; j < iov_len; j++) {
                    shadow = vec.getShadow(j);
                    if (shadow != null) {
                        Util.offerLastTemporaryDirectBuffer(shadow);
                    }
                    vec.clearRefs(j);
                }
            }
            return 0;
        }
        long bytesRead = nd.readv(fd, vec.address, iov_len);
        long left = bytesRead;
        for (j = 0; j < iov_len; j++) {
            shadow = vec.getShadow(j);
            if (left > 0) {
                buf = vec.getBuffer(j);
                rem = vec.getRemaining(j);
                int n = left > ((long) rem) ? rem : (int) left;
                if (shadow == null) {
                    buf.position(vec.getPosition(j) + n);
                } else {
                    shadow.limit(shadow.position() + n);
                    buf.put(shadow);
                }
                left -= (long) n;
            }
            if (shadow != null) {
                Util.offerLastTemporaryDirectBuffer(shadow);
            }
            vec.clearRefs(j);
        }
        if (!true) {
            for (j = 0; j < iov_len; j++) {
                shadow = vec.getShadow(j);
                if (shadow != null) {
                    Util.offerLastTemporaryDirectBuffer(shadow);
                }
                vec.clearRefs(j);
            }
        }
        return bytesRead;
    }

    static FileDescriptor newFD(int i) {
        FileDescriptor fd = new FileDescriptor();
        setfdVal(fd, i);
        return fd;
    }
}
