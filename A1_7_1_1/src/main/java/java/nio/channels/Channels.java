package java.nio.channels;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import sun.nio.ch.ChannelInputStream;
import sun.nio.cs.StreamDecoder;
import sun.nio.cs.StreamEncoder;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class Channels {

    private static class ReadableByteChannelImpl extends AbstractInterruptibleChannel implements ReadableByteChannel {
        private static final int TRANSFER_SIZE = 8192;
        private byte[] buf = new byte[0];
        InputStream in;
        private boolean open = true;
        private Object readLock = new Object();

        ReadableByteChannelImpl(InputStream in) {
            this.in = in;
        }

        /* JADX WARNING: Missing block: B:36:0x005a, code:
            return r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int read(ByteBuffer dst) throws IOException {
            boolean z = true;
            int len = dst.remaining();
            int totalRead = 0;
            int bytesRead = 0;
            synchronized (this.readLock) {
                while (totalRead < len) {
                    int bytesToRead = Math.min(len - totalRead, 8192);
                    if (this.buf.length < bytesToRead) {
                        this.buf = new byte[bytesToRead];
                    }
                    if (totalRead > 0 && this.in.available() <= 0) {
                        break;
                    }
                    try {
                        boolean z2;
                        begin();
                        bytesRead = this.in.read(this.buf, 0, bytesToRead);
                        if (bytesRead > 0) {
                            z2 = true;
                        } else {
                            z2 = false;
                        }
                        end(z2);
                        if (bytesRead < 0) {
                            break;
                        }
                        totalRead += bytesRead;
                        dst.put(this.buf, 0, bytesRead);
                    } catch (Throwable th) {
                        if (bytesRead <= 0) {
                            z = false;
                        }
                        end(z);
                    }
                }
                if (bytesRead >= 0 || totalRead != 0) {
                } else {
                    return -1;
                }
            }
        }

        protected void implCloseChannel() throws IOException {
            this.in.close();
            this.open = false;
        }
    }

    private static class WritableByteChannelImpl extends AbstractInterruptibleChannel implements WritableByteChannel {
        private static final int TRANSFER_SIZE = 8192;
        private byte[] buf;
        private boolean open;
        OutputStream out;
        private Object writeLock;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.nio.channels.Channels.WritableByteChannelImpl.<init>(java.io.OutputStream):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        WritableByteChannelImpl(java.io.OutputStream r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.nio.channels.Channels.WritableByteChannelImpl.<init>(java.io.OutputStream):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.nio.channels.Channels.WritableByteChannelImpl.<init>(java.io.OutputStream):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.nio.channels.Channels.WritableByteChannelImpl.implCloseChannel():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected void implCloseChannel() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.nio.channels.Channels.WritableByteChannelImpl.implCloseChannel():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.nio.channels.Channels.WritableByteChannelImpl.implCloseChannel():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.nio.channels.Channels.WritableByteChannelImpl.write(java.nio.ByteBuffer):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public int write(java.nio.ByteBuffer r1) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.nio.channels.Channels.WritableByteChannelImpl.write(java.nio.ByteBuffer):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.nio.channels.Channels.WritableByteChannelImpl.write(java.nio.ByteBuffer):int");
        }
    }

    private Channels() {
    }

    private static void checkNotNull(Object o, String name) {
        if (o == null) {
            throw new NullPointerException("\"" + name + "\" is null!");
        }
    }

    private static void writeFullyImpl(WritableByteChannel ch, ByteBuffer bb) throws IOException {
        while (bb.remaining() > 0) {
            if (ch.write(bb) <= 0) {
                throw new RuntimeException("no bytes written");
            }
        }
    }

    private static void writeFully(WritableByteChannel ch, ByteBuffer bb) throws IOException {
        if (ch instanceof SelectableChannel) {
            SelectableChannel sc = (SelectableChannel) ch;
            synchronized (sc.blockingLock()) {
                if (sc.isBlocking()) {
                    writeFullyImpl(ch, bb);
                } else {
                    throw new IllegalBlockingModeException();
                }
            }
            return;
        }
        writeFullyImpl(ch, bb);
    }

    public static InputStream newInputStream(ReadableByteChannel ch) {
        checkNotNull(ch, "ch");
        return new ChannelInputStream(ch);
    }

    public static OutputStream newOutputStream(final WritableByteChannel ch) {
        checkNotNull(ch, "ch");
        return new OutputStream() {
            private byte[] b1 = null;
            private ByteBuffer bb = null;
            private byte[] bs = null;

            public synchronized void write(int b) throws IOException {
                if (this.b1 == null) {
                    this.b1 = new byte[1];
                }
                this.b1[0] = (byte) b;
                write(this.b1);
            }

            public synchronized void write(byte[] bs, int off, int len) throws IOException {
                if (off >= 0) {
                    if (off <= bs.length && len >= 0) {
                        if (off + len <= bs.length && off + len >= 0) {
                            if (len != 0) {
                                ByteBuffer bb;
                                if (this.bs == bs) {
                                    bb = this.bb;
                                } else {
                                    bb = ByteBuffer.wrap(bs);
                                }
                                bb.limit(Math.min(off + len, bb.capacity()));
                                bb.position(off);
                                this.bb = bb;
                                this.bs = bs;
                                Channels.writeFully(ch, bb);
                                return;
                            }
                            return;
                        }
                    }
                }
                throw new IndexOutOfBoundsException();
            }

            public void close() throws IOException {
                ch.close();
            }
        };
    }

    public static ReadableByteChannel newChannel(InputStream in) {
        checkNotNull(in, "in");
        if ((in instanceof FileInputStream) && FileInputStream.class.equals(in.getClass())) {
            return ((FileInputStream) in).getChannel();
        }
        return new ReadableByteChannelImpl(in);
    }

    public static WritableByteChannel newChannel(OutputStream out) {
        checkNotNull(out, "out");
        return new WritableByteChannelImpl(out);
    }

    public static Reader newReader(ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap) {
        checkNotNull(ch, "ch");
        return StreamDecoder.forDecoder(ch, dec.reset(), minBufferCap);
    }

    public static Reader newReader(ReadableByteChannel ch, String csName) {
        checkNotNull(csName, "csName");
        return newReader(ch, Charset.forName(csName).newDecoder(), -1);
    }

    public static Writer newWriter(WritableByteChannel ch, CharsetEncoder enc, int minBufferCap) {
        checkNotNull(ch, "ch");
        return StreamEncoder.forEncoder(ch, enc.reset(), minBufferCap);
    }

    public static Writer newWriter(WritableByteChannel ch, String csName) {
        checkNotNull(csName, "csName");
        return newWriter(ch, Charset.forName(csName).newEncoder(), -1);
    }
}
