package javax.crypto;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
final class JceSecurity {
    private static final URL NULL_URL = null;
    private static final Object PROVIDER_VERIFIED = null;
    static final SecureRandom RANDOM = null;
    private static final Map codeBaseCacheRef = null;
    private static CryptoPermissions defaultPolicy;
    private static CryptoPermissions exemptPolicy;
    private static boolean isRestricted;
    private static final Map verificationResults = null;
    private static final Map verifyingProviders = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: javax.crypto.JceSecurity.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: javax.crypto.JceSecurity.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.crypto.JceSecurity.<clinit>():void");
    }

    private JceSecurity() {
    }

    static Instance getInstance(String type, Class clazz, String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = getVerificationResult(s.getProvider());
        if (ve == null) {
            return GetInstance.getInstance(s, clazz);
        }
        throw ((NoSuchProviderException) new NoSuchProviderException("JCE cannot authenticate the provider " + provider).initCause(ve));
    }

    static Instance getInstance(String type, Class clazz, String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = getVerificationResult(provider);
        if (ve == null) {
            return GetInstance.getInstance(s, clazz);
        }
        throw new SecurityException("JCE cannot authenticate the provider " + provider.getName(), ve);
    }

    static Instance getInstance(String type, Class clazz, String algorithm) throws NoSuchAlgorithmException {
        Throwable failure = null;
        for (Service s : GetInstance.getServices(type, algorithm)) {
            if (canUseProvider(s.getProvider())) {
                try {
                    return GetInstance.getInstance(s, clazz);
                } catch (Throwable e) {
                    failure = e;
                }
            }
        }
        throw new NoSuchAlgorithmException("Algorithm " + algorithm + " not available", failure);
    }

    static CryptoPermissions verifyExemptJar(URL codeBase) throws Exception {
        JarVerifier jv = new JarVerifier(codeBase, true);
        jv.verify();
        return jv.getPermissions();
    }

    static void verifyProviderJar(URL codeBase) throws Exception {
        new JarVerifier(codeBase, false).verify();
    }

    static synchronized Exception getVerificationResult(Provider p) {
        synchronized (JceSecurity.class) {
            Object o = verificationResults.get(p);
            if (o == PROVIDER_VERIFIED) {
                return null;
            } else if (o != null) {
                Exception exception = (Exception) o;
                return exception;
            } else if (verifyingProviders.get(p) != null) {
                Exception noSuchProviderException = new NoSuchProviderException("Recursion during verification");
                return noSuchProviderException;
            } else {
                try {
                    verifyingProviders.put(p, Boolean.FALSE);
                    verifyProviderJar(getCodeBase(p.getClass()));
                    verificationResults.put(p, PROVIDER_VERIFIED);
                    verifyingProviders.remove(p);
                    return null;
                } catch (Exception e) {
                    verificationResults.put(p, e);
                    verifyingProviders.remove(p);
                    return e;
                } catch (Throwable th) {
                    verifyingProviders.remove(p);
                }
            }
        }
    }

    static boolean canUseProvider(Provider p) {
        return true;
    }

    static URL getCodeBase(final Class clazz) {
        URL url = (URL) codeBaseCacheRef.get(clazz);
        if (url == null) {
            url = (URL) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    ProtectionDomain pd = clazz.getProtectionDomain();
                    if (pd != null) {
                        CodeSource cs = pd.getCodeSource();
                        if (cs != null) {
                            return cs.getLocation();
                        }
                    }
                    return JceSecurity.NULL_URL;
                }
            });
            codeBaseCacheRef.put(clazz, url);
        }
        return url == NULL_URL ? null : url;
    }

    private static void setupJurisdictionPolicies() throws Exception {
        String javaHomeDir = System.getProperty("java.home");
        String sep = File.separator;
        String pathToPolicyJar = javaHomeDir + sep + "lib" + sep + "security" + sep;
        File exportJar = new File(pathToPolicyJar, "US_export_policy.jar");
        File importJar = new File(pathToPolicyJar, "local_policy.jar");
        if (ClassLoader.getSystemResource("javax/crypto/Cipher.class") != null && exportJar.exists() && importJar.exists()) {
            CryptoPermissions defaultExport = new CryptoPermissions();
            CryptoPermissions exemptExport = new CryptoPermissions();
            loadPolicies(exportJar, defaultExport, exemptExport);
            CryptoPermissions defaultImport = new CryptoPermissions();
            CryptoPermissions exemptImport = new CryptoPermissions();
            loadPolicies(importJar, defaultImport, exemptImport);
            if (defaultExport.isEmpty() || defaultImport.isEmpty()) {
                throw new SecurityException("Missing mandatory jurisdiction policy files");
            }
            defaultPolicy = defaultExport.getMinimum(defaultImport);
            if (exemptExport.isEmpty()) {
                if (exemptImport.isEmpty()) {
                    exemptImport = null;
                }
                exemptPolicy = exemptImport;
                return;
            }
            exemptPolicy = exemptExport.getMinimum(exemptImport);
            return;
        }
        throw new SecurityException("Cannot locate policy or framework files!");
    }

    private static void loadPolicies(File jarPathName, CryptoPermissions defaultPolicy, CryptoPermissions exemptPolicy) throws Exception {
        JarFile jf = new JarFile(jarPathName);
        Enumeration entries = jf.entries();
        while (entries.hasMoreElements()) {
            JarEntry je = (JarEntry) entries.nextElement();
            InputStream inputStream = null;
            try {
                if (je.getName().startsWith("default_")) {
                    inputStream = jf.getInputStream(je);
                    defaultPolicy.load(inputStream);
                } else if (je.getName().startsWith("exempt_")) {
                    inputStream = jf.getInputStream(je);
                    exemptPolicy.load(inputStream);
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                JarVerifier.verifyPolicySigned(je.getCertificates());
            } catch (Throwable th) {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        jf.close();
    }

    static CryptoPermissions getDefaultPolicy() {
        return defaultPolicy;
    }

    static CryptoPermissions getExemptPolicy() {
        return exemptPolicy;
    }

    static boolean isRestricted() {
        return isRestricted;
    }
}
