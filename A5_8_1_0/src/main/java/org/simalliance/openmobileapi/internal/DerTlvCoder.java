package org.simalliance.openmobileapi.internal;

import org.simalliance.openmobileapi.util.ISO7816;

public final class DerTlvCoder {
    public static final byte[] TAG_INTEGER = new byte[]{(byte) 2};
    public static final byte[] TAG_OCTET_STRING = new byte[]{(byte) 4};
    public static final byte[] TAG_SEQUENCE = new byte[]{(byte) 48};

    private DerTlvCoder() {
    }

    public static byte[] encodeLength(int lengthValue) throws IllegalArgumentException {
        if (lengthValue < 0) {
            throw new IllegalArgumentException(ErrorStrings.paramInvalidValue("lengthValue"));
        }
        byte[] rawLength = ByteArrayConverter.intToByteArray(lengthValue);
        if (lengthValue <= 127) {
            return new byte[]{(byte) (rawLength[3] & 127)};
        } else if (lengthValue <= ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) {
            return new byte[]{(byte) -127, rawLength[3]};
        } else if (lengthValue <= ISO7816.MAX_COMMAND_DATA_LENGTH) {
            return new byte[]{ISO7816.INS_EXTERNAL_AUTHENTICATE, rawLength[2], rawLength[3]};
        } else if (lengthValue <= 16777215) {
            return new byte[]{(byte) -125, rawLength[1], rawLength[2], rawLength[3]};
        } else {
            return new byte[]{ISO7816.INS_GET_CHALLENGE, rawLength[0], rawLength[1], rawLength[2], rawLength[3]};
        }
    }

    public static byte[] encodeInteger(int value) {
        byte[] valueByteArray = ByteArrayConverter.intToByteArray(value);
        while (valueByteArray[0] == (byte) 0) {
            byte[] tmp = new byte[(valueByteArray.length - 1)];
            System.arraycopy(valueByteArray, 1, tmp, 0, tmp.length);
            valueByteArray = new byte[tmp.length];
            System.arraycopy(tmp, 0, valueByteArray, 0, tmp.length);
        }
        byte[] lengthByteArray = encodeLength(valueByteArray.length);
        byte[] encodedInteger = new byte[((TAG_INTEGER.length + lengthByteArray.length) + valueByteArray.length)];
        System.arraycopy(TAG_INTEGER, 0, encodedInteger, 0, TAG_INTEGER.length);
        System.arraycopy(lengthByteArray, 0, encodedInteger, TAG_INTEGER.length, lengthByteArray.length);
        System.arraycopy(valueByteArray, 0, encodedInteger, TAG_INTEGER.length + lengthByteArray.length, valueByteArray.length);
        return encodedInteger;
    }

    public static byte[] encodeOctetString(byte[] octetString) {
        byte[] lengthByteArray = encodeLength(octetString.length);
        byte[] encodedOctetString = new byte[((TAG_OCTET_STRING.length + lengthByteArray.length) + octetString.length)];
        System.arraycopy(TAG_OCTET_STRING, 0, encodedOctetString, 0, TAG_OCTET_STRING.length);
        System.arraycopy(lengthByteArray, 0, encodedOctetString, TAG_OCTET_STRING.length, lengthByteArray.length);
        System.arraycopy(octetString, 0, encodedOctetString, TAG_OCTET_STRING.length + lengthByteArray.length, octetString.length);
        return encodedOctetString;
    }

    public static byte[] encodeSequence(byte[] sequence) {
        byte[] lengthByteArray = encodeLength(sequence.length);
        byte[] encodedSequence = new byte[((TAG_SEQUENCE.length + lengthByteArray.length) + sequence.length)];
        System.arraycopy(TAG_SEQUENCE, 0, encodedSequence, 0, TAG_SEQUENCE.length);
        System.arraycopy(lengthByteArray, 0, encodedSequence, TAG_SEQUENCE.length, lengthByteArray.length);
        System.arraycopy(sequence, 0, encodedSequence, TAG_SEQUENCE.length + lengthByteArray.length, sequence.length);
        return encodedSequence;
    }
}
