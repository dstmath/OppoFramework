package com.android.server.am;

import android.app.ApplicationErrorReport;
import android.content.Context;
import android.os.DropBoxManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.IColorActivityRecordEx;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorEapManager implements IColorEapManager {
    public static final String TAG = "ColorEapManager";
    private static String mFileName;
    private static ColorEapManager sColorEapManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    protected ActivityManagerService mAms = null;
    protected IColorActivityManagerServiceEx mColorAmsEx = null;
    boolean mDynamicDebug = false;
    private String mErrorPkgName;
    private String mTimeInfo;

    private ColorEapManager() {
    }

    public static ColorEapManager getInstance() {
        if (sColorEapManager == null) {
            synchronized (ColorEapManager.class) {
                if (sColorEapManager == null) {
                    sColorEapManager = new ColorEapManager();
                }
            }
        }
        return sColorEapManager;
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mColorAmsEx = amsEx;
            this.mAms = amsEx.getActivityManagerService();
            ColorEapUtils.getInstance().initEapConfig();
        }
    }

    public void addTimeInfo(StringBuilder sb) {
        this.mTimeInfo = ColorEapUtils.getTimeInfo();
        sb.append("Time: ");
        sb.append(this.mTimeInfo);
        sb.append("\n");
    }

    public void setErrorPackageName(String pkg) {
        this.mErrorPkgName = pkg;
    }

    public void collectEapInfo(Context ctx, String tag, String type, ProcessRecord process, String subject, File dataFile, ApplicationErrorReport.CrashInfo crashInfo) {
        ColorEapUtils.getInstance().collectInfo(ctx, tag, type, process, this.mErrorPkgName, this.mTimeInfo, (IColorActivityRecordEx) null, subject, dataFile, crashInfo);
    }

    public int getDataFileSizeAjusted(int prevSize, int lineSize, File file) {
        ActivityManagerService activityManagerService = this.mAms;
        int maxFileSize = prevSize - (lineSize * 100);
        if (file != null) {
            return (int) file.length();
        }
        return maxFileSize;
    }

    public void appendCpuInfo(StringBuilder sb, String eventType) {
        try {
            if (this.mAms != null) {
                ActivityManagerService activityManagerService = this.mAms;
                if ("crash".equals(eventType)) {
                    long anrTime = SystemClock.uptimeMillis();
                    this.mAms.updateCpuStatsNow();
                    synchronized (this.mAms.mProcessCpuTracker) {
                        String cpuInfo = this.mAms.mProcessCpuTracker.printCurrentState(anrTime);
                        if (cpuInfo != null && cpuInfo.length() > 2000) {
                            cpuInfo = cpuInfo.substring(0, 2000);
                        }
                        sb.append("\n");
                        sb.append(cpuInfo);
                        sb.append("\n");
                    }
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "fail to add cpu info, " + e);
        }
    }

    public void setCrashProcessRecord(ProcessRecord processRecord) {
        ColorEapUtils.getInstance().setCrashProcessRecord(processRecord);
    }

    public void addEntryToEap(String tag, DropBoxManager.Entry entry) throws IOException {
        if (tag.contains("app_anr") || tag.contains("app_crash")) {
            String timeInfo = getTimeInfo(entry);
            String packageName = getPackageName(entry);
            int userId = getCallingUserId(entry, 300);
            Log.i(TAG, "UserId of entry: " + userId);
            ColorEapUtils.getInstance().collectFile(this.mAms.mContext, mFileName, packageName, tag, timeInfo, userId);
        }
        if (tag.equals("SYSTEM_TOMBSTONE")) {
            String timeInfo2 = ColorEapUtils.getTimeInfo();
            int userId2 = getCallingUserId(entry, 900);
            Log.i(TAG, "UserId of native entry: " + userId2);
            if (this.mAms.mContext != null) {
                ColorEapUtils.getInstance().readTombstoneFile(this.mAms.mContext, mFileName, entry.getInputStream(), timeInfo2, userId2);
            }
        }
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    private String getPackageName(DropBoxManager.Entry entry) {
        try {
            String text = entry.getText(500);
            String value = text.substring(text.indexOf("Package: ") + 9, text.indexOf("Foreground:") - 1);
            return value.substring(0, value.indexOf(" v"));
        } catch (Exception e) {
            Slog.d(TAG, "fail to init package name, " + e);
            return "null";
        }
    }

    private String getTimeInfo(DropBoxManager.Entry entry) {
        try {
            String info = entry.getText(300);
            return info.substring(info.indexOf("Time: ") + 6, info.indexOf("Flags:") - 1);
        } catch (Exception e) {
            Slog.d(TAG, "fail to init time value, " + e);
            return "0";
        }
    }

    private int getCallingUserId(DropBoxManager.Entry entry, int maxBytes) {
        int endIndex;
        String info = entry.getText(maxBytes);
        int startIndex = info.indexOf("UID: ");
        if (startIndex == -1) {
            startIndex = info.indexOf("uid: ");
        }
        if (startIndex == -1 || (endIndex = info.indexOf(10, startIndex)) == -1) {
            return 0;
        }
        String value = info.substring(startIndex + 5, endIndex).trim();
        try {
            return UserHandle.getUserId(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            Slog.d(TAG, "fail to get uid from value:" + value);
            return 0;
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog#### mDynamicDebug = " + this.mDynamicDebug);
        setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + this.mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorEapManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }
}
