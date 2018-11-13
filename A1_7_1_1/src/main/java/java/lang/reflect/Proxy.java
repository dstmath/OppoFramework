package java.lang.reflect;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import libcore.util.EmptyArray;
import sun.reflect.CallerSensitive;

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
public class Proxy implements Serializable {
    private static final Comparator<Method> ORDER_BY_SIGNATURE_AND_SUBTYPE = null;
    private static final Class[] constructorParams = null;
    private static Map<ClassLoader, Map<List<String>, Object>> loaderToCache = null;
    private static long nextUniqueNumber = 0;
    private static Object nextUniqueNumberLock = null;
    private static Object pendingGenerationMarker = null;
    private static final String proxyClassNamePrefix = "$Proxy";
    private static Map<Class<?>, Void> proxyClasses = null;
    private static final long serialVersionUID = -2222568056686623797L;
    protected InvocationHandler h;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.reflect.Proxy.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.reflect.Proxy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.reflect.Proxy.<clinit>():void");
    }

    private static native Class<?> generateProxy(String str, Class<?>[] clsArr, ClassLoader classLoader, Method[] methodArr, Class<?>[][] clsArr2);

    private Proxy() {
    }

    protected Proxy(InvocationHandler h) {
        this.h = h;
    }

    @CallerSensitive
    public static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces) throws IllegalArgumentException {
        return getProxyClass0(loader, interfaces);
    }

    /* JADX WARNING: Missing block: B:52:?, code:
            r4.put(r15, pendingGenerationMarker);
     */
    /* JADX WARNING: Missing block: B:54:0x0115, code:
            r25 = null;
            r10 = 0;
     */
    /* JADX WARNING: Missing block: B:57:0x011f, code:
            if (r10 >= r33.length) goto L_0x018b;
     */
    /* JADX WARNING: Missing block: B:59:0x012b, code:
            if (java.lang.reflect.Modifier.isPublic(r33[r10].getModifiers()) != false) goto L_0x014c;
     */
    /* JADX WARNING: Missing block: B:60:0x012d, code:
            r19 = r33[r10].getName();
            r18 = r19.lastIndexOf(46);
     */
    /* JADX WARNING: Missing block: B:61:0x0143, code:
            if (r18 != -1) goto L_0x0152;
     */
    /* JADX WARNING: Missing block: B:62:0x0145, code:
            r22 = "";
     */
    /* JADX WARNING: Missing block: B:63:0x0148, code:
            if (r25 != null) goto L_0x0161;
     */
    /* JADX WARNING: Missing block: B:64:0x014a, code:
            r25 = r22;
     */
    /* JADX WARNING: Missing block: B:65:0x014c, code:
            r10 = r10 + 1;
     */
    /* JADX WARNING: Missing block: B:71:?, code:
            r22 = r19.substring(0, r18 + 1);
     */
    /* JADX WARNING: Missing block: B:73:0x0169, code:
            if (r22.equals(r25) != false) goto L_0x014c;
     */
    /* JADX WARNING: Missing block: B:75:0x0173, code:
            throw new java.lang.IllegalArgumentException("non-public interfaces from different packages");
     */
    /* JADX WARNING: Missing block: B:77:0x0175, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:78:0x0176, code:
            if (r23 != null) goto L_0x0178;
     */
    /* JADX WARNING: Missing block: B:80:?, code:
            r4.put(r15, new java.lang.ref.WeakReference(r23));
     */
    /* JADX WARNING: Missing block: B:81:0x0186, code:
            r4.notifyAll();
     */
    /* JADX WARNING: Missing block: B:84:0x018b, code:
            if (r25 != null) goto L_0x0190;
     */
    /* JADX WARNING: Missing block: B:86:?, code:
            r25 = "";
     */
    /* JADX WARNING: Missing block: B:87:0x0190, code:
            r16 = getMethods(r33);
            java.util.Collections.sort(r16, ORDER_BY_SIGNATURE_AND_SUBTYPE);
            validateReturnTypes(r16);
            r7 = deduplicateAndGetExceptions(r16);
            r17 = (java.lang.reflect.Method[]) r16.toArray(new java.lang.reflect.Method[r16.size()]);
            r8 = (java.lang.Class[][]) r7.toArray(new java.lang.Class[r7.size()][]);
            r28 = nextUniqueNumberLock;
     */
    /* JADX WARNING: Missing block: B:88:0x01cc, code:
            monitor-enter(r28);
     */
    /* JADX WARNING: Missing block: B:90:?, code:
            r20 = nextUniqueNumber;
            nextUniqueNumber = 1 + r20;
     */
    /* JADX WARNING: Missing block: B:92:?, code:
            monitor-exit(r28);
     */
    /* JADX WARNING: Missing block: B:93:0x01d6, code:
            r23 = generateProxy(r25 + proxyClassNamePrefix + r20, r33, r32, r17, r8);
            proxyClasses.put(r23, null);
     */
    /* JADX WARNING: Missing block: B:94:0x020f, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:95:0x0210, code:
            if (r23 == null) goto L_0x0228;
     */
    /* JADX WARNING: Missing block: B:97:?, code:
            r4.put(r15, new java.lang.ref.WeakReference(r23));
     */
    /* JADX WARNING: Missing block: B:98:0x0220, code:
            r4.notifyAll();
     */
    /* JADX WARNING: Missing block: B:99:0x0223, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:100:0x0224, code:
            return r23;
     */
    /* JADX WARNING: Missing block: B:106:?, code:
            r4.remove(r15);
     */
    /* JADX WARNING: Missing block: B:111:?, code:
            r4.remove(r15);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Class<?> getProxyClass0(ClassLoader loader, Class<?>... interfaces) {
        if (interfaces.length <= 65535) {
            Map<List<String>, Object> cache;
            Class<?> proxyClass = null;
            String[] interfaceNames = new String[interfaces.length];
            Set<Class<?>> interfaceSet = new HashSet();
            int i = 0;
            while (i < interfaces.length) {
                String interfaceName = interfaces[i].getName();
                Class<?> interfaceClass = null;
                try {
                    interfaceClass = Class.forName(interfaceName, false, loader);
                } catch (ClassNotFoundException e) {
                }
                if (interfaceClass != interfaces[i]) {
                    throw new IllegalArgumentException(interfaces[i] + " is not visible from class loader");
                } else if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
                } else if (interfaceSet.contains(interfaceClass)) {
                    throw new IllegalArgumentException("repeated interface: " + interfaceClass.getName());
                } else {
                    interfaceSet.add(interfaceClass);
                    interfaceNames[i] = interfaceName;
                    i++;
                }
            }
            List<String> key = Arrays.asList(interfaceNames);
            synchronized (loaderToCache) {
                cache = (Map) loaderToCache.get(loader);
                if (cache == null) {
                    cache = new HashMap();
                    loaderToCache.put(loader, cache);
                }
            }
            synchronized (cache) {
                while (true) {
                    Object value = cache.get(key);
                    if (value instanceof Reference) {
                        proxyClass = (Class) ((Reference) value).get();
                    }
                    if (proxyClass == null) {
                        if (value != pendingGenerationMarker) {
                            break;
                        }
                        try {
                            cache.wait();
                        } catch (InterruptedException e2) {
                        }
                    } else {
                        return proxyClass;
                    }
                }
            }
        }
        throw new IllegalArgumentException("interface limit exceeded");
    }

    private static List<Class<?>[]> deduplicateAndGetExceptions(List<Method> methods) {
        List<Class<?>[]> exceptions = new ArrayList(methods.size());
        int i = 0;
        while (i < methods.size()) {
            Method method = (Method) methods.get(i);
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            if (i <= 0 || Method.ORDER_BY_SIGNATURE.compare(method, (Method) methods.get(i - 1)) != 0) {
                exceptions.add(exceptionTypes);
                i++;
            } else {
                exceptions.set(i - 1, intersectExceptions((Class[]) exceptions.get(i - 1), exceptionTypes));
                methods.remove(i);
            }
        }
        return exceptions;
    }

    private static Class<?>[] intersectExceptions(Class<?>[] aExceptions, Class<?>[] bExceptions) {
        if (aExceptions.length == 0 || bExceptions.length == 0) {
            return EmptyArray.CLASS;
        }
        if (Arrays.equals((Object[]) aExceptions, (Object[]) bExceptions)) {
            return aExceptions;
        }
        Set<Class<?>> intersection = new HashSet();
        for (Class<?> a : aExceptions) {
            for (Class<?> b : bExceptions) {
                if (a.isAssignableFrom(b)) {
                    intersection.add(b);
                } else if (b.isAssignableFrom(a)) {
                    intersection.add(a);
                }
            }
        }
        return (Class[]) intersection.toArray(new Class[intersection.size()]);
    }

    private static void validateReturnTypes(List<Method> methods) {
        Method vs = null;
        for (Object method : methods) {
            if (vs == null || !vs.equalNameAndParameters(method)) {
                vs = method;
            } else {
                Class<?> returnType = method.getReturnType();
                Class<?> vsReturnType = vs.getReturnType();
                if (!returnType.isInterface() || !vsReturnType.isInterface()) {
                    if (vsReturnType.isAssignableFrom(returnType)) {
                        vs = method;
                    } else if (!returnType.isAssignableFrom(vsReturnType)) {
                        throw new IllegalArgumentException("proxied interface methods have incompatible return types:\n  " + vs + "\n  " + method);
                    }
                }
            }
        }
    }

    private static List<Method> getMethods(Class<?>[] interfaces) {
        List<Method> result = new ArrayList();
        try {
            Class[] clsArr = new Class[1];
            clsArr[0] = Object.class;
            result.add(Object.class.getMethod("equals", clsArr));
            result.add(Object.class.getMethod("hashCode", EmptyArray.CLASS));
            result.add(Object.class.getMethod("toString", EmptyArray.CLASS));
            getMethodsRecursive(interfaces, result);
            return result;
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        }
    }

    private static void getMethodsRecursive(Class<?>[] interfaces, List<Method> methods) {
        for (Class<?> i : interfaces) {
            getMethodsRecursive(i.getInterfaces(), methods);
            Collections.addAll(methods, i.getDeclaredMethods());
        }
    }

    @CallerSensitive
    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException {
        if (h == null) {
            throw new NullPointerException();
        }
        try {
            return newInstance(getProxyClass0(loader, interfaces).getConstructor(constructorParams), h);
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0021 A:{ExcHandler: java.lang.IllegalAccessException (r0_0 'e' java.lang.ReflectiveOperationException), Splitter: B:1:0x0001} */
    /* JADX WARNING: Missing block: B:11:0x0021, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:13:0x002b, code:
            throw new java.lang.InternalError(r0.toString());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Object newInstance(Constructor<?> cons, InvocationHandler h) {
        try {
            Object[] objArr = new Object[1];
            objArr[0] = h;
            return cons.newInstance(objArr);
        } catch (ReflectiveOperationException e) {
        } catch (InvocationTargetException e2) {
            Throwable t = e2.getCause();
            if (t instanceof RuntimeException) {
                throw ((RuntimeException) t);
            }
            throw new InternalError(t.toString());
        }
    }

    public static boolean isProxyClass(Class<?> cl) {
        if (cl != null) {
            return proxyClasses.containsKey(cl);
        }
        throw new NullPointerException();
    }

    public static InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        if (proxy instanceof Proxy) {
            return ((Proxy) proxy).h;
        }
        throw new IllegalArgumentException("not a proxy instance");
    }

    private static Object invoke(Proxy proxy, Method method, Object[] args) throws Throwable {
        return proxy.h.invoke(proxy, method, args);
    }

    private static void reserved1() {
    }

    private static void reserved2() {
    }
}
