package org.simalliance.openmobileapi.internal;

import java.util.ArrayList;

public final class HistoricalBytesUtilities {
    private HistoricalBytesUtilities() {
    }

    public static byte[] getHistBytes(byte[] atr) {
        byte tdi = atr[1];
        boolean areNextInterfaceBytesPresent = (tdi & 240) != 0;
        ArrayList<Integer> tValues = new ArrayList();
        tValues.add(Integer.valueOf(tdi & 15));
        int position = 2;
        while (areNextInterfaceBytesPresent) {
            if ((tdi & 16) != 0) {
                position++;
            }
            if ((tdi & 32) != 0) {
                position++;
            }
            if ((tdi & 64) != 0) {
                position++;
            }
            if ((tdi & 128) != 0) {
                tdi = atr[position];
                areNextInterfaceBytesPresent = (tdi & 240) != 0;
                tValues.add(Integer.valueOf(tdi & 15));
                position++;
            } else {
                areNextInterfaceBytesPresent = false;
            }
        }
        int length = atr.length - position;
        if (isTckPresent(tValues)) {
            length--;
        }
        byte[] historicalBytes = new byte[length];
        System.arraycopy(atr, position, historicalBytes, 0, length);
        return historicalBytes;
    }

    private static boolean isTckPresent(ArrayList<Integer> tValues) {
        int i = 0;
        if (tValues.size() == 1) {
            i = tValues.contains(Integer.valueOf(0));
        }
        return i ^ 1;
    }
}
