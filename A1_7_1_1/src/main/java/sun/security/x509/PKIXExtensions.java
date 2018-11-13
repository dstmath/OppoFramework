package sun.security.x509;

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
public class PKIXExtensions {
    public static final ObjectIdentifier AuthInfoAccess_Id = null;
    private static final int[] AuthInfoAccess_data = null;
    public static final ObjectIdentifier AuthorityKey_Id = null;
    private static final int[] AuthorityKey_data = null;
    public static final ObjectIdentifier BasicConstraints_Id = null;
    private static final int[] BasicConstraints_data = null;
    public static final ObjectIdentifier CRLDistributionPoints_Id = null;
    private static final int[] CRLDistributionPoints_data = null;
    public static final ObjectIdentifier CRLNumber_Id = null;
    private static final int[] CRLNumber_data = null;
    public static final ObjectIdentifier CertificateIssuer_Id = null;
    private static final int[] CertificateIssuer_data = null;
    public static final ObjectIdentifier CertificatePolicies_Id = null;
    private static final int[] CertificatePolicies_data = null;
    public static final ObjectIdentifier DeltaCRLIndicator_Id = null;
    private static final int[] DeltaCRLIndicator_data = null;
    public static final ObjectIdentifier ExtendedKeyUsage_Id = null;
    private static final int[] ExtendedKeyUsage_data = null;
    public static final ObjectIdentifier FreshestCRL_Id = null;
    private static final int[] FreshestCRL_data = null;
    public static final ObjectIdentifier HoldInstructionCode_Id = null;
    private static final int[] HoldInstructionCode_data = null;
    public static final ObjectIdentifier InhibitAnyPolicy_Id = null;
    private static final int[] InhibitAnyPolicy_data = null;
    public static final ObjectIdentifier InvalidityDate_Id = null;
    private static final int[] InvalidityDate_data = null;
    public static final ObjectIdentifier IssuerAlternativeName_Id = null;
    private static final int[] IssuerAlternativeName_data = null;
    public static final ObjectIdentifier IssuingDistributionPoint_Id = null;
    private static final int[] IssuingDistributionPoint_data = null;
    public static final ObjectIdentifier KeyUsage_Id = null;
    private static final int[] KeyUsage_data = null;
    public static final ObjectIdentifier NameConstraints_Id = null;
    private static final int[] NameConstraints_data = null;
    public static final ObjectIdentifier OCSPNoCheck_Id = null;
    private static final int[] OCSPNoCheck_data = null;
    public static final ObjectIdentifier PolicyConstraints_Id = null;
    private static final int[] PolicyConstraints_data = null;
    public static final ObjectIdentifier PolicyMappings_Id = null;
    private static final int[] PolicyMappings_data = null;
    public static final ObjectIdentifier PrivateKeyUsage_Id = null;
    private static final int[] PrivateKeyUsage_data = null;
    public static final ObjectIdentifier ReasonCode_Id = null;
    private static final int[] ReasonCode_data = null;
    public static final ObjectIdentifier SubjectAlternativeName_Id = null;
    private static final int[] SubjectAlternativeName_data = null;
    public static final ObjectIdentifier SubjectDirectoryAttributes_Id = null;
    private static final int[] SubjectDirectoryAttributes_data = null;
    public static final ObjectIdentifier SubjectInfoAccess_Id = null;
    private static final int[] SubjectInfoAccess_data = null;
    public static final ObjectIdentifier SubjectKey_Id = null;
    private static final int[] SubjectKey_data = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.x509.PKIXExtensions.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.x509.PKIXExtensions.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.PKIXExtensions.<clinit>():void");
    }
}
