package com.android.server.pm.rsa;

import com.android.server.pm.rsa.BaseCoder;
import java.math.BigInteger;

public class SauBase64 extends BaseCoder {
    private static final byte[] CHUNK_SEPARATOR = {13, 10};
    private static final byte[] DECODE_TABLE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};
    private static final byte[] STANDARD_ENCODE_TABLE = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    private static final byte[] URL_SAFE_ENCODE_TABLE = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
    private final int decodeSize;
    private final byte[] decodeTable;
    private final int encodeSize;
    private final byte[] encodeTable;
    private final byte[] lineSeparator;

    public SauBase64() {
        this(0);
    }

    public SauBase64(boolean urlSafe) {
        this(76, CHUNK_SEPARATOR, urlSafe);
    }

    public SauBase64(int lineLength) {
        this(lineLength, CHUNK_SEPARATOR);
    }

    public SauBase64(int lineLength, byte[] lineSeparator2) {
        this(lineLength, lineSeparator2, false);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SauBase64(int lineLength, byte[] lineSeparator2, boolean urlSafe) {
        super(3, 4, lineLength, lineSeparator2 == null ? 0 : lineSeparator2.length);
        this.decodeTable = DECODE_TABLE;
        if (lineSeparator2 == null) {
            this.encodeSize = 4;
            this.lineSeparator = null;
        } else if (containsAlphabetOrPad(lineSeparator2)) {
            String sep = new String(lineSeparator2);
            throw new IllegalArgumentException("lineSeparator must not contain base64 characters: [" + sep + "]");
        } else if (lineLength > 0) {
            this.encodeSize = lineSeparator2.length + 4;
            this.lineSeparator = new byte[lineSeparator2.length];
            System.arraycopy(lineSeparator2, 0, this.lineSeparator, 0, lineSeparator2.length);
        } else {
            this.encodeSize = 4;
            this.lineSeparator = null;
        }
        this.decodeSize = this.encodeSize - 1;
        this.encodeTable = urlSafe ? URL_SAFE_ENCODE_TABLE : STANDARD_ENCODE_TABLE;
    }

    public boolean isUrlSafe() {
        return this.encodeTable == URL_SAFE_ENCODE_TABLE;
    }

    /* JADX DEBUG: Additional 2 move instruction added to help type inference */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v3, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v6, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v7, resolved type: byte} */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r10v2 byte: [D('inPos' int), D('b' int)] */
    /* access modifiers changed from: package-private */
    @Override // com.android.server.pm.rsa.BaseCoder
    public void encode(byte[] in, int inPos, int inAvail, BaseCoder.BaseContent content) {
        if (!content.mEof) {
            if (inAvail < 0) {
                content.mEof = true;
                if (content.mModulus != 0 || this.lineLength != 0) {
                    byte[] buffer = ensureBufferSize(this.encodeSize, content);
                    int savedPos = content.mPos;
                    int i = content.mModulus;
                    if (i != 0) {
                        if (i == 1) {
                            int i2 = content.mPos;
                            content.mPos = i2 + 1;
                            buffer[i2] = this.encodeTable[(content.mIbitWorkArea >> 2) & 63];
                            int i3 = content.mPos;
                            content.mPos = i3 + 1;
                            buffer[i3] = this.encodeTable[(content.mIbitWorkArea << 4) & 63];
                            if (this.encodeTable == STANDARD_ENCODE_TABLE) {
                                int i4 = content.mPos;
                                content.mPos = i4 + 1;
                                buffer[i4] = this.pad;
                                int i5 = content.mPos;
                                content.mPos = i5 + 1;
                                buffer[i5] = this.pad;
                            }
                        } else if (i == 2) {
                            int i6 = content.mPos;
                            content.mPos = i6 + 1;
                            buffer[i6] = this.encodeTable[(content.mIbitWorkArea >> 10) & 63];
                            int i7 = content.mPos;
                            content.mPos = i7 + 1;
                            buffer[i7] = this.encodeTable[(content.mIbitWorkArea >> 4) & 63];
                            int i8 = content.mPos;
                            content.mPos = i8 + 1;
                            buffer[i8] = this.encodeTable[(content.mIbitWorkArea << 2) & 63];
                            if (this.encodeTable == STANDARD_ENCODE_TABLE) {
                                int i9 = content.mPos;
                                content.mPos = i9 + 1;
                                buffer[i9] = this.pad;
                            }
                        } else {
                            throw new IllegalStateException("Impossible modulus " + content.mModulus);
                        }
                    }
                    content.mCurrentLinePos += content.mPos - savedPos;
                    if (this.lineLength > 0 && content.mCurrentLinePos > 0) {
                        System.arraycopy(this.lineSeparator, 0, buffer, content.mPos, this.lineSeparator.length);
                        content.mPos += this.lineSeparator.length;
                        return;
                    }
                    return;
                }
                return;
            }
            int i10 = 0;
            while (i10 < inAvail) {
                byte[] buffer2 = ensureBufferSize(this.encodeSize, content);
                content.mModulus = (content.mModulus + 1) % 3;
                int inPos2 = inPos + 1;
                byte b = in[inPos];
                int inPos3 = b;
                if (b < 0) {
                    inPos3 = b + 256;
                }
                content.mIbitWorkArea = (content.mIbitWorkArea << 8) + inPos3;
                if (content.mModulus == 0) {
                    int i11 = content.mPos;
                    content.mPos = i11 + 1;
                    buffer2[i11] = this.encodeTable[(content.mIbitWorkArea >> 18) & 63];
                    int i12 = content.mPos;
                    content.mPos = i12 + 1;
                    buffer2[i12] = this.encodeTable[(content.mIbitWorkArea >> 12) & 63];
                    int i13 = content.mPos;
                    content.mPos = i13 + 1;
                    buffer2[i13] = this.encodeTable[(content.mIbitWorkArea >> 6) & 63];
                    int i14 = content.mPos;
                    content.mPos = i14 + 1;
                    buffer2[i14] = this.encodeTable[content.mIbitWorkArea & 63];
                    content.mCurrentLinePos += 4;
                    if (this.lineLength > 0 && this.lineLength <= content.mCurrentLinePos) {
                        System.arraycopy(this.lineSeparator, 0, buffer2, content.mPos, this.lineSeparator.length);
                        content.mPos += this.lineSeparator.length;
                        content.mCurrentLinePos = 0;
                    }
                }
                i10++;
                inPos = inPos2;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r9v2 byte: [D('inPos' int), D('b' byte)] */
    /* access modifiers changed from: package-private */
    @Override // com.android.server.pm.rsa.BaseCoder
    public void decode(byte[] in, int inPos, int inAvail, BaseCoder.BaseContent content) {
        byte b;
        if (!content.mEof) {
            if (inAvail < 0) {
                content.mEof = true;
            }
            int i = 0;
            while (true) {
                if (i >= inAvail) {
                    break;
                }
                byte[] buffer = ensureBufferSize(this.decodeSize, content);
                int inPos2 = inPos + 1;
                byte b2 = in[inPos];
                if (b2 == this.pad) {
                    content.mEof = true;
                    break;
                }
                if (b2 >= 0) {
                    byte[] bArr = DECODE_TABLE;
                    if (b2 < bArr.length && (b = bArr[b2]) >= 0) {
                        content.mModulus = (content.mModulus + 1) % 4;
                        content.mIbitWorkArea = (content.mIbitWorkArea << 6) + b;
                        if (content.mModulus == 0) {
                            int i2 = content.mPos;
                            content.mPos = i2 + 1;
                            buffer[i2] = (byte) ((content.mIbitWorkArea >> 16) & 255);
                            int i3 = content.mPos;
                            content.mPos = i3 + 1;
                            buffer[i3] = (byte) ((content.mIbitWorkArea >> 8) & 255);
                            int i4 = content.mPos;
                            content.mPos = i4 + 1;
                            buffer[i4] = (byte) (content.mIbitWorkArea & 255);
                        }
                    }
                }
                i++;
                inPos = inPos2;
            }
            if (content.mEof && content.mModulus != 0) {
                byte[] buffer2 = ensureBufferSize(this.decodeSize, content);
                int i5 = content.mModulus;
                if (i5 == 1) {
                    return;
                }
                if (i5 == 2) {
                    content.mIbitWorkArea >>= 4;
                    int i6 = content.mPos;
                    content.mPos = i6 + 1;
                    buffer2[i6] = (byte) (content.mIbitWorkArea & 255);
                } else if (i5 == 3) {
                    content.mIbitWorkArea >>= 2;
                    int i7 = content.mPos;
                    content.mPos = i7 + 1;
                    buffer2[i7] = (byte) ((content.mIbitWorkArea >> 8) & 255);
                    int i8 = content.mPos;
                    content.mPos = i8 + 1;
                    buffer2[i8] = (byte) (content.mIbitWorkArea & 255);
                } else {
                    throw new IllegalStateException("Impossible modulus " + content.mModulus);
                }
            }
        }
    }

    @Deprecated
    public static boolean isArrayByteBase64(byte[] arrayOctet) {
        return isBase64(arrayOctet);
    }

    public static boolean isBase64(byte octet) {
        if (octet != 61) {
            if (octet >= 0) {
                byte[] bArr = DECODE_TABLE;
                if (octet >= bArr.length || bArr[octet] == -1) {
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean isBase64(String base64) {
        return isBase64(base64.getBytes());
    }

    public static boolean isBase64(byte[] arrayOctet) {
        for (int i = 0; i < arrayOctet.length; i++) {
            if (!isBase64(arrayOctet[i]) && !isWhiteSpace(arrayOctet[i])) {
                return false;
            }
        }
        return true;
    }

    public static byte[] encodeBase64(byte[] binaryData) {
        return encodeBase64(binaryData, false);
    }

    public static String encodeBase64String(byte[] binaryData) {
        return new String(encodeBase64(binaryData, false));
    }

    public static byte[] encodeBase64URLSafe(byte[] binaryData) {
        return encodeBase64(binaryData, false, true);
    }

    public static String encodeBase64URLSafeString(byte[] binaryData) {
        return new String(encodeBase64(binaryData, false, true));
    }

    public static byte[] encodeBase64Chunked(byte[] binaryData) {
        return encodeBase64(binaryData, true);
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked) {
        return encodeBase64(binaryData, isChunked, false);
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked, boolean urlSafe) {
        return encodeBase64(binaryData, isChunked, urlSafe, Integer.MAX_VALUE);
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked, boolean urlSafe, int maxResultSize) {
        SauBase64 b64;
        if (binaryData == null || binaryData.length == 0) {
            return binaryData;
        }
        if (!isChunked) {
            b64 = new SauBase64(0, CHUNK_SEPARATOR, urlSafe);
        }
        long len = b64.getEncodedLength(binaryData);
        if (len <= ((long) maxResultSize)) {
            return b64.encode(binaryData);
        }
        throw new IllegalArgumentException("Input array too big, the output array would be bigger (" + len + ") than the specified maximum size of " + maxResultSize);
    }

    public static byte[] decodeBase64(String base64String) {
        return new SauBase64().decodeString(base64String);
    }

    public static byte[] decodeBase64(byte[] base64Data) {
        return new SauBase64().decode(base64Data);
    }

    public static BigInteger decodeInteger(byte[] pArray) {
        return new BigInteger(1, decodeBase64(pArray));
    }

    public static byte[] encodeInteger(BigInteger bigInt) {
        if (bigInt != null) {
            return encodeBase64(toIntegerBytes(bigInt), false);
        }
        throw new NullPointerException("encodeInteger called with null parameter");
    }

    private static byte[] toIntegerBytes(BigInteger bigInt) {
        int bitlen = ((bigInt.bitLength() + 7) >> 3) << 3;
        byte[] bigBytes = bigInt.toByteArray();
        if (bigInt.bitLength() % 8 != 0 && (bigInt.bitLength() / 8) + 1 == bitlen / 8) {
            return bigBytes;
        }
        int startSrc = 0;
        int len = bigBytes.length;
        if (bigInt.bitLength() % 8 == 0) {
            startSrc = 1;
            len--;
        }
        byte[] resizedBytes = new byte[(bitlen / 8)];
        System.arraycopy(bigBytes, startSrc, resizedBytes, (bitlen / 8) - len, len);
        return resizedBytes;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.rsa.BaseCoder
    public boolean isInAlphabet(byte octet) {
        if (octet >= 0) {
            byte[] bArr = this.decodeTable;
            return octet < bArr.length && bArr[octet] != -1;
        }
    }
}
