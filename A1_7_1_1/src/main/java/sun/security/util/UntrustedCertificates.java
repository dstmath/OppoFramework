package sun.security.util;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Set;

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
public final class UntrustedCertificates {
    private static final Set<X509Certificate> untrustedCerts = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.util.UntrustedCertificates.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.util.UntrustedCertificates.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.UntrustedCertificates.<clinit>():void");
    }

    public static boolean isUntrusted(X509Certificate cert) {
        return untrustedCerts.contains(cert);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0047 A:{SYNTHETIC, Splitter: B:15:0x0047} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0080 A:{Catch:{ CertificateException -> 0x004d, CertificateException -> 0x004d }} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x004c A:{SYNTHETIC, Splitter: B:18:0x004c} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x004d A:{ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException), Splitter: B:18:0x004c} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0070 A:{ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException), Splitter: B:27:0x006f} */
    /* JADX WARNING: Missing block: B:20:0x004d, code:
            r2 = e;
     */
    /* JADX WARNING: Missing block: B:22:0x0067, code:
            throw new java.lang.RuntimeException("Incorrect untrusted certificate: " + r10, r2);
     */
    /* JADX WARNING: Missing block: B:29:0x0070, code:
            r2 = e;
     */
    /* JADX WARNING: Missing block: B:30:0x0071, code:
            r3 = r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void add(String alias, String pemCert) {
        Throwable th;
        Throwable th2 = null;
        ByteArrayInputStream is = null;
        try {
            ByteArrayInputStream is2 = new ByteArrayInputStream(pemCert.getBytes());
            try {
                X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is2);
                if (untrustedCerts.add(cert)) {
                    if (is2 != null) {
                        try {
                            is2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (CertificateException e) {
                        }
                    } else {
                        return;
                    }
                }
                throw new RuntimeException("Duplicate untrusted certificate: " + cert.getSubjectX500Principal());
            } catch (Throwable th4) {
                th = th4;
                is = is2;
                if (is != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable th5) {
            th = th5;
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 == null) {
                try {
                    throw th2;
                } catch (CertificateException e2) {
                }
            } else {
                throw th;
            }
        }
    }
}
