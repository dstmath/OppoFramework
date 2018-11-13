package android.security.keystore;

import java.security.spec.RSAKeyGenParameterSpec;

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
public class SoterRSAKeyGenParameterSpec extends RSAKeyGenParameterSpec {
    private boolean isAutoAddCounterWhenGetPublicKey;
    private boolean isAutoSignedWithAttkWhenGetPublicKey;
    private boolean isAutoSignedWithCommonkWhenGetPublicKey;
    private boolean isForSoter;
    private boolean isNeedUseNextAttk;
    private boolean isSecmsgFidCounterSignedWhenSign;
    private String mAutoSignedKeyNameWhenGetPublicKey;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.security.keystore.SoterRSAKeyGenParameterSpec.<init>(int, java.math.BigInteger, boolean, boolean, boolean, java.lang.String, boolean, boolean, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public SoterRSAKeyGenParameterSpec(int r1, java.math.BigInteger r2, boolean r3, boolean r4, boolean r5, java.lang.String r6, boolean r7, boolean r8, boolean r9) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.security.keystore.SoterRSAKeyGenParameterSpec.<init>(int, java.math.BigInteger, boolean, boolean, boolean, java.lang.String, boolean, boolean, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.<init>(int, java.math.BigInteger, boolean, boolean, boolean, java.lang.String, boolean, boolean, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.keystore.SoterRSAKeyGenParameterSpec.<init>(boolean, boolean, boolean, java.lang.String, boolean, boolean, boolean):void, dex: 
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
    public SoterRSAKeyGenParameterSpec(boolean r1, boolean r2, boolean r3, java.lang.String r4, boolean r5, boolean r6, boolean r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.keystore.SoterRSAKeyGenParameterSpec.<init>(boolean, boolean, boolean, java.lang.String, boolean, boolean, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.<init>(boolean, boolean, boolean, java.lang.String, boolean, boolean, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterRSAKeyGenParameterSpec.getAutoSignedKeyNameWhenGetPublicKey():java.lang.String, dex: 
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
    public java.lang.String getAutoSignedKeyNameWhenGetPublicKey() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.security.keystore.SoterRSAKeyGenParameterSpec.getAutoSignedKeyNameWhenGetPublicKey():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.getAutoSignedKeyNameWhenGetPublicKey():java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoAddCounterWhenGetPublicKey():boolean, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoAddCounterWhenGetPublicKey():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoAddCounterWhenGetPublicKey():boolean, dex: 
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
    public boolean isAutoAddCounterWhenGetPublicKey() {
        /*
        // Can't load method instructions: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoAddCounterWhenGetPublicKey():boolean, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoAddCounterWhenGetPublicKey():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoAddCounterWhenGetPublicKey():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoSignedWithAttkWhenGetPublicKey():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean isAutoSignedWithAttkWhenGetPublicKey() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoSignedWithAttkWhenGetPublicKey():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoSignedWithAttkWhenGetPublicKey():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoSignedWithCommonkWhenGetPublicKey():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean isAutoSignedWithCommonkWhenGetPublicKey() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoSignedWithCommonkWhenGetPublicKey():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.isAutoSignedWithCommonkWhenGetPublicKey():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isForSoter():boolean, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isForSoter():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isForSoter():boolean, dex: 
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
    public boolean isForSoter() {
        /*
        // Can't load method instructions: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isForSoter():boolean, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isForSoter():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.isForSoter():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isNeedUseNextAttk():boolean, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isNeedUseNextAttk():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isNeedUseNextAttk():boolean, dex: 
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
    public boolean isNeedUseNextAttk() {
        /*
        // Can't load method instructions: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isNeedUseNextAttk():boolean, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isNeedUseNextAttk():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.isNeedUseNextAttk():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isSecmsgFidCounterSignedWhenSign():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean isSecmsgFidCounterSignedWhenSign() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.security.keystore.SoterRSAKeyGenParameterSpec.isSecmsgFidCounterSignedWhenSign():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.isSecmsgFidCounterSignedWhenSign():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setAutoSignedKeyNameWhenGetPublicKey(java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void setAutoSignedKeyNameWhenGetPublicKey(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setAutoSignedKeyNameWhenGetPublicKey(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.setAutoSignedKeyNameWhenGetPublicKey(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoAddCounterWhenGetPublicKey(boolean):void, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoAddCounterWhenGetPublicKey(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoAddCounterWhenGetPublicKey(boolean):void, dex: 
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
    public void setIsAutoAddCounterWhenGetPublicKey(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoAddCounterWhenGetPublicKey(boolean):void, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoAddCounterWhenGetPublicKey(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoAddCounterWhenGetPublicKey(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoSignedWithAttkWhenGetPublicKey(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setIsAutoSignedWithAttkWhenGetPublicKey(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoSignedWithAttkWhenGetPublicKey(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoSignedWithAttkWhenGetPublicKey(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoSignedWithCommonkWhenGetPublicKey(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setIsAutoSignedWithCommonkWhenGetPublicKey(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoSignedWithCommonkWhenGetPublicKey(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsAutoSignedWithCommonkWhenGetPublicKey(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsForSoter(boolean):void, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsForSoter(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsForSoter(boolean):void, dex: 
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
    public void setIsForSoter(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsForSoter(boolean):void, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsForSoter(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsForSoter(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsNeedUseNextAttk(boolean):void, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsNeedUseNextAttk(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsNeedUseNextAttk(boolean):void, dex: 
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
    public void setIsNeedUseNextAttk(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsNeedUseNextAttk(boolean):void, dex:  in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsNeedUseNextAttk(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsNeedUseNextAttk(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsSecmsgFidCounterSignedWhenSign(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setIsSecmsgFidCounterSignedWhenSign(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsSecmsgFidCounterSignedWhenSign(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.setIsSecmsgFidCounterSignedWhenSign(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterRSAKeyGenParameterSpec.toString():java.lang.String, dex: 
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
    public java.lang.String toString() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keystore.SoterRSAKeyGenParameterSpec.toString():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keystore.SoterRSAKeyGenParameterSpec.toString():java.lang.String");
    }
}
