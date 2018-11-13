package com.android.server.secrecy.policy.util;

import android.os.Build;
import android.os.OppoUsageManager;
import android.util.Log;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class Utils {
    private static String PATH_OF_DOWNLOAD_TIME_IN_MTK_PLATFORM = null;
    private static String PATTERN_FOR_DATE = null;
    private static String PATTERN_FOR_TIME = null;
    private static final int SECITON_DOWNLOAD_STATUS = 1;
    private static final String TAG = "SecrecyService.Utils";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.policy.util.Utils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.policy.util.Utils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.secrecy.policy.util.Utils.<clinit>():void");
    }

    public static String getDownloadStatusString() {
        if (isQCOMPlatform()) {
            return getDownlodStatusFromUsageService();
        }
        return readStringFromFile(PATH_OF_DOWNLOAD_TIME_IN_MTK_PLATFORM);
    }

    private static String getDownlodStatusFromUsageService() {
        OppoUsageManager usageManager = OppoUsageManager.getOppoUsageManager();
        String downloadStatus = "none";
        if (usageManager != null) {
            return usageManager.getDownloadStatusString(1);
        }
        return downloadStatus;
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
        File file = new File(path);
        BufferedReader reader = null;
        String tempString = IElsaManager.EMPTY_PACKAGE;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(file));
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
        String dateStr = IElsaManager.EMPTY_PACKAGE;
        while (dateMatcher.find()) {
            dateStr = dateMatcher.group();
            Log.d(TAG, "dateMatcher = " + dateStr);
        }
        return dateStr;
    }

    public static String getDownloadTime(String downloadStatus) {
        Matcher timeMatcher = Pattern.compile(PATTERN_FOR_TIME).matcher(downloadStatus);
        String timeStr = IElsaManager.EMPTY_PACKAGE;
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
