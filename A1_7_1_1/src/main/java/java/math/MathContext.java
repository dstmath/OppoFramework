package java.math;

import android.icu.text.PluralRules;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

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
    public static final MathContext DECIMAL128 = null;
    public static final MathContext DECIMAL32 = null;
    public static final MathContext DECIMAL64 = null;
    public static final MathContext UNLIMITED = null;
    private static final long serialVersionUID = 5579720004786848255L;
    private final int precision;
    private final RoundingMode roundingMode;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.math.MathContext.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.math.MathContext.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.math.MathContext.<clinit>():void");
    }

    public MathContext(int precision) {
        this(precision, RoundingMode.HALF_UP);
    }

    public MathContext(int precision, RoundingMode roundingMode) {
        this.precision = precision;
        this.roundingMode = roundingMode;
        checkValid();
    }

    public MathContext(String s) {
        int precisionLength = "precision=".length();
        int roundingModeLength = "roundingMode=".length();
        if (s.startsWith("precision=")) {
            int spaceIndex = s.indexOf(32, precisionLength);
            if (spaceIndex != -1) {
                try {
                    this.precision = Integer.parseInt(s.substring(precisionLength, spaceIndex));
                    int roundingModeStart = spaceIndex + 1;
                    if (s.regionMatches(roundingModeStart, "roundingMode=", 0, roundingModeLength)) {
                        this.roundingMode = RoundingMode.valueOf(s.substring(roundingModeStart + roundingModeLength));
                        checkValid();
                        return;
                    }
                    throw invalidMathContext("Missing rounding mode", s);
                } catch (NumberFormatException e) {
                    throw invalidMathContext("Bad precision", s);
                }
            }
        }
        throw invalidMathContext("Missing precision", s);
    }

    private IllegalArgumentException invalidMathContext(String reason, String s) {
        throw new IllegalArgumentException(reason + PluralRules.KEYWORD_RULE_SEPARATOR + s);
    }

    private void checkValid() {
        if (this.precision < 0) {
            throw new IllegalArgumentException("Negative precision: " + this.precision);
        } else if (this.roundingMode == null) {
            throw new NullPointerException("roundingMode == null");
        }
    }

    public int getPrecision() {
        return this.precision;
    }

    public RoundingMode getRoundingMode() {
        return this.roundingMode;
    }

    public boolean equals(Object x) {
        if (!(x instanceof MathContext) || ((MathContext) x).getPrecision() != this.precision) {
            return false;
        }
        if (((MathContext) x).getRoundingMode() == this.roundingMode) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.precision << 3) | this.roundingMode.ordinal();
    }

    public String toString() {
        return "precision=" + this.precision + " roundingMode=" + this.roundingMode;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        try {
            checkValid();
        } catch (Exception ex) {
            throw new StreamCorruptedException(ex.getMessage());
        }
    }
}
