package java.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

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
public class BigInteger extends Number implements Comparable<BigInteger>, Serializable {
    static final BigInteger MINUS_ONE = null;
    public static final BigInteger ONE = null;
    static final BigInteger[] SMALL_VALUES = null;
    public static final BigInteger TEN = null;
    public static final BigInteger ZERO = null;
    private static final long serialVersionUID = -8287574255936472291L;
    private transient BigInt bigInt;
    transient int[] digits;
    private transient int firstNonzeroDigit;
    private transient int hashCode;
    private transient boolean javaIsValid;
    private byte[] magnitude;
    private transient boolean nativeIsValid;
    transient int numberLength;
    transient int sign;
    private int signum;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.math.BigInteger.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.math.BigInteger.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.math.BigInteger.<clinit>():void");
    }

    BigInteger(BigInt bigInt) {
        this.nativeIsValid = false;
        this.javaIsValid = false;
        this.firstNonzeroDigit = -2;
        this.hashCode = 0;
        if (bigInt == null || bigInt.getNativeBIGNUM() == 0) {
            throw new AssertionError();
        }
        setBigInt(bigInt);
    }

    BigInteger(int sign, long value) {
        boolean z = false;
        this.nativeIsValid = false;
        this.javaIsValid = false;
        this.firstNonzeroDigit = -2;
        this.hashCode = 0;
        BigInt bigInt = new BigInt();
        if (sign < 0) {
            z = true;
        }
        bigInt.putULongInt(value, z);
        setBigInt(bigInt);
    }

    BigInteger(int sign, int numberLength, int[] digits) {
        this.nativeIsValid = false;
        this.javaIsValid = false;
        this.firstNonzeroDigit = -2;
        this.hashCode = 0;
        setJavaRepresentation(sign, numberLength, digits);
    }

    public BigInteger(int numBits, Random random) {
        this.nativeIsValid = false;
        this.javaIsValid = false;
        this.firstNonzeroDigit = -2;
        this.hashCode = 0;
        if (numBits < 0) {
            throw new IllegalArgumentException("numBits < 0: " + numBits);
        }
        if (numBits == 0) {
            int[] iArr = new int[1];
            iArr[0] = 0;
            setJavaRepresentation(0, 1, iArr);
        } else {
            int numberLength = (numBits + 31) >> 5;
            int[] digits = new int[numberLength];
            for (int i = 0; i < numberLength; i++) {
                digits[i] = random.nextInt();
            }
            int i2 = numberLength - 1;
            digits[i2] = digits[i2] >>> ((-numBits) & 31);
            setJavaRepresentation(1, numberLength, digits);
        }
        this.javaIsValid = true;
    }

    public BigInteger(int bitLength, int certainty, Random random) {
        this.nativeIsValid = false;
        this.javaIsValid = false;
        this.firstNonzeroDigit = -2;
        this.hashCode = 0;
        if (bitLength < 2) {
            throw new ArithmeticException("bitLength < 2: " + bitLength);
        } else if (bitLength < 16) {
            int candidate;
            do {
                candidate = (random.nextInt() & ((1 << bitLength) - 1)) | (1 << (bitLength - 1));
                if (bitLength > 2) {
                    candidate |= 1;
                }
            } while (!isSmallPrime(candidate));
            BigInt prime = new BigInt();
            prime.putULongInt((long) candidate, false);
            setBigInt(prime);
        } else {
            while (true) {
                setBigInt(BigInt.generatePrimeDefault(bitLength));
                if (bitLength() == bitLength) {
                    return;
                }
            }
        }
    }

    private static boolean isSmallPrime(int x) {
        if (x == 2) {
            return true;
        }
        if (x % 2 == 0) {
            return false;
        }
        int max = (int) Math.sqrt((double) x);
        for (int i = 3; i <= max; i += 2) {
            if (x % i == 0) {
                return false;
            }
        }
        return true;
    }

    public BigInteger(String value) {
        this.nativeIsValid = false;
        this.javaIsValid = false;
        this.firstNonzeroDigit = -2;
        this.hashCode = 0;
        BigInt bigInt = new BigInt();
        bigInt.putDecString(value);
        setBigInt(bigInt);
    }

    public BigInteger(String value, int radix) {
        this.nativeIsValid = false;
        this.javaIsValid = false;
        this.firstNonzeroDigit = -2;
        this.hashCode = 0;
        BigInt bigInt;
        if (value == null) {
            throw new NullPointerException("value == null");
        } else if (radix == 10) {
            bigInt = new BigInt();
            bigInt.putDecString(value);
            setBigInt(bigInt);
        } else if (radix == 16) {
            bigInt = new BigInt();
            bigInt.putHexString(value);
            setBigInt(bigInt);
        } else if (radix < 2 || radix > 36) {
            throw new NumberFormatException("Invalid radix: " + radix);
        } else if (value.isEmpty()) {
            throw new NumberFormatException("value.isEmpty()");
        } else {
            parseFromString(this, value, radix);
        }
    }

    public BigInteger(int signum, byte[] magnitude) {
        boolean z = true;
        this.nativeIsValid = false;
        this.javaIsValid = false;
        this.firstNonzeroDigit = -2;
        this.hashCode = 0;
        if (magnitude == null) {
            throw new NullPointerException("magnitude == null");
        } else if (signum < -1 || signum > 1) {
            throw new NumberFormatException("Invalid signum: " + signum);
        } else {
            if (signum == 0) {
                for (byte element : magnitude) {
                    if (element != (byte) 0) {
                        throw new NumberFormatException("signum-magnitude mismatch");
                    }
                }
            }
            BigInt bigInt = new BigInt();
            if (signum >= 0) {
                z = false;
            }
            bigInt.putBigEndian(magnitude, z);
            setBigInt(bigInt);
        }
    }

    public BigInteger(byte[] value) {
        this.nativeIsValid = false;
        this.javaIsValid = false;
        this.firstNonzeroDigit = -2;
        this.hashCode = 0;
        if (value.length == 0) {
            throw new NumberFormatException("value.length == 0");
        }
        BigInt bigInt = new BigInt();
        bigInt.putBigEndianTwosComplement(value);
        setBigInt(bigInt);
    }

    BigInt getBigInt() {
        boolean z = false;
        if (this.nativeIsValid) {
            return this.bigInt;
        }
        synchronized (this) {
            if (this.nativeIsValid) {
                BigInt bigInt = this.bigInt;
                return bigInt;
            }
            BigInt bigInt2 = new BigInt();
            int[] iArr = this.digits;
            if (this.sign < 0) {
                z = true;
            }
            bigInt2.putLittleEndianInts(iArr, z);
            setBigInt(bigInt2);
            return bigInt2;
        }
    }

    private void setBigInt(BigInt bigInt) {
        this.bigInt = bigInt;
        this.nativeIsValid = true;
    }

    private void setJavaRepresentation(int sign, int numberLength, int[] digits) {
        while (numberLength > 0) {
            numberLength--;
            if (digits[numberLength] != 0) {
                break;
            }
        }
        int numberLength2 = numberLength + 1;
        if (digits[numberLength] == 0) {
            sign = 0;
        }
        this.sign = sign;
        this.digits = digits;
        this.numberLength = numberLength2;
        this.javaIsValid = true;
    }

    void prepareJavaRepresentation() {
        if (!this.javaIsValid) {
            synchronized (this) {
                if (this.javaIsValid) {
                    return;
                }
                int[] digits;
                int sign = this.bigInt.sign();
                if (sign != 0) {
                    digits = this.bigInt.littleEndianIntsMagnitude();
                } else {
                    digits = new int[1];
                    digits[0] = 0;
                }
                setJavaRepresentation(sign, digits.length, digits);
            }
        }
    }

    public static BigInteger valueOf(long value) {
        if (value < 0) {
            if (value != -1) {
                return new BigInteger(-1, -value);
            }
            return MINUS_ONE;
        } else if (value < ((long) SMALL_VALUES.length)) {
            return SMALL_VALUES[(int) value];
        } else {
            return new BigInteger(1, value);
        }
    }

    public byte[] toByteArray() {
        return twosComplement();
    }

    public BigInteger abs() {
        BigInt bigInt = getBigInt();
        if (bigInt.sign() >= 0) {
            return this;
        }
        BigInt a = bigInt.copy();
        a.setSign(1);
        return new BigInteger(a);
    }

    public BigInteger negate() {
        BigInt bigInt = getBigInt();
        int sign = bigInt.sign();
        if (sign == 0) {
            return this;
        }
        BigInt a = bigInt.copy();
        a.setSign(-sign);
        return new BigInteger(a);
    }

    public BigInteger add(BigInteger value) {
        BigInt lhs = getBigInt();
        BigInt rhs = value.getBigInt();
        if (rhs.sign() == 0) {
            return this;
        }
        if (lhs.sign() == 0) {
            return value;
        }
        return new BigInteger(BigInt.addition(lhs, rhs));
    }

    public BigInteger subtract(BigInteger value) {
        BigInt lhs = getBigInt();
        BigInt rhs = value.getBigInt();
        if (rhs.sign() == 0) {
            return this;
        }
        return new BigInteger(BigInt.subtraction(lhs, rhs));
    }

    public int signum() {
        if (this.javaIsValid) {
            return this.sign;
        }
        return getBigInt().sign();
    }

    public BigInteger shiftRight(int n) {
        return shiftLeft(-n);
    }

    public BigInteger shiftLeft(int n) {
        if (n == 0) {
            return this;
        }
        int sign = signum();
        if (sign == 0) {
            return this;
        }
        if (sign > 0 || n >= 0) {
            return new BigInteger(BigInt.shift(getBigInt(), n));
        }
        return BitLevel.shiftRight(this, -n);
    }

    BigInteger shiftLeftOneBit() {
        return signum() == 0 ? this : BitLevel.shiftLeftOneBit(this);
    }

    public int bitLength() {
        if (this.nativeIsValid || !this.javaIsValid) {
            return getBigInt().bitLength();
        }
        return BitLevel.bitLength(this);
    }

    public boolean testBit(int n) {
        boolean z = true;
        if (n < 0) {
            throw new ArithmeticException("n < 0: " + n);
        }
        int sign = signum();
        if (sign > 0 && this.nativeIsValid && !this.javaIsValid) {
            return getBigInt().isBitSet(n);
        }
        prepareJavaRepresentation();
        if (n == 0) {
            if ((this.digits[0] & 1) == 0) {
                z = false;
            }
            return z;
        }
        int intCount = n >> 5;
        if (intCount >= this.numberLength) {
            if (sign >= 0) {
                z = false;
            }
            return z;
        }
        int digit = this.digits[intCount];
        n = 1 << (n & 31);
        if (sign < 0) {
            int firstNonZeroDigit = getFirstNonzeroDigit();
            if (intCount < firstNonZeroDigit) {
                return false;
            }
            if (firstNonZeroDigit == intCount) {
                digit = -digit;
            } else {
                digit = ~digit;
            }
        }
        if ((digit & n) == 0) {
            z = false;
        }
        return z;
    }

    public BigInteger setBit(int n) {
        prepareJavaRepresentation();
        if (testBit(n)) {
            return this;
        }
        return BitLevel.flipBit(this, n);
    }

    public BigInteger clearBit(int n) {
        prepareJavaRepresentation();
        if (testBit(n)) {
            return BitLevel.flipBit(this, n);
        }
        return this;
    }

    public BigInteger flipBit(int n) {
        prepareJavaRepresentation();
        if (n >= 0) {
            return BitLevel.flipBit(this, n);
        }
        throw new ArithmeticException("n < 0: " + n);
    }

    public int getLowestSetBit() {
        prepareJavaRepresentation();
        if (this.sign == 0) {
            return -1;
        }
        int i = getFirstNonzeroDigit();
        return (i << 5) + Integer.numberOfTrailingZeros(this.digits[i]);
    }

    public int bitCount() {
        prepareJavaRepresentation();
        return BitLevel.bitCount(this);
    }

    public BigInteger not() {
        prepareJavaRepresentation();
        return Logical.not(this);
    }

    public BigInteger and(BigInteger value) {
        prepareJavaRepresentation();
        value.prepareJavaRepresentation();
        return Logical.and(this, value);
    }

    public BigInteger or(BigInteger value) {
        prepareJavaRepresentation();
        value.prepareJavaRepresentation();
        return Logical.or(this, value);
    }

    public BigInteger xor(BigInteger value) {
        prepareJavaRepresentation();
        value.prepareJavaRepresentation();
        return Logical.xor(this, value);
    }

    public BigInteger andNot(BigInteger value) {
        prepareJavaRepresentation();
        value.prepareJavaRepresentation();
        return Logical.andNot(this, value);
    }

    public int intValue() {
        if (this.nativeIsValid && this.bigInt.twosCompFitsIntoBytes(4)) {
            return (int) this.bigInt.longInt();
        }
        prepareJavaRepresentation();
        return this.sign * this.digits[0];
    }

    public long longValue() {
        if (this.nativeIsValid && this.bigInt.twosCompFitsIntoBytes(8)) {
            return this.bigInt.longInt();
        }
        long value;
        prepareJavaRepresentation();
        if (this.numberLength > 1) {
            value = (((long) this.digits[1]) << 32) | (((long) this.digits[0]) & 4294967295L);
        } else {
            value = ((long) this.digits[0]) & 4294967295L;
        }
        return ((long) this.sign) * value;
    }

    public float floatValue() {
        return (float) doubleValue();
    }

    public double doubleValue() {
        return Conversion.bigInteger2Double(this);
    }

    public int compareTo(BigInteger value) {
        return BigInt.cmp(getBigInt(), value.getBigInt());
    }

    public BigInteger min(BigInteger value) {
        return compareTo(value) == -1 ? this : value;
    }

    public BigInteger max(BigInteger value) {
        return compareTo(value) == 1 ? this : value;
    }

    public int hashCode() {
        if (this.hashCode == 0) {
            prepareJavaRepresentation();
            int hash = 0;
            for (int i = 0; i < this.numberLength; i++) {
                hash = (hash * 33) + this.digits[i];
            }
            this.hashCode = this.sign * hash;
        }
        return this.hashCode;
    }

    public boolean equals(Object x) {
        boolean z = true;
        if (this == x) {
            return true;
        }
        if (!(x instanceof BigInteger)) {
            return false;
        }
        if (compareTo((BigInteger) x) != 0) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return getBigInt().decString();
    }

    public String toString(int radix) {
        if (radix == 10) {
            return getBigInt().decString();
        }
        prepareJavaRepresentation();
        return Conversion.bigInteger2String(this, radix);
    }

    public BigInteger gcd(BigInteger value) {
        return new BigInteger(BigInt.gcd(getBigInt(), value.getBigInt()));
    }

    public BigInteger multiply(BigInteger value) {
        return new BigInteger(BigInt.product(getBigInt(), value.getBigInt()));
    }

    public BigInteger pow(int exp) {
        if (exp >= 0) {
            return new BigInteger(BigInt.exp(getBigInt(), exp));
        }
        throw new ArithmeticException("exp < 0: " + exp);
    }

    public BigInteger[] divideAndRemainder(BigInteger divisor) {
        BigInt divisorBigInt = divisor.getBigInt();
        BigInt quotient = new BigInt();
        BigInt remainder = new BigInt();
        BigInt.division(getBigInt(), divisorBigInt, quotient, remainder);
        BigInteger[] bigIntegerArr = new BigInteger[2];
        bigIntegerArr[0] = new BigInteger(quotient);
        bigIntegerArr[1] = new BigInteger(remainder);
        return bigIntegerArr;
    }

    public BigInteger divide(BigInteger divisor) {
        BigInt quotient = new BigInt();
        BigInt.division(getBigInt(), divisor.getBigInt(), quotient, null);
        return new BigInteger(quotient);
    }

    public BigInteger remainder(BigInteger divisor) {
        BigInt remainder = new BigInt();
        BigInt.division(getBigInt(), divisor.getBigInt(), null, remainder);
        return new BigInteger(remainder);
    }

    public BigInteger modInverse(BigInteger m) {
        if (m.signum() > 0) {
            return new BigInteger(BigInt.modInverse(getBigInt(), m.getBigInt()));
        }
        throw new ArithmeticException("modulus not positive");
    }

    public BigInteger modPow(BigInteger exponent, BigInteger modulus) {
        if (modulus.signum() <= 0) {
            throw new ArithmeticException("modulus.signum() <= 0");
        }
        int exponentSignum = exponent.signum();
        if (exponentSignum == 0) {
            return ONE.mod(modulus);
        }
        return new BigInteger(BigInt.modExp((exponentSignum < 0 ? modInverse(modulus) : this).getBigInt(), exponent.getBigInt(), modulus.getBigInt()));
    }

    public BigInteger mod(BigInteger m) {
        if (m.signum() > 0) {
            return new BigInteger(BigInt.modulus(getBigInt(), m.getBigInt()));
        }
        throw new ArithmeticException("m.signum() <= 0");
    }

    public boolean isProbablePrime(int certainty) {
        if (certainty <= 0) {
            return true;
        }
        return getBigInt().isPrime(certainty);
    }

    public BigInteger nextProbablePrime() {
        if (this.sign >= 0) {
            return Primality.nextProbablePrime(this);
        }
        throw new ArithmeticException("sign < 0");
    }

    public static BigInteger probablePrime(int bitLength, Random random) {
        return new BigInteger(bitLength, 100, random);
    }

    private byte[] twosComplement() {
        prepareJavaRepresentation();
        if (this.sign == 0) {
            byte[] bArr = new byte[1];
            bArr[0] = (byte) 0;
            return bArr;
        }
        int highBytes;
        int digit;
        int i;
        int bitLen = bitLength();
        int iThis = getFirstNonzeroDigit();
        int bytesLen = (bitLen >> 3) + 1;
        byte[] bytes = new byte[bytesLen];
        int firstByteNumber = 0;
        int bytesInInteger = 4;
        if (bytesLen - (this.numberLength << 2) == 1) {
            int i2;
            if (this.sign < 0) {
                i2 = -1;
            } else {
                i2 = 0;
            }
            bytes[0] = (byte) i2;
            highBytes = 4;
            firstByteNumber = 1;
        } else {
            int hB = bytesLen & 3;
            highBytes = hB == 0 ? 4 : hB;
        }
        int digitIndex = iThis;
        bytesLen -= iThis << 2;
        if (this.sign < 0) {
            digit = -this.digits[iThis];
            digitIndex = iThis + 1;
            if (digitIndex == this.numberLength) {
                bytesInInteger = highBytes;
            }
            i = 0;
            while (i < bytesInInteger) {
                bytesLen--;
                bytes[bytesLen] = (byte) digit;
                i++;
                digit >>= 8;
            }
            while (bytesLen > firstByteNumber) {
                digit = ~this.digits[digitIndex];
                digitIndex++;
                if (digitIndex == this.numberLength) {
                    bytesInInteger = highBytes;
                }
                i = 0;
                while (i < bytesInInteger) {
                    bytesLen--;
                    bytes[bytesLen] = (byte) digit;
                    i++;
                    digit >>= 8;
                }
            }
            return bytes;
        }
        while (bytesLen > firstByteNumber) {
            digit = this.digits[digitIndex];
            digitIndex++;
            if (digitIndex == this.numberLength) {
                bytesInInteger = highBytes;
            }
            i = 0;
            while (i < bytesInInteger) {
                bytesLen--;
                bytes[bytesLen] = (byte) digit;
                i++;
                digit >>= 8;
            }
        }
        return bytes;
    }

    static int multiplyByInt(int[] res, int[] a, int aSize, int factor) {
        long carry = 0;
        for (int i = 0; i < aSize; i++) {
            carry += (((long) a[i]) & 4294967295L) * (((long) factor) & 4294967295L);
            res[i] = (int) carry;
            carry >>>= 32;
        }
        return (int) carry;
    }

    static int inplaceAdd(int[] a, int aSize, int addend) {
        long carry = ((long) addend) & 4294967295L;
        int i = 0;
        while (carry != 0 && i < aSize) {
            carry += ((long) a[i]) & 4294967295L;
            a[i] = (int) carry;
            carry >>= 32;
            i++;
        }
        return (int) carry;
    }

    private static void parseFromString(BigInteger bi, String value, int radix) {
        int sign;
        int startChar;
        int stringLength = value.length();
        int endChar = stringLength;
        if (value.charAt(0) == '-') {
            sign = -1;
            startChar = 1;
            stringLength--;
        } else {
            sign = 1;
            startChar = 0;
        }
        int charsPerInt = Conversion.digitFitInInt[radix];
        int bigRadixDigitsLength = stringLength / charsPerInt;
        int topChars = stringLength % charsPerInt;
        if (topChars != 0) {
            bigRadixDigitsLength++;
        }
        int[] digits = new int[bigRadixDigitsLength];
        int bigRadix = Conversion.bigRadices[radix - 2];
        int digitIndex = 0;
        if (topChars == 0) {
            topChars = charsPerInt;
        }
        int substrEnd = startChar + topChars;
        int substrStart = startChar;
        while (true) {
            int digitIndex2 = digitIndex;
            if (substrStart < endChar) {
                digitIndex = digitIndex2 + 1;
                digits[digitIndex2] = multiplyByInt(digits, digits, digitIndex2, bigRadix) + inplaceAdd(digits, digitIndex2, Integer.parseInt(value.substring(substrStart, substrEnd), radix));
                substrStart = substrEnd;
                substrEnd += charsPerInt;
            } else {
                int numberLength = digitIndex2;
                bi.setJavaRepresentation(sign, digitIndex2, digits);
                return;
            }
        }
    }

    int getFirstNonzeroDigit() {
        if (this.firstNonzeroDigit == -2) {
            int i;
            if (this.sign == 0) {
                i = -1;
            } else {
                i = 0;
                while (this.digits[i] == 0) {
                    i++;
                }
            }
            this.firstNonzeroDigit = i;
        }
        return this.firstNonzeroDigit;
    }

    BigInteger copy() {
        prepareJavaRepresentation();
        int[] copyDigits = new int[this.numberLength];
        System.arraycopy(this.digits, 0, copyDigits, 0, this.numberLength);
        return new BigInteger(this.sign, this.numberLength, copyDigits);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        boolean z = false;
        in.defaultReadObject();
        BigInt bigInt = new BigInt();
        byte[] bArr = this.magnitude;
        if (this.signum < 0) {
            z = true;
        }
        bigInt.putBigEndian(bArr, z);
        setBigInt(bigInt);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        BigInt bigInt = getBigInt();
        this.signum = bigInt.sign();
        this.magnitude = bigInt.bigEndianMagnitude();
        out.defaultWriteObject();
    }
}
