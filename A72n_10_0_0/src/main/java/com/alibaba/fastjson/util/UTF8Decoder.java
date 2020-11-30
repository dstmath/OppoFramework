package com.alibaba.fastjson.util;

import com.alibaba.fastjson.asm.Opcodes;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public class UTF8Decoder extends CharsetDecoder {
    private static final Charset charset = Charset.forName("UTF-8");

    public UTF8Decoder() {
        super(charset, 1.0f, 1.0f);
    }

    private static boolean isNotContinuation(int b) {
        return (b & Opcodes.CHECKCAST) != 128;
    }

    private static boolean isMalformed2(int b1, int b2) {
        return (b1 & 30) == 0 || (b2 & Opcodes.CHECKCAST) != 128;
    }

    private static boolean isMalformed3(int b1, int b2, int b3) {
        return ((b1 != -32 || (b2 & 224) != 128) && (b2 & Opcodes.CHECKCAST) == 128 && (b3 & Opcodes.CHECKCAST) == 128) ? false : true;
    }

    private static boolean isMalformed4(int b2, int b3, int b4) {
        return ((b2 & Opcodes.CHECKCAST) == 128 && (b3 & Opcodes.CHECKCAST) == 128 && (b4 & Opcodes.CHECKCAST) == 128) ? false : true;
    }

    private static CoderResult lookupN(ByteBuffer src, int n) {
        for (int i = 1; i < n; i++) {
            if (isNotContinuation(src.get())) {
                return CoderResult.malformedForLength(i);
            }
        }
        return CoderResult.malformedForLength(n);
    }

    public static CoderResult malformedN(ByteBuffer src, int nb) {
        int i = 2;
        switch (nb) {
            case 1:
                int b1 = src.get();
                if ((b1 >> 2) == -2) {
                    if (src.remaining() < 4) {
                        return CoderResult.UNDERFLOW;
                    }
                    return lookupN(src, 5);
                } else if ((b1 >> 1) != -2) {
                    return CoderResult.malformedForLength(1);
                } else {
                    if (src.remaining() < 5) {
                        return CoderResult.UNDERFLOW;
                    }
                    return lookupN(src, 6);
                }
            case 2:
                return CoderResult.malformedForLength(1);
            case 3:
                int b12 = src.get();
                int b2 = src.get();
                if ((b12 == -32 && (b2 & 224) == 128) || isNotContinuation(b2)) {
                    i = 1;
                }
                return CoderResult.malformedForLength(i);
            case 4:
                int b13 = src.get() & 255;
                int b22 = src.get() & 255;
                if (b13 > 244 || ((b13 == 240 && (b22 < 144 || b22 > 191)) || ((b13 == 244 && (b22 & 240) != 128) || isNotContinuation(b22)))) {
                    return CoderResult.malformedForLength(1);
                }
                if (isNotContinuation(src.get())) {
                    return CoderResult.malformedForLength(2);
                }
                return CoderResult.malformedForLength(3);
            default:
                throw new IllegalStateException();
        }
    }

    private static CoderResult malformed(ByteBuffer src, int sp, CharBuffer dst, int dp, int nb) {
        src.position(sp - src.arrayOffset());
        CoderResult cr = malformedN(src, nb);
        src.position(sp);
        dst.position(dp);
        return cr;
    }

    private static CoderResult xflow(Buffer src, int sp, int sl, Buffer dst, int dp, int nb) {
        src.position(sp);
        dst.position(dp);
        return (nb == 0 || sl - sp < nb) ? CoderResult.UNDERFLOW : CoderResult.OVERFLOW;
    }

    private CoderResult decodeArrayLoop(ByteBuffer src, CharBuffer dst) {
        int destPosition;
        byte[] srcArray = src.array();
        int srcPosition = src.arrayOffset() + src.position();
        int srcLength = src.arrayOffset() + src.limit();
        char[] destArray = dst.array();
        int destPosition2 = dst.arrayOffset() + dst.position();
        int destLength = dst.arrayOffset() + dst.limit();
        for (int destPosition3 = Math.min(srcLength - srcPosition, destLength - destPosition2) + destPosition2; destPosition2 < destPosition3 && srcArray[srcPosition] >= 0; destPosition3 = destPosition3) {
            destArray[destPosition2] = (char) srcArray[srcPosition];
            destPosition2++;
            srcPosition++;
        }
        int srcPosition2 = srcPosition;
        int destPosition4 = destPosition2;
        while (srcPosition2 < srcLength) {
            byte b = srcArray[srcPosition2];
            if (b < 0) {
                if ((b >> 5) == -2) {
                    if (srcLength - srcPosition2 < 2 || destPosition4 >= destLength) {
                        return xflow(src, srcPosition2, srcLength, dst, destPosition4, 2);
                    }
                    byte b2 = srcArray[srcPosition2 + 1];
                    if (isMalformed2(b, b2)) {
                        return malformed(src, srcPosition2, dst, destPosition4, 2);
                    }
                    destPosition = destPosition4 + 1;
                    destArray[destPosition4] = (char) (((b << 6) ^ b2) ^ 3968);
                    srcPosition2 += 2;
                } else if ((b >> 4) == -2) {
                    if (srcLength - srcPosition2 < 3 || destPosition4 >= destLength) {
                        return xflow(src, srcPosition2, srcLength, dst, destPosition4, 3);
                    }
                    byte b3 = srcArray[srcPosition2 + 1];
                    byte b4 = srcArray[srcPosition2 + 2];
                    if (isMalformed3(b, b3, b4)) {
                        return malformed(src, srcPosition2, dst, destPosition4, 3);
                    }
                    destPosition = destPosition4 + 1;
                    destArray[destPosition4] = (char) ((((b << 12) ^ (b3 << 6)) ^ b4) ^ 8064);
                    srcPosition2 += 3;
                } else if ((b >> 3) != -2) {
                    return malformed(src, srcPosition2, dst, destPosition4, 1);
                } else {
                    if (srcLength - srcPosition2 < 4 || destLength - destPosition4 < 2) {
                        return xflow(src, srcPosition2, srcLength, dst, destPosition4, 4);
                    }
                    byte b5 = srcArray[srcPosition2 + 1];
                    byte b6 = srcArray[srcPosition2 + 2];
                    byte b7 = srcArray[srcPosition2 + 3];
                    int uc = ((b & 7) << 18) | ((b5 & 63) << 12) | ((b6 & 63) << 6) | (b7 & 63);
                    if (!isMalformed4(b5, b6, b7) && uc >= 65536) {
                        if (uc <= 1114111) {
                            int destPosition5 = destPosition4 + 1;
                            destArray[destPosition4] = (char) (55296 | (((uc - 65536) >> 10) & 1023));
                            destArray[destPosition5] = (char) (((uc - 65536) & 1023) | 56320);
                            srcPosition2 += 4;
                            destPosition4 = destPosition5 + 1;
                        }
                    }
                    return malformed(src, srcPosition2, dst, destPosition4, 4);
                }
                destPosition4 = destPosition;
            } else if (destPosition4 >= destLength) {
                return xflow(src, srcPosition2, srcLength, dst, destPosition4, 1);
            } else {
                destArray[destPosition4] = (char) b;
                srcPosition2++;
                destPosition4++;
            }
        }
        return xflow(src, srcPosition2, srcLength, dst, destPosition4, 0);
    }

    /* access modifiers changed from: protected */
    public CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        return decodeArrayLoop(src, dst);
    }
}
