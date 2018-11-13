package org.simalliance.openmobileapi.internal;

import org.simalliance.openmobileapi.util.ISO7816;

public class DerTlvParser extends TlvParser {
    public byte[] getTagBytes(byte[] array, int position) throws IllegalArgumentException {
        byte[] tagBytes;
        if ((array[position] & 31) != 31) {
            tagBytes = new byte[1];
        } else {
            int i = position;
            i = position + 1;
            if ((array[i] & 127) == 0) {
                throw new IllegalArgumentException(ErrorStrings.TLV_INVALID_TAG);
            }
            int length = 2;
            while ((array[i] & 128) == 128) {
                length++;
                i++;
            }
            tagBytes = new byte[length];
        }
        System.arraycopy(array, position, tagBytes, 0, tagBytes.length);
        return tagBytes;
    }

    public byte[] getLengthBytes(byte[] array, int position) throws IllegalArgumentException {
        byte[] lengthBytes;
        if ((array[position] & 128) != 128) {
            lengthBytes = new byte[1];
        } else if ((array[position] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) == ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) {
            throw new IllegalArgumentException(ErrorStrings.TLV_INVALID_LENGTH);
        } else {
            lengthBytes = new byte[((array[position] & 127) + 1)];
        }
        System.arraycopy(array, position, lengthBytes, 0, lengthBytes.length);
        return lengthBytes;
    }

    public int getLengthValue(byte[] lengthBytes) {
        int valueStartPosition;
        byte[] lengthValue;
        if (lengthBytes.length == 1) {
            valueStartPosition = 0;
            lengthValue = new byte[1];
        } else {
            valueStartPosition = 1;
            lengthValue = new byte[(lengthBytes.length - 1)];
        }
        System.arraycopy(lengthBytes, valueStartPosition, lengthValue, 0, lengthValue.length);
        return ByteArrayConverter.byteArrayToInt(lengthValue);
    }
}
