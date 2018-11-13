package org.simalliance.openmobileapi.internal;

import org.simalliance.openmobileapi.util.ISO7816;

public final class ByteArrayConverter {
    private ByteArrayConverter() {
    }

    public static String byteArrayToPathString(byte[] rawPath) throws IllegalArgumentException {
        if (rawPath.length % 2 != 0) {
            throw new IllegalArgumentException("Invald path");
        }
        byte[] buffer = new byte[2];
        String path = "";
        for (int i = 0; i < rawPath.length; i += 2) {
            System.arraycopy(rawPath, i, buffer, 0, 2);
            String fid = byteArrayToHexString(buffer);
            if (!fid.equalsIgnoreCase("3F00")) {
                path = path.concat(fid);
                if (i != rawPath.length - 2) {
                    path = path.concat(":");
                }
            }
        }
        return path;
    }

    public static String byteArrayToHexString(byte[] array, int offset, int length) {
        if (array == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", new Object[]{Integer.valueOf(array[offset + i] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED)}));
        }
        return sb.toString();
    }

    public static String byteArrayToHexString(byte[] array, int offset) {
        StringBuffer s = new StringBuffer();
        for (int i = offset; i < array.length; i++) {
            s.append(Integer.toHexString((array[i] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) + ISO7816.MAX_RESPONSE_DATA_LENGTH_NO_EXTENDED).substring(1));
        }
        return s.toString();
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        if (byteArray == null) {
            return "";
        }
        return byteArrayToHexString(byteArray, 0, byteArray.length);
    }

    public static byte[] hexStringToByteArray(String str, int offset, int length) {
        if (length % 2 != 0) {
            throw new IllegalArgumentException("length must be multiple of 2");
        }
        str = str.toUpperCase();
        byte[] outputBytes = new byte[(str.length() / 2)];
        int i = 0;
        while (i < length) {
            char c1 = str.charAt(i + offset);
            char c2 = str.charAt((i + 1) + offset);
            if (isHexChar(c1) && (isHexChar(c2) ^ 1) == 0) {
                outputBytes[i / 2] = (byte) ((Character.digit(c1, 16) << 4) + Character.digit(c2, 16));
                i += 2;
            } else {
                throw new IllegalArgumentException("Invalid char found");
            }
        }
        return outputBytes;
    }

    public static byte[] hexStringToByteArray(String str) {
        return hexStringToByteArray(str, 0, str.length());
    }

    public static byte[] intToByteArray(int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
    }

    public static int byteArrayToInt(byte[] byteArray) {
        switch (byteArray.length) {
            case ISO7816.OFFSET_CLA /*0*/:
                return 0;
            case ISO7816.OFFSET_INS /*1*/:
                return byteArray[0] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED;
            case 2:
                return ((byteArray[0] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 8) | (byteArray[1] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
            case ISO7816.OFFSET_P2 /*3*/:
                return (((byteArray[0] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 16) | ((byteArray[1] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 8)) | (byteArray[2] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
            default:
                return ((((byteArray[0] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 24) | ((byteArray[1] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 16)) | ((byteArray[2] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 8)) | (byteArray[3] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
        }
    }

    public static boolean isHexChar(char c) {
        if (Character.isLowerCase(c)) {
            c = Character.toUpperCase(c);
        }
        if (c >= '0' && c <= '9') {
            return true;
        }
        if (c < 'A' || c > 'F') {
            return false;
        }
        return true;
    }
}
