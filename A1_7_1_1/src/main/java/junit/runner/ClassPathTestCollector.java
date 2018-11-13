package junit.runner;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

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
public abstract class ClassPathTestCollector implements TestCollector {
    static final int SUFFIX_LENGTH = 0;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: junit.runner.ClassPathTestCollector.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: junit.runner.ClassPathTestCollector.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: junit.runner.ClassPathTestCollector.<clinit>():void");
    }

    public Enumeration collectTests() {
        return collectFilesInPath(System.getProperty("java.class.path")).elements();
    }

    public Hashtable collectFilesInPath(String classPath) {
        return collectFilesInRoots(splitClassPath(classPath));
    }

    Hashtable collectFilesInRoots(Vector roots) {
        Hashtable result = new Hashtable(100);
        Enumeration e = roots.elements();
        while (e.hasMoreElements()) {
            gatherFiles(new File((String) e.nextElement()), "", result);
        }
        return result;
    }

    void gatherFiles(File classRoot, String classFileName, Hashtable result) {
        File thisRoot = new File(classRoot, classFileName);
        if (thisRoot.isFile()) {
            if (isTestClass(classFileName)) {
                String className = classNameFromFile(classFileName);
                result.put(className, className);
            }
            return;
        }
        String[] contents = thisRoot.list();
        if (contents != null) {
            for (String str : contents) {
                gatherFiles(classRoot, classFileName + File.separatorChar + str, result);
            }
        }
    }

    Vector splitClassPath(String classPath) {
        Vector result = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(classPath, System.getProperty("path.separator"));
        while (tokenizer.hasMoreTokens()) {
            result.addElement(tokenizer.nextToken());
        }
        return result;
    }

    protected boolean isTestClass(String classFileName) {
        if (!classFileName.endsWith(".class") || classFileName.indexOf(36) >= 0 || classFileName.indexOf("Test") <= 0) {
            return false;
        }
        return true;
    }

    protected String classNameFromFile(String classFileName) {
        String s2 = classFileName.substring(0, classFileName.length() - SUFFIX_LENGTH).replace(File.separatorChar, '.');
        if (s2.startsWith(".")) {
            return s2.substring(1);
        }
        return s2;
    }
}
