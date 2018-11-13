package javax.net.ssl;

import java.io.IOException;
import java.net.Socket;
import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Security;
import javax.net.SocketFactory;

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
public abstract class SSLSocketFactory extends SocketFactory {
    static final boolean DEBUG = false;
    private static SSLSocketFactory defaultSocketFactory;
    private static int lastVersion;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: javax.net.ssl.SSLSocketFactory.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: javax.net.ssl.SSLSocketFactory.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.net.ssl.SSLSocketFactory.<clinit>():void");
    }

    public abstract Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException;

    public abstract String[] getDefaultCipherSuites();

    public abstract String[] getSupportedCipherSuites();

    private static void log(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    public static synchronized SocketFactory getDefault() {
        synchronized (SSLSocketFactory.class) {
            SocketFactory socketFactory;
            if (defaultSocketFactory == null || lastVersion != Security.getVersion()) {
                lastVersion = Security.getVersion();
                SSLSocketFactory previousDefaultSocketFactory = defaultSocketFactory;
                defaultSocketFactory = null;
                String clsName = getSecurityProperty("ssl.SocketFactory.provider");
                if (clsName != null) {
                    if (previousDefaultSocketFactory == null || !clsName.equals(previousDefaultSocketFactory.getClass().getName())) {
                        log("setting up default SSLSocketFactory");
                        Class cls = null;
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
                        try {
                            log("class " + clsName + " is loaded");
                            defaultSocketFactory = (SSLSocketFactory) cls.newInstance();
                            log("instantiated an instance of class " + clsName);
                            if (defaultSocketFactory != null) {
                                socketFactory = defaultSocketFactory;
                                return socketFactory;
                            }
                        } catch (Exception e2) {
                            log("SSLSocketFactory instantiation failed: " + e2.toString());
                        }
                    } else {
                        defaultSocketFactory = previousDefaultSocketFactory;
                        socketFactory = defaultSocketFactory;
                        return socketFactory;
                    }
                }
                try {
                    SSLContext context = SSLContext.getDefault();
                    if (context != null) {
                        defaultSocketFactory = context.getSocketFactory();
                    }
                } catch (NoSuchAlgorithmException e3) {
                }
                if (defaultSocketFactory == null) {
                    defaultSocketFactory = new DefaultSSLSocketFactory(new IllegalStateException("No factory found."));
                }
                socketFactory = defaultSocketFactory;
                return socketFactory;
            }
            socketFactory = defaultSocketFactory;
            return socketFactory;
        }
    }

    static String getSecurityProperty(final String name) {
        return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                String s = Security.getProperty(name);
                if (s == null) {
                    return s;
                }
                s = s.trim();
                if (s.length() == 0) {
                    return null;
                }
                return s;
            }
        });
    }
}
