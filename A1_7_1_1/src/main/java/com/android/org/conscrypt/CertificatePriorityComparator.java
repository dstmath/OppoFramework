package com.android.org.conscrypt;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

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
public final class CertificatePriorityComparator implements Comparator<X509Certificate> {
    private static final Map<String, Integer> ALGORITHM_OID_PRIORITY_MAP = null;
    private static final Integer PRIORITY_MD5 = null;
    private static final Integer PRIORITY_SHA1 = null;
    private static final Integer PRIORITY_SHA224 = null;
    private static final Integer PRIORITY_SHA256 = null;
    private static final Integer PRIORITY_SHA384 = null;
    private static final Integer PRIORITY_SHA512 = null;
    private static final Integer PRIORITY_UNKNOWN = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.CertificatePriorityComparator.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.CertificatePriorityComparator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.CertificatePriorityComparator.<clinit>():void");
    }

    public int compare(X509Certificate lhs, X509Certificate rhs) {
        boolean lhsSelfSigned = lhs.getSubjectDN().equals(lhs.getIssuerDN());
        boolean rhsSelfSigned = rhs.getSubjectDN().equals(rhs.getIssuerDN());
        if (lhsSelfSigned != rhsSelfSigned) {
            return rhsSelfSigned ? 1 : -1;
        }
        int result = compareStrength(rhs, lhs);
        if (result != 0) {
            return result;
        }
        result = rhs.getNotAfter().compareTo(lhs.getNotAfter());
        if (result != 0) {
            return result;
        }
        return rhs.getNotBefore().compareTo(lhs.getNotBefore());
    }

    private int compareStrength(X509Certificate lhs, X509Certificate rhs) {
        PublicKey lhsPublicKey = lhs.getPublicKey();
        PublicKey rhsPublicKey = rhs.getPublicKey();
        int result = compareKeyAlgorithm(lhsPublicKey, rhsPublicKey);
        if (result != 0) {
            return result;
        }
        result = compareKeySize(lhsPublicKey, rhsPublicKey);
        if (result != 0) {
            return result;
        }
        return compareSignatureAlgorithm(lhs, rhs);
    }

    private int compareKeyAlgorithm(PublicKey lhs, PublicKey rhs) {
        String lhsAlgorithm = lhs.getAlgorithm().toUpperCase(Locale.US);
        if (lhsAlgorithm.equals(rhs.getAlgorithm().toUpperCase(Locale.US))) {
            return 0;
        }
        if ("EC".equals(lhsAlgorithm)) {
            return 1;
        }
        return -1;
    }

    private int compareKeySize(PublicKey lhs, PublicKey rhs) {
        if (lhs.getAlgorithm().toUpperCase(Locale.US).equals(rhs.getAlgorithm().toUpperCase(Locale.US))) {
            return getKeySize(lhs) - getKeySize(rhs);
        }
        throw new IllegalArgumentException("Keys are not of the same type");
    }

    private int getKeySize(PublicKey pkey) {
        if (pkey instanceof ECPublicKey) {
            return ((ECPublicKey) pkey).getParams().getCurve().getField().getFieldSize();
        }
        if (pkey instanceof RSAPublicKey) {
            return ((RSAPublicKey) pkey).getModulus().bitLength();
        }
        throw new IllegalArgumentException("Unsupported public key type: " + pkey.getClass().getName());
    }

    private int compareSignatureAlgorithm(X509Certificate lhs, X509Certificate rhs) {
        Integer lhsPriority = (Integer) ALGORITHM_OID_PRIORITY_MAP.get(lhs.getSigAlgOID());
        Integer rhsPriority = (Integer) ALGORITHM_OID_PRIORITY_MAP.get(rhs.getSigAlgOID());
        if (lhsPriority == null) {
            lhsPriority = PRIORITY_UNKNOWN;
        }
        if (rhsPriority == null) {
            rhsPriority = PRIORITY_UNKNOWN;
        }
        return rhsPriority.intValue() - lhsPriority.intValue();
    }
}
