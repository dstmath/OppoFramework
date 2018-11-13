package java.math;

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
class Multiplication {
    static final BigInteger[] bigFivePows = null;
    static final BigInteger[] bigTenPows = null;
    static final int[] fivePows = null;
    static final int[] tenPows = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.math.Multiplication.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.math.Multiplication.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.math.Multiplication.<clinit>():void");
    }

    private Multiplication() {
    }

    static BigInteger multiplyByPositiveInt(BigInteger val, int factor) {
        BigInt bi = val.getBigInt().copy();
        bi.multiplyByPositiveInt(factor);
        return new BigInteger(bi);
    }

    static BigInteger multiplyByTenPow(BigInteger val, long exp) {
        if (exp < ((long) tenPows.length)) {
            return multiplyByPositiveInt(val, tenPows[(int) exp]);
        }
        return val.multiply(powerOf10(exp));
    }

    static BigInteger powerOf10(long exp) {
        int intExp = (int) exp;
        if (exp < ((long) bigTenPows.length)) {
            return bigTenPows[intExp];
        }
        if (exp <= 50) {
            return BigInteger.TEN.pow(intExp);
        }
        BigInteger res;
        if (exp <= 2147483647L) {
            try {
                res = bigFivePows[1].pow(intExp).shiftLeft(intExp);
            } catch (OutOfMemoryError error) {
                throw new ArithmeticException(error.getMessage());
            }
        }
        long longExp;
        BigInteger powerOfFive = bigFivePows[1].pow(Integer.MAX_VALUE);
        res = powerOfFive;
        intExp = (int) (exp % 2147483647L);
        for (longExp = exp - 2147483647L; longExp > 2147483647L; longExp -= 2147483647L) {
            res = res.multiply(powerOfFive);
        }
        res = res.multiply(bigFivePows[1].pow(intExp)).shiftLeft(Integer.MAX_VALUE);
        for (longExp = exp - 2147483647L; longExp > 2147483647L; longExp -= 2147483647L) {
            res = res.shiftLeft(Integer.MAX_VALUE);
        }
        res = res.shiftLeft(intExp);
        return res;
    }

    static BigInteger multiplyByFivePow(BigInteger val, int exp) {
        if (exp < fivePows.length) {
            return multiplyByPositiveInt(val, fivePows[exp]);
        }
        if (exp < bigFivePows.length) {
            return val.multiply(bigFivePows[exp]);
        }
        return val.multiply(bigFivePows[1].pow(exp));
    }
}
