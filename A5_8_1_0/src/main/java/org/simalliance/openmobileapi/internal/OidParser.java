package org.simalliance.openmobileapi.internal;

import java.util.ArrayList;

public final class OidParser {
    private OidParser() {
    }

    public static byte[] encodeOid(String oid) throws IllegalArgumentException {
        ArrayList<Byte> byteList = new ArrayList();
        String[] oidNumbers = oid.split("\\.");
        if (oidNumbers.length < 3) {
            throw new IllegalArgumentException();
        }
        try {
            byteList.add(Byte.valueOf(encondeFirstDigits(Integer.parseInt(oidNumbers[0]), Integer.parseInt(oidNumbers[1]))));
            int j = 2;
            while (j < oidNumbers.length) {
                try {
                    for (byte b : encodeInteger(Integer.parseInt(oidNumbers[j]))) {
                        byteList.add(Byte.valueOf(b));
                    }
                    j++;
                } catch (Exception e) {
                    throw new IllegalArgumentException();
                }
            }
            byte[] result = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                result[i] = ((Byte) byteList.get(i)).byteValue();
            }
            return result;
        } catch (Exception e2) {
            throw new IllegalArgumentException();
        }
    }

    public static byte encondeFirstDigits(int first, int second) throws IllegalArgumentException {
        if (first >= 0 && second >= 0) {
            return (byte) ((first * 40) + second);
        }
        throw new IllegalArgumentException();
    }

    public static byte[] encodeInteger(int number) throws IllegalArgumentException {
        if (number < 0) {
            throw new IllegalArgumentException();
        } else if (number == 0) {
            return new byte[]{(byte) 0};
        } else {
            boolean isFirstIteration = true;
            byte[] result = new byte[0];
            while (number != 0) {
                byte[] aux = new byte[result.length];
                System.arraycopy(result, 0, aux, 0, result.length);
                result = new byte[(result.length + 1)];
                System.arraycopy(aux, 0, result, 1, aux.length);
                byte value = (byte) (number & 127);
                if (isFirstIteration) {
                    isFirstIteration = false;
                } else {
                    value = (byte) (value | 128);
                }
                result[0] = value;
                number >>= 7;
            }
            return result;
        }
    }
}
