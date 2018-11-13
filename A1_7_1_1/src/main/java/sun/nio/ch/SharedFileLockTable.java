package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.channels.Channel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
/* compiled from: FileLockTable */
class SharedFileLockTable extends FileLockTable {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f145-assertionsDisabled = false;
    private static ConcurrentHashMap<FileKey, List<FileLockReference>> lockMap;
    private static ReferenceQueue<FileLock> queue;
    private final Channel channel;
    private final FileKey fileKey;

    /* compiled from: FileLockTable */
    private static class FileLockReference extends WeakReference<FileLock> {
        private FileKey fileKey;

        FileLockReference(FileLock referent, ReferenceQueue<FileLock> queue, FileKey key) {
            super(referent, queue);
            this.fileKey = key;
        }

        FileKey fileKey() {
            return this.fileKey;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.SharedFileLockTable.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.nio.ch.SharedFileLockTable.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SharedFileLockTable.<clinit>():void");
    }

    SharedFileLockTable(Channel channel, FileDescriptor fd) throws IOException {
        this.channel = channel;
        this.fileKey = FileKey.create(fd);
    }

    public void add(FileLock fl) throws OverlappingFileLockException {
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        while (true) {
            List<FileLockReference> list2;
            if (list == null) {
                List<FileLockReference> prev;
                list = new ArrayList(2);
                synchronized (list) {
                    prev = (List) lockMap.putIfAbsent(this.fileKey, list);
                    if (prev == null) {
                        list.add(new FileLockReference(fl, queue, this.fileKey));
                        break;
                    }
                }
                list = prev;
            }
            synchronized (list) {
                List<FileLockReference> current = (List) lockMap.get(this.fileKey);
                if (list == current) {
                    checkList(list, fl.position(), fl.size());
                    list.add(new FileLockReference(fl, queue, this.fileKey));
                    break;
                }
                list2 = current;
            }
            list = list2;
        }
        removeStaleEntries();
    }

    private void removeKeyIfEmpty(FileKey fk, List<FileLockReference> list) {
        if (f145-assertionsDisabled || Thread.holdsLock(list)) {
            if (!f145-assertionsDisabled) {
                if ((lockMap.get(fk) == list ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            if (list.isEmpty()) {
                lockMap.remove(fk);
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    public void remove(FileLock fl) {
        Object obj = 1;
        if (!f145-assertionsDisabled) {
            if ((fl != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (index < list.size()) {
                    FileLockReference ref = (FileLockReference) list.get(index);
                    FileLock lock = (FileLock) ref.get();
                    if (lock == fl) {
                        if (!f145-assertionsDisabled) {
                            if (lock == null || lock.acquiredBy() != this.channel) {
                                obj = null;
                            }
                            if (obj == null) {
                                throw new AssertionError();
                            }
                        }
                        ref.clear();
                        list.remove(index);
                    } else {
                        index++;
                    }
                }
            }
        }
    }

    public List<FileLock> removeAll() {
        List<FileLock> result = new ArrayList();
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (index < list.size()) {
                    FileLockReference ref = (FileLockReference) list.get(index);
                    FileLock lock = (FileLock) ref.get();
                    if (lock == null || lock.acquiredBy() != this.channel) {
                        index++;
                    } else {
                        ref.clear();
                        list.remove(index);
                        result.add(lock);
                    }
                }
                removeKeyIfEmpty(this.fileKey, list);
            }
        }
        return result;
    }

    public void replace(FileLock fromLock, FileLock toLock) {
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (!f145-assertionsDisabled) {
            if ((list != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        synchronized (list) {
            for (int index = 0; index < list.size(); index++) {
                FileLockReference ref = (FileLockReference) list.get(index);
                if (((FileLock) ref.get()) == fromLock) {
                    ref.clear();
                    list.set(index, new FileLockReference(toLock, queue, this.fileKey));
                    break;
                }
            }
        }
    }

    private void checkList(List<FileLockReference> list, long position, long size) throws OverlappingFileLockException {
        if (f145-assertionsDisabled || Thread.holdsLock(list)) {
            for (FileLockReference ref : list) {
                FileLock fl = (FileLock) ref.get();
                if (fl != null && fl.overlaps(position, size)) {
                    throw new OverlappingFileLockException();
                }
            }
            return;
        }
        throw new AssertionError();
    }

    private void removeStaleEntries() {
        while (true) {
            Object ref = (FileLockReference) queue.poll();
            if (ref != null) {
                FileKey fk = ref.fileKey();
                List<FileLockReference> list = (List) lockMap.get(fk);
                if (list != null) {
                    synchronized (list) {
                        list.remove(ref);
                        removeKeyIfEmpty(fk, list);
                    }
                }
            } else {
                return;
            }
        }
    }
}
