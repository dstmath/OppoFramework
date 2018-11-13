package com.android.org.bouncycastle.asn1.x9;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import java.util.Enumeration;

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
public class DHDomainParameters extends ASN1Object {
    private ASN1Integer g;
    private ASN1Integer j;
    private ASN1Integer p;
    private ASN1Integer q;
    private DHValidationParms validationParms;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public DHDomainParameters(com.android.org.bouncycastle.asn1.ASN1Integer r1, com.android.org.bouncycastle.asn1.ASN1Integer r2, com.android.org.bouncycastle.asn1.ASN1Integer r3, com.android.org.bouncycastle.asn1.ASN1Integer r4, com.android.org.bouncycastle.asn1.x9.DHValidationParms r5) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.ASN1Integer, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Sequence):void, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Sequence):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Sequence):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private DHDomainParameters(com.android.org.bouncycastle.asn1.ASN1Sequence r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Sequence):void, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Sequence):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(com.android.org.bouncycastle.asn1.ASN1Sequence):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public DHDomainParameters(java.math.BigInteger r1, java.math.BigInteger r2, java.math.BigInteger r3, java.math.BigInteger r4, com.android.org.bouncycastle.asn1.x9.DHValidationParms r5) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.<init>(java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, com.android.org.bouncycastle.asn1.x9.DHValidationParms):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getInstance(java.lang.Object):com.android.org.bouncycastle.asn1.x9.DHDomainParameters, dex: 
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
    public static com.android.org.bouncycastle.asn1.x9.DHDomainParameters getInstance(java.lang.Object r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getInstance(java.lang.Object):com.android.org.bouncycastle.asn1.x9.DHDomainParameters, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getInstance(java.lang.Object):com.android.org.bouncycastle.asn1.x9.DHDomainParameters");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getG():com.android.org.bouncycastle.asn1.ASN1Integer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public com.android.org.bouncycastle.asn1.ASN1Integer getG() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getG():com.android.org.bouncycastle.asn1.ASN1Integer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getG():com.android.org.bouncycastle.asn1.ASN1Integer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getJ():com.android.org.bouncycastle.asn1.ASN1Integer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public com.android.org.bouncycastle.asn1.ASN1Integer getJ() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getJ():com.android.org.bouncycastle.asn1.ASN1Integer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getJ():com.android.org.bouncycastle.asn1.ASN1Integer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getP():com.android.org.bouncycastle.asn1.ASN1Integer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public com.android.org.bouncycastle.asn1.ASN1Integer getP() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getP():com.android.org.bouncycastle.asn1.ASN1Integer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getP():com.android.org.bouncycastle.asn1.ASN1Integer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getQ():com.android.org.bouncycastle.asn1.ASN1Integer, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getQ():com.android.org.bouncycastle.asn1.ASN1Integer, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getQ():com.android.org.bouncycastle.asn1.ASN1Integer, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public com.android.org.bouncycastle.asn1.ASN1Integer getQ() {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getQ():com.android.org.bouncycastle.asn1.ASN1Integer, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getQ():com.android.org.bouncycastle.asn1.ASN1Integer, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getQ():com.android.org.bouncycastle.asn1.ASN1Integer");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getValidationParms():com.android.org.bouncycastle.asn1.x9.DHValidationParms, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getValidationParms():com.android.org.bouncycastle.asn1.x9.DHValidationParms, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getValidationParms():com.android.org.bouncycastle.asn1.x9.DHValidationParms, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public com.android.org.bouncycastle.asn1.x9.DHValidationParms getValidationParms() {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getValidationParms():com.android.org.bouncycastle.asn1.x9.DHValidationParms, dex:  in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getValidationParms():com.android.org.bouncycastle.asn1.x9.DHValidationParms, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.getValidationParms():com.android.org.bouncycastle.asn1.x9.DHValidationParms");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.toASN1Primitive():com.android.org.bouncycastle.asn1.ASN1Primitive, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public com.android.org.bouncycastle.asn1.ASN1Primitive toASN1Primitive() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.toASN1Primitive():com.android.org.bouncycastle.asn1.ASN1Primitive, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.DHDomainParameters.toASN1Primitive():com.android.org.bouncycastle.asn1.ASN1Primitive");
    }

    public static DHDomainParameters getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    private static ASN1Encodable getNext(Enumeration e) {
        return e.hasMoreElements() ? (ASN1Encodable) e.nextElement() : null;
    }
}
