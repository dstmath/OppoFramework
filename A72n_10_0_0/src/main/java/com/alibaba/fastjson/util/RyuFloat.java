package com.alibaba.fastjson.util;

public final class RyuFloat {
    private static final int[][] POW5_INV_SPLIT = {new int[]{268435456, 1}, new int[]{214748364, 1717986919}, new int[]{171798691, 1803886265}, new int[]{137438953, 1013612282}, new int[]{219902325, 1192282922}, new int[]{175921860, 953826338}, new int[]{140737488, 763061070}, new int[]{225179981, 791400982}, new int[]{180143985, 203624056}, new int[]{144115188, 162899245}, new int[]{230584300, 1978625710}, new int[]{184467440, 1582900568}, new int[]{147573952, 1266320455}, new int[]{236118324, 308125809}, new int[]{188894659, 675997377}, new int[]{151115727, 970294631}, new int[]{241785163, 1981968139}, new int[]{193428131, 297084323}, new int[]{154742504, 1955654377}, new int[]{247588007, 1840556814}, new int[]{198070406, 613451992}, new int[]{158456325, 61264864}, new int[]{253530120, 98023782}, new int[]{202824096, 78419026}, new int[]{162259276, 1780722139}, new int[]{259614842, 1990161963}, new int[]{207691874, 733136111}, new int[]{166153499, 1016005619}, new int[]{265845599, 337118801}, new int[]{212676479, 699191770}, new int[]{170141183, 988850146}};
    private static final int[][] POW5_SPLIT = {new int[]{536870912, 0}, new int[]{671088640, 0}, new int[]{838860800, 0}, new int[]{1048576000, 0}, new int[]{655360000, 0}, new int[]{819200000, 0}, new int[]{1024000000, 0}, new int[]{640000000, 0}, new int[]{800000000, 0}, new int[]{1000000000, 0}, new int[]{625000000, 0}, new int[]{781250000, 0}, new int[]{976562500, 0}, new int[]{610351562, 1073741824}, new int[]{762939453, 268435456}, new int[]{953674316, 872415232}, new int[]{596046447, 1619001344}, new int[]{745058059, 1486880768}, new int[]{931322574, 1321730048}, new int[]{582076609, 289210368}, new int[]{727595761, 898383872}, new int[]{909494701, 1659850752}, new int[]{568434188, 1305842176}, new int[]{710542735, 1632302720}, new int[]{888178419, 1503507488}, new int[]{555111512, 671256724}, new int[]{693889390, 839070905}, new int[]{867361737, 2122580455}, new int[]{542101086, 521306416}, new int[]{677626357, 1725374844}, new int[]{847032947, 546105819}, new int[]{1058791184, 145761362}, new int[]{661744490, 91100851}, new int[]{827180612, 1187617888}, new int[]{1033975765, 1484522360}, new int[]{646234853, 1196261931}, new int[]{807793566, 2032198326}, new int[]{1009741958, 1466506084}, new int[]{631088724, 379695390}, new int[]{788860905, 474619238}, new int[]{986076131, 1130144959}, new int[]{616297582, 437905143}, new int[]{770371977, 1621123253}, new int[]{962964972, 415791331}, new int[]{601853107, 1333611405}, new int[]{752316384, 1130143345}, new int[]{940395480, 1412679181}};

    public static String toString(float value) {
        char[] result = new char[15];
        return new String(result, 0, toString(value, result, 0));
    }

    /* JADX INFO: Multiple debug info for r3v1 int: [D('mp' int), D('FLOAT_MANTISSA_MASK' int)] */
    /* JADX INFO: Multiple debug info for r0v4 int: [D('dv' int), D('olength' int)] */
    /* JADX INFO: Multiple debug info for r2v6 int: [D('current' int), D('index' int)] */
    /* JADX INFO: Multiple debug info for r2v16 int: [D('i' int), D('index' int)] */
    /* JADX INFO: Multiple debug info for r1v14 int: [D('index' int), D('output' int)] */
    /* JADX INFO: Multiple debug info for r13v31 long: [D('bits' int), D('pis1' long)] */
    public static int toString(float value, char[] result, int off) {
        int m2;
        int e2;
        int dv;
        int index;
        boolean dvIsTrailingZeros;
        boolean dpIsTrailingZeros;
        int e10;
        int q;
        int mp;
        int dm;
        int lastRemovedDigit;
        int lastRemovedDigit2;
        int dv2;
        int index2;
        int index3;
        int index4;
        int output;
        int i;
        int dm2;
        int dp;
        int i2;
        int dp2;
        int ii;
        if (Float.isNaN(value)) {
            int index5 = off + 1;
            result[off] = 'N';
            int index6 = index5 + 1;
            result[index5] = 'a';
            result[index6] = 'N';
            return (index6 + 1) - off;
        } else if (value == Float.POSITIVE_INFINITY) {
            int index7 = off + 1;
            result[off] = 'I';
            int index8 = index7 + 1;
            result[index7] = 'n';
            int index9 = index8 + 1;
            result[index8] = 'f';
            int index10 = index9 + 1;
            result[index9] = 'i';
            int index11 = index10 + 1;
            result[index10] = 'n';
            int index12 = index11 + 1;
            result[index11] = 'i';
            int index13 = index12 + 1;
            result[index12] = 't';
            result[index13] = 'y';
            return (index13 + 1) - off;
        } else if (value == Float.NEGATIVE_INFINITY) {
            int index14 = off + 1;
            result[off] = '-';
            int index15 = index14 + 1;
            result[index14] = 'I';
            int index16 = index15 + 1;
            result[index15] = 'n';
            int index17 = index16 + 1;
            result[index16] = 'f';
            int index18 = index17 + 1;
            result[index17] = 'i';
            int index19 = index18 + 1;
            result[index18] = 'n';
            int index20 = index19 + 1;
            result[index19] = 'i';
            int index21 = index20 + 1;
            result[index20] = 't';
            result[index21] = 'y';
            return (index21 + 1) - off;
        } else {
            int bits = Float.floatToIntBits(value);
            if (bits == 0) {
                int index22 = off + 1;
                result[off] = '0';
                int index23 = index22 + 1;
                result[index22] = '.';
                result[index23] = '0';
                return (index23 + 1) - off;
            } else if (bits == Integer.MIN_VALUE) {
                int index24 = off + 1;
                result[off] = '-';
                int index25 = index24 + 1;
                result[index24] = '0';
                int index26 = index25 + 1;
                result[index25] = '.';
                result[index26] = '0';
                return (index26 + 1) - off;
            } else {
                int ieeeExponent = (bits >> 23) & 255;
                int ieeeMantissa = bits & 8388607;
                if (ieeeExponent == 0) {
                    e2 = -149;
                    m2 = ieeeMantissa;
                } else {
                    e2 = (ieeeExponent - 127) - 23;
                    m2 = ieeeMantissa | 8388608;
                }
                int i3 = 0;
                boolean sign = bits < 0;
                boolean even = (m2 & 1) == 0;
                int mv = 4 * m2;
                int i4 = 2;
                int FLOAT_MANTISSA_MASK = (4 * m2) + 2;
                int i5 = 4 * m2;
                if (((long) m2) == 8388608 && ieeeExponent > 1) {
                    i4 = 1;
                }
                int i6 = i5 - i4;
                int e22 = e2 - 2;
                if (e22 >= 0) {
                    int q2 = (int) ((((long) e22) * 3010299) / 10000000);
                    int i7 = (-e22) + q2 + (((q2 == 0 ? 1 : (int) ((((((long) q2) * 23219280) + 10000000) - 1) / 10000000)) + 59) - 1);
                    long pis0 = (long) POW5_INV_SPLIT[q2][0];
                    long pis1 = (long) POW5_INV_SPLIT[q2][1];
                    index = off;
                    int dv3 = (int) (((((long) mv) * pis0) + ((((long) mv) * pis1) >> 31)) >> (i7 - 31));
                    int dp3 = (int) (((((long) FLOAT_MANTISSA_MASK) * pis0) + ((((long) FLOAT_MANTISSA_MASK) * pis1) >> 31)) >> (i7 - 31));
                    dm = (int) (((((long) i6) * pis0) + ((((long) i6) * pis1) >> 31)) >> (i7 - 31));
                    if (q2 == 0 || (dp3 - 1) / 10 > dm / 10) {
                        dp2 = dp3;
                        ii = 0;
                    } else {
                        int e = q2 - 1;
                        int qx = q2 - 1;
                        dp2 = dp3;
                        ii = (int) ((((((long) mv) * ((long) POW5_INV_SPLIT[qx][0])) + ((((long) mv) * ((long) POW5_INV_SPLIT[qx][1])) >> 31)) >> (((((-e22) + q2) - 1) + ((59 + (e == 0 ? 1 : (int) ((((((long) e) * 23219280) + 10000000) - 1) / 10000000))) - 1)) - 31)) % 10);
                    }
                    e10 = q2;
                    int pow5Factor_mp = 0;
                    int v = FLOAT_MANTISSA_MASK;
                    while (v > 0 && v % 5 == 0) {
                        v /= 5;
                        pow5Factor_mp++;
                    }
                    int pow5Factor_mv = 0;
                    int v2 = mv;
                    while (v2 > 0 && v2 % 5 == 0) {
                        v2 /= 5;
                        pow5Factor_mv++;
                    }
                    int pow5Factor_mm = 0;
                    int v3 = i6;
                    while (v3 > 0 && v3 % 5 == 0) {
                        v3 /= 5;
                        pow5Factor_mm++;
                    }
                    dpIsTrailingZeros = pow5Factor_mp >= q2;
                    dvIsTrailingZeros = pow5Factor_mv >= q2;
                    q = pow5Factor_mm >= q2 ? 1 : 0;
                    dv = dv3;
                    mp = ii;
                    lastRemovedDigit = dp2;
                } else {
                    index = off;
                    int q3 = (int) ((((long) (-e22)) * 6989700) / 10000000);
                    int i8 = (-e22) - q3;
                    int i9 = i8 == 0 ? 1 : (int) ((((((long) i8) * 23219280) + 10000000) - 1) / 10000000);
                    long ps0 = (long) POW5_SPLIT[i8][0];
                    long ps1 = (long) POW5_SPLIT[i8][1];
                    int j31 = (q3 - (i9 - 61)) - 31;
                    dv = (int) (((((long) mv) * ps0) + ((((long) mv) * ps1) >> 31)) >> j31);
                    int dp4 = (int) (((((long) FLOAT_MANTISSA_MASK) * ps0) + ((((long) FLOAT_MANTISSA_MASK) * ps1) >> 31)) >> j31);
                    int dm3 = (int) (((((long) i6) * ps0) + ((((long) i6) * ps1) >> 31)) >> j31);
                    if (q3 == 0 || (dp4 - 1) / 10 > dm3 / 10) {
                        dp = dp4;
                        dm2 = dm3;
                        i2 = 0;
                    } else {
                        int e3 = i8 + 1;
                        int ix = i8 + 1;
                        dp = dp4;
                        dm2 = dm3;
                        i2 = (int) ((((((long) mv) * ((long) POW5_SPLIT[ix][0])) + ((((long) mv) * ((long) POW5_SPLIT[ix][1])) >> 31)) >> (((q3 - 1) - ((e3 == 0 ? 1 : (int) ((((((long) e3) * 23219280) + 10000000) - 1) / 10000000)) - 61)) - 31)) % 10);
                    }
                    e10 = q3 + e22;
                    dpIsTrailingZeros = 1 >= q3;
                    boolean dvIsTrailingZeros2 = q3 < 23 && (((1 << (q3 + -1)) - 1) & mv) == 0;
                    q = (i6 % 2 == 1 ? 0 : 1) >= q3 ? 1 : 0;
                    dvIsTrailingZeros = dvIsTrailingZeros2;
                    lastRemovedDigit = dp;
                    mp = i2;
                    dm = dm2;
                }
                int dplength = 10;
                int factor = 1000000000;
                while (dplength > 0 && lastRemovedDigit < factor) {
                    factor /= 10;
                    dplength--;
                }
                int exp = (e10 + dplength) - 1;
                boolean scientificNotation = exp < -3 || exp >= 7;
                int removed = 0;
                if (dpIsTrailingZeros && !even) {
                    lastRemovedDigit--;
                }
                while (lastRemovedDigit / 10 > dm / 10 && (lastRemovedDigit >= 100 || !scientificNotation)) {
                    q &= dm % 10 == 0 ? 1 : 0;
                    lastRemovedDigit /= 10;
                    mp = dv % 10;
                    dv /= 10;
                    dm /= 10;
                    removed++;
                    mv = mv;
                }
                if (q == 0 || !even) {
                    dv2 = dv;
                    lastRemovedDigit2 = mp;
                } else {
                    lastRemovedDigit2 = mp;
                    while (dm % 10 == 0 && (lastRemovedDigit >= 100 || !scientificNotation)) {
                        lastRemovedDigit /= 10;
                        lastRemovedDigit2 = dv % 10;
                        dv /= 10;
                        dm /= 10;
                        removed++;
                    }
                    dv2 = dv;
                }
                if (dvIsTrailingZeros && lastRemovedDigit2 == 5 && dv2 % 2 == 0) {
                    lastRemovedDigit2 = 4;
                }
                int i10 = (((dv2 != dm || (q != 0 && even)) && lastRemovedDigit2 < 5) ? 0 : 1) + dv2;
                int olength = dplength - removed;
                if (sign) {
                    result[index] = '-';
                    index++;
                }
                if (scientificNotation) {
                    while (i3 < olength - 1) {
                        result[(index + olength) - i3] = (char) (48 + (i10 % 10));
                        i3++;
                        dm = dm;
                        lastRemovedDigit2 = lastRemovedDigit2;
                        i10 /= 10;
                    }
                    result[index] = (char) (48 + (i10 % 10));
                    result[index + 1] = '.';
                    int index27 = index + olength + 1;
                    if (olength == 1) {
                        index3 = index27 + 1;
                        result[index27] = '0';
                    } else {
                        index3 = index27;
                    }
                    int index28 = index3 + 1;
                    result[index3] = 'E';
                    if (exp < 0) {
                        index4 = index28 + 1;
                        result[index28] = '-';
                        exp = -exp;
                    } else {
                        index4 = index28;
                    }
                    if (exp >= 10) {
                        output = i10;
                        i = 48;
                        result[index4] = (char) (48 + (exp / 10));
                        index4++;
                    } else {
                        output = i10;
                        i = 48;
                    }
                    result[index4] = (char) (i + (exp % 10));
                    index2 = index4 + 1;
                } else if (exp < 0) {
                    int index29 = index + 1;
                    result[index] = '0';
                    index2 = index29 + 1;
                    result[index29] = '.';
                    int i11 = -1;
                    while (i11 > exp) {
                        result[index2] = '0';
                        i11--;
                        index2++;
                    }
                    int current = index2;
                    while (i3 < olength) {
                        result[((current + olength) - i3) - 1] = (char) (48 + (i10 % 10));
                        i10 /= 10;
                        index2++;
                        i3++;
                        i6 = i6;
                        current = current;
                    }
                } else if (exp + 1 >= olength) {
                    while (i3 < olength) {
                        result[((index + olength) - i3) - 1] = (char) (48 + (i10 % 10));
                        i10 /= 10;
                        i3++;
                    }
                    int index30 = index + olength;
                    int i12 = olength;
                    while (i12 < exp + 1) {
                        result[index30] = '0';
                        i12++;
                        index30++;
                    }
                    int index31 = index30 + 1;
                    result[index30] = '.';
                    index2 = index31 + 1;
                    result[index31] = '0';
                } else {
                    int current2 = index + 1;
                    while (i3 < olength) {
                        if ((olength - i3) - 1 == exp) {
                            result[((current2 + olength) - i3) - 1] = '.';
                            current2--;
                        }
                        result[((current2 + olength) - i3) - 1] = (char) (48 + (i10 % 10));
                        i10 /= 10;
                        i3++;
                        current2 = current2;
                    }
                    index2 = index + olength + 1;
                }
                return index2 - off;
            }
        }
    }
}
