package com.mediatek.mtklogger.c2klogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EtsUtil {
    public static short checkSum(byte[] data) {
        long checksum = 0;
        for (byte d : data) {
            checksum += (long) ((short) ((d & 255) | 0));
        }
        return (short) ((int) (65535 & checksum));
    }

    public static int checkSum2(byte[] data) {
        long checksum = 0;
        int length = data.length;
        byte[] temp = new byte[2];
        for (int index = 0; index < length - 1; index += 2) {
            temp[0] = data[index];
            temp[1] = data[index + 1];
            checksum += (long) (bytes2short(temp) & 65535);
        }
        return (int) (-1 & checksum);
    }

    public static int checkSum(File f) {
        long checksum = 0;
        try {
            FileInputStream fileImg = new FileInputStream(f);
            while (true) {
                byte[] block = new byte[2];
                if (fileImg.read(block) != 2) {
                    fileImg.close();
                    return (int) (-1 & checksum);
                }
                checksum += (long) (bytes2short(block) & 65535);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (IOException e2) {
            e2.printStackTrace();
            return 0;
        }
    }

    public static byte[] short2bytes(short val) {
        return new byte[]{(byte) (val & 255), (byte) ((val >> 8) & 255)};
    }

    public static short bytes2short(byte[] data) {
        return (short) ((data[1] << 8) | ((short) ((data[0] & 255) | 0)));
    }

    public static byte[] int2bytes(int val) {
        return new byte[]{(byte) (val & 255), (byte) ((val >> 8) & 255), (byte) ((val >> 16) & 255), (byte) ((val >> 24) & 255)};
    }

    public static int bytes2int(byte[] data) {
        return 0 | (data[0] & 255) | ((data[1] << 8) & 65280) | ((data[2] << EtsFun.BAND_CLASS_16) & 16711680) | ((data[3] << 24) & -16777216);
    }

    public static byte[] long2bytes(long val) {
        return new byte[]{(byte) ((int) (val & 255)), (byte) ((int) ((val >> 8) & 255)), (byte) ((int) ((val >> 16) & 255)), (byte) ((int) ((val >> 24) & 255)), (byte) ((int) ((val >> 32) & 255)), (byte) ((int) ((val >> 40) & 255)), (byte) ((int) ((val >> 48) & 255)), (byte) ((int) (255 & (val >> 56)))};
    }

    public static byte[] doubleToByte(double d) {
        byte[] bytes = new byte[8];
        long l = Double.doubleToLongBits(d);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Long.valueOf(l).byteValue();
            l >>= 8;
        }
        return bytes;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            String hv = Integer.toHexString(b & 255);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
            stringBuilder.append(' ');
        }
        return stringBuilder.toString();
    }
}
