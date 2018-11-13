package com.android.server.am;

import android.app.ApplicationErrorReport.CrashInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.server.LocationManagerService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class ColorEapUtils {
    private static final int MAX_SIZE = 10240;
    private static final String TAG = "ColorEapUtils";

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0074  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getStackTrace(File dataFile) {
        Exception e;
        Throwable th;
        String result;
        if (dataFile == null) {
            return "";
        }
        BufferedReader reader = null;
        StringBuilder info = new StringBuilder();
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(dataFile);
            try {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(stream2));
                try {
                    String startText = "\"main\" prio=";
                    String line = "";
                    boolean flag = true;
                    boolean isFindText = false;
                    while (true) {
                        line = reader2.readLine();
                        if (line == null || !flag) {
                            try {
                                reader2.close();
                                stream2.close();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            stream = stream2;
                            break;
                        }
                        if (line.contains(startText)) {
                            isFindText = true;
                        }
                        if (isFindText) {
                            if (line.startsWith("  at ")) {
                                info.append(line);
                                Log.d(TAG, "append:" + line);
                            } else if (line.equals("")) {
                                flag = false;
                            }
                        }
                    }
                } catch (Exception e3) {
                    e2 = e3;
                    stream = stream2;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    stream = stream2;
                    reader = reader2;
                }
            } catch (Exception e4) {
                e2 = e4;
                stream = stream2;
                try {
                    e2.printStackTrace();
                    try {
                        reader.close();
                        stream.close();
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                    result = info.toString();
                    if (result.length() > MAX_SIZE) {
                    }
                    Log.d(TAG, "trace info:" + result);
                    return result;
                } catch (Throwable th3) {
                    th = th3;
                    try {
                        reader.close();
                        stream.close();
                    } catch (Exception e222) {
                        e222.printStackTrace();
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                stream = stream2;
                reader.close();
                stream.close();
                throw th;
            }
        } catch (Exception e5) {
            e222 = e5;
            e222.printStackTrace();
            reader.close();
            stream.close();
            result = info.toString();
            if (result.length() > MAX_SIZE) {
            }
            Log.d(TAG, "trace info:" + result);
            return result;
        }
        result = info.toString();
        if (result.length() > MAX_SIZE) {
            result = result.substring(0, MAX_SIZE);
        }
        Log.d(TAG, "trace info:" + result);
        return result;
    }

    public static void collectErrorInfo(Context context, String dropboxTag, String eventType, ProcessRecord process, String packageName, String timeInfo, ActivityRecord activity, String subject, File dataFile, CrashInfo crashInfo) {
        final String str = dropboxTag;
        final String str2 = eventType;
        final String str3 = packageName;
        final String str4 = timeInfo;
        final ProcessRecord processRecord = process;
        final ActivityRecord activityRecord = activity;
        final String str5 = subject;
        final File file = dataFile;
        final CrashInfo crashInfo2 = crashInfo;
        final Context context2 = context;
        new Thread("EAP") {
            public void run() {
                try {
                    if (str.contains("app_crash") || str.contains("app_anr")) {
                        Intent errorIntent = new Intent("oppo.intent.action.EAP_APP_ERROR");
                        errorIntent.setFlags(67108864);
                        errorIntent.putExtra("eventType", str2);
                        errorIntent.putExtra("processName", str3);
                        errorIntent.putExtra("fileId", str4);
                        if (processRecord != null) {
                            errorIntent.putExtra("foreground", processRecord.isInterestingToUserLocked());
                        }
                        if (activityRecord != null) {
                            errorIntent.putExtra(OppoAppStartupManager.TYPE_ACTIVITY, activityRecord.shortComponentName);
                        }
                        if ("anr".equals(str2)) {
                            errorIntent.putExtra("message", str5);
                            errorIntent.putExtra("stackTrace", ColorEapUtils.getStackTrace(file));
                        }
                        if ("crash".equals(str2) && crashInfo2 != null) {
                            errorIntent.putExtra("className", crashInfo2.exceptionClassName);
                            errorIntent.putExtra("message", crashInfo2.exceptionMessage);
                            errorIntent.putExtra("stackTrace", crashInfo2.stackTrace);
                        }
                        context2.sendBroadcast(errorIntent);
                    }
                } catch (Exception e) {
                    Log.e(ColorEapUtils.TAG, "fail to collect app error info, " + e);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static String getTimeInfo() {
        long time = System.currentTimeMillis();
        Random random = new Random();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            buf.append(random.nextInt(10));
        }
        return buf.toString() + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + time;
    }
}
