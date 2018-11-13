package java.lang.reflect;

import com.android.dex.Dex;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import libcore.reflect.Types;

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
public final class Method extends AbstractMethod implements GenericDeclaration, Member {
    public static final Comparator<Method> ORDER_BY_SIGNATURE = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.reflect.Method.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.reflect.Method.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.reflect.Method.<clinit>():void");
    }

    private native <A extends Annotation> A getAnnotationNative(Class<A> cls);

    private native Annotation[][] getParameterAnnotationsNative();

    public native Object getDefaultValue();

    public native Class<?>[] getExceptionTypes();

    public native Object invoke(Object obj, Object... objArr) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    private Method() {
    }

    public Class<?> getDeclaringClass() {
        return super.getDeclaringClass();
    }

    public String getName() {
        Dex dex = this.declaringClassOfOverriddenMethod.getDex();
        return this.declaringClassOfOverriddenMethod.getDexCacheString(dex, dex.nameIndexFromMethodIndex(this.dexMethodIndex));
    }

    public int getModifiers() {
        return super.getModifiers();
    }

    public TypeVariable<Method>[] getTypeParameters() {
        return (TypeVariable[]) getMethodOrConstructorGenericInfo().formalTypeParameters.clone();
    }

    public Class<?> getReturnType() {
        Dex dex = this.declaringClassOfOverriddenMethod.getDex();
        return this.declaringClassOfOverriddenMethod.getDexCacheType(dex, dex.returnTypeIndexFromMethodIndex(this.dexMethodIndex));
    }

    public Type getGenericReturnType() {
        return Types.getType(getMethodOrConstructorGenericInfo().genericReturnType);
    }

    public Class<?>[] getParameterTypes() {
        return super.getParameterTypes();
    }

    public Type[] getGenericParameterTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfo().genericParameterTypes, false);
    }

    public Type[] getGenericExceptionTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfo().genericExceptionTypes, false);
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof Method)) {
            Method other = (Method) obj;
            if (getDeclaringClass() != other.getDeclaringClass() || getName() != other.getName() || !getReturnType().equals(other.getReturnType())) {
                return false;
            }
            Class<?>[] params1 = getParameterTypes();
            Class<?>[] params2 = other.getParameterTypes();
            if (params1.length == params2.length) {
                for (int i = 0; i < params1.length; i++) {
                    if (params1[i] != params2[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.methodModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(' ');
            }
            sb.append(Field.getTypeName(getReturnType())).append(' ');
            sb.append(Field.getTypeName(getDeclaringClass())).append('.');
            sb.append(getName()).append('(');
            Class<?>[] params = getParameterTypes();
            for (int j = 0; j < params.length; j++) {
                sb.append(Field.getTypeName(params[j]));
                if (j < params.length - 1) {
                    sb.append(',');
                }
            }
            sb.append(')');
            Class<?>[] exceptions = getExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append(exceptions[k].getName());
                    if (k < exceptions.length - 1) {
                        sb.append(',');
                    }
                }
            }
            return sb.toString();
        } catch (Object e) {
            return "<" + e + ">";
        }
    }

    public String toGenericString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.methodModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(' ');
            }
            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for (TypeVariable<?> typeparm : typeparms) {
                    if (!first) {
                        sb.append(',');
                    }
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }
            Type genRetType = getGenericReturnType();
            sb.append(genRetType instanceof Class ? Field.getTypeName((Class) genRetType) : genRetType.toString()).append(' ');
            sb.append(Field.getTypeName(getDeclaringClass())).append('.');
            sb.append(getName()).append('(');
            Type[] params = getGenericParameterTypes();
            int j = 0;
            while (j < params.length) {
                String param;
                if (params[j] instanceof Class) {
                    param = Field.getTypeName((Class) params[j]);
                } else {
                    param = params[j].toString();
                }
                if (isVarArgs() && j == params.length - 1) {
                    param = param.replaceFirst("\\[\\]$", "...");
                }
                sb.append(param);
                if (j < params.length - 1) {
                    sb.append(',');
                }
                j++;
            }
            sb.append(')');
            Type[] exceptions = getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    String name;
                    if (exceptions[k] instanceof Class) {
                        name = ((Class) exceptions[k]).getName();
                    } else {
                        name = exceptions[k].toString();
                    }
                    sb.append(name);
                    if (k < exceptions.length - 1) {
                        sb.append(',');
                    }
                }
            }
            return sb.toString();
        } catch (Object e) {
            return "<" + e + ">";
        }
    }

    public boolean isBridge() {
        return (getModifiers() & 64) != 0;
    }

    public boolean isVarArgs() {
        return (getModifiers() & 128) != 0;
    }

    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        if (annotationType != null) {
            return getAnnotationNative(annotationType);
        }
        throw new NullPointerException("annotationType == null");
    }

    public Annotation[][] getParameterAnnotations() {
        Annotation[][] parameterAnnotations = getParameterAnnotationsNative();
        if (parameterAnnotations != null) {
            return parameterAnnotations;
        }
        int[] iArr = new int[2];
        iArr[0] = getParameterTypes().length;
        iArr[1] = 0;
        return (Annotation[][]) Array.newInstance(Annotation.class, iArr);
    }

    String getSignature() {
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (Class<?> parameterType : getParameterTypes()) {
            result.append(Types.getSignature(parameterType));
        }
        result.append(')');
        result.append(Types.getSignature(getReturnType()));
        return result.toString();
    }

    boolean equalNameAndParameters(Method m) {
        return getName().equals(m.getName()) ? equalMethodParameters(m.getParameterTypes()) : false;
    }

    public boolean isDefault() {
        return super.isDefault();
    }
}
