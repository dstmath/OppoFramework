package com.android.dex;

import com.android.dex.util.ByteInput;
import com.android.dex.util.ByteOutput;

public final class Leb128 {
    private Leb128() {
    }

    public static int unsignedLeb128Size(int value) {
        int remaining = value >> 7;
        int count = 0;
        while (remaining != 0) {
            remaining >>= 7;
            count++;
        }
        return count + 1;
    }

    public static int signedLeb128Size(int value) {
        int remaining = value >> 7;
        int count = 0;
        boolean hasMore = true;
        int end = (Integer.MIN_VALUE & value) == 0 ? 0 : -1;
        while (hasMore) {
            hasMore = remaining == end ? (remaining & 1) != ((value >> 6) & 1) : true;
            value = remaining;
            remaining >>= 7;
            count++;
        }
        return count;
    }

    public static int readSignedLeb128(ByteInput in) {
        int cur;
        int result = 0;
        int count = 0;
        int signBits = -1;
        do {
            cur = in.readByte() & 255;
            result |= (cur & 127) << (count * 7);
            signBits <<= 7;
            count++;
            if ((cur & 128) != 128) {
                break;
            }
        } while (count < 5);
        if ((cur & 128) == 128) {
            throw new DexException("invalid LEB128 sequence");
        } else if (((signBits >> 1) & result) != 0) {
            return result | signBits;
        } else {
            return result;
        }
    }

    public static int readUnsignedLeb128(ByteInput in) {
        int cur;
        int result = 0;
        int count = 0;
        do {
            cur = in.readByte() & 255;
            result |= (cur & 127) << (count * 7);
            count++;
            if ((cur & 128) != 128) {
                break;
            }
        } while (count < 5);
        if ((cur & 128) != 128) {
            return result;
        }
        throw new DexException("invalid LEB128 sequence");
    }

    public static void writeUnsignedLeb128(ByteOutput out, int value) {
        for (int remaining = value >>> 7; remaining != 0; remaining >>>= 7) {
            out.writeByte((byte) ((value & 127) | 128));
            value = remaining;
        }
        out.writeByte((byte) (value & 127));
    }

    public static void writeSignedLeb128(ByteOutput out, int value) {
        int remaining = value >> 7;
        boolean hasMore = true;
        int end = (Integer.MIN_VALUE & value) == 0 ? 0 : -1;
        while (hasMore) {
            int i;
            if (remaining != end) {
                hasMore = true;
            } else if ((remaining & 1) != ((value >> 6) & 1)) {
                hasMore = true;
            } else {
                hasMore = false;
            }
            int i2 = value & 127;
            if (hasMore) {
                i = 128;
            } else {
                i = 0;
            }
            out.writeByte((byte) (i | i2));
            value = remaining;
            remaining >>= 7;
        }
    }
}
