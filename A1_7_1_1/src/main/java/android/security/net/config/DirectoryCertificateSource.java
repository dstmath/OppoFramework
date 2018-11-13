package android.security.net.config;

import android.util.ArraySet;
import com.android.org.conscrypt.Hex;
import com.android.org.conscrypt.NativeCrypto;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import libcore.io.IoUtils;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
abstract class DirectoryCertificateSource implements CertificateSource {
    private static final String LOG_TAG = "DirectoryCertificateSrc";
    private final CertificateFactory mCertFactory;
    private Set<X509Certificate> mCertificates;
    private final File mDir;
    private final Object mLock;

    private interface CertSelector {
        boolean match(X509Certificate x509Certificate);
    }

    /* renamed from: android.security.net.config.DirectoryCertificateSource$2 */
    class AnonymousClass2 implements CertSelector {
        final /* synthetic */ DirectoryCertificateSource this$0;
        final /* synthetic */ X509Certificate val$cert;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.security.net.config.DirectoryCertificateSource.2.<init>(android.security.net.config.DirectoryCertificateSource, java.security.cert.X509Certificate):void, dex: 
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
        AnonymousClass2(android.security.net.config.DirectoryCertificateSource r1, java.security.cert.X509Certificate r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.security.net.config.DirectoryCertificateSource.2.<init>(android.security.net.config.DirectoryCertificateSource, java.security.cert.X509Certificate):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.DirectoryCertificateSource.2.<init>(android.security.net.config.DirectoryCertificateSource, java.security.cert.X509Certificate):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.DirectoryCertificateSource.2.match(java.security.cert.X509Certificate):boolean, dex: 
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
        public boolean match(java.security.cert.X509Certificate r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.net.config.DirectoryCertificateSource.2.match(java.security.cert.X509Certificate):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.DirectoryCertificateSource.2.match(java.security.cert.X509Certificate):boolean");
        }
    }

    protected abstract boolean isCertMarkedAsRemoved(String str);

    protected DirectoryCertificateSource(File caDir) {
        this.mLock = new Object();
        this.mDir = caDir;
        try {
            this.mCertFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("Failed to obtain X.509 CertificateFactory", e);
        }
    }

    public Set<X509Certificate> getCertificates() {
        synchronized (this.mLock) {
            if (this.mCertificates != null) {
                return this.mCertificates;
            }
            Set<X509Certificate> certs = new ArraySet();
            if (this.mDir.isDirectory()) {
                for (String caFile : this.mDir.list()) {
                    if (!isCertMarkedAsRemoved(caFile)) {
                        X509Certificate cert = readCertificate(caFile);
                        if (cert != null) {
                            certs.add(cert);
                        }
                    }
                }
            }
            this.mCertificates = certs;
            return this.mCertificates;
        }
    }

    public X509Certificate findBySubjectAndPublicKey(final X509Certificate cert) {
        return findCert(cert.getSubjectX500Principal(), new CertSelector() {
            public boolean match(X509Certificate ca) {
                return ca.getPublicKey().equals(cert.getPublicKey());
            }
        });
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
    public java.security.cert.X509Certificate findByIssuerAndSignature(java.security.cert.X509Certificate r3) {
        /*
        r2 = this;
        r0 = r3.getIssuerX500Principal();
        r1 = new android.security.net.config.DirectoryCertificateSource$2;
        r1.<init>(r2, r3);
        r0 = r2.findCert(r0, r1);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.DirectoryCertificateSource.findByIssuerAndSignature(java.security.cert.X509Certificate):java.security.cert.X509Certificate");
    }

    public Set<X509Certificate> findAllByIssuerAndSignature(final X509Certificate cert) {
        return findCerts(cert.getIssuerX500Principal(), new CertSelector(this) {
            final /* synthetic */ DirectoryCertificateSource this$0;

            public boolean match(X509Certificate ca) {
                try {
                    cert.verify(ca.getPublicKey());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }

    public void handleTrustStorageUpdate() {
        synchronized (this.mLock) {
            this.mCertificates = null;
        }
    }

    private Set<X509Certificate> findCerts(X500Principal subj, CertSelector selector) {
        String hash = getHash(subj);
        Set<X509Certificate> certs = null;
        for (int index = 0; index >= 0; index++) {
            String fileName = hash + "." + index;
            if (!new File(this.mDir, fileName).exists()) {
                break;
            }
            if (!isCertMarkedAsRemoved(fileName)) {
                X509Certificate cert = readCertificate(fileName);
                if (cert != null && subj.equals(cert.getSubjectX500Principal()) && selector.match(cert)) {
                    if (certs == null) {
                        certs = new ArraySet();
                    }
                    certs.add(cert);
                }
            }
        }
        if (certs != null) {
            return certs;
        }
        return Collections.emptySet();
    }

    private X509Certificate findCert(X500Principal subj, CertSelector selector) {
        String hash = getHash(subj);
        for (int index = 0; index >= 0; index++) {
            String fileName = hash + "." + index;
            if (!new File(this.mDir, fileName).exists()) {
                break;
            }
            if (!isCertMarkedAsRemoved(fileName)) {
                X509Certificate cert = readCertificate(fileName);
                if (cert != null && subj.equals(cert.getSubjectX500Principal()) && selector.match(cert)) {
                    return cert;
                }
            }
        }
        return null;
    }

    private String getHash(X500Principal name) {
        return Hex.intToHexString(NativeCrypto.X509_NAME_hash_old(name), 8);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0046 A:{ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException), Splitter: B:3:0x0012} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x001e A:{ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException), Splitter: B:1:0x0001} */
    /* JADX WARNING: Missing block: B:7:0x001e, code:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            android.util.Log.e(LOG_TAG, "Failed to read certificate from " + r7, r0);
     */
    /* JADX WARNING: Missing block: B:10:0x0039, code:
            libcore.io.IoUtils.closeQuietly(r1);
     */
    /* JADX WARNING: Missing block: B:11:0x003d, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:12:0x003e, code:
            r3 = th;
     */
    /* JADX WARNING: Missing block: B:17:0x0046, code:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:18:0x0047, code:
            r1 = r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private X509Certificate readCertificate(String file) {
        AutoCloseable is = null;
        try {
            InputStream is2 = new BufferedInputStream(new FileInputStream(new File(this.mDir, file)));
            try {
                X509Certificate x509Certificate = (X509Certificate) this.mCertFactory.generateCertificate(is2);
                IoUtils.closeQuietly(is2);
                return x509Certificate;
            } catch (CertificateException e) {
            } catch (Throwable th) {
                Throwable th2 = th;
                Object is3 = is2;
                IoUtils.closeQuietly(is);
                throw th2;
            }
        } catch (CertificateException e2) {
        }
    }
}
