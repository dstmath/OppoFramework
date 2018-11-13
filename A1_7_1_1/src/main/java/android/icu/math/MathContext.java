package android.icu.math;

import java.io.Serializable;

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
public final class MathContext implements Serializable {
    public static final MathContext DEFAULT = null;
    private static final int DEFAULT_DIGITS = 9;
    private static final int DEFAULT_FORM = 1;
    private static final boolean DEFAULT_LOSTDIGITS = false;
    private static final int DEFAULT_ROUNDINGMODE = 4;
    public static final int ENGINEERING = 2;
    private static final int MAX_DIGITS = 999999999;
    private static final int MIN_DIGITS = 0;
    public static final int PLAIN = 0;
    private static final int[] ROUNDS = null;
    private static final String[] ROUNDWORDS = null;
    public static final int ROUND_CEILING = 2;
    public static final int ROUND_DOWN = 1;
    public static final int ROUND_FLOOR = 3;
    public static final int ROUND_HALF_DOWN = 5;
    public static final int ROUND_HALF_EVEN = 6;
    public static final int ROUND_HALF_UP = 4;
    public static final int ROUND_UNNECESSARY = 7;
    public static final int ROUND_UP = 0;
    public static final int SCIENTIFIC = 1;
    private static final long serialVersionUID = 7163376998892515376L;
    int digits;
    int form;
    boolean lostDigits;
    int roundingMode;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.math.MathContext.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.math.MathContext.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.math.MathContext.<clinit>():void");
    }

    public MathContext(int setdigits) {
        this(setdigits, 1, false, 4);
    }

    public MathContext(int setdigits, int setform) {
        this(setdigits, setform, false, 4);
    }

    public MathContext(int setdigits, int setform, boolean setlostdigits) {
        this(setdigits, setform, setlostdigits, 4);
    }

    public MathContext(int setdigits, int setform, boolean setlostdigits, int setroundingmode) {
        if (setdigits != 9) {
            if (setdigits < 0) {
                throw new IllegalArgumentException("Digits too small: " + setdigits);
            } else if (setdigits > MAX_DIGITS) {
                throw new IllegalArgumentException("Digits too large: " + setdigits);
            }
        }
        if (setform != 1 && setform != 2 && setform != 0) {
            throw new IllegalArgumentException("Bad form value: " + setform);
        } else if (isValidRound(setroundingmode)) {
            this.digits = setdigits;
            this.form = setform;
            this.lostDigits = setlostdigits;
            this.roundingMode = setroundingmode;
        } else {
            throw new IllegalArgumentException("Bad roundingMode value: " + setroundingmode);
        }
    }

    public int getDigits() {
        return this.digits;
    }

    public int getForm() {
        return this.form;
    }

    public boolean getLostDigits() {
        return this.lostDigits;
    }

    public int getRoundingMode() {
        return this.roundingMode;
    }

    public String toString() {
        String formstr;
        String str;
        String roundword = null;
        if (this.form == 1) {
            formstr = "SCIENTIFIC";
        } else if (this.form == 2) {
            formstr = "ENGINEERING";
        } else {
            formstr = "PLAIN";
        }
        int $1 = ROUNDS.length;
        int r = 0;
        while ($1 > 0) {
            if (this.roundingMode == ROUNDS[r]) {
                roundword = ROUNDWORDS[r];
                break;
            }
            $1--;
            r++;
        }
        StringBuilder append = new StringBuilder().append("digits=").append(this.digits).append(" ").append("form=").append(formstr).append(" ").append("lostDigits=");
        if (this.lostDigits) {
            str = "1";
        } else {
            str = AndroidHardcodedSystemProperties.JAVA_VERSION;
        }
        return append.append(str).append(" ").append("roundingMode=").append(roundword).toString();
    }

    private static boolean isValidRound(int testround) {
        int $2 = ROUNDS.length;
        int r = 0;
        while ($2 > 0) {
            if (testround == ROUNDS[r]) {
                return true;
            }
            $2--;
            r++;
        }
        return false;
    }
}
