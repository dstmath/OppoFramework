package javax.net.ssl;

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import javax.net.ServerSocketFactory;

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
public abstract class SSLServerSocketFactory extends ServerSocketFactory {
    private static SSLServerSocketFactory defaultServerSocketFactory;
    private static int lastVersion;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: javax.net.ssl.SSLServerSocketFactory.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: javax.net.ssl.SSLServerSocketFactory.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.net.ssl.SSLServerSocketFactory.<clinit>():void");
    }

    public abstract String[] getDefaultCipherSuites();

    public abstract String[] getSupportedCipherSuites();

    private static void log(String msg) {
        if (SSLSocketFactory.DEBUG) {
            System.out.println(msg);
        }
    }

    protected SSLServerSocketFactory() {
    }

    public static synchronized ServerSocketFactory getDefault() {
        synchronized (SSLServerSocketFactory.class) {
            ServerSocketFactory serverSocketFactory;
            if (defaultServerSocketFactory == null || lastVersion != Security.getVersion()) {
                lastVersion = Security.getVersion();
                SSLServerSocketFactory previousDefaultServerSocketFactory = defaultServerSocketFactory;
                defaultServerSocketFactory = null;
                String clsName = SSLSocketFactory.getSecurityProperty("ssl.ServerSocketFactory.provider");
                if (clsName != null) {
                    if (previousDefaultServerSocketFactory == null || !clsName.equals(previousDefaultServerSocketFactory.getClass().getName())) {
                        Class cls = null;
                        log("setting up default SSLServerSocketFactory");
                        try {
                            log("setting up default SSLServerSocketFactory");
                            try {
                                cls = Class.forName(clsName);
                            } catch (ClassNotFoundException e) {
                                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                                if (cl == null) {
                                    cl = ClassLoader.getSystemClassLoader();
                                }
                                if (cl != null) {
                                    cls = Class.forName(clsName, true, cl);
                                }
                            }
                            log("class " + clsName + " is loaded");
                            SSLServerSocketFactory fac = (SSLServerSocketFactory) cls.newInstance();
                            log("instantiated an instance of class " + clsName);
                            defaultServerSocketFactory = fac;
                            if (defaultServerSocketFactory != null) {
                                serverSocketFactory = defaultServerSocketFactory;
                                return serverSocketFactory;
                            }
                        } catch (Object e2) {
                            log("SSLServerSocketFactory instantiation failed: " + e2);
                        }
                    } else {
                        defaultServerSocketFactory = previousDefaultServerSocketFactory;
                        serverSocketFactory = defaultServerSocketFactory;
                        return serverSocketFactory;
                    }
                }
                try {
                    SSLContext context = SSLContext.getDefault();
                    if (context != null) {
                        defaultServerSocketFactory = context.getServerSocketFactory();
                    }
                } catch (NoSuchAlgorithmException e3) {
                }
                if (defaultServerSocketFactory == null) {
                    defaultServerSocketFactory = new DefaultSSLServerSocketFactory(new IllegalStateException("No ServerSocketFactory implementation found"));
                }
                serverSocketFactory = defaultServerSocketFactory;
                return serverSocketFactory;
            }
            serverSocketFactory = defaultServerSocketFactory;
            return serverSocketFactory;
        }
    }
}
