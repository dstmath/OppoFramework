package com.android.org.bouncycastle.jce.provider;

import java.security.cert.X509Certificate;

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
class RFC3280CertPathUtilities {
    public static final String ANY_POLICY = "2.5.29.32.0";
    public static final String AUTHORITY_KEY_IDENTIFIER = null;
    public static final String BASIC_CONSTRAINTS = null;
    public static final String CERTIFICATE_POLICIES = null;
    public static final String CRL_DISTRIBUTION_POINTS = null;
    public static final String CRL_NUMBER = null;
    protected static final int CRL_SIGN = 6;
    private static final PKIXCRLUtil CRL_UTIL = null;
    public static final String DELTA_CRL_INDICATOR = null;
    public static final String FRESHEST_CRL = null;
    public static final String INHIBIT_ANY_POLICY = null;
    public static final String ISSUING_DISTRIBUTION_POINT = null;
    protected static final int KEY_CERT_SIGN = 5;
    public static final String KEY_USAGE = null;
    public static final String NAME_CONSTRAINTS = null;
    public static final String POLICY_CONSTRAINTS = null;
    public static final String POLICY_MAPPINGS = null;
    public static final String SUBJECT_ALTERNATIVE_NAME = null;
    protected static final String[] crlReasons = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.<init>():void, dex: 
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
    RFC3280CertPathUtilities() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.checkCRL(com.android.org.bouncycastle.asn1.x509.DistributionPoint, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.util.Date, java.security.cert.X509Certificate, java.security.PublicKey, com.android.org.bouncycastle.jce.provider.CertStatus, com.android.org.bouncycastle.jce.provider.ReasonsMask, java.util.List, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static void checkCRL(com.android.org.bouncycastle.asn1.x509.DistributionPoint r1, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters r2, java.security.cert.X509Certificate r3, java.util.Date r4, java.security.cert.X509Certificate r5, java.security.PublicKey r6, com.android.org.bouncycastle.jce.provider.CertStatus r7, com.android.org.bouncycastle.jce.provider.ReasonsMask r8, java.util.List r9, com.android.org.bouncycastle.jcajce.util.JcaJceHelper r10) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.checkCRL(com.android.org.bouncycastle.asn1.x509.DistributionPoint, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.util.Date, java.security.cert.X509Certificate, java.security.PublicKey, com.android.org.bouncycastle.jce.provider.CertStatus, com.android.org.bouncycastle.jce.provider.ReasonsMask, java.util.List, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.checkCRL(com.android.org.bouncycastle.asn1.x509.DistributionPoint, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.util.Date, java.security.cert.X509Certificate, java.security.PublicKey, com.android.org.bouncycastle.jce.provider.CertStatus, com.android.org.bouncycastle.jce.provider.ReasonsMask, java.util.List, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.checkCRLs(com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.util.Date, java.security.cert.X509Certificate, java.security.PublicKey, java.util.List, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void checkCRLs(com.android.org.bouncycastle.jcajce.PKIXExtendedParameters r1, java.security.cert.X509Certificate r2, java.util.Date r3, java.security.cert.X509Certificate r4, java.security.PublicKey r5, java.util.List r6, com.android.org.bouncycastle.jcajce.util.JcaJceHelper r7) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.checkCRLs(com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.util.Date, java.security.cert.X509Certificate, java.security.PublicKey, java.util.List, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.checkCRLs(com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.util.Date, java.security.cert.X509Certificate, java.security.PublicKey, java.util.List, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareCertB(java.security.cert.CertPath, int, java.util.List[], com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, int):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static com.android.org.bouncycastle.jce.provider.PKIXPolicyNode prepareCertB(java.security.cert.CertPath r1, int r2, java.util.List[] r3, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode r4, int r5) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareCertB(java.security.cert.CertPath, int, java.util.List[], com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, int):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareCertB(java.security.cert.CertPath, int, java.util.List[], com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, int):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertA(java.security.cert.CertPath, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void prepareNextCertA(java.security.cert.CertPath r1, int r2) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertA(java.security.cert.CertPath, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertA(java.security.cert.CertPath, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertG(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXNameConstraintValidator):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void prepareNextCertG(java.security.cert.CertPath r1, int r2, com.android.org.bouncycastle.jce.provider.PKIXNameConstraintValidator r3) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertG(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXNameConstraintValidator):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertG(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXNameConstraintValidator):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertH1(java.security.cert.CertPath, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static int prepareNextCertH1(java.security.cert.CertPath r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertH1(java.security.cert.CertPath, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertH1(java.security.cert.CertPath, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertH2(java.security.cert.CertPath, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static int prepareNextCertH2(java.security.cert.CertPath r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertH2(java.security.cert.CertPath, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertH2(java.security.cert.CertPath, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertH3(java.security.cert.CertPath, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static int prepareNextCertH3(java.security.cert.CertPath r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertH3(java.security.cert.CertPath, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertH3(java.security.cert.CertPath, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertI1(java.security.cert.CertPath, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static int prepareNextCertI1(java.security.cert.CertPath r1, int r2, int r3) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertI1(java.security.cert.CertPath, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertI1(java.security.cert.CertPath, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertI2(java.security.cert.CertPath, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static int prepareNextCertI2(java.security.cert.CertPath r1, int r2, int r3) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertI2(java.security.cert.CertPath, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertI2(java.security.cert.CertPath, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertJ(java.security.cert.CertPath, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static int prepareNextCertJ(java.security.cert.CertPath r1, int r2, int r3) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertJ(java.security.cert.CertPath, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertJ(java.security.cert.CertPath, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertK(java.security.cert.CertPath, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void prepareNextCertK(java.security.cert.CertPath r1, int r2) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertK(java.security.cert.CertPath, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertK(java.security.cert.CertPath, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertL(java.security.cert.CertPath, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static int prepareNextCertL(java.security.cert.CertPath r1, int r2, int r3) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertL(java.security.cert.CertPath, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertL(java.security.cert.CertPath, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertM(java.security.cert.CertPath, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static int prepareNextCertM(java.security.cert.CertPath r1, int r2, int r3) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertM(java.security.cert.CertPath, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertM(java.security.cert.CertPath, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertN(java.security.cert.CertPath, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void prepareNextCertN(java.security.cert.CertPath r1, int r2) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertN(java.security.cert.CertPath, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertN(java.security.cert.CertPath, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertO(java.security.cert.CertPath, int, java.util.Set, java.util.List):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void prepareNextCertO(java.security.cert.CertPath r1, int r2, java.util.Set r3, java.util.List r4) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertO(java.security.cert.CertPath, int, java.util.Set, java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.prepareNextCertO(java.security.cert.CertPath, int, java.util.Set, java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLA1i(java.util.Date, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.security.cert.X509CRL):java.util.Set, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static java.util.Set processCRLA1i(java.util.Date r1, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters r2, java.security.cert.X509Certificate r3, java.security.cert.X509CRL r4) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLA1i(java.util.Date, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.security.cert.X509CRL):java.util.Set, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLA1i(java.util.Date, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.security.cert.X509CRL):java.util.Set");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLA1ii(java.util.Date, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.security.cert.X509CRL):java.util.Set[], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static java.util.Set[] processCRLA1ii(java.util.Date r1, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters r2, java.security.cert.X509Certificate r3, java.security.cert.X509CRL r4) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLA1ii(java.util.Date, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.security.cert.X509CRL):java.util.Set[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLA1ii(java.util.Date, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.security.cert.X509Certificate, java.security.cert.X509CRL):java.util.Set[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLB1(com.android.org.bouncycastle.asn1.x509.DistributionPoint, java.lang.Object, java.security.cert.X509CRL):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void processCRLB1(com.android.org.bouncycastle.asn1.x509.DistributionPoint r1, java.lang.Object r2, java.security.cert.X509CRL r3) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLB1(com.android.org.bouncycastle.asn1.x509.DistributionPoint, java.lang.Object, java.security.cert.X509CRL):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLB1(com.android.org.bouncycastle.asn1.x509.DistributionPoint, java.lang.Object, java.security.cert.X509CRL):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLB2(com.android.org.bouncycastle.asn1.x509.DistributionPoint, java.lang.Object, java.security.cert.X509CRL):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void processCRLB2(com.android.org.bouncycastle.asn1.x509.DistributionPoint r1, java.lang.Object r2, java.security.cert.X509CRL r3) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLB2(com.android.org.bouncycastle.asn1.x509.DistributionPoint, java.lang.Object, java.security.cert.X509CRL):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLB2(com.android.org.bouncycastle.asn1.x509.DistributionPoint, java.lang.Object, java.security.cert.X509CRL):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLC(java.security.cert.X509CRL, java.security.cert.X509CRL, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters):void, dex: 
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
    protected static void processCRLC(java.security.cert.X509CRL r1, java.security.cert.X509CRL r2, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters r3) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLC(java.security.cert.X509CRL, java.security.cert.X509CRL, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLC(java.security.cert.X509CRL, java.security.cert.X509CRL, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLD(java.security.cert.X509CRL, com.android.org.bouncycastle.asn1.x509.DistributionPoint):com.android.org.bouncycastle.jce.provider.ReasonsMask, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static com.android.org.bouncycastle.jce.provider.ReasonsMask processCRLD(java.security.cert.X509CRL r1, com.android.org.bouncycastle.asn1.x509.DistributionPoint r2) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLD(java.security.cert.X509CRL, com.android.org.bouncycastle.asn1.x509.DistributionPoint):com.android.org.bouncycastle.jce.provider.ReasonsMask, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLD(java.security.cert.X509CRL, com.android.org.bouncycastle.asn1.x509.DistributionPoint):com.android.org.bouncycastle.jce.provider.ReasonsMask");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLF(java.security.cert.X509CRL, java.lang.Object, java.security.cert.X509Certificate, java.security.PublicKey, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.util.List, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):java.util.Set, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static java.util.Set processCRLF(java.security.cert.X509CRL r1, java.lang.Object r2, java.security.cert.X509Certificate r3, java.security.PublicKey r4, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters r5, java.util.List r6, com.android.org.bouncycastle.jcajce.util.JcaJceHelper r7) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLF(java.security.cert.X509CRL, java.lang.Object, java.security.cert.X509Certificate, java.security.PublicKey, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.util.List, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):java.util.Set, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLF(java.security.cert.X509CRL, java.lang.Object, java.security.cert.X509Certificate, java.security.PublicKey, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.util.List, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):java.util.Set");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLG(java.security.cert.X509CRL, java.util.Set):java.security.PublicKey, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static java.security.PublicKey processCRLG(java.security.cert.X509CRL r1, java.util.Set r2) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLG(java.security.cert.X509CRL, java.util.Set):java.security.PublicKey, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLG(java.security.cert.X509CRL, java.util.Set):java.security.PublicKey");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLH(java.util.Set, java.security.PublicKey):java.security.cert.X509CRL, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static java.security.cert.X509CRL processCRLH(java.util.Set r1, java.security.PublicKey r2) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLH(java.util.Set, java.security.PublicKey):java.security.cert.X509CRL, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLH(java.util.Set, java.security.PublicKey):java.security.cert.X509CRL");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLI(java.util.Date, java.security.cert.X509CRL, java.lang.Object, com.android.org.bouncycastle.jce.provider.CertStatus, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void processCRLI(java.util.Date r1, java.security.cert.X509CRL r2, java.lang.Object r3, com.android.org.bouncycastle.jce.provider.CertStatus r4, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters r5) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLI(java.util.Date, java.security.cert.X509CRL, java.lang.Object, com.android.org.bouncycastle.jce.provider.CertStatus, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLI(java.util.Date, java.security.cert.X509CRL, java.lang.Object, com.android.org.bouncycastle.jce.provider.CertStatus, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLJ(java.util.Date, java.security.cert.X509CRL, java.lang.Object, com.android.org.bouncycastle.jce.provider.CertStatus):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void processCRLJ(java.util.Date r1, java.security.cert.X509CRL r2, java.lang.Object r3, com.android.org.bouncycastle.jce.provider.CertStatus r4) throws com.android.org.bouncycastle.jce.provider.AnnotatedException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLJ(java.util.Date, java.security.cert.X509CRL, java.lang.Object, com.android.org.bouncycastle.jce.provider.CertStatus):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCRLJ(java.util.Date, java.security.cert.X509CRL, java.lang.Object, com.android.org.bouncycastle.jce.provider.CertStatus):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertA(java.security.cert.CertPath, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, int, java.security.PublicKey, boolean, com.android.org.bouncycastle.asn1.x500.X500Name, java.security.cert.X509Certificate, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void processCertA(java.security.cert.CertPath r1, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters r2, int r3, java.security.PublicKey r4, boolean r5, com.android.org.bouncycastle.asn1.x500.X500Name r6, java.security.cert.X509Certificate r7, com.android.org.bouncycastle.jcajce.util.JcaJceHelper r8) throws com.android.org.bouncycastle.jce.exception.ExtCertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertA(java.security.cert.CertPath, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, int, java.security.PublicKey, boolean, com.android.org.bouncycastle.asn1.x500.X500Name, java.security.cert.X509Certificate, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertA(java.security.cert.CertPath, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, int, java.security.PublicKey, boolean, com.android.org.bouncycastle.asn1.x500.X500Name, java.security.cert.X509Certificate, com.android.org.bouncycastle.jcajce.util.JcaJceHelper):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertBC(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXNameConstraintValidator):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void processCertBC(java.security.cert.CertPath r1, int r2, com.android.org.bouncycastle.jce.provider.PKIXNameConstraintValidator r3) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertBC(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXNameConstraintValidator):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertBC(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXNameConstraintValidator):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertD(java.security.cert.CertPath, int, java.util.Set, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, java.util.List[], int):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static com.android.org.bouncycastle.jce.provider.PKIXPolicyNode processCertD(java.security.cert.CertPath r1, int r2, java.util.Set r3, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode r4, java.util.List[] r5, int r6) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertD(java.security.cert.CertPath, int, java.util.Set, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, java.util.List[], int):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertD(java.security.cert.CertPath, int, java.util.Set, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, java.util.List[], int):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertE(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static com.android.org.bouncycastle.jce.provider.PKIXPolicyNode processCertE(java.security.cert.CertPath r1, int r2, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode r3) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertE(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertE(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertF(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, int):void, dex: 
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
    protected static void processCertF(java.security.cert.CertPath r1, int r2, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode r3, int r4) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertF(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.processCertF(java.security.cert.CertPath, int, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.wrapupCertB(java.security.cert.CertPath, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static int wrapupCertB(java.security.cert.CertPath r1, int r2, int r3) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.wrapupCertB(java.security.cert.CertPath, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.wrapupCertB(java.security.cert.CertPath, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.wrapupCertF(java.security.cert.CertPath, int, java.util.List, java.util.Set):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static void wrapupCertF(java.security.cert.CertPath r1, int r2, java.util.List r3, java.util.Set r4) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.wrapupCertF(java.security.cert.CertPath, int, java.util.List, java.util.Set):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.wrapupCertF(java.security.cert.CertPath, int, java.util.List, java.util.Set):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.wrapupCertG(java.security.cert.CertPath, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.util.Set, int, java.util.List[], com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, java.util.Set):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected static com.android.org.bouncycastle.jce.provider.PKIXPolicyNode wrapupCertG(java.security.cert.CertPath r1, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters r2, java.util.Set r3, int r4, java.util.List[] r5, com.android.org.bouncycastle.jce.provider.PKIXPolicyNode r6, java.util.Set r7) throws java.security.cert.CertPathValidatorException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.wrapupCertG(java.security.cert.CertPath, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.util.Set, int, java.util.List[], com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, java.util.Set):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.RFC3280CertPathUtilities.wrapupCertG(java.security.cert.CertPath, com.android.org.bouncycastle.jcajce.PKIXExtendedParameters, java.util.Set, int, java.util.List[], com.android.org.bouncycastle.jce.provider.PKIXPolicyNode, java.util.Set):com.android.org.bouncycastle.jce.provider.PKIXPolicyNode");
    }

    protected static int wrapupCertA(int explicitPolicy, X509Certificate cert) {
        if (CertPathValidatorUtilities.isSelfIssued(cert) || explicitPolicy == 0) {
            return explicitPolicy;
        }
        return explicitPolicy - 1;
    }
}
