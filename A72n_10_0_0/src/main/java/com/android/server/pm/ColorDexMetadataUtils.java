package com.android.server.pm;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.ColorAppCrashClearManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class ColorDexMetadataUtils {
    private static final String TAG = "ColorDexMetadataUtils";

    private ColorDexMetadataUtils() {
    }

    public static boolean isScreenOn() {
        try {
            return IPowerManager.Stub.asInterface(ServiceManager.getService("power")).isInteractive();
        } catch (RemoteException e) {
            Slog.e(TAG, "getting screen status error", e);
            return false;
        }
    }

    public static boolean isFrequentUsageApp(Context context, String packageName, int month, int number) {
        List<Map.Entry<String, Integer>> usageStatsList = getUsageStatisticsList(context, month, number);
        if (usageStatsList == null || usageStatsList.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, Integer> entry : usageStatsList) {
            if (entry != null && entry.getKey().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private static List<Map.Entry<String, Integer>> getUsageStatisticsList(Context context, int month, int number) {
        List<Map.Entry<String, Integer>> list;
        if (context == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(2, month);
        List<UsageStats> usageStatsList = ((UsageStatsManager) context.getSystemService("usagestats")).queryUsageStats(2, calendar.getTimeInMillis(), System.currentTimeMillis());
        if (usageStatsList == null) {
            list = null;
        } else if (usageStatsList.size() == 0) {
            list = null;
        } else {
            Map<String, Integer> packageUsageMap = new TreeMap<>();
            List<PackageInfo> installedPackageList = context.getPackageManager().getInstalledPackages(0);
            for (UsageStats usageStats : usageStatsList) {
                String packageName = usageStats.getPackageName();
                if (!isSystemApp(context, packageName, installedPackageList)) {
                    packageUsageMap.put(packageName, Integer.valueOf(usageStats.getAppLaunchCount()));
                }
            }
            Comparator<Map.Entry<String, Integer>> valueComparator = new Comparator<Map.Entry<String, Integer>>() {
                /* class com.android.server.pm.ColorDexMetadataUtils.AnonymousClass1 */

                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue().intValue() - o1.getValue().intValue();
                }
            };
            List<Map.Entry<String, Integer>> list2 = new ArrayList<>(packageUsageMap.entrySet());
            Collections.sort(list2, valueComparator);
            if (list2.size() <= number) {
                return list2;
            }
            try {
                return list2.subList(0, number);
            } catch (Exception e) {
                Slog.e(TAG, "Exception: ", e);
                return null;
            }
        }
        Slog.w(TAG, "the user may not allow the access to apps usage");
        return list;
    }

    private static boolean isSystemApp(Context context, String packageName, List<PackageInfo> installedPackageList) {
        if (packageName == null || !isInstalledApp(packageName, installedPackageList)) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
            if (info == null || (info.flags & 1) == 0) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "NameNotFoundException: ", e);
            return false;
        }
    }

    private static boolean isInstalledApp(String packageName, List<PackageInfo> installedPackageList) {
        if (!(installedPackageList == null || installedPackageList.isEmpty() || packageName == null)) {
            for (PackageInfo packageInfo : installedPackageList) {
                if (packageInfo != null && packageName.equals(packageInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            try {
                if (!file.delete()) {
                    Slog.w(TAG, "delete file failed at: " + file.getPath());
                }
            } catch (Exception e) {
                Slog.e(TAG, "Exception: ", e);
            }
        }
    }

    public static ArraySet<String> readFromXmlFile(File file) {
        StringBuilder sb;
        int type;
        String pkg;
        ArraySet<String> pkgs = new ArraySet<>();
        if (file == null || !file.exists()) {
            return pkgs;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (pkg = parser.getAttributeValue(null, "attr")) != null) {
                    pkgs.add(pkg);
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (Exception e2) {
            Slog.e(TAG, "Exception: ", e2);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "IOException: " + e4);
                }
            }
            throw th;
        }
        return pkgs;
        sb.append("IOException: ");
        sb.append(e);
        Slog.e(TAG, sb.toString());
        return pkgs;
    }

    public static void saveAsXmlFile(File file, ArraySet<String> pkgs) {
        StringBuilder sb;
        if (file != null && file.exists() && pkgs != null && !pkgs.isEmpty()) {
            FileOutputStream stream = null;
            try {
                FileOutputStream stream2 = new FileOutputStream(file);
                XmlSerializer out = Xml.newSerializer();
                out.setOutput(stream2, "utf-8");
                out.startDocument(null, true);
                out.startTag(null, BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                Iterator<String> it = pkgs.iterator();
                while (it.hasNext()) {
                    String pkg = it.next();
                    if (pkg != null) {
                        out.startTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                        out.attribute(null, "attr", pkg);
                        out.endTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                    }
                }
                out.endTag(null, BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                out.endDocument();
                try {
                    stream2.close();
                    return;
                } catch (IOException e) {
                    e = e;
                    sb = new StringBuilder();
                }
            } catch (Exception e2) {
                Slog.e(TAG, "Exception: " + e2);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        Slog.e(TAG, "IOException: " + e4);
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("IOException: ");
        sb.append(e);
        Slog.e(TAG, sb.toString());
    }

    public static String readFromFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream2 = new FileInputStream(file);
            BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
            StringBuffer buffer = new StringBuffer();
            while (true) {
                String line = bufferedReader2.readLine();
                if (line == null) {
                    break;
                }
                buffer.append(line + "\n");
            }
            String stringBuffer = buffer.toString();
            try {
                inputStream2.close();
                bufferedReader2.close();
            } catch (IOException e) {
                Slog.e(TAG, "IOException: ", e);
            }
            return stringBuffer;
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "FileNotFoundException: ", e2);
            if (0 != 0) {
                inputStream.close();
            }
            if (0 != 0) {
                bufferedReader.close();
            }
        } catch (IOException e3) {
            Slog.e(TAG, "IOException: ", e3);
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "IOException: ", e4);
                    return null;
                }
            }
            if (0 != 0) {
                bufferedReader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "IOException: ", e5);
                    throw th;
                }
            }
            if (0 != 0) {
                bufferedReader.close();
            }
            throw th;
        }
    }

    public static boolean saveToFile(String content, String filePath) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(filePath));
            fileOutputStream.write(content.getBytes());
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                Slog.e(TAG, "IOException: ", e);
            }
            return true;
        } catch (Exception e2) {
            Slog.e(TAG, "Exception: ", e2);
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "IOException: ", e3);
                }
            }
            return false;
        } catch (Throwable th) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "IOException: ", e4);
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x006d  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0083 A[LOOP:1: B:27:0x0083->B:41:0x00c2, LOOP_START, PHI: r5 r7 
      PHI: (r5v2 'i' int) = (r5v1 'i' int), (r5v3 'i' int) binds: [B:24:0x006b, B:41:0x00c2] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r7v1 char) = (r7v0 char), (r7v3 char) binds: [B:24:0x006b, B:41:0x00c2] A[DONT_GENERATE, DONT_INLINE]] */
    public static boolean isValidPackageName(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        int length = packageName.length();
        boolean front = true;
        int i = 0;
        while (true) {
            char c = '9';
            if (i < length && packageName.charAt(i) != '-') {
                char c2 = packageName.charAt(i);
                if ((c2 >= 'a' && c2 <= 'z') || (c2 >= 'A' && c2 <= 'Z')) {
                    front = false;
                } else if (front || ((c2 < '0' || c2 > '9') && c2 != '_')) {
                    if (c2 == '.') {
                        front = true;
                    } else {
                        Slog.w(TAG, "bad character '" + c2 + "' in " + packageName);
                        return false;
                    }
                }
                i++;
            } else if (!front) {
                Slog.w(TAG, "missing separator in " + packageName);
                return false;
            } else {
                while (i < length) {
                    char c3 = packageName.charAt(i);
                    if ((c3 < 'a' || c3 > 'z') && ((c3 < 'A' || c3 > 'Z') && !((c3 >= '0' && c3 <= c) || c3 == '_' || c3 == '-' || c3 == '='))) {
                        Slog.w(TAG, "bad suffix character '" + c3 + "' in " + packageName);
                        return false;
                    }
                    i++;
                    c = '9';
                }
                return true;
            }
        }
        if (!front) {
        }
    }
}
