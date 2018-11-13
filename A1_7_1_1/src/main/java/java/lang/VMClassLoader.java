package java.lang;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import libcore.io.ClassPathURLStreamHandler;

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
class VMClassLoader {
    private static final ClassPathURLStreamHandler[] bootClassPathUrlHandlers = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.lang.VMClassLoader.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.lang.VMClassLoader.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.VMClassLoader.<clinit>():void");
    }

    static native Class findLoadedClass(ClassLoader classLoader, String str);

    private static native String[] getBootClassPathEntries();

    VMClassLoader() {
    }

    private static ClassPathURLStreamHandler[] createBootClassPathUrlHandlers() {
        String[] bootClassPathEntries = getBootClassPathEntries();
        ArrayList<String> zipFileUris = new ArrayList(bootClassPathEntries.length);
        ArrayList<URLStreamHandler> urlStreamHandlers = new ArrayList(bootClassPathEntries.length);
        for (String bootClassPathEntry : bootClassPathEntries) {
            try {
                String entryUri = new File(bootClassPathEntry).toURI().toString();
                URLStreamHandler urlStreamHandler = new ClassPathURLStreamHandler(bootClassPathEntry);
                zipFileUris.add(entryUri);
                urlStreamHandlers.add(urlStreamHandler);
            } catch (IOException e) {
                System.logE("Unable to open boot classpath entry: " + bootClassPathEntry, e);
            }
        }
        return (ClassPathURLStreamHandler[]) urlStreamHandlers.toArray(new ClassPathURLStreamHandler[urlStreamHandlers.size()]);
    }

    static URL getResource(String name) {
        for (ClassPathURLStreamHandler urlHandler : bootClassPathUrlHandlers) {
            URL url = urlHandler.getEntryUrlOrNull(name);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    static List<URL> getResources(String name) {
        ArrayList<URL> list = new ArrayList();
        for (ClassPathURLStreamHandler urlHandler : bootClassPathUrlHandlers) {
            URL url = urlHandler.getEntryUrlOrNull(name);
            if (url != null) {
                list.add(url);
            }
        }
        return list;
    }
}
