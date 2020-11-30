package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.AlarmManagerService;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class ColorAlarmTempWhitelist implements IColorAlarmTempWhitelist {
    private static final boolean DEBUG_PANIC = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG_PANIC);
    public static final String TAG = "ColorAlarmTempWhitelist";
    private static volatile ColorAlarmTempWhitelist mInstance = null;
    private ArrayList<String> mGlobalRestrictTempWhitelist = new ArrayList<>();

    public static ColorAlarmTempWhitelist getInstance() {
        if (mInstance == null) {
            synchronized (ColorAlarmTempWhitelist.class) {
                if (mInstance == null) {
                    mInstance = new ColorAlarmTempWhitelist();
                }
            }
        }
        return mInstance;
    }

    public boolean addRestrictTempWhitelist(String pkgName) {
        synchronized (this.mGlobalRestrictTempWhitelist) {
            if (this.mGlobalRestrictTempWhitelist.contains(pkgName)) {
                return DEBUG_PANIC;
            }
            return this.mGlobalRestrictTempWhitelist.add(pkgName);
        }
    }

    public boolean removeRestrictTempWhitelist(String pkgName) {
        boolean remove;
        synchronized (this.mGlobalRestrictTempWhitelist) {
            remove = this.mGlobalRestrictTempWhitelist.remove(pkgName);
        }
        return remove;
    }

    public boolean isAlarmTempWhitelist(PendingIntent operation, String callingPackage, int flags, AlarmManager.AlarmClockInfo alarmClock) {
        boolean tempWhitelist = DEBUG_PANIC;
        if ((flags & 1) != 0 && alarmClock == null) {
            String sourcePkg = callingPackage;
            if (operation != null) {
                sourcePkg = operation.getCreatorPackage();
            }
            synchronized (this.mGlobalRestrictTempWhitelist) {
                tempWhitelist = this.mGlobalRestrictTempWhitelist.contains(sourcePkg);
            }
            if (DEBUG_PANIC && tempWhitelist) {
                Slog.d(TAG, sourcePkg + " is in temp whitelist when set exact");
            }
        }
        return tempWhitelist;
    }

    public boolean isAlarmTempWhitelist(AlarmManagerService.Alarm a, boolean interactive) {
        boolean tempWhitelist = DEBUG_PANIC;
        if (!interactive && (a.flags & 1) == 0) {
            synchronized (this.mGlobalRestrictTempWhitelist) {
                tempWhitelist = this.mGlobalRestrictTempWhitelist.contains(a.sourcePackage);
                if (DEBUG_PANIC && tempWhitelist) {
                    Slog.d(TAG, a.sourcePackage + " is in temp whitelist when align");
                }
            }
        }
        return tempWhitelist;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length >= 1 && "tempWhiteList".equals(args[0])) {
            pw.println("Global restrict tempwhitelist:");
            synchronized (this.mGlobalRestrictTempWhitelist) {
                Iterator<String> it = this.mGlobalRestrictTempWhitelist.iterator();
                while (it.hasNext()) {
                    pw.println(it.next());
                }
            }
        }
    }
}
