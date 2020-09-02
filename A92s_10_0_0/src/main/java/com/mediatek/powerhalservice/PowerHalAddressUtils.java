package com.mediatek.powerhalservice;

import android.util.Log;
import java.util.regex.Pattern;

public class PowerHalAddressUtils {
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");
    private static final Pattern IPV6_IPV4_COMPATIBLE_PATTERN = Pattern.compile("^::[fF]{4}:(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
    private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
    private static final int IP_FORMAT_IPV4 = 1;
    private static final int IP_FORMAT_IPV6 = 2;
    private static final int IP_FORMAT_UNKONWN = 0;
    private static final String TAG = "PowerHalAddressUitls";

    private PowerHalAddressUtils() {
    }

    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv4SourceAddress(String input) {
        if (input.equals("0.0.0.0/0")) {
            return true;
        }
        if (!isIPv4Address(input) || input.equals("0.0.0.0")) {
            return false;
        }
        return true;
    }

    public static boolean isIPv4MulticastAddress(String input) {
        int ipStartValue;
        String[] items = input.split("\\.");
        if (items.length <= 0 || (ipStartValue = Integer.parseInt(items[0])) < 224 || ipStartValue > 239) {
            return false;
        }
        return true;
    }

    public static boolean isIPv6StdAddress(String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6HexCompressedAddress(String input) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6IPv4CompatibleAddress(String input) {
        return IPV6_IPV4_COMPATIBLE_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6Address(String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input) || isIPv6IPv4CompatibleAddress(input);
    }

    public static boolean isIPv6SourceAddress(String input) {
        return input.equals("::/0") || isIPv6Address(input);
    }

    public static boolean isIpPairValid(String src_ip, String dst_ip, int src_port, int dst_port) {
        int src_format;
        int dst_format;
        if (src_port < -1 || src_port > 65535 || dst_port < -1 || dst_port > 65535) {
            logd("invalid port:" + src_port + "," + dst_port);
            return false;
        }
        if (isIPv4SourceAddress(src_ip)) {
            src_format = 1;
        } else if (isIPv6SourceAddress(src_ip)) {
            src_format = 2;
        } else {
            logd("src unknown:" + src_ip);
            return false;
        }
        if (isIPv4Address(dst_ip)) {
            dst_format = 1;
        } else if (isIPv6Address(dst_ip)) {
            dst_format = 2;
        } else {
            logd("dst unknown:" + dst_ip);
            return false;
        }
        if (src_format != dst_format) {
            logd("not match:" + src_ip + "," + dst_ip);
            return false;
        }
        if (dst_format == 1) {
            if (dst_ip.startsWith("127")) {
                logd("violate: loopback address:" + dst_ip);
                return false;
            } else if (dst_ip.equals("255.255.255.255")) {
                logd("violate: broadcast:" + dst_ip);
                return false;
            } else if (isIPv4MulticastAddress(dst_ip)) {
                logd("violate: multicasting:" + dst_ip);
                return false;
            }
        }
        return true;
    }

    private static void log(String info) {
        Log.i(TAG, info + " ");
    }

    private static void logd(String info) {
        Log.d(TAG, info + " ");
    }

    private static void loge(String info) {
        Log.e(TAG, "ERR: " + info + " ");
    }
}
