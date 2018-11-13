package com.qualcomm.qcrilhook;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PrimitiveParser {
    public static void checkByte(short val) throws NumberFormatException {
        if ((65280 & val) != 0) {
            throw new NumberFormatException();
        }
    }

    public static void checkByte(int val) throws NumberFormatException {
        if ((val & -256) != 0) {
            throw new NumberFormatException();
        }
    }

    public static byte parseByte(short val) throws NumberFormatException {
        checkByte(val);
        return (byte) (val & 255);
    }

    public static byte parseByte(int val) throws NumberFormatException {
        checkByte(val);
        return (byte) (val & 255);
    }

    public static byte parseByte(char val) throws NumberFormatException {
        checkByte((int) val);
        return (byte) (val & 255);
    }

    public static void checkShort(int val) throws NumberFormatException {
        if ((-65536 & val) != 0) {
            throw new NumberFormatException();
        }
    }

    public static short parseShort(int val) throws NumberFormatException {
        checkShort(val);
        return (short) (65535 & val);
    }

    public static void checkInt(long val) throws NumberFormatException {
        if ((-4294967296L & val) != 0) {
            throw new NumberFormatException();
        }
    }

    public static int parseInt(long val) throws NumberFormatException {
        checkInt(val);
        return (int) (4294967295L & val);
    }

    public static long parseLong(String val) throws NumberFormatException {
        ByteBuffer buf = ByteBuffer.wrap(new BigInteger(val).toByteArray());
        buf.order(ByteOrder.BIG_ENDIAN);
        return buf.getLong();
    }

    public static short toUnsigned(byte val) {
        return (short) (val & 255);
    }

    public static int toUnsigned(short val) {
        return 65535 & val;
    }

    public static long toUnsigned(int val) {
        return (long) (val & -1);
    }

    public static byte parseUnsignedByte(String in) throws NumberFormatException {
        short t = Short.parseShort(in);
        if ((65280 & t) == 0) {
            return (byte) (t & 255);
        }
        throw new NumberFormatException();
    }

    public static short parseUnsignedShort(String in) throws NumberFormatException {
        int t = Integer.parseInt(in);
        if ((-65536 & t) == 0) {
            return (short) (65535 & t);
        }
        throw new NumberFormatException();
    }

    public static int parseUnsignedInt(String in) throws NumberFormatException {
        long t = Long.parseLong(in);
        if ((-4294967296L & t) == 0) {
            return (int) (4294967295L & t);
        }
        throw new NumberFormatException();
    }

    public static String toUnsignedString(byte in) {
        return String.valueOf(toUnsigned(in));
    }

    public static String toUnsignedString(short in) {
        return String.valueOf(toUnsigned(in));
    }

    public static String toUnsignedString(int in) {
        return String.valueOf(toUnsigned(in));
    }
}
