package com.android.server.wifi.util;

import android.net.wifi.WifiSsid;
import android.util.Log;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.wificond.HiddenNetwork;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class GbkUtil {
    private static final String TAG = "GbkUtil";
    private static ArrayList<String> mGbkList = new ArrayList<>();

    private GbkUtil() {
    }

    public static void clear() {
        synchronized (mGbkList) {
            mGbkList.clear();
        }
    }

    public static void checkAndSetGbk(WifiSsid ssid) {
        byte[] byteArray = ssid.octets.toByteArray();
        if (isNotUtf8(byteArray, 0, byteArray.length)) {
            Log.d(TAG, "SSID " + ssid.toString() + " is GBK encoding");
            ssid.mIsGbkEncoding = true;
            String str = ssid.toString();
            synchronized (mGbkList) {
                if (!mGbkList.contains(str)) {
                    mGbkList.add(str);
                }
            }
        }
    }

    public static boolean checkAndSetGbk(byte[] byteArray) {
        if (!isNotUtf8(byteArray, 0, byteArray.length)) {
            return false;
        }
        WifiSsid ssid = WifiSsid.createFromByteArray(byteArray);
        Log.d(TAG, "SSID " + ssid.toString() + " is GBK encoding");
        ssid.mIsGbkEncoding = true;
        String str = ssid.toString();
        synchronized (mGbkList) {
            if (!mGbkList.contains(str)) {
                mGbkList.add(str);
            }
        }
        return true;
    }

    public static ArrayList<Byte> stringToByteArrayList(String str) {
        ArrayList<Byte> byteArrayList = new ArrayList<>();
        try {
            for (byte b : str.getBytes("GBK")) {
                byteArrayList.add(new Byte(b));
            }
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "UnsupportedEncodingException: " + e.toString());
        }
        return byteArrayList;
    }

    public static byte[] stringToByteArray(String str) {
        try {
            return str.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "UnsupportedEncodingException: " + e.toString());
            return null;
        }
    }

    public static boolean isGbkSsid(String ssid) {
        boolean contains;
        synchronized (mGbkList) {
            contains = mGbkList.contains(NativeUtil.removeEnclosingQuotes(ssid));
        }
        return contains;
    }

    public static HiddenNetwork needAddExtraGbkSsid(String ssid_original) {
        String ssid = NativeUtil.removeEnclosingQuotes(ssid_original);
        if (isAllASCII(NativeUtil.decodeSsid(ssid_original)) || isGbkSsid(ssid)) {
            return null;
        }
        HiddenNetwork network = new HiddenNetwork();
        try {
            network.ssid = NativeUtil.byteArrayFromArrayList(stringToByteArrayList(ssid));
            return network;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal argument " + ssid, e);
            return null;
        }
    }

    public static boolean isAllASCII(ArrayList<Byte> ssidByteArray) {
        if (ssidByteArray == null) {
            return false;
        }
        for (int i = 0; i < ssidByteArray.size(); i++) {
            if (!isASCII(ssidByteArray.get(i).byteValue())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNotUtf8(byte[] input, int ssidStartPos, int ssidEndPos) {
        int nBytes = 0;
        int lastWildcar = 0;
        boolean isAllAscii = true;
        boolean isAllGBK = true;
        boolean isWildcardChar = false;
        int Utf_bit = 0;
        byte Utf_char_H = 0;
        int i = ssidStartPos;
        while (i < ssidEndPos && i < input.length) {
            byte chr = input[i];
            if (!isASCII(chr)) {
                isAllAscii = false;
                isWildcardChar = !isWildcardChar;
                if (isWildcardChar) {
                    if (i >= input.length - 1) {
                        isAllGBK = false;
                    } else if (!isGBKChar(chr, input[i + 1])) {
                        isAllGBK = false;
                    }
                }
            } else {
                isWildcardChar = false;
            }
            if (nBytes == 0) {
                if ((chr & 255) >= 128) {
                    lastWildcar = i;
                    int nBytes2 = getUtf8CharLen(chr);
                    if (nBytes2 == 0) {
                        return true;
                    }
                    if (nBytes2 == 6) {
                        Utf_bit = 5;
                    } else if (nBytes2 == 5) {
                        Utf_bit = 4;
                    } else if (nBytes2 == 4) {
                        Utf_bit = 3;
                    } else if (nBytes2 == 3) {
                        Utf_bit = 2;
                    } else if (nBytes2 == 2) {
                        Utf_bit = 2;
                    }
                    Utf_char_H = chr;
                    nBytes = nBytes2 - 1;
                } else {
                    continue;
                }
            } else if ((chr & 192) != 128) {
                break;
            } else {
                if (nBytes == 5 && Utf_bit == 5) {
                    Utf_bit = 0;
                    if ((Utf_char_H & 1) == 0 && (chr & 255) < 132) {
                        return true;
                    }
                } else if (nBytes == 4 && Utf_bit == 4) {
                    Utf_bit = 0;
                    if ((Utf_char_H & 3) == 0 && (chr & 255) < 136) {
                        return true;
                    }
                } else if (nBytes == 3 && Utf_bit == 3) {
                    Utf_bit = 0;
                    if ((Utf_char_H & 7) == 0 && (chr & 255) < 144) {
                        return true;
                    }
                } else if (nBytes == 2 && Utf_bit == 2) {
                    Utf_bit = 0;
                    if ((Utf_char_H & 15) == 0 && (chr & 255) < 160) {
                        return true;
                    }
                } else if (nBytes == 1 && Utf_bit == 2) {
                    Utf_bit = 0;
                    if ((Utf_char_H & 255) < 194) {
                        return true;
                    }
                }
                nBytes--;
            }
            i++;
        }
        if (nBytes <= 0 || isAllAscii) {
            return false;
        }
        if (isAllGBK) {
            return true;
        }
        int nBytes3 = getUtf8CharLen(input[lastWildcar]);
        int j = lastWildcar;
        while (j < lastWildcar + nBytes3 && j < input.length) {
            if (!isASCII(input[j])) {
                input[j] = 32;
            }
            j++;
        }
        return false;
    }

    private static int getUtf8CharLen(byte firstByte) {
        if (firstByte >= -4 && firstByte <= -3) {
            return 6;
        }
        if (firstByte >= -8) {
            return 5;
        }
        if (firstByte >= -16) {
            return 4;
        }
        if (firstByte >= -32) {
            return 3;
        }
        if (firstByte >= -64) {
            return 2;
        }
        return 0;
    }

    private static boolean isASCII(byte b) {
        if ((b & 128) == 0) {
            return true;
        }
        return false;
    }

    private static boolean isGBKChar(byte head, byte tail) {
        int b0 = head & Constants.BYTE_MASK;
        int b1 = tail & Constants.BYTE_MASK;
        if (b0 >= 161 && b0 <= 169 && b1 >= 161 && b1 <= 254) {
            return true;
        }
        if (b0 >= 176 && b0 <= 247 && b1 >= 161 && b1 <= 254) {
            return true;
        }
        if (b0 >= 129 && b0 <= 160 && b1 >= 64 && b1 <= 254 && b1 != 127) {
            return true;
        }
        if (b0 >= 170 && b0 <= 254 && b1 >= 64 && b1 <= 160 && b1 != 127) {
            return true;
        }
        if (b0 >= 168 && b0 <= 169 && b1 >= 64 && b1 <= 160 && b1 != 127) {
            return true;
        }
        if (b0 >= 170 && b0 <= 175 && b1 >= 161 && b1 <= 254) {
            return true;
        }
        if (b0 >= 248 && b0 <= 254 && b1 >= 161 && b1 <= 254) {
            return true;
        }
        if (b0 < 161 || b0 > 167 || b1 < 64 || b1 > 160 || b1 == 127) {
            return false;
        }
        return true;
    }
}
