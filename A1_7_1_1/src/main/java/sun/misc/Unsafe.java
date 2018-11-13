package sun.misc;

import dalvik.system.VMStack;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
public final class Unsafe {
    public static final int INVALID_FIELD_OFFSET = -1;
    private static final Unsafe THE_ONE = null;
    private static final Unsafe theUnsafe = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.misc.Unsafe.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.misc.Unsafe.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.Unsafe.<clinit>():void");
    }

    private static native int getArrayBaseOffsetForComponentType(Class cls);

    private static native int getArrayIndexScaleForComponentType(Class cls);

    public native int addressSize();

    public native Object allocateInstance(Class<?> cls);

    public native long allocateMemory(long j);

    public native boolean compareAndSwapInt(Object obj, long j, int i, int i2);

    public native boolean compareAndSwapLong(Object obj, long j, long j2, long j3);

    public native boolean compareAndSwapObject(Object obj, long j, Object obj2, Object obj3);

    public native void copyMemory(long j, long j2, long j3);

    public native void copyMemoryFromPrimitiveArray(Object obj, long j, long j2, long j3);

    public native void copyMemoryToPrimitiveArray(long j, Object obj, long j2, long j3);

    public native void freeMemory(long j);

    public native void fullFence();

    public native boolean getBoolean(Object obj, long j);

    public native byte getByte(long j);

    public native byte getByte(Object obj, long j);

    public native char getChar(long j);

    public native char getChar(Object obj, long j);

    public native double getDouble(long j);

    public native double getDouble(Object obj, long j);

    public native float getFloat(long j);

    public native float getFloat(Object obj, long j);

    public native int getInt(long j);

    public native int getInt(Object obj, long j);

    public native int getIntVolatile(Object obj, long j);

    public native long getLong(long j);

    public native long getLong(Object obj, long j);

    public native long getLongVolatile(Object obj, long j);

    public native Object getObject(Object obj, long j);

    public native Object getObjectVolatile(Object obj, long j);

    public native short getShort(long j);

    public native short getShort(Object obj, long j);

    public native void loadFence();

    public native int pageSize();

    public native void putBoolean(Object obj, long j, boolean z);

    public native void putByte(long j, byte b);

    public native void putByte(Object obj, long j, byte b);

    public native void putChar(long j, char c);

    public native void putChar(Object obj, long j, char c);

    public native void putDouble(long j, double d);

    public native void putDouble(Object obj, long j, double d);

    public native void putFloat(long j, float f);

    public native void putFloat(Object obj, long j, float f);

    public native void putInt(long j, int i);

    public native void putInt(Object obj, long j, int i);

    public native void putIntVolatile(Object obj, long j, int i);

    public native void putLong(long j, long j2);

    public native void putLong(Object obj, long j, long j2);

    public native void putLongVolatile(Object obj, long j, long j2);

    public native void putObject(Object obj, long j, Object obj2);

    public native void putObjectVolatile(Object obj, long j, Object obj2);

    public native void putOrderedInt(Object obj, long j, int i);

    public native void putOrderedLong(Object obj, long j, long j2);

    public native void putOrderedObject(Object obj, long j, Object obj2);

    public native void putShort(long j, short s);

    public native void putShort(Object obj, long j, short s);

    public native void setMemory(long j, long j2, byte b);

    public native void storeFence();

    private Unsafe() {
    }

    public static Unsafe getUnsafe() {
        ClassLoader calling = VMStack.getCallingClassLoader();
        if (calling == null || calling == Unsafe.class.getClassLoader()) {
            return THE_ONE;
        }
        throw new SecurityException("Unsafe access denied");
    }

    public long objectFieldOffset(Field field) {
        if (!Modifier.isStatic(field.getModifiers())) {
            return (long) field.getOffset();
        }
        throw new IllegalArgumentException("valid for instance fields only");
    }

    public int arrayBaseOffset(Class clazz) {
        Class<?> component = clazz.getComponentType();
        if (component != null) {
            return getArrayBaseOffsetForComponentType(component);
        }
        throw new IllegalArgumentException("Valid for array classes only: " + clazz);
    }

    public int arrayIndexScale(Class clazz) {
        Class<?> component = clazz.getComponentType();
        if (component != null) {
            return getArrayIndexScaleForComponentType(component);
        }
        throw new IllegalArgumentException("Valid for array classes only: " + clazz);
    }

    public void park(boolean absolute, long time) {
        if (absolute) {
            Thread.currentThread().parkUntil$(time);
        } else {
            Thread.currentThread().parkFor$(time);
        }
    }

    public void unpark(Object obj) {
        if (obj instanceof Thread) {
            ((Thread) obj).unpark$();
            return;
        }
        throw new IllegalArgumentException("valid for Threads only");
    }

    public final int getAndAddInt(Object o, long offset, int delta) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!compareAndSwapInt(o, offset, v, v + delta));
        return v;
    }

    public final long getAndAddLong(Object o, long offset, long delta) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!compareAndSwapLong(o, offset, v, v + delta));
        return v;
    }

    public final int getAndSetInt(Object o, long offset, int newValue) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!compareAndSwapInt(o, offset, v, newValue));
        return v;
    }

    public final long getAndSetLong(Object o, long offset, long newValue) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!compareAndSwapLong(o, offset, v, newValue));
        return v;
    }

    public final Object getAndSetObject(Object o, long offset, Object newValue) {
        Object v;
        do {
            v = getObjectVolatile(o, offset);
        } while (!compareAndSwapObject(o, offset, v, newValue));
        return v;
    }
}
