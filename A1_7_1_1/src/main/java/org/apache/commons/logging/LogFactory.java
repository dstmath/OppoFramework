package org.apache.commons.logging;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.apache.commons.logging.impl.Jdk14Logger;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@Deprecated
public abstract class LogFactory {
    public static final String DIAGNOSTICS_DEST_PROPERTY = "org.apache.commons.logging.diagnostics.dest";
    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.impl.LogFactoryImpl";
    public static final String FACTORY_PROPERTIES = "commons-logging.properties";
    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";
    public static final String HASHTABLE_IMPLEMENTATION_PROPERTY = "org.apache.commons.logging.LogFactory.HashtableImpl";
    public static final String PRIORITY_KEY = "priority";
    protected static final String SERVICE_ID = "META-INF/services/org.apache.commons.logging.LogFactory";
    public static final String TCCL_KEY = "use_tccl";
    private static final String WEAK_HASHTABLE_CLASSNAME = "org.apache.commons.logging.impl.WeakHashtable";
    private static String diagnosticPrefix;
    private static PrintStream diagnosticsStream;
    protected static Hashtable factories;
    protected static LogFactory nullClassLoaderFactory;
    private static ClassLoader thisClassLoader;

    /* renamed from: org.apache.commons.logging.LogFactory$2 */
    static class AnonymousClass2 implements PrivilegedAction {
        final /* synthetic */ ClassLoader val$classLoader;
        final /* synthetic */ String val$factoryClass;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.LogFactory.2.<init>(java.lang.String, java.lang.ClassLoader):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(java.lang.String r1, java.lang.ClassLoader r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.LogFactory.2.<init>(java.lang.String, java.lang.ClassLoader):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.2.<init>(java.lang.String, java.lang.ClassLoader):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.commons.logging.LogFactory.2.run():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.commons.logging.LogFactory.2.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.2.run():java.lang.Object");
        }
    }

    /* renamed from: org.apache.commons.logging.LogFactory$3 */
    static class AnonymousClass3 implements PrivilegedAction {
        final /* synthetic */ ClassLoader val$loader;
        final /* synthetic */ String val$name;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.LogFactory.3.<init>(java.lang.ClassLoader, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass3(java.lang.ClassLoader r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.LogFactory.3.<init>(java.lang.ClassLoader, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.3.<init>(java.lang.ClassLoader, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.commons.logging.LogFactory.3.run():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.commons.logging.LogFactory.3.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.3.run():java.lang.Object");
        }
    }

    /* renamed from: org.apache.commons.logging.LogFactory$4 */
    static class AnonymousClass4 implements PrivilegedAction {
        final /* synthetic */ ClassLoader val$loader;
        final /* synthetic */ String val$name;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.LogFactory.4.<init>(java.lang.ClassLoader, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass4(java.lang.ClassLoader r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.LogFactory.4.<init>(java.lang.ClassLoader, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.4.<init>(java.lang.ClassLoader, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.apache.commons.logging.LogFactory.4.run():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.apache.commons.logging.LogFactory.4.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.4.run():java.lang.Object");
        }
    }

    /* renamed from: org.apache.commons.logging.LogFactory$5 */
    static class AnonymousClass5 implements PrivilegedAction {
        final /* synthetic */ URL val$url;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.LogFactory.5.<init>(java.net.URL):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass5(java.net.URL r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: org.apache.commons.logging.LogFactory.5.<init>(java.net.URL):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.5.<init>(java.net.URL):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.apache.commons.logging.LogFactory.5.run():java.lang.Object, dex:  in method: org.apache.commons.logging.LogFactory.5.run():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.apache.commons.logging.LogFactory.5.run():java.lang.Object, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: null in method: org.apache.commons.logging.LogFactory.5.run():java.lang.Object, dex:  in method: org.apache.commons.logging.LogFactory.5.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.5.run():java.lang.Object");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.apache.commons.logging.LogFactory.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.apache.commons.logging.LogFactory.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.<clinit>():void");
    }

    public abstract Object getAttribute(String str);

    public abstract String[] getAttributeNames();

    public abstract Log getInstance(Class cls) throws LogConfigurationException;

    public abstract Log getInstance(String str) throws LogConfigurationException;

    public abstract void release();

    public abstract void removeAttribute(String str);

    public abstract void setAttribute(String str, Object obj);

    protected LogFactory() {
    }

    private static final Hashtable createFactoryStore() {
        Hashtable result = null;
        String storeImplementationClass = System.getProperty(HASHTABLE_IMPLEMENTATION_PROPERTY);
        if (storeImplementationClass == null) {
            storeImplementationClass = WEAK_HASHTABLE_CLASSNAME;
        }
        try {
            result = (Hashtable) Class.forName(storeImplementationClass).newInstance();
        } catch (Throwable th) {
            if (!WEAK_HASHTABLE_CLASSNAME.equals(storeImplementationClass)) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("[ERROR] LogFactory: Load of custom hashtable failed");
                } else {
                    System.err.println("[ERROR] LogFactory: Load of custom hashtable failed");
                }
            }
        }
        if (result == null) {
            return new Hashtable();
        }
        return result;
    }

    public static LogFactory getFactory() throws LogConfigurationException {
        ClassLoader contextClassLoader = getContextClassLoader();
        if (contextClassLoader == null && isDiagnosticsEnabled()) {
            logDiagnostic("Context classloader is null.");
        }
        LogFactory factory = getCachedFactory(contextClassLoader);
        if (factory != null) {
            return factory;
        }
        String factoryClass;
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[LOOKUP] LogFactory implementation requested for the first time for context classloader " + objectId(contextClassLoader));
            logHierarchy("[LOOKUP] ", contextClassLoader);
        }
        Properties props = getConfigurationFile(contextClassLoader, FACTORY_PROPERTIES);
        ClassLoader baseClassLoader = contextClassLoader;
        if (props != null) {
            String useTCCLStr = props.getProperty(TCCL_KEY);
            if (!(useTCCLStr == null || Boolean.valueOf(useTCCLStr).booleanValue())) {
                baseClassLoader = thisClassLoader;
            }
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[LOOKUP] Looking for system property [org.apache.commons.logging.LogFactory] to define the LogFactory subclass to use...");
        }
        try {
            factoryClass = System.getProperty(FACTORY_PROPERTY);
            if (factoryClass != null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("[LOOKUP] Creating an instance of LogFactory class '" + factoryClass + "' as specified by system property " + FACTORY_PROPERTY);
                }
                factory = newFactory(factoryClass, baseClassLoader, contextClassLoader);
            } else if (isDiagnosticsEnabled()) {
                logDiagnostic("[LOOKUP] No system property [org.apache.commons.logging.LogFactory] defined.");
            }
        } catch (SecurityException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[LOOKUP] A security exception occurred while trying to create an instance of the custom factory class: [" + e.getMessage().trim() + "]. Trying alternative implementations...");
            }
        } catch (RuntimeException e2) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[LOOKUP] An exception occurred while trying to create an instance of the custom factory class: [" + e2.getMessage().trim() + "] as specified by a system property.");
            }
            throw e2;
        }
        if (factory == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[LOOKUP] Looking for a resource file of name [META-INF/services/org.apache.commons.logging.LogFactory] to define the LogFactory subclass to use...");
            }
            try {
                InputStream is = getResourceAsStream(contextClassLoader, SERVICE_ID);
                if (is != null) {
                    BufferedReader rd;
                    try {
                        rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    } catch (UnsupportedEncodingException e3) {
                        rd = new BufferedReader(new InputStreamReader(is));
                    }
                    String factoryClassName = rd.readLine();
                    rd.close();
                    if (!(factoryClassName == null || "".equals(factoryClassName))) {
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("[LOOKUP]  Creating an instance of LogFactory class " + factoryClassName + " as specified by file '" + SERVICE_ID + "' which was present in the path of the context" + " classloader.");
                        }
                        factory = newFactory(factoryClassName, baseClassLoader, contextClassLoader);
                    }
                } else if (isDiagnosticsEnabled()) {
                    logDiagnostic("[LOOKUP] No resource file with name 'META-INF/services/org.apache.commons.logging.LogFactory' found.");
                }
            } catch (Exception ex) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("[LOOKUP] A security exception occurred while trying to create an instance of the custom factory class: [" + ex.getMessage().trim() + "]. Trying alternative implementations...");
                }
            }
        }
        if (factory == null) {
            if (props != null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("[LOOKUP] Looking in properties file for entry with key 'org.apache.commons.logging.LogFactory' to define the LogFactory subclass to use...");
                }
                factoryClass = props.getProperty(FACTORY_PROPERTY);
                if (factoryClass != null) {
                    if (isDiagnosticsEnabled()) {
                        logDiagnostic("[LOOKUP] Properties file specifies LogFactory subclass '" + factoryClass + "'");
                    }
                    factory = newFactory(factoryClass, baseClassLoader, contextClassLoader);
                } else if (isDiagnosticsEnabled()) {
                    logDiagnostic("[LOOKUP] Properties file has no entry specifying LogFactory subclass.");
                }
            } else if (isDiagnosticsEnabled()) {
                logDiagnostic("[LOOKUP] No properties file available to determine LogFactory subclass from..");
            }
        }
        if (factory == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[LOOKUP] Loading the default LogFactory implementation 'org.apache.commons.logging.impl.LogFactoryImpl' via the same classloader that loaded this LogFactory class (ie not looking in the context classloader).");
            }
            factory = newFactory(FACTORY_DEFAULT, thisClassLoader, contextClassLoader);
        }
        if (factory != null) {
            cacheFactory(contextClassLoader, factory);
            if (props != null) {
                Enumeration names = props.propertyNames();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    factory.setAttribute(name, props.getProperty(name));
                }
            }
        }
        return factory;
    }

    public static Log getLog(Class clazz) throws LogConfigurationException {
        return getLog(clazz.getName());
    }

    public static Log getLog(String name) throws LogConfigurationException {
        return new Jdk14Logger(name);
    }

    public static void release(ClassLoader classLoader) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Releasing factory for classloader " + objectId(classLoader));
        }
        synchronized (factories) {
            if (classLoader != null) {
                LogFactory factory = (LogFactory) factories.get(classLoader);
                if (factory != null) {
                    factory.release();
                    factories.remove(classLoader);
                }
            } else if (nullClassLoaderFactory != null) {
                nullClassLoaderFactory.release();
                nullClassLoaderFactory = null;
            }
        }
    }

    public static void releaseAll() {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Releasing factory for all classloaders.");
        }
        synchronized (factories) {
            Enumeration elements = factories.elements();
            while (elements.hasMoreElements()) {
                ((LogFactory) elements.nextElement()).release();
            }
            factories.clear();
            if (nullClassLoaderFactory != null) {
                nullClassLoaderFactory.release();
                nullClassLoaderFactory = null;
            }
        }
    }

    protected static ClassLoader getClassLoader(Class clazz) {
        try {
            return clazz.getClassLoader();
        } catch (SecurityException ex) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Unable to get classloader for class '" + clazz + "' due to security restrictions - " + ex.getMessage());
            }
            throw ex;
        }
    }

    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        return (ClassLoader) AccessController.doPrivileged(
/*
Method generation error in method: org.apache.commons.logging.LogFactory.getContextClassLoader():java.lang.ClassLoader, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000b: RETURN  (wrap: java.lang.ClassLoader
  0x0009: CHECK_CAST  (r0_2 java.lang.ClassLoader) = (wrap: java.lang.Object
  0x0005: INVOKE  (r0_1 java.lang.Object) = (wrap: java.security.PrivilegedAction
  0x0002: CONSTRUCTOR  (r0_0 java.security.PrivilegedAction) =  org.apache.commons.logging.LogFactory.1.<init>():void CONSTRUCTOR) java.security.AccessController.doPrivileged(java.security.PrivilegedAction):java.lang.Object type: STATIC) java.lang.ClassLoader) in method: org.apache.commons.logging.LogFactory.getContextClassLoader():java.lang.ClassLoader, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:205)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0009: CHECK_CAST  (r0_2 java.lang.ClassLoader) = (wrap: java.lang.Object
  0x0005: INVOKE  (r0_1 java.lang.Object) = (wrap: java.security.PrivilegedAction
  0x0002: CONSTRUCTOR  (r0_0 java.security.PrivilegedAction) =  org.apache.commons.logging.LogFactory.1.<init>():void CONSTRUCTOR) java.security.AccessController.doPrivileged(java.security.PrivilegedAction):java.lang.Object type: STATIC) java.lang.ClassLoader in method: org.apache.commons.logging.LogFactory.getContextClassLoader():java.lang.ClassLoader, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:101)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:286)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 16 more
Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0005: INVOKE  (r0_1 java.lang.Object) = (wrap: java.security.PrivilegedAction
  0x0002: CONSTRUCTOR  (r0_0 java.security.PrivilegedAction) =  org.apache.commons.logging.LogFactory.1.<init>():void CONSTRUCTOR) java.security.AccessController.doPrivileged(java.security.PrivilegedAction):java.lang.Object type: STATIC in method: org.apache.commons.logging.LogFactory.getContextClassLoader():java.lang.ClassLoader, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:101)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:264)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:213)
	... 19 more
Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0002: CONSTRUCTOR  (r0_0 java.security.PrivilegedAction) =  org.apache.commons.logging.LogFactory.1.<init>():void CONSTRUCTOR in method: org.apache.commons.logging.LogFactory.getContextClassLoader():java.lang.ClassLoader, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:101)
	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:688)
	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:658)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:340)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:213)
	... 22 more
Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Null container variable
	at jadx.core.utils.RegionUtils.notEmpty(RegionUtils.java:151)
	at jadx.core.codegen.InsnGen.inlineAnonymousConstr(InsnGen.java:595)
	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:561)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:336)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:213)
	... 27 more

*/

    protected static ClassLoader directGetContextClassLoader() throws LogConfigurationException {
        ClassLoader classLoader = null;
        try {
            return (ClassLoader) Thread.class.getMethod("getContextClassLoader", (Class[]) null).invoke(Thread.currentThread(), (Object[]) null);
        } catch (IllegalAccessException e) {
            throw new LogConfigurationException("Unexpected IllegalAccessException", e);
        } catch (InvocationTargetException e2) {
            if (e2.getTargetException() instanceof SecurityException) {
                return classLoader;
            }
            throw new LogConfigurationException("Unexpected InvocationTargetException", e2.getTargetException());
        } catch (NoSuchMethodException e3) {
            return getClassLoader(LogFactory.class);
        }
    }

    private static LogFactory getCachedFactory(ClassLoader contextClassLoader) {
        if (contextClassLoader == null) {
            return nullClassLoaderFactory;
        }
        return (LogFactory) factories.get(contextClassLoader);
    }

    private static void cacheFactory(ClassLoader classLoader, LogFactory factory) {
        if (factory == null) {
            return;
        }
        if (classLoader == null) {
            nullClassLoaderFactory = factory;
        } else {
            factories.put(classLoader, factory);
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    protected static org.apache.commons.logging.LogFactory newFactory(java.lang.String r4, java.lang.ClassLoader r5, java.lang.ClassLoader r6) throws org.apache.commons.logging.LogConfigurationException {
        /*
        r2 = new org.apache.commons.logging.LogFactory$2;
        r2.<init>(r4, r5);
        r1 = java.security.AccessController.doPrivileged(r2);
        r2 = r1 instanceof org.apache.commons.logging.LogConfigurationException;
        if (r2 == 0) goto L_0x0032;
    L_0x000d:
        r0 = r1;
        r0 = (org.apache.commons.logging.LogConfigurationException) r0;
        r2 = isDiagnosticsEnabled();
        if (r2 == 0) goto L_0x0031;
    L_0x0016:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "An error occurred while loading the factory class:";
        r2 = r2.append(r3);
        r3 = r0.getMessage();
        r2 = r2.append(r3);
        r2 = r2.toString();
        logDiagnostic(r2);
    L_0x0031:
        throw r0;
    L_0x0032:
        r2 = isDiagnosticsEnabled();
        if (r2 == 0) goto L_0x0062;
    L_0x0038:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Created object ";
        r2 = r2.append(r3);
        r3 = objectId(r1);
        r2 = r2.append(r3);
        r3 = " to manage classloader ";
        r2 = r2.append(r3);
        r3 = objectId(r6);
        r2 = r2.append(r3);
        r2 = r2.toString();
        logDiagnostic(r2);
    L_0x0062:
        r1 = (org.apache.commons.logging.LogFactory) r1;
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.newFactory(java.lang.String, java.lang.ClassLoader, java.lang.ClassLoader):org.apache.commons.logging.LogFactory");
    }

    protected static LogFactory newFactory(String factoryClass, ClassLoader classLoader) {
        return newFactory(factoryClass, classLoader, null);
    }

    protected static Object createFactory(String factoryClass, ClassLoader classLoader) {
        if (classLoader != null) {
            try {
                Class logFactoryClass = classLoader.loadClass(factoryClass);
                if (LogFactory.class.isAssignableFrom(logFactoryClass)) {
                    if (isDiagnosticsEnabled()) {
                        logDiagnostic("Loaded class " + logFactoryClass.getName() + " from classloader " + objectId(classLoader));
                    }
                } else if (isDiagnosticsEnabled()) {
                    logDiagnostic("Factory class " + logFactoryClass.getName() + " loaded from classloader " + objectId(logFactoryClass.getClassLoader()) + " does not extend '" + LogFactory.class.getName() + "' as loaded by this classloader.");
                    logHierarchy("[BAD CL TREE] ", classLoader);
                }
                return (LogFactory) logFactoryClass.newInstance();
            } catch (ClassNotFoundException ex) {
                if (classLoader == thisClassLoader) {
                    if (isDiagnosticsEnabled()) {
                        logDiagnostic("Unable to locate any class called '" + factoryClass + "' via classloader " + objectId(classLoader));
                    }
                    throw ex;
                }
            } catch (NoClassDefFoundError e) {
                if (classLoader == thisClassLoader) {
                    if (isDiagnosticsEnabled()) {
                        logDiagnostic("Class '" + factoryClass + "' cannot be loaded" + " via classloader " + objectId(classLoader) + " - it depends on some other class that cannot" + " be found.");
                    }
                    throw e;
                }
            } catch (ClassCastException e2) {
                if (classLoader == thisClassLoader) {
                    boolean implementsLogFactory = implementsLogFactory(null);
                    String msg = "The application has specified that a custom LogFactory implementation should be used but Class '" + factoryClass + "' cannot be converted to '" + LogFactory.class.getName() + "'. ";
                    if (implementsLogFactory) {
                        msg = msg + "The conflict is caused by the presence of multiple LogFactory classes in incompatible classloaders. " + "Background can be found in http://jakarta.apache.org/commons/logging/tech.html. " + "If you have not explicitly specified a custom LogFactory then it is likely that " + "the container has set one without your knowledge. " + "In this case, consider using the commons-logging-adapters.jar file or " + "specifying the standard LogFactory from the command line. ";
                    } else {
                        msg = msg + "Please check the custom implementation. ";
                    }
                    msg = msg + "Help can be found @http://jakarta.apache.org/commons/logging/troubleshooting.html.";
                    if (isDiagnosticsEnabled()) {
                        logDiagnostic(msg);
                    }
                    throw new ClassCastException(msg);
                }
            } catch (Throwable e3) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("Unable to create LogFactory instance.");
                }
                if (null == null || LogFactory.class.isAssignableFrom(null)) {
                    return new LogConfigurationException(e3);
                }
                return new LogConfigurationException("The chosen LogFactory implementation does not extend LogFactory. Please check your configuration.", e3);
            }
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Unable to load factory class via classloader " + objectId(classLoader) + " - trying the classloader associated with this LogFactory.");
        }
        return (LogFactory) Class.forName(factoryClass).newInstance();
    }

    private static boolean implementsLogFactory(Class logFactoryClass) {
        boolean implementsLogFactory = false;
        if (logFactoryClass != null) {
            try {
                ClassLoader logFactoryClassLoader = logFactoryClass.getClassLoader();
                if (logFactoryClassLoader == null) {
                    logDiagnostic("[CUSTOM LOG FACTORY] was loaded by the boot classloader");
                } else {
                    logHierarchy("[CUSTOM LOG FACTORY] ", logFactoryClassLoader);
                    implementsLogFactory = Class.forName(FACTORY_PROPERTY, false, logFactoryClassLoader).isAssignableFrom(logFactoryClass);
                    if (implementsLogFactory) {
                        logDiagnostic("[CUSTOM LOG FACTORY] " + logFactoryClass.getName() + " implements LogFactory but was loaded by an incompatible classloader.");
                    } else {
                        logDiagnostic("[CUSTOM LOG FACTORY] " + logFactoryClass.getName() + " does not implement LogFactory.");
                    }
                }
            } catch (SecurityException e) {
                logDiagnostic("[CUSTOM LOG FACTORY] SecurityException thrown whilst trying to determine whether the compatibility was caused by a classloader conflict: " + e.getMessage());
            } catch (LinkageError e2) {
                logDiagnostic("[CUSTOM LOG FACTORY] LinkageError thrown whilst trying to determine whether the compatibility was caused by a classloader conflict: " + e2.getMessage());
            } catch (ClassNotFoundException e3) {
                logDiagnostic("[CUSTOM LOG FACTORY] LogFactory class cannot be loaded by classloader which loaded the custom LogFactory implementation. Is the custom factory in the right classloader?");
            }
        }
        return implementsLogFactory;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static java.io.InputStream getResourceAsStream(java.lang.ClassLoader r1, java.lang.String r2) {
        /*
        r0 = new org.apache.commons.logging.LogFactory$3;
        r0.<init>(r1, r2);
        r0 = java.security.AccessController.doPrivileged(r0);
        r0 = (java.io.InputStream) r0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.getResourceAsStream(java.lang.ClassLoader, java.lang.String):java.io.InputStream");
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static java.util.Enumeration getResources(java.lang.ClassLoader r2, java.lang.String r3) {
        /*
        r0 = new org.apache.commons.logging.LogFactory$4;
        r0.<init>(r2, r3);
        r1 = java.security.AccessController.doPrivileged(r0);
        r1 = (java.util.Enumeration) r1;
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.getResources(java.lang.ClassLoader, java.lang.String):java.util.Enumeration");
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static java.util.Properties getProperties(java.net.URL r2) {
        /*
        r0 = new org.apache.commons.logging.LogFactory$5;
        r0.<init>(r2);
        r1 = java.security.AccessController.doPrivileged(r0);
        r1 = (java.util.Properties) r1;
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.LogFactory.getProperties(java.net.URL):java.util.Properties");
    }

    private static final Properties getConfigurationFile(ClassLoader classLoader, String fileName) {
        Properties props = null;
        double priority = 0.0d;
        Object propsUrl = null;
        try {
            Enumeration urls = getResources(classLoader, fileName);
            if (urls == null) {
                return null;
            }
            while (urls.hasMoreElements()) {
                URL url = (URL) urls.nextElement();
                Properties newProps = getProperties(url);
                if (newProps != null) {
                    if (props == null) {
                        propsUrl = url;
                        props = newProps;
                        String priorityStr = newProps.getProperty(PRIORITY_KEY);
                        priority = 0.0d;
                        if (priorityStr != null) {
                            priority = Double.parseDouble(priorityStr);
                        }
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("[LOOKUP] Properties file found at '" + url + "'" + " with priority " + priority);
                        }
                    } else {
                        String newPriorityStr = newProps.getProperty(PRIORITY_KEY);
                        double newPriority = 0.0d;
                        if (newPriorityStr != null) {
                            newPriority = Double.parseDouble(newPriorityStr);
                        }
                        if (newPriority > priority) {
                            if (isDiagnosticsEnabled()) {
                                logDiagnostic("[LOOKUP] Properties file at '" + url + "'" + " with priority " + newPriority + " overrides file at '" + propsUrl + "'" + " with priority " + priority);
                            }
                            propsUrl = url;
                            props = newProps;
                            priority = newPriority;
                        } else if (isDiagnosticsEnabled()) {
                            logDiagnostic("[LOOKUP] Properties file at '" + url + "'" + " with priority " + newPriority + " does not override file at '" + propsUrl + "'" + " with priority " + priority);
                        }
                    }
                }
            }
            if (isDiagnosticsEnabled()) {
                if (props == null) {
                    logDiagnostic("[LOOKUP] No properties file of name '" + fileName + "' found.");
                } else {
                    logDiagnostic("[LOOKUP] Properties file of name '" + fileName + "' found at '" + propsUrl + '\"');
                }
            }
            return props;
        } catch (SecurityException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("SecurityException thrown while trying to find/read config files.");
            }
        }
    }

    private static void initDiagnostics() {
        try {
            String dest = System.getProperty(DIAGNOSTICS_DEST_PROPERTY);
            if (dest != null) {
                String classLoaderName;
                if (dest.equals("STDOUT")) {
                    diagnosticsStream = System.out;
                } else if (dest.equals("STDERR")) {
                    diagnosticsStream = System.err;
                } else {
                    try {
                        diagnosticsStream = new PrintStream(new FileOutputStream(dest, true));
                    } catch (IOException e) {
                        return;
                    }
                }
                try {
                    ClassLoader classLoader = thisClassLoader;
                    if (thisClassLoader == null) {
                        classLoaderName = "BOOTLOADER";
                    } else {
                        classLoaderName = objectId(classLoader);
                    }
                } catch (SecurityException e2) {
                    classLoaderName = "UNKNOWN";
                }
                diagnosticPrefix = "[LogFactory from " + classLoaderName + "] ";
            }
        } catch (SecurityException e3) {
        }
    }

    protected static boolean isDiagnosticsEnabled() {
        return diagnosticsStream != null;
    }

    private static final void logDiagnostic(String msg) {
        if (diagnosticsStream != null) {
            diagnosticsStream.print(diagnosticPrefix);
            diagnosticsStream.println(msg);
            diagnosticsStream.flush();
        }
    }

    protected static final void logRawDiagnostic(String msg) {
        if (diagnosticsStream != null) {
            diagnosticsStream.println(msg);
            diagnosticsStream.flush();
        }
    }

    private static void logClassLoaderEnvironment(Class clazz) {
        if (isDiagnosticsEnabled()) {
            try {
                logDiagnostic("[ENV] Extension directories (java.ext.dir): " + System.getProperty("java.ext.dir"));
                logDiagnostic("[ENV] Application classpath (java.class.path): " + System.getProperty("java.class.path"));
            } catch (SecurityException e) {
                logDiagnostic("[ENV] Security setting prevent interrogation of system classpaths.");
            }
            String className = clazz.getName();
            try {
                ClassLoader classLoader = getClassLoader(clazz);
                logDiagnostic("[ENV] Class " + className + " was loaded via classloader " + objectId(classLoader));
                logHierarchy("[ENV] Ancestry of classloader which loaded " + className + " is ", classLoader);
            } catch (SecurityException e2) {
                logDiagnostic("[ENV] Security forbids determining the classloader for " + className);
            }
        }
    }

    private static void logHierarchy(String prefix, ClassLoader classLoader) {
        if (isDiagnosticsEnabled()) {
            if (classLoader != null) {
                logDiagnostic(prefix + objectId(classLoader) + " == '" + classLoader.toString() + "'");
            }
            try {
                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                if (classLoader != null) {
                    StringBuffer buf = new StringBuffer(prefix + "ClassLoader tree:");
                    do {
                        buf.append(objectId(classLoader));
                        if (classLoader == systemClassLoader) {
                            buf.append(" (SYSTEM) ");
                        }
                        try {
                            classLoader = classLoader.getParent();
                            buf.append(" --> ");
                        } catch (SecurityException e) {
                            buf.append(" --> SECRET");
                        }
                    } while (classLoader != null);
                    buf.append("BOOT");
                    logDiagnostic(buf.toString());
                }
            } catch (SecurityException e2) {
                logDiagnostic(prefix + "Security forbids determining the system classloader.");
            }
        }
    }

    public static String objectId(Object o) {
        if (o == null) {
            return "null";
        }
        return o.getClass().getName() + "@" + System.identityHashCode(o);
    }
}
