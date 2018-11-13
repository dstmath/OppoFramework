package java.lang;

import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.misc.DoubleConsts;
import sun.misc.FDBigInt;
import sun.misc.FpUtils;

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
public class FloatingDecimal {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f26-assertionsDisabled = false;
    private static final ThreadLocal<FloatingDecimal> TL_INSTANCE = null;
    private static FDBigInt[] b5p = null;
    private static final double[] big10pow = null;
    static final int bigDecimalExponent = 324;
    static final int expBias = 1023;
    static final long expMask = 9218868437227405312L;
    static final long expOne = 4607182418800017408L;
    static final int expShift = 52;
    static final long fractHOB = 4503599627370496L;
    static final long fractMask = 4503599627370495L;
    private static Pattern hexFloatPattern = null;
    static final long highbit = Long.MIN_VALUE;
    static final long highbyte = -72057594037927936L;
    private static final char[] infinity = null;
    static final int intDecimalDigits = 9;
    private static final long[] long5pow = null;
    static final long lowbytes = 72057594037927935L;
    static final int maxDecimalDigits = 15;
    static final int maxDecimalExponent = 308;
    static final int maxSmallBinExp = 62;
    private static final int maxSmallTen = 0;
    static final int minDecimalExponent = -324;
    static final int minSmallBinExp = -21;
    private static final int[] n5bits = null;
    private static final char[] notANumber = null;
    private static ThreadLocal perThreadBuffer = null;
    static final long signMask = Long.MIN_VALUE;
    static final int singleExpBias = 127;
    static final int singleExpMask = 2139095040;
    static final int singleExpShift = 23;
    static final int singleFractHOB = 8388608;
    static final int singleFractMask = 8388607;
    static final int singleMaxDecimalDigits = 7;
    static final int singleMaxDecimalExponent = 38;
    private static final int singleMaxSmallTen = 0;
    static final int singleMinDecimalExponent = -45;
    static final int singleSignMask = Integer.MIN_VALUE;
    private static final float[] singleSmall10pow = null;
    private static final double[] small10pow = null;
    private static final int[] small5pow = null;
    private static final double[] tiny10pow = null;
    private static final char[] zero = null;
    int bigIntExp;
    int bigIntNBits;
    int decExponent;
    char[] digits;
    boolean fromHex;
    boolean isExceptional;
    boolean isNegative;
    boolean mustSetRoundDir;
    int nDigits;
    int roundDir;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.lang.FloatingDecimal.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.lang.FloatingDecimal.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.FloatingDecimal.<clinit>():void");
    }

    /* synthetic */ FloatingDecimal(FloatingDecimal floatingDecimal) {
        this();
    }

    private static int countBits(long v) {
        if (v == 0) {
            return 0;
        }
        while ((highbyte & v) == 0) {
            v <<= 8;
        }
        while (v > 0) {
            v <<= 1;
        }
        int n = 0;
        while ((lowbytes & v) != 0) {
            v <<= 8;
            n += 8;
        }
        while (v != 0) {
            v <<= 1;
            n++;
        }
        return n;
    }

    private static synchronized FDBigInt big5pow(int p) {
        Object obj = null;
        synchronized (FloatingDecimal.class) {
            if (!f26-assertionsDisabled) {
                if (p >= 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError(Integer.valueOf(p));
                }
            }
            if (b5p == null) {
                b5p = new FDBigInt[(p + 1)];
            } else if (b5p.length <= p) {
                Object t = new FDBigInt[(p + 1)];
                System.arraycopy(b5p, 0, t, 0, b5p.length);
                b5p = t;
            }
            FDBigInt fDBigInt;
            if (b5p[p] != null) {
                fDBigInt = b5p[p];
                return fDBigInt;
            } else if (p < small5pow.length) {
                fDBigInt = new FDBigInt(small5pow[p]);
                b5p[p] = fDBigInt;
                return fDBigInt;
            } else if (p < long5pow.length) {
                fDBigInt = new FDBigInt(long5pow[p]);
                b5p[p] = fDBigInt;
                return fDBigInt;
            } else {
                int q = p >> 1;
                int r = p - q;
                FDBigInt bigq = b5p[q];
                if (bigq == null) {
                    bigq = big5pow(q);
                }
                if (r < small5pow.length) {
                    fDBigInt = bigq.mult(small5pow[r]);
                    b5p[p] = fDBigInt;
                    return fDBigInt;
                }
                FDBigInt bigr = b5p[r];
                if (bigr == null) {
                    bigr = big5pow(r);
                }
                fDBigInt = bigq.mult(bigr);
                b5p[p] = fDBigInt;
                return fDBigInt;
            }
        }
    }

    private static FDBigInt multPow52(FDBigInt v, int p5, int p2) {
        if (p5 != 0) {
            if (p5 < small5pow.length) {
                v = v.mult(small5pow[p5]);
            } else {
                v = v.mult(big5pow(p5));
            }
        }
        if (p2 != 0) {
            v.lshiftMe(p2);
        }
        return v;
    }

    private static FDBigInt constructPow52(int p5, int p2) {
        FDBigInt v = new FDBigInt(big5pow(p5));
        if (p2 != 0) {
            v.lshiftMe(p2);
        }
        return v;
    }

    private FDBigInt doubleToBigInt(double dval) {
        long lbits = Double.doubleToLongBits(dval) & Long.MAX_VALUE;
        int binexp = (int) (lbits >>> expShift);
        lbits &= 4503599627370495L;
        if (binexp > 0) {
            lbits |= fractHOB;
        } else {
            if (!f26-assertionsDisabled) {
                if ((lbits != 0 ? 1 : null) == null) {
                    throw new AssertionError(Long.valueOf(lbits));
                }
            }
            binexp++;
            while ((fractHOB & lbits) == 0) {
                lbits <<= 1;
                binexp--;
            }
        }
        binexp -= 1023;
        int nbits = countBits(lbits);
        lbits >>>= 53 - nbits;
        this.bigIntExp = (binexp + 1) - nbits;
        this.bigIntNBits = nbits;
        return new FDBigInt(lbits);
    }

    private static double ulp(double dval, boolean subtracting) {
        double ulpval;
        long lbits = Double.doubleToLongBits(dval) & Long.MAX_VALUE;
        int binexp = (int) (lbits >>> 52);
        if (subtracting && binexp >= expShift && (4503599627370495L & lbits) == 0) {
            binexp--;
        }
        if (binexp > expShift) {
            ulpval = Double.longBitsToDouble(((long) (binexp - 52)) << 52);
        } else if (binexp == 0) {
            ulpval = Double.MIN_VALUE;
        } else {
            ulpval = Double.longBitsToDouble(1 << (binexp - 1));
        }
        if (subtracting) {
            return -ulpval;
        }
        return ulpval;
    }

    float stickyRound(double dval) {
        long lbits = Double.doubleToLongBits(dval);
        long binexp = lbits & 9218868437227405312L;
        if (binexp == 0 || binexp == 9218868437227405312L) {
            return (float) dval;
        }
        return (float) Double.longBitsToDouble(lbits + ((long) this.roundDir));
    }

    private void developLongDigits(int decExponent, long lvalue, long insignificant) {
        int ndigits;
        char[] digits;
        int digitno;
        int i = 0;
        while (insignificant >= 10) {
            insignificant /= 10;
            i++;
        }
        if (i != 0) {
            long pow10 = long5pow[i] << i;
            long residue = lvalue % pow10;
            lvalue /= pow10;
            decExponent += i;
            if (residue >= (pow10 >> 1)) {
                lvalue++;
            }
        }
        int c;
        int digitno2;
        if (lvalue <= 2147483647L) {
            if (!f26-assertionsDisabled) {
                if ((lvalue > 0 ? 1 : null) == null) {
                    throw new AssertionError(Long.valueOf(lvalue));
                }
            }
            int ivalue = (int) lvalue;
            ndigits = 10;
            digits = (char[]) perThreadBuffer.get();
            digitno = 9;
            c = ivalue % 10;
            ivalue /= 10;
            while (c == 0) {
                decExponent++;
                c = ivalue % 10;
                ivalue /= 10;
            }
            while (true) {
                digitno2 = digitno;
                if (ivalue == 0) {
                    break;
                }
                digitno = digitno2 - 1;
                digits[digitno2] = (char) (c + 48);
                decExponent++;
                c = ivalue % 10;
                ivalue /= 10;
            }
            digits[digitno2] = (char) (c + 48);
            digitno = digitno2;
        } else {
            ndigits = 20;
            digits = (char[]) perThreadBuffer.get();
            digitno = 19;
            c = (int) (lvalue % 10);
            lvalue /= 10;
            while (c == 0) {
                decExponent++;
                c = (int) (lvalue % 10);
                lvalue /= 10;
            }
            while (true) {
                digitno2 = digitno;
                if (lvalue == 0) {
                    break;
                }
                digitno = digitno2 - 1;
                digits[digitno2] = (char) (c + 48);
                decExponent++;
                c = (int) (lvalue % 10);
                lvalue /= 10;
            }
            digits[digitno2] = (char) (c + 48);
            digitno = digitno2;
        }
        ndigits -= digitno;
        char[] result = new char[ndigits];
        System.arraycopy(digits, digitno, result, 0, ndigits);
        this.digits = result;
        this.decExponent = decExponent + 1;
        this.nDigits = ndigits;
    }

    private void roundup() {
        int i = this.nDigits - 1;
        int q = this.digits[i];
        if (q == 57) {
            while (q == 57 && i > 0) {
                this.digits[i] = '0';
                i--;
                q = this.digits[i];
            }
            if (q == 57) {
                this.decExponent++;
                this.digits[0] = '1';
                return;
            }
        }
        this.digits[i] = (char) (q + 1);
    }

    private FloatingDecimal() {
        this.mustSetRoundDir = f26-assertionsDisabled;
        this.fromHex = f26-assertionsDisabled;
        this.roundDir = 0;
    }

    public static FloatingDecimal getThreadLocalInstance() {
        return (FloatingDecimal) TL_INSTANCE.get();
    }

    public FloatingDecimal loadDouble(double d) {
        long dBits = Double.doubleToLongBits(d);
        this.mustSetRoundDir = f26-assertionsDisabled;
        this.fromHex = f26-assertionsDisabled;
        this.roundDir = 0;
        if ((Long.MIN_VALUE & dBits) != 0) {
            this.isNegative = true;
            dBits ^= Long.MIN_VALUE;
        } else {
            this.isNegative = f26-assertionsDisabled;
        }
        int binExp = (int) ((9218868437227405312L & dBits) >> expShift);
        long fractBits = dBits & 4503599627370495L;
        if (binExp == 2047) {
            this.isExceptional = true;
            if (fractBits == 0) {
                this.digits = infinity;
            } else {
                this.digits = notANumber;
                this.isNegative = f26-assertionsDisabled;
            }
            this.nDigits = this.digits.length;
            return this;
        }
        int nSignificantBits;
        this.isExceptional = f26-assertionsDisabled;
        if (binExp != 0) {
            fractBits |= fractHOB;
            nSignificantBits = 53;
        } else if (fractBits == 0) {
            this.decExponent = 0;
            this.digits = zero;
            this.nDigits = 1;
            return this;
        } else {
            while ((fractHOB & fractBits) == 0) {
                fractBits <<= 1;
                binExp--;
            }
            nSignificantBits = (binExp + expShift) + 1;
            binExp++;
        }
        dtoa(binExp - 1023, fractBits, nSignificantBits);
        return this;
    }

    public FloatingDecimal loadFloat(float f) {
        int fBits = Float.floatToIntBits(f);
        this.mustSetRoundDir = f26-assertionsDisabled;
        this.fromHex = f26-assertionsDisabled;
        this.roundDir = 0;
        if ((fBits & Integer.MIN_VALUE) != 0) {
            this.isNegative = true;
            fBits ^= Integer.MIN_VALUE;
        } else {
            this.isNegative = f26-assertionsDisabled;
        }
        int binExp = (2139095040 & fBits) >> 23;
        int fractBits = fBits & 8388607;
        if (binExp == 255) {
            this.isExceptional = true;
            if (((long) fractBits) == 0) {
                this.digits = infinity;
            } else {
                this.digits = notANumber;
                this.isNegative = f26-assertionsDisabled;
            }
            this.nDigits = this.digits.length;
            return this;
        }
        int nSignificantBits;
        this.isExceptional = f26-assertionsDisabled;
        if (binExp != 0) {
            fractBits |= singleFractHOB;
            nSignificantBits = 24;
        } else if (fractBits == 0) {
            this.decExponent = 0;
            this.digits = zero;
            this.nDigits = 1;
            return this;
        } else {
            while ((fractBits & singleFractHOB) == 0) {
                fractBits <<= 1;
                binExp--;
            }
            nSignificantBits = (binExp + 23) + 1;
            binExp++;
        }
        dtoa(binExp - 127, ((long) fractBits) << 29, nSignificantBits);
        return this;
    }

    private void dtoa(int binExp, long fractBits, int nSignificantBits) {
        int nFractBits = countBits(fractBits);
        int nTinyBits = Math.max(0, (nFractBits - binExp) - 1);
        if (binExp > maxSmallBinExp || binExp < minSmallBinExp || nTinyBits >= long5pow.length || n5bits[nTinyBits] + nFractBits >= 64 || nTinyBits != 0) {
            int i;
            int ndigit;
            boolean low;
            boolean high;
            long lowDigitDifference;
            int decExp = (int) Math.floor((((Double.longBitsToDouble((-4503599627370497L & fractBits) | expOne) - 1.5d) * 0.289529654d) + 0.176091259d) + (((double) binExp) * 0.301029995663981d));
            int B5 = Math.max(0, -decExp);
            int B2 = (B5 + nTinyBits) + binExp;
            int S5 = Math.max(0, decExp);
            int S2 = S5 + nTinyBits;
            int M5 = B5;
            int M2 = B2 - nSignificantBits;
            fractBits >>>= 53 - nFractBits;
            B2 -= nFractBits - 1;
            int common2factor = Math.min(B2, S2);
            B2 -= common2factor;
            S2 -= common2factor;
            M2 -= common2factor;
            if (nFractBits == 1) {
                M2--;
            }
            if (M2 < 0) {
                B2 -= M2;
                S2 -= M2;
                M2 = 0;
            }
            char[] digits = new char[18];
            this.digits = digits;
            int Bbits = (nFractBits + B2) + (B5 < n5bits.length ? n5bits[B5] : B5 * 3);
            int i2 = S2 + 1;
            if (S5 + 1 < n5bits.length) {
                i = n5bits[S5 + 1];
            } else {
                i = (S5 + 1) * 3;
            }
            int tenSbits = i2 + i;
            int q;
            int ndigit2;
            if (Bbits >= 64 || tenSbits >= 64) {
                FDBigInt Bval = multPow52(new FDBigInt(fractBits), B5, B2);
                FDBigInt Sval = constructPow52(S5, S2);
                FDBigInt Mval = constructPow52(B5, M2);
                int shiftBias = Sval.normalizeMe();
                Bval.lshiftMe(shiftBias);
                Mval.lshiftMe(shiftBias);
                FDBigInt tenSval = Sval.mult(10);
                ndigit = 0;
                q = Bval.quoRemIteration(Sval);
                Mval = Mval.mult(10);
                low = Bval.cmp(Mval) < 0 ? true : f26-assertionsDisabled;
                high = Bval.add(Mval).cmp(tenSval) > 0 ? true : f26-assertionsDisabled;
                if (!f26-assertionsDisabled) {
                    if ((q < 10 ? 1 : null) == null) {
                        throw new AssertionError(Integer.valueOf(q));
                    }
                }
                if (q != 0 || high) {
                    ndigit = 1;
                    digits[0] = (char) (q + 48);
                } else {
                    decExp--;
                }
                if (decExp < -3 || decExp >= 8) {
                    low = f26-assertionsDisabled;
                    high = f26-assertionsDisabled;
                    ndigit2 = ndigit;
                } else {
                    ndigit2 = ndigit;
                }
                while (!low && !high) {
                    q = Bval.quoRemIteration(Sval);
                    Mval = Mval.mult(10);
                    if (!f26-assertionsDisabled) {
                        if ((q < 10 ? 1 : null) == null) {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    low = Bval.cmp(Mval) < 0 ? true : f26-assertionsDisabled;
                    high = Bval.add(Mval).cmp(tenSval) > 0 ? true : f26-assertionsDisabled;
                    ndigit = ndigit2 + 1;
                    digits[ndigit2] = (char) (q + 48);
                    ndigit2 = ndigit;
                }
                if (high && low) {
                    Bval.lshiftMe(1);
                    lowDigitDifference = (long) Bval.cmp(tenSval);
                    ndigit = ndigit2;
                } else {
                    lowDigitDifference = 0;
                    ndigit = ndigit2;
                }
            } else if (Bbits >= 32 || tenSbits >= 32) {
                long b = (long5pow[B5] * fractBits) << B2;
                long s = long5pow[S5] << S2;
                long tens = s * 10;
                ndigit = 0;
                q = (int) (b / s);
                b = 10 * (b % s);
                long m = (long5pow[B5] << M2) * 10;
                low = b < m ? true : f26-assertionsDisabled;
                high = b + m > tens ? true : f26-assertionsDisabled;
                if (!f26-assertionsDisabled) {
                    if ((q < 10 ? 1 : null) == null) {
                        throw new AssertionError(Integer.valueOf(q));
                    }
                }
                if (q != 0 || high) {
                    ndigit = 1;
                    digits[0] = (char) (q + 48);
                } else {
                    decExp--;
                }
                if (decExp < -3 || decExp >= 8) {
                    low = f26-assertionsDisabled;
                    high = f26-assertionsDisabled;
                    ndigit2 = ndigit;
                } else {
                    ndigit2 = ndigit;
                }
                while (!low && !high) {
                    q = (int) (b / s);
                    b = 10 * (b % s);
                    m *= 10;
                    if (!f26-assertionsDisabled) {
                        if ((q < 10 ? 1 : null) == null) {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    if (m > 0) {
                        low = b < m ? true : f26-assertionsDisabled;
                        high = b + m > tens ? true : f26-assertionsDisabled;
                    } else {
                        low = true;
                        high = true;
                    }
                    ndigit = ndigit2 + 1;
                    digits[ndigit2] = (char) (q + 48);
                    ndigit2 = ndigit;
                }
                lowDigitDifference = (b << 1) - tens;
                ndigit = ndigit2;
            } else {
                int b2 = (((int) fractBits) * small5pow[B5]) << B2;
                int s2 = small5pow[S5] << S2;
                int tens2 = s2 * 10;
                ndigit = 0;
                q = b2 / s2;
                b2 = (b2 % s2) * 10;
                int m2 = (small5pow[B5] << M2) * 10;
                low = b2 < m2 ? true : f26-assertionsDisabled;
                high = b2 + m2 > tens2 ? true : f26-assertionsDisabled;
                if (!f26-assertionsDisabled) {
                    Object obj;
                    if (q < 10) {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError(Integer.valueOf(q));
                    }
                }
                if (q != 0 || high) {
                    ndigit = 1;
                    digits[0] = (char) (q + 48);
                } else {
                    decExp--;
                }
                if (decExp < -3 || decExp >= 8) {
                    low = f26-assertionsDisabled;
                    high = f26-assertionsDisabled;
                    ndigit2 = ndigit;
                } else {
                    ndigit2 = ndigit;
                }
                while (!low && !high) {
                    q = b2 / s2;
                    b2 = (b2 % s2) * 10;
                    m2 *= 10;
                    if (!f26-assertionsDisabled) {
                        if ((q < 10 ? 1 : null) == null) {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    if (((long) m2) > 0) {
                        low = b2 < m2 ? true : f26-assertionsDisabled;
                        high = b2 + m2 > tens2 ? true : f26-assertionsDisabled;
                    } else {
                        low = true;
                        high = true;
                    }
                    ndigit = ndigit2 + 1;
                    digits[ndigit2] = (char) (q + 48);
                    ndigit2 = ndigit;
                }
                lowDigitDifference = (long) ((b2 << 1) - tens2);
                ndigit = ndigit2;
            }
            this.decExponent = decExp + 1;
            this.digits = digits;
            this.nDigits = ndigit;
            if (high) {
                if (!low) {
                    roundup();
                } else if (lowDigitDifference == 0) {
                    if ((digits[this.nDigits - 1] & 1) != 0) {
                        roundup();
                    }
                } else if (lowDigitDifference > 0) {
                    roundup();
                }
            }
            return;
        }
        long halfULP;
        if (binExp > nSignificantBits) {
            halfULP = 1 << ((binExp - nSignificantBits) - 1);
        } else {
            halfULP = 0;
        }
        if (binExp >= expShift) {
            fractBits <<= binExp - 52;
        } else {
            fractBits >>>= 52 - binExp;
        }
        developLongDigits(0, fractBits, halfULP);
    }

    public String toString() {
        StringBuffer result = new StringBuffer(this.nDigits + 8);
        if (this.isNegative) {
            result.append('-');
        }
        if (this.isExceptional) {
            result.append(this.digits, 0, this.nDigits);
        } else {
            result.append("0.");
            result.append(this.digits, 0, this.nDigits);
            result.append('e');
            result.append(this.decExponent);
        }
        return new String(result);
    }

    public String toJavaFormatString() {
        char[] result = (char[]) perThreadBuffer.get();
        return new String(result, 0, getChars(result));
    }

    private int getChars(char[] result) {
        if (!f26-assertionsDisabled) {
            if ((this.nDigits <= 19 ? 1 : 0) == 0) {
                throw new AssertionError(Integer.valueOf(this.nDigits));
            }
        }
        int i = 0;
        if (this.isNegative) {
            result[0] = '-';
            i = 1;
        }
        int i2;
        if (this.isExceptional) {
            System.arraycopy(this.digits, 0, result, i, this.nDigits);
            return i + this.nDigits;
        } else if (this.decExponent > 0 && this.decExponent < 8) {
            int charLength = Math.min(this.nDigits, this.decExponent);
            System.arraycopy(this.digits, 0, result, i, charLength);
            i += charLength;
            if (charLength < this.decExponent) {
                charLength = this.decExponent - charLength;
                System.arraycopy(zero, 0, result, i, charLength);
                i += charLength;
                i2 = i + 1;
                result[i] = '.';
                i = i2 + 1;
                result[i2] = '0';
                return i;
            }
            i2 = i + 1;
            result[i] = '.';
            if (charLength < this.nDigits) {
                int t = this.nDigits - charLength;
                System.arraycopy(this.digits, charLength, result, i2, t);
                return i2 + t;
            }
            i = i2 + 1;
            result[i2] = '0';
            return i;
        } else if (this.decExponent > 0 || this.decExponent <= -3) {
            int e;
            i2 = i + 1;
            result[i] = this.digits[0];
            i = i2 + 1;
            result[i2] = '.';
            if (this.nDigits > 1) {
                System.arraycopy(this.digits, 1, result, i, this.nDigits - 1);
                i += this.nDigits - 1;
            } else {
                i2 = i + 1;
                result[i] = '0';
                i = i2;
            }
            i2 = i + 1;
            result[i] = 'E';
            if (this.decExponent <= 0) {
                i = i2 + 1;
                result[i2] = '-';
                e = (-this.decExponent) + 1;
                i2 = i;
            } else {
                e = this.decExponent - 1;
            }
            if (e <= 9) {
                i = i2 + 1;
                result[i2] = (char) (e + 48);
                return i;
            } else if (e <= 99) {
                i = i2 + 1;
                result[i2] = (char) ((e / 10) + 48);
                i2 = i + 1;
                result[i] = (char) ((e % 10) + 48);
                return i2;
            } else {
                i = i2 + 1;
                result[i2] = (char) ((e / 100) + 48);
                e %= 100;
                i2 = i + 1;
                result[i] = (char) ((e / 10) + 48);
                i = i2 + 1;
                result[i2] = (char) ((e % 10) + 48);
                return i;
            }
        } else {
            i2 = i + 1;
            result[i] = '0';
            i = i2 + 1;
            result[i2] = '.';
            if (this.decExponent != 0) {
                System.arraycopy(zero, 0, result, i, -this.decExponent);
                i -= this.decExponent;
            }
            System.arraycopy(this.digits, 0, result, i, this.nDigits);
            return i + this.nDigits;
        }
    }

    public void appendTo(AbstractStringBuilder buf) {
        if (this.isNegative) {
            buf.append('-');
        }
        if (this.isExceptional) {
            buf.append(this.digits, 0, this.nDigits);
            return;
        }
        if (this.decExponent > 0 && this.decExponent < 8) {
            int charLength = Math.min(this.nDigits, this.decExponent);
            buf.append(this.digits, 0, charLength);
            if (charLength < this.decExponent) {
                buf.append(zero, 0, this.decExponent - charLength);
                buf.append(".0");
            } else {
                buf.append('.');
                if (charLength < this.nDigits) {
                    buf.append(this.digits, charLength, this.nDigits - charLength);
                } else {
                    buf.append('0');
                }
            }
        } else if (this.decExponent > 0 || this.decExponent <= -3) {
            int e;
            buf.append(this.digits[0]);
            buf.append('.');
            if (this.nDigits > 1) {
                buf.append(this.digits, 1, this.nDigits - 1);
            } else {
                buf.append('0');
            }
            buf.append('E');
            if (this.decExponent <= 0) {
                buf.append('-');
                e = (-this.decExponent) + 1;
            } else {
                e = this.decExponent - 1;
            }
            if (e <= 9) {
                buf.append((char) (e + 48));
            } else if (e <= 99) {
                buf.append((char) ((e / 10) + 48));
                buf.append((char) ((e % 10) + 48));
            } else {
                buf.append((char) ((e / 100) + 48));
                e %= 100;
                buf.append((char) ((e / 10) + 48));
                buf.append((char) ((e % 10) + 48));
            }
        } else {
            buf.append("0.");
            if (this.decExponent != 0) {
                buf.append(zero, 0, -this.decExponent);
            }
            buf.append(this.digits, 0, this.nDigits);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x0114 A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x0218  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x011c A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0124 A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Missing block: B:59:0x0118, code:
            if (r23 != 0) goto L_0x011a;
     */
    /* JADX WARNING: Missing block: B:84:0x0179, code:
            if (r16 != r11) goto L_0x017b;
     */
    /* JADX WARNING: Missing block: B:101:0x01ea, code:
            r16 = r16 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public FloatingDecimal readJavaFormatString(String in) throws NumberFormatException {
        boolean isNegative = f26-assertionsDisabled;
        boolean signSeen = f26-assertionsDisabled;
        try {
            in = in.trim();
            int l = in.length();
            if (l == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = 0;
            switch (in.charAt(0)) {
                case '+':
                    break;
                case '-':
                    isNegative = true;
                    break;
            }
            i = 1;
            signSeen = true;
            char c = in.charAt(i);
            if (c == 'N' || c == 'I') {
                char[] targetChars;
                boolean potentialNaN = f26-assertionsDisabled;
                if (c == 'N') {
                    targetChars = notANumber;
                    potentialNaN = true;
                } else {
                    targetChars = infinity;
                }
                int j = 0;
                while (i < l && j < targetChars.length) {
                    if (in.charAt(i) == targetChars[j]) {
                        i++;
                        j++;
                    } else {
                        throw new NumberFormatException("For input string: \"" + in + "\"");
                    }
                }
                if (j == targetChars.length && i == l) {
                    FloatingDecimal loadDouble;
                    if (potentialNaN) {
                        loadDouble = loadDouble(Double.NaN);
                    } else {
                        double d;
                        if (isNegative) {
                            d = Double.NEGATIVE_INFINITY;
                        } else {
                            d = Double.POSITIVE_INFINITY;
                        }
                        loadDouble = loadDouble(d);
                    }
                    return loadDouble;
                }
                throw new NumberFormatException("For input string: \"" + in + "\"");
            }
            int decExp;
            if (c == '0' && l > i + 1) {
                char ch = in.charAt(i + 1);
                if (ch == 'x' || ch == 'X') {
                    return parseHexString(in);
                }
            }
            char[] digits = new char[l];
            int nDigits = 0;
            boolean decSeen = f26-assertionsDisabled;
            int decPt = 0;
            int nLeadZero = 0;
            int nTrailZero = 0;
            while (i < l) {
                c = in.charAt(i);
                switch (c) {
                    case ZipConstants.CENHDR /*46*/:
                        if (decSeen) {
                            throw new NumberFormatException("multiple points");
                        }
                        decPt = i;
                        if (signSeen) {
                            decPt--;
                        }
                        decSeen = true;
                        continue;
                    case '0':
                        if (nDigits <= 0) {
                            nLeadZero++;
                            break;
                        }
                        nTrailZero++;
                        continue;
                    case '1':
                    case '2':
                    case '3':
                    case expShift /*52*/:
                    case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        int nDigits2 = nDigits;
                        while (nTrailZero > 0) {
                            nDigits = nDigits2 + 1;
                            digits[nDigits2] = '0';
                            nTrailZero--;
                            nDigits2 = nDigits;
                        }
                        nDigits = nDigits2 + 1;
                        digits[nDigits2] = c;
                        continue;
                    default:
                        break;
                }
                if (nDigits == 0) {
                    digits = zero;
                    nDigits = 1;
                }
                if (decSeen) {
                    decExp = nDigits + nTrailZero;
                } else {
                    decExp = decPt - nLeadZero;
                }
                if (i < l) {
                    c = in.charAt(i);
                    if (c == 'e' || c == 'E') {
                        int expSign = 1;
                        int expVal = 0;
                        boolean expOverflow = f26-assertionsDisabled;
                        i++;
                        switch (in.charAt(i)) {
                            case '+':
                                break;
                            case '-':
                                expSign = -1;
                                break;
                        }
                        i++;
                        int expAt = i;
                        while (true) {
                            int i2 = i;
                            if (i2 < l) {
                                if (expVal >= 214748364) {
                                    expOverflow = true;
                                }
                                i = i2 + 1;
                                c = in.charAt(i2);
                                switch (c) {
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case expShift /*52*/:
                                    case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                        expVal = (expVal * 10) + (c - 48);
                                    default:
                                        i--;
                                        break;
                                }
                            }
                            i = i2;
                        }
                        int expLimit = (nDigits + bigDecimalExponent) + nTrailZero;
                        if (expOverflow || expVal > expLimit) {
                            decExp = expSign * expLimit;
                        } else {
                            decExp += expSign * expVal;
                        }
                    }
                }
                if (i >= l || (i == l - 1 && (in.charAt(i) == 'f' || in.charAt(i) == 'F' || in.charAt(i) == 'd' || in.charAt(i) == 'D'))) {
                    this.isNegative = isNegative;
                    this.decExponent = decExp;
                    this.digits = digits;
                    this.nDigits = nDigits;
                    this.isExceptional = f26-assertionsDisabled;
                    return this;
                }
                throw new NumberFormatException("For input string: \"" + in + "\"");
            }
            if (nDigits == 0) {
            }
            if (decSeen) {
            }
            if (i < l) {
            }
            this.isNegative = isNegative;
            this.decExponent = decExp;
            this.digits = digits;
            this.nDigits = nDigits;
            this.isExceptional = f26-assertionsDisabled;
            return this;
        } catch (StringIndexOutOfBoundsException e) {
        }
    }

    public double doubleValue() {
        int kDigits = Math.min(this.nDigits, 16);
        if (this.digits != infinity && this.digits != notANumber) {
            int i;
            int i2;
            if (this.mustSetRoundDir) {
                this.roundDir = 0;
            }
            int iValue = this.digits[0] - 48;
            int iDigits = Math.min(kDigits, 9);
            for (i = 1; i < iDigits; i++) {
                iValue = ((iValue * 10) + this.digits[i]) - 48;
            }
            long lValue = (long) iValue;
            for (i = iDigits; i < kDigits; i++) {
                lValue = (10 * lValue) + ((long) (this.digits[i] - 48));
            }
            double dValue = (double) lValue;
            int exp = this.decExponent - kDigits;
            if (this.nDigits <= 15) {
                double rValue;
                double tValue;
                if (exp == 0 || dValue == 0.0d) {
                    if (this.isNegative) {
                        dValue = -dValue;
                    }
                    return dValue;
                } else if (exp >= 0) {
                    if (exp <= maxSmallTen) {
                        rValue = dValue * small10pow[exp];
                        if (this.mustSetRoundDir) {
                            tValue = rValue / small10pow[exp];
                            if (tValue == dValue) {
                                i2 = 0;
                            } else if (tValue < dValue) {
                                i2 = 1;
                            } else {
                                i2 = -1;
                            }
                            this.roundDir = i2;
                        }
                        if (this.isNegative) {
                            rValue = -rValue;
                        }
                        return rValue;
                    }
                    int slop = 15 - kDigits;
                    if (exp <= maxSmallTen + slop) {
                        dValue *= small10pow[slop];
                        rValue = dValue * small10pow[exp - slop];
                        if (this.mustSetRoundDir) {
                            tValue = rValue / small10pow[exp - slop];
                            if (tValue == dValue) {
                                i2 = 0;
                            } else if (tValue < dValue) {
                                i2 = 1;
                            } else {
                                i2 = -1;
                            }
                            this.roundDir = i2;
                        }
                        if (this.isNegative) {
                            rValue = -rValue;
                        }
                        return rValue;
                    }
                } else if (exp >= (-maxSmallTen)) {
                    rValue = dValue / small10pow[-exp];
                    tValue = rValue * small10pow[-exp];
                    if (this.mustSetRoundDir) {
                        if (tValue == dValue) {
                            i2 = 0;
                        } else if (tValue < dValue) {
                            i2 = 1;
                        } else {
                            i2 = -1;
                        }
                        this.roundDir = i2;
                    }
                    if (this.isNegative) {
                        rValue = -rValue;
                    }
                    return rValue;
                }
            }
            int j;
            double t;
            if (exp > 0) {
                if (this.decExponent > 309) {
                    return this.isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                }
                if ((exp & 15) != 0) {
                    dValue *= small10pow[exp & 15];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = 0;
                    while (exp > 1) {
                        if ((exp & 1) != 0) {
                            dValue *= big10pow[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                    t = dValue * big10pow[j];
                    if (Double.isInfinite(t)) {
                        if (Double.isInfinite((dValue / 2.0d) * big10pow[j])) {
                            return this.isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        }
                        t = Double.MAX_VALUE;
                    }
                    dValue = t;
                }
            } else if (exp < 0) {
                exp = -exp;
                if (this.decExponent < -325) {
                    return this.isNegative ? -0.0d : 0.0d;
                }
                if ((exp & 15) != 0) {
                    dValue /= small10pow[exp & 15];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = 0;
                    while (exp > 1) {
                        if ((exp & 1) != 0) {
                            dValue *= tiny10pow[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                    t = dValue * tiny10pow[j];
                    if (t == 0.0d) {
                        if ((dValue * 2.0d) * tiny10pow[j] == 0.0d) {
                            return this.isNegative ? -0.0d : 0.0d;
                        }
                        t = Double.MIN_VALUE;
                    }
                    dValue = t;
                }
            }
            FDBigInt bigD0 = new FDBigInt(lValue, this.digits, kDigits, this.nDigits);
            exp = this.decExponent - this.nDigits;
            do {
                int B5;
                int B2;
                int D5;
                int D2;
                int hulpbias;
                boolean overvalue;
                FDBigInt diff;
                FDBigInt bigB = doubleToBigInt(dValue);
                if (exp >= 0) {
                    B5 = 0;
                    B2 = 0;
                    D5 = exp;
                    D2 = exp;
                } else {
                    B5 = -exp;
                    B2 = B5;
                    D5 = 0;
                    D2 = 0;
                }
                if (this.bigIntExp >= 0) {
                    B2 += this.bigIntExp;
                } else {
                    D2 -= this.bigIntExp;
                }
                int Ulp2 = B2;
                if (this.bigIntExp + this.bigIntNBits <= -1022) {
                    hulpbias = (this.bigIntExp + 1023) + expShift;
                } else {
                    hulpbias = 54 - this.bigIntNBits;
                }
                B2 += hulpbias;
                D2 += hulpbias;
                int common2 = Math.min(B2, Math.min(D2, Ulp2));
                D2 -= common2;
                Ulp2 -= common2;
                bigB = multPow52(bigB, B5, B2 - common2);
                FDBigInt bigD = multPow52(new FDBigInt(bigD0), D5, D2);
                int cmpResult = bigB.cmp(bigD);
                if (cmpResult <= 0) {
                    if (cmpResult >= 0) {
                        break;
                    }
                    overvalue = f26-assertionsDisabled;
                    diff = bigD.sub(bigB);
                } else {
                    overvalue = true;
                    diff = bigB.sub(bigD);
                    if (this.bigIntNBits == 1 && this.bigIntExp > -1022) {
                        Ulp2--;
                        if (Ulp2 < 0) {
                            Ulp2 = 0;
                            diff.lshiftMe(1);
                        }
                    }
                }
                cmpResult = diff.cmp(constructPow52(B5, Ulp2));
                if (cmpResult >= 0) {
                    if (cmpResult != 0) {
                        dValue += ulp(dValue, overvalue);
                        if (dValue == 0.0d) {
                            break;
                        }
                    } else {
                        dValue += ulp(dValue, overvalue) * 0.5d;
                        if (this.mustSetRoundDir) {
                            this.roundDir = overvalue ? -1 : 1;
                        }
                    }
                } else if (this.mustSetRoundDir) {
                    if (overvalue) {
                        i2 = -1;
                    } else {
                        i2 = 1;
                    }
                    this.roundDir = i2;
                }
            } while (dValue != Double.POSITIVE_INFINITY);
            if (this.isNegative) {
                dValue = -dValue;
            }
            return dValue;
        } else if (this.digits == notANumber) {
            return Double.NaN;
        } else {
            return this.isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
    }

    public float floatValue() {
        int kDigits = Math.min(this.nDigits, 8);
        if (this.digits != infinity && this.digits != notANumber) {
            int i;
            int iValue = this.digits[0] - 48;
            for (i = 1; i < kDigits; i++) {
                iValue = ((iValue * 10) + this.digits[i]) - 48;
            }
            float fValue = (float) iValue;
            int exp = this.decExponent - kDigits;
            if (this.nDigits <= 7) {
                if (exp == 0 || fValue == 0.0f) {
                    if (this.isNegative) {
                        fValue = -fValue;
                    }
                    return fValue;
                } else if (exp >= 0) {
                    if (exp <= singleMaxSmallTen) {
                        fValue *= singleSmall10pow[exp];
                        if (this.isNegative) {
                            fValue = -fValue;
                        }
                        return fValue;
                    }
                    int slop = 7 - kDigits;
                    if (exp <= singleMaxSmallTen + slop) {
                        fValue = (fValue * singleSmall10pow[slop]) * singleSmall10pow[exp - slop];
                        if (this.isNegative) {
                            fValue = -fValue;
                        }
                        return fValue;
                    }
                } else if (exp >= (-singleMaxSmallTen)) {
                    fValue /= singleSmall10pow[-exp];
                    if (this.isNegative) {
                        fValue = -fValue;
                    }
                    return fValue;
                }
            } else if (this.decExponent >= this.nDigits && this.nDigits + this.decExponent <= 15) {
                long lValue = (long) iValue;
                for (i = kDigits; i < this.nDigits; i++) {
                    lValue = (10 * lValue) + ((long) (this.digits[i] - 48));
                }
                fValue = (float) (((double) lValue) * small10pow[this.decExponent - this.nDigits]);
                if (this.isNegative) {
                    fValue = -fValue;
                }
                return fValue;
            }
            if (this.decExponent > 39) {
                return this.isNegative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            } else if (this.decExponent < -46) {
                return this.isNegative ? -0.0f : 0.0f;
            } else {
                this.mustSetRoundDir = this.fromHex ? f26-assertionsDisabled : true;
                return stickyRound(doubleValue());
            }
        } else if (this.digits == notANumber) {
            return Float.NaN;
        } else {
            return this.isNegative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        }
    }

    private static synchronized Pattern getHexFloatPattern() {
        Pattern pattern;
        synchronized (FloatingDecimal.class) {
            if (hexFloatPattern == null) {
                hexFloatPattern = Pattern.compile("([-+])?0[xX](((\\p{XDigit}+)\\.?)|((\\p{XDigit}*)\\.(\\p{XDigit}+)))[pP]([-+])?(\\p{Digit}+)[fFdD]?");
            }
            pattern = hexFloatPattern;
        }
        return pattern;
    }

    /* JADX WARNING: Missing block: B:69:0x01e6, code:
            r17 = r17 + 1;
     */
    /* JADX WARNING: Missing block: B:70:0x01ea, code:
            if (r17 >= r34) goto L_0x01ee;
     */
    /* JADX WARNING: Missing block: B:71:0x01ec, code:
            if (r38 == false) goto L_0x0277;
     */
    /* JADX WARNING: Missing block: B:101:0x0277, code:
            r6 = (long) getHexDigit(r35, r17);
     */
    /* JADX WARNING: Missing block: B:102:0x0282, code:
            if (r38 != 0) goto L_0x028a;
     */
    /* JADX WARNING: Missing block: B:104:0x0288, code:
            if (r6 == 0) goto L_0x0290;
     */
    /* JADX WARNING: Missing block: B:105:0x028a, code:
            r38 = true;
     */
    /* JADX WARNING: Missing block: B:107:0x0290, code:
            r38 = -assertionsDisabled;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    FloatingDecimal parseHexString(String s) {
        Matcher m = getHexFloatPattern().matcher(s);
        if (m.matches()) {
            double sign;
            String significandString;
            int leftDigits;
            int exponentAdjust;
            String group1 = m.group(1);
            if (group1 == null || group1.equals("+")) {
                sign = 1.0d;
            } else {
                sign = -1.0d;
            }
            int rightDigits = 0;
            String group4 = m.group(4);
            if (group4 != null) {
                significandString = stripLeadingZeros(group4);
                leftDigits = significandString.length();
            } else {
                String group6 = stripLeadingZeros(m.group(6));
                leftDigits = group6.length();
                String group7 = m.group(7);
                rightDigits = group7.length();
                StringBuilder stringBuilder = new StringBuilder();
                if (group6 == null) {
                    group6 = "";
                }
                significandString = stringBuilder.append(group6).append(group7).toString();
            }
            significandString = stripLeadingZeros(significandString);
            int signifLength = significandString.length();
            if (leftDigits >= 1) {
                exponentAdjust = (leftDigits - 1) * 4;
            } else {
                exponentAdjust = ((rightDigits - signifLength) + 1) * -4;
            }
            if (signifLength == 0) {
                return loadDouble(0.0d * sign);
            }
            String group8 = m.group(8);
            boolean positiveExponent = group8 != null ? group8.equals("+") : true;
            try {
                long significand;
                int nextShift;
                long exponent = ((positiveExponent ? 1 : -1) * ((long) Integer.parseInt(m.group(9)))) + ((long) exponentAdjust);
                boolean round = f26-assertionsDisabled;
                int sticky = f26-assertionsDisabled;
                long leadingDigit = (long) getHexDigit(significandString, 0);
                if (leadingDigit == 1) {
                    significand = 0 | (leadingDigit << expShift);
                    nextShift = 48;
                } else if (leadingDigit <= 3) {
                    significand = 0 | (leadingDigit << 51);
                    nextShift = 47;
                    exponent++;
                } else if (leadingDigit <= 7) {
                    significand = 0 | (leadingDigit << 50);
                    nextShift = 46;
                    exponent += 2;
                } else if (leadingDigit <= 15) {
                    significand = 0 | (leadingDigit << 49);
                    nextShift = 45;
                    exponent += 3;
                } else {
                    throw new AssertionError((Object) "Result from digit conversion too large!");
                }
                int i = 1;
                while (i < signifLength && nextShift >= 0) {
                    significand |= ((long) getHexDigit(significandString, i)) << nextShift;
                    nextShift -= 4;
                    i++;
                }
                if (i < signifLength) {
                    long currentDigit = (long) getHexDigit(significandString, i);
                    switch (nextShift) {
                        case Types.LONGVARBINARY /*-4*/:
                            round = (8 & currentDigit) != 0 ? true : f26-assertionsDisabled;
                            if ((7 & currentDigit) == 0) {
                                sticky = f26-assertionsDisabled;
                                break;
                            }
                            sticky = true;
                            break;
                        case -3:
                            significand |= (8 & currentDigit) >> 3;
                            round = (4 & currentDigit) != 0 ? true : f26-assertionsDisabled;
                            if ((3 & currentDigit) == 0) {
                                sticky = f26-assertionsDisabled;
                                break;
                            }
                            sticky = true;
                            break;
                        case -2:
                            significand |= (12 & currentDigit) >> 2;
                            round = (2 & currentDigit) != 0 ? true : f26-assertionsDisabled;
                            if ((1 & currentDigit) == 0) {
                                sticky = f26-assertionsDisabled;
                                break;
                            }
                            sticky = true;
                            break;
                        case -1:
                            significand |= (14 & currentDigit) >> 1;
                            if ((1 & currentDigit) == 0) {
                                round = f26-assertionsDisabled;
                                break;
                            }
                            round = true;
                            break;
                        default:
                            throw new AssertionError((Object) "Unexpected shift distance remainder.");
                    }
                }
                if (exponent > 1023) {
                    return loadDouble(Double.POSITIVE_INFINITY * sign);
                }
                if (exponent <= 1023 && exponent >= -1022) {
                    significand = (((1023 + exponent) << expShift) & 9218868437227405312L) | (4503599627370495L & significand);
                } else if (exponent < -1075) {
                    return loadDouble(0.0d * sign);
                } else {
                    sticky = sticky == 0 ? round : 1;
                    int bitsDiscarded = 53 - ((((int) exponent) + 1074) + 1);
                    if (!f26-assertionsDisabled) {
                        Object obj;
                        if (bitsDiscarded < 1 || bitsDiscarded > 53) {
                            obj = null;
                        } else {
                            obj = 1;
                        }
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    round = ((1 << (bitsDiscarded + -1)) & significand) != 0 ? true : f26-assertionsDisabled;
                    if (bitsDiscarded > 1) {
                        sticky = (sticky == 0 && (significand & (~(-1 << (bitsDiscarded - 1)))) == 0) ? 0 : 1;
                    }
                    significand = 0 | (4503599627370495L & (significand >> bitsDiscarded));
                }
                boolean leastZero = (1 & significand) == 0 ? true : f26-assertionsDisabled;
                if ((leastZero && round && sticky != 0) || (!leastZero && round)) {
                    significand++;
                }
                loadDouble(FpUtils.rawCopySign(Double.longBitsToDouble(significand), sign));
                if (exponent >= -150 && exponent <= 127 && (268435455 & significand) == 0 && (round || sticky != 0)) {
                    if (leastZero) {
                        if ((round ^ sticky) != 0) {
                            this.roundDir = 1;
                        }
                    } else if (round) {
                        this.roundDir = -1;
                    }
                }
                this.fromHex = true;
                return this;
            } catch (NumberFormatException e) {
                return loadDouble((positiveExponent ? Double.POSITIVE_INFINITY : 0.0d) * sign);
            }
        }
        throw new NumberFormatException("For input string: \"" + s + "\"");
    }

    static String stripLeadingZeros(String s) {
        return s.replaceFirst("^0+", "");
    }

    static int getHexDigit(String s, int position) {
        int value = Character.digit(s.charAt(position), 16);
        if (value > -1 && value < 16) {
            return value;
        }
        throw new AssertionError("Unexpected failure of digit conversion of " + s.charAt(position));
    }
}
