package android.test;

import com.google.android.collect.Maps;
import com.google.android.collect.Sets;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
@Deprecated
public class ClassPathPackageInfoSource {
    private static final String CLASS_EXTENSION = ".class";
    private static final ClassLoader CLASS_LOADER = null;
    private static String[] apkPaths;
    private final SimpleCache<String, ClassPathPackageInfo> cache;
    private ClassLoader classLoader;
    private final String[] classPath;
    private final Map<File, Set<String>> jarFiles;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.test.ClassPathPackageInfoSource.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.test.ClassPathPackageInfoSource.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.test.ClassPathPackageInfoSource.<clinit>():void");
    }

    ClassPathPackageInfoSource() {
        this.cache = new SimpleCache<String, ClassPathPackageInfo>() {
            protected ClassPathPackageInfo load(String pkgName) {
                return ClassPathPackageInfoSource.this.createPackageInfo(pkgName);
            }
        };
        this.jarFiles = Maps.newHashMap();
        this.classPath = getClassPath();
    }

    public static void setApkPaths(String[] apkPaths) {
        apkPaths = apkPaths;
    }

    public ClassPathPackageInfo getPackageInfo(String pkgName) {
        return (ClassPathPackageInfo) this.cache.get(pkgName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0042 A:{ExcHandler: java.lang.ClassNotFoundException (r3_0 'e' java.lang.Throwable), Splitter: B:7:0x0033} */
    /* JADX WARNING: Missing block: B:13:0x0042, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:14:0x0043, code:
            android.util.Log.w("ClassPathPackageInfoSource", "Cannot load class. Make sure it is in your apk. Class name: '" + r0 + "'. Message: " + r3.getMessage(), r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ClassPathPackageInfo createPackageInfo(String packageName) {
        Set<String> subpackageNames = new TreeSet();
        Set<String> classNames = new TreeSet();
        Set<Class<?>> topLevelClasses = Sets.newHashSet();
        findClasses(packageName, classNames, subpackageNames);
        for (String className : classNames) {
            if (!(className.endsWith(".R") || className.endsWith(".Manifest"))) {
                try {
                    topLevelClasses.add(Class.forName(className, false, this.classLoader != null ? this.classLoader : CLASS_LOADER));
                } catch (Throwable e) {
                }
            }
        }
        return new ClassPathPackageInfo(this, packageName, subpackageNames, topLevelClasses);
    }

    private void findClasses(String packageName, Set<String> classNames, Set<String> subpackageNames) {
        String pathPrefix = (packageName + '.').replace('.', '/');
        for (String entryName : this.classPath) {
            if (new File(entryName).exists()) {
                try {
                    if (entryName.endsWith(".apk")) {
                        findClassesInApk(entryName, packageName, classNames, subpackageNames);
                    } else {
                        for (String apkPath : apkPaths) {
                            scanForApkFiles(new File(apkPath), packageName, classNames, subpackageNames);
                        }
                    }
                } catch (IOException e) {
                    throw new AssertionError("Can't read classpath entry " + entryName + ": " + e.getMessage());
                }
            }
        }
    }

    private void scanForApkFiles(File source, String packageName, Set<String> classNames, Set<String> subpackageNames) throws IOException {
        if (source.getPath().endsWith(".apk")) {
            findClassesInApk(source.getPath(), packageName, classNames, subpackageNames);
            return;
        }
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                scanForApkFiles(file, packageName, classNames, subpackageNames);
            }
        }
    }

    private void findClassesInDirectory(File classDir, String packagePrefix, String pathPrefix, Set<String> classNames, Set<String> subpackageNames) throws IOException {
        File directory = new File(classDir, pathPrefix);
        if (directory.exists()) {
            for (File f : directory.listFiles()) {
                String name = f.getName();
                if (name.endsWith(CLASS_EXTENSION) && isToplevelClass(name)) {
                    classNames.add(packagePrefix + getClassName(name));
                } else if (f.isDirectory()) {
                    subpackageNames.add(packagePrefix + name);
                }
            }
        }
    }

    private void findClassesInJar(File jarFile, String pathPrefix, Set<String> classNames, Set<String> subpackageNames) throws IOException {
        Set<String> entryNames = getJarEntries(jarFile);
        if (entryNames.contains(pathPrefix)) {
            int prefixLength = pathPrefix.length();
            for (String entryName : entryNames) {
                if (entryName.startsWith(pathPrefix) && entryName.endsWith(CLASS_EXTENSION)) {
                    int index = entryName.indexOf(47, prefixLength);
                    if (index >= 0) {
                        subpackageNames.add(entryName.substring(0, index).replace('/', '.'));
                    } else if (isToplevelClass(entryName)) {
                        classNames.add(getClassName(entryName).replace('/', '.'));
                    }
                }
            }
        }
    }

    private void findClassesInApk(String apkPath, String packageName, Set<String> classNames, Set<String> subpackageNames) throws IOException {
        Throwable th;
        DexFile dexFile = null;
        try {
            DexFile dexFile2 = new DexFile(apkPath);
            try {
                Enumeration<String> apkClassNames = dexFile2.entries();
                while (apkClassNames.hasMoreElements()) {
                    String className = (String) apkClassNames.nextElement();
                    if (className.startsWith(packageName)) {
                        String subPackageName = packageName;
                        int lastPackageSeparator = className.lastIndexOf(46);
                        if (lastPackageSeparator > 0) {
                            subPackageName = className.substring(0, lastPackageSeparator);
                        }
                        if (subPackageName.length() > packageName.length()) {
                            subpackageNames.add(subPackageName);
                        } else if (isToplevelClass(className)) {
                            classNames.add(className);
                        }
                    }
                }
                if (dexFile2 != null) {
                }
            } catch (IOException e) {
                dexFile = dexFile2;
                if (dexFile == null) {
                }
            } catch (Throwable th2) {
                th = th2;
                dexFile = dexFile2;
                if (dexFile == null) {
                }
                throw th;
            }
        } catch (IOException e2) {
            if (dexFile == null) {
            }
        } catch (Throwable th3) {
            th = th3;
            if (dexFile == null) {
            }
            throw th;
        }
    }

    private Set<String> getJarEntries(File jarFile) throws IOException {
        Set<String> entryNames = (Set) this.jarFiles.get(jarFile);
        if (entryNames == null) {
            entryNames = Sets.newHashSet();
            Enumeration<? extends ZipEntry> entries = new ZipFile(jarFile).entries();
            while (entries.hasMoreElements()) {
                String entryName = ((ZipEntry) entries.nextElement()).getName();
                if (entryName.endsWith(CLASS_EXTENSION)) {
                    entryNames.add(entryName);
                    int lastIndex = entryName.lastIndexOf(47);
                    while (true) {
                        entryNames.add(entryName.substring(0, lastIndex + 1));
                        lastIndex = entryName.lastIndexOf(47, lastIndex - 1);
                        if (lastIndex <= 0) {
                            break;
                        }
                    }
                }
            }
            this.jarFiles.put(jarFile, entryNames);
        }
        return entryNames;
    }

    private static boolean isToplevelClass(String fileName) {
        return fileName.indexOf(36) < 0;
    }

    private static String getClassName(String className) {
        return className.substring(0, className.length() - CLASS_EXTENSION.length());
    }

    private static String[] getClassPath() {
        return System.getProperty("java.class.path").split(Pattern.quote(System.getProperty("path.separator", ":")));
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
