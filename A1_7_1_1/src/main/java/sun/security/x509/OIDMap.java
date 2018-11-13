package sun.security.x509;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Map;
import sun.security.util.ObjectIdentifier;

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
public class OIDMap {
    private static final String AUTH_INFO_ACCESS = "x509.info.extensions.AuthorityInfoAccess";
    private static final String AUTH_KEY_IDENTIFIER = "x509.info.extensions.AuthorityKeyIdentifier";
    private static final String BASIC_CONSTRAINTS = "x509.info.extensions.BasicConstraints";
    private static final String CERT_ISSUER = "x509.info.extensions.CertificateIssuer";
    private static final String CERT_POLICIES = "x509.info.extensions.CertificatePolicies";
    private static final String CRL_DIST_POINTS = "x509.info.extensions.CRLDistributionPoints";
    private static final String CRL_NUMBER = "x509.info.extensions.CRLNumber";
    private static final String CRL_REASON = "x509.info.extensions.CRLReasonCode";
    private static final String DELTA_CRL_INDICATOR = "x509.info.extensions.DeltaCRLIndicator";
    private static final String EXT_KEY_USAGE = "x509.info.extensions.ExtendedKeyUsage";
    private static final String FRESHEST_CRL = "x509.info.extensions.FreshestCRL";
    private static final String INHIBIT_ANY_POLICY = "x509.info.extensions.InhibitAnyPolicy";
    private static final String ISSUER_ALT_NAME = "x509.info.extensions.IssuerAlternativeName";
    private static final String ISSUING_DIST_POINT = "x509.info.extensions.IssuingDistributionPoint";
    private static final String KEY_USAGE = "x509.info.extensions.KeyUsage";
    private static final String NAME_CONSTRAINTS = "x509.info.extensions.NameConstraints";
    private static final String NETSCAPE_CERT = "x509.info.extensions.NetscapeCertType";
    private static final int[] NetscapeCertType_data = null;
    private static final String OCSPNOCHECK = "x509.info.extensions.OCSPNoCheck";
    private static final String POLICY_CONSTRAINTS = "x509.info.extensions.PolicyConstraints";
    private static final String POLICY_MAPPINGS = "x509.info.extensions.PolicyMappings";
    private static final String PRIVATE_KEY_USAGE = "x509.info.extensions.PrivateKeyUsage";
    private static final String ROOT = "x509.info.extensions";
    private static final String SUBJECT_INFO_ACCESS = "x509.info.extensions.SubjectInfoAccess";
    private static final String SUB_ALT_NAME = "x509.info.extensions.SubjectAlternativeName";
    private static final String SUB_KEY_IDENTIFIER = "x509.info.extensions.SubjectKeyIdentifier";
    private static final Map<String, OIDInfo> nameMap = null;
    private static final Map<ObjectIdentifier, OIDInfo> oidMap = null;

    private static class OIDInfo {
        private final Class clazz;
        final String name;
        final ObjectIdentifier oid;

        OIDInfo(String name, ObjectIdentifier oid, Class clazz) {
            this.name = name;
            this.oid = oid;
            this.clazz = clazz;
        }

        Class getClazz() throws CertificateException {
            return this.clazz;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.x509.OIDMap.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.x509.OIDMap.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.OIDMap.<clinit>():void");
    }

    private OIDMap() {
    }

    private static void addInternal(String name, ObjectIdentifier oid, Class clazz) {
        OIDInfo info = new OIDInfo(name, oid, clazz);
        oidMap.put(oid, info);
        nameMap.put(name, info);
    }

    public static void addAttribute(String name, String oid, Class clazz) throws CertificateException {
        try {
            ObjectIdentifier objId = new ObjectIdentifier(oid);
            OIDInfo info = new OIDInfo(name, objId, clazz);
            if (oidMap.put(objId, info) != null) {
                throw new CertificateException("Object identifier already exists: " + oid);
            } else if (nameMap.put(name, info) != null) {
                throw new CertificateException("Name already exists: " + name);
            }
        } catch (IOException e) {
            throw new CertificateException("Invalid Object identifier: " + oid);
        }
    }

    public static String getName(ObjectIdentifier oid) {
        OIDInfo info = (OIDInfo) oidMap.get(oid);
        if (info == null) {
            return null;
        }
        return info.name;
    }

    public static ObjectIdentifier getOID(String name) {
        OIDInfo info = (OIDInfo) nameMap.get(name);
        if (info == null) {
            return null;
        }
        return info.oid;
    }

    public static Class getClass(String name) throws CertificateException {
        OIDInfo info = (OIDInfo) nameMap.get(name);
        if (info == null) {
            return null;
        }
        return info.getClazz();
    }

    public static Class getClass(ObjectIdentifier oid) throws CertificateException {
        OIDInfo info = (OIDInfo) oidMap.get(oid);
        if (info == null) {
            return null;
        }
        return info.getClazz();
    }
}
