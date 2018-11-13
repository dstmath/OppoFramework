package android.net.wifi;

import android.R;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Locale;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WifiSsid implements Parcelable {
    public static final Creator<WifiSsid> CREATOR = null;
    private static final int HEX_RADIX = 16;
    public static final String NONE = "<unknown ssid>";
    private static final String TAG = "WifiSsid";
    private boolean mIsGbkEncoding;
    public final ByteArrayOutputStream octets;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiSsid.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiSsid.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiSsid.<clinit>():void");
    }

    /* synthetic */ WifiSsid(WifiSsid wifiSsid) {
        this();
    }

    private WifiSsid() {
        this.octets = new ByteArrayOutputStream(32);
        this.mIsGbkEncoding = false;
    }

    public static WifiSsid createFromAsciiEncoded(String asciiEncoded) {
        WifiSsid a = new WifiSsid();
        a.convertToBytes(asciiEncoded);
        return a;
    }

    public static WifiSsid createFromHex(String hexStr) {
        WifiSsid a = new WifiSsid();
        if (hexStr == null) {
            return a;
        }
        if (hexStr.startsWith("0x") || hexStr.startsWith("0X")) {
            hexStr = hexStr.substring(2);
        }
        for (int i = 0; i < hexStr.length() - 1; i += 2) {
            int val;
            try {
                val = Integer.parseInt(hexStr.substring(i, i + 2), 16);
            } catch (NumberFormatException e) {
                val = 0;
            }
            a.octets.write(val);
        }
        a.checkAndSetIsGbkEncoding();
        return a;
    }

    private void convertToBytes(String asciiEncoded) {
        int i = 0;
        while (i < asciiEncoded.length()) {
            char c = asciiEncoded.charAt(i);
            switch (c) {
                case '\\':
                    i++;
                    int val;
                    switch (asciiEncoded.charAt(i)) {
                        case '\"':
                            this.octets.write(34);
                            i++;
                            break;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                            val = asciiEncoded.charAt(i) - 48;
                            i++;
                            if (asciiEncoded.charAt(i) >= '0' && asciiEncoded.charAt(i) <= '7') {
                                val = ((val * 8) + asciiEncoded.charAt(i)) - 48;
                                i++;
                            }
                            if (asciiEncoded.charAt(i) >= '0' && asciiEncoded.charAt(i) <= '7') {
                                val = ((val * 8) + asciiEncoded.charAt(i)) - 48;
                                i++;
                            }
                            this.octets.write(val);
                            break;
                        case '\\':
                            this.octets.write(92);
                            i++;
                            break;
                        case 'e':
                            this.octets.write(27);
                            i++;
                            break;
                        case 'n':
                            this.octets.write(10);
                            i++;
                            break;
                        case 'r':
                            this.octets.write(13);
                            i++;
                            break;
                        case 't':
                            this.octets.write(9);
                            i++;
                            break;
                        case 'x':
                            i++;
                            if (i != asciiEncoded.length() && i + 2 <= asciiEncoded.length()) {
                                try {
                                    val = Integer.parseInt(asciiEncoded.substring(i, i + 2), 16);
                                } catch (NumberFormatException e) {
                                    val = -1;
                                }
                                if (val >= 0) {
                                    this.octets.write(val);
                                    i += 2;
                                    break;
                                }
                                val = Character.digit(asciiEncoded.charAt(i), 16);
                                if (val < 0) {
                                    break;
                                }
                                this.octets.write(val);
                                i++;
                                break;
                            }
                            Log.e(TAG, "convertToBytes met StringIndexOutOfBoundsException!! asciiEncoded:" + asciiEncoded);
                            i++;
                            break;
                        default:
                            break;
                    }
                default:
                    this.octets.write(c);
                    i++;
                    break;
            }
        }
        checkAndSetIsGbkEncoding();
    }

    public String toString() {
        byte[] ssidBytes = this.octets.toByteArray();
        if (this.octets.size() <= 0 || isArrayAllZeroes(ssidBytes)) {
            return "";
        }
        boolean DBG = SystemProperties.get("persist.wifi.gbk.debug").equals(WifiEnterpriseConfig.ENGINE_ENABLE);
        boolean ssidGbkEncoding = SystemProperties.get("persist.wifi.gbk.encoding").equals(WifiEnterpriseConfig.ENGINE_ENABLE);
        Charset charset = Charset.forName("UTF-8");
        if (ssidGbkEncoding || this.mIsGbkEncoding) {
            charset = Charset.forName("GB2312");
        }
        CharsetDecoder decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        CharBuffer out = CharBuffer.allocate(32);
        CoderResult result = decoder.decode(ByteBuffer.wrap(ssidBytes), out, true);
        out.flip();
        if (result.isError()) {
            return NONE;
        }
        if (DBG) {
            Log.d(TAG, "persist.wifi.gbk.encoding: " + ssidGbkEncoding + ", isGbk: " + this.mIsGbkEncoding + ", toString: " + out.toString());
        }
        return out.toString();
    }

    private boolean isArrayAllZeroes(byte[] ssidBytes) {
        for (byte b : ssidBytes) {
            if (b != (byte) 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isHidden() {
        return isArrayAllZeroes(this.octets.toByteArray());
    }

    public byte[] getOctets() {
        return this.octets.toByteArray();
    }

    public String getHexString() {
        String out = "0x";
        byte[] ssidbytes = getOctets();
        for (int i = 0; i < this.octets.size(); i++) {
            StringBuilder append = new StringBuilder().append(out);
            Object[] objArr = new Object[1];
            objArr[0] = Byte.valueOf(ssidbytes[i]);
            out = append.append(String.format(Locale.US, "%02x", objArr)).toString();
        }
        return this.octets.size() > 0 ? out : null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.octets.size());
        dest.writeByteArray(this.octets.toByteArray());
        dest.writeInt(this.mIsGbkEncoding ? 1 : 0);
    }

    private static boolean isGBK(byte[] byteArray, int ssidStartPos, int ssidEndPos) {
        boolean DBG = SystemProperties.get("persist.wifi.gbk.debug").equals(WifiEnterpriseConfig.ENGINE_ENABLE);
        if (isNotUtf8(byteArray, ssidStartPos, ssidEndPos)) {
            if (DBG) {
                Log.d(TAG, "is not utf8");
            }
            return true;
        }
        if (DBG) {
            Log.d(TAG, "is utf8 format");
        }
        return false;
    }

    private static boolean isNotUtf8(byte[] input, int ssidStartPos, int ssidEndPos) {
        int nBytes = 0;
        int lastWildcar = 0;
        int Utf_bit = 0;
        int Utf_char_H = 0;
        boolean isAllAscii = true;
        boolean isAllGBK = true;
        boolean isWildcardChar = false;
        int i = ssidStartPos;
        while (i < ssidEndPos && i < input.length) {
            byte chr = input[i];
            if (isASCII(chr)) {
                isWildcardChar = false;
            } else {
                isAllAscii = false;
                isWildcardChar = !isWildcardChar;
                if (isWildcardChar && i < input.length - 1 && !isGBKChar(chr, input[i + 1])) {
                    isAllGBK = false;
                }
            }
            if (nBytes == 0) {
                if ((chr & 255) >= 128) {
                    lastWildcar = i;
                    nBytes = getUtf8CharLen(chr);
                    if (nBytes == 0) {
                        return true;
                    }
                    if (nBytes == 6) {
                        Utf_bit = 5;
                    } else if (nBytes == 5) {
                        Utf_bit = 4;
                    } else if (nBytes == 4) {
                        Utf_bit = 3;
                    } else if (nBytes == 3) {
                        Utf_bit = 2;
                    } else if (nBytes == 2) {
                        Utf_bit = 2;
                    }
                    Utf_char_H = chr;
                    nBytes--;
                } else {
                    continue;
                }
            } else if ((chr & 192) != 128) {
                break;
            } else {
                byte Utf_char_L;
                if (nBytes == 5 && Utf_bit == 5) {
                    Utf_char_L = chr;
                    Utf_bit = 0;
                    if ((Utf_char_H & 1) == 0 && (chr & 255) < 132) {
                        return true;
                    }
                } else if (nBytes == 4 && Utf_bit == 4) {
                    Utf_char_L = chr;
                    Utf_bit = 0;
                    if ((Utf_char_H & 3) == 0 && (chr & 255) < 136) {
                        return true;
                    }
                } else if (nBytes == 3 && Utf_bit == 3) {
                    Utf_char_L = chr;
                    Utf_bit = 0;
                    if ((Utf_char_H & 7) == 0 && (chr & 255) < 144) {
                        return true;
                    }
                } else if (nBytes == 2 && Utf_bit == 2) {
                    Utf_char_L = chr;
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
        if (nBytes <= 0) {
            return false;
        }
        if (isAllAscii) {
            return false;
        }
        if (isAllGBK) {
            return true;
        }
        nBytes = getUtf8CharLen(input[lastWildcar]);
        int j = lastWildcar;
        while (j < lastWildcar + nBytes && j < input.length) {
            if (!isASCII(input[j])) {
                input[j] = (byte) 32;
            }
            j++;
        }
        return false;
    }

    private static int getUtf8CharLen(byte firstByte) {
        int nBytes;
        if (firstByte >= (byte) -4 && firstByte <= (byte) -3) {
            nBytes = 6;
        } else if (firstByte >= (byte) -8) {
            nBytes = 5;
        } else if (firstByte >= (byte) -16) {
            nBytes = 4;
        } else if (firstByte >= (byte) -32) {
            nBytes = 3;
        } else if (firstByte < (byte) -64) {
            return 0;
        } else {
            nBytes = 2;
        }
        return nBytes;
    }

    private static boolean isASCII(byte b) {
        if ((b & 128) == 0) {
            return true;
        }
        return false;
    }

    private static boolean isGBKChar(byte head, byte tail) {
        int b0 = head & 255;
        int b1 = tail & 255;
        if ((b0 < 161 || b0 > 169 || b1 < 161 || b1 > 254) && ((b0 < 176 || b0 > R.styleable.Theme_buttonBarNegativeButtonStyle || b1 < 161 || b1 > 254) && ((b0 < 129 || b0 > 160 || b1 < 64 || b1 > 254) && ((b0 < 170 || b0 > 254 || b1 < 64 || b1 > 160 || b1 == 127) && ((b0 < 168 || b0 > 169 || b1 < 64 || b1 > 160 || b1 == 127) && ((b0 < 170 || b0 > 175 || b1 < 161 || b1 > 254 || b1 == 127) && ((b0 < R.styleable.Theme_actionBarPopupTheme || b0 > 254 || b1 < 161 || b1 > 254) && (b0 < 161 || b0 > 167 || b1 < 64 || b1 > 160 || b1 == 127)))))))) {
            return false;
        }
        return true;
    }

    private void checkAndSetIsGbkEncoding() {
        byte[] ssidBytes = this.octets.toByteArray();
        this.mIsGbkEncoding = isGBK(ssidBytes, 0, ssidBytes.length);
    }

    public boolean isGBK() {
        return this.mIsGbkEncoding;
    }
}
