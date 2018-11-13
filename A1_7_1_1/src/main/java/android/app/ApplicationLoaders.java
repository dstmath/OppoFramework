package android.app;

import android.os.Trace;
import android.util.ArrayMap;
import com.android.internal.os.PathClassLoaderFactory;
import dalvik.system.PathClassLoader;

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
class ApplicationLoaders {
    private static final ApplicationLoaders gApplicationLoaders = null;
    private final ArrayMap<String, ClassLoader> mLoaders;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ApplicationLoaders.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ApplicationLoaders.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ApplicationLoaders.<clinit>():void");
    }

    private static native void setupVulkanLayerPath(ClassLoader classLoader, String str);

    ApplicationLoaders() {
        this.mLoaders = new ArrayMap();
    }

    public static ApplicationLoaders getDefault() {
        return gApplicationLoaders;
    }

    public ClassLoader getClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent) {
        ClassLoader baseParent = ClassLoader.getSystemClassLoader().getParent();
        synchronized (this.mLoaders) {
            if (parent == null) {
                parent = baseParent;
            }
            PathClassLoader pathClassloader;
            if (parent == baseParent) {
                ClassLoader loader = (ClassLoader) this.mLoaders.get(zip);
                if (loader != null) {
                    return loader;
                }
                Trace.traceBegin(64, zip);
                pathClassloader = PathClassLoaderFactory.createClassLoader(zip, librarySearchPath, libraryPermittedPath, parent, targetSdkVersion, isBundled);
                Trace.traceEnd(64);
                Trace.traceBegin(64, "setupVulkanLayerPath");
                setupVulkanLayerPath(pathClassloader, librarySearchPath);
                Trace.traceEnd(64);
                this.mLoaders.put(zip, pathClassloader);
                return pathClassloader;
            }
            Trace.traceBegin(64, zip);
            pathClassloader = new PathClassLoader(zip, parent);
            Trace.traceEnd(64);
            return pathClassloader;
        }
    }

    void addPath(ClassLoader classLoader, String dexPath) {
        if (classLoader instanceof PathClassLoader) {
            ((PathClassLoader) classLoader).addDexPath(dexPath);
            return;
        }
        throw new IllegalStateException("class loader is not a PathClassLoader");
    }
}
