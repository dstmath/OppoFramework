package com.android.server.secrecy.policy.util;

import android.os.Build;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static String PATH_OF_DOWNLOAD_TIME_IN_MTK_PLATFORM = "/proc/oppoCustom/DownloadTime";
    private static String PATTERN_FOR_DATE = "[1-9][0-9]{3}-[0-9]{2}-[0-9]{2}";
    private static String PATTERN_FOR_TIME = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
    private static final int SECITON_DOWNLOAD_STATUS = 1;
    private static final String TAG = "SecrecyService.Utils";

    public static String getDownloadStatusString() {
        String status;
        if (isQCOMPlatform()) {
            status = getQcomDownloadStatusString();
        } else {
            status = readStringFromFile(PATH_OF_DOWNLOAD_TIME_IN_MTK_PLATFORM);
        }
        return status == null ? "" : status;
    }

    public static String getQcomDownloadStatusString() {
        String status = "";
        try {
            status = (String) Class.forName("android.engineer.OppoEngineerManager").getMethod("getDownloadStatus", new Class[0]).invoke(null, new Object[0]);
        } catch (Exception e) {
            Log.i(TAG, "getDownloadStatus failed Exception" + e.getMessage());
        }
        Log.i(TAG, "getDownloadStatus status = " + status);
        return status == null ? "" : status;
    }

    private static boolean isQCOMPlatform() {
        if (Build.HARDWARE.startsWith("qcom")) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x00ac A:{SYNTHETIC, Splitter: B:23:0x00ac} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String readStringFromFile(String path) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        String tempString = "";
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(path)));
            try {
                tempString = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e1) {
                        Log.e(TAG, "readStringFromFile io close exception :" + e1.getMessage());
                    }
                }
                reader = reader2;
            } catch (IOException e2) {
                e = e2;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            try {
                Log.e(TAG, "readStringFromFile io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Log.e(TAG, "readStringFromFile io close exception :" + e12.getMessage());
                    }
                }
                Log.i(TAG, "readStringFromFile path:" + path + ", result:" + tempString);
                return tempString;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Log.e(TAG, "readStringFromFile io close exception :" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
        Log.i(TAG, "readStringFromFile path:" + path + ", result:" + tempString);
        return tempString;
    }

    public static long getFlashIimeInMillis(String downloadDate, String downloadTime) {
        String downloadExactTime = downloadDate + " " + downloadTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        long downloadExactTimeInMillis = 0;
        try {
            downloadExactTimeInMillis = dateFormat.parse(downloadExactTime).getTime();
            Log.d(TAG, "downloadTime = " + downloadExactTimeInMillis);
            return downloadExactTimeInMillis;
        } catch (ParseException e) {
            e.printStackTrace();
            return downloadExactTimeInMillis;
        }
    }

    public static String getDownloadDate(String downloadStatus) {
        Matcher dateMatcher = Pattern.compile(PATTERN_FOR_DATE).matcher(downloadStatus);
        String dateStr = "";
        while (dateMatcher.find()) {
            dateStr = dateMatcher.group();
            Log.d(TAG, "dateMatcher = " + dateStr);
        }
        return dateStr;
    }

    public static String getDownloadTime(String downloadStatus) {
        Matcher timeMatcher = Pattern.compile(PATTERN_FOR_TIME).matcher(downloadStatus);
        String timeStr = "";
        while (timeMatcher.find()) {
            timeStr = timeMatcher.group();
            Log.d(TAG, "timeMatcher = " + timeStr);
        }
        return timeStr;
    }

    public static boolean isFlashedInternal(String downloadStatus) {
        return downloadStatus.indexOf("intranet") != -1;
    }
}
