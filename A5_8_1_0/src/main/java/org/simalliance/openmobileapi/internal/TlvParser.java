package org.simalliance.openmobileapi.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TlvParser {
    abstract byte[] getLengthBytes(byte[] bArr, int i) throws IllegalArgumentException;

    abstract int getLengthValue(byte[] bArr);

    abstract byte[] getTagBytes(byte[] bArr, int i) throws IllegalArgumentException;

    public List<TlvEntryWrapper> parseArray(byte[] array) {
        byte[] data = getValidTlvData(array);
        int position = 0;
        ArrayList<TlvEntryWrapper> list = new ArrayList();
        while (position < data.length) {
            TlvEntryWrapper nextEntry = new TlvEntryWrapper(data, position, this);
            list.add(nextEntry);
            position += nextEntry.getTotalLength();
        }
        return list;
    }

    public int searchTag(byte[] data, byte[] tag, int startPosition) throws IllegalArgumentException {
        if (startPosition < 0) {
            throw new IllegalArgumentException(ErrorStrings.paramInvalidValue("startPosition"));
        }
        int position = startPosition;
        while (position < data.length) {
            TlvEntryWrapper derEntry = new TlvEntryWrapper(data, position, this);
            if (Arrays.equals(tag, derEntry.getTag())) {
                return position;
            }
            position += derEntry.getTotalLength();
        }
        throw new IllegalArgumentException(ErrorStrings.TLV_TAG_NOT_FOUND);
    }

    public byte[] getValidTlvData(byte[] rawData) {
        if (isValidTlvStructure(rawData)) {
            return rawData;
        }
        int position = 0;
        while (true) {
            int tmpPosition = position;
            try {
                tmpPosition += getTagBytes(rawData, tmpPosition).length;
                byte[] length = getLengthBytes(rawData, tmpPosition);
                tmpPosition = (tmpPosition + length.length) + getLengthValue(length);
                if (tmpPosition > rawData.length) {
                    break;
                }
                position = tmpPosition;
            } catch (Exception e) {
            }
        }
        byte[] validData = new byte[position];
        System.arraycopy(rawData, 0, validData, 0, position);
        return validData;
    }

    public boolean isValidTlvStructure(byte[] data) {
        boolean z = false;
        int position = 0;
        while (position < data.length) {
            try {
                position += getTagBytes(data, position).length;
                byte[] length = getLengthBytes(data, position);
                position = (position + length.length) + getLengthValue(length);
            } catch (Exception e) {
                return false;
            }
        }
        if (position == data.length) {
            z = true;
        }
        return z;
    }
}
