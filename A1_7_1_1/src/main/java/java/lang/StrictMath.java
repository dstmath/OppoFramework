package java.lang;

import java.util.Random;
import sun.misc.DoubleConsts;
import sun.misc.FpUtils;

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
public final class StrictMath {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f35-assertionsDisabled = false;
    public static final double E = 2.718281828459045d;
    public static final double PI = 3.141592653589793d;
    private static long negativeZeroDoubleBits;
    private static long negativeZeroFloatBits;
    private static Random randomNumberGenerator;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.lang.StrictMath.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.lang.StrictMath.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.StrictMath.<clinit>():void");
    }

    public static native double IEEEremainder(double d, double d2);

    public static native double acos(double d);

    public static native double asin(double d);

    public static native double atan(double d);

    public static native double atan2(double d, double d2);

    public static native double cbrt(double d);

    public static native double cos(double d);

    public static native double cosh(double d);

    public static native double exp(double d);

    public static native double expm1(double d);

    public static native double hypot(double d, double d2);

    public static native double log(double d);

    public static native double log10(double d);

    public static native double log1p(double d);

    public static native double pow(double d, double d2);

    public static native double sin(double d);

    public static native double sinh(double d);

    public static native double sqrt(double d);

    public static native double tan(double d);

    public static native double tanh(double d);

    private StrictMath() {
    }

    public static double toRadians(double angdeg) {
        return (angdeg / 180.0d) * 3.141592653589793d;
    }

    public static double toDegrees(double angrad) {
        return (180.0d * angrad) / 3.141592653589793d;
    }

    public static double ceil(double a) {
        return floorOrCeil(a, -0.0d, 1.0d, 1.0d);
    }

    public static double floor(double a) {
        return floorOrCeil(a, -1.0d, 0.0d, -1.0d);
    }

    private static double floorOrCeil(double a, double negativeBoundary, double positiveBoundary, double sign) {
        int exponent = Math.getExponent(a);
        if (exponent < 0) {
            if (a != 0.0d) {
                a = a < 0.0d ? negativeBoundary : positiveBoundary;
            }
            return a;
        } else if (exponent >= 52) {
            return a;
        } else {
            if (!f35-assertionsDisabled) {
                Object obj = (exponent < 0 || exponent > 51) ? null : 1;
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            long doppel = Double.doubleToRawLongBits(a);
            long mask = DoubleConsts.SIGNIF_BIT_MASK >> exponent;
            if ((mask & doppel) == 0) {
                return a;
            }
            double result = Double.longBitsToDouble((~mask) & doppel);
            if (sign * a > 0.0d) {
                result += sign;
            }
            return result;
        }
    }

    public static double rint(double a) {
        double sign = FpUtils.rawCopySign(1.0d, a);
        a = Math.abs(a);
        if (a < 4.503599627370496E15d) {
            a = (4.503599627370496E15d + a) - 4.503599627370496E15d;
        }
        return sign * a;
    }

    public static int round(float a) {
        return Math.round(a);
    }

    public static long round(double a) {
        return Math.round(a);
    }

    private static synchronized Random initRNG() {
        Random random;
        synchronized (StrictMath.class) {
            random = randomNumberGenerator;
            if (random == null) {
                random = new Random();
                randomNumberGenerator = random;
            }
        }
        return random;
    }

    public static double random() {
        Random rnd = randomNumberGenerator;
        if (rnd == null) {
            rnd = initRNG();
        }
        return rnd.nextDouble();
    }

    public static int addExact(int x, int y) {
        return Math.addExact(x, y);
    }

    public static long addExact(long x, long y) {
        return Math.addExact(x, y);
    }

    public static int subtractExact(int x, int y) {
        return Math.subtractExact(x, y);
    }

    public static long subtractExact(long x, long y) {
        return Math.subtractExact(x, y);
    }

    public static int multiplyExact(int x, int y) {
        return Math.multiplyExact(x, y);
    }

    public static long multiplyExact(long x, long y) {
        return Math.multiplyExact(x, y);
    }

    public static int toIntExact(long value) {
        return Math.toIntExact(value);
    }

    public static int floorDiv(int x, int y) {
        return Math.floorDiv(x, y);
    }

    public static long floorDiv(long x, long y) {
        return Math.floorDiv(x, y);
    }

    public static int floorMod(int x, int y) {
        return Math.floorMod(x, y);
    }

    public static long floorMod(long x, long y) {
        return Math.floorMod(x, y);
    }

    public static int abs(int a) {
        return a < 0 ? -a : a;
    }

    public static long abs(long a) {
        return a < 0 ? -a : a;
    }

    public static float abs(float a) {
        return a <= 0.0f ? 0.0f - a : a;
    }

    public static double abs(double a) {
        return a <= 0.0d ? 0.0d - a : a;
    }

    public static int max(int a, int b) {
        return a >= b ? a : b;
    }

    public static long max(long a, long b) {
        return a >= b ? a : b;
    }

    public static float max(float a, float b) {
        if (a != a) {
            return a;
        }
        if (a == 0.0f && b == 0.0f && ((long) Float.floatToRawIntBits(a)) == negativeZeroFloatBits) {
            return b;
        }
        if (a < b) {
            a = b;
        }
        return a;
    }

    public static double max(double a, double b) {
        if (a != a) {
            return a;
        }
        if (a == 0.0d && b == 0.0d && Double.doubleToRawLongBits(a) == negativeZeroDoubleBits) {
            return b;
        }
        if (a < b) {
            a = b;
        }
        return a;
    }

    public static int min(int a, int b) {
        return a <= b ? a : b;
    }

    public static long min(long a, long b) {
        return a <= b ? a : b;
    }

    public static float min(float a, float b) {
        if (a != a) {
            return a;
        }
        if (a == 0.0f && b == 0.0f && ((long) Float.floatToRawIntBits(b)) == negativeZeroFloatBits) {
            return b;
        }
        if (a > b) {
            a = b;
        }
        return a;
    }

    public static double min(double a, double b) {
        if (a != a) {
            return a;
        }
        if (a == 0.0d && b == 0.0d && Double.doubleToRawLongBits(b) == negativeZeroDoubleBits) {
            return b;
        }
        if (a > b) {
            a = b;
        }
        return a;
    }

    public static double ulp(double d) {
        return FpUtils.ulp(d);
    }

    public static float ulp(float f) {
        return FpUtils.ulp(f);
    }

    public static double signum(double d) {
        return FpUtils.signum(d);
    }

    public static float signum(float f) {
        return FpUtils.signum(f);
    }

    public static double copySign(double magnitude, double sign) {
        return FpUtils.copySign(magnitude, sign);
    }

    public static float copySign(float magnitude, float sign) {
        return FpUtils.copySign(magnitude, sign);
    }

    public static int getExponent(float f) {
        return FpUtils.getExponent(f);
    }

    public static int getExponent(double d) {
        return FpUtils.getExponent(d);
    }

    public static double nextAfter(double start, double direction) {
        return FpUtils.nextAfter(start, direction);
    }

    public static float nextAfter(float start, double direction) {
        return FpUtils.nextAfter(start, direction);
    }

    public static double nextUp(double d) {
        return FpUtils.nextUp(d);
    }

    public static float nextUp(float f) {
        return FpUtils.nextUp(f);
    }

    public static double nextDown(double d) {
        return Math.nextDown(d);
    }

    public static float nextDown(float f) {
        return Math.nextDown(f);
    }

    public static double scalb(double d, int scaleFactor) {
        return FpUtils.scalb(d, scaleFactor);
    }

    public static float scalb(float f, int scaleFactor) {
        return FpUtils.scalb(f, scaleFactor);
    }
}
