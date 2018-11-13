package java.lang;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import libcore.util.BasicLruCache;
import libcore.util.EmptyArray;

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
public abstract class Enum<E extends Enum<E>> implements Comparable<E>, Serializable {
    private static final BasicLruCache<Class<? extends Enum>, Object[]> sharedConstantsCache = null;
    private final String name;
    private final int ordinal;

    /* renamed from: java.lang.Enum$1 */
    static class AnonymousClass1 extends BasicLruCache<Class<? extends Enum>, Object[]> {
        AnonymousClass1(int $anonymous0) {
            super($anonymous0);
        }

        protected Object[] create(Class<? extends Enum> enumType) {
            if (!enumType.isEnum()) {
                return null;
            }
            try {
                Method method = enumType.getDeclaredMethod("values", EmptyArray.CLASS);
                method.setAccessible(true);
                return (Object[]) method.invoke((Object[]) null, new Object[0]);
            } catch (NoSuchMethodException impossible) {
                throw new AssertionError("impossible", impossible);
            } catch (IllegalAccessException impossible2) {
                throw new AssertionError("impossible", impossible2);
            } catch (InvocationTargetException impossible3) {
                throw new AssertionError("impossible", impossible3);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.Enum.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.Enum.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Enum.<clinit>():void");
    }

    public final String name() {
        return this.name;
    }

    public final int ordinal() {
        return this.ordinal;
    }

    protected Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    public String toString() {
        return this.name;
    }

    public final boolean equals(Object other) {
        return this == other;
    }

    public final int hashCode() {
        return super.hashCode();
    }

    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final int compareTo(E o) {
        E other = o;
        if (getClass() == o.getClass() || getDeclaringClass() == o.getDeclaringClass()) {
            return this.ordinal - o.ordinal;
        }
        throw new ClassCastException();
    }

    public final Class<E> getDeclaringClass() {
        Class clazz = getClass();
        Class zuper = clazz.getSuperclass();
        return zuper == Enum.class ? clazz : zuper;
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if (enumType == null) {
            throw new NullPointerException("enumType == null");
        } else if (name == null) {
            throw new NullPointerException("Name is null");
        } else {
            T[] values = getSharedConstants(enumType);
            T result = null;
            if (values != null) {
                for (T value : values) {
                    if (name.equals(value.name())) {
                        result = value;
                    }
                }
                if (result != null) {
                    return result;
                }
                throw new IllegalArgumentException("No enum constant " + enumType.getCanonicalName() + "." + name);
            }
            throw new IllegalArgumentException(enumType.toString() + " is not an enum type.");
        }
    }

    public static <T extends Enum<T>> T[] getSharedConstants(Class<T> enumType) {
        return (Enum[]) sharedConstantsCache.get(enumType);
    }

    protected final void finalize() {
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("can't deserialize enum");
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("can't deserialize enum");
    }
}
