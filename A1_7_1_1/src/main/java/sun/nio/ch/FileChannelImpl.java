package sun.nio.ch;

import android.system.ErrnoException;
import android.system.OsConstants;
import dalvik.system.BlockGuard;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DirectByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.AccessController;
import java.util.List;
import libcore.io.Libcore;
import sun.misc.Cleaner;
import sun.misc.IoTrace;
import sun.security.action.GetPropertyAction;

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
public class FileChannelImpl extends FileChannel {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f18-assertionsDisabled = false;
    private static final long MAPPED_TRANSFER_SIZE = 8388608;
    private static final int MAP_PV = 2;
    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int TRANSFER_SIZE = 8192;
    private static final long allocationGranularity = 0;
    private static volatile boolean fileSupported;
    private static boolean isSharedFileLockTable;
    private static volatile boolean pipeSupported;
    private static volatile boolean propertyChecked;
    private static volatile boolean transferSupported;
    private final boolean append;
    public final FileDescriptor fd;
    private volatile FileLockTable fileLockTable;
    private final FileDispatcher nd;
    private final Object parent;
    private final String path;
    private final Object positionLock;
    private final boolean readable;
    private final NativeThreadSet threads;
    private final boolean writable;

    private static class SimpleFileLockTable extends FileLockTable {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f19-assertionsDisabled = false;
        private final List<FileLock> lockList;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.<init>():void, dex: 
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
        public SimpleFileLockTable() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.checkList(long, long):void, dex: 
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
        private void checkList(long r1, long r3) throws java.nio.channels.OverlappingFileLockException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.checkList(long, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.checkList(long, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.add(java.nio.channels.FileLock):void, dex: 
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
        public void add(java.nio.channels.FileLock r1) throws java.nio.channels.OverlappingFileLockException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.add(java.nio.channels.FileLock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.add(java.nio.channels.FileLock):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.remove(java.nio.channels.FileLock):void, dex: 
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
        public void remove(java.nio.channels.FileLock r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.remove(java.nio.channels.FileLock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.remove(java.nio.channels.FileLock):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.removeAll():java.util.List<java.nio.channels.FileLock>, dex: 
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
        public java.util.List<java.nio.channels.FileLock> removeAll() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.removeAll():java.util.List<java.nio.channels.FileLock>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.removeAll():java.util.List<java.nio.channels.FileLock>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.replace(java.nio.channels.FileLock, java.nio.channels.FileLock):void, dex: 
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
        public void replace(java.nio.channels.FileLock r1, java.nio.channels.FileLock r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.replace(java.nio.channels.FileLock, java.nio.channels.FileLock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.SimpleFileLockTable.replace(java.nio.channels.FileLock, java.nio.channels.FileLock):void");
        }
    }

    private static class Unmapper implements Runnable {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f20-assertionsDisabled = false;
        static volatile int count;
        private static final NativeDispatcher nd = null;
        static volatile long totalCapacity;
        static volatile long totalSize;
        private volatile long address;
        private final int cap;
        private final FileDescriptor fd;
        private final long size;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.FileChannelImpl.Unmapper.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.FileChannelImpl.Unmapper.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.Unmapper.<clinit>():void");
        }

        /* synthetic */ Unmapper(long address, long size, int cap, FileDescriptor fd, Unmapper unmapper) {
            this(address, size, cap, fd);
        }

        private Unmapper(long address, long size, int cap, FileDescriptor fd) {
            if (!f20-assertionsDisabled) {
                if ((address != 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            this.address = address;
            this.size = size;
            this.cap = cap;
            this.fd = fd;
            synchronized (Unmapper.class) {
                count++;
                totalSize += size;
                totalCapacity += (long) cap;
            }
        }

        public void run() {
            if (this.address != 0) {
                FileChannelImpl.unmap0(this.address, this.size);
                this.address = 0;
                if (this.fd.valid()) {
                    try {
                        nd.close(this.fd);
                    } catch (IOException e) {
                    }
                }
                synchronized (Unmapper.class) {
                    count--;
                    totalSize -= this.size;
                    totalCapacity -= (long) this.cap;
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.FileChannelImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.FileChannelImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.FileChannelImpl.<clinit>():void");
    }

    private static native long initIDs();

    private native long map0(int i, long j, long j2) throws IOException;

    private native long position0(FileDescriptor fileDescriptor, long j);

    private native long transferTo0(int i, long j, long j2, int i2);

    private static native int unmap0(long j, long j2);

    private FileChannelImpl(FileDescriptor fd, String path, boolean readable, boolean writable, boolean append, Object parent) {
        this.threads = new NativeThreadSet(2);
        this.positionLock = new Object();
        this.fd = fd;
        this.readable = readable;
        this.writable = writable;
        this.append = append;
        this.parent = parent;
        this.path = path;
        this.nd = new FileDispatcherImpl(append);
    }

    public static FileChannel open(FileDescriptor fd, String path, boolean readable, boolean writable, Object parent) {
        return new FileChannelImpl(fd, path, readable, writable, f18-assertionsDisabled, parent);
    }

    public static FileChannel open(FileDescriptor fd, String path, boolean readable, boolean writable, boolean append, Object parent) {
        return new FileChannelImpl(fd, path, readable, writable, append, parent);
    }

    private void ensureOpen() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    protected void implCloseChannel() throws IOException {
        if (this.fileLockTable != null) {
            for (FileLock fl : this.fileLockTable.removeAll()) {
                synchronized (fl) {
                    if (fl.isValid()) {
                        this.nd.release(this.fd, fl.position(), fl.size());
                        ((FileLockImpl) fl).invalidate();
                    }
                }
            }
        }
        this.threads.signalAndWait();
        if (this.parent != null) {
            ((Closeable) this.parent).close();
        } else {
            this.nd.close(this.fd);
        }
    }

    /* JADX WARNING: Missing block: B:23:0x004a, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:45:0x0088, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(ByteBuffer dst) throws IOException {
        int normalize;
        boolean z = true;
        boolean z2 = f18-assertionsDisabled;
        ensureOpen();
        if (this.readable) {
            synchronized (this.positionLock) {
                int n = 0;
                int ti = -1;
                Object traceContext = IoTrace.fileReadBegin(this.path);
                try {
                    begin();
                    ti = this.threads.add();
                    if (isOpen()) {
                        int i;
                        do {
                            n = IOUtil.read(this.fd, dst, -1, this.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        normalize = IOStatus.normalize(n);
                        this.threads.remove(ti);
                        if (n > 0) {
                            i = n;
                        } else {
                            i = 0;
                        }
                        IoTrace.fileReadEnd(traceContext, (long) i);
                        if (n <= 0) {
                            z = f18-assertionsDisabled;
                        }
                        end(z);
                        if (f18-assertionsDisabled || IOStatus.check(n)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    IoTrace.fileReadEnd(traceContext, (long) null);
                    end(f18-assertionsDisabled);
                    if (f18-assertionsDisabled || IOStatus.check(0)) {
                    } else {
                        throw new AssertionError();
                    }
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    if (n > 0) {
                        normalize = n;
                    } else {
                        normalize = 0;
                    }
                    IoTrace.fileReadEnd(traceContext, (long) normalize);
                    if (n > 0) {
                        z2 = true;
                    }
                    end(z2);
                    if (!f18-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        } else {
            throw new NonReadableChannelException();
        }
    }

    /* JADX WARNING: Missing block: B:53:0x00a2, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        }
        ensureOpen();
        if (this.readable) {
            synchronized (this.positionLock) {
                long n = 0;
                int ti = -1;
                Object traceContext = IoTrace.fileReadBegin(this.path);
                try {
                    begin();
                    ti = this.threads.add();
                    if (isOpen()) {
                        do {
                            n = IOUtil.read(this.fd, dsts, offset, length, this.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        long normalize = IOStatus.normalize(n);
                        this.threads.remove(ti);
                        IoTrace.fileReadEnd(traceContext, n > 0 ? n : 0);
                        end(n > 0 ? true : f18-assertionsDisabled);
                        if (f18-assertionsDisabled || IOStatus.check(n)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    IoTrace.fileReadEnd(traceContext, 0);
                    end(f18-assertionsDisabled);
                    if (f18-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    IoTrace.fileReadEnd(traceContext, n > 0 ? n : 0);
                    end(n > 0 ? true : f18-assertionsDisabled);
                    if (!f18-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        } else {
            throw new NonReadableChannelException();
        }
    }

    /* JADX WARNING: Missing block: B:23:0x004a, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:44:0x0086, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int write(ByteBuffer src) throws IOException {
        boolean z = true;
        int i = 0;
        ensureOpen();
        if (this.writable) {
            synchronized (this.positionLock) {
                int n = 0;
                int ti = -1;
                Object traceContext = IoTrace.fileWriteBegin(this.path);
                try {
                    begin();
                    ti = this.threads.add();
                    if (isOpen()) {
                        do {
                            n = IOUtil.write(this.fd, src, -1, this.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        int normalize = IOStatus.normalize(n);
                        this.threads.remove(ti);
                        if (n <= 0) {
                            z = f18-assertionsDisabled;
                        }
                        end(z);
                        if (n > 0) {
                            i = n;
                        }
                        IoTrace.fileWriteEnd(traceContext, (long) i);
                        if (f18-assertionsDisabled || IOStatus.check(n)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    end(f18-assertionsDisabled);
                    IoTrace.fileWriteEnd(traceContext, (long) null);
                    if (f18-assertionsDisabled || IOStatus.check(0)) {
                    } else {
                        throw new AssertionError();
                    }
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    if (n <= 0) {
                        z = f18-assertionsDisabled;
                    }
                    end(z);
                    if (n > 0) {
                        i = n;
                    }
                    IoTrace.fileWriteEnd(traceContext, (long) i);
                    if (!f18-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    /* JADX WARNING: Missing block: B:53:0x00a2, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        ensureOpen();
        if (this.writable) {
            synchronized (this.positionLock) {
                long n = 0;
                int ti = -1;
                Object traceContext = IoTrace.fileWriteBegin(this.path);
                try {
                    begin();
                    ti = this.threads.add();
                    if (isOpen()) {
                        do {
                            n = IOUtil.write(this.fd, srcs, offset, length, this.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        long normalize = IOStatus.normalize(n);
                        this.threads.remove(ti);
                        IoTrace.fileWriteEnd(traceContext, n > 0 ? n : 0);
                        end(n > 0 ? true : f18-assertionsDisabled);
                        if (f18-assertionsDisabled || IOStatus.check(n)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    IoTrace.fileWriteEnd(traceContext, 0);
                    end(f18-assertionsDisabled);
                    if (f18-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    IoTrace.fileWriteEnd(traceContext, n > 0 ? n : 0);
                    end(n > 0 ? true : f18-assertionsDisabled);
                    if (!f18-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    /* JADX WARNING: Missing block: B:44:0x008b, code:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long position() throws IOException {
        boolean z = true;
        ensureOpen();
        synchronized (this.positionLock) {
            long p = -1;
            int ti = -1;
            try {
                begin();
                ti = this.threads.add();
                if (isOpen()) {
                    if (this.append) {
                        BlockGuard.getThreadPolicy().onWriteToDisk();
                    }
                    do {
                        p = this.append ? this.nd.size(this.fd) : position0(this.fd, -1);
                        if (p != -3) {
                            break;
                        }
                    } while (isOpen());
                    long normalize = IOStatus.normalize(p);
                    this.threads.remove(ti);
                    if (p <= -1) {
                        z = f18-assertionsDisabled;
                    }
                    end(z);
                    if (f18-assertionsDisabled || IOStatus.check(p)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.threads.remove(ti);
                end(f18-assertionsDisabled);
                if (f18-assertionsDisabled || IOStatus.check(-1)) {
                    return 0;
                }
                throw new AssertionError();
            } catch (Throwable th) {
                this.threads.remove(ti);
                if (p <= -1) {
                    z = f18-assertionsDisabled;
                }
                end(z);
                if (!f18-assertionsDisabled && !IOStatus.check(p)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:40:0x007f, code:
            return r13;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public FileChannel position(long newPosition) throws IOException {
        boolean z = true;
        ensureOpen();
        if (newPosition < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (this.positionLock) {
            int ti = -1;
            try {
                begin();
                ti = this.threads.add();
                if (isOpen()) {
                    long p;
                    BlockGuard.getThreadPolicy().onReadFromDisk();
                    do {
                        p = position0(this.fd, newPosition);
                        if (p == -3) {
                        }
                        break;
                    } while (isOpen());
                    this.threads.remove(ti);
                    if (p <= -1) {
                        z = f18-assertionsDisabled;
                    }
                    end(z);
                    if (f18-assertionsDisabled || IOStatus.check(p)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.threads.remove(ti);
                end(f18-assertionsDisabled);
                if (f18-assertionsDisabled || IOStatus.check(-1)) {
                    return null;
                }
                throw new AssertionError();
            } catch (Throwable th) {
                this.threads.remove(ti);
                if (-1 <= -1) {
                    z = f18-assertionsDisabled;
                }
                end(z);
                if (!f18-assertionsDisabled && !IOStatus.check(-1)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0039, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:35:0x0071, code:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long size() throws IOException {
        boolean z = true;
        ensureOpen();
        synchronized (this.positionLock) {
            long s = -1;
            int ti = -1;
            try {
                begin();
                ti = this.threads.add();
                if (isOpen()) {
                    do {
                        s = this.nd.size(this.fd);
                        if (s != -3) {
                            break;
                        }
                    } while (isOpen());
                    long normalize = IOStatus.normalize(s);
                    this.threads.remove(ti);
                    if (s <= -1) {
                        z = f18-assertionsDisabled;
                    }
                    end(z);
                    if (f18-assertionsDisabled || IOStatus.check(s)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.threads.remove(ti);
                end(f18-assertionsDisabled);
                if (f18-assertionsDisabled || IOStatus.check(-1)) {
                } else {
                    throw new AssertionError();
                }
            } catch (Throwable th) {
                this.threads.remove(ti);
                if (s <= -1) {
                    z = f18-assertionsDisabled;
                }
                end(z);
                if (!f18-assertionsDisabled && !IOStatus.check(s)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:25:0x0051, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:41:0x0086, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:82:0x00f2, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:102:0x0124, code:
            return r13;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public FileChannel truncate(long size) throws IOException {
        boolean z = true;
        ensureOpen();
        if (size < 0) {
            throw new IllegalArgumentException();
        } else if (this.writable) {
            synchronized (this.positionLock) {
                int rv = -1;
                int ti = -1;
                begin();
                ti = this.threads.add();
                if (isOpen()) {
                    long p;
                    do {
                        try {
                            p = position0(this.fd, -1);
                            if (p != -3) {
                                break;
                            }
                        } catch (Throwable th) {
                            this.threads.remove(ti);
                            if (rv <= -1) {
                                z = f18-assertionsDisabled;
                            }
                            end(z);
                            if (!f18-assertionsDisabled && !IOStatus.check(rv)) {
                                AssertionError assertionError = new AssertionError();
                            }
                        }
                    } while (isOpen());
                    if (isOpen()) {
                        if (!f18-assertionsDisabled) {
                            boolean z2;
                            if (p >= 0) {
                                z2 = true;
                            } else {
                                z2 = false;
                            }
                            if (!z2) {
                                throw new AssertionError();
                            }
                        }
                        if (size < size()) {
                            do {
                                rv = this.nd.truncate(this.fd, size);
                                if (rv != -3) {
                                    break;
                                }
                            } while (isOpen());
                            if (!isOpen()) {
                                this.threads.remove(ti);
                                if (rv <= -1) {
                                    z = f18-assertionsDisabled;
                                }
                                end(z);
                                if (f18-assertionsDisabled || IOStatus.check(rv)) {
                                } else {
                                    throw new AssertionError();
                                }
                            }
                        }
                        if (p > size) {
                            p = size;
                        }
                        do {
                            rv = (int) position0(this.fd, p);
                            if (rv == -3) {
                            }
                            break;
                        } while (isOpen());
                        this.threads.remove(ti);
                        if (rv <= -1) {
                            z = f18-assertionsDisabled;
                        }
                        end(z);
                        if (f18-assertionsDisabled || IOStatus.check(rv)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                    this.threads.remove(ti);
                    end(f18-assertionsDisabled);
                    if (f18-assertionsDisabled || IOStatus.check(-1)) {
                    } else {
                        throw new AssertionError();
                    }
                }
                this.threads.remove(ti);
                end(f18-assertionsDisabled);
                if (f18-assertionsDisabled || IOStatus.check(-1)) {
                } else {
                    throw new AssertionError();
                }
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public void force(boolean metaData) throws IOException {
        boolean z = true;
        ensureOpen();
        int rv = -1;
        int ti = -1;
        try {
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                do {
                    rv = this.nd.force(this.fd, metaData);
                    if (rv != -3) {
                        break;
                    }
                } while (isOpen());
                this.threads.remove(ti);
                if (rv <= -1) {
                    z = f18-assertionsDisabled;
                }
                end(z);
                if (!f18-assertionsDisabled && !IOStatus.check(rv)) {
                    throw new AssertionError();
                }
                return;
            }
            this.threads.remove(ti);
            end(f18-assertionsDisabled);
            if (!f18-assertionsDisabled && !IOStatus.check(-1)) {
                throw new AssertionError();
            }
        } catch (Throwable th) {
            this.threads.remove(ti);
            if (rv <= -1) {
                z = f18-assertionsDisabled;
            }
            end(z);
            if (!f18-assertionsDisabled && !IOStatus.check(rv)) {
                AssertionError assertionError = new AssertionError();
            }
        }
    }

    private long transferToDirectly(long position, int icount, WritableByteChannel target) throws IOException {
        if (!transferSupported) {
            return -4;
        }
        FileDescriptor targetFD = null;
        if (target instanceof FileChannelImpl) {
            if (!fileSupported) {
                return -6;
            }
            targetFD = ((FileChannelImpl) target).fd;
        } else if (target instanceof SelChImpl) {
            if ((target instanceof SinkChannelImpl) && !pipeSupported) {
                return -6;
            }
            targetFD = ((SelChImpl) target).getFD();
        }
        if (targetFD == null) {
            return -4;
        }
        int thisFDVal = IOUtil.fdVal(this.fd);
        int targetFDVal = IOUtil.fdVal(targetFD);
        if (thisFDVal == targetFDVal) {
            return -4;
        }
        long n = -1;
        int ti = -1;
        try {
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                BlockGuard.getThreadPolicy().onWriteToDisk();
                do {
                    n = transferTo0(thisFDVal, position, (long) icount, targetFDVal);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                boolean z;
                if (n == -6) {
                    if (target instanceof SinkChannelImpl) {
                        pipeSupported = f18-assertionsDisabled;
                    }
                    if (target instanceof FileChannelImpl) {
                        fileSupported = f18-assertionsDisabled;
                    }
                    this.threads.remove(ti);
                    if (n > -1) {
                        z = true;
                    } else {
                        z = f18-assertionsDisabled;
                    }
                    end(z);
                    return -6;
                } else if (n == -4) {
                    transferSupported = f18-assertionsDisabled;
                    this.threads.remove(ti);
                    if (n > -1) {
                        z = true;
                    } else {
                        z = f18-assertionsDisabled;
                    }
                    end(z);
                    return -4;
                } else {
                    long normalize = IOStatus.normalize(n);
                    this.threads.remove(ti);
                    end(n > -1 ? true : f18-assertionsDisabled);
                    return normalize;
                }
            }
            this.threads.remove(ti);
            end(f18-assertionsDisabled);
            return -1;
        } catch (Throwable th) {
            this.threads.remove(ti);
            end(n > -1 ? true : f18-assertionsDisabled);
        }
    }

    private long transferToTrustedChannel(long position, long count, WritableByteChannel target) throws IOException {
        boolean isSelChImpl = target instanceof SelChImpl;
        if (!(!(target instanceof FileChannelImpl) ? isSelChImpl : true)) {
            return -4;
        }
        long remaining = count;
        while (remaining > 0) {
            MappedByteBuffer dbb;
            try {
                dbb = map(MapMode.READ_ONLY, position, Math.min(remaining, (long) MAPPED_TRANSFER_SIZE));
                int n = target.write(dbb);
                if (!f18-assertionsDisabled) {
                    if ((n >= 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                remaining -= (long) n;
                if (isSelChImpl) {
                    unmap(dbb);
                    break;
                }
                if (!f18-assertionsDisabled) {
                    if ((n > 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                position += (long) n;
                unmap(dbb);
            } catch (ClosedByInterruptException e) {
                if (!f18-assertionsDisabled) {
                    Object obj;
                    if (target.isOpen()) {
                        obj = null;
                    } else {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                try {
                    close();
                } catch (Throwable suppressed) {
                    e.addSuppressed(suppressed);
                }
                throw e;
            } catch (IOException ioe) {
                if (remaining == count) {
                    throw ioe;
                }
            } catch (Throwable th) {
                unmap(dbb);
            }
        }
        return count - remaining;
    }

    private long transferToArbitraryChannel(long position, int icount, WritableByteChannel target) throws IOException {
        ByteBuffer bb = Util.getTemporaryDirectBuffer(Math.min(icount, 8192));
        long tw = 0;
        long pos = position;
        try {
            Util.erase(bb);
            while (tw < ((long) icount)) {
                bb.limit(Math.min((int) (((long) icount) - tw), 8192));
                int nr = read(bb, pos);
                if (nr > 0) {
                    bb.flip();
                    int nw = target.write(bb);
                    tw += (long) nw;
                    if (nw != nr) {
                        break;
                    }
                    pos += (long) nw;
                    bb.clear();
                } else {
                    break;
                }
            }
            Util.releaseTemporaryDirectBuffer(bb);
            return tw;
        } catch (IOException x) {
            if (tw > 0) {
                Util.releaseTemporaryDirectBuffer(bb);
                return tw;
            }
            throw x;
        } catch (Throwable th) {
            Util.releaseTemporaryDirectBuffer(bb);
            throw th;
        }
    }

    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        ensureOpen();
        if (!target.isOpen()) {
            throw new ClosedChannelException();
        } else if (!this.readable) {
            throw new NonReadableChannelException();
        } else if ((target instanceof FileChannelImpl) && !((FileChannelImpl) target).writable) {
            throw new NonWritableChannelException();
        } else if (position < 0 || count < 0) {
            throw new IllegalArgumentException();
        } else {
            long sz = size();
            if (position > sz) {
                return 0;
            }
            int icount = (int) Math.min(count, 2147483647L);
            if (sz - position < ((long) icount)) {
                icount = (int) (sz - position);
            }
            long n = transferToDirectly(position, icount, target);
            if (n >= 0) {
                return n;
            }
            n = transferToTrustedChannel(position, (long) icount, target);
            if (n >= 0) {
                return n;
            }
            return transferToArbitraryChannel(position, icount, target);
        }
    }

    private long transferFromFileChannel(FileChannelImpl src, long position, long count) throws IOException {
        if (src.readable) {
            long nwritten;
            synchronized (src.positionLock) {
                long pos = src.position();
                long max = Math.min(count, src.size() - pos);
                long remaining = max;
                long p = pos;
                while (remaining > 0) {
                    MappedByteBuffer bb = src.map(MapMode.READ_ONLY, p, Math.min(remaining, (long) MAPPED_TRANSFER_SIZE));
                    try {
                        long n = (long) write(bb, position);
                        if (!f18-assertionsDisabled) {
                            if ((n > 0 ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        p += n;
                        position += n;
                        remaining -= n;
                        unmap(bb);
                    } catch (IOException ioe) {
                        if (remaining == max) {
                            throw ioe;
                        } else {
                            unmap(bb);
                        }
                    } catch (Throwable th) {
                        unmap(bb);
                    }
                }
                nwritten = max - remaining;
                src.position(pos + nwritten);
            }
            return nwritten;
        }
        throw new NonReadableChannelException();
    }

    private long transferFromArbitraryChannel(ReadableByteChannel src, long position, long count) throws IOException {
        ByteBuffer bb = Util.getTemporaryDirectBuffer((int) Math.min(count, 8192));
        long tw = 0;
        long pos = position;
        try {
            Util.erase(bb);
            while (tw < count) {
                bb.limit((int) Math.min(count - tw, 8192));
                int nr = src.read(bb);
                if (nr > 0) {
                    bb.flip();
                    int nw = write(bb, pos);
                    tw += (long) nw;
                    if (nw != nr) {
                        break;
                    }
                    pos += (long) nw;
                    bb.clear();
                } else {
                    break;
                }
            }
            Util.releaseTemporaryDirectBuffer(bb);
            return tw;
        } catch (IOException x) {
            if (tw > 0) {
                Util.releaseTemporaryDirectBuffer(bb);
                return tw;
            }
            throw x;
        } catch (Throwable th) {
            Util.releaseTemporaryDirectBuffer(bb);
            throw th;
        }
    }

    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        ensureOpen();
        if (!src.isOpen()) {
            throw new ClosedChannelException();
        } else if (!this.writable) {
            throw new NonWritableChannelException();
        } else if (position < 0 || count < 0) {
            throw new IllegalArgumentException();
        } else if (position > size()) {
            return 0;
        } else {
            if (!(src instanceof FileChannelImpl)) {
                return transferFromArbitraryChannel(src, position, count);
            }
            return transferFromFileChannel((FileChannelImpl) src, position, count);
        }
    }

    public int read(ByteBuffer dst, long position) throws IOException {
        if (dst == null) {
            throw new NullPointerException();
        } else if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (this.readable) {
            ensureOpen();
            if (!this.nd.needsPositionLock()) {
                return readInternal(dst, position);
            }
            int readInternal;
            synchronized (this.positionLock) {
                readInternal = readInternal(dst, position);
            }
            return readInternal;
        } else {
            throw new NonReadableChannelException();
        }
    }

    private int readInternal(ByteBuffer dst, long position) throws IOException {
        boolean z = true;
        boolean z2 = f18-assertionsDisabled;
        if (!f18-assertionsDisabled) {
            if (!(this.nd.needsPositionLock() ? Thread.holdsLock(this.positionLock) : true)) {
                throw new AssertionError();
            }
        }
        int n = 0;
        int ti = -1;
        Object traceContext = IoTrace.fileReadBegin(this.path);
        int normalize;
        try {
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                int i;
                do {
                    n = IOUtil.read(this.fd, dst, position, this.nd);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                normalize = IOStatus.normalize(n);
                this.threads.remove(ti);
                if (n > 0) {
                    i = n;
                } else {
                    i = 0;
                }
                IoTrace.fileReadEnd(traceContext, (long) i);
                if (n > 0) {
                    z2 = true;
                }
                end(z2);
                if (f18-assertionsDisabled || IOStatus.check(n)) {
                    return normalize;
                }
                throw new AssertionError();
            }
            this.threads.remove(ti);
            IoTrace.fileReadEnd(traceContext, (long) null);
            end(f18-assertionsDisabled);
            if (f18-assertionsDisabled || IOStatus.check(0)) {
                return -1;
            }
            throw new AssertionError();
        } catch (Throwable th) {
            this.threads.remove(ti);
            if (n > 0) {
                normalize = n;
            } else {
                normalize = 0;
            }
            IoTrace.fileReadEnd(traceContext, (long) normalize);
            if (n <= 0) {
                z = f18-assertionsDisabled;
            }
            end(z);
            if (!f18-assertionsDisabled && !IOStatus.check(n)) {
                AssertionError assertionError = new AssertionError();
            }
        }
    }

    public int write(ByteBuffer src, long position) throws IOException {
        if (src == null) {
            throw new NullPointerException();
        } else if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (this.writable) {
            ensureOpen();
            if (!this.nd.needsPositionLock()) {
                return writeInternal(src, position);
            }
            int writeInternal;
            synchronized (this.positionLock) {
                writeInternal = writeInternal(src, position);
            }
            return writeInternal;
        } else {
            throw new NonWritableChannelException();
        }
    }

    private int writeInternal(ByteBuffer src, long position) throws IOException {
        boolean z = true;
        int i = 0;
        if (!f18-assertionsDisabled) {
            if (!(this.nd.needsPositionLock() ? Thread.holdsLock(this.positionLock) : true)) {
                throw new AssertionError();
            }
        }
        int n = 0;
        int ti = -1;
        Object traceContext = IoTrace.fileWriteBegin(this.path);
        try {
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                do {
                    n = IOUtil.write(this.fd, src, position, this.nd);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                int normalize = IOStatus.normalize(n);
                this.threads.remove(ti);
                if (n <= 0) {
                    z = f18-assertionsDisabled;
                }
                end(z);
                if (n > 0) {
                    i = n;
                }
                IoTrace.fileWriteEnd(traceContext, (long) i);
                if (f18-assertionsDisabled || IOStatus.check(n)) {
                    return normalize;
                }
                throw new AssertionError();
            }
            this.threads.remove(ti);
            end(f18-assertionsDisabled);
            IoTrace.fileWriteEnd(traceContext, (long) null);
            if (f18-assertionsDisabled || IOStatus.check(0)) {
                return -1;
            }
            throw new AssertionError();
        } catch (Throwable th) {
            this.threads.remove(ti);
            if (n <= 0) {
                z = f18-assertionsDisabled;
            }
            end(z);
            if (n > 0) {
                i = n;
            }
            IoTrace.fileWriteEnd(traceContext, (long) i);
            if (!f18-assertionsDisabled && !IOStatus.check(n)) {
                AssertionError assertionError = new AssertionError();
            }
        }
    }

    private static void unmap(MappedByteBuffer bb) {
        Cleaner cl = ((DirectBuffer) bb).cleaner();
        if (cl != null) {
            cl.clean();
        }
    }

    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        ensureOpen();
        if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (size < 0) {
            throw new IllegalArgumentException("Negative size");
        } else if (position + size < 0) {
            throw new IllegalArgumentException("Position + size overflow");
        } else if (size > 2147483647L) {
            throw new IllegalArgumentException("Size exceeds Integer.MAX_VALUE");
        } else {
            int imode = -1;
            if (mode == MapMode.READ_ONLY) {
                imode = 0;
            } else if (mode == MapMode.READ_WRITE) {
                imode = 1;
            } else if (mode == MapMode.PRIVATE) {
                imode = 2;
            }
            if (!f18-assertionsDisabled) {
                if ((imode >= 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            if (mode != MapMode.READ_ONLY && !this.writable) {
                throw new NonWritableChannelException();
            } else if (this.readable) {
                long addr = -1;
                int ti = -1;
                long mapPosition;
                long mapSize;
                try {
                    begin();
                    ti = this.threads.add();
                    if (isOpen()) {
                        if (size() < position + size) {
                            do {
                                try {
                                    if (this.nd.truncate(this.fd, position + size) != -3) {
                                        break;
                                    }
                                } catch (IOException r) {
                                    if (OsConstants.S_ISREG(Libcore.os.fstat(this.fd).st_mode)) {
                                        throw r;
                                    }
                                } catch (ErrnoException e) {
                                    e.rethrowAsIOException();
                                }
                            } while (isOpen());
                        }
                        if (size == 0) {
                            FileDescriptor dummy = new FileDescriptor();
                            boolean z = (!this.writable || imode == 0) ? true : f18-assertionsDisabled;
                            MappedByteBuffer directByteBuffer = new DirectByteBuffer(0, 0, dummy, null, z);
                            this.threads.remove(ti);
                            end(IOStatus.checkAll(0));
                            return directByteBuffer;
                        }
                        int pagePosition = (int) (position % allocationGranularity);
                        mapPosition = position - ((long) pagePosition);
                        mapSize = size + ((long) pagePosition);
                        BlockGuard.getThreadPolicy().onReadFromDisk();
                        addr = map0(imode, mapPosition, mapSize);
                        FileDescriptor mfd = this.nd.duplicateForMapping(this.fd);
                        if (f18-assertionsDisabled || IOStatus.checkAll(addr)) {
                            if (!f18-assertionsDisabled) {
                                if ((addr % allocationGranularity == 0 ? 1 : null) == null) {
                                    throw new AssertionError();
                                }
                            }
                            int isize = (int) size;
                            Unmapper um = new Unmapper(addr, mapSize, isize, mfd, null);
                            long j = addr + ((long) pagePosition);
                            boolean z2 = (!this.writable || imode == 0) ? true : f18-assertionsDisabled;
                            MappedByteBuffer directByteBuffer2 = new DirectByteBuffer(isize, j, mfd, um, z2);
                            this.threads.remove(ti);
                            end(IOStatus.checkAll(addr));
                            return directByteBuffer2;
                        }
                        throw new AssertionError();
                    }
                    this.threads.remove(ti);
                    end(IOStatus.checkAll(addr));
                    return null;
                } catch (IOException ioe) {
                    unmap0(addr, mapSize);
                    throw ioe;
                } catch (Throwable y) {
                    throw new IOException("Map failed", y);
                } catch (OutOfMemoryError e2) {
                    System.gc();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e3) {
                        Thread.currentThread().interrupt();
                    }
                    addr = map0(imode, mapPosition, mapSize);
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    end(IOStatus.checkAll(addr));
                }
            } else {
                throw new NonReadableChannelException();
            }
        }
    }

    private static boolean isSharedFileLockTable() {
        boolean z = true;
        if (!propertyChecked) {
            synchronized (FileChannelImpl.class) {
                if (!propertyChecked) {
                    String value = (String) AccessController.doPrivileged(new GetPropertyAction("sun.nio.ch.disableSystemWideOverlappingFileLockCheck"));
                    if (value != null) {
                        z = value.equals("false");
                    }
                    isSharedFileLockTable = z;
                    propertyChecked = true;
                }
            }
        }
        return isSharedFileLockTable;
    }

    private FileLockTable fileLockTable() throws IOException {
        if (this.fileLockTable == null) {
            synchronized (this) {
                if (this.fileLockTable == null) {
                    if (isSharedFileLockTable()) {
                        int ti = this.threads.add();
                        try {
                            ensureOpen();
                            this.fileLockTable = FileLockTable.newSharedFileLockTable(this, this.fd);
                            this.threads.remove(ti);
                        } catch (Throwable th) {
                            this.threads.remove(ti);
                        }
                    } else {
                        this.fileLockTable = new SimpleFileLockTable();
                    }
                }
            }
        }
        return this.fileLockTable;
    }

    public FileLock lock(long position, long size, boolean shared) throws IOException {
        ensureOpen();
        if (shared && !this.readable) {
            throw new NonReadableChannelException();
        } else if (shared || this.writable) {
            FileLockImpl fli = new FileLockImpl(this, position, size, shared);
            FileLockTable flt = fileLockTable();
            flt.add(fli);
            boolean completed = f18-assertionsDisabled;
            int ti = -1;
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                int n;
                while (true) {
                    try {
                        n = this.nd.lock(this.fd, true, position, size, shared);
                        if (n == 2) {
                            if (!isOpen()) {
                                break;
                            }
                        }
                        break;
                    } finally {
                        if (null == null) {
                            flt.remove(fli);
                        }
                        this.threads.remove(ti);
                        try {
                            end(f18-assertionsDisabled);
                        } catch (ClosedByInterruptException e) {
                            throw new FileLockInterruptionException();
                        }
                    }
                }
                if (isOpen()) {
                    if (n == 1) {
                        if (f18-assertionsDisabled || shared) {
                            FileLockImpl fli2 = new FileLockImpl(this, position, size, f18-assertionsDisabled);
                            flt.replace(fli, fli2);
                            fli = fli2;
                        } else {
                            throw new AssertionError();
                        }
                    }
                    completed = true;
                }
                if (!completed) {
                    flt.remove(fli);
                }
                this.threads.remove(ti);
                try {
                    end(completed);
                    return fli;
                } catch (ClosedByInterruptException e2) {
                    throw new FileLockInterruptionException();
                }
            }
            try {
                end(f18-assertionsDisabled);
                return null;
            } catch (ClosedByInterruptException e3) {
                throw new FileLockInterruptionException();
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        ensureOpen();
        if (shared && !this.readable) {
            throw new NonReadableChannelException();
        } else if (shared || this.writable) {
            FileLockImpl fli = new FileLockImpl(this, position, size, shared);
            FileLockTable flt = fileLockTable();
            flt.add(fli);
            int ti = this.threads.add();
            try {
                ensureOpen();
                int result = this.nd.lock(this.fd, f18-assertionsDisabled, position, size, shared);
                if (result == -1) {
                    flt.remove(fli);
                    this.threads.remove(ti);
                    return null;
                } else if (result != 1) {
                    this.threads.remove(ti);
                    return fli;
                } else if (f18-assertionsDisabled || shared) {
                    FileLockImpl fli2 = new FileLockImpl(this, position, size, f18-assertionsDisabled);
                    flt.replace(fli, fli2);
                    this.threads.remove(ti);
                    return fli2;
                } else {
                    throw new AssertionError();
                }
            } catch (IOException e) {
                flt.remove(fli);
                throw e;
            } catch (Throwable th) {
                this.threads.remove(ti);
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    void release(FileLockImpl fli) throws IOException {
        int ti = this.threads.add();
        try {
            ensureOpen();
            this.nd.release(this.fd, fli.position(), fli.size());
            if (!f18-assertionsDisabled) {
                if ((this.fileLockTable != null ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            this.fileLockTable.remove(fli);
        } finally {
            this.threads.remove(ti);
        }
    }
}
