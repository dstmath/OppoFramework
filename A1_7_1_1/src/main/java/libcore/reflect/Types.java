package libcore.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import libcore.util.EmptyArray;

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
public final class Types {
    private static final Map<Class<?>, String> PRIMITIVE_TO_SIGNATURE = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: libcore.reflect.Types.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: libcore.reflect.Types.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.reflect.Types.<clinit>():void");
    }

    private Types() {
    }

    public static Type[] getTypeArray(ListOfTypes types, boolean clone) {
        if (types.length() == 0) {
            return EmptyArray.TYPE;
        }
        Type[] result = types.getResolvedTypes();
        return clone ? (Type[]) result.clone() : result;
    }

    public static Type getType(Type type) {
        if (type instanceof ParameterizedTypeImpl) {
            return ((ParameterizedTypeImpl) type).getResolvedType();
        }
        return type;
    }

    public static String getSignature(Class<?> clazz) {
        String primitiveSignature = (String) PRIMITIVE_TO_SIGNATURE.get(clazz);
        if (primitiveSignature != null) {
            return primitiveSignature;
        }
        if (clazz.isArray()) {
            return "[" + getSignature(clazz.getComponentType());
        }
        return "L" + clazz.getName() + ";";
    }

    public static String toString(Class<?>[] types) {
        if (types.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        appendTypeName(result, types[0]);
        for (int i = 1; i < types.length; i++) {
            result.append(',');
            appendTypeName(result, types[i]);
        }
        return result.toString();
    }

    public static void appendTypeName(StringBuilder out, Class<?> c) {
        int dimensions = 0;
        while (c.isArray()) {
            c = c.getComponentType();
            dimensions++;
        }
        out.append(c.getName());
        for (int d = 0; d < dimensions; d++) {
            out.append("[]");
        }
    }

    public static void appendArrayGenericType(StringBuilder out, Type[] types) {
        if (types.length != 0) {
            appendGenericType(out, types[0]);
            for (int i = 1; i < types.length; i++) {
                out.append(',');
                appendGenericType(out, types[i]);
            }
        }
    }

    public static void appendGenericType(StringBuilder out, Type type) {
        if (type instanceof TypeVariable) {
            out.append(((TypeVariable) type).getName());
        } else if (type instanceof ParameterizedType) {
            out.append(type.toString());
        } else if (type instanceof GenericArrayType) {
            appendGenericType(out, ((GenericArrayType) type).getGenericComponentType());
            out.append("[]");
        } else if (type instanceof Class) {
            Class c = (Class) type;
            if (c.isArray()) {
                String[] as = c.getName().split("\\[");
                int len = as.length - 1;
                if (as[len].length() > 1) {
                    out.append(as[len].substring(1, as[len].length() - 1));
                } else {
                    char ch = as[len].charAt(0);
                    if (ch == 'I') {
                        out.append("int");
                    } else if (ch == 'B') {
                        out.append("byte");
                    } else if (ch == 'J') {
                        out.append("long");
                    } else if (ch == 'F') {
                        out.append("float");
                    } else if (ch == 'D') {
                        out.append("double");
                    } else if (ch == 'S') {
                        out.append("short");
                    } else if (ch == 'C') {
                        out.append("char");
                    } else if (ch == 'Z') {
                        out.append("boolean");
                    } else if (ch == 'V') {
                        out.append("void");
                    }
                }
                for (int i = 0; i < len; i++) {
                    out.append("[]");
                }
                return;
            }
            out.append(c.getName());
        }
    }
}
