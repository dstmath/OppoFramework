package java.util.concurrent.atomic;

import java.io.Serializable;
import sun.misc.Unsafe;

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
public class AtomicBoolean implements Serializable {
    private static final Unsafe U = null;
    private static final long VALUE = 0;
    private static final long serialVersionUID = 4654671469794556979L;
    private volatile int value;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.atomic.AtomicBoolean.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.atomic.AtomicBoolean.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.AtomicBoolean.<clinit>():void");
    }

    public AtomicBoolean(boolean initialValue) {
        this.value = initialValue ? 1 : 0;
    }

    public final boolean get() {
        return this.value != 0;
    }

    public final boolean compareAndSet(boolean expect, boolean update) {
        int i;
        int i2 = 1;
        Unsafe unsafe = U;
        long j = VALUE;
        if (expect) {
            i = 1;
        } else {
            i = 0;
        }
        if (!update) {
            i2 = 0;
        }
        return unsafe.compareAndSwapInt(this, j, i, i2);
    }

    public boolean weakCompareAndSet(boolean expect, boolean update) {
        int i;
        int i2 = 1;
        Unsafe unsafe = U;
        long j = VALUE;
        if (expect) {
            i = 1;
        } else {
            i = 0;
        }
        if (!update) {
            i2 = 0;
        }
        return unsafe.compareAndSwapInt(this, j, i, i2);
    }

    public final void set(boolean newValue) {
        this.value = newValue ? 1 : 0;
    }

    public final void lazySet(boolean newValue) {
        U.putOrderedInt(this, VALUE, newValue ? 1 : 0);
    }

    public final boolean getAndSet(boolean newValue) {
        boolean prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, newValue));
        return prev;
    }

    public String toString() {
        return Boolean.toString(get());
    }
}
