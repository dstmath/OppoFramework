package com.android.org.conscrypt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import libcore.io.IoUtils;
import org.conscrypt.OpenSSLX509Certificate;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public class TrustedCertificateStore {
    private static final CertificateFactory CERT_FACTORY = null;
    private static final String PREFIX_SYSTEM = "system:";
    private static final String PREFIX_USER = "user:";
    private static File defaultCaCertsAddedDir;
    private static File defaultCaCertsDeletedDir;
    private static File defaultCaCertsSystemDir;
    private final File addedDir;
    private final File deletedDir;
    private final File systemDir;

    private interface CertSelector {
        boolean match(X509Certificate x509Certificate);
    }

    /* renamed from: com.android.org.conscrypt.TrustedCertificateStore$3 */
    class AnonymousClass3 implements CertSelector {
        final /* synthetic */ TrustedCertificateStore this$0;
        final /* synthetic */ X509Certificate val$c;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.org.conscrypt.TrustedCertificateStore.3.<init>(com.android.org.conscrypt.TrustedCertificateStore, java.security.cert.X509Certificate):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass3(com.android.org.conscrypt.TrustedCertificateStore r1, java.security.cert.X509Certificate r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.org.conscrypt.TrustedCertificateStore.3.<init>(com.android.org.conscrypt.TrustedCertificateStore, java.security.cert.X509Certificate):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.TrustedCertificateStore.3.<init>(com.android.org.conscrypt.TrustedCertificateStore, java.security.cert.X509Certificate):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.TrustedCertificateStore.3.match(java.security.cert.X509Certificate):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean match(java.security.cert.X509Certificate r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.TrustedCertificateStore.3.match(java.security.cert.X509Certificate):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.TrustedCertificateStore.3.match(java.security.cert.X509Certificate):boolean");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.TrustedCertificateStore.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.TrustedCertificateStore.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.TrustedCertificateStore.<clinit>():void");
    }

    public static final boolean isSystem(String alias) {
        return alias.startsWith(PREFIX_SYSTEM);
    }

    public static final boolean isUser(String alias) {
        return alias.startsWith(PREFIX_USER);
    }

    public static void setDefaultUserDirectory(File root) {
        defaultCaCertsAddedDir = new File(root, "cacerts-added");
        defaultCaCertsDeletedDir = new File(root, "cacerts-removed");
    }

    public TrustedCertificateStore() {
        this(defaultCaCertsSystemDir, defaultCaCertsAddedDir, defaultCaCertsDeletedDir);
    }

    public TrustedCertificateStore(File systemDir, File addedDir, File deletedDir) {
        this.systemDir = systemDir;
        this.addedDir = addedDir;
        this.deletedDir = deletedDir;
    }

    public Certificate getCertificate(String alias) {
        return getCertificate(alias, false);
    }

    public Certificate getCertificate(String alias, boolean includeDeletedSystem) {
        File file = fileForAlias(alias);
        if (file == null || (isUser(alias) && isTombstone(file))) {
            return null;
        }
        X509Certificate cert = readCertificate(file);
        if (cert == null || (isSystem(alias) && !includeDeletedSystem && isDeletedSystemCertificate(cert))) {
            return null;
        }
        return cert;
    }

    private File fileForAlias(String alias) {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        File file;
        if (isSystem(alias)) {
            file = new File(this.systemDir, alias.substring(PREFIX_SYSTEM.length()));
        } else if (!isUser(alias)) {
            return null;
        } else {
            file = new File(this.addedDir, alias.substring(PREFIX_USER.length()));
        }
        if (!file.exists() || isTombstone(file)) {
            return null;
        }
        return file;
    }

    private boolean isTombstone(File file) {
        return file.length() == 0;
    }

    private X509Certificate readCertificate(File file) {
        Throwable th;
        if (!file.isFile()) {
            return null;
        }
        InputStream is = null;
        try {
            InputStream is2 = new BufferedInputStream(new FileInputStream(file));
            try {
                X509Certificate x509Certificate = (X509Certificate) CERT_FACTORY.generateCertificate(is2);
                IoUtils.closeQuietly(is2);
                return x509Certificate;
            } catch (IOException e) {
                is = is2;
                IoUtils.closeQuietly(is);
                return null;
            } catch (CertificateException e2) {
                is = is2;
                IoUtils.closeQuietly(is);
                return null;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                IoUtils.closeQuietly(is);
                throw th;
            }
        } catch (IOException e3) {
            IoUtils.closeQuietly(is);
            return null;
        } catch (CertificateException e4) {
            IoUtils.closeQuietly(is);
            return null;
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(is);
            throw th;
        }
    }

    private void writeCertificate(File file, X509Certificate cert) throws IOException, CertificateException {
        Throwable th;
        File dir = file.getParentFile();
        dir.mkdirs();
        dir.setReadable(true, false);
        dir.setExecutable(true, false);
        OutputStream os = null;
        try {
            OutputStream os2 = new FileOutputStream(file);
            try {
                os2.write(cert.getEncoded());
                IoUtils.closeQuietly(os2);
                file.setReadable(true, false);
            } catch (Throwable th2) {
                th = th2;
                os = os2;
                IoUtils.closeQuietly(os);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(os);
            throw th;
        }
    }

    private boolean isDeletedSystemCertificate(X509Certificate x) {
        return getCertificateFile(this.deletedDir, x).exists();
    }

    public Date getCreationDate(String alias) {
        if (!containsAlias(alias)) {
            return null;
        }
        File file = fileForAlias(alias);
        if (file == null) {
            return null;
        }
        long time = file.lastModified();
        if (time == 0) {
            return null;
        }
        return new Date(time);
    }

    public Set<String> aliases() {
        Set<String> result = new HashSet();
        addAliases(result, PREFIX_USER, this.addedDir);
        addAliases(result, PREFIX_SYSTEM, this.systemDir);
        return result;
    }

    public Set<String> userAliases() {
        Set<String> result = new HashSet();
        addAliases(result, PREFIX_USER, this.addedDir);
        return result;
    }

    private void addAliases(Set<String> result, String prefix, File dir) {
        String[] files = dir.list();
        if (files != null) {
            for (String filename : files) {
                String alias = prefix + filename;
                if (containsAlias(alias)) {
                    result.add(alias);
                }
            }
        }
    }

    public Set<String> allSystemAliases() {
        Set<String> result = new HashSet();
        String[] files = this.systemDir.list();
        if (files == null) {
            return result;
        }
        for (String filename : files) {
            String alias = PREFIX_SYSTEM + filename;
            if (containsAlias(alias, true)) {
                result.add(alias);
            }
        }
        return result;
    }

    public boolean containsAlias(String alias) {
        return containsAlias(alias, false);
    }

    private boolean containsAlias(String alias, boolean includeDeletedSystem) {
        return getCertificate(alias, includeDeletedSystem) != null;
    }

    public String getCertificateAlias(Certificate c) {
        return getCertificateAlias(c, false);
    }

    public String getCertificateAlias(Certificate c, boolean includeDeletedSystem) {
        if (c == null || !(c instanceof X509Certificate)) {
            return null;
        }
        X509Certificate x = (X509Certificate) c;
        File user = getCertificateFile(this.addedDir, x);
        if (user.exists()) {
            return PREFIX_USER + user.getName();
        }
        if (!includeDeletedSystem && isDeletedSystemCertificate(x)) {
            return null;
        }
        File system = getCertificateFile(this.systemDir, x);
        if (system.exists()) {
            return PREFIX_SYSTEM + system.getName();
        }
        return null;
    }

    public boolean isUserAddedCertificate(X509Certificate cert) {
        return getCertificateFile(this.addedDir, cert).exists();
    }

    public File getCertificateFile(File dir, final X509Certificate x) {
        return (File) findCert(dir, x.getSubjectX500Principal(), new CertSelector() {
            public boolean match(X509Certificate cert) {
                return cert.equals(x);
            }
        }, File.class);
    }

    public X509Certificate getTrustAnchor(final X509Certificate c) {
        CertSelector selector = new CertSelector() {
            public boolean match(X509Certificate ca) {
                return ca.getPublicKey().equals(c.getPublicKey());
            }
        };
        X509Certificate user = (X509Certificate) findCert(this.addedDir, c.getSubjectX500Principal(), selector, X509Certificate.class);
        if (user != null) {
            return user;
        }
        X509Certificate system = (X509Certificate) findCert(this.systemDir, c.getSubjectX500Principal(), selector, X509Certificate.class);
        if (system == null || isDeletedSystemCertificate(system)) {
            return null;
        }
        return system;
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public java.security.cert.X509Certificate findIssuer(java.security.cert.X509Certificate r8) {
        /*
        r7 = this;
        r6 = 0;
        r1 = new com.android.org.conscrypt.TrustedCertificateStore$3;
        r1.<init>(r7, r8);
        r0 = r8.getIssuerX500Principal();
        r4 = r7.addedDir;
        r5 = java.security.cert.X509Certificate.class;
        r3 = r7.findCert(r4, r0, r1, r5);
        r3 = (java.security.cert.X509Certificate) r3;
        if (r3 == 0) goto L_0x0017;
    L_0x0016:
        return r3;
    L_0x0017:
        r4 = r7.systemDir;
        r5 = java.security.cert.X509Certificate.class;
        r2 = r7.findCert(r4, r0, r1, r5);
        r2 = (java.security.cert.X509Certificate) r2;
        if (r2 == 0) goto L_0x0029;
    L_0x0023:
        r4 = r7.isDeletedSystemCertificate(r2);
        if (r4 == 0) goto L_0x002a;
    L_0x0029:
        return r6;
    L_0x002a:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.TrustedCertificateStore.findIssuer(java.security.cert.X509Certificate):java.security.cert.X509Certificate");
    }

    public Set<X509Certificate> findAllIssuers(final X509Certificate c) {
        Set<X509Certificate> issuers = null;
        CertSelector selector = new CertSelector(this) {
            final /* synthetic */ TrustedCertificateStore this$0;

            public boolean match(X509Certificate ca) {
                try {
                    c.verify(ca.getPublicKey());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        };
        X500Principal issuer = c.getIssuerX500Principal();
        Set<X509Certificate> userAddedCerts = (Set) findCert(this.addedDir, issuer, selector, Set.class);
        if (userAddedCerts != null) {
            issuers = userAddedCerts;
        }
        Set<X509Certificate> systemCerts = (Set) findCert(this.systemDir, issuer, new CertSelector(this) {
            final /* synthetic */ TrustedCertificateStore this$0;

            public boolean match(X509Certificate ca) {
                try {
                    if (this.this$0.isDeletedSystemCertificate(ca)) {
                        return false;
                    }
                    c.verify(ca.getPublicKey());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }, Set.class);
        if (systemCerts != null) {
            if (issuers != null) {
                issuers.addAll(systemCerts);
            } else {
                issuers = systemCerts;
            }
        }
        return issuers != null ? issuers : Collections.emptySet();
    }

    private static boolean isSelfIssuedCertificate(OpenSSLX509Certificate cert) {
        long ctx = cert.getContext();
        if (NativeCrypto.X509_check_issued(ctx, ctx) == 0) {
            return true;
        }
        return false;
    }

    private static OpenSSLX509Certificate convertToOpenSSLIfNeeded(X509Certificate cert) throws CertificateException {
        if (cert == null) {
            return null;
        }
        if (cert instanceof OpenSSLX509Certificate) {
            return (OpenSSLX509Certificate) cert;
        }
        try {
            return OpenSSLX509Certificate.fromX509Der(cert.getEncoded());
        } catch (Exception e) {
            throw new CertificateException(e);
        }
    }

    public List<X509Certificate> getCertificateChain(X509Certificate leaf) throws CertificateException {
        LinkedHashSet<OpenSSLX509Certificate> chain = new LinkedHashSet();
        OpenSSLX509Certificate cert = convertToOpenSSLIfNeeded(leaf);
        chain.add(cert);
        while (!isSelfIssuedCertificate(cert)) {
            cert = convertToOpenSSLIfNeeded(findIssuer(cert));
            if (cert == null || chain.contains(cert)) {
                break;
            }
            chain.add(cert);
        }
        return new ArrayList(chain);
    }

    private <T> T findCert(File dir, X500Principal subject, CertSelector selector, Class<T> desiredReturnType) {
        T certs = null;
        String hash = hash(subject);
        int index = 0;
        while (true) {
            File file = file(dir, hash, index);
            if (file.isFile()) {
                if (!isTombstone(file)) {
                    X509Certificate cert = readCertificate(file);
                    if (cert != null && selector.match(cert)) {
                        if (desiredReturnType == X509Certificate.class) {
                            return cert;
                        }
                        if (desiredReturnType == Boolean.class) {
                            return Boolean.TRUE;
                        }
                        if (desiredReturnType == File.class) {
                            return file;
                        }
                        if (desiredReturnType == Set.class) {
                            if (certs == null) {
                                certs = new HashSet();
                            }
                            certs.add(cert);
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
                index++;
            } else if (desiredReturnType == Boolean.class) {
                return Boolean.FALSE;
            } else {
                if (desiredReturnType == File.class) {
                    return file;
                }
                if (desiredReturnType == Set.class) {
                    return certs;
                }
                return null;
            }
        }
    }

    private String hash(X500Principal name) {
        return Hex.intToHexString(NativeCrypto.X509_NAME_hash_old(name), 8);
    }

    private File file(File dir, String hash, int index) {
        return new File(dir, hash + '.' + index);
    }

    public void installCertificate(X509Certificate cert) throws IOException, CertificateException {
        if (cert == null) {
            throw new NullPointerException("cert == null");
        } else if (getCertificateFile(this.systemDir, cert).exists()) {
            File deleted = getCertificateFile(this.deletedDir, cert);
            if (deleted.exists() && !deleted.delete()) {
                throw new IOException("Could not remove " + deleted);
            }
        } else {
            File user = getCertificateFile(this.addedDir, cert);
            if (!user.exists()) {
                writeCertificate(user, cert);
            }
        }
    }

    public void deleteCertificateEntry(String alias) throws IOException, CertificateException {
        if (alias != null) {
            File file = fileForAlias(alias);
            if (file != null) {
                if (isSystem(alias)) {
                    X509Certificate cert = readCertificate(file);
                    if (cert != null) {
                        File deleted = getCertificateFile(this.deletedDir, cert);
                        if (!deleted.exists()) {
                            writeCertificate(deleted, cert);
                        }
                    }
                } else if (isUser(alias)) {
                    new FileOutputStream(file).close();
                    removeUnnecessaryTombstones(alias);
                }
            }
        }
    }

    private void removeUnnecessaryTombstones(String alias) throws IOException {
        if (isUser(alias)) {
            int dotIndex = alias.lastIndexOf(46);
            if (dotIndex == -1) {
                throw new AssertionError(alias);
            }
            String hash = alias.substring(PREFIX_USER.length(), dotIndex);
            int lastTombstoneIndex = Integer.parseInt(alias.substring(dotIndex + 1));
            if (!file(this.addedDir, hash, lastTombstoneIndex + 1).exists()) {
                while (lastTombstoneIndex >= 0) {
                    File file = file(this.addedDir, hash, lastTombstoneIndex);
                    if (!isTombstone(file)) {
                        break;
                    } else if (file.delete()) {
                        lastTombstoneIndex--;
                    } else {
                        throw new IOException("Could not remove " + file);
                    }
                }
                return;
            }
            return;
        }
        throw new AssertionError(alias);
    }
}
