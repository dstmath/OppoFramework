package com.android.org.bouncycastle.crypto.params;

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
public class DESedeParameters extends DESParameters {
    public static final int DES_EDE_KEY_LENGTH = 24;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.crypto.params.DESedeParameters.<init>(byte[]):void, dex: 
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
    public DESedeParameters(byte[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.bouncycastle.crypto.params.DESedeParameters.<init>(byte[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.params.DESedeParameters.<init>(byte[]):void");
    }

    public static boolean isWeakKey(byte[] key, int offset, int length) {
        for (int i = offset; i < length; i += 8) {
            if (DESParameters.isWeakKey(key, i)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWeakKey(byte[] key, int offset) {
        return isWeakKey(key, offset, key.length - offset);
    }

    public static boolean isRealEDEKey(byte[] key, int offset) {
        return key.length == 16 ? isReal2Key(key, offset) : isReal3Key(key, offset);
    }

    public static boolean isReal2Key(byte[] key, int offset) {
        boolean isValid = false;
        for (int i = offset; i != offset + 8; i++) {
            if (key[i] != key[i + 8]) {
                isValid = true;
            }
        }
        return isValid;
    }

    public static boolean isReal3Key(byte[] key, int offset) {
        boolean diff12 = false;
        int diff13 = 0;
        boolean diff23 = false;
        for (int i = offset; i != offset + 8; i++) {
            int i2;
            if (key[i] != key[i + 8]) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            diff12 |= i2;
            if (key[i] != key[i + 16]) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            diff13 |= i2;
            if (key[i + 8] != key[i + 16]) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            diff23 |= i2;
        }
        return (!diff12 || diff13 == 0) ? false : diff23;
    }
}
