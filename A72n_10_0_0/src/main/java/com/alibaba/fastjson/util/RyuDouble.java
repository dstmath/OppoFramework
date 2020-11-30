package com.alibaba.fastjson.util;

import java.lang.reflect.Array;
import java.math.BigInteger;

public final class RyuDouble {
    private static final int[][] POW5_INV_SPLIT = ((int[][]) Array.newInstance(int.class, 291, 4));
    private static final int[][] POW5_SPLIT = ((int[][]) Array.newInstance(int.class, 326, 4));

    static {
        BigInteger mask = BigInteger.ONE.shiftLeft(31).subtract(BigInteger.ONE);
        BigInteger invMask = BigInteger.ONE.shiftLeft(31).subtract(BigInteger.ONE);
        int i = 0;
        while (i < 326) {
            BigInteger pow = BigInteger.valueOf(5).pow(i);
            int pow5len = pow.bitLength();
            int expectedPow5Bits = i == 0 ? 1 : (int) ((((((long) i) * 23219280) + 10000000) - 1) / 10000000);
            if (expectedPow5Bits == pow5len) {
                if (i < POW5_SPLIT.length) {
                    for (int j = 0; j < 4; j++) {
                        POW5_SPLIT[i][j] = pow.shiftRight((pow5len - 121) + ((3 - j) * 31)).and(mask).intValue();
                    }
                }
                if (i < POW5_INV_SPLIT.length) {
                    BigInteger inv = BigInteger.ONE.shiftLeft(pow5len + 121).divide(pow).add(BigInteger.ONE);
                    for (int k = 0; k < 4; k++) {
                        if (k == 0) {
                            POW5_INV_SPLIT[i][k] = inv.shiftRight((3 - k) * 31).intValue();
                        } else {
                            POW5_INV_SPLIT[i][k] = inv.shiftRight((3 - k) * 31).and(invMask).intValue();
                        }
                    }
                }
                i++;
            } else {
                throw new IllegalStateException(pow5len + " != " + expectedPow5Bits);
            }
        }
    }

    public static String toString(double value) {
        char[] result = new char[24];
        return new String(result, 0, toString(value, result, 0));
    }

    /* JADX INFO: Multiple debug info for r1v11 int: [D('i' int), D('index' int)] */
    /* JADX INFO: Multiple debug info for r0v25 int: [D('dm' long), D('c' int)] */
    /* JADX INFO: Multiple debug info for r2v20 long: [D('i' int), D('bits03' long)] */
    /* JADX INFO: Multiple debug info for r0v34 long: [D('q' int), D('bits11' long)] */
    /* JADX INFO: Multiple debug info for r11v15 long: [D('bits12' long), D('bits10' long)] */
    /* JADX INFO: Multiple debug info for r7v4 long: [D('bits00' long), D('mHigh' long)] */
    /* JADX INFO: Multiple debug info for r0v35 long: [D('bits11' long), D('dv' long)] */
    /* JADX INFO: Multiple debug info for r2v21 long: [D('bits03' long), D('mHigh' long)] */
    /* JADX INFO: Multiple debug info for r0v37 long: [D('bits11' long), D('dv' long)] */
    /* JADX INFO: Multiple debug info for r12v61 long: [D('bits12' long), D('bits10' long)] */
    /* JADX INFO: Multiple debug info for r2v23 long: [D('bits00' long), D('mHigh' long)] */
    /* JADX INFO: Multiple debug info for r0v38 long: [D('bits11' long), D('mHigh' long)] */
    /* JADX INFO: Multiple debug info for r7v14 long: [D('bits01' long), D('bits13' long)] */
    /* JADX INFO: Multiple debug info for r11v20 long: [D('bits12' long), D('bits10' long)] */
    /* JADX INFO: Multiple debug info for r0v40 long: [D('bits00' long), D('mHigh' long)] */
    /* JADX INFO: Multiple debug info for r0v47 int: [D('i' int), D('actualShift' int)] */
    /* JADX INFO: Multiple debug info for r0v50 long: [D('actualShift' int), D('bits01' long)] */
    /* JADX INFO: Multiple debug info for r4v40 long: [D('q' int), D('bits10' long)] */
    /* JADX INFO: Multiple debug info for r0v51 long: [D('bits01' long), D('dv' long)] */
    /* JADX INFO: Multiple debug info for r2v30 long: [D('bits11' long), D('mHigh' long)] */
    /* JADX INFO: Multiple debug info for r0v53 long: [D('bits02' long), D('dv' long)] */
    /* JADX INFO: Multiple debug info for r13v18 long: [D('bits12' long), D('bits10' long)] */
    /* JADX INFO: Multiple debug info for r2v32 long: [D('bits00' long), D('mHigh' long)] */
    /* JADX INFO: Multiple debug info for r0v54 long: [D('dp' long), D('bits02' long)] */
    /* JADX INFO: Multiple debug info for r2v33 long: [D('bits00' long), D('mHigh' long)] */
    /* JADX INFO: Multiple debug info for r0v56 long: [D('bits11' long), D('dp' long)] */
    /* JADX INFO: Multiple debug info for r11v29 long: [D('bits12' long), D('bits10' long)] */
    /* JADX INFO: Multiple debug info for r2v35 long: [D('bits00' long), D('mHigh' long)] */
    /* JADX INFO: Multiple debug info for r3v48 long: [D('q' int), D('dp' long)] */
    public static int toString(double value, char[] result, int off) {
        long m2;
        int e2;
        long dv;
        long dp;
        int index;
        boolean dmIsTrailingZeros;
        boolean dvIsTrailingZeros;
        int e10;
        long bits11;
        int vplength;
        long output;
        int index2;
        int index3;
        boolean dmIsTrailingZeros2;
        int e22;
        int i;
        boolean dmIsTrailingZeros3;
        int pow5Factor_mp;
        int pow5Factor_mm;
        int pow5Factor_mv;
        if (Double.isNaN(value)) {
            int index4 = off + 1;
            result[off] = 'N';
            int index5 = index4 + 1;
            result[index4] = 'a';
            result[index5] = 'N';
            return (index5 + 1) - off;
        } else if (value == Double.POSITIVE_INFINITY) {
            int index6 = off + 1;
            result[off] = 'I';
            int index7 = index6 + 1;
            result[index6] = 'n';
            int index8 = index7 + 1;
            result[index7] = 'f';
            int index9 = index8 + 1;
            result[index8] = 'i';
            int index10 = index9 + 1;
            result[index9] = 'n';
            int index11 = index10 + 1;
            result[index10] = 'i';
            int index12 = index11 + 1;
            result[index11] = 't';
            result[index12] = 'y';
            return (index12 + 1) - off;
        } else if (value == Double.NEGATIVE_INFINITY) {
            int index13 = off + 1;
            result[off] = '-';
            int index14 = index13 + 1;
            result[index13] = 'I';
            int index15 = index14 + 1;
            result[index14] = 'n';
            int index16 = index15 + 1;
            result[index15] = 'f';
            int index17 = index16 + 1;
            result[index16] = 'i';
            int index18 = index17 + 1;
            result[index17] = 'n';
            int index19 = index18 + 1;
            result[index18] = 'i';
            int index20 = index19 + 1;
            result[index19] = 't';
            result[index20] = 'y';
            return (index20 + 1) - off;
        } else {
            long bits = Double.doubleToLongBits(value);
            if (bits == 0) {
                int index21 = off + 1;
                result[off] = '0';
                int index22 = index21 + 1;
                result[index21] = '.';
                result[index22] = '0';
                return (index22 + 1) - off;
            } else if (bits == Long.MIN_VALUE) {
                int index23 = off + 1;
                result[off] = '-';
                int index24 = index23 + 1;
                result[index23] = '0';
                int index25 = index24 + 1;
                result[index24] = '.';
                result[index25] = '0';
                return (index25 + 1) - off;
            } else {
                int ieeeExponent = (int) ((bits >>> 52) & 2047);
                long ieeeMantissa = bits & 4503599627370495L;
                if (ieeeExponent == 0) {
                    e2 = -1074;
                    m2 = ieeeMantissa;
                } else {
                    e2 = (ieeeExponent - 1023) - 52;
                    m2 = ieeeMantissa | 4503599627370496L;
                }
                boolean sign = bits < 0;
                boolean even = (m2 & 1) == 0;
                long mv = 4 * m2;
                long mp = (4 * m2) + 2;
                int mmShift = (m2 != 4503599627370496L || ieeeExponent <= 1) ? 1 : 0;
                long mm = ((4 * m2) - 1) - ((long) mmShift);
                int e23 = e2 - 2;
                if (e23 >= 0) {
                    int q = Math.max(0, ((int) ((((long) e23) * 3010299) / 10000000)) - 1);
                    if (q == 0) {
                        e22 = e23;
                        i = 1;
                    } else {
                        e22 = e23;
                        i = (int) ((((((long) q) * 23219280) + 10000000) - 1) / 10000000);
                    }
                    int actualShift = ((((-e22) + q) + ((122 + i) - 1)) - 93) - 21;
                    if (actualShift >= 0) {
                        int[] ints = POW5_INV_SPLIT[q];
                        long mHigh = mv >>> 31;
                        long mLow = mv & 2147483647L;
                        long dv2 = ((((((((((((((long) ints[3]) * mLow) >>> 31) + (((long) ints[2]) * mLow)) + (((long) ints[3]) * mHigh)) >>> 31) + (((long) ints[1]) * mLow)) + (((long) ints[2]) * mHigh)) >>> 31) + (((long) ints[0]) * mLow)) + (((long) ints[1]) * mHigh)) >>> 21) + ((((long) ints[0]) * mHigh) << 10)) >>> actualShift;
                        long mHigh2 = mp >>> 31;
                        long mLow2 = mp & 2147483647L;
                        long bits13 = ((long) ints[0]) * mHigh2;
                        long bits12 = ((long) ints[1]) * mHigh2;
                        index = off;
                        long j = (((((((((long) ints[3]) * mLow2) >>> 31) + (((long) ints[2]) * mLow2)) + (((long) ints[3]) * mHigh2)) >>> 31) + (((long) ints[1]) * mLow2)) + (((long) ints[2]) * mHigh2)) >>> 31;
                        long mHigh3 = mm >>> 31;
                        long mLow3 = mm & 2147483647L;
                        long bits132 = ((long) ints[0]) * mHigh3;
                        long bits122 = ((long) ints[1]) * mHigh3;
                        dp = ((((j + (((long) ints[0]) * mLow2)) + bits12) >>> 21) + (bits13 << 10)) >>> actualShift;
                        bits11 = ((((((((((((((long) ints[3]) * mLow3) >>> 31) + (((long) ints[2]) * mLow3)) + (((long) ints[3]) * mHigh3)) >>> 31) + (((long) ints[1]) * mLow3)) + (((long) ints[2]) * mHigh3)) >>> 31) + (((long) ints[0]) * mLow3)) + bits122) >>> 21) + (bits132 << 10)) >>> actualShift;
                        e10 = q;
                        if (q <= 21) {
                            if (mv % 5 == 0) {
                                if (mv % 5 != 0) {
                                    pow5Factor_mv = 0;
                                } else if (mv % 25 != 0) {
                                    pow5Factor_mv = 1;
                                } else if (mv % 125 != 0) {
                                    pow5Factor_mv = 2;
                                } else if (mv % 625 != 0) {
                                    pow5Factor_mv = 3;
                                } else {
                                    int pow5Factor_mv2 = 4;
                                    long v = mv / 625;
                                    while (v > 0 && v % 5 == 0) {
                                        v /= 5;
                                        pow5Factor_mv2++;
                                    }
                                    pow5Factor_mv = pow5Factor_mv2;
                                }
                                dvIsTrailingZeros = pow5Factor_mv >= q;
                                dmIsTrailingZeros3 = false;
                                dmIsTrailingZeros = dmIsTrailingZeros3;
                                dv = dv2;
                            } else if (even) {
                                if (mm % 5 != 0) {
                                    pow5Factor_mm = 0;
                                } else if (mm % 25 != 0) {
                                    pow5Factor_mm = 1;
                                } else if (mm % 125 != 0) {
                                    pow5Factor_mm = 2;
                                } else if (mm % 625 != 0) {
                                    pow5Factor_mm = 3;
                                } else {
                                    int pow5Factor_mm2 = 4;
                                    long v2 = mm / 625;
                                    while (v2 > 0 && v2 % 5 == 0) {
                                        v2 /= 5;
                                        pow5Factor_mm2++;
                                    }
                                    pow5Factor_mm = pow5Factor_mm2;
                                }
                                dmIsTrailingZeros3 = pow5Factor_mm >= q;
                                dvIsTrailingZeros = false;
                                dmIsTrailingZeros = dmIsTrailingZeros3;
                                dv = dv2;
                            } else {
                                if (mp % 5 != 0) {
                                    pow5Factor_mp = 0;
                                } else if (mp % 25 != 0) {
                                    pow5Factor_mp = 1;
                                } else if (mp % 125 != 0) {
                                    pow5Factor_mp = 2;
                                } else if (mp % 625 != 0) {
                                    pow5Factor_mp = 3;
                                } else {
                                    int pow5Factor_mp2 = 4;
                                    long v3 = mp / 625;
                                    while (v3 > 0 && v3 % 5 == 0) {
                                        v3 /= 5;
                                        pow5Factor_mp2++;
                                    }
                                    pow5Factor_mp = pow5Factor_mp2;
                                }
                                if (pow5Factor_mp >= q) {
                                    dp--;
                                }
                            }
                        }
                        dmIsTrailingZeros3 = false;
                        dvIsTrailingZeros = false;
                        dmIsTrailingZeros = dmIsTrailingZeros3;
                        dv = dv2;
                    } else {
                        throw new IllegalArgumentException("" + actualShift);
                    }
                } else {
                    dmIsTrailingZeros = false;
                    index = off;
                    int q2 = Math.max(0, ((int) ((((long) (-e23)) * 6989700) / 10000000)) - 1);
                    int i2 = (-e23) - q2;
                    int actualShift2 = ((q2 - ((i2 == 0 ? 1 : (int) ((((((long) i2) * 23219280) + 10000000) - 1) / 10000000)) - 121)) - 93) - 21;
                    if (actualShift2 >= 0) {
                        int[] ints2 = POW5_SPLIT[i2];
                        long mHigh4 = mv >>> 31;
                        long mLow4 = mv & 2147483647L;
                        long bits133 = ((long) ints2[0]) * mHigh4;
                        long bits123 = ((long) ints2[1]) * mHigh4;
                        long j2 = (((((((((long) ints2[3]) * mLow4) >>> 31) + (((long) ints2[2]) * mLow4)) + (((long) ints2[3]) * mHigh4)) >>> 31) + (((long) ints2[1]) * mLow4)) + (((long) ints2[2]) * mHigh4)) >>> 31;
                        long mHigh5 = mp >>> 31;
                        long mLow5 = mp & 2147483647L;
                        long bits134 = ((long) ints2[0]) * mHigh5;
                        long bits124 = ((long) ints2[1]) * mHigh5;
                        dv = ((((j2 + (((long) ints2[0]) * mLow4)) + bits123) >>> 21) + (bits133 << 10)) >>> actualShift2;
                        dp = ((((((((((((((long) ints2[3]) * mLow5) >>> 31) + (((long) ints2[2]) * mLow5)) + (((long) ints2[3]) * mHigh5)) >>> 31) + (((long) ints2[1]) * mLow5)) + (((long) ints2[2]) * mHigh5)) >>> 31) + (((long) ints2[0]) * mLow5)) + bits124) >>> 21) + (bits134 << 10)) >>> actualShift2;
                        long mHigh6 = mm >>> 31;
                        long mLow6 = mm & 2147483647L;
                        bits11 = ((((((((((((((long) ints2[3]) * mLow6) >>> 31) + (((long) ints2[2]) * mLow6)) + (((long) ints2[3]) * mHigh6)) >>> 31) + (((long) ints2[1]) * mLow6)) + (((long) ints2[2]) * mHigh6)) >>> 31) + (((long) ints2[0]) * mLow6)) + (((long) ints2[1]) * mHigh6)) >>> 21) + ((((long) ints2[0]) * mHigh6) << 10)) >>> actualShift2;
                        e10 = q2 + e23;
                        if (q2 <= 1) {
                            dvIsTrailingZeros = true;
                            if (even) {
                                if (mmShift == 1) {
                                    dmIsTrailingZeros2 = true;
                                } else {
                                    dmIsTrailingZeros2 = false;
                                }
                                dmIsTrailingZeros = dmIsTrailingZeros2;
                            } else {
                                dp--;
                            }
                        } else if (q2 < 63) {
                            dvIsTrailingZeros = (mv & ((1 << (q2 + -1)) - 1)) == 0;
                        } else {
                            dvIsTrailingZeros = false;
                        }
                    } else {
                        throw new IllegalArgumentException("" + actualShift2);
                    }
                }
                if (dp >= 1000000000000000000L) {
                    vplength = 19;
                } else if (dp >= 100000000000000000L) {
                    vplength = 18;
                } else if (dp >= 10000000000000000L) {
                    vplength = 17;
                } else if (dp >= 1000000000000000L) {
                    vplength = 16;
                } else if (dp >= 100000000000000L) {
                    vplength = 15;
                } else if (dp >= 10000000000000L) {
                    vplength = 14;
                } else if (dp >= 1000000000000L) {
                    vplength = 13;
                } else if (dp >= 100000000000L) {
                    vplength = 12;
                } else if (dp >= 10000000000L) {
                    vplength = 11;
                } else if (dp >= 1000000000) {
                    vplength = 10;
                } else if (dp >= 100000000) {
                    vplength = 9;
                } else if (dp >= 10000000) {
                    vplength = 8;
                } else if (dp >= 1000000) {
                    vplength = 7;
                } else if (dp >= 100000) {
                    vplength = 6;
                } else if (dp >= 10000) {
                    vplength = 5;
                } else if (dp >= 1000) {
                    vplength = 4;
                } else if (dp >= 100) {
                    vplength = 3;
                } else if (dp >= 10) {
                    vplength = 2;
                } else {
                    vplength = 1;
                }
                int exp = (e10 + vplength) - 1;
                boolean scientificNotation = exp < -3 || exp >= 7;
                int removed = 0;
                int lastRemovedDigit = 0;
                if (dmIsTrailingZeros || dvIsTrailingZeros) {
                    while (dp / 10 > bits11 / 10 && (dp >= 100 || !scientificNotation)) {
                        dmIsTrailingZeros &= bits11 % 10 == 0;
                        dvIsTrailingZeros &= lastRemovedDigit == 0;
                        lastRemovedDigit = (int) (dv % 10);
                        dp /= 10;
                        dv /= 10;
                        bits11 /= 10;
                        removed++;
                    }
                    if (dmIsTrailingZeros && even) {
                        while (bits11 % 10 == 0 && (dp >= 100 || !scientificNotation)) {
                            dvIsTrailingZeros &= lastRemovedDigit == 0;
                            lastRemovedDigit = (int) (dv % 10);
                            dp /= 10;
                            dv /= 10;
                            bits11 /= 10;
                            removed++;
                        }
                    }
                    if (dvIsTrailingZeros && lastRemovedDigit == 5 && dv % 2 == 0) {
                        lastRemovedDigit = 4;
                    }
                    output = dv + ((long) (((dv != bits11 || (dmIsTrailingZeros && even)) && lastRemovedDigit < 5) ? 0 : 1));
                } else {
                    while (dp / 10 > bits11 / 10 && (dp >= 100 || !scientificNotation)) {
                        lastRemovedDigit = (int) (dv % 10);
                        dp /= 10;
                        dv /= 10;
                        bits11 /= 10;
                        removed++;
                    }
                    output = dv + ((long) ((dv == bits11 || lastRemovedDigit >= 5) ? 1 : 0));
                }
                int olength = vplength - removed;
                if (sign) {
                    index2 = index + 1;
                    result[index] = '-';
                } else {
                    index2 = index;
                }
                if (scientificNotation) {
                    int i3 = 0;
                    while (i3 < olength - 1) {
                        output /= 10;
                        result[(index2 + olength) - i3] = (char) (48 + ((int) (output % 10)));
                        i3++;
                        bits11 = bits11;
                    }
                    result[index2] = (char) ((int) (48 + (output % 10)));
                    result[index2 + 1] = '.';
                    int index26 = index2 + olength + 1;
                    if (olength == 1) {
                        result[index26] = '0';
                        index26++;
                    }
                    int index27 = index26 + 1;
                    result[index26] = 'E';
                    if (exp < 0) {
                        result[index27] = '-';
                        exp = -exp;
                        index27++;
                    }
                    if (exp >= 100) {
                        int index28 = index27 + 1;
                        result[index27] = (char) (48 + (exp / 100));
                        exp %= 100;
                        index27 = index28 + 1;
                        result[index28] = (char) (48 + (exp / 10));
                    } else if (exp >= 10) {
                        result[index27] = (char) (48 + (exp / 10));
                        index27++;
                    }
                    result[index27] = (char) (48 + (exp % 10));
                    return (index27 + 1) - off;
                }
                if (exp < 0) {
                    int index29 = index2 + 1;
                    result[index2] = '0';
                    index3 = index29 + 1;
                    result[index29] = '.';
                    int i4 = -1;
                    while (i4 > exp) {
                        result[index3] = '0';
                        i4--;
                        index3++;
                    }
                    int current = index3;
                    int i5 = 0;
                    while (i5 < olength) {
                        result[((current + olength) - i5) - 1] = (char) ((int) (48 + (output % 10)));
                        output /= 10;
                        index3++;
                        i5++;
                        e10 = e10;
                        current = current;
                    }
                } else if (exp + 1 >= olength) {
                    for (int i6 = 0; i6 < olength; i6++) {
                        result[((index2 + olength) - i6) - 1] = (char) ((int) (48 + (output % 10)));
                        output /= 10;
                    }
                    int index30 = index2 + olength;
                    int i7 = olength;
                    while (i7 < exp + 1) {
                        result[index30] = '0';
                        i7++;
                        index30++;
                    }
                    int index31 = index30 + 1;
                    result[index30] = '.';
                    index3 = index31 + 1;
                    result[index31] = '0';
                } else {
                    int current2 = index2 + 1;
                    int i8 = 0;
                    while (i8 < olength) {
                        if ((olength - i8) - 1 == exp) {
                            result[((current2 + olength) - i8) - 1] = '.';
                            current2--;
                        }
                        result[((current2 + olength) - i8) - 1] = (char) ((int) (48 + (output % 10)));
                        output /= 10;
                        i8++;
                        removed = removed;
                    }
                    index3 = index2 + olength + 1;
                    return index3 - off;
                }
                return index3 - off;
            }
        }
    }
}
