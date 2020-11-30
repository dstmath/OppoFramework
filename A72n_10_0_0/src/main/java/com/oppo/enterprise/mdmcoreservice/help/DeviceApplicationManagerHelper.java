package com.oppo.enterprise.mdmcoreservice.help;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DeviceApplicationManagerHelper {
    private static final DeviceApplicationManagerHelper sDeviceApplicationManagerHelper = new DeviceApplicationManagerHelper();
    private final Uri AUTHORITY_URI = Uri.parse("content://com.color.provider.SafeProvider");
    private final int BACKGROUND = 0;
    private final int CALIBRATED_YES = 1;
    private final String COLUMN_APP_FOREGROUND = "app_foreground";
    private final String COLUMN_PACKAGE = "app_package";
    private final String COLUMN_TIME_CALIBRATED = "time_calibrated";
    private final String COLUMN_USED_STAMP = "used_time_stamp";
    private final int FOREGROUND = 1;
    private final String SELF_PACKAGE_SAFEPROVIDER = "com.color.provider.SafeProvider";
    private final Uri URI_APP_USAGE = Uri.withAppendedPath(this.AUTHORITY_URI, "app_usage");

    private DeviceApplicationManagerHelper() {
    }

    public static DeviceApplicationManagerHelper getInstance() {
        return sDeviceApplicationManagerHelper;
    }

    public List getAppRunInfo(Context context, ActivityManager activityManager, PackageManager packageManager) {
        Exception e;
        List<PackageInfo> infos = packageManager.getInstalledPackages(0);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        List<String[]> result = new ArrayList<>();
        HashMap<String, ArrayList<UsedStamp>> hashMap = getUsedStampList(context);
        try {
            for (PackageInfo info : infos) {
                long usedTime = calculateAppUsedTime(hashMap.get(info.packageName), System.currentTimeMillis());
                try {
                    String uid = getUid(context, info.packageName);
                    Iterator<ActivityManager.RunningAppProcessInfo> it = list.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ActivityManager.RunningAppProcessInfo instance = it.next();
                        if (info.packageName.equals(instance.processName)) {
                            result.add(new String[]{String.valueOf(instance.pid), String.valueOf(instance.uid), instance.processName, String.valueOf(usedTime)});
                            break;
                        }
                    }
                    result.add(new String[]{"", uid, info.packageName, String.valueOf(usedTime)});
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();
                    return result;
                }
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            return result;
        }
        return result;
    }

    public String getUid(Context context, String pakcageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pakcageName, 4096);
            if (packageInfo == null || packageInfo.applicationInfo == null) {
                return "";
            }
            return String.valueOf(packageInfo.applicationInfo.uid);
        } catch (Exception e) {
            return "";
        }
    }

    public HashMap<String, ArrayList<UsedStamp>> getUsedStampList(Context context) {
        Exception ee;
        HashMap<String, ArrayList<UsedStamp>> hashMap = new HashMap<>();
        ArrayList<UsedStamp> usedStampList = new ArrayList<>();
        Cursor cursor = null;
        try {
            Cursor cursor2 = context.getContentResolver().query(this.URI_APP_USAGE, new String[]{"used_time_stamp", "app_foreground", "app_package"}, "time_calibrated=? ", new String[]{String.valueOf(1)}, null);
            if (cursor2 != null && cursor2.getCount() > 0) {
                while (cursor2.moveToNext()) {
                    UsedStamp usedStamp = new UsedStamp();
                    usedStamp.mStamp = cursor2.getLong(cursor2.getColumnIndex("used_time_stamp"));
                    usedStamp.mForeground = cursor2.getInt(cursor2.getColumnIndex("app_foreground"));
                    usedStamp.packageName = cursor2.getString(cursor2.getColumnIndex("app_package"));
                    int lastIndex = usedStampList.size() - 1;
                    if (lastIndex < 0 || usedStamp.mForeground != usedStampList.get(lastIndex).mForeground) {
                        usedStampList.add(usedStamp);
                        if (hashMap.get(usedStamp.packageName) == null) {
                            hashMap.put(usedStamp.packageName, new ArrayList<>());
                        }
                        hashMap.get(usedStamp.packageName).add(usedStamp);
                    }
                }
                usedStampList.clear();
            }
            if (cursor2 != null) {
                cursor2.close();
                cursor2 = null;
            }
            if (cursor2 != null) {
                try {
                    cursor2.close();
                } catch (Exception e) {
                    ee = e;
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (Exception e3) {
                    ee = e3;
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (Exception ee2) {
                    ee2.printStackTrace();
                }
            }
            throw th;
        }
        return hashMap;
        ee.printStackTrace();
        return hashMap;
    }

    public long calculateAppUsedTime(ArrayList<UsedStamp> list, long curTime) {
        long usedTime = 0;
        if (list != null && !list.isEmpty()) {
            int count = list.size();
            UsedStamp lastUsedStamp = null;
            for (int i = 0; i < count; i++) {
                UsedStamp curStamp = list.get(i);
                if (curStamp.mForeground == 1) {
                    usedTime -= curStamp.mStamp - 0;
                } else if (curStamp.mForeground == 0 && i != 0) {
                    usedTime += curStamp.mStamp - 0;
                }
                lastUsedStamp = curStamp;
            }
            if (lastUsedStamp != null && lastUsedStamp.mForeground == 1) {
                usedTime += curTime - 0;
            }
        }
        if (usedTime < 0) {
            return 0;
        }
        return usedTime;
    }

    public static class UsedStamp {
        public int mForeground;
        public long mStamp;
        public String packageName;

        public String toString() {
            return "packageName =" + this.packageName + " mStamp=" + this.mStamp + " mForeground=" + this.mForeground + "\n";
        }
    }
}
