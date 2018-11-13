package sun.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

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
public class NetProperties {
    private static Properties props;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.net.NetProperties.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.net.NetProperties.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.NetProperties.<clinit>():void");
    }

    private NetProperties() {
    }

    private static void loadDefaultProperties() {
        String fname = System.getProperty("java.home");
        if (fname == null) {
            throw new Error("Can't find java.home ??");
        }
        try {
            InputStream bin = new BufferedInputStream(new FileInputStream(new File(new File(fname, "lib"), "net.properties").getCanonicalPath()));
            props.load(bin);
            bin.close();
        } catch (Exception e) {
        }
    }

    public static String get(String key) {
        try {
            return System.getProperty(key, props.getProperty(key));
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e2) {
        }
        return null;
    }

    public static Integer getInteger(String key, int defval) {
        String val = null;
        try {
            val = System.getProperty(key, props.getProperty(key));
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e2) {
        }
        if (val != null) {
            try {
                return Integer.decode(val);
            } catch (NumberFormatException e3) {
            }
        }
        return new Integer(defval);
    }

    public static Boolean getBoolean(String key) {
        String val = null;
        try {
            val = System.getProperty(key, props.getProperty(key));
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e2) {
        }
        if (val != null) {
            try {
                return Boolean.valueOf(val);
            } catch (NumberFormatException e3) {
            }
        }
        return null;
    }
}
