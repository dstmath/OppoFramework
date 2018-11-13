package java.lang;

import dalvik.system.PathClassLoader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.misc.CompoundEnumeration;
import sun.reflect.CallerSensitive;

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
public abstract class ClassLoader {
    private transient long allocator;
    private transient long classTable;
    private final HashMap<String, Package> packages;
    private final ClassLoader parent;
    public final Map<List<Class<?>>, Class<?>> proxyCache;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static class SystemClassLoader {
        public static ClassLoader loader;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.ClassLoader.SystemClassLoader.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.ClassLoader.SystemClassLoader.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.ClassLoader.SystemClassLoader.<clinit>():void");
        }

        private SystemClassLoader() {
        }
    }

    private static ClassLoader createSystemClassLoader() {
        return new PathClassLoader(System.getProperty("java.class.path", "."), System.getProperty("java.library.path", ""), BootClassLoader.getInstance());
    }

    private static Void checkCreateClassLoader() {
        return null;
    }

    private ClassLoader(Void unused, ClassLoader parent) {
        this.proxyCache = new HashMap();
        this.packages = new HashMap();
        this.parent = parent;
    }

    protected ClassLoader(ClassLoader parent) {
        this(checkCreateClassLoader(), parent);
    }

    protected ClassLoader() {
        this(checkCreateClassLoader(), getSystemClassLoader());
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        long t0 = System.nanoTime();
        try {
            if (this.parent != null) {
                c = this.parent.loadClass(name, false);
            } else {
                c = findBootstrapClassOrNull(name);
            }
        } catch (ClassNotFoundException e) {
        }
        if (c != null) {
            return c;
        }
        long t1 = System.nanoTime();
        return findClass(name);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    @Deprecated
    protected final Class<?> defineClass(byte[] b, int off, int len) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    protected final Class<?> defineClass(String name, byte[] b, int off, int len) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    protected final Class<?> defineClass(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    protected final Class<?> defineClass(String name, ByteBuffer b, ProtectionDomain protectionDomain) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    protected final void resolveClass(Class<?> cls) {
    }

    protected final Class<?> findSystemClass(String name) throws ClassNotFoundException {
        return Class.forName(name, false, getSystemClassLoader());
    }

    private Class findBootstrapClassOrNull(String name) {
        return null;
    }

    protected final Class<?> findLoadedClass(String name) {
        ClassLoader loader;
        if (this == BootClassLoader.getInstance()) {
            loader = null;
        } else {
            loader = this;
        }
        return VMClassLoader.findLoadedClass(loader, name);
    }

    protected final void setSigners(Class<?> cls, Object[] signers) {
    }

    public URL getResource(String name) {
        URL url;
        if (this.parent != null) {
            url = this.parent.getResource(name);
        } else {
            url = getBootstrapResource(name);
        }
        if (url == null) {
            return findResource(name);
        }
        return url;
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration[] tmp = new Enumeration[2];
        if (this.parent != null) {
            tmp[0] = this.parent.getResources(name);
        } else {
            tmp[0] = getBootstrapResources(name);
        }
        tmp[1] = findResources(name);
        return new CompoundEnumeration(tmp);
    }

    protected URL findResource(String name) {
        return null;
    }

    protected Enumeration<URL> findResources(String name) throws IOException {
        return Collections.emptyEnumeration();
    }

    @CallerSensitive
    protected static boolean registerAsParallelCapable() {
        return true;
    }

    public static URL getSystemResource(String name) {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResource(name);
        }
        return system.getResource(name);
    }

    public static Enumeration<URL> getSystemResources(String name) throws IOException {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResources(name);
        }
        return system.getResources(name);
    }

    private static URL getBootstrapResource(String name) {
        return null;
    }

    private static Enumeration<URL> getBootstrapResources(String name) throws IOException {
        return null;
    }

    public InputStream getResourceAsStream(String name) {
        InputStream inputStream = null;
        URL url = getResource(name);
        if (url != null) {
            try {
                inputStream = url.openStream();
            } catch (IOException e) {
                return inputStream;
            }
        }
        return inputStream;
    }

    public static InputStream getSystemResourceAsStream(String name) {
        InputStream inputStream = null;
        URL url = getSystemResource(name);
        if (url != null) {
            try {
                inputStream = url.openStream();
            } catch (IOException e) {
                return inputStream;
            }
        }
        return inputStream;
    }

    @CallerSensitive
    public final ClassLoader getParent() {
        return this.parent;
    }

    @CallerSensitive
    public static ClassLoader getSystemClassLoader() {
        return SystemClassLoader.loader;
    }

    protected Package definePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException {
        Package pkg;
        synchronized (this.packages) {
            if (((Package) this.packages.get(name)) != null) {
                throw new IllegalArgumentException(name);
            }
            pkg = new Package(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase, this);
            this.packages.put(name, pkg);
        }
        return pkg;
    }

    protected Package getPackage(String name) {
        Package pkg;
        synchronized (this.packages) {
            pkg = (Package) this.packages.get(name);
        }
        return pkg;
    }

    protected Package[] getPackages() {
        Map<String, Package> map;
        synchronized (this.packages) {
            map = new HashMap(this.packages);
        }
        return (Package[]) map.values().toArray(new Package[map.size()]);
    }

    protected String findLibrary(String libname) {
        return null;
    }

    public void setDefaultAssertionStatus(boolean enabled) {
    }

    public void setPackageAssertionStatus(String packageName, boolean enabled) {
    }

    public void setClassAssertionStatus(String className, boolean enabled) {
    }

    public void clearAssertionStatus() {
    }
}
