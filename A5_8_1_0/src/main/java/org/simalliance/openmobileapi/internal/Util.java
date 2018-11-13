package org.simalliance.openmobileapi.internal;

import android.content.Context;
import android.content.pm.PackageManager;
import java.security.AccessControlException;
import org.simalliance.openmobileapi.util.ISO7816;

public class Util {
    public static final byte END = (byte) -1;

    public static byte[] mergeBytes(byte[] array1, byte[] array2) {
        byte[] data = new byte[(array1.length + array2.length)];
        System.arraycopy(array1, 0, data, 0, array1.length);
        System.arraycopy(array2, 0, data, array1.length, array2.length);
        return data;
    }

    public static byte[] getMid(byte[] array, int start, int length) {
        byte[] data = new byte[length];
        System.arraycopy(array, start, data, 0, length);
        return data;
    }

    @Deprecated
    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x ", new Object[]{Integer.valueOf(bytes[i] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED)}));
        }
        String str = sb.toString();
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static byte[] appendResponse(byte[] r1, byte[] r2, int length) {
        byte[] rsp = new byte[(r1.length + length)];
        System.arraycopy(r1, 0, rsp, 0, r1.length);
        System.arraycopy(r2, 0, rsp, r1.length, length);
        return rsp;
    }

    public static String createMessage(String commandName, int sw) {
        StringBuilder message = new StringBuilder();
        if (commandName != null) {
            message.append(commandName).append(" ");
        }
        message.append("SW1/2 error: ");
        message.append(Integer.toHexString(ISO7816.MAX_RESPONSE_DATA_LENGTH | sw).substring(1));
        return message.toString();
    }

    public static String createMessage(String commandName, String message) {
        if (commandName == null) {
            return message;
        }
        return commandName + " " + message;
    }

    @Deprecated
    public static String bytesToString(byte[] array, int offset, int length, String prefix) {
        if (array == null) {
            return null;
        }
        if (length == -1) {
            length = array.length - offset;
        }
        StringBuilder buffer = new StringBuilder();
        for (int ind = offset; ind < offset + length; ind++) {
            buffer.append(prefix).append(Integer.toHexString((array[ind] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) + ISO7816.MAX_RESPONSE_DATA_LENGTH_NO_EXTENDED).substring(1));
        }
        return buffer.toString();
    }

    public static String getPackageNameFromCallingUid(Context context, int uid) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            String[] packageName = packageManager.getPackagesForUid(uid);
            if (packageName != null && packageName.length > 0) {
                return packageName[0];
            }
        }
        throw new AccessControlException("Caller PackageName can not be determined");
    }

    public static byte setChannelToClassByte(byte cla, int channelNumber) {
        if (channelNumber < 4) {
            return (byte) ((cla & 188) | channelNumber);
        }
        if (channelNumber < 20) {
            boolean isSM = (cla & 12) != 0;
            cla = (byte) (((cla & 176) | 64) | (channelNumber - 4));
            if (isSM) {
                return (byte) (cla | 32);
            }
            return cla;
        }
        throw new IllegalArgumentException("Channel number must be within [0..19]");
    }

    public static byte clearChannelNumber(byte cla) {
        if ((cla & 64) == 0) {
            return (byte) (cla & 252);
        }
        return (byte) (cla & 240);
    }

    public static int parseChannelNumber(byte cla) {
        if ((cla & 64) == 0) {
            return cla & 3;
        }
        return (cla & 15) + 4;
    }
}
