package com.android.org.bouncycastle.jce.provider;

import java.security.cert.CertPathBuilderSpi;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class PKIXCertPathBuilderSpi extends CertPathBuilderSpi {
    private Exception certPathException;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi.<init>():void, dex: 
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
    public PKIXCertPathBuilderSpi() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi.build(java.security.cert.X509Certificate, com.android.org.bouncycastle.jcajce.PKIXExtendedBuilderParameters, java.util.List):java.security.cert.CertPathBuilderResult, dex: 
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
    protected java.security.cert.CertPathBuilderResult build(java.security.cert.X509Certificate r1, com.android.org.bouncycastle.jcajce.PKIXExtendedBuilderParameters r2, java.util.List r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi.build(java.security.cert.X509Certificate, com.android.org.bouncycastle.jcajce.PKIXExtendedBuilderParameters, java.util.List):java.security.cert.CertPathBuilderResult, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi.build(java.security.cert.X509Certificate, com.android.org.bouncycastle.jcajce.PKIXExtendedBuilderParameters, java.util.List):java.security.cert.CertPathBuilderResult");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi.engineBuild(java.security.cert.CertPathParameters):java.security.cert.CertPathBuilderResult, dex: 
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
    public java.security.cert.CertPathBuilderResult engineBuild(java.security.cert.CertPathParameters r1) throws java.security.cert.CertPathBuilderException, java.security.InvalidAlgorithmParameterException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi.engineBuild(java.security.cert.CertPathParameters):java.security.cert.CertPathBuilderResult, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi.engineBuild(java.security.cert.CertPathParameters):java.security.cert.CertPathBuilderResult");
    }
}
