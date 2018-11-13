package sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X509CertImpl;

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
class ConstraintsChecker extends PKIXCertPathChecker {
    private static final Debug debug = null;
    private final int certPathLength;
    private int i;
    private int maxPathLength;
    private NameConstraintsExtension prevNC;
    private Set<String> supportedExts;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.provider.certpath.ConstraintsChecker.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.provider.certpath.ConstraintsChecker.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.ConstraintsChecker.<clinit>():void");
    }

    ConstraintsChecker(int certPathLength) {
        this.certPathLength = certPathLength;
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.i = 0;
        this.maxPathLength = this.certPathLength;
        this.prevNC = null;
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        if (this.supportedExts == null) {
            this.supportedExts = new HashSet(2);
            this.supportedExts.add(PKIXExtensions.BasicConstraints_Id.toString());
            this.supportedExts.add(PKIXExtensions.NameConstraints_Id.toString());
            this.supportedExts = Collections.unmodifiableSet(this.supportedExts);
        }
        return this.supportedExts;
    }

    public void check(Certificate cert, Collection<String> unresCritExts) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate) cert;
        this.i++;
        checkBasicConstraints(currCert);
        verifyNameConstraints(currCert);
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.BasicConstraints_Id.toString());
            unresCritExts.remove(PKIXExtensions.NameConstraints_Id.toString());
        }
    }

    private void verifyNameConstraints(X509Certificate currCert) throws CertPathValidatorException {
        String msg = "name constraints";
        if (debug != null) {
            debug.println("---checking " + msg + "...");
        }
        if (this.prevNC != null && (this.i == this.certPathLength || !X509CertImpl.isSelfIssued(currCert))) {
            if (debug != null) {
                debug.println("prevNC = " + this.prevNC);
                debug.println("currDN = " + currCert.getSubjectX500Principal());
            }
            try {
                if (!this.prevNC.verify(currCert)) {
                    throw new CertPathValidatorException(msg + " check failed", null, null, -1, PKIXReason.INVALID_NAME);
                }
            } catch (Throwable ioe) {
                throw new CertPathValidatorException(ioe);
            }
        }
        this.prevNC = mergeNameConstraints(currCert, this.prevNC);
        if (debug != null) {
            debug.println(msg + " verified.");
        }
    }

    static NameConstraintsExtension mergeNameConstraints(X509Certificate currCert, NameConstraintsExtension prevNC) throws CertPathValidatorException {
        try {
            Object newConstraints = X509CertImpl.toImpl(currCert).getNameConstraintsExtension();
            if (debug != null) {
                debug.println("prevNC = " + prevNC);
                debug.println("newNC = " + String.valueOf(newConstraints));
            }
            if (prevNC == null) {
                if (debug != null) {
                    debug.println("mergedNC = " + String.valueOf(newConstraints));
                }
                if (newConstraints == null) {
                    return newConstraints;
                }
                return (NameConstraintsExtension) newConstraints.clone();
            }
            try {
                prevNC.merge(newConstraints);
                if (debug != null) {
                    debug.println("mergedNC = " + prevNC);
                }
                return prevNC;
            } catch (Throwable ioe) {
                throw new CertPathValidatorException(ioe);
            }
        } catch (Throwable ce) {
            throw new CertPathValidatorException(ce);
        }
    }

    private void checkBasicConstraints(X509Certificate currCert) throws CertPathValidatorException {
        String msg = "basic constraints";
        if (debug != null) {
            debug.println("---checking " + msg + "...");
            debug.println("i = " + this.i);
            debug.println("maxPathLength = " + this.maxPathLength);
        }
        if (this.i < this.certPathLength) {
            int pathLenConstraint = -1;
            if (currCert.getVersion() >= 3) {
                pathLenConstraint = currCert.getBasicConstraints();
            } else if (this.i == 1 && X509CertImpl.isSelfIssued(currCert)) {
                pathLenConstraint = Integer.MAX_VALUE;
            }
            if (pathLenConstraint == -1) {
                throw new CertPathValidatorException(msg + " check failed: this is not a CA certificate", null, null, -1, PKIXReason.NOT_CA_CERT);
            }
            if (!X509CertImpl.isSelfIssued(currCert)) {
                if (this.maxPathLength <= 0) {
                    throw new CertPathValidatorException(msg + " check failed: pathLenConstraint violated - " + "this cert must be the last cert in the " + "certification path", null, null, -1, PKIXReason.PATH_TOO_LONG);
                }
                this.maxPathLength--;
            }
            if (pathLenConstraint < this.maxPathLength) {
                this.maxPathLength = pathLenConstraint;
            }
        }
        if (debug != null) {
            debug.println("after processing, maxPathLength = " + this.maxPathLength);
            debug.println(msg + " verified.");
        }
    }

    static int mergeBasicConstraints(X509Certificate cert, int maxPathLength) {
        int pathLenConstraint = cert.getBasicConstraints();
        if (!X509CertImpl.isSelfIssued(cert)) {
            maxPathLength--;
        }
        if (pathLenConstraint < maxPathLength) {
            return pathLenConstraint;
        }
        return maxPathLength;
    }
}
