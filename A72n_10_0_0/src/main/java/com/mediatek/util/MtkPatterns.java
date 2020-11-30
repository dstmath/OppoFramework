package com.mediatek.util;

import android.os.Bundle;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MtkPatterns {
    public static final String KEY_URLDATA_END = "end";
    public static final String KEY_URLDATA_START = "start";
    public static final String KEY_URLDATA_VALUE = "value";
    private static final String[] MTK_WEB_PROTOCOL_NAMES = {"http://", "https://", "rtsp://", "ftp://"};
    private static final String TAG = "MtkPatterns";
    private static final String mBadEndRemovingRegex = String.format("([\\.\\:][%s)]+[/%s]*)([\\.\\:]?[^%s\\.\\:\\s/]+[^\\.=&%%/]*$)", mValidCharRegex, mValidCharRegex, mValidCharRegex);
    private static final String mBadFrontRemovingRegex = String.format("(^[^.]*[^%s.://#&=]+)(?:[a-zA-Z]+://|[%s]+.)", mValidCharRegex, mValidCharRegex);
    private static final String mValidCharRegex = "a-zA-Z0-9\\-_";

    public static String[] getWebProtocolNames(String[] defaultProtocols) {
        return MTK_WEB_PROTOCOL_NAMES;
    }

    private static final String replaceGroup(String regex, String source, int groupToReplace, String replacement) {
        return replaceGroup(regex, source, groupToReplace, 1, replacement);
    }

    private static final String replaceGroup(String regex, String source, int groupToReplace, int groupOccurrence, String replacement) {
        Matcher m = Pattern.compile(regex).matcher(source);
        for (int i = 0; i < groupOccurrence; i++) {
            if (!m.find()) {
                return source;
            }
        }
        return new StringBuilder(source).replace(m.start(groupToReplace), m.end(groupToReplace), replacement).toString();
    }

    public static Bundle getWebUrl(String urlStr, int start, int end) {
        Log.d("@M_MtkPatterns", "getWebUrl,  start=" + start + " end=" + end);
        if (urlStr != null) {
            if (Pattern.compile(mBadFrontRemovingRegex).matcher(urlStr).find()) {
                urlStr = replaceGroup(mBadFrontRemovingRegex, urlStr, 1, "");
                start = end - urlStr.length();
            }
            if (Pattern.compile(mBadEndRemovingRegex).matcher(urlStr).find()) {
                urlStr = replaceGroup(mBadEndRemovingRegex, urlStr, 2, "");
                end = start + urlStr.length();
            }
        }
        Bundle data = new Bundle();
        data.putString(KEY_URLDATA_VALUE, urlStr);
        data.putInt(KEY_URLDATA_START, start);
        data.putInt(KEY_URLDATA_END, end);
        return data;
    }

    public static final Pattern getMtkWebUrlPattern(Pattern defaultPattern) {
        return ChinaPatterns.CHINA_AUTOLINK_WEB_URL;
    }
}
