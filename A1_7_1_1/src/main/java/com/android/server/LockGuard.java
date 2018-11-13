package com.android.server;

import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

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
public class LockGuard {
    private static final String TAG = "LockGuard";
    private static ArrayMap<Object, LockInfo> sKnown;

    private static class LockInfo {
        public ArraySet<Object> children;
        public String label;

        /* synthetic */ LockInfo(LockInfo lockInfo) {
            this();
        }

        private LockInfo() {
            this.children = new ArraySet(0, true);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.LockGuard.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.LockGuard.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.LockGuard.<clinit>():void");
    }

    private static LockInfo findOrCreateLockInfo(Object lock) {
        LockInfo info = (LockInfo) sKnown.get(lock);
        if (info != null) {
            return info;
        }
        info = new LockInfo();
        info.label = "0x" + Integer.toHexString(System.identityHashCode(lock)) + " [" + new Throwable().getStackTrace()[2].toString() + "]";
        sKnown.put(lock, info);
        return info;
    }

    public static Object guard(Object lock) {
        if (lock == null || Thread.holdsLock(lock)) {
            return lock;
        }
        int i;
        boolean triggered = false;
        LockInfo info = findOrCreateLockInfo(lock);
        for (i = 0; i < info.children.size(); i++) {
            Object child = info.children.valueAt(i);
            if (child != null && Thread.holdsLock(child)) {
                Slog.w(TAG, "Calling thread " + Thread.currentThread().getName() + " is holding " + lockToString(child) + " while trying to acquire " + lockToString(lock), new Throwable());
                triggered = true;
            }
        }
        if (!triggered) {
            for (i = 0; i < sKnown.size(); i++) {
                Object test = sKnown.keyAt(i);
                if (!(test == null || test == lock || !Thread.holdsLock(test))) {
                    ((LockInfo) sKnown.valueAt(i)).children.add(lock);
                }
            }
        }
        return lock;
    }

    public static void installLock(Object lock, String label) {
        findOrCreateLockInfo(lock).label = label;
    }

    private static String lockToString(Object lock) {
        LockInfo info = (LockInfo) sKnown.get(lock);
        if (info != null) {
            return info.label;
        }
        return "0x" + Integer.toHexString(System.identityHashCode(lock));
    }

    public static void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        for (int i = 0; i < sKnown.size(); i++) {
            LockInfo info = (LockInfo) sKnown.valueAt(i);
            pw.println("Lock " + lockToString(sKnown.keyAt(i)) + ":");
            for (int j = 0; j < info.children.size(); j++) {
                pw.println("  Child " + lockToString(info.children.valueAt(j)));
            }
            pw.println();
        }
    }
}
