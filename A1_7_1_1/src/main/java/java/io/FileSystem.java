package java.io;

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
abstract class FileSystem {
    public static final int ACCESS_EXECUTE = 1;
    public static final int ACCESS_OK = 8;
    public static final int ACCESS_READ = 4;
    public static final int ACCESS_WRITE = 2;
    public static final int BA_DIRECTORY = 4;
    public static final int BA_EXISTS = 1;
    public static final int BA_HIDDEN = 8;
    public static final int BA_REGULAR = 2;
    public static final int SPACE_FREE = 1;
    public static final int SPACE_TOTAL = 0;
    public static final int SPACE_USABLE = 2;
    static boolean useCanonCaches;
    static boolean useCanonPrefixCache;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.io.FileSystem.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.io.FileSystem.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.FileSystem.<clinit>():void");
    }

    public static native FileSystem getFileSystem();

    public abstract String canonicalize(String str) throws IOException;

    public abstract boolean checkAccess(File file, int i);

    public abstract int compare(File file, File file2);

    public abstract boolean createDirectory(File file);

    public abstract boolean createFileExclusively(String str) throws IOException;

    public abstract boolean delete(File file);

    public abstract String fromURIPath(String str);

    public abstract int getBooleanAttributes(File file);

    public abstract String getDefaultParent();

    public abstract long getLastModifiedTime(File file);

    public abstract long getLength(File file);

    public abstract char getPathSeparator();

    public abstract char getSeparator();

    public abstract long getSpace(File file, int i);

    public abstract int hashCode(File file);

    public abstract boolean isAbsolute(File file);

    public abstract String[] list(File file);

    public abstract File[] listRoots();

    public abstract String normalize(String str);

    public abstract int prefixLength(String str);

    public abstract boolean rename(File file, File file2);

    public abstract String resolve(File file);

    public abstract String resolve(String str, String str2);

    public abstract boolean setLastModifiedTime(File file, long j);

    public abstract boolean setPermission(File file, int i, boolean z, boolean z2);

    public abstract boolean setReadOnly(File file);

    FileSystem() {
    }

    private static boolean getBooleanProperty(String prop, boolean defaultVal) {
        String val = System.getProperty(prop);
        if (val == null) {
            return defaultVal;
        }
        if (val.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }
}
