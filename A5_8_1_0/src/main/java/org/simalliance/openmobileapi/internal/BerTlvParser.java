package org.simalliance.openmobileapi.internal;

import org.simalliance.openmobileapi.util.ISO7816;

public final class BerTlvParser extends TlvParser {
    public byte[] getTagBytes(byte[] array, int position) throws IllegalArgumentException {
        byte[] tagBytes;
        if ((array[position] & 31) != 31) {
            tagBytes = new byte[1];
        } else if ((array[position + 1] >= (byte) 0 && array[position + 1] <= (byte) 30) || (array[position + 1] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) == 128) {
            throw new IllegalArgumentException("Invalid \"tag\" field at position " + position + ".");
        } else if ((array[position + 1] & 128) == 0) {
            tagBytes = new byte[2];
        } else if ((array[position + 1] & 128) == 128 && (array[position + 2] & 128) == 0) {
            tagBytes = new byte[3];
        } else {
            throw new IllegalArgumentException("Invalid \"tag\" field at position " + position + ".");
        }
        System.arraycopy(array, position, tagBytes, 0, tagBytes.length);
        return tagBytes;
    }

    public byte[] getLengthBytes(byte[] array, int position) throws IllegalArgumentException {
        byte[] length;
        if ((array[position] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) < 128) {
            length = new byte[1];
        } else if ((array[position] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) == 129) {
            length = new byte[2];
        } else if ((array[position] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) == 130) {
            length = new byte[3];
        } else if ((array[position] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) == 131) {
            length = new byte[4];
        } else if ((array[position] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) == 132) {
            length = new byte[5];
        } else {
            throw new IllegalArgumentException("Invalid length field at position " + position + ".");
        }
        System.arraycopy(array, position, length, 0, length.length);
        return length;
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
